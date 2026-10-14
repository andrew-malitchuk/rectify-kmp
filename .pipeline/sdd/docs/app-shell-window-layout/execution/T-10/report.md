# app-shell-window-layout — T-10/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-10/report |
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

8a62fe0adc87a5c6fbf0efdaf91ffaf5e71f8f44760aacd02e9eba16a3fdaf18

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 0626dbba1debc8671ab3d626c7c2308eb38fe0d5ee43282ab82f0fbd86331a0e |

## Data

### taskId

T-10

### type

business-logic

### outcome

success

### touchedPaths

- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt
- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt
- compose-application/build.gradle.kts

### testRun

#### command

./gradlew :compose-application:compileKotlinDesktop :desktop-application:compileKotlin ; 4x desktopTest --rerun-tasks ; :desktop-application:run

#### passed

true

#### output

BUILD SUCCESSFUL. 44 tests, 0 failures. THE APP REALLY RAN: JVM started, Skiko macOS arm64 renderer loaded, no exception, process alive until killed. Live AWT probe: size=1240x820 min=1024x700 scale=2.0 fullWindowContent=true transparentTitleBar=true windowTitleVisible=false. Probe instrumentation removed before commit.

### implementation

#### filesChanged

- RectifyWindow.kt
- DesktopEntry.kt
- compose-application/build.gradle.kts

#### summary

ApplicationScope.RectifyWindow(): rememberWindowState(ShellWindowDefaults.InitialSize); FrameWindowScope LaunchedEffect(window) sets minimumSize from ShellWindowDefaults.MinimumSize and, behind an os.name startsWith(Mac) guard inside runCatching, the three apple.awt rootPane client properties with a Logger.w fallback. onPreviewKeyEvent -> private handleShellShortcut() calling the pure shortcutToTab, returning true ONLY when a tab matched AND the panel slot is non-null. GetSidePanelWidthUseCase read once in LaunchedEffect(Unit); SetSidePanelWidthUseCase called from onPanelWidthCommit in a rememberCoroutineScope - drag-end only. ObserveThemeUseCase -> ThemeMode with System as both initial and failure value. runDesktopApp() now renders RectifyWindow; desktop-application/Main.kt has ZERO diff.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| presentation-core-ui#organism/shell/ShellWindowDefaults | Initial/minimum size and traffic-light inset read from the object; no number re-declared in the window layer |
| presentation-core-ui#organism/shell/{shortcutToTab,ShellState} | Key mapping and tab/width state reused as-is |
| domain-usecase-api#configuration/{Get,Set}SidePanelWidth + Observe/SetTheme | Injected via koinInject; no new repository or preference |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| compose-application/build.gradle.kts | desktopMain needed compose.material3 (Text) and kermit (Logger.w fallbacks); neither was on the classpath. Two implementation lines added to compose-application's own block - no lower-layer module touched. | See issue; handled in-task or routed to the terminal wave. | info |
| ApplyWindowChrome | AWT transiently reports 1024x700 while minimumSize is applied, settling to 1240x820 before the window is shown (isVisible=false at that point). Measured, not assumed; no user-visible effect. | See issue; handled in-task or routed to the terminal wave. | info |
| CLAUDE.md | Root CLAUDE.md line 56 still describes the desktop entry as 'runDesktopApp() -> application { Window { App() } }', now stale. Executor correctly did not edit a repo doc on its own authority; orchestrator routed it to the terminal wave. | See issue; handled in-task or routed to the terminal wave. | warn |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-33
- AC-34
- AC-35
- AC-39
- NFR-10
