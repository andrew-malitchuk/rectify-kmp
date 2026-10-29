package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * One captured frame.
 *
 * NOT a value-equality type: [bytes] makes `equals` reference-based, so a frame must
 * never be a reduced-state field or a `distinctUntilChanged` key.
 *
 * @property bytes Encoded PNG bytes.
 * @property widthPx Frame width in pixels.
 * @property heightPx Frame height in pixels.
 * @property capturedAtMs Wall-clock capture time.
 */
public data class MirrorFrame(
    val bytes: ByteArray,
    val widthPx: Int,
    val heightPx: Int,
    val capturedAtMs: Long,
) : Resource
