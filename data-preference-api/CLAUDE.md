# data-preference-api — Claude Instructions

## Module Purpose

Pure API contract module for application preferences. Defines the generic `PreferenceSource<T>`
base interface and typed preference datasources (theme, language, side-panel width, adb path).
No implementations, no Koin modules — all of that lives in `data-preference-impl`.

## Package Layout Convention

```
data.preference.api/
└── source/
    ├── datasource/
    │   ├── base/
    │   │   └── PreferenceSource.kt        — generic suspend interface
    │   ├── ThemePreferenceSource.kt
    │   ├── LanguagePreferenceSource.kt
    └── model/
        ├── ThemePreference.kt
        ├── LanguagePreference.kt
```

## PreferenceSource Contract

```kotlin
public interface PreferenceSource<T> {
    suspend fun getData(): T          // one-shot read, never null
    suspend fun setData(data: T)      // persist and notify observers
    fun observeData(): Flow<T>        // hot reactive stream
}
```

Each concrete interface is a typed alias: `ThemePreferenceSource : PreferenceSource<ThemePreference>`.

## Preference Models

| Model | Key fields | Defaults |
|-------|-----------|---------|
| `ThemePreference` | `theme: String` | `"system"` |
| `LanguagePreference` | `languageCode: String` | `"en"` |

All models implement `data.core.source.resource.Resource`.

## Adding a New Preference

1. Create the model under `source/model/`, implementing `Resource`.
2. Create the datasource interface under `source/datasource/`, extending `PreferenceSource<YourModel>`.
3. Add a corresponding implementation in `data-preference-impl`.
4. Register it in `DataPreferenceImplModule`.

## Build

```shell
./gradlew :data-preference-api:build
```
