# Codebase recon — F-9 Verify & Re-diff Loop

Read-only survey at `sdd/verify-and-rediff-loop` (branched off `development` just after F-8
merged). Authoritative for "what already exists"; **do not re-derive**.

## The four real gaps — everything else is reuse

1. **Read one batch with its annotations.** `HistoryEntryModel` carries `annotationCount` only,
   **not the annotations**. `HistoryEntryWithAnnotations` (the Room `@Relation`) already has the
   data — the read mapper throws it away. Needs: a `@Transaction @Query(… WHERE id = :id)` on the
   DAO, a method on `HistoryDataSource`, a domain model carrying rect+comment+cropPath,
   `HistoryRepository.get(batchId)`, a use case. Note `ReadableDataSource<T>.getById(id: Int)` is
   **Int-keyed and useless here** (batch ids are Strings) — add a bespoke method, don't mix it in.
2. **Read bytes back off disk.** `BatchFileStore` is **write-only**
   (`suspend fun write(batchId, files): String?`), and `presentation-feature-mirror` has no
   file-read capability at all. Needs a read seam in the same `fun interface` + `desktopMain` shape.
3. **Pixel region diff — nothing exists.** Zero diff/histogram/SSIM/compare code in the repo.
4. **`densityDpi` is not persisted.** It comes from a separate `wm density` call and is dropped at
   record time. Frame pixel size is recoverable (root-node bounds, or decode `frame.png`); density
   is **not** recoverable for the historical side. Every `dp` label depends on it. Either take
   density from the fresh live dump, or add a column — and see the migration warning below.

## 1. F-8 history surfaces

| Symbol | Path |
|---|---|
| `HistoryEntryModel` | `domain-core/.../source/model/history/HistoryEntryModel.kt` |
| `SentBatchModel`, `SentAnnotationModel` | `domain-core/.../source/model/history/SentBatchModel.kt` |
| `BatchStatusModel` | `domain-core/.../source/model/history/BatchStatusModel.kt` |

- `HistoryEntryModel`: `id, sentAtMs, status, title, annotationCount, thumbPng: ByteArray?, framePath: String?`
- `BatchStatusModel`: `Sent, InProgress, Fixed, Verified, FollowUpSent` — all five already
  persisted and rendered; only `Sent` is ever written. **`Verified`/`FollowUpSent` exist for F-9,
  no schema change needed.**
- `SentBatchModel` (write path): `id, sentAtMs, deviceLabel, prompt, frameBytes, thumbPng,
  hierarchyXml, annotations: List<SentAnnotationModel>`;
  `SentAnnotationModel(left, top, right, bottom, comment, cropBytes)` in device pixels.

`HistoryRepository` (`domain-repository-api/.../source/repository/HistoryRepository.kt`):
`observe(): Flow<List<HistoryEntryModel>>` (errors degrade to `emptyList()`), `record(batch)`,
**`updateStatus(batchId, status)` — zero callers, documented as "the hook F-9 advances a batch with".**
**There is no `getById`/`observeById` anywhere.**

Entities (`data-database-impl/.../source/entity/`, public per the `database.md` exception):
- `history_entry`: `id` PK, `sentAtMs`, `status`, `title`, `annotationCount`, `deviceLabel`,
  `prompt`, `dirPath`, `framePath`, `hierarchyPath`, `thumbPng: ByteArray?`
- `history_annotation`: composite PK `(batchId, index)`, `left, top, right, bottom, comment,
  cropPath`; FK CASCADE, `Index("batchId")`
- DAO `core/dao/HistoryEntryDao.kt`: `insert`, `observeAll()` (`sentAtMs DESC`), `updateStatus`. No by-id query.

### On-disk layout
`FileSystemBatchFileStore` root `~/.rectify/history/`, dir name = sanitised batchId
(rejects `""`/`"."`/`".."`), dirs `rwx------`, files `rw-------` (POSIX only).

```
~/.rectify/history/<sanitised batchId>/
    frame.png       full adb capture, NOT a re-encode
    hierarchy.xml   verbatim uiautomator stdout (== HierarchySnapshotModel.rawDump)
    crop-<i>.png    one per annotation whose encode succeeded
```
Paths stored **absolute** in `dirPath`/`framePath`/`hierarchyPath`/`cropPath`; `null` iff bytes were
absent or the write failed. The KDoc is emphatic: a stored path is a **hint, never a guarantee**.
`cropPath` is not exposed in any domain model today. Nothing prunes the directory
(documented `ponytail:` ceiling).

> **F-8 review finding F-8:** `cropFile(index)` is **0-based in history** while the envelope's
> `cropFileName(index)` is **1-based**. The same image is `crop-1.png` in the handoff slot and
> `crop-0.png` under history. F-9's diff loop must know both.

## 2. `MirrorHistoryTab` — the gate and the stub

`presentation-feature-mirror/.../source/mirror/MirrorHistoryTab.kt`, `private fun HistoryEntryRow`,
`trailing` slot, **lines 178-186** — this single `if` is the whole gate:
```kotlin
if (entry.status == BatchStatusModel.Fixed) {
    AppButton(text = verifyLabel, onClick = { onIntent(MirrorIntent.VerifyBatch(entry.id)) }, size = AppButtonSize.ListAction)
}
```
`MirrorIntent.VerifyBatch(batchId: String)` — `MirrorContract.kt:500`, unconditional.
Stub handler `MirrorViewModel.kt:388`: a single `log.i`. Dispatched at `MirrorViewModel.kt:304`.
`HistoryRow.onClick = {}` is a deliberate no-op (A11Y-8) — the natural second entry point.

**Collateral when the gate is removed:** `MirrorHistoryTab.kt:240` preview KDoc, `strings.xml:182`
comment, `MirrorContract.kt:490-500` KDoc. Consider `entry.framePath != null` as the honest
replacement gate — a row whose disk write failed has nothing to diff against.

## 3. F-5 measure-mode — the key reuse

All `internal` under `presentation-feature-mirror/.../core/measure/`, pure, already unit-tested.
**Reuse in-module; do not copy out.**

- **`NodeSnap.kt` → `snapTarget(annotation: DeviceRect, candidates: List<MeasureNode>): MeasureNode?`**
  — largest-area candidate fully contained in the annotation, `SNAP_SLACK_PX = 4` per edge, `null`
  → `NO_MATCH_NODE_ID = "View (no match)"`. **This is the matched-node function for both frames.**
- **`MeasureProjection.kt` → `projectMeasureNodes(snapshot, frozenFrameSize): List<MeasureNode>`**
  — ⚠️ **discards the whole snapshot if the snapshot's frame size disagrees with the passed size.**
  Before- and after-frames may differ; feeding the wrong size silently empties the candidate list.
- `MeasureNode(id, rect, isTappable)`, `MeasureNeighbours`, `MeasureGaps`; `neighboursOf`, `gapsOf`
- `Measure.kt → measure(annotation, index, nodes, frameSize, densityDpi): MeasureReadout` — snap →
  neighbours → gaps → convert → format → finding → rail. Linear in nodes, ~100 ms for a 300-node
  dump (NFR-2/NFR-13). `MeasureReadout` is in `MirrorContract.kt:~810`.

**Units / formatting — reuse verbatim for the `Δ … → …` strings:**
- `PixelUnits.kt`: `displayValueOf(px, dpi)`, `toDisplayUnit(px, dpi)`, `unitOf(dpi)`
  (`dpi <= 0` ⇒ raw px labelled `px`)
- `MeasureFormat.kt`: `deltaLabel` → `Δ6dp` (U+0394), `plainValueLabel` → `6dp`, `sizeLabel`,
  `positionLabel`, `touchTargetFinding`, `noteLine`, `replaceNoteLine`; `SEPARATOR = " · "` (U+00B7)
- `Findings.kt`: `displayDelta(fromPx, toPx, dpi)` (converts **then** rounds — literally the `Δ`
  number), `findingOf(...)`, `TOUCH_TARGET_MIN_DP = 32`

The design's strings decompose exactly: `top Δ6dp → 0dp` = `"top " + deltaLabel(before) + " → " +
deltaLabel(after)`; `height 24dp → 32dp · want 48dp` = `plainValueLabel`s joined by `SEPARATOR`.
**There is no before→after formatter** — that is one small new function built on these. Do not
reimplement unit conversion.

## 4. Live capture

- `StartMirroringUseCase(deviceId): Flow<MirrorFrameModel>` — **stream only, no one-shot capture use case.**
- `DumpHierarchyUseCase(deviceId): Result<HierarchySnapshotModel>` — one-shot, safe during
  mirroring, **measured ~2.7 s on a Pixel 8**. Budget for it.
- `MirrorFrameModel(bytes, widthPx, heightPx, capturedAtMs)` — identity equality, never reduced state.

**The lazy correct "re-capture":** `MirrorViewModel.currentHeld: MutableStateFlow<HeldFrame?>`
(`HeldFrame(bitmap, bytes)`, `MirrorViewModel.kt:85`) already holds the latest decoded frame with
its original bytes. One atomic `.value` read — exactly how `stageAnnotation` freezes — zero adb cost.
Cancellation precedent: `dumpJob: Job?` + `flatMapLatest` over `sessionDeviceId` +
`conflate() + delay(STATS_TICK_MS)`.

## 5. Image primitives — nothing exists, but commonMain suffices

**Zero pixel-comparison code in the repo.** What you build on:
- **`ImageBitmap.readPixels(buffer: IntArray, startX, startY, width, height, bufferOffset, stride)`
  is a commonMain member** (proven by `FakeFrameBitmap` overriding it); `toPixelMap()` is also
  commonMain. **A per-region pixel diff can be written entirely in `commonMain` — no expect/actual,
  no new dependency.** `startX/startY/width/height` set to the annotation rect *is* a region read.
- `ThumbnailBitmap.kt → cropAndScale(frame, rect, maxEdgePx = 128): ImageBitmap?` — pure commonMain;
  useful to normalise two differently-sized regions before diffing.
- `CropEncoder` (`fun interface`, `source/mirror/CropEncoder.kt`) — the established seam pattern.
- Decoding: `decodeToImageBitmap()` on a `ByteArray` (commonMain, already used for frames and
  history thumbnails). **Reading `frame.png` off disk needs a file-read seam — none exists.**

> ⚠️ **Skiko trap (the most important testability constraint).** `presentation-feature-mirror`'s
> `desktopTest` has **no Skiko native library** (only `:compose-application` pulls
> `compose.desktop.currentOs`), so `ImageBitmap(w, h)` throws `LibraryLoadException` there.
> **Keep the diff arithmetic over `IntArray`s, not `ImageBitmap`s**, with the `readPixels` call at
> the edge — then it is testable without Skiko, as `FakeFrameBitmap` already demonstrates.

## 6. F-7 re-send — the minimal follow-up path

Types in `MirrorContract.kt`: `DeviceRect(left, top, right, bottom)` (:609),
`StagedAnnotation(index /*1-based*/, rect, comment)` (:634),
`FrozenFrame(deviceSize, annotations, selectedIndex, frameId, deviceLabel, promptOverride)` (:672).
Intents: `StageAnnotation`, `CommentAnnotation`, `DeleteAnnotation`, `SelectAnnotation`,
`ClearStaged`, `EditPrompt`, `ResolveAndSend`, `ResumeLive`.

`stageAnnotation` (`MirrorViewModel.kt:505`) creates the frozen frame: reads `currentHeld.value`
once atomically, sets `currentFrozenFrame`/`currentFrozenHeld`, stamps `frameId = ++freezeCounter`,
fires `dumpHierarchyFor` on first freeze only.

`resolveAndSend` (`MirrorViewModel.kt:886`): guard → `Preparing` → `assembleEnvelope(...)` in
`withContext(Dispatchers.Default)` → `Publishing` → `handoff.publish` → on success release, then
`recordSentBatch(sentBatch(envelope, images, held.bytes, thumbPng))`, then `SendComplete`.
Session id `mintSessionId(deviceId, frameId, sentAtMs)` = `<deviceId>-f<frameId>-<epochMillis>`.

> **Minimal one-click follow-up:** build a `FrozenFrame` with exactly one `StagedAnnotation`
> (renumbered `index = 1`) over the freshly captured `HeldFrame`, set the three `current*` flows,
> reduce `state.copy(frozen = …)`, then **reuse `resolveAndSend()` unchanged** — no new send path.
> Then `historyRepository.updateStatus(originalBatchId, FollowUpSent)`.

## 7. Stage switching — the seam already exists

**There is already a stage enum**: `MirrorStage { Picker, Mirror, NoSignal, IosDeferred, Frozen }`
(`MirrorContract.kt:90`), derived by `MirrorUiState.stage` (`MirrorContract.kt:331`) and dispatched
in `MirrorStageContent`'s `when` (`MirrorContent.kt:124`, behind an error-card short-circuit).

**Add a `MirrorStage.Verify` case + a branch in that `when` + a condition in the `stage`
derivation. `RectifyWindow.kt` needs no change** — it already delegates the whole decision
(`stage = { MirrorStageContent(...) }`, ~line 440) and `RectifyWindowShell.stage` is an opinion-free
slot. Mind the ordering: `frozen != null -> Frozen` precedes `Streaming`.

`MirrorHost` hands `content(state, frozenFrame ?: frame, frozenHierarchy, ::handleIntent)` — a view
needing **two** bitmaps does not fit. Add a fourth plain `StateFlow` (the established pattern).
**There is exactly one `MirrorHost` call site; keep it that way.**
The `← Back to live` chip maps onto `ResumeLive` + a tab selection (precedent:
`RectifyWindow.kt:214`'s `LaunchedEffect(hasStaged) { shellState.selectTab(...) }`).

## 8. `VerificationRow` already exists and is a near-exact fit

`presentation-core-ui/.../kit/molecule/row/VerificationRow.kt`:
```kotlin
public enum class VerificationOutcome { Fixed, StillOff }
@Composable public fun VerificationRow(
    title: String, delta: String, outcome: VerificationOutcome,
    modifier: Modifier = Modifier, outcomeLabel: String = outcome.defaultLabel(),
)
```
Renders `GlyphBadge("✓"/"!")` + title (`Theme.typography.body`/`ink`) + delta
(`monoData`/`ink2`) + the outcome word in the accent colour — `Theme.color.sentFixed` (green) /
`Theme.color.warnStillOff` (amber), both identical in light and dark (NFR-9 is already satisfied
by the tokens). **Its own `@Preview` uses the design's exact strings.** This molecule is done;
F-9 only has to feed it. `outcomeLabel` defaults to a raw `String`, so the localised copy must be
resolved at the call site per `localisation.md`.

## 9. The FR-21 gate conflict — confirmed

PRD FR-21: *"**On any sent batch (not gated on a 'Fixed' status, since nothing in v1 is confirmed to
set one)**, provide a 'Verify' action…"*; AC-23 repeats "Given **any** sent batch".
**US-9 still says "on Fixed batches" — the PRD is internally inconsistent; FR-21/AC-23 are
normative.** What shipped is the Fixed-only `if` above, and since F-8 only ever writes `Sent`,
**the button is unreachable today** (which is why F-8's verdict waived `demo:AC-36`).

## Binding rules that bite here

- **Migration:** `database.md` — a schema change needs a `@Database(version)` bump **and** a real
  `Migration`; destructive migration is disabled. **F-8's "stayed at version 1" was a one-off,
  justified only because the DB file relocated to a never-written path — that exception is
  explicitly NOT reusable.** Adding a `densityDpi` column means a real migration.
- **Pixels never enter reduced state** (`presentation-feature-mirror/CLAUDE.md` Deviation 2):
  identity-compared types live on plain `StateFlow`s beside `frame`/`frozenFrame`. The audited
  exception is `MirrorUiState.history` and it is not casually extendable.
- All pixel work under explicit `withContext(Dispatchers.Default)` / `Dispatchers.IO`, never on the
  intent context. **Null-on-failure is universal** — a failed image never fails the operation.
- **Prefer a `fun interface` seam over `expect`/`actual`** (`CropEncoder`/`BatchFileStore` precedent;
  only `DatabaseBuilder`/`SettingsProvider` are real expect/actual). Null-returning Android/iOS
  stand-ins (AS-29).
- `domain-layer.md` / `error-handling.md`: one-shot ⇒ `Result<T>`, reactive ⇒ bare `Flow<T>`, never
  `Flow<Result<T>>`; `resultLauncher(errorMapper = Failure.Technical::<Kind>)`, never raw `runCatching`.
- `orbit-mvi.md`: `handleIntent` → private handlers, each `= intent { reduce{}; postSideEffect() }`;
  the `container(...) { }` trailing lambda is **init-time only**.
- `koin-di.md`: one module per Gradle module; `initKoin()` order is load-order-significant — append
  within the layer group, never reorder.
- `package-layout.md` + `explicitApi()`: `core/` internal-only, `source/` public, explicit
  visibility *and* return type.
- `testing.md`: no comma or colon in backtick test names (apostrophes fine); desktop runs;
  `:<module>:build` also runs the iosSimulatorArm64 suite; **no Compose UI-test harness exists**;
  `:compose-application` has no test wiring.
- `platform-coverage.md`: Android + iosArm64 + iosSimulatorArm64 + `jvm("desktop")`. No macOS/Linux
  native targets.
- **Performance budgets live in KDoc, not rule files:** `measure()` linear, ~100 ms for 300 nodes;
  `dumpHierarchy` ~2.7 s; frames up to 10 fps; stats reduce ≤1/s. **MET-3 (<5% false positives)
  argues for a tunable threshold constant with a named ceiling, not a magic number.**
