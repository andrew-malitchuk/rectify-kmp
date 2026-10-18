package presentation.feature.mirror.di

import data.handoff.api.source.handoff.AnnotationHandoff
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.DumpHierarchyUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import presentation.feature.mirror.source.mirror.CropEncoder
import presentation.feature.mirror.source.mirror.MirrorViewModel
import presentation.feature.mirror.source.mirror.provideHandoffSources

/**
 * DI for the mirror surface.
 *
 * Explicit `get<T>()` rather than `viewModelOf(::MirrorViewModel)` because three constructor
 * parameters — the PNG decode seam, the wall clock and the monotonic clock — have defaults and
 * no Koin definition, so the reflective constructor binding could not satisfy them.
 *
 * [provideHandoffSources] registers the two that ARE injected, per platform: the real file-slot
 * handoff and `AwtCropEncoder` on desktop, the AS-29 stand-ins on Android/iOS.
 */
public val presentationFeatureMirrorModule: Module = module {
    provideHandoffSources()
    viewModel {
        MirrorViewModel(
            detectTools = get<DetectToolsUseCase>(),
            observeDiscovery = get<ObserveDeviceDiscoveryUseCase>(),
            bootEmulator = get<BootEmulatorUseCase>(),
            startMirroring = get<StartMirroringUseCase>(),
            observeMirrorStats = get<ObserveMirrorStatsUseCase>(),
            refreshDiscovery = get<RefreshDiscoveryUseCase>(),
            restartAdbServer = get<RestartAdbServerUseCase>(),
            setAdbPath = get<SetAdbPathUseCase>(),
            dumpHierarchy = get<DumpHierarchyUseCase>(),
            handoff = get<AnnotationHandoff>(),
            encodeCrop = get<CropEncoder>(),
        )
    }
}
