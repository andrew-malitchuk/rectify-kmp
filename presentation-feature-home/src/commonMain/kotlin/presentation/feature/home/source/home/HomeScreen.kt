package presentation.feature.home.source.home

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import presentation.core.navigation.api.core.composition.LocalAppNavigator
import presentation.core.navigation.api.source.destination.Destination
import presentation.core.ui.source.kit.atom.snackbar.StackedSnackbarDuration
import presentation.core.ui.source.kit.atom.snackbar.rememberStackedSnackbarHostState

@Composable
public fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val appNavigator = LocalAppNavigator.current
    val state = viewModel.collectAsState()
    val snackbarHostState = rememberStackedSnackbarHostState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            HomeSideEffect.NavigateToSettings -> appNavigator?.navigate(Destination.Settings)
            HomeSideEffect.NavigateToStyleguide -> appNavigator?.navigate(Destination.Styleguide)
            is HomeSideEffect.ShowError -> snackbarHostState.showSnackbar(
                title = "Error",
                duration = StackedSnackbarDuration.Short,
            )
        }
    }

    HomeContent(
        state = state.value,
        onIntent = viewModel::handleIntent,
        snackbarHostState = snackbarHostState,
    )
}
