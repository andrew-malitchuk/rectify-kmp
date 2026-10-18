# history-batch-tracking — candidates/candidate-1

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | candidates/candidate-1 |
| status | done |
| producedBy | sdd-build-plan-draft |
| schemaVersion | 1 |
| createdAt | 2026-10-18T18:57:20Z |
| updatedAt | 2026-10-18T18:57:20Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-18T18:57:20Z

## Provenance

### selfHash

0ea97ba637754b9fd6d283893977726e1782989a350329c25772208a4e31d9c2

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 42b2b0eb4629896ec4eaf148e33daf36b0aa73a81ba9b786b488f1886624e6ca |

## Data

### meta

#### title

History & Batch Tracking (F-8) — implementation plan

#### version

1

#### status

draft

### goal

Make every batch F-7 publishes durable (Room row + per-batch files under ~/.rectify) and surface it as the third, desktop-only segment of RectifyWindow's inspector tab strip: a LazyColumn of Today/Yesterday/Older-grouped rows with a union-bbox thumbnail, title, meta line and status chip, plus an In-progress badge and a 'claude · session <id>' footer — without a new Gradle module, a new design-system component, or any change to F-7's merged wire contract.

### problemStatement

PER-2 (QA tester) has no durable record of batches already sent to the coding agent. FileSlotAnnotationHandoff.publish prunes every handoff-slot directory except the newest and data-handoff-api states verbatim 'No history, no ring buffer, no expiry', so the previous send's images vanish on the next send and nothing at all survives an app restart. There is therefore no way to see what has already been reported and in what state without re-testing, and F-9's Verify loop has no durable original frame to diff against.

### highLevelDescription

One persistence vertical hung off the existing mirror surface. Write path: MirrorViewModel.resolveAndSend() already holds `frozen`, `held.bitmap`, `held.bytes` and, after assembly, `(envelope, images)` as in-memory locals; inside the SAME withContext(Dispatchers.Default) assembly block it additionally encodes one small thumbnail PNG from the union bounding box of the batch's annotation rects. On `published.onSuccess`, BEFORE postSideEffect(SendComplete), the VM calls `recordSentBatch(...): Result<Unit>` and consumes it with `.onFailure { log.w }` and nothing else — the persist lives in its own failure domain and can never touch SendPhase. Down the stack RecordSentBatchUseCaseImpl → HistoryRepositoryImpl writes bulk bytes through a `fun interface BatchFileStore` (desktop java.io, null-returning stand-in elsewhere, never throws) to ~/.rectify/history/<sanitised batchId>/{frame.png, crop-N.png, hierarchy.xml}, then inserts the parent row plus normalised history_annotation children in one @Transaction with whatever paths the store returned (or null). Read path: HistoryEntryDao exposes Flow<List<HistoryEntryEntity>> ordered sentAtMs DESC → HistoryEntryMapper.toModel → ObserveHistoryUseCase (bare Flow) → collected once in MirrorViewModel's container init into MirrorUiState.history, which feeds both the list and the badge. Render path: MirrorHistoryTab = Column { LazyColumn(weight(1f)) { group headers + HistoryRow items }; footer }, grouping computed at render time by pure groupHistory(entries, nowMs, zone); the thumbnail is a few-KB BLOB decoded with the already-used ByteArray.decodeToImageBitmap(), so the list performs zero filesystem reads. Two edits in RectifyWindow.kt close the loop.

### background

F-7 (resolve-and-send-mcp) is merged and ships AnnotationHandoff/AnnotationEnvelope, mintSessionId, EnvelopeAssembly and CropEncoder. ShellTab.History, the Cmd+3 shortcut, SidebarTabs' per-tab count, HistoryRow, StatusPill/StatusPillTone and AppButtonSize.ListAction are all already built and registered — only the panel body (`else -> Unit` in RectifyWindow.kt) and all persistence are missing. data-database-impl is a live but never-instantiated Room skeleton: AppDatabase is a lazy Koin single nothing injects, and neither ~/rectify.db nor ~/.rectify/ exists on disk, which is what makes relocating the DB and staying at @Database(version = 1) safe and makes Step 0 a blocking spike.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | preparation/preparation.md (signed off, readyForPlan true — FR/NFR/AC/SC ids frozen) | .pipeline/sdd/docs/history-batch-tracking/preparation/preparation.md |
| code | preparation/brainstorm.md — 5 closed architecture decisions, 12 implementation steps, edge cases | .pipeline/sdd/docs/history-batch-tracking/preparation/brainstorm.md |
| code | preparation/clarify.md — 13 closed requirement decisions | .pipeline/sdd/docs/history-batch-tracking/preparation/clarify.md |
| code | inputs/codebase-recon.md — verified ground truth on files, symbols, Room skeleton state, design tokens | .pipeline/sdd/docs/history-batch-tracking/inputs/codebase-recon.md |
| code | inputs/design-history-tab.md — pixel-level UI contract extracted from the HTML prototype | .pipeline/sdd/docs/history-batch-tracking/inputs/design-history-tab.md |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt#resolveAndSend (L801-877) — the single integration point |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/CropWindow.kt — verified: designPad=14f, boxW=84f, boxH=72f, returns a render window, NOT a PNG; not reusable for the thumbnail |  |
| code | data-database-impl/src/desktopMain/kotlin/data/database/impl/core/DatabaseBuilder.desktop.kt — verified: File(user.home, "rectify.db"), BundledSQLiteDriver, no setQueryCoroutineContext |  |
| code | .pipeline/sdd/docs/registry/{components,features,styles,bugs}.json |  |

### scope

#### in

- Persist every successful F-7 AnnotationHandoff.publish as a batch record: metadata + per-annotation child rows in Room, frame.png / crop-N.png / hierarchy.xml as files under ~/.rectify/history/<batchId>/, and a small derived thumbnail PNG as a BLOB column.
- Relocate the desktop Room database to ~/.rectify/rectify.db, add .setQueryCoroutineContext(Dispatchers.IO), keep @Database(version = 1) with no Migration, delete ExampleEntity/ExampleDao — shipped as one atomic change.
- Desktop-only History tab as the third RectifyWindow inspector segment, rendered with Compose LazyColumn, grouped Today / Yesterday / Older by local calendar day, newest-first inside each group.
- Row anatomy on the existing HistoryRow: union-bbox thumbnail, single-line ellipsised title (first annotation's comment), meta line '<n> notes · HH:mm · #<shortId>', StatusPill with tone dot.
- BatchStatusModel with 5 values (Sent, InProgress, Fixed, Verified, FollowUpSent); F-8 writes only the initial Sent and exposes HistoryRepository.updateStatus for F-9.
- Verify button on Fixed rows only, routed through MirrorIntent.VerifyBatch to a one-line logging stub F-9 fills.
- Tab-strip badge = count of In-progress batches, the map key omitted entirely at zero.
- Footer line 'claude · session <sessionId>' from the most recently persisted batch; project path omitted.
- Resilience: failed file write still persists the row (SC-18); DB read failure degrades the tab to empty; a hand-deleted batch directory cannot break a row.
- Pure formatting/geometry functions unit-tested in presentation-feature-mirror commonTest; repository/mapper branches unit-tested in data-repository-impl commonTest; a Room insert+observe round-trip test in data-database-impl desktopTest.

#### out

- Verify/re-diff execution logic (F-9) — F-8 ships only the button affordance and its visibility rule.
- Any automatic status transition beyond the initial Sent write; In progress / Fixed / Verified / Follow-up sent are set by future consumers through HistoryRepository.updateStatus.
- An UpdateBatchStatusUseCase — FR-33 asks for a repository API and F-8 has no caller; a use case with zero callers is scaffolding F-9 adds in one file.
- Search, filter, pagination, load-more, retention policy, batch deletion and any storage cap.
- Row-click detail view or re-opening the original annotation UI from a History row (row click is a no-op in v1).
- The live agent-activity 'sub' line under the status chip — no data source exists in the F-7 handoff contract.
- Android/iOS support — History is desktop-only; Android/iOS get null-returning stand-ins only so the modules compile.
- Any change to AnnotationEnvelope / AnnotationHandoff / mintSessionId (F-7's merged wire contract).
- A new Gradle module, a second koinViewModel() call site, a new design-system atom/molecule, a Room Migration, and a Compose UI-test harness (runComposeUiTest) — F-8 is not the spec that introduces one.
- Any network egress or telemetry (NFR-4).

### assumptions

- AS-1: a single integration point (the resolveAndSend call site) is sufficient to trigger the persist; F-8 never polls or reads back the handoff slot.
- AS-2: AC-22's 'title/summary' is the batch's first annotation's comment text — no separate generated summary field.
- AS-3: the static 'claude' agent label is acceptable; mcp-application ships exactly one tool and one consumer.
- AS-4: Sent shares Verified's neutral (StatusPillTone.Neutral / ink3) tone until product says otherwise.
- AS-29: BatchFileStore never throws and returns null on any failure; Android/iOS bind a null-returning stand-in, unreachable in practice because UnsupportedAnnotationHandoff.publish throws first.
- AS-30: assembly, crop encoding and the thumbnail encode all run inside the existing withContext(Dispatchers.Default) boundaries already documented in resolveAndSend's KDoc.
- PLAN-AS-1 (resolved plan-stage question): core/payload/CropWindow.kt#cropWindow uses designPad=14f, boxW=84f, boxH=72f and returns a render window (srcOffset/srcSize/dstSize/scale), not PNG bytes. It is NOT reused for the thumbnail. FR-29's pad=18 / bw=bh=40 is the prototype's crop() formula; the plan reimplements it as a pure thumbnailRect(union, frameSize, designPad = 18f, designWidth = 296f) in core/history/, keeping cropWindow's design-space pad-scaling convention (pad = designPad * frameWidth / designWidth).
- PLAN-AS-2 (resolved plan-stage question): the repo contains zero pluralStringResource usages and zero <plurals> resources. The note count ships as two plain keys (mirror_history_meta_note_one / mirror_history_meta_note_other) selected by a pure function; no 's' is hand-concatenated and no plural API is introduced.
- PLAN-AS-3 (resolved plan-stage question): SidebarTabs' rendering of a zero count is not relied on at all — tabCounts is built with buildMap and the ShellTab.History key is omitted when the In-progress count is 0, which makes AC-34 true regardless of SidebarTabs' behaviour.
- PLAN-AS-4: HistoryRow ships a 56dp thumbnail plate (registry: components.json#history-row) while the design quotes 44×44. The shipped component is reused as-is; the encoded thumbnail is capped at 128px on its longest edge so it stays crisp inside the 56dp plate. Changing the plate size is a design-system change no AC requires.
- PLAN-AS-5: Room's builder() does not touch the filesystem — the database file is opened on first query — so a bad path or unwritable home surfaces as a query exception inside the guarded Flow or inside resultLauncher, not as a crash while Koin constructs the graph.
- PLAN-AS-6: the batch directory name is batchId.replace(Regex("[^A-Za-z0-9._-]"), "_") because mintSessionId embeds a device id that is not guaranteed filesystem-safe.
- PLAN-AS-7: the row's id is the envelope's sessionId; a same-millisecond collision on the same device+frame degrades to an overwrite via @Insert(onConflict = REPLACE) rather than a crash.

### constraints

| type | text |
| --- | --- |
| tech | Desktop-only (jvm target) — matches F-7's desktop-only AnnotationHandoff and RectifyWindow, the only host for the tab strip. |
| tech | Must use the existing Room KMP scaffold (data-database-api/data-database-impl); F-8 is its first real consumer. KSP is already configured for kspAndroid/kspIosArm64/kspIosSimulatorArm64/kspDesktop — do not re-wire it. |
| tech | Must ingest exclusively via the existing AnnotationHandoff/AnnotationEnvelope contract; AnnotationEnvelope's field order is wire-breaking and is not touched. |
| tech | DB relocates to ~/.rectify/rectify.db AND stays at @Database(version = 1) with no Migration — the two decisions ship together or not at all. Destructive migration is disabled repo-wide. |
| tech | No new Gradle module: MirrorHost's KDoc states there is exactly one call site, and a :presentation-feature-history module would force a second koinViewModel(). |
| tech | No new runtime dependency: no okio, no kotlinx-io, no image library. kotlinx-datetime is already in the version catalog (one build-file line, not a new dependency). |
| tech | :compose-application has NO test wiring — nothing testable may land there. Pure logic lives in presentation-feature-mirror/core/history/ (commonTest), data-repository-impl (commonTest) or data-database-impl (desktopTest). |
| tech | commonTest compiles for iOS: backtick test names containing a comma or a colon fail compileTestKotlinIosArm64. Plain camelCase names only (apostrophes are verified fine). |
| business | NFR-4: no network egress — all batch storage is local filesystem plus the local database; no analytics, no telemetry, no crash-report payloads carrying annotation text. |
| tech | Step 0 is a blocking spike. Room has never been instantiated in this repo; no entity/DAO/repository work starts until an insert+observe round-trip is green on desktop. |

### dependencies

| on | note |
| --- | --- |
| F-7 resolve-and-send-mcp (merged) | F-7 resolve-and-send-mcp (merged) — source of AnnotationHandoff/AnnotationEnvelope, mintSessionId, EnvelopeAssembly and the frame/crop bytes. F-8 makes zero changes to it. |
| data-database-api / data-database-impl Room KMP scaffold | data-database-api / data-database-impl Room KMP scaffold — registry-tagged 'scaffold only' (features.json#database); must be re-tagged once F-8 lands. |
| presentation-core-ui | presentation-core-ui — HistoryRow, StatusPill, AppButton, SidePanelFrame, SidebarTabs, ShellTab, RectifyWindowShell: all shipped, all reused unchanged. |
| kotlinx-datetime 0.8.0 (already in gradle/libs.versions.toml, already used by data-repository-impl) for local calendar-day bucketing. | kotlinx-datetime 0.8.0 (already in gradle/libs.versions.toml, already used by data-repository-impl) for local calendar-day bucketing. |
| F-9 verify-and-rediff-loop (future consumer) | F-9 verify-and-rediff-loop (future consumer) — will call HistoryRepository.updateStatus and fill MirrorIntent.VerifyBatch's handler body. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Room's KMP toolchain (KSP output, generated AppDatabaseConstructor actuals, BundledSQLiteDriver linkage and packaging, schemas/ export) has literally never been exercised in this repo; the first real table may surface build or packaging problems unrelated to F-8's logic. | high | high | Step 0 blocking spike, timeboxed to half a day, landed as data-database-impl/src/desktopTest/.../HistoryEntryDaoTest.kt so the proof is committed rather than thrown away. If the bundled driver fights desktop packaging, that is a scope conversation, not an in-branch debug session. |
| Staying at schema version 1 while changing the entity set would crash any machine that somehow already holds a v1 rectify.db. | low | high | Relocating the file to ~/.rectify/rectify.db in the same change makes a pre-existing file impossible by construction. The two decisions ship together; if the relocation is dropped, the version bump plus a Migration becomes mandatory. |
| A full-resolution union-bbox crop would be hundreds of KB per row, contradicting the 'few KB BLOB' basis of the thumbnail decision and ballooning MirrorUiState at scale. | med | med | Downscale before encoding: crop+scale the union window to <=128px on its longest edge into a scratch ImageBitmap (commonMain Canvas(ImageBitmap) + drawImageRect) and hand THAT to the existing CropEncoder. Keeps one encoder seam. Fallback if the common Canvas path misbehaves off the main thread: do the scale inside a desktop AwtCropEncoder sibling (getSubimage + getScaledInstance) bound by qualifier. |
| Adding two non-defaulted MirrorViewModel constructor params breaks every existing VM test until MirrorFakes is updated. | high | low | Update MirrorFakes.kt in the same commit: RecordSentBatchUseCase { Result.success(Unit) } and ObserveHistoryUseCase { emptyFlow() }. Defaults are deliberately refused — a default turns a missing Koin binding into silently losing every batch. |
| Disk + SQLite work inside resolveAndSend lengthens the send's perceived latency. | low | low | A few hundred KB on Dispatchers.IO inside an already multi-second operation. Documented single-line escape hatch (detached viewModelScope.launch) if ever measured as felt; not taken now because it makes SC-16 racy against the toast. |
| Growing MirrorUiState with a list that changes on every DB emission adds reducer passes to a pixel-sensitive state machine. | low | low | Emissions happen once per send, not once per frame, and the BLOBs are a few KB decoded lazily by the row. This respects deviation 2 in presentation-feature-mirror/CLAUDE.md (pixels stay out of reduced state) because the thumbnail is bytes, not a decoded frame. |
| Storing thumbnail bytes in the DB could be read as contradicting the closed 'paths, not BLOBs' decision. | med | low | Stated explicitly in this plan and in the entity's KDoc: frame.png and crop-N.png remain files on disk with paths in the DB exactly as decided; the BLOB is a separate derived few-KB thumbnail that exists only so a commonMain composable needs no file-reading seam. |
| AC-34's badge can never be demonstrated end-to-end in F-8 because nothing ever writes In progress. | high | low | Unit-test the count-to-badge mapping (including the omit-at-zero branch) and seed one row manually in the DB during the demo. Never ship a fake transition to make a screenshot look right. |
| Unbounded disk growth from retained frame/crop/hierarchy copies. | med | low | Closed, accepted ceiling (clarify decision). Marked with a `ponytail:` comment in BatchFileStore naming the upgrade path (age or count cap) rather than re-litigated. |
| A failed DB insert is invisible to the user — one batch quietly missing from History. | low | med | Accepted: F-8 has no error surface and the alternative (failing the send) is explicitly ruled out. log.w carries the batch id. Cheap upgrade if it proves unacceptable: reuse the existing SendComplete snackbar path with a different summary. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-20 | Provide a History tab listing every batch ever sent, grouped by time period, each row showing a thumbnail crop, title/summary, timestamp, and a status chip (e.g. Fixed, In progress). |
| FR-26 | On every successful AnnotationHandoff.publish (F-7), persist a new Batch record (frozen frame copy, per-annotation crop copies, hierarchy dump, prompt, sessionId, sentAtMs, device info) to the local Room database with initial status Sent. |
| FR-27 | Copy the frozen frame PNG and each per-annotation crop PNG from the handoff slot into app-owned storage at persist time, because F-7 prunes every handoff-slot image directory except the latest after each publish and a bare path reference would go stale. |
| FR-28 | Render History rows grouped under Today / Yesterday / Older headers, based on the batch's local calendar day relative to now, newest batch first within each group. |
| FR-29 | Compute each row's 44x44 thumbnail by running the existing crop() algorithm (pad=18, bw=bh=40) against the union bounding box of all of the batch's annotation rects. |
| FR-30 | Render each row's title as the batch's first annotation's comment text (ellipsised, single line) and its meta line as '<n> notes . <HH:mm sent time> . #<last 4 hex chars of batch id>'. CORRECTION OF RECORD: implemented as (id.hashCode() and 0xFFFF).toString(16).padStart(4, '0') — mintSessionId returns '<deviceId>-f<frameId>-<sentAtMs>' in decimal, so the literal wording is not satisfiable. |
| FR-31 | Render a status chip with a tone dot per BatchStatus value (Sent, In progress, Fixed, Verified, Follow-up sent), matching the design's tone mapping; Sent shares Verified's neutral tone since the prototype defines no distinct tone for it. |
| FR-32 | Show a Verify button only on rows with status Fixed; clicking it invokes a caller-supplied callback (no-op by default). The diff/verify logic itself is out of scope (F-9). |
| FR-33 | Expose a BatchRepository.updateStatus(batchId, BatchStatus) API for a future consumer (F-9) to drive In progress / Fixed / Verified / Follow-up sent transitions; F-8 itself never calls it beyond the initial Sent write. |
| FR-34 | Show History as the third segment of RectifyWindow's existing inspector tab strip, with a badge equal to the count of batches currently in In progress status (hidden when zero). |
| FR-35 | Show a footer line 'claude . session <sessionId>' sourced from the most recently persisted batch's sessionId; project path is omitted because AnnotationEnvelope carries no such field. |
| FR-36 | Keep all persisted batches visible indefinitely; no delete or retention UI ships in this spec. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication. |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-13 | performance | The History list renders via a lazily-virtualized list (Compose LazyColumn) so UI render cost stays independent of total persisted batch count. |
| NFR-14 | performance | Copying frame and crop PNGs to durable per-batch storage at publish time runs off the UI thread, consistent with AnnotationHandoff's existing suspend contract. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-22 | Given batches have been sent previously, when the History tab is opened, then batches are grouped by time period, each showing thumbnail, summary, timestamp, and status chip. | must | test |
| AC-33 | Given History contains at least one batch, when the app is quit and relaunched, then all batches, their status chips, stored frozen frames, and per-annotation records are still present and openable. | must | test |
| AC-34 | Given the History tab badge, when zero batches have status In progress, then no badge is shown on the tab; when N batches are In progress, the badge shows N. | must | test |
| AC-35 | Given a batch has more than one annotation, when its History row thumbnail is generated, then the crop is computed from the union bounding box of all of the batch's annotation rects, not just the first. | must | test |
| AC-36 | Given a batch's status, when its History row renders, then the Verify button is shown only for status Fixed and is absent for every other status. | must | demo |
| AC-37 | Given at least one batch has been persisted, when the History list footer renders, then it shows 'claude . session <id>' sourced from the most recently persisted batch's sessionId. | should | inspection |
| AC-38 | Given F-7 prunes the handoff slot's image files after each publish, when a batch is persisted, then its frame and crop images have already been copied into app-owned storage so they remain available after subsequent sends and app restarts. | must | test |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Open History tab", "Locate a Fixed batch", "Click Verify", "Diff runs per annotation against the frozen frame", "Annotation marked resolved"] |
| SC-15 | History survives an app restart | edge | ["Send two batches", "Quit Rectify", "Relaunch Rectify", "Open History tab", "Both batches, statuses and stored frames are intact"] |
| SC-16 | New batch appears while History is open | happy | ["Open History tab", "Resolve & Send a new batch", "A new row appears at the top of the Today group with status Sent", "The tab badge count stays unchanged because Sent is not In progress"] |
| SC-17 | Multi-annotation batch thumbnail | edge | ["Resolve & Send a batch with 3 annotations scattered across the frame", "Open History tab", "The row's thumbnail shows a crop centered on the union bounding box of all 3 annotation rects"] |
| SC-18 | Image copy fails during persist | error | ["Resolve & Send succeeds and publishes to the handoff slot", "The per-batch frame/crop file copy fails (e.g. disk full)", "The batch record is still persisted with its metadata and status chip", "The row shows a placeholder thumbnail instead of blocking the History entry"] |

### definitionOfReady

- Preparation signed off with readyForPlan true and all FR/NFR/AC/SC ids frozen (done).
- Branch sdd/history-batch-tracking cut from development with F-7 merged (done).
- Step 0 spike green: a Room insert + Flow observe round-trip runs on desktop, KSP output and the generated AppDatabaseConstructor actual compile, schemas/1.json is exported, and the bundled SQLite driver links in the desktop run configuration.
- The three plan-stage verifications are resolved and recorded as PLAN-AS-1/2/3 (cropWindow constants, pluralStringResource absence, zero-count badge) — done in this plan.
- No open contradiction between the chosen union-rect/thumbnail math and FR-29's prototype wording (recorded as PLAN-AS-1).

### definitionOfDone

- Every file in the change list added/edited/deleted, with ExampleEntity.kt and ExampleDao.kt removed and schemas/<AppDatabase>/1.json committed.
- ./gradlew :presentation-feature-mirror:desktopTest :data-repository-impl:desktopTest :data-database-impl:desktopTest is green.
- Cross-target build green: ./gradlew :presentation-feature-mirror:build :data-repository-impl:build :data-database-impl:build (commonTest compiles for iOS; use --max-workers=1 per bugs.json#PP-TD-8 if the Kotlin/Native linker OOMs).
- ./gradlew :desktop-application:run opens, Cmd+3 reaches History, and the manual demo passes: send two batches, quit, relaunch, both rows render with thumbnails, titles, meta lines and Sent chips (AC-33/SC-15).
- Manual AC-34 demo with one row hand-seeded to In progress: badge shows 1; delete the seed, badge disappears.
- Manual AC-36 demo with one row hand-seeded to Fixed: Verify button present there and absent on every other row; clicking it logs through MirrorIntent.VerifyBatch.
- ~/.rectify/history/<batchId>/ contains frame.png, crop-N.png and hierarchy.xml after a send; deleting that directory by hand leaves the row rendering unchanged.
- No literal Color/dp/TextStyle at any new call site; every new string resolved through Res.string.*; previews are private light+dark pairs wrapped in AppTheme.
- No new Gradle module, no new design-system component, no new runtime dependency, no Migration added.
- Docs updated: .claude/rules/database.md gains the source/entity/ placement line, data-database-impl/CLAUDE.md loses the 'placeholder' framing, and registry deltas are reported to the orchestrator.

### successMetrics

- PER-2 can determine a previously sent batch's status from History alone, with zero re-testing.
- Zero history data loss across restart (AC-33) across a week of normal usage.
- Persist adds no measurable failure to the send path: zero occurrences of SendPhase.Failed attributable to a history write (structurally impossible by design, confirmed by code review and the VM test).

### architecture

#### approach

One persistence vertical (entity + child entity + DAO + datasource + repository + mapper + two use cases) hung off the existing mirror surface, with one new side effect inside MirrorViewModel.resolveAndSend() and one new field on MirrorUiState. Write-after-success in its own failure domain; read through a bare Flow collected in the container init; render with LazyColumn over shipped design-system components; two edits in RectifyWindow.kt.

#### rationale

Persist-after-success was chosen over persist-before-publish (would show a lying 'Sent' row for a batch the agent never received, contradicting FR-26), over sharing publish's runCatching (a full disk could reduce SendPhase.Failed for a send that actually succeeded — the one outcome explicitly ruled out), and over a detached fire-and-forget launch (makes SC-16's row racy against the toast and escapes the test harness's advanceUntilIdle discipline) — for one extra .onFailure line. The brainstorm's key realisation holds: assembleEnvelope already returns (envelope, images) as in-memory values and held.bytes/held.bitmap are locals, so F-8 never reads anything back from ~/.rectify/handoff and FR-27/AC-38 are satisfied by construction, not by racing the pruner. A normalised child table beats a JSON blob because the blob would embed AnnotationEnvelope (documented as wire-breaking) in durable storage, store knowingly dead cropPaths, and drag kotlinx-serialization into presentation-feature-mirror; the child table costs ~30 lines and makes F-9's per-annotation 'resolved' flag a column add. A thumbnail BLOB beats thumb.png + path because a commonMain composable cannot read a file, so a path would require a whole new expect/actual image-loading seam for a single plate — and the BLOB makes 'the row survives manual file deletion' structural rather than defensive. BatchFileStore as a fun-interface Koin seam matches two existing precedents (CropEncoder, HandoffSourcesProvider), avoids adding okio/kotlinx-io for three file writes, and is the only option that lets SC-18 be unit-tested without a real disk.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Persist before publish | Shows status Sent for a batch the agent never received; contradicts FR-26/AC-38 and needs compensating deletion on publish failure — more code to be less correct. |
| Persist inside publish's runCatching | A Room/IO failure would reduce SendPhase.Failed and tell the user a successful send failed. One saved line for an expensive class of false-negative bug. |
| Detached viewModelScope.launch after publish | Removes latency nobody has measured while making SC-16's row arrival racy against the toast and escaping advanceUntilIdle. Kept as a documented one-line escape hatch. |
| Persist in the data layer by observing handoff.latest() | latest() reads the slot that is being pruned — exactly the staleness F-8 exists to avoid — and needs polling plus duplicate detection. AS-1 says one call-site hook suffices. |
| Single annotationsJson TEXT column | Embeds the wire-breaking AnnotationEnvelope in storage, persists dead handoff cropPaths, and adds a serialization dependency edge to presentation-feature-mirror. |
| thumb.png on disk + path column | Requires a new expect/actual image-file-reading seam whose only consumer is one 56dp plate, and reintroduces the missing-file failure mode the BLOB deletes. |
| Store the full frame and crop at render time (as the HTML prototype does) | Loads hundreds of KB per visible row, fighting NFR-13 for no benefit. |
| New :presentation-feature-history Gradle module | MirrorHost's KDoc states there is exactly one call site; a second module would force a second koinViewModel() and a second ViewModel for a side-panel slot. |
| Reuse core/payload/CropWindow.kt#cropWindow for the thumbnail | Verified on disk: it uses designPad=14f into an 84×72 cell and returns a render window, not PNG bytes. FR-29 wants pad=18 into a 40×40 box and the result must be encoded once at persist time. |
| Add an UpdateBatchStatusUseCase in F-8 | FR-33 specifies a repository API; a use case with zero callers is scaffolding F-9 can add in one file when it has one. |
| Add okio or kotlinx-io for the file writes | A new filesystem dependency for three file writes fails the reuse ladder; java.io in a desktopMain source set is the shipped precedent. |
| Bump to @Database(version = 2) with MIGRATION_1_2 | Writes migration code no database can ever execute — nothing has ever injected AppDatabase and the relocation makes a pre-existing file impossible by construction. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| HistoryRow | ui | reuse | components.json#history-row | THE History row, unchanged: 56dp thumbnail slot, title (Theme.typography.body), meta (monoMeta + ink3), StatusPill, trailing RowScope slot for Verify. presentation-core-ui/.../kit/molecule/row/HistoryRow.kt. |
| StatusPill / StatusPillTone | ui | reuse | components.json#status-pill | Status chip with tone dot. Done=Fixed, Busy=In progress, Alert=Follow-up sent, Neutral=Verified and Sent (AS-4). No new tone token. |
| AppButton (AppButtonSize.ListAction) | ui | reuse | components.json#app-button | The in-row Verify action on Fixed rows only. |
| SidePanelFrame / SidebarTabs / ShellTab | ui | reuse | components.json#side-panel-frame | The tab strip, its per-tab count badge and the History segment with its Cmd+3 shortcut — all already shipped; F-8 only fills the content slot and the tabCounts map. |
| RectifyWindowShell | ui | reuse | components.json#rectify-window-shell | Hands the tab body Modifier.weight(1f) and the tabCounts map — the two contracts MirrorHistoryTab must honour. |
| CropEncoder / AwtCropEncoder | logic | reuse | presentation-feature-mirror/src/commonMain/.../source/mirror/CropEncoder.kt | Encodes the (pre-scaled) thumbnail PNG; never throws, returns null on Android/iOS and on any failure — which IS SC-18's placeholder path. |
| assembleEnvelope / HandoffImage | logic | reuse | presentation-feature-mirror/.../core/send/EnvelopeAssembly.kt | Already returns frame and crop bytes in memory before publish — the reason FR-27/AC-38 need no file copy out of the handoff slot. |
| HandoffSourcesProvider | logic | reuse | data-handoff-api/.../di/HandoffSourcesProvider.{kt,desktop,android,ios}.kt | The per-platform Koin binding shape BatchStoreProvider copies verbatim. |
| ModelResourceMapper | logic | reuse | data-repository-impl/core/mapper/base/ModelResourceMapper.kt | Base for HistoryEntryMapper (toModel/toResource, never throws). |
| resultLauncher | logic | reuse | domain-usecase-impl/core/ResultLauncher.kt | Wraps RecordSentBatchUseCaseImpl with errorMapper = Failure.Technical::Database. |
| data-database-api base datasources | data | reuse | data-database-api/.../source/datasource/base/ | ObservableDataSource / AppendableDataSource / UpsertableDataSource mixed into HistoryDataSource where it is consumed, per database.md. |
| HistoryEntryEntity | data | new |  | data-database-impl/src/commonMain/.../source/entity/HistoryEntryEntity.kt — @Entity(tableName = "history_entry"), public (documented deviation: it is the public datasource's wire type). Columns id (PK, = envelope.sessionId), sentAtMs, status, title, annotationCount, deviceLabel, prompt, dirPath?, framePath?, hierarchyPath?, thumbPng BLOB?. |
| HistoryAnnotationEntity | data | new |  | Same folder — @Entity(tableName = "history_annotation"), composite PK (batchId, index), FK to history_entry onDelete CASCADE, @Index on batchId. Columns left/top/right/bottom, comment, cropPath?. |
| HistoryEntryDao | data | new |  | data-database-impl/.../core/dao/HistoryEntryDao.kt (internal) — @Transaction suspend insert(entry, annotations) with @Insert(onConflict = REPLACE); @Query observeAll(): Flow<List<HistoryEntryWithAnnotations>> ORDER BY sentAtMs DESC; @Query UPDATE history_entry SET status = :status WHERE id = :id. |
| AppDatabase (edit) | data | reuse | data-database-impl/.../core/database/AppDatabase.kt | entities = [HistoryEntryEntity::class, HistoryAnnotationEntity::class], version stays 1, exportSchema stays true, add historyEntryDao(); keep AppDatabaseConstructor's explicit body-less override fun initialize() or the build fails. Delete ExampleEntity/ExampleDao. |
| DatabaseBuilder.desktop (edit) | data | reuse | data-database-impl/src/desktopMain/.../core/DatabaseBuilder.desktop.kt | File(System.getProperty("user.home"), ".rectify").apply { mkdirs() } then rectify.db inside it; add .setQueryCoroutineContext(Dispatchers.IO). Same root as the handoff slot; the new path is why version 1 can stand. |
| HistoryDataSource / HistoryDataSourceImpl | data | new |  | Public interface in data-database-impl/.../source/datasource/ mixing ObservableDataSource/AppendableDataSource/UpsertableDataSource; internal DAO-backed impl in core/datasource/. Registered in dataDatabaseImplModule. |
| BatchFileStore | logic | new |  | data-repository-impl/.../source/store/BatchFileStore.kt — public fun interface { suspend fun write(batchId: String, files: List<Pair<String, ByteArray>>): String? }. Never throws; null on any failure. Carries the `ponytail:` comment naming the unbounded-growth ceiling and its upgrade path. |
| FileSystemBatchFileStore | logic | new |  | data-repository-impl/src/desktopMain/.../core/store/FileSystemBatchFileStore.kt — java.io writes to ~/.rectify/history/<sanitised batchId>/ on Dispatchers.IO inside runCatching, returns the directory path or null. |
| BatchStoreProvider (expect/actual) | logic | new |  | data-repository-impl/.../di/BatchStoreProvider.{kt,desktop.kt,android.kt,ios.kt} — desktop binds FileSystemBatchFileStore; android/ios bind BatchFileStore { _, _ -> null } (AS-29). Shape copied from HandoffSourcesProvider. |
| HistoryEntryModel / BatchStatusModel / SentBatchModel / SentAnnotationModel | data | new |  | domain-core/.../source/model/history/ — read model (id, sentAtMs, status, title, annotationCount, thumbPng: ByteArray?, framePath: String?, sessionId) and write model carrying frameBytes, crops, thumbPng, hierarchyXml, prompt, deviceLabel and annotations. Zero dependencies; ByteArray is stdlib. |
| HistoryRepository | logic | new |  | domain-repository-api/.../source/repository/HistoryRepository.kt — observe(): Flow<List<HistoryEntryModel>>, suspend record(batch: SentBatchModel), suspend updateStatus(batchId: String, status: BatchStatusModel) (FR-33, exposed and never called by F-8). |
| HistoryRepositoryImpl | logic | new |  | data-repository-impl/.../source/repository/HistoryRepositoryImpl.kt (internal) — orchestrates BatchFileStore then HistoryDataSource; observe() maps entities through HistoryEntryMapper and is guarded with .catch { log.w(it); emit(emptyList()) }. |
| HistoryEntryMapper | logic | new |  | data-repository-impl/.../core/mapper/HistoryEntryMapper.kt — one internal object implementing ModelResourceMapper; never throws; an unrecognised stored status string falls back to BatchStatusModel.Sent. |
| RecordSentBatchUseCase / ObserveHistoryUseCase | logic | new |  | domain-usecase-api/.../source/usecase/history/ + impls in domain-usecase-impl. One-shot ⇒ Result<Unit> inside resultLauncher(errorMapper = Failure.Technical::Database); reactive ⇒ bare Flow<List<HistoryEntryModel>>, never Flow<Result<T>>. |
| HistoryFormatting.kt (pure) | logic | new |  | presentation-feature-mirror/.../core/history/HistoryFormatting.kt — groupHistory(entries, nowMs, zone): List<HistoryGroup>, shortBatchId(id) = (id.hashCode() and 0xFFFF).toString(16).padStart(4,'0'), metaTime(sentAtMs, zone) HH:mm, unionRect(annotations): DeviceRect?, thumbnailRect(union, frameSize, designPad = 18f, designWidth = 296f), statusTone(status): StatusPillTone, inProgressCount(entries), noteCountKey(count). Sibling of core/stats/StatsFormatter.kt; unit-tested in commonTest. One file, one concern (avoids bugs.json#PP-TD-6's partitioning smell). |
| ThumbnailBitmap.kt | logic | new |  | presentation-feature-mirror/.../core/history/ThumbnailBitmap.kt — internal fun cropAndScale(frame: ImageBitmap, rect: DeviceRect, maxEdgePx: Int = 128): ImageBitmap using commonMain Canvas(ImageBitmap) + drawImageRect, so the existing CropEncoder encodes a few-KB PNG instead of a full-resolution union crop. No new DI seam. |
| MirrorContract (edits) | logic | reuse | presentation-feature-mirror/.../source/mirror/MirrorContract.kt | Add MirrorUiState.history: List<HistoryEntryModel> = emptyList() and MirrorIntent.VerifyBatch(val batchId: String). |
| MirrorViewModel (edits) | logic | reuse | presentation-feature-mirror/.../source/mirror/MirrorViewModel.kt | Two REQUIRED constructor params (recordSentBatch, observeHistory), no defaults; container init collects observeHistory() into state.history; resolveAndSend() encodes the thumbnail inside the existing assembly withContext and calls recordSentBatch in published.onSuccess before postSideEffect, consuming the Result with .onFailure { log.w } only; handleIntent gains the VerifyBatch log.i stub F-9 fills. |
| MirrorHistoryTab | ui | new |  | presentation-feature-mirror/.../source/mirror/MirrorHistoryTab.kt — fun MirrorHistoryTab(state, onIntent, modifier); empty-state early return copying MirrorNotesTab; Column { LazyColumn(Modifier.weight(1f)) { group header items (Theme.typography.overline + ink3) + HistoryRow items }; footer (monoMeta + ink3 above Theme.stroke.thin / Theme.color.line) }; two private light/dark previews. No new design-system component. |
| RectifyWindow.kt (edits) | ui | reuse | compose-application/src/desktopMain/.../source/window/RectifyWindow.kt | Edit 1 (~L238-258): replace `else -> Unit` with `ShellTab.History -> MirrorHistoryTab(state = mirror, onIntent = onIntent, modifier = Modifier.weight(1f))`. Edit 2 (~L428): tabCounts = buildMap { put(ShellTab.Notes, mirror.stagedCount); put(ShellTab.Payload, mirror.stagedCount); val n = inProgressCount(mirror.history); if (n > 0) put(ShellTab.History, n) }. Nothing testable lands here — :compose-application has no test wiring. |
| strings.xml additions | ui | reuse | presentation-core-localisation/src/commonMain/composeResources/values/strings.xml | 15 mirror_history_* keys, no AAPT-style apostrophe escapes (regression fixed in dae0893). |

#### dataModel

| entity | fields |
| --- | --- |
| history_entry (HistoryEntryEntity) | ["id: String (PK, = AnnotationEnvelope.sessionId)", "sentAtMs: Long", "status: String (BatchStatusModel name)", "title: String (first annotation's comment, may be empty)", "annotationCount: Int", "deviceLabel: String", "prompt: String", "dirPath: String? (null when the file write failed — SC-18)", "framePath: String?", "hierarchyPath: String?", "thumbPng: ByteArray? (few-KB derived thumbnail; null ⇒ bare plate)"] |
| history_annotation (HistoryAnnotationEntity) | ["batchId: String (FK → history_entry.id, onDelete CASCADE, @Index)", "index: Int (composite PK with batchId)", "left: Int", "top: Int", "right: Int", "bottom: Int", "comment: String", "cropPath: String?"] |
| HistoryEntryModel (domain read model) | ["id: String", "sentAtMs: Long", "status: BatchStatusModel", "title: String", "annotationCount: Int", "thumbPng: ByteArray?", "framePath: String?"] |
| SentBatchModel (domain write model) | ["id: String (sessionId)", "sentAtMs: Long", "deviceLabel: String", "prompt: String", "hierarchyXml: String?", "frameBytes: ByteArray", "thumbPng: ByteArray?", "annotations: List<SentAnnotationModel>"] |
| SentAnnotationModel | ["index: Int", "left: Int", "top: Int", "right: Int", "bottom: Int", "comment: String", "cropBytes: ByteArray?"] |
| BatchStatusModel (enum) | ["Sent", "InProgress", "Fixed", "Verified", "FollowUpSent"] |
| On-disk layout | ["~/.rectify/rectify.db (relocated, version 1)", "~/.rectify/history/<sanitised batchId>/frame.png", "~/.rectify/history/<sanitised batchId>/crop-N.png", "~/.rectify/history/<sanitised batchId>/hierarchy.xml", "~/.rectify/handoff/ (F-7's slot, untouched)"] |

#### interfaces

| name | contract |
| --- | --- |
| HistoryRepository | fun observe(): Flow<List<HistoryEntryModel>>; suspend fun record(batch: SentBatchModel); suspend fun updateStatus(batchId: String, status: BatchStatusModel) |
| RecordSentBatchUseCase | public suspend operator fun invoke(batch: SentBatchModel): Result<Unit> — resultLauncher(errorMapper = Failure.Technical::Database) |
| ObserveHistoryUseCase | public operator fun invoke(): Flow<List<HistoryEntryModel>> — bare Flow, never Flow<Result<T>> |
| HistoryDataSource | public interface HistoryDataSource : ObservableDataSource<List<HistoryEntryWithAnnotations>>, AppendableDataSource<HistoryEntryWithAnnotations>, UpsertableDataSource<HistoryEntryWithAnnotations> — plus suspend fun updateStatus(id: String, status: String) |
| HistoryEntryDao | @Transaction suspend fun insert(entry: HistoryEntryEntity, annotations: List<HistoryAnnotationEntity>); fun observeAll(): Flow<List<HistoryEntryWithAnnotations>>; suspend fun updateStatus(id: String, status: String) |
| BatchFileStore | public fun interface BatchFileStore { suspend fun write(batchId: String, files: List<Pair<String, ByteArray>>): String? } — never throws, null on failure |
| MirrorIntent.VerifyBatch | data class VerifyBatch(val batchId: String) — handled by a one-line log.i stub; F-9 fills the body without touching the tab or the window |
| MirrorUiState.history | val history: List<HistoryEntryModel> = emptyList() — single source for both the list and the badge |

#### stateManagement

Orbit MVI following presentation-feature-mirror's documented local precedent (MirrorHost, not Screen/Content; heavy pixel values in plain StateFlows outside the reducer). History adds exactly one reduced field, MirrorUiState.history, populated by a collector started in the container init block from ObserveHistoryUseCase's bare Flow. Thumbnail bytes are small and stay bytes (never decoded ImageBitmaps) in state, so deviation 2 in presentation-feature-mirror/CLAUDE.md is respected; decoding happens lazily inside the row. No second ViewModel, no second observation path, no new side effect type.

#### navigation

- No Destination, no NavigationHost branch, no navSavedStateConfiguration entry — History is a shell slot, not a nav destination (navigation.md's five-step checklist is deliberately not applied, as presentation-feature-mirror/CLAUDE.md already documents for this surface).
- Reached via the existing tab strip segment and the already-shipped Cmd+3 shortcut (shortcutToTab in ShellTab.kt); ShellState.onSessionStarted() still resets to Notes.

#### affectedModules

- data-database-impl — ADD src/commonMain/kotlin/data/database/impl/source/entity/HistoryEntryEntity.kt; ADD source/entity/HistoryAnnotationEntity.kt; ADD source/entity/HistoryEntryWithAnnotations.kt (@Embedded + @Relation); ADD core/dao/HistoryEntryDao.kt; ADD source/datasource/HistoryDataSource.kt; ADD core/datasource/HistoryDataSourceImpl.kt; EDIT core/database/AppDatabase.kt (entity set, historyEntryDao(), version stays 1); EDIT core/DatabaseBuilder.desktop.kt (~/.rectify/rectify.db + mkdirs + setQueryCoroutineContext(Dispatchers.IO)); EDIT di/DataDatabaseImplModule.kt (bind HistoryDataSource); DELETE core/entity/ExampleEntity.kt; DELETE core/dao/ExampleDao.kt; ADD schemas/data.database.impl.core.database.AppDatabase/1.json (committed); ADD src/desktopTest/kotlin/.../HistoryEntryDaoTest.kt. core/migration/Migrations.kt stays empty with a comment that the FIRST real migration must also add addMigrations(...) in all three DatabaseBuilder actuals.
- domain-core — ADD src/commonMain/.../source/model/history/HistoryEntryModel.kt, BatchStatusModel.kt, SentBatchModel.kt (incl. SentAnnotationModel). Zero new dependencies.
- domain-repository-api — ADD src/commonMain/.../source/repository/HistoryRepository.kt.
- domain-usecase-api — ADD src/commonMain/.../source/usecase/history/RecordSentBatchUseCase.kt and ObserveHistoryUseCase.kt.
- domain-usecase-impl — ADD src/commonMain/.../source/usecase/history/RecordSentBatchUseCaseImpl.kt and ObserveHistoryUseCaseImpl.kt; EDIT di/DomainUsecaseImplModule.kt (two bindings appended inside the existing layer group).
- data-repository-impl — EDIT build.gradle.kts (add implementation(projects.dataDatabaseImpl) — a new module edge; dataDatabaseApi only today); ADD core/mapper/HistoryEntryMapper.kt; ADD source/repository/HistoryRepositoryImpl.kt (internal); ADD source/store/BatchFileStore.kt; ADD src/desktopMain/.../core/store/FileSystemBatchFileStore.kt; ADD di/BatchStoreProvider.kt + BatchStoreProvider.desktop.kt + BatchStoreProvider.android.kt + BatchStoreProvider.ios.kt; EDIT di/DataRepositoryImplModule.kt (repository + store bindings); ADD src/commonTest/.../HistoryRepositoryImplTest.kt and HistoryEntryMapperTest.kt.
- presentation-feature-mirror — EDIT build.gradle.kts (add libs.kotlinx.datetime to commonMain); ADD core/history/HistoryFormatting.kt; ADD core/history/ThumbnailBitmap.kt; EDIT source/mirror/MirrorContract.kt (MirrorUiState.history, MirrorIntent.VerifyBatch); EDIT source/mirror/MirrorViewModel.kt (two required ctor params, container-init collection, thumbnail encode inside the existing assembly withContext, post-publish persist in onSuccess before postSideEffect, VerifyBatch stub); ADD source/mirror/MirrorHistoryTab.kt (+ two private previews); EDIT di/PresentationFeatureMirrorModule.kt (the explicit viewModel { } block gains two get<T>() params); EDIT src/commonTest/.../MirrorFakes.kt (two trivial fakes); ADD src/commonTest/.../HistoryFormattingTest.kt.
- presentation-core-localisation — EDIT src/commonMain/composeResources/values/strings.xml: mirror_history_empty, mirror_history_group_today, mirror_history_group_yesterday, mirror_history_group_older, mirror_history_meta, mirror_history_meta_note_one, mirror_history_meta_note_other, mirror_history_verify, mirror_history_status_sent, mirror_history_status_in_progress, mirror_history_status_fixed, mirror_history_status_verified, mirror_history_status_followup, mirror_history_footer, mirror_history_thumbnail_description.
- compose-application — EDIT src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt, exactly two edits: (1) ~L238-258 `else -> Unit` becomes `ShellTab.History -> MirrorHistoryTab(state = mirror, onIntent = onIntent, modifier = Modifier.weight(1f))`; (2) ~L428 tabCounts gains `ShellTab.History to <in-progress count>` via buildMap, key omitted at zero. No logic beyond those two lines — the module has no test wiring.
- docs — EDIT .claude/rules/database.md (one line: an @Entity exposed as a public datasource's wire type lives under source/entity/, not core/entity/, because core/ is internal-only; plus a note that F-8 stayed at version 1 only because the DB was relocated to a provably empty path); EDIT data-database-impl/CLAUDE.md (drop the ExampleEntity placeholder framing, record the new path and the first real tables).

### projectContext

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| history-row | HistoryRow | components.json#history-row |
| status-pill | StatusPill / StatusPillTone | components.json#status-pill |
| app-button | AppButton (AppButtonSize.ListAction) | components.json#app-button |
| side-panel-frame | SidePanelFrame (ShellTab.History slot) | components.json#side-panel-frame |
| sidebar-tabs | SidebarTabs (per-tab count badge) | components.json#sidebar-tabs |
| rectify-window-shell | RectifyWindowShell (tabCounts + weight(1f) contract) | components.json#rectify-window-shell |

#### reuseAssets

- No new asset. The thumbnail is generated at runtime; SC-18's placeholder is the bare Theme.color.chrome/stage plate HistoryRow already draws when the thumbnail slot is null — no placeholder image is added.

#### reuseStyles

- styles.json#theme-accessor / #app-theme — all new UI reads Theme.*; no literal Color/dp/TextStyle.
- styles.json#theme-typography — Theme.typography.overline (uppercase group header), .body (title), .monoMeta (meta line + footer).
- styles.json#theme-color — ink/ink2/ink3, line/line2, hover, stage, chrome, primary/onPrimary.
- styles.json#theme-spacing, #theme-radius-corner, #theme-stroke — spacingXS/S/L, cornerToken.nested, stroke.thin.
- styles.json#theme-motion — reducedMotion resolves centrally; no component-level motion branch.

#### existingFeatures

- features.json#database — 'Database (Room KMP) — scaffold only', status partial, waived 'No production consumer'. F-8 is that consumer; the entry must be re-tagged and un-waived when this lands.
- features.json#drag-annotate-frozen-frame — supplies FrozenFrame/StagedAnnotation/DeviceRect and the frozen-frame bytes.
- features.json#app-shell-window-layout — supplies the tab strip, ShellTab.History and the Cmd+3 shortcut.
- features.json#error-propagation — Failure.Technical.Database + resultLauncher, reused verbatim.
- features.json#localisation — Res.string.* and AppLocaleProvider; new keys go to values/strings.xml only (no other locale ships yet).
- F-7 resolve-and-send-mcp — merged, not yet registered in features.json; supplies AnnotationHandoff, AnnotationEnvelope, mintSessionId, EnvelopeAssembly, CropEncoder.

#### newToBuild

- HistoryEntryEntity + HistoryAnnotationEntity + HistoryEntryWithAnnotations (first real Room tables)
- HistoryEntryDao
- HistoryDataSource + HistoryDataSourceImpl
- HistoryEntryModel / BatchStatusModel / SentBatchModel / SentAnnotationModel
- HistoryRepository + HistoryRepositoryImpl + HistoryEntryMapper
- BatchFileStore + FileSystemBatchFileStore + BatchStoreProvider (4 files)
- RecordSentBatchUseCase + ObserveHistoryUseCase (api + impl)
- HistoryFormatting.kt + ThumbnailBitmap.kt (pure, unit-tested)
- MirrorHistoryTab.kt (composed entirely from existing design-system components)
- HistoryFormattingTest, HistoryRepositoryImplTest, HistoryEntryMapperTest, HistoryEntryDaoTest

### resources

#### rules

| ref | why |
| --- | --- |
| .claude/rules/database.md | Entity/DAO/AppDatabase placement, AppDatabaseConstructor's mandatory body-less override fun initialize(), kspDesktop, and the bump-and-migrate rule this plan is explicitly exempt from (relocated path, provably no install). Needs a one-line amendment for source/entity/. |
| .claude/rules/domain-layer.md | One-shot ⇒ Result<T>, reactive ⇒ bare Flow<T> (never Flow<Result<T>>); every *UseCaseImpl wrapped in resultLauncher(errorMapper = Failure.Technical::Database); presentation never imports a data-*-impl type. |
| .claude/rules/mappers.md | HistoryEntryMapper is one internal object implementing ModelResourceMapper under data-repository-impl/core/mapper/ and must never throw — unknown status falls back to Sent. |
| .claude/rules/koin-di.md | One Koin module per Gradle module; initKoin()'s data → domain → presentation order is load-order-significant; append inside the layer group; presentationFeatureMirrorModule keeps its explicit viewModel { } form. |
| .claude/rules/orbit-mvi.md + presentation-feature-mirror/CLAUDE.md | Follow mirror's local precedent (MirrorHost, heavy values outside the reducer), not the generic Screen/Content shape. |
| .claude/rules/design-tokens.md | No literal Color/dp/TextStyle; atom → molecule → organism layering; reducedMotion resolved centrally. |
| .claude/rules/package-layout.md | core/ internal-only, source/ public — the reason the public entity lands in source/entity/ and the datasource impl in core/datasource/. |
| .claude/rules/localisation.md | All copy via Res.string.*, <feature>_<element> key naming, no AAPT-style apostrophe escapes (dae0893). |
| .claude/rules/testing.md | kotlin.test is wired by the library/feature convention plugins only; :compose-application has none. Desktop test tasks named per module; commonTest compiles for iOS so test names carry no comma or colon; run build, not just desktopTest, before declaring done. |
| .claude/rules/platform-coverage.md | Desktop is jvm("desktop") only; every commonMain expect needs a desktop actual; Android/iOS get null-returning stand-ins (AS-29). |
| .claude/rules/compose-preview-convention.md | MirrorHistoryTab ships private light+dark previews wrapped in AppTheme with default-constructed state. |
| .claude/rules/error-handling.md | Failure taxonomy and the Result boundary; F-8 deliberately stops at log.w instead of ShowError because it has no error surface. |
| .claude/rules/navigation.md | Explicitly NOT applied — History is a shell slot, not a Destination; recorded so a reviewer does not flag the omission. |
| data-handoff-api/CLAUDE.md | 'No history, no ring buffer, no expiry' — the contract statement that makes F-8's own copy mandatory and forbids reading the slot back. |

#### skills


#### agents


#### docs

- data-database-impl/CLAUDE.md — the Room KMP constructor pattern, KSP target list, schemas/ export and the 'replace the placeholder' checklist (step 4 of which F-8 is exempt from).
- presentation-feature-mirror/CLAUDE.md — the two documented Orbit deviations and the commonTest naming/dispatcher facts MirrorFakes depends on.
- .pipeline/sdd/docs/history-batch-tracking/inputs/codebase-recon.md — authoritative file/symbol map; do not re-derive.
- .pipeline/sdd/docs/history-batch-tracking/inputs/design-history-tab.md — the pixel contract (group header, row anatomy, footer, tone table).

### errorStates

| state | behavior |
| --- | --- |
| empty | No persisted batches: MirrorHistoryTab early-returns an empty state copying MirrorNotesTab's shape, with mirror_history_empty. The footer is hidden when there is no batch (AC-37 is conditioned on 'at least one batch persisted'). |
| loading | No spinner. MirrorUiState.history defaults to emptyList() and the first Flow emission replaces it, typically within a frame of the panel opening; a transient empty state is indistinguishable from the genuine empty state and needs no extra UI. |
| error | DB read failure: HistoryRepositoryImpl.observe() is guarded with .catch { log.w(it); emit(emptyList()) }, so the tab degrades to the empty state and the mirror surface never crashes. |
| error | DB write failure (SQLiteException, disk full, locked file): mapped to Failure.Technical.Database by resultLauncher, returned as Result.failure, consumed in resolveAndSend with .onFailure { log.w } only. SendPhase is untouched, no side effect is posted, the send still reports success. One batch is silently missing from History — the accepted trade. |
| error | File write failure (SC-18): BatchFileStore returns null instead of throwing; the row is still inserted with dirPath/framePath/hierarchyPath = null and its thumbPng intact, so it renders completely. Only F-9's future diff needs the frame, and it existence-checks at use time. |
| error | Crop/thumbnail encoder returned null (Android/iOS stand-in, or any AWT failure): thumbPng is null and HistoryRow draws its bare plate. No separate placeholder asset exists or is needed. |
| error | Stored image path whose file the user deleted by hand: the row renders unchanged, because rendering reads only DB columns (thumbnail BLOB included) and performs zero filesystem reads. This is structural, not defensive. |
| error | Batch with zero annotations: unblocked by canSend today, but unionRect returns null, the thumbnail encode is skipped, thumbPng is null, the title falls back to neutral localised copy and the meta line reads '0 notes'. Nothing crashes. |
| error | DB open failure on first launch (unwritable ~/.rectify, corrupt file): Room builds lazily and opens on first query, so the failure surfaces inside the guarded observe() Flow (empty tab + log.w) or inside resultLauncher on the first write (logged), never as a Koin graph construction crash. DatabaseBuilder.desktop mkdirs() the directory first so the common case cannot fail. |
| error | Same-millisecond id collision: @Insert(onConflict = REPLACE) overwrites the parent row and its cascaded children rather than throwing. |

### observability

#### logs

- log.w on a failed recordSentBatch, including the batch id and the Failure type — the only trace of a silently missing row.
- log.w inside HistoryRepositoryImpl.observe()'s .catch before emitting emptyList().
- log.w inside FileSystemBatchFileStore's runCatching when a file write fails (path + reason), before returning null.
- log.i one-liner in the MirrorIntent.VerifyBatch stub naming the batch id — the handler body F-9 replaces.
- log.d (debug only) of the resolved database path on first build, so a support question about 'where is my data' is one log line.

#### metrics

- None shipped. NFR-4 forbids egress and no local metrics sink exists; the performance budget below is verified by manual measurement during the demo, not by instrumentation.

#### analyticsEvents

- None. No analytics pipeline exists in this product and NFR-4 rules one out for this data.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | The thumbnail plate carries a contentDescription from mirror_history_thumbnail_description (localised, batch-title interpolated); a null thumbnail contributes no empty-string announcement. |
| A11Y-2 | Status is never colour-only: StatusPill always renders its text label alongside the tone dot, so the five statuses are distinguishable without colour perception. |
| A11Y-3 | The Verify button uses AppButtonSize.ListAction's shipped hit target and is reachable by keyboard focus traversal within the LazyColumn; it is omitted (not hidden-but-focusable) on non-Fixed rows so focus order matches what is visible. |
| A11Y-4 | Group headers use Theme.typography.overline with Theme.color.ink3 and act as list section labels; they are plain text items in the LazyColumn so screen-reader traversal hits them in order. |
| A11Y-5 | The History tab is reachable without a pointer via the already-shipped Cmd+3 shortcut (shortcutToTab), and the panel's existing focus handling is unchanged. |
| A11Y-6 | Title and meta are ellipsised, never truncated mid-glyph, and the full comment remains the semantic text of the row so assistive tech can read beyond the visual clip. |
| A11Y-7 | No new animation is introduced; any scroll/hover affordance resolves through Theme.motion so reducedMotion is honoured centrally. |

### performanceBudget

| metric | target |
| --- | --- |
| Thumbnail BLOB size (p95) | <= 8 KB per row (<=128px longest edge, PNG). Enforced by cropAndScale's maxEdgePx before encoding. |
| Added latency on resolveAndSend (p95) | <= 150 ms for a 3-annotation batch (thumbnail encode + ~1 MB of file writes + one transaction), inside an already multi-second operation; all of it off the UI thread (NFR-14). |
| History list query + map (1000 rows) | < 100 ms per emission on desktop, with queries on Dispatchers.IO via setQueryCoroutineContext. |
| Reduced-state memory for history (1000 rows) | <= 10 MB of thumbnail bytes in MirrorUiState. |
| Scroll cost | Independent of total batch count (NFR-13) — LazyColumn composes only visible rows; the footer sits outside the scroll (design's flex:none). |
| Emission frequency | One state emission per send (not per frame); no polling, no midnight timer. |
| Disk growth | Unbounded by design (accepted ceiling); ~0.5-2 MB per batch of frame + crops + hierarchy. Recorded in a `ponytail:` comment in BatchFileStore with the age/count-cap upgrade path. |

### i18n

#### locales

- en

#### strings

- mirror_history_empty
- mirror_history_group_today
- mirror_history_group_yesterday
- mirror_history_group_older
- mirror_history_meta
- mirror_history_meta_note_one
- mirror_history_meta_note_other
- mirror_history_verify
- mirror_history_status_sent
- mirror_history_status_in_progress
- mirror_history_status_fixed
- mirror_history_status_verified
- mirror_history_status_followup
- mirror_history_footer
- mirror_history_thumbnail_description

#### notes

Only values/strings.xml (English) exists in this repo; no other locale ships yet. Keys follow <feature>_<element>. PLAN-AS-2: the repo has zero pluralStringResource usages and zero <plurals> resources, so the note count ships as two plain keys selected by a pure function — never a hand-concatenated 's'. No AAPT-style apostrophe escapes (regression fixed in dae0893). The footer string carries the separator and the 'claude' literal with the session id interpolated; the meta string interpolates note count, time and short id. Every piece of copy in MirrorHistoryTab resolves through stringResource — the contract types carry no labels, matching the module's existing convention.

### security

#### dataHandling

- NFR-4: nothing leaves the machine. No HTTP client exists in the dependency graph of any module F-8 touches, and none is added; storage is the local filesystem plus a local SQLite file.
- All app-owned data consolidates under ~/.rectify (rectify.db + history/ + the existing handoff/), so a user who wants to erase Rectify's data deletes one directory.
- Hierarchy XML can be hundreds of KB and may contain on-screen text: it is written as a file inside the batch directory, never into a list-query column, and never logged.
- Annotation comments and prompts are persisted in TEXT columns and never emitted to logs; log lines carry batch ids only.
- Batch directory names are sanitised with [^A-Za-z0-9._-] replaced by '_' so a device id can never escape the history root via path traversal.

#### permissions

- Desktop filesystem access to the user's home directory only — already exercised by F-7's handoff slot; no new OS permission, no new entitlement, no network permission.

#### privacy

- No analytics, no telemetry, no crash reporting carries annotation text, prompts, screenshots or hierarchy dumps.
- No retention policy ships (FR-36): data persists until the user deletes ~/.rectify/history. This is a deliberate, recorded ceiling; a future retention spec owns the delete affordance.
- File permissions are whatever java.io creates under the user's home (owner-readable); no attempt is made to broaden them.

### rollout

#### featureFlags

- None. The tab segment already exists and currently renders nothing (`else -> Unit`); filling it is additive and desktop-only, so a flag would guard a hole rather than a risk.

#### migrations

- No Room Migration. @Database(version = 1) stands because the desktop file relocates from ~/rectify.db to ~/.rectify/rectify.db in the same change and neither path exists on any machine (verified on disk; AppDatabase is a lazy Koin single nothing has ever injected). The relocation and the stay-at-v1 ship together or not at all.
- Migrations.kt stays empty, with a comment that the first real migration (likely F-9's per-annotation resolved column) must bump the version, add the Migration AND add addMigrations(...) to all three DatabaseBuilder actuals, which no builder calls today.
- schemas/<AppDatabase>/1.json is regenerated and committed, replacing the example-table schema.

#### backwardCompatibility

Zero changes to F-7's merged wire contract (AnnotationEnvelope field order, AnnotationHandoff, mintSessionId, the handoff slot layout and its pruning). ExampleEntity/ExampleDao are deleted safely because nothing injects them and no database file containing the example table can exist. MirrorViewModel gains two required constructor params — an internal, compile-time-visible break fixed in the same commit (MirrorFakes + the Koin viewModel { } block); no public API outside the module changes. Android/iOS keep compiling via null-returning stand-ins and are behaviourally unchanged.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json#PP-TD-1 | Crop cell fitted to a constant 84dp but drawn in weight(1f), clipping the outline at narrow panel widths; cropWindow's KDoc about the pad was inaccurate. | Repeating the same mistake in the thumbnail: sizing the crop against a constant while the plate is laid out by the parent. The thumbnail plate is HistoryRow's fixed 56dp (not a weighted cell), and thumbnailRect scales its pad by frameWidth/designWidth exactly like cropWindow does. | Compute thumbnailRect purely from the frame size and the union rect, encode once at persist time, and let the plate scale the decoded bitmap with ContentScale — no composable-side arithmetic (the constraint TAC-4 already imposed). |
| bugs.json#PP-TD-4 | Tautological test asserting a literal against itself (assertEquals(1, PAYLOAD_FORMAT_VERSION)) — would pass under any edit. | The same shape creeping into HistoryFormattingTest, e.g. asserting the status-to-tone map by restating the map, or asserting shortBatchId against hashCode recomputed in the test. | Assert behaviour against hand-computed literals: a fixed id string to a fixed 4-char expected value, three concrete rects to one concrete union, three fixed epochs across a day boundary to the expected group split. |
| bugs.json#PP-TD-5 | Test name and comment claimed a structural property the body never asserted (Empty to empty, twice). | A groupHistory test named for ordering that only checks group names, or an SC-18 test named for the row surviving that only checks the store returned null. | Every new test's assertions must name-match its title; the SC-18 test asserts the datasource received an insert with null paths AND a non-null thumbnail, not merely that nothing threw. |
| bugs.json#PP-TD-6 | core/payload file names do not partition their contents — one label function in PayloadLabels.kt while two live in PayloadEstimate.kt; PayloadGate.kt holds three unrelated predicates. | core/history/ repeating it by scattering grouping, formatting and geometry across ad-hoc files, or by becoming a junk drawer. | Exactly two files with crisp boundaries: HistoryFormatting.kt (pure text/time/id/tone/geometry decisions) and ThumbnailBitmap.kt (the one non-pure bitmap helper). Anything that is neither goes in a third, named file — never appended to these. |
| bugs.json#PP-TD-8 | Kotlin/Native release linker OOMs under ~4 parallel linkReleaseFramework tasks against a 4 GiB daemon heap. | The cross-target gate (step 13) hanging or dying with 'Java heap space' — pre-existing, affects every cross-target build in this repo, not caused by F-8. | Run the cross-target gate with --max-workers=1, as every prior spec did. Do not debug it as an F-8 failure; the real fix (kotlin.native.linkerThreads or a bigger daemon heap) is its own task. |
| bugs.json#PP-TD-3 | HierarchyRow's `matched` is exactly `tag != null` at every call site yet defaults false, dimming the tree for forgetful consumers — a defaulted parameter that silently degrades behaviour. | The same trap in the new code: a defaulted MirrorViewModel use-case parameter would turn a missing Koin binding into silently losing every batch. | recordSentBatch and observeHistory are REQUIRED constructor params with no defaults (matching handoff's documented reasoning); MirrorFakes is updated in the same commit to keep the test suite compiling. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 0 | BLOCKING SPIKE — prove Room runs on desktop. Relocate DatabaseBuilder.desktop.kt to ~/.rectify/rectify.db (mkdirs first) plus .setQueryCoroutineContext(Dispatchers.IO), inject AppDatabase somewhere temporary, and prove one insert + one Flow emission. Confirms KSP output, the generated AppDatabaseConstructor actual, BundledSQLiteDriver linkage in the desktop run configuration, and schemas/ export. Timeboxed; a packaging fight is a scope conversation, not an in-branch debug session. | [] | ["AC-33"] | 0.5d |
| 1 | data-database-impl tables: add HistoryEntryEntity + HistoryAnnotationEntity + HistoryEntryWithAnnotations under source/entity/, HistoryEntryDao under core/dao/ (@Transaction insert, observeAll ORDER BY sentAtMs DESC, status UPDATE); update AppDatabase (entity set, historyEntryDao(), version stays 1, keep the body-less initialize()); delete ExampleEntity/ExampleDao; commit schemas/.../1.json. | [0] | ["AC-33", "AC-22"] | 0.5d |
| 2 | data-database-impl datasource: public HistoryDataSource in source/datasource/ mixing the data-database-api bases, internal DAO-backed impl in core/datasource/, registered in dataDatabaseImplModule. Convert the step-0 spike into the committed desktopTest HistoryEntryDaoTest (insert parent + 2 children, observe, assert order and cascade). | [1] | ["AC-33"] | 0.5d |
| 3 | domain-core models: HistoryEntryModel, BatchStatusModel (5 values), SentBatchModel + SentAnnotationModel under source/model/history/. Zero dependencies. | [0] | ["AC-22", "AC-33"] | 0.25d |
| 4 | domain-repository-api: HistoryRepository with observe() / record() / updateStatus() — updateStatus exposed for F-9 and never called by F-8 (no use case). | [3] | ["AC-34"] | 0.25d |
| 5 | data-repository-impl: add the implementation(projects.dataDatabaseImpl) edge; HistoryEntryMapper (never throws, unknown status to Sent); BatchFileStore fun interface + desktop FileSystemBatchFileStore + BatchStoreProvider expect/actual with null stand-ins; HistoryRepositoryImpl (store then datasource, observe() guarded with .catch); register in dataRepositoryImplModule. Tests: HistoryRepositoryImplTest (SC-18: store returns null so the row is still inserted with null paths and a non-null thumbnail) and HistoryEntryMapperTest (unknown status falls back). | [2, 4] | ["AC-38", "AC-33"] | 1d |
| 6 | domain-usecase-api + domain-usecase-impl: RecordSentBatchUseCase (Result<Unit> via resultLauncher(errorMapper = Failure.Technical::Database)) and ObserveHistoryUseCase (bare Flow); two bindings appended to the usecase Koin module inside the existing layer group. | [5] | ["AC-33"] | 0.25d |
| 7 | presentation-feature-mirror pure core: add libs.kotlinx.datetime; write core/history/HistoryFormatting.kt (groupHistory, metaTime, shortBatchId, unionRect, thumbnailRect, statusTone, inProgressCount, noteCountKey) and core/history/ThumbnailBitmap.kt (cropAndScale to <=128px). Write HistoryFormattingTest in commonTest covering the Today/Yesterday/Older boundaries, newest-first order inside a group, unionRect over 3 scattered rects (SC-17/AC-35), shortBatchId against a fixed literal, the full status-to-tone map, and inProgressCount including the zero case (AC-34). camelCase test names only. | [3] | ["AC-35", "AC-22", "AC-34"] | 0.75d |
| 8 | presentation-feature-mirror wiring: MirrorUiState.history + MirrorIntent.VerifyBatch; two required MirrorViewModel constructor params; container-init collection of observeHistory(); thumbnail encode inside the existing assembly withContext(Dispatchers.Default); recordSentBatch in published.onSuccess BEFORE postSideEffect, consumed with .onFailure { log.w } only; VerifyBatch log.i stub; update MirrorFakes.kt and the explicit viewModel { } block in PresentationFeatureMirrorModule. | [6, 7] | ["AC-38", "AC-33", "AC-22"] | 0.75d |
| 9 | presentation-core-localisation: add the 15 mirror_history_* keys to values/strings.xml, no apostrophe escapes, two note-count keys instead of a plural resource (PLAN-AS-2). | [] | ["AC-22", "AC-37"] | 0.25d |
| 10 | presentation-feature-mirror UI: MirrorHistoryTab.kt — empty-state early return, Column { LazyColumn(Modifier.weight(1f)) { group headers + HistoryRow items with decoded thumbnail, meta line, StatusPill and a Fixed-only Verify button }; footer 'claude · session <id>' from the newest entry }, plus two private light/dark previews. Zero new design-system components. | [8, 9] | ["AC-22", "AC-36", "AC-37"] | 0.75d |
| 11 | compose-application: the two RectifyWindow.kt edits — `else -> Unit` becomes `ShellTab.History -> MirrorHistoryTab(state = mirror, onIntent = onIntent, modifier = Modifier.weight(1f))`, and tabCounts via buildMap with the History key omitted when the In-progress count is zero. | [10] | ["AC-22", "AC-34"] | 0.25d |
| 12 | Docs + registry deltas: amend .claude/rules/database.md (source/entity/ placement for entities exposed through a public datasource; the relocated-path rationale for staying at version 1) and data-database-impl/CLAUDE.md; report the features.json#database re-tag and the new-component deltas to the orchestrator (never edit the registry here). | [11] | [] | 0.25d |
| 13 | Verify: ./gradlew :presentation-feature-mirror:desktopTest :data-repository-impl:desktopTest :data-database-impl:desktopTest, then the cross-target build (:presentation-feature-mirror:build :data-repository-impl:build :data-database-impl:build, --max-workers=1 per bugs.json#PP-TD-8), then the manual demo: send two batches, quit, relaunch, both rows intact with thumbnails (AC-33/SC-15); hand-seed one In-progress row for the badge (AC-34) and one Fixed row for the Verify button (AC-36); delete ~/.rectify/history/<id>/ and confirm the row still renders. | [11] | ["AC-22", "AC-33", "AC-34", "AC-35", "AC-36", "AC-37", "AC-38"] | 0.5d |

### testStrategy

#### unit

- presentation-feature-mirror commonTest — HistoryFormattingTest: groupHistory puts a now-epoch under Today, a now-minus-26h epoch under Yesterday and a now-minus-5d epoch under Older using a fixed zone and fixed nowMs; rows inside a group come back newest-first; empty input yields no groups. Task: ./gradlew :presentation-feature-mirror:desktopTest
- presentation-feature-mirror commonTest — unionRect over three scattered DeviceRects returns the hand-computed min/max envelope and is NOT the first rect (AC-35/SC-17); unionRect(emptyList()) returns null (zero-annotation batch).
- presentation-feature-mirror commonTest — thumbnailRect expands the union by pad = 18 * frameWidth / 296 and clamps to the frame on all four sides (FR-29 math, prototype-equivalent).
- presentation-feature-mirror commonTest — shortBatchId maps a fixed sessionId literal to a fixed 4-char lowercase hex string, padded; deterministic across runs (FR-30 correction of record).
- presentation-feature-mirror commonTest — statusTone maps all five BatchStatusModel values to the expected StatusPillTone (Fixed to Done, InProgress to Busy, FollowUpSent to Alert, Verified to Neutral, Sent to Neutral), asserted against literals rather than by restating the map (bugs.json#PP-TD-4).
- presentation-feature-mirror commonTest — inProgressCount returns 0 for a list with no In-progress entries and N otherwise; the RectifyWindow rule 'omit the key at zero' is asserted at the function boundary since the window itself has no test wiring (AC-34).
- presentation-feature-mirror commonTest — noteCountKey selects the one/other string key at 1 and at N (PLAN-AS-2).
- data-repository-impl commonTest — HistoryEntryMapperTest: toModel on an unrecognised status string falls back to Sent and never throws; toModel/toResource round-trip preserves id, sentAtMs, title, annotationCount and thumbPng. Task: ./gradlew :data-repository-impl:desktopTest
- data-repository-impl commonTest — HistoryRepositoryImplTest (SC-18): with a fake BatchFileStore returning null, the fake datasource still receives exactly one insert whose dirPath/framePath/hierarchyPath are null and whose thumbPng is non-null; with a store returning a directory, the inserted paths are that directory's children.
- data-repository-impl commonTest — observe() with a datasource flow that throws emits emptyList() instead of propagating (guarded .catch).

#### integration

- data-database-impl desktopTest — HistoryEntryDaoTest (the committed artefact of the step-0 spike): build a temp-file AppDatabase with BundledSQLiteDriver, insert a parent plus two annotation children in one transaction, assert observeAll() emits them ordered sentAtMs DESC with both children attached, assert updateStatus changes the status column, assert deleting the parent cascades the children, and assert a re-opened database on the same file still returns the row (AC-33's automated half). Task: ./gradlew :data-database-impl:desktopTest
- presentation-feature-mirror commonTest (existing MirrorViewModel harness) — a resolveAndSend test with a failing RecordSentBatchUseCase asserts SendPhase never becomes Failed and SendComplete is still posted; a succeeding one asserts the persist call happens before the side effect and that state.history reflects the fake flow (SC-16).

#### ui

- No Compose UI-test harness exists in this repo and F-8 is deliberately not the spec that introduces one (that is a real infrastructure decision).
- Two private @Preview pairs (light + dark, AppTheme-wrapped) on MirrorHistoryTab: one populated with all five statuses across two groups, one empty state.
- Manual demo script, recorded in the PR: (1) send two batches, quit, relaunch, open History — both rows with thumbnails, titles, meta lines and Sent chips (AC-33/SC-15); (2) with History open, send a third — a new row appears at the top of Today and the badge does not change (SC-16); (3) send a 3-annotation batch and eyeball the union crop (AC-35/SC-17); (4) hand-seed status InProgress on one row — badge shows 1; remove it — badge disappears (AC-34); (5) hand-seed status Fixed — Verify appears on that row only and logs on click (AC-36); (6) read the footer against the newest row's sessionId (AC-37); (7) delete ~/.rectify/history/<id>/ — the row still renders; (8) confirm ~/.rectify/history/<id>/ holds frame.png, crop-N.png and hierarchy.xml after a send (AC-38).

#### linkedScenarios

- SC-8
- SC-15
- SC-16
- SC-17
- SC-18

#### coverageGoal

100% of the pure functions in core/history/ and of HistoryEntryMapper's fallback branch; both HistoryRepositoryImpl write branches (store-ok and store-null) and its observe() error branch; the DAO's insert/observe/update/cascade paths. No coverage target for composables — they have no harness and are covered by previews plus the manual script. SC-8 is only partially coverable in F-8: the Verify affordance and its visibility rule are, the diff behind it belongs to F-9.

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-22 | ["FR-20", "FR-28", "FR-29", "FR-30", "FR-31"] | ["MirrorHistoryTab", "HistoryRow", "StatusPill", "HistoryFormatting.kt", "ObserveHistoryUseCase", "HistoryRepositoryImpl", "HistoryEntryDao", "RectifyWindow.kt (edit 1)"] | ["HistoryFormattingTest groupHistory cases", "HistoryEntryDaoTest", "SC-15", "SC-16", "previews + manual demo step 1"] |
| AC-33 | ["FR-26", "FR-27", "FR-36"] | ["HistoryEntryEntity", "HistoryAnnotationEntity", "HistoryEntryDao", "HistoryDataSource", "HistoryRepositoryImpl", "RecordSentBatchUseCase", "DatabaseBuilder.desktop (edit)", "BatchFileStore"] | ["HistoryEntryDaoTest (re-open same file, cascade, ordering)", "HistoryRepositoryImplTest", "SC-15", "manual demo step 1"] |
| AC-34 | ["FR-34", "FR-33"] | ["HistoryFormatting.kt#inProgressCount", "MirrorUiState.history", "RectifyWindow.kt (edit 2, buildMap omit-at-zero)", "SidebarTabs", "HistoryRepository.updateStatus"] | ["HistoryFormattingTest inProgressCount (zero and N)", "HistoryEntryDaoTest updateStatus", "SC-16", "manual demo step 4 (hand-seeded row)"] |
| AC-35 | ["FR-29"] | ["HistoryFormatting.kt#unionRect", "HistoryFormatting.kt#thumbnailRect", "ThumbnailBitmap.kt#cropAndScale", "CropEncoder"] | ["HistoryFormattingTest unionRect over three scattered rects", "HistoryFormattingTest thumbnailRect padding and clamping", "SC-17", "manual demo step 3"] |
| AC-36 | ["FR-32", "FR-31"] | ["MirrorHistoryTab (trailing slot)", "AppButton (ListAction)", "MirrorIntent.VerifyBatch", "HistoryFormatting.kt#statusTone"] | ["HistoryFormattingTest statusTone", "SC-8 (first three steps only)", "previews with all five statuses", "manual demo step 5"] |
| AC-37 | ["FR-35"] | ["MirrorHistoryTab footer", "HistoryEntryModel id/sessionId", "strings.xml#mirror_history_footer"] | ["inspection against the newest row's sessionId", "previews", "manual demo step 6"] |
| AC-38 | ["FR-27", "FR-26"] | ["assembleEnvelope (in-memory bytes)", "MirrorViewModel.resolveAndSend (persist in onSuccess)", "BatchFileStore", "FileSystemBatchFileStore", "HistoryEntryEntity.framePath"] | ["HistoryRepositoryImplTest (paths written / null-on-failure)", "SC-18", "manual demo steps 7-8 (files present after a later send; row survives directory deletion)"] |

### openQuestions

- Should Sent get its own distinct tone/colour once product reviews the design, instead of sharing Verified's neutral ink3 tone (AS-4)?
- If disk usage from retained frame/crop/hierarchy copies becomes a real problem, what retention policy (age cap, count cap, manual delete) should a future spec add? No cap is in scope for F-8.
- Should a future MCP tool or heartbeat channel exist so the 'editing X.kt' activity sub-line becomes possible, or is that permanently out of scope for a single-request-response MCP server?
- HistoryRow ships a 56dp thumbnail plate while the design quotes 44x44 (PLAN-AS-4). Reused as-is here; does the design-system owner want the plate parameterised, or the design updated to 56?
- Confirm during step 7 that commonMain Canvas(ImageBitmap) + drawImageRect behaves off the main thread on the desktop Skiko renderer; the documented fallback is a desktop AwtCropEncoder sibling doing getSubimage + getScaledInstance.
- database.md needs one amended line (entities exposed through a public datasource live under source/entity/) — confirm the doc delta is acceptable rather than paying ~60 lines of DTO-copying boilerplate to satisfy the current wording.

### readyForBacklog

true
