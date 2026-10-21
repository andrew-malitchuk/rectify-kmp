# ios-platform-support — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-19T01:54:24Z |
| updatedAt | 2026-10-19T01:54:24Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T01:54:24Z

## Provenance

### selfHash

de1f03b17a9b8d197b530b04d7c9ef96019e6fb2fdf4b6075231451e0b6d24f0

### inputs

| artifact | hash |
| --- | --- |
| inputs/prd-handoff.json | 9f4b49638938b46105d8fa3138e5db4088d622c7717c9c19ce1afcbfa370661a |

## Data

### title

iOS Platform Support (v1.1+)

### goalStatement

Extends the proven Android flow to iOS by implementing the platform switch's iOS branch over idb + simctl: simulator and physical-device discovery, live mirroring, screenshot and view-hierarchy capture, and the two iOS-specific error cards — simulator-not-booted ('Boot simulator' / 'Choose another') and device-not-trusted ("I've tapped Trust — retry" / 'Use simulator'). Reuses F-1's shell, F-4/F-5's annotation and measure surfaces, F-7's MCP handoff and F-9's Verify engine behind a platform abstraction, so only the device-adapter layer and the iOS error states are new. Why: deliberately deferred out of v1 (AS-5) so the complete Android loop ships first; it is the obvious parity follow-up once that loop is validated.

### rawDescription

Extends the proven Android flow to iOS by implementing the platform switch's iOS branch over idb + simctl: simulator and physical-device discovery, live mirroring, screenshot and view-hierarchy capture, and the two iOS-specific error cards — simulator-not-booted ('Boot simulator' / 'Choose another') and device-not-trusted ("I've tapped Trust — retry" / 'Use simulator'). Reuses F-1's shell, F-4/F-5's annotation and measure surfaces, F-7's MCP handoff and F-9's Verify engine behind a platform abstraction, so only the device-adapter layer and the iOS error states are new. Why: deliberately deferred out of v1 (AS-5) so the complete Android loop ships first; it is the obvious parity follow-up once that loop is validated.

### sources

| type | ref | cached | fetchedAt | summary |
| --- | --- | --- | --- | --- |
| text | inputs/prd-handoff.json | raw/prd-handoff.json | 2026-10-11T16:12:17Z | PRD feature F-10 iOS Platform Support: priority could, phase PH-2, estimate L. Contains FR-25, NFR-5/8/9/10, AC-31, SC-14, US-14 — all carried verbatim. Depends on F-2, F-3, F-9 (merged). |
| text | inputs/codebase-recon.md | raw/codebase-recon.md | 2026-10-19T00:00:00Z | Verified survey of F-1..F-9 merged state. Key finding: PRD's claim 'only device-adapter + error states are new' is half true. Interfaces are neutral, but platform abstraction is missing at repository/domain layer. DeviceRepository has no platform param, Koin binds one unqualified instance per interface, adb hardcoded in argv builders, ToolKind/DeviceCommand/HostCommand are closed sets. F-10 is extraction + new iOS actual, not new actual alone. Error cards architecture ready, hierarchy model mostly neutral (iOS can degrade to empty on v1), ProcessRunner reusable. |
| text | inputs/ios-tooling-probe.md | raw/ios-tooling-probe.md | 2026-10-19T00:00:00Z | Measured on macOS 25.1: xcrun simctl present (Xcode 26.3, iOS 17.5 + 18.1 runtimes), idb and idb_companion both installed. No simulator booted. Cannot test physical device-not-trusted without real untrusted iPhone. |
| url | raw/rectify-screens.html | raw/rectify-screens.html | 2026-10-11T00:00:00Z | Design prototype with Mirror/Annotate/Verify/History surfaces. iOS error cards (simulator-not-booted, device-not-trusted) recorded as deferred but already drawn. |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| rectify-kmp | domain-repository-api/src/commonMain/dev/rectify/io/source/repository/DeviceRepository.kt | DeviceRepository | Domain interface: observeDiscovery, mirror, dumpHierarchy, detectTools, bootEmulator, restartAdbServer, setAdbPath. Must add platform param threading. |
| rectify-kmp | data-repository-impl/src/commonMain/dev/rectify/io/source/DeviceRepositoryImpl.kt | DeviceRepositoryImpl | Repository: poll(), listDevicesEscalating(), healDaemon(), killThenStart(). Unconditionally reaches for adbHostSource; must dispatch by platform. |
| rectify-kmp | data-device-impl/src/desktopMain/dev/rectify/io/source/DeviceSourcesProvider.desktop.kt | provideDeviceSources | Single Koin seam for device sources. Currently one unqualified binding per interface. Must widen to qualified Android + iOS bindings. |
| rectify-kmp | data-device-api/src/commonMain/dev/rectify/io/source/parse/DeviceArgv.kt | buildDeviceArgv | Argv builder: hardcodes adb -s selector. Must support simctl -u and idb --udid variants. |
| rectify-kmp | data-device-api/src/commonMain/dev/rectify/io/source/tool/ToolKind.kt | ToolKind | Closed enum: Adb, Idb. Missing xcrun/simctl kind. KDoc warns widening must be visible API change. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/dev/rectify/io/source/window/shell/RectifyWindow.kt | RectifyWindow | Desktop toolbar platform segmented control (:370-382). Hardcodes PLATFORM_SOURCES list; iOS picker card has enabled=platform==Android. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/dev/rectify/io/source/view/MirrorContent.kt | MirrorStageContent | Renders errorCard() BEFORE stage when (:124-128). Platform-blind errorCard drives shadowing trap: iOS selected on no-adb host shows Android card. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/dev/rectify/io/source/model/MirrorContract.kt | MirrorErrorKind | Closed enum: ToolMissing, NoDevice, PollFailed, MultipleDevices. KDoc records err-sim, err-trust as deferred. Must add SimulatorNotBooted, DeviceNotTrusted. |
| rectify-kmp | domain-core/src/commonMain/dev/rectify/io/source/model/device/DiscoveryModel.kt | DiscoveryModel | Currently: Found(physical, emulators). Must add iOS-specific variants for simulator-not-booted, device-not-trusted states. |
| rectify-kmp | data-device-api/src/commonMain/dev/rectify/io/source/datasource/MirrorTransport.kt | MirrorTransport | Interface: frames(DeviceId): Flow<MirrorFrame>, stats: StateFlow<TransportStats>. Already neutral; iOS impl will use xcrun simctl io screenshot or idb screenshot. |
| rectify-kmp | data-device-impl/src/desktopMain/dev/rectify/io/source/datasource/transport/ScreencapMirrorTransport.kt | ScreencapMirrorTransport | Current transport: single-flight loop, trimToPngSignature, PNG IHDR read, adaptive delay. Reusable verbatim if executor speaks screenshot tooling. |
| rectify-kmp | data-device-api/src/commonMain/dev/rectify/io/source/datasource/DeviceCommandExecutor.kt | DeviceCommandExecutor | Interface: run(DeviceId, DeviceCommand): ByteArray. Neutral; iOS impls will map DeviceCommand.Screenshot/UiAutomatorDump to xcrun/idb equivalents or throw. |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| Mirror view with iOS platform selected | rectify-screens.html#err-sim | raw/rectify-screens.html | Error card design: simulator-not-booted state with 'Boot simulator' primary action and 'Choose another' secondary. Already drawn but deferred. |
| Mirror view with iOS device selected | rectify-screens.html#err-trust | raw/rectify-screens.html | Error card design: device-not-trusted state with "I've tapped Trust — retry" primary action and 'Use simulator' secondary. Already drawn but deferred. |

### ticketRefs

| system | id | url | status |
| --- | --- | --- | --- |
| prd | F-10 | rectify-kmp/.pipeline/prd.json#F-10 | ready-for-sdd |

### existingSolutions

| where | summary | link |
| --- | --- | --- |
| this-project | Android device adapter (F-2, F-3) — DeviceRepository + five datasource interfaces + closed DeviceCommand/HostCommand sets. Neutral measure/verify/diff toolkit (F-9). Shell reuses F-1's RectifyWindow. Annotation surface F-4/F-5. All ready to be wrapped by platform abstraction. | data-device-api/, data-repository-impl/, data-device-impl/src/desktopMain/ |
| this-project | Error card projection (MirrorErrorCard, MirrorErrorStage, DiscoveryErrorProjection) — already structured for platform-specific cards but currently platform-blind. | presentation-feature-mirror/.../core/discovery/DiscoveryErrorProjection.kt |
| this-project | Device picker UI (DevicePickerPanel, MirrorRow, PickerPlatform) — already multi-device aware, NFR-8 keyboard nav already wired, only picker-card enabled state needs Android→platform check. | presentation-feature-mirror/src/commonMain/.../picker/ |

### glossary

| term | definition |
| --- | --- |
| idb | Facebook's iOS Development Bridge; command-line tool for iOS simulator/device control. Installed on this host at ~/.local/bin/idb. |
| simctl | xcrun simctl (Xcode's Simulator Control); command-line tool for Simulator control. Part of Xcode 26.3 on this host. |
| idb_companion | Daemon backing idb; must be running for idb to communicate with physical devices. Present on this host. |
| Platform abstraction | Dispatch seam at repository or datasource level that selects Android (adb) vs iOS (idb/simctl) implementations based on selected PlatformModel. Currently missing; F-10 must extract it. |
| Device-not-trusted | iOS security state: physical device has not been tapped to trust the developer certificate. User must tap Trust on the device, then retry. AC-31 error card state. |
| Simulator-not-booted | iOS simulator is installed but not running. User can boot it on-demand via error card action, or choose another device. AC-31 error card state. |
| MirrorErrorKind | Closed enum of error states in device discovery: ToolMissing, NoDevice, PollFailed, MultipleDevices. Will add SimulatorNotBooted, DeviceNotTrusted. |
| DiscoveryModel | State model for device discovery: currently Found(physical, emulators) for Android. Must add iOS variants for simulator/device states and error conditions. |
| DeviceCommand | Closed enum of device-side commands: Screenshot, UiAutomatorDump, etc. KDoc says widening must be visible API change. iOS will map to simctl/idb equivalents or reuse existing. |
| HostCommand | Closed enum of host-side commands for tool management: ListDevices, StartServer, KillServer, etc. KDoc says widening must be visible API change. iOS may need simctl-specific hosts. |
| ToolKind | Closed enum: currently Adb, Idb. Missing xcrun/simctl variant. KDoc says widening must be visible API change. |
| Qualified Koin binding | Koin DI binding with a `named()` qualifier, e.g. `single { Android impl } bind Interface with named('android')`. Currently F-10 has only unqualified bindings. |
| AX JSON | iOS Accessibility (AX) tree returned by `idb ui describe-all`; analogous to Android's uiautomator XML dump. Needs parsing to HierarchyNodeModel. |

### attachments


### ambiguities

- SCOPE: Platform abstraction extraction — qualified Koin bindings + threading `platform: PlatformModel` through DeviceRepository methods (bigger diff, cleaner API shape), versus a dispatching decorator over `StateFlow<PlatformModel>` (smaller diff, but less explicit). Recon costs both; Clarify should nail which one.
- SHADOWING BUG: MirrorStageContent computes errorCard() BEFORE the stage when, and errorCard() is platform-blind while the Android poll keeps running. Picking iOS on a no-adb machine shows Android 'no device' card instead of iOS error. Any iOS error state must enter through errorCard() projection or be shadowed. How to solve: make errorCard() platform-aware, or inject DiscoveryModel variants that are already iOS-specific?
- MIRRORING MECHANISM: Poll xcrun simctl io <udid> screenshot --type=png / idb screenshot (reuses ScreencapMirrorTransport + ProcessRunner unchanged), versus idb video-stream (higher fps, but needs streaming ProcessRunner entry point + H.264/MJPEG decode that decodeToImageBitmap cannot do). Recon covers both; Clarify should decide.
- HIERARCHY RAWDUMP: idb ui describe-all returns AX JSON, not uiautomator XML. HierarchySnapshotModel/HierarchyNodeModel are neutral, measure+verify stack are neutral. But rawDump is documented as verbatim uiautomator stdout and hierarchyXml field is literally named XML. Today attachedHierarchyXml is Android-gated so iOS degrades to '' for free. Decide: carry AX JSON in v1, or ship iOS with no hierarchy and defer to v1.1?
- JSON PARSING: data-device-api has zero dependencies beyond :data-core and no serialization plugin. Repo precedent is hand-rolled parsers. Hand-roll an AX JSON reader, or add kotlinx-serialization-json (in catalog, unused there)?
- PHYSICAL DEVICE: AC-31's device-not-trusted half needs a real untrusted iPhone. This environment has none — expect it fake-driven via a test DiscoveryModel. Flag coverage honestly in test plan.
- CLOSED COMMAND SETS: DeviceCommand/HostCommand/ToolKind are deliberately closed with KDoc saying widening must be visible API change. No simctl/xcrun ToolKind exists. Widening these is the first breaking change; decision should be explicit.
- PRIORITY & PHASE: Spec is priority=could, phase=PH-2 (only non-must in this PRD). How far to take it: full parity (discovery + mirroring + hierarchy + error cards), or discovery + error cards + mirroring with hierarchy deferred to v1.1?

### dictation



### rawCacheDeltas
