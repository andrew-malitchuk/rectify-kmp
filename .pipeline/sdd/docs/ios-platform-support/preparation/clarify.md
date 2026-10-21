# ios-platform-support — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | done |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-19T01:59:03Z |
| updatedAt | 2026-10-19T01:59:03Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T01:59:03Z

## Provenance

### selfHash

19b420cc5a7c75b3fd98088e0c6f9bc1521b84b2796ca0d27c4e1aaa0a606779

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | de1f03b17a9b8d197b530b04d7c9ef96019e6fb2fdf4b6075231451e0b6d24f0 |

## Data

### overview

F-10 wires the iOS branch of the existing Android mirror/annotate/verify loop over idb + simctl. Recon shows the PRD's 'only the adapter is new' claim is half true: DeviceRepository has no platform parameter, Koin binds one unqualified instance per datasource interface, and adb is hardcoded into the argv builder and command sets. F-10 is therefore 'extract a platform-dispatch seam, then add the iOS actual' — scoped to discovery, mirroring, and the two AC-31 error cards; view-hierarchy capture stays deferred to v1.1 since it already degrades to an empty string for free today.

### problemStatement

Picking iOS in the platform switch currently just kills the session and shows a static 'deferred' string (MirrorStage.IosDeferred) — no discovery, no mirroring, and the two iOS error-card designs (simulator-not-booted, device-not-trusted) that are already drawn in the design file are unreachable. Worse, because MirrorStageContent computes errorCard() before the stage `when` and errorCard() is platform-blind while the Android poll keeps running adb, picking iOS on a machine with no Android device today shows the wrong (Android) error card — a shadowing bug that must be fixed before any iOS error state can ever render.

### purpose

Give the Mobile Developer persona (US-14) one tool across both platforms their app ships on, by extending the proven Android device-adapter pattern to iOS through idb/simctl, reusing F-1's shell, F-4/F-5's annotation surface, F-7's MCP handoff, and F-9's platform-neutral measure/verify stack unchanged.

### goals

- Extract a platform-dispatch seam (qualified Koin bindings + platform threaded through DeviceRepository) without changing observable Android behavior
- Implement iOS simulator + physical-device discovery and live mirroring via idb/simctl, reusing ScreencapMirrorTransport and ProcessRunner unchanged
- Fix the errorCard() shadowing bug so iOS error states are reachable at all
- Ship the two AC-31 error cards (simulator-not-booted, device-not-trusted) end to end
- Explicitly defer iOS view-hierarchy capture to v1.1 — zero cost today since it already degrades to an empty string

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-31 | Given iOS is selected as platform, when the simulator isn't booted, then an error card with 'Boot simulator' primary / 'Choose another' secondary renders; when the device isn't trusted, an error card with "I've tapped Trust — retry" primary / 'Use simulator' secondary renders. | could | test | true |
| AC-46 | Given iOS is selected and discovery runs, when no simulator is booted (measured on a real host via simctl/idb per ios-tooling-probe.md), then DiscoveryModel reflects the iOS-specific simulator-not-booted state and the Android 'no device' card does not render instead (shadowing fix verified). | must | test | true |
| AC-47 | Given iOS is selected and the error card shows 'Boot simulator', when the user clicks it, then the app invokes the simulator boot command for the selected simulator and discovery re-polls and surfaces it as selectable without an app restart. | must | test | true |
| AC-48 | Given iOS is selected and a simulator or trusted physical device is available, when mirroring starts, then live frames render through the generalized DeviceCommandExecutor/ScreencapMirrorTransport path with the same MirrorFrame contract (PNG bytes, width/height, fps stats) Android uses today. | must | demo | true |
| AC-49 | Given the platform-dispatch extraction (threading `platform` through DeviceRepository + qualified Koin bindings) lands, when the existing Android device-discovery/mirroring/error-card test suite runs, then every existing Android test still passes with no behavior change. | must | test | true |
| AC-50 | Given iOS is selected, when a view-hierarchy dump is requested, then the app returns the same empty hierarchy (rawDump "") it returns today, confirming hierarchy capture is deliberately deferred with no regression and no new wire-field rename. | could | inspection | true |

### functionalRequirements

| id | text |
| --- | --- |
| FR-25 | (v1.1+) Support iOS simulator/device mirroring via idb+simctl, including simulator-not-booted and device-not-trusted error-card states. |
| FR-48 | Extract a platform-dispatch seam: thread `platform: PlatformModel` through DeviceRepository's methods (sourced from MirrorViewModel's existing state.platform) and register qualified Koin bindings (named("android") / named("ios")) for each of the five datasource interfaces in DeviceSourcesProvider.desktop.kt. |
| FR-49 | Add a `ToolKind.Simctl` (xcrun-based) member and widen `DeviceCommand`/`HostCommand` with new iOS-shaped variants (screenshot, list simulators/devices, boot simulator) as an explicit, documented, visible API change; the sets otherwise stay closed. |
| FR-50 | Generalize `buildDeviceArgv` so the device-selector flag is chosen per `ToolKind` (adb `-s`, simctl `-u`, idb `--udid`) while preserving the NFR-5 invariant that the argv always embeds the one explicitly selected, connected device id. |
| FR-51 | Implement iOS discovery polling (booted/unbooted simulators and connected physical devices via idb/simctl) producing new DiscoveryModel variants for simulator-not-booted and device-not-trusted, replacing the current static MirrorStage.IosDeferred stage when platform == Ios. |
| FR-52 | Fix the errorCard() shadowing bug: the discovery poll stops issuing adb once platform == Ios (a direct consequence of FR-48's platform threading), and DiscoveryErrorProjection/errorCard() render the new iOS-specific DiscoveryModel variants instead of being shadowed by stale Android state. |
| FR-53 | Add `MirrorErrorKind.SimulatorNotBooted` and `MirrorErrorKind.DeviceNotTrusted` with their `MirrorErrorCard` content, new intents (BootSimulator, UseSimulator, RetryTrust, ChooseAnother) and MirrorViewModel wiring, satisfying AC-31. |
| FR-54 | Implement iOS live mirroring by generalizing DeviceCommandExecutor to dispatch iOS screenshot commands (`xcrun simctl io <udid> screenshot --type=png -` for simulators, `idb screenshot` for physical devices) and reusing `ScreencapMirrorTransport` unchanged. |
| FR-55 | Extend the device picker: reuse `MirrorRowGroup{Physical,Emulator}` and `MirrorRowKind.Boot` unchanged for iOS rows; replace `platformCards()`'s hardcoded `enabled = platform == Android`; relabel `mirror_group_emulator` to a platform-neutral string. |
| FR-56 | Add `Unsupported*` stand-ins for any newly introduced iOS-facing datasource surface so the Android/iOS app Koin graphs still resolve, per the existing iosMain stand-in convention (e.g. UnsupportedAdbHostSource). |
| FR-57 | Leave iOS view-hierarchy capture as today's no-op: `attachedHierarchyXml` stays Android-gated and `HierarchySnapshotModel.rawDump` remains "" on iOS — explicitly deferred to v1.1 per the scope decision below. |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |
| NFR-18 | testability | AC-31's simulator-not-booted half must be verified against the real simctl/idb toolchain on a host with no booted simulator (measured feasible per ios-tooling-probe.md); the device-not-trusted half is permitted to be fake-driven via a constructed DiscoveryModel fixture — documented as a waiver since no untrusted physical iPhone is available in this environment. |
| NFR-19 | compatibility | All idb/simctl process invocation and iOS datasource implementations live exclusively under data-device-impl/src/desktopMain (per platform-coverage.md); commonMain stays free of java.* and process APIs, and :ios-application is untouched since F-10 is a desktop-host capability. |
| NFR-20 | reliability | The platform-dispatch extraction (FR-48) must not change observable Android behavior — identical DiscoveryModel outputs, error cards, and mirroring characteristics — verified by the existing Android test suite remaining green (AC-49). |

### outOfScope

- iOS view-hierarchy capture / AX JSON parsing and the rawDump/hierarchyXml rename it would require — deferred to v1.1 (FR-57)
- idb video-stream high-fps mirroring and the H.264/MJPEG decode it needs — screenshot polling only in v1
- :ios-application (the on-device Rectify app) — F-10 is a desktop-host capability only, per platform-coverage.md
- Windows/Linux builds (already excluded by NFR-10)
- Adding kotlinx-serialization-json to data-device-api — moot while hierarchy capture is deferred
- Simulator lifecycle management beyond boot (create/delete/erase/reset simulators)

### constraints

| type | text |
| --- | --- |
| tech | iOS adapter classes are bound inside the existing desktopMain actual (provideDeviceSources()); no new expect/actual is introduced, per the repo's expect/actual-for-DI-registration-only convention. |
| tech | Widening ToolKind/DeviceCommand/HostCommand must be an explicit, documented, visible API change per their existing KDoc contract — no silently overloading an Android-shaped case with iOS semantics. |
| tech | buildDeviceArgv must keep enforcing the NFR-5 single-explicit-device invariant for every ToolKind, including the new Simctl kind. |
| business | Priority is could, phase PH-2, estimate L — the smallest scope that satisfies AC-31/SC-14 is deliberately preferred over the full goal-statement parity list. |
| time | Effort tier for this clarify pass is fast; implementation planning happens downstream. |

### assumptions

| id | text |
| --- | --- |
| AS-1 | A Mac host with Xcode command-line tools, simctl, idb, and idb_companion installed is the only supported dev/build environment for F-10, consistent with NFR-10's macOS-only scope. |
| AS-2 | idb's list-targets JSON output can reliably distinguish booted/shutdown simulators and trusted/untrusted physical devices; the exact field/error signal for 'untrusted' is confirmed during implementation, not finalized in this clarify pass. |
| AS-3 | No environment available to this project has a real untrusted iPhone; device-not-trusted coverage is accepted as fixture-driven per NFR-18 rather than blocking the feature. |
| AS-4 | Hierarchy capture on iOS is out of scope for this feature; the existing Android-gated attachedHierarchyXml behavior (empty string on iOS) is sufficient and requires no change (FR-57). |

### dependencies

| on | note |
| --- | --- |
| F-2/F-3 (Android device discovery & mirroring) | Merged; the datasource interfaces, ProcessRunner, and error-card projection this feature extends. |
| F-9 (Verify & re-diff loop) | Merged; the measure/verify/diff stack is already rect/pixel-neutral and needs no change for iOS. |
| Xcode command-line tools, idb, idb_companion | Must be present on the developer's Mac; probed present on this host (ios-tooling-probe.md). |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-14 | (v1.1+) iOS simulator not booted / device not trusted | error | ["Select iOS platform", "Simulator not booted -> error card offers Boot simulator", "Or device not trusted -> error card offers Trust-and-retry / Use simulator"] |
| SC-25 | iOS simulator discovery and mirroring happy path | happy | ["Select iOS platform", "A simulator is already booted and appears in the picker", "Select it", "Live frames render via simctl screenshot polling"] |
| SC-26 | iOS physical device discovery happy path | happy | ["Select iOS platform", "A trusted physical device appears in the picker via idb discovery", "Select it", "Live frames render via idb screenshot"] |
| SC-27 | iOS simulator-not-booted error recovers via Boot simulator | edge | ["Select iOS platform", "No simulator is booted (measured real state on this host)", "Error card renders Boot simulator / Choose another", "Click Boot simulator", "Discovery re-polls and the simulator becomes selectable"] |
| SC-28 | iOS device-not-trusted error (fixture-driven) | error | ["Select iOS platform", "Construct a DiscoveryModel.DeviceNotTrusted fixture (no real untrusted device available in this environment)", "Error card renders \"I've tapped Trust — retry\" / Use simulator", "Click Use simulator", "Picker filters to simulator rows"] |
| SC-29 | Android regression check after platform-dispatch extraction | happy | ["Select Android platform", "Run the existing discovery/mirror/error-card flows", "Confirm no behavior change versus the pre-F-10 baseline (AC-49)"] |

### successMetrics

- iOS screenshot-poll mirroring reaches the same ~10fps capture floor as Android (CaptureFloorMs=100) with no new tuning constants
- Zero regressions in the existing Android discovery/mirror/error-card test suite after the platform-dispatch extraction

### openQuestions

- Exact idb JSON field/error signal used to detect a physical device's 'untrusted' state — confirm against measured idb output during planning/implementation.
- Whether AX JSON hierarchy capture is worth the rawDump/hierarchyXml rename in a future v1.1, now that iOS mirroring/error-cards ship without it.
- Whether idb video-stream's higher fps is worth the streaming-ProcessRunner + H.264/MJPEG decode investment if 10fps screenshot polling proves insufficient in real usage.

### survey

| question | options | answer | source |
| --- | --- | --- | --- |
| F-10 scope — how far should this feature go? | ["(a) full parity including hierarchy", "(b) discovery + mirroring + error cards, hierarchy deferred", "(c) discovery + error cards only, no mirroring"] | (b) discovery + mirroring + error cards, hierarchy deferred | ai-proposed |
| Platform-dispatch seam shape | ["Qualified Koin bindings + platform threaded through DeviceRepository", "Dispatching decorator over StateFlow<PlatformModel>"] | Qualified Koin bindings + platform threaded through DeviceRepository | ai-proposed |
| Shadowing bug fix | ["errorCard() becomes platform-aware only", "poll stops running adb on iOS only", "both"] | both | ai-proposed |
| Mirroring mechanism | ["Screenshot polling (simctl io / idb screenshot)", "idb video-stream"] | Screenshot polling (simctl io / idb screenshot) | ai-proposed |
| Hierarchy format | ["Stay a no-op (defer to v1.1)", "Carry AX JSON now"] | Stay a no-op (defer to v1.1) | ai-proposed |
| JSON parsing dependency | ["Moot — no JSON parsing needed in v1", "Hand-roll a parser now for future use", "Add kotlinx-serialization-json now for future use"] | Moot — no JSON parsing needed in v1 | ai-proposed |
| Closed command set widening | ["Add ToolKind.Simctl + new iOS-shaped DeviceCommand/HostCommand variants; argv branches selector flag per ToolKind", "Reuse ToolKind.Idb for both idb and simctl invocations to avoid a new enum member", "Leave the sets Android-only and hardcode iOS tool invocation outside them"] | Add ToolKind.Simctl + new iOS-shaped DeviceCommand/HostCommand variants; argv branches selector flag per ToolKind | ai-proposed |
| Device picker grouping | ["Reuse Physical/Emulator groups, relabel mirror_group_emulator", "Add a new 'Simulator' row group"] | Reuse Physical/Emulator groups, relabel mirror_group_emulator | ai-proposed |
| AC-31 test coverage honesty | ["Simulator-not-booted: real measured state on this host. Device-not-trusted: fixture-driven DiscoveryModel, documented as a waiver.", "Fake-drive both halves for consistency", "Block the feature until a real untrusted iPhone is available"] | Simulator-not-booted: real measured state on this host. Device-not-trusted: fixture-driven DiscoveryModel, documented as a waiver. | ai-proposed |
| DiscoveryModel variant naming | ["SimulatorNotBooted / DeviceNotTrusted (unprefixed, since the iOS poll is the only producer)", "IosSimulatorNotBooted / IosDeviceNotTrusted (platform-prefixed)"] | SimulatorNotBooted / DeviceNotTrusted (unprefixed, since the iOS poll is the only producer) | ai-proposed |
