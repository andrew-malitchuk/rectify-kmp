# app-shell-window-layout — T-3/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-3/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-14T20:53:18Z |
| updatedAt | 2026-10-14T20:53:36Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-14T20:53:36Z

## Provenance

### selfHash

bcd25254e4c14d323233d2aff2e676b8c16417607f4e3d30bba9bd27674bd403

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-3

### type

business-logic

### outcome

success

### touchedPaths

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellTab.kt
- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellWindowDefaults.kt
- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellState.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ClampPanelWidthTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShortcutToTabTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellTabTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellWindowDefaultsTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellStateTest.kt

### testRun

#### command

./gradlew :presentation-core-ui:desktopTest

#### passed

true

#### output

ClampPanelWidthTest 4, ShellStateTest 11, ShellTabTest 3, ShellWindowDefaultsTest 5, ShortcutToTabTest 3 = 26 tests, 0 failures, 0 errors. Also green: :presentation-core-ui:compileKotlinIosSimulatorArm64 (confirms Key.One/Two/Three resolve on native).

### implementation

#### filesChanged

- ShellTab.kt
- ShellWindowDefaults.kt
- ShellState.kt
- +5 commonTest files

#### summary

Three-entry ShellTab (Notes/Payload/History, canonical order) with pure shortcutToTab(isMeta,key); ShellWindowDefaults constants (1240x820 initial, 1024x700 min, panel 320 default / 260 min / 480 max) plus clampPanelWidth; @Stable ShellState with mutableStateOf-backed activeTab/panelWidth, clamped construction, commit-only-if-changed onDragStopped(), onSessionStarted() resetting only the tab. No Orbit MVI, no ViewModel, no new Gradle module, no core/ dir.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-ui#molecule/bar/tab/SidebarTabs | ShellTab labels and ordering target the shipped SidebarTabs row; fixes copy to Notes/Payload/History rather than SidebarTabs' KDoc 'Annotations' |
| presentation-core-styling#Theme | No colour/typography introduced; shell files declare geometry constants only, following SidebarTabs' precedent |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| ShellState.kt | CONFLICTING_JVM_DECLARATIONS: the plan's contract declared both `var panelWidth private set` and `fun setPanelWidth(Dp)` - identical JVM signature setPanelWidth-0680j_4(F)V. Resolved by backing panelWidth with a private state field and exposing a read-only val; the public API in the plan is unchanged. | See issue text; handled in-task or routed onward. | warn |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-12
- AC-32
- AC-33
- AC-34
- AC-38
