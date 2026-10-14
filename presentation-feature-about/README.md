# presentation-feature-about

About screen — displays app name, version, build number, and a brief description with a back navigation button.

## Module Type

Feature (KMP + Compose Multiplatform)

## Key Classes

| Class | Description |
|-------|-------------|
| `AboutScreen` | Entry-point composable; collects side effects and drives navigation |
| `AboutViewModel` | Orbit MVI ViewModel; handles back navigation via `GoBackEffect` |
| `AboutContract` | MVI contract: `AboutState`, `AboutSideEffect`, `AboutIntent` |
| `AboutContent` | Root content composable; hosts `AboutSuccessContent` and snackbar |
| `AboutSuccessContent` | Full-screen info layout with title, app-info card, version/build card, and back CTA |
| `PresentationFeatureAboutModule` | Koin module registering `AboutViewModel` |

## Architecture

Follows Orbit MVI pattern: `AboutScreen` → `AboutViewModel (ContainerHost)` → `AboutContract (State/SideEffect/Intent)`.

On "BACK" tap the ViewModel emits `GoBackEffect`. `AboutScreen` handles it via `LocalAppNavigator.backAction()`.

## MVI Contract

**State (`AboutState`):** `isLoading`, `appName`, `appVersion`, `buildNumber`

**Intents (`AboutIntent`):** `OnBackClick`

**Side Effects (`AboutSideEffect`):** `GoBackEffect`, `ShowError(message)`

## Dependencies

- `domain-core` — core domain types
- `presentation-core-*` — shared UI, styling, localisation, navigation
- Orbit MVI — state management
- Koin — dependency injection

## Build

```shell
./gradlew :presentation-feature-about:build
```
