package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * MeasureReadout.rail populates ONLY for the top-misalignment finding -- it is not a general
 * "any finding present" flag, and the left-misalignment / touch-target / no-finding branches
 * must all leave it null.
 */
class MeasureRailTest {

    private fun node(
        id: String,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        tappable: Boolean = false,
    ): MeasureNode = MeasureNode(id = id, rect = DeviceRect(left, top, right, bottom), isTappable = tappable)

    private val frame = IntSize(400, 800)
    private val densityDpi = 160
    private val target = node("item_price", 162, 300, 210, 317)

    @Test
    fun theTopMisalignmentBranchPopulatesTheRail() {
        val leftNeighbour = node("item_title", 40, 294, 100, 310) // top delta 6
        val readout = measure(
            annotation = target.rect,
            index = 1,
            nodes = listOf(target, leftNeighbour),
            frameSize = frame,
            densityDpi = densityDpi,
        )
        assertNotNull(readout.rail)
    }

    @Test
    fun theLeftMisalignmentBranchLeavesTheRailNull() {
        val topNeighbour = node("item_icon", 153, 270, 190, 291) // left delta 9, no left neighbour
        val readout = measure(
            annotation = target.rect,
            index = 1,
            nodes = listOf(target, topNeighbour),
            frameSize = frame,
            densityDpi = densityDpi,
        )
        assertNull(readout.rail)
    }

    @Test
    fun theTouchTargetBranchLeavesTheRailNull() {
        val shortTappableTarget = node("item_button", 162, 300, 210, 320, tappable = true) // 20dp tall
        val readout = measure(
            annotation = shortTappableTarget.rect,
            index = 1,
            nodes = listOf(shortTappableTarget),
            frameSize = frame,
            densityDpi = densityDpi,
        )
        assertNull(readout.rail)
    }

    @Test
    fun theNoFindingCaseLeavesTheRailNull() {
        val wellBehaved = node("item_price", 162, 300, 210, 348, tappable = true) // 48dp tall
        val readout = measure(
            annotation = wellBehaved.rect,
            index = 1,
            nodes = listOf(wellBehaved),
            frameSize = frame,
            densityDpi = densityDpi,
        )
        assertNull(readout.rail)
    }
}
