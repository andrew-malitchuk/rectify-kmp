package domain.usecase.impl.di

import domain.usecase.api.source.usecase.configuration.GetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.DumpHierarchyUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import domain.usecase.api.source.usecase.history.GetSentBatchUseCase
import domain.usecase.api.source.usecase.history.ObserveHistoryUseCase
import domain.usecase.api.source.usecase.history.RecordSentBatchUseCase
import domain.usecase.api.source.usecase.history.UpdateBatchStatusUseCase
import domain.usecase.impl.source.usecase.configuration.GetSidePanelWidthUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveApplicationLanguageUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.ObserveThemeUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetSidePanelWidthUseCaseImpl
import domain.usecase.impl.source.usecase.configuration.SetThemeUseCaseImpl
import domain.usecase.impl.source.usecase.device.BootEmulatorUseCaseImpl
import domain.usecase.impl.source.usecase.device.DetectToolsUseCaseImpl
import domain.usecase.impl.source.usecase.device.DumpHierarchyUseCaseImpl
import domain.usecase.impl.source.usecase.device.ObserveDeviceDiscoveryUseCaseImpl
import domain.usecase.impl.source.usecase.device.ObserveMirrorStatsUseCaseImpl
import domain.usecase.impl.source.usecase.device.RestartAdbServerUseCaseImpl
import domain.usecase.impl.source.usecase.device.RefreshDiscoveryUseCaseImpl
import domain.usecase.impl.source.usecase.device.SetAdbPathUseCaseImpl
import domain.usecase.impl.source.usecase.device.StartMirroringUseCaseImpl
import domain.usecase.impl.source.usecase.history.GetSentBatchUseCaseImpl
import domain.usecase.impl.source.usecase.history.ObserveHistoryUseCaseImpl
import domain.usecase.impl.source.usecase.history.RecordSentBatchUseCaseImpl
import domain.usecase.impl.source.usecase.history.UpdateBatchStatusUseCaseImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public val domainUseCaseImplModule: Module = module {
    // Configuration
    singleOf(::SetThemeUseCaseImpl) bind SetThemeUseCase::class
    singleOf(::ObserveThemeUseCaseImpl) bind ObserveThemeUseCase::class
    singleOf(::ObserveApplicationLanguageUseCaseImpl) bind ObserveApplicationLanguageUseCase::class
    singleOf(::GetSidePanelWidthUseCaseImpl) bind GetSidePanelWidthUseCase::class
    singleOf(::SetSidePanelWidthUseCaseImpl) bind SetSidePanelWidthUseCase::class

    // Device
    singleOf(::DetectToolsUseCaseImpl) bind DetectToolsUseCase::class
    singleOf(::ObserveDeviceDiscoveryUseCaseImpl) bind ObserveDeviceDiscoveryUseCase::class
    singleOf(::BootEmulatorUseCaseImpl) bind BootEmulatorUseCase::class
    singleOf(::StartMirroringUseCaseImpl) bind StartMirroringUseCase::class
    singleOf(::ObserveMirrorStatsUseCaseImpl) bind ObserveMirrorStatsUseCase::class
    singleOf(::RefreshDiscoveryUseCaseImpl) bind RefreshDiscoveryUseCase::class
    singleOf(::SetAdbPathUseCaseImpl) bind SetAdbPathUseCase::class
    singleOf(::RestartAdbServerUseCaseImpl) bind RestartAdbServerUseCase::class
    singleOf(::DumpHierarchyUseCaseImpl) bind DumpHierarchyUseCase::class

    // History
    singleOf(::RecordSentBatchUseCaseImpl) bind RecordSentBatchUseCase::class
    singleOf(::ObserveHistoryUseCaseImpl) bind ObserveHistoryUseCase::class
    singleOf(::GetSentBatchUseCaseImpl) bind GetSentBatchUseCase::class
    singleOf(::UpdateBatchStatusUseCaseImpl) bind UpdateBatchStatusUseCase::class
}
