package presentation.feature.mirror.core.measure

import presentation.feature.mirror.source.mirror.MeasureUnit
import kotlin.math.abs

/**
 * Largest top-edge difference from the LEFT neighbour that still reads as a misalignment.
 *
 * In **display units** — see [displayDelta] for why the conversion comes first.
 * `inputs/design-measure-source.md`'s `Math.abs(L.y - t.y) <= 10`.
 */
private const val TOP_MISALIGNMENT_MAX = 10

/**
 * Largest left-edge difference from the TOP neighbour that still reads as a misalignment.
 *
 * In **display units**, same as [TOP_MISALIGNMENT_MAX].
 * `inputs/design-measure-source.md`'s `Math.abs(T.x - t.x) <= 24`.
 */
private const val LEFT_MISALIGNMENT_MAX = 24

/** Height below which a tappable node is reported, in dp. See [touchTargetFinding] on 32-vs-48. */
internal const val TOUCH_TARGET_MIN_DP: Int = 32

/**
 * An edge difference in the display unit the finding will be **labelled** in.
 *
 * The design (`inputs/design-measure-source.md`) works in a single logical coordinate space —
 * its frame is 296×640, i.e. dp-like — so its `10` and `24` mean ten and twenty-four *logical*
 * units. A real dump's coordinates are physical pixels (T-1: 984×2132 at 420 dpi = 375 dp
 * wide), so comparing a raw-px delta against `10` would make the bound ±3.8 dp — 2.6× stricter
 * than designed, and the top-misalignment finding would essentially never fire on that device.
 * Hence conversion first, exactly as this project already extended the guide-suppression rule
 * (FR-39) and for the same reason.
 *
 * Rounded, not fractional, so the bound is checked against the **same number the message
 * prints**: a 1px delta at 420 dpi is 0.38 dp, reads as `0`, and is therefore the good case —
 * which is also why no message can ever say `Δ0dp`.
 *
 * When density is unknown this is the raw px delta and the message carries the `px` suffix it
 * already carries: an honest "this is what was measured" beats going silently dark.
 */
internal fun displayDelta(fromPx: Int, toPx: Int, densityDpi: Int): Int =
    toDisplayUnit(abs(fromPx - toPx), densityDpi).first

/** Which rule produced a [Finding] — the rail is drawn for [TopMisalignment] and nothing else. */
internal enum class FindingKind {
    /** The target's top edge is just off its left neighbour's. */
    TopMisalignment,

    /** The target's left edge is just off its top neighbour's. */
    LeftMisalignment,

    /** A tappable node is shorter than the touch-target floor. */
    TouchTarget,
}

/**
 * The single finding reported for a measured node.
 *
 * @property kind Which rule fired. The UI needs this, not just the message: only
 *   [FindingKind.TopMisalignment] draws an alignment rail.
 * @property message The verbatim design string, already formatted.
 */
internal data class Finding(
    val kind: FindingKind,
    val message: String,
)

/**
 * The one finding worth reporting for [target], or `null` when there is none (FR-40 / AC-61..64).
 *
 * An ordered single-return `when`: top misalignment beats left misalignment beats touch target,
 * first match wins. That ordering IS the never-two-findings invariant — there is no dedup step
 * to forget, because there is only ever one return value.
 *
 * - **Top misalignment** compares the target's top against its LEFT neighbour's and requires a
 *   **non-zero** delta within [TOP_MISALIGNMENT_MAX]: perfectly aligned is the good case.
 * - **Left misalignment** is the same shape on the TOP neighbour's left edge, bounded by
 *   [LEFT_MISALIGNMENT_MAX] — a wider window, because vertical stacks drift further before
 *   it stops looking like an attempt at alignment.
 * - **Touch target** needs a tappable node, a known density and a height under
 *   [TOUCH_TARGET_MIN_DP]. A minimum-touch-size guideline is inherently a dp rule and is
 *   meaningless in raw px, so unknown density silences it outright — unlike the two
 *   misalignment bounds, which stay useful applied to px and say `px` when they are.
 *
 * All three bounds are compared in the display unit actually in use, via [displayDelta] and
 * [toDisplayUnit] — never a raw-px delta against a dp-like threshold.
 *
 * @param target Snapped (or fallback) node being measured.
 * @param neighbours Result of [neighboursOf] for the same target.
 * @param unit Unit the message labels its numbers in.
 * @param densityDpi Device density, `0` when unknown.
 */
internal fun findingOf(
    target: MeasureNode,
    neighbours: MeasureNeighbours,
    unit: MeasureUnit,
    densityDpi: Int,
): Finding? {
    val left = neighbours.left
    val above = neighbours.top
    // 0 doubles as "no such neighbour": a zero delta is the GOOD case and reports nothing
    // either, so the two collapse into one condition rather than needing a nullable delta.
    // Note a delta that converts to 0 (a 1px drift at 420 dpi) is a zero delta here too —
    // the bound and the message read the same number, so neither can disagree with the other.
    val topDelta = if (left == null) 0 else displayDelta(left.rect.top, target.rect.top, densityDpi)
    val leftDelta =
        if (above == null) 0 else displayDelta(above.rect.left, target.rect.left, densityDpi)
    val (heightDisplay, _) = toDisplayUnit(target.rect.height, densityDpi)
    return when {
        left != null && topDelta in 1..TOP_MISALIGNMENT_MAX -> Finding(
            kind = FindingKind.TopMisalignment,
            message = topMisalignmentFinding(delta = topDelta, unit = unit, neighbourId = left.id),
        )

        above != null && leftDelta in 1..LEFT_MISALIGNMENT_MAX -> Finding(
            kind = FindingKind.LeftMisalignment,
            message = leftMisalignmentFinding(
                delta = leftDelta,
                unit = unit,
                neighbourId = above.id,
            ),
        )

        target.isTappable && densityDpi > 0 && heightDisplay < TOUCH_TARGET_MIN_DP -> Finding(
            kind = FindingKind.TouchTarget,
            message = touchTargetFinding(heightDisplay, unit),
        )

        else -> null
    }
}
