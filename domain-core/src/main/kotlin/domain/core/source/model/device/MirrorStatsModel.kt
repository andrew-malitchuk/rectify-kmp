package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * Live statistics of a mirroring session.
 *
 * @property toolVersion Version of the tool driving the session.
 * @property widthPx Frame width in pixels.
 * @property heightPx Frame height in pixels.
 * @property fps Achieved frames per second, never the target rate.
 * @property latencyMs Measured latency, or `null` when it cannot be measured —
 *   render a placeholder, never a fabricated number.
 * @property droppedFrames Frames skipped since the session started.
 */
public data class MirrorStatsModel(
    val toolVersion: String,
    val widthPx: Int,
    val heightPx: Int,
    val fps: Float,
    val latencyMs: Long? = null,
    val droppedFrames: Int = 0,
) : Model
