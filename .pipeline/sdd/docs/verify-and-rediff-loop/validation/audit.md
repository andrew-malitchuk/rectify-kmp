# verify-and-rediff-loop — audit

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | audit |
| status | done |
| producedBy | sdd-build-validation-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-19T01:04:37Z |
| updatedAt | 2026-10-19T01:04:37Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-19T01:04:37Z

## Provenance

### selfHash

ce10dd48619f8c6a2d1fcff6b153af647c4c6fd5051235dc446b4e51526b6548

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | bdc42d0542417163c43c47fef822b6ae648680b7b0e8fd68c8fcce4acf14e28f |

## Data

### iteration

1

### findings

| id | severity | about | ref | issue | evidence | recommendedTarget |
| --- | --- | --- | --- | --- | --- | --- |
| AF-1 | warn | task | T-17 | The manual end-to-end GUI demo (T-17 TAC-3) was NOT executed, and MET-3 (<5% false positives) is unverified in v1. Both are honestly recorded rather than implied-satisfied — but the only record is the wave-9 git commit message (3489090). This spec has no execution/<task-id>/report.json directory at all, so a reader of plan.json/backlog.json/state.json alone sees T-17 'done' with no visible waiver. AC-23's visual half (re-capture against a real device, 296x640 letterboxing, verdict-coloured rects), AC-39's rendered button and NFR-9's light/dark parity rest on that unexecuted demo. | git show 3489090 commit body: '... MET-3's <5% false-positive target is NOT verified (no golden corpus, no UI-test harness ...) and the end-to-end GUI demo was not executed.' \| `find .pipeline/sdd/docs/verify-and-rediff-loop -name report.json` returns nothing; sibling specs (measure-mode, drag-annotate-frozen-frame) all have execution/T-*/report.json. | T-17 |
| AF-2 | warn | task | T-11 | Documentation inconsistency, not a missing feature. backlog.json T-11 still lists StatusLine's four-step strip (Sent -> Agent fixed -> Recaptured -> Verified n/N) as a deliverable, and the design's AGENT CHANGES file list is in the same family. Both were deliberately cut for want of a data source (nothing in this app knows which files the agent touched). The cut is documented ONLY in MirrorVerifyStage.kt's KDoc; backlog.json was never amended, so a verifier reading the backlog alone reports a gap. | backlog.json T-11 description: "StatusLine's four-step strip (Sent -> Agent fixed [static, countless] -> Recaptured -> Verified n/N)" vs MirrorVerifyStage.kt:119-123: "Cut from the design on purpose, both for want of a data source rather than for want of time: the four-step ... strip and the AGENT CHANGES file list." The shipped StatusLine at MirrorVerifyStage.kt:262 is the THREE-step progress line (Loading batch / Recapturing / Diffing regions). | T-11 |
| AF-3 | warn | ac | AC-43 | Two of the three updateBatchStatus call sites drop the returned Optional entirely — no .onFailure, no log, no UI signal. A failed 'Verified' write (the exact write AC-43 names) is invisible: the row silently keeps its old status and the panel still shows every row Fixed. The third call site (the FollowUpSent write) does log. Inconsistent pattern within one file. | MirrorViewModel.kt:615 `updateBatchStatus(batchId, BatchStatusModel.Verified)` and :833 `updateBatchStatus(view.batchId, BatchStatusModel.Verified)` — bare calls. Contrast :1454 `updateBatchStatus(followUpFor, BatchStatusModel.FollowUpSent).onFailure { error -> log.w(error) { ... } }`. | T-14 |
| AF-4 | warn | registry | bugs.json#PP-TD-10 | Registry is stale: PP-TD-10 ('FakeFrameBitmap.readPixels is implemented as = Unit, so it never fills the caller buffer') still carries "status": "open" although T-8 fixed it in wave 1 and FakeFrameBitmapTest now proves the fill. Phantom open bug. | .pipeline/sdd/docs/registry/bugs.json PP-TD-10 status=open vs MirrorFakes.kt:201-215 — a real loop filling `buffer[bufferOffset + row * stride + column] = pixel(startX + column, startY + row)`; FakeFrameBitmapTest.kt 5 tests, all green in the regression run. | registry |
| AF-5 | warn | registry | components.json / features.json | No registry entry exists for anything this spec shipped. components.json (95 entries) names none of MirrorVerifyStage, VerifyVerdict, RegionDiff, RegionPixels, VerifyThresholds, BeforeAfterFormat, VerifyLabels, FollowUp, BatchFileReader, GetSentBatchUseCase or UpdateBatchStatusUseCase. features.json (17 entries) has no verify-and-rediff-loop / F-9 entry — and no resolve-and-send / F-7 entry either, so the lag predates this spec. Missing entries, no phantoms. | python filter over registry/components.json for ('Verify','RegionDiff','FollowUp','BatchFileReader','GetSentBatch','UpdateBatchStatus','BeforeAfter') returns []; features.json ids = ['app-shell-window-layout','android-device-discovery-mirroring','drag-annotate-frozen-frame','error-handling-and-recovery','splash','onboarding','home','settings','about','styleguide-showcase','navigation','localisation','preferences','database','theming','error-propagation','history-batch-tracking']. | registry |
| AF-6 | info | task | T-1 | data-database-impl's new HistoryEntryDaoFindByIdTest lives in desktopTest only, and that module's iosSimulatorArm64Test task is SKIPPED, so NFR-7's single-row read is never exercised on iOS. Pre-existing module shape (Room/KSP), honestly reported in the wave-9 commit message, not introduced here. | regression log: '> Task :data-database-impl:iosSimulatorArm64Test SKIPPED'; test file path data-database-impl/src/desktopTest/kotlin/data/database/impl/core/dao/HistoryEntryDaoFindByIdTest.kt; 11 desktop tests, 0 ios tests for that module. | T-1 |
| AF-7 | info | diff | MirrorViewModel.releaseVerify | Claim nuance, not a defect. releaseVerify() clears FOUR fields, not five: currentVerifyFrames, verifyAfterHeld, verifyAfterHierarchy, pendingFollowUpBatchId. The 'fifth verify-scoped field' is the reduced MirrorUiState.verify, which is cleared by a separate reduce in exitVerify and sendFollowUp and is DELIBERATELY not cleared by abortVerify (it must hold the error view). The wave-5 leak cannot recur: all four teardown paths — verifyBatch restart (:526), abortVerify (:638), sendFollowUp (:768), exitVerify (:855) — route through the one helper. markAllVerified intentionally retains the pair because the user stays on the stage. | MirrorViewModel.kt:860-866 releaseVerify body (four assignments); call sites at :526, :638, :768, :855. Tests anAbortAfterTheDiffStartedLeavesNoPixelsBehind, exitVerifyClearsEveryVerifyFieldAndReturnsToTheLiveStage, verifyingAnotherBatchMidRunKeepsOnlyTheSecondRun. | T-15 |
| AF-8 | info | diff | HistoryRepositoryImpl.get | The SentBatchModel handed to the verify run carries entry.thumbPng (a DB blob) and the full hierarchyXml string, neither of which the verify path ever reads — MirrorViewModel uses frameBytes, hierarchy, annotations, deviceLabel and sentAtMs only. Needless retention per run; correctness unaffected. AC-45 itself holds: cropBytes is explicitly null and the only file reads are framePath and hierarchyPath. | HistoryRepositoryImpl.kt get(): `thumbPng = entry.thumbPng`, `hierarchyXml = hierarchyXml`; grep of MirrorViewModel's verify path shows neither field read. cropBytes = null carries the comment 'Never read: the full frame plus this rect dominates a crop'. | T-3 |
| AF-9 | info | diff | BatchFileReader | Security note, theoretical only. BatchFileReader.read(path) takes an arbitrary absolute path string straight out of the history_entry row with no confinement to ~/.rectify/history. The paths are app-written, the bytes only feed a PNG decode and an XML parse, and the follow-up envelope carries the FRESH dump (verifyAfterHierarchy) rather than the stored XML, so there is no exfiltration path from a tampered row. No new network surface, no secret, and the reader never throws. | BatchFileReader.kt KDoc 'Not a general file-read API: the only paths ever passed are the absolute ones this app wrote itself'; FileSystemBatchFileReader.kt uses an isFile check plus a catch-all, returning null. | T-2 |
| AF-10 | info | diff | MirrorVerifyStage.kt | Design-token judgement call, recorded for the human. MirrorVerifyStage.kt declares four raw .dp layout constants as named private vals (FrameColumnWidth 296.dp, FrameColumnMaxHeight 640.dp, PanelWidth 300.dp, CaptionDotSize 6.dp). .claude/rules/design-tokens.md forbids a raw .dp 'at a call site'; these are not call sites and match the module's own established precedent, so T-11 TAC-7 reads as satisfied. Zero literal Color(0x...) and zero inline TextStyle were added anywhere in the diff. | MirrorVerifyStage.kt:2485-2490 (diff positions) vs pre-existing baseline MirrorAnnotationStage.kt:146 `private val GutterRowHeight: Dp = 54.dp`, :159 `MeasureCardWidth: Dp = 228.dp`, MirrorHistoryTab.kt:234 `PreviewPanelHeight: Dp = 420.dp` on development. `git diff development...HEAD \| grep '^+' \| grep -E 'Color\(0x\|TextStyle\('` returns nothing. | T-11 |
| AF-11 | info | diff | pre-existing tests edited | All five edits to pre-existing tests are legitimate maintenance, none is a test weakened to fit the code. (1) MirrorUiStateSendFieldTest re-baselined to 'exactly one new field named verify' with 'history' appended to the prior baseline — the invariant it guards ('one considered field at a time, nothing renamed') is intact and the removed-field assertion is unchanged. (2) verifyBatchIsAnInertStubRatherThanACrash was INVERTED to verifyBatchIsNoLongerAnInertStub and made STRICTER: assertNotEquals plus a specific assertEquals(VerifyError.NoLiveSession, ...). (3)(4) FindingsTest / MeasureThresholdUnitsTest changed one import because FindingKind moved core/measure -> source/mirror, which the package-layout rule requires now that MeasureReadout.findingKind made it public. (5) HistoryRepositoryImplTest absorbed the new constructor arity with a `BatchFileReader { null }` named noFiles. | git diff development...HEAD over the five files; MirrorUiStateSendFieldTest.kt:45 baseline now ends with "history", assertion at :58 is setOf("verify"); MirrorViewModelResolveAndSendTest.kt:533-534. | T-16 |

### gaps

#### unsatisfiedAC


#### unsatisfiedSC


#### incompleteTasks


### unresolvedFlags

| task | flag | reason |
| --- | --- | --- |
| T-17 | needsHumanReview | Manual end-to-end GUI demo not executed and MET-3 not verified. Waived honestly, but the waiver lives only in the wave-9 commit message (3489090) — not in any pipeline artifact. state.json carries no flag of its own; every one of the 17 tasks is bare 'done' with no failed/blocked/needsHumanReview field anywhere in the file, and no execution/<task-id>/report.json exists for this spec, so there are no executor claims on disk to cross-examine. Human must decide whether the commit-message waiver is sufficient evidence for the GUI-only half of AC-23 / AC-39 / NFR-9. |

### regressionRun

#### command

./gradlew --max-workers=1 --no-build-cache :data-database-impl:build :data-repository-impl:build :domain-core:build :domain-repository-api:build :domain-usecase-api:build :domain-usecase-impl:build :presentation-feature-mirror:build :presentation-core-localisation:build :presentation-core-ui:build :compose-application:build

#### passed

true

#### output

```
FIRST RUN (plain :build) was rejected as evidence: every test task came back UP-TO-DATE ('1229 actionable tasks: 84 executed, 1145 up-to-date'), so nothing actually ran. SECOND RUN (test-results cleared) returned desktopTest FROM-CACHE. THIRD RUN below deleted */build/test-results and added --no-build-cache, which forced real execution.

> Task :data-database-impl:desktopTest
> Task :data-database-impl:iosSimulatorArm64Test SKIPPED
> Task :data-repository-impl:desktopTest
> Task :data-repository-impl:iosSimulatorArm64Test
> Task :domain-core:desktopTest
> Task :domain-core:iosSimulatorArm64Test
> Task :domain-repository-api:desktopTest NO-SOURCE
> Task :domain-repository-api:iosSimulatorArm64Test SKIPPED
> Task :domain-usecase-api:desktopTest NO-SOURCE
> Task :domain-usecase-api:iosSimulatorArm64Test SKIPPED
> Task :domain-usecase-impl:desktopTest
> Task :domain-usecase-impl:iosSimulatorArm64Test
> Task :presentation-feature-mirror:desktopTest
> Task :presentation-feature-mirror:iosSimulatorArm64Test
> Task :presentation-core-localisation:desktopTest NO-SOURCE
> Task :presentation-core-localisation:iosSimulatorArm64Test SKIPPED
> Task :presentation-core-ui:desktopTest
> Task :presentation-core-ui:iosSimulatorArm64Test
> Task :compose-application:desktopTest NO-SOURCE
> Task :compose-application:iosSimulatorArm64Test SKIPPED
> Task :compose-application:build UP-TO-DATE

BUILD SUCCESSFUL in 8s
1225 actionable tasks: 101 executed, 1124 up-to-date
EXIT=0

COUNTS from build/test-results/**/*.xml after the forced run:
  data-database-impl    desktopTest            tests=11  failures=0 errors=0 skipped=0
  data-repository-impl  desktopTest            tests=86  failures=0 errors=0 skipped=0
  data-repository-impl  iosSimulatorArm64Test  tests=75  failures=0 errors=0 skipped=0
  domain-core           desktopTest            tests=1   failures=0 errors=0 skipped=0
  domain-core           iosSimulatorArm64Test  tests=1   failures=0 errors=0 skipped=0
  domain-usecase-impl   desktopTest            tests=5   failures=0 errors=0 skipped=0
  domain-usecase-impl   iosSimulatorArm64Test  tests=5   failures=0 errors=0 skipped=0
  presentation-core-ui  desktopTest            tests=29  failures=0 errors=0 skipped=0
  presentation-core-ui  iosSimulatorArm64Test  tests=29  failures=0 errors=0 skipped=0
  presentation-feature-mirror desktopTest      tests=470 failures=0 errors=0 skipped=0
  presentation-feature-mirror iosSimulatorArm64Test tests=458 failures=0 errors=0 skipped=0
  TOTAL 1170 tests, 0 failures, 0 errors, 0 skipped.

The wave-9 commit claimed '572 desktop tests across four suites and 568 iosSimulatorArm64 tests across five modules'. Independently reproduced: the four gated desktop suites are 470+86+11+5 = 572, and the five ios modules are 458+75+1+5+29 = 568. Both counts are exact. data-database-impl's ios task is SKIPPED (see AF-6).
```

#### newFailures


### aggregateDiffFindings

| id | category | location | issue |
| --- | --- | --- | --- |
| AD-1 | inconsistent-pattern | presentation-feature-mirror/.../MirrorViewModel.kt:615, :833 vs :1454 | updateBatchStatus's Optional is logged on one call site and silently dropped on the other two, so AC-43's Verified write can fail with no trace. See AF-3. |
| AD-2 | performance | data-repository-impl/.../HistoryRepositoryImpl.kt get() | The verify read hydrates thumbPng (DB blob) and the full hierarchyXml string into SentBatchModel; the only consumer reads neither. See AF-8. Pixel-heavy work itself is correctly fenced: decode, projection, per-annotation measure, regionPixels and changedRatio all run inside withContext(Dispatchers.Default) (MirrorViewModel.kt:590), satisfying NFR-15's thread discipline. |
| AD-3 | security | data-repository-impl/.../BatchFileReader.kt + FileSystemBatchFileReader.kt | Unconfined path read from a DB-stored string; theoretical only, never throws, no exfiltration path. See AF-9. Android/iOS stand-ins return null, so no file surface is added on those targets. |
| AD-4 | dead-code | presentation-feature-mirror/.../core/verify/ | None found. Every internal function in core/verify has live production call sites (changedRatio, verdictOf, followUpAnnotations, beforeAfterLine, the three *BeforeAfterLabel helpers, frameColumnLabel, verifyMarks, verifySteps, verifyErrorTitleKey, verifyErrorBodyKey). No TODO/FIXME/HACK and no println was added anywhere in the cumulative diff. |
| AD-5 | inconsistent-pattern | cross-cutting convention sweep | No violations found. Database: @Database version = 1, Migrations object still empty, @Transaction on the new findById. Domain layer: both new use cases return Result<T>/Optional through resultLauncher with Failure.Technical::Database, no raw runCatching, no Flow<Result<T>>. Mappers: HierarchySnapshotMapper degrades to no nodes rather than throwing; BatchFileReader/BatchFileStore never throw. Koin: initKoin() untouched; new bindings follow singleOf(...) bind ...::class and the existing expect/actual provider split. Orbit: the whole run is the one coroutine intent {} returns, cancelled via verifyJob — the module's own dumpJob precedent. Package layout: FindingKind moved core/measure -> source/mirror precisely because it became public. Localisation: 22 new strings, zero AAPT-style apostrophe escapes. Preview convention: four @Preview functions, light/dark pair for Ready and for Error. Testing: new mirror tests are plain-word camelCase per that module's CLAUDE.md, the data/domain modules keep their backtick style, and no test name contains a comma or a colon (the iosArm64 compile trap) — confirmed by the green iosSimulatorArm64 runs. |

### registryConsistency

#### consistent

false

#### phantomEntries

- bugs.json#PP-TD-10 — status "open" but fixed in wave 1 (MirrorFakes.kt readPixels now fills the buffer; FakeFrameBitmapTest green). Delta: set status to resolved/fixed with fixedIn=verify-and-rediff-loop, T-8.

#### missingEntries

- features.json — no entry for verify-and-rediff-loop (F-9). Note the registry also has no resolve-and-send (F-7) entry, so the lag is not new.
- components.json — MirrorVerifyStage (presentation-feature-mirror/.../source/mirror/MirrorVerifyStage.kt)
- components.json — core/verify pure core: VerifyVerdict.verdictOf, RegionDiff.changedRatio, VerifyThresholds, BeforeAfterFormat, VerifyLabels, FollowUp.followUpAnnotations, RegionPixels.regionPixels
- components.json — BatchFileReader / FileSystemBatchFileReader (data-repository-impl)
- components.json — GetSentBatchUseCase + UpdateBatchStatusUseCase (domain-usecase-api/impl)
- components.json — HistoryRepository.get + HistoryEntryDao.findById / HistoryDataSource.getBatch

### traceability

| ref | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-23 | test | true | Logic half proven. MirrorViewModelVerifyTest (33 behavioural tests, all green in the forced run) walks the seven-step run; the pixel half is RegionDiffTest (9) and the read path is HistoryRepositoryGetTest + FileSystemBatchFileReaderTest + HistoryEntryDaoFindByIdTest. CAVEAT: 're-captures the live screen' is only ever exercised through fakes (FakeFrameBitmap, scripted dump); no real adb capture was run — that is the unexecuted demo of AF-1. |
| AC-24 | test | true | VerifyVerdict.kt:38-45 is literally `matched && afterFinding == null && (regionPx < MIN_REGION_PIXELS \|\| changedRatio > REGION_CHANGE_RATIO)` — exactly the AMENDED change-evidence wording, with MIN_REGION_PIXELS short-circuiting only the pixel conjunct. VerifyVerdictTest (8) covers both ceilings including the tiny-region-with-a-finding case; FollowUpRenumberTest (4) covers the one-click re-stage. |
| AC-39 | inspection | true | The BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:177-185 is deleted in the diff; the AppButton now renders unconditionally with `enabled = canVerify(entry)` and HistoryFormatting.kt:178 is `canVerify(entry) = entry.framePath != null`. No status reference survives in the trailing slot. HistoryFormattingTest sweeps canVerify across every status. The rendered button itself is GUI-only (AF-1). |
| AC-40 | test | true | MirrorViewModel.kt:552-558 raises VerifyError.SizeMismatch before any per-annotation work and before the dump; test aFrameSizeDisagreementAbortsBeforeAnyPerAnnotationWork asserts empty rows and zero status writes. |
| AC-41 | test | true | Both halves distinct in code and in tests. Unmatched annotation -> StillOff via verdictOf's first conjunct (VerifyVerdictTest anUnmatchedNodeIsStillOffEvenWithNoFindingAndFullyChangedPixels). Non-empty dump projecting to zero candidates -> MirrorViewModel.kt:568-575 VerifyError.HierarchyUnusable with zero rows (aHierarchyInAnotherCoordinateSpaceIsUnusable), while an EMPTY dump is not an abort (anEmptyHierarchyStillProducesOneStillOffRowPerAnnotation). |
| AC-42 | test | true | sendFollowUp (MirrorViewModel.kt:747-800): currentFrozenHeld = verifyAfterHeld, followUpAnnotations renumbers 1-based, promptOverride = null, and `state.copy(frozen = frozen, verify = null, ...)` is ONE reduction. FollowUpSent is written at :1454 only inside onSuccess, on the original batch id. Tests: aFollowUpReStagesOnlyTheStillOffRowsRenumberedFromOne, aFollowUpTearsTheVerifyStageDownInTheSameReduction, aSuccessfulFollowUpSendsTheAfterFrameAndMarksTheOriginalBatch, aFollowUpWhosePublishFailsWritesNoStatus, aPlainSendWithNoFollowUpPendingWritesNoStatus. |
| AC-43 | test | true | Automatic path MirrorViewModel.kt:614-616 guarded on rows.isNotEmpty() && all Fixed; manual path markAllVerified cancels verifyJob first and refuses a rowless or errored view. Tests: aRunWhereEveryRowIsFixedWritesVerifiedExactlyOnce, aMixedRunWritesNoStatus, aZeroRowRunWritesNoStatus, markAllVerifiedForcesEveryRowFixedAndWritesVerifiedOnce, markAllVerifiedMidRunCancelsItSoNoLateVerdictLands, markAllVerifiedOnARowlessViewWritesNothing. Caveat AF-3: the write's failure is not logged. |
| AC-44 | test | true | sessionDeviceId is re-read after every suspension point (:550, :565, :608) and a failed dump reads as Disconnected; abortVerify forces empty rows and writes no status, leaving the batch's stored status untouched and the state retryable. Tests: aDeviceThatGoesAwayMidRunIsDisconnectedAndWritesNoStatus, aSwapToAnotherDeviceMidRunIsDisconnectedAndWritesNoStatus, aDumpThatFailsReadsAsDisconnected. |
| AC-45 | inspection | true | HistoryRepositoryImpl.get reads back exactly two paths — entry.framePath and entry.hierarchyPath; annotations are built with `cropBytes = null` and an explicit comment. No crop-N.png read exists anywhere on the diff path. RegionPixels.regionPixels reads the full-res frame with the stored rects, clamped. Note AF-8: the DB-resident thumbPng blob is still carried in the model (not a file read). |
| NFR-7 | inspection | true | AppDatabase.kt:18 `version = 1` unchanged; Migrations.kt is still `internal object Migrations` with no body; the only DAO addition is a read (@Transaction @Query findById). No schema change, so no migration is owed. Persistence across restart is the inherited F-8 manual ceiling (AF-1). |
| NFR-9 | inspection | true | No new colour token is introduced — the stage reuses Theme.color.sentFixed / warnStillOff through the existing VerificationRow, so light/dark parity holds by construction. MirrorVerifyStage ships four @Preview functions (Ready light/dark, Error light/dark). The actual rendered parity is GUI-only (AF-1). |
| NFR-15 | test | true | VerifyPhase advances Loading -> Recapturing -> Diffing -> Ready with a reduce at each boundary (:559-561, :583-584, :610-611); test phasesAdvanceInOrder asserts the sequence. Decode, the whole per-annotation diff loop and the send-path encode all sit inside withContext(Dispatchers.Default). |
| NFR-16 | test | true | Only two terminal writes exist and both are gated on a completed, rowful, all-Fixed run; every abort routes through abortVerify, which cannot write. Proven by aMixedRunWritesNoStatus, aZeroRowRunWritesNoStatus and the three disconnect tests. |
| NFR-17 | inspection | true | All three constants live in one file, each with a KDoc naming its ceiling and its manual tuning method. Independently grepped: NO test asserts a threshold by literal value — VerifyVerdictTest and MirrorViewModelVerifyTest import MIN_REGION_PIXELS / REGION_CHANGE_RATIO only to CONSTRUCT inputs (MIN_REGION_PIXELS * 10, MIN_REGION_PIXELS - 1, REGION_CHANGE_RATIO * 2f, REGION_CHANGE_RATIO / 2f), and RegionDiffTest uses CHANNEL_NOISE_FLOOR and CHANNEL_NOISE_FLOOR + 1 the same way. bugs.json#PP-TD-4/PP-TD-5 are not repeated. MET-3 itself stays unverified-in-v1 and is recorded as such (AF-1). |
| AS-1 (amended) | test | true | runVerify reads currentHeld.value ONCE at :539 and aborts with VerifyError.NoLiveSession at :541 before getSentBatch is ever called. Test noLiveSessionAbortsBeforeTheBatchIsEvenRead asserts the empty rows and that the use case was never invoked; the re-baselined verifyBatchIsNoLongerAnInertStub asserts the same error from the other direction. |
| SC-8 | test | true | Covered end to end by MirrorViewModelVerifyTest's happy-path tests plus VerifyStageDerivationTest (Verify outranks Frozen and Streaming). The visual half of the walkthrough is GUI-only (AF-1). |
| SC-9 | test | true | Five follow-up tests, including the CR-B pair aFollowUpCarriesTheRunsOwnAfterHierarchyAndNotTheOneItReplaced and aFollowUpAfterARunWhoseDumpWasEmptyStillSendsWithNoHierarchy. |
| SC-19 | test | true | The status gate is gone (AC-39) and HistoryFormattingTest exercises canVerify over every BatchStatusModel including Sent. |
| SC-20 | test | true | markAllVerified's three tests; the override forces every row Fixed, writes Verified exactly once and deliberately stays on the stage. |
| SC-21 | test | true | aDeviceThatGoesAwayMidRunIsDisconnectedAndWritesNoStatus plus the swap and failed-dump variants. |
| SC-22 | test | true | aFrameSizeDisagreementAbortsBeforeAnyPerAnnotationWork. |
| SC-23 | test | true | noLiveSessionAbortsBeforeTheBatchIsEvenRead. |
| SC-24 | test | true | aHierarchyInAnotherCoordinateSpaceIsUnusable, paired with anEmptyHierarchyStillProducesOneStillOffRowPerAnnotation to prove the two cases stay distinct. |
