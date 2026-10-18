# measure-mode — T-9

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-9 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T07:40:07Z |
| updatedAt | 2026-10-17T07:40:07Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-17T07:40:07Z

## Provenance

### selfHash

4a42d2a2abbf0cc4f673b3f00bcdcedd01b7f84a0ecdc2b5a1b6e465c889f871

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-8/report.json | be0e2bf284621fec9790d255c95e1b22f784dd3246341d91f90f51f329088fdd |

## Data

### taskId

T-9

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest

#### passed

true

#### output

BUILD SUCCESSFUL in 7s / 57 actionable tasks: 17 executed, 40 up-to-date -- aggregated from build/test-results/desktopTest/*.xml: total 183 failures 0 skipped 0, of which MirrorContractTest 3 tests 0 failures (measureDefaultsToNullAndHierarchyStatusDefaultsToAbsent and anUnchangedMeasureReadoutComparesEqual are T-9's two). Also green on iosSimulatorArm64Test: total 183 failures 0; compileTestKotlinIosArm64 BUILD SUCCESSFUL. The run was taken at the batch tip (after T-10's commit), because the batch's frozen tests are a single tree: MirrorFakes.kt already passes dumpHierarchy to the constructor and MirrorViewModelMeasureTest references frozenHierarchy, so the suite cannot compile at T-9's own commit. T-9's own two assertions are in MirrorContractTest and depend on nothing from T-10.

### implementation

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt

#### summary

MirrorUiState gains measure: MeasureReadout? = null and hierarchyStatus: HierarchyStatus = Absent (both types already shipped in T-6/T-8, so nothing new was minted for them); MirrorIntent gains StartMeasure(index: Int) and EndMeasure as sealed cases with no handler. MirrorViewModel.kt is untouched by this commit (verified: the commit's diffstat is one file, 26 insertions) -- handleIntent's when (intent) is a statement, so Kotlin demands no exhaustiveness and the module compiles with no branch for either case. No stub '-> Unit' branch was added anywhere, per CARRY-FORWARD-inert-intents.md. The hierarchyStatus KDoc records why the snapshot itself is NOT reduced (it is a few-hundred-node list read only by core/measure), which is the decision T-10 then implements as a StateFlow.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| T-9 commit boundary | T-9's DoD clause 'desktopTest green' cannot be evaluated at T-9's own commit: the frozen tests for both tasks were authored and frozen as one tree, and MirrorFakes.kt (shared harness) already passes a dumpHierarchy argument the T-9-only constructor does not accept. | Both commits were made in the required order (T-9 MirrorContract.kt only, then T-10), and the suite was run once at the batch tip. T-9's two new assertions live in MirrorContractTest and exercise only T-9's fields, so the clause is satisfied jointly rather than per-commit. Recorded for Stage 5 rather than papered over. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-70
- AC-72
