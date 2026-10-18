package presentation.feature.mirror

import presentation.feature.mirror.core.payload.hierarchySourceLabel
import presentation.feature.mirror.source.mirror.HierarchyStatus
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * hierarchySourceLabel (T-5 / AC-15, AC-74, AC-75, AC-80): machine text for the payload
 * preview's hierarchy-source line -- never localised, so every string here is pinned
 * character-exactly, including the middle dot (U+00B7) and the ellipsis (U+2026). Maps the
 * four shipped [HierarchyStatus] values only; no new status, no new dump.
 */
class PayloadLabelsTest {

    // TAC-4: Ready reports the live node count with a middle dot, not three ASCII periods.
    @Test
    fun readyFormatsTheNodeCountWithAMiddleDot() {
        assertEquals("uiautomator · 42 nodes", hierarchySourceLabel(HierarchyStatus.Ready, 42))
    }

    // TAC-4: n = 0 is still a usable (empty) snapshot -- it must not fall through to the
    // no-hierarchy label.
    @Test
    fun readyWithZeroNodesStillReportsTheCountNotTheNoHierarchyLabel() {
        assertEquals("uiautomator · 0 nodes", hierarchySourceLabel(HierarchyStatus.Ready, 0))
    }

    // TAC-4: Dumping reports the ellipsis character, never the node count -- a dump in flight
    // has no usable count yet.
    @Test
    fun dumpingReportsTheEllipsisRegardlessOfNodeCount() {
        assertEquals("dumping…", hierarchySourceLabel(HierarchyStatus.Dumping, 0))
        assertEquals("dumping…", hierarchySourceLabel(HierarchyStatus.Dumping, 999))
    }

    // TAC-4: Absent and Unavailable are distinct domain states (see HierarchyStatus's own
    // KDoc), but this label deliberately folds both into the same explicit no-hierarchy text --
    // no new status gets invented to tell them apart here.
    @Test
    fun absentReportsTheExplicitNoHierarchyLabel() {
        assertEquals("no hierarchy", hierarchySourceLabel(HierarchyStatus.Absent, 0))
    }

    @Test
    fun unavailableReportsTheSameNoHierarchyLabelAsAbsent() {
        assertEquals(
            hierarchySourceLabel(HierarchyStatus.Absent, 0),
            hierarchySourceLabel(HierarchyStatus.Unavailable, 0),
        )
        assertEquals("no hierarchy", hierarchySourceLabel(HierarchyStatus.Unavailable, 0))
    }

    // Guard against a fifth HierarchyStatus value showing up without this suite being revisited:
    // the mapping above is a closed four-way `when`, not an `else` branch.
    @Test
    fun hierarchyStatusStillHasExactlyFourShippedValues() {
        assertEquals(4, HierarchyStatus.entries.size)
    }
}
