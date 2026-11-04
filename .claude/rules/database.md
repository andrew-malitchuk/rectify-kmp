---
paths:
  - "data-database-api/**"
  - "data-database-impl/**"
---

# Database (Room)

- `data-database-api` — generic, composable base interfaces only
  (`ReadableDataSource<T>`, `ObservableDataSource<T>`, `AppendableDataSource<T>`,
  `UpsertableDataSource<T>`). `DeletableDataSource`/`ClearableDataSource` were deleted as
  dead code — add them back only when something implements them. No concrete
  entity-specific interface lives here — define those where they're consumed (e.g. in
  `domain-repository-api` or a feature module) by mixing in whichever base interfaces apply.
- `data-database-impl` — the Room implementation: `@Entity` classes under `core/entity/`,
  `@Dao` interfaces under `core/dao/`, the single `AppDatabase` under `core/database/`.
  **Exception:** an `@Entity` that is also the wire type of a public datasource (e.g.
  `HistoryEntryEntity`/`HistoryAnnotationEntity`, returned by the public `HistoryDataSource`)
  lives under `source/entity/` instead — `package-layout.md` reserves `core/` for
  `internal`/`private` declarations only, and `explicitApi()` forbids a public member naming an
  internal type. The `@Dao` interface itself stays `internal` under `core/dao/` regardless;
  only the entity classes it returns need to be public.
- **No `@ConstructedBy`/`RoomDatabaseConstructor`.** That pair is the Room *KMP* pattern,
  needed because a `expect object` constructor has to be generated per target. This is a plain
  JVM project: Room resolves `AppDatabase_Impl` itself, and `AppDatabase` carries `@Database`
  alone. Don't reintroduce `AppDatabaseConstructor` — it will not compile without the KMP
  plugin.
- `DatabaseBuilder` is a plain `internal fun Module.provideDatabaseBuilder()` storing
  `rectify.db` under `~/.rectify/` (relocated from directly under
  `System.getProperty("user.home")` by F-8, same root as the handoff slot). KSP is wired with
  the ordinary `ksp(libs.room.compiler)` configuration — no per-target `ksp<Target>` entries.
- Schema changes: bump `@Database(version = ...)` and add a `Migration` to `Migrations.kt`.
  Destructive migration is disabled — there is no fallback path, a missing migration crashes
  the app on existing installs. **Narrow exception:** F-8 added two tables and stayed at
  `version = 1` with no `Migration`, because the same change relocated the desktop DB file
  from `~/rectify.db` to `~/.rectify/rectify.db` — a path nothing had ever written to, which is
  what made skipping the migration provably safe. Don't reuse "stay at the current version" for
  a change that touches an already-live table or doesn't move to an unwritten file first.
