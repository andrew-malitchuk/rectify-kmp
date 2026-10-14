package dev.rectify.io.source.window

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import domain.core.source.model.ThemeModel
import domain.usecase.api.source.usecase.configuration.GetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.ObserveThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.CounterBadge
import presentation.core.ui.source.kit.atom.badge.CounterBadgeState
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.button.ButtonSizeType
import presentation.core.ui.source.kit.atom.button.IconButton
import presentation.core.ui.source.kit.atom.icon.Sun
import presentation.core.ui.source.kit.atom.indicator.StatusIndicator
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState
import presentation.core.ui.source.kit.molecule.button.SegmentedButtonGroup
import presentation.core.ui.source.kit.molecule.chip.DeviceChip
import presentation.core.ui.source.kit.molecule.chip.DeviceChipState
import presentation.core.ui.source.kit.organism.shell.RectifyWindowShell
import presentation.core.ui.source.kit.organism.shell.ShellState
import presentation.core.ui.source.kit.organism.shell.ShellStatusBar
import presentation.core.ui.source.kit.organism.shell.ShellTab
import presentation.core.ui.source.kit.organism.shell.ShellToolbar
import presentation.core.ui.source.kit.organism.shell.ShellWindowDefaults
import presentation.core.ui.source.kit.organism.shell.rememberShellState
import presentation.core.ui.source.kit.organism.shell.shortcutToTab
import java.awt.Dimension
import kotlin.math.roundToInt

private val log = Logger.withTag("RectifyWindow")

/** Window title — hidden by the macOS full-size-content chrome, still used by the window menu. */
private const val WINDOW_TITLE = "Rectify"

/** Build marker rendered in the status bar's trailing slot. */
private const val VERSION_LABEL = "Rectify 0.4"

/** Platform sources offered by the toolbar switch; iOS is inert in this deliverable. */
private val PLATFORM_SOURCES = listOf("Android", "iOS")

/**
 * The Rectify desktop window: geometry, macOS chrome, shortcut dispatch and preference wiring.
 *
 * This is the desktop root — it replaces `App()`/`NavigationHost` on this platform, which is
 * why it repeats the theme observation rather than reusing it: `App()` is still the
 * Android/iOS root and stays untouched.
 *
 * Everything visual comes from [RectifyShellContent]; everything stateful lives here, so the
 * composition can be rendered without Koin, an AWT window or a running preference store.
 */
@Composable
public fun ApplicationScope.RectifyWindow() {
    val observeTheme = koinInject<ObserveThemeUseCase>()
    val setTheme = koinInject<SetThemeUseCase>()
    val getSidePanelWidth = koinInject<GetSidePanelWidthUseCase>()
    val setSidePanelWidth = koinInject<SetSidePanelWidthUseCase>()

    // NOTE: System is both the initial and the failure value — the preference is unset on
    // first launch, and a storage that cannot be read must not stop the window opening.
    val theme by observeTheme()
        .collectAsState(initial = Result.success(ThemeModel.System))
    val themeModel = theme.getOrNull() ?: ThemeModel.System

    val shellState = rememberShellState()
    val scope = rememberCoroutineScope()

    // Read once at launch, never polled: the width is a preference, not a stream (AC-39).
    LaunchedEffect(Unit) {
        getSidePanelWidth()
            .onSuccess { shellState.setPanelWidth(it.dp) }
            .onFailure { log.w(it) { "Side panel width unreadable — keeping the default." } }
    }

    // AC-31: no mirroring session exists in this deliverable, so there is no panel content to
    // put in the slot. Neither the panel nor its resize handle is composed, and Cmd+1/2/3 has
    // nothing to select — so it must stay a no-op that does NOT consume the event (SC-4).
    val sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = ShellWindowDefaults.InitialSize),
        title = WINDOW_TITLE,
        onPreviewKeyEvent = { event ->
            handleShellShortcut(
                event = event,
                isPanelVisible = sidePanel != null,
                onTabSelect = shellState::selectTab,
            )
        },
    ) {
        ApplyWindowChrome()

        AppTheme(mode = themeModel.toThemeMode()) {
            RectifyShellContent(
                state = shellState,
                sidePanel = sidePanel,
                onPanelWidthCommit = { width ->
                    // Fires on drag END only (RectifyWindowShell contract) — one write per
                    // resize that changed something, never one per frame.
                    scope.launch {
                        setSidePanelWidth(width.value.roundToInt())
                            .onFailure { log.w(it) { "Side panel width not persisted." } }
                    }
                },
                onThemeCycle = {
                    scope.launch {
                        setTheme(themeModel.next())
                            .onFailure { log.w(it) { "Theme override not persisted." } }
                    }
                },
            )
        }
    }
}

/**
 * The window's whole composition: the shell with every slot filled from shipped kit components.
 *
 * Separate from [RectifyWindow] and free of Koin, AWT and coroutines on purpose — this is what
 * a screenshot harness and a human reviewer can both look at in isolation.
 *
 * Every slot here is a **static stub** (AC-37): nothing detects a device, and nothing in this
 * file knows what adb or idb is.
 *
 * @param state Hoisted shell state (active tab + panel width).
 * @param onPanelWidthCommit Called once per resize drag that changed the width.
 * @param onThemeCycle Called when the status-bar theme override is clicked.
 * @param modifier Modifier applied to the shell.
 * @param sidePanel Panel content for the live session, or `null` while none exists.
 */
@Composable
internal fun RectifyShellContent(
    state: ShellState,
    onPanelWidthCommit: (Dp) -> Unit,
    onThemeCycle: () -> Unit,
    modifier: Modifier = Modifier,
    sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null,
) {
    RectifyWindowShell(
        toolbar = {
            ShellToolbar(
                platformSwitch = {
                    SegmentedButtonGroup(items = PLATFORM_SOURCES, selectedIndex = 0, onSelect = {})
                },
                deviceIndicator = {
                    DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {})
                },
                stagedCounter = {
                    CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged")
                },
                clearAction = {
                    AppButton(
                        text = "Clear",
                        onClick = {},
                        variant = AppButtonVariant.Ghost,
                        enabled = false,
                    )
                },
                primaryAction = {
                    AppButton(text = "Resolve & Send", onClick = {}, shortcut = "⌘↵", enabled = false)
                },
            )
        },
        statusBar = {
            ShellStatusBar(
                leading = {
                    StatusIndicator(
                        state = StatusIndicatorState.Offline,
                        label = "Waiting for device…",
                    )
                },
                trailing = {
                    Text(
                        text = VERSION_LABEL,
                        style = Theme.typography.monoMeta,
                        color = Theme.color.ink2,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
                    IconButton(icon = Sun, onClick = onThemeCycle, size = ButtonSizeType.Small)
                },
            )
        },
        modifier = modifier,
        state = state,
        sidePanel = sidePanel,
        onPanelWidthCommit = onPanelWidthCommit,
        stage = {
            // One neutral placeholder: the dashed outline and onboarding copy of comp 02 are
            // the downstream device spec's content, not the shell's.
            Text(
                text = "NO SIGNAL",
                style = Theme.typography.monoMeta,
                color = Theme.color.ink2,
                modifier = Modifier.align(Alignment.Center),
            )
        },
    )
}

/**
 * Applies the AWT-level window chrome: the resize floor and, on macOS, full-size content.
 *
 * Both live in a [LaunchedEffect] keyed on the window because neither is expressible through
 * Compose's `WindowState` — `minimumSize` is an AWT property and the traffic-light treatment
 * is a `JRootPane` client property the macOS peer reads.
 */
@Composable
private fun FrameWindowScope.ApplyWindowChrome() {
    LaunchedEffect(window) {
        // Dp maps 1:1 onto AWT units here — the same conversion Compose Desktop itself uses
        // when it applies WindowState.size, so the floor and the initial size stay consistent.
        window.minimumSize = Dimension(
            ShellWindowDefaults.MinimumSize.width.value.roundToInt(),
            ShellWindowDefaults.MinimumSize.height.value.roundToInt(),
        )

        // NFR-10: macOS only. Elsewhere these keys are meaningless, so the window simply keeps
        // its standard title bar — the toolbar's reserved inset then reads as extra padding.
        if (!isMacOs()) return@LaunchedEffect
        runCatching {
            window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }.onFailure {
            log.w(it) { "macOS full-size content unavailable — standard title bar." }
        }
    }
}

/** Whether the host is macOS, where the traffic lights can be pulled into the toolbar. */
private fun isMacOs(): Boolean =
    System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)

/**
 * Dispatches a window key event to the side-panel tab shortcuts (AC-33).
 *
 * The mapping itself is [shortcutToTab]; this only decides whether the event is ours. It is
 * consumed **only** when a tab matched *and* the panel is on screen — with no panel there is
 * nothing to select, so the event must pass through to whatever else wants it (SC-4).
 *
 * @return `true` when the event was handled and must not propagate.
 */
private fun handleShellShortcut(
    event: KeyEvent,
    isPanelVisible: Boolean,
    onTabSelect: (ShellTab) -> Unit,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    val tab = shortcutToTab(isMeta = event.isMetaPressed, key = event.key) ?: return false
    if (!isPanelVisible) return false
    onTabSelect(tab)
    return true
}

/** Maps the persisted theme choice onto the styling layer's mode. */
private fun ThemeModel.toThemeMode(): ThemeMode = when (this) {
    ThemeModel.Light -> ThemeMode.Light
    ThemeModel.Dark -> ThemeMode.Dark
    ThemeModel.System -> ThemeMode.System
}

/** Next theme in the status-bar override cycle: System → Light → Dark → System. */
private fun ThemeModel.next(): ThemeModel = when (this) {
    ThemeModel.System -> ThemeModel.Light
    ThemeModel.Light -> ThemeModel.Dark
    ThemeModel.Dark -> ThemeModel.System
}
