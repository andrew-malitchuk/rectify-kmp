# compose-application — Claude Instructions

## Module Purpose

Shared application shell. Owns three things only: the root `App()` composable, the Koin
graph bootstrap, and the desktop entry point. No feature logic, no ViewModels, no repositories.

## Package Layout Convention

```
dev.rectify.io/
├── core/          — internal-only components
└── source/        — public API, one subfolder per semantic group
    ├── app/       — App() composable
    ├── di/        — initKoin()
    └── window/    — runDesktopApp(), RectifyWindow()
```

**Rule:** anything `internal` goes in `core/`. Public declarations go in `source/<group>/`.

## Platforms

Desktop only — a plain `kotlin("jvm")` module with `compose.desktop.currentOs`. If you add
another platform, update this file and the Build Commands section below.

## This Module Is a Library, Not an Application

`:compose-application` has no `main()`. The runnable entry point lives in the plain-JVM
`:desktop-application` module, which calls `initKoin()` then `runDesktopApp()`.

## Demo Mode

`DEMO_MODE` is a `private const val` in `App.kt`. It must always be `false` before committing.
module. Do not promote it to `public` or move it to `source/`.

## Koin Module Registration Order

The order in `initKoin()` is **load-order significant**:

```
data → domain → presentation
```

Registering a presentation module before its domain use case throws
`No definition found for type '...'` at the first injection site. Do not reorder.

When adding a new feature module's DI, append it to the end of its layer group.

## Build Commands

```bash
# Desktop app (via desktop-application)
./gradlew :desktop-application:run
```
