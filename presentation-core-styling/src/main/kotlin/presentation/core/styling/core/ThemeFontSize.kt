package presentation.core.styling.core

import androidx.compose.ui.unit.TextUnit

/**
 * Design-token holder for font sizes across the Rectify typographic scale ("Rectify UI Kit"
 * style guide, section 02 — Typography, 16 tokens).
 *
 * @param titleL Screen title — picker — 20 sp, Roboto SemiBold.
 * @param titleM Empty-state heading — 18 sp, Roboto SemiBold.
 * @param titleS Error / card heading — 15 sp, Roboto SemiBold.
 * @param body Default UI text, menus — 13 sp, Roboto Regular.
 * @param bodyStrong Buttons, device names, toasts — 13 sp, Roboto Medium.
 * @param comment Annotation text, rows — 12.5 sp, Roboto Regular.
 * @param label Panel headings — 12.5 sp, Roboto SemiBold.
 * @param secondary Supporting copy, row meta — 12 sp, Roboto Regular.
 * @param small 22px buttons, sub-actions — 11.5 sp, Roboto Medium.
 * @param caption Group headers in lists — 11 sp, Roboto Medium.
 * @param monoData Node ids, commands, chip meta — 11 sp, JetBrains Mono Regular.
 * @param monoMeta Status line, payload, coords — 10.5 sp, JetBrains Mono Regular.
 * @param overline Section labels · UPPERCASE — 10.5 sp, JetBrains Mono Regular.
 * @param monoBadge Number badges, mode label — 10 sp, JetBrains Mono Medium.
 * @param monoTag Measure labels, hierarchy tags (white on signal fill) — 9.5 sp, JetBrains Mono Medium.
 * @param monoRuler Ruler numerals only — 8.5 sp, JetBrains Mono Regular.
 * @param display Hero value — not part of the UI Kit v2 scale; kept at its pre-migration size
 *   (52 sp ExtraBold, now Roboto) because [BentoTile][presentation.core.ui.source.kit.atom.tile.BentoTile]
 *   has no equivalent role in the new 16-token scale.
 * @param numeralXl Bento tile numeral — legacy size (34 sp Bold, now Roboto), see [display].
 * @param numeralLg Compact tile numeral — legacy size (26 sp Bold, now Roboto), see [display].
 * @see ThemeTypography
 * @see Theme.fontSize
 */
public data class ThemeFontSize(
    val titleL: TextUnit,
    val titleM: TextUnit,
    val titleS: TextUnit,
    val body: TextUnit,
    val bodyStrong: TextUnit,
    val comment: TextUnit,
    val label: TextUnit,
    val secondary: TextUnit,
    val small: TextUnit,
    val caption: TextUnit,
    val monoData: TextUnit,
    val monoMeta: TextUnit,
    val overline: TextUnit,
    val monoBadge: TextUnit,
    val monoTag: TextUnit,
    val monoRuler: TextUnit,
    val display: TextUnit,
    val numeralXl: TextUnit,
    val numeralLg: TextUnit,
)
