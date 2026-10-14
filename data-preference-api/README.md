# data-preference-api

> Preference data source interfaces and models for persisting app configuration.

## Responsibility

Declares the contracts for reading, writing, and observing app preferences. Each preference type has a dedicated source interface extending `PreferenceSource<T>`, ensuring type-safe access to persisted values.

## Dependencies

| Depends on | Purpose |
|---|---|
| `data-core` | `Resource` marker interface for preference models |

## Public API

### Base Interface

| Interface | Description |
|---|---|
| `PreferenceSource<T>` | Generic contract: `getData(): T`, `setData(data: T)`, `observeData(): Flow<T>` |

### Preference Sources

| Interface | Type Parameter | Description |
|---|---|---|
| `LanguagePreferenceSource` | `LanguagePreference` | App language locale code |
| `ThemePreferenceSource` | `ThemePreference` | App theme (light, dark, system) |
| `OnboardingPreferenceSource` | `OnboardingPreference` | Onboarding completion flag |
| `UnitSystemPreferenceSource` | `UnitSystemPreference` | Measurement unit system (metric, imperial) |

### Models

| Class | Properties | Description |
|---|---|---|
| `LanguagePreference` | `languageCode: String` (default: `"en"`) | ISO 639-1 language code |
| `ThemePreference` | `theme: String` (default: `"system"`) | Theme identifier |
| `OnboardingPreference` | `isCompleted: Boolean` (default: `false`) | Onboarding status |
| `UnitSystemPreference` | `unitSystem: String` (default: `"metric"`) | Unit system identifier |

## Usage

```kotlin
// Read and observe a preference
val source: ThemePreferenceSource = get()
val current = source.getData() // ThemePreference(theme = "dark")
source.observeData().collect { pref -> applyTheme(pref.theme) }

// Write a preference
source.setData(ThemePreference(theme = "light"))
```

## Testing

```bash
./gradlew :data-preference-api:test
```
