package presentation.feature.mirror

import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.neighboursOf
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FR-37 / AC-58 -- neighboursOf: per side, the nearest non-overlapping node under the
 * band-overlap + closest-edge rule. A node diagonal to the target must be excluded on BOTH
 * axes, not picked as a neighbour on either.
 */
class NeighboursTest {

    private fun node(id: String, left: Int, top: Int, right: Int, bottom: Int): MeasureNode =
        MeasureNode(id = id, rect = DeviceRect(left, top, right, bottom), isTappable = false)

    // target: (100,100)-(200,150), 100 wide, 50 tall.
    private val target = node("target", 100, 100, 200, 150)

    @Test
    fun leftPicksTheRightMostOfTheCandidatesLeftOfTarget() {
        val nearer = node("left_near", 0, 110, 80, 140) // right edge 80
        val farther = node("left_far", 0, 110, 60, 140) // right edge 60
        val result = neighboursOf(target, listOf(farther, nearer))
        assertEquals(nearer, result.left)
    }

    @Test
    fun rightPicksTheLeftMostOfTheCandidatesRightOfTarget() {
        val nearer = node("right_near", 220, 110, 300, 140) // left edge 220
        val farther = node("right_far", 260, 110, 320, 140) // left edge 260
        val result = neighboursOf(target, listOf(farther, nearer))
        assertEquals(nearer, result.right)
    }

    @Test
    fun topPicksTheLowestOfTheCandidatesAboveTarget() {
        val nearer = node("top_near", 110, 40, 190, 90) // bottom edge 90
        val farther = node("top_far", 110, 10, 190, 50) // bottom edge 50
        val result = neighboursOf(target, listOf(farther, nearer))
        assertEquals(nearer, result.top)
    }

    @Test
    fun bottomPicksTheHighestOfTheCandidatesBelowTarget() {
        val nearer = node("bottom_near", 110, 160, 190, 200) // top edge 160
        val farther = node("bottom_far", 110, 180, 190, 220) // top edge 180
        val result = neighboursOf(target, listOf(farther, nearer))
        assertEquals(nearer, result.bottom)
    }

    @Test
    fun aNodeDiagonalToTheTargetIsExcludedOnBothAxes() {
        // Up and to the left, with no vertical OR horizontal band overlap with target.
        val diagonal = node("diagonal", 0, 0, 80, 80)
        val result = neighboursOf(target, listOf(diagonal))
        assertNull(result.left)
        assertNull(result.right)
        assertNull(result.top)
        assertNull(result.bottom)
    }

    @Test
    fun aNodeOverlappingTheTargetsRectIsNeverPickedOnAnySide() {
        // Intersects target's own rect directly.
        val overlapping = node("overlapping", 150, 90, 250, 160)
        val result = neighboursOf(target, listOf(overlapping))
        assertNull(result.left)
        assertNull(result.right)
        assertNull(result.top)
        assertNull(result.bottom)
    }

    @Test
    fun aNodeExactlyTouchingTheLeftEdgeIsIncludedByTheLessOrEqualForm() {
        // right edge == target.left exactly.
        val touching = node("touching", 0, 110, 100, 140)
        val result = neighboursOf(target, listOf(touching))
        assertEquals(touching, result.left)
    }

    @Test
    fun sidesWithNoQualifyingCandidateAreIndependentlyNull() {
        val onlyLeft = node("left_only", 0, 110, 80, 140)
        val result = neighboursOf(target, listOf(onlyLeft))
        assertEquals(onlyLeft, result.left)
        assertNull(result.right)
        assertNull(result.top)
        assertNull(result.bottom)
    }

    @Test
    fun noCandidatesAtAllYieldsAllFourSidesNull() {
        val result = neighboursOf(target, emptyList())
        assertNull(result.left)
        assertNull(result.right)
        assertNull(result.top)
        assertNull(result.bottom)
    }
}
