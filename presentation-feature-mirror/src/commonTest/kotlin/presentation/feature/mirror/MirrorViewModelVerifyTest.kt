package presentation.feature.mirror

import androidx.compose.ui.graphics.ImageBitmap
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.HierarchyNodeModel
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.MirrorFrameModel
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.SentAnnotationModel
import domain.core.source.model.history.SentBatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Orbit
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.feature.mirror.core.verify.MIN_REGION_PIXELS
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorStage
import presentation.feature.mirror.source.mirror.MirrorViewModel
import presentation.feature.mirror.source.mirror.VerifyError
import presentation.feature.mirror.source.mirror.VerifyPhase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val BATCH_ID = "VERIFY-BATCH"
private const val PHONE_ID = "VERIFY-PHONE"
private const val FRAME_WIDTH = 1080
private const val FRAME_HEIGHT = 2400
private const val DENSITY_DPI = 420

/** Which frame a decode was asked for — the one seam serves the live stream AND the stored PNG. */
private const val BEFORE_TAG: Byte = 1
private const val AFTER_TAG: Byte = 2

private const val BLACK = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

/**
 * The annotated region, sized FROM [MIN_REGION_PIXELS] rather than from a literal.
 *
 * A retune of that constant must not be able to silently slide these cases into the
 * short-circuit branch, which would make every pixel assertion here vacuous.
 */
private val RECT = DeviceRect(
    left = 10,
    top = 20,
    right = 10 + MIN_REGION_PIXELS,
    bottom = 20 + MIN_REGION_PIXELS,
)

/** Far away from [RECT], so nothing in the hierarchy can snap to it. */
private val UNMATCHED_RECT = DeviceRect(
    left = 600,
    top = 1_600,
    right = 600 + MIN_REGION_PIXELS,
    bottom = 1_600 + MIN_REGION_PIXELS,
)

private fun phone(): DeviceModel = DeviceModel(
    id = PHONE_ID,
    displayName = "Pixel 8 Pro",
    serial = PHONE_ID,
    state = DeviceStateModel.Device,
    isEmulator = false,
    transportLabel = "USB",
)

private fun scriptedPhoneDiscovery(): FakeObserveDiscovery = FakeObserveDiscovery(
    listOf(DiscoveryModel.Found(physical = listOf(phone()), emulators = emptyList())),
)

private fun liveFrame(): MirrorFrameModel = MirrorFrameModel(
    bytes = byteArrayOf(AFTER_TAG),
    widthPx = FRAME_WIDTH,
    heightPx = FRAME_HEIGHT,
    capturedAtMs = 0L,
)

/**
 * Decodes the two tagged frames into fakes of a known size and a known flat colour.
 *
 * Flat colours and not the default gradient: the region diff is the point of several of these
 * tests, and "every channel differs by the full range" / "no channel differs at all" are the
 * only two inputs that cannot be argued with whatever the noise floor is tuned to.
 */
private fun verifyDecoder(
    beforeWidth: Int = FRAME_WIDTH,
    beforeHeight: Int = FRAME_HEIGHT,
    afterColour: Int = WHITE,
): suspend (ByteArray) -> ImageBitmap? = { bytes ->
    when (bytes.firstOrNull()) {
        BEFORE_TAG -> FakeFrameBitmap(beforeWidth, beforeHeight) { _, _ -> BLACK }
        AFTER_TAG -> FakeFrameBitmap(FRAME_WIDTH, FRAME_HEIGHT) { _, _ -> afterColour }
        else -> null
    }
}

private fun annotation(rect: DeviceRect, comment: String = "still clipped"): SentAnnotationModel =
    SentAnnotationModel(
        left = rect.left,
        top = rect.top,
        right = rect.right,
        bottom = rect.bottom,
        comment = comment,
    )

private fun sentBatch(
    annotations: List<SentAnnotationModel> = listOf(annotation(RECT)),
    frameBytes: ByteArray? = byteArrayOf(BEFORE_TAG),
): SentBatchModel = SentBatchModel(
    id = BATCH_ID,
    sentAtMs = 1_700_000_000_000L,
    deviceLabel = "Pixel 8 Pro",
    prompt = "fix the clipping",
    frameBytes = frameBytes,
    annotations = annotations,
)

/** One node, contained in [RECT] with room to spare, with no neighbour to misalign against. */
private fun matchingNode(): HierarchyNodeModel = HierarchyNodeModel(
    id = "item_price",
    className = "android.widget.TextView",
    left = RECT.left + 2,
    top = RECT.top + 2,
    right = RECT.right - 2,
    bottom = RECT.bottom - 2,
    isTappable = false,
    depth = 1,
)

private fun snapshot(
    nodes: List<HierarchyNodeModel> = listOf(matchingNode()),
    frameWidthPx: Int = FRAME_WIDTH,
    frameHeightPx: Int = FRAME_HEIGHT,
    densityDpi: Int = DENSITY_DPI,
): HierarchySnapshotModel = HierarchySnapshotModel(
    nodes = nodes,
    frameWidthPx = frameWidthPx,
    frameHeightPx = frameHeightPx,
    densityDpi = densityDpi,
)

/** Device px that measure [dp] at [DENSITY_DPI] — never a literal px height in a case below. */
private fun devicePx(dp: Int): Int = dp * DENSITY_DPI / 160

/** Height the annotated node had when the batch was sent, and the height it has now. */
private const val BEFORE_HEIGHT_DP = 14
private const val AFTER_HEIGHT_DP = 26

/** The annotated node as it was SENT: tappable and well under the touch-target floor. */
private fun shortTappableNode(): HierarchyNodeModel = matchingNode().copy(
    isTappable = true,
    bottom = matchingNode().top + devicePx(BEFORE_HEIGHT_DP),
)

/** The same node after the agent grew it — still inside [RECT]'s snap slack. */
private fun grownTappableNode(): HierarchyNodeModel = matchingNode().copy(
    isTappable = true,
    top = RECT.top - 2,
    bottom = RECT.top - 2 + devicePx(AFTER_HEIGHT_DP),
)

/**
 * T-9 — `verifyBatch`'s seven-step run and `ExitVerify`'s teardown, TAC-1..TAC-7.
 *
 * Every abort case asserts the three things together, never just the first: the named
 * [VerifyError], an EMPTY row list, and ZERO status writes. An abort that leaked a row would
 * read as a verdict about a run that never ran, and one that wrote a status would move a batch
 * on evidence it never gathered (C-2).
 *
 * No threshold is ever asserted by value — [MIN_REGION_PIXELS] only ever CONSTRUCTS an input
 * here (`bugs.json#PP-TD-4/5`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelVerifyTest {

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        // Required, not optional -- Orbit's defaults are PROCESS-WIDE, so without this these
        // tests would pass only by inheriting a sibling class's configuration.
        useTestDispatcherForOrbit(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Orbit.resetDefaults()
        Dispatchers.resetMain()
    }

    /**
     * Drains BOTH clocks a few times.
     *
     * The run hops onto the real `Dispatchers.Default` for its pixel work, and
     * `advanceUntilIdle()` only drains the virtual scheduler: a coroutine parked on a real
     * dispatcher does not count as "idle" to it. Alternating a virtual drain with a short real
     * delay gives that hop wall-clock time to land its continuation back on the pinned
     * dispatcher. Same helper, same reason, as `MirrorViewModelResolveAndSendTest`.
     */
    private suspend fun TestScope.settle() {
        repeat(10) {
            advanceUntilIdle()
            withContext(Dispatchers.Default) { delay(1) }
        }
        advanceUntilIdle()
    }

    /** Opens a real capture session through the same intents a user would. */
    private suspend fun TestScope.startSession(
        viewModel: MirrorViewModel,
        mirroring: FakeStartMirroring,
    ) {
        subscribe(viewModel)
        advanceUntilIdle()
        viewModel.handleIntent(MirrorIntent.Commit)
        advanceUntilIdle()
        mirroring.pushFrame(liveFrame())
        advanceUntilIdle()
    }

    // -- TAC-1: no live session -----------------------------------------------------------------

    @Test
    fun noLiveSessionAbortsBeforeTheBatchIsEvenRead() = runTest(mainDispatcher) {
        val batches = FakeGetSentBatch(sentBatch())
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            getSentBatch = batches,
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        subscribe(viewModel)
        advanceUntilIdle()

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyError.NoLiveSession, verify.error)
        assertTrue(verify.rows.isEmpty(), "an aborted run must carry no verdicts")
        assertTrue(batches.calls.isEmpty(), "there is nothing to compare a batch against")
        assertTrue(status.writes.isEmpty(), "an abort must never move the batch on")
    }

    // -- TAC-2: nothing on disk -----------------------------------------------------------------

    @Test
    fun aMissingBatchIsNotOnDisk() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            getSentBatch = FakeGetSentBatch(batch = null),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyError.NotOnDisk, verify.error)
        assertTrue(verify.rows.isEmpty())
        assertTrue(status.writes.isEmpty())
    }

    @Test
    fun anUndecodableStoredFrameIsNotOnDisk() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            // Tagged as neither frame, so the decode seam answers null -- a file that is there
            // but is not a readable PNG is as unverifiable as one that is gone.
            getSentBatch = FakeGetSentBatch(sentBatch(frameBytes = byteArrayOf(9))),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyError.NotOnDisk, verify.error)
        assertTrue(verify.rows.isEmpty())
        assertTrue(status.writes.isEmpty())
    }

    // -- TAC-3: the frames are different sizes --------------------------------------------------

    @Test
    fun aFrameSizeDisagreementAbortsBeforeAnyPerAnnotationWork() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val dump = FakeDumpHierarchy(Result.success(snapshot()))
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = dump,
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            // The stored frame is a rotation of the live one.
            decodeFrame = verifyDecoder(beforeWidth = FRAME_HEIGHT, beforeHeight = FRAME_WIDTH),
        )
        startSession(viewModel, mirroring)
        val dumpsBefore = dump.calls.size

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyError.SizeMismatch, verify.error)
        assertTrue(verify.rows.isEmpty())
        assertEquals(dumpsBefore, dump.calls.size, "the ~2.7 s dump must not have been paid")
        assertTrue(status.writes.isEmpty())
    }

    // -- TAC-4: unusable hierarchy vs an absent one ---------------------------------------------

    @Test
    fun aHierarchyInAnotherCoordinateSpaceIsUnusable() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            // The measured Pixel 8 display override: real nodes, wrong coordinate space.
            dumpHierarchy = FakeDumpHierarchy(
                Result.success(snapshot(frameWidthPx = 984, frameHeightPx = 2132)),
            ),
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyError.HierarchyUnusable, verify.error)
        assertTrue(verify.rows.isEmpty(), "N false verdicts would be worse than none")
        assertTrue(status.writes.isEmpty())
    }

    @Test
    fun anEmptyHierarchyStillProducesOneStillOffRowPerAnnotation() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(HierarchySnapshotModel.Empty)),
            getSentBatch = FakeGetSentBatch(
                sentBatch(listOf(annotation(RECT), annotation(UNMATCHED_RECT))),
            ),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertNull(verify.error, "an absent hierarchy degrades per annotation, it does not abort")
        assertEquals(2, verify.rows.size)
        assertEquals(listOf(0, 1), verify.rows.map { it.index }, "history order, 0-based")
        assertTrue(verify.rows.all { it.outcome == VerificationOutcome.StillOff })
        assertTrue(status.writes.isEmpty())
    }

    // -- CR-A: the BEFORE hierarchy is projected too ---------------------------------------------

    @Test
    fun aStoredBeforeHierarchyProducesTheFindingsOwnBeforeAfterLine() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot(listOf(grownTappableNode())))),
            getSentBatch = FakeGetSentBatch(
                sentBatch().copy(
                    // Parsed by the repository, which is the only layer that may parse it. Its
                    // own density is the 0 sentinel -- the stored dump never carried one.
                    hierarchy = snapshot(listOf(shortTappableNode()), densityDpi = 0),
                ),
            ),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val row = assertNotNull(viewModel.currentState.verify).rows.single()
        assertEquals(
            "height $BEFORE_HEIGHT_DP" + "dp → $AFTER_HEIGHT_DP" + "dp · want 48dp",
            row.resultLine,
            "the touch-target line, not the size fallback the BEFORE-less path produces",
        )
        // Both sides are the snapped NODE now, not the hand-drawn rect they used to be.
        assertEquals(devicePx(BEFORE_HEIGHT_DP), row.beforeRect.height)
        assertEquals(devicePx(AFTER_HEIGHT_DP), row.afterRect.height)
    }

    @Test
    fun aBatchWithNoStoredHierarchyKeepsTheDrawnRectAndTheSizeLine() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot())),
            // Every batch recorded before the dump was parsed back looks like this.
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = FakeUpdateBatchStatus(),
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val row = assertNotNull(viewModel.currentState.verify).rows.single()
        assertEquals(RECT, row.beforeRect, "nothing to snap to means the annotation's own rect")
        assertTrue(
            '×' in row.resultLine,
            "the size pair is still the fallback, not a fabricated finding: ${row.resultLine}",
        )
    }

    // -- TAC-5: the terminal write ---------------------------------------------------------------

    @Test
    fun aRunWhereEveryRowIsFixedWritesVerifiedExactlyOnce() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot())),
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyPhase.Ready, verify.phase)
        assertEquals(listOf(VerificationOutcome.Fixed), verify.rows.map { it.outcome })
        assertEquals(listOf(BATCH_ID to BatchStatusModel.Verified), status.writes)
    }

    @Test
    fun aMixedRunWritesNoStatus() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot())),
            getSentBatch = FakeGetSentBatch(
                sentBatch(listOf(annotation(RECT), annotation(UNMATCHED_RECT))),
            ),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(
            listOf(VerificationOutcome.Fixed, VerificationOutcome.StillOff),
            verify.rows.map { it.outcome },
        )
        assertTrue(status.writes.isEmpty(), "one unfixed row is enough to withhold Verified")
    }

    @Test
    fun aZeroRowRunWritesNoStatus() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot())),
            getSentBatch = FakeGetSentBatch(sentBatch(annotations = emptyList())),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertTrue(verify.rows.isEmpty())
        assertTrue(
            status.writes.isEmpty(),
            "every row being fixed is vacuously true at zero rows -- it must not mark Verified",
        )
    }

    @Test
    fun anUnchangedRegionIsStillOffEvenWhenTheNodeMatchesCleanly() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot())),
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            // The AFTER frame is pixel-identical to the BEFORE one: the agent changed nothing.
            decodeFrame = verifyDecoder(afterColour = BLACK),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(
            listOf(VerificationOutcome.StillOff),
            verify.rows.map { it.outcome },
            "a matched node with no finding is not a fix without change evidence",
        )
        assertTrue(status.writes.isEmpty())
    }

    // -- TAC-6: phases and cancellation ----------------------------------------------------------

    @Test
    fun phasesAdvanceInOrder() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val batches = FakeGetSentBatch(sentBatch(), manual = true)
        val dump = FakeDumpHierarchy(manual = true)
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = dump,
            getSentBatch = batches,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        // Every phase the reducer publishes, in order. Diffing is deliberately NOT asserted on
        // this trace: a StateFlow conflates, and the diff of a one-annotation batch can land
        // between two collections. What IS asserted is that nothing ever goes backwards.
        val seen = mutableListOf<VerifyPhase>()
        backgroundScope.launch {
            viewModel.container.stateFlow.collect { state ->
                state.verify?.phase?.let { if (seen.lastOrNull() != it) seen += it }
            }
        }

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()
        assertEquals(VerifyPhase.Loading, viewModel.currentState.verify?.phase)

        batches.release()
        settle()
        assertEquals(VerifyPhase.Recapturing, viewModel.currentState.verify?.phase)

        dump.resolve(Result.success(snapshot()))
        settle()
        assertEquals(VerifyPhase.Ready, viewModel.currentState.verify?.phase)

        assertEquals(
            seen.sortedBy { it.ordinal },
            seen,
            "a phase must never go backwards -- the strip would un-tick a finished step",
        )
    }

    @Test
    fun aSecondVerifyCancelsTheFirstSoOnlyOneTerminalReductionLands() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val dump = FakeDumpHierarchy(manual = true)
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = dump,
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()
        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()

        // Releases the SECOND run's dump; the first is parked on a gate nobody completes.
        dump.resolve(Result.success(snapshot()))
        settle()

        assertEquals(2, dump.calls.size, "setup check -- both runs reached the dump")
        assertEquals(
            listOf(BATCH_ID to BatchStatusModel.Verified),
            status.writes,
            "the cancelled run must not land a second terminal write",
        )
    }

    // -- AC-44: the device goes away mid-run -----------------------------------------------------

    @Test
    fun aDeviceThatGoesAwayMidRunIsDisconnectedAndWritesNoStatus() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val dump = FakeDumpHierarchy(manual = true)
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = dump,
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()
        viewModel.handleIntent(MirrorIntent.StopMirroring)
        settle()
        dump.resolve(Result.success(snapshot()))
        settle()

        val verify = assertNotNull(viewModel.currentState.verify)
        assertEquals(VerifyError.Disconnected, verify.error)
        assertTrue(verify.rows.isEmpty())
        assertTrue(status.writes.isEmpty(), "the batch keeps the status it had")
    }

    // -- TAC-7: ExitVerify ------------------------------------------------------------------------

    @Test
    fun exitVerifyClearsEveryVerifyFieldAndReturnsToTheLiveStage() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = FakeDumpHierarchy(Result.success(snapshot())),
            getSentBatch = FakeGetSentBatch(sentBatch()),
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()
        assertNotNull(viewModel.verifyFrames.value, "setup check -- the run published its pair")
        assertNotNull(viewModel.verifyAfterHeld, "setup check -- the run retained its frame")

        viewModel.handleIntent(MirrorIntent.ExitVerify)
        settle()

        assertNull(viewModel.currentState.verify)
        assertNull(viewModel.verifyFrames.value)
        assertNull(viewModel.verifyAfterHeld)
        assertNull(viewModel.pendingFollowUpBatchId, "no later send may inherit this batch")
        assertEquals(MirrorStage.Mirror, viewModel.currentState.stage)
    }

    @Test
    fun exitVerifyCancelsAnInFlightRunAndNothingLandsAfterwards() = runTest(mainDispatcher) {
        val mirroring = FakeStartMirroring()
        val dump = FakeDumpHierarchy(manual = true)
        val status = FakeUpdateBatchStatus()
        val viewModel = mirrorViewModel(
            discovery = scriptedPhoneDiscovery(),
            mirroring = mirroring,
            dumpHierarchy = dump,
            getSentBatch = FakeGetSentBatch(sentBatch()),
            updateBatchStatus = status,
            decodeFrame = verifyDecoder(),
        )
        startSession(viewModel, mirroring)

        viewModel.handleIntent(MirrorIntent.VerifyBatch(BATCH_ID))
        settle()
        viewModel.handleIntent(MirrorIntent.ExitVerify)
        settle()

        // The dump the cancelled run was waiting on answers late. Nothing may come of it.
        dump.resolve(Result.success(snapshot()))
        settle()

        assertNull(viewModel.currentState.verify, "a cancelled run must not reduce a verdict")
        assertNull(viewModel.verifyFrames.value)
        assertNull(viewModel.verifyAfterHeld)
        assertTrue(status.writes.isEmpty())
    }
}
