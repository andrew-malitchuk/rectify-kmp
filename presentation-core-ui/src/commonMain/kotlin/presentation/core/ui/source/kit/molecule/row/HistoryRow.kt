package presentation.core.ui.source.kit.molecule.row

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.pill.StatusPill
import presentation.core.ui.source.kit.atom.pill.StatusPillTone

/** Row geometry. */
private val ThumbnailSize: Dp = 56.dp

/**
 * A past session in the history list — the frame it was taken from, what was noted on it, and
 * where it got to.
 *
 * @param title What the session was about.
 * @param meta Count and time, e.g. `3 notes · 14:02`; rendered mono as machine output.
 * @param statusLabel Status pill copy, e.g. `Fixed`.
 * @param statusTone Tone of the status pill.
 * @param onClick Called when the row is opened.
 * @param modifier Modifier applied to the root composable.
 * @param thumbnail Frame thumbnail; a neutral plate is drawn when no slot is supplied.
 * @param trailing Trailing slot, typically the one action this session still needs.
 */
@Composable
public fun HistoryRow(
    title: String,
    meta: String,
    statusLabel: String,
    statusTone: StatusPillTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: @Composable (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(Theme.spacing.spacingL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
    ) {
        Box(
            modifier = Modifier
                .size(ThumbnailSize)
                .clip(Theme.cornerToken.nested)
                .background(Theme.color.chrome),
            contentAlignment = Alignment.Center,
        ) {
            thumbnail?.invoke()
        }
        Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS)) {
            androidx.compose.material3.Text(
                text = title,
                style = Theme.typography.body,
                color = Theme.color.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            androidx.compose.material3.Text(
                text = meta,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
            )
            StatusPill(label = statusLabel, tone = statusTone)
        }
        Spacer(modifier = Modifier.weight(1f))
        trailing()
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryRowPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            HistoryRow(
                title = "Price sits 6px below the title",
                meta = "3 notes · 14:02",
                statusLabel = "Fixed",
                statusTone = StatusPillTone.Done,
                onClick = {},
            ) {
                AppButton(text = "Verify", onClick = {}, size = AppButtonSize.ListAction)
            }
            HistoryRow(
                title = "Checkout button off the margin",
                meta = "1 note · 13:47",
                statusLabel = "In progress",
                statusTone = StatusPillTone.Busy,
                onClick = {},
            )
        }
    }
}
