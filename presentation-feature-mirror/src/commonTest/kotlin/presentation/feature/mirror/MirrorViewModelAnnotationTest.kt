package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
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
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorStage
import presentation.feature.mirror.source.mirror.MirrorViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val PHONE = "37281FDH2000AB"

/** Two rectangles that are obviously not each other, in device pixels. */
private val FIRST_RECT = DeviceRect(left = 100, top = 200, right = 318, bottom = 277)
private val SECOND_RECT = DeviceRect(left = 40, top = 900, right = 585, bottom = 1154)

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
 * The freeze/annotate half of the reducer.
 *
 * Everything here is asserted through the public `handleIntent` surface and the two
 * `StateFlow`s, with no display attached: freezing is a render decision taken in the reducer,
 * so nothing in this file needs a window, a device or a real bitmap.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelAnnotationTest {

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
    fun firstStageAnnotationFreezesTheFrame() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))

        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        val frozen = viewModel.currentState.frozen
        assertEquals(IntSize(width = 104, height = 208), frozen?.deviceSize)
        assertEquals(listOf(FIRST_RECT), frozen?.annotations?.map { it.rect })
        assertEquals(1, viewModel.currentState.stagedCount)
        assertEquals(MirrorStage.Frozen, viewModel.currentState.stage)
        assertTrue(viewModel.frozenFrame.value != null)
    }

    @Test
    fun freezeUsesTheFrameHeldAtRelease() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = streamingWithFrame(frameOf(4), mirroring)

        // A frame lands mid-drag. Nothing was buffered at drag start, so this is the one the
        // release must pin — AC-47.
        mirroring.pushFrame(frameOf(9))
        advanceUntilIdle()
        val atRelease = viewModel.frame.value

        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        assertSame(atRelease, viewModel.frozenFrame.value)
        assertEquals(109, viewModel.frozenFrame.value?.width)
        assertEquals(218, viewModel.frozenFrame.value?.height)
        assertEquals(IntSize(width = 109, height = 218), viewModel.currentState.frozen?.deviceSize)
    }

    @Test
    fun secondDragAddsAnnotationWithoutRefreezing() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = streamingWithFrame(frameOf(4), mirroring)

        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        val pinned = viewModel.frozenFrame.value

        // The capture loop never stopped, so newer frames keep arriving behind the freeze.
        mirroring.pushFrame(frameOf(9))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()

        assertSame(pinned, viewModel.frozenFrame.value)
        val frozen = viewModel.currentState.frozen
        assertEquals(listOf(1, 2), frozen?.annotations?.map { it.index })
        assertEquals(listOf(FIRST_RECT, SECOND_RECT), frozen?.annotations?.map { it.rect })
        assertEquals(IntSize(width = 104, height = 208), frozen?.deviceSize)
        assertEquals(2, viewModel.currentState.stagedCount)
    }

    @Test
    fun stageAnnotationWithNoFrameIsDropped() = runTest(mainDispatcher) {
        val viewModel = mirrorViewModel(discovery = onePhone(), decodeFrame = sizedFrameDecoder())
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        assertNull(viewModel.currentState.frozen)
        assertNull(viewModel.frozenFrame.value)
        assertEquals(0, viewModel.currentState.stagedCount)
    }

    @Test
    fun commentCommitStoresTheText() = runTest(mainDispatcher) {
        val viewModel = frozenWithTwoAnnotations()

        viewModel.handleIntent(MirrorIntent.CommentAnnotation(index = 2, comment = "padding is off"))
        advanceUntilIdle()

        val annotations = viewModel.currentState.frozen?.annotations.orEmpty()
        assertEquals("", annotations[0].comment)
        assertEquals("padding is off", annotations[1].comment)
    }

    @Test
    fun deleteDecrementsStagedCountAndRenumbers() = runTest(mainDispatcher) {
        val viewModel = frozenWithTwoAnnotations()

        viewModel.handleIntent(MirrorIntent.DeleteAnnotation(index = 1))
        advanceUntilIdle()

        val annotations = viewModel.currentState.frozen?.annotations.orEmpty()
        assertEquals(1, viewModel.currentState.stagedCount)
        assertEquals(listOf(1), annotations.map { it.index })
        assertEquals(SECOND_RECT, annotations.single().rect)
    }

    @Test
    fun deleteOfSelectedAnnotationClearsOrClampsSelection() = runTest(mainDispatcher) {
        val viewModel = frozenWithTwoAnnotations()

        // Deleting the selected one closes its popover.
        viewModel.handleIntent(MirrorIntent.SelectAnnotation(2))
        viewModel.handleIntent(MirrorIntent.DeleteAnnotation(2))
        advanceUntilIdle()
        assertNull(viewModel.currentState.frozen?.selectedIndex)

        // Deleting a different one keeps the popover on the same annotation, renumbered.
        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectAnnotation(2))
        viewModel.handleIntent(MirrorIntent.DeleteAnnotation(1))
        advanceUntilIdle()

        assertEquals(1, viewModel.currentState.frozen?.selectedIndex)
        assertEquals(SECOND_RECT, viewModel.currentState.frozen?.selected?.rect)
    }

    @Test
    fun selectAnnotationNullKeepsTheAnnotationAndTheCount() = runTest(mainDispatcher) {
        val viewModel = frozenWithTwoAnnotations()

        viewModel.handleIntent(MirrorIntent.SelectAnnotation(1))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.SelectAnnotation(null))
        advanceUntilIdle()

        assertNull(viewModel.currentState.frozen?.selectedIndex)
        assertEquals(2, viewModel.currentState.stagedCount)
        assertEquals(MirrorStage.Frozen, viewModel.currentState.stage)
    }

    @Test
    fun clearStagedEmptiesTheListAndKeepsTheFrameFrozen() = runTest(mainDispatcher) {
        val viewModel = frozenWithTwoAnnotations()
        val pinned = viewModel.frozenFrame.value

        viewModel.handleIntent(MirrorIntent.ClearStaged)
        advanceUntilIdle()

        assertEquals(0, viewModel.currentState.stagedCount)
        assertNull(viewModel.currentState.frozen?.selectedIndex)
        assertSame(pinned, viewModel.frozenFrame.value)
        assertEquals(MirrorStage.Frozen, viewModel.currentState.stage)
    }

    @Test
    fun resumeLiveClearsTheFrozenFrameAndAnnotations() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = frozenWithTwoAnnotations(mirroring)

        viewModel.handleIntent(MirrorIntent.ResumeLive)
        advanceUntilIdle()

        assertNull(viewModel.currentState.frozen)
        assertNull(viewModel.frozenFrame.value)
        assertEquals(MirrorStage.Mirror, viewModel.currentState.stage)
        // The session was never stopped, so there is nothing to reconnect (AC-51 / SC-11).
        assertEquals(MirrorSession.Streaming(PHONE), viewModel.currentState.session)
        assertEquals(listOf(PHONE), mirroring.startedDevices)
        assertEquals(1, mirroring.activeCollectors)
    }

    @Test
    fun dragAfterResumeLiveFreezesANewFrame() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = frozenWithTwoAnnotations(mirroring)

        viewModel.handleIntent(MirrorIntent.ResumeLive)
        advanceUntilIdle()
        mirroring.pushFrame(frameOf(9))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()

        assertEquals(IntSize(width = 109, height = 218), viewModel.currentState.frozen?.deviceSize)
        assertEquals(1, viewModel.currentState.stagedCount)
        assertEquals(MirrorStage.Frozen, viewModel.currentState.stage)
    }

    @Test
    fun selectPlatformIosClearsTheFrozenFrame() = runTest(mainDispatcher) {
        val viewModel = frozenWithTwoAnnotations()

        viewModel.handleIntent(MirrorIntent.SelectPlatform(PlatformModel.Ios))
        advanceUntilIdle()

        assertNull(viewModel.currentState.frozen)
        assertNull(viewModel.frozenFrame.value)
        assertEquals(MirrorStage.IosDeferred, viewModel.currentState.stage)
    }

    @Test
    fun sessionLostWhileFrozenKeepsTheAnnotations() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = frozenWithTwoAnnotations(mirroring)

        mirroring.failure = IllegalStateException("device unplugged")
        mirroring.pushFrame(frameOf(9))
        advanceUntilIdle()

        assertEquals(2, viewModel.currentState.stagedCount)
        assertEquals(MirrorStage.Frozen, viewModel.currentState.stage)
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

    /** The frozen stage with two annotations staged against one pinned frame. */
    private suspend fun TestScope.frozenWithTwoAnnotations(
        mirroring: FakeStartMirroring = FakeStartMirroring(),
    ): MirrorViewModel {
        val viewModel = streamingWithFrame(frameOf(4), mirroring)
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()
        return viewModel
    }
}
