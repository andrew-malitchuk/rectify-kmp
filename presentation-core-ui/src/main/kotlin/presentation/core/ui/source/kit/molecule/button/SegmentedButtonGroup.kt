package presentation.core.ui.source.kit.molecule.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Track geometry. The inner radius is the outer minus the track inset, so the corners nest. */
private val TrackHeight: Dp = 30.dp
private val TrackRadius: Dp = 8.dp
private val TrackInset: Dp = 2.dp
private val SegmentRadius: Dp = 6.dp

/**
 * A single-choice segmented control — the toolbar's platform switch (`Android | iOS`).
 *
 * The selected segment is a raised surface chip inside a recessed track, rather than an
 * ink-filled block: this control sits in a toolbar beside primary actions, and a solid ink
 * fill here would read as the page's call to action.
 *
 * @param items Segment labels.
 * @param selectedIndex Index of the currently selected segment.
 * @param onSelect Callback with the tapped segment index.
 * @param modifier Modifier applied to the track.
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun SegmentedButtonGroup(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(TrackHeight)
            .clip(RoundedCornerShape(TrackRadius))
            .background(Theme.color.seg)
            .padding(TrackInset),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrackInset),
    ) {
        items.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val shape = RoundedCornerShape(SegmentRadius)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(shape)
                    .background(if (isSelected) Theme.color.surface else Color.Transparent)
                    .then(
                        if (isSelected) {
                            Modifier.border(Theme.stroke.thin, Theme.color.line, shape)
                        } else {
                            Modifier
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onSelect(index) },
                    )
                    .padding(horizontal = Theme.spacing.spacingL - Theme.spacing.spacingXS),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = if (isSelected) Theme.typography.bodyStrong else Theme.typography.body,
                    color = if (isSelected) Theme.color.ink else Theme.color.ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedButtonGroupPreview() {
    AppTheme {
        SegmentedButtonGroup(
            items = listOf("Android", "iOS"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
        )
    }
}
