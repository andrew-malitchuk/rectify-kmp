package presentation.core.ui.kit.shell

import kotlin.test.Test
import kotlin.test.assertEquals
import presentation.core.ui.source.kit.organism.shell.ShellTab

/** AC-12: the side panel offers exactly three tabs, in a fixed order, with fixed labels. */
class ShellTabTest {

    @Test
    fun `has exactly three entries`() {
        assertEquals(3, ShellTab.entries.size)
    }

    @Test
    fun `entries are ordered Notes Payload History`() {
        assertEquals(
            listOf(ShellTab.Notes, ShellTab.Payload, ShellTab.History),
            ShellTab.entries.toList(),
        )
    }

    @Test
    fun `labels are the shipped copy - not SidebarTabs KDoc Annotations`() {
        assertEquals("Notes", ShellTab.Notes.label)
        assertEquals("Payload", ShellTab.Payload.label)
        assertEquals("History", ShellTab.History.label)
    }
}
