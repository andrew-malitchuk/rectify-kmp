package presentation.feature.mirror

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.runBlocking
import presentation.feature.mirror.core.crop.AwtCropEncoder
import presentation.feature.mirror.core.history.cropAndScale
import presentation.feature.mirror.core.history.thumbnailRect
import presentation.feature.mirror.core.history.unionRect
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TAC-7, the C-1 gate — a union box over scattered annotations on a full-resolution capture
 * must reach the history row as a few-KB PNG, not a quarter-megabyte one.
 *
 * `AwtCropEncoder` only `getSubimage`s; it never scales. Without [cropAndScale] in front of it
 * the thumbnail of this very batch is ~1000x1800 device pixels and every history row would
 * hold that in Orbit state. If this test cannot be made to pass, the one documented fallback
 * is a desktop AWT sibling encoder — not a third invention.
 */
class ThumbnailBitmapTest {

    private val frameWidth = 1080
    private val frameHeight = 2400

    /** Scattered annotations on a tall phone capture — the worst realistic case. */
    private val annotations = listOf(
        StagedAnnotation(index = 1, rect = DeviceRect(left = 900, top = 120, right = 1020, bottom = 200)),
        StagedAnnotation(index = 2, rect = DeviceRect(left = 60, top = 1800, right = 200, bottom = 1900)),
        StagedAnnotation(index = 3, rect = DeviceRect(left = 400, top = 900, right = 520, bottom = 1000)),
    )

    /**
     * A stand-in for an `adb screencap` of a shopping app: flat surfaces, a status bar, cards,
     * text lines and one accent button.
     *
     * Content shape matters to the budget, so it is chosen to resemble what the encoder will
     * actually be handed rather than to be easy. A flat fill would compress to nothing and
     * prove nothing; the opposite extreme — per-pixel noise — is not a screenshot either, and
     * was measured at 12,524 bytes for the same 72x128 thumbnail, i.e. the real ceiling if a
     * capture were literally incompressible.
     */
    private fun capture(): ImageBitmap {
        val image = ImageBitmap(frameWidth, frameHeight)
        val canvas = Canvas(image)
        val paint = Paint()

        fun box(left: Float, top: Float, right: Float, bottom: Float, color: Color) {
            paint.color = color
            canvas.drawRect(Rect(left, top, right, bottom), paint)
        }

        box(0f, 0f, 1080f, 2400f, Color(0xFFF7F7F9))
        box(0f, 0f, 1080f, 90f, Color(0xFF1B1B1F))
        box(0f, 90f, 1080f, 260f, Color.White)
        box(48f, 150f, 520f, 200f, Color(0xFF2B2B33))
        var cardTop = 320f
        while (cardTop < 2200f) {
            box(48f, cardTop, 1032f, cardTop + 300f, Color.White)
            box(80f, cardTop + 40f, 380f, cardTop + 240f, Color(0xFFDCDCE4))
            box(420f, cardTop + 50f, 900f, cardTop + 86f, Color(0xFF2B2B33))
            box(420f, cardTop + 110f, 760f, cardTop + 138f, Color(0xFF8A8A96))
            box(420f, cardTop + 180f, 620f, cardTop + 240f, Color(0xFF2F6BFF))
            cardTop += 360f
        }
        box(0f, 2260f, 1080f, 2400f, Color.White)
        return image
    }

    @Test
    fun cropAndScaleCapsTheThumbnailAtOneHundredAndTwentyEightPixelsOnBothAxes() {
        val union = assertNotNull(unionRect(annotations))
        val rect = thumbnailRect(union, IntSize(frameWidth, frameHeight))

        val thumbnail = assertNotNull(cropAndScale(capture(), rect))

        assertTrue(thumbnail.width <= 128, "thumbnail width was ${thumbnail.width}")
        assertTrue(thumbnail.height <= 128, "thumbnail height was ${thumbnail.height}")
        assertTrue(thumbnail.width > 0 && thumbnail.height > 0, "a scaled thumbnail is never empty")
    }

    @Test
    fun theUnchangedCropEncoderTurnsTheScaledThumbnailIntoAPngUnderEightKilobytes() = runBlocking {
        val union = assertNotNull(unionRect(annotations))
        val rect = thumbnailRect(union, IntSize(frameWidth, frameHeight))
        val thumbnail = assertNotNull(cropAndScale(capture(), rect))

        val png = AwtCropEncoder().encode(
            frame = thumbnail,
            rect = DeviceRect(left = 0, top = 0, right = thumbnail.width, bottom = thumbnail.height),
        )

        assertNotNull(png, "the unchanged CropEncoder must handle the pre-scaled bitmap")
        // Measured on this fixture: 541 bytes. The budget has three orders of magnitude of
        // headroom over the ~250 KB unscaled crop this function exists to prevent.
        assertTrue(png.size <= 8 * 1024, "thumbnail PNG was ${png.size} bytes, budget is 8192")
    }

    @Test
    fun cropAndScaleKeepsACropAlreadySmallerThanTheCapAtItsOwnSize() {
        val small = DeviceRect(left = 10, top = 10, right = 50, bottom = 40)

        val thumbnail = assertNotNull(cropAndScale(capture(), small))

        assertTrue(thumbnail.width == 40 && thumbnail.height == 30, "upscaling is never wanted")
    }

    @Test
    fun cropAndScaleReturnsNullRatherThanThrowingOnADegenerateRect() {
        val frame = capture()

        assertNull(cropAndScale(frame, DeviceRect(left = 500, top = 0, right = 500, bottom = 100)))
        assertNull(cropAndScale(frame, DeviceRect(left = 2000, top = 0, right = 3000, bottom = 100)))
        assertNull(cropAndScale(frame, DeviceRect(left = 0, top = 0, right = 100, bottom = 100), maxEdgePx = 0))
    }
}
