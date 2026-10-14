# SDD input — App Shell & Canonical Window Layout

`app-shell-window-layout`

## Goal

The foundational macOS app shell implementing the canonical 'Rectify Window': a bezel-less live-mirror region with a side panel exposing exactly three tabs (Notes, Payload, History), plus the toolbar frame and status-bar chrome that every other feature plugs into. Includes the full light/dark theme system with the signal-color palette (red = bug, green = fixed, orange = warning, blue = measurement) and contrast parity in both themes. Why first: all 16 catalog screens import this layout, and every subsequent feature renders inside it, so the shell and theming must exist before any device, annotation or payload work can land.

## Description

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

### Open questions

- Exact pixel/token-level design specs (1240x820 window size, chrome/stage/surface/ink color tokens, oklch signal-color values, toolbar sub-component layout, typography choices) come only from PRD-intake context referencing a Claude Design project, not a live, independently verifiable Figma/UI-UX handoff bundle for this run. These values should be confirmed against the live design source before implementation if pixel-perfect fidelity is required; FR-11/NFR-9/NFR-10/AC-12/AC-30 are the authoritative, already-confirmed requirements and do not depend on these exact values.
- Implementation platform/framework is unspecified: the PRD does not state whether this is a native macOS app (AppKit/SwiftUI) or a cross-platform framework (e.g. Electron) targeting macOS only (per NFR-10). This is a real and material gap for an SDD architecture stage to resolve before design/implementation work begins.
- No UI/UX handoff bundle exists for this project (PRD-only flow); designReferences is empty rather than fabricated.

