package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * One captured frame.
 *
 * NOT a value-equality type: [bytes] makes `equals` reference-based, so a
 * `MirrorFrameModel` must never be a reduced-state field or a `distinctUntilChanged` key.
 *
 * @property bytes Encoded image bytes.
 * @property widthPx Frame width in pixels.
 * @property heightPx Frame height in pixels.
 * @property capturedAtMs Wall-clock capture time.
 */
public data class MirrorFrameModel(
    val bytes: ByteArray,
    val widthPx: Int,
    val heightPx: Int,
    val capturedAtMs: Long,
) : Model
