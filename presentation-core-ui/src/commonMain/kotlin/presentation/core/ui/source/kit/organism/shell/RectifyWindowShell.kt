package presentation.core.ui.source.kit.organism.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.CounterBadge
import presentation.core.ui.source.kit.atom.badge.CounterBadgeState
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.handle.PanelResizeHandle
import presentation.core.ui.source.kit.atom.indicator.StatusIndicator
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState
import presentation.core.ui.source.kit.molecule.button.SegmentedButtonGroup
import presentation.core.ui.source.kit.molecule.chip.DeviceChip
import presentation.core.ui.source.kit.molecule.chip.DeviceChipState

/**
 * The application window's layout contract: toolbar, stage, optional side panel, status bar.
 *
 * ```
 * Column [ toolbar, Row [ StageSurface, PanelResizeHandle, SidePanelFrame ], statusBar ]
 * ```
 *
 * [sidePanel] is a nullable *slot*, not a visibility flag: before a mirroring session exists
 * there is nothing to put in the panel, so neither the panel nor its resize handle is
 * composed at all (FR-15 / AC-31). A caller cannot accidentally show an empty panel, because
 * there is no way to express "panel, but with no content".
 *
 * Panel width is read from [state] and committed *on drag end only* —
 * [ShellState.onDragStopped] returns a width to persist or `null` when the drag changed
 * nothing. The shell itself persists nothing; it hands the value to [onPanelWidthCommit] and
 * the window layer decides what that means.
 *
 * @param toolbar Top chrome bar — typically `ShellToolbar`.
 * @param statusBar Bottom chrome bar — typically `ShellStatusBar`.
 * @param modifier Modifier applied to the root composable.
 * @param state Hoisted shell state; defaults to a freshly remembered one.
 * @param sidePanel Side-panel content for the active tab, or `null` while no session is live.
 * @param onPanelWidthCommit Called once per resize drag that actually changed the width.
 * @param stage Mirror-region content, composed on the bezel-less stage.
 */
@Composable
public fun RectifyWindowShell(
    toolbar: @Composable () -> Unit,
    statusBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: ShellState = rememberShellState(),
    sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null,
    // Deliberately NOT defaulted: a `= {}` default silently swallows every committed drag, which
    // is exactly AC-39's failure mode and is invisible at the call site. A caller that renders a
    // side panel must say what happens to the width.
    onPanelWidthCommit: (Dp) -> Unit,
    stage: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize().background(Theme.color.bg)) {
        toolbar()
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            StageSurface(
                modifier = Modifier.fillMaxHeight().weight(1f),
                content = stage,
            )
            if (sidePanel != null) {
                PanelResizeHandle(
                    // The handle sits on the panel's leading edge, so dragging left (negative
                    // delta) widens the panel.
                    onDelta = { delta -> state.resizePanelBy(-delta) },
                    onDragStarted = state::onDragStarted,
                    onDragStopped = { state.onDragStopped()?.let(onPanelWidthCommit) },
                )
                SidePanelFrame(
                    activeTab = state.activeTab,
                    onTabSelect = state::selectTab,
                    modifier = Modifier.fillMaxHeight().width(state.panelWidth),
                    content = sidePanel,
                )
            }
        }
        statusBar()
    }
}

/** Comp 02's toolbar composition — shipped kit components in every slot. */
@Composable
private fun ShellPreviewToolbar() {
    ShellToolbar(
        platformSwitch = {
            SegmentedButtonGroup(items = listOf("Android", "iOS"), selectedIndex = 0, onSelect = {})
        },
        deviceIndicator = { DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {}) },
        stagedCounter = { CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged") },
        clearAction = {
            AppButton(text = "Clear", onClick = {}, variant = AppButtonVariant.Ghost, enabled = false)
        },
        primaryAction = {
            AppButton(text = "Resolve & Send", onClick = {}, shortcut = "⌘↵", enabled = false)
        },
    )
}

/** Comp 02's status bar — the neutral waiting state, no tool detection. */
@Composable
private fun ShellPreviewStatusBar() {
    ShellStatusBar(
        leading = { StatusIndicator(state = StatusIndicatorState.Offline, label = "Waiting for device…") },
        trailing = {
            Text(
                text = "Rectify 0.4",
                style = Theme.typography.monoMeta,
                color = Theme.color.ink2,
                maxLines = 1,
            )
        },
    )
}

/** Stand-in stage content; the real empty state is downstream scope. */
@Composable
private fun BoxScope.ShellPreviewStage() {
    Text(
        text = "NO SIGNAL",
        style = Theme.typography.monoMeta,
        color = Theme.color.ink3,
        modifier = Modifier.align(Alignment.Center),
    )
}

/** No session yet: neither the handle nor the panel is composed. */
@Composable
private fun RectifyWindowShellNoPanelPreviewContent() {
    RectifyWindowShell(
        toolbar = { ShellPreviewToolbar() },
        statusBar = { ShellPreviewStatusBar() },
        // No panel in this state, so no width is ever committed.
        onPanelWidthCommit = {},
        stage = { ShellPreviewStage() },
    )
}

/** Session live: the panel slot is filled, so the handle and panel both appear. */
@Composable
private fun RectifyWindowShellWithPanelPreviewContent() {
    RectifyWindowShell(
        toolbar = { ShellPreviewToolbar() },
        statusBar = { ShellPreviewStatusBar() },
        sidePanel = { tab ->
            Box(modifier = Modifier.fillMaxSize().padding(Theme.spacing.spacingL)) {
                Text(text = tab.label, style = Theme.typography.secondary, color = Theme.color.ink2)
            }
        },
        // A preview has nowhere to persist to; the real call site writes through
        // SetSidePanelWidthUseCase.
        onPanelWidthCommit = {},
        stage = { ShellPreviewStage() },
    )
}

@Preview(showBackground = true)
@Composable
private fun RectifyWindowShellNoPanelPreviewLight() {
    AppTheme(mode = ThemeMode.Light) { RectifyWindowShellNoPanelPreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun RectifyWindowShellNoPanelPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) { RectifyWindowShellNoPanelPreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun RectifyWindowShellWithPanelPreviewLight() {
    AppTheme(mode = ThemeMode.Light) { RectifyWindowShellWithPanelPreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun RectifyWindowShellWithPanelPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) { RectifyWindowShellWithPanelPreviewContent() }
}
