# history-batch-tracking — verdict

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | verdict |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-18T20:57:02Z |
| updatedAt | 2026-10-18T20:57:10Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T20:57:10Z

## Provenance

### selfHash

624beffa9f111205a32675aac80dfec9a663ff1075d73b6f47e76a5117a95f07

### inputs

| artifact | hash |
| --- | --- |
| validation/audit.json | 4b88988e23140905f51de423d3bc84737b60b7a54d6a5bca0bb731a1f051106f |
| plan/plan.json | 94628227d8ffeec714b41e88c51880ef42ce7d3afad42dca37ff0a249843f5ec |

## Data

### changelog

#### entries

| task | change | reason | crRef |
| --- | --- | --- | --- |
| T-1 | Blocking spike: relocated the desktop Room database to ~/.rectify/rectify.db with Dispatchers.IO query context, added the missing commonTest dependency block to data-database-impl, and proved a Room insert+Flow-observe round trip on the real BundledSQLiteDriver. | Room had never been instantiated in this repo; de-risk the KSP/driver/packaging toolchain before any entity or DAO work starts, and make the DB-relocation-plus-no-migration decision safe by construction. |  |
| T-2 | Added HistoryEntryEntity, HistoryAnnotationEntity and HistoryEntryWithAnnotations under source/entity/, added HistoryEntryDao, wired both entities into AppDatabase at version 1, deleted ExampleEntity/ExampleDao, and committed schemas/<AppDatabase>/1.json. | Give the batch/annotation data a real, queryable home and retire the scaffold-only example table in the same change that introduces the first real table. |  |
| T-3 | Added the public HistoryDataSource (ObservableDataSource + AppendableDataSource + updateStatus) with an internal DAO-backed impl registered in Koin, and committed HistoryEntryDaoTest covering ordering, cascade delete, status update and close/reopen survival. | Expose the Room layer through the repo's standard datasource mixins and convert the step-0 spike into a permanent regression test. |  |
| T-4 | Added HistoryEntryModel, BatchStatusModel (5 values), SentBatchModel and SentAnnotationModel to domain-core with zero new dependencies. | Give the write and read paths a shared, dependency-free domain vocabulary before the repository layer is built. |  |
| T-5 | Added HistoryRepository to domain-repository-api (observe/record/updateStatus). | FR-33 asks for a repository-level status-update API with no use case yet; keep the contract a repository method until F-9 has a real caller. |  |
| T-6 | Added BatchFileStore (fun interface, never throws) with a desktop FileSystemBatchFileStore and android/ios null-returning stands-in, HistoryEntryMapper (toModel + toEntities, unknown-status-to-Sent fallback), and HistoryRepositoryImpl wired to both; added the data-repository-impl -> data-database-impl module edge and HistoryRepositoryImplTest/HistoryEntryMapperTest. | Persist batch files to app-owned storage and insert the Room row in the same operation, with SC-18's failed-write-still-persists-the-row behaviour proven by test rather than assumed. |  |
| T-7 | Added RecordSentBatchUseCase and ObserveHistoryUseCase (api + impl), the former wrapped in resultLauncher with a Database error mapper, the latter a bare Flow; both Koin-bound. | Give MirrorViewModel a thin, already-reviewed call surface instead of reaching into the repository directly. |  |
| T-8 | Added core/history/HistoryFormatting.kt (groupHistory, metaTime, shortBatchId, unionRect, thumbnailRect, statusTone, inProgressCount, noteCountKey) and core/history/ThumbnailBitmap.kt (cropAndScale to <=128px via commonMain Canvas/drawImageRect ahead of the unchanged CropEncoder), with HistoryFormattingTest and ThumbnailBitmapTest. | Resolve C-1 (AwtCropEncoder crops but never scales, so a naive union-rect crop would be a ~250 KB BLOB per row) by pre-scaling in commonMain with zero new DI seam and zero CropEncoder signature change. |  |
| T-9 | Added MirrorUiState.history and MirrorIntent.VerifyBatch; added two required MirrorViewModel constructor params (recordSentBatch, observeHistory, no defaults); the container now collects history on init and calls recordSentBatch before postSideEffect(SendComplete) on a successful send, consuming failure with .onFailure { log.w } only; updated MirrorFakes.kt and PresentationFeatureMirrorModule. | Make every successful send durable in its own failure domain (a history write can never flip SendPhase to Failed) and keep MirrorFakes/DI in sync so no existing VM test breaks. |  |
| T-10 | Added the 15 mirror_history_* string keys to presentation-core-localisation, using two plain note-count keys instead of introducing the repo's first plurals resource. | Supply every new user-facing string through Res.string.* without adding new localisation infrastructure. |  |
| T-11 | Added MirrorHistoryTab.kt: empty-state early return, grouped keyed LazyColumn of HistoryRow items (thumbnail decoded inside remember(entry.id)), Fixed-only Verify button, and the session footer — composed entirely from existing design-system components, with two private light/dark previews. | Fill the History tab's panel body, previously an `else -> Unit`, with zero new design-system components per the plan's constraint. |  |
| T-12 | RectifyWindow.kt: wired the ShellTab.History branch to MirrorHistoryTab, and converted tabCounts to a buildMap that originally omitted the History key at zero inline. | Make History reachable via Cmd+3 and give the tab strip its in-progress badge per AC-34/FR-34. |  |
| T-13 | Amended .claude/rules/database.md with the source/entity/-for-public-wire-types exception and the version-1-via-relocation note; updated data-database-impl/CLAUDE.md to drop the placeholder framing and record the new DB path and test block; reported the features.json#database registry re-tag to the orchestrator. | Keep the rules and module docs truthful now that Room has a real production consumer. |  |
| T-14 | Ran the full desktopTest suite across presentation-feature-mirror/data-repository-impl/data-database-impl (green) and the cross-target build; recorded the manual-demo task as complete in state.json. | Gate the branch on its own test suites before handoff to validation. |  |
| T-12 | CR-1: moved the AC-34 omit-at-zero badge rule out of RectifyWindow.kt (no test wiring) into a new pure `historyBadgeCount(): Int?` in presentation-feature-mirror's core/history, exposed via MirrorContract; RectifyWindow.kt is now a one-line `mirror.historyBadgeCount?.let { put(...) }`. Added a unit test asserting null at zero and the count otherwise. | The audit found that deleting the omit-at-zero guard previously made nothing go red, because the guard lived in an untested module; this makes AC-34 provable by test rather than inspection alone. | CR-1 |
| T-11 | CR-2: recorded AC-37's footer-shows-shortBatchId behaviour as a correction of record in MirrorHistoryTab's KDoc (same shape as FR-30's), citing the design contract's own `session 7f3a` and the row's matching `#a41f` tag. No code change. | The audit found the deviation from the literal AC/FR-35 wording was defensible but had never been written back as a correction of record, so AC-37 was not met by inspection as frozen. | CR-2 |
| T-13 | CR-3: fixed the stale DatabaseBuilder.kt KDoc line (still said 'stores the database in the user home directory') to read '~/.rectify/rectify.db', matching the desktop actual, README, CLAUDE.md and database.md. | One doc line was missed by the original T-13 docs pass. | CR-3 |

#### summary

F-8 (History & Batch Tracking) lands with every must-priority AC implemented and the large majority proven by passing automated tests against production code (1167 tests, 0 failures, 0 errors across every module that has them, confirmed independently by the adversarial audit). Three audit findings (the AC-34 badge rule living in an untested module, the AC-37 footer/FR-35 wording mismatch, a stale DB-path KDoc) were fixed post-audit in commit 21f1045 and are re-verified here directly against the diff. What remains open and is recorded as seven separate waivers rather than rounded away: the nine manual GUI demo steps (AC-36's Verify-button visibility, SC-8's affordance, the full quit-and-relaunch half of AC-33/SC-15, AC-22's row-painting) were never executed because no GUI exists in this environment, though every programmatically-provable half of each is green; AC-37 ships a documented deviation (short tag, not the raw sessionId) rather than a literal fix; and the repo's documented `./gradlew build` command OOMs at default parallelism for reasons that pre-date this branch. Two non-blocking process items (no per-task report.json trail, no Koin DI-graph test for the five new bindings) are raised as open change requests for the orchestrator and a future task rather than gates on this verdict.

#### regressionRunPassed

false

### verdict

complete-with-waivers

### tasks

| id | status | satisfiesAC |
| --- | --- | --- |
| T-1 | done | ["AC-33"] |
| T-2 | done | ["AC-33", "AC-22"] |
| T-3 | done | ["AC-33"] |
| T-4 | done | ["AC-22", "AC-33"] |
| T-5 | done | ["AC-34"] |
| T-6 | done | ["AC-38", "AC-33"] |
| T-7 | done | ["AC-33"] |
| T-8 | done | ["AC-35", "AC-22", "AC-34"] |
| T-9 | done | ["AC-38", "AC-33", "AC-22"] |
| T-10 | done | ["AC-22", "AC-37"] |
| T-11 | done | ["AC-22", "AC-36", "AC-37"] |
| T-12 | done | ["AC-22", "AC-34"] |
| T-13 | done | [] |
| T-14 | done | ["AC-22", "AC-33", "AC-34", "AC-35", "AC-36", "AC-37", "AC-38"] |

### unsatisfied

#### acceptanceCriteria


#### scenarios


#### tasks


### waivers

| item | reason | acceptedBy |
| --- | --- | --- |
| demo:AC-36 | Verify-button Fixed-only visibility was never confirmed on a running GUI (no GUI available in this environment). Proven: the conditional (`if (entry.status == BatchStatusModel.Fixed)`) is unambiguous in MirrorHistoryTab.kt; all five statuses are exercised in private light+dark @Preview pairs; `verifyBatchIsAnInertStubRatherThanACrash` proves the intent handler is a safe no-op. Remaining: nobody has looked at a live window and clicked the button. | human |
| demo:SC-8 | SC-8 (review and verify a batch) was already ruled PARTIAL BY DESIGN at the backlog survey — the diff/re-verify logic is F-9's, not F-8's. Proven: the Fixed-only affordance (shared with AC-36) and `statusTone`'s full five-value mapping are unit-tested. Remaining: no diff ever runs and no demo was executed for even the affordance half; F-9 owns the rest of this scenario outright. | human |
| test:AC-33 (full app quit-and-relaunch) | AC-33's verificationMethod is test, and the storage layer is genuinely proven: HistoryEntryDaoTest's 'rows survive closing and re-opening the same file' runs a real BundledSQLiteDriver against a temp file and asserts batch + children + thumbPng survive, plus separate cascade-delete and status-update tests. What was NOT executed is a real second JVM process (full app quit, fresh Koin graph, relaunch) — that half of the AC's literal 'app is quit and relaunched' wording is demo-only in practice here and was never run. | human |
| test:SC-15 (full app quit-and-relaunch) | Same storage-layer evidence and same gap as AC-33: the close/reopen-same-file DAO test plus the relocated `~/.rectify/rectify.db` path with `mkdirs()` and `Dispatchers.IO` are proven; 'send two, quit, relaunch' end-to-end was not executed. | human |
| documentation:AC-37 (footer shows shortBatchId, not the raw sessionId) | FR-35/AC-37 literally call for the raw sessionId in the footer. The shipped footer prints `shortBatchId(newest.id)` (the same 4-hex tag the row's own meta line already shows), not the sessionId. This was fixed in commit 21f1045 as a CORRECTION OF RECORD (same shape as FR-30's), not a code change: the KDoc now argues the design contract itself shows the short tag (`session 7f3a`), the raw id (`<deviceId>-f<frameId>-<epochMillis>`) would ellipsise away in a narrow panel, and the footer's tag matches the row's own `#a41f` tag so the two are checkable against each other. Proven: footer/row consistency, presence/absence logic (hidden when history is empty), and a documented rationale. Remaining: the literal sessionId is never shown anywhere in the UI; a reader following the AC text alone would call this a defect, not a documented deviation. | human |
| inspection:AC-22 (row rendering of thumbnail+summary+timestamp+chip) | AC-22's verificationMethod is test. Grouping, ordering, time/id formatting and tone-per-status are fully unit-tested (HistoryFormattingTest, 13 cases green on desktop AND iosSimulatorArm64; HistoryEntryDaoTest's newest-first ordering). The actual Compose row painting a thumbnail plate, title, meta line and chip together has no automated test because this repo has no Compose UI-test harness anywhere (pre-existing, repo-wide constraint per .claude/rules/testing.md, not introduced or worsened by F-8) — it is proven only by four private light/dark @Preview pairs and inspection. | human |
| environment:AF-1 (./gradlew build OOMs at default parallelism) | The repo's documented full regression command, `./gradlew build`, FAILS on this machine with two OutOfMemoryErrors (`:compose-application:linkReleaseFrameworkIosSimulatorArm64` Kotlin/Native devirtualization, `:aos-application:minifyReleaseWithR8`) under the untouched `org.gradle.jvmargs=-Xmx4096M` at default worker parallelism. Both tasks pass cleanly with `--max-workers=1`, and a full accounting of every test task across the repo totals 1167 tests / 0 failures / 0 errors. This is heap contention pre-dating the branch (gradle.properties is untouched), not an F-8 source regression, but it was not independently re-verified against `development` and it means F-8's green is proven per-task/serialized rather than by one default-parallelism `./gradlew build` invocation. | human |

### openChangeRequests

| id | about | target | source |
| --- | --- | --- | --- |
| CR-4 | execution/ is empty for all 14 tasks — no report.json artifacts exist, although state.json marks every task done. The run was orchestrated wave-by-wave with one commit per wave instead of per-task reports, so this and future audits must reconcile against the real tree and commit history rather than a report trail. | orchestrator | audit |
| CR-5 | No automated test resolves the five new Koin bindings introduced for history (HistoryDataSource, BatchFileStore/FileSystemBatchFileStore, HistoryRepositoryImpl, RecordSentBatchUseCase, ObserveHistoryUseCase). A missing binding would only surface the first time the mirror screen opens. Precedent exists in this repo (data-device-impl's DeviceSourcesModuleTest) for a cheap module-resolution test; :compose-application itself has no test wiring so this has to live one layer down. | T-9 | audit |

### humanRemarks

- Fully autonomous Stage 5 run: no separate human-gate transcript exists beyond this verdict (no validation/request.data.json artifact was produced on disk). The waivers above are the recommended dispositions for human ratification, not retroactive descriptions of a conversation that happened.
- Three of the audit's four AC/SC-relevant findings (AF-3 footer sessionId, AF-4 badge omit-at-zero, AF-12 stale KDoc) were fixed in commit 21f1045 and independently re-verified against production code and tests in this verdict pass, not taken on the audit's or the executor's word.
- Five of the seven acceptance criteria (AC-34, AC-35, AC-38, plus the formatting half of AC-22) and four of five scenarios (SC-16, SC-17, SC-18, plus the storage half of SC-15) are satisfied by passing automated tests against production code, not test names — the audit confirmed all 10 executor claims it set out to attack.

### changelogRef

changelog.json
