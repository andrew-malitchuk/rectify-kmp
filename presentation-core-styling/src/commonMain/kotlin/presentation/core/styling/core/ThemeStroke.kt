package presentation.core.styling.core

import androidx.compose.ui.unit.Dp

/**
 * Design-token holder for stroke / border widths.
 *
 * Brutalist design uses 1 dp borders by default; 2 dp for active emphasis (gauge arc,
 * active nav indicator).
 *
 * @param hairline Sub-pixel decorative stroke (0.5 dp).
 * @param thin Standard tile / input border (1 dp).
 * @param regular Active-emphasis stroke — gauge arc, active indicator (2 dp).
 * @see Theme.stroke
 */
public data class ThemeStroke(
    val hairline: Dp,
    val thin: Dp,
    val regular: Dp,
)
