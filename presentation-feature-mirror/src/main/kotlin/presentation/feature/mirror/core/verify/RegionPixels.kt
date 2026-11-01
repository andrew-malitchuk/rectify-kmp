package presentation.feature.mirror.core.verify

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.source.mirror.DeviceRect

/**
 * The annotation's region of this frame as ARGB pixels, row-major, or `null` for an empty one.
 *
 * **This is the only file in the verify path that names `ImageBitmap`, and only as the
 * extension receiver.** Every other verify function takes `IntArray`s and primitives, which is
 * what keeps the arithmetic unit-testable with no graphics backend; a pure function taking an
 * `ImageBitmap` would be a defect. For the same reason this function is **deliberately left
 * untested**: exercising it means owning a real bitmap, and a readback with no arithmetic in it
 * has nothing a test could check that the Compose implementation does not already guarantee.
 *
 * The rect is **clamped** into the frame before reading: an annotation is dragged by hand and
 * may overhang the frame, and `readPixels` would throw on an out-of-bounds region.
 *
 * @param rect Annotation bounds in device pixels, right/bottom exclusive.
 * @param frameSize The frame these coordinates were drawn against.
 * @return `width * height` pixels, or `null` when the clamped rect has no area.
 */
internal fun ImageBitmap.regionPixels(rect: DeviceRect, frameSize: IntSize): IntArray? {
    val maxX = minOf(frameSize.width, width)
    val maxY = minOf(frameSize.height, height)
    val left = rect.left.coerceIn(0, maxX)
    val top = rect.top.coerceIn(0, maxY)
    val regionWidth = rect.right.coerceIn(0, maxX) - left
    val regionHeight = rect.bottom.coerceIn(0, maxY) - top
    if (regionWidth <= 0 || regionHeight <= 0) return null
    val buffer = IntArray(regionWidth * regionHeight)
    readPixels(buffer, left, top, regionWidth, regionHeight, 0, regionWidth)
    return buffer
}
