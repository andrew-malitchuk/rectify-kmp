# presentation-core-navigation-api

Navigation abstractions — sealed destination hierarchy, navigator interface, and composition locals.

## Overview

Defines the app's type-safe navigation contract. Feature modules depend on this module to
read current navigation state and declare navigation targets without coupling to the
Navigation 3 runtime or any specific implementation.

## Public API

### AppNavigator

```kotlin
public interface AppNavigator {
    fun navigate(destination: Destination, options: NavOptions = NavOptions.Default)
    fun popBackStack()
    fun backAction()

    enum class NavOptions { Default, SingleTop, ClearTask }
}
```

### Destinations

| Destination | Description |
|-------------|-------------|
| `Destination.Splash` | Launch splash screen with animation |
| `Destination.Onboarding` | First-time onboarding flow |
| `Destination.Home` | Main home screen |
| `Destination.Settings` | App settings (theme, language, unit system) |
| `Destination.LanguagePicker` | Full-screen locale selection list |
| `Destination.About` | App information screen |
| `Destination.Styleguide` | Design-system showcase (dev utility) |

### Composition Locals

| Local | Type | Default behaviour |
|-------|------|-------------------|
| `LocalAppNavigator` | `AppNavigator?` | Throws if accessed without a provider |
| `LocalBackAction` | `() -> Unit` | Throws if accessed without a provider |

## Usage

```kotlin
val navigator = LocalAppNavigator.current
navigator?.navigate(Destination.Settings)
navigator?.navigate(Destination.Home, AppNavigator.NavOptions.ClearTask)

val back = LocalBackAction.current
back()
```

## Dependencies

- `androidx.navigation3:navigation3-ui`
- `org.jetbrains.kotlinx:kotlinx-serialization-core`
