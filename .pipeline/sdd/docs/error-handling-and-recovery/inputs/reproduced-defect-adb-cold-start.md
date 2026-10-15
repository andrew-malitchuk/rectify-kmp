# Reproduced defect: discovery livelocks on a cold adb daemon

**Status:** reproduced on real hardware, 2026-10-15. Not a hypothesis.
**Severity:** blocking. On any machine where the adb daemon is not already
running, Rectify can never discover a device — it sits on the first screen forever.

## What happens

`MirrorTuning.HostCommandTimeoutMs` is `3_000L`. `adb devices -l` on a machine
with no running daemon must first fork the daemon, and that measured **3.053 s**
on this machine — just over the budget.

So the poll times out, `ProcessRunner`'s `finally` calls `destroyForcibly()`,
and that kills `adb` *while it is still bringing the daemon up*. The daemon
therefore never finishes starting. `DiscoveryPollMs` fires 2 s later into the
same cold state, and the cycle repeats forever.

This is a livelock, not a slow path: there is no attempt count after which it
succeeds, because each attempt actively destroys the progress the last one made.

## Evidence

Desktop run, 2026-10-15 16:04–16:10:

- 488 log lines, **41 consecutive `exceeded 3000ms, killing it`, zero successes**.
- The log stopped growing at 16:10:46 — the exact moment a `adb devices -l` was
  run *manually from a shell, with no timeout*, which let the daemon finish
  starting (pid 12403, `adb -L tcp:5037 fork-server server`).
- After that, the app recovered on its own and found the device:
  `38031FDJH00JVF  device usb:0-0 product:shiba model:Pixel_8`.

Timings, same machine, same binary:

| adb daemon state | `adb devices -l` wall time |
|---|---|
| not running (must fork it) | **3.053 s** — over the 3.000 s budget |
| already running | 0.01 s |

## What it is NOT

The pipe-EOF hazard documented in `ProcessRunner`'s own KDoc ("a grandchild
holds the write end, so the read never reaches EOF") was the first suspect and
is **ruled out**. A minimal JVM repro using the same `waitFor()` + concurrent
`readAllBytes()` shape reached EOF on both pipes in 8 ms. The drains are fine;
the budget is the problem.

## Suggested fix (for the plan to accept or reject)

Raising `HostCommandTimeoutMs` is the obvious move and the wrong one — cold
start time varies per machine, so any constant is a guess that fails on a
slower one.

Prefer making the daemon start an explicit, separately-budgeted step:

1. Add `HostCommand.StartServer -> listOf("start-server")` to the closed set in
   `DeviceArgv.kt` (adding a case is sanctioned by `data-device-api/CLAUDE.md`;
   adding a raw-argv escape hatch is not).
2. Run it once before the first discovery poll, under its own generous budget
   (`DaemonStartTimeoutMs`, suggest 15 s), and again whenever a poll fails with
   a timeout rather than a parse error.
3. Only then does `ListDevices` poll at `HostCommandTimeoutMs`.

## Test that must be shown failing first

Per the NFR-6 note already in `events.jsonl`, this chain has already shipped one
vacuous test that passed against deleted code. The regression test here must
drive a fake `DeviceCommandExecutor` whose first N `ListDevices` calls time out,
and assert the source **recovers** rather than polling forever. Demonstrate it
red against current `main` before trusting it green.

## Follow-up measurement: `adb start-server` in isolation

Run three times, each preceded by `adb kill-server`:

| run | wall time |
|---|---|
| 1 | 3.03 s |
| 2 | 3.03 s |
| 3 | 3.03 s |

Identical to the hundredth of a second across runs. That is not variable work
being measured — it is a fixed internal delay inside adb's fork-server path.

Two consequences for the chosen fix:

1. **The 15 s `DaemonStartTimeoutMs` watchdog has ~5x headroom.** It is
   comfortably clear of the real cost without becoming a per-machine guess,
   which is exactly the property the decision was chosen for.
2. **The old budget missed by ~30 ms, deterministically.** `HostCommandTimeoutMs`
   is 3000 ms and the daemon start is 3030 ms, every single time. This was never
   a flaky timing race that some machines would survive — on any machine with a
   cold daemon it fails on every attempt, forever. That is why the symptom is a
   livelock rather than intermittent slowness.

A useful property for the regression test: because the cost is fixed rather than
distributed, the cold-start path is reproducible on demand with
`adb kill-server` and does not need a timing tolerance.

## `start-server` is idempotent and non-disruptive (measured)

With a daemon already running and the Pixel 8 attached:

| check | result |
|---|---|
| daemon pid before | 78433 |
| `adb start-server` wall time | **0.00 s** |
| daemon pid after | 78433 — unchanged, not restarted |
| `adb devices` after | `38031FDJH00JVF  device` — session undisturbed |

So the healing step costs nothing on a warm daemon and does not bounce a live
connection.

**This simplifies the chosen fix.** The decision was worded as "before the
first poll and after any timeout-failed poll", which implies tracking whether
the daemon is believed up and whether the last poll failed on a timeout
specifically. That conditional state is unnecessary: because the warm case is
free (0.00 s) and side-effect-free (same pid, device retained), the step can be
run **unconditionally** ahead of each discovery poll.

Fewer moving parts, and it removes a failure mode of its own — a stale "daemon
is up" belief that survives a daemon the user killed from a shell. Prefer the
unconditional form unless the plan has a reason this measurement does not cover.

Caveat worth keeping: this measures the *same* adb binary talking to its own
daemon. A version mismatch (a second adb from a different SDK) makes
`start-server` kill and restart the daemon rather than no-op, which WOULD
disturb a live session. That is a pre-existing hazard of the environment, not
of this fix, but the error path should not assume the 0.00 s case.

## A WEDGED daemon is a second, distinct failure mode — and `start-server` does not heal it

Measured by `SIGSTOP`-ing the running daemon (pid 78433) to simulate a daemon
that is alive but not serving — a hung daemon, not a missing one:

| command, against a wedged daemon | result |
|---|---|
| `adb devices` | **hung, >8 s, never returned** |
| `adb start-server` | **also hung** (one earlier run returned 0.00 s instead) |

Two things follow, and the second one contradicts the simplification proposed
in the section above.

### 1. This validates minting `HostCommand.KillServer`

`start-server` cannot recover a wedged daemon — it is not a repair command, it
is a "make sure one exists" command, and one already exists. Only
`kill-server` (or killing the pid) clears it. So the AC-27 "Restart adb" action
genuinely needs `kill-server` + `start-server`, not `start-server` alone. The
judge's ruling to widen the closed set is therefore empirically correct, not
merely a copy-fidelity concern.

### 2. The UNCONDITIONAL `start-server` idea is WRONG — withdraw it

The preceding section recommended running `start-server` unconditionally ahead
of every discovery poll, on the grounds that the warm case costs 0.00 s and is
side-effect-free. That reasoning only covered a *healthy* warm daemon. Against
a wedged one the call hangs, so an unconditional pre-poll `start-server` would
burn the full `DaemonStartTimeoutMs` (15 s) watchdog on **every** poll — a
second livelock, introduced by the fix itself and strictly worse than the
3-second one it replaces, because the watchdog is five times longer.

**The step must stay budgeted and must escalate**, roughly:

1. `ListDevices` at its 3 s budget. Success — done.
2. On a timeout: `StartServer` under `DaemonStartTimeoutMs`. This covers the
   cold case (3.03 s, fixed).
3. If `StartServer` itself times out, the daemon is wedged rather than absent:
   `KillServer`, then `StartServer`. This is the only path that recovers a
   wedged daemon, and it is also what AC-27's button does.
4. Still failing — surface `PollFailed` with the reason. Do not loop silently.

### Honesty note on determinism

Unlike the cold-start cost (3.03 s on every run, to the hundredth), the wedged
case was **not** deterministic: `start-server` hung in one run and returned
0.00 s in another against the same stopped daemon. Treat "wedged" as a flaky
state, which is a further argument for every step carrying its own budget
rather than relying on any observed timing. A `SIGSTOP`-ed daemon is also only
an approximation of the real-world wedge (a half-open socket, a USB-stack
hang); do not over-fit the recovery logic to this exact simulation.
