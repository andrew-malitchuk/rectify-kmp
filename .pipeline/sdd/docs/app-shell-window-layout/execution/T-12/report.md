# app-shell-window-layout — T-12/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-12/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T21:25:03Z |
| updatedAt | 2026-10-14T21:25:03Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-14T21:25:03Z

## Provenance

### selfHash

fce236ae8807b76fe4adc9de1cd6aaa46778d435bc62fd5fe5206f15f0d3b784

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-12

### type

test

### outcome

partial

### touchedPaths


### testRun

#### command

ImageComposeScene render harness (TEMPORARY, reverted) ; ./gradlew :desktop-application:run

#### passed

true

#### output

Harness: 4 PNGs written at 1240x820, rendering RectifyShellContent itself (the real composition, not a preview copy), 40 re-renders per scene so async composeResources fonts finish loading. Live run: app started cleanly. Post-revert: 44 tests 0 failures, worktree clean, DEMO_MODE false.

### implementation

#### filesChanged


#### summary

Evidence set for the seven inspection-only ACs, written to execution/T-12/evidence/ in the MAIN repo (not the worktree): shell-{light,dark}-{no-session,session-active}.png. Verdicts - AC-12 PASS (three tabs in order, Notes selected, bezel-less stage; also pinned by ShellTabTest). AC-30 PASS with partial comparison (both palettes render completely and legibly, tokens swap consistently; NO dark design comp exists so the honest claim is 'renders correctly and consistently', NOT 'matches the design'). AC-31 PASS (no-session renders no panel AND no resize hairline; enforced structurally by the nullable slot). AC-34 PASS for geometry via live probe, resize DRAG unverified. AC-36 PASS (zero diff in presentation-core-styling). AC-37 PASS (slot order matches comp 02; intentional deltas: the AC-35 theme toggle absent from the comp, and a single NO SIGNAL placeholder instead of the comp's dashed outline + onboarding steps + Boot control, which are downstream scope). AC-40 WEAK EVIDENCE - the only signal-bearing element in the stub states is the Offline dot, which reads a neutral token; PaletteContrastTest is the real coverage, not these screenshots.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| AC-34 live resize floor | Dragging the window edge below 1024x700 cannot be exercised headlessly - screencapture and osascript are both permission-denied in this environment. Substituted a live-run readback of window.minimumSize/size on a scale=2.0 Retina display. The drag itself needs a human. | See issue; handled in-task or routed to the terminal wave. | warn |
| AC-33 live dispatch | No way to inject a real Cmd+1 event without a UI-test harness (explicitly out of scope per gate decision 5). shortcutToTab has 3 unit tests; the onPreviewKeyEvent branch is code-review-only. | See issue; handled in-task or routed to the terminal wave. | warn |
| TAC-5 fonts | The AC text says 'SF Pro elsewhere'. SF Pro appears NOWHERE in this repo - Typography.kt bundles roboto_flex.ttf and jetbrains_mono.ttf via composeResources. Both families resolve correctly in the renders; the proportional family is Roboto Flex. Recorded as a factual correction to the AC, not a defect. | See issue; handled in-task or routed to the terminal wave. | info |
| AC-30 dark theme | No dark design comp exists (Stage 1 carried open question). Claim deliberately limited to 'renders correctly and consistently'. | See issue; handled in-task or routed to the terminal wave. | info |

### needsHumanReview

#### required

true

#### reason

Three inspection items cannot be closed in this environment and need a human at a 2x Retina macOS display: (1) drag-resize actually stopping at 1024x700, (2) the macOS traffic lights visually sitting inside the 78dp toolbar inset - the three client properties are confirmed applied but were never seen, (3) Cmd+1/2/3 dispatch and theme-toggle persistence across a relaunch. The agent never saw the window: Screen Recording and Accessibility permissions are denied here, so every live claim comes from an in-process probe, quoted verbatim in the report.

### linkedAC

- AC-12
- AC-30
- AC-31
- AC-34
- AC-36
- AC-37
- AC-40
