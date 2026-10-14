# app-shell-window-layout — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | awaiting-review |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-14T18:16:35Z |
| updatedAt | 2026-10-14T18:16:35Z |

## Gate

### type

human

## Provenance

### selfHash

44fd97a33c244db67d1873155524fd79164b87ebf87f5cf968493cb9578dbf30

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 5e31b6387e3b1c79bd434c100ad248431396d5d1df5006f81b0b4252e2c9c385 |

## Data

### overview

The App Shell & Canonical Window Layout deliverable is the single macOS Compose Desktop window shell every other Rectify feature renders inside: a bezel-less live-mirror stage, a resizable three-tab side panel (Notes, Payload, History), a toolbar frame, a bottom status bar, and the light/dark theming system. This deliverable adopts the existing Rectify UI Kit token system (presentation-core-styling in rectify-kmp) rather than authoring new palettes. It defines slots and regions only -- device discovery, device-picker content, annotation, and tab content are downstream specs' responsibility.

### problemStatement

No shared window shell exists yet. All 16 catalog screens and every downstream feature (device discovery, annotation, payload, history) need a single canonical layout to render inside; without it each feature would improvise its own window chrome. A complete light/dark design-token system (ThemeColor, AppTheme) already exists in a sibling repository (rectify-kmp's presentation-core-styling module) authored from the same style guide as this deliverable's design source -- this shell must wire into it rather than re-author colors.

### purpose

Ship a reusable Compose Multiplatform Desktop (JVM 21, macOS-only) window shell -- toolbar frame, bezel-less mirror stage, resizable three-tab side panel frame, status bar -- wired to the existing Rectify UI Kit theme system (AppTheme/ThemeMode, light/dark palettes, signal colors), so downstream features only implement content inside already-defined slots.

### goals

- Establish the canonical Rectify Window structure (toolbar, bezel-less stage, resizable three-tab side panel, status bar) as a reusable Compose Desktop shell.
- Adopt and wire the existing light/dark design-token system and signal-color palette (presentation-core-styling / AppTheme) with confirmed, checkable contrast parity -- do not author a new palette.
- Resolve the window's initial and minimum size, and the side panel's resize/persistence behavior.
- Keep the shell's acceptance surface limited to frame/slots/theming; explicitly exclude device discovery, tab content, and live toolbar/status-bar wiring.

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-12 | Given the canonical window layout is confirmed, then the side panel exposes exactly three tabs: Notes, Payload, History, over a bezel-less mirror. | must | inspection | true |
| AC-30 | Given the user toggles light/dark theme, then all screens render correctly in both themes with consistent signal colors (red/green/orange/blue) and no unreadable contrast regions. | must | inspection | true |
| AC-31 | Given no mirroring session is active, when the window is displayed, then the side panel is not rendered at all; the toolbar shows its stubbed 'No device' state and the bezel-less stage shows the empty-state placeholder region, matching both cached design screenshots. | must | inspection | true |
| AC-32 | Given a mirroring session becomes active, when the side panel first appears, then the Notes tab is the active/selected tab by default. | should | inspection | true |
| AC-33 | Given the side panel is visible, when the user presses Cmd+1, Cmd+2, or Cmd+3, then the Notes, Payload, or History tab (respectively) becomes active, in addition to click-to-switch. | should | test | true |
| AC-34 | Given the application window, then its initial size is 1240x820, it is user-resizable, and it cannot be resized below 1024x700. | must | inspection | true |
| AC-35 | Given the user toggles the manual theme override control, then the app switches between light and dark palettes immediately without restart, and the chosen theme persists across app relaunch; absent a manual override, the app follows macOS system appearance (ThemeMode.System). | must | test | true |
| AC-36 | Given the existing Rectify UI Kit token system (attributeLightColorPalette / attributeDarkColorPalette in presentation-core-styling), then this deliverable adopts it as-is -- every neutral token (bg, chrome, stage, surface, pop, ink, ink2, ink3, primary, primaryHover, primaryPress, onPrimary, focus, line, line2, hover, seg, codeBg) and every signal token (annotate, sentFixed, warnStillOff, measure) is used unmodified, and surface/ink contrast in both palettes meets WCAG AA (>=4.5:1 text, >=3:1 non-text). | must | inspection | true |
| AC-37 | Given the toolbar and status bar are rendered, then their content is limited to stubbed/static placeholder slots in this deliverable -- the platform switch, device indicator, staged counter, Clear control, Resolve & Send control, and tool-version status line render in a fixed, non-functional placeholder state with no live adb/idb/device wiring. | must | inspection | true |
| AC-38 | Given the side panel is visible, when the user drags the handle between the stage and panel, then the panel width adjusts live and is clamped between 260px and 480px (the drag cannot push it narrower or wider than those bounds). | must | test | true |
| AC-39 | Given the user has resized the side panel and then relaunches the app, then the panel reopens at the previously set width (not reset to a default) the next time a mirroring session makes it visible. | must | test | true |
| AC-40 | Given light or dark theme is active, then each signal color (annotate/red, sentFixed/green, warnStillOff/orange, measure/blue) -- identical in both themes per the adopted token system -- meets WCAG AA contrast against its paired surface/ink tokens in that theme (>=3:1 for non-text indicators such as dots/icons, >=4.5:1 where rendered as text); parity is verified through surface/ink pairing per theme, not by asserting different signal values between themes. | must | inspection | true |

### functionalRequirements

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

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |
| NFR-11 | accessibility | Every surface/ink token pairing and every signal-color-on-surface pairing, in both light and dark themes, meets WCAG AA contrast: >=4.5:1 for text against its background, >=3:1 for non-text UI components (borders, icons, focus indicators) against adjacent colors. |

### outOfScope

- Device discovery, adb/idb detection logic, and device-picker content (belongs to android-device-discovery-mirroring).
- Notes, Payload, and History tab CONTENT -- only the three-tab frame/slots are in scope here.
- Annotation tooling on the mirrored stage.
- Resolve & Send / MCP wiring and behavior -- the toolbar only reserves the control slot.
- Windows/Linux builds (NFR-10).
- Live wiring of toolbar/status bar to real tool-version or device state -- stubbed placeholder content only (FR-12, FR-13, AC-37).
- Authoring new light/dark token values or signal colors -- this deliverable adopts the existing presentation-core-styling (Rectify UI Kit) palette and AppTheme as-is (FR-19, AC-36).
- Deciding HOW the presentation-core-styling token system is obtained from the rectify-kmp repository into this (different) repository's build -- module reuse, source port, or shared dependency is a Stage 2 architecture decision.

### constraints

| type | text |
| --- | --- |
| tech | Kotlin + Compose Multiplatform Desktop (JVM 21), macOS host via compose.desktop.currentOs -- resolved by operator dictation; no AppKit/SwiftUI/Electron. |
| tech | Carry forward the proven platform choices from the rectify-poc reference (Compose Desktop window lifecycle, JVM 21, kotlinx-serialization) without preserving its module structure verbatim; may adopt rectify-kmp conventions (core/ vs source/, explicitApi()) where the plan calls for it. |
| tech | Theming must adopt the existing Rectify UI Kit token system (presentation-core-styling: ThemeColor, attributeLightColorPalette, attributeDarkColorPalette, AppTheme) rather than a new one; per that module's own rule (.claude/rules/design-tokens.md), every token is read only behind the Theme singleton from @Composable scope -- never a literal Color(0x...), raw .dp, or hardcoded TextStyle at a call site. |
| business | v1 ships macOS only (NFR-10). |

### assumptions

| id | text |
| --- | --- |
| AS-1 | The 1240x820 window size and the OKLCH signal-color values captured as 'unverified design context' in the upstream PRD handoff are confirmed spec as of this clarify survey. |
| AS-2 | Device discovery, device-picker content, annotation, and payload/history tab content are implemented entirely in downstream specs (e.g., android-device-discovery-mirroring); this deliverable only defines the slots/frames those features render into. |
| AS-3 | macOS system-appearance (light/dark) detection is available through Compose Multiplatform Desktop / Skiko APIs (isSystemInDarkTheme()) on JVM 21 without additional native interop -- already proven by rectify-kmp's AppTheme / ThemeMode.System. |
| AS-4 | This deliverable depends on and adopts the existing Rectify UI Kit design-token system (presentation-core-styling module in /Users/andrew-malitchuk/StudioProjects/rectify-kmp: ThemeColor, attributeLightColorPalette, attributeDarkColorPalette, AppTheme, ThemeMode) rather than authoring a new palette. The four signal colors (annotate, sentFixed, warnStillOff, measure) are identical across light and dark themes by design, so theme contrast parity (NFR-9/AC-30) is achieved via the surface/ink tokens, not by varying signal values between themes. |
| AS-5 | AppTheme's own function-parameter default is ThemeMode.Dark ('the primary Rectify experience' per its KDoc); this shell passes ThemeMode.System explicitly per FR-18's survey-confirmed default, overriding AppTheme's parameter default rather than relying on it, with a persisted manual override on top. |

### dependencies

| on | note |
| --- | --- |
| android-device-discovery-mirroring | Downstream spec consumes this shell's toolbar device-indicator slot and stage region; not a blocking dependency for this deliverable. |
| Claude Design project 'Rectify Screens' | Cached screenshots (01-first-run-platform-source.png, 02-empty-no-device.png) are the authoritative visual reference for toolbar/status-bar/stage composition; URL must not be re-fetched. |
| presentation-core-styling (rectify-kmp repository) | Source of the existing light/dark token system (ThemeColor, attributeLightColorPalette, attributeDarkColorPalette, AppTheme) this deliverable adopts rather than re-authors. Lives in a different repository than this spec's working directory; integration mechanism is a Stage 2 decision (see open questions). |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring"] |
| SC-2 | Toggle light/dark theme | happy | ["User opens app; theme follows macOS system appearance by default (ThemeMode.System)", "User triggers the manual theme-override toggle", "App immediately re-renders toolbar, stage, side panel, and status bar using the existing dark palette, with all four signal colors unchanged and still legible", "User relaunches the app", "The manually chosen theme persists"] |
| SC-3 | Switch side-panel tabs via keyboard | happy | ["Mirroring session is active and the side panel is visible on the default Notes tab", "User presses Cmd+2", "Payload tab becomes active", "User presses Cmd+3", "History tab becomes active"] |
| SC-4 | Pre-mirror window has no side panel | edge | ["App launches with no device connected", "Window displays toolbar (stubbed 'No device' state), stubbed status bar, and the bezel-less empty-state stage", "Side panel is absent", "A mirroring session starts", "Side panel appears, defaulting to the Notes tab"] |
| SC-5 | Resize and persist side-panel width | edge | ["Mirroring session active, side panel visible at its default width", "User drags the resize handle narrower", "Panel width stops at the 260px minimum", "User drags wider past the maximum", "Panel width stops at the 480px maximum", "User relaunches the app", "Panel reopens at the last-set width, not the default"] |

### successMetrics

- 100% of the adopted light-theme and dark-theme token pairs (surface/ink and signal-on-surface) pass WCAG AA contrast (NFR-11).
- The shell renders with zero layout changes required when a downstream feature (e.g., android-device-discovery-mirroring) fills the toolbar/stage/panel slots with real content.

### openQuestions

- Whether the macOS window traffic-light controls are native OS-drawn decorations or custom-drawn chrome is not yet decided; recommend native decorations for v1 to avoid extra custom-chrome implementation work, to be confirmed during planning.
- Exact persistence mechanism (e.g., multiplatform-settings key names) for the manual theme-override choice and the side-panel width is left to the implementation plan.
- Minor OKLCH discrepancy: the upstream PRD handoff records the green 'sent/fixed' signal color at hue 152 (oklch(0.66 0.15 152)), but the shipped presentation-core-styling token (sentFixed) encodes it as oklch(0.66 0.15 150) -- a 2-degree hue difference. Not resolved here; flagged for Stage 2 to confirm which value is authoritative (likely the shipped code, since it is the working implementation).
- This spec's working repository (/Users/andrew-malitchuk/Documents/rectify) is different from rectify-kmp, where presentation-core-styling actually lives. How the token system is obtained into this repository's build -- module/source reuse, a ported copy, or a shared published dependency -- is an architecture decision for Stage 2 brainstorm/plan, not resolved here.

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Side panel (Notes/Payload/History) width behavior | ["Fixed width (320px), not user-resizable", "Resizable via a drag handle between stage and panel, min 260px / max 480px, remembers width across sessions", "Resizable via a drag handle, min/max clamped, width resets to default each launch (not persisted)"] | Fixed width (320px), not user-resizable | Neither cached screenshot shows any resize affordance anywhere in the UI; a fixed width is the simplest shell contract for downstream Notes/Payload/History content to design against and matches the 'canonical window' framing (one fixed geometry) rather than introducing a resizable-layout feature not requested by FR-11/AC-12. |
| Side panel presence before mirroring starts, and collapsibility | ["Panel is not rendered at all until a mirroring session is live (matches both cached screenshots, which show zero panel in pre-mirror/empty states); once shown, it is always visible (not collapsible)", "Panel is always present in the window frame, including pre-mirror, showing empty/placeholder tab content; collapsible via a toolbar toggle button", "Panel is not rendered pre-mirror; once shown, the user can collapse/expand it via a drag handle or toolbar icon"] | Panel is not rendered at all until a mirroring session is live (matches both cached screenshots, which show zero panel in pre-mirror/empty states); once shown, it is always visible (not collapsible) | This exactly matches the two authoritative design screenshots (neither shows a panel in the pre-mirror/empty states), keeps this deliverable's acceptance surface minimal (no collapse-state persistence logic to build and verify), and still satisfies AC-12's 'canonical window layout is confirmed' framing, which implies an active session. |
| Default active tab and tab-switch affordance | ["Default to the Notes tab on every mirroring session start; switch by click only", "Default to the Notes tab; switch by click or Cmd+1/Cmd+2/Cmd+3 keyboard shortcuts", "Remember the last active tab across sessions (persisted); switch by click only"] | Default to the Notes tab; switch by click or Cmd+1/Cmd+2/Cmd+3 keyboard shortcuts | Notes-first matches FR-11's canonical left-to-right tab order (Notes, Payload, History); the device-picker screenshot already establishes a keyboard-first interaction pattern (its footer shows '↑↓ select · ↵ start'), so adding numeric shortcuts is consistent and cheap, whereas persisting the last-used tab adds state-management scope not justified for a shell-only deliverable. |
| How is light/dark theme selected? | ["Follow macOS system appearance automatically only (no manual override)", "Manual toggle only (a control in the toolbar or status bar), independent of system appearance", "Both: default to following macOS system appearance, with a manual override toggle that persists"] | Both: default to following macOS system appearance, with a manual override toggle that persists | Native macOS apps are expected to respect system appearance by default (reinforced by NFR-10's macOS-only scope), but a manual override is only a small toggle on top of the token system NFR-9 already requires, covering users who want to force a theme independent of system setting at low added cost. |
| How are dark-theme tokens derived from the confirmed light tokens? | ["Mechanically invert: convert each light OKLCH token's lightness (L' = 1 - L) while holding chroma and hue constant, including the four signal colors (red/green/orange/blue)", "Hand-tuned dark ramp: independently choose dark bg/chrome/surface/ink/line/etc. values (not derived via a formula), constraining only the signal colors to meet the same contrast ratio as light", "Hybrid: mechanically invert the neutral tokens (bg, chrome, surface, ink, line, hover, codeBg, shadow) but hand-tune the four signal colors (red/green/orange/blue) individually for perceptual balance and contrast against the dark background"] | Hybrid: mechanically invert the neutral tokens (bg, chrome, surface, ink, line, hover, codeBg, shadow) but hand-tune the four signal colors (red/green/orange/blue) individually for perceptual balance and contrast against the dark background | Naive lightness-inversion of saturated signal colors (e.g., red oklch(0.63 0.22 27)) often blows out chroma or loses contrast against a dark background; neutrals invert cleanly and predictably, but there are only 4 signal colors, so hand-picking them against the NFR-9/AC-30/NFR-11 contrast requirement is cheap and yields a specific, checkable value set rather than an unverified formula output. |
| Target and minimum window size | ["Fixed initial size 1240x820 (from design context), minimum resizable down to 1024x700", "Fixed initial size 1240x820, window is NOT resizable (fixed size only) for v1", "Initial size 1240x820, minimum 900x600, no maximum (user can resize freely)"] | Fixed initial size 1240x820 (from design context), minimum resizable down to 1024x700 | 1240x820 is the only concrete size figure carried from the design-source context (flagged 'unverified' upstream, confirmed via this survey); a sensible minimum (1024x700) keeps the bezel-less stage plus side panel usable without shipping a frozen, non-resizable window, which would be atypical for a native macOS app and need separate justification. |
| Is toolbar/status-bar content part of this deliverable's acceptance surface, or only a stubbed slot? | ["Stubbed slots only: toolbar/status bar render fixed, non-functional placeholder content (e.g., static 'No device' text, disabled buttons); only the frame/regions and theming are verified here. Real device state, counts, and wiring belong to downstream specs (android-device-discovery-mirroring, etc.)", "Toolbar/status bar are fully functional and wired to live state in this deliverable (device detection, staged count, Resolve & Send action)"] | Stubbed slots only: toolbar/status bar render fixed, non-functional placeholder content (e.g., static 'No device' text, disabled buttons); only the frame/regions and theming are verified here. Real device state, counts, and wiring belong to downstream specs (android-device-discovery-mirroring, etc.) | Matches the operator dictation's explicit scope boundary ('specify the shell's slots, regions, tokens and theming here; do not implement device discovery') and keeps this deliverable to shell + theme only, consistent with android-device-discovery-mirroring owning the live content. |

### survey

| question | options | answer | source |
| --- | --- | --- | --- |
| Side panel (Notes/Payload/History) width behavior | ["Fixed width (320px), not user-resizable", "Resizable via a drag handle between stage and panel, min 260px / max 480px, remembers width across sessions", "Resizable via a drag handle, min/max clamped, width resets to default each launch (not persisted)"] | Resizable via a drag handle between stage and panel, min 260px / max 480px, remembers width across sessions | user-edited |
| Side panel presence before mirroring starts, and collapsibility | ["Panel is not rendered at all until a mirroring session is live (matches both cached screenshots, which show zero panel in pre-mirror/empty states); once shown, it is always visible (not collapsible)", "Panel is always present in the window frame, including pre-mirror, showing empty/placeholder tab content; collapsible via a toolbar toggle button", "Panel is not rendered pre-mirror; once shown, the user can collapse/expand it via a drag handle or toolbar icon"] | Panel is not rendered at all until a mirroring session is live (matches both cached screenshots, which show zero panel in pre-mirror/empty states); once shown, it is always visible (not collapsible) | user-edited |
| Default active tab and tab-switch affordance | ["Default to the Notes tab on every mirroring session start; switch by click only", "Default to the Notes tab; switch by click or Cmd+1/Cmd+2/Cmd+3 keyboard shortcuts", "Remember the last active tab across sessions (persisted); switch by click only"] | Default to the Notes tab; switch by click or Cmd+1/Cmd+2/Cmd+3 keyboard shortcuts | user-edited |
| How is light/dark theme selected? | ["Follow macOS system appearance automatically only (no manual override)", "Manual toggle only (a control in the toolbar or status bar), independent of system appearance", "Both: default to following macOS system appearance, with a manual override toggle that persists"] | Both: default to following macOS system appearance, with a manual override toggle that persists | user-edited |
| How are dark-theme tokens derived from the confirmed light tokens? | ["Mechanically invert: convert each light OKLCH token's lightness (L' = 1 - L) while holding chroma and hue constant, including the four signal colors (red/green/orange/blue)", "Hand-tuned dark ramp: independently choose dark bg/chrome/surface/ink/line/etc. values (not derived via a formula), constraining only the signal colors to meet the same contrast ratio as light", "Hybrid: mechanically invert the neutral tokens (bg, chrome, surface, ink, line, hover, codeBg, shadow) but hand-tune the four signal colors (red/green/orange/blue) individually for perceptual balance and contrast against the dark background"] | use exist one -- the existing presentation-core-styling / Rectify UI Kit token system (attributeLightColorPalette, attributeDarkColorPalette, AppTheme) is adopted as-is; no new dark values are derived, inverted, or hand-tuned | user-edited |
| Target and minimum window size | ["Fixed initial size 1240x820 (from design context), minimum resizable down to 1024x700", "Fixed initial size 1240x820, window is NOT resizable (fixed size only) for v1", "Initial size 1240x820, minimum 900x600, no maximum (user can resize freely)"] | Fixed initial size 1240x820 (from design context), minimum resizable down to 1024x700 | user-edited |
| Is toolbar/status-bar content part of this deliverable's acceptance surface, or only a stubbed slot? | ["Stubbed slots only: toolbar/status bar render fixed, non-functional placeholder content (e.g., static 'No device' text, disabled buttons); only the frame/regions and theming are verified here. Real device state, counts, and wiring belong to downstream specs (android-device-discovery-mirroring, etc.)", "Toolbar/status bar are fully functional and wired to live state in this deliverable (device detection, staged count, Resolve & Send action)"] | Stubbed slots only: toolbar/status bar render fixed, non-functional placeholder content (e.g., static 'No device' text, disabled buttons); only the frame/regions and theming are verified here. Real device state, counts, and wiring belong to downstream specs (android-device-discovery-mirroring, etc.) | user-edited |
