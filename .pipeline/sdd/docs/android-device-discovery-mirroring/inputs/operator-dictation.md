# Operator dictation — android-device-discovery-mirroring (F-2)

Autonomous run. The operator granted full autonomy for the whole chain; gates are closed by the
orchestrator taking the recommended option, with the rationale recorded in `survey[]`.

## 1. F-1 is DONE and merged — build on it, do not rebuild it

`app-shell-window-layout` shipped and merged to `development` (merge commit `62808cc`), verdict
`complete-with-waivers`, tech review `approve-with-nits`. **Everything below already exists and
is your foundation.** Read it before designing anything.

`presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/`
- `RectifyWindowShell` — THE slot contract: `Column[toolbar, Row[StageSurface, PanelResizeHandle,
  SidePanelFrame], statusBar]`. `sidePanel` is a nullable slot: pass `null` and neither the panel
  nor the handle is composed. `onPanelWidthCommit` is deliberately NOT defaulted.
- `ShellToolbar` — six fixed named slots in canonical order: a leading
  `ShellWindowDefaults.TrafficLightInset` (99dp) spacer, `platformSwitch`, `deviceIndicator`,
  a weight-1 spacer, `stagedCounter`, `clearAction`, `primaryAction`.
- `ShellStatusBar` — 28dp, `leading` / `trailing` slots.
- `StageSurface` — the bezel-less mirror region, `Theme.color.stage` + a Canvas dot grid, with a
  required `content` slot. **This is where the live mirror goes.**
- `SidePanelFrame`, `ShellState`, `ShellTab`, `ShellWindowDefaults`, `clampPanelWidth`,
  `shortcutToTab`.

`compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt`
- The desktop root. Window geometry, macOS full-size-content chrome behind an `os.name` guard,
  Cmd+1/2/3 via `shortcutToTab`, theme + panel-width persistence, `AppLocaleProvider`.
- `RectifyShellContent` is Koin/AWT/coroutine-free on purpose so a render harness can drive it.
- **Every toolbar and status-bar slot is currently a STATIC STUB.** Making them live is exactly
  this spec's job: `SegmentedButtonGroup(Android|iOS)`, `DeviceChip(None)`,
  `StatusIndicator(Offline, "Waiting for device…")`.

## 2. THE defining architecture question — live mirroring

FR-4 says "connects to the selected device and begins the live mirror stream" and the PRD names
"adb + scrcpy". NFR-1 targets **glass-to-glass latency under ~150ms**. That number is what makes
drag-to-freeze feel responsive, and it is the hardest constraint in this spec.

Verified facts on this machine:
- **`scrcpy 3.3.1` IS installed** at `/opt/homebrew/bin/scrcpy`. `adb` is at `/opt/homebrew/bin/adb`.
- **The poc does NOT do live mirroring.** `/Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Devices.kt`
  (249 lines) captures single frames with `adb exec-out screencap -p`, dumps the hierarchy with
  `uiautomator`, and reads `getprop`/`wm size`/`wm density`. Useful and directly reusable for
  discovery and for the still-frame paths — but it is NOT a stream.

So the plan must choose how a live Android screen actually reaches `StageSurface`'s content slot.
Give each of these real analysis, with measured or cited latency, and say plainly what you cannot
know without a spike:
- **scrcpy as a child process in its own OS window** — trivial to launch, but it is a separate
  window, so it does not composite into the Rectify stage at all and breaks the canonical window
  (FR-11/AC-12). Probably disqualifying; say so if you agree.
- **scrcpy's server component** (`scrcpy-server.jar` pushed to the device) + consume its H.264
  socket and decode in-JVM. This is how third-party clients do it. Heaviest to build; the only
  option that plausibly meets 150ms inside our own surface.
- **`adb exec-out screenrecord --output-format=h264`** + decode. No scrcpy dependency; higher
  latency, device-side encoder quirks.
- **Screenshot polling** (`screencap` at 10-15fps, the poc's path). Simplest and already proven in
  the poc, but will NOT meet NFR-1 and will feel like a slideshow under drag.
- Anything better you find.

Also decide what a JVM H.264 decoder would even be on Compose Desktop — there is no bundled
decoder. Check what is actually on the classpath before proposing one, and treat "add a new
dependency" as a real cost that needs justifying against `.claude/rules/version-catalog-hygiene.md`.

If the honest answer is that NFR-1 cannot be met in v1 without a disproportionate build, SAY SO
and propose a staged path (e.g. ship screenshot polling behind the same interface, with the
decoder as a follow-up) rather than quietly promising 150ms.

## 3. Reference implementation — reuse, do not re-derive

`/Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Devices.kt`
- `listDevices()` parses `adb devices -l` → this is FR-3's Physical/Emulator sections.
- `captureScreenshot()` handles a real-world quirk worth keeping: some foldables prepend a warning
  line to `screencap` stdout, so it trims to the PNG signature.
- `deviceInfo()` reads `wm size` / `wm density` / `getprop` → FR-22's resolution field.
- There is an `idb` path too, for the iOS side that v1 defers.

Treat its *logic* as proven and its *structure* as a spike: it is a flat 249-line file with a
`runCommand(vararg String)` helper, no error typing, and no coroutines. This repo requires the
`Failure` hierarchy, `resultLauncher`, and the data→domain→presentation layering
(`.claude/rules/domain-layer.md`, `error-handling.md`).

## 4. Design comps — all five are cached at `raw/design/`

- `01-first-run-platform-source.png` and `03-device-picker-first-run.png` — **the device picker**,
  this spec's main new screen: a centred "Choose what to mirror" panel on `bg` (#F6F6F5, NOT the
  dotted `stage`), a `PLATFORM` section with two selectable cards showing detected tool versions
  (`adb 35.0.1 · /opt/homebrew/bin`, `idb 1.1.8 · Xcode 16.0`), a `SOURCE` section headed
  `3 found · adb devices -l` with grouped `Physical device` / `Emulator` rows carrying radio
  controls, monospace ids, and a trailing slot (`Android 15 · USB`, a green `Running` dot, or an
  outlined `Boot` button), keyboard hints `↑↓ select · ↵ start`, and a primary `Start mirroring`.
  Note the status bar in these comps reads the GREEN `adb 35.0.1 · idb 1.1.8 · ready` — that is
  FR-1 tool detection, which this spec delivers.
- `02-empty-no-device.png` — the NO SIGNAL state (FR-5/AC-4): dotted `stage`, a dashed phone
  outline, "Waiting for an Android device", two numbered steps, an inline emulator dropdown +
  `Boot`, and `adb devices · polling every 2 s`. **F-1 deliberately left the dashed outline and
  the guidance copy to THIS spec** — F-1 ships only a private preview-only placeholder.
- `04-error-adb-not-on-path.png` and `05-error-no-device-detected.png` — error cards. These look
  like `error-handling-and-recovery` (F-3) territory, but they are *device-discovery* errors and
  F-3 depends on this spec. Decide the boundary explicitly in the plan and state it; do not build
  both specs at once.

Measure against the comps rather than eyeballing. F-1's proven method: normalise the comp to the
shell's logical space and compare container landmarks — that found two real pixel defects.

## 5. Conventions and known traps

`CLAUDE.md` plus all 19 `.claude/rules/`. Specifically relevant here:
- `design-tokens.md` — no literal `Color(0x…)`, no raw `.dp` at a call site, strict
  atom → molecule → organism direction. `presentation-core-ui` has ~109 existing components;
  **browse the whole `kit/` tree before authoring anything new.** `SourceListRow` already exists
  and looks built for FR-3's picker rows.
- `package-layout.md` — `core/` internal-only, `source/` public, `explicitApi()` repo-wide.
- `testing.md` — now accurate after F-1. `kotlin.test` is wired via the Library/Feature convention
  plugins; the Stage 5 command is the four `desktopTest` tasks. **A `commonTest` backtick name
  containing a comma or apostrophe compiles on the JVM and fails `compileTestKotlinIosArm64`** —
  F-1 shipped that bug and it broke the repo-wide build. Do not repeat it.
- `error-handling.md`, `domain-layer.md`, `koin-di.md` (initKoin order is load-order significant).
- Process execution from `desktopMain` is new territory for this repo: nothing currently shells
  out. NFR-5 requires commands only ever target the user's explicitly selected device — design
  that constraint into the API, do not leave it to call-site discipline.

Known environment limit: the Kotlin daemon is capped at 3GB in `gradle.properties`; building three
KMP modules × all targets at once OOMs on this 18GB host. Build per module.

## 6. Scope

FR-1, FR-2, FR-3, FR-4, FR-5, FR-22 and AC-1, AC-2, AC-3, AC-4, AC-25, scenarios SC-1 and SC-2.
iOS is explicitly deferred to v1.1+ by FR-2 — the switch exists and shows detected `idb`, but only
Android is functional. Annotation, measurement, payload, history and MCP are later specs.
