package presentation.feature.mirror

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.AnnotationHandoff
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.ToolModel
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import domain.core.source.model.history.SentBatchModel
import domain.usecase.api.source.monad.Optional
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DumpHierarchyUseCase
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import domain.core.source.monad.Failure
import domain.usecase.api.source.usecase.history.GetSentBatchUseCase
import domain.usecase.api.source.usecase.history.ObserveHistoryUseCase
import domain.usecase.api.source.usecase.history.RecordSentBatchUseCase
import domain.usecase.api.source.usecase.history.UpdateBatchStatusUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import presentation.feature.mirror.source.mirror.CropEncoder
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.MirrorViewModel
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A selectable physical device carrying both an Android release and a transport — the shape
 * `DiscoveryProjection`'s `toRow()` renders as `detail = "Android 15 · USB"` (T-1 / AC-14). Every
 * parameter is defaulted so a test that only needs *a* device, not this exact one, can ignore
 * all of them.
 */
internal fun physicalDevice(
    id: String = "PHYSICAL-DEFAULT",
    androidVersion: String = "15",
    transportLabel: String = "USB",
): DeviceModel = DeviceModel(
    id = id,
    displayName = "Pixel 8 Pro",
    serial = id,
    state = DeviceStateModel.Device,
    isEmulator = false,
    androidVersion = androidVersion,
    transportLabel = transportLabel,
)

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
internal class FakeFrameBitmap(
    override val width: Int,
    override val height: Int,
    private val pixel: (x: Int, y: Int) -> Int = ::gradientPixel,
) : ImageBitmap {
    override val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888
    override val colorSpace: ColorSpace = ColorSpaces.Srgb
    override val hasAlpha: Boolean = false

    override fun prepareToDraw(): Unit = Unit

    /**
     * Fills the caller's buffer from [pixel], honouring every argument.
     *
     * It used to be `= Unit`, which left every region read all-zero: a BEFORE/AFTER pair then
     * compared equal whatever the test intended, and every pixel assertion passed vacuously
     * (bugs.json#PP-TD-10). A fake that silently answers nothing is worse than no fake.
     *
     * Row `r` of the requested region starts at `bufferOffset + r * stride`, so a stride wider
     * than the region leaves the gap between rows untouched -- exactly what the real
     * implementation does.
     */
    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) {
        for (row in 0 until height) {
            val rowStart = bufferOffset + row * stride
            for (column in 0 until width) {
                buffer[rowStart + column] = pixel(startX + column, startY + row)
            }
        }
    }
}

/**
 * The default [FakeFrameBitmap] content: deterministic, opaque, and different at every
 * coordinate, so two regions of the same frame never compare equal by accident.
 */
internal fun gradientPixel(x: Int, y: Int): Int =
    (0xFF shl 24) or (((x * 7) and 0xFF) shl 16) or (((y * 11) and 0xFF) shl 8) or ((x + y) and 0xFF)

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
 * Runs [block] and, when the CALLER is cancelled mid-wait, returns [onCancellation] instead of
 * letting the cancellation unwind.
 *
 * Deliberately worse-behaved than a bare `await()`, because the real thing is. A use case parked
 * here stands in for `adb`'s blocking process read — which no `cancel()` can interrupt, so the
 * dump genuinely COMPLETES after the user walked away — and for `domain-usecase-impl`'s
 * `resultLauncher`, which used to catch `Throwable`, `CancellationException` included, and hand
 * it back as an ordinary `Result`. A fake that unwound cleanly on `await()` was therefore better
 * behaved than production, and every "a cancelled run lands nothing" test here was passing on
 * the fake's good manners rather than on anything the ViewModel does (`review#COR-1`).
 *
 * `resultLauncher` rethrows cancellation now, but this stays: the uninterruptible `adb` read is
 * real either way, so a cancelled run still resumes and still has to be stopped by
 * `MirrorViewModel`'s own run-generation check rather than by the coroutine machinery.
 */
private suspend fun <T> uninterruptibly(onCancellation: T, block: suspend () -> T): T = try {
    block()
} catch (cancellation: CancellationException) {
    onCancellation
}

/**
 * Resolves immediately with [result] by default — the right shape for every test that does not
 * care about the in-flight window. Set [manual] to gate resolution behind [resolve] instead, so
 * a test can assert the `Dumping` state deterministically before completing the dump, rather
 * than racing a real suspension against `advanceUntilIdle()`.
 *
 * **Cancellation is swallowed on purpose** — see [uninterruptibly].
 */
internal class FakeDumpHierarchy(
    private val result: Result<HierarchySnapshotModel> = Result.success(HierarchySnapshotModel.Empty),
    private val manual: Boolean = false,
) : DumpHierarchyUseCase {

    val calls: MutableList<String> = mutableListOf()
    private var gate: CompletableDeferred<Result<HierarchySnapshotModel>>? = null

    override suspend fun invoke(deviceId: String): Result<HierarchySnapshotModel> {
        calls += deviceId
        if (!manual) return result
        val deferred = CompletableDeferred<Result<HierarchySnapshotModel>>()
        gate = deferred
        // The cancelled case hands back [result], not a failure: that is what an uninterruptible
        // process read does — it finishes, and the abandoned run walks on with a good dump.
        return uninterruptibly(onCancellation = result) { deferred.await() }
    }

    /** Completes the most recently started call that is still pending. */
    fun resolve(newResult: Result<HierarchySnapshotModel> = result) {
        gate?.complete(newResult)
    }
}

/**
 * Records every publish, and can be made to throw instead (T-9's failure path).
 *
 * [manual] mirrors [FakeDumpHierarchy]'s gate: a test proving "a second ResolveAndSend while one
 * is already in flight is dropped" (TAC-1) needs [publish] to still be suspended when the second
 * dispatch happens, rather than racing a real suspension against `advanceUntilIdle()`.
 */
internal class FakeAnnotationHandoff(
    private val failure: Throwable? = null,
    private val manual: Boolean = false,
) : AnnotationHandoff {

    val published: MutableList<AnnotationEnvelope> = mutableListOf()
    val publishedImages: MutableList<List<HandoffImage>> = mutableListOf()

    private var gate: CompletableDeferred<Unit>? = null

    override suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>) {
        if (manual) {
            val deferred = CompletableDeferred<Unit>()
            gate = deferred
            deferred.await()
        }
        failure?.let { throw it }
        published += envelope
        publishedImages += images
    }

    override suspend fun latest(): AnnotationEnvelope? = published.lastOrNull()

    override fun resolve(envelope: AnnotationEnvelope, name: String): String = "file:///fake/$name"

    /** Releases a [publish] call parked behind [manual]. */
    fun release() {
        gate?.complete(Unit)
    }
}

/** Records every crop region it was asked to encode; returns [bytes] for every call. */
internal class FakeCropEncoder(private val bytes: ByteArray? = ByteArray(1)) : CropEncoder {

    val encodedRects: MutableList<DeviceRect> = mutableListOf()

    override suspend fun encode(frame: ImageBitmap, rect: DeviceRect): ByteArray? {
        encodedRects += rect
        return bytes
    }
}

/**
 * Records every batch history was asked to keep, and can be made to fail like the real use
 * case does — as a `Failure`-carrying `Result`, never as a throw.
 *
 * [recordedAt] is how a test proves the ORDERING the whole persist design rests on: it is the
 * number of `SendComplete` effects already posted when the write happened, so `0` means the
 * batch was recorded before the toast.
 */
internal class FakeRecordSentBatch(
    private val failure: Throwable? = null,
    private val sideEffectCount: () -> Int = { 0 },
) : RecordSentBatchUseCase {

    val recorded: MutableList<SentBatchModel> = mutableListOf()
    val recordedAt: MutableList<Int> = mutableListOf()

    override suspend fun invoke(batch: SentBatchModel): Optional {
        recorded += batch
        recordedAt += sideEffectCount()
        return failure?.let { Result.failure(it) } ?: Result.success(Unit)
    }
}

/**
 * Hands back one recorded batch, or the `NotFound` the real use case raises when the row or
 * its `frame.png` is gone.
 *
 * [manual] mirrors [FakeDumpHierarchy]'s gate so a test can observe
 * [presentation.feature.mirror.source.mirror.VerifyPhase.Loading] while the read is still in
 * flight, rather than racing a real suspension against `advanceUntilIdle()`.
 */
internal class FakeGetSentBatch(
    private val batch: SentBatchModel? = null,
    private val manual: Boolean = false,
) : GetSentBatchUseCase {

    val calls: MutableList<String> = mutableListOf()
    private var gate: CompletableDeferred<Unit>? = null

    override suspend fun invoke(batchId: String): Result<SentBatchModel> {
        calls += batchId
        if (manual) {
            val deferred = CompletableDeferred<Unit>()
            gate = deferred
            // Swallows cancellation, exactly like [FakeDumpHierarchy] — see [uninterruptibly].
            uninterruptibly(onCancellation = Unit) { deferred.await() }
        }
        return batch?.let { Result.success(it) } ?: Result.failure(Failure.Logic.NotFound)
    }

    /** Releases a read parked behind [manual]. */
    fun release() {
        gate?.complete(Unit)
    }
}

/**
 * Records every write, so "exactly once" and "never" are both directly assertable;
 * [failure] makes them all fail while still recording the attempt.
 */
internal class FakeUpdateBatchStatus(
    private val failure: Throwable? = null,
) : UpdateBatchStatusUseCase {

    val writes: MutableList<Pair<String, BatchStatusModel>> = mutableListOf()

    override suspend fun invoke(batchId: String, status: BatchStatusModel): Optional {
        writes += batchId to status
        return failure?.let { Result.failure(it) } ?: Result.success(Unit)
    }
}

/** A history stream the test owns: empty until [emit] puts rows on it. */
internal class FakeObserveHistory(
    initial: List<HistoryEntryModel> = emptyList(),
) : ObserveHistoryUseCase {

    private val entries = MutableStateFlow(initial)

    override fun invoke(): Flow<List<HistoryEntryModel>> = entries

    fun emit(next: List<HistoryEntryModel>) {
        entries.value = next
    }
}

/**
 * A [TimeSource] whose every mark reports a FIXED [elapsedNow] — T-9's determinism seam so a test
 * never has to assert against how long the test process actually took to run.
 */
internal class FakeTimeSource(private val elapsedPerMark: Duration) : TimeSource {
    override fun markNow(): TimeMark = object : TimeMark {
        override fun elapsedNow(): Duration = elapsedPerMark
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
    dumpHierarchy: DumpHierarchyUseCase = FakeDumpHierarchy(),
    recordSentBatch: RecordSentBatchUseCase = FakeRecordSentBatch(),
    observeHistory: ObserveHistoryUseCase = FakeObserveHistory(),
    getSentBatch: GetSentBatchUseCase = FakeGetSentBatch(),
    updateBatchStatus: UpdateBatchStatusUseCase = FakeUpdateBatchStatus(),
    decodeFrame: suspend (ByteArray) -> ImageBitmap? = { null },
    handoff: AnnotationHandoff = FakeAnnotationHandoff(),
    encodeCrop: CropEncoder = CropEncoder { _, _ -> null },
    nowMs: () -> Long = { 0L },
    elapsed: TimeSource = FakeTimeSource(Duration.ZERO),
): MirrorViewModel = MirrorViewModel(
    detectTools = detectTools,
    observeDiscovery = discovery,
    bootEmulator = boot,
    startMirroring = mirroring,
    observeMirrorStats = stats,
    refreshDiscovery = refreshDiscovery,
    restartAdbServer = restartAdbServer,
    setAdbPath = setAdbPath,
    dumpHierarchy = dumpHierarchy,
    recordSentBatch = recordSentBatch,
    observeHistory = observeHistory,
    getSentBatch = getSentBatch,
    updateBatchStatus = updateBatchStatus,
    decodeFrame = decodeFrame,
    handoff = handoff,
    encodeCrop = encodeCrop,
    nowMs = nowMs,
    elapsed = elapsed,
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
