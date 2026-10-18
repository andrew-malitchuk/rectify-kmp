package presentation.feature.mirror.source.mirror

import data.handoff.api.source.handoff.AnnotationHandoff
import org.koin.core.module.Module
import presentation.feature.mirror.core.unsupported.UnsupportedAnnotationHandoff

/**
 * Android implementation of [provideHandoffSources].
 *
 * There is no desktop file slot on a mobile target, so the graph resolves with
 * [UnsupportedAnnotationHandoff] and a [CropEncoder] that always answers `null` (AS-29).
 */
internal actual fun Module.provideHandoffSources() {
    single<AnnotationHandoff> { UnsupportedAnnotationHandoff }
    single<CropEncoder> { CropEncoder { _, _ -> null } }
}
