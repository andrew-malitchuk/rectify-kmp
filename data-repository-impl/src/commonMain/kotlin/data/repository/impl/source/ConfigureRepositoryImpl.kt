package data.repository.impl.source

import data.preference.api.source.datasource.LanguagePreferenceSource
import data.preference.api.source.datasource.OnboardingPreferenceSource
import data.preference.api.source.datasource.SidePanelWidthPreferenceSource
import data.preference.api.source.datasource.ThemePreferenceSource
import data.preference.api.source.datasource.UnitSystemPreferenceSource
import data.preference.api.source.model.OnboardingPreference
import data.preference.api.source.model.ThemePreference
import data.preference.api.source.model.UnitSystemPreference
import data.repository.impl.core.mapper.LanguagePreferenceMapper
import data.repository.impl.core.mapper.OnboardingPreferenceMapper
import data.repository.impl.core.mapper.SidePanelWidthPreferenceMapper
import data.repository.impl.core.mapper.ThemePreferenceMapper
import data.repository.impl.core.mapper.UnitSystemPreferenceMapper
import domain.core.source.model.OnboardingModel
import domain.core.source.model.ThemeModel
import domain.core.source.model.UnitSystemModel
import domain.repository.api.source.repository.ConfigureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default implementation of [ConfigureRepository].
 *
 * Delegates theme, onboarding, and language persistence to the corresponding
 * [PreferenceSource] instances.
 *
 * @property themePreferenceSource Data source for theme preferences.
 * @property onboardingPreferenceSource Data source for onboarding preferences.
 * @property languagePreferenceSource Data source for language preferences.
 * @property unitSystemPreferenceSource Data source for the measurement unit system.
 * @property sidePanelWidthPreferenceSource Data source for the shell side panel width.
 */
internal class ConfigureRepositoryImpl(
    private val themePreferenceSource: ThemePreferenceSource,
    private val onboardingPreferenceSource: OnboardingPreferenceSource,
    private val languagePreferenceSource: LanguagePreferenceSource,
    private val unitSystemPreferenceSource: UnitSystemPreferenceSource,
    private val sidePanelWidthPreferenceSource: SidePanelWidthPreferenceSource,
) : ConfigureRepository {

    /** @see ConfigureRepository.getTheme */
    override suspend fun getTheme(): ThemeModel? =
        ThemePreferenceMapper.toModel.map(themePreferenceSource.getData())

    /** @see ConfigureRepository.setTheme */
    override suspend fun setTheme(theme: ThemeModel?) {
        val preference = theme?.let { ThemePreferenceMapper.toResource.map(it) } ?: ThemePreference()
        themePreferenceSource.setData(preference)
    }

    /** @see ConfigureRepository.observeTheme */
    override fun observeTheme(): Flow<ThemeModel?> =
        themePreferenceSource.observeData().map { ThemePreferenceMapper.toModel.map(it) }

    /** @see ConfigureRepository.getOnboarding */
    override suspend fun getOnboarding(): OnboardingModel? =
        OnboardingPreferenceMapper.toModel.map(onboardingPreferenceSource.getData())

    /** @see ConfigureRepository.setOnboarding */
    override suspend fun setOnboarding(status: OnboardingModel?) {
        val preference = status?.let { OnboardingPreferenceMapper.toResource.map(it) } ?: OnboardingPreference()
        onboardingPreferenceSource.setData(preference)
    }

    /** @see ConfigureRepository.observeOnboarding */
    override fun observeOnboarding(): Flow<OnboardingModel?> =
        onboardingPreferenceSource.observeData().map { OnboardingPreferenceMapper.toModel.map(it) }

    /** @see ConfigureRepository.setApplicationLanguage */
    override suspend fun setApplicationLanguage(localeCode: String) {
        languagePreferenceSource.setData(LanguagePreferenceMapper.toResource.map(localeCode))
    }

    /** @see ConfigureRepository.getApplicationLanguage */
    override suspend fun getApplicationLanguage(): String? =
        LanguagePreferenceMapper.toModel.map(languagePreferenceSource.getData())

    /** @see ConfigureRepository.observeApplicationLanguage */
    override fun observeApplicationLanguage(): Flow<String?> =
        languagePreferenceSource.observeData().map { LanguagePreferenceMapper.toModel.map(it) }

    /** @see ConfigureRepository.getUnitSystem */
    override suspend fun getUnitSystem(): UnitSystemModel? =
        UnitSystemPreferenceMapper.toModel.map(unitSystemPreferenceSource.getData())

    /** @see ConfigureRepository.setUnitSystem */
    override suspend fun setUnitSystem(unitSystem: UnitSystemModel?) {
        val preference = unitSystem?.let { UnitSystemPreferenceMapper.toResource.map(it) } ?: UnitSystemPreference()
        unitSystemPreferenceSource.setData(preference)
    }

    /** @see ConfigureRepository.observeUnitSystem */
    override fun observeUnitSystem(): Flow<UnitSystemModel?> =
        unitSystemPreferenceSource.observeData().map { UnitSystemPreferenceMapper.toModel.map(it) }

    /** @see ConfigureRepository.getSidePanelWidth */
    override suspend fun getSidePanelWidth(): Int =
        SidePanelWidthPreferenceMapper.toModel.map(sidePanelWidthPreferenceSource.getData())

    /** @see ConfigureRepository.setSidePanelWidth */
    override suspend fun setSidePanelWidth(widthDp: Int) {
        sidePanelWidthPreferenceSource.setData(SidePanelWidthPreferenceMapper.toResource.map(widthDp))
    }
}
