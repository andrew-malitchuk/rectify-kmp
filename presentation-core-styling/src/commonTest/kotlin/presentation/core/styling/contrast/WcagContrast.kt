package presentation.core.styling.contrast

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * WCAG 2.x contrast maths — test-only, no dependency.
 *
 * Implements the sRGB linearisation (`c / 12.92` below the 0.04045 knee, `((c + 0.055) / 1.055)^2.4`
 * above it), the luminance weights `0.2126 R + 0.7152 G + 0.0722 B`, and the ratio
 * `(Lmax + 0.05) / (Lmin + 0.05)`.
 *
 * There is deliberately no `compositeOver` here: Compose already ships
 * [androidx.compose.ui.graphics.compositeOver], which is the exact code path that puts the
 * palette's rgba overlay tokens on screen at runtime. Re-deriving it would risk measuring
 * something the renderer never draws.
 */
internal fun relativeLuminance(color: Color): Double =
    0.2126 * linearise(color.red) + 0.7152 * linearise(color.green) + 0.0722 * linearise(color.blue)

/** Contrast ratio between two opaque colours; order-independent, in `[1.0, 21.0]`. */
internal fun contrastRatio(a: Color, b: Color): Double {
    val first = relativeLuminance(a)
    val second = relativeLuminance(b)
    return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
}

private fun linearise(channel: Float): Double {
    val c = channel.toDouble()
    return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}
