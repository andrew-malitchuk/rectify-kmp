package presentation.core.ui.source.kit.atom.snackbar

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import presentation.core.ui.source.kit.atom.snackbar.Constant.TWEEN_ANIMATION_DURATION

/**
 * Animation presets for [StackedSnackbarHost] enter / exit transitions.
 *
 * - [Bounce] uses spring physics with high bouncy damping.
 * - [Slide] uses a tween with fast-out-slow-in easing.
 */
@Stable
public enum class StackedSnackbarAnimation(
    public val paddingAnimationSpec: AnimationSpec<Dp>,
    public val scaleAnimationSpec: AnimationSpec<Float>,
    public val enterAnimationSpec: FiniteAnimationSpec<IntOffset>,
    public val exitAnimationSpec: FiniteAnimationSpec<IntOffset>,
) {
    /**
     * The shipped preset: quick, with a small overshoot.
     *
     * `StiffnessLow` + `DampingRatioHighBouncy` — the values this started on — is a slow,
     * wide wobble: low stiffness stretches the settle out and high bounce keeps it visibly
     * oscillating afterwards. `MediumLow` stiffness with medium bounce arrives roughly three
     * times sooner and overshoots once, which is the "snappy" half of the feel without
     * becoming a linear slide.
     */
    Bounce(
        paddingAnimationSpec =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        scaleAnimationSpec =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        enterAnimationSpec =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
        exitAnimationSpec =
        spring(
            // No overshoot on the way out: a card bouncing as it leaves draws the eye back to
            // something that is already finished.
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioNoBouncy,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        ),
    ),
    Slide(
        paddingAnimationSpec =
        tween(
            TWEEN_ANIMATION_DURATION,
            0,
            FastOutSlowInEasing,
        ),
        scaleAnimationSpec =
        tween(
            TWEEN_ANIMATION_DURATION,
            0,
            FastOutSlowInEasing,
        ),
        enterAnimationSpec =
        tween(
            TWEEN_ANIMATION_DURATION,
            0,
            FastOutSlowInEasing,
        ),
        exitAnimationSpec =
        tween(
            TWEEN_ANIMATION_DURATION,
            0,
            FastOutSlowInEasing,
        ),
    ),
}

/**
 * Display-duration presets for stacked snackbars.
 */
@Stable
public enum class StackedSnackbarDuration {
    Short,
    Long,
    Indefinite,
}

/** Layout and animation magic numbers shared across the snackbar subsystem. */
internal object Constant {
    const val SCALE_DECREMENT = 0.05f
    const val PADDING_INCREMENT = 16
    // Positive = below the resting position: the host is bottom-anchored, so a snackbar
    // slides up into view and drops back down on the way out.
    //
    // Deliberately SHORT. At 300px the card spent the whole spring travelling, which reads as
    // sluggish no matter how stiff the spring is — the perceived speed of an entrance is the
    // distance as much as the duration.
    const val Y_TARGET_ENTER = 110
    const val Y_TARGET_EXIT = 110
    const val X_TARGET_EXIT_RIGHT = 1000
    const val X_TARGET_EXIT_LEFT = -1000
    const val OFFSET_THRESHOLD_EXIT_LEFT = -350
    const val OFFSET_THRESHOLD_EXIT_RIGHT = 350
    const val TWEEN_ANIMATION_DURATION = 100
}
