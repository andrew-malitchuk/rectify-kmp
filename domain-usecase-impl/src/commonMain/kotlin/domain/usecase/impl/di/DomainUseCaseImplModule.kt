package domain.usecase.impl.di

import domain.usecase.api.source.usecase.configuration.GetApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.GetOnboardingStatusUseCase
import domain.usecase.api.source.usecase.configuration.GetThemeUseCase
import domain.usecase.api.source.usecase.configuration.GetUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveThemeUseCase
import domain.usecase.api.source.usecase.configuration.ObserveUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.SetApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.SetOnboardingStatusUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetUnitSystemUseCase
import domain.usecase.impl.source.usecase.configuration.GetApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetOnboardingStatusUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetUnitSystemUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveUnitSystemUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetOnboardingStatusUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetUnitSystemUseCaseImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public val domainUseCaseImplModule: Module = module {
    // Configuration
    singleOf(::GetThemeUseCaseImpl) bind GetThemeUseCase::class
    singleOf(::SetThemeUseCaseImpl) bind SetThemeUseCase::class
    singleOf(::ObserveThemeUseCaseImpl) bind ObserveThemeUseCase::class
    singleOf(::GetOnboardingStatusUseCaseImpl) bind GetOnboardingStatusUseCase::class
    singleOf(::SetOnboardingStatusUseCaseImpl) bind SetOnboardingStatusUseCase::class
    singleOf(::SetApplicationLanguageUseCaseImpl) bind SetApplicationLanguageUseCase::class
    singleOf(::GetApplicationLanguageUseCaseImpl) bind GetApplicationLanguageUseCase::class
    singleOf(::ObserveApplicationLanguageUseCaseImpl) bind ObserveApplicationLanguageUseCase::class
    singleOf(::GetUnitSystemUseCaseImpl) bind GetUnitSystemUseCase::class
    singleOf(::SetUnitSystemUseCaseImpl) bind SetUnitSystemUseCase::class
    singleOf(::ObserveUnitSystemUseCaseImpl) bind ObserveUnitSystemUseCase::class
}
