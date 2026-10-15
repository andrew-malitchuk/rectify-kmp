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
