package data.device.impl.source.datasource

import data.device.api.source.datasource.IdbHostSource
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.device.impl.core.tool.ToolResolver

/**
 * Locates `idb` through the same resolver `adb` and `emulator` use.
 *
 * No command path: nothing in this release runs `idb`, so there is deliberately no executor
 * here to run one with.
 *
 * @property tools Locates the `idb` binary.
 */
internal class IdbHostSourceImpl(
    private val tools: ToolResolver,
) : IdbHostSource {

    override suspend fun probe(): ToolProbe = tools.resolve(ToolKind.Idb)
}
