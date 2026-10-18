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
