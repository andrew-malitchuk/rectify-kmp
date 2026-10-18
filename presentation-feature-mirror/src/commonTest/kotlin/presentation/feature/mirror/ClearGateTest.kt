package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.send.canClear
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.SendPhase
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
 * TAC-2 (T-10): `canClear` — "Clear is disabled for the same in-flight window Resolve & Send
 * is". New for this task: before T-10 the toolbar's `Clear` ghost button only read
 * `mirror.stagedCount > 0` (see `RectifyWindow.kt`'s `clearAction` slot) — it did not consult
 * [SendPhase.isInFlight] at all, so a second concurrent send and a batch-wipe mid-send were both
 * reachable at once. Every case here is new coverage, unlike `SendGateTest`'s and
 * `SendPhaseTest`'s cases, which T-8/T-9 already locked down.
 */
class ClearGateTest {

    @Test
    fun falseAtZeroStagedAnnotations() {
        val state = MirrorUiState(frozen = frozenWith(0), send = SendPhase.Idle)

        assertFalse(canClear(state))
    }

    @Test
    fun trueWithAtLeastOneStagedAndIdlePhase() {
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Idle)

        assertTrue(canClear(state))
    }

    @Test
    fun falseWhilePreparingEvenWithAStagedBatch() {
        // The new clause TAC-2 adds: previously this read only stagedCount > 0 and would have
        // been true here.
        val state = MirrorUiState(frozen = frozenWith(2), send = SendPhase.Preparing)

        assertFalse(canClear(state))
    }

    @Test
    fun falseWhilePublishingEvenWithAStagedBatch() {
        val state = MirrorUiState(frozen = frozenWith(2), send = SendPhase.Publishing)

        assertFalse(canClear(state))
    }

    @Test
    fun trueAfterAFailedSendLeavesTheBatchClearable() {
        // AS-31/AC-20: a failed send re-enables Clear over the SAME untouched batch, exactly
        // like it re-enables Resolve & Send.
        val state = MirrorUiState(frozen = frozenWith(2), send = SendPhase.Failed)

        assertTrue(canClear(state))
    }

    @Test
    fun trueAfterASuccessfulSendPriorToTheCounterReset() {
        val state = MirrorUiState(frozen = frozenWith(1), send = SendPhase.Sent)

        assertTrue(canClear(state))
    }
}
