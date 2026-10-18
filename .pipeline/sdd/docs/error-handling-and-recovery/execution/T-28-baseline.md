# T-28 Baseline — captured before any F-3 code change

Captured 2026-10-15T18:47:10Z, on commit `52dc9e0`.
This is the reference every later 'no regression' claim is measured against.

## Test baseline

```
TOTAL 195 tests | failures 0 | skipped 0
```

| module | tests |
|---|---|
| presentation-feature-mirror | 66 |
| data-device-api | 39 |
| data-repository-impl | 35 |
| presentation-core-ui | 29 |
| data-device-impl | 16 |
| data-preference-impl | 5 |
| presentation-core-styling | 5 |

Full pass list (195 fully-qualified names) is in `baseline-tests.txt` beside this file.
A later run must contain **every** one of these names still passing — a name that
disappears is as much a regression as a name that fails, and is easier to miss.

### Correction: this baseline was first recorded as 196

The first capture counted 196, because the glob over `*/build/test-results/` picked up
a stale `compose-application` result for `PixelProbe.renderBoth` — a throwaway render
harness used earlier in this session for the pixel-perfect design comparison. It is not a
spec test, is not tracked in git, and no longer exists on disk in any form; only its
orphaned JUnit XML survived long enough to be counted.

The T-1 executor caught this independently by stashing its change and re-measuring, which
is the only reason it was found. **195 is the real number**, and it is what every later
no-regression claim is measured against. `compose-application` has no test source set at
all (`.claude/rules/testing.md` records that it deliberately gets no test wiring), so any
future count attributing tests to it is reading stale output and should be distrusted.

The lesson for later waves: count from a freshly-run suite, not from whatever XML happens
to be lying in `build/`.

## Command

```shell
./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest \
          :presentation-feature-mirror:desktopTest :data-preference-impl:desktopTest \
          :data-repository-impl:desktopTest :data-device-api:desktopTest \
          :data-device-impl:desktopTest :domain-usecase-impl:desktopTest
```

Desktop is the target that runs. A cross-target `:<module>:build` is still required before
the spec is declared done — Kotlin/Native rejects backtick test names containing commas,
apostrophes or colons, so a green desktop suite does not prove cross-target compilation.

## Hardware baseline

| | |
|---|---|
| Device |  |
| adb | Android Debug Bridge version 1.0.41 |
| Daemon | running |

NOTE: at capture time the Pixel 8 (38031FDJH00JVF) was UNPLUGGED — it had been attached
earlier in the session and is the device the NFR-1 and cold-start measurements were taken
against. The hardware checks in plan.json's commandNotes therefore cannot be run until it
is reattached; the virtual-time tests (T1, T15, T16) are the authority regardless, and the
hardware runs are sanity checks on top of them.

## Measured facts this baseline carries forward

- Cold `adb start-server`: **3.03 s**, three runs, identical to the hundredth — a fixed
  internal delay, not variable work. Against `HostCommandTimeoutMs = 3000` that is a
  deterministic ~30 ms miss, which is why the symptom is a livelock rather than flakiness.
- Warm `adb start-server`: **0.00 s**, daemon pid unchanged, session undisturbed.
- Wedged daemon (SIGSTOP): `adb devices` hangs >8 s AND `start-server` hangs too — the
  reason `KillServer` is minted. Non-deterministic (one run returned 0.00 s), so every
  step must carry its own budget rather than trust an observed time.
- `adb exec-out screencap -p`: mean **2665 ms** / p95 2983 ms / 1869 KiB = **0.4 fps**.
  Raw `screencap` (no on-device PNG): mean 1166 ms = 0.9 fps. NFR-1 budgets ~150 ms, so
  the shipped transport is ~18x over and cannot be tuned into range. Out of F-3 scope.

## Live-app verification of the defect

| run | daemon state | `exceeded 3000ms` log lines |
|---|---|---|
| 1 | cold | **41, zero successful polls** |
| 2 | warm | **0** |

Same binary, same machine, same commit. The only variable is whether the daemon was
already up, which localises the defect to the budget rather than to `ProcessRunner`
or the polling loop.
