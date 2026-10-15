package presentation.feature.mirror

import presentation.feature.mirror.core.picker.firstSelectableIndex
import presentation.feature.mirror.core.picker.nextSelectableIndex
import presentation.feature.mirror.source.mirror.MirrorRow
import presentation.feature.mirror.source.mirror.MirrorRowGroup
import presentation.feature.mirror.source.mirror.MirrorRowKind
import presentation.feature.mirror.source.mirror.MirrorUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun row(id: String, selectable: Boolean): MirrorRow = MirrorRow(
    id = id,
    name = id,
    serial = null,
    group = MirrorRowGroup.Physical,
    kind = MirrorRowKind.Detail,
    selectable = selectable,
)

class PickerNavigationTest {

    private val rows = listOf(
        row("usable-a", selectable = true),
        row("unauthorized", selectable = false),
        row("usable-b", selectable = true),
        row("offline", selectable = false),
    )

    @Test
    fun movingDownSkipsAnUnselectableRow() {
        assertEquals(2, nextSelectableIndex(rows, from = 0, direction = 1))
    }

    @Test
    fun movingUpSkipsAnUnselectableRow() {
        assertEquals(0, nextSelectableIndex(rows, from = 2, direction = -1))
    }

    @Test
    fun movingDownPastTheLastSelectableRowStaysPut() {
        assertEquals(2, nextSelectableIndex(rows, from = 2, direction = 1))
    }

    @Test
    fun movingUpPastTheFirstSelectableRowStaysPut() {
        assertEquals(0, nextSelectableIndex(rows, from = 0, direction = -1))
    }

    @Test
    fun anEmptyListIsANoOp() {
        assertEquals(0, nextSelectableIndex(emptyList(), from = 0, direction = 1))
        assertEquals(3, nextSelectableIndex(emptyList(), from = 3, direction = -1))
    }

    @Test
    fun aListWithNothingSelectableNeverMoves() {
        val unusable = listOf(row("a", selectable = false), row("b", selectable = false))
        assertEquals(0, nextSelectableIndex(unusable, from = 0, direction = 1))
    }

    @Test
    fun theFirstSelectableRowIsFoundPastUnusableOnes() {
        val leadingJunk = listOf(row("a", selectable = false), row("b", selectable = true))
        assertEquals(1, firstSelectableIndex(leadingJunk))
    }

    @Test
    fun theFirstSelectableRowFallsBackToTheTopWhenNothingIsUsable() {
        assertEquals(0, firstSelectableIndex(listOf(row("a", selectable = false))))
        assertEquals(0, firstSelectableIndex(emptyList()))
    }
}

/** A11Y-4: whether the panel offers a way out at all. */
class PickerDismissabilityTest {

    @Test
    fun theFirstRunPickerOffersNoWayOut() {
        // Nothing behind it yet, so neither esc nor a close button may be wired up.
        assertFalse(MirrorUiState().isPickerDismissable)
    }

    @Test
    fun aPickerReopenedOverASessionCanBeDismissed() {
        assertTrue(MirrorUiState(selectedDeviceId = "37281FDH2000AB").isPickerDismissable)
    }
}
