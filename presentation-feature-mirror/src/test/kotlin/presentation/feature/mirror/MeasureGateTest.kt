package presentation.feature.mirror

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import presentation.feature.mirror.core.measure.MeasureDispatch
import presentation.feature.mirror.core.measure.measureDispatch
import presentation.feature.mirror.core.measure.measureTarget
import presentation.feature.mirror.core.measure.shouldMeasure

/**
 * The Alt layer's two decisions, which nothing else in this repo can check.
 *
 * There is no Compose UI-test harness and this host cannot load Skiko, so the gating and the
 * dispatch edge were pulled out of `MirrorAnnotationStage`'s composable specifically to be
 * assertable here. Everything else in that layer (focus, key events, pointer passes) stays
 * demo-verified.
 */
class MeasureGateTest {

    @Test
    fun altHeldOverAnAnnotationOnAFrozenFrameMeasures() {
        assertTrue(
            shouldMeasure(altHeld = true, dragInProgress = false, frozen = true, hoverIndex = 1),
        )
    }

    @Test
    fun withoutAltNothingMeasures() {
        assertFalse(
            shouldMeasure(altHeld = false, dragInProgress = false, frozen = true, hoverIndex = 1),
        )
    }

    @Test
    fun aDragInProgressIgnoresAlt() {
        assertFalse(
            shouldMeasure(altHeld = true, dragInProgress = true, frozen = true, hoverIndex = 1),
        )
    }

    @Test
    fun aLiveStageIgnoresAlt() {
        assertFalse(
            shouldMeasure(altHeld = true, dragInProgress = false, frozen = false, hoverIndex = 1),
        )
    }

    @Test
    fun altOverEmptyFrameMeasuresNothing() {
        assertFalse(
            shouldMeasure(altHeld = true, dragInProgress = false, frozen = true, hoverIndex = null),
        )
    }

    @Test
    fun measureTargetCarriesTheHoveredIndexWhenAdmissible() {
        assertEquals(
            3,
            measureTarget(altHeld = true, dragInProgress = false, frozen = true, hoverIndex = 3),
        )
    }

    @Test
    fun measureTargetIsNullWheneverAnyGateFails() {
        assertNull(
            measureTarget(altHeld = true, dragInProgress = true, frozen = true, hoverIndex = 3),
        )
        assertNull(
            measureTarget(altHeld = false, dragInProgress = false, frozen = true, hoverIndex = 3),
        )
        assertNull(
            measureTarget(altHeld = true, dragInProgress = false, frozen = false, hoverIndex = 3),
        )
    }

    @Test
    fun pressingAltOverAnAnnotationStartsOnce() {
        assertEquals(MeasureDispatch.Start, measureDispatch(active = null, desired = 1))
    }

    @Test
    fun holdingAltStillOverTheSameAnnotationDispatchesNothing() {
        assertEquals(MeasureDispatch.None, measureDispatch(active = 1, desired = 1))
    }

    @Test
    fun movingToAnotherAnnotationStartsRatherThanEnding() {
        assertEquals(MeasureDispatch.Start, measureDispatch(active = 1, desired = 2))
    }

    @Test
    fun releasingAltEndsOnce() {
        assertEquals(MeasureDispatch.End, measureDispatch(active = 1, desired = null))
        assertEquals(MeasureDispatch.None, measureDispatch(active = null, desired = null))
    }

    /**
     * The TAC-24 shape: three annotations hovered under one hold produce three StartMeasures and
     * exactly one EndMeasure, with the frames in between silent.
     */
    @Test
    fun threeAnnotationsUnderOneHoldGiveThreeStartsAndOneEnd() {
        // Alt down, then the pointer crosses annotations 1, 2 and 3 with several idle frames on
        // each, then Alt is released.
        val hovers = listOf(1, 1, 1, 2, 2, 3, 3, 3)
        var active: Int? = null
        val dispatched = mutableListOf<MeasureDispatch>()

        hovers.forEach { hover ->
            val desired = measureTarget(
                altHeld = true,
                dragInProgress = false,
                frozen = true,
                hoverIndex = hover,
            )
            dispatched += measureDispatch(active = active, desired = desired)
            active = desired
        }
        val released = measureTarget(
            altHeld = false,
            dragInProgress = false,
            frozen = true,
            hoverIndex = 3,
        )
        dispatched += measureDispatch(active = active, desired = released)

        assertEquals(3, dispatched.count { it == MeasureDispatch.Start })
        assertEquals(1, dispatched.count { it == MeasureDispatch.End })
        assertEquals(MeasureDispatch.End, dispatched.last())
    }

    /**
     * A key-up the key path never saw. Focus loss and the next pointer-down both clear `altHeld`,
     * which is the same input to this decision as a KeyUp — so all three recovery paths produce
     * the one EndMeasure that unsticks the Measuring panel.
     */
    @Test
    fun aMissedKeyUpStillEndsThroughFocusLossOrPointerDown() {
        var active: Int? = measureTarget(
            altHeld = true,
            dragInProgress = false,
            frozen = true,
            hoverIndex = 2,
        )
        assertEquals(2, active)

        // altHeld cleared by onFocusChanged(lost) or by the pointer-down sampler branch.
        val desired = measureTarget(
            altHeld = false,
            dragInProgress = false,
            frozen = true,
            hoverIndex = 2,
        )
        assertEquals(MeasureDispatch.End, measureDispatch(active = active, desired = desired))

        // And the recovery does not then repeat on every later frame.
        active = desired
        assertEquals(MeasureDispatch.None, measureDispatch(active = active, desired = desired))
    }

    /** A frame vanishing under a live hold ends the measurement rather than leaving it stuck. */
    @Test
    fun resumingLiveUnderAHoldEnds() {
        val desired = measureTarget(
            altHeld = true,
            dragInProgress = false,
            frozen = false,
            hoverIndex = 1,
        )
        assertEquals(MeasureDispatch.End, measureDispatch(active = 1, desired = desired))
    }
}
