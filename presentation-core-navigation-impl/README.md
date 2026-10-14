# presentation-core-navigation-impl

> Navigation graph implementation — wires all feature screens into a single NavigationHost.

## Responsibility

Implements the `AppNavigator` interface using Navigation 3 and assembles the full navigation graph. The `NavigationHost` composable maps each `Destination` to its corresponding feature screen composable. Handles saved-state serialization for process-death recovery.

## Public API

| Class / Composable | Description |
|---|---|
| `AppNavigatorImpl` | Navigation 3-based implementation of `AppNavigator` |
| `NavigationHost` | Root composable; creates the back stack and routes destinations to screens |
| `navSavedStateConfiguration` | Serialization config registering all `Destination` subclasses for saved state |

## Destination Routing

| Destination | Screen |
|---|---|
| `Splash` | `SplashScreen()` |
| `Onboarding` | `OnboardingScreen()` |
| `Home` | `HomeScreen()` |
| `Settings` | `SettingsScreen()` |
| `LanguagePicker` | `LanguagePickerScreen()` |
| `About` | `AboutScreen()` |
| `Styleguide` | `StyleguideScreen(onBack)` |

## Dependencies

| Depends on | Purpose |
|---|---|
| `presentation-core-navigation-api` | `AppNavigator` interface and `Destination` types |
| `presentation-core-styling` | `Theme.color.canvas` for edge-to-edge background |
| `presentation-core-ui` | `StyleguideScreen` |
| `presentation-feature-splash` | Splash screen |
| `presentation-feature-onboarding` | Onboarding screen |
| `presentation-feature-home` | Home screen |
| `presentation-feature-settings` | Settings and LanguagePicker screens |
| `presentation-feature-about` | About screen |

## Usage

```kotlin
// Used in App.kt — typically called once at the root
@Composable
fun App() {
    AppTheme {
        NavigationHost()
    }
}
```

## Build

```bash
./gradlew :presentation-core-navigation-impl:build
```
