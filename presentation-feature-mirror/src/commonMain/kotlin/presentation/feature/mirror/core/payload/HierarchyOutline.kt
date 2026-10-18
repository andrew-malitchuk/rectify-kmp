package presentation.feature.mirror.core.payload

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.HierarchySnapshotModel
import presentation.feature.mirror.core.measure.projectMeasureNodes
import presentation.feature.mirror.core.measure.snapTarget
import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * One node of the payload preview's hierarchy outline, ready for `HierarchyRow`.
 *
 * @property nodeId The node's display label, already resolved by the data layer.
 * @property depth Indent level, passed through from the snapshot unchanged.
 * @property last Whether the guide in this node's **own column** ends here — see `HierarchyRow`'s
 *   KDoc. The column has to keep running whenever anything below still draws in it: a child
 *   (which draws this column as one of its ancestor guides) or a following sibling (which draws
 *   its own branch there). Only a following node that is *shallower* truly ends the column, so
 *   `last` is `next == null || next.depth < depth` — not `<=`.
 * @property badge 1-based annotation index marking this node, or `null` when no annotation
 *   snapped to it.
 * @property matched Whether an annotation snapped here — `badge != null`, named for the
 *   renderer's benefit so a row can be styled without unwrapping the badge.
 */
internal data class PayloadNodeRow(
    val nodeId: String,
    val depth: Int,
    val last: Boolean,
    val badge: Int?,
    val matched: Boolean,
)

/**
 * The cached hierarchy as outline rows, with the annotation badges folded in.
 *
 * Takes **only already-captured data** — there is no use case, repository or dumper parameter,
 * so "the preview never triggers a dump" (TAC-5) is a property of the signature rather than a
 * rule someone has to remember. A missing or mismatched snapshot is simply fewer candidates.
 *
 * Costs one `projectMeasureNodes` + one `snapTarget` **per annotation**, never per node: the
 * snap results are folded into a node → lowest-annotation-index map first, and the rows are
 * then produced in a single pass over [HierarchySnapshotModel.nodes]. A ~400-node dump with a
 * handful of annotations therefore stays linear in the node count.
 *
 * Two annotations snapping to the same node yield **one** badge carrying the lower (first-filed)
 * index — the badge is "the first annotation that measured this node", not a list.
 *
 * @param snapshot Hierarchy captured for the pinned frame; [HierarchySnapshotModel.Empty] gives
 *   no rows at all.
 * @param frozenFrameSize `FrozenFrame.deviceSize` — the screenshot the annotations were drawn
 *   on. A disagreement discards every candidate (see [projectMeasureNodes]), which leaves every
 *   node present and unbadged rather than silently measuring the wrong coordinate space.
 * @param annotations Staged annotation rectangles in badge order, in device pixels.
 */
internal fun payloadHierarchyRows(
    snapshot: HierarchySnapshotModel,
    frozenFrameSize: IntSize,
    annotations: List<DeviceRect>,
): List<PayloadNodeRow> {
    val nodes = snapshot.nodes
    if (nodes.isEmpty()) return emptyList()

    val badgeByNodeId = mutableMapOf<String, Int>()
    if (annotations.isNotEmpty()) {
        val candidates = projectMeasureNodes(snapshot, frozenFrameSize)
        annotations.forEachIndexed { position, annotation ->
            // Ascending order, so getOrPut already keeps the lower index on a collision.
            snapTarget(annotation, candidates)?.let { badgeByNodeId.getOrPut(it.id) { position + 1 } }
        }
    }

    return nodes.mapIndexed { position, node ->
        val badge = badgeByNodeId[node.id]
        val next = nodes.getOrNull(position + 1)
        PayloadNodeRow(
            nodeId = node.id,
            depth = node.depth,
            last = next == null || next.depth < node.depth,
            badge = badge,
            matched = badge != null,
        )
    }
}
