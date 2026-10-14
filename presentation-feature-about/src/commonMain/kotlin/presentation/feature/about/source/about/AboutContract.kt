package presentation.feature.about.source.about

/**
 * UI state for the About screen.
 *
 * @property isLoading Whether the screen is in a loading state.
 * @property appName Display name of the application.
 * @property appVersion Version string (e.g. "v0.0.1").
 * @property buildNumber Build number string.
 *
 * @see AboutViewModel
 */
public data class AboutState(
    val isLoading: Boolean = false,
    val appName: String = "RECTIFY",
    val appVersion: String = "v0.0.1",
    val buildNumber: String = "1",
)

/**
 * One-shot side effects emitted by [AboutViewModel].
 *
 * @see AboutScreen
 */
public sealed class AboutSideEffect {
    /** Navigates back to the previous screen. */
    public data object GoBackEffect : AboutSideEffect()

    /** Displays an error message via snackbar. */
    public data class ShowError(val messageId: Int) : AboutSideEffect()
}

/**
 * User intents dispatched from the About screen UI.
 *
 * @see AboutViewModel.handleIntent
 */
public sealed class AboutIntent {
    /** User tapped the back/navigation button. */
    public data object OnBackClick : AboutIntent()
}
