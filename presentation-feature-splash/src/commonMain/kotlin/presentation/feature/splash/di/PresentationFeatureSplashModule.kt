package presentation.feature.splash.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import presentation.feature.splash.source.splash.SplashViewModel

/**
 * Koin module for the Splash feature.
 *
 * Registers [SplashViewModel] so it can be injected via `koinViewModel()`.
 * The ViewModel is constructed with `GetOnboardingStatusUseCase` provided by DI.
 *
 * @see SplashViewModel
 */
public val presentationFeatureSplashModule: Module = module {
    viewModelOf(::SplashViewModel)
}
