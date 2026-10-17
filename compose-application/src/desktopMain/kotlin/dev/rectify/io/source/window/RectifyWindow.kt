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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import domain.core.source.model.ThemeModel
import domain.core.source.model.device.PlatformModel
import domain.usecase.api.source.usecase.configuration.GetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.ObserveApplicationLanguageUseCase
import domain.usecase.api.source.usecase.configuration.ObserveThemeUseCase
import domain.usecase.api.source.usecase.configuration.SetSidePanelWidthUseCase
import domain.usecase.api.source.usecase.configuration.SetThemeUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import presentation.core.localisation.source.provider.AppLocaleProvider
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
import dev.rectify.io.core.chooser.chooseAdbPath
import presentation.feature.mirror.source.mirror.MirrorSideEffect
import presentation.feature.mirror.source.mirror.MirrorHost
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorNotesTab
import presentation.feature.mirror.source.mirror.MirrorPayloadTab
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorStageContent
import presentation.feature.mirror.source.mirror.MirrorStatusLine
import presentation.feature.mirror.source.mirror.MirrorUiState
import java.awt.Dimension
import kotlin.math.roundToInt

private val log = Logger.withTag("RectifyWindow")

/** Window title — hidden by the macOS full-size-content chrome, still used by the window menu. */
private const val WINDOW_TITLE = "Rectify"

/** Build marker rendered in the status bar's trailing slot. */
private const val VERSION_LABEL = "Rectify 0.4"

/**
 * Platform sources offered by the toolbar switch, in `PlatformModel` order — the index the
 * switch reports is mapped straight onto that enum.
 */
private val PLATFORM_SOURCES = listOf("Android", "iOS")

/** Chip label before anything has been selected. */
private const val NO_DEVICE_LABEL = "No device"


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
    val observeLanguage = koinInject<ObserveApplicationLanguageUseCase>()
    val languageCode by observeLanguage()
        .map { it.getOrNull() ?: "en" }
        // Mirrors App.kt: "en" is both the initial value and the failed-Result fallback,
        // because the preference is unset on first launch.
        .collectAsState(initial = "en")

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

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = ShellWindowDefaults.InitialSize),
        title = WINDOW_TITLE,
        onPreviewKeyEvent = { event ->
            handleShellShortcut(
                event = event,
                // The panel is composed for the whole life of the window now that the Notes tab
                // has content (FR-28), so Cmd+1/2/3 always has something to select. This is a
                // literal and not a read of the panel slot because `onPreviewKeyEvent` is a
                // Window *parameter* — it cannot see into the Window's content lambda.
                isPanelVisible = true,
                onTabSelect = shellState::selectTab,
            )
        },
    ) {
        ApplyWindowChrome()

        // The desktop root no longer goes through App(), so it must provide the locale itself —
        // without this, pref_language is inert on desktop.
        AppLocaleProvider(languageCode = languageCode) {
            AppTheme(mode = themeModel.toThemeMode()) {
                // The mirror ViewModel is resolved HERE, not inside RectifyShellContent:
                // the shell composition stays Koin-free so a render harness can drive it
                // from literals.
                MirrorHost(
                    // AC-26. Only the window layer owns a file dialog, so the ViewModel asks
                    // for one by posting an effect and takes the answer back as an intent.
                    // `window` is the Rectify frame, so the panel attaches to it rather than
                    // floating free.
                    onSideEffect = { effect, onIntent ->
                        when (effect) {
                            MirrorSideEffect.LocateAdb ->
                                chooseAdbPath(window)?.let { onIntent(MirrorIntent.AdbPathChosen(it)) }
                            else -> Unit
                        }
                    },
                ) { mirror, frame, hierarchy, onMirrorIntent ->
                    // AC-49: the first annotation of a frozen frame reveals the Notes tab, and
                    // only the first — the key stays true from the second one on, so a user who
                    // has walked over to Payload is not yanked back on every drag.
                    val hasStaged = mirror.stagedCount >= 1
                    LaunchedEffect(hasStaged) {
                        if (hasStaged) shellState.selectTab(ShellTab.Notes)
                    }

                    RectifyShellContent(
                        state = shellState,
                        mirror = mirror,
                        frame = frame,
                        onIntent = onMirrorIntent,
                        sidePanel = { tab ->
                            // Notes and Payload have content; Context is empty rather than
                            // absent, because the tab strip is the thing Cmd+1/2/3 drives and
                            // hiding a tab would make a shortcut dead.
                            //
                            // `weight(1f)` on both is load-bearing, not cosmetic: each tab pins
                            // its own footer with an inner `weight(1f)`, which collapses to zero
                            // under an unbounded height. The panel's bounded height is what makes
                            // the bodies render at all.
                            when (tab) {
                                ShellTab.Notes -> MirrorNotesTab(
                                    state = mirror,
                                    onIntent = onMirrorIntent,
                                    modifier = Modifier.weight(1f),
                                )
                                // The bitmap and the hierarchy both arrive through MirrorHost's
                                // content lambda — the crops need the pixels and the node labels
                                // need the dump, and neither is in the reduced state.
                                ShellTab.Payload -> MirrorPayloadTab(
                                    state = mirror,
                                    frame = frame,
                                    hierarchy = hierarchy,
                                    onIntent = onMirrorIntent,
                                    modifier = Modifier.weight(1f),
                                )
                                else -> Unit
                            }
                        },
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
    }
}

/**
 * The window's whole composition: the shell with every slot filled from shipped kit components.
 *
 * Separate from [RectifyWindow] and free of Koin, AWT and coroutines on purpose — this is what
 * a screenshot harness and a human reviewer can both look at in isolation.
 *
 * The device slots are live, driven entirely by the [mirror] snapshot and [frame] handed in.
 * All three mirror parameters are defaulted so a harness — and every pre-existing call site —
 * can compose the shell with no ViewModel at all.
 *
 * @param state Hoisted shell state (active tab + panel width).
 * @param onPanelWidthCommit Called once per resize drag that changed the width.
 * @param onThemeCycle Called when the status-bar theme override is clicked.
 * @param modifier Modifier applied to the shell.
 * @param sidePanel Panel content for the live session, or `null` while none exists.
 * @param mirror Mirror state snapshot.
 * @param frame Latest decoded device frame, or `null` when nothing is streaming.
 * @param onIntent Mirror intent sink.
 */
@Composable
@Suppress("LongParameterList")
internal fun RectifyShellContent(
    state: ShellState,
    onPanelWidthCommit: (Dp) -> Unit,
    onThemeCycle: () -> Unit,
    modifier: Modifier = Modifier,
    sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null,
    mirror: MirrorUiState = MirrorUiState(),
    frame: ImageBitmap? = null,
    onIntent: (MirrorIntent) -> Unit = {},
) {
    RectifyWindowShell(
        toolbar = {
            ShellToolbar(
                platformSwitch = {
                    SegmentedButtonGroup(
                        items = PLATFORM_SOURCES,
                        selectedIndex = mirror.platform.ordinal,
                        onSelect = { index ->
                            onIntent(MirrorIntent.SelectPlatform(PlatformModel.entries[index]))
                        },
                    )
                },
                deviceIndicator = {
                    DeviceChip(
                        name = mirror.selectedDeviceName ?: NO_DEVICE_LABEL,
                        state = mirror.chipState(),
                        // AC-43: the chip is the way back to the picker once a device is picked.
                        onClick = { onIntent(MirrorIntent.OpenPicker) },
                        detail = mirror.chipDetail(),
                    )
                },
                stagedCounter = {
                    CounterBadge(
                        count = mirror.stagedCount,
                        state = if (mirror.stagedCount == 0) {
                            CounterBadgeState.Empty
                        } else {
                            CounterBadgeState.Staged
                        },
                        label = "staged",
                    )
                },
                clearAction = {
                    AppButton(
                        text = "Clear",
                        // Empties the staged list; the frame stays pinned until Resume live
                        // (AC-48).
                        onClick = { onIntent(MirrorIntent.ClearStaged) },
                        variant = AppButtonVariant.Ghost,
                        enabled = mirror.stagedCount > 0,
                    )
                },
                primaryAction = {
                    AppButton(text = "Resolve & Send", onClick = {}, shortcut = "⌘↵", enabled = false)
                },
            )
        },
        statusBar = {
            ShellStatusBar(
                leading = { MirrorStatusLine(state = mirror) },
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
        // The tab counts and the toolbar badge read the same number by construction.
        tabCounts = mapOf(
            ShellTab.Notes to mirror.stagedCount,
            ShellTab.Payload to mirror.stagedCount,
        ),
        onPanelWidthCommit = onPanelWidthCommit,
        stage = {
            MirrorStageContent(
                state = mirror,
                frame = frame,
                onIntent = onIntent,
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

/** Dot colour of the toolbar chip, from the session the user actually has. */
private fun MirrorUiState.chipState(): DeviceChipState = when (session) {
    is MirrorSession.Streaming -> DeviceChipState.Ready
    is MirrorSession.Lost -> DeviceChipState.Lost
    MirrorSession.Idle -> if (selectedDeviceId == null) DeviceChipState.None else DeviceChipState.Warn
}

/** Machine detail beside the chip name — the serial, which is what identifies the device. */
private fun MirrorUiState.chipDetail(): String? = selectedDeviceId

/** Next theme in the status-bar override cycle: System → Light → Dark → System. */
private fun ThemeModel.next(): ThemeModel = when (this) {
    ThemeModel.System -> ThemeModel.Light
    ThemeModel.Light -> ThemeModel.Dark
    ThemeModel.Dark -> ThemeModel.System
}
