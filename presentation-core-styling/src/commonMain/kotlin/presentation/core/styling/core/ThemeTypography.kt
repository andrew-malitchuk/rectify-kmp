package presentation.core.styling.core

import androidx.compose.ui.text.TextStyle

/**
 * Design-token holder for fully composed [TextStyle] instances across the Rectify type scale
 * ("Rectify UI Kit" style guide, section 02 — Typography).
 *
 * Two font families, selected from a single variable font file per family via
 * `FontVariation.Settings` (no separate static-weight files):
 * - **Roboto Flex** — everything a person reads: titles, labels, comments, errors.
 * - **JetBrains Mono** — everything a machine produced: ids, coordinates, sizes, commands,
 *   versions, timestamps, shortcuts, status.
 *
 * None of these carry a baked-in [androidx.compose.ui.graphics.Color] — the suggested ink role
 * per token (documented below) is applied by the caller via `Theme.color.*`, same as before.
 * Casing is a call-site convention, not a token: [label] and [overline] are rendered uppercase
 * by the caller.
 *
 * @param titleL Screen title — picker. Suggested color: `Theme.color.ink`.
 * @param titleM Empty-state heading. Suggested color: `Theme.color.ink`.
 * @param titleS Error / card heading. Suggested color: `Theme.color.ink`.
 * @param body Default UI text, menus. Suggested color: `Theme.color.ink`.
 * @param bodyStrong Buttons, device names, toasts. Suggested color: `Theme.color.ink`.
 * @param comment Annotation text, rows. Suggested color: `Theme.color.ink`.
 * @param label Panel headings. Suggested color: `Theme.color.ink`.
 * @param secondary Supporting copy, row meta. Suggested color: `Theme.color.ink2`.
 * @param small 22px buttons, sub-actions. Suggested color: `Theme.color.ink2`.
 * @param caption Group headers in lists. Suggested color: `Theme.color.ink3`.
 * @param monoData Node ids, commands, chip meta. Suggested color: `Theme.color.ink`.
 * @param monoMeta Status line, payload, coords. Suggested color: `Theme.color.ink3`.
 * @param overline Section labels · UPPERCASE. Suggested color: `Theme.color.ink3`.
 * @param monoBadge Number badges, mode label. Suggested color: `Theme.color.ink2`.
 * @param monoTag Measure labels, hierarchy tags — rendered on a signal-colored fill, always
 *   white; don't source its color from `Theme.color.*`.
 * @param monoRuler Ruler numerals only. Suggested color: `Theme.color.ink3`.
 * @param display Hero value — not part of the UI Kit v2 scale; see [ThemeFontSize.display].
 * @param numeralXl Bento tile numeral — legacy, see [ThemeFontSize.display].
 * @param numeralLg Compact tile numeral — legacy, see [ThemeFontSize.display].
 * @see Theme.typography
 * @see ThemeFontSize
 * @see ThemeLineHeight
 */
public data class ThemeTypography(
    val titleL: TextStyle,
    val titleM: TextStyle,
    val titleS: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val comment: TextStyle,
    val label: TextStyle,
    val secondary: TextStyle,
    val small: TextStyle,
    val caption: TextStyle,
    val monoData: TextStyle,
    val monoMeta: TextStyle,
    val overline: TextStyle,
    val monoBadge: TextStyle,
    val monoTag: TextStyle,
    val monoRuler: TextStyle,
    val display: TextStyle,
    val numeralXl: TextStyle,
    val numeralLg: TextStyle,
)
