package presentation.core.ui.source.kit.molecule.annotation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Popover geometry. */
private val PopoverRadius: Dp = 10.dp
private val IndexBadgeSize: Dp = 20.dp
private val IndexBadgeRadius: Dp = 5.dp
private val PopoverMaxWidth: Dp = 280.dp

/**
 * The gutter popover that carries an annotation's comment away from a crowded frame.
 *
 * Always dark, in both themes — it floats over mirrored device content whose brightness is
 * unknown, and a theme-following surface would disappear against a light screenshot. Its text
 * is therefore a literal white rather than an ink token.
 *
 * Placement and leader-line routing belong to the host: when marks crowd each other the style
 * guide staggers the elbows by 8 px, which needs the full set of mark positions this component
 * deliberately does not know about.
 *
 * @param text The annotation comment.
 * @param modifier Modifier applied to the root composable.
 * @param index Optional mark index shown in the leading badge; `null` hides it.
 * @param indexColor Fill of the index badge; defaults to the annotation red.
 */
@Composable
public fun AnnotationPopover(
    text: String,
    modifier: Modifier = Modifier,
    index: Int? = null,
    indexColor: Color = Theme.color.annotate,
) {
    Row(
        modifier = modifier
            .widthIn(max = PopoverMaxWidth)
            .clip(RoundedCornerShape(PopoverRadius))
            .background(Theme.color.pop)
            .padding(horizontal = Theme.spacing.spacingS, vertical = Theme.spacing.spacingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        if (index != null) {
            Box(
                modifier = Modifier
                    .size(IndexBadgeSize)
                    .clip(RoundedCornerShape(IndexBadgeRadius))
                    .background(indexColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    style = Theme.typography.monoBadge,
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = text,
            style = Theme.typography.body,
            // The popover is always dark, so its copy is always white — see the KDoc.
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnotationPopoverPreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.bg).padding(Theme.spacing.spacingL)) {
            AnnotationPopover(text = "Price sits 6px below the title", index = 1)
        }
    }
}
