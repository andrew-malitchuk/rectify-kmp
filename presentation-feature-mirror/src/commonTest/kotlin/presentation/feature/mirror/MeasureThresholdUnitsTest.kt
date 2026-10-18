package presentation.feature.mirror

import presentation.feature.mirror.core.measure.MeasureNeighbours
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.findingOf
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FindingKind
import presentation.feature.mirror.source.mirror.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FR-40 -- the three finding thresholds, in the unit actually in use.
 *
 * `FindingsTest` pins the rules at 160 dpi, where `px == dp` and therefore NO assertion can
 * distinguish "compare raw px" from "compare converted dp". That is exactly what hid a real
 * defect: the 10/24 misalignment bounds compared raw device pixels against numbers the design
 * means in its own dp-like 296x640 space, making them 2.6x stricter than designed on T-1's
 * 420 dpi Pixel 8 -- a finding that silently stops firing, which is worse than one that is
 * absent, because the panel then looks like it checked.
 *
 * So every case here runs at **420 dpi**, where `dp = px * 160 / 420`, and every delta is
 * chosen so the px and dp readings genuinely disagree about which side of the bound they are
 * on. Each of the first two tests FAILS against the raw-px implementation.
 */
class MeasureThresholdUnitsTest {

    /** T-1's measured Pixel 8 density: 160/420 means 21px is exactly 8dp and 42px exactly 16dp. */
    private val pixel8Dpi = 420

    private fun node(
        id: String,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        tappable: Boolean = false,
    ): MeasureNode =
        MeasureNode(id = id, rect = DeviceRect(left, top, right, bottom), isTappable = tappable)

    private val target = node("item_price", 100, 100, 300, 140)
    private val noNeighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)

    @Test
    fun topMisalignmentFiresOnADeltaOverTenPxThatIsUnderTenDpOnceConverted() {
        // top delta = 21px = exactly 8dp at 420 dpi. Raw px reads 21 > 10 and stays silent;
        // the design's bound is 10 logical units, and 8 <= 10, so it must fire.
        val leftNeighbour = node("item_title", 0, 79, 90, 119)
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        val finding = findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = pixel8Dpi)
        assertEquals(FindingKind.TopMisalignment, finding?.kind)
        assertEquals("top Δ8dp vs item_title · expected 0", finding?.message)
    }

    @Test
    fun leftMisalignmentFiresOnADeltaOverTwentyFourPxThatIsUnderTwentyFourDpOnceConverted() {
        // left delta = 42px = exactly 16dp at 420 dpi. Raw px reads 42 > 24 and stays silent.
        val topNeighbour = node("item_icon", 58, 40, 160, 90)
        val neighbours = noNeighbours.copy(top = topNeighbour)
        val finding = findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = pixel8Dpi)
        assertEquals(FindingKind.LeftMisalignment, finding?.kind)
        assertEquals("left Δ16dp vs item_icon · expected 0", finding?.message)
    }

    @Test
    fun topMisalignmentStillStopsAtTheBoundWhenTheConvertedDeltaExceedsIt() {
        // top delta = 29px = 11.05dp -> 11 > 10. Converting is not the same as loosening.
        val leftNeighbour = node("item_title", 0, 71, 90, 111)
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        assertNull(findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = pixel8Dpi))
    }

    @Test
    fun aDriftTooSmallToShowAsANonZeroFigureIsTreatedAsAlignedRatherThanReportedAsZero() {
        // top delta = 1px = 0.38dp, which reads as 0. "Δ0dp vs x · expected 0" would be
        // nonsense, so the bound and the message read the same converted number.
        val leftNeighbour = node("item_title", 0, 99, 90, 139)
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        assertNull(findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = pixel8Dpi))
    }

    @Test
    fun withUnknownDensityTheMisalignmentBoundsApplyToPxAndTheMessageSaysSo() {
        // Honest rather than dark: it says what it measured, in the unit it measured it in.
        val leftNeighbour = node("item_title", 0, 92, 90, 132) // top delta = 8px
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        val finding = findingOf(target, neighbours, MeasureUnit.Px, densityDpi = 0)
        assertEquals(FindingKind.TopMisalignment, finding?.kind)
        assertEquals("top Δ8px vs item_title · expected 0", finding?.message)
    }

    @Test
    fun withUnknownDensityAPxDeltaOverTheBoundIsStillOverTheBound() {
        val leftNeighbour = node("item_title", 0, 79, 90, 119) // top delta = 21px
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        assertNull(findingOf(target, neighbours, MeasureUnit.Px, densityDpi = 0))
    }

    @Test
    fun theTouchTargetBoundIsReadInDpAndNotInPx() {
        // 63px tall = 24dp at 420 dpi: under the 32 floor, so it fires -- and the figure in the
        // message is the dp one, never the 63 that would sail past the floor as raw px.
        val shortTarget = node("item_button", 100, 100, 300, 163, tappable = true)
        val finding = findingOf(shortTarget, noNeighbours, MeasureUnit.Dp, densityDpi = pixel8Dpi)
        assertEquals(FindingKind.TouchTarget, finding?.kind)
        assertEquals("height 24dp · min touch 48dp", finding?.message)
    }

    @Test
    fun theTouchTargetFloorIsInclusiveAtNonBaselineDensityToo() {
        // 84px tall = exactly 32dp, so it does NOT fire -- the same inclusive boundary
        // FindingsTest pins at 160 dpi, re-checked where the conversion is not the identity.
        val borderlineTarget = node("item_button", 100, 100, 300, 184, tappable = true)
        assertNull(findingOf(borderlineTarget, noNeighbours, MeasureUnit.Dp, densityDpi = pixel8Dpi))
    }
}
