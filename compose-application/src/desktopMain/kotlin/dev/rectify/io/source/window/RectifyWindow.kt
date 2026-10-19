package dev.rectify.io.source.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
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
import org.jetbrains.compose.ui.tooling.preview.Preview
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
import presentation.core.ui.source.kit.atom.snackbar.StackedSnackbarDuration
import presentation.core.ui.source.kit.atom.snackbar.StackedSnackbarHost
import presentation.core.ui.source.kit.atom.snackbar.StackedSnakbarHostState
import presentation.core.ui.source.kit.atom.snackbar.rememberStackedSnackbarHostState
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
import dev.rectify.io.core.clipboard.copyToClipboard
import presentation.feature.mirror.source.mirror.MirrorSideEffect
import presentation.feature.mirror.source.mirror.MirrorHost
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorHistoryTab
import presentation.feature.mirror.source.mirror.MirrorNotesTab
import presentation.feature.mirror.source.mirror.MirrorPayloadTab
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorStageContent
import presentation.feature.mirror.source.mirror.MirrorStatusLine
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.VerifyFrames
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

/** Toolbar copy for the send action and its in-flight label (AC-17). Not localised — see below. */
private const val SEND_LABEL = "Resolve & Send"
private const val SEND_BUSY_LABEL = "Sending…"
private const val SEND_SHORTCUT = "⌘↵"

/** Clear ghost-button copy. */
private const val CLEAR_LABEL = "Clear"

/** The completion toast's action (FR-58): copies the minted session id to the clipboard. */
private const val OPEN_SESSION_LABEL = "Open session"


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

    // ⌘↩ (A11Y-2). Hoisted to the same scope as `shellState` and NOT read out of the mirror
    // state, because `onPreviewKeyEvent` below is a Window *parameter*: it is evaluated outside
    // the Window's content lambda and can see nothing composed inside it. `MirrorHost` writes
    // the action in, so the holder is non-null exactly while `canSend` is true — one predicate,
    // never a second copy of the three-clause gate.
    var sendAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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
                sendAction = sendAction,
            )
        },
    ) {
        ApplyWindowChrome()

        // The desktop root no longer goes through App(), so it must provide the locale itself —
        // without this, pref_language is inert on desktop.
        AppLocaleProvider(languageCode = languageCode) {
            AppTheme(mode = themeModel.toThemeMode()) {
                // Newly mounted here: the desktop root does not go through SafeContainer, so
                // without this the completion toast has nowhere to land (FR-18).
                val snackbarHostState = rememberStackedSnackbarHostState()
                Box(modifier = Modifier.fillMaxSize()) {
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
                                // FR-18/FR-58: the toast and the clipboard are both platform
                                // affordances, so the ViewModel asks for them rather than owning
                                // them, exactly as it does for the file dialog above.
                                is MirrorSideEffect.SendComplete -> snackbarHostState.showSendComplete(effect)
                                else -> Unit
                            }
                        },
                        // The fifth slot is a verification run's BEFORE/AFTER pair; the only
                        // surface that draws it is the verify stage, so it travels down to
                        // MirrorStageContent and nowhere else.
                    ) { mirror, frame, hierarchy, verifyFrames, onMirrorIntent ->
                        // AC-49: the first annotation of a frozen frame reveals the Notes tab, and
                        // only the first — the key stays true from the second one on, so a user who
                        // has walked over to Payload is not yanked back on every drag.
                        val hasStaged = mirror.stagedCount >= 1
                        LaunchedEffect(hasStaged) {
                            if (hasStaged) shellState.selectTab(ShellTab.Notes)
                        }

                        // One primary action at a time, never two (A11Y-2 / TAC-3). The two
                        // branches below are mutually exclusive by construction and not by
                        // luck: `canSend` is false for as long as a verify stage is up (see
                        // core/send/SendGate), which is exactly when the follow-up branch can
                        // fire. So ⌘↩ and the toolbar button always dispatch the SAME intent,
                        // and ⌘↩ is dark whenever the button is.
                        //
                        // The verify stage prints the same ⌘↩ hint on its own primary action,
                        // so the holder points at the follow-up while that stage owns the
                        // screen — the same precedence MirrorUiState.stage gives Verify over
                        // Frozen. A hint for a key that does nothing is what is avoided here.
                        val canSend = mirror.canSend
                        val followUpCount = mirror.verify?.stillOffRows?.size ?: 0
                        LaunchedEffect(canSend, followUpCount) {
                            sendAction = when {
                                followUpCount > 0 -> { { onMirrorIntent(MirrorIntent.SendFollowUp) } }
                                canSend -> { { onMirrorIntent(MirrorIntent.ResolveAndSend) } }
                                else -> null
                            }
                        }

                        RectifyShellContent(
                            state = shellState,
                            mirror = mirror,
                            frame = frame,
                            verifyFrames = verifyFrames,
                            onIntent = onMirrorIntent,
                            sidePanel = { tab ->
                                // Exhaustive by design (no `else`): the tab strip is what
                                // Cmd+1/2/3 drives, so a future ShellTab entry must fail to
                                // compile here rather than silently render nothing.
                                //
                                // `weight(1f)` on each is load-bearing, not cosmetic: each tab pins
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
                                    ShellTab.History -> MirrorHistoryTab(
                                        state = mirror,
                                        onIntent = onMirrorIntent,
                                        modifier = Modifier.weight(1f),
                                    )
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
                    // Last child, top-centre: the stacked snackbar slides in from above
                    // (`Constant.Y_TARGET_ENTER`), and it must not take hit-testing away from
                    // the stage, so it sits over everything rather than inside the shell.
                    StackedSnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}

/**
 * Shows the send-completion toast (AC-19, AC-21, FR-18, FR-58).
 *
 * Extracted so the real call site and the previews below enqueue the same toast rather than two
 * drifting approximations of it.
 *
 * **The duration is explicit and finite on purpose.** `showSnackbar` defaults to
 * [StackedSnackbarDuration.Indefinite] — taking the default would leave the confirmation on
 * screen until the user swiped it away, over the stage they just went back to working on
 * (TAC-4). [StackedSnackbarDuration.Long] (10 s) rather than `Short` (4 s) because this toast
 * carries an action: A11Y-2 asks for `Open session` to stay reachable by keyboard for the whole
 * time the toast is up, and four seconds is not that.
 *
 * @param effect The completed send, straight off the ViewModel.
 */
private fun StackedSnakbarHostState.showSendComplete(effect: MirrorSideEffect.SendComplete) {
    showSnackbar(
        // Machine text, already assembled by `core/send/toastLabel` — "Sent 3 annotations to
        // Claude Code · frame + hierarchy · 1.4s". Localising it is out of scope and recorded
        // as debt; it is also the string the user screenshots, so it reads the same everywhere.
        title = effect.summary,
        // What the action is about to put on the clipboard, shown rather than implied.
        description = effect.sessionId,
        actionTitle = OPEN_SESSION_LABEL,
        action = { copyToClipboard(effect.sessionId) },
        duration = StackedSnackbarDuration.Long,
    )
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
 * @param verifyFrames BEFORE/AFTER pair of a verification run, or `null` when there is none.
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
    verifyFrames: VerifyFrames? = null,
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
                        text = CLEAR_LABEL,
                        // Empties the staged list; the frame stays pinned until Resume live
                        // (AC-48).
                        onClick = { onIntent(MirrorIntent.ClearStaged) },
                        variant = AppButtonVariant.Ghost,
                        // Not `stagedCount > 0` any more: wiping the batch out from under the
                        // send that is assembling it is what TAC-2 closes, so Clear is dark for
                        // exactly the window Resolve & Send is.
                        enabled = mirror.canClear,
                    )
                },
                primaryAction = {
                    AppButton(
                        text = SEND_LABEL,
                        onClick = { onIntent(MirrorIntent.ResolveAndSend) },
                        shortcut = SEND_SHORTCUT,
                        enabled = mirror.canSend,
                        busy = mirror.send.isInFlight,
                        busyText = SEND_BUSY_LABEL,
                    )
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
        // AC-34's omit-at-zero rule lives in `core/history`'s historyBadgeCount, which is
        // testable; this module has no test wiring, so it only put-or-omits what it is given.
        tabCounts = buildMap {
            put(ShellTab.Notes, mirror.stagedCount)
            put(ShellTab.Payload, mirror.stagedCount)
            mirror.historyBadgeCount?.let { put(ShellTab.History, it) }
        },
        onPanelWidthCommit = onPanelWidthCommit,
        stage = {
            MirrorStageContent(
                state = mirror,
                frame = frame,
                onIntent = onIntent,
                modifier = Modifier.align(Alignment.Center),
                verifyFrames = verifyFrames,
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
 * ⌘↩ joins it on the same terms (AC-17): consumed **only** when [sendAction] is non-null, which
 * `MirrorHost` keeps in lockstep with whichever primary action is live — `ResolveAndSend` on a
 * frozen frame, `SendFollowUp` while the verify stage owns the screen, and never both, because
 * `canSend` is false for the whole of that stage. No live action therefore lets the event fall
 * through instead of swallowing a Return the rest of the window might want — the same SC-4
 * reasoning as the panel check below.
 *
 * @return `true` when the event was handled and must not propagate.
 */
private fun handleShellShortcut(
    event: KeyEvent,
    isPanelVisible: Boolean,
    onTabSelect: (ShellTab) -> Unit,
    sendAction: (() -> Unit)?,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    if (event.isMetaPressed && event.key == Key.Enter) {
        val send = sendAction ?: return false
        send()
        return true
    }
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


@Composable
private fun SendCompleteToastPreviewContent() {
    val hostState = rememberStackedSnackbarHostState()
    LaunchedEffect(Unit) {
        hostState.showSendComplete(
            MirrorSideEffect.SendComplete(
                count = 3,
                summary = "Sent 3 annotations to Claude Code · frame + hierarchy · 1.4s",
                sessionId = "emulator-5554-f3-1760784512345",
            ),
        )
    }
    Box(modifier = Modifier.background(Theme.color.bg).padding(Theme.spacing.spacingL)) {
        StackedSnackbarHost(hostState = hostState)
    }
}

@Preview
@Composable
private fun SendCompleteToastPreview() {
    AppTheme { SendCompleteToastPreviewContent() }
}

@Preview
@Composable
private fun SendCompleteToastDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) { SendCompleteToastPreviewContent() }
}
