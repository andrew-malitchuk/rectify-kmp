package presentation.core.styling.source.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.attribute.AttributeTypography
import presentation.core.styling.source.attribute.attributeCorner
import presentation.core.styling.source.attribute.attributeFontSize
import presentation.core.styling.source.attribute.attributeLineHeight
import presentation.core.styling.source.attribute.attributeMotion
import presentation.core.styling.source.attribute.attributeRadius
import presentation.core.styling.source.attribute.attributeSize
import presentation.core.styling.source.attribute.attributeSpacing
import presentation.core.styling.source.attribute.attributeStroke
import presentation.core.styling.source.attribute.color.attributeDarkColorPalette
import presentation.core.styling.source.attribute.color.attributeLightColorPalette
import presentation.core.styling.source.attribute.getAttributeCornerToken
import presentation.core.styling.source.attribute.getAttributeSpacingToken
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
 * Top-level composable that provides Rectify design-system tokens to the composition tree.
 *
 * Wrap your root content with [AppTheme]:
 * ```
 * AppTheme(mode = ThemeMode.Dark) {
 *     Scaffold(containerColor = Theme.color.bg) { ... }
 * }
 * ```
 *
 * The **Dark** mode is the primary Rectify experience. Light mode swaps to a warm grey
 * background with paper surfaces and darkened ink.
 *
 * Set [reducedMotion] to `true` to resolve motion tokens to a suppressed set, so animations
 * stop without any call-site opting in — e.g. wire it to the platform's accessibility
 * "reduce motion" setting.
 *
 * @param mode The desired appearance mode; defaults to [ThemeMode.Dark].
 * @param reducedMotion Resolves motion tokens to the suppressed set when `true`. Defaults to `false`.
 * @param content Composable content that will have access to the provided design tokens.
 * @see presentation.core.styling.core.Theme
 * @see ThemeMode
 */
@Composable
public fun AppTheme(
    mode: ThemeMode = ThemeMode.Dark,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()

    val currentColorPalette = when (mode) {
        ThemeMode.Light -> attributeLightColorPalette
        ThemeMode.Dark -> attributeDarkColorPalette
        ThemeMode.System -> if (isSystemDark) attributeDarkColorPalette else attributeLightColorPalette
    }

    val typography = AttributeTypography()

    CompositionLocalProvider(
        // Color
        LocalThemeColor provides currentColorPalette,
        // Typography
        LocalThemeFontSize provides attributeFontSize,
        LocalThemeLineHeight provides attributeLineHeight,
        LocalThemeTypography provides typography,
        // Spacing
        LocalThemeSpacing provides attributeSpacing,
        LocalThemeSpacingToken provides getAttributeSpacingToken(attributeSpacing),
        // Radius / Corner
        LocalThemeRadius provides attributeRadius,
        LocalThemeCorner provides attributeCorner,
        LocalThemeCornerToken provides getAttributeCornerToken(attributeCorner),
        // Size
        LocalThemeSize provides attributeSize,
        // Stroke
        LocalThemeStroke provides attributeStroke,
        // Motion
        LocalThemeMotion provides attributeMotion(reducedMotion),
        content = content,
    )
}
