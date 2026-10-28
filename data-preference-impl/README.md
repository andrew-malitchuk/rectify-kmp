# data-preference-impl

> Preference storage implementations using multiplatform-settings.

## Responsibility

Implements all preference source interfaces from `data-preference-api`. Each implementation uses `MutableStateFlow` for reactive observation. The module provides a Koin module that registers the `Settings` instance and all preference source singletons.

## Dependencies

| Depends on | Purpose |
|---|---|
| `data-preference-api` | Preference interfaces to implement |
| `data-core` | `Resource` marker interface |

## Public API

| Symbol | Description |
|---|---|
| `dataPreferenceImplModule` | Koin module registering `Settings` and all preference source singletons |

### Internal Implementations

| Class | Implements | Storage Key |
|---|---|---|
| `LanguagePreferenceSourceImpl` | `LanguagePreferenceSource` | `pref_language` |
| `ThemePreferenceSourceImpl` | `ThemePreferenceSource` | `pref_theme` |

### Settings Provider

| Platform | Backend |
|---|---|
| Desktop | `java.util.prefs.Preferences` via `PreferencesSettings` (node `dev.rectify.io`) |

## Usage

```kotlin
// Koin DI wiring — include in your app module list
startKoin {
    modules(dataPreferenceImplModule)
}
```

## Testing

```bash
./gradlew :data-preference-impl:test
```
