package presentation.core.styling.core

/**
 * Design-token holder for motion / animation configuration.
 *
 * When [reducedMotion] is `true`, [durations] is already resolved to a suppressed set — every
 * animated duration reads 0 ms. Call-sites therefore never test [reducedMotion] themselves;
 * they simply use the tokens and get instant behaviour for free. This is the contract:
 * **one place decides, everywhere obeys.**
 *
 * Wire this to the platform's accessibility "reduce motion" setting, or to any other condition
 * your app wants to suppress animation for.
 *
 * @param durations Duration tokens in milliseconds, already suppressed if [reducedMotion].
 * @param easings Easing curve tokens.
 * @param reducedMotion Whether the suppressed duration set is in effect. Read this only to
 *   disable *non-timed* decoration, never to branch on durations.
 * @see Theme.motion
 */
public data class ThemeMotion(
    val durations: ThemeMotionDurations,
    val easings: ThemeMotionEasings,
    val reducedMotion: Boolean = false,
)
