package presentation.core.styling.source.attribute

import androidx.compose.ui.unit.sp
import presentation.core.styling.core.ThemeLineHeight

/**
 * Concrete line-height values for the Rectify typographic scale.
 *
 * @see ThemeLineHeight
 * @see attributeFontSize
 * @see AttributeTypography
 */
internal val attributeLineHeight: ThemeLineHeight =
    ThemeLineHeight(
        titleL = 26.sp,
        titleM = 24.sp,
        titleS = 20.sp,
        body = 19.sp,
        bodyStrong = 19.sp,
        comment = 17.sp,
        label = 17.sp,
        secondary = 18.sp,
        small = 15.sp,
        caption = 15.sp,
        monoData = 16.sp,
        monoMeta = 16.sp,
        overline = 14.sp,
        monoBadge = 12.sp,
        monoTag = 13.sp,
        monoRuler = 12.sp,
        display = 48.sp,
        numeralXl = 34.sp,
        numeralLg = 26.sp,
    )
