package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.send.MirrorUiState
import presentation.feature.mirror.core.send.SendPhase
import presentation.feature.mirror.core.send.canSend
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun stagedAnnotation(index: Int = 1) = StagedAnnotation(
    index = index,
    rect = DeviceRect(left = 120, top = 420, right = 960, bottom = 674),
)

private fun frozenWith(count: Int) = FrozenFrame(
    deviceSize = IntSize(width = 1080, height = 2400),
    annotations = (1..count).map { stagedAnnotation(it) },
)

/**
 * TAC-3 (FR-16, FR-18, SC-27): `canSend` — read by the composable, the reducer and the hoisted
 * ⌘↩ holder as the ONE definition of "may Resolve & Send be dispatched right now".
 *
 * [MirrorUiState] here is `core/send`'s own stub pending T-8's `send: SendPhase` field landing on
 * the real `MirrorContract.MirrorUiState` (see `SendGate.kt`'s KDoc) — not the real contract
 * type.
 */
class SendGateTest {

    @Test
    fun falseAtZeroStagedAnnotations() {
        val state = MirrorUiState(frozen = frozenWith(0), send = SendPhase.Idle)

        assertFalse(canSend(state))
    }

    @Test
    fun falseWithNoFrozenFrameEvenWithIdlePhase() {
        val state = MirrorUiState(frozen = null, send = SendPhase.Idle)

        assertFalse(canSend(state))
    }

    @Test
    fun falseWhilePreparing() {
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Preparing)

        assertFalse(canSend(state))
    }

    @Test
    fun falseWhilePublishing() {
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Publishing)

        assertFalse(canSend(state))
    }

    @Test
    fun trueWithAtLeastOneStagedAndIdlePhase() {
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Idle)

        assertTrue(canSend(state))
    }

    @Test
    fun trueWithSeveralStagedAndIdlePhase() {
        val state = MirrorUiState(frozen = frozenWith(3), send = SendPhase.Idle)

        assertTrue(canSend(state))
    }

    @Test
    fun trueAfterAFailedSendLeavesTheBatchIntact() {
        // AS-31/AC-20: a failed send re-enables the button over the SAME untouched batch.
        val state = MirrorUiState(frozen = frozenWith(2), send = SendPhase.Failed)

        assertTrue(canSend(state))
    }

    @Test
    fun trueAfterASuccessfulSendPriorToTheCounterReset() {
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Sent)

        assertTrue(canSend(state))
    }

    // -- SC-27: canSend deliberately does not test the session at all ---------------------------
    //
    // There is no MirrorSession parameter on canSend's signature -- these two tests prove that
    // is not an oversight by holding every other input identical.

    @Test
    fun trueEvenWhileTheStreamIsLostBecauseCanSendCarriesNoSessionParameterAtAll() {
        // canSend's signature literally cannot read MirrorSession.Lost -- a dropped stream must
        // not disable sending, because the held frame bytes and staged annotations do not depend
        // on the transport staying up. This is the point SC-27 and this test exist to prove: the
        // omission of a session parameter IS the fix, not an accident to be "completed" later.
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Idle)

        assertTrue(canSend(state))
    }
}
