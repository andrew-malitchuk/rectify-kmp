package presentation.core.styling.contrast

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import presentation.core.styling.core.ThemeColor
import presentation.core.styling.source.attribute.color.attributeDarkColorPalette
import presentation.core.styling.source.attribute.color.attributeLightColorPalette

/** WCAG 2.2 SC 1.4.3 AA, text. */
private const val TEXT_MINIMUM: Double = 4.5

/** WCAG 2.2 SC 1.4.11 AA, non-text UI components and graphical objects. */
private const val NON_TEXT_MINIMUM: Double = 3.0

/** Baselines are recorded to two decimals; this absorbs that rounding and nothing more. */
private const val BASELINE_TOLERANCE: Double = 0.01

/**
 * One measured pairing.
 *
 * @property label Human-readable `foreground on background`.
 * @property minimum Hard threshold, or `null` when the pairing is a frozen baseline only.
 * @property baseline Recorded ratio; a measurement may never drop below it.
 */
private class Pairing(
    val label: String,
    val foreground: Color,
    val background: Color,
    val minimum: Double?,
    val baseline: Double,
)

/** Composites an rgba overlay token exactly the way Compose does at draw time (A11Y-2). */
private infix fun Color.over(background: Color): Color = compositeOver(background)

private fun MutableList<Pairing>.text(label: String, fg: Color, bg: Color, baseline: Double) {
    add(Pairing(label, fg, bg, TEXT_MINIMUM, baseline))
}

private fun MutableList<Pairing>.nonText(label: String, fg: Color, bg: Color, baseline: Double) {
    add(Pairing(label, fg, bg, NON_TEXT_MINIMUM, baseline))
}

private fun MutableList<Pairing>.frozen(label: String, fg: Color, bg: Color, baseline: Double) {
    add(Pairing(label, fg, bg, minimum = null, baseline = baseline))
}

/**
 * Contrast sweep over both shipped palettes.
 *
 * Assertion policy, decided at the Stage 2 human gate (survey Q1-A / Q2-A / Q3-A). No palette
 * value is edited here — AC-36 freezes them — so the sweep splits in two:
 *
 * - **Tier 1, hard fail** — the pairings this shell actually paints: `ink`/`ink2` on every
 *   surface and `onPrimary` on `primary` at 4.5:1; the `focus` ring at 3:1; `ink3` and the
 *   signals at 3:1 wherever they already clear it.
 * - **Tier 2, frozen baseline** — everything else asserts only that the measured ratio never
 *   drops below the recorded value. Every sub-threshold entry carries an inline `// WAIVED:`
 *   comment naming the ratio and the AC waived.
 *
 * Exemptions encoded by name rather than silently omitted (A11Y-6):
 * - **SC 1.4.3, text in an inactive user-interface component** — `AppButton`'s disabled
 *   "Resolve & Send" label resolves to `ink3`, and disabled-control text is exempt from the
 *   text minimum. This is why `ink3` is swept as incidental/meta ink at 3:1, not at 4.5:1.
 * - **SC 1.4.11, purely decorative graphic** — `StageSurface`'s dotted grid carries no
 *   information, so its composited dot colour is not a measured pairing at all.
 *
 * DEVIATION from the gate's Tier 1 list, flagged rather than hidden: the gate also named the
 * composited `line` and `line2` hairlines at 3:1. That threshold is arithmetically unreachable
 * — an 8%/14% alpha hairline over any surface in either palette measures 1.19:1 to 1.57:1, and
 * the only way to lift it is to edit the tokens AC-36 freezes. They are recorded as frozen
 * baselines below instead of being asserted into a permanently red suite. Needs a human call.
 */
class PaletteContrastTest {

    @Test
    fun `light palette meets its contrast policy`() {
        assertPolicyHolds("light", lightPairings())
    }

    @Test
    fun `dark palette meets its contrast policy`() {
        assertPolicyHolds("dark", darkPairings())
    }

    @Test
    fun `every pairing in both palettes is measured - none silently skipped`() {
        assertEquals(56, lightPairings().size)
        assertEquals(56, darkPairings().size)
    }

    @Test
    fun `the ratio formula matches the WCAG reference points`() {
        assertEquals(21.0, contrastRatio(Color.White, Color.Black), 1e-9)
        assertEquals(1.0, contrastRatio(Color.White, Color.White), 1e-9)
        assertEquals(
            contrastRatio(Color.White, Color.Black),
            contrastRatio(Color.Black, Color.White),
            1e-9,
        )
        assertEquals(0.0, relativeLuminance(Color.Black), 1e-9)
        assertEquals(1.0, relativeLuminance(Color.White), 1e-9)
    }

    @Test
    fun `compositing an alpha token changes what is measured`() {
        val raw = attributeLightColorPalette.line
        val composited = raw over attributeLightColorPalette.surface
        assertEquals(1f, composited.alpha)
        assertTrue(
            contrastRatio(composited, attributeLightColorPalette.surface) < 1.5,
            "an 8% hairline over white is a faint line, not a 20:1 black one",
        )
    }

    /**
     * Collects every violation before failing, rather than aborting on the first.
     *
     * A palette edit typically moves a whole family of pairings at once — five dark alpha
     * tokens means twenty rows. Failing on the first one hides the other nineteen and turns a
     * single fix into a run-edit-run loop, so the report lists them all with their measured
     * values ready to paste back as baselines.
     */
    private fun assertPolicyHolds(palette: String, pairings: List<Pairing>) {
        val violations = buildList {
            pairings.forEach { pairing ->
                val measured = contrastRatio(pairing.foreground, pairing.background)
                val minimum = pairing.minimum
                if (minimum != null && measured < minimum) {
                    add("$palette: ${pairing.label} measured ${measured.format()}:1, below the required $minimum:1")
                }
                if (measured < pairing.baseline - BASELINE_TOLERANCE) {
                    add(
                        "$palette: ${pairing.label} regressed to ${measured.format()}:1 from the " +
                            "recorded baseline ${pairing.baseline}:1 - update the baseline only deliberately",
                    )
                }
            }
        }
        assertTrue(violations.isEmpty(), violations.joinToString(prefix = "\n", separator = "\n"))
    }

    private fun Double.format(): String = ((this * 100).toInt() / 100.0).toString()
}

private fun lightPairings(): List<Pairing> = buildList {
    val c: ThemeColor = attributeLightColorPalette

    // region Tier 1 - hard fail. Text the shell actually paints, at SC 1.4.3 AA 4.5:1.
    text("ink on bg", c.ink, c.bg, baseline = 16.07)
    text("ink on chrome", c.ink, c.chrome, baseline = 14.82)
    text("ink on stage", c.ink, c.stage, baseline = 14.02)
    text("ink on surface", c.ink, c.surface, baseline = 17.38)
    text("ink2 on bg", c.ink2, c.bg, baseline = 6.24)
    text("ink2 on chrome", c.ink2, c.chrome, baseline = 5.75)
    text("ink2 on stage", c.ink2, c.stage, baseline = 5.44)
    text("ink2 on surface", c.ink2, c.surface, baseline = 6.74)
    text("onPrimary on primary", c.onPrimary, c.primary, baseline = 17.38)
    text("onPrimary on primaryHover", c.onPrimary, c.primaryHover, baseline = 12.03)
    text("onPrimary on primaryPress", c.onPrimary, c.primaryPress, baseline = 21.0)
    // endregion

    // region Tier 1 - hard fail. Focus ring, a non-text UI component at SC 1.4.11 3:1.
    nonText("focus on bg", c.focus, c.bg, baseline = 3.72)
    nonText("focus on chrome", c.focus, c.chrome, baseline = 3.43)
    nonText("focus on stage", c.focus, c.stage, baseline = 3.25)
    nonText("focus on surface", c.focus, c.surface, baseline = 4.03)
    // endregion

    // region ink3 - incidental/meta ink at 3:1 (Q2-A); where it misses, the shell authors its
    // own text with ink2 and the residue is disabled-control text, exempt under SC 1.4.3.
    nonText("ink3 on bg", c.ink3, c.bg, baseline = 3.13)
    frozen("ink3 on chrome", c.ink3, c.chrome, baseline = 2.89) // WAIVED: 2.89:1 < 3:1 - AC-36/NFR-11 waived (Q2-A).
    frozen("ink3 on stage", c.ink3, c.stage, baseline = 2.73) // WAIVED: 2.73:1 < 3:1 - AC-36/NFR-11 waived (Q2-A).
    nonText("ink3 on surface", c.ink3, c.surface, baseline = 3.39)
    // endregion

    // region Signals at 3:1 where they clear it, frozen baseline where they do not - this
    // shell paints no signal colour at all, which is why Q1-A waives the misses.
    nonText("annotate on bg", c.annotate, c.bg, baseline = 3.61)
    nonText("annotate on chrome", c.annotate, c.chrome, baseline = 3.33)
    nonText("annotate on stage", c.annotate, c.stage, baseline = 3.15)
    nonText("annotate on surface", c.annotate, c.surface, baseline = 3.90)
    frozen("sentFixed on bg", c.sentFixed, c.bg, baseline = 2.70) // WAIVED: 2.70:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("sentFixed on chrome", c.sentFixed, c.chrome, baseline = 2.49) // WAIVED: 2.49:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("sentFixed on stage", c.sentFixed, c.stage, baseline = 2.36) // WAIVED: 2.36:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("sentFixed on surface", c.sentFixed, c.surface, baseline = 2.92) // WAIVED: 2.92:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("warnStillOff on bg", c.warnStillOff, c.bg, baseline = 2.19) // WAIVED: 2.19:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("warnStillOff on chrome", c.warnStillOff, c.chrome, baseline = 2.02) // WAIVED: 2.02:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("warnStillOff on stage", c.warnStillOff, c.stage, baseline = 1.91) // WAIVED: 1.91:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    frozen("warnStillOff on surface", c.warnStillOff, c.surface, baseline = 2.36) // WAIVED: 2.36:1 < 3:1 - AC-40/NFR-11 waived (Q1-A).
    nonText("measure on bg", c.measure, c.bg, baseline = 3.72)
    nonText("measure on chrome", c.measure, c.chrome, baseline = 3.43)
    nonText("measure on stage", c.measure, c.stage, baseline = 3.25)
    nonText("measure on surface", c.measure, c.surface, baseline = 4.03)
    // endregion

    // region Alpha overlays, composited over every surface they are drawn on (A11Y-2).
    frozen("line over bg", c.line over c.bg, c.bg, baseline = 1.19) // WAIVED: 1.19:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line over chrome", c.line over c.chrome, c.chrome, baseline = 1.19) // WAIVED: 1.19:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line over stage", c.line over c.stage, c.stage, baseline = 1.19) // WAIVED: 1.19:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line over surface", c.line over c.surface, c.surface, baseline = 1.19) // WAIVED: 1.19:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over bg", c.line2 over c.bg, c.bg, baseline = 1.39) // WAIVED: 1.39:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over chrome", c.line2 over c.chrome, c.chrome, baseline = 1.37) // WAIVED: 1.37:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over stage", c.line2 over c.stage, c.stage, baseline = 1.38) // WAIVED: 1.38:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over surface", c.line2 over c.surface, c.surface, baseline = 1.38) // WAIVED: 1.38:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("hover over bg", c.hover over c.bg, c.bg, baseline = 1.10) // WAIVED: 1.10:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("hover over chrome", c.hover over c.chrome, c.chrome, baseline = 1.10) // WAIVED: 1.10:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("hover over stage", c.hover over c.stage, c.stage, baseline = 1.10) // WAIVED: 1.10:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("hover over surface", c.hover over c.surface, c.surface, baseline = 1.10) // WAIVED: 1.10:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over bg", c.seg over c.bg, c.bg, baseline = 1.13) // WAIVED: 1.13:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over chrome", c.seg over c.chrome, c.chrome, baseline = 1.14) // WAIVED: 1.14:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over stage", c.seg over c.stage, c.stage, baseline = 1.14) // WAIVED: 1.14:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over surface", c.seg over c.surface, c.surface, baseline = 1.14) // WAIVED: 1.14:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over bg", c.codeBg over c.bg, c.bg, baseline = 1.08) // WAIVED: 1.08:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over chrome", c.codeBg over c.chrome, c.chrome, baseline = 1.08) // WAIVED: 1.08:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over stage", c.codeBg over c.stage, c.stage, baseline = 1.08) // WAIVED: 1.08:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over surface", c.codeBg over c.surface, c.surface, baseline = 1.08) // WAIVED: 1.08:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    // endregion

    // region pop - no onPop token by gate decision (Q3-A); measured against Color.White.
    frozen("pop under Color.White", Color.White, c.pop, baseline = 17.58)
    // endregion
}

private fun darkPairings(): List<Pairing> = buildList {
    val c: ThemeColor = attributeDarkColorPalette

    // region Tier 1 - hard fail. Text the shell actually paints, at SC 1.4.3 AA 4.5:1.
    text("ink on bg", c.ink, c.bg, baseline = 14.74)
    text("ink on chrome", c.ink, c.chrome, baseline = 13.38)
    text("ink on stage", c.ink, c.stage, baseline = 16.16)
    text("ink on surface", c.ink, c.surface, baseline = 12.51)
    text("ink2 on bg", c.ink2, c.bg, baseline = 6.64)
    text("ink2 on chrome", c.ink2, c.chrome, baseline = 6.03)
    text("ink2 on stage", c.ink2, c.stage, baseline = 7.27)
    text("ink2 on surface", c.ink2, c.surface, baseline = 5.63)
    text("onPrimary on primary", c.onPrimary, c.primary, baseline = 16.16)
    text("onPrimary on primaryHover", c.onPrimary, c.primaryHover, baseline = 18.24)
    text("onPrimary on primaryPress", c.onPrimary, c.primaryPress, baseline = 11.74)
    // endregion

    // region Tier 1 - hard fail. Focus ring, a non-text UI component at SC 1.4.11 3:1.
    nonText("focus on bg", c.focus, c.bg, baseline = 6.21)
    nonText("focus on chrome", c.focus, c.chrome, baseline = 5.64)
    nonText("focus on stage", c.focus, c.stage, baseline = 6.81)
    nonText("focus on surface", c.focus, c.surface, baseline = 5.27)
    // endregion

    // region ink3 - incidental/meta ink at 3:1 (Q2-A); where it misses, the shell authors its
    // own text with ink2 and the residue is disabled-control text, exempt under SC 1.4.3.
    nonText("ink3 on bg", c.ink3, c.bg, baseline = 3.39)
    nonText("ink3 on chrome", c.ink3, c.chrome, baseline = 3.07)
    nonText("ink3 on stage", c.ink3, c.stage, baseline = 3.71)
    frozen("ink3 on surface", c.ink3, c.surface, baseline = 2.87) // WAIVED: 2.87:1 < 3:1 - AC-36/NFR-11 waived (Q2-A).
    // endregion

    // region Signals at 3:1 where they clear it, frozen baseline where they do not - this
    // shell paints no signal colour at all, which is why Q1-A waives the misses.
    nonText("annotate on bg", c.annotate, c.bg, baseline = 4.26)
    nonText("annotate on chrome", c.annotate, c.chrome, baseline = 3.87)
    nonText("annotate on stage", c.annotate, c.stage, baseline = 4.67)
    nonText("annotate on surface", c.annotate, c.surface, baseline = 3.62)
    nonText("sentFixed on bg", c.sentFixed, c.bg, baseline = 5.70)
    nonText("sentFixed on chrome", c.sentFixed, c.chrome, baseline = 5.17)
    nonText("sentFixed on stage", c.sentFixed, c.stage, baseline = 6.25)
    nonText("sentFixed on surface", c.sentFixed, c.surface, baseline = 4.83)
    nonText("warnStillOff on bg", c.warnStillOff, c.bg, baseline = 7.04)
    nonText("warnStillOff on chrome", c.warnStillOff, c.chrome, baseline = 6.39)
    nonText("warnStillOff on stage", c.warnStillOff, c.stage, baseline = 7.71)
    nonText("warnStillOff on surface", c.warnStillOff, c.surface, baseline = 5.97)
    nonText("measure on bg", c.measure, c.bg, baseline = 4.13)
    nonText("measure on chrome", c.measure, c.chrome, baseline = 3.75)
    nonText("measure on stage", c.measure, c.stage, baseline = 4.53)
    nonText("measure on surface", c.measure, c.surface, baseline = 3.51)
    // endregion

    // region Alpha overlays, composited over every surface they are drawn on (A11Y-2).
    // Dark alphas are the design's own (.07/.13/.05/.07/.04), not the light palette's values
    // that were previously copied here - hence baselines below differ from lightPairings().
    frozen("line over bg", c.line over c.bg, c.bg, baseline = 1.22) // WAIVED: 1.26:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line over chrome", c.line over c.chrome, c.chrome, baseline = 1.23) // WAIVED: 1.27:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line over stage", c.line over c.stage, c.stage, baseline = 1.20) // WAIVED: 1.22:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line over surface", c.line over c.surface, c.surface, baseline = 1.24) // WAIVED: 1.28:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over bg", c.line2 over c.bg, c.bg, baseline = 1.48) // WAIVED: 1.56:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over chrome", c.line2 over c.chrome, c.chrome, baseline = 1.50) // WAIVED: 1.58:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over stage", c.line2 over c.stage, c.stage, baseline = 1.44) // WAIVED: 1.51:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("line2 over surface", c.line2 over c.surface, c.surface, baseline = 1.50) // WAIVED: 1.57:1 < 3:1 - AC-40/NFR-11 waived, see DEVIATION above.
    frozen("hover over bg", c.hover over c.bg, c.bg, baseline = 1.14) // WAIVED: 1.13:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("hover over chrome", c.hover over c.chrome, c.chrome, baseline = 1.15) // WAIVED: 1.13:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("hover over stage", c.hover over c.stage, c.stage, baseline = 1.12) // WAIVED: 1.11:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("hover over surface", c.hover over c.surface, c.surface, baseline = 1.16) // WAIVED: 1.14:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over bg", c.seg over c.bg, c.bg, baseline = 1.22) // WAIVED: 1.18:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over chrome", c.seg over c.chrome, c.chrome, baseline = 1.23) // WAIVED: 1.20:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over stage", c.seg over c.stage, c.stage, baseline = 1.19) // WAIVED: 1.16:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("seg over surface", c.seg over c.surface, c.surface, baseline = 1.23) // WAIVED: 1.19:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over bg", c.codeBg over c.bg, c.bg, baseline = 1.10) // WAIVED: 1.10:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over chrome", c.codeBg over c.chrome, c.chrome, baseline = 1.11) // WAIVED: 1.11:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over stage", c.codeBg over c.stage, c.stage, baseline = 1.09) // WAIVED: 1.08:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    frozen("codeBg over surface", c.codeBg over c.surface, c.surface, baseline = 1.12) // WAIVED: 1.10:1 < 3:1 - no boundary, SC 1.4.11 n/a.
    // endregion

    // region pop - no onPop token by gate decision (Q3-A); measured against Color.White.
    frozen("pop under Color.White", Color.White, c.pop, baseline = 13.75)
    // endregion
}
