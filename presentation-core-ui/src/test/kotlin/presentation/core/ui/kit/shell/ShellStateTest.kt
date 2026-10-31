package presentation.core.ui.kit.shell

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import presentation.core.ui.source.kit.organism.shell.ShellState
import presentation.core.ui.source.kit.organism.shell.ShellTab
import presentation.core.ui.source.kit.organism.shell.ShellWindowDefaults

/** AC-32 / AC-38 / AC-39: the shell's hoisted state, with no Compose harness. */
class ShellStateTest {

    private fun state(initialPanelWidth: Int = 320): ShellState =
        ShellState(initialPanelWidth = initialPanelWidth.dp)

    @Test
    fun `active tab defaults to Notes`() {
        assertEquals(ShellTab.Notes, state().activeTab)
    }

    @Test
    fun `selectTab changes the active tab`() {
        val shell = state()
        shell.selectTab(ShellTab.History)
        assertEquals(ShellTab.History, shell.activeTab)
    }

    @Test
    fun `onSessionStarted resets the tab but leaves the panel width alone`() {
        val shell = state()
        shell.selectTab(ShellTab.Payload)
        shell.setPanelWidth(400.dp)
        shell.onSessionStarted()
        assertEquals(ShellTab.Notes, shell.activeTab)
        assertEquals(400.dp, shell.panelWidth)
    }

    @Test
    fun `a constructor width outside the bounds is clamped`() {
        assertEquals(ShellWindowDefaults.PanelMinWidth, state(initialPanelWidth = 100).panelWidth)
        assertEquals(ShellWindowDefaults.PanelMaxWidth, state(initialPanelWidth = 900).panelWidth)
        assertEquals(320.dp, state().panelWidth)
    }

    @Test
    fun `setPanelWidth clamps`() {
        val shell = state()
        shell.setPanelWidth(10.dp)
        assertEquals(ShellWindowDefaults.PanelMinWidth, shell.panelWidth)
        shell.setPanelWidth(10_000.dp)
        assertEquals(ShellWindowDefaults.PanelMaxWidth, shell.panelWidth)
        shell.setPanelWidth(333.dp)
        assertEquals(333.dp, shell.panelWidth)
    }

    @Test
    fun `resizePanelBy accumulates and clamps at both ends`() {
        val shell = state()
        shell.resizePanelBy(40.dp)
        assertEquals(360.dp, shell.panelWidth)
        shell.resizePanelBy((-40).dp)
        assertEquals(320.dp, shell.panelWidth)
        shell.resizePanelBy(1_000.dp)
        assertEquals(ShellWindowDefaults.PanelMaxWidth, shell.panelWidth)
        shell.resizePanelBy((-1_000.dp))
        assertEquals(ShellWindowDefaults.PanelMinWidth, shell.panelWidth)
    }

    @Test
    fun `onDragStopped returns the new width only when the drag changed it`() {
        val shell = state()
        shell.onDragStarted()
        shell.resizePanelBy(60.dp)
        assertEquals(380.dp, shell.onDragStopped())
    }

    @Test
    fun `onDragStopped returns null when the drag was a no-op`() {
        val shell = state()
        shell.onDragStarted()
        assertNull(shell.onDragStopped())
    }

    @Test
    fun `onDragStopped returns null when the drag netted out to the original width`() {
        val shell = state()
        shell.onDragStarted()
        shell.resizePanelBy(50.dp)
        shell.resizePanelBy((-50).dp)
        assertNull(shell.onDragStopped())
    }

    @Test
    fun `onDragStopped returns null when the drag only pushed against a clamped bound`() {
        val shell = state(initialPanelWidth = 480)
        shell.onDragStarted()
        shell.resizePanelBy(200.dp)
        assertNull(shell.onDragStopped())
    }

    @Test
    fun `a width change outside a drag is not reported by the next onDragStopped`() {
        val shell = state()
        shell.setPanelWidth(400.dp)
        shell.onDragStarted()
        assertNull(shell.onDragStopped())
    }
}
