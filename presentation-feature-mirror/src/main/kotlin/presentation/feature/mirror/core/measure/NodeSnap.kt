package presentation.feature.mirror.core.measure

import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * Slack, in device pixels, allowed on each edge when testing containment.
 *
 * The user drags a rectangle by hand, so an annotation meant to sit on a node lands a pixel or
 * two outside it. Four px per edge is the design's own figure (`e.x >= a.x - 4` and friends).
 */
private const val SNAP_SLACK_PX = 4

/**
 * The node a hand-drawn annotation is measuring: the **largest-area** candidate fully contained
 * in the annotation, allowing [SNAP_SLACK_PX] of slack per edge (FR-36 / AC-57).
 *
 * Largest-area and not nearest-centre: a row and the label inside it are both contained by a
 * box drawn over the row, and the user who drew a row-sized box meant the row. Not
 * first-found either — document order would make the answer depend on the dump's traversal.
 *
 * Ties on area keep input order (`maxByOrNull` returns the first maximal element), which is
 * document order, so the answer is deterministic rather than id-sorted.
 *
 * @param annotation The drawn rectangle, in device pixels.
 * @param candidates Projected nodes; the full-frame root must already be gone (see
 *   [projectMeasureNodes]) or it wins every time.
 * @return The snapped node, or `null` when nothing qualifies — a first-class outcome handled by
 *   [measure]'s `View (no match)` fallback, not an error.
 */
internal fun snapTarget(annotation: DeviceRect, candidates: List<MeasureNode>): MeasureNode? =
    candidates
        .filter { it.rect.isContainedWithinSlack(annotation) }
        .maxByOrNull { it.rect.width * it.rect.height }

private fun DeviceRect.isContainedWithinSlack(annotation: DeviceRect): Boolean =
    left >= annotation.left - SNAP_SLACK_PX &&
        top >= annotation.top - SNAP_SLACK_PX &&
        right <= annotation.right + SNAP_SLACK_PX &&
        bottom <= annotation.bottom + SNAP_SLACK_PX
