package presentation.feature.mirror.core.payload

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import presentation.feature.mirror.source.mirror.FrozenFrame

/**
 * Whether the pinned frame has anything to preview as a payload.
 *
 * Named and lifted out of the composable so AC-77 — "zero staged annotations renders only the
 * empty-state copy" — can be asserted by a unit test. The condition is two clauses and would be
 * trivial to inline, but inlined it sat inside `MirrorPayloadTab`'s body where this project has
 * no way to reach it: there is no Compose UI-test harness (see `.claude/rules/testing.md`), so a
 * gate expressed only as an `if` in a `@Composable` is provable by nothing but eye. A test that
 * re-wrote the same boolean would have asserted against itself and proved nothing either.
 *
 * Both clauses are load-bearing and neither implies the other. `frozen == null` is the live
 * stream — nothing is pinned, so there is no frame to describe. A non-null frame with an empty
 * annotation list is the moment after `ClearStaged`: the frame is still pinned, but a payload
 * with no annotations would carry a prompt asking to fix nothing, crops of nothing, and a
 * `0 crops` footer.
 *
 * Carries a `contract` so a `true` result smart-casts [frozen] to non-null at the call site, the
 * same way `isNullOrEmpty` does in the stdlib. Without it, lifting the condition out of the
 * composable would cost every later `frozen.` access a `!!` — trading one untestable `if` for a
 * scattering of non-null assertions is not a trade worth making.
 *
 * @param frozen The pinned frame, or `null` while the stream is live.
 * @return `true` when at least one annotation is staged on a pinned frame.
 */
@OptIn(ExperimentalContracts::class)
internal fun hasPayload(frozen: FrozenFrame?): Boolean {
    contract { returns(true) implies (frozen != null) }
    return frozen != null && frozen.annotations.isNotEmpty()
}
