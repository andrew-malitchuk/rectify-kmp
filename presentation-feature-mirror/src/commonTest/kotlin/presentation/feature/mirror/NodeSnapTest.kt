package presentation.feature.mirror

import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.snapTarget
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FR-36 / AC-57 -- snapTarget: largest fully-contained candidate within 4px slack, never
 * nearest-centre, never first-found.
 */
class NodeSnapTest {

    private fun node(id: String, left: Int, top: Int, right: Int, bottom: Int): MeasureNode =
        MeasureNode(id = id, rect = DeviceRect(left, top, right, bottom), isTappable = false)

    @Test
    fun aNodeExactlyAtTheFourPxSlackBoundaryStillQualifies() {
        val annotation = DeviceRect(40, 200, 1040, 260)
        // 4px larger than the annotation on every edge -- the outer edge of the slack box.
        val candidate = node("row_item", 36, 196, 1044, 264)
        assertEquals(candidate, snapTarget(annotation, listOf(candidate)))
    }

    @Test
    fun aNodeOnePxBeyondTheSlackBoundaryIsRejected() {
        val annotation = DeviceRect(40, 200, 1040, 260)
        // left edge is 5px outside the annotation -- one px past the 4px slack.
        val tooFarLeft = node("row_item", 35, 200, 1040, 260)
        assertNull(snapTarget(annotation, listOf(tooFarLeft)))
    }

    @Test
    fun theLargestAreaCandidateWinsOverAnEarlierSmallerOne() {
        val annotation = DeviceRect(40, 200, 1040, 260)
        val label = node("item_title", 60, 210, 300, 250) // area 9_600, listed first
        val row = node("row_item", 40, 200, 1040, 260) // area 60_000, listed second
        assertEquals(row, snapTarget(annotation, listOf(label, row)))
    }

    @Test
    fun theLargestAreaCandidateWinsOverANearerToCentreCandidate() {
        val annotation = DeviceRect(0, 0, 200, 200)
        // Centred exactly on the annotation's centre, but tiny.
        val centred = node("centred_small", 90, 90, 110, 110) // area 400
        // Off-centre, but far bigger, and still fully contained with slack.
        val offCentreLarge = node("off_centre_large", 0, 0, 150, 150) // area 22_500
        assertEquals(
            offCentreLarge,
            snapTarget(annotation, listOf(centred, offCentreLarge)),
        )
    }

    @Test
    fun equalAreaCandidatesBreakTiesByInputOrderNotById() {
        val annotation = DeviceRect(0, 0, 200, 200)
        val p = node("p", 0, 0, 100, 100) // area 10_000
        val q = node("q", 100, 100, 200, 200) // area 10_000
        assertEquals(p, snapTarget(annotation, listOf(p, q)))
        assertEquals(q, snapTarget(annotation, listOf(q, p)))
    }

    @Test
    fun noQualifyingCandidateReturnsNull() {
        val annotation = DeviceRect(0, 0, 200, 200)
        val farAway = node("far_away", 300, 300, 400, 400)
        assertNull(snapTarget(annotation, listOf(farAway)))
        assertNull(snapTarget(annotation, emptyList()))
    }

    @Test
    fun aDegenerateZeroAreaAnnotationMatchesNothingRealSized() {
        // 8x8 slack box around a single point -- nothing with real size can fit inside it.
        val annotation = DeviceRect(50, 50, 50, 50)
        val row = node("row_item", 40, 200, 1040, 260)
        assertNull(snapTarget(annotation, listOf(row)))
    }
}
