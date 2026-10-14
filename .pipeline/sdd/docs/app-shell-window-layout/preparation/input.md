# app-shell-window-layout — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | awaiting-review |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-14T17:57:44Z |
| updatedAt | 2026-10-14T17:57:44Z |

## Gate

### type

human

## Provenance

### selfHash

5e31b6387e3b1c79bd434c100ad248431396d5d1df5006f81b0b4252e2c9c385

### inputs

| artifact | hash |
| --- | --- |
| inputs/input.json | 4076afb601a505708346e04ceec1d25b77364e5431dc5ee9653ba5583cee9fd6 |

## Data

### title

App Shell & Canonical Window Layout

### goalStatement

The foundational macOS app shell implementing the canonical 'Rectify Window': a bezel-less live-mirror region with a side panel exposing exactly three tabs (Notes, Payload, History), plus the toolbar frame and status-bar chrome that every other feature plugs into. Includes the full light/dark theme system with the signal-color palette (red = bug, green = fixed, orange = warning, blue = measurement) and contrast parity in both themes. Why first: all 16 catalog screens import this layout, and every subsequent feature renders inside it, so the shell and theming must exist before any device, annotation or payload work can land.

### rawDescription

```
The foundational macOS app shell implementing the canonical 'Rectify Window': a bezel-less live-mirror region with a side panel exposing exactly three tabs (Notes, Payload, History), plus the toolbar frame and status-bar chrome that every other feature plugs into. Includes the full light/dark theme system with the signal-color palette (red = bug, green = fixed, orange = warning, blue = measurement) and contrast parity in both themes. Why first: all 16 catalog screens import this layout, and every subsequent feature renders inside it, so the shell and theming must exist before any device, annotation or payload work can land.

--- Confirmed requirements (from PRD handoff, feature F-1, trace ids preserved) ---

Functional Requirements:
- FR-11 (must): Use the canonical 'Rectify Window' layout: a bezel-less live mirror with a side panel exposing exactly three tabs (Notes, Payload, History).

Non-Functional Requirements:
- NFR-9 (ux): All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes.
- NFR-10 (compatibility): v1 targets macOS only; no Windows/Linux builds.

Acceptance Criteria:
- AC-12 (must, verify=inspection): Given the canonical window layout is confirmed, then the side panel exposes exactly three tabs: Notes, Payload, History, over a bezel-less mirror.
- AC-30 (must, verify=inspection): Given the user toggles light/dark theme, then all screens render correctly in both themes with consistent signal colors (red/green/orange/blue) and no unreadable contrast regions.

Scenarios:
- SC-1 'First-run setup and tool detection' (happy): Launch app -> Detect adb/idb and show status dots -> Open device picker showing Physical/Emulator sections -> Select a device via keyboard or click -> Click Start mirroring.

User Stories:
- US-13: As a Mobile Developer (PER-1), I want the canonical 3-tab window with live stream stats, readable in light and dark themes, so that I can work in my preferred appearance and always see stream health at a glance. Linked FR: FR-11, FR-22 (FR-22 belongs to a different feature; out of scope here, retained only for traceability). Linked AC: AC-12, AC-25 (AC-25 belongs to a different feature; out of scope here), AC-30. Linked scenarios: SC-1. Priority: must.

Note: FR-22 and AC-25 referenced by US-13 are owned by other PRD features (not F-1) and are NOT elaborated here — listed only because US-13 cross-links them.

--- Design-source context (unverified, needs confirmation) ---

The original PRD intake captured design-source context from a Claude Design project ("Rectify: Bug annotation tool") describing the canonical 'Rectify Window' layout in more pixel-level detail than the confirmed FR/NFR/AC above: a 1240x820 macOS window, bezel-less live mirror region, 3-tab side panel (Notes/Payload/History), toolbar with platform switch + device indicator + staged counter + Clear + Resolve&Send button, bottom status bar, full light/dark theme tokens (bg/chrome/stage/surface/ink/ink2/ink3/line/line2/hover/seg/segOn/primary/onPrimary/codeBg/shadow), and signal colors (red=annotate/oklch(0.63 0.22 27), green=sent/oklch(0.66 0.15 152), orange=warn/oklch(0.74 0.15 70), blue=measure). Typography: SF Pro for titles/body, JetBrains Mono for ids/coords/status. This detail is directionally consistent with FR-11/NFR-9/NFR-10/AC-12/AC-30 but these exact pixel/token values have not been re-verified against a live Figma/design source and should be treated as helpful context, not confirmed spec, unless pixel-perfect fidelity matters for this feature.
```

### sources

| type | ref | summary |
| --- | --- | --- |
| text | inputs/input.json | upstream PRD + UI/UX handoff |
| text | .pipeline/sdd/docs/app-shell-window-layout/inputs/raw/prd-handoff.json | PRD handoff bundle for feature F-1 (App Shell & Canonical Window Layout): FR-11, NFR-9, NFR-10, AC-12, AC-30, SC-1, US-13. Confirmed/approved PRD content, authoritative for this feature. |
| url | https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html | Claude Design project: Rectify Screens. Two cached screenshots (01-first-run-platform-source.png, 02-empty-no-device.png) show shell chrome and toolbar composition. Design URL is authenticated and must not be re-fetched; cached images are authoritative. |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| /Users/andrew-malitchuk/StudioProjects/rectify-poc | app/src/main/kotlin/rectify/Main.kt | main, RectifyScreen | Working POC: Compose Desktop window entry point + application {Window} structure. Establishes Compose Desktop JVM 21 platform choice and window lifecycle. Do not treat as template to preserve verbatim, but reference the proven platform decisions (Compose Desktop, JVM 21, kotlinx-serialization). |
| /Users/andrew-malitchuk/StudioProjects/rectify-poc | app/src/main/kotlin/rectify/Devices.kt | Platform, DeviceRef, DeviceBridge, AndroidBridge, IosBridge | Working POC: adb device discovery, screenshot capture, hierarchy dump, and UI element resolution. Establishes device discovery logic that the toolbar device indicator must surface. |
| /Users/andrew-malitchuk/StudioProjects/rectify-poc | app/build.gradle.kts | compose.desktop.currentOs, jvmToolchain(21) | Working POC build config: Compose Desktop, JVM 21 toolchain, kotlinx-serialization-json 1.7.3. |
| /Users/andrew-malitchuk/StudioProjects/rectify-kmp | CLAUDE.md | convention plugins, module structure | Conventions reference: KMP Kotlin multiplatform structure, convention plugins (build-logic/convention), module organization. NOT a target-platform reference — do not assume Android/iOS targets; this deliverable is macOS desktop only (NFR-10). Use for package layout, DI (Koin), MVI (Orbit), and design-token conventions only. |
| /Users/andrew-malitchuk/StudioProjects/rectify-kmp | .claude/rules/package-layout.md | core/ vs source/, explicitApi() | Conventions: package layout split (core internal, source public), explicitApi() enforcement. Recommended pattern for organizing this module. |
| /Users/andrew-malitchuk/StudioProjects/rectify-kmp | .claude/rules/design-tokens.md | design token system | Conventions: design token pattern for theme system. Reference for implementing light/dark theme parity (NFR-9). |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| 01 First run — platform & source | 01-first-run-platform-source | raw/design/01-first-run-platform-source.png | Light theme. Toolbar (top, full width): macOS traffic-light dots (red/amber/green) at far left; segmented Android \| iOS platform switch (Android active, white pill on grey track); device indicator dropdown reading '● No device ▾'; far right — staged counter '⓪ staged', Clear text button, disabled-looking primary Resolve & Send ⌘↵ button. Body: centred first-run panel 'Choose what to mirror / Switch platform or device any time from the toolbar.' PLATFORM section with two selectable cards (Android selected with radio dot, iOS unselected with radio ring), each card showing platform name (semibold) and status dot + monospace detection line. SOURCE section with monospace meta '3 found · adb devices -l', bordered grouped list by header rows (Physical device, Emulator) with light grey header backgrounds, rows with radio control, device name, monospace id, and right-aligned trailing slot (plain text, green dot + Running, or Boot button). Footer: monospace keyboard hints '↑↓ select · ↵ start' at left, primary Start mirroring button at right. Status bar (bottom, full width): green dot + monospace 'adb 35.0.1 · idb 1.1.8 · ready' at left, 'Rectify 0.4' at right. |
| 02 Empty — no device yet | 02-empty-no-device | raw/design/02-empty-no-device.png | Light theme. Same toolbar and status bar frame (status bar reads '● Waiting for device…'). Stage area fills window with subtle dotted/grid texture on warm grey ground (bezel-less live-mirror region in empty state). Left-of-centre: large rounded-rectangle dashed phone outline placeholder with centred monospace NO SIGNAL. Right: text block — 'Waiting for an Android device' heading, 'Mirroring starts on its own as soon as adb sees one.' subtitle, then two numbered steps with circled numerals: (1) 'Plug in a phone with USB debugging on and accept the prompt.' (2) 'Or boot an emulator' followed by dropdown (Pixel_7_API_34 ▾) next to dark Boot button. Below: muted monospace line with dot 'adb devices · polling every 2 s'. |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| other-project | Working proof-of-concept at /Users/andrew-malitchuk/StudioProjects/rectify-poc demonstrates Compose Desktop JVM 21 platform choice, window lifecycle, and device discovery infrastructure (adb/idb bridges). Not a template to preserve, but evidence of proven platform stack. | /Users/andrew-malitchuk/StudioProjects/rectify-poc |

### glossary

| term | definition |
| --- | --- |
| Rectify Window | The canonical app shell layout for Rectify: a bezel-less live-mirror stage region plus a side panel with exactly three tabs (Notes, Payload, History), a toolbar, and a status bar. All other features render inside this shell. |
| side panel | The fixed region of the Rectify Window hosting exactly three tabs: Notes, Payload, History. |
| bezel-less mirror | The live device-mirror stage region of the window, rendered without a surrounding device bezel chrome. |
| signal color | A themed color coding used across the UI: red = bug/annotate, green = fixed/sent, orange = warning, blue = measurement. Must maintain contrast parity in both light and dark themes. |
| theme parity | The requirement (NFR-9) that all screens render correctly and legibly in both light and dark themes, including consistent signal colors and sufficient contrast. |

### attachments


### ambiguities

- AC-12 side-panel design: The three-tab structure (Notes, Payload, History) is confirmed in FR-11, but the cached screenshots (01-first-run-platform-source.png, 02-empty-no-device.png) both show pre-mirroring states where the side panel is NOT visible. The side panel's geometry, default active tab, collapse behavior, tab-switch affordance, and positioning relative to the stage are unillustrated and must be designed during the clarify step.
- AC-30 dark-theme design: Both cached screenshots are light theme only. No dark theme reference exists. The dark token values (background, chrome, surface, ink, ink2, ink3, line, line2, hover, segmented, segmentedOn, primary, onPrimary, codeBg, shadow) and the signal-color oklch values in dark mode are unillustrated and must be resolved during clarify to ensure contrast parity (NFR-9).

### dictation

Engineer dictation provided at pipeline launch (2026-10-14) resolves the platform question: Implementation platform is RESOLVED as Kotlin + Compose Multiplatform Desktop (JVM 21), macOS host via compose.desktop.currentOs. This satisfies NFR-10 (macOS-only v1) because Skiko renderer is built for host OS only, no cross-compilation. Working proof-of-concept exists at /Users/andrew-malitchuk/StudioProjects/rectify-poc: app/src/main/kotlin/rectify/Main.kt establishes Compose Desktop window + entry point; app/src/main/kotlin/rectify/Devices.kt establishes adb/idb device discovery and UI element resolution feeding the toolbar device indicator; build config (app/build.gradle.kts, build.gradle.kts) confirms Kotlin 2.4.20, Compose 1.12.1, JVM 21, kotlinx-serialization. Conventions reference: /Users/andrew-malitchuk/StudioProjects/rectify-kmp is a separate, more rigorously structured KMP template by the same author (convention plugins in build-logic/, core/ vs source/ package split, explicitApi(), Koin DI, Orbit MVI, design tokens). Use as CONVENTIONS reference for module layout only; do NOT assume its Android/iOS targets apply — this deliverable is macOS desktop only (NFR-10). Design source: Claude Design project 'Rectify Screens' at https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html. Two screenshots cached: 01-first-run-platform-source.png (first-run device picker UI, light theme, toolbar + status bar visible), 02-empty-no-device.png (empty state waiting for device, light theme, stage area with placeholder). Both screenshots show shell chrome (toolbar platform switch, device indicator, staged counter, Clear, Resolve & Send button; status bar with tool versions and status). They do NOT show the three-tab side panel (pre-mirroring states) or dark theme (light theme only). Scope: this deliverable is the SHELL + THEME SYSTEM only — window frame, toolbar, side-panel frame, bezel-less mirror stage, status bar, and full light/dark token system. Device-picker content and empty-state content visible in screenshots belong to downstream spec android-device-discovery-mirroring; specify shell slots here, not content.
