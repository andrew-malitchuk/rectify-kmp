package presentation.feature.mirror

import androidx.compose.ui.unit.IntOffset
import presentation.feature.mirror.core.measure.annotationAt
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** annotationAt: pure topmost-wins hit test in device pixels. */
class AnnotationHitTestTest {

    private fun annotation(index: Int, rect: DeviceRect): StagedAnnotation =
        StagedAnnotation(index = index, rect = rect)

    @Test
    fun theHighestIndexAnnotationWinsAmongOverlappingOnes() {
        val annotations = listOf(
            annotation(1, DeviceRect(0, 0, 100, 100)),
            annotation(2, DeviceRect(50, 50, 150, 150)),
        )
        assertEquals(annotations[1], annotationAt(IntOffset(60, 60), annotations))
    }

    @Test
    fun aPointOutsideEveryAnnotationReturnsNull() {
        val annotations = listOf(annotation(1, DeviceRect(0, 0, 100, 100)))
        assertNull(annotationAt(IntOffset(500, 500), annotations))
    }

    @Test
    fun anEmptyListAlwaysReturnsNull() {
        assertNull(annotationAt(IntOffset(0, 0), emptyList()))
    }

    @Test
    fun theRightAndBottomEdgesAreExclusivePerDeviceRectsOwnContract() {
        val annotations = listOf(annotation(1, DeviceRect(0, 0, 100, 100)))
        assertNull(annotationAt(IntOffset(100, 50), annotations))
        assertNull(annotationAt(IntOffset(50, 100), annotations))
        assertEquals(annotations[0], annotationAt(IntOffset(99, 99), annotations))
    }
}
