# data-database-api — Claude Instructions

## Module Purpose

Pure API contract module for local database operations. Defines generic, composable base
interfaces for data sources (readable, observable, appendable, upsertable, deletable,
clearable) that a Room-backed (or any other) data source can implement. No implementations,
no Room annotations, no Koin modules — all of that lives in `data-database-impl`.

## Package Layout Convention

```
data.database.api/
└── source/
    └── datasource/
        └── base/
            ├── ReadableDataSource.kt      — getAll() / getById(id)
            ├── ObservableDataSource.kt    — observeAll(): Flow<List<T>>
            ├── AppendableDataSource.kt    — insert(resource): Long
            ├── UpsertableDataSource.kt    — upsert(resource): Int
            ├── DeletableDataSource.kt     — deleteById(id) / deleteAll()
            └── ClearableDataSource.kt     — clearAll()
```

**Rule:** anything `internal` goes in `core/`. This module currently has no internal
components, so `core/` is absent. Do not create it unless adding an `internal` declaration.

## What Belongs Here vs data-database-impl

| Belongs in data-database-api | Belongs in data-database-impl |
|---|---|
| Generic data-source mix-in interfaces | Room DAO implementations |
| — | `@Entity` annotated classes |
| — | Room `@Database` class |
| — | KSP-generated Room code |

## Usage Pattern

A concrete data source interface (defined where it's consumed — e.g. in `domain-repository-api`
or a feature module) mixes in whichever of these base interfaces it needs:

```kotlin
public interface ExampleDataSource :
    ReadableDataSource<ExampleModel>,
    ObservableDataSource<ExampleModel>,
    UpsertableDataSource<ExampleModel>,
    DeletableDataSource
```

## Adding a New Base Interface

1. Create the file under `source/datasource/base/`.
2. All symbols must be `public` — `explicitApi()` is enforced.
3. Document with KDoc.
4. Update `README.md` Public API table.

## Build

```shell
./gradlew :data-database-api:build
```
