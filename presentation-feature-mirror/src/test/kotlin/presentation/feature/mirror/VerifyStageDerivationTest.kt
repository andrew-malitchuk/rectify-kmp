package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorStage
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.VerifyError
import presentation.feature.mirror.source.mirror.VerifyPhase
import presentation.feature.mirror.source.mirror.VerifyRow
import presentation.feature.mirror.source.mirror.VerifyUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * T-7 — where [MirrorStage.Verify] sits in the derivation, and the invariant that keeps an
 * aborted run from reading as a set of verdicts.
 */
class VerifyStageDerivationTest {

    private fun frozen() = FrozenFrame(
        deviceSize = IntSize(1080, 2400),
        annotations = emptyList(),
    )

    private fun verify(error: VerifyError? = null, rows: List<VerifyRow> = emptyList()) =
        VerifyUiState(
            batchId = "s-1",
            deviceLabel = "Pixel 8",
            phase = VerifyPhase.Ready,
            rows = rows,
            error = error,
        )

    private fun state() = MirrorUiState(
        isPickerOpen = false,
        session = MirrorSession.Streaming(deviceId = "emulator-5554"),
    )

    /**
     * The whole ordering in one classification: Verify beats Frozen and Streaming, and loses
     * to the picker.
     *
     * One [assertEquals] over all three rather than three chained asserts — a chain
     * short-circuits, so an ordering mistake that kills one case reads as if it killed the
     * rest too.
     *
     * There is no iOS case here any more: the `platform == Ios -> IosDeferred` guard that used
     * to outrank Verify is gone, and iOS now derives its stage like any other platform.
     */
    @Test
    fun verifyOutranksFrozenAndStreamingButNotThePicker() {
        val verifying = state().copy(verify = verify(), frozen = frozen())

        assertEquals(
            listOf(
                MirrorStage.Verify,
                MirrorStage.Verify,
                MirrorStage.Picker,
            ),
            listOf(
                verifying.stage,
                verifying.copy(frozen = null).stage,
                verifying.copy(isPickerOpen = true).stage,
            ),
        )
    }

    /** Leaving the verify stage falls back to whatever was underneath it, never past it. */
    @Test
    fun clearingVerifyFallsBackToFrozenThenToTheLiveStage() {
        val verifying = state().copy(verify = verify(), frozen = frozen())

        assertEquals(MirrorStage.Frozen, verifying.copy(verify = null).stage)
        assertEquals(MirrorStage.Mirror, verifying.copy(verify = null, frozen = null).stage)
    }

    /** C-2 — an abort carries no verdicts, and the type refuses to represent one that does. */
    @Test
    fun anErrorCannotCarryVerdictRows() {
        val row = VerifyRow(
            index = 0,
            comment = "too tight",
            beforeRect = DeviceRect(10, 20, 30, 40),
            afterRect = DeviceRect(10, 20, 30, 40),
            resultLine = "top 6dp -> 0dp",
            outcome = VerificationOutcome.StillOff,
        )

        assertFailsWith<IllegalArgumentException> {
            verify(error = VerifyError.HierarchyUnusable, rows = listOf(row))
        }
        assertTrue(verify(error = VerifyError.NoLiveSession).rows.isEmpty())
    }

    /** The `n of m` header and the follow-up count come off the rows, not a second field. */
    @Test
    fun summaryCountsComeOffTheRows() {
        val rows = listOf(
            VerifyRow(0, "a", DeviceRect(0, 0, 1, 1), DeviceRect(0, 0, 1, 1), "", VerificationOutcome.Fixed),
            VerifyRow(1, "b", DeviceRect(0, 0, 1, 1), DeviceRect(0, 0, 1, 1), "", VerificationOutcome.StillOff),
            VerifyRow(2, "c", DeviceRect(0, 0, 1, 1), DeviceRect(0, 0, 1, 1), "", VerificationOutcome.Fixed),
        )
        val ready = verify(rows = rows)

        assertEquals(2, ready.fixedCount)
        assertEquals(3, ready.rowCount)
        assertEquals(listOf(1), ready.stillOffRows.map { it.index })
    }
}
