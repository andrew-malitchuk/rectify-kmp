# CLAUDE.md — presentation-feature-onboarding

## Purpose

First-launch onboarding flow — a two-page (Welcome → Ready) pager that persists the onboarding-completed flag so subsequent launches skip directly to Home.

## Convention Plugins

- `dev.rectify.io.convention.feature`
- `dev.rectify.io.convention.di`

## Package Layout

```
presentation/feature/onboarding/
  core/
    component/   ← OnboardingPageContent, OnboardingPagerIndicator, ParallaxText (internal)
    page/        ← OnboardingPage enum (Welcome, Ready)
  di/
    PresentationFeatureOnboardingModule.kt
  source/
    onboarding/     ← public-facing composables, ViewModel, contract
```

> **Rule:** `source/` holds the public screen surface. Any internal helpers must go into `core/`.

## Key Files

### `source/onboarding/`

| File | Purpose |
|------|---------|
| `OnboardingScreen` | Entry point composable; collects side effects and drives navigation |
| `OnboardingViewModel` | Orbit MVI ViewModel; persists onboarding flag via `SetOnboardingStatusUseCase` |
| `OnboardingContract` | `OnboardingState` / `OnboardingSideEffect` / `OnboardingIntent` definitions |
| `OnboardingContent` | Root content composable; hosts `OnboardingSuccessContent` and snackbar |
| `OnboardingSuccessContent` | Pager UI (`OnboardingPage.Welcome` → `.Ready`) with a Next/Initialize CTA |

### `core/`

| File | Purpose |
|------|---------|
| `OnboardingPage` | Two-value enum driving the pager (`Welcome`, `Ready`) |
| `OnboardingPageContent` | Per-page title/body with parallax text |
| `OnboardingPagerIndicator` | Page-count indicator at the top of the screen |

### `di/`

| File | Purpose |
|------|---------|
| `PresentationFeatureOnboardingModule` | Koin module registering `OnboardingViewModel` |

## Module Dependencies

- `domain-core`
- `domain-usecase-api`
- `presentation-core-localisation`
- `presentation-core-navigation-api`
- `presentation-core-styling`
- `presentation-core-ui`

## Build

```shell
./gradlew :presentation-feature-onboarding:build
```
