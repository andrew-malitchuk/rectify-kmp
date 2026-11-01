package presentation.feature.mirror.source.mirror

import data.handoff.api.source.handoff.AnnotationHandoff
import data.handoff.api.source.handoff.fileSlotAnnotationHandoff
import org.koin.core.module.Module
import presentation.feature.mirror.core.crop.AwtCropEncoder

/**
 * Desktop implementation of [provideHandoffSources].
 *
 * Registers the real `AnnotationHandoff`/`AwtCropEncoder` pair. Not `singleOf` (TAC-2): neither
 * constructor parameter is a Koin dependency — `fileSlotAnnotationHandoff()`'s `root` defaults to
 * `~/.rectify/handoff`, and `AwtCropEncoder` takes none at all.
 */
internal fun Module.provideHandoffSources() {
    single<AnnotationHandoff> { fileSlotAnnotationHandoff() }
    single<CropEncoder> { AwtCropEncoder() }
}
