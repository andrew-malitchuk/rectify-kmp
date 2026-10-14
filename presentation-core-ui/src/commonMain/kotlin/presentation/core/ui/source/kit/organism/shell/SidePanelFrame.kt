package presentation.core.ui.source.kit.organism.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.molecule.bar.tab.SidebarTabItem
import presentation.core.ui.source.kit.molecule.bar.tab.SidebarTabs

/**
 * Panel header geometry.
 *
 * **Open question — no comp exists for this panel.** The header height is derived from
 * `SidebarTabs`' own 26 dp tab plus breathing room; flagged for design review.
 */
private val PanelHeaderHeight: Dp = 40.dp

/** Preview-only panel width. */
private val PreviewPanelWidth: Dp = 320.dp
private val PreviewPanelHeight: Dp = 240.dp

/**
 * The side panel's chrome: a leading hairline, the tab header, and the active tab's content.
 *
 * Renders the three shell tabs straight from [ShellTab] entries through the shipped
 * `SidebarTabs` molecule, so the tab set cannot drift from the enum the shortcuts
 * (`shortcutToTab`) and the state holder already agree on.
 *
 * Holds nothing: [activeTab] comes in and [onTabSelect] goes out, both owned by `ShellState`.
 * Tab *content* is entirely the caller's — this frame never knows what a note is.
 *
 * @param activeTab Tab currently shown.
 * @param onTabSelect Called with the tab the user picked.
 * @param modifier Modifier applied to the root composable.
 * @param tabCounts Optional per-tab item counts rendered next to the tab labels.
 * @param content Content of the active tab.
 */
@Composable
public fun SidePanelFrame(
    activeTab: ShellTab,
    onTabSelect: (ShellTab) -> Unit,
    modifier: Modifier = Modifier,
    tabCounts: Map<ShellTab, Int> = emptyMap(),
    content: @Composable ColumnScope.(ShellTab) -> Unit,
) {
    // No leading hairline here: when the panel is shown, PanelResizeHandle already draws the
    // divider on this exact edge. Painting a second one stacked `line` against `line2` and read
    // as a doubled rule. The handle owns the seam.
    Row(modifier = modifier.background(Theme.color.surface)) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PanelHeaderHeight)
                    .padding(horizontal = Theme.spacing.spacingS),
                contentAlignment = Alignment.CenterStart,
            ) {
                SidebarTabs(
                    items = ShellTab.entries.map { tab ->
                        SidebarTabItem(label = tab.label, count = tabCounts[tab])
                    },
                    selectedIndex = activeTab.ordinal,
                    onSelect = { index -> onTabSelect(ShellTab.entries[index]) },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Theme.stroke.thin)
                    .background(Theme.color.line),
            )
            content(activeTab)
        }
    }
}

@Composable
private fun SidePanelFramePreviewContent() {
    SidePanelFrame(
        activeTab = ShellTab.Notes,
        onTabSelect = {},
        modifier = Modifier.width(PreviewPanelWidth).height(PreviewPanelHeight),
    ) { tab ->
        Text(
            text = tab.label,
            style = Theme.typography.secondary,
            color = Theme.color.ink2,
            modifier = Modifier.padding(Theme.spacing.spacingL),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SidePanelFramePreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        SidePanelFramePreviewContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun SidePanelFramePreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        SidePanelFramePreviewContent()
    }
}
