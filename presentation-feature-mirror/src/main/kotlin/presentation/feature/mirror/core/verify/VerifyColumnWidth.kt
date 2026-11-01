package presentation.feature.mirror.core.verify

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How wide each BEFORE/AFTER column may be inside a stage of [stageWidth].
 *
 * The design's [nominal] width is a CEILING, not a fixed size. The stage the verify view draws
 * into is not a constant: the 300 dp verification panel sits beside it, the side panel is a
 * width the user dragged, and the window itself can be at its 1024 dp minimum. Two fixed-width
 * children in a `Row` do not shrink when the row is too narrow — they overflow and the second
 * one is clipped, which is exactly how the AFTER column went missing.
 *
 * Pure and in `core/` so the arithmetic is unit-testable: `MirrorVerifyStage` has no test
 * harness of its own, and this is the part of it that can be wrong.
 *
 * @param stageWidth Width available to the whole pair, chrome included.
 * @param horizontalPadding Padding applied on EACH side of the pair.
 * @param gap Space between the two columns.
 * @param nominal The design width a column gets whenever there is room for it.
 * @return A width in `[0.dp, nominal]`.
 */
internal fun verifyColumnWidth(
    stageWidth: Dp,
    horizontalPadding: Dp,
    gap: Dp,
    nominal: Dp,
): Dp = ((stageWidth - horizontalPadding * 2 - gap) / 2)
    .coerceAtMost(nominal)
    // A stage narrower than its own chrome yields a negative half-width. Clamping here keeps
    // that out of the layout pass rather than letting Compose throw on a negative constraint.
    .coerceAtLeast(0.dp)
