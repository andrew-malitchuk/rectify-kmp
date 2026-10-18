# data-database-impl

Room database implementation for Rectify KMP — persists sent-batch history.

## Overview

Provides the local persistence layer using Jetpack Room with KSP code generation, supporting
Android, iOS, and Desktop via platform-specific database builders. Two normalised tables back
the History tab: `history_entry` (one row per sent batch) and `history_annotation` (its
per-rect children, cascade-deleted with the parent).

## Architecture

```
DataDatabaseImplModule (Koin)
    └── DatabaseBuilder (expect/actual per platform)
        └── AppDatabase (Room, v1)
            └── HistoryEntryDao   — transactional insert, observeAll(), updateStatus()
                └── HistoryDataSourceImpl — bound to the public HistoryDataSource
```

## Database

- **Schema version:** 1
- **Tables:** `history_entry`, `history_annotation` (FK CASCADE on `history_entry.id`, indexed
  on `batchId`)
- **Desktop path:** `~/.rectify/rectify.db`
- Destructive migration is disabled; add a `Migration` in `Migrations.kt` when you change the
  schema further (version 1 shipped with no migration only because it relocated the desktop DB
  to a provably unwritten path in the same change — see `CLAUDE.md`).

## Package

```
data.database.impl.core             — internal: DatabaseBuilder, dao, datasource, database, migration
data.database.impl.source.entity    — public: HistoryEntryEntity, HistoryAnnotationEntity, HistoryEntryWithAnnotations
data.database.impl.source.datasource — public: HistoryDataSource
data.database.impl.di
```

## Dependencies

- `:data-database-api`
- `:data-core`
- `androidx.room:room-runtime`
- `androidx.sqlite:sqlite-bundled`
- `:koin-core`

## Setup

Included via Koin:

```kotlin
val dataDatabaseImplModule: Module = module {
    provideDatabaseBuilder()
}
```

## Adding Another Table

1. Add the `@Entity` class(es) — `source/entity/` if a public datasource returns it, otherwise
   `core/entity/` (see `.claude/rules/database.md`).
2. Add DAO method(s).
3. Update `AppDatabase.entities` and add the DAO accessor method(s).
4. Bump the Room `version` and add a `Migration`.
