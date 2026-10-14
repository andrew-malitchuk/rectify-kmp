# app-shell-window-layout — plan

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | plan |
| status | done |
| producedBy | sdd-build-plan-refine |
| schemaVersion | 1 |
| createdAt | 2026-10-14T19:27:45Z |
| updatedAt | 2026-10-14T19:32:19Z |

## Gate

### type

human

### approved

true

### approvedBy

human

## Provenance

### selfHash

e2130d43a2c30655ac9375c37bb1bc293a7c4595e1a8f0dbe8c510b2e5d9b344

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | b2550ab6226dcf9b1e5cedd7badbf431cb6b43c8f30680dd1585dadbc579ebb0 |
| plan/candidates/candidate-1.json | 010a71c30e34b2ead94e019806befba01e1c0e85ff878f3e03969abc08c42431 |
| plan/evaluation.json | ab51ca4e1e8d6b55e464d6edb69fbe039fbbd5918b8ad59bac5e230e91bf3af2 |

## Data

### meta

#### title

App Shell & Canonical Window Layout — Implementation Plan (final)

#### version

2

#### status

final

### goal

Ship the canonical Rectify macOS window shell inside the rectify-kmp repository — a toolbar frame, a bezel-less live-mirror stage, a resizable three-tab side panel frame (Notes/Payload/History) and a status bar — assembled from the already-shipped Rectify UI Kit (presentation-core-styling tokens + the presentation-core-ui component library), wired to the existing AppTheme/ThemeMode and multiplatform-settings chains, with every downstream-owned region exposed as a typed composable slot and every slot stub-filled. The slot contract (RectifyWindowShell) is the deliverable's real public API: it is what android-device-discovery-mirroring and every later spec builds against.

### problemStatement

No shared window shell exists. Every downstream feature (device discovery, annotation, payload, history) needs one canonical layout to render inside; without it each feature improvises its own chrome. A complete light/dark token system (ThemeColor, AppTheme) and the Rectify UI-kit components already exist in this repo — the shell must assemble them, not re-author or fork them. The desktop entry point today opens an un-sized window rendering App() -> NavigationHost() (the template app), which is not the Rectify shell.

### highLevelDescription

Three additions, no new Gradle module. (1) presentation-core-ui gains a shell organism family under source/kit/organism/shell/ plus one atom (PanelResizeHandle): RectifyWindowShell = Column[ShellToolbar, Row[StageSurface(weight 1f), PanelResizeHandle, SidePanelFrame(panelWidth)], ShellStatusBar] on Theme.color.bg, with the side panel as a NULLABLE slot so the pre-mirror state (AC-31) is a type, not a flag. Shell state (activeTab, panelWidth) is plain Compose state hoisting via rememberShellState — no Orbit MVI, no ViewModel, so the shell stays previewable and usable from desktopMain without Koin. (2) compose-application/desktopMain gains RectifyWindow: rememberWindowState(1240x820), AWT window.minimumSize 1024x700, macOS full-size-content rootPane client properties so the OS traffic lights sit inside the Rectify toolbar behind a reserved leading inset, onPreviewKeyEvent -> shortcutToTab for Cmd+1/2/3, theme through the existing ObserveThemeUseCase, panel width read once at launch and written only on drag-end. runDesktopApp() renders RectifyWindow instead of App(). (3) One new persisted preference, pref_side_panel_width (Int dp, default 320), through the full PreferenceSource -> mapper -> ConfigureRepository -> use-case chain; pref_theme is reused unchanged with only its effective default flipping Light -> System. Alongside: the repo's FIRST test suite — kotlin.test wired once into the Library/Feature convention plugins, five pure-logic tests, two preference round-trip tests, and a WCAG contrast sweep over both shipped palettes in presentation-core-styling's own commonTest (the palettes are internal). The sweep's ASSERTION POLICY is not fixed by this plan: it is parameterised by the human gate's answer to proposedSurvey Q1/Q2/Q3, because the mandated thresholds and the mandated 'adopt the tokens unmodified' rule provably cannot both hold on the light palette.

### background

Stage 1 is human-approved (readyForPlan=true) with six binding decisions: build in this repo assembling the existing UI Kit; native decorations + macOS full-size-content window; plain Compose state hoisting in presentation-core-ui + compose-application, no new module, no Orbit MVI (orbit-mvi.md's frontmatter scopes it to presentation-feature-*/**); reuse the multiplatform-settings chain with pref_theme unchanged and pref_side_panel_width added, persisted on drag-END only because java.util.prefs flushes to disk; kotlin.test only, wired once in the convention plugins, plus a WCAG sweep over both palettes; and the shipped sentFixed token Color(0xFF3EAB5E) authoritative over the PRD's hue 152 (amend the doc, change no code). This plan implements those decisions and does not revisit them, with ONE escalated exception: decision 6 / AC-36 ('tokens adopted unmodified') and AC-30 / AC-40 / NFR-11 ('every pairing meets WCAG AA') are mutually unsatisfiable on the LIGHT palette, proven by measured ratios. REFINER PASS: there was no competitive judge at this tier (planDrafts=1; evaluation.json carries zero corrections), so this refinement is the only validation before the human gate. Every path, signature, catalog entry, convention hook and token value the draft asserted was re-opened against the real tree; corrections are listed in AS-7. NOTE: .pipeline/sdd/docs/registry/ does not exist — no components.json/assets.json/styles.json/features.json/bugs.json. Reuse below came from direct source inspection, and knownRisksFromQA is EMPTY because no bug history exists to draw on — not because the area is believed clean.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | preparation/preparation.json (Stage 1, human-approved, readyForPlan=true) | .pipeline/sdd/docs/app-shell-window-layout/preparation/preparation.json |
| code | preparation/clarify.json — 12 FR, 3 NFR, 12 AC, SC-1..SC-5, 7 user-answered survey entries | .pipeline/sdd/docs/app-shell-window-layout/preparation/clarify.json |
| code | preparation/brainstorm.json — 12 components, 6 alternatives, 7 openDecisions, 13 readyMadeSolutions, 6 user-answered survey entries | .pipeline/sdd/docs/app-shell-window-layout/preparation/brainstorm.json |
| figma | 'Rectify Screens' 01 First run, platform & source (LIGHT). Authoritative toolbar order: traffic lights, Android\|iOS segmented switch, '· No device ▾' chip, right-aligned '0 staged', 'Clear', disabled 'Resolve & Send ⌘↵'. Status bar: left '· adb 35.0.1 · idb 1.1.8 · ready' (mono, GREEN dot), right 'Rectify 0.4'. REFINER NOTE: that green dot is sentFixed and appears only because adb/idb DETECTION succeeded — detection is downstream, so this deliverable renders screenshot 02's status-bar state, not this one. URL must not be re-fetched. | raw/design/01-first-run-platform-source.png |
| figma | 'Rectify Screens' 02 Empty, no device yet (LIGHT) — THIS deliverable's shipped state: full-bleed dotted stage texture, dashed 'NO SIGNAL' outline (downstream content), 'Waiting for an Android device' copy (downstream content), status bar left '· Waiting for device…' with a NEUTRAL (ink3) dot, right 'Rectify 0.4', no side panel, no bezel. Verified by the refiner: NO signal colour (red/green/orange/blue) appears anywhere in this frame. | raw/design/02-empty-no-device.png |
| code | operator dictation — scope boundary: specify the shell's slots, regions, tokens and theming; do not implement device discovery | inputs/operator-dictation.md |
| code | presentation-core-styling/.../core/ThemeColor.kt (22-token contract, VERIFIED: no onPop token) + source/attribute/color/{Light,Dark}Color.kt (both internal; DarkColor's KDoc calls dark 'the primary Rectify experience') + source/theme/AppTheme.kt |  |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/ — component library, strict atom -> molecule -> organism layering; signal-token call sites enumerated by grep (A11Y-5) |  |
| code | compose-application/src/desktopMain/.../window/DesktopEntry.kt (VERIFIED: application { Window(onCloseRequest = ::exitApplication, title = "Rectify") { App() } }) + desktop-application/src/main/kotlin/dev/rectify/io/source/entry/Main.kt |  |
| code | VERIFIED theme chain: data-preference-api/.../ThemePreference.kt (DEFAULT_THEME = "system"); data-repository-impl/.../ThemePreferenceMapper.kt (toModel falls back to ThemeModel.Light); domain-core/.../ThemeModel.kt (Light, Dark, MaterialU("materialu") — nothing whose mode == "system"); compose-application/.../App.kt (line 48 already maps MaterialU -> ThemeMode.System; the null branch and collectAsState initial are ThemeMode.Light) |  |
| code | build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/{library,feature}/*ConventionPlugin.kt — VERIFIED actual paths (the draft omitted the second 'convention' segment). Both expose configureCommonDependencies() containing explicitApi() + a sourceSets block — where kotlin.test lands. |  |
| code | CLAUDE.md + .claude/rules/* (no .claude/skills/, no .claude/agents/ in this repo) |  |

### scope

#### in

- Build location: /Users/andrew-malitchuk/StudioProjects/rectify-kmp — both the spec workspace and the build target. Stage 4 writes Kotlin here; Stage 5 runs Gradle here.
- RectifyWindowShell slot contract (organism) — the public API downstream specs build against: Column[ShellToolbar, Row[StageSurface, PanelResizeHandle, SidePanelFrame], ShellStatusBar] with a nullable sidePanel slot (FR-11, FR-14, FR-15, AC-12, AC-31).
- ShellToolbar (organism) with six NAMED slots in canonical order — trafficLightInset (reserved width only), platformSwitch, deviceIndicator, stagedCounter, clearAction, primaryAction — so downstream cannot reorder the canonical toolbar (FR-12, AC-37).
- ShellStatusBar (organism) with leading/trailing slots, stub-filled to screenshot 02's state ('· Waiting for device…' with a neutral ink3 dot; 'Rectify 0.4' right) — NOT screenshot 01's green 'adb … ready' line, which needs tool detection this deliverable does not implement (FR-13, AC-37).
- StageSurface (shell region) — bezel-less, Theme.color.stage ground plus a Canvas-drawn dotted texture, with a content slot for downstream empty-state/mirror content (FR-14, AC-31).
- SidePanelFrame (organism) reusing the shipped SidebarTabs molecule with exactly three tabs Notes/Payload/History, Notes active by default, content as a per-tab slot (FR-11, FR-15, FR-16, AC-12, AC-32).
- PanelResizeHandle (atom) + pure clampPanelWidth: live resize clamped to 260..480dp, persisted on drag-END only and only when changed (FR-20, FR-21, AC-38, AC-39).
- Cmd+1/2/3 tab switching via a pure shortcutToTab(isMeta, key) installed through onPreviewKeyEvent, in addition to click (FR-16, AC-33).
- Window geometry: initial 1240x820 via rememberWindowState, minimum 1024x700 via AWT window.minimumSize inside FrameWindowScope (FR-17, AC-34). Constants live in presentation-core-ui's ShellWindowDefaults — VERIFIED as the only placement that makes AC-34 assertable, since compose-application uses ApplicationConventionPlugin and the kotlin.test wiring lands only in the Library/Feature plugins.
- macOS native decorations + full-size-content window (apple.awt.fullWindowContent / transparentTitleBar / windowTitleVisible on window.rootPane) with a reserved leading inset for the OS traffic lights.
- Theme: ThemeMode.System as the effective first-run default with a persisted manual override, through the shipped AppTheme + pref_theme chain; the only behavioural change is the mapper/initial fallback flipping Light -> System (FR-18, FR-19, AC-35, AC-36, AS-5).
- Adoption of the existing token system UNMODIFIED — every neutral and signal token read behind the Theme singleton, zero Color(0x...) literals and zero hardcoded TextStyles at shell call sites (FR-19, FR-23, AC-36, AC-40).
- One new persisted preference, pref_side_panel_width (Int dp, default 320), through the full chain, registered at the END of each Koin layer group (FR-21, AC-39).
- The repo's first test suite: kotlin.test wired ONCE into LibraryConventionPlugin and FeatureConventionPlugin; pure-function tests (clamp 260/480, Cmd+1/2/3 mapping, three-tab assertion, window constants, drag-end commit-only-if-changed); preference round-trip tests; and the WCAG sweep over BOTH palettes in presentation-core-styling's commonTest (NFR-11, AC-36, AC-40).
- The contrast sweep's ASSERTION POLICY, selected by the human gate from proposedSurvey Q1 (signals), Q2 (ink3) and Q3 (pop/onPop). Step 2 is written against the chosen options; no other step changes. This is in scope as a decision, not deferred as an open question — Stage 5 cannot sign off AC-30/AC-36/AC-40 without it.
- Light and dark @Preview pairs for the no-panel and with-panel shell states, as standing inspection evidence (AC-30, AC-31).
- Documentation amendment: the PRD's green-signal note changes from oklch hue 152 to the shipped 150; no code change.

#### out

- Device discovery, adb/idb detection, device-picker content, emulator boot — android-device-discovery-mirroring. This is also why screenshot 01's green 'ready' status dot is out of scope.
- Notes, Payload and History tab CONTENT — only the three-tab frame and the per-tab content slot are in scope.
- Screenshot 02's 'NO SIGNAL' outline and 'Waiting for an Android device' copy — stage CONTENT filled downstream through StageSurface's slot.
- Annotation and measurement tooling on the stage.
- Resolve & Send / MCP wiring — the toolbar only reserves the slot, rendered disabled.
- Live wiring of toolbar/status bar to real tool-version or device state — static placeholders only (AC-37).
- Authoring or editing any token VALUE, including the 2-degree sentFixed hue question (binding decision 6). If the gate picks survey Q1 option B or Q2 option B this line is explicitly overridden by that decision and step 2 grows a palette edit — which is precisely why it goes to the gate rather than being decided here.
- Windows/Linux builds (NFR-10); iOS/Android shell behaviour — App()/NavigationHost stays untouched there.
- Compose UI-test harness (runComposeUiTest / ui-test-junit4) — binding decision 5.
- macOS packaging (nativeDistributions / packageDmg) — a known gap, not this deliverable.
- Deciding the fate of the existing template features (onboarding/home/settings/about/splash, Navigation3, Room) now that the desktop root bypasses NavigationHost — carried forward as an OPEN question, deliberately NOT decided here and deliberately NOT put to this survey.
- Changing any shipped UI-kit component's internal token choices (CounterBadge's ink3 content, DeviceChip's ink3 chevron, StatusIndicator's ink3 Offline dot) — the shell configures those components, it does not edit them.
- Any write to .pipeline/sdd/docs/registry/ — no registry exists; the orchestrator owns any future registry delta.

### assumptions

- AS-1: The 1240x820 window size and the OKLCH signal values captured as 'unverified design context' upstream are confirmed spec as of the clarify survey.
- AS-2: Device discovery, picker content, annotation and tab content are downstream; this deliverable defines only the slots they render into.
- AS-3: macOS appearance detection is available via isSystemInDarkTheme() on JVM 21/Skiko without native interop. AppTheme already branches on it, but a LIVE appearance switch has never been observed on a shipped desktop build — spiked at the start of step 10.
- AS-4: The four signal colors are identical across light and dark by design (VERIFIED: both palette files carry the same four hex values under '// region Signal — same in both themes'), so theme parity comes from surface/ink pairing per theme, not from varying signal values.
- AS-5: AppTheme's own parameter default is ThemeMode.Dark; the shell always passes the resolved mode explicitly.
- AS-6: Contrast figures here are WCAG 2.x sRGB relative-luminance ratios from the shipped hex values. The surface/bg figures for annotate, sentFixed, warnStillOff, measure, ink, ink2 and ink3 were computed independently by the orchestrator and re-derived by the refiner to the same values. The chrome/stage figures (LIGHT: ink3/chrome 2.89, ink3/stage 2.73, ink2/chrome 5.87, sentFixed/chrome 2.49; DARK: ink3/chrome 3.07) were computed by the refiner only and MUST be re-derived by the step-2 sweep, never hardcoded from this document.
- AS-7: REFINER CORRECTIONS APPLIED, each verified against the tree: (a) convention-plugin paths carry a second 'convention' segment — build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/{library,feature}/; (b) presentation-core-styling AND presentation-core-ui both apply dev.rectify.io.convention.feature, while data-preference-impl and data-repository-impl apply dev.rectify.io.convention.library — so wiring kotlin.test into exactly those two plugins covers all four test modules and nothing else; (c) the status-bar stub is screenshot 02's neutral 'Waiting for device…', not screenshot 01's green 'adb … ready' (the draft's grey-dot-plus-'ready' text was incoherent and would have made the shell's ONLY signal-colour pairing); (d) App.kt already maps MaterialU -> ThemeMode.System at line 48, so step 9's App.kt work is a rename plus the two ThemeMode.Light defaults, not a change to that branch; (e) the draft's two structural corrections to Stage 1's sketch — ShellWindowDefaults in presentation-core-ui, and the one-line ThemePreferenceMapper fallback as the actual delivery of FR-18 — were both re-verified and KEPT.
- The desktop JDK exposes the Apple AWT client properties. If absent, decorations degrade to a standard title bar above the toolbar — functional, ugly, zero extra code.
- commonTest sees its module's own commonMain implementation dependencies and internal declarations (KMP test compilations are associated with the main compilation) — VERIFIED as the mechanism that lets the sweep read the two internal palettes and lets the Dp-based pure functions be tested without a Compose UI harness; Compose's Color type reaches commonTest through the feature plugin's compose.ui dependency.
- Running the app to capture light+dark screenshots is available to Stage 5 on a macOS host; if not, every inspection-only AC falls back to code review plus the @Preview pairs.

### constraints

| type | text |
| --- | --- |
| tech | Built inside /Users/andrew-malitchuk/StudioProjects/rectify-kmp; spec, code and design system share one git repo. |
| tech | Kotlin 2.4.21 / Compose Multiplatform 1.12.1 / JVM 21, macOS host via compose.desktop.currentOs. No AppKit, SwiftUI or Electron. Versions come from gradle/libs.versions.toml — never hardcoded in a module build file. |
| tech | .claude/rules/design-tokens.md: tokens only behind the Theme singleton from @Composable scope; no literal Color(0x...), no raw .dp, no hardcoded TextStyle at a call site; AppTheme the single provider; strict atom -> molecule -> organism import direction. Component-OWN geometry (toolbar height, dot cell size) follows the shipped precedent of private file-level Dp vals inside the component file — not a Theme token, and not inlined at a call site either. |
| tech | .claude/rules/package-layout.md: core/ internal-only, source/ public, never an empty core/. explicitApi() is enforced repo-wide (VERIFIED: both Library and Feature plugins call it inside configureCommonDependencies()) — explicit visibility AND explicit return type on every public declaration. Test source sets are exempt, so the new commonTest files need no visibility ceremony. |
| tech | .claude/rules/preferences.md: an existing key is NEVER renamed. pref_theme stays as-is; the new key is pref_side_panel_width, a const val KEY_* in the impl. |
| tech | .claude/rules/koin-di.md: initKoin()'s list is load-order significant (data -> domain -> presentation); new bindings APPEND to the end of their layer group. One Koin module per Gradle module. |
| tech | .claude/rules/orbit-mvi.md is scoped by frontmatter to presentation-feature-*/** — it does not apply to a layout organism, which is why plain state hoisting is compliant rather than a deviation. |
| tech | .claude/rules/testing.md: ZERO tests exist today; test deps are wired once in the Library/Feature plugins. .claude/rules/version-catalog-hygiene.md: a new catalog entry must be wired in the same change — and it names the existing junit = "4.13.2" pin (VERIFIED present, unconsumed) as dead scaffolding. |
| business | v1 ships macOS only (NFR-10). Android/iOS keep compiling; no shell behaviour is specified for them. |
| business | AC-36 / decision 6 ('tokens unmodified') and AC-30 / AC-40 / NFR-11 ('every pairing meets WCAG AA') are a hard contradiction on the LIGHT palette, proven by measurement. Stage 4 must not write step 2 until the gate answers survey Q1 and Q2; every other step is unblocked and can proceed in parallel. |

### dependencies

| on | note |
| --- | --- |
| presentation-core-styling (in-repo) | Source of ThemeColor (22 tokens), attributeLightColorPalette / attributeDarkColorPalette (both internal), AppTheme(mode, reducedMotion), ThemeMode.Light/Dark/System and the spacing/typography/radius/corner/stroke/motion/size tokens. Consumed as a plain project dependency, so 'adopted unmodified' is satisfied by construction. Already a dependency of presentation-core-ui. Applies dev.rectify.io.convention.feature (VERIFIED). |
| presentation-core-ui (in-repo) | Host module for the new shell organism family. Already depends on presentation-core-styling and compose.material3; already a dependency of compose-application. Applies dev.rectify.io.convention.feature (VERIFIED). No new Gradle wiring. |
| :desktop-application -> initKoin() -> runDesktopApp() | The working macOS entry point (./gradlew :desktop-application:run). This deliverable extends compose-application/desktopMain only; desktop-application/Main.kt is unchanged (initKoin() stays in main() per that module's CLAUDE.md). |
| multiplatform-settings preference chain | data-preference-api/-impl (desktop backend PreferencesSettings on node dev.rectify.io), data-repository-impl mappers + ConfigureRepositoryImpl, domain-repository-api, domain-usecase-api/-impl. Extended with one new key; the theme path is reused. data-preference-impl and data-repository-impl both apply dev.rectify.io.convention.library (VERIFIED). |
| build-logic/convention (composite build) | LibraryConventionPlugin and FeatureConventionPlugin gain the one-time kotlin.test wiring inside their EXISTING configureCommonDependencies(). This is the only build-logic change and it blocks every test in the plan. |
| android-device-discovery-mirroring (downstream) | Consumes ShellToolbar.deviceIndicator / platformSwitch, StageSurface.content and SidePanelFrame.content, and is the spec that first renders signal colours on chrome/surface — so whichever contrast policy the gate picks in Q1 is inherited by it as a standing constraint. |
| Cached design screenshots | raw/design/01-first-run-platform-source.png and 02-empty-no-device.png are the authoritative visual reference. Both LIGHT; no dark comp exists. Must not be re-fetched. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| BLOCKING SPEC CONFLICT (verified by measurement, not estimated). LIGHT palette: sentFixed #3EAB5E measures 2.92:1 on surface #FFFFFF, 2.70:1 on bg #F6F6F5 and (refiner-computed) 2.49:1 on chrome #EDEDEB; warnStillOff #E69825 measures 2.36:1 / 2.19:1. Both sit under the 3:1 non-text threshold NFR-11/AC-40 demand. annotate (3.90 / 3.61) and measure (4.03 / 3.72) pass. DARK passes everywhere: annotate 3.62/4.26, sentFixed 4.83/5.70, warnStillOff 5.97/7.04, measure 3.51/4.13. The tokens cannot change under AC-36 + decision 6, so AC-36 and AC-40 cannot both hold as literally written. | high | high | Escalated to the human gate as proposedSurvey Q1 with four concrete, mutually exclusive policies, each stating exactly what the test asserts and what Stage 5 would see. MATERIAL MITIGATING FACT established by the refiner and not available to the draft: this deliverable renders ZERO signal tokens. Its stub state is screenshot 02 — DeviceChip(None) uses ink3, CounterBadge(Empty) uses seg + ink3, StatusIndicator(Offline) uses ink3 (all three verified by reading the components' colour resolvers) — so the four failing pairs are not painted by any pixel this spec ships. They bite android-device-discovery-mirroring, which is where the policy chosen here must be enforced. |
| ink3 FAILS as text, and unlike the signals it IS rendered by this shell. LIGHT: 3.13:1 on bg (verified), 3.39:1 on surface, 2.89:1 on chrome and 2.73:1 on stage (refiner-computed) — the last two below even 3:1. DARK: 3.39:1 on bg (verified), 3.07:1 on chrome. ink3 carries the status bar's 'Rectify 0.4' and its dot, CounterBadge's '0', DeviceChip's '▾' and None dot, SidebarTabs' counts and AppButton's disabled label. 4.5:1 is out of reach without editing the token. | high | high | Its own gate question (Q2) because it differs from the signals in kind: it is text, it is rendered here, and the worst pairings are chrome/stage rather than surface. The recommended option costs nothing and shrinks the failing surface: the shell's OWN new files use ink2 for any text the shell itself authors (status-bar trailing version, stage placeholder), leaving ink3 usage confined to shipped components that are out of scope to edit, each recorded as a waived baseline entry. WCAG 1.4.3 already exempts AppButton's disabled label (inactive component). |
| ThemeColor has no onPop token (VERIFIED: 22 fields, no onPop), yet pop is deliberately dark in BOTH palettes (#18191C light, #2C2D31 dark). Pairing pop with the theme's ink reports ink-on-pop 1.01:1 in LIGHT — a catastrophic-looking but entirely artificial number. | high | med | Gate question Q3. Note the shell renders NO pop surface (toolbar/status bar are chrome, stage is stage, panel is surface), so this is a sweep-design question, not a rendering defect. Adding onPop would be ADDITIVE — it changes no existing token value, so it does not violate AC-36 the way editing one would — but it buys this deliverable nothing, so the recommendation is to keep CounterBadge's documented Color.White precedent in the sweep and record the gap. |
| isSystemInDarkTheme() may not react to a LIVE macOS appearance change on JVM/Skiko, degrading FR-18's System mode to launch-time detection. | med | med | Spike at the start of step 10, before the override toggle is built. If static, keep System as the launch-time default, document the limitation against FR-18, leave the manual override as the live path. A reactive fallback needs an NSApplication appearance observer — real work, out of scope. |
| macOS traffic lights overlap toolbar content, or the full-size-content client properties behave differently under a non-JBR JDK. | med | low | Reserve the leading inset unconditionally (ShellWindowDefaults.TrafficLightInset = 78.dp) and apply the client properties behind an os.name check; without them the window falls back to a standard title bar above the toolbar. |
| Writing the panel width on every drag frame would thrash java.util.prefs, which flushes to disk. | med | med | Resolved by design: ShellState records the width at drag start and onDragStopped() returns a value only when it actually changed; the window layer persists exactly that. Covered by ShellStateTest. |
| Establishing the repo's first test suite becomes a yak-shave (CI, desktop test runtime, Compose UI test deps) and consumes Stage 5. | med | med | kotlin.test only, wired once in two convention plugins, plus exactly one more catalog entry (multiplatform-settings-test) wired in the same change. No Compose UI harness. Stage 5 runs four named :desktopTest tasks, not ./gradlew check. Step 1 is deliberately proven end to end with a throwaway assertion before anything depends on it. |
| Seven of twelve ACs are verificationMethod=inspection, so Stage 5 cannot fully gate them automatically. | high | med | Convert what converts: AC-36/AC-40 -> contrast sweep; AC-34 -> ShellWindowDefaults constants test; AC-12 -> three-tab assertion; AC-32 -> ShellState default/onSessionStarted test. The residue (AC-30, AC-31, AC-37, the live window behaviour of AC-34) is gated by light+dark @Preview pairs plus running screenshots compared against the two cached designs, marked explicitly as inspection in the traceabilityMatrix. |
| Scope creep: screenshot 02's dashed NO SIGNAL outline and onboarding copy are tempting to build as 'the empty state'. | high | med | StageSurface takes a content slot filled here with one neutral placeholder. The outline, instruction steps and Boot control are downstream. Step 11's definition of done is 'slots exist and hold static stubs'. |
| App()/NavigationHost and RectifyWindow become two competing roots; the template features become dead code on desktop. | med | low | On desktop only, runDesktopApp() renders RectifyWindow instead of App(). App() stays untouched for Android/iOS. The cleanup decision stays an explicit open question for a later spec. |
| Renaming ThemeModel.MaterialU to ThemeModel.System touches a public enum in domain-core. | low | low | VERIFIED by grep: exactly two call sites repo-wide — ThemeModel.kt and App.kt:48, which already maps that entry to ThemeMode.System. The stored string 'materialu' is KEPT so no persisted value is orphaned. ThemePreferenceMapperTest pins all five cases. |
| The effective first-run theme default moves Light -> System on ALL platforms, not just desktop (the mapper fallback and App.kt's initial value are shared). | high | low | Intended per FR-18 and called out in rollout.migrations as release-note-worthy. Installs that explicitly chose a theme are unaffected; installs that never chose one follow the OS from the next launch. |
| The stage's dotted texture has no specified cell size, dot radius or opacity, and no token exists for it. | high | low | Approximate from screenshot 02 as private component geometry in StageSurface.kt (cell 16dp, radius 0.75dp, ink3 at a named alpha constant) and carry Stage 1's open question forward: promote to named tokens only if the values survive design review. |

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
| NFR-11 | accessibility | Every surface/ink token pairing and every signal-color-on-surface pairing, in both light and dark themes, meets WCAG AA contrast: >=4.5:1 for text against its background, >=3:1 for non-text UI components (borders, icons, focus indicators) against adjacent colors. CONFLICT NOTE (refiner): as literally written this is unsatisfiable on the LIGHT palette alongside AC-36; the human gate's answer to survey Q1/Q2 is what NFR-11 actually means for this build. |

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
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring", "SHELL SCOPE NOTE: only the window opening at 1240x820 with a stub-filled toolbar ('· No device'), the bezel-less stage and the stub status bar ('· Waiting for device…') is in scope; detection, picker content and Start mirroring belong to android-device-discovery-mirroring."] |
| SC-2 | Toggle light/dark theme | happy | ["User opens app; theme follows macOS system appearance by default (ThemeMode.System)", "User triggers the manual theme-override toggle in the status bar", "App immediately re-renders toolbar, stage, side panel and status bar using the other palette, with all four signal colors unchanged", "User relaunches the app", "The manually chosen theme persists (pref_theme)"] |
| SC-3 | Switch side-panel tabs via keyboard | happy | ["Mirroring session is active and the side panel is visible on the default Notes tab", "User presses Cmd+2", "Payload tab becomes active", "User presses Cmd+3", "History tab becomes active"] |
| SC-4 | Pre-mirror window has no side panel | edge | ["App launches with no device connected", "Window displays toolbar (stubbed 'No device' state), stubbed status bar and the bezel-less empty-state stage", "Side panel is absent (sidePanel slot is null — neither the panel nor the resize handle is composed)", "Cmd+1/2/3 pressed in this state is a no-op and the event is not consumed", "A mirroring session starts", "Side panel appears, defaulting to the Notes tab"] |
| SC-5 | Resize and persist side-panel width | edge | ["Mirroring session active, side panel visible at its default width (320dp on first run)", "User drags the resize handle narrower", "Panel width stops at the 260dp minimum", "User drags wider past the maximum", "Panel width stops at the 480dp maximum", "On drag release the width is written once to pref_side_panel_width (and only if it changed)", "User relaunches the app", "Panel reopens at the last-set width, not the default"] |

### definitionOfReady

- Stage 1 Preparation is human-approved with readyForPlan=true (satisfied).
- The six binding decisions are recorded and not reopened by this plan (satisfied).
- Both cached design screenshots are present under raw/design/ and have been read by the refiner (satisfied).
- The reuse inventory was established by reading the actual source files; no registry exists (satisfied, recorded in projectContext).
- BLOCKING: the human gate has answered proposedSurvey Q1 (signal contrast policy) and Q2 (ink3 policy). Steps 1 and 3-13 can start without them; step 2 cannot be written, and Stage 5 cannot sign off AC-30/AC-36/AC-40, until they are answered.
- Q3 (pop / onPop) is answered or explicitly deferred with its default (keep the Color.White precedent) recorded.

### definitionOfDone

- ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest is green.
- The contrast sweep implements exactly the policy the gate chose in Q1/Q2/Q3, and every waived pairing carries an inline comment naming the ratio, the AC it is waived against and the survey answer that authorised it.
- ./gradlew :desktop-application:run opens a 1240x820 window that cannot be resized below 1024x700, with the macOS traffic lights inside the Rectify toolbar behind the reserved inset.
- The running window matches cached screenshot 02 in LIGHT: toolbar order (inset, Android|iOS, · No device ▾, …, 0 staged, Clear, disabled Resolve & Send ⌘↵), bezel-less dotted stage, status bar with the neutral '· Waiting for device…' mono line left and 'Rectify 0.4' right, and NO side panel.
- Flipping the status-bar theme override re-renders both palettes immediately and survives relaunch.
- With the side panel forced visible (preview or a temporary debug toggle), the three tabs render Notes/Payload/History with Notes active, Cmd+1/2/3 switch tabs, and the handle resizes between 260 and 480dp with the width surviving relaunch.
- Light and dark @Preview pairs exist for the no-panel and with-panel shell states and render without crashing (every preview wrapped in AppTheme).
- Zero Color(0x...) literals, zero hardcoded TextStyles and zero raw .dp at any shell CALL site; component-own geometry only as private file-level vals (grep the new files to confirm).
- ./gradlew :presentation-core-ui:build and :compose-application:build pass with explicitApi() satisfied.
- No empty core/ directory was created; everything public lives under source/.
- pref_theme's key string is unchanged; pref_side_panel_width is registered at the END of its Koin layer group in every layer.
- DEMO_MODE in App.kt is still false.
- The PRD's green-signal note reads hue 150; unless the gate chose a palette amendment, git diff over presentation-core-styling/src/commonMain is EMPTY.

### successMetrics

- Every pairing in the sweep is either asserted against its WCAG threshold or recorded as an explicitly waived baseline entry with a human decision attached — zero pairings silently unmeasured.
- The shell renders with zero layout changes required when a downstream feature fills the toolbar/stage/panel slots — measured at the start of android-device-discovery-mirroring: that spec adds no parameters to RectifyWindowShell, ShellToolbar, StageSurface or SidePanelFrame.
- Repo test count goes from 0 to the 9 test files named in testStrategy, all runnable from a single Gradle invocation.
- The shell's new Kotlin footprint stays under roughly 900 lines across presentation-core-ui + compose-application — materially larger means something is being authored that should have been assembled.

### architecture

#### approach

Assemble, don't author. A new organism family in presentation-core-ui (source/kit/organism/shell/) plus one atom defines the slot contract; compose-application/desktopMain owns the window, the macOS chrome, the key handling and the persistence wiring; the data/domain preference chain gains exactly one new value. Shell state is plain Compose state hoisting in a @Stable ShellState class, so every acceptance-relevant behaviour (clamp, drag-end commit, tab default, shortcut mapping, window constants) is a pure function or a plain class method, unit-testable without any Compose UI harness. No new Gradle module, no Orbit MVI, no ViewModel, no new third-party dependency except one test-only artifact.

#### rationale

presentation-core-ui is the repo's shared-component module and compose-application is its entry-point module; a new Gradle module would cost a settings entry, a convention-plugin application, a Koin module and an iOS framework baseName for zero benefit, and a presentation-feature-* name would drag in the Orbit convention the shell does not need. Expressing the pre-mirror state as a NULLABLE sidePanel slot rather than a boolean means a downstream feature physically cannot render an empty panel. Named toolbar slots rather than a free-form RowScope mean a downstream feature physically cannot reorder the canonical toolbar. Keeping the window-geometry constants in presentation-core-ui is what makes AC-34's values testable at all: the kotlin.test wiring lands in the Library/Feature plugins and compose-application uses the application plugin (verified). Reusing the multiplatform-settings chain over a direct java.util.prefs call costs ~6 mechanical files but keeps the presentation layer from punching through three architectural layers in the one file every future developer opens first.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| New presentation-feature-shell module with the Orbit four-file shape | The shell holds three values with no async work, no navigation and no one-shot effects; orbit-mvi.md's frontmatter does not even apply. A ViewModel would also make the shell un-previewable and unusable from desktopMain without Koin. Rejected at the brainstorm gate (decision 3). |
| Port presentation-core-styling into a fresh desktop-only project, or consume rectify-kmp via a composite build | Both rejected at the brainstorm gate (decision 1). A port forks ~140 files and structurally breaks AC-36's 'adopted unmodified'; a composite build still drags AGP, the Android SDK and the iOS targets in. |
| undecorated = true with custom-drawn traffic lights | Re-implements hit-testing, drag regions, double-click-to-zoom and fullscreen for a requirement nobody wrote. Decision 2 chose native decorations + full-size-content client properties. |
| Window constants declared in compose-application/desktopMain next to the Window call | VERIFIED: compose-application uses ApplicationConventionPlugin, which does not get the kotlin.test wiring (decision 5 scopes it to Library/Feature). Declaring them there leaves AC-34 with no automatable surface. They live in presentation-core-ui's ShellWindowDefaults. |
| Read/write the panel width directly from java.util.prefs in the window composable (2 lines) | Rejected at the brainstorm gate (decision 4). It punches the presentation layer straight through to storage in the most-copied file in the app. |
| Add a Compose UI test harness (runComposeUiTest) to assert tab clicks and handle drags | Rejected at the brainstorm gate (decision 5). The shell's real acceptance surface (clamp, shortcut mapping, commit-on-change, persistence round-trip, contrast) is all pure logic; rendering fidelity is what the inspection ACs and the light/dark previews are for. |
| Introduce a ThemeStageTexture token group now for the dotted stage ground | No specified values exist for cell size, dot radius or opacity; adding tokens for guessed values is the palette modification AC-36 forbids in spirit. Kept as private component geometry and carried as an open question. |
| Let the sweep assert the literal WCAG thresholds everywhere and ship a knowingly red test | Stage 5 runs the real suite; a plan that schedules a failing test without a human-approved resolution stalls the pipeline there. Hence proposedSurvey Q1/Q2 rather than a brave assertion. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| RectifyWindowShell — presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt (organism) | ui | new |  | THE slot contract. Renders Column(Modifier.fillMaxSize().background(Theme.color.bg))[ toolbar(), Row(Modifier.weight(1f))[ StageSurface(Modifier.weight(1f), content = stage), if (sidePanel != null) { PanelResizeHandle(...); SidePanelFrame(Modifier.width(state.panelWidth), ...) } ], statusBar() ]. sidePanel == null renders neither panel NOR handle (AC-31/FR-15). Drag end: state.onDragStopped()?.let(onPanelWidthCommit) — the shell never persists anything itself. Carries four @Preview functions: no-panel/with-panel x Light/Dark. |
| ShellToolbar — .../organism/shell/ShellToolbar.kt (organism) | ui | new |  | Full-width Theme.color.chrome bar, private ToolbarHeight = 52.dp, bottom Theme.color.line hairline via Theme.stroke.thin. Row order fixed: Spacer(leadingInset), platformSwitch, deviceIndicator, Spacer(weight 1f), stagedCounter, clearAction, primaryAction — named slots, not a free-form RowScope, so the canonical order cannot be changed downstream. |
| ShellStatusBar — .../organism/shell/ShellStatusBar.kt (organism) | ui | new |  | Full-width Theme.color.chrome bar, private StatusBarHeight = 28.dp, top Theme.color.line hairline, leading slot then Spacer(weight 1f) then trailing. Hosts the theme-override control in the trailing slot (filled by the window, not by the organism). REFINER NOTE: any text this organism or its stub authors uses Theme.color.ink2, not ink3, pending survey Q2 — ink3 on chrome measures 2.89:1 in LIGHT. |
| StageSurface — .../organism/shell/StageSurface.kt (shell region; imports only tokens, so it satisfies the layering rule trivially) | ui | new |  | Box filling the remaining space, background Theme.color.stage, NO border/bezel/card (FR-14's 'bezel-less'), with a Canvas drawing a dotted grid behind the content. Private geometry: DotCellSize = 16.dp, DotRadius = 0.75.dp, DOT_ALPHA applied to Theme.color.ink3 — approximated from screenshot 02, flagged for tokenisation. The decorative dot grid is exempt from contrast thresholds (WCAG 1.4.11 excludes purely decorative graphics); the sweep must not measure it. |
| SidePanelFrame — .../organism/shell/SidePanelFrame.kt (organism) | ui | new |  | Column on Theme.color.surface with a leading Theme.color.line hairline; header row (private PanelHeaderHeight = 40.dp, horizontal padding Theme.spacing.spacingS) hosts the REUSED SidebarTabs molecule built from ShellTab.entries, selectedIndex = activeTab.ordinal, onSelect = { onTabSelect(ShellTab.entries[it]) }; below it a divider and content(activeTab). Tab CONTENT is downstream. |
| PanelResizeHandle — presentation-core-ui/.../source/kit/atom/handle/PanelResizeHandle.kt (atom) | ui | new |  | Private geometry HitWidth = 8.dp, LineWidth = 1.dp; a Theme.color.line2 hairline centred in the hit area, Modifier.draggable(orientation = Horizontal) converting pixel deltas to Dp via LocalDensity, plus pointerHoverIcon (E-resize on desktop). Contains NO clamping and NO persistence — both live outside so they are testable. |
| ShellTab + shortcutToTab — .../organism/shell/ShellTab.kt (pure logic) | logic | new |  | public enum class ShellTab(public val label: String) { Notes("Notes"), Payload("Payload"), History("History") } — exactly three entries, canonical order, labels from FR-11 (NOT SidebarTabs' KDoc example label 'Annotations'). public fun shortcutToTab(isMeta: Boolean, key: Key): ShellTab? = if (!isMeta) null else when (key) { Key.One -> Notes; Key.Two -> Payload; Key.Three -> History; else -> null }. Pure, so AC-33 and AC-12 become runnable assertions. |
| ShellState + rememberShellState + clampPanelWidth — .../organism/shell/ShellState.kt (logic) | logic | new |  | clampPanelWidth(width: Dp): Dp = width.coerceIn(PanelMinWidth, PanelMaxWidth) — the whole AC-38 acceptance surface in one function. @Stable class ShellState(initialPanelWidth: Dp): activeTab (private set, default Notes); panelWidth (private set, clamped on construction so a corrupt stored value is never trusted); selectTab; setPanelWidth (clamped, used to restore from preferences); resizePanelBy (clamped; dragging left widens the panel, so the delta is subtracted); onDragStarted (records widthAtDragStart); onDragStopped(): Dp? (returns the new width ONLY if it differs — the 'never write per frame, never write unchanged' rule made testable); onSessionStarted() (resets activeTab to Notes per AC-32, deliberately NOT touching panelWidth, which has a different lifetime per AC-39). rememberShellState(initialPanelWidth = PanelDefaultWidth). |
| ShellWindowDefaults — .../organism/shell/ShellWindowDefaults.kt (logic) | logic | new |  | public object ShellWindowDefaults { InitialSize: DpSize = DpSize(1240.dp, 820.dp); MinimumSize: DpSize = DpSize(1024.dp, 700.dp); PanelMinWidth = 260.dp; PanelMaxWidth = 480.dp; PanelDefaultWidth = 320.dp; TrafficLightInset = 78.dp }. Window/shell geometry, not Theme tokens — same precedent as AppButton's per-size metrics. VERIFIED placement: it lives here so AC-34's values are assertable from a module that has kotlin.test wired. |
| RectifyWindow — compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt | ui | new |  | ApplicationScope-scoped composable owning: rememberWindowState(size = ShellWindowDefaults.InitialSize); inside FrameWindowScope a LaunchedEffect setting window.minimumSize = Dimension(1024, 700) and, when os.name starts with 'Mac', window.rootPane.putClientProperty for apple.awt.fullWindowContent / transparentTitleBar / windowTitleVisible; onPreviewKeyEvent mapping KeyDown + shortcutToTab to state.selectTab, returning true only when the panel is visible and a tab matched (otherwise the event passes through — SC-4); theme from ObserveThemeUseCase; panel-width restore via GetSidePanelWidthUseCase in LaunchedEffect(Unit) and persistence via SetSidePanelWidthUseCase from onPanelWidthCommit inside a rememberCoroutineScope; and AppTheme(mode) { RectifyWindowShell(...) } with every slot stub-filled. |
| runDesktopApp() — compose-application/src/desktopMain/.../window/DesktopEntry.kt (EDIT) | ui | reuse | source:compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt | application { RectifyWindow(onCloseRequest = ::exitApplication) } instead of Window(title = "Rectify") { App() }. :desktop-application/Main.kt is UNCHANGED — initKoin() stays in main(). |
| SidePanelWidthPreference + SidePanelWidthPreferenceSource — data-preference-api/src/commonMain/kotlin/data/preference/api/source/model/SidePanelWidthPreference.kt and .../source/datasource/SidePanelWidthPreferenceSource.kt | data | new |  | public data class SidePanelWidthPreference(val widthDp: Int = DEFAULT_WIDTH_DP) : Resource with companion const DEFAULT_WIDTH_DP = 320; public interface SidePanelWidthPreferenceSource : PreferenceSource<SidePanelWidthPreference>. Mirrors ThemePreference/ThemePreferenceSource exactly. |
| SidePanelWidthPreferenceSourceImpl — data-preference-impl/src/commonMain/kotlin/data/preference/impl/source/datasource/SidePanelWidthPreferenceSourceImpl.kt | data | new |  | internal class taking Settings; private const val KEY_SIDE_PANEL_WIDTH = "pref_side_panel_width"; MutableStateFlow seeded by settings.getIntOrNull(KEY) ?: DEFAULT_WIDTH_DP; setData writes through and updates the flow; getData re-reads. The ThemePreferenceSourceImpl shape, verbatim. |
| SidePanelWidthPreferenceMapper — data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/SidePanelWidthPreferenceMapper.kt | data | new |  | internal object : ModelResourceMapper<Int, SidePanelWidthPreference>. MODEL is a plain Int (mappers.md allows plain types as MODEL, cf. LanguagePreferenceMapper) so no new domain model class is created. |
| ConfigureRepository / ConfigureRepositoryImpl (EDIT) | data | reuse | source:domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/ConfigureRepository.kt + data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/ConfigureRepositoryImpl.kt | Append suspend fun getSidePanelWidth(): Int and suspend fun setSidePanelWidth(widthDp: Int) at the END of the interface, with KDoc; implement in ConfigureRepositoryImpl via the new mapper and a new constructor parameter. No observe* variant — the width is read once at launch and written on drag-end (YAGNI). |
| Get/SetSidePanelWidthUseCase (+ Impl) — domain-usecase-api/.../source/usecase/configuration/ and domain-usecase-impl/.../source/usecase/configuration/ | logic | new |  | GetSidePanelWidthUseCase: suspend operator fun invoke(): Result<Int>. SetSidePanelWidthUseCase: suspend operator fun invoke(widthDp: Int): Optional. Both impls wrap the repository call in resultLauncher(errorMapper = Failure.Technical::Preference), so a BackingStoreException surfaces as Failure.Technical.Preference and never crashes the window. |
| ThemeModel (EDIT) — domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt | data | reuse | source:domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt | Rename the misleading entry MaterialU to System, KEEPING its stored string value "materialu" so no persisted value is orphaned, and fix the KDoc (it says 'Material You colors', which has never been what this entry meant on desktop). VERIFIED by grep: exactly two call sites repo-wide — this file and App.kt:48. |
| ThemePreferenceMapper (EDIT) — data-repository-impl/.../core/mapper/ThemePreferenceMapper.kt | data | reuse | source:data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/ThemePreferenceMapper.kt | Change toModel's fallback from ThemeModel.Light to ThemeModel.System. VERIFIED: this single line is what makes FR-18 true on a fresh install — ThemePreference.DEFAULT_THEME is already "system", which matches no ThemeModel entry, so today the shipped default silently resolves to Light. With the new fallback both the unset default and any unrecognised stored string resolve to System. |
| App.kt (EDIT) — compose-application/src/commonMain/kotlin/dev/rectify/io/source/app/App.kt | ui | reuse | source:compose-application/src/commonMain/kotlin/dev/rectify/io/source/app/App.kt | Rename the ThemeModel.MaterialU branch to ThemeModel.System (it already maps to ThemeMode.System) and change the null branch and collectAsState(initial = ...) from ThemeMode.Light to ThemeMode.System, so Android/iOS and the desktop window agree on the default. DEMO_MODE stays false. |
| LibraryConventionPlugin / FeatureConventionPlugin (EDIT) — build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/{library,feature}/ | logic | reuse | source:build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/library/LibraryConventionPlugin.kt + .../source/convention/feature/FeatureConventionPlugin.kt | CORRECTED PATHS (the draft omitted the second 'convention' segment). Inside each plugin's existing configureCommonDependencies() — the same block that already calls explicitApi() and populates commonMain — add sourceSets.commonTest.dependencies { implementation(libs.findLibrary("kotlin-test").get()) }. One edit per plugin, no new BaseConventionPlugin hook. These two plugins cover exactly the four modules that need tests: presentation-core-styling and presentation-core-ui (feature), data-preference-impl and data-repository-impl (library). |
| SegmentedButtonGroup (molecule) | ui | reuse | source:presentation-core-ui/.../molecule/button/SegmentedButtonGroup.kt | Toolbar platform-switch stub: items listOf("Android", "iOS"), selectedIndex 0, onSelect = {}. Recessed Theme.color.seg track with a raised selected chip; selected label ink, unselected ink2 — exactly screenshot 01/02. |
| DeviceChip + DeviceChipState (molecule) | ui | reuse | source:presentation-core-ui/.../molecule/chip/DeviceChip.kt | Toolbar device-indicator stub: DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {}). VERIFIED: the None state resolves its dot to Theme.color.ink3 and tints the ChevronDown ink3 — no signal colour, matching '· No device ▾' in both comps. |
| CounterBadge + CounterBadgeState (atom) | ui | reuse | source:presentation-core-ui/.../atom/badge/CounterBadge.kt | Toolbar staged-counter stub: CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged"). VERIFIED: Empty resolves container to Theme.color.seg and content to ink3, with the 'staged' label in ink2 — the neutral badge in screenshot 01. (Staged/Sent would resolve to annotate/sentFixed, which this deliverable never renders.) |
| AppButton (atom) | ui | reuse | source:presentation-core-ui/.../atom/button/AppButton.kt | Two toolbar stubs: AppButton("Clear", variant = Ghost) and AppButton("Resolve & Send", shortcut = "⌘↵", enabled = false). VERIFIED: AppButton has a first-class shortcut parameter (its KDoc example is literally Resolve & Send / ⌘↵) and resolves a disabled label to ink3 — WCAG 1.4.3 exempts inactive components, which the sweep must encode rather than waive silently. |
| KeyCap (atom) | ui | reuse | source:presentation-core-ui/.../atom/keycap/KeyCap.kt | Available for the Cmd+1/2/3 affordance in the panel header and as an alternative rendering of the Resolve & Send shortcut. Not composed by default (AppButton.shortcut covers it) — recorded so it is not rebuilt. |
| SidebarTabs + SidebarTabItem (molecule) | ui | reuse | source:presentation-core-ui/.../molecule/bar/tab/SidebarTabs.kt | The side panel's three-tab header, verbatim. Built for this exact panel. VERIFIED colours: selected label ink, unselected ink2, count badge ink3. Labels come from ShellTab, so FR-11's 'Notes' wins over the KDoc's 'Annotations' with zero code conflict. |
| StatusIndicator + StatusIndicatorState (atom) | ui | reuse | source:presentation-core-ui/.../atom/indicator/StatusIndicator.kt | Status-bar leading stub: StatusIndicator(state = Offline, label = "Waiting for device…"). VERIFIED: Offline resolves the dot to Theme.color.ink3 and the label to ink2 — matching screenshot 02. CORRECTION vs the draft: do NOT pair the Offline state with screenshot 01's 'adb 35.0.1 · idb 1.1.8 · ready' text; a grey dot next to 'ready' is incoherent, and the green version of that line requires tool detection that is downstream. |
| StatusLine / StatusStep / StatusPill (molecule/atom) | ui | reuse | source:presentation-core-ui/.../molecule/status/StatusLine.kt + .../atom/pill/StatusPill.kt | Available for the status bar when a live pipeline exists; both resolve to signal colours, so neither is composed in this deliverable's stub. Recorded so the downstream spec does not re-author them — and so the Q1 policy is applied when it does. |
| IconButton + Sun icon (atom) | ui | reuse | source:presentation-core-ui/.../atom/button/IconButton.kt + .../atom/icon/Sun.kt | The status-bar theme-override control (cycles System -> Light -> Dark through SetThemeUseCase). No new component. |
| AppTheme / Theme / ThemeMode / ThemeColor and both palettes | ui | reuse | source:presentation-core-styling/.../source/theme/AppTheme.kt + core/Theme*.kt + source/attribute/color/{Light,Dark}Color.kt | Adopted unmodified (FR-19/AC-36). AppTheme already resolves Light/Dark/System via isSystemInDarkTheme(); the shell passes the resolved mode explicitly. Unless the gate chooses a palette amendment in Q1/Q2/Q3, the ONLY files this deliverable adds to presentation-core-styling are under src/commonTest. |
| StyleguideScreen / DemoHost | ui | reuse | source:presentation-core-ui/.../source/showcase/ + compose-application/src/commonMain/kotlin/dev/rectify/io/core/demo/DemoHost.kt | Existing in-app visual QA surface rendering every token and component in both themes — the fastest way to produce AC-30/AC-36/AC-40 inspection evidence, and the one place a human can actually SEE the failing light-mode signal pairings before answering Q1. DEMO_MODE must be false before committing. |

#### dataModel

| entity | fields |
| --- | --- |
| SidePanelWidthPreference (data-preference-api, Resource) | ["widthDp: Int — default 320, persisted under the settings key pref_side_panel_width (Int)"] |
| ThemePreference (existing, unchanged) | ["theme: String — key pref_theme, DEFAULT_THEME = \"system\" (verified, already); the only change is downstream, in ThemePreferenceMapper's fallback"] |
| ThemeModel (domain-core, edited) | ["Light(\"light\")", "Dark(\"dark\")", "System(\"materialu\") — entry RENAMED from MaterialU, stored string deliberately unchanged so no install loses its value"] |
| ShellState (presentation-core-ui, in-memory only) | ["activeTab: ShellTab — session-scoped, resets to Notes on onSessionStarted() (AC-32)", "panelWidth: Dp — app-scoped, clamped on every mutation, persisted across relaunch (AC-39)", "widthAtDragStart: Dp — private; drives the commit-only-if-changed rule"] |
| ShellTab (presentation-core-ui) | ["Notes(label=\"Notes\")", "Payload(label=\"Payload\")", "History(label=\"History\") — exactly three entries; the test asserts entries.size == 3 and the exact order"] |
| ContrastBaseline (presentation-core-styling/src/commonTest, test-only data) | ["pairing: String — e.g. \"light/sentFixed-on-surface\"", "measured: Double — the ratio recorded at the time of writing", "threshold: Double? — null when the pairing is waived", "waiverNote: String — the AC waived and the survey answer that authorised it. Shape depends on the Q1/Q2 answer; under Q1 option C there is no threshold column at all."] |

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
| WcagContrast (test-only helper, presentation-core-styling/src/commonTest) | internal fun relativeLuminance(color: Color): Double ; internal fun contrastRatio(a: Color, b: Color): Double ; internal fun compositeOver(fg: Color, bg: Color): Color — ~20 lines, no dependency, sRGB linearisation per WCAG (0.04045 / 12.92 / 2.4 gamma), ratio (L1 + 0.05) / (L2 + 0.05) |

#### stateManagement

Plain Compose state hoisting (decision 3). A single @Stable ShellState holds activeTab and panelWidth as mutableStateOf fields with private setters and intent-named methods; rememberShellState creates it. No Orbit container, no ViewModel, no Koin inside presentation-core-ui — which is what keeps the shell previewable and usable from desktopMain. Persisted values enter and leave through the window layer only: RectifyWindow reads pref_theme through ObserveThemeUseCase and pref_side_panel_width once through GetSidePanelWidthUseCase, and writes the width through SetSidePanelWidthUseCase from onPanelWidthCommit inside a rememberCoroutineScope. Session liveness is not owned by the shell at all: it is expressed by whether the caller passes a sidePanel lambda, so the downstream device spec owns that state in its own ViewModel and the shell stays stateless about it.

#### navigation

- No Navigation3 destination is added. On desktop, runDesktopApp() renders RectifyWindow directly instead of App()/NavigationHost — the shell IS the root.
- App()/NavigationHost and the five template feature screens stay untouched and keep working on Android/iOS; on desktop they become unreachable. The cleanup decision is an explicit open question, not taken here.
- In-shell 'navigation' is tab selection only: click via SidebarTabs, keyboard via onPreviewKeyEvent -> shortcutToTab -> ShellState.selectTab.

#### affectedModules

- :presentation-core-ui (feature plugin) — 9 new files (8 under source/kit/organism/shell/, 1 under source/kit/atom/handle/) + 5 new commonTest files. No build.gradle.kts change.
- :presentation-core-styling (feature plugin) — 2 new commonTest files ONLY (plus a palette edit IF and ONLY IF the gate chooses Q1-B or Q2-B). No production file otherwise — this is the mechanical guarantee behind AC-36.
- :compose-application (application plugin — gets NO kotlin.test wiring, by design) — 1 new desktopMain file (RectifyWindow.kt), 2 edits (DesktopEntry.kt, App.kt). No build.gradle.kts change.
- :data-preference-api (library plugin) — 2 new files.
- :data-preference-impl (library plugin) — 1 new file, 1 edit (DataPreferenceImplModule.kt), 1 new commonTest file, 1 build.gradle.kts edit (multiplatform-settings-test on commonTest).
- :data-repository-impl (library plugin) — 1 new mapper, 2 edits (ThemePreferenceMapper.kt fallback, ConfigureRepositoryImpl.kt), 2 new commonTest files.
- :domain-repository-api — 1 edit (two methods appended to ConfigureRepository).
- :domain-core — 1 edit (ThemeModel entry rename, stored value preserved).
- :domain-usecase-api — 2 new files. :domain-usecase-impl — 2 new files, 1 edit (DomainUseCaseImplModule.kt, appended).
- build-logic/convention — 2 edits: source/convention/library/LibraryConventionPlugin.kt and source/convention/feature/FeatureConventionPlugin.kt (CORRECTED PATHS).
- gradle/libs.versions.toml — 2 new [libraries] entries (kotlin-test on version.ref = "kotlin", multiplatform-settings-test on version.ref = "multiplatformSettings"), both wired in the same change.
- :desktop-application, :aos-application, :ios-application, all :presentation-feature-* — UNCHANGED.

### projectContext

#### registryStatus

ABSENT. .pipeline/sdd/docs/registry/ does not exist — no components.json, assets.json, styles.json, features.json or bugs.json. No registry-based reuse lookup was possible and there is NO bug-recurrence history, so the recurrence check is a verified no-op rather than a skipped step. Every reuse entry was established by DIRECT SOURCE INSPECTION (each file opened and its public signature read), so registryRef carries a 'source:<path>' pointer. This plan writes nothing to the registry.

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| SidebarTabs | SidebarTabs + SidebarTabItem (molecule) — the panel's three-tab header; selected ink / unselected ink2 / count ink3 | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/bar/tab/SidebarTabs.kt |
| SegmentedButtonGroup | SegmentedButtonGroup (molecule) — items/selectedIndex/onSelect; the Android\|iOS toolbar switch | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/button/SegmentedButtonGroup.kt |
| DeviceChip | DeviceChip + DeviceChipState (molecule) — None state resolves dot and chevron to ink3 (verified) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/chip/DeviceChip.kt |
| CounterBadge | CounterBadge + CounterBadgeState (atom) — Empty resolves to seg container + ink3 content (verified); Staged/Sent would use annotate/sentFixed | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/badge/CounterBadge.kt |
| AppButton | AppButton (atom) — variants x sizes with a built-in `shortcut` parameter; disabled label resolves to ink3 (verified) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt |
| KeyCap | KeyCap (atom) — keyboard-hint affordance, available, not composed | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/keycap/KeyCap.kt |
| StatusIndicator | StatusIndicator + StatusIndicatorState (atom) — dot + label; Offline resolves dot ink3 / label ink2 (verified) | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/indicator/StatusIndicator.kt |
| StatusLine | StatusLine + StatusStep + StatusStepState (molecule) — signal-coloured pipeline row; reserved for the downstream live status bar | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/status/StatusLine.kt |
| StatusPill | StatusPill + StatusPillTone (atom) — compact pill with a signal dot; downstream | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/pill/StatusPill.kt |
| IconButton | IconButton (atom) + Sun icon — the status-bar theme-override control | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/IconButton.kt, .../atom/icon/Sun.kt |
| ChevronDown | ChevronDown (atom icon) — already composed inside DeviceChip's disclosure affordance | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/ChevronDown.kt |
| signal-consumers | DOWNSTREAM signal-token consumers enumerated by grep so the Q1 policy can be applied where it actually bites: GlyphBadge, StatusLine, Ruler, CounterBadge(Staged/Sent), VerificationRow, AnnotationPopover, AnnotationRow, FeedbackBar, SourceListRow, DeviceChip(Ready/Warn/Lost), MeasureTag, MeasureCard, CommentPopover, KeyValueRow, ErrorGlyph, StatusPill, AnnotationMark | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/ |
| panel-content-rows | HistoryRow / AnnotationRow / KeyValueRow / HierarchyRow / VerificationRow — the row family that will fill the Notes/Payload/History slots; DOWNSTREAM, listed so the panel-content spec does not re-author them | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/ |
| empty-error-states | ErrorState / ErrorCard / ErrorGlyph — existing empty/error presentation available to the stage content slot | source:presentation-core-ui/.../molecule/state/ErrorState.kt, .../organism/error/ErrorCard.kt, .../atom/glyph/ErrorGlyph.kt |
| picker-components | SourceListRow / SelectableCard / CommandBlock — screenshot 01's device-picker body; DOWNSTREAM | source:presentation-core-ui/.../molecule/row/SourceListRow.kt, .../molecule/item/SelectableCard.kt, .../molecule/command/CommandBlock.kt |

#### reuseAssets

| id | name | registryRef |
| --- | --- | --- |
| icon-set | The ImageVector icon set under source/kit/atom/icon/ (ChevronDown, Sun, Smartphone, X, Check, AlertTriangle, …) plus iconCatalogue/resolveIcon — the shell needs no new icon asset | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/ |
| RectifyLogo | RectifyLogo (atom icon) — available if the toolbar ever needs branding; not composed | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/RectifyLogo.kt |
| design-screenshots | Two cached light-theme design screenshots (01 first run, 02 empty) — authoritative visual reference; no dark comp exists | source:.pipeline/sdd/docs/app-shell-window-layout/raw/design/ |

#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| ThemeColor | 22 colour tokens (bg, chrome, stage, surface, pop, ink, ink2, ink3, primary, primaryHover, primaryPress, onPrimary, focus, line, line2, hover, seg, codeBg, annotate, sentFixed, warnStillOff, measure) — adopted unmodified; VERIFIED there is no onPop | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/ThemeColor.kt |
| palettes | attributeLightColorPalette / attributeDarkColorPalette — both internal, which is precisely why the sweep lives in this module's own commonTest. Alpha tokens (line 0x14, line2 0x24, hover 0x0B, seg 0x0F, codeBg 0x09) are rgba overlays in both palettes and must be composited before measurement. | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/source/attribute/color/LightColor.kt, DarkColor.kt |
| AppTheme | AppTheme(mode: ThemeMode = Dark, reducedMotion: Boolean = false, content) — the single provider, already resolving System via isSystemInDarkTheme() | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/source/theme/AppTheme.kt |
| non-colour-tokens | ThemeSpacing/Typography/Radius/Corner/CornerToken/Stroke/Motion/Size — 8dp spacing scale, typography incl. monoMeta/monoData/overline, stroke.thin, cornerToken.chip, motion durations/easings, icon sizes — all consumed, none modified | source:presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/ |

#### existingFeatures

| id | name | registryRef |
| --- | --- | --- |
| desktop-entry | :desktop-application main() -> initKoin() -> runDesktopApp() -> application { Window { App() } } — works today; extended, not replaced | source:desktop-application/src/main/kotlin/dev/rectify/io/source/entry/Main.kt, compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt |
| theme-persistence | pref_theme end-to-end: ThemePreferenceSourceImpl -> ThemePreferenceMapper -> ConfigureRepositoryImpl -> Get/Set/ObserveThemeUseCase -> App.kt. Desktop backend PreferencesSettings(Preferences.userRoot().node("dev.rectify.io")). AC-35's persistence is already built — only the default changes. | source:data-preference-impl/.../ThemePreferenceSourceImpl.kt + data-repository-impl/.../ThemePreferenceMapper.kt |
| koin-graph | initKoin() with the load-order-significant data -> domain -> presentation list; new bindings append to their layer group | source:compose-application/src/commonMain/kotlin/dev/rectify/io/source/di/InitKoin.kt |
| convention-plugins | application/feature/library/di plugins with explicitApi(), jvm("desktop"), compose.desktop.currentOs on the application plugin only — the 8-hook Template Method in source/convention/base/BaseConventionPlugin.kt | source:build-logic/convention/src/main/kotlin/dev/rectify/io/convention/ |
| styleguide | StyleguideScreen + DemoHost (DEMO_MODE in App.kt) — existing visual QA surface for both themes | source:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/showcase/, compose-application/src/commonMain/kotlin/dev/rectify/io/core/demo/DemoHost.kt |
| template-features | onboarding/home/settings/about/splash + Navigation3 + Room + localisation — present, untouched, unreachable on desktop once the shell is the root. Fate NOT decided here. | source:settings.gradle.kts |
| dead-catalog-entries | junit = "4.13.2" and the ktor-*/couchbase/play-services pins — VERIFIED present and unconsumed; version-catalog-hygiene.md names them as dead scaffolding. The new kotlin-test entry sits next to junit; removing junit is left as an open question to keep this diff about the shell. | source:gradle/libs.versions.toml |

#### newToBuild

- presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt
- presentation-core-ui/.../source/kit/organism/shell/ShellToolbar.kt
- presentation-core-ui/.../source/kit/organism/shell/ShellStatusBar.kt
- presentation-core-ui/.../source/kit/organism/shell/StageSurface.kt
- presentation-core-ui/.../source/kit/organism/shell/SidePanelFrame.kt
- presentation-core-ui/.../source/kit/organism/shell/ShellTab.kt
- presentation-core-ui/.../source/kit/organism/shell/ShellState.kt
- presentation-core-ui/.../source/kit/organism/shell/ShellWindowDefaults.kt
- presentation-core-ui/.../source/kit/atom/handle/PanelResizeHandle.kt
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
- EDITS (paths verified): build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/library/LibraryConventionPlugin.kt, .../source/convention/feature/FeatureConventionPlugin.kt, gradle/libs.versions.toml, data-preference-impl/build.gradle.kts, data-preference-impl/src/commonMain/kotlin/data/preference/impl/di/DataPreferenceImplModule.kt, data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/ConfigureRepositoryImpl.kt, data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/ThemePreferenceMapper.kt, domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/ConfigureRepository.kt, domain-core/src/commonMain/kotlin/domain/core/source/model/ThemeModel.kt, domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/di/DomainUseCaseImplModule.kt, compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/DesktopEntry.kt, compose-application/src/commonMain/kotlin/dev/rectify/io/source/app/App.kt, presentation-core-ui/CLAUDE.md (package tree)

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md (root) | Module map, build commands, toolchain pins (Kotlin 2.4.21, JVM 21, Compose MP 1.12.1), and the pointer to .claude/rules/. |
| .claude/rules/design-tokens.md | THE binding constraint on every shell file: tokens only behind Theme from @Composable scope, no Color(0x...)/raw .dp/hardcoded TextStyle at call sites, AppTheme as sole provider, strict atom -> molecule -> organism direction. |
| .claude/rules/package-layout.md | core/ internal-only, source/ public, never an empty core/; explicitApi() means explicit visibility AND return type — shapes every signature in architecture.interfaces. |
| .claude/rules/testing.md | Confirms ZERO tests exist; mandates wiring kotlin.test once in the Library/Feature plugins and names pure deterministic logic as the first high-value target — exactly this plan's test shape. |
| .claude/rules/preferences.md | PreferenceSource<T> shape, the pref_<name> convention, and the prohibition on renaming a key — why pref_theme is untouched and pref_side_panel_width is new. |
| .claude/rules/mappers.md | ModelResourceMapper pairing, mappers never throw and fall back in toModel (the one line that fixes the theme default), and plain types allowed as MODEL — why the width needs no domain model class. |
| .claude/rules/domain-layer.md + .claude/rules/error-handling.md | One interface per use case with one suspend operator invoke returning Result<T>/Optional; every impl body through resultLauncher(errorMapper = Failure.Technical::Preference) so a BackingStoreException cannot crash the window. |
| .claude/rules/koin-di.md | One Koin module per Gradle module, singleOf(::Impl) bind Interface::class, and the load-order-significant data -> domain -> presentation list. |
| .claude/rules/convention-plugins.md + build-logic/convention/CLAUDE.md | The 8-hook Template Method and its fixed order; kotlin.test goes inside the EXISTING configureCommonDependencies() of the library and feature plugins — no new hook. Also the verified task name ./gradlew :build-logic:convention:assemble for the composite build. |
| .claude/rules/platform-coverage.md | 'Desktop' is jvm("desktop") current-OS only; compose.desktop.currentOs lives solely on ApplicationConventionPlugin; :desktop-application is a plain-JVM module with no convention plugin — which is why the kotlin.test wiring cannot reach it and the window constants live in presentation-core-ui. |
| .claude/rules/compose-preview-convention.md | Private @Preview functions named <Composable>Preview, always wrapped in AppTheme { } (a preview outside AppTheme crashes on the first Theme.* access) — the format of the light/dark inspection evidence. |
| .claude/rules/orbit-mvi.md | Its frontmatter (paths: presentation-feature-*/**) is the documented reason plain state hoisting is compliant — cited so the human gate does not re-litigate decision 3. |
| .claude/rules/version-catalog-hygiene.md | junit is a dead pin; a new catalog entry must be wired in the same change — both new entries are wired immediately in steps 1 and 8. |
| .claude/rules/localisation.md + .claude/rules/compose-resources-namespace.md | New strings belong in presentation-core-localisation/src/commonMain/composeResources/values/strings.xml with generated Res accessors — relevant to the deliberate decision NOT to localise the AC-37 stub strings yet. |
| .claude/rules/navigation.md | Checked to confirm the desktop shell can bypass NavigationHost without breaking the Navigation3 contract on other platforms; no Destination is added. |
| presentation-core-ui/CLAUDE.md | Authoritative kit package layout. NOTE: its tree predates the Rectify-specific components (no badge/, chip/, keycap/, handle/, organism/shell/ entries) — it must be updated in the same commit or it goes further out of date. |
| compose-application/CLAUDE.md + desktop-application/CLAUDE.md | core/ vs source/ in the app shell module, the Koin load-order warning, and the rule that initKoin() stays in :desktop-application's main(). |
| data-preference-api/impl, data-repository-impl, domain-usecase-api/impl, domain-core, domain-repository-api CLAUDE.md files | The 'adding a new preference / use case / repository method' checklists these modules already document — the pref_side_panel_width chain follows them literally, which is what makes step 8 mechanical. |

#### skills

| name | why |
| --- | --- |
| (none) | No .claude/skills/ directory exists in this repository (only .claude/rules/ and .claude/settings.local.json). None is claimed. |

#### agents

| name | why |
| --- | --- |
| (none) | No .claude/agents/ directory exists in this repository. None is claimed. |

#### docs

| ref | why |
| --- | --- |
| build-logic/convention/README.md | Plugin comparison table; the kotlin.test addition adds no plugin but the dependency rows are the natural place to record it. |
| presentation-core-ui/CLAUDE.md package tree | Must gain organism/shell/ and atom/handle/ in the same change. |
| WCAG 2.2 SC 1.4.3 (contrast minimum 4.5:1, with its exemptions for incidental and INACTIVE-component text) and SC 1.4.11 (non-text contrast 3:1, with its exemptions for decorative graphics and for information also conveyed by other visual means) | The exact thresholds and exemption clauses every option in survey Q1/Q2 turns on. The disabled Resolve & Send label and the decorative stage dot grid fall under these exemptions regardless of which option is chosen. |
| Upstream PRD green-signal note (hue 152) | Step 13 amends it to 150 to match the shipped token — the only documentation edit required by decision 6. |

### errorStates

| state | behavior |
| --- | --- |
| empty | No mirroring session: RectifyWindow passes sidePanel = null, so neither SidePanelFrame nor PanelResizeHandle is composed (AC-31). Toolbar shows DeviceChip("No device", None), CounterBadge(0, Empty, "staged") and a disabled Resolve & Send; the status bar shows StatusIndicator(Offline, "Waiting for device…") and 'Rectify 0.4'; StageSurface shows its dotted ground with one neutral placeholder. Screenshot 02's richer content (dashed NO SIGNAL outline, instruction steps, Boot control) is downstream. |
| loading | No loading state — the shell has no async work. The one asynchronous read (panel width, in LaunchedEffect(Unit)) resolves during the first frames; the panel opens at PanelDefaultWidth until it arrives and is not visible pre-session anyway, so no flicker is user-visible. |
| error | Preference storage unreadable or read-only (java.util.prefs can throw BackingStoreException on a locked/sandboxed profile): both use-case impls wrap the repository call in resultLauncher(errorMapper = Failure.Technical::Preference); the window falls back to ThemeMode.System and 320dp and still opens. A persistence failure must never block the window. A stored width outside 260..480 is clamped on read by ShellState's constructor. An unrecognised pref_theme string resolves to System via the mapper fallback. Cmd+1/2/3 with no panel visible is a no-op and the event is NOT consumed. |

### observability

#### logs

- kermit is already a dependency of presentation-core-ui; the shell itself adds NO logging — no async work, no I/O, no failure path inside it worth a line.
- RectifyWindow logs one Logger.w when the macOS full-size-content client properties cannot be applied (non-Mac os.name or a JDK that rejects them) — it silently changes the window's appearance and is otherwise invisible in a bug report.
- RectifyWindow logs one Logger.w on a failed panel-width read or write (Failure.Technical.Preference), since the fallback is silent by design.

#### metrics

- None. No metrics pipeline exists in this repository and adding one for a window shell would be unrequested infrastructure.

#### analyticsEvents

- None. No analytics SDK exists; the shell is a local desktop layout with no product-analytics requirement in any FR/NFR.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | WCAG 2.2 AA is the standard held: >=4.5:1 for text (SC 1.4.3) and >=3:1 for non-text UI components and graphical objects (SC 1.4.11). Ratios are computed from sRGB relative luminance per the WCAG formula ((L1+0.05)/(L2+0.05) with the 0.04045/12.92 linearisation) in presentation-core-styling/src/commonTest/.../WcagContrast.kt — no library, ~20 lines. WHICH pairings are hard-asserted against those thresholds and which are recorded as waived baselines is decided by the human gate (survey Q1/Q2/Q3), not by this plan. |
| A11Y-2 | Alpha tokens MUST be composited over their base surface before measurement. line (0x14), line2 (0x24), hover (0x0B), seg (0x0F) and codeBg (0x09) are rgba overlays in both palettes (black in light, white in dark); measuring them raw produces meaningless numbers. The sweep composites each over every surface it is drawn on (bg, chrome, stage, surface) with the standard src-over formula, mirroring Compose's compositeOver at runtime. |
| A11Y-3 | pop MUST be paired with on-pop ink, never with the theme's default ink: pop is deliberately dark in BOTH palettes (#18191C light, #2C2D31 dark), so ink-on-pop measures 1.01:1 in light — an artefact, not a defect. VERIFIED: ThemeColor has no onPop token, and the shell renders no pop surface at all. Resolution is survey Q3. |
| A11Y-4 | MEASURED NEUTRAL LADDER, recorded as data in the test rather than hidden in assertions. LIGHT: ink on bg 16.07 PASS; ink2 on bg 6.24 PASS, ink2 on chrome ~5.87 PASS; ink3 on surface 3.39, on bg 3.13, on chrome ~2.89, on stage ~2.73 — all below 4.5:1 and the last two below 3:1. DARK: ink 14.74, ink2 6.64, ink on pop 12.19 PASS; ink3 on bg 3.39, on chrome ~3.07. ink3 is used as TEXT (status-bar version, counter value, chip detail, tab counts) so a literal reading of AC-36/NFR-11 fails in BOTH palettes. This is survey Q2 — not a silent relaxation. |
| A11Y-5 | SIGNAL MEASUREMENTS, verified: LIGHT annotate 3.90 (surface) / 3.61 (bg) PASS; measure 4.03 / 3.72 PASS; sentFixed 2.92 / 2.70 and ~2.49 on chrome FAIL 3:1; warnStillOff 2.36 / 2.19 FAIL. DARK: annotate 3.62/4.26, sentFixed 4.83/5.70, warnStillOff 5.97/7.04, measure 3.51/4.13 — all PASS. KEY FINDING: this deliverable renders NONE of the four signal tokens (DeviceChip None, CounterBadge Empty and StatusIndicator Offline all resolve to ink3/seg — verified by reading each component's colour resolver, and confirmed against screenshot 02 which contains no signal colour). The failing pairs are therefore theoretical for THIS spec and real for android-device-discovery-mirroring. Survey Q1 chooses the policy both inherit. |
| A11Y-6 | Exemptions the sweep encodes explicitly rather than waiving: AppButton's disabled 'Resolve & Send' label resolves to ink3 but SC 1.4.3 exempts text that is part of an INACTIVE user-interface component; StageSurface's dotted grid is a purely decorative graphic, exempt from SC 1.4.11. Both are listed by name in the test so a future reader sees a reason, not an omission. |
| A11Y-7 | Keyboard operability: Cmd+1/2/3 switch tabs without a pointer (FR-16/AC-33); SidebarTabs items carry Role.Tab and AppButton carries Role.Button, both already shipped; AppButton renders a focus ring from Theme.color.focus in InputMode.Keyboard, so keyboard navigation of the toolbar stubs is already visible. The shell adds no interactive element without a role. |
| A11Y-8 | PanelResizeHandle has an 8dp pointer target around a 1dp hairline and sets an E-resize hover cursor. KNOWN GAP, flagged not hidden: it is pointer-only — no keyboard path to resize (WCAG 2.2 SC 2.1.1). The width is a non-essential preference with a working default and clamped bounds, so this plan accepts the gap and records it as an open question rather than inventing an unrequested keyboard gesture. |
| A11Y-9 | Reduced motion is handled centrally: AppTheme(reducedMotion) resolves Theme.motion and components read Theme.motion.durations.* rather than branching on their own boolean. The shell introduces no animation of its own, so it inherits this for free. |

### performanceBudget

| metric | target |
| --- | --- |
| Panel resize responsiveness | Width follows the pointer within one frame (no animation, no snapping); clampPanelWidth is pure arithmetic on a Dp, and only the panel's width modifier is invalidated, not the stage content. |
| Preference writes during a drag | EXACTLY ZERO writes while dragging, and exactly one write per drag that changes the width (zero if unchanged). Enforced by ShellState.onDragStopped() returning null on no-change, covered by ShellStateTest — java.util.prefs flushes to disk, so a per-frame write would be pathological. |
| Stage texture cost | One Canvas pass over the visible stage area with no per-dot allocation inside the draw loop (precompute the step from LocalDensity); it must not re-draw on panel resize beyond the stage's own size change. |
| Cold window open | The window opens and paints without waiting on preference I/O — the width read happens in a LaunchedEffect after first composition, and the panel is not visible pre-session anyway. |
| Test suite wall time | The four :desktopTest tasks complete in well under a minute on a warm daemon; all tests are pure JVM with no Compose UI harness and no device. |

### i18n

#### locales

- en — the only locale shipped today (presentation-core-localisation/src/commonMain/composeResources/values/strings.xml is the single values file)

#### strings

- ShellTab labels: "Notes", "Payload", "History" — plain English constants on the enum. Real UI, not stubs; they SHOULD move to strings.xml when the tabs gain content.
- AC-37 stub strings, deliberately NOT localised because a downstream spec replaces them: "Android", "iOS", "No device", "staged", "Clear", "Resolve & Send", "⌘↵", "Waiting for device…", "Rectify 0.4".
- Machine output (tool versions, device ids, FPS, frame numbers) is never localised per the style guide's mono rule — verbatim in Theme.typography.monoMeta/monoData.

#### notes

New strings would live in presentation-core-localisation/src/commonMain/composeResources/values/strings.xml and be read through the generated rectify_kmp.presentation_core_localisation.generated.resources.Res accessors. This deliverable adds none: localising AC-37 placeholders would create keys the next spec deletes. If the gate wants the three tab labels localised now, it is three keys and one import — left as an open question, not a survey item, because it does not change the build's shape.

### security

#### dataHandling

- The only persisted data is one integer (panel width in dp) and one existing theme identifier string, both in java.util.prefs.Preferences.userRoot().node("dev.rectify.io"). No PII, no credentials, no device identifiers.
- Nothing is transmitted anywhere — no network call, no network dependency (there is still no network layer in this repo).

#### permissions

- None requested or required. The shell does not touch adb/idb, the filesystem, the camera, screen-recording APIs or any entitlement-gated macOS capability — all of that arrives downstream.

#### privacy

- No analytics, no telemetry, no crash reporting is added.
- The mirrored device screen — the privacy-sensitive surface in this product — is downstream content rendered into StageSurface's slot; this deliverable never acquires, stores or forwards a frame.

### rollout

#### featureFlags

- None. The desktop root switches from App() to RectifyWindow in one commit — a flag would mean maintaining two desktop roots for a change whose blast radius is one function body, and the repo has no flag infrastructure.
- The existing DEMO_MODE const in App.kt is NOT a release flag and must remain false; it is the StyleguideScreen escape hatch, useful as inspection evidence during step 12 only.

#### migrations

- No data migration. pref_theme's key and stored values are untouched; ThemeModel.System keeps the stored string "materialu", so any existing install carrying that value resolves to the same mode under its new, honest name.
- pref_side_panel_width is a new key: absent on every existing install, which is exactly the documented first-run default path (320dp).
- Release-note-worthy behavioural change: the effective first-run theme default moves Light -> System on ALL platforms (the mapper fallback and App.kt's initial value are shared). Installs that explicitly chose a theme are unaffected.

#### backwardCompatibility

Android and iOS keep booting App() -> NavigationHost with every template feature intact; only their default theme changes. Desktop's visible behaviour changes completely and intentionally. presentation-core-ui's public API is additive only (new organisms, one new atom, one new object); no existing component signature changes. ConfigureRepository gains two methods — source-incompatible for any out-of-tree implementer, of which there are none (one implementer in-repo, no published artifact). If the gate chooses Q3 option A, ThemeColor gains a constructor parameter: in-repo impact is the two palette files only, but it is a binary-incompatible change to a public data class, which is why it is a gate decision rather than a silent fix.

### knownRisksFromQA


### implementationPlan

| step | title | dependsOn | linkedAC | estimate | changes | verification |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Wire kotlin.test once into the Library and Feature convention plugins (the repo's first test infrastructure) | [] | [] | S | Add kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" } to gradle/libs.versions.toml [libraries]. In BOTH build-logic/convention/src/main/kotlin/dev/rectify/io/convention/source/convention/library/LibraryConventionPlugin.kt and .../source/convention/feature/FeatureConventionPlugin.kt (CORRECTED PATHS — the draft omitted the second 'convention' segment), extend the EXISTING configureCommonDependencies() with sourceSets.commonTest.dependencies { implementation(libs.findLibrary("kotlin-test").get()) }, inside the same extensions.getByType<KotlinMultiplatformExtension>().apply { } block that already calls explicitApi(). No new BaseConventionPlugin hook. Prove it with a throwaway assertTrue(true) in presentation-core-ui, then delete it. | ./gradlew :build-logic:convention:assemble and ./gradlew :presentation-core-ui:desktopTest both succeed; the throwaway test is gone from the committed tree. |
| 2 | WCAG contrast sweep over both shipped palettes — assertion policy per the gate's survey answers | [1] | ["AC-30", "AC-36", "AC-40"] | M | BLOCKED until proposedSurvey Q1 and Q2 are answered; do not start it before then. New presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/WcagContrast.kt (relativeLuminance, contrastRatio, compositeOver — ~20 lines, no dependency). New .../contrast/PaletteContrastTest.kt iterating attributeLightColorPalette and attributeDarkColorPalette (visible because the test lives in this module) over an EXPLICIT, NAMED pairing table — never a blind cartesian product — with alpha tokens composited per A11Y-2 and the SC 1.4.3/1.4.11 exemptions of A11Y-6 encoded by name. Which pairings hard-fail and which are recorded as waived baseline entries is exactly the Q1/Q2/Q3 answer; every waived entry carries an inline comment naming the ratio, the AC and the survey answer that authorised it. Unless the gate chose a palette amendment, NO production file in this module is touched. | ./gradlew :presentation-core-styling:desktopTest is GREEN (a red suite here stalls Stage 5 — that is the whole reason for the gate); git diff shows only new files under src/commonTest unless the gate authorised a palette edit. |
| 3 | Shell pure logic and state: ShellTab, shortcutToTab, ShellWindowDefaults, clampPanelWidth, ShellState, rememberShellState (+ their five tests) | [1] | ["AC-12", "AC-32", "AC-33", "AC-34", "AC-38"] | M | Create presentation-core-ui/src/commonMain/.../source/kit/organism/shell/{ShellTab.kt, ShellWindowDefaults.kt, ShellState.kt} with the signatures in architecture.interfaces, plus ClampPanelWidthTest, ShortcutToTabTest, ShellTabTest, ShellWindowDefaultsTest and ShellStateTest. Deliberately lands BEFORE any composable, so the whole automatable acceptance surface exists and is green before a pixel is drawn. Independent of step 2 — not blocked by the gate. | ./gradlew :presentation-core-ui:desktopTest — all five files green. |
| 4 | PanelResizeHandle atom and StageSurface | [3] | ["AC-31", "AC-38"] | S | New .../source/kit/atom/handle/PanelResizeHandle.kt (draggable hit area, Theme.color.line2 hairline, E-resize hover cursor, no clamping, no persistence). New .../organism/shell/StageSurface.kt (Theme.color.stage ground, Canvas dotted grid from private geometry, content slot, explicitly NO border/bezel). Both carry a private @Preview wrapped in AppTheme. | Previews render; ./gradlew :presentation-core-ui:build passes explicitApi(). |
| 5 | ShellToolbar and ShellStatusBar organisms, assembled from existing kit components | [4] | ["AC-37"] | M | New .../organism/shell/ShellToolbar.kt (six named slots, chrome fill, bottom hairline, reserved leading inset) and .../organism/shell/ShellStatusBar.kt (leading/trailing slots, chrome fill, top hairline). ZERO new leaf components: previews fill the slots with the shipped SegmentedButtonGroup, DeviceChip, CounterBadge, AppButton and StatusIndicator. Text the shell itself authors uses ink2 pending Q2. If a slot seems to need a new component, stop and re-check the kit. | Light and dark previews match screenshot 02's toolbar and status-bar composition on inspection. |
| 6 | SidePanelFrame reusing SidebarTabs with exactly three tabs | [5] | ["AC-12", "AC-32"] | S | New .../organism/shell/SidePanelFrame.kt built from ShellTab.entries, defaulting to Notes, content as a per-tab slot, optional tabCounts (SidebarTabItem already supports a count). Header geometry derives from SidebarTabs' existing metrics — this is the part with no design comp, so keep it minimal and flag it for design review. | Preview shows Notes · Payload · History with Notes selected; ShellTabTest already pins the count and order. |
| 7 | RectifyWindowShell — compose the slot contract, with four previews | [6] | ["AC-12", "AC-30", "AC-31"] | M | New .../organism/shell/RectifyWindowShell.kt: Column[toolbar, Row[StageSurface(weight 1f), (handle + panel only when sidePanel != null)], statusBar] on Theme.color.bg, wiring PanelResizeHandle -> state.resizePanelBy / onDragStarted / onDragStopped()?.let(onPanelWidthCommit). Four private @Preview functions: no-panel and with-panel, each in Light and Dark — the standing inspection evidence for AC-30/AC-31. Update presentation-core-ui/CLAUDE.md's package tree in the same commit. | All four previews render; the no-panel preview composes neither handle nor panel. |
| 8 | pref_side_panel_width through the full preference chain | [1] | ["AC-39"] | M | data-preference-api: SidePanelWidthPreference (Resource, default 320) + SidePanelWidthPreferenceSource. data-preference-impl: SidePanelWidthPreferenceSourceImpl (KEY_SIDE_PANEL_WIDTH = "pref_side_panel_width"), append singleOf(::SidePanelWidthPreferenceSourceImpl) bind SidePanelWidthPreferenceSource::class to DataPreferenceImplModule, add multiplatform-settings-test (version.ref = "multiplatformSettings") to the catalog AND to this module's commonTest in the same change. data-repository-impl: SidePanelWidthPreferenceMapper + two methods on ConfigureRepositoryImpl + the new constructor parameter. domain-repository-api: two methods appended with KDoc. domain-usecase-api/-impl: Get/SetSidePanelWidthUseCase (+Impls through resultLauncher) and APPEND both bindings to DomainUseCaseImplModule. Tests: SidePanelWidthPreferenceSourceTest and SidePanelWidthPreferenceMapperTest. Independent of steps 2-7; can run in parallel. | ./gradlew :data-preference-impl:desktopTest :data-repository-impl:desktopTest; the app still boots (Koin graph resolves). |
| 9 | Flip the effective theme default from Light to System | [1] | ["AC-35"] | S | domain-core/ThemeModel.kt: rename MaterialU -> System KEEPING mode = "materialu", fix the KDoc. data-repository-impl/ThemePreferenceMapper.kt: toModel fallback Light -> System (the single line that makes the already-shipped DEFAULT_THEME string "system" resolve correctly instead of silently landing on Light). compose-application/App.kt: rename the when-branch (it already maps to ThemeMode.System) and change the null branch and collectAsState(initial = …) from ThemeMode.Light to ThemeMode.System. New ThemePreferenceMapperTest pinning "light"->Light, "dark"->Dark, "materialu"->System, "system"->System, garbage->System, and toResource(System).theme == "materialu". | ./gradlew :data-repository-impl:desktopTest; grep confirms only the two known MaterialU call sites changed; pref_theme's key string is untouched. |
| 10 | RectifyWindow — window geometry, macOS chrome, key handling, theme and width wiring | [7, 8, 9] | ["AC-33", "AC-34", "AC-35", "AC-39"] | L | FIRST spike AS-3: run the app, change the macOS appearance, record whether isSystemInDarkTheme() recomposes; if not, note the limitation against FR-18 before building the toggle. Then new compose-application/src/desktopMain/.../window/RectifyWindow.kt per architecture.components (rememberWindowState(ShellWindowDefaults.InitialSize); window.minimumSize via AWT Dimension inside FrameWindowScope; the three apple.awt rootPane client properties behind an os.name check; onPreviewKeyEvent -> shortcutToTab consumed only when the panel is visible; ObserveThemeUseCase -> ThemeMode; GetSidePanelWidthUseCase in LaunchedEffect(Unit) -> state.setPanelWidth; SetSidePanelWidthUseCase from onPanelWidthCommit in a rememberCoroutineScope). Edit DesktopEntry.kt to render RectifyWindow. :desktop-application/Main.kt stays untouched. | ./gradlew :desktop-application:run — window opens at 1240x820, refuses to go below 1024x700 (check on a 2x Retina display), traffic lights sit inside the toolbar behind the inset. |
| 11 | Fill every slot with static stubs and add the status-bar theme-override control | [10] | ["AC-31", "AC-35", "AC-37"] | S | In RectifyWindow: platformSwitch = SegmentedButtonGroup(Android\|iOS, selected 0, onSelect = {}); deviceIndicator = DeviceChip("No device", None, onClick = {}); stagedCounter = CounterBadge(0, Empty, "staged"); clearAction = AppButton("Clear", Ghost); primaryAction = AppButton("Resolve & Send", shortcut = "⌘↵", enabled = false); status bar leading = StatusIndicator(Offline, "Waiting for device…") per screenshot 02 — NOT screenshot 01's green 'adb … ready' line, which needs tool detection this spec does not implement — trailing = the app-version text plus IconButton(Sun) cycling System -> Light -> Dark through SetThemeUseCase. Stage content = one neutral placeholder; sidePanel = null. Nothing reads adb. | The running window matches screenshot 02's toolbar and status bar; the theme control switches palettes instantly and survives relaunch. |
| 12 | Inspection evidence pass | [11] | ["AC-12", "AC-30", "AC-31", "AC-34", "AC-36", "AC-37", "AC-40"] | S | Run the app in both themes; capture the pre-mirror window (light and dark) and the with-panel state (from the previews, since no session exists yet); compare against the two cached designs; record evidence paths in the Stage 5 verification artifact. Optionally flip DEMO_MODE true LOCALLY to capture StyleguideScreen in both themes as token evidence — and flip it back before committing. Also confirm mono (JetBrains Mono) and SF Pro actually resolve on desktop rather than silently falling back. | Evidence captured for every inspection-only AC; DEMO_MODE is false in the committed tree. |
| 13 | Amend the PRD's green-signal note from oklch hue 152 to the shipped 150 | [] | ["AC-36", "AC-40"] | XS | Documentation only, per decision 6. No code, and specifically no edit to LightColor.kt or DarkColor.kt — chasing a sub-JND 2-degree hue shift would itself violate AC-36. (Independent of the Q1 decision: even a gate-authorised palette amendment would target contrast, not hue 152.) | The PRD and the shipped token agree; git diff touches no .kt file. |

### testStrategy

#### unit

- presentation-core-ui/src/commonTest/kotlin/presentation/core/ui/kit/shell/ClampPanelWidthTest.kt — clampPanelWidth(200.dp) == 260.dp; (600.dp) == 480.dp; (320.dp) == 320.dp; the exact bounds are returned unchanged. [AC-38]
- presentation-core-ui/src/commonTest/.../ShortcutToTabTest.kt — shortcutToTab(true, Key.One/Two/Three) == Notes/Payload/History; (false, Key.One) == null; (true, Key.Four) == null; (true, Key.A) == null. [AC-33]
- presentation-core-ui/src/commonTest/.../ShellTabTest.kt — entries.size == 3; order exactly Notes, Payload, History; labels exactly "Notes", "Payload", "History" (not SidebarTabs' KDoc 'Annotations'). The automatable half of AC-12. [AC-12]
- presentation-core-ui/src/commonTest/.../ShellWindowDefaultsTest.kt — InitialSize == DpSize(1240.dp, 820.dp); MinimumSize == DpSize(1024.dp, 700.dp); PanelMin/Max/Default == 260/480/320.dp. Converts AC-34's and FR-20/21's numbers into a pass-or-fail check. [AC-34]
- presentation-core-ui/src/commonTest/.../ShellStateTest.kt — activeTab defaults to Notes; selectTab changes it; onSessionStarted() resets activeTab but does NOT change panelWidth (the two lifetimes of AC-32 vs AC-39); a constructor value outside the bounds is clamped; resizePanelBy clamps at both ends; onDragStopped() returns the new width when changed and null when not. [AC-32, AC-38, AC-39]
- presentation-core-styling/src/commonTest/kotlin/presentation/core/styling/contrast/PaletteContrastTest.kt — the sweep over BOTH palettes described in A11Y-1..A11Y-6, with WcagContrast.kt as its helper. Its assertion policy is the gate's Q1/Q2/Q3 answer; its pairing table is explicit and named. [AC-30, AC-36, AC-40, NFR-9, NFR-11]
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/ThemePreferenceMapperTest.kt — "light"->Light, "dark"->Dark, "materialu"->System, "system"->System (the shipped DEFAULT_THEME, today silently resolving to Light), unrecognised->System, and toResource(System).theme == "materialu". [AC-35]
- data-repository-impl/src/commonTest/.../SidePanelWidthPreferenceMapperTest.kt — toModel/toResource round-trip for 260, 320 and 480. [AC-39]

#### integration

- data-preference-impl/src/commonTest/kotlin/data/preference/impl/SidePanelWidthPreferenceSourceTest.kt — against com.russhwolf:multiplatform-settings-test's MapSettings: unset reads 320; setData(480) then getData() returns 480; observeData() emits the new value; a FRESH SidePanelWidthPreferenceSourceImpl over the same backing settings reads 480 — the relaunch half of AC-39, the only honest way to test 'survives restart' without restarting a window. [AC-39]
- NOT automated, by deliberate scope decision (decision 5): the Compose-side integration — pointer drag on PanelResizeHandle, click on SidebarTabs, the real onPreviewKeyEvent dispatch, AWT minimumSize enforcement. Verified by inspection in steps 10-12. Everything downstream of them (clamp, mapping, commit rule, constants) IS automated above, so the untested surface is the Compose/AWT plumbing itself, not Rectify's logic.

#### ui

- No Compose UI-test harness (no runComposeUiTest, no ui-test-junit4) — decision 5.
- Standing visual evidence instead: four private @Preview functions on RectifyWindowShell (no-panel/with-panel x Light/Dark) plus the existing per-component previews, each wrapped in AppTheme.
- Manual inspection script for step 12: (1) ./gradlew :desktop-application:run; (2) confirm 1240x820 and that dragging the window edge stops at 1024x700; (3) confirm the traffic lights sit inside the toolbar behind the inset; (4) compare the pre-mirror window against raw/design/02-empty-no-device.png (bezel-less dotted stage, no panel, '· No device', '0 staged', disabled Resolve & Send ⌘↵, neutral '· Waiting for device…' left, 'Rectify 0.4' right); (5) cycle the status-bar theme control through System/Light/Dark and relaunch to confirm persistence; (6) capture both themes as evidence.

#### linkedScenarios

- SC-1
- SC-2
- SC-3
- SC-4
- SC-5

#### coverageGoal

Not a line-coverage target — this repo has no coverage tooling and adding some would be unrequested infrastructure. The goal is acceptance coverage: every AC with verificationMethod=test (AC-33, AC-35, AC-38, AC-39) is covered by at least one named test file, and four of the eight inspection-only ACs (AC-12, AC-34, AC-36, AC-40) are additionally backed by an automated assertion. The four that remain inspection-only (AC-30, AC-31, AC-32's rendering, AC-37) are each pinned to a named preview or screenshot comparison in the traceabilityMatrix. No AC is unmapped and no FR is unmapped.

#### command

./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest

#### commandNotes

VERIFIED for this repo: all four modules apply a convention plugin that registers jvm("desktop") (presentation-core-styling and presentation-core-ui via dev.rectify.io.convention.feature; data-preference-impl and data-repository-impl via dev.rectify.io.convention.library), so the KMP desktop test task is `desktopTest` in each. Those are exactly the two plugins that gain the kotlin.test wiring in step 1 — no module is left with tests it cannot compile, and :compose-application (application plugin) deliberately gets none, which is why ShellWindowDefaults lives in presentation-core-ui. Stage 5 runs exactly this command, NOT ./gradlew check (check would also build every module's Android and iOS targets for zero benefit here). Build sanity separately with ./gradlew :presentation-core-ui:build :compose-application:build; convention-plugin sanity with ./gradlew :build-logic:convention:assemble (the composite build is included via includeBuild("build-logic") in settings.gradle.kts); run the app with ./gradlew :desktop-application:run. Both new catalog entries (kotlin-test, multiplatform-settings-test) are wired in the same change that adds them, per version-catalog-hygiene.md.

### traceabilityMatrix

| ac | functional | components | tests | steps | verification |
| --- | --- | --- | --- | --- | --- |
| AC-12 | ["FR-11", "FR-15"] | ["ShellTab", "SidePanelFrame", "SidebarTabs (reuse)", "StageSurface", "RectifyWindowShell"] | ["ShellTabTest (entries.size == 3, order, labels)", "SC-3", "step 6 + step 7 previews (inspection: three tabs over a bezel-less stage)"] | [3, 6, 7, 12] | test + inspection |
| AC-30 | ["FR-18", "FR-19", "FR-23"] | ["AppTheme (reuse)", "both palettes (reuse)", "RectifyWindowShell previews", "theme-override control (IconButton + Sun, reuse)"] | ["PaletteContrastTest (policy per survey Q1/Q2)", "SC-2", "step 12 light+dark screenshots"] | [2, 7, 11, 12] | inspection, partially automated by the sweep; the meaning of 'no unreadable contrast regions' is fixed by the gate's Q1/Q2 answer |
| AC-31 | ["FR-12", "FR-14", "FR-15"] | ["RectifyWindowShell (nullable sidePanel slot)", "ShellToolbar", "ShellStatusBar", "StageSurface", "DeviceChip (reuse, None state)", "RectifyWindow"] | ["SC-1 (shell-scoped steps only)", "SC-4", "RectifyWindowShell no-panel preview (Light+Dark)", "step 12 comparison against raw/design/02-empty-no-device.png"] | [4, 5, 7, 11, 12] | inspection — the nullability of the slot is the structural guarantee; no automated render assertion exists by design |
| AC-32 | ["FR-15", "FR-16"] | ["ShellState.onSessionStarted()", "ShellTab.Notes", "SidePanelFrame"] | ["ShellStateTest (default is Notes; onSessionStarted resets activeTab and leaves panelWidth alone)", "SC-4", "step 6 preview"] | [3, 6] | test (state) + inspection (render) |
| AC-33 | ["FR-16"] | ["shortcutToTab", "ShellState.selectTab", "RectifyWindow onPreviewKeyEvent", "SidebarTabs (reuse, click path)"] | ["ShortcutToTabTest (Cmd+1/2/3 mapping, no-Cmd and unknown-key null cases)", "SC-3"] | [3, 10] | test — the mapping is automated; the onPreviewKeyEvent dispatch itself is inspection |
| AC-34 | ["FR-17"] | ["ShellWindowDefaults", "RectifyWindow (rememberWindowState + AWT window.minimumSize)"] | ["ShellWindowDefaultsTest (1240x820, 1024x700)", "SC-1", "step 10/12 manual resize check incl. a 2x Retina display"] | [3, 10, 12] | test (constants) + inspection (live AWT enforcement) |
| AC-35 | ["FR-18", "FR-19"] | ["ThemeModel (edit)", "ThemePreferenceMapper (edit)", "App.kt (edit)", "ObserveThemeUseCase/SetThemeUseCase (reuse)", "AppTheme (reuse)", "status-bar theme-override control"] | ["ThemePreferenceMapperTest (default/unknown -> System; 'materialu' round-trip)", "SC-2", "step 11 manual toggle + relaunch"] | [9, 10, 11] | test (persistence/mapping) + inspection (immediate re-render) |
| AC-36 | ["FR-19", "FR-23"] | ["ThemeColor + both palettes (reuse, unmodified)", "AppTheme (reuse)", "every shell component (token-only call sites)"] | ["PaletteContrastTest (every neutral pairing, alpha tokens composited, pop handled per Q3)", "git diff over presentation-core-styling/src/commonMain must be EMPTY unless the gate authorised a palette edit", "grep for Color(0x / raw .dp / TextStyle( in the new shell files must return nothing at a call site"] | [2, 4, 5, 6, 7, 12] | inspection + automated sweep. CONFLICT: this AC's 'adopted unmodified' clause and its own WCAG clause cannot both hold on the light palette — survey Q1/Q2 decides which clause yields, and Stage 5 must read that answer before signing off. |
| AC-37 | ["FR-12", "FR-13"] | ["ShellToolbar (six named slots)", "ShellStatusBar", "SegmentedButtonGroup/DeviceChip/CounterBadge/AppButton/StatusIndicator (reuse, stub-configured)"] | ["step 11 slot fill + step 12 comparison against raw/design/02-empty-no-device.png", "code review: no adb/idb/device import anywhere in the shell or RectifyWindow"] | [5, 11, 12] | inspection |
| AC-38 | ["FR-20"] | ["clampPanelWidth", "ShellState.resizePanelBy", "PanelResizeHandle", "RectifyWindowShell"] | ["ClampPanelWidthTest (260/480 bounds)", "ShellStateTest (clamping through resizePanelBy)", "SC-5"] | [3, 4, 7] | test — the drag gesture itself is inspection, everything it feeds is automated |
| AC-39 | ["FR-15", "FR-21"] | ["SidePanelWidthPreference(+Source/+Impl)", "SidePanelWidthPreferenceMapper", "ConfigureRepository (edit)", "Get/SetSidePanelWidthUseCase", "ShellState.onDragStopped", "RectifyWindow"] | ["SidePanelWidthPreferenceSourceTest (round-trip incl. a fresh instance over the same store = the relaunch proxy)", "SidePanelWidthPreferenceMapperTest", "ShellStateTest (commit only on change)", "SC-5"] | [3, 8, 10] | test |
| AC-40 | ["FR-19", "FR-23"] | ["both palettes' signal tokens (reuse, identical across themes)", "PaletteContrastTest signal tier", "DeviceChip/StatusIndicator/CounterBadge (reuse — all resolve to ink3/seg in the states this shell renders, so NO signal token is painted by this deliverable)"] | ["PaletteContrastTest signal tier per the gate's Q1 answer", "step 12 light+dark inspection"] | [2, 12] | inspection + automated sweep. Four light-mode signal pairs fail the literal 3:1 threshold and none of them is rendered by this spec — escalated as survey Q1, inherited by android-device-discovery-mirroring. |
| NFR-9 / NFR-11 (carried, no AC id of their own) | ["FR-19", "FR-23"] | ["both palettes", "WcagContrast.kt", "PaletteContrastTest", "ShellStatusBar (ink2 rather than ink3 for shell-authored text, pending Q2)"] | ["PaletteContrastTest (both palettes, every tier)", "A11Y-4/A11Y-5 measurement tables recorded as test data"] | [2, 5] | test, with the threshold policy set by the human gate |
| NFR-10 (carried) | [] | ["compose.desktop.currentOs (ApplicationConventionPlugin, reuse)", "RectifyWindow os.name guard"] | ["Satisfied by construction: only :desktop-application produces a runnable artifact and it builds for the current OS only; the macOS client properties sit behind an os.name check so a non-Mac host degrades rather than breaks."] | [10] | inspection |

### openQuestions

- CARRIED FROM STAGE 1 (1/7): No dark-theme design reference exists for the shell; both cached screenshots are LIGHT. The dark palette is adopted and validated by the sweep plus token correctness, not against a visual comp — flag for design review once the shell runs.
- CARRIED FROM STAGE 1 (2/7): The side panel's header height, padding, tab-row placement and divider treatment are designed here from FR-11 plus SidebarTabs' existing geometry, not copied from a screenshot — neither comp shows the panel. Flag for design review.
- CARRIED FROM STAGE 1 (3/7): The bezel-less stage's dotted texture has no specified cell size, dot radius or opacity token. Approximated as private component geometry in StageSurface.kt; promote to a named token group only if the values survive design review.
- CARRIED FROM STAGE 1 (4/7): AS-3 — isSystemInDarkTheme() on JVM/Skiko has never been observed reacting to a LIVE macOS appearance switch. Spiked at the start of step 10; a fallback would need an NSApplication appearance observer, out of scope.
- CARRIED FROM STAGE 1 (5/7) — DELIBERATELY LEFT OPEN, NOT PUT TO THIS SURVEY: no decision exists on what happens to this repo's template features (onboarding/home/settings/about/splash, Navigation3, Room, localisation) once the shell becomes the desktop root. The shell bypasses NavigationHost on desktop only, leaving those paths live on Android/iOS and dead on desktop. Needs its own cleanup spec.
- CARRIED FROM STAGE 1 (6/7): No macOS packaging (nativeDistributions / packageDmg) is configured in :desktop-application — out of scope here, but the first thing missing before a shippable macOS build exists.
- CARRIED FROM STAGE 1 (7/7): JetBrains Mono / SF Pro availability in the desktop build is unverified. If the desktop silently falls back, every monoMeta/monoData string in the toolbar and status bar renders in the wrong face — checked during step 12.
- ESCALATED TO THE HUMAN GATE AS proposedSurvey Q1 (signal contrast policy), Q2 (ink3 as text) and Q3 (pop / onPop). These are no longer open questions: they are the gate's decisions, and step 2 cannot be written until Q1 and Q2 are answered.
- PanelResizeHandle is pointer-only — no keyboard path to resize the panel (WCAG 2.2 SC 2.1.1). This plan accepts the gap because the width is a non-essential preference with a working default and clamped bounds. Confirm, or schedule a keyboard affordance in a later spec.
- ShellTab's labels are plain English constants, not localised strings. Localise now (three keys), or when the tabs gain content? This plan defers.
- The toolbar's reserved traffic-light inset is a single 78dp constant; the real inset depends on the macOS title-bar height and fullscreen state. Confirm after step 10 whether the fixed value suffices or it must be measured from the rootPane.
- ConfigureRepository gains getSidePanelWidth/setSidePanelWidth but NO observeSidePanelWidth (YAGNI). Confirm nothing downstream needs to react to width changes across windows; adding it later is three small files.
- AC-34 says the window 'cannot be resized below 1024x700', but window.minimumSize is an AWT Dimension in AWT units. Confirm during step 10 that the behaviour is correct on a 2x Retina display and is not silently halved or doubled.
- The dead junit = "4.13.2" pin now sits next to a real kotlin-test entry. Remove it in this change, or leave it for a separate tidy? This plan leaves it, to keep the diff about the shell.
- presentation-core-ui/CLAUDE.md's package tree predates the Rectify-specific component families (no badge/, chip/, keycap/, pill/, tag/, annotation/ entries). Step 7 adds organism/shell/ and atom/handle/ to it; whether to refresh the whole stale tree in the same commit is left to Stage 4's judgement.

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Q1 — BLOCKING. The mandated contrast sweep cannot pass as specified: AC-36 / binding decision 6 require the tokens be adopted UNMODIFIED, while AC-30 / AC-40 / NFR-11 require every signal-on-surface pairing to meet WCAG AA. On the LIGHT palette, sentFixed #3EAB5E measures 2.92:1 on surface and 2.70:1 on bg (and ~2.49:1 on chrome), and warnStillOff #E69825 measures 2.36:1 / 2.19:1 — all below the 3:1 non-text threshold. DARK passes everywhere. Material fact established during refinement: THIS deliverable renders none of the four signal tokens — its stub states (DeviceChip None, CounterBadge Empty, StatusIndicator Offline) all resolve to ink3/seg, and screenshot 02 contains no signal colour. The failing pairs first get painted by android-device-discovery-mirroring. Which assertion policy should PaletteContrastTest implement? | ["A — Rendered-scope WCAG + frozen baseline for the rest (RECOMMENDED). The test hard-fails on exactly the pairings this shell paints (ink and ink2 on bg/chrome/stage/surface at 4.5:1; focus and composited line/line2 at 3:1; onPrimary on primary at 4.5:1) and, for every other pairing in both palettes (all four signals x four surfaces, ink3 everywhere, pop), asserts only that the measured ratio never drops below a checked-in recorded value. Each sub-threshold entry carries an inline `// WAIVED:` comment naming the ratio, the AC waived and this survey answer. Satisfies AC-36 intact and NFR-11 for everything actually rendered; waives the literal AC-40 for four light-mode pairs this spec never paints; binds the downstream spec to re-open the question when it first paints them. Stage 5 sees a green suite plus an explicit waiver list.", "B — Strict WCAG, amend the tokens. Reverses binding decision 6 and AC-36's 'unmodified' clause: the test asserts full thresholds for every pairing, and LightColor.kt changes (sentFixed and warnStillOff darkened to reach 3:1 on white, ink3 darkened to reach 4.5:1). Buys literal AC-30/36/40/NFR-11 compliance and a palette that is correct by construction for every future spec. Costs: the light appearance of every shipped component changes, the kit stops matching the published style guide and both cached screenshots, and the whole component library needs a visual re-review — a far larger change than this shell.", "C — Baseline only, no absolute thresholds anywhere. The test asserts nothing but 'no pairing got worse than its recorded value'. Cheapest to write and always green; converts AC-30/36/40 wholly into a regression guard and gives up the ability to catch a newly introduced bad pairing. Honest, but it stops being an accessibility test.", "D — Dark held to AA, light held to no-regression. Dark (the primary Rectify experience per DarkColor.kt's own KDoc) hard-fails on every pairing at full WCAG AA; light is baseline-only. Matches where the product's quality bar actually is; costs: light mode is never gated, so a light regression that stays above baseline ships silently."] | A — Rendered-scope WCAG + frozen baseline for the rest (RECOMMENDED). The test hard-fails on exactly the pairings this shell paints (ink and ink2 on bg/chrome/stage/surface at 4.5:1; focus and composited line/line2 at 3:1; onPrimary on primary at 4.5:1) and, for every other pairing in both palettes (all four signals x four surfaces, ink3 everywhere, pop), asserts only that the measured ratio never drops below a checked-in recorded value. Each sub-threshold entry carries an inline `// WAIVED:` comment naming the ratio, the AC waived and this survey answer. Satisfies AC-36 intact and NFR-11 for everything actually rendered; waives the literal AC-40 for four light-mode pairs this spec never paints; binds the downstream spec to re-open the question when it first paints them. Stage 5 sees a green suite plus an explicit waiver list. | A is the only option that keeps a real accessibility assertion where it can bite, keeps AC-36 untouched, and does not schedule a knowingly red test into Stage 5. It is also the only one supported by a verified fact rather than an argument: the shell paints no signal token, so holding this spec to signal thresholds tests pixels that do not exist. B is the technically ideal fix but reverses a decision the user already made with full information and balloons the blast radius from one shell to the whole kit — offer it, do not impose it. C gives up too much. D is defensible but leaves the only theme with cached design comps ungated. |
| Q2 — BLOCKING. ink3 is a separate problem from the signals: it IS rendered by this shell, and it is TEXT. LIGHT: 3.39:1 on surface, 3.13:1 on bg, ~2.89:1 on chrome, ~2.73:1 on stage — the last two fail even the 3:1 non-text bar, and none reaches the 4.5:1 text bar. DARK: 3.39:1 on bg, ~3.07:1 on chrome. ink3 carries the status bar's version text and its dot, CounterBadge's '0', DeviceChip's chevron, SidebarTabs' counts and AppButton's disabled label (the last is exempt under WCAG 1.4.3 as an inactive component). ThemeColor documents ink3 as 'tertiary ink — metadata, placeholders'. How should the sweep and the shell treat it? | ["A — Classify ink3 as incidental/meta ink at 3:1, AND have the shell's own new files use ink2 for any text the shell itself authors (RECOMMENDED). The sweep asserts ink3 >= 3:1 where it passes, records the chrome and stage pairings as waived baseline entries, and encodes the inactive-component exemption by name. The shell's status-bar version text and stage placeholder use Theme.color.ink2 (5.87:1 on chrome), so no text this deliverable authors is unreadable; remaining ink3 text lives inside shipped components that are out of scope to edit. Costs nothing, changes no token, and shrinks the failing surface to third-party call sites. Visual cost: those two strings render slightly darker than screenshot 02's lightest grey.", "B — Hold ink3 to the 4.5:1 text bar and darken the token. Light ink3 moves roughly #8B8B91 -> #727278 and dark ink3 moves #707077 -> lighter. Violates AC-36 the same way Q1-B does, and ink3 appears across the whole kit, so every screen's metadata layer changes tone. Buys literal NFR-11 compliance for all metadata text.", "C — Accept ink3 as-is everywhere, waived, with no shell-level re-slotting. The shell uses ink3 exactly where screenshot 02 does, the sweep records every ink3 pairing as a baseline waiver, and the kit's metadata contrast is formally accepted as sub-AA in both palettes. Maximum design fidelity, zero code change, largest accessibility debt — and it must be written down somewhere a future audit will find it."] | A — Classify ink3 as incidental/meta ink at 3:1, AND have the shell's own new files use ink2 for any text the shell itself authors (RECOMMENDED). The sweep asserts ink3 >= 3:1 where it passes, records the chrome and stage pairings as waived baseline entries, and encodes the inactive-component exemption by name. The shell's status-bar version text and stage placeholder use Theme.color.ink2 (5.87:1 on chrome), so no text this deliverable authors is unreadable; remaining ink3 text lives inside shipped components that are out of scope to edit. Costs nothing, changes no token, and shrinks the failing surface to third-party call sites. Visual cost: those two strings render slightly darker than screenshot 02's lightest grey. | A is the only option that improves real readability inside this deliverable without touching a token or a shipped component — the lazy fix that is also the correct one. B is the honest long-term answer but is a kit-wide retone that dwarfs this spec; if you want it, do it as its own spec with a visual review. C is defensible only if the slightly-darker status-bar text in A is considered a design regression worth the accessibility cost; say so explicitly and it will be recorded as such. |
| Q3 — ThemeColor has no onPop token (verified: 22 fields), yet pop is deliberately dark in BOTH palettes (#18191C light, #2C2D31 dark). Pairing pop with the theme's default ink reports 1.01:1 in light — an artefact of a missing token, not a real defect. CounterBadge already hardcodes Color.White on a signal fill with a KDoc explaining why. Note this shell renders no pop surface at all (toolbar/status bar are chrome, stage is stage, panel is surface). How should the sweep handle pop? | ["A — Keep the Color.White literal in the sweep, add no token (RECOMMENDED). The sweep pairs pop with Color.White following CounterBadge's documented precedent and records the ratio; the missing token stays an open question for whichever spec first renders a popover. Zero code change, consistent with Q1-A's baseline tier, and nothing in this deliverable renders pop.", "B — Add an onPop token now (light #FFFFFF, dark #F1F1F2) to ThemeColor and both palettes, and pair against it. This is ADDITIVE — it changes no existing token value, so it does not violate AC-36's 'used unmodified' the way editing a value would, and the refiner agrees it is defensible under AC-36 as written. Cost: one new parameter on a public data class (binary-incompatible, though the only constructors are the two in-repo palettes), two palette lines, and a follow-up to migrate CounterBadge's literal. Buys a correct pairing for every future popover and removes a permanent footnote from the sweep.", "C — Exclude pop from the sweep entirely until something renders it. Honest about scope (this shell paints no pop surface), smallest test, and defers both the token and the measurement. Risk: the pairing is simply unmeasured rather than measured-and-waived, so a future reader cannot tell it was considered."] | A — Keep the Color.White literal in the sweep, add no token (RECOMMENDED). The sweep pairs pop with Color.White following CounterBadge's documented precedent and records the ratio; the missing token stays an open question for whichever spec first renders a popover. Zero code change, consistent with Q1-A's baseline tier, and nothing in this deliverable renders pop. | A buys a recorded, honest number for zero code. B is genuinely legitimate under AC-36 (additive, not a modification) and is the right call the moment a popover ships — but adding a public API field for a surface this deliverable never paints is scaffolding for later, and later can add it in one line. C leaves a hole in the record for no saving over A. |

### readyForBacklog

true

### survey

| question | options | answer | source |
| --- | --- | --- | --- |
| Contrast-sweep assertion policy (AC-36 unmodified-tokens vs AC-30/AC-40/NFR-11 WCAG) | ["A rendered-scope WCAG + frozen baseline", "B strict WCAG, amend tokens", "C baseline only", "D dark AA / light no-regression"] | A - Rendered-scope WCAG + frozen baseline for the rest. Hard-fail on the pairings this shell actually paints (ink/ink2 on bg/chrome/stage/surface at 4.5:1; focus and composited line/line2 at 3:1; onPrimary on primary at 4.5:1); every other pairing asserts only no-regression against a checked-in recorded value, each sub-threshold entry carrying an inline // WAIVED: comment naming the ratio and the AC waived. AC-36 stays intact; literal AC-40 is waived for four light-mode pairs this spec never renders; the downstream android-device-discovery-mirroring spec must re-open the question when it first paints them. | user-edited |
| ink3 treatment - it IS rendered by this shell and it is text (light 3.13:1 on bg, ~2.89:1 on chrome, ~2.73:1 on stage; dark 3.39:1 on bg, ~3.07:1 on chrome) | ["A meta ink at 3:1 + shell authors with ink2", "B hold to 4.5:1 and darken the token", "C accept as-is, fully waived"] | A - Classify ink3 as incidental/meta ink at 3:1 and have the shell's own new files use Theme.color.ink2 (5.87:1 on chrome) for any text the shell itself authors. Sweep asserts ink3 >= 3:1 where it passes, records the chrome and stage pairings as waived baseline entries, and encodes the WCAG 1.4.3 inactive-component exemption by name. No token changes; residual ink3 text lives inside shipped components out of scope to edit. | user-edited |
| pop has no onPop token (ThemeColor has 22 fields); pop is dark in both palettes so default ink pairs at 1.01:1 in light | ["A Color.White literal in the sweep, no token", "B add an onPop token now", "C exclude pop from the sweep"] | A - Keep the Color.White literal in the sweep, following CounterBadge's documented precedent, and record the ratio. Add no token. The missing onPop stays an open question for whichever spec first renders a popover; this shell renders no pop surface. | user-edited |
