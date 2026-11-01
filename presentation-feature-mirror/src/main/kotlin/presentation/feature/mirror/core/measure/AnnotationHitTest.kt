package presentation.feature.mirror.core.measure

import androidx.compose.ui.unit.IntOffset
import presentation.feature.mirror.source.mirror.StagedAnnotation

/**
 * The annotation under a point, topmost first, in **device pixels**.
 *
 * Topmost means the highest [StagedAnnotation.index]: badges are drawn in index order, so the
 * one the user sees on top of an overlap is the one with the largest number. Right and bottom
 * edges are exclusive, matching [presentation.feature.mirror.source.mirror.DeviceRect]'s own
 * contract.
 *
 * Pure and pointer-free: the caller converts its pointer offset with `FrameGeometry` first, so
 * this stays testable with no display attached.
 */
internal fun annotationAt(point: IntOffset, annotations: List<StagedAnnotation>): StagedAnnotation? =
    annotations
        .filter {
            point.x >= it.rect.left && point.x < it.rect.right &&
                point.y >= it.rect.top && point.y < it.rect.bottom
        }
        .maxByOrNull { it.index }
