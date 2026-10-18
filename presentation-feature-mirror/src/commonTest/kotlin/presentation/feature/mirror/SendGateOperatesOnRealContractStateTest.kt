package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.send.canSend
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.SendPhase
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Reconciliation forcing test (T-8).
 *
 * Wave 1's T-4 left `core/send/SendGate.kt::canSend` reading a LOCAL stand-in
 * `core.send.MirrorUiState`, documented there as temporary pending this task landing
 * `send: SendPhase` on the REAL `presentation.feature.mirror.source.mirror.MirrorUiState`.
 *
 * This test builds the real contract type directly and hands it straight to `canSend` — it does
 * not go through the stand-in at all. It will not compile until the implementer deletes the
 * `core/send/SendGate.kt` stand-in and repoints `canSend`'s parameter at this real type (per that
 * file's own KDoc, `canSend`'s body needs no change: it already reads exactly `frozen`,
 * `stagedCount` and `send`, all three of which the real type already carries).
 */
class SendGateOperatesOnRealContractStateTest {

    @Test
    fun canSendAcceptsTheRealContractMirrorUiStateDirectly() {
        val state = MirrorUiState(
            frozen = FrozenFrame(
                deviceSize = IntSize(width = 1080, height = 2400),
                annotations = listOf(
                    StagedAnnotation(index = 1, rect = DeviceRect(10, 20, 30, 40)),
                ),
            ),
            send = SendPhase.Idle,
        )

        assertTrue(canSend(state))
    }
}
