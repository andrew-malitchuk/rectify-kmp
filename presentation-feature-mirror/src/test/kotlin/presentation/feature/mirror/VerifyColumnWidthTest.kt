package presentation.feature.mirror

import androidx.compose.ui.unit.dp
import presentation.feature.mirror.core.verify.verifyColumnWidth
import kotlin.test.Test
import kotlin.test.assertEquals

/** The shipped geometry: `MirrorVerifyStage`'s own constants, so the cases are real sizes. */
private val NOMINAL = 296.dp
private val PADDING = 24.dp
private val GAP = 56.dp

/**
 * The AFTER column went missing because two fixed-width children in a `Row` overflow instead of
 * shrinking. This is the arithmetic that replaced the fixed width.
 */
class VerifyColumnWidthTest {

    /**
     * A roomy stage gets the design width, and a cramped one gets a smaller column rather than
     * a clipped one.
     *
     * 696 dp is the exact fit — `296·2 + 56 + 24·2` — so the two clauses either side of it are
     * the boundary, not two arbitrary numbers. 640 dp is roughly the stage left over on a
     * minimum-width window once both panels have taken their share, which is the case that
     * produced the bug.
     */
    @Test
    fun theColumnTakesTheDesignWidthWhenItFitsAndShrinksWhenItDoesNot() {
        assertEquals(
            expected = listOf("wide=296.0.dp", "exactFit=296.0.dp", "cramped=268.0.dp"),
            actual = listOf(
                "wide=${width(1200.dp)}",
                "exactFit=${width(696.dp)}",
                "cramped=${width(640.dp)}",
            ),
        )
    }

    /**
     * A stage narrower than its own chrome clamps at zero.
     *
     * Without the floor the halved slack is NEGATIVE, and a negative width is a crash in the
     * layout pass rather than a small column — which is the one failure mode worse than the
     * clipping this function exists to fix.
     */
    @Test
    fun aStageNarrowerThanItsOwnChromeClampsAtZeroRatherThanGoingNegative() {
        assertEquals(
            expected = listOf("tiny=0.0.dp", "zero=0.0.dp"),
            actual = listOf("tiny=${width(80.dp)}", "zero=${width(0.dp)}"),
        )
    }

    private fun width(stage: androidx.compose.ui.unit.Dp) =
        verifyColumnWidth(stageWidth = stage, horizontalPadding = PADDING, gap = GAP, nominal = NOMINAL)
}
