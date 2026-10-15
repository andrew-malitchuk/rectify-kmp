package data.device.impl.transport

import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.error.DeviceCommandException
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.tuning.MirrorTuning
import data.device.impl.source.transport.ScreencapMirrorTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val DEVICE: DeviceId = requireNotNull(DeviceId.of("37281FDH2000AB"))

/** Signature + length + `IHDR` tag, then the width and height the transport reads back. */
private fun pngOf(width: Int, height: Int, prefix: ByteArray = ByteArray(0)): ByteArray =
    prefix +
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) +
        byteArrayOf(0x00, 0x00, 0x00, 0x0D) +
        "IHDR".encodeToByteArray() +
        width.beBytes() +
        height.beBytes() +
        byteArrayOf(0x08, 0x06, 0x00, 0x00, 0x00)

private fun Int.beBytes(): ByteArray = byteArrayOf(
    (this ushr 24).toByte(),
    (this ushr 16).toByte(),
    (this ushr 8).toByte(),
    toByte(),
)

/** Wall clock the capture itself advances, so "this capture took N ms" is literal. */
private class FakeClock {
    var nowMs: Long = 0L
    fun read(): Long = nowMs
}

/**
 * Replays scripted `screencap` stdout and burns [captureMs] of wall clock per call.
 *
 * @param responses One entry per capture; the last entry repeats once the script runs out.
 */
private class FakeExecutor(
    private val responses: List<ByteArray>,
    private val clock: FakeClock = FakeClock(),
    private val captureMs: Long = 0L,
) : DeviceCommandExecutor {

    var calls: Int = 0
        private set

    var failAfter: Int = Int.MAX_VALUE

    override suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray {
        if (calls >= failAfter) throw DeviceCommandException(listOf("adb"), null, "device offline")
        clock.nowMs += captureMs
        return responses[minOf(calls, responses.lastIndex)].also { calls++ }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ScreencapMirrorTransportTest {

    @Test
    fun aCaptureIsEmittedWithTheSizeReadFromItsHeader() = runTest {
        val executor = FakeExecutor(listOf(pngOf(1080, 2400)))
        val transport = ScreencapMirrorTransport(executor) { 0L }

        val frames = transport.frames(DEVICE).take(2).toList()

        assertEquals(2, frames.size)
        assertEquals(1080, frames.first().widthPx)
        assertEquals(2400, frames.first().heightPx)
        assertEquals(0, transport.stats.value.droppedFrames)
    }

    @Test
    fun aWarningLinePrependedByTheDeviceIsTrimmedOff() = runTest {
        val noisy = pngOf(720, 1600, prefix = "WARNING: multiple displays\n".encodeToByteArray())
        val transport = ScreencapMirrorTransport(FakeExecutor(listOf(noisy))) { 0L }

        val frame = transport.frames(DEVICE).take(1).toList().single()

        assertEquals(720, frame.widthPx)
        assertEquals(1600, frame.heightPx)
    }

    @Test
    fun stdoutThatIsNotAPngCostsAFrameAndNotTheSession() = runTest {
        val executor = FakeExecutor(listOf("error: closed\n".encodeToByteArray(), pngOf(1080, 2400)))
        val transport = ScreencapMirrorTransport(executor) { 0L }

        val frames = transport.frames(DEVICE).take(1).toList()

        assertEquals(1, frames.size)
        assertEquals(1080, frames.single().widthPx)
        assertEquals(1, transport.stats.value.droppedFrames)
    }

    @Test
    fun aTruncatedHeaderCostsAFrameToo() = runTest {
        val stump = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val executor = FakeExecutor(listOf(stump, pngOf(1080, 2400)))
        val transport = ScreencapMirrorTransport(executor) { 0L }

        transport.frames(DEVICE).take(1).toList()

        assertEquals(1, transport.stats.value.droppedFrames)
    }

    @Test
    fun aCaptureThatOverrunsTheFloorReportsTheSlotsItAte() = runTest {
        // Three capture slots' worth of wall clock for one frame: two slots are frames the
        // user did not get, and they are counted rather than smoothed away. One frame past
        // the one under test, because `take` cancels before the emitting pass updates stats.
        val overrunMs = MirrorTuning.CaptureFloorMs * 3
        val clock = FakeClock()
        val transport = ScreencapMirrorTransport(
            executor = FakeExecutor(listOf(pngOf(1080, 2400)), clock, captureMs = overrunMs),
            nowMs = clock::read,
        )

        transport.frames(DEVICE).take(2).toList()

        assertEquals(2, transport.stats.value.droppedFrames)
        assertEquals(overrunMs, transport.stats.value.latencyMs)
    }

    @Test
    fun aCaptureInsideTheFloorDropsNothing() = runTest {
        val clock = FakeClock()
        val transport = ScreencapMirrorTransport(
            executor = FakeExecutor(
                responses = listOf(pngOf(1080, 2400)),
                clock = clock,
                captureMs = MirrorTuning.CaptureFloorMs / 2,
            ),
            nowMs = clock::read,
        )

        transport.frames(DEVICE).take(3).toList()

        assertEquals(0, transport.stats.value.droppedFrames)
    }

    @Test
    fun theReportedRateIsTheAchievedOneOverTheStatsWindow() = runTest {
        // Four captures of a quarter second each: one second of wall clock, four frames.
        val clock = FakeClock()
        val transport = ScreencapMirrorTransport(
            executor = FakeExecutor(
                responses = listOf(pngOf(1080, 2400)),
                clock = clock,
                captureMs = MirrorTuning.StatsTickMs / 4,
            ),
            nowMs = clock::read,
        )

        transport.frames(DEVICE).take(5).toList()

        assertEquals(4f, transport.stats.value.fps)
    }

    @Test
    fun theRateStaysAtZeroUntilTheWindowCloses() = runTest {
        val clock = FakeClock()
        val transport = ScreencapMirrorTransport(
            executor = FakeExecutor(
                responses = listOf(pngOf(1080, 2400)),
                clock = clock,
                captureMs = MirrorTuning.CaptureFloorMs,
            ),
            nowMs = clock::read,
        )

        transport.frames(DEVICE).take(2).toList()

        assertEquals(0f, transport.stats.value.fps)
    }

    @Test
    fun aDeviceThatVanishesEndsTheSessionInsteadOfSpinning() = runTest {
        val executor = FakeExecutor(listOf(pngOf(1080, 2400)))
        executor.failAfter = 2
        val transport = ScreencapMirrorTransport(executor) { 0L }

        assertFailsWith<DeviceCommandException> {
            transport.frames(DEVICE).take(10).toList()
        }
        assertTrue(executor.calls <= 3, "the loop kept calling a dead device")
    }

    @Test
    fun eachSessionStartsFromCleanStatistics() = runTest {
        val executor = FakeExecutor(listOf("not a png".encodeToByteArray(), pngOf(1080, 2400)))
        val transport = ScreencapMirrorTransport(executor) { 0L }

        transport.frames(DEVICE).take(1).toList()
        assertEquals(1, transport.stats.value.droppedFrames)

        transport.frames(DEVICE).take(1).toList()
        assertEquals(0, transport.stats.value.droppedFrames)
    }
}
