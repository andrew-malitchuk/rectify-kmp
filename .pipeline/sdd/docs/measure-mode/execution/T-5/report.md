# measure-mode — T-5

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-5 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-16T22:19:20Z |
| updatedAt | 2026-10-16T22:19:20Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-16T22:19:20Z

## Provenance

### selfHash

5dacded2f743c5dd5ed781cc67fe1b56412e100b302785855e502b942dbf5344

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-4/report.json | dba32fe3257eb8606b802374700e68bbbb1a1d13d0f0a25f53452714d1e55d63 |

## Data

### taskId

T-5

### type

business-logic

### outcome

success

### touchedPaths

- domain-core/src/commonMain/kotlin/domain/core/source/model/device/HierarchyNodeModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/HierarchySnapshotModel.kt
- domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/DeviceRepository.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/HierarchySnapshotMapper.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/DumpHierarchyUseCase.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/DumpHierarchyUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/di/DomainUseCaseImplModule.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

#### testsWritten

- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceHierarchyDumpFallbackTest.kt::a thrown wm density degrades to the 0 sentinel while a thrown dump propagates
- data-repository-impl/src/commonTest/kotlin/data/repository/impl/DeviceHierarchyDumpFallbackTest.kt::a readable dump with no nodes keeps the parsed density instead of collapsing to Empty

#### note

The five DeviceHierarchyDumpTest tests and DumpHierarchyUseCaseImplTest remain frozen and untouched. The two wave-4 follow-up tests live in a NEW file (DeviceHierarchyDumpFallbackTest) precisely so the frozen fixture is not edited - they need an executor that can throw per command, which the frozen private fake cannot do.

### testRun

#### command

./gradlew :data-repository-impl:desktopTest :domain-usecase-impl:desktopTest  &&  ./gradlew :data-repository-impl:compileTestKotlinIosArm64 :domain-usecase-impl:compileTestKotlinIosArm64

#### passed

true

#### output

```
--- ./gradlew :data-repository-impl:desktopTest :domain-usecase-impl:desktopTest ---
> Task :data-repository-impl:desktopTest
> Task :domain-usecase-impl:desktopTest
BUILD SUCCESSFUL in 1s
34 actionable tasks: 14 executed, 20 up-to-date

(JUnit XML aggregate, build/test-results/desktopTest)
:data-repository-impl  tests=50 skipped=0 failures=0 errors=0
:domain-usecase-impl   tests=1  skipped=0 failures=0 errors=0
TEST-data.repository.impl.DeviceHierarchyDumpTest.xml          tests="5" failures="0" (frozen, untouched)
TEST-data.repository.impl.DeviceHierarchyDumpFallbackTest.xml  tests="2" failures="0"
  a thrown wm density degrades to the 0 sentinel while a thrown dump propagates[desktop] PASSED
  a readable dump with no nodes keeps the parsed density instead of collapsing to Empty[desktop] PASSED
TEST-domain.usecase.impl...DumpHierarchyUseCaseImplTest.xml    tests="1" failures="0"
=> 48 -> 50 tests in :data-repository-impl, 0 failures; :domain-usecase-impl now runs and passes

--- ./gradlew :data-repository-impl:compileTestKotlinIosArm64 :domain-usecase-impl:compileTestKotlinIosArm64 ---
> Task :domain-usecase-impl:compileTestKotlinIosArm64 UP-TO-DATE
> Task :data-repository-impl:compileKotlinIosArm64
> Task :data-repository-impl:iosArm64MainKlibrary
> Task :data-repository-impl:compileTestKotlinIosArm64
BUILD SUCCESSFUL in 1s
(no comma or colon in either new backtick test name, per .claude/rules/testing.md)

```

### implementation

#### filesChanged

- domain-core/src/commonMain/kotlin/domain/core/source/model/device/HierarchyNodeModel.kt
- domain-core/src/commonMain/kotlin/domain/core/source/model/device/HierarchySnapshotModel.kt
- domain-repository-api/src/commonMain/kotlin/domain/repository/api/source/repository/DeviceRepository.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/HierarchySnapshotMapper.kt
- data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt
- domain-usecase-api/src/commonMain/kotlin/domain/usecase/api/source/usecase/device/DumpHierarchyUseCase.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/source/usecase/device/DumpHierarchyUseCaseImpl.kt
- domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/di/DomainUseCaseImplModule.kt

#### summary

HierarchyNodeModel / HierarchySnapshotModel (with the Empty companion sentinel) added to domain-core, both marker-tagged : Model like every sibling. DeviceRepository gains suspend dumpHierarchy(deviceId): HierarchySnapshotModel, implemented in DeviceRepositoryImpl: it resolves the DeviceId (invalid id throws DeviceCommandException, same as mirror()), issues DeviceCommand.UiAutomatorDump then DeviceCommand.WmDensity back to back on the single DeviceCommandExecutor - two executor calls inside one repository call, per the wave-4 gate addendum - and maps both halves together. A wm-density command FAILURE degrades to the densityDpi=0 sentinel with a log.d (the dump is the payload; a geometry-complete snapshot is not discarded because dp conversion is unavailable); a failing dump still throws. The one-direction HierarchySnapshotMapper (data-repository-impl/core/mapper/) reuses wave 3's parseUiAutomatorDump and the pre-existing parseWmDensity; it takes a named internal HierarchyDumpStdout(dump, density) rather than two String parameters so the reworded invariant - never a snapshot from one half without the other - is enforced by the type and the two interchangeable Strings cannot be swapped at the call site. It deliberately does NOT implement ModelResourceMapper: a node model drops resource-id/text/content-desc, so no honest toResource exists. Fallbacks: no parseable nodes -> HierarchySnapshotModel.Empty; absent or unparseable density -> densityDpi = 0. frameWidthPx/frameHeightPx come from the dump's own root node bounds (first node of the depth-first walk), not wm size, per T-1's measured 9.76% override-vs-physical gap. DumpHierarchyUseCase (domain-usecase-api) + DumpHierarchyUseCaseImpl via resultLauncher(errorMapper = Failure.Technical::Platform), registered with singleOf(...) bind ... in the existing domainUseCaseImplModule device block - no new Koin module, no build-file change in any module. WAVE-4 T-5 FOLLOW-UP (both KDoc-only decisions now ratified and asserted): decision 1 KEPT unchanged (a thrown wm-density DeviceCommandException degrades to densityDpi=0 while a thrown dump still propagates) and is now pinned by a single test asserting BOTH halves, because the asymmetry itself is the decision. Decision 2 CHANGED in HierarchySnapshotMapper: the `nodes.firstOrNull() ?: return Empty` early return is gone. A dump that parses to zero nodes now keeps whatever density was obtained and reports frameWidthPx/frameHeightPx = 0 (the frame IS the root node's bounds, so no nodes means no root means no frame - 0 with a real density is the honest result, not a discarded measurement). HierarchySnapshotModel.Empty survives as the sentinel for 'nothing at all' and is now reached NATURALLY when both halves fail, instead of being a special case on the node count - which is why both frozen Empty assertions (garbage stdout and empty stdout, each paired with empty density stdout) still hold unedited. Rationale: measure mode's 'View (no match)' fallback measures the annotation rect against the frame edges and still needs the density to label its numbers in dp; discarding a good density would downgrade that fallback to px in exactly the case it is meant to keep producing full measurements. The mapper KDoc now states plainly WHY it is not a ModelResourceMapper (no honest toResource exists, and the named HierarchyDumpStdout input is what makes 'never a snapshot from one half without the other' a type-level property), so .claude/rules/mappers.md's bidirectional shape is not read as a violation.

#### reusedFromRegistry


### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest :data-repository-impl | true | 1 | [] |
| desktopTest :domain-usecase-impl | true | 1 | ["Previously red only because the frozen @Test method was non-void. The orchestrator fixed it with a block body; the assertion itself is untouched. 1 test, 0 failures."] |
| compileTestKotlinIosArm64 :data-repository-impl | true | 1 | [] |
| compileTestKotlinIosArm64 :domain-usecase-impl | true | 1 | [] |
| decision 1 asserted - density degradation asymmetry | true | 1 | ["DeviceHierarchyDumpFallbackTest: a thrown WmDensity gives densityDpi=0 with both nodes and frame intact; a thrown UiAutomatorDump propagates as DeviceCommandException. One test, two halves, so a future change cannot collapse them and stay green."] |
| decision 2 changed and asserted - nodeless dump keeps the density | true | 1 | ["DeviceHierarchyDumpFallbackTest: a READABLE nodeless dump (`<hierarchy rotation=\"0\"></hierarchy>` + the AOSP trailer) with a parseable `Physical density: 420` now yields nodes=[] and densityDpi=420 and asserts NOT equal to HierarchySnapshotModel.Empty. Red against the previous implementation, which returned Empty."] |
| frozen-assertion contradiction check | true | 1 | ["Checked all five frozen DeviceHierarchyDumpTest tests against the decision-2 change. NONE contradicts it: both `garbage stdout -> Empty` and `empty stdout -> Empty` pass densityStdout=\"\", so the density is 0 anyway and the new result is structurally equal to Empty. The distinguishing case (readable dump, no nodes, density present) is not asserted by any frozen test. No frozen test was edited."] |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| domain-usecase-impl/src/commonTest/kotlin/domain/usecase/impl/source/usecase/device/DumpHierarchyUseCaseImplTest.kt:29 | RESOLVED. The frozen @Test method was expression-bodied over runBlocking, and kotlin.test.assertIs<T> returns T, so the method's return type was non-Unit: JUnit4 rejected the class with InvalidTestClassError on desktop and Kotlin/Native rejected it as a COMPILE error for iosArm64. | Fixed by the ORCHESTRATOR, not by this agent and not by loosening anything: the method now has a block body wrapping the same runBlocking, and the assertIs<Failure.Technical.Platform> assertion is byte-for-byte unchanged. :domain-usecase-impl:desktopTest now runs the test and passes (1/1) and compileTestKotlinIosArm64 is green. The earlier out-of-band scratch proof is superseded by the real run. | info |
| domain-usecase-impl test wiring | This task is the first ever test in :domain-usecase-impl, so its commonTest source set was exercised for the first time. | The wiring is fine as .claude/rules/testing.md describes: kotlin-test arrives from LibraryConventionPlugin.configureCommonDependencies() and kotlinx-coroutines-core resolves through the associated main compilation. NO build-file change was made in any module. | info |
| data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt dumpHierarchy | RESOLVED - decision 1, KEPT and now asserted. A thrown DeviceCommandException from `wm density` is caught and degraded to the densityDpi=0 sentinel, while a thrown dump still propagates. | Ratified unchanged: the dump costs ~2.7s and is the valuable payload, so losing it to a failed trivial density probe is the wrong trade, and densityDpi=0 makes the UI label px - honest rather than confidently wrong dp, consistent with the plan's standing rule. No longer KDoc-only: DeviceHierarchyDumpFallbackTest pins BOTH halves in one test so the asymmetry cannot be collapsed silently. | info |
| data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/HierarchySnapshotMapper.kt | RESOLVED - decision 2, CHANGED and now asserted. A dump parsing to zero nodes used to return HierarchySnapshotModel.Empty even when the density parsed fine, discarding a real measurement. | Behaviour changed: the early `return Empty` is gone, so a nodeless dump keeps the parsed density and reports 0 frame dimensions (no nodes means no root means no frame - the frame comes from root bounds, so 0 is the honest answer). Empty remains the sentinel for 'nothing at all' and is now reached naturally when both halves fail. Measure mode's 'View (no match)' fallback measures against the frame edges and needs the density to label dp, so discarding it would downgrade exactly the case the fallback is meant to serve. Asserted by DeviceHierarchyDumpFallbackTest; no frozen assertion contradicted the change. | info |
| data-repository-impl/core/mapper/HierarchySnapshotMapper.kt KDoc | .claude/rules/mappers.md describes ModelResourceMapper as the data-repository-impl mapper shape, but it is strictly bidirectional and this mapping is one-direction only. | Ratified as correct and the KDoc now says so plainly, so a reviewer does not read it as a rule violation: a HierarchyNodeModel drops resource-id/text/content-desc so a toResource could only invent them, and the named internal HierarchyDumpStdout(dump, density) input is what makes 'never a snapshot from one half without the other' a property the compiler enforces rather than a convention a caller can forget. The repo's one mapping contract (Mapper fun interface) and the internal object in core/mapper/ are both kept. | info |

### backlogUpdate

#### taskId

T-5

#### status

done

### needsHumanReview

#### required

false

#### reason

Nothing left outstanding. The one previous blocker - the frozen DumpHierarchyUseCaseImplTest being non-void - was fixed by the orchestrator with a block body, the assertion untouched, and :domain-usecase-impl now runs it green on desktop and compiles for iosArm64. Both previously KDoc-only mapping decisions have been ruled on and are now asserted: the density-failure asymmetry is KEPT and pinned by a two-halves test, and the nodeless-dump case was CHANGED to preserve the parsed density (0 frame dimensions, since the frame comes from root bounds and there is no root) with its own test. No frozen test was edited; none contradicted the change.

### linkedAC

- AC-69
- AC-72
