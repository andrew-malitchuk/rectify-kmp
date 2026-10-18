package presentation.feature.mirror

import presentation.feature.mirror.core.verify.misalignmentBeforeAfterLabel
import presentation.feature.mirror.core.verify.sizeBeforeAfterLabel
import presentation.feature.mirror.core.verify.touchTargetBeforeAfterLabel
import presentation.feature.mirror.source.mirror.MeasureSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * AC-41 -- the before/after strings the verification rows render, composed from F-5's own
 * labels so no unit conversion is reimplemented. Density 160 is the dp baseline (1px == 1dp),
 * which keeps the fixtures readable; the 420 dpi case proves the conversion is the shared one.
 */
class BeforeAfterFormatTest {

    private val baselineDpi = 160

    @Test
    fun aTopMisalignmentThatWentToZeroRendersTheDesignsString() {
        val label = misalignmentBeforeAfterLabel(
            side = MeasureSide.Top,
            beforeNeighbourEdgePx = 100,
            beforeTargetEdgePx = 106,
            afterNeighbourEdgePx = 100,
            afterTargetEdgePx = 100,
            densityDpi = baselineDpi,
        )

        assertEquals("top Δ6dp → 0dp", label)
        assertTrue('Δ' in label)
    }

    @Test
    fun theLeftEdgeUsesItsOwnSideLabel() {
        val label = misalignmentBeforeAfterLabel(
            side = MeasureSide.Left,
            beforeNeighbourEdgePx = 40,
            beforeTargetEdgePx = 49,
            afterNeighbourEdgePx = 40,
            afterTargetEdgePx = 42,
            densityDpi = baselineDpi,
        )

        assertEquals("left Δ9dp → 2dp", label)
    }

    @Test
    fun theDeltaIsConvertedBeforeItIsRoundedJustLikeTheFindingItIllustrates() {
        // 16px at 420 dpi is 6.1dp; 2px is 0.76dp and reads 1dp.
        val label = misalignmentBeforeAfterLabel(
            side = MeasureSide.Top,
            beforeNeighbourEdgePx = 100,
            beforeTargetEdgePx = 116,
            afterNeighbourEdgePx = 100,
            afterTargetEdgePx = 102,
            densityDpi = 420,
        )

        assertEquals("top Δ6dp → 1dp", label)
    }

    @Test
    fun anUnknownDensityDegradesToRawPixels() {
        val label = misalignmentBeforeAfterLabel(
            side = MeasureSide.Top,
            beforeNeighbourEdgePx = 100,
            beforeTargetEdgePx = 106,
            afterNeighbourEdgePx = 100,
            afterTargetEdgePx = 100,
            densityDpi = 0,
        )

        assertEquals("top Δ6px → 0px", label)
    }

    @Test
    fun aTouchTargetThatGrewButStillFailsCarriesTheAdviceAfterTheMiddleDot() {
        val label = touchTargetBeforeAfterLabel(
            beforeHeightPx = 24,
            afterHeightPx = 32,
            densityDpi = baselineDpi,
        )

        assertEquals("height 24dp → 32dp · want 48dp", label)
        assertTrue('·' in label)
    }

    @Test
    fun withNoFindingOnEitherSideTheSizeLineIsTheFallback() {
        val label = sizeBeforeAfterLabel(
            beforeWidthPx = 48,
            beforeHeightPx = 17,
            afterWidthPx = 48,
            afterHeightPx = 20,
            densityDpi = baselineDpi,
        )

        assertEquals("48×17dp → 48×20dp", label)
    }

    @Test
    fun theSizeFallbackNeverFabricatesTheDesignsPrototypeCopy() {
        val label = sizeBeforeAfterLabel(
            beforeWidthPx = 48,
            beforeHeightPx = 17,
            afterWidthPx = 48,
            afterHeightPx = 17,
            densityDpi = 0,
        )

        assertEquals("48×17px → 48×17px", label)
        assertTrue("placeholder" !in label)
    }
}
