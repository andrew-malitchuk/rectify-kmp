package presentation.core.ui.source.kit.molecule.select

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.ChevronDown

/** Select geometry (comp 02). Matches the `Boot` button it sits beside. */
private val SelectHeight: Dp = 28.dp
private val SelectMinWidth: Dp = 150.dp

/** Dim applied when there is nothing to choose from. */
private const val DISABLED_ALPHA = 0.5f

/**
 * The AVD picker from comp 02 — `Pixel_7_API_34 ▾`, the one fast path out of NO SIGNAL.
 *
 * AVD names are `avdmanager` output, not prose: they are mono and never localised. Only the
 * zero-state label is translatable copy, which is why [emptyLabel] is a parameter and the
 * names are not.
 *
 * **Empty is a state, not an empty menu.** With no AVDs the control renders dimmed, shows
 * [emptyLabel] instead of a name, refuses to open and is announced as disabled — opening an
 * empty popup would tell the user nothing about *why* it is empty.
 *
 * Stateless apart from its own open/closed flag, which is pure view state no caller needs.
 *
 * @param avds AVD names as reported by the emulator tooling; empty disables the control.
 * @param selected Currently chosen AVD; falls back to the first entry when `null`.
 * @param onSelect Called with the AVD the user picked.
 * @param modifier Modifier applied to the root composable.
 * @param emptyLabel Copy shown in place of a name when [avds] is empty.
 */
@Composable
public fun EmulatorSelect(
    avds: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    emptyLabel: String = "No AVDs installed",
) {
    var expanded by remember { mutableStateOf(false) }
    val enabled = avds.isNotEmpty()
    val contentAlpha = if (enabled) 1f else DISABLED_ALPHA
    val label = selected ?: avds.firstOrNull() ?: emptyLabel
    val shape = Theme.cornerToken.input

    Row(
        modifier = modifier
            .height(SelectHeight)
            .defaultMinSize(minWidth = SelectMinWidth)
            .clip(shape)
            .background(Theme.color.surface)
            .border(width = Theme.stroke.thin, color = Theme.color.line, shape = shape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.DropdownList,
                        onClick = { expanded = true },
                    )
                } else {
                    Modifier.semantics { disabled() }
                },
            )
            .padding(horizontal = Theme.spacing.spacingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Text(
            text = label,
            // The empty label is prose, an AVD name is machine output — type says which.
            style = if (enabled) Theme.typography.monoMeta else Theme.typography.secondary,
            color = Theme.color.ink.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = ChevronDown,
            contentDescription = null,
            tint = Theme.color.ink2.copy(alpha = contentAlpha),
            modifier = Modifier.size(Theme.size.iconXS),
        )

        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            containerColor = Theme.color.pop,
        ) {
            avds.forEach { avd ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = avd,
                            style = Theme.typography.monoMeta,
                            color = Theme.color.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(avd)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmulatorSelectPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            EmulatorSelect(
                avds = listOf("Pixel_7_API_34", "Pixel_Fold_API_35"),
                selected = "Pixel_7_API_34",
                onSelect = {},
            )
            EmulatorSelect(avds = emptyList(), selected = null, onSelect = {})
        }
    }
}
