package presentation.core.ui.source.kit.molecule.command

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant

/** Block geometry. */
private val BlockRadius: Dp = 8.dp

/**
 * A copyable shell command — `$ brew install --cask android-platform-tools`.
 *
 * The command is shown in full and scrolls sideways rather than wrapping or truncating: a
 * half-shown command is worse than useless, because the user cannot tell what they would be
 * pasting.
 *
 * Copying is the host's: Compose Multiplatform's `ClipboardManager` is deprecated in the
 * version this project builds against, and its replacement is suspend-based, so wiring either
 * one in here would bake a moving API into the design system. [onCopy] also lets the caller
 * confirm the copy however the surrounding screen already does.
 *
 * @param command The command to display, without the leading `$`.
 * @param onCopy Called when the copy action is tapped.
 * @param modifier Modifier applied to the root composable.
 * @param copyLabel Label of the copy action.
 */
@Composable
public fun CommandBlock(
    command: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    copyLabel: String = "Copy",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(BlockRadius))
            .background(Theme.color.codeBg)
            .padding(
                horizontal = Theme.spacing.spacingS + Theme.spacing.spacingXS,
                vertical = Theme.spacing.spacingS,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Text(
            text = "$",
            style = Theme.typography.monoData,
            color = Theme.color.ink3,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
        ) {
            Text(
                text = command,
                style = Theme.typography.monoData,
                color = Theme.color.ink,
                maxLines = 1,
            )
        }
        AppButton(
            text = copyLabel,
            onClick = onCopy,
            variant = AppButtonVariant.Secondary,
            size = AppButtonSize.InlineRow,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommandBlockPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
        ) {
            CommandBlock(
                command = "brew install --cask android-platform-tools",
                onCopy = {},
            )
        }
    }
}
