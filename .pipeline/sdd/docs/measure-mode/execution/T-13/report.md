# measure-mode — T-13

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-13 |
| status | done |
| producedBy | orchestrator-under-operator-delegation |
| schemaVersion | 1 |
| createdAt | 2026-10-17T08:52:18Z |
| updatedAt | 2026-10-17T08:52:18Z |

## Gate

### type

auto

### approvedBy

orchestrator-under-operator-delegation

### approved

true

### at

2026-10-17T08:52:18Z

## Provenance

### selfHash

1e88a1439a91321e9177bcd1df71c893747e5350a84086cb1340e62fef32195e

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-12/report.json | c89f2efbb4cb8b4cd78347b842e72ee8ee685abe72b15ab3c749c36532b0ce0c |

## Data

### taskId

T-13

### type

test

### outcome

partial

### touchedPaths

- .pipeline/sdd/docs/measure-mode/execution/T-13/gate-note.md
- .pipeline/sdd/docs/measure-mode/execution/MANUAL-CHECKS.md

### implementation

#### filesChanged

- execution/T-13/gate-note.md
- execution/MANUAL-CHECKS.md

#### summary

Terminal cross-target gate. Ran all four machine gates green, recorded the aggregate 547-test figure, read the device's real dpi/size facts off the attached Pixel 8, smoke-tested the desktop app with the measure code in it, and wrote runnable instructions for the four criteria only a human can close. No production code: this task verifies, it does not build.

### testRun

#### command

./gradlew :data-device-api:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest ; :data-device-api:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64 ; :presentation-core-ui:build ; grep -n '-> Unit' MirrorViewModel.kt

#### passed

true

#### output

All BUILD SUCCESSFUL (core-ui in 2m 1s). grep exit 1, no output. Aggregate: data-device-api 100, data-repository-impl 93, presentation-feature-mirror 284 (desktop + iosSimulatorArm64), presentation-core-ui 58, presentation-core-styling 12 = 547 tests, 0 failures.

### coverage

skipped

### needsHumanReview

#### required

true

#### reason

Four DoD clauses need a human at this machine and are recorded BLOCKED, not waived: the light/dark previews rendered by eye (MC-4), the AC-10/11/67/69/71/72 interaction demo on hardware (MC-2, MC-3, MC-5), and the dropped-frame delta read from TransportStats across a freeze-with-dump. Instructions in execution/MANUAL-CHECKS.md.

### linkedAC

- AC-10
- AC-11
- AC-67
- AC-69
- AC-71
- AC-72

### blockedDependents


### registryUpdates


### backlogUpdate

#### taskId

T-13

#### status

done

### visualValidation

skipped

### missingTestsFound

- No test asserts the dumpJob cancellation path: a dump still in flight when the frame is released is cancelled in releaseHierarchy(), so a late resolution cannot reduce Ready about a gone frame. Reported honestly by the batch-B implementer and left uncovered.
- The design's Math.max(h, 1) neighbour-height floor is unexercised by the oracle: no ELS node has h == 0, so the real fixture cannot reach it.
- MeasureReadout.guides endpoint geometry is now asserted (batch-A follow-up), but the ordinary mid-axis guides' sub-pixel rounding is not: the design's mid-y is a real-valued 206.5 on an odd-height target and the implementation truncates to 206, since IntOffset cannot carry half a pixel.

### testsAdded

- MeasureGateTest — 14 cases, batch C, making the Alt gating and dispatch-edge decisions testable by pulling them out of the composable
- MeasureThresholdUnitsTest — 8 cases at 420 dpi, batch-A follow-up, catching that the misalignment bounds were read in raw px
- MeasureGuideGeometryTest — 2 cases, batch-A follow-up, pinning the four guides against the design's own H/V helpers

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| DoD - three desktopTest suites green | true | 1 | ["BUILD SUCCESSFUL."] |
| DoD - compileTestKotlinIosArm64 green for data-device-api and presentation-feature-mirror | true | 1 | ["BUILD SUCCESSFUL. presentation-feature-mirror additionally RUNS on iosSimulatorArm64, so the measure logic passes rather than merely compiles on Kotlin/Native."] |
| DoD - presentation-core-ui:build green | true | 1 | ["BUILD SUCCESSFUL in 2m 1s."] |
| DoD - the inert-intent grep prints nothing | true | 1 | ["No output, exit 1."] |
| DoD - the device's reported dpi recorded | true | 1 | ["Pixel 8, Android 16, density 420 (2.625 px/dp), physical 1080x2400, override 984x2132 - a 9.76 percent gap, and uiautomator reports the override geometry."] |
| DoD - the px-degradation path observed at least once | true | 1 | ["TEST-VERIFIED rather than seen on screen: PixelUnitsTest pins densityDpi <= 0 degrading to (px, Px) and FindingsTest pins the touch-target finding going silent at unknown density. Recorded as asserted, not demonstrated."] |
| DoD - light and dark previews rendered by a human | false | 0 | ["BLOCKED, not waived. MC-4."] |
| DoD - the AC-10/11/67/69/71/72 demo run on real hardware | false | 0 | ["BLOCKED, not waived. The dpi half is recorded; the interaction half needs a person. MC-2, MC-3, MC-5."] |
| DoD - dropped-frame delta recorded from TransportStats | false | 0 | ["BLOCKED, not waived. Needs the app streaming and a human reading the stats row."] |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| T-13 renderer assumption | Earlier stages recorded, on an agent's report, that this host cannot load Skiko - and that claim shaped how several criteria were scoped. | Measured and WITHDRAWN. ./gradlew :desktop-application:run loads Skiko, opens a window and runs clean for over 30 seconds with no exceptions. What this session lacks is Screen Recording permission, so screencapture fails with 'could not create image from display'. The blocker is capture, not rendering: a human at this machine needs no extra setup. The clean boot also verifies something no unit test does - Koin resolves the new DumpHierarchyUseCase binding at runtime, which is the one out-of-scope DI edit batch B flagged. | info |
| T-13 effort-tier note | The fast tier's contract skips the terminal coverage-gap sweep, but T-13 is an explicit backlog task rather than the generic sweep. | Run in full as a backlog item. The generic coverage-gap sweep was separately skipped per the tier; the three gaps found above are recorded in missingTestsFound rather than silently dropped. | info |
