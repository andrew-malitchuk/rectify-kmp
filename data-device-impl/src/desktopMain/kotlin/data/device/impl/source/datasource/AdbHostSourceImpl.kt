package data.device.impl.source.datasource

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildHostArgv
import data.device.api.source.parse.parseAdbDevices
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.process.ProcessRunner
import data.device.impl.core.tool.ToolResolver

/**
 * Deviceless `adb` calls, run as child processes.
 *
 * @property tools Locates the `adb` binary.
 */
internal class AdbHostSourceImpl(
    private val tools: ToolResolver,
) : AdbHostSource {

    override suspend fun probe(): ToolProbe = tools.resolve(ToolKind.Adb)

    /** A missing `adb` lists nothing; it is not an error here, it is a [probe] result. */
    override suspend fun listDevices(): List<DeviceResource> {
        val path = tools.resolve(ToolKind.Adb).path ?: return emptyList()
        val output = ProcessRunner.run(
            argv = buildHostArgv(path, HostCommand.ListDevices),
            timeoutMs = MirrorTuning.HostCommandTimeoutMs,
        )
        return parseAdbDevices(output.decodeToString())
    }
}
