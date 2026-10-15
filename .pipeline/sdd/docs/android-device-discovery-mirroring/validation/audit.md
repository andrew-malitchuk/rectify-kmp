# android-device-discovery-mirroring — audit

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | audit |
| status | done |
| producedBy | sdd-build-validation-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-15T02:13:31Z |
| updatedAt | 2026-10-15T02:13:31Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-15T02:13:31Z

## Provenance

### selfHash

ce4c7dcc475934eb062f5e12933bd15a2831ee45737db758a340a4d39a79ec36

### inputs

| artifact | hash |
| --- | --- |
| plan/plan.json | 685e70a97c538c5bf3ac6b5b50a06601c803863ee89f04bc6dfccfc721dd1d3e |
| backlog/backlog.json | 5f2a490e5882f332cb358e5e0bb113a5f5c69beedb37ec61f3ba29412a140056 |

## Data

### iteration

1

### verdict

F-2 is substantially delivered and the architecture claims hold up under adversarial checking. Three real gaps block a clean close: the status bar reports a capture round-trip as 'latency' where the plan mandates an explicit placeholder, the iOS tool dot is hardcoded instead of probed (the probe machinery already exists and is unit-tested), and Escape/close is unimplemented for the device picker (A11Y-4). Everything else flagged is either a human-gate judgement call or cosmetic.

### findings

| id | severity | about | area | ref | target | recommendedTarget | issue | description | evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| AF-1 | error | ac | ac | AC-45 | T-4 | T-4 | The status bar will print a latency NUMBER where the plan mandates an explicit placeholder. ScreencapMirrorTransport sets latencyMs to the host-side capture round-trip on every frame, so TransportStats.latencyMs is never null during a session and StatsFormatter's placeholder branch is dead in production. A reader will take that number for the NFR-1 glass-to-glass latency it is not — which is precisely the honesty the NFR-1 waiver was granted on. | ScreencapMirrorTransport.kt:95 reports captureMs as latencyMs; plan requires null + placeholder for the polling transport. | data-device-impl/src/desktopMain/kotlin/data/device/impl/source/transport/ScreencapMirrorTransport.kt:95 `latencyMs = captureMs,` — unconditional. Contradicts plan.performanceBudget 'Status-bar content' -> "latency as '—'" and the NFR-1 row -> "the polling transport cannot even measure it end-to-end, so TransportStats.latencyMs is null and the status bar shows an explicit placeholder". The placeholder path at presentation-feature-mirror/.../core/stats/StatsFormatter.kt:38 (`stats.latencyMs?.let { "latency $it ms" } ?: latencyPlaceholder`) is therefore unreachable while streaming; only StatsFormatterTest exercises it. |
| AF-2 | error | ac | ac | AC-1 | T-5 | T-5 | AC-1 asks for a status dot reflecting DETECTED tool versions for BOTH platforms. The iOS dot is a hardcoded not-installed constant, never a probe. This is a gap, not an acceptable deferral, because the probe machinery already exists and is already unit-tested: ToolKind.Idb, probeCandidates' non-SDK branch, ToolResolver.readVersion's Idb case, installCommand and envHint are all shipped. Only a datasource member and one repository call are missing. | iOS tool detection is a hardcoded constant although ToolResolver already supports ToolKind.Idb. | data-repository-impl/.../source/DeviceRepositoryImpl.kt:60 `return listOf(ToolProbeMapper.toModel.map(adb), iosNotProbed)` with iosNotProbed defined at lines 201-205 (isInstalled = false, hardcoded). Capability that already exists and is tested: data-device-api/.../model/ToolKind.kt:21 `Idb("idb", null)`; data-device-impl/.../core/tool/ToolResolver.kt:91/110/116 (Idb version/install/env branches); data-device-api/src/commonTest/.../ToolProbeCandidatesTest.kt:93 `a tool that does not ship in the sdk skips the sdk roots`. The code comment at presentation-feature-mirror/.../source/mirror/MirrorContent.kt:212 ("iOS is probed so the user can see it is installed") is factually false today. |
| AF-3 | error | ac | ac | NFR-8 | T-9 | T-9 | A11Y-4 ('Escape closes the picker only when a device is already selected') is not implemented in the UI at all. `Key.Escape` does not appear anywhere in the repository. MirrorIntent.ClosePicker has a reducer and two tests but no production dispatcher, and DevicePickerPanel ships no close affordance either — so re-opening the picker from the toolbar DeviceChip (AC-43) is a one-way door: the only way out is Enter, which starts a session. | No Escape handler and no close affordance for the device picker; ClosePicker intent is test-only. | `grep -rn "Key.Escape" --include=*.kt` over the whole repo (excluding build/) returns zero hits. presentation-core-ui/.../organism/picker/DevicePickerPanel.kt:190-199 `onPreviewKeyEvent` handles only DirectionUp / DirectionDown / Enter / NumPadEnter. MirrorIntent.ClosePicker is declared at presentation-feature-mirror/.../MirrorContract.kt:206... (ClosePicker at MirrorContract.kt:169), handled at MirrorViewModel.kt:98, and dispatched only from MirrorViewModelSessionTest.kt:181 and :193. |
| AF-4 | warn | ac | ac | NFR-8 | T-14 | T-14 | A11Y-3's second half ('the keyboard hint reflects the highlighted row's actual action') is unimplemented: the hint is a constant string. Enter on a cold emulator Boots rather than Starts — that part works and is tested — but the footer still reads '↑↓ select · ↵ start'. | Static keyboard hint does not change for a Boot row. | presentation-feature-mirror/.../MirrorContent.kt:190 `keyHint = stringResource(Res.string.mirror_hint_keys)` is passed unconditionally; DevicePickerPanel.kt default is the same fixed string. No branch on MirrorRowKind anywhere in the keyHint path. |
| AF-5 | warn | ac | ac | AC-25 | T-12 | T-12 | HARDWARE GATE — ADJUDICATED: let F-2 close with the performance budget unverified, PROVIDED AF-1 is fixed first. The executor behaved correctly: no number was invented, CaptureFloorMs stays at an honest 100 ms, and a 5-step re-measurement harness is in the KDoc. NFR-1 was already waived at Stage 1 and re-flagged for pre-ship review, so the measurement is not a release gate for F-2 itself. But the waiver was granted on the premise that the UI stays honest about what it cannot measure — which AF-1 currently breaks. Fix AF-1 and the waiver stands on its own terms; leave AF-1 and F-2 ships a number that quietly contradicts it. | Capture rate / glass-to-glass latency never measured; no authorised device and no bootable AVD on the build host. | data-device-api/.../tuning/MirrorTuning.kt:22-41 documents the value as 'STILL UNMEASURED' with the device-side reason (handset unauthorized, only AVD an arm64 android-tv image that stayed offline) and a 5-step harness. execution/T-12/report.json outcome = 'partial'. No measured fps/latency figure exists anywhere in the tree. |
| AF-6 | warn | diff | i18n | AC-4 | T-9 | T-9 | The zero-AVD empty-state label is hardcoded English and bypasses the localisation key that was created for it. NoSignalStage calls EmulatorSelect without emptyLabel, so the composable's hardcoded default renders; mirror_emulator_none is defined and referenced by nothing. plan.errorStates explicitly specifies 'EmulatorSelect renders disabled with an explanatory label'. | mirror_emulator_none defined but unused; hardcoded 'No AVDs installed' ships instead. | presentation-core-ui/.../organism/stage/NoSignalStage.kt:123 `EmulatorSelect(avds = avds, selected = selectedAvd, onSelect = onSelectAvd)` — no emptyLabel. Default at presentation-core-ui/.../molecule/select/EmulatorSelect.kt:69 `emptyLabel: String = "No AVDs installed"`. Key at presentation-core-localisation/src/commonMain/composeResources/values/strings.xml:95. NoSignalStage exposes no emptyLabel parameter, so MirrorContent cannot pass one either. |
| AF-7 | warn | diff | behaviour | AC-43 | T-11 | T-11 | Re-opening the picker during a live session shows a FROZEN device list and keeps capturing behind the panel. Discovery is gated off whenever a session is active, and OpenPicker does not clear the session gate — so a user who plugs in a second phone and clicks the DeviceChip to switch never sees it. AC-43's letter ('clicking the chip reopens the same panel') is met; the resulting behaviour is a trap. | Discovery stays paused while the picker is reopened mid-session; row list is stale and the capture loop keeps running. | presentation-feature-mirror/.../MirrorViewModel.kt:118-120 `sessionDeviceId.flatMapLatest { device -> if (device == null) observeDiscovery() else emptyFlow() }`; MirrorIntent.OpenPicker -> setPickerOpen(true) at MirrorViewModel.kt:97/166-168 touches only isPickerOpen, never sessionDeviceId. MirrorStage.Picker wins over MirrorStage.Mirror in MirrorContract.kt:141-146, so the panel covers a still-running capture loop. |
| AF-8 | warn | task | test-coverage | T-4 | T-4 | T-4 | ADJUDICATED: the gap is real and cheap to close, so close it rather than waive it. ProcessRunner's timeout, destroyForcibly-in-finally, orphan registry and concurrent dual drain are the highest-risk code in the feature and the only new production class with ZERO automated coverage. None of it needs a phone: `/bin/sh -c 'sleep 5'` with timeoutMs = 100 asserts the timeout path and that the child is dead afterwards; `/bin/sh -c 'printf ... >&2; exit 3'` asserts the non-zero-exit and stderr-tail path; a large stdout writer asserts the no-deadlock drain. data-device-impl already has a desktopTest source set. | ProcessRunner (the repository's only ProcessBuilder) has no test; the rest of the module does. | data-device-impl/src/desktopTest/ contains exactly one file, transport/ScreencapMirrorTransportTest.kt (10 tests, fake executor). No test names ProcessRunner. The class is data-device-impl/src/desktopMain/.../core/process/ProcessRunner.kt (110 lines, the only ProcessBuilder in the repo). |
| AF-9 | warn | diff | security | NFR-5 | T-2 | T-2 | The plan's security prose overstates mechanism (2). DeviceCommand is NOT free-text-free: GetProp carries an unvalidated String that lands in `adb shell getprop <name>`, and adb re-joins shell arguments into a device-side sh command line — so a hostile name would be device-side injection, not an inert argv element. NFR-5's real-world guarantee survives only because GetProp is constructed nowhere in production code, which also makes it dead API surface. Either validate the property name (same treatment as DeviceId/AvdName) or delete the member. | DeviceCommand.GetProp(name: String) is free text, contradicting the 'no free text' claim; also unused in production. | data-device-api/.../model/DeviceCommand.kt:25 `public data class GetProp(public val name: String) : DeviceCommand`. Argv at data-device-api/.../parse/DeviceArgv.kt:63 `is DeviceCommand.GetProp -> listOf("shell", "getprop", name)`. `grep -rn "GetProp("` over the repo returns only the declaration and DeviceArgvTest.kt:27 and :73 — zero production construction sites. plan.security.dataHandling[0] mechanism (2) claims 'no vararg, no free text'. |
| AF-10 | info | task | process | T-9 | T-9 | T-9 | Two of the sixteen tasks have no execution report on disk, although state.json marks both done and T-9 additionally needsHumanReview. The CODE landed — every T-9 and T-10 artefact is present and verified — so this is an audit-trail gap, not a delivery gap. Worth noting because the T-9 design conflict being put to the human gate has no on-disk report backing it. | execution/ holds 14 report.json files, not 16: T-9 and T-10 are missing. | `find .pipeline/sdd/docs/android-device-discovery-mirroring/execution -type f` lists T-1,T-2,T-3,T-4,T-5,T-6,T-7,T-8,T-11,T-12,T-13,T-14,T-15,T-16 only. Deliverables confirmed present anyway: RadioMark.kt, PlatformToolCard.kt, MirrorFrameView.kt, EmulatorSelect.kt, DevicePickerPanel.kt, NoSignalStage.kt, SourceListRow `enabled` param (SourceListRow.kt:68) and SourceListHeader (SourceListRow.kt:125); 27 mirror_* keys in strings.xml. |
| AF-11 | info | task | task | T-7 | T-7 | T-7 | ADJUDICATED: ACCEPT the vacuous satisfaction of T7-TAC3; require no change. The criterion's intent is that a reader can find out why a call is unbounded. There are genuinely zero withTimeoutOrNull sites in the domain layer because every child process is bounded inside ProcessRunner, and each use case that could plausibly want a deadline carries a comment saying why it has none. Adding a second redundant bound would make the failure mode WORSE — the KDoc explains that a domain-level withTimeoutOrNull can only express itself as null, which the UI cannot distinguish from a missing tool. | T7-TAC3 satisfied vacuously; the reasons are documented where they belong. | `grep -rn withTimeout --include=*.kt` outside build/ returns exactly 4 hits: ProcessRunner.kt:10/61/65 and the explanatory KDoc at DetectToolsUseCaseImpl.kt:12-15. See also BootEmulatorUseCaseImpl.kt:12-14 and StartMirroringUseCaseImpl.kt:11-12. Zero non-OrNull withTimeout anywhere — confirmed independently. |
| AF-12 | info | task | task | T-14 | T-14 | T-14 | ADJUDICATED: the headless substitutes are sufficient for AC-43's logic and for every state-machine AC, but NOT for the visual ACs. What the Koin probe and the discovery probe cannot show is (a) that the composition renders at all under a real Skiko window and (b) pixel fidelity to comps 01/02/03. Hold AC-1, AC-4, AC-25, AC-43, AC-45 and SC-1/SC-2 at 'requires human demo' rather than declaring them satisfied. The underlying logic is covered by 273 real, passing tests. | No window observed; headless Koin + discovery probes substituted. | execution/T-14/report.json needsHumanReview.required = true, 'no window could be observed in this environment'. I independently confirmed the integration compiles and links: :desktop-application:compileKotlin BUILD SUCCESSFUL on the merged tree this run. RectifyShellContent IS harness-renderable as claimed — verified by reading RectifyWindow.kt:201-270: no koinInject, no scope.launch, no java.awt in its body; all three mirror parameters are defaulted. |
| AF-13 | info | diff | design-fidelity | AC-4 | T-9 | T-9 | ADJUDICATED: ship both deltas, do not re-cut them in F-2. The 180x380dp outline was MANDATED by T-9's own task text ('ported verbatim so the shipped empty stage and the preview that defined it cannot drift apart'), so the executor obeyed the backlog over the comp; changing it now would re-introduce exactly the drift the task was written to prevent. The CTA ceiling claim is accurate — PageCta at 32dp is the largest of six AppButtonSize tiers, so 39dp would mean a seventh tier, which is a design-system decision, not a feature decision. Put both to the human as comp-vs-token reconciliation work for a design pass. | NoSignalStage outline 180x380dp vs comp 02 ~274x578dp; picker CTA 32dp vs comp ~39dp. | presentation-core-ui/.../organism/stage/NoSignalStage.kt:42-43 `OutlineWidth = 180.dp` / `OutlineHeight = 380.dp` with the porting rationale at lines 38-41. presentation-core-ui/.../atom/button/AppButton.kt:128-135 enumerates six tiers; :93 gives PageCta height = 32.dp, the largest. DevicePickerPanel.kt:255 uses AppButtonSize.PageCta. |
| AF-14 | info | diff | dead-code | T-10 | T-10 | T-10 | Two of the 27 mirror_* keys are referenced by nothing. mirror_emulator_none is the AF-6 bug; mirror_status_ready is simply unused — the status indicator label branches cover Streaming/ToolMissing/Detecting/waiting and never 'ready'. | mirror_emulator_none and mirror_status_ready defined and never read. | `comm -23` of the 27 declared `name="mirror_*"` keys against every mirror_* identifier referenced under presentation-feature-mirror/src, compose-application/src and presentation-core-ui/src yields exactly: mirror_emulator_none, mirror_status_ready. strings.xml:89 and :95. Label branches at MirrorContent.kt:161-166. |
| AF-15 | info | diff | dead-code | T-11 | T-11 | T-11 | MirrorIntent.StopMirroring has a reducer and a test but no production dispatcher — the same shape as ClosePicker (AF-3). There is no UI affordance to stop a running mirror session; the only exits are switching platform or losing the device. Either wire a stop control or delete the intent. | StopMirroring intent is test-only. | Declared presentation-feature-mirror/.../MirrorContract.kt:206, handled MirrorViewModel.kt:105, dispatched only from MirrorViewModelSessionTest.kt:167. No other hit in the repo. |
| AF-16 | info | diff | inconsistent-pattern | T-13 | T-13 | T-13 | The new test suites disagree with each other on naming. data-device-api and data-repository-impl use backtick-quoted sentence names (matching the pre-existing shell/palette/preference tests); presentation-feature-mirror and data-device-impl use camelCase identifiers. Both run on iOS, so this was not forced by Kotlin/Native. Cosmetic, but it is the only stylistic split in 106 files. | Mixed backtick vs camelCase test naming across the four new test suites. | Backticks: data-device-api/src/commonTest/.../DeviceIdTest.kt:14 ``fun `of accepts a physical serial and an emulator serial`()``; data-repository-impl/src/commonTest/.../DeviceDiscoveryPollingTest.kt:41. camelCase: presentation-feature-mirror/src/commonTest/.../MirrorViewModelSessionTest.kt:72 `fun arrowAndEnterSelectARowAndStartMirroringIt()`; data-device-impl/src/desktopTest/.../ScreencapMirrorTransportTest.kt:70. |
| AF-17 | info | registry | registry | registry |  |  | No registry exists at .pipeline/sdd/docs/registry/ — declared a known non-defect by the orchestrator and confirmed absent. Nothing to reconcile: no phantom entries are possible and no task report claims a registry reuse (every report's reusedFromRegistry is empty). | Registry directory absent; consistency check vacuous. | `ls .pipeline/sdd/docs/registry/` -> No such file or directory. execution/T-16/report.json implementation.reusedFromRegistry = []. |
| AF-18 | info | diff | docs | T-16 | T-16 | T-16 | ADJUDICATED on root CLAUDE.md: NOT a defect, no change required. That section enumerates only application/entry-point modules plus build-logic — it has never listed a single data-, domain- or presentation- library, so omitting the three new ones is consistent with its scope, and T-16's reasoning is correct. The genuinely stale doc is a different one: .claude/rules/testing.md still opens 'No test exists anywhere in this repo today (zero files under any *Test* source set)', which is now plainly false — 273 tests across seven modules. It predates F-2 (pre-existing shell/palette/preference tests already falsified it) but F-2 made it actively misleading for the next agent, and the three new module CLAUDE.md files it should point at are now the real pattern to extend. | Root CLAUDE.md module list correctly unchanged; .claude/rules/testing.md is now actively wrong. | CLAUDE.md '### Module Structure' lists exactly compose-application, aos-application, desktop-application, build-logic/convention, ios-application — no library modules at all. .claude/rules/testing.md paragraph 1 vs this run's measured 142 desktopTest + 131 iosSimulatorArm64Test executions. |
| AF-19 | info | diff | behaviour | AC-4 | T-5 | T-5 | Once discovery escalates to NoDeviceFound, the AVD list is frozen: emulatorSource.listAvds() is only called inside the Found branch, and the escalated state is self-sustaining until a device attaches. A user who creates a new AVD while the NO SIGNAL stage is up will not see it in the inline picker. The retention fix (which IS correct) keeps the LAST KNOWN list alive, which is the right call; this is the residual edge it cannot cover. | AVD list is not refreshed while the discovery state is escalated. | data-repository-impl/.../DeviceRepositoryImpl.kt:92-107 — listAvds() is reached only via emulators(...) in the DiscoveryModel.Found branch at line 104; the NoDeviceFound branch (lines 94-99) carries no emulator data, and emptyPolls only resets when a usable device appears (line 90). |
| AF-20 | info | diff | observability | FR-4 | T-14 | T-14 | MirrorSideEffect.BootFailed is posted but silently discarded in production: RectifyWindow calls MirrorHost without onSideEffect, and the default is a no-op lambda. F-3 owns the error card, which is correct scoping, but in F-2 as shipped a failed emulator boot produces no UI feedback and not even a log line. A one-line `onSideEffect = { log.w { ... } }` would close it until F-3 lands. | Boot failures are dropped with no UI and no log. | presentation-feature-mirror/.../MirrorHost.kt:30 `onSideEffect: (MirrorSideEffect) -> Unit = {}`; compose-application/.../RectifyWindow.kt:152 `MirrorHost { mirror, frame, onMirrorIntent -> ... }` passes only the content trailing lambda. Side effect posted at MirrorViewModel.kt:227. |

### gaps

#### unsatisfiedAC

- AC-1
- AC-45

#### unsatisfiedSC

- SC-1
- SC-2

#### unsatisfiedRequirements

- FR-1
- FR-2
- NFR-8

#### incompleteTasks

- T-5
- T-12

### unresolvedFlags

| task | flag | kind | target | reason | detail |
| --- | --- | --- | --- | --- | --- |
| T-12 | needsHumanReview | needsHumanReview | T-12 | HARDWARE GATE. Capture rate and glass-to-glass latency never measured; CaptureFloorMs stays at an unmeasured 100 ms. AC-25 and AC-45 rest on it. Audit position (AF-5): acceptable to close F-2 under the Stage-1 NFR-1 waiver ONLY once AF-1 is fixed, because the waiver was granted on the UI staying honest about what it cannot measure. | state.json T-12 outcome=partial, needsHumanReview=true. Requires a human with an authorised Android device to run the 5-step harness in MirrorTuning's KDoc. |
| T-14 | needsHumanReview | needsHumanReview | T-14 | No window was ever observed; sandbox denies screen capture. Audit position (AF-12): headless Koin + discovery probes cover the logic, not the pixels. A single human run of ':desktop-application:run' is still required to close AC-1, AC-4, AC-25, AC-43, AC-45, SC-1, SC-2. | Integration compile independently re-verified green this run (:desktop-application:compileKotlin). RectifyShellContent confirmed Koin/AWT/coroutine-free by direct reading of RectifyWindow.kt:201-270. |
| T-5 | needsHumanReview | needsHumanReview | T-5 | Hardcoded iOS 'not installed'. Audit position (AF-2): this is a GAP, not a satisfaction of AC-1. The probe machinery (ToolKind.Idb, probeCandidates non-SDK branch, ToolResolver Idb case) is already shipped and unit-tested; only a datasource member and a repository call are missing. The second half of the flag (NoDeviceFound carries no AVDs) was correctly closed — retention is implemented and tested. | DeviceRepositoryImpl.kt:60 and :201-205. |
| T-7 | needsHumanReview | needsHumanReview | T-7 | T7-TAC3 satisfied vacuously. Audit position (AF-11): ACCEPT, no change. Zero domain-layer withTimeoutOrNull sites is correct because ProcessRunner bounds every child, and each use case documents why it adds no second bound. | Resolvable now; does not need the human gate. |
| T-4 | needsHumanReview | needsHumanReview | T-4 | ProcessRunner has no automated test. Audit position (AF-8): close the gap rather than waive it — the timeout, destroyForcibly and dual-drain paths are all testable with /bin/sh and no device, and data-device-impl already has a desktopTest source set. | The capture-loop half of this flag WAS closed: 10 tests in ScreencapMirrorTransportTest via a fake executor, confirmed running and green this audit. |
| T-9 | needsHumanReview | needsHumanReview | T-9 | Design conflict: NoSignalStage outline 180x380dp vs comp 02 ~274x578dp, and picker CTA 32dp vs comp ~39dp. Audit position (AF-13): ship both; the outline was mandated verbatim by the task and the CTA is at the largest existing AppButtonSize tier. Route to a design pass, not to F-2. Separately, T-9 has NO execution report on disk (AF-10). | Deliverables all verified present despite the missing report. |

### regressionRun

#### command

per-module: ./gradlew :<module>:desktopTest --rerun (7 modules) + ./gradlew :<module>:iosSimulatorArm64Test --rerun (6 modules) + ./gradlew :desktop-application:compileKotlin — split per module because the Kotlin daemon is capped at 3GB on this host; the repo has no detekt/ktlint, so 'check' is compile + test and this is the entire suite

#### passed

true

#### output

```
=== HOST: macOS 26.1 | Thu Oct 15 05:07:00 EEST 2026 | branch sdd/android-device-discovery-mirroring @ 6f4fa63
=== No detekt/ktlint in this repo; 'check' == compile + test. Suite run PER MODULE (Kotlin daemon capped at 3GB).

--- DESKTOP (JVM) :desktopTest --rerun ---
########## :data-device-api:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0
########## :data-device-impl:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0
########## :data-repository-impl:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0
########## :presentation-feature-mirror:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0
########## :presentation-core-ui:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0
########## :presentation-core-styling:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0
########## :data-preference-impl:desktopTest --rerun ##########
BUILD SUCCESSFUL in 1s
EXIT=0

--- IOS SIMULATOR ARM64 :iosSimulatorArm64Test --rerun ---
########## :data-device-api:iosSimulatorArm64Test ##########
BUILD SUCCESSFUL in 5s
########## :data-repository-impl:iosSimulatorArm64Test ##########
BUILD SUCCESSFUL in 4s
########## :presentation-feature-mirror:iosSimulatorArm64Test ##########
BUILD SUCCESSFUL in 8s
########## :presentation-core-ui:iosSimulatorArm64Test ##########
BUILD SUCCESSFUL in 4s
########## :presentation-core-styling:iosSimulatorArm64Test ##########
BUILD SUCCESSFUL in 4s
########## :data-preference-impl:iosSimulatorArm64Test ##########
BUILD SUCCESSFUL in 3s

--- JUnit XML TOTALS (parsed from build/test-results, all files rewritten during this run, timestamps 04:59-05:01) ---
desktopTest          : tests=142 skipped=0 failures=0 errors=0
iosSimulatorArm64Test: tests=131 skipped=0 failures=0 errors=0
TOTAL                : 273 test executions, 0 failures, 0 errors, 0 skipped

--- EXTRA INTEGRATION COMPILE (merged tree) ---
:desktop-application:compileKotlin  BUILD SUCCESSFUL (159 actionable tasks, 37 executed)

NOTE: a first pass without --rerun reported every task UP-TO-DATE and was discarded as non-evidence; the numbers above come from forced re-execution.
```

#### newFailures


#### testsExecuted

273

#### modulesCovered

7

### aggregateDiffFindings

| id | category | location | issue |
| --- | --- | --- | --- |
| AD-1 | security | data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceCommand.kt:25 | DeviceCommand.GetProp carries unvalidated free text into `adb shell getprop <name>`, which adb re-joins into a device-side sh command line. Contradicts the plan's 'no free text' claim; harmless today only because nothing in production constructs it. See AF-9. |
| AD-2 | dead-code | presentation-feature-mirror/.../MirrorContract.kt:169,206 and presentation-core-localisation/.../strings.xml:89,95 | Four production-unreachable artefacts: MirrorIntent.ClosePicker, MirrorIntent.StopMirroring, mirror_emulator_none, mirror_status_ready. Each has a reducer or a definition and a test, and no dispatcher or reader. Two of the four (ClosePicker, mirror_emulator_none) are symptoms of real gaps (AF-3, AF-6), not merely dead weight. |
| AD-3 | inconsistent-pattern | presentation-feature-mirror/src/commonTest, data-device-impl/src/desktopTest vs data-device-api/src/commonTest, data-repository-impl/src/commonTest | Two test-naming conventions in one feature: camelCase in the B4/B2 suites, backtick sentences in the B1 suites and in every pre-existing test. See AF-16. |
| AD-4 | performance | data-device-impl/.../transport/ScreencapMirrorTransport.kt:52-102 | No performance regression found, and the hot-path design is sound: single-flight by construction (the loop awaits the capture before issuing the next), overrun slots counted rather than smoothed (:79), PNG size read from the IHDR header per frame so rotation follows (:112), frame bytes passed by reference through MirrorFrameMapper rather than copied, and pixels bypass Orbit entirely via MirrorViewModel.frame so a 10 fps stream cannot cause 10 state copies a second. The only perf-relevant defect is reporting (AF-1), not throughput. |
| AD-5 | security | whole repository | NFR-5 VERIFIED ADVERSARIALLY AND HOLDS. I looked for a raw-argv path, a public constructor and a String overload and found none. All five ProcessRunner entry points go through buildDeviceArgv/buildHostArgv (ToolResolver.kt:94, AdbHostSourceImpl.kt:28, EmulatorSourceImpl.kt:29 and :45, DeviceCommandExecutorImpl.kt:27); DeviceId and AvdName both have private constructors with validated factories and negative tests; DeviceCommandExecutor has one method with a non-defaultable DeviceId; ProcessBuilder appears in exactly one internal desktopMain file; `connect`, `-H` and `-P` appear in no command member. The one soft spot is that the DOMAIN seam is stringly typed (DeviceRepository.mirror(deviceId: String), bootEmulator(avdName: String)) — but both re-validate through DeviceId.of / AvdName.of at the data boundary (DeviceRepositoryImpl.kt:114 and :120) and reject rather than pass through, so the guarantee is not weakened, only enforced one layer lower than the prose implies. |
| AD-6 | scope | 106-file diff | SCOPE DISCIPLINE CLEAN. Nothing from F-3 (error cards), annotation, measure, payload, history or MCP appears in the diff. presentation-core-ui/.../organism/error/ErrorCard.kt is untouched and pre-existing. gradle/libs.versions.toml diff is 0 lines (independently confirmed). No TODO/FIXME/HACK, no println, no literal Color(0x or TextStyle( in added lines; two @Suppress("LongParameterList") on genuinely slot-heavy composables. Largest new file is 421 lines. |
| AD-7 | correctness | presentation-feature-mirror/.../core/discovery/DiscoveryProjection.kt:35-42,69-74 | THE RETAINED-EMULATOR-LIST FIX IS CORRECT AND TESTED. B2's warning was accurate (NoDeviceFound genuinely carries no emulator payload — DeviceRepositoryImpl.kt:94-99) and B4's retention is the right shape: only Emulator-group rows survive, each demoted to MirrorRowKind.Boot with its serial dropped and its id rewritten to the AVD name, so a stale running row cannot be committed as a live session. Covered by MirrorViewModelDiscoveryTest.theLastKnownEmulatorsSurviveAnEscalatedPoll. Residual edge in AF-19. |

### registryConsistency

#### consistent

true

#### phantomEntries


#### missingEntries


#### issues


#### note

No registry exists at .pipeline/sdd/docs/registry/ (declared a known non-defect). Nothing to reconcile; no report claims a registry reuse.

### traceability

| ref | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-1 | demo | false | Android half real (ToolResolver probe -> PlatformToolCard). iOS half is a hardcoded not-installed constant, DeviceRepositoryImpl.kt:201-205, so the dot reflects no detection (AF-2). Also demo-pending: no window observed (AF-12). |
| AC-2 | test | true | Physical/Emulator grouping via MirrorRowGroup + DevicePickerPanel.SourceList; arrow/Enter via PickerNavigation + DevicePickerPanel.kt:190-199. Green this run: AdbDevicesParserTest, DeviceResourceMapperTest, PickerNavigationTest, MirrorViewModelDiscoveryTest.devicesFoundPopulateThePickerAndHighlightTheFirstSelectableRow, MirrorViewModelSessionTest.arrowAndEnterSelectARowAndStartMirroringIt. |
| AC-3 | test | true | Commit -> MirrorSession.Streaming -> sessionDeviceId -> StartMirroringUseCase -> ScreencapMirrorTransport.frames (MirrorViewModel.kt:185-199, :124-139). MirrorViewModelSessionTest + ScreencapMirrorTransportTest (10 tests) green this run. |
| AC-4 | demo | false | Logic present and tested (NoSignalStage with guidance, EmulatorSelect + Boot; DeviceDiscoveryPollingTest). Not satisfiable as a demo yet: no window observed (AF-12), and the zero-AVD label is hardcoded English instead of mirror_emulator_none (AF-6). |
| AC-25 | demo | false | Status line implemented (MirrorContent.MirrorStatusLine + StatsFormatter, throttled to 1/s at MirrorViewModel.kt:141-152). Blocked on the unmeasured capture rate (AF-5) and on the latency field (AF-1), and demo-pending (AF-12). |
| AC-41 | test | true | DiscoveryModel.ToolMissing emitted as a success value with probedPaths/installCommand/envHint (DeviceRepositoryImpl.kt:73-84); UI renders only a dot + 'adb not found', no Locate/Retry/Copy. DeviceDiscoveryPollingTest.`a missing adb arrives as a ToolMissing value` and `a missing adb never escalates to NoDeviceFound` + MirrorViewModelDiscoveryTest.aMissingToolIsASuccessValueThatEmptiesTheList — green this run. |
| AC-42 | test | true | NoDeviceFound(toolVersion, lastPollEpochMs, attempts, attachedCount) at the EmptyPollEscalation=3 boundary (DeviceRepositoryImpl.kt:90-99); the stage stays passive. DeviceDiscoveryPollingTest.`an empty poll stays passive until the escalation threshold` and `the escalation poll reports attempts and the last poll time` — green. |
| AC-43 | demo | false | Letter met: opaque Theme.color.bg fill of the stage slot (DevicePickerPanel.kt:186-188), one panel implementation, chip reopens it (MirrorViewModel.kt:97). Held open because it is demo-method and no window was observed (AF-12), and because reopening is a one-way door with no Escape/close (AF-3) onto a frozen list (AF-7). |
| AC-44 | test | true | Verified adversarially — see AD-5. DeviceArgvTest.`buildDeviceArgv targets the device for every command variant` / `names the selector exactly once` and DeviceIdTest (6 tests) green; exactly one ProcessBuilder file; all five ProcessRunner call sites route through the two argv builders. |
| AC-45 | demo | false | fps is genuinely the achieved rate (ScreencapMirrorTransport.kt:83-90, asserted by theReportedRateIsTheAchievedOneOverTheStatsWindow). Latency is NOT the specified explicit placeholder: line 95 reports the capture round-trip unconditionally, so the placeholder branch never fires in production (AF-1). Plus the unmeasured budget (AF-5). |
| FR-1 | test | false | adb detection complete and tested (ToolProbeCandidatesTest, 7 tests). idb detection is not wired despite ToolKind.Idb and the ToolResolver Idb branches existing (AF-2). |
| FR-2 | inspection | false | Platform switch, iOS-disabled card and the IosDeferred stage notice all ship (MirrorContent.kt:93-99, :214), and switching to iOS stops the session (MirrorViewModelSessionTest.switchingPlatformToIosStopsTheSession, green). The plan's stated inspection — 'the iOS card shows the detected idb probe' — FAILS: the card shows a hardcoded constant (AF-2). |
| FR-3 | test | true | DevicePickerPanel + SourceListRow(enabled) + SourceListHeader + PickerNavigation + EmulatorSelect all present; AdbDevicesParserTest, AvdListParserTest, PickerNavigationTest, MirrorViewModelDiscoveryTest green. |
| FR-4 | test | true | Start-mirroring CTA -> Commit -> Streaming -> frames, with Enter on a cold AVD booting instead (MirrorViewModelSessionTest.enterOnAColdEmulatorBootsItInsteadOfStartingASession). The shipped mirror_waiting_subtitle matches the no-auto-start behaviour, so survey Q1's consistency requirement holds. |
| FR-5 | test | true | MirrorTuning.DiscoveryPollMs = 2000 with the delay between poll completions (DeviceRepositoryImpl.kt:109), so a slow adb cannot stack polls. DeviceDiscoveryPollingTest.`poll emissions land two seconds apart` green under virtual time. |
| FR-22 | test | false | Tool version, resolution and fps are correct and tested (StatsFormatterTest, 4 tests green). The latency field is wrong per AF-1. |
| FR-24 | test | true | Screenshot-polling transport with the foldable trim ported (trimToPngSignature; PngTrimTest 5 tests green, including the prepended-warning-line case). The shortfall is documented in MirrorTuning's KDoc — though as an unmeasured shortfall (AF-5), not the measured one FR-24's own test row asks for. |
| FR-25 | inspection | true | INSPECTED: MirrorTransport (data-device-api/.../datasource/MirrorTransport.kt) has exactly two members, frames(DeviceId) and stats. No presentation or domain file imports ScreencapMirrorTransport — grep returns only its own file and DeviceSourcesProvider.desktop.kt:30, which is the single DI line a swap would touch. |
| NFR-1 | test | false | WAIVED at Stage 1 and still unverified: no measurement exists (AF-5). The waiver's own condition — 'the real achieved number must be measured and recorded' — is unmet, and AC-45's compensating honesty is broken by AF-1. |
| NFR-5 | test | true | Attacked and held — see AD-5 for the full enumeration. DeviceArgvTest + DeviceIdTest green; one ProcessBuilder; no connect/-H/-P representable. |
| NFR-8 | test | false | A11Y-1/2/3(first half)/6 satisfied and tested (nextSelectableIndex clamps and skips, SourceListRow drops the clickable and adds semantics.disabled). A11Y-4 (Escape) is entirely unimplemented — zero Key.Escape in the repo (AF-3). A11Y-3's second half (hint reflects the row's action) unimplemented (AF-4). A11Y-5's focus-return-on-close is unimplemented, which follows from there being no close path. |
| NFR-10 | inspection | true | INSPECTED and re-run: provideDeviceSources expect at data-device-impl/src/commonMain with android, ios and desktop actuals; UnsupportedDeviceSources registers all four contracts for the mobile targets and fails loudly rather than silently. :data-device-api, :data-repository-impl, :presentation-feature-mirror and :presentation-core-ui iosSimulatorArm64Test all built, ran and passed this audit (131 tests). No macOS/Linux native target added. |
| SC-1 | test | false | The state path is green end to end (MirrorViewModelDiscoveryTest + MirrorViewModelSessionTest, 12 tests). The scenario's other half — step 15's manual walkthrough — never ran (AF-12), and its 'detect adb/idb and show status dots' step is incomplete regardless (AF-2). |
| SC-2 | test | false | Poll cadence, escalation and the NO SIGNAL state are tested (DeviceDiscoveryPollingTest, 10 tests green). The unplug -> NO SIGNAL -> Boot walkthrough the scenario also requires was never performed (AF-12). |
| SC-6 | test | true | Fully covered without hardware: ToolProbeCandidatesTest (probe order incl. empty env and packaged-.app PATH), DeviceDiscoveryPollingTest ToolMissing cases, MirrorViewModelDiscoveryTest.aMissingToolIsASuccessValueThatEmptiesTheList. All green this run. |
| SC-7 | test | true | DeviceDiscoveryPollingTest covers the escalation boundary, attempts and lastPollEpochMs; MirrorViewModelDiscoveryTest.sustainedEmptyPollsSurfaceNoDeviceFoundWhileTheStageStaysPassive confirms F-2's stage stays passive. Green. |

### tasksChecked

| id | status | done | evidence |
| --- | --- | --- | --- |
| T-1 | done | true | 8 device models under domain-core/.../model/device/ + Failure.Technical.Process added to Failure.kt — all present in the diff. |
| T-2 | done | true | data-device-api module exists with 4 datasource contracts, 10 models, 5 parsers, ToolProbeCandidates and MirrorTuning. Caveat AF-9 (GetProp free text). |
| T-3 | done | true | 7 test classes, 38 tests, all green on desktop AND iosSimulatorArm64 this run. |
| T-4 | done | true | data-device-impl ships ProcessRunner (the only ProcessBuilder), ToolResolver, 3 datasource impls and the transport. Two defects: AF-1 (latency reporting) and AF-8 (ProcessRunner untested). |
| T-5 | done | false | DeviceRepositoryImpl, 4 mappers and the 2 s polling flow are all real and tested — but the iOS probe is hardcoded (AF-2), so the task's AC-1 contribution is incomplete. |
| T-6 | done | true | DeviceDiscoveryPollingTest (10 virtual-time tests) + DeviceResourceMapperTest + EmulatorResourceMapperTest, green on both targets. |
| T-7 | done | true | DeviceRepository + 5 use case interfaces + 5 impls + Koin bindings all present and wired. T7-TAC3 accepted (AF-11). |
| T-8 | done | true | dataDeviceImplModule and presentationFeatureMirrorModule added to InitKoin.kt in data->domain->presentation order; both module dependencies added to compose-application/build.gradle.kts. Graph re-verified by :desktop-application:compileKotlin green. |
| T-9 | done | true | RadioMark extracted, SourceListRow.enabled added with semantics.disabled, and all 5 new components present. No report.json on disk (AF-10). Design deltas adjudicated in AF-13; the Escape gap (AF-3) and the empty-label gap (AF-6) land here. |
| T-10 | done | true | Exactly 27 mirror_* keys in strings.xml; 25 referenced, 2 dead (AF-14). No report.json on disk (AF-10). |
| T-11 | done | true | MirrorViewModel, MirrorContract, MirrorHost, MirrorContent, DiscoveryProjection, PickerNavigation, StatsFormatter, DI module — all present; frames bypass Orbit as designed. Caveats AF-7, AF-15. |
| T-12 | done | false | outcome=partial in both state.json and the report. No measurement was taken and none was fabricated — the honest outcome, but the task's deliverable does not exist (AF-5). |
| T-13 | done | true | 4 test classes, 24 tests, green on both targets, including the retention test that closes B2's warning (AD-7). |
| T-14 | done | true | Every shell slot is live: platform switch, DeviceChip, stage, status bar. RectifyShellContent verified Koin/AWT/coroutine-free by direct reading. Visual confirmation still owed (AF-12). |
| T-15 | done | true | Re-verified independently rather than trusted: 1 ProcessBuilder file, 0 raw withTimeout, 0 catalog lines, 0 Color(0x / TextStyle( added, iOS test-compiles and :desktop-application:compileKotlin green. The one sweep item that could not be done — the manual demo walkthrough — is correctly carried as T-14's flag. |
| T-16 | done | true | data-device-api/CLAUDE.md, data-device-impl/CLAUDE.md, presentation-feature-mirror/CLAUDE.md all exist and presentation-core-ui/CLAUDE.md was updated. Its escalation about root CLAUDE.md is ruled NOT a defect (AF-18); .claude/rules/testing.md is the doc that actually needs the edit. |

### acceptanceCriteriaChecked

| id | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-1 | demo | false | iOS dot hardcoded (AF-2); demo not performed (AF-12). |
| AC-2 | test | true | 5 test classes green. |
| AC-3 | test | true | MirrorViewModelSessionTest + ScreencapMirrorTransportTest green. |
| AC-4 | demo | false | Demo not performed; hardcoded empty-AVD label (AF-6). |
| AC-25 | demo | false | Unmeasured budget (AF-5) + latency field (AF-1) + demo not performed. |
| AC-41 | test | true | ToolMissing typed state green; no F-3 UI built. |
| AC-42 | test | true | NoDeviceFound with full poll metadata green at the 3-poll boundary. |
| AC-43 | demo | false | Opaque single-panel implementation correct; demo not performed; no Escape/close (AF-3) and stale list on reopen (AF-7). |
| AC-44 | test | true | Attacked adversarially and held (AD-5). |
| AC-45 | demo | false | fps honest; latency contradicts the spec (AF-1). |

### scenariosChecked

| id | satisfied | evidence |
| --- | --- | --- |
| SC-1 | false | State path green; manual walkthrough never ran; iOS detection missing. |
| SC-2 | false | Polling/escalation/NO SIGNAL tested; unplug->Boot walkthrough never ran. |
| SC-6 | true | Fully covered by tests with no hardware. |
| SC-7 | true | Fully covered by tests with no hardware. |

### flags

| kind | target | detail |
| --- | --- | --- |
| needsHumanReview | T-12 | Hardware gate; unmeasured performance budget. Audit recommends closing under the standing NFR-1 waiver once AF-1 is fixed. |
| needsHumanReview | T-14 | No window observed; a single human ':desktop-application:run' still closes 7 demo-method rows. |
| needsHumanReview | T-5 | Hardcoded iOS probe — audit rules this a GAP (AF-2), not a satisfaction. |
| needsHumanReview | T-7 | Vacuous T7-TAC3 — audit ACCEPTS, no change needed (AF-11). |
| needsHumanReview | T-4 | ProcessRunner untested — audit recommends closing the gap, it needs no hardware (AF-8). |
| needsHumanReview | T-9 | Comp geometry deltas — audit recommends shipping both and routing to a design pass (AF-13). |

### aggregateDiffReview

- AD-1 security: DeviceCommand.GetProp is unvalidated free text reaching a device-side shell; contradicts the plan's 'no free text' claim and is unused in production.
- AD-2 dead-code: ClosePicker, StopMirroring, mirror_emulator_none and mirror_status_ready are all production-unreachable; two of them mark real gaps.
- AD-3 inconsistent-pattern: two test-naming conventions across the four new suites.
- AD-4 performance: no regression; the capture hot path is single-flight, allocation-light and keeps pixels out of the Orbit reducer. Only the latency REPORT is wrong.
- AD-5 security: NFR-5 attacked from four angles (raw argv, public constructor, String overload, remote target) and held at every one; the domain seam is stringly typed but re-validated and rejecting.
- AD-6 scope: clean — nothing from F-3, annotation, measure, payload, history or MCP; 0 catalog lines; no TODO/println/hardcoded Color or TextStyle.
- AD-7 correctness: the retained-emulator-list fix is correct, well-shaped (rows demoted to Boot, serial dropped) and covered by a dedicated test.

### verifiedClaims

- 186-test claim: SUPERSEDED by a forced re-run — 273 test executions (142 desktop + 131 iosSimulatorArm64), 0 failures, 0 errors, 0 skipped. A first pass reported UP-TO-DATE and was discarded.
- gradle/libs.versions.toml diff is 0 lines — confirmed.
- ProcessBuilder in exactly one file (data-device-impl/.../core/process/ProcessRunner.kt) — confirmed.
- Zero non-OrNull withTimeout( anywhere — confirmed (4 total withTimeout hits, all OrNull or prose).
- Zero literal Color(0x or TextStyle( in added production lines — confirmed over the whole diff.
- iOS test-compile green on 4 modules — exceeded: the iOS tests were EXECUTED on the simulator, not merely compiled.
- :desktop-application:compileKotlin green on the merged tree — confirmed this run.
- RectifyShellContent body is Koin/AWT/coroutine-free — confirmed by reading RectifyWindow.kt:201-270. DevicePickerPanel's own LaunchedEffect is a FocusRequester, which a render harness drives fine.
- The retained-emulator-list fix is real, correctly shaped and tested — confirmed.

### humanDecisionsRequired

- AF-5 / T-12: accept closing F-2 with an unmeasured performance budget under the standing NFR-1 waiver? Audit recommends YES, conditional on AF-1 being fixed first.
- AF-12 / T-14: one human run of ':desktop-application:run' to close AC-1, AC-4, AC-25, AC-43, AC-45, SC-1 and SC-2 — the only remaining evidence no automated check can produce.
- AF-13 / T-9: comp-02 outline geometry and a possible seventh AppButtonSize tier are design-system calls, not feature calls.
