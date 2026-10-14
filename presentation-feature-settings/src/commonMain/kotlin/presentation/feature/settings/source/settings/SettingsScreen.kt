package presentation.feature.settings.source.settings

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
public fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val appNavigator = LocalAppNavigator.current
    val state = viewModel.collectAsState()
    val snackbarHostState = rememberStackedSnackbarHostState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            SettingsSideEffect.GoBackEffect -> appNavigator?.backAction()
            SettingsSideEffect.NavigateToLanguagePicker -> appNavigator?.navigate(Destination.LanguagePicker)
            SettingsSideEffect.NavigateToAbout -> appNavigator?.navigate(Destination.About)
            SettingsSideEffect.NavigateToSplash -> appNavigator?.navigate(Destination.Splash, AppNavigator.NavOptions.ClearTask)
            is SettingsSideEffect.ShowError -> snackbarHostState.showSnackbar(
                title = "Error",
                duration = StackedSnackbarDuration.Short,
            )
        }
    }

    SettingsContent(
        state = state.value,
        onIntent = { intent -> viewModel.handleIntent(intent) },
        snackbarHostState = snackbarHostState,
    )
}
