package presentation.feature.about.source.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.snackbar.StackedSnackbarHost
import presentation.core.ui.source.kit.atom.snackbar.StackedSnakbarHostState
import presentation.core.ui.source.kit.atom.snackbar.rememberStackedSnackbarHostState

/**
 * Content router for the About screen that delegates to the appropriate sub-content.
 *
 * @param state Current UI state driving the content.
 * @param onIntent Callback to dispatch user intents to the ViewModel.
 *
 * @see AboutSuccessContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun AboutContent(
    state: AboutState,
    onIntent: (AboutIntent) -> Unit,
    snackbarHostState: StackedSnakbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AboutSuccessContent(
            state = state,
            onIntent = onIntent,
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            StackedSnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Preview
@Composable
private fun AboutContentPreview() {
    AppTheme {
        AboutContent(
            state = AboutState(),
            onIntent = {},
            snackbarHostState = rememberStackedSnackbarHostState(),
        )
    }
}
