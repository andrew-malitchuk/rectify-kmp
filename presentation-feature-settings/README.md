# presentation-feature-settings

App settings feature module — language, theme, unit system, data erasure, and system information.

## Module Type

Feature (KMP + Compose Multiplatform)

## Key Classes

### Settings

| Class | Description |
|-------|-------------|
| `SettingsScreen` | Entry point composable; wires navigation and snackbar |
| `SettingsViewModel` | Orbit MVI ViewModel; orchestrates all settings actions |
| `SettingsContract` | MVI contract: `SettingsState`, `SettingsSideEffect`, `SettingsIntent` |
| `SettingsContent` | Root composable; cross-fades shimmer ↔ success based on `isLoading` |
| `SettingsSuccessContent` | Full settings UI (Appearance, Danger, Info sections) |
| `SettingsPlaceholderContent` | Skeleton placeholder while preferences are loading |
| `PresentationFeatureSettingsModule` | Koin module registering `SettingsViewModel` and `LanguagePickerViewModel` |

### Language Picker

| Class | Description |
|-------|-------------|
| `LanguagePickerScreen` | Entry point composable for locale selection |
| `LanguagePickerViewModel` | Orbit MVI ViewModel; loads and persists the selected locale |
| `LanguagePickerContract` | MVI contract: `LanguagePickerState`, `LanguagePickerSideEffect`, `LanguagePickerIntent` |
| `LanguagePickerContent` | Root content composable; renders a scrollable locale list |

## Architecture

Follows Orbit MVI pattern: `Screen` → `ViewModel (ContainerHost)` → `Contract (State/SideEffect/Intent)`.

Two independent sub-features share the same Koin module:
- **Settings** — root screen with language/theme/unit-system sections, erase-all-data, and a link to About
- **LanguagePicker** — separate destination pushed from the Settings screen; persists locale and navigates back immediately, triggering a reactive update in `SettingsViewModel` via `ObserveApplicationLanguageUseCase`

## MVI Contract — Settings

**State (`SettingsState`):**
`isLoading`, `selectedLanguageIndex`, `languageOptions`, `languageCodes`, `selectedThemeIndex`, `themeOptions`, `selectedUnitIndex`, `unitSystemOptions`, `showEraseConfirmation`, `appVersion`

**Intents (`SettingsIntent`):**
`OnBackClick`, `OnLanguagePickerClick`, `SelectTheme(index)`, `SelectUnitSystem(index)`, `OnSystemInfoClick`, `OnEraseAllDataClick`, `OnDismissEraseConfirmation`, `OnEraseAllData`

**Side Effects (`SettingsSideEffect`):**
`GoBackEffect`, `NavigateToLanguagePicker`, `NavigateToAbout`, `NavigateToSplash`, `ShowError(messageId)`

## MVI Contract — Language Picker

**State (`LanguagePickerState`):** `selectedIndex`, `options`, `codes`

**Intents (`LanguagePickerIntent`):** `OnBackClick`, `SelectLanguage(index)`

**Side Effects (`LanguagePickerSideEffect`):** `GoBack`

## Dependencies

- `domain-core` — core domain types
- `domain-usecase-api` — use case interfaces for language, theme, unit system, and onboarding
- `presentation-core-*` — shared UI, styling, localisation, navigation
- Orbit MVI — state management
- Koin — dependency injection

## Build

```shell
./gradlew :presentation-feature-settings:build
```
