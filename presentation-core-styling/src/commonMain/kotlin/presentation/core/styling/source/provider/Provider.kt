package presentation.core.styling.source.provider

import androidx.compose.runtime.staticCompositionLocalOf
import presentation.core.styling.core.ThemeColor
import presentation.core.styling.core.ThemeCorner
import presentation.core.styling.core.ThemeCornerToken
import presentation.core.styling.core.ThemeFontSize
import presentation.core.styling.core.ThemeLineHeight
import presentation.core.styling.core.ThemeMotion
import presentation.core.styling.core.ThemeRadius
import presentation.core.styling.core.ThemeSize
import presentation.core.styling.core.ThemeSpacing
import presentation.core.styling.core.ThemeSpacingToken
import presentation.core.styling.core.ThemeStroke
import presentation.core.styling.core.ThemeTypography

// CompositionLocal instances that carry each design-token group down the tree.
// Using `staticCompositionLocalOf` because theme tokens rarely change mid-composition;
// static locals avoid per-recomposition lookup overhead compared to `compositionLocalOf`.

// region Color
internal val LocalThemeColor =
    staticCompositionLocalOf<ThemeColor> { error("No ThemeColor provided") }
// endregion

// region Typography
internal val LocalThemeFontSize =
    staticCompositionLocalOf<ThemeFontSize> { error("No ThemeFontSize provided") }

internal val LocalThemeLineHeight =
    staticCompositionLocalOf<ThemeLineHeight> { error("No ThemeLineHeight provided") }

internal val LocalThemeTypography =
    staticCompositionLocalOf<ThemeTypography> { error("No ThemeTypography provided") }
// endregion

// region Spacing
internal val LocalThemeSpacing =
    staticCompositionLocalOf<ThemeSpacing> { error("No ThemeSpacing provided") }

internal val LocalThemeSpacingToken =
    staticCompositionLocalOf<ThemeSpacingToken> { error("No ThemeSpacingToken provided") }
// endregion

// region Radius / Corner
internal val LocalThemeRadius =
    staticCompositionLocalOf<ThemeRadius> { error("No ThemeRadius provided") }

internal val LocalThemeCorner =
    staticCompositionLocalOf<ThemeCorner> { error("No ThemeCorner provided") }

internal val LocalThemeCornerToken =
    staticCompositionLocalOf<ThemeCornerToken> { error("No ThemeCornerToken provided") }
// endregion

// region Size
internal val LocalThemeSize =
    staticCompositionLocalOf<ThemeSize> { error("No ThemeSize provided") }
// endregion

// region Stroke
internal val LocalThemeStroke =
    staticCompositionLocalOf<ThemeStroke> { error("No ThemeStroke provided") }
// endregion

// region Motion
internal val LocalThemeMotion =
    staticCompositionLocalOf<ThemeMotion> { error("No ThemeMotion provided") }
// endregion
