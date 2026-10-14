package presentation.core.ui.source.kit.atom.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * Visual height of the chip — the kit's compact control scale, matching the toolbar button.
 */
private val ChipHeight: Dp = 28.dp

/**
 * Padding added *inside* the clickable bounds to lift the hit area to 44 dp without inflating
 * the chip itself. The kit draws small controls because it is a desktop tool, but this is a
 * KMP app that also ships to phones, where a 28 dp target is below the accessible minimum.
 */
private val TouchPadding: Dp = 8.dp

/**
 * A selectable filter chip — `All`, `Starred`, `Recent`.
 *
 * Selected chips take the primary fill; unselected ones stay a plain outline so a row of them
 * reads as one control rather than a row of buttons.
 *
 * @param label Chip text content.
 * @param selected Whether the chip is currently selected.
 * @param onClick Callback for chip tap.
 * @param modifier Modifier applied to the chip's hit area.
 */
@Composable
public fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = Theme.cornerToken.chip
    val containerColor = if (selected) Theme.color.primary else Color.Transparent
    val contentColor = if (selected) Theme.color.onPrimary else Theme.color.ink
    val borderColor = if (selected) Theme.color.primary else Theme.color.line2

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            // Inside the clickable, so it grows the touch target, not the pill.
            .padding(vertical = TouchPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(ChipHeight)
                .clip(shape)
                .background(containerColor)
                .border(width = Theme.stroke.thin, color = borderColor, shape = shape)
                .padding(horizontal = Theme.spacing.spacingL - Theme.spacing.spacingXS),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = Theme.typography.bodyStrong,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Chip(label = "All", selected = false, onClick = {})
            Chip(label = "Starred", selected = true, onClick = {})
            Chip(label = "Recent", selected = false, onClick = {})
        }
    }
}
