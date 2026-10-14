package presentation.core.ui.source.kit.organism.shell

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Window and side-panel geometry for the desktop shell (FR-20, FR-21).
 *
 * Lives in `presentation-core-ui` rather than next to the desktop `Window` call on purpose:
 * only the library/feature convention plugins wire `kotlin.test`, so this is the nearest module
 * where these numbers can be asserted at all.
 */
public object ShellWindowDefaults {

    /** Window size on first launch. */
    public val InitialSize: DpSize = DpSize(1240.dp, 820.dp)

    /** Smallest size the window may be resized to — below this the toolbar truncates. */
    public val MinimumSize: DpSize = DpSize(1024.dp, 700.dp)

    /** Narrowest usable side panel. */
    public val PanelMinWidth: Dp = 260.dp

    /** Widest side panel — beyond this the stage stops being the focus. */
    public val PanelMaxWidth: Dp = 480.dp

    /** Side-panel width before the user has ever dragged the handle. */
    public val PanelDefaultWidth: Dp = 320.dp

    /** Leading toolbar padding that keeps content clear of the macOS traffic lights. */
    public val TrafficLightInset: Dp = 78.dp
}

/**
 * Coerces a side-panel width into `[PanelMinWidth, PanelMaxWidth]` (AC-38).
 *
 * Bounds are inclusive: an exact bound is returned unchanged.
 */
public fun clampPanelWidth(width: Dp): Dp =
    width.coerceIn(ShellWindowDefaults.PanelMinWidth, ShellWindowDefaults.PanelMaxWidth)
