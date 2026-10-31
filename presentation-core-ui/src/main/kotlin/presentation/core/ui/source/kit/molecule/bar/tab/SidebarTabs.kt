package presentation.core.ui.source.kit.molecule.bar.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Tab geometry. */
private val TabHeight: Dp = 26.dp
private val TabRadius: Dp = 6.dp

/**
 * A single tab inside [SidebarTabs].
 *
 * @param label Tab name.
 * @param count Optional item count rendered after the label; `null` hides it.
 */
public data class SidebarTabItem(
    val label: String,
    val count: Int? = null,
)

/**
 * The sidebar's tab row — `Annotations 3 · Payload · History 1`.
 *
 * Selection is a filled chip rather than an underline: this row sits inside a panel, where an
 * underline would compete with the panel's own dividers. Distinct from
 * [TabBar][presentation.core.ui.source.kit.molecule.bar.tab.TabBar], which is the icon-only
 * bottom bar for top-level navigation.
 *
 * @param items Ordered tabs to render.
 * @param selectedIndex Zero-based index of the active tab.
 * @param onSelect Called with the index of the tapped tab.
 * @param modifier Modifier applied to the root [Row].
 */
@Composable
public fun SidebarTabs(
    items: List<SidebarTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val shape = RoundedCornerShape(TabRadius)
            Row(
                modifier = Modifier
                    .height(TabHeight)
                    .clip(shape)
                    .background(if (isSelected) Theme.color.seg else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onSelect(index) },
                    )
                    .padding(horizontal = Theme.spacing.spacingS + Theme.spacing.spacingXS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS + 2.dp),
            ) {
                Text(
                    text = item.label,
                    style = if (isSelected) Theme.typography.bodyStrong else Theme.typography.body,
                    color = if (isSelected) Theme.color.ink else Theme.color.ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.count != null) {
                    Text(
                        text = item.count.toString(),
                        style = Theme.typography.monoMeta,
                        color = Theme.color.ink3,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SidebarTabsPreview() {
    AppTheme {
        SidebarTabs(
            items = listOf(
                SidebarTabItem(label = "Annotations", count = 3),
                SidebarTabItem(label = "Payload"),
                SidebarTabItem(label = "History", count = 1),
            ),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
        )
    }
}
