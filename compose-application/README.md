# compose-application

> Shared application shell — root composable, Koin initialization, and the desktop window.

## Responsibility

Wires all modules together into a running application. Hosts the root `App()` composable
(locale and theme), bootstraps the full Koin dependency graph via `initKoin()`, and
provides the desktop entry point.

This module is a **library** — the runnable `main()` lives in `:desktop-application`.

## Dependencies

| Depends on | Purpose |
|---|---|
| `data-preference-impl` | Persisted language, theme, and unit-system preferences |
| `data-database-impl` | Local database |
| `data-repository-impl` | Repository implementations |
| `domain-usecase-impl` | Use case implementations |
| `presentation-core-localisation` | `AppLocaleProvider` |
| `presentation-core-styling` | `AppTheme`, `ThemeMode` |
| `presentation-core-ui` | Shared UI kit |
| All `presentation-feature-*` | Feature screens and their DI modules |

## Public API

| Declaration | Platform | Description |
|---|---|---|
| `initKoin(config)` | main | Initializes Koin with all module registrations; accepts optional extra config |
| `runDesktopApp()` | main | Opens the Rectify `Window` via Compose Desktop's `application {}` |

## Usage

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
