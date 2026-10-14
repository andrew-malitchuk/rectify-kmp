package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.preference.api.source.model.SidePanelWidthPreference
import data.repository.impl.core.mapper.base.ModelResourceMapper

/**
 * Bidirectional mapper between a plain side panel width in dp and [SidePanelWidthPreference].
 *
 * The domain representation is a plain [Int] rather than a dedicated model — there is
 * nothing to name beyond the number. Range clamping belongs to the presentation layer's
 * shell defaults, so this mapper is a pure unwrap/wrap.
 */
internal object SidePanelWidthPreferenceMapper : ModelResourceMapper<Int, SidePanelWidthPreference> {

    /** @see ModelResourceMapper.toModel */
    override val toModel: Mapper<SidePanelWidthPreference, Int> =
        Mapper { preference -> preference.widthDp }

    /** @see ModelResourceMapper.toResource */
    override val toResource: Mapper<Int, SidePanelWidthPreference> =
        Mapper { widthDp -> SidePanelWidthPreference(widthDp = widthDp) }
}
