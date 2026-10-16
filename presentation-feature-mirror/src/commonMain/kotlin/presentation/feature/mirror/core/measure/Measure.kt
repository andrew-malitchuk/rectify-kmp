package presentation.feature.mirror.core.measure

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MeasureGuide
import presentation.feature.mirror.source.mirror.MeasureReadout
import presentation.feature.mirror.source.mirror.MeasureSide
import presentation.feature.mirror.source.mirror.MeasureUnit
import presentation.feature.mirror.source.mirror.MeasureValue

/**
 * Node id reported when nothing snapped — verbatim from the design, which uses the same string.
 *
 * "No match" is a first-class outcome (FR-41), not an error: every value is still measured,
 * against the annotation's own rect and the frame edges.
 */
internal const val NO_MATCH_NODE_ID: String = "View (no match)"

/**
 * One annotation measured against one hierarchy — the whole feature's arithmetic, composed.
 *
 * Mirrors the design's own `measure(annotation, unit)`: snap (or fall back), find the four
 * neighbours, measure the four gaps, convert and format, pick the single finding, and draw the
 * alignment rail for the top-misalignment case only.
 *
 * Linear in [nodes] — five single passes, no nesting — which is what keeps a 300-node dump well
 * inside the ~100 ms budget (NFR-2 / NFR-13).
 *
 * @param annotation The drawn rectangle, in device pixels.
 * @param index The annotation's 1-based badge number, carried through for the UI.
 * @param nodes Projected candidates from [projectMeasureNodes]; may be empty.
 * @param frameSize The frozen frame's size in device pixels.
 * @param densityDpi Device density, `0` when unknown — then every figure is labelled `px`.
 */
internal fun measure(
    annotation: DeviceRect,
    index: Int,
    nodes: List<MeasureNode>,
    frameSize: IntSize,
    densityDpi: Int,
): MeasureReadout {
    val target = snapTarget(annotation, nodes)
        ?: MeasureNode(id = NO_MATCH_NODE_ID, rect = annotation, isTappable = false)
    val neighbours = neighboursOf(target, nodes)
    val gaps = gapsOf(target, neighbours, frameSize)
    val unit = unitOf(densityDpi)
    val rect = target.rect
    val display = { px: Int -> toDisplayUnit(px, densityDpi).first }

    val values = listOf(
        MeasureValue(MeasureSide.Size, sizeLabel(display(rect.width), display(rect.height), unit)),
        MeasureValue(MeasureSide.Top, plainValueLabel(display(gaps.topPx), unit)),
        MeasureValue(MeasureSide.Right, plainValueLabel(display(gaps.rightPx), unit)),
        MeasureValue(MeasureSide.Bottom, plainValueLabel(display(gaps.bottomPx), unit)),
        MeasureValue(MeasureSide.Left, plainValueLabel(display(gaps.leftPx), unit)),
    )

    val guides = gapGuides(rect, gaps, densityDpi)

    val finding = findingOf(target, neighbours, unit, densityDpi)
    val rail = railOf(finding, target, neighbours.left, densityDpi, unit)

    return MeasureReadout(
        annotationIndex = index,
        nodeId = target.id,
        targetRect = rect,
        // [L, R, T, B] in the design's own order, absent sides dropped. The 1px height floor is
        // the design's `Math.max(e.h, 1)`, so a 1px divider stays a visible outline.
        neighbourRects = with(neighbours) { listOfNotNull(left, right, top, bottom) }
            .map { it.rect.withMinimumHeight() },
        values = values,
        guides = guides,
        rail = rail,
        finding = finding?.message,
        unit = unit,
        noteLine = noteLine(nodeId = target.id, values = values, finding = finding?.message),
    )
}

/**
 * The four gap guides, each running from the neighbour's facing edge to the target's, along the
 * target's own centre line. A side whose gap is under one display unit is **absent** (FR-39).
 *
 * Geometry is `inputs/design-measure-source.md`'s own `H`/`V` helpers, in its own order
 * (left, right, top, bottom): horizontals at the target's mid-y spanning `lx..t.x` and
 * `t.x+t.w..rx`, verticals at its mid-x spanning `ty..t.y` and `t.y+t.h..by`. The design's
 * `t.y + t.h / 2` is a real-valued half-pixel on an odd height (206.5 for `item_price`); here
 * it truncates, because the overlay draws on an integer pixel grid and half a pixel either way
 * is not a placement anyone can see.
 */
private fun gapGuides(rect: DeviceRect, gaps: MeasureGaps, densityDpi: Int): List<MeasureGuide> {
    val midX = rect.left + rect.width / 2
    val midY = rect.top + rect.height / 2
    return listOfNotNull(
        guideOf(
            MeasureSide.Left, gaps.leftPx, densityDpi,
            from = IntOffset(rect.left - gaps.leftPx, midY), to = IntOffset(rect.left, midY),
        ),
        guideOf(
            MeasureSide.Right, gaps.rightPx, densityDpi,
            from = IntOffset(rect.right, midY), to = IntOffset(rect.right + gaps.rightPx, midY),
        ),
        guideOf(
            MeasureSide.Top, gaps.topPx, densityDpi,
            from = IntOffset(midX, rect.top - gaps.topPx), to = IntOffset(midX, rect.top),
        ),
        guideOf(
            MeasureSide.Bottom, gaps.bottomPx, densityDpi,
            from = IntOffset(midX, rect.bottom), to = IntOffset(midX, rect.bottom + gaps.bottomPx),
        ),
    )
}

/** One gap guide, or `null` when its gap is under a display unit — see [guideNumeral]. */
private fun guideOf(
    side: MeasureSide,
    gapPx: Int,
    densityDpi: Int,
    from: IntOffset,
    to: IntOffset,
): MeasureGuide? = guideNumeral(gapPx, densityDpi)
    ?.let { MeasureGuide(side = side, label = it, from = from, to = to) }

/**
 * The alignment rail, for the top-misalignment branch **only**.
 *
 * Not a general "there is a finding" flag: the rail means "these two edges should have lined
 * up", which the left-misalignment and touch-target findings do not say. It runs from the left
 * neighbour's left edge, along the neighbour's own top, to the target's right edge.
 */
private fun railOf(
    finding: Finding?,
    target: MeasureNode,
    left: MeasureNode?,
    densityDpi: Int,
    unit: MeasureUnit,
): MeasureGuide? {
    if (finding?.kind != FindingKind.TopMisalignment || left == null) return null
    return MeasureGuide(
        side = MeasureSide.Top,
        // The same [displayDelta] the finding itself used, so the rail's label can never
        // disagree with the message it illustrates.
        label = deltaLabel(displayDelta(left.rect.top, target.rect.top, densityDpi), unit),
        from = IntOffset(left.rect.left, left.rect.top),
        to = IntOffset(target.rect.right, left.rect.top),
    )
}

private fun DeviceRect.withMinimumHeight(): DeviceRect =
    if (height >= 1) this else copy(bottom = top + 1)
