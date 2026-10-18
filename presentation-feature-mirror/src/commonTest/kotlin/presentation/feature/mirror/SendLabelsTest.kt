package presentation.feature.mirror

import presentation.feature.mirror.core.send.toastLabel
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TAC-4 (AC-19, AC-21): `toastLabel` — the completion toast's body text. Machine text, never
 * localised, same precedent as `core/payload/PayloadLabels.kt`'s `hierarchySourceLabel` and
 * `buildPrompt`'s singular/plural handling.
 */
class SendLabelsTest {

    @Test
    fun singleAnnotationUsesTheSingularNoun() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 1, elapsedMs = 1_400),
        )
    }

    @Test
    fun multipleAnnotationsUseThePluralNoun() {
        assertEquals(
            "Sent 3 annotations to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 3, elapsedMs = 1_400),
        )
    }

    @Test
    fun zeroAnnotationsStillUsesThePluralNoun() {
        // Matches buildPrompt's own "count == 1" singular test: zero is plural, same as "0 issues".
        assertEquals(
            "Sent 0 annotations to Claude Code · frame + hierarchy · 1.4s",
            toastLabel(count = 0, elapsedMs = 1_400),
        )
    }

    @Test
    fun separatorsAreTheLiteralMiddleDotNotAsciiStandIns() {
        val label = toastLabel(count = 1, elapsedMs = 500)

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
            toastLabel(count = 1, elapsedMs = 1_449),
        )
    }

    @Test
    fun elapsedTimeExactlyOnATenthBoundaryIsNotBumpedToTheNextTenth() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 2.0s",
            toastLabel(count = 1, elapsedMs = 2_000),
        )
    }

    @Test
    fun subHundredMillisecondElapsedFloorsToOneTenthOfASecondNotZero() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 50),
        )
    }

    @Test
    fun zeroElapsedMillisecondsStillFloorsToOneTenthOfASecond() {
        // A send is never reported as having taken no time at all.
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 0),
        )
    }

    @Test
    fun ninetyNineMillisecondsStillFloorsToOneTenthOfASecond() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 99),
        )
    }

    @Test
    fun oneHundredMillisecondsIsExactlyOneTenthOfASecondUnclamped() {
        assertEquals(
            "Sent 1 annotation to Claude Code · frame + hierarchy · 0.1s",
            toastLabel(count = 1, elapsedMs = 100),
        )
    }
}
