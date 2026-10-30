package presentation.core.styling.source.attribute

import presentation.core.styling.core.ThemeCorner
import presentation.core.styling.core.ThemeCornerToken

/**
 * Maps [ThemeCorner] shapes to semantic [ThemeCornerToken] component roles.
 *
 * @param corner The resolved corner shape set.
 * @return [ThemeCornerToken] with all semantic fields mapped.
 * @see ThemeCornerToken
 */
internal fun getAttributeCornerToken(corner: ThemeCorner): ThemeCornerToken =
    ThemeCornerToken(
        tile = corner.lg,
        tileHero = corner.xl,
        sheet = corner.xl,
        card = corner.md,
        nested = corner.sm,
        badge = corner.xs,
        button = corner.pill,
        chip = corner.pill,
        input = corner.md,
    )
