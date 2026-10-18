# verify-and-rediff-loop — plan

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | plan |
| status | done |
| producedBy | sdd-build-plan-refine |
| schemaVersion | 1 |
| createdAt | 2026-10-18T22:15:15Z |
| updatedAt | 2026-10-18T22:15:15Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T22:15:15Z

## Provenance

### selfHash

1309a704111a300b5006cd5a46115cd4a23b10ab2dfa305f195ef9f0816f07dd

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | e29fafc0ed03b1a471853efe650050f47d7db2da68a4c1f984fb4a95ae212144 |
| plan/evaluation.json | d47b64869659119a9cc99e38becab7a7d4630ea5f87f825614f1d79daf66170e |

## Data

### meta

#### title

Verify & re-diff loop (F-9)

#### version

2

#### status

final

### goal

Close the Rectify loop: one click on any sent History batch re-captures the live device, diffs it per annotation against the batch's stored frozen frame (pixel-region change-evidence plus an F-5 layout-finding oracle), renders a per-annotation Fixed / Still off verdict, and re-stages exactly the still-off annotations back through F-7's send path — persisting Verified / Follow-up sent through F-8's so-far-unused HistoryRepository.updateStatus hook.

### problemStatement

F-8 shipped a Verify button gated to BatchStatusModel.Fixed, but nothing in v1 ever writes Fixed, so the button is dead on arrival (MirrorHistoryTab.kt:177-185) and the PRD's strongest differentiator — per-annotation, per-region pixel + hierarchy verification — is unbuilt. Users cannot confirm an agent's fix landed without eyeballing two screenshots, and have no tight way to push back just the annotations that are still wrong.

### highLevelDescription

Four real gaps, everything else is reuse. (1) Read one batch WITH its annotations: HistoryEntryWithAnnotations already carries the data, the read mapper drops it — add @Transaction @Query findById(String) to HistoryEntryDao (ReadableDataSource.getById is Int-keyed and useless), HistoryDataSource.getBatch, HistoryRepository.get(batchId): SentBatchModel? (the existing write-path model reused as the read model), GetSentBatchUseCase / UpdateBatchStatusUseCase. (2) Read bytes off disk: BatchFileStore is write-only, so a new `fun interface BatchFileReader { suspend fun read(path: String): ByteArray? }` with a java.io desktop impl and null-returning Android/iOS stand-ins — the one genuinely new seam. (3) Pixel diff: pure IntArray arithmetic in commonMain (RegionDiff.changedRatio) fed by a ~10-line ImageBitmap.readPixels edge function, with NO PixelDiffer seam of any kind — a seam would push the arithmetic into desktopMain where desktopTest has no Skiko and destroy unit-testability. (4) densityDpi is not persisted: take it from the fresh AFTER dump and apply it to both sides — no column, no migration, no schema change. The UI is a new MirrorStage.Verify case rendered inside the ONE existing MirrorHost/MirrorContent composition, with the BEFORE/AFTER bitmaps delivered on a new plain StateFlow<VerifyFrames?> as a 5th content-lambda argument (a ~2-line RectifyWindow.kt edit). Verdict: Fixed iff nodeMatched && afterFinding == null && (regionPx < MIN_REGION_PIXELS || changedRatio > REGION_CHANGE_RATIO) — change-evidence, because the literal AC-24 wording would report an agent that did nothing as having fixed the bug.

### background

F-5 shipped a measure-mode toolkit (snapTarget, projectMeasureNodes, measure, findingOf, deltaLabel, plainValueLabel, displayDelta, unitOf, SEPARATOR) that is exactly the layout oracle this feature needs. Verified against the source during refinement: measure() already calls snapTarget internally and falls back to a synthetic MeasureNode(id = NO_MATCH_NODE_ID), and it already calls findingOf and returns the message on MeasureReadout.finding: String?. So the verify loop needs NEITHER a second snapTarget call NOR a second findingOf call — `matched` is `readout.nodeId != NO_MATCH_NODE_ID` and `afterFinding` is `afterReadout.finding`. One real friction point was found: SEPARATOR (and DELTA) are `private const val` inside MeasureFormat.kt, so BeforeAfterFormat.kt cannot reach them; SEPARATOR is widened to `internal` (one word) rather than duplicating the literal. F-7 shipped resolveAndSend()/assembleEnvelope/CropEncoder, which the follow-up re-enters unchanged except for one pendingFollowUpBatchId hook plus installing the retained AFTER HeldFrame as the frozen held frame so the crops come from the AFTER pixels for free. F-8 shipped the history tables, the on-disk ~/.rectify/history/<batchId>/{frame.png,hierarchy.xml,crop-<i>.png} layout, all five BatchStatusModel values and the zero-caller updateStatus hook, and presentation-core-ui already ships VerificationRow/VerificationOutcome whose own @Preview uses the design's exact strings. The three corrections of record carried from Preparation (no PixelDiffer seam; AC-24 change-evidence semantics; AS-1 requires an active mirror session) are honoured verbatim.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/inputs/codebase-recon.md | inputs/codebase-recon.md |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/inputs/design-verify-view.md | inputs/design-verify-view.md |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/preparation/preparation.md | preparation/preparation.json |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/preparation/brainstorm.md | preparation/brainstorm.json |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/preparation/clarify.md | preparation/clarify.json |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/plan/evaluation.md (corrections C-1 thresholds, C-2 abort paths) | plan/evaluation.json |
| code | presentation-feature-mirror/core/measure/{Measure,MeasureFormat,Findings,MeasureProjection,NodeSnap,PixelUnits}.kt — re-read during refinement; source of the verdictOf/beforeAfterLabel signature corrections |  |
| code | presentation-feature-mirror/commonTest/MirrorFakes.kt:176-192 — FakeFrameBitmap, whose readPixels override is a no-op and must start filling the buffer |  |
| code | presentation-feature-mirror/source/mirror/MirrorHistoryTab.kt:176-186 and MirrorContract.kt:331-340 — the gate and the stage derivation |  |
| code | presentation-core-ui/source/kit/atom/button/AppButton.kt:169-180 — `enabled: Boolean = true` already exists |  |
| code | .pipeline/sdd/docs/registry/{components,features,bugs,styles}.json |  |

### scope

#### in

- Delete the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:177-185 (the two-line comment plus the `if` wrapper) so Verify renders for every sent batch, plus the two collateral doc sites (MirrorHistoryTab.kt:239-243 preview KDoc, strings.xml:182 comment) and the stale MirrorContract.kt:490-500 VerifyBatch KDoc.
- Gate the button on data instead of status: `enabled = entry.framePath != null` (HistoryEntryModel already carries framePath; AppButton already has `enabled`). A row whose disk write failed has nothing to diff, so the affordance stays visible but inert rather than guaranteed-to-fail.
- New read path: HistoryEntryDao.findById → HistoryDataSource.getBatch → BatchFileReader → HistoryRepository.get(batchId): SentBatchModel? → GetSentBatchUseCase; plus UpdateBatchStatusUseCase over F-8's zero-caller updateStatus hook.
- Per-annotation pixel-region diff as pure commonMain IntArray arithmetic (Chebyshev RGB distance, integer-only, one pass, alpha ignored) behind a ~10-line ImageBitmap.readPixels edge function.
- Per-annotation hierarchy/layout diff reusing F-5's projectMeasureNodes + measure (which already snaps and already computes the finding), with one new before→after formatter built on deltaLabel/plainValueLabel/displayDelta/SEPARATOR.
- Widen `SEPARATOR` in core/measure/MeasureFormat.kt from `private` to `internal` — the one-word change that lets BeforeAfterFormat reuse it instead of duplicating the literal.
- The closed three-conjunct AND-rule combining both halves into the already-shipped VerificationOutcome (Fixed/StillOff).
- New MirrorStage.Verify phase inside the existing single MirrorHost/MirrorContent composition: BEFORE/AFTER frame columns with verdict-coloured rects, the 300px Verification panel of VerificationRows, the four-step strip, footer actions, the '← Back to live' chip.
- VerifyFrames on a new plain StateFlow as a 5th MirrorHost content-lambda argument (~2-line RectifyWindow.kt edit).
- 'Send follow-up · N' re-staging exactly the N still-off annotations from the retained AFTER HeldFrame, renumbered 1..N, through resolveAndSend() unchanged plus one pendingFollowUpBatchId hook; the Verify stage is torn down at that moment so F-7's existing send and send-failure UI drives the rest.
- 'Mark all as verified' override, which cancels verifyJob first.
- Persisting Verified and Follow-up sent via HistoryRepository.updateStatus.
- Abort/error states NoLiveSession, NotOnDisk, SizeMismatch, HierarchyUnusable, Disconnected, each with its own copy and each a distinct, user-legible outcome — never folded into N 'Still off' verdicts (C-2).
- VerifyPhase (Loading/Recapturing/Diffing/Ready) progress state and verifyJob cancellation, covering the ~2.7 s dumpHierarchy cost and mid-verify device disconnect.
- densityDpi taken from the fresh AFTER dump and applied to both sides — no schema change, no migration.
- New localisation keys (mirror_verify_*) in the one English strings.xml.
- Teach MirrorFakes' FakeFrameBitmap to actually fill the readPixels buffer from a deterministic synthetic function, so ViewModel-level verify tests cannot pass vacuously on all-zero regions. Still no Skiko and still no real ImageBitmap.

#### out

- The 'AGENT CHANGES' file-diff panel — no data source exists (AnnotationEnvelope carries no file metadata, there is no git/MCP reverse channel); cut for v1 exactly as F-8 cut its activity sub-line. The four-step strip keeps its 'Agent fixed' step as a static, countless label.
- Any PixelDiffer seam — expect/actual, fun interface or otherwise. Explicitly overrides clarify decision #4.
- Reading crop-N.png on the diff path (full-res frame.png + stored rects strictly dominates; this is what makes the 0-based/1-based crop-index conflict vanish) and reading the ≤128px thumbnail BLOB.
- A densityDpi column, a @Database version bump or any Room migration.
- A one-shot/cold-start capture use case — Verify requires an active mirror session (AS-1 as amended).
- A golden before/after corpus, a parameterised threshold-tuning test, or any Compose UI-test harness. MET-3 stays unverified-in-v1 (NFR-17); the tuning path is written down, not built (C-1).
- Temporal stabilisation of the AFTER frame (multi-sample to defeat mid-animation capture).
- Writing BatchStatusModel.InProgress or BatchStatusModel.Fixed from any v1 flow.
- Device serial matching / re-targeting Verify to a device other than the one currently mirrored (deviceLabel stays a display label).
- Android/iOS Verify UI or pixel diffing — desktop-only, matching F-7/F-8.
- Batch-level (whole-screen) diffing, or verifying more than one batch at a time.
- Any new colour token, any new domain model type, any new Gradle module, any new version-catalog entry.
- Fixing F-8's observeAll missing-LIMIT debt (out of scope — but findById must not copy that pattern; it reads exactly one row by primary key).
- CUT IN REFINEMENT — a `mirror_verify_retry` action inside the stage. The '← Back to live' chip plus the History row's Verify button already retry in two clicks; a second control for a rare error path is an affordance for nothing. Add it if manual use shows the error paths are common.
- CUT IN REFINEMENT — `mirror_verify_empty` copy for a zero-annotation batch. F-8 only records a batch that had staged annotations, so the state is unreachable; the run simply ends at Ready with zero rows. Add the string the day a zero-annotation batch is proven reachable.
- CUT IN REFINEMENT — the conditional '.claude/rules/database.md edit if a reviewer wants it'. An undecided doc edit is debt; the density-gap note lives in HistoryRepository.get's KDoc and nowhere else.
- CUT IN REFINEMENT — 'changedRatio read out of the debugger' as an observability metric. It is a non-action, not a metric; the per-run wall-clock log stays.

### assumptions

- AS-1 [AMENDED — correction #3, false as originally written]: there is no on-demand capture pipeline. StartMirroringUseCase is stream-only and no one-shot capture use case exists. Verify therefore requires an active mirror session on the device: it reads MirrorViewModel.currentHeld.value atomically (the same freezing pattern stageAnnotation already uses) and fails with VerifyError.NoLiveSession otherwise — a distinct, named, actionable error, never a 'Still off' verdict.
- AS-2: the device used for Verify is the same device that was mirrored when the batch was sent; v1 does not re-target a disconnected/replaced device (deviceLabel is a display label, not a serial, and is not used to enforce this).
- AS-3: F-5's node matching runs headlessly against a stored hierarchy snapshot. CONFIRMED BY SOURCE: measure() itself calls snapTarget and findingOf, so the oracle is reused by calling measure() twice and reading MeasureReadout.nodeId / .finding — no new matching algorithm and no second oracle call.
- AS-4: F-8's persisted frame.png and hierarchy.xml are sufficient diff inputs; densityDpi is the one known gap and is addressed via the AFTER-dump fallback, not a schema change. crop files are not read at all.
- AS-5: PRD Stage-4's resolution ('Verify is available on any sent batch regardless of status; History status chips remain informational only') is authoritative and supersedes F-8's AC-36 outright; this plan does not re-open it. US-9's 'on Fixed batches' line is the known-inconsistent PRD wording — FR-21/AC-23/AC-39 are normative and win.
- ImageBitmap.readPixels(buffer, startX, startY, width, height, bufferOffset, stride) is a commonMain member. VERIFIED: MirrorFakes.kt:183-191 overrides that exact seven-parameter signature on a plain `: ImageBitmap` implementation, which is only possible if it is an interface member in common code.
- FR-46's single frame-size check collapses both sides onto ONE IntSize and ONE density, which is what makes projectMeasureNodes safe to call with a shared frame size and makes one density for both sides coherent.

### constraints

| type | text |
| --- | --- |
| tech | No second MirrorHost call site. Verify is a new stage rendered inside the existing MirrorHost/MirrorContent composition (presentation-feature-mirror/CLAUDE.md Deviation 1: there is no Destination for this surface and there will not be one). |
| tech | [AMENDED — correction #1] Pixel diffing is pure IntArray arithmetic in commonMain (RegionDiff.changedRatio) with a ~10-line ImageBitmap.readPixels edge function. No PixelDiffer seam of any kind — not expect/actual, not a fun interface. BatchFileReader is the one genuinely new seam and only reads bytes off disk. |
| tech | Skiko trap, architectural not advisory: presentation-feature-mirror's desktopTest has no Skiko native library, so ImageBitmap(w, h) throws LibraryLoadException there. Exactly ONE file (core/verify/RegionPixels.kt) may mention ImageBitmap, and only as the extension receiver. No pure function anywhere may take or return an ImageBitmap — changedRatio/verdictOf/beforeAfterLabel take primitives and IntArrays only. The only other ImageBitmap mentions in the feature are the existing transport-shaped ones (MirrorViewModel.frame/frozenFrame, HeldFrame) plus the new VerifyFrames holder, none of which is a function under test. No test constructs a bitmap; FakeFrameBitmap (a plain interface implementation) is the only stand-in. |
| tech | The diff reads the full-resolution stored frame.png only — never crop-N.png, never the ≤128px thumbnail BLOB (AC-45). |
| tech | Hierarchy/layout diffing reuses F-5's projectMeasureNodes / measure / deltaLabel / plainValueLabel / displayDelta / unitOf / SEPARATOR in place (all internal to presentation-feature-mirror). Never copy them out, never reimplement unit conversion. SEPARATOR must be widened from `private` to `internal` in MeasureFormat.kt — it is file-private today, which is why this is called out as a change and not an assumption. |
| tech | [CORRECTED — the recon was wrong here] Adding the Verify stage DOES require a ~2-line RectifyWindow.kt edit: the MirrorHost content lambda widens by one parameter (a 5th StateFlow<VerifyFrames?>), the same precedent as its earlier 3→4 widening for frozenHierarchy. VerifyFrames is therefore `public` (the shell reads it), unlike the deliberately internal HeldFrame. |
| tech | Pixels never enter reduced state (presentation-feature-mirror/CLAUDE.md Deviation 2). VerifyFrames rides a plain StateFlow beside frame/frozenFrame; MirrorUiState.history remains the only audited exception and is not extended. |
| tech | No database schema change or migration. database.md requires a @Database version bump AND a hand-written Migration for any new column, and F-8's stay-at-version-1 was a one-off explicitly flagged as NOT reusable. densityDpi comes from the fresh AFTER dump. Verified against HistoryEntryEntity: it has no density column and gains none. |
| tech | domain-layer / error-handling: one-shot operations return Result<T>, reactive ones return a bare Flow<T> (never Flow<Result<T>>); every *UseCaseImpl body runs inside resultLauncher(errorMapper = Failure.Technical::Database), never raw runCatching. HistoryRepository.get returns a plain nullable, matching observe()'s bare-Flow precedent and record()'s throw-and-let-resultLauncher-map contract. Mappers never throw; null-on-failure is universal — a failed image never fails the operation. |
| tech | orbit-mvi (mirror's local precedent): handleIntent dispatches to private handlers, each `= intent { reduce {}; postSideEffect() }`; the container(...) trailing lambda is init-time only. package-layout + explicitApi(): core/ is internal-only, source/ is public, explicit visibility AND return type everywhere. |
| tech | koin-di: one module per Gradle module; initKoin()'s load order is significant — append new bindings within their existing layer group, never reorder. |
| tech | localisation: every piece of copy is a Res.string.* key resolved at the call site (VerificationRow.outcomeLabel takes a raw String, so the localised word is resolved by the caller). Key convention mirror_verify_*; no AAPT-style \' escapes in strings.xml. |
| tech | testing: presentation-feature-mirror/CLAUDE.md is stricter than the root rule and wins locally — plain-word camelCase backtick names only. The root testing.md fact behind it is that a comma or a colon fails compileTestKotlinIosArm64 (an apostrophe does not, corrected by experiment), but the module's own convention is the one to follow here. Desktop is the task that runs; :compose-application has no test wiring, so nothing testable may live there; no Compose UI-test harness exists, so the stage is covered by light/dark @Preview pairs per compose-preview-convention.md. |
| tech | design-tokens: no literal Color(0x…), no raw .dp, no hardcoded TextStyle at a call site. Verdict colours are Theme.color.sentFixed / warnStillOff, which are already identical in light and dark — NFR-9 is satisfied by using them and adding no new token. |
| business | Desktop (macOS) only in v1, matching F-7/F-8 and NFR-10. |
| time | dumpHierarchy costs ~2.7 s on a Pixel 8 and measure() is ~100 ms per 300-node dump (×2 per annotation), so the run must be cancellable and visibly staged — a naive implementation reads as a frozen window. |

### dependencies

| on | note |
| --- | --- |
| F-7 (resolve-and-send-mcp) | The one-click follow-up reuses resolveAndSend() / MirrorIntent.ResolveAndSend / assembleEnvelope / CropEncoder / mintSessionId unchanged except for one pendingFollowUpBatchId hook on the success path. Crops come from the AFTER frame because the retained AFTER HeldFrame is installed as currentFrozenHeld before resolveAndSend runs — assembleEnvelope crops the frozen held bitmap, so AC-42 is satisfied by that one assignment rather than by new crop code. |
| F-8 (history-batch-tracking) | Consumes HistoryRepository.updateStatus (zero callers today), the stored frame.png/hierarchy.xml, BatchStatusModel.Verified/FollowUpSent (already persisted and rendered, no schema change) and the inert MirrorIntent.VerifyBatch stub + verifyBatch handler seam at MirrorViewModel.kt:388. Inherits two review findings: the 0-vs-1-based crop index (resolved by never reading crops, FR-47) and observeAll's missing LIMIT (explicitly NOT to be copied into findById). |
| F-5 (measure-mode) | The hierarchy half IS F-5: projectMeasureNodes projects both snapshots against the one shared frame size and measure() does the snap, the fallback and the finding in one call. Only one before→after formatter is missing, and one `private`→`internal` widening of SEPARATOR. |
| presentation-core-ui kit (VerificationRow, StatusLine, MirrorFrameView, AnnotationOverlay, ErrorState, AppButton with its existing `enabled`, Chip) | Every visual element of the design already exists. F-9 adds zero kit components and changes none. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Threshold tuning is unvalidated (MET-3). CHANNEL_NOISE_FLOOR / REGION_CHANGE_RATIO / MIN_REGION_PIXELS are a first guess reasoned from antialiasing, caret and scroll cases, not measured. | high | med | Three named consts in one file, each KDoc'd with its ceiling and tuning method; 'Mark all as verified' is always available as the human override; recorded unverified-in-v1 per NFR-17; the golden-corpus upgrade path is written down, not built. No test may assert a constant's literal value (C-1 / bugs.json#PP-TD-4). |
| A display-override device (the documented Pixel 8 984×2132-vs-1080×2400 case) makes projectMeasureNodes discard the whole hierarchy, which would otherwise emit N confident, wrong 'Still off' verdicts — the most expensive lie a verification tool can tell. | med | high | Explicit non-empty-but-projects-to-zero check on BOTH sides → VerifyError.HierarchyUnusable, abort before any verdict with an EMPTY rows list. Kept distinct from the empty-snapshot case, which still degrades per-annotation to NO_MATCH exactly as AC-41 requires. Asserted by a test on the rows list being empty, not merely on the error being set (C-2, and bugs.json#PP-TD-5's failure mode). |
| The Skiko trap: if any diff code drifts into an ImageBitmap-typed helper, the feature's own tests stop compiling or throw LibraryLoadException. | med | high | Exactly one file (RegionPixels.kt, ~10 lines) mentions ImageBitmap as a receiver, with a KDoc note saying why; RegionDiff/VerifyVerdict/BeforeAfterFormat take primitives only; no test constructs a bitmap; a grep step in the hygiene step enforces it. |
| FakeFrameBitmap.readPixels is a no-op today, so every ViewModel-level region read would return an all-zero IntArray and every pixel conjunct would be vacuously false — a whole class of verify tests would pass or fail for reasons unrelated to the code under test. | high | high | Extend FakeFrameBitmap to fill the buffer from a deterministic per-pixel function the test supplies (default: a stable gradient), so a BEFORE/AFTER pair can be made identical or different on purpose. It stays a plain ImageBitmap interface implementation — no Skiko, no bitmap construction. Found during refinement; this was not in the draft. |
| dumpHierarchy costs ~2.7 s and N region diffs follow, so a naive implementation reads as a frozen window and the user clicks Verify again. | high | med | VerifyPhase (Loading → Recapturing → Diffing → Ready) in reduced state drives a visible progress UI (NFR-15); all decode/diff under withContext(Dispatchers.Default); verifyJob cancellable from ExitVerify, a second Verify, MarkAllVerified, SendFollowUp or a device change (the dumpJob precedent). |
| Reusing resolveAndSend() as 'unchanged' needs a pendingFollowUpBatchId hook; a sloppy edit could make every normal send try to write FollowUpSent. | low | med | Nullable field, null-checked, cleared in a finally; one ViewModel test asserting a plain (non-follow-up) send writes no status at all. |
| Because MirrorStage.Verify is derived AHEAD of Frozen, staging the follow-up (which sets state.frozen) would leave the user pinned on a stale Verify view and hide F-7's send and send-failure UI entirely. | med | high | SendFollowUp tears the stage down at the moment it stages: cancel verifyJob, clear VerifyUiState and verifyFrames, set the frozen frame, THEN call resolveAndSend(). The stage falls back to Frozen and F-7's existing send path owns the rest including failure. pendingFollowUpBatchId is a plain field, not reduced state, so it survives the teardown. Found during refinement; the draft left the user stuck. |
| The 0-based (history) vs 1-based (envelope) crop index resurfaces when renumbering the follow-up annotations. | low | med | Never read crop-N.png, so there is exactly one conversion point: the mapIndexed building StagedAnnotation(index = i + 1). One test over a 2-of-3 still-off selection asserting indices are 1 and 2, not 0 and 2. |
| Verify requiring a live mirror session (AS-1 corrected) reads as a bug on the History tab ('I clicked Verify and got an error'). | med | low | VerifyError.NoLiveSession renders a specific, actionable ErrorState naming the device to start mirroring, not a generic failure — and never a verdict (C-2). |
| Scope creep: F-9 touches data, database, domain, repository, presentation and the shell call site at once. | med | med | Steps 1-6 (read path + pure verify core) have no dependency on any UI type and are independently testable and mergeable before the stage exists — re-verified during refinement: RegionDiff/VerifyThresholds/VerifyVerdict/BeforeAfterFormat need only F-5's internal measure types, and RegionPixels needs only DeviceRect + ImageBitmap, both of which already exist. Step 6 does not depend on step 5 and was unchained accordingly. |
| An annotation that was already correct and that the agent correctly left alone reads 'Still off' under change-evidence semantics. | med | low | Taken knowingly as the cheap error — a false 'Still off' costs one click of 'Mark all as verified'; a false 'Fixed' destroys trust in the whole tool. Documented in VerifyVerdict's KDoc as the accepted ceiling. |
| A deliberate `wm density` change between send and verify silently relabels the BEFORE side in the new density. | low | low | Unmeasurable in v1 (density was never persisted) and strictly better than labelling everything in raw px. Recorded as a data gap with the densityDpi-column migration as the written-down upgrade path. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-21 | On any sent batch (not gated on a 'Fixed' status, since nothing in v1 is confirmed to set one), provide a 'Verify' action that re-captures the live screen and diffs it, per annotation box, against the original frozen frame; matches mark the annotation resolved, mismatches surface a one-click follow-up that re-stages that single annotation for re-send. |
| FR-37 | Remove the BatchStatusModel.Fixed gate on the History row's Verify button (MirrorHistoryTab.kt HistoryEntryRow trailing slot, lines 177-185) so it renders for every sent batch regardless of status. The button is enabled when `entry.framePath != null` and disabled otherwise — a data precondition, never a status one; the run still fails with NotOnDisk if the file disappears after render. |
| FR-38 | On Verify click, enter a Verify phase that re-captures the live device frame and hierarchy for the device that sent the batch (AS-1/AS-2, AS-1 as amended), reusing the existing mirror capture pipeline rather than a new one. |
| FR-39 | [AMENDED — correction #1, overrides clarify decision #4] For each annotation, read the matching region from the frozen and re-captured frames via ImageBitmap.readPixels(startX, startY, w, h) — a commonMain member, with the annotation rect as the region arguments — and compute the pixel-region diff as pure IntArray arithmetic in commonMain (no PixelDiffer seam, no expect/actual, no fun interface for the diff itself). A separate fun interface BatchFileReader (desktop java.io impl, null-returning Android/iOS stand-ins, matching the BatchFileStore/CropEncoder pattern) is added solely to read frame.png off disk. |
| FR-40 | For each annotation, re-run F-5 measure-mode's node matching against the re-captured hierarchy and compare it to the original stored measurement, producing a dp-delta result string in the same format as live measure-mode (e.g. 'top Δ6dp → 0dp'). Implemented as two measure() calls — the function already snaps, already falls back to NO_MATCH_NODE_ID and already computes the finding — plus one new before→after formatter. |
| FR-41 | [AMENDED — correction #2, change-evidence semantics] Mark an annotation's verdict 'Fixed' iff: the hierarchy node matched on both sides AND the matched node's layout finding is gone in the AFTER measurement AND the pixel region shows change-evidence (regionPx < MIN_REGION_PIXELS, where the ratio carries no information and the hierarchy half decides alone, OR changedRatio > REGION_CHANGE_RATIO). Anything else — including an unmatched hierarchy node — is 'Still off', never silently skipped as 'Fixed'. The MIN_REGION_PIXELS branch short-circuits only the pixel conjunct; it never bypasses the match or finding conjuncts. |
| FR-42 | Render the Verify view per design-verify-view.md as a new phase inside the existing MirrorHost/MirrorContent composition (no second MirrorHost call site): two 296×640 frames (BEFORE/AFTER) with per-annotation rects coloured by verdict, a 300px 'Verification' panel with result rows and verdict words, an 'AGENT CHANGES' section cut for v1 (see out-of-scope), footer actions 'Send follow-up · N' and 'Mark all as verified', a four-step header strip, and a '← Back to live' chip that resets to the live mirror with the History tab selected. |
| FR-43 | 'Send follow-up · N' re-stages exactly the N still-off annotations (plural; AC-42) into a new AnnotationEnvelope sourced from the retained AFTER HeldFrame — not the stale frozen frame — renumbered 1..N for the envelope with promptOverride explicitly null, and re-enters F-7's send path via resolveAndSend() unchanged except for one pendingFollowUpBatchId hook that writes Follow-up sent on its success path. Staging tears the Verify stage down first (cancel verifyJob, clear VerifyUiState and verifyFrames) so the send is driven by F-7's existing UI. |
| FR-44 | 'Mark all as verified' force-sets every annotation's verdict to Fixed without further diffing and writes the batch status to Verified; if a run is still in progress it cancels verifyJob first so a late completion cannot overwrite the override. |
| FR-45 | Persist verify outcomes through HistoryRepository.updateStatus: Verified when a run ends with at least one row and every row Fixed (or via the Mark-all override), Follow-up sent immediately after a follow-up send succeeds (written on the ORIGINAL batch; the follow-up itself records its own new Sent row). A run that produced zero rows — including every abort — writes nothing. No v1 code path writes In progress or BatchStatusModel.Fixed. |
| FR-46 | If the re-captured frame's pixel dimensions differ from the stored frozen frame's, abort the diff before any per-annotation comparison runs and surface an error state (SizeMismatch) rather than comparing misaligned regions. |
| FR-47 | Reconcile the crop-index convention mismatch (0-based in history storage, 1-based in the AnnotationHandoff envelope) by never reading crop-N.png on the read side at all — the diff loop reads full-res frame.png plus stored rects, which strictly dominates the crops and makes the conflict vanish there. The only conversion point is the follow-up envelope build (mapIndexed → StagedAnnotation(index = i + 1)). |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. Satisfied in-place by the already-shipped VerificationRow/VerificationOutcome molecule and Theme.color.sentFixed/warnStillOff tokens. |
| NFR-15 | performance | Re-capture and the per-annotation diff loop run off the UI thread (Dispatchers.Default, consistent with AnnotationHandoff's existing suspend contract) and show a visible in-progress state (VerifyPhase: Loading/Recapturing/Diffing/Ready), since N region diffs plus a ~2.7s hierarchy dump can take long enough to need one. |
| NFR-16 | reliability | A device disconnect mid-verify aborts the run cleanly: no partial status write, the original batch status is untouched, and the user lands in a retryable state rather than a stuck spinner. Reuses the existing flatMapLatest(sessionDeviceId) teardown that already cancels in-flight jobs. |
| NFR-17 | maintainability | MET-3's <5% false-positive target is not automatically verifiable in this codebase in v1 — there is no golden before/after dataset and no Compose UI-test harness. Diff thresholds (CHANNEL_NOISE_FLOOR, REGION_CHANGE_RATIO, MIN_REGION_PIXELS) are named tunable constants with stated ceilings, validated by manual spot-check only; MET-3 is recorded as unverified-in-v1 rather than asserted by an automated test. No test may pin a threshold's literal value — tests may only use a constant to CONSTRUCT an input (floor-1, floor+1) so that retuning changes the data, not the expectation. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-23 | Given any sent batch, when the user clicks 'Verify', then the app re-captures the live screen and diffs it, per annotation box, against the stored frozen frame using a pixel-region diff plus a hierarchy/layout diff of the matched node. | must | test |
| AC-24 | [AMENDED — correction #2, change-evidence semantics replaces the self-defeating literal wording] Given a Verify diff, an annotation is marked 'Fixed' only when its hierarchy node matches, its layout finding is gone in the AFTER measurement, AND its pixel region shows change-evidence (regionPx < MIN_REGION_PIXELS, or changedRatio > REGION_CHANGE_RATIO); given any of these fails, the annotation is 'Still off' and a one-click follow-up re-stages exactly the still-off annotations for re-send. The original wording ('no material difference → resolved') is rejected because it would report an agent that changed nothing as having fixed the bug. | must | test |
| AC-39 | Given the History tab, when a row's batch status is anything other than Fixed (e.g. Sent), then the Verify button is still rendered and clickable — the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:177-185 is removed, superseding F-8's AC-36. The only condition that may disable it is the data one (framePath == null); status may never gate it again. | must | inspection |
| AC-40 | Given Verify is clicked, when the re-captured frame's pixel dimensions differ from the stored frozen frame's (resolution or orientation changed), then no per-annotation diff runs and an error state is shown instead of a misaligned comparison. | must | test |
| AC-41 | Given a Verify run, when an annotation's original hierarchy node cannot be matched in the re-captured hierarchy (moved, renamed, or removed), then that annotation's verdict is 'Still off' regardless of its pixel-region diff result. Distinctly: when a NON-EMPTY hierarchy projects to ZERO candidates on either side (the display-override coordinate mismatch), the run aborts with VerifyError.HierarchyUnusable and produces NO verdicts at all rather than N 'Still off' rows. | must | test |
| AC-42 | Given at least one annotation is 'Still off' after Verify, when the user clicks 'Send follow-up · N', then exactly the N still-off annotations are re-staged from the re-captured (AFTER) frame and crops and sent through F-7's existing send path, and the batch status updates to Follow-up sent. | must | test |
| AC-43 | Given a Verify run completes with every annotation 'Fixed', or the user clicks 'Mark all as verified', then the batch status updates to Verified via HistoryRepository.updateStatus. | must | test |
| AC-44 | Given a Verify run is in progress (re-capture or diffing), when the device disconnects, then the run aborts, the original batch status is left unchanged, and the user can retry Verify once reconnected. | must | test |
| AC-45 | Given the diff reads image bytes for comparison, then it reads the full-resolution stored frame.png (never crop-N.png, never the ≤128px thumbnail BLOB), reusing stored rects for region extraction. | must | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Start mirroring the device", "Open History tab", "Locate a sent batch", "Click Verify", "Phase shows Recapturing then Diffing", "Diff runs per annotation against the frozen frame", "Every annotation reads Fixed and the batch status becomes Verified"] |
| SC-9 | Follow-up re-send after a failed verify | edge | ["Verify finds a remaining mismatch for one or more annotations", "Footer reads 'Send follow-up · N' with N = still-off count", "Click it", "The Verify stage tears down and the AFTER frame becomes the frozen frame", "Exactly those N annotations are re-staged, renumbered 1..N, promptOverride null", "resolveAndSend() publishes the envelope through F-7's unchanged path", "The original batch flips to Follow-up sent while the follow-up records its own new Sent row"] |
| SC-19 | Verify a batch whose status is still Sent | edge | ["Open History tab", "Locate a batch with status Sent (not Fixed)", "Confirm the Verify button is present and enabled because its framePath is non-null", "Click Verify", "Diff runs exactly as it would on any other status"] |
| SC-20 | Mark all as verified override | happy | ["Open the Verify view with 2 of 3 annotations Still off", "Click 'Mark all as verified'", "Any in-flight verifyJob is cancelled first", "All annotations show Fixed", "Batch status updates to Verified"] |
| SC-21 | Device disconnects mid-verify | error | ["Click Verify on a sent batch", "Re-capture or diffing is in progress", "Device disconnects", "Verify run aborts with VerifyError.Disconnected", "Original batch status is unchanged and Verify can be retried after reconnect"] |
| SC-22 | Resolution mismatch aborts the diff | error | ["Click Verify on a batch sent at one device resolution/orientation", "The live frame is at a different resolution/orientation", "The single IntSize check fails before any per-annotation work", "VerifyError.SizeMismatch is shown instead of a misaligned result"] |
| SC-23 | Verify without a live mirror session | error | ["Open History with no device streaming", "Click Verify on a sent batch", "currentHeld.value is null", "VerifyError.NoLiveSession names the device and says to start mirroring", "No batch is read, no verdict is produced and no status is written"] |
| SC-24 | Display-override device makes the hierarchy unusable | error | ["Click Verify on a batch captured on a device with a wm-size override", "Both hierarchies are non-empty but project to zero candidates against the shared frame size", "The run aborts before the per-annotation loop", "VerifyError.HierarchyUnusable is shown with an empty verdict list — not N 'Still off' rows", "A log.w line records both frame sizes so the cause is diagnosable from the log alone"] |

### definitionOfReady

- Preparation signed off with readyForPlan: true and all three corrections of record (no PixelDiffer seam; AC-24 change-evidence; AS-1 live session required) carried verbatim.
- codebase-recon.md accepted as ground truth for existing symbols and paths — no re-derivation of the four gaps.
- design-verify-view.md available as the pixel-level UI contract, with the AGENT CHANGES panel confirmed cut.
- An Android device reachable over adb with a live mirror session, so the manual verification demo can actually run (F-8's demo steps were waived for lack of a GUI run — do not repeat that if a device is available).
- The six closed brainstorm survey decisions and nine closed clarify decisions are read and not re-opened.
- Evaluation corrections C-1 (behavioural threshold tests only) and C-2 (two distinct, user-legible aborts) applied and visible in testStrategy and errorStates.

### definitionOfDone

- AC-23 and AC-24 (the must-pass pair) are each covered by at least one green commonTest assertion in presentation-feature-mirror.
- The BatchStatusModel.Fixed gate and both collateral doc sites are gone; grep for 'BatchStatusModel.Fixed' returns no gate in presentation code, only the labelKey() branch and preview fixtures.
- No file outside presentation-feature-mirror/core/verify/RegionPixels.kt mentions ImageBitmap for diffing purposes, no pure function takes or returns one, and no test constructs one.
- C-1 check: `grep -n 'CHANNEL_NOISE_FLOOR\|REGION_CHANGE_RATIO\|MIN_REGION_PIXELS' presentation-feature-mirror/src/commonTest` shows the constants used only to BUILD inputs, never as an expected value in an assert.
- C-2 check: VerifyError.HierarchyUnusable and VerifyError.NoLiveSession each have a ViewModel test that asserts the rows list is EMPTY and UpdateBatchStatusUseCase was called zero times — not merely that an error field is set.
- ./gradlew :presentation-feature-mirror:desktopTest :data-repository-impl:desktopTest :data-database-impl:desktopTest :domain-usecase-impl:desktopTest is green.
- Cross-target gate green: ./gradlew :presentation-feature-mirror:build :data-repository-impl:build --max-workers=1 (the --max-workers=1 workaround for bugs.json#PP-TD-8's Kotlin/Native linker OOM).
- MirrorVerifyStage ships a light/dark @Preview pair per compose-preview-convention.md.
- Every new string is a Res.string.mirror_verify_* key in the one English strings.xml, with no AAPT-style apostrophe escapes.
- @Database version is still 1 and no Migration file was added.
- The three threshold constants each carry a KDoc naming their ceiling and the manual tuning method, and NFR-17/MET-3 is recorded as unverified-in-v1 in the verdict rather than asserted.
- A manual end-to-end demo against a real device: send a batch, let an agent (or a hand edit) change the UI, Verify, confirm the verdicts, send a follow-up, confirm both status writes. If no device/GUI is available, record it as an explicit waiver rather than a silent pass.

### successMetrics

- Every sent batch with a stored frame shows a clickable Verify action regardless of status (AC-39) — zero batches stuck behind an unreachable gate.
- MET-3 (<5% false positives) tracked via manual threshold spot-check only in v1; not asserted by an automated test (NFR-17).
- A verify run on a 3-annotation batch completes within ~4 s end to end (dominated by the one ~2.7 s dumpHierarchy), with a visible phase change inside the first frame after the click.
- Zero new Gradle modules, zero new version-catalog entries, zero new kit components, zero new colour tokens, zero schema migrations.

### architecture

#### approach

A thin new read path down the existing layer stack, one new pure internal package in presentation-feature-mirror (core/verify/), one new stage case inside the existing MirrorHost composition, and one new plain StateFlow for the two bitmaps. The verify run is a single cancellable suspend function on verifyJob with seven ordered steps: (1) snapshot sessionDeviceId + currentHeld.value atomically — null held ⇒ NoLiveSession, abort with zero rows; (2) GetSentBatchUseCase(batchId) — null/failure ⇒ NotOnDisk; (3) decodeToImageBitmap the stored frameBytes and compare its IntSize to the live HeldFrame's — disagree ⇒ SizeMismatch, abort before any per-annotation work (from here there is exactly ONE IntSize and ONE density for both sides); (4) DumpHierarchyUseCase(deviceId) for the AFTER hierarchy, phase = Recapturing (the ~2.7 s cost); (5) projectMeasureNodes both snapshots against that one shared IntSize — non-empty-but-zero-candidates on either side ⇒ HierarchyUnusable and abort with zero rows, while an empty-to-begin-with snapshot degrades per-annotation to NO_MATCH (AC-41); (6) per annotation i (history's 0-based order): measure() both sides (each call already snaps and already computes its finding), regionPixels + changedRatio, verdictOf, beforeAfterLabel — then publish VerifyFrames and reduce the rows; (7) rows non-empty and all Fixed ⇒ UpdateBatchStatusUseCase(batchId, Verified), otherwise write nothing. The AFTER HeldFrame is retained in a private verifyAfterHeld field for the follow-up path. Device identity is re-checked at every suspension point; a change or null ⇒ Disconnected with no status write. Every abort produces a named VerifyError and an EMPTY rows list, so no abort can ever be mistaken for a verdict (C-2).

#### rationale

Every choice is the highest rung of the ladder that holds. Reuse an existing commonMain API (ImageBitmap.readPixels) over inventing a seam — a seam here would be an interface with one implementation AND would push the arithmetic into desktopMain where desktopTest has no Skiko, destroying the testability that is the whole point. Reuse an existing oracle: measure() already snaps, already falls back to NO_MATCH_NODE_ID and already returns the finding message, so the verify loop calls it twice and reads two fields instead of re-running snapTarget and findingOf. Reuse an existing model (SentBatchModel) over a new read-model type. Reuse an existing composition (MirrorStage) over a new Destination. Reuse an existing send path (resolveAndSend) over a new envelope pipeline — made literally true by installing the retained AFTER HeldFrame as the frozen held frame before calling it. Reuse an existing molecule (VerificationRow) and an existing `enabled` parameter on AppButton over new UI. The only genuinely new seam is BatchFileReader, because BatchFileStore is write-only and there is no okio/kotlinx-io in the catalog. Status writes happen last and only at terminal points, which makes NFR-16's 'no partial status write' true by ordering rather than by a transaction.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| fun interface PixelDiffer with a desktopMain implementation (the shape a reviewer pattern-matches for) | One interface, one implementation, no second caller — the exact shape the repo's own rules forbid — and it pushes the arithmetic into desktopMain, which STILL has no Skiko, so it buys nothing for testability and costs the ability to test the diff at all. CropEncoder exists because AWT ImageIO has no commonMain equivalent; BatchFileStore because there is no okio in the catalog. Neither justification applies to integer subtraction. |
| expect/actual PixelDiffer exactly as clarify decision #4 states | Strictly dominated: the repo reserves expect/actual for DatabaseBuilder and SettingsProvider only, it needs an actual in four source sets for identical code, and it inherits every con of the fun-interface option plus more files. Recorded only so the override is traceable. |
| Pixel half as similarity — Fixed when the region still matches BEFORE within threshold (AC-24 as literally worded) | An agent that does nothing at all gets every findings-free annotation marked Fixed — the single worst outcome this feature can produce, and precisely the false-positive class MET-3 names. AC-24's wording is amended instead of implemented as written. |
| Pixel half advisory only — verdict from the hierarchy half alone | Genuinely the laziest option and tempting (the design never displays a pixel percentage), but it deletes half of FR-41/AC-23 and goes blind to every defect with no geometry signature: wrong colour, wrong text, wrong icon, clipping, overlap. |
| Drop the MIN_REGION_PIXELS branch and let the ratio decide every region | Re-examined under C-1. In a region under 64 px a single antialiasing pixel is already >1.5%, so the 2% threshold is pure noise in BOTH directions there — it would flip a 6×6 icon to 'Fixed' on a subpixel wobble as easily as to 'Still off'. The branch removes a signal that carries no information rather than adding a loophole: it short-circuits ONLY the pixel conjunct, so match and finding still have to hold. Its accepted ceiling, recorded in VerifyVerdict's KDoc: a tiny region whose finding disappeared because a NEIGHBOUR moved reads Fixed with zero pixels changed. |
| Call snapTarget and findingOf directly from the verify loop (as the draft's verdictOf(matched, afterFinding: Finding?) signature implied) | Found wrong during refinement. measure() already does both internally and returns MeasureReadout(nodeId, finding: String?). Calling them again means rebuilding MeasureNeighbours by hand, doubling the per-annotation cost and risking a second, divergent oracle. verdictOf therefore takes `afterFinding: String?` and `matched` is derived as `nodeId != NO_MATCH_NODE_ID` on both readouts. |
| Add a densityDpi column to history_entry with a @Database version bump and a hand-written Migration | A migration cannot retroactively supply density for rows already on disk, so the live-density fallback must be built either way — building only the fallback is strictly less code for strictly the same coverage. Correct direction, wrong release; recorded as the upgrade path. |
| Build a one-shot CaptureFrameUseCase so Verify works from a cold History tab (making AS-1 true) | A new adb path down through data-device, a new use case, new failures to surface and a duplicated decode path — a feature-sized detour inside an already-XL feature, with no second caller. Right thing to build the day one appears. |
| Let a non-empty hierarchy that projects to zero candidates degrade per-annotation to no-match (AC-41 applied literally) | It would emit N confident, wrong 'Still off' verdicts from a tooling coordinate-space mismatch and push the user into a pointless follow-up send. One extra error case against a whole class of silent wrong answers. This is half of correction C-2. |
| Bundle frame + frozenFrame + verifyFrames into one MirrorFrames holder to keep the content lambda at 4 args | A bigger diff across unrelated call paths for a cosmetic win. The lambda already widened 3→4 once; widening it again is the established precedent and there is exactly one call site. (Putting the bitmaps in reduced state is forbidden outright by Deviation 2.) |
| Carry only the BEFORE bitmap and reuse the existing frozenFrame StateFlow for the AFTER one | Tempting (it would avoid the new type entirely) but it couples the verify run to the freeze lifecycle: currentFrozenHeld pairs with state.frozen, which drives the Frozen stage and the annotation-staging UI, and MirrorViewModelHeldFramePairingTest polices that pairing. Installing the AFTER frame there is correct exactly once — at SendFollowUp — and wrong for the whole diff phase. |
| Hide the Verify button when entry.framePath == null | Hiding reintroduces the invisible-affordance bug from the other direction. Disabling keeps the control discoverable (AC-39 is about status never gating it, not about a data precondition), costs one already-existing AppButton parameter and zero plumbing — HistoryEntryModel already carries framePath into reduced state. The NotOnDisk error stays for the file-deleted-after-render race. |
| Normalise differently-sized regions with ThumbnailBitmap.cropAndScale before diffing | Unnecessary: FR-46 guarantees one shared frame size before any per-annotation work runs, so both regions are the same dimensions by construction. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| HistoryEntryDao.findById | data | new | components.json#HistoryEntryDao | @Transaction @Query("SELECT * FROM history_entry WHERE id = :id") returning HistoryEntryWithAnnotations?. The @Relation already carries the annotations; only the query is missing. Explicitly NOT ReadableDataSource.getById (Int-keyed, useless for String batch ids) and explicitly NOT a copy of observeAll's missing-LIMIT pattern — it reads exactly one row by primary key. |
| HistoryDataSource.getBatch(id: String) | data | new | components.json#HistoryDataSource | Bespoke String-keyed read method on the existing public datasource, delegating to the DAO. Returns null when absent. |
| BatchFileReader | data | new |  | `fun interface BatchFileReader { suspend fun read(path: String): ByteArray? }` in data-repository-impl beside BatchFileStore. One abstract method so SAM conversion survives (adding read() to BatchFileStore would destroy its fun-interface-ness). Never throws: missing, unreadable, permission-denied or truncated all return null. The only genuinely new seam in F-9. |
| FileSystemBatchFileReader (desktopMain) + null-returning android/ios stand-ins | data | new | components.json#BatchFileStore | java.io implementation under Dispatchers.IO, same shape and same never-throws contract as FileSystemBatchFileStore; provided through the existing BatchStoreProvider expect/actual trio so no new DI plumbing shape is invented. |
| HistoryRepository.get(batchId): SentBatchModel? | data | new | components.json#HistoryRepository | Assembles the EXISTING SentBatchModel from the @Relation children plus BatchFileReader bytes (frameBytes from framePath, hierarchyXml from hierarchyPath, cropBytes left empty because nothing consumes them on this path). Returns null when the row is absent or the files are unreadable — a stored path is documented as a hint, never a guarantee. Its KDoc is the single place the density gap and its migration upgrade path are recorded. |
| GetSentBatchUseCase (+Impl) | logic | new |  | Thin resultLauncher(errorMapper = Failure.Technical::Database) wrapper returning Result<SentBatchModel>; Failure.Logic.NotFound when the repository returns null. No branching logic of its own beyond that one null check. |
| UpdateBatchStatusUseCase (+Impl) | logic | new |  | Thin resultLauncher wrapper over HistoryRepository.updateStatus(batchId, status) — the hook F-8 built for F-9 and that has had zero callers until now. Writes only Verified and FollowUpSent. |
| HierarchySnapshotMapper.toModel(HierarchyDumpStdout(storedXml, "")) | data | reuse |  | Re-parses the stored hierarchy.xml into the BEFORE HierarchySnapshotModel with the exact semantics the live path uses. Called INSIDE data-repository-impl because presentation must never import data-device-api. Density comes back 0 — which is precisely why the AFTER-dump density decision exists. |
| core/measure/MeasureFormat.kt — SEPARATOR visibility | logic | reuse |  | ONE WORD CHANGED: `private const val SEPARATOR` becomes `internal const val SEPARATOR` so BeforeAfterFormat can join segments with the same middle dot every other measure string uses. Verified during refinement — the draft assumed it was already reachable and it is not. DELTA stays private; BeforeAfterFormat goes through deltaLabel(). |
| core/verify/RegionPixels.kt | logic | new |  | `internal fun ImageBitmap.regionPixels(rect: DeviceRect, frameSize: IntSize): IntArray?` — clamps the annotation rect into the frame (a drag can overhang), allocates w*h and calls the commonMain readPixels(buffer, startX, startY, w, h, 0, w); null for a degenerate rect. The ONLY file in the feature that names ImageBitmap for diffing, ~10 lines, deliberately untested because Skiko is absent from desktopTest. Carries a KDoc saying exactly that. |
| core/verify/RegionDiff.kt | logic | new |  | `internal fun changedRatio(before: IntArray, after: IntArray): Float` — per-pixel Chebyshev distance in RGB, changed(p) = max(\|Δr\|,\|Δg\|,\|Δb\|) > CHANNEL_NOISE_FLOOR, ratio = changed/n, integer-only, one pass, alpha ignored, allocation-free. No Compose type anywhere in the file, so it runs in the Skiko-less desktopTest. The single most important testability decision in the feature. |
| core/verify/VerifyThresholds.kt | logic | new |  | CHANNEL_NOISE_FLOOR = 10 (of 255), REGION_CHANGE_RATIO = 0.02f, MIN_REGION_PIXELS = 64 — three internal consts in one file, each KDoc'd with its ceiling (antialias/subpixel/gamma wobble; a 2×30 caret in a 200×60 region is 0.5%; below 64 px one antialiased pixel already exceeds 1.5% so the ratio carries no information in either direction) and the manual tuning method per NFR-17/MET-3. Never inlined at a call site, and never asserted by literal in a test (C-1). |
| core/verify/VerifyVerdict.kt | logic | new |  | `internal fun verdictOf(matched: Boolean, afterFinding: String?, changedRatio: Float, regionPx: Int): VerificationOutcome` — the AND-rule in exactly one expression and nowhere else: matched && afterFinding == null && (regionPx < MIN_REGION_PIXELS \|\| changedRatio > REGION_CHANGE_RATIO). `afterFinding` is MeasureReadout.finding (a String?), not a Finding object — corrected during refinement against the real measure() return type. Returns the already-shipped VerificationOutcome rather than inventing a parallel enum. KDoc records two accepted ceilings: a NEW unrelated finding in AFTER also reads Still off (it is a regression and deserves the flag), and a sub-64px region whose finding vanished because a neighbour moved reads Fixed with zero changed pixels. |
| core/verify/BeforeAfterFormat.kt | logic | new |  | The one formatter the repo lacks: 'top ' + deltaLabel(before) + ' → ' + deltaLabel(after) for a delta finding, and plainValueLabels joined by SEPARATOR for the touch-target shape ('height 24dp → 32dp · want 48dp'). Built entirely on F-5's displayDelta/deltaLabel/plainValueLabel/unitOf/SEPARATOR — zero unit conversion is reimplemented. When neither side has a finding it falls back to a plainValueLabel size line (the design's 'placeholder fits · no clipping' is prototype copy with no computable source and is not faked). |
| MirrorStage.Verify + the MirrorStageContent branch + the stage-derivation condition | ui | reuse | components.json#mirror-stage-content | One enum case (MirrorContract.kt:90-99), one branch in MirrorStageContent's when (MirrorContent.kt:124, behind the existing error-card short-circuit), one condition in MirrorUiState.stage (MirrorContract.kt:331-340) placed AFTER the isPickerOpen/Ios guards and BEFORE the `frozen != null -> Frozen` case. Because Verify outranks Frozen, SendFollowUp must clear the verify state before it stages — see the follow-up component. No new Destination, no second MirrorHost call site. |
| VerifyUiState / VerifyRow / VerifyPhase / VerifyError | ui | new | components.json#mirror-stage-content | Value-equal reduced-state types in MirrorContract.kt. VerifyUiState(batchId, deviceLabel, sentAtMs, recapturedAtMs, phase, rows, error); VerifyRow(index, comment, beforeRect, afterRect, resultLine, outcome) — no bitmaps, no labels (MirrorContent resolves every string); VerifyPhase { Loading, Recapturing, Diffing, Ready }; VerifyError { NoLiveSession, NotOnDisk, SizeMismatch, HierarchyUnusable, Disconnected }. An error is always accompanied by an empty rows list — the invariant that keeps an abort from reading as a verdict. |
| VerifyFrames + MirrorViewModel.verifyFrames StateFlow | ui | new | components.json#mirror-host | `public data class VerifyFrames(val before: ImageBitmap, val after: ImageBitmap)` on a plain MutableStateFlow<VerifyFrames?>, handed in as the 5th MirrorHost content-lambda argument. Public because :compose-application reads it (unlike the internal HeldFrame). Honours Deviation 2 (pixels never enter reduced state) while delivering the two bitmaps the design needs. |
| MirrorIntent.SendFollowUp / MarkAllVerified / ExitVerify | ui | new |  | Three new intents beside the already-shipped VerifyBatch(batchId). ExitVerify cancels verifyJob and clears ALL THREE verify fields (VerifyUiState, verifyFrames, verifyAfterHeld) plus pendingFollowUpBatchId, then routes to ResumeLive + a History tab selection — the bugs.json#PP-TD-2 lifecycle-hole guard. |
| MirrorViewModel.verifyBatch (replaces the log.i stub at MirrorViewModel.kt:388) | logic | reuse |  | The seven-step run on verifyJob: Job? (the dumpJob cancellation precedent at :584-585/:613-614), every decode/diff step inside withContext(Dispatchers.Default), device identity re-checked at every suspension point, VerifyPhase reduced as it advances, the AFTER HeldFrame retained in verifyAfterHeld. A second Verify cancels the first (flatMapLatest semantics by hand). |
| MirrorViewModel follow-up re-stage + pendingFollowUpBatchId | logic | reuse |  | In one handler, in this order: cancel verifyJob; capture the still-off rows; set currentFrozenHeld = verifyAfterHeld (this is what makes CropEncoder crop the AFTER pixels with no new code); build a fresh FrozenFrame with the still-off annotations renumbered via mapIndexed to StagedAnnotation(index = i + 1) (FR-47's only conversion point) and promptOverride explicitly null (bugs.json#PP-TD-7); set the three current* flows; reduce state.copy(frozen = …, verify = null) and clear verifyFrames so the stage falls back to Frozen and F-7's send UI takes over; set pendingFollowUpBatchId; call resolveAndSend() UNCHANGED. Its success path consumes the nullable field (null-checked, cleared in a finally) to write FollowUpSent on the ORIGINAL batch. |
| MirrorVerifyStage.kt | ui | new |  | The stateless stage composable: two aspect-correct MirrorFrameView columns inside 296×640 boxes (BEFORE rects neutral, AFTER rects coloured by verdict), the 300px Verification panel of VerificationRows with its 'n of N fixed' header, StatusLine for the four-step strip (the 'Agent fixed' step is a static countless label — no data source, nothing fabricated), footer AppButtons ('Send follow-up · N' hard-disabled at N = 0, 'Mark all as verified'), the '← Back to live' Chip as the single exit, and ErrorState for the five VerifyError cases. Light/dark @Preview pair. |
| VerificationRow / VerificationOutcome | ui | reuse | components.json#verification-row | Already shipped and a near-exact fit: GlyphBadge ✓/! + title + mono delta + the outcome word in Theme.color.sentFixed/warnStillOff. Its own @Preview uses the design's exact strings. F-9 only feeds it, resolving outcomeLabel from strings.xml at the call site per localisation.md, and reuses VerificationOutcome as the verdict type rather than defining a parallel enum. |
| StatusLine | ui | reuse | components.json#status-line | The 34dp multi-step dot+label line with a trailing slot — exactly the design's four-step strip (Sent → Agent fixed → Recaptured → Verified n/N). No new component. |
| MirrorFrameView + AnnotationOverlay + AnnotationMark + GlyphBadge | ui | reuse | components.json#mirror-frame-view | The two frame columns with per-annotation rects and numeric badges. The overlay takes Dp-space rects, so the stage does the px→dp conversion exactly as the existing frozen stage already does (FrameGeometry's render leg — the ONLY density division), derived from the MEASURED frame box rather than the nominal 296×640 (bugs.json#PP-TD-1). |
| ErrorState | ui | reuse | components.json#error-state | Renders all five VerifyError cases as title + description without inventing a new error layout. No separate retry control — the Back-to-live chip plus the History row's Verify button are the retry path. |
| F-5 core/measure toolkit (projectMeasureNodes, measure, deltaLabel, plainValueLabel, displayDelta, unitOf, SEPARATOR) | logic | reuse |  | The entire hierarchy half. projectMeasureNodes runs once per side; measure() per annotation per side does the snap, the NO_MATCH fallback, the gaps and the finding in one call; the format functions produce the design's strings verbatim. Internal and in-module — reuse in place, never copy out. snapTarget and findingOf are NOT called directly. |
| resolveAndSend / assembleEnvelope / CropEncoder / mintSessionId | logic | reuse |  | The entire follow-up send, reused with one status hook and one frozen-held assignment. Crops are taken from the current frozen held bitmap, which the follow-up has just pointed at the AFTER frame — so AC-42's 'from the AFTER frame' is satisfied with zero new code. |
| MirrorHistoryTab.HistoryEntryRow trailing slot | ui | reuse | components.json#MirrorHistoryTab | DELETE the two-line comment at :177-178 and the `if (entry.status == BatchStatusModel.Fixed)` wrapper at :179-185 so AppButton(verifyLabel) renders for every row (FR-37/AC-39), and add `enabled = entry.framePath != null`. Fix the preview KDoc at :239-243 that still advertises the Fixed-only button. |
| MirrorFakes.FakeFrameBitmap | logic | reuse |  | EDIT: its readPixels override is a no-op (MirrorFakes.kt:183-191), which would make every ViewModel-level region read return zeros and every pixel conjunct vacuously false. Give it a `pixel: (x: Int, y: Int) -> Int` constructor parameter (defaulted) and fill the buffer honouring startX/startY/width/height/bufferOffset/stride. Still a plain ImageBitmap interface implementation — no Skiko, no bitmap construction, no build.gradle.kts edit. |

#### dataModel

| entity | fields |
| --- | --- |
| SentBatchModel (reused as the read model — no new type) | ["id: String", "sentAtMs: Long", "deviceLabel: String", "prompt: String", "frameBytes: ByteArray", "thumbPng: ByteArray?", "hierarchyXml: String", "annotations: List<SentAnnotationModel>"] |
| SentAnnotationModel (reused, device pixels) | ["left: Int", "top: Int", "right: Int", "bottom: Int", "comment: String", "cropBytes: ByteArray? (left empty on the read path — nothing consumes it)"] |
| VerifyUiState (new, reduced state, value-equal, no bitmaps) | ["batchId: String", "deviceLabel: String", "sentAtMs: Long", "recapturedAtMs: Long", "phase: VerifyPhase", "rows: List<VerifyRow> (always empty when error != null)", "error: VerifyError?"] |
| VerifyRow (new, reduced state, no labels — MirrorContent resolves copy) | ["index: Int (0-based, history order)", "comment: String", "beforeRect: DeviceRect", "afterRect: DeviceRect", "resultLine: String", "outcome: VerificationOutcome"] |
| VerifyPhase (new enum) | ["Loading", "Recapturing", "Diffing", "Ready"] |
| VerifyError (new enum) | ["NoLiveSession", "NotOnDisk", "SizeMismatch", "HierarchyUnusable", "Disconnected"] |
| VerifyFrames (new, public, NOT reduced state — plain StateFlow only) | ["before: ImageBitmap", "after: ImageBitmap"] |
| MirrorViewModel private fields (not state) | ["verifyJob: Job?", "verifyAfterHeld: HeldFrame? — the AFTER frame the diff ran against, reused verbatim by the follow-up", "pendingFollowUpBatchId: String? — null-checked, cleared in a finally"] |
| history_entry / history_annotation (UNCHANGED — @Database stays version 1, no migration) | ["no new columns", "densityDpi deliberately not persisted; taken from the fresh AFTER dump for both sides"] |

#### interfaces

| name | contract |
| --- | --- |
| HistoryEntryDao.findById | @Transaction @Query("SELECT * FROM history_entry WHERE id = :id") suspend fun findById(id: String): HistoryEntryWithAnnotations? |
| HistoryDataSource.getBatch | suspend fun getBatch(id: String): HistoryEntryWithAnnotations? — bespoke String-keyed method; does NOT reuse ReadableDataSource<T>.getById(Int). |
| BatchFileReader | fun interface BatchFileReader { suspend fun read(path: String): ByteArray? } — never throws; null on missing/unreadable/permission-denied/truncated. |
| HistoryRepository.get | suspend fun get(batchId: String): SentBatchModel? — one-shot, so Result at the use-case boundary and a plain nullable here, matching observe()'s bare-Flow precedent and record()'s throw-and-map contract. |
| GetSentBatchUseCase | suspend operator fun invoke(batchId: String): Result<SentBatchModel> — Failure.Logic.NotFound when absent, Failure.Technical.Database otherwise. |
| UpdateBatchStatusUseCase | suspend operator fun invoke(batchId: String, status: BatchStatusModel): Result<Unit> |
| regionPixels | internal fun ImageBitmap.regionPixels(rect: DeviceRect, frameSize: IntSize): IntArray? — clamped; null for a degenerate (zero-area) rect. |
| changedRatio | internal fun changedRatio(before: IntArray, after: IntArray): Float — requires equal lengths (FR-46 guarantees it); 0f for empty input. |
| verdictOf | internal fun verdictOf(matched: Boolean, afterFinding: String?, changedRatio: Float, regionPx: Int): VerificationOutcome — afterFinding is MeasureReadout.finding; matched is (beforeReadout.nodeId != NO_MATCH_NODE_ID && afterReadout.nodeId != NO_MATCH_NODE_ID). |
| beforeAfterLabel | internal fun beforeAfterLabel(before: MeasureReadout, after: MeasureReadout, densityDpi: Int): String |
| MirrorHost content lambda | content(state, frozenFrame ?: frame, frozenHierarchy, verifyFrames, ::handleIntent) — widens 4→5 args; exactly one call site, RectifyWindow.kt. |

#### stateManagement

Orbit MVI with mirror's two documented deviations. Semantics live in reduced MirrorUiState (stage, VerifyUiState with its phase/rows/error — all value-equal); pixels never do (VerifyFrames rides a plain MutableStateFlow<VerifyFrames?> beside frame/frozenFrame, exactly as Deviation 2 prescribes). handleIntent dispatches VerifyBatch/SendFollowUp/MarkAllVerified/ExitVerify to private handlers, each `= intent { reduce {}; … }`. The long-running work hangs off verifyJob: Job? (the dumpJob precedent) and is cancelled by ExitVerify, a second VerifyBatch, MarkAllVerified, SendFollowUp, or the existing flatMapLatest(sessionDeviceId) teardown on a device change. Two non-state fields ride alongside: verifyAfterHeld (the diffed AFTER frame, reused by the follow-up) and pendingFollowUpBatchId (null-checked, cleared in a finally, so a normal send can never write a status). MirrorStage.Verify is derived ahead of Frozen, which is why SendFollowUp clears the verify state in the same reduction that sets the frozen frame.

#### navigation

- No Destination, no navSavedStateConfiguration entry, no NavigationHost branch — presentation-feature-mirror/CLAUDE.md Deviation 1 explicitly exempts this surface from navigation.md's five-step checklist.
- Entry: History tab Verify button (enabled when framePath != null) → MirrorIntent.VerifyBatch(batchId) → MirrorUiState.stage derives Verify (ordered before Frozen) → MirrorStageContent's when renders MirrorVerifyStage.
- Exit A: the '← Back to live' chip → MirrorIntent.ExitVerify → cancel verifyJob, clear VerifyUiState + verifyFrames + verifyAfterHeld + pendingFollowUpBatchId, then ResumeLive + shellState.selectTab(History) (the LaunchedEffect precedent at RectifyWindow.kt:214).
- Exit B: SendFollowUp clears the same verify fields as part of staging, so the stage becomes Frozen and F-7's existing send flow (including its failure surface) owns the screen; its success path releases the frozen frame and resumes live exactly as it already does.

#### affectedModules

- data-database-impl — EDIT core/dao/HistoryEntryDao.kt (add @Transaction @Query findById(id: String): HistoryEntryWithAnnotations?, no LIMIT copy-paste from observeAll); EDIT source/datasource/HistoryDataSource.kt + its Impl (add suspend fun getBatch(id: String)); ADD desktopTest/HistoryEntryDaoFindByIdTest.kt (present / absent / with-children). No entity change, no @Database version bump, no Migration. Test task: :data-database-impl:desktopTest.
- data-repository-impl — ADD commonMain/source/store/BatchFileReader.kt (fun interface); ADD desktopMain/core/store/FileSystemBatchFileReader.kt (java.io, Dispatchers.IO, never throws); EDIT the BatchStoreProvider expect/actual trio (null-returning stand-ins for android/ios); EDIT di/DataRepositoryImplModule.kt (one binding appended inside the existing group); EDIT source/repository/HistoryRepositoryImpl.kt (add get(batchId) assembling SentBatchModel from the @Relation + reader bytes and re-parsing the stored XML through the existing HierarchySnapshotMapper); ADD commonTest/HistoryRepositoryGetTest.kt; ADD desktopTest/FileSystemBatchFileReaderTest.kt. Test task: :data-repository-impl:desktopTest.
- domain-repository-api — EDIT source/repository/HistoryRepository.kt (add `public suspend fun get(batchId: String): SentBatchModel?`; update updateStatus's KDoc, which currently says 'Nothing in F-8 calls this — it is the hook the verify-and-rediff loop (F-9) advances a batch with').
- domain-core — NO CHANGE. SentBatchModel/SentAnnotationModel/BatchStatusModel already carry everything; Verified and FollowUpSent already exist and are already persisted and rendered.
- domain-usecase-api — ADD source/usecase/history/GetSentBatchUseCase.kt and UpdateBatchStatusUseCase.kt.
- domain-usecase-impl — ADD the two *UseCaseImpl files (thin resultLauncher(errorMapper = Failure.Technical::Database) wrappers); EDIT di/DomainUseCaseImplModule.kt (two bindings APPENDED within the existing history layer group — koin-di.md: load order is significant, never reorder). One NotFound-mapping assertion on GetSentBatchUseCaseImpl only; no per-use-case test by rote.
- presentation-feature-mirror (commonMain, core/) — ADD core/verify/RegionPixels.kt, RegionDiff.kt, VerifyThresholds.kt, VerifyVerdict.kt, BeforeAfterFormat.kt (five files, one concern each — bugs.json#PP-TD-6 is the counter-example not to repeat). EDIT core/measure/MeasureFormat.kt: SEPARATOR `private` → `internal`, nothing else. No other change to core/measure/* — it is reused in place.
- presentation-feature-mirror (commonMain, source/) — EDIT MirrorContract.kt (MirrorStage.Verify at ~:90-99; the Verify condition in the stage derivation at ~:331-340 after the Ios guard and before Frozen; VerifyUiState/VerifyRow/VerifyPhase/VerifyError; VerifyFrames; SendFollowUp/MarkAllVerified/ExitVerify intents; DELETE the stale VerifyBatch KDoc at :490-500); EDIT MirrorViewModel.kt (replace the log.i stub at :388 with the seven-step run; add verifyJob, verifyFrames, verifyAfterHeld, pendingFollowUpBatchId; two new constructor use-case params; the follow-up re-stage; the one hook in resolveAndSend's success path at ~:886); EDIT MirrorHost.kt (pass verifyFrames as the 5th content-lambda argument); EDIT MirrorContent.kt (one new branch in MirrorStageContent's when at ~:124, behind the existing error-card short-circuit; the extra lambda parameter); ADD MirrorVerifyStage.kt (stage composable + light/dark @Preview pair); EDIT MirrorHistoryTab.kt (DELETE :177-185, add enabled = entry.framePath != null, fix the preview KDoc at :239-243); EDIT di/PresentationFeatureMirrorModule.kt (two new use cases into the existing explicit `viewModel { MirrorViewModel(...) }` resolution).
- presentation-feature-mirror (commonTest) — ADD RegionDiffTest.kt, VerifyVerdictTest.kt, BeforeAfterFormatTest.kt, VerifyStageDerivationTest.kt, FollowUpRenumberTest.kt, MirrorViewModelVerifyTest.kt; EDIT MirrorFakes.kt (fake GetSentBatchUseCase / UpdateBatchStatusUseCase, a fake batch fixture, and the FakeFrameBitmap buffer-filling fix; the existing Orbit.configureDefaults pinning and runCurrent() subscription trick are reused as-is). No test constructs an ImageBitmap. Plain-word camelCase backtick names per this module's CLAUDE.md. Test task: :presentation-feature-mirror:desktopTest.
- presentation-core-localisation — EDIT composeResources/values/strings.xml: ADD the mirror_verify_* keys (see i18n.strings); DELETE the stale comment at :182 that documents the Fixed-only gate. English only; no AAPT-style apostrophe escapes (they leak into the UI — fixed once already in commit dae0893).
- presentation-core-ui — NO CHANGE. VerificationRow, StatusLine, MirrorFrameView, AnnotationOverlay, AnnotationMark, GlyphBadge, ErrorState, AppButton (whose `enabled` parameter already exists at AppButton.kt:178) and Chip cover every element of the design.
- presentation-core-styling — NO CHANGE. Theme.color.sentFixed / warnStillOff are identical in light and dark, so NFR-9 needs no new token.
- compose-application — EDIT desktopMain/.../RectifyWindow.kt, exactly ~2 lines: the MirrorHost content lambda gains its 5th parameter (StateFlow<VerifyFrames?>) and forwards it into MirrorStageContent. Nothing testable goes here — this module has no test wiring.
- docs — EDIT presentation-feature-mirror/CLAUDE.md: record core/verify/ and the one-file ImageBitmap rule in Deviation 2's neighbourhood. No other doc file is edited.

### projectContext

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| verification-row | VerificationRow / VerificationOutcome | components.json#verification-row |
| status-line | StatusLine (the four-step strip) | components.json#status-line |
| mirror-frame-view | MirrorFrameView (the two frame columns) | components.json#mirror-frame-view |
| annotation-overlay | AnnotationOverlay (per-annotation rects) | components.json#annotation-overlay |
| annotation-mark | AnnotationMark (rect + numeric badge) | components.json#annotation-mark |
| glyph-badge | GlyphBadge (the ✓ / ! square) | components.json#glyph-badge |
| error-state | ErrorState (the five VerifyError cases) | components.json#error-state |
| app-button | AppButton (Verify row action with its existing `enabled`, plus both footer actions) | components.json#app-button |
| chip | Chip (the '← Back to live' chip) | components.json#chip |
| history-row | HistoryRow (unchanged; only its trailing slot's gate is deleted) | components.json#history-row |
| mirror-host | MirrorHost (content lambda widens 4→5 args; still exactly one call site) | components.json#mirror-host |
| mirror-stage-content | MirrorStageContent (one new when branch) | components.json#mirror-stage-content |
| MirrorHistoryTab | MirrorHistoryTab (gate deletion + enabled) | components.json#MirrorHistoryTab |
| HistoryEntryDao | HistoryEntryDao (gains findById) | components.json#HistoryEntryDao |
| HistoryDataSource | HistoryDataSource (gains getBatch) | components.json#HistoryDataSource |
| HistoryEntryWithAnnotations | HistoryEntryWithAnnotations (the @Relation that already has the data) | components.json#HistoryEntryWithAnnotations |
| HistoryRepository | HistoryRepository (gains get(batchId); updateStatus finally gets a caller) | components.json#HistoryRepository |
| BatchFileStore | BatchFileStore (the shape BatchFileReader copies — do NOT extend it) | components.json#BatchFileStore |
| ThumbnailBitmap | ThumbnailBitmap (precedent that non-trivial pixel work belongs in commonMain with no seam) | components.json#ThumbnailBitmap |
| HistoryFormatting | HistoryFormatting (relative time labels for the header/step strip) | components.json#HistoryFormatting |

#### reuseAssets


#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| theme-color-sent-fixed | Theme.color.sentFixed (green verdict accent, identical light/dark) | styles.json |
| theme-color-warn-still-off | Theme.color.warnStillOff (amber verdict accent, identical light/dark) | styles.json |
| theme-typography-mono-data | Theme.typography.monoData (the result lines and step strip) | styles.json |
| theme-tokens | Theme.spacing / cornerToken / stroke / color.ink,ink2,ink3,line,line2,surface (the whole verify layout) | styles.json |

#### existingFeatures

| id | name | registryRef |
| --- | --- | --- |
| history-batch-tracking | History & Batch Tracking (F-8) | features.json#history-batch-tracking |
| resolve-and-send-mcp | Resolve & Send / MCP handoff (F-7) | features.json |
| measure-mode | Measure mode (F-5) — the layout oracle | features.json |
| drag-annotate-frozen-frame | Drag-to-annotate & frozen frame (FrameGeometry's px↔dp legs) | features.json#drag-annotate-frozen-frame |
| database | Database (Room KMP) — stays at version 1 | features.json#database |
| error-propagation | Failure / Result propagation (resultLauncher) | features.json#error-propagation |
| localisation | Localisation (Compose Resources) | features.json#localisation |

#### newToBuild

- HistoryEntryDao.findById + HistoryDataSource.getBatch (the Room @Relation already has the data; only the query is missing)
- BatchFileReader fun interface + FileSystemBatchFileReader desktop impl + null-returning android/ios stand-ins
- HistoryRepository.get(batchId): SentBatchModel? (reusing the write-path model as the read model)
- GetSentBatchUseCase + UpdateBatchStatusUseCase (+ impls, + Koin bindings appended)
- presentation-feature-mirror/core/verify/: RegionPixels.kt, RegionDiff.kt, VerifyThresholds.kt, VerifyVerdict.kt, BeforeAfterFormat.kt
- MirrorStage.Verify + VerifyUiState/VerifyRow/VerifyPhase/VerifyError + SendFollowUp/MarkAllVerified/ExitVerify intents
- VerifyFrames + MirrorViewModel.verifyFrames StateFlow (the 5th MirrorHost content arg)
- MirrorViewModel.verifyBatch run (verifyJob, verifyAfterHeld) + the follow-up re-stage + pendingFollowUpBatchId
- MirrorVerifyStage.kt composable + the MirrorStageContent branch + light/dark @Preview pair
- mirror_verify_* localisation keys
- Two one-line enablers found during refinement: SEPARATOR `private`→`internal`, and a buffer-filling FakeFrameBitmap.readPixels

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md (root) — module structure, convention plugins, toolchain | Confirms no new Gradle module is needed and which modules are KMP vs plain-JVM. |
| .claude/rules/package-layout.md | core/verify/ is internal-only; source/mirror/ is the public surface; explicitApi() demands explicit visibility and return types on every new declaration — including VerifyFrames, which must be public because the shell reads it. |
| .claude/rules/testing.md | Comma and colon fail compileTestKotlinIosArm64 (the apostrophe does not — corrected by experiment); desktop is the task that runs; :compose-application has no test wiring; no Compose UI-test harness exists; :<module>:build also runs the iosSimulatorArm64 suite. |
| presentation-feature-mirror/CLAUDE.md | Binding and stricter than the root rule for this module: Deviation 1 (no Destination), Deviation 2 (pixels never in reduced state), plain-word camelCase test names, and the two Orbit test-environment facts (configureDefaults pinning, runCurrent() subscription). |
| .claude/rules/domain-layer.md + .claude/rules/error-handling.md | One-shot ⇒ Result<T>, reactive ⇒ bare Flow<T>; every *UseCaseImpl body inside resultLauncher(errorMapper = Failure.Technical::Database), never raw runCatching; ShowError's messageId must be resolved through stringResource rather than extending the hardcoded-Error gap. |
| .claude/rules/mappers.md | The repository-side hierarchy re-parse and the entity→model assembly must never throw; degrade to null. |
| .claude/rules/koin-di.md | One module per Gradle module; initKoin() load order is significant — append the two new use cases and BatchFileReader within their existing layer groups, never reorder. |
| .claude/rules/orbit-mvi.md | handleIntent → private handlers, each `= intent { reduce {}; … }`; the container trailing lambda is init-time only. Mirror's local precedent overrides the four-file shape. |
| .claude/rules/design-tokens.md | No literal Color/dp/TextStyle at any call site; verdict colours are the existing sentFixed/warnStillOff tokens; atom→molecule→organism layering is untouched because no kit component is added. |
| .claude/rules/localisation.md | mirror_verify_* key convention, Res.string.* resolution at the call site (VerificationRow.outcomeLabel takes a raw String), English-only values/strings.xml, and keep a key only as long as something references it — which is why two draft keys were cut. |
| .claude/rules/compose-preview-convention.md | MirrorVerifyStage needs a private <Name>Preview wrapped in AppTheme — the only rendering evidence this repo has. |
| .claude/rules/database.md | The binding reason NOT to add a densityDpi column: a schema change needs a @Database version bump AND a real Migration, and F-8's stay-at-version-1 is explicitly NOT a reusable precedent. |
| .claude/rules/platform-coverage.md | Android + iosArm64 + iosSimulatorArm64 + jvm("desktop"); the Android/iOS BatchFileReader stand-ins must exist for the build to stay green even though the feature is desktop-only. |
| .claude/rules/navigation.md | Read and deliberately NOT applied — Deviation 1 exempts this surface. Recorded so a reviewer does not flag the omission. |
| .claude/rules/version-catalog-hygiene.md | F-9 adds no dependency and no catalog entry; the integer-arithmetic diff is exactly what makes that possible. |

#### skills


#### agents


#### docs

| ref | why |
| --- | --- |
| .pipeline/sdd/docs/verify-and-rediff-loop/inputs/codebase-recon.md | Authoritative for existing symbols, paths and the four gaps — except where this refinement re-read the source and found otherwise (SEPARATOR's visibility, measure()'s return shape, FakeFrameBitmap's no-op readPixels, the gate's exact line range). |
| .pipeline/sdd/docs/verify-and-rediff-loop/inputs/design-verify-view.md | The pixel-level UI contract for MirrorVerifyStage (296×640 columns, 300px panel, the four-step strip, the footer actions). |
| .pipeline/sdd/docs/verify-and-rediff-loop/plan/evaluation.md | C-1 (behavioural thresholds) and C-2 (distinct aborts) are applied and each is traceable to a DoD line and a test. |
| data-handoff-api/CLAUDE.md and mcp-application/CLAUDE.md | Context for the follow-up envelope's wire contract — read-only; F-9 changes neither. |

### errorStates

| state | behavior |
| --- | --- |
| loading | VerifyPhase.Loading (reading the batch off disk) then Recapturing (the ~2.7 s dumpHierarchy) then Diffing (the per-annotation loop). Each phase renders its own label in the Verification panel; the frames appear as soon as VerifyFrames is published, rows fill in as the loop advances. Never a bare indeterminate spinner with no text. |
| empty | A run that produced zero rows (only reachable via an abort, since F-8 never records an annotation-less batch) shows the error copy, 'Send follow-up · 0' hard-disabled, and writes NO status — 'every row is Fixed' is never vacuously true. |
| error | VerifyError.NoLiveSession [C-2, abort (b)] — the batch was never read, no verdict exists and no status is written. ErrorState names the device and tells the user to start mirroring first, then retry. This must read as a precondition, not a failure, and must never appear as N 'Still off' rows (AS-1 as amended; StartMirroringUseCase is stream-only, so there is no frame to diff against). |
| error | VerifyError.NotOnDisk — the row's framePath/hierarchyPath is null or the file is gone/unreadable (a stored path is a hint, never a guarantee). The button is disabled up front when framePath is null; this error covers the file-deleted-after-render race. |
| error | VerifyError.SizeMismatch — the live frame's IntSize differs from the decoded frame.png's (resolution or orientation changed). Checked ONCE, before any per-annotation work (FR-46/AC-40); rows stay empty. |
| error | VerifyError.HierarchyUnusable [C-2, abort (a)] — the AFTER dump failed, or a non-empty snapshot projects to ZERO candidates on either side (the documented Pixel 8 984×2132-vs-1080×2400 display-override case, where projectMeasureNodes discards every candidate on a frame-size disagreement). The run aborts before the per-annotation loop with an EMPTY rows list, so the user sees one honest 'the hierarchy does not line up' message instead of N confident wrong 'Still off' verdicts. Distinct from an empty-to-begin-with snapshot, which degrades per-annotation to NO_MATCH per AC-41. |
| error | VerifyError.Disconnected — sessionDeviceId changed or went null at any suspension point. No status write, the original batch status untouched, and the state is retryable once the device is back (NFR-16/AC-44). |
| error | A follow-up send that fails (handoff.publish failure) leaves the ORIGINAL batch status untouched — FollowUpSent is written only after resolveAndSend reaches its success point — and surfaces through F-7's existing SendPhase.Failed path with no new error surface, which is why SendFollowUp tears the Verify stage down before calling it. |

### observability

#### logs

- MirrorViewModel log.i on verify start: batchId, annotation count, live frame size (replaces the existing stub's single log.i line).
- log.i per terminal outcome: Ready with n/N fixed, or the VerifyError case name — one line per run, not per annotation.
- log.w when projectMeasureNodes returns empty for a non-empty snapshot, including BOTH frame sizes — this is the display-override landmine and it must be diagnosable from a log alone.
- log.i on each status write (batchId, status) so the two previously-unexercised BatchStatusModel values are traceable.
- No per-pixel or per-annotation logging — N region diffs would flood the log for no benefit.

#### metrics

- Wall-clock of the dumpHierarchy step and of the whole diff loop, logged once per run, so the ~2.7 s KDoc budget can be re-checked against real hardware without a profiler.

#### analyticsEvents


### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | The verdict is never colour-only: VerificationRow pairs the accent colour with a ✓ / ! GlyphBadge AND the literal word 'Fixed' / 'Still off'. The AFTER frame's verdict-coloured rects keep their numeric badges, which tie each rect back to a labelled row. |
| A11Y-2 | Both verdict tokens (Theme.color.sentFixed, warnStillOff) are identical in light and dark and already shipped with contrast reviewed — no new colour token, so NFR-9 parity holds by construction. |
| A11Y-3 | 'Send follow-up · N' keeps F-7's ⌘↩ hint and is reachable by keyboard; it is hard-disabled (not merely dimmed) at N = 0 so the keyboard path cannot fire a no-op send. The History row's Verify button uses the same mechanism when framePath is null. |
| A11Y-4 | Every error state renders a title AND a description through ErrorState — no icon-only or colour-only failure. NoLiveSession in particular names the required action ('start mirroring this device'). |
| A11Y-5 | The in-progress phase is announced in text ('Recapturing…', 'Diffing…'), not as a bare spinner, so the ~2.7 s wait is legible rather than ambiguous. |
| A11Y-6 | The '← Back to live' chip is a real focusable control with a text label, not a bare glyph; it is the single documented exit from the stage. |
| A11Y-7 | Result lines use Theme.typography.monoData at the design's size via tokens only — no hardcoded sub-10px text is introduced. |

### performanceBudget

| metric | target |
| --- | --- |
| DumpHierarchyUseCase (the AFTER capture) | ~2.7 s measured on a Pixel 8 — budgeted, not optimised. It must be the only adb round trip in the run (the AFTER frame costs one atomic currentHeld.value read) and it must be visibly staged as VerifyPhase.Recapturing. |
| measure() per annotation per side | ~100 ms for a 300-node dump (F-5's documented KDoc budget), i.e. ~2N×100 ms for the loop; projectMeasureNodes runs ONCE per side, not per annotation, and no extra snapTarget/findingOf call is made. |
| Region diff per annotation | O(w·h) single pass, integer-only, two IntArray allocations (one per side) and no per-pixel allocation. A 200×60 region is 12k pixels — immeasurable next to the dump. |
| Phase visibility | The first phase change reduces within one frame of the Verify click; no step may leave the window without a text label for more than ~100 ms. |
| Thread discipline | Every decode/project/measure/diff step inside withContext(Dispatchers.Default); nothing on the intent context; the UI thread only ever receives reduced value types and two already-decoded bitmaps. |
| Reduced-state churn | Rows are reduced in one batch at the end of the loop (or at most once per annotation), never per pixel; VerifyFrames never enters reduced state at all. |
| Memory | At most two full-res bitmaps plus two per-region IntArrays alive at once; the stored frameBytes are released after decode, crop files are never read, and verifyAfterHeld is cleared on every exit from the stage. |

### i18n

#### locales

- en

#### strings

- mirror_verify_panel_title — 'Verification'
- mirror_verify_summary — '%1$d of %2$d fixed'
- mirror_verify_outcome_fixed — 'Fixed' (passed into VerificationRow.outcomeLabel, which takes a raw String)
- mirror_verify_outcome_still_off — 'Still off'
- mirror_verify_column_before — 'BEFORE'
- mirror_verify_column_after — 'AFTER'
- mirror_verify_send_follow_up — 'Send follow-up · %1$d'
- mirror_verify_mark_all — 'Mark all as verified'
- mirror_verify_back_to_live — '← Back to live'
- mirror_verify_step_sent — 'Sent %1$s'
- mirror_verify_step_agent_fixed — 'Agent fixed' (static and countless — no data source; nothing is fabricated)
- mirror_verify_step_recaptured — 'Recaptured %1$s'
- mirror_verify_step_verified — 'Verified %1$d/%2$d'
- mirror_verify_phase_loading — 'Loading batch…'
- mirror_verify_phase_recapturing — 'Recapturing…'
- mirror_verify_phase_diffing — 'Diffing regions…'
- mirror_verify_error_no_live_session_title / _body — 'Start mirroring first' / names the device
- mirror_verify_error_not_on_disk_title / _body
- mirror_verify_error_size_mismatch_title / _body
- mirror_verify_error_hierarchy_unusable_title / _body
- mirror_verify_error_disconnected_title / _body

#### notes

English-only; presentation-core-localisation/src/commonMain/composeResources/values/strings.xml is the single file. Keys follow <feature>_<element>. Every string is resolved via Res.string.* at the call site — the contract types (VerifyRow, VerifyError) carry no labels. No AAPT-style apostrophe escapes (they leaked into the UI once already; see commit dae0893) — avoid apostrophes in new copy or write them bare. The stale comment at strings.xml:182 describing the Fixed-only Verify gate is deleted with the gate. Two draft keys were CUT in refinement: mirror_verify_retry (the Back-to-live chip plus the History row's Verify button already retry) and mirror_verify_empty (F-8 never records an annotation-less batch, so the state is unreachable) — localisation.md says to keep a key only as long as something references it. Result strings themselves (dp deltas) are produced by F-5's formatters and are intentionally not localised — they are measurement data, same as live measure-mode.

### security

#### dataHandling

- BatchFileReader reads absolute paths that came out of this app's own Room database and were written by FileSystemBatchFileStore under ~/.rectify/history/<sanitised batchId>/. It is not a general file-read API: it is used only for the two stored paths of one batch, and it never throws or surfaces path contents on failure.
- Screenshots, hierarchy XML and comments never leave the machine. The follow-up envelope goes through F-7's existing local file-slot handoff; F-9 adds no network call and no new egress point.
- Decoding stored bytes is wrapped and degrades to null (VerifyError.NotOnDisk) rather than crashing on a corrupt or truncated PNG — null-on-failure is universal in this repo.
- No new persisted data: no new column, no new file on disk, no new preference. The only writes are two BatchStatusModel values through the existing updateStatus.

#### permissions

- None new. The feature requires an already-authorised adb connection and an active mirror session, both established before Verify can succeed.

#### privacy

- Frame pixels live in memory for the duration of the run and in the already-existing on-disk history directory. F-9 neither prunes nor extends that directory — the documented ceiling (nothing prunes ~/.rectify/history) is inherited from F-8 and explicitly not addressed here.

### rollout

#### featureFlags


#### migrations

- None. @Database stays at version 1 and no Migration is written — a deliberate, recorded decision (densityDpi comes from the fresh AFTER dump, because a migration could never retroactively supply density for rows already on disk). Verified against HistoryEntryEntity during refinement: no column is added, so database.md's version-bump-plus-hand-written-Migration requirement is never triggered. Adding the column later remains the written-down upgrade path.

#### backwardCompatibility

Fully backward compatible with every batch F-8 already persisted: the read path uses only columns that already exist, and a row whose framePath is null renders a disabled Verify button rather than a failing one (and a file deleted after render still surfaces VerifyError.NotOnDisk). Existing statuses render unchanged — the status chip stays informational and no longer gates anything. F-7's normal send path is byte-for-byte unchanged apart from one null-checked hook, and a plain send still writes no status.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json#PP-TD-8 | Kotlin/Native release linker OOMs (Java heap space) under ~4 parallel linkReleaseFramework tasks against a 4 GiB daemon heap. Pre-existing, affects every cross-target gate in this repo. | The mandatory ./gradlew :presentation-feature-mirror:build cross-target gate failing with Java heap space rather than a real compile error. | Run the gate with --max-workers=1, exactly as F-7 did. Do not 'fix' the build by weakening the gate. |
| bugs.json#PP-TD-4 | Tautological test (assertEquals(1, PAYLOAD_FORMAT_VERSION)) that would pass under any edit — the test was the defect. | The same failure mode in a VerifyThresholds test: asserting CHANNEL_NOISE_FLOOR == 10 proves nothing and would pass under any retuning. Also watch for the subtler form — an expected value computed from a constant, e.g. assertEquals(REGION_CHANGE_RATIO, changedRatio(a, b)). | [C-1] There is NO test file over the constants. Tests use a constant only to CONSTRUCT an input (a channel delta of floor and of floor+1; a region of MIN_REGION_PIXELS-1 and of MIN_REGION_PIXELS) and assert the resulting BEHAVIOUR, so retuning changes the data and the expectation still holds. A DoD grep enforces it. Thresholds are tunable by design (NFR-17); pinning their literals would actively obstruct tuning. |
| bugs.json#PP-TD-5 | A test whose name and comment claimed a structural property the body never asserted (the body asserted Empty → empty twice). | A VerifyVerdictTest or MirrorViewModelVerifyTest named for 'aborts before any verdict' or 'unmatched node is Still off' while actually asserting something weaker — e.g. only that error != null. | [C-2] Each test name states exactly one claim and the body asserts that claim. The HierarchyUnusable and NoLiveSession tests each assert THREE things: the specific error value, that rows is EMPTY (no verdict was produced), and that UpdateBatchStatusUseCase was called zero times. |
| bugs.json#PP-TD-6 | core/payload file names do not partition their contents — one label function in PayloadLabels.kt while two live in PayloadEstimate.kt, and PayloadGate.kt accumulated three unrelated predicates. | The new core/verify/ package drifting the same way — e.g. the verdict rule leaking into RegionDiff.kt, or thresholds inlined at call sites. | Five files, one concern each, fixed up front: RegionPixels (the ImageBitmap edge), RegionDiff (the metric), VerifyThresholds (the constants), VerifyVerdict (the AND-rule), BeforeAfterFormat (the formatter). The AND-rule exists in exactly one expression in exactly one file. |
| bugs.json#PP-TD-7 | ROOT CAUSE: promptOverride carries no record of the payload it was authored against, so nothing can distinguish a stale override from a deliberate one. | The follow-up re-stage inheriting a stale promptOverride from the original (pre-verify) frozen frame and silently sending the agent the wrong prompt. | The follow-up builds a FRESH FrozenFrame over the retained AFTER HeldFrame with promptOverride explicitly null (comments are re-sent verbatim — the closed decision), so no override can leak across the verify boundary. One test asserts the re-staged frame's promptOverride is null. |
| bugs.json#PP-TD-2 | ClearStaged kept promptOverride, resurfacing a prompt for deleted annotations (fixed in F-7, same defect class as F-1). | The same lifecycle hole in the new handlers — leaving VerifyUiState, verifyFrames, verifyAfterHeld or pendingFollowUpBatchId set after leaving the stage, so a later unrelated send writes FollowUpSent on an old batch or the user is pinned on a stale Verify view (MirrorStage.Verify outranks Frozen). | ExitVerify cancels verifyJob and clears ALL FOUR fields; SendFollowUp clears the three verify fields in the same reduction that stages the follow-up; pendingFollowUpBatchId is additionally cleared in a finally on the send path. One test asserts a plain (non-follow-up) send writes no status at all, and one asserts the stage leaves Verify when the follow-up is staged. |
| bugs.json#PP-TD-1 | A crop cell fitted to a constant 84dp but drawn in weight(1f), clipping the outline at narrow panel widths — arithmetic done against an assumed size rather than a measured one. | The same class of bug in the verify stage: the design fixes the frames at 296×640, but a real device frame has its own aspect ratio, and the panel is resizable. | Render each frame aspect-correct WITHIN a 296×640 box (letterbox, never stretch — stretching a frame in a pixel-verification tool misrepresents the thing being verified), and derive the annotation-rect overlay from the SAME measured frame box via FrameGeometry's render leg, never from the nominal constant. |
| bugs.json#PP-TD-3 | HierarchyRow.matched is exactly `tag != null` at every call site and defaults false, dimming the tree for forgetful consumers. | Not in F-9's touched area — the verify stage uses VerificationRow, not HierarchyRow — but the same defaulting trap applies to the new VerifyRow.outcome if it were ever given a default. Recorded after a full sweep of bugs.json so the no-recurrence check is complete rather than selective. | VerifyRow.outcome has NO default value: every row is constructed by verdictOf, so a forgetful caller is a compile error rather than a silently optimistic verdict. bugs.json#PP-TD-9 (DiscoveryProjection detail gap) was also reviewed and is untouched by F-9. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 1 | Data read path: HistoryEntryDao.findById (@Transaction @Query by String id, no LIMIT copy) + HistoryDataSource.getBatch + desktopTest for present / absent / with-children | [] | ["AC-23", "AC-45"] | S |
| 2 | BatchFileReader fun interface + FileSystemBatchFileReader (java.io, Dispatchers.IO, never throws) + null-returning android/ios stand-ins through BatchStoreProvider + Koin binding appended; desktopTest round-trip against FileSystemBatchFileStore | [] | ["AC-45"] | S |
| 3 | HistoryRepository.get(batchId): assemble the existing SentBatchModel from the @Relation plus reader bytes and re-parse the stored XML via HierarchySnapshotMapper.toModel(HierarchyDumpStdout(xml, "")); its KDoc records the density gap and the migration upgrade path; commonTest with a fake data source and a fake reader (present, missing row, null framePath, unreadable file) | [1, 2] | ["AC-23", "AC-45"] | M |
| 4 | GetSentBatchUseCase + UpdateBatchStatusUseCase (api + impl + Koin appended within the existing history layer group); one test for the NotFound mapping only — no per-use-case test by rote | [3] | ["AC-23", "AC-43"] | S |
| 5 | core/verify pure core: RegionDiff.kt + VerifyThresholds.kt + VerifyVerdict.kt + BeforeAfterFormat.kt, plus the one-word SEPARATOR private→internal widening in core/measure/MeasureFormat.kt. All commonTest-covered over IntArrays, Strings and primitives — no Compose type, no bitmap, and no assertion over a threshold literal (C-1). This is the step that proves the no-seam decision and the step AC-24 lives or dies on | [] | ["AC-24", "AC-41"] | M |
| 6 | core/verify/RegionPixels.kt — the clamp + readPixels edge, ~10 lines, KDoc'd as the one ImageBitmap-touching file and deliberately untested (Skiko is absent from desktopTest). Independent of step 5: it needs only DeviceRect and ImageBitmap, both of which already exist | [] | ["AC-23", "AC-45"] | S |
| 7 | MirrorContract.kt: MirrorStage.Verify, the stage-derivation condition placed after the Ios guard and BEFORE Frozen, VerifyUiState/VerifyRow/VerifyPhase/VerifyError, public VerifyFrames, SendFollowUp/MarkAllVerified/ExitVerify intents, and deletion of the stale VerifyBatch gate KDoc at :490-500; stage-derivation test | [5] | ["AC-23", "AC-40", "AC-44"] | S |
| 8 | MirrorViewModel.verifyBatch: replace the log.i stub at :388 with the seven-step run on verifyJob (held-frame snapshot → batch → size check → AFTER dump → project both sides → per-annotation measure+diff+verdict → terminal status write), retaining verifyAfterHeld, every pixel step under withContext(Dispatchers.Default), device identity re-checked at each suspension point, every abort leaving rows EMPTY. Includes the MirrorFakes prerequisite: teach FakeFrameBitmap to fill the readPixels buffer, or every pixel conjunct is vacuously false and the tests prove nothing | [4, 6, 7] | ["AC-23", "AC-24", "AC-40", "AC-41", "AC-43", "AC-44"] | L |
| 9 | verifyFrames StateFlow + the 5th MirrorHost content-lambda argument + the one ~2-line RectifyWindow.kt call-site edit (nothing testable goes into :compose-application) | [8] | ["AC-23"] | S |
| 10 | MirrorVerifyStage.kt + the MirrorStageContent branch + the mirror_verify_* strings + a light/dark @Preview pair: two aspect-correct frame columns letterboxed in 296×640 boxes with the overlay derived from the MEASURED box (PP-TD-1), the 300px Verification panel of VerificationRows, StatusLine's four-step strip, footer actions, the Back-to-live chip, ErrorState for the five error cases | [9] | ["AC-23", "AC-24", "AC-40", "AC-42", "AC-43"] | L |
| 11 | Delete the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:177-185 and add enabled = entry.framePath != null; fix the preview KDoc at :239-243 and the strings.xml:182 comment — the button renders for every row, is inert only when there is nothing on disk to diff, and fails with NotOnDisk on the delete-after-render race | [7] | ["AC-39"] | S |
| 12 | Follow-up path: cancel verifyJob, install verifyAfterHeld as currentFrozenHeld (this is what makes CropEncoder crop the AFTER pixels), build a fresh FrozenFrame with the still-off annotations renumbered via mapIndexed to index = i + 1 and promptOverride explicitly null, reduce state.copy(frozen = …, verify = null) and clear verifyFrames so F-7's send UI takes over, set pendingFollowUpBatchId, call resolveAndSend() UNCHANGED; consume the pending id in its success path to write FollowUpSent on the ORIGINAL batch, cleared in a finally | [8, 10] | ["AC-42"] | M |
| 13 | Mark-all-as-verified (cancel verifyJob first, force every row to Fixed, write Verified) and the automatic all-Fixed terminal write guarded on a non-empty rows list; tests that a mixed run and a zero-row abort each write NOTHING | [8, 10] | ["AC-43"] | S |
| 14 | Disconnect/cancellation hardening: re-check sessionDeviceId at every suspension point, verify the existing flatMapLatest teardown cancels verifyJob, assert no status write on abort and that Verify is retryable after reconnect | [8] | ["AC-44"] | S |
| 15 | Docs + hygiene: record core/verify/ and the one-file ImageBitmap rule in presentation-feature-mirror/CLAUDE.md; confirm @Database is still version 1 and no Migration exists; grep that no new Color/dp literal, no new catalog entry and no threshold-literal assertion crept in, and that ImageBitmap appears in exactly the expected files | [10, 11, 12, 13] | ["AC-45"] | S |
| 16 | Gates: ./gradlew :presentation-feature-mirror:desktopTest :data-repository-impl:desktopTest :data-database-impl:desktopTest :domain-usecase-impl:desktopTest, then the cross-target build (./gradlew :presentation-feature-mirror:build :data-repository-impl:build --max-workers=1 — bugs.json#PP-TD-8), then the manual end-to-end demo against a real device (send → change → Verify → follow-up → both status writes), waived explicitly if no device/GUI is available | [14, 15] | ["AC-23", "AC-24", "AC-39", "AC-42", "AC-43"] | M |

### testStrategy

#### unit

- :presentation-feature-mirror:desktopTest — RegionDiffTest: two identical IntArrays give 0f; a channel delta of exactly CHANNEL_NOISE_FLOOR is NOT counted while floor+1 is (the boundary asserted from both sides, with the constant used to BUILD the input and never as an expected value — C-1); a chroma-only change at equal luma IS counted (this is why Chebyshev over RGB rather than luma); alpha differences alone are ignored; an all-different array gives 1f; empty input gives 0f. Pure IntArrays — no bitmap is ever constructed.
- :presentation-feature-mirror:desktopTest — VerifyVerdictTest: the AND-rule over its combinations — matched + no afterFinding + ratio above REGION_CHANGE_RATIO is Fixed; matched + no afterFinding + ratio below is StillOff (the agent-did-nothing case, AC-24's headline); matched + afterFinding present is StillOff regardless of ratio; unmatched is StillOff regardless of everything (AC-41); a region below MIN_REGION_PIXELS with no afterFinding and a zero ratio is Fixed (the hierarchy half decides alone); and — the branch's real guard — a region below MIN_REGION_PIXELS WITH an afterFinding is still StillOff, proving the short-circuit bypasses only the pixel conjunct. Inputs are sized and scaled from the constants; no assertion names a literal (C-1).
- :presentation-feature-mirror:desktopTest — BeforeAfterFormatTest: a top-misalignment finding that went to zero renders exactly 'top Δ6dp → 0dp' (U+0394); a touch-target finding that moved but still fails renders 'height 24dp → 32dp · want 48dp' with the U+00B7 SEPARATOR; densityDpi <= 0 degrades to raw px labels via the existing PixelUnits/unitOf behaviour; no-finding-on-either-side falls back to a plain size line and never fabricates the design's prototype 'placeholder fits' copy.
- :presentation-feature-mirror:desktopTest — VerifyStageDerivationTest: MirrorUiState.stage yields Verify when VerifyUiState is present; Verify takes precedence over Frozen and Streaming but not over Picker or IosDeferred; clearing it while frozen is set yields Frozen (the follow-up hand-off), and clearing both returns to the live stage.
- :presentation-feature-mirror:desktopTest — FollowUpRenumberTest: a 2-of-3 still-off selection produces StagedAnnotation indices 1 and 2 (not 0 and 2, not 1 and 3) with the original comments and the AFTER rects, and promptOverride null — the single FR-47 conversion point plus the PP-TD-7 guard.
- :data-database-impl:desktopTest — HistoryEntryDaoFindByIdTest: an inserted batch comes back with its annotation children in index order; an absent id returns null; a batch with zero annotations returns a row with an empty child list. (DAO tests are desktopTest-only by design — they need BundledSQLiteDriver and file IO.)
- :data-repository-impl:desktopTest — HistoryRepositoryGetTest (commonTest source set, runs on desktop): a present row with readable files yields a fully populated SentBatchModel whose hierarchyXml round-trips through HierarchySnapshotMapper; a missing row, a null framePath and a null-returning reader each yield null rather than throwing; the BEFORE snapshot comes back with densityDpi 0 (the documented gap the AFTER-dump fallback exists for).
- :data-repository-impl:desktopTest — FileSystemBatchFileReaderTest: bytes written by FileSystemBatchFileStore read back identical; a missing path, a directory path and an unreadable path each return null without throwing.
- :domain-usecase-impl:desktopTest — one assertion that GetSentBatchUseCaseImpl maps a null repository result to Failure.Logic.NotFound and a thrown exception to Failure.Technical.Database. UpdateBatchStatusUseCaseImpl gets no test of its own — it is a bare resultLauncher wrapper and testing.md explicitly says not to add one per use case by rote.

#### integration

- :presentation-feature-mirror:desktopTest — MirrorViewModelVerifyTest against fakes (FakeFrameBitmap now fills its readPixels buffer, so a BEFORE/AFTER pair can be made identical or different on purpose): a verify with no held frame reduces VerifyError.NoLiveSession, leaves rows EMPTY, never calls GetSentBatchUseCase and calls UpdateBatchStatusUseCase zero times [C-2 abort (b)]; a null batch reduces NotOnDisk; a live frame whose size differs from the stored frame reduces SizeMismatch with rows EMPTY (AC-40 — no per-annotation work ran); a non-empty hierarchy that projects to zero candidates reduces HierarchyUnusable with rows EMPTY and zero status writes [C-2 abort (a)], while an empty-to-begin-with snapshot produces N rows all StillOff (AC-41, the two cases kept provably distinct); a run where every row is Fixed calls UpdateBatchStatusUseCase exactly once with Verified; a mixed run and a zero-row run each call it zero times.
- :presentation-feature-mirror:desktopTest — phase and cancellation: the phases advance Loading → Recapturing → Diffing → Ready in order; a second VerifyBatch cancels the first so only one terminal reduction lands; MarkAllVerified during a run cancels verifyJob first, forces every row Fixed and writes Verified exactly once, and a late completion cannot overwrite it (AC-43/SC-20); ExitVerify clears all three verify fields and the pending id.
- :presentation-feature-mirror:desktopTest — disconnect: a device change mid-run reduces VerifyError.Disconnected, calls UpdateBatchStatusUseCase zero times, and leaves the state retryable (AC-44/SC-21/NFR-16).
- :presentation-feature-mirror:desktopTest — follow-up: SendFollowUp with 2 of 3 still off re-stages exactly those two over the retained AFTER held frame with promptOverride null, leaves MirrorStage.Verify (so F-7's send UI is reachable), re-enters resolveAndSend, and on success writes FollowUpSent exactly once on the ORIGINAL batch id; a failing publish writes no status at all; and a plain ResolveAndSend with no pending follow-up writes no status (the PP-TD-7/PP-TD-2 regression guard).
- Cross-target compile+run gate: ./gradlew :presentation-feature-mirror:build :data-repository-impl:build --max-workers=1 — commonTest compiles for iosArm64 and the iosSimulatorArm64 suite actually runs (Orbit's dispatcher defaults are process-wide and have hidden a real failure in this repo before).

#### ui

- No automated UI coverage exists or is added — there is no Compose UI-test harness in this repo and introducing one is a real infrastructure decision, not a casual addition (testing.md).
- MirrorVerifyStage ships a light/dark @Preview pair per compose-preview-convention.md, feeding a default-constructed VerifyUiState with one Fixed and one Still-off row. VerificationRow's own existing @Preview already renders the design's exact strings.
- AC-39 is verified by inspection (the gate is deleted; the button renders unconditionally and is disabled only on framePath == null) plus the manual demo, not by a UI test — the same ceiling F-8's AC-36 hit.
- The manual end-to-end demo is the only evidence for the rendered stage, the 296×640 letterboxing, the verdict-coloured rects and the four-step strip. If no device or GUI run is available, record an explicit waiver rather than a silent pass (F-8 left nine such steps unexecuted — do not repeat that silently).

#### linkedScenarios

- SC-8
- SC-9
- SC-19
- SC-20
- SC-21
- SC-22
- SC-23
- SC-24

#### coverageGoal

Every pure function in core/verify/ except RegionPixels.kt has at least one behavioural (never tautological, never literal-pinning) commonTest assertion, and every VerifyError case has one ViewModel-level assertion that checks the error value, the EMPTY rows list and the zero status writes together. RegionPixels.kt is deliberately uncovered and kept to ~10 lines precisely because Skiko's absence makes it untestable in this module. No test constructs an ImageBitmap anywhere; FakeFrameBitmap is the only stand-in and it now fills its buffer. MET-3's <5% false-positive rate is explicitly NOT a coverage target — it is unverifiable in v1 (NFR-17) and is recorded as such rather than asserted.

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-23 | ["FR-21", "FR-38", "FR-39", "FR-40", "FR-42"] | ["MirrorViewModel.verifyBatch", "GetSentBatchUseCase", "HistoryRepository.get", "RegionPixels", "RegionDiff", "F-5 projectMeasureNodes + measure", "MirrorVerifyStage", "MirrorStage.Verify"] | ["test — SC-8", "test — SC-19", "MirrorViewModelVerifyTest", "RegionDiffTest", "HistoryRepositoryGetTest"] |
| AC-24 | ["FR-41", "FR-43"] | ["VerifyVerdict.verdictOf", "VerifyThresholds", "RegionDiff.changedRatio", "MeasureReadout.finding (reuse)", "VerificationOutcome (reuse)", "MirrorViewModel follow-up re-stage"] | ["test — SC-8", "test — SC-9", "VerifyVerdictTest (behavioural only, no threshold literal — C-1)", "RegionDiffTest", "FollowUpRenumberTest"] |
| AC-39 | ["FR-21", "FR-37"] | ["MirrorHistoryTab.HistoryEntryRow trailing slot (gate deleted, enabled = framePath != null)", "MirrorContract VerifyBatch KDoc", "strings.xml:182 comment"] | ["inspection — the deleted gate at :177-185", "test — SC-19", "manual demo"] |
| AC-40 | ["FR-46"] | ["MirrorViewModel.verifyBatch step 3 (single IntSize check)", "VerifyError.SizeMismatch", "ErrorState (reuse)"] | ["test — SC-22", "MirrorViewModelVerifyTest — size mismatch leaves rows empty and writes no status"] |
| AC-41 | ["FR-40", "FR-41"] | ["Measure.measure NO_MATCH_NODE_ID fallback (reuse)", "MeasureProjection.projectMeasureNodes (reuse)", "VerifyVerdict.verdictOf first conjunct", "VerifyError.HierarchyUnusable (the distinct abort case — C-2)"] | ["VerifyVerdictTest — unmatched is StillOff", "test — SC-24", "MirrorViewModelVerifyTest — empty snapshot gives N StillOff rows while non-empty-but-rejected aborts with zero rows"] |
| AC-42 | ["FR-43", "FR-47", "FR-45"] | ["MirrorViewModel follow-up re-stage + verifyAfterHeld + pendingFollowUpBatchId", "resolveAndSend / assembleEnvelope / CropEncoder (reuse, unchanged)", "UpdateBatchStatusUseCase", "MirrorVerifyStage footer action"] | ["test — SC-9", "FollowUpRenumberTest", "MirrorViewModelVerifyTest — follow-up writes FollowUpSent once on the original batch and leaves the Verify stage; a plain send writes nothing"] |
| AC-43 | ["FR-44", "FR-45"] | ["UpdateBatchStatusUseCase", "HistoryRepository.updateStatus (reuse, first caller)", "MirrorViewModel markAllVerified (cancels verifyJob first)", "MirrorVerifyStage footer action"] | ["test — SC-20", "MirrorViewModelVerifyTest — all-Fixed writes Verified once; mixed and zero-row write nothing; Mark-all during a run cancels then writes once"] |
| AC-44 | ["FR-38", "FR-45"] | ["verifyJob + the existing flatMapLatest(sessionDeviceId) teardown", "VerifyError.Disconnected", "VerifyPhase"] | ["test — SC-21", "MirrorViewModelVerifyTest — disconnect aborts with zero status writes and a retryable state"] |
| AC-45 | ["FR-39", "FR-47"] | ["BatchFileReader", "HistoryRepository.get (frameBytes from framePath only)", "RegionPixels (reads the full-res frame with stored rects)"] | ["inspection — no crop-N.png or thumbPng read exists on the diff path", "HistoryRepositoryGetTest", "FileSystemBatchFileReaderTest"] |
| NFR-7 | ["FR-45"] | ["HistoryRepository.updateStatus (reuse)", "history_entry (unchanged schema, version 1)"] | ["HistoryEntryDaoFindByIdTest", "inspection — @Database version is still 1 and no Migration file exists", "manual app-restart check (inherited F-8 ceiling)"] |
| NFR-9 | ["FR-42"] | ["VerificationRow (reuse)", "Theme.color.sentFixed / warnStillOff (reuse, identical light/dark)"] | ["inspection — light/dark @Preview pair; no new colour token is added so parity holds by construction"] |
| NFR-15 | ["FR-38", "FR-42"] | ["VerifyPhase in reduced state", "withContext(Dispatchers.Default) around every decode/diff step", "MirrorVerifyStage phase labels"] | ["MirrorViewModelVerifyTest — phase ordering Loading → Recapturing → Diffing → Ready", "inspection — performanceBudget thread discipline"] |
| NFR-16 | ["FR-45"] | ["verifyJob", "flatMapLatest(sessionDeviceId) teardown (reuse)", "terminal-only status writes"] | ["test — SC-21", "MirrorViewModelVerifyTest — no partial status write"] |
| NFR-17 | ["FR-41"] | ["VerifyThresholds (three KDoc'd tunable consts)"] | ["inspection — MET-3 recorded as unverified-in-v1; the DoD grep proves no test asserts a threshold literal; golden-corpus path written down, not built (C-1)"] |
| AS-1 (amended) | ["FR-38"] | ["MirrorViewModel currentHeld.value atomic read", "VerifyError.NoLiveSession"] | ["test — SC-23", "MirrorViewModelVerifyTest — no held frame reduces NoLiveSession with rows EMPTY, never calls the use case and writes no status (C-2)"] |
| AS-2 | ["FR-38"] | ["MirrorVerifyStage header showing the stored deviceLabel against the live device"] | ["inspection — v1 does not match serials; recorded as a known gap and an open question"] |
| AS-3 | ["FR-40"] | ["Measure.measure (reuse, headless, already snaps and already computes the finding)", "MeasureReadout.nodeId / .finding"] | ["BeforeAfterFormatTest", "VerifyVerdictTest", "the existing F-5 measure suite (unchanged)"] |
| AS-4 | ["FR-39", "FR-47"] | ["HistoryRepository.get", "BatchFileReader", "the AFTER-dump density fallback"] | ["HistoryRepositoryGetTest — densityDpi comes back 0 on the stored side", "inspection — no schema change"] |
| AS-5 | ["FR-21", "FR-37"] | ["MirrorHistoryTab gate deletion"] | ["test — SC-19", "inspection — US-9's 'on Fixed batches' line is superseded; FR-21/AC-23/AC-39 are normative"] |

### openQuestions

- Should a later feature give the coding agent an MCP-side signal to write BatchStatusModel.InProgress, or does that value stay permanently unused?
- Should Verify ever support re-targeting a reconnected-but-different device than the one that sent the original batch (would need a persisted device serial, not just deviceLabel)?
- Should the 'AGENT CHANGES' file-diff section come back later via a git-diff-capable MCP tool, or is it permanently out of scope?
- Should a densityDpi column with a real Room migration be added later so historical batches carry their own density, rather than relying on the AFTER-dump fallback indefinitely?
- Should the follow-up envelope reframe the comment for the agent ('still off after your fix: …') via promptOverride, or continue re-sending verbatim (v1 answer: verbatim)?
- Should temporal stabilisation (sampling two AFTER frames ~250 ms apart) be added if manual spot-checks show animation-driven flakiness in the pixel conjunct?
- Should MET-3 tuning move from manual spot-check to a hand-labelled before/after corpus with one parameterised test once such a corpus exists?

### readyForBacklog

true

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| A History row whose framePath is null has nothing to diff. How should the Verify button behave on it? | ["Always render and always enabled; fail at runtime with VerifyError.NotOnDisk (the draft's choice)", "Always render, enabled = entry.framePath != null; NotOnDisk still covers the delete-after-render race", "Hide the button when framePath is null"] | Always render, enabled = entry.framePath != null; NotOnDisk still covers the delete-after-render race | ai-proposed | AC-39 forbids a STATUS gate, not a data precondition. HistoryEntryModel already carries framePath into reduced state and AppButton already has `enabled`, so this costs one argument and zero plumbing. Always-enabled ships a control that is guaranteed to fail — the same dead-affordance bug F-9 exists to delete, just quieter — and hiding reintroduces the invisible affordance from the other direction. |
| MirrorStage.Verify is derived ahead of Frozen, so staging the follow-up (which sets state.frozen) would otherwise pin the user on a stale Verify view and hide F-7's send and send-failure UI. When should the Verify stage be torn down? | ["At the moment SendFollowUp stages — clear VerifyUiState and verifyFrames in the same reduction that sets the frozen frame, then call resolveAndSend()", "Keep the Verify stage visible during the send and teach it to render SendPhase itself", "Tear down only on the send's success path"] | At the moment SendFollowUp stages — clear VerifyUiState and verifyFrames in the same reduction that sets the frozen frame, then call resolveAndSend() | ai-proposed | It is the only option that keeps 'resolveAndSend is reused unchanged' literally true, including its failure surface. Teaching the Verify stage to render SendPhase duplicates F-7's UI; tearing down on success only leaves a failed follow-up stranded behind a stage that no longer has a job running. pendingFollowUpBatchId is a plain field, not reduced state, so it survives the teardown and still writes FollowUpSent. |
| BeforeAfterFormat needs the ' · ' separator, but SEPARATOR is `private const val` inside core/measure/MeasureFormat.kt. How should it be reached? | ["Widen it to `internal const val` in MeasureFormat.kt (one word)", "Duplicate the literal in BeforeAfterFormat.kt", "Put beforeAfterLabel inside MeasureFormat.kt so the private constant is in scope"] | Widen it to `internal const val` in MeasureFormat.kt (one word) | ai-proposed | One word, one source of truth for the design's U+00B7, and it stays inside the internal core/ package so no public surface changes. Duplicating the literal is how two separators drift apart; moving the verify formatter into MeasureFormat.kt repeats exactly the file-partitioning smell bugs.json#PP-TD-6 records. |
