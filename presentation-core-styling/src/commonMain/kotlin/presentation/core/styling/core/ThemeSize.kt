package presentation.core.styling.core

import androidx.compose.ui.unit.Dp

/**
 * Design-token holder for icon sizes used across the UI.
 *
 * @param iconXXS Smallest icon (12 dp) — inline status indicators.
 * @param iconXS Extra-small icon (16 dp).
 * @param iconS Small icon (20 dp).
 * @param iconM Medium icon (24 dp) — default action icon.
 * @param iconL Large icon (28 dp).
 * @param iconXL Extra-large icon (32 dp).
 * @param icon2XL Double extra-large icon (40 dp).
 * @param icon3XL Triple extra-large icon (56 dp).
 * @param icon4XL Quadruple extra-large icon (120 dp) — hero / splash.
 * @see Theme.size
 */
public data class ThemeSize(
    val iconXXS: Dp,
    val iconXS: Dp,
    val iconS: Dp,
    val iconM: Dp,
    val iconL: Dp,
    val iconXL: Dp,
    val icon2XL: Dp,
    val icon3XL: Dp,
    val icon4XL: Dp,
)
