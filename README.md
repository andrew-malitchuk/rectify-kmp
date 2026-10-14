# Rectify KMP

A Kotlin Multiplatform (KMP) starter template targeting **Android**, **iOS**, and **Desktop** (JVM), with shared UI via Compose Multiplatform.

## Overview

Rectify KMP is a modular, production-ready KMP template. It ships with a complete build system, dependency injection wiring, type-safe navigation, a full UI component library, onboarding flow, settings screen, about screen, and localisation/theme infrastructure — ready to extend with your own domain logic.

## Module Structure

```
rectify-kmp/
├── build-logic/convention/          ← Gradle convention plugins
├── compose-application/             ← KMP app entry point (common, Android, iOS, desktop)
├── aos-application/                 ← Android-specific application module
├── desktop-application/             ← Plain-JVM desktop application module
├── ios-application/                 ← Xcode project / iOS entry point
│
├── presentation-feature-splash/     ← Splash screen
├── presentation-feature-onboarding/ ← First-launch onboarding flow
├── presentation-feature-home/       ← Home screen
├── presentation-feature-settings/   ← Settings screen (incl. language picker)
├── presentation-feature-about/      ← About screen
│
├── presentation-core-ui/            ← Shared UI component library (atoms → organisms)
├── presentation-core-styling/       ← Theme tokens, colours, typography
├── presentation-core-localisation/  ← String resources & language switching
├── presentation-core-navigation-api/   ← Destination sealed class & AppNavigator interface
├── presentation-core-navigation-impl/  ← Navigation host (Navigation 3)
│
├── domain-core/                     ← Shared domain models
├── domain-repository-api/           ← Repository interfaces
├── domain-usecase-api/              ← Use case interfaces
├── domain-usecase-impl/             ← Use case implementations (theme, language, onboarding, unit system)
│
├── data-core/                       ← Shared data-layer utilities
├── data-preference-api/             ← Preference source interfaces
├── data-preference-impl/            ← Preference implementations (DataStore / NSUserDefaults)
├── data-database-api/               ← Local database contracts
├── data-database-impl/              ← Room database implementation (skeleton + example entity)
├── data-repository-impl/            ← Repository implementations
└── common-core/                     ← Shared utilities
```

## Convention Plugins

Defined in `build-logic/convention` under the `dev.rectify.io.convention` namespace:

| Plugin ID | Purpose |
|---|---|
| `dev.rectify.io.convention.application` | Android/iOS app module setup |
| `dev.rectify.io.convention.feature` | Feature modules with Compose & Orbit MVI |
| `dev.rectify.io.convention.library` | Shared library modules |
| `dev.rectify.io.convention.di` | Koin DI wiring |

## Build Commands

```bash
# Build Android debug APK
./gradlew :aos-application:assembleDebug

# Run all checks
./gradlew check

# Build iOS debug framework (used by Xcode)
./gradlew :compose-application:linkDebugFrameworkIosArm64

# Run the desktop app
./gradlew :desktop-application:run

# Clean build
./gradlew clean
```

For iOS, open `ios-application/` in Xcode 16+ and run from there.

## Tech Stack

| Aspect | Library |
|---|---|
| UI | Compose Multiplatform 1.12.1, Material3 |
| State management | Orbit MVI 12.0.1 |
| Dependency injection | Koin 4.2.2 |
| Navigation | AndroidX Navigation 3 |
| Local database | Room 2.8.5 |
| Preferences | multiplatform-settings 1.3.0 |
| Serialization | kotlinx-serialization 1.11.0 |
| Coroutines | kotlinx-coroutines 1.11.0 |
| Build | Gradle 9.8.1, Kotlin 2.4.21, JVM 21 |
| Android | minSdk 27, targetSdk 37 |

## Prerequisites

- Android Studio Ladybug or newer
- Xcode 16+ (for iOS)
- JDK 21 (for Desktop)

## License

Apache 2.0 License

```
Copyright (c) 2026 Andrew Malitchuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
