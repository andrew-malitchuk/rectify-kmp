package presentation.core.ui.source.kit.atom.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/** Badge geometry. */
private val BadgeSize: Dp = 20.dp
private val BadgeRadius: Dp = 5.dp

/**
 * The small square badge that marks an item — an annotation index, a `✓`, a `!`.
 *
 * Always sits on a signal fill, so its glyph is white in **both** themes; passing an
 * unsaturated [containerColor] will produce unreadable text.
 *
 * @param text Short glyph — an index, `✓`, or `!`.
 * @param containerColor Signal fill behind the glyph.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun GlyphBadge(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(BadgeSize)
            .clip(RoundedCornerShape(BadgeRadius))
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = Theme.typography.monoBadge,
            // On a signal fill — white in both themes; see MeasureTag's KDoc for why this is
            // a literal rather than an ink token.
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GlyphBadgePreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            GlyphBadge(text = "1", containerColor = Theme.color.annotate)
            GlyphBadge(text = "✓", containerColor = Theme.color.sentFixed)
            GlyphBadge(text = "!", containerColor = Theme.color.warnStillOff)
        }
    }
}
