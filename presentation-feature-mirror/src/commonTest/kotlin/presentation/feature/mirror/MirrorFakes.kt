package presentation.feature.mirror

import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.ToolModel
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
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
 * alive"; [failure] stands in for a device that vanished as the session opened.
 */
internal class FakeStartMirroring : StartMirroringUseCase {

    private val source = MutableSharedFlow<MirrorFrameModel>()

    val startedDevices: MutableList<String> = mutableListOf()

    var activeCollectors: Int = 0
        private set

    var failure: Throwable? = null

    override fun invoke(deviceId: String): Flow<MirrorFrameModel> = source
        .onStart {
            startedDevices += deviceId
            activeCollectors++
            failure?.let { throw it }
        }
        .onCompletion { activeCollectors-- }

    suspend fun pushFrame(frame: MirrorFrameModel) {
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
 * Builds a ViewModel with every seam faked.
 *
 * The decode seam returns `null`: these tests assert the state machine, and a real
 * `ImageBitmap` would drag a graphics backend into a unit test for no added coverage.
 */
internal fun mirrorViewModel(
    detectTools: DetectToolsUseCase = FakeDetectTools(),
    discovery: ObserveDeviceDiscoveryUseCase = FakeObserveDiscovery(),
    boot: BootEmulatorUseCase = FakeBootEmulator(),
    mirroring: StartMirroringUseCase = FakeStartMirroring(),
    stats: ObserveMirrorStatsUseCase = FakeObserveMirrorStats(),
): MirrorViewModel = MirrorViewModel(
    detectTools = detectTools,
    observeDiscovery = discovery,
    bootEmulator = boot,
    startMirroring = mirroring,
    observeMirrorStats = stats,
    decodeFrame = { null },
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
