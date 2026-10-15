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
    public val PanelDefaultWidth: Dp = 300.dp

    /**
     * Leading toolbar padding that keeps content clear of the macOS traffic lights.
     *
     * Measured from the design comp rather than guessed: macOS puts the green light's right
     * edge at 68dp, and the comp starts the first control at 102dp — a 34dp breathing gap.
     * The segmented group contributes ~3dp of its own leading padding, so the inset itself
     * is 99dp. At the previous 78dp the first control sat at 81dp, only 13dp clear of the
     * lights and 21dp left of the comp.
     */
    public val TrafficLightInset: Dp = 99.dp
}

/**
 * Coerces a side-panel width into `[PanelMinWidth, PanelMaxWidth]` (AC-38).
 *
 * Bounds are inclusive: an exact bound is returned unchanged.
 */
public fun clampPanelWidth(width: Dp): Dp =
    width.coerceIn(ShellWindowDefaults.PanelMinWidth, ShellWindowDefaults.PanelMaxWidth)
