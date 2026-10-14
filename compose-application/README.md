# compose-application

> Shared KMP application shell — root composable, Koin initialization, and platform entry points.

## Responsibility

Wires all KMP modules together into a running application. Hosts the root `App()` composable
(locale, theme, navigation), bootstraps the full Koin dependency graph via `initKoin()`, and
provides platform-specific entry points for Android, iOS, and Desktop.

This module is an AGP KMP **library** — it has no `android:name` Application class.
The Android entry point (`RectifyApplication`, `MainActivity`) lives in `:aos-application`.

## Dependencies

| Depends on | Purpose |
|---|---|
| `data-preference-impl` | Persisted language, theme, and unit-system preferences |
| `data-database-impl` | Local database |
| `data-repository-impl` | Repository implementations |
| `domain-usecase-impl` | Use case implementations |
| `presentation-core-localisation` | `AppLocaleProvider` |
| `presentation-core-navigation-api/impl` | `NavigationHost`, navigation contracts |
| `presentation-core-styling` | `AppTheme`, `ThemeMode` |
| `presentation-core-ui` | Shared UI kit |
| All `presentation-feature-*` | Feature screens and their DI modules |

## Public API

| Declaration | Platform | Description |
|---|---|---|
| `App()` | commonMain | Root composable — injects use cases, observes locale/theme, hosts navigation |
| `initKoin(config)` | commonMain | Initializes Koin with all module registrations; accepts optional platform config |
| `doInitKoin()` | commonMain | No-arg wrapper for `initKoin()` — used by iOS Swift entry point |
| `MainViewController()` | iosMain | Returns `UIViewController` wrapping `App()` for SwiftUI integration |
| `runDesktopApp()` | desktopMain | Opens a `Window` running `App()` via Compose Desktop's `application {}` |

## Usage

### Android (called from `aos-application`)

```kotlin
// RectifyApplication.kt
initKoin {
    androidLogger()
    androidContext(this@RectifyApplication)
}

// MainActivity.kt
setContent { App() }
```

### iOS (called from Swift)

```swift
// iOSApp.swift
KoinHelperKt.doInitKoin()

// ContentView.swift
MainViewControllerKt.MainViewController()
```

### Desktop (called from `desktop-application`)

```kotlin
// Main.kt
fun main() {
    initKoin()
    runDesktopApp()
}
```

## Demo Mode

`App()` contains a `private const val DEMO_MODE = false`. Set it to `true` to replace the
navigation host with the design system preview (`StyleguideScreen`).

**Never commit `DEMO_MODE = true`** — it disables the entire application flow.

## Koin Module Registration Order

`initKoin()` registers modules in a fixed order. Do not reorder — domain modules depend on
data contracts, and presentation modules depend on domain use cases:

```
data: preference → database → repository
domain: usecase
presentation: about → home → onboarding → settings → splash
```

## Testing

```bash
./gradlew :compose-application:check
```
