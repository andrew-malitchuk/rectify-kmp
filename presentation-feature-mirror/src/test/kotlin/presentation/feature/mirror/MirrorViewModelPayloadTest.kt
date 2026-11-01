package presentation.feature.mirror

import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val PHONE = "PAYLOAD-PHONE-01"

/** Two rectangles that are obviously not each other, in device pixels. */
private val FIRST_RECT = DeviceRect(left = 100, top = 200, right = 318, bottom = 277)
private val SECOND_RECT = DeviceRect(left = 40, top = 900, right = 585, bottom = 1154)

private fun onePhone(): FakeObserveDiscovery = FakeObserveDiscovery(
    listOf(
        DiscoveryModel.Found(
            physical = listOf(
                physicalDevice(id = PHONE, androidVersion = "15", transportLabel = "USB"),
            ),
            emulators = emptyList(),
        ),
    ),
)

/** A frame whose decoded size is driven by its byte count — see `sizedFrameDecoder`. */
private fun frameOf(byteCount: Int): MirrorFrameModel = MirrorFrameModel(
    bytes = ByteArray(byteCount),
    widthPx = 1080,
    heightPx = 2400,
    capturedAtMs = 0L,
)

/**
 * T-1 (AC-14 / AC-79) — [presentation.feature.mirror.source.mirror.FrozenFrame.frameId],
 * `deviceLabel` and `promptOverride`, plus [MirrorIntent.EditPrompt].
 *
 * TAC-4 (the `freezeCounter` increment living in `stageAnnotation`'s intent body rather than
 * inside `reduce`) is deliberately NOT asserted here. The only way the wrong placement could be
 * observed is a `reduce {}` CAS retry re-running the lambda, and that retry only happens when
 * `MutableStateFlow.update` loses a race — which, on `StandardTestDispatcher`'s single-threaded,
 * cooperative scheduling, cannot occur: nothing here suspends between a state read and its
 * `compareAndSet`, so there is never a second coroutine to lose the race to. A test asserting
 * "frameId == 1 after one freeze" (below) passes identically whether the increment sits inside
 * or outside `reduce`, so it does not cover TAC-4 either — that criterion is left to diff
 * inspection, as the task brief anticipated.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelPayloadTest {

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

    @Test
    fun freezeStampsFrameIdOnFirstAnnotation() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))

        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        assertEquals(1, viewModel.currentState.frozen?.frameId)
    }

    @Test
    fun secondDragOnTheSamePinnedFrameKeepsTheSameFrameId() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        val firstFrameId = viewModel.currentState.frozen?.frameId

        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()

        assertEquals(1, firstFrameId)
        assertEquals(firstFrameId, viewModel.currentState.frozen?.frameId)
        assertEquals(2, viewModel.currentState.stagedCount)
    }

    @Test
    fun aNewFreezeAfterResumeLiveAdvancesTheFrameId() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = streamingWithFrame(frameOf(4), mirroring)
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        val firstFrameId = viewModel.currentState.frozen?.frameId

        viewModel.handleIntent(MirrorIntent.ResumeLive)
        advanceUntilIdle()
        mirroring.pushFrame(frameOf(9))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()

        assertEquals(1, firstFrameId)
        assertEquals(2, viewModel.currentState.frozen?.frameId)
    }

    @Test
    fun deviceLabelIsTheSelectedRowsDetailWithTheTransportSegmentStripped() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))

        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        // physicalDevice()'s detail is rendered as "Android 15 · USB" by DiscoveryProjection;
        // the transport segment (" · USB") must be gone from the stamped label.
        assertEquals("Android 15", viewModel.currentState.frozen?.deviceLabel)
    }

    @Test
    fun editPromptSurvivesAnAnnotationAddAndADelete() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.EditPrompt("check the contrast here"))
        advanceUntilIdle()
        assertEquals("check the contrast here", viewModel.currentState.frozen?.promptOverride)

        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()
        assertEquals("check the contrast here", viewModel.currentState.frozen?.promptOverride)

        viewModel.handleIntent(MirrorIntent.DeleteAnnotation(1))
        advanceUntilIdle()
        assertEquals("check the contrast here", viewModel.currentState.frozen?.promptOverride)
    }

    @Test
    fun editPromptSurvivesASessionLossAndReconnect() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = streamingWithFrame(frameOf(4), mirroring)
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.EditPrompt("flag this spacing"))
        advanceUntilIdle()

        mirroring.failStream(IllegalStateException("device unplugged"), frameOf(9))
        advanceUntilIdle()
        assertEquals(MirrorSession.Lost(PHONE), viewModel.currentState.session)
        assertEquals("flag this spacing", viewModel.currentState.frozen?.promptOverride)

        viewModel.handleIntent(MirrorIntent.Reconnect)
        advanceUntilIdle()

        assertEquals("flag this spacing", viewModel.currentState.frozen?.promptOverride)
    }

    /**
     * MUTATION guard: dropping `promptOverride` on `SelectAnnotation`/`CommentAnnotation`
     * instead of only on `ResumeLive` would still pass every test above but fails this one.
     */
    @Test
    fun resumeLiveIsTheOnlyIntentThatDropsThePromptOverride() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = streamingWithFrame(frameOf(4), mirroring)
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.EditPrompt("drop me"))
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.SelectAnnotation(1))
        viewModel.handleIntent(MirrorIntent.CommentAnnotation(index = 1, comment = "noted"))
        advanceUntilIdle()
        assertNotNull(viewModel.currentState.frozen?.promptOverride)

        viewModel.handleIntent(MirrorIntent.ResumeLive)
        advanceUntilIdle()

        assertNull(viewModel.currentState.frozen)
    }

    @Test
    fun editPromptWithNoFrozenFrameIsANoOp() = runTest(mainDispatcher) {
        val viewModel = mirrorViewModel(discovery = onePhone(), decodeFrame = sizedFrameDecoder())
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.EditPrompt("nothing to attach this to"))
        advanceUntilIdle()

        assertNull(viewModel.currentState.frozen)
    }

    /** A streaming session whose first decoded frame has already landed. */
    private suspend fun TestScope.streamingWithFrame(
        frame: MirrorFrameModel,
        mirroring: FakeStartMirroring = FakeStartMirroring(),
    ): MirrorViewModel {
        val viewModel = mirrorViewModel(
            discovery = onePhone(),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
        )
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()
        mirroring.pushFrame(frame)
        advanceUntilIdle()
        return viewModel
    }
}
