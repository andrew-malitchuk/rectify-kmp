# T-8 — hardware verification, RUN

Device reattached, so this is no longer blocked. Run against the Pixel 8
(`38031FDJH00JVF`, Android 16, USB) on commit `34b87d5` plus the observability fix
described below.

## TAC-1 — cold-daemon recovery: PASS

```
adb kill-server          # cold state confirmed: no daemon pid
./gradlew :desktop-application:run
```

```
Debug: (ProcessRunner)  adb devices -l exceeded 3000ms, killing it
Debug: (DeviceRepository) poll timed out; starting the adb daemon
```

| metric | before the fix | now |
|---|---|---|
| `exceeded 3000ms` lines | **41, zero successful polls** | **1** |
| daemon starts | 0 (nothing ever started it) | **1** |
| wedge escalations | n/a | 0 — correct, the daemon was absent, not wedged |
| device found | never | `38031FDJH00JVF` |

The daemon was started **by the app**. I ran `kill-server` and never ran
`start-server`; afterwards `adb devices` reports the handset. SC-16 satisfied:
recovery with no manual shell intervention, exactly one start, no run of timeouts.

### An observability defect this run exposed

The first two attempts looked like a FAILURE: one timeout, **zero `start-server`
lines**, and a device that appeared anyway. I nearly recorded the ladder as not
firing.

Cause: `ProcessRunner` logs only when it **kills** a process. A repair that works
emits nothing, so the fix was indistinguishable from the bug in the log — the one
place a person looks. Fixed by logging on the way *in* to `healDaemon()`, not on
success. An unobservable repair is one nobody can support in the field, and the
near-misdiagnosis is the evidence that it mattered.

## TAC-2 — wedged daemon (SIGSTOP): escalation CORRECT, recovery LIMITED

`kill -STOP` on the daemon pid, then launch:

```
adb devices -l    exceeded 3000ms
  poll timed out; starting the adb daemon
adb start-server  exceeded 15000ms          <- start-server HANGS on a wedge, as measured
  daemon start timed out; it is wedged rather than absent, restarting it
adb kill-server   exceeded 3000ms           <- kill-server cannot kill it either
adb devices -l    exceeded 3000ms
  ... ladder repeats on the next poll
```

Counted: 3 three-second timeouts, 2 heals, **2 wedge escalations**, 2 fifteen-second
timeouts.

**The ladder classifies the wedge correctly and escalates exactly as designed.**
Step 3 fires, every time, on the right signal.

**It does not recover from this particular wedge**, and the reason is inherent:
`adb kill-server` works by *connecting to the daemon and asking it to exit*. A
daemon that cannot answer `devices` cannot answer that either. Clearing a hard
wedge needs the pid killed directly, which this spec does not do and arguably
should not — Rectify killing processes it did not start is a bigger decision than
error handling.

What it does instead is the designed fallback: emits `PollFailed` with the real
reason and polls again. **No silent loop**, and the user gets a card saying why.

Cost per cycle is ~24 s (3 + 15 + 3 + 3). Slow, and correct: every step is paying
a budget that exists to bound exactly this case.

Caveat the plan already recorded and this run confirms: `SIGSTOP` is only an
approximation of a real wedge, and the wedged case measured
**non-deterministically** earlier (`start-server` hung once, returned 0.00 s
another time against the same stopped daemon). The virtual-time tests T15/T16
remain the authority; this is a sanity check that the ladder reaches step 3 on
real hardware, and it does.

## TAC-3 — orphan audit: PASS

```
pgrep -fl adb        # after closing the window
# no adb child besides the daemon
pgrep -fl screencap
# none
```

**First time this has ever been verified.** `data-device-impl/CLAUDE.md` has
carried `ProcessRunner`'s real timeout/orphan/drain behaviour as *unverified
without a device* since the module was written. The `destroyForcibly` in `finally`
plus the `ConcurrentHashMap` live-child registry and the shutdown hook do what
they claim: closing the window left nothing behind, including after a run that
killed children on three separate timeouts.

## Open item this run created

Recovery from a hard wedge is unsolved, by choice. Options, for a human:

1. Leave it. The user sees a card with the reason and can run `adb kill-server`
   themselves — the card already shows exactly that command.
2. Escalate to killing the daemon pid. More capable, and a real escalation in what
   Rectify is willing to do to processes it does not own.

Option 1 is what ships. Recording it as an open item rather than a waiver: nobody
decided the risk was acceptable, the limitation was discovered here.
