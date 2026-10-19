package data.device.impl.source.datasource

import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.ToolKind
import data.device.api.source.parse.buildDeviceArgv
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.process.ProcessRunner
import data.device.impl.core.tool.ToolResolver

/**
 * The sole device-scoped execution path on the desktop host.
 *
 * Every argv it runs comes out of [buildDeviceArgv], which always emits
 * `[adb, -s, <device>, ...]` - there is no overload taking a raw string, so no caller can
 * address a device other than the one it was handed (NFR-5).
 *
 * @property tools Locates the `adb` binary.
 */
internal class DeviceCommandExecutorImpl(
    private val tools: ToolResolver,
) : DeviceCommandExecutor {

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        val path = tools.requirePath(ToolKind.Adb)
        return ProcessRunner.run(
            argv = buildDeviceArgv(ToolKind.Adb, path, device, command),
            timeoutMs = command.timeoutMs(),
        )
    }

    /** A screen capture moves megabytes and gets the long budget; a shell read does not. */
    private fun DeviceCommand.timeoutMs(): Long = when (this) {
        DeviceCommand.Screencap -> MirrorTuning.CaptureTimeoutMs
        DeviceCommand.UiAutomatorDump -> MirrorTuning.DumpTimeoutMs
        else -> MirrorTuning.HostCommandTimeoutMs
    }
}
