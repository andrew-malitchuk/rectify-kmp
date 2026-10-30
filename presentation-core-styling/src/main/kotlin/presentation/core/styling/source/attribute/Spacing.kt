package presentation.core.styling.source.attribute

import androidx.compose.ui.unit.dp
import presentation.core.styling.core.ThemeSpacing

/**
 * Concrete spacing-scale values — strict 4/8/16/24/32/48/64 dp brand scale.
 *
 * @see ThemeSpacing
 */
internal val attributeSpacing: ThemeSpacing =
    ThemeSpacing(
        spacingXS = 4.dp,
        spacingS = 8.dp,
        spacingL = 16.dp,
        spacingXL = 24.dp,
        spacing2XL = 32.dp,
        spacing3XL = 48.dp,
        spacing4XL = 64.dp,
    )
