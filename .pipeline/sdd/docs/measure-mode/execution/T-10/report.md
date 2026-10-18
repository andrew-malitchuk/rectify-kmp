# measure-mode — T-10

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-10 |
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

6239b9a362ae8fbdc5e75515e0c06c41ec323bbfa33551d77cc1a2c8de59b3e7

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-8/report.json | be0e2bf284621fec9790d255c95e1b22f784dd3246341d91f90f51f329088fdd |

## Data

### taskId

T-10

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/discovery/DiscoveryErrorProjection.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/di/PresentationFeatureMirrorModule.kt

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

BUILD SUCCESSFUL in 7s / 57 actionable tasks: 17 executed, 40 up-to-date. Aggregated from build/test-results/desktopTest/*.xml: total 183 failures 0 skipped 0 (169 pre-existing + 14 new: MirrorViewModelMeasureTest 11, MirrorContractTest +2, MirrorViewModelAnnotationTest +1; DiscoveryErrorProjectionTest stayed at 11 because its Dumping row extends an existing table test). Per class: MirrorViewModelMeasureTest 11/0, MirrorContractTest 3/0, DiscoveryErrorProjectionTest 11/0, MirrorViewModelAnnotationTest 16/0. Cross-target: ./gradlew :presentation-feature-mirror:compileTestKotlinIosArm64 -> BUILD SUCCESSFUL in 8s; and because Orbit's dispatcher defaults are process-wide (.claude/rules/testing.md), the suite was also RUN on device: ./gradlew :presentation-feature-mirror:iosSimulatorArm64Test -> BUILD SUCCESSFUL in 14s, aggregated ios total 183 failures 0. TAC-20 gate: `grep -n '\-> Unit' presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt` printed NOTHING (exit status 1, no match).

### implementation

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/discovery/DiscoveryErrorProjection.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/di/PresentationFeatureMirrorModule.kt

#### summary

frozenHierarchy: StateFlow<HierarchySnapshotModel> (initial HierarchySnapshotModel.Empty, never null) is a third flow beside frame/frozenFrame, for a different reason than theirs: the dump is read only by core/measure and never rendered, so reducing it would copy an unread list on every state change. It is filled fire-and-forget on the freeze transition inside stageAnnotation -- hierarchyStatus = Dumping is reduced in the SAME pass as the freeze, the use case runs on viewModelScope so the freeze is instant, and the outcome is never discarded (success -> Ready with the snapshot published, any failure -> Unavailable). The dump fires only on the freeze transition and only when state.selectedDeviceId exists, so a second drag against the same pinned frame starts no second dump. The three clear sites -- selectPlatform (was line 209), stopSession (284), resumeLive (444) -- each call a new releaseHierarchy() (cancels the in-flight dumpJob, resets the snapshot flow) and reduce through a new MirrorUiState.withoutMeasurements() extension setting measure = null and hierarchyStatus = Absent; reconnect() (461) calls neither, deliberately, since it preserves the frozen frame (NFR-6). Cancelling the job is what stops a late resolution reducing Ready about a frame that is gone. startMeasure(index) is one reduce that reads state.frozen inside reduce and delegates the whole computation to core/measure's measure(annotation, index, projectMeasureNodes(snapshot, deviceSize), deviceSize, snapshot.densityDpi) -- no geometry is reimplemented, which is exactly what the frozen oracle test checks by calling the same function. endMeasure() is one reduce that writes replaceNoteLine(annotation.comment, readout.noteLine) onto the annotation named by readout.annotationIndex and clears the readout; a vanished index or an absent readout returns state unchanged. deleteAnnotation and clearStaged now also drop the readout, because it names its annotation by index and the survivors renumber -- without that, an EndMeasure that loses the race to a DeleteAnnotation writes the note onto whichever annotation inherited the number, which is precisely what MirrorViewModelAnnotationTest's extended race case forbids (it asserts comment= empty on the survivor). indicatorDot gains exactly one branch, hierarchyStatus == Dumping -> StatusIndicatorState.Dumping, inserted above the existing frozen != null case; no existing branch was reordered or altered, and this is the first producer of a state that shipped with none. PresentationFeatureMirrorModule passes dumpHierarchy = get<DumpHierarchyUseCase>() -- the constructor seam has no default because an interface cannot have one, so the Koin wiring is required for the app to compile, not optional.

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| features.json#measure-mode core/measure | startMeasure delegates to the merged T-6/T-7/T-8 pure functions measure()/projectMeasureNodes(); endMeasure reuses replaceNoteLine() for the one-line-replacement rule. No arithmetic or formatting was written in the ViewModel. |
| components.json#StatusIndicator.StatusIndicatorState.Dumping | indicatorDot now produces the already-shipped Dumping state (label 'DUMPING...', warnStillOff token) instead of a new component. |
| features.json#DumpHierarchyUseCase | The freeze-time dump calls wave 4's use case as-is; nothing in the data or domain layer was touched. |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| endMeasure vs DeleteAnnotation race (MirrorViewModelAnnotationTest.endMeasureRacingDeleteAnnotationOnTheSameIndexDoesNotResurrectIt) | Matching the readout to its annotation by index alone cannot satisfy this frozen test. With two annotations, DeleteAnnotation(1) renumbers the survivor to index 1, so an EndMeasure running afterwards finds an annotation at index 1 and writes the note onto SECOND_RECT -- but the test asserts comment= empty on the survivor. The test's KDoc phrase 'EndMeasure sees a vanished index' is not literally achievable after renumbering. | deleteAnnotation (and clearStaged) now clear state.measure in the same reduce, so the readout cannot outlive the index it names. Both interleavings then satisfy the assertion: delete-first makes EndMeasure a no-op, note-first has the delete remove the annotation it just commented. This is a production-code change, not a test change, and it is the root-cause fix rather than a guard at one call site. | info |
| PresentationFeatureMirrorModule.kt | The file is outside T-10's declared predictedScope (MirrorViewModel.kt, DiscoveryErrorProjection.kt, commonTest/**), but the new constructor parameter is an interface and therefore cannot carry a default, so without the Koin line the shipped app would not compile. | Added the one get<DumpHierarchyUseCase>() line. Two lines total (import + argument), no binding was introduced: domainUseCaseImplModule already binds DumpHierarchyUseCaseImpl from wave 4. Flagged for Stage 5 as a scope delta, not a surprise. | info |
| A stale dump resolving after the frame is released | Neither the backlog nor any frozen test covers the case where the user resumes live (or switches platform) while the ~2.7 s dump is still in flight: a naive implementation would then reduce Ready and publish a snapshot belonging to a frame that no longer exists. | The dump is held in a dumpJob field and cancelled by releaseHierarchy() at all three clear sites. Four lines; no test asserts it, so it is recorded here rather than claimed as covered. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-69
- AC-70
- AC-72
