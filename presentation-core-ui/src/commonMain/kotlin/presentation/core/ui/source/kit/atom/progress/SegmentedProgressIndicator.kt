package presentation.core.ui.source.kit.atom.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Number of segments the track is divided into. Reads as an instrument, not a smooth bar. */
private const val SEGMENT_COUNT = 24

/** Gap between segments. */
private val SegmentGap: Dp = 2.dp

/** Default bar height. */
private val BarHeight: Dp = 6.dp

/** Corner radius of a single segment. */
private val SegmentCorner: Dp = 1.dp

/**
 * A segmented progress bar — the RECTIFY replacement for v1's animated wavy progress.
 *
 * Progress is quantised into [SEGMENT_COUNT] discrete blocks rather than drawn as a
 * continuous fill. This is deliberate: a segmented readout makes small changes legible at a
 * glance, and it removes the perpetual animation that a wavy indicator would run even when
 * nothing was happening.
 *
 * The bar carries no color of its own beyond theme ink. Pass [activeColor] only when the
 * value being reported genuinely has a status — a failing upload, for instance.
 *
 * ```kotlin
 * SegmentedProgressIndicator(progress = upload.progress)
 * ```
 *
 * @param progress Current progress, clamped internally to the `0f..1f` range.
 * @param modifier [Modifier] applied to the underlying [Canvas].
 * @param activeColor Color of filled segments. Defaults to primary ink.
 * @param inactiveColor Color of empty segments. Defaults to the tile outline.
 * @param height Bar height.
 */
@Composable
public fun SegmentedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = Theme.color.ink,
    inactiveColor: Color = Theme.color.line,
    height: Dp = BarHeight,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val filledSegments = (clampedProgress * SEGMENT_COUNT).roundToInt()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val gapPx = SegmentGap.toPx()
        val totalGap = gapPx * (SEGMENT_COUNT - 1)
        val segmentWidth = ((size.width - totalGap) / SEGMENT_COUNT).coerceAtLeast(1f)
        val corner = CornerRadius(SegmentCorner.toPx())

        repeat(SEGMENT_COUNT) { index ->
            drawRoundRect(
                color = if (index < filledSegments) activeColor else inactiveColor,
                topLeft = Offset(index * (segmentWidth + gapPx), 0f),
                size = Size(segmentWidth, size.height),
                cornerRadius = corner,
            )
        }
    }
}

/**
 * A bar chart of recent values — the trend counterpart to [SegmentedProgressIndicator].
 *
 * Each entry in [values] is drawn as a vertical bar scaled against the largest value in the
 * series, so the chart always uses its full height. One bar may be singled out with
 * [highlightIndex] — a peak, or the sample the user has selected.
 *
 * @param values Series to plot, in display order. An empty series draws nothing.
 * @param modifier [Modifier] applied to the underlying [Canvas].
 * @param highlightIndex Index drawn in [highlightColor]; `-1` highlights nothing.
 * @param barColor Color of ordinary bars.
 * @param highlightColor Color of the highlighted bar.
 * @param height Chart height.
 */
@Composable
public fun SegmentedBars(
    values: List<Float>,
    modifier: Modifier = Modifier,
    highlightIndex: Int = -1,
    barColor: Color = Theme.color.ink3,
    highlightColor: Color = Theme.color.primary,
    height: Dp = 56.dp,
) {
    if (values.isEmpty()) return
    val maxValue = values.max().takeIf { it > 0f } ?: 1f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val gapPx = SegmentGap.toPx()
        val totalGap = gapPx * (values.size - 1)
        val barWidth = ((size.width - totalGap) / values.size).coerceAtLeast(1f)
        val corner = CornerRadius(SegmentCorner.toPx() * 2)

        values.forEachIndexed { index, value ->
            val barHeight = ceil(size.height * (value / maxValue)).coerceAtLeast(1f)
            drawRoundRect(
                color = if (index == highlightIndex) highlightColor else barColor,
                topLeft = Offset(index * (barWidth + gapPx), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = corner,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedProgressIndicatorPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
        ) {
            SegmentedProgressIndicator(progress = 0.34f)
            SegmentedProgressIndicator(progress = 0.78f, activeColor = Theme.color.primary)
            SegmentedProgressIndicator(progress = 1f)
            SegmentedBars(
                values = listOf(34f, 52f, 70f, 88f, 100f, 74f, 58f, 42f, 66f, 80f),
                highlightIndex = 4,
            )
        }
    }
}
