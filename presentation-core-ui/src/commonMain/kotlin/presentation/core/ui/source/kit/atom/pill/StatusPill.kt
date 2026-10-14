package presentation.core.ui.source.kit.atom.pill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Pill geometry. */
private val DotSize: Dp = 7.dp

/**
 * Tone of a [StatusPill] — drives the dot only.
 */
public enum class StatusPillTone {
    /** Finished successfully. */
    Done,

    /** Still working. */
    Busy,

    /** Nothing to report either way. */
    Neutral,

    /** Needs attention. */
    Alert,
}

/**
 * A compact status pill — `● Fixed`, `● In progress`.
 *
 * The fill stays neutral across every tone and only the dot carries the colour: a row can hold
 * several of these, and tinting the whole pill would turn a list into a traffic light.
 *
 * @param label Status copy.
 * @param tone Tone driving the dot colour.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun StatusPill(
    label: String,
    tone: StatusPillTone,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(Theme.cornerToken.chip)
            .background(Theme.color.seg)
            .padding(horizontal = Theme.spacing.spacingS, vertical = Theme.spacing.spacingXS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS + 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(DotSize)
                .clip(CircleShape)
                .background(tone.dotColor()),
        )
        Text(
            text = label,
            style = Theme.typography.secondary,
            color = Theme.color.ink2,
            maxLines = 1,
        )
    }
}

@Composable
private fun StatusPillTone.dotColor(): Color = when (this) {
    StatusPillTone.Done -> Theme.color.sentFixed
    StatusPillTone.Busy -> Theme.color.warnStillOff
    StatusPillTone.Neutral -> Theme.color.ink3
    StatusPillTone.Alert -> Theme.color.annotate
}

@Preview(showBackground = true)
@Composable
private fun StatusPillPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            StatusPill(label = "Fixed", tone = StatusPillTone.Done)
            StatusPill(label = "In progress", tone = StatusPillTone.Busy)
            StatusPill(label = "Verified", tone = StatusPillTone.Neutral)
            StatusPill(label = "Follow-up sent", tone = StatusPillTone.Alert)
        }
    }
}
