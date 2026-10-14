package presentation.core.ui.source.kit.organism.shell

import androidx.compose.ui.input.key.Key

/**
 * The three — and only three — tabs of the shell's side panel (FR-11).
 *
 * Declaration order is the rendered order; [label] is the shipped copy. The first tab is also
 * the one the shell falls back to whenever a session starts
 * ([ShellState.onSessionStarted]).
 *
 * @property label Tab copy as rendered in the panel header.
 */
public enum class ShellTab(public val label: String) {
    Notes("Notes"),
    Payload("Payload"),
    History("History"),
}

/**
 * Maps a keyboard shortcut to the tab it selects: `Cmd+1` / `Cmd+2` / `Cmd+3` (FR-16).
 *
 * Pure so the mapping is testable without a Compose key-event harness — the caller feeds it
 * whatever its `onPreviewKeyEvent` saw.
 *
 * @param isMeta Whether the platform meta modifier (Cmd on macOS) was held.
 * @param key The pressed key.
 * @return The tab to select, or `null` when the combination is not a tab shortcut.
 */
public fun shortcutToTab(isMeta: Boolean, key: Key): ShellTab? {
    if (!isMeta) return null
    return when (key) {
        Key.One -> ShellTab.Notes
        Key.Two -> ShellTab.Payload
        Key.Three -> ShellTab.History
        else -> null
    }
}
