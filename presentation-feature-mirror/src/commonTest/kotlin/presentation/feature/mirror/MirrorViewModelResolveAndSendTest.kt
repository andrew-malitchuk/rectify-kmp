package presentation.feature.mirror

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.core.send.canClear
import presentation.feature.mirror.core.send.canSend
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorSideEffect
import presentation.feature.mirror.source.mirror.SendPhase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val PHONE_ID = "SEND-PHONE"
private val RECT_1 = DeviceRect(left = 10, top = 20, right = 110, bottom = 220)
private val RECT_2 = DeviceRect(left = 200, top = 220, right = 300, bottom = 420)

private fun phone(): DeviceModel = DeviceModel(
    id = PHONE_ID,
    displayName = "Pixel 8 Pro",
    serial = PHONE_ID,
    state = DeviceStateModel.Device,
    isEmulator = false,
    transportLabel = "USB",
)

private fun frame(byteCount: Int = 16): MirrorFrameModel = MirrorFrameModel(
    bytes = ByteArray(byteCount) { index -> (index + 1).toByte() },
    widthPx = 1080,
    heightPx = 2400,
    capturedAtMs = 0L,
)

private fun scriptedPhoneDiscovery(): FakeObserveDiscovery =
    FakeObserveDiscovery(listOf(DiscoveryModel.Found(physical = listOf(phone()), emulators = emptyList())))

/**
 * T-9 — the Resolve & Send handler, TAC-1..TAC-5. TAC-6 and TAC-7 live in
 * `desktopTest/MirrorViewModelResolveAndSendDesktopTest.kt`: the former needs JVM reflection to
 * force an otherwise-unreachable invariant break, the latter needs real JVM thread identity.
 *
 * Every test here freezes a real pinned frame through the SAME public intents
 * [MirrorViewModelHeldFramePairingTest] uses (discover, commit, push a frame, stage an
 * annotation) rather than constructing [presentation.feature.mirror.source.mirror.FrozenFrame]
 * by hand, so a send is attempted against the exact state shape the running app would produce.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelResolveAndSendTest {

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        // Required, not optional -- Orbit's defaults are PROCESS-WIDE (see
        // MirrorRecoveryActionsTest's identical note); omitting this would make these tests pass
        // only by inheriting a sibling test class's prior configuration.
        useTestDispatcherForOrbit(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Orbit.resetDefaults()
        Dispatchers.resetMain()
    }

    /** Freezes a frame via real intents and stages one annotation per entry of [rects]. */
    private suspend fun TestScope.freezeAndStage(
        viewModel: presentation.feature.mirror.source.mirror.MirrorViewModel,
        mirroring: FakeStartMirroring,
        rects: List<DeviceRect>,
    ) {
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()
        mirroring.pushFrame(frame())
        advanceUntilIdle()
        rects.forEach { rect ->
            viewModel.handleIntent(MirrorIntent.StageAnnotation(rect))
            advanceUntilIdle()
        }
    }

    /**
     * Drains BOTH clocks a few times.
     *
     * The handler's own "assemble off the main thread" requirement (AS-30 / TAC-7) means its
     * implementation very plausibly hops onto the REAL `Dispatchers.Default` thread pool for
     * part of the work, not just the virtual-time dispatcher Orbit is pinned to here.
     * `advanceUntilIdle()` alone only drains the virtual scheduler and returns the instant
     * nothing on IT is runnable -- a coroutine genuinely parked on a different, real dispatcher
     * does not count as "idle" to it, so a result that depends on that hop finishing could be
     * read one line too early. Alternating a virtual drain with a short REAL delay gives that
     * hop real wall-clock time to land its continuation back onto the pinned dispatcher before
     * the next virtual drain. The fakes used in this file do no real work, so this settles in
     * one or two iterations in practice; the loop is a safety margin, not a sleep-and-hope retry.
     */
    private suspend fun TestScope.settle() {
        repeat(10) {
            advanceUntilIdle()
            withContext(Dispatchers.Default) { delay(1) }
        }
        advanceUntilIdle()
    }

    // -- TAC-1: a second ResolveAndSend while one is in flight is dropped -----------------------

    @Test
    fun aSecondResolveAndSendWhileInFlightIsDropped() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val handoff = FakeAnnotationHandoff(manual = true)
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
            handoff = handoff,
        )
        freezeAndStage(viewModel, mirroring, listOf(RECT_1))

        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        settle()
        assertTrue(
            viewModel.currentState.send.isInFlight,
            "setup did not reach an in-flight phase -- the rest of this test would be vacuous",
        )

        // The backstop under test: a second dispatch while the first is still parked in publish.
        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        settle()

        handoff.release()
        settle()

        assertEquals(
            1,
            handoff.published.size,
            "a second concurrent ResolveAndSend must not reach publish a second time",
        )
    }

    // -- TAC-2: ResolveAndSend at zero staged is dropped -----------------------------------------

    @Test
    fun resolveAndSendAtZeroStagedIsDropped() = runTest(mainDispatcher) {
        val handoff = FakeAnnotationHandoff()
        val viewModel = mirrorViewModel(handoff = handoff)
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        advanceUntilIdle()

        assertEquals(SendPhase.Idle, viewModel.currentState.send)
        assertTrue(handoff.published.isEmpty(), "nothing staged must mean nothing published")
    }

    // -- TAC-3: success resumes live and reports the PRE-reset count ----------------------------

    @Test
    fun successResumesLiveAndTheToastReportsTheCountFromBeforeTheReset() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val handoff = FakeAnnotationHandoff()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
            handoff = handoff,
            nowMs = { 1_760_000_000_000L },
        )
        val effects = mutableListOf<MirrorSideEffect>()
        freezeAndStage(viewModel, mirroring, listOf(RECT_1, RECT_2))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.container.sideEffectFlow.collect { effects += it }
        }
        advanceUntilIdle()
        assertEquals(2, viewModel.currentState.stagedCount, "setup did not stage two annotations")

        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        settle()

        val sent = effects.filterIsInstance<MirrorSideEffect.SendComplete>().singleOrNull()
        assertNotNull(sent, "ResolveAndSend over a healthy session did not post SendComplete")
        assertEquals(
            2,
            sent.count,
            "the toast must report the count read BEFORE the reset, not the post-reset 0",
        )
        assertNull(viewModel.currentState.frozen, "a healthy-session success must resume live")
        assertNull(viewModel.frozenFrame.value, "the pinned bitmap must be released along with it")
        assertEquals(0, viewModel.currentState.stagedCount)
        assertEquals(1, handoff.published.size)
        assertEquals(2, handoff.published.single().annotations.size)
    }

    // -- TAC-4: success while Lost does NOT resume live, but still confirms (SC-27) -------------

    @Test
    fun successWhileTheSessionIsLostDoesNotResumeLiveButStillConfirms() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val handoff = FakeAnnotationHandoff()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
            handoff = handoff,
        )
        val effects = mutableListOf<MirrorSideEffect>()
        freezeAndStage(viewModel, mirroring, listOf(RECT_1))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.container.sideEffectFlow.collect { effects += it }
        }
        advanceUntilIdle()

        mirroring.failStream(IllegalStateException("device vanished"), frame())
        advanceUntilIdle()
        assertTrue(
            viewModel.currentState.session is MirrorSession.Lost,
            "setup did not actually drop the session -- the rest of this test would be vacuous",
        )
        assertNotNull(
            viewModel.currentState.frozen,
            "a dropped session must not by itself release the pinned frame (see Reconnect's own KDoc)",
        )

        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        settle()

        val sent = effects.filterIsInstance<MirrorSideEffect.SendComplete>().singleOrNull()
        assertNotNull(sent, "a send over a Lost session must still confirm per SC-27")
        assertNotNull(
            viewModel.currentState.frozen,
            "resume live must be SKIPPED while the session is Lost (SC-27)",
        )
        assertNotNull(viewModel.frozenFrame.value, "the pinned bitmap must stay pinned too")
    }

    // -- TAC-5: failure leaves the frame, its bytes and the batch untouched ----------------------

    @Test
    fun failureLeavesTheFrozenFrameItsHeldBytesAndTheBatchUntouchedAndReenablesTheButton() =
        runTest(mainDispatcher) {
            val mirroring = FakeStartMirroring()
            val handoff = FakeAnnotationHandoff(failure = IllegalStateException("disk full"))
            val viewModel = mirrorViewModel(
                discovery = scriptedPhoneDiscovery(),
                mirroring = mirroring,
                decodeFrame = sizedFrameDecoder(),
                handoff = handoff,
            )
            freezeAndStage(viewModel, mirroring, listOf(RECT_1))

            val frozenBefore = viewModel.currentState.frozen
            val heldBefore = viewModel.frozenHeld.value
            assertNotNull(frozenBefore, "setup did not pin a frame")
            assertNotNull(heldBefore, "setup did not pin a HeldFrame")

            viewModel.handleIntent(MirrorIntent.ResolveAndSend)
            settle()

            assertEquals(SendPhase.Failed, viewModel.currentState.send)
            assertSame(
                frozenBefore,
                viewModel.currentState.frozen,
                "a failed send must not touch the frozen frame or its annotations at all",
            )
            assertSame(
                heldBefore,
                viewModel.frozenHeld.value,
                "a failed send must not touch the pinned HeldFrame bytes at all",
            )
            assertTrue(
                canSend(viewModel.currentState),
                "a failed send must re-enable the button over the SAME untouched batch (AC-20)",
            )
            assertFalse(viewModel.currentState.send.isInFlight)
        }

    // -- Stage 6 F-2: staging mid-send must not re-enable the buttons (AC-20/AC-21) -------------

    @Test
    fun stagingAnAnnotationWhileASendIsInFlightNeitherReEnablesTheButtonsNorJoinsTheBatch() =
        runTest(mainDispatcher) {
            val mirroring = FakeStartMirroring()
            val handoff = FakeAnnotationHandoff(manual = true)
            val viewModel = mirrorViewModel(
                discovery = scriptedPhoneDiscovery(),
                mirroring = mirroring,
                decodeFrame = sizedFrameDecoder(),
                handoff = handoff,
            )
            freezeAndStage(viewModel, mirroring, listOf(RECT_1))

            viewModel.handleIntent(MirrorIntent.ResolveAndSend)
            settle()
            assertTrue(
                viewModel.currentState.send.isInFlight,
                "setup did not reach an in-flight phase -- the rest of this test would be vacuous",
            )

            // The drag the user starts while the publish is parked.
            viewModel.handleIntent(MirrorIntent.StageAnnotation(RECT_2))
            settle()

            assertTrue(
                viewModel.currentState.send.isInFlight,
                "staging mid-send reset the phase and took the status overlay down (AC-20)",
            )
            assertFalse(
                canSend(viewModel.currentState),
                "staging mid-send re-enabled Resolve & Send while a send was running (AC-20)",
            )
            assertFalse(
                canClear(viewModel.currentState),
                "staging mid-send re-enabled Clear while a send was running (AC-20)",
            )
            assertEquals(
                1,
                viewModel.currentState.stagedCount,
                "the batch being published must not grow under the send",
            )

            handoff.release()
            settle()

            assertEquals(1, handoff.published.size)
            assertEquals(
                1,
                handoff.published.single().annotations.size,
                "the published batch must be exactly the one the send started with",
            )
        }
}
