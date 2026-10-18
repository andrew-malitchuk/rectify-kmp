package data.preference.impl.di

import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.datasource.LanguagePreferenceSource
import data.preference.api.source.datasource.OnboardingPreferenceSource
import data.preference.api.source.datasource.SidePanelWidthPreferenceSource
import data.preference.api.source.datasource.ThemePreferenceSource
import data.preference.api.source.datasource.UnitSystemPreferenceSource
import data.preference.impl.source.provideSettings
import data.preference.impl.source.datasource.AdbPathPreferenceSourceImpl
import data.preference.impl.source.datasource.LanguagePreferenceSourceImpl
import data.preference.impl.source.datasource.OnboardingPreferenceSourceImpl
import data.preference.impl.source.datasource.SidePanelWidthPreferenceSourceImpl
import data.preference.impl.source.datasource.ThemePreferenceSourceImpl
import data.preference.impl.source.datasource.UnitSystemPreferenceSourceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin dependency injection module for the preference data layer.
 *
 * Registers the following bindings:
 * - [com.russhwolf.settings.Settings] — **singleton** platform-specific settings store.
 * - [ThemePreferenceSource] — **singleton** for theme preferences.
 * - [OnboardingPreferenceSource] — **singleton** for onboarding state.
 * - [LanguagePreferenceSource] — **singleton** for language preferences.
 * - [UnitSystemPreferenceSource] — **singleton** for measurement unit system preference.
 * - [SidePanelWidthPreferenceSource] — **singleton** for the shell side panel width.
 * - [AdbPathPreferenceSource] — **singleton** for the user-chosen `adb` binary path.
 */
public val dataPreferenceImplModule: Module = module {
    provideSettings()
    singleOf(::ThemePreferenceSourceImpl) bind ThemePreferenceSource::class
    singleOf(::OnboardingPreferenceSourceImpl) bind OnboardingPreferenceSource::class
    singleOf(::LanguagePreferenceSourceImpl) bind LanguagePreferenceSource::class
    singleOf(::UnitSystemPreferenceSourceImpl) bind UnitSystemPreferenceSource::class
    singleOf(::SidePanelWidthPreferenceSourceImpl) bind SidePanelWidthPreferenceSource::class
    singleOf(::AdbPathPreferenceSourceImpl) bind AdbPathPreferenceSource::class
}
