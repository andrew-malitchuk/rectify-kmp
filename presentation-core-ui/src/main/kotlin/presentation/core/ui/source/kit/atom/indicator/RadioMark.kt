package presentation.core.ui.source.kit.atom.indicator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Mark geometry. Matches the 18 dp control the comps draw in rows and platform cards. */
private val RadioMarkSize: Dp = 18.dp

/**
 * The single-choice selection mark — a filled dot when picked, an empty ring otherwise.
 *
 * Drawn rather than composed from Material's `RadioButton`: the comps want a flat ink dot with
 * no ripple, no 48 dp touch target of its own and no state layer, because the whole row (or
 * card) around it is the hit area. Purely presentational — it handles no input and carries no
 * semantics; the clickable parent owns [androidx.compose.ui.semantics.Role.RadioButton] and the
 * selected state announcement.
 *
 * @param selected Whether this option is the active one.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun RadioMark(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val ring = Theme.color.line2
    val fill = Theme.color.ink
    Canvas(modifier = modifier.size(RadioMarkSize)) {
        val radius = size.minDimension / 2f
        val stroke = radius * 0.14f
        drawCircle(color = ring, radius = radius - stroke / 2f, style = Stroke(width = stroke))
        if (selected) {
            drawCircle(color = fill, radius = radius - stroke / 2f, style = Stroke(width = stroke * 2.4f))
            drawCircle(color = fill, radius = radius * 0.42f)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RadioMarkPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.surface)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            RadioMark(selected = true)
            RadioMark(selected = false)
        }
    }
}
