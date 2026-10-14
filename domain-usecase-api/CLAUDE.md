# domain-usecase-api — Claude Instructions

## Module Purpose

The primary domain API consumed by the presentation layer. Defines all use case interfaces
for app configuration (theme, language, onboarding, unit system) and the `Optional` typealias.

## Package Layout Convention

```
domain.usecase.api/
└── source/
    ├── monad/
    │   └── Optional.kt                    — typealias Result<Unit>
    └── usecase/
        └── configuration/                 — theme, onboarding, language, unit-system use cases
```

**Rule:** `core/` holds `internal` declarations only. This module has none — do not create it.
All public symbols go under `source/`.

## Optional Typealias

`Optional = Result<Unit>` — used for operations that succeed silently or fail with a
`Failure`. Import from `domain.usecase.api.source.monad.Optional`.

## Use Case Contract Rules

- Every use case exposes exactly **one** `operator fun invoke(...)`.
- Suspend operations return `Result<T>` or `Optional`.
- Reactive operations return `Flow<T>` (no `Result` wrapper at the Flow level).
- Use `@see` in the interface to cross-reference the implementation.

## What Belongs Here vs domain-usecase-impl

| Belongs in domain-usecase-api | Belongs in domain-usecase-impl |
|---|---|
| Use case interfaces | `*UseCaseImpl` classes |
| `Optional` typealias | `resultLauncher` utility |
| — | `DomainUseCaseImplModule` |

## Adding a New Use Case

1. Define the interface here under `source/usecase/<group>/`.
2. Implement it in `domain-usecase-impl` and register the binding in `DomainUseCaseImplModule`.

## Build

```shell
./gradlew :domain-usecase-api:build
```
