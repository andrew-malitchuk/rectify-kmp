# data-preference-impl — Claude Instructions

## Module Purpose

Platform-specific implementation of `data-preference-api`. Uses `multiplatform-settings` as
the persistence backend (Android DataStore, iOS NSUserDefaults, Desktop java.util.prefs) and
exposes reactive `Flow`-based datasources via Koin.

## Package Layout Convention

```
data.preference.impl/
├── commonMain/
│   ├── source/
│   │   ├── SettingsProvider.kt            — internal expect function
│   │   └── datasource/
│   │       ├── ThemePreferenceSourceImpl.kt
│   │       ├── LanguagePreferenceSourceImpl.kt
│   │       ├── OnboardingPreferenceSourceImpl.kt
│   │       └── UnitSystemPreferenceSourceImpl.kt
│   └── di/
│       └── DataPreferenceImplModule.kt
├── androidMain/source/
│   ├── SettingsProvider.android.kt    — DataStore-backed Settings
│   └── DataStoreSettings.kt           — Settings adapter for DataStore
├── iosMain/source/
│   └── SettingsProvider.ios.kt        — NSUserDefaults-backed
└── desktopMain/source/
    └── SettingsProvider.desktop.kt    — java.util.prefs-backed
```

Android, iOS, and Desktop (JVM) — no macOS/Linux native source sets exist. If you add them,
update this file.

## Implementation Pattern

Each `*SourceImpl` holds a `MutableStateFlow` initialised from the backing store on
construction. `setData()` writes through to `Settings` and updates the flow. `observeData()`
returns the state flow. `getData()` reads fresh from `Settings` for correctness on first call.

```kotlin
// Key naming convention
const val KEY_THEME = "pref_theme"
const val KEY_LANGUAGE = "pref_language"
const val KEY_ONBOARDING = "pref_onboarding_completed"
const val KEY_UNIT_SYSTEM = "pref_unit_system"
```

**Never change existing keys** — changing a key silently loses stored data on existing installs.

## Platform Differences

| Platform | Backend |
|----------|---------|
| Android | `DataStore<Preferences>` via `DataStoreSettings` adapter |
| iOS | `NSUserDefaults` via `multiplatform-settings` |
| Desktop | `java.util.prefs.Preferences` via `PreferencesSettings` (node `dev.rectify.io`) |

Android uses a named DataStore (`rectify_prefs`). iOS and Desktop use their platform default
storage directly.

## Adding a New Preference

1. Add the API contract in `data-preference-api` first.
2. Create `YourPreferenceSourceImpl` in `source/datasource/`.
3. Add `const KEY_* = "pref_..."` constants — all lowercase, underscore-separated.
4. Register in `DataPreferenceImplModule`: `singleOf(::YourPreferenceSourceImpl) bind YourPreferenceSource::class`.

## Build

```shell
./gradlew :data-preference-impl:build
```
