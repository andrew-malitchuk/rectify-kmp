# domain-repository-api

> Repository interfaces defining the contract between domain and data layers.

## Responsibility

Declares the repository abstractions that the domain layer consumes and the data layer implements. This module enforces the dependency inversion principle — upper layers depend only on these interfaces, never on concrete data implementations.

## Dependencies

| Depends on | Purpose |
|---|---|
| `domain-core` | Domain models and Failure types |

## Public API

| Interface | Description |
|---|---|

### ConfigureRepository — Key Methods

| Method | Description |
|---|---|
| `observeTheme()` | Observe theme changes as `Flow` |
| `getTheme()` / `setTheme()` | Read/write current theme |
| `observeApplicationLanguage()` | Observe language preference |
| `getApplicationLanguage()` / `setApplicationLanguage()` | Read/write language |

## Usage

```kotlin
// Injected via Koin in UseCase
class GetThemeUseCaseImpl(
    private val configureRepository: ConfigureRepository,
) : GetThemeUseCase {
    override suspend fun invoke(): Result<ThemeModel> =
        configureRepository.getTheme()
}
```

## Testing

```bash
./gradlew :domain-repository-api:test
```
