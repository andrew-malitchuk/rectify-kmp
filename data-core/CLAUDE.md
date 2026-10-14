# data-core — Claude Instructions

## Module Purpose

Zero-dependency foundation for the entire data layer. Defines the `Resource` marker interface
that all data-layer models must implement, providing an architectural boundary that separates
data classes from domain models.

## Package Layout Convention

```
data.core/
└── source/
    └── resource/
        └── Resource.kt   — public marker interface
```

**Rule:** `core/` is for `internal` components. This module has none — everything here is
a public contract. Do not add `core/` unless you introduce an `internal` declaration.

## What Belongs Here

| Belongs in data-core | Does NOT belong |
|---|---|
| Marker interfaces that tag data-layer types | Business logic |
| Shared base types for the data layer | Implementations |
| Utility extensions used only by the data layer | Domain models |

## Adding New Types

1. Place files under `source/<semantic-group>/`.
2. All symbols must be `public` — `explicitApi()` is enforced.
3. No external dependencies allowed — this module is a leaf.

## Build

```shell
./gradlew :data-core:build
```
