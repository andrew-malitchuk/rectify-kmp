package presentation.feature.mirror.core.verify

import kotlin.math.abs

/**
 * Fraction of [before]'s pixels that visibly changed in [after], in `0f..1f`.
 *
 * Per-pixel **Chebyshev distance in RGB**: a pixel counts when
 * `max(|Δr|, |Δg|, |Δb|) > `[CHANNEL_NOISE_FLOOR]. Per-channel max rather than luma on
 * purpose — a chroma-only recolour at equal luma (an error-red label turned grey) is exactly
 * the kind of fix this feature has to see, and a luma metric would miss it entirely.
 *
 * **Alpha is ignored.** Both arrays come from opaque screen captures, and a readback that fills
 * alpha differently between two frames must not read as a change.
 *
 * Integer-only, one pass, allocation-free: regions are up to a full frame (a few million
 * pixels) and this runs per annotation.
 *
 * Takes `IntArray`s and no Compose type at all, deliberately — the `ImageBitmap` readback lives
 * alone in `RegionPixels.kt`, so the metric itself stays unit-testable with no graphics backend.
 *
 * @param before ARGB pixels of the region, as laid out by [regionPixels].
 * @param after The same region of the later frame; must be the same length.
 * @return `0f` for identical input, for empty input, and for a length mismatch — the latter is
 *   not a measurement, and "no pixel evidence" is the safe direction (it can only withhold a
 *   `Fixed`, never invent one).
 */
internal fun changedRatio(before: IntArray, after: IntArray): Float {
    if (before.isEmpty() || before.size != after.size) return 0f
    var changed = 0
    for (index in before.indices) {
        val b = before[index]
        val a = after[index]
        val red = abs(((b shr 16) and 0xFF) - ((a shr 16) and 0xFF))
        val green = abs(((b shr 8) and 0xFF) - ((a shr 8) and 0xFF))
        val blue = abs((b and 0xFF) - (a and 0xFF))
        if (maxOf(red, green, blue) > CHANNEL_NOISE_FLOOR) changed++
    }
    return changed.toFloat() / before.size
}
