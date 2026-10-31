package presentation.core.ui.source.kit.molecule.annotation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.tag.MeasureTag

/** Ruler geometry. */
private val RulerHeight: Dp = 22.dp
private val MajorTickHeight: Dp = 7.dp
private val MinorTickHeight: Dp = 4.dp

/**
 * A horizontal ruler strip measuring the mirrored frame in **device pixels**, with the current
 * selection's extent picked out in red.
 *
 * Numerals use the ruler type token — the smallest in the system, and the only role allowed to
 * go that small, because a ruler is scanned rather than read.
 *
 * Only the horizontal orientation exists: the vertical ruler in the reference is the same strip
 * rotated by the host, and duplicating the tick maths for it would buy nothing.
 *
 * @param totalPx Width of the frame in device pixels — the value at the right edge.
 * @param modifier Modifier applied to the root composable.
 * @param extentPx Selection extent in device pixels, highlighted in red; `null` draws a plain ruler.
 * @param majorTickEvery Device-pixel interval between labelled major ticks.
 * @param minorTicksPerMajor Minor ticks drawn between two major ticks.
 */
@Composable
public fun Ruler(
    totalPx: Int,
    modifier: Modifier = Modifier,
    extentPx: IntRange? = null,
    majorTickEvery: Int = 500,
    minorTicksPerMajor: Int = 4,
) {
    val lineColor = Theme.color.line2
    val accent = Theme.color.annotate
    val density = LocalDensity.current
    val majorPx = with(density) { MajorTickHeight.toPx() }
    val minorPx = with(density) { MinorTickHeight.toPx() }
    val strokePx = with(density) { Theme.stroke.thin.toPx() }
    val accentStrokePx = with(density) { Theme.stroke.regular.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(RulerHeight)
            .background(Theme.color.chrome),
    ) {
        val widthDp = maxWidth

        Canvas(modifier = Modifier.fillMaxWidth().height(RulerHeight)) {
            if (totalPx <= 0) return@Canvas
            val scale = size.width / totalPx.toFloat()
            val baseline = size.height

            // Minor ticks first so major ticks draw over them at shared positions.
            val minorEvery = majorTickEvery / (minorTicksPerMajor + 1).coerceAtLeast(1)
            if (minorEvery > 0) {
                var px = 0
                while (px <= totalPx) {
                    if (px % majorTickEvery != 0) {
                        drawTick(px * scale, baseline, minorPx, lineColor, strokePx)
                    }
                    px += minorEvery
                }
            }
            var px = 0
            while (px <= totalPx) {
                drawTick(px * scale, baseline, majorPx, lineColor, strokePx)
                px += majorTickEvery
            }

            // The extent rides the baseline so it reads as a span, not another tick.
            if (extentPx != null) {
                val start = extentPx.first.coerceIn(0, totalPx) * scale
                val end = extentPx.last.coerceIn(0, totalPx) * scale
                drawLine(
                    color = accent,
                    start = Offset(start, baseline - accentStrokePx / 2f),
                    end = Offset(end, baseline - accentStrokePx / 2f),
                    strokeWidth = accentStrokePx,
                )
            }
        }

        // Major-tick numerals.
        if (totalPx > 0) {
            var px = 0
            while (px <= totalPx) {
                val fraction = px / totalPx.toFloat()
                androidx.compose.material3.Text(
                    text = px.toString(),
                    style = Theme.typography.monoRuler,
                    color = Theme.color.ink3,
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = widthDp * fraction + 2.dp, y = 1.dp),
                )
                px += majorTickEvery
            }
        }

        // Extent label, pinned to the start of the span.
        if (extentPx != null && totalPx > 0) {
            val fraction = extentPx.first.coerceIn(0, totalPx) / totalPx.toFloat()
            MeasureTag(
                text = (extentPx.last - extentPx.first).toString(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = widthDp * fraction, y = 0.dp),
            )
        }
    }
}

private fun DrawScope.drawTick(
    x: Float,
    baseline: Float,
    tickHeight: Float,
    color: Color,
    strokeWidth: Float,
) {
    drawLine(
        color = color,
        start = Offset(x, baseline - tickHeight),
        end = Offset(x, baseline),
        strokeWidth = strokeWidth,
    )
}

@Preview(showBackground = true)
@Composable
private fun RulerPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
        ) {
            Ruler(totalPx = 1344, extentPx = 272..800)
        }
    }
}
