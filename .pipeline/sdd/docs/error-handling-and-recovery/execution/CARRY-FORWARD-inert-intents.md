# Carry-forward: five intents are currently INERT

Raised by the T-9 executor, wave 1. This is a live trap for waves 2+ and must be
closed before the spec can be called done.

## What exists now

`MirrorContract.kt` declares five new intents, and `MirrorViewModel.handleIntent`
has a branch for each — all of them `-> Unit`:

```kotlin
// Listed, not `else`-swallowed: the handlers land with the surfaces that dispatch
// them, and an `else` here would silently absorb the next intent added too.
MirrorIntent.Reconnect -> Unit
MirrorIntent.RetryDiscovery -> Unit
MirrorIntent.RestartAdb -> Unit
MirrorIntent.LocateAdb -> Unit
is MirrorIntent.AdbPathChosen -> Unit
```

Those branches were **compile-forced**, not optional: Kotlin raises
`NO_ELSE_IN_WHEN` for a non-exhaustive `when` over a sealed interface, so the
module would not build and no test could run without them. Listing them
explicitly rather than using `else` is deliberate — an `else` would silently
absorb the *next* intent added too, which is the same class of defect as the
reworded-log-literal that `TIMEOUT_REASON_PREFIX` exists to prevent.

## Why this is dangerous

A later task can wire a button to one of these intents, see it compile, see every
test stay green, and ship a **button that does nothing**. That is precisely the
defect the Stage-2 judge penalised candidate-a for ("mints `Retry`/`RestartAdb`
but never specifies or tests either handler"), and plan.json's FR-37 exists
specifically to forbid it: *every error card's recovery action performs real work
rather than waiting out the 2 s poll*.

A `-> Unit` branch is invisible at the call site. Nothing fails. The user presses
Reconnect and the app shrugs.

## Required before done — each of these must be replaced by a real handler

| intent | owning requirement | dispatched from |
|---|---|---|
| `Reconnect` | FR-35 | the stream-drop banner (AC-29) |
| `RetryDiscovery` | FR-37 | AC-26 secondary, AC-53 primary |
| `RestartAdb` | FR-37 | AC-27 primary — must really kill+start, not re-poll |
| `LocateAdb` | AC-26 primary | opens the file dialog via `MirrorSideEffect.LocateAdb` |
| `AdbPathChosen` | AC-26 | the dialog's result, writes `pref_adb_path` |

## Verification that cannot be skipped

Grep for `-> Unit` in `handleIntent` as an exit check:

```shell
grep -n '\-> Unit' presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
```

**This must print nothing when the spec is complete.** Any surviving line is an
inert intent, which means a dead button, regardless of what the test suite says.

Add this grep to the Stage 5 validation checklist — it is a one-line structural
check that a green suite demonstrably cannot catch.

## Second flag from the same executor

TAC-3's `T4` test (`resumeLiveIsNotOfferedWhileTheSessionIsLost`) is formally
owned by test task **T-11**, but T-9 already wrote it in order to prove its own
mutation. T-11 must **extend** rather than re-add it, or the suite gains a
duplicate under two names and the mutation record becomes ambiguous.

---

# Carry-forward 2: three test fixtures still hand-type the timeout string

Raised by the T-2/T-3 executor. T-3's definition of done says *exactly one literal
definition of the timeout message text exists in the codebase*. Production-side
that now holds — `ProcessRunner` builds its reason from
`DeviceCommandException.TIMEOUT_REASON_PREFIX`. But three pre-existing **test
fixtures** still type the string by hand:

- `data-repository-impl/src/commonTest/.../DeviceDiscoveryPollingTest.kt:190`
- `data-repository-impl/src/commonTest/.../DeviceDiscoveryPollingTest.kt:286`
- `presentation-feature-mirror/src/commonTest/.../MirrorViewModelDiscoveryTest.kt:172`

They were left alone deliberately: the first file is owned by T-5/T-15/T-16 in a
later wave, and `presentation-feature-mirror` had an agent writing in it at the time.

**T-5 must rewrite all three to `"${TIMEOUT_REASON_PREFIX}3000ms"`.** Otherwise a
fixture can keep claiming to be a timeout after the prefix changes — the fixture
would still say "timed out after 3000ms", `isTimeout()` would return false for it,
and the test would go on passing against a ladder that no longer fires. That is the
same silent-disarm the shared constant exists to prevent, just relocated into the
test suite where it is harder to see.

---

# Correction to a MUTATION clause I wrote: it under-kills

The T-2/T-3 executor proved the prescribed mutation for T1b is weaker than stated,
and this correction belongs with the test rather than being lost in a report.

The clause says: *change `isTimeout()`'s body to `exitCode == null` — the
non-zero-exit AND could-not-start assertions must fail.*

Only the could-not-start assertion fails. In the non-zero-exit case `exitCode` is
`1`, so a bare null check already answers `false` correctly:

```
expected:<[timedOut=true, nonZeroExit=false, couldNotStart=false]>
 but was:<[timedOut=true, nonZeroExit=false, couldNotStart=true]>
```

The executor ran a **second** mutation (`isTimeout()` → `true`) to show the
non-zero-exit assertion is load-bearing rather than decoration:

```
expected:<[timedOut=true, nonZeroExit=false, couldNotStart=false]>
 but was:<[timedOut=true, nonZeroExit=true,  couldNotStart=true]>
```

Both assertions are live; **no single mutation covers both**, so the clause needed
two. The verbatim clause plus this correction are recorded in the test's own KDoc.

It also restructured the test to classify all three cases first and assert once over
a list, instead of chaining `assertTrue`/`assertFalse`. A chain short-circuits — the
first failure hides the rest, which is precisely how a half-proven mutation looks
fully proven. Worth copying as a pattern wherever a mutation is supposed to kill more
than one assertion.
