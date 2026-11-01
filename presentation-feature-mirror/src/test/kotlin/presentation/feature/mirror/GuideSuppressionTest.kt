package presentation.feature.mirror

import presentation.feature.mirror.core.measure.guideNumeral
import presentation.feature.mirror.core.measure.plainValueLabel
import presentation.feature.mirror.core.measure.toDisplayUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FR-39 -- guide suppression, evaluated AFTER px->dp conversion, not before. A 2px gap at 420
 * dpi is 0.76 dp pre-rounding: a wrong implementation that suppresses on raw px (where 2 >= 1
 * is trivially always true) would wrongly render this guide, and one that rounds BEFORE
 * comparing (round(0.76) == 1) would wrongly render it too. Both must be caught here.
 */
class GuideSuppressionTest {

    @Test
    fun aSubUnitGapAfterConversionYieldsNoGuideAndNoNumeral() {
        // 2px * 160 / 420 = 0.7619... < 1 unit, even though it would ROUND to 1dp.
        assertNull(guideNumeral(gapPx = 2, densityDpi = 420))
    }

    @Test
    fun aGapOfExactlyOneUnitStillRenders() {
        // 1px at 160dpi is exactly 1.0 dp -- the inclusive boundary of "less than 1".
        assertEquals("1dp", guideNumeral(gapPx = 1, densityDpi = 160))
    }

    @Test
    fun nothingIsEverRoundedUpToOneWhenSuppressed() {
        // Same case as the first test, phrased as the AC's own wording: never shown as a
        // rounded-up "1dp", and never shown as "0dp" either -- just absent.
        val numeral = guideNumeral(gapPx = 2, densityDpi = 420)
        assertNull(numeral)
    }

    @Test
    fun thePanelsOwnValueRowStillPrintsEvenWhenTheGuideForThatSideIsSuppressed() {
        // The guide (dashed line + numeral) is suppressed below 1 unit, but the Top/Right/
        // Bottom/Left VALUE ROW is a separate, always-shown figure and is unaffected.
        val (rowValue, unit) = toDisplayUnit(2, densityDpi = 420)
        assertEquals("1dp", plainValueLabel(rowValue, unit))
        assertNull(guideNumeral(gapPx = 2, densityDpi = 420))
    }
}
