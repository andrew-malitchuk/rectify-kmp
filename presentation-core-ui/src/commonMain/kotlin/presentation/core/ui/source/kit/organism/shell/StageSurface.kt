package presentation.core.ui.source.kit.organism.shell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme

/**
 * Dot-grid texture geometry — measured from the design's stage background, which is
 * `radial-gradient(var(--stageDot) 1px, transparent 1px)` on a `16px 16px` cell.
 *
 * The colour is *not* here: it is `Theme.color.stageDot`, because the two themes need different
 * alphas to read as the same weight. Tinting [Theme.color.ink3] instead — which this did — came
 * out roughly three times too dark in both themes.
 */
private val DotCellSize: Dp = 16.dp
private val DotRadius: Dp = 0.5.dp

/** Preview-only phone-outline geometry (see [StageSurfaceEmptyPreviewContent]). */
private val PreviewOutlineWidth: Dp = 180.dp
private val PreviewOutlineHeight: Dp = 380.dp
private val PreviewOutlineRadius: Dp = 28.dp
private val PreviewDashOn: Dp = 6.dp
private val PreviewDashOff: Dp = 5.dp

/**
 * The mirror region — the bezel-less ground the device stream is painted onto (FR-14).
 *
 * Deliberately *not* a card: no border, no elevation, no corner radius. The only decoration is
 * a faint dot grid, which gives the empty stage a sense of depth and, once mirroring starts,
 * marks where the frame ends without drawing a frame.
 *
 * Stage content — the mirrored frame, or whatever empty-state copy the caller wants — comes in
 * through [content] and composes on top of the texture.
 *
 * @param modifier Modifier applied to the root composable.
 * @param content Stage content, drawn above the dot grid.
 */
@Composable
public fun StageSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val dotColor = Theme.color.stageDot
    val density = LocalDensity.current
    // Stepped once per composition, not per dot: the draw loop only adds floats.
    val cellPx = with(density) { DotCellSize.toPx() }
    val dotRadiusPx = with(density) { DotRadius.toPx() }

    Box(
        modifier = modifier.background(Theme.color.stage),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var y = cellPx / 2f
            while (y < size.height) {
                var x = cellPx / 2f
                while (x < size.width) {
                    // Offset is a value class — no allocation per dot.
                    drawCircle(color = dotColor, radius = dotRadiusPx, center = Offset(x, y))
                    x += cellPx
                }
                y += cellPx
            }
        }
        content()
    }
}

/**
 * The empty stage from comp 02 — a dashed phone outline with a centred `NO SIGNAL`.
 *
 * Preview-only on purpose: the empty state is stage *content*, owned by whoever fills the
 * slot, so shipping it as a component here would pre-empt a decision this spec does not make.
 */
@Composable
private fun StageSurfaceEmptyPreviewContent() {
    val outlineColor = Theme.color.ink3.copy(alpha = 0.9f)
    val strokeWidth = Theme.stroke.thin
    Box(
        modifier = Modifier
            .width(PreviewOutlineWidth)
            .height(PreviewOutlineHeight),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = strokeWidth.toPx() / 2f
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(PreviewOutlineRadius.toPx()),
                style = Stroke(
                    width = strokeWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(PreviewDashOn.toPx(), PreviewDashOff.toPx()),
                    ),
                ),
            )
        }
        Text(
            text = "NO SIGNAL",
            style = Theme.typography.monoMeta,
            color = Theme.color.ink3,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StageSurfacePreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        StageSurface(modifier = Modifier.fillMaxSize().padding(Theme.spacing.spacingL)) {
            StageSurfaceEmptyPreviewContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StageSurfacePreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        StageSurface(modifier = Modifier.fillMaxSize().padding(Theme.spacing.spacingL)) {
            StageSurfaceEmptyPreviewContent()
        }
    }
}
