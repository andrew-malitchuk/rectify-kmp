# drag-annotate-frozen-frame — T-6/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-6/report |
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

b8488cadd63f05488123d0f5a8cbbbf15b917088cc4a1a657c3c6932ad655c87

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-6

### type

ui

### outcome

success

### touchedPaths

- presentation-feature-mirror/.../MirrorAnnotationStage.kt
- MirrorHost.kt
- MirrorContent.kt

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks ; :compose-application:compileKotlinDesktop

#### passed

true

#### output

Both BUILD SUCCESSFUL, 188 repo-wide at that point.

### implementation

#### filesChanged

- presentation-feature-mirror/.../MirrorAnnotationStage.kt
- MirrorHost.kt
- MirrorContent.kt

#### summary

Drag state lives in composable remember, NEVER in Orbit - the reducer sees exactly one pass per gesture, at release, gated on isMeaningfulDrag. Guides are the kit's horizontal Ruler plus the same Ruler rotated 270 degrees, both fed device pixels.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| MirrorAnnotationStage.kt | pointerInput(Unit) would capture a stale FrameGeometry (framePx is Size.Zero on first composition). | geometry and onIntent read through rememberUpdatedState inside the release handler. Without this the drag layer silently stages a zero rect. | warn |
| MirrorAnnotationStage.kt | The two ruler strips overlap in the top-left corner square. | Insetting either would make its scale stop matching the frame it measures - roughly 3% error on a 700dp stage - which is the worse bug. Overlap is deliberate and documented. | info |

### needsHumanReview

#### required

true

#### reason

Needs one rendered pass. This host cannot load Skiko in-process (ImageBitmap(2,3) throws LibraryLoadException) and the repo has no Compose UI-test harness, so nothing visual is asserted by a test.

### linkedAC

- AC-5
- FR-6
- NFR-1
- SC-3
