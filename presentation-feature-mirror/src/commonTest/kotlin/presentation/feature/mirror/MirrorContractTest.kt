package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.PlatformModel
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.MeasureReadout
import presentation.feature.mirror.source.mirror.MeasureUnit
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorStage
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals

class MirrorContractTest {

    private fun frozen() = FrozenFrame(
        deviceSize = IntSize(1080, 2400),
        annotations = listOf(StagedAnnotation(index = 1, rect = DeviceRect(10, 20, 30, 40))),
    )

    /**
     * T4 — Resume Live is withdrawn once the device is gone (NFR-6).
     *
     * MUTATION (verbatim, from T-11 TAC-3): make `canResumeLive` return `frozen != null`
     * unconditionally — the Lost case must fail.
     *
     * The three cases are classified into ONE [assertEquals] rather than chained
     * `assertTrue`/`assertFalse`: a chain short-circuits on the first failure, so a mutation
     * that kills only one case reads as if it had killed every case after it too.
     */
    @Test
    fun resumeLiveIsNotOfferedWhileTheSessionIsLost() {
        val streaming = MirrorUiState(
            frozen = frozen(),
            session = MirrorSession.Streaming(deviceId = "emulator-5554"),
        )
        val lost = streaming.copy(session = MirrorSession.Lost(deviceId = "emulator-5554"))

        assertEquals(
            expected = listOf("streaming=true", "lost=false", "noFrozenFrame=false"),
            actual = listOf(
                "streaming=${streaming.canResumeLive}",
                "lost=${lost.canResumeLive}",
                "noFrozenFrame=${streaming.copy(frozen = null).canResumeLive}",
            ),
        )
    }

    /**
     * T-19 — `MirrorStage` is still a closed set a `when` can cover without an `else`.
     *
     * The `when` below is the real assertion: `IosDeferred` was deleted in F-10, and had any
     * branch of it survived — or had a new member been added without a render branch — this
     * file would not COMPILE. The equality underneath pins the five remaining members so the
     * deleted one cannot quietly come back as a sixth.
     */
    @Test
    fun everyMirrorStageIsCoveredByAnExhaustiveWhen() {
        val labels = MirrorStage.entries.map { stage ->
            when (stage) {
                MirrorStage.Picker -> "picker"
                MirrorStage.Mirror -> "mirror"
                MirrorStage.NoSignal -> "noSignal"
                MirrorStage.Frozen -> "frozen"
                MirrorStage.Verify -> "verify"
            }
        }

        assertEquals(listOf("picker", "mirror", "noSignal", "frozen", "verify"), labels)
    }

    /** T-19 — and the derivation reaches each of them; iOS no longer short-circuits to a stage. */
    @Test
    fun theStageDerivationHasNoIosBranchLeft() {
        // The picker outranks every other stage and defaults to open, so it has to be closed
        // for the session underneath to be the thing under test.
        val streaming = MirrorUiState(
            session = MirrorSession.Streaming(deviceId = "emulator-5554"),
            isPickerOpen = false,
        )

        assertEquals(
            expected = listOf(MirrorStage.Mirror, MirrorStage.Mirror),
            actual = listOf(
                streaming.copy(platform = PlatformModel.Android).stage,
                // The one that used to be IosDeferred regardless of what the session was doing.
                streaming.copy(platform = PlatformModel.Ios).stage,
            ),
        )
    }

    /** T-9 / TAC-18 — the two new fields default to "nothing measured yet". */
    @Test
    fun measureDefaultsToNullAndHierarchyStatusDefaultsToAbsent() {
        val state = MirrorUiState()

        assertEquals(
            expected = listOf("measure=null", "hierarchyStatus=${HierarchyStatus.Absent}"),
            actual = listOf(
                "measure=${state.measure}",
                "hierarchyStatus=${state.hierarchyStatus}",
            ),
        )
    }

    /**
     * T-9 — a sanity check that [MeasureReadout]'s final shape (minted in T-8) is still a plain
     * value-equal data class. Guards against a future field turning it into something `StartMeasure`
     * could not safely cache and compare.
     */
    @Test
    fun anUnchangedMeasureReadoutComparesEqual() {
        val readout = MeasureReadout(
            annotationIndex = 1,
            nodeId = "item_price",
            targetRect = DeviceRect(228, 198, 276, 215),
            neighbourRects = listOf(DeviceRect(84, 192, 166, 209)),
            values = emptyList(),
            guides = emptyList(),
            rail = null,
            finding = null,
            unit = MeasureUnit.Dp,
            noteLine = "⌥ item_price 48×17dp",
        )

        assertEquals(readout, readout.copy())
    }
}
