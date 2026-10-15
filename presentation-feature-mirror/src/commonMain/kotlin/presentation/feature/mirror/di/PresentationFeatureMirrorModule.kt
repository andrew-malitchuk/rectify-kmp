package presentation.feature.mirror.di

import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import presentation.feature.mirror.source.mirror.MirrorViewModel

/**
 * DI for the mirror surface.
 *
 * Explicit `get<T>()` rather than `viewModelOf(::MirrorViewModel)` because the constructor's
 * last parameter is the PNG decode seam, which has a default and no Koin definition.
 */
public val presentationFeatureMirrorModule: Module = module {
    viewModel {
        MirrorViewModel(
            detectTools = get<DetectToolsUseCase>(),
            observeDiscovery = get<ObserveDeviceDiscoveryUseCase>(),
            bootEmulator = get<BootEmulatorUseCase>(),
            startMirroring = get<StartMirroringUseCase>(),
            observeMirrorStats = get<ObserveMirrorStatsUseCase>(),
        )
    }
}
