package presentation.core.ui.source.kit.atom.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Tag geometry — small enough to sit on a mark's edge without covering the content under it. */
private val TagRadius: Dp = 4.dp
private val TagPaddingHorizontal: Dp = 5.dp
private val TagPaddingVertical: Dp = 2.dp

/**
 * A measurement tag — the small signal-filled chip that labels a mark's size, a ruler extent,
 * or a hierarchy delta: `545 × 254`, `272`, `Δ6dp`.
 *
 * The label is always white, in **both** themes: this chip only ever sits on a signal fill
 * (red / green / amber / blue), where the style guide fixes the text to white. That is why the
 * color is a literal here rather than `Theme.color.onPrimary`, which inverts to near-black in
 * dark mode and would be unreadable on red.
 *
 * Formatting is the caller's job and the style guide is strict about it: `×` (U+00D7) with no
 * spaces in tight meta, `Δ` with no space before the unit, size before `@ x,y`.
 *
 * @param text The measurement to display, already formatted.
 * @param modifier Modifier applied to the root composable.
 * @param containerColor Signal fill behind the label; defaults to the annotation red.
 */
@Composable
public fun MeasureTag(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Theme.color.annotate,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(TagRadius))
            .background(containerColor)
            .padding(horizontal = TagPaddingHorizontal, vertical = TagPaddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = Theme.typography.monoTag,
            // Deliberate literal — see the KDoc above.
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasureTagPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MeasureTag(text = "545 × 254")
            MeasureTag(text = "272")
            MeasureTag(text = "Δ6dp", containerColor = Theme.color.measure)
            MeasureTag(text = "fixed", containerColor = Theme.color.sentFixed)
        }
    }
}
