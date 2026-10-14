# CLAUDE.md — presentation-feature-settings

## Purpose

App settings — language, theme, unit system, erase-all-data, and a link to the About screen.

## Convention Plugins

- `dev.rectify.io.convention.feature`
- `dev.rectify.io.convention.di`

## Package Layout

```
presentation/feature/settings/
  di/
    PresentationFeatureSettingsModule.kt
  source/
    settings/       ← public-facing Settings sub-feature
    language/       ← public-facing Language Picker sub-feature
```

> **Rule:** `core/` is for `internal`/`private` components consumed only within the feature.
> `source/` is for screens, ViewModels, and contracts that form the public surface.

## Key Files

### `source/settings/`

| File | Purpose |
|------|---------|
| `SettingsScreen` | Entry point composable; drives navigation and snackbar |
| `SettingsViewModel` | Orbit MVI ViewModel; orchestrates all settings actions |
| `SettingsContract` | `SettingsState` / `SettingsSideEffect` / `SettingsIntent` definitions |
| `SettingsContent` | Root composable; cross-fades shimmer ↔ success |
| `SettingsSuccessContent` | Full settings UI: Appearance (language/theme/unit system), Danger (erase data), Info (version) |
| `SettingsPlaceholderContent` | Skeleton loading placeholder |

### `source/language/`

| File | Purpose |
|------|---------|
| `LanguagePickerScreen` | Entry point composable for the language picker |
| `LanguagePickerViewModel` | Orbit MVI ViewModel; loads and persists locale |
| `LanguagePickerContract` | `LanguagePickerState` / `LanguagePickerSideEffect` / `LanguagePickerIntent` |
| `LanguagePickerContent` | Root content composable; renders the locale list using `LanguageOptionRow` from `presentation-core-ui` |

### `di/`

| File | Purpose |
|------|---------|
| `PresentationFeatureSettingsModule` | Koin module registering `SettingsViewModel` and `LanguagePickerViewModel` |

## Module Dependencies

- `domain-core`
- `domain-usecase-api`
- `presentation-core-localisation`
- `presentation-core-navigation-api`
- `presentation-core-styling`
- `presentation-core-ui`

## Build

```shell
./gradlew :presentation-feature-settings:build
```
