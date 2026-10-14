# domain-usecase-api

> Use case interfaces that define the primary domain API consumed by the presentation layer.

## Responsibility

Provides single-responsibility use case contracts. Each use case wraps one repository operation and serves as the sole entry point from presentation into the domain layer.

## Dependencies

| Depends on | Purpose |
|---|---|
| `domain-core` | Domain models and Failure types |

## Public API

### Configuration Use Cases

| Interface | Description |
|---|---|
| `GetThemeUseCase` | Retrieve current theme |
| `SetThemeUseCase` | Update theme preference |
| `ObserveThemeUseCase` | Observe theme changes as Flow |
| `GetOnboardingStatusUseCase` | Get onboarding completion status |
| `SetOnboardingStatusUseCase` | Mark onboarding as complete |
| `GetApplicationLanguageUseCase` | Get current language |
| `SetApplicationLanguageUseCase` | Set application language |
| `ObserveApplicationLanguageUseCase` | Observe language changes as Flow |

### Utilities

| Class | Description |
|---|---|
| `Optional<T>` | Monad for representing optional values |

## Usage

```kotlin
// In a ViewModel — use cases are injected via Koin
class SettingsViewModel(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
) : ViewModel(), ContainerHost<State, SideEffect> {

    fun loadTheme() = intent {
        getThemeUseCase()
            .onSuccess { theme -> reduce { state.copy(theme = theme) } }
            .onFailure { postSideEffect(SideEffect.ShowError(it)) }
    }
}
```

## Testing

```bash
./gradlew :domain-usecase-api:test
```
