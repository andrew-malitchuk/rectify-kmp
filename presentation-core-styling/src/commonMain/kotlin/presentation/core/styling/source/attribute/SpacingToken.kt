package presentation.core.styling.source.attribute

import presentation.core.styling.core.ThemeSpacing
import presentation.core.styling.core.ThemeSpacingToken

/**
 * Maps the raw [ThemeSpacing] scale to semantic [ThemeSpacingToken] roles.
 *
 * @param spacing The resolved spacing scale.
 * @return [ThemeSpacingToken] with all semantic fields mapped.
 * @see ThemeSpacingToken
 */
internal fun getAttributeSpacingToken(spacing: ThemeSpacing): ThemeSpacingToken =
    ThemeSpacingToken(
        contentPadding = spacing.spacingL,
        itemGap = spacing.spacingS,
        sectionGap = spacing.spacing2XL,
        inlinePadding = spacing.spacingS,
        compactGap = spacing.spacingXS,
    )
