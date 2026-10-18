package presentation.feature.mirror.core.payload

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.math.max
import kotlin.math.min

/**
 * The crop of the pinned frame shown in one cell of the Payload tab's crop grid.
 *
 * All of the grid's arithmetic lives here so the composable can draw with no maths of its own:
 * it blits [srcSize] pixels from [srcOffset] into a [dstSize] box and strokes [outlineRect].
 *
 * Float throughout — device pixels are a continuous quantity once the design-space pad is scaled,
 * and rounding to `Int` here would reintroduce the truncate-vs-round question the pad was
 * deliberately kept clear of.
 *
 * @property srcOffset Top-left of the crop in the frame's device-pixel space.
 * @property srcSize Size of the crop in device pixels, clamped to the frame on all four sides.
 * @property dstSize Size the crop is drawn at, i.e. [srcSize] times [scale].
 * @property scale Device pixels to container pixels.
 * @property outlineRect The annotation's own bounds in **container** coordinates, always inside
 *   [dstSize].
 */
internal data class CropWindow(
    val srcOffset: Offset,
    val srcSize: Size,
    val dstSize: Size,
    val scale: Float,
    val outlineRect: Rect,
)

/**
 * Builds the crop window for [rect] on a frame of [frameSize].
 *
 * The breathing room around the annotation is a **design-space** measure: [designPad] is quoted
 * against a [designWidth]-wide mock, so it is scaled by `frameSize.width / designWidth` before it
 * is applied. A literal [designPad] device pixels would be 37px too tight on a 1080-wide frame.
 *
 * [scale] fits the padded window into the [boxW] x [boxH] cell on its tighter axis, so a rect
 * larger than the cell is only ever scaled down.
 *
 * The design's `fixed` cell flag is not modelled — nothing at this call site sets it.
 */
internal fun cropWindow(
    rect: DeviceRect,
    frameSize: IntSize,
    boxW: Float = 84f,
    boxH: Float = 72f,
    designPad: Float = 14f,
    designWidth: Float = 296f,
): CropWindow {
    val pad = designPad * frameSize.width / designWidth

    val cropX = max(0f, rect.left - pad)
    val cropY = max(0f, rect.top - pad)
    val cropW = min(frameSize.width - cropX, rect.width + 2f * pad)
    val cropH = min(frameSize.height - cropY, rect.height + 2f * pad)

    val scale = min(boxW / cropW, boxH / cropH)

    return CropWindow(
        srcOffset = Offset(cropX, cropY),
        srcSize = Size(cropW, cropH),
        dstSize = Size(cropW * scale, cropH * scale),
        scale = scale,
        outlineRect = Rect(
            offset = Offset((rect.left - cropX) * scale, (rect.top - cropY) * scale),
            size = Size(rect.width * scale, rect.height * scale),
        ),
    )
}
