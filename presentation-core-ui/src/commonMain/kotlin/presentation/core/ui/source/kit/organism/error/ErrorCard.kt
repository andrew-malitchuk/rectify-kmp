package presentation.core.ui.source.kit.organism.error

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import presentation.core.ui.source.kit.atom.glyph.ErrorGlyph
import presentation.core.ui.source.kit.atom.glyph.ErrorKind
import presentation.core.ui.source.kit.molecule.command.CommandBlock

/** Card geometry. */
private val CardMaxWidth: Dp = 520.dp

/**
 * The error card — the app's standard way of reporting a problem the user can act on.
 *
 * Its order is the style guide's rule for errors, and the parameter list follows it so a call
 * site cannot easily get it wrong: **problem → one primary action → exact command →
 * diagnostic**. [title] states the fact, [body] gives the one next step in prose, [command] is
 * the literal thing to run, and [diagnostic] is the raw machine output, last and muted, for
 * whoever needs to dig.
 *
 * There is exactly one [primaryActionLabel]. A second, lower-emphasis [secondaryActionLabel] is
 * allowed for retrying or dismissing, but an error that offers two equal choices has not
 * decided what it wants the user to do.
 *
 * For a full-screen empty/error placeholder rather than an actionable card, use
 * [ErrorState][presentation.core.ui.source.kit.molecule.state.ErrorState].
 *
 * @param title What went wrong, stated as a fact.
 * @param body The one next action, in prose.
 * @param modifier Modifier applied to the root composable.
 * @param kind Problem kind driving the leading glyph.
 * @param command Exact command to run; `null` hides the command block.
 * @param onCopyCommand Called when the user copies [command].
 * @param primaryActionLabel Label of the primary action; `null` hides it.
 * @param onPrimaryAction Called when the primary action is tapped.
 * @param secondaryActionLabel Label of the secondary action; `null` hides it.
 * @param onSecondaryAction Called when the secondary action is tapped.
 * @param diagnostic Raw diagnostic line; `null` hides the footer.
 */
@Composable
public fun ErrorCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    kind: ErrorKind = ErrorKind.Fixable,
    command: String? = null,
    onCopyCommand: () -> Unit = {},
    primaryActionLabel: String? = null,
    onPrimaryAction: () -> Unit = {},
    secondaryActionLabel: String? = null,
    onSecondaryAction: () -> Unit = {},
    diagnostic: String? = null,
) {
    Column(
        modifier = modifier
            .widthIn(max = CardMaxWidth)
            .clip(Theme.cornerToken.card)
            .background(Theme.color.surface)
            .border(Theme.stroke.thin, Theme.color.line, Theme.cornerToken.card)
            .padding(Theme.spacing.spacingL),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
        ) {
            ErrorGlyph(kind = kind)
            Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS)) {
                Text(
                    text = title,
                    style = Theme.typography.titleS,
                    color = Theme.color.ink,
                )
                Text(
                    text = body,
                    style = Theme.typography.body,
                    color = Theme.color.ink2,
                )
            }
        }

        if (command != null) {
            CommandBlock(command = command, onCopy = onCopyCommand)
        }

        if (primaryActionLabel != null || secondaryActionLabel != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            ) {
                if (primaryActionLabel != null) {
                    AppButton(
                        text = primaryActionLabel,
                        onClick = onPrimaryAction,
                        size = AppButtonSize.Dialog,
                    )
                }
                if (secondaryActionLabel != null) {
                    AppButton(
                        text = secondaryActionLabel,
                        onClick = onSecondaryAction,
                        variant = AppButtonVariant.Ghost,
                        size = AppButtonSize.Dialog,
                    )
                }
            }
        }

        if (diagnostic != null) {
            Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Theme.stroke.hairline)
                        .background(Theme.color.line),
                )
                Text(
                    text = diagnostic,
                    style = Theme.typography.monoMeta,
                    color = Theme.color.ink3,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorCardPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
        ) {
            ErrorCard(
                title = "adb isn't on your PATH",
                body = "Install platform-tools, or point Rectify at an adb you already have.",
                command = "brew install --cask android-platform-tools",
                primaryActionLabel = "Locate adb…",
                secondaryActionLabel = "Retry",
                diagnostic = "which adb → not found · \$ANDROID_HOME unset",
            )
        }
    }
}
