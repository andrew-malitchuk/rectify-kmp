package presentation.feature.mirror.core.annotation

import androidx.compose.ui.geometry.Rect
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.StagedAnnotation

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

/**
 * Nominal popover height in dp — **one** line of text.
 *
 * Checked against the composition it describes rather than left as arithmetic: `AnnotationPopover`
 * is a row of a 20 dp index badge and 19 dp body text with `spacingS` (8 dp) above and below, so
 * `max(20, 19) + 16 = 36`. A two-line comment measures 54 and the gutter pitches its rows at that
 * figure instead — see `GutterRowHeight` in `MirrorAnnotationStage.kt`.
 */
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

/**
 * Gap in dp between a mark's bottom edge and the popover's tail.
 *
 * Shared with `MirrorAnnotationStage`'s `PopoverTailGap` so the rectangles the crowding test
 * reasons about are the rectangles that actually get placed.
 */
internal const val POPOVER_TAIL_GAP_DP: Float = 6f

/**
 * Where every comment popover *would* sit if it stayed on its mark — the input to the crowding
 * test (FR-9 / AC-9).
 *
 * Plain dp floats in the overlay's own coordinate space, matching what the stage does with the
 * popovers it actually places. Nominal sizes, never measured: see [NOMINAL_POPOVER_WIDTH_DP].
 */
internal fun nominalPopoverRects(geometry: FrameGeometry, frozen: FrozenFrame): List<Rect> =
    frozen.annotations.map { annotation ->
        val mark = geometry.toDp(geometry.toFrame(annotation.rect))
        val top = mark.bottom.value + POPOVER_TAIL_GAP_DP
        Rect(
            left = mark.left.value,
            top = top,
            right = mark.left.value + NOMINAL_POPOVER_WIDTH_DP,
            bottom = top + NOMINAL_POPOVER_HEIGHT_DP,
        )
    }

/**
 * Which non-selected annotations show their read-only popover on their mark and which are
 * relocated into the right-edge gutter.
 *
 * Crowding chooses the **layout**, never whether a comment is shown at all: FR-8 asks for a
 * per-annotation popover unconditionally, and an earlier reading that composed the gutter only
 * when crowded made an uncrowded annotation's committed comment vanish from the stage the
 * moment another one was selected — crowding made *more* content appear.
 *
 * The selected annotation is in neither list: it keeps its editable `CommentPopover` on the
 * mark in both layouts, and a read-only copy would be the same text twice.
 *
 * @param geometry The one coordinate converter for this stage.
 * @param frozen Annotations and selection for the pinned frame.
 * @return Exactly one of [PopoverLayout.anchored] / [PopoverLayout.gutter] populated.
 */
internal fun popoverLayout(geometry: FrameGeometry, frozen: FrozenFrame): PopoverLayout {
    val unselected = frozen.annotations.filter { it.index != frozen.selectedIndex }
    return if (anyOverlap(nominalPopoverRects(geometry = geometry, frozen = frozen))) {
        PopoverLayout(anchored = emptyList(), gutter = unselected)
    } else {
        PopoverLayout(anchored = unselected, gutter = emptyList())
    }
}

/**
 * The read-only popovers of a frozen frame, split by where they are placed.
 *
 * @property anchored Shown on their own mark — the uncrowded case.
 * @property gutter Relocated to the right-edge gutter on leader lines — the crowded case.
 */
internal data class PopoverLayout(
    val anchored: List<StagedAnnotation>,
    val gutter: List<StagedAnnotation>,
)
