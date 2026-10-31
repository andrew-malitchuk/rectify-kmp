package presentation.core.ui.source.kit.atom.handle

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * Handle geometry. The hit area is deliberately wider than the hairline it draws — a 1 dp
 * target is unhittable with a mouse, so the hairline is centred inside an 8 dp grab strip.
 */
private val HandleHitWidth: Dp = 8.dp

/** Preview-only height; the real handle fills the shell row. */
private val PreviewHandleHeight: Dp = 96.dp

/**
 * The draggable divider between the stage and the side panel.
 *
 * Reports raw horizontal drag deltas and nothing else: it does **not** own, clamp or persist
 * the panel width. Clamping lives in `clampPanelWidth`, the width itself in `ShellState`, and
 * persistence in the window layer — so each stays testable without a gesture harness.
 *
 * Sign convention is the caller's problem: [onDelta] is positive when the pointer moves right.
 * A handle sitting on the panel's leading edge therefore widens the panel with `-delta`.
 *
 * Commit on drag **end**, not per frame: `ShellState.onDragStopped()` returns the width to
 * persist (or `null` when the drag changed nothing), which is the only value worth writing.
 *
 * @param onDelta Called per drag frame with the horizontal movement since the last frame.
 * @param onDragStarted Called once when a drag begins.
 * @param onDragStopped Called once when the drag ends — the caller's commit point.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun PanelResizeHandle(
    onDelta: (Dp) -> Unit,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The draggable state is remembered once, so read the callbacks and the density through
    // rememberUpdatedState rather than capturing the first composition's instances.
    val currentOnDelta by rememberUpdatedState(onDelta)
    val currentDensity by rememberUpdatedState(LocalDensity.current)
    val dragState = rememberDraggableState { deltaPx ->
        currentOnDelta(with(currentDensity) { deltaPx.toDp() })
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(HandleHitWidth)
            // The hit strip sits between the stage and the panel. Without a fill it exposes the
            // shell's `bg`, which is neither of its neighbours — the seam then reads as three
            // different greys. Painting it `stage` makes the gutter continuous with the mirror.
            .background(Theme.color.stage)
            // Compose's common API has no E-resize pointer icon (it is an AWT cursor);
            // Hand is the closest "this is grabbable" affordance available here.
            .pointerHoverIcon(PointerIcon.Hand)
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStarted = { onDragStarted() },
                onDragStopped = { onDragStopped() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(Theme.stroke.thin)
                .background(Theme.color.line2),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PanelResizeHandlePreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .height(PreviewHandleHeight)
                .padding(Theme.spacing.spacingL),
        ) {
            PanelResizeHandle(onDelta = {}, onDragStarted = {}, onDragStopped = {})
        }
    }
}
