package data.device.api.source.parse

private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

/**
 * Drops anything preceding the PNG signature in a screen-capture buffer.
 *
 * Some devices (foldables with more than one display) prepend a warning line to
 * `screencap` stdout. Returns `null` instead of throwing when there is no signature at
 * all, so a disconnected device degrades to a dropped frame rather than a crash.
 *
 * @return The buffer from the signature onward, or `null` when no signature is present.
 */
public fun trimToPngSignature(raw: ByteArray): ByteArray? {
    val start = (0..raw.size - PNG_SIGNATURE.size).firstOrNull { i ->
        PNG_SIGNATURE.indices.all { j -> raw[i + j] == PNG_SIGNATURE[j] }
    } ?: return null
    return raw.copyOfRange(start, raw.size)
}
