package presentation.core.ui.source.kit.atom.annotation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.GlyphBadge
import presentation.core.ui.source.kit.atom.tag.MeasureTag

/** Mark geometry. */
private val MarkRadius: Dp = 3.dp
private val HandleRadius: Dp = 4.5.dp
private val BadgeSize: Dp = 20.dp
private val BadgeRadius: Dp = 5.dp

/** Opacity of the translucent wash inside a mark, over the mirrored device content. */
private const val FILL_ALPHA = 0.10f

/** Dash pattern of a mark still being drawn. */
private val DashIntervals = floatArrayOf(6f, 4f)

/**
 * Lifecycle state of an [AnnotationMark], driving its color and border treatment.
 *
 * Red is reserved for annotations — a mark is never red for any other reason.
 */
public enum class AnnotationState {
    /** Being drawn right now — dashed border, size tag follows the pointer. */
    Drawing,

    /** Committed and waiting to be sent. */
    Pinned,

    /** Picked up for editing — shows resize handles. */
    Selected,

    /** Frozen: the stream is paused, so the mark sits on an opaque plate instead of a wash. */
    Frozen,

    /** Sent and verified fixed. */
    Sent,

    /** Verified, but the problem is still there. */
    StillOff,
}

/**
 * A rectangular annotation mark drawn over the mirrored device frame.
 *
 * Purely presentational and fully driven by its parameters: hit-testing, dragging, resizing and
 * coordinate mapping belong to the host that owns the device frame, not to the kit. Size the
 * mark with [modifier] — it fills whatever box it is given.
 *
 * [AnnotationState.Frozen] renders an opaque plate rather than a translucent wash, because the
 * host dims the rest of the frame behind it; the dimming itself is the host's to draw.
 *
 * @param modifier Modifier applied to the mark; supplies its position and size.
 * @param state Lifecycle state driving color and border treatment.
 * @param badge Short glyph shown in the corner badge — an index, `✓`, or `!`; `null` hides it.
 * @param sizeLabel Measurement shown on the bottom edge, e.g. `545 × 254`; `null` hides it.
 * @param showHandles Whether to draw the four corner resize handles.
 */
@Composable
public fun AnnotationMark(
    modifier: Modifier = Modifier,
    state: AnnotationState = AnnotationState.Pinned,
    badge: String? = null,
    sizeLabel: String? = null,
    showHandles: Boolean = false,
) {
    val accent = state.accentColor()
    val isFrozen = state == AnnotationState.Frozen
    val fill = if (isFrozen) Theme.color.surface else accent.copy(alpha = FILL_ALPHA)
    val strokeWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
        Theme.stroke.regular.toPx()
    }
    val handleRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        HandleRadius.toPx()
    }
    val cornerPx = with(androidx.compose.ui.platform.LocalDensity.current) { MarkRadius.toPx() }
    val dashed = state == AnnotationState.Drawing

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMarkBody(
                fill = fill,
                accent = accent,
                strokeWidth = strokeWidth,
                cornerPx = cornerPx,
                dashed = dashed,
            )
            if (showHandles) {
                drawHandles(accent = accent, radiusPx = handleRadiusPx, strokeWidth = strokeWidth)
            }
        }

        if (badge != null) {
            GlyphBadge(
                text = badge,
                containerColor = accent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    // Sits astride the top-left corner so it never covers the content it marks.
                    .offset(x = (-2).dp, y = -(BadgeSize + 4.dp)),
            )
        }

        if (sizeLabel != null) {
            MeasureTag(
                text = sizeLabel,
                containerColor = accent,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 10.dp),
            )
        }
    }
}

/** Draws the wash and the border. */
private fun DrawScope.drawMarkBody(
    fill: Color,
    accent: Color,
    strokeWidth: Float,
    cornerPx: Float,
    dashed: Boolean,
) {
    val inset = strokeWidth / 2f
    val corner = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx)
    drawRoundRect(color = fill, cornerRadius = corner)
    drawRoundRect(
        color = accent,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = corner,
        style = Stroke(
            width = strokeWidth,
            pathEffect = if (dashed) PathEffect.dashPathEffect(DashIntervals, 0f) else null,
        ),
    )
}

/** Draws the four corner resize handles. */
private fun DrawScope.drawHandles(accent: Color, radiusPx: Float, strokeWidth: Float) {
    listOf(
        Offset(0f, 0f),
        Offset(size.width, 0f),
        Offset(0f, size.height),
        Offset(size.width, size.height),
    ).forEach { corner ->
        drawCircle(color = Color.White, radius = radiusPx, center = corner)
        drawCircle(
            color = accent,
            radius = radiusPx,
            center = corner,
            style = Stroke(width = strokeWidth),
        )
    }
}

// The corner badge is GlyphBadge — the same badge the annotation and verification rows use, so
// a mark on the frame and its row in the sidebar stay identical.

@Composable
private fun AnnotationState.accentColor(): Color = when (this) {
    AnnotationState.Drawing,
    AnnotationState.Pinned,
    AnnotationState.Selected,
    AnnotationState.Frozen,
    -> Theme.color.annotate
    AnnotationState.Sent -> Theme.color.sentFixed
    AnnotationState.StillOff -> Theme.color.warnStillOff
}

@Preview(showBackground = true)
@Composable
private fun AnnotationMarkPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingXL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacing2XL),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacing2XL)) {
                AnnotationMark(
                    modifier = Modifier.width(140.dp).height(64.dp),
                    state = AnnotationState.Drawing,
                    sizeLabel = "545 × 254",
                )
                AnnotationMark(
                    modifier = Modifier.width(140.dp).height(64.dp),
                    state = AnnotationState.Selected,
                    badge = "2",
                    showHandles = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacing2XL)) {
                AnnotationMark(
                    modifier = Modifier.width(140.dp).height(64.dp),
                    state = AnnotationState.Sent,
                    badge = "✓",
                )
                AnnotationMark(
                    modifier = Modifier.width(140.dp).height(64.dp),
                    state = AnnotationState.StillOff,
                    badge = "!",
                )
            }
        }
    }
}
