# app-shell-window-layout — changelog

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | changelog |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-14T22:38:53Z |
| updatedAt | 2026-10-14T22:38:53Z |

## Gate

### type

auto

### approvedBy

sdd-build-validation-verdict

### approved

true

### at

2026-10-14T22:38:53Z

## Provenance

### selfHash

434a29cc20c4881877d747aae1c941d12042bc78d028d1829d8c9b84035131eb

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | b304963f333fed1a5bc8d7a5c171b6ff98e19791a2a68b805eae4cd4e0a9f112 |

## Data

### entries

| at | task | summary | kind | filesChanged | changeRequest |
| --- | --- | --- | --- | --- | --- |
| dc9d7b0 | preparation | Preparation stage complete for app-shell-window-layout. | added |  |  |
| 2a5aa01 | plan | Plan complete: 12 AC / 12 FR / 3 NFR / 5 SC, 14-row traceability matrix, survey including the Q1/Q2/Q3 contrast-policy gate answers. | added |  |  |
| 4980db8 | backlog | Backlog complete: 13 tasks across 4 execution waves. | added |  |  |
|  | T-1 | Wired kotlin-test into commonTest for LibraryConventionPlugin and FeatureConventionPlugin -- the first test infrastructure anywhere in this repo (previously zero test files existed). | added | ["gradle/libs.versions.toml", "build-logic/convention/src/main/kotlin/.../LibraryConventionPlugin.kt", "build-logic/convention/src/main/kotlin/.../FeatureConventionPlugin.kt"] |  |
|  | T-2 | WCAG contrast sweep (WcagContrast.kt sRGB relative-luminance implementation + PaletteContrastTest, 54 pairings per palette at ship time) over both shipped palettes, per the gate's Q1/Q2/Q3 policy answers. Zero edits to presentation-core-styling/src/commonMain -- mechanical proof tokens were adopted unmodified (AC-36). | added | ["presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/contrast/WcagContrast.kt", "presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt"] |  |
|  | T-3 | Shell pure logic and state: ShellTab, shortcutToTab, ShellWindowDefaults, clampPanelWidth, ShellState, each with a dedicated unit-test suite (26 tests total). | added |  |  |
|  | T-4 | PanelResizeHandle atom (onDelta/onDragStarted/onDragStopped, no internal clamping or persistence) and StageSurface (dotted bezel-less stage region, dot grid drawn once per visible area). | added |  |  |
|  | T-5 | ShellToolbar (six fixed named slots) and ShellStatusBar organisms, assembled entirely from shipped kit components -- no new leaf component authored. | added |  |  |
|  | T-6 | SidePanelFrame reusing the existing SidebarTabs molecule with exactly three tabs (Notes/Payload/History). | added |  |  |
|  | T-7 | RectifyWindowShell composes the full slot contract (toolbar/stage/statusBar required, sidePanel nullable); when sidePanel is null neither PanelResizeHandle nor SidePanelFrame composes (a genuine compositional branch, not a visibility modifier). Four previews (no-panel/with-panel x Light/Dark). | added |  |  |
|  | T-8 | pref_side_panel_width wired through the full preference chain (new key; pref_theme untouched): PreferenceSource/Impl, Mapper, ConfigureRepository, Get/SetSidePanelWidthUseCase, both appended at the end of their respective layer groups. | added |  |  |
|  | T-9 | Flipped the effective theme default from Light to System: ThemeModel.MaterialU renamed to System (stored value "materialu" preserved for backward compatibility), App.kt null-branch and initial state both resolve to ThemeMode.System. | changed |  |  |
|  | T-10 | RectifyWindow: 1240x820 initial / 1024x700 minimum window geometry via rememberWindowState + AWT minimumSize, macOS-only traffic-light chrome behind an os.name guard, Cmd+1/2/3 key handling, theme and side-panel-width wiring. runDesktopApp() now renders RectifyWindow (desktop-application/Main.kt unchanged). | added |  |  |
|  | T-11 | Filled every toolbar/status-bar slot with static stubs matching design comp 02, and added the status-bar theme-override control (System -> Light -> Dark cycle, persists across relaunch). | added |  |  |
|  | T-12 | Inspection evidence pass: four in-process Compose render PNGs (light/dark x no-panel/with-panel) plus supporting evidence captured later in the cycle. | added |  |  |
|  | T-13 | Verified the PRD green-signal hue-150 reconciliation as a no-op: '152' absent from PRD docs and prd-handoff.json; no plan/preparation file or Kotlin source modified. | changed |  |  |
| a71d232 | T-3 | Fixed three commonTest backtick function names containing a comma/apostrophe, illegal on Kotlin/Native. Desktop had compiled fine so every per-task gate was honest but incomplete; compileTestKotlinIosArm64/IosSimulatorArm64 was failing and breaking ./gradlew build repo-wide. This was caught by Stage 5's full-build regression check, not by any per-task gate. | fixed | ["data-repository-impl/src/commonTest/kotlin/data/repository/impl/SidePanelWidthPreferenceMapperTest.kt", "presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt", "presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellTabTest.kt"] | regression |
| c20d4e4 | T-2 | Cleared three audit-found defects approved at the gate: rewrote .claude/rules/testing.md (it falsely claimed zero tests exist and prescribed T-1's already-finished work as future work -- this file auto-loads into every agent's context); fixed domain-core/README.md:21's stale ThemeModel(..., MaterialU) reference (last MaterialU string outside .pipeline); added the four PaletteContrastTest pairings its own guard test had silently omitted (onPrimary over primaryHover/primaryPress, both palettes -- both named by AC-36), raising the guard count 54 -> 56. | fixed | [".claude/rules/testing.md", "domain-core/README.md", "presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt"] | AF-1 / AF-2 / AF-6 |
| 29e6c84 | T-5 | Fixed the disabled 'Resolve & Send' AppButton, which used Theme.color.chrome as its disabled fill -- the same colour as the toolbar it sits on, rendering it invisible. Switched to Theme.color.seg; seg over chrome composites to (223,223,221), an exact match for the measured design value. | fixed | ["presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt"] | human-gate (inspection) |
| 1e3f539 | T-5 | Fixed the macOS traffic-light leading inset, measured 21dp short of the design: the build reserved 78dp with a 13dp gap after the green light, the design puts the first toolbar control at 102dp with a 34dp gap. Corrected 78dp -> 99dp; the pre-existing ShellWindowDefaultsTest pinned the old value and was updated with the measurement rationale. Re-verified in the real running app: gap now exactly 34pt. | fixed | ["presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellWindowDefaults.kt", "presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellWindowDefaultsTest.kt"] | human-gate (inspection) |
| 9dd3806 | T-12 | Added the A/B toolbar-vs-design comparison screenshot and the operator's real running-app screenshot, closing the two remaining human-gate inspection items (AC-34 window-chrome placement and theme rendering) with direct visual evidence rather than the in-process renders alone. | added | ["execution/T-12/report.json", "execution/T-12/report.md"] |  |
| 6f78fa5 | execution | Execution stage complete: all 13 tasks done, 44 tests across 9 suites (the first test suite of any kind in this repo), full cross-target build green. | changed |  |  |

### summary

app-shell-window-layout replaces the template's single-pane desktop entry point with RectifyWindowShell: a toolbar/stage/status-bar/side-panel slot contract, System-default theming with persistence, Cmd+1/2/3 tab-mapping, a clamped resizable side panel with persisted width, and macOS-only traffic-light chrome -- backed by the repo's first-ever test suite (44 tests, 9 suites, introduced from zero by T-1). Stage 5 found and fixed one repo-wide regression (illegal Kotlin/Native test names breaking the full build), two visual defects found only by human inspection against the design comp (an invisible disabled button, a 21dp-short macOS chrome inset), one audit-found contrast-sweep gap (two untested token pairings), and two stale docs (an auto-loading rule file and a module README) that this spec's own changes had made false. Seven items are explicit human waivers -- most notably ratifying a change to a previously sealed Q1-A survey answer on hairline contrast, which is a deviation from a human decision rather than merely from a dispatch brief.

### regressionRunPassed

true
