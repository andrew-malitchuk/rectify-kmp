package presentation.feature.mirror.core.measure

import androidx.compose.ui.unit.IntSize

/**
 * The four gaps around [target], in device pixels (FR-38 / AC-59).
 *
 * A side with a neighbour measures to that neighbour's facing edge; a side without one measures
 * to the frame edge (`0` on the left and top, [IntSize.width] / [IntSize.height] on the right
 * and bottom). **No side is ever omitted** — "no neighbour" is a measurement against the frame,
 * not a missing value, which is what keeps the panel's five rows always populated.
 *
 * @param target Node being measured.
 * @param neighbours Result of [neighboursOf] for the same target.
 * @param frameSize The frozen frame's size in device pixels — the implicit fifth neighbour.
 */
internal fun gapsOf(
    target: MeasureNode,
    neighbours: MeasureNeighbours,
    frameSize: IntSize,
): MeasureGaps {
    val t = target.rect
    return MeasureGaps(
        leftPx = t.left - (neighbours.left?.rect?.right ?: 0),
        rightPx = (neighbours.right?.rect?.left ?: frameSize.width) - t.right,
        topPx = t.top - (neighbours.top?.rect?.bottom ?: 0),
        bottomPx = (neighbours.bottom?.rect?.top ?: frameSize.height) - t.bottom,
    )
}
