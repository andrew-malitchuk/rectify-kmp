package data.device.impl.source.transport

import co.touchlab.kermit.Logger
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.MirrorTransport
import data.device.api.source.model.DeviceCommand
import data.device.api.source.model.DeviceId
import data.device.api.source.model.MirrorFrame
import data.device.api.source.model.TransportStats
import data.device.api.source.parse.trimToPngSignature
import data.device.api.source.tuning.MirrorTuning
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private val log = Logger.withTag("ScreencapMirrorTransport")

/**
 * Frames from `adb exec-out screencap -p`.
 *
 * The loop is adaptive and single-flight BY CONSTRUCTION: it awaits the current capture
 * before issuing the next one, then sleeps only the remainder of
 * [MirrorTuning.CaptureFloorMs]. A capture that overruns the floor therefore cannot stack a
 * second `adb` child on the USB interface - it costs frames, which are counted, not processes.
 *
 * @property executor The device-scoped command path.
 * @property nowMs Wall-clock reader. A parameter for the same reason
 *   `DeviceRepositoryImpl` takes one: the dropped-frame arithmetic and the fps window are
 *   measured against the real clock, so they are only assertable under virtual time if the
 *   clock is injectable.
 */
internal class ScreencapMirrorTransport(
    private val executor: DeviceCommandExecutor,
    private val nowMs: () -> Long = System::currentTimeMillis,
) : MirrorTransport {

    private val transportStats = MutableStateFlow(TransportStats())

    override val stats: StateFlow<TransportStats> = transportStats.asStateFlow()

    override fun frames(device: DeviceId): Flow<MirrorFrame> = flow {
        transportStats.value = TransportStats()
        var dropped = 0
        var framesInWindow = 0
        var windowStartedAtMs = nowMs()

        while (currentCoroutineContext().isActive) {
            val issuedAtMs = nowMs()
            // A DeviceCommandException propagates on purpose: the device vanishing mid-stream
            // ends the session (the collector sees the failure and flips the chip to Lost)
            // rather than spinning on a dead serial. ProcessRunner has already killed the child.
            val raw = executor.run(device, DeviceCommand.Screencap)
            val captureMs = nowMs() - issuedAtMs
            val png = trimToPngSignature(raw)
            val size = png?.let(::pngSize)

            if (png == null || size == null) {
                // Truncated or non-PNG stdout: one lost frame, not a lost session.
                dropped++
                log.d { "screencap produced no decodable PNG for ${device.value}" }
            } else {
                framesInWindow++
                emit(
                    MirrorFrame(
                        bytes = png,
                        widthPx = size.first,
                        heightPx = size.second,
                        capturedAtMs = issuedAtMs,
                    )
                )
            }
            // Overdue: the capture ate more than one slot at the target rate. Those slots are
            // frames the user did not get, so they are reported rather than silently smoothed.
            dropped += ((captureMs / MirrorTuning.CaptureFloorMs) - 1).coerceAtLeast(0).toInt()

            val now = nowMs()
            val windowMs = now - windowStartedAtMs
            val achievedFps = if (windowMs >= MirrorTuning.StatsTickMs) {
                (framesInWindow * 1000f / windowMs).also {
                    framesInWindow = 0
                    windowStartedAtMs = now
                }
            } else {
                transportStats.value.fps
            }
            transportStats.value = TransportStats(
                fps = achievedFps,
                // latencyMs stays null ON PURPOSE (AC-45): a polling transport cannot see
                // the device-side compositing or the host-side decode and paint, so it has
                // no glass-to-glass number to report and the status bar renders a
                // placeholder. What IS measured is the host-side capture round-trip, and it
                // is reported under that name rather than dressed up as latency.
                captureRoundTripMs = captureMs,
                droppedFrames = dropped,
                widthPx = size?.first ?: transportStats.value.widthPx,
                heightPx = size?.second ?: transportStats.value.heightPx,
            )

            delay((MirrorTuning.CaptureFloorMs - captureMs).coerceAtLeast(0))
        }
    }

    /**
     * Width and height from the PNG IHDR header.
     *
     * Read per frame rather than cached, so rotating the device is followed immediately.
     *
     * @return width to height, or `null` when the buffer is too short to hold a header.
     */
    private fun pngSize(png: ByteArray): Pair<Int, Int>? {
        if (png.size < IHDR_END) return null
        return png.beInt(IHDR_WIDTH_AT) to png.beInt(IHDR_HEIGHT_AT)
    }

    private fun ByteArray.beInt(at: Int): Int =
        (this[at].toInt() and 0xFF shl 24) or
            (this[at + 1].toInt() and 0xFF shl 16) or
            (this[at + 2].toInt() and 0xFF shl 8) or
            (this[at + 3].toInt() and 0xFF)

    private companion object {
        /** 8-byte signature + 4-byte length + 4-byte `IHDR` tag. */
        const val IHDR_WIDTH_AT = 16
        const val IHDR_HEIGHT_AT = 20
        const val IHDR_END = 24
    }
}
