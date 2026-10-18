package presentation.core.styling.source.attribute.color

import androidx.compose.ui.graphics.Color
import presentation.core.styling.core.ThemeColor

/**
 * Dark-mode color palette — "Rectify UI Kit" style guide, section 01, dark theme — the primary
 * Rectify experience.
 *
 * OKLCH-authored tokens ([ThemeColor.focus]) are converted to sRGB hex at build time via Björn
 * Ottosson's Oklab matrices with CSS-Color-4-style chroma reduction for out-of-gamut values —
 * the original OKLCH triplet is kept in a trailing comment for traceability. The Signal group is
 * identical to [attributeLightColorPalette] by design.
 *
 * @see attributeLightColorPalette
 * @see ThemeColor
 */
internal val attributeDarkColorPalette: ThemeColor =
    ThemeColor(
        // region Surfaces
        bg = Color(0xFF1E1E20),
        chrome = Color(0xFF262628),
        stage = Color(0xFF151517),
        stageDot = Color(0x0EFFFFFF), // rgba(255,255,255,.055)
        surface = Color(0xFF2B2B2E),
        pop = Color(0xFF2C2D31),
        overlayScrim = Color(0xFF0C0C0E), // rgb(12,12,14) — fixed near-black, identical in LightColor by design
        // endregion
        // region Ink
        ink = Color(0xFFF1F1F2),
        ink2 = Color(0xFFA3A3AA),
        ink3 = Color(0xFF707077),
        primary = Color(0xFFF1F1F2),
        primaryHover = Color(0xFFFFFFFF),
        primaryPress = Color(0xFFCFCFD3),
        onPrimary = Color(0xFF151517),
        focus = Color(0xFF53A0FF), // oklch(0.7 0.17 255)
        // endregion
        // region Lines & Fills — rgba(255,255,255,alpha), same alpha steps as light
        line = Color(0x12FFFFFF), // rgba(255,255,255,.07)
        line2 = Color(0x21FFFFFF), // rgba(255,255,255,.13)
        hover = Color(0x0DFFFFFF), // rgba(255,255,255,.05)
        seg = Color(0x12FFFFFF), // rgba(255,255,255,.07)
        codeBg = Color(0x0AFFFFFF), // rgba(255,255,255,.04)
        // endregion
        // region Signal — same in both themes
        annotate = Color(0xFFF13936), // oklch(0.63 0.22 27)
        sentFixed = Color(0xFF3EAB5E), // oklch(0.66 0.15 150)
        warnStillOff = Color(0xFFE69825), // oklch(0.74 0.15 70)
        measure = Color(0xFF007DF2), // oklch(0.6 0.2 255)
        // endregion
    )
