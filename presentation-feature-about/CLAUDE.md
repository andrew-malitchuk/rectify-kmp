# CLAUDE.md — presentation-feature-about

## Purpose

Simple informational screen displaying app name, version, build number, and a brief description. Minimal MVI implementation with back navigation only — no async loading.

## Convention Plugins

- `dev.rectify.io.convention.feature`
- `dev.rectify.io.convention.di`

## Package Layout

```
presentation/feature/about/
  di/
    PresentationFeatureAboutModule.kt
  source/
    about/     ← public-facing composables, ViewModel, contract
```

> **Rule:** `source/` holds the public screen surface. Any internal helpers must go into `core/`.

## Key Files

### `source/about/`

| File | Purpose |
|------|---------|
| `AboutScreen` | Entry point composable; collects side effects and drives navigation |
| `AboutViewModel` | Orbit MVI ViewModel; handles back navigation |
| `AboutContract` | `AboutState` / `AboutSideEffect` / `AboutIntent` definitions |
| `AboutContent` | Root content composable; hosts `AboutSuccessContent` and snackbar |
| `AboutSuccessContent` | Full-screen info layout: title, app-info card, version card, back CTA |

### `di/`

| File | Purpose |
|------|---------|
| `PresentationFeatureAboutModule` | Koin module registering `AboutViewModel` |

## Module Dependencies

- `domain-core`
- `presentation-core-localisation`
- `presentation-core-navigation-api`
- `presentation-core-styling`
- `presentation-core-ui`

## Build

```shell
./gradlew :presentation-feature-about:build
```
