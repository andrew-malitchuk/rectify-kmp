package presentation.feature.mirror.source.mirror

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.PlatformModel
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.DumpHierarchyUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlin.time.Clock
import presentation.feature.mirror.core.discovery.errorCard
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import presentation.feature.mirror.core.discovery.applyDiscovery
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.core.measure.projectMeasureNodes
import presentation.feature.mirror.core.measure.replaceNoteLine
import presentation.feature.mirror.core.picker.nextSelectableIndex

/**
 * Shortest gap between two reduced stats updates. AC-25 asks for "at least once a second";
 * the transport reports far more often than that, and every extra reduction is a full state
 * copy nobody can read.
 */
private const val STATS_TICK_MS = 1_000L

/** Decodes a captured PNG off the main thread, or `null` when the bytes are not an image. */
private suspend fun decodePng(bytes: ByteArray): ImageBitmap? =
    withContext(Dispatchers.Default) { runCatching { bytes.decodeToImageBitmap() }.getOrNull() }

/**
 * The mirror surface's state machine.
 *
 * Two deliberate splits make this cheap at ten frames a second:
 *
 * - **Pixels bypass Orbit.** Decoded frames are published on [frame], a plain `StateFlow`
 *   outside the reduced state. A `ByteArray`-bearing type compares by identity, so putting one
 *   in [MirrorUiState] would mean a full state copy per frame that could never deduplicate.
 * - **Stats are throttled, not streamed.** The transport reports per frame; the reducer sees
 *   at most one update a second.
 *
 * Discovery and the capture loop are mutually exclusive: [sessionDeviceId] gates both through
 * `flatMapLatest`, so polling pauses while a session streams (one `adb` conversation at a
 * time) and rapid start/stop can never leave two capture loops alive.
 *
 * @property detectTools Launch-time probe of the platform binaries.
 * @property observeDiscovery The 2 s discovery poll; its cadence lives in the data layer.
 * @property bootEmulator Launches a cold AVD.
 * @property startMirroring Opens a capture session on a device.
 * @property observeMirrorStats Live transport statistics.
 * @property refreshDiscovery Cuts the poll interval short so a Retry acts now.
 * @property restartAdbServer Kills and restarts the adb daemon — AC-27's primary action, and
 *   the only thing that clears a WEDGED daemon (`start-server` alone hangs against one).
 * @property setAdbPath Persists the `adb` the user pointed Rectify at (AC-26).
 * @property dumpHierarchy Captures the view hierarchy to measure against. Fired once per
 *   freeze, never per keystroke: T-1 measured the dump at ~2.7 s, so asking for one when
 *   Option goes down would make measure mode unusable (NFR-2).
 * @property decodeFrame Seam for PNG decoding — a parameter so tests need no real image.
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class MirrorViewModel(
    private val detectTools: DetectToolsUseCase,
    private val observeDiscovery: ObserveDeviceDiscoveryUseCase,
    private val bootEmulator: BootEmulatorUseCase,
    private val startMirroring: StartMirroringUseCase,
    private val observeMirrorStats: ObserveMirrorStatsUseCase,
    private val restartAdbServer: RestartAdbServerUseCase,
    private val refreshDiscovery: RefreshDiscoveryUseCase,
    private val setAdbPath: SetAdbPathUseCase,
    private val dumpHierarchy: DumpHierarchyUseCase,
    private val decodeFrame: suspend (ByteArray) -> ImageBitmap? = ::decodePng,
) : ContainerHost<MirrorUiState, MirrorSideEffect>, ViewModel() {

    /** `null` while nothing streams — the gate both discovery and the capture loop read. */
    private val sessionDeviceId = MutableStateFlow<String?>(null)

    private val currentFrame = MutableStateFlow<ImageBitmap?>(null)

    /** The latest decoded frame. Deliberately not part of [MirrorUiState]. */
    public val frame: StateFlow<ImageBitmap?> = currentFrame.asStateFlow()

    private val currentFrozenFrame = MutableStateFlow<ImageBitmap?>(null)

    /**
     * The pinned frame, or `null` while the stage is live.
     *
     * A second `StateFlow` beside [frame] rather than a field of [MirrorUiState], for the same
     * reason [frame] is one: an `ImageBitmap` compares by identity, so a state copy carrying it
     * could never deduplicate. The reducer keeps only its [IntSize], in
     * [MirrorUiState.frozen]. The annotation list is the opposite case and does live in state.
     */
    public val frozenFrame: StateFlow<ImageBitmap?> = currentFrozenFrame.asStateFlow()

    private val currentFrozenHierarchy = MutableStateFlow(HierarchySnapshotModel.Empty)

    /**
     * The hierarchy captured for the pinned frame, or [HierarchySnapshotModel.Empty].
     *
     * A third `StateFlow` beside [frame] and [frozenFrame], for a different reason than theirs:
     * a dump is a few hundred nodes read only by `core/measure`'s arithmetic and never rendered,
     * so reducing it would copy a list nobody reads on every unrelated state change.
     * [MirrorUiState.hierarchyStatus] is the part the UI does read, and that IS reduced.
     *
     * Never `null`: [HierarchySnapshotModel.Empty] already means "nothing usable", and
     * `projectMeasureNodes` turns it into the `View (no match)` fallback that FR-41 asks for.
     */
    public val frozenHierarchy: StateFlow<HierarchySnapshotModel> =
        currentFrozenHierarchy.asStateFlow()

    /** The dump in flight, so a clear site can cancel it instead of reducing a stale verdict. */
    private var dumpJob: Job? = null

    override val container: Container<MirrorUiState, MirrorSideEffect> =
        container(MirrorUiState()) {
            detectInstalledTools()
            observeDiscoveryWhileIdle()
            observeFrames()
            observeStats()
        }

    /** Dispatches a user action. */
    public fun handleIntent(intent: MirrorIntent) {
        when (intent) {
            is MirrorIntent.SelectPlatform -> selectPlatform(intent.platform)
            MirrorIntent.OpenPicker -> setPickerOpen(true)
            MirrorIntent.ClosePicker -> closePicker()
            is MirrorIntent.HighlightRow -> highlightRow(intent.id)
            is MirrorIntent.MoveHighlight -> moveHighlight(intent.delta)
            MirrorIntent.Commit -> commit()
            is MirrorIntent.BootRow -> bootRow(intent.id)
            is MirrorIntent.SelectAvd -> selectAvd(intent.avdName)
            MirrorIntent.BootSelectedAvd -> bootSelectedAvd()
            MirrorIntent.StopMirroring -> stopSession()
            is MirrorIntent.StageAnnotation -> stageAnnotation(intent.rect)
            is MirrorIntent.SelectAnnotation -> selectAnnotation(intent.index)
            is MirrorIntent.CommentAnnotation -> commentAnnotation(intent.index, intent.comment)
            is MirrorIntent.DeleteAnnotation -> deleteAnnotation(intent.index)
            is MirrorIntent.StartMeasure -> startMeasure(intent.index)
            MirrorIntent.EndMeasure -> endMeasure()
            MirrorIntent.ClearStaged -> clearStaged()
            MirrorIntent.ResumeLive -> resumeLive()
            MirrorIntent.Reconnect -> reconnect()
            MirrorIntent.RetryDiscovery -> retryDiscovery()
            MirrorIntent.RestartAdb -> restartAdb()
            MirrorIntent.LocateAdb -> locateAdb()
            is MirrorIntent.AdbPathChosen -> adbPathChosen(intent.path)
        }
    }

    private fun detectInstalledTools() = intent {
        // A missing binary is a value, not a failure: the probe list still names both
        // platforms so the picker can say "adb not found" instead of showing nothing.
        val tools = detectTools().getOrNull().orEmpty()
        reduce { state.copy(tools = tools) }
    }

    /**
     * No `.catch` here, unlike [observeFrames], and the asymmetry is deliberate: `catch` is
     * terminal, so guarding the discovery stream at this end would render one failure and
     * then stop polling for the life of the process. The guard sits inside the repository's
     * poll loop, which is the only place that can report the failure AND try again two
     * seconds later; a `DeviceCommandException` therefore arrives here as a
     * `DiscoveryModel.PollFailed` value. [observeFrames] is the opposite case - a lost
     * device really does end that session, so terminating is the right behaviour there.
     */
    private fun observeDiscoveryWhileIdle() {
        viewModelScope.launch {
            sessionDeviceId
                .flatMapLatest { device -> if (device == null) observeDiscovery() else emptyFlow() }
                .collect { discovery -> intent { reduce { state.applyDiscovery(discovery) } } }
        }
    }

    private fun observeFrames() {
        viewModelScope.launch {
            sessionDeviceId
                .flatMapLatest { device ->
                    if (device == null) {
                        // Clears the stage when a session ends, without a second signal path.
                        flowOf(null)
                    } else {
                        startMirroring(device)
                            .map { captured -> decodeFrame(captured.bytes) }
                            .catch { onSessionLost(device) }
                    }
                }
                .collect { bitmap -> currentFrame.value = bitmap }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            observeMirrorStats()
                .conflate()
                .collect { stats ->
                    intent { reduce { state.copy(stats = stats) } }
                    // Throttles by sleeping the collector: conflate keeps only the newest
                    // report, so this is a ticker without an always-pending timer.
                    delay(STATS_TICK_MS)
                }
        }
    }

    private suspend fun onSessionLost(deviceId: String) {
        intent { reduce { state.copy(session = MirrorSession.Lost(deviceId)) } }
        sessionDeviceId.value = null
    }

    private fun selectPlatform(platform: PlatformModel) = intent {
        // iOS is detected but not mirrorable this release, so switching platform must stop the
        // running session rather than leave a frozen frame on the stage.
        sessionDeviceId.value = null
        currentFrozenFrame.value = null
        releaseHierarchy()
        reduce {
            state.copy(platform = platform, session = MirrorSession.Idle, frozen = null)
                .withoutMeasurements()
        }
    }

    private fun setPickerOpen(open: Boolean) = intent {
        reduce { state.copy(isPickerOpen = open) }
    }

    private fun closePicker() = intent {
        // Escape before the first selection would leave an empty stage with no way back — that
        // is why this is a no-op rather than a plain toggle, and F-2 confirmed it.
        //
        // COR-2 narrows it rather than removing it. The "no way back" premise assumed the stage
        // behind the picker is empty; with an error card available it is not. Dismissing the
        // picker on two attached handsets now reveals AC-28's ambiguity card, which HAS a route
        // onward: its primary action mirrors the top device, and the toolbar chip reopens the
        // picker (AC-43). Without this the card was unreachable in AC-28's own scenario — the
        // only way to close the picker was to resolve the ambiguity first, which is the thing
        // the card exists to help with.
        //
        // The original guard still holds where it was written for: no selection AND no card
        // means there is genuinely nothing behind the picker, so Escape still does nothing.
        // Asked counterfactually — "would a card be there if I closed?" — because `errorCard()`
        // yields to an OPEN picker, so asking it directly here always answers null and the
        // guard could never open.
        val somethingIsBehindIt = state.selectedDeviceId != null ||
            state.copy(isPickerOpen = false)
                .errorCard(Clock.System.now().toEpochMilliseconds()) != null
        if (somethingIsBehindIt) reduce { state.copy(isPickerOpen = false) }
    }

    private fun highlightRow(id: String) = intent {
        val index = state.rows.indexOfFirst { it.id == id && it.selectable }
        if (index >= 0) reduce { state.copy(highlightedIndex = index) }
    }

    private fun moveHighlight(delta: Int) = intent {
        val index = nextSelectableIndex(state.rows, state.highlightedIndex, delta)
        reduce { state.copy(highlightedIndex = index) }
    }

    private fun commit() = intent {
        val row = state.highlightedRow?.takeIf { it.selectable } ?: return@intent
        when (row.kind) {
            MirrorRowKind.Boot -> boot(row.avdName ?: row.id)
            MirrorRowKind.Detail, MirrorRowKind.Running -> {
                reduce {
                    state.copy(
                        isPickerOpen = false,
                        selectedDeviceId = row.id,
                        selectedDeviceName = row.name,
                        session = MirrorSession.Streaming(row.id),
                    )
                }
                sessionDeviceId.value = row.id
            }
        }
    }

    private fun bootRow(id: String) = intent {
        val row = state.rows.firstOrNull { it.id == id } ?: return@intent
        boot(row.avdName ?: row.id)
    }

    private fun selectAvd(avdName: String) = intent {
        reduce { state.copy(selectedAvd = avdName) }
    }

    private fun bootSelectedAvd() = intent {
        val avd = state.selectedAvd ?: state.avds.firstOrNull() ?: return@intent
        boot(avd)
    }

    private fun stopSession() = intent {
        sessionDeviceId.value = null
        currentFrozenFrame.value = null
        releaseHierarchy()
        reduce { state.copy(session = MirrorSession.Idle, frozen = null).withoutMeasurements() }
    }

    /**
     * Commits a finished drag, freezing the frame on the first one (AC-6).
     *
     * `currentFrame.value` is read **exactly once, here, at release** — that single read IS
     * AC-47: nothing is buffered at drag start, so a frame that lands mid-drag is the one that
     * gets pinned. The freeze is guarded on the bitmap being absent, which is what makes a
     * second drag add an annotation against the SAME pinned frame instead of re-freezing
     * (FR-31 / AC-52).
     *
     * Nothing here touches [sessionDeviceId]: freezing is a render decision, the capture loop
     * keeps running (FR-30), and that is why Resume Live costs no reconnection.
     *
     * This and every other annotation handler read `state.frozen` **inside** `reduce`. Orbit
     * launches each intent in its own coroutine, so a read taken outside is a pre-image two
     * overlapping intents can both capture, and the later `reduce` then writes back a copy
     * that never saw the earlier one — a per-keystroke comment racing a delete resurrects the
     * annotation the user just removed. Inside `reduce` the read is safe for a concrete
     * reason: Orbit reduces with `MutableStateFlow.update`, a CAS loop that re-runs the
     * lambda against the winning value whenever it loses the race.
     */
    private fun stageAnnotation(rect: DeviceRect) = intent {
        val held = currentFrame.value ?: return@intent
        val freezing = currentFrozenFrame.value == null
        if (freezing) currentFrozenFrame.value = held
        // Only a freeze starts a dump, and only when there is a device to ask: a second drag
        // annotates the SAME pinned frame (FR-31), whose hierarchy is already captured.
        val dumpTarget = state.selectedDeviceId?.takeIf { freezing }
        reduce {
            val frozen = state.frozen
                ?: FrozenFrame(deviceSize = IntSize(width = held.width, height = held.height))
            val staged = frozen.annotations +
                StagedAnnotation(index = frozen.annotations.size + 1, rect = rect)
            state.copy(
                frozen = frozen.copy(annotations = staged, selectedIndex = staged.size),
                // Reduced in the SAME pass as the freeze, so the status line can never show a
                // frozen frame with no dump accounted for (TAC-22).
                hierarchyStatus =
                    if (dumpTarget == null) state.hierarchyStatus else HierarchyStatus.Dumping,
            )
        }
        if (dumpTarget != null) dumpHierarchyFor(dumpTarget)
    }

    /**
     * Captures the hierarchy behind the just-pinned frame, **fire and forget**.
     *
     * Launched on [viewModelScope] rather than awaited inside the freeze's own intent: the dump
     * costs ~2.7 s (measured, T-1) and the user's freeze has to be instant, so the result lands
     * behind a frame that is already on screen. The outcome is never discarded — success
     * reduces [HierarchyStatus.Ready], any failure reduces [HierarchyStatus.Unavailable], so the
     * status cannot be left stuck at [HierarchyStatus.Dumping].
     *
     * A dump that is still in flight when the frame is released is cancelled by
     * [releaseHierarchy] instead of being allowed to reduce a verdict about a frame that is gone.
     *
     * A dump whose own frame size disagrees with the frozen frame also reduces
     * [HierarchyStatus.Unavailable], even though the command itself succeeded.
     * [projectMeasureNodes] discards such a snapshot wholesale — the T-1 spike measured a 9.76 %
     * gap between what `uiautomator` reports and the physical screen, so numbers taken from a
     * mismatched hierarchy would be confidently wrong rather than absent. Reducing `Ready` here
     * would then leave the status claiming a usable hierarchy while every measure silently fell
     * back to `View (no match)` — the status contradicting its own behaviour, which is the defect
     * AC-72 and the `Dumping` label both exist to prevent.
     */
    private fun dumpHierarchyFor(deviceId: String) {
        dumpJob?.cancel()
        dumpJob = viewModelScope.launch {
            val snapshot = dumpHierarchy(deviceId).getOrNull()
            currentFrozenHierarchy.value = snapshot ?: HierarchySnapshotModel.Empty
            intent {
                reduce {
                    val usable = snapshot != null &&
                        projectMeasureNodes(snapshot, state.frozen?.deviceSize ?: IntSize.Zero)
                            .isNotEmpty()
                    state.copy(
                        hierarchyStatus =
                            if (usable) HierarchyStatus.Ready else HierarchyStatus.Unavailable,
                    )
                }
            }
        }
    }

    /** Drops the captured hierarchy and any dump still running for a frame being released. */
    private fun releaseHierarchy() {
        dumpJob?.cancel()
        dumpJob = null
        currentFrozenHierarchy.value = HierarchySnapshotModel.Empty
    }

    /** The reduced half of [releaseHierarchy] — no frame means nothing to measure against. */
    private fun MirrorUiState.withoutMeasurements(): MirrorUiState =
        copy(measure = null, hierarchyStatus = HierarchyStatus.Absent)

    /**
     * Measures one annotation against the captured hierarchy (FR-36).
     *
     * Delegates the whole computation to `core/measure`'s pure [measure]: every rule about
     * snapping, neighbours, gaps, units, suppression and findings is decided there, where it is
     * testable without a ViewModel, and this handler only stores the result.
     *
     * An index that is no longer staged measures nothing rather than failing — the annotation
     * can be deleted between Option-down and Option-up.
     */
    private fun startMeasure(index: Int) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            val annotation = frozen.annotations.firstOrNull { it.index == index }
                ?: return@reduce state
            val snapshot = currentFrozenHierarchy.value
            state.copy(
                measure = measure(
                    annotation = annotation.rect,
                    index = index,
                    nodes = projectMeasureNodes(snapshot, frozen.deviceSize),
                    frameSize = frozen.deviceSize,
                    densityDpi = snapshot.densityDpi,
                ),
            )
        }
    }

    /**
     * Option released: the readout's `⌥` line goes into the annotation's note (FR-40).
     *
     * `replaceNoteLine` is what keeps re-measuring idempotent — exactly one glyph-prefixed line
     * survives, and the user's own prose is untouched (TAC-21).
     *
     * No readout means nothing was measured, so this is a no-op rather than an empty write; a
     * readout whose annotation has vanished leaves the state alone for the same reason.
     */
    private fun endMeasure() = intent {
        reduce {
            val readout = state.measure ?: return@reduce state
            val frozen = state.frozen ?: return@reduce state
            if (frozen.annotations.none { it.index == readout.annotationIndex }) return@reduce state
            val staged = frozen.annotations.map { annotation ->
                if (annotation.index != readout.annotationIndex) {
                    annotation
                } else {
                    annotation.copy(comment = replaceNoteLine(annotation.comment, readout.noteLine))
                }
            }
            state.copy(frozen = frozen.copy(annotations = staged), measure = null)
        }
    }

    private fun selectAnnotation(index: Int?) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            state.copy(frozen = frozen.copy(selectedIndex = index))
        }
    }

    private fun commentAnnotation(index: Int, comment: String) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            val staged = frozen.annotations.map { annotation ->
                if (annotation.index == index) annotation.copy(comment = comment) else annotation
            }
            state.copy(frozen = frozen.copy(annotations = staged))
        }
    }

    /**
     * Drops one annotation and renumbers the survivors so the badges stay contiguous (AC-8).
     *
     * The selection is resolved by following the selected annotation itself through the
     * renumbering rather than by arithmetic on the index: deleting the selected one clears the
     * popover, deleting anything else leaves it open on the same annotation under its new
     * number.
     */
    private fun deleteAnnotation(index: Int) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            val kept = frozen.annotations.filterNot { it.index == index }
            val renumbered = kept.mapIndexed { position, annotation ->
                annotation.copy(index = position + 1)
            }
            val selected = frozen.selected
                ?.takeIf { it.index != index }
                ?.let { annotation -> kept.indexOf(annotation).takeIf { it >= 0 }?.plus(1) }
            // The readout names its annotation by index and the survivors renumber, so the
            // index it holds now points at a DIFFERENT annotation. Dropping it is what stops an
            // EndMeasure that lost the race to a delete writing the note onto the survivor that
            // inherited the number.
            state.copy(
                frozen = frozen.copy(annotations = renumbered, selectedIndex = selected),
                measure = null,
            )
        }
    }

    /** Empties the staged list. The frame stays pinned — only Resume Live unpins it (AC-48). */
    private fun clearStaged() = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            state.copy(
                frozen = frozen.copy(annotations = emptyList(), selectedIndex = null),
                measure = null,
            )
        }
    }

    /**
     * Back to the live mirror (AC-51).
     *
     * Releases the pinned bitmap and the staged list and nothing else: the session was never
     * stopped, so the next frame from the still-running capture loop simply shows up.
     */
    /**
     * AC-26's secondary and AC-53's primary — FR-37.
     *
     * Asks the repository's poll loop to skip the rest of its 2 s wait, so the Retry acts now
     * instead of looking identical to a dead button for up to two seconds.
     *
     * The trigger lives in the data layer rather than here on purpose. Restarting the
     * collection from this end was tried three ways and withdrawn: it had to be threaded
     * through the discovery gate F-2 shipped and confirmed, and it proved timing-dependent
     * (see execution/GAP-fr37-immediate-repoll.md). Beside the delay it shortens, it is
     * covered by virtual-time tests instead of Orbit's dispatcher scheduling.
     */
    private fun retryDiscovery() = intent {
        // No `.onFailure` here, and the asymmetry with restartAdb/adbPathChosen is deliberate:
        // refreshDiscovery bottoms out in `Channel.trySend`, which cannot throw, so a failure
        // branch would be a case the running app can never reach — and a test for it could
        // only be written by faking the use case, which would prove nothing about this code.
        // The sibling actions DO have real throwing paths (start/kill-server, a settings
        // write), which is why they report.
        refreshDiscovery()
    }

    /**
     * AC-27's primary. A real daemon restart, then an immediate re-poll.
     *
     * Deliberately NOT an alias of [retryDiscovery]: a button labelled "Restart adb" that only
     * re-polls is the lying copy `presentation-core-ui/CLAUDE.md` forbids, and measurement
     * makes it concrete — `start-server` alone cannot clear a wedged daemon, so re-polling
     * would be unable to fix the very state the user presses it in.
     *
     * The re-poll is awaited after the restart, not before: polling into a daemon that is
     * still coming up would spend the poll's budget and report the same failure again.
     */
    private fun restartAdb() = intent {
        // COR-7. The Optional is read, not discarded. Without this an unmounted SDK made the
        // primary action silently do nothing, which is exactly the dead button FR-37 forbids —
        // and the repository throws here specifically so this layer can say so.
        restartAdbServer()
            .onSuccess { refreshDiscovery() }
            .onFailure { postSideEffect(MirrorSideEffect.RecoveryFailed(RecoveryAction.RestartAdb)) }
    }

    /**
     * AC-26's primary. Asks the window layer for a file, since only it owns a file dialog.
     *
     * The chosen path arrives back as [MirrorIntent.AdbPathChosen]; nothing is validated here
     * (see [adbPathChosen]).
     */
    private fun locateAdb() = intent {
        postSideEffect(MirrorSideEffect.LocateAdb)
    }

    /**
     * Stores the path the dialog returned and immediately re-probes.
     *
     * No validation at this end on purpose: the path is checked against the filesystem by
     * `ToolResolver`'s existing `isExecutable` predicate, the same one every other candidate
     * passes through, so a path that is missing, a directory, or not executable simply falls
     * through to the ordered candidate list. Validating here as well would be a second
     * definition of "usable", free to drift from the one that actually decides.
     */
    private fun adbPathChosen(path: String) = intent {
        // Re-probe only if the path actually persisted: polling against a preference that was
        // not written would re-render the same ToolMissing card and read as the dialog having
        // been cancelled.
        setAdbPath(path)
            .onSuccess { refreshDiscovery() }
            .onFailure { postSideEffect(MirrorSideEffect.RecoveryFailed(RecoveryAction.SaveAdbPath)) }
    }

    private fun resumeLive() = intent {
        currentFrozenFrame.value = null
        releaseHierarchy()
        reduce { state.copy(frozen = null).withoutMeasurements() }
    }

    /**
     * Re-opens the capture session on the device that dropped (FR-35 / AC-54).
     *
     * The sibling of [resumeLive], not a variant of it: this one touches neither
     * [currentFrozenFrame] nor `state.frozen`, so the pinned pixels and every staged
     * annotation survive the reconnect. That is the whole of NFR-6 — the drop itself already
     * preserved them ([onSessionLost] only moves the session to [MirrorSession.Lost]), and
     * before this handler existed the only action on screen was the one that destroys them.
     *
     * Writing [sessionDeviceId] is what actually restarts the transport: `observeFrames`'
     * `flatMapLatest` gate re-subscribes `startMirroring`, and `observeDiscoveryWhileIdle`
     * stops polling again. The id comes from [MirrorSession.Lost] rather than from a new
     * state field, which is why nothing had to be remembered for this.
     */
    private fun reconnect() = intent {
        val lost = state.session as? MirrorSession.Lost ?: return@intent
        reduce { state.copy(session = MirrorSession.Streaming(lost.deviceId)) }
        sessionDeviceId.value = lost.deviceId
    }

    /**
     * Launches an AVD. The image takes tens of seconds to appear; the next discovery poll is
     * what puts it in the list, so there is nothing to wait for here.
     */
    private suspend fun Syntax<MirrorUiState, MirrorSideEffect>.boot(avdName: String) {
        bootEmulator(avdName).onFailure { postSideEffect(MirrorSideEffect.BootFailed(avdName)) }
    }
}
