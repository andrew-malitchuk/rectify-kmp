package presentation.feature.mirror.core.measure

import presentation.feature.mirror.source.mirror.MeasureUnit
import kotlin.math.roundToInt

/** Dpi of the dp baseline — `1dp == 1px` at 160 dpi, by Android's definition. */
private const val BASELINE_DPI = 160.0

/**
 * A device-pixel measurement in display units, **unrounded**.
 *
 * Only the suppression rule needs the fraction (a 2px gap at 420 dpi is 0.76 dp, which must
 * stay suppressed even though it rounds to `1dp`), so this is deliberately separate from
 * [toDisplayUnit] rather than folded into it.
 *
 * @param px Measurement in device pixels.
 * @param densityDpi Device density, or `0`/negative when unknown — then px is already the
 *   display value.
 */
internal fun displayValueOf(px: Int, densityDpi: Int): Double =
    if (densityDpi <= 0) px.toDouble() else px * BASELINE_DPI / densityDpi

/**
 * A device-pixel measurement as a whole display value plus the unit it is honestly labelled in.
 *
 * `px * 160 / densityDpi`, rounded to nearest — never truncated, so a 1.9 dp gap reads `2dp`.
 *
 * An unknown density ([domain.core.source.model.device.HierarchySnapshotModel.densityDpi]'s `0`
 * sentinel, or anything non-positive) degrades to the **raw px value labelled `px`**. That is
 * the standing rule from the degradation gate: a wrong density scales every figure it touches,
 * so an honest `px` beats a confident wrong `dp`. Note it does **not** divide by the frame —
 * a zero-node snapshot can carry a real density with `frameWidthPx == 0`.
 *
 * @return The whole display value and its unit, destructurable as `val (value, unit) = ...`.
 */
internal fun toDisplayUnit(px: Int, densityDpi: Int): Pair<Int, MeasureUnit> =
    if (densityDpi <= 0) {
        px to MeasureUnit.Px
    } else {
        displayValueOf(px, densityDpi).roundToInt() to MeasureUnit.Dp
    }

/** The unit a device of this density can be honestly labelled in. See [toDisplayUnit]. */
internal fun unitOf(densityDpi: Int): MeasureUnit =
    if (densityDpi <= 0) MeasureUnit.Px else MeasureUnit.Dp
