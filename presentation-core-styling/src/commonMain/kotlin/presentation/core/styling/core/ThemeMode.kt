package presentation.core.styling.core

/**
 * Available theme appearance modes for the Rectify design system.
 *
 * **Dark** is the primary experience — ink background, graphite surfaces, paper text.
 * **Light** uses mist + paper + ink for landing-page-style sections.
 *
 * @see presentation.core.styling.source.theme.AppTheme
 */
public enum class ThemeMode {
    /** Forces the light palette (mist/paper/ink) regardless of the system setting. */
    Light,

    /** Forces the dark palette — the primary Rectify experience (ink/graphite/paper). */
    Dark,

    /** Delegates to the platform's current dark-mode preference at runtime. */
    System,
}
