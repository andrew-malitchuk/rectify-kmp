# Codebase recon — F-8 History & Batch Tracking

Read-only survey of the repo at branch `sdd/history-batch-tracking`. Authoritative for
"what already exists"; downstream agents should NOT re-derive this.

## 1. Tab strip — History tab already exists, its body does not

| Thing | Path | Symbol |
|---|---|---|
| Tab enum | `presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellTab.kt` | `ShellTab { Notes("Annotations"), Payload("Payload"), History("History") }`, `shortcutToTab` (Cmd+3 → History) |
| Panel chrome | `.../organism/shell/SidePanelFrame.kt` | `SidePanelFrame(activeTab, onTabSelect, modifier, tabCounts: Map<ShellTab,Int>, content: @Composable ColumnScope.(ShellTab)->Unit)` |
| Tab state | `.../organism/shell/ShellState.kt` | `activeTab`, `selectTab()`, `onSessionStarted()` (resets to Notes) |
| **Content switch** | `compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt` ~L238-258 | `sidePanel = { tab -> when (tab) { Notes -> MirrorNotesTab(..); Payload -> MirrorPayloadTab(..); else -> Unit } }` ← **`else -> Unit` is the hole** |
| **Badges** | same file ~L428 | `tabCounts = mapOf(ShellTab.Notes to mirror.stagedCount, ShellTab.Payload to mirror.stagedCount)` — History absent ⇒ no badge |

Tab bodies live in `presentation-feature-mirror/src/commonMain/.../source/mirror/`:
`MirrorNotesTab.kt` (118 lines, closest template — `fun MirrorXTab(state, onIntent, modifier)`,
empty-state early return, `verticalScroll`, two previews) and `MirrorPayloadTab.kt`.
Each tab is given `Modifier.weight(1f)` by the call site — load-bearing.

### UI already built — reuse, do not rebuild
- `presentation-core-ui/.../kit/molecule/row/HistoryRow.kt` —
  `HistoryRow(title, meta, statusLabel, statusTone: StatusPillTone, onClick, modifier,
  thumbnail: @Composable (()->Unit)?, trailing: @Composable RowScope.()->Unit)`.
  56.dp thumbnail plate (`Theme.color.chrome`, `Theme.cornerToken.nested`), title
  `Theme.typography.body`, meta `Theme.typography.monoMeta` + `ink3`, `StatusPill`.
  **This is exactly the F-8 row.**
- `.../kit/atom/pill/StatusPill.kt` — `StatusPillTone { Done, Busy, Neutral, Alert }`.
- `.../kit/atom/button/AppButton` + `AppButtonSize.ListAction` — the "Verify" trailing action.
- `.../kit/molecule/row/VerificationRow.kt` — exists (F-9 territory).
- `StyleguideScreen.kt` already renders `HistoryRow`.

## 2. Batch send pipeline (F-7) — and its single-shot trap

1. `MirrorIntent.ResolveAndSend` (toolbar button / ⌘↩ via `handleShellShortcut`), gated by
   `MirrorUiState.canSend` (`core/send/SendGate.kt`).
2. `MirrorViewModel.resolveAndSend()` — `presentation-feature-mirror/.../source/mirror/MirrorViewModel.kt`
   L801-878. Reads `state.frozen: FrozenFrame`, `currentFrozenHeld.value: HeldFrame(bitmap, bytes)`,
   `currentFrozenHierarchy.value`, `state.hierarchyStatus`, `state.platform`,
   `state.selectedDeviceId`, `sentAtMs = nowMs()`. Phases: `Preparing → Publishing → Idle|Failed`.
3. Payload build — `core/send/EnvelopeAssembly.kt#assembleEnvelope` →
   `Pair<AnnotationEnvelope, List<HandoffImage>>`. Prompt from `core/payload/PromptAssembly.kt#buildPrompt`
   (or `frozen.promptOverride`); hierarchy XML from `core/payload/`; crop geometry
   `core/payload/CropWindow.kt#cropWindow`; session id `core/send/SessionId.kt#mintSessionId(deviceId, frameId, sentAtMs)`;
   `PAYLOAD_FORMAT_VERSION` in `core/payload/PayloadEstimate.kt`.
4. `handoff.publish(envelope, images)`.
5. `MirrorSideEffect.SendComplete(count, summary, sessionId)` → snackbar in `RectifyWindow.kt`.

Batch model (`data-handoff-api/src/commonMain/.../source/envelope/AnnotationEnvelope.kt`):
`AnnotationEnvelope(version, sessionId, sentAtMs, device: EnvelopeDevice, prompt, hierarchyXml,
hierarchyAvailable, framePath, annotations: List<EnvelopeAnnotation>)` — `@Serializable`,
**field order is wire-breaking**. Plus `EnvelopeDevice(id,label,widthPx,heightPx)`,
`EnvelopeAnnotation(index,left,top,right,bottom,comment,cropPath)`, `HandoffImage(name,bytes)`.
Contract `.../source/handoff/AnnotationHandoff.kt`: `publish` / `latest` / `resolve`.
Desktop impl `desktopMain/.../core/slot/FileSlotAnnotationHandoff.kt`, factory
`fileSlotAnnotationHandoff(root = ~/.rectify/handoff)`. Android/iOS: `UnsupportedAnnotationHandoff`.
Koin: `HandoffSourcesProvider.{kt,desktop.kt,android.kt,ios.kt}`.

> **⚠️ Critical:** `FileSlotAnnotationHandoff.publish` **prunes every sibling `<sentAtMs>/`
> directory except the newest**, and `data-handoff-api/CLAUDE.md:48` says verbatim *"No history,
> no ring buffer, no expiry."* History therefore **cannot** read its thumbnails back out of
> `~/.rectify/handoff` — it must capture its own copy **at publish time**, from
> `frozenHeld.value.bytes`, before `publish()` runs.

## 3. Persistence — Room is a live but unused skeleton

- `@Database`: `data-database-impl/src/commonMain/.../core/database/AppDatabase.kt` —
  `internal abstract class AppDatabase : RoomDatabase()`, `entities = [ExampleEntity::class]`,
  `version = 1`, `exportSchema = true`, `@ConstructedBy(AppDatabaseConstructor::class)`.
- Only `core/entity/ExampleEntity.kt` + `core/dao/ExampleDao.kt` exist — documented placeholders,
  delete them when the first real table lands. `Migrations.kt` is an empty `internal object`;
  destructive migration is **disabled**.
- `core/DatabaseBuilder.kt` expect/actual; **desktop DB file = `~/rectify.db`** (not `~/.rectify/`),
  `BundledSQLiteDriver`. KSP configured for all four targets incl. `kspDesktop`;
  `room { schemaDirectory("$projectDir/schemas") }`.
- Koin `dataDatabaseImplModule` registered 2nd in `initKoin()`.
- **Nothing injects `AppDatabase` or any DAO today.** No history/batch table anywhere.
- `data-database-api` holds only generic bases under `source/datasource/base/`:
  `ReadableDataSource<T>`, `ObservableDataSource<T>`, `AppendableDataSource<T>`,
  `UpsertableDataSource<T>`, `DeletableDataSource`, `ClearableDataSource`.
- multiplatform-settings (`data-preference-impl`) carries all current persistence — key/value
  scalars only (Theme, Language, Onboarding, UnitSystem, SidePanelWidth, AdbPath). A history
  **list** does not belong there; Room is the right home.

## 4. Frozen frame & annotations at runtime

All in `presentation-feature-mirror/src/commonMain/.../source/mirror/`:

| Thing | Type | Held where |
|---|---|---|
| Live frame | `ImageBitmap` | `MirrorViewModel.frame: StateFlow<ImageBitmap?>` (outside Orbit state, deliberately) |
| Pinned frame | `ImageBitmap` | `MirrorViewModel.frozenFrame: StateFlow<ImageBitmap?>` |
| **Pinned frame PNG bytes** | `internal data class HeldFrame(bitmap, bytes)` (MirrorViewModel.kt:75) | **`frozenHeld: StateFlow<HeldFrame?>`** — `.bytes` is the adb-captured PNG attached as `frame.png`. **Thumbnail source.** |
| Hierarchy | `HierarchySnapshotModel` | `frozenHierarchy: StateFlow<..>` |
| Annotations + meta | `FrozenFrame(deviceSize, annotations: List<StagedAnnotation>, selectedIndex, frameId, deviceLabel, promptOverride, …)` (MirrorContract.kt:639) | **inside** Orbit state: `MirrorUiState.frozen` (:253) |
| One annotation | `StagedAnnotation(index, rect: DeviceRect, comment)` (:601) | inside `FrozenFrame` |
| Rect | `DeviceRect(left, top, right, bottom)` (:576), device-pixel space | |
| Send phase | `SendPhase { Idle, Preparing, Publishing, Sent, Failed }` (:185) | `MirrorUiState.send` |

PNG encoding already exists: `source/mirror/CropEncoder.kt`
(`fun interface CropEncoder { suspend fun encode(frame: ImageBitmap, rect: DeviceRect): ByteArray? }`,
never throws), desktop impl `desktopMain/.../core/crop/AwtCropEncoder.kt`
(clamp → `toAwtImage().getSubimage(..)` → `ImageIO.write(.., "png", ..)` on `Dispatchers.IO`);
Android/iOS actuals return `null` (AS-29). Decode seam: private `decodePng`
(`ByteArray.decodeToImageBitmap()`) in `MirrorViewModel`.
No downscaling helper exists — `getSubimage` crops but does not scale.

## 5. Module layout & where each F-8 piece goes

Modules (`settings.gradle.kts`): apps `:compose-application :aos-application :desktop-application
:mcp-application`; `:common-core`; data `:data-core :data-handoff-api :data-device-api
:data-device-impl :data-preference-api :data-preference-impl :data-database-api
:data-database-impl :data-repository-impl`; domain `:domain-core :domain-repository-api
:domain-usecase-api :domain-usecase-impl`; presentation-core `:presentation-core-localisation
:presentation-core-navigation-api :presentation-core-navigation-impl :presentation-core-styling
:presentation-core-ui`; features `:presentation-feature-{about,home,mirror,onboarding,settings,splash}`.
(`:ios-application` is on disk but not in settings.gradle.kts.)

| Piece | Where | Copy from |
|---|---|---|
| `@Entity` | `data-database-impl/.../core/entity/HistoryEntryEntity.kt` | `core/entity/ExampleEntity.kt` |
| `@Dao` | `.../core/dao/HistoryEntryDao.kt` | `core/dao/ExampleDao.kt` |
| DB registration | edit `core/database/AppDatabase.kt` (entities, bump version, accessor) + `core/migration/Migrations.kt` | — |
| DataSource impl | `data-database-impl/.../source/datasource/HistoryDataSourceImpl.kt` (folder is new) | shape: `data-preference-impl/.../source/datasource/ThemePreferenceSourceImpl.kt` |
| Repository interface | `domain-repository-api/.../source/repository/HistoryRepository.kt` | `ConfigureRepository.kt`, `DeviceRepository.kt` |
| Repository impl | `data-repository-impl/.../source/HistoryRepositoryImpl.kt` (`internal`) | `ConfigureRepositoryImpl.kt`, `DeviceRepositoryImpl.kt` |
| Mapper | `data-repository-impl/.../core/mapper/HistoryEntryMapper.kt` | `ThemePreferenceMapper.kt`, `HierarchySnapshotMapper.kt`, `DeviceResourceMapper.kt` |
| Domain model | `domain-core/.../source/model/history/HistoryEntryModel.kt` | `model/device/DeviceModel.kt`, `MirrorStatsModel.kt` |
| Use case api | `domain-usecase-api/.../source/usecase/history/*.kt` | `usecase/device/ObserveMirrorStatsUseCase.kt` (reactive), `usecase/configuration/SetThemeUseCase.kt` (one-shot) |
| Use case impl | `domain-usecase-impl/.../source/usecase/history/*UseCaseImpl.kt` | `ObserveMirrorStatsUseCaseImpl.kt`, `GetThemeUseCaseImpl.kt` |
| UI panel | `presentation-feature-mirror/.../source/mirror/MirrorHistoryTab.kt` | **`MirrorNotesTab.kt`** |
| Tab wiring | `compose-application/.../RectifyWindow.kt` (`else -> Unit`, `tabCounts`) | the two existing branches |
| Strings | `presentation-core-localisation/src/commonMain/composeResources/values/strings.xml` (67 `mirror_*` keys) | `mirror_annotation_notes_empty` |

**No new Gradle module is needed.** The History tab is a side-panel slot of the existing mirror
surface — same `MirrorViewModel` / `MirrorUiState` / `MirrorIntent`, exactly as Notes and Payload.
`MirrorHost`'s KDoc explicitly warns there is exactly **one** `MirrorHost` call site; a
`:presentation-feature-history` module would force a second `koinViewModel()` call site.
`compose-application` already depends on `dataDatabaseImpl` + `presentationFeatureMirror`;
`presentation-feature-mirror` already depends on `domainUsecaseApi` + `dataHandoffApi`.

## 6. Design tokens

Everything behind `presentation.core.styling.core.Theme` (composable-scope accessors
`Theme.color/.typography/.fontSize/.lineHeight/.spacing/.spacingToken/.radius/.corner/.cornerToken/.size/.stroke/.motion`).

| CSS var | Kotlin |
|---|---|
| `--ink` / `--ink2` / `--ink3` | `Theme.color.ink` / `.ink2` / `.ink3` |
| `--line` / `--line2` | `Theme.color.line` / `.line2` |
| `--hover` | `Theme.color.hover` |
| `--seg` | `Theme.color.seg` |
| `--stage` | `Theme.color.stage` (+ `.stageDot`) |
| `--primary` / `--onPrimary` | `Theme.color.primary` / `.onPrimary` (+ `.primaryHover`, `.primaryPress`) |
| surfaces | `.bg .chrome .surface .pop .codeBg .overlayScrim` |
| signals | `.annotate .sentFixed .warnStillOff .measure .focus` (identical in both themes) |

Typography: Roboto Flex (human) + JetBrains Mono (machine). Mono tokens `monoMeta` (status/meta —
what `HistoryRow` already uses, pair with `ink3`), `monoData`, `monoBadge`, `monoTag`, `monoRuler`.
The design's uppercase group header (JetBrains Mono 10.5px, `.08em`, uppercase, `--ink3`) maps to
`Theme.typography.overline` + `Theme.color.ink3`. Spacing `spacingXS/S/L/XL/2XL/3XL/4XL`;
corners `Theme.cornerToken.{tile,tileHero,sheet,card,nested,badge,button,chip,input}`; `Theme.stroke.thin`.

## 7. What exists for "history" today

Done & tested: `ShellTab.History`, Cmd+3 shortcut, `ShellState.selectTab`, `SidebarTabs` with
count, `HistoryRow`, `StatusPill`/`StatusPillTone`, `AppButtonSize.ListAction`, `VerificationRow`.
**Missing:** the panel body (`else -> Unit`), *all* persistence of sent batches (no entity, DAO,
table, repository, use case or state field), and any time-period grouping / timestamp formatting
(nearest prior art: `core/stats/StatsFormatter.kt`, `core/send/SendLabels.kt#toastLabel`).

## 8. Binding conventions (`.claude/rules/*.md`) — the ones that touch F-8

- **database.md** — `data-database-api` holds *only* generic composable base interfaces; never an
  entity-specific interface there — mix bases where consumed. `@Entity` → `core/entity/`,
  `@Dao` → `core/dao/`, one `AppDatabase` in `core/database/`. `AppDatabaseConstructor` must keep
  its explicit body-less `override fun initialize()` or the build fails. Schema change ⇒ **bump
  `@Database(version)` AND add a `Migration`** — destructive migration is off, a missing migration
  crashes existing installs. Delete `ExampleEntity`/`ExampleDao` once a real table lands.
- **domain-layer.md** — `domain-core` has zero deps (`Model` marker, sealed `Failure` with
  `Technical.{Database,Platform,Network,Preference}` / `Logic.{NotFound,Business}`).
  `domain-repository-api` = interfaces returning domain models / `Flow<Model?>`, never data
  resources. `domain-usecase-api` = one interface, one `public suspend operator fun invoke(...)`;
  **one-shot ⇒ `Result<T>` (`Optional = Result<Unit>`), reactive ⇒ bare `Flow<T>`, never
  `Flow<Result<T>>`**. Every `*UseCaseImpl` suspend body wraps in
  `resultLauncher(errorMapper = Failure.Technical::Database) { }` — not raw `runCatching`.
  Presentation and usecase-impl must never import a `data-*-impl` type.
- **mappers.md** — `common.core.source.mapper.Mapper<I,O>`; entity↔model pairs go through
  `data.repository.impl.core.mapper.base.ModelResourceMapper<MODEL, RESOURCE>` (`toModel`/`toResource`)
  as one `internal object` per type under `data-repository-impl/core/mapper/`. **A mapper never
  throws** — unknown stored values fall back to a safe default in `toModel`.
- **koin-di.md** — one Koin module per Gradle module at `di/<Name>Module.kt`, one
  `public val <camelCase>Module: Module`. `initKoin()` order is data → domain → presentation and is
  **load-order-significant**; append within the layer group, never reorder.
  `singleOf(::Impl) bind Interface::class`; explicit `viewModel { }` when raw `get<T>()` is needed
  (which is why `presentationFeatureMirrorModule` uses the explicit form).
- **orbit-mvi.md** — standard shape is `XContract/XViewModel/XScreen/XContent`. **Mirror
  deliberately deviates**: `MirrorHost` instead of Screen/Content (it is a set of shell slots, not a
  nav destination) and keeps `ImageBitmap`/`HeldFrame`/`HierarchySnapshotModel` in plain
  `StateFlow`s outside the reducer, because identity-compared values would make every frame a full
  state copy. **Follow mirror's local precedent inside that module.**
- **design-tokens.md** — no literal `Color(0x…)`, raw `.dp`, or inline `TextStyle` at a call site;
  a missing token is added to the `Theme*` data class. `AppTheme(mode, reducedMotion)` is the only
  provider. `presentation-core-ui` enforces atom → molecule → organism layering.
  `reducedMotion` resolves centrally via `Theme.motion.*`.
- **package-layout.md** — `core/` = internal/private only; `source/` = public, one subfolder per
  semantic concern. No empty `core/`. `explicitApi()` is repo-wide.
- Also touching F-8: **localisation.md** (all copy via `Res.string.*`; recent fix `dae0893` —
  no AAPT-style apostrophe escapes), **compose-preview-convention.md** (previews `private`,
  light+dark pairs), **error-handling.md**, **testing.md**, **platform-coverage.md** (desktop is the
  only real target; Android/iOS get null/no-op stand-ins, cf. AS-29).

## 9. Shortest viable shape (recon's recommendation)

One `@Entity` + `@Dao` in `data-database-impl` (first real table — delete `ExampleEntity`, bump to
v2 with a migration), one repository + mapper, two use cases (`RecordSentBatch`, `ObserveHistory`),
a `history: List<HistoryEntryModel>` field on `MirrorUiState`, `MirrorHistoryTab.kt` built from the
existing `HistoryRow`, and two edits in `RectifyWindow.kt`. Thumbnail bytes must be captured in
`resolveAndSend()` from `frozenHeld.value.bytes` **before** publish.
