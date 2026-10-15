package presentation.feature.mirror.source.mirror

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.core.source.model.device.PlatformModel
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import presentation.feature.mirror.core.discovery.applyDiscovery
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
 * @property decodeFrame Seam for PNG decoding — a parameter so tests need no real image.
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class MirrorViewModel(
    private val detectTools: DetectToolsUseCase,
    private val observeDiscovery: ObserveDeviceDiscoveryUseCase,
    private val bootEmulator: BootEmulatorUseCase,
    private val startMirroring: StartMirroringUseCase,
    private val observeMirrorStats: ObserveMirrorStatsUseCase,
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
            MirrorIntent.ClearStaged -> clearStaged()
            MirrorIntent.ResumeLive -> resumeLive()
            MirrorIntent.Reconnect -> reconnect()
            // Listed, not `else`-swallowed: the handlers land with the surfaces that dispatch
            // them, and an `else` here would silently absorb the next intent added too.
            MirrorIntent.RetryDiscovery -> Unit
            MirrorIntent.RestartAdb -> Unit
            MirrorIntent.LocateAdb -> Unit
            is MirrorIntent.AdbPathChosen -> Unit
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
        reduce { state.copy(platform = platform, session = MirrorSession.Idle, frozen = null) }
    }

    private fun setPickerOpen(open: Boolean) = intent {
        reduce { state.copy(isPickerOpen = open) }
    }

    private fun closePicker() = intent {
        // Escape before the first selection would leave an empty stage with no way back.
        if (state.selectedDeviceId != null) reduce { state.copy(isPickerOpen = false) }
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
        reduce { state.copy(session = MirrorSession.Idle, frozen = null) }
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
        if (currentFrozenFrame.value == null) currentFrozenFrame.value = held
        reduce {
            val frozen = state.frozen
                ?: FrozenFrame(deviceSize = IntSize(width = held.width, height = held.height))
            val staged = frozen.annotations +
                StagedAnnotation(index = frozen.annotations.size + 1, rect = rect)
            state.copy(frozen = frozen.copy(annotations = staged, selectedIndex = staged.size))
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
            state.copy(frozen = frozen.copy(annotations = renumbered, selectedIndex = selected))
        }
    }

    /** Empties the staged list. The frame stays pinned — only Resume Live unpins it (AC-48). */
    private fun clearStaged() = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            state.copy(frozen = frozen.copy(annotations = emptyList(), selectedIndex = null))
        }
    }

    /**
     * Back to the live mirror (AC-51).
     *
     * Releases the pinned bitmap and the staged list and nothing else: the session was never
     * stopped, so the next frame from the still-running capture loop simply shows up.
     */
    private fun resumeLive() = intent {
        currentFrozenFrame.value = null
        reduce { state.copy(frozen = null) }
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
