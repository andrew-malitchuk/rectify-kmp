package presentation.core.ui.source.kit.atom.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/**
 * Displays an uppercase section label used to group related settings or content blocks.
 *
 * @param title Section title text (rendered in uppercase).
 * @param modifier Modifier to be applied to the [Text].
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        style = Theme.typography.overline,
        color = Theme.color.ink2,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Theme.spacing.spacingL,
                vertical = Theme.spacing.spacingS,
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    AppTheme {
        SectionHeader(title = "appearance")
    }
}
