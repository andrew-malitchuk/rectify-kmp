# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rectify KMP — a Kotlin Multiplatform (KMP) project targeting Android, iOS, and Desktop (JVM), using Compose Multiplatform for shared UI. Package: `dev.rectify.io` (source and Android app ID).

Topic-specific conventions (network, database, domain layer, mappers, navigation, Koin DI, Orbit MVI, design tokens, package layout, testing) live in `.claude/rules/` and load automatically — see that directory rather than duplicating them here.

## Build Commands

```shell
# Build Android debug APK
./gradlew :aos-application:assembleDebug

# Run all checks
./gradlew check

# Build iOS framework (used by Xcode)
./gradlew :compose-application:linkDebugFrameworkIosArm64

# Run the desktop app
./gradlew :desktop-application:run

# Clean build
./gradlew clean
```

iOS app is built via Xcode from the `ios-application/` directory.

## Architecture

### Module Structure

- **compose-application** — Shared KMP module containing app entry points (common, Android, iOS, desktop source sets)
- **aos-application** — Android-specific Application/Activity entry point
- **desktop-application** — Plain-JVM module hosting the desktop `main()` entry point
- **build-logic/convention** — Gradle convention plugins for standardized module configuration
- **ios-application** — Xcode project wrapping the Compose UI in SwiftUI via `ComposeUIViewController`

### Convention Plugins (build-logic)

Four registered plugins under `dev.rectify.io.convention`:
- `application` — Android/iOS app module setup
- `feature` — Feature modules with Compose dependencies
- `library` — Shared library modules
- `di` — Koin dependency injection wiring

All plugins extend `BaseConventionPlugin` which provides hooks for plugin, platform, and dependency configuration. Module naming derives from project path (e.g., `:feature-login` → `feature.login`). Explicit API mode is enforced in all KMP modules.

### Platform Entry Points

- **Android:** `MainActivity` → `setContent { App() }`
- **iOS:** `MainViewController()` → `ComposeUIViewController { App() }`, wrapped in SwiftUI via `ComposeView` (UIViewControllerRepresentable)
- **Desktop:** `:desktop-application`'s `main()` → `initKoin()` then `runDesktopApp()` → `application { RectifyWindow() }` — the canonical Rectify window (1240×820, min 1024×700, macOS full-size-content chrome) hosting `RectifyWindowShell`
- **Common:** `App()` composable with Material3 theming

### Key Dependencies

| Purpose | Library |
|---------|---------|
| UI | Compose Multiplatform, Material3 |
| DI | Koin (core, compose, viewmodel) |
| Navigation | AndroidX Navigation 3 |
| Settings | multiplatform-settings |
| Serialization | kotlinx-serialization |
| Coroutines | kotlinx-coroutines-core |

### Toolchain

- Kotlin 2.4.21, Gradle 9.8.1, JVM target 21
- Android: minSdk 27, targetSdk 37
- iOS: Static frameworks for arm64, simulator arm64 (Intel simulator/`iosX64` dropped — unsupported by current Compose Multiplatform/AndroidX KMP artifacts)
- Desktop: `jvm("desktop")` target, current-OS Skiko renderer via `compose.desktop.currentOs` (no cross-compilation — packaging targets the OS you build on)
