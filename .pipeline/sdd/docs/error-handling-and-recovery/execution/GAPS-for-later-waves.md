# Two gaps raised in wave 2 that later waves must not paper over

## 1. T-11's pre-fix RED run for T2 is UNRECOVERABLE — do not claim otherwise

T-11's TAC-1 requires T2 (`reconnect preserves annotations`) to be demonstrated
failing against **pre-fix** code, via the only route that existed before the fix
(`ResumeLive` while `Lost`), and recorded before T-10 lands.

That ordering was broken by circumstance, not by choice. The first T-10/T-12 agent
was killed mid-flight by an API session limit, and when the finishing agent arrived
`reconnect()` was **already in the tree**. The pre-fix state no longer existed to run
against, and reconstructing it by reverting would have required `git` writes into a
tree five sibling agents were writing to — a race I had explicitly forbidden.

What exists instead, and it is strong evidence but not the same evidence: mutation M8
routes `Reconnect` through `resumeLive()` and produces the identical failure shape the
pre-fix code would have produced —

```
expected: frame=pinned, annotations=[2 staged], session=Streaming, stage=Frozen
but was:  frame=null,   annotations=null,       session=Lost,      stage=NoSignal
```

That is the bug reproduced on demand, permanently, as part of the suite. A one-off
pre-fix run would have been a transcript; this is a regression guard.

**T-11 must state plainly that the pre-fix red was not captured, and why.** It must
not describe M8 as if it were the pre-fix run. This chain has already shipped one
vacuous test and the whole spec is built around not overstating what a test proves —
overstating the provenance of a red run would be the same failure in a new place.

## 2. AC-28's multi-device card may be unreachable in practice

Raised by the same agent while testing the projection.

`applyDiscovery(Found)` **never closes the picker**, and `errorCard()` deliberately
yields to an open picker (its TAC-1). So on first discovery the picker wins and the
multi-device card never renders. The agent judged this "correct per spec" — each half
is — but the composition means **a user may never be able to see the card AC-28
describes**.

This needs resolving in T-14 (the NoSignal-branch routing), not assumed away:

- If the picker is the intended multi-device affordance on first discovery, then
  AC-28's card is for the *later* case (a second device arrives mid-session), and
  T-14 must say so explicitly and prove the card is reachable in that path.
- If AC-28's card is meant to replace the picker for the multi-device case, then
  something must close the picker on `Found`, and that is a behaviour change with its
  own blast radius against F-2's confirmed picker ACs.

Either way T-14 must not ship a card no code path can display. A rendering task whose
output is unreachable is a decorative button with extra steps — the same defect class
FR-37 exists to forbid, relocated from the action to the surface.

Note the agent could not resolve this itself because it owned the projection, not the
routing. That is correct scope discipline; the question is simply now T-14's.
