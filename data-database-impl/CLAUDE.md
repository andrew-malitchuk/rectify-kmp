# data-database-impl — Claude Instructions

## Module Purpose

Room database implementation. Provides the local persistence skeleton — KSP-generated Room
DAOs for Android, iOS, and Desktop — with a placeholder entity/DAO pair (`ExampleEntity`/
`ExampleDao`) demonstrating the pattern. Replace them with your own tables.

## Package Layout Convention

```
data.database.impl/
└── core/
    ├── DatabaseBuilder.kt         — internal expect function (platform-specific DB init)
    ├── database/
    │   └── AppDatabase.kt         — abstract Room database class + AppDatabaseConstructor
    ├── dao/
    │   └── ExampleDao.kt          — placeholder DAO, delete and replace
    ├── entity/
    │   └── ExampleEntity.kt       — placeholder @Entity, delete and replace
    └── migration/
        └── Migrations.kt         — empty; add Migration objects here as the schema evolves
└── di/
    └── DataDatabaseImplModule.kt
```

Platform actuals for `DatabaseBuilder`:
- `androidMain/core/DatabaseBuilder.android.kt` — uses `Context`
- `iosMain/core/DatabaseBuilder.ios.kt` — stores in Documents directory
- `desktopMain/core/DatabaseBuilder.desktop.kt` — stores `rectify.db` under `user.home`

No macOS/Linux native source sets exist. If you add them, update this file.

## Architecture

Dependency chain: `DataDatabaseImplModule` → `DatabaseBuilder` (expect/actual)
→ `AppDatabase` → DAOs. Resource ↔ Entity mapping (if any) belongs in `data-repository-impl`,
not in DAO or Entity classes.

## Room KMP Constructor Pattern

`AppDatabaseConstructor` is an `expect object` implementing `RoomDatabaseConstructor<AppDatabase>`
with an explicit `override fun initialize(): AppDatabase` member (no body). This explicit
override is required — without it, Kotlin treats the `expect object` as needing to satisfy the
interface itself rather than deferring to the per-platform `actual` that KSP generates.

## Room Database

Current schema version: **1**, with a single placeholder table (`example`, from
`ExampleEntity`). `exportSchema = true` — when you change the entity set, bump `version` and add
a `Migration` in `Migrations.kt` rather than relying on destructive migration (disabled by
default).

## KSP Configuration

Room code generation requires `ksp<target>` configurations for every target:
`kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`, `kspDesktop`. See `build.gradle.kts` for
the full list.

## Replacing the Placeholder

1. Delete `ExampleEntity.kt` and `ExampleDao.kt`.
2. Add your own `@Entity` class(es) under `core/entity/` and DAO interface(s) under `core/dao/`.
3. Update `AppDatabase.entities` and add the DAO accessor method(s).
4. Add a `Migration` in `Migrations.kt` if this isn't a fresh install (version 1 → 2, etc.).

## Build

```shell
./gradlew :data-database-impl:build
```

Schema JSON files are exported to `schemas/` — commit these to track migrations in version
control.
