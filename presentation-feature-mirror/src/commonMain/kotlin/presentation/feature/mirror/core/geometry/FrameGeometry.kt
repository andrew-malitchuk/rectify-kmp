package presentation.feature.mirror.core.geometry

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Smallest drag, in **device pixels**, that becomes an annotation.
 *
 * Device pixels rather than host pixels on purpose: the same flick of the wrist must either
 * count or not count regardless of how large the user has made the window, and a threshold in
 * host pixels would silently get stricter as the frame is scaled down.
 */
internal const val MIN_DRAG_DEVICE_PX: Int = 8

/**
 * The three coordinate spaces of the mirrored frame, and the only place that converts between
 * them.
 *
 * ```
 * pointer px  ──toDevice──▶  device px  ──toFrame──▶  frame px  ──toDp──▶  dp
 * ```
 *
 * **Pointer offsets and [framePx] share one pixel space.** `pointerInput` reports positions in
 * the same pixels `onSizeChanged` reports the box in, so the storage leg is a pure ratio:
 * `devicePx = pointerPx / framePx * deviceSize`, with **no density term at all**. Density
 * belongs in exactly one member, [toDp], converting back out for Compose layout — which is
 * precisely where the proof-of-concept put a stray division and drew every mark at half size on
 * a Retina display. Adding a density factor anywhere else reintroduces that bug class, and
 * `pointerToDeviceToDpRoundTripLandsUnderTheCursor` is the test that catches it.
 *
 * Pure, non-`@Composable` and free of any ViewModel or Orbit import, so the whole conversion
 * story is testable with no display attached.
 *
 * @property framePx Size of the painted frame box, in host/pointer pixels.
 * @property deviceSize Size of the device screenshot, in device pixels.
 * @property density Host display density. Never hardcoded — a 2.0 Retina host and a 1.0 host
 *   must both be expressible (AS-10).
 */
internal data class FrameGeometry(
    val framePx: Size,
    val deviceSize: IntSize,
    val density: Float,
) {
    /** True when the frame box has no area to map into — nothing has been laid out yet. */
    private val isDegenerate: Boolean get() = framePx.width <= 0f || framePx.height <= 0f

    /**
     * The storage leg: a pointer drag to the device-pixel rectangle that gets filed in the bug
     * report. **No density term.**
     *
     * Normalises a reversed drag, rounds each corner independently so a non-integer frame scale
     * cannot bias the width, and clamps to the device bounds so a drag released out in the
     * stage's letterbox still produces a rectangle that exists on the device.
     *
     * @param start Pointer position where the drag began, in host pixels.
     * @param end Pointer position where the drag ended, in host pixels.
     * @return The normalised, clamped device-pixel rectangle.
     */
    fun toDevice(start: Offset, end: Offset): DeviceRect {
        if (isDegenerate) return EMPTY_DEVICE_RECT
        val scaleX = deviceSize.width / framePx.width
        val scaleY = deviceSize.height / framePx.height
        return DeviceRect(
            left = (min(start.x, end.x) * scaleX).toDeviceCoordinate(deviceSize.width),
            top = (min(start.y, end.y) * scaleY).toDeviceCoordinate(deviceSize.height),
            right = (max(start.x, end.x) * scaleX).toDeviceCoordinate(deviceSize.width),
            bottom = (max(start.y, end.y) * scaleY).toDeviceCoordinate(deviceSize.height),
        )
    }

    /**
     * The inverse of [toDevice]: a filed device-pixel rectangle back onto the painted frame box.
     * **No density term** either — this is still pixels, just the host's.
     *
     * @param rect Annotation bounds in device pixels.
     * @return The same rectangle in host pixels, relative to the frame box.
     */
    fun toFrame(rect: DeviceRect): Rect {
        if (isDegenerate || deviceSize.width <= 0 || deviceSize.height <= 0) return Rect.Zero
        val scaleX = framePx.width / deviceSize.width
        val scaleY = framePx.height / deviceSize.height
        return Rect(
            left = rect.left * scaleX,
            top = rect.top * scaleY,
            right = rect.right * scaleX,
            bottom = rect.bottom * scaleY,
        )
    }

    /**
     * The render leg, and the **only** member that touches [density]: host pixels to the dp a
     * Compose modifier can be given.
     *
     * @param rect Bounds in host pixels, relative to the frame box.
     * @return The same rectangle in dp.
     */
    fun toDp(rect: Rect): DpRect = DpRect(
        left = (rect.left / density).dp,
        top = (rect.top / density).dp,
        right = (rect.right / density).dp,
        bottom = (rect.bottom / density).dp,
    )

    /**
     * Whether a drag is large enough to become an annotation rather than a stray click.
     *
     * Measured after conversion, in device pixels, against [MIN_DRAG_DEVICE_PX], and in **both**
     * axes: a rectangle 400 px wide and 2 px tall selects nothing a user could have meant.
     *
     * @param start Pointer position where the drag began, in host pixels.
     * @param end Pointer position where the drag ended, in host pixels.
     * @return True when the drag should be committed.
     */
    fun isMeaningfulDrag(start: Offset, end: Offset): Boolean {
        val rect = toDevice(start, end)
        return rect.width >= MIN_DRAG_DEVICE_PX && rect.height >= MIN_DRAG_DEVICE_PX
    }

    private companion object {
        val EMPTY_DEVICE_RECT = DeviceRect(left = 0, top = 0, right = 0, bottom = 0)
    }
}

/** Rounds one edge to a whole device pixel and keeps it on the device. */
private fun Float.toDeviceCoordinate(bound: Int): Int = roundToInt().coerceIn(0, max(bound, 0))
