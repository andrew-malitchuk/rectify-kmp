# compose-application — Claude Instructions

## Module Purpose

Shared KMP application shell. Owns three things only: the root `App()` composable, the Koin
graph bootstrap, and platform entry points. No feature logic, no ViewModels, no repositories.

## Package Layout Convention

```
dev.rectify.io/
├── core/          — internal-only components
│   └── demo/      — DemoHost (renders StyleguideScreen, never ship this)
└── source/        — public API, one subfolder per semantic group
    ├── app/       — App() composable
    ├── di/        — initKoin(), doInitKoin()
    ├── viewcontroller/  — iosMain: MainViewController()
    └── window/    — desktopMain: runDesktopApp()
```

**Rule:** anything `internal` goes in `core/`. Public declarations go in `source/<group>/`.

## Platforms

Targets Android, iOS, and Desktop (`jvm("desktop")`, current-OS only — no macOS/Linux native
target). If you add a native macOS/Linux target, update this file and the Build Commands section
below.

## This Module Is an AGP Library, Not an Application

The `:compose-application` module uses `com.android.kotlin.multiplatform.library`, not
`com.android.application`. It has no `android:name` in a manifest and no `applicationId`.
The Android `Application` subclass and `MainActivity` live in `:aos-application`.

## Demo Mode

`DEMO_MODE` is a `private const val` in `App.kt`. It must always be `false` before committing.
`DemoHost` (`core/demo/`) is `internal` — it cannot be accessed from outside this
module. Do not promote it to `public` or move it to `source/`.

## Koin Module Registration Order

The order in `initKoin()` is **load-order significant**:

```
data → domain → presentation
```

Registering a presentation module before its domain use case throws
`No definition found for type '...'` at the first injection site. Do not reorder.

When adding a new feature module's DI, append it to the end of its layer group.

## Swift Interop: Filename-Derived Class Names

Kotlin/Native exports top-level functions under a class named after their **file**, not their
package. Current mappings:

| Kotlin file | Swift class | Used in |
|---|---|---|
| `MainViewController.kt` | `MainViewControllerKt` | `ContentView.swift` |
| `KoinHelper.kt` | `KoinHelperKt` | `iOSApp.swift` |

If you rename a file, update the Swift call site. If you only move it to a different package,
no Swift change is needed — the class name stays the same. This does not apply to
`DesktopEntry.kt` (`runDesktopApp()`) — it's called from plain Kotlin JVM code
(`:desktop-application`'s `Main.kt`), not Swift, so file-rename-changes-class-name rules don't
matter there; a normal Kotlin import rename is enough.

## doInitKoin() Exists for Swift

Swift cannot easily construct a `KoinAppDeclaration` (a Kotlin functional type) to pass to
`initKoin()`. `doInitKoin()` is a no-arg bridge that calls `initKoin {}` with an empty config.
Do not inline it or remove it — the iOS entry point depends on it.

## Build Commands

```bash
# Android debug APK (via aos-application)
./gradlew :aos-application:assembleDebug

# iOS framework
./gradlew :compose-application:linkDebugFrameworkIosArm64

# Desktop app (via desktop-application)
./gradlew :desktop-application:run
```
