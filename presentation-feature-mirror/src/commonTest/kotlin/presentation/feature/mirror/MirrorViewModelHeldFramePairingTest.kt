package presentation.feature.mirror

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorFrameModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.Orbit
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorIntent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private const val PHONE_ID = "HELDFRAME-PHONE"
private val RECT = DeviceRect(left = 10, top = 20, right = 310, bottom = 420)

private fun phone(): DeviceModel = DeviceModel(
    id = PHONE_ID,
    displayName = "Pixel 8 Pro",
    serial = PHONE_ID,
    state = DeviceStateModel.Device,
    isEmulator = false,
    transportLabel = "USB",
)

/**
 * A frame whose bytes are NOT all-zero (`ByteArray(n)` defaults to zeros, which would make two
 * same-length arrays `contentEquals` by accident). [sizedFrameDecoder] ties the decoded bitmap's
 * dimensions to the byte count, so distinguishable byte counts also decode to distinguishable
 * bitmaps.
 */
private fun frame(byteCount: Int): MirrorFrameModel = MirrorFrameModel(
    bytes = ByteArray(byteCount) { index -> (index + 1).toByte() },
    widthPx = 1080,
    heightPx = 2400,
    capturedAtMs = 0L,
)

/**
 * T-1 / TAC-1 — the pairing invariant, proven through [MirrorViewModel.frozenHeld]
 * (`internal`, test-only — see its KDoc for why the public `frame`/`frozenFrame` surface cannot
 * carry this).
 *
 * What this test actually guarantees: for a SCRIPTED sequence (push A, push B, freeze), the
 * [presentation.feature.mirror.source.mirror.HeldFrame] pinned by `stageAnnotation` carries a
 * bitmap and bytes that both belong to frame B, never a bitmap from A paired with B's bytes or
 * vice versa. It does NOT, and cannot, force the genuine concurrent interleaving the hypothesis
 * describes (a newly arrived frame landing between two independent `.value` reads) — that
 * requires real thread-level parallelism; `StandardTestDispatcher` is single-threaded and
 * cooperative, so two reads with no suspension point between them can never be interleaved by
 * anything else, no matter how the implementation is shaped. What rules the race out is
 * `HeldFrame` being ONE value on ONE `MutableStateFlow` — impossible-by-construction, not
 * test-caught. This test's job is the part that construction alone does not cover: that the
 * value which actually gets retained is the CORRECT one, and that both of its fields agree with
 * each other.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelHeldFramePairingTest {

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
    fun freezingPinsTheBitmapAndBytesOfTheSameSourceFrame() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(
            discovery = FakeObserveDiscovery(
                listOf(DiscoveryModel.Found(physical = listOf(phone()), emulators = emptyList())),
            ),
            mirroring = mirroring,
            decodeFrame = sizedFrameDecoder(),
        )
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()

        val frameA = frame(4)
        val frameB = frame(9)
        mirroring.pushFrame(frameA)
        advanceUntilIdle()
        mirroring.pushFrame(frameB)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.StageAnnotation(RECT))
        advanceUntilIdle()

        val held = assertNotNull(
            viewModel.frozenHeld.value,
            "stageAnnotation did not retain a HeldFrame on freeze",
        )
        // sizedFrameDecoder makes the bitmap's size a pure function of the byte count, so a
        // bitmap genuinely decoded from frameB's bytes has EXACTLY this shape. 104x208 (frameA's
        // shape) or any hybrid of the two would both be the mis-pairing this test exists to catch.
        assertEquals(109, held.bitmap.width, "pinned bitmap is not frame B's")
        assertEquals(218, held.bitmap.height, "pinned bitmap is not frame B's")
        assertContentEquals(frameB.bytes, held.bytes, "pinned bytes are not frame B's")
    }
}
