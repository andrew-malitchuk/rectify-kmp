package domain.usecase.impl.di

import domain.usecase.api.source.usecase.configuration.GetApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.GetOnboardingStatusUseCase
import domain.usecase.api.source.usecase.configuration.GetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.GetThemeUseCase
import domain.usecase.api.source.usecase.configuration.GetUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveThemeUseCase
import domain.usecase.api.source.usecase.configuration.ObserveUnitSystemUseCase
import domain.usecase.api.source.usecase.configuration.SetApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.SetOnboardingStatusUseCase
import domain.usecase.api.source.usecase.configuration.SetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetUnitSystemUseCase
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import domain.usecase.impl.source.usecase.configuration.GetApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetOnboardingStatusUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetSidePanelWidthUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.GetUnitSystemUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveUnitSystemUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetOnboardingStatusUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetSidePanelWidthUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetUnitSystemUseCaseImpl
import domain.usecase.impl.source.usecase.device.BootEmulatorUseCaseImpl
import domain.usecase.impl.source.usecase.device.DetectToolsUseCaseImpl
import domain.usecase.impl.source.usecase.device.ObserveDeviceDiscoveryUseCaseImpl
import domain.usecase.impl.source.usecase.device.ObserveMirrorStatsUseCaseImpl
import domain.usecase.impl.source.usecase.device.StartMirroringUseCaseImpl
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
    singleOf(::GetSidePanelWidthUseCaseImpl) bind GetSidePanelWidthUseCase::class
    singleOf(::SetSidePanelWidthUseCaseImpl) bind SetSidePanelWidthUseCase::class

    // Device
    singleOf(::DetectToolsUseCaseImpl) bind DetectToolsUseCase::class
    singleOf(::ObserveDeviceDiscoveryUseCaseImpl) bind ObserveDeviceDiscoveryUseCase::class
    singleOf(::BootEmulatorUseCaseImpl) bind BootEmulatorUseCase::class
    singleOf(::StartMirroringUseCaseImpl) bind StartMirroringUseCase::class
    singleOf(::ObserveMirrorStatsUseCaseImpl) bind ObserveMirrorStatsUseCase::class
}
