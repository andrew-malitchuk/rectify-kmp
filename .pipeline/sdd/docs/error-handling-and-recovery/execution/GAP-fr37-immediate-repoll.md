# FR-37's immediate re-poll: not implemented, and why

FR-37 asks every error-card recovery action to do real work "rather than waiting
out the 2 s poll". **The real-work half is implemented and proven. The
immediacy half is not.**

## What IS implemented and mutation-proven

| action | what it does | mutation |
|---|---|---|
| `RestartAdb` (AC-27) | calls `RestartAdbServerUseCase` — a genuine `kill-server` + `start-server` | drop the call → `restartAdbReallyRestartsTheDaemon` fails |
| `AdbPathChosen` (AC-26) | persists the path via `SetAdbPathUseCase` | drop the call → `aChosenAdbPathIsStored` fails |
| `LocateAdb` (AC-26) | posts `MirrorSideEffect.LocateAdb` so the window layer opens its dialog | — |

None of these is a dead button. `RestartAdb` in particular really restarts the
daemon, which matters more than the timing: `start-server` alone cannot clear a
wedged daemon, so a button that merely re-polled could not fix the state a user
presses it in.

## What is NOT implemented

`RetryDiscovery` does not force a poll to happen *now*. It is `= Unit`.

## Why, and what was actually tried

The intended mechanism was a `discoveryNonce: MutableStateFlow<Int>` multiplexed
into the discovery gate, bumped by each recovery action, so `flatMapLatest`
would cancel the old collection and start a fresh one. Three placements were
tried and measured:

1. `combine(sessionDeviceId, discoveryNonce) { device, _ -> device }` — broke
   `MirrorViewModelSessionTest.aLostDeviceEndsTheSessionAndResumesPolling`.
   `combine` conflates, so a session that starts and dies inside one dispatch
   window collapses to a single emission and the gate never sees the device
   leave. **Polling never resumed.**
2. `discoveryNonce.flatMapLatest { sessionDeviceId.flatMapLatest { … } }` — same
   test, same failure, measured `before=1 after=1`.
3. The nonce nested inside the gate's idle branch, leaving the outer gate
   byte-identical to F-2's. The session test passed, but the nonce bump did not
   reliably resubscribe: the three new tests reported `before=0 after=1`,
   `before=1 after=1` and `before=0 after=1` — **inconsistent between tests in
   the same run**, i.e. a timing-dependent mechanism.

The gate being threaded through is the one F-2 shipped and had **confirmed**. A
half-working restart there would trade proven behaviour for an unproven
convenience, and the flakiness means any test guarding it would be a test that
passes for timing reasons rather than behavioural ones. So the nonce was removed
rather than shipped.

Worth recording: an intermediate version of the retry test asserted only
`subscriptions > before`. Its own mutation run exposed it as **vacuous** — with
the initial subscription landing after the intent, the count rose above zero
whether or not the handler did anything, so a no-op `retryDiscovery()` passed.
That is the third vacuity caught by mutation in this spec, and it was caught in a
test written to guard against exactly this class of defect.

## What the user actually experiences

The repository's own 2 s poll re-polls, and FR-36's escalation ladder runs on
that poll. So recovery does happen without further input — up to two seconds
later instead of at once.

That is a **latency shortfall, not a dead button**, and the distinction is the
one FR-37 exists to enforce: the state moves on its own. A dead button is one
where nothing ever happens.

## Recommendation

Record as an **open gap** for Stage 5, not a waiver — the shortfall was measured,
not accepted as a tradeoff.

The fix belongs in the data layer, not the view model: give `observeDiscovery()`
a `refresh()` trigger inside `DeviceRepositoryImpl`'s own poll loop, where the
2 s `delay` lives. A `select`-style race between the delay and a refresh channel
would cut the wait without touching the presentation gate at all — and the loop
is already covered by virtual-time tests, so it can be proven there rather than
through Orbit's dispatcher timing, which is what made this flaky.
