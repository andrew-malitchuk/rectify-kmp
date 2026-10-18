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
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val PHONE = "CLEAR-PROMPT-PHONE-01"

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
 * T-7 / bugs.json#PP-TD-2 (SC-30, FR-17) — `ClearStaged` is the one reducer every Clear caller
 * routes through, and it empties `annotations`/`selectedIndex` but leaves `promptOverride`
 * intact. That was cosmetic while only the Payload preview read the field; now `EnvelopeAssembly`
 * (F-7's T-6) reads `promptOverride ?: buildPrompt(...)` for the text actually sent to the agent,
 * so a stale override shipped on top of a cleared-then-restaged frame would describe deleted
 * annotations to Claude Code.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClearStagedPromptOverrideTest {

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
     * TAC-1: ClearStaged must null `promptOverride` while the frozen frame itself survives — a
     * fix that nulled the whole frame (e.g. treating Clear like ResumeLive) would be wrong too.
     */
    @Test
    fun clearStagedAlsoClearsPromptOverride() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.EditPrompt("check the contrast here"))
        advanceUntilIdle()
        assertEquals("check the contrast here", viewModel.currentState.frozen?.promptOverride)

        viewModel.handleIntent(MirrorIntent.ClearStaged)
        advanceUntilIdle()

        val frozen = viewModel.currentState.frozen
        assertNotNull(frozen, "ClearStaged must not drop the frame itself, only the override")
        assertNull(frozen.promptOverride)
        assertTrue(frozen.annotations.isEmpty())
    }

    /**
     * TAC-2: after Clear, staging a fresh annotation and assembling the prompt from current
     * state (`promptOverride ?: buildPrompt(frozen)`, the same precedence `MirrorPayloadTab`
     * uses) must describe the NEW annotation, never the stale override text. This exercises the
     * reducer plus the already-shipped `buildPrompt` (F-6) rather than stubbing T-6's
     * not-yet-written `EnvelopeAssembly`.
     */
    @Test
    fun promptAfterClearAndRestageDescribesTheNewAnnotationNotTheStaleOverride() = runTest(mainDispatcher) {
        val viewModel = streamingWithFrame(frameOf(4))
        viewModel.handleIntent(MirrorIntent.StageAnnotation(FIRST_RECT))
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.EditPrompt("fix the OLD deleted annotation's contrast"))
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.ClearStaged)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.StageAnnotation(SECOND_RECT))
        advanceUntilIdle()

        val frozen = viewModel.currentState.frozen
        assertNotNull(frozen)
        val effectivePrompt = frozen.promptOverride ?: buildPrompt(frozen) { null }

        assertFalse(
            effectivePrompt.contains("OLD deleted annotation"),
            "the assembled prompt must not still describe the cleared override",
        )
        assertTrue(effectivePrompt.contains("#1"), "the new annotation must be in the assembled prompt")
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
