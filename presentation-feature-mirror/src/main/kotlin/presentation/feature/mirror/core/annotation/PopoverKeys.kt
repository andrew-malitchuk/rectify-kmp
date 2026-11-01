package presentation.feature.mirror.core.annotation

import androidx.compose.ui.input.key.Key

/**
 * Whether a key press inside the open comment popover should close it (A11Y-2 / AC-50).
 *
 * `↵` and `esc` both resolve to the same answer, and that is not an oversight: the comment is
 * written through `CommentAnnotation` on every keystroke, so by the time either key arrives
 * there is nothing left to commit. `↵` means "done typing" and `esc` means "dismiss"; both
 * leave the annotation pinned and the staged count untouched.
 *
 * [isMeta] is the guard that keeps this away from the window's own shortcuts: `Cmd+1/2/3`
 * (`shortcutToTab`) and the reserved `Cmd+↵` must reach the window, which dispatches them
 * before the focused field ever sees them. Pure, so the disjointness is checked by a test
 * rather than by a rendered window.
 *
 * The picker's `pickerKeyAction` claims the same two keys, but `MirrorStage.Picker` and
 * `MirrorStage.Frozen` are branches of one `when` — the two surfaces are never composed at the
 * same time, so neither can steal the other's key.
 *
 * @param isMeta Whether the platform meta modifier was held.
 * @param key The pressed key.
 * @return True when the popover should close and the event must be consumed.
 */
internal fun closesPopover(isMeta: Boolean, key: Key): Boolean = when {
    isMeta -> false
    else -> key == Key.Enter || key == Key.NumPadEnter || key == Key.Escape
}
