package presentation.feature.home.source.home

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
 * Root content composable for the Home screen.
 *
 * Acts as a state-driven content switcher. Currently delegates directly to
 * [HomeSuccessContent]; a shimmer or error branch can be added here when
 * the Home screen acquires its own loading state.
 *
 * @param state Current immutable UI state snapshot from [HomeViewModel].
 * @param onIntent Callback that forwards user intents to the ViewModel.
 *
 * @see HomeSuccessContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    snackbarHostState: StackedSnakbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Directly show the success layout; shimmer/error states can be
        // branched here in the future based on state.isLoading.
        HomeSuccessContent(
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
private fun HomeContentPreview() {
    AppTheme {
        HomeContent(
            state = HomeState(),
            onIntent = {},
            snackbarHostState = rememberStackedSnackbarHostState(),
        )
    }
}
