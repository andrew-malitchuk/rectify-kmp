package presentation.core.styling.core

import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Semantic corner tokens mapping UI components to their designated [ThemeCorner] shapes.
 *
 * Bento rule: tiles are [ThemeCorner.lg], the one hero tile per screen is [ThemeCorner.xl],
 * and every control is a pill.
 *
 * @param tile Default bento tile (maps to [ThemeCorner.lg], 22 dp).
 * @param tileHero Hero tile — one per screen (maps to [ThemeCorner.xl], 28 dp).
 * @param sheet Bottom sheet / modal (maps to [ThemeCorner.xl], 28 dp).
 * @param card Small card, list row (maps to [ThemeCorner.md], 16 dp).
 * @param nested Block nested inside a tile — chart, inner panel (maps to [ThemeCorner.sm], 12 dp).
 * @param badge Badge nested inside a tile (maps to [ThemeCorner.xs], 8 dp).
 * @param button CTA / ghost button (maps to [ThemeCorner.pill]).
 * @param chip Chip / tag / label pill (maps to [ThemeCorner.pill]).
 * @param input Text input field (maps to [ThemeCorner.md], 16 dp).
 * @see Theme.cornerToken
 * @see ThemeCorner
 */
public data class ThemeCornerToken(
    val tile: RoundedCornerShape,
    val tileHero: RoundedCornerShape,
    val sheet: RoundedCornerShape,
    val card: RoundedCornerShape,
    val nested: RoundedCornerShape,
    val badge: RoundedCornerShape,
    val button: RoundedCornerShape,
    val chip: RoundedCornerShape,
    val input: RoundedCornerShape,
)
