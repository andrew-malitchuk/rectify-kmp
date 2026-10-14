package presentation.core.navigation.api.source.destination

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Sealed hierarchy of all navigable screens in the application.
 *
 * Each subclass is [Serializable] to support Navigation 3 saved-state persistence.
 * Register new destinations in [navSavedStateConfiguration] and [NavigationHost].
 *
 * @see AppNavigator
 */
@Serializable
public sealed class Destination : NavKey {
    /** App launch splash screen with animation. */
    @Serializable
    public data object Splash : Destination()

    /** First-time onboarding flow. */
    @Serializable
    public data object Onboarding : Destination()

    /** Main home screen. */
    @Serializable
    public data object Home : Destination()

    /** Application settings (theme, language). */
    @Serializable
    public data object Settings : Destination()

    /** About screen with app information. */
    @Serializable
    public data object About : Destination()

    /** Design-system styleguide showcase (dev utility). */
    @Serializable
    public data object Styleguide : Destination()

    /** Language picker — full-screen list to change the application locale. */
    @Serializable
    public data object LanguagePicker : Destination()
}
