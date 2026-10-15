package presentation.feature.mirror.source.mirror

import androidx.compose.ui.graphics.ImageBitmap
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
        }
    }

    private fun detectInstalledTools() = intent {
        // A missing binary is a value, not a failure: the probe list still names both
        // platforms so the picker can say "adb not found" instead of showing nothing.
        val tools = detectTools().getOrNull().orEmpty()
        reduce { state.copy(tools = tools) }
    }

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
        reduce { state.copy(platform = platform, session = MirrorSession.Idle) }
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
        reduce { state.copy(session = MirrorSession.Idle) }
    }

    /**
     * Launches an AVD. The image takes tens of seconds to appear; the next discovery poll is
     * what puts it in the list, so there is nothing to wait for here.
     */
    private suspend fun Syntax<MirrorUiState, MirrorSideEffect>.boot(avdName: String) {
        bootEmulator(avdName).onFailure { postSideEffect(MirrorSideEffect.BootFailed(avdName)) }
    }
}
