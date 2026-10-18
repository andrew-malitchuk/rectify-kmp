# FR-37's immediate re-poll — CLOSED

> **STATUS CORRECTION.** This file originally recorded the immediate re-poll as not
> implemented. **It is implemented and mutation-proven** as of commit `34b87d5`.
> The note below was written before that and went stale; the Stage 5 verdict agent
> caught the discrepancy by checking the code instead of believing the document,
> which is the right instinct and the reason this header exists rather than a quiet
> edit.
>
> What shipped: `DeviceRepositoryImpl` holds a CONFLATED refresh channel, and the
> poll's wait is a race — `withTimeoutOrNull(DiscoveryPollMs) { refreshRequests
> .receive() }`. Exposed as `DeviceRepository.refreshDiscovery()` +
> `RefreshDiscoveryUseCase`, called by all three recovery actions. Proven by four
> mutations: a plain `delay` fails both new tests, a rendezvous channel fails the
> between-polls test, and reverting `retryDiscovery()` to a no-op fails
> `retryDiscoveryNudgesThePollLoop`.
>
> The analysis below is kept because it is why the fix landed in the data layer
> rather than the presentation layer, and that reasoning is still load-bearing.

---


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

## What was NOT implemented at the time this was written

`RetryDiscovery` was `= Unit`. That is no longer true — see the status correction at
the top of this file. The audit later classified it as **AF-1, a dead button**, and
it was fixed in the data layer exactly as the recommendation at the bottom of this
file proposed.

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

## Recommendation — ACTED ON

This was recorded as an open gap rather than a waiver because the shortfall was
measured, not accepted. It was then fixed along exactly these lines: give `observeDiscovery()`
a `refresh()` trigger inside `DeviceRepositoryImpl`'s own poll loop, where the
2 s `delay` lives. A `select`-style race between the delay and a refresh channel
would cut the wait without touching the presentation gate at all — and the loop
is already covered by virtual-time tests, so it can be proven there rather than
through Orbit's dispatcher timing, which is what made this flaky.
