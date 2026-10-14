# rectify — android-device-discovery-mirroring

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | android-device-discovery-mirroring |
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

08e3eca737437b34e1eda970186a13b250f64a540a4fccc201c1fe073e807fd6

## Data

### sourcePrd

rectify

### id

F-2

### slug

android-device-discovery-mirroring

### title

Android Device Discovery & Live Mirroring

### goalStatement

Everything needed to get a live Android screen into the window: launch-time adb/idb tool detection with per-platform toolbar status dots, the Android/iOS platform switch (Android functional in v1), the device picker with Physical and Emulator sections including inline Boot for shut-down emulators and arrow-key/Enter navigation, the primary 'Start mirroring' action over adb + scrcpy, the 2s-polling 'NO SIGNAL' empty state with inline emulator boot, and the status bar reporting tool version, resolution, fps and latency. Why: the live mirror is the annotation surface and the whole product's entry point; low latency here (NFR-1) is what makes drag-and-freeze feel responsive.

### rawDescription

Everything needed to get a live Android screen into the window: launch-time adb/idb tool detection with per-platform toolbar status dots, the Android/iOS platform switch (Android functional in v1), the device picker with Physical and Emulator sections including inline Boot for shut-down emulators and arrow-key/Enter navigation, the primary 'Start mirroring' action over adb + scrcpy, the 2s-polling 'NO SIGNAL' empty state with inline emulator boot, and the status bar reporting tool version, resolution, fps and latency. Why: the live mirror is the annotation surface and the whole product's entry point; low latency here (NFR-1) is what makes drag-and-freeze feel responsive.

### priority

must

### phase

PH-1

### estimate

L

### dependsOn

- F-1

### linkedGoals

- GOAL-1
- GOAL-5

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-1 | On launch, detect installed adb and idb tool versions and show a status dot per platform in the toolbar. | must |
| FR-2 | Show a platform switch (Android/iOS) in the toolbar on every screen; Android is functional in v1, iOS is deferred to v1.1+. | must |
| FR-3 | Provide a device picker dropdown with Physical-device and Emulator sections (running, or shut-down with an inline Boot button), navigable via arrow keys and Enter. | must |
| FR-4 | Provide a primary 'Start mirroring' action that connects to the selected device and begins the live mirror stream. | must |
| FR-5 | When no device is connected, poll adb every 2s and show a 'NO SIGNAL' empty state with guidance to plug in a USB-debug device or boot an emulator inline. | must |
| FR-22 | Show a status bar with live stream stats: tool version, resolution, fps, and latency. | should |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-1 | performance | Live mirror glass-to-glass latency should stay low enough (target under ~150ms) that drag-to-select and freeze feel responsive. |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-1 | Given adb and idb are installed, when the app launches, then the toolbar shows a status dot reflecting detected tool versions for both platforms. | must | demo |
| AC-2 | Given a physical device is connected and an emulator is running, when the device picker is opened, then both appear in distinct Physical/Emulator sections and arrow keys plus Enter select an entry. | must | test |
| AC-3 | Given a device is selected in the picker, when the user clicks 'Start mirroring', then the live mirror begins streaming. | must | test |
| AC-4 | Given no device is connected, when adb polling runs, then the 'NO SIGNAL' state shows with guidance text and an inline emulator picker plus Boot button. | must | demo |
| AC-25 | Given live mirror is active, then the status bar displays current tool version, resolution, fps, and latency, updating at least once per second. | should | demo |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring"] |
| SC-2 | No device connected (empty state) | edge | ["No device is connected", "App polls adb every 2s", "Show NO SIGNAL placeholder with guidance", "Offer inline emulator picker + Boot button"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-1 | Mobile Developer (PER-1) | Rectify to detect my adb/idb install and list my connected devices and emulators on launch | I can start mirroring the device under test without touching a terminal | ["FR-1", "FR-2", "FR-3", "FR-4"] | ["AC-1", "AC-2", "AC-3"] | ["SC-1"] | must |
| US-2 | QA Tester (PER-2) | a clear NO SIGNAL state with inline guidance and an emulator Boot button when nothing is attached | I can get to a working mirror myself without asking a developer for help | ["FR-5"] | ["AC-4"] | ["SC-2"] | must |
| US-13 | Mobile Developer (PER-1) | the canonical 3-tab window with live stream stats, readable in light and dark themes | I can work in my preferred appearance and always see stream health at a glance | ["FR-11", "FR-22"] | ["AC-12", "AC-25", "AC-30"] | ["SC-1"] | must |

### sddReady

true
