# app-shell-window-layout — candidates/candidate-1

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | candidates/candidate-1 |
| status | done |
| producedBy | sdd-build-plan-draft |
| schemaVersion | 1 |
| createdAt | 2026-10-14T18:59:33Z |
| updatedAt | 2026-10-14T18:59:33Z |

## Gate

### type

auto

### approvedBy

sdd-build-plan-draft

### approved

true

### at

2026-10-14T18:59:33Z

## Provenance

### selfHash

010a71c30e34b2ead94e019806befba01e1c0e85ff878f3e03969abc08c42431

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | b2550ab6226dcf9b1e5cedd7badbf431cb6b43c8f30680dd1585dadbc579ebb0 |

## Data

### meta

#### title

App Shell & Canonical Window Layout — Implementation Plan

#### version

1

#### status

draft

### goal

Ship the canonical Rectify macOS window shell inside the rectify-kmp repository — a toolbar frame, a bezel-less live-mirror stage, a resizable three-tab side panel frame (Notes/Payload/History) and a status bar — assembled from the already-shipped Rectify UI Kit (presentation-core-styling tokens + ~109 presentation-core-ui components), wired to the existing AppTheme/ThemeMode and multiplatform-settings chains, with every downstream-owned region exposed as a typed composable slot and every slot stub-filled. The slot contract (RectifyWindowShell) is the deliverable's real public API: it is what android-device-discovery-mirroring and every later spec builds against.

### problemStatement

No shared window shell exists yet. All 16 catalog screens and every downstream feature (device discovery, annotation, payload, history) need a single canonical layout to render inside; without it each feature would improvise its own window chrome. A complete light/dark design-token system (ThemeColor, AppTheme) and ~109 Rectify-specific UI-kit components authored from the same style guide already exist in rectify-kmp (presentation-core-styling / presentation-core-ui) — this shell must be built there and assemble them rather than re-author or fork them. The desktop entry point currently opens an un-sized window rendering App() → NavigationHost() (the template app), which is not the Rectify shell.

### highLevelDescription

Three additions, no new Gradle module. (1) presentation-core-ui gains a shell organism family under source/kit/organism/shell/ plus one atom (PanelResizeHandle): RectifyWindowShell = Column[ShellToolbar, Row[StageSurface(weight 1f), PanelResizeHandle, SidePanelFrame(panelWidth)], ShellStatusBar] on Theme.color.bg, with the side panel expressed as a NULLABLE slot so the pre-mirror state (AC-31) is a type, not a flag. Shell state (activeTab, panelWidth) is plain Compose state hoisting via rememberShellState — no Orbit MVI, no ViewModel, so the shell stays previewable and usable from desktopMain without Koin. (2) compose-application/desktopMain gains RectifyWindow: rememberWindowState(1240x820), AWT window.minimumSize 1024x700, macOS full-size-content rootPane client properties so the OS traffic lights sit inside the Rectify toolbar behind a reserved leading inset, onPreviewKeyEvent → shortcutToTab for Cmd+1/2/3, theme resolved through the existing ObserveThemeUseCase, panel width loaded once at launch and written only on drag-end. runDesktopApp() renders RectifyWindow instead of App(). (3) One new persisted preference, pref_side_panel_width (Int dp, default 320), added through the full PreferenceSource → mapper → ConfigureRepository → use-case chain; pref_theme is reused unchanged, with only its effective default flipping from Light to System. Alongside this, the repository's FIRST test suite: kotlin.test wired once into the Library/Feature convention plugins, five pure-logic test files, two preference round-trip tests, and an automated WCAG contrast sweep over both shipped palettes living in presentation-core-styling's own commonTest (the palettes are internal).

### background

Stage 1 (Preparation) is complete and human-approved (readyForPlan=true) with six binding decisions taken at the brainstorm gate: build in this repo assembling the existing UI Kit; native decorations + macOS full-size-content window; plain Compose state hoisting in presentation-core-ui + compose-application with no new module and no Orbit MVI (orbit-mvi.md's own frontmatter scopes it to presentation-feature-*/**); reuse of the multiplatform-settings chain with pref_theme unchanged and pref_side_panel_width added, persisted on drag-END only because java.util.prefs flushes to disk; kotlin.test only, wired once in the convention plugins, with a WCAG contrast sweep over both palettes; and the shipped sentFixed token oklch(0.66 0.15 150) / Color(0xFF3EAB5E) authoritative over the PRD's hue 152 (amend the doc, change no code). This plan implements those decisions and does not revisit them. NOTE: .pipeline/sdd/docs/registry/ does not exist — there is no components.json/assets.json/styles.json/features.json/bugs.json. Reuse below was therefore established by direct source inspection of this repository (file paths and signatures read, not inferred), and knownRisksFromQA is empty because no bug-recurrence history exists to draw on.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | preparation/preparation.json (Stage 1, human-approved, readyForPlan=true) | .pipeline/sdd/docs/app-shell-window-layout/preparation/preparation.json |
| code | preparation/clarify.json — 12 FR, 3 NFR, 12 AC, SC-1..SC-5, 7 user-answered survey entries | .pipeline/sdd/docs/app-shell-window-layout/preparation/clarify.json |
| code | preparation/brainstorm.json — 12 components, 6 alternatives, 7 openDecisions, 13 readyMadeSolutions, 6 user-answered survey entries | .pipeline/sdd/docs/app-shell-window-layout/preparation/brainstorm.json |
| figma | Claude Design project 'Rectify Screens' — 01 First run, platform & source (LIGHT). Authoritative for toolbar order: traffic lights, Android\|iOS segmented switch, '● No device ▾' chip, then right-aligned '0 staged', 'Clear', disabled 'Resolve & Send ⌘↵'. Status bar: left '● adb 35.0.1 · idb 1.1.8 · ready' (mono), right 'Rectify 0.4'. No side panel. URL must not be re-fetched. | raw/design/01-first-run-platform-source.png |
| figma | Claude Design project 'Rectify Screens' — 02 Empty, no device yet (LIGHT). Authoritative for the bezel-less stage: full-bleed dotted texture on the stage ground, a dashed rounded-rectangle 'NO SIGNAL' device outline (downstream content), right-hand 'Waiting for an Android device' copy (downstream content), status bar left '● Waiting for device…'. No side panel, no bezel, no card around the stage. | raw/design/02-empty-no-device.png |
| code | operator dictation — scope boundary: specify the shell's slots, regions, tokens and theming; do not implement device discovery | inputs/operator-dictation.md |
| code | presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/ — ThemeColor.kt (22-token contract), LightColor.kt + DarkColor.kt (both internal), AppTheme.kt (already resolves Light/Dark/System via isSystemInDarkTheme()) |  |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/ — 101 component files, strict atom → molecule → organism layering |  |
| code | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt + desktop-application/src/main/kotlin/dev/rectify/io/source/entry/Main.kt — the working macOS entry point |  |
| code | data-preference-api / data-preference-impl / data-repository-impl / domain-repository-api / domain-usecase-api / domain-usecase-impl — the end-to-end pref_theme path to be mirrored for pref_side_panel_width |  |
| code | CLAUDE.md + all 19 files in .claude/rules/ (no .claude/skills/, no .claude/agents/ exist in this repo) |  |

### scope

#### in

- Build location: this repository, /Users/andrew-malitchuk/StudioProjects/rectify-kmp, which is both the spec workspace (.pipeline/ already relocated here) and the build target. Stage 4 writes Kotlin here; Stage 5 runs Gradle test tasks here.
- RectifyWindowShell slot contract (organism) — the deliverable's public API surface that downstream specs build against: Column[ShellToolbar, Row[StageSurface, PanelResizeHandle, SidePanelFrame], ShellStatusBar] with a nullable sidePanel slot (FR-11, FR-14, FR-15, AC-12, AC-31).
- ShellToolbar (organism) with six NAMED slots in canonical screenshot order — trafficLightInset (reserved width only), platformSwitch, deviceIndicator, stagedCounter, clearAction, primaryAction — so downstream specs cannot reorder the canonical toolbar (FR-12, AC-37).
- ShellStatusBar (organism) with leading/trailing slots, stub-filled (FR-13, AC-37).
- StageSurface (shell region) — bezel-less, Theme.color.stage ground plus a Canvas-drawn dotted texture, with a content slot for the downstream empty-state/mirror content (FR-14, AC-31).
- SidePanelFrame (organism) reusing the shipped SidebarTabs molecule with exactly three tabs Notes/Payload/History, Notes active by default, content as a per-tab slot (FR-11, FR-15, FR-16, AC-12, AC-32).
- PanelResizeHandle (atom) + pure clampPanelWidth: live resize clamped to 260..480dp, width persisted on drag-END only and only when it changed (FR-20, FR-21, AC-38, AC-39).
- Cmd+1/2/3 tab switching via a pure shortcutToTab(isMeta, key) mapping installed through onPreviewKeyEvent, in addition to click (FR-16, AC-33).
- Window geometry: initial 1240x820 via rememberWindowState, minimum 1024x700 via AWT window.minimumSize inside FrameWindowScope (FR-17, AC-34).
- macOS native decorations + full-size-content window (apple.awt.fullWindowContent / apple.awt.transparentTitleBar / apple.awt.windowTitleVisible on window.rootPane) with a reserved leading inset in the toolbar for the OS traffic lights.
- Theme: ThemeMode.System as the effective first-run default with a persisted manual override, wired through the already-shipped AppTheme + pref_theme chain; the only behavioural change is the mapper/initial fallback flipping from Light to System (FR-18, FR-19, AC-35, AC-36, AS-5).
- Adoption of the existing presentation-core-styling token system UNMODIFIED — every neutral and signal token read behind the Theme singleton, zero Color(0x...) literals and zero hardcoded TextStyles at shell call sites (FR-19, FR-23, AC-36, AC-40).
- One new persisted preference, pref_side_panel_width (Int dp, default 320), through the full PreferenceSource → mapper → ConfigureRepository → use-case chain, registered at the END of each Koin layer group (FR-21, AC-39).
- The repository's first test suite: kotlin.test wired ONCE into LibraryConventionPlugin and FeatureConventionPlugin; pure-function tests (panel-width clamp 260/480, Cmd+1/2/3 mapping, three-tab assertion, window constants, drag-end commit-only-if-changed); preference round-trip tests; and an automated WCAG contrast sweep over BOTH shipped palettes in presentation-core-styling's own commonTest (NFR-11, AC-36, AC-40).
- Light and dark @Preview pairs for the no-panel and with-panel shell states, as inspection evidence for the inspection-only ACs (AC-30, AC-31).
- Documentation amendment: the PRD's green-signal note changes from oklch hue 152 to the shipped 150; no code changes.

#### out

- Device discovery, adb/idb detection, device-picker content, emulator boot — belongs to android-device-discovery-mirroring.
- Notes, Payload and History tab CONTENT — only the three-tab frame and the per-tab content slot are in scope.
- The stage's 'NO SIGNAL' device outline and the 'Waiting for an Android device' copy visible in screenshot 02 — those are stage CONTENT filled by the downstream spec through StageSurface's content slot.
- Annotation and measurement tooling on the stage.
- Resolve & Send / MCP wiring — the toolbar only reserves the control slot, rendered disabled.
- Live wiring of toolbar/status bar to real tool-version or device state — static placeholder content only (AC-37).
- Authoring or editing any token value, including the 2-degree sentFixed hue question (binding decision 6: shipped value wins, change no code).
- Windows/Linux builds (NFR-10); iOS/Android behaviour of the shell — the existing App()/NavigationHost root stays untouched on those platforms.
- Compose UI-test harness (runComposeUiTest / ui-test-junit4) — deliberately excluded (binding decision 5).
- macOS packaging (nativeDistributions / packageDmg) — a known gap, not this deliverable.
- Deciding the fate of this repo's existing template features (onboarding/home/settings/about/splash, Navigation3, Room) now that the desktop root bypasses NavigationHost — carried forward as an open question, NOT decided here.
- Any write to .pipeline/sdd/docs/registry/ — no registry exists; the orchestrator owns any future registry delta.

### assumptions

- AS-1: The 1240x820 window size and the OKLCH signal-color values captured as 'unverified design context' in the upstream PRD handoff are confirmed spec as of the clarify survey.
- AS-2: Device discovery, device-picker content, annotation, and payload/history tab content are implemented entirely in downstream specs; this deliverable only defines the slots/frames those features render into.
- AS-3: macOS system-appearance detection is available through Compose Multiplatform Desktop / Skiko (isSystemInDarkTheme()) on JVM 21 without native interop. AppTheme already branches on it, but a LIVE appearance switch has never been observed on a shipped desktop build — treated as an open verification item, spiked in step 10 before the override toggle is built.
- AS-4: This deliverable adopts the existing Rectify UI Kit token system rather than authoring a palette. The four signal colors are identical across light and dark by design, so theme contrast parity is achieved via surface/ink pairing per theme, not by varying signal values.
- AS-5: AppTheme's own parameter default is ThemeMode.Dark; the shell passes the resolved mode explicitly (System on first run) rather than relying on that default.
- The desktop JDK in use exposes the JetBrains/Apple AWT client properties (apple.awt.fullWindowContent, apple.awt.transparentTitleBar). If absent, native decorations degrade to a standard title bar above the toolbar — functional, ugly, zero extra code.
- presentation-core-ui's commonTest source set can see the module's own commonMain implementation dependencies (Compose runtime, kotlinx-coroutines), so ShellState and the Dp-based pure functions are unit-testable without a Compose UI harness.
- Running the desktop app to capture light+dark inspection screenshots is available to Stage 5 (./gradlew :desktop-application:run on a macOS host); if it is not, every inspection-only AC falls back to code review plus the @Preview pairs.

### constraints

| type | text |
| --- | --- |
| tech | Built inside /Users/andrew-malitchuk/StudioProjects/rectify-kmp; the .pipeline/ SDD workspace already lives here, so spec, code and design system share one git repo. |
| tech | Kotlin 2.4.21 / Compose Multiplatform 1.12.1 / JVM 21, macOS host via compose.desktop.currentOs. No AppKit, SwiftUI or Electron. Versions come from gradle/libs.versions.toml — never hardcoded in a module build file (.claude/rules/convention-plugins.md). |
| tech | .claude/rules/design-tokens.md: every token is read behind the Theme singleton from @Composable scope. No literal Color(0x...), no raw .dp and no hardcoded TextStyle at a call site. AppTheme is the single provider. Strict atom → molecule → organism import direction. Component-OWN geometry (e.g. toolbar height, dot cell size) follows the existing precedent of private file-level Dp vals inside the component file (SidebarTabs' TabHeight, AppButton's per-size metrics) — it is not a Theme token and must not be inlined at a call site either. |
| tech | .claude/rules/package-layout.md: core/ holds internal-only declarations, source/ holds the public API, no empty core/ directories. explicitApi() is enforced repo-wide by the convention plugins — every public declaration needs an explicit visibility modifier AND an explicit return type. |
| tech | .claude/rules/preferences.md: an existing storage key is NEVER renamed. pref_theme stays exactly as-is; the new key is pref_side_panel_width, declared as a const val KEY_* in the impl, following the pref_<name> convention. |
| tech | .claude/rules/koin-di.md: initKoin()'s module list is load-order significant (data → domain → presentation); new bindings are APPENDED to the end of their layer group, never reordered. One Koin module per Gradle module. |
| tech | .claude/rules/orbit-mvi.md is scoped by frontmatter to presentation-feature-*/** — it does not apply to a layout organism, which is why plain state hoisting is correct here rather than a deviation. |
| tech | .claude/rules/testing.md: ZERO tests exist in this repo today. Test dependencies are wired once in the Library/Feature convention plugins, never module-by-module. .claude/rules/version-catalog-hygiene.md: a new catalog entry must be wired into a module in the same change. |
| business | v1 ships macOS only (NFR-10). The Android/iOS targets keep compiling but no shell behaviour is specified for them. |

### dependencies

| on | note |
| --- | --- |
| presentation-core-styling (:presentation-core-styling, in-repo) | Source of ThemeColor (22 tokens), attributeLightColorPalette / attributeDarkColorPalette (both internal), AppTheme(mode, reducedMotion), ThemeMode.Light/Dark/System, and the spacing/typography/radius/corner/stroke/motion/size tokens. Consumed as a plain project dependency — 'adopted unmodified' is satisfied by construction, not by discipline. Already a dependency of presentation-core-ui. |
| presentation-core-ui (:presentation-core-ui, in-repo) | Host module for the new shell organism family. Already depends on presentation-core-styling and compose.material3; already a dependency of compose-application. No new Gradle wiring needed. |
| :desktop-application → initKoin() → runDesktopApp() | The working macOS entry point (./gradlew :desktop-application:run). This deliverable extends compose-application/desktopMain only; desktop-application/Main.kt is unchanged (initKoin() stays in main() per that module's CLAUDE.md). |
| multiplatform-settings preference chain | data-preference-api/-impl (desktop backend PreferencesSettings on node dev.rectify.io), data-repository-impl mappers + ConfigureRepositoryImpl, domain-repository-api, domain-usecase-api/-impl. Extended with one new key; the theme path is reused. |
| build-logic/convention (composite build) | LibraryConventionPlugin and FeatureConventionPlugin gain the one-time kotlin.test wiring. This is the only change to build-logic and it blocks every test in the plan. |
| android-device-discovery-mirroring (downstream) | Consumes ShellToolbar.deviceIndicator / platformSwitch, StageSurface.content and SidePanelFrame.content. Not a blocking dependency; the slot signatures in this plan are the contract it will be written against. |
| Cached design screenshots | raw/design/01-first-run-platform-source.png and 02-empty-no-device.png are the authoritative visual reference for toolbar/status-bar/stage composition. Both are LIGHT; no dark comp exists. The URL must not be re-fetched. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| The WCAG sweep will likely FAIL on the light palette for two signal-on-surface pairs: sentFixed #3EAB5E on surface #FFFFFF measures approximately 2.9:1 and warnStillOff #E69825 on #FFFFFF approximately 2.4:1 — both below the 3:1 non-text threshold NFR-11/AC-40 demand. The tokens cannot be changed (AC-36/decision 6), so this is a specification conflict, not a coding bug. | high | high | Design the sweep in two tiers: a MUST tier (surface/ink text pairings, focus ring, borders) that hard-fails, and a SIGNAL tier that asserts against a checked-in baseline map of measured ratios — any pair at or above 3:1 must stay at or above 3:1, any pair currently below is pinned to its measured value so a regression still fails while reality is recorded rather than faked. Surface the two failing pairs to the human gate as openQuestions: accept the WCAG 1.4.11 redundancy argument (every signal dot in the kit is accompanied by a text label — DeviceChip, StatusPill, StatusIndicator, StatusLine all pair dot+label, so the dot is not the sole carrier of information), or amend the palette (which contradicts AC-36). Do NOT silently relax the threshold. |
| ink3 (#8B8B91 light) measures approximately 3.1:1 against bg #F6F6F5 — it is used as TEXT (monoMeta, placeholders, counts) throughout the kit, so a strict reading of AC-36/NFR-11's 4.5:1 text rule fails. | high | med | The sweep classifies ink and ink2 as text (4.5:1) and ink3 as incidental/meta ink (3:1), and the classification is written down in the test file as data, not buried in an assertion. Raised as an openQuestion for the human gate; darkening ink3 would violate AC-36. |
| ThemeColor has no on-pop ink token, yet pop is deliberately dark in BOTH palettes (#18191C light, #2C2D31 dark). Pairing pop with the theme's default ink reports a false failure in light mode. | high | med | Binding decision 5 already requires pairing pop with on-pop ink. Since no onPop token exists, the sweep pairs pop with Color.White, following the existing deliberate precedent in CounterBadge (whose KDoc explains exactly why white is a literal there). Recommend adding an onPop token in a later spec; adding it now would modify the palette. |
| isSystemInDarkTheme() may not react to a LIVE macOS appearance change on JVM/Skiko, degrading FR-18's System mode to launch-time detection only. | med | med | Spike it at the start of step 10, before the override toggle is built. If static, keep System as the launch-time default, document the limitation against FR-18, and leave the manual override as the live path. A reactive fallback would need an NSApplication appearance observer — real work, out of scope. |
| macOS traffic lights overlap toolbar content, or the full-size-content client properties behave differently under a non-JBR JDK. | med | low | Reserve the leading inset unconditionally (ShellWindowDefaults.TrafficLightInset, 78dp) and apply the client properties behind an os.name check. Without them the window falls back to a standard title bar above the toolbar. |
| Writing the panel width on every drag frame would thrash java.util.prefs, which flushes to disk. | med | med | Resolved by design: ShellState records the width at drag start and onDragStop() returns a value only when it actually changed; the window layer persists exactly that. Covered by ShellStateTest, so the behaviour cannot silently regress. |
| Establishing the repo's first test suite turns into a yak-shave (CI, desktop test runtime, Compose UI test deps) and consumes Stage 5. | med | med | kotlin.test only, wired once in two convention plugins, plus exactly one extra catalog entry (multiplatform-settings-test) wired in the same change. No Compose UI test harness. Stage 5 runs four named :desktopTest tasks, not ./gradlew check. |
| Seven of twelve ACs are verificationMethod=inspection, so Stage 5 cannot fully gate them automatically. | high | med | Convert what converts: AC-36/AC-40 → contrast sweep; AC-34 → ShellWindowDefaults constants test; AC-12 → three-tab assertion on ShellTab.entries; AC-32 → ShellState default/onSessionStarted test. The residue (AC-30, AC-31, AC-37, and the live window behaviour of AC-34) is gated by light+dark @Preview pairs plus running screenshots compared against the two cached designs; the traceabilityMatrix marks these explicitly as inspection. |
| Scope creep: screenshot 02 shows a dashed NO SIGNAL device outline and an onboarding copy block that are tempting to build as 'the empty state'. | high | med | StageSurface takes a content slot; this deliverable fills it with a single neutral placeholder. The dashed outline, the instruction steps and the Boot control are android-device-discovery-mirroring's content. Definition of done for step 11 is 'slots exist and hold static stubs'. |
| App()/NavigationHost and RectifyWindow become two competing roots; the template features (onboarding/home/settings/about/splash, Navigation3, Room) become dead code on desktop. | med | low | On desktop only, runDesktopApp() renders RectifyWindow instead of App(). App() stays untouched for Android/iOS. The cleanup decision is an explicit open question for a later spec, not taken here. |
| Renaming ThemeModel.MaterialU to ThemeModel.System touches a public enum in domain-core. | low | low | Only two call sites exist repo-wide (ThemeModel.kt itself and App.kt's when-branch — verified by grep). The stored string value 'materialu' is KEPT, so no persisted value is orphaned; and because the mapper fallback becomes System, the data-layer default string 'system' (ThemePreference.DEFAULT_THEME, which matches no enum entry today and currently silently resolves to Light) now resolves to System too. ThemePreferenceMapperTest pins all three cases. |
| The stage's dotted texture has no specified cell size, dot radius or opacity, and no token exists for it. | high | low | Approximate from screenshot 02 as private component geometry inside StageSurface.kt (cell 16dp, dot radius 0.75dp, ink3 at a named alpha constant) and carry Stage 1's open question forward: promote to named ThemeStageTexture tokens only if the values survive design review. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-11 | Use the canonical 'Rectify Window' layout: a bezel-less live mirror with a side panel exposing exactly three tabs (Notes, Payload, History). |
| FR-12 | Render a toolbar frame (top, full width) hosting placeholder slots for: window traffic-light region, platform switch (Android \| iOS), device indicator, staged counter, Clear control, and Resolve & Send control -- all stub-rendered, non-functional in this deliverable. |
| FR-13 | Render a status bar frame (bottom, full width) hosting a left tool-version/status text slot and a right app-version text slot, stub-rendered with static placeholder text. |
| FR-14 | Render the bezel-less live-mirror stage region filling the remaining window area between toolbar and status bar, including its empty-state (no device) placeholder presentation. |
| FR-15 | Render the three-tab side panel (Notes, Payload, History) adjacent to the stage, only once a mirroring session is live; the panel is absent in pre-mirror/empty-state presentations. Its width is resizable (FR-20) and persisted (FR-21). |
| FR-16 | Support switching the active side-panel tab by click, and by Cmd+1 / Cmd+2 / Cmd+3 keyboard shortcuts, defaulting to the Notes tab when the panel first appears. |
| FR-17 | Set the application window's initial size to 1240x820 and its minimum resizable size to 1024x700. |
| FR-18 | Select the active theme (light/dark) by following macOS system appearance by default (ThemeMode.System), with a manual override toggle (toolbar or status bar control) that persists across app relaunch. |
| FR-19 | Adopt the existing Rectify UI Kit design-token system (presentation-core-styling module: ThemeColor, attributeLightColorPalette, attributeDarkColorPalette, AppTheme, ThemeMode) for this shell's light/dark palettes rather than authoring new tokens; wire theme selection (FR-18) through AppTheme(mode, ...). |
| FR-20 | Make the side panel's width user-resizable via a drag handle on the boundary between the stage and the panel, clamped to a minimum of 260px and a maximum of 480px. |
| FR-21 | Persist the side panel's current width across app relaunch -- restore it to its last-set value on next launch, or to a documented default width on first run. |
| FR-23 | The four signal colors (annotate/red, sentFixed/green, warnStillOff/orange, measure/blue) render identically in both light and dark themes per the adopted token system; theme contrast parity (NFR-9/AC-30) is achieved via surface/ink token pairing per theme, not by varying the signal-color values between themes. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. rectify-kmp's Android/iOS targets are not in scope for this deliverable even though the host repo carries them. |
| NFR-11 | accessibility | Every surface/ink token pairing and every signal-color-on-surface pairing, in both light and dark themes, meets WCAG AA contrast: >=4.5:1 for text against its background, >=3:1 for non-text UI components (borders, icons, focus indicators) against adjacent colors. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-12 | Given the canonical window layout is confirmed, then the side panel exposes exactly three tabs: Notes, Payload, History, over a bezel-less mirror. | must | inspection |
| AC-30 | Given the user toggles light/dark theme, then all screens render correctly in both themes with consistent signal colors (red/green/orange/blue) and no unreadable contrast regions. | must | inspection |
| AC-31 | Given no mirroring session is active, when the window is displayed, then the side panel is not rendered at all; the toolbar shows its stubbed 'No device' state and the bezel-less stage shows the empty-state placeholder region, matching both cached design screenshots. | must | inspection |
| AC-32 | Given a mirroring session becomes active, when the side panel first appears, then the Notes tab is the active/selected tab by default. | should | inspection |
| AC-33 | Given the side panel is visible, when the user presses Cmd+1, Cmd+2, or Cmd+3, then the Notes, Payload, or History tab (respectively) becomes active, in addition to click-to-switch. | should | test |
| AC-34 | Given the application window, then its initial size is 1240x820, it is user-resizable, and it cannot be resized below 1024x700. | must | inspection |
| AC-35 | Given the user toggles the manual theme override control, then the app switches between light and dark palettes immediately without restart, and the chosen theme persists across app relaunch; absent a manual override, the app follows macOS system appearance (ThemeMode.System). | must | test |
| AC-36 | Given the existing Rectify UI Kit token system (attributeLightColorPalette / attributeDarkColorPalette in presentation-core-styling), then this deliverable adopts it as-is -- every neutral token (bg, chrome, stage, surface, pop, ink, ink2, ink3, primary, primaryHover, primaryPress, onPrimary, focus, line, line2, hover, seg, codeBg) and every signal token (annotate, sentFixed, warnStillOff, measure) is used unmodified, and surface/ink contrast in both palettes meets WCAG AA (>=4.5:1 text, >=3:1 non-text). | must | inspection |
| AC-37 | Given the toolbar and status bar are rendered, then their content is limited to stubbed/static placeholder slots in this deliverable -- the platform switch, device indicator, staged counter, Clear control, Resolve & Send control, and tool-version status line render in a fixed, non-functional placeholder state with no live adb/idb/device wiring. | must | inspection |
| AC-38 | Given the side panel is visible, when the user drags the handle between the stage and panel, then the panel width adjusts live and is clamped between 260px and 480px (the drag cannot push it narrower or wider than those bounds). | must | test |
| AC-39 | Given the user has resized the side panel and then relaunches the app, then the panel reopens at the previously set width (not reset to a default) the next time a mirroring session makes it visible. | must | test |
| AC-40 | Given light or dark theme is active, then each signal color (annotate/red, sentFixed/green, warnStillOff/orange, measure/blue) -- identical in both themes per the adopted token system -- meets WCAG AA contrast against its paired surface/ink tokens in that theme (>=3:1 for non-text indicators such as dots/icons, >=4.5:1 where rendered as text); parity is verified through surface/ink pairing per theme, not by asserting different signal values between themes. | must | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring", "SHELL SCOPE NOTE: only the window opening at 1240x820 with a stub-filled toolbar ('● No device'), the bezel-less stage and the stub status bar is in scope here; detection, picker content and Start mirroring belong to android-device-discovery-mirroring."] |
| SC-2 | Toggle light/dark theme | happy | ["User opens app; theme follows macOS system appearance by default (ThemeMode.System)", "User triggers the manual theme-override toggle in the status bar", "App immediately re-renders toolbar, stage, side panel and status bar using the other palette, with all four signal colors unchanged and still legible", "User relaunches the app", "The manually chosen theme persists (pref_theme)"] |
| SC-3 | Switch side-panel tabs via keyboard | happy | ["Mirroring session is active and the side panel is visible on the default Notes tab", "User presses Cmd+2", "Payload tab becomes active", "User presses Cmd+3", "History tab becomes active"] |
| SC-4 | Pre-mirror window has no side panel | edge | ["App launches with no device connected", "Window displays toolbar (stubbed 'No device' state), stubbed status bar and the bezel-less empty-state stage", "Side panel is absent (sidePanel slot is null — neither the panel nor the resize handle is composed)", "Cmd+1/2/3 pressed in this state is a no-op", "A mirroring session starts", "Side panel appears, defaulting to the Notes tab"] |
| SC-5 | Resize and persist side-panel width | edge | ["Mirroring session active, side panel visible at its default width (320dp on first run)", "User drags the resize handle narrower", "Panel width stops at the 260dp minimum", "User drags wider past the maximum", "Panel width stops at the 480dp maximum", "On drag release the width is written once to pref_side_panel_width (and only if it changed)", "User relaunches the app", "Panel reopens at the last-set width, not the default"] |

### definitionOfReady

- Stage 1 Preparation is human-approved with readyForPlan=true (satisfied).
- The six binding decisions are recorded and not reopened by this plan (satisfied).
- Both cached design screenshots are present under raw/design/ and have been read (satisfied).
- The reuse inventory below was established by reading the actual source files, not by registry lookup — no registry exists (satisfied, recorded in projectContext).
- The human gate has resolved, or explicitly deferred with an owner, the two contrast conflicts raised in openQuestions (sentFixed/warnStillOff below 3:1 on light surfaces; ink3 below 4.5:1 as text). Stage 4 can start without this; Stage 5 cannot sign off AC-36/AC-40 without it.

### definitionOfDone

- ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest is green.
- ./gradlew :desktop-application:run opens a 1240x820 window that cannot be resized below 1024x700, with the macOS traffic lights sitting inside the Rectify toolbar behind the reserved inset.
- The running window matches cached screenshot 01/02 composition in LIGHT: toolbar order (inset, Android|iOS, ● No device, …, 0 staged, Clear, disabled Resolve & Send ⌘↵), bezel-less dotted stage, status bar with mono tool-version text left and 'Rectify 0.4' right, and NO side panel.
- Flipping the status-bar theme override re-renders both palettes immediately and survives relaunch.
- With the side panel forced visible (preview or a temporary debug toggle), the three tabs render Notes/Payload/History with Notes active, Cmd+1/2/3 switch tabs, and the handle resizes between 260 and 480dp with the width surviving relaunch.
- Light and dark @Preview pairs exist for the no-panel and with-panel shell states and render without crashing (every preview wrapped in AppTheme per .claude/rules/compose-preview-convention.md).
- Zero Color(0x...) literals, zero hardcoded TextStyles and zero raw .dp at any shell CALL site; component-own geometry only as private file-level vals (grep the new files to confirm).
- ./gradlew :presentation-core-ui:build and :compose-application:build pass with explicitApi() satisfied (explicit visibility AND return type on every public declaration).
- No empty core/ directory was created in any module; everything public lives under source/.
- pref_theme's key string is unchanged; pref_side_panel_width is registered at the END of its Koin layer group in every layer.
- DEMO_MODE in App.kt is still false.
- The PRD's green-signal note reads hue 150, matching the shipped token; no palette file was edited (git diff over presentation-core-styling/src/commonMain shows only the new commonTest files).

### successMetrics

- 100% of the MUST-tier surface/ink token pairings in both palettes pass their WCAG AA threshold in the automated sweep, and every SIGNAL-tier pairing is either at/above 3:1 or pinned to a documented baseline with a human decision attached.
- The shell renders with zero layout changes required when a downstream feature fills the toolbar/stage/panel slots with real content — measured at the start of android-device-discovery-mirroring: that spec adds no parameters to RectifyWindowShell, ShellToolbar, StageSurface or SidePanelFrame.
- Repo test count goes from 0 to the 9 test files named in testStrategy, all runnable from a single Gradle invocation.
- The shell's new Kotlin footprint stays under roughly 900 lines across presentation-core-ui + compose-application — if it is materially larger, something is being authored that should have been assembled.

### architecture

#### approach

Assemble, don't author. A new organism family in presentation-core-ui (source/kit/organism/shell/) plus one atom defines the slot contract; compose-application/desktopMain owns the window, the macOS chrome, the key handling and the persistence wiring; the data/domain preference chain gains exactly one new value. Shell state is plain Compose state hoisting in a @Stable ShellState class, so every acceptance-relevant behaviour (clamp, drag-end commit, tab default, shortcut mapping, window constants) is a pure function or a plain class method, unit-testable without any Compose UI harness. No new Gradle module, no Orbit MVI, no ViewModel, no new third-party dependency except one test-only artifact.

#### rationale

presentation-core-ui is literally the repo's shared-component module and compose-application is literally its entry-point module; a new Gradle module would cost a settings entry, a convention-plugin application, a Koin module and an iOS framework baseName for zero benefit, and a presentation-feature-* name would drag in the Orbit convention the shell does not need (orbit-mvi.md's frontmatter scopes it to presentation-feature-*/**). Expressing the pre-mirror state as a NULLABLE sidePanel slot rather than a boolean means a downstream feature physically cannot render an empty panel. Named toolbar slots rather than a free-form RowScope mean a downstream feature physically cannot reorder the canonical toolbar. Keeping the window-geometry constants in presentation-core-ui (not in desktopMain) is what makes AC-34's values testable at all, because the kotlin.test wiring lands in the Library/Feature convention plugins and compose-application uses the application plugin. Reusing the multiplatform-settings chain over a direct java.util.prefs call costs ~6 mechanical files but keeps the presentation layer from punching through three architectural layers in the one file every future developer opens first.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| New presentation-feature-shell module with the Orbit four-file shape | The shell holds three values with no async work, no navigation and no one-shot effects; orbit-mvi.md's frontmatter does not even apply to it. A ViewModel would also make the shell un-previewable and unusable from desktopMain without Koin. Rejected at the brainstorm gate (binding decision 3). |
| Port presentation-core-styling into a fresh desktop-only project, or consume rectify-kmp via a Gradle composite build | Both rejected at the brainstorm gate (binding decision 1). A port forks ~140 files and structurally breaks AC-36's 'adopted unmodified'; a composite build still drags AGP, the Android SDK and the iOS targets in, because presentation-core-styling applies the feature convention plugin. |
| undecorated = true with custom-drawn traffic lights | Re-implements hit-testing, drag regions, double-click-to-zoom and fullscreen for a requirement nobody wrote. Binding decision 2 chose native decorations + full-size-content client properties. |
| Window constants (1240x820 / 1024x700) declared in compose-application/desktopMain next to the Window call | compose-application uses ApplicationConventionPlugin, which does not get the kotlin.test wiring (binding decision 5 scopes it to Library/Feature). Declaring them there would leave AC-34 with no automatable surface at all. They live in presentation-core-ui's ShellWindowDefaults and are consumed by the window. |
| Read/write the panel width directly from java.util.prefs in the window composable (2 lines) | Rejected at the brainstorm gate (binding decision 4). It punches the presentation layer straight through to storage in the most-copied file in the app. |
| Add a Compose UI test harness (runComposeUiTest) to assert tab clicks and handle drags | Rejected at the brainstorm gate (binding decision 5). The repo has zero test infrastructure; the shell's real acceptance surface (clamp, shortcut mapping, commit-on-change, persistence round-trip, contrast) is all pure logic, and rendering fidelity is what the inspection ACs and the light/dark previews are for. |
| Introduce a ThemeStageTexture token group now for the dotted stage ground | No specified values exist for cell size, dot radius or opacity, and adding tokens to presentation-core-styling for guessed values is exactly the palette modification AC-36 forbids in spirit. Kept as private component geometry inside StageSurface.kt and carried forward as an open question. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| RectifyWindowShell — presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt (organism) | ui | new |  | THE slot contract. public fun RectifyWindowShell(toolbar: @Composable () -> Unit, statusBar: @Composable () -> Unit, modifier: Modifier = Modifier, state: ShellState = rememberShellState(), sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null, onPanelWidthCommit: (Dp) -> Unit = {}, stage: @Composable BoxScope.() -> Unit). Renders Column(Modifier.fillMaxSize().background(Theme.color.bg))[ toolbar(), Row(Modifier.weight(1f))[ StageSurface(Modifier.weight(1f), content = stage), if (sidePanel != null) { PanelResizeHandle(...); SidePanelFrame(Modifier.width(state.panelWidth), activeTab = state.activeTab, onTabSelect = state::selectTab, content = sidePanel) } ], statusBar() ]. sidePanel == null renders neither the panel NOR the handle (AC-31/FR-15). Drag end: state.onDragStop()?.let(onPanelWidthCommit) — the shell never persists anything itself. Carries four @Preview functions: no-panel/with-panel x Light/Dark. |
| ShellToolbar — .../organism/shell/ShellToolbar.kt (organism) | ui | new |  | public fun ShellToolbar(platformSwitch: @Composable () -> Unit, deviceIndicator: @Composable () -> Unit, stagedCounter: @Composable () -> Unit, clearAction: @Composable () -> Unit, primaryAction: @Composable () -> Unit, modifier: Modifier = Modifier, leadingInset: Dp = ShellWindowDefaults.TrafficLightInset). Full-width Theme.color.chrome bar, private ToolbarHeight = 52.dp, bottom Theme.color.line hairline via Theme.stroke.thin. Row order is fixed: Spacer(leadingInset), platformSwitch, deviceIndicator, Spacer(weight 1f), stagedCounter, clearAction, primaryAction — named slots, not a free-form RowScope, so the canonical order cannot be reordered downstream. |
| ShellStatusBar — .../organism/shell/ShellStatusBar.kt (organism) | ui | new |  | public fun ShellStatusBar(leading: @Composable RowScope.() -> Unit, trailing: @Composable RowScope.() -> Unit, modifier: Modifier = Modifier). Full-width Theme.color.chrome bar, private StatusBarHeight = 28.dp, top Theme.color.line hairline, leading slot then Spacer(weight 1f) then trailing. Hosts the theme-override control in the trailing slot (filled by the window, not by the organism). |
| StageSurface — .../organism/shell/StageSurface.kt (shell region; imports only tokens, so it satisfies the layering rule trivially) | ui | new |  | public fun StageSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit). Box filling the remaining space, background Theme.color.stage, NO border/bezel/card (FR-14's 'bezel-less'), with a Canvas drawing a dotted grid behind the content. Private geometry: DotCellSize = 16.dp, DotRadius = 0.75.dp, DOT_ALPHA = 0.28f applied to Theme.color.ink3 — approximated from screenshot 02, flagged for tokenisation. |
| SidePanelFrame — .../organism/shell/SidePanelFrame.kt (organism) | ui | new |  | public fun SidePanelFrame(activeTab: ShellTab, onTabSelect: (ShellTab) -> Unit, modifier: Modifier = Modifier, tabCounts: Map<ShellTab, Int> = emptyMap(), content: @Composable ColumnScope.(ShellTab) -> Unit). Column on Theme.color.surface with a leading Theme.color.line hairline; header row (private PanelHeaderHeight = 40.dp, horizontal padding Theme.spacing.spacingS) hosts the REUSED SidebarTabs molecule built from ShellTab.entries, selectedIndex = activeTab.ordinal, onSelect = { onTabSelect(ShellTab.entries[it]) }; below it a divider and content(activeTab). Tab CONTENT is downstream. |
| PanelResizeHandle — presentation-core-ui/.../source/kit/atom/handle/PanelResizeHandle.kt (atom) | ui | new |  | public fun PanelResizeHandle(onDelta: (Dp) -> Unit, onDragStarted: () -> Unit, onDragStopped: () -> Unit, modifier: Modifier = Modifier). Private geometry HitWidth = 8.dp, LineWidth = 1.dp; a Theme.color.line2 hairline centred in the hit area, Modifier.draggable(orientation = Horizontal) converting pixel deltas to Dp via LocalDensity, plus pointerHoverIcon(PointerIcon.Hand on common / E-resize on desktop). Contains NO clamping and NO persistence — both live outside so they are testable. |
| ShellTab + shortcutToTab — .../organism/shell/ShellTab.kt (pure logic) | logic | new |  | public enum class ShellTab(public val label: String) { Notes("Notes"), Payload("Payload"), History("History") } — exactly three entries, canonical order, labels from FR-11 (NOT SidebarTabs' KDoc example label 'Annotations'). public fun shortcutToTab(isMeta: Boolean, key: Key): ShellTab? = if (!isMeta) null else when (key) { Key.One -> Notes; Key.Two -> Payload; Key.Three -> History; else -> null }. Pure, so AC-33 and AC-12 become runnable assertions. |
| ShellState + rememberShellState + clampPanelWidth — .../organism/shell/ShellState.kt (logic) | logic | new |  | public fun clampPanelWidth(width: Dp): Dp = width.coerceIn(ShellWindowDefaults.PanelMinWidth, ShellWindowDefaults.PanelMaxWidth) — the whole AC-38 acceptance surface in one function. @Stable public class ShellState(initialPanelWidth: Dp) with: public var activeTab: ShellTab by mutableStateOf(ShellTab.Notes) private set; public var panelWidth: Dp by mutableStateOf(clampPanelWidth(initialPanelWidth)) private set (so a corrupt stored value is clamped on read); public fun selectTab(tab: ShellTab); public fun setPanelWidth(width: Dp) (clamped — used to restore from preferences); public fun resizePanelBy(delta: Dp) (clamped; dragging the handle left widens the panel, so the delta is subtracted); public fun onDragStarted() (records widthAtDragStart); public fun onDragStopped(): Dp? (returns the new width ONLY if it differs from widthAtDragStart, else null — this is the 'never write per frame, never write unchanged' rule made testable); public fun onSessionStarted() (resets activeTab to Notes — AC-32; deliberately does NOT touch panelWidth, which has a different lifetime per AC-39). @Composable public fun rememberShellState(initialPanelWidth: Dp = ShellWindowDefaults.PanelDefaultWidth): ShellState. |
| ShellWindowDefaults — .../organism/shell/ShellWindowDefaults.kt (logic) | logic | new |  | public object ShellWindowDefaults { public val InitialSize: DpSize = DpSize(1240.dp, 820.dp); public val MinimumSize: DpSize = DpSize(1024.dp, 700.dp); public val PanelMinWidth: Dp = 260.dp; public val PanelMaxWidth: Dp = 480.dp; public val PanelDefaultWidth: Dp = 320.dp; public val TrafficLightInset: Dp = 78.dp }. Window/shell geometry, not a Theme token — same precedent as AppButton's per-size metrics ('lives in the component rather than in Theme on purpose'). Lives in presentation-core-ui so that AC-34's values are assertable from a module that has kotlin.test wired. |
| RectifyWindow — compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt | ui | new |  | ApplicationScope-scoped composable owning: rememberWindowState(size = ShellWindowDefaults.InitialSize); inside FrameWindowScope a LaunchedEffect setting window.minimumSize = Dimension(1024, 700) and, when System.getProperty("os.name").startsWith("Mac"), window.rootPane.putClientProperty for apple.awt.fullWindowContent / apple.awt.transparentTitleBar / apple.awt.windowTitleVisible; onPreviewKeyEvent mapping KeyDown + shortcutToTab to state.selectTab, returning true only when the panel is visible and a tab matched (otherwise the event passes through — SC-4); theme resolution from ObserveThemeUseCase (same mapping App.kt uses, with the System fallback); panel-width restore via GetSidePanelWidthUseCase in a LaunchedEffect(Unit) and persistence via SetSidePanelWidthUseCase from onPanelWidthCommit inside a rememberCoroutineScope; and AppTheme(mode) { RectifyWindowShell(...) } with every slot stub-filled. |
| runDesktopApp() — compose-application/src/desktopMain/.../window/DesktopEntry.kt (EDIT) | ui | reuse | source:compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt | application { RectifyWindow(onCloseRequest = ::exitApplication) } instead of Window { App() }. :desktop-application/Main.kt is UNCHANGED — initKoin() stays in main() per that module's CLAUDE.md. |
| SidePanelWidthPreference + SidePanelWidthPreferenceSource — data-preference-api/src/commonMain/kotlin/data/preference/api/source/model/SidePanelWidthPreference.kt and .../source/datasource/SidePanelWidthPreferenceSource.kt | data | new |  | public data class SidePanelWidthPreference(val widthDp: Int = DEFAULT_WIDTH_DP) : Resource with companion const DEFAULT_WIDTH_DP = 320; public interface SidePanelWidthPreferenceSource : PreferenceSource<SidePanelWidthPreference>. Mirrors ThemePreference/ThemePreferenceSource exactly. |
| SidePanelWidthPreferenceSourceImpl — data-preference-impl/src/commonMain/kotlin/data/preference/impl/source/datasource/SidePanelWidthPreferenceSourceImpl.kt | data | new |  | internal class taking Settings; private const val KEY_SIDE_PANEL_WIDTH = "pref_side_panel_width"; MutableStateFlow seeded by readFromSettings() using settings.getIntOrNull(KEY) ?: DEFAULT_WIDTH_DP; setData writes through and updates the flow; getData re-reads. Byte-for-byte the ThemePreferenceSourceImpl shape. |
| SidePanelWidthPreferenceMapper — data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/SidePanelWidthPreferenceMapper.kt | data | new |  | internal object : ModelResourceMapper<Int, SidePanelWidthPreference> — toModel takes widthDp, toResource wraps it. MODEL is a plain Int (mappers.md explicitly allows plain types as MODEL, cf. LanguagePreferenceMapper) so no new domain model class is created. |
| ConfigureRepository / ConfigureRepositoryImpl (EDIT) | data | reuse | source:domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/ConfigureRepository.kt + data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/ConfigureRepositoryImpl.kt | Add suspend fun getSidePanelWidth(): Int and suspend fun setSidePanelWidth(widthDp: Int) at the END of the interface, with KDoc on both; implement them in ConfigureRepositoryImpl via the new mapper and a new constructor parameter sidePanelWidthPreferenceSource. No observe* variant — the width is read once at launch and written on drag-end (YAGNI; add it when something needs to react). |
| Get/SetSidePanelWidthUseCase (+ Impl) — domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/configuration/ and domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/configuration/ | logic | new |  | GetSidePanelWidthUseCase: suspend operator fun invoke(): Result<Int>. SetSidePanelWidthUseCase: suspend operator fun invoke(widthDp: Int): Optional. Both impls wrap the repository call in resultLauncher(errorMapper = Failure.Technical::Preference) per .claude/rules/domain-layer.md / error-handling.md, so a BackingStoreException surfaces as Failure.Technical.Preference and never crashes the window. |
| ThemeModel (EDIT) — domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt | data | reuse | source:domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt | Rename the misleading entry MaterialU to System, KEEPING its stored string value "materialu" so no persisted value is orphaned, and update the KDoc (it currently says 'Material You colors', which has never been what this entry meant on desktop). Only two call sites exist repo-wide (verified by grep): the enum itself and App.kt's when-branch. |
| ThemePreferenceMapper (EDIT) — data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/ThemePreferenceMapper.kt | data | reuse | source:data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/ThemePreferenceMapper.kt | Change the toModel fallback from ThemeModel.Light to ThemeModel.System. This single line is what makes FR-18 true on a fresh install: ThemePreference.DEFAULT_THEME is already the string "system", which matches no ThemeModel entry, so today it silently resolves to Light. With the new fallback, both the unset default and any unrecognised stored string resolve to System. |
| App.kt (EDIT) — compose-application/src/commonMain/kotlin/dev/rectify/io/source/app/App.kt | ui | reuse | source:compose-application/src/commonMain/kotlin/dev/rectify/io/source/app/App.kt | Rename the ThemeModel.MaterialU branch to ThemeModel.System and change the null-branch and collectAsState(initial = ...) from ThemeMode.Light to ThemeMode.System, so Android/iOS and the desktop window agree on the default. Nothing else in App() changes; DEMO_MODE stays false. |
| LibraryConventionPlugin / FeatureConventionPlugin (EDIT) — build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/{library,feature}/ | logic | reuse | source:build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/library/LibraryConventionPlugin.kt + .../feature/FeatureConventionPlugin.kt | Inside each plugin's existing configureCommonDependencies(), add sourceSets.commonTest.dependencies { implementation(libs.findLibrary("kotlin-test").get()) }. One edit per plugin, both inside an existing method — no new BaseConventionPlugin hook (convention-plugins.md: don't add a hook only one plugin would use, and here no new hook is needed at all). |
| SegmentedButtonGroup (molecule) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/button/SegmentedButtonGroup.kt | Toolbar platform switch stub: SegmentedButtonGroup(items = listOf("Android", "iOS"), selectedIndex = 0, onSelect = {}). Its own KDoc names it 'the toolbar's platform switch (Android \| iOS)'. 30dp recessed Theme.color.seg track with a raised selected chip — exactly screenshot 01. |
| DeviceChip + DeviceChipState (molecule) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/chip/DeviceChip.kt | Toolbar device indicator stub: DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {}) — the None state already renders the ink3 dot and the ChevronDown disclosure, matching '● No device ▾'. |
| CounterBadge + CounterBadgeState (atom) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/badge/CounterBadge.kt | Toolbar staged counter stub: CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged") — the Empty state is the seg-filled neutral badge in screenshot 01. |
| AppButton (atom) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt | Two toolbar stubs: AppButton(text = "Clear", variant = Ghost, onClick = {}) and AppButton(text = "Resolve & Send", shortcut = "⌘↵", enabled = false, onClick = {}). NOTE: AppButton already has a first-class shortcut parameter (its KDoc example is literally Resolve & Send with ⌘↵) and renders it muted inside the button — so the primary action needs no separate KeyCap composition. KeyCap stays the atom to use if design review later wants a cap-rendered glyph; recorded as reuse either way. |
| KeyCap (atom) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/keycap/KeyCap.kt | Available for the Cmd+1/2/3 affordance in the side-panel header and as the alternative rendering of the Resolve & Send shortcut. Not composed by default (AppButton.shortcut covers the toolbar case) — flagged for design review rather than built twice. |
| SidebarTabs + SidebarTabItem (molecule) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/bar/tab/SidebarTabs.kt | The side panel's three-tab header, verbatim. Built for this exact panel (its KDoc example is 'Annotations · Payload · History' and it explains the filled-chip selection precisely because the row sits inside a panel). Labels come from ShellTab, so FR-11's 'Notes' wins over the KDoc's 'Annotations' with zero code conflict. |
| StatusIndicator + StatusIndicatorState (atom) | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/indicator/StatusIndicator.kt | Status-bar leading stub: StatusIndicator(state = Offline, label = "adb 35.0.1 · idb 1.1.8 · ready") — a 7dp dot plus an overline label, which is what screenshot 01's status bar is. |
| StatusLine / StatusStep / StatusPill (molecule/atom) | ui | reuse | source:presentation-core-ui/.../molecule/status/StatusLine.kt + .../atom/pill/StatusPill.kt | Available for the status bar when a live pipeline exists (StatusLine is a chrome-filled 34dp row of pipeline steps; StatusPill is the compact '● Fixed' pill). Not composed in the shipped stub — screenshot 01's status bar is a single dot+mono-text line, which StatusIndicator already is. Recorded so the downstream spec does not re-author them. |
| IconButton + Sun icon (atom) | ui | reuse | source:presentation-core-ui/.../atom/button/IconButton.kt + .../atom/icon/Sun.kt | The status-bar theme-override control (cycles System → Light → Dark through SetThemeUseCase). No new component. |
| AppTheme / Theme / ThemeMode / ThemeColor and both palettes | ui | reuse | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/source/theme/AppTheme.kt + core/Theme*.kt + source/attribute/color/{Light,Dark}Color.kt | Adopted unmodified (FR-19/AC-36). AppTheme already resolves Light/Dark/System via isSystemInDarkTheme(); the shell passes the resolved mode explicitly. The ONLY files this deliverable adds to presentation-core-styling are under src/commonTest. |
| StyleguideScreen / DemoHost | ui | reuse | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/showcase/ + compose-application/src/commonMain/kotlin/dev/rectify/io/core/demo/DemoHost.kt | Existing in-app visual QA surface rendering every token and component in both themes — the fastest way to produce AC-30/AC-36/AC-40 inspection evidence. DEMO_MODE must be false before committing. |

#### dataModel

| entity | fields |
| --- | --- |
| SidePanelWidthPreference (data-preference-api, Resource) | ["widthDp: Int — default 320, persisted under the settings key pref_side_panel_width (Int)"] |
| ThemePreference (existing, unchanged) | ["theme: String — key pref_theme, DEFAULT_THEME = \"system\" (already); the only change is downstream, in ThemePreferenceMapper's fallback"] |
| ThemeModel (domain-core, edited) | ["Light(\"light\")", "Dark(\"dark\")", "System(\"materialu\") — entry RENAMED from MaterialU, stored string value deliberately unchanged so no install loses its stored value"] |
| ShellState (presentation-core-ui, in-memory only) | ["activeTab: ShellTab — session-scoped, resets to Notes on onSessionStarted() (AC-32)", "panelWidth: Dp — app-scoped, clamped on every mutation, persisted across relaunch (AC-39)", "widthAtDragStart: Dp — private; drives the commit-only-if-changed rule"] |
| ShellTab (presentation-core-ui) | ["Notes(label=\"Notes\")", "Payload(label=\"Payload\")", "History(label=\"History\") — exactly three entries; the test asserts entries.size == 3 and the exact order"] |

#### interfaces

| name | contract |
| --- | --- |
| RectifyWindowShell (the deliverable's public API) | @Composable public fun RectifyWindowShell(toolbar: @Composable () -> Unit, statusBar: @Composable () -> Unit, modifier: Modifier = Modifier, state: ShellState = rememberShellState(), sidePanel: (@Composable ColumnScope.(ShellTab) -> Unit)? = null, onPanelWidthCommit: (Dp) -> Unit = {}, stage: @Composable BoxScope.() -> Unit): Unit |
| ShellToolbar | @Composable public fun ShellToolbar(platformSwitch: @Composable () -> Unit, deviceIndicator: @Composable () -> Unit, stagedCounter: @Composable () -> Unit, clearAction: @Composable () -> Unit, primaryAction: @Composable () -> Unit, modifier: Modifier = Modifier, leadingInset: Dp = ShellWindowDefaults.TrafficLightInset): Unit |
| ShellStatusBar | @Composable public fun ShellStatusBar(leading: @Composable RowScope.() -> Unit, trailing: @Composable RowScope.() -> Unit, modifier: Modifier = Modifier): Unit |
| StageSurface | @Composable public fun StageSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit): Unit |
| SidePanelFrame | @Composable public fun SidePanelFrame(activeTab: ShellTab, onTabSelect: (ShellTab) -> Unit, modifier: Modifier = Modifier, tabCounts: Map<ShellTab, Int> = emptyMap(), content: @Composable ColumnScope.(ShellTab) -> Unit): Unit |
| PanelResizeHandle | @Composable public fun PanelResizeHandle(onDelta: (Dp) -> Unit, onDragStarted: () -> Unit, onDragStopped: () -> Unit, modifier: Modifier = Modifier): Unit |
| Shell pure logic | public fun clampPanelWidth(width: Dp): Dp \| public fun shortcutToTab(isMeta: Boolean, key: Key): ShellTab? \| public enum class ShellTab(public val label: String) \| public object ShellWindowDefaults |
| ShellState | @Stable public class ShellState(initialPanelWidth: Dp) { public var activeTab: ShellTab private set; public var panelWidth: Dp private set; public fun selectTab(tab: ShellTab); public fun setPanelWidth(width: Dp); public fun resizePanelBy(delta: Dp); public fun onDragStarted(); public fun onDragStopped(): Dp?; public fun onSessionStarted() } ; @Composable public fun rememberShellState(initialPanelWidth: Dp = ShellWindowDefaults.PanelDefaultWidth): ShellState |
| ConfigureRepository (extended) | public suspend fun getSidePanelWidth(): Int ; public suspend fun setSidePanelWidth(widthDp: Int) |
| Side-panel width use cases | public interface GetSidePanelWidthUseCase { public suspend operator fun invoke(): Result<Int> } ; public interface SetSidePanelWidthUseCase { public suspend operator fun invoke(widthDp: Int): Optional } |
| SidePanelWidthPreferenceSource | public interface SidePanelWidthPreferenceSource : PreferenceSource<SidePanelWidthPreference> (key pref_side_panel_width, Int, default 320) |

#### stateManagement

Plain Compose state hoisting (binding decision 3). A single @Stable ShellState class holds activeTab and panelWidth as mutableStateOf fields with private setters and public intent-named methods; rememberShellState creates it. No Orbit container, no ViewModel, no Koin dependency inside presentation-core-ui — which is exactly what keeps the shell previewable and usable from desktopMain. Persisted values enter and leave through the window layer only: RectifyWindow reads pref_theme through the existing ObserveThemeUseCase and pref_side_panel_width once through GetSidePanelWidthUseCase, and writes the width through SetSidePanelWidthUseCase from the onPanelWidthCommit callback inside a rememberCoroutineScope. Session liveness is not owned by the shell at all: it is expressed by whether the caller passes a sidePanel lambda, so the downstream device spec owns that state in its own ViewModel and the shell stays stateless about it.

#### navigation

- No Navigation3 destination is added. On desktop, runDesktopApp() renders RectifyWindow directly instead of App()/NavigationHost — the shell IS the root.
- App()/NavigationHost and the five template feature screens stay untouched and keep working on Android/iOS; on desktop they become unreachable. The cleanup decision is an explicit open question, not taken here.
- In-shell 'navigation' is tab selection only: click via SidebarTabs, keyboard via Cmd+1/2/3 through onPreviewKeyEvent → shortcutToTab → ShellState.selectTab.

#### affectedModules

- :presentation-core-ui — 9 new files (8 under source/kit/organism/shell/, 1 under source/kit/atom/handle/) + 5 new commonTest files. No build.gradle.kts change.
- :presentation-core-styling — 2 new commonTest files ONLY. No production file is touched (this is the mechanical guarantee behind AC-36).
- :compose-application — 1 new desktopMain file (RectifyWindow.kt), 2 edits (DesktopEntry.kt, App.kt). No build.gradle.kts change (presentation-core-ui and domain-usecase-impl are already dependencies).
- :data-preference-api — 2 new files.
- :data-preference-impl — 1 new file, 1 edit (DataPreferenceImplModule.kt), 1 new commonTest file, 1 build.gradle.kts edit (multiplatform-settings-test for commonTest).
- :data-repository-impl — 1 new mapper, 2 edits (ThemePreferenceMapper.kt fallback, ConfigureRepositoryImpl.kt), 2 new commonTest files.
- :domain-repository-api — 1 edit (two methods appended to ConfigureRepository).
- :domain-core — 1 edit (ThemeModel entry rename, stored value preserved).
- :domain-usecase-api — 2 new files.
- :domain-usecase-impl — 2 new files, 1 edit (DomainUseCaseImplModule.kt, appended).
- build-logic/convention — 2 edits (kotlin.test in LibraryConventionPlugin and FeatureConventionPlugin).
- gradle/libs.versions.toml — 2 new [libraries] entries (kotlin-test, multiplatform-settings-test), both wired in the same change per version-catalog-hygiene.md.
- :desktop-application — UNCHANGED.
- :aos-application, :ios-application, all :presentation-feature-* — UNCHANGED.

### projectContext

#### registryStatus

ABSENT. .pipeline/sdd/docs/registry/ does not exist — there is no components.json, assets.json, styles.json, features.json or bugs.json. No registry-based reuse lookup was possible and there is no bug-recurrence history. Every reuse entry below was established by DIRECT SOURCE INSPECTION of this repository (each file was opened and its public signature read), and registryRef therefore carries a 'source:<path>' pointer rather than a registry pointer. This plan writes nothing to the registry; any registry delta is the orchestrator's to make.

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| SidebarTabs | SidebarTabs + SidebarTabItem (molecule) — public fun SidebarTabs(items: List<SidebarTabItem>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/bar/tab/SidebarTabs.kt |
| SegmentedButtonGroup | SegmentedButtonGroup (molecule) — public fun SegmentedButtonGroup(items: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/button/SegmentedButtonGroup.kt |
| DeviceChip | DeviceChip + DeviceChipState (molecule) — public fun DeviceChip(name: String, state: DeviceChipState, onClick: () -> Unit, modifier: Modifier, detail: String?) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/chip/DeviceChip.kt |
| CounterBadge | CounterBadge + CounterBadgeState (atom) — public fun CounterBadge(count: Int, state: CounterBadgeState, modifier: Modifier, label: String?) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/badge/CounterBadge.kt |
| AppButton | AppButton (atom) — 3 variants x 6 sizes, with a built-in `shortcut` parameter (its KDoc example is Resolve & Send / ⌘↵) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt |
| KeyCap | KeyCap (atom) — public fun KeyCap(text: String, modifier: Modifier); available for keyboard-hint affordances | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/keycap/KeyCap.kt |
| StatusIndicator | StatusIndicator + StatusIndicatorState (atom) — dot + overline label; Offline state for the stub status bar | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/indicator/StatusIndicator.kt |
| StatusLine | StatusLine + StatusStep + StatusStepState (molecule) — pipeline status row; reserved for the downstream live-session status bar | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/status/StatusLine.kt |
| StatusPill | StatusPill + StatusPillTone (atom) — compact neutral-fill pill with a coloured dot | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/pill/StatusPill.kt |
| IconButton | IconButton (atom) + Sun icon — the status-bar theme-override control | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/IconButton.kt, .../atom/icon/Sun.kt |
| ChevronDown | ChevronDown (atom icon) — already composed inside DeviceChip's disclosure affordance | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/ChevronDown.kt |
| FeedbackBar | FeedbackBar (molecule) — not composed by the shell; recorded so the downstream spec does not re-author it | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/status/FeedbackBar.kt |
| SourceListRow | SourceListRow (molecule) — the device-picker row in screenshot 01; DOWNSTREAM, listed so it is not rebuilt | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/SourceListRow.kt |
| SelectableCard | SelectableCard (molecule) — the Android/iOS platform cards in screenshot 01; DOWNSTREAM | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/item/SelectableCard.kt |
| CommandBlock | CommandBlock (molecule) — 'adb devices -l' style machine output; DOWNSTREAM | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/command/CommandBlock.kt |
| HistoryRow / AnnotationRow / KeyValueRow / HierarchyRow / VerificationRow | The row family that will fill the Notes/Payload/History tab slots; DOWNSTREAM, listed to keep the panel-content spec from re-authoring them | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/ |
| ErrorState / ErrorCard / ErrorGlyph | Existing empty/error presentation components available to the stage content slot | source:presentation-core-ui/.../molecule/state/ErrorState.kt, .../organism/error/ErrorCard.kt, .../atom/glyph/ErrorGlyph.kt |

#### reuseAssets

| id | name | registryRef |
| --- | --- | --- |
| icon-set | 22 ImageVector icons under source/kit/atom/icon/ (ChevronDown, Sun, Smartphone, X, Check, AlertTriangle, …) plus iconCatalogue/resolveIcon — the shell needs no new icon asset | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/ |
| RectifyLogo | RectifyLogo (atom icon) — available if the toolbar ever needs branding; not composed by this shell | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/RectifyLogo.kt |
| design-screenshots | Two cached light-theme design screenshots (01 first run, 02 empty) — the authoritative visual reference; no dark comp exists | source:.pipeline/sdd/docs/app-shell-window-layout/raw/design/ |

#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| ThemeColor | 22 colour tokens (bg, chrome, stage, surface, pop, ink, ink2, ink3, primary, primaryHover, primaryPress, onPrimary, focus, line, line2, hover, seg, codeBg, annotate, sentFixed, warnStillOff, measure) — adopted unmodified | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/ThemeColor.kt |
| attributeLightColorPalette / attributeDarkColorPalette | Both shipped palettes, internal — which is precisely why the contrast sweep must live in this module's own commonTest | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/source/attribute/color/LightColor.kt, DarkColor.kt |
| AppTheme | AppTheme(mode: ThemeMode = Dark, reducedMotion: Boolean = false, content) — the single provider, already resolving System via isSystemInDarkTheme() | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/source/theme/AppTheme.kt |
| ThemeSpacing/Typography/Radius/Corner/CornerToken/Stroke/Motion/Size | The rest of the token system (8dp spacing scale, typography incl. monoMeta/monoData/overline, stroke.thin, cornerToken.chip, motion durations/easings, icon sizes) — all consumed, none modified | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/ |

#### existingFeatures

| id | name | registryRef |
| --- | --- | --- |
| desktop-entry | :desktop-application main() → initKoin() → runDesktopApp() → application { Window { App() } } — works today via ./gradlew :desktop-application:run; extended, not replaced | source:desktop-application/src/main/kotlin/dev/rectify/io/source/entry/Main.kt, compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt |
| theme-persistence | pref_theme end-to-end: ThemePreferenceSourceImpl → ThemePreferenceMapper → ConfigureRepositoryImpl → Get/Set/ObserveThemeUseCase → App.kt. Desktop backend PreferencesSettings(Preferences.userRoot().node("dev.rectify.io")). AC-35's persistence is already built. | source:data-preference-impl/.../ThemePreferenceSourceImpl.kt + data-repository-impl/.../ThemePreferenceMapper.kt + domain-usecase-api/.../configuration/ |
| koin-graph | initKoin() with the load-order-significant data → domain → presentation module list; new bindings append to their layer group | source:compose-application/src/commonMain/kotlin/dev/rectify/io/source/di/InitKoin.kt |
| convention-plugins | application/feature/library/di convention plugins with explicitApi(), jvm("desktop"), compose.desktop.currentOs on the application plugin only | source:build-logic/convention/src/main/kotlin/dev/rectify/io/convention/ |
| styleguide | StyleguideScreen + DemoHost (DEMO_MODE flag in App.kt) — existing visual QA surface for both themes | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/showcase/, compose-application/src/commonMain/kotlin/dev/rectify/io/core/demo/DemoHost.kt |
| template-features | onboarding/home/settings/about/splash + Navigation3 + Room + localisation — present, untouched, and unreachable on desktop once the shell is the root. Fate NOT decided here (open question). | source:settings.gradle.kts |

#### newToBuild

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt (organism)
- presentation-core-ui/.../source/kit/organism/shell/ShellToolbar.kt (organism)
- presentation-core-ui/.../source/kit/organism/shell/ShellStatusBar.kt (organism)
- presentation-core-ui/.../source/kit/organism/shell/StageSurface.kt (shell region, token-only imports)
- presentation-core-ui/.../source/kit/organism/shell/SidePanelFrame.kt (organism)
- presentation-core-ui/.../source/kit/organism/shell/ShellTab.kt (enum + shortcutToTab, pure)
- presentation-core-ui/.../source/kit/organism/shell/ShellState.kt (ShellState + rememberShellState + clampPanelWidth)
- presentation-core-ui/.../source/kit/organism/shell/ShellWindowDefaults.kt (window/panel geometry constants)
- presentation-core-ui/.../source/kit/atom/handle/PanelResizeHandle.kt (atom)
- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt
- data-preference-api/src/commonMain/kotlin/data/preference/api/source/model/SidePanelWidthPreference.kt
- data-preference-api/src/commonMain/kotlin/data/preference/api/source/datasource/SidePanelWidthPreferenceSource.kt
- data-preference-impl/src/commonMain/kotlin/data/preference/impl/source/datasource/SidePanelWidthPreferenceSourceImpl.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/SidePanelWidthPreferenceMapper.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/configuration/GetSidePanelWidthUseCase.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/configuration/SetSidePanelWidthUseCase.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/configuration/GetSidePanelWidthUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/configuration/SetSidePanelWidthUseCaseImpl.kt
- presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/WcagContrast.kt
- presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ClampPanelWidthTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShortcutToTabTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellTabTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellWindowDefaultsTest.kt
- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ShellStateTest.kt
- data-preference-impl/src/commonTest/kotlin/data/preference/impl/SidePanelWidthPreferenceSourceTest.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/ThemePreferenceMapperTest.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/SidePanelWidthPreferenceMapperTest.kt
- EDITS: build-logic/.../LibraryConventionPlugin.kt, .../FeatureConventionPlugin.kt, gradle/libs.versions.toml, data-preference-impl/build.gradle.kts, data-preference-impl/.../di/DataPreferenceImplModule.kt, data-repository-impl/.../source/ConfigureRepositoryImpl.kt, data-repository-impl/.../core/mapper/ThemePreferenceMapper.kt, domain-repository-api/.../ConfigureRepository.kt, domain-core/.../ThemeModel.kt, domain-usecase-impl/.../di/DomainUseCaseImplModule.kt, compose-application/.../window/DesktopEntry.kt, compose-application/.../app/App.kt

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md (root) | Module map, build commands (./gradlew :desktop-application:run), toolchain pins (Kotlin 2.4.21, JVM 21, Compose MP 1.12.1), and the statement that topic conventions live in .claude/rules/. |
| .claude/rules/design-tokens.md | THE binding constraint on every shell file: tokens only behind Theme from @Composable scope, no Color(0x...)/raw .dp/hardcoded TextStyle at call sites, AppTheme as sole provider, strict atom → molecule → organism import direction. |
| .claude/rules/package-layout.md | core/ internal-only, source/ public, never create an empty core/; explicitApi() means explicit visibility AND explicit return type on every public declaration — directly shapes every signature in architecture.interfaces. |
| .claude/rules/testing.md | Confirms ZERO tests exist; mandates wiring kotlin.test once in Library/Feature convention plugins rather than per module, and names pure deterministic logic as the first high-value target — exactly the shape of this plan's test strategy. |
| .claude/rules/preferences.md | PreferenceSource<T> shape (MutableStateFlow seeded on construction, write-through setData, fresh-read getData), the pref_<name> key convention, and the absolute prohibition on renaming an existing key — why pref_theme is untouched and pref_side_panel_width is new. |
| .claude/rules/mappers.md | ModelResourceMapper<MODEL, RESOURCE> pairing, mappers never throw and fall back in toModel (the one line that fixes the theme default), and the explicit allowance for plain types as MODEL — why the panel width needs no new domain model class. |
| .claude/rules/domain-layer.md + .claude/rules/error-handling.md | One interface per use case with exactly one suspend operator invoke returning Result<T>/Optional; every impl body through resultLauncher(errorMapper = Failure.Technical::Preference) so a java.util.prefs BackingStoreException can never crash the window. |
| .claude/rules/koin-di.md | One Koin module per Gradle module, singleOf(::Impl) bind Interface::class, and the load-order-significant data → domain → presentation list — new bindings append to the end of their layer group. |
| .claude/rules/convention-plugins.md | The 8-hook Template Method and its fixed order; kotlin.test goes inside the EXISTING configureCommonDependencies() of the library and feature plugins — no new hook (the rule explicitly discourages single-consumer hooks). Also: SDK/versions always via libs.findVersion/findLibrary. |
| .claude/rules/platform-coverage.md | 'Desktop' here is jvm("desktop") current-OS only; compose.desktop.currentOs lives solely on ApplicationConventionPlugin; :desktop-application is a plain-JVM module with no convention plugin — which is why the kotlin.test wiring cannot reach it and the window constants live in presentation-core-ui. |
| .claude/rules/compose-preview-convention.md | Private @Preview functions named <Composable>Preview, always wrapped in AppTheme { } (a preview outside AppTheme crashes on the first Theme.* access) — the format of the light/dark inspection evidence for AC-30/AC-31. |
| .claude/rules/orbit-mvi.md | Its frontmatter (paths: presentation-feature-*/**) is the documented reason plain state hoisting is compliant rather than a deviation — cited so the refiner and the human gate do not re-litigate binding decision 3. |
| .claude/rules/version-catalog-hygiene.md | junit is a dead pin (no test source set consumes it); a new catalog entry must be wired in the same change — both new entries (kotlin-test, multiplatform-settings-test) are wired immediately in steps 1 and 8. |
| .claude/rules/localisation.md + .claude/rules/compose-resources-namespace.md | New strings belong in presentation-core-localisation/src/commonMain/composeResources/values/strings.xml with generated Res.string accessors — relevant to the deliberate decision NOT to localise the AC-37 stub strings yet, recorded in i18n. |
| .claude/rules/navigation.md | Checked to confirm the desktop shell can bypass NavigationHost without breaking the Navigation3 contract on the other platforms; no Destination is added. |
| presentation-core-ui/CLAUDE.md | Authoritative package layout for the kit (atom/molecule/organism folders and the layering rule) — fixes exactly where each new shell file goes. |
| compose-application/CLAUDE.md + desktop-application/CLAUDE.md | core/ vs source/ in the app shell module, the Koin load order warning, and the rule that initKoin() stays in :desktop-application's main() and must not move into runDesktopApp(). |
| data-preference-api/CLAUDE.md + data-preference-impl/CLAUDE.md + data-repository-impl/CLAUDE.md + domain-usecase-api/CLAUDE.md + domain-usecase-impl/CLAUDE.md + domain-core/CLAUDE.md + domain-repository-api/CLAUDE.md | The four-to-six-step 'adding a new preference / use case / repository method' checklists these modules already document — the pref_side_panel_width chain follows them literally, which is what makes step 8 mechanical. |

#### skills

| name | why |
| --- | --- |
| (none) | No .claude/skills/ directory exists in this repository (only .claude/rules/ and .claude/settings.local.json). No project-local skill was discoverable, so none is claimed. |

#### agents

| name | why |
| --- | --- |
| (none) | No .claude/agents/ directory exists in this repository. None is claimed. |

#### docs

| ref | why |
| --- | --- |
| build-logic/convention/README.md | Plugin comparison table that must be updated if the convention-plugin change is considered user-visible; the kotlin.test addition does not add a plugin, but the table's dependency rows are the natural place to record it. |
| presentation-core-ui/CLAUDE.md package-layout tree | Must gain the organism/shell/ and atom/handle/ entries in the same change, or the module doc goes stale the moment the shell lands. |
| WCAG 2.2 SC 1.4.3 (contrast minimum, 4.5:1) and SC 1.4.11 (non-text contrast, 3:1, with its exemption for information also conveyed by other visual means) | The exact thresholds and the exemption clause the contrast sweep and the two open contrast questions turn on. |
| Upstream PRD green-signal note (hue 152) | Step 13 amends it to 150 to match the shipped token; this is the only documentation edit required by binding decision 6. |

### errorStates

| state | behavior |
| --- | --- |
| empty | No mirroring session: RectifyWindow passes sidePanel = null, so neither SidePanelFrame nor PanelResizeHandle is composed (AC-31). The toolbar shows DeviceChip(name = "No device", state = None), the staged counter shows CounterBadge(0, Empty, "staged"), Resolve & Send is rendered disabled, and StageSurface shows its dotted ground with a single neutral placeholder in the content slot. The richer empty-state content in screenshot 02 (dashed NO SIGNAL outline, instruction steps, Boot control) is downstream. |
| loading | There is no loading state in this deliverable — the shell has no async work. The one asynchronous read (panel width, in a LaunchedEffect(Unit)) resolves before or during the first frames; the panel simply opens at ShellWindowDefaults.PanelDefaultWidth until it arrives, and the panel is not even visible pre-session, so no flicker is user-visible. |
| error | Preference storage unreadable or read-only (java.util.prefs can throw BackingStoreException on a locked/sandboxed profile): both use-case impls wrap the repository call in resultLauncher(errorMapper = Failure.Technical::Preference), the window's .getOrNull()/.onFailure handling falls back to ThemeMode.System and 320dp, and the window still opens. A persistence failure must never block the window. A stored width outside 260..480 (corrupt or hand-edited prefs) is clamped on read by ShellState's init, never trusted. An unrecognised pref_theme string resolves to System via the mapper fallback. Cmd+1/2/3 pressed with no panel visible is a no-op and the event is NOT consumed. |

### observability

#### logs

- kermit is already a dependency of presentation-core-ui; the shell adds NO logging — there is no async work, no I/O and no failure path inside it worth a log line.
- RectifyWindow logs exactly one Logger.w line when the macOS full-size-content client properties cannot be applied (non-Mac os.name or a JDK that rejects them), because that silently changes the window's appearance and is otherwise invisible in a bug report.
- RectifyWindow logs one Logger.w line on a failed panel-width read or write (Failure.Technical.Preference), since the fallback is silent by design.

#### metrics

- None. No metrics pipeline exists in this repository and adding one for a window shell would be unrequested infrastructure.

#### analyticsEvents

- None. No analytics SDK exists in this repository; the shell is a local desktop layout with no product-analytics requirement in any FR/NFR.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | WCAG 2.2 AA thresholds are the standard held: >=4.5:1 for text against its background (SC 1.4.3) and >=3:1 for non-text UI components and graphical objects (SC 1.4.11). Ratios are computed from sRGB relative luminance per the WCAG formula ((L1+0.05)/(L2+0.05), with the 0.04045/12.92 linearisation), implemented in presentation-core-styling/src/commonTest/.../WcagContrast.kt — no library, ~20 lines. |
| A11Y-2 | Alpha tokens MUST be composited over their base surface before measurement. line (0x14), line2 (0x24), hover (0x0B), seg (0x0F) and codeBg (0x09) are rgba overlays (alpha 0.035–0.14) in both palettes; measuring them raw produces meaningless numbers. The sweep composites each over every surface it is actually drawn on (bg, chrome, stage, surface) using the standard src-over formula, mirroring what Compose's compositeOver does at runtime (the pattern AppButton already relies on). |
| A11Y-3 | pop MUST be paired with on-pop ink, never with the theme's default ink. pop is deliberately dark in BOTH palettes (#18191C light, #2C2D31 dark); pairing it with light-mode ink reports a false failure. ThemeColor has no onPop token, so the sweep pairs pop with Color.White following CounterBadge's documented precedent, and the absence of the token is raised as an open question rather than papered over. |
| A11Y-4 | Ink classification, written as data in the test rather than hidden in assertions: ink and ink2 are TEXT (4.5:1); ink3 is incidental/meta ink (3:1) because it measures approximately 3.1:1 on light bg and cannot reach 4.5:1 without modifying the palette, which AC-36 forbids. This classification is a flagged decision for the human gate, not a silent relaxation. |
| A11Y-5 | Signal colours (annotate, sentFixed, warnStillOff, measure) are measured against every surface they are drawn on in both palettes, at the 3:1 non-text threshold per AC-40 (they are dots and indicators, not text, in every kit component that uses them). Two light-mode pairs are expected to fail (sentFixed ≈2.9:1 and warnStillOff ≈2.4:1 on surface #FFFFFF); they are pinned to a documented baseline and escalated, not suppressed. |
| A11Y-6 | Keyboard operability: Cmd+1/2/3 switch tabs without a pointer (FR-16/AC-33); SidebarTabs' items carry Role.Tab and AppButton carries Role.Button, both already shipped; AppButton already renders a focus ring from Theme.color.focus only in InputMode.Keyboard, so keyboard navigation of the toolbar stubs is already visible. The shell adds no interactive element without a role. |
| A11Y-7 | PanelResizeHandle has a pointer-target of at least 8dp (hit area) around a 1dp visual hairline and sets an E-resize hover cursor so the affordance is discoverable. KNOWN GAP, flagged not hidden: it is pointer-only — there is no keyboard path to resize the panel. WCAG 2.2 SC 2.1.1 would want one; the panel width is a non-essential preference with a working default and clamped bounds, so this plan accepts the gap and records it as an open question rather than inventing an unrequested keyboard resize gesture. |
| A11Y-8 | Reduced motion is already handled centrally: AppTheme(reducedMotion) resolves Theme.motion, and components read Theme.motion.durations.* rather than branching on their own boolean. The shell introduces no animation of its own, so it inherits this for free. |

### performanceBudget

| metric | target |
| --- | --- |
| Panel resize responsiveness | Width follows the pointer within one frame (no animation, no snapping); clampPanelWidth is pure arithmetic on a Dp, and only the panel's width modifier is invalidated, not the stage content. |
| Preference writes during a drag | EXACTLY ZERO writes while dragging, and exactly one write per drag that changes the width (zero if it does not change). Enforced by ShellState.onDragStopped() returning null on no-change and covered by ShellStateTest — java.util.prefs flushes to disk, so a per-frame write would be pathological. |
| Stage texture cost | The dotted grid is drawn in a single Canvas pass over the visible stage area with no per-dot allocation inside the draw loop (precompute the step from LocalDensity); it must not re-draw on panel resize beyond the stage's own size change. |
| Cold window open | The window must open and paint without waiting on preference I/O — the width read happens in a LaunchedEffect after first composition, and the panel is not visible pre-session anyway. |
| Test suite wall time | The four :desktopTest tasks complete in well under a minute on a warm Gradle daemon; all tests are pure JVM with no Compose UI harness, no device and no Gradle configuration beyond the one-time kotlin.test wiring. |

### i18n

#### locales

- en — the only locale shipped today (presentation-core-localisation/src/commonMain/composeResources/values/strings.xml is the single values file in the repo)

#### strings

- ShellTab labels: "Notes", "Payload", "History" — currently plain English constants on the enum. These are real UI, not stubs, and SHOULD move to strings.xml when the tabs gain content; doing it now means a localisation key for a tab whose content does not exist.
- AC-37 stub strings, deliberately NOT localised because they are placeholder content a downstream spec replaces: "Android", "iOS", "No device", "staged", "Clear", "Resolve & Send", "⌘↵", "adb 35.0.1 · idb 1.1.8 · ready", "Rectify 0.4".
- Machine output (tool versions, device ids, FPS, frame numbers) is never localised per the style guide's mono rule — it stays verbatim in Theme.typography.monoMeta/monoData.

#### notes

New strings would go in presentation-core-localisation/src/commonMain/composeResources/values/strings.xml and be read through the generated rectify_kmp.presentation_core_localisation.generated.resources.Res accessors (.claude/rules/localisation.md, compose-resources-namespace.md). This deliverable adds none: localising AC-37 placeholders would create keys that are deleted by the next spec. Flagged for the refiner: if the human gate wants the three tab labels localised now, it is three keys and one import.

### security

#### dataHandling

- The only data this deliverable persists is one integer (panel width in dp) and one existing theme identifier string, both in the local desktop preference store java.util.prefs.Preferences.userRoot().node("dev.rectify.io"). No PII, no credentials, no device identifiers.
- Nothing is transmitted anywhere — this deliverable makes no network call and adds no network dependency (there is still no network layer in this repo; .claude/rules/network.md).

#### permissions

- None requested or required. The shell does not touch adb/idb, the filesystem, the camera, the screen-recording APIs or any entitlement-gated macOS capability — all of that arrives with the downstream device-mirroring spec.

#### privacy

- No analytics, no telemetry, no crash reporting is added.
- The mirrored device screen (the privacy-sensitive surface in this product) is downstream content rendered into StageSurface's slot; this deliverable never acquires, stores or forwards a frame.

### rollout

#### featureFlags

- None. The desktop root switches from App() to RectifyWindow in one commit — a flag would mean maintaining two desktop roots for a change whose blast radius is one function body, and the repo has no flag infrastructure.
- The existing DEMO_MODE const in App.kt is NOT a release flag and must remain false; it is the StyleguideScreen escape hatch and is useful as inspection evidence during step 12 only.

#### migrations

- No data migration. pref_theme's key and stored values are untouched; ThemeModel.System deliberately keeps the stored string "materialu", so any existing install carrying that value keeps resolving to the same mode under its new, honest name.
- pref_side_panel_width is a new key: absent on every existing install, which is exactly the documented first-run default path (320dp).
- Behavioural change worth calling out in release notes: the effective first-run theme default moves from Light to System on ALL platforms (the ThemePreferenceMapper fallback and App.kt's initial value are shared). Existing installs that explicitly chose a theme are unaffected; installs that never chose one will follow the OS from the next launch.

#### backwardCompatibility

Android and iOS keep booting App() → NavigationHost with every template feature intact; only their default theme changes. Desktop's visible behaviour changes completely and intentionally: the window now opens the Rectify shell at 1240x820 instead of the template navigation host. The public API of presentation-core-ui is additive only (new organisms, one new atom, one new object); no existing component signature changes. ConfigureRepository gains two methods, which is a source-incompatible change for any out-of-tree implementer — there is exactly one implementer in this repo (ConfigureRepositoryImpl) and no published artifact, so the impact is nil.

### knownRisksFromQA


### implementationPlan

| step | title | dependsOn | linkedAC | estimate | changes | verification |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Wire kotlin.test once into the Library and Feature convention plugins (the repo's first test infrastructure) | [] | [] | S | Add kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" } to gradle/libs.versions.toml [libraries]. In BOTH build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/library/LibraryConventionPlugin.kt and .../feature/FeatureConventionPlugin.kt, extend the EXISTING configureCommonDependencies() with sourceSets.commonTest.dependencies { implementation(libs.findLibrary("kotlin-test").get()) }. No new BaseConventionPlugin hook. Verify with ./gradlew :build-logic:convention:assemble, then a throwaway assertTrue(true) test in presentation-core-ui to confirm :presentation-core-ui:desktopTest exists and runs, then delete it. | ./gradlew :build-logic:convention:assemble and ./gradlew :presentation-core-ui:desktopTest both succeed. |
| 2 | WCAG contrast sweep over both shipped palettes, in presentation-core-styling's own commonTest | [1] | ["AC-30", "AC-36", "AC-40"] | M | New presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/WcagContrast.kt: relativeLuminance(Color), contrastRatio(a, b), compositeOver(fg, bg) — ~20 lines, no dependency. New .../contrast/PaletteContrastTest.kt: iterates attributeLightColorPalette and attributeDarkColorPalette (visible because the test lives in this module) over an explicit, named pairing table — text pairs (ink/ink2 on bg/chrome/stage/surface, onPrimary on primary/primaryHover/primaryPress, White on pop) at 4.5:1; non-text pairs (ink3, composited line/line2/hover/seg/codeBg over each base surface, focus on each surface) at 3:1; and a SIGNAL tier (annotate/sentFixed/warnStillOff/measure over each surface) asserted against a checked-in baseline map so pairs at or above 3:1 can never regress and the two expected light-mode failures are recorded with their measured value rather than suppressed. NO production file in this module is touched. | ./gradlew :presentation-core-styling:desktopTest; git diff shows only new files under src/commonTest. |
| 3 | Shell pure logic and state: ShellTab, shortcutToTab, ShellWindowDefaults, clampPanelWidth, ShellState, rememberShellState (+ their tests) | [1] | ["AC-12", "AC-32", "AC-33", "AC-34", "AC-38"] | M | Create presentation-core-ui/src/commonMain/.../source/kit/organism/shell/{ShellTab.kt, ShellWindowDefaults.kt, ShellState.kt} with the signatures in architecture.interfaces, plus the five commonTest files (ClampPanelWidthTest, ShortcutToTabTest, ShellTabTest, ShellWindowDefaultsTest, ShellStateTest). This step deliberately lands BEFORE any composable, so the whole automatable acceptance surface exists and is green before a single pixel is drawn. | ./gradlew :presentation-core-ui:desktopTest — all five files green. |
| 4 | PanelResizeHandle atom and StageSurface | [3] | ["AC-14", "AC-38"] | S | New .../source/kit/atom/handle/PanelResizeHandle.kt (draggable hit area, Theme.color.line2 hairline, E-resize hover cursor, no clamping, no persistence). New .../organism/shell/StageSurface.kt (Theme.color.stage ground, Canvas dotted grid from private geometry, content slot, explicitly NO border/bezel). Both carry a private @Preview wrapped in AppTheme. | Previews render; ./gradlew :presentation-core-ui:build passes explicitApi(). |
| 5 | ShellToolbar and ShellStatusBar organisms, assembled from existing kit components | [4] | ["AC-37"] | M | New .../organism/shell/ShellToolbar.kt (six named slots, chrome fill, bottom hairline, reserved leading inset) and .../organism/shell/ShellStatusBar.kt (leading/trailing slots, chrome fill, top hairline). ZERO new leaf components: the previews fill the slots with the already-shipped SegmentedButtonGroup, DeviceChip, CounterBadge, AppButton and StatusIndicator. If a slot seems to need a new component, stop and re-check the kit first. | Light and dark previews match screenshot 01's toolbar and status-bar composition on inspection. |
| 6 | SidePanelFrame reusing SidebarTabs with exactly three tabs | [5] | ["AC-12", "AC-32"] | S | New .../organism/shell/SidePanelFrame.kt built from ShellTab.entries, defaulting to Notes, with content as a per-tab slot and an optional tabCounts map (SidebarTabItem already supports a count). Header geometry comes from SidebarTabs' existing 26dp tab height and 6dp radius — this is the part with no design comp, so keep it minimal and flag it. | Preview shows Notes · Payload · History with Notes selected; ShellTabTest already pins the count and order. |
| 7 | RectifyWindowShell — compose the slot contract, with four previews | [6] | ["AC-12", "AC-30", "AC-31"] | M | New .../organism/shell/RectifyWindowShell.kt: Column[toolbar, Row[StageSurface(weight 1f), (handle + panel only when sidePanel != null)], statusBar] on Theme.color.bg, wiring PanelResizeHandle → state.resizePanelBy / onDragStarted / onDragStopped()?.let(onPanelWidthCommit). Four private @Preview functions: no-panel and with-panel, each in ThemeMode.Light and ThemeMode.Dark — these are the standing inspection evidence for AC-30/AC-31. Update presentation-core-ui/CLAUDE.md's package tree in the same commit. | All four previews render; the no-panel preview composes neither handle nor panel. |
| 8 | pref_side_panel_width through the full preference chain | [1] | ["AC-39"] | M | data-preference-api: SidePanelWidthPreference (Resource, default 320) + SidePanelWidthPreferenceSource. data-preference-impl: SidePanelWidthPreferenceSourceImpl (KEY_SIDE_PANEL_WIDTH = "pref_side_panel_width") + append singleOf(::SidePanelWidthPreferenceSourceImpl) bind SidePanelWidthPreferenceSource::class to DataPreferenceImplModule + add multiplatform-settings-test to the catalog and to this module's commonTest. data-repository-impl: SidePanelWidthPreferenceMapper + two methods on ConfigureRepositoryImpl + the new constructor parameter. domain-repository-api: two methods appended to ConfigureRepository with KDoc. domain-usecase-api/-impl: Get/SetSidePanelWidthUseCase (+Impls through resultLauncher) and APPEND the two bindings to DomainUseCaseImplModule. Tests: SidePanelWidthPreferenceSourceTest (round-trip over MapSettings: unset→320, write→read, survives a new instance over the same backing map) and SidePanelWidthPreferenceMapperTest. This step is independent of steps 2–7 and can run in parallel. | ./gradlew :data-preference-impl:desktopTest :data-repository-impl:desktopTest; app still boots (Koin graph resolves). |
| 9 | Flip the effective theme default from Light to System | [1] | ["AC-35"] | S | domain-core/ThemeModel.kt: rename MaterialU → System KEEPING mode = "materialu", fix the KDoc. data-repository-impl/ThemePreferenceMapper.kt: toModel fallback Light → System (the single line that makes the already-shipped DEFAULT_THEME string "system" resolve correctly instead of silently landing on Light). compose-application/App.kt: rename the when-branch and change both the null branch and collectAsState(initial = …) from ThemeMode.Light to ThemeMode.System. New ThemePreferenceMapperTest pinning: "light"→Light, "dark"→Dark, "materialu"→System, "system"→System, garbage→System, and round-trip toResource(System).theme == "materialu". | ./gradlew :data-repository-impl:desktopTest; grep confirms only the two known MaterialU call sites changed; pref_theme's key string is untouched. |
| 10 | RectifyWindow — window geometry, macOS chrome, key handling, theme and width wiring | [7, 8, 9] | ["AC-33", "AC-34", "AC-35", "AC-39"] | L | FIRST, spike AS-3: run the app, change the macOS appearance, and record whether isSystemInDarkTheme() recomposes — if it does not, note the limitation against FR-18 before building the toggle. Then new compose-application/src/desktopMain/.../window/RectifyWindow.kt per architecture.components (rememberWindowState(ShellWindowDefaults.InitialSize), window.minimumSize via AWT Dimension inside FrameWindowScope, the three apple.awt rootPane client properties behind an os.name check, onPreviewKeyEvent → shortcutToTab consumed only when the panel is visible, ObserveThemeUseCase → ThemeMode, GetSidePanelWidthUseCase in a LaunchedEffect(Unit) → state.setPanelWidth, SetSidePanelWidthUseCase from onPanelWidthCommit in a rememberCoroutineScope). Edit DesktopEntry.kt to render RectifyWindow. :desktop-application/Main.kt stays untouched. | ./gradlew :desktop-application:run — window opens at 1240x820, refuses to go below 1024x700, traffic lights sit inside the toolbar behind the inset. |
| 11 | Fill every slot with static stubs and add the status-bar theme-override control | [10] | ["AC-35", "AC-37", "AC-31"] | S | In RectifyWindow: platformSwitch = SegmentedButtonGroup(Android\|iOS, selected 0, onSelect = {}); deviceIndicator = DeviceChip("No device", None, onClick = {}); stagedCounter = CounterBadge(0, Empty, "staged"); clearAction = AppButton("Clear", Ghost); primaryAction = AppButton("Resolve & Send", shortcut = "⌘↵", enabled = false); statusBar leading = StatusIndicator(Offline, "adb 35.0.1 · idb 1.1.8 · ready"), trailing = Text("Rectify 0.4", monoMeta, ink3) plus an IconButton(Sun) cycling System → Light → Dark through SetThemeUseCase. Stage content = one neutral placeholder; sidePanel = null. Nothing reads adb. | Running window matches screenshot 01's toolbar/status bar; the theme control switches palettes instantly and survives relaunch. |
| 12 | Inspection evidence pass | [11] | ["AC-12", "AC-30", "AC-31", "AC-34", "AC-36", "AC-37", "AC-40"] | S | Run the app in both themes; capture screenshots of the pre-mirror window (light and dark) and of the with-panel state (from the previews, since no session exists yet); compare against the two cached designs; record the evidence paths in the Stage 5 verification artifact. Optionally flip DEMO_MODE to true locally to capture the StyleguideScreen in both themes as token evidence — and flip it back to false before committing. | Evidence captured for every inspection-only AC; DEMO_MODE is false in the committed tree. |
| 13 | Amend the PRD's green-signal note from oklch hue 152 to the shipped 150 | [] | ["AC-36", "AC-40"] | XS | Documentation only, per binding decision 6. No code, and specifically no edit to LightColor.kt or DarkColor.kt — editing the palette to chase a sub-JND 2-degree hue shift would itself violate AC-36. | The PRD and the shipped token agree; git diff touches no .kt file. |

### testStrategy

#### unit

- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ClampPanelWidthTest.kt — clampPanelWidth(200.dp) == 260.dp; clampPanelWidth(600.dp) == 480.dp; clampPanelWidth(320.dp) == 320.dp; the exact bounds 260.dp and 480.dp are returned unchanged. [AC-38]
- presentation-core-ui/src/commonTest/.../ShortcutToTabTest.kt — shortcutToTab(true, Key.One/Two/Three) == Notes/Payload/History; shortcutToTab(false, Key.One) == null (no Cmd); shortcutToTab(true, Key.Four) == null; shortcutToTab(true, Key.A) == null. [AC-33]
- presentation-core-ui/src/commonTest/.../ShellTabTest.kt — ShellTab.entries.size == 3; the order is exactly Notes, Payload, History; labels are exactly "Notes", "Payload", "History" (not SidebarTabs' KDoc example 'Annotations'). This is the automatable half of AC-12. [AC-12]
- presentation-core-ui/src/commonTest/.../ShellWindowDefaultsTest.kt — InitialSize == DpSize(1240.dp, 820.dp); MinimumSize == DpSize(1024.dp, 700.dp); PanelMinWidth/PanelMaxWidth/PanelDefaultWidth == 260/480/320.dp. Converts AC-34's and FR-20/21's numbers from prose into a failing-or-passing check. [AC-34]
- presentation-core-ui/src/commonTest/.../ShellStateTest.kt — activeTab defaults to Notes; selectTab changes it; onSessionStarted() resets activeTab to Notes but does NOT change panelWidth (the two different lifetimes of AC-32 vs AC-39); a constructor value outside the bounds is clamped on read; resizePanelBy clamps at both ends; onDragStarted/onDragStopped returns the new width when it changed and null when it did not (the 'never write per frame, never write unchanged' rule). [AC-32, AC-38, AC-39]
- presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt — the two-tier WCAG sweep over BOTH palettes described in accessibility A11Y-1..A11Y-5, with WcagContrast.kt as its helper. MUST tier hard-fails; SIGNAL tier asserts against a documented baseline. [AC-30, AC-36, AC-40, NFR-9, NFR-11]
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/ThemePreferenceMapperTest.kt — "light"→Light, "dark"→Dark, "materialu"→System, "system"→System (the shipped DEFAULT_THEME), unrecognised→System, and toResource(System).theme == "materialu" so no stored value is orphaned. [AC-35]
- data-repository-impl/src/commonTest/.../SidePanelWidthPreferenceMapperTest.kt — toModel/toResource round-trip for 260, 320 and 480. [AC-39]

#### integration

- data-preference-impl/src/commonTest/kotlin/data/preference/impl/SidePanelWidthPreferenceSourceTest.kt — against com.russhwolf:multiplatform-settings-test's MapSettings: unset reads 320; setData(480) then getData() returns 480; observeData() emits the new value; a FRESH SidePanelWidthPreferenceSourceImpl over the same backing settings reads 480 (this is the relaunch half of AC-39 — the only honest way to test 'survives restart' without actually restarting a window). [AC-39]
- NOT automated, by deliberate scope decision (binding decision 5): the Compose-side integration — pointer drag on PanelResizeHandle, click on SidebarTabs, the real onPreviewKeyEvent dispatch, and the AWT minimumSize enforcement. These are verified by inspection in steps 10–12. Everything downstream of them (the clamp, the mapping, the commit rule, the constants) IS automated above, so the untested surface is the Compose/AWT plumbing itself, not Rectify's logic.

#### ui

- No Compose UI-test harness in this deliverable (no runComposeUiTest, no ui-test-junit4) — binding decision 5.
- Standing visual evidence instead: four private @Preview functions on RectifyWindowShell (no-panel/with-panel x Light/Dark) plus the existing per-component previews, each wrapped in AppTheme per .claude/rules/compose-preview-convention.md.
- Manual inspection script for step 12: (1) ./gradlew :desktop-application:run; (2) confirm 1240x820 and that dragging the window edge stops at 1024x700; (3) confirm the traffic lights sit inside the toolbar behind the inset; (4) compare the pre-mirror window against raw/design/02-empty-no-device.png (bezel-less dotted stage, no panel, '● No device', '0 staged', disabled Resolve & Send ⌘↵, mono status line left, 'Rectify 0.4' right); (5) cycle the status-bar theme control through System/Light/Dark and relaunch to confirm persistence; (6) capture both themes as evidence.

#### linkedScenarios

- SC-1
- SC-2
- SC-3
- SC-4
- SC-5

#### coverageGoal

Not a line-coverage target — this repo has no coverage tooling and adding some would be unrequested infrastructure. The goal is acceptance coverage: every AC with verificationMethod=test (AC-33, AC-35, AC-38, AC-39) is covered by at least one named test file, and four of the eight inspection-only ACs (AC-12, AC-34, AC-36, AC-40) are additionally backed by an automated assertion. The four that remain inspection-only (AC-30, AC-31, AC-32 rendering, AC-37) are each pinned to a named preview or screenshot comparison in the traceabilityMatrix. No AC is unmapped.

#### command

./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest

#### commandNotes

Stage 5 runs exactly this, NOT ./gradlew check — check would also build the Android and iOS targets of every module for zero benefit to this deliverable (brainstorm option A's documented mitigation). The desktopTest task name comes from the jvm("desktop") target registered by the Library/Feature convention plugins. Build sanity separately with ./gradlew :presentation-core-ui:build :compose-application:build, and run the app with ./gradlew :desktop-application:run.

### traceabilityMatrix

| ac | functional | components | tests | steps | verification |
| --- | --- | --- | --- | --- | --- |
| AC-12 | ["FR-11", "FR-15"] | ["ShellTab", "SidePanelFrame", "SidebarTabs (reuse)", "StageSurface", "RectifyWindowShell"] | ["ShellTabTest (entries.size == 3, order, labels)", "SC-3", "step 6 + step 7 previews (inspection: three tabs over a bezel-less stage)"] | [3, 6, 7, 12] | test + inspection |
| AC-30 | ["FR-18", "FR-19", "FR-23"] | ["AppTheme (reuse)", "both palettes (reuse)", "RectifyWindowShell previews", "ThemeOverrideToggle (IconButton+Sun reuse)"] | ["PaletteContrastTest (MUST tier: no unreadable surface/ink region in either palette)", "SC-2", "step 12 light+dark screenshots"] | [2, 7, 11, 12] | inspection, partially automated by the sweep |
| AC-31 | ["FR-12", "FR-14", "FR-15"] | ["RectifyWindowShell (nullable sidePanel slot)", "ShellToolbar", "StageSurface", "DeviceChip (reuse, None state)", "RectifyWindow"] | ["SC-4", "RectifyWindowShell no-panel preview (Light+Dark)", "step 12 comparison against raw/design/02-empty-no-device.png"] | [4, 5, 7, 11, 12] | inspection — the nullability of the slot is the structural guarantee; no automated render assertion exists by design |
| AC-32 | ["FR-16", "FR-15"] | ["ShellState.onSessionStarted()", "ShellTab.Notes", "SidePanelFrame"] | ["ShellStateTest (default is Notes; onSessionStarted resets activeTab and leaves panelWidth alone)", "SC-4", "step 6 preview"] | [3, 6] | test (state) + inspection (render) |
| AC-33 | ["FR-16"] | ["shortcutToTab", "ShellState.selectTab", "RectifyWindow onPreviewKeyEvent", "SidebarTabs (reuse, click path)"] | ["ShortcutToTabTest (Cmd+1/2/3 mapping, no-Cmd and unknown-key null cases)", "SC-3"] | [3, 10] | test — the mapping is automated; the onPreviewKeyEvent dispatch itself is inspection |
| AC-34 | ["FR-17"] | ["ShellWindowDefaults", "RectifyWindow (rememberWindowState + AWT window.minimumSize)"] | ["ShellWindowDefaultsTest (1240x820, 1024x700)", "step 10/12 manual resize check"] | [3, 10, 12] | test (constants) + inspection (live AWT enforcement) |
| AC-35 | ["FR-18", "FR-19"] | ["ThemeModel (edit)", "ThemePreferenceMapper (edit)", "App.kt (edit)", "ObserveThemeUseCase/SetThemeUseCase (reuse)", "AppTheme (reuse)", "ThemeOverrideToggle"] | ["ThemePreferenceMapperTest (default/unknown → System; 'materialu' round-trip)", "SC-2", "step 11 manual toggle + relaunch"] | [9, 10, 11] | test (persistence/mapping) + inspection (immediate re-render) |
| AC-36 | ["FR-19", "FR-23"] | ["ThemeColor + both palettes (reuse, unmodified)", "AppTheme (reuse)", "every shell component (token-only call sites)"] | ["PaletteContrastTest (every neutral token pairing, alpha tokens composited, pop paired with on-pop ink)", "git diff over presentation-core-styling/src/commonMain must be empty", "grep for Color(0x / raw .dp / TextStyle( in the new shell files must return nothing at a call site"] | [2, 4, 5, 6, 7, 12] | inspection + automated sweep; see openQuestions for the ink3 and signal-contrast conflicts this AC cannot currently satisfy as literally written |
| AC-37 | ["FR-12", "FR-13"] | ["ShellToolbar (six named slots)", "ShellStatusBar", "SegmentedButtonGroup/DeviceChip/CounterBadge/AppButton/StatusIndicator (reuse, stub-configured)"] | ["step 11 slot fill + step 12 comparison against raw/design/01-first-run-platform-source.png", "code review: no adb/idb/device import anywhere in the shell or RectifyWindow"] | [5, 11, 12] | inspection |
| AC-38 | ["FR-20"] | ["clampPanelWidth", "ShellState.resizePanelBy", "PanelResizeHandle", "RectifyWindowShell"] | ["ClampPanelWidthTest (260/480 bounds)", "ShellStateTest (clamping through resizePanelBy)", "SC-5"] | [3, 4, 7] | test — the drag gesture itself is inspection, everything it feeds is automated |
| AC-39 | ["FR-21", "FR-15"] | ["SidePanelWidthPreference(+Source/+Impl)", "SidePanelWidthPreferenceMapper", "ConfigureRepository (edit)", "Get/SetSidePanelWidthUseCase", "ShellState.onDragStopped", "RectifyWindow"] | ["SidePanelWidthPreferenceSourceTest (round-trip incl. a fresh instance over the same store = the relaunch proxy)", "SidePanelWidthPreferenceMapperTest", "ShellStateTest (commit only on change)", "SC-5"] | [3, 8, 10] | test |
| AC-40 | ["FR-23", "FR-19"] | ["both palettes' signal tokens (reuse, identical across themes)", "PaletteContrastTest SIGNAL tier", "DeviceChip/StatusIndicator/StatusPill/CounterBadge (reuse — every signal dot is paired with a text label)"] | ["PaletteContrastTest SIGNAL tier at 3:1 with a pinned baseline", "step 12 light+dark inspection"] | [2, 12] | inspection + automated sweep; two light-mode pairs are expected to fail the literal 3:1 threshold — escalated, not suppressed (see risks and openQuestions) |
| NFR-9 / NFR-11 (carried, no AC id of their own) | ["FR-19", "FR-23"] | ["both palettes", "WcagContrast.kt", "PaletteContrastTest"] | ["PaletteContrastTest (both tiers, both palettes)"] | [2] | test |
| NFR-10 (carried) | [] | ["compose.desktop.currentOs (ApplicationConventionPlugin, reuse)", "RectifyWindow os.name guard"] | ["Satisfied by construction: only :desktop-application produces a runnable artifact and it builds for the current OS only; the macOS client properties are applied behind an os.name check so a non-Mac host degrades rather than breaks."] | [10] | inspection |

### openQuestions

- CARRIED FROM STAGE 1 (1/7): No dark-theme design reference exists for the shell; both cached screenshots are LIGHT. The dark palette is adopted and will be validated by the automated contrast sweep plus token correctness, not against a visual comp — flag for design review once the shell is running.
- CARRIED FROM STAGE 1 (2/7): The three-tab side panel's header height, padding, tab-row placement and divider treatment are designed here from FR-11 plus SidebarTabs' existing geometry (26dp tab height, 6dp radius), not copied from a screenshot — neither cached design shows the panel. Flag for design review.
- CARRIED FROM STAGE 1 (3/7): The bezel-less stage's dotted/grid texture has no specified cell size, dot radius or opacity token. This plan approximates it as private component geometry in StageSurface.kt (cell 16dp, dot radius 0.75dp, ink3 at alpha 0.28) and proposes promoting it to a named ThemeStageTexture token group in presentation-core-styling ONLY if the values survive design review — adding tokens now for guessed values would be the palette modification AC-36 forbids in spirit.
- CARRIED FROM STAGE 1 (4/7): AS-3 — macOS system-appearance detection via isSystemInDarkTheme() on JVM/Skiko has never been observed as a LIVE appearance switch on a shipped desktop build. Spiked at the start of step 10; a fallback would need an NSApplication appearance observer, which is real work and out of scope.
- CARRIED FROM STAGE 1 (5/7): No decision exists on what happens to this repo's existing template features (onboarding/home/settings/about/splash, Navigation3, Room, localisation) once the shell becomes the desktop root. The shell bypasses NavigationHost on desktop only, leaving those paths live on Android/iOS and dead on desktop. NOT decided here — needs its own cleanup spec.
- CARRIED FROM STAGE 1 (6/7): No macOS packaging (nativeDistributions / packageDmg) is configured in :desktop-application — out of scope for this shell, but the first thing missing before a shippable macOS build exists.
- CARRIED FROM STAGE 1 (7/7): Monospace (JetBrains Mono) and SF Pro font availability in the desktop build is unverified. The typography tokens exist, but if the desktop silently falls back, every monoMeta/monoData string in the toolbar and status bar renders in the wrong face — worth a glance during step 12.
- NEW — CONTRAST CONFLICT A (blocks AC-40 sign-off): sentFixed #3EAB5E measures approximately 2.9:1 and warnStillOff #E69825 approximately 2.4:1 against surface #FFFFFF in the LIGHT palette, both below the 3:1 non-text threshold NFR-11/AC-40 demand. The tokens cannot be changed (AC-36 + binding decision 6). Choose: (a) accept the WCAG 1.4.11 exemption on the grounds that every signal dot in the kit is paired with a text label (DeviceChip, StatusPill, StatusIndicator, StatusLine all do), so the colour is never the sole information carrier; (b) restrict signal dots to surfaces where they pass; (c) amend the palette, contradicting AC-36. This plan implements (a) with a pinned baseline so regressions still fail, and escalates the choice rather than making it.
- NEW — CONTRAST CONFLICT B (blocks AC-36 sign-off): ink3 #8B8B91 measures approximately 3.1:1 on bg #F6F6F5 in the LIGHT palette but is used as TEXT (monoMeta, placeholders, counts) across the kit, so a literal reading of AC-36/NFR-11's 4.5:1 text rule fails. This plan classifies ink3 as incidental/meta ink at 3:1 and records the classification as data in the test. Needs an explicit human accept-or-change decision; darkening ink3 would violate AC-36.
- NEW: ThemeColor has no onPop token, yet pop is deliberately dark in BOTH palettes. The sweep pairs pop with Color.White following CounterBadge's documented precedent. Should an onPop token be added (a palette change, so not in this deliverable), or is the literal-white convention accepted and documented?
- NEW: PanelResizeHandle is pointer-only — there is no keyboard path to resize the side panel (WCAG 2.2 SC 2.1.1). This plan accepts the gap because the width is a non-essential preference with a working default and clamped bounds. Confirm, or schedule a keyboard affordance (e.g. Ctrl+Alt+arrow) in a later spec.
- NEW: ShellTab's labels ("Notes", "Payload", "History") are plain English constants, not localised strings. They are real UI rather than AC-37 stubs. Localise now (three keys in presentation-core-localisation's strings.xml), or when the tabs gain content? This plan defers.
- NEW: The toolbar's reserved traffic-light inset is a single 78dp constant. The real inset depends on the macOS title-bar height and whether the window is in fullscreen. Confirm after step 10 whether a fixed value is good enough or whether it must be measured from the rootPane.
- NEW: ConfigureRepository gains getSidePanelWidth/setSidePanelWidth but NO observeSidePanelWidth (YAGNI — the width is read once at launch and written on drag-end). Confirm nothing downstream needs to react to width changes across windows; adding the observe variant later is three small files.
- NEW: AC-34 says the window 'cannot be resized below 1024x700', but window.minimumSize is an AWT Dimension in AWT units. Confirm during step 10 that the behaviour is correct on a 2x Retina display and is not silently halved or doubled.
- NEW: The dead junit = "4.13.2" version pin in gradle/libs.versions.toml has no consumer and now sits next to a real kotlin-test entry (version-catalog-hygiene.md explicitly names it as dead scaffolding). Remove it in this change, or leave it for a separate tidy? This plan leaves it, to keep the diff about the shell.

### readyForBacklog

false
