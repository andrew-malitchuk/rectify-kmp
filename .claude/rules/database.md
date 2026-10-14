---
paths:
  - "data-database-api/**"
  - "data-database-impl/**"
---

# Database (Room)

- `data-database-api` — generic, composable base interfaces only
  (`ReadableDataSource<T>`, `ObservableDataSource<T>`, `AppendableDataSource<T>`,
  `UpsertableDataSource<T>`, `DeletableDataSource`, `ClearableDataSource`). No concrete
  entity-specific interface lives here — define those where they're consumed (e.g. in
  `domain-repository-api` or a feature module) by mixing in whichever base interfaces apply.
- `data-database-impl` — the Room implementation: `@Entity` classes under `core/entity/`,
  `@Dao` interfaces under `core/dao/`, the single `AppDatabase` under `core/database/`.
  Ships with a placeholder `ExampleEntity`/`ExampleDao` (schema v1) demonstrating the wiring —
  delete and replace with your own tables; nothing currently depends on them.
- **Room KMP constructor pattern:** `AppDatabaseConstructor` must be declared as
  `internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> { override fun initialize(): AppDatabase }`
  with an explicit, body-less `override fun initialize()`. Omitting that override is a real bug
  (not a style preference) — without it Kotlin treats the `expect object` as needing to satisfy
  the interface itself instead of deferring to the per-platform `actual` KSP generates, and the
  build fails with "is not abstract and does not implement abstract member".
- `DatabaseBuilder` is `expect`/`actual` per platform — Android uses `Context` (via Koin),
  iOS stores in the Documents directory, Desktop stores `rectify.db` under
  `System.getProperty("user.home")`. Room's KSP code generation needs a `kspDesktop`
  configuration entry too (see `build.gradle.kts` — alongside `kspAndroid`/`kspIosArm64`/
  `kspIosSimulatorArm64`), or the `desktopMain` actual won't compile.
- Schema changes: bump `@Database(version = ...)` and add a `Migration` to `Migrations.kt`.
  Destructive migration is disabled — there is no fallback path, a missing migration crashes
  the app on existing installs.
