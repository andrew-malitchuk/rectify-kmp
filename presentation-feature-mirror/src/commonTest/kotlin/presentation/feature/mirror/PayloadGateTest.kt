package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import presentation.feature.mirror.core.payload.HierarchyOutlineDisplay
import presentation.feature.mirror.core.payload.hasPayload
import presentation.feature.mirror.core.payload.hierarchyOutlineDisplay
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.StagedAnnotation

/**
 * AC-77: the Payload tab shows only its empty-state copy until something is actually staged.
 *
 * The three states below are the three the tab can be opened in, and they are distinct for
 * different reasons — a live stream, a pinned frame that was just cleared, and a real payload.
 * The middle one is the case an `isEmpty()`-only check would get wrong.
 */
class PayloadGateTest {

    @Test
    fun liveStreamWithNoPinnedFrameHasNoPayload() {
        assertFalse(hasPayload(null))
    }

    @Test
    fun pinnedFrameWithNoStagedAnnotationsHasNoPayload() {
        // The moment after ClearStaged: still frozen, but a payload here would carry a prompt
        // asking to fix nothing and a "0 crops" footer.
        val frozen = FrozenFrame(deviceSize = IntSize(width = 1080, height = 2400))
        assertFalse(hasPayload(frozen))
    }

    @Test
    fun pinnedFrameWithOneStagedAnnotationHasPayload() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(width = 1080, height = 2400),
            annotations = listOf(
                StagedAnnotation(
                    index = 1,
                    rect = DeviceRect(left = 120, top = 420, right = 960, bottom = 674),
                ),
            ),
        )
        assertTrue(hasPayload(frozen))
    }
}

/**
 * AC-80: `Absent`/`Unavailable` renders **zero rows plus an explicit no-hierarchy line**.
 *
 * The reachable state this exists for is the one the AC-80 preview pair cannot reach, because it
 * pins an empty snapshot: `dumpHierarchyFor` publishes the full dump *before* it reduces the
 * verdict, so a frame-size mismatch (the measured 9.76 % uiautomator display-override gap) leaves
 * a several-hundred-node snapshot held while the status reads `Unavailable`. The rows are
 * projected from the snapshot regardless of status, so they exist; the readout says "no
 * hierarchy". Rendering both at once is the defect — a verdict contradicted by the tree printed
 * underneath it.
 *
 * Asserted as literal expectations per state, not against a re-derived boolean: the first
 * reviewed version of this block carried a test that re-wrote the predicate and proved nothing.
 */
class PayloadHierarchyDisplayTest {

    private val dumpedRowCount = 312

    @Test
    fun unavailableWithHeldSnapshotShowsNoHierarchyLineInsteadOfRows() {
        assertEquals(
            HierarchyOutlineDisplay.NoHierarchyLine,
            hierarchyOutlineDisplay(HierarchyStatus.Unavailable, rowCount = dumpedRowCount),
        )
    }

    @Test
    fun absentWithHeldSnapshotShowsNoHierarchyLineInsteadOfRows() {
        assertEquals(
            HierarchyOutlineDisplay.NoHierarchyLine,
            hierarchyOutlineDisplay(HierarchyStatus.Absent, rowCount = dumpedRowCount),
        )
    }

    @Test
    fun unavailableWithNoSnapshotShowsNoHierarchyLine() {
        assertEquals(
            HierarchyOutlineDisplay.NoHierarchyLine,
            hierarchyOutlineDisplay(HierarchyStatus.Unavailable, rowCount = 0),
        )
    }

    @Test
    fun readyDumpShowsRows() {
        assertEquals(
            HierarchyOutlineDisplay.Rows,
            hierarchyOutlineDisplay(HierarchyStatus.Ready, rowCount = dumpedRowCount),
        )
    }

    @Test
    fun readyButEmptyDumpShowsNeitherRowsNorLine() {
        // A usable snapshot that genuinely contains no nodes; the readout says "0 nodes".
        assertEquals(
            HierarchyOutlineDisplay.Nothing,
            hierarchyOutlineDisplay(HierarchyStatus.Ready, rowCount = 0),
        )
    }

    @Test
    fun dumpInFlightShowsNeitherRowsNorLine() {
        assertEquals(
            HierarchyOutlineDisplay.Nothing,
            hierarchyOutlineDisplay(HierarchyStatus.Dumping, rowCount = 0),
        )
    }
}
