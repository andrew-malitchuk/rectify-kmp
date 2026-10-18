package presentation.feature.mirror.core.send

import presentation.feature.mirror.source.mirror.FrozenFrame

/**
 * STUB owned by T-8 (wave 2) — see plan.json's "MirrorContract additions" entry. The real
 * `presentation.feature.mirror.source.mirror.MirrorUiState` does not yet carry a
 * `send: SendPhase` field; until T-8 lands it there, this is a minimal stand-in carrying the
 * same `frozen`/`stagedCount` shape (copied verbatim from the real class's own definitions —
 * `stagedCount` is `frozen?.annotations?.size ?: 0` there too) plus the one extra field [canSend]
 * needs.
 *
 * T-8 should delete this type and repoint [canSend]'s parameter at the real `MirrorUiState` once
 * `send` exists there — [canSend]'s body does not need to change, since it already reads exactly
 * `frozen`, `stagedCount` and `send`.
 */
internal data class MirrorUiState(
    val frozen: FrozenFrame? = null,
    val send: SendPhase = SendPhase.Idle,
) {
    /** Mirrors the real `MirrorUiState.stagedCount` getter. */
    val stagedCount: Int get() = frozen?.annotations?.size ?: 0
}

/**
 * Whether `MirrorIntent.ResolveAndSend` may be dispatched right now.
 *
 * Read by three independent consumers — the composable (to enable/disable the button), the
 * reducer (the guard a double-dispatch cannot bypass), and the hoisted ⌘↩ shortcut holder — all
 * three evaluating the exact same predicate, which is the whole reason this is one function
 * rather than three copies of the same three-clause `&&`.
 *
 * **Deliberately does NOT read [MirrorUiState.send]'s session** — there is no session parameter
 * at all. A dropped stream (SC-27) does not disable sending: the held frame bytes and the staged
 * annotations do not depend on the transport staying up, so Resolve & Send stays available while
 * [presentation.feature.mirror.source.mirror.MirrorSession.Lost] holds. This is also what closes
 * `features.json#error-handling-and-recovery`'s deferred AC-29 clause. The only in-flight guard
 * is [SendPhase.isInFlight], which blocks a second concurrent send.
 *
 * @param state Reduced mirror state (see [MirrorUiState] stub note above).
 * @return `true` only when a frame is pinned, at least one annotation is staged, and no send is
 *   already running.
 */
internal fun canSend(state: MirrorUiState): Boolean =
    state.frozen != null && state.stagedCount > 0 && !state.send.isInFlight
