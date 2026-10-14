# presentation-feature-onboarding

First-launch onboarding feature module — a two-page (Welcome → Ready) pager that marks onboarding as completed so subsequent launches skip directly to Home.

## Module Type

Feature (KMP + Compose Multiplatform)

## Key Classes

| Class | Description |
|-------|-------------|
| `OnboardingScreen` | Entry point composable; observes side effects and navigates to Home |
| `OnboardingViewModel` | Orbit MVI ViewModel; persists onboarding completion via `SetOnboardingStatusUseCase` |
| `OnboardingContract` | MVI contract: `OnboardingState`, `OnboardingSideEffect`, `OnboardingIntent` |
| `OnboardingContent` | Root content composable; hosts success UI and snackbar |
| `OnboardingSuccessContent` | `HorizontalPager` over `OnboardingPage.entries` (Welcome, Ready) with a Next/Initialize CTA |
| `PresentationFeatureOnboardingModule` | Koin module registering `OnboardingViewModel` |

## Architecture

Follows Orbit MVI pattern: `OnboardingScreen` → `OnboardingViewModel (ContainerHost)` → `OnboardingContract (State/SideEffect/Intent)`.

On "Initialize" tap (last page) the ViewModel calls `SetOnboardingStatusUseCase(true)`. Only after a successful persist does it emit `NavigateToHome` with `ClearTask` so the back stack is cleared and the user cannot return to onboarding.

Replace the two placeholder pages (`OnboardingPage.Welcome`/`.Ready`) with your own onboarding steps — add enum values in `core/page/OnboardingPage.kt` and their copy in `onboarding_<page>_title`/`_body` string resources.

## MVI Contract

**State (`OnboardingState`):** `currentPage`, `isLoading`

**Intents (`OnboardingIntent`):** `OnPageChanged(page)`, `OnGetStartedClick`

**Side Effects (`OnboardingSideEffect`):** `NavigateToHome`, `ShowError(messageId)`

## Dependencies

- `domain-core` — core domain types
- `domain-usecase-api` — `SetOnboardingStatusUseCase`
- `presentation-core-*` — shared UI, styling, localisation, navigation
- Orbit MVI — state management
- Koin — dependency injection

## Build

```shell
./gradlew :presentation-feature-onboarding:build
```
