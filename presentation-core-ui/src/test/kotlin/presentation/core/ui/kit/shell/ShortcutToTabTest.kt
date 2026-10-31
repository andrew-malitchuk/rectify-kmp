package presentation.core.ui.kit.shell

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import presentation.core.ui.source.kit.organism.shell.ShellTab
import presentation.core.ui.source.kit.organism.shell.shortcutToTab

/** AC-33: Cmd+1/2/3 select the three tabs; nothing else maps. */
class ShortcutToTabTest {

    @Test
    fun `meta plus one two three map to the three tabs in order`() {
        assertEquals(ShellTab.Notes, shortcutToTab(isMeta = true, key = Key.One))
        assertEquals(ShellTab.Payload, shortcutToTab(isMeta = true, key = Key.Two))
        assertEquals(ShellTab.History, shortcutToTab(isMeta = true, key = Key.Three))
    }

    @Test
    fun `digit without the meta modifier maps to nothing`() {
        assertNull(shortcutToTab(isMeta = false, key = Key.One))
        assertNull(shortcutToTab(isMeta = false, key = Key.Two))
        assertNull(shortcutToTab(isMeta = false, key = Key.Three))
    }

    @Test
    fun `meta plus an unmapped key maps to nothing`() {
        assertNull(shortcutToTab(isMeta = true, key = Key.Four))
        assertNull(shortcutToTab(isMeta = true, key = Key.Zero))
        assertNull(shortcutToTab(isMeta = true, key = Key.A))
        assertNull(shortcutToTab(isMeta = true, key = Key.Enter))
    }
}
