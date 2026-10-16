package presentation.feature.mirror

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.StagedAnnotation
import presentation.feature.mirror.source.mirror.overlayMarks

/** A 1080x2400 device painted 1:1 on a density-1 host, so dp and device px coincide. */
private val GEOMETRY = FrameGeometry(
    framePx = Size(1080f, 2400f),
    deviceSize = IntSize(1080, 2400),
    density = 1f,
)

private val RECT = DeviceRect(left = 40, top = 200, right = 440, bottom = 300)

/**
 * The scrim's gate reads `marks.isNotEmpty()`, so what has to hold is that an empty frozen
 * frame really does produce no marks.
 *
 * The design dims only when something is annotated. Dimming an un-annotated frozen frame hands
 * the user a uniformly darkened screenshot with nothing punched out of it and no indication
 * why — the kind of state that reads as a rendering bug rather than a mode.
 *
 * Asserted against [overlayMarks] rather than against a copy of the predicate: restating
 * `frozen.annotations.isNotEmpty() || dragRect != null` here would be a second definition of
 * the same rule, free to drift from the one the composable actually reads. This tests the
 * input the gate consumes, so the two cannot disagree.
 *
 * The composition itself — the `dimmed` argument reaching `AnnotationOverlay` — is covered by
 * the light/dark `@Preview` pairs. There is no Compose UI-test harness in this repo and this
 * spec deliberately does not introduce one.
 */
class ScrimGateTest {

    @Test
    fun aFrozenFrameWithNothingAnnotatedProducesNoMarksAndSoIsNotDimmed() {
        val empty = FrozenFrame(
            deviceSize = GEOMETRY.deviceSize,
            annotations = emptyList(),
            selectedIndex = null,
        )

        // One classified assert rather than a chain: a chain short-circuits, so a mutation
        // that broke only the drag case would read as having killed both.
        assertEquals(
            expected = listOf("noAnnotations=0", "midDrag=1"),
            actual = listOf(
                "noAnnotations=" + overlayMarks(
                    geometry = GEOMETRY,
                    frozen = empty,
                    dragRect = null,
                ).size,
                // A drag in progress IS something to show, so the scrim is wanted even before
                // the first annotation is committed.
                "midDrag=" + overlayMarks(
                    geometry = GEOMETRY,
                    frozen = empty,
                    dragRect = RECT,
                ).size,
            ),
        )
    }

    @Test
    fun oneStagedAnnotationIsEnoughToDim() {
        val staged = FrozenFrame(
            deviceSize = GEOMETRY.deviceSize,
            annotations = listOf(StagedAnnotation(index = 1, rect = RECT, comment = "")),
            selectedIndex = null,
        )

        assertEquals(1, overlayMarks(geometry = GEOMETRY, frozen = staged, dragRect = null).size)
    }
}
