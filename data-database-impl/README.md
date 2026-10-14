# data-database-impl

Room database implementation for Rectify KMP — a skeleton ready for your own tables.

## Overview

Provides the local persistence layer using Jetpack Room with KSP code generation, supporting
Android, iOS, and Desktop via platform-specific database builders. Ships with a placeholder
`ExampleEntity`/`ExampleDao` pair demonstrating the pattern — delete and replace with your own.

## Architecture

```
DataDatabaseImplModule (Koin)
    └── DatabaseBuilder (expect/actual per platform)
        └── AppDatabase (Room, v1)
            └── ExampleDao   — placeholder, replace with your own DAO(s)
```

## Database

- **Schema version:** 1
- **Tables:** `example` (placeholder — delete once you add your own entities)
- Destructive migration is disabled; add a `Migration` in `Migrations.kt` when you change the schema.

## Package

```
data.database.impl.core
data.database.impl.core.database
data.database.impl.core.dao
data.database.impl.core.entity
data.database.impl.core.migration
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

## Replacing the Placeholder Entity

1. Delete `core/entity/ExampleEntity.kt` and `core/dao/ExampleDao.kt`.
2. Add your own `@Entity` class(es) and DAO interface(s).
3. Update `AppDatabase.entities` and add the DAO accessor method(s).
4. Bump the Room `version` and add a `Migration` if this isn't a fresh install.
