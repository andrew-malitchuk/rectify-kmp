package presentation.feature.mirror.core.stats

import domain.core.source.model.device.MirrorStatsModel
import kotlin.math.roundToInt

/** One decimal place, formatted by hand — `String.format` is not in the common stdlib. */
private const val FPS_SCALE = 10f

/** Separator used by every machine line in this design language. */
private const val SEPARATOR = " · "

/**
 * The status bar's machine line, e.g. `adb 35.0.1 · 1080x2400 · 4.2 fps · polling · latency —`.
 *
 * Latency is never fabricated (AC-45): a transport that cannot measure it reports `null` and
 * this renders [latencyPlaceholder] instead of a number. Fields that are not known yet are
 * dropped rather than printed as zero, so the line grows as the session warms up instead of
 * lying about a 0x0 frame.
 *
 * @param stats Latest statistics, or `null` when no session has reported any.
 * @param toolName Binary driving the session, e.g. `adb`.
 * @param pollingHint Copy naming the transport's polling nature.
 * @param latencyPlaceholder Copy shown when latency cannot be measured.
 * @return The formatted line, or an empty string when there is nothing to report.
 */
internal fun formatStatusLine(
    stats: MirrorStatsModel?,
    toolName: String,
    pollingHint: String,
    latencyPlaceholder: String,
): String {
    if (stats == null) return ""
    val parts = buildList {
        add(if (stats.toolVersion.isEmpty()) toolName else "$toolName ${stats.toolVersion}")
        if (stats.widthPx > 0 && stats.heightPx > 0) add("${stats.widthPx}x${stats.heightPx}")
        add("${formatFps(stats.fps)} fps")
        add(pollingHint)
        add(stats.latencyMs?.let { "latency $it ms" } ?: latencyPlaceholder)
    }
    return parts.joinToString(SEPARATOR)
}

/** Rounds to one decimal without pulling in a formatter. */
private fun formatFps(fps: Float): String {
    val tenths = (fps * FPS_SCALE).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
