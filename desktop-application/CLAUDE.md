# desktop-application — Claude Instructions

## Module Purpose

Plain-JVM (not KMP) desktop application module. Contains the single required entry point
(`main()`) and nothing else. No business logic, no ViewModels, no repositories — all of that
lives in KMP-shared modules, same as `aos-application`'s role for Android.

This module deliberately does **not** use the project's convention plugins
(`dev.rectify.io.convention.*`) — those are built around `KotlinMultiplatformExtension`, and
this module isn't a KMP module. It applies `kotlin("jvm")` + `composeMultiplatform` +
`composeCompiler` directly, mirroring how `aos-application` applies `com.android.application` +
`composeMultiplatform` + `composeCompiler` directly instead of a convention plugin.

## What Belongs in This Module

| Belongs here | Does NOT belong here |
|---|---|
| `main()` entry point | ViewModels |
| `compose.desktop.application { }` packaging config | Use case implementations |
| | Repository / DataSource logic |
| | Compose UI screens, shared KMP logic |

If you are tempted to add a ViewModel or a repository, put it in the appropriate `domain-*` or
`presentation-feature-*` module instead.

## Key Files

| File | Role |
|---|---|
| `source/entry/Main.kt` | JVM entry point — calls `initKoin()` then `runDesktopApp()` |
| `build.gradle.kts` | `compose.desktop.application { mainClass }`, Desktop toolchain |

## Why `main()` Calls `initKoin()` Directly

Android has `RectifyApplication.onCreate()` and iOS has Swift's `doInitKoin()` call site to
bootstrap Koin before any UI renders. Desktop has neither — there is no lifecycle callback that
runs before `main()`. `main()` is therefore the only place Koin can be started; don't move
`initKoin()` into `runDesktopApp()` (`compose-application`) — that would start Koin on every
platform's shared code path, duplicating what Android/iOS already do themselves.

## Build / Run Commands

```bash
./gradlew :desktop-application:run        # run the app
./gradlew :desktop-application:assemble   # build only
```

## Packaging

`compose.desktop.application { mainClass = "dev.rectify.io.source.entry.MainKt" }` in
`build.gradle.kts` is the only packaging config present. Native installer tasks
(`packageDmg`/`packageMsi`/`packageDeb`) are available from the Compose plugin but not
configured here — add a `nativeDistributions { }` block if you need them.
