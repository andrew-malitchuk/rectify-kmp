# ios-platform-support — plan

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | plan |
| status | done |
| producedBy | sdd-build-plan-refine |
| schemaVersion | 1 |
| createdAt | 2026-10-19T02:56:19Z |
| updatedAt | 2026-10-19T05:52:37Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T02:56:19Z

## Provenance

### selfHash

69edd9e6f6f5eca6b6ef71fc602ede32817013db2ad2ab98db859ce0f36f07d0

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 3557f6c6047c978f16b9e48604e4d7f3d911bbca0185436a0d3ca4d08214371a |
| plan/evaluation.json | d2b35d45fc568ac1b8ce319641552cf21f2d924fb5d70218487cecf69ca44ba5 |

## Data

### meta

#### title

iOS Platform Support (F-10) — platform-dispatch seam + simctl/idb adapter

#### version

2

#### status

final

### goal

Make PlatformModel.Ios a real, working branch of the existing Android mirror loop on the macOS desktop host: iOS simulator + physical-device discovery via xcrun simctl and idb, live screenshot-poll mirroring through the UNCHANGED ScreencapMirrorTransport, and the two already-drawn AC-31 error cards (simulator-not-booted, device-not-trusted) reachable for the first time. Hierarchy capture stays deferred to v1.1.

### problemStatement

Picking iOS today kills the session and shows a static 'deferred' string (MirrorStage.IosDeferred, derived at MirrorContract.kt:358, rendered at MirrorContent.kt:131-137) — no discovery, no mirroring, and the two drawn iOS error-card designs are unreachable. Worse, MirrorContent computes errorCard() BEFORE the stage `when` (MirrorContent.kt:124-128: `if (card != null) MirrorErrorStage(...) else when (state.stage)`), errorCard() is platform-blind, and the discovery poll keeps issuing adb while iOS is selected — so picking iOS on a machine with no Android device would render the ANDROID 'no device' card. That shadowing must be fixed on both halves before any iOS error state can ever render. Underneath, the PRD's claim that 'only the device-adapter layer is new' is half true: DeviceRepository has no platform parameter, Koin binds one unqualified instance per datasource interface, adb is hardcoded in four load-bearing places (buildDeviceArgv's positional `-s` at DeviceArgv.kt:60-61, DeviceCommand's literal adb shell lines in DeviceArgv.kt's private `arguments()`, DeviceRepositoryImpl's unconditional adbHostSource in poll() plus the PollFailed catch's hardcoded `ToolKind.Adb.binaryName` at DeviceRepositoryImpl.kt:146, and the unqualified Koin bindings in DeviceSourcesProvider.desktop.kt), and ToolKind/DeviceCommand/HostCommand are documented closed sets. F-10 is therefore 'extract a dispatch seam, then add the actual'.

### highLevelDescription

Two commits. COMMIT 1 is a pure refactor with ZERO iOS code: add `platform: PlatformModel` to exactly five DeviceRepository methods (observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats) and let it ripple through 5 use-case interfaces, 5 use-case impls, MirrorViewModel and the test fakes. DeviceRepositoryImpl accepts the parameter and ignores it. 20 files, reviewable as a `git diff --word-diff` whose only insertions are `platform`, `state.platform`, `PlatformModel.Android`, two import lines and one `@Suppress("UNUSED_PARAMETER")`. The existing Android suite green IS AC-49. COMMIT 2 adds iOS: ToolKind.Simctl + widened DeviceCommand/HostCommand, a generalized buildDeviceArgv with a SELECTOR-SCOPED structural NFR-5 check, two hand-rolled parsers (parseSimctlDevices / parseIdbTargets), one IosHostSource replacing the deleted probe-only IdbHostSource, an IosDeviceCommandExecutor that maps the REUSED DeviceCommand.Screencap onto `xcrun simctl io <udid> screenshot --type=png -` / idb, qualified named("android")/named("ios") Koin bindings for DeviceCommandExecutor and MirrorTransport only (in all THREE provider actuals, not just desktop), DeviceRepositoryImpl's exhaustive `when` dispatch with poll() split into pollAndroid()/pollIos(), two new DiscoveryModel variants, the shadowing fix (platform as a flatMapLatest key + platform-correct card copy via a new MirrorErrorCard.platform field), the two MirrorErrorKind members with ZERO new intents, and the picker relabel. ScreencapMirrorTransport, ProcessRunner, MirrorStatsModel and MirrorIntent are edited ZERO times.

### background

F-2/F-3 shipped the Android device adapter with clean, already-neutral interfaces: DeviceCommandExecutor.run(DeviceId, DeviceCommand): ByteArray, MirrorTransport.frames(DeviceId): Flow<MirrorFrame> + stats, and a ScreencapMirrorTransport that depends only on DeviceCommandExecutor and never names adb (verified: it calls trimToPngSignature then reads IHDR width/height at offsets 16/20 OF THE TRIMMED array, so any tool-emitted prefix is already tolerated). F-9 proved the whole measure/verify/diff stack below HierarchySnapshotModel is rect- and pixel-neutral. ProcessRunner is a single internal object with run()/spawnDetached() that knows nothing about adb. The missing piece is platform dispatch — which lives nowhere today. The iOS tooling probe (2026-10-19) measured this host: Xcode 26.3 (build 17C529) with runtimes iOS 17.5 and 18.1, idb at ~/.local/bin/idb, idb_companion at /opt/homebrew/bin/idb_companion, and NO simulator booted — the natural, free reproduction of AC-31's simulator-not-booted half.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/ios-platform-support/inputs/codebase-recon.md | inputs/codebase-recon.md |
| code | .pipeline/sdd/docs/ios-platform-support/inputs/ios-tooling-probe.md | inputs/ios-tooling-probe.md |
| code | .pipeline/sdd/docs/ios-platform-support/preparation/preparation.json | preparation/preparation.json |
| code | .pipeline/sdd/docs/ios-platform-support/preparation/brainstorm.json | preparation/brainstorm.json |
| code | .pipeline/sdd/docs/ios-platform-support/preparation/clarify.json | preparation/clarify.json |
| code | .pipeline/sdd/docs/ios-platform-support/plan/candidates/candidate-1.json | plan/candidates/candidate-1.json |
| code | .pipeline/sdd/docs/ios-platform-support/plan/evaluation.json | plan/evaluation.json |
| code | .pipeline/sdd/docs/registry/bugs.json | registry/bugs.json |
| code | .pipeline/sdd/docs/registry/components.json | registry/components.json |
| code | .pipeline/sdd/docs/registry/features.json | registry/features.json |

### scope

#### in

- COMMIT 1 — platform-dispatch extraction as a standalone pure refactor: `platform: PlatformModel` added to the five dispatching DeviceRepository methods and threaded through 5 use-case interfaces, 5 use-case impls, MirrorViewModel, MirrorFakes, MirrorViewModelVerifyTest's inline DumpHierarchyUseCase fake and four data-repository-impl device test files. 20 files. Zero iOS code, zero new tests.
- ToolKind.Simctl("xcrun", null); DeviceCommand.SimulatorBoot; HostCommand.{XcrunVersion, ListSimulators, ListTargets} — explicit, KDoc-documented, visible API widening of closed sets. (HostCommand.IdbVersion CUT: nothing renders an idb version.)
- buildDeviceArgv(tool, toolPath, device, command): per-ToolKind selector branch (adb `-s` prefix / simctl positional-after-subcommand / idb `--udid`) with the NFR-5 invariant asserted over the SELECTOR SEGMENT ONLY, not the whole argv — a payload token that happens to equal the udid (GetProp's free text) is not a second target and must not trip a `check()`.
- Hand-rolled parseSimctlDevices over `xcrun simctl list devices available` plain text and parseIdbTargets over `idb list-targets` rows, with fixtures captured from THIS host (Xcode 26.3). No new dependency.
- IosHostSource (probeSimctl/probeIdb/listSimulators/listDevices — FOUR methods; boot is a DeviceCommand, not a host command) REPLACING the probe-only IdbHostSource, which is deleted with IdbHostSourceImpl and UnsupportedIdbHostSource.
- IosDeviceCommandExecutor dispatching the REUSED DeviceCommand.Screencap and the new SimulatorBoot to simctl/idb; ScreencapMirrorTransport registered twice and edited zero times; ProcessRunner edited zero times.
- Qualified named("android")/named("ios") Koin bindings for DeviceCommandExecutor and MirrorTransport ONLY, in ALL THREE provideDeviceSources() actuals (desktop, android, ios), using explicit `single<T>(named(...)) { ... get(named(...)) }` per koin-di.md. No unqualified binding of those two types survives anywhere.
- DeviceRepositoryImpl: exhaustive `when (platform)` selection, poll() split into pollAndroid() (moved verbatim) and pollIos() (new), detectTools() re-pointed from idbHostSource.probe() to iosHostSource.probeSimctl(), the private `adbVersion` flow renamed `toolVersion` and fed by whichever platform polled, and the PollFailed catch's hardcoded ToolKind.Adb.binaryName fixed to name the platform's own tool.
- DiscoveryModel.SimulatorNotBooted and DiscoveryModel.DeviceNotTrusted (unprefixed, SUCCESS values, never Failure).
- Shadowing fix, BOTH halves, corrected from the draft: (a) STRUCTURAL — the discovery collection already runs through `sessionDeviceId.flatMapLatest { ... observeDiscovery() }`, so platform joins it as a second flatMapLatest key (`combine(sessionDeviceId, discoveryPlatform)`); flatMapLatest cancels the previous inner flow for free and no hand-rolled Job is introduced. selectPlatform() writes discoveryPlatform and resets discovery = Detecting / rows = emptyList() in the same reduction. (b) COPY — MirrorErrorCard gains `platform: PlatformModel = Android`, and MirrorErrorStage renders iOS-worded title/body and iOS-safe actions for the SHARED ToolMissing/NoDevice/PollFailed/MultipleDevices kinds. The draft's 'errorCard() returns null for Android-only variants when platform == Ios' is REJECTED: those variants are not Android-only (iOS can genuinely have no simctl and no devices), and returning null would leave iOS with a blank stage.
- errorCard()'s picker-yields guard extended to the two new variants, or 'Choose another' / 'Use simulator' are dead buttons: the card is computed BEFORE the stage `when`, so OpenPicker would set isPickerOpen = true while the card kept the stage. SimulatorNotBooted yields to the picker unconditionally; DeviceNotTrusted yields only when simulatorsAvailable.
- Deletion of MirrorStage.IosDeferred, its derivation branch (MirrorContract.kt:358), its render branch (MirrorContent.kt:131-137), the mirror_ios_deferred string and its import, plus the three commonTest files that assert on it (MirrorViewModelAnnotationTest:287, MirrorViewModelSessionTest:165, VerifyStageDerivationTest:62) and presentation-feature-mirror/CLAUDE.md's stage list.
- MirrorErrorKind.SimulatorNotBooted / DeviceNotTrusted with their MirrorErrorCard content and ~12 new strings; ZERO new intents — 'Boot simulator' is MirrorIntent.BootRow(udid), 'Choose another' and 'Use simulator' are MirrorIntent.OpenPicker, "I've tapped Trust — retry" is MirrorIntent.RetryDiscovery.
- A platform-aware boot target: `bootTargetOf(row) = if (platform == Ios) row.id else row.avdName ?: row.id`, used by commit(), bootRow() and bootSelectedAvd(). Without it the iOS Boot path boots by simulator NAME, which is ambiguous across runtimes — EmulatorModel.toRow sets avdName = avdName and bootRow() currently calls `boot(row.avdName ?: row.id)`.
- EmulatorModel.runningDeviceId carries the udid even for a SHUT-DOWN simulator (KDoc widened, no new field), so row.id is the udid and bootTargetOf resolves it.
- Picker: platformCards()'s hardcoded `enabled = platform == Android` replaced; mirror_group_emulator's VALUE relabelled platform-neutral (key kept); MirrorRowGroup{Physical,Emulator} and MirrorRowKind.Boot reused unchanged.
- Unsupported* stand-ins: UnsupportedIosHostSource added, UnsupportedIdbHostSource deleted, and the existing UnsupportedDeviceCommandExecutor / UnsupportedMirrorTransport registered under BOTH qualifiers in DeviceSourcesProvider.android.kt AND DeviceSourcesProvider.ios.kt — two files the draft's change list omitted entirely.

#### out

- iOS view-hierarchy capture / AX JSON parsing and the rawDump/hierarchyXml rename — deferred to v1.1 (FR-57). iOS degrades to "" for free via the Android-gated attachedHierarchyXml.
- idb video-stream high-fps mirroring and its H.264/MJPEG decode — needs a second ProcessRunner entry point (stream(argv): Flow<ByteArray>) and a second transport, destroying the 'ScreencapMirrorTransport unchanged' saving. Screenshot polling only in v1.
- :ios-application (an Xcode project, not a Gradle module) — F-10 is a desktop-host capability per platform-coverage.md. Zero files touched there.
- :compose-application — nothing testable may land there (no test wiring by design). RectifyWindow.kt's PLATFORM_SOURCES already lists iOS.
- Windows/Linux builds (NFR-10).
- kotlinx-serialization-json in data-device-api — moot while hierarchy capture is deferred; `-j` JSON stays the documented escape hatch.
- Simulator lifecycle beyond boot (create/delete/erase/reset), and auto-starting idb_companion.
- Renaming bootEmulator to bootTarget (6 files for zero behavioural value; KDoc carries the truth).
- CUT from the draft — MirrorIntent.BootSimulator(udid): redundant with the existing BootRow(id) once bootTargetOf() exists. Zero new intents is strictly better than one.
- CUT from the draft — MirrorStatsModel.simctlVersion: the model's field is ALREADY platform-neutral (`toolVersion`); only DeviceRepositoryImpl's PRIVATE `adbVersion` flow is adb-shaped. Renaming the private val is zero API change, so no domain-core edit and no new field.
- CUT from the draft — HostCommand.IdbVersion and ToolKind.Idb.readVersion: no surface renders an idb version; ToolResolver's isExecutable check already answers 'is idb present'. One fewer closed-set member and one fewer process spawn per probe.
- CUT from the draft — IosHostSource.bootSimulator() and IosHostSourceImplTest: boot is device-scoped so it belongs on DeviceCommandExecutor (and is then covered by the argv sweep); a desktopTest over IosHostSourceImpl would have to spawn a real process, which always times out inside runTest's virtual clock.
- Platform-specific picker group wording ('Emulators' / 'Simulators') — needs a new DevicePickerPanel parameter for a cosmetic win.
- A Compose UI-test harness. None exists in the repo.

### assumptions

- AS-1 — A Mac host with Xcode command-line tools, simctl, idb and idb_companion installed is the only supported dev/build environment for F-10, consistent with NFR-10's macOS-only scope.
- AS-2 — idb's list-targets output can distinguish booted/shutdown simulators and trusted/untrusted physical devices; the exact field/error signal for 'untrusted' is confirmed during implementation and contained in ONE named token set inside parseIdbTargets so it is correctable from one place.
- AS-3 — No environment available to this project has a real untrusted iPhone; device-not-trusted coverage is accepted as fixture-driven per NFR-18 rather than blocking the feature.
- AS-4 — Hierarchy capture on iOS is out of scope; the existing Android-gated attachedHierarchyXml behaviour (empty string on iOS) is sufficient (FR-57).
- PLAN-AS-1 (C-1, VERIFIED) — Preparation describes commit 1 as 'qualified Koin bindings + platform threaded through'. Those cannot both be zero-iOS-code: a named("ios") binding needs an iOS implementation to bind, and binding the Android impl under both qualifiers temporarily is dead code. Only the signature threading lands in commit 1; the qualifier registration lands in commit 2. REVIEWABILITY RE-CHECKED AGAINST THE REAL TREE AND QUALIFIED: the word-diff is NOT purely the token `platform`. It is `platform` (declarations), `state.platform` (the four intent-scoped MirrorViewModel call sites), `PlatformModel.Android` (the TWO flow-collection sites — observeDiscoveryWhileIdle and observeStats run outside `intent {}` and cannot read `state`, so commit 1 passes the literal and commit 2 replaces it with the flatMapLatest key), two import lines, and one `@Suppress("UNUSED_PARAMETER")` on DeviceRepositoryImpl. Those five shapes are still mechanically reviewable and still prove 'no logic changed' — but AC-49's evidence must be stated as those five shapes, not as 'only platform insertions', or the claim is falsifiable on first inspection.
- PLAN-AS-2 (C-2, RECOUNTED) — Preparation's '~45 files' is not the refactor's number and the draft's 19 was one short. Recounted against the real tree: COMMIT 1 is 20 files (the draft's 19 plus presentation-feature-mirror/commonTest/MirrorViewModelVerifyTest.kt, which implements DumpHierarchyUseCase inline at line 201 and therefore cannot compile without the new parameter). COMMIT 2 is ~55 file-level changes (the draft's 47 plus DeviceSourcesProvider.android.kt, DeviceSourcesProvider.ios.kt, MirrorViewModelAnnotationTest.kt, MirrorViewModelSessionTest.kt, VerifyStageDerivationTest.kt and three module CLAUDE.md files, minus MirrorStatsModel.kt which the simctlVersion cut removes). ~9 files overlap, so the spec touches ~66 distinct files. VERDICT: 20 is the honest refactor cost; ~45 was a whole-spec guess attached to the wrong half; 19 undercounted the test ripple.
- PLAN-AS-3 — A measured fixture of this host's `xcrun simctl list devices available` and `idb list-targets` output does not exist yet. Capturing it is cheap, available now, and is a hard prerequisite for the parser tests.
- PLAN-AS-4 (NEW, from check 3) — `xcrun simctl io <udid> screenshot --type=png -` writes a clean PNG to stdout and is VERIFIED-BY-DESIGN safe for the unchanged transport either way: trimToPngSignature runs first and the IHDR offsets 16/20 are read from the TRIMMED array, so even a prefixed warning line costs nothing. `idb screenshot`'s stdout contract is NOT verified — idb's CLI has historically taken a destination path rather than `-`. Contained: if `-` is rejected, IosDeviceCommandExecutor writes to a temp file and returns its bytes. ScreencapMirrorTransport still sees ByteArray and is still edited zero times, and buildDeviceArgv's selector invariant is unaffected (a temp path is not a device id). SC-26 is code-review-only regardless, so this does not gate the feature.

### constraints

| type | text |
| --- | --- |
| tech | iOS adapter classes are bound inside the existing provideDeviceSources() actuals; no new expect/actual is introduced. The repo's convention is expect/actual for the DI REGISTRATION FUNCTION only — plain interface + Koin binding for behaviour. |
| tech | ToolKind/DeviceCommand/HostCommand widening must be an explicit, documented, visible API change per their KDoc contract. Reusing DeviceCommand.Screencap for iOS is honoured by REWRITING its KDoc to the platform-neutral meaning it already has ('capture the screen as an encoded PNG on stdout'), not by silently overloading an Android-shaped case. data-device-api/CLAUDE.md's closed-set counts (DeviceCommand 6 cases, HostCommand 5 cases) must be updated in the same commit. |
| tech | buildDeviceArgv keeps enforcing NFR-5 for every ToolKind, but the assertion is SELECTOR-SCOPED. `check(argv.count { it == device.value } == 1)` over the whole argv is a DESIGN BUG: DeviceCommand.GetProp carries free text (`GetProp(name: String)`), so GetProp(name = <the udid>) would make the count 2 and throw IllegalStateException from a pure commonMain function on user-reachable data. The check runs over the selector segment the `when (tool)` produced; the payload is covered separately by the connect/-H/-P denylist. |
| tech | platform-coverage.md — ALL idb/simctl process invocation and every iOS datasource implementation lives exclusively under data-device-impl/src/desktopMain/. data-device-impl's commonMain names no java.* and no process API. There is no macosMain. |
| tech | error-handling.md — a missing tool or an empty device list is a SUCCESS value, never an exception. The iOS cards ride the MirrorErrorCard projection off DiscoveryModel, not Failure. Do NOT copy the recorded repo-wide gap where ShowError(messageId) handlers ignore messageId. |
| tech | The five external-process conventions hold unchanged: ONE process-starting site (ProcessRunner), closed DeviceCommand/HostCommand sets, ONE argv construction point enforcing the device selector, every budget named in MirrorTuning (no new tuning constants), and TIMEOUT_REASON_PREFIX shared producer-to-consumer. |
| tech | koin-di.md — one module per Gradle module, load order significant; provideDeviceSources() stays the single registration seam; qualified registrations use explicit `single<T>(named(...)) { ... }` rather than singleOf. ScreencapMirrorTransport is already registered with explicit `single` (not singleOf) because its clock parameter has a default — that stays true for both qualified instances. |
| tech | package-layout.md + explicitApi() — core/ is internal-only, source/ is public with one subfolder per semantic concern, explicit visibility and explicit return types everywhere. data-device-api has no core/ and must not grow one. |
| tech | testing.md — the desktop target is the one that runs, but a cross-target build must pass: commonTest compiles for iosArm64 and Kotlin/Native rejects a backtick test name containing a comma or a colon (an apostrophe is fine, measured; presentation-feature-mirror/CLAUDE.md is stricter still and asks for plain camelCase names in that module). Nothing testable may land in :compose-application. A ProcessRunner spawn inside runTest's virtual clock ALWAYS times out. |
| tech | mappers.md — Mapper<I,O> is a fun interface and a mapper NEVER throws; unknown values get a safe default in toModel. SimulatorResourceMapper and both new parsers obey this: an unparseable line is skipped, never fatal. |
| business | Priority is `could`, phase PH-2, estimate L. The smallest scope satisfying AC-31/SC-14 is deliberately preferred over the full goal-statement parity list. |
| time | Effort tier fast; single drafter, no judge. The two-commit sequencing is the risk control that replaces a longer review cycle. |

### dependencies

| on | note |
| --- | --- |
| F-2/F-3 (Android device discovery & mirroring) | Merged. Supplies the five datasource interfaces, ProcessRunner, ToolResolver/probeCandidates, ScreencapMirrorTransport, DeviceArgv, the hand-rolled-parser precedent and the DiscoveryModel contract. registry: features.json#android-device-discovery-mirroring. |
| F-6 (Error handling & recovery) | Merged. Supplies MirrorErrorKind/MirrorErrorCard, the pure DiscoveryErrorProjection (errorCard/indicatorDot/dropBanner), MirrorErrorStage and the ErrorCard organism. registry: features.json#error-handling-and-recovery. |
| F-9 (Verify & re-diff loop) | Merged. The measure/verify/diff stack is rect/pixel-neutral and needs zero change for iOS — the reason deleting MirrorStage.IosDeferred is safe. Note VerifyStageDerivationTest asserts on the IosDeferred constant and must be edited. |
| Xcode command-line tools (xcrun/simctl), idb, idb_companion | Probed present on this host: Xcode 26.3 build 17C529, runtimes iOS 17.5 and 18.1, idb at ~/.local/bin/idb, idb_companion at /opt/homebrew/bin/idb_companion. Whether idb_companion is RUNNING and whether idb is authorised against a physical device are separate, unprobed questions. |
| A captured fixture of this host's `xcrun simctl list devices available` and `idb list-targets` output | Prerequisite for the parser tests (step 6). Cheap and available now; capture it before writing the parsers so the tests pin real output. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| AC-49 regression: the five-signature change ripples through 20 files before any iOS behaviour exists. A mechanical edit at that scale is where a quiet behaviour change hides. | med | high | Land it as its own commit with ZERO iOS code and ZERO new tests. DeviceRepositoryImpl's bodies stay untouched (commit 1 does not even split poll()). Review as a pure refactor; the word-diff must contain only the five token shapes named in PLAN-AS-1. |
| The NFR-5 `check()` as drafted is tripped by data, not by a programming error: GetProp's free text can equal the udid, making the whole-argv count 2 and throwing from a pure commonMain function. | low | high | Scope the assertion to the selector segment the per-tool `when` produced. The sweep test adds an explicit regression case — buildDeviceArgv(Adb, path, device, GetProp(name = device.value)) must build a valid argv and NOT throw — alongside the mutation check. |
| 'Choose another' / 'Use simulator' are dead buttons: errorCard() is computed BEFORE the stage `when`, so OpenPicker sets isPickerOpen while the card keeps the stage. | high | high | Extend errorCard()'s existing picker-yields guard (today: Found and PollFailed) to SimulatorNotBooted unconditionally and to DeviceNotTrusted when simulatorsAvailable. Asserted in DiscoveryErrorProjectionTest: with isPickerOpen = true and each iOS variant, errorCard() returns null. |
| The iOS Boot path boots by simulator NAME. EmulatorModel.toRow sets avdName = avdName and bootRow() calls boot(row.avdName ?: row.id); 'iPhone 16' exists under both iOS 17.5 and 18.1 on this host, so the boot targets the wrong runtime or fails. | high | high | One `bootTargetOf(row)` helper used by commit(), bootRow() and bootSelectedAvd(): the udid (row.id) on iOS, the AVD name on Android. This is the concrete consumer that makes 'runningDeviceId carries the udid even when shut down' load-bearing rather than decorative. bootSelectedAvd() additionally resolves its name back to a row first, which fixes the NoSignal-stage path on iOS where DiscoveryModel.Found leaves avds populated with simulator names. |
| `simctl list devices` plain-text format drifts with an Xcode release and parseSimctlDevices silently returns an empty list, rendering as 'no simulators' rather than a visible failure. | med | med | The parser never throws, but pollIos() distinguishes 'command succeeded, zero rows parsed, NON-EMPTY stdout' and emits PollFailed(tool = "xcrun", reason = <stdout head>) instead of SimulatorNotBooted. `-j` JSON is the documented escape hatch. |
| `idb list-targets` hangs or fails without a running idb_companion, stalling the whole iOS poll once per interval. | high | med | Budgeted by MirrorTuning.HostCommandTimeoutMs (3s). The idb call is wrapped so its failure yields an EMPTY physical list and never aborts the simctl half. Deliberately no companion auto-start. |
| `simctl boot` against an already-booted simulator exits non-zero and would be reported as a failure. | high | med | Key on the STDERR TOKEN, never on the exit code: CoreSimulator's message is 'Unable to boot device in current state: Booted' (SimError domain). DeviceCommandException already carries (argv, exitCode, stderrTail), so the repository swallows exactly that failure and rethrows every other. The token lives in ONE named constant, the same containment AS-2 gets. The exit-code NUMBER is deliberately not asserted anywhere — it was not measured on this host (no simulator was booted at probe time). |
| Deleting MirrorStage.IosDeferred routes iOS through the full stage machine, exposing every stage to a platform none were written against. | med | med | That exposure IS the feature, and the stack below HierarchySnapshotModel is already rect/pixel-neutral. The one iOS-shaped hole is hierarchy, which is Android-gated and degrades to "" — made an ASSERTED behaviour by AC-50's never-reach-the-executor test. |
| iOS mirroring is materially slower than ~10fps and the feature ships feeling broken. `simctl io screenshot` plausibly costs 200-400 ms. | med | low | Measure once a simulator is booted and record the NUMBER in the verification table. Do NOT add an iOS tuning constant. The adaptive single-flight loop degrades gracefully and TransportStats reports the truth. |
| AS-2 unresolved: nobody has measured what `idb list-targets` prints for an untrusted physical iPhone. | med | med | Contain, do not guess. One named token set inside parseIdbTargets plus a fallback classifying a listed-but-unusable physical target as untrusted. The fixture test pins whatever is decided. |
| AC-31's device-not-trusted half and the whole SC-26 physical path ship fixture-driven / code-review-verified only (AS-3). | high | med | Write the waiver EXPLICITLY into the verification table; do not block the feature on it (NFR-18). AC-31's two halves are recorded separately in the AC text, the traceability matrix AND the coverage goal so the simulator-not-booted measurement is never read as proving both. |
| The qualified Koin bindings fail to resolve when the app runs on Android or iOS HARDWARE, because DeviceSourcesProvider.android.kt and DeviceSourcesProvider.ios.kt still register UNQUALIFIED DeviceCommandExecutor/MirrorTransport. This is a startup crash on a phone, not a desktop test failure — and the draft's change list omitted both files. | high | high | FR-56: edit all three actuals together. Each non-desktop actual registers AdbHostSource, EmulatorSource, IosHostSource (new stand-in) unqualified plus DeviceCommandExecutor and MirrorTransport under BOTH named("android") and named("ios") — seven registrations, up from five. No unqualified binding of those two interfaces survives in ANY actual, so a forgotten qualifier is a resolution failure the extended DeviceSourcesModuleTest catches rather than a silent fallback. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-25 | (v1.1+) Support iOS simulator/device mirroring via idb+simctl, including simulator-not-booted and device-not-trusted error-card states. |
| FR-48 | Extract a platform-dispatch seam: thread `platform: PlatformModel` through DeviceRepository's methods (sourced from MirrorViewModel's existing state.platform) and register qualified Koin bindings (named("android") / named("ios")) in the provideDeviceSources() actuals. PLAN REFINEMENT: `platform` lands on exactly the FIVE dispatching methods (observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats). detectTools/refreshDiscovery/restartAdbServer/setAdbPath are deliberately unchanged — re-verified against their real call sites: detectTools probes every platform, refreshDiscovery is a conflated Channel nudge with nothing to dispatch, and restartAdbServer/setAdbPath are Android-only affordances that must stay UNREACHABLE from the iOS surface. Qualifiers are registered for DeviceCommandExecutor and MirrorTransport ONLY — AdbHostSource, EmulatorSource and IosHostSource have one implementation each and stay unqualified. |
| FR-49 | Add a `ToolKind.Simctl` (xcrun-based) member and widen `DeviceCommand`/`HostCommand` with iOS-shaped variants as an explicit, documented, visible API change. PLAN REFINEMENT: screenshot REUSES DeviceCommand.Screencap (KDoc rewritten platform-neutral); the genuinely new members are DeviceCommand.SimulatorBoot and HostCommand.{XcrunVersion, ListSimulators, ListTargets}. HostCommand.IdbVersion is cut — no surface renders it. |
| FR-50 | Generalize `buildDeviceArgv` so the device-selector flag is chosen per `ToolKind` while preserving the NFR-5 invariant that the argv always embeds the one explicitly selected, connected device id. PLAN REFINEMENT: simctl's selector is POSITIONAL-AFTER-SUBCOMMAND (`simctl io <udid> screenshot ...`), not `-u`; and the invariant is asserted over the SELECTOR SEGMENT, not the whole argv, because GetProp's free text can legally equal the udid. |
| FR-51 | Implement iOS discovery polling (booted/unbooted simulators and connected physical devices via idb/simctl) producing new DiscoveryModel variants for simulator-not-booted and device-not-trusted, replacing the static MirrorStage.IosDeferred stage when platform == Ios. |
| FR-52 | Fix the errorCard() shadowing bug. PLAN REFINEMENT, two halves: (a) the discovery poll stops issuing adb once platform == Ios because platform becomes a second flatMapLatest key on the existing `sessionDeviceId.flatMapLatest { observeDiscovery() }` collection — flatMapLatest cancels the previous inner flow, so no hand-rolled Job is introduced; (b) the SHARED DiscoveryModel variants render platform-correct copy, tool name, command line and actions via a new `MirrorErrorCard.platform` field, instead of the draft's return-null guard which would have left iOS with a blank stage. errorCard()'s picker-yields guard is extended to the two new variants so the secondary actions are not dead buttons. |
| FR-53 | Add `MirrorErrorKind.SimulatorNotBooted` and `MirrorErrorKind.DeviceNotTrusted` with their `MirrorErrorCard` content and MirrorViewModel wiring, satisfying AC-31. PLAN REFINEMENT, re-verified at the real call sites: ZERO new intents. 'Boot simulator' is `MirrorIntent.BootRow(card.primaryDeviceId)` — bootRow() already looks the row up in state.rows and boots it, and bootTargetOf() makes it resolve the udid on iOS. 'Choose another' and 'Use simulator' are `MirrorIntent.OpenPicker` (handler: setPickerOpen(true)). "I've tapped Trust — retry" is `MirrorIntent.RetryDiscovery` (handler: refreshDiscovery(), which cuts the poll interval short). FR-53 named four intents; none of them is new. |
| FR-54 | Implement iOS live mirroring by generalizing DeviceCommandExecutor to dispatch iOS screenshot commands (`xcrun simctl io <udid> screenshot --type=png -` for simulators, idb for physical devices) and reusing `ScreencapMirrorTransport` unchanged. Verified unchanged-safe: the transport trims to the PNG signature before reading IHDR offsets 16/20, so a tool-emitted prefix costs nothing. |
| FR-55 | Extend the device picker: reuse `MirrorRowGroup{Physical,Emulator}` and `MirrorRowKind.Boot` unchanged for iOS rows; replace `platformCards()`'s hardcoded `enabled = platform == Android`; relabel `mirror_group_emulator`'s value to a platform-neutral string. PLAN ADDITION: the reuse is only correct once the boot TARGET is platform-aware (bootTargetOf), and the iOS simulator row carries its runtime in `detail` so two identically-named simulators are distinguishable. |
| FR-56 | Add `Unsupported*` stand-ins so the Android/iOS app Koin graphs still resolve. PLAN REFINEMENT: this is THREE files, not one — UnsupportedDeviceSources.kt gains UnsupportedIosHostSource and loses UnsupportedIdbHostSource, and BOTH DeviceSourcesProvider.android.kt and DeviceSourcesProvider.ios.kt go from five registrations to seven (the two qualified executor/transport pairs). |
| FR-57 | Leave iOS view-hierarchy capture as today's no-op: `attachedHierarchyXml` stays Android-gated and `HierarchySnapshotModel.rawDump` remains "" on iOS — deferred to v1.1. dumpHierarchy(Ios, ...) short-circuits to HierarchySnapshotModel.Empty IN THE REPOSITORY without spawning a process and without throwing. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. STRENGTHENED by this spec: today's guarantee is POSITIONAL (`listOf(toolPath, "-s", device.value) + command.arguments()`), enforced only by the shape of one line, and nothing stops a future arguments() branch appending a second target. After this spec it is STRUCTURAL — buildDeviceArgv builds a per-tool selector segment in a `when (tool)` and asserts the selected id appears exactly once IN THAT SEGMENT, backed by an exhaustive ToolKind x DeviceCommand sweep that also asserts argv[0] == toolPath and that no element matches the connect/-H/-P denylist. The assertion is deliberately NOT over the whole argv: DeviceCommand.GetProp carries free text, so a whole-argv count would turn a legal payload into an IllegalStateException from a pure function. NFR-5 ends up stronger AND un-trippable by data. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable. FREE for iOS: pickerKeyAction / nextSelectableIndex / the single onPreviewKeyEvent + FocusRequester path know nothing about adb. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system with sufficient contrast in both themes. FREE for iOS: the two new cards render through the existing ErrorCard organism with existing tokens; covered by @Preview pairs in light and dark. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |
| NFR-18 | testability | AC-31's simulator-not-booted half must be verified against the real simctl/idb toolchain on a host with no booted simulator (measured feasible per ios-tooling-probe.md); the device-not-trusted half is permitted to be fixture-driven via a constructed DiscoveryModel — documented as a WAIVER since no untrusted physical iPhone is available in this environment. |
| NFR-19 | compatibility | All idb/simctl process invocation and iOS datasource implementations live exclusively under data-device-impl/src/desktopMain; commonMain stays free of java.* and process APIs, and :ios-application is untouched. |
| NFR-20 | reliability | The platform-dispatch extraction (FR-48) must not change observable Android behavior — identical DiscoveryModel outputs, error cards, and mirroring characteristics — verified by the existing Android test suite remaining green (AC-49). |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-31 | Given iOS is selected as platform, when the simulator isn't booted, then an error card with 'Boot simulator' primary / 'Choose another' secondary renders; when the device isn't trusted, an error card with "I've tapped Trust — retry" primary / 'Use simulator' secondary renders. COVERAGE IS SPLIT AND MUST BE READ AS SPLIT: (a) SIMULATOR-NOT-BOOTED is REAL AND MEASURED on this host (no simulator booted at probe time; simctl and idb installed) — the projection assertion is pure commonTest and the underlying state is produced from real `xcrun simctl list devices available` output in the guarded desktopTest; (b) DEVICE-NOT-TRUSTED is FIXTURE-DRIVEN AND WAIVED per NFR-18/AS-3 — no untrusted physical iPhone exists here, so a constructed DiscoveryModel.DeviceNotTrusted drives the projection and nothing about real idb trust behaviour is proven. Half (a)'s measurement must never be reported as covering half (b). Both halves additionally require the picker-yields guard, or their secondary actions do nothing. | must | test |
| AC-46 | Given iOS is selected and discovery runs, when no simulator is booted (measured on a real host via simctl/idb), then DiscoveryModel reflects the iOS-specific simulator-not-booted state and the Android 'no device' card does not render instead (shadowing fix verified on both halves: the adb flow is structurally cancelled by the flatMapLatest key, and any shared variant renders iOS copy). | must | test |
| AC-47 | Given iOS is selected and the error card shows 'Boot simulator', when the user clicks it, then the app boots the selected simulator BY UDID and discovery re-polls and surfaces it as selectable without an app restart. An already-booted simulator's non-zero exit is treated as success on its stderr token, not on its exit code. | must | test |
| AC-48 | Given iOS is selected and a simulator or trusted physical device is available, when mirroring starts, then live frames render through the generalized DeviceCommandExecutor/ScreencapMirrorTransport path with the same MirrorFrame contract (PNG bytes, width/height, fps stats) Android uses today. | must | demo |
| AC-49 | Given the platform-dispatch extraction lands, when the existing Android device-discovery/mirroring/error-card test suite runs, then every existing Android test still passes with no behavior change. Evidence is commit 1's word-diff containing only the five token shapes named in PLAN-AS-1 — not 'only the token platform', which is not literally true. | must | test |
| AC-50 | Given iOS is selected, when a view-hierarchy dump is requested, then the app returns the same empty hierarchy (rawDump "") it returns today, confirming hierarchy capture is deliberately deferred with no regression and no new wire-field rename. | could | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-14 | (v1.1+) iOS simulator not booted / device not trusted | error | ["Select iOS platform", "Simulator not booted -> error card offers Boot simulator", "Or device not trusted -> error card offers Trust-and-retry / Use simulator"] |
| SC-25 | iOS simulator discovery and mirroring happy path | happy | ["Select iOS platform", "A simulator is already booted and appears in the picker", "Select it", "Live frames render via simctl screenshot polling"] |
| SC-26 | iOS physical device discovery happy path | happy | ["Select iOS platform", "A trusted physical device appears in the picker via idb discovery", "Select it", "Live frames render via idb screenshot (CODE-REVIEW-VERIFIED ONLY — idb has never been exercised against a physical device on this host, and idb screenshot's stdout contract is unverified per PLAN-AS-4)"] |
| SC-27 | iOS simulator-not-booted error recovers via Boot simulator | edge | ["Select iOS platform", "No simulator is booted (measured real state on this host)", "Error card renders Boot simulator / Choose another", "Click Boot simulator -> BootRow(udid) -> bootEmulator(Ios, udid)", "Discovery re-polls and the simulator becomes selectable"] |
| SC-28 | iOS device-not-trusted error (fixture-driven, WAIVED) | error | ["Select iOS platform", "Construct a DiscoveryModel.DeviceNotTrusted fixture (no real untrusted device available)", "Error card renders \"I've tapped Trust — retry\" / Use simulator", "Click Use simulator -> OpenPicker -> errorCard() yields the stage because simulatorsAvailable", "Picker lists the simulator rows"] |
| SC-29 | Android regression check after platform-dispatch extraction | happy | ["Select Android platform", "Run the existing discovery/mirror/error-card flows", "Confirm no behavior change versus the pre-F-10 baseline (AC-49)"] |

### definitionOfReady

- Preparation is signed off with readyForPlan: true and all FR/NFR/AC/SC/AS ids frozen.
- codebase-recon.md is accepted as ground truth for file paths and symbols — this plan re-verified the load-bearing ones against the tree and records the corrections inline.
- ios-tooling-probe.md confirms xcrun/simctl (Xcode 26.3), idb and idb_companion present and NO simulator booted on the build host.
- A real fixture of this host's `xcrun simctl list devices available` and `idb list-targets` stdout has been captured and committed under data-device-api/src/commonTest (PLAN-AS-3).
- The AC-31 coverage split (real simulator-not-booted vs fixture-driven, waived device-not-trusted) is agreed in writing before implementation.
- Agreement that commit 1 ships with zero iOS code, zero new tests, and a word-diff of the five token shapes in PLAN-AS-1 (PLAN-AS-1).

### definitionOfDone

- Commit 1 merged as a standalone pure refactor: 20 files, `git diff --word-diff` showing only `platform` / `state.platform` / `PlatformModel.Android` / two imports / one `@Suppress`, no logic change, no new test.
- The pre-existing Android suite is green after commit 1 AND after commit 2 — AC-49/NFR-20.
- pollAndroid() is a verbatim move of today's poll() body (reviewable as a move, not a rewrite).
- `./gradlew :data-device-api:desktopTest :data-device-impl:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest :domain-usecase-impl:desktopTest` green.
- A cross-target build passes (`:data-device-api:build :data-repository-impl:build :presentation-feature-mirror:build --max-workers=1`, PP-TD-8) — commonTest compiles for iosArm64 and no new backtick test name contains a comma or a colon.
- The exhaustive ToolKind x DeviceCommand argv sweep passes, asserting argv[0] == toolPath, exactly one occurrence of device.value IN THE SELECTOR SEGMENT, no connect/-H/-P element, the documented throw for Android-only commands on iOS tools, and the GetProp(name = udid) regression case building without throwing.
- The guarded real-toolchain desktopTest runs `xcrun simctl list devices available` through ProcessRunner, feeds it to parseSimctlDevices, and asserts simulators found with none Booted — SKIPPING (not failing) when xcrun is absent.
- The Koin graph resolves on desktop with both qualifiers, and the android and ios actuals resolve with both qualifiers too (DeviceSourcesModuleTest extended). No unqualified DeviceCommandExecutor or MirrorTransport binding remains in any actual.
- MirrorStage.IosDeferred, its derivation branch, its render branch, mirror_ios_deferred, IdbHostSource, IdbHostSourceImpl and UnsupportedIdbHostSource are gone from the tree (grep returns nothing, including the three commonTest files and presentation-feature-mirror/CLAUDE.md).
- dumpHierarchy(Ios, ...) returns HierarchySnapshotModel.Empty against a fake executor whose run() THROWS — the test passes only because the executor is never reached (AC-50).
- Measured iOS capture round-trip recorded as a NUMBER in the verification table, with an explicit note if it misses the ~10fps CaptureFloorMs aspiration. No iOS tuning constant added.
- The AC-31 coverage split and the SC-26 physical-device code-review-only status are written into the verification table verbatim, not implied.
- Zero files changed in :compose-application, :ios-application, ScreencapMirrorTransport.kt, ProcessRunner.kt, MirrorStatsModel.kt and MirrorContract's MirrorIntent set.
- data-device-api/CLAUDE.md's closed-set counts, data-device-impl/CLAUDE.md's datasource list and presentation-feature-mirror/CLAUDE.md's stage list are updated in the same commit as the code.
- No new version-catalog entry, no new Gradle module, no new intent, no new tuning constant.

### successMetrics

- iOS screenshot-poll mirroring reaches the same ~10fps capture floor as Android (MirrorTuning.CaptureFloorMs = 100) with no new tuning constants — measured and recorded as a number, treated as an aspiration not a prediction (CaptureFloorMs is itself an unmeasured Android constant, waiver W-2).
- Zero regressions in the existing Android discovery/mirror/error-card test suite after the extraction (AC-49).
- NFR-5 is strictly stronger after this spec than before it, and is not trippable by user data.
- Commit 1's diff is reviewable as a word-diff of five token shapes across 20 files — the stated review-cost control actually holds.
- Net discipline: ZERO new intents, zero new tuning constants, zero new dependencies, zero new Gradle modules, zero new domain-model fields, two interfaces deleted for one added.

### architecture

#### approach

Keep every existing interface and every existing transport/process primitive. Add `platform: PlatformModel` to exactly the five DeviceRepository methods that genuinely dispatch, resolve per-platform implementations through qualified Koin bindings inside the existing registration seams, and let DeviceRepositoryImpl select with an exhaustive `when` (compile-time totality, not a map lookup). Reuse the mechanisms that already exist rather than adding parallel ones: platform becomes a second flatMapLatest key on the discovery collection that is already keyed by sessionDeviceId; 'Boot simulator' becomes the existing BootRow intent with a platform-aware target; the iOS version string rides the existing platform-neutral MirrorStatsModel.toolVersion. Sequence it as two commits: a pure signature refactor with zero iOS code, then the iOS adapter.

#### rationale

Dispatch becomes explicit at the type level — a reader of `mirror(platform, id)` sees which device family it targets. ScreencapMirrorTransport is reused VERBATIM as two Koin instances, so 'iOS mirroring' at the transport layer is zero new code; that is the single largest saving in the spec. There is no hidden mutable platform state in the data layer, so MirrorViewModel's state.platform stays the single source of truth. And `observeDiscovery(platform)` being a PARAMETERISED flow is what forces the adb loop to actually stop when the user switches — fixing half the shadowing bug by construction. The honest cost is 20 mechanical files before any iOS behaviour exists; the diff is a signature change with no logic change, which is exactly what makes the existing Android suite a real regression guard rather than a formality. Three places where the draft would have ADDED machinery are places the codebase already had an answer: flatMapLatest (not a hand-rolled Job), BootRow (not a new intent), toolVersion (not a new field).

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Dispatching decorator over a StateFlow<PlatformModel> | Far smaller diff but it hides platform in mutable shared state: `mirror(deviceId)` reads one thing today and another after a toolbar click with no signature to show it. It introduces a race invisible in the types and still needs a setter path from the ViewModel. Explicitly rejected by the confirmed clarify survey. |
| A single PlatformDeviceSource super-interface collapsing all five datasources into one per platform | Forces a lowest-common-denominator shape on surfaces that genuinely differ (adb has a daemon lifecycle, simctl has none; emulator boot is a detached foreground process, simctl boot is not), rewrites the Android path wholesale putting NFR-20/AC-49 at real risk, and deletes the fine-grained test fakes four test files depend on. |
| A hand-rolled discovery Job cancelled and re-collected in selectPlatform() (the draft's fix) | The collection ALREADY runs inside `sessionDeviceId.flatMapLatest { ... }`, which cancels its inner flow on every key change. Adding a Job alongside it means two cancellation mechanisms on one flow and a new way to leak a collector. Making platform a second key is strictly smaller and strictly safer, and it also removes the draft's worry about resultLauncher swallowing a CancellationException — ObserveDeviceDiscoveryUseCase returns a bare Flow (domain-layer rule), so no resultLauncher is in that path at all. |
| MirrorIntent.BootSimulator(udid) as a new intent (the draft's one addition) | `bootRow(id)` already resolves the row from state.rows and boots it; the error card already carries the udid in MirrorErrorCard.primaryDeviceId; SimulatorNotBooted already projects simulator rows into state.rows. The only thing missing was a platform-aware boot target, which commit() and bootSelectedAvd() need anyway. A second intent that reduces to the same handler is the duplicate someone decodes at 3am. |
| errorCard() returning null for the shared variants when platform == Ios (the draft's guard) | ToolMissing, NoDeviceFound, PollFailed and MultipleDevices are not Android-only states — iOS can genuinely have no simctl, no devices, a failed poll and two targets. Returning null would hand iOS a blank stage for four real conditions. Carrying `platform` on MirrorErrorCard and branching copy in MirrorErrorStage is one data-class field and four `when` arms, and it keeps the projection the single place that decides what the card says. |
| MirrorStatsModel.simctlVersion alongside adbVersion (the draft's addition) | There is no `adbVersion` on the model — it is `toolVersion: String`, already neutral. The adb-shaped name is a PRIVATE MutableStateFlow inside DeviceRepositoryImpl. Renaming a private val is a zero-API change; adding a public domain field for the same information is not. |
| `check(argv.count { it == device.value } == 1)` over the whole argv (the draft's invariant) | GetProp carries free text, so a property name equal to the udid turns a legal call into an IllegalStateException thrown from a pure commonMain function. Scoping the assertion to the selector segment keeps the security property and removes the data-reachable crash. |
| `simctl list devices -j` (JSON) instead of plain text | data-device-api has zero dependencies beyond :data-core and no serialization plugin. The plain-text rows are regex-shaped and match parseAdbDevices' precedent exactly. `-j` stays the documented escape hatch for v1.1. |
| `idb video-stream` for mirroring | Needs a second ProcessRunner entry point (run() buffers stdout to EOF), H.264/MJPEG decode against a contractually-PNG MirrorFrame.bytes, and a second transport. Revisit only if measured iOS screenshot latency proves 10fps unusable. |
| Add `EmulatorModel.bootId: String?` for a shut-down simulator's udid | A new field on a shared domain model for one platform's identifier quirk. Populating the existing runningDeviceId with the udid (KDoc widened) produces exactly the right row through the UNCHANGED EmulatorModel.toRow: id = udid, serial = udid, kind = Boot driven by isRunning. |
| Add DeviceCommand.IosScreenshot as a distinct member | It would force a platform-aware branch INSIDE ScreencapMirrorTransport — the one file this spec most wants to leave alone. |
| Rename bootEmulator to bootTarget | Six files for zero behavioural value; inflates the AC-49 regression surface. KDoc carries the truth. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| DeviceRepository (five methods gain `platform: PlatformModel`) | logic | reuse | features.json#android-device-discovery-mirroring | domain-repository-api/.../source/repository/DeviceRepository.kt. observeDiscovery, mirror, dumpHierarchy, bootEmulator, observeMirrorStats take `platform`. NOTE the real signatures take plain STRING identifiers, not DeviceId (`mirror(deviceId: String)`, `dumpHierarchy(deviceId: String)`, `bootEmulator(avdName: String)`) — validating a string back into a device id is the data layer's job, per the interface's own KDoc. detectTools (probes every platform), refreshDiscovery (a conflated Channel nudge), restartAdbServer and setAdbPath are deliberately unchanged; the last two become explicitly Android-only via widened KDoc and must be UNREACHABLE from the iOS surface — the iOS cards offer no 'Restart adb' / 'Locate adb' action. bootEmulator keeps its name with a widened KDoc (it takes an AVD name on Android and a udid on iOS). |
| DeviceRepositoryImpl (exhaustive `when` dispatch; poll() split; detectTools re-pointed) | logic | reuse | features.json#android-device-discovery-mirroring | data-repository-impl/.../source/DeviceRepositoryImpl.kt. Constructor swaps idbHostSource: IdbHostSource for iosHostSource: IosHostSource and takes qualified android/ios DeviceCommandExecutor and MirrorTransport pairs; private `executor(p)` / `transport(p)` select with an exhaustive `when` over PlatformModel. poll() becomes pollAndroid(avdNames, emptyPolls) — today's body moved VERBATIM — plus pollIos(emptyPolls). detectTools() keeps its signature but swaps `idbHostSource.probe()` for `iosHostSource.probeSimctl()` so the toolbar still reports two probes, one per platform. The private `adbVersion` MutableStateFlow is renamed `toolVersion` and written by whichever poll ran, so observeMirrorStats(platform) needs no new model field. bootEmulator(Ios, udid) must NOT go through AvdName.of — it routes to executor(Ios).run(DeviceId.of(udid), DeviceCommand.SimulatorBoot). dumpHierarchy(Ios, ...) short-circuits to HierarchySnapshotModel.Empty without reaching the executor and without throwing. |
| pollIos() | logic | new |  | The one place iOS discovery semantics live. probeSimctl() -> ToolMissing if absent; listSimulators(); listDevices() wrapped so an idb failure (no idb_companion) degrades to an EMPTY physical list rather than hiding simctl's answer; then classifies into DiscoveryModel.{Found, SimulatorNotBooted, DeviceNotTrusted, ToolMissing, NoDeviceFound}. Distinguishes 'succeeded, zero rows parsed, non-empty stdout' and emits PollFailed(tool = "xcrun", reason = <stdout head>) so parser drift is visible. |
| observeDiscovery's PollFailed catch — tool-name fix | logic | reuse | features.json#android-device-discovery-mirroring | DeviceRepositoryImpl.kt:146 hardcodes `tool = ToolKind.Adb.binaryName` inside `catch (failure: DeviceCommandException)`. On the iOS branch that reports an adb failure for an xcrun one. Takes the polling platform's own binary name. CONFIRMED present in this plan: a genuine latent bug the dispatch seam exposes, and the fix is one expression. |
| buildDeviceArgv(tool, toolPath, device, command) | logic | reuse | features.json#android-device-discovery-mirroring | data-device-api/.../source/parse/DeviceArgv.kt (which also hosts HostCommand and buildHostArgv — one file, not three). The single argv construction point. A `when (tool)` produces the selector segment — adb `[toolPath, "-s", id]` prefix, simctl `[xcrunPath, "simctl", <sub>, id]` positional, idb `[idbPath, <sub>, "--udid", id]` — and the NFR-5 assertion runs over THAT SEGMENT: `check(selector.count { it == device.value } == 1)`. The command payload is appended afterwards and checked only against the connect/-H/-P denylist. Android-only commands (WmSize, WmDensity, GetProp, EmuAvdName, UiAutomatorDump) on an iOS tool throw a documented DeviceCommandException naming the tool and the command rather than returning empty bytes. |
| ToolKind.Simctl("xcrun", null) | data | new |  | The binary is xcrun (at /usr/bin, present with Xcode CLT); the argv branch inserts the `simctl` subcommand. A new candidate entry in probeCandidates keyed off the kind, same mechanism as sdkSubdirectory. ToolKind.Idb is unchanged — its readVersion stays null now that HostCommand.IdbVersion is cut. |
| DeviceCommand.SimulatorBoot + HostCommand.{XcrunVersion, ListSimulators, ListTargets} | data | new |  | Explicit, KDoc-documented widening of two closed sets (each member carries a 'widened for F-10 (FR-49)' line). DeviceCommand.Screencap is REUSED for iOS capture with its KDoc rewritten platform-neutral — that reuse is what keeps ScreencapMirrorTransport untouched. SimulatorBoot is device-scoped on purpose: it needs a udid selector, so it belongs in DeviceCommand where buildDeviceArgv's invariant and the sweep test cover it. |
| parseSimctlDevices | logic | new |  | data-device-api/.../source/parse/SimctlDevicesParser.kt, commonMain, pure. Hand-rolled line scanner over `xcrun simctl list devices available`: `-- iOS 18.1 --` runtime headers, `    iPhone 16 (UDID) (Booted\|Shutdown)` rows. Never throws; an unparseable line is skipped; `(unavailable, ...)` rows are dropped defensively. Returns List<SimulatorResource>. Same contract and test style as parseAdbDevices. |
| parseIdbTargets | logic | new |  | data-device-api/.../source/parse/IdbTargetsParser.kt, commonMain, pure. Splits `idb list-targets` rows on `\|`, keeps target_type == device, reads name/udid/state. Untrusted detection lives behind ONE named token set so AS-2 can be corrected in one place, with a fallback classifying a listed-but-unusable physical target as untrusted. Never throws. |
| SimulatorResource / IosTargetResource | data | new |  | data-device-api/.../source/model/, one file each (PP-TD-6: one concern per file). Plain resource records consumed by the mapper layer; the domain never sees them. |
| IosHostSource (replaces IdbHostSource) | data | new |  | data-device-api/.../source/datasource/IosHostSource.kt: probeSimctl(): ToolProbe, probeIdb(): ToolProbe, listSimulators(): List<SimulatorResource>, listDevices(): List<IosTargetResource>. FOUR methods — bootSimulator is CUT because booting is device-scoped and lives on the executor, keeping HostCommand genuinely deviceless. One interface because one iOS poll uses both binaries in a single round trip. IdbHostSource is DELETED rather than grown: its only caller is detectTools()'s probe, which probeSimctl() replaces. |
| IosHostSourceImpl | data | new |  | data-device-impl/src/desktopMain/.../source/datasource/IosHostSourceImpl.kt (NFR-19). Resolves xcrun and idb through the existing ToolResolver and runs the three new HostCommands through ProcessRunner.run() at MirrorTuning.HostCommandTimeoutMs. probeSimctl() additionally verifies the SUBCOMMAND resolves (a CLT-only Mac has xcrun but no simctl: `xcrun: error: unable to find utility "simctl"`) so it reports ToolMissing with the 'xcode-select --install' hint rather than failing at first use. No new tuning constants. No dedicated unit test — a test here would have to spawn a real process, which always times out inside runTest's virtual clock; the guarded real-toolchain test is the honest coverage. |
| IosDeviceCommandExecutor | data | new |  | data-device-impl/src/desktopMain/.../source/datasource/IosDeviceCommandExecutor.kt. Implements the EXISTING DeviceCommandExecutor interface. Chooses ToolKind.Simctl vs ToolKind.Idb per call from the target set the last poll produced, then buildDeviceArgv(tool, path, device, command) -> ProcessRunner.run. Supports Screencap (`simctl io <udid> screenshot --type=png -`; idb per PLAN-AS-4, with a temp-file fallback if idb will not write to stdout) and SimulatorBoot at MirrorTuning.DaemonStartTimeoutMs, where a non-zero exit whose stderr contains the ONE named already-booted token ('Unable to boot device in current state: Booted') is SWALLOWED AS SUCCESS and every other non-zero exit is rethrown. The exit-code number is deliberately not asserted — it was not measured. The uiautomator/wm/getprop members throw the documented unsupported-on-this-tool DeviceCommandException. |
| AdbDeviceCommandExecutor (renamed from DeviceCommandExecutorImpl) | data | reuse | features.json#android-device-discovery-mirroring | One file, one import site, renamed for symmetry with its new iOS sibling. Body unchanged except for buildDeviceArgv's new first argument (ToolKind.Adb). |
| ScreencapMirrorTransport | data | reuse | features.json#android-device-discovery-mirroring | REGISTERED TWICE, EDITED ZERO TIMES. VERIFIED against the file: the adaptive single-flight loop calls `trimToPngSignature(raw)` and then reads width/height at IHDR_WIDTH_AT = 16 / IHDR_HEIGHT_AT = 20 of the TRIMMED array, so simctl's clean PNG and any hypothetical prefixed output both work without a change. The only difference between the two instances is which executor they were handed. A mid-stream DeviceCommandException (simulator shut down) propagates and flips the session to Lost, identical to an unplugged handset. |
| ProcessRunner | data | reuse | features.json#android-device-discovery-mirroring | EDITED ZERO TIMES. run(argv, timeoutMs) serves every simctl/idb call including boot; the two-pipe concurrent drain, withTimeoutOrNull, finally destroyForcibly, the liveChildren shutdown hook and the TIMEOUT_REASON_PREFIX discriminator all carry over free. The ONE gap — run() buffers stdout to EOF — is exactly why polling was chosen over idb video-stream. |
| ToolResolver + probeCandidates | logic | reuse | features.json#android-device-discovery-mirroring | Learns ToolKind.Simctl: a /usr/bin candidate, readVersion via `xcrun --version`, installCommand 'xcode-select --install', envHint ''. Its injected isExecutable/identifyAdb/userPath lambdas are the established test seam and the reason iOS resolution is unit-testable — ToolResolver's own KDoc warns that spawning a process inside runTest's virtual clock ALWAYS times out. |
| provideDeviceSources() — all THREE actuals | logic | reuse | features.json#android-device-discovery-mirroring | DeviceSourcesProvider.desktop.kt keeps AdbHostSourceImpl / EmulatorSourceImpl / IosHostSourceImpl as unqualified singleOf+bind, and gives DeviceCommandExecutor and MirrorTransport explicit `single<T>(named("android"))` / `single<T>(named("ios"))` registrations (the transport stays explicit `single`, never singleOf, because its clock parameter has a default). DeviceSourcesProvider.android.kt and DeviceSourcesProvider.ios.kt — BOTH omitted from the draft's change list — go from five registrations to seven each, swapping UnsupportedIdbHostSource for UnsupportedIosHostSource and registering the existing Unsupported executor/transport under both qualifiers. No unqualified binding of those two interfaces survives anywhere, so a forgotten qualifier fails loudly in DeviceSourcesModuleTest instead of silently resolving the wrong instance. |
| UnsupportedDeviceSources | logic | reuse | features.json#android-device-discovery-mirroring | data-device-impl/src/commonMain/.../core/unsupported/. Gains UnsupportedIosHostSource (four no-op methods: two absent ToolProbes, two empty lists) and loses UnsupportedIdbHostSource. The existing UnsupportedDeviceCommandExecutor / UnsupportedMirrorTransport objects are reused as-is — only their registration changes. Easy to miss because it is NOT a desktop test failure; it is a Koin startup crash on a phone (FR-56). |
| DiscoveryModel.SimulatorNotBooted / DiscoveryModel.DeviceNotTrusted | data | new |  | domain-core/.../model/device/DiscoveryModel.kt. SimulatorNotBooted(simulators: List<EmulatorModel>, toolVersion: String, lastPollEpochMs: Long) and DeviceNotTrusted(deviceId: String, deviceName: String, simulatorsAvailable: Boolean). Unprefixed per the confirmed survey. Both are SUCCESS values. Adding them makes every `when` over DiscoveryModel a compile error until handled — which is how applyDiscovery, errorCard and indicatorDot are forced to acknowledge them. |
| EmulatorModel.runningDeviceId (KDoc widened) | data | reuse | features.json#android-device-discovery-mirroring | Carries the udid even when the simulator is SHUT DOWN, because simulator names are not unique across runtimes ('iPhone 16' exists under both iOS 17.5 and 18.1 on this host). The UNCHANGED EmulatorModel.toRow then yields exactly the right row: id = runningDeviceId ?: avdName = the udid, serial = udid, kind = Boot (driven by isRunning). One sentence of KDoc, no new field — and bootTargetOf() is its concrete consumer. |
| bootTargetOf(row) + the three boot call sites | logic | new |  | MirrorViewModel. `if (state.platform == Ios) row.id else row.avdName ?: row.id`. Required because EmulatorModel.toRow sets avdName = avdName for every platform and today's commit()/bootRow() call `boot(row.avdName ?: row.id)` — i.e. boot by NAME, which is ambiguous across iOS runtimes. bootSelectedAvd() additionally resolves state.selectedAvd back to its row before calling the helper, which closes the NoSignal-stage path (DiscoveryModel.Found on iOS leaves state.avds populated with simulator names). Three call sites, one helper, no new intent. |
| DiscoveryErrorProjection.errorCard() — two arms, platform-correct copy, extended picker guard | logic | reuse | features.json#error-handling-and-recovery | presentation-feature-mirror/.../core/discovery/DiscoveryErrorProjection.kt. Signature unchanged — `platform` is already on MirrorUiState. THREE changes: (1) two new `when` arms for the iOS variants; (2) every card it builds carries `platform = platform`, and the machine text that is currently the hardcoded private `ADB` constant / `ADB_RESTART_COMMAND` / `"adb devices -l"` becomes the polling platform's own tool line (iOS gets no restart command at all — there is no daemon to restart); (3) the picker-yields guard at line 70 grows to cover SimulatorNotBooted unconditionally and DeviceNotTrusted when simulatorsAvailable, without which OpenPicker is a dead button because the card is computed before the stage `when`. Stays pure and Compose-free, which is why AC-46's shadowing assertion is a plain commonTest with no device. |
| MirrorViewModel — platform as a flatMapLatest key | logic | reuse | features.json#android-device-discovery-mirroring | The STRUCTURAL half of the shadowing fix. observeDiscoveryWhileIdle() today is `sessionDeviceId.flatMapLatest { device -> if (device == null) observeDiscovery() else emptyFlow() }`. It becomes `combine(sessionDeviceId, discoveryPlatform, ::Pair).flatMapLatest { (device, platform) -> if (device == null) observeDiscovery(platform) else emptyFlow() }`, with `discoveryPlatform: MutableStateFlow<PlatformModel>` written by selectPlatform — the same mirror-the-state pattern sessionDeviceId already is. flatMapLatest cancels the previous inner flow, so the adb loop is structurally gone the instant iOS is selected and NO hand-rolled Job is introduced. selectPlatform() additionally resets discovery = Detecting and rows = emptyList() in its reduction so no stale emission survives the switch window. observeStats() gets the same treatment for observeMirrorStats(platform). |
| MirrorErrorKind.SimulatorNotBooted / DeviceNotTrusted + MirrorErrorCard.platform | data | reuse | features.json#error-handling-and-recovery | presentation-feature-mirror/.../source/mirror/MirrorContract.kt. Two new members of a Compose-free, string-free enum whose KDoc ALREADY records err-sim and err-trust as drawn-but-deferred (that KDoc, and MirrorErrorStage's matching paragraph, must be rewritten). MirrorErrorCard gains ONE field, `platform: PlatformModel = PlatformModel.Android`, so the stateless MirrorErrorStage can resolve iOS copy without a new parameter threaded through MirrorContent. MirrorErrorCard.primaryDeviceId already exists and carries the udid the Boot action needs. |
| MirrorErrorStage / ErrorCard organism | ui | reuse | components.json#error-card | Two new `when` arms plus a platform branch on the ToolMissing and NoDevice arms (iOS must never offer 'Locate adb' or 'Restart adb' — it gets Retry instead). The 520dp ErrorCard organism already takes title/body/kind/command/onCopyCommand/primary/secondary/diagnostic — exactly the err-sim and err-trust designs. The MirrorErrorStageSample preview grows two cases so the light/dark @Preview pair still covers every kind. |
| DevicePickerPanel + PlatformToolCard + SourceListRow | ui | reuse | components.json#device-picker-panel | Unchanged organisms. MirrorRowGroup{Physical,Emulator} maps 1:1 onto physical iPhone / simulator and MirrorRowKind.Boot already means 'cold, committing boots it'. The only changes are at the call site: platformCards()'s hardcoded `enabled = platform == Android` and the group label string. NFR-8 keyboard navigation is free. |
| MirrorStageContent / MirrorStage.IosDeferred (deleted) | ui | reuse | components.json#mirror-stage-content | MirrorStage.IosDeferred (MirrorContract.kt:102-103), its derivation branch (MirrorContract.kt:358), its render branch (MirrorContent.kt:131-137) and the mirror_ios_deferred string + import are DELETED outright — together with the three commonTest assertions on it (MirrorViewModelAnnotationTest:287, MirrorViewModelSessionTest:165, VerifyStageDerivationTest:62) and presentation-feature-mirror/CLAUDE.md's stage list. iOS then flows through the same Picker/Mirror/NoSignal derivation Android uses. |
| SimulatorResourceMapper / ToolProbeMapper / DiscoveryProjection | logic | reuse | features.json#android-device-discovery-mirroring | SimulatorResourceMapper is new (SimulatorResource -> EmulatorModel, populating runningDeviceId with the udid in both states); ToolProbeMapper is extended so Simctl maps to PlatformModel.Ios. DiscoveryProjection gains `applyDiscovery` arms for the two new variants and populates the iOS simulator row's `detail` with its runtime ('iOS 18.1') — additive, Android untouched, and the only thing that disambiguates two identically-named simulators (see PP-TD-9). All obey mappers.md: NEVER throw. |

#### dataModel

| entity | fields |
| --- | --- |
| SimulatorResource (data-device-api, new) | ["name: String", "udid: String", "runtime: String", "state: String (Booted \| Shutdown \| other, verbatim)"] |
| IosTargetResource (data-device-api, new) | ["name: String", "udid: String", "state: String (verbatim)", "targetType: String", "trusted: Boolean (derived from one named token set — AS-2, correctable in one place)"] |
| DiscoveryModel.SimulatorNotBooted (domain-core, new variant) | ["simulators: List<EmulatorModel>", "toolVersion: String", "lastPollEpochMs: Long"] |
| DiscoveryModel.DeviceNotTrusted (domain-core, new variant) | ["deviceId: String", "deviceName: String", "simulatorsAvailable: Boolean"] |
| EmulatorModel (domain-core, KDoc widened only — no field change) | ["runningDeviceId: String? — now carries the simulator udid even when SHUT DOWN"] |
| MirrorErrorCard (presentation-feature-mirror, ONE new field) | ["platform: PlatformModel = PlatformModel.Android — so the stateless card renderer can resolve iOS copy and suppress adb-only actions without a new composable parameter"] |
| ToolKind (data-device-api, widened) | ["Adb(\"adb\", sdkSubdirectory)", "Emulator(...)", "Idb(\"idb\", null) — unchanged", "Simctl(\"xcrun\", null) — NEW; the simctl subcommand is inserted by the argv branch"] |
| DeviceCommand (data-device-api, widened 6 -> 7) | ["Screencap — REUSED for iOS, KDoc rewritten platform-neutral", "SimulatorBoot — NEW", "WmSize / WmDensity / GetProp / EmuAvdName / UiAutomatorDump — Android-only; throw a documented DeviceCommandException on an iOS tool"] |
| HostCommand (data-device-api, widened 7 -> 10, declared in DeviceArgv.kt) | ["XcrunVersion — NEW", "ListSimulators — NEW", "ListTargets — NEW", "IdbVersion — CUT, nothing renders it"] |
| MirrorStatsModel (domain-core, UNCHANGED) | ["toolVersion: String — already platform-neutral; the adb-shaped name is a private flow inside DeviceRepositoryImpl, renamed there. No new field, no domain-core edit."] |

#### interfaces

| name | contract |
| --- | --- |
| DeviceRepository (five methods) | observeDiscovery(platform: PlatformModel): Flow<DiscoveryModel>; mirror(platform: PlatformModel, deviceId: String): Flow<MirrorFrameModel>; dumpHierarchy(platform: PlatformModel, deviceId: String): HierarchySnapshotModel; suspend bootEmulator(platform: PlatformModel, target: String); observeMirrorStats(platform: PlatformModel): Flow<MirrorStatsModel>. The identifiers are STRINGS, not DeviceId — corrected from the draft against the real interface. detectTools(), refreshDiscovery(), restartAdbServer(), setAdbPath(path) UNCHANGED. |
| IosHostSource | suspend fun probeSimctl(): ToolProbe; suspend fun probeIdb(): ToolProbe; suspend fun listSimulators(): List<SimulatorResource>; suspend fun listDevices(): List<IosTargetResource>. All four are SUCCESS-value returning: a missing tool is a ToolProbe result, an empty device list is an empty list. |
| DeviceCommandExecutor (unchanged interface, two qualified implementations) | suspend fun run(device: DeviceId, command: DeviceCommand): ByteArray. Bound as named("android") -> AdbDeviceCommandExecutor and named("ios") -> IosDeviceCommandExecutor. No unqualified binding exists after this spec. |
| MirrorTransport (unchanged interface, two qualified instances of ONE implementation) | fun frames(device: DeviceId): Flow<MirrorFrame>; val stats: StateFlow<TransportStats>. Both qualifiers resolve to ScreencapMirrorTransport, differing only in the executor handed to them. |
| buildDeviceArgv | public fun buildDeviceArgv(tool: ToolKind, toolPath: String, device: DeviceId, command: DeviceCommand): List<String>. Post-conditions enforced in code: argv[0] == toolPath AND the per-tool selector segment contains device.value exactly once. Violation is a programming error and throws IllegalStateException via check(). The command payload is NOT counted — GetProp's free text may legally equal the udid. |

#### stateManagement

Orbit MVI, unchanged. MirrorViewModel's `state.platform` stays the SINGLE source of truth; `discoveryPlatform: MutableStateFlow<PlatformModel>` is a collector-facing mirror of it, exactly as `sessionDeviceId` already mirrors `state.session` — not a second owner. Nothing in the data layer holds mutable platform state. selectPlatform() reduces state, writes discoveryPlatform (which re-keys the existing flatMapLatest and cancels the adb flow for free) and resets discovery = Detecting / rows = emptyList() in the same reduction. errorCard()/indicatorDot()/dropBanner() remain pure Compose-free projections over MirrorUiState — that purity is what makes the shadowing fix assertable in commonTest with no device attached. ZERO new intents join the contract.

#### navigation

- No navigation change. The desktop root is RectifyWindowShell; MirrorHost owns the stage. The platform switch is the existing toolbar segmented control (PLATFORM_SOURCES already lists 'Android' and 'iOS') plus the picker platform cards — neither gains a destination.
- Stage transitions change only by DELETION: MirrorStage.IosDeferred and its derivation branch go away, so platform == Ios falls through to the same Picker -> Mirror -> NoSignal derivation Android uses.

#### affectedModules

- data-device-api (commonMain + commonTest) — ToolKind, DeviceCommand, DeviceArgv (HostCommand + buildHostArgv + buildDeviceArgv all live in this ONE file), ToolProbeCandidates, two new parsers, two new resources, IosHostSource added / IdbHostSource deleted, CLAUDE.md closed-set counts
- data-device-impl (commonMain unsupported + desktopMain + desktopTest) — IosHostSourceImpl, IosDeviceCommandExecutor, ToolResolver, all THREE DeviceSourcesProvider actuals, UnsupportedDeviceSources; IdbHostSourceImpl deleted; CLAUDE.md datasource list
- domain-core — DiscoveryModel (+2 variants), EmulatorModel KDoc, PlatformModel KDoc. NOT MirrorStatsModel.
- domain-repository-api — DeviceRepository (five signatures)
- domain-usecase-api — five use-case interfaces
- domain-usecase-impl (+ DumpHierarchyUseCaseImplTest) — five use-case impls
- data-repository-impl (commonMain + commonTest) — DeviceRepositoryImpl, SimulatorResourceMapper, ToolProbeMapper, DataRepositoryImplModule, four existing device test files
- presentation-feature-mirror (commonMain + commonTest) — MirrorContract, MirrorViewModel, MirrorContent, MirrorErrorStage, DiscoveryErrorProjection, DiscoveryProjection, MirrorFakes and SIX test files (DiscoveryErrorProjectionTest, MirrorContractTest, MirrorViewModelVerifyTest, MirrorViewModelAnnotationTest, MirrorViewModelSessionTest, VerifyStageDerivationTest), CLAUDE.md stage list
- presentation-core-localisation — strings.xml (~12 added, 1 value changed, 1 deleted)
- NOT TOUCHED (by design): compose-application, desktop-application, ios-application, presentation-core-ui, presentation-core-styling, data-database-*, data-handoff-api, mcp-application, ScreencapMirrorTransport.kt, ProcessRunner.kt, MirrorTuning.kt, MirrorStatsModel.kt

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
| error-handling-and-recovery | Error handling & recovery (F-6) | features.json#error-handling-and-recovery | Supplies MirrorErrorKind/MirrorErrorCard, the pure errorCard()/dropBanner() projections, MirrorErrorStage and the budgeted escalation ladder. Its KDoc already names err-sim and err-trust as drawn-but-deferred — this spec fills exactly those two slots and must rewrite both KDoc paragraphs that say 'only the Android four are modelled'. |
| verify-and-rediff-loop | Verify & Re-diff Loop (F-9) | features.json#verify-and-rediff-loop | Rect/pixel-neutral; zero behavioural change needed. Its neutrality is the evidence that deleting MirrorStage.IosDeferred is safe — but VerifyStageDerivationTest asserts on the constant and must be edited. |
| error-propagation | Failure / Result error propagation | features.json#error-propagation | resultLauncher(errorMapper = Failure.Technical::Kind) in every *UseCaseImpl is unchanged — the five iOS-aware use cases stay thin wrappers, and the two reactive ones return a bare Flow with no resultLauncher in the path at all. Its recorded gap (ShowError ignores messageId) must NOT be copied. |
| localisation | Localisation (Compose Resources) | features.json#localisation | ~12 new keys join the ~95 existing English keys; one value changed; one key deleted. |
| history-batch-tracking | History & Batch Tracking (F-8) | features.json#history-batch-tracking | Untouched. iOS batches persist through the same path because everything from HierarchySnapshotModel downward is already neutral. |

#### newToBuild

- data-device-api: SimulatorResource, IosTargetResource, parseSimctlDevices, parseIdbTargets, IosHostSource (5 new files)
- data-device-impl/desktopMain: IosHostSourceImpl, IosDeviceCommandExecutor (2 new files)
- domain-core: DiscoveryModel.SimulatorNotBooted, DiscoveryModel.DeviceNotTrusted (2 new sealed variants, no new file)
- data-repository-impl: pollIos(), SimulatorResourceMapper (1 new file)
- presentation-feature-mirror: MirrorErrorKind.SimulatorNotBooted, MirrorErrorKind.DeviceNotTrusted, MirrorErrorCard.platform, bootTargetOf() (no new file, no new intent)
- data-device-impl commonMain: UnsupportedIosHostSource (new object inside the existing UnsupportedDeviceSources.kt)
- Tests: SimctlDevicesParserTest, IdbTargetsParserTest, IosDiscoveryFixtures, SimctlRealToolchainTest (guarded), DeviceIosDiscoveryPollingTest, MirrorIosErrorCardTest, MirrorViewModelIosPlatformSwitchTest (7 new test files; IosHostSourceImplTest cut — it would have to spawn)
- Localisation: ~12 new string keys
- DELETED: IdbHostSource, IdbHostSourceImpl, UnsupportedIdbHostSource, MirrorStage.IosDeferred + its derivation and render branches + its three test assertions, mirror_ios_deferred

### resources

#### rules

| ref | why |
| --- | --- |
| .claude/rules/platform-coverage.md | All idb/simctl process invocation and every iOS datasource impl must live under data-device-impl/src/desktopMain; that module's commonMain names no java.*. 'Desktop' is the current-OS JVM target only — there is no macosMain. :ios-application's Xcode scheme must not be disturbed (NFR-19). |
| .claude/rules/error-handling.md | A missing tool or an empty device list is a SUCCESS value, never an exception; the iOS cards ride the MirrorErrorCard projection off DiscoveryModel, not Failure. Also records the gap to NOT copy: every ShowError(messageId) handler ignores messageId. |
| .claude/rules/koin-di.md | One module per Gradle module, load order significant, provideDeviceSources() is the single registration seam, and qualifiers use explicit `single { ... get(named(...)) }` over singleOf. |
| .claude/rules/package-layout.md | core/ is internal-only, source/ is public with one subfolder per semantic concern. explicitApi() polices the boundary: every new public declaration needs an explicit visibility modifier and an explicit return type. data-device-api still needs no core/. |
| .claude/rules/mappers.md | Mapper<I,O> is a fun interface and a mapper NEVER throws; unknown values get a safe default in toModel. Binds SimulatorResourceMapper, the extended ToolProbeMapper, and by precedent both new parsers. |
| .claude/rules/testing.md | kotlin.test is wired once by the convention plugins. The desktop target runs, but a cross-target build must pass: commonTest compiles for iosArm64 and Kotlin/Native rejects a backtick name containing a comma or a colon (an apostrophe is fine, measured). :compose-application has no test wiring, so nothing testable lands there. A ProcessRunner spawn inside runTest's virtual clock always times out. |
| .claude/rules/orbit-mvi.md | selectPlatform()'s discoveryPlatform write and the reused BootRow handler must follow the repo's intent/reduce/sideEffect shape; state.platform stays the single source of truth. |
| .claude/rules/localisation.md | ~12 new keys and one value change go into presentation-core-localisation/src/commonMain/composeResources/values/strings.xml; machine output (argv, stderr tails) stays unlocalised. Beware AAPT-style apostrophe escaping — a recently fixed bug in this repo, and "I've tapped Trust" contains an apostrophe. Keys follow <feature>_<element>. |
| .claude/rules/design-tokens.md | The two new cards read colours only through Theme.color; never a Color(0x...) literal, a raw .dp or a hardcoded TextStyle at a call site (NFR-9). |
| .claude/rules/compose-preview-convention.md | The two new MirrorErrorStage branches join the existing MirrorErrorStageSample so the light/dark @Preview pair still covers every kind — the only visual coverage this repo has. |
| .claude/rules/domain-layer.md | DiscoveryModel's two new variants and the widened EmulatorModel KDoc live in domain-core and must stay free of platform/resource types. Also the rule that reactive use cases return a bare Flow<T> — which is why no resultLauncher sits in the discovery path. |
| .claude/rules/version-catalog-hygiene.md | Zero new catalog entries — the no-new-dependency decision (plain-text parsing over kotlinx-serialization-json) is enforced here. |
| .claude/rules/convention-plugins.md | No new Gradle module and no new convention-plugin hook; the existing library/feature plugins already register jvm("desktop") and wire kotlin.test. |
| CLAUDE.md#architecture | Module structure and the Desktop-is-a-plain-JVM-module fact that keeps :desktop-application and :compose-application out of this spec's blast radius. |
| data-device-api/CLAUDE.md | Module-local conventions for the closed command sets, the single argv construction point and the NFR-5 enforcement table — all of which this spec edits and must keep accurate. |
| data-device-impl/CLAUDE.md | Module-local conventions: one process-starting site (ProcessRunner), every budget named in MirrorTuning, TIMEOUT_REASON_PREFIX, and the datasource/unsupported inventory this spec changes. |
| presentation-feature-mirror/CLAUDE.md | Documents the MirrorStage case list (including IosDeferred) and the module's stricter test-naming rule (plain camelCase backtick names, no apostrophes either). Both need updating. |

#### skills


#### agents


#### docs

| ref | why |
| --- | --- |
| .pipeline/sdd/docs/ios-platform-support/inputs/codebase-recon.md | Ground truth for file paths, symbols and the four adb hardcoding sites. Re-verified against the tree for every load-bearing claim this plan makes; corrections are recorded inline (String vs DeviceId identifiers, HostCommand living in DeviceArgv.kt, the three extra IosDeferred test files, the two omitted provider actuals). |
| .pipeline/sdd/docs/ios-platform-support/inputs/ios-tooling-probe.md | Measured toolchain state: Xcode 26.3, runtimes 17.5/18.1, idb + idb_companion present, NO simulator booted. The evidence base for AC-46's measured half and for AS-3's waiver. |
| .pipeline/sdd/docs/registry/bugs.json | Recurrence check for every touched area — ten entries reviewed, seven relevant, all carried into knownRisksFromQA with a watchFor. |

### errorStates

| state | behavior |
| --- | --- |
| loading | DiscoveryModel.Detecting while the first iOS poll is in flight. selectPlatform() resets to Detecting on every switch so the user never sees the previous platform's answer attributed to the new one. |
| empty | No simulators and no physical devices at all -> DiscoveryModel.NoDeviceFound rendered with iOS-worded copy (card.platform == Ios) and no 'Restart adb' action. Distinct from SimulatorNotBooted, which means 'simulators exist but none is running'. Both are success values. |
| error | SIMULATOR NOT BOOTED (err-sim) — simctl lists simulators but none is Booted. MirrorErrorKind.SimulatorNotBooted; primary 'Boot simulator' -> MirrorIntent.BootRow(card.primaryDeviceId) which resolves the row and boots BY UDID via bootTargetOf; secondary 'Choose another' -> MirrorIntent.OpenPicker, which works only because errorCard() now yields the stage to the picker for this variant. Recovers without an app restart (AC-47). |
| error | DEVICE NOT TRUSTED (err-trust) — a physical iPhone is the ONLY candidate and is listed-but-unusable. MirrorErrorKind.DeviceNotTrusted; primary "I've tapped Trust — retry" -> MirrorIntent.RetryDiscovery (already cuts the poll interval short); secondary 'Use simulator' -> MirrorIntent.OpenPicker, which yields the stage when simulatorsAvailable. If a booted simulator ALSO exists, Found wins and the untrusted device shows as a disabled row with its state verbatim — exactly how parseAdbDevices treats an `unauthorized` handset. |
| error | TOOL MISSING — xcrun present but simctl absent (Command Line Tools without full Xcode: `xcrun: error: unable to find utility "simctl"`). probeSimctl() verifies the SUBCOMMAND resolves and reports ToolMissing with the 'xcode-select --install' hint. The card renders iOS copy and offers Retry — never 'Locate adb'. |
| error | POLL FAILED — parser drift. simctl exits zero, stdout is non-empty, and parseSimctlDevices returns zero rows: emit PollFailed(tool = "xcrun", reason = <stdout head>), NOT SimulatorNotBooted. Makes an Xcode format change visible instead of silently rendering 'no simulators'. The card's diagnostic already reads outcome.tool, so it needs no platform branch. |
| error | IDB UNAVAILABLE — `idb list-targets` fails or times out (no idb_companion running). Budgeted by MirrorTuning.HostCommandTimeoutMs; the failure degrades the PHYSICAL list to empty and never aborts the simctl half. No companion auto-start. |
| error | SIMULATOR DIES MID-STREAM — `simctl io screenshot` exits non-zero; DeviceCommandException propagates through the unchanged transport and flips the session to Lost, identical to an unplugged Android handset. Zero new code. |
| error | ALREADY BOOTED — `simctl boot <udid>` exits non-zero with 'Unable to boot device in current state: Booted'. Treated as SUCCESS on that STDERR TOKEN (one named constant), never on the exit-code number, which was not measured on this host. This is why boot uses run() rather than spawnDetached: the outcome matters, and spawnDetached would discard it. |
| error | MULTIPLE DEVICES — a booted simulator AND a trusted physical iPhone both present yields Found with >=2 selectable physical rows and the existing MultipleDevices card, whose diagnostic line names the iOS tool instead of `adb devices -l`. |
| error | HIERARCHY ON iOS — dumpHierarchy(Ios, ...) short-circuits to HierarchySnapshotModel.Empty in the repository WITHOUT spawning a process and WITHOUT throwing. attachedHierarchyXml stays Android-gated so the prompt degrades to "" exactly as today (FR-57, AC-50). |

### observability

#### logs

- DeviceCommandException(argv, exitCode, stderrTail) from ProcessRunner remains the single diagnostic record for every failed simctl/idb invocation, with the TIMEOUT_REASON_PREFIX discriminator unchanged.
- PollFailed(tool, reason) now reports the PLATFORM'S OWN binary name instead of the hardcoded ToolKind.Adb.binaryName at DeviceRepositoryImpl.kt:146 — on iOS that means 'xcrun', surfaced verbatim in the error card's diagnostic line.
- Parser-drift signal: 'simctl succeeded, zero rows parsed, non-empty stdout' is carried into PollFailed's reason as the stdout head so an Xcode format change is diagnosable from the card itself.
- The already-booted swallow is logged on the way in (the same precedent healDaemon() set: an unobservable repair is one nobody can support in the field).

#### metrics

- TransportStats(fps, latencyMs = null by design, captureRoundTripMs, droppedFrames, widthPx, heightPx) — unchanged contract, now populated from the iOS capture loop.
- Measured iOS capture round-trip, recorded ONCE as a number in the verification table rather than asserted as a threshold (CaptureFloorMs = 100 is itself an unmeasured Android constant).
- Tool version in the status bar rides the EXISTING MirrorStatsModel.toolVersion — adb's version on Android, xcrun's on iOS — so the line never goes blank and no new field is added.

#### analyticsEvents

- None. This repo ships no analytics pipeline and this spec does not introduce one.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | NFR-8 — the device picker stays fully keyboard-navigable for iOS rows with ZERO new code: pickerKeyAction maps DirectionUp/DirectionDown/Enter/NumPadEnter/Escape through one onPreviewKeyEvent with a FocusRequester, and nextSelectableIndex does the arithmetic. Nothing in that path knows about adb. Verified by the existing PickerNavigationTest, which must stay green. |
| A11Y-2 | Both new error cards expose their primary and secondary actions as real focusable AppButtons through the shipped ErrorCard organism — same keyboard reachability as every existing card. No bespoke action affordance is invented, and no action is a dead button (the picker-yields guard is what makes the secondaries real). |
| A11Y-3 | A shut-down simulator renders as MirrorRowKind.Boot — a selectable row whose commit action boots it by udid — rather than a disabled row with a hidden affordance, so the recovery path is reachable by keyboard alone (AC-47 via the picker as well as via the card). |
| A11Y-4 | NFR-9 — both new cards read every colour through Theme.color and join the existing MirrorErrorStageSample so the light/dark @Preview pair still covers every kind. No new signal colour is introduced; the orange/warning role already used for recoverable states is reused. |
| A11Y-5 | Machine output (argv, stderr tails, udids, tool versions) stays unlocalised and renders in the mono CommandBlock, per the existing convention — it is diagnostic text, not prose. |

### performanceBudget

| metric | target |
| --- | --- |
| iOS frame capture round-trip (`xcrun simctl io <udid> screenshot --type=png -`) | MEASURED 2026-10-19 on a booted iPhone 16 / iOS 18.1: median 300ms, min 277, max 608 (cold), n=10 through the adapter's exact argv. The plan predicted 200-400ms and said to record the number rather than paper over a miss - so recorded: the iOS ceiling is ~3.3 fps, NOT the ~10 fps MirrorTuning.CaptureFloorMs=100 assumes for Android. No iOS tuning constant was added; ScreencapMirrorTransport's adaptive loop simply never sleeps on iOS because CaptureFloorMs - captureMs is always <= 0, which the reuse decision handles unmodified. Any 'about 10 fps' claim is Android-only. |
| `idb list-targets` per poll | AMENDED (audit AF-3): MirrorTuning.DaemonStartTimeoutMs = 15000ms, NOT the 3000ms originally budgeted. idb list-targets was MEASURED at 5.27s on this host (three runs within 0.02s), so a 3s budget would have missed on EVERY call and left the physical-device list permanently and silently empty. Shipped code uses the 15s constant; no new tuning value was introduced. Timeout still degrades the physical list to empty while the simctl half is unaffected. |
| `xcrun simctl list devices available` per poll | MirrorTuning.HostCommandTimeoutMs = 3000ms (existing). |
| `xcrun simctl boot <udid>` | MirrorTuning.DaemonStartTimeoutMs = 15000ms (existing, reused — honours the no-new-tuning-constants rule). |
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
- ADD mirror_error_action_trust_retry — "I've tapped Trust — retry" (contains an apostrophe AND an em dash; do NOT let AAPT-style escaping leak into the UI — that exact bug was fixed in this repo recently)
- ADD mirror_error_action_use_simulator — "Use simulator"
- ADD mirror_error_tool_missing_ios_title / _body — REQUIRED, not optional: ToolMissing is a shared DiscoveryModel variant and iOS reaches it whenever simctl is absent. Without an iOS-worded pair the card either lies about adb or renders blank.
- ADD mirror_error_no_device_ios_title / _body — same reason: NoDeviceFound is reachable on iOS (no simulators AND no devices).
- CHANGE mirror_group_emulator's VALUE from "Emulator" to a neutral "Virtual devices". The KEY is kept deliberately: renaming it touches MirrorContent's import plus strings.xml for zero behaviour.
- DELETE mirror_ios_deferred (strings.xml:102) along with MirrorStage.IosDeferred, its render branch and its import

#### notes

~12 added, 1 value changed, 1 deleted. Keys follow the existing mirror_error_* / mirror_group_* conventions. Machine output (argv, stderr tails, udids, tool versions) stays unlocalised and renders in CommandBlock. The Android-worded mirror_error_tool_missing_* and mirror_error_no_device_* keys are NOT reused for iOS — but the draft's conclusion that 'a platform guard makes sure Android copy never renders on iOS' left nothing in their place; the iOS pairs above are what actually renders, selected by MirrorErrorCard.platform in MirrorErrorStage.

### security

#### dataHandling

- NFR-5 moves from a POSITIONAL guarantee to a STRUCTURAL one. Today `listOf(toolPath, "-s", device.value) + command.arguments()` is enforced only by the shape of one line. After this spec, buildDeviceArgv builds a per-tool SELECTOR SEGMENT in a `when (tool)` and asserts `check(selector.count { it == device.value } == 1)` before appending the payload. Tool-agnostic, fails loudly for any branch that forgets or duplicates the selector, and machine-checked by an exhaustive ToolKind x DeviceCommand sweep.
- The assertion is scoped to the selector and NOT to the whole argv, by instruction and by defect analysis: DeviceCommand.GetProp carries unvalidated free text, so a whole-argv count would let a property name equal to the udid throw IllegalStateException out of a pure commonMain function. Security invariants must not be trippable by data; that is how they get deleted later.
- The sweep test asserts per pair: argv[0] == toolPath, the id appears exactly once in the selector, no element matches the connect/-H/-P denylist, and Android-only commands throw on iOS tools. It adds one explicit regression case for GetProp(name = device.value).
- DeviceId keeps its private constructor and DeviceCommand/HostCommand stay closed sets, so the argv remains unforgeable from user data. All process invocation stays behind ONE site (ProcessRunner), unchanged.
- Screenshot bytes stay in memory and flow through the existing MirrorFrame path; iOS adds no new persistence and no new network call. The one conditional exception is PLAN-AS-4's temp-file fallback for idb, which — if it proves necessary — writes to a process-owned temp path, reads it back and deletes it in a `finally`.

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

No persisted-data or wire-format change. AnnotationEnvelope's field order is untouched; hierarchyXml keeps its name because hierarchy capture is deferred (FR-57). No Room migration. The breaking changes are SOURCE-level and internal to this repo: DeviceRepository's five signatures (rippling into 5 use-case interfaces and their impls), buildDeviceArgv's signature (one Android call site), DeviceCommandExecutorImpl renamed to AdbDeviceCommandExecutor (one import site), MirrorErrorCard gaining a defaulted field (source-compatible), DeviceRepositoryImpl's constructor, and the deletion of IdbHostSource/IdbHostSourceImpl/UnsupportedIdbHostSource/MirrorStage.IosDeferred/mirror_ios_deferred. All are compile-time visible — explicitApi() and the exhaustive `when`s make an unhandled case a compile error, not a runtime surprise. The ONE change that is NOT compile-time visible is the Koin qualifier work, which is why no unqualified DeviceCommandExecutor/MirrorTransport binding is left anywhere and why DeviceSourcesModuleTest must cover all three graphs. Rollout is a straight merge; the two-commit split is the risk control, not a flag.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json#PP-TD-8 | Kotlin/Native release linker OOMs under ~4 parallel linkReleaseFramework tasks (Java heap space against a 4 GiB daemon heap). Pre-existing, affects EVERY cross-target gate in this repo. | The mandated cross-target build (`:data-device-api:build :data-repository-impl:build :presentation-feature-mirror:build`) failing with 'Java heap space' rather than a real compile error — easy to misread as this spec's bug. | Run the cross-target gate with `--max-workers=1`, the documented workaround from the previous spec. Do not chase a phantom iOS compile failure. |
| bugs.json#PP-TD-9 | EmulatorModel.toRow sets no detail, unlike DeviceModel.toRow — an information gap in the device picker. Lives in presentation-feature-mirror/core/discovery/DiscoveryProjection.kt, which this spec edits. | iOS simulator rows inheriting the blank detail, so the picker shows 'iPhone 16' twice with no runtime to disambiguate — which matters MORE on iOS than Android because simulator names are NOT unique across runtimes (confirmed on this host: iOS 17.5 and 18.1 both ship one). | Do not fix PP-TD-9's Android half here (it belongs to android-device-discovery-mirroring and its own gate), but DO populate the iOS simulator row's detail with the runtime. Additive, does not touch the Android branch. Flag the Android half as still open. |
| bugs.json#PP-TD-2 | ClearStaged keeps promptOverride, resurfacing a prompt authored against deleted annotations. OPEN, in MirrorViewModel.kt — a file this spec edits in both commits. | selectPlatform()'s widened reset (discovery, rows, discoveryPlatform) doing the same thing one level up: switching Android -> iOS releases the session and the frozen frame but would leave a promptOverride authored against an Android payload attached to an iOS session. | Include promptOverride in selectPlatform()'s reset — it is one field in a reduction this spec is already editing, and leaving it out would reproduce PP-TD-2's exact failure mode on a new axis. Do NOT attempt PP-TD-7's root-cause fix here. |
| bugs.json#PP-TD-7 | ROOT CAUSE of PP-TD-2: promptOverride carries no record of the payload it was authored against. OPEN, in MirrorContract.kt — a file this spec edits. | Being tempted to fix it while MirrorContract.kt is open, inflating the AC-49 regression surface for an unrelated defect; or conversely adding a new state field that repeats the same no-provenance mistake. | Explicitly out of scope. The ONE new state field this spec adds (MirrorErrorCard.platform) is derived fresh from MirrorUiState.platform on every projection pass, so it cannot go stale — note that in its KDoc so the contrast with promptOverride is on the record. |
| bugs.json#PP-TD-4 | Tautological test: assertEquals(1, PAYLOAD_FORMAT_VERSION) — asserts a literal against itself and would pass under any edit. | The same failure mode here: asserting that ToolKind.Simctl.binaryName == "xcrun" or that DiscoveryModel.SimulatorNotBooted exists proves nothing. | Every new test must be falsifiable by a deliberate mutation. For the argv sweep, verify by temporarily deleting the selector from one branch and confirming the sweep goes red. |
| bugs.json#PP-TD-5 | Test name and comment claim a structural property the body does not assert (body asserts Empty -> empty twice). In presentation-feature-mirror/commonTest/HierarchyOutlineTest.kt. | AC-50's 'iOS returns an empty hierarchy' test degenerating into asserting Empty == Empty. | Write the AC-50 test against a fake DeviceCommandExecutor whose run() THROWS; the test passes only because the repository never reaches it. |
| bugs.json#PP-TD-6 | core/payload file names do not partition their contents (one label function in one file while two live in another). | The same drift in the new iOS files: dumping both parsers plus both resources into one IosParsers.kt. | One concern per file, matching the existing AdbDevicesParser.kt / AvdListParser.kt / WmOutputParser.kt partition exactly. Four files, not one. |
| features.json#android-device-discovery-mirroring (waiver W-2, HARDWARE GATE) | Android capture rate was never measured (handset unauthorized, AVD never booted), so MirrorTuning.CaptureFloorMs stays at an UNMEASURED 100ms and latency is an explicit placeholder. | SC-25's success metric ('iOS reaches the same ~10fps floor as Android') being read as a comparison against a measured Android baseline. There is none. Also the temptation to report a fabricated iOS latency — TransportStats.latencyMs is null BY DESIGN. | Record the measured iOS captureRoundTripMs as a raw number with the explicit note that the Android comparison point is itself unmeasured. Do not fabricate latency. |
| features.json#android-device-discovery-mirroring (waiver SEC-1) | DeviceCommand.GetProp takes unvalidated free text into an adb shell (no production callers today). | TWO things now. (1) The buildDeviceArgv generalisation being the moment that free text reaches a NEW tool — GetProp has no iOS meaning and must throw a documented DeviceCommandException on an iOS tool. (2) The free text colliding with the NFR-5 assertion: a GetProp name equal to the udid would trip a whole-argv `check()`, turning SEC-1 from a latent risk into a live crash. | Scope the assertion to the selector segment, cover GetProp x Simctl and GetProp x Idb in the sweep as documented throws, and add the GetProp(name = device.value) non-throwing regression case. This spec adds the first machine-checked guard over that family without taking on the Android-side validation fix. |
| features.json#error-propagation (recorded gap) | Every feature's XSideEffect.ShowError(messageId) handler IGNORES messageId and shows a hardcoded, unlocalized 'Error' title. Documented as a gap, NOT a pattern to extend. | Routing an iOS failure through ShowError instead of the MirrorErrorCard projection, which would both lose the message and copy the gap. | By construction: a missing tool and an empty device list are SUCCESS values on DiscoveryModel and ride the error-card projection. No new ShowError call site is added by this spec. |
| features.json#error-propagation (recorded gap) | resultLauncher catches CancellationException — an inherited open change request carried across four specs, still unfixed. | A platform switch mid-poll producing a swallowed CancellationException that surfaces as a spurious Failure. | LARGELY MOOT AS REPLANNED, and that is the point of the flatMapLatest fix: ObserveDeviceDiscoveryUseCase returns a BARE Flow (domain-layer rule — reactive use cases never return Flow<Result<T>>), so no resultLauncher sits in the discovery path, and flatMapLatest's cancellation of the inner flow never crosses one. Still add the explicit test that a platform switch mid-poll leaves state at Detecting with no error side effect. Do not attempt the repo-wide resultLauncher fix here. |
| features.json#app-shell-window-layout (waiver 2) | Cmd+1/2/3 LIVE dispatch is unverifiable; only the pure shortcutToTab mapping is tested. Precedent: live keyboard dispatch in this shell is not machine-verified. | Claiming NFR-8 keyboard coverage for the new iOS picker rows on the strength of PickerNavigationTest alone — that test covers the pure arithmetic, not live dispatch. | State NFR-8's iOS coverage as 'pure navigation logic tested, live dispatch unverified — same standing as Android', not as newly proven. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 1 | COMMIT 1 / step 0 — capture the real toolchain fixtures before touching code: this host's `xcrun simctl list devices available` and `idb list-targets` stdout, committed under data-device-api/src/commonTest as IosDiscoveryFixtures (PLAN-AS-3) | [] | ["AC-46", "AC-31"] | 0.25d |
| 2 | COMMIT 1 — thread `platform: PlatformModel` through DeviceRepository's five dispatching methods (domain-repository-api), keeping the plain-String identifiers; widen bootEmulator/restartAdbServer/setAdbPath KDoc; leave detectTools/refreshDiscovery alone | [] | ["AC-49"] | 0.25d |
| 3 | COMMIT 1 — ripple the signature through 5 use-case interfaces (domain-usecase-api), 5 use-case impls (domain-usecase-impl) and DumpHierarchyUseCaseImplTest. Pure churn: resultLauncher bodies untouched, the two reactive use cases keep returning bare Flows | ["2"] | ["AC-49"] | 0.25d |
| 4 | COMMIT 1 — accept `platform` in DeviceRepositoryImpl (unused, `@Suppress("UNUSED_PARAMETER")`, documented); update MirrorViewModel's call sites — `state.platform` at the four intent-scoped ones and the LITERAL `PlatformModel.Android` at the two flow-collection ones (observeDiscoveryWhileIdle, observeStats), which cannot read state; update MirrorFakes, MirrorViewModelVerifyTest's inline DumpHierarchyUseCase fake (line 201) and the four data-repository-impl device test files. Run the full existing suite; the word-diff must show only the five token shapes of PLAN-AS-1. MERGE AS A STANDALONE COMMIT — 20 files, AC-49's evidence | ["3"] | ["AC-49"] | 0.5d |
| 5 | COMMIT 2 — widen the closed sets in data-device-api/commonMain: ToolKind.Simctl("xcrun", null), DeviceCommand.SimulatorBoot, HostCommand.{XcrunVersion, ListSimulators, ListTargets} with their buildHostArgv entries (all in DeviceArgv.kt), DeviceCommand.Screencap's KDoc rewritten platform-neutral, each new member carrying a 'widened for F-10 (FR-49)' line, and data-device-api/CLAUDE.md's closed-set counts updated | ["4"] | ["AC-48"] | 0.25d |
| 6 | COMMIT 2 — generalize buildDeviceArgv to (tool, toolPath, device, command): a per-tool `when` producing the SELECTOR SEGMENT, `check(selector.count { it == device.value } == 1)`, then the payload and the denylist check. Update the one Android call site. Write the EXHAUSTIVE ToolKind x DeviceCommand sweep (argv[0] == toolPath, id exactly once in the selector, no connect/-H/-P element, Android-only commands throwing on iOS tools, and GetProp(name = device.value) building WITHOUT throwing). Verify by mutation: delete one selector and confirm the sweep goes red | ["5"] | ["AC-48"] | 0.5d |
| 7 | COMMIT 2 — add SimulatorResource / IosTargetResource and the two hand-rolled parsers (parseSimctlDevices, parseIdbTargets) in data-device-api/commonMain, one concern per file (PP-TD-6), with fixture tests pinned to step 1's captured output. Neither parser throws; parseIdbTargets' untrusted detection lives behind ONE named token set (AS-2) | ["1", "5"] | ["AC-46", "AC-31"] | 0.75d |
| 8 | COMMIT 2 — add IosHostSource (four methods, no bootSimulator) to data-device-api; DELETE IdbHostSource | ["7"] | ["AC-46"] | 0.25d |
| 9 | COMMIT 2 — ToolResolver and probeCandidates learn ToolKind.Simctl (/usr/bin candidate, readVersion via `xcrun --version`, installCommand 'xcode-select --install', envHint ''). Test through the INJECTED isExecutable/identifyAdb/userPath predicates — never spawn inside runTest's virtual clock | ["5"] | ["AC-46"] | 0.5d |
| 10 | COMMIT 2 — IosHostSourceImpl and IosDeviceCommandExecutor in data-device-impl/src/desktopMain (NFR-19); rename DeviceCommandExecutorImpl -> AdbDeviceCommandExecutor and its test. probeSimctl verifies the SUBCOMMAND resolves; SimulatorBoot runs at DaemonStartTimeoutMs and swallows ONLY the named already-booted stderr token (never an exit-code number); Screencap uses `simctl io <udid> screenshot --type=png -`, with the idb path following PLAN-AS-4 (stdout if accepted, temp-file round trip otherwise). ScreencapMirrorTransport and ProcessRunner are NOT opened | ["6", "8", "9"] | ["AC-47", "AC-48"] | 1d |
| 11 | COMMIT 2 — rewrite ALL THREE provideDeviceSources() actuals. desktop: unqualified singleOf for AdbHostSourceImpl/EmulatorSourceImpl/IosHostSourceImpl plus explicit `single<DeviceCommandExecutor>(named(...))` and `single<MirrorTransport>(named(...)) { ScreencapMirrorTransport(executor = get(named(...))) }`. android + ios: five registrations become seven, UnsupportedIdbHostSource replaced by UnsupportedIosHostSource and the existing Unsupported executor/transport registered under both qualifiers. Leave NO unqualified binding of those two interfaces anywhere. Extend DeviceSourcesModuleTest to resolve all three graphs | ["10"] | ["AC-48", "AC-49"] | 0.5d |
| 12 | COMMIT 2 — domain-core: add DiscoveryModel.SimulatorNotBooted and DiscoveryModel.DeviceNotTrusted, widen EmulatorModel.runningDeviceId's KDoc (udid even when shut down), drop PlatformModel.Ios's 'not wired in this feature' line. MirrorStatsModel is NOT touched — toolVersion is already neutral | ["4"] | ["AC-46", "AC-31"] | 0.25d |
| 13 | COMMIT 2 — data-repository-impl: split poll() into pollAndroid() (MOVED VERBATIM) and pollIos() (new); add the exhaustive `when (platform)` executor/transport selection; re-point detectTools() from idbHostSource.probe() to iosHostSource.probeSimctl(); rename the private adbVersion flow to toolVersion and feed it from whichever poll ran; route bootEmulator(Ios, udid) through the executor's SimulatorBoot instead of AvdName.of; short-circuit dumpHierarchy(Ios); FIX the PollFailed catch at line 146 so it names the platform's own tool; add SimulatorResourceMapper; extend ToolProbeMapper for Simctl -> Ios; update DataRepositoryImplModule's constructor call | ["11", "12"] | ["AC-46", "AC-49", "AC-50"] | 1d |
| 14 | COMMIT 2 — data-repository-impl tests: DeviceIosDiscoveryPollingTest against fake sources under virtual time, covering SimulatorNotBooted, DeviceNotTrusted, Found, ToolMissing, NoDeviceFound, the idb-fails-but-simctl-survives degradation, the parser-drift PollFailed path, PollFailed naming xcrun rather than adb, and the AC-50 'executor is never called' assertion (fake executor throws if invoked) | ["13"] | ["AC-46", "AC-50", "AC-31"] | 0.75d |
| 15 | COMMIT 2 — presentation state: DELETE MirrorStage.IosDeferred, its derivation branch (MirrorContract.kt:358) and its render branch (MirrorContent.kt:131-137) plus the three commonTest assertions on it; add MirrorErrorKind.{SimulatorNotBooted, DeviceNotTrusted} and MirrorErrorCard.platform; rewrite the two KDoc paragraphs that call err-sim/err-trust deferred; extend applyDiscovery with two branches and put the simulator runtime in the iOS row detail (PP-TD-9); add errorCard()'s two arms, the platform-correct tool/command/diagnostic lines for the shared variants, and the EXTENDED picker-yields guard (SimulatorNotBooted always, DeviceNotTrusted when simulatorsAvailable) without which the secondaries are dead buttons | ["12"] | ["AC-31", "AC-46"] | 0.75d |
| 16 | COMMIT 2 — MirrorViewModel: replace the two commit-1 literals with real platform; make observeDiscoveryWhileIdle `combine(sessionDeviceId, discoveryPlatform).flatMapLatest { ... }` and do the same for observeStats; selectPlatform() writes discoveryPlatform and resets discovery = Detecting / rows = emptyList() / promptOverride = null (PP-TD-2) in the same reduction; add bootTargetOf(row) and route commit(), bootRow() and bootSelectedAvd() through it. Add MirrorViewModelIosPlatformSwitchTest and MirrorIosErrorCardTest (including the fixture-driven, WAIVED device-not-trusted half) | ["15", "13"] | ["AC-31", "AC-46", "AC-47"] | 0.75d |
| 17 | COMMIT 2 — UI + copy: MirrorErrorStage's two new branches plus the card.platform branch on the ToolMissing and NoDevice arms (iOS never offers Locate adb / Restart adb), with both new cases added to MirrorErrorStageSample so the light/dark @Preview pair still covers every kind; ~12 new strings (watch the apostrophe and em dash in "I've tapped Trust — retry"); DELETE mirror_ios_deferred; relabel mirror_group_emulator's value; replace platformCards()'s hardcoded `enabled = platform == Android` and update PickerPlatform.enabled's KDoc | ["15"] | ["AC-31"] | 0.5d |
| 18 | COMMIT 2 — the one real-toolchain test (AC-46's measured half): a desktopTest that spawns `xcrun simctl list devices available` through ProcessRunner, feeds stdout to parseSimctlDevices, and asserts simulators found with NONE Booted. It SKIPS (not fails) when xcrun is absent, per ProcessRunnerTest's precedent. Always-run-with-skip, not tag/ignore | ["10"] | ["AC-46", "AC-31"] | 0.25d |
| 19 | COMMIT 2 — documentation truth-up in the same commit as the code: data-device-api/CLAUDE.md (closed-set counts, NFR-5 table), data-device-impl/CLAUDE.md (datasource inventory, unsupported list), presentation-feature-mirror/CLAUDE.md (MirrorStage case list) | ["15", "11"] | ["AC-49"] | 0.25d |
| 20 | Gate: `./gradlew :data-device-api:desktopTest :data-device-impl:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest :domain-usecase-impl:desktopTest`, then the cross-target build `./gradlew :data-device-api:build :data-repository-impl:build :presentation-feature-mirror:build --max-workers=1` (PP-TD-8). Grep-verify every deletion. Confirm zero diff in ScreencapMirrorTransport.kt, ProcessRunner.kt, MirrorTuning.kt, MirrorStatsModel.kt, :compose-application and :ios-application, and that MirrorIntent gained no member | ["14", "16", "17", "18", "19"] | ["AC-49", "AC-50"] | 0.5d |
| 21 | Manual demo + verification table (AC-48, SC-25, SC-27): boot a simulator, mirror it, record the MEASURED captureRoundTripMs as a number, exercise Boot simulator from the error card AND 'Choose another' (proving the picker-yields guard), and write the honest coverage table — AC-31 split (measured vs fixture-waived), SC-26 physical path code-review-only, AC-48 demo-verified | ["20"] | ["AC-48", "AC-31"] | 0.5d |

### testStrategy

#### unit

- parseSimctlDevices — data-device-api/commonTest, task `:data-device-api:desktopTest`. Fixture tests over THIS host's real `xcrun simctl list devices available` output: runtime headers, Booted and Shutdown rows, the duplicate 'iPhone 16' across two runtimes, an unparseable line skipped, an `(unavailable, ...)` row dropped, empty input -> empty list. Never throws. Zero hardware.
- parseIdbTargets — data-device-api/commonTest, `:data-device-api:desktopTest`. Pipe splitting, target_type filtering, and the untrusted token set (AS-2) pinned by fixture. Never throws.
- buildDeviceArgv — data-device-api/commonTest, `:data-device-api:desktopTest`. The EXHAUSTIVE ToolKind x DeviceCommand sweep: argv[0] == toolPath, the id exactly once IN THE SELECTOR SEGMENT, no connect/-H/-P element, Android-only commands throwing the documented DeviceCommandException on Simctl/Idb, and GetProp(name = device.value) building without throwing. This test IS NFR-5's proof and must be mutation-verified.
- probeCandidates — data-device-api/commonTest, `:data-device-api:desktopTest`. ToolKind.Simctl's /usr/bin candidate ordering against a fake env.
- ToolResolver — data-device-impl/desktopTest, `:data-device-impl:desktopTest`. Simctl resolution via the INJECTED isExecutable/identifyAdb/userPath predicates. NEVER spawn inside runTest: ToolResolver's own KDoc records that a real spawn always times out against the virtual clock, which is exactly why these are parameters — and why IosHostSourceImpl gets no unit test of its own.
- errorCard() / DiscoveryErrorProjection — presentation-feature-mirror/commonTest, `:presentation-feature-mirror:desktopTest`. The two new iOS arms with the right kind and the right primary/secondary; the SHARED variants carrying platform = Ios and iOS tool text rather than 'adb'; and the extended picker-yields guard (isPickerOpen + SimulatorNotBooted -> null; isPickerOpen + DeviceNotTrusted(simulatorsAvailable = true) -> null; simulatorsAvailable = false -> card kept). Pure, device-free.
- applyDiscovery / DiscoveryProjection — presentation-feature-mirror/commonTest, same task. A shut-down simulator becomes MirrorRowGroup.Emulator + MirrorRowKind.Boot with id = udid and serial = udid; two same-named simulators on different runtimes produce two rows distinguishable by `detail`.
- bootTargetOf — presentation-feature-mirror/commonTest, same task. On iOS a Boot row resolves to row.id (the udid), on Android to row.avdName. The regression this prevents is booting 'iPhone 16' when two runtimes ship one.
- SimulatorResourceMapper / ToolProbeMapper — data-repository-impl/commonTest, `:data-repository-impl:desktopTest`. toModel never throws, an unknown state gets a safe default, Simctl maps to PlatformModel.Ios.
- MirrorContract — presentation-feature-mirror/commonTest, same task. The stage derivation no longer has an Ios branch and a `when` over MirrorStage still compiles exhaustively after IosDeferred's deletion.

#### integration

- DeviceIosDiscoveryPollingTest — data-repository-impl/commonTest, `:data-repository-impl:desktopTest`, fake IosHostSource under virtual time. pollIos classifies Found / SimulatorNotBooted / DeviceNotTrusted / ToolMissing / NoDeviceFound; an idb failure degrades the physical list to empty WITHOUT erasing simctl's answer; 'succeeded, zero rows, non-empty stdout' emits PollFailed(tool = "xcrun"); PollFailed names xcrun rather than adb.
- AC-50 — same module and task. dumpHierarchy(Ios, ...) returns HierarchySnapshotModel.Empty against a fake DeviceCommandExecutor whose run() THROWS: the test passes only because the executor is never reached. Fails the day iOS starts returning nodes.
- Transport loop against a FAKE iOS executor under virtual time — data-device-impl/desktopTest, `:data-device-impl:desktopTest`, reusing ScreencapMirrorTransportTest's existing shape: frames emitted, dropped-frame arithmetic, cancellation stops the loop, a mid-stream DeviceCommandException propagates. The transport file itself is not edited.
- Koin graph — data-device-impl/desktopTest, same task. DeviceSourcesModuleTest extended: both qualifiers resolve on desktop AND on the android and ios graphs (FR-56), and no unqualified DeviceCommandExecutor/MirrorTransport resolves anywhere. A missing qualifier here is a phone startup crash, not a desktop test failure.
- MirrorViewModelIosPlatformSwitchTest — presentation-feature-mirror/commonTest, `:presentation-feature-mirror:desktopTest`. Switching to iOS re-keys the flatMapLatest so the Android flow is cancelled, resets discovery to Detecting / rows to empty / promptOverride to null, and a late emission from the cancelled Android flow cannot resurrect stale state or raise a spurious error side effect.
- AC-47 — same module and task. BootRow(udid) from the error card dispatches bootEmulator(Ios, udid) and the next poll surfaces the simulator as selectable, with no restart. Driven through fakes.
- SimctlRealToolchainTest — data-device-impl/desktopTest, `:data-device-impl:desktopTest`. The ONE genuine hardware claim: spawns `xcrun simctl list devices available` through ProcessRunner, parses it, asserts simulators found with none Booted. SKIPS when xcrun is absent. It lives in desktopTest and not commonTest precisely because it spawns.
- AC-49/SC-29 — the ENTIRE pre-existing Android suite, run unchanged after commit 1 and again after commit 2. The regression guard, not a new test.

#### ui

- No Compose UI-test harness exists in this repo and introducing one is out of scope. The two new MirrorErrorStage branches are covered by the extended light/dark @Preview pair plus the pure errorCard() projection tests — exactly the standing of every other card. Stated as a limitation, not claimed as coverage.
- NOTHING testable lands in :compose-application (no test wiring by design) or in :domain-core (no test source set in use here); every assertion above names a module that already runs a desktopTest task.
- AC-48 is verified by DEMO (its declared verificationMethod): boot a simulator, start mirroring, observe live frames and the fps/resolution status line, record the measured captureRoundTripMs as a number.
- SC-27's click-through (error card -> Boot simulator -> selectable, and error card -> Choose another -> picker actually opens) is demo-verified end to end; both logic halves are machine-verified above.

#### linkedScenarios

- SC-14
- SC-25
- SC-26
- SC-27
- SC-28
- SC-29

#### coverageGoal

Every pure/logic surface this spec adds is machine-covered in a module that already has a desktopTest task: both parsers and the argv sweep in :data-device-api; ToolResolver's Simctl branch, the Koin graph for all three target graphs and the transport loop in :data-device-impl; pollIos's five classifications and the AC-50 never-reach-the-executor assertion in :data-repository-impl; errorCard()'s two arms, the shared-variant platform copy, the extended picker-yields guard, applyDiscovery's two branches, bootTargetOf and the platform-switch cancellation in :presentation-feature-mirror; the use-case ripple in :domain-usecase-impl. ONE guarded real-toolchain desktopTest provides the only genuine hardware claim and SKIPS where xcrun is absent. HONESTLY UNCOVERED, stated rather than implied: (1) AC-31 IS SPLIT — the simulator-not-booted half is REAL AND MEASURED on this host, the device-not-trusted half is FIXTURE-DRIVEN AND WAIVED per NFR-18/AS-3, and the measured half must never be reported as covering the waived one; (2) the whole SC-26 physical-device path is CODE-REVIEW-VERIFIED ONLY — idb_companion's running state at poll time has never been exercised here and `idb screenshot`'s stdout contract is unverified (PLAN-AS-4); (3) all rendering is @Preview-only, repo-wide; (4) AC-48 is demo-verified by its own declared verificationMethod; (5) the iOS capture rate is measured-and-recorded, never asserted against a threshold, because the Android baseline is itself unmeasured (W-2); (6) `simctl boot`'s exit code for an already-booted simulator was not measured, so the success path keys on a stderr token instead. ProcessRunner spawning inside runTest's virtual clock always times out — that constraint shapes every seam above, is why IosHostSourceImpl has no unit test, and is why nothing testable lands in :compose-application.

#### fileChangeList

##### commit1PureRefactorNoIosCode

###### summary

20 files, ALL edited. Zero added, zero deleted, zero new tests. The word-diff shows only `platform`, `state.platform`, `PlatformModel.Android`, two imports and one `@Suppress`. This commit is AC-49's evidence. (Recount of C-2: the draft said 19 and missed MirrorViewModelVerifyTest.kt, which implements DumpHierarchyUseCase inline at line 201.)

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
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelVerifyTest.kt

##### commit2Ios

###### summary

~55 file-level changes: 16 added, 35 edited, 2 renamed, 2 deleted. ~9 files overlap with commit 1, so the two commits together touch ~66 distinct files. Corrections to the draft's 47: ADDED DeviceSourcesProvider.android.kt and DeviceSourcesProvider.ios.kt (a missing qualifier there is a phone startup crash), MirrorViewModelAnnotationTest.kt, MirrorViewModelSessionTest.kt and VerifyStageDerivationTest.kt (all three assert on MirrorStage.IosDeferred), and three module CLAUDE.md files; REMOVED MirrorStatsModel.kt (the simctlVersion cut).

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
- data-device-impl/src/desktopTest/kotlin/data/device/impl/datasource/SimctlRealToolchainTest.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/SimulatorResourceMapper.kt
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceIosDiscoveryPollingTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorIosErrorCardTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelIosPlatformSwitchTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/BootTargetTest.kt

###### edited

- data-device-api/src/commonMain/kotlin/data/device/api/source/model/ToolKind.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceCommand.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt (HostCommand + buildHostArgv + buildDeviceArgv all live here)
- data-device-api/src/commonMain/kotlin/data/device/api/source/tool/ToolProbeCandidates.kt
- data-device-api/src/commonTest/kotlin/data/device/api/DeviceArgvTest.kt
- data-device-api/src/commonTest/kotlin/data/device/api/ToolProbeCandidatesTest.kt
- data-device-api/CLAUDE.md
- data-device-impl/src/desktopMain/kotlin/data/device/impl/core/tool/ToolResolver.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/DeviceSourcesProvider.desktop.kt
- data-device-impl/src/androidMain/kotlin/data/device/impl/source/DeviceSourcesProvider.android.kt
- data-device-impl/src/iosMain/kotlin/data/device/impl/source/DeviceSourcesProvider.ios.kt
- data-device-impl/src/commonMain/kotlin/data/device/impl/core/unsupported/UnsupportedDeviceSources.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/tool/ToolResolverTest.kt
- data-device-impl/src/desktopTest/kotlin/data/device/impl/di/DeviceSourcesModuleTest.kt
- data-device-impl/CLAUDE.md
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/DiscoveryModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/EmulatorModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/PlatformModel.kt
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
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelAnnotationTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelSessionTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/VerifyStageDerivationTest.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt
- presentation-feature-mirror/CLAUDE.md
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
- the IosDeferred render branch (MirrorContent.kt:131-137) and its mirror_ios_deferred import
- three commonTest assertions on it (MirrorViewModelAnnotationTest:287, MirrorViewModelSessionTest:165, VerifyStageDerivationTest:62)
- string mirror_ios_deferred (strings.xml:102)
- UnsupportedIdbHostSource (UnsupportedDeviceSources.kt) and its two registrations

###### deliberatelyUntouched

- data-device-impl/.../source/transport/ScreencapMirrorTransport.kt — registered twice, edited zero times
- data-device-impl/.../core/process/ProcessRunner.kt — edited zero times
- data-device-api/.../source/tuning/MirrorTuning.kt — no new tuning constants
- domain-core/.../model/device/MirrorStatsModel.kt — toolVersion is already platform-neutral
- MirrorIntent (inside MirrorContract.kt) — no new member; BootRow, OpenPicker and RetryDiscovery cover all four card actions
- compose-application/**, ios-application/**, presentation-core-ui/**
- gradle/libs.versions.toml and settings.gradle.kts (no new dependency, no new module)

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-31 | ["FR-25", "FR-51", "FR-52", "FR-53", "FR-55"] | ["DiscoveryModel.SimulatorNotBooted / DeviceNotTrusted", "pollIos()", "DiscoveryErrorProjection.errorCard() — two arms + extended picker-yields guard", "MirrorErrorKind.SimulatorNotBooted / DeviceNotTrusted + MirrorErrorCard.platform", "MirrorErrorStage / ErrorCard organism (components.json#error-card)", "bootTargetOf(row) + the reused BootRow/OpenPicker/RetryDiscovery intents", "parseSimctlDevices", "parseIdbTargets"] | ["MirrorIosErrorCardTest (projection, both halves) — :presentation-feature-mirror:desktopTest", "SimctlRealToolchainTest — simulator-not-booted half: REAL AND MEASURED on this host — :data-device-impl:desktopTest", "IdbTargetsParserTest + a constructed DiscoveryModel.DeviceNotTrusted — device-not-trusted half: FIXTURE-DRIVEN AND WAIVED per NFR-18/AS-3; the measured half must NOT be read as covering it", "DiscoveryErrorProjectionTest — the picker-yields guard, without which both secondaries are dead buttons", "SC-14, SC-27, SC-28"] |
| AC-46 | ["FR-48", "FR-51", "FR-52"] | ["pollIos()", "IosHostSource / IosHostSourceImpl", "parseSimctlDevices", "DiscoveryErrorProjection — platform-correct copy for the shared variants", "MirrorViewModel — platform as a flatMapLatest key", "observeDiscovery's PollFailed catch — tool-name fix"] | ["SimctlRealToolchainTest (real `xcrun simctl list devices available`, skip-guarded)", "DeviceIosDiscoveryPollingTest (SimulatorNotBooted classification; PollFailed names xcrun)", "DiscoveryErrorProjectionTest (no Android copy renders on iOS)", "MirrorViewModelIosPlatformSwitchTest (the adb flow is structurally cancelled, state resets to Detecting)", "SC-27"] |
| AC-47 | ["FR-49", "FR-51", "FR-53"] | ["IosDeviceCommandExecutor.SimulatorBoot (run(), not spawnDetached; already-booted stderr token == success)", "DeviceCommand.SimulatorBoot", "MirrorIntent.BootRow (reused) + bootTargetOf", "EmulatorModel.runningDeviceId (KDoc widened — carries the udid when shut down)", "DeviceRepository.bootEmulator(platform, target)"] | ["MirrorViewModelIosPlatformSwitchTest (BootRow(udid) -> bootEmulator(Ios, udid) -> refreshDiscovery, no restart)", "BootTargetTest (iOS boots by udid, Android by AVD name)", "DeviceIosDiscoveryPollingTest (re-poll surfaces the booted simulator as selectable)", "DeviceArgvTest sweep (SimulatorBoot x Simctl argv carries the udid exactly once)", "SC-27"] |
| AC-48 | ["FR-49", "FR-50", "FR-54"] | ["buildDeviceArgv(tool, toolPath, device, command)", "IosDeviceCommandExecutor", "ScreencapMirrorTransport (reused verbatim, registered twice)", "ProcessRunner (reused verbatim)", "provideDeviceSources() — qualified bindings", "DeviceRepositoryImpl (exhaustive `when` dispatch)"] | ["DeviceArgvTest (exhaustive sweep incl. Screencap x Simctl and Screencap x Idb)", "Transport loop against a fake iOS executor under virtual time", "DeviceSourcesModuleTest (both qualifiers resolve)", "DEMO (AC-48's declared verificationMethod) — live frames + measured captureRoundTripMs as a number", "SC-25; SC-26 is CODE-REVIEW-VERIFIED ONLY"] |
| AC-49 | ["FR-48", "FR-56"] | ["DeviceRepository (five methods gain `platform`)", "the five use-case interfaces and impls", "DeviceRepositoryImpl (pollAndroid() = verbatim move)", "MirrorViewModel (six call sites)", "UnsupportedDeviceSources + all three provideDeviceSources actuals", "AdbDeviceCommandExecutor (renamed, body unchanged)"] | ["The ENTIRE pre-existing Android suite, run unchanged after commit 1 AND after commit 2", "`git diff --word-diff` on commit 1 showing only PLAN-AS-1's five token shapes across 20 files (inspection evidence attached to the test claim)", "DeviceSourcesModuleTest (desktop, android and ios graphs all resolve)", "Cross-target build with --max-workers=1 (PP-TD-8)", "SC-29"] |
| AC-50 | ["FR-57"] | ["DeviceRepositoryImpl.dumpHierarchy(Ios) — short-circuits to HierarchySnapshotModel.Empty without spawning", "attachedHierarchyXml (Android-gated, unchanged)", "HierarchySnapshotModel.rawDump (unchanged, \"\" on iOS)"] | ["INSPECTION (AC-50's declared method) — rawDump is \"\" and AnnotationEnvelope.hierarchyXml is NOT renamed", "DeviceIosDiscoveryPollingTest's AC-50 case: a fake executor whose run() THROWS; the test passes only because it is never reached (guards against PP-TD-5's tautology)"] |
| SC-14 | ["FR-25", "FR-51", "FR-53"] | ["DiscoveryModel's two new variants", "MirrorErrorKind's two new members", "MirrorErrorStage's two new branches"] | ["MirrorIosErrorCardTest", "@Preview light/dark pair via the extended MirrorErrorStageSample"] |
| SC-25 | ["FR-51", "FR-54"] | ["pollIos() -> Found", "IosDeviceCommandExecutor (simctl Screencap)", "ScreencapMirrorTransport (unchanged)"] | ["DeviceIosDiscoveryPollingTest (Found)", "Transport loop against a fake iOS executor", "DEMO with a booted simulator + measured captureRoundTripMs"] |
| SC-26 | ["FR-51", "FR-54"] | ["parseIdbTargets", "IosHostSourceImpl.listDevices", "IosDeviceCommandExecutor (idb Screencap — PLAN-AS-4)"] | ["IdbTargetsParserTest (fixture)", "DeviceArgvTest (Screencap x Idb)", "CODE REVIEW ONLY for the live path: no physical iPhone and no exercised idb_companion on this host; idb's stdout contract is unverified"] |
| SC-27 | ["FR-51", "FR-53", "FR-55"] | ["pollIos() -> SimulatorNotBooted", "errorCard()'s SimulatorNotBooted arm + picker-yields guard", "BootRow + bootTargetOf", "IosDeviceCommandExecutor.SimulatorBoot"] | ["MirrorIosErrorCardTest", "BootTargetTest", "DeviceIosDiscoveryPollingTest", "DEMO click-through (Boot simulator AND Choose another)"] |
| SC-28 | ["FR-51", "FR-53"] | ["DiscoveryModel.DeviceNotTrusted", "errorCard()'s DeviceNotTrusted arm (yields to the picker only when simulatorsAvailable)", "OpenPicker / RetryDiscovery (reused)"] | ["MirrorIosErrorCardTest from a constructed fixture — WAIVED per NFR-18/AS-3, nothing about real idb trust behaviour is proven"] |
| SC-29 | ["FR-48", "FR-56"] | ["every commit-1 file", "pollAndroid() verbatim move", "AdbDeviceCommandExecutor"] | ["The entire pre-existing Android suite after both commits", "Cross-target build --max-workers=1"] |
| NFR-5 | ["FR-49", "FR-50"] | ["buildDeviceArgv's per-tool selector segment + selector-scoped check()", "DeviceId's private constructor", "the closed DeviceCommand/HostCommand sets", "ProcessRunner as the single spawn site"] | ["DeviceArgvTest's exhaustive ToolKind x DeviceCommand sweep (argv[0], exactly-one-in-selector, denylist, documented throws, GetProp(name = udid) does NOT throw)", "Mutation check: delete one selector branch and the sweep must go red"] |
| NFR-8 | ["FR-55"] | ["pickerKeyAction / nextSelectableIndex / onPreviewKeyEvent + FocusRequester (unchanged)", "ErrorCard's focusable AppButtons", "MirrorRowKind.Boot for shut-down simulators"] | ["The existing PickerNavigationTest stays green (pure arithmetic only — live dispatch remains unverified, same standing as Android, per app-shell waiver 2)"] |
| NFR-9 | ["FR-53"] | ["MirrorErrorStage's two new branches through the shipped ErrorCard", "Theme.color tokens only"] | ["Extended MirrorErrorStageSample with light/dark @Preview pair — the repo's only visual coverage, stated as a limitation"] |
| NFR-10 | ["FR-48", "FR-56"] | ["all three provideDeviceSources actuals", "UnsupportedIosHostSource", "desktopMain-only iOS implementations"] | ["DeviceSourcesModuleTest across desktop/android/ios graphs", "Cross-target build (no Windows/Linux target exists to break)"] |
| NFR-18 | ["FR-51", "FR-53"] | ["SimctlRealToolchainTest (skip-guarded)", "the constructed DeviceNotTrusted fixture"] | ["The AC-31 coverage split written verbatim into the verification table: half (a) measured, half (b) WAIVED"] |
| NFR-19 | ["FR-54", "FR-56"] | ["IosHostSourceImpl and IosDeviceCommandExecutor under desktopMain only", "data-device-api's pure commonMain parsers", "ios-application untouched"] | ["Cross-target build proves commonMain names no java.*", "Grep gate: exactly one ProcessBuilder path in the repo", "Zero-diff check on :ios-application"] |
| NFR-20 | ["FR-48"] | ["commit 1 as a standalone pure refactor", "pollAndroid() verbatim move", "DeviceRepositoryImpl's ignored parameter"] | ["The pre-existing Android suite green after commit 1 and after commit 2 (AC-49)", "Word-diff inspection of commit 1"] |

### openQuestions

- AS-2 / CARRIED: the exact `idb list-targets` field or error signal that identifies an untrusted physical iPhone. Recommendation on record and adopted: contain it in ONE named token set inside parseIdbTargets plus a fallback classifying a listed-but-unusable physical target as untrusted. Does not block implementation.
- PLAN-AS-4 / NEW: whether `idb screenshot` will write PNG bytes to stdout the way `simctl io ... screenshot -` does. Unverified on this host. Contained inside IosDeviceCommandExecutor (temp-file round trip if not), which leaves the transport and the argv invariant untouched. SC-26 is code-review-only regardless.
- NEW (plan, C-1 RESOLVED WITH A CAVEAT): Preparation describes commit 1 as 'qualified Koin bindings + platform threaded through'. A named("ios") binding cannot exist with zero iOS code, so the qualifier registration moves to commit 2. The reviewability claim survives but must be stated accurately: the word-diff contains five token shapes, not just `platform`, because two MirrorViewModel flow sites cannot read `state` and take the literal `PlatformModel.Android` until commit 2.
- NEW (plan, C-2 RESOLVED): the honest file counts are 20 for the pure refactor and ~55 for the iOS commit (~66 distinct). Preparation's ~45 was a whole-spec guess attached to the refactor; the draft's 19 undercounted the test ripple. Worth correcting in the backlog so nobody quotes 45 for the refactor.
- NEW (plan): `simctl boot`'s exit code for an already-booted simulator was NOT measured (no simulator was booted at probe time, and booting one is a side effect the probe declined). The plan keys success on the stderr token instead. If the measured code turns out to be stable it may be worth asserting both — but not before it is measured.
- CARRIED: whether AX JSON hierarchy capture is worth the rawDump/hierarchyXml rename in a future v1.1, now that iOS mirroring and error cards ship without it.
- CARRIED: whether idb video-stream's higher fps is worth the streaming-ProcessRunner + H.264/MJPEG decode investment. Decide after step 21's measured number.
- CARRIED: whether bootEmulator should be renamed bootTarget. Adopted: keep the name, add platform, widen the KDoc.
- CARRIED: whether observeMirrorStats needs a second version field. RESOLVED AGAINST the draft's recommendation: MirrorStatsModel.toolVersion is already platform-neutral, so no new field — only a private flow rename inside DeviceRepositoryImpl.
- CARRIED: picker group label wording. Adopted: one neutral value under the existing mirror_group_emulator key; the 'Emulators'/'Simulators' pair needs a new DevicePickerPanel parameter and is noted as a cosmetic upgrade path.
- CARRIED: always-run-with-skip vs tag/ignore for the real-toolchain simctl test. Adopted: always run with a skip — it is the one honest hardware-coverage claim this feature can make.
- CARRIED: whether the iOS simulator row should carry its runtime in MirrorRow.detail. Adopted: yes — additive, Android untouched, and the only thing that disambiguates two identically-named simulators. PP-TD-9's Android half stays open under its own gate.
- NEW (plan): whether to also fix PP-TD-2's promptOverride-on-ClearStaged defect while MirrorViewModel is open. Adopted: NO for ClearStaged, YES for the one line this spec is already writing — selectPlatform()'s reset includes promptOverride, because omitting it would reproduce the same defect on a new axis.

### readyForBacklog

true

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| 'Boot simulator' on the err-sim card: add MirrorIntent.BootSimulator(udid) as the draft planned, or reuse the existing MirrorIntent.BootRow(id) with a platform-aware boot target? | ["Reuse BootRow(id) + a bootTargetOf(row) helper — zero new intents", "Add MirrorIntent.BootSimulator(udid) as the draft planned"] | Reuse BootRow(id) + a bootTargetOf(row) helper — zero new intents | ai-proposed | bootRow() already resolves the row from state.rows and boots it, MirrorErrorCard.primaryDeviceId already carries the udid, and SimulatorNotBooted already projects simulator rows into state.rows. The only genuinely missing piece is a platform-aware boot target — and that is needed anyway, because EmulatorModel.toRow sets avdName for every platform and today's commit()/bootRow()/bootSelectedAvd() all call boot(row.avdName ?: row.id), i.e. they boot 'iPhone 16' by NAME, which is ambiguous across the iOS 17.5 and 18.1 runtimes on this host. One helper at three call sites fixes a real bug AND removes the new intent; a second intent reducing to the same handler is pure duplication. |
| iOS copy for the SHARED error states (ToolMissing, NoDeviceFound, MultipleDevices): add iOS-worded strings and a `platform` field on MirrorErrorCard, or keep the draft's guard where errorCard() returns null on iOS for those variants? | ["Add MirrorErrorCard.platform + 4 iOS-worded strings; MirrorErrorStage branches copy and suppresses adb-only actions", "Keep the draft's guard: errorCard() returns null for those variants when platform == Ios"] | Add MirrorErrorCard.platform + 4 iOS-worded strings; MirrorErrorStage branches copy and suppresses adb-only actions | ai-proposed | Those variants are not Android-only — iOS genuinely reaches ToolMissing (no simctl on a CLT-only Mac), NoDeviceFound (no simulators and no devices) and MultipleDevices. The draft's guard would hand iOS a blank stage for three real conditions while still letting the hardcoded 'adb' strings in DiscoveryErrorProjection (ADB, ADB_RESTART_COMMAND, 'adb devices -l') be the only text the user could ever see. One defaulted data-class field plus four strings is the smaller honest fix, and it keeps the pure projection as the single place that decides what the card says. |
| The NFR-5 argv invariant: assert the device id appears exactly once across the WHOLE argv (the draft), or only within the per-tool selector segment? | ["Selector-segment scoped: check(selector.count { it == device.value } == 1), payload covered by the denylist", "Whole-argv scoped exactly as the draft wrote it"] | Selector-segment scoped: check(selector.count { it == device.value } == 1), payload covered by the denylist | ai-proposed | DeviceCommand.GetProp carries unvalidated free text (the recorded SEC-1 waiver). A whole-argv count means GetProp(name = <the udid>) produces a count of 2 and throws IllegalStateException out of a pure commonMain function on data a caller can legally supply — turning a latent waiver into a live crash. Scoping to the selector keeps the full security property (exactly one explicitly selected target, no connect/-H/-P) while making it un-trippable by data, and the sweep test adds GetProp(name = device.value) as an explicit must-not-throw case. |
