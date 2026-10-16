package data.repository.impl.source

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IdbHostSource
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
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import data.repository.impl.core.mapper.DeviceResourceMapper
import data.repository.impl.core.mapper.EmulatorResourceMapper
import data.repository.impl.core.mapper.MirrorFrameMapper
import data.repository.impl.core.mapper.ToolProbeMapper
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
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
 * @property idbHostSource Deviceless `idb` calls — a probe and nothing else, which is the
 *   whole of iOS this release.
 * @property emulatorSource Deviceless `emulator` calls.
 * @property deviceCommandExecutor The device-scoped command path, used to ask a running
 *   emulator for its AVD name.
 * @property mirrorTransport Frame source for an active session.
 * @property adbPathPreferenceSource Where a user-chosen `adb` path is persisted. Written here,
 *   read by `ToolResolver` at probe time — the resolver validates it, this does not.
 * @property nowMs Wall-clock reader. A parameter so the poll metadata is assertable under
 *   virtual time instead of needing a real two-second wait.
 */
internal class DeviceRepositoryImpl(
    private val adbHostSource: AdbHostSource,
    private val idbHostSource: IdbHostSource,
    private val emulatorSource: EmulatorSource,
    private val deviceCommandExecutor: DeviceCommandExecutor,
    private val mirrorTransport: MirrorTransport,
    private val adbPathPreferenceSource: AdbPathPreferenceSource,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : DeviceRepository {

    /** Last version `adb` reported, for the status bar of an active session. */
    private val adbVersion = MutableStateFlow("")

    /**
     * Probes both platforms (AC-1).
     *
     * iOS is PROBED, not assumed: FR-2 defers iOS mirroring, but the toolbar dot reports a
     * real answer about the `idb` binary rather than a hardcoded "not installed".
     */
    override suspend fun detectTools(): List<ToolModel> {
        val adb = adbHostSource.probe()
        adbVersion.value = adb.version.orEmpty()
        return listOf(adb, idbHostSource.probe()).map(ToolProbeMapper.toModel::map)
    }

    override fun observeDiscovery(): Flow<DiscoveryModel> = flow {
        emit(DiscoveryModel.Detecting)
        // Collector-local, so two collectors cannot race on it. A serial's AVD name cannot
        // change while it stays attached, which is what makes caching it safe - and worth
        // it, since the alternative is an extra child process per emulator per poll.
        val avdNames = mutableMapOf<String, AvdName>()
        var emptyPolls = 0

        while (true) {
            // The guard belongs HERE and not in a `Flow.catch` further up: `catch` is
            // terminal, so a use-case or collector guard could render the failure exactly
            // once and discovery would still be over for good. Only the loop itself can
            // turn a failed poll into "try again on the next tick" - and `adb` fails
            // transiently all the time, most reliably on the cold start where the very
            // first `adb devices -l` has to boot the server inside HostCommandTimeoutMs.
            val emission = try {
                poll(avdNames, emptyPolls).also { emptyPolls = it.emptyPolls }
            } catch (failure: DeviceCommandException) {
                // emptyPolls is left exactly as it was: a poll that never answered neither
                // confirms an empty bench nor clears a streak of them.
                PollOutcome(
                    model = DiscoveryModel.PollFailed(
                        tool = ToolKind.Adb.binaryName,
                        reason = failure.stderrTail.ifBlank { failure.message.orEmpty() },
                    ),
                    emptyPolls = emptyPolls,
                )
            }
            // Emitted outside the try: catching around `emit` would also swallow a
            // downstream failure, which flow exception transparency forbids.
            emit(emission.model)
            delay(MirrorTuning.DiscoveryPollMs)
        }
    }

    /** One poll's emission plus the empty-poll streak it leaves behind. */
    private data class PollOutcome(val model: DiscoveryModel, val emptyPolls: Int)

    /**
     * One discovery round trip. Throws [DeviceCommandException] like any other data-layer
     * call - the caller decides what a failed round trip means for the stream.
     */
    private suspend fun poll(avdNames: MutableMap<String, AvdName>, emptyPolls: Int): PollOutcome {
        val probe = adbHostSource.probe()
        if (!probe.found) {
            // Not an error: a missing tool is a rendered state, so the counter does not
            // advance - we learned nothing about whether a device is attached.
            return PollOutcome(
                model = DiscoveryModel.ToolMissing(
                    tool = ToolKind.Adb.binaryName,
                    probedPaths = probe.probedPaths,
                    installCommand = probe.installCommand,
                    envHint = probe.envHint,
                ),
                emptyPolls = 0,
            )
        }
        adbVersion.value = probe.version.orEmpty()
        val devices = listDevicesEscalating()
        avdNames.keys.retainAll(devices.mapTo(mutableSetOf()) { it.id.value })
        val usable = devices.count { it.state == DeviceStateResource.Device }
        val streak = if (usable == 0) emptyPolls + 1 else 0

        val model = if (streak >= MirrorTuning.EmptyPollEscalation) {
            DiscoveryModel.NoDeviceFound(
                toolVersion = probe.version.orEmpty(),
                lastPollEpochMs = nowMs(),
                attempts = streak,
                attachedCount = devices.count { it.state != DeviceStateResource.Device },
            )
        } else {
            DiscoveryModel.Found(
                physical = devices.filterNot(DeviceResource::isEmulator)
                    .map(DeviceResourceMapper.toModel::map),
                emulators = emulators(devices, avdNames),
            )
        }
        return PollOutcome(model, streak)
    }

    /**
     * `adb devices -l`, with the FR-36 escalation ladder behind it.
     *
     * 1. Ask at the normal [MirrorTuning.HostCommandTimeoutMs] budget. Answered — done.
     * 2. A TIMEOUT-classified failure, and only that, means the daemon may be cold: heal it
     *    ([healDaemon]) and ask exactly once more. A parse error or a non-zero exit is a real
     *    answer about something else and is rethrown untouched, so it reaches the caller as
     *    `PollFailed` without a single extra process being spawned. Firing a 15 s watchdog at
     *    output that merely failed to parse would be a bug of its own.
     * 3. Anything still failing is rethrown, so the loop turns it into `PollFailed` with the
     *    real reason and tries again on the next tick. One escalation per poll — never a loop.
     *
     * There is deliberately NO pre-emptive `startServer()` ahead of the first poll. It was
     * proposed on the measurement that a warm `start-server` costs 0.00 s, then WITHDRAWN on
     * the later measurement that it HANGS against a wedged daemon: unconditional, it would
     * burn the full [MirrorTuning.DaemonStartTimeoutMs] on every poll — a second livelock,
     * five times longer than the 3 s one this fixes. Keying on a timeout also means there is
     * no "daemon is believed up" flag to keep, a flag whose belief outlived any daemon the
     * user killed from a shell.
     */
    private suspend fun listDevicesEscalating(): List<DeviceResource> = try {
        adbHostSource.listDevices()
    } catch (failure: DeviceCommandException) {
        // Step 2. Only a timeout is a candidate for healing; everything else is a real answer
        // about something other than the daemon and goes straight back out untouched.
        if (!failure.isTimeout()) throw failure
        healDaemon()
        // Step 3. Exactly one more attempt. If this throws, the caller turns it into
        // PollFailed with the real reason and the next tick starts over - one escalation per
        // poll, never a loop, which is what stops the cure from becoming a second livelock.
        adbHostSource.listDevices()
    }

    /**
     * Brings the adb daemon back, cold case first.
     *
     * A cold daemon is merely absent and `start-server` forks it in a measured, fixed 3.03 s.
     * A WEDGED daemon — alive but not serving — is a second, distinct failure mode that
     * `start-server` cannot repair: it hangs there, because one already exists. Only
     * `kill-server` clears it, so a timed-out [AdbHostSource.startServer] escalates into the
     * very same kill-then-start sequence AC-27's "Restart adb" button runs.
     *
     * A non-timeout `start-server` failure is not a wedge and is rethrown: nothing to repair.
     */
    private suspend fun healDaemon() {
        try {
            adbHostSource.startServer()
        } catch (failure: DeviceCommandException) {
            if (!failure.isTimeout()) throw failure
            restartAdbServer()
        }
    }

    /**
     * @see DeviceRepository.restartAdbServer
     *
     * Each half carries its own budget inside [AdbHostSource] — `kill-server` closes a local
     * socket at the ordinary host-command budget, `start-server` forks a daemon under the far
     * longer [MirrorTuning.DaemonStartTimeoutMs] watchdog. Either half failing throws, so the
     * use case reports a `Failure` rather than a button that silently did nothing.
     */
    override suspend fun restartAdbServer() {
        adbHostSource.killServer()
        adbHostSource.startServer()
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

    /**
     * @see DeviceRepository.setAdbPath
     *
     * No validation and no probe here on purpose. The path is checked against the filesystem
     * by `ToolResolver`'s existing `isExecutable` predicate, the same one every other candidate
     * passes through, so a bad path falls through to the ordered candidate list rather than
     * becoming an error the user has to clear.
     */
    override suspend fun setAdbPath(path: String) {
        adbPathPreferenceSource.setData(AdbPathPreference(path))
    }
}
