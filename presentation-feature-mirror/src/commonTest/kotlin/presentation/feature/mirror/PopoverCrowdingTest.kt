package presentation.feature.mirror

import androidx.compose.ui.geometry.Rect
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_HEIGHT_DP
import presentation.feature.mirror.core.annotation.NOMINAL_POPOVER_WIDTH_DP
import presentation.feature.mirror.core.annotation.anyOverlap
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
