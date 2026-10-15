package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * Live transport statistics.
 *
 * @property fps Achieved frame rate, never the target rate.
 * @property latencyMs END-TO-END latency, or `null` when it cannot be measured — callers
 *   render a placeholder rather than fabricating a number. A transport that only knows how
 *   long its own host-side capture took reports that in [captureRoundTripMs] and leaves this
 *   `null`: the two are not the same quantity and must never be substituted for each other.
 * @property captureRoundTripMs How long the host waited for the pixels it is about to show —
 *   issuing the capture command until the bytes are in hand. Diagnostic only: it excludes
 *   device-side compositing and host-side decode and paint, so it is NOT the NFR-1
 *   glass-to-glass latency and is not rendered as one.
 * @property droppedFrames Frames skipped since the session started.
 * @property widthPx Frame width in pixels.
 * @property heightPx Frame height in pixels.
 */
public data class TransportStats(
    val fps: Float = 0f,
    val latencyMs: Long? = null,
    val captureRoundTripMs: Long? = null,
    val droppedFrames: Int = 0,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
) : Resource
