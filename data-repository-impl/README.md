# data-repository-impl

> Repository implementations bridging domain interfaces to preference data sources.

## Responsibility

Implements `ConfigureRepository` from the domain layer by orchestrating calls to preference data sources. Uses the `ModelResourceMapper` pattern for bidirectional conversion between domain models and data-layer resources.

## Dependencies

| Depends on | Purpose |
|---|---|
| `domain-repository-api` | Repository interfaces to implement |
| `domain-core` | Domain models and Failure types |
| `data-preference-api` | Preference data source interfaces |
| `data-core` | `Resource` marker interface |
| `common-core` | Shared utilities |

## Public API

| Symbol | Description |
|---|---|
| `ModelResourceMapper<MODEL, RESOURCE>` | Base interface for bidirectional mappers (`toModel` / `toResource`) |
| `dataRepositoryImplModule` | Koin module registering repository singletons |

### Internal Implementations

| Class | Implements | Data Sources Used |
|---|---|---|
| `ConfigureRepositoryImpl` | `ConfigureRepository` | `ThemePreferenceSource`, `LanguagePreferenceSource`, `SidePanelWidthPreferenceSource` |

### Mappers

| Object | Model | Resource |
|---|---|---|
| `ThemePreferenceMapper` | `ThemeModel` | `ThemePreference` |
| `LanguagePreferenceMapper` | `String?` | `LanguagePreference` |

## Usage

```kotlin
// Koin DI wiring
val repositoryModule = dataRepositoryImplModule

// Koin registration (inside the module)
singleOf(::ConfigureRepositoryImpl) bind ConfigureRepository::class
```

## Testing

```bash
./gradlew :data-repository-impl:test
```
