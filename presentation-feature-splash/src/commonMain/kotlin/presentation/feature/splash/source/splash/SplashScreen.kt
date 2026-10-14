package presentation.feature.splash.source.splash

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import presentation.core.navigation.api.core.composition.LocalAppNavigator
import presentation.core.navigation.api.source.destination.AppNavigator
import presentation.core.navigation.api.source.destination.Destination

@Composable
public fun SplashScreen(viewModel: SplashViewModel = koinViewModel()) {
    val appNavigator = LocalAppNavigator.current

    viewModel.collectSideEffect { effect ->
        when (effect) {
            SplashSideEffect.NavigateToOnboarding -> appNavigator?.navigate(
                Destination.Onboarding,
                AppNavigator.NavOptions.ClearTask,
            )
            SplashSideEffect.NavigateToHome -> appNavigator?.navigate(
                Destination.Home,
                AppNavigator.NavOptions.ClearTask,
            )
        }
    }

    SplashContent()
}
