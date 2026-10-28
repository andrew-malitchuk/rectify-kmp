# data-database-api

Pure API contract module for local database operations in Rectify KMP.

## Overview

Defines generic, composable data-source base interfaces that a Room-backed (or any other)
implementation can mix in as needed. Contains no implementations and no concrete entity/DAO
types — all persistence logic lives in `data-database-impl`.

## Public API

| Interface | Description |
|-----------|-------------|
| `ReadableDataSource<T>` | `getAll()` / `getById(id)` one-shot reads |
| `ObservableDataSource<T>` | `observeAll(): Flow<List<T>>` |
| `AppendableDataSource<T>` | `insert(resource): Long` (no update) |
| `UpsertableDataSource<T>` | `upsert(resource): Int` (insert-or-update) |

## Package

```
data.database.api.source.datasource.base
```

## Dependencies

- `:data-core` — `Resource` marker interface

## Usage

Define a concrete data-source interface (wherever it's consumed) by mixing in the base
interfaces it needs:

```kotlin
interface ExampleDataSource :
    ReadableDataSource<ExampleModel>,
    ObservableDataSource<ExampleModel>,
    UpsertableDataSource<ExampleModel>

// build.gradle.kts
implementation(projects.dataDatabaseApi)
```
