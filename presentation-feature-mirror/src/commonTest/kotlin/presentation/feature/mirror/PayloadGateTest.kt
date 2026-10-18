package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import presentation.feature.mirror.core.payload.HierarchyOutlineDisplay
import presentation.feature.mirror.core.payload.hasPayload
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.core.payload.hierarchyOutlineDisplay
import presentation.feature.mirror.core.payload.promptSeedIsStale
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.StagedAnnotation

/**
 * AC-77: the Payload tab shows only its empty-state copy until something is actually staged.
 *
 * The three states below are the three the tab can be opened in, and they are distinct for
 * different reasons — a live stream, a pinned frame that was just cleared, and a real payload.
 * The middle one is the case an `isEmpty()`-only check would get wrong.
 */
class PayloadGateTest {

    @Test
    fun liveStreamWithNoPinnedFrameHasNoPayload() {
        assertFalse(hasPayload(null))
    }

    @Test
    fun pinnedFrameWithNoStagedAnnotationsHasNoPayload() {
        // The moment after ClearStaged: still frozen, but a payload here would carry a prompt
        // asking to fix nothing and a "0 crops" footer.
        val frozen = FrozenFrame(deviceSize = IntSize(width = 1080, height = 2400))
        assertFalse(hasPayload(frozen))
    }

    @Test
    fun pinnedFrameWithOneStagedAnnotationHasPayload() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(width = 1080, height = 2400),
            annotations = listOf(
                StagedAnnotation(
                    index = 1,
                    rect = DeviceRect(left = 120, top = 420, right = 960, bottom = 674),
                ),
            ),
        )
        assertTrue(hasPayload(frozen))
    }
}

/**
 * AC-80: `Absent`/`Unavailable` renders **zero rows plus an explicit no-hierarchy line**.
 *
 * The reachable state this exists for is the one the AC-80 preview pair cannot reach, because it
 * pins an empty snapshot: `dumpHierarchyFor` publishes the full dump *before* it reduces the
 * verdict, so a frame-size mismatch (the measured 9.76 % uiautomator display-override gap) leaves
 * a several-hundred-node snapshot held while the status reads `Unavailable`. The rows are
 * projected from the snapshot regardless of status, so they exist; the readout says "no
 * hierarchy". Rendering both at once is the defect — a verdict contradicted by the tree printed
 * underneath it.
 *
 * Asserted as literal expectations per state, not against a re-derived boolean: the first
 * reviewed version of this block carried a test that re-wrote the predicate and proved nothing.
 */
class PayloadHierarchyDisplayTest {

    private val dumpedRowCount = 312

    @Test
    fun unavailableWithHeldSnapshotShowsNoHierarchyLineInsteadOfRows() {
        assertEquals(
            HierarchyOutlineDisplay.NoHierarchyLine,
            hierarchyOutlineDisplay(HierarchyStatus.Unavailable, rowCount = dumpedRowCount),
        )
    }

    @Test
    fun absentWithHeldSnapshotShowsNoHierarchyLineInsteadOfRows() {
        assertEquals(
            HierarchyOutlineDisplay.NoHierarchyLine,
            hierarchyOutlineDisplay(HierarchyStatus.Absent, rowCount = dumpedRowCount),
        )
    }

    @Test
    fun unavailableWithNoSnapshotShowsNoHierarchyLine() {
        assertEquals(
            HierarchyOutlineDisplay.NoHierarchyLine,
            hierarchyOutlineDisplay(HierarchyStatus.Unavailable, rowCount = 0),
        )
    }

    @Test
    fun readyDumpShowsRows() {
        assertEquals(
            HierarchyOutlineDisplay.Rows,
            hierarchyOutlineDisplay(HierarchyStatus.Ready, rowCount = dumpedRowCount),
        )
    }

    @Test
    fun readyButEmptyDumpShowsNeitherRowsNorLine() {
        // A usable snapshot that genuinely contains no nodes; the readout says "0 nodes".
        assertEquals(
            HierarchyOutlineDisplay.Nothing,
            hierarchyOutlineDisplay(HierarchyStatus.Ready, rowCount = 0),
        )
    }

    @Test
    fun dumpInFlightShowsNeitherRowsNorLine() {
        assertEquals(
            HierarchyOutlineDisplay.Nothing,
            hierarchyOutlineDisplay(HierarchyStatus.Dumping, rowCount = 0),
        )
    }
}

/**
 * F-1 (RCR-1): an open prompt editor must not commit a seed that describes annotations that are
 * gone.
 *
 * The editor seeds `MultilineInput` once per session — keyed on nothing that moves per keystroke,
 * because `MultilineInput` re-keys its own buffer on `initialText` — and the input emits its
 * whole buffer on the first keystroke. So a stale seed does not merely look wrong, it is
 * committed as a `promptOverride` that never auto-clears, and the export then permanently
 * describes 1 of 2 annotations.
 *
 * Every case below runs the **real** [buildPrompt] over two payload states rather than comparing
 * hand-written strings: the question is whether the machine's output actually moved, and only
 * `buildPrompt` knows that. Re-deriving the comparison here would assert against itself, which is
 * how two of this spec's defects got past review.
 */
class PromptSeedStalenessTest {

    private val deviceSize = IntSize(width = 1080, height = 2400)

    private fun annotation(
        index: Int,
        width: Int = 840,
        height: Int = 254,
        comment: String = "",
    ) = StagedAnnotation(
        index = index,
        rect = DeviceRect(left = 120, top = 420, right = 120 + width, bottom = 420 + height),
        comment = comment,
    )

    /** The prompt the Payload tab would show for this state; no hierarchy, as before a dump. */
    private fun generated(
        annotations: List<StagedAnnotation>,
        override: String? = null,
        selectedIndex: Int? = null,
    ) = buildPrompt(
        FrozenFrame(
            deviceSize = deviceSize,
            annotations = annotations,
            selectedIndex = selectedIndex,
            deviceLabel = "Android 15",
            promptOverride = override,
        ),
    ) { null }

    @Test
    fun stagingASecondAnnotationUnderAnOpenEditorMakesTheSeedStale() {
        val seededAgainst = generated(listOf(annotation(1)))

        assertTrue(
            promptSeedIsStale(
                seededAgainst = seededAgainst,
                generated = generated(listOf(annotation(1), annotation(2))),
            ),
        )
    }

    @Test
    fun deletingTheOtherAnnotationUnderAnOpenEditorMakesTheSeedStale() {
        val seededAgainst = generated(listOf(annotation(1), annotation(2)))

        assertTrue(
            promptSeedIsStale(
                seededAgainst = seededAgainst,
                generated = generated(listOf(annotation(1))),
            ),
        )
    }

    @Test
    fun aDeleteAndAStageThatLeaveTheCountUnchangedAreStillStale() {
        // MUTATION guard: a staleness check written against the annotation count passes every
        // other case here and fails this one. The survivors renumber, so one annotation swapped
        // for another is a different payload at the same count.
        val seededAgainst = generated(listOf(annotation(1)))

        assertTrue(
            promptSeedIsStale(
                seededAgainst = seededAgainst,
                generated = generated(listOf(annotation(index = 1, width = 300, height = 96))),
            ),
        )
    }

    @Test
    fun aCommentTypedOntoAStagedAnnotationMakesTheSeedStale() {
        // The comment is a line of the prompt, so the editor's seed no longer matches what the
        // payload would send even though the annotation set is the same size and the same rects.
        val seededAgainst = generated(listOf(annotation(1)))

        assertTrue(
            promptSeedIsStale(
                seededAgainst = seededAgainst,
                generated = generated(listOf(annotation(index = 1, comment = "contrast too low"))),
            ),
        )
    }

    @Test
    fun typingIntoTheEditorLeavesTheSeedFresh() {
        // T-7 REGRESSION guard, and the reason the comparison is against `generated` and not
        // `effective`: every keystroke round-trips through `promptOverride`, so a check that read
        // the override would fire on the user's own typing — closing the editor mid-word and
        // re-keying the input's buffer, a worse bug than the stale seed.
        val annotations = listOf(annotation(1))
        val seededAgainst = generated(annotations)

        assertFalse(
            promptSeedIsStale(
                seededAgainst = seededAgainst,
                generated = generated(annotations, override = "check the tap target here"),
            ),
        )
    }

    @Test
    fun hoveringOrSelectingAnAnnotationLeavesTheSeedFresh() {
        // The panel recomposes on hover and on selection; neither reaches the prompt, and an
        // editor that closed on a mouse move would be unusable.
        val annotations = listOf(annotation(1), annotation(2))
        val seededAgainst = generated(annotations)

        assertFalse(
            promptSeedIsStale(
                seededAgainst = seededAgainst,
                generated = generated(annotations, selectedIndex = 2),
            ),
        )
    }
}
