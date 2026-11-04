# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

# [0.1.0] - 2026-11-04

First public release. Rectify mirrors a connected Android device or iOS simulator, turns a
frozen frame into numbered annotations, and hands the whole bundle to Claude Code over MCP —
then re-captures the device and proves, per annotation, whether the fix landed.

## Added

- **Shell and window** (F-1) — the Rectify window at 1240×820 with macOS full-size-content
  chrome, a resizable side panel, tabbed stage and keyboard shortcuts.
- **Android device discovery and mirroring** (F-2) — live frames from handsets and emulators
  over `adb`, with device polling and a picker.
- **Error handling and recovery** (F-3) — every missing toolchain, dead device and failed
  command surfaces as a recoverable error card with an action, never a crash.
- **Drag to annotate a frozen frame** (F-4) — dragging freezes the exact pixels and turns the
  box into a numbered annotation with a comment field.
- **Measure mode** (F-5) — holding <kbd>⌥</kbd> snaps the box to the matched hierarchy node
  and reports dp gaps to its neighbours.
- **Payload preview** (F-6) — the frame, per-annotation crops, hierarchy XML and generated
  prompt, assembled and inspectable before sending.
- **Resolve and send over MCP** (F-7) — a versioned annotation envelope published to an
  atomic single-slot handoff under `~/.rectify/handoff/`, served to Claude Code by a
  standalone stdio MCP server exposing `get_latest_annotation`.
- **History and batch tracking** (F-8) — every sent batch persisted to a local Room database
  with its frames and crops, browsable from the History tab.
- **Verify and re-diff loop** (F-9) — re-capture the device and diff each annotated region
  against the frozen original; matches resolve, mismatches offer a one-click re-send.
- **iOS simulator support** (F-10) — the same loop against simulators over
  `xcrun simctl`, and physical devices over `idb`.
- **Documentation site** — `docs/` rendered to static GitHub Pages by a build-time generator,
  deployed by CI on every push to `main` that touches it.
- **App icon and repository banner** — one source SVG driving the macOS, Windows and Linux
  icon formats, the runtime window icon and the generated README banner.

## Changed

- **Migrated the whole project from Kotlin Multiplatform to plain JVM.** Every module now
  applies `kotlin("jvm")` with the classic `src/main` / `src/test` layout; no
  `commonMain`/`desktopMain` split, no `expect`/`actual`, no second target. "Android" and
  "iOS" remain throughout the codebase as domain concepts — they are the devices Rectify
  talks to, not platforms it builds for.
- Convention plugins retargeted at `kotlin("jvm")` + `java-library`, pinned to
  `jvmToolchain(21)`, with `explicitApi()` enforced repo-wide.
- Project rules and module documentation rewritten for the JVM layout.
- README rewritten around the loop rather than the module inventory.

## Removed

- The Android and iOS application modules, which had no build target to serve.
- The navigation layer (`presentation-core-navigation-api` / `-impl`) — one window, one
  screen, no host.
- The splash, home, about, onboarding and settings features, scaffolding left from the
  multi-screen shell.
