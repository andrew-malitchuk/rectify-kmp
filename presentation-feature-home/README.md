# presentation-feature-home

Home screen — the landing screen shown after onboarding. A starting point to replace with your
own app's main screen.

## Module Type

Feature (KMP + Compose Multiplatform)

## Key Classes

| Class | Description |
|-------|-------------|
| `HomeScreen` | Entry-point composable; handles side effects and drives navigation |
| `HomeViewModel` | Orbit MVI ViewModel; no dependencies beyond the Orbit container |
| `HomeContract` | MVI contract: `HomeState`, `HomeSideEffect`, `HomeIntent` |
| `HomeContent` | Root content composable; hosts `HomeSuccessContent` and snackbar |
| `HomeSuccessContent` | Landing UI with Settings and Styleguide buttons |
| `PresentationFeatureHomeModule` | Koin module registering `HomeViewModel` |

## Architecture

Follows Orbit MVI: `HomeScreen` → `HomeViewModel (ContainerHost)` → `HomeContract`.

Currently a minimal placeholder — two buttons navigate to Settings and the Styleguide showcase.
Replace `HomeSuccessContent` with your own app's main screen and extend `HomeIntent`/`HomeSideEffect` as needed.

## MVI Contract

**State (`HomeState`):** `isLoading`

**Intents (`HomeIntent`):** `OnSettingsClick`, `OnStyleguideClick`

**Side Effects (`HomeSideEffect`):** `NavigateToSettings`, `NavigateToStyleguide`, `ShowError(messageId)`

## Dependencies

- `presentation-core-*` — shared UI, styling, localisation, navigation
- Orbit MVI — state management
- Koin — dependency injection

## Build

```shell
./gradlew :presentation-feature-home:build
```
