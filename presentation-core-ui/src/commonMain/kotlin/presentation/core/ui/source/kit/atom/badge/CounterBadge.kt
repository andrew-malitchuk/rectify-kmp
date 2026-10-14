package presentation.core.ui.source.kit.atom.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/** Badge geometry — a circle at rest, widening into a stadium for 2+ digit counts. */
private val BadgeSize: Dp = 20.dp
private val BadgePadding: Dp = 5.dp

/**
 * Lifecycle state of a [CounterBadge], driving its fill.
 */
public enum class CounterBadgeState {
    /** Nothing counted yet — neutral, recedes. */
    Empty,

    /** Items are staged and awaiting an action. */
    Staged,

    /** Items have been sent / resolved. */
    Sent,
}

/**
 * A circular count badge with an optional trailing label — `0 staged`, `3 staged`, `3 sent`.
 *
 * @param count The number displayed inside the badge.
 * @param state Lifecycle state driving the fill color.
 * @param modifier Modifier applied to the root composable.
 * @param label Optional copy rendered after the badge; `null` renders the badge alone.
 */
@Composable
public fun CounterBadge(
    count: Int,
    state: CounterBadgeState,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Box(
            modifier = Modifier
                .height(BadgeSize)
                .defaultMinSize(minWidth = BadgeSize)
                .clip(CircleShape)
                .background(state.containerColor())
                .padding(horizontal = BadgePadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                style = Theme.typography.monoBadge,
                color = state.contentColor(),
                maxLines = 1,
            )
        }
        if (label != null) {
            Text(
                text = label,
                style = Theme.typography.secondary,
                color = Theme.color.ink2,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CounterBadgeState.containerColor(): Color = when (this) {
    CounterBadgeState.Empty -> Theme.color.seg
    CounterBadgeState.Staged -> Theme.color.annotate
    CounterBadgeState.Sent -> Theme.color.sentFixed
}

/**
 * Resolves the numeral color.
 *
 * [CounterBadgeState.Staged] and [CounterBadgeState.Sent] sit on a signal fill, where the
 * style guide requires white in **both** themes — so this is a deliberate literal rather than
 * `Theme.color.onPrimary`, which flips to near-black in dark mode and would turn the numeral
 * unreadable on red and green.
 */
@Composable
private fun CounterBadgeState.contentColor(): Color = when (this) {
    CounterBadgeState.Empty -> Theme.color.ink3
    CounterBadgeState.Staged, CounterBadgeState.Sent -> Color.White
}

@Preview(showBackground = true)
@Composable
private fun CounterBadgePreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged")
            CounterBadge(count = 3, state = CounterBadgeState.Staged, label = "staged")
            CounterBadge(count = 3, state = CounterBadgeState.Sent, label = "sent")
            CounterBadge(count = 12, state = CounterBadgeState.Staged)
        }
    }
}
