# domain-core — Claude Instructions

## Module Purpose

Zero-dependency foundation for the entire domain layer. Defines the shared marker interface
(`Model`), the sealed `Failure` hierarchy for typed error handling, and the core domain models
(`ThemeModel`, plus the `device`/`history` model groups) that are referenced across both domain and
data modules.

## Package Layout Convention

```
domain.core/
└── source/
    ├── monad/
    │   └── Failure.kt        — sealed error hierarchy
    └── model/
        ├── base/
        │   └── Model.kt      — public marker interface
        ├── ThemeModel.kt
```

**Rule:** anything `internal` goes in `core/`. This module has no internal components —
all declarations are `public`. Do not create `core/` unless adding an `internal` declaration.

## Failure Hierarchy

```
Failure (sealed : Throwable)
├── Technical (sealed)
│   ├── Database(cause)   — Room / SQLite errors
│   ├── Platform(cause)   — Android/iOS OS-level errors
│   ├── Network(cause)    — HTTP / IO communication errors
│   └── Preference(cause) — Settings read/write errors
└── Logic (sealed)
    ├── NotFound          — requested entity does not exist
    └── Business(message) — business rule violation
```

Use `resultLauncher` (in `domain-usecase-impl`) to catch raw `Throwable`s and map them to the
correct `Failure.Technical.*` subtype. Never throw `Failure.Logic.*` from infrastructure code.

## What Belongs Here

| Belongs in domain-core | Does NOT belong |
|---|---|
| Marker interfaces (`Model`) | Business logic or use cases |
| Shared error hierarchy (`Failure`) | Repository interfaces |
| Domain models used by 2+ layers | Platform-specific code |
| — | Koin DI modules |

## Build

```shell
./gradlew :domain-core:build
```
