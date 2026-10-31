package presentation.core.ui.source.kit.molecule.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
 * A single language option row for language picker screens.
 *
 * Displays the language [label] and its ISO [code]. The selected row is highlighted with a
 * [Theme.color.primary]-coloured border and a brand-tinted chevron.
 *
 * @param label Human-readable language name (e.g. "English").
 * @param code ISO locale code shown as secondary text (e.g. "EN").
 * @param selected Whether this row is the currently active language.
 * @param onClick Invoked when the user taps this row.
 *
 * @see SelectionRow
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun LanguageOptionRow(
    label: String,
    code: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accentColor = if (selected) Theme.color.primary else Theme.color.line
    val labelColor = if (selected) Theme.color.ink else Theme.color.ink2
    val shape = RoundedCornerShape(Theme.spacing.spacingXS)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Theme.spacing.spacingL,
                vertical = Theme.spacing.spacingXS,
            )
            .clip(shape)
            .background(Theme.color.stage)
            .border(
                width = Theme.stroke.thin,
                color = accentColor,
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(Theme.spacing.spacingL),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = if (selected) Theme.typography.titleS else Theme.typography.body,
                    color = labelColor,
                )
                Spacer(modifier = Modifier.height(Theme.spacing.spacingXS))
                Text(
                    text = code.uppercase(),
                    style = Theme.typography.monoData,
                    color = Theme.color.ink2,
                )
            }
            Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
            Image(
                imageVector = ChevronRight,
                contentDescription = null,
                colorFilter = ColorFilter.tint(accentColor),
                modifier = Modifier.size(Theme.size.iconS),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageOptionRowPreview() {
    AppTheme {
        Column {
            LanguageOptionRow(label = "English", code = "EN", selected = true, onClick = {})
        }
    }
}
