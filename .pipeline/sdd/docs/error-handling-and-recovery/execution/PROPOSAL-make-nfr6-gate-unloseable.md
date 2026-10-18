# Proposal: make the NFR-6 gate impossible to remove silently

**Not applied in F-3.** The Stage 5 verdict is being computed against the current
tree; changing it underneath would invalidate the audit the verdict rests on. This
is written for reflection to act on.

## The problem

The single most serious open item in F-3: **the fix itself is unguarded.**

```
if (canResumeLive) { AppButton(text = "Resume live", …) }   // MirrorAnnotationStage.kt:321
dimmed = frozen != null && marks.isNotEmpty()               // :286
```

Mutate either back to its pre-fix form — `if (true)`, `frozen != null` — and all
**235 tests stay green**. Both live inside a `@Composable`, and this repo has no
Compose UI-test harness (and F-3 deliberately did not introduce one).

So a future edit can reinstate the exact NFR-6 violation this spec exists to remove,
and nothing fails. The chain is: predicate proven (`canResumeLive` has its own test),
rendering eyeballed (light/dark previews), **wiring between them unproven**.

## Why the obvious fixes do not work

- **Hoisting the gate into the projection layer** — already done, and it is not
  enough. `MirrorUiState.canResumeLive` IS a tested pure projection. The gap is not
  the value, it is whether the composable *consults* it, which is inherently a
  render question.
- **A test that greps the source** — brittle, and already proven defeatable: the
  `-> Unit` exit check printed nothing while a dead button existed, because the
  no-op had moved from a `when` branch into a function body. A textual check loses
  to a refactor.
- **A UI-test harness** — the real answer, and a genuine infrastructure decision
  that belongs in its own spec, not smuggled in here.

## The proposal: delete the boolean

Replace the `canResumeLive: Boolean` parameter with a **closed sealed slot**:

```kotlin
internal sealed interface FrozenAffordance {
    /** The session is healthy; the user can discard the pin and go back to live. */
    data object ResumeLive : FrozenAffordance
    /** The device dropped; the only offer is to get it back, never to discard. */
    data class Reconnect(val annotationCount: Int) : FrozenAffordance
}

internal fun MirrorUiState.frozenAffordance(): FrozenAffordance? = when {
    frozen == null -> null
    session is MirrorSession.Lost -> FrozenAffordance.Reconnect(frozen.annotations.size)
    else -> FrozenAffordance.ResumeLive
}
```

The stage takes `affordance: FrozenAffordance?` and renders it with an exhaustive
`when`. Three properties follow, none of which needs a harness:

1. **The two states become mutually exclusive by type.** Showing Resume Live while
   `Lost` is no longer a wrong boolean — it is unrepresentable.
2. **Dropping a branch is a compile error.** `when` over a sealed interface is
   exhaustive, the same mechanism that made adding `HostCommand.StartServer` safe
   and that forced the five intents to be listed rather than `else`-swallowed.
3. **The decision is a pure function**, so `commonTest` asserts it directly and a
   mutation on it goes red — unlike today's `if (canResumeLive)`.

The same shape handles the scrim: fold `dim: Boolean` into the affordance, computed
from the mark count, so "frozen with nothing annotated" is its own case rather than
a conjunction a future edit can simplify away.

## Cost and risk

Small: one sealed interface, one projection function, one `when` in the stage, and
the `banner` parameter folds into `Reconnect` (it already carries the same count).
Deletes a parameter rather than adding one.

The risk is that `MirrorFrozenStage` is also called by F-4's previews, so the
signature change touches files outside F-3's declared scope — which is exactly why
this is a reflection item and not a last-minute edit.

## Why it is worth doing

This is the third defect in this chain whose root cause was *a correct value that
nothing forced the consumer to use*: the vacuous session-loss test, the
`TIMEOUT_REASON_PREFIX` the consumer could stop matching, and now this. The
constant fixed its case by binding producer and consumer to one symbol. This does
the same thing for an affordance: makes the mistake a compile error instead of a
silent regression.
