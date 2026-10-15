package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * Live transport statistics.
 *
 * @property fps Achieved frame rate, never the target rate.
 * @property latencyMs Measured latency, or `null` when it cannot be measured — callers
 *   render a placeholder rather than fabricating a number.
 * @property droppedFrames Frames skipped since the session started.
 * @property widthPx Frame width in pixels.
 * @property heightPx Frame height in pixels.
 */
public data class TransportStats(
    val fps: Float = 0f,
    val latencyMs: Long? = null,
    val droppedFrames: Int = 0,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
) : Resource
