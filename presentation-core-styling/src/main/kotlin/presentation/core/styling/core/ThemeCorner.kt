package presentation.core.styling.core

import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Design-token holder for named corner shapes built from [ThemeRadius] values.
 *
 * @param xs Small badges nested inside tiles ([ThemeRadius.xs]).
 * @param sm Nested blocks, charts ([ThemeRadius.sm]).
 * @param md Small cards, text inputs ([ThemeRadius.md]).
 * @param lg Default bento tile ([ThemeRadius.lg]).
 * @param xl Hero tile, bottom sheet ([ThemeRadius.xl]).
 * @param pill Fully rounded controls ([ThemeRadius.pill]).
 * @see Theme.corner
 * @see ThemeRadius
 */
public data class ThemeCorner(
    val xs: RoundedCornerShape,
    val sm: RoundedCornerShape,
    val md: RoundedCornerShape,
    val lg: RoundedCornerShape,
    val xl: RoundedCornerShape,
    val pill: RoundedCornerShape,
)
