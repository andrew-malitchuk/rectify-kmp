package presentation.core.ui.source.kit.organism.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp

/**
 * Hoisted state of the window shell: which side-panel tab is active and how wide the panel is.
 *
 * Plain Compose state holder — no MVI, no ViewModel: the shell is a layout organism, and the
 * two values it owns have different lifetimes (the tab is per-session, the width is a
 * persisted preference, AC-32 vs AC-39). Whether a mirroring session exists is deliberately
 * *not* modelled here; the owner calls [onSessionStarted] when it starts one.
 *
 * @param initialPanelWidth Starting panel width; clamped by [clampPanelWidth].
 */
@Stable
public class ShellState(initialPanelWidth: Dp = ShellWindowDefaults.PanelDefaultWidth) {

    /** Tab currently shown in the side panel. */
    public var activeTab: ShellTab by mutableStateOf(ShellTab.Notes)
        private set

    private var currentPanelWidth: Dp by mutableStateOf(clampPanelWidth(initialPanelWidth))

    /**
     * Current side-panel width, always within the [ShellWindowDefaults] bounds.
     *
     * Read-only property over a private backing state rather than `private set`: the generated
     * setter would clash with [setPanelWidth] on the JVM.
     */
    public val panelWidth: Dp get() = currentPanelWidth

    /** Panel width when the in-flight resize drag started; `null` while no drag is in flight. */
    private var dragStartWidth: Dp? = null

    /** Selects [tab]. */
    public fun selectTab(tab: ShellTab) {
        activeTab = tab
    }

    /** Sets the panel width, clamped. */
    public fun setPanelWidth(width: Dp) {
        currentPanelWidth = clampPanelWidth(width)
    }

    /** Widens (positive) or narrows (negative) the panel by [delta], clamped. */
    public fun resizePanelBy(delta: Dp) {
        setPanelWidth(panelWidth + delta)
    }

    /** Marks the start of a resize drag, so [onDragStopped] can tell whether it changed anything. */
    public fun onDragStarted() {
        dragStartWidth = panelWidth
    }

    /**
     * Ends a resize drag.
     *
     * @return The new width when the drag actually changed it — the caller persists that — or
     *   `null` when the width ended up where it started (including a drag that only pushed
     *   against a clamped bound), so no write is issued.
     */
    public fun onDragStopped(): Dp? {
        val startWidth = dragStartWidth
        dragStartWidth = null
        return panelWidth.takeIf { startWidth != null && it != startWidth }
    }

    /** Resets the panel to its default tab when a session starts; the width is untouched. */
    public fun onSessionStarted() {
        activeTab = ShellTab.Notes
    }
}

/**
 * Remembers a [ShellState] for the lifetime of the composition.
 *
 * [initialPanelWidth] is read once; a width that arrives later (from the persisted preference)
 * is applied through [ShellState.setPanelWidth] rather than by re-keying, which would also
 * drop the active tab.
 */
@Composable
public fun rememberShellState(
    initialPanelWidth: Dp = ShellWindowDefaults.PanelDefaultWidth,
): ShellState = remember { ShellState(initialPanelWidth) }
