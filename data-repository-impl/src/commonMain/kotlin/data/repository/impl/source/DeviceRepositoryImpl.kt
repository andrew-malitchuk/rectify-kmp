package data.repository.impl.source

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource
import data.device.api.source.model.ToolKind
import data.device.api.source.tuning.MirrorTuning
import data.repository.impl.core.mapper.DeviceResourceMapper
import data.repository.impl.core.mapper.EmulatorResourceMapper
import data.repository.impl.core.mapper.MirrorFrameMapper
import data.repository.impl.core.mapper.ToolProbeMapper
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.device.ToolModel
import domain.repository.api.source.repository.DeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Discovery and mirroring on top of the device datasources.
 *
 * @property adbHostSource Deviceless `adb` calls.
 * @property emulatorSource Deviceless `emulator` calls.
 * @property deviceCommandExecutor The device-scoped command path, used to ask a running
 *   emulator for its AVD name.
 * @property mirrorTransport Frame source for an active session.
 * @property nowMs Wall-clock reader. A parameter so the poll metadata is assertable under
 *   virtual time instead of needing a real two-second wait.
 */
internal class DeviceRepositoryImpl(
    private val adbHostSource: AdbHostSource,
    private val emulatorSource: EmulatorSource,
    private val deviceCommandExecutor: DeviceCommandExecutor,
    private val mirrorTransport: MirrorTransport,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : DeviceRepository {

    /** Last version `adb` reported, for the status bar of an active session. */
    private val adbVersion = MutableStateFlow("")

    override suspend fun detectTools(): List<ToolModel> {
        val adb = adbHostSource.probe()
        adbVersion.value = adb.version.orEmpty()
        return listOf(ToolProbeMapper.toModel.map(adb), iosNotProbed)
    }

    override fun observeDiscovery(): Flow<DiscoveryModel> = flow {
        emit(DiscoveryModel.Detecting)
        // Collector-local, so two collectors cannot race on it. A serial's AVD name cannot
        // change while it stays attached, which is what makes caching it safe - and worth
        // it, since the alternative is an extra child process per emulator per poll.
        val avdNames = mutableMapOf<String, AvdName>()
        var emptyPolls = 0

        while (true) {
            val probe = adbHostSource.probe()
            if (!probe.found) {
                // Not an error: a missing tool is a rendered state, so the counter does not
                // advance - we learned nothing about whether a device is attached.
                emptyPolls = 0
                emit(
                    DiscoveryModel.ToolMissing(
                        tool = ToolKind.Adb.binaryName,
                        probedPaths = probe.probedPaths,
                        installCommand = probe.installCommand,
                        envHint = probe.envHint,
                    )
                )
            } else {
                adbVersion.value = probe.version.orEmpty()
                val devices = adbHostSource.listDevices()
                avdNames.keys.retainAll(devices.mapTo(mutableSetOf()) { it.id.value })
                val usable = devices.count { it.state == DeviceStateResource.Device }
                emptyPolls = if (usable == 0) emptyPolls + 1 else 0

                emit(
                    if (emptyPolls >= MirrorTuning.EmptyPollEscalation) {
                        DiscoveryModel.NoDeviceFound(
                            toolVersion = probe.version.orEmpty(),
                            lastPollEpochMs = nowMs(),
                            attempts = emptyPolls,
                            attachedCount = devices.count { it.state != DeviceStateResource.Device },
                        )
                    } else {
                        DiscoveryModel.Found(
                            physical = devices.filterNot(DeviceResource::isEmulator)
                                .map(DeviceResourceMapper.toModel::map),
                            emulators = emulators(devices, avdNames),
                        )
                    }
                )
            }
            delay(MirrorTuning.DiscoveryPollMs)
        }
    }

    override suspend fun bootEmulator(avdName: String) {
        val avd = AvdName.of(avdName)
            ?: throw DeviceCommandException(listOf(ToolKind.Emulator.binaryName), null, "invalid AVD name $avdName")
        emulatorSource.boot(avd)
    }

    override fun mirror(deviceId: String): Flow<MirrorFrameModel> {
        val device = DeviceId.of(deviceId)
            ?: return flow {
                throw DeviceCommandException(listOf(ToolKind.Adb.binaryName), null, "invalid device id $deviceId")
            }
        return mirrorTransport.frames(device).map(MirrorFrameMapper.toModel::map)
    }

    override fun observeMirrorStats(): Flow<MirrorStatsModel> =
        combine(mirrorTransport.stats, adbVersion) { stats, version ->
            MirrorStatsModel(
                toolVersion = version,
                widthPx = stats.widthPx,
                heightPx = stats.heightPx,
                fps = stats.fps,
                latencyMs = stats.latencyMs,
                droppedFrames = stats.droppedFrames,
            )
        }

    /**
     * Configured AVDs merged with the running instances.
     *
     * An AVD that is both listed by `emulator -list-avds` and currently running appears
     * exactly once, marked running: the match is consumed out of [running] as it is made,
     * and only the leftovers (a running instance of an unlisted or unnamed AVD) are appended.
     */
    private suspend fun emulators(
        devices: List<DeviceResource>,
        avdNames: MutableMap<String, AvdName>,
    ): List<EmulatorModel> {
        val running = mutableMapOf<String, DeviceId>()
        val unnamed = mutableListOf<DeviceId>()
        devices.filter { it.isEmulator && it.state == DeviceStateResource.Device }
            .forEach { device ->
                when (val name = avdNameOf(device.id, avdNames)) {
                    null -> unnamed += device.id
                    else -> running[name.value] = device.id
                }
            }

        val listed = emulatorSource.listAvds()
            .map { avd -> AvdResource(name = avd.name, runningAs = running.remove(avd.name.value)) }
            .map(EmulatorResourceMapper.toModel::map)
        val unlisted = running.map { (name, id) ->
            EmulatorModel(avdName = name, isRunning = true, runningDeviceId = id.value)
        }
        val anonymous = unnamed.map { id ->
            EmulatorModel(avdName = id.value, isRunning = true, runningDeviceId = id.value)
        }
        return listed + unlisted + anonymous
    }

    /**
     * AVD name of a running emulator, asked once per attached serial.
     *
     * @return The name, or `null` when the instance will not answer - it is still listed,
     *   just under its serial, rather than disappearing from the picker.
     */
    private suspend fun avdNameOf(device: DeviceId, avdNames: MutableMap<String, AvdName>): AvdName? {
        avdNames[device.value]?.let { return it }
        val output = try {
            deviceCommandExecutor.run(device, DeviceCommand.EmuAvdName).decodeToString()
        } catch (failure: DeviceCommandException) {
            return null
        }
        val name = output.lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && it != "OK" }
            .firstNotNullOfOrNull(AvdName::of)
            ?: return null
        avdNames[device.value] = name
        return name
    }

    private companion object {
        /**
         * iOS tool entry.
         *
         * There is no idb datasource in this slice (FR-2 defers iOS), so the card reports
         * "not installed" with the install line rather than claiming a probe that never ran.
         */
        val iosNotProbed = ToolModel(
            platform = PlatformModel.Ios,
            isInstalled = false,
            installCommand = "brew tap facebook/fb && brew install idb-companion",
        )
    }
}
