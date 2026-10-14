# Operator dictation — app-shell-window-layout

Provided by the engineer at pipeline launch (2026-10-14). Treat as authoritative
for the items it resolves; it closes two of the three open questions carried by
`inputs/input.md`.

## 1. Implementation platform — RESOLVED

`inputs/input.md` flags "Implementation platform/framework is unspecified" as a
material gap. It is now closed:

**Kotlin + Compose Multiplatform Desktop (JVM), macOS host.** Not AppKit/SwiftUI,
not Electron.

Evidence — the working proof-of-concept at
`/Users/andrew-malitchuk/StudioProjects/rectify-poc`:

```
settings.gradle.kts   rootProject.name = "rectify-kmp"; include(":app")
build.gradle.kts      kotlin("jvm") 2.4.20
                      org.jetbrains.compose 1.12.1
                      org.jetbrains.kotlin.plugin.compose 2.4.20
                      kotlin("plugin.serialization") 2.4.20
app/build.gradle.kts  jvmToolchain(21)
                      implementation(compose.desktop.currentOs)
                      kotlinx-serialization-json 1.7.3
                      compose.desktop.application { mainClass = "rectify.MainKt" }
```

This satisfies NFR-10 (macOS-only v1) via `compose.desktop.currentOs` — the Skiko
renderer is built for the host OS, so there is no cross-compilation and no
Windows/Linux artifact.

## 2. Reference implementation — `/Users/andrew-malitchuk/StudioProjects/rectify-poc`

A thin but REAL working spike. Read it before designing; do not re-derive what it
already proves.

| File | What it establishes |
|---|---|
| `app/src/main/kotlin/rectify/Main.kt` | The Compose Desktop window + entry point |
| `app/src/main/kotlin/rectify/Devices.kt` | adb device discovery (feeds the toolbar device indicator this shell must host) |
| `mcp-server/src/main/kotlin/rectify/mcp/Main.kt` | Separate local MCP server module (belongs to `resolve-and-send-mcp`, out of scope here — but the shell's toolbar reserves the "Resolve & Send" affordance for it) |

Module shape to carry forward: a Gradle multi-module JVM build, `:app` for the UI,
`:mcp-server` separate. The poc is a spike, not a template to preserve verbatim —
improve the structure where the plan calls for it, but keep the proven platform
choices (Compose Desktop, JVM 21, kotlinx-serialization).

Secondary reference: `/Users/andrew-malitchuk/StudioProjects/rectify-kmp` is a
separate, more rigorously structured Kotlin Multiplatform template by the same
author (convention plugins in `build-logic/`, `core/` vs `source/` package split,
`explicitApi()`, Koin DI, Orbit MVI). Use it as a CONVENTIONS reference for how
this author likes modules laid out. Do not assume its Android/iOS targets apply —
this deliverable is macOS desktop only (NFR-10).

## 3. Design source — screenshots cached, URL for reference

Claude Design project, file `Rectify Screens.dc.html`:
https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html

Two screens were supplied as images and are cached in this spec at
`raw/design/`. They are the primary visual authority for this deliverable —
they show the canonical Rectify Window chrome that FR-11 / AC-12 describe.

### `raw/design/01-first-run-platform-source.png` — "01 First run — platform & source"
Caption in the design: *"Platform switch lives in the toolbar on every screen;
tool versions are detected up front."* Light theme.

Observed chrome (this IS the shell under specification):
- **Toolbar** (top, full width): macOS traffic-light dots (red/amber/green) at
  far left; a segmented **Android | iOS** platform switch (Android active, white
  pill on a grey track); a **device indicator** dropdown reading `● No device ▾`;
  then far right — a staged counter `⓪ staged`, a **Clear** text button, and a
  disabled-looking primary **Resolve & Send ⌘↵** button.
- **Body**: a centred first-run panel, "Choose what to mirror" / "Switch platform
  or device any time from the toolbar."
  - `PLATFORM` section label (small, uppercase, letterspaced, muted) with two
    selectable cards side by side. Android card is selected — 1px near-black
    border, filled radio dot at top right; iOS card unselected — light grey
    border, empty radio ring. Each card: platform name in semibold, and below it
    a green status dot + monospace detection line (`adb 35.0.1 · /opt/homebrew/bin`
    and `idb 1.1.8 · Xcode 16.0`).
  - `SOURCE` section label with a right-aligned monospace meta line
    `3 found · adb devices -l`.
  - A bordered, rounded list grouped by header rows (`Physical device`,
    `Emulator`) with light grey header backgrounds. Rows carry a radio control,
    a device name, a monospace id (`37281FDH2000AB`, `emulator-5554`,
    `shut down`), and a right-aligned trailing slot — plain text
    (`Android 15 · USB`), a green dot + `Running`, or an outlined **Boot** button.
    The selected row has a subtle grey fill.
  - Footer row: monospace keyboard hints `↑↓ select · ↵ start` at left, and the
    primary **Start mirroring** button (near-black fill, white text, pill-ish
    radius) at right.
- **Status bar** (bottom, full width): green dot + monospace
  `adb 35.0.1 · idb 1.1.8 · ready` at left, `Rectify 0.4` at right.

### `raw/design/02-empty-no-device.png` — "02 Empty — no device yet"
Caption: *"Polls adb quietly; the one fast path (boot an emulator) is right
there."* Light theme. Same toolbar and status bar frame (status bar now reads
`● Waiting for device…`).

- **Stage area** fills the window with a subtle dotted/grid texture on a warm
  grey ground — this is the live-mirror region in its empty state.
- Left-of-centre: a large rounded-rectangle **dashed phone outline** placeholder
  with centred monospace `NO SIGNAL`.
- Right: a text block — "Waiting for an Android device" heading, "Mirroring
  starts on its own as soon as adb sees one." subtitle, then two numbered steps
  with small circled numerals: (1) "Plug in a phone with **USB debugging** on and
  accept the prompt." (2) "Or boot an emulator" followed by a dropdown
  (`Pixel_7_API_34 ▾`) next to a dark **Boot** button.
  Below, a muted monospace line with a dot: `adb devices · polling every 2 s`.

### What the screenshots confirm vs. the PRD's "unverified" design context
`inputs/input.md` lists the pixel/token detail (1240x820 window, chrome/stage/
surface/ink tokens, oklch signal colors, SF Pro + JetBrains Mono) as
*unverified context, not confirmed spec*. These two screenshots independently
corroborate the structural claims: the toolbar composition (platform switch +
device indicator + staged counter + Clear + Resolve & Send), the bottom status
bar, the bezel-less stage, and the dual-typeface system (proportional for
titles/body, monospace for ids/coords/versions/status). They do NOT show the
3-tab side panel (Notes/Payload/History) — these are pre-mirroring states where
no panel is present — so **AC-12's three-tab requirement is still unillustrated
and must be designed from FR-11 plus the remaining catalog screens.** They also
do not show dark theme, so **AC-30's light/dark parity is unillustrated** — both
remain open and must be resolved during planning, not assumed.

## 4. Scope reminder

This deliverable is the SHELL and the THEME SYSTEM — the frame every other
feature renders inside. The device-picker content and the empty-state content
visible in these screenshots belong to
`android-device-discovery-mirroring` (portfolio #1, blocked on this). Specify the
shell's slots, regions, tokens and theming here; do not implement device
discovery. Where a screenshot shows feature content, read it as evidence of what
the shell must be able to HOST, not as work to do now.
