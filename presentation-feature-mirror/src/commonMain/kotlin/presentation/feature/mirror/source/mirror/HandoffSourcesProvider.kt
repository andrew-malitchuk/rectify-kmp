package presentation.feature.mirror.source.mirror

import org.koin.core.module.Module

/**
 * Registers [data.handoff.api.source.handoff.AnnotationHandoff] and [CropEncoder] in the Koin
 * module.
 *
 * - **Desktop:** the real [data.handoff.api.source.handoff.fileSlotAnnotationHandoff] and
 *   `AwtCropEncoder`.
 * - **Android / iOS:**
 *   [presentation.feature.mirror.core.unsupported.UnsupportedAnnotationHandoff] and a
 *   [CropEncoder] that always returns `null`, so the graph resolves on every target even though
 *   neither feature works there (AS-29).
 *
 * Verbatim copy of `data-device-impl`'s `Module.provideDeviceSources()` shape.
 */
internal expect fun Module.provideHandoffSources()
