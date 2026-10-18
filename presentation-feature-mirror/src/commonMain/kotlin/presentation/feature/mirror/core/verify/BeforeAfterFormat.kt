package presentation.feature.mirror.core.verify

import presentation.feature.mirror.core.measure.SEPARATOR
import presentation.feature.mirror.core.measure.deltaLabel
import presentation.feature.mirror.core.measure.displayDelta
import presentation.feature.mirror.core.measure.plainValueLabel
import presentation.feature.mirror.core.measure.sizeLabel
import presentation.feature.mirror.core.measure.toDisplayUnit
import presentation.feature.mirror.core.measure.unitOf
import presentation.feature.mirror.source.mirror.FindingKind
import presentation.feature.mirror.source.mirror.MeasureReadout
import presentation.feature.mirror.source.mirror.MeasureSide

/** `→`, U+2192 with a space either side — the design's before/after link. Never `->`. */
private const val ARROW = " → "

/**
 * The touch-target size to aim at, in dp.
 *
 * The finding FIRES below 32 (`TOUCH_TARGET_MIN_DP`) while the advice is 48 — two deliberately
 * different numbers kept from the design, see `touchTargetFinding`, which prints the same 48.
 */
private const val TOUCH_TARGET_ADVISED_DP = 48

/**
 * `top Δ6dp → 0dp` — a misalignment that was measured on both frames.
 *
 * The BEFORE side carries the `Δ` prefix (it is the complaint the annotation was about), the
 * AFTER side is a plain value (it is what the edge measures now, `0dp` when it lined up).
 *
 * Both sides go through [displayDelta], so the conversion happens before the rounding exactly
 * once per side and this can never disagree with the finding message or the rail label it
 * illustrates. Density `<= 0` degrades to raw px labelled `px`, as everywhere else.
 *
 * @param side [MeasureSide.Top] or [MeasureSide.Left] — the edge the finding named.
 * @param beforeNeighbourEdgePx The neighbour's facing edge on the BEFORE frame, in device px.
 * @param beforeTargetEdgePx The target's own edge on the BEFORE frame, in device px.
 * @param afterNeighbourEdgePx The neighbour's facing edge on the AFTER frame.
 * @param afterTargetEdgePx The target's own edge on the AFTER frame.
 * @param densityDpi Device density, `0` when unknown.
 */
internal fun misalignmentBeforeAfterLabel(
    side: MeasureSide,
    beforeNeighbourEdgePx: Int,
    beforeTargetEdgePx: Int,
    afterNeighbourEdgePx: Int,
    afterTargetEdgePx: Int,
    densityDpi: Int,
): String {
    val unit = unitOf(densityDpi)
    val before = displayDelta(beforeNeighbourEdgePx, beforeTargetEdgePx, densityDpi)
    val after = displayDelta(afterNeighbourEdgePx, afterTargetEdgePx, densityDpi)
    return "${side.label} ${deltaLabel(before, unit)}$ARROW${plainValueLabel(after, unit)}"
}

/**
 * `height 24dp → 32dp · want 48dp` — a touch target that grew but is still under the guideline.
 *
 * The trailing segment is the advice ([TOUCH_TARGET_ADVISED_DP]), joined with the same U+00B7
 * [SEPARATOR] every other measure string uses.
 */
internal fun touchTargetBeforeAfterLabel(
    beforeHeightPx: Int,
    afterHeightPx: Int,
    densityDpi: Int,
): String {
    val unit = unitOf(densityDpi)
    val before = toDisplayUnit(beforeHeightPx, densityDpi).first
    val after = toDisplayUnit(afterHeightPx, densityDpi).first
    return "height ${plainValueLabel(before, unit)}$ARROW${plainValueLabel(after, unit)}" +
        "${SEPARATOR}want $TOUCH_TARGET_ADVISED_DP${unit.suffix}"
}

/**
 * `48×17dp → 48×20dp` — the fallback when neither frame reported a finding.
 *
 * The design's prototype copy for this row ("placeholder fits · no clipping") has no computable
 * source, so the honest line is the one measurement that always exists: the node's own size on
 * each frame.
 */
internal fun sizeBeforeAfterLabel(
    beforeWidthPx: Int,
    beforeHeightPx: Int,
    afterWidthPx: Int,
    afterHeightPx: Int,
    densityDpi: Int,
): String {
    val unit = unitOf(densityDpi)
    val display = { px: Int -> toDisplayUnit(px, densityDpi).first }
    return sizeLabel(display(beforeWidthPx), display(beforeHeightPx), unit) + ARROW +
        sizeLabel(display(afterWidthPx), display(afterHeightPx), unit)
}

/**
 * The one result line of a verify row: the BEFORE finding's own measurement on both frames.
 *
 * The **BEFORE** readout picks the line, never the AFTER one. The row is about the complaint
 * the annotation was filed over, and the good outcome is precisely the one where the AFTER
 * side has no finding left to name — reading the kind off the AFTER side would silence the
 * line exactly when it matters most.
 *
 * Falls back to [sizeBeforeAfterLabel] in the two cases where there is nothing better to say:
 * no BEFORE finding at all (including every batch stored before the hierarchy was parsed back,
 * whose BEFORE readout snaps to nothing), and a misalignment whose neighbour has no counterpart
 * on the other frame. Both sides are measured at the same density — the fresh AFTER one, since
 * the stored dump never carried its own.
 *
 * @param before The annotation measured against the hierarchy as it was SENT.
 * @param after The same annotation measured against the freshly dumped hierarchy.
 * @param densityDpi Device density, `0` when unknown.
 */
internal fun beforeAfterLine(
    before: MeasureReadout,
    after: MeasureReadout,
    densityDpi: Int,
): String = when (before.findingKind) {
    FindingKind.TopMisalignment -> misalignmentLineOrNull(
        side = MeasureSide.Top,
        beforeNeighbourEdgePx = before.leftNeighbourTopPx,
        beforeTargetEdgePx = before.targetRect.top,
        afterNeighbourEdgePx = after.leftNeighbourTopPx,
        afterTargetEdgePx = after.targetRect.top,
        densityDpi = densityDpi,
    )

    FindingKind.LeftMisalignment -> misalignmentLineOrNull(
        side = MeasureSide.Left,
        beforeNeighbourEdgePx = before.topNeighbourLeftPx,
        beforeTargetEdgePx = before.targetRect.left,
        afterNeighbourEdgePx = after.topNeighbourLeftPx,
        afterTargetEdgePx = after.targetRect.left,
        densityDpi = densityDpi,
    )

    FindingKind.TouchTarget -> touchTargetBeforeAfterLabel(
        beforeHeightPx = before.targetRect.height,
        afterHeightPx = after.targetRect.height,
        densityDpi = densityDpi,
    )

    null -> null
} ?: sizeBeforeAfterLabel(
    beforeWidthPx = before.targetRect.width,
    beforeHeightPx = before.targetRect.height,
    afterWidthPx = after.targetRect.width,
    afterHeightPx = after.targetRect.height,
    densityDpi = densityDpi,
)

/** [misalignmentBeforeAfterLabel], or `null` when either frame has no neighbour on that side. */
private fun misalignmentLineOrNull(
    side: MeasureSide,
    beforeNeighbourEdgePx: Int?,
    beforeTargetEdgePx: Int,
    afterNeighbourEdgePx: Int?,
    afterTargetEdgePx: Int,
    densityDpi: Int,
): String? {
    if (beforeNeighbourEdgePx == null || afterNeighbourEdgePx == null) return null
    return misalignmentBeforeAfterLabel(
        side = side,
        beforeNeighbourEdgePx = beforeNeighbourEdgePx,
        beforeTargetEdgePx = beforeTargetEdgePx,
        afterNeighbourEdgePx = afterNeighbourEdgePx,
        afterTargetEdgePx = afterTargetEdgePx,
        densityDpi = densityDpi,
    )
}
