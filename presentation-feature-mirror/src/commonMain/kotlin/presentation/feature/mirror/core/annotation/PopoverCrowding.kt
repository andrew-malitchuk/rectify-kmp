package presentation.feature.mirror.core.annotation

import androidx.compose.ui.geometry.Rect

/**
 * Nominal popover width in dp — `AnnotationPopover`'s own `PopoverMaxWidth`.
 *
 * Nominal, not measured, on purpose: measuring a popover in order to decide whether to compose
 * it is a circular layout dependency that needs `SubcomposeLayout`, and the only decision this
 * drives is "stack them in the gutter or leave them in place" (FR-9). A rectangle that is a few
 * dp too wide changes that answer in almost no real arrangement, and when it does, stacking is
 * the safe way to be wrong.
 */
internal const val NOMINAL_POPOVER_WIDTH_DP: Float = 280f

/** Nominal popover height in dp — one line of text: a 20 dp index badge plus 8 dp above and below. */
internal const val NOMINAL_POPOVER_HEIGHT_DP: Float = 36f

/**
 * Whether any two popover rectangles overlap, i.e. whether the frame is too crowded to leave
 * the comments sitting on their marks (FR-9 / AC-9).
 *
 * O(n^2) pairwise, which is free at the handful of annotations a single frozen frame carries.
 * Edges that merely touch do not count — `Rect.overlaps` is already half-open, and two popovers
 * flush against each other are legible.
 *
 * @param rects Nominal popover bounds, in one shared coordinate space.
 * @return True when at least one pair overlaps.
 */
internal fun anyOverlap(rects: List<Rect>): Boolean =
    rects.indices.any { i ->
        (i + 1..rects.lastIndex).any { j -> rects[i].overlaps(rects[j]) }
    }
