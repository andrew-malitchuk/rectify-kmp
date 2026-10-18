# verify-and-rediff-loop — candidates/candidate-1

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | candidates/candidate-1 |
| status | done |
| producedBy | sdd-build-plan-draft |
| schemaVersion | 1 |
| createdAt | 2026-10-18T21:50:17Z |
| updatedAt | 2026-10-18T21:50:17Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-18T21:50:17Z

## Provenance

### selfHash

b5dc12424ec463d658c2a0909311f0d32fd5cc5df1254270fde1a7fe066e23f5

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | e29fafc0ed03b1a471853efe650050f47d7db2da68a4c1f984fb4a95ae212144 |

## Data

### meta

#### title

Verify & re-diff loop (F-9)

#### version

1

#### status

draft

### goal

Close the Rectify loop: one click on any sent History batch re-captures the live device, diffs it per annotation against the batch's stored frozen frame (pixel-region change-evidence plus an F-5 layout-finding oracle), renders a per-annotation Fixed / Still off verdict, and re-stages exactly the still-off annotations back through F-7's send path — persisting Verified / Follow-up sent through F-8's so-far-unused HistoryRepository.updateStatus hook.

### problemStatement

F-8 shipped a Verify button gated to BatchStatusModel.Fixed, but nothing in v1 ever writes Fixed, so the button is dead on arrival (MirrorHistoryTab.kt:178-186) and the PRD's strongest differentiator — per-annotation, per-region pixel + hierarchy verification — is unbuilt. Users cannot confirm an agent's fix landed without eyeballing two screenshots, and have no tight way to push back just the annotations that are still wrong.

### highLevelDescription

Four real gaps, everything else is reuse. (1) Read one batch WITH its annotations: HistoryEntryWithAnnotations already carries the data, the read mapper drops it — add @Transaction @Query findById(String) to HistoryEntryDao (ReadableDataSource.getById is Int-keyed and useless), HistoryDataSource.getBatch, HistoryRepository.get(batchId): SentBatchModel? (the existing write-path model reused as the read model), GetSentBatchUseCase / UpdateBatchStatusUseCase. (2) Read bytes off disk: BatchFileStore is write-only, so a new `fun interface BatchFileReader { suspend fun read(path: String): ByteArray? }` with a java.io desktop impl and null-returning Android/iOS stand-ins — the one genuinely new seam. (3) Pixel diff: pure IntArray arithmetic in commonMain (RegionDiff.changedRatio) fed by a ~10-line ImageBitmap.readPixels edge function, with NO PixelDiffer seam of any kind — a seam would push the arithmetic into desktopMain where desktopTest has no Skiko and destroy unit-testability. (4) densityDpi is not persisted: take it from the fresh AFTER dump and apply it to both sides — no column, no migration, no schema change. The UI is a new MirrorStage.Verify case rendered inside the ONE existing MirrorHost/MirrorContent composition, with the BEFORE/AFTER bitmaps delivered on a new plain StateFlow<VerifyFrames?> as a 5th content-lambda argument (a ~2-line RectifyWindow.kt edit). Verdict: Fixed iff nodeMatched && afterFinding == null && (regionPx < MIN_REGION_PIXELS || changedRatio > REGION_CHANGE_RATIO) — change-evidence, because the literal AC-24 wording would report an agent that did nothing as having fixed the bug.

### background

F-5 shipped a measure-mode toolkit (snapTarget, projectMeasureNodes, measure, findingOf, deltaLabel, plainValueLabel, displayDelta, SEPARATOR) that is exactly the layout oracle this feature needs — the hierarchy half of the diff is measure() on both sides plus one comparison, and the design's own result strings ('top Δ6dp → 0dp', 'height 24dp → 32dp · want 48dp') decompose verbatim into those functions. F-7 shipped resolveAndSend()/assembleEnvelope/CropEncoder, which the follow-up re-enters unchanged except for one pendingFollowUpBatchId hook. F-8 shipped the history tables, the on-disk ~/.rectify/history/<batchId>/{frame.png,hierarchy.xml,crop-<i>.png} layout, all five BatchStatusModel values and the zero-caller updateStatus hook, and presentation-core-ui already ships VerificationRow/VerificationOutcome whose own @Preview uses the design's exact strings. The three corrections of record carried from Preparation (no PixelDiffer seam; AC-24 change-evidence semantics; AS-1 requires an active mirror session) were forced by the codebase recon after clarify closed and are honoured here verbatim.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/inputs/codebase-recon.md | inputs/codebase-recon.md |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/inputs/design-verify-view.md | inputs/design-verify-view.md |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/preparation/preparation.md | preparation/preparation.json |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/preparation/brainstorm.md | preparation/brainstorm.json |
| code | .pipeline/sdd/docs/verify-and-rediff-loop/preparation/clarify.md | preparation/clarify.json |
| code | presentation-feature-mirror/CLAUDE.md (Deviations 1 and 2 — no Destination; pixels never enter reduced state) |  |
| code | presentation-core-ui/.../kit/molecule/row/VerificationRow.kt (shipped, feed it) |  |
| code | .pipeline/sdd/docs/registry/{components,features,bugs}.json |  |

### scope

#### in

- Delete the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:178-186 so Verify renders and is clickable on every sent batch, plus the three collateral comments (MirrorHistoryTab.kt:240 preview KDoc, strings.xml:182 comment, MirrorContract.kt:490-500 KDoc).
- New read path: HistoryEntryDao.findById → HistoryDataSource.getBatch → BatchFileReader → HistoryRepository.get(batchId): SentBatchModel? → GetSentBatchUseCase; plus UpdateBatchStatusUseCase over F-8's zero-caller updateStatus hook.
- Per-annotation pixel-region diff as pure commonMain IntArray arithmetic (Chebyshev RGB distance, integer-only, one pass, alpha ignored) behind a ~10-line ImageBitmap.readPixels edge function.
- Per-annotation hierarchy/layout diff reusing F-5's snapTarget/projectMeasureNodes/measure/findingOf, with one new before→after formatter built on deltaLabel/plainValueLabel/displayDelta/SEPARATOR.
- The closed three-conjunct AND-rule combining both halves into the already-shipped VerificationOutcome (Fixed/StillOff).
- New MirrorStage.Verify phase inside the existing single MirrorHost/MirrorContent composition: BEFORE/AFTER frame columns with verdict-coloured rects, the 300px Verification panel of VerificationRows, the four-step strip, footer actions, the '← Back to live' chip.
- VerifyFrames on a new plain StateFlow as a 5th MirrorHost content-lambda argument (~2-line RectifyWindow.kt edit).
- 'Send follow-up · N' re-staging exactly the N still-off annotations from the AFTER frame, renumbered 1..N, through resolveAndSend() unchanged plus one pendingFollowUpBatchId hook.
- 'Mark all as verified' override, which cancels verifyJob first.
- Persisting Verified and Follow-up sent via HistoryRepository.updateStatus.
- Abort/error states NoLiveSession, NotOnDisk, SizeMismatch, HierarchyUnusable, Disconnected, each with its own copy.
- VerifyPhase (Loading/Recapturing/Diffing/Ready) progress state and verifyJob cancellation, covering the ~2.7 s dumpHierarchy cost and mid-verify device disconnect.
- densityDpi taken from the fresh AFTER dump and applied to both sides — no schema change, no migration.
- New localisation keys (mirror_verify_*) in the one English strings.xml.

#### out

- The 'AGENT CHANGES' file-diff panel — no data source exists (AnnotationEnvelope carries no file metadata, there is no git/MCP reverse channel); cut for v1 exactly as F-8 cut its activity sub-line. The four-step strip keeps its 'Agent fixed' step as a static, countless label.
- Any PixelDiffer seam — expect/actual, fun interface or otherwise. Explicitly overrides clarify decision #4.
- Reading crop-N.png on the diff path (full-res frame.png + stored rects strictly dominates; this is what makes the 0-based/1-based crop-index conflict vanish) and reading the ≤128px thumbnail BLOB.
- A densityDpi column, a @Database version bump or any Room migration.
- A one-shot/cold-start capture use case — Verify requires an active mirror session (AS-1 as amended).
- A golden before/after corpus, a parameterised threshold-tuning test, or any Compose UI-test harness. MET-3 stays unverified-in-v1 (NFR-17); the tuning path is written down, not built.
- Temporal stabilisation of the AFTER frame (multi-sample to defeat mid-animation capture).
- Writing BatchStatusModel.InProgress or BatchStatusModel.Fixed from any v1 flow.
- Device serial matching / re-targeting Verify to a device other than the one currently mirrored (deviceLabel stays a display label).
- Android/iOS Verify UI or pixel diffing — desktop-only, matching F-7/F-8.
- Batch-level (whole-screen) diffing, or verifying more than one batch at a time.
- Any new colour token, any new domain model type, any new Gradle module, any new version-catalog entry.
- Fixing F-8's observeAll missing-LIMIT debt (out of scope — but findById must not copy that pattern; it reads exactly one row by primary key).

### assumptions

- AS-1 [AMENDED — correction #3, false as originally written]: there is no on-demand capture pipeline. StartMirroringUseCase is stream-only and no one-shot capture use case exists. Verify therefore requires an active mirror session on the device: it reads MirrorViewModel.currentHeld.value atomically (the same freezing pattern stageAnnotation already uses) and fails with VerifyError.NoLiveSession otherwise.
- AS-2: the device used for Verify is the same device that was mirrored when the batch was sent; v1 does not re-target a disconnected/replaced device (deviceLabel is a display label, not a serial, and is not used to enforce this).
- AS-3: F-5's node matching runs headlessly against a stored hierarchy snapshot; findingOf is reused directly as the layout oracle — no new matching algorithm.
- AS-4: F-8's persisted frame.png and hierarchy.xml are sufficient diff inputs; densityDpi is the one known gap and is addressed via the AFTER-dump fallback, not a schema change. crop files are not read at all.
- AS-5: PRD Stage-4's resolution ('Verify is available on any sent batch regardless of status; History status chips remain informational only') is authoritative and supersedes F-8's AC-36 outright; this plan does not re-open it.
- ImageBitmap.readPixels(buffer, startX, startY, width, height, bufferOffset, stride) is a commonMain member (proven by the existing FakeFrameBitmap overriding that exact signature), so a region read is just the annotation rect as arguments.
- FR-46's single frame-size check collapses both sides onto ONE IntSize and ONE density, which is what makes projectMeasureNodes safe to call with a shared frame size and makes one density for both sides coherent.

### constraints

| type | text |
| --- | --- |
| tech | No second MirrorHost call site. Verify is a new stage rendered inside the existing MirrorHost/MirrorContent composition (presentation-feature-mirror/CLAUDE.md Deviation 1: there is no Destination for this surface and there will not be one). |
| tech | [AMENDED — correction #1] Pixel diffing is pure IntArray arithmetic in commonMain (RegionDiff.changedRatio) with a ~10-line ImageBitmap.readPixels edge function. No PixelDiffer seam of any kind — not expect/actual, not a fun interface. BatchFileReader is the one genuinely new seam and only reads bytes off disk. |
| tech | Skiko trap: presentation-feature-mirror's desktopTest has no Skiko native library, so ImageBitmap(w, h) throws LibraryLoadException there. Exactly ONE file (core/verify/RegionPixels.kt) may mention ImageBitmap; everything else takes IntArrays. No test ever constructs a bitmap. |
| tech | The diff reads the full-resolution stored frame.png only — never crop-N.png, never the ≤128px thumbnail BLOB (AC-45). |
| tech | Hierarchy/layout diffing reuses F-5's snapTarget / projectMeasureNodes / measure / findingOf / deltaLabel / plainValueLabel / displayDelta / SEPARATOR in place (they are internal to presentation-feature-mirror). Never copy them out, never reimplement unit conversion. |
| tech | [CORRECTED — the recon was wrong here] Adding the Verify stage DOES require a ~2-line RectifyWindow.kt edit: the MirrorHost content lambda widens by one parameter (a 5th StateFlow<VerifyFrames?>), the same precedent as its earlier 3→4 widening for frozenHierarchy. |
| tech | Pixels never enter reduced state (presentation-feature-mirror/CLAUDE.md Deviation 2). VerifyFrames rides a plain StateFlow beside frame/frozenFrame; MirrorUiState.history remains the only audited exception and is not extended. |
| tech | No database schema change or migration. database.md requires a @Database version bump AND a hand-written Migration for any new column, and F-8's stay-at-version-1 was a one-off explicitly flagged as NOT reusable. densityDpi comes from the fresh AFTER dump. |
| tech | domain-layer / error-handling: one-shot operations return Result<T>, reactive ones return a bare Flow<T> (never Flow<Result<T>>); every *UseCaseImpl body runs inside resultLauncher(errorMapper = Failure.Technical::Database), never raw runCatching. Mappers never throw; null-on-failure is universal — a failed image never fails the operation. |
| tech | orbit-mvi (mirror's local precedent): handleIntent dispatches to private handlers, each `= intent { reduce {}; postSideEffect() }`; the container(...) trailing lambda is init-time only. package-layout + explicitApi(): core/ is internal-only, source/ is public, explicit visibility AND return type everywhere. |
| tech | koin-di: one module per Gradle module; initKoin()'s load order is significant — append new bindings within their existing layer group, never reorder. |
| tech | localisation: every piece of copy is a Res.string.* key resolved at the call site (VerificationRow.outcomeLabel takes a raw String, so the localised word is resolved by the caller). Key convention mirror_verify_*; no AAPT-style \' escapes in strings.xml. |
| tech | testing: no comma or colon in backtick test names (they fail compileTestKotlinIosArm64); desktop is the target that runs; :compose-application has no test wiring, so nothing testable may live there; no Compose UI-test harness exists, so the stage is covered by light/dark @Preview pairs per compose-preview-convention.md. |
| tech | design-tokens: no literal Color(0x…), no raw .dp, no hardcoded TextStyle at a call site. Verdict colours are Theme.color.sentFixed / warnStillOff, which are already identical in light and dark — NFR-9 is satisfied by using them and adding no new token. |
| business | Desktop (macOS) only in v1, matching F-7/F-8 and NFR-10. |
| time | dumpHierarchy costs ~2.7 s on a Pixel 8 and measure() is ~100 ms per 300-node dump (×2 per annotation), so the run must be cancellable and visibly staged — a naive implementation reads as a frozen window. |

### dependencies

| on | note |
| --- | --- |
| F-7 (resolve-and-send-mcp) | The one-click follow-up reuses resolveAndSend() / MirrorIntent.ResolveAndSend / assembleEnvelope / CropEncoder / mintSessionId unchanged except for one pendingFollowUpBatchId hook on the success path. Crops come from the AFTER frame for free because assembleEnvelope crops the current frozen held bitmap. |
| F-8 (history-batch-tracking) | Consumes HistoryRepository.updateStatus (zero callers today), the stored frame.png/hierarchy.xml, BatchStatusModel.Verified/FollowUpSent (already persisted and rendered, no schema change) and the inert MirrorIntent.VerifyBatch stub + verifyBatch handler seam. Inherits two review findings: the 0-vs-1-based crop index (resolved by never reading crops, FR-47) and observeAll's missing LIMIT (explicitly NOT to be copied into findById). |
| F-5 (measure-mode) | The hierarchy half IS F-5: snapTarget matches the node on both frames, projectMeasureNodes projects both snapshots against the one shared frame size, measure()/findingOf is the layout oracle, and deltaLabel/plainValueLabel/displayDelta/SEPARATOR produce the design's result strings verbatim. Only one before→after formatter is missing. |
| presentation-core-ui kit (VerificationRow, StatusLine, MirrorFrameView, AnnotationOverlay, ErrorState, AppButton, Chip) | Every visual element of the design already exists. F-9 adds zero kit components. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Threshold tuning is unvalidated (MET-3). CHANNEL_NOISE_FLOOR / REGION_CHANGE_RATIO / MIN_REGION_PIXELS are a first guess reasoned from antialiasing, caret and scroll cases, not measured. | high | med | Three named consts in one file, each KDoc'd with its ceiling and tuning method; 'Mark all as verified' is always available as the human override; recorded unverified-in-v1 per NFR-17; the golden-corpus upgrade path (a hand-labelled before/after corpus under commonTest/resources replayed by one parameterised test) is written down, not built. |
| A display-override device (the documented Pixel 8 984×2132-vs-1080×2400 case) makes projectMeasureNodes discard the whole hierarchy, which would otherwise emit N confident, wrong 'Still off' verdicts — the most expensive lie a verification tool can tell. | med | high | Explicit non-empty-but-projects-to-zero check on BOTH sides → VerifyError.HierarchyUnusable, abort before any verdict. Kept distinct from the empty-snapshot case, which still degrades per-annotation to NO_MATCH exactly as AC-41 requires. |
| The Skiko trap: if any diff code drifts into an ImageBitmap-typed helper, the feature's own tests stop compiling or throw LibraryLoadException. | med | high | Exactly one file (RegionPixels.kt, ~10 lines) mentions ImageBitmap, with a KDoc note saying why; RegionDiff/VerifyVerdict/BeforeAfterFormat take primitives only; no test constructs a bitmap. |
| dumpHierarchy costs ~2.7 s and N region diffs follow, so a naive implementation reads as a frozen window and the user clicks Verify again. | high | med | VerifyPhase (Loading → Recapturing → Diffing → Ready) in reduced state drives a visible progress UI (NFR-15); all decode/diff under withContext(Dispatchers.Default); verifyJob cancellable from ExitVerify, a second Verify, MarkAllVerified or a device change (the dumpJob precedent). |
| Reusing resolveAndSend() as 'unchanged' needs a pendingFollowUpBatchId hook; a sloppy edit could make every normal send try to write FollowUpSent. | low | med | Nullable field, null-checked, cleared in a finally; one ViewModel test asserting a plain (non-follow-up) send writes no status at all. |
| The 0-based (history) vs 1-based (envelope) crop index resurfaces when renumbering the follow-up annotations. | low | med | Never read crop-N.png, so there is exactly one conversion point: the mapIndexed building StagedAnnotation(index = i + 1). One test over a 2-of-3 still-off selection asserting indices are 1 and 2, not 0 and 2. |
| Verify requiring a live mirror session (AS-1 corrected) reads as a bug on the History tab ('I clicked Verify and got an error'). | med | low | VerifyError.NoLiveSession renders a specific, actionable ErrorState naming the device to start mirroring, not a generic failure. |
| Scope creep: F-9 touches data, database, domain, repository, presentation and the shell call site at once. | med | med | Steps are ordered so the read path (1-4) and the pure verify core (5-6) are independently testable and mergeable before any UI exists; the UI steps (7-11) cannot start the arithmetic over. |
| An annotation that was already correct and that the agent correctly left alone reads 'Still off' under change-evidence semantics. | med | low | Taken knowingly as the cheap error — a false 'Still off' costs one click of 'Mark all as verified'; a false 'Fixed' destroys trust in the whole tool. Documented in VerifyVerdict's KDoc as the accepted ceiling. |
| A deliberate `wm density` change between send and verify silently relabels the BEFORE side in the new density. | low | low | Unmeasurable in v1 (density was never persisted) and strictly better than labelling everything in raw px. Recorded as a data gap with the densityDpi-column migration as the written-down upgrade path. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-21 | On any sent batch (not gated on a 'Fixed' status, since nothing in v1 is confirmed to set one), provide a 'Verify' action that re-captures the live screen and diffs it, per annotation box, against the original frozen frame; matches mark the annotation resolved, mismatches surface a one-click follow-up that re-stages that single annotation for re-send. |
| FR-37 | Remove the BatchStatusModel.Fixed gate on the History row's Verify button (MirrorHistoryTab.kt HistoryEntryRow trailing slot) so it renders and is clickable for every sent batch regardless of status. |
| FR-38 | On Verify click, enter a Verify phase that re-captures the live device frame and hierarchy for the device that sent the batch (AS-1/AS-2, AS-1 as amended below), reusing the existing mirror capture pipeline rather than a new one. |
| FR-39 | [AMENDED — correction #1, overrides clarify decision #4] For each annotation, read the matching region from the frozen and re-captured frames via ImageBitmap.readPixels(startX, startY, w, h) — a commonMain member, with the annotation rect as the region arguments — and compute the pixel-region diff as pure IntArray arithmetic in commonMain (no PixelDiffer seam, no expect/actual, no fun interface for the diff itself). A separate fun interface BatchFileReader (desktop java.io impl, null-returning Android/iOS stand-ins, matching the BatchFileStore/CropEncoder pattern) is added solely to read frame.png and crop files off disk. |
| FR-40 | For each annotation, re-run F-5 measure-mode's node matching against the re-captured hierarchy and compare it to the original stored measurement, producing a dp-delta result string in the same format as live measure-mode (e.g. 'top Δ6dp → 0dp'). |
| FR-41 | [AMENDED — correction #2, change-evidence semantics] Mark an annotation's verdict 'Fixed' iff: the hierarchy node matched on both sides AND the matched node's layout finding is gone in the AFTER measurement AND the pixel region shows change-evidence (regionPx < MIN_REGION_PIXELS, where the ratio is statistically meaningless and the hierarchy half decides alone, OR changedRatio > REGION_CHANGE_RATIO). Anything else — including an unmatched hierarchy node — is 'Still off', never silently skipped as 'Fixed'. |
| FR-42 | Render the Verify view per design-verify-view.md as a new phase inside the existing MirrorHost/MirrorContent composition (no second MirrorHost call site): two 296×640 frames (BEFORE/AFTER) with per-annotation rects coloured by verdict, a 300px 'Verification' panel with result rows and verdict words, an 'AGENT CHANGES' section cut for v1 (see out-of-scope), footer actions 'Send follow-up · N' and 'Mark all as verified', a four-step header strip, and a '← Back to live' chip that resets to the live mirror with the History tab selected. |
| FR-43 | 'Send follow-up · N' re-stages exactly the N still-off annotations (plural; AC-42) into a new AnnotationEnvelope sourced from the re-captured (AFTER) frame and its crops — not the stale frozen frame — renumbered 1..N for the envelope, and re-enters F-7's send path via MirrorIntent.ResolveAndSend by reusing resolveAndSend() unchanged except for one pendingFollowUpBatchId hook that writes Follow-up sent on its success path. |
| FR-44 | 'Mark all as verified' force-sets every annotation's verdict to Fixed without further diffing and writes the batch status to Verified; if a run is still in progress it cancels verifyJob first so a late completion cannot overwrite the override. |
| FR-45 | Persist verify outcomes through HistoryRepository.updateStatus: Verified when a run ends with every annotation Fixed (or via the Mark-all override), Follow-up sent immediately after a follow-up send succeeds (written on the ORIGINAL batch; the follow-up itself records its own new Sent row). No v1 code path writes In progress or BatchStatusModel.Fixed — that remains an intentional gap (see out-of-scope). |
| FR-46 | If the re-captured frame's pixel dimensions differ from the stored frozen frame's, abort the diff before any per-annotation comparison runs and surface an error state (SizeMismatch) rather than comparing misaligned regions. |
| FR-47 | Reconcile the crop-index convention mismatch (0-based in history storage, 1-based in the AnnotationHandoff envelope) by never reading crop-N.png on the read side at all — the diff loop reads full-res frame.png plus stored rects, which strictly dominates the crops and makes the conflict vanish there. The only conversion point is the follow-up envelope build (mapIndexed → StagedAnnotation(index = i + 1)), where history's 0-based iteration order is renumbered 1-based for F-7. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. Satisfied in-place by the already-shipped VerificationRow/VerificationOutcome molecule and Theme.color.sentFixed/warnStillOff tokens. |
| NFR-15 | performance | Re-capture and the per-annotation diff loop run off the UI thread (Dispatchers.Default, consistent with AnnotationHandoff's existing suspend contract) and show a visible in-progress state (VerifyPhase: Loading/Recapturing/Diffing/Ready), since N region diffs plus a ~2.7s hierarchy dump can take long enough to need one. |
| NFR-16 | reliability | A device disconnect mid-verify aborts the run cleanly: no partial status write, the original batch status is untouched, and the user lands in a retryable state rather than a stuck spinner. Reuses the existing flatMapLatest(sessionDeviceId) teardown that already cancels in-flight jobs. |
| NFR-17 | maintainability | MET-3's <5% false-positive target is not automatically verifiable in this codebase in v1 — there is no golden before/after dataset and no Compose UI-test harness. Diff thresholds (CHANNEL_NOISE_FLOOR, REGION_CHANGE_RATIO, MIN_REGION_PIXELS) are named tunable constants with stated ceilings, validated by manual spot-check only; MET-3 is recorded as unverified-in-v1 rather than asserted by an automated test. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-23 | Given any sent batch, when the user clicks 'Verify', then the app re-captures the live screen and diffs it, per annotation box, against the stored frozen frame using a pixel-region diff plus a hierarchy/layout diff of the matched node. | must | test |
| AC-24 | [AMENDED — correction #2, change-evidence semantics replaces the self-defeating literal wording] Given a Verify diff, an annotation is marked 'Fixed' only when its hierarchy node matches, its layout finding is gone in the AFTER measurement, AND its pixel region shows change-evidence (regionPx < MIN_REGION_PIXELS, or changedRatio > REGION_CHANGE_RATIO); given any of these fails, the annotation is 'Still off' and a one-click follow-up re-stages exactly the still-off annotations for re-send. The original wording ('no material difference → resolved') is rejected because it would report an agent that changed nothing as having fixed the bug. | must | test |
| AC-39 | Given the History tab, when a row's batch status is anything other than Fixed (e.g. Sent), then the Verify button is still rendered and clickable — the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:179 is removed, superseding F-8's AC-36. | must | inspection |
| AC-40 | Given Verify is clicked, when the re-captured frame's pixel dimensions differ from the stored frozen frame's (resolution or orientation changed), then no per-annotation diff runs and an error state is shown instead of a misaligned comparison. | must | test |
| AC-41 | Given a Verify run, when an annotation's original hierarchy node cannot be matched in the re-captured hierarchy (moved, renamed, or removed), then that annotation's verdict is 'Still off' regardless of its pixel-region diff result. | must | test |
| AC-42 | Given at least one annotation is 'Still off' after Verify, when the user clicks 'Send follow-up · N', then exactly the N still-off annotations are re-staged from the re-captured (AFTER) frame and crops and sent through F-7's existing send path, and the batch status updates to Follow-up sent. | must | test |
| AC-43 | Given a Verify run completes with every annotation 'Fixed', or the user clicks 'Mark all as verified', then the batch status updates to Verified via HistoryRepository.updateStatus. | must | test |
| AC-44 | Given a Verify run is in progress (re-capture or diffing), when the device disconnects, then the run aborts, the original batch status is left unchanged, and the user can retry Verify once reconnected. | must | test |
| AC-45 | Given the diff reads image bytes for comparison, then it reads the full-resolution stored frame.png (never crop-N.png, never the ≤128px thumbnail BLOB), reusing stored rects for region extraction. | must | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Start mirroring the device", "Open History tab", "Locate a sent batch", "Click Verify", "Phase shows Recapturing then Diffing", "Diff runs per annotation against the frozen frame", "Every annotation reads Fixed and the batch status becomes Verified"] |
| SC-9 | Follow-up re-send after a failed verify | edge | ["Verify finds a remaining mismatch for one or more annotations", "Footer reads 'Send follow-up · N' with N = still-off count", "Click it", "Exactly those N annotations are re-staged from the AFTER frame, renumbered 1..N", "resolveAndSend() publishes the envelope", "The original batch flips to Follow-up sent while the follow-up records its own new Sent row"] |
| SC-19 | Verify a batch whose status is still Sent | edge | ["Open History tab", "Locate a batch with status Sent (not Fixed)", "Confirm the Verify button is present and clickable", "Click Verify", "Diff runs exactly as it would on any other status"] |
| SC-20 | Mark all as verified override | happy | ["Open the Verify view with 2 of 3 annotations Still off", "Click 'Mark all as verified'", "Any in-flight verifyJob is cancelled first", "All annotations show Fixed", "Batch status updates to Verified"] |
| SC-21 | Device disconnects mid-verify | error | ["Click Verify on a sent batch", "Re-capture or diffing is in progress", "Device disconnects", "Verify run aborts with VerifyError.Disconnected", "Original batch status is unchanged and Verify can be retried after reconnect"] |
| SC-22 | Resolution mismatch aborts the diff | error | ["Click Verify on a batch sent at one device resolution/orientation", "The live frame is at a different resolution/orientation", "The single IntSize check fails before any per-annotation work", "VerifyError.SizeMismatch is shown instead of a misaligned result"] |

### definitionOfReady

- Preparation signed off with readyForPlan: true and all three corrections of record (no PixelDiffer seam; AC-24 change-evidence; AS-1 live session required) carried verbatim.
- codebase-recon.md accepted as ground truth for existing symbols and paths — no re-derivation of the four gaps.
- design-verify-view.md available as the pixel-level UI contract, with the AGENT CHANGES panel confirmed cut.
- An Android device reachable over adb with a live mirror session, so the manual verification demo can actually run (F-8's demo steps were waived for lack of a GUI run — do not repeat that if a device is available).
- The six closed brainstorm survey decisions and nine closed clarify decisions are read and not re-opened.

### definitionOfDone

- AC-23 and AC-24 (the must-pass pair) are each covered by at least one green commonTest assertion in presentation-feature-mirror.
- The BatchStatusModel.Fixed gate and all three collateral comments are gone; grep for 'BatchStatusModel.Fixed' returns no gate in presentation code.
- No file outside presentation-feature-mirror/core/verify/RegionPixels.kt mentions ImageBitmap for diffing purposes, and no test constructs an ImageBitmap.
- ./gradlew :presentation-feature-mirror:desktopTest :data-repository-impl:desktopTest :data-database-impl:desktopTest :domain-usecase-impl:desktopTest is green.
- Cross-target gate green: ./gradlew :presentation-feature-mirror:build :data-repository-impl:build --max-workers=1 (the --max-workers=1 workaround for bugs.json#PP-TD-8's Kotlin/Native linker OOM).
- MirrorVerifyStage ships a light/dark @Preview pair per compose-preview-convention.md.
- Every new string is a Res.string.mirror_verify_* key in the one English strings.xml, with no AAPT-style apostrophe escapes.
- @Database version is still 1 and no Migration file was added.
- The three threshold constants each carry a KDoc naming their ceiling and the manual tuning method, and NFR-17/MET-3 is recorded as unverified-in-v1 in the verdict rather than asserted.
- A manual end-to-end demo against a real device: send a batch, let an agent (or a hand edit) change the UI, Verify, confirm the verdicts, send a follow-up, confirm both status writes. If no device/GUI is available, record it as an explicit waiver rather than a silent pass.

### successMetrics

- Every sent batch shows a clickable Verify action regardless of status (AC-39) — zero batches stuck behind an unreachable gate.
- MET-3 (<5% false positives) tracked via manual threshold spot-check only in v1; not asserted by an automated test (NFR-17).
- A verify run on a 3-annotation batch completes within ~4 s end to end (dominated by the one ~2.7 s dumpHierarchy), with a visible phase change inside the first frame after the click.
- Zero new Gradle modules, zero new version-catalog entries, zero new kit components, zero new colour tokens, zero schema migrations.

### architecture

#### approach

A thin new read path down the existing layer stack, one new pure internal package in presentation-feature-mirror (core/verify/), one new stage case inside the existing MirrorHost composition, and one new plain StateFlow for the two bitmaps. The verify run is a single cancellable suspend function on verifyJob with seven ordered steps: (1) snapshot sessionDeviceId + currentHeld.value atomically — null held ⇒ NoLiveSession; (2) GetSentBatchUseCase(batchId) — null/failure ⇒ NotOnDisk; (3) decodeToImageBitmap the stored frameBytes and compare its IntSize to the live HeldFrame's — disagree ⇒ SizeMismatch, abort before any per-annotation work (from here there is exactly ONE IntSize and ONE density for both sides); (4) DumpHierarchyUseCase(deviceId) for the AFTER hierarchy, phase = Recapturing (the ~2.7 s cost); (5) projectMeasureNodes both snapshots against that one shared IntSize — non-empty-but-zero-candidates on either side ⇒ HierarchyUnusable and abort, while an empty-to-begin-with snapshot degrades per-annotation to NO_MATCH (AC-41); (6) per annotation i (history's 0-based order): measure() both sides, regionPixels + changedRatio, verdictOf, beforeAfterLabel — then publish VerifyFrames and reduce the rows; (7) all rows Fixed ⇒ UpdateBatchStatusUseCase(batchId, Verified), otherwise write nothing. Device identity is re-checked at every suspension point; a change or null ⇒ Disconnected with no status write.

#### rationale

Every choice is the highest rung of the ladder that holds. Reuse an existing commonMain API (ImageBitmap.readPixels) over inventing a seam — a seam here would be an interface with one implementation AND would push the arithmetic into desktopMain where desktopTest has no Skiko, destroying the testability that is the whole point. Reuse an existing oracle (F-5's findingOf) over a new layout-diff algorithm. Reuse an existing model (SentBatchModel) over a new read-model type. Reuse an existing composition (MirrorStage) over a new Destination. Reuse an existing send path (resolveAndSend) over a new envelope pipeline. Reuse an existing molecule (VerificationRow, whose @Preview already uses the design's exact strings) over a new row. The only genuinely new seam is BatchFileReader, because BatchFileStore is write-only and there is no okio/kotlinx-io in the catalog — that gap is real and is not the pixel gap. Status writes happen last and only at terminal points, which makes NFR-16's 'no partial status write' true by ordering rather than by a transaction.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| fun interface PixelDiffer with a desktopMain implementation (the shape a reviewer pattern-matches for) | One interface, one implementation, no second caller — the exact shape the repo's own rules forbid — and it pushes the arithmetic into desktopMain, which STILL has no Skiko, so it buys nothing for testability and costs the ability to test the diff at all. CropEncoder exists because AWT ImageIO has no commonMain equivalent; BatchFileStore because there is no okio in the catalog. Neither justification applies to integer subtraction. |
| expect/actual PixelDiffer exactly as clarify decision #4 states | Strictly dominated: the repo reserves expect/actual for DatabaseBuilder and SettingsProvider only, it needs an actual in four source sets for identical code, and it inherits every con of the fun-interface option plus more files. Recorded only so the override is traceable. |
| Pixel half as similarity — Fixed when the region still matches BEFORE within threshold (AC-24 as literally worded) | An agent that does nothing at all gets every findings-free annotation marked Fixed — the single worst outcome this feature can produce, and precisely the false-positive class MET-3 names. AC-24's wording is amended instead of implemented as written. |
| Pixel half advisory only — verdict from the hierarchy half alone | Genuinely the laziest option and tempting (the design never displays a pixel percentage), but it deletes half of FR-41/AC-23 and goes blind to every defect with no geometry signature: wrong colour, wrong text, wrong icon, clipping, overlap. |
| Add a densityDpi column to history_entry with a @Database version bump and a hand-written Migration | A migration cannot retroactively supply density for rows already on disk, so the live-density fallback must be built either way — building only the fallback is strictly less code for strictly the same coverage. Correct direction, wrong release; recorded as the upgrade path. |
| Build a one-shot CaptureFrameUseCase so Verify works from a cold History tab (making AS-1 true) | A new adb path down through data-device, a new use case, new failures to surface and a duplicated decode path — a feature-sized detour inside an already-XL feature, with no second caller. Right thing to build the day one appears. |
| Let a non-empty hierarchy that projects to zero candidates degrade per-annotation to no-match (AC-41 applied literally) | It would emit N confident, wrong 'Still off' verdicts from a tooling coordinate-space mismatch and push the user into a pointless follow-up send. One extra error case against a whole class of silent wrong answers. |
| Bundle frame + frozenFrame + verifyFrames into one MirrorFrames holder to keep the content lambda at 4 args | A bigger diff across unrelated call paths for a cosmetic win. The lambda already widened 3→4 once; widening it again is the established precedent and there is exactly one call site. (Putting the bitmaps in reduced state is forbidden outright by Deviation 2.) |
| Re-gate the Verify button on entry.framePath != null instead of on status | Kept as a noted option, not taken: AC-39 just deleted a status-shaped gate, and adding a different gate in the same commit invites the same dead-affordance bug from the other direction. The button always renders and fails with a specific NotOnDisk error. Disabling (rather than hiding) is the reasonable second choice if review disagrees. |
| Normalise differently-sized regions with ThumbnailBitmap.cropAndScale before diffing | Unnecessary: FR-46 guarantees one shared frame size before any per-annotation work runs, so both regions are the same dimensions by construction. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| HistoryEntryDao.findById | data | new | components.json#HistoryEntryDao | @Transaction @Query("SELECT * FROM history_entry WHERE id = :id") returning HistoryEntryWithAnnotations?. The @Relation already carries the annotations; only the query is missing. Explicitly NOT ReadableDataSource.getById (Int-keyed, useless for String batch ids) and explicitly NOT a copy of observeAll's missing-LIMIT pattern — it reads exactly one row by primary key. |
| HistoryDataSource.getBatch(id: String) | data | new | components.json#HistoryDataSource | Bespoke String-keyed read method on the existing public datasource, delegating to the DAO. Returns null when absent. |
| BatchFileReader | data | new |  | `fun interface BatchFileReader { suspend fun read(path: String): ByteArray? }` in data-repository-impl beside BatchFileStore. One abstract method so SAM conversion survives (adding read() to BatchFileStore would destroy its fun-interface-ness). Never throws: missing, unreadable, permission-denied or truncated all return null. The only genuinely new seam in F-9. |
| FileSystemBatchFileReader (desktopMain) + null-returning android/ios stand-ins | data | new | components.json#BatchFileStore | java.io implementation under Dispatchers.IO, same shape and same never-throws contract as FileSystemBatchFileStore; provided through the existing BatchStoreProvider expect/actual trio so no new DI plumbing shape is invented. |
| HistoryRepository.get(batchId): SentBatchModel? | data | new | components.json#HistoryRepository | Assembles the EXISTING SentBatchModel from the @Relation children plus BatchFileReader bytes (frameBytes from framePath, hierarchyXml from hierarchyPath, cropBytes left empty because nothing consumes them on this path). Returns null when the row is absent or the files are unreadable — a stored path is documented as a hint, never a guarantee. |
| GetSentBatchUseCase (+Impl) | logic | new |  | Thin resultLauncher(errorMapper = Failure.Technical::Database) wrapper returning Result<SentBatchModel>; Failure.Logic.NotFound when the repository returns null. No branching logic of its own beyond that one null check. |
| UpdateBatchStatusUseCase (+Impl) | logic | new |  | Thin resultLauncher wrapper over HistoryRepository.updateStatus(batchId, status) — the hook F-8 built for F-9 and that has had zero callers until now. Writes only Verified and FollowUpSent. |
| HierarchySnapshotMapper.toModel(HierarchyDumpStdout(storedXml, "")) | data | reuse |  | Re-parses the stored hierarchy.xml into the BEFORE HierarchySnapshotModel with the exact semantics the live path uses. Called INSIDE data-repository-impl because presentation must never import data-device-api. Density comes back 0 — which is precisely why the AFTER-dump density decision exists. |
| core/verify/RegionPixels.kt | logic | new |  | `internal fun ImageBitmap.regionPixels(rect: DeviceRect, frameSize: IntSize): IntArray?` — clamps the annotation rect into the frame (a drag can overhang), allocates w*h and calls the commonMain readPixels(buffer, startX, startY, w, h, 0, w); null for a degenerate rect. The ONLY file in the feature allowed to mention ImageBitmap for diffing, ~10 lines, deliberately untested because Skiko is absent from desktopTest. Carries a KDoc saying exactly that. |
| core/verify/RegionDiff.kt | logic | new |  | `internal fun changedRatio(before: IntArray, after: IntArray): Float` — per-pixel Chebyshev distance in RGB, changed(p) = max(\|Δr\|,\|Δg\|,\|Δb\|) > CHANNEL_NOISE_FLOOR, ratio = changed/n, integer-only, one pass, alpha ignored, allocation-free. No Compose type anywhere in the file, so it runs in the Skiko-less desktopTest. The single most important testability decision in the feature. |
| core/verify/VerifyThresholds.kt | logic | new |  | CHANNEL_NOISE_FLOOR = 10 (of 255), REGION_CHANGE_RATIO = 0.02f, MIN_REGION_PIXELS = 64 — three internal consts in one file, each KDoc'd with its ceiling (antialias/subpixel/gamma wobble; a 2×30 caret in a 200×60 region is 0.5%; below 64 px the ratio is statistically meaningless) and the manual tuning method per NFR-17/MET-3. Never inlined at a call site. |
| core/verify/VerifyVerdict.kt | logic | new |  | `internal fun verdictOf(matched: Boolean, afterFinding: Finding?, changedRatio: Float, regionPx: Int): VerificationOutcome` — the AND-rule in exactly one expression and nowhere else: matched && afterFinding == null && (regionPx < MIN_REGION_PIXELS \|\| changedRatio > REGION_CHANGE_RATIO). Returns the already-shipped VerificationOutcome rather than inventing a parallel enum. KDoc records the accepted ceiling: a NEW unrelated finding in AFTER also reads Still off (it is a regression and deserves the flag). |
| core/verify/BeforeAfterFormat.kt | logic | new |  | The one formatter the repo lacks: 'top ' + deltaLabel(before) + ' → ' + deltaLabel(after) for a delta finding, and plainValueLabels joined by SEPARATOR for the touch-target shape ('height 24dp → 32dp · want 48dp'). Built entirely on F-5's displayDelta/deltaLabel/plainValueLabel/SEPARATOR — zero unit conversion is reimplemented. When neither side has a finding it falls back to a plainValueLabel size line (the design's 'placeholder fits · no clipping' is prototype copy with no computable source and is not faked). |
| MirrorStage.Verify + the MirrorStageContent branch + the stage-derivation condition | ui | reuse | components.json#mirror-stage-content | One enum case (MirrorContract.kt:90), one branch in MirrorStageContent's when (MirrorContent.kt:124, behind the existing error-card short-circuit), one condition in MirrorUiState.stage (MirrorContract.kt:331) ordered BEFORE the `frozen != null -> Frozen` case. No new Destination, no second MirrorHost call site. |
| VerifyUiState / VerifyRow / VerifyPhase / VerifyError | ui | new | components.json#mirror-stage-content | Value-equal reduced-state types in MirrorContract.kt. VerifyUiState(batchId, deviceLabel, sentAtMs, recapturedAtMs, phase, rows, error); VerifyRow(index, comment, beforeRect, afterRect, resultLine, outcome) — no bitmaps, no labels (MirrorContent resolves every string); VerifyPhase { Loading, Recapturing, Diffing, Ready }; VerifyError { NoLiveSession, NotOnDisk, SizeMismatch, HierarchyUnusable, Disconnected }. |
| VerifyFrames + MirrorViewModel.verifyFrames StateFlow | ui | new | components.json#mirror-host | `data class VerifyFrames(before: ImageBitmap, after: ImageBitmap)` on a plain MutableStateFlow<VerifyFrames?>, handed in as the 5th MirrorHost content-lambda argument. Honours Deviation 2 (pixels never enter reduced state) while delivering the two bitmaps the design needs. |
| MirrorIntent.SendFollowUp / MarkAllVerified / ExitVerify | ui | new |  | Three new intents beside the already-shipped VerifyBatch(batchId). ExitVerify cancels verifyJob, clears verifyFrames and routes to ResumeLive + a History tab selection. |
| MirrorViewModel.verifyBatch (replaces the log.i stub at MirrorViewModel.kt:388) | logic | reuse |  | The seven-step run on verifyJob: Job? (the dumpJob cancellation precedent), every decode/diff step inside withContext(Dispatchers.Default), device identity re-checked at every suspension point, VerifyPhase reduced as it advances. A second Verify cancels the first (flatMapLatest semantics by hand). |
| MirrorViewModel follow-up re-stage + pendingFollowUpBatchId | logic | reuse |  | Builds a FrozenFrame over the fresh AFTER HeldFrame with the still-off annotations renumbered via mapIndexed to StagedAnnotation(index = i + 1) (FR-47's only conversion point), sets the three current* flows, reduces state.copy(frozen = …), sets pendingFollowUpBatchId and calls resolveAndSend() UNCHANGED. The success path consumes the nullable field (null-checked, cleared in a finally) to write FollowUpSent on the ORIGINAL batch. |
| MirrorVerifyStage.kt | ui | new |  | The stateless stage composable: two aspect-correct MirrorFrameView columns inside 296×640 boxes (BEFORE rects neutral, AFTER rects coloured by verdict), the 300px Verification panel of VerificationRows with its 'n of N fixed' header, StatusLine for the four-step strip (the 'Agent fixed' step is a static countless label — no data source, nothing fabricated), footer AppButtons ('Send follow-up · N' disabled at N = 0, 'Mark all as verified'), the '← Back to live' Chip, and ErrorState for the five VerifyError cases. Light/dark @Preview pair. |
| VerificationRow / VerificationOutcome | ui | reuse | components.json#verification-row | Already shipped and a near-exact fit: GlyphBadge ✓/! + title + mono delta + the outcome word in Theme.color.sentFixed/warnStillOff. Its own @Preview uses the design's exact strings. F-9 only feeds it, resolving outcomeLabel from strings.xml at the call site per localisation.md, and reuses VerificationOutcome as the verdict type rather than defining a parallel enum. |
| StatusLine | ui | reuse | components.json#status-line | The 34dp multi-step dot+label line with a trailing slot — exactly the design's four-step strip (Sent → Agent fixed → Recaptured → Verified n/N). No new component. |
| MirrorFrameView + AnnotationOverlay + AnnotationMark + GlyphBadge | ui | reuse | components.json#mirror-frame-view | The two frame columns with per-annotation rects and numeric badges. The overlay takes Dp-space rects, so the stage does the px→dp conversion exactly as the existing frozen stage already does (FrameGeometry's render leg — the ONLY density division). |
| ErrorState | ui | reuse | components.json#error-state | Renders all five VerifyError cases as title + description without inventing a new error layout. |
| F-5 core/measure toolkit (snapTarget, projectMeasureNodes, measure, findingOf, deltaLabel, plainValueLabel, displayDelta, SEPARATOR) | logic | reuse |  | The entire hierarchy half. snapTarget is the matched-node function for BOTH frames; measure()/findingOf is the layout oracle; the format functions produce the design's strings verbatim. Internal and in-module — reuse in place, never copy out. |
| resolveAndSend / assembleEnvelope / CropEncoder / mintSessionId | logic | reuse |  | The entire follow-up send, reused with one status hook. Crops are taken from the current frozen held bitmap, so AC-42's 'from the AFTER frame' is satisfied with zero new code. |
| MirrorHistoryTab.HistoryEntryRow trailing slot | ui | reuse | components.json#MirrorHistoryTab | DELETE the `if (entry.status == BatchStatusModel.Fixed)` wrapper at lines 178-186 so AppButton(verifyLabel) renders unconditionally for every row (FR-37/AC-39), plus the three collateral comments. |

#### dataModel

| entity | fields |
| --- | --- |
| SentBatchModel (reused as the read model — no new type) | ["id: String", "sentAtMs: Long", "deviceLabel: String", "prompt: String", "frameBytes: ByteArray", "thumbPng: ByteArray?", "hierarchyXml: String", "annotations: List<SentAnnotationModel>"] |
| SentAnnotationModel (reused, device pixels) | ["left: Int", "top: Int", "right: Int", "bottom: Int", "comment: String", "cropBytes: ByteArray? (left empty on the read path — nothing consumes it)"] |
| VerifyUiState (new, reduced state, value-equal, no bitmaps) | ["batchId: String", "deviceLabel: String", "sentAtMs: Long", "recapturedAtMs: Long", "phase: VerifyPhase", "rows: List<VerifyRow>", "error: VerifyError?"] |
| VerifyRow (new, reduced state, no labels — MirrorContent resolves copy) | ["index: Int (0-based, history order)", "comment: String", "beforeRect: DeviceRect", "afterRect: DeviceRect", "resultLine: String", "outcome: VerificationOutcome"] |
| VerifyPhase (new enum) | ["Loading", "Recapturing", "Diffing", "Ready"] |
| VerifyError (new enum) | ["NoLiveSession", "NotOnDisk", "SizeMismatch", "HierarchyUnusable", "Disconnected"] |
| VerifyFrames (new, NOT reduced state — plain StateFlow only) | ["before: ImageBitmap", "after: ImageBitmap"] |
| history_entry / history_annotation (UNCHANGED — @Database stays version 1, no migration) | ["no new columns", "densityDpi deliberately not persisted; taken from the fresh AFTER dump for both sides"] |

#### interfaces

| name | contract |
| --- | --- |
| HistoryEntryDao.findById | @Transaction @Query("SELECT * FROM history_entry WHERE id = :id") suspend fun findById(id: String): HistoryEntryWithAnnotations? |
| HistoryDataSource.getBatch | suspend fun getBatch(id: String): HistoryEntryWithAnnotations? — bespoke String-keyed method; does NOT reuse ReadableDataSource<T>.getById(Int). |
| BatchFileReader | fun interface BatchFileReader { suspend fun read(path: String): ByteArray? } — never throws; null on missing/unreadable/permission-denied/truncated. |
| HistoryRepository.get | suspend fun get(batchId: String): SentBatchModel? — one-shot, so Result at the use-case boundary and a plain nullable here, matching observe()'s bare-Flow/degrade-to-empty precedent. |
| GetSentBatchUseCase | suspend operator fun invoke(batchId: String): Result<SentBatchModel> — Failure.Logic.NotFound when absent, Failure.Technical.Database otherwise. |
| UpdateBatchStatusUseCase | suspend operator fun invoke(batchId: String, status: BatchStatusModel): Result<Unit> |
| regionPixels | internal fun ImageBitmap.regionPixels(rect: DeviceRect, frameSize: IntSize): IntArray? — clamped; null for a degenerate (zero-area) rect. |
| changedRatio | internal fun changedRatio(before: IntArray, after: IntArray): Float — requires equal lengths (FR-46 guarantees it); 0f for empty input. |
| verdictOf | internal fun verdictOf(matched: Boolean, afterFinding: Finding?, changedRatio: Float, regionPx: Int): VerificationOutcome |
| beforeAfterLabel | internal fun beforeAfterLabel(before: MeasureReadout, after: MeasureReadout, dpi: Int): String |
| MirrorHost content lambda | content(state, frozenFrame ?: frame, frozenHierarchy, verifyFrames, ::handleIntent) — widens 4→5 args; exactly one call site, RectifyWindow.kt. |

#### stateManagement

Orbit MVI with mirror's two documented deviations. Semantics live in reduced MirrorUiState (stage, VerifyUiState with its phase/rows/error — all value-equal); pixels never do (VerifyFrames rides a plain MutableStateFlow<VerifyFrames?> beside frame/frozenFrame, exactly as Deviation 2 prescribes, because identity-compared types in reduced state can never deduplicate). handleIntent dispatches VerifyBatch/SendFollowUp/MarkAllVerified/ExitVerify to private handlers, each `= intent { reduce {}; … }`. The long-running work hangs off verifyJob: Job? (the dumpJob precedent) and is cancelled by ExitVerify, a second VerifyBatch, MarkAllVerified, or the existing flatMapLatest(sessionDeviceId) teardown on a device change. pendingFollowUpBatchId is a plain nullable field, null-checked and cleared in a finally, so a normal send can never write a status.

#### navigation

- No Destination, no navSavedStateConfiguration entry, no NavigationHost branch — presentation-feature-mirror/CLAUDE.md Deviation 1 explicitly exempts this surface from navigation.md's five-step checklist.
- Entry: History tab Verify button → MirrorIntent.VerifyBatch(batchId) → MirrorUiState.stage derives Verify (ordered before Frozen) → MirrorStageContent's when renders MirrorVerifyStage.
- Exit: the '← Back to live' chip → MirrorIntent.ExitVerify → cancel verifyJob, clear verifyFrames and the VerifyUiState, then ResumeLive + shellState.selectTab(History) (the LaunchedEffect precedent at RectifyWindow.kt:214).
- After a successful follow-up send, the existing resolveAndSend success path releases the frozen frame and resumes live as it already does — the user lands back on the live mirror with History selected.

#### affectedModules

- data-database-impl — EDIT src/commonMain/kotlin/data/database/impl/core/dao/HistoryEntryDao.kt (add @Transaction @Query findById(id: String): HistoryEntryWithAnnotations?, no LIMIT copy-paste from observeAll); EDIT src/commonMain/kotlin/data/database/impl/source/datasource/HistoryDataSource.kt + its Impl (add suspend fun getBatch(id: String)); ADD src/desktopTest/.../HistoryEntryDaoFindByIdTest.kt (present / absent / with-children). No entity change, no @Database version bump, no Migration. Test task: :data-database-impl:desktopTest (DAO tests are desktopTest-only by design — they need BundledSQLiteDriver + file IO).
- data-repository-impl — ADD src/commonMain/kotlin/data/repository/impl/source/store/BatchFileReader.kt (fun interface); ADD src/desktopMain/kotlin/data/repository/impl/core/store/FileSystemBatchFileReader.kt (java.io, Dispatchers.IO, never throws); EDIT src/commonMain/.../di/BatchStoreProvider.kt + src/desktopMain/.../di/BatchStoreProvider.desktop.kt + src/androidMain/.../BatchStoreProvider.android.kt + src/iosMain/.../BatchStoreProvider.ios.kt (null-returning stand-ins for android/ios); EDIT src/commonMain/.../di/DataRepositoryImplModule.kt (one binding appended inside the existing group); EDIT src/commonMain/kotlin/data/repository/impl/source/repository/HistoryRepositoryImpl.kt (add get(batchId) assembling SentBatchModel from the @Relation + reader bytes and re-parsing the stored XML through the existing HierarchySnapshotMapper); ADD src/commonTest/.../HistoryRepositoryGetTest.kt (fake data source + fake reader); ADD src/desktopTest/.../FileSystemBatchFileReaderTest.kt (round-trip with FileSystemBatchFileStore, missing file, unreadable path). Test tasks: :data-repository-impl:desktopTest.
- domain-repository-api — EDIT src/commonMain/kotlin/domain/repository/api/source/repository/HistoryRepository.kt (add `suspend fun get(batchId: String): SentBatchModel?`; update the updateStatus KDoc that currently says 'zero callers, the hook F-9 advances a batch with').
- domain-core — NO CHANGE. SentBatchModel/SentAnnotationModel/BatchStatusModel already carry everything; Verified and FollowUpSent already exist and are already persisted and rendered.
- domain-usecase-api — ADD src/commonMain/kotlin/domain/usecase/api/source/usecase/history/GetSentBatchUseCase.kt and UpdateBatchStatusUseCase.kt.
- domain-usecase-impl — ADD src/commonMain/kotlin/domain/usecase/impl/source/usecase/history/GetSentBatchUseCaseImpl.kt and UpdateBatchStatusUseCaseImpl.kt (both thin resultLauncher(errorMapper = Failure.Technical::Database) wrappers); EDIT src/commonMain/kotlin/domain/usecase/impl/di/DomainUseCaseImplModule.kt (two bindings APPENDED within the existing history layer group — koin-di.md: initKoin() load order is significant, never reorder). No per-use-case test by rote (testing.md) beyond one NotFound-mapping assertion on GetSentBatchUseCaseImpl, since it is the only one with a branch.
- presentation-feature-mirror (commonMain, core/) — ADD core/verify/RegionPixels.kt (the ~10-line ImageBitmap edge, untested on purpose, KDoc'd with the Skiko reason), core/verify/RegionDiff.kt, core/verify/VerifyThresholds.kt, core/verify/VerifyVerdict.kt, core/verify/BeforeAfterFormat.kt. File names partition their contents one concern per file (bugs.json#PP-TD-6 is the counter-example not to repeat). NO change to core/measure/* — it is reused in place.
- presentation-feature-mirror (commonMain, source/) — EDIT source/mirror/MirrorContract.kt (MirrorStage.Verify at ~:90; the Verify condition in the MirrorUiState.stage derivation at ~:331 ordered BEFORE Frozen; VerifyUiState/VerifyRow/VerifyPhase/VerifyError; SendFollowUp/MarkAllVerified/ExitVerify intents; DELETE the stale VerifyBatch KDoc at :490-500 that describes the gate); EDIT source/mirror/MirrorViewModel.kt (replace the log.i stub at :388 with the seven-step run; add verifyJob: Job?, verifyFrames: MutableStateFlow<VerifyFrames?>, pendingFollowUpBatchId: String?; two new constructor use-case params; the follow-up re-stage; the one hook in resolveAndSend's success path); EDIT source/mirror/MirrorHost.kt (pass verifyFrames as the 5th content-lambda argument); EDIT source/mirror/MirrorContent.kt (one new branch in MirrorStageContent's when at ~:124, behind the existing error-card short-circuit; the extra lambda parameter); ADD source/mirror/MirrorVerifyStage.kt (the stage composable + light/dark @Preview pair); EDIT source/mirror/MirrorHistoryTab.kt (DELETE the `if (entry.status == BatchStatusModel.Fixed)` gate at :178-186 so the AppButton renders unconditionally; fix the now-wrong preview KDoc at :240); EDIT di/PresentationFeatureMirrorModule.kt (two new use cases into the existing explicit `viewModel { MirrorViewModel(...) }` resolution).
- presentation-feature-mirror (commonTest) — ADD RegionDiffTest.kt, VerifyVerdictTest.kt, BeforeAfterFormatTest.kt, VerifyStageDerivationTest.kt, FollowUpRenumberTest.kt, MirrorViewModelVerifyTest.kt; EDIT MirrorFakes.kt (fake GetSentBatchUseCase / UpdateBatchStatusUseCase / BatchFileReader-backed batch; the existing Orbit.configureDefaults pinning and runCurrent() subscription trick are reused as-is). No test constructs an ImageBitmap. Backtick names contain no comma and no colon. Test task: :presentation-feature-mirror:desktopTest.
- presentation-core-localisation — EDIT src/commonMain/composeResources/values/strings.xml: ADD the mirror_verify_* keys (see i18n.strings); EDIT the stale comment at :182 that documents the Fixed-only gate. English only; no AAPT-style \' escapes (they leak into the UI — fixed once already in commit dae0893).
- presentation-core-ui — NO CHANGE. VerificationRow, StatusLine, MirrorFrameView, AnnotationOverlay, AnnotationMark, GlyphBadge, ErrorState, AppButton and Chip already cover every element of the design.
- presentation-core-styling — NO CHANGE. Theme.color.sentFixed / warnStillOff are identical in light and dark, so NFR-9 needs no new token.
- compose-application — EDIT src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt, exactly ~2 lines: the MirrorHost content lambda gains its 5th parameter (StateFlow<VerifyFrames?>) and forwards it into MirrorStageContent. Nothing testable goes here — this module has no test wiring.
- docs — EDIT presentation-feature-mirror/CLAUDE.md (record core/verify/ and the one-file ImageBitmap rule under Deviation 2's neighbourhood); EDIT .claude/rules/database.md only if a reviewer wants the 'density was never persisted, AFTER-dump fallback, column is the written-down upgrade path' note recorded there — otherwise it lives in VerifyThresholds/HistoryRepository KDoc.

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
| app-button | AppButton (Verify row action + both footer actions) | components.json#app-button |
| chip | Chip (the '← Back to live' chip) | components.json#chip |
| history-row | HistoryRow (unchanged; only its trailing slot's gate is deleted) | components.json#history-row |
| mirror-host | MirrorHost (content lambda widens 4→5 args; still exactly one call site) | components.json#mirror-host |
| mirror-stage-content | MirrorStageContent (one new when branch) | components.json#mirror-stage-content |
| MirrorHistoryTab | MirrorHistoryTab (gate deletion) | components.json#MirrorHistoryTab |
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
- MirrorViewModel.verifyBatch run (verifyJob) + the follow-up re-stage + pendingFollowUpBatchId
- MirrorVerifyStage.kt composable + the MirrorStageContent branch + light/dark @Preview pair
- mirror_verify_* localisation keys

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md (root) — module structure, convention plugins, toolchain | Confirms no new Gradle module is needed and which modules are KMP vs plain-JVM. |
| .claude/rules/package-layout.md | core/verify/ is internal-only; source/mirror/ is the public surface; explicitApi() demands explicit visibility and return types on every new declaration. |
| .claude/rules/testing.md | No comma or colon in backtick test names (compileTestKotlinIosArm64 fails); desktop is the task that runs; :compose-application has no test wiring; no Compose UI-test harness exists; :<module>:build also runs the iosSimulatorArm64 suite. |
| .claude/rules/domain-layer.md + .claude/rules/error-handling.md | One-shot ⇒ Result<T>, reactive ⇒ bare Flow<T>; every *UseCaseImpl body inside resultLauncher(errorMapper = Failure.Technical::Database), never raw runCatching; ShowError's messageId must be resolved through stringResource rather than extending the hardcoded-"Error" gap. |
| .claude/rules/mappers.md | The repository-side hierarchy re-parse and the entity→model assembly must never throw; degrade to null. |
| .claude/rules/koin-di.md | One module per Gradle module; initKoin() load order is significant — append the two new use cases and BatchFileReader within their existing layer groups, never reorder. |
| .claude/rules/orbit-mvi.md + presentation-feature-mirror/CLAUDE.md | Mirror's LOCAL precedent is binding here: Deviation 1 (no Destination, MirrorHost is the entry point) and Deviation 2 (pixels never enter reduced state) decide both the stage shape and the VerifyFrames StateFlow. |
| .claude/rules/design-tokens.md | No literal Color/dp/TextStyle at any call site; verdict colours are the existing sentFixed/warnStillOff tokens; atom→molecule→organism layering is untouched because no kit component is added. |
| .claude/rules/localisation.md | mirror_verify_* key convention, Res.string.* resolution at the call site (VerificationRow.outcomeLabel takes a raw String), English-only values/strings.xml. |
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
| .pipeline/sdd/docs/verify-and-rediff-loop/inputs/codebase-recon.md | Authoritative for existing symbols, paths, the four gaps and the binding rules that bite. Cited, not re-derived. |
| .pipeline/sdd/docs/verify-and-rediff-loop/inputs/design-verify-view.md | The pixel-level UI contract for MirrorVerifyStage (296×640 columns, 300px panel, the four-step strip, the footer actions). |
| presentation-feature-mirror/CLAUDE.md | Deviations 1 and 2 are binding; also documents the MirrorFakes Orbit-dispatcher pinning the new ViewModel tests need. |
| data-handoff-api/CLAUDE.md and mcp-application/CLAUDE.md | Context for the follow-up envelope's wire contract — read-only; F-9 changes neither. |

### errorStates

| state | behavior |
| --- | --- |
| loading | VerifyPhase.Loading (reading the batch off disk) then Recapturing (the ~2.7 s dumpHierarchy) then Diffing (the per-annotation loop). Each phase renders its own label in the Verification panel; the frames appear as soon as VerifyFrames is published, rows fill in as the loop advances. Never a bare indeterminate spinner with no text. |
| empty | A batch with zero annotations shows an empty Verification panel, 'Send follow-up · 0' hard-disabled, and 'Mark all as verified' as the only sensible action. |
| error | VerifyError.NoLiveSession — ErrorState naming the device and telling the user to start mirroring first, then retry (AS-1 as amended; this must read as a precondition, not a failure). |
| error | VerifyError.NotOnDisk — the row's framePath/hierarchyPath is null or the file is gone/unreadable (a stored path is a hint, never a guarantee). The Verify button is still rendered per AC-39; the honest failure happens here, not as a second status-shaped gate. |
| error | VerifyError.SizeMismatch — the live frame's IntSize differs from the decoded frame.png's (resolution or orientation changed). Checked ONCE, before any per-annotation work (FR-46/AC-40). |
| error | VerifyError.HierarchyUnusable — the AFTER dump failed, or a non-empty snapshot projects to ZERO candidates (the documented Pixel 8 984×2132-vs-1080×2400 display-override case). Abort before any verdict rather than emitting N confident wrong 'Still off' results. Distinct from an empty-to-begin-with snapshot, which degrades per-annotation to NO_MATCH per AC-41. |
| error | VerifyError.Disconnected — sessionDeviceId changed or went null at any suspension point. No status write, the original batch status untouched, and the state is retryable once the device is back (NFR-16/AC-44). |
| error | A follow-up send that fails (handoff.publish failure) leaves the ORIGINAL batch status untouched — FollowUpSent is written only after resolveAndSend reaches its success point — and surfaces through F-7's existing SendPhase.Failed path with no new error surface. |

### observability

#### logs

- MirrorViewModel log.i on verify start: batchId, annotation count, live frame size (replaces the existing stub's single log.i line).
- log.i per terminal outcome: Ready with n/N fixed, or the VerifyError case name — one line per run, not per annotation.
- log.w when projectMeasureNodes returns empty for a non-empty snapshot, including both frame sizes — this is the display-override landmine and it must be diagnosable from a log alone.
- log.i on each status write (batchId, status) so the two previously-unexercised BatchStatusModel values are traceable.
- No per-pixel or per-annotation logging — N region diffs would flood the log for no benefit.

#### metrics

- Wall-clock of the dumpHierarchy step and of the whole diff loop, logged once per run, so the ~2.7 s KDoc budget can be re-checked against real hardware without a profiler.
- changedRatio per annotation is held in memory for the run (not persisted, not rendered — the design shows measurement deltas, never pixel percentages) and is the number a manual MET-3 spot-check reads out of the debugger.

#### analyticsEvents


### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | The verdict is never colour-only: VerificationRow pairs the accent colour with a ✓ / ! GlyphBadge AND the literal word 'Fixed' / 'Still off'. The AFTER frame's verdict-coloured rects keep their numeric badges, which tie each rect back to a labelled row. |
| A11Y-2 | Both verdict tokens (Theme.color.sentFixed, warnStillOff) are identical in light and dark and already shipped with contrast reviewed — no new colour token, so NFR-9 parity holds by construction. |
| A11Y-3 | 'Send follow-up · N' keeps F-7's ⌘↩ hint and is reachable by keyboard; it is hard-disabled (not merely dimmed) at N = 0 so the keyboard path cannot fire a no-op send. |
| A11Y-4 | Every error state renders a title AND a description through ErrorState — no icon-only or colour-only failure. NoLiveSession in particular names the required action ('start mirroring this device'). |
| A11Y-5 | The in-progress phase is announced in text ('Recapturing…', 'Diffing…'), not as a bare spinner, so the ~2.7 s wait is legible rather than ambiguous. |
| A11Y-6 | The '← Back to live' chip is a real focusable control with a text label, not a bare glyph; it is the single documented exit from the stage. |
| A11Y-7 | Result lines use Theme.typography.monoData at the design's size via tokens only — no hardcoded sub-10px text is introduced. |

### performanceBudget

| metric | target |
| --- | --- |
| DumpHierarchyUseCase (the AFTER capture) | ~2.7 s measured on a Pixel 8 — budgeted, not optimised. It must be the only adb round trip in the run (the AFTER frame costs one atomic currentHeld.value read) and it must be visibly staged as VerifyPhase.Recapturing. |
| measure() per annotation per side | ~100 ms for a 300-node dump (F-5's documented KDoc budget), i.e. ~2N×100 ms for the loop; projectMeasureNodes runs ONCE per side, not per annotation. |
| Region diff per annotation | O(w·h) single pass, integer-only, two IntArray allocations (one per side) and no per-pixel allocation. A 200×60 region is 12k pixels — immeasurable next to the dump. |
| Phase visibility | The first phase change reduces within one frame of the Verify click; no step may leave the window without a text label for more than ~100 ms. |
| Thread discipline | Every decode/project/measure/diff step inside withContext(Dispatchers.Default); nothing on the intent context; the UI thread only ever receives reduced value types and two already-decoded bitmaps. |
| Reduced-state churn | Rows are reduced in one batch at the end of the loop (or at most once per annotation), never per pixel; VerifyFrames never enters reduced state at all. |
| Memory | At most two full-res bitmaps plus two per-region IntArrays alive at once; the stored frameBytes are released after decode and crop files are never read. |

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
- mirror_verify_retry — 'Retry'
- mirror_verify_empty — 'This batch has no annotations'

#### notes

English-only; presentation-core-localisation/src/commonMain/composeResources/values/strings.xml is the single file. Keys follow <feature>_<element>. Every string is resolved via Res.string.* at the call site — the contract types (VerifyRow, VerifyError) carry no labels, matching MirrorContent's existing stateless-copy rule. No AAPT-style \' apostrophe escapes (they leaked into the UI once already; see commit dae0893) — avoid apostrophes in new copy or write them bare. The stale comment at strings.xml:182 describing the Fixed-only Verify gate is deleted with the gate. Result strings themselves (dp deltas) are produced by F-5's formatters and are intentionally not localised — they are measurement data, same as live measure-mode.

### security

#### dataHandling

- BatchFileReader reads absolute paths that came out of this app's own Room database and were written by FileSystemBatchFileStore under ~/.rectify/history/<sanitised batchId>/. It is not a general file-read API: it is used only for the two stored paths of one batch, and it never throws or surfaces path contents on failure.
- Screenshots, hierarchy XML and comments never leave the machine. The follow-up envelope goes through F-7's existing local file-slot handoff; F-9 adds no network call and no new egress point.
- Decoding stored bytes is wrapped and degrades to null (VerifyError.NotOnDisk) rather than crashing on a corrupt or truncated PNG — null-on-failure is universal in this repo.
- No new persisted data: no new column, no new file on disk, no new preference. The only writes are two BatchStatusModel values through the existing updateStatus.

#### permissions

- None new. The feature requires an already-authorised adb connection and an active mirror session, both established before Verify is reachable.

#### privacy

- Frame pixels live in memory for the duration of the run and in the already-existing on-disk history directory. F-9 neither prunes nor extends that directory — the documented ponytail ceiling (nothing prunes ~/.rectify/history) is inherited from F-8 and explicitly not addressed here.

### rollout

#### featureFlags


#### migrations

- None. @Database stays at version 1 and no Migration is written — this is a deliberate, recorded decision (densityDpi comes from the fresh AFTER dump, because a migration could never retroactively supply density for rows already on disk). Adding the column later remains the written-down upgrade path and would then need a real version bump plus a hand-written Migration per database.md.

#### backwardCompatibility

Fully backward compatible with every batch F-8 already persisted: the read path uses only columns that already exist, and a row whose framePath/hierarchyPath is null (or whose files were deleted) surfaces VerifyError.NotOnDisk instead of failing. Existing statuses render unchanged — the status chip stays informational and no longer gates anything. F-7's normal send path is byte-for-byte unchanged apart from one null-checked hook, and a plain send still writes no status.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json#PP-TD-8 | Kotlin/Native release linker OOMs (Java heap space) under ~4 parallel linkReleaseFramework tasks against a 4 GiB daemon heap. Pre-existing, affects every cross-target gate in this repo. | The mandatory ./gradlew :presentation-feature-mirror:build cross-target gate failing with Java heap space rather than a real compile error. | Run the gate with --max-workers=1, exactly as F-7 did. Do not 'fix' the build by weakening the gate. |
| bugs.json#PP-TD-4 | Tautological test (assertEquals(1, PAYLOAD_FORMAT_VERSION)) that would pass under any edit — the test was the defect. | The same failure mode in VerifyThresholdsTest: asserting CHANNEL_NOISE_FLOOR == 10 proves nothing and would pass under any retuning. | Do NOT write a test over the constants' values. Test the BEHAVIOUR they gate: changedRatio over hand-built IntArrays that straddle the floor, and verdictOf over the eight conjunct combinations. Thresholds are tunable by design (NFR-17), so pinning their literals would actively obstruct tuning. |
| bugs.json#PP-TD-5 | A test whose name and comment claimed a structural property the body never asserted (the body asserted Empty → empty twice). | VerifyVerdictTest or VerifyStageDerivationTest claiming 'aborts before any verdict' or 'unmatched node is Still off' while actually asserting something weaker. | Each test name states exactly one claim and the body asserts that claim; the HierarchyUnusable test asserts the rows list is EMPTY (no verdict was produced), not merely that an error is set. |
| bugs.json#PP-TD-6 | core/payload file names do not partition their contents — one label function in PayloadLabels.kt while two live in PayloadEstimate.kt, and PayloadGate.kt accumulated three unrelated predicates. | The new core/verify/ package drifting the same way — e.g. the verdict rule leaking into RegionDiff.kt, or thresholds inlined at call sites. | Five files, one concern each, fixed up front: RegionPixels (the ImageBitmap edge), RegionDiff (the metric), VerifyThresholds (the constants), VerifyVerdict (the AND-rule), BeforeAfterFormat (the formatter). The AND-rule exists in exactly one expression in exactly one file. |
| bugs.json#PP-TD-7 | ROOT CAUSE: promptOverride carries no record of the payload it was authored against, so nothing can distinguish a stale override from a deliberate one. | The follow-up re-stage inheriting a stale promptOverride from the original (pre-verify) frozen frame and silently sending the agent the wrong prompt. | The follow-up builds a FRESH FrozenFrame over the AFTER HeldFrame with promptOverride explicitly null (comments are re-sent verbatim — the closed decision), so no override can leak across the verify boundary. One test asserts the re-staged frame's promptOverride is null. |
| bugs.json#PP-TD-2 | ClearStaged kept promptOverride, resurfacing a prompt for deleted annotations (fixed in F-7, same defect class as F-1). | The same lifecycle hole in the new ExitVerify handler — leaving VerifyUiState, verifyFrames or pendingFollowUpBatchId set after leaving the stage, so a later unrelated send writes FollowUpSent on an old batch. | ExitVerify cancels verifyJob and clears all three (state, frames, pending id); pendingFollowUpBatchId is additionally cleared in a finally on the send path. One test asserts a plain (non-follow-up) send writes no status at all. |
| bugs.json#PP-TD-1 | A crop cell fitted to a constant 84dp but drawn in weight(1f), clipping the outline at narrow panel widths — arithmetic done against an assumed size rather than a measured one. | The same class of bug in the verify stage: the design fixes the frames at 296×640, but a real device frame has its own aspect ratio, and the panel is resizable. | Render each frame aspect-correct WITHIN a 296×640 box (letterbox, never stretch — stretching a frame in a pixel-verification tool misrepresents the thing being verified), and derive the annotation-rect overlay from the SAME measured frame box via FrameGeometry's render leg, never from the nominal constant. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 1 | Data read path: HistoryEntryDao.findById (@Transaction @Query by String id, no LIMIT copy) + HistoryDataSource.getBatch + desktopTest for present / absent / with-children | [] | ["AC-23", "AC-45"] | S |
| 2 | BatchFileReader fun interface + FileSystemBatchFileReader (java.io, Dispatchers.IO, never throws) + null-returning android/ios stand-ins through BatchStoreProvider + Koin binding appended; desktopTest round-trip against FileSystemBatchFileStore | [] | ["AC-45"] | S |
| 3 | HistoryRepository.get(batchId): assemble the existing SentBatchModel from the @Relation plus reader bytes and re-parse the stored XML via HierarchySnapshotMapper.toModel(HierarchyDumpStdout(xml, "")); commonTest with a fake data source and a fake reader (present, missing row, null framePath, unreadable file) | [1, 2] | ["AC-23", "AC-45"] | M |
| 4 | GetSentBatchUseCase + UpdateBatchStatusUseCase (api + impl + Koin appended within the existing history layer group); one test for the NotFound mapping only — no per-use-case test by rote | [3] | ["AC-23", "AC-43"] | S |
| 5 | core/verify pure core: RegionDiff.kt + VerifyThresholds.kt + VerifyVerdict.kt + BeforeAfterFormat.kt, all commonTest-covered over IntArrays and primitives, no Compose type and no bitmap anywhere. This is the step that proves the no-seam decision and the step AC-24 lives or dies on | [] | ["AC-24", "AC-41"] | M |
| 6 | core/verify/RegionPixels.kt — the clamp + readPixels edge, ~10 lines, KDoc'd as the one ImageBitmap-touching file and deliberately untested (Skiko is absent from desktopTest) | [5] | ["AC-23", "AC-45"] | S |
| 7 | MirrorContract.kt: MirrorStage.Verify, the stage-derivation condition ordered BEFORE Frozen, VerifyUiState/VerifyRow/VerifyPhase/VerifyError, SendFollowUp/MarkAllVerified/ExitVerify intents, and deletion of the stale VerifyBatch gate KDoc at :490-500; stage-derivation test | [5] | ["AC-23", "AC-40", "AC-44"] | S |
| 8 | MirrorViewModel.verifyBatch: replace the log.i stub at :388 with the seven-step run on verifyJob (held-frame snapshot → batch → size check → AFTER dump → project both sides → per-annotation measure+diff+verdict → terminal status write), every pixel step under withContext(Dispatchers.Default), device identity re-checked at each suspension point; ViewModel tests with fakes (MirrorFakes already pins Orbit's event-loop dispatcher) | [4, 6, 7] | ["AC-23", "AC-24", "AC-40", "AC-41", "AC-43", "AC-44"] | L |
| 9 | verifyFrames StateFlow + the 5th MirrorHost content-lambda argument + the one ~2-line RectifyWindow.kt call-site edit (nothing testable goes into :compose-application) | [8] | ["AC-23"] | S |
| 10 | MirrorVerifyStage.kt + the MirrorStageContent branch + the mirror_verify_* strings + a light/dark @Preview pair: two aspect-correct frame columns in 296×640 boxes (AFTER rects verdict-coloured), the 300px Verification panel of VerificationRows, StatusLine's four-step strip, footer actions, the Back-to-live chip, ErrorState for the five error cases | [9] | ["AC-23", "AC-24", "AC-40", "AC-42", "AC-43"] | L |
| 11 | Delete the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:178-186 and the three collateral comments (MirrorHistoryTab.kt:240 preview KDoc, strings.xml:182, MirrorContract.kt:490-500) — the button renders for every row and fails with NotOnDisk when framePath is null, rather than gaining a new gate | [7] | ["AC-39"] | S |
| 12 | Follow-up path: build a fresh FrozenFrame over the AFTER HeldFrame with the still-off annotations renumbered via mapIndexed to index = i + 1 and promptOverride explicitly null, set the three current* flows, reduce, set pendingFollowUpBatchId, call resolveAndSend() UNCHANGED; consume the pending id in its success path to write FollowUpSent on the ORIGINAL batch, cleared in a finally | [8, 10] | ["AC-42"] | M |
| 13 | Mark-all-as-verified (cancel verifyJob first, force every row to Fixed, write Verified) and the automatic all-Fixed terminal write; tests that a mixed run with no user action writes NOTHING | [8, 10] | ["AC-43"] | S |
| 14 | Disconnect/cancellation hardening: re-check sessionDeviceId at every suspension point, verify the existing flatMapLatest teardown cancels verifyJob, assert no status write on abort and that Verify is retryable after reconnect | [8] | ["AC-44"] | S |
| 15 | Docs + hygiene: record core/verify/ and the one-file ImageBitmap rule in presentation-feature-mirror/CLAUDE.md; confirm @Database is still version 1 and no Migration exists; grep that no new Color/dp literal and no new catalog entry crept in | [10, 11, 12, 13] | ["AC-45"] | S |
| 16 | Gates: ./gradlew :presentation-feature-mirror:desktopTest :data-repository-impl:desktopTest :data-database-impl:desktopTest :domain-usecase-impl:desktopTest, then the cross-target build (./gradlew :presentation-feature-mirror:build :data-repository-impl:build --max-workers=1 — bugs.json#PP-TD-8), then the manual end-to-end demo against a real device (send → change → Verify → follow-up → both status writes), waived explicitly if no device/GUI is available | [14, 15] | ["AC-23", "AC-24", "AC-39", "AC-42", "AC-43"] | M |

### testStrategy

#### unit

- :presentation-feature-mirror:desktopTest — RegionDiffTest: two identical IntArrays give 0f; a single channel differing by exactly CHANNEL_NOISE_FLOOR is NOT counted while floor+1 is (the boundary, asserted from both sides); a chroma-only change at equal luma IS counted (this is why Chebyshev over RGB rather than luma); alpha differences alone are ignored; an all-different array gives 1f; empty input gives 0f. Pure IntArrays — no bitmap is ever constructed. This is the Skiko-free core the whole no-seam decision rests on.
- :presentation-feature-mirror:desktopTest — VerifyVerdictTest: the AND-rule over its combinations — matched + no afterFinding + ratio above REGION_CHANGE_RATIO is Fixed; matched + no afterFinding + ratio below is StillOff (the agent-did-nothing case, AC-24's headline); matched + afterFinding present is StillOff regardless of ratio; unmatched is StillOff regardless of everything (AC-41); regionPx below MIN_REGION_PIXELS lets the hierarchy half decide alone (a 6×6 icon with no afterFinding and a zero ratio is Fixed). Assertions are behavioural, never over the constants' literals (bugs.json#PP-TD-4).
- :presentation-feature-mirror:desktopTest — BeforeAfterFormatTest: a top-misalignment finding that went to zero renders exactly 'top Δ6dp → 0dp' (U+0394); a touch-target finding that moved but still fails renders 'height 24dp → 32dp · want 48dp' with the U+00B7 SEPARATOR; dpi <= 0 degrades to raw px labels via the existing PixelUnits behaviour; no-finding-on-either-side falls back to a plain size line and never fabricates the design's prototype 'placeholder fits' copy.
- :presentation-feature-mirror:desktopTest — VerifyStageDerivationTest: MirrorUiState.stage yields Verify when VerifyUiState is present, and Verify takes precedence over Frozen and Streaming; clearing it returns the previous stage.
- :presentation-feature-mirror:desktopTest — FollowUpRenumberTest: a 2-of-3 still-off selection produces StagedAnnotation indices 1 and 2 (not 0 and 2, not 1 and 3) with the original comments and the AFTER rects — the single FR-47 conversion point.
- :data-database-impl:desktopTest — HistoryEntryDaoFindByIdTest: an inserted batch comes back with its annotation children in index order; an absent id returns null; a batch with zero annotations returns a row with an empty child list. (DAO tests are desktopTest-only by design — they need BundledSQLiteDriver and file IO.)
- :data-repository-impl:desktopTest — HistoryRepositoryGetTest (commonTest source set, runs on desktop): a present row with readable files yields a fully populated SentBatchModel whose hierarchyXml round-trips through HierarchySnapshotMapper; a missing row, a null framePath and a null-returning reader each yield null rather than throwing; the BEFORE snapshot comes back with densityDpi 0 (the documented gap the AFTER-dump fallback exists for).
- :data-repository-impl:desktopTest — FileSystemBatchFileReaderTest: bytes written by FileSystemBatchFileStore read back identical; a missing path, a directory path and an unreadable path each return null without throwing.
- :domain-usecase-impl:desktopTest — one assertion that GetSentBatchUseCaseImpl maps a null repository result to Failure.Logic.NotFound and a thrown exception to Failure.Technical.Database. UpdateBatchStatusUseCaseImpl gets no test of its own — it is a bare resultLauncher wrapper and testing.md explicitly says not to add one per use case by rote.

#### integration

- :presentation-feature-mirror:desktopTest — MirrorViewModelVerifyTest against fakes: a verify with no held frame reduces VerifyError.NoLiveSession and never calls the use case; a null batch reduces NotOnDisk; a live frame whose size differs from the stored frame reduces SizeMismatch AND the rows list stays EMPTY (AC-40 — no per-annotation work ran); a non-empty hierarchy that projects to zero candidates reduces HierarchyUnusable with an EMPTY rows list, while an empty-to-begin-with snapshot produces N rows all StillOff (AC-41, the two cases kept distinct); a run where every row is Fixed calls UpdateBatchStatusUseCase exactly once with Verified; a mixed run calls it zero times.
- :presentation-feature-mirror:desktopTest — phase and cancellation: the phases advance Loading → Recapturing → Diffing → Ready in order; a second VerifyBatch cancels the first so only one terminal reduction lands; MarkAllVerified during a run cancels verifyJob first, forces every row Fixed and writes Verified exactly once, and a late completion cannot overwrite it (AC-43/SC-20).
- :presentation-feature-mirror:desktopTest — disconnect: a device change mid-run reduces VerifyError.Disconnected, calls UpdateBatchStatusUseCase zero times, and leaves the state retryable (AC-44/SC-21/NFR-16).
- :presentation-feature-mirror:desktopTest — follow-up: SendFollowUp with 2 of 3 still off re-stages exactly those two over the AFTER held frame with promptOverride null, re-enters resolveAndSend, and on success writes FollowUpSent exactly once on the ORIGINAL batch id; a failing publish writes no status at all; and a plain ResolveAndSend with no pending follow-up writes no status (the PP-TD-7/PP-TD-2 regression guard).
- Cross-target compile+run gate: ./gradlew :presentation-feature-mirror:build :data-repository-impl:build --max-workers=1 — commonTest compiles for iosArm64 and the iosSimulatorArm64 suite actually runs (Orbit's dispatcher defaults are process-wide and have hidden a real failure in this repo before).

#### ui

- No automated UI coverage exists or is added — there is no Compose UI-test harness in this repo and introducing one is a real infrastructure decision, not a casual addition (testing.md).
- MirrorVerifyStage ships a light/dark @Preview pair per compose-preview-convention.md, feeding a default-constructed VerifyUiState with one Fixed and one Still-off row. VerificationRow's own existing @Preview already renders the design's exact strings.
- AC-39 is verified by inspection (the gate is deleted; the button renders unconditionally) plus the manual demo, not by a UI test — the same ceiling F-8's AC-36 hit.
- The manual end-to-end demo is the only evidence for the rendered stage, the 296×640 letterboxing, the verdict-coloured rects and the four-step strip. If no device or GUI run is available, record an explicit waiver rather than a silent pass (F-8 left nine such steps unexecuted — do not repeat that silently).

#### linkedScenarios

- SC-8
- SC-9
- SC-19
- SC-20
- SC-21
- SC-22

#### coverageGoal

Every pure function in core/verify/ except RegionPixels.kt has at least one behavioural (never tautological) commonTest assertion, and every VerifyError case has one ViewModel-level assertion. RegionPixels.kt is deliberately uncovered and kept to ~10 lines precisely because Skiko's absence makes it untestable in this module. No test constructs an ImageBitmap anywhere. MET-3's <5% false-positive rate is explicitly NOT a coverage target — it is unverifiable in v1 (NFR-17) and is recorded as such rather than asserted.

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-23 | ["FR-21", "FR-38", "FR-39", "FR-40", "FR-42"] | ["MirrorViewModel.verifyBatch", "GetSentBatchUseCase", "HistoryRepository.get", "RegionPixels", "RegionDiff", "F-5 measure/snapTarget/projectMeasureNodes", "MirrorVerifyStage", "MirrorStage.Verify"] | ["SC-8", "SC-19", "MirrorViewModelVerifyTest", "RegionDiffTest", "HistoryRepositoryGetTest"] |
| AC-24 | ["FR-41", "FR-43"] | ["VerifyVerdict.verdictOf", "VerifyThresholds", "RegionDiff.changedRatio", "Findings.findingOf (reuse)", "VerificationOutcome (reuse)", "MirrorViewModel follow-up re-stage"] | ["SC-8", "SC-9", "VerifyVerdictTest", "RegionDiffTest", "FollowUpRenumberTest"] |
| AC-39 | ["FR-21", "FR-37"] | ["MirrorHistoryTab.HistoryEntryRow trailing slot (gate deleted)", "MirrorContract VerifyBatch KDoc", "strings.xml:182 comment"] | ["SC-19", "inspection of the deleted gate", "manual demo"] |
| AC-40 | ["FR-46"] | ["MirrorViewModel.verifyBatch step 3 (single IntSize check)", "VerifyError.SizeMismatch", "ErrorState (reuse)"] | ["SC-22", "MirrorViewModelVerifyTest — size mismatch leaves rows empty"] |
| AC-41 | ["FR-40", "FR-41"] | ["NodeSnap.snapTarget (reuse, NO_MATCH_NODE_ID)", "MeasureProjection.projectMeasureNodes (reuse)", "VerifyVerdict.verdictOf first conjunct", "VerifyError.HierarchyUnusable (the distinct abort case)"] | ["VerifyVerdictTest — unmatched is StillOff", "MirrorViewModelVerifyTest — empty snapshot gives N StillOff rows while non-empty-but-rejected aborts with zero rows"] |
| AC-42 | ["FR-43", "FR-47", "FR-45"] | ["MirrorViewModel follow-up re-stage + pendingFollowUpBatchId", "resolveAndSend / assembleEnvelope / CropEncoder (reuse, unchanged)", "UpdateBatchStatusUseCase", "MirrorVerifyStage footer action"] | ["SC-9", "FollowUpRenumberTest", "MirrorViewModelVerifyTest — follow-up writes FollowUpSent once on the original batch; a plain send writes nothing"] |
| AC-43 | ["FR-44", "FR-45"] | ["UpdateBatchStatusUseCase", "HistoryRepository.updateStatus (reuse, first caller)", "MirrorViewModel markAllVerified (cancels verifyJob first)", "MirrorVerifyStage footer action"] | ["SC-20", "MirrorViewModelVerifyTest — all-Fixed writes Verified once; mixed writes nothing; Mark-all during a run cancels then writes once"] |
| AC-44 | ["FR-38", "FR-45"] | ["verifyJob + the existing flatMapLatest(sessionDeviceId) teardown", "VerifyError.Disconnected", "VerifyPhase"] | ["SC-21", "MirrorViewModelVerifyTest — disconnect aborts with zero status writes and a retryable state"] |
| AC-45 | ["FR-39", "FR-47"] | ["BatchFileReader", "HistoryRepository.get (frameBytes from framePath only)", "RegionPixels (reads the full-res frame with stored rects)"] | ["inspection — no crop-N.png or thumbPng read exists on the diff path", "HistoryRepositoryGetTest", "FileSystemBatchFileReaderTest"] |
| NFR-7 | ["FR-45"] | ["HistoryRepository.updateStatus (reuse)", "history_entry (unchanged schema, version 1)"] | ["HistoryEntryDaoFindByIdTest", "manual app-restart check (inherited F-8 ceiling)"] |
| NFR-9 | ["FR-42"] | ["VerificationRow (reuse)", "Theme.color.sentFixed / warnStillOff (reuse, identical light/dark)"] | ["light/dark @Preview pair — inspection; no new colour token is added so parity holds by construction"] |
| NFR-15 | ["FR-38", "FR-42"] | ["VerifyPhase in reduced state", "withContext(Dispatchers.Default) around every decode/diff step", "MirrorVerifyStage phase labels"] | ["MirrorViewModelVerifyTest — phase ordering Loading → Recapturing → Diffing → Ready", "performanceBudget inspection"] |
| NFR-16 | ["FR-45"] | ["verifyJob", "flatMapLatest(sessionDeviceId) teardown (reuse)", "terminal-only status writes"] | ["SC-21", "MirrorViewModelVerifyTest — no partial status write"] |
| NFR-17 | ["FR-41"] | ["VerifyThresholds (three KDoc'd tunable consts)"] | ["inspection — MET-3 recorded as unverified-in-v1; behavioural threshold tests only, never literal assertions; golden-corpus path written down, not built"] |
| AS-1 (amended) | ["FR-38"] | ["MirrorViewModel currentHeld.value atomic read", "VerifyError.NoLiveSession"] | ["MirrorViewModelVerifyTest — no held frame reduces NoLiveSession and never calls the use case"] |
| AS-2 | ["FR-38"] | ["MirrorVerifyStage header showing the stored deviceLabel against the live device"] | ["inspection — v1 does not match serials; recorded as a known gap"] |
| AS-3 | ["FR-40"] | ["Findings.findingOf (reuse as the oracle)", "Measure.measure (reuse, headless)"] | ["BeforeAfterFormatTest", "VerifyVerdictTest", "the existing F-5 measure suite (unchanged)"] |
| AS-4 | ["FR-39", "FR-47"] | ["HistoryRepository.get", "BatchFileReader", "the AFTER-dump density fallback"] | ["HistoryRepositoryGetTest — densityDpi comes back 0 on the stored side"] |
| AS-5 | ["FR-21", "FR-37"] | ["MirrorHistoryTab gate deletion"] | ["SC-19", "inspection"] |

### openQuestions

- Should a later feature give the coding agent an MCP-side signal to write BatchStatusModel.InProgress, or does that value stay permanently unused?
- Should Verify ever support re-targeting a reconnected-but-different device than the one that sent the original batch (would need a persisted device serial, not just deviceLabel)?
- Should the 'AGENT CHANGES' file-diff section come back later via a git-diff-capable MCP tool, or is it permanently out of scope?
- Should a densityDpi column with a real Room migration be added later so historical batches carry their own density, rather than relying on the AFTER-dump fallback indefinitely?
- Should the follow-up envelope reframe the comment for the agent ('still off after your fix: …') via promptOverride, or continue re-sending verbatim (v1 answer: verbatim)?
- Should temporal stabilisation (sampling two AFTER frames ~250 ms apart) be added if manual spot-checks show animation-driven flakiness in the pixel conjunct?
- Should MET-3 tuning move from manual spot-check to a hand-labelled before/after corpus with one parameterised test once such a corpus exists?
- Should the Verify button be DISABLED (not hidden) when entry.framePath == null? v1 always renders it and fails with NotOnDisk, deliberately avoiding a second gate in the same commit that deletes the first — but disabling is a defensible second choice if review prefers it.

### readyForBacklog

true
