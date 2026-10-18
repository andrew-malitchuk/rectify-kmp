package presentation.feature.mirror

import presentation.core.ui.source.kit.molecule.status.StatusStep
import presentation.core.ui.source.kit.molecule.status.StatusStepState
import presentation.feature.mirror.core.send.STEP_DUMPING_HIERARCHY
import presentation.feature.mirror.core.send.STEP_FROZEN
import presentation.feature.mirror.core.send.STEP_SENT
import presentation.feature.mirror.core.send.STEP_STREAMING
import presentation.feature.mirror.core.send.STEP_STREAM_DROPPED
import presentation.feature.mirror.core.send.sendSteps
import presentation.feature.mirror.core.send.sendTrailing
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.SendPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val DEVICE_ID = "RF8M90ABCDE"

/**
 * TAC-1 (AC-19, SC-27): `sendSteps` — the four-step `Streaming › Frozen › Dumping hierarchy ›
 * Sent` pipeline, reduced to a pure projection because this project has no Compose UI-test
 * harness. Every combination below is one `sendSteps` enumerates per the plan's test strategy;
 * see `SendProjection.kt`'s own KDoc for the exact per-step rules this suite pins.
 */
class SendProjectionTest {

    @Test
    fun sendStepsReturnsExactlyFourStepsInPipelineOrder() {
        val steps = sendSteps(SendPhase.Idle, MirrorSession.Idle, HierarchyStatus.Absent)

        assertEquals(4, steps.size)
        assertEquals(
            listOf(STEP_STREAMING, STEP_FROZEN, STEP_DUMPING_HIERARCHY, STEP_SENT),
            steps.map { it.label },
        )
    }

    // -- Step 1: Streaming / Stream dropped ---------------------------------------------------

    @Test
    fun streamingSessionShowsStreamingDoneAsFirstStep() {
        val steps = sendSteps(SendPhase.Publishing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_STREAMING, StatusStepState.Done), steps[0])
    }

    @Test
    fun idleSessionAlsoShowsStreamingDoneAsFirstStep() {
        // A send's precondition is a frozen frame, not a live session -- MirrorSession.Idle is
        // the "not Lost" bucket exactly like Streaming is, not a third rendering.
        val steps = sendSteps(SendPhase.Preparing, MirrorSession.Idle, HierarchyStatus.Absent)

        assertEquals(StatusStep(STEP_STREAMING, StatusStepState.Done), steps[0])
    }

    @Test
    fun lostSessionShowsStreamDroppedErrorAsFirstStep() {
        val steps = sendSteps(SendPhase.Publishing, MirrorSession.Lost(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_STREAM_DROPPED, StatusStepState.Error), steps[0])
    }

    @Test
    fun lostSessionStillCompletesTheRemainingThreeSteps() {
        // SC-27's whole point: a dropped stream fails ONLY the first step. The held frame bytes
        // and the already-dumped hierarchy do not depend on the transport staying up, so the
        // send finishes to completion behind the Error dot.
        val steps = sendSteps(SendPhase.Sent, MirrorSession.Lost(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(
            listOf(
                StatusStep(STEP_STREAM_DROPPED, StatusStepState.Error),
                StatusStep(STEP_FROZEN, StatusStepState.Frozen),
                StatusStep(STEP_DUMPING_HIERARCHY, StatusStepState.Done),
                StatusStep(STEP_SENT, StatusStepState.Done),
            ),
            steps,
        )
    }

    // -- Step 2: Frozen -------------------------------------------------------------------------

    @Test
    fun frozenStepIsAlwaysFrozenStateRegardlessOfPhaseOrHierarchy() {
        // A send's precondition (canSend) requires frozen != null, so this step never renders a
        // "not yet frozen" Pending -- there is no reachable case for it inside sendSteps' domain.
        val combos = listOf(
            Triple(SendPhase.Idle, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Absent),
            Triple(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Dumping),
            Triple(SendPhase.Publishing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready),
            Triple(SendPhase.Sent, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Unavailable),
            Triple(SendPhase.Failed, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready),
        )

        combos.forEach { (phase, session, hierarchy) ->
            val steps = sendSteps(phase, session, hierarchy)
            assertEquals(StatusStep(STEP_FROZEN, StatusStepState.Frozen), steps[1])
        }
    }

    // -- Step 3: Dumping hierarchy ----------------------------------------------------------------

    @Test
    fun dumpingHierarchyStepIsBusyWhileADumpIsInFlight() {
        val steps = sendSteps(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Dumping)

        assertEquals(StatusStep(STEP_DUMPING_HIERARCHY, StatusStepState.Busy), steps[2])
    }

    @Test
    fun dumpingHierarchyStepIsDoneWhenAUsableSnapshotIsHeld() {
        val steps = sendSteps(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_DUMPING_HIERARCHY, StatusStepState.Done), steps[2])
    }

    @Test
    fun dumpingHierarchyStepIsPendingWhenNoDumpWasAttempted() {
        val steps = sendSteps(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Absent)

        assertEquals(StatusStep(STEP_DUMPING_HIERARCHY, StatusStepState.Pending), steps[2])
    }

    @Test
    fun dumpingHierarchyStepIsPendingWhenTheDumpProducedNothingUsable() {
        // Folded with Absent, per plan.json's "empty" state note: "the Dumping hierarchy step
        // still renders ... it resolves to Pending rather than Done" for Absent OR Unavailable
        // (iOS/AS-29 included) -- same precedent as hierarchySourceLabel folding these two.
        val steps = sendSteps(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Unavailable)

        assertEquals(StatusStep(STEP_DUMPING_HIERARCHY, StatusStepState.Pending), steps[2])
    }

    // -- Step 4: Sent -----------------------------------------------------------------------------

    @Test
    fun sentStepIsPendingWhileIdle() {
        val steps = sendSteps(SendPhase.Idle, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_SENT, StatusStepState.Pending), steps[3])
    }

    @Test
    fun sentStepIsBusyWhilePreparing() {
        val steps = sendSteps(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_SENT, StatusStepState.Busy), steps[3])
    }

    @Test
    fun sentStepIsBusyWhilePublishing() {
        val steps = sendSteps(SendPhase.Publishing, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_SENT, StatusStepState.Busy), steps[3])
    }

    @Test
    fun sentStepIsDoneOnlyInSendPhaseSent() {
        val steps = sendSteps(SendPhase.Sent, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_SENT, StatusStepState.Done), steps[3])
    }

    @Test
    fun sentStepIsErrorInSendPhaseFailed() {
        val steps = sendSteps(SendPhase.Failed, MirrorSession.Streaming(DEVICE_ID), HierarchyStatus.Ready)

        assertEquals(StatusStep(STEP_SENT, StatusStepState.Error), steps[3])
    }

    // -- Guard against new cases arriving silently -----------------------------------------------

    @Test
    fun sendPhaseStillHasExactlyFiveShippedValues() {
        assertEquals(5, SendPhase.entries.size)
    }

    @Test
    fun hierarchyStatusStillHasExactlyFourShippedValues() {
        assertEquals(4, HierarchyStatus.entries.size)
    }
}

/**
 * TAC-2 (SC-27): `sendTrailing` — the short trailing word (`live`/`failed`/`sent`/`null`) shown
 * beside the status line. See `SendProjection.kt`'s own KDoc for the full priority rule this
 * truth table pins, including the one asymmetric case: `Sent` wins over a dropped session.
 */
class SendTrailingTest {

    @Test
    fun idleIsNullWhenSessionIsStreaming() {
        assertNull(sendTrailing(SendPhase.Idle, MirrorSession.Streaming(DEVICE_ID)))
    }

    @Test
    fun idleIsNullEvenWhenSessionIsLost() {
        // The overlay that shows this trailing word is hidden whenever phase == Idle, so the
        // session dimension is only consulted once a send has actually started.
        assertNull(sendTrailing(SendPhase.Idle, MirrorSession.Lost(DEVICE_ID)))
    }

    @Test
    fun preparingIsLiveWhenSessionIsStreaming() {
        assertEquals("live", sendTrailing(SendPhase.Preparing, MirrorSession.Streaming(DEVICE_ID)))
    }

    @Test
    fun publishingIsLiveWhenSessionIsStreaming() {
        assertEquals("live", sendTrailing(SendPhase.Publishing, MirrorSession.Streaming(DEVICE_ID)))
    }

    @Test
    fun preparingIsFailedWhenSessionIsLost() {
        // The stream can drop before the reducer has reduced SendPhase.Failed -- the trailing
        // word reads it early rather than lagging one event behind.
        assertEquals("failed", sendTrailing(SendPhase.Preparing, MirrorSession.Lost(DEVICE_ID)))
    }

    @Test
    fun publishingIsFailedWhenSessionIsLost() {
        assertEquals("failed", sendTrailing(SendPhase.Publishing, MirrorSession.Lost(DEVICE_ID)))
    }

    @Test
    fun sentIsSentRegardlessOfSession() {
        assertEquals("sent", sendTrailing(SendPhase.Sent, MirrorSession.Streaming(DEVICE_ID)))
    }

    @Test
    fun sentWinsOverALostSessionPerSc27() {
        // The one asymmetric case: AS-31's "resume live is skipped when the session is Lost"
        // confirms a send can finish successfully after the device went away mid-pipeline, so
        // the terminal word must say "sent", not "failed".
        assertEquals("sent", sendTrailing(SendPhase.Sent, MirrorSession.Lost(DEVICE_ID)))
    }

    @Test
    fun failedIsFailedRegardlessOfSession() {
        assertEquals("failed", sendTrailing(SendPhase.Failed, MirrorSession.Streaming(DEVICE_ID)))
        assertEquals("failed", sendTrailing(SendPhase.Failed, MirrorSession.Lost(DEVICE_ID)))
    }
}
