# domain-repository-api — Claude Instructions

## Module Purpose

Defines the repository contracts (interfaces) that sit at the boundary between the domain
layer and the data layer. Implementations live in `data-repository-impl`. The presentation
layer and use case layer must never depend on `data-repository-impl` directly.

## Package Layout Convention

```
domain.repository.api/
└── source/
    └── repository/
        └── ConfigureRepository.kt
```

**Rule:** API modules have no `internal` code, so `core/` is absent. Do not create it.

## Repository Contracts

### ConfigureRepository

Manages user-facing configuration: theme, onboarding status, application language, and unit system.

| Method | Description |
|--------|-------------|
| `getTheme()` / `setTheme()` / `observeTheme()` | Read/write/observe selected `ThemeModel` |
| `getApplicationLanguage()` / `setApplicationLanguage()` / `observeApplicationLanguage()` | Locale code management |

## Dependency Inversion

```
presentation-layer / domain-usecase-impl
    → domain-repository-api  (this module)
        ← data-repository-impl  (implements the contract)
```

Neither presentation nor domain-usecase-impl should ever import `data-repository-impl` types.

## Adding a New Repository

1. Create a new interface file under `source/repository/`.
2. Add KDoc on the interface and every method.
3. Add `implementation(projects.domainRepositoryApi)` to the consuming module's `build.gradle.kts`.
4. Create the implementation in `data-repository-impl/source/`.
5. Register it in `DataRepositoryImplModule`.

## Build

```shell
./gradlew :domain-repository-api:build
```
