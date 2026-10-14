package presentation.core.ui.source.kit.atom.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * A compact status indicator composed of a filled dot and an uppercase mono label.
 *
 * Used for the status line at the bottom of the stage, where the mirroring pipeline reports
 * what it is doing right now. The [label] defaults to the state's own name but is usually
 * overridden to carry the machine detail that goes with it — `LIVE · 58 FPS`,
 * `FROZEN · FRAME 18422`.
 *
 * For a step *inside* a pipeline sequence, use
 * [StatusLine][presentation.core.ui.source.kit.molecule.status.StatusLine] instead; this one
 * reports a single current state.
 *
 * @param state Visual state that controls the dot fill color.
 * @param label Uppercase status copy shown to the right of the dot.
 * @param modifier Modifier applied to the root [Row].
 *
 * @see StatusIndicatorState
 */
@Composable
public fun StatusIndicator(
    state: StatusIndicatorState,
    label: String = state.defaultLabel(),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS + 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(state.dotColor()),
        )
        Text(
            text = label,
            style = Theme.typography.overline,
            color = Theme.color.ink2,
        )
    }
}

/**
 * State of the mirroring pipeline, as reported on the status line.
 */
public enum class StatusIndicatorState {
    /** The stream is running. */
    Live,

    /** The stream is paused on a frame so it can be annotated. */
    Frozen,

    /** The view hierarchy is being dumped off the device. */
    Dumping,

    /** Annotations have been handed off. */
    Sent,

    /** The stream died unexpectedly. */
    Dropped,

    /** No device attached. */
    Offline,
}

/** Default display label derived from the state name. */
private fun StatusIndicatorState.defaultLabel(): String = when (this) {
    StatusIndicatorState.Live -> "LIVE"
    StatusIndicatorState.Frozen -> "FROZEN"
    // Ellipsis, not three dots — the style guide reserves … for work in progress.
    StatusIndicatorState.Dumping -> "DUMPING…"
    StatusIndicatorState.Sent -> "SENT"
    StatusIndicatorState.Dropped -> "DROPPED"
    StatusIndicatorState.Offline -> "OFFLINE"
}

/**
 * Resolves the dot fill.
 *
 * [StatusIndicatorState.Live] is red for the same reason a record light is: it marks the one
 * state where what is on screen is being captured, not because anything is wrong.
 */
@Composable
private fun StatusIndicatorState.dotColor(): Color = when (this) {
    StatusIndicatorState.Live -> Theme.color.annotate
    StatusIndicatorState.Frozen -> Theme.color.ink
    StatusIndicatorState.Dumping -> Theme.color.warnStillOff
    StatusIndicatorState.Sent -> Theme.color.sentFixed
    StatusIndicatorState.Dropped -> Theme.color.annotate
    StatusIndicatorState.Offline -> Theme.color.ink3
}

@Preview(showBackground = true)
@Composable
private fun StatusIndicatorPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            StatusIndicator(state = StatusIndicatorState.Live, label = "LIVE · 58 FPS")
            StatusIndicator(state = StatusIndicatorState.Frozen, label = "FROZEN · FRAME 18422")
            StatusIndicator(state = StatusIndicatorState.Dumping)
            StatusIndicator(state = StatusIndicatorState.Sent)
            StatusIndicator(state = StatusIndicatorState.Dropped)
            StatusIndicator(state = StatusIndicatorState.Offline)
        }
    }
}
