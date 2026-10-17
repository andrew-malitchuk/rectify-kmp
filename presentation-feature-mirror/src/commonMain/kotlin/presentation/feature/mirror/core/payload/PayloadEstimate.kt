package presentation.feature.mirror.core.payload

import kotlin.math.roundToInt

/** Tokens the payload costs before any crop is attached. */
private const val TOKEN_BASE = 360

/** Tokens each crop adds. */
private const val TOKEN_PER_CROP = 140

/** Megabytes the payload weighs before any crop is attached. */
private const val SIZE_BASE_MB = 1.6

/** Megabytes each crop adds. */
private const val SIZE_PER_CROP_MB = 0.12

/** Middle dot (U+00B7), the separator in every machine-text payload label. */
private const val SEPARATOR = " · "

/**
 * Wire format of the exported payload. Bumping it is a deliberate, reviewed decision — a reader
 * on the other side keys its parsing off this number.
 */
internal const val PAYLOAD_FORMAT_VERSION: Int = 1

/**
 * Estimated token cost of the payload for [count] crops.
 *
 * A linear heuristic, not a tokenizer: the UI labels it `≈ tokens`, and running a real tokenizer
 * over the frame, the crops and the hierarchy dump would cost more than the number is worth.
 */
internal fun tokenEstimate(count: Int): Int = TOKEN_BASE + count * TOKEN_PER_CROP

/**
 * Estimated payload size for [count] crops, e.g. `"1.8 MB"` — note the space before the unit.
 *
 * Also a heuristic; the real size is not known until the frame is encoded.
 */
internal fun payloadSizeLabel(count: Int): String =
    oneDecimal(SIZE_BASE_MB + count * SIZE_PER_CROP_MB) + " MB"

/**
 * The payload footer, e.g. `"frame.png · 2 crops · hierarchy.xml"`.
 *
 * Machine text, never localised — it is copied into bug reports and must read the same on every
 * machine. The count is not pluralised for the same reason.
 */
internal fun footerLabel(count: Int): String =
    "frame.png${SEPARATOR}$count crops${SEPARATOR}hierarchy.xml"

/**
 * Rounds to exactly one decimal place without a formatter — `String.format` is JVM-only and does
 * not link on Kotlin/Native.
 *
 * Rounds rather than truncates: `1.96` must read `2.0`, not `1.9`. Always emits the tenths digit,
 * so a whole tenth reads `"3.0"` rather than `"3"`.
 */
private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
