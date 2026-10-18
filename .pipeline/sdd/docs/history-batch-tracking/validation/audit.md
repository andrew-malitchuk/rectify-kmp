# history-batch-tracking — audit

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | audit |
| status | done |
| producedBy | sdd-build-validation-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-18T20:57:02Z |
| updatedAt | 2026-10-18T20:57:02Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-18T20:57:02Z

## Provenance

### selfHash

4b88988e23140905f51de423d3bc84737b60b7a54d6a5bca0bb731a1f051106f

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | eff06fc5c8f1046cfd75c6bdac6c1ee4f41734eb1f9a24dafaab832844126a86 |

## Data

### iteration

1

### findings

| id | severity | about | ref | issue | evidence | recommendedTarget |
| --- | --- | --- | --- | --- | --- | --- |
| AF-1 | warn | task | T-14 | The mandated full regression `./gradlew build` FAILED on this machine with 2 failures, both `java.lang.OutOfMemoryError: Java heap space` at default parallelism: `:compose-application:linkReleaseFrameworkIosSimulatorArm64` (Kotlin/Native DevirtualizationAnalysis) and `:aos-application:minifyReleaseWithR8`. Re-running exactly those two tasks with `--max-workers=1` gave BUILD SUCCESSFUL, so this is heap contention between parallel konan/R8 workers against `org.gradle.jvmargs=-Xmx4096M` (gradle.properties, untouched by this branch), not a source regression introduced by F-8. T-14's own TAC-2 already encodes `--max-workers=1`. The honest statement is: F-8 is green only under a serialised build on this hardware; `./gradlew build` with default workers is red for reasons that pre-date the branch but were not re-verified against `development`. | `BUILD FAILED in 7m 7s / 1609 actionable tasks` with `e: Compilation failed: Java heap space` (konan) and `ERROR: R8: java.lang.OutOfMemoryError: Java heap space`; second run `./gradlew :compose-application:linkReleaseFrameworkIosSimulatorArm64 :aos-application:minifyReleaseWithR8 ... --max-workers=1` -> `BUILD SUCCESSFUL in 3m 35s, 581 actionable tasks: 57 executed`. | T-14 |
| AF-2 | warn | task | T-1..T-14 | `.pipeline/sdd/docs/history-batch-tracking/execution/` is EMPTY — there is no `report.json` for any of the 14 tasks, although `state.json` marks all 14 `done`. Every executor claim therefore exists only as orchestrator context and commit messages; there is no artifact to reconcile against. This audit consequently rests entirely on the real tree and on test runs I executed myself (which is the correct basis, but the missing trail is a process gap for the human gate). | `ls .pipeline/sdd/docs/history-batch-tracking/execution` -> empty; `state.json` tasks T-1..T-14 all `"status": "done"`; `events.jsonl` records wave-1..wave-10 start/end only. | orchestrator |
| AF-3 | warn | ac | AC-37 | AC-37 / FR-35 say the footer shows 'claude . session <sessionId>' sourced from the most recently persisted batch's sessionId. The shipped footer interpolates `shortBatchId(newest.id)` — a 16-bit hash of the id rendered as four hex characters (`#a41f` shape) — so the session id itself is never shown. The deviation is argued in the composable's KDoc (narrow panel, id is `<deviceId>-f<frameId>-<epochMillis>`), and it is defensible, but unlike FR-30 it was never written back into the plan as a CORRECTION OF RECORD, so the AC as frozen is not met by inspection (its own verificationMethod). | presentation-feature-mirror/.../source/mirror/MirrorHistoryTab.kt HistoryFooter: `text = stringResource(Res.string.mirror_history_footer, shortBatchId(newest.id))`; strings.xml: `<string name="mirror_history_footer">claude · session %1$s</string>`; HistoryFormatting.kt `shortBatchId` = `(id.hashCode() and 0xFFFF).toString(16).padStart(4,'0')`. | T-11 |
| AF-4 | warn | ac | AC-34 | AC-34's verificationMethod is `test`, but only half of it is tested. `inProgressCount` has unit coverage (zero, N, filtered); the omit-the-key-at-zero decision lives in `:compose-application`'s `RectifyWindow.kt`, a module that deliberately has no test wiring at all, so the badge-absent-not-zero behaviour is evidence-by-inspection only. Inspection does confirm it: `buildMap { ...; if (inProgress > 0) put(ShellTab.History, inProgress) }` and `SidebarTabItem(count: Int?)` renders the count only `if (item.count != null)`. | compose-application/src/desktopMain/.../RectifyWindow.kt:429-439; presentation-core-ui/.../molecule/bar/tab/SidebarTabs.kt:91-98; HistoryFormattingTest.kt:169-178. | T-12 |
| AF-5 | warn | task | T-14 | T-14 TAC-3 claims 'All 9 manual demo steps pass and are recorded in the PR'. There is no PR, and no record of a demo run anywhere in the repo or in `.pipeline`. Everything that is only provable by launching the desktop app was NOT executed in this audit: Koin graph resolution of the new bindings at app start, a real send writing `~/.rectify/rectify.db` and `~/.rectify/history/<batch>/`, full quit-and-relaunch durability (SC-15), the Fixed-only Verify affordance on screen (AC-36 / SC-8), row rendering of thumbnail+summary+timestamp+chip (AC-22), survival after deleting the history directory (SC-18 at the GUI), and the <=150 ms added-send-latency budget. | `ls ~/.rectify` -> No such file or directory (the app has never been run on this machine); backlog T-14 TAC-3; no demo artefact under .pipeline/sdd/docs/history-batch-tracking/. | T-14 |
| AF-6 | warn | diff | DI wiring | No automated test covers the new Koin graph: `HistoryDataSource` (needs a real `AppDatabase` single), `BatchFileStore` expect/actual, `HistoryRepositoryImpl`, and the two use cases. `MirrorViewModel` takes `recordSentBatch`/`observeHistory` with no defaults (good), so a missing binding is a hard runtime failure the first time the mirror screen is opened — and the only thing that would catch it today is launching the app. `data-device-impl`'s `DeviceSourcesModuleTest` is the existing precedent for a cheap module-resolution test. | data-database-impl/.../di/DataDatabaseImplModule.kt (new `single<HistoryDataSource>`), data-repository-impl/.../di/DataRepositoryImplModule.kt (`provideBatchStore()`, `singleOf(::HistoryRepositoryImpl)`), presentation-feature-mirror/.../di/PresentationFeatureMirrorModule.kt; no test referencing these modules exists. | T-9 |
| AF-7 | info | ac | AC-22 | AC-22's verificationMethod is `test`, but there is no Compose UI-test harness anywhere in this repo (`.claude/rules/testing.md`). Grouping, ordering, time format, short id, tone mapping and plural key are all genuinely unit-tested (HistoryFormattingTest, 13 cases, green on desktop AND iosSimulatorArm64); that each row actually paints thumbnail + summary + timestamp + status chip is evidenced only by the four private light/dark `@Preview`s and the unexecuted demo. | HistoryFormattingTest[desktop] tests=13 failures=0; iosSimulatorArm64 same 13; MirrorHistoryTab.kt previews lines ~290-330. | T-11 |
| AF-8 | info | registry | features.json#database | Registry entry is now false in four ways: name 'Database (Room KMP) -- scaffold only', status `partial`, description naming 'a placeholder ExampleEntity/ExampleDao' and 'Nothing in the shipped product reads or writes it yet', and `waived: "No production consumer, example entity/dao only, no tests."`. Reality: two real tables, a transactional DAO, a public datasource, a production consumer (HistoryRepositoryImpl -> MirrorViewModel) and 7 green DAO tests. Delta reported only — not applied, per the task boundary. | .pipeline/sdd/docs/registry/features.json entry id=`database`; data-database-impl/src/commonMain/.../source/entity/*.kt; HistoryEntryDaoTest[desktop] tests=7 failures=0. | registry |
| AF-9 | info | registry | components.json | `MirrorHistoryTab` is a new public composable with the exact shape of the already-registered `mirror-notes-tab` / `mirror-payload-tab` entries, and is not in components.json. Separately, `history-row`'s description still ends '...the History tab's intended row (tab content itself is unbuilt)', which is now false. features.json also has no feature entry for F-8 / history-batch-tracking at all. | .pipeline/sdd/docs/registry/components.json (84 entries; `mirror-notes-tab`, `mirror-payload-tab` present, no `mirror-history-tab`); features.json ids list has no history entry. | registry |
| AF-10 | info | diff | MirrorHistoryTab.kt | The composable's KDoc cites 'known issue A11Y-8' (no-op row click still announces Role.Button). A11Y-8 exists only inside this spec's plan.json/plan.md; `registry/bugs.json` tracks ids of the form PP-TD-n and has no A11Y-8. A reader following the id from the code lands nowhere. | MirrorHistoryTab.kt KDoc 'Row click is deliberately a no-op this release (known issue A11Y-8)'; `grep A11Y-8 .pipeline/sdd/docs/registry/*.json` -> no match. | registry |
| AF-11 | info | diff | MirrorUiStateSendFieldTest.kt | Judgement on the one pre-existing test that was edited: the re-baseline is LEGITIMATE maintenance, not a weakening. The invariant ('exactly one considered new field per task, nothing renamed or removed') is preserved — the baseline set gained exactly `send` (the field the test originally proved), the positive assertion became `assertEquals(setOf("history"), added)`, and the `removed == emptySet()` half is untouched. A weakened version would have relaxed the added-set to a subset check or dropped the removal assertion; neither happened. Residual nit: the file/class name (`MirrorUiStateSendFieldTest`) and the test name now describe `history`, so the name is stale. | git diff development...HEAD -- presentation-feature-mirror/src/desktopTest/.../MirrorUiStateSendFieldTest.kt (+'send' to baseline, assertion `setOf("history")`, removal assertion unchanged). | T-9 |
| AF-12 | info | diff | data-database-impl/src/commonMain/.../core/DatabaseBuilder.kt | Stale KDoc missed by the T-13 docs pass: the common `DatabaseBuilder` expect still says '**Desktop:** stores the database in the user home directory.' The desktop actual, README.md, CLAUDE.md and .claude/rules/database.md were all updated to `~/.rectify/rectify.db`; this one line was not. | data-database-impl/src/commonMain/kotlin/data/database/impl/core/DatabaseBuilder.kt:11. | T-13 |
| AF-13 | info | diff | data-database-impl ios test coverage | The reported known gap is ACCURATELY described and hides no failure: `data-database-impl` has only a `desktopTest` source set, so the iOS test tasks are genuinely NO-SOURCE/SKIPPED rather than silently failing. Verified by running them. | `:data-database-impl:compileTestKotlinIosSimulatorArm64 NO-SOURCE`, `:data-database-impl:linkDebugTestIosSimulatorArm64 NO-SOURCE`, `:data-database-impl:iosSimulatorArm64Test SKIPPED`; only `data-database-impl/build/test-results/desktopTest` exists (7 tests, 0 failures). | none |

### gaps

#### unsatisfiedAC

- AC-37

#### unsatisfiedSC


#### incompleteTasks

- T-14

### unresolvedFlags

| task | flag | reason |
| --- | --- | --- |
| T-14 | needsHumanReview | No state.json/report flag is set anywhere (no failed, blocked or needsHumanReview on any of T-1..T-14), but T-14's TAC-3 (nine manual demo steps recorded) has no evidence on disk and could not be executed by this audit — the GUI-only ACs (AC-36 demo, SC-8, full-restart half of SC-15/AC-33, the <=150 ms latency budget) are unproven and need the human gate. |

### regressionRun

#### command

./gradlew build  (then: ./gradlew :compose-application:linkReleaseFrameworkIosSimulatorArm64 :aos-application:minifyReleaseWithR8 :domain-usecase-impl:iosSimulatorArm64Test :domain-core:iosSimulatorArm64Test :data-database-impl:iosSimulatorArm64Test --max-workers=1)

#### passed

false

#### output

```
RUN 1 — ./gradlew build (default parallelism)

> Task :compose-application:linkReleaseFrameworkIosSimulatorArm64 FAILED
w: Cannot infer a bundle ID from packages of source files and exported dependencies, use the bundle name instead: application_compose.
e: Compilation failed: Java heap space

 * Compiler version: 2.4.21
 * Output kind: FRAMEWORK

e: java.lang.OutOfMemoryError: Java heap space
	at org.jetbrains.kotlin.backend.konan.util.LongHashSet.grow(LongHashSet.kt:66)
	at org.jetbrains.kotlin.backend.konan.optimizations.DevirtualizationAnalysis$DevirtualizationAnalysisImpl$ConstraintGraphBuilder.addEdge(DevirtualizationAnalysis.kt:1129)
	... (konan LTO/Devirtualization stack)

> Task :aos-application:minifyReleaseWithR8 FAILED
ERROR: R8: java.lang.OutOfMemoryError: Java heap space
	at com.android.tools.r8.internal.fz0.a(R8_9.4.24_...)
	... (R8 ForkJoinPool stack)

FAILURE: Build completed with 2 failures.
1: Execution failed for task ':compose-application:linkReleaseFrameworkIosSimulatorArm64' > Compilation finished with errors
2: Execution failed for task ':aos-application:minifyReleaseWithR8' > A failure occurred while executing R8Task$R8Runnable > Compilation failed to complete

BUILD FAILED in 7m 7s
1609 actionable tasks: 637 executed, 12 from cache, 960 up-to-date

RUN 2 — the two failing tasks plus the three iOS test tasks that had no results, --max-workers=1

> Task :domain-usecase-impl:linkDebugTestIosSimulatorArm64
> Task :domain-usecase-impl:iosSimulatorArm64Test
> Task :domain-core:linkDebugTestIosSimulatorArm64
> Task :domain-core:iosSimulatorArm64Test
> Task :data-database-impl:compileTestKotlinIosSimulatorArm64 NO-SOURCE
> Task :data-database-impl:linkDebugTestIosSimulatorArm64 NO-SOURCE
> Task :data-database-impl:iosSimulatorArm64Test SKIPPED

BUILD SUCCESSFUL in 3m 35s
581 actionable tasks: 57 executed, 524 up-to-date

TEST TOTALS (parsed from every build/test-results/**/TEST-*.xml after both runs)
presentation-feature-mirror desktopTest        tests=382 failures=0 errors=0
presentation-feature-mirror iosSimulatorArm64  tests=370 failures=0 errors=0
data-repository-impl        desktopTest        tests=71  failures=0 errors=0
data-repository-impl        iosSimulatorArm64  tests=68  failures=0 errors=0
data-device-api             desktopTest/ios    tests=61/61 failures=0 errors=0
data-device-impl            desktopTest        tests=26  failures=0 errors=0
presentation-core-ui        desktopTest/ios    tests=29/29 failures=0 errors=0
mcp-application             test               tests=27  failures=0 errors=0
data-handoff-api            desktopTest        tests=15  failures=0 errors=0
data-preference-impl        desktopTest        tests=9   failures=0 errors=0
data-database-impl          desktopTest        tests=7   failures=0 errors=0  (no iOS task — NO-SOURCE)
presentation-core-styling   desktopTest        tests=6   failures=0 errors=0
domain-usecase-impl         desktopTest/ios    tests=2/2 failures=0 errors=0
domain-core                 desktopTest/ios    tests=1/1 failures=0 errors=0
TOTAL tests=1167 failures=0 errors=0

F-8's own suites, all green on every target they exist for:
HistoryFormattingTest        13 desktop + 13 iosSimulatorArm64
MirrorViewModelResolveAndSendTest 11 desktop + 11 iosSimulatorArm64 (5 of them new in F-8)
ThumbnailBitmapTest           4 desktop (Skiko-dependent, desktopTest by design)
HistoryEntryMapperTest        9 desktop + ios
HistoryRepositoryImplTest     7 desktop + ios
HistoryEntryDaoTest           7 desktop only
FileSystemBatchFileStoreTest  3 desktop only
RecordSentBatchUseCaseImplTest 1 desktop + ios
MirrorUiStateSendFieldTest    1 desktop (re-baselined, passing)
```

#### newFailures

- :compose-application:linkReleaseFrameworkIosSimulatorArm64 — java.lang.OutOfMemoryError: Java heap space (konan DevirtualizationAnalysis); PASSES when re-run with --max-workers=1, so environmental/parallelism, not an F-8 source regression
- :aos-application:minifyReleaseWithR8 — java.lang.OutOfMemoryError: Java heap space (R8); PASSES when re-run with --max-workers=1, same cause

### aggregateDiffFindings

| id | category | location | issue |
| --- | --- | --- | --- |
| AD-1 | security | data-repository-impl/src/desktopMain/.../core/store/FileSystemBatchFileStore.kt | Clean: the batch id (which embeds a device id) is sanitised with `Regex("[^A-Za-z0-9._-]")` before it becomes a directory name, and that is covered by `writeSanitisesABatchIdSoItCannotEscapeTheHistoryRoot`. No HTTP client, no new permission, nothing logged beyond ids and paths — NFR-4 holds by construction. Residual (accepted by FR-36, not a defect): frame PNGs, per-annotation crops, hierarchy XML (which can contain on-screen text) and prompts are stored in plaintext under ~/.rectify/history with no delete or retention UI and no prune, so the on-disk footprint and its exposure grow without bound. |
| AD-2 | performance | presentation-feature-mirror/.../MirrorViewModel.kt resolveAndSend/thumbnailPng | The plan's 'added latency on resolveAndSend (p95) <= 150 ms' is modelled, not measured. `postSideEffect(SendComplete)` is now gated behind cropAndScale + PNG encode + up to ~1 MB of file writes + one DB transaction; the ordering is deliberate and correct (history before the toast, failure dropped), and all of it is off the UI thread (Dispatchers.Default / Dispatchers.IO, NFR-14 satisfied by inspection), but the user-visible toast delay has never been timed. Only a GUI run can close this. |
| AD-3 | performance | MirrorContract.kt MirrorUiState.history | Every persisted batch's inline thumbnail rides in reduced Orbit state forever, and nothing prunes history. The budget ('<= 10 MB of thumbnail bytes at 1000 rows') follows from the measured 541-byte fixture thumbnail and the 8 KB cap, so it is plausible, but it is an extrapolation: no test and no run exercises a large history. `ByteArray` identity equality also means each DB emission produces a new state instance regardless of content; mitigated, not eliminated, by `key = { it.id }` and `remember(entry.id)`. |
| AD-4 | dead-code | history write path (entity, mapper, repository, domain model) | Written or exposed but never read anywhere in F-8: `HistoryEntryEntity.prompt`, `.deviceLabel`, `.dirPath`, `.hierarchyPath`, `.framePath`, `HistoryAnnotationEntity.cropPath`/rect columns, `HistoryEntryModel.framePath`, the rowid returned by `HistoryDataSource.insert`, `HistoryRepository.updateStatus` (FR-33 says F-8 never calls it), and four of the five `BatchStatusModel` values (only `Sent` is ever written). All are declared F-9 seams and FR-33 explicitly blesses `updateStatus`; listing them so the human gate sees how much of this diff is unexercised-by-design rather than live behaviour. The only one currently reachable end-to-end is `status` via the DAO test. |
| AD-5 | inconsistent-pattern | compose-application/.../RectifyWindow.kt:429-439 | Within one tab strip, History omits its key at zero (AC-34) while Notes and Payload are still put unconditionally with `mirror.stagedCount`, so with nothing staged the strip paints 'Annotations 0 · Payload 0 · History'. The Notes/Payload behaviour pre-dates this branch and no AC covers it, but the asymmetry is new and visible, and the AC-34 comment ('SidebarTabs renders any non-null count, so a zero would paint a literal "0"') documents exactly the bug the siblings still have. |
| AD-6 | inconsistent-pattern | data-repository-impl/build.gradle.kts +implementation(projects.dataDatabaseImpl) | First impl-on-impl module edge in the repo: `data-repository-impl` previously depended only on `*-api` modules (dataPreferenceApi, dataDeviceApi, dataDatabaseApi). It now compiles against `data-database-impl` because `.claude/rules/database.md` places entity-specific datasources in `-impl` (and the Room `@Entity` wire types with them). The rule amendment in this diff documents the `source/entity/` exception but not the new dependency direction; worth an explicit note so the next planner does not treat it as precedent for arbitrary impl->impl edges. |
| AD-7 | inconsistent-pattern | convention compliance sweep | No violations found against the rules checked. domain-layer: one-shot `RecordSentBatchUseCase` returns `Optional` via `resultLauncher(errorMapper = Failure.Technical::Database)` (no raw runCatching in the use case), reactive `ObserveHistoryUseCase`/`HistoryRepository.observe()` return bare `Flow<List<HistoryEntryModel>>`. mappers: `HistoryEntryMapper` is a named object, never throws (unknown status -> Sent, proven by test), never touches the filesystem, and deliberately avoids a fabricated inverse. koin-di: `InitKoin.kt` is NOT in the diff — module load order is byte-identical. orbit-mvi: follows the mirror's local precedent (intent -> reduce, side effect for the toast, collector started in `container { }`). design-tokens: the only raw `.dp` is `private val PreviewPanelHeight: Dp = 420.dp`, which copies `MirrorPayloadTab.kt`'s existing preview-only constant; no `Color(0x…)`, no inline `TextStyle`. localisation: 14 new keys, all consumed via `Res.string.*`, zero AAPT `\'` escapes. package-layout: `core/` stays internal-only, with the approved `source/entity/` exception for the public datasource wire types. compose-preview-convention: four private previews, light+dark pairs, wrapped in `AppTheme`. |

### registryConsistency

#### consistent

false

#### phantomEntries

- features.json#database — name 'Database (Room KMP) -- scaffold only', status 'partial', description claiming 'a placeholder ExampleEntity/ExampleDao' and 'Nothing in the shipped product reads or writes it yet', and waiver 'No production consumer, example entity/dao only, no tests.' All four are now false: ExampleEntity/ExampleDao are deleted (zero references repo-wide), two real tables ship, HistoryRepositoryImpl is a production consumer, and HistoryEntryDaoTest has 7 green cases.
- components.json#history-row — description tail '(tab content itself is unbuilt)' is now false; MirrorHistoryTab is the content.

#### missingEntries

- components.json — no `mirror-history-tab` entry for the new public `MirrorHistoryTab(state, onIntent, modifier)` composable, although its siblings `mirror-notes-tab` and `mirror-payload-tab` are registered.
- features.json — no feature entry for F-8 / history-batch-tracking (sent-batch history: DB, file store, repository, use cases, History tab).
- bugs.json — A11Y-8 is referenced from shipped code (MirrorHistoryTab KDoc) but exists only in this spec's plan; either register it or reword the comment.

### traceability

| ref | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-22 | test | true | PARTIAL BY EVIDENCE TYPE. Grouping/ordering/format are proven: HistoryFormattingTest (13 cases, green desktop + iosSimulatorArm64) covers calendar-day bucketing, newest-first within a group, dropped empty buckets, zero-padded time, four-hex short id, tone-per-status and plural key; HistoryEntryDaoTest `observe orders newest first` (green). The rendered row (thumbnail + summary + timestamp + chip inside the shipped HistoryRow) is inspection + @Preview only — this repo has no Compose UI-test harness. |
| AC-33 | test | true | HistoryEntryDaoTest `rows survive closing and re-opening the same file` (green, real BundledSQLiteDriver on a temp file) proves batch + children + thumbPng survive a close/reopen; `deleting a batch cascades to its annotations` proves the FK; `status update replaces the stored value` proves the chip's source. Full app quit-and-relaunch is GUI-only and was NOT executed. |
| AC-34 | test | true | Half tested, half inspected (see AF-4): HistoryFormattingTest asserts inProgressCount of 0 for an empty list, 0 when no row is InProgress, and 2 when two are; the omit-at-zero itself is `buildMap { ... if (inProgress > 0) put(ShellTab.History, inProgress) }` in RectifyWindow.kt, in a module with no tests, and SidebarTabItem renders the count only when non-null. No automated test can fail if that `if` is deleted. |
| AC-35 | test | true | HistoryFormattingTest `unionRectEnvelopesEveryScatteredAnnotation` (three scattered rects, result is the union, not the first) + `thumbnailRectExpandsByTheDesignScaledPad` / `...ClampsToTheFrameOnAllFourSides`; ThumbnailBitmapTest drives the whole union->thumbnailRect->cropAndScale->AwtCropEncoder chain on a 1080x2400 fixture. Production path confirmed by reading MirrorViewModel.thumbnailPng, which calls exactly those three functions in that order. |
| AC-36 | demo | false | REQUIRES HUMAN CONFIRMATION. Code is correct by inspection — `trailing = { if (entry.status == BatchStatusModel.Fixed) AppButton(... AppButtonSize.ListAction) }` in MirrorHistoryTab, with all five statuses present in the light/dark previews, and `verifyBatchIsAnInertStubRatherThanACrash` proving the intent is inert. No demo was run. |
| AC-37 | inspection | false | NOT MET AS WRITTEN (AF-3). Inspection of HistoryFooter shows `claude · session #<4 hex chars of a hash of the id>`, not the sessionId the AC and FR-35 name. The substitution is argued in the KDoc but was never recorded as a correction to FR-35. Footer presence/absence logic itself is correct (hidden when empty). |
| AC-38 | test | true | Verified in production code, not just test names: `sentBatch(envelope, images, held.bytes, thumbPng)` is built from the in-memory envelope and the published HandoffImage bytes (`images.firstOrNull { it.name == path }?.bytes`) and `held.bytes`, so nothing is re-read from the handoff slot F-7 prunes. HistoryRepositoryImplTest `recordInsertsPathsThatAreChildrenOfTheDirectoryTheStoreReturned` asserts frame.png/hierarchy.xml/crop-N.png are handed to the store and their paths stored; FileSystemBatchFileStoreTest writes them for real on desktop. Surviving a LATER send plus a restart on a real machine is the demo half and was not run. |
| SC-8 | demo | false | PARTIAL BY DESIGN (the plan says so) AND UNDEMONSTRATED. F-8 ships only the affordance and its Fixed-only visibility rule plus `HistoryRepository.updateStatus` as F-9's hook; the diff and the resolved-marking are F-9's. statusTone is tested for all five values; the button on screen is demo-only. |
| SC-15 | test | true | Same evidence as AC-33 at the storage layer (reopen-the-same-file test green), plus `~/.rectify/rectify.db` as the desktop path with `mkdirs()` and `setQueryCoroutineContext(Dispatchers.IO)`, and thumbPng asserted non-null after reopen. 'Send two, quit, relaunch' was not executed. |
| SC-16 | test | true | `historyFromTheUseCaseReachesState` (state starts empty, then reflects the fake flow) and `theBatchIsRecordedBeforeSendCompleteIsPosted` (asserts `recordedAt == listOf(0)`, i.e. zero SendComplete effects had been posted when the write ran) — both green on desktop AND iosSimulatorArm64. The collector is started in `container(MirrorUiState()) { ... observeHistoryEntries() }`. |
| SC-17 | test | true | unionRect over three scattered rects is asserted to be the envelope of all three (not the first), and ThumbnailBitmapTest confirms the encoded result of that union is <=128 px on both axes and <=8192 bytes (fixture measured 541 bytes, recorded as a comment, not asserted). |
| SC-18 | test | true | HistoryRepositoryImplTest `recordStillInsertsTheRowWithNullPathsWhenTheStoreCouldNotWriteAnything` (row inserted, dirPath/framePath/hierarchyPath and both cropPaths null, thumbPng non-null) + HistoryEntryMapperTest `toEntitiesLeavesEveryPathNullWhenTheStoreFailed` and `toModelCarriesAStoredPathThroughWithoutCheckingThatTheFileStillExists`. The mapper demonstrably never touches the filesystem, so a row whose files were deleted still renders from the inline BLOB. |
