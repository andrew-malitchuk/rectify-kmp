package presentation.core.ui.source.kit.molecule.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.ChevronRight

/**
 * A clickable row that displays the currently selected value with a trailing chevron.
 *
 * Tapping typically navigates to a dedicated picker screen. The [label] is rendered uppercase.
 *
 * @param label Text of the currently selected value (rendered uppercase).
 * @param onClick Invoked when the row is tapped.
 * @param modifier Modifier applied to the root [Row].
 *
 * @see LanguageOptionRow
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun SelectionRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Theme.spacing.spacingXS)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Theme.color.surface)
            .border(
                width = Theme.stroke.thin,
                color = Theme.color.line,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = Theme.spacing.spacingL,
                vertical = Theme.spacing.spacingS,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            style = Theme.typography.body,
            color = Theme.color.ink,
            modifier = Modifier.weight(1f),
        )
        Image(
            imageVector = ChevronRight,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Theme.color.ink2),
            modifier = Modifier.size(Theme.size.iconS),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionRowPreview() {
    AppTheme {
        SelectionRow(
            label = "English",
            onClick = {},
            modifier = Modifier.padding(Theme.spacing.spacingL),
        )
    }
}
