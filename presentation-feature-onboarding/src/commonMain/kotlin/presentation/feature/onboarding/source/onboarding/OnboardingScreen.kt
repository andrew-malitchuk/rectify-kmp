package presentation.feature.onboarding.source.onboarding

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import presentation.core.navigation.api.core.composition.LocalAppNavigator
import presentation.core.navigation.api.source.destination.AppNavigator
import presentation.core.navigation.api.source.destination.Destination
import presentation.core.ui.source.kit.atom.snackbar.StackedSnackbarDuration
import presentation.core.ui.source.kit.atom.snackbar.rememberStackedSnackbarHostState

@Composable
public fun OnboardingScreen(viewModel: OnboardingViewModel = koinViewModel()) {
    val appNavigator = LocalAppNavigator.current
    val state = viewModel.collectAsState()
    val snackbarHostState = rememberStackedSnackbarHostState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            OnboardingSideEffect.NavigateToHome -> appNavigator?.navigate(
                Destination.Home,
                AppNavigator.NavOptions.ClearTask,
            )
            is OnboardingSideEffect.ShowError -> snackbarHostState.showSnackbar(
                title = "Error",
                duration = StackedSnackbarDuration.Short,
            )
        }
    }

    OnboardingContent(
        state = state.value,
        onIntent = { intent -> viewModel.handleIntent(intent) },
        snackbarHostState = snackbarHostState,
    )
}
