package presentation.feature.mirror.core.history

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Longest edge of a history thumbnail, in pixels. */
private const val MAX_THUMBNAIL_EDGE_PX = 128

/**
 * Crops [rect] out of [frame] **and downscales it** so the longest edge is at most [maxEdgePx].
 *
 * The downscale is the whole point. `CropEncoder`'s only implementation crops and never
 * scales, so encoding a union box over scattered annotations on a 1080x2400 capture would put
 * a ~250 KB PNG on every history row and hold all of them in Orbit state. Pre-scaling here
 * leaves `CropEncoder` untouched — no new DI seam, no expect/actual, no signature change: the
 * caller just hands the small bitmap to the encoder it already has.
 *
 * `commonMain` only, built from compose-ui-graphics' own `ImageBitmap`/`Canvas`/`drawImageRect`,
 * so there is nothing platform-specific to implement.
 *
 * [rect] is clamped to the frame first; a rect that clamps away to nothing returns `null`
 * rather than throwing — the same null-on-failure contract `CropEncoder` already uses, and the
 * caller's cue to render a bare plate.
 */
internal fun cropAndScale(
    frame: ImageBitmap,
    rect: DeviceRect,
    maxEdgePx: Int = MAX_THUMBNAIL_EDGE_PX,
): ImageBitmap? {
    val left = rect.left.coerceIn(0, frame.width)
    val top = rect.top.coerceIn(0, frame.height)
    val right = rect.right.coerceIn(left, frame.width)
    val bottom = rect.bottom.coerceIn(top, frame.height)
    val srcWidth = right - left
    val srcHeight = bottom - top
    if (srcWidth <= 0 || srcHeight <= 0 || maxEdgePx <= 0) return null

    // Only ever scales down: a crop already smaller than the cap keeps its own pixels.
    val scale = min(1f, maxEdgePx.toFloat() / max(srcWidth, srcHeight))
    val dstWidth = max(1, (srcWidth * scale).roundToInt())
    val dstHeight = max(1, (srcHeight * scale).roundToInt())

    val target = ImageBitmap(dstWidth, dstHeight)
    Canvas(target).drawImageRect(
        image = frame,
        srcOffset = IntOffset(left, top),
        srcSize = IntSize(srcWidth, srcHeight),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(dstWidth, dstHeight),
        paint = Paint().apply { filterQuality = FilterQuality.Medium },
    )
    return target
}
