package presentation.feature.mirror.core.measure

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.HierarchySnapshotModel
import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * A captured hierarchy as measure candidates, or **nothing at all**.
 *
 * Two rules, both found by tracing real dumps rather than by reading the design:
 *
 * 1. **The snapshot must agree with the frozen frame.** T-1 measured a Pixel 8 whose dump root
 *    reports the display-override resolution (984×2132) while the panel is 1080×2400 — a 9.76%
 *    gap. Measuring one coordinate space against a screenshot taken in the other produces
 *    numbers that look plausible and are all wrong, so a mismatched snapshot is discarded
 *    whole. An empty list makes [measure] fall back to `View (no match)`, which is honest.
 * 2. **Full-frame nodes are never candidates.** The root fully contains every other node and
 *    therefore always has the largest area, so [snapTarget] would return it for every
 *    annotation ever drawn and the feature could not work at all. The test is "covers the whole
 *    frame" rather than "depth == 0" and is **deliberately wider than the step-2 gate record's
 *    wording**, which named only the root: a real decor chain stacks several full-frame
 *    ancestors (`DecorView` → `LinearLayout` → `FrameLayout` → content), each of which would
 *    beat every real node on area, so excluding only `depth == 0` leaves the bug in place one
 *    level down. Geometry, not depth, is the property that actually makes a node unsnappable.
 *    A reviewer comparing this against the narrower gate phrasing should resolve it here.
 *
 * @param snapshot Latest hierarchy capture.
 * @param frozenFrameSize `FrozenFrame.deviceSize` — the screenshot the annotations were drawn on.
 */
internal fun projectMeasureNodes(
    snapshot: HierarchySnapshotModel,
    frozenFrameSize: IntSize,
): List<MeasureNode> {
    if (snapshot.nodes.isEmpty()) return emptyList()
    if (snapshot.frameWidthPx != frozenFrameSize.width ||
        snapshot.frameHeightPx != frozenFrameSize.height
    ) {
        return emptyList()
    }
    return snapshot.nodes
        .map {
            MeasureNode(
                id = it.id,
                rect = DeviceRect(it.left, it.top, it.right, it.bottom),
                isTappable = it.isTappable,
            )
        }
        .filterNot { it.rect.coversWholeFrame(frozenFrameSize) }
}

private fun DeviceRect.coversWholeFrame(frame: IntSize): Boolean =
    left == 0 && top == 0 && right == frame.width && bottom == frame.height
