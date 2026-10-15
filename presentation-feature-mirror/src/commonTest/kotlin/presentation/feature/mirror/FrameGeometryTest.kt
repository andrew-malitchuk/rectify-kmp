package presentation.feature.mirror

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.core.geometry.MIN_DRAG_DEVICE_PX
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A 1080x2400 device painted into a 1426x3168 frame box on a 2.0 Retina host.
 *
 * The frame scale 1426/1080 is 1.320 — deliberately neither 1.0 nor equal to the density, so a
 * run that confused the scale with the density produces a visibly different number instead of
 * accidentally agreeing.
 */
private val RETINA = FrameGeometry(
    framePx = Size(1426f, 3168f),
    deviceSize = IntSize(1080, 2400),
    density = 2f,
)

private const val TOLERANCE = 1.5f

private fun assertNear(expected: Float, actual: Float, what: String) {
    assertTrue(abs(expected - actual) <= TOLERANCE, "$what expected ~$expected but was $actual")
}

class FrameGeometryTest {

    @Test
    fun densityTwoDoesNotScaleTheStoredRect() {
        val rect = RETINA.toDevice(Offset(100f, 200f), Offset(500f, 800f))

        // Hand-computed: 100 * 1080/1426 = 75.74 -> 76, 200 * 2400/3168 = 151.52 -> 152,
        // 500 * 1080/1426 = 378.68 -> 379, 800 * 2400/3168 = 606.06 -> 606. No factor of 2.
        assertEquals(DeviceRect(left = 76, top = 152, right = 379, bottom = 606), rect)

        // The same drag mapped by a geometry that differs ONLY in density must be identical.
        assertEquals(rect, RETINA.copy(density = 1f).toDevice(Offset(100f, 200f), Offset(500f, 800f)))
    }

    @Test
    fun toDpDividesByDensityExactlyOnce() {
        val dp = RETINA.toDp(Rect(left = 0f, top = 0f, right = 100f, bottom = 50f))

        assertEquals(50f, dp.right.value)
        assertEquals(25f, dp.bottom.value)
    }

    @Test
    fun pointerToDeviceToDpRoundTripLandsUnderTheCursor() {
        val start = Offset(260f, 540f)
        val end = Offset(940f, 1700f)

        val backInDp = RETINA.toDp(RETINA.toFrame(RETINA.toDevice(start, end)))

        // Multiplying dp back by the density returns the pointer's own pixel space. A missing
        // or doubled division in toDp misses the cursor by exactly the density factor.
        assertNear(start.x, backInDp.left.value * RETINA.density, "left")
        assertNear(start.y, backInDp.top.value * RETINA.density, "top")
        assertNear(end.x, backInDp.right.value * RETINA.density, "right")
        assertNear(end.y, backInDp.bottom.value * RETINA.density, "bottom")
    }

    @Test
    fun reversedDragNormalisesToPositiveRect() {
        val forward = RETINA.toDevice(Offset(100f, 200f), Offset(500f, 800f))
        val reversed = RETINA.toDevice(Offset(500f, 800f), Offset(100f, 200f))

        assertEquals(forward, reversed)
        assertTrue(reversed.width > 0 && reversed.height > 0, "$reversed")
    }

    @Test
    fun dragEndingInLetterboxClampsToDeviceBounds() {
        val rect = RETINA.toDevice(Offset(-400f, -900f), Offset(3000f, 7000f))

        assertEquals(DeviceRect(left = 0, top = 0, right = 1080, bottom = 2400), rect)
    }

    @Test
    fun nonIntegerScaleRoundsEachCornerIndependently() {
        // Scale 1080/1426: 3 px -> 2.27 -> 2, 4 px -> 3.03 -> 3. Rounding the ORIGIN and then
        // adding a rounded width would give 2..2 or 2..3 depending on order; independent
        // rounding of both edges is the only answer that stays put when the box is resized.
        val rect = RETINA.toDevice(Offset(3f, 3f), Offset(4f, 4f))

        assertEquals(2, rect.left)
        assertEquals(3, rect.right)
    }

    @Test
    fun zeroFrameBoxReturnsEmptyRect() {
        val unlaidOut = RETINA.copy(framePx = Size.Zero)

        assertEquals(DeviceRect(left = 0, top = 0, right = 0, bottom = 0), unlaidOut.toDevice(Offset(10f, 10f), Offset(90f, 90f)))
        assertEquals(Rect.Zero, unlaidOut.toFrame(DeviceRect(left = 1, top = 2, right = 3, bottom = 4)))
    }

    @Test
    fun roundTripToFrameAndBackIsStable() {
        val original = DeviceRect(left = 120, top = 340, right = 660, bottom = 1200)

        val frame = RETINA.toFrame(original)
        val returned = RETINA.toDevice(Offset(frame.left, frame.top), Offset(frame.right, frame.bottom))

        assertEquals(original, returned)
    }

    @Test
    fun subMinimumDragIsNotMeaningful() {
        // MIN_DRAG_DEVICE_PX device px is 8 * 1426/1080 = 10.56 pointer px on this host.
        assertFalse(RETINA.isMeaningfulDrag(Offset(400f, 400f), Offset(405f, 405f)))
        assertTrue(RETINA.isMeaningfulDrag(Offset(400f, 400f), Offset(430f, 430f)))
        assertEquals(8, MIN_DRAG_DEVICE_PX)
    }
}
