package presentation.feature.about.source.about

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.core.modifier.PlaceholderProvider

/**
 * Loading placeholder for the About screen.
 *
 * Displayed while the screen data is being loaded.
 *
 * @see AboutContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun AboutPlaceholderContent() {
    PlaceholderProvider(isLoading = true) {
        // TODO: implement about screen placeholder
    }
}

@Preview
@Composable
private fun AboutPlaceholderContentPreview() {
    AppTheme {
        AboutPlaceholderContent()
    }
}
