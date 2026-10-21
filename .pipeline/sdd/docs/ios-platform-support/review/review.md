# ios-platform-support — review

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | review |
| status | done |
| producedBy | sdd-build-correctness-review |
| schemaVersion | 1 |
| createdAt | 2026-10-19T06:22:45Z |
| updatedAt | 2026-10-19T06:22:45Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T06:22:45Z

## Provenance

### selfHash

057614890ca3e12caf108943e31106c07358a7e2a41bc0cb99a33a155a07caac

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | 6a6b2cb66225c7ce784d68d8a469d0f8cc1bc564f5b477de166fd4a3e13dc869 |

## Data

### verdict

approve-with-nits

### reviewers

| dimension | agent | findingCount |
| --- | --- | --- |
| merged (correctness+concurrency+architecture+conventions+performance+security+maintainability) | sdd-build-correctness-review | 11 |

### summary

Single merged reviewer at fast tier. The refactor half is clean: platform threading, the qualified Koin graph, and the ScreencapMirrorTransport zero-diff reuse all hold, and NFR-5 is genuinely stronger - every buildDeviceArgv branch routes through DeviceId.selector(), and because DeviceId.of rejects a leading dash the temp-file substitution cannot collide with a udid. The transport degrades correctly at the measured 300ms. Zero convention violations. But the headline AC did not fire on the host it was measured on: COR-1. Original verdict request-changes; revised to approve-with-nits after fd16c3b.

### findings

| id | severity | dimension | file | line | rule | summary | failureScenario | status | fixedIn | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| COR-1 | blocking | correctness | data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt | 278 | AC-31(a), AC-46, SC-27, FR-51 | pollIos treated every idb row as a physical handset; parseIdbTargets returns simulators too and isTrusted returns true for any non-device target. | On this host idb list-targets returns the committed fixture: 54 simulator rows, ZERO device rows. So usable = 54, Found(physical = 54 simulators marked isEmulator=false isSelectable=true), errorCard yields 'multipleDevices' - '54 devices connected' - the picker lists every simulator twice, and clicking a 'physical' row starts a session against a shut-down simulator, which dies. DiscoveryModel.SimulatorNotBooted is unreachable whenever idb resolves, which is the feature's entire reason to exist. Every test passed because the test helper hardcoded targetType = 'device', so the real payload never reached pollIos. | fixed | fd16c3b | The parser already carried the 'device' token and deliberately returned true for non-device rows - the information needed to prevent this was present and the single consumer never asked. Fixed as a named IosTargetResource.isPhysical predicate, with a test fed the 54-row capture byte-for-byte; red before, green after. |
| COR-2 | major | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContent.kt | 235 | FR-52(b), AC-48 | MirrorStatusLine passed toolName = ADB unconditionally while the repository feeds xcrun's version on iOS. | iOS selected, simulator mirroring: the status bar renders 'adb 26.3 - 1179x2556 - 3.3 fps'. The binary name and the version attached to it come from different toolchains. This codebase treats machine text as load-bearing. | fixed | fd16c3b |  |
| COR-3 | major | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContent.kt | 335 | FR-49, FR-52(b) | platformCards labelled the iOS card IDB, but detectTools now returns probeSimctl -> ToolProbe(kind = Simctl, path = /usr/bin/xcrun). | Tool present: the card reads 'idb 26.3 - /usr/bin/xcrun' - wrong binary beside xcrun's path and version. CLT-only Mac: the card says 'idb not found' next to a card saying 'Install Xcode'. DiscoveryErrorProjection had already made the opposite, explicitly reasoned choice; this call site was missed. | fixed | fd16c3b |  |
| COR-4 | minor | correctness | presentation-core-localisation/src/commonMain/composeResources/values/strings.xml | 67 | FR-52(b) | mirror_source_summary hardcoded 'adb devices -l'. | The iOS picker's SOURCE section reads '12 found - adb devices -l' while listDevicesCommand() already had the platform-correct spelling. | fixed | fd16c3b |  |
| COR-5 | minor | ux | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/discovery/DiscoveryErrorProjection.kt | 258 | the file's own documented card/status pairing rule | Neither indicatorDot nor indicatorLabel branched for SimulatorNotBooted or DeviceNotTrusted; both fell through to else. | iOS, no simulator booted: the err-sim card renders 'No simulator is running / Boot simulator' beside a grey OFFLINE dot labelled 'Waiting for device...'. The file's own comments call that exact contradiction out as previously fixed - blocked is waiting on a person, and a grey dot reads as idle. | fixed | fd16c3b |  |
| COR-6 | minor | correctness | data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt | 264 | SC-26, FR-51 | pollIos short-circuits on probeSimctl().found, so idb is never consulted on a CLT-only Mac. | Homebrew idb + idb_companion, Xcode CLT only, a trusted physical iPhone attached: output is ToolMissing(xcrun) with 'Install Xcode', and the handset idb can see and drive is unreachable. The inverse (simctl present, idb missing) degrades correctly, so the asymmetry is undocumented. | deferred |  | Scoped out of the fix pass by the verdict. |
| COR-7 | minor | performance | data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosDeviceCommandExecutor.kt | 99 | SC-26, PLAN-AS-4 | screencap falls back to the temp file on ANY DeviceCommandException from idb, including a timeout, and never records that the dash sink was rejected. | If idb genuinely does not accept '-' (PLAN-AS-4 records this as unverified), every frame pays a failed spawn plus a second spawn - about 6 idb processes per second at the measured 3.3 fps, forever. A device that went away pays 2x CaptureTimeoutMs per frame instead of 1x, doubling drop detection time. One var beside the existing toolByDevice cache removes both. | deferred |  |  |
| COR-8 | minor | concurrency | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 433 | FR-52(a) | The flatMapLatest re-key cancels the previous flow, but the reduction is not platform-guarded and Orbit's intent{} enqueues asynchronously. | An Android emission dispatched before selectPlatform's reduce but ordered after it repopulates rows with Android devices under an iOS toolbar. applyDiscovery(Detecting) does not clear rows and the first iOS emission is over 5.3s away, so a stale clickable bench persists for seconds. The existing late-emission test emits after advanceUntilIdle, i.e. after cancellation already propagated, so it cannot catch this ordering. | deferred |  |  |
| COR-9 | nit | maintainability | data-device-api/src/commonMain/kotlin/data/device/api/source/datasource/IosHostSource.kt | 38 |  | probeIdb has no production caller - only the interface, the impl, the Unsupported stand-in and two fakes. | Dead surface; the spec's own 'two interfaces deleted for one added' discipline argues for removing it until something renders it. It would also give COR-6 a natural home. | deferred |  |  |
| COR-10 | nit | maintainability | data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosDeviceCommandExecutor.kt | 20 |  | Two stale comments: one claimed the already-booted exit code was never measured (it is - 149), the other said the platform-aware copy split had not landed when it lands eight lines below. | A reader trusts the stated reason for keying on the stderr token and finds it false, or re-does work already done. | fixed | fd16c3b |  |
| COR-11 | nit | security | data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/IosDeviceCommandExecutor.kt | 107 | NFR-4 | File.createTempFile created the idb screenshot sink at default umask. | A full screenshot of the user's device sits in the shared temp directory readable by any local user for the duration of the capture. This is the SAME bug class F-8 already fixed, where the history archive was world-readable while its sibling slot was deliberately 0700. | fixed | fd16c3b |  |

### changeRequests

| id | about | target | status | resolvedIn |
| --- | --- | --- | --- | --- |
| RCR-1 | COR-1 filter idb targets to physical devices | DeviceRepositoryImpl.kt + IdbTargetsParser.kt | resolved | fd16c3b |
| RCR-2 | COR-2/3/4 wrong binary name in machine text | MirrorContent.kt + strings.xml | resolved | fd16c3b |
| RCR-3 | COR-5/10/11 blocked-not-offline, stale comments, 0600 temp file | DiscoveryErrorProjection.kt + IosDeviceCommandExecutor.kt | resolved | fd16c3b |

### techDebt

- COR-6 CLT-only Mac: idb never consulted when simctl is absent, so a drivable physical iPhone is unreachable.
- COR-7 idb sink-rejection is not cached, risking a doubled spawn per frame forever if idb rejects the dash sink.
- COR-8 a late Android emission can repopulate rows under an iOS toolbar for seconds; the existing test cannot catch the ordering.
- COR-9 probeIdb has no production caller.
- mirror_status_tool_missing and mirror_status_poll_failed still name adb unconditionally on the iOS status LINE - the error cards got _ios copy, the status labels did not. Same class as COR-2, found while fixing it.
- AC-48/SC-25 demo click-through never performed; no UI-test harness exists in this repo.
- The physical-iPhone idb path (SC-26, AC-31 device-not-trusted) is code-review-only; idb screenshot remains UNMEASURED.
- pollIos serialises a 5.27s idb call ahead of a 2s poll interval.
- Two traceability names (BootTargetTest, a fake-iOS-executor transport test) point at files that do not exist under those names.

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| COR-1 makes the simulator-not-booted card unreachable whenever idb resolves - exactly AC-31(a)/AC-46/SC-27 - and every test passed because the helper hardcoded targetType. Fix before merge, or merge and follow up? | ["Fix COR-1 (+COR-2/COR-3) before merge", "Merge and follow up"] | Fix COR-1 (+COR-2/COR-3) before merge | ai-proposed | Merging would record AC-31/AC-46 as satisfied for a code path unreachable on the measured host, and the verdict's waivers already rest on 'every primitive underneath the click is proven' - COR-1 shows the composition above those primitives was not. Roughly three lines of production code; deferring costs more in re-opened traceability than fixing costs now. |

### approved

true
