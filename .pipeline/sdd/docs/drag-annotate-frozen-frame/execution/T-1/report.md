# drag-annotate-frozen-frame — T-1/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-1/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-15T05:15:01Z |
| updatedAt | 2026-10-15T05:15:01Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-15T05:15:01Z

## Provenance

### selfHash

0ee6ad92e021b4ed285d3f6beb95c0c957aea1f52fe9c433ef13e1741f3301d9

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-1

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/.../core/geometry/FrameGeometry.kt
- +FrameGeometryTest.kt

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks ; :compileTestKotlinIosArm64

#### passed

true

#### output

FrameGeometryTest 9 tests, 0 failures. MUTATION-VERIFIED: dropping /density from toDp failed the round-trip and the divides-once test; smuggling *density into toDevice failed 4 tests.

### implementation

#### filesChanged

- presentation-feature-mirror/.../core/geometry/FrameGeometry.kt
- +FrameGeometryTest.kt

#### summary

internal FrameGeometry(framePx, deviceSize, density) with toDevice/toFrame/toDp/isMeaningfulDrag. toDevice is a pure ratio with NO density term; toDp is the only member dividing by it. Normalises reversed drags, rounds corners independently, clamps to device bounds. MIN_DRAG_DEVICE_PX=8 required in BOTH axes. The highest-risk piece in the spec, built and tested before any UI existed.

#### reusedFromRegistry


### troubleshooting


### needsHumanReview

#### required

true

#### reason

isMeaningfulDrag requires the threshold in BOTH axes; the spec names the constant but not the conjunction. Chosen as AND (a 400x2 rect selects nothing a user meant) and documented.

### linkedAC

- AC-5
- AC-46
- FR-26
- SC-8
