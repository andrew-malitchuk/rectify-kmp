# desktop-application — Claude Instructions

## Module Purpose

The runnable desktop application module. Contains the single required entry point
(`main()`) and nothing else. No business logic, no ViewModels, no repositories — all of that
lives in the shared modules.

This module deliberately does **not** use the project's convention plugins
(`dev.rectify.io.convention.*`) — those wire a library's shared dependency set, and this
module is a binary with its own packaging config. It applies `kotlin("jvm")` +
`composeMultiplatform` + `composeCompiler` directly. `:data-mcp-impl` does the same, for the
same reason: the convention plugins all wire a dependency set it has no use for.

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

There is no lifecycle callback that runs before `main()` on desktop, so `main()` is the only
place Koin can be started. Don't move `initKoin()` into `runDesktopApp()`
(`compose-application`) — bootstrapping DI is the entry point's job, and `runDesktopApp()` is
shared code that a test or a second host could call without wanting a second `startKoin`.

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
