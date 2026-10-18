package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.HierarchyNodeResource
import data.device.api.source.parse.parseUiAutomatorDump
import data.device.api.source.parse.parseWmDensity
import domain.core.source.model.device.HierarchyNodeModel
import domain.core.source.model.device.HierarchySnapshotModel

/**
 * Both halves of one hierarchy capture, as raw stdout.
 *
 * A named type rather than two parameters or a `Pair`: it is what makes
 * [HierarchySnapshotMapper] unable to build a snapshot from a dump without its density (the
 * reworded AC's actual invariant), and it keeps the two interchangeable [String]s from being
 * swappable at the call site.
 *
 * @property dump `uiautomator dump` stdout, trailer and all.
 * @property density `wm density` stdout, empty when the device would not answer.
 */
internal data class HierarchyDumpStdout(val dump: String, val density: String)

/**
 * One-direction mapper from a [HierarchyDumpStdout] pair to a [HierarchySnapshotModel].
 *
 * **Deliberately NOT a [data.repository.impl.core.mapper.base.ModelResourceMapper], despite
 * `.claude/rules/mappers.md` describing a bidirectional shape.** Two reasons, both structural
 * rather than stylistic:
 * - *One direction exists.* A [HierarchyNodeModel] drops `resource-id`, `text` and
 *   `content-desc` on the way in, so a `toResource` could only invent them. A mapper
 *   interface that forces an inverse where none exists buys a lie, not symmetry.
 * - *The input is a named type, not a resource.* [HierarchyDumpStdout] pairs the two stdouts,
 *   which is what makes "never a snapshot built from one half without the other" a property
 *   the compiler enforces rather than a convention a future caller can forget.
 *
 * Never throws. Both fallbacks are deliberate:
 * - **An absent or unparseable density** maps to `densityDpi = 0`, the sentinel for "unknown".
 *   Nodes still survive: the geometry was read fine, only the dp conversion is unavailable.
 * - **No nodes** keeps whatever density WAS obtained and reports `0` frame dimensions. The
 *   frame comes from the root node's bounds, so no nodes means no root means no frame — but
 *   the density came from a separate command and is still a real measurement. Measure mode's
 *   "View (no match)" fallback measures against the frame edges and still needs that density
 *   to label its numbers in dp; discarding it would downgrade the fallback to px in exactly
 *   the case it is supposed to keep working. [HierarchySnapshotModel.Empty] is therefore the
 *   sentinel for "nothing at all" — no nodes AND no density — which this returns naturally
 *   when both halves failed, not a special case on the node count.
 */
internal object HierarchySnapshotMapper {

    /** Maps the pair; see the class KDoc for the two fallbacks. */
    val toModel: Mapper<HierarchyDumpStdout, HierarchySnapshotModel> =
        Mapper { stdout ->
            val nodes = parseUiAutomatorDump(stdout.dump)
            // The root is the first node of a depth-first walk, and its bounds are the frame.
            // No nodes means no root and so no frame - but the density is a separate command's
            // answer and survives on its own, see the class KDoc.
            val root = nodes.firstOrNull()
            HierarchySnapshotModel(
                nodes = nodes.map(node::map),
                frameWidthPx = root?.let { it.right - it.left } ?: 0,
                frameHeightPx = root?.let { it.bottom - it.top } ?: 0,
                densityDpi = parseWmDensity(stdout.density) ?: 0,
            )
        }

    private val node: Mapper<HierarchyNodeResource, HierarchyNodeModel> =
        Mapper { resource ->
            HierarchyNodeModel(
                id = resource.nodeId,
                className = resource.className,
                left = resource.left,
                top = resource.top,
                right = resource.right,
                bottom = resource.bottom,
                isTappable = resource.clickable,
                depth = resource.depth,
            )
        }
}
