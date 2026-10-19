package data.device.impl.core.unsupported

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.IosTargetResource
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.SimulatorResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.model.TransportStats
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildHostArgv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

/** Reason carried by every failure these sources raise. */
private const val UNSUPPORTED = "Device tooling runs on the desktop host only"

/** A probe that reports the tool as absent — which, on a phone, it is. */
private fun absent(kind: ToolKind): ToolProbe = ToolProbe(kind = kind, found = false)

/**
 * The failure a deviceless `adb` command raises here, with the argv it would have run.
 *
 * Built through [buildHostArgv] rather than a hand-listed argv so this stays bound to the
 * closed command set like every other call site.
 */
private fun unsupported(command: HostCommand): DeviceCommandException =
    DeviceCommandException(buildHostArgv(ToolKind.Adb.binaryName, command), null, UNSUPPORTED)

/** No-op [AdbHostSource] for targets that cannot spawn a process. */
internal object UnsupportedAdbHostSource : AdbHostSource {

    override suspend fun probe(): ToolProbe = absent(ToolKind.Adb)

    override suspend fun listDevices(): List<DeviceResource> = emptyList()

    override suspend fun startServer(): Unit = throw unsupported(HostCommand.StartServer)

    override suspend fun killServer(): Unit = throw unsupported(HostCommand.KillServer)
}

/**
 * No-op [IosHostSource] for targets that cannot spawn a process.
 *
 * Bound by the Android and iOS `provideDeviceSources()` actuals in place of the deleted
 * `UnsupportedIdbHostSource`, whose probe-only surface `probeIdb()` subsumes.
 */
internal object UnsupportedIosHostSource : IosHostSource {

    override suspend fun probeSimctl(): ToolProbe = absent(ToolKind.Simctl)

    override suspend fun probeIdb(): ToolProbe = absent(ToolKind.Idb)

    override suspend fun listSimulators(): List<SimulatorResource> = emptyList()

    override suspend fun listDevices(): List<IosTargetResource> = emptyList()
}

/** No-op [EmulatorSource] for targets that cannot spawn a process. */
internal object UnsupportedEmulatorSource : EmulatorSource {

    override suspend fun probe(): ToolProbe = absent(ToolKind.Emulator)

    override suspend fun listAvds(): List<AvdResource> = emptyList()

    override suspend fun boot(avd: AvdName): Unit =
        throw DeviceCommandException(listOf(ToolKind.Emulator.binaryName), null, UNSUPPORTED)
}

/** No-op [DeviceCommandExecutor] for targets that cannot spawn a process. */
internal object UnsupportedDeviceCommandExecutor : DeviceCommandExecutor {

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray =
        throw DeviceCommandException(listOf(ToolKind.Adb.binaryName, "-s", device.value), null, UNSUPPORTED)
}

/** No-op [MirrorTransport] for targets that cannot spawn a process. */
internal object UnsupportedMirrorTransport : MirrorTransport {

    private val transportStats = MutableStateFlow(TransportStats())

    override fun frames(device: DeviceId): Flow<MirrorFrame> = emptyFlow()

    override val stats: StateFlow<TransportStats> = transportStats.asStateFlow()
}
