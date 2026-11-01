package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.HierarchyNodeModel
import domain.core.source.model.device.HierarchySnapshotModel
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.projectMeasureNodes
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * HierarchySnapshotModel -> List<MeasureNode>, including the geometry-mismatch guard against
 * FrozenFrame.deviceSize (T-1's measured 984x2132-override-vs-1080x2400-physical gap).
 */
class MeasureProjectionTest {

    private fun node(
        id: String,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        tappable: Boolean = false,
        depth: Int = 0,
    ): HierarchyNodeModel = HierarchyNodeModel(
        id = id,
        className = "android.widget.TextView",
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        isTappable = tappable,
        depth = depth,
    )

    @Test
    fun emptySnapshotProjectsToAnEmptyList() {
        assertTrue(projectMeasureNodes(HierarchySnapshotModel.Empty, IntSize(1080, 2400)).isEmpty())
    }

    @Test
    fun aSnapshotWhoseFrameMatchesTheFrozenFrameProjectsEveryNonRootNode() {
        val root = node("root", 0, 0, 1080, 2400, depth = 0)
        val child = node("item_title", 100, 200, 300, 240, tappable = true, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, child),
            frameWidthPx = 1080,
            frameHeightPx = 2400,
            densityDpi = 420,
        )
        val result = projectMeasureNodes(snapshot, IntSize(1080, 2400))
        assertEquals(
            listOf(MeasureNode(id = "item_title", rect = DeviceRect(100, 200, 300, 240), isTappable = true)),
            result,
        )
    }

    @Test
    fun theFullFrameRootNodeIsNeverItselfACandidate() {
        // A root-only snapshot (no children at all) must project to nothing to snap to.
        val root = node("root", 0, 0, 1080, 2400, depth = 0)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root),
            frameWidthPx = 1080,
            frameHeightPx = 2400,
            densityDpi = 420,
        )
        assertTrue(projectMeasureNodes(snapshot, IntSize(1080, 2400)).isEmpty())
    }

    @Test
    fun aSnapshotWhoseRootDisagreesWithTheFrozenFrameIsDiscardedEntirely() {
        // T-1: dump root reports the override resolution (984x2132); the frozen frame here
        // carries the physical resolution (1080x2400) -- a 9.76% gap. Must not be measured
        // against mismatched coordinates.
        val root = node("root", 0, 0, 984, 2132, depth = 0)
        val child = node("item_title", 100, 200, 300, 240, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, child),
            frameWidthPx = 984,
            frameHeightPx = 2132,
            densityDpi = 420,
        )
        assertTrue(projectMeasureNodes(snapshot, IntSize(1080, 2400)).isEmpty())
    }
}
