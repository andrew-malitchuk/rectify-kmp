# CLAUDE.md — presentation-feature-home

## Purpose

Landing screen shown after onboarding completes. Currently a minimal placeholder with Settings
and Styleguide shortcuts — replace `HomeSuccessContent` with your own app's main screen.

## Convention Plugins

- `dev.rectify.io.convention.feature`
- `dev.rectify.io.convention.di`

## Package Layout

```
presentation/feature/home/
  di/
    PresentationFeatureHomeModule.kt
  source/
    home/     ← public-facing composables, ViewModel, contract
```

> **Rule:** `source/` holds the public screen surface. Any internal helpers must go into `core/`.

## Key Files

### `source/home/`

| File | Purpose |
|------|---------|
| `HomeScreen` | Entry point composable; handles side effects and drives navigation |
| `HomeViewModel` | Orbit MVI ViewModel; no use-case dependencies — just routes intents to side effects |
| `HomeContract` | `HomeState` / `HomeSideEffect` / `HomeIntent` definitions |
| `HomeContent` | Root content composable; hosts `HomeSuccessContent` and snackbar |
| `HomeSuccessContent` | Landing UI with Settings and Styleguide buttons |

### `di/`

| File | Purpose |
|------|---------|
| `PresentationFeatureHomeModule` | Koin module registering `HomeViewModel` |

## Module Dependencies

- `presentation-core-localisation`
- `presentation-core-navigation-api`
- `presentation-core-styling`
- `presentation-core-ui`

## Build

```shell
./gradlew :presentation-feature-home:build
```
