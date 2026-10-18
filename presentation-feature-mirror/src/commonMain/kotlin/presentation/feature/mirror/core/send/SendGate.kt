package presentation.feature.mirror.core.send

import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.SendPhase

/**
 * Whether `MirrorIntent.ResolveAndSend` may be dispatched right now.
 *
 * Read by three independent consumers — the composable (to enable/disable the button), the
 * reducer (the guard a double-dispatch cannot bypass), and the hoisted ⌘↩ shortcut holder — all
 * three evaluating the exact same predicate, which is the whole reason this is one function
 * rather than three copies of the same three-clause `&&`.
 *
 * **Deliberately does NOT read the session** — there is no session parameter at all. A dropped
 * stream (SC-27) does not disable sending: the held frame bytes and the staged annotations do
 * not depend on the transport staying up, so Resolve & Send stays available while
 * [presentation.feature.mirror.source.mirror.MirrorSession.Lost] holds. This is also what closes
 * `features.json#error-handling-and-recovery`'s deferred AC-29 clause. The only in-flight guard
 * is [SendPhase.isInFlight], which blocks a second concurrent send.
 *
 * @param state Reduced mirror state.
 * @return `true` only when a frame is pinned, at least one annotation is staged, and no send is
 *   already running.
 */
internal fun canSend(state: MirrorUiState): Boolean =
    state.frozen != null && state.stagedCount > 0 && !state.send.isInFlight

/**
 * Whether `MirrorIntent.ClearStaged` may be dispatched right now (T-10, TAC-2).
 *
 * Distinct from [canSend]: it does not require [MirrorUiState.frozen] on its own (though in
 * practice [MirrorUiState.stagedCount] already implies a frozen frame — `stagedCount` is derived
 * from `frozen?.annotations?.size`), and its only consumer is the toolbar's `Clear` ghost button,
 * which reads it through [MirrorUiState.canClear]. It still shares [SendPhase.isInFlight] with [canSend] because wiping the
 * staged batch out from under a send already assembling that same batch's envelope is the one
 * thing TAC-2 exists to forbid — "Clear is disabled for the same in-flight window Resolve & Send
 * is".
 *
 * @param state Reduced mirror state.
 * @return `true` only when at least one annotation is staged and no send is already running.
 */
internal fun canClear(state: MirrorUiState): Boolean =
    state.stagedCount > 0 && !state.send.isInFlight
