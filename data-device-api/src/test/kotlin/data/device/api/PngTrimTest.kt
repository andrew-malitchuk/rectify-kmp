package data.device.api

import data.device.api.source.parse.trimToPngSignature
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

/** Pins the foldable warning quirk and the no signature case that must not throw. */
class PngTrimTest {

    private val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val png = signature + byteArrayOf(1, 2, 3)

    @Test
    fun `trimToPngSignature returns a clean buffer unchanged`() {
        assertContentEquals(png, trimToPngSignature(png))
    }

    @Test
    fun `trimToPngSignature drops the warning line a foldable prepends`() {
        val noisy = "WARNING multiple displays detected\n".encodeToByteArray() + png
        assertContentEquals(png, trimToPngSignature(noisy))
    }

    @Test
    fun `trimToPngSignature returns null for a buffer with no signature`() {
        assertNull(trimToPngSignature(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)))
    }

    @Test
    fun `trimToPngSignature returns null for an empty buffer`() {
        assertNull(trimToPngSignature(ByteArray(0)))
    }

    @Test
    fun `trimToPngSignature returns null for a truncated signature`() {
        assertNull(trimToPngSignature(signature.copyOfRange(0, 5)))
    }
}
