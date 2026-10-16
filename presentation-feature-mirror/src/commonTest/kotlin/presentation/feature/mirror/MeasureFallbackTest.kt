package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.core.measure.MeasureNode
import presentation.feature.mirror.core.measure.measure
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.MeasureSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FR-41 / AC-65 -- no snap match is a first-class outcome: node id falls back to
 * "View (no match)" and every value is still computed, against the annotation's own rect and
 * the frame edges, rather than degrading to a partial or missing readout.
 */
class MeasureFallbackTest {

    private val annotation = DeviceRect(100, 100, 200, 150)
    private val frame = IntSize(1080, 2400)

    @Test
    fun anEmptyNodeListFallsBackToViewNoMatchWithFullValues() {
        val readout = measure(
            annotation = annotation,
            index = 1,
            nodes = emptyList(),
            frameSize = frame,
            densityDpi = 160,
        )
        assertEquals("View (no match)", readout.nodeId)
        assertEquals(annotation, readout.targetRect)
        assertEquals(5, readout.values.size)
        assertEquals(
            setOf(MeasureSide.Size, MeasureSide.Top, MeasureSide.Right, MeasureSide.Bottom, MeasureSide.Left),
            readout.values.map { it.side }.toSet(),
        )
        // No neighbours exist for the fallback rect, so every gap is measured to the frame edge.
        assertEquals("100dp", readout.values.first { it.side == MeasureSide.Top }.value)
        assertEquals("880dp", readout.values.first { it.side == MeasureSide.Right }.value)
        assertEquals("2250dp", readout.values.first { it.side == MeasureSide.Bottom }.value)
        assertEquals("100dp", readout.values.first { it.side == MeasureSide.Left }.value)
    }

    @Test
    fun aNodeListWithNoContainingCandidateAlsoFallsBackToViewNoMatch() {
        val farAway = MeasureNode(
            id = "unrelated",
            rect = DeviceRect(900, 2000, 1000, 2100),
            isTappable = false,
        )
        val readout = measure(
            annotation = annotation,
            index = 1,
            nodes = listOf(farAway),
            frameSize = frame,
            densityDpi = 160,
        )
        assertEquals("View (no match)", readout.nodeId)
        assertEquals(5, readout.values.size)
    }

    @Test
    fun theFallbackReportsNoFindingUnlessTheAnnotationRectItselfIsUndersizedAndTappable() {
        val readout = measure(
            annotation = annotation,
            index = 1,
            nodes = emptyList(),
            frameSize = frame,
            densityDpi = 160,
        )
        // The annotation rect itself has no tappable flag to report -- no node matched it.
        assertNull(readout.finding)
    }
}
