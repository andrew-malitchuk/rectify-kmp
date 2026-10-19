# ios-platform-support — candidates/candidate-1

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | candidates/candidate-1 |
| status | done |
| producedBy | sdd-build-plan-draft |
| schemaVersion | 1 |
| createdAt | 2026-10-19T02:25:36Z |
| updatedAt | 2026-10-19T02:25:36Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-19T02:25:36Z

## Provenance

### selfHash

9f1f6f64c52c3960dd3dd2d3ac90b786d41afd10bf1142cd7ed2b4dd3949f597

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 3557f6c6047c978f16b9e48604e4d7f3d911bbca0185436a0d3ca4d08214371a |

## Data

### meta

#### title

iOS Platform Support (F-10) — platform-dispatch seam + simctl/idb adapter

#### version

1

#### status

draft

### goal

Make PlatformModel.Ios a real, working branch of the existing Android mirror loop on the macOS desktop host: iOS simulator + physical-device discovery via xcrun simctl and idb, live screenshot-poll mirroring through the UNCHANGED ScreencapMirrorTransport, and the two already-drawn AC-31 error cards (simulator-not-booted, device-not-trusted) reachable for the first time. Hierarchy capture stays deferred to v1.1.

### problemStatement

Picking iOS today kills the session and shows a static 'deferred' string (MirrorStage.IosDeferred) — no discovery, no mirroring, and the two drawn iOS error-card designs are unreachable. Worse, MirrorStageContent computes errorCard() BEFORE the stage `when` (MirrorContent.kt:124-128), errorCard() is platform-blind, and the discovery poll keeps issuing adb while iOS is selected — so picking iOS on a machine with no Android device renders the ANDROID 'no device' card. That shadowing bug must be fixed on both halves before any iOS error state can ever render. Underneath, the PRD's claim that 'only the device-adapter layer is new' is half true: DeviceRepository has no platform parameter, Koin binds one unqualified instance per datasource interface, adb is hardcoded in four load-bearing places (buildDeviceArgv's positional `-s`, DeviceCommand's literal adb shell lines, DeviceCommandExecutorImpl's unconditional ToolKind.Adb + poll()'s unconditional adbHostSource, and the unqualified Koin bindings), and ToolKind/DeviceCommand/HostCommand are documented closed sets. F-10 is therefore 'extract a dispatch seam, then add the actual'.

### highLevelDescription

Two commits. COMMIT 1 is a pure refactor with ZERO iOS code: add `platform: PlatformModel` to exactly five DeviceRepository methods (observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats) and let it ripple through 5 use-case interfaces, 5 use-case impls, MirrorViewModel, MirrorFakes and the four data-repository-impl device test files. DeviceRepositoryImpl accepts the parameter and ignores it; `git diff --word-diff` shows only `platform` insertions. The existing Android suite green IS AC-49. COMMIT 2 adds iOS: ToolKind.Simctl + widened DeviceCommand/HostCommand, a generalized buildDeviceArgv with a STRUCTURAL NFR-5 check, two hand-rolled parsers (parseSimctlDevices / parseIdbTargets), one IosHostSource replacing the deleted probe-only IdbHostSource, an IosDeviceCommandExecutor that maps the REUSED DeviceCommand.Screencap onto `xcrun simctl io <udid> screenshot --type=png -` / `idb screenshot --udid`, qualified named("android")/named("ios") Koin bindings for DeviceCommandExecutor and MirrorTransport only, DeviceRepositoryImpl's exhaustive `when` dispatch with poll() split into pollAndroid()/pollIos(), two new DiscoveryModel variants, the shadowing fix on both halves, the two MirrorErrorKind members with exactly ONE new intent (BootSimulator(udid)), and the picker relabel. ScreencapMirrorTransport and ProcessRunner are edited ZERO times.

### background

F-2/F-3 shipped the Android device adapter with clean, already-neutral interfaces: DeviceCommandExecutor.run(DeviceId, DeviceCommand): ByteArray, MirrorTransport.frames(DeviceId): Flow<MirrorFrame> + stats, and a ScreencapMirrorTransport that depends only on DeviceCommandExecutor and never names adb. F-9 proved the whole measure/verify/diff stack below HierarchySnapshotModel is rect- and pixel-neutral. ProcessRunner is a single internal object with run()/spawnDetached() that knows nothing about adb. The missing piece is platform dispatch — which lives nowhere today. The iOS tooling probe (2026-10-19) measured this host: Xcode 26.3 (build 17C529) with runtimes iOS 17.5 and 18.1, idb at ~/.local/bin/idb, idb_companion at /opt/homebrew/bin/idb_companion, and NO simulator booted — which is the natural, free reproduction of AC-31's simulator-not-booted half.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/ios-platform-support/inputs/codebase-recon.md | inputs/codebase-recon.md |
| code | .pipeline/sdd/docs/ios-platform-support/inputs/ios-tooling-probe.md | inputs/ios-tooling-probe.md |
| code | .pipeline/sdd/docs/ios-platform-support/preparation/preparation.json | preparation/preparation.json |
| code | .pipeline/sdd/docs/ios-platform-support/preparation/brainstorm.json | preparation/brainstorm.json |
| code | .pipeline/sdd/docs/ios-platform-support/preparation/clarify.json | preparation/clarify.json |
| code | .pipeline/sdd/docs/registry/bugs.json | registry/bugs.json |
| code | .pipeline/sdd/docs/registry/components.json | registry/components.json |
| code | .pipeline/sdd/docs/registry/features.json | registry/features.json |

### scope

#### in

- COMMIT 1 — platform-dispatch extraction as a standalone pure refactor: `platform: PlatformModel` added to the five dispatching DeviceRepository methods (observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats) and threaded through 5 use-case interfaces, 5 use-case impls, MirrorViewModel, MirrorFakes and four data-repository-impl test files. Zero iOS code, zero new tests, reviewable as a `git diff --word-diff` of `platform` insertions.
- ToolKind.Simctl("xcrun", null); DeviceCommand.SimulatorBoot; HostCommand.{XcrunVersion, ListSimulators, IdbVersion, ListTargets} — each an explicit, KDoc-documented, visible API widening of a closed set.
- buildDeviceArgv(tool, toolPath, device, command): per-ToolKind selector branch (adb `-s` prefix / simctl positional-after-subcommand / idb `--udid` suffix) followed by `check(argv.count { it == device.value } == 1)`, backed by an exhaustive ToolKind × DeviceCommand sweep test.
- Hand-rolled parseSimctlDevices over `xcrun simctl list devices available` plain text and parseIdbTargets over `idb list-targets` pipe-split rows, with fixtures captured from THIS host (Xcode 26.3). No new dependency.
- IosHostSource (probeSimctl/probeIdb/listSimulators/listDevices/bootSimulator) REPLACING the probe-only IdbHostSource, which is deleted along with IdbHostSourceImpl and UnsupportedIdbHostSource.
- IosDeviceCommandExecutor dispatching the REUSED DeviceCommand.Screencap to simctl/idb; ScreencapMirrorTransport registered twice and edited zero times; ProcessRunner edited zero times.
- Qualified named("android")/named("ios") Koin bindings for DeviceCommandExecutor and MirrorTransport ONLY, in the single existing seam DeviceSourcesProvider.desktop.kt, using explicit `single { get(named(...)) }` per koin-di.md.
- DeviceRepositoryImpl: exhaustive `when (platform)` selection, poll() split into pollAndroid() (moved verbatim) and pollIos() (new), and the PollFailed catch's hardcoded ToolKind.Adb.binaryName fixed to name the platform's own tool.
- DiscoveryModel.SimulatorNotBooted and DiscoveryModel.DeviceNotTrusted (unprefixed, SUCCESS values, never Failure).
- Shadowing fix on BOTH halves: selectPlatform() cancels and re-collects the discovery job and resets discovery = Detecting / rows = emptyList(); errorCard() gains a platform guard returning null for Android-only variants when platform == Ios.
- Deletion of MirrorStage.IosDeferred, its `platform == Ios -> IosDeferred` derivation branch, its render branch, and the mirror_ios_deferred string.
- MirrorErrorKind.SimulatorNotBooted / DeviceNotTrusted with their MirrorErrorCard content and ~9 new strings; exactly ONE new intent, MirrorIntent.BootSimulator(udid); 'Choose another' and 'Use simulator' reuse OpenPicker, "I've tapped Trust — retry" reuses RetryDiscovery.
- EmulatorModel.runningDeviceId carries the udid even for a SHUT-DOWN simulator (KDoc widened, no new field) because simulator names are not unique across runtimes.
- Picker: platformCards()'s hardcoded `enabled = platform == Android` replaced; mirror_group_emulator relabelled to a platform-neutral value; MirrorRowGroup{Physical,Emulator} and MirrorRowKind.Boot reused unchanged.
- Unsupported* stand-ins: UnsupportedIosHostSource added; the existing UnsupportedDeviceCommandExecutor / UnsupportedMirrorTransport registered under BOTH qualifiers so the Android and iOS app Koin graphs still resolve.
- observeMirrorStats gains a simctlVersion field alongside adbVersion so the iOS status line is not blank.

#### out

- iOS view-hierarchy capture / AX JSON parsing and the rawDump/hierarchyXml rename it would require — deferred to v1.1 (FR-57). iOS already degrades to "" for free via the Android-gated attachedHierarchyXml.
- idb video-stream high-fps mirroring and the H.264/MJPEG decode it needs — it would require a second ProcessRunner entry point (stream(argv): Flow<ByteArray>) and a second transport, destroying the 'ScreencapMirrorTransport unchanged' saving. Screenshot polling only in v1.
- :ios-application (the on-device Rectify app, an Xcode project not a Gradle module) — F-10 is a desktop-host capability per platform-coverage.md. Zero files touched there.
- :compose-application — nothing testable may land there (it has no test wiring by design). RectifyWindow.kt's PLATFORM_SOURCES already lists iOS; no change needed.
- Windows/Linux builds (NFR-10).
- Adding kotlinx-serialization-json to data-device-api — moot while hierarchy capture is deferred; `-j` JSON is the documented escape hatch, not this spec's path.
- Simulator lifecycle beyond boot (create/delete/erase/reset), and auto-starting idb_companion — that is somebody else's daemon.
- Renaming bootEmulator to bootTarget (6 files for zero behavioural value; KDoc carries the truth instead).
- Platform-specific picker group wording ('Emulators' / 'Simulators') — needs a new DevicePickerPanel parameter for a cosmetic win; one neutral string ships instead.
- A Compose UI-test harness. None exists in the repo; adopting one is its own infrastructure decision.

### assumptions

- AS-1 — A Mac host with Xcode command-line tools, simctl, idb and idb_companion installed is the only supported dev/build environment for F-10, consistent with NFR-10's macOS-only scope.
- AS-2 — idb's list-targets output can distinguish booted/shutdown simulators and trusted/untrusted physical devices; the exact field/error signal for 'untrusted' is confirmed during implementation, contained in ONE named token set inside parseIdbTargets so it is correctable from one place.
- AS-3 — No environment available to this project has a real untrusted iPhone; device-not-trusted coverage is accepted as fixture-driven per NFR-18 rather than blocking the feature.
- AS-4 — Hierarchy capture on iOS is out of scope; the existing Android-gated attachedHierarchyXml behaviour (empty string on iOS) is sufficient and requires no change (FR-57).
- PLAN-AS-1 — Preparation describes commit 1 as 'qualified Koin bindings + platform threaded through'. Those cannot both be zero-iOS-code: a named("ios") binding needs an iOS implementation to bind. This plan resolves it by putting ONLY the signature threading in commit 1 (which is what makes the word-diff claim literally true) and the qualifier registration in commit 2, where its sibling exists. Recorded as a deliberate deviation, not an omission.
- PLAN-AS-2 — Preparation's '~45 files' estimate conflates the refactor ripple with the iOS work. Recounted against the real tree: the pure refactor is 19 files; the iOS commit is 47 file-level changes. Total ≈58 distinct files. The ~45 figure is not wrong in spirit; it is just not the refactor's number.
- PLAN-AS-3 — A measured fixture of this host's `xcrun simctl list devices available` output does not exist yet. Capturing it is cheap, available now, and is a hard prerequisite for the parser tests (step 5).

### constraints

| type | text |
| --- | --- |
| tech | iOS adapter classes are bound inside the existing desktopMain actual provideDeviceSources(); no new expect/actual is introduced. The repo's convention is expect/actual for the DI REGISTRATION FUNCTION only — plain interface + Koin binding for behaviour. There are no expect class datasources. |
| tech | ToolKind/DeviceCommand/HostCommand widening must be an explicit, documented, visible API change per their existing KDoc contract. Reusing DeviceCommand.Screencap for iOS is honoured by REWRITING its KDoc to the platform-neutral meaning it already has ('capture the screen as an encoded PNG on stdout'), not by silently overloading an Android-shaped case. |
| tech | buildDeviceArgv must keep enforcing the NFR-5 single-explicit-device invariant for every ToolKind including Simctl. The `check()` throwing IllegalStateException from a commonMain pure function is correct here and is NOT a mappers-never-throw violation: a selector-less argv is a programming error, unreachable from user data. |
| tech | platform-coverage.md — ALL idb/simctl process invocation and every iOS datasource implementation lives exclusively under data-device-impl/src/desktopMain/. data-device-impl's commonMain names no java.* and no process API. Desktop here means the current-OS JVM target only; there is no macosMain. |
| tech | error-handling.md — a missing tool or an empty device list is a SUCCESS value, never an exception. The iOS cards ride the MirrorErrorCard projection off DiscoveryModel, not Failure. Do NOT copy the recorded repo-wide gap where ShowError(messageId) handlers ignore messageId and show a hardcoded 'Error'. |
| tech | The five external-process conventions hold unchanged: ONE process-starting site (ProcessRunner), closed DeviceCommand/HostCommand sets, ONE argv construction point enforcing the device selector, every budget named in MirrorTuning (no new tuning constants), and TIMEOUT_REASON_PREFIX shared producer↔consumer. |
| tech | koin-di.md — one module per Gradle module, load order significant; provideDeviceSources() stays the single registration seam; qualified registrations use explicit `single<T>(named(...)) { ... get(named(...)) }` rather than singleOf. |
| tech | package-layout.md + explicitApi() — core/ is internal-only, source/ is public with one subfolder per semantic concern, explicit visibility and explicit return types everywhere. |
| tech | testing.md — the desktop target is the one that runs, but a cross-target build must pass before done: commonTest compiles for iosArm64 and Kotlin/Native rejects a backtick test name containing a comma or a colon (an apostrophe is fine, measured). Nothing testable may land in :compose-application, which has no test wiring by design. |
| tech | mappers.md — Mapper<I,O> is a fun interface and a mapper NEVER throws; unknown values get a safe default in toModel. Both new parsers and SimulatorResourceMapper obey this: an unparseable line is skipped, never fatal. |
| business | Priority is `could`, phase PH-2, estimate L. The smallest scope satisfying AC-31/SC-14 is deliberately preferred over the full goal-statement parity list. |
| time | Effort tier fast; single drafter, no judge. The two-commit sequencing is the risk control that replaces a longer review cycle. |

### dependencies

| on | note |
| --- | --- |
| F-2/F-3 (Android device discovery & mirroring) | Merged. Supplies the five datasource interfaces, ProcessRunner, ToolResolver/probeCandidates, ScreencapMirrorTransport, DeviceArgv, the hand-rolled-parser precedent and the DiscoveryModel contract this feature extends. registry: features.json#android-device-discovery-mirroring. |
| F-6 (Error handling & recovery) | Merged. Supplies MirrorErrorKind/MirrorErrorCard, the pure DiscoveryErrorProjection (errorCard/indicatorDot/dropBanner), MirrorErrorStage and the ErrorCard organism — the exact surfaces the two iOS cards plug into. registry: features.json#error-handling-and-recovery. |
| F-9 (Verify & re-diff loop) | Merged. The measure/verify/diff stack is already rect/pixel-neutral and needs zero change for iOS — it is the reason deleting MirrorStage.IosDeferred is safe. |
| Xcode command-line tools (xcrun/simctl), idb, idb_companion | Must be present on the developer's Mac. Probed present on this host: Xcode 26.3 build 17C529, runtimes iOS 17.5 and 18.1, idb at ~/.local/bin/idb, idb_companion at /opt/homebrew/bin/idb_companion. Whether idb_companion is RUNNING and whether idb is authorised against a physical device are separate, unprobed questions. |
| A captured fixture of this host's `xcrun simctl list devices available` and `idb list-targets` output | Prerequisite for step 5's parser tests. Cheap and available now; capture it before writing the parsers so the tests pin real output rather than imagined output. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| AC-49 regression: the five-signature change ripples through ~19 files before any iOS behaviour exists. A mechanical edit at that scale is where a quiet behaviour change hides. | med | high | Land it as its own commit with ZERO iOS code and ZERO new tests. The only acceptable outcome is the existing Android suite green with DeviceRepositoryImpl's bodies untouched (commit 1 does not even split poll() — that happens in commit 2, where pollAndroid() is a verbatim MOVE). Review as a pure refactor; `git diff --word-diff` must show only `platform` insertions. |
| `simctl list devices` plain-text format drifts with an Xcode release and parseSimctlDevices silently returns an empty list, rendering as 'no simulators' rather than a visible failure. | med | med | The parser never throws (repo convention), but pollIos() distinguishes 'command succeeded, zero rows parsed, NON-EMPTY stdout' and emits PollFailed(tool = "xcrun", reason = <stdout head>) instead of SimulatorNotBooted. That is the same NoDeviceFound-vs-PollFailed split DiscoveryModel already makes, and it makes drift visible. `-j` JSON is the documented escape hatch. |
| `idb list-targets` hangs or fails without a running idb_companion, stalling the whole iOS poll once per interval. | high | med | Already budgeted by MirrorTuning.HostCommandTimeoutMs (3s). Additionally the idb call is wrapped so its failure yields an EMPTY physical list and never aborts the simctl half — simctl's simulator answer always survives. Deliberately no companion auto-start. |
| Generalising buildDeviceArgv weakens NFR-5 — the security invariant this module exists to make unrepresentable. | low | high | This is the opposite of the usual 'we generalised and weakened an invariant' story and the plan says so explicitly. Today's guarantee is POSITIONAL (`listOf(toolPath, "-s", device.value) + command.arguments()`), enforced only by the shape of one line, and nothing stops a future arguments() branch appending a second target. After this spec it is STRUCTURAL: `check(argv.count { it == device.value } == 1)` plus an exhaustive ToolKind × DeviceCommand sweep asserting (a) argv[0] == toolPath, (b) the id appears exactly once, (c) no element matches the connect/-H/-P denylist. NFR-5 ends up STRONGER than before — provable by a test rather than by reading one line. |
| Deleting MirrorStage.IosDeferred routes iOS through the full stage machine (Picker, Mirror, Frozen, Verify, NoSignal), exposing every stage to a platform none were written against. | med | med | That exposure IS the feature, and the stack below HierarchySnapshotModel is already rect/pixel-neutral (recon §6). The one iOS-shaped hole is hierarchy, which is Android-gated and degrades to "" — made an ASSERTED behaviour by AC-50's inspection step plus a test that fails if iOS ever starts returning nodes, rather than trusting the gate. |
| iOS mirroring is materially slower than ~10fps and the feature ships feeling broken. `simctl io screenshot` plausibly costs 200–400 ms. | med | low | Measure once a simulator is booted and record the NUMBER in the verification table, the way CaptureFloorMs's waiver is recorded. Do NOT add an iOS tuning constant to paper over it. The adaptive single-flight loop degrades gracefully and TransportStats reports the truth. idb video-stream is the named upgrade path and is already out of scope. |
| Scope creep back into hierarchy capture, because `idb ui describe-all` looks tantalisingly close once the adapter exists. | med | med | FR-57 and AC-50 make 'iOS returns an empty hierarchy' an asserted behaviour, not an omission. dumpHierarchy(Ios, …) short-circuits to HierarchySnapshotModel.Empty IN THE REPOSITORY without spawning a process and without throwing. A test that fails the moment iOS returns nodes is the cheapest guard. |
| AS-2 unresolved: nobody has measured what `idb list-targets` prints for an untrusted physical iPhone, so DeviceNotTrusted's detection signal is a guess until measured. | med | med | Contain, do not guess. One named token set inside parseIdbTargets plus a fallback classifying a listed-but-unusable physical target as untrusted. Correctable from one place after a real measurement; the fixture test pins whatever is decided. |
| No untrusted physical iPhone exists here (AS-3), and idb against a physical device has never been exercised on this host. AC-31's device-not-trusted half and the whole SC-26 physical path ship fixture-driven / code-review-verified only. | high | med | Write the waiver EXPLICITLY into the verification table rather than implying it; do not block the feature on it (NFR-18). AC-31's two halves are recorded separately so the simulator-not-booted measurement is never read as proving both. |
| The qualified Koin bindings fail to resolve when the app runs on Android or iOS HARDWARE (not desktop) because UnsupportedDeviceSources does not register the qualifiers. This is not a desktop test failure — it is a startup crash on a phone. | med | high | FR-56: register the existing UnsupportedDeviceCommandExecutor / UnsupportedMirrorTransport under BOTH named("android") and named("ios"), add UnsupportedIosHostSource, remove UnsupportedIdbHostSource. Guard it with a Koin-graph resolution test in data-device-impl's desktopTest (DeviceSourcesModuleTest already exists for exactly this) and a cross-target build. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-25 | (v1.1+) Support iOS simulator/device mirroring via idb+simctl, including simulator-not-booted and device-not-trusted error-card states. |
| FR-48 | Extract a platform-dispatch seam: thread `platform: PlatformModel` through DeviceRepository's methods (sourced from MirrorViewModel's existing state.platform) and register qualified Koin bindings (named("android") / named("ios")) for each of the five datasource interfaces in DeviceSourcesProvider.desktop.kt. PLAN REFINEMENT: `platform` lands on exactly the FIVE dispatching methods (observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats); detectTools/refreshDiscovery/restartAdbServer/setAdbPath are deliberately unchanged. Qualifiers are registered for DeviceCommandExecutor and MirrorTransport ONLY — AdbHostSource, EmulatorSource and IosHostSource have one implementation each and stay unqualified. |
| FR-49 | Add a `ToolKind.Simctl` (xcrun-based) member and widen `DeviceCommand`/`HostCommand` with new iOS-shaped variants (screenshot, list simulators/devices, boot simulator) as an explicit, documented, visible API change; the sets otherwise stay closed. PLAN REFINEMENT: screenshot REUSES DeviceCommand.Screencap (its KDoc rewritten platform-neutral); the genuinely new members are DeviceCommand.SimulatorBoot and HostCommand.{XcrunVersion, ListSimulators, IdbVersion, ListTargets}. |
| FR-50 | Generalize `buildDeviceArgv` so the device-selector flag is chosen per `ToolKind` (adb `-s`, simctl `-u`, idb `--udid`) while preserving the NFR-5 invariant that the argv always embeds the one explicitly selected, connected device id. PLAN REFINEMENT: simctl's selector is POSITIONAL-AFTER-SUBCOMMAND (`simctl io <udid> screenshot …`), not `-u`; the invariant is re-expressed structurally rather than positionally. |
| FR-51 | Implement iOS discovery polling (booted/unbooted simulators and connected physical devices via idb/simctl) producing new DiscoveryModel variants for simulator-not-booted and device-not-trusted, replacing the current static MirrorStage.IosDeferred stage when platform == Ios. |
| FR-52 | Fix the errorCard() shadowing bug: the discovery poll stops issuing adb once platform == Ios (a direct consequence of FR-48's platform threading), and DiscoveryErrorProjection/errorCard() render the new iOS-specific DiscoveryModel variants instead of being shadowed by stale Android state. |
| FR-53 | Add `MirrorErrorKind.SimulatorNotBooted` and `MirrorErrorKind.DeviceNotTrusted` with their `MirrorErrorCard` content, new intents (BootSimulator, UseSimulator, RetryTrust, ChooseAnother) and MirrorViewModel wiring, satisfying AC-31. CONFIRMED BY SURVEY: only `MirrorIntent.BootSimulator(udid)` is genuinely new — 'Choose another' and 'Use simulator' both map to the existing `MirrorIntent.OpenPicker`, and "I've tapped Trust — retry" maps to the existing `MirrorIntent.RetryDiscovery` (which already cuts the poll interval short via refreshDiscovery). Implement ONE new intent plus two reused ones, not four distinct intent types. |
| FR-54 | Implement iOS live mirroring by generalizing DeviceCommandExecutor to dispatch iOS screenshot commands (`xcrun simctl io <udid> screenshot --type=png -` for simulators, `idb screenshot` for physical devices) and reusing `ScreencapMirrorTransport` unchanged. |
| FR-55 | Extend the device picker: reuse `MirrorRowGroup{Physical,Emulator}` and `MirrorRowKind.Boot` unchanged for iOS rows; replace `platformCards()`'s hardcoded `enabled = platform == Android`; relabel `mirror_group_emulator` to a platform-neutral string. |
| FR-56 | Add `Unsupported*` stand-ins for any newly introduced iOS-facing datasource surface so the Android/iOS app Koin graphs still resolve, per the existing iosMain stand-in convention (e.g. UnsupportedAdbHostSource). |
| FR-57 | Leave iOS view-hierarchy capture as today's no-op: `attachedHierarchyXml` stays Android-gated and `HierarchySnapshotModel.rawDump` remains "" on iOS — explicitly deferred to v1.1 per the scope decision below. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. STRENGTHENED by this spec: today's guarantee is POSITIONAL (`listOf(toolPath, "-s", device.value) + command.arguments()`), enforced only by the shape of one line. After this spec it is STRUCTURAL — buildDeviceArgv builds the per-tool argv in a `when (tool)` (adb `-s` prefix / simctl positional-after-subcommand / idb `--udid` suffix) and then asserts `check(argv.count { it == device.value } == 1)` before returning, backed by an exhaustive ToolKind × DeviceCommand sweep test. This is the opposite of the usual 'we generalised and weakened an invariant' story — recorded explicitly, by instruction. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). FREE for iOS: pickerKeyAction / nextSelectableIndex / the single onPreviewKeyEvent + FocusRequester path know nothing about adb and are platform-blind already. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. FREE for iOS: the two new cards render through the existing ErrorCard organism with existing tokens; covered by @Preview pairs in light and dark, as every other card is. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |
| NFR-18 | testability | AC-31's simulator-not-booted half must be verified against the real simctl/idb toolchain on a host with no booted simulator (measured feasible per ios-tooling-probe.md: no simulator booted, simctl and idb installed); the device-not-trusted half is permitted to be fake-driven via a constructed DiscoveryModel fixture — documented as a waiver since no untrusted physical iPhone is available in this environment. |
| NFR-19 | compatibility | All idb/simctl process invocation and iOS datasource implementations live exclusively under data-device-impl/src/desktopMain (per platform-coverage.md); commonMain stays free of java.* and process APIs, and :ios-application is untouched since F-10 is a desktop-host capability. |
| NFR-20 | reliability | The platform-dispatch extraction (FR-48) must not change observable Android behavior — identical DiscoveryModel outputs, error cards, and mirroring characteristics — verified by the existing Android test suite remaining green (AC-49). |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-31 | Given iOS is selected as platform, when the simulator isn't booted, then an error card with 'Boot simulator' primary / 'Choose another' secondary renders; when the device isn't trusted, an error card with "I've tapped Trust — retry" primary / 'Use simulator' secondary renders. COVERAGE IS SPLIT AND MUST BE READ AS SPLIT: (a) simulator-not-booted is REAL and MEASURED on this host (no simulator booted at probe time; simctl and idb installed) — the projection assertion is pure commonTest and the underlying state is produced from real `xcrun simctl list devices available` output in the guarded desktopTest; (b) device-not-trusted is FIXTURE-DRIVEN and WAIVED per NFR-18/AS-3 — no untrusted physical iPhone exists here, so a constructed DiscoveryModel.DeviceNotTrusted drives the projection and nothing about real idb trust behaviour is proven. Do not let (a)'s measurement be read as covering (b). | must | test |
| AC-46 | Given iOS is selected and discovery runs, when no simulator is booted (measured on a real host via simctl/idb per ios-tooling-probe.md), then DiscoveryModel reflects the iOS-specific simulator-not-booted state and the Android 'no device' card does not render instead (shadowing fix verified). | must | test |
| AC-47 | Given iOS is selected and the error card shows 'Boot simulator', when the user clicks it, then the app invokes the simulator boot command for the selected simulator and discovery re-polls and surfaces it as selectable without an app restart. | must | test |
| AC-48 | Given iOS is selected and a simulator or trusted physical device is available, when mirroring starts, then live frames render through the generalized DeviceCommandExecutor/ScreencapMirrorTransport path with the same MirrorFrame contract (PNG bytes, width/height, fps stats) Android uses today. | must | demo |
| AC-49 | Given the platform-dispatch extraction (threading `platform` through DeviceRepository + qualified Koin bindings) lands, when the existing Android device-discovery/mirroring/error-card test suite runs, then every existing Android test still passes with no behavior change. | must | test |
| AC-50 | Given iOS is selected, when a view-hierarchy dump is requested, then the app returns the same empty hierarchy (rawDump "") it returns today, confirming hierarchy capture is deliberately deferred with no regression and no new wire-field rename. | could | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-14 | (v1.1+) iOS simulator not booted / device not trusted | error | ["Select iOS platform", "Simulator not booted -> error card offers Boot simulator", "Or device not trusted -> error card offers Trust-and-retry / Use simulator"] |
| SC-25 | iOS simulator discovery and mirroring happy path | happy | ["Select iOS platform", "A simulator is already booted and appears in the picker", "Select it", "Live frames render via simctl screenshot polling"] |
| SC-26 | iOS physical device discovery happy path | happy | ["Select iOS platform", "A trusted physical device appears in the picker via idb discovery", "Select it", "Live frames render via idb screenshot"] |
| SC-27 | iOS simulator-not-booted error recovers via Boot simulator | edge | ["Select iOS platform", "No simulator is booted (measured real state on this host)", "Error card renders Boot simulator / Choose another", "Click Boot simulator", "Discovery re-polls and the simulator becomes selectable"] |
| SC-28 | iOS device-not-trusted error (fixture-driven) | error | ["Select iOS platform", "Construct a DiscoveryModel.DeviceNotTrusted fixture (no real untrusted device available in this environment)", "Error card renders \"I've tapped Trust — retry\" / Use simulator", "Click Use simulator", "Picker filters to simulator rows"] |
| SC-29 | Android regression check after platform-dispatch extraction | happy | ["Select Android platform", "Run the existing discovery/mirror/error-card flows", "Confirm no behavior change versus the pre-F-10 baseline (AC-49)"] |

### definitionOfReady

- Preparation is signed off with readyForPlan: true and all FR/NFR/AC/SC/AS ids frozen.
- codebase-recon.md is accepted as ground truth for file paths, symbols, the four adb hardcoding sites and the binding rules — no re-derivation.
- ios-tooling-probe.md confirms xcrun/simctl (Xcode 26.3), idb and idb_companion present and NO simulator booted on the build host.
- A real fixture of this host's `xcrun simctl list devices available` and `idb list-targets` stdout has been captured and committed under data-device-api/src/commonTest (PLAN-AS-3).
- The AC-31 coverage split (real simulator-not-booted vs fixture-driven, waived device-not-trusted) is agreed in writing before implementation, not negotiated at verification time.
- Agreement that commit 1 ships with zero iOS code and zero new tests (PLAN-AS-1).

### definitionOfDone

- Commit 1 merged as a standalone pure refactor: `git diff --word-diff` shows only `platform` insertions across the 19 files, no logic change, no new test.
- The pre-existing Android suite is green after commit 1 AND after commit 2 — AC-49/NFR-20.
- pollAndroid() is a verbatim move of today's poll() body (reviewable as a move, not a rewrite).
- `./gradlew :data-device-api:desktopTest :data-device-impl:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest :domain-usecase-impl:desktopTest` green.
- A cross-target build passes (`:data-device-api:build :data-repository-impl:build :presentation-feature-mirror:build`) — commonTest compiles for iosArm64 and no new backtick test name contains a comma or a colon. Use `--max-workers=1` for the Kotlin/Native link step (see knownRisksFromQA PP-TD-8).
- The exhaustive ToolKind × DeviceCommand argv sweep test passes, asserting argv[0] == toolPath, exactly-one occurrence of device.value, and no connect/-H/-P element.
- The guarded real-toolchain desktopTest runs `xcrun simctl list devices available` through ProcessRunner, feeds it to parseSimctlDevices, and asserts simulators found with none Booted — SKIPPING (not failing) when xcrun is absent.
- The Koin graph resolves on desktop with both qualifiers, and UnsupportedDeviceSources resolves on Android and iOS (DeviceSourcesModuleTest extended).
- MirrorStage.IosDeferred, its derivation branch, its render branch, mirror_ios_deferred, IdbHostSource, IdbHostSourceImpl and UnsupportedIdbHostSource are all gone from the tree (grep returns nothing).
- dumpHierarchy(Ios, …) returns HierarchySnapshotModel.Empty without spawning a process and without throwing; a test fails if iOS ever returns nodes (AC-50).
- Measured iOS capture round-trip recorded as a NUMBER in the verification table, with an explicit note if it misses the ~10fps CaptureFloorMs aspiration. No iOS tuning constant added to paper over it.
- The AC-31 coverage split and the SC-26 physical-device code-review-only status are written into the verification table verbatim, not implied.
- Zero files changed in :compose-application, :ios-application, ScreencapMirrorTransport.kt and ProcessRunner.kt.
- No new version-catalog entry and no new Gradle module.

### successMetrics

- iOS screenshot-poll mirroring reaches the same ~10fps capture floor as Android (MirrorTuning.CaptureFloorMs = 100) with no new tuning constants — measured and recorded as a number, treated as an aspiration not a prediction (CaptureFloorMs is itself an unmeasured Android constant).
- Zero regressions in the existing Android discovery/mirror/error-card test suite after the platform-dispatch extraction (AC-49).
- NFR-5 is strictly stronger after this spec than before it: the single-explicit-device invariant moves from a positional one-liner to a machine-checked structural assertion covered by an exhaustive ToolKind × DeviceCommand sweep.
- Commit 1's diff is reviewable as `--word-diff` of `platform` insertions only — the stated review-cost control actually holds.
- Net file-count discipline: exactly one new intent, zero new tuning constants, zero new dependencies, zero new Gradle modules, two interfaces deleted for one added.

### architecture

#### approach

Keep every existing interface and every existing transport/process primitive. Add `platform: PlatformModel` to exactly the five DeviceRepository methods that genuinely dispatch, resolve per-platform implementations through qualified Koin bindings inside the ONE existing registration seam (DeviceSourcesProvider.desktop.kt), and let DeviceRepositoryImpl select with an exhaustive `when` (compile-time totality, not a map lookup). Sequence it as two commits: a pure signature refactor with zero iOS code, then the iOS adapter.

#### rationale

Dispatch becomes explicit at the type level — a reader of `mirror(platform, id)` sees which device family it targets. ScreencapMirrorTransport is reused VERBATIM as two Koin instances, so 'iOS mirroring' at the transport layer is zero new code; that is the single largest saving in the spec and the reason screenshot polling beat idb video-stream. There is no hidden mutable platform state in the data layer, so MirrorViewModel's state.platform stays the single source of truth. And `observeDiscovery(platform)` being a PARAMETERISED flow is what forces the adb loop to actually stop when the user switches — fixing half the shadowing bug by construction rather than by a flag check. The honest cost is 19 mechanical files before any iOS behaviour exists; the diff is a signature change with no logic change, which is exactly what makes the existing Android suite a real regression guard rather than a formality.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Dispatching decorator over a StateFlow<PlatformModel> | Far smaller diff (no DeviceRepository signature changes, AC-49 nearly free) but it hides platform in mutable shared state: `mirror(deviceId)` reads one thing today and another after a toolbar click with no signature to show it. It introduces a race invisible in the types — a mirror() call in flight while the platform flips targets the wrong tool — and still needs a setter path from the ViewModel, so the 'no call sites change' saving is partly illusory. Smaller now, worse at 3am. Explicitly rejected by the confirmed clarify survey. |
| A single PlatformDeviceSource super-interface collapsing all five datasources into one per platform | The textbook-prettiest option and the worst fit for a `could`-priority feature whose headline constraint is 'do not change Android behaviour'. It forces a lowest-common-denominator shape on surfaces that genuinely differ (adb has a daemon lifecycle, simctl has none; emulator boot is a detached foreground process, simctl boot is not), rewrites the Android path wholesale putting NFR-20/AC-49 at real risk, and deletes the fine-grained test fakes four test files depend on. |
| `simctl list devices -j` (JSON) instead of plain text | data-device-api has zero dependencies beyond :data-core and no serialization plugin, so JSON means adding kotlinx-serialization-json for one list of three fields, or hand-rolling a JSON reader. It also contradicts the confirmed clarify answer 'Moot — no JSON parsing needed in v1'. The plain-text rows are regex-shaped and match parseAdbDevices' precedent exactly. `-j` is kept as the documented escape hatch and becomes the right answer the day v1.1 hierarchy capture needs real JSON anyway. |
| `idb video-stream` for mirroring | Higher fps, but it needs a second ProcessRunner entry point (run() buffers stdout to EOF), H.264/MJPEG decode that decodeToImageBitmap() cannot do against a contractually-PNG MirrorFrame.bytes, and a second transport implementation — destroying the 'ScreencapMirrorTransport unchanged' saving. Revisit only if measured iOS screenshot latency proves 10fps unusable. |
| Add `EmulatorModel.bootId: String?` to carry a shut-down simulator's udid | Honest but structural: a new field on a shared domain model for one platform's identifier quirk, plus matching reads in DiscoveryProjection.toRow and anywhere else that builds a boot target. Populating the existing runningDeviceId with the udid even when shut down (KDoc widened) is zero structural change and produces exactly the right row — id = udid, serial = udid, kind = Boot driven by isRunning. Add the real field the day a second consumer keys 'is running' off `runningDeviceId != null`. |
| Add DeviceCommand.IosScreenshot as a distinct member | It would force a platform-aware branch INSIDE ScreencapMirrorTransport — the one file this spec most wants to leave alone. Reusing Screencap (same semantics: 'capture the screen as an encoded PNG on stdout'; only the argv differs per ToolKind) is precisely what makes the transport reusable verbatim. The 'no silent overloading' constraint is honoured by rewriting Screencap's KDoc to the platform-neutral meaning it already had. |
| Rename bootEmulator to bootTarget | Touches 6 files for zero behavioural value and inflates the AC-49 regression surface. The KDoc carries the truth instead. Revisit if a third platform ever lands. |
| Platform-specific picker group labels ('Emulators' / 'Simulators') | Needs a new parameter on the shipped DevicePickerPanel organism for a cosmetic win. One neutral string ('Virtual devices') ships instead; the pair is the noted upgrade path. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| DeviceRepository (five methods gain `platform: PlatformModel`) | logic | reuse | features.json#android-device-discovery-mirroring | domain-repository-api/.../repository/DeviceRepository.kt. observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats take `platform`. detectTools (already probes every platform), refreshDiscovery (nudges whichever loop runs), restartAdbServer and setAdbPath are deliberately unchanged — the last two become explicitly Android-only affordances via widened KDoc, and must be UNREACHABLE from the iOS surface, not merely harmless: the iOS error cards offer no 'Restart adb' / 'Locate adb' action. bootEmulator keeps its name with a widened KDoc. |
| DeviceRepositoryImpl (exhaustive `when` dispatch; poll() split) | logic | reuse | features.json#android-device-discovery-mirroring | data-repository-impl/.../source/DeviceRepositoryImpl.kt. Holds qualified android/ios instances of DeviceCommandExecutor and MirrorTransport plus unqualified AdbHostSource, EmulatorSource and IosHostSource; private `executor(p)` / `transport(p)` select with an exhaustive `when` over PlatformModel. poll() becomes pollAndroid(avdNames, emptyPolls) — today's body, moved VERBATIM — plus pollIos(emptyPolls). observeDiscovery(platform) picks one in its loop. dumpHierarchy(Ios, …) short-circuits to HierarchySnapshotModel.Empty without reaching the executor and without throwing (FR-57/AC-50). |
| pollIos() | logic | new |  | The one place iOS discovery semantics live. probeSimctl() -> ToolMissing if absent; listSimulators(); listDevices() wrapped so an idb failure (no idb_companion) degrades to an EMPTY physical list rather than hiding simctl's answer; then classifies into DiscoveryModel.{Found, SimulatorNotBooted, DeviceNotTrusted, ToolMissing, NoDeviceFound}. Distinguishes 'succeeded, zero rows parsed, non-empty stdout' and emits PollFailed(tool = "xcrun", reason = <stdout head>) so parser drift is visible rather than silent. |
| observeDiscovery's PollFailed catch — tool-name fix | logic | reuse | features.json#android-device-discovery-mirroring | Today `catch (failure: DeviceCommandException)` hardcodes `tool = ToolKind.Adb.binaryName`. On the iOS branch that reports an adb failure for an xcrun one. Must take the platform's own binary name. This is a genuine latent bug the dispatch seam exposes. |
| buildDeviceArgv(tool, toolPath, device, command) | logic | reuse | features.json#android-device-discovery-mirroring | data-device-api/.../source/parse/DeviceArgv.kt. The single argv construction point. Branches the device selector per ToolKind — adb `-s <id>` prefix, simctl `simctl <sub> <udid> …` positional, idb `<sub> --udid <id>` suffix — then enforces NFR-5 structurally: `check(argv.count { it == device.value } == 1)`. Android-only commands (WmSize, WmDensity, GetProp, EmuAvdName, UiAutomatorDump) on an iOS tool throw a documented DeviceCommandException naming the tool and the command rather than silently returning empty bytes. |
| ToolKind.Simctl("xcrun", null) | data | new |  | The binary is xcrun (at /usr/bin, always present with Xcode CLT); the argv branch inserts the `simctl` subcommand. A new candidate entry in probeCandidates keyed off the kind, same mechanism as sdkSubdirectory today. Modelling the real executable keeps ToolResolver honest. |
| DeviceCommand.SimulatorBoot + HostCommand.{XcrunVersion, ListSimulators, IdbVersion, ListTargets} | data | new |  | Explicit, KDoc-documented widening of two closed sets (each member carries a 'widened for F-10 (FR-49)' line). DeviceCommand.Screencap is REUSED for iOS capture with its KDoc rewritten platform-neutral — that reuse is what keeps ScreencapMirrorTransport untouched. |
| parseSimctlDevices | logic | new |  | data-device-api/.../source/parse/SimctlDevicesParser.kt, commonMain, pure. Hand-rolled line scanner over `xcrun simctl list devices available`: `-- iOS 18.1 --` runtime headers, `    iPhone 16 (UDID) (Booted\|Shutdown)` rows. Never throws; an unparseable line is skipped; `(unavailable, …)` rows are dropped defensively even though `available` already hides them. Returns List<SimulatorResource>. Same contract and test style as parseAdbDevices. |
| parseIdbTargets | logic | new |  | data-device-api/.../source/parse/IdbTargetsParser.kt, commonMain, pure. Splits `idb list-targets` rows on `\|`, keeps target_type == device, reads name/udid/state. Untrusted detection lives behind ONE named token set so AS-2 can be corrected in one place once measured, with a fallback classifying a listed-but-unusable physical target as untrusted. Never throws. |
| SimulatorResource / IosTargetResource | data | new |  | data-device-api/.../source/model/. Plain resource records (name, udid, runtime, state / name, udid, state, trusted) consumed by the mapper layer. Resource-side only; the domain never sees them. |
| IosHostSource (replaces IdbHostSource) | data | new |  | data-device-api/.../source/datasource/IosHostSource.kt: probeSimctl(): ToolProbe, probeIdb(): ToolProbe, listSimulators(): List<SimulatorResource>, listDevices(): List<IosTargetResource>, bootSimulator(DeviceId). One interface because one iOS poll uses both binaries in a single round trip. IdbHostSource is DELETED rather than grown: its only caller today is detectTools()'s probe, which probeIdb() subsumes — so this costs one Unsupported* stand-in instead of two. |
| IosHostSourceImpl | data | new |  | data-device-impl/src/desktopMain/.../source/datasource/IosHostSourceImpl.kt (NFR-19). Resolves xcrun and idb through the existing ToolResolver, runs the four new HostCommands through ProcessRunner.run() at MirrorTuning.HostCommandTimeoutMs, and bootSimulator at MirrorTuning.DaemonStartTimeoutMs. `simctl boot` uses run(), NOT spawnDetached — it returns once the boot is initiated and its exit code matters: an already-booted simulator exits non-zero ('Unable to boot device in current state: Booted') and that specific failure is treated as SUCCESS. spawnDetached stays reserved for the long-lived foreground `emulator -avd` process. probeSimctl() additionally verifies the SUBCOMMAND resolves (a CLT-only Mac has xcrun but no simctl) so it reports ToolMissing with an install hint rather than failing at first use. No new tuning constants. |
| IosDeviceCommandExecutor | data | new |  | data-device-impl/src/desktopMain/.../source/datasource/IosDeviceCommandExecutor.kt. Implements the EXISTING DeviceCommandExecutor interface. Chooses ToolKind.Simctl vs ToolKind.Idb per call from the target set the last poll produced (simulator udids are known to be simulators), then buildDeviceArgv(tool, path, device, command) -> ProcessRunner.run. Supports Screencap and SimulatorBoot; the uiautomator/wm/getprop members throw a documented unsupported-on-this-tool DeviceCommandException. |
| AdbDeviceCommandExecutor (renamed from DeviceCommandExecutorImpl) | data | reuse | features.json#android-device-discovery-mirroring | One file, one import site, renamed for symmetry with its new iOS sibling. Body unchanged except for buildDeviceArgv's new first argument (ToolKind.Adb). |
| ScreencapMirrorTransport | data | reuse | features.json#android-device-discovery-mirroring | REGISTERED TWICE, EDITED ZERO TIMES. The adaptive single-flight loop, trimToPngSignature, per-frame IHDR size read (offsets 16/20), dropped-frame arithmetic and fps window all apply unchanged; the only difference between the two instances is which executor they were handed. `simctl io … screenshot --type=png -` writes a clean PNG so trimToPngSignature is a no-op rather than a necessity — it stays in the path because the transport is reused verbatim, which is the point. A mid-stream DeviceCommandException (simulator shut down) propagates and flips the session to Lost, identical to an unplugged handset. |
| ProcessRunner | data | reuse | features.json#android-device-discovery-mirroring | EDITED ZERO TIMES. run(argv, timeoutMs) serves every simctl/idb call including boot; the two-pipe concurrent drain, withTimeoutOrNull, finally destroyForcibly, the liveChildren shutdown hook and the TIMEOUT_REASON_PREFIX discriminator all carry over free. The ONE gap — run() buffers stdout to EOF, so a long-lived `idb video-stream` would need a stream(argv): Flow<ByteArray> entry point — is exactly why polling was chosen. |
| ToolResolver + probeCandidates | logic | reuse | features.json#android-device-discovery-mirroring | Learns ToolKind.Simctl: a /usr/bin candidate, readVersion via `xcrun --version`, installCommand 'xcode-select --install', envHint ''. ToolKind.Idb.readVersion stops returning null now that IdbVersion exists. Its injected isExecutable/identifyAdb/userPath lambdas are the established test seam and the reason iOS resolution is unit-testable — ToolResolver's own KDoc warns that spawning a process inside runTest's virtual clock ALWAYS times out. |
| DeviceSourcesProvider.desktop.kt (qualified bindings) | logic | reuse | features.json#android-device-discovery-mirroring | The single registration seam, unchanged in location. AdbHostSourceImpl / EmulatorSourceImpl / IosHostSourceImpl stay singleOf+bind and UNQUALIFIED (one implementation each — qualifying them would be ceremony). DeviceCommandExecutor and MirrorTransport get explicit `single<T>(named("android")) { … }` / `single<T>(named("ios")) { … }` per koin-di.md's preference for explicit single over singleOf when qualifiers are involved. |
| UnsupportedDeviceSources | logic | reuse | features.json#android-device-discovery-mirroring | data-device-impl/src/commonMain/.../core/unsupported/. Gains UnsupportedIosHostSource; registers the SAME existing UnsupportedDeviceCommandExecutor / UnsupportedMirrorTransport instances under BOTH named("android") and named("ios"); loses UnsupportedIdbHostSource. Four registrations, no new behaviour. Easy to miss because it is NOT a desktop test failure — it is a Koin startup crash on a phone (FR-56). |
| DiscoveryModel.SimulatorNotBooted / DiscoveryModel.DeviceNotTrusted | data | new |  | domain-core/.../model/device/DiscoveryModel.kt. SimulatorNotBooted(simulators: List<EmulatorModel>, toolVersion: String, lastPollEpochMs: Long) and DeviceNotTrusted(deviceId: String, deviceName: String, simulatorsAvailable: Boolean). Unprefixed per the confirmed survey (the iOS poll is the only producer). Both are SUCCESS values per DiscoveryModel's existing contract — neither ever becomes a Failure. |
| EmulatorModel.runningDeviceId (KDoc widened) | data | reuse | features.json#android-device-discovery-mirroring | Carries the udid even when the simulator is SHUT DOWN, because simulator names are not unique across runtimes ('iPhone 16' exists under both iOS 17.5 and 18.1 on this host) and booting by name is ambiguous. DiscoveryProjection.toRow then yields exactly the right row with zero structural change: id = udid, serial = udid, kind = Boot (driven by isRunning, not by runningDeviceId). Android is untouched — its path builds EmulatorModel through its own mapper. One sentence of KDoc, no new field. |
| DiscoveryErrorProjection.errorCard() — platform guard + two arms | logic | reuse | features.json#error-handling-and-recovery | presentation-feature-mirror/.../core/discovery/DiscoveryErrorProjection.kt. Signature unchanged — `platform` is already on MirrorUiState. Two new `when` arms for the iOS variants plus an early guard returning null when platform == Ios and the variant is Android-only. Stays pure and Compose-free, which is why AC-46's shadowing assertion is a plain commonTest with no device. |
| MirrorViewModel.selectPlatform() — discovery job cancel/re-collect | logic | reuse | features.json#android-device-discovery-mirroring | The STRUCTURAL half of the shadowing fix. Already nulls sessionDeviceId and releases the frozen frame; now also cancels the discovery collection Job and re-collects observeDiscovery(newPlatform), and resets discovery = DiscoveryModel.Detecting and rows = emptyList() in the same reduction so no stale Android emission (or a late in-flight one from the old flow) survives the switch. The adb loop is structurally gone the instant iOS is selected, not merely ignored. |
| MirrorErrorKind.SimulatorNotBooted / DeviceNotTrusted + MirrorErrorCard content | data | reuse | features.json#error-handling-and-recovery | Two new members of a Compose-free, string-free model whose KDoc ALREADY records err-sim and err-trust as drawn-but-deferred. MirrorErrorCard.primaryDeviceId already exists and carries the udid the Boot action needs — no new card field. |
| MirrorIntent.BootSimulator(udid) — the ONLY new intent | logic | new |  | Dispatches bootEmulator(Ios, udid) then refreshDiscovery() so the simulator becomes selectable with no app restart (AC-47). The other three card actions reuse what already ships: 'Choose another' and 'Use simulator' are both MirrorIntent.OpenPicker; "I've tapped Trust — retry" is MirrorIntent.RetryDiscovery, which already cuts the poll interval short via refreshDiscovery. FR-53 names four; three already exist. |
| MirrorErrorStage / ErrorCard organism | ui | reuse | components.json#error-card | Two new `when` arms only. The 520dp ErrorCard organism already takes title/body/kind/command/onCopyCommand/primary/secondary/diagnostic — exactly the err-sim and err-trust designs. No new error layout is invented. |
| DevicePickerPanel + PlatformToolCard + SourceListRow | ui | reuse | components.json#device-picker-panel | Unchanged organism. MirrorRowGroup{Physical,Emulator} maps 1:1 onto physical iPhone / simulator and MirrorRowKind.Boot already means 'cold, committing boots it' — exactly a shut-down simulator. The only changes are at the call site: platformCards()'s hardcoded `enabled = platform == Android` and the emulatorGroupLabel string. NFR-8 keyboard navigation (pickerKeyAction, nextSelectableIndex, the single onPreviewKeyEvent + FocusRequester) knows nothing about adb and is free. |
| MirrorStageContent / MirrorStage.IosDeferred (deleted) | ui | reuse | components.json#mirror-stage-content | MirrorStage.IosDeferred, its `platform == Ios -> IosDeferred` derivation branch at MirrorContract.kt:358, its render branch at MirrorContent.kt:131-139 and the mirror_ios_deferred string are DELETED outright. iOS then flows through the same Picker/Mirror/NoSignal derivation Android uses, which is what makes every downstream surface work for free. |
| SimulatorResourceMapper / ToolProbeMapper | logic | reuse | features.json#android-device-discovery-mirroring | data-repository-impl/.../core/mapper/. SimulatorResourceMapper is new (SimulatorResource -> EmulatorModel, populating runningDeviceId with the udid in both states); ToolProbeMapper is extended so Simctl maps to PlatformModel.Ios alongside Idb. Both obey mappers.md: NEVER throw, unknown values get a safe default in toModel. |

#### dataModel

| entity | fields |
| --- | --- |
| SimulatorResource (data-device-api, new) | ["name: String", "udid: String", "runtime: String", "state: String (Booted \| Shutdown \| other, verbatim)"] |
| IosTargetResource (data-device-api, new) | ["name: String", "udid: String", "state: String (verbatim)", "targetType: String", "trusted: Boolean (derived from one named token set — AS-2, correctable in one place)"] |
| DiscoveryModel.SimulatorNotBooted (domain-core, new variant) | ["simulators: List<EmulatorModel>", "toolVersion: String", "lastPollEpochMs: Long"] |
| DiscoveryModel.DeviceNotTrusted (domain-core, new variant) | ["deviceId: String", "deviceName: String", "simulatorsAvailable: Boolean"] |
| EmulatorModel (domain-core, KDoc widened only — no field change) | ["runningDeviceId: String? — now carries the simulator udid even when SHUT DOWN, because simulator names are not unique across runtimes"] |
| ToolKind (data-device-api, widened) | ["Adb(\"adb\", sdkSubdirectory)", "Emulator(…)", "Idb(\"idb\", null)", "Simctl(\"xcrun\", null) — NEW; the simctl subcommand is inserted by the argv branch"] |
| DeviceCommand (data-device-api, widened) | ["Screencap — REUSED for iOS, KDoc rewritten platform-neutral", "SimulatorBoot — NEW", "WmSize / WmDensity / GetProp / EmuAvdName / UiAutomatorDump — Android-only; throw a documented DeviceCommandException on an iOS tool"] |
| HostCommand (data-device-api, widened) | ["XcrunVersion — NEW", "ListSimulators — NEW", "IdbVersion — NEW", "ListTargets — NEW"] |
| MirrorErrorKind (presentation-feature-mirror, widened) | ["SimulatorNotBooted — NEW (err-sim, already named in the existing KDoc as drawn-but-deferred)", "DeviceNotTrusted — NEW (err-trust, same)"] |
| MirrorStatsModel / observeMirrorStats (widened) | ["simctlVersion: String — NEW, mirrors the existing adbVersion StateFlow so the iOS status line is not blank (three lines; a blank line looks like a bug)"] |

#### interfaces

| name | contract |
| --- | --- |
| DeviceRepository (five methods) | observeDiscovery(platform: PlatformModel): Flow<DiscoveryModel>; mirror(platform: PlatformModel, deviceId: DeviceId): Flow<MirrorFrameModel>; dumpHierarchy(platform: PlatformModel, deviceId: DeviceId): HierarchySnapshotModel; bootEmulator(platform: PlatformModel, target: String); observeMirrorStats(platform: PlatformModel): Flow<MirrorStatsModel>. detectTools(), refreshDiscovery(), restartAdbServer(), setAdbPath(path) UNCHANGED. |
| IosHostSource | suspend fun probeSimctl(): ToolProbe; suspend fun probeIdb(): ToolProbe; suspend fun listSimulators(): List<SimulatorResource>; suspend fun listDevices(): List<IosTargetResource>; suspend fun bootSimulator(device: DeviceId). All five are SUCCESS-value returning: a missing tool is a ToolProbe result, an empty device list is an empty list. Never an exception for 'nothing found'. |
| DeviceCommandExecutor (unchanged interface, two qualified implementations) | suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray. Bound as named("android") -> AdbDeviceCommandExecutor and named("ios") -> IosDeviceCommandExecutor. |
| MirrorTransport (unchanged interface, two qualified instances of ONE implementation) | fun frames(device: DeviceId): Flow<MirrorFrame>; val stats: StateFlow<TransportStats>. Both qualifiers resolve to ScreencapMirrorTransport, differing only in the executor handed to them. |
| buildDeviceArgv | public fun buildDeviceArgv(tool: ToolKind, toolPath: String, device: DeviceId, command: DeviceCommand): List<String>. Post-condition, enforced in code: argv[0] == toolPath AND argv.count { it == device.value } == 1. Violation is a programming error and throws IllegalStateException via check(). |

#### stateManagement

Orbit MVI, unchanged. MirrorViewModel's `state.platform` stays the SINGLE source of truth for platform — nothing in the data layer holds mutable platform state, which is the whole point of passing it as a parameter. selectPlatform() reduces state AND cancels/re-collects the discovery job in the same handler; the reduction resets discovery = Detecting and rows = emptyList() so a late emission from the cancelled flow cannot resurrect stale Android state. errorCard()/indicatorDot()/dropBanner() remain pure Compose-free projections over MirrorUiState — that purity is what makes the shadowing fix assertable in commonTest with no device attached. Exactly one new intent (BootSimulator(udid)) joins the contract; its handler calls bootEmulator(Ios, udid) then refreshDiscovery().

#### navigation

- No navigation change. The desktop root is RectifyWindowShell; MirrorHost owns the stage. The platform switch is the existing toolbar segmented control (PLATFORM_SOURCES already lists 'Android' and 'iOS') plus the picker platform cards — neither gains a destination.
- Stage transitions change only by DELETION: MirrorStage.IosDeferred and its derivation branch go away, so platform == Ios now falls through to the same Picker -> Mirror -> NoSignal derivation Android uses.

#### affectedModules

- data-device-api (commonMain + commonTest) — ToolKind, DeviceCommand, HostCommand, DeviceArgv, two new parsers, two new resources, IosHostSource added / IdbHostSource deleted
- data-device-impl (commonMain unsupported + desktopMain + desktopTest) — IosHostSourceImpl, IosDeviceCommandExecutor, ToolResolver, DeviceSourcesProvider.desktop.kt, UnsupportedDeviceSources; IdbHostSourceImpl deleted
- domain-core — DiscoveryModel (+2 variants), EmulatorModel KDoc, PlatformModel KDoc, MirrorStatsModel (+simctlVersion)
- domain-repository-api — DeviceRepository (five signatures)
- domain-usecase-api — five use-case interfaces
- domain-usecase-impl (+ its one device test) — five use-case impls
- data-repository-impl (commonMain + commonTest) — DeviceRepositoryImpl, SimulatorResourceMapper, ToolProbeMapper, DataRepositoryImplModule, four existing device test files
- presentation-feature-mirror (commonMain + commonTest) — MirrorContract, MirrorViewModel, MirrorContent, MirrorErrorStage, DiscoveryErrorProjection, DiscoveryProjection, MirrorFakes and three test files
- presentation-core-localisation — strings.xml (~9 added, 1 value changed, 1 deleted)
- NOT TOUCHED (by design): compose-application, desktop-application, ios-application, presentation-core-ui, presentation-core-styling, data-database-*, data-handoff-api, mcp-application, ScreencapMirrorTransport.kt, ProcessRunner.kt

### projectContext

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| error-card | ErrorCard | components.json#error-card |
| device-picker-panel | DevicePickerPanel | components.json#device-picker-panel |
| platform-tool-card | PlatformToolCard | components.json#platform-tool-card |
| source-list-row | SourceListRow | components.json#source-list-row |
| command-block | CommandBlock | components.json#command-block |
| error-glyph | ErrorGlyph | components.json#error-glyph |
| device-chip | DeviceChip | components.json#device-chip |
| mirror-stage-content | MirrorStageContent | components.json#mirror-stage-content |
| mirror-host | MirrorHost | components.json#mirror-host |
| mirror-frame-view | MirrorFrameView | components.json#mirror-frame-view |
| no-signal-stage | NoSignalStage | components.json#no-signal-stage |
| status-line | StatusLine | components.json#status-line |
| shell-status-bar | ShellStatusBar | components.json#shell-status-bar |
| rectify-window-shell | RectifyWindowShell | components.json#rectify-window-shell |
| emulator-select | EmulatorSelect | components.json#emulator-select |

#### reuseAssets


#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| theme-accessor | Theme | styles.json#theme-accessor |
| app-theme | AppTheme | styles.json#app-theme |
| theme-color | ThemeColor (Theme.color) — signal colours for the two new cards | styles.json#theme-color |

#### existingFeatures

| id | name | registryRef | note |
| --- | --- | --- | --- |
| android-device-discovery-mirroring | Android device discovery & live mirroring (F-2/F-3) | features.json#android-device-discovery-mirroring | The feature being generalised. Supplies ProcessRunner, ToolResolver/probeCandidates, ScreencapMirrorTransport, DeviceArgv, the five datasource interfaces, the hand-rolled-parser precedent and the DiscoveryModel contract. Its recorded waivers W-2 (capture rate never measured) and SEC-1 (GetProp takes unvalidated free text into adb shell) are both live for this spec — see knownRisksFromQA. |
| error-handling-and-recovery | Error handling & recovery (F-6) | features.json#error-handling-and-recovery | Supplies MirrorErrorKind/MirrorErrorCard, the pure errorCard()/dropBanner() projections, MirrorErrorStage and the budgeted escalation ladder. Its KDoc already names err-sim and err-trust as drawn-but-deferred — this spec fills exactly those two slots. |
| verify-and-rediff-loop | Verify & Re-diff Loop (F-9) | features.json#verify-and-rediff-loop | Rect/pixel-neutral; zero change needed. Its neutrality is the evidence that deleting MirrorStage.IosDeferred and routing iOS through the full stage machine is safe. |
| error-propagation | Failure / Result error propagation | features.json#error-propagation | resultLauncher(errorMapper = Failure.Technical::Kind) in every *UseCaseImpl is unchanged by this spec — the five iOS-aware use cases stay thin wrappers. Its recorded gap (ShowError ignores messageId) must NOT be copied. |
| localisation | Localisation (Compose Resources) | features.json#localisation | ~9 new keys join the ~95 existing English keys under rectify_kmp.presentation_core_localisation.generated.resources.Res.string.*; one value changed; one key deleted. |
| history-batch-tracking | History & Batch Tracking (F-8) | features.json#history-batch-tracking | Untouched. iOS batches persist through the same path because everything from HierarchySnapshotModel downward is already neutral. |

#### newToBuild

- data-device-api: SimulatorResource, IosTargetResource, parseSimctlDevices, parseIdbTargets, IosHostSource (5 new files)
- data-device-impl/desktopMain: IosHostSourceImpl, IosDeviceCommandExecutor (2 new files)
- domain-core: DiscoveryModel.SimulatorNotBooted, DiscoveryModel.DeviceNotTrusted (2 new sealed variants, no new file)
- data-repository-impl: pollIos(), SimulatorResourceMapper (1 new file)
- presentation-feature-mirror: MirrorErrorKind.SimulatorNotBooted, MirrorErrorKind.DeviceNotTrusted, MirrorIntent.BootSimulator(udid) (3 new sealed/enum members, no new file)
- data-device-impl commonMain: UnsupportedIosHostSource (new class inside the existing UnsupportedDeviceSources.kt)
- Tests: SimctlDevicesParserTest, IdbTargetsParserTest, IosDiscoveryFixtures, IosHostSourceImplTest, SimctlRealToolchainTest (guarded), DeviceIosDiscoveryPollingTest, MirrorIosErrorCardTest, MirrorViewModelIosPlatformSwitchTest (8 new test files)
- Localisation: ~9 new string keys
- DELETED: IdbHostSource, IdbHostSourceImpl, UnsupportedIdbHostSource, MirrorStage.IosDeferred + its derivation and render branches, mirror_ios_deferred

### resources

#### rules

| ref | why |
| --- | --- |
| .claude/rules/platform-coverage.md | All idb/simctl process invocation and every iOS datasource impl must live under data-device-impl/src/desktopMain; that module's commonMain names no java.*. 'Desktop' is the current-OS JVM target only — there is no macosMain and no native macOS target. :ios-application's Xcode scheme must not be disturbed (NFR-19). |
| .claude/rules/error-handling.md | A missing tool or an empty device list is a SUCCESS value, never an exception; the iOS cards ride the MirrorErrorCard projection off DiscoveryModel, not Failure. Also records the gap to NOT copy: every ShowError(messageId) handler currently ignores messageId and shows a hardcoded 'Error'. |
| .claude/rules/koin-di.md | One module per Gradle module, load order significant, provideDeviceSources() is the single registration seam, and when qualifiers are introduced the rule prefers explicit `single { … get(named(…)) }` over singleOf — which is exactly how the two qualified bindings are written. |
| .claude/rules/package-layout.md | core/ is internal-only, source/ is public with one subfolder per semantic concern. explicitApi() polices the boundary at compile time: every new public declaration needs an explicit visibility modifier and an explicit return type. The new parsers/resources/interface go under source/; nothing new needs a core/ directory in data-device-api. |
| .claude/rules/mappers.md | Mapper<I,O> is a fun interface and a mapper NEVER throws; unknown values get a safe default in toModel. Binds SimulatorResourceMapper, the extended ToolProbeMapper, and (by precedent) both new parsers. |
| .claude/rules/testing.md | kotlin.test is wired once by the convention plugins — never add kotlin-test per module. The desktop target is the one that runs, but a cross-target build must pass: commonTest compiles for iosArm64 and Kotlin/Native rejects a backtick test name containing a comma or a colon (an apostrophe is fine — measured). :compose-application deliberately has no test wiring, so nothing testable may land there. No Compose UI-test harness exists; the two new cards are covered by pure projection tests plus @Preview pairs. |
| .claude/rules/orbit-mvi.md | selectPlatform()'s job cancel/re-collect and the new BootSimulator handler must follow the repo's intent/reduce/sideEffect shape; state.platform stays the single source of truth. |
| .claude/rules/localisation.md | ~9 new keys and one value change go into presentation-core-localisation/src/commonMain/composeResources/values/strings.xml with generated accessors; machine output (argv, stderr tails) stays unlocalised by convention. Beware AAPT-style apostrophe escaping — a recently fixed bug in this repo ("I've tapped Trust" contains an apostrophe). |
| .claude/rules/design-tokens.md | The two new cards read colours only through Theme.color; never a Color(0x…) literal, a raw .dp or a hardcoded TextStyle at a call site (NFR-9). |
| .claude/rules/compose-preview-convention.md | The two new MirrorErrorStage branches ship with light/dark @Preview pairs — the only visual coverage this repo has. |
| .claude/rules/domain-layer.md | DiscoveryModel's two new variants and the widened EmulatorModel KDoc live in domain-core and must stay free of platform/resource types. |
| .claude/rules/version-catalog-hygiene.md | Zero new catalog entries — the 'no new dependency' decision (plain-text parsing over kotlinx-serialization-json) is enforced here. |
| .claude/rules/convention-plugins.md | No new Gradle module and no new convention-plugin hook is needed; the existing library/feature plugins already register jvm("desktop") and wire kotlin.test. |
| CLAUDE.md#architecture | Module structure, package dev.rectify.io, and the Desktop-is-a-plain-JVM-module fact that keeps :desktop-application and :compose-application out of this spec's blast radius. |
| data-device-api/CLAUDE.md | Module-local conventions for the closed command sets and the single argv construction point. |
| data-device-impl/CLAUDE.md | Module-local conventions: one process-starting site (ProcessRunner), every budget named in MirrorTuning, TIMEOUT_REASON_PREFIX shared producer↔consumer. |

#### skills


#### agents


#### docs

| ref | why |
| --- | --- |
| .pipeline/sdd/docs/ios-platform-support/inputs/codebase-recon.md | Verified ground truth for file paths, symbols, the four adb hardcoding sites, what is already neutral, and the binding rules. Cited, not re-derived. |
| .pipeline/sdd/docs/ios-platform-support/inputs/ios-tooling-probe.md | Measured toolchain state on the build host: Xcode 26.3, runtimes 17.5/18.1, idb + idb_companion present, NO simulator booted. The evidence base for AC-46's measured half and for AS-3's waiver. |
| .pipeline/sdd/docs/registry/bugs.json | Recurrence check for every touched area — see knownRisksFromQA. |

### errorStates

| state | behavior |
| --- | --- |
| loading | DiscoveryModel.Detecting while the first iOS poll is in flight. selectPlatform() resets to Detecting on every switch so the user never sees the previous platform's answer attributed to the new one. |
| empty | No simulators and no physical devices at all -> DiscoveryModel.NoDeviceFound with iOS-worded copy. Distinct from SimulatorNotBooted, which means 'simulators exist but none is running'. Both are success values. |
| error | SIMULATOR NOT BOOTED (err-sim) — simctl lists simulators but none is Booted. MirrorErrorKind.SimulatorNotBooted; primary 'Boot simulator' -> MirrorIntent.BootSimulator(udid from MirrorErrorCard.primaryDeviceId); secondary 'Choose another' -> MirrorIntent.OpenPicker. Recovers without an app restart (AC-47). |
| error | DEVICE NOT TRUSTED (err-trust) — a physical iPhone is the ONLY candidate and is listed-but-unusable. MirrorErrorKind.DeviceNotTrusted; primary "I've tapped Trust — retry" -> MirrorIntent.RetryDiscovery (already cuts the poll interval short); secondary 'Use simulator' -> MirrorIntent.OpenPicker. If a booted simulator ALSO exists, Found wins and the untrusted device shows as a disabled row with its state verbatim — exactly how parseAdbDevices treats an `unauthorized` handset. |
| error | TOOL MISSING — xcrun present but simctl absent (Command Line Tools without full Xcode: `xcrun: error: unable to find utility "simctl"`). Probing the xcrun BINARY alone is not sufficient; probeSimctl() verifies the subcommand resolves and reports ToolMissing with the 'xcode-select --install' hint rather than failing at first use. The iOS card never offers 'Restart adb' or 'Locate adb…'. |
| error | POLL FAILED — parser drift. simctl exits zero, stdout is non-empty, and parseSimctlDevices returns zero rows: emit PollFailed(tool = "xcrun", reason = <stdout head>), NOT SimulatorNotBooted. Makes an Xcode format change visible instead of silently rendering 'no simulators'. |
| error | IDB UNAVAILABLE — `idb list-targets` fails or times out (no idb_companion running). Budgeted by MirrorTuning.HostCommandTimeoutMs; the failure degrades the PHYSICAL list to empty and never aborts the simctl half, so simulators still render. No companion auto-start. |
| error | SIMULATOR DIES MID-STREAM — `simctl io screenshot` exits non-zero; DeviceCommandException propagates through the unchanged transport and flips the session to Lost, identical to an unplugged Android handset. Zero new code. |
| error | ALREADY BOOTED — `simctl boot <udid>` exits non-zero with 'Unable to boot device in current state: Booted'. Treated as SUCCESS: the user's goal is achieved. This is why boot uses run() rather than spawnDetached — the exit code matters. |
| error | MULTIPLE DEVICES — a booted simulator AND a trusted physical iPhone both present yields Found with ≥2 selectable physical rows and the existing MultipleDevices card. Correct and already handled; zero iOS work. |
| error | HIERARCHY ON iOS — dumpHierarchy(Ios, …) short-circuits to HierarchySnapshotModel.Empty in the repository WITHOUT spawning a process and WITHOUT throwing. attachedHierarchyXml stays Android-gated so the prompt degrades to "" exactly as today (FR-57, AC-50). |

### observability

#### logs

- DeviceCommandException(argv, exitCode, stderrTail) from ProcessRunner remains the single diagnostic record for every failed simctl/idb invocation, with the TIMEOUT_REASON_PREFIX discriminator unchanged.
- PollFailed(tool, reason) now reports the PLATFORM'S OWN binary name instead of the hardcoded ToolKind.Adb.binaryName — on iOS that means 'xcrun' or 'idb', surfaced verbatim in the error card's diagnostic line.
- Parser-drift signal: 'simctl succeeded, zero rows parsed, non-empty stdout' is logged into PollFailed's reason as the stdout head so an Xcode format change is diagnosable from the card itself.

#### metrics

- TransportStats(fps, latencyMs = null by design, captureRoundTripMs, droppedFrames, widthPx, heightPx) — unchanged contract, now populated from the iOS capture loop. captureRoundTripMs is the measurement that answers SC-25's ~10fps aspiration honestly.
- Measured iOS capture round-trip, recorded ONCE as a number in the verification table rather than asserted as a threshold (CaptureFloorMs = 100 is itself an unmeasured Android constant).
- Tool version line in the status bar: adbVersion on Android, the new simctlVersion on iOS — so the line never goes blank and look like a bug.

#### analyticsEvents

- None. This repo ships no analytics pipeline and this spec does not introduce one.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | NFR-8 — the device picker stays fully keyboard-navigable for iOS rows with ZERO new code: pickerKeyAction(key) maps DirectionUp/DirectionDown/Enter/NumPadEnter/Escape to MoveUp/MoveDown/Start/Close through one onPreviewKeyEvent with a FocusRequester, and nextSelectableIndex does the arithmetic. Nothing in that path knows about adb. Verified by the existing PickerNavigationTest, which must stay green. |
| A11Y-2 | Both new error cards expose their primary and secondary actions as real focusable AppButtons through the shipped ErrorCard organism — same keyboard reachability as every existing card. No bespoke action affordance is invented. |
| A11Y-3 | A shut-down simulator renders as MirrorRowKind.Boot — a selectable row whose commit action boots it — rather than a disabled row with a hidden affordance, so the recovery path is reachable by keyboard alone (AC-47 via the picker as well as via the card). |
| A11Y-4 | NFR-9 — both new cards read every colour through Theme.color and ship light/dark @Preview pairs. No new signal colour is introduced; the orange/warning role already in use for recoverable states is reused. |
| A11Y-5 | Machine output (argv, stderr tails, udids) stays unlocalised and renders in the mono CommandBlock, per the existing convention — it is diagnostic text, not prose. |

### performanceBudget

| metric | target |
| --- | --- |
| iOS frame capture round-trip (`xcrun simctl io <udid> screenshot --type=png -`) | ≤100ms to hit MirrorTuning.CaptureFloorMs (~10fps). ASPIRATION, NOT A PREDICTION — plausibly 200–400ms. Measure once a simulator is booted and record the number; the adaptive single-flight loop degrades gracefully and TransportStats reports the truth. Do NOT add an iOS tuning constant to paper over a miss. |
| `idb list-targets` per poll | MirrorTuning.HostCommandTimeoutMs = 3000ms (existing). Timeout degrades the physical list to empty; the simctl half is unaffected. |
| `xcrun simctl list devices available` per poll | MirrorTuning.HostCommandTimeoutMs = 3000ms (existing). |
| `xcrun simctl boot <udid>` | MirrorTuning.DaemonStartTimeoutMs = 15000ms (existing, reused — honours the 'no new tuning constants' rule). |
| Android discovery/mirroring characteristics after the extraction | Byte-identical behaviour. pollAndroid() is a verbatim move; any measurable change is an AC-49 failure, not a tuning question. |
| New tuning constants added | Zero. |

### i18n

#### locales

- en (the repo ships English only; ~95 existing keys)

#### strings

- ADD mirror_error_sim_not_booted_title — e.g. "No simulator is running"
- ADD mirror_error_sim_not_booted_body — simctl sees simulators but none is booted; boot one to start mirroring.
- ADD mirror_error_device_not_trusted_title — e.g. "iPhone hasn't trusted this Mac"
- ADD mirror_error_device_not_trusted_body — unlock the iPhone and tap Trust on the prompt, then retry.
- ADD mirror_error_action_boot_simulator — "Boot simulator"
- ADD mirror_error_action_choose_another — "Choose another"
- ADD mirror_error_action_trust_retry — "I've tapped Trust — retry" (NOTE: contains an apostrophe and an em dash; do NOT let AAPT-style escaping leak into the UI — that exact bug was fixed in this repo recently)
- ADD mirror_error_action_use_simulator — "Use simulator"
- ADD mirror_error_tool_missing_simctl_body — simctl isn't available; install the full Xcode command line tools (shown with the `xcode-select --install` CommandBlock)
- CHANGE mirror_group_emulator value from "Emulator" to a neutral "Virtual devices". The KEY is kept deliberately: renaming it touches MirrorContent's import plus strings.xml for zero behaviour. Cosmetic debt, recorded.
- DELETE mirror_ios_deferred (strings.xml:102) along with MirrorStage.IosDeferred and its render branch

#### notes

Keys follow the existing mirror_error_* / mirror_group_* conventions. Machine output (argv, stderr tails, udids, tool versions) stays unlocalised by convention and renders in CommandBlock. The two Android-worded keys mirror_error_tool_missing_* and mirror_error_no_device_* are NOT reused for iOS — the platform guard in errorCard() makes sure the Android copy never renders on iOS, which is the readable half of the shadowing fix.

### security

#### dataHandling

- NFR-5 moves from a POSITIONAL guarantee to a STRUCTURAL one. Today `listOf(toolPath, "-s", device.value) + command.arguments()` is enforced only by the shape of one line, and nothing stops a future arguments() branch appending a second target. After this spec, buildDeviceArgv builds per-tool in a `when (tool)` and then asserts `check(argv.count { it == device.value } == 1)` before returning. Tool-agnostic, fails loudly for any branch that forgets or duplicates the selector, and machine-checked by an exhaustive ToolKind × DeviceCommand sweep. NFR-5 ends up STRONGER after this spec than before it.
- The sweep test asserts three properties per pair: argv[0] == toolPath, the device id appears exactly once, and no element matches the connect/-H/-P denylist (no remote or arbitrary target can be addressed).
- DeviceId keeps its private constructor and DeviceCommand/HostCommand stay closed sets, so the argv remains unforgeable from user data. `check()` throwing IllegalStateException from a commonMain pure function is correct here — a selector-less argv is a programming error, not unexpected input, and must never be reachable from user input.
- All process invocation stays behind ONE site (ProcessRunner), unchanged. No new process-starting code is introduced anywhere.
- Screenshot bytes stay in memory and flow through the existing MirrorFrame path; iOS adds no new persistence, no new file write and no new network call.

#### permissions

- No new OS permission is requested. simctl operates on the user's own simulators; idb requires the user to have trusted this Mac on the physical device — the untrusted case is precisely the error state this spec surfaces rather than a permission to request.
- idb_companion is NOT auto-started. It is the user's daemon; the app reports its absence (degraded physical list) rather than spawning it.

#### privacy

- Device udids and simulator names appear in the picker, the status line and error-card diagnostics — the same visibility level adb serials already have. Nothing is transmitted off-host.
- Carry-over from the Android feature's recorded SEC-1: DeviceCommand.GetProp takes unvalidated free text into an adb shell (no production callers). The sweep test adds the first machine-checked guard over that family — see knownRisksFromQA.

### rollout

#### featureFlags


#### migrations


#### backwardCompatibility

No persisted-data or wire-format change. AnnotationEnvelope's field order (wire-breaking) is untouched; hierarchyXml keeps its name because hierarchy capture is deferred (FR-57) — that rename is explicitly NOT paid for here. No Room migration. The only breaking changes are SOURCE-level and internal to this repo: DeviceRepository's five signatures (ripples into 5 use-case interfaces and their impls), buildDeviceArgv's signature (one Android call site), DeviceCommandExecutorImpl renamed to AdbDeviceCommandExecutor (one import site), and the deletion of IdbHostSource/IdbHostSourceImpl/UnsupportedIdbHostSource/MirrorStage.IosDeferred/mirror_ios_deferred. All are compile-time visible — explicitApi() and the exhaustive `when` over PlatformModel make an unhandled case a compile error, not a runtime surprise. Rollout is a straight merge; the two-commit split is the risk control, not a flag.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json#PP-TD-8 | Kotlin/Native release linker OOMs under ~4 parallel linkReleaseFramework tasks (Java heap space against a 4 GiB daemon heap). Pre-existing, affects EVERY cross-target gate in this repo. | The mandated cross-target build (`:data-device-api:build :data-repository-impl:build :presentation-feature-mirror:build`) failing with 'Java heap space' rather than a real compile error — easy to misread as this spec's bug. | Run the cross-target gate with `--max-workers=1`, the documented workaround used for every cross-target gate on the previous spec. Do not chase a phantom iOS compile failure. |
| bugs.json#PP-TD-9 | EmulatorModel.toRow sets no detail, unlike DeviceModel.toRow — an information gap in the device picker. Located in presentation-feature-mirror/core/discovery/DiscoveryProjection.kt, which this spec edits. | iOS simulator rows inheriting the same blank detail, so the picker shows 'iPhone 16' twice with no runtime ('iOS 18.1') to disambiguate — which matters MORE on iOS than Android because simulator names are NOT unique across runtimes. | Do not fix PP-TD-9 here (it belongs to android-device-discovery-mirroring and its own gate), but DO populate the iOS simulator row's detail with the runtime so the duplicate-name case is distinguishable. That is additive and does not touch the Android branch. Flag the Android half as still open. |
| bugs.json#PP-TD-4 | Tautological test: assertEquals(1, PAYLOAD_FORMAT_VERSION) — asserts a literal against itself and would pass under any edit. | The same failure mode in this spec's new tests: asserting that ToolKind.Simctl.binaryName == "xcrun" or that DiscoveryModel.SimulatorNotBooted exists proves nothing. The argv sweep and the parser fixtures must assert BEHAVIOUR against real captured output. | Every new test must be falsifiable by a deliberate mutation. For the argv sweep, verify by temporarily deleting the selector from one branch and confirming the sweep goes red. |
| bugs.json#PP-TD-5 | Test name and comment claim a structural property the body does not assert (body asserts Empty -> empty twice). Same self-referential failure mode that produced three defects on that spec. | AC-50's 'iOS returns an empty hierarchy' test degenerating into asserting Empty == Empty. It must assert that dumpHierarchy(Ios, …) returns Empty WITHOUT the executor being called — a fake executor that fails the test if invoked is the real assertion. | Write the AC-50 test against a fake DeviceCommandExecutor whose run() throws; the test passes only because the repository never reaches it. |
| bugs.json#PP-TD-6 | core/payload file names do not partition their contents (one label function in one file while two live in another). | The same drift in the new iOS files: parseSimctlDevices and parseIdbTargets each getting their own file is correct; resisting the temptation to dump both plus the two resources into one IosParsers.kt. | One concern per file, matching the existing AdbDevicesParser.kt / AvdListParser.kt / WmOutputParser.kt partition exactly. |
| features.json#android-device-discovery-mirroring (waiver W-2, HARDWARE GATE) | Android capture rate was never measured (handset unauthorized, AVD never booted), so MirrorTuning.CaptureFloorMs stays at an UNMEASURED 100ms and latency is reported as an explicit placeholder rather than a fabricated figure. | SC-25's success metric ('iOS reaches the same ~10fps floor as Android') being read as a comparison against a measured Android baseline. There is no measured Android baseline. Also watch for the temptation to report a fabricated iOS latency — TransportStats.latencyMs is null BY DESIGN. | Record the measured iOS captureRoundTripMs as a raw number with the explicit note that the Android comparison point is itself unmeasured. Do not fabricate latency. |
| features.json#android-device-discovery-mirroring (waiver SEC-1) | DeviceCommand.GetProp takes unvalidated free text into an adb shell (no production callers today). | The buildDeviceArgv generalisation being the moment that free text reaches a NEW tool. GetProp has no iOS meaning and must throw a documented DeviceCommandException on an iOS tool rather than being quietly argv-built. | The exhaustive ToolKind × DeviceCommand sweep covers the GetProp × Simctl and GetProp × Idb pairs explicitly, asserting the documented throw. This spec adds the first machine-checked guard over that family without taking on the Android-side fix. |
| features.json#error-propagation (recorded gap) | Every feature's XSideEffect.ShowError(messageId) handler IGNORES messageId and shows a hardcoded, unlocalized 'Error' title. Explicitly documented as a gap, NOT a pattern to extend. | Routing an iOS failure through ShowError instead of the MirrorErrorCard projection, which would both lose the message and copy the gap. | By construction: a missing tool and an empty device list are SUCCESS values on DiscoveryModel and ride the error-card projection. No new ShowError call site is added by this spec. |
| features.json#error-propagation (recorded gap) | resultLauncher catches CancellationException — an inherited open change request carried across four specs, still unfixed. | selectPlatform()'s NEW discovery-job cancellation producing a swallowed CancellationException that surfaces as a spurious Failure instead of a clean stop when the user flips platforms mid-poll. | Cancel the collection JOB in the ViewModel (outside the use case's resultLauncher scope) rather than cancelling inside the flow, and add an explicit test that a platform switch mid-poll leaves state at Detecting with no error side effect. Do not attempt the repo-wide resultLauncher fix here. |
| features.json#app-shell-window-layout (waiver 2) | Cmd+1/2/3 LIVE dispatch is unverifiable; only the pure shortcutToTab mapping is tested. The precedent: live keyboard dispatch in this shell is not machine-verified. | Claiming NFR-8 keyboard coverage for the new iOS picker rows on the strength of PickerNavigationTest alone. That test covers the pure arithmetic, not live dispatch. | State NFR-8's iOS coverage as 'pure navigation logic tested, live dispatch unverified — same standing as Android', not as newly proven. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 1 | COMMIT 1 / step 1 — capture the real toolchain fixtures before touching code | [] | ["AC-46", "AC-31"] | 0.25d |
| 2 | COMMIT 1 — thread `platform: PlatformModel` through DeviceRepository's five dispatching methods (domain-repository-api), widening bootEmulator/restartAdbServer/setAdbPath KDoc; leave detectTools/refreshDiscovery alone | [] | ["AC-49"] | 0.25d |
| 3 | COMMIT 1 — ripple the signature through 5 use-case interfaces (domain-usecase-api), 5 use-case impls (domain-usecase-impl) and DumpHierarchyUseCaseImplTest. Pure churn: no logic change, resultLauncher bodies untouched | ["2"] | ["AC-49"] | 0.25d |
| 4 | COMMIT 1 — accept `platform` in DeviceRepositoryImpl (unused, documented), update MirrorViewModel's five call sites to pass state.platform, update MirrorFakes and the four data-repository-impl device test files. Run the full existing suite; `git diff --word-diff` must show only `platform` insertions. MERGE AS A STANDALONE COMMIT — this is AC-49's evidence | ["3"] | ["AC-49"] | 0.5d |
| 5 | COMMIT 2 — widen the closed sets in data-device-api/commonMain: ToolKind.Simctl("xcrun", null), DeviceCommand.SimulatorBoot, HostCommand.{XcrunVersion, ListSimulators, IdbVersion, ListTargets} with their buildHostArgv entries, and DeviceCommand.Screencap's KDoc rewritten platform-neutral. Each member carries a 'widened for F-10 (FR-49)' KDoc line | ["4"] | ["AC-48"] | 0.25d |
| 6 | COMMIT 2 — generalize buildDeviceArgv to (tool, toolPath, device, command) with the per-tool selector `when` and the structural `check(argv.count { it == device.value } == 1)`; update the one Android call site; write the EXHAUSTIVE ToolKind × DeviceCommand sweep test (argv[0] == toolPath, id appears exactly once, no connect/-H/-P element, Android-only commands throw on iOS tools). Verify by mutation: delete one selector and confirm the sweep goes red | ["5"] | ["AC-48"] | 0.5d |
| 7 | COMMIT 2 — add SimulatorResource / IosTargetResource and the two hand-rolled parsers (parseSimctlDevices, parseIdbTargets) in data-device-api/commonMain, with fixture tests pinned to step 1's captured output. Neither parser throws; parseIdbTargets' untrusted detection lives behind ONE named token set (AS-2) | ["1", "5"] | ["AC-46", "AC-31"] | 0.75d |
| 8 | COMMIT 2 — add IosHostSource to data-device-api; DELETE IdbHostSource | ["7"] | ["AC-46"] | 0.25d |
| 9 | COMMIT 2 — ToolResolver learns ToolKind.Simctl (/usr/bin candidate, readVersion via `xcrun --version`, installCommand 'xcode-select --install', envHint ''), and ToolKind.Idb.readVersion stops returning null. Test through the INJECTED predicates — never spawn inside runTest's virtual clock | ["5"] | ["AC-46"] | 0.5d |
| 10 | COMMIT 2 — IosHostSourceImpl and IosDeviceCommandExecutor in data-device-impl/src/desktopMain (NFR-19); rename DeviceCommandExecutorImpl -> AdbDeviceCommandExecutor and its test. probeSimctl verifies the SUBCOMMAND resolves; bootSimulator uses run() at DaemonStartTimeoutMs and treats 'already Booted' non-zero as success; ScreencapMirrorTransport and ProcessRunner are NOT opened | ["6", "8", "9"] | ["AC-47", "AC-48"] | 1d |
| 11 | COMMIT 2 — rewrite DeviceSourcesProvider.desktop.kt: unqualified singleOf for AdbHostSourceImpl/EmulatorSourceImpl/IosHostSourceImpl, explicit qualified `single<DeviceCommandExecutor>(named(…))` and `single<MirrorTransport>(named(…)) { ScreencapMirrorTransport(executor = get(named(…))) }`. Add UnsupportedIosHostSource and register the existing Unsupported executor/transport under BOTH qualifiers; remove UnsupportedIdbHostSource. Extend DeviceSourcesModuleTest to resolve the whole graph | ["10"] | ["AC-48", "AC-49"] | 0.5d |
| 12 | COMMIT 2 — domain-core: add DiscoveryModel.SimulatorNotBooted and DiscoveryModel.DeviceNotTrusted, widen EmulatorModel.runningDeviceId's KDoc (udid even when shut down), drop PlatformModel.Ios's 'Not wired in this feature' line, add simctlVersion to the mirror-stats model | ["4"] | ["AC-46", "AC-31"] | 0.25d |
| 13 | COMMIT 2 — data-repository-impl: split poll() into pollAndroid() (MOVED VERBATIM) and pollIos() (new); add the exhaustive `when (platform)` executor/transport selection; short-circuit dumpHierarchy(Ios) to HierarchySnapshotModel.Empty without reaching the executor; FIX observeDiscovery's PollFailed catch so it names the platform's own tool; add SimulatorResourceMapper; extend ToolProbeMapper for Simctl -> Ios; update DataRepositoryImplModule's constructor call | ["11", "12"] | ["AC-46", "AC-49", "AC-50"] | 1d |
| 14 | COMMIT 2 — data-repository-impl tests: add DeviceIosDiscoveryPollingTest against fake sources under virtual time, covering SimulatorNotBooted, DeviceNotTrusted, Found, ToolMissing, NoDeviceFound, the idb-fails-but-simctl-survives degradation, the parser-drift PollFailed path, and the AC-50 'executor is never called' assertion (fake executor throws if invoked) | ["13"] | ["AC-46", "AC-50", "AC-31"] | 0.75d |
| 15 | COMMIT 2 — presentation state: DELETE MirrorStage.IosDeferred, its derivation branch and its render branch; add MirrorErrorKind.{SimulatorNotBooted, DeviceNotTrusted} and MirrorIntent.BootSimulator(udid); extend applyDiscovery with two branches; add errorCard()'s two arms AND the platform guard returning null for Android-only variants when platform == Ios; add the simulator runtime to the iOS row detail (see PP-TD-9) | ["12"] | ["AC-31", "AC-46"] | 0.75d |
| 16 | COMMIT 2 — MirrorViewModel: thread state.platform into the five calls, CANCEL and re-collect the discovery job in selectPlatform() while resetting discovery = Detecting / rows = emptyList(), and add the BootSimulator handler (bootEmulator(Ios, udid) then refreshDiscovery()). Add MirrorViewModelIosPlatformSwitchTest and MirrorIosErrorCardTest (including the fixture-driven, WAIVED device-not-trusted half) | ["15", "13"] | ["AC-31", "AC-46", "AC-47"] | 0.75d |
| 17 | COMMIT 2 — UI + copy: MirrorErrorStage's two new branches with light/dark @Preview pairs; ~9 new strings (watch the apostrophe in "I've tapped Trust — retry"); DELETE mirror_ios_deferred; relabel mirror_group_emulator's value to 'Virtual devices'; replace platformCards()'s hardcoded `enabled = platform == Android` and update PickerPlatform.enabled's KDoc | ["15"] | ["AC-31"] | 0.5d |
| 18 | COMMIT 2 — the one real-toolchain test (AC-46's measured half): a desktopTest that spawns `xcrun simctl list devices available` through ProcessRunner, feeds stdout to parseSimctlDevices, and asserts simulators found with NONE Booted. It SKIPS (not fails) when xcrun is absent, per ProcessRunnerTest's precedent. Always-run-with-skip, not tag/ignore — this is the only honest hardware-coverage claim the feature can make | ["10"] | ["AC-46", "AC-31"] | 0.25d |
| 19 | Gate: `./gradlew :data-device-api:desktopTest :data-device-impl:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest :domain-usecase-impl:desktopTest`, then the cross-target build `./gradlew :data-device-api:build :data-repository-impl:build :presentation-feature-mirror:build --max-workers=1` (PP-TD-8). Grep-verify every deletion. Confirm zero diff in ScreencapMirrorTransport.kt, ProcessRunner.kt, :compose-application and :ios-application | ["14", "16", "17", "18"] | ["AC-49", "AC-50"] | 0.5d |
| 20 | Manual demo + verification table (AC-48, SC-25, SC-27): boot a simulator, mirror it, record the MEASURED captureRoundTripMs as a number, exercise Boot simulator from the error card, and write the honest coverage table — AC-31 split (measured vs fixture-waived), SC-26 physical path code-review-only, AC-48 demo-verified | ["19"] | ["AC-48", "AC-31"] | 0.5d |

### testStrategy

#### unit

- parseSimctlDevices — fixture tests over THIS host's real `xcrun simctl list devices available` output (Xcode 26.3, runtimes iOS 17.5 + 18.1): runtime headers, Booted and Shutdown rows, the duplicate 'iPhone 16' across two runtimes, an unparseable line skipped, an `(unavailable, …)` row dropped, empty input -> empty list. Never throws. commonTest, zero hardware.
- parseIdbTargets — fixture tests over real `idb list-targets` output: pipe splitting, target_type filtering, and the untrusted token set (AS-2) with a constructed fixture pinning whatever is decided. Never throws.
- buildDeviceArgv — the EXHAUSTIVE ToolKind × DeviceCommand sweep: for every pair, argv[0] == toolPath, `argv.count { it == device.value } == 1`, no element in the connect/-H/-P denylist, and Android-only commands (WmSize, WmDensity, GetProp, EmuAvdName, UiAutomatorDump) throwing the documented DeviceCommandException on Simctl/Idb. This test IS NFR-5's proof and must be mutation-verified.
- probeCandidates — ToolKind.Simctl's /usr/bin candidate ordering against a fake env.
- ToolResolver — Simctl and Idb resolution via the INJECTED isExecutable/identifyAdb/userPath predicates. Never spawn a process inside runTest: ToolResolver's own KDoc records that it always times out against the virtual clock, which is exactly why these are parameters.
- errorCard() / DiscoveryErrorProjection — the two new iOS arms AND the platform guard: assert errorCard() returns null for every Android-only DiscoveryModel variant when platform == Ios (this is AC-46's shadowing assertion, pure and device-free), and returns the right MirrorErrorKind with the right primary/secondary action for SimulatorNotBooted and DeviceNotTrusted.
- applyDiscovery / DiscoveryProjection — a shut-down simulator becomes MirrorRowGroup.Emulator + MirrorRowKind.Boot with id = udid and serial = udid; two same-named simulators on different runtimes produce two distinguishable rows.
- SimulatorResourceMapper / ToolProbeMapper — toModel never throws, unknown state gets a safe default, Simctl maps to PlatformModel.Ios.
- MirrorContract — MirrorStage derivation no longer has an Ios branch; a `when` over MirrorStage still compiles exhaustively after IosDeferred's deletion.

#### integration

- DeviceIosDiscoveryPollingTest (data-repository-impl/commonTest, fake IosHostSource, virtual time) — pollIos classifies Found / SimulatorNotBooted / DeviceNotTrusted / ToolMissing / NoDeviceFound correctly; an idb failure degrades the physical list to empty WITHOUT erasing simctl's simulator answer; 'succeeded, zero rows, non-empty stdout' emits PollFailed(tool = "xcrun") not SimulatorNotBooted; PollFailed names xcrun/idb rather than adb.
- AC-50 — dumpHierarchy(Ios, …) returns HierarchySnapshotModel.Empty against a fake DeviceCommandExecutor whose run() THROWS: the test passes only because the executor is never reached. Fails the day iOS starts returning nodes.
- The transport loop against a FAKE executor under virtual time (ScreencapMirrorTransportTest's existing shape, re-pointed at the iOS executor binding) — frames emitted, dropped-frame arithmetic, cancellation stops the loop, a mid-stream DeviceCommandException propagates.
- Koin graph — DeviceSourcesModuleTest extended: both qualifiers resolve on desktop; UnsupportedDeviceSources resolves on the Android and iOS graphs (FR-56). A missing qualifier here is a phone startup crash, not a desktop test failure.
- MirrorViewModelIosPlatformSwitchTest — switching to iOS cancels the discovery job, resets discovery to Detecting and rows to empty, and a late emission from the cancelled Android flow cannot resurrect stale state or raise a spurious error side effect.
- AC-47 — BootSimulator(udid) dispatches bootEmulator(Ios, udid) then refreshDiscovery(), and the next poll surfaces the simulator as selectable, with no restart. Driven through fakes.
- AC-49/SC-29 — the ENTIRE pre-existing Android suite, run unchanged after commit 1 and again after commit 2. This is the regression guard, not a new test.

#### ui

- No Compose UI-test harness exists in this repo and introducing one is out of scope. The two new MirrorErrorStage branches are covered by light/dark @Preview pairs plus the pure errorCard() projection tests — exactly the standing of every other card today. Stated as a limitation, not claimed as coverage.
- AC-48 is verified by DEMO (its declared verificationMethod): boot a simulator, start mirroring, observe live frames and the fps/resolution status line, and record the measured captureRoundTripMs as a number.
- SC-27's click-through (error card -> Boot simulator -> selectable) is demo-verified end to end; its logic half is machine-verified in the integration tests.

#### linkedScenarios

- SC-14
- SC-25
- SC-26
- SC-27
- SC-28
- SC-29

#### coverageGoal

Every pure/logic surface this spec adds is machine-covered: both parsers, buildDeviceArgv's exhaustive sweep, ToolResolver's Simctl branch via injected predicates, pollIos's five classifications, errorCard()'s two arms AND the platform guard, applyDiscovery's two branches, the Koin graph under all three target graphs, and the AC-50 never-reach-the-executor assertion. ONE guarded real-toolchain desktopTest provides the only genuine hardware claim (AC-46's measured simulator-not-booted half) and SKIPS where xcrun is absent. HONESTLY UNCOVERED, stated rather than implied: (1) AC-31's device-not-trusted half is FIXTURE-DRIVEN and WAIVED (NFR-18/AS-3 — no untrusted iPhone exists here); (2) the whole SC-26 physical-device path is CODE-REVIEW-VERIFIED ONLY, since idb_companion's running state at poll time has never been exercised on this host; (3) all rendering is @Preview-only, repo-wide; (4) AC-48 is demo-verified, not test-verified, by its own declared verificationMethod; (5) the iOS capture rate is measured-and-recorded, never asserted against a threshold, because the Android baseline it would be compared to is itself unmeasured (W-2). ProcessRunner spawning inside runTest's virtual clock always times out — that constraint shapes every seam above and is why nothing testable lands in :compose-application.

#### fileChangeList

##### commit1PureRefactorNoIosCode

###### summary

19 files, ALL edited. Zero added, zero deleted, zero new tests. `git diff --word-diff` shows only `platform` insertions. This commit is AC-49's evidence.

###### edited

- domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/DeviceRepository.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/ObserveDeviceDiscoveryUseCase.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/StartMirroringUseCase.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/DumpHierarchyUseCase.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/BootEmulatorUseCase.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/ObserveMirrorStatsUseCase.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/ObserveDeviceDiscoveryUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/StartMirroringUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/DumpHierarchyUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/BootEmulatorUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/ObserveMirrorStatsUseCaseImpl.kt
- domain-usecase-impl/src/commonTest/kotlin/domain/usecase/impl/source/usecase/device/DumpHierarchyUseCaseImplTest.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceDiscoveryPollingTest.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceToolDetectionTest.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceHierarchyDumpTest.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceHierarchyDumpFallbackTest.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt

##### commit2Ios

###### summary

47 file-level changes: 16 added, 27 edited, 2 renamed, 2 deleted. ~8 files overlap with commit 1, so the two commits together touch ~58 distinct files.

###### added

- data-device-api/src/commonMain/kotlin/data/device/api/source/model/SimulatorResource.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/model/IosTargetResource.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/parse/SimctlDevicesParser.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/parse/IdbTargetsParser.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/datasource/IosHostSource.kt
- data-device-api/src/commonTest/kotlin/data/device/api/SimctlDevicesParserTest.kt
- data-device-api/src/commonTest/kotlin/data/device/api/IdbTargetsParserTest.kt
- data-device-api/src/commonTest/kotlin/data/device/api/IosDiscoveryFixtures.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosHostSourceImpl.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosDeviceCommandExecutor.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/datasource/IosHostSourceImplTest.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/datasource/SimctlRealToolchainTest.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/SimulatorResourceMapper.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceIosDiscoveryPollingTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorIosErrorCardTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelIosPlatformSwitchTest.kt

###### edited

- data-device-api/src/commonMain/kotlin/data/device/api/source/model/ToolKind.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceCommand.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/tool/ToolProbeCandidates.kt
- data-device-api/src/commonTest/kotlin/data/device/api/DeviceArgvTest.kt
- data-device-api/src/commonTest/kotlin/data/device/api/ToolProbeCandidatesTest.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/core/tool/ToolResolver.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/DeviceSourcesProvider.desktop.kt
- data-device-impl/src/commonMain/kotlin/data/device/impl/core/unsupported/UnsupportedDeviceSources.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/tool/ToolResolverTest.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/di/DeviceSourcesModuleTest.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/DiscoveryModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/EmulatorModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/PlatformModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/MirrorStatsModel.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/ToolProbeMapper.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/di/DataRepositoryImplModule.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContent.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorErrorStage.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/discovery/DiscoveryErrorProjection.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/discovery/DiscoveryProjection.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/DiscoveryErrorProjectionTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorContractTest.kt
- presentation-core-localisation/src/commonMain/composeResources/values/strings.xml

###### renamed

- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/DeviceCommandExecutorImpl.kt -> AdbDeviceCommandExecutor.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/datasource/DeviceCommandExecutorImplTest.kt -> AdbDeviceCommandExecutorTest.kt

###### deleted

- data-device-api/src/commonMain/kotlin/data/device/api/source/datasource/IdbHostSource.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IdbHostSourceImpl.kt

###### symbolDeletionsInsideEditedFiles

- MirrorStage.IosDeferred (MirrorContract.kt:102-103)
- the `platform == Ios -> IosDeferred` derivation branch (MirrorContract.kt:358)
- the IosDeferred render branch (MirrorContent.kt:131-139) and its mirror_ios_deferred import
- string mirror_ios_deferred (strings.xml:102)
- UnsupportedIdbHostSource (UnsupportedDeviceSources.kt)

###### deliberatelyUntouched

- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/transport/ScreencapMirrorTransport.kt — registered twice, edited zero times
- data-device-impl/src/desktopMain/kotlin/data/device/impl/core/process/ProcessRunner.kt — edited zero times
- data-device-api/src/commonMain/kotlin/data/device/api/source/tuning/MirrorTuning.kt — no new tuning constants
- compose-application/** (no test wiring by design; PLATFORM_SOURCES already lists iOS)
- ios-application/** (an Xcode project, out of scope)
- presentation-core-ui/** (ErrorCard and DevicePickerPanel reused as-is)
- gradle/libs.versions.toml and settings.gradle.kts (no new dependency, no new module)

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-31 | ["FR-25", "FR-51", "FR-52", "FR-53", "FR-55"] | ["DiscoveryModel.SimulatorNotBooted / DiscoveryModel.DeviceNotTrusted", "pollIos()", "DiscoveryErrorProjection.errorCard() — platform guard + two arms", "MirrorErrorKind.SimulatorNotBooted / DeviceNotTrusted + MirrorErrorCard content", "MirrorIntent.BootSimulator(udid) — the ONLY new intent", "MirrorErrorStage / ErrorCard organism (components.json#error-card)", "parseSimctlDevices", "parseIdbTargets"] | ["SC-14", "SC-27", "SC-28", "MirrorIosErrorCardTest (projection, both halves)", "SimctlRealToolchainTest (simulator-not-booted half: REAL, MEASURED on this host)", "IdbTargetsParserTest (device-not-trusted half: FIXTURE-DRIVEN and WAIVED per NFR-18/AS-3 — no untrusted iPhone exists here; the measured half must NOT be read as covering this half)"] |
| AC-46 | ["FR-48", "FR-51", "FR-52"] | ["pollIos()", "IosHostSource / IosHostSourceImpl", "parseSimctlDevices", "DiscoveryErrorProjection.errorCard() — platform guard", "MirrorViewModel.selectPlatform() — discovery job cancel/re-collect", "observeDiscovery's PollFailed catch — tool-name fix"] | ["SC-27", "SimctlRealToolchainTest (real `xcrun simctl list devices available`, skip-guarded)", "DeviceIosDiscoveryPollingTest (SimulatorNotBooted classification)", "DiscoveryErrorProjectionTest (errorCard() returns null for every Android-only variant when platform == Ios — the shadowing assertion)", "MirrorViewModelIosPlatformSwitchTest (the adb loop is structurally cancelled, state resets to Detecting)"] |
| AC-47 | ["FR-49", "FR-51", "FR-53"] | ["IosHostSourceImpl.bootSimulator (run(), not spawnDetached; already-booted non-zero == success)", "DeviceCommand.SimulatorBoot", "MirrorIntent.BootSimulator(udid)", "EmulatorModel.runningDeviceId (KDoc widened — carries the udid when shut down)", "DeviceRepository.bootEmulator(platform, target)"] | ["SC-27", "MirrorViewModelIosPlatformSwitchTest (BootSimulator -> bootEmulator(Ios, udid) -> refreshDiscovery, no restart)", "DeviceIosDiscoveryPollingTest (re-poll surfaces the booted simulator as selectable)", "DeviceArgvTest sweep (SimulatorBoot × Simctl argv carries the udid exactly once)"] |
| AC-48 | ["FR-49", "FR-50", "FR-54"] | ["buildDeviceArgv(tool, toolPath, device, command)", "IosDeviceCommandExecutor", "ScreencapMirrorTransport (reused verbatim, registered twice)", "ProcessRunner (reused verbatim)", "DeviceSourcesProvider.desktop.kt (qualified bindings)", "DeviceRepositoryImpl (exhaustive `when` dispatch)"] | ["SC-25", "SC-26", "DeviceArgvTest (exhaustive ToolKind × DeviceCommand sweep, including Screencap × Simctl and Screencap × Idb)", "ScreencapMirrorTransportTest against a fake iOS executor under virtual time", "DeviceSourcesModuleTest (both qualifiers resolve)", "DEMO (AC-48's declared verificationMethod) — live frames + measured captureRoundTripMs recorded as a number. SC-26's physical path is CODE-REVIEW-VERIFIED ONLY: idb against a physical device has never been exercised here."] |
| AC-49 | ["FR-48", "FR-56"] | ["DeviceRepository (five methods gain `platform`)", "the five use-case interfaces and impls", "DeviceRepositoryImpl (pollAndroid() = verbatim move)", "MirrorViewModel (five call sites)", "UnsupportedDeviceSources (both qualifiers registered)", "AdbDeviceCommandExecutor (renamed, body unchanged)"] | ["SC-29", "The ENTIRE pre-existing Android suite, run unchanged after commit 1 AND after commit 2", "`git diff --word-diff` on commit 1 showing only `platform` insertions (inspection evidence attached to the test claim)", "DeviceSourcesModuleTest (Android and iOS app graphs still resolve — a missing qualifier is a phone startup crash, not a desktop test failure)", "Cross-target build with --max-workers=1 (PP-TD-8)"] |
| AC-50 | ["FR-57"] | ["DeviceRepositoryImpl.dumpHierarchy(Ios) — short-circuits to HierarchySnapshotModel.Empty without spawning a process", "attachedHierarchyXml (Android-gated, unchanged)", "HierarchySnapshotModel.rawDump (unchanged, \"\" on iOS)"] | ["INSPECTION (AC-50's declared verificationMethod) — confirm rawDump is \"\" and AnnotationEnvelope.hierarchyXml is NOT renamed", "DeviceIosDiscoveryPollingTest's AC-50 case: a fake DeviceCommandExecutor whose run() THROWS; the test passes only because the executor is never reached. Fails the day iOS starts returning nodes — the cheapest guard against half-shipping v1.1."] |

### openQuestions

- AS-2 / CARRIED: the exact idb list-targets field or error signal that identifies an untrusted physical iPhone. Recommendation on record: contain it in ONE named token set inside parseIdbTargets plus a fallback classifying a listed-but-unusable physical target as untrusted; correct from one place after a real measurement, with the fixture test pinning whatever is decided. Does not block implementation.
- NEW (plan): Preparation describes commit 1 as 'qualified Koin bindings + platform threaded through', but a named("ios") binding cannot exist with zero iOS code. This plan puts ONLY the signature threading in commit 1 and the qualifier registration in commit 2. If a reviewer insists both land in commit 1, the only way is binding the Android impl under both qualifiers temporarily — which this plan rejects as dead code. Flagging rather than silently resolving.
- NEW (plan): Preparation's '~45 files' for the extraction is a recount away from reality — the pure refactor is 19 files; the iOS commit is 47 file-level changes. Worth correcting in the backlog so the estimate is not read as 45-for-the-refactor.
- CARRIED: whether AX JSON hierarchy capture is worth the rawDump/hierarchyXml rename in a future v1.1, now that iOS mirroring and error cards ship without it.
- CARRIED: whether idb video-stream's higher fps is worth the streaming-ProcessRunner + H.264/MJPEG decode investment, if 10fps screenshot polling proves insufficient in real usage. Decide after step 20's measured number.
- CARRIED: whether bootEmulator should be renamed bootTarget now that it boots simulators too. Recommendation on record and adopted here: keep the name, add platform, widen the KDoc — a rename touches 6 files for zero behavioural value and inflates the AC-49 regression surface.
- CARRIED: whether observeMirrorStats needs a second version field (simctlVersion). Recommendation on record and adopted here: add it — three lines, and a status line that goes blank on iOS looks like a bug to the user and a gap to the reviewer.
- CARRIED: picker group label wording — one neutral string ('Virtual devices') vs a platform-specific pair ('Emulators'/'Simulators'). Adopted: one neutral string now, keeping the mirror_group_emulator KEY to avoid a two-file rename. The pair needs a new DevicePickerPanel parameter and is noted as a cosmetic upgrade path.
- CARRIED: whether to always-run-with-skip the real-toolchain simctl test vs tag/ignore it. Adopted: always run with a skip — AC-46's measured half is the one honest hardware-coverage claim this feature can make, and gating it behind a manual step throws it away.
- NEW (plan, cheap and available now): the measured fixture of this host's `xcrun simctl list devices available` and `idb list-targets` output does not exist yet. Step 1 captures it; everything in step 7 depends on it.
- NEW (plan): whether the iOS simulator row should carry its runtime ('iOS 18.1') in MirrorRow.detail. This plan says yes — it is additive, does not touch the Android branch, and it is the only thing that disambiguates two identically-named simulators. The Android-side gap (PP-TD-9) stays open under its own spec's gate.

### readyForBacklog

true
