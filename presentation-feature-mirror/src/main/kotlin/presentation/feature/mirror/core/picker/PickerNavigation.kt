package presentation.feature.mirror.core.picker

import presentation.feature.mirror.source.mirror.MirrorRow

/**
 * Index the cursor lands on after an arrow key.
 *
 * Non-selectable rows are skipped rather than stepped onto, and both ends clamp: running out
 * of list returns [from] unchanged, so a long tail of unauthorised devices can never strand
 * the cursor somewhere `↵` would do nothing.
 *
 * @param rows Rows in display order.
 * @param from Current cursor index.
 * @param direction `-1` for up, `+1` for down. Anything else is a no-op.
 * @return The new index, or [from] when there is nowhere to go.
 */
internal fun nextSelectableIndex(rows: List<MirrorRow>, from: Int, direction: Int): Int {
    if (rows.isEmpty() || direction == 0) return from
    var index = from + direction
    while (index in rows.indices) {
        if (rows[index].selectable) return index
        index += direction
    }
    return from
}

/**
 * Index of the first row the cursor may rest on.
 *
 * @return The first selectable index, or `0` when nothing is selectable — the cursor still has
 *   to point somewhere, and an empty-or-unusable list makes commit a no-op anyway.
 */
internal fun firstSelectableIndex(rows: List<MirrorRow>): Int =
    rows.indexOfFirst { it.selectable }.coerceAtLeast(0)
