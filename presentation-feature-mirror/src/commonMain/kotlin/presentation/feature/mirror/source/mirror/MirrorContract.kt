package presentation.feature.mirror.source.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.MirrorStatsModel
import domain.core.source.model.device.PlatformModel
import domain.core.source.model.device.ToolModel

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

    /** iOS is detected but not mirrorable this release. */
    IosDeferred,

    /**
     * A drag has pinned one frame; the capture loop keeps running behind it (FR-30).
     *
     * Frozen is a **render** decision, not a transport one: nothing is stopped, so Resume Live
     * costs no `adb` reconnection.
     */
    Frozen,
}

/**
 * Which actionable error the card is reporting — the semantics only, like [MirrorRowKind]: the
 * headline, body and button labels are resolved in the content layer.
 *
 * The design file draws five card states, two of which ([`err-sim`, `err-trust`]) are iOS and
 * deferred (NFR-10), so only the Android four are modelled. [PollFailed] has no design state of
 * its own and is still a distinct case here: it is the only thing that separates "`adb` answered
 * and saw nothing" from "`adb` did not answer at all", and folding it into [NoDevice] is exactly
 * the misreport FR-32 forbids.
 */
public enum class MirrorErrorKind {
    /** `adb` is not on the PATH at all (`err-adb`). */
    ToolMissing,

    /** `adb` runs and sees no device (`err-nodevice`). */
    NoDevice,

    /** The discovery poll itself failed — no answer, as opposed to an empty answer. */
    PollFailed,

    /** More than one usable device is attached and the user has to pick (`err-multi`). */
    MultipleDevices,
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
 */
public data class MirrorErrorCard(
    val kind: MirrorErrorKind,
    val command: String? = null,
    val diagnostic: String = "",
    val rows: List<MirrorRow> = emptyList(),
    val primaryDeviceId: String? = null,
    val canBootEmulator: Boolean = false,
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
) {

    /** How many annotations are staged — the toolbar's counter badge (AC-48). */
    public val stagedCount: Int get() = frozen?.annotations?.size ?: 0

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
     * What the stage shows.
     *
     * The picker wins while it is open — it is a decision the user has to make. Escalated
     * discovery closes it (there is nothing left to pick), which is what lets the passive
     * NO SIGNAL stage of AC-4 appear without a second flag.
     */
    public val stage: MirrorStage
        get() = when {
            isPickerOpen -> MirrorStage.Picker
            platform == PlatformModel.Ios -> MirrorStage.IosDeferred
            // Ahead of Streaming: the session is still running under a frozen frame, and the
            // stage must show the pinned pixels rather than the ones still arriving.
            frozen != null -> MirrorStage.Frozen
            session is MirrorSession.Streaming -> MirrorStage.Mirror
            else -> MirrorStage.NoSignal
        }
}

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
 * @property deviceSize Size of the pinned screenshot, in device pixels.
 * @property annotations Staged annotations in badge order.
 * @property selectedIndex [StagedAnnotation.index] of the annotation whose popover is open, or
 *   `null` when none is.
 */
public data class FrozenFrame(
    val deviceSize: IntSize,
    val annotations: List<StagedAnnotation> = emptyList(),
    val selectedIndex: Int? = null,
) {
    /** The annotation the popover belongs to, or `null`. */
    public val selected: StagedAnnotation?
        get() = annotations.firstOrNull { it.index == selectedIndex }
}
