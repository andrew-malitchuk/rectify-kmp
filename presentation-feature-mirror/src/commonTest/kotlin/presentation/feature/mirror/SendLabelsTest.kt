package presentation.feature.mirror

import presentation.feature.mirror.core.send.toastLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * TAC-4 (AC-19, AC-21): `toastLabel` — the completion toast's body text. Machine text, never
 * localised, same precedent as `core/payload/PayloadLabels.kt`'s `hierarchySourceLabel` and
 * `buildPrompt`'s singular/plural handling.
 *
 * Every case that pins the full string passes `hierarchyAttached = true` because that is the
 * shape these cases were written to pin; the `frame + hierarchy` segment is only truthful for
 * a send that carried one, which the AF-4 cases at the bottom cover from the other side.
 */
class SendLabelsTest {

    @Test
    fun singleAnnotationUsesTheSingularNoun() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 1, elapsedMs = 1_400, hierarchyAttached = true),
        )
    }

    @Test
    fun multipleAnnotationsUseThePluralNoun() {
        assertEquals(
            "Sent 3 annotations to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 3, elapsedMs = 1_400, hierarchyAttached = true),
        )
    }

    @Test
    fun zeroAnnotationsStillUsesThePluralNoun() {
        // Matches buildPrompt's own "count == 1" singular test: zero is plural, same as "0 issues".
        assertEquals(
            "Sent 0 annotations to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 0, elapsedMs = 1_400, hierarchyAttached = true),
        )
    }

    @Test
    fun separatorsAreTheLiteralMiddleDotNotAsciiStandIns() {
        val label = toastLabel(count = 1, elapsedMs = 500, hierarchyAttached = true)

        assertEquals(2, label.count { it == '·' })
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.5s",
            label,
        )
    }

    @Test
    fun elapsedTimeIsFlooredToOneDecimalPlaceNotRounded() {
        // 1,449 ms floors to 1.4s -- rounding would read 1.5s.
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 1, elapsedMs = 1_449, hierarchyAttached = true),
        )
    }

    @Test
    fun elapsedTimeExactlyOnATenthBoundaryIsNotBumpedToTheNextTenth() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 2.0s",
            toastLabel(count = 1, elapsedMs = 2_000, hierarchyAttached = true),
        )
    }

    @Test
    fun subHundredMillisecondElapsedFloorsToOneTenthOfASecondNotZero() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 50, hierarchyAttached = true),
        )
    }

    @Test
    fun zeroElapsedMillisecondsStillFloorsToOneTenthOfASecond() {
        // A send is never reported as having taken no time at all.
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 0, hierarchyAttached = true),
        )
    }

    @Test
    fun ninetyNineMillisecondsStillFloorsToOneTenthOfASecond() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 99, hierarchyAttached = true),
        )
    }

    @Test
    fun oneHundredMillisecondsIsExactlyOneTenthOfASecondUnclamped() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 100, hierarchyAttached = true),
        )
    }

    // -- AF-4 (Stage 5): the toast may not claim a hierarchy that was never sent ----------------

    @Test
    fun aSendWithoutAHierarchyReportsTheFrameAlone() {
        // iOS, or an Absent/Unavailable dump: the envelope ships hierarchyAvailable = false, so
        // the toast must not say otherwise. The rest of the shape is untouched.
        val label = toastLabel(count = 3, elapsedMs = 1_400, hierarchyAttached = false)

        assertEquals("Sent 3 annotations to Claude Code · frame · 1.4s", label)
        assertFalse(label.contains("hierarchy"))
        assertEquals(2, label.count { it == '·' })
    }

    @Test
    fun theHierarchySegmentIsTheOnlyDifferenceBetweenTheTwoShapes() {
        assertEquals(
            toastLabel(count = 2, elapsedMs = 1_400, hierarchyAttached = true),
            toastLabel(count = 2, elapsedMs = 1_400, hierarchyAttached = false)
                .replace("· frame ·", "· frame + hierarchy ·"),
        )
    }
}
