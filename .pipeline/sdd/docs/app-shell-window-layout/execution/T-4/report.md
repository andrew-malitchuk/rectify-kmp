# app-shell-window-layout — T-4/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-4/report |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T21:07:40Z |
| updatedAt | 2026-10-14T21:07:40Z |

## Gate

### type

auto

### approvedBy

sdd-build-frontend-implement

### approved

true

### at

2026-10-14T21:07:40Z

## Provenance

### selfHash

c3a5f2d4d13a52dbe2ace25b96011189451de90cfc543a830f185f5b004d15e4

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-4

### type

ui

### outcome

success

### touchedPaths

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/handle/PanelResizeHandle.kt
- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/StageSurface.kt

### testRun

#### command

./gradlew :presentation-core-ui:desktopTest

#### passed

true

#### output

26 tests, 0 failures (T-3's suite intact; no new unit tests - this task is composables). Agent additionally rendered the compositions via renderComposeScene to PNG and compared against comp 02, then reverted the temporary build.gradle.kts wiring (verified absent from the commit).

### implementation

#### filesChanged

- PanelResizeHandle.kt
- StageSurface.kt

#### summary

PanelResizeHandle: 8dp hit strip, 1dp line2 hairline, Modifier.draggable(Horizontal), emitting onDelta/onDragStarted/onDragStopped only - no clamping, no persistence, does not own the width. StageSurface: Box on Theme.color.stage, no border/card/elevation (bezel-less per AC-31), Canvas dot grid with step precomputed from LocalDensity, required content slot on top.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-styling#Theme.stroke/color/spacing | line2 hairline, stage ground, ink3-derived dot colour |

#### authoredNew

- PanelResizeHandle - no draggable divider exists in the kit (verified across atom/ and molecule/)
- StageSurface - required by the plan contract

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| PanelResizeHandle.kt | commonMain has no E-resize PointerIcon (it is an AWT cursor, desktop-only). Used PointerIcon.Hand with an explanatory comment; a true resize cursor needs expect/actual, out of scope for this spec. | Handled in-task; see issue text. | warn |

### needsHumanReview

#### required

true

#### reason

Dot-grid cell size / radius / alpha are not design-specified. Chose DotCellSize=16.dp, DotRadius=0.75.dp, DOT_ALPHA=0.45f on Theme.color.ink3 (0.25f rendered effectively invisible at 1x; 0.45f matches comp 02's visible-but-faint ground). Kept as private file-level constants and KDoc'd as an unresolved design question, deliberately NOT promoted to a Theme token. Carried-forward open question from Stage 1.

### linkedAC

- AC-31
- AC-38
