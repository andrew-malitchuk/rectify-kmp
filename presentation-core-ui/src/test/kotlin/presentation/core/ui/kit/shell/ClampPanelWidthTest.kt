package presentation.core.ui.kit.shell

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import presentation.core.ui.source.kit.organism.shell.ShellWindowDefaults
import presentation.core.ui.source.kit.organism.shell.clampPanelWidth

/** AC-38: the side-panel width is coerced into [260dp, 480dp]. */
class ClampPanelWidthTest {

    @Test
    fun `below the minimum clamps up`() {
        assertEquals(ShellWindowDefaults.PanelMinWidth, clampPanelWidth(200.dp))
        assertEquals(ShellWindowDefaults.PanelMinWidth, clampPanelWidth(0.dp))
        assertEquals(ShellWindowDefaults.PanelMinWidth, clampPanelWidth((-120).dp))
    }

    @Test
    fun `above the maximum clamps down`() {
        assertEquals(ShellWindowDefaults.PanelMaxWidth, clampPanelWidth(600.dp))
        assertEquals(ShellWindowDefaults.PanelMaxWidth, clampPanelWidth(10_000.dp))
    }

    @Test
    fun `a value inside the range is returned unchanged`() {
        assertEquals(320.dp, clampPanelWidth(320.dp))
        assertEquals(261.dp, clampPanelWidth(261.dp))
        assertEquals(479.dp, clampPanelWidth(479.dp))
    }

    @Test
    fun `the exact bounds are inclusive and returned unchanged`() {
        assertEquals(260.dp, clampPanelWidth(260.dp))
        assertEquals(480.dp, clampPanelWidth(480.dp))
    }
}
