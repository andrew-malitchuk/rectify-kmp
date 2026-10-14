# app-shell-window-layout — T-7/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-7/report |
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

928205adf906df93211d828b8eae1367188565154c6306fbf1a81ecda5ffaa00

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-7

### type

ui

### outcome

success

### touchedPaths

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt
- presentation-core-ui/CLAUDE.md

### testRun

#### command

./gradlew :presentation-core-ui:desktopTest ; :compose-application:compileKotlinDesktop ; :presentation-core-ui:compileKotlinIosSimulatorArm64

#### passed

true

#### output

All green post-merge. Four previews render. Agent rendered all four compositions at 1240x820 to PNG against comp 02: toolbar inset, stage texture, panel tabs, status bar and the no-panel branch all match. The no-panel render shows neither hairline nor panel, confirming AC-31 visually as well as by code.

### implementation

#### filesChanged

- RectifyWindowShell.kt
- presentation-core-ui/CLAUDE.md

#### summary

THE DELIVERABLE'S PUBLIC API. Column(fillMaxSize, Theme.color.bg)[toolbar(), Row(weight 1f)[StageSurface(weight 1f, content=stage), if (sidePanel != null) { PanelResizeHandle; SidePanelFrame(width=state.panelWidth) }], statusBar()]. sidePanel is a NULLABLE SLOT TYPE, so a caller cannot express 'panel with no content' - the AC-31 hidden-until-session rule is a compositional branch, not a visibility flag. Handle wired onDelta={state.resizePanelBy(-it)} (handle sits on the panel's leading edge), onDragStopped={state.onDragStopped()?.let(onPanelWidthCommit)} - the shell itself persists NOTHING, matching the drag-end-only decision. Four previews: no-panel/with-panel x Light/Dark.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-ui#organism/shell/{ShellState,ShellTab,ShellWindowDefaults,clampPanelWidth} (T-3) | State, tabs and geometry reused as-is; nothing re-declared |
| presentation-core-ui#organism/shell/{StageSurface,ShellToolbar,ShellStatusBar,SidePanelFrame} + atom/handle/PanelResizeHandle | Assembled from T-4/T-5/T-6 in post-order |

#### authoredNew

- RectifyWindowShell

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| RectifyWindowShell.kt | The dashed phone outline and 'NO SIGNAL' copy live as a PRIVATE preview-only composable, not shipped stage content. T-4's backlog outOfScope explicitly assigns that copy downstream, and StageSurface's plan contract has a required non-defaulted content slot. The visual is verified by render but is not public API. Orchestrator confirmed both ink3 text uses are inside private preview composables, so the ink2-for-authored-text gate decision is not violated. | Handled in-task; see issue text. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-12
- AC-30
- AC-31
