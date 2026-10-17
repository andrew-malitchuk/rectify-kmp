# measure-mode — T-11

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-11 |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T08:46:48Z |
| updatedAt | 2026-10-17T08:46:48Z |

## Gate

### type

auto

### approvedBy

orchestrator-under-operator-delegation

### approved

true

### at

2026-10-17T08:46:48Z

## Provenance

### selfHash

163e450c91f62bf4f5e42ef11bd8dc4c7e9093cb5714d72f6d2d4fb4269f42cb

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-10/report.json | 6239b9a362ae8fbdc5e75515e0c06c41ec323bbfa33551d77cc1a2c8de59b3e7 |

## Data

### taskId

T-11

### type

ui

### outcome

partial

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureGate.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MeasureGateTest.kt

### implementation

#### filesChanged

- MirrorAnnotationStage.kt
- core/measure/MeasureGate.kt
- commonTest/MeasureGateTest.kt

#### summary

Alt detection as the planned hybrid: a focus-independent pointerInput sampler reading event.keyboardModifiers.isAltPressed at PointerEventPass.Initial (which also supplies the hover position), plus a new overlay-root FocusRequester with an onPreviewKeyEvent isAltPressed branch, plus EndMeasure on KeyUp, on focus loss and on the next pointer-down. Hover offset, altHeld and the dispatch edge live in remember, never in Orbit state, so only StartMeasure/EndMeasure reduce. MeasureOverlay draws the target and up to four neighbour MeasureMarks, the dashed mid-axis guide Canvas, the alignment rail and the MeasureTag numerals; CommentPopover is suppressed while a readout exists. The gating decision was deliberately extracted into pure internal functions in core/measure/MeasureGate.kt so it could be tested at all.

### tdd

#### testsWritten

- MeasureGateTest — 14 cases over shouldMeasure, measureTarget and measureDispatch

#### testsFrozen

true

#### cycle

logic extracted from the composable, then tested

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks ; :presentation-feature-mirror:compileTestKotlinIosArm64

#### passed

true

#### output

desktopTest 197 tests, 0 failures, 0 errors, 0 skipped (183 pre-existing + 14 MeasureGateTest). compileTestKotlinIosArm64 BUILD SUCCESSFUL. Verified by the orchestrator, not reported by an agent.

### needsHumanReview

#### required

true

#### reason

Three DoD clauses need a human at a screen and are recorded BLOCKED, not waived: the stuck-panel demo (Cmd-Tab mid-hold, click-outside, release-outside, Alt-during-drag), the popover typing-focus check, and the on-device truth of the overlay geometry. Instructions are in execution/MANUAL-CHECKS.md as MC-2, MC-3 and MC-5. A compiled-but-unrendered composable is not verification - this repo's HARDWARE-BLOCKED precedent and the drag-annotate W-2 waiver.

### linkedAC

- AC-10
- AC-11
- AC-71

### blockedDependents


### registryUpdates


### backlogUpdate

#### taskId

T-11

#### status

done

### visualValidation

skipped

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| DoD - every coordinate routes through FrameGeometry exactly once | true | 1 | ["Verified by grep over the added lines only: batch C added exactly three matches for /density, *density, .toPx() or N.dp - the one sanctioned named constant MeasureCardWidth = 228.dp and two strokeWidth = guideWidth.toPx() calls. A stroke width is not a coordinate, and the same file's pre-existing leaderWidth.toPx() establishes that precedent. Guide and rail coordinates all route through geometry.guideFrame() / geometry.toDp(geometry.toFrame())."] |
| DoD - measure mode gated off while dragStart != null and while frozen == null | true | 1 | ["Pinned by MeasureGateTest's aDragInProgressIgnoresAlt and aLiveStageIgnoresAlt."] |
| DoD - a missed key release still recovers | true | 1 | ["Logic pinned by aMissedKeyUpStillEndsThroughFocusLossOrPointerDown and resumingLiveUnderAHoldEnds. Whether the real OS key-up arrives is MC-2, blocked on a human."] |
| DoD - dispatch edge gives one StartMeasure per target, not per frame | true | 1 | ["Pinned by threeAnnotationsUnderOneHoldGiveThreeStartsAndOneEnd, holdingAltStillOverTheSameAnnotationDispatchesNothing and movingToAnotherAnnotationStartsRatherThanEnding."] |
| DoD - early manual demo of the stuck-panel and popover-focus risks | false | 0 | ["BLOCKED, not waived. No renderer on this host and no human present. MC-2 and MC-3 in execution/MANUAL-CHECKS.md."] |
| DoD - desktopTest + compileTestKotlinIosArm64 green | true | 1 | ["197 tests, 0 failures, both targets."] |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| batch C planning | No Compose UI-test harness exists in this repo and this host cannot load Skiko, so no composable in either task can be asserted automatically. | The gating decision was extracted into pure internal functions (shouldMeasure, measureTarget, measureDispatch) in core/measure/MeasureGate.kt and unit-tested with 14 cases, converting the testable part of the interaction into commonTest coverage. Everything genuinely visual is recorded blocked in execution/MANUAL-CHECKS.md with runnable instructions. | warn |
| T-11 AS-21 | AS-21 assumed the Alt key-up always arrives. The plan's own risk 3 contradicted it: onPreviewKeyEvent needs focus and the stage root requested it nowhere, so a stuck Measuring panel was plausible. | Implemented the hybrid rather than trusting the assumption - pointer sampler plus focus requester plus three independent end paths - and made the decision logic pure so 14 cases could pin it. The residual risk is whether the real key-up fires, which only MC-2 can answer. | warn |
