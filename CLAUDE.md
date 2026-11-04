# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rectify — a desktop (JVM) application built with Compose Multiplatform. Every module is a plain `kotlin("jvm")` module; sources live in the classic `src/main/kotlin` + `src/test/kotlin` layout. Package: `dev.rectify.io`. (The repo is still *named* `rectify-kmp` — that is a name, not a target.)

Note: "Android" and "iOS" appear widely in this codebase as *domain* concepts — Rectify is a desktop tool that mirrors Android devices over `adb` and iOS simulators over `xcrun simctl`/`idb`. There are no Android or iOS *build targets*. See `.claude/rules/platform-coverage.md`.

Topic-specific conventions (network, database, domain layer, mappers, Koin DI, Orbit MVI, design tokens, package layout, testing) live in `.claude/rules/` and load automatically — see that directory rather than duplicating them here.

## Build Commands

```shell
# Run the desktop app
./gradlew :desktop-application:run

# Build everything and run all tests
./gradlew build

# Run all checks
./gradlew check

# Clean build
./gradlew clean

# Render the documentation site into docs-generator/build/docs-site
./gradlew generateDocs
```

## Architecture

### Module Structure

- **compose-application** — Shared module containing the app composition and the Rectify window
- **desktop-application** — Plain-JVM module hosting the desktop `main()` entry point
- **mcp-application** — Plain-JVM module hosting the MCP (Model Context Protocol) server Claude Code spawns over stdio; reads back the latest annotation batch Rectify published (see `mcp-application/CLAUDE.md`)
- **docs-generator** — Build-time tool that renders `docs/` into the static GitHub Pages site (`./gradlew generateDocs`); see `docs/README.md`
- **build-logic/convention** — Gradle convention plugins for standardized module configuration
- **data-handoff-api** — Koin-free library module defining the cross-process handoff contract (`AnnotationHandoff`) and wire envelope shared between the desktop app and `:mcp-application` (see `data-handoff-api/CLAUDE.md`)

### Convention Plugins (build-logic)

Four registered plugins under `dev.rectify.io.convention`:
- `application` — Shared app module setup
- `feature` — Feature modules with Compose dependencies
- `library` — Shared library modules
- `di` — Koin dependency injection wiring

All plugins extend `BaseConventionPlugin` which provides hooks for plugin, platform, and dependency configuration. Module naming derives from project path (e.g., `:feature-login` → `feature.login`). Explicit API mode is enforced in every module.

### Platform Entry Points

- **Desktop:** `:desktop-application`'s `main()` → `initKoin()` then `runDesktopApp()` → `application { RectifyWindow() }` — the canonical Rectify window (1240×820, min 1024×700, macOS full-size-content chrome) hosting `RectifyWindowShell`. This is the only entry point; there is no navigation host.

### Key Dependencies

| Purpose | Library |
|---------|---------|
| UI | Compose Multiplatform, Material3 |
| DI | Koin (core, compose, viewmodel) |
| Settings | multiplatform-settings |
| Serialization | kotlinx-serialization |
| Coroutines | kotlinx-coroutines-core |

### Toolchain

- Kotlin 2.4.21, Gradle 9.8.1, JVM target 21
- Desktop: plain `kotlin("jvm")`, current-OS Skiko renderer via `compose.desktop.currentOs` (no cross-compilation — packaging targets the OS you build on)
