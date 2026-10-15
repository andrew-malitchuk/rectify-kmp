package data.device.impl.source.datasource

import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.api.source.parse.HostCommand
import data.device.api.source.parse.buildHostArgv
import data.device.api.source.parse.parseAvdList
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.core.process.ProcessRunner
import data.device.impl.core.tool.ToolResolver

/**
 * Deviceless `emulator` calls, run as child processes.
 *
 * @property tools Locates the `emulator` binary (in `<sdk>/emulator`, not platform-tools).
 */
internal class EmulatorSourceImpl(
    private val tools: ToolResolver,
) : EmulatorSource {

    override suspend fun probe(): ToolProbe = tools.resolve(ToolKind.Emulator)

    /** A missing `emulator` lists nothing; it is not an error here, it is a [probe] result. */
    override suspend fun listAvds(): List<AvdResource> {
        val path = tools.resolve(ToolKind.Emulator).path ?: return emptyList()
        val output = ProcessRunner.run(
            argv = buildHostArgv(path, HostCommand.ListAvds),
            timeoutMs = MirrorTuning.HostCommandTimeoutMs,
        )
        // runningAs is filled in by the repository, which is the only layer that sees both
        // the AVD list and the attached-device list.
        return parseAvdList(output.decodeToString()).map(::AvdResource)
    }

    /**
     * Boots [avd] detached: an emulator takes tens of seconds to come up and must outlive
     * this call, so there is no timeout and no exit code to inspect. That also makes the
     * lock-file non-zero exit of an already-running AVD a no-op success, as specified.
     */
    override suspend fun boot(avd: AvdName) {
        val path = tools.requirePath(ToolKind.Emulator)
        ProcessRunner.spawnDetached(buildHostArgv(path, HostCommand.BootAvd(avd)))
    }
}
