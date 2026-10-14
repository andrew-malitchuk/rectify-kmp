package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.preference.api.source.model.UnitSystemPreference
import data.repository.impl.core.mapper.base.ModelResourceMapper
import domain.core.source.model.UnitSystemModel

/**
 * Bidirectional mapper between [UnitSystemModel] and [UnitSystemPreference].
 *
 * Falls back to [UnitSystemModel.Metric] when the stored string does not match any known entry.
 */
internal object UnitSystemPreferenceMapper : ModelResourceMapper<UnitSystemModel, UnitSystemPreference> {

    override val toModel: Mapper<UnitSystemPreference, UnitSystemModel> =
        Mapper { preference ->
            UnitSystemModel.entries.firstOrNull { it.id == preference.unitSystem }
                ?: UnitSystemModel.Metric
        }

    override val toResource: Mapper<UnitSystemModel, UnitSystemPreference> =
        Mapper { model ->
            UnitSystemPreference(unitSystem = model.id)
        }
}
