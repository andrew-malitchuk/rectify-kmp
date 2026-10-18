# Hardware-dependent tasks: blocked, not done

Two wave-5 tasks require a physically attached Android device and **cannot be
completed autonomously** while none is connected.

Checked at wave-2 dispatch: `adb devices -l` reports an empty list. The Pixel 8
(`38031FDJH00JVF`, Android 16) was attached earlier in this session — all the
measurements this spec rests on were taken against it — but it has since been
unplugged, and it is not on USB either (`system_profiler SPUSBDataType` finds no
Google device).

| task | what it needs | status |
|---|---|---|
| **T-8** | SC-16 cold-daemon recovery end to end, H1, H2 orphan audit | BLOCKED |
| **T-18** | SC-13 physical unplug/replug with annotations staged, H3, H4 | BLOCKED |

## What is NOT blocked, and why that matters

The authority for all of this spec's behaviour is the **virtual-time test suite** —
T1, T15, T16 drive a fake `DeviceCommandExecutor` through the escalation ladder with
no hardware at all. That was a deliberate architectural choice recorded in
`data-device-api/CLAUDE.md`: every parser and policy lives in `commonMain` precisely
so the discovery logic is testable with no phone attached.

So the hardware runs are **sanity checks on top of a proof, not the proof itself**.
Their absence does not block the spec's correctness claims; it blocks a specific
class of confirmation:

- that a real cold daemon (3.03 s fork) is actually recovered by the real ladder, not
  just by a fake that reports a timeout
- that `ProcessRunner`'s orphan reaping works against real `adb` children — its
  `CLAUDE.md` has recorded this as unverified since the module was written
- that a real mid-session unplug preserves staged annotations through the real
  `.catch { onSessionLost(device) }` path

## Honest handling

These two tasks will be marked **blocked**, not complete, and the Stage 5 verdict
must record them as an open gap rather than a waiver — a waiver implies a decision was
made to accept a risk, whereas this is simply work that could not be attempted.

Reproduction steps are already written down in `plan/plan.json`'s `commandNotes` and
in `inputs/reproduced-defect-adb-cold-start.md`, so whoever reattaches a device can run
them without re-deriving anything:

```shell
# cold path — pre-fix this livelocks with 41+ timeouts and zero successes
adb kill-server && ./gradlew :desktop-application:run

# wedged path — SIGSTOP approximates a wedge; remember to CONT afterwards
kill -STOP $(pgrep -f 'adb -L tcp:5037')
kill -CONT $(pgrep -f 'adb -L tcp:5037')

# orphan audit — must be empty after the window closes
pgrep -fl adb
```

Caveat already recorded and still standing: `SIGSTOP` only approximates a real wedge
(a half-open socket, a USB-stack hang), and the wedged case behaved
**non-deterministically** when measured — `start-server` hung in one run and returned
0.00 s in another against the same stopped daemon. Do not tune recovery logic to this
simulation.
