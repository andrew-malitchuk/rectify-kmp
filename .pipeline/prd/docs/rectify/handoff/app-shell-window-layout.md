# rectify — app-shell-window-layout

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | app-shell-window-layout |
| status | done |
| producedBy | prd.py handoff |
| schemaVersion | 1 |
| createdAt | 2026-10-11T16:12:17Z |
| updatedAt | 2026-10-11T16:12:17Z |
| dryRun | false |

## Gate

### type

auto

### approvedBy

prd.py

### approved

true

### at

2026-10-11T16:12:17Z

## Provenance

### inputs

| artifact | hash |
| --- | --- |
| prd.json | a7b4b0d1c39a04a00aec0acd67900ad85595779106bea6f82ae1265133169110 |

### selfHash

95e4574e93f01bebe3fee40faeed30c81df6de034be05f040085a4af159d1a3f

## Data

### sourcePrd

rectify

### id

F-1

### slug

app-shell-window-layout

### title

App Shell & Canonical Window Layout

### goalStatement

The foundational macOS app shell implementing the canonical 'Rectify Window': a bezel-less live-mirror region with a side panel exposing exactly three tabs (Notes, Payload, History), plus the toolbar frame and status-bar chrome that every other feature plugs into. Includes the full light/dark theme system with the signal-color palette (red = bug, green = fixed, orange = warning, blue = measurement) and contrast parity in both themes. Why first: all 16 catalog screens import this layout, and every subsequent feature renders inside it, so the shell and theming must exist before any device, annotation or payload work can land.

### rawDescription

The foundational macOS app shell implementing the canonical 'Rectify Window': a bezel-less live-mirror region with a side panel exposing exactly three tabs (Notes, Payload, History), plus the toolbar frame and status-bar chrome that every other feature plugs into. Includes the full light/dark theme system with the signal-color palette (red = bug, green = fixed, orange = warning, blue = measurement) and contrast parity in both themes. Why first: all 16 catalog screens import this layout, and every subsequent feature renders inside it, so the shell and theming must exist before any device, annotation or payload work can land.

### priority

must

### phase

PH-1

### estimate

M

### dependsOn


### linkedGoals

- GOAL-5

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-11 | Use the canonical 'Rectify Window' layout: a bezel-less live mirror with a side panel exposing exactly three tabs (Notes, Payload, History). | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-12 | Given the canonical window layout is confirmed, then the side panel exposes exactly three tabs: Notes, Payload, History, over a bezel-less mirror. | must | inspection |
| AC-30 | Given the user toggles light/dark theme, then all screens render correctly in both themes with consistent signal colors (red/green/orange/blue) and no unreadable contrast regions. | must | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-13 | Mobile Developer (PER-1) | the canonical 3-tab window with live stream stats, readable in light and dark themes | I can work in my preferred appearance and always see stream health at a glance | ["FR-11", "FR-22"] | ["AC-12", "AC-25", "AC-30"] | ["SC-1"] | must |

### sddReady

true
