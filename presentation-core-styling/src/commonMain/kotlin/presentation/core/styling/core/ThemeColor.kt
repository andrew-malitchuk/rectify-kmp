package presentation.core.styling.core

import androidx.compose.ui.graphics.Color

/**
 * Design-token holder for the Rectify color palette ("Rectify UI Kit" style guide, section
 * 01 — Color).
 *
 * A flat ink/surface system — one accent ladder ([primary]/[primaryHover]/[primaryPress]), no
 * multi-accent "bento" scheme. [annotate]/[sentFixed]/[warnStillOff]/[measure] are identical in
 * both themes by design.
 *
 * @param bg Root app background.
 * @param chrome Toolbar / header / nav chrome background — one step up from [bg].
 * @param stage Raised secondary surface — modals, sheets, elevated panels.
 * @param surface Card / tile content surface.
 * @param pop Highest-contrast surface — popovers, code chips, tooltips. Deliberately dark-ish
 *   in both themes.
 * @param ink Primary text / icon ink.
 * @param ink2 Secondary ink — captions, labels.
 * @param ink3 Tertiary ink — metadata, placeholders.
 * @param primary Primary accent — buttons, links, active state.
 * @param primaryHover [primary] hover state.
 * @param primaryPress [primary] pressed state.
 * @param onPrimary Ink rendered on top of a [primary]-filled surface.
 * @param focus Focus ring color.
 * @param line Hairline border — subtle.
 * @param line2 Hairline border — stronger.
 * @param hover Neutral hover fill.
 * @param seg Segmented-control / tab fill.
 * @param codeBg Code block background.
 * @param annotate Signal: annotation / error. Same value in both themes.
 * @param sentFixed Signal: sent / fixed (success). Same value in both themes.
 * @param warnStillOff Signal: warning / still off. Same value in both themes.
 * @param measure Signal: measurement highlight. Same value in both themes.
 * @see Theme.color
 */
public data class ThemeColor(
    // region Surfaces
    val bg: Color,
    val chrome: Color,
    val stage: Color,
    val surface: Color,
    val pop: Color,
    // endregion
    // region Ink
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val primary: Color,
    val primaryHover: Color,
    val primaryPress: Color,
    val onPrimary: Color,
    val focus: Color,
    // endregion
    // region Lines & Fills
    val line: Color,
    val line2: Color,
    val hover: Color,
    val seg: Color,
    val codeBg: Color,
    // endregion
    // region Signal — same in both themes
    val annotate: Color,
    val sentFixed: Color,
    val warnStillOff: Color,
    val measure: Color,
    // endregion
)
