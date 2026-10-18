package presentation.feature.mirror.core.send

import presentation.core.ui.source.kit.molecule.status.StatusStep
import presentation.core.ui.source.kit.molecule.status.StatusStepState
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.MirrorSession

/**
 * STUB owned by T-8 (wave 2) — see plan.json's "MirrorContract additions" entry
 * (`public enum class SendPhase { Idle, Preparing, Publishing, Sent, Failed }` with a public
 * `isInFlight: Boolean` getter, plus `MirrorUiState.send: SendPhase`). Declared `internal` here,
 * duplicating the five documented cases verbatim, only so T-4's tests compile ahead of T-8
 * landing the real type in `MirrorContract.kt`. T-8 should delete this declaration and repoint
 * every reference in `core/send/` at the real, `public` `SendPhase` — nothing here changes shape
 * when that happens, including [isInFlight].
 */
internal enum class SendPhase {
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

    /** Whether a send is actively running — [Preparing] or [Publishing]. */
    val isInFlight: Boolean get() = this == Preparing || this == Publishing
}

/** [StatusStep.label] for the pipeline's first step when the session is live or idle. */
internal const val STEP_STREAMING: String = "Streaming"

/** [StatusStep.label] for the pipeline's first step when the session dropped mid-pipeline (SC-27). */
internal const val STEP_STREAM_DROPPED: String = "Stream dropped"

/** [StatusStep.label] for the pipeline's second step — the pinned frame behind the send. */
internal const val STEP_FROZEN: String = "Frozen"

/** [StatusStep.label] for the pipeline's third step. */
internal const val STEP_DUMPING_HIERARCHY: String = "Dumping hierarchy"

/** [StatusStep.label] for the pipeline's fourth and final step. */
internal const val STEP_SENT: String = "Sent"

/**
 * The four-step `Streaming › Frozen › Dumping hierarchy › Sent` pipeline (AC-19), reduced to a
 * pure `(phase, session, hierarchy) -> List<StatusStep>` projection because this project has no
 * Compose UI-test harness (`.claude/rules/testing.md`) — the animated ordering is provable only
 * here.
 *
 * Every call to this function happens after [presentation.feature.mirror.source.mirror.MirrorUiState.frozen]
 * is already non-null: [canSend] requires a frozen frame before `MirrorIntent.ResolveAndSend` can
 * even be dispatched, so step 2 ("Frozen") is always rendered in its [StatusStepState.Frozen]
 * state — there is no reachable "not yet frozen" case inside this function's domain, unlike the
 * generic `StatusLine` demo in its own `@Preview`.
 *
 * Step 1 is the one place [session] can override the pipeline mid-flight: a device going away
 * (SC-27) swaps the label from "Streaming" to "Stream dropped" and the state to
 * [StatusStepState.Error] — **while steps 2 through 4 keep completing normally**, because the
 * held frame bytes and the already-dumped hierarchy do not depend on the transport staying up.
 *
 * @param phase Current send phase.
 * @param session Current mirroring session — read only for its [MirrorSession.Lost] case.
 * @param hierarchy Hierarchy verdict for the pinned frame, which drives step 3 independently of
 *   [phase]: the dump happens at freeze time, not during the send.
 * @return Exactly four steps, in pipeline order.
 */
internal fun sendSteps(
    phase: SendPhase,
    session: MirrorSession,
    hierarchy: HierarchyStatus,
): List<StatusStep> = listOf(
    if (session is MirrorSession.Lost) {
        StatusStep(STEP_STREAM_DROPPED, StatusStepState.Error)
    } else {
        StatusStep(STEP_STREAMING, StatusStepState.Done)
    },
    StatusStep(STEP_FROZEN, StatusStepState.Frozen),
    StatusStep(
        STEP_DUMPING_HIERARCHY,
        when (hierarchy) {
            HierarchyStatus.Dumping -> StatusStepState.Busy
            HierarchyStatus.Ready -> StatusStepState.Done
            // Folded, same as hierarchySourceLabel: nothing usable is nothing to show done.
            HierarchyStatus.Absent, HierarchyStatus.Unavailable -> StatusStepState.Pending
        },
    ),
    StatusStep(
        STEP_SENT,
        when (phase) {
            SendPhase.Idle -> StatusStepState.Pending
            SendPhase.Preparing, SendPhase.Publishing -> StatusStepState.Busy
            SendPhase.Sent -> StatusStepState.Done
            SendPhase.Failed -> StatusStepState.Error
        },
    ),
)

/**
 * Short trailing state word shown beside the pipeline (`live`, `failed`, `sent`), or `null` while
 * nothing is in flight.
 *
 * [SendPhase.Idle] is **always** `null` regardless of [session] — the overlay that shows this
 * trailing word is hidden whenever `phase == Idle`, so the session dimension is only consulted
 * once a send has actually started. Within the two in-flight phases ([SendPhase.Preparing],
 * [SendPhase.Publishing]), a dropped session reads as `"failed"` early, ahead of the reducer
 * actually landing [SendPhase.Failed].
 *
 * The one asymmetric case: [SendPhase.Sent] **always** wins over a dropped [session], even though
 * it is reached via an in-flight phase first. A send can finish successfully after the device
 * went away mid-pipeline (SC-27, AS-31's "resume live is skipped when the session is Lost")
 * because publishing never needed the live transport, only the bytes already held from the
 * freeze — so the terminal word must say `"sent"`, not `"failed"`.
 *
 * @param phase Current send phase.
 * @param session Current mirroring session.
 * @return `"sent"`, `"failed"`, `"live"`, or `null`.
 */
internal fun sendTrailing(phase: SendPhase, session: MirrorSession): String? =
    when (phase) {
        SendPhase.Idle -> null
        SendPhase.Sent -> "sent"
        SendPhase.Failed -> "failed"
        SendPhase.Preparing, SendPhase.Publishing ->
            if (session is MirrorSession.Lost) "failed" else "live"
    }
