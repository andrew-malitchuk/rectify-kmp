# app-shell-window-layout — T-11/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-11/report |
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

8655b595e91a65b4723433c02609906249973c20345d189c1f5129203a148e72

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-11

### type

ui

### outcome

success

### touchedPaths

- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt

### testRun

#### command

./gradlew :compose-application:compileKotlinDesktop ; 4x desktopTest --rerun-tasks

#### passed

true

#### output

BUILD SUCCESSFUL, 44 tests 0 failures. Composition rendered to four 1240x820 PNGs and compared against raw/design/02-empty-no-device.png. Orchestrator independently viewed shell-light-no-session.png and shell-dark-session-active.png and confirms the match.

### implementation

#### filesChanged

- RectifyWindow.kt

#### summary

internal RectifyShellContent(state, onPanelWidthCommit, onThemeCycle, modifier, sidePanel) holds the whole composition and is Koin/AWT/coroutine-free, so it renders in isolation - which is what made the T-12 evidence possible. Toolbar: SegmentedButtonGroup(Android|iOS), DeviceChip(None), CounterBadge(0, Empty, staged), AppButton(Clear, Ghost, disabled), AppButton(Resolve & Send, shortcut, disabled). Status bar: StatusIndicator(Offline, 'Waiting for device...'), Text('Rectify 0.4', monoMeta, ink2), IconButton(Sun) cycling System->Light->Dark via SetThemeUseCase. sidePanel=null so neither panel nor handle composes (AC-31) - expressed as the slot value itself, which the key handler also tests, so the two cannot drift. Zero Color(0x...), zero raw .dp at a call site, zero hardcoded TextStyle, all authored text on ink2.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-ui#organism/shell/{RectifyWindowShell,ShellToolbar,ShellStatusBar} | Slot contract assembled, not modified |
| presentation-core-ui#{SegmentedButtonGroup,DeviceChip,CounterBadge,AppButton,StatusIndicator,IconButton,Sun} | Every slot filled from shipped components; zero new kit components authored |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| status bar trailing slot | IconButton(Small) has a 32dp min height inside a 28dp status bar so it renders compressed to 28dp. Checked in the renders: the 16dp Sun glyph centres cleanly, nothing clips. Left alone rather than adding a size override. | See issue; handled in-task or routed to the terminal wave. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-31
- AC-35
- AC-37
