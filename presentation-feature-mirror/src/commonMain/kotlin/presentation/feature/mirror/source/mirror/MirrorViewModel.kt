package presentation.feature.mirror.source.mirror

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.HandoffImage
import data.handoff.api.source.handoff.AnnotationHandoff
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.SentAnnotationModel
import domain.core.source.model.history.SentBatchModel
import domain.usecase.api.source.usecase.device.BootEmulatorUseCase
import domain.usecase.api.source.usecase.device.DetectToolsUseCase
import domain.usecase.api.source.usecase.device.DumpHierarchyUseCase
import domain.usecase.api.source.usecase.device.ObserveDeviceDiscoveryUseCase
import domain.usecase.api.source.usecase.device.ObserveMirrorStatsUseCase
import domain.usecase.api.source.usecase.device.StartMirroringUseCase
import domain.usecase.api.source.usecase.history.GetSentBatchUseCase
import domain.usecase.api.source.usecase.history.ObserveHistoryUseCase
import domain.usecase.api.source.usecase.history.RecordSentBatchUseCase
import domain.usecase.api.source.usecase.history.UpdateBatchStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlin.time.Clock
import presentation.feature.mirror.core.discovery.errorCard
import domain.usecase.api.source.usecase.device.RefreshDiscoveryUseCase
import domain.usecase.api.source.usecase.device.RestartAdbServerUseCase
import domain.usecase.api.source.usecase.device.SetAdbPathUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import presentation.feature.mirror.core.discovery.applyDiscovery
import presentation.feature.mirror.core.history.cropAndScale
import presentation.feature.mirror.core.history.thumbnailRect
import presentation.feature.mirror.core.history.unionRect
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.NO_MATCH_NODE_ID
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.core.measure.projectMeasureNodes
import presentation.feature.mirror.core.measure.replaceNoteLine
import presentation.feature.mirror.core.payload.PAYLOAD_FORMAT_VERSION
import presentation.feature.mirror.core.picker.nextSelectableIndex
import presentation.feature.mirror.core.send.assembleEnvelope
import presentation.feature.mirror.core.verify.changedRatio
import presentation.feature.mirror.core.verify.followUpAnnotations
import presentation.feature.mirror.core.verify.regionPixels
import presentation.feature.mirror.core.verify.beforeAfterLine
import presentation.feature.mirror.core.verify.verdictOf
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.feature.mirror.core.send.canSend
import presentation.feature.mirror.core.send.toastLabel
import co.touchlab.kermit.Logger
import kotlin.time.TimeSource

/**
 * Shortest gap between two reduced stats updates. AC-25 asks for "at least once a second";
 * the transport reports far more often than that, and every extra reduction is a full state
 * copy nobody can read.
 */
private const val STATS_TICK_MS = 1_000L

private val log = Logger.withTag("MirrorViewModel")

/** Decodes a captured PNG off the main thread, or `null` when the bytes are not an image. */
private suspend fun decodePng(bytes: ByteArray): ImageBitmap? =
    withContext(Dispatchers.Default) { runCatching { bytes.decodeToImageBitmap() }.getOrNull() }

/**
 * The decoded bitmap and the raw bytes it was decoded from (T-1 / TAC-1).
 *
 * Pairing both on ONE value is what makes a freeze's read atomic: with two separate flows,
 * [MirrorViewModel.stageAnnotation] would need two independent `.value` reads to rebuild this
 * pair, and a newly arrived frame could land between them, pinning a bitmap from one frame
 * alongside bytes from another. [MirrorViewModel.frozenHeld] is the only thing that reads this
 * type; it must never reach [MirrorUiState] (TAC-3).
 */
internal data class HeldFrame(val bitmap: ImageBitmap, val bytes: ByteArray)

/**
 * Projects a published send into the durable record.
 *
 * Every field comes from the **envelope**, never from [MirrorUiState]. The prompt especially:
 * a `promptOverride` can outlive the annotations it described (`bugs.json#PP-TD-2`), and
 * history has to record what the agent actually received, not what the editor still holds
 * (PLAN-AS-12).
 *
 * Nothing here is read back out of the handoff slot: [envelope] and [images] are the same
 * in-memory values that were published, so the slot's own pruning cannot reach history
 * (FR-27/AC-38).
 *
 * @param envelope The envelope the agent received.
 * @param images Everything it referenced — the frame plus one crop per annotation that encoded.
 * @param frameBytes The adb capture itself, straight off [HeldFrame].
 * @param thumbPng The scaled batch thumbnail, or `null` when there was none to make.
 */
private fun sentBatch(
    envelope: AnnotationEnvelope,
    images: List<HandoffImage>,
    frameBytes: ByteArray,
    thumbPng: ByteArray?,
): SentBatchModel = SentBatchModel(
    id = envelope.sessionId,
    sentAtMs = envelope.sentAtMs,
    deviceLabel = envelope.device.label,
    prompt = envelope.prompt,
    frameBytes = frameBytes,
    thumbPng = thumbPng,
    hierarchyXml = envelope.hierarchyXml.takeIf { it.isNotEmpty() },
    annotations = envelope.annotations.map { annotation ->
        SentAnnotationModel(
            left = annotation.left,
            top = annotation.top,
            right = annotation.right,
            bottom = annotation.bottom,
            comment = annotation.comment,
            // Looked up rather than re-encoded: assembleEnvelope already produced these bytes.
            cropBytes = annotation.cropPath
                ?.let { path -> images.firstOrNull { it.name == path }?.bytes },
        )
    },
)

/**
 * The mirror surface's state machine.
 *
 * Two deliberate splits make this cheap at ten frames a second:
 *
 * - **Pixels bypass Orbit.** Decoded frames are published on [frame], a plain `StateFlow`
 *   outside the reduced state. A `ByteArray`-bearing type compares by identity, so putting one
 *   in [MirrorUiState] would mean a full state copy per frame that could never deduplicate.
 * - **Stats are throttled, not streamed.** The transport reports per frame; the reducer sees
 *   at most one update a second.
 *
 * Discovery and the capture loop are mutually exclusive: [sessionDeviceId] gates both through
 * `flatMapLatest`, so polling pauses while a session streams (one `adb` conversation at a
 * time) and rapid start/stop can never leave two capture loops alive.
 *
 * @property detectTools Launch-time probe of the platform binaries.
 * @property observeDiscovery The 2 s discovery poll; its cadence lives in the data layer.
 * @property bootEmulator Launches a cold AVD.
 * @property startMirroring Opens a capture session on a device.
 * @property observeMirrorStats Live transport statistics.
 * @property refreshDiscovery Cuts the poll interval short so a Retry acts now.
 * @property restartAdbServer Kills and restarts the adb daemon — AC-27's primary action, and
 *   the only thing that clears a WEDGED daemon (`start-server` alone hangs against one).
 * @property setAdbPath Persists the `adb` the user pointed Rectify at (AC-26).
 * @property dumpHierarchy Captures the view hierarchy to measure against. Fired once per
 *   freeze, never per keystroke: T-1 measured the dump at ~2.7 s, so asking for one when
 *   Option goes down would make measure mode unusable (NFR-2).
 * @property recordSentBatch Makes a successful send durable (AC-38). REQUIRED, no default,
 *   exactly like [handoff]: a default would turn a missing Koin binding into history silently
 *   losing every batch instead of a startup wiring error.
 * @property observeHistory The recorded batches, newest first — collected once at container
 *   init into [MirrorUiState.history]. REQUIRED for the same reason as [recordSentBatch].
 * @property getSentBatch Rehydrates one recorded batch — the BEFORE half of a verification run
 *   (F-9 / AC-23). REQUIRED, no default, for the same reason as [recordSentBatch].
 * @property updateBatchStatus Moves a verified batch to [BatchStatusModel.Verified] (AC-43).
 *   REQUIRED, no default, for the same reason as [recordSentBatch].
 * @property decodeFrame Seam for PNG decoding — a parameter so tests need no real image.
 * @property handoff T-9's publish target for a resolved send (AC-17/AC-19/FR-16/FR-18).
 *   REQUIRED, no default — a default would turn a missing Koin binding into a silent runtime
 *   no-op rather than a startup wiring error.
 * @property encodeCrop Encodes one annotation's padded crop region to PNG bytes for the send
 *   pipeline (T-5). Defaults to a no-op that always reports a missing crop, mirroring the
 *   AS-29 Android/iOS stand-in until a real desktop binding is wired.
 * @property nowMs Wall-clock seam for `sentAtMs`/`mintSessionId` — a parameter so a test can
 *   assert an exact session id instead of racing the real clock.
 * @property elapsed Monotonic seam for timing a send end to end — a parameter so a test is never
 *   forced to assert against wall-clock timing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class MirrorViewModel(
    private val detectTools: DetectToolsUseCase,
    private val observeDiscovery: ObserveDeviceDiscoveryUseCase,
    private val bootEmulator: BootEmulatorUseCase,
    private val startMirroring: StartMirroringUseCase,
    private val observeMirrorStats: ObserveMirrorStatsUseCase,
    private val restartAdbServer: RestartAdbServerUseCase,
    private val refreshDiscovery: RefreshDiscoveryUseCase,
    private val setAdbPath: SetAdbPathUseCase,
    private val dumpHierarchy: DumpHierarchyUseCase,
    private val recordSentBatch: RecordSentBatchUseCase,
    private val observeHistory: ObserveHistoryUseCase,
    private val getSentBatch: GetSentBatchUseCase,
    private val updateBatchStatus: UpdateBatchStatusUseCase,
    private val decodeFrame: suspend (ByteArray) -> ImageBitmap? = ::decodePng,
    private val handoff: AnnotationHandoff,
    private val encodeCrop: CropEncoder = CropEncoder { _, _ -> null },
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val elapsed: TimeSource = TimeSource.Monotonic,
) : ContainerHost<MirrorUiState, MirrorSideEffect>, ViewModel() {

    /** `null` while nothing streams — the gate both discovery and the capture loop read. */
    private val sessionDeviceId = MutableStateFlow<String?>(null)

    /**
     * The live frame as ONE [HeldFrame] — bitmap and the bytes it decoded from.
     *
     * Written in the same statement as [currentFrame], from the same collected value, so a
     * freeze needs exactly one `.value` read to obtain a bitmap and bytes that agree.
     */
    private val currentHeld = MutableStateFlow<HeldFrame?>(null)

    private val currentFrame = MutableStateFlow<ImageBitmap?>(null)

    /** The latest decoded frame. Deliberately not part of [MirrorUiState]. */
    public val frame: StateFlow<ImageBitmap?> = currentFrame.asStateFlow()

    private val currentFrozenFrame = MutableStateFlow<ImageBitmap?>(null)

    /**
     * The pinned frame, or `null` while the stage is live.
     *
     * A second `StateFlow` beside [frame] rather than a field of [MirrorUiState], for the same
     * reason [frame] is one: an `ImageBitmap` compares by identity, so a state copy carrying it
     * could never deduplicate. The reducer keeps only its [IntSize], in
     * [MirrorUiState.frozen]. The annotation list is the opposite case and does live in state.
     */
    public val frozenFrame: StateFlow<ImageBitmap?> = currentFrozenFrame.asStateFlow()

    private val currentFrozenHeld = MutableStateFlow<HeldFrame?>(null)

    /**
     * T-1 (TAC-1) test seam: the decoded bitmap and the raw bytes it was decoded from, pinned as
     * ONE [HeldFrame] value so a freeze's read is atomic.
     *
     * `internal`, not `public`: nothing on the public surface pairs bytes with the bitmap
     * ([frame] / [frozenFrame] stay `ImageBitmap`-only) and a [HeldFrame] must never reach
     * [MirrorUiState] (TAC-3 / `presentation-feature-mirror/CLAUDE.md` deviation 2). `internal`
     * is exactly the visibility this needs: invisible outside the module, visible to this
     * module's own tests — see `MirrorViewModelHeldFramePairingTest`.
     *
     * Set by [stageAnnotation] at the same moment [frozenFrame] is, from the same single read
     * of [currentHeld]; cleared wherever the pinned frame is released.
     */
    internal val frozenHeld: StateFlow<HeldFrame?> = currentFrozenHeld.asStateFlow()

    private val currentFrozenHierarchy = MutableStateFlow(HierarchySnapshotModel.Empty)

    /**
     * The hierarchy captured for the pinned frame, or [HierarchySnapshotModel.Empty].
     *
     * A third `StateFlow` beside [frame] and [frozenFrame], for a different reason than theirs:
     * a dump is a few hundred nodes read only by `core/measure`'s arithmetic and never rendered,
     * so reducing it would copy a list nobody reads on every unrelated state change.
     * [MirrorUiState.hierarchyStatus] is the part the UI does read, and that IS reduced.
     *
     * Never `null`: [HierarchySnapshotModel.Empty] already means "nothing usable", and
     * `projectMeasureNodes` turns it into the `View (no match)` fallback that FR-41 asks for.
     */
    public val frozenHierarchy: StateFlow<HierarchySnapshotModel> =
        currentFrozenHierarchy.asStateFlow()

    /** The dump in flight, so a clear site can cancel it instead of reducing a stale verdict. */
    private var dumpJob: Job? = null

    private val currentVerifyFrames = MutableStateFlow<VerifyFrames?>(null)

    /**
     * The BEFORE/AFTER pair a verification run renders, or `null` outside one.
     *
     * A fourth `StateFlow` beside [frame], [frozenFrame] and [frozenHierarchy], for the same
     * reason the first two are: these are bitmaps, and deviation 2 keeps pixels out of the
     * reduced state. [MirrorUiState.verify] carries the row semantics and nothing else.
     */
    public val verifyFrames: StateFlow<VerifyFrames?> = currentVerifyFrames.asStateFlow()

    /**
     * The verification run in flight, so a second [MirrorIntent.VerifyBatch] or an
     * [MirrorIntent.ExitVerify] can cancel it rather than let a stale verdict land.
     *
     * The [dumpJob] precedent taken one step further: the whole run — its reductions included —
     * is the ONE coroutine `intent {}` returns, so a cancel can never be raced by a reduce that
     * was already dispatched from a separate job.
     */
    private var verifyJob: Job? = null

    /**
     * The live frame the AFTER half of a run was measured against, retained for the follow-up.
     *
     * `internal`, exactly like [frozenHeld] and for the same reason: a [HeldFrame] must never
     * reach [MirrorUiState], but this module's own tests have to see that
     * [MirrorIntent.ExitVerify] let go of it (`bugs.json#PP-TD-2`).
     */
    internal var verifyAfterHeld: HeldFrame? = null
        private set

    /**
     * The AFTER dump the run measured against, retained for the follow-up — or `null` when the
     * run produced none it could measure with (an empty dump).
     *
     * A plain field and not reduced state, for the reason `presentation-feature-mirror/CLAUDE.md`
     * deviation 2 gives for [frozenHierarchy] itself: a few hundred nodes, identity-compared,
     * read only by arithmetic. It pairs with [verifyAfterHeld] — the same frame, so attaching it
     * to the follow-up is the one case where the dump provably describes the frame being sent.
     */
    internal var verifyAfterHierarchy: HierarchySnapshotModel? = null
        private set

    /**
     * The batch a follow-up send is being published FOR, or `null` for a plain send.
     *
     * Declared and cleared here, with the other four verify-scoped fields, because
     * [exitVerify]'s whole job is that none of them survives an exit — a leftover id would make
     * the next, unrelated send write a status on a batch it has nothing to do with
     * (`bugs.json#PP-TD-2`). The send path that consumes it lands with the follow-up itself.
     */
    internal var pendingFollowUpBatchId: String? = null
        private set

    /**
     * Freezes so far this session, stamped onto [FrozenFrame.frameId].
     *
     * Incremented in [stageAnnotation]'s **intent body**, never inside its `reduce` — Orbit
     * reduces through a CAS loop that re-runs the lambda on a lost race, which would advance
     * this twice for one freeze and mis-stamp the frame.
     */
    private var freezeCounter: Int = 0

    override val container: Container<MirrorUiState, MirrorSideEffect> =
        container(MirrorUiState()) {
            detectInstalledTools()
            observeDiscoveryWhileIdle()
            observeFrames()
            observeStats()
            observeHistoryEntries()
        }

    /** Dispatches a user action. */
    public fun handleIntent(intent: MirrorIntent) {
        when (intent) {
            is MirrorIntent.SelectPlatform -> selectPlatform(intent.platform)
            MirrorIntent.OpenPicker -> setPickerOpen(true)
            MirrorIntent.ClosePicker -> closePicker()
            is MirrorIntent.HighlightRow -> highlightRow(intent.id)
            is MirrorIntent.MoveHighlight -> moveHighlight(intent.delta)
            MirrorIntent.Commit -> commit()
            is MirrorIntent.BootRow -> bootRow(intent.id)
            is MirrorIntent.SelectAvd -> selectAvd(intent.avdName)
            MirrorIntent.BootSelectedAvd -> bootSelectedAvd()
            MirrorIntent.StopMirroring -> stopSession()
            is MirrorIntent.StageAnnotation -> stageAnnotation(intent.rect)
            is MirrorIntent.SelectAnnotation -> selectAnnotation(intent.index)
            is MirrorIntent.CommentAnnotation -> commentAnnotation(intent.index, intent.comment)
            is MirrorIntent.DeleteAnnotation -> deleteAnnotation(intent.index)
            is MirrorIntent.StartMeasure -> startMeasure(intent.index)
            MirrorIntent.EndMeasure -> endMeasure()
            MirrorIntent.ClearStaged -> clearStaged()
            is MirrorIntent.EditPrompt -> editPrompt(intent.text)
            MirrorIntent.ResolveAndSend -> resolveAndSend()
            MirrorIntent.ResumeLive -> resumeLive()
            MirrorIntent.Reconnect -> reconnect()
            MirrorIntent.RetryDiscovery -> retryDiscovery()
            MirrorIntent.RestartAdb -> restartAdb()
            MirrorIntent.LocateAdb -> locateAdb()
            is MirrorIntent.AdbPathChosen -> adbPathChosen(intent.path)
            is MirrorIntent.VerifyBatch -> verifyBatch(intent.batchId)
            MirrorIntent.ExitVerify -> exitVerify()
            MirrorIntent.SendFollowUp -> sendFollowUp()
            MirrorIntent.MarkAllVerified -> markAllVerified()
        }
    }

    private fun detectInstalledTools() = intent {
        // A missing binary is a value, not a failure: the probe list still names both
        // platforms so the picker can say "adb not found" instead of showing nothing.
        val tools = detectTools().getOrNull().orEmpty()
        reduce { state.copy(tools = tools) }
    }

    /**
     * No `.catch` here, unlike [observeFrames], and the asymmetry is deliberate: `catch` is
     * terminal, so guarding the discovery stream at this end would render one failure and
     * then stop polling for the life of the process. The guard sits inside the repository's
     * poll loop, which is the only place that can report the failure AND try again two
     * seconds later; a `DeviceCommandException` therefore arrives here as a
     * `DiscoveryModel.PollFailed` value. [observeFrames] is the opposite case - a lost
     * device really does end that session, so terminating is the right behaviour there.
     */
    private fun observeDiscoveryWhileIdle() {
        viewModelScope.launch {
            sessionDeviceId
                .flatMapLatest { device -> if (device == null) observeDiscovery() else emptyFlow() }
                .collect { discovery -> intent { reduce { state.applyDiscovery(discovery) } } }
        }
    }

    private fun observeFrames() {
        viewModelScope.launch {
            sessionDeviceId
                .flatMapLatest { device ->
                    if (device == null) {
                        // Clears the stage when a session ends, without a second signal path.
                        flowOf(null)
                    } else {
                        startMirroring(device)
                            // The captured bytes ride along with the bitmap they decoded to:
                            // frame.png must be the adb capture itself, never a re-encode, and
                            // only one value on one flow makes the freeze's read atomic.
                            .map { captured ->
                                decodeFrame(captured.bytes)
                                    ?.let { bitmap -> HeldFrame(bitmap, captured.bytes) }
                            }
                            .catch { onSessionLost(device) }
                    }
                }
                .collect { held ->
                    currentHeld.value = held
                    currentFrame.value = held?.bitmap
                }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            observeMirrorStats()
                .conflate()
                .collect { stats ->
                    intent { reduce { state.copy(stats = stats) } }
                    // Throttles by sleeping the collector: conflate keeps only the newest
                    // report, so this is a ticker without an always-pending timer.
                    delay(STATS_TICK_MS)
                }
        }
    }

    /**
     * Mirrors [observeStats]' shape with neither its throttle nor a `.catch`: the database
     * reports once per send rather than once per frame, and the repository already degrades an
     * unreadable table to an empty list, so there is no failure left to guard here.
     */
    private fun observeHistoryEntries() {
        viewModelScope.launch {
            observeHistory().collect { entries ->
                intent { reduce { state.copy(history = entries) } }
            }
        }
    }

    /**
     * Re-captures the device and diffs it, annotation by annotation, against the batch that was
     * sent (FR-32 / AC-23 / AC-24 / SC-8).
     *
     * Seven ordered steps, all of them inside the ONE coroutine `intent {}` returns and all of
     * them cancellable through [verifyJob]:
     *
     * 1. **A live session is required.** `StartMirroringUseCase` is stream-only and there is no
     *    one-shot capture anywhere in this app, so the AFTER frame can only be the one already
     *    streaming. [currentHeld] is read ONCE, exactly as [stageAnnotation] reads it, so the
     *    bitmap and the bytes behind it agree. Nothing streaming is [VerifyError.NoLiveSession]
     *    — and the batch is never even read, because there would be nothing to compare it to.
     * 2. **The BEFORE batch** comes from [getSentBatch]; absent, or with a `frame.png` that is
     *    gone or no longer decodable, is [VerifyError.NotOnDisk].
     * 3. **One frame size, or none.** A stored frame that disagrees with the live one means a
     *    rotation, a resolution change or a different device, so every stored rect would land
     *    somewhere else: [VerifyError.SizeMismatch], raised BEFORE any per-annotation work.
     *    From here there is exactly one [IntSize] and one density for both sides.
     * 4. **The AFTER hierarchy** is a fresh dump — the ~2.7 s step (T-1), which is why
     *    [VerifyPhase.Recapturing] exists and why this whole run is cancellable. A dump that
     *    FAILS means the device went away ([DumpHierarchyUseCase] degrades an unreadable dump
     *    to [HierarchySnapshotModel.Empty] and fails only on a dead device, a missing `adb` or
     *    a timeout), so it reads as [VerifyError.Disconnected].
     * 5. **Projection is all-or-nothing.** [projectMeasureNodes] discards a snapshot whose own
     *    frame size disagrees with the frame (the measured Pixel 8 984x2132-vs-1080x2400
     *    display override), so a NON-EMPTY dump that projects to zero candidates is a
     *    coordinate-space mismatch: every verdict would be confident and wrong, hence
     *    [VerifyError.HierarchyUnusable] and zero rows. A dump that was empty to begin with is
     *    a different case and is NOT an abort — it degrades per annotation to `View (no match)`
     *    and therefore to "Still off", exactly as AC-41 asks.
     * 6. **Per annotation**, in history's own 0-based order: one [measure] against the AFTER
     *    candidates (it already snaps and already computes its finding — there is no second
     *    oracle), one region read per frame, one [changedRatio], one [verdictOf].
     * 7. **The terminal write** happens only when there are rows AND all of them are fixed. The
     *    emptiness guard is the point: without it a zero-annotation batch would satisfy "every
     *    row is fixed" vacuously and mark itself [BatchStatusModel.Verified] having verified
     *    nothing.
     *
     * Every abort carries a named [VerifyError] with an EMPTY row list and writes no status —
     * a run that could not run must never be readable as a set of verdicts.
     *
     * **A device change does not CANCEL this run, and must not.** The `flatMapLatest`
     * teardown on [sessionDeviceId] tears down the capture loop and leaves [verifyJob] alone,
     * because a cancelled run reduces nothing at all: the stage would sit on
     * [VerifyPhase.Recapturing] with no error and no way back. The run instead re-reads
     * [sessionDeviceId] after every suspension point — decode, dump, diff — and ends itself
     * with [VerifyError.Disconnected], which is a state the user can retry from. A device
     * SWAP reads the same way as a device loss: the comparison is against the id the run
     * started on, not against null.
     *
     * **Both hierarchies are projected**, not just the AFTER one: `HistoryRepository.get`
     * parses the stored dump in the data layer (where the parser legally lives) and hands it
     * back as [SentBatchModel.hierarchy], so step 6 measures the annotation TWICE — once per
     * frame — and [VerifyRow] carries node-to-node geometry on both sides. Only the AFTER
     * measurement feeds `verdictOf`; the BEFORE one exists to say what the complaint measured
     * when it was filed. A batch with no stored dump (or one stored before this existed) snaps
     * to nothing on the BEFORE side, which degrades every row to the annotation's own rect and
     * the size line — exactly the behaviour that shipped before.
     *
     * **Still a real gap — density.** The stored dump is `uiautomator dump` stdout alone, so
     * the BEFORE snapshot's own `densityDpi` is the `0` sentinel. The fresh AFTER density is
     * applied to both sides rather than labelling half the row in px; persisting it would cost
     * a column, a `@Database` bump and a migration that could not help any row already on disk.
     */
    private fun verifyBatch(batchId: String) {
        // Cancelled HERE rather than inside the new run: a second Verify must not be able to
        // interleave with the old one's reductions at all.
        verifyJob?.cancel()
        releaseVerify()
        verifyJob = intent { runVerify(batchId) }
    }

    private suspend fun Syntax<MirrorUiState, MirrorSideEffect>.runVerify(batchId: String) {
        val deviceId = sessionDeviceId.value
        val after = currentHeld.value
        var view = VerifyUiState(
            batchId = batchId,
            // Provisional: the batch's own label replaces it the moment the batch is read. The
            // NoLiveSession copy needs A device name and aborts before there is a batch to ask.
            deviceLabel = state.selectedDeviceName.orEmpty(),
            sentAtMs = state.history.firstOrNull { it.id == batchId }?.sentAtMs ?: 0L,
        )
        reduce { state.copy(verify = view) }
        if (deviceId == null || after == null) return abortVerify(view, VerifyError.NoLiveSession)

        val batch = getSentBatch(batchId).getOrNull()
        val beforeBytes = batch?.frameBytes
        if (batch == null || beforeBytes == null) {
            return abortVerify(view, VerifyError.NotOnDisk)
        }
        view = view.copy(deviceLabel = batch.deviceLabel, sentAtMs = batch.sentAtMs)
        val before = decodeFrame(beforeBytes) ?: return abortVerify(view, VerifyError.NotOnDisk)
        if (sessionDeviceId.value != deviceId) return abortVerify(view, VerifyError.Disconnected)

        if (before.width != after.bitmap.width || before.height != after.bitmap.height) {
            log.w {
                "Verify $batchId: stored frame ${before.width}x${before.height} does not match " +
                    "the live ${after.bitmap.width}x${after.bitmap.height}"
            }
            return abortVerify(view, VerifyError.SizeMismatch)
        }
        val frameSize = IntSize(width = after.bitmap.width, height = after.bitmap.height)

        view = view.copy(phase = VerifyPhase.Recapturing)
        reduce { state.copy(verify = view) }
        val snapshot = dumpHierarchy(deviceId).getOrNull()
        if (snapshot == null || sessionDeviceId.value != deviceId) {
            return abortVerify(view, VerifyError.Disconnected)
        }

        val nodes = projectMeasureNodes(snapshot, frameSize)
        if (snapshot.nodes.isNotEmpty() && nodes.isEmpty()) {
            log.w {
                "Verify $batchId: hierarchy is ${snapshot.frameWidthPx}x${snapshot.frameHeightPx} " +
                    "but the frame is ${frameSize.width}x${frameSize.height} — nothing to measure"
            }
            return abortVerify(view, VerifyError.HierarchyUnusable)
        }

        // Projected against the SAME frame size as the AFTER side, not against its own: step 3
        // already proved both frames measure the same, so a stored dump in another coordinate
        // space is as unmeasurable here as it is there. It is NOT an abort though — a BEFORE
        // side that cannot be projected costs the row its before/after line and nothing else.
        val beforeNodes = batch.hierarchy?.let { projectMeasureNodes(it, frameSize) }.orEmpty()

        view = view.copy(phase = VerifyPhase.Diffing, recapturedAtMs = nowMs())
        reduce { state.copy(verify = view) }
        verifyAfterHeld = after
        // Retained only when it PROJECTS: an empty dump measures nothing, so a follow-up over
        // it has to go out as Absent rather than claim a tree that snaps to no annotation.
        verifyAfterHierarchy = snapshot.takeIf { nodes.isNotEmpty() }
        currentVerifyFrames.value = VerifyFrames(before = before, after = after.bitmap)
        // The pixel work is up to a full frame per annotation, so it stays off the reducer's
        // own context — the same hop decodePng and the send path already take (AS-30).
        val rows = withContext(Dispatchers.Default) {
            batch.annotations.mapIndexed { index, annotation ->
                verifyRow(
                    index = index,
                    annotation = annotation,
                    nodes = nodes,
                    beforeNodes = beforeNodes,
                    before = before,
                    after = after.bitmap,
                    frameSize = frameSize,
                    // From the FRESH dump, applied to both sides: `wm density` was never
                    // persisted, so the stored hierarchy cannot supply one (AC-23's data gap).
                    densityDpi = snapshot.densityDpi,
                )
            }
        }
        if (sessionDeviceId.value != deviceId) return abortVerify(view, VerifyError.Disconnected)

        view = view.copy(phase = VerifyPhase.Ready, rows = rows)
        reduce { state.copy(verify = view) }

        // Guarded on emptiness, not merely on `all` — see step 7 in this function's KDoc.
        if (rows.isNotEmpty() && rows.all { it.outcome == VerificationOutcome.Fixed }) {
            updateBatchStatus(batchId, BatchStatusModel.Verified)
        }
    }

    /**
     * Ends the run with a named reason and NO verdicts (C-2).
     *
     * [VerifyUiState] enforces the empty-rows half at construction; this is the only place that
     * sets an error, so "every abort is rowless and writes no status" is one call site, not a
     * rule spread over six returns.
     *
     * The pixel pair goes with the verdicts (T-15). The last two aborts are reachable AFTER the
     * run published [verifyFrames] and retained [verifyAfterHeld] — a disconnect noticed at the
     * post-diff check is the common one — and leaving those behind would hand the stage two
     * full-size bitmaps for a run that has no rows, and leave a follow-up croppable from a
     * frame nobody is looking at, and a surviving [verifyAfterHierarchy] attaches a dead run's
     * node tree to the next send. The five verify-scoped fields must agree in BOTH directions,
     * not only on the way out through [exitVerify].
     */
    private suspend fun Syntax<MirrorUiState, MirrorSideEffect>.abortVerify(
        view: VerifyUiState,
        error: VerifyError,
    ) {
        releaseVerify()
        reduce { state.copy(verify = view.copy(rows = emptyList(), error = error)) }
    }

    /**
     * One annotation's verdict: both measurements, the region diff, and the AND-rule.
     *
     * [measure] is called once PER FRAME and the two calls have different jobs. The AFTER one
     * gives the verdict both halves it needs — `matched` is its node id not being the no-match
     * sentinel, and the finding is the one `findingOf` already produced; re-running either
     * would be a second oracle free to disagree with the first. The BEFORE one is display
     * only: it says where the annotated node sat and what was wrong with it when the batch was
     * sent, and it never reaches [verdictOf]. With no stored hierarchy it snaps to nothing and
     * reports the annotation's own rect, which is what this row carried before.
     *
     * The SAME rect is read on both frames: it is the region the annotation is about, and the
     * question the pixels answer is whether anything in it moved at all.
     */
    private fun verifyRow(
        index: Int,
        annotation: SentAnnotationModel,
        nodes: List<MeasureNode>,
        beforeNodes: List<MeasureNode>,
        before: ImageBitmap,
        after: ImageBitmap,
        frameSize: IntSize,
        densityDpi: Int,
    ): VerifyRow {
        val rect = DeviceRect(
            left = annotation.left,
            top = annotation.top,
            right = annotation.right,
            bottom = annotation.bottom,
        )
        val readout = measure(
            annotation = rect,
            // measure() names annotations by their 1-based badge; VerifyRow keeps history's
            // own 0-based order. The two numbering schemes meet here and nowhere else.
            index = index + 1,
            nodes = nodes,
            frameSize = frameSize,
            densityDpi = densityDpi,
        )
        val beforeReadout = measure(
            annotation = rect,
            index = index + 1,
            nodes = beforeNodes,
            frameSize = frameSize,
            // The AFTER dump's density on the BEFORE frame too: the stored dump never carried
            // one, and labelling the two halves of one line in different units is worse.
            densityDpi = densityDpi,
        )
        val beforePixels = before.regionPixels(rect, frameSize)
        val afterPixels = after.regionPixels(rect, frameSize)
        val changed = if (beforePixels == null || afterPixels == null) {
            0f
        } else {
            changedRatio(beforePixels, afterPixels)
        }
        return VerifyRow(
            index = index,
            comment = annotation.comment,
            beforeRect = beforeReadout.targetRect,
            afterRect = readout.targetRect,
            resultLine = beforeAfterLine(
                before = beforeReadout,
                after = readout,
                densityDpi = densityDpi,
            ),
            outcome = verdictOf(
                matched = readout.nodeId != NO_MATCH_NODE_ID,
                afterFinding = readout.finding,
                changedRatio = changed,
                regionPx = beforePixels?.size ?: 0,
            ),
        )
    }

    /**
     * Re-sends the rows a run reported as still off, as a NEW batch (AC-42 / FR-47 / SC-9).
     *
     * The whole re-stage is four assignments and one reduction, and then the send path F-7
     * already ships runs unchanged — including its failure and retry UI:
     *
     * - **[currentFrozenHeld] becomes [verifyAfterHeld]**, with [currentFrozenFrame] beside it
     *   as everywhere else in this class. That one pairing is what makes "cropped from the
     *   AFTER frame" true with no new crop code: `resolveAndSend` encodes crops and `frame.png`
     *   out of the held frame it finds, and the one it now finds is the re-captured one.
     * - **The annotations are renumbered** by `followUpAnnotations` — the only place the rows'
     *   0-based history order becomes a 1-based badge, which the envelope's `crop-N.png` names
     *   depend on.
     * - **[FrozenFrame.promptOverride] is explicitly `null`** (`bugs.json#PP-TD-7`/`PP-TD-2`):
     *   an override belongs to the frame it was typed against, and carrying the old one onto a
     *   new frame would describe annotations it was never about. The comments ride along
     *   verbatim — v1 re-files the complaint, it does not reframe it.
     * - **[MirrorUiState.verify] is cleared in the SAME reduction that sets `frozen`.**
     *   [MirrorStage.Verify] is derived AHEAD of [MirrorStage.Frozen], so a staging that left
     *   it set would pin the user on a stale Verify view with the send UI unreachable behind
     *   it (the coupling is recorded on [MirrorUiState.stage] itself).
     *
     * **The hierarchy goes with the frame too** — [verifyAfterHierarchy], not whatever the
     * previous frozen frame left behind. It was dumped against the AFTER frame this stages, so
     * it is the one node tree that provably describes what is being sent; a follow-up therefore
     * carries the same payload an ordinary send does. A run whose dump measured nothing leaves
     * it `null` and the follow-up is honestly sent with `hierarchyStatus = Absent`.
     *
     * [pendingFollowUpBatchId] is a plain field and not reduced state precisely so it survives
     * that teardown — see where it is declared, and [resolveAndSend], which consumes it.
     */
    private fun sendFollowUp() {
        // Cancelled out here rather than inside the intent, exactly as [exitVerify] does it: an
        // in-flight run must not be able to reduce a verdict over the frame being staged.
        verifyJob?.cancel()
        verifyJob = null
        intent {
            val view = state.verify ?: return@intent
            val held = verifyAfterHeld ?: return@intent
            // Read before [releaseVerify] clears it — it is one of the five fields that go.
            val hierarchy = verifyAfterHierarchy
            val stillOff = view.stillOffRows
            // The button is hard-disabled at zero and [resolveAndSend] owns the in-flight
            // window; both are re-checked here so a stray dispatch cannot stage an empty batch
            // or re-freeze the frame a publish is already reading.
            if (stillOff.isEmpty() || state.send.isInFlight) return@intent

            currentFrozenFrame.value = held.bitmap
            currentFrozenHeld.value = held
            // The run owned that frame; the frozen slot owns it now. Everything else it held
            // goes with it, through the same helper [exitVerify] uses -- the pending id is
            // re-armed below, deliberately AFTER this.
            releaseVerify()
            // Drops whatever the PREVIOUS frozen frame left behind (and any dump still running
            // for it), then attaches the run's own AFTER dump — the one taken against this very
            // frame. A run that produced nothing measurable leaves Empty, i.e. Absent.
            releaseHierarchy()
            if (hierarchy != null) currentFrozenHierarchy.value = hierarchy
            val frozen = FrozenFrame(
                deviceSize = IntSize(width = held.bitmap.width, height = held.bitmap.height),
                annotations = followUpAnnotations(stillOff),
                // Stamped in the intent body, never inside `reduce` -- same CAS reason as
                // [stageAnnotation]'s.
                frameId = ++freezeCounter,
                deviceLabel = state.selectedRowDetailWithoutTransport(),
                promptOverride = null,
            )
            pendingFollowUpBatchId = view.batchId
            reduce {
                state.copy(frozen = frozen, verify = null, send = SendPhase.Idle)
                    .withoutMeasurements()
                    // After [withoutMeasurements], which zeroes the status for the frame that
                    // just went away; the retained dump is the new frame's, and `Ready` is what
                    // makes `resolveAndSend` attach it (AS-29 / AF-4's one gate).
                    .copy(
                        hierarchyStatus =
                            if (hierarchy != null) HierarchyStatus.Ready else HierarchyStatus.Absent,
                    )
            }
            // Dispatched AFTER the reduce has landed, so the send reads the batch this just
            // staged rather than the state it replaced.
            resolveAndSend()
        }
    }

    /**
     * The tester's override: this batch is fixed, whatever the diff said (AC-43 / SC-20).
     *
     * The run is cancelled FIRST, so a late completion cannot reduce its own verdicts over the
     * human's — the same ordering, for the same reason, as [verifyBatch]'s own cancel.
     *
     * **It does not leave the stage, and that is deliberate.** Forcing every row to
     * [VerificationOutcome.Fixed] is what the user sees happen: the panel's `n of m` becomes
     * `m of m` and every row flips. Exiting in the same breath would throw that confirmation
     * away and make the override indistinguishable from a click that did nothing.
     * [MirrorIntent.ExitVerify] stays the one and only exit — it is the documented owner of
     * the five-field teardown, and the "← Back to live" chip is on screen throughout.
     *
     * A rowless view writes nothing: an error carries no verdicts by construction, and a run
     * that has not produced any rows yet has nothing to overrule — marking a batch verified on
     * a view with zero rows is exactly the vacuous write step 7 of [runVerify] guards against,
     * and this manual path reaches the same end state under the same rule.
     */
    private fun markAllVerified() {
        verifyJob?.cancel()
        verifyJob = null
        intent {
            val view = state.verify ?: return@intent
            if (view.error != null || view.rows.isEmpty()) return@intent
            reduce {
                val current = state.verify ?: return@reduce state
                state.copy(
                    verify = current.copy(
                        rows = current.rows.map { it.copy(outcome = VerificationOutcome.Fixed) },
                    ),
                )
            }
            updateBatchStatus(view.batchId, BatchStatusModel.Verified)
        }
    }

    /**
     * Leaves the verify stage — the "← Back to live" chip, and the only exit (TAC-7).
     *
     * All five verify-scoped fields go together, which is the whole point
     * (`bugs.json#PP-TD-2`): a surviving [VerifyUiState] pins the user on a stale view with the
     * send UI hidden behind it, surviving pixels render the wrong pair into the next run, a
     * surviving [verifyAfterHeld] crops a follow-up from a frame nobody is looking at, a
     * surviving [verifyAfterHierarchy] attaches a dead run's node tree to the next send, and a
     * surviving [pendingFollowUpBatchId] writes a status on the next, unrelated send.
     *
     * The job is cancelled BEFORE the clearing intent is dispatched, so an in-flight run cannot
     * reduce a verdict in between. Clearing [MirrorUiState.verify] is also all the routing this
     * needs: the stage derivation falls straight back to the frozen frame or the live one, and
     * the side panel is already on History — it is where the Verify button was pressed.
     */
    private fun exitVerify() {
        verifyJob?.cancel()
        verifyJob = null
        releaseVerify()
        intent { reduce { state.copy(verify = null) } }
    }

    /** Drops everything a verification run owns outside the reduced state. */
    private fun releaseVerify() {
        currentVerifyFrames.value = null
        verifyAfterHeld = null
        verifyAfterHierarchy = null
        pendingFollowUpBatchId = null
    }

    private suspend fun onSessionLost(deviceId: String) {
        intent { reduce { state.copy(session = MirrorSession.Lost(deviceId)) } }
        sessionDeviceId.value = null
    }

    private fun selectPlatform(platform: PlatformModel) = intent {
        // iOS is detected but not mirrorable this release, so switching platform must stop the
        // running session rather than leave a frozen frame on the stage.
        sessionDeviceId.value = null
        releaseFrozenFrame()
        releaseHierarchy()
        reduce {
            state.copy(platform = platform, session = MirrorSession.Idle, frozen = null)
                .withoutMeasurements()
        }
    }

    private fun setPickerOpen(open: Boolean) = intent {
        reduce { state.copy(isPickerOpen = open) }
    }

    private fun closePicker() = intent {
        // Escape before the first selection would leave an empty stage with no way back — that
        // is why this is a no-op rather than a plain toggle, and F-2 confirmed it.
        //
        // COR-2 narrows it rather than removing it. The "no way back" premise assumed the stage
        // behind the picker is empty; with an error card available it is not. Dismissing the
        // picker on two attached handsets now reveals AC-28's ambiguity card, which HAS a route
        // onward: its primary action mirrors the top device, and the toolbar chip reopens the
        // picker (AC-43). Without this the card was unreachable in AC-28's own scenario — the
        // only way to close the picker was to resolve the ambiguity first, which is the thing
        // the card exists to help with.
        //
        // The original guard still holds where it was written for: no selection AND no card
        // means there is genuinely nothing behind the picker, so Escape still does nothing.
        // Asked counterfactually — "would a card be there if I closed?" — because `errorCard()`
        // yields to an OPEN picker, so asking it directly here always answers null and the
        // guard could never open.
        val somethingIsBehindIt = state.selectedDeviceId != null ||
            state.copy(isPickerOpen = false)
                .errorCard(Clock.System.now().toEpochMilliseconds()) != null
        if (somethingIsBehindIt) reduce { state.copy(isPickerOpen = false) }
    }

    private fun highlightRow(id: String) = intent {
        val index = state.rows.indexOfFirst { it.id == id && it.selectable }
        if (index >= 0) reduce { state.copy(highlightedIndex = index) }
    }

    private fun moveHighlight(delta: Int) = intent {
        val index = nextSelectableIndex(state.rows, state.highlightedIndex, delta)
        reduce { state.copy(highlightedIndex = index) }
    }

    private fun commit() = intent {
        val row = state.highlightedRow?.takeIf { it.selectable } ?: return@intent
        when (row.kind) {
            MirrorRowKind.Boot -> boot(row.avdName ?: row.id)
            MirrorRowKind.Detail, MirrorRowKind.Running -> {
                reduce {
                    state.copy(
                        isPickerOpen = false,
                        selectedDeviceId = row.id,
                        selectedDeviceName = row.name,
                        session = MirrorSession.Streaming(row.id),
                    )
                }
                sessionDeviceId.value = row.id
            }
        }
    }

    private fun bootRow(id: String) = intent {
        val row = state.rows.firstOrNull { it.id == id } ?: return@intent
        boot(row.avdName ?: row.id)
    }

    private fun selectAvd(avdName: String) = intent {
        reduce { state.copy(selectedAvd = avdName) }
    }

    private fun bootSelectedAvd() = intent {
        val avd = state.selectedAvd ?: state.avds.firstOrNull() ?: return@intent
        boot(avd)
    }

    private fun stopSession() = intent {
        sessionDeviceId.value = null
        releaseFrozenFrame()
        releaseHierarchy()
        reduce { state.copy(session = MirrorSession.Idle, frozen = null).withoutMeasurements() }
    }

    /**
     * Commits a finished drag, freezing the frame on the first one (AC-6).
     *
     * `currentFrame.value` is read **exactly once, here, at release** — that single read IS
     * AC-47: nothing is buffered at drag start, so a frame that lands mid-drag is the one that
     * gets pinned. The freeze is guarded on the bitmap being absent, which is what makes a
     * second drag add an annotation against the SAME pinned frame instead of re-freezing
     * (FR-31 / AC-52).
     *
     * Nothing here touches [sessionDeviceId]: freezing is a render decision, the capture loop
     * keeps running (FR-30), and that is why Resume Live costs no reconnection.
     *
     * This and every other annotation handler read `state.frozen` **inside** `reduce`. Orbit
     * launches each intent in its own coroutine, so a read taken outside is a pre-image two
     * overlapping intents can both capture, and the later `reduce` then writes back a copy
     * that never saw the earlier one — a per-keystroke comment racing a delete resurrects the
     * annotation the user just removed. Inside `reduce` the read is safe for a concrete
     * reason: Orbit reduces with `MutableStateFlow.update`, a CAS loop that re-runs the
     * lambda against the winning value whenever it loses the race.
     */
    private fun stageAnnotation(rect: DeviceRect) = intent {
        // A send in flight OWNS the batch it is publishing (AC-20). Staging into it would
        // re-enable Resolve & Send and Clear mid-publish, and the new annotation would then be
        // wiped unseen by the success path's batch clear (AC-21) or silently join a retry of a
        // batch it was never part of. The drag is dropped instead -- the in-flight window is
        // the one the status overlay is on screen for.
        if (state.send.isInFlight) return@intent
        val held = currentHeld.value ?: return@intent
        val freezing = currentFrozenFrame.value == null
        if (freezing) {
            // Both halves of the SAME read, pinned together: the bytes behind frame.png are
            // guaranteed to be the ones the pinned bitmap was decoded from (TAC-1).
            currentFrozenFrame.value = held.bitmap
            currentFrozenHeld.value = held
        }
        // Stamped HERE, in the intent body, and never inside `reduce`: the reducer's CAS loop
        // re-runs its lambda on a lost race, so an increment in there would burn two ids on one
        // freeze (T-1 / TAC-4).
        val stampedFrameId = if (freezing) ++freezeCounter else freezeCounter
        val stampedDeviceLabel = state.selectedRowDetailWithoutTransport()
        // Only a freeze starts a dump, and only when there is a device to ask: a second drag
        // annotates the SAME pinned frame (FR-31), whose hierarchy is already captured.
        val dumpTarget = state.selectedDeviceId?.takeIf { freezing }
        reduce {
            val frozen = state.frozen
                ?: FrozenFrame(
                    deviceSize = IntSize(width = held.bitmap.width, height = held.bitmap.height),
                    frameId = stampedFrameId,
                    deviceLabel = stampedDeviceLabel,
                )
            val staged = frozen.annotations +
                StagedAnnotation(index = frozen.annotations.size + 1, rect = rect)
            state.copy(
                frozen = frozen.copy(annotations = staged, selectedIndex = staged.size),
                // Reduced in the SAME pass as the freeze, so the status line can never show a
                // frozen frame with no dump accounted for (TAC-22).
                hierarchyStatus =
                    if (dumpTarget == null) state.hierarchyStatus else HierarchyStatus.Dumping,
                // A new annotation starts a new batch, so the previous attempt's TERMINAL
                // phase is retired here and the status overlay goes away. [SendPhase] has no
                // self-clearing transition out of [SendPhase.Sent]/[SendPhase.Failed] and
                // deliberately gets no timer and no extra intent: the three natural triggers
                // the user already has — this one, [clearStaged] and [resumeLive] — are what
                // put it back. This is also the ONLY one of the three that the SC-27 path
                // reaches, where a successful send keeps the frame pinned instead of resuming
                // live, so without it that path would leave the overlay on screen.
                //
                // Only out of a TERMINAL phase, never out of an in-flight one: the guard at the
                // top of this intent already drops the whole stage mid-send, and this is the
                // same rule restated where the reducer's CAS loop can see the winning value --
                // a send that starts between that read and this one still cannot be reset.
                send = if (state.send.isInFlight) state.send else SendPhase.Idle,
            )
        }
        if (dumpTarget != null) dumpHierarchyFor(dumpTarget)
    }

    /**
     * Captures the hierarchy behind the just-pinned frame, **fire and forget**.
     *
     * Launched on [viewModelScope] rather than awaited inside the freeze's own intent: the dump
     * costs ~2.7 s (measured, T-1) and the user's freeze has to be instant, so the result lands
     * behind a frame that is already on screen. The outcome is never discarded — success
     * reduces [HierarchyStatus.Ready], any failure reduces [HierarchyStatus.Unavailable], so the
     * status cannot be left stuck at [HierarchyStatus.Dumping].
     *
     * A dump that is still in flight when the frame is released is cancelled by
     * [releaseHierarchy] instead of being allowed to reduce a verdict about a frame that is gone.
     *
     * A dump whose own frame size disagrees with the frozen frame also reduces
     * [HierarchyStatus.Unavailable], even though the command itself succeeded.
     * [projectMeasureNodes] discards such a snapshot wholesale — the T-1 spike measured a 9.76 %
     * gap between what `uiautomator` reports and the physical screen, so numbers taken from a
     * mismatched hierarchy would be confidently wrong rather than absent. Reducing `Ready` here
     * would then leave the status claiming a usable hierarchy while every measure silently fell
     * back to `View (no match)` — the status contradicting its own behaviour, which is the defect
     * AC-72 and the `Dumping` label both exist to prevent.
     */
    private fun dumpHierarchyFor(deviceId: String) {
        dumpJob?.cancel()
        dumpJob = viewModelScope.launch {
            val snapshot = dumpHierarchy(deviceId).getOrNull()
            currentFrozenHierarchy.value = snapshot ?: HierarchySnapshotModel.Empty
            intent {
                reduce {
                    val usable = snapshot != null &&
                        projectMeasureNodes(snapshot, state.frozen?.deviceSize ?: IntSize.Zero)
                            .isNotEmpty()
                    state.copy(
                        hierarchyStatus =
                            if (usable) HierarchyStatus.Ready else HierarchyStatus.Unavailable,
                    )
                }
            }
        }
    }

    /**
     * Unpins the frozen frame — both the bitmap the stage renders and the [HeldFrame] behind
     * it, which are only ever written and cleared together.
     */
    private fun releaseFrozenFrame() {
        currentFrozenFrame.value = null
        currentFrozenHeld.value = null
    }

    /** Drops the captured hierarchy and any dump still running for a frame being released. */
    private fun releaseHierarchy() {
        dumpJob?.cancel()
        dumpJob = null
        currentFrozenHierarchy.value = HierarchySnapshotModel.Empty
    }

    /** The reduced half of [releaseHierarchy] — no frame means nothing to measure against. */
    private fun MirrorUiState.withoutMeasurements(): MirrorUiState =
        copy(measure = null, hierarchyStatus = HierarchyStatus.Absent)

    /**
     * Empties the staged batch while leaving the frame pinned — [clearStaged]'s whole body, and
     * what a successful send does over a [MirrorSession.Lost] session (SC-27), which is why it
     * is a shared extension rather than two copies of the same `copy`.
     *
     * `promptOverride` goes with the annotations (bugs.json#PP-TD-2): it was written to describe
     * the batch being emptied, and `EnvelopeAssembly` reads `promptOverride ?: buildPrompt(...)`
     * for the text actually sent, so keeping it would ship a prompt describing annotations that
     * no longer exist.
     */
    private fun MirrorUiState.withoutStagedBatch(): MirrorUiState {
        val frozen = frozen ?: return this
        return copy(
            frozen = frozen.copy(
                annotations = emptyList(),
                selectedIndex = null,
                promptOverride = null,
            ),
            measure = null,
        )
    }

    /**
     * Measures one annotation against the captured hierarchy (FR-36).
     *
     * Delegates the whole computation to `core/measure`'s pure [measure]: every rule about
     * snapping, neighbours, gaps, units, suppression and findings is decided there, where it is
     * testable without a ViewModel, and this handler only stores the result.
     *
     * An index that is no longer staged measures nothing rather than failing — the annotation
     * can be deleted between Option-down and Option-up.
     */
    private fun startMeasure(index: Int) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            val annotation = frozen.annotations.firstOrNull { it.index == index }
                ?: return@reduce state
            val snapshot = currentFrozenHierarchy.value
            state.copy(
                measure = measure(
                    annotation = annotation.rect,
                    index = index,
                    nodes = projectMeasureNodes(snapshot, frozen.deviceSize),
                    frameSize = frozen.deviceSize,
                    densityDpi = snapshot.densityDpi,
                ),
            )
        }
    }

    /**
     * Option released: the readout's `⌥` line goes into the annotation's note (FR-40).
     *
     * `replaceNoteLine` is what keeps re-measuring idempotent — exactly one glyph-prefixed line
     * survives, and the user's own prose is untouched (TAC-21).
     *
     * No readout means nothing was measured, so this is a no-op rather than an empty write; a
     * readout whose annotation has vanished leaves the state alone for the same reason.
     */
    private fun endMeasure() = intent {
        reduce {
            val readout = state.measure ?: return@reduce state
            val frozen = state.frozen ?: return@reduce state
            if (frozen.annotations.none { it.index == readout.annotationIndex }) return@reduce state
            val staged = frozen.annotations.map { annotation ->
                if (annotation.index != readout.annotationIndex) {
                    annotation
                } else {
                    annotation.copy(comment = replaceNoteLine(annotation.comment, readout.noteLine))
                }
            }
            state.copy(frozen = frozen.copy(annotations = staged), measure = null)
        }
    }

    private fun selectAnnotation(index: Int?) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            state.copy(frozen = frozen.copy(selectedIndex = index))
        }
    }

    private fun commentAnnotation(index: Int, comment: String) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            val staged = frozen.annotations.map { annotation ->
                if (annotation.index == index) annotation.copy(comment = comment) else annotation
            }
            state.copy(frozen = frozen.copy(annotations = staged))
        }
    }

    /**
     * Drops one annotation and renumbers the survivors so the badges stay contiguous (AC-8).
     *
     * The selection is resolved by following the selected annotation itself through the
     * renumbering rather than by arithmetic on the index: deleting the selected one clears the
     * popover, deleting anything else leaves it open on the same annotation under its new
     * number.
     */
    private fun deleteAnnotation(index: Int) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            val kept = frozen.annotations.filterNot { it.index == index }
            val renumbered = kept.mapIndexed { position, annotation ->
                annotation.copy(index = position + 1)
            }
            val selected = frozen.selected
                ?.takeIf { it.index != index }
                ?.let { annotation -> kept.indexOf(annotation).takeIf { it >= 0 }?.plus(1) }
            // The readout names its annotation by index and the survivors renumber, so the
            // index it holds now points at a DIFFERENT annotation. Dropping it is what stops an
            // EndMeasure that lost the race to a delete writing the note onto the survivor that
            // inherited the number.
            state.copy(
                frozen = frozen.copy(annotations = renumbered, selectedIndex = selected),
                measure = null,
            )
        }
    }

    /**
     * Empties the staged list. The frame stays pinned — only Resume Live unpins it (AC-48).
     *
     * See [withoutStagedBatch] for what "empties" covers and why `promptOverride` goes with it.
     *
     * Also retires a terminal [SendPhase] — the AS-31 route out of [SendPhase.Failed], where the
     * batch survived the failure and the user chooses to drop it rather than retry. The reset is
     * written here and NOT inside [withoutStagedBatch], which the success path calls one `copy`
     * after reducing the send phase. Since AF-5 the success path reduces [SendPhase.Idle]
     * directly, so the overlay never outlives the state it describes.
     */
    private fun clearStaged() = intent {
        reduce { state.withoutStagedBatch().copy(send = SendPhase.Idle) }
    }

    /**
     * AC-26's secondary and AC-53's primary — FR-37.
     *
     * Asks the repository's poll loop to skip the rest of its 2 s wait, so the Retry acts now
     * instead of looking identical to a dead button for up to two seconds.
     *
     * The trigger lives in the data layer rather than here on purpose. Restarting the
     * collection from this end was tried three ways and withdrawn: it had to be threaded
     * through the discovery gate F-2 shipped and confirmed, and it proved timing-dependent
     * (see execution/GAP-fr37-immediate-repoll.md). Beside the delay it shortens, it is
     * covered by virtual-time tests instead of Orbit's dispatcher scheduling.
     */
    private fun retryDiscovery() = intent {
        // No `.onFailure` here, and the asymmetry with restartAdb/adbPathChosen is deliberate:
        // refreshDiscovery bottoms out in `Channel.trySend`, which cannot throw, so a failure
        // branch would be a case the running app can never reach — and a test for it could
        // only be written by faking the use case, which would prove nothing about this code.
        // The sibling actions DO have real throwing paths (start/kill-server, a settings
        // write), which is why they report.
        refreshDiscovery()
    }

    /**
     * AC-27's primary. A real daemon restart, then an immediate re-poll.
     *
     * Deliberately NOT an alias of [retryDiscovery]: a button labelled "Restart adb" that only
     * re-polls is the lying copy `presentation-core-ui/CLAUDE.md` forbids, and measurement
     * makes it concrete — `start-server` alone cannot clear a wedged daemon, so re-polling
     * would be unable to fix the very state the user presses it in.
     *
     * The re-poll is awaited after the restart, not before: polling into a daemon that is
     * still coming up would spend the poll's budget and report the same failure again.
     */
    private fun restartAdb() = intent {
        // COR-7. The Optional is read, not discarded. Without this an unmounted SDK made the
        // primary action silently do nothing, which is exactly the dead button FR-37 forbids —
        // and the repository throws here specifically so this layer can say so.
        restartAdbServer()
            .onSuccess { refreshDiscovery() }
            .onFailure { postSideEffect(MirrorSideEffect.RecoveryFailed(RecoveryAction.RestartAdb)) }
    }

    /**
     * AC-26's primary. Asks the window layer for a file, since only it owns a file dialog.
     *
     * The chosen path arrives back as [MirrorIntent.AdbPathChosen]; nothing is validated here
     * (see [adbPathChosen]).
     */
    private fun locateAdb() = intent {
        postSideEffect(MirrorSideEffect.LocateAdb)
    }

    /**
     * Stores the path the dialog returned and immediately re-probes.
     *
     * No validation at this end on purpose: the path is checked against the filesystem by
     * `ToolResolver`'s existing `isExecutable` predicate, the same one every other candidate
     * passes through, so a path that is missing, a directory, or not executable simply falls
     * through to the ordered candidate list. Validating here as well would be a second
     * definition of "usable", free to drift from the one that actually decides.
     */
    private fun adbPathChosen(path: String) = intent {
        // Re-probe only if the path actually persisted: polling against a preference that was
        // not written would re-render the same ToolMissing card and read as the dialog having
        // been cancelled.
        setAdbPath(path)
            .onSuccess { refreshDiscovery() }
            .onFailure { postSideEffect(MirrorSideEffect.RecoveryFailed(RecoveryAction.SaveAdbPath)) }
    }

    /**
     * Stores the prompt the user typed for the pinned frame (AC-79).
     *
     * Dropped only by [resumeLive], which drops the whole frame — every annotation handler
     * copies [FrozenFrame] and so carries the override through an add, a delete and a
     * reconnection untouched. A `null` [text] clears it, which the Payload tab sends when it
     * discards an editing session whose seed went stale; no reducer clears it on its own.
     */
    private fun editPrompt(text: String?) = intent {
        reduce {
            val frozen = state.frozen ?: return@reduce state
            state.copy(frozen = frozen.copy(promptOverride = text))
        }
    }

    /**
     * The selected picker row's machine detail without its transport segment
     * (`"Android 15 · USB"` → `"Android 15"`).
     *
     * Read off the row that is already in state rather than reduced into a field of its own:
     * the detail string is the one place the Android release is already formatted, and the
     * stamp is taken once per freeze.
     */
    private fun MirrorUiState.selectedRowDetailWithoutTransport(): String =
        rows.firstOrNull { it.id == selectedDeviceId }
            ?.detail
            ?.substringBefore(" · ")
            .orEmpty()

    /**
     * Resolves the staged batch against the pinned frame and publishes it (AC-17/FR-16/FR-18).
     *
     * Two guards, not one, and they are not redundant:
     *
     * - [canSend] is the same predicate the button and the ⌘↩ holder read, re-evaluated here so
     *   a double dispatch cannot bypass it. It covers "nothing staged" and "already in flight".
     * - [currentFrozenHeld] is checked separately because `canSend` reads [MirrorUiState] only
     *   and the state carries no bytes (see this class's own KDoc). An enabled button over a
     *   frame whose [HeldFrame] went missing must fail visibly rather than return silently —
     *   hence [SendPhase.Failed] plus a warning, not a bare `return`.
     *
     * Assembly, crop encoding and the slot write all happen inside the two explicit
     * `withContext(Dispatchers.Default)` boundaries below (AS-30), the same hop [decodePng]
     * already uses for this file's other pixel-heavy path. Only the `reduce`/`postSideEffect`
     * calls stay on the intent's own context.
     *
     * Failure — including `UnsupportedAnnotationHandoff.publish` throwing on Android/iOS
     * (AS-29) — reduces [SendPhase.Failed] and touches nothing else: the frozen frame, its
     * [HeldFrame] bytes and the staged list all survive, so the retry is over the same batch
     * (AS-31/AC-20/SC-25).
     *
     * Success additionally records the batch (AC-38) — in its own failure domain, logged and
     * dropped, strictly before the toast; see the call site. Success clears the batch
     * (FR-19/AC-21) and resumes live — **unless** the session is
     * [MirrorSession.Lost], where the frame stays pinned because there is no live mirror to
     * return to (SC-27). The toast's count is read off the frame that was actually sent, which
     * is by construction the pre-reset count.
     */
    private fun resolveAndSend() = intent {
        // Read once and cleared in the same breath, BEFORE the first guard and before any
        // suspension: that is the `finally` this needs, bought without one. No path out of
        // this handler -- a dropped dispatch, a failure, a cancellation -- can leave the id
        // behind for the next, unrelated send to write a status with (`bugs.json#PP-TD-2`).
        //
        // Ceiling, taken knowingly: a follow-up whose publish FAILS and is then retried
        // through F-7's retry publishes the same batch but no longer relabels the original,
        // which keeps its `Sent` status. A status label is the only thing lost, and the
        // alternative -- an id that outlives its send -- is the bug this guard exists for.
        val followUpFor = pendingFollowUpBatchId
        pendingFollowUpBatchId = null
        if (!canSend(state)) return@intent
        val frozen = state.frozen
        val held = currentFrozenHeld.value
        if (frozen == null || held == null) {
            log.w { "Resolve & Send: the pinned frame has no held bytes — nothing to publish" }
            reduce { state.copy(send = SendPhase.Failed) }
            return@intent
        }

        val started = elapsed.markNow()
        reduce { state.copy(send = SendPhase.Preparing) }

        val hierarchy = currentFrozenHierarchy.value
        val hierarchyStatus = state.hierarchyStatus
        val platform = state.platform
        val deviceId = state.selectedDeviceId
        val sentAtMs = nowMs()

        val published = runCatching {
            val (envelope, images) = withContext(Dispatchers.Default) {
                assembleEnvelope(
                    frozen = frozen,
                    frameBytes = held.bytes,
                    hierarchy = hierarchy,
                    hierarchyStatus = hierarchyStatus,
                    platform = platform,
                    // Curried here because `assembleEnvelope` holds no bitmap of its own.
                    encodeCrop = { rect -> encodeCrop.encode(held.bitmap, rect) },
                    sentAtMs = sentAtMs,
                    deviceId = deviceId,
                    // Passed from this call site: the constant stays internal to core/payload.
                    version = PAYLOAD_FORMAT_VERSION,
                )
            }
            reduce { state.copy(send = SendPhase.Publishing) }
            withContext(Dispatchers.Default) { handoff.publish(envelope, images) }
            envelope to images
        }

        published
            .onFailure { error ->
                log.w(error) { "Resolve & Send failed — frame and batch left untouched" }
                reduce { state.copy(send = SendPhase.Failed) }
            }
            .onSuccess { (envelope, images) ->
                // Read off the frame that was sent, so it is the pre-reset count by
                // construction rather than by ordering luck (FR-19).
                val count = frozen.annotations.size
                // Read here, not at the toast: the user waited on the publish, not on the
                // thumbnail encode and history write that follow it (and the first send in a
                // process pays Skiko's one-off native-library load inside that encode).
                val elapsedMs = started.elapsedNow().inWholeMilliseconds
                if (state.session is MirrorSession.Lost) {
                    // AF-5 (Stage 5): reset to Idle here, not Sent. Leaving the terminal phase
                    // set kept AC-19's final dot visible but let the overlay outlive the state it
                    // describes -- post-success it rendered a "Frozen" dot with nothing frozen and
                    // "Dumping hierarchy" back at Pending, falsifying sendSteps' own invariant.
                    // AC-19's evidence now rests on the toast and the T-13 manual demo.
                    reduce { state.copy(send = SendPhase.Idle).withoutStagedBatch() }
                } else {
                    releaseFrozenFrame()
                    releaseHierarchy()
                    reduce {
                        state.copy(frozen = null, send = SendPhase.Idle).withoutMeasurements()
                    }
                }
                // AC-38, and the ordering is the whole guarantee: history is written
                // BEFORE the toast is posted, and its failure is logged and dropped right
                // here. It never reaches SendPhase and never posts a side effect -- a send
                // the agent received stays successful even when the database does not.
                val thumbPng = withContext(Dispatchers.Default) {
                    thumbnailPng(frozen, held.bitmap)
                }
                recordSentBatch(sentBatch(envelope, images, held.bytes, thumbPng)).onFailure { error ->
                    log.w(error) { "History not recorded for ${envelope.sessionId} -- the send stands" }
                }
                // AC-42's other half, and only ever on SUCCESS: the ORIGINAL batch is marked
                // as followed up, after the follow-up's own `Sent` row exists, in the same
                // logged-and-dropped failure domain as that write. A plain send has no pending
                // id and writes nothing at all.
                if (followUpFor != null) {
                    updateBatchStatus(followUpFor, BatchStatusModel.FollowUpSent).onFailure { error ->
                        log.w(error) { "Follow-up status not written for $followUpFor" }
                    }
                }
                postSideEffect(
                    MirrorSideEffect.SendComplete(
                        count = count,
                        summary = toastLabel(
                            count = count,
                            elapsedMs = elapsedMs,
                            // Off the published envelope, never re-derived (AF-4).
                            hierarchyAttached = envelope.hierarchyAvailable,
                        ),
                        sessionId = envelope.sessionId,
                    ),
                )
            }
    }

    /**
     * The batch thumbnail: the union box of every annotation, downscaled and encoded.
     *
     * Computed with the persist step rather than with the envelope, although both are pixel
     * work on the same dispatcher: the thumbnail is history's alone and is never published, so
     * running it before `publish` would delay the handoff the user is waiting on for the sake
     * of a row they have not looked at yet.
     *
     * Returns `null` and never throws, on purpose. `CropEncoder` already contracts to report
     * failure as `null`, but [cropAndScale] builds a real `ImageBitmap` and draws into it, and
     * that is backend work -- a frame the graphics backend will not accept must cost the row
     * its picture (the list renders a bare plate) and never the send.
     */
    private suspend fun thumbnailPng(frozen: FrozenFrame, frame: ImageBitmap): ByteArray? =
        runCatching {
            val union = unionRect(frozen.annotations) ?: return null
            val scaled = cropAndScale(frame, thumbnailRect(union, frozen.deviceSize)) ?: return null
            encodeCrop.encode(scaled, DeviceRect(0, 0, scaled.width, scaled.height))
        }.getOrNull()

    /**
     * Back to the live mirror (AC-51).
     *
     * Releases the pinned bitmap and the staged list and nothing else: the session was never
     * stopped, so the next frame from the still-running capture loop simply shows up.
     *
     * Retires a terminal [SendPhase] with them: this is the trigger the happy path itself goes
     * through (the send resumes live on success), which is what takes the status overlay off a
     * live stage after the toast has already carried the confirmation (FR-18).
     */
    private fun resumeLive() = intent {
        releaseFrozenFrame()
        releaseHierarchy()
        reduce { state.copy(frozen = null, send = SendPhase.Idle).withoutMeasurements() }
    }

    /**
     * Re-opens the capture session on the device that dropped (FR-35 / AC-54).
     *
     * The sibling of [resumeLive], not a variant of it: this one touches neither
     * [currentFrozenFrame] nor `state.frozen`, so the pinned pixels and every staged
     * annotation survive the reconnect. That is the whole of NFR-6 — the drop itself already
     * preserved them ([onSessionLost] only moves the session to [MirrorSession.Lost]), and
     * before this handler existed the only action on screen was the one that destroys them.
     *
     * Writing [sessionDeviceId] is what actually restarts the transport: `observeFrames`'
     * `flatMapLatest` gate re-subscribes `startMirroring`, and `observeDiscoveryWhileIdle`
     * stops polling again. The id comes from [MirrorSession.Lost] rather than from a new
     * state field, which is why nothing had to be remembered for this.
     */
    private fun reconnect() = intent {
        val lost = state.session as? MirrorSession.Lost ?: return@intent
        reduce { state.copy(session = MirrorSession.Streaming(lost.deviceId)) }
        sessionDeviceId.value = lost.deviceId
    }

    /**
     * Launches an AVD. The image takes tens of seconds to appear; the next discovery poll is
     * what puts it in the list, so there is nothing to wait for here.
     */
    private suspend fun Syntax<MirrorUiState, MirrorSideEffect>.boot(avdName: String) {
        bootEmulator(avdName).onFailure { postSideEffect(MirrorSideEffect.BootFailed(avdName)) }
    }
}
