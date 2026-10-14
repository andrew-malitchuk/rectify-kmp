# app-shell-window-layout — T-5/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-5/report |
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

bdb8b6e94e04ee2b5ca683554b75422e39a1976b5807d3035fca6cf2199cbbdf

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-5

### type

ui

### outcome

success

### touchedPaths

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellToolbar.kt
- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellStatusBar.kt

### testRun

#### command

./gradlew :presentation-core-ui:desktopTest

#### passed

true

#### output

26 tests, 0 failures; module compiles on desktop and iosSimulatorArm64.

### implementation

#### filesChanged

- ShellToolbar.kt
- ShellStatusBar.kt

#### summary

ShellToolbar: 52dp chrome bar, bottom hairline, SIX FIXED NAMED SLOTS in canonical order - leading Spacer(ShellWindowDefaults.TrafficLightInset) reserving room for the macOS traffic lights, platformSwitch, deviceIndicator, Spacer(weight 1f), stagedCounter, clearAction, primaryAction. Deliberately not a free-form RowScope, so downstream specs cannot reorder the canonical chrome. ShellStatusBar: 28dp chrome bar, top hairline, leading/trailing RowScope slots either side of Spacer(weight 1f). All content STUBBED per AC-37 - no device logic, no adb/idb import.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-ui#molecule/button/SegmentedButtonGroup | Android\|iOS platform switch |
| presentation-core-ui#molecule/chip/DeviceChip | DeviceChipState.None renders the dot + 'No device' |
| presentation-core-ui#atom/badge/CounterBadge | CounterBadgeState.Empty, count 0, label 'staged' |
| presentation-core-ui#atom/button/AppButton | Ghost disabled 'Clear'; Primary disabled 'Resolve & Send' using AppButton's own shortcut parameter |
| presentation-core-ui#atom/indicator/StatusIndicator | StatusIndicatorState.Offline, label 'Waiting for device...' |

#### authoredNew

- ShellToolbar and ShellStatusBar - the two container organisms only. ZERO new leaf components.

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| ShellToolbar.kt | KeyCap was briefed as the Cmd+Enter affordance but is deliberately NOT used: comp 02 shows the glyph as flat muted text inside the button, and AppButton already ships a `shortcut` parameter whose own KDoc example is literally 'Resolve & Send' + the glyph. Using KeyCap would have added a bordered cap the comp does not show. | Handled in-task; see issue text. | info |
| ShellStatusBar.kt | StatusPill and StatusLine were read and rejected: StatusLine models a multi-step pipeline (Streaming > Frozen > ...) and StatusPill is a seg-filled chip. The status bar needs a bare dot + label, which is exactly StatusIndicator. | Handled in-task; see issue text. | info |
| ShellStatusBar.kt | Status-bar copy follows comp 02's neutral 'Waiting for device...', NOT comp 01's green 'adb ... ready' - the green dot requires downstream tool detection this spec does not build. | Handled in-task; see issue text. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-37
