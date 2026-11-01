package presentation.feature.mirror.core.verify

import presentation.feature.mirror.source.mirror.StagedAnnotation
import presentation.feature.mirror.source.mirror.VerifyRow

/**
 * Turns the rows a run reported as still off into a fresh staged batch (FR-47 / AC-42).
 *
 * **The one and only place the two numbering schemes meet.** [VerifyRow.index] is history's
 * 0-based position in the batch that was sent; [StagedAnnotation.index] is the 1-based badge
 * the frame, the sidebar and the envelope's `crop-N.png` file names all read. Re-staging 2 of 3
 * rows must therefore produce badges `1, 2` — NOT the rows' own `1, 2` (0-based holes) and not
 * `2, 3` (the original badges), either of which would hand the agent a crop file name that
 * points at a different annotation than the comment beside it.
 *
 * [VerifyRow.afterRect] and not `beforeRect`: the follow-up is drawn on the RE-CAPTURED frame,
 * so the rect has to be where the node sits NOW. The comment is carried over verbatim — v1
 * re-files the complaint the tester wrote, it does not reframe it.
 *
 * @param rows The still-off rows, in batch order.
 * @return One annotation per row, renumbered contiguously from 1.
 */
internal fun followUpAnnotations(rows: List<VerifyRow>): List<StagedAnnotation> =
    rows.mapIndexed { position, row ->
        StagedAnnotation(index = position + 1, rect = row.afterRect, comment = row.comment)
    }
