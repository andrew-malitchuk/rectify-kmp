package presentation.feature.mirror

import presentation.feature.mirror.source.mirror.SendPhase
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TAC-1 (T-8): `SendPhase.isInFlight` is `true` for [SendPhase.Preparing] and
 * [SendPhase.Publishing] only.
 *
 * Every enum value is checked in one [assertEquals] against a full expected map rather than a
 * sample of `assertTrue`/`assertFalse` calls — a chain of those short-circuits on the first
 * failure, so a mutation that breaks only one case would read as if it broke every case after it
 * too (same precedent as `MirrorContractTest.resumeLiveIsNotOfferedWhileTheSessionIsLost`).
 */
class SendPhaseTest {

    @Test
    fun isInFlightIsTrueOnlyForPreparingAndPublishing() {
        val expected = mapOf(
            SendPhase.Idle to false,
            SendPhase.Preparing to true,
            SendPhase.Publishing to true,
            SendPhase.Sent to false,
            SendPhase.Failed to false,
        )

        val actual = SendPhase.entries.associateWith { it.isInFlight }

        assertEquals(expected, actual)
    }
}
