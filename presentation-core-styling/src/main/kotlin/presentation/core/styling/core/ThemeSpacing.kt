package presentation.core.styling.core

import androidx.compose.ui.unit.Dp

/**
 * Design-token holder for the spacing scale based on an 8 dp grid unit (4/8/16/24/32/48/64).
 *
 * Any value outside this scale is a brand violation. No arbitrary pixel values in components.
 *
 * @param spacingXS 4 dp — minimum touch gap.
 * @param spacingS 8 dp — inner element gap, inline padding.
 * @param spacingL 16 dp — standard screen-edge padding, tile outer padding.
 * @param spacingXL 24 dp.
 * @param spacing2XL 32 dp — section gap.
 * @param spacing3XL 48 dp.
 * @param spacing4XL 64 dp — desktop outer margin.
 * @see Theme.spacing
 */
public data class ThemeSpacing(
    val spacingXS: Dp,
    val spacingS: Dp,
    val spacingL: Dp,
    val spacingXL: Dp,
    val spacing2XL: Dp,
    val spacing3XL: Dp,
    val spacing4XL: Dp,
)
