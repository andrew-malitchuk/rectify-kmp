package presentation.feature.mirror.core.measure

import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * One hierarchy node as the measure arithmetic sees it, in **device pixels**.
 *
 * Deliberately smaller than `HierarchyNodeModel`: this package needs an identifier, a rect and
 * the tappable flag and nothing else, so `className`/`depth` stay behind in
 * [presentation.feature.mirror.core.measure.projectMeasureNodes]. Keeping the measure types free
 * of the domain model is also what lets every rule in this package be exercised from a literal
 * fixture — the design's own `ELS` array — with no snapshot plumbing.
 *
 * @property id Display label, already resolved by the data layer's four-step
 *   `resource-id → text → content-desc → class[n]` chain. Nothing here re-derives it.
 * @property rect Bounds in device pixels, already normalised.
 * @property isTappable Whether the node reports itself clickable — the touch-target finding's
 *   only gate.
 */
internal data class MeasureNode(
    val id: String,
    val rect: DeviceRect,
    val isTappable: Boolean,
)

/**
 * The nearest non-overlapping node on each side of the target, or `null` on a side with none.
 *
 * All four sides are independent: a target against the left frame edge legitimately has no left
 * neighbour while still having the other three.
 */
internal data class MeasureNeighbours(
    val left: MeasureNode?,
    val right: MeasureNode?,
    val top: MeasureNode?,
    val bottom: MeasureNode?,
)

/**
 * The four gaps around the target, in **device pixels** — never display units.
 *
 * Px and not dp because suppression (FR-39) and the panel's value rows need the same raw
 * measurement converted at different moments: the value row rounds it, the guide compares it
 * against one display unit BEFORE rounding. Converting here would destroy the information the
 * suppression rule needs.
 */
internal data class MeasureGaps(
    val leftPx: Int,
    val rightPx: Int,
    val topPx: Int,
    val bottomPx: Int,
)
