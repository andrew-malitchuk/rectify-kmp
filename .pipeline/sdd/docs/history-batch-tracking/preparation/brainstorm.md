# history-batch-tracking — brainstorm

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | brainstorm |
| status | done |
| producedBy | sdd-build-architecture-brainstorm |
| schemaVersion | 1 |
| createdAt | 2026-10-18T18:36:27Z |
| updatedAt | 2026-10-18T18:36:27Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T18:36:27Z

## Provenance

### selfHash

b1b3f183efd501af308dcad73c8435138ed86edf411287bdb8cb998ed0ac7a5c

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | a5bed37cd18f641dd903ce2bbddc2c3d4346ed08fcace9dde987538436614ec8 |
| preparation/clarify.json | d974c9ddd8498776c1d1e0745017c59654b2d912616181cb1110bca3021f73f3 |

## Data

### highLevelPlan

- Step 0 (blocking spike): Room has NEVER been instantiated in this repo — `AppDatabase` is a lazy Koin `single` nothing injects, so KSP output, `BundledSQLiteDriver` linkage, the `expect object AppDatabaseConstructor` actuals and `schemas/` export have never been exercised at runtime on any target. Prove a trivial insert+observe round-trip on desktop BEFORE any F-8 code is written; everything else is downstream of that.
- Make the durable write free rather than carefully ordered: `assembleEnvelope` already returns `(AnnotationEnvelope, List<HandoffImage>)` as in-memory values and `held.bytes`/`held.bitmap` are locals in `resolveAndSend()`. Nothing F-8 persists is ever read back from `~/.rectify/handoff`, so FileSlotAnnotationHandoff's pruning is a non-event (FR-27/AC-38 are satisfied by construction, not by racing publish).
- Add exactly one persistence vertical: entity+child entity, DAO, public datasource, repository+interface, ModelResourceMapper, two domain models, two use cases. Delete `ExampleEntity`/`ExampleDao` — F-8 is the first real table.
- Keep all history state on the existing `MirrorUiState` (one new field) and the existing `MirrorViewModel`: no new Gradle module, no second `koinViewModel()` call site, no new design-system component — `HistoryRow`, `StatusPill`, `AppButtonSize.ListAction` already are the row.
- Put every pure decision (day bucketing, meta line, short id, union rect, status→tone) in one file in `presentation-feature-mirror/core/history/`, unit-tested in that module's `commonTest` (the feature convention plugin wires `kotlin.test`; `:compose-application` has none — nothing testable may land there).
- Close the loop with two edits in `RectifyWindow.kt` (the `else -> Unit` branch and `tabCounts`) and ~12 string keys.
- Verification is pure-function tests + light/dark previews + a manual two-send-then-restart demo. No Compose UI-test harness exists and F-8 is not the spec that introduces one.

### proposedArchitecture

```
A single persistence vertical hung off the existing mirror surface, with one new side effect inside `MirrorViewModel.resolveAndSend()`.

Write path: `resolveAndSend()` already holds `frozen`, `held.bitmap`, `held.bytes` and, after assembly, `(envelope, images)`. Inside the SAME `withContext(Dispatchers.Default)` assembly block it additionally encodes one thumbnail — `encodeCrop.encode(held.bitmap, unionRect(frozen.annotations))` — so by the time `handoff.publish(...)` runs, every byte History needs is already in memory. On `published.onSuccess`, and BEFORE `postSideEffect(SendComplete)`, the VM calls `recordSentBatch(SentBatchModel(...)): Result<Unit>` and reacts to it with `.onFailure { log.w(...) }` and nothing else — the DB write lives in its own failure domain and can never reduce `SendPhase.Failed`.

Down the stack: `RecordSentBatchUseCaseImpl` wraps `resultLauncher(errorMapper = Failure.Technical::Database)` around `HistoryRepository.record(...)`. `HistoryRepositoryImpl` (data-repository-impl, internal) does two things in order: (1) hands the bulky bytes to an injected `BatchFileStore` (fun-interface seam, exactly the `CropEncoder` precedent — desktop AWT/java.io actual, null-returning stand-in on Android/iOS per AS-29) which writes `~/.rectify/history/<sanitised batchId>/{frame.png, crop-N.png, hierarchy.xml}` on `Dispatchers.IO` and returns the directory path or `null`, never throwing; (2) inserts the row + child annotation rows through `HistoryDataSource`, with the file paths from (1) or `null`. A failed file write therefore costs paths, never the row (SC-18).

Read path: `HistoryDao` exposes `Flow<List<HistoryEntryEntity>>` ordered `sentAtMs DESC`; `HistoryRepositoryImpl` maps it with `HistoryEntryMapper.toModel` to `Flow<List<HistoryEntryModel>>`; `ObserveHistoryUseCase` returns the bare `Flow` (reactive ⇒ no `Result`, per domain-layer.md); `MirrorViewModel`'s container init block collects it into `state.history`. That one field feeds both the list and the badge, so no second observation path exists.

Render path: `MirrorHistoryTab(state, onIntent, modifier)` — `Column { LazyColumn(Modifier.weight(1f)) { ... }; HistoryFooter() }`, because the design's footer is `flex:none` and the call site hands the tab `Modifier.weight(1f)`. Grouping is computed at render time by a pure `groupHistory(entries, nowMs, zone)`. The thumbnail is a tiny pre-encoded PNG carried as a BLOB column and decoded with the already-used `ByteArray.decodeToImageBitmap()`, which is what lets a commonMain composable render it with ZERO filesystem access — the list cannot be broken by a user deleting files, because rendering never touches a path. Only F-9's diff will open `framePath`, and it checks existence at use time.
```

### components

| name | responsibility |
| --- | --- |
| HistoryEntryEntity (+ HistoryAnnotationEntity) | data-database-impl `source/entity/` — the Room row(s). Columns: `id` TEXT PK (= envelope.sessionId), `sentAtMs`, `status` TEXT, `title`, `annotationCount`, `deviceLabel`, `prompt`, `dirPath?`, `framePath?`, `hierarchyPath?`, `thumbPng` BLOB?. Child table `history_annotation(batchId FK CASCADE, index, left, top, right, bottom, comment, cropPath?)` with an index on `batchId`. Public (not `core/entity/`) because it is the datasource's wire type and `ModelResourceMapper` lives in data-repository-impl — see openDecisions, this is a documented deviation from database.md's placement line. |
| HistoryEntryDao | data-database-impl `core/dao/` — `@Transaction @Insert` of row+children, `@Query("...ORDER BY sentAtMs DESC") fun observeAll(): Flow<List<HistoryEntryEntity>>`, `@Query("UPDATE ... SET status=:status WHERE id=:id")`. Internal. |
| AppDatabase edit | data-database-impl `core/database/` — entity set becomes the two history entities, `historyEntryDao()` accessor, `ExampleEntity`/`ExampleDao` deleted. Keep the body-less `override fun initialize()` on `AppDatabaseConstructor` or the build fails. |
| DatabaseBuilder.desktop.kt edit | Relocate the file to `~/.rectify/rectify.db` (same root as the handoff slot, `mkdirs()` first) and add `.setQueryCoroutineContext(Dispatchers.IO)`. Side benefit: the new path provably has no pre-existing database, which is the whole basis for staying at schema version 1 (see survey). |
| HistoryDataSource / HistoryDataSourceImpl | data-database-impl `source/datasource/` (interface, public, mixing `ObservableDataSource`/`AppendableDataSource`/`UpsertableDataSource` from data-database-api — the rule says mix the generic bases where consumed) and the internal DAO-backed impl. Registered in `dataDatabaseImplModule`. |
| BatchFileStore + DesktopBatchFileStore | data-repository-impl `source/store/BatchFileStore.kt` (`public fun interface { suspend fun write(batchId: String, files: List<Pair<String, ByteArray>>): String? }`) plus `desktopMain/core/store/FileSystemBatchFileStore.kt` using `java.io` on `Dispatchers.IO`. NEVER throws, returns `null` on any failure — the exact contract `CropEncoder` already uses. Android/iOS bind a `BatchFileStore { _, _ -> null }` stand-in (AS-29). Bound per platform via a `BatchStoreProvider.{kt,desktop.kt,android.kt,ios.kt}` expect/actual, copying `HandoffSourcesProvider`'s shape. |
| HistoryRepository / HistoryRepositoryImpl | `domain-repository-api/source/repository/HistoryRepository.kt`: `observe(): Flow<List<HistoryEntryModel>>`, `suspend fun record(batch: SentBatchModel)`, `suspend fun updateStatus(batchId: String, status: BatchStatusModel)` (FR-33 — exposed, never called by F-8). Impl internal in data-repository-impl `source/`, orchestrating BatchFileStore then HistoryDataSource. |
| HistoryEntryMapper | data-repository-impl `core/mapper/` — one `internal object` with `toModel`/`toResource` per `ModelResourceMapper`. `toModel` must map an unknown stored `status` string to `BatchStatusModel.Sent` rather than throwing (mappers.md: a mapper never throws). |
| HistoryEntryModel / BatchStatusModel / SentBatchModel | domain-core `source/model/history/`. `HistoryEntryModel(id, sentAtMs, status, title, annotationCount, thumbPng: ByteArray?, framePath: String?)` is the read model; `SentBatchModel` is the write model carrying the raw bytes down (`frameBytes`, `crops: List<ByteArray?>`, `thumbPng`, `hierarchyXml`, `prompt`, annotations). domain-core keeps zero dependencies — `ByteArray` is stdlib. |
| RecordSentBatchUseCase / ObserveHistoryUseCase | domain-usecase-api `source/usecase/history/` + impls. One-shot ⇒ `Result<Unit>` wrapped in `resultLauncher(errorMapper = Failure.Technical::Database)`; reactive ⇒ bare `Flow<List<HistoryEntryModel>>`. Exactly two use cases — `updateStatus` gets none, because F-8 has no caller for it and a use case with no caller is scaffolding. |
| HistoryFormatting.kt (pure) | presentation-feature-mirror `core/history/` — `groupHistory(entries, nowMs, zone): List<HistoryGroup>` (Today/Yesterday/Older by local calendar day, newest-first within group), `metaLine(count, sentAtMs, shortId)`, `shortBatchId(id)`, `unionRect(annotations): DeviceRect?`, `statusTone(status): StatusPillTone`. Sibling of the existing `core/stats/StatsFormatter.kt`. Unit-tested in that module's `commonTest` (plain camelCase test names — a comma or colon in a backtick name breaks `compileTestKotlinIosArm64`). |
| MirrorContract edits | `MirrorUiState.history: List<HistoryEntryModel> = emptyList()` and `MirrorIntent.VerifyBatch(val batchId: String)`. The intent handler is a deliberate one-line `log.i` stub that F-9 fills (FR-32's "caller-supplied callback") — routing through `onIntent` keeps the tab identical in shape to Notes/Payload instead of introducing a bespoke callback parameter. |
| MirrorViewModel edits | Two new REQUIRED constructor params (`recordSentBatch`, `observeHistory`) with no defaults — `handoff`'s KDoc states the reasoning: a default turns a missing Koin binding into a silent runtime no-op, which for History means silently losing every batch. Container init collects `observeHistory()`; `resolveAndSend()` gains the thumbnail encode and the post-publish persist. `MirrorFakes.kt` must gain two trivial fakes or every existing VM test stops compiling. |
| MirrorHistoryTab.kt | presentation-feature-mirror `source/mirror/` — `Column { LazyColumn(weight(1f)) { group headers + HistoryRow items }; footer }`, empty-state early return copying `MirrorNotesTab`, two private light/dark previews. Group header = `Theme.typography.overline` + `Theme.color.ink3`; footer = `Theme.typography.monoMeta` + `ink3` over `Theme.stroke.thin`/`Theme.color.line`. No new atom or molecule is created. |
| RectifyWindow.kt edits | Replace `else -> Unit` with `ShellTab.History -> MirrorHistoryTab(state = mirror, onIntent = onIntent, modifier = Modifier.weight(1f))`, and build `tabCounts` with `buildMap { ...; val n = mirror.history.count { it.status == InProgress }; if (n > 0) put(ShellTab.History, n) }` — omitting the key at zero makes AC-34 true regardless of how `SidebarTabs` renders a 0. |
| strings.xml additions | presentation-core-localisation — `mirror_history_empty`, `mirror_history_group_{today,yesterday,older}`, `mirror_history_meta` , note count (plural), `mirror_history_verify`, `mirror_history_status_{sent,in_progress,fixed,verified,followup}`, `mirror_history_footer` (`claude · session %1$s`). No AAPT-style apostrophe escapes (regression fixed in dae0893). |

### alternatives

| title | pros | cons | tradeoffs | chosen |
| --- | --- | --- | --- | --- |
| Persist AFTER a successful publish, in its own failure domain (RECOMMENDED) | ["The 'capture before prune' constraint disappears: `(envelope, images)` and `held.bytes` are already in-memory locals, so no file is ever read back from the handoff slot and the pruning in `FileSlotAnnotationHandoff.publish` is irrelevant to F-8", "FR-26 says 'on every SUCCESSFUL publish' — this is literally that", "`Result<Unit>` + `.onFailure { log.w }` cannot touch `SendPhase`; a dead disk cannot make a successful send look failed", "Sequential (not detached), so SC-16's new row is in state before the toast fires and the existing Orbit test harness stays deterministic"] | ["Adds a few ms of disk + SQLite work to the send's perceived latency", "A DB failure is silent to the user (log only) — one batch quietly missing from History"] | Trades a silent, logged data loss on a rare failure for the guarantee that persistence can never degrade the send UX. Given F-8 has no error surface of its own and the send is the user-visible operation, that is the correct direction. | true |
| Persist BEFORE publish | ["A row exists even if the agent handoff fails", "Removes any doubt about byte availability"] | ["History would show status Sent for a batch that never reached the agent — an actively lying UI", "Contradicts FR-26 and AC-38's wording", "Needs compensating deletion on publish failure, i.e. more code to be less correct"] | Only buys something if you want a record of failed sends, which no requirement asks for. | false |
| Persist inside the same `runCatching` as publish | ["Smallest diff of all — one extra line in the existing block"] | ["A Room/IO failure reduces `SendPhase.Failed`, telling the user the send failed when the agent already has the batch", "Directly violates the stated requirement that a DB write must not fail the send"] | One saved line for a class of false-negative bug that is very expensive to diagnose from a bug report. | false |
| Fire-and-forget `viewModelScope.launch` after publish | ["Zero added latency on the send path"] | ["SC-16's row may land after the toast", "Escapes the intent's structured scope and the existing test harness's `advanceUntilIdle` discipline", "Error handling has nowhere natural to live"] | Latency we have no evidence is a problem, bought with nondeterminism. Keep as the documented escape hatch if profiling ever shows the write is felt. | false |
| Persist in the data layer by observing `handoff.latest()` | ["Presentation stays untouched", "Would also catch sends published by some future non-UI producer"] | ["`latest()` reads the slot that is being pruned — exactly the staleness F-8 exists to avoid", "Needs polling plus duplicate detection", "AS-1 explicitly states one call-site hook is sufficient"] | Architectural purity for real machinery and a real race. No. | false |
| Normalised child table for annotations (RECOMMENDED) vs. one JSON blob column | ["No serialization dependency added to presentation-feature-mirror", "Does not embed `AnnotationEnvelope` — a contract data-handoff-api documents as wire-breaking on field order — into storage", "F-9's per-annotation `resolved` flag is a column add, not a blob rewrite", "Room does the whole thing in one `@Transaction`, ~30 lines total"] | ["One more entity, one more table, one more mapper branch than stuffing `Json.encodeToString(envelope)` into a TEXT column"] | The blob is the lazier diff, but it inherits two warts: stale `cropPath`s pointing at the pruned handoff dir, and storage coupled to a wire contract. 30 lines is the right price to avoid both. | true |
| Thumbnail as a small BLOB column (RECOMMENDED) vs. a thumb.png file + path | ["A commonMain composable cannot read a file — a path-based thumbnail would need a whole new expect/actual image-loading seam just for the list", "Rendering touches the filesystem zero times, so a user deleting the batch directory cannot break a row (directly answers the missing-file requirement)", "The thumbnail is a derived crop of a few KB, not one of the 'frame.png / crop-N.png copies' the closed decision governs — frame and crops still live on disk as files with paths in the DB, exactly as decided"] | ["Each list query drags ~3–8 KB per row (≈8 MB at 1000 batches)", "A second storage mechanism alongside the files"] | Spends a few MB of query memory at implausible scale to delete an entire platform seam and make the missing-file edge case structurally impossible. | true |

### exceptions

- `BatchFileStore.write` NEVER throws — returns `null` on any IOException, disk-full or permission failure (CropEncoder's contract, copied deliberately).
- `HistoryEntryMapper.toModel` never throws: an unrecognised stored status string falls back to `BatchStatusModel.Sent` (mappers.md).
- `RecordSentBatchUseCaseImpl` wraps `resultLauncher(errorMapper = Failure.Technical::Database)`; a Room `SQLiteException` surfaces as `Failure.Technical.Database`, never as a raw throwable past the use-case boundary.
- `resolveAndSend()` consumes that `Result` with `.onFailure { log.w }` only. No `SendPhase` change, no `ShowError` side effect, no new string.
- `ObserveHistoryUseCase` returns a bare Flow; a DB read failure there terminates the flow. Guard with `.catch { log.w(it); emit(emptyList()) }` in the repository impl so the History tab degrades to empty rather than crashing the mirror surface.
- `UnsupportedAnnotationHandoff.publish` already throws on Android/iOS, so `resolveAndSend` never reaches the persist call there — the null-returning `BatchFileStore` stand-in is belt-and-braces, not the primary guard.
- Batch directory name is `batchId.replace(Regex("[^A-Za-z0-9._-]"), "_")` — `mintSessionId` embeds a device id straight into the string and that id is not guaranteed filesystem-safe.

### edgeCases

- FR-30 asks for the 'last 4 hex chars of batch id', but `mintSessionId` returns `<deviceId>-f<frameId>-<sentAtMs>` — decimal, not hex, and its last 4 chars are just milliseconds digits. Derive the display id instead: `(id.hashCode() and 0xFFFF).toString(16).padStart(4, '0')`. One pure line, deterministic, matches the design's `#a41f` shape. Flag this to the plan stage as a literal-wording deviation from FR-30.
- Batch with zero annotations: `canSend` already blocks it, but `unionRect` must still return `null` and the thumbnail must degrade to the bare plate rather than crash.
- `encodeCrop` returns `null` on every non-desktop target and on any encode failure ⇒ `thumbPng == null` ⇒ bare `Theme.color.chrome` plate. That IS SC-18's placeholder; no separate placeholder asset is needed.
- Day rollover while the app sits open: grouping is computed at render time from `nowMs()`, so a row stays under 'Today' until the next recomposition. Accepted — the alternative is a midnight timer.
- Two sends in the same millisecond on the same device and frame would collide on the `sessionId` primary key. Practically unreachable (a send takes seconds); use `@Insert(onConflict = REPLACE)` so it degrades to an overwrite rather than a crash.
- User deletes `~/.rectify/history/<id>/` by hand: the row still renders in full, because rendering reads only DB columns. `framePath` simply points at nothing — F-9 must existence-check before diffing.
- AC-34's badge is permanently invisible in F-8, because nothing ever writes `In progress`. The mapping must still be implemented (it is one line) and should be demoed by temporarily seeding a row, not by shipping a fake status.
- Very large hierarchy XML (observed dumps run to hundreds of KB): written as a file, never a column, so the list query stays small.
- Compose Resources plural support for '1 note' vs 'N notes' is unverified in this repo — if `pluralStringResource` is unavailable, fall back to two keys. Do not hand-concatenate an 's'.
- `MirrorNotesTab` uses `verticalScroll`; History must use `LazyColumn` (NFR-13) and the footer must sit OUTSIDE it, since the design's footer is `flex:none`.
- Existing `MirrorViewModel` tests construct the VM directly — adding two non-defaulted params breaks compilation in `MirrorFakes.kt` until fakes are added. That is a known, intentional cost of refusing defaults.

### implementationSteps

- 0. SPIKE (blocking): relocate the desktop DB to `~/.rectify/rectify.db`, add `.setQueryCoroutineContext(Dispatchers.IO)`, inject `AppDatabase` somewhere temporary and prove one insert + one Flow emission on desktop. Confirms KSP output, BundledSQLiteDriver linkage, the generated `AppDatabaseConstructor` actual and `schemas/` export — none of which this repo has ever executed.
- 1. data-database-impl: add `HistoryEntryEntity` + `HistoryAnnotationEntity` + `HistoryEntryDao`; update `AppDatabase` (entities, `historyEntryDao()`); delete `ExampleEntity`/`ExampleDao`; keep `version = 1` (see survey) and commit the exported `schemas/*.json`.
- 2. data-database-impl: `source/datasource/HistoryDataSource.kt` (public, mixes data-database-api bases) + internal impl; register in `dataDatabaseImplModule`.
- 3. domain-core: `HistoryEntryModel`, `BatchStatusModel`, `SentBatchModel` under `source/model/history/`.
- 4. domain-repository-api: `HistoryRepository` (observe / record / updateStatus).
- 5. data-repository-impl: add `implementation(projects.dataDatabaseImpl)` to build.gradle.kts (new edge — currently only dataDatabaseApi); add `HistoryEntryMapper`, `HistoryRepositoryImpl`, `BatchFileStore` + desktop impl + `BatchStoreProvider` expect/actual (null stand-in on android/ios); register in `dataRepositoryImplModule`.
- 6. domain-usecase-api + domain-usecase-impl: `RecordSentBatchUseCase` (`Result<Unit>`, `resultLauncher`) and `ObserveHistoryUseCase` (bare Flow); register in the usecase Koin module.
- 7. presentation-feature-mirror: add `libs.kotlinx.datetime` to build.gradle.kts; write `core/history/HistoryFormatting.kt`; write `commonTest/.../HistoryFormattingTest.kt` covering Today/Yesterday/Older boundaries, newest-first ordering, `unionRect` over 3 scattered rects (SC-17), `shortBatchId`, and the status→tone map.
- 8. presentation-feature-mirror: `MirrorUiState.history`, `MirrorIntent.VerifyBatch`, VM constructor params, container-init collection, thumbnail encode inside the existing assembly `withContext`, post-publish persist in `onSuccess`; update `MirrorFakes.kt`; update `PresentationFeatureMirrorModule`'s explicit `viewModel { }` block.
- 9. presentation-core-localisation: add the ~12 `mirror_history_*` keys.
- 10. presentation-feature-mirror: `MirrorHistoryTab.kt` + two private previews (light/dark, `AppTheme`-wrapped).
- 11. compose-application: `RectifyWindow.kt` — the `ShellTab.History` branch and the `tabCounts` entry.
- 12. Verify: `./gradlew :presentation-feature-mirror:desktopTest`, then a cross-target build (`:presentation-feature-mirror:build :data-repository-impl:build :data-database-impl:build`) because commonTest compiles for iOS too; then the manual demo — send two batches, quit, relaunch, confirm both rows and their thumbnails (AC-33/SC-15).

### gaps

- Room has never run in this project. KSP generation, `BundledSQLiteDriver` packaging into the desktop distributable, and `schemas/` export are all unverified. This is the single biggest schedule risk in F-8 and the reason for step 0.
- `CropWindow.kt#cropWindow`'s padding constant was not read during this pass — FR-29 specifies pad=18. The plan stage must check it and either reuse `cropWindow` directly or expand the union rect by 18 before encoding.
- `SidebarTabs`' rendering of a zero count is unverified; mitigated by omitting the map key entirely at zero rather than relying on its behaviour.
- Compose Resources `pluralStringResource` availability in this version is unverified (note-count copy).
- No Compose UI-test harness exists, so AC-22/AC-34/AC-36 cannot be automated. Their evidence is pure-function tests, previews and a manual demo. Introducing `runComposeUiTest` is a real infrastructure decision and is deliberately NOT part of F-8.
- database.md says `@Entity` lives under `core/entity/` while package-layout.md says `core/` is internal-only. A Room entity exposed as the datasource's wire type cannot satisfy both. The rule doc needs one line amended (recommend: entities exposed through a public datasource live under `source/entity/`). A doc delta, not code.
- `Migrations.kt` is empty AND no builder calls `addMigrations(...)`. F-8 stays at version 1 and therefore needs neither — but the FIRST real migration (likely F-9's per-annotation `resolved` column) must add the builder wiring in all three actuals. Leave a comment saying so; do not build the scaffolding now.
- Unbounded disk growth from retained frame/crop/hierarchy files is a closed, accepted ceiling. Mark it with a `ponytail:`-style comment in `BatchFileStore` naming the upgrade path (age or count cap) rather than re-litigating it.
- A failed DB insert is invisible to the user (log only). No error UI exists in F-8's scope; if that proves unacceptable, the cheap upgrade is reusing the existing `SendComplete` snackbar path with a different summary.

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Room's KMP toolchain (KSP + generated constructor actuals + bundled SQLite driver) has literally never been exercised in this repo; the first real table may surface build or packaging problems unrelated to F-8's logic. | high | high | Step 0 spike before any F-8 code. Timebox it; if the desktop driver fights the packaging step, that is a scope conversation, not something to debug inside the feature branch. |
| Staying at schema version 1 while changing the entity set would crash any machine that somehow already has a v1 `rectify.db`. | low | high | Relocating the file to `~/.rectify/rectify.db` in the same change makes a pre-existing file impossible by construction. The two decisions must ship together or not at all. |
| Adding two non-defaulted constructor params breaks every existing MirrorViewModel test until MirrorFakes is updated. | high | low | Update `MirrorFakes.kt` in the same commit: `RecordSentBatchUseCase { Result.success(Unit) }` and `ObserveHistoryUseCase { emptyFlow() }`. |
| Disk/SQLite work added to `resolveAndSend` lengthens the send's perceived latency. | low | low | Writes are a few hundred KB on Dispatchers.IO inside an already-multi-second operation. If ever measured as felt, move the persist into a detached `viewModelScope.launch` — the single-line escape hatch. |
| Growing `MirrorUiState` with a list that changes on every DB emission adds reducer passes to an already pixel-sensitive state machine. | low | low | Emissions happen once per send, not per frame. This respects the module's rule that pixels stay out of state (the thumbnail BLOBs are small and already decoded lazily by the row). |
| Storing thumbnail bytes in state/DB could be read as contradicting the closed 'paths, not BLOBs' decision. | med | low | Document it explicitly in the plan: frame.png and crop-N.png ARE files with paths exactly as decided; the BLOB is a derived few-KB thumbnail that exists so a commonMain composable needs no file-reading seam. |
| AC-34's badge can never be demonstrated because nothing writes `In progress` in F-8. | high | low | Prove it by unit-testing the count-to-badge mapping function and by a one-off manual DB seed during the demo. Do not ship a fake transition to make a screenshot look right. |

### readyMadeSolutions

| name | ref | fit | license |
| --- | --- | --- | --- |
| HistoryRow | presentation-core-ui/.../kit/molecule/row/HistoryRow.kt | Already exactly the F-8 row: 56.dp thumbnail plate, title/meta/status slots, `thumbnail` and `trailing` composable slots. Used as-is; the Verify button goes in `trailing`, the decoded thumbnail in `thumbnail`. Zero new components. | in-repo |
| StatusPill / StatusPillTone | presentation-core-ui/.../kit/atom/pill/StatusPill.kt | Done/Busy/Neutral/Alert covers Fixed/In progress/Sent+Verified/Follow-up sent with no new tone token (AS-4). | in-repo |
| AppButton + AppButtonSize.ListAction | presentation-core-ui/.../kit/atom/button/AppButton.kt | The 22px Verify action, already sized for an in-row action. | in-repo |
| CropEncoder + AwtCropEncoder | presentation-feature-mirror/.../source/mirror/CropEncoder.kt, desktopMain/.../core/crop/AwtCropEncoder.kt | Generates the thumbnail PNG from the union rect with zero new code and is the exact precedent for the never-throws / null-on-unsupported-platform seam F-8 copies for file writing (AS-29). | in-repo |
| assembleEnvelope's `List<HandoffImage>` | presentation-feature-mirror/.../core/send/EnvelopeAssembly.kt | Frame and crop bytes are ALREADY in memory before publish. This is what turns FR-27's 'copy the files before they are pruned' into a non-problem — there is no copy and no race, just a second write of bytes we already hold. | in-repo |
| HandoffSourcesProvider.{kt,desktop,android,ios} | data-handoff-api/.../di/ | The shape to copy for `BatchStoreProvider`: desktop binds the real implementation, android/ios bind an unsupported stand-in. | in-repo |
| ModelResourceMapper + ThemePreferenceMapper | data-repository-impl/core/mapper/ | The mapper shape and the never-throw/safe-default rule for the status column. | in-repo |
| ObserveMirrorStatsUseCase / SetThemeUseCase | domain-usecase-api/.../usecase/ | Reactive (bare Flow) and one-shot (`Result<T>` via `resultLauncher`) templates — copy signatures verbatim. | in-repo |
| StatsFormatter | presentation-feature-mirror/core/stats/StatsFormatter.kt | Local precedent for 'pure formatting function in the feature module's core/, unit-tested in commonTest' — HistoryFormatting is its sibling. | in-repo |
| kotlinx-datetime 0.8.0 | gradle/libs.versions.toml (already in catalog, used by data-repository-impl) | Local calendar-day bucketing for Today/Yesterday/Older. Already a project dependency — one build file line, not a new dependency. | Apache-2.0 |
| androidx.room + BundledSQLiteDriver | data-database-impl | The whole persistence layer, already wired with kspDesktop and schema export. F-8 is its first real consumer. | Apache-2.0 |
| ByteArray.decodeToImageBitmap() | already used by MirrorViewModel's private decodePng | Decodes the thumbnail BLOB in commonMain with no platform seam. | Apache-2.0 |

### techStack

| area | choice | reason |
| --- | --- | --- |
| Persistence | Room KMP (existing scaffold), schema stays at version 1, DB relocated to ~/.rectify/rectify.db | Mandated by the clarify constraints; relocation makes 'no existing install' provable, which is what removes the migration obligation. |
| Bulk image/XML storage | Plain java.io file writes in a desktopMain source set, under ~/.rectify/history/<batchId>/ | No okio or kotlinx-io is in the catalog and a new dependency for three file writes fails the reuse ladder. Same root as the handoff slot, so all app-owned data lives in one hidden directory. |
| Thumbnail generation | Existing CropEncoder (AWT getSubimage + ImageIO) over the union bounding box, stored as a BLOB column | Reuses shipped crop math unchanged (FR-29) and removes the need for any file-reading seam in commonMain UI code. |
| Date bucketing | kotlinx-datetime, pure function in presentation-feature-mirror/core/history/ | Already in the catalog; a pure function in a feature module is unit-testable, unlike anything placed in :compose-application. |
| List rendering | Compose LazyColumn, footer outside the scroll | NFR-13; the design's footer is flex:none. |
| State transport | One new field on the existing MirrorUiState, fed by a bare Flow use case collected in the container init block | No second ViewModel or Gradle module; MirrorHost's KDoc states there is exactly one call site. |
| DI | Existing per-module Koin modules; two new use-case bindings, one repository binding, one datasource binding, one per-platform BatchFileStore binding | koin-di.md — append within the layer group, never reorder initKoin(). |

### openDecisions

| decision | options | recommendation |
| --- | --- | --- |
| Where the public Room entity physically lives, given database.md says core/entity/ and package-layout.md says core/ is internal-only | ["source/entity/ + a one-line amendment to database.md", "core/entity/ with a public entity (breaks package-layout.md)", "Internal entity + a separate public resource DTO + an extra internal mapper hop"] | source/entity/ plus the doc amendment. The third option is ~60 lines of field-copying boilerplate to satisfy a placement sentence; the second silently breaks the rule that explicitApi() exists to police. |
| Whether `updateStatus` gets a use case in F-8 | ["Repository method only (FR-33 says 'repository API')", "Repository method + an UpdateBatchStatusUseCase nobody calls"] | Repository method only. FR-33 specifies a repository API; a use case with zero callers is scaffolding F-9 can add in one file when it actually has a caller. |
| How the Verify click is routed | ["MirrorIntent.VerifyBatch(batchId) with a logging stub handler", "A plain onVerify callback parameter on MirrorHistoryTab wired to {} in RectifyWindow"] | The intent. Notes and Payload both route through onIntent; a bespoke callback would be the only one of its kind, and F-9 fills the handler body without touching the tab or the window. |
| Whether the prompt is a column or a file | ["TEXT column (a few KB)", "prompt.txt in the batch directory"] | Column — it is small and may well be shown in a future detail view. Only the hierarchy XML (hundreds of KB) is large enough to justify a file. |

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| Where does the 'record the batch' write hook into MirrorViewModel.resolveAndSend(), and what are its failure semantics? | ["After a successful handoff.publish(), inside published.onSuccess and before postSideEffect(SendComplete), as a Result<Unit> consumed with .onFailure { log.w } only — never touching SendPhase. The thumbnail is encoded in the same withContext block as envelope assembly, so every byte is in memory before publish runs.", "Before handoff.publish(), so a row exists even when publishing fails", "Inside the same runCatching as publish, so one failure path covers both", "Detached in viewModelScope.launch after publish, fire-and-forget"] | After a successful handoff.publish(), inside published.onSuccess and before postSideEffect(SendComplete), as a Result<Unit> consumed with .onFailure { log.w } only — never touching SendPhase. The thumbnail is encoded in the same withContext block as envelope assembly, so every byte is in memory before publish runs. | ai-proposed | The premise that the write must race the pruning turns out to be false: assembleEnvelope already hands back (envelope, images) as in-memory values and held.bytes/held.bitmap are locals, so F-8 never reads anything back from ~/.rectify/handoff and the pruning is irrelevant. That frees the ordering to be chosen on correctness instead: FR-26 says 'on every successful publish', so persisting before publish would put a lying 'Sent' row in History for a batch the agent never received. Sharing publish's runCatching would let a full disk reduce SendPhase.Failed and tell the user a successful send failed — the one outcome explicitly ruled out. Detaching into viewModelScope removes latency nobody has measured as a problem while making SC-16's row arrival racy against the toast and escaping the test harness's advanceUntilIdle discipline. Sequential-but-non-fatal gets determinism and the correct failure isolation for one extra .onFailure line. |
| The desktop DB is at ~/rectify.db, the handoff slot at ~/.rectify/handoff. Where do the DB and the per-batch image files go, and does the schema need a v1→v2 migration? | ["Relocate the DB to ~/.rectify/rectify.db, put images at ~/.rectify/history/<batchId>/, and keep @Database(version = 1) with no Migration — AppDatabase is a lazy Koin single nothing has ever injected, so no rectify.db exists anywhere, and the new path makes that provable", "Leave the DB at ~/rectify.db, put images at ~/.rectify/history/<batchId>/, bump to version 2 and write MIGRATION_1_2 (DROP example, CREATE history tables)", "Relocate the DB and also bump to version 2 with a migration, for belt and braces", "Put the images next to the DB under ~/ and leave everything else alone"] | Relocate the DB to ~/.rectify/rectify.db, put images at ~/.rectify/history/<batchId>/, and keep @Database(version = 1) with no Migration — AppDatabase is a lazy Koin single nothing has ever injected, so no rectify.db exists anywhere, and the new path makes that provable | ai-proposed | database.md's bump-and-migrate rule exists to protect existing installs, and there are provably none: nothing in the repo injects AppDatabase or any DAO, and Koin singles are lazy, so the file has never been created on any machine. Relocating costs one line and turns that argument from 'probably no file' into 'certainly a different file', eliminating the only crash scenario. It also stops the app scattering data across two roots — everything app-owned then lives under ~/.rectify alongside the handoff slot, which is what a user looking to clear Rectify's data would expect. Doing it later is a real data migration; doing it now, before the first row ever exists, is free. Option 3 writes migration code that no database can ever execute, which is the definition of scaffolding for later. Migrations.kt stays empty for F-9, the first change that will actually need one. |
| How are a batch's per-annotation records (AC-33) stored? | ["A normalised child table history_annotation(batchId FK CASCADE, index, rect, comment, cropPath), written with the parent row in one @Transaction", "A single annotationsJson TEXT column holding Json.encodeToString(envelope)", "Only annotationCount and the first comment — skip per-annotation persistence entirely"] | A normalised child table history_annotation(batchId FK CASCADE, index, rect, comment, cropPath), written with the parent row in one @Transaction | ai-proposed | The JSON blob is the smaller diff and my first instinct, but it carries two warts that outlive F-8: it embeds AnnotationEnvelope — a contract data-handoff-api documents as wire-breaking — into persistent storage, and its per-annotation cropPath values point at handoff directories that F-7 prunes, so the stored record would contain knowingly dead paths. It also forces a kotlinx-serialization-json dependency edge into presentation-feature-mirror just to write a column. Room does the parent-plus-children insert natively in about thirty lines, and F-9's per-annotation 'resolved' flag — which SC-8 already implies — becomes a column add rather than a blob rewrite. Option 3 fails AC-33's 'per-annotation records are still present' outright. |
| How does the 44x44 row thumbnail get from storage to a commonMain composable? | ["Encode the union-bbox crop once at persist time and store those few KB as a BLOB column; the row decodes it with the already-used ByteArray.decodeToImageBitmap(). frame.png and crop-N.png still ship to disk as files with paths in the DB, exactly as decided.", "Write thumb.png to the batch directory, store the path, and add a new expect/actual image-file reader so the UI can load it", "Store only the full frame and apply the design's scale/offset crop at render time, as the HTML prototype does"] | Encode the union-bbox crop once at persist time and store those few KB as a BLOB column; the row decodes it with the already-used ByteArray.decodeToImageBitmap(). frame.png and crop-N.png still ship to disk as files with paths in the DB, exactly as decided. | ai-proposed | A commonMain composable cannot read a file, so a path-based thumbnail drags in an entire new expect/actual image-loading seam whose only consumer is a 44dp plate. The BLOB deletes that seam and has a second, larger payoff: the History list then performs zero filesystem reads, so a user deleting ~/.rectify/history cannot break a row — the missing-file resilience requirement becomes structural rather than defensive. Option 3 would load a full multi-hundred-KB frame per visible row, fighting NFR-13 for no benefit. This does not reopen the closed paths-not-BLOBs decision: that decision governs the frame and crop copies, which still live on disk as files; the thumbnail is a derived artefact the decision never covered. Cost is a few MB of query memory at a thousand batches, which is the right trade. |
| What is the seam that actually writes bytes to disk, given no okio or kotlinx-io is in the version catalog? | ["A public fun interface BatchFileStore injected into HistoryRepositoryImpl, with a java.io desktop implementation and a null-returning stand-in bound on Android/iOS — the CropEncoder/HandoffSourcesProvider pattern this repo already uses twice", "An internal expect/actual top-level function with three actuals and no DI involvement", "Add okio or kotlinx-io to the catalog and write one common implementation"] | A public fun interface BatchFileStore injected into HistoryRepositoryImpl, with a java.io desktop implementation and a null-returning stand-in bound on Android/iOS — the CropEncoder/HandoffSourcesProvider pattern this repo already uses twice | ai-proposed | Adding a filesystem library for three file writes fails the reuse ladder outright. Between the remaining two, a one-implementation interface normally smells, but here it is the repo's established cross-platform stand-in pattern — CropEncoder is literally a fun interface with a never-throws, null-on-unsupported-platform contract and a desktop-only binding, and HandoffSourcesProvider is the per-platform Koin shape. Matching them costs one extra file over the expect/actual function and buys the ability to test SC-18 (file write fails, row still persists) without touching a real disk, which is the only branch in the repository impl worth a test at all. |
