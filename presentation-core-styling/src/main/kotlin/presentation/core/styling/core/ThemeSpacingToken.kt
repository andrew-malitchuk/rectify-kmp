package presentation.core.styling.core

import androidx.compose.ui.unit.Dp

/**
 * Semantic spacing tokens that map raw [ThemeSpacing] scale values to UI-role names.
 *
 * @param contentPadding Screen-edge / content padding (maps to [ThemeSpacing.spacingL], 16 dp).
 * @param itemGap Default gap between list items (maps to [ThemeSpacing.spacingM], 12 dp).
 * @param sectionGap Gap between major sections (maps to [ThemeSpacing.spacingXL], 24 dp).
 * @param inlinePadding Internal padding for buttons / chips (maps to [ThemeSpacing.spacingS], 8 dp).
 * @param compactGap Tight arrangement spacing (maps to [ThemeSpacing.spacingXS], 4 dp).
 * @see Theme.spacingToken
 * @see ThemeSpacing
 */
public data class ThemeSpacingToken(
    val contentPadding: Dp,
    val itemGap: Dp,
    val sectionGap: Dp,
    val inlinePadding: Dp,
    val compactGap: Dp,
)
