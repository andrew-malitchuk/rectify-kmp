package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.MeasureReadout
import presentation.feature.mirror.source.mirror.MeasureUnit
import presentation.feature.mirror.source.mirror.MirrorSession
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
