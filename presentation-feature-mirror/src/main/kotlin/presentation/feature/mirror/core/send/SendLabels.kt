package presentation.feature.mirror.core.send

/**
 * The completion toast's body text (AC-19, AC-21): `Sent N annotations to Claude Code · frame +
 * hierarchy · 1.4s`, degrading to `· frame ·` when no hierarchy went with the send.
 *
 * Machine text, never localised — same reasoning as `core/payload/PayloadLabels.kt`'s
 * `hierarchySourceLabel` and `buildPrompt`: it is read back off a toast the user screenshots or
 * pastes, so it must read the same on every machine, including the literal middle-dot (U+00B7)
 * separators.
 *
 * Singular/plural follows `buildPrompt`'s own `if (count == 1) "issue" else "issues"` precedent
 * exactly — `"annotation"` for `count == 1`, `"annotations"` otherwise, with no other wording
 * change between the two.
 *
 * [elapsedMs] is floored to one decimal place of seconds (1,449 ms -> `1.4s`, never rounded up to
 * `1.5s`), and the floored value is itself floored at `0.1s`: any elapsed time under 100 ms still
 * renders `0.1s` rather than `0.0s` or a bare `0s` — a send is never reported as having taken no
 * time at all.
 *
 * @param count Annotations included in the send — [presentation.feature.mirror.source.mirror.MirrorUiState.stagedCount]
 *   read **before** the post-send reset (FR-19), so a successful send that just cleared the
 *   batch still reports what was actually sent.
 * @param elapsedMs Wall-clock duration of the send, in milliseconds.
 * @param hierarchyAttached Whether the published envelope really carried the view hierarchy —
 *   `AnnotationEnvelope.hierarchyAvailable`, read off the envelope that was actually published
 *   rather than re-derived. Stage 5 `AF-4`: this used to be hardcoded `frame + hierarchy`, so
 *   every iOS send and every failed dump was reported as having shipped a hierarchy it did not.
 * @return The exact toast body string.
 */
internal fun toastLabel(count: Int, elapsedMs: Long, hierarchyAttached: Boolean): String {
    val noun = if (count == 1) "annotation" else "annotations"
    // Integer tenths, so the floor is the division itself -- no rounding mode to get wrong, and
    // no `String.format` — the manual pad keeps this pure and allocation-free.
    val tenths = (elapsedMs / 100).coerceAtLeast(1)
    val elapsed = "${tenths / 10}.${tenths % 10}s"
    val attached = if (hierarchyAttached) "frame + hierarchy" else "frame"
    return "Sent $count $noun to Claude Code · $attached · $elapsed"
}
