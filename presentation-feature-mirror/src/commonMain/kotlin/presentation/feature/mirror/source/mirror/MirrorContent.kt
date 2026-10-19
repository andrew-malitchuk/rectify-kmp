package presentation.feature.mirror.source.mirror

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.device.ToolModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.indicator.StatusIndicator
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState
import presentation.core.ui.source.kit.molecule.card.PlatformToolState
import presentation.core.ui.source.kit.molecule.status.StatusLine
import presentation.core.ui.source.kit.organism.picker.DevicePickerPanel
import presentation.core.ui.source.kit.organism.picker.PickerDevice
import presentation.core.ui.source.kit.organism.picker.PickerDeviceGroup
import presentation.core.ui.source.kit.organism.picker.PickerDeviceTrailing
import presentation.core.ui.source.kit.organism.picker.PickerPlatform
import presentation.core.ui.source.kit.organism.stage.NoSignalStage
import presentation.feature.mirror.core.send.sendSteps
import presentation.feature.mirror.core.send.sendTrailing
import presentation.feature.mirror.core.stats.formatStatusLine
import kotlin.time.Clock
import presentation.feature.mirror.core.discovery.errorCard
import presentation.feature.mirror.core.discovery.indicatorDot
import presentation.feature.mirror.core.discovery.dropBanner
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_action_close
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_action_start
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_group_emulator
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_group_physical
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_hint_keys
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_no_signal_label
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_picker_subtitle
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_picker_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_row_boot
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_row_running
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_row_shut_down
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_section_platform
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_section_source
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_source_summary
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_detecting
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_dumping
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_latency_placeholder
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_no_device_attached
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_awaiting_choice
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_poll_failed
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_polling_hint
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_tool_missing
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_status_waiting
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_tool_not_found
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_waiting_footer
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_waiting_step_emulator
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_waiting_step_usb
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_waiting_subtitle
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_waiting_title

/** Binary names, shown verbatim beside each platform card. Machine output, never localised. */
private const val ADB = "adb"
private const val IDB = "idb"

/**
 * Whatever belongs in the shell's stage slot right now.
 *
 * Stateless: it reads [state] and [frame] and emits intents. This is the only place that knows
 * which of the four stage surfaces a given [MirrorStage] means, so the window does not have to.
 *
 * @param state Current mirror state.
 * @param frame Latest decoded frame, from `MirrorViewModel.frame`.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 * @param verifyFrames The BEFORE/AFTER pair of a verification run, from
 *   `MirrorViewModel.verifyFrames`. Defaulted so every pre-existing call site and preview that
 *   can never reach [MirrorStage.Verify] stays as it is.
 */
@Composable
public fun MirrorStageContent(
    state: MirrorUiState,
    frame: ImageBitmap?,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
    verifyFrames: VerifyFrames? = null,
) {
    // Resolved ahead of the stage `when`, not inside its NoSignal branch. The plan said
    // NoSignal-only for the smallest blast radius, but of the four discovery states only
    // NoDeviceFound closes the picker - F-2 keeps it open for ToolMissing and PollFailed, and
    // those are confirmed ACs - so NoSignal-only routing left AC-26, AC-53 and AC-28's cards
    // unreachable. The projection itself stands down for `Found`, where the picker is the real
    // affordance, so the picker still keeps the stage wherever it has something to show.
    // ONE clock read and ONE projection per pass, shared downstream. It was being computed
    // three times — here, again inside indicatorLabel(), and a third time inside
    // indicatorDot() — with two different wall-clock reads for what is conceptually one
    // decision about one frame. The projection does string joins and list filters, so that was
    // unbounded duplicate work on every recomposition, and worse, the status line could
    // disagree with the card beside it purely because it asked a moment later.
    val nowMs = Clock.System.now().toEpochMilliseconds()
    val card = remember(state, nowMs) { state.errorCard(nowMs) }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (card != null) {
            MirrorErrorStage(card = card, onIntent = onIntent)
        } else when (state.stage) {
            MirrorStage.Picker -> MirrorPicker(state = state, onIntent = onIntent)

            // A session that has not produced its first frame leaves the stage's own dot grid
            // showing: a one-frame flash of "no device" copy would contradict the status bar.
            MirrorStage.Mirror -> if (frame != null) {
                MirrorLiveStage(
                    frame = frame,
                    deviceName = state.selectedDeviceName.orEmpty(),
                    onIntent = onIntent,
                )
            }

            // [frame] is the pinned bitmap here, not the live one: MirrorHost resolves
            // `frozenFrame ?: frame` so this branch needs no second parameter.
            MirrorStage.Frozen -> if (frame != null && state.frozen != null) {
                MirrorFrozenStage(
                    frame = frame,
                    deviceName = state.selectedDeviceName.orEmpty(),
                    frozen = state.frozen,
                    canResumeLive = state.canResumeLive,
                    banner = state.dropBanner(),
                    measure = state.measure,
                    hierarchyStatus = state.hierarchyStatus,
                    onIntent = onIntent,
                )
            }

            // `state.verify` is what selected this branch, so the let is a smart-cast
            // formality rather than a case: there is no reachable Verify stage without it.
            // [verifyFrames] IS nullable here on purpose - the run shows its progress and its
            // errors long before it has two bitmaps.
            MirrorStage.Verify -> state.verify?.let { verify ->
                MirrorVerifyStage(verify = verify, frames = verifyFrames, onIntent = onIntent)
            }

            MirrorStage.NoSignal -> NoSignalStage(
                avds = state.avds,
                selectedAvd = state.selectedAvd,
                onSelectAvd = { avd -> onIntent(MirrorIntent.SelectAvd(avd)) },
                onBoot = { onIntent(MirrorIntent.BootSelectedAvd) },
                noSignalLabel = stringResource(Res.string.mirror_no_signal_label),
                title = stringResource(Res.string.mirror_waiting_title),
                subtitle = stringResource(Res.string.mirror_waiting_subtitle),
                stepUsb = stringResource(Res.string.mirror_waiting_step_usb),
                stepEmulator = stringResource(Res.string.mirror_waiting_step_emulator),
                bootLabel = stringResource(Res.string.mirror_row_boot),
                footer = stringResource(Res.string.mirror_waiting_footer),
            )
        }
        SendStatusOverlay(state = state, modifier = Modifier.align(Alignment.TopCenter))
    }
}

/**
 * The send pipeline over the top of the stage (AC-19), or nothing while no send has run.
 *
 * The shipped [StatusLine] molecule verbatim — the whole of this composable is the projection
 * `core/send/sendSteps` + `sendTrailing` already compute and the alignment the design asks for.
 * Nothing new is drawn, which is why there is no new kit component.
 *
 * Visible whenever the phase is not [SendPhase.Idle], which includes the two TERMINAL phases:
 * the line stays up after a send so its last step reads `Sent` (green) or `Sent` (red) rather
 * than vanishing the instant the result is known. `MirrorViewModel` retires the phase back to
 * [SendPhase.Idle] at the next natural user action — a new annotation, Clear or Resume Live —
 * so this never becomes permanent, and no timer or extra intent exists to do it.
 *
 * The fade reads its duration from `Theme.motion.durations`, which `attributeMotion` has
 * already resolved to 0 ms under `Theme.motion.reducedMotion` (A11Y-6) — the branch is the
 * theme's, never a parameter of this composable's own.
 *
 * @param state Current mirror state.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
private fun SendStatusOverlay(state: MirrorUiState, modifier: Modifier = Modifier) {
    val fade = tween<Float>(durationMillis = Theme.motion.durations.fastMs)
    AnimatedVisibility(
        visible = state.send != SendPhase.Idle,
        enter = fadeIn(animationSpec = fade),
        exit = fadeOut(animationSpec = fade),
        modifier = modifier,
    ) {
        StatusLine(
            steps = sendSteps(
                phase = state.send,
                session = state.session,
                hierarchy = state.hierarchyStatus,
            ),
            trailing = sendTrailing(phase = state.send, session = state.session),
            modifier = Modifier.padding(Theme.spacing.spacingL),
        )
    }
}

/**
 * The status bar's leading slot: the pipeline indicator plus the machine stat line (AC-25).
 *
 * An inline [Row] rather than a new molecule — one surface uses it, and the part worth testing
 * is the formatting, which lives in the pure `StatsFormatter`.
 *
 * @param state Current mirror state.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun MirrorStatusLine(state: MirrorUiState, modifier: Modifier = Modifier) {
    // One clock read and one projection for this composable too. The dot and the label both
    // need the card, and asking twice let them disagree about the same frame purely because
    // one asked a moment later. `MirrorStageContent` has its own read: these are separate
    // render scopes, and threading a parameter down from the window layer would widen a public
    // API to save a list filter.
    val nowMs = Clock.System.now().toEpochMilliseconds()
    val card = remember(state, nowMs) { state.errorCard(nowMs) }
    val line = formatStatusLine(
        stats = state.stats,
        toolName = ADB,
        pollingHint = stringResource(Res.string.mirror_status_polling_hint),
        latencyPlaceholder = stringResource(Res.string.mirror_status_latency_placeholder),
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
    ) {
        StatusIndicator(state = state.indicatorState(card), label = state.indicatorLabel(card))
        if (line.isNotEmpty()) {
            Text(text = line, style = Theme.typography.monoMeta, color = Theme.color.ink2, maxLines = 1)
        }
    }
}

/**
 * Dot colour of the status indicator: live only while frames are actually arriving.
 *
 * Frozen is checked ahead of Streaming — the session really is still running underneath (FR-30),
 * but the pixels on screen are pinned, and a LIVE dot over a still frame is a lie. This is the
 * first production caller of the shipped-but-unused [StatusIndicatorState.Frozen].
 */
private fun MirrorUiState.indicatorState(card: MirrorErrorCard?): StatusIndicatorState =
    indicatorDot(card)

@Composable
private fun MirrorUiState.indicatorLabel(card: MirrorErrorCard?): String = when {
    // Above the frozen case, in the same order `indicatorDot` puts it: T-10 gave the DOT a
    // Dumping branch and left the label beside it saying FROZEN, which is the status line
    // contradicting its own indicator for the ~2.7 s the dump takes (AC-72).
    hierarchyStatus == HierarchyStatus.Dumping -> stringResource(Res.string.mirror_status_dumping)
    // Machine state, not prose — uppercased from the enum exactly as the LIVE label is.
    frozen != null -> StatusIndicatorState.Frozen.name.uppercase()
    session is MirrorSession.Streaming -> StatusIndicatorState.Live.name.uppercase()
    discovery is DiscoveryModel.ToolMissing -> stringResource(Res.string.mirror_status_tool_missing)
    // A failed poll is its own label: "waiting for device" would claim adb answered.
    discovery is DiscoveryModel.PollFailed -> stringResource(Res.string.mirror_status_poll_failed)
    discovery is DiscoveryModel.Detecting -> stringResource(Res.string.mirror_status_detecting)
    // The design pairs each error card with its own status line. Without these two the bar
    // kept saying "Waiting for device…" beside a card explaining that adb saw nothing, or
    // that three devices were attached and it needed a choice.
    // COR-5. The count is READ, not hardcoded. attachedCount is the number of attached but
    // unusable devices, so a single unauthorised handset made the card footer say "1 attached"
    // while the line beside it said "0 attached" — the exact contradiction T-32 existed to
    // remove, reintroduced two commits later by a literal in a string resource.
    discovery is DiscoveryModel.NoDeviceFound ->
        stringResource(
            Res.string.mirror_status_no_device_attached,
            (discovery as DiscoveryModel.NoDeviceFound).attachedCount,
        )
    card?.kind == MirrorErrorKind.MultipleDevices ->
        stringResource(
            Res.string.mirror_status_awaiting_choice,
            rows.count { it.group == MirrorRowGroup.Physical && it.selectable },
        )
    else -> stringResource(Res.string.mirror_status_waiting)
}

@Composable
private fun MirrorPicker(state: MirrorUiState, onIntent: (MirrorIntent) -> Unit) {
    val bootLabel = stringResource(Res.string.mirror_row_boot)
    val runningLabel = stringResource(Res.string.mirror_row_running)
    val shutDownLabel = stringResource(Res.string.mirror_row_shut_down)
    DevicePickerPanel(
        platforms = state.platformCards(),
        selectedPlatformId = state.platform.name,
        onPlatformClick = { id -> onIntent(MirrorIntent.SelectPlatform(PlatformModel.valueOf(id))) },
        devices = state.rows.map { row -> row.toPickerDevice(bootLabel, runningLabel, shutDownLabel) },
        selectedDeviceId = state.highlightedRow?.id,
        onDeviceClick = { id -> onIntent(MirrorIntent.HighlightRow(id)) },
        onDeviceAction = { id -> onIntent(MirrorIntent.BootRow(id)) },
        onMoveSelection = { delta -> onIntent(MirrorIntent.MoveHighlight(delta)) },
        onStart = { onIntent(MirrorIntent.Commit) },
        title = stringResource(Res.string.mirror_picker_title),
        subtitle = stringResource(Res.string.mirror_picker_subtitle),
        platformSectionLabel = stringResource(Res.string.mirror_section_platform),
        sourceSectionLabel = stringResource(Res.string.mirror_section_source),
        sourceSummary = stringResource(Res.string.mirror_source_summary, state.rows.size),
        physicalGroupLabel = stringResource(Res.string.mirror_group_physical),
        emulatorGroupLabel = stringResource(Res.string.mirror_group_emulator),
        keyHint = stringResource(Res.string.mirror_hint_keys),
        startLabel = stringResource(Res.string.mirror_action_start),
        closeLabel = stringResource(Res.string.mirror_action_close),
        // Dismissable only once a session exists behind the panel; the reducer enforces the
        // same rule, so a stray esc cannot strand the user on an empty stage (A11Y-4).
        onClose = { onIntent(MirrorIntent.ClosePicker) }.takeIf { state.isPickerDismissable },
    )
}

/**
 * The two platform cards.
 *
 * A tool that is not installed still gets a card saying so — "adb not found" is the answer to
 * "why is the list empty", and hiding the platform would hide the answer.
 */
@Composable
private fun MirrorUiState.platformCards(): List<PickerPlatform> {
    val notFound = stringResource(Res.string.mirror_tool_not_found)
    return PlatformModel.entries.map { platform ->
        val binary = if (platform == PlatformModel.Android) ADB else IDB
        val tool = tools.firstOrNull { it.platform == platform }
        PickerPlatform(
            id = platform.name,
            title = if (platform == PlatformModel.Android) "Android" else "iOS",
            detail = tool.toDetail(binary, notFound),
            toolState = if (tool?.isInstalled == true) PlatformToolState.Found else PlatformToolState.Missing,
            // Both platforms are selectable as of F-10. Deliberately NOT gated on the probe
            // either: picking a platform whose tool is missing is how the user reaches the
            // ToolMissing card that tells them what to install, and disabling the card would
            // hide the only answer to "why is this list empty".
        )
    }
}

private fun ToolModel?.toDetail(binary: String, notFoundLabel: String): String = when {
    this == null || !isInstalled -> "$binary $notFoundLabel"
    else -> listOfNotNull(version?.let { "$binary $it" } ?: binary, path).joinToString(" · ")
}

private fun MirrorRow.toPickerDevice(
    bootLabel: String,
    runningLabel: String,
    shutDownLabel: String,
): PickerDevice = PickerDevice(
    id = id,
    name = name,
    serial = serial ?: shutDownLabel.takeIf { kind == MirrorRowKind.Boot },
    group = when (group) {
        MirrorRowGroup.Physical -> PickerDeviceGroup.Physical
        MirrorRowGroup.Emulator -> PickerDeviceGroup.Emulator
    },
    trailing = when (kind) {
        MirrorRowKind.Detail -> PickerDeviceTrailing.Detail(detail)
        MirrorRowKind.Running -> PickerDeviceTrailing.Running(runningLabel)
        MirrorRowKind.Boot -> PickerDeviceTrailing.Boot(bootLabel)
    },
    selectable = selectable,
)

@Preview
@Composable
private fun MirrorStageContentPickerPreview() {
    AppTheme {
        MirrorStageContent(
            state = MirrorUiState(
                tools = listOf(
                    ToolModel(PlatformModel.Android, isInstalled = true, version = "35.0.1", path = "/opt/homebrew/bin"),
                    ToolModel(PlatformModel.Ios, isInstalled = false),
                ),
                rows = listOf(
                    MirrorRow(
                        id = "37281FDH2000AB",
                        name = "Pixel 8 Pro",
                        serial = "37281FDH2000AB",
                        group = MirrorRowGroup.Physical,
                        kind = MirrorRowKind.Detail,
                        detail = "Android 15 · USB",
                    ),
                    MirrorRow(
                        id = "Pixel_Fold_API_35",
                        name = "Pixel_Fold_API_35",
                        serial = null,
                        group = MirrorRowGroup.Emulator,
                        kind = MirrorRowKind.Boot,
                        avdName = "Pixel_Fold_API_35",
                    ),
                ),
            ),
            frame = null,
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun MirrorStageContentNoSignalPreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.stage)) {
            MirrorStageContent(
                state = MirrorUiState(isPickerOpen = false, avds = listOf("Pixel_7_API_34")),
                frame = null,
                onIntent = {},
            )
        }
    }
}

@Preview
@Composable
private fun MirrorStatusLinePreview() {
    AppTheme {
        Box(modifier = Modifier.background(Theme.color.chrome).padding(Theme.spacing.spacingS)) {
            MirrorStatusLine(
                state = MirrorUiState(
                    isPickerOpen = false,
                    session = MirrorSession.Streaming("37281FDH2000AB"),
                    stats = MirrorStatsModel(
                        toolVersion = "35.0.1",
                        widthPx = 1080,
                        heightPx = 2400,
                        fps = 4.2f,
                    ),
                ),
            )
        }
    }
}

@Composable
private fun SendStatusOverlayPreviewContent() {
    Box(modifier = Modifier.background(Theme.color.stage)) {
        MirrorStageContent(
            state = MirrorUiState(
                isPickerOpen = false,
                session = MirrorSession.Streaming("37281FDH2000AB"),
                hierarchyStatus = HierarchyStatus.Ready,
                frozen = FrozenFrame(
                    deviceSize = IntSize(width = 1080, height = 2400),
                    annotations = listOf(
                        StagedAnnotation(index = 1, rect = DeviceRect(120, 420, 960, 674)),
                    ),
                ),
                send = SendPhase.Publishing,
            ),
            frame = null,
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun SendStatusOverlayPreview() {
    AppTheme { SendStatusOverlayPreviewContent() }
}

@Preview
@Composable
private fun SendStatusOverlayDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) { SendStatusOverlayPreviewContent() }
}
