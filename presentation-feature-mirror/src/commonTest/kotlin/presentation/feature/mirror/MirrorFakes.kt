package presentation.feature.mirror

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.ToolModel
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.MirrorViewModel

/** Probe result list, handed over verbatim. */
internal class FakeDetectTools(private val tools: List<ToolModel> = emptyList()) : DetectToolsUseCase {
    override suspend fun invoke(): Result<List<ToolModel>> = Result.success(tools)
}

/**
 * Replays a scripted discovery sequence and then stays open, the way the real 2 s poll does.
 *
 * [subscriptions] is what makes "polling pauses while streaming and resumes afterwards"
 * observable without a clock.
 */
internal class FakeObserveDiscovery(
    private val script: List<DiscoveryModel> = emptyList(),
) : ObserveDeviceDiscoveryUseCase {

    var subscriptions: Int = 0
        private set

    override fun invoke(): Flow<DiscoveryModel> = flow {
        subscriptions++
        script.forEach { emit(it) }
        awaitCancellation()
    }
}

/** Records every boot request and can be told to fail. */
internal class FakeBootEmulator : BootEmulatorUseCase {

    val booted: MutableList<String> = mutableListOf()
    var failure: Throwable? = null

    override suspend fun invoke(avdName: String): Optional {
        booted += avdName
        return failure?.let { Result.failure(it) } ?: Result.success(Unit)
    }
}

/**
 * A frame source that counts its live collectors.
 *
 * [activeCollectors] is the assertion behind "rapid start/stop never leaves two capture loops
 * alive"; [failure] stands in for a device that vanished as the session opened, and
 * [failStream] for one that vanished afterwards.
 */
internal class FakeStartMirroring : StartMirroringUseCase {

    private val source = MutableSharedFlow<MirrorFrameModel>()

    val startedDevices: MutableList<String> = mutableListOf()

    var activeCollectors: Int = 0
        private set

    /** Fails the flow at subscription — the device was already gone when the session opened. */
    var failure: Throwable? = null

    private var midStreamFailure: Throwable? = null

    override fun invoke(deviceId: String): Flow<MirrorFrameModel> = source
        .onStart {
            startedDevices += deviceId
            activeCollectors++
            failure?.let { throw it }
        }
        .onEach { midStreamFailure?.let { throw it } }
        .onCompletion { activeCollectors-- }

    suspend fun pushFrame(frame: MirrorFrameModel) {
        source.emit(frame)
    }

    /**
     * Breaks an **already running** collection, which `failure` cannot do: `onStart` has long
     * since run by the time a session is open, so only a throw on an emission reaches the
     * ViewModel's `.catch` and the session-lost path behind it.
     *
     * [frame] is never delivered — it is the carrier that gets the throw into the collector.
     */
    suspend fun failStream(cause: Throwable, frame: MirrorFrameModel) {
        midStreamFailure = cause
        source.emit(frame)
    }
}

/** Emits scripted statistics; empty by default because most tests do not read them. */
internal class FakeObserveMirrorStats(
    private val script: Flow<MirrorStatsModel> = emptyFlow(),
) : ObserveMirrorStatsUseCase {
    override fun invoke(): Flow<MirrorStatsModel> = script
}

/**
 * A frame the tests can hold and compare without a graphics backend.
 *
 * `ImageBitmap(w, h)` is NOT usable here: this module's `desktopTest` runtime has no Skiko
 * native library (only `:compose-application` pulls `compose.desktop.currentOs`), so the real
 * factory throws `LibraryLoadException: Cannot find libskiko-macos-arm64.dylib.sha256`.
 * `ImageBitmap` is a plain interface, though, so implementing it is enough to assert which
 * frame got pinned — which is the whole of AC-47 — with no native dependency and no
 * `build.gradle.kts` edit.
 */
internal class FakeFrameBitmap(override val width: Int, override val height: Int) : ImageBitmap {
    override val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888
    override val colorSpace: ColorSpace = ColorSpaces.Srgb
    override val hasAlpha: Boolean = false

    override fun prepareToDraw(): Unit = Unit

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ): Unit = Unit
}

/**
 * A decoder whose output differs in **both** axes per frame, so an assertion on the pinned
 * frame cannot pass by accidentally matching one dimension.
 */
internal fun sizedFrameDecoder(): suspend (ByteArray) -> ImageBitmap? = { bytes ->
    FakeFrameBitmap(width = 100 + bytes.size, height = 200 + bytes.size * 2)
}

/**
 * Records daemon restarts so a test can prove AC-27's button did a real restart rather than a
 * re-poll — the distinction FR-37 exists to enforce.
 */
internal class FakeRestartAdbServer : RestartAdbServerUseCase {

    var restarts: Int = 0
    var failure: Throwable? = null

    override suspend fun invoke(): Optional {
        restarts += 1
        return failure?.let { Result.failure(it) } ?: Result.success(Unit)
    }
}

/** Counts poll-interval nudges, so a Retry that does nothing can be told from one that works. */
internal class FakeRefreshDiscovery : RefreshDiscoveryUseCase {

    var nudges: Int = 0

    override suspend fun invoke(): Optional {
        nudges += 1
        return Result.success(Unit)
    }
}

/** Records the paths AC-26's file dialog handed back. */
internal class FakeSetAdbPath : SetAdbPathUseCase {

    val paths: MutableList<String> = mutableListOf()

    override suspend fun invoke(path: String): Optional {
        paths += path
        return Result.success(Unit)
    }
}

/**
 * Builds a ViewModel with every seam faked.
 *
 * The decode seam returns `null` by default: most tests assert the state machine and need no
 * frame at all. Pass [sizedFrameDecoder] when the test is about *which* frame got pinned.
 */
internal fun mirrorViewModel(
    detectTools: DetectToolsUseCase = FakeDetectTools(),
    discovery: ObserveDeviceDiscoveryUseCase = FakeObserveDiscovery(),
    boot: BootEmulatorUseCase = FakeBootEmulator(),
    mirroring: StartMirroringUseCase = FakeStartMirroring(),
    stats: ObserveMirrorStatsUseCase = FakeObserveMirrorStats(),
    refreshDiscovery: RefreshDiscoveryUseCase = FakeRefreshDiscovery(),
    restartAdbServer: RestartAdbServerUseCase = FakeRestartAdbServer(),
    setAdbPath: SetAdbPathUseCase = FakeSetAdbPath(),
    decodeFrame: suspend (ByteArray) -> ImageBitmap? = { null },
): MirrorViewModel = MirrorViewModel(
    detectTools = detectTools,
    observeDiscovery = discovery,
    bootEmulator = boot,
    startMirroring = mirroring,
    observeMirrorStats = stats,
    refreshDiscovery = refreshDiscovery,
    restartAdbServer = restartAdbServer,
    setAdbPath = setAdbPath,
    decodeFrame = decodeFrame,
)

/**
 * Pins Orbit's container event loop to the test dispatcher.
 *
 * Orbit defaults both dispatchers to real thread pools (`Dispatchers.Default` /
 * `Dispatchers.Unconfined`), so without this the reducer runs off the test scheduler and
 * `advanceUntilIdle()` returns before any state has changed. `Orbit.configureDefaults` is the
 * library's own documented hook for this — it keeps the ViewModel free of a test-only
 * constructor parameter and adds no dependency.
 */
internal fun useTestDispatcherForOrbit(dispatcher: TestDispatcher) {
    Orbit.configureDefaults {
        eventLoopDispatcher = { dispatcher }
        intentLaunchingDispatcher = { dispatcher }
    }
}

/**
 * Subscribes to the container so Orbit runs its lazy `onCreate` block.
 *
 * `runCurrent()` and not `advanceUntilIdle()`: since coroutines 1.11 the latter deliberately
 * ignores `backgroundScope` work, so the subscription itself would never start.
 */
internal fun TestScope.subscribe(viewModel: MirrorViewModel) {
    backgroundScope.launch { viewModel.container.stateFlow.collect() }
    runCurrent()
}

/** The container's current state. */
internal val MirrorViewModel.currentState: MirrorUiState get() = container.stateFlow.value
