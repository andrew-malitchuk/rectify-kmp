package presentation.feature.mirror.core.measure

import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * The nearest node on each side of [target] under the band-overlap + closest-edge rule
 * (FR-37 / AC-58).
 *
 * Two filters, in this order:
 *
 * 1. **Overlap exclusion.** Anything intersecting the target's own rect is not a neighbour on
 *    any side — a gap to something sitting on top of the target is not a gap. The test is
 *    **strict** on all four edges, so a node merely *touching* an edge is kept (and is then the
 *    nearest neighbour on that side, with a zero gap).
 * 2. **Band overlap.** A left/right neighbour must share some of the target's vertical band; a
 *    top/bottom neighbour must share some of its horizontal band. This is what excludes a
 *    diagonal node on **both** axes instead of letting it answer for a side it is not actually
 *    beside.
 *
 * Then the closest facing edge wins per side: right-most on the left, left-most on the right,
 * lowest above, highest below.
 *
 * @param target Snapped (or fallback) node being measured.
 * @param candidates Projected nodes, which may include [target] itself — it is dropped here.
 */
internal fun neighboursOf(target: MeasureNode, candidates: List<MeasureNode>): MeasureNeighbours {
    val t = target.rect
    val others = candidates.filter { it != target && !it.rect.overlaps(t) }
    val verticalBand = others.filter { it.rect.top < t.bottom && it.rect.bottom > t.top }
    val horizontalBand = others.filter { it.rect.left < t.right && it.rect.right > t.left }
    return MeasureNeighbours(
        left = verticalBand.filter { it.rect.right <= t.left }.maxByOrNull { it.rect.right },
        right = verticalBand.filter { it.rect.left >= t.right }.minByOrNull { it.rect.left },
        top = horizontalBand.filter { it.rect.bottom <= t.top }.maxByOrNull { it.rect.bottom },
        bottom = horizontalBand.filter { it.rect.top >= t.bottom }.minByOrNull { it.rect.top },
    )
}

private fun DeviceRect.overlaps(other: DeviceRect): Boolean =
    left < other.right && right > other.left && top < other.bottom && bottom > other.top
