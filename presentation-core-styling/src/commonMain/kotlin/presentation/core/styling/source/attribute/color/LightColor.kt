package presentation.core.styling.source.attribute.color

import androidx.compose.ui.graphics.Color
import presentation.core.styling.core.ThemeColor

/**
 * Light-mode color palette — "Rectify UI Kit" style guide, section 01, light theme.
 *
 * OKLCH-authored tokens ([ThemeColor.focus], the Signal group) are converted to sRGB hex at
 * build time via Björn Ottosson's Oklab matrices with CSS-Color-4-style chroma reduction for
 * out-of-gamut values — the original OKLCH triplet is kept in a trailing comment for
 * traceability.
 *
 * @see attributeDarkColorPalette
 * @see ThemeColor
 */
internal val attributeLightColorPalette: ThemeColor =
    ThemeColor(
        // region Surfaces
        bg = Color(0xFFF6F6F5),
        chrome = Color(0xFFEDEDEB),
        stage = Color(0xFFE7E7E4),
        surface = Color(0xFFFFFFFF),
        pop = Color(0xFF18191C),
        // endregion
        // region Ink
        ink = Color(0xFF1A1A1C),
        ink2 = Color(0xFF5B5B61),
        ink3 = Color(0xFF8B8B91),
        primary = Color(0xFF1A1A1C),
        primaryHover = Color(0xFF36363A),
        primaryPress = Color(0xFF000000),
        onPrimary = Color(0xFFFFFFFF),
        focus = Color(0xFF007DF2), // oklch(0.6 0.2 255)
        // endregion
        // region Lines & Fills — rgba(0,0,0,alpha)
        line = Color(0x14000000), // rgba(0,0,0,.08)
        line2 = Color(0x24000000), // rgba(0,0,0,.14)
        hover = Color(0x0B000000), // rgba(0,0,0,.045)
        seg = Color(0x0F000000), // rgba(0,0,0,.06)
        codeBg = Color(0x09000000), // rgba(0,0,0,.035)
        // endregion
        // region Signal — same in both themes
        annotate = Color(0xFFF13936), // oklch(0.63 0.22 27)
        sentFixed = Color(0xFF3EAB5E), // oklch(0.66 0.15 150)
        warnStillOff = Color(0xFFE69825), // oklch(0.74 0.15 70)
        measure = Color(0xFF007DF2), // oklch(0.6 0.2 255)
        // endregion
    )
