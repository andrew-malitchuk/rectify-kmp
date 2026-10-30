package presentation.core.styling.core

import androidx.compose.ui.unit.Dp

/**
 * Design-token holder for corner radius values (UI Kit v2 — Bento).
 *
 * The v1 brutalist 0 dp rule is retired: the device is sharp because it is a pixel
 * grid, the phone is a soft object in the hand. There is no `none` value — a component
 * that wants square corners is out of system.
 *
 * @param xs Small badges nested inside tiles (8 dp).
 * @param sm Nested blocks, charts (12 dp).
 * @param md Small cards, text inputs (16 dp).
 * @param lg **Default bento tile** (22 dp).
 * @param xl Hero tile, bottom sheet (28 dp).
 * @param pill Fully rounded — all buttons, chips, toggles, tab bar (999 dp).
 * @see Theme.radius
 */
public data class ThemeRadius(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val pill: Dp,
)
