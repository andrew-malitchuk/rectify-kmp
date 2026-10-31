package presentation.core.ui.source.kit.organism.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.CounterBadge
import presentation.core.ui.source.kit.atom.badge.CounterBadgeState
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.molecule.button.SegmentedButtonGroup
import presentation.core.ui.source.kit.molecule.chip.DeviceChip
import presentation.core.ui.source.kit.molecule.chip.DeviceChipState

/** Toolbar geometry. */
private val ToolbarHeight: Dp = 52.dp

/**
 * The window's top chrome bar.
 *
 * The slots are **named and fixed in order** rather than a free-form `RowScope` content
 * lambda: this is the application's one canonical toolbar, and a caller that can reorder it
 * can also make two screens disagree about where "Clear" lives.
 *
 * Authors no content of its own and knows nothing about devices — every slot is filled from
 * the shipped UI kit by the caller (AC-37).
 *
 * [leadingInset] reserves room for the macOS traffic lights, which sit *inside* this bar once
 * the window turns on full-size content; on a platform without them, pass `0.dp`.
 *
 * @param platformSwitch Android / iOS source selector.
 * @param deviceIndicator Current device chip.
 * @param stagedCounter Staged-annotation count, or `null` to omit it.
 * @param clearAction Low-emphasis "clear staged" action, or `null` to omit it.
 * @param primaryAction The bar's single call to action, or `null` to omit it.
 *
 * The three trailing slots are independently optional because the no-device toolbar has
 * nothing to stage, clear or send: leaving three dead controls on screen advertises actions
 * that cannot be taken.
 * @param modifier Modifier applied to the root composable.
 * @param leadingInset Leading space kept clear for the window's traffic lights.
 */
@Composable
public fun ShellToolbar(
    platformSwitch: @Composable () -> Unit,
    deviceIndicator: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    stagedCounter: (@Composable () -> Unit)? = null,
    clearAction: (@Composable () -> Unit)? = null,
    primaryAction: (@Composable () -> Unit)? = null,
    leadingInset: Dp = ShellWindowDefaults.TrafficLightInset,
) {
    Column(modifier = modifier.fillMaxWidth().background(Theme.color.chrome)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ToolbarHeight)
                .padding(end = Theme.spacing.spacingL),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(leadingInset))
            platformSwitch()
            Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
            deviceIndicator()
            Spacer(modifier = Modifier.weight(1f))
            // Each trailing slot carries its own leading spacer so that omitting one leaves no
            // orphaned gap — the row must look deliberate with all three absent, which is the
            // no-device toolbar.
            stagedCounter?.let { it() }
            clearAction?.let {
                Spacer(modifier = Modifier.width(Theme.spacing.spacingL))
                it()
            }
            primaryAction?.let {
                Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
                it()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme.stroke.thin)
                .background(Theme.color.line),
        )
    }
}

/**
 * Comp 02's toolbar: Android selected, no device, nothing staged, the CTA inert.
 *
 * Every slot is a shipped kit component — this preview is the standing evidence that the
 * toolbar needed no new leaf component.
 */
@Composable
private fun ShellToolbarEmptyPreviewContent() {
    ShellToolbar(
        platformSwitch = {
            SegmentedButtonGroup(items = listOf("Android", "iOS"), selectedIndex = 0, onSelect = {})
        },
        deviceIndicator = {
            DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {})
        },
        stagedCounter = {
            CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged")
        },
        clearAction = {
            AppButton(text = "Clear", onClick = {}, variant = AppButtonVariant.Ghost, enabled = false)
        },
        primaryAction = {
            AppButton(text = "Resolve & Send", onClick = {}, shortcut = "⌘↵", enabled = false)
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ShellToolbarPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        ShellToolbarEmptyPreviewContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun ShellToolbarPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        ShellToolbarEmptyPreviewContent()
    }
}
