package presentation.feature.mirror

import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import kotlinx.coroutines.runBlocking
import presentation.feature.mirror.core.crop.AwtCropEncoder
import presentation.feature.mirror.source.mirror.DeviceRect
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * A frame [AwtCropEncoder] can clamp against without a real graphics backend.
 *
 * `ImageBitmap.toAwtImage()` only calls [readPixels], so a plain interface implementation is
 * a real exercise of production code, not a stand-in for it — and it needs no Skiko-backed
 * surface.
 */
private class FixedSizeFrame(override val width: Int, override val height: Int) : ImageBitmap {
    override val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888
    override val colorSpace: ColorSpace = ColorSpaces.Srgb
    override val hasAlpha: Boolean = false

    override fun prepareToDraw(): Unit = Unit

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ): Unit = Unit
}

/**
 * TAC-1 — [AwtCropEncoder.encode] clamps [DeviceRect] to the frame's bounds first, then encodes
 * a PNG, returning `null` (never throwing) on failure.
 *
 * Whether the encode happens off the main thread is an implementation detail of `withContext`
 * dispatch, not independently asserted here — same call this repo already made for
 * `ProcessRunner`'s `Dispatchers.IO` usage: behaviour is tested, dispatcher identity is not.
 */
class AwtCropEncoderTest {

    private val encoder = AwtCropEncoder()

    @Test
    fun encodeClampsARectThatIsEntirelyOutOfBoundsToTheFullFrame() = runBlocking {
        val frame = FixedSizeFrame(width = 20, height = 10)
        val entirelyOutOfBounds = DeviceRect(left = -50, top = -50, right = 500, bottom = 500)

        val png = encoder.encode(frame, entirelyOutOfBounds)

        assertNotNull(png, "a rect outside the frame on every edge must still clamp and encode")
        val decoded = ImageIO.read(ByteArrayInputStream(png))
        assertEquals(frame.width, decoded.width, "clamped width must match the frame, not the requested rect")
        assertEquals(frame.height, decoded.height, "clamped height must match the frame, not the requested rect")
    }

    @Test
    fun encodeClampsARectThatIsPartiallyOutOfBoundsPerEdge() = runBlocking {
        val frame = FixedSizeFrame(width = 20, height = 10)
        // left/top are inside the frame; right/bottom overshoot - each edge clamps on its own.
        val partiallyOutOfBounds = DeviceRect(left = 5, top = 2, right = 50, bottom = 50)

        val png = encoder.encode(frame, partiallyOutOfBounds)

        assertNotNull(png)
        val decoded = ImageIO.read(ByteArrayInputStream(png))
        assertEquals(15, decoded.width, "right must clamp to the frame edge, not stay at 50")
        assertEquals(8, decoded.height, "bottom must clamp to the frame edge, not stay at 50")
    }

    @Test
    fun encodeReturnsNullRatherThanThrowingWhenTheClampedRectHasNoOverlap() = runBlocking {
        val frame = FixedSizeFrame(width = 20, height = 10)
        // Entirely to the right of the frame - clamps to a zero-width rect.
        val noOverlap = DeviceRect(left = 30, top = 0, right = 40, bottom = 10)

        val png = encoder.encode(frame, noOverlap)

        assertNull(png, "a rect with no overlap after clamping must fail safely, not throw")
    }
}
