package presentation.core.styling.source.attribute

import presentation.core.styling.core.ThemeMotion
import presentation.core.styling.core.ThemeMotionDurations
import presentation.core.styling.core.ThemeMotionEasings

/**
 * Suppressed duration set used when reduced motion is on: every animated duration collapses to 0 ms.
 *
 * [ThemeMotionDurations.numeralRefreshMs] is deliberately preserved — it throttles how
 * often a live value is allowed to change, which matters regardless of motion preference.
 */
private val reducedMotionDurations: ThemeMotionDurations =
    ThemeMotionDurations(
        instantMs = 0,
        fastMs = 0,
        normalMs = 0,
        slowMs = 0,
        tileCascadeMs = 0,
    )

/**
 * Builds the motion configuration for the current reduced-motion state.
 *
 * Resolving the duration set here — rather than asking every animation to check a flag —
 * is what keeps reduced motion honest: a new animation added next month is suppressed
 * automatically because it reads the same tokens.
 *
 * @param reducedMotion `true` to suppress all decorative animation durations.
 * @see ThemeMotion
 */
internal fun attributeMotion(reducedMotion: Boolean = false): ThemeMotion =
    ThemeMotion(
        durations = if (reducedMotion) reducedMotionDurations else ThemeMotionDurations(),
        easings = ThemeMotionEasings(),
        reducedMotion = reducedMotion,
    )
