# ios-platform-support — audit

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | audit |
| status | done |
| producedBy | sdd-build-validation-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-19T06:03:55Z |
| updatedAt | 2026-10-19T06:03:55Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-19T06:03:55Z

## Provenance

### selfHash

ad6982ad7d985b9649e114117ff26cf8730b17fe3c0485b570d97e493c2605bf

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 67ba639e31656af7d408259e4fe0eaaa9f0e2fd7c59bcf001881607a29f6d5ca |

## Data

### iteration

1

### findings

| id | severity | about | ref | issue | evidence | recommendedTarget |
| --- | --- | --- | --- | --- | --- | --- |
| AF-1 | warn | ac | AC-48 | AC-48's declared verificationMethod is `demo` and the demo was NOT performed end-to-end. `inputs/demo-measurements.md` records a single shell invocation of `xcrun simctl io <udid> screenshot --type=png -` (exit=0, 1179x2556 PNG) and explicitly states 'No GUI was driven'. The app was never run against a booted simulator, no live frames rendered, and the `captureRoundTripMs` number that the AC-48 traceability row names as required DEMO evidence (`DEMO (AC-48's declared verificationMethod) -- live frames + measured captureRoundTripMs as a number`) does not exist anywhere in the spec outputs. AC-48 must go to the human gate as UNVERIFIED, not as satisfied-by-proxy. | .pipeline/sdd/docs/ios-platform-support/inputs/demo-measurements.md lines 41-51: '- No GUI was driven. Clicking "Boot simulator" in a rendered error card is still manual-not-executed; what is proven is that the command the button dispatches works.' No captureRoundTripMs figure appears in the file. | T-26 |
| AF-2 | error | diff | IosDeviceCommandExecutor.kt | ZERO test coverage on the one new class that carries all new iOS RUNTIME behaviour. `grep -rln IosDeviceCommandExecutor --include=*.kt` matches only three production files and no test anywhere in the repo. Three behaviours specified by AC-47/SC-26/PLAN-AS-4 are therefore inspection-only: (a) the already-booted swallow -- `boot()`'s `if (!failure.stderrTail.contains(ALREADY_BOOTED)) throw failure` is the literal mechanism AC-47 names ('treated as success on its stderr token, not on its exit code') and nothing asserts it; (b) `screencapViaTempFile()`, the idb `-`-rejected fallback, is unreachable from any test and unexecuted on any host -- it is effectively dead code with a latent defect (`File.createTempFile` pre-creates the file, and whether `idb screenshot <existing path>` overwrites or refuses is unmeasured); (c) `toolFor()`'s simctl-vs-idb classification and its unbounded cache. Note the asymmetry: the Android twin HAS `AdbDeviceCommandExecutorTest` (31 tests in :data-device-impl:desktopTest), so this is a coverage hole specific to the new code, not a repo-wide norm. | data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosDeviceCommandExecutor.kt lines 82-120; `ls data-device-impl/src/desktopTest/kotlin/data/device/impl/datasource/` -> AdbDeviceCommandExecutorTest.kt, AdbHostSourceImplTest.kt, SimctlRealToolchainTest.kt (no IosDeviceCommandExecutorTest). | T-12 |
| AF-3 | error | diff | plan.performanceBudget[1] | PLAN/CODE DIVERGENCE on the `idb list-targets` budget. The plan's performanceBudget row 2 reads: metric '`idb list-targets` per poll', target 'MirrorTuning.HostCommandTimeoutMs = 3000ms (existing)'. The implementation deliberately and correctly moved it to `MirrorTuning.DaemonStartTimeoutMs` (15000 ms) after measuring 5.26/5.27/5.28 s. The CODE is right; the PLAN row was never amended and now states a budget the shipped code violates by 5x. Claim 10 asked whether anything still budgets it at 3 s -- the answer is yes: the plan itself. This needs an explicit plan amendment or a recorded waiver at the human gate, otherwise the next reader reconciles plan-to-code and 'fixes' a real livelock back in. | plan.json#data.performanceBudget[1].target = 'MirrorTuning.HostCommandTimeoutMs = 3000ms (existing). Timeout degrades the physical list to empty; the simctl half is unaffected.' vs data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosHostSourceImpl.kt:119 `timeoutMs = MirrorTuning.DaemonStartTimeoutMs,` | T-16 |
| AF-4 | warn | diff | plan.performanceBudget[0] | The plan's first performance budget gives an explicit instruction that was not carried out: 'iOS frame capture round-trip ... <=100ms ... ASPIRATION, NOT A PREDICTION -- plausibly 200-400ms. Measure once a simulator is booted and record the number.' A simulator WAS booted during T-26 (demo-measurements.md: `xcrun simctl boot ... exit=0, 2.79 s wall`, then `1 booted`) and the capture round-trip number was still NOT recorded -- only exit=0 and the PNG dimensions. The one cheap measurement the plan asked for, with the precondition satisfied, was skipped. | .pipeline/sdd/docs/ios-platform-support/inputs/demo-measurements.md section 'Screenshot to stdout' records `exit=0` and `1179 x 2556 ... 68 641 bytes` but no wall time. | T-26 |
| AF-5 | warn | task | T-14 | The composed `initKoin()` graph is still NOT machine-verified, which is the exact failure class that escaped three waves. `DeviceSourcesModuleTest` resolves `dataDeviceImplModule` IN ISOLATION and `provideUnsupportedDeviceSources()` IN ISOLATION. The Wave 8 crash was `DeviceRepositoryImpl` (in :data-repository-impl) asking for bindings registered in a DIFFERENT module (:data-device-impl). This test would catch the specific sub-case 'the qualified registration is missing from dataDeviceImplModule' -- so the guard is real and claim 5's half about the guard is CONFIRMED. It would NOT catch a consumer/producer qualifier mismatch, because nothing anywhere composes `dataDeviceImplModule + dataRepositoryImplModule`, let alone the full 11-module `initKoin()`. Residual risk is low (both sides import the same `ANDROID_SOURCE`/`IOS_SOURCE` constants from `DeviceSourceQualifiers.kt`, so a typo cannot compile) but the class of bug that cost three waves remains unguarded at its actual boundary. `initKoin()` lives in :compose-application, which per .claude/rules/testing.md deliberately has no test wiring -- so a composed-graph test would have to live in :data-repository-impl, which already depends on :data-device-impl. | data-device-impl/src/desktopTest/kotlin/data/device/impl/di/DeviceSourcesModuleTest.kt:78 `private fun koinFor(sources: Module): Koin = koinApplication { modules(module { single<AdbPathPreferenceSource> { UnsetAdbPath } }, sources) }.koin` -- one module at a time. `grep -rn koinApplication --include=*.kt` across the repo returns only this file and HandoffSourcesProviderTest.kt. | T-14 |
| AF-6 | warn | task | T-1 | Commit 1 is 21 code files, not the 20 that PLAN-AS-2 recounted and AC-49 cites as its evidence condition ('commit 1's word-diff containing only the five token shapes named in PLAN-AS-1'). The 21st file is `data-device-api/src/commonTest/kotlin/data/device/api/IosDiscoveryFixtures.kt`, a NEW 168-line file of captured iOS toolchain stdout (T-1's deliverable, bundled into the same wave-1 commit). So 'zero iOS code in commit 1' is literally false. MITIGATION, verified: the file contains 0 `@Test` annotations (it is pure data), introduces no production code, and the other 19 code files' word-diff IS confined to PLAN-AS-1's five token shapes -- I checked the full production diff of DeviceRepositoryImpl.kt and MirrorViewModel.kt line by line and found nothing but `platform` threading, the two documented `PlatformModel.Android` literals, imports and KDoc. AC-49's substance (no Android behaviour changed) holds; its stated EVIDENCE (20 files, five token shapes only) does not, and the plan said in PLAN-AS-1 that an overstated claim here 'is falsifiable on first inspection'. It was. | `git show --name-only --format='' 49094cd \| wc -l` -> 23 (21 code + .pipeline/sdd/docs/index.json + events.jsonl). `git show 49094cd:data-device-api/src/commonTest/kotlin/data/device/api/IosDiscoveryFixtures.kt \| grep -c '@Test'` -> 0. | T-1 |
| AF-7 | warn | sc | SC-25 | Two named test artifacts in the traceability matrix do not exist. (a) AC-47/SC-27 name `BootTargetTest` -- there is no file by that name; its coverage was folded into `MirrorViewModelIosPlatformSwitchTest.theBootTargetIsTheUdidOnIosAndTheAvdNameOnAndroid`, so coverage EXISTS but the traceability reference is stale. (b) AC-48/SC-25 name 'Transport loop against a fake iOS executor under virtual time' -- `ScreencapMirrorTransportTest.kt` is ZERO-DIFF across this spec and has no iOS case; its 12 tests use a generic fake executor and prove the transport is platform-agnostic, which is a weaker (though reasonable) claim than the one the matrix makes. | `git diff --stat development...HEAD -- data-device-impl/src/desktopTest/.../ScreencapMirrorTransportTest.kt` is empty; `find . -name BootTargetTest.kt` returns nothing; backlog T-21 declares `presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/BootTargetTest.kt`. | T-21 |
| AF-8 | info | diff | IosDeviceCommandExecutor.kt:19-25 | Stale KDoc contradicted by this spec's own measurement. The `ALREADY_BOOTED` KDoc says 'the code this failure carries was never measured on this host (no simulator was booted to measure it against)'. T-26 subsequently measured it: exit=149. The DECISION (key on the token, never the code) remains correct and is now better justified, but the comment asserts a non-measurement that is no longer true. | IosDeviceCommandExecutor.kt:21-22 vs demo-measurements.md:36 `exit=149`. | T-12 |
| AF-9 | info | task | T-20 | Two pre-existing tests lost a stage assertion without a replacement in the same file. `MirrorViewModelAnnotationTest.kt:284` and `MirrorViewModelSessionTest.kt:162` each deleted `assertEquals(MirrorStage.IosDeferred, ...state.stage)` and asserted nothing about `stage` in its place. JUDGEMENT: legitimate, not weakening -- the asserted constant no longer exists, and the coverage was relocated to `MirrorContractTest.theStageDerivationHasNoIosBranchLeft`, which asserts that an iOS state with a streaming session now derives `MirrorStage.Mirror`. `VerifyStageDerivationTest`'s edit is similarly clean (the ordering tuple shrank from four to three because the fourth ranking rule was deleted). All ~40 edits to the four :data-repository-impl test files are mechanical constructor/signature maintenance; no assertion was loosened. | `git diff development...HEAD -- presentation-feature-mirror/src/commonTest/.../MirrorViewModelAnnotationTest.kt` is a single `-assertEquals(MirrorStage.IosDeferred, viewModel.currentState.stage)`; MirrorContractTest.kt:76-93 is the replacement. | T-20 |
| AF-10 | info | diff | MirrorErrorStage.kt imports | Import ordering violation: `mirror_error_poll_failed_title_ios` is imported after `mirror_error_simulator_not_booted_title`, breaking alphabetical order. No ktlint/detekt/spotless is configured in this repo (`grep -rn 'ktlint\|detekt\|spotless' --include=*.kts --include=*.toml` returns nothing), so this is cosmetic and cannot break the build. | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorErrorStage.kt, added import block. | T-18 |
| AF-11 | info | diff | pollIos() | Latent iOS discovery latency, not covered by any budget row. `pollIos()` calls `iosHostSource.listDevices()` (idb, MEASURED at 5.27 s) on EVERY poll, serially after `listSimulators()`, and then waits `DiscoveryPollMs` (2 s). So an iOS discovery cycle on this host costs roughly 5.3-8 s versus Android's ~2 s, and the simulator list -- the only thing the user can act on -- is held behind the idb call. A `withTimeoutOrNull`/concurrent-await or an idb-presence short-circuit would decouple them. Contained today because a missing idb short-circuits to `emptyList()` before spawning, and because the whole physical path is waived (AS-3). Flagging it so it is a known cost rather than a surprise during the first real iOS session. | data-repository-impl/.../DeviceRepositoryImpl.kt:277-283 (`listSimulators()` then `listDevices()` sequentially) + IosHostSourceImpl.kt:107-112 KDoc recording 5.26/5.27/5.28 s. | T-16 |
| AF-12 | warn | registry | features.json | `.pipeline/sdd/docs/registry/features.json` has 19 entries and NONE is `ios-platform-support`. Every other shipped spec in this repo has a feature entry (the most recent, `resolve-and-send-mcp`, is there). A whole feature touching 10 modules, 33 files in :data-device-* alone, is invisible to the registry. | `[x['id'] for x in features.json]` -> ['app-shell-window-layout', 'android-device-discovery-mirroring', ..., 'resolve-and-send-mcp'] -- no ios entry. | registry |

### gaps

#### unsatisfiedAC

- AC-48

#### unsatisfiedSC

- SC-25

#### incompleteTasks


### unresolvedFlags

| task | flag | reason |
| --- | --- | --- |
| T-15 | needsHumanReview | `EmulatorModel.runtime` was added against T-15's TAC-3 and flagged at execution time. AUDIT JUDGEMENT: JUSTIFIED. The field is nullable with a default (no Android call site changes, no wire rename), it is genuinely CONSUMED -- `DiscoveryProjection.rowDetail()` renders `runtime + udid-head` for simulator rows and returns "" when runtime is null -- and the justification is backed by this host's real capture (`iPad Pro 11-inch (M4)` under all four runtimes, two `iPhone 15 Pro` under iOS 17.5 alone), so without it the picker lists indistinguishable duplicates. It also closes the iOS half of bugs.json#PP-TD-9 while explicitly leaving the Android half open under its own gate. Recommend accept. |
| T-26 | needsHumanReview | AC-48 and SC-25 carry `verificationMethod: demo` and the demo was partial -- the primitive command was measured, the application path was not run. Requires explicit human confirmation or an explicit waiver; cannot be closed by any test this repo contains. See AF-1 and AF-4. |
| T-13 | needsHumanReview | SC-26 (physical iOS device via idb) and AS-3 (device-not-trusted) are CODE-REVIEW-ONLY / WAIVED by design, not by omission. The spec records this honestly and consistently in AC-31's text, the SC-26 traceability row, DeviceArgv.kt's `idb screenshot -` comment, IosDeviceCommandExecutor's `screencap` KDoc, and demo-measurements.md. Human must accept the waiver, not the audit. |

### regressionRun

#### command

./gradlew --max-workers=1 --no-build-cache --console=plain :data-device-api:build :data-device-impl:build :data-repository-impl:build :domain-core:build :domain-repository-api:build :domain-usecase-api:build :domain-usecase-impl:build :presentation-core-localisation:build :presentation-core-ui:build :presentation-feature-mirror:build :presentation-core-styling:build :data-preference-impl:build  THEN  ./gradlew --max-workers=1 --no-build-cache --console=plain :desktop-application:compileKotlin :aos-application:assembleDebug :compose-application:linkDebugFrameworkIosArm64

#### passed

true

#### output

```
FORCED, NOT CACHED. Before running I deleted all 31 project `build/` directories (3.4 G reclaimed; free space went 1.9 Gi -> 5.5 Gi, which also resolved the disk pressure the orchestrator warned about) AND passed `--no-build-cache` to defeat `org.gradle.caching=true` in gradle.properties. Audit of the log confirms no real work was skipped: of 144 UP-TO-DATE lines, zero are compile/test/link tasks -- they are all lifecycle no-ops (`metadataNativeMainClasses`, `androidPreBuild`, `prepareLintJarForPublish`). Zero FROM-CACHE lines.

RUN 1 -- per-module :build, --max-workers=1:
> Task :data-device-api:desktopTest
> Task :data-device-api:iosSimulatorArm64Test
> Task :data-device-impl:desktopTest
> Task :data-repository-impl:desktopTest
> Task :data-repository-impl:iosSimulatorArm64Test
> Task :domain-core:desktopTest
> Task :domain-core:iosSimulatorArm64Test
> Task :domain-usecase-impl:desktopTest
> Task :domain-usecase-impl:iosSimulatorArm64Test
> Task :presentation-core-ui:desktopTest
> Task :presentation-core-ui:iosSimulatorArm64Test
> Task :presentation-feature-mirror:desktopTest
> Task :presentation-feature-mirror:iosSimulatorArm64Test
> Task :presentation-core-styling:desktopTest
> Task :presentation-core-styling:iosSimulatorArm64Test
> Task :data-preference-impl:desktopTest
> Task :data-preference-impl:iosSimulatorArm64Test
BUILD SUCCESSFUL in 18m 22s
EXIT=0

TEST TOTALS, counted from the freshly written JUnit XML in build/test-results (not from the console):
  data-device-api        :: desktopTest             86 tests, 0 failures, 0 errors, 0 skipped
  data-device-api        :: iosSimulatorArm64Test   86 tests, 0 failures, 0 errors, 0 skipped
  data-device-impl       :: desktopTest             31 tests, 0 failures, 0 errors, 0 skipped
  data-preference-impl   :: desktopTest              9 tests, 0 failures, 0 errors, 0 skipped
  data-preference-impl   :: iosSimulatorArm64Test    9 tests, 0 failures, 0 errors, 0 skipped
  data-repository-impl   :: desktopTest             97 tests, 0 failures, 0 errors, 0 skipped
  data-repository-impl   :: iosSimulatorArm64Test   86 tests, 0 failures, 0 errors, 0 skipped
  domain-core            :: desktopTest              1 test,  0 failures, 0 errors, 0 skipped
  domain-core            :: iosSimulatorArm64Test    1 test,  0 failures, 0 errors, 0 skipped
  domain-usecase-impl    :: desktopTest              8 tests, 0 failures, 0 errors, 0 skipped
  domain-usecase-impl    :: iosSimulatorArm64Test    8 tests, 0 failures, 0 errors, 0 skipped
  presentation-core-styling :: desktopTest           6 tests, 0 failures, 0 errors, 0 skipped
  presentation-core-styling :: iosSimulatorArm64Test 6 tests, 0 failures, 0 errors, 0 skipped
  presentation-core-ui   :: desktopTest             29 tests, 0 failures, 0 errors, 0 skipped
  presentation-core-ui   :: iosSimulatorArm64Test   29 tests, 0 failures, 0 errors, 0 skipped
  presentation-feature-mirror :: desktopTest       491 tests, 0 failures, 0 errors, 0 skipped
  presentation-feature-mirror :: iosSimulatorArm64Test 479 tests, 0 failures, 0 errors, 0 skipped
  ------------------------------------------------------------------
  TOTAL                                           1462 tests, 0 failures, 0 errors, 0 skipped

The orchestrator's measured gate reported 1432; mine is 1462 over a slightly wider module set (I added :presentation-core-styling and :data-preference-impl). Either way: 0 failures.

THE REAL-TOOLCHAIN TEST ACTUALLY RAN -- it did not take its skip branch. Verbatim from the freshly written data-device-impl/build/test-results/desktopTest/TEST-data.device.impl.datasource.SimctlRealToolchainTest.xml (file mtime Oct 19 08:27:58, 18 min before this audit, i.e. produced by MY run after the build dir was deleted):
  <testsuite name="SimctlRealToolchainTest[desktop]" tests="1" skipped="0" failures="0" errors="0" time="0.316">
  <testcase name="theInstalledSimctlListsParsableSimulators[desktop]" .../>
  <system-out><![CDATA[simctl xcrun version 72. at /usr/bin/xcrun: 54 simulators, 4 runtimes [iOS 17.5, iOS 18.1, iOS 18.5, iOS 26.3], 0 booted]]></system-out>
This is the strongest single piece of evidence in the spec: real `xcrun simctl list devices available` stdout was parsed into 54 distinct-udid rows across 4 runtimes with 0 booted, on this host, in this run.

RUN 2 -- app entry points, --max-workers=1 --no-build-cache (all three executed, none UP-TO-DATE):
> Task :compose-application:compileKotlinDesktop
> Task :desktop-application:compileKotlin
> Task :aos-application:assembleDebug
> Task :compose-application:compileKotlinIosArm64
> Task :compose-application:linkDebugFrameworkIosArm64
BUILD SUCCESSFUL in 33s
EXIT=0

No heap OOM (bugs.json#PP-TD-8 did not reproduce under --max-workers=1) and no disk exhaustion.
```

#### newFailures


### aggregateDiffFindings

| id | category | location | issue |
| --- | --- | --- | --- |
| AD-1 | security | data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt | NFR-5 IS GENUINELY STRENGTHENED, and I verified this against the pre-spec code rather than taking the claim. On `development` the function was `buildDeviceArgv(toolPath, device, command) = listOf(toolPath, "-s", device.value) + command.arguments()` -- NO runtime check at all, the invariant was positional-by-construction only. HEAD adds a named, shared `private fun DeviceId.selector(segment: List<String>)` carrying `check(segment.count { it == value } == 1)`, called from all three tool branches, so a branch that forgets it cannot produce an argv. The scoping is SELECTOR-SEGMENT, exactly as claimed, and the reason is sound: `DeviceCommand.GetProp` carries unvalidated free text, so a whole-argv count would make `GetProp(name = <udid>)` throw IllegalStateException from a pure function. That must-not-throw case is swept at DeviceArgvTest.kt:128 with the mutation note attached. `DeviceId` keeps its private constructor, `DeviceCommand`/`HostCommand` stay closed sets with no `connect`/`-H`/`-P` member, and `ProcessBuilder` still appears at exactly ONE path in the whole repo (data-device-impl/src/desktopMain/.../ProcessRunner.kt). No new attack surface. |
| AD-2 | performance | data-repository-impl/.../DeviceRepositoryImpl.kt:277-283 | iOS discovery serialises a measured-5.27 s `idb list-targets` ahead of the 2 s poll interval, holding the actionable simulator list behind the physical-device list. See AF-11. Android is unaffected (`pollAndroid()` is a verbatim move and the full Android suite is green, so AC-49/NFR-20's 'byte-identical behaviour' holds). |
| AD-3 | dead-code | data-device-impl/src/desktopMain/.../IosDeviceCommandExecutor.kt:103-120 | `screencapViaTempFile()` is the PLAN-AS-4 contingency for `idb screenshot -` being rejected. It is unreachable from every test, unexecuted on every host, and guards a condition nobody has observed. It is cheap and defensible (13 lines, honestly documented as unmeasured) but it is untested error-recovery code with a plausible latent defect: `File.createTempFile` CREATES the file, and whether `idb screenshot <existing-path>` overwrites or refuses is itself unmeasured -- so the fallback could fail for a second, different reason. See AF-2(b). |
| AD-4 | inconsistent-pattern | data-device-impl/src/desktopTest/ | Executor test asymmetry: `AdbDeviceCommandExecutor` has `AdbDeviceCommandExecutorTest`; its deliberately symmetric twin `IosDeviceCommandExecutor` (named for that symmetry in its own KDoc: 'Renamed from DeviceCommandExecutorImpl for symmetry with IosDeviceCommandExecutor') has no test at all. See AF-2. |
| AD-5 | inconsistent-pattern | MirrorErrorStage.kt / MirrorContent.kt | CONVENTION PASS -- clean, recorded for completeness. design-tokens: no `Color(0x...)` literal in any changed UI file; the only `.dp` values are pre-existing named private vals (`ContentMaxWidth`, `RunningDotSize`, `ErrorCardWidth`); new copy reads `Theme.color.*` / `Theme.spacing.*`. localisation: 14 new strings, all with LITERAL apostrophes and em dashes (`I've tapped Trust - retry`, `This iPhone hasn't been trusted`) -- zero AAPT `\'` escapes, and the one `\'` in the file is inside a comment explaining the rule; `mirror_ios_deferred` removed with its last consumer. compose-preview-convention: the light/dark `@Preview` pair on `MirrorErrorStageSample` is retained and the sample was extended with err-sim, err-trust and a shared-kind-on-iOS card, so all six kinds render in both themes (GUI-only, UNEXECUTED -- no UI-test harness exists in this repo, as .claude/rules/testing.md states). platform-coverage: every `ProcessBuilder`/`idb`/`simctl` SPAWN is under `desktopMain`; commonMain carries only pure parsers, models and interfaces that merely NAME those tools, and `grep '^import java\.' ` over data-device-api/impl + data-repository-impl commonMain returns nothing; `:ios-application/` and `:compose-application/` are byte-for-byte zero-diff, as are `ScreencapMirrorTransport.kt`, `ProcessRunner.kt` and `MirrorTuning.kt` -- the spec's central architectural bet held. error-handling: a missing tool is a VALUE (`probeSimctl()` -> `absent(ToolKind.Simctl)` -> `DiscoveryModel.ToolMissing`), an empty device list is a VALUE (`UnsupportedIosHostSource.listDevices() = emptyList()`, and a missing idb short-circuits to `emptyList()` before spawning), and an idb failure mid-poll is caught and degraded to simulators-only rather than thrown. mappers: `SimulatorResourceMapper` and `IosTargetResourceMapper` contain no `throw`/`error(`/`require(`/`check(`/`!!`, and so do `parseSimctlDevices`/`parseIdbTargets`. package-layout: new public API under `source/`, `UnsupportedDeviceSources` and `ToolResolver` internal under `core/`. No violation found in any of these. |
| AD-6 | inconsistent-pattern | plan.json#traceabilityMatrix | Traceability names two artifacts that were never created (`BootTargetTest`, 'transport loop against a fake iOS executor') and a file count that is off by one. The underlying coverage exists in every case except the transport-loop row; the matrix just was not reconciled to the tree after the waves re-organised the work. See AF-6, AF-7. |

### registryConsistency

#### consistent

false

#### phantomEntries


#### missingEntries

- features.json -- ADD an `ios-platform-support` entry. It is the only shipped spec in this repo with no feature row (19 entries, none ios). Suggested shape: id `ios-platform-support`, specRef `ios-platform-support`, status `partial` (simulator path measured; physical-device path code-review-only per SC-26/AS-3; AC-48 demo unverified), modules [data-device-api, data-device-impl, data-repository-impl, domain-core, domain-repository-api, domain-usecase-api, domain-usecase-impl, presentation-core-localisation, presentation-core-ui, presentation-feature-mirror].
- features.json#android-device-discovery-mirroring -- its description should note that DeviceRepository's five methods now take `platform: PlatformModel` and that the executor/transport bindings are Koin-qualified, since that entry owns those components.
- components.json#error-card -- description still reads 'The single surface for every recoverable failure (adb missing, no device, poll failed)'. It now also renders simulator-not-booted and device-not-trusted, and the four shared kinds resolve iOS-worded copy via `MirrorErrorCard.platform`. Six kinds, not three.
- components.json#platform-tool-card -- description example is adb-only ('adb 35.0.1 . /opt/homebrew/bin'); the iOS card is now selectable and shows an xcrun probe line. Worth one clause.
- components.json#device-picker-panel -- `PickerPlatform.enabled` no longer ships iOS disabled; the emulator group label default changed from 'Emulator' to 'Virtual devices'. Cosmetic but the registry quotes these.
- bugs.json#PP-TD-9 ('EmulatorModel.toRow sets no detail') -- now HALF fixed: `DiscoveryProjection.rowDetail()` emits `runtime . udid-head` for iOS simulator rows and deliberately keeps Android's empty detail (runtime is null there). Annotate as partially resolved by F-10 with the Android half still open; do NOT close it.
- NOTE: components.json is a UI-kit registry (106 entries, all presentation-core-ui composables). The absence of IosHostSource / IosDeviceCommandExecutor / parseSimctlDevices / DeviceSourceQualifiers from it is BY DESIGN, not a gap -- no data-layer type is registered there. I found ZERO phantom entries: nothing in any registry file references IdbHostSource, UnsupportedIdbHostSource, DeviceCommandExecutorImpl, MirrorStage.IosDeferred or mirror_ios_deferred.

### traceability

| ref | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-31 | test | true | SATISFIED AS SPLIT, and the split is honestly recorded everywhere I checked. Half (a) simulator-not-booted is MEASURED: SimctlRealToolchainTest RAN in my forced gate (did not skip) and parsed real `xcrun simctl list devices available` into 54 distinct-udid rows / 4 runtimes / 0 booted; the classification and projection are then asserted by DeviceIosDiscoveryPollingTest.`only shut down simulators report SimulatorNotBooted` and MirrorIosErrorCardTest (:presentation-feature-mirror:desktopTest, 491 green). Copy matches the AC byte for byte: primary 'Boot simulator', secondary 'Choose another'. Half (b) device-not-trusted is FIXTURE-DRIVEN AND WAIVED: driven by a constructed DiscoveryModel.DeviceNotTrusted; primary 'I've tapped Trust - retry', secondary 'Use simulator'. ADVERSARIAL CHECK PASSED -- I read every new test NAME looking for one that implies hardware it never touched, and found none: `theTrustCardRepollsAndItsSimulatorFallbackOpensThePicker` and `aLoneUntrustedHandsetReportsDeviceNotTrusted` both describe projection behaviour, not a device. The picker-yields guard both halves depend on is real (DiscoveryErrorProjection.kt:87 `if (isPickerOpen && discovery.yieldsToPicker()) return null`, with DeviceNotTrusted yielding only when `simulatorsAvailable`) and is covered by DiscoveryErrorProjectionTest.`theTwoIosCardsYieldToThePickerTheirOwnSecondaryOpens`. |
| AC-46 | test | true | SATISFIED, including the structural half. The adb loop is STRUCTURALLY cancelled, not ignored: MirrorViewModel.kt:434 and :447 are now `combine(sessionDeviceId, discoveryPlatform, ::Pair).flatMapLatest { (device, platform) -> ... observeDiscovery(platform) ... }` and :480 is `discoveryPlatform.flatMapLatest { platform -> observeMirrorStats(platform) }` -- so a platform switch re-keys the operator and tears the adb flow down the same way starting a session does. All THREE `PlatformModel.Android` literals that commit 1 deliberately left behind are gone; the only remaining occurrences are the `MutableStateFlow(PlatformModel.Android)` initial value at :227 and the `when` arm in `bootTargetOf` at :1667, both correct. Covered by MirrorViewModelIosPlatformSwitchTest (4 tests incl. `aLateAndroidPollAfterTheSwitchCannotResurrectStaleStateOrRaiseAnEffect`) and DiscoveryErrorProjectionTest.`theSharedKindsCarryIosToolTextAndNeverSayAdb`. PollFailed now names the polling platform's own binary (`toolNameOf(platform)`), asserted by `parser drift surfaces as PollFailed naming xcrun rather than adb`. |
| AC-47 | test | true | SATISFIED with one caveat. Boot-by-udid is proven twice: DeviceIosDiscoveryPollingTest.`booting a simulator addresses its udid through the executor` asserts the recorded call is exactly `BOOTED_UDID to DeviceCommand.SimulatorBoot`, and `bootTargetOf` (MirrorViewModel.kt:1665) is `PlatformModel.Ios -> row.id` vs `Android -> row.avdName ?: row.id`, asserted by `theBootTargetIsTheUdidOnIosAndTheAvdNameOnAndroid`. The name is never used, which matters on this host (the real capture has two `iPhone 15 Pro` under iOS 17.5 alone). The already-booted token matches the measurement BYTE FOR BYTE -- code constant `"Unable to boot device in current state: Booted"` == demo-measurements.md's captured stderr -- and `boot()` keys on `failure.stderrTail.contains(ALREADY_BOOTED)`, never on the exit code (which measured 149, an unguessable value). CAVEAT: that swallow branch has NO test (AF-2); it is inspection-verified only. |
| AC-48 | demo | false | REQUIRES HUMAN CONFIRMATION AND IS NOT MET AS WRITTEN. The architecture is in place and verified by inspection: `ScreencapMirrorTransport` is registered twice under `named(ANDROID_SOURCE)`/`named(IOS_SOURCE)` with the matching executor injected, and the file is ZERO-DIFF. The CLI primitive is measured (`xcrun simctl io <udid> screenshot --type=png -` -> exit=0, clean PNG at offset 0, 1179x2556, status line on stderr not stdout -- which is precisely what justifies the zero-diff transport). But no live frame ever rendered in the app, no fps stat was observed, and the `captureRoundTripMs` figure the traceability row demands as the demo's deliverable was never recorded even though a simulator WAS booted. See AF-1 and AF-4. |
| AC-49 | test | true | SUBSTANCE SATISFIED, STATED EVIDENCE NOT. Substance: the entire pre-existing Android suite is green after both commits in my forced, uncached run -- 1462 tests, 0 failures across desktop + iosSimulatorArm64; `pollAndroid()` is a verbatim move; `AdbDeviceCommandExecutor` is a pure rename (git recorded it as a similarity rename). Evidence condition: AC-49 requires 'commit 1's word-diff containing only the five token shapes named in PLAN-AS-1' across 20 files. Commit 1 is 21 code files and the extra one is a new 168-line iOS fixture (AF-6). I verified the other 19 by hand -- the full production diff of DeviceRepositoryImpl.kt and MirrorViewModel.kt contains nothing but `platform` threading, the two documented `PlatformModel.Android` literals, imports and KDoc. Marking satisfied because the property AC-49 actually protects (no Android behaviour changed) is proven; the human should note the evidence wording is now falsifiable, which PLAN-AS-1 itself warned about. |
| AC-50 | inspection | true | SATISFIED BY INSPECTION, and better than required. DeviceRepositoryImpl.kt:466 `if (platform == PlatformModel.Ios) return HierarchySnapshotModel.Empty` short-circuits before any executor call; `HierarchySnapshotModel.Empty` is `HierarchySnapshotModel(emptyList(), 0, 0, 0)` so rawDump stays "". `git diff development...HEAD \| grep hierarchyXml` is EMPTY -- no wire-field rename. The bonus: DeviceIosDiscoveryPollingTest.`dumpHierarchy on iOS answers Empty without reaching the executor` injects an `ExplodingExecutor` whose run() throws, so the test fails if the iOS path ever reaches the executor. That is a genuine guard against PP-TD-5's tautology pattern, not a quiet fake. |
| SC-14 | test | true | DiscoveryModel gained SimulatorNotBooted + DeviceNotTrusted, MirrorErrorKind gained the two matching members, MirrorErrorStage gained the two branches; all exercised by MirrorIosErrorCardTest and DiscoveryErrorProjectionTest. The light/dark @Preview pair over the extended MirrorErrorStageSample covers all six kinds -- GUI-ONLY AND UNEXECUTED, which is the repo's standing limitation (no UI-test harness exists, per .claude/rules/testing.md), not a regression introduced here. |
| SC-25 | demo | false | Same unmet demo as AC-48: no booted-simulator live loop and no measured captureRoundTripMs. `pollIos() -> Found` IS covered (DeviceIosDiscoveryPollingTest.`a booted simulator is Found`), and the transport is covered generically (ScreencapMirrorTransportTest, 12 tests, zero-diff) -- but the named 'transport loop against a fake iOS executor' was never written (AF-7). |
| SC-26 | inspection | true | SATISFIED ONLY AS 'CODE-REVIEW-VERIFIED', which is exactly what the plan declared it to be. `parseIdbTargets` is fixture-tested (IdbTargetsParserTest, 102 lines), `Screencap x Idb` is swept by DeviceArgvTest (`idb appends the udid selector` asserts `[idb, screenshot, -, --udid, <udid>]`). The LIVE path is unproven and honestly labelled UNMEASURED in three separate places in the source (DeviceArgv.kt's idbArguments comment, IosDeviceCommandExecutor.screencap KDoc, demo-measurements.md). demo-measurements.md additionally records a NEW negative result not in the plan: `idb list-targets` did not report the booted simulator within a 20 s window. No test name or doc overstates this anywhere I looked. |
| SC-27 | test | true | The SimulatorNotBooted arm, its boot-by-udid target and the picker-yields secondary are all asserted (MirrorIosErrorCardTest.`bootSimulatorBootsTheUdidTheCardNamesAndTheNextPollShowsItRunning`, `chooseAnotherOpensThePickerAndTheCardStepsAsideForIt`). The DEMO click-through named in the traceability row did NOT happen -- demo-measurements.md: 'No GUI was driven.' The intent-level behaviour is machine-proven; the rendered click is not. |
| SC-28 | test | true | WAIVED-AS-DESIGNED per NFR-18/AS-3. Fixture-driven only; nothing about real idb trust behaviour is proven. MirrorIosErrorCardTest.`theTrustCardRepollsAndItsSimulatorFallbackOpensThePicker` plus DiscoveryErrorProjectionTest.`deviceNotTrustedProjectsARetryCardWhoseFallbackDependsOnASimulator`. The implementation is appropriately conservative: `pollIos()` only emits DeviceNotTrusted when `untrusted.size == 1 && simulators.isEmpty()`, so an untrusted handset never hides a usable simulator (`an untrusted handset beside a simulator never hides the simulator`). |
| SC-29 | test | true | Full pre-existing Android suite green after both commits, in a forced run with all build dirs deleted and --no-build-cache: 1462 tests, 0 failures, 0 errors, 0 skipped. Cross-target build at --max-workers=1 succeeded for desktop, iosArm64, iosSimulatorArm64 and Android, with no PP-TD-8 heap OOM. |
| NFR-5 | test | true | CONFIRMED GENUINELY STRONGER THAN BEFORE -- see AD-1 for the before/after. Selector-segment scoped via a shared `DeviceId.selector()` helper; the `GetProp(name = <udid>)` must-not-throw case is in the sweep (DeviceArgvTest.kt:128) with the mutation note ('change the check to count over the whole argv - this fails'). 86 tests in :data-device-api on BOTH desktop and iosSimulatorArm64. DeviceId keeps a private constructor; DeviceCommand/HostCommand remain closed; ProcessRunner is still the single spawn site in the repo. |
| NFR-8 | test | true | FREE BY CONSTRUCTION -- pickerKeyAction / nextSelectableIndex / onPreviewKeyEvent were not touched and PickerNavigationTest stays green (:presentation-core-ui, 29 tests both targets). Live key DISPATCH remains unverified, the same standing waiver as Android. No new keyboard surface was added. |
| NFR-9 | inspection | true | The two new cards render through the shipped ErrorCard organism using only `Theme.color.*` tokens -- I grepped the changed UI files for `Color(0x` and found none. Verified by INSPECTION and by the extended light/dark @Preview pair, which is UNEXECUTED (no UI-test harness in this repo). |
| NFR-10 | test | true | All three `provideDeviceSources()` actuals exist and were verified by reading them, not by report: desktop registers ToolResolver + 7 device bindings with real impls; androidMain and iosMain each delegate to `provideUnsupportedDeviceSources()`, which registers exactly 7 (AdbHostSource, EmulatorSource, IosHostSource, DeviceCommandExecutor x2 qualifiers, MirrorTransport x2 qualifiers) -- the claimed count is exact. DeviceSourcesModuleTest resolves all 7 on both the desktop graph and the mobile graph, AND asserts the negative half (`assertNull(koin.getOrNull<DeviceCommandExecutor>())`) so no unqualified fallback can silently absorb a forgotten qualifier. Green in my run. Caveat on the COMPOSED graph: see AF-5 -- `initKoin()` end-to-end is still not machine-verified anywhere, though a qualifier mismatch is impossible by construction because producer and consumer import the same constants from DeviceSourceQualifiers.kt. |
| NFR-18 | inspection | true | The AC-31 coverage split is written verbatim into AC-31's own text and repeated in the SC-26/SC-28 rows, the source comments and demo-measurements.md. I actively hunted for a place where the measured half is presented as covering the waived half and did not find one. SimctlRealToolchainTest is skip-guarded rather than @Ignore'd (it prints a SKIPPED line), and on THIS host it did not skip -- it ran and passed. |
| NFR-19 | test | true | Every idb/simctl SPAWN lives under data-device-impl/src/desktopMain. commonMain in data-device-api/impl and data-repository-impl names no `java.*`/`javax.*` (grep returns nothing) and carries only pure parsers, models and interfaces. Exactly ONE ProcessBuilder path in the repo. `git diff --stat development...HEAD -- ios-application/` and `-- compose-application/` are both EMPTY, and so are ScreencapMirrorTransport.kt, ProcessRunner.kt and MirrorTuning.kt -- claim 2 confirmed in full. Cross-target build green including linkDebugFrameworkIosArm64. |
| NFR-20 | test | true | Commit 1's production diff is confined to PLAN-AS-1's shapes (I read it in full, not the report). `pollAndroid()` is a verbatim move; `DeviceRepositoryImpl` documents the ignored parameter in KDoc rather than the `@Suppress("UNUSED_PARAMETER")` PLAN-AS-1 predicted -- a harmless deviation. The full Android suite is green after both commits. The one qualification is the file count (AF-6). |
