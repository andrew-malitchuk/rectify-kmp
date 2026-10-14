# rectify — error-handling-and-recovery

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | error-handling-and-recovery |
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

1dec12c70191083c62977c55269548096a4005a3395fc2b7491758ccebc1d56c

## Data

### sourcePrd

rectify

### id

F-3

### slug

error-handling-and-recovery

### title

Error Handling & Stream Recovery

### goalStatement

The structured error-state system: a reusable error-card component (icon-in-circle, title, body, optional copyable command block, primary + secondary actions, footer diagnostic line) instantiated for adb-not-in-PATH ('Locate adb…' / 'Retry'), no-device-detected (kill-server command, 'Restart adb' / 'Boot emulator instead'), and multiple-devices-connected (inline device list rather than a modal, primary action naming the top device); plus the mid-session stream-drop banner that floats non-blockingly over the stale mirror, keeps staged annotations pinned, offers 'Reconnect', and leaves 'Resolve & Send' usable while disconnected. Why: research flags adb/scrcpy fragility across OS and device versions as the biggest execution risk to the >=95% first-attempt connect target, so recoverable failure is a product feature, not polish.

### rawDescription

The structured error-state system: a reusable error-card component (icon-in-circle, title, body, optional copyable command block, primary + secondary actions, footer diagnostic line) instantiated for adb-not-in-PATH ('Locate adb…' / 'Retry'), no-device-detected (kill-server command, 'Restart adb' / 'Boot emulator instead'), and multiple-devices-connected (inline device list rather than a modal, primary action naming the top device); plus the mid-session stream-drop banner that floats non-blockingly over the stale mirror, keeps staged annotations pinned, offers 'Reconnect', and leaves 'Resolve & Send' usable while disconnected. Why: research flags adb/scrcpy fragility across OS and device versions as the biggest execution risk to the >=95% first-attempt connect target, so recoverable failure is a product feature, not polish.

### priority

must

### phase

PH-1

### estimate

M

### dependsOn

- F-1
- F-2

### linkedGoals

- GOAL-1
- GOAL-5

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-23 | Render structured error-card states (icon-in-circle, title, body, optional copyable command block, primary+secondary actions, footer diagnostic line) for: adb not in PATH, no device detected, and multiple devices connected (with an inline device list/picker instead of a modal, defaulting the primary action to the top-listed device). | must |
| FR-24 | On a mid-session stream drop, show a non-blocking floating banner over the stale mirror ('Stream dropped' / 'Your N annotations are kept on the frozen frame' / 'Reconnect'); staged annotations stay pinned and 'Resolve & Send' remains usable while disconnected. | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-6 | reliability | Stream-drop handling must preserve all staged annotations without loss across a disconnect/reconnect cycle. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-26 | Given adb is not found on PATH, then an error card renders with the install command (with Copy), primary 'Locate adb…' and secondary 'Retry' actions. | must | test |
| AC-27 | Given no Android device is detected after launch, then an error card renders with the kill-server command and primary 'Restart adb' / secondary 'Boot emulator instead' actions. | must | test |
| AC-28 | Given 3 devices are connected simultaneously, then the error card shows an inline device list (not a modal) and the primary action names the top device (e.g. 'Mirror Pixel 8 Pro'). | must | test |
| AC-29 | Given the stream drops mid-session with staged annotations present, then a non-blocking banner appears stating the stream dropped and that annotations are kept on the frozen frame, with a 'Reconnect' primary action; 'Resolve & Send' remains clickable and annotations remain pinned throughout. | must | test |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-10 | adb not on PATH | error | ["Launch with adb missing from PATH", "Error card renders with install command", "User copies command or clicks Locate adb…/Retry"] |
| SC-11 | No Android device detected | error | ["adb present but no device attached", "Error card renders with kill-server command", "User runs suggested command or boots an emulator instead"] |
| SC-12 | Multiple devices connected | error | ["3 devices are connected simultaneously", "Inline device list/picker renders (no modal)", "User accepts the top-named device or picks another"] |
| SC-13 | Stream dropped mid-annotation | error | ["Stream drops while annotations are staged", "Non-blocking banner appears over the stale mirror", "Annotations remain pinned", "User clicks Reconnect or still sends via Resolve & Send"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-11 | QA Tester (PER-2) | actionable error cards with copyable commands when adb is missing, no device is found, or several devices are attached | I can fix my own setup rather than filing a tooling ticket | ["FR-23"] | ["AC-26", "AC-27", "AC-28"] | ["SC-10", "SC-11", "SC-12"] | must |
| US-12 | Mobile Developer (PER-1) | my staged annotations to survive a mid-session stream drop and still be sendable | a flaky USB cable never costs me the bug report I just wrote | ["FR-24"] | ["AC-29"] | ["SC-13"] | must |

### sddReady

true
