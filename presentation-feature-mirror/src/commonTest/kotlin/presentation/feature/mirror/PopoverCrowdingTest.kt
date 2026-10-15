package presentation.feature.mirror

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_HEIGHT_DP
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_WIDTH_DP
import presentation.feature.mirror.core.annotation.anyOverlap
import presentation.feature.mirror.core.annotation.popoverLayout
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A 1080x2400 device painted 1:1 on a density-1 host, so dp and device px coincide. */
private val GEOMETRY = FrameGeometry(
    framePx = Size(1080f, 2400f),
    deviceSize = IntSize(1080, 2400),
    density = 1f,
)

private fun annotation(index: Int, top: Int, comment: String): StagedAnnotation = StagedAnnotation(
    index = index,
    rect = DeviceRect(left = 40, top = top, right = 440, bottom = top + 100),
    comment = comment,
)

/** A nominal popover anchored at [x], [y] — never a measured size. */
private fun popover(x: Float, y: Float): Rect = Rect(
    left = x,
    top = y,
    right = x + NOMINAL_POPOVER_WIDTH_DP,
    bottom = y + NOMINAL_POPOVER_HEIGHT_DP,
)

class PopoverCrowdingTest {

    @Test
    fun nonOverlappingRectsDoNotCrowd() {
        assertFalse(anyOverlap(listOf(popover(0f, 0f), popover(0f, 100f), popover(0f, 200f))))
    }

    @Test
    fun touchingEdgesDoNotCount() {
        val stacked = listOf(popover(0f, 0f), popover(0f, NOMINAL_POPOVER_HEIGHT_DP))

        assertFalse(anyOverlap(stacked))
    }

    @Test
    fun oneOverlappingPairCrowdsEverything() {
        val crowded = listOf(popover(0f, 0f), popover(0f, 500f), popover(4f, 12f))

        assertTrue(anyOverlap(crowded))
    }

    @Test
    fun emptyAndSingleListsNeverCrowd() {
        assertFalse(anyOverlap(emptyList()))
        assertFalse(anyOverlap(listOf(popover(0f, 0f))))
    }
}

/**
 * The AF-1 regression: crowding chooses WHERE the read-only popovers go, never whether the
 * non-selected annotations have one at all.
 */
class PopoverLayoutTest {

    @Test
    fun uncrowdedNonSelectedAnnotationKeepsItsPopoverOnTheMark() {
        val frozen = FrozenFrame(
            deviceSize = GEOMETRY.deviceSize,
            annotations = listOf(
                annotation(index = 1, top = 200, comment = "Row title clips"),
                annotation(index = 2, top = 1400, comment = ""),
            ),
            selectedIndex = 2,
        )

        val layout = popoverLayout(geometry = GEOMETRY, frozen = frozen)

        assertEquals(listOf(1), layout.anchored.map { it.index })
        assertEquals(emptyList(), layout.gutter)
    }

    @Test
    fun crowdedAnnotationsRelocateTheWholeNonSelectedSetToTheGutter() {
        val frozen = FrozenFrame(
            deviceSize = GEOMETRY.deviceSize,
            annotations = listOf(
                annotation(index = 1, top = 200, comment = "Row title clips"),
                annotation(index = 2, top = 220, comment = "Price sits low"),
                annotation(index = 3, top = 240, comment = ""),
            ),
            selectedIndex = 3,
        )

        val layout = popoverLayout(geometry = GEOMETRY, frozen = frozen)

        assertEquals(listOf(1, 2), layout.gutter.map { it.index })
        assertEquals(emptyList(), layout.anchored)
    }

    @Test
    fun theSelectedAnnotationIsNeverGivenAReadOnlyCopy() {
        val frozen = FrozenFrame(
            deviceSize = GEOMETRY.deviceSize,
            annotations = listOf(annotation(index = 1, top = 200, comment = "Only one")),
            selectedIndex = 1,
        )

        val layout = popoverLayout(geometry = GEOMETRY, frozen = frozen)

        assertEquals(emptyList(), layout.anchored)
        assertEquals(emptyList(), layout.gutter)
    }

    @Test
    fun deselectingPutsEveryCrowdedAnnotationInTheGutter() {
        val frozen = FrozenFrame(
            deviceSize = GEOMETRY.deviceSize,
            annotations = listOf(
                annotation(index = 1, top = 200, comment = "a"),
                annotation(index = 2, top = 220, comment = "b"),
                annotation(index = 3, top = 240, comment = "c"),
            ),
            selectedIndex = null,
        )

        assertEquals(
            listOf(1, 2, 3),
            popoverLayout(geometry = GEOMETRY, frozen = frozen).gutter.map { it.index },
        )
    }
}
