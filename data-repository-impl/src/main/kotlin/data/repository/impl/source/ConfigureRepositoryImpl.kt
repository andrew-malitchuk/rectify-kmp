package data.repository.impl.source

import data.preference.api.source.datasource.LanguagePreferenceSource
import data.preference.api.source.datasource.SidePanelWidthPreferenceSource
import data.preference.api.source.datasource.ThemePreferenceSource
import data.preference.api.source.model.ThemePreference
import data.repository.impl.core.mapper.LanguagePreferenceMapper
import data.repository.impl.core.mapper.SidePanelWidthPreferenceMapper
import data.repository.impl.core.mapper.ThemePreferenceMapper
import domain.core.source.model.ThemeModel
import domain.repository.api.source.repository.ConfigureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default implementation of [ConfigureRepository].
 *
 * Delegates theme, language, and side-panel-width persistence to the corresponding
 * [data.preference.api.source.datasource.base.PreferenceSource] instances.
 *
 * @property themePreferenceSource Data source for theme preferences.
 * @property languagePreferenceSource Data source for language preferences.
 * @property sidePanelWidthPreferenceSource Data source for the shell side panel width.
 */
internal class ConfigureRepositoryImpl(
    private val themePreferenceSource: ThemePreferenceSource,
    private val languagePreferenceSource: LanguagePreferenceSource,
    private val sidePanelWidthPreferenceSource: SidePanelWidthPreferenceSource,
) : ConfigureRepository {

    /** @see ConfigureRepository.setTheme */
    override suspend fun setTheme(theme: ThemeModel?) {
        val preference = theme?.let { ThemePreferenceMapper.toResource.map(it) } ?: ThemePreference()
        themePreferenceSource.setData(preference)
    }

    /** @see ConfigureRepository.observeTheme */
    override fun observeTheme(): Flow<ThemeModel?> =
        themePreferenceSource.observeData().map { ThemePreferenceMapper.toModel.map(it) }

    /** @see ConfigureRepository.observeApplicationLanguage */
    override fun observeApplicationLanguage(): Flow<String?> =
        languagePreferenceSource.observeData().map { LanguagePreferenceMapper.toModel.map(it) }

    /** @see ConfigureRepository.getSidePanelWidth */
    override suspend fun getSidePanelWidth(): Int =
        SidePanelWidthPreferenceMapper.toModel.map(sidePanelWidthPreferenceSource.getData())

    /** @see ConfigureRepository.setSidePanelWidth */
    override suspend fun setSidePanelWidth(widthDp: Int) {
        sidePanelWidthPreferenceSource.setData(SidePanelWidthPreferenceMapper.toResource.map(widthDp))
    }
}
