package presentation.feature.onboarding.source.onboarding

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
 * Root content composable for the Onboarding screen.
 *
 * Acts as a container that hosts [OnboardingSuccessContent]. Future iterations
 * may add shimmer/loading states via animated visibility transitions.
 *
 * @param state Current [OnboardingState] driving the UI.
 * @param onIntent Callback to dispatch [OnboardingIntent] actions to the ViewModel.
 *
 * @see OnboardingSuccessContent
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
internal fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    snackbarHostState: StackedSnakbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingSuccessContent(
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
private fun OnboardingContentPreview() {
    AppTheme {
        OnboardingContent(
            state = OnboardingState(),
            onIntent = {},
            snackbarHostState = rememberStackedSnackbarHostState(),
        )
    }
}
