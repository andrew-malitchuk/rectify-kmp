package presentation.feature.mirror.source.mirror

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.device.ToolModel
import domain.core.source.model.history.HistoryEntryModel
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
// Aliased: these properties carry the same names as the functions they delegate to, and an
// alias says which one is being called without the reader having to know Kotlin's overload
// resolution order.
import presentation.feature.mirror.core.history.historyBadgeCount as historyBadgeCountOf
import presentation.feature.mirror.core.send.canClear as canClearGate
import presentation.feature.mirror.core.send.canSend as canSendGate

/** Which heading a picker row sits under. */
public enum class MirrorRowGroup {
    /** A handset on a USB or TCP transport. */
    Physical,

    /** An AVD, running or cold. */
    Emulator,
}

/**
 * What a row's trailing edge means — the semantics only; the copy is resolved in the content
 * layer, which is the only place `stringResource` can be read.
 */
public enum class MirrorRowKind {
    /** Plain machine detail, e.g. `Android 15 · USB`. */
    Detail,

    /** A warm emulator. */
    Running,

    /** A cold emulator — committing the row boots it instead of starting a session. */
    Boot,
}

/**
 * One row of the device picker.
 *
 * [selectable] means "the user can put the cursor here and act on it", NOT "can be mirrored":
 * a cold emulator is selectable and its commit boots the image, while an unauthorised handset
 * is not selectable at all because there is nothing to do with it until the prompt is accepted.
 *
 * @property id Stable identifier — a serial for a device, the AVD name for a cold emulator.
 * @property name Display label.
 * @property serial Mono sub-label, or `null` when the row has no serial yet (a cold emulator).
 * @property group Heading this row belongs under.
 * @property kind What the trailing edge means.
 * @property detail Machine-produced trailing text for [MirrorRowKind.Detail]. Never localised.
 * @property selectable Whether arrow keys may land on the row and commit may act on it.
 * @property avdName AVD backing the row, when it is an emulator.
 */
public data class MirrorRow(
    val id: String,
    val name: String,
    val serial: String?,
    val group: MirrorRowGroup,
    val kind: MirrorRowKind,
    val detail: String = "",
    val selectable: Boolean = true,
    val avdName: String? = null,
)

/** Lifecycle of the mirroring session. */
public sealed interface MirrorSession {

    /** Nothing is streaming; discovery polls. */
    public data object Idle : MirrorSession

    /**
     * A capture loop is running.
     *
     * @property deviceId Device being mirrored.
     */
    public data class Streaming(val deviceId: String) : MirrorSession

    /**
     * The device went away mid-stream.
     *
     * @property deviceId Device that was lost.
     */
    public data class Lost(val deviceId: String) : MirrorSession
}

/** What fills the shell's stage slot right now. */
public enum class MirrorStage {
    /** The device picker (comps 01 / 03). */
    Picker,

    /** The live frame. */
    Mirror,

    /** The passive waiting stage (comp 02). */
    NoSignal,

    /**
     * A drag has pinned one frame; the capture loop keeps running behind it (FR-30).
     *
     * Frozen is a **render** decision, not a transport one: nothing is stopped, so Resume Live
     * costs no `adb` reconnection.
     */
    Frozen,

    /**
     * The BEFORE/AFTER verification of one sent batch (F-9).
     *
     * Derived **ahead of [Frozen]** — see [MirrorUiState.stage] for why, and for the one
     * consequence that ordering has on [MirrorIntent.SendFollowUp].
     */
    Verify,
}

/**
 * Which actionable error the card is reporting — the semantics only, like [MirrorRowKind]: the
 * headline, body and button labels are resolved in the content layer.
 *
 * All six of the design file's card states are modelled: the four shared ones below plus the
 * two iOS states ([SimulatorNotBooted] = `err-sim`, [DeviceNotTrusted] = `err-trust`) that were
 * drawn and deferred under NFR-10 and are wired by F-10. The first four are PLATFORM-SHARED,
 * not Android-only — a missing `xcrun` and a missing `adb` are the same card with different
 * copy, which is what [MirrorErrorCard.platform] resolves. [PollFailed] has no design state of
 * its own and is still a distinct case here: it is the only thing that separates "the tool
 * answered and saw nothing" from "the tool did not answer at all", and folding it into
 * [NoDevice] is exactly the misreport FR-32 forbids.
 */
public enum class MirrorErrorKind {
    /** The platform tool is not on the PATH at all (`err-adb`). */
    ToolMissing,

    /** The tool runs and sees no device (`err-nodevice`). */
    NoDevice,

    /** The discovery poll itself failed — no answer, as opposed to an empty answer. */
    PollFailed,

    /** More than one usable device is attached and the user has to pick (`err-multi`). */
    MultipleDevices,

    /** Simulators exist but none is running; the fix is to boot one (`err-sim`). */
    SimulatorNotBooted,

    /** A physical iOS device is attached but has not been trusted from it (`err-trust`). */
    DeviceNotTrusted,
}

/**
 * The actionable error card's spec — **not** its copy.
 *
 * Deliberately Compose-free and string-free so the projection that builds it can be asserted in
 * `commonTest`, which is the only test harness this module has. Same precedent as [MirrorRow]:
 * semantics here, `stringResource` in the content layer.
 *
 * @property kind Which state this is. The only field that tells [MirrorErrorKind.PollFailed]
 *   apart from [MirrorErrorKind.NoDevice].
 * @property command Exact shell line for the command block, or `null` when the state has none
 *   (as [MirrorErrorKind.MultipleDevices] does not). Machine text, never localised.
 * @property diagnostic Mono footer — probed paths, `adb` version, poll age. Never localised.
 * @property rows Picker rows for [MirrorErrorKind.MultipleDevices], carried verbatim from
 *   [MirrorUiState.rows] including a `selectable = false` unauthorised handset so the card can
 *   show it as disabled rather than hide it; empty for every other kind.
 * @property primaryDeviceId First usable row's id for [MirrorErrorKind.MultipleDevices] — the
 *   target of the "Mirror <top device>" shortcut; `null` otherwise.
 * @property canBootEmulator Whether any AVD is known, so [MirrorErrorKind.NoDevice] can hide the
 *   "Boot emulator instead" secondary instead of offering a dead button.
 * @property platform Which platform this card is reporting about.
 *
 *   The four shared kinds are reachable on BOTH platforms — `xcrun` can be missing exactly as
 *   `adb` can — so the stateless card renderer needs to know which one it is looking at to
 *   pick the copy and to suppress the adb-only actions. Defaulted to
 *   [PlatformModel.Android] so every existing construction site and test stays source
 *   compatible. Suppressing the shared kinds on iOS instead was the alternative and it is a
 *   defect: it hands the user a blank stage for four real conditions.
 */
public data class MirrorErrorCard(
    val kind: MirrorErrorKind,
    val command: String? = null,
    val diagnostic: String = "",
    val rows: List<MirrorRow> = emptyList(),
    val primaryDeviceId: String? = null,
    val canBootEmulator: Boolean = false,
    val platform: PlatformModel = PlatformModel.Android,
)

/**
 * The "stream dropped" banner's spec, shown over a frozen frame whose device went away (FR-24).
 *
 * @property deviceId Device from [MirrorSession.Lost] — the one [MirrorIntent.Reconnect]
 *   re-attempts, so no new state field is needed to remember it.
 * @property annotationCount `frozen.annotations.size`, so the banner can say how much work the
 *   drop did **not** destroy.
 */
public data class MirrorDropBanner(
    val deviceId: String,
    val annotationCount: Int,
)

/**
 * Lifecycle of a Resolve & Send attempt (AC-17, AC-19, AC-20, AC-21, FR-16, FR-18, FR-58).
 *
 * A value-equal enum and nothing more: every byte the send pipeline touches (crops, the
 * assembled envelope) stays off [MirrorUiState] for the same reason frame bytes do — see that
 * class's own KDoc. [isInFlight] is the one piece of behaviour riding on this type, since three
 * independent call sites (the composable's button, the reducer's own guard, and the hoisted
 * `⌘↩` shortcut holder) all need the exact same "is a send already running" predicate.
 */
public enum class SendPhase {
    /** Nothing in flight — the status overlay is hidden. */
    Idle,

    /** Encoding crops, assembling the envelope. */
    Preparing,

    /** Writing the handoff slot. */
    Publishing,

    /** Terminal success. */
    Sent,

    /** Terminal failure; frame and batch preserved (AS-31). */
    Failed,
    ;

    /** Whether a send is actively running — [Preparing] or [Publishing] only. */
    public val isInFlight: Boolean
        get() = this == Preparing || this == Publishing
}

/**
 * The mirror surface's reduced state.
 *
 * Carries **no frame bytes**: pixels arrive up to ten times a second and a `ByteArray`-bearing
 * type compares by identity, so the decoded frame lives in [MirrorViewModel.frame], a
 * `StateFlow` outside the reducer. Only semantics go through Orbit.
 *
 * @property platform Target platform selected in the toolbar.
 * @property tools One probe result per platform, from the launch-time detection.
 * @property discovery Latest discovery outcome — every value here is a success value.
 * @property rows Picker rows derived from [discovery], in display order.
 * @property isPickerOpen Whether the picker owns the stage.
 * @property highlightedIndex Cursor into [rows].
 * @property selectedDeviceId Device the current (or last) session was started on.
 * @property selectedDeviceName Display name of [selectedDeviceId], for the toolbar chip.
 * @property session Session lifecycle.
 * @property stats Latest transport statistics, reduced at most once a second.
 * @property avds Known AVD names, retained across an escalated poll.
 * @property selectedAvd AVD chosen in the waiting stage's inline picker.
 * @property frozen The pinned frame's semantics, or `null` while the stage is live. Carries no
 *   pixels either, for the same reason this class carries no frame: the bitmap itself lives on
 *   [MirrorViewModel.frozenFrame]. Annotations, unlike pixels, DO belong here — they are small,
 *   value-equal and change only when the user acts.
 * @property measure The measuring readout in flight, or `null` while nothing is being measured.
 *   Fully computed and value-equal (see [MeasureReadout]), so it costs a state copy no more than
 *   an annotation does — the same reason the staged list lives here and the pixels do not.
 * @property hierarchyStatus Whether a view hierarchy is available for [frozen]. The snapshot
 *   itself is **not** here: it is a few hundred nodes per dump, read only by the measuring
 *   arithmetic and never rendered directly, so it lives on [MirrorViewModel.frozenHierarchy]
 *   beside the bitmaps. Only this four-state verdict is reduced.
 * @property send Lifecycle of the current (or last) Resolve & Send attempt. A value-equal enum,
 *   exactly like [hierarchyStatus] — no byte-bearing value ever rides on this property.
 * @property history Every recorded batch, newest first — the single source for both the
 *   History list and its badge count. The one byte-bearing value that IS reduced, and the
 *   exception is measured, not casual: a row's [domain.core.source.model.history.HistoryEntryModel.thumbPng]
 *   is a few KB and arrives once per send, not ten times a second like a frame, so the identity
 *   comparison a `ByteArray` forces costs one extra state emission per database change rather
 *   than per rendered frame. The list is rendered with `key = { it.id }`, so an unchanged row is
 *   not recomposed by that emission.
 * @property verify The verification run in flight or on screen, or `null` when there is none.
 *   Value-equal and pixel-free by construction — the two bitmaps it is rendered with ride
 *   a plain `StateFlow<VerifyFrames?>` instead (deviation 2). Non-`null` is what puts the stage in
 *   [MirrorStage.Verify], ahead of [frozen].
 */
public data class MirrorUiState(
    val platform: PlatformModel = PlatformModel.Android,
    val tools: List<ToolModel> = emptyList(),
    val discovery: DiscoveryModel = DiscoveryModel.Detecting,
    val rows: List<MirrorRow> = emptyList(),
    val isPickerOpen: Boolean = true,
    val highlightedIndex: Int = 0,
    val selectedDeviceId: String? = null,
    val selectedDeviceName: String? = null,
    val session: MirrorSession = MirrorSession.Idle,
    val stats: MirrorStatsModel? = null,
    val avds: List<String> = emptyList(),
    val selectedAvd: String? = null,
    val frozen: FrozenFrame? = null,
    val measure: MeasureReadout? = null,
    val hierarchyStatus: HierarchyStatus = HierarchyStatus.Absent,
    val send: SendPhase = SendPhase.Idle,
    val history: List<HistoryEntryModel> = emptyList(),
    val verify: VerifyUiState? = null,
) {

    /** How many annotations are staged — the toolbar's counter badge (AC-48). */
    public val stagedCount: Int get() = frozen?.annotations?.size ?: 0

    /**
     * The History tab's badge — in-progress batches, or `null` when there are none (AC-34).
     *
     * Same delegate shape, and the same reason, as [canSend]: the decision lives in
     * `core/history` (internal by `package-layout.md`) and its reader is the shell in
     * `:compose-application`. `null` rather than `0` because `SidebarTabs` paints any non-null
     * count, and the omit-at-zero rule has to be testable where `:compose-application` is not.
     */
    public val historyBadgeCount: Int? get() = historyBadgeCountOf(history)

    /** Row under the cursor, or `null` when the list is empty. */
    public val highlightedRow: MirrorRow? get() = rows.getOrNull(highlightedIndex)

    /**
     * Whether the picker may be dismissed (A11Y-4).
     *
     * `false` until a device has been chosen once: before that the picker IS the stage, so
     * `esc` and the close button would both strand the user on an empty surface. The same
     * rule guards [MirrorIntent.ClosePicker] in the reducer — this property is what lets the
     * UI stop OFFERING the affordance instead of offering a no-op.
     */
    public val isPickerDismissable: Boolean get() = selectedDeviceId != null

    /**
     * Whether Resume Live may be **offered** (NFR-6).
     *
     * [MirrorIntent.ResumeLive] drops the staged annotations, which is the right trade while the
     * stream is still running behind the pinned frame. Once the device is gone it is not: there
     * is no live frame to go back to, so the only affordance on screen would be one that
     * destroys the user's work. Withdrawing the button — the [isPickerDismissable] precedent —
     * is what fixes that, not changing what the intent does: `ResumeLive`'s own behaviour stays
     * exactly as AC-51 confirmed it. [MirrorIntent.Reconnect] is what the drop banner offers
     * instead.
     */
    public val canResumeLive: Boolean get() = frozen != null && session !is MirrorSession.Lost

    /**
     * Whether Resolve & Send may be offered (AC-17/AC-20, TAC-1).
     *
     * A one-line delegate to `core/send`'s [presentation.feature.mirror.core.send.canSend],
     * which stays the single definition of the predicate — the reducer's own guard and these
     * two properties therefore cannot drift. The delegate exists because the predicate lives in
     * `core/` (internal by `package-layout.md`) and its loudest reader, the desktop toolbar in
     * `:compose-application`, is in another module.
     */
    public val canSend: Boolean get() = canSendGate(state = this)

    /**
     * Whether Clear may be offered (TAC-2) — same batch, same in-flight window as [canSend].
     */
    public val canClear: Boolean get() = canClearGate(state = this)

    /**
     * What the stage shows.
     *
     * The picker wins while it is open — it is a decision the user has to make. Escalated
     * discovery closes it (there is nothing left to pick), which is what lets the passive
     * NO SIGNAL stage of AC-4 appear without a second flag.
     *
     * **[verify] outranks [frozen]**, and that ordering has one consequence worth stating
     * where it is created rather than where it bites: staging a follow-up sets [frozen], so a
     * [MirrorIntent.SendFollowUp] that did not also clear [verify] would leave the user pinned
     * on a stale Verify view with F-7's send and send-failure UI unreachable behind it.
     * `SendFollowUp` therefore tears the verify state down in the SAME reduction that sets the
     * frozen frame. Verify ranks above Frozen because the run holds its own BEFORE/AFTER
     * pixels: showing the pinned live frame underneath it would be showing a third frame
     * nobody asked for.
     */
    public val stage: MirrorStage
        get() = when {
            isPickerOpen -> MirrorStage.Picker
            // Ahead of Frozen - see the KDoc, and SendFollowUp's teardown obligation.
            verify != null -> MirrorStage.Verify
            // Ahead of Streaming: the session is still running under a frozen frame, and the
            // stage must show the pinned pixels rather than the ones still arriving.
            frozen != null -> MirrorStage.Frozen
            session is MirrorSession.Streaming -> MirrorStage.Mirror
            else -> MirrorStage.NoSignal
        }
}

/** How far a verification run has got — the progress strip's only data source (NFR-15). */
public enum class VerifyPhase {
    /** Reading the sent batch back off disk. */
    Loading,

    /** Re-capturing the hierarchy from the device — the ~2.7 s step. */
    Recapturing,

    /** Measuring and diffing each annotation. */
    Diffing,

    /** Every row has its verdict. */
    Ready,
}

/**
 * Why a verification run produced no verdicts at all.
 *
 * Every case is a distinct, user-legible reason rather than one generic failure, because the
 * alternative to naming the reason is implying a verdict — and "Still off" on a run that never
 * ran is the single most expensive lie this tool can tell. A [VerifyUiState] carrying any of
 * these always carries an EMPTY [VerifyUiState.rows] list, and no batch status is written.
 */
public enum class VerifyError {
    /**
     * Nothing is streaming from the device, so there is no AFTER frame to diff against.
     *
     * Verify is not a one-shot capture: `StartMirroringUseCase` is stream-only and no on-demand
     * capture pipeline exists, so a verification genuinely requires a live session. The copy
     * names the device and says to start mirroring (SC-23).
     */
    NoLiveSession,

    /**
     * The sent batch is no longer readable — either no such batch, or its `frame.png` is gone
     * because the user deleted `~/.rectify/history/`. A stored path is a hint, never a
     * guarantee.
     */
    NotOnDisk,

    /**
     * The stored frame and the live frame are different sizes (a rotation, a resolution change
     * or a different device), so every rect would land somewhere else. Caught once, before any
     * per-annotation work (SC-22).
     */
    SizeMismatch,

    /**
     * A non-empty hierarchy projects to ZERO candidates against the shared frame size — the
     * display-override case (a Pixel 8 reporting 984×2132 nodes on a 1080×2400 frame).
     *
     * Distinct from an empty hierarchy, which still degrades per annotation to "no match"
     * (AC-41): here the nodes exist but are in a coordinate space the frame cannot be matched
     * against, so every verdict would be confident and wrong.
     */
    HierarchyUnusable,

    /** The device went away mid-run. The batch's status is left exactly as it was (SC-21). */
    Disconnected,
}

/**
 * One annotation's verification result — semantics only, no copy and no pixels.
 *
 * [outcome] deliberately has **no default**: a caller that forgets it must be a compile error,
 * never a silently optimistic `Fixed`.
 *
 * @property index 0-based position in the sent batch, i.e. history's own order. (The envelope's
 *   1-based numbering belongs to the send path and is not reused here.)
 * @property comment What the tester wrote when the annotation was filed.
 * @property beforeRect Where the matched node sat on the frame the annotation was drawn on, in
 *   device pixels — the annotation's own rect when nothing matched, which is also every batch
 *   whose hierarchy was never stored or can no longer be read.
 * @property afterRect Where the matched node sits now; equal to [beforeRect] when nothing moved.
 * @property resultLine The machine-produced BEFORE → AFTER measurement line, already formatted
 *   by `core/verify` — the BEFORE finding's own measurement on both frames where there is one,
 *   the node's size on each frame otherwise. Never localised — it is numbers, like every other
 *   mono readout here.
 * @property outcome The verdict, from `verdictOf`.
 */
public data class VerifyRow(
    val index: Int,
    val comment: String,
    val beforeRect: DeviceRect,
    val afterRect: DeviceRect,
    val resultLine: String,
    val outcome: VerificationOutcome,
)

/**
 * The verification panel's whole reduced state.
 *
 * **Invariant: an [error] always comes with an empty [rows] list** — [fixedCount] and
 * [rowCount] are therefore `0 of 0` on any abort, and an abort can never be read as a set of
 * verdicts. Enforced at construction, not merely by convention, so no reducer can produce the
 * mixed state in the first place.
 *
 * Pixel-free by construction (deviation 2): the BEFORE/AFTER bitmaps ride a plain
 * `StateFlow<VerifyFrames?>` beside the live frame, never this.
 *
 * @property batchId The sent batch under verification.
 * @property deviceLabel Device the batch was captured on, for the header and for
 *   [VerifyError.NoLiveSession]'s copy.
 * @property sentAtMs When the batch was sent, epoch milliseconds.
 * @property recapturedAtMs When the AFTER capture was taken, epoch milliseconds; `0` until
 *   [VerifyPhase.Recapturing] has completed.
 * @property phase How far the run has got.
 * @property rows One row per annotation, in batch order. Empty while loading and empty on any
 *   [error].
 * @property error Why the run produced nothing, or `null` while it is fine.
 */
public data class VerifyUiState(
    val batchId: String,
    val deviceLabel: String,
    val sentAtMs: Long = 0,
    val recapturedAtMs: Long = 0,
    val phase: VerifyPhase = VerifyPhase.Loading,
    val rows: List<VerifyRow> = emptyList(),
    val error: VerifyError? = null,
) {
    init {
        require(error == null || rows.isEmpty()) {
            "an aborted verify run must carry no verdicts - $error came with ${rows.size} rows"
        }
    }

    /** How many annotations came back [VerificationOutcome.Fixed] — the `n` of `n of m`. */
    public val fixedCount: Int get() = rows.count { it.outcome == VerificationOutcome.Fixed }

    /** How many annotations were verified at all — the `m` of `n of m`. */
    public val rowCount: Int get() = rows.size

    /** The rows a follow-up would re-send — "Send follow-up · N" is hard-disabled at `0`. */
    public val stillOffRows: List<VerifyRow>
        get() = rows.filter { it.outcome == VerificationOutcome.StillOff }
}

/**
 * The two bitmaps a verification run is rendered with.
 *
 * `public`, unlike the internal `HeldFrame`, because `:compose-application`'s window hands this
 * flow into `MirrorHost`'s content lambda. It is the ONE place pixels are allowed near this
 * contract — on a plain `StateFlow`, never inside [MirrorUiState] (deviation 2).
 *
 * @property before The frame as it was sent, decoded from the stored `frame.png`.
 * @property after The frame as re-captured for this run.
 */
public data class VerifyFrames(
    val before: ImageBitmap,
    val after: ImageBitmap,
)

/**
 * Every user action on the mirror surface.
 *
 * None of these carries a modifier-key flag: `Cmd+1/2/3` belongs to the window's own shortcut
 * handler, and arrow / `↵` belong here. Splitting the ownership is what stops the two from
 * racing for the same key event.
 */
public sealed interface MirrorIntent {

    /**
     * Toolbar platform switch.
     *
     * @property platform Platform to target.
     */
    public data class SelectPlatform(val platform: PlatformModel) : MirrorIntent

    /** Reopen the picker — the toolbar chip (AC-43). */
    public data object OpenPicker : MirrorIntent

    /** Dismiss the picker. A no-op until a device has been selected at least once. */
    public data object ClosePicker : MirrorIntent

    /**
     * Point the cursor at a row.
     *
     * @property id Row identifier.
     */
    public data class HighlightRow(val id: String) : MirrorIntent

    /**
     * Move the cursor.
     *
     * @property delta `-1` or `+1`.
     */
    public data class MoveHighlight(val delta: Int) : MirrorIntent

    /** Act on the highlighted row — start mirroring, or boot a cold emulator. */
    public data object Commit : MirrorIntent

    /**
     * Boot the emulator behind a row, from its trailing action.
     *
     * @property id Row identifier.
     */
    public data class BootRow(val id: String) : MirrorIntent

    /**
     * Choose an AVD in the waiting stage.
     *
     * @property avdName AVD to select.
     */
    public data class SelectAvd(val avdName: String) : MirrorIntent

    /** Boot the AVD chosen in the waiting stage. */
    public data object BootSelectedAvd : MirrorIntent

    /** End the session and resume discovery. */
    public data object StopMirroring : MirrorIntent

    /**
     * Commit a finished drag as a new annotation, freezing the frame on the first one.
     *
     * Carries the finished rectangle only — the drag in progress never reaches the reducer, or
     * a 60 Hz pointer stream would become 60 state copies a second.
     *
     * @property rect Drawn bounds, in device pixels.
     */
    public data class StageAnnotation(val rect: DeviceRect) : MirrorIntent

    /**
     * Put the comment popover on one annotation, or dismiss it.
     *
     * @property index 1-based annotation index, or `null` to deselect. Deselecting keeps the
     *   annotation and the staged count (AC-50).
     */
    public data class SelectAnnotation(val index: Int?) : MirrorIntent

    /**
     * Store the note typed against one annotation.
     *
     * @property index 1-based annotation index.
     * @property comment Text as typed.
     */
    public data class CommentAnnotation(val index: Int, val comment: String) : MirrorIntent

    /**
     * Remove one annotation; the survivors renumber and the staged count drops (AC-8).
     *
     * @property index 1-based annotation index.
     */
    public data class DeleteAnnotation(val index: Int) : MirrorIntent

    /** Empty the staged list. The frame stays frozen (AC-48). */
    public data object ClearStaged : MirrorIntent

    /**
     * Replace the payload's default prompt for the pinned frame (AC-79).
     *
     * A no-op while the stage is live: the override belongs to [FrozenFrame], so there is
     * nothing to attach it to until a frame is pinned.
     *
     * @property text Prompt as typed, or `null` to drop the override and fall back to the
     *   generated prompt. `null` is sent by one caller only — the Payload tab discarding an
     *   editing session that was seeded against annotations which no longer exist, where the
     *   text on screen is the machine's own previous output and keeping it would export a prompt
     *   describing a payload that is gone.
     */
    public data class EditPrompt(val text: String?) : MirrorIntent

    /**
     * Resolve the staged annotations against the pinned frame and publish the handoff payload
     * (AC-17, AC-19, FR-16, FR-18). Guarded by `canSend` (`core/send/SendGate.kt`) at every
     * dispatch site — the composable's button, the reducer, and the hoisted `⌘↩` shortcut holder
     * — so a second concurrent send while [SendPhase.isInFlight] holds is not reachable.
     */
    public data object ResolveAndSend : MirrorIntent

    /** Unpin the frame and drop the staged list — back to the live mirror (AC-51). */
    public data object ResumeLive : MirrorIntent

    /**
     * Re-attempt the capture session after a drop (FR-33), from the banner's only action.
     *
     * Carries no device id: [MirrorSession.Lost] already holds the one to retry, so remembering
     * it costs no new state field. Distinct from [ResumeLive] in the one way that matters — it
     * keeps the frozen frame and its annotations.
     */
    public data object Reconnect : MirrorIntent

    /**
     * Measure one annotation against the captured hierarchy — Option held down (FR-36).
     *
     * @property index 1-based annotation index. An index that is no longer staged measures
     *   nothing rather than failing: the annotation can be deleted between the two intents.
     */
    public data class StartMeasure(val index: Int) : MirrorIntent

    /**
     * Option released — write the readout's `⌥` line into the annotation's note and drop it.
     *
     * Carries no index: [MirrorUiState.measure] already names the annotation it was computed
     * for, and taking the index from the readout rather than from the key event is what stops
     * the note landing on a different annotation after a delete renumbered the list.
     */
    public data object EndMeasure : MirrorIntent

    /**
     * Re-capture the device and diff it against a sent batch (FR-21 / AC-23 / SC-8).
     *
     * Fired from **any** sent batch, never only a `Fixed` one: nothing in the app writes
     * [domain.core.source.model.history.BatchStatusModel.Fixed], so a status gate would make
     * this intent unreachable. The row's button is instead disabled when the batch has no
     * frame on disk to diff against.
     *
     * Requires a live mirror session: there is no one-shot capture pipeline, so a run with
     * nothing streaming fails with [VerifyError.NoLiveSession] rather than guessing. A frame
     * whose stored path has gone stale since the row was drawn fails with
     * [VerifyError.NotOnDisk].
     *
     * @property batchId [domain.core.source.model.history.HistoryEntryModel.id] of the row the
     *   button sits on.
     */
    public data class VerifyBatch(val batchId: String) : MirrorIntent

    /**
     * Re-send the annotations a run reported as still off, as a new batch.
     *
     * Staging sets [MirrorUiState.frozen], and [MirrorStage.Verify] outranks
     * [MirrorStage.Frozen], so the handler MUST clear the verify state in the same reduction
     * or the user stays pinned on the stale Verify view with the send UI hidden behind it.
     */
    public data object SendFollowUp : MirrorIntent

    /**
     * Override the verdicts and record the batch as verified — the user's judgement wins.
     */
    public data object MarkAllVerified : MirrorIntent

    /**
     * Leave the verify stage — the "← Back to live" chip, and the only exit.
     *
     * Its handler cancels the run and clears ALL FOUR verify-scoped fields, or the next run
     * inherits the last one's leftovers: [MirrorUiState.verify], the `verifyFrames` flow, the
     * retained `verifyAfterHeld` frame and `pendingFollowUpBatchId`.
     */
    public data object ExitVerify : MirrorIntent

    /** Restart the discovery collection — the error card's "Retry" (AC-26, AC-53). */
    public data object RetryDiscovery : MirrorIntent

    /**
     * Restart the `adb` daemon — `kill-server` then `start-server` (AC-27's primary).
     *
     * Deliberately not an alias of [RetryDiscovery]: a button that says "Restart adb" and only
     * re-polls is a lying string, and re-polling cannot clear a wedged daemon anyway.
     */
    public data object RestartAdb : MirrorIntent

    /** Ask the host for an `adb` to use — the error card's "Locate adb…" (AC-26's primary). */
    public data object LocateAdb : MirrorIntent

    /**
     * An `adb` the user picked in the host's file dialog.
     *
     * Still probed by `isExecutable` before it is used: coming from a dialog makes a path
     * user-chosen, not valid.
     *
     * @property path Absolute path to the chosen binary.
     */
    public data class AdbPathChosen(val path: String) : MirrorIntent
}

/** One-shot events of the mirror surface. */
/**
 * The recovery actions that can fail visibly (see [MirrorSideEffect.RecoveryFailed]).
 *
 * `RetryDiscovery` is deliberately absent: it bottoms out in `Channel.trySend`, which cannot
 * throw, so a case for it would be unreachable in the running app. An enum case nothing can
 * produce is worse than no case — it invites a `when` branch and a test that both look like
 * coverage.
 */
public enum class RecoveryAction {
    RestartAdb,
    SaveAdbPath,
}

public sealed interface MirrorSideEffect {

    /**
     * An emulator failed to launch.
     *
     * F-2 does not render error UI — the actionable card is F-3's. This exists so the host can
     * forward the failure instead of it vanishing.
     *
     * @property avdName AVD that would not boot.
     */
    public data class BootFailed(val avdName: String) : MirrorSideEffect

    /**
     * Open the host's file picker so the user can point Rectify at an `adb` (AC-26).
     *
     * A side effect and not a reduced flag because only the host can show a native dialog; its
     * result comes back as [MirrorIntent.AdbPathChosen].
     */
    public data object LocateAdb : MirrorSideEffect

    /**
     * A recovery action failed, so the user is told instead of watching nothing happen.
     *
     * Added for COR-7. The three recovery handlers discarded the `Optional` their use cases
     * returned, which made a real failure — an unmounted SDK after the probe cached a success,
     * say — indistinguishable from the dead button FR-37 exists to forbid: the card simply
     * re-rendered as if the user had cancelled. `DeviceRepositoryImpl.restartAdbServer`'s own
     * KDoc justifies throwing precisely so "the use case reports a Failure rather than a button
     * that silently did nothing", and the only reader was throwing it away.
     *
     * @property action Which action failed, for the message.
     */
    public data class RecoveryFailed(val action: RecoveryAction) : MirrorSideEffect

    /**
     * A Resolve & Send attempt finished publishing (AC-17, AC-19, AC-20, AC-21).
     *
     * A side effect and not a reduced flag for the same reason [LocateAdb] is one: only the host
     * can show a toast and touch the clipboard. Carries everything the host needs so it never has
     * to query the ViewModel back for it.
     *
     * @property count How many annotations were resolved into the payload.
     * @property summary Short human-readable summary line for the toast.
     * @property sessionId Identifier of the published handoff session, for the clipboard copy.
     */
    public data class SendComplete(
        val count: Int,
        val summary: String,
        val sessionId: String,
    ) : MirrorSideEffect
}

/**
 * An annotation rectangle in **device screenshot pixels** — never host pixels, never dp.
 *
 * Device pixels are the only coordinate space that survives the trip to a bug report: the host
 * window can be resized and the display can be Retina or not, but `1035,862` on a 1080×2400
 * screenshot means the same place on every machine that opens it (FR-6). Converting in and out
 * of this space is `FrameGeometry`'s job and nothing else's.
 *
 * Always normalised — [right] >= [left] and [bottom] >= [top] — because `FrameGeometry.toDevice`
 * normalises a reversed drag before it ever gets here.
 *
 * @property left Left edge, in device pixels.
 * @property top Top edge, in device pixels.
 * @property right Right edge, in device pixels, exclusive.
 * @property bottom Bottom edge, in device pixels, exclusive.
 */
public data class DeviceRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    /** Width in device pixels. */
    public val width: Int get() = right - left

    /** Height in device pixels. */
    public val height: Int get() = bottom - top
}

/**
 * One annotation drawn on the frozen frame and waiting to be filed.
 *
 * Lives inside [MirrorUiState] rather than beside it: a handful of value-equal records that
 * change only on a user action is exactly what a reducer is for. Frames are the opposite case
 * and stay out — see [MirrorUiState]'s own note.
 *
 * @property index 1-based badge number, contiguous across the list. Renumbered on delete so the
 *   badge on the frame and the row in the sidebar can never disagree.
 * @property rect Bounds in device pixels.
 * @property comment Note typed by the user; empty until they type one.
 */
public data class StagedAnnotation(
    val index: Int,
    val rect: DeviceRect,
    val comment: String = "",
)

/**
 * Everything about the pinned frame **except its pixels**.
 *
 * [deviceSize] and not the bitmap: it is all the reducer needs to place a mark, it is value-equal,
 * and keeping the `ImageBitmap` out means a state copy stays cheap. The bitmap sits on
 * [MirrorViewModel.frozenFrame], the second `StateFlow` beside `frame`.
 *
 * [frameId], [deviceLabel] and [promptOverride] are **captured at freeze, dies with the frame**:
 * each is stamped or typed against this one pinned frame and goes with it when the frame is
 * released, which is why none of them is a field of [MirrorUiState]. Re-deriving them later
 * would read a device chip or a prompt the user has since changed, and the exported payload
 * would describe a frame that never existed.
 *
 * @property deviceSize Size of the pinned screenshot, in device pixels.
 * @property annotations Staged annotations in badge order.
 * @property selectedIndex [StagedAnnotation.index] of the annotation whose popover is open, or
 *   `null` when none is.
 * @property frameId 1-based freeze number within the session, stamped once at freeze and shared
 *   by every annotation on this frame — `0` only for a frame built without a stamp.
 * @property deviceLabel The selected row's [MirrorRow.detail] with the transport segment
 *   stripped (`"Android 15 · USB"` → `"Android 15"`), stamped at freeze. Empty when there was
 *   no selected row to read.
 * @property promptOverride Prompt text the user typed for this frame, or `null` while the
 *   default prompt applies. Survives every annotation edit and a session loss. Two reducers drop
 *   it: [MirrorIntent.ResumeLive], which drops the whole frame, and [MirrorIntent.ClearStaged],
 *   which keeps the frame but clears the override along with the annotations it described
 *   (bugs.json#PP-TD-2 — an override that outlived its annotations would otherwise be sent to
 *   the agent as the payload's prompt). The Payload tab clears it
 *   with `EditPrompt(null)` in one case — an open editor whose seed was invalidated by an
 *   annotation edit, where the text on screen is stale machine output rather than anything the
 *   user wrote.
 */
public data class FrozenFrame(
    val deviceSize: IntSize,
    val annotations: List<StagedAnnotation> = emptyList(),
    val selectedIndex: Int? = null,
    val frameId: Int = 0,
    val deviceLabel: String = "",
    val promptOverride: String? = null,
) {
    /** The annotation the popover belongs to, or `null`. */
    public val selected: StagedAnnotation?
        get() = annotations.firstOrNull { it.index == selectedIndex }
}

/**
 * Unit every measurement in the panel is labelled in.
 *
 * [Px] is not a preference — it is what an **unknown device density** degrades to. A wrong
 * density scales every figure it touches, so raw px with an honest label beats a confidently
 * wrong `dp` (see `core/measure`'s `toDisplayUnit`).
 *
 * @property suffix Machine text appended to a number, e.g. the `dp` of `48×17dp`. Never
 *   localised — these strings go into bug reports and must read the same on every machine.
 */
public enum class MeasureUnit(public val suffix: String) {
    /** Density-independent pixels — the normal case. */
    Dp("dp"),

    /** Raw device pixels, used when density is unknown. */
    Px("px"),
}

/**
 * Which row of the measuring panel a value or guide belongs to.
 *
 * @property label Lowercase machine word used in the `⌥` note line (`top 15dp`). [Size] carries
 *   one for completeness; the note line prints its value unlabelled because `48×17dp` already
 *   says what it is.
 */
public enum class MeasureSide(public val label: String) {
    /** The node's own width × height. */
    Size("size"),

    /** Gap above the node. */
    Top("top"),

    /** Gap to the right of the node. */
    Right("right"),

    /** Gap below the node. */
    Bottom("bottom"),

    /** Gap to the left of the node. */
    Left("left"),
}

/**
 * Whether a view hierarchy is available to measure against.
 *
 * [Unavailable] and [Absent] are deliberately distinct, for the same reason
 * [MirrorErrorKind.PollFailed] is distinct from [MirrorErrorKind.NoDevice]: "nobody has asked
 * for a dump yet" and "the dump was attempted and the device would not give one" need different
 * UI, and folding them together misreports one as the other.
 */
public enum class HierarchyStatus {
    /** No dump has been attempted for the current frozen frame. */
    Absent,

    /** A dump is in flight — it costs ~2.7 s, so this state is visible to the user. */
    Dumping,

    /** A usable snapshot is held. */
    Ready,

    /** The dump was attempted and produced nothing usable. */
    Unavailable,
}

/**
 * Which rule produced a measurement's single finding.
 *
 * Public, and on [MeasureReadout], because two consumers need the RULE rather than its message:
 * the overlay draws an alignment rail for [TopMisalignment] and nothing else, and the verify
 * row picks which BEFORE → AFTER line to format. Re-deriving either by matching on the message
 * string would make a copy tweak a behaviour change.
 */
public enum class FindingKind {
    /** The target's top edge is just off its left neighbour's. */
    TopMisalignment,

    /** The target's left edge is just off its top neighbour's. */
    LeftMisalignment,

    /** A tappable node is shorter than the touch-target floor. */
    TouchTarget,
}

/**
 * One already-formatted row of the measuring panel.
 *
 * Formatted and not numeric: `×`, `Δ`, the unit suffix and the suppression rule are all decided
 * in `core/measure` where they are unit-testable, so the UI only ever places strings. The same
 * precedent as [MirrorErrorCard] carrying no copy — except here the strings are machine text,
 * never localised.
 *
 * @property side Which row this is.
 * @property value Formatted figure, e.g. `48×17dp` or `15dp`.
 */
public data class MeasureValue(
    val side: MeasureSide,
    val value: String,
)

/**
 * One dashed guide drawn over the frozen frame, in **device pixels**.
 *
 * Used for both the four gap guides and the single alignment rail — same geometry, same label
 * shape, so one type carries both rather than two near-identical ones.
 *
 * A guide whose gap falls under one display unit is **absent from the list entirely** (FR-39),
 * never present with a rounded-up `1` label.
 *
 * @property side Which gap this guide measures, or [MeasureSide.Top] for the alignment rail.
 * @property label Formatted numeral, e.g. `15dp`, or `Δ6dp` on the rail.
 * @property from Start point in device pixels.
 * @property to End point in device pixels.
 */
public data class MeasureGuide(
    val side: MeasureSide,
    val label: String,
    val from: IntOffset,
    val to: IntOffset,
)

/**
 * Everything the measuring overlay and panel draw for one annotation.
 *
 * Fully computed and fully formatted: it is the output of one pure `core/measure.measure(...)`
 * call over a hierarchy snapshot, so the reducer stores it and the UI places it, and neither
 * does arithmetic. Value-equal, so it can live in [MirrorUiState] without making a state copy
 * expensive.
 *
 * @property annotationIndex 1-based badge number of the annotation being measured.
 * @property nodeId Snapped node's label, or `View (no match)` when nothing snapped — a
 *   first-class outcome (FR-41) in which every value below is still measured, against the
 *   annotation's own rect and the frame edges.
 * @property targetRect The snapped node's bounds, or the annotation's own when nothing snapped.
 * @property neighbourRects Bounds of the nearest neighbour on each side that exists, in
 *   `[left, right, top, bottom]` order, each with a 1px height floor so a hairline divider stays
 *   visible.
 * @property values The five panel rows, in display order, already formatted.
 * @property guides Gap guides to draw; a side whose gap is under one display unit is absent.
 * @property rail The alignment rail, populated **only** for the top-misalignment finding — it
 *   means "these two edges should have lined up", which neither of the other findings says.
 * @property finding The single finding message, or `null` when there is nothing to report.
 *   Never more than one, by construction.
 * @property findingKind Which rule produced [finding], `null` alongside it.
 * @property leftNeighbourTopPx Top edge of the nearest LEFT neighbour, `null` when there is
 *   none — the edge [FindingKind.TopMisalignment] is measured against. Carried separately from
 *   [neighbourRects], which drops absent sides and is therefore positionally ambiguous, and
 *   carried whether or not the finding fired: the AFTER side of a before/after line needs the
 *   same edge precisely in the good case, where the finding is gone.
 * @property topNeighbourLeftPx Left edge of the nearest TOP neighbour, the
 *   [FindingKind.LeftMisalignment] counterpart of [leftNeighbourTopPx].
 * @property unit Unit every figure here is labelled in.
 * @property noteLine The `⌥` one-liner pasted into the annotation's note.
 */
public data class MeasureReadout(
    val annotationIndex: Int,
    val nodeId: String,
    val targetRect: DeviceRect,
    val neighbourRects: List<DeviceRect>,
    val values: List<MeasureValue>,
    val guides: List<MeasureGuide>,
    val rail: MeasureGuide?,
    val finding: String?,
    val unit: MeasureUnit,
    val noteLine: String,
    val findingKind: FindingKind? = null,
    val leftNeighbourTopPx: Int? = null,
    val topNeighbourLeftPx: Int? = null,
)
