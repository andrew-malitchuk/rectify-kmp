package presentation.feature.settings.di

import domain.usecase.api.source.usecase.configuration.GetThemeUseCase
import domain.usecase.api.source.usecase.configuration.GetUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.SetOnboardingStatusUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetUnitSystemUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import presentation.feature.settings.source.language.LanguagePickerViewModel
import presentation.feature.settings.source.settings.SettingsViewModel

public val presentationFeatureSettingsModule: Module = module {
    viewModel {
        SettingsViewModel(
            observeApplicationLanguage = get<ObserveApplicationLanguageUseCase>(),
            getThemeUseCase = get<GetThemeUseCase>(),
            setThemeUseCase = get<SetThemeUseCase>(),
            getUnitSystemUseCase = get<GetUnitSystemUseCase>(),
            setUnitSystemUseCase = get<SetUnitSystemUseCase>(),
            observeUnitSystemUseCase = get<ObserveUnitSystemUseCase>(),
            setOnboardingStatus = get<SetOnboardingStatusUseCase>(),
        )
    }
    viewModelOf(::LanguagePickerViewModel)
}
