# data-preference-impl — Claude Instructions

## Module Purpose

Implementation of `data-preference-api`. Uses `multiplatform-settings` as the persistence
backend (Desktop `java.util.prefs`) and exposes reactive `Flow`-based datasources via Koin.

## Package Layout Convention

```
data.preference.impl/
├── source/
│   ├── SettingsProvider.kt            — registers Settings (java.util.prefs-backed)
│   └── datasource/
│       ├── ThemePreferenceSourceImpl.kt
│       ├── LanguagePreferenceSourceImpl.kt
└── di/
    └── DataPreferenceImplModule.kt
```

## Implementation Pattern

Each `*SourceImpl` holds a `MutableStateFlow` initialised from the backing store on
construction. `setData()` writes through to `Settings` and updates the flow. `observeData()`
returns the state flow. `getData()` reads fresh from `Settings` for correctness on first call.

```kotlin
// Key naming convention
const val KEY_THEME = "pref_theme"
const val KEY_LANGUAGE = "pref_language"
```

**Never change existing keys** — changing a key silently loses stored data on existing installs.

## Backend

| Platform | Backend |
|----------|---------|
| Desktop | `java.util.prefs.Preferences` via `PreferencesSettings` (node `dev.rectify.io`) |

## Adding a New Preference

1. Add the API contract in `data-preference-api` first.
2. Create `YourPreferenceSourceImpl` in `source/datasource/`.
3. Add `const KEY_* = "pref_..."` constants — all lowercase, underscore-separated.
4. Register in `DataPreferenceImplModule`: `singleOf(::YourPreferenceSourceImpl) bind YourPreferenceSource::class`.

## Build

```shell
./gradlew :data-preference-impl:build
```
