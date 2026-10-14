package presentation.core.ui.source.kit.molecule.row

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.GlyphBadge

/**
 * Outcome of a re-check after the user said they fixed something.
 */
public enum class VerificationOutcome {
    /** The measurement now matches what was asked for. */
    Fixed,

    /** The change landed but the problem is still there. */
    StillOff,
}

/**
 * A verification result row — what was checked, what the numbers did, and whether that settled
 * it.
 *
 * [delta] is the whole argument of the row and is always mono: it shows the measurement moving,
 * `top Δ6dp → 0dp`, so the user can see *why* it passed or failed rather than trusting a word.
 *
 * @param title What was checked.
 * @param delta The measurement before and after, e.g. `top Δ6dp → 0dp`.
 * @param outcome Whether the fix held.
 * @param modifier Modifier applied to the root composable.
 * @param outcomeLabel Override for the outcome word; defaults to the outcome's own name.
 */
@Composable
public fun VerificationRow(
    title: String,
    delta: String,
    outcome: VerificationOutcome,
    modifier: Modifier = Modifier,
    outcomeLabel: String = outcome.defaultLabel(),
) {
    val accent = outcome.accentColor()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Theme.spacing.spacingS + Theme.spacing.spacingXS),
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
    ) {
        GlyphBadge(
            text = if (outcome == VerificationOutcome.Fixed) "✓" else "!",
            containerColor = accent,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS)) {
            Text(
                text = title,
                style = Theme.typography.body,
                color = Theme.color.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = delta,
                style = Theme.typography.monoData,
                color = Theme.color.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = outcomeLabel,
                style = Theme.typography.body,
                color = accent,
                maxLines = 1,
            )
        }
    }
}

private fun VerificationOutcome.defaultLabel(): String = when (this) {
    VerificationOutcome.Fixed -> "Fixed"
    VerificationOutcome.StillOff -> "Still off"
}

@Composable
private fun VerificationOutcome.accentColor(): Color = when (this) {
    VerificationOutcome.Fixed -> Theme.color.sentFixed
    VerificationOutcome.StillOff -> Theme.color.warnStillOff
}

@Preview(showBackground = true)
@Composable
private fun VerificationRowPreview() {
    AppTheme {
        Column(
            modifier = Modifier.background(Theme.color.surface),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            VerificationRow(
                title = "Price sits 6px below the title",
                delta = "top Δ6dp → 0dp",
                outcome = VerificationOutcome.Fixed,
            )
            VerificationRow(
                title = "Stepper hit target",
                delta = "height 24dp → 32dp · want 48dp",
                outcome = VerificationOutcome.StillOff,
            )
        }
    }
}
