package presentation.core.styling.core

import androidx.compose.runtime.Composable
import presentation.core.styling.source.provider.LocalThemeColor
import presentation.core.styling.source.provider.LocalThemeCorner
import presentation.core.styling.source.provider.LocalThemeCornerToken
import presentation.core.styling.source.provider.LocalThemeFontSize
import presentation.core.styling.source.provider.LocalThemeLineHeight
import presentation.core.styling.source.provider.LocalThemeMotion
import presentation.core.styling.source.provider.LocalThemeRadius
import presentation.core.styling.source.provider.LocalThemeSize
import presentation.core.styling.source.provider.LocalThemeSpacing
import presentation.core.styling.source.provider.LocalThemeSpacingToken
import presentation.core.styling.source.provider.LocalThemeStroke
import presentation.core.styling.source.provider.LocalThemeTypography

/**
 * Central accessor for all Rectify design-system tokens provided by
 * [AppTheme][presentation.core.styling.source.theme.AppTheme].
 *
 * Usage from any `@Composable` scope:
 * ```
 * val bg = Theme.color.bg
 * val gap = Theme.spacing.spacingL
 * val shape = Theme.cornerToken.button
 * val reducedMotion = Theme.motion.reducedMotion
 * ```
 *
 * @see presentation.core.styling.source.theme.AppTheme
 */
public object Theme {

    // region Color
    public val color: ThemeColor
        @Composable get() = LocalThemeColor.current
    // endregion

    // region Typography
    public val fontSize: ThemeFontSize
        @Composable get() = LocalThemeFontSize.current

    public val lineHeight: ThemeLineHeight
        @Composable get() = LocalThemeLineHeight.current

    public val typography: ThemeTypography
        @Composable get() = LocalThemeTypography.current
    // endregion

    // region Spacing
    public val spacing: ThemeSpacing
        @Composable get() = LocalThemeSpacing.current

    public val spacingToken: ThemeSpacingToken
        @Composable get() = LocalThemeSpacingToken.current
    // endregion

    // region Radius / Corner
    public val radius: ThemeRadius
        @Composable get() = LocalThemeRadius.current

    public val corner: ThemeCorner
        @Composable get() = LocalThemeCorner.current

    public val cornerToken: ThemeCornerToken
        @Composable get() = LocalThemeCornerToken.current
    // endregion

    // region Size
    public val size: ThemeSize
        @Composable get() = LocalThemeSize.current
    // endregion

    // region Stroke
    public val stroke: ThemeStroke
        @Composable get() = LocalThemeStroke.current
    // endregion

    // region Motion
    public val motion: ThemeMotion
        @Composable get() = LocalThemeMotion.current
    // endregion
}
