# T-16: what is proven, and what is only preview-covered

T-16 made three changes. **One is test-proven. Two are not**, and this file exists so
Stage 5 does not read "T-16 complete" as "T-16 verified".

## Proven by mutation

**The scrim's input.** `ScrimGateTest` asserts `overlayMarks` yields nothing for a
frozen frame with no annotations and no drag, and yields a mark once either exists.

Mutation M2 — make the drag branch fire unconditionally, so an empty frame emits a
mark:

```
ScrimGateTest > aFrozenFrameWithNothingAnnotatedProducesNoMarksAndSoIsNotDimmed FAILED
ScrimGateTest > oneStagedAnnotationIsEnoughToDim FAILED
```

Reverted byte-exact, green. The test asserts against `overlayMarks` rather than
against a copy of the predicate on purpose: restating
`frozen.annotations.isNotEmpty() || dragRect != null` in the test would be a second
definition of the same rule, free to drift from the one the composable reads.

## NOT proven — compiles and the suite stays green

Two mutations were applied and **both passed**:

| mutation | result |
|---|---|
| `dimmed = frozen != null && marks.isNotEmpty()` → `dimmed = frozen != null` (restores the full-frame dim) | **green, no test fails** |
| `if (canResumeLive)` → `if (true)` (restores the NFR-6 violation) | **green, no test fails** |

Both live inside a `@Composable`. There is no Compose UI-test harness in this
repository, and this spec deliberately introduces none — adding one is a real
infrastructure decision, not a casual addition (`.claude/rules/testing.md`).

So the second mutation is the serious one: **the NFR-6 fix itself is not
regression-guarded.** A future edit can reinstate the exact violation this spec
exists to remove, and every one of the 225 tests will still pass.

## What does cover them

- `MirrorFrozenStageCrowdedPreview` was deliberately given `canResumeLive = false`
  and a populated `banner`, so the dropped state — banner visible, Resume Live
  withheld — renders in both light and dark previews. That is the repo's only
  rendering check and it is a human looking at a picture.
- `MirrorContractTest.resumeLiveIsNotOfferedWhileTheSessionIsLost` proves the
  *predicate* `canResumeLive` is false while `Lost`. What is unproven is that the
  button actually consults it.

So the chain is: predicate proven, rendering eyeballed, **the wiring between them
unproven**. That is exactly the seam a `-> Unit` intent would hide in, and it is the
same shape as the carry-forward risk recorded for inert intents.

## Recommendation for Stage 5

Record this as an **open gap**, not a waiver — nothing was decided to be acceptable
here, there simply is no harness. Two honest ways to close it, both out of scope for
F-3:

1. Introduce a Compose UI-test harness in its own spec, then assert the two gates.
2. Hoist both gates into the pure projection layer — e.g. have the stage take a
   `resumeLiveVisible: Boolean` and a `dim: Boolean` computed by
   `DiscoveryErrorProjection`, where `commonTest` can reach them. This is the
   cheaper option and it follows the pattern the rest of this spec already uses
   (`errorCard`, `dropBanner`, `canResumeLive` are all pure for exactly this
   reason). It would make the NFR-6 gate assertable without any new infrastructure.

Option 2 is the better change and should be proposed in reflection.
