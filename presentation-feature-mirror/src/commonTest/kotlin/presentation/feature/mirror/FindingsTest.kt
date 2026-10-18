package presentation.feature.mirror

import presentation.feature.mirror.core.measure.FindingKind
import presentation.feature.mirror.core.measure.MeasureNeighbours
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.findingOf
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MeasureUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FR-40 / AC-61..64 -- findingOf: an ordered, single-return selector. Top misalignment
 * (+/-10px vs the LEFT neighbour) beats left misalignment (+/-24px vs the TOP neighbour) beats
 * touch target (tappable && height < 32dp). Density 160 is used throughout so px == dp and the
 * numbers in the assertions are exact, not rounded.
 */
class FindingsTest {

    private fun node(
        id: String,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        tappable: Boolean = false,
    ): MeasureNode = MeasureNode(id = id, rect = DeviceRect(left, top, right, bottom), isTappable = tappable)

    private val target = node("item_price", 100, 100, 300, 140)
    private val noNeighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)

    @Test
    fun topMisalignmentFiresWithinTenPxOfTheLeftNeighbour() {
        val leftNeighbour = node("item_title", 0, 94, 90, 134) // top delta = 6
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        val finding = findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = 160)
        assertEquals(FindingKind.TopMisalignment, finding?.kind)
        assertEquals("top Δ6dp vs item_title · expected 0", finding?.message)
    }

    @Test
    fun topMisalignmentDoesNotFireAtElevenPx() {
        val leftNeighbour = node("item_title", 0, 89, 90, 129) // top delta = 11
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        assertNull(findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = 160))
    }

    @Test
    fun topMisalignmentDoesNotFireOnAZeroDelta() {
        val leftNeighbour = node("item_title", 0, 100, 90, 140) // top delta = 0
        val neighbours = noNeighbours.copy(left = leftNeighbour)
        assertNull(findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = 160))
    }

    @Test
    fun leftMisalignmentFiresWithinTwentyFourPxOfTheTopNeighbourWhenTopDoesNotApply() {
        val topNeighbour = node("item_icon", 91, 40, 160, 90) // left delta = 9
        val neighbours = noNeighbours.copy(top = topNeighbour)
        val finding = findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = 160)
        assertEquals(FindingKind.LeftMisalignment, finding?.kind)
        assertEquals("left Δ9dp vs item_icon · expected 0", finding?.message)
    }

    @Test
    fun leftMisalignmentDoesNotFireAtTwentyFivePx() {
        val topNeighbour = node("item_icon", 75, 40, 160, 90) // left delta = 25
        val neighbours = noNeighbours.copy(top = topNeighbour)
        assertNull(findingOf(target, neighbours, MeasureUnit.Dp, densityDpi = 160))
    }

    @Test
    fun touchTargetFiresWhenNeitherAlignmentFindingAppliesAndTheNodeIsTappableAndUndersized() {
        val shortTarget = node("item_button", 100, 100, 300, 120, tappable = true) // 20dp tall
        val finding = findingOf(shortTarget, noNeighbours, MeasureUnit.Dp, densityDpi = 160)
        assertEquals(FindingKind.TouchTarget, finding?.kind)
        assertEquals("height 20dp · min touch 48dp", finding?.message)
    }

    @Test
    fun touchTargetDoesNotFireAtExactlyThirtyTwoDp() {
        val borderlineTarget = node("item_button", 100, 100, 300, 132, tappable = true)
        assertNull(findingOf(borderlineTarget, noNeighbours, MeasureUnit.Dp, densityDpi = 160))
    }

    @Test
    fun touchTargetDoesNotFireWhenNotTappable() {
        val shortButUntappable = node("item_label", 100, 100, 300, 120, tappable = false)
        assertNull(findingOf(shortButUntappable, noNeighbours, MeasureUnit.Dp, densityDpi = 160))
    }

    @Test
    fun touchTargetNeverFiresWhenDensityIsUnknownEvenIfOtherwiseTrue() {
        val shortTarget = node("item_button", 100, 100, 300, 120, tappable = true)
        assertNull(findingOf(shortTarget, noNeighbours, MeasureUnit.Px, densityDpi = 0))
    }

    @Test
    fun allThreeConditionsTrueAtOnceStillReturnsExactlyTheTopFinding() {
        val shortTappableTarget = node("item_price", 100, 100, 300, 120, tappable = true) // 20dp tall
        val leftNeighbour = node("item_title", 0, 94, 90, 134) // top delta = 6, fires top
        val topNeighbour = node("item_icon", 91, 40, 160, 90) // left delta = 9, would fire left
        val neighbours = MeasureNeighbours(left = leftNeighbour, right = null, top = topNeighbour, bottom = null)
        val finding = findingOf(shortTappableTarget, neighbours, MeasureUnit.Dp, densityDpi = 160)
        assertEquals(FindingKind.TopMisalignment, finding?.kind)
        assertEquals("top Δ6dp vs item_title · expected 0", finding?.message)
    }

    @Test
    fun aWellBehavedTargetWithNoNeighboursReturnsNoFinding() {
        val wellBehaved = node("item_price", 100, 100, 300, 148, tappable = true) // 48dp tall
        assertNull(findingOf(wellBehaved, noNeighbours, MeasureUnit.Dp, densityDpi = 160))
    }
}
