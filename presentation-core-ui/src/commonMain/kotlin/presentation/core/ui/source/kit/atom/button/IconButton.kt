package presentation.core.ui.source.kit.atom.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.core.ext.has
import presentation.core.ui.source.kit.atom.button.core.model.ButtonColor
import presentation.core.ui.source.kit.atom.button.core.model.ButtonInteractionState
import presentation.core.ui.source.kit.atom.button.core.state.StateIconButton
import presentation.core.ui.source.kit.atom.icon.Settings

/**
 * Icon-only button variant with selection and loading states.
 *
 * @param icon The vector icon displayed inside the button.
 * @param onClick Callback invoked when the button is tapped.
 * @param size Predefined size category controlling padding and icon dimensions.
 * @param modifier Modifier applied to the root composable.
 * @param enabled Whether the button accepts input.
 * @param isSelected Whether the button is in its selected visual state.
 * @param isLoading When `true`, replaces the icon with a loading indicator.
 *
 * @see <a href="https://www.figma.com/design/STUB_REPLACE_ME">Figma</a>
 */
@Composable
public fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: ButtonSizeType,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    isLoading: Boolean = false,
) {
    val sizeValues = size.resolveIcon()
    val colors = iconButtonColors()
    val corner = Theme.spacing.spacingS

    StateIconButton(
        onClick = onClick,
        icon = icon,
        colors = colors,
        sizes = sizeValues,
        corner = corner,
        modifier = modifier,
        enabled = enabled,
        isSelected = isSelected,
        isLoading = isLoading,
    )
}

/**
 * Provides the color scheme for [IconButton], resolving foreground, background,
 * and border colors based on interaction, enabled, and selection states.
 */
@Composable
private fun iconButtonColors(): ButtonColor {
    val containerColor = Theme.color.stage
    val contentColor = Theme.color.ink
    val selectedContainerColor = Theme.color.primary
    val selectedContentColor = Theme.color.surface
    val disabledContentColor = Theme.color.ink2
    val disabledContainerColor = Theme.color.stage

    return object : ButtonColor {

        @Composable
        override fun borderColor(interactionState: Int, enabled: Boolean, loading: Boolean): State<Color> =
            rememberUpdatedState(Color.Transparent)

        @Composable
        override fun foregroundColor(interactionState: Int, enabled: Boolean, loading: Boolean): State<Color> =
            rememberUpdatedState(
                when {
                    !enabled -> disabledContentColor
                    interactionState has ButtonInteractionState.SELECTED -> selectedContentColor
                    else -> contentColor
                },
            )

        @Composable
        override fun backgroundColor(interactionState: Int, enabled: Boolean, loading: Boolean): State<Color> =
            rememberUpdatedState(
                when {
                    !enabled -> disabledContainerColor
                    interactionState has ButtonInteractionState.PRESSED -> Theme.color.primaryHover
                    interactionState has ButtonInteractionState.SELECTED -> selectedContainerColor
                    else -> containerColor
                },
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonPreview() {
    AppTheme {
        Row(
            modifier = Modifier.padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            IconButton(icon = Settings, onClick = {}, size = ButtonSizeType.Medium)
            IconButton(icon = Settings, onClick = {}, size = ButtonSizeType.Medium, isSelected = true)
            IconButton(icon = Settings, onClick = {}, size = ButtonSizeType.Medium, enabled = false)
            IconButton(icon = Settings, onClick = {}, size = ButtonSizeType.Large)
        }
    }
}
