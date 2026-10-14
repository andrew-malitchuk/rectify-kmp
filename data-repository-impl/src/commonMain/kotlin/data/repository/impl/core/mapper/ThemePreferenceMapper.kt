package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.preference.api.source.model.ThemePreference
import data.repository.impl.core.mapper.base.ModelResourceMapper
import domain.core.source.model.ThemeModel

/**
 * Bidirectional mapper between [ThemeModel] and [ThemePreference].
 *
 * Converts the data-layer [ThemePreference] (string-based) to the domain-layer
 * [ThemeModel] enum and vice-versa. Falls back to [ThemeModel.System] when the
 * stored string does not match any known [ThemeModel] entry — this also covers
 * [data.preference.api.source.model.ThemePreference.DEFAULT_THEME] (`"system"`),
 * so an unset preference resolves to the system appearance.
 */
internal object ThemePreferenceMapper : ModelResourceMapper<ThemeModel, ThemePreference> {

    /** @see ModelResourceMapper.toModel */
    override val toModel: Mapper<ThemePreference, ThemeModel> =
        Mapper { preference ->
            ThemeModel.entries.firstOrNull { it.mode == preference.theme }
                ?: ThemeModel.System
        }

    /** @see ModelResourceMapper.toResource */
    override val toResource: Mapper<ThemeModel, ThemePreference> =
        Mapper { model ->
            ThemePreference(theme = model.mode)
        }
}
