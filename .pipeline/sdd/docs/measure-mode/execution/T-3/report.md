# measure-mode — T-3

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-3 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-16T21:55:19Z |
| updatedAt | 2026-10-16T21:55:19Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-16T21:55:19Z

## Provenance

### selfHash

d037be795441ce3196a820f034d085e77074657532876416b071f3f30794a1c6

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-2/report.json | 264bb1abfe4f4cff8e7284029163e71dedc7f9f0fd3042b0fefea549909750de |

## Data

### taskId

T-3

### type

business-logic

### outcome

success

### touchedPaths

- data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceCommand.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/tuning/MirrorTuning.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/DeviceCommandExecutorImpl.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

### testRun

#### command

./gradlew :data-device-api:desktopTest :data-device-impl:desktopTest :data-device-api:compileTestKotlinIosArm64

#### passed

true

#### output

```
> Task :data-device-api:desktopTest
> Task :data-device-impl:desktopTest FAILED
DeviceArgvTest[desktop] tests="6" failures="0"   <- UiAutomatorDump argv + exhaustive sealed list green
DeviceCommandExecutorImplTest[desktop] > uiAutomatorDumpIsBudgetedByDumpTimeoutMsNotTheHostCommandBudget[desktop] FAILED
    java.lang.AssertionError: expected:<[(underBudget, ok), (overBudget, timeout)]> but was:<[(underBudget, failed adb not found), (overBudget, failed adb not found)]>
26 tests completed, 1 failed  (data-device-impl)
61 tests completed, 1 failed  (data-device-api; the one failure belongs to T-4, not T-3)

Out-of-band proof the timeout branch is correct (throwaway DumpBudgetProbe, same assertions,
ToolResolver wired the way the passing AdbHostSourceImplTest wires it -- env ANDROID_HOME +
homeDir "" + <sdk>/platform-tools/adb layout; probe deleted after the run):
> Task :data-device-impl:desktopTest
BUILD SUCCESSFUL in 20s
(8s sleep survived, 12s sleep timed out: only DumpTimeoutMs=10s produces that pair; 3s or 5s
would have failed the under-budget leg.)

> Task :data-device-api:compileTestKotlinIosArm64
> Task :data-device-impl:compileKotlinDesktop
BUILD SUCCESSFUL in 4s

--- re-run after the test-side fix, verified by the orchestrator ---
BUILD SUCCESSFUL in 2s (26 actionable tasks: 5 executed, 21 up-to-date)
```

### implementation

#### filesChanged

- data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceCommand.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt
- data-device-api/src/commonMain/kotlin/data/device/api/source/tuning/MirrorTuning.kt
- data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/DeviceCommandExecutorImpl.kt

#### summary

One commit (a160fac): DeviceCommand.UiAutomatorDump on the closed sealed set, its argv branch returning exactly [adb, -s, <id>, exec-out, uiautomator, dump, /dev/tty], MirrorTuning.DumpTimeoutMs = 10_000L (KDoc cites the measured 2317/2711/2938 ms), and the UiAutomatorDump -> DumpTimeoutMs branch in DeviceCommandExecutorImpl.timeoutMs() above the `else`. timeoutMs() visibility unchanged (still private). No other API change. Note the sealed-case count in data-device-api/CLAUDE.md (5 -> 6 cases) rode in the T-4 commit, which is where that file is attributed.

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | ["DeviceCommandExecutorImplTest fails on its own harness, not on the implementation: the frozen test constructs ToolResolver with only isExecutable, leaving env = System.getenv() and the default homeDir, and puts the fake adb directly in a temp dir, so probeCandidates never yields that path and requirePath throws 'adb not found' before any timeout is applied.", "Harness defect resolved by the test author after this report was first written: fakeAdbSleeping now writes the script to <sdk>/platform-tools/adb and ToolResolver is wired with env=ANDROID_HOME, homeDir=\"\" and a pinned isExecutable, matching AdbHostSourceImplTest's three-field pattern. Sleep durations are still derived from MirrorTuning.DumpTimeoutMs, never the literal. ./gradlew :data-device-impl:desktopTest BUILD SUCCESSFUL in 27s. No production code changed for the fix."] |
| desktopTest (DeviceArgvTest only) | true | 1 | [] |
| out-of-band budget probe (throwaway, deleted) | true | 1 | ["Same assertions as the frozen test with ToolResolver wired like AdbHostSourceImplTest: underBudget=ok, overBudget=timeout, 20 s wall clock. Proves the UiAutomatorDump branch resolves to 10 s and not to CaptureTimeoutMs (5 s) or HostCommandTimeoutMs (3 s)."] |
| compileTestKotlinIosArm64 + compileKotlinDesktop | true | 1 | [] |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| data-device-impl/src/desktopTest/kotlin/data/device/impl/datasource/DeviceCommandExecutorImplTest.kt | The frozen test cannot pass against any correct implementation: its fake adb is unreachable by ToolResolver, so both legs report 'failed adb not found' rather than ok/timeout. ToolResolver only accepts candidates produced by probeCandidates(kind, env, homeDir); the test overrides neither env nor homeDir and uses a flat temp dir instead of <sdk>/platform-tools/adb. | Left unedited (anti-overfit). Test-side fix is three lines, copied verbatim from the passing AdbHostSourceImplTest.adbHostSource: put the script at <sdk>/platform-tools/adb and pass env = mapOf("ANDROID_HOME" to sdk.absolutePath), homeDir = "", isExecutable = { it == adb.absolutePath }. Verified by a throwaway probe doing exactly that, which passed and was then deleted. | warn |
| T-3 frozen test | A frozen test was red on a harness/predicate defect, not on production behaviour. The implement agent refused to edit it and proved the implementation correct out-of-band. | Harness defect resolved by the test author after this report was first written: fakeAdbSleeping now writes the script to <sdk>/platform-tools/adb and ToolResolver is wired with env=ANDROID_HOME, homeDir="" and a pinned isExecutable, matching AdbHostSourceImplTest's three-field pattern. Sleep durations are still derived from MirrorTuning.DumpTimeoutMs, never the literal. ./gradlew :data-device-impl:desktopTest BUILD SUCCESSFUL in 27s. No production code changed for the fix. | info |

### needsHumanReview

#### required

false

#### reason

Resolved before the wave checkpoint. The out-of-band probe is no longer the only evidence: the frozen test itself now passes against the unchanged production branch.

### linkedAC

- AC-69
