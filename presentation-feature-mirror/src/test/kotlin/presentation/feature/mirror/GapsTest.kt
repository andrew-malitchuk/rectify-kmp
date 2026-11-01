package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.measure.MeasureGaps
import presentation.feature.mirror.core.measure.MeasureNeighbours
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.gapsOf
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * FR-38 / AC-59 -- gapsOf: measured to the neighbour's facing edge, or to the frame edge when a
 * side has no neighbour. Never omitted.
 */
class GapsTest {

    private fun node(id: String, left: Int, top: Int, right: Int, bottom: Int): MeasureNode =
        MeasureNode(id = id, rect = DeviceRect(left, top, right, bottom), isTappable = false)

    private val frame = IntSize(1080, 2400)
    private val target = node("target", 100, 200, 300, 260)

    @Test
    fun eachSideMeasuresToItsNeighboursFacingEdgeWhenOnePresent() {
        val neighbours = MeasureNeighbours(
            left = node("left", 0, 200, 60, 260), // right edge 60
            right = node("right", 340, 200, 400, 260), // left edge 340
            top = node("top", 100, 150, 300, 180), // bottom edge 180
            bottom = node("bottom", 100, 300, 300, 330), // top edge 300
        )
        val gaps = gapsOf(target, neighbours, frame)
        assertEquals(MeasureGaps(leftPx = 40, rightPx = 40, topPx = 20, bottomPx = 40), gaps)
    }

    @Test
    fun leftWithNoNeighbourMeasuresToTheLeftFrameEdgeZero() {
        val neighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)
        val gaps = gapsOf(target, neighbours, frame)
        assertEquals(target.rect.left, gaps.leftPx)
    }

    @Test
    fun topWithNoNeighbourMeasuresToTheTopFrameEdgeZero() {
        val neighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)
        val gaps = gapsOf(target, neighbours, frame)
        assertEquals(target.rect.top, gaps.topPx)
    }

    @Test
    fun rightWithNoNeighbourMeasuresToTheFrameWidth() {
        val neighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)
        val gaps = gapsOf(target, neighbours, frame)
        assertEquals(frame.width - target.rect.right, gaps.rightPx)
    }

    @Test
    fun bottomWithNoNeighbourMeasuresToTheFrameHeight() {
        val neighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)
        val gaps = gapsOf(target, neighbours, frame)
        assertEquals(frame.height - target.rect.bottom, gaps.bottomPx)
    }

    @Test
    fun allFourSidesMissingAtOnceAreAllStillReportedAgainstTheFrame() {
        val neighbours = MeasureNeighbours(left = null, right = null, top = null, bottom = null)
        val gaps = gapsOf(target, neighbours, frame)
        assertEquals(
            MeasureGaps(
                leftPx = target.rect.left,
                rightPx = frame.width - target.rect.right,
                topPx = target.rect.top,
                bottomPx = frame.height - target.rect.bottom,
            ),
            gaps,
        )
    }
}
