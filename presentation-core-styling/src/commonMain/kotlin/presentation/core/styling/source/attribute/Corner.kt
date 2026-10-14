package presentation.core.styling.source.attribute

import androidx.compose.foundation.shape.RoundedCornerShape
import presentation.core.styling.core.ThemeCorner

/**
 * Concrete corner shapes built from [attributeRadius] values.
 *
 * @see ThemeCorner
 * @see attributeRadius
 */
internal val attributeCorner: ThemeCorner =
    ThemeCorner(
        xs = RoundedCornerShape(attributeRadius.xs),
        sm = RoundedCornerShape(attributeRadius.sm),
        md = RoundedCornerShape(attributeRadius.md),
        lg = RoundedCornerShape(attributeRadius.lg),
        xl = RoundedCornerShape(attributeRadius.xl),
        pill = RoundedCornerShape(attributeRadius.pill),
    )
