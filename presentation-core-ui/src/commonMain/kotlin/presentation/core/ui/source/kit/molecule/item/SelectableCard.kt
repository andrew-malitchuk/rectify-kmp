package presentation.core.ui.source.kit.molecule.item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * A selectable card displaying a [title] and [subtitle].
 *
 * The selected state is indicated by a [Theme.color.primary]-coloured border.
 * Tapping invokes [onClick] to toggle the selection.
 *
 * @param title Primary label.
 * @param subtitle Secondary description line.
 * @param selected Whether this card is currently selected.
 * @param onClick Invoked when the card is tapped.
 * @param modifier Modifier applied to the root [Column].
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun SelectableCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Theme.color.primary else Theme.color.line
    val shape = RoundedCornerShape(Theme.spacing.spacingXS)

    Column(
        modifier = modifier
            .clip(shape)
            .background(Theme.color.surface)
            .border(
                width = Theme.stroke.thin,
                color = borderColor,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(Theme.spacing.spacingS),
    ) {
        Text(
            text = title,
            style = Theme.typography.titleS,
            color = Theme.color.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(Theme.spacing.spacingXS))
        Text(
            text = subtitle,
            style = Theme.typography.comment,
            color = Theme.color.ink2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectableCardPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            SelectableCard(
                title = "MONOLITH",
                subtitle = "Dark theme",
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            SelectableCard(
                title = "BRUTALIST",
                subtitle = "Light theme",
                selected = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
