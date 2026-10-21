package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.ToolKind
import data.device.api.source.model.ToolProbe
import data.repository.impl.core.mapper.base.ModelResourceMapper
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.device.ToolModel

/**
 * Bidirectional mapper between [ToolModel] and [ToolProbe].
 *
 * Never throws. The mapping is lossy in one direction by design: the domain model groups
 * `adb` and `emulator` under [PlatformModel.Android], so [toResource] picks `adb` as the
 * representative Android tool.
 */
internal object ToolProbeMapper : ModelResourceMapper<ToolModel, ToolProbe> {

    /** @see ModelResourceMapper.toModel */
    override val toModel: Mapper<ToolProbe, ToolModel> =
        Mapper { probe ->
            ToolModel(
                platform = when (probe.kind) {
                    ToolKind.Adb, ToolKind.Emulator -> PlatformModel.Android
                    ToolKind.Idb, ToolKind.Simctl -> PlatformModel.Ios
                },
                isInstalled = probe.found,
                version = probe.version,
                path = probe.path,
                probedPaths = probe.probedPaths,
                installCommand = probe.installCommand,
            )
        }

    /** @see ModelResourceMapper.toResource */
    override val toResource: Mapper<ToolModel, ToolProbe> =
        Mapper { model ->
            ToolProbe(
                kind = when (model.platform) {
                    PlatformModel.Android -> ToolKind.Adb
                    PlatformModel.Ios -> ToolKind.Idb
                },
                found = model.isInstalled,
                path = model.path,
                version = model.version,
                probedPaths = model.probedPaths,
                installCommand = model.installCommand,
            )
        }
}
