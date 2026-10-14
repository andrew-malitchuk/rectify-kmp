package presentation.core.ui.kit.shell

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import presentation.core.ui.source.kit.organism.shell.ShellWindowDefaults

/** AC-34 / FR-20 / FR-21: the window and panel geometry numbers, as a pass-or-fail check. */
class ShellWindowDefaultsTest {

    @Test
    fun `initial window size is 1240 by 820`() {
        assertEquals(DpSize(1240.dp, 820.dp), ShellWindowDefaults.InitialSize)
    }

    @Test
    fun `minimum window size is 1024 by 700`() {
        assertEquals(DpSize(1024.dp, 700.dp), ShellWindowDefaults.MinimumSize)
    }

    @Test
    fun `panel widths are 260 default 320 max 480`() {
        assertEquals(260.dp, ShellWindowDefaults.PanelMinWidth)
        assertEquals(320.dp, ShellWindowDefaults.PanelDefaultWidth)
        assertEquals(480.dp, ShellWindowDefaults.PanelMaxWidth)
    }

    @Test
    fun `traffic light inset is 78`() {
        assertEquals(78.dp, ShellWindowDefaults.TrafficLightInset)
    }

    @Test
    fun `the minimum window fits the widest panel and the stage`() {
        assertTrue(ShellWindowDefaults.MinimumSize.width > ShellWindowDefaults.PanelMaxWidth * 2)
        assertTrue(ShellWindowDefaults.MinimumSize.width <= ShellWindowDefaults.InitialSize.width)
        assertTrue(ShellWindowDefaults.MinimumSize.height <= ShellWindowDefaults.InitialSize.height)
    }
}
