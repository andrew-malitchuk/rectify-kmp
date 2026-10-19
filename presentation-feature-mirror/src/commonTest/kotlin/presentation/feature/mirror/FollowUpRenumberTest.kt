package presentation.feature.mirror

import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.feature.mirror.core.verify.followUpAnnotations
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.VerifyRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun row(
    index: Int,
    comment: String,
    beforeRect: DeviceRect = DeviceRect(0, 0, 10, 10),
    afterRect: DeviceRect = DeviceRect(100, 200, 180, 260),
): VerifyRow = VerifyRow(
    index = index,
    comment = comment,
    beforeRect = beforeRect,
    afterRect = afterRect,
    resultLine = "48×17 → 48×17",
    outcome = VerificationOutcome.StillOff,
)

/**
 * T-13 (TAC-1) — the 0-based history order to 1-based badge conversion, on its own.
 *
 * Kept away from the ViewModel on purpose: the renumbering is the part a reviewer has to be
 * able to read at a glance, and the mistake it guards against (re-sending the rows' ORIGINAL
 * badges, or their 0-based indices) is arithmetic, not wiring.
 */
class FollowUpRenumberTest {

    @Test
    fun stillOffRowsAreRenumberedContiguouslyFromOne() {
        // The rows that survived are the batch's FIRST and THIRD, so neither their 0-based
        // history index nor their original badge can pass for the answer by coincidence.
        val staged = followUpAnnotations(
            listOf(row(index = 0, comment = "first"), row(index = 2, comment = "third")),
        )

        assertEquals(
            listOf(1, 2),
            staged.map { it.index },
            "a 2-of-3 re-stage badges 1 and 2 - not the rows' 0,2 and not their old 1,3",
        )
    }

    @Test
    fun eachAnnotationKeepsItsCommentAndItsAfterRect() {
        val first = row(index = 0, comment = "still clipped", afterRect = DeviceRect(1, 2, 3, 4))
        val second = row(index = 2, comment = "still off by 4dp", afterRect = DeviceRect(5, 6, 7, 8))

        val staged = followUpAnnotations(listOf(first, second))

        assertEquals(listOf("still clipped", "still off by 4dp"), staged.map { it.comment })
        assertEquals(
            listOf(first.afterRect, second.afterRect),
            staged.map { it.rect },
            "the follow-up is drawn on the re-captured frame, so it carries the AFTER rects",
        )
    }

    @Test
    fun aTailOfTheBatchStillStartsAtOne() {
        val staged = followUpAnnotations(
            listOf(row(index = 3, comment = "fourth"), row(index = 4, comment = "fifth")),
        )

        assertEquals(listOf(1, 2), staged.map { it.index }, "badges are contiguous from 1")
    }

    @Test
    fun noRowsStageNothing() {
        assertTrue(followUpAnnotations(emptyList()).isEmpty())
    }
}
