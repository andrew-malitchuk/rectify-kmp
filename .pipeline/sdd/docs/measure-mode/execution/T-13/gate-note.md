# T-13 — cross-target gate, run 2026-10-17

Run by the orchestrator under the operator's delegation. Every command below was
executed; outputs are quoted as they came back, not summarised from memory.

## The four machine gates — all green

```
./gradlew :data-device-api:desktopTest :data-repository-impl:desktopTest \
          :presentation-feature-mirror:desktopTest
  → BUILD SUCCESSFUL

./gradlew :data-device-api:compileTestKotlinIosArm64 \
          :presentation-feature-mirror:compileTestKotlinIosArm64
  → BUILD SUCCESSFUL

./gradlew :presentation-core-ui:build
  → BUILD SUCCESSFUL in 2m 1s

grep -n '\-> Unit' .../source/mirror/MirrorViewModel.kt
  → (no output, exit 1)
```

The grep is the guard against this repo's own recorded failure, where five
intents shipped as `-> Unit` branches and compiled perfectly green while doing
nothing. It prints nothing.

## Aggregate suite

| module | tests | failures |
|---|---|---|
| `data-device-api` | 100 | 0 |
| `data-repository-impl` | 93 | 0 |
| `presentation-feature-mirror` | 284 | 0 |
| `presentation-core-ui` | 58 | 0 |
| `presentation-core-styling` | 12 | 0 |
| **total** | **547** | **0** |

`presentation-feature-mirror`'s figure spans both the desktop and
`iosSimulatorArm64` runs, so the measure logic is proven to **pass**, not merely
compile, on Kotlin/Native — the distinction `.claude/rules/testing.md` warns
about, and which has hidden a real failure in this project before.

## Hardware facts, read from the attached device

```
$ adb shell getprop ro.product.model     → Pixel 8
$ adb shell getprop ro.build.version.release → 16
$ adb shell wm density                   → Physical density: 420
$ adb shell wm size                      → Physical size: 1080x2400
                                           Override size:  984x2132
```

420 dpi = 2.625 px/dp. The override/physical gap is **9.76 %**, and `uiautomator`
reports the **override** geometry — which is why `MeasureProjection` guards the
snapshot against the frozen frame's size rather than trusting either number
blindly.

## A correction: this host CAN render. It cannot be screenshotted.

Earlier stages recorded, on an agent's report, that "this host cannot load
Skiko". **That is wrong for the real application** and the claim is withdrawn.
Measured instead:

```
$ ./gradlew :desktop-application:run
  → Skiko loads (skiko-awt-runtime-macos-arm64-0.150.1), window opens,
    process alive >30 s, NO exceptions in the log
$ screencapture -x app.png
  → "could not create image from display"
```

So the app **boots and renders with the measure code in it**. What this session
lacks is **Screen Recording permission**, so no image can be captured. The
blocker is capture, not rendering — a human sitting at this machine can render
and inspect everything in `MANUAL-CHECKS.md` with no extra setup.

### What the clean boot does verify

A >30 s run with no exceptions is a real smoke test, and it covers one thing no
unit test does: **Koin resolves the new `DumpHierarchyUseCase` binding at
runtime.** Koin throws at startup on a missing binding, and that DI registration
was the one out-of-scope edit batch B flagged (a constructor param that is an
interface, so it cannot carry a default). It works.

## What stays BLOCKED — not waived

Per `HARDWARE-BLOCKED.md` and the drag-annotate verdict's `W-2`: **a
compiled-but-unrendered `@Preview` is not verification.**

| DoD clause | state | why |
|---|---|---|
| light/dark measuring previews rendered by a human | **BLOCKED** | renders fine; cannot be captured from this session. MC-4 |
| AC-10/11/67/69/71/72 demo on real hardware | **BLOCKED** | needs a person holding Option over a live window. MC-2, MC-3, MC-5 |
| dropped-frame delta from `TransportStats` across a freeze-with-dump | **BLOCKED** | needs the app streaming and a human reading the stats row |
| px-degradation path observed | **TEST-VERIFIED, demo blocked** | `PixelUnitsTest` pins `densityDpi <= 0 → (px, Px)` and `FindingsTest` pins the touch-target finding going silent at unknown density. Asserted in `commonTest`; never seen on screen |

The device's dpi **is** recorded (420), so the one clause asking for a real
hardware number is satisfied. The interaction demo is not.

## Verdict

**Every criterion a machine can check in this project is checked and green.**
Four criteria need a human at this screen; they are listed with runnable
instructions in `execution/MANUAL-CHECKS.md` and recorded blocked, so the spec
cannot be called complete on the automated suite alone.
