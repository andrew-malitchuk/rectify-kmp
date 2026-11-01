package presentation.feature.mirror

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlinx.datetime.TimeZone
import presentation.core.ui.source.kit.atom.annotation.AnnotationState
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.core.ui.source.kit.molecule.status.StatusStepState
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.core.verify.VerifySide
import presentation.feature.mirror.core.verify.frameColumnLabel
import presentation.feature.mirror.core.verify.verifyErrorBodyKey
import presentation.feature.mirror.core.verify.verifyErrorTitleKey
import presentation.feature.mirror.core.verify.verifyMarks
import presentation.feature.mirror.core.verify.verifySteps
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.VerifyError
import presentation.feature.mirror.source.mirror.VerifyPhase
import presentation.feature.mirror.source.mirror.VerifyRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 2025-10-18 14:02:00 UTC and 14:09:00 UTC — the design's own two clock readings. */
private const val SENT_AT_MS = 1_760_796_120_000L
private const val RECAPTURED_AT_MS = 1_760_796_540_000L

/** A 1080x2400 device, which is NOT the design column's 296x640 aspect ratio. */
private val DEVICE_SIZE = IntSize(width = 1080, height = 2400)

private fun row(
    index: Int,
    outcome: VerificationOutcome,
    beforeRect: DeviceRect = DeviceRect(left = 0, top = 0, right = 540, bottom = 1200),
    afterRect: DeviceRect = DeviceRect(left = 540, top = 1200, right = 1080, bottom = 2400),
): VerifyRow = VerifyRow(
    index = index,
    comment = "comment $index",
    beforeRect = beforeRect,
    afterRect = afterRect,
    resultLine = "top 6dp",
    outcome = outcome,
)

class VerifyLabelsTest {

    @Test
    fun frameColumnLabelAppendsTheLocalClock() {
        assertEquals(
            "BEFORE · 14:02",
            frameColumnLabel(prefix = "BEFORE", atMs = SENT_AT_MS, zone = TimeZone.UTC),
        )
    }

    @Test
    fun frameColumnLabelDropsTheClockUntilTheFrameExists() {
        assertEquals(
            "AFTER",
            frameColumnLabel(prefix = "AFTER", atMs = 0L, zone = TimeZone.UTC),
        )
    }

    @Test
    fun frameColumnLabelReadsTheRecaptureClock() {
        assertEquals(
            "AFTER · 14:09",
            frameColumnLabel(prefix = "AFTER", atMs = RECAPTURED_AT_MS, zone = TimeZone.UTC),
        )
    }

    @Test
    fun verifyMarksScaleWithTheMeasuredBoxRatherThanTheNominalColumn() {
        // A letterboxed frame in a 296x640 column: 288x640 painted, NOT the column itself.
        val painted = FrameGeometry(
            framePx = Size(288f, 640f),
            deviceSize = DEVICE_SIZE,
            density = 1f,
        )
        val nominal = FrameGeometry(
            framePx = Size(296f, 640f),
            deviceSize = DEVICE_SIZE,
            density = 1f,
        )
        val rows = listOf(row(index = 0, outcome = VerificationOutcome.Fixed))

        val fromPainted = verifyMarks(painted, rows, VerifySide.Before).single().bounds
        val fromNominal = verifyMarks(nominal, rows, VerifySide.Before).single().bounds

        assertEquals(144f, fromPainted.right.value)
        assertTrue(fromPainted.right != fromNominal.right)
    }

    @Test
    fun verifyMarksTakeTheBeforeRectNeutralAndTheAfterRectByVerdict() {
        val rows = listOf(
            row(index = 0, outcome = VerificationOutcome.Fixed),
            row(index = 1, outcome = VerificationOutcome.StillOff),
        )
        val geometry = FrameGeometry(
            framePx = Size(1080f, 2400f),
            deviceSize = DEVICE_SIZE,
            density = 1f,
        )

        val before = verifyMarks(geometry, rows, VerifySide.Before)
        val after = verifyMarks(geometry, rows, VerifySide.After)

        assertEquals(listOf(AnnotationState.Pinned, AnnotationState.Pinned), before.map { it.state })
        assertEquals(
            listOf(AnnotationState.Sent, AnnotationState.StillOff),
            after.map { it.state },
        )
        // The BEFORE rect starts at the origin, the AFTER rect at the middle of the frame.
        assertEquals(0f, before.first().bounds.left.value)
        assertEquals(540f, after.first().bounds.left.value)
    }

    @Test
    fun verifyMarksNumberTheBadgesFromOne() {
        val rows = listOf(
            row(index = 0, outcome = VerificationOutcome.Fixed),
            row(index = 1, outcome = VerificationOutcome.StillOff),
        )
        val geometry = FrameGeometry(Size(1080f, 2400f), DEVICE_SIZE, density = 1f)

        assertEquals(
            listOf("1", "2"),
            verifyMarks(geometry, rows, VerifySide.After).map { it.badge },
        )
    }

    @Test
    fun verifyStepsMarkThePhaseBusyAndEverythingBeforeItDone() {
        val steps = verifySteps(VerifyPhase.Diffing, "load", "recapture", "diff")

        assertEquals(
            listOf(StatusStepState.Done, StatusStepState.Done, StatusStepState.Busy),
            steps.map { it.state },
        )
        assertEquals(listOf("load", "recapture", "diff"), steps.map { it.label })
    }

    @Test
    fun verifyStepsStartWithOnlyTheFirstStepBusy() {
        assertEquals(
            listOf(StatusStepState.Busy, StatusStepState.Pending, StatusStepState.Pending),
            verifySteps(VerifyPhase.Loading, "load", "recapture", "diff").map { it.state },
        )
    }

    @Test
    fun verifyStepsAreAllDoneOnceTheRunIsReady() {
        assertTrue(
            verifySteps(VerifyPhase.Ready, "load", "recapture", "diff")
                .all { it.state == StatusStepState.Done },
        )
    }

    @Test
    fun everyVerifyErrorHasItsOwnTitleAndBody() {
        val titles = VerifyError.entries.map { verifyErrorTitleKey(it) }
        val bodies = VerifyError.entries.map { verifyErrorBodyKey(it) }

        assertEquals(VerifyError.entries.size, titles.toSet().size)
        assertEquals(VerifyError.entries.size, bodies.toSet().size)
        assertTrue(titles.none { it in bodies })
    }
}
