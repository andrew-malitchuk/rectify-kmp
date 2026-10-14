package presentation.core.ui.source.kit.molecule.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.chip.Chip

/**
 * A horizontally-wrapping group of selectable [Chip]s where only one can be active.
 *
 * This is classified as a **molecule** because it composes multiple [Chip] atoms
 * into a coordinated group with shared single-selection state.
 *
 * @param items List of chip labels.
 * @param selectedIndex Index of the currently selected chip.
 * @param onSelect Callback with the tapped chip index.
 * @param modifier Modifier to be applied to the [FlowRow].
 *
 * @see Chip
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun ChipGroup(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        // No vertical gap: each chip already reserves padding around itself to keep its touch
        // target accessible, and adding more here double-spaces the wrapped rows.
        verticalArrangement = Arrangement.Top,
    ) {
        items.forEachIndexed { index, label ->
            Chip(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipGroupPreview() {
    AppTheme {
        ChipGroup(
            items = listOf("SYSTEM", "SHELL", "PROMPT", "SHORTCUT", "MEDIA", "BRIGHTNESS"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
        )
    }
}
