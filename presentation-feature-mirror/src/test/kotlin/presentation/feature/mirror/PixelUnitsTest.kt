package presentation.feature.mirror

import presentation.feature.mirror.core.measure.toDisplayUnit
import presentation.feature.mirror.source.mirror.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * toDisplayUnit: px * 160 / densityDpi, rounded to the nearest whole unit; MeasureUnit.Px with
 * the raw px value whenever densityDpi is unknown or non-positive -- the degradation gate
 * ruling's "label everything px rather than show a wrong dp".
 */
class PixelUnitsTest {

    @Test
    fun theReferenceDeviceDensityConvertsTheRootWidthToTheKnownDpFigure() {
        // Measured on the Pixel 8 spike: 420 dpi, override resolution 1080 wide -> 411 dp.
        val (value, unit) = toDisplayUnit(1080, densityDpi = 420)
        assertEquals(411, value)
        assertEquals(MeasureUnit.Dp, unit)
    }

    @Test
    fun oneSixtyDpiIsExactlyOnePxPerDp() {
        val (value, unit) = toDisplayUnit(1, densityDpi = 160)
        assertEquals(1, value)
        assertEquals(MeasureUnit.Dp, unit)
    }

    @Test
    fun zeroDensityDegradesToTheRawPxValueLabelledPx() {
        val (value, unit) = toDisplayUnit(37, densityDpi = 0)
        assertEquals(37, value)
        assertEquals(MeasureUnit.Px, unit)
    }

    @Test
    fun negativeDensityAlsoDegradesToPxRatherThanAWrongDp() {
        val (value, unit) = toDisplayUnit(50, densityDpi = -5)
        assertEquals(50, value)
        assertEquals(MeasureUnit.Px, unit)
    }

    @Test
    fun aFractionalResultRoundsToTheNearestWholeUnit() {
        // 5 * 160 / 420 = 1.904... -> rounds up to 2, not truncated down to 1.
        val (value, unit) = toDisplayUnit(5, densityDpi = 420)
        assertEquals(2, value)
        assertEquals(MeasureUnit.Dp, unit)
    }
}
