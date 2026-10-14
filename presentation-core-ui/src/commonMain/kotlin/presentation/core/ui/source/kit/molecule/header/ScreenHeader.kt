package presentation.core.ui.source.kit.molecule.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.ButtonSizeType
import presentation.core.ui.source.kit.atom.button.IconButton
import presentation.core.ui.source.kit.atom.divider.HorizontalAnimatedDivider
import presentation.core.ui.source.kit.atom.icon.ChevronLeft
import presentation.core.ui.source.kit.atom.icon.Settings

/**
 * Standard screen toolbar with a leading back button and an optional trailing slot.
 *
 * Used as the top navigation bar across all feature screens to ensure consistent
 * padding (`spacingM` horizontal, `spacingS` vertical) and button sizing.
 *
 * When [trailingContent] is provided the row uses [Arrangement.SpaceBetween]; without
 * it the back button sits at the leading edge.
 *
 * @param onBackClick Called when the user taps the back (chevron-left) button.
 * @param modifier [Modifier] applied to the root [Column].
 * @param showDivider When `true` an animated [HorizontalAnimatedDivider] is shown below
 *   the toolbar row, signalling that scrollable content has been scrolled behind the header.
 *   Typically driven by `scrollState.canScrollBackward` or `lazyListState.canScrollBackward`.
 * @param trailingContent Optional composable rendered on the trailing side (e.g. an
 *   [IconButton] or a text action).
 *
 * @see NavigationHeader
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun ScreenHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Theme.spacing.spacingS,
                    vertical = Theme.spacing.spacingS,
                ),
            horizontalArrangement = if (trailingContent != null) Arrangement.SpaceBetween else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                icon = ChevronLeft,
                onClick = onBackClick,
                size = ButtonSizeType.Medium,
            )
            trailingContent?.invoke()
        }
        HorizontalAnimatedDivider(isVisible = showDivider)
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderBackOnlyPreview() {
    AppTheme {
        ScreenHeader(onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderWithTrailingPreview() {
    AppTheme {
        ScreenHeader(
            onBackClick = {},
            trailingContent = {
                IconButton(
                    icon = Settings,
                    onClick = {},
                    size = ButtonSizeType.Medium,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderWithDividerPreview() {
    AppTheme {
        ScreenHeader(
            onBackClick = {},
            showDivider = true,
        )
    }
}
