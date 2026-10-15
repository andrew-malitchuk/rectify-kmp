package presentation.feature.mirror

import domain.core.source.model.device.MirrorStatsModel
import presentation.feature.mirror.core.stats.formatStatusLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val POLLING = "polling"
private const val PLACEHOLDER = "latency —"

private fun line(stats: MirrorStatsModel?): String =
    formatStatusLine(stats = stats, toolName = "adb", pollingHint = POLLING, latencyPlaceholder = PLACEHOLDER)

class StatsFormatterTest {

    private val streaming = MirrorStatsModel(
        toolVersion = "35.0.1",
        widthPx = 1080,
        heightPx = 2400,
        fps = 4.24f,
        latencyMs = 180L,
    )

    @Test
    fun aLiveSessionReportsToolResolutionRateAndLatency() {
        assertEquals("adb 35.0.1 · 1080x2400 · 4.2 fps · polling · latency 180 ms", line(streaming))
    }

    @Test
    fun anUnmeasurableLatencyRendersThePlaceholderAndNeverANumber() {
        val rendered = line(streaming.copy(latencyMs = null))
        assertTrue(rendered.endsWith(PLACEHOLDER), rendered)
        assertTrue(!rendered.contains("latency 0"), rendered)
    }

    @Test
    fun theResolutionFollowsTheCurrentFrame() {
        assertTrue(line(streaming.copy(widthPx = 1440, heightPx = 3120)).contains("1440x3120"))
    }

    @Test
    fun anUnknownResolutionIsDroppedRatherThanPrintedAsZero() {
        val rendered = line(streaming.copy(widthPx = 0, heightPx = 0))
        assertTrue(!rendered.contains("0x0"), rendered)
    }

    @Test
    fun noStatisticsMeansNoLine() {
        assertEquals("", line(null))
    }

    @Test
    fun theToolVersionIsOmittedUntilTheToolHasAnswered() {
        assertTrue(line(streaming.copy(toolVersion = "")).startsWith("adb · "))
    }
}
