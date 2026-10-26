package domain.core.source.model.device

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TAC-2: [HierarchySnapshotModel.Empty] keeps `rawDump == ""` once the field is added.
 *
 * `rawDump` must default to `""` so every existing construction site (including [Empty] and
 * every other fixture across the codebase) keeps compiling untouched — this pins the sentinel
 * half of that contract from the domain side.
 */
class HierarchySnapshotModelTest {

    @Test
    fun `Empty has a blank rawDump`() {
        assertEquals("", HierarchySnapshotModel.Empty.rawDump)
    }
}
