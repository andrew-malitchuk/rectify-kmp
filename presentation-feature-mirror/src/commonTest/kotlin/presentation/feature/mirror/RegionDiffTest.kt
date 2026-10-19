package presentation.feature.mirror

import presentation.feature.mirror.core.verify.CHANNEL_NOISE_FLOOR
import presentation.feature.mirror.core.verify.changedRatio
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * FR-39 / AC-24 -- the pixel metric: per-pixel Chebyshev distance in RGB, gated by
 * [CHANNEL_NOISE_FLOOR], alpha ignored.
 *
 * The thresholds are tuning knobs (NFR-17), so they appear here only to BUILD inputs (a channel
 * delta of exactly the floor, and of floor + 1) and never as an expected value -- retuning the
 * constant moves the data and leaves every expectation below true (bugs.json#PP-TD-4).
 */
class RegionDiffTest {

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    @Test
    fun identicalRegionsReportNoChange() {
        val pixels = intArrayOf(argb(255, 10, 20, 30), argb(255, 200, 100, 50))

        assertEquals(0f, changedRatio(pixels, pixels.copyOf()))
    }

    @Test
    fun aChannelDeltaOfExactlyTheNoiseFloorIsNotCounted() {
        val before = intArrayOf(argb(255, 0, 100, 100))
        val after = intArrayOf(argb(255, CHANNEL_NOISE_FLOOR, 100, 100))

        assertEquals(0f, changedRatio(before, after))
    }

    @Test
    fun aChannelDeltaOneAboveTheNoiseFloorIsCounted() {
        val before = intArrayOf(argb(255, 0, 100, 100))
        val after = intArrayOf(argb(255, CHANNEL_NOISE_FLOOR + 1, 100, 100))

        assertEquals(1f, changedRatio(before, after))
    }

    @Test
    fun theFloorIsCheckedPerChannelSoABlueOnlyShiftStillCounts() {
        val before = intArrayOf(argb(255, 100, 100, 0))
        val after = intArrayOf(argb(255, 100, 100, CHANNEL_NOISE_FLOOR + 1))

        assertEquals(1f, changedRatio(before, after))
    }

    @Test
    fun aChromaOnlySwapAtEqualLumaIsCounted() {
        // Red and blue swapped: identical under any luma metric, obvious to the eye.
        val before = intArrayOf(argb(255, 220, 40, 40))
        val after = intArrayOf(argb(255, 40, 40, 220))

        assertEquals(1f, changedRatio(before, after))
    }

    @Test
    fun alphaOnlyDifferencesAreIgnored() {
        val before = intArrayOf(argb(255, 100, 100, 100))
        val after = intArrayOf(argb(0, 100, 100, 100))

        assertEquals(0f, changedRatio(before, after))
    }

    @Test
    fun theRatioIsChangedPixelsOverTotal() {
        val same = argb(255, 100, 100, 100)
        val different = argb(255, 100, 200, 100)
        val before = intArrayOf(same, same, same, same)
        val after = intArrayOf(same, different, same, same)

        assertEquals(0.25f, changedRatio(before, after))
    }

    @Test
    fun emptyInputReportsNoChange() {
        assertEquals(0f, changedRatio(IntArray(0), IntArray(0)))
    }

    @Test
    fun aLengthMismatchIsNotAMeasurementAndReportsNoChange() {
        val before = intArrayOf(argb(255, 0, 0, 0), argb(255, 0, 0, 0))
        val after = intArrayOf(argb(255, 255, 255, 255))

        assertEquals(0f, changedRatio(before, after))
    }
}
