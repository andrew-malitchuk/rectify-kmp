package presentation.feature.mirror.core.measure

import presentation.feature.mirror.core.annotation.TIMES
import presentation.feature.mirror.source.mirror.MeasureSide
import presentation.feature.mirror.source.mirror.MeasureUnit
import presentation.feature.mirror.source.mirror.MeasureValue

/** `Δ`, U+0394 — the design's delta prefix. Never the ASCII letter `D` or a `d`. */
private const val DELTA = 'Δ'

/** `·`, U+00B7 — the middle dot every measure string separates its segments with. */
private const val SEPARATOR = " · "

/**
 * `⌥`, U+2325 — the glyph that marks a measure line as machine-written inside a user's note.
 *
 * It is what [replaceNoteLine] recognises, so a re-measure can rewrite its own line without
 * ever touching the user's prose.
 */
private const val OPTION_GLYPH = '⌥'

/** `48×17dp` — the size row. `×` is U+00D7, reused from `AnnotationFormat`, never a letter `x`. */
internal fun sizeLabel(width: Int, height: Int, unit: MeasureUnit): String =
    "$width$TIMES$height${unit.suffix}"

/** `Δ6dp` — a misalignment delta. No space before the unit. */
internal fun deltaLabel(value: Int, unit: MeasureUnit): String = "$DELTA$value${unit.suffix}"

/** `6dp` — one of the panel's Top/Right/Bottom/Left value rows. */
internal fun plainValueLabel(value: Int, unit: MeasureUnit): String = "$value${unit.suffix}"

/** `(1035, 862)` — a device-pixel position. Parenthesised, comma then space. */
internal fun positionLabel(x: Int, y: Int): String = "($x, $y)"

/**
 * The numeral drawn on a gap guide, or `null` when the guide is suppressed (FR-39).
 *
 * Suppression is "below one display unit", evaluated **after** px→dp conversion and **before**
 * rounding. Both halves matter: comparing raw px would never suppress anything (every gap is
 * `>= 1` px), and comparing the rounded value would show a 0.76 dp hairline as `1dp`. A
 * suppressed guide is absent — never a rounded-up `1dp`, never a `0dp`. The panel's own value
 * row for that side is a separate, always-shown figure and is unaffected.
 */
internal fun guideNumeral(gapPx: Int, densityDpi: Int): String? {
    if (displayValueOf(gapPx, densityDpi) < 1.0) return null
    val (value, unit) = toDisplayUnit(gapPx, densityDpi)
    return plainValueLabel(value, unit)
}

/** `top Δ6dp vs item_title · expected 0` — verbatim, including the neighbour's own id. */
internal fun topMisalignmentFinding(delta: Int, unit: MeasureUnit, neighbourId: String): String =
    "top ${deltaLabel(delta, unit)} vs $neighbourId${SEPARATOR}expected 0"

/** `left Δ9dp vs item_icon · expected 0` — the same shape on the top neighbour. */
internal fun leftMisalignmentFinding(delta: Int, unit: MeasureUnit, neighbourId: String): String =
    "left ${deltaLabel(delta, unit)} vs $neighbourId${SEPARATOR}expected 0"

/**
 * `height 20dp · min touch 48dp`.
 *
 * The advised minimum is 48 while the finding FIRES below 32 ([TOUCH_TARGET_MIN_DP]) — two
 * deliberately different numbers from the design, kept as they are: 32 is "suspicious enough to
 * mention", 48 is the platform guideline to aim at.
 */
internal fun touchTargetFinding(height: Int, unit: MeasureUnit): String =
    "height ${plainValueLabel(height, unit)}${SEPARATOR}min touch 48${unit.suffix}"

/**
 * The one-line measure summary pasted into an annotation's note, e.g.
 * `⌥ item_price 48×17dp · top 15dp · right 20dp · bottom 49dp · left 62dp`.
 *
 * The size row carries no label (it is self-evident) while the other four are named, and the
 * finding, when there is one, is appended as a final segment.
 *
 * @param nodeId The snapped node's id, or `View (no match)`.
 * @param values The panel's five rows, in display order.
 * @param finding The finding message, or `null`.
 */
internal fun noteLine(nodeId: String, values: List<MeasureValue>, finding: String?): String {
    val rows = values.joinToString(SEPARATOR) { value ->
        if (value.side == MeasureSide.Size) value.value else "${value.side.label} ${value.value}"
    }
    val line = "$OPTION_GLYPH $nodeId $rows"
    return if (finding == null) line else "$line$SEPARATOR$finding"
}

/**
 * [comment] with its measure line replaced by [line], or [line] appended when it has none.
 *
 * Re-measuring the same annotation must not stack up lines, and must never eat the user's own
 * prose — the glyph prefix is what tells the two apart.
 */
internal fun replaceNoteLine(comment: String, line: String): String {
    val prose = comment.lineSequence()
        .filterNot { it.startsWith(OPTION_GLYPH) }
        .toList()
        .dropLastWhile { it.isBlank() }
    return (prose + line).joinToString("\n")
}
