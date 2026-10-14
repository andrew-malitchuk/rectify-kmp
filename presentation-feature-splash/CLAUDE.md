# CLAUDE.md — presentation-feature-splash

## Purpose

Initial app launch screen — resolves navigation destination (Onboarding or Home) based on onboarding status.

## Convention Plugins

- `dev.rectify.io.convention.feature`
- `dev.rectify.io.convention.di`

## Key Files

| File | Purpose |
|------|---------|
| `SplashScreen` | Entry point composable; collects side effects and drives navigation |
| `SplashViewModel` | Orbit MVI ViewModel — resolves next destination via use cases |
| `SplashContract` | `SplashState` / `SplashSideEffect` / `SplashIntent` definitions |
| `SplashContent` | Root content composable; hosts `SplashSuccessContent` and snackbar |
| `SplashSuccessContent` | Full-screen success UI with sequenced animations and CTA button |
| `PresentationFeatureSplashModule` | Koin module registering `SplashViewModel` |

## Module Dependencies

- `domain-core`
- `domain-usecase-api`
- `presentation-core-localisation`
- `presentation-core-navigation-api`
- `presentation-core-styling`
- `presentation-core-ui`

## Build

```shell
./gradlew :presentation-feature-splash:build
```
