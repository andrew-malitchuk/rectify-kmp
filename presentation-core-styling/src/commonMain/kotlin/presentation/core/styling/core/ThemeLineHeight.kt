package presentation.core.styling.core

import androidx.compose.ui.unit.TextUnit

/**
 * Design-token holder for line heights across the Rectify typographic scale.
 *
 * Each value corresponds to its counterpart in [ThemeFontSize].
 *
 * @see ThemeFontSize
 * @see ThemeTypography
 * @see Theme.lineHeight
 */
public data class ThemeLineHeight(
    val titleL: TextUnit,
    val titleM: TextUnit,
    val titleS: TextUnit,
    val body: TextUnit,
    val bodyStrong: TextUnit,
    val comment: TextUnit,
    val label: TextUnit,
    val secondary: TextUnit,
    val small: TextUnit,
    val caption: TextUnit,
    val monoData: TextUnit,
    val monoMeta: TextUnit,
    val overline: TextUnit,
    val monoBadge: TextUnit,
    val monoTag: TextUnit,
    val monoRuler: TextUnit,
    // Legacy — see ThemeFontSize.display
    val display: TextUnit,
    val numeralXl: TextUnit,
    val numeralLg: TextUnit,
)
