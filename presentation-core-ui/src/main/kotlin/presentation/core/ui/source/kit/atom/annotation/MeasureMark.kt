package presentation.core.ui.source.kit.atom.annotation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import presentation.core.styling.source.theme.AppTheme

/** Mark geometry. */
private val MarkRadius: Dp = 2.dp

/** Dash pattern of a neighbour's outline. */
private val DashIntervals = floatArrayOf(4f, 3f)

/**
 * Role of a [MeasureMark] in a measurement, driving its outline treatment.
 */
public enum class MeasureMarkRole {
    /** The node being measured — solid outline. */
    Target,

    /** A node the target is measured against — dashed outline. */
    Neighbour,
}

/**
 * A rectangular measure mark drawn over the mirrored device frame.
 *
 * Blue is reserved for measurement, the way red is reserved for annotation — a measure mark is
 * never red, and an [AnnotationMark] is never blue. Unlike an annotation mark this one carries
 * no wash: a measurement describes the geometry of UI that has to stay readable underneath it.
 *
 * Gap labels between marks are [MeasureTag][presentation.core.ui.source.kit.atom.tag.MeasureTag]s,
 * and the connector lines that carry them are the host's to route — like a leader line, that
 * needs every mark's geometry at once, which this component deliberately does not know.
 *
 * @param modifier Modifier applied to the mark; supplies its position and size.
 * @param role Whether this is the measured node or one of its neighbours.
 */
@Composable
public fun MeasureMark(
    modifier: Modifier = Modifier,
    role: MeasureMarkRole = MeasureMarkRole.Target,
) {
    val accent = Theme.color.measure
    val density = LocalDensity.current
    val strokeWidth = with(density) { Theme.stroke.thin.toPx() }
    val cornerPx = with(density) { MarkRadius.toPx() }
    val dashed = role == MeasureMarkRole.Neighbour

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = strokeWidth / 2f
            drawRoundRect(
                color = accent,
                topLeft = Offset(inset, inset),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = if (dashed) PathEffect.dashPathEffect(DashIntervals, 0f) else null,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasureMarkPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingXL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXL),
        ) {
            MeasureMark(modifier = Modifier.width(120.dp).height(40.dp))
            MeasureMark(
                modifier = Modifier.width(120.dp).height(40.dp),
                role = MeasureMarkRole.Neighbour,
            )
        }
    }
}
