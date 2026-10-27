# domain-usecase-impl

> Concrete use case implementations that bridge repository contracts to the presentation layer.

## Responsibility

Implements every use case interface from `domain-usecase-api` by delegating to the corresponding repository method. Wraps all calls in the `resultLauncher` pattern to convert exceptions into the sealed `Failure` hierarchy. Provides the Koin module that registers all use case singletons.

## Dependencies

| Depends on | Purpose |
|---|---|
| `domain-usecase-api` | Use case interfaces to implement |
| `domain-repository-api` | Repository interfaces to delegate to |
| `domain-core` | Domain models and Failure types |

## Public API

| Class | Description |
|---|---|
| `GetThemeUseCaseImpl` | Implements `GetThemeUseCase` |
| `SetThemeUseCaseImpl` | Implements `SetThemeUseCase` |
| `ObserveThemeUseCaseImpl` | Implements `ObserveThemeUseCase` |
| `GetApplicationLanguageUseCaseImpl` | Implements `GetApplicationLanguageUseCase` |
| `SetApplicationLanguageUseCaseImpl` | Implements `SetApplicationLanguageUseCase` |
| `ObserveApplicationLanguageUseCaseImpl` | Implements `ObserveApplicationLanguageUseCase` |
| `resultLauncher` | Internal suspend helper wrapping calls into `Result<T>` with `Failure` mapping |
| `DomainUseCaseImplModule` | Koin module registering all use case singletons |

## Key Patterns

### resultLauncher

All use case implementations use `resultLauncher` instead of `runCatching` to convert `Throwable` into the sealed `Failure` hierarchy:

```kotlin
internal suspend inline fun <T> resultLauncher(
    errorMapper: (Throwable) -> Failure,
    crossinline block: suspend () -> T,
): Result<T>
```

### Koin DI Registration

```kotlin
val domainUseCaseImplModule = module {
    single<GetThemeUseCase> { GetThemeUseCaseImpl(get()) }
    single<ListFilesUseCase> { ListFilesUseCaseImpl(get()) }
    // ... all use cases registered as singletons
}
```

## Testing

```bash
./gradlew :domain-usecase-impl:test
```
