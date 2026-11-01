package presentation.feature.mirror

import presentation.feature.mirror.core.annotation.metaLine
import presentation.feature.mirror.core.annotation.sizeLabel
import presentation.feature.mirror.source.mirror.DeviceRect
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotationFormatTest {

    @Test
    fun metaLineMatchesTheDocumentedShape() {
        val rect = DeviceRect(left = 1035, top = 862, right = 1253, bottom = 939)
        assertEquals("218×77 @ 1035,862", metaLine(rect))
    }

    @Test
    fun sizeLabelMatchesTheMarkShape() {
        val rect = DeviceRect(left = 100, top = 200, right = 645, bottom = 454)
        assertEquals("545 × 254", sizeLabel(rect))
    }

    @Test
    fun zeroSizeRectFormatsWithoutCrashing() {
        val rect = DeviceRect(left = 7, top = 9, right = 7, bottom = 9)
        assertEquals("0×0 @ 7,9", metaLine(rect))
        assertEquals("0 × 0", sizeLabel(rect))
    }
}
