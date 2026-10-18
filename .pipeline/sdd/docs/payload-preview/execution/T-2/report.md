# payload-preview — T-2/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-2/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T17:22:52Z |
| updatedAt | 2026-10-17T17:22:52Z |

## Gate

### type

auto

### approvedBy

orchestrator-verified

### approved

true

### at

2026-10-17T17:22:52Z

## Provenance

### selfHash

775c83e14c4c77df9c9549d45166eac81c6cf99b56c8194ac49780a64b6a4e7f

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | a0d78474590386b4950f70a3807ae7a5e9006ae680e326103a8e1d652c3aef23 |

## Data

### taskId

T-2

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/PayloadEstimate.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PayloadEstimateTest.kt

### tdd

#### cycle

green

#### testsFrozen

true

#### testsWritten

- PayloadEstimateTest: 18 tests covering tokenEstimate/payloadSizeLabel/footerLabel for counts 0,1,2,3,12 plus PAYLOAD_FORMAT_VERSION, the leading-space unit and the literal U+00B7 middot

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun  then  :presentation-feature-mirror:build :presentation-core-ui:build

#### passed

true

#### output

Verified independently by the orchestrator with --rerun: :presentation-feature-mirror:desktopTest 244 tests, 0 failures, 0 errors, 0 skipped (+26 this wave); :presentation-core-ui:desktopTest 29 tests, 0 failures. Cross-target :build green for BOTH modules (includes compileTestKotlinIosArm64 and runs iosSimulatorArm64Test). PayloadEstimateTest 18/18 green.

### implementation

#### summary

New internal core/payload/PayloadEstimate.kt: tokenEstimate (360 + count*140, FINAL heuristic, no tokenizer), payloadSizeLabel (oneDecimal(1.6 + count*0.12) + ' MB'), footerLabel ('frame.png · N crops · hierarchy.xml'), PAYLOAD_FORMAT_VERSION = 1. oneDecimal is hand-rolled on (value*10).roundToInt() — String.format is JVM-only and would break Kotlin/Native; it appears in this file only inside a KDoc line explaining why it is avoided. Rounds rather than truncates (count 3 -> '2.0 MB') and always emits the tenths digit (count 12 -> '3.0 MB').

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | [] |
| cross-target-build | true | 2 | ["one unexplained iOS release-link failure, green on re-run"] |
| frozen-test-integrity | true | 1 | [] |
| rounding-boundary | true | 1 | ["TAC-1's boundary clause was initially unmet: counts 0/1/2/12 have hundredths 0,2,4,4 so rounding and truncation never diverge. Orchestrator sent the test author back to add count 3 (1.96 -> 2.0 round vs 1.9 truncate) before freeze; a truncating implementation would otherwise have passed the whole suite."] |

### troubleshooting

| at | severity | issue | resolution |
| --- | --- | --- | --- |
| frozen-test integrity | info | Implementer could have weakened a frozen test to go green. | Orchestrator verified: no test file modified (git status shows only the two new untracked test files), 18+8 @Test, zero backticks, zero @Ignore. |
| wave-2 cross-target build | warn | iosSimulatorArm64 linkReleaseFramework failed once with no diagnostic line under parallel release links. | Passed unchanged on re-run; orchestrator could not reproduce. Treated as a Kotlin/Native linker flake, not a code defect. Logged as a Stage 7 reflection candidate. |
| oneDecimal | info | core/stats/StatsFormatter.kt already has a private tenths-formatting trick; reusing it would mean widening its visibility across package boundaries for two lines. | Kept a local private oneDecimal using the same technique. No shared helper introduced. |

### registryUpdates


### needsHumanReview

#### required

false

#### reason

Both gates green on real runs, verified by the orchestrator.

### linkedAC

- AC-16
- AC-78

### backlogUpdate

#### taskId

T-2

#### status

done

### coverage

skipped (config.coverageTool = none)
