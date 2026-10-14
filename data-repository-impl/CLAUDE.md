# data-repository-impl — Claude Instructions

## Module Purpose

Bridges data-layer sources (preferences, database) to domain-layer repository contracts.
Contains `*RepositoryImpl` classes, a mapper layer, and a single Koin DI module. No domain
logic lives here — only coordination and mapping between data resources and domain models.

## Package Layout Convention

```
data.repository.impl/
├── core/
│   └── mapper/
│       ├── base/
│       │   └── ModelResourceMapper.kt     — internal bidirectional mapper interface
│       ├── ThemePreferenceMapper.kt
│       ├── LanguagePreferenceMapper.kt
│       ├── OnboardingPreferenceMapper.kt
│       └── UnitSystemPreferenceMapper.kt
├── source/
│   └── ConfigureRepositoryImpl.kt
└── di/
    └── DataRepositoryImplModule.kt
```

## Mapper Pattern

Each mapper object exposes two `Mapper<A, B>` instances:

```kotlin
internal object ThemePreferenceMapper {
    val toModel: Mapper<ThemePreference, ThemeModel> = Mapper { ... }
    val toResource: Mapper<ThemeModel, ThemePreference> = Mapper { ... }
}
```

## Repository Responsibilities

| Class | Implemented Contract | Data sources |
|-------|---------------------|--------------|
| `ConfigureRepositoryImpl` | `ConfigureRepository` | ThemePreferenceSource, OnboardingPreferenceSource, LanguagePreferenceSource, UnitSystemPreferenceSource |

## Adding a New Repository

1. Define the contract in `domain-repository-api`.
2. Create the impl class in `source/`.
3. Add mappers in `core/mapper/` if resource ↔ model conversion is needed.
4. Register in `DataRepositoryImplModule` with `singleOf(::YourImpl) bind YourInterface::class`.

## Build

```shell
./gradlew :data-repository-impl:build
```
