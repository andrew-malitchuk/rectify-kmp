package presentation.core.ui.source.kit.atom.text

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.Settings

/**
 * A section header row with a leading icon and a label.
 *
 * Used to head groups of related settings or content blocks. The icon is tinted with
 * [Theme.color.ink2] to match the label colour.
 *
 * @param icon Icon shown to the left of [title].
 * @param title Section title text.
 * @param modifier Modifier applied to the root [Row].
 *
 * @see SectionHeader
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun SectionHeaderWithIcon(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Theme.spacing.spacingL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Image(
            imageVector = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Theme.color.ink2),
            modifier = Modifier.size(Theme.size.iconS),
        )
        Text(
            text = title,
            style = Theme.typography.overline,
            color = Theme.color.ink2,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderWithIconPreview() {
    AppTheme {
        SectionHeaderWithIcon(
            icon = Settings,
            title = "PERMISSIONS",
        )
    }
}
