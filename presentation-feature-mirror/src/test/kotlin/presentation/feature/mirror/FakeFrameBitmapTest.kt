package presentation.feature.mirror

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The fake frame is a prerequisite, not a convenience: while its `readPixels` was a no-op every
 * region read came back all-zero, so any pixel assertion built on it passed for a reason that
 * had nothing to do with the code under test (bugs.json#PP-TD-10).
 *
 * These cases fail if the offsets, the start coordinates or the stride are mishandled -- the
 * expected buffer below is written out position by position, including the untouched gaps.
 */
class FakeFrameBitmapTest {

    /** Encodes its own coordinates, so a misread position is visible in the failure message. */
    private val coordinate: (Int, Int) -> Int = { x, y -> x * 100 + y }

    @Test
    fun regionReadsHonourStartCoordinatesBufferOffsetAndStride() {
        val frame = FakeFrameBitmap(width = 10, height = 10, pixel = coordinate)
        val buffer = IntArray(20) { -1 }

        frame.readPixels(
            buffer = buffer,
            startX = 2,
            startY = 3,
            width = 3,
            height = 2,
            bufferOffset = 4,
            stride = 5,
        )

        assertContentEquals(
            intArrayOf(
                -1, -1, -1, -1, // before the offset
                203, 303, 403, // row y = 3, x = 2..4
                -1, -1, // the stride gap
                204, 304, 404, // row y = 4
                -1, -1, -1, -1, -1, -1, -1, -1, // past the region
            ),
            buffer,
        )
    }

    @Test
    fun aFullFrameReadFillsEveryPixelInRowMajorOrder() {
        val frame = FakeFrameBitmap(width = 3, height = 2, pixel = coordinate)
        val buffer = IntArray(6)

        frame.readPixels(buffer, startX = 0, startY = 0, width = 3, height = 2, bufferOffset = 0, stride = 3)

        assertContentEquals(intArrayOf(0, 100, 200, 1, 101, 201), buffer)
    }

    @Test
    fun twoFramesWithTheSamePixelFunctionReadBackIdentical() {
        val before = FakeFrameBitmap(width = 4, height = 4, pixel = coordinate)
        val after = FakeFrameBitmap(width = 4, height = 4, pixel = coordinate)
        val beforePixels = IntArray(4)
        val afterPixels = IntArray(4)

        before.readPixels(beforePixels, 1, 1, 2, 2, 0, 2)
        after.readPixels(afterPixels, 1, 1, 2, 2, 0, 2)

        assertContentEquals(beforePixels, afterPixels)
    }

    @Test
    fun aFrameCanBeMadeDeliberatelyDifferentFromAnother() {
        val before = FakeFrameBitmap(width = 4, height = 4, pixel = coordinate)
        val after = FakeFrameBitmap(width = 4, height = 4, pixel = { x, y -> coordinate(x, y) + 1 })
        val beforePixels = IntArray(4)
        val afterPixels = IntArray(4)

        before.readPixels(beforePixels, 0, 0, 2, 2, 0, 2)
        after.readPixels(afterPixels, 0, 0, 2, 2, 0, 2)

        assertTrue(beforePixels.indices.all { afterPixels[it] == beforePixels[it] + 1 })
    }

    @Test
    fun theDefaultContentIsOpaqueAndVariesWithBothAxes() {
        val frame = FakeFrameBitmap(width = 4, height = 4)
        val pixels = IntArray(4)

        frame.readPixels(pixels, 0, 0, 2, 2, 0, 2)

        assertEquals(4, pixels.toSet().size, "every coordinate must differ")
        assertTrue(pixels.all { (it ushr 24) and 0xFF == 0xFF }, "opaque alpha")
    }
}
