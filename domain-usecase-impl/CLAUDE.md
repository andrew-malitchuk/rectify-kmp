# domain-usecase-impl — Claude Instructions

## Module Purpose

Concrete implementations of all use case interfaces defined in `domain-usecase-api`. Wraps
every suspending call in `resultLauncher` for uniform error mapping. Provides the single Koin
module that registers all use case singletons.

## Package Layout Convention

```
domain.usecase.impl/
├── core/                             — internal utilities (not part of public API)
│   └── ResultLauncher.kt            — internal suspend helper
└── source/
    ├── di/
    │   └── DomainUseCaseImplModule.kt
    └── usecase/
        └── configuration/            — theme / onboarding / language / unit-system implementations
```

**Rule:** `core/` is for `internal` declarations. `ResultLauncher` is `internal` — it stays
in `core/`. All `public` symbols go under `source/`.

## resultLauncher

```kotlin
internal suspend inline fun <T> resultLauncher(
    errorMapper: (Throwable) -> Failure,
    crossinline block: suspend () -> T,
): Result<T>
```

- Catches `Failure` (domain error) and wraps it as-is in `Result.failure`.
- Catches any other `Throwable`, maps it through `errorMapper`, then wraps.
- Typical usage: `resultLauncher(errorMapper = Failure.Technical::Preference) { ... }`

**Never** use raw `runCatching` in use case impls that go through a repository — always
use `resultLauncher` so infrastructure exceptions are properly typed.

## Koin Registration

All use case singletons are registered in `DomainUseCaseImplModule` using `singleOf()`, grouped
under Configuration (theme, onboarding, language, unit system).

## Adding a New Use Case

1. Define the interface in `domain-usecase-api/source/usecase/<group>/`.
2. Create `YourUseCaseImpl` in `source/usecase/<group>/` here.
3. Add `singleOf(::YourUseCaseImpl) bind YourUseCase::class` to `DomainUseCaseImplModule`.

## Build

```shell
./gradlew :domain-usecase-impl:build
```
