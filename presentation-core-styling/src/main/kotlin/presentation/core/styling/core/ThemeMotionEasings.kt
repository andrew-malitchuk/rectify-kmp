package presentation.core.styling.core

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

/**
 * Easing curve tokens for Rectify animations.
 *
 * There is deliberately no elastic or bounce curve: transitions on the device are abrupt,
 * and the app must not feel softer than the hardware it drives.
 *
 * @param easeOut Decelerating curve — pills, toggles, state changes.
 * @param easeIn Accelerating curve — press feedback.
 * @param easeInOut Symmetric curve — tile entrance, screen transitions, sheets.
 * @param step Step function (no interpolation) — mascot frames, live numeral updates.
 * @see ThemeMotion
 * @see Theme.motion
 */
public data class ThemeMotionEasings(
    val easeOut: Easing = LinearOutSlowInEasing,
    val easeIn: Easing = FastOutLinearInEasing,
    val easeInOut: Easing = FastOutSlowInEasing,
    val step: Easing = Easing { if (it < 1f) 0f else 1f },
)
