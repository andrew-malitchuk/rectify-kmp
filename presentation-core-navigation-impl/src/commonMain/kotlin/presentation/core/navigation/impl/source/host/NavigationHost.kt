package presentation.core.navigation.impl.source.host

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import presentation.core.navigation.api.core.composition.LocalAppNavigator
import presentation.core.navigation.api.core.composition.LocalBackAction
import presentation.core.navigation.api.source.destination.Destination
import presentation.core.navigation.impl.source.navigator.AppNavigatorImpl
import presentation.core.navigation.impl.source.serialization.navSavedStateConfiguration
import presentation.core.styling.core.Theme
import presentation.core.ui.source.showcase.StyleguideScreen
import presentation.feature.about.source.about.AboutScreen
import presentation.feature.home.source.home.HomeScreen
import presentation.feature.onboarding.source.onboarding.OnboardingScreen
import presentation.feature.settings.source.language.LanguagePickerScreen
import presentation.feature.settings.source.settings.SettingsScreen
import presentation.feature.splash.source.splash.SplashScreen

/**
 * Root navigation host that maps [Destination] entries to feature screen composables.
 *
 * Sets up the Navigation 3 back stack, provides [AppNavigator] and back-action callback
 * via composition locals, and disables default transition animations.
 *
 * A canvas-coloured outer [Box] draws behind the system bars (status bar and navigation
 * bar) so there is no colour mismatch on edge-to-edge devices. An inner [Box] with
 * [statusBarsPadding] constrains all screen content below the status bar. Navigation bar
 * padding is intentionally omitted so screens extend edge-to-edge to the bottom.
 *
 * @param startDestination Initial destination, defaults to [Destination.Splash].
 *
 * @see Destination
 * @see AppNavigatorImpl
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun NavigationHost(startDestination: Destination? = null) {
    val backStack = rememberNavBackStack(
        navSavedStateConfiguration,
        startDestination ?: Destination.Splash
    )
    val appNavigator = remember(backStack) { AppNavigatorImpl(backStack) }

    CompositionLocalProvider(
        LocalAppNavigator provides appNavigator,
        LocalBackAction provides { appNavigator.popBackStack() },
    ) {
        // Outer box: fills the full window including behind system bars with the
        // canvas colour so there is no visible gap on edge-to-edge displays.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Theme.color.bg),
        ) {
            // Inner box: pushes content below the status bar only. Screens extend
            // edge-to-edge to the bottom — no navigation bar padding is applied.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                // Constrain content width on large screens (tablets, foldables, desktop)
                // so layouts don't stretch awkwardly beyond a comfortable reading width.
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxHeight(),
                ) {
                NavDisplay(
                    backStack = backStack,
                    transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    entryProvider = entryProvider {
                        entry<Destination.Splash> { Box(Modifier.fillMaxSize()) { SplashScreen() } }
                        entry<Destination.Onboarding> { Box(Modifier.fillMaxSize()) { OnboardingScreen() } }
                        entry<Destination.Home> { Box(Modifier.fillMaxSize()) { HomeScreen() } }
                        entry<Destination.Settings> { Box(Modifier.fillMaxSize()) { SettingsScreen() } }
                        entry<Destination.LanguagePicker> { Box(Modifier.fillMaxSize()) { LanguagePickerScreen() } }
                        entry<Destination.About> { Box(Modifier.fillMaxSize()) { AboutScreen() } }
                        entry<Destination.Styleguide> {
                            Box(Modifier.fillMaxSize()) {
                                StyleguideScreen(onBack = { appNavigator.popBackStack() })
                            }
                        }
                    },
                )
                } // end adaptive width Box
            }
        }
    }
}
