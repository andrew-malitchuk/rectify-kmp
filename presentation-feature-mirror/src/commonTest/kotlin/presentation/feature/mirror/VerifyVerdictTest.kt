package presentation.feature.mirror

import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.feature.mirror.core.verify.MIN_REGION_PIXELS
import presentation.feature.mirror.core.verify.REGION_CHANGE_RATIO
import presentation.feature.mirror.core.verify.verdictOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * AC-24 as amended / AC-41 -- the verdict is change-evidence, never the literal "no material
 * difference means resolved" wording, which would report an agent that did nothing as having
 * fixed the bug.
 *
 * Both thresholds are used only to SIZE the inputs (a ratio either side of the bar, a region
 * either side of the floor) -- no assertion names a literal, so retuning them cannot break
 * this suite (bugs.json#PP-TD-4).
 */
class VerifyVerdictTest {

    private val aboveTheBar = REGION_CHANGE_RATIO * 2f
    private val belowTheBar = REGION_CHANGE_RATIO / 2f
    private val bigRegion = MIN_REGION_PIXELS * 10
    private val tinyRegion = MIN_REGION_PIXELS - 1

    @Test
    fun matchedWithNoFindingAndChangedPixelsIsFixed() {
        assertEquals(
            VerificationOutcome.Fixed,
            verdictOf(matched = true, afterFinding = null, changedRatio = aboveTheBar, regionPx = bigRegion),
        )
    }

    @Test
    fun matchedWithNoFindingButUnchangedPixelsIsStillOff() {
        assertEquals(
            VerificationOutcome.StillOff,
            verdictOf(matched = true, afterFinding = null, changedRatio = belowTheBar, regionPx = bigRegion),
        )
    }

    @Test
    fun aFindingInTheAfterFrameIsStillOffHoweverManyPixelsMoved() {
        assertEquals(
            VerificationOutcome.StillOff,
            verdictOf(
                matched = true,
                afterFinding = "top Δ6dp vs item_title · expected 0",
                changedRatio = 1f,
                regionPx = bigRegion,
            ),
        )
    }

    @Test
    fun anUnmatchedNodeIsStillOffEvenWithNoFindingAndFullyChangedPixels() {
        assertEquals(
            VerificationOutcome.StillOff,
            verdictOf(matched = false, afterFinding = null, changedRatio = 1f, regionPx = bigRegion),
        )
    }

    @Test
    fun belowTheRegionFloorTheHierarchyDecidesAloneAndZeroChangeIsFixed() {
        assertEquals(
            VerificationOutcome.Fixed,
            verdictOf(matched = true, afterFinding = null, changedRatio = 0f, regionPx = tinyRegion),
        )
    }

    @Test
    fun belowTheRegionFloorAFindingIsStillOffBecauseOnlyThePixelConjunctIsSkipped() {
        assertEquals(
            VerificationOutcome.StillOff,
            verdictOf(
                matched = true,
                afterFinding = "height 20dp · min touch 48dp",
                changedRatio = 0f,
                regionPx = tinyRegion,
            ),
        )
    }

    @Test
    fun atTheRegionFloorThePixelConjunctAppliesAgain() {
        assertEquals(
            VerificationOutcome.StillOff,
            verdictOf(matched = true, afterFinding = null, changedRatio = 0f, regionPx = MIN_REGION_PIXELS),
        )
    }

    @Test
    fun anUnreadableRegionCannotProduceAFixedOnPixelsAloneWhenTheNodeIsUnmatched() {
        assertEquals(
            VerificationOutcome.StillOff,
            verdictOf(matched = false, afterFinding = null, changedRatio = 0f, regionPx = 0),
        )
    }
}
