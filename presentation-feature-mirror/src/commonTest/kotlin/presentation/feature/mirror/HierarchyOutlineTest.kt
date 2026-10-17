package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.HierarchyNodeModel
import domain.core.source.model.device.HierarchySnapshotModel
import presentation.feature.mirror.core.payload.PayloadNodeRow
import presentation.feature.mirror.core.payload.payloadHierarchyRows
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * payloadHierarchyRows (T-5 / AC-15, AC-74, AC-75, AC-80): projects a cached
 * [HierarchySnapshotModel] into [PayloadNodeRow]s, running the existing `projectMeasureNodes` +
 * `snapTarget` once per annotation and folding the result into a node -> lowest-annotation-index
 * map before a single pass over `snapshot.nodes`.
 */
class HierarchyOutlineTest {

    private val frame = IntSize(1080, 2400)

    private fun node(
        id: String,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        depth: Int,
    ): HierarchyNodeModel = HierarchyNodeModel(
        id = id,
        className = "android.widget.TextView",
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        isTappable = false,
        depth = depth,
    )

    // TAC-1: the badge lands on the snapTarget node and nowhere else; a non-matching annotation
    // produces no badge at all (SC-21).
    @Test
    fun theBadgeLandsOnlyOnTheSnappedNodeAndNoneOfTheOthers() {
        val root = node("root", 0, 0, 1080, 2400, depth = 0)
        val itemA = node("item_a", 40, 200, 1040, 260, depth = 1)
        val itemB = node("item_b", 40, 300, 1040, 360, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, itemA, itemB),
            frameWidthPx = 1080,
            frameHeightPx = 2400,
            densityDpi = 420,
        )
        val annotation = DeviceRect(40, 200, 1040, 260) // sits exactly on item_a only.

        val rows = payloadHierarchyRows(snapshot, frame, listOf(annotation))

        assertEquals(3, rows.size)
        val byId = rows.associateBy { it.nodeId }
        // Both item_a and item_b are leaves -- each one's own column guide ends right there,
        // regardless of sibling order (see depthAndLastAreDerivedFromDocumentOrderUnaffectedByAnnotations
        // below for the case that actually distinguishes "leaf" from "last sibling"). Only root,
        // which has children, carries last = false.
        assertEquals(PayloadNodeRow(nodeId = "root", depth = 0, last = false, badge = null, matched = false), byId["root"])
        assertEquals(PayloadNodeRow(nodeId = "item_a", depth = 1, last = true, badge = 1, matched = true), byId["item_a"])
        assertEquals(PayloadNodeRow(nodeId = "item_b", depth = 1, last = true, badge = null, matched = false), byId["item_b"])
    }

    // SC-21: an annotation that snaps to nothing leaves every row unbadged.
    @Test
    fun anAnnotationWithNoMatchingNodeLeavesEveryRowUnbadged() {
        val root = node("root", 0, 0, 1080, 2400, depth = 0)
        val itemA = node("item_a", 40, 200, 1040, 260, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, itemA),
            frameWidthPx = 1080,
            frameHeightPx = 2400,
            densityDpi = 420,
        )
        val annotation = DeviceRect(40, 2000, 100, 2060) // far away from every real node.

        val rows = payloadHierarchyRows(snapshot, frame, listOf(annotation))

        assertTrue(rows.all { it.badge == null && !it.matched })
    }

    // TAC-2: two annotations snapping to the same node yield one badge, carrying the lower
    // (first-filed) annotation index -- never the higher one, regardless of area.
    @Test
    fun twoAnnotationsSnappingToTheSameNodeKeepTheLowerIndex() {
        val root = node("root", 0, 0, 1080, 2400, depth = 0)
        val item = node("item", 40, 200, 1040, 260, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, item),
            frameWidthPx = 1080,
            frameHeightPx = 2400,
            densityDpi = 420,
        )
        // Both drawn rectangles are fully contained by `item` (within slack) so both snap to it.
        val firstFiled = DeviceRect(40, 200, 1040, 260) // index 1
        val secondFiled = DeviceRect(50, 210, 1030, 250) // index 2, smaller, still inside `item`

        val rows = payloadHierarchyRows(snapshot, frame, listOf(firstFiled, secondFiled))

        val itemRow = rows.single { it.nodeId == "item" }
        assertEquals(1, itemRow.badge)
        assertTrue(itemRow.matched)
    }

    // TAC-3: depth and the per-row `last` flag are a deterministic function of the snapshot's
    // own document order and depth field -- never touched by the badge/annotation pass.
    @Test
    fun depthAndLastAreDerivedFromDocumentOrderUnaffectedByAnnotations() {
        // root(0) -> A(1, leaf) -> B(1, has child C) -> C(2, leaf) -> D(1, leaf, last overall).
        val root = node("root", 0, 0, 1080, 2400, depth = 0)
        val a = node("a", 0, 0, 100, 100, depth = 1)
        val b = node("b", 0, 100, 500, 900, depth = 1)
        val c = node("c", 10, 110, 490, 890, depth = 2)
        val d = node("d", 0, 900, 1080, 1000, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, a, b, c, d),
            frameWidthPx = 1080,
            frameHeightPx = 2400,
            densityDpi = 420,
        )

        val rows = payloadHierarchyRows(snapshot, frame, annotations = emptyList())

        assertEquals(listOf("root", "a", "b", "c", "d"), rows.map { it.nodeId })
        assertEquals(listOf(0, 1, 1, 2, 1), rows.map { it.depth })
        // root and b both have a deeper node directly following them (still inside their own
        // branch), so their own column's guide keeps going -- not last. a, c and d have nothing
        // deeper following them, so each one's own guide ends right there -- last, regardless of
        // whether a sibling still follows (c is immediately followed by sibling d, and is still
        // `last` because that sibling is one level shallower, not a child of c).
        assertEquals(listOf(false, true, false, true, true), rows.map { it.last })
    }

    // A frame-size mismatch makes `projectMeasureNodes` discard every candidate (see
    // MeasureProjectionTest), but every node must still get a row -- just none of them badged.
    @Test
    fun aMismatchedFrameSizeStillProducesARowPerNodeWithNoBadges() {
        val root = node("root", 0, 0, 984, 2132, depth = 0)
        val item = node("item", 100, 200, 300, 240, depth = 1)
        val snapshot = HierarchySnapshotModel(
            nodes = listOf(root, item),
            frameWidthPx = 984,
            frameHeightPx = 2132,
            densityDpi = 420,
        )
        val annotation = DeviceRect(100, 200, 300, 240)

        val rows = payloadHierarchyRows(snapshot, frame, listOf(annotation))

        assertEquals(2, rows.size)
        assertTrue(rows.all { it.badge == null })
    }

    // TAC-5, covered structurally rather than by an injected fake: `payloadHierarchyRows` takes
    // only already-cached data (`snapshot`, `frozenFrameSize`, `annotations`) -- there is no
    // DumpHierarchyUseCase / repository parameter anywhere in this signature for it to call, so
    // "never dumps" is a property of the call site compiling with nothing dump-capable in scope,
    // not something a runtime verify() on a mock could add anything to.
    @Test
    fun theFunctionTakesNothingDumpCapableOnlyAnAlreadyCachedSnapshot() {
        val rows = payloadHierarchyRows(HierarchySnapshotModel.Empty, frame, annotations = emptyList())
        assertTrue(rows.isEmpty())
        assertNull(rows.firstOrNull())
    }
}
