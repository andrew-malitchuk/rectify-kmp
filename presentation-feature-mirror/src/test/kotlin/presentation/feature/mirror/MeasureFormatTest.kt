package presentation.feature.mirror

import presentation.feature.mirror.core.measure.deltaLabel
import presentation.feature.mirror.core.measure.leftMisalignmentFinding
import presentation.feature.mirror.core.measure.noteLine
import presentation.feature.mirror.core.measure.plainValueLabel
import presentation.feature.mirror.core.measure.positionLabel
import presentation.feature.mirror.core.measure.replaceNoteLine
import presentation.feature.mirror.core.measure.sizeLabel
import presentation.feature.mirror.core.measure.topMisalignmentFinding
import presentation.feature.mirror.core.measure.touchTargetFinding
import presentation.feature.mirror.source.mirror.MeasureSide
import presentation.feature.mirror.source.mirror.MeasureUnit
import presentation.feature.mirror.source.mirror.MeasureValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * AC-66 / FR-43 / FR-45 -- the formatting rules from the design's own UI-kit table: × is
 * U+00D7 (never the ASCII letter 'x'), Δ is U+0394 with no space before the unit, positions are
 * parenthesised, and the finding strings are verbatim including their `vs {id}` segment and the
 * U+00B7 middle-dot separator -- ground truth confirmed by MeasureCard's own shipped @Preview
 * (`top Δ6dp vs item_title · expected 0`).
 */
class MeasureFormatTest {

    @Test
    fun sizeUsesTheRealMultiplicationSignNeverTheAsciiLetterX() {
        val label = sizeLabel(48, 17, MeasureUnit.Dp)
        assertEquals("48×17dp", label)
        assertTrue(label.contains('×'))
        assertTrue('x' !in label)
        assertTrue('X' !in label)
    }

    @Test
    fun deltaUsesTheCapitalDeltaCodepointWithNoSpaceBeforeTheUnit() {
        val label = deltaLabel(6, MeasureUnit.Dp)
        assertEquals("Δ6dp", label)
        assertEquals('Δ', label[0])
    }

    @Test
    fun pxUnitFormatsTheSameWayAsDp() {
        assertEquals("Δ6px", deltaLabel(6, MeasureUnit.Px))
        assertEquals("6px", plainValueLabel(6, MeasureUnit.Px))
    }

    @Test
    fun positionIsParenthesisedWithACommaSpace() {
        assertEquals("(1035, 862)", positionLabel(1035, 862))
    }

    @Test
    fun topMisalignmentFindingIsVerbatimIncludingTheVsSegment() {
        assertEquals(
            "top Δ6dp vs item_title · expected 0",
            topMisalignmentFinding(6, MeasureUnit.Dp, "item_title"),
        )
        assertTrue(topMisalignmentFinding(6, MeasureUnit.Dp, "item_title").contains('·'))
    }

    @Test
    fun leftMisalignmentFindingIsVerbatimIncludingTheVsSegment() {
        assertEquals(
            "left Δ9dp vs item_icon · expected 0",
            leftMisalignmentFinding(9, MeasureUnit.Dp, "item_icon"),
        )
    }

    @Test
    fun touchTargetFindingIsVerbatimWithBothFigures() {
        assertEquals(
            "height 20dp · min touch 48dp",
            touchTargetFinding(20, MeasureUnit.Dp),
        )
    }

    @Test
    fun noteLineComposesTheFiveRowsInOrderWithTheOptionGlyphPrefix() {
        val values = listOf(
            MeasureValue(MeasureSide.Size, "48×17dp"),
            MeasureValue(MeasureSide.Top, "9dp"),
            MeasureValue(MeasureSide.Right, "20dp"),
            MeasureValue(MeasureSide.Bottom, "49dp"),
            MeasureValue(MeasureSide.Left, "62dp"),
        )
        assertEquals(
            "⌥ item_price 48×17dp · top 9dp · right 20dp · bottom 49dp · left 62dp",
            noteLine(nodeId = "item_price", values = values, finding = null),
        )
    }

    @Test
    fun noteLineAppendsTheFindingWhenPresent() {
        val values = listOf(
            MeasureValue(MeasureSide.Size, "48×17dp"),
            MeasureValue(MeasureSide.Top, "9dp"),
            MeasureValue(MeasureSide.Right, "20dp"),
            MeasureValue(MeasureSide.Bottom, "49dp"),
            MeasureValue(MeasureSide.Left, "62dp"),
        )
        val finding = "top Δ6dp vs item_title · expected 0"
        assertEquals(
            "⌥ item_price 48×17dp · top 9dp · right 20dp · bottom 49dp · left 62dp" +
                " · top Δ6dp vs item_title · expected 0",
            noteLine(nodeId = "item_price", values = values, finding = finding),
        )
    }

    @Test
    fun replaceNoteLineDropsAPriorOptionLineButKeepsUserProse() {
        val existing = "Looks great\n⌥ item_price 48×17dp · top 9dp · right 20dp · bottom 49dp · left 62dp"
        val replaced = replaceNoteLine(existing, "⌥ item_price 10×10dp · top 1dp · right 1dp · bottom 1dp · left 1dp")
        assertEquals(
            "Looks great\n⌥ item_price 10×10dp · top 1dp · right 1dp · bottom 1dp · left 1dp",
            replaced,
        )
    }

    @Test
    fun replaceNoteLineAppendsWhenNoPriorOptionLineExists() {
        val existing = "Just my own note"
        val replaced = replaceNoteLine(existing, "⌥ item_price 10×10dp · top 1dp · right 1dp · bottom 1dp · left 1dp")
        assertEquals(
            "Just my own note\n⌥ item_price 10×10dp · top 1dp · right 1dp · bottom 1dp · left 1dp",
            replaced,
        )
    }

    @Test
    fun replaceNoteLineOnAnEmptyCommentJustWritesTheLine() {
        assertEquals(
            "⌥ item_price 10×10dp · top 1dp · right 1dp · bottom 1dp · left 1dp",
            replaceNoteLine("", "⌥ item_price 10×10dp · top 1dp · right 1dp · bottom 1dp · left 1dp"),
        )
    }
}
