package presentation.feature.mirror

import androidx.compose.ui.graphics.ImageBitmap
import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.AnnotationHandoff
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
import presentation.feature.mirror.core.send.canSend
import presentation.feature.mirror.source.mirror.CropEncoder
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.HeldFrame
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorSideEffect
import presentation.feature.mirror.source.mirror.MirrorViewModel
import presentation.feature.mirror.source.mirror.SendPhase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val PHONE_ID = "SEND-PHONE-JVM"
private val RECT_1 = DeviceRect(left = 10, top = 20, right = 110, bottom = 220)

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
 * Forces [viewModel]'s pinned [HeldFrame] to `null` while leaving its reduced `frozen` state
 * alone — the exact gap TAC-6 exists to guard, and one that is otherwise unreachable through any
 * public intent: `stageAnnotation` only ever sets `currentFrozenFrame` and `currentFrozenHeld`
 * together, and every release (`releaseFrozenFrame`) clears both together. `java.lang.reflect`
 * is the only way to split a pair the public API keeps atomic on purpose — same precedent as
 * `MirrorUiStateSendFieldTest`'s reflection use.
 */
@Suppress("UNCHECKED_CAST")
private fun forceFrozenHeldToNull(viewModel: MirrorViewModel) {
    val field = MirrorViewModel::class.java.getDeclaredField("currentFrozenHeld")
    field.isAccessible = true
    val flow = field.get(viewModel) as MutableStateFlow<HeldFrame?>
    flow.value = null
}

/** Records the JVM thread name every [encode] call actually ran on. */
private class ThreadCapturingCropEncoder(private val bytes: ByteArray? = ByteArray(1)) : CropEncoder {
    val threadNames: MutableList<String> = mutableListOf()
    override suspend fun encode(frame: ImageBitmap, rect: DeviceRect): ByteArray? {
        threadNames += Thread.currentThread().name
        return bytes
    }
}

/** Records the JVM thread name every [publish] call actually ran on. */
private class ThreadCapturingAnnotationHandoff : AnnotationHandoff {
    val threadNames: MutableList<String> = mutableListOf()
    val published: MutableList<AnnotationEnvelope> = mutableListOf()
    override suspend fun publish(envelope: AnnotationEnvelope, images: List<HandoffImage>) {
        threadNames += Thread.currentThread().name
        published += envelope
    }
    override suspend fun latest(): AnnotationEnvelope? = published.lastOrNull()
    override fun resolve(envelope: AnnotationEnvelope, name: String): String = "file:///fake/$name"
}

/**
 * T-9's two JVM-only cases.
 *
 * TAC-6 needs `java.lang.reflect` to force an invariant the public API cannot break on its own;
 * TAC-7 needs real JVM thread identity, which virtual-time dispatchers do not
 * expose. That is why these live apart from
 * `MirrorViewModelResolveAndSendTest` (TAC-1..TAC-5).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelResolveAndSendDesktopTest {

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

    private suspend fun TestScope.freezeAndStage(
        viewModel: MirrorViewModel,
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

    /** See `MirrorViewModelResolveAndSendTest.settle`'s KDoc for why this alternation exists. */
    private suspend fun TestScope.settle() {
        repeat(10) {
            advanceUntilIdle()
            withContext(Dispatchers.Default) { delay(1) }
        }
        advanceUntilIdle()
    }

    // -- TAC-6: no HeldFrame fails visibly instead of returning silently -------------------------

    @Test
    fun aSendWithNoHeldFrameReducesFailedInsteadOfReturningSilently() = runTest(mainDispatcher) {
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
        assertNotNull(viewModel.currentState.frozen, "setup did not pin a frame")
        assertTrue(
            canSend(viewModel.currentState),
            "setup must look sendable to canSend -- that gap is exactly what this guard closes",
        )

        forceFrozenHeldToNull(viewModel)
        assertNull(viewModel.frozenHeld.value, "forcing the invariant break failed")

        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        settle()

        assertEquals(SendPhase.Failed, viewModel.currentState.send)
        assertTrue(handoff.published.isEmpty(), "a missing HeldFrame must never reach publish")
        assertTrue(
            effects.filterIsInstance<MirrorSideEffect.SendComplete>().isEmpty(),
            "a missing HeldFrame must never report a success",
        )
        // Not assertable here: that the Kermit warning the spec calls for is actually logged.
        // This test proves the VISIBLE half of "fails visibly rather than returning silently" --
        // the state transition and the absence of a false success -- which is the half a test
        // can hold a mutation to. The log call itself is a review item.
    }

    // -- TAC-7: assembly, encoding and publishing do not run on the pinned main dispatcher --------

    @Test
    fun assemblyEncodingAndPublishingDoNotRunOnTheThreadDrivingTheMainDispatcher() = runTest(mainDispatcher) {
        // StandardTestDispatcher never spawns a thread of its own -- every virtual-time task it
        // runs, runs on whichever real thread drives advanceUntilIdle()/runCurrent(), which is
        // this one. Anything that lands on a DIFFERENT real thread provably did not run on the
        // dispatcher Orbit is pinned to here.
        val mainThreadName = Thread.currentThread().name
        val mirroring = FakeStartMirroring()
        val cropEncoder = ThreadCapturingCropEncoder()
        val handoff = ThreadCapturingAnnotationHandoff()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
            handoff = handoff,
            encodeCrop = cropEncoder,
        )
        freezeAndStage(viewModel, mirroring, listOf(RECT_1))

        viewModel.handleIntent(MirrorIntent.ResolveAndSend)
        settle()

        assertTrue(handoff.published.isNotEmpty(), "setup did not reach publish -- nothing to prove")
        (cropEncoder.threadNames + handoff.threadNames).forEach { threadName ->
            assertNotEquals(
                mainThreadName,
                threadName,
                "ran on the thread driving the pinned main dispatcher -- not off the main thread",
            )
        }
    }
}
