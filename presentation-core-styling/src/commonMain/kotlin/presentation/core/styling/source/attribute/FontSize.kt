package presentation.core.styling.source.attribute

import androidx.compose.ui.unit.sp
import presentation.core.styling.core.ThemeFontSize

/**
 * Concrete font-size values for the Rectify typographic scale.
 *
 * @see ThemeFontSize
 * @see AttributeTypography
 */
internal val attributeFontSize: ThemeFontSize =
    ThemeFontSize(
        titleL = 20.sp,
        titleM = 18.sp,
        titleS = 15.sp,
        body = 13.sp,
        bodyStrong = 13.sp,
        comment = 12.5.sp,
        label = 12.5.sp,
        secondary = 12.sp,
        small = 11.5.sp,
        caption = 11.sp,
        monoData = 11.sp,
        monoMeta = 10.5.sp,
        overline = 10.5.sp,
        monoBadge = 10.sp,
        monoTag = 9.5.sp,
        monoRuler = 8.5.sp,
        display = 52.sp,
        numeralXl = 34.sp,
        numeralLg = 26.sp,
    )
