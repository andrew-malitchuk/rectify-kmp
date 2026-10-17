package presentation.feature.mirror

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.HierarchyNodeModel
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.core.measure.projectMeasureNodes
import presentation.feature.mirror.core.measure.replaceNoteLine
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val PHONE = "37281FDH2000AB"

/** The annotation drawn for every test here — fully contained in the fixture's `button` node. */
private val FIRST_RECT = DeviceRect(left = 10, top = 20, right = 50, bottom = 40)
private val SECOND_RECT = DeviceRect(left = 10, top = 60, right = 50, bottom = 80)

private fun phone(): DeviceModel = DeviceModel(
    id = PHONE,
    displayName = "Pixel 8 Pro",
    serial = PHONE,
    state = DeviceStateModel.Device,
    isEmulator = false,
    transportLabel = "USB",
)

private fun onePhone(): FakeObserveDiscovery = FakeObserveDiscovery(
    listOf(DiscoveryModel.Found(physical = listOf(phone()), emulators = emptyList())),
)

/** A frame whose decoded size is driven by its byte count — see `sizedFrameDecoder`. */
private fun frameOf(byteCount: Int): MirrorFrameModel = MirrorFrameModel(
    bytes = ByteArray(byteCount),
    widthPx = 1080,
    heightPx = 2400,
    capturedAtMs = 0L,
)

/**
 * A minimal, deterministic hierarchy whose frame agrees with `frameOf(4)`'s decoded size
 * (104x208, see `sizedFrameDecoder`) and whose one non-root node exactly matches [FIRST_RECT],
 * so `snapTarget` has nothing to disambiguate.
 */
private fun snapshotFor(densityDpi: Int = 160): HierarchySnapshotModel = HierarchySnapshotModel(
    nodes = listOf(
        HierarchyNodeModel(
            id = "root", className = "android.widget.FrameLayout",
            left = 0, top = 0, right = 104, bottom = 208, isTappable = false, depth = 0,
        ),
        HierarchyNodeModel(
            id = "button", className = "android.widget.Button",
            left = 10, top = 20, right = 50, bottom = 40, isTappable = true, depth = 1,
        ),
    ),
    frameWidthPx = 104,
    frameHeightPx = 208,
    densityDpi = densityDpi,
)

/**
 * The reducer half of measure mode: `StartMeasure`/`EndMeasure`, the dump lifecycle fired at
 * freeze time, and the three clear sites plus `reconnect()`'s asymmetry with them.
 *
 * Same harness shape as `MirrorViewModelAnnotationTest` — freezing and annotating is a render
 * decision taken entirely in the reducer, so nothing here needs a window or a real bitmap.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelMeasureTest {

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        useTestDispatcherForOrbit(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Orbit.resetDefaults()
        Dispatchers.resetMain()
    }

    /**
     * T-10 — `StartMeasure` stores a readout matching the pure function's own output, proving
     * the handler delegates rather than reimplements `measure(...)`.
     */
    @Test
    fun startMeasureStoresAReadoutMatchingThePureFunction() = runTest(mainDispatcher) {
        val snapshot = snapshotFor()
        val viewModel = frozenWithDump(FakeDumpHierarchy(result = Result.success(snapshot)))
        val frozen = viewModel.currentState.frozen
        checkNotNull(frozen)

        viewModel.handleIntent(MirrorIntent.StartMeasure(index = 1))
        advanceUntilIdle()

        val expected = measure(
            annotation = frozen.annotations.single().rect,
            index = 1,
            nodes = projectMeasureNodes(snapshot, frozen.deviceSize),
            frameSize = frozen.deviceSize,
            densityDpi = snapshot.densityDpi,
        )
        assertEquals(expected, viewModel.currentState.measure)
    }

    /**
     * T-10 / TAC-21 — `EndMeasure` writes exactly one `⌥`-prefixed line, replacing any prior one
     * and preserving the user's own prose, then clears the readout.
     */
    @Test
    fun endMeasureWritesOneNoteLineReplacingAnyPriorOneAndPreservesProse() = runTest(mainDispatcher) {
        val snapshot = snapshotFor()
        val viewModel = frozenWithDump(FakeDumpHierarchy(result = Result.success(snapshot)))
        val priorComment = "needs more padding\n⌥ a stale measure line from last time"
        viewModel.handleIntent(MirrorIntent.CommentAnnotation(index = 1, comment = priorComment))
        viewModel.handleIntent(MirrorIntent.StartMeasure(index = 1))
        advanceUntilIdle()
        val readout = viewModel.currentState.measure
        checkNotNull(readout)

        viewModel.handleIntent(MirrorIntent.EndMeasure)
        advanceUntilIdle()

        val annotation = viewModel.currentState.frozen?.annotations?.single()
        assertEquals(replaceNoteLine(priorComment, readout.noteLine), annotation?.comment)
        assertNull(viewModel.currentState.measure)
    }

    /** T-10 — an annotation deleted between `StartMeasure` and `EndMeasure` leaves state unchanged. */
    @Test
    fun endMeasureOnAVanishedIndexLeavesStateUnchanged() = runTest(mainDispatcher) {
        val viewModel = frozenWithDump()
        viewModel.handleIntent(MirrorIntent.StartMeasure(index = 1))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.DeleteAnnotation(index = 1))
        advanceUntilIdle()
        val beforeEndMeasure = viewModel.currentState

        viewModel.handleIntent(MirrorIntent.EndMeasure)
        advanceUntilIdle()

        assertEquals(beforeEndMeasure, viewModel.currentState)
    }

    /** T-10 — `EndMeasure` with no readout in flight (no prior `StartMeasure`) is a no-op. */
    @Test
    fun endMeasureWithNoReadoutIsANoOp() = runTest(mainDispatcher) {
        val viewModel = frozenWithDump()
        val before = viewModel.currentState

        viewModel.handleIntent(MirrorIntent.EndMeasure)
        advanceUntilIdle()

        assertEquals(before, viewModel.currentState)
    }

    /**
     * T-10 / TAC-22 / AC-72 — the freeze transition reduces `Dumping` IMMEDIATELY, before the
     * fake use case resolves. A test that only checked the final state would pass an
     * implementation that skips straight to `Ready`/`Unavailable` and never shows `Dumping` at
     * all — this is the one assertion that would catch that.
     */
    @Test
    fun freezeTransitionReducesDumpingImmediatelyBeforeTheDumpResolves() = runTest(mainDispatcher) {
        val (viewModel, _) = freezeWithManualDump()

        assertEquals(
            expected = listOf("frozen=true", "status=${HierarchyStatus.Dumping}"),
            actual = listOf(
                "frozen=${viewModel.currentState.frozen != null}",
                "status=${viewModel.currentState.hierarchyStatus}",
            ),
        )
    }

    /** T-10 / TAC-22 — a successful dump reduces `Ready` without delaying or altering the freeze. */
    @Test
    fun dumpSuccessReducesReadyWithoutAlteringTheFreeze() = runTest(mainDispatcher) {
        val (viewModel, dumpHierarchy) = freezeWithManualDump()
        val frozenBefore = viewModel.currentState.frozen
        val snapshot = snapshotFor()

        dumpHierarchy.resolve(Result.success(snapshot))
        advanceUntilIdle()

        assertEquals(HierarchyStatus.Ready, viewModel.currentState.hierarchyStatus)
        assertEquals(snapshot, viewModel.frozenHierarchy.value)
        assertEquals(frozenBefore, viewModel.currentState.frozen)
    }

    /** T-10 / TAC-22 — a failed dump reduces `Unavailable` without delaying or altering the freeze. */
    @Test
    fun dumpFailureReducesUnavailableWithoutAlteringTheFreeze() = runTest(mainDispatcher) {
        val (viewModel, dumpHierarchy) = freezeWithManualDump()
        val frozenBefore = viewModel.currentState.frozen

        dumpHierarchy.resolve(Result.failure(IllegalStateException("adb timed out")))
        advanceUntilIdle()

        assertEquals(HierarchyStatus.Unavailable, viewModel.currentState.hierarchyStatus)
        assertEquals(frozenBefore, viewModel.currentState.frozen)
    }

    @Test
    fun selectPlatformClearsBothMeasureFieldsAlongWithTheFrame() = runTest(mainDispatcher) {
        val viewModel = measuredFrozenState()

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()

        assertBothMeasureFieldsCleared(viewModel)
    }

    @Test
    fun stopMirroringClearsBothMeasureFieldsAlongWithTheFrame() = runTest(mainDispatcher) {
        val viewModel = measuredFrozenState()

        viewModel.handleIntent(MirrorIntent.StopMirroring)
        advanceUntilIdle()

        assertBothMeasureFieldsCleared(viewModel)
    }

    @Test
    fun resumeLiveClearsBothMeasureFieldsAlongWithTheFrame() = runTest(mainDispatcher) {
        val viewModel = measuredFrozenState()

        viewModel.handleIntent(MirrorIntent.ResumeLive)
        advanceUntilIdle()

        assertBothMeasureFieldsCleared(viewModel)
    }

    /**
     * T-10 — the asymmetry the three clear sites don't share. `reconnect()` preserves the
     * frozen frame and its annotations (NFR-6); this pins that the measure readout and the
     * hierarchy status survive right alongside them.
     */
    @Test
    fun reconnectClearsNeitherMeasureField() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = frozenWithDump(mirroring = mirroring)
        viewModel.handleIntent(MirrorIntent.StartMeasure(index = 1))
        advanceUntilIdle()
        val measureBefore = viewModel.currentState.measure
        val statusBefore = viewModel.currentState.hierarchyStatus
        checkNotNull(measureBefore)

        mirroring.failStream(IllegalStateException("device unplugged"), frameOf(9))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Reconnect)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("measure=$measureBefore", "status=$statusBefore"),
            actual = listOf(
                "measure=${viewModel.currentState.measure}",
                "status=${viewModel.currentState.hierarchyStatus}",
            ),
        )
    }

    private fun assertBothMeasureFieldsCleared(viewModel: MirrorViewModel) {
        assertEquals(
            expected = listOf(null, HierarchyStatus.Absent),
            actual = listOf(viewModel.currentState.measure, viewModel.currentState.hierarchyStatus),
        )
    }

    /** A frozen frame with one annotation and a resolved (by default) hierarchy dump. */
    private suspend fun TestScope.frozenWithDump(
        dumpHierarchy: FakeDumpHierarchy = FakeDumpHierarchy(result = Result.success(snapshotFor())),
        mirroring: FakeStartMirroring = FakeStartMirroring(),
    ): MirrorViewModel {
        val viewModel = mirrorViewModel(
            discovery = onePhone(),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
            dumpHierarchy = dumpHierarchy,
        )
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()
        mirroring.pushFrame(frameOf(4))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        return viewModel
    }

    /** The same freeze, but the dump is gated behind [FakeDumpHierarchy.resolve]. */
    private suspend fun TestScope.freezeWithManualDump(): Pair<MirrorViewModel, FakeDumpHierarchy> {
        val dumpHierarchy = FakeDumpHierarchy(manual = true)
        val viewModel = frozenWithDump(dumpHierarchy = dumpHierarchy)
        return viewModel to dumpHierarchy
    }

    /** A frozen frame whose annotation has already been through one full `StartMeasure`. */
    private suspend fun TestScope.measuredFrozenState(): MirrorViewModel {
        val viewModel = frozenWithDump()
        viewModel.handleIntent(MirrorIntent.StartMeasure(index = 1))
        advanceUntilIdle()
        return viewModel
    }
}
