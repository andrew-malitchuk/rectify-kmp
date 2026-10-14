package dev.rectify.io.source.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.rectify.io.core.demo.DemoHost
import domain.core.source.model.ThemeModel
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveThemeUseCase
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import presentation.core.localisation.source.provider.AppLocaleProvider
import presentation.core.navigation.impl.source.host.NavigationHost
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme

/**
 * Set to `true` to launch the Style Guide / UI Kit demo screens
 * instead of the normal app flow. Flip back to `false` for production.
 */
private const val DEMO_MODE = false

/**
 * Root application composable that wires locale, theme, and navigation.
 *
 * Observes the persisted language and theme preferences reactively,
 * then wraps the navigation graph in [AppLocaleProvider] and [AppTheme]
 * so that every screen receives the correct locale and colour scheme.
 *
 * @see NavigationHost
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun App() {
    val observeLanguage = koinInject<ObserveApplicationLanguageUseCase>()
    val languageCode by observeLanguage()
        .map { it.getOrNull() ?: "en" }
        // NOTE: "en" is used as both the initial and the fallback for a failed Result —
        // the preference is unset on first launch, so English is the safe default.
        .collectAsState(initial = "en")

    val observeTheme = koinInject<ObserveThemeUseCase>()
    val themeMode by observeTheme()
        .map { result ->
            when (result.getOrNull()) {
                ThemeModel.Light -> ThemeMode.Light
                ThemeModel.Dark -> ThemeMode.Dark
                ThemeModel.System -> ThemeMode.System
                // NOTE: null means the preference has not been written yet (first launch).
                // System is the safe default — follow the platform appearance instead of
                // flashing a hard-coded light theme on startup.
                null -> ThemeMode.System
            }
        }
        .collectAsState(initial = ThemeMode.System)

    AppLocaleProvider(languageCode = languageCode) {
        AppTheme(mode = themeMode) {
            if (DEMO_MODE) {
                DemoHost()
            } else {
                NavigationHost()
            }
        }
    }
}
