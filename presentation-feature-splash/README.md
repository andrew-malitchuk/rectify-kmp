# presentation-feature-splash

Splash screen feature module — shows the launch UI while resolving whether onboarding is complete, then routes to Home or Onboarding.

## Module Type

Feature (KMP + Compose Multiplatform)

## Key Classes

| Class | Description |
|-------|-------------|
| `SplashScreen` | Entry point composable; observes side effects and navigates accordingly |
| `SplashViewModel` | Orbit MVI ViewModel; reads onboarding status and decides where to go next |
| `SplashContract` | `SplashState` (object, no fields) / `SplashSideEffect` |
| `SplashContent` | Root content composable hosting the success UI |
| `SplashSuccessContent` | Full-screen launch UI with the logo mark |
| `PresentationFeatureSplashModule` | Koin module registering `SplashViewModel` |

## Architecture

Follows Orbit MVI pattern: `SplashScreen` → `SplashViewModel (ContainerHost)` → `SplashContract`.

On container init, `GetOnboardingStatusUseCase` runs concurrently with a 1500 ms minimum display
timer (`max(usecase, 1500ms)`, not their sum), then:

1. Onboarding completed → `NavigateToHome`
2. Onboarding not completed → `NavigateToOnboarding`

Both navigation side effects use `AppNavigator.NavOptions.ClearTask` so the splash screen is removed from the back stack.

## Dependencies

- `domain-core` — core domain types
- `domain-usecase-api` — `GetOnboardingStatusUseCase`
- `presentation-core-*` — shared UI, styling, localisation, navigation
- Orbit MVI — state management
- Koin — dependency injection

## Build

```shell
./gradlew :presentation-feature-splash:build
```
