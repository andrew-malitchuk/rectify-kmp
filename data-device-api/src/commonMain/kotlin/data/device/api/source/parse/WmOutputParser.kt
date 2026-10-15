package data.device.api.source.parse

private val TRAILING_INT = Regex("(\\d+)\\s*$")
private val SIZE = Regex("(\\d+)x(\\d+)\\s*$")

/**
 * Parses `wm size`.
 *
 * `Override size` WINS over `Physical size` on purpose: when a display override is set,
 * the override is the resolution screencap actually produces, so matching it keeps the
 * mirrored frame and the reported size consistent. Do not "fix" this to prefer physical.
 *
 * @return width to height in pixels, or `null` when neither line is present.
 */
public fun parseWmSize(output: String): Pair<Int, Int>? =
    output.sizeAfter("Override size") ?: output.sizeAfter("Physical size")

/**
 * Parses `wm density`.
 *
 * `Override density` wins over `Physical density`, for the same reason as [parseWmSize].
 *
 * @return density in dpi, or `null` when neither line is present.
 */
public fun parseWmDensity(output: String): Int? =
    output.intAfter("Override density") ?: output.intAfter("Physical density")

private fun String.lineStartingWith(prefix: String): String? =
    lineSequence().map(String::trim).firstOrNull { it.startsWith(prefix) }

private fun String.sizeAfter(prefix: String): Pair<Int, Int>? =
    lineStartingWith(prefix)?.let { line ->
        SIZE.find(line)?.let { it.groupValues[1].toInt() to it.groupValues[2].toInt() }
    }

private fun String.intAfter(prefix: String): Int? =
    lineStartingWith(prefix)?.let { line -> TRAILING_INT.find(line)?.groupValues?.get(1)?.toInt() }
