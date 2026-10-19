# data-device-api — Claude Instructions

## Module Purpose

Contracts, resources and **every pure parser** for Android/iOS device discovery and live
mirroring. Holds no implementation and spawns no process — `data-device-impl` does that.

## Why every parser lives here, in `commonMain`

This is the module's load-bearing decision, not an accident of layout. The parsers are pure
functions over strings and bytes, so putting them in `commonMain` makes the discovery logic
testable with `kotlin.test` and **no phone attached** — which is most of what this feature is.
Moving a parser into `data-device-impl` would drag the desktop implementation onto the test
classpath and make those tests need hardware.

86 tests in `commonTest` cover the parsers and the invariants below. They also run on
`iosSimulatorArm64Test`.

## Package Layout

```
data.device.api/
└── source/
    ├── model/        — DeviceId, AvdName, DeviceCommand, HostCommand, resources
    │                   (DeviceResource, SimulatorResource, IosTargetResource), MirrorFrame,
    │                   TransportStats, ToolKind, ToolProbe
    ├── datasource/   — DeviceCommandExecutor, AdbHostSource, EmulatorSource, IosHostSource,
    │                   MirrorTransport, DeviceSourceQualifiers (the `android` / `ios` Koin
    │                   qualifier names, public so the registrations and their one consumer
    │                   cannot spell them differently)
    ├── parse/        — AdbDevicesParser, WmOutputParser, AvdListParser, PngTrim, DeviceArgv,
    │                   UiAutomatorDumpParser, SimctlDevicesParser, IdbTargetsParser
    ├── tool/         — ToolProbeCandidates
    ├── tuning/       — MirrorTuning
    └── error/        — DeviceCommandException
```

No `core/` directory: this module has no `internal` declarations. Do not create one "for
consistency" (see `.claude/rules/package-layout.md`).

## NFR-5 Is Enforced Here, By Type

> adb/idb commands are only ever invoked against the user's explicitly selected, connected device.

That is a security property, and it holds because the API makes the alternative unrepresentable —
not because call sites are careful:

| Mechanism | What it prevents |
|---|---|
| `DeviceId` / `AvdName` are `@JvmInline value class` with **private constructors** and a validating `of()` | Constructing an id from an arbitrary string |
| `DeviceCommand` is a **closed** sealed interface (7 cases, `SimulatorBoot` added for F-10) | Expressing a command the module did not sanction |
| `HostCommand` is a separate closed set (10 cases, `XcrunVersion` / `ListSimulators` / `ListTargets` added for F-10) | Device-less commands silently acquiring a `-s` flag |
| `buildDeviceArgv(tool, toolPath, device, command)` is the **single** argv construction point, and its invariant is checked against the SELECTOR SEGMENT only — `check(selector.count { it == device.value } == 1)` | Argv assembled ad hoc somewhere else, and an id smuggled in twice |

The selector segment is per-tool and is why the check is scoped rather than whole-argv: adb
prefixes `[toolPath, "-s", id]`, simctl places the udid positionally after its subcommand
(`[xcrunPath, "simctl", <sub>, id]`), idb flags it (`[idbPath, <sub>, "--udid", id]`). The
payload is appended after the selector and is checked only against the `connect`/`-H`/`-P`
denylist. An Android-only command on an iOS tool throws rather than returning empty bytes.

**Do not add a raw-argv or raw-String escape hatch.** It would void all four at once. If a new
command is needed, add a case to the closed set.

## Parser Behaviour Worth Knowing

- `AdbDevicesParser` keeps non-`device` rows (an `unauthorized` handset must still be *shown*,
  just not selectable) and does not mistake the no-permissions help URL for a tag.
- `PngTrim.trimToPngSignature` returns `null` rather than throwing. Some devices — foldables
  especially — prepend a warning line to `screencap` stdout, so the PNG signature is not always
  at byte 0. This quirk was carried over from the proof-of-concept, where it was found the hard way.
- `WmOutputParser` prefers an `Override` size/density over `Physical` when both are present.
- `parseUiAutomatorDump` is a hand-rolled scanner, not an XML parse, because the payload is not
  XML: adb glues `UI hierchary dumped to: /dev/tty` (AOSP's own misspelling) onto `</hierarchy>`.
  It never throws — unreadable input yields an empty list, a node with unusable `bounds` is
  skipped — and it normalises inverted rects, which real dumps contain.
- `parseSimctlDevices` reads `xcrun simctl list devices available`: `-- iOS 18.5 --` runtime
  headers, rows whose udid is the SECOND-to-last parenthesised group (names contain
  parentheses — `iPhone SE (3rd generation)`). Never throws; an `(unavailable, …)` row is
  dropped. Parser DRIFT is caught one layer up, in `IosHostSourceImpl`, which raises output it
  recognised nothing in rather than reporting a silent empty bench.
- `parseIdbTargets` splits `idb list-targets` on `|` and returns **both** target types —
  `idb` lists simulators beside handsets — and decides "trusted" behind one named token set so
  that assumption can be corrected in one place. Never throws. Selecting the physical half is
  the caller's job and there is exactly one correct way to do it: the `isPhysical` predicate
  next to the parser. `trusted` is `true` for every simulator by construction, so a caller that
  skips the filter counts shut-down simulators as connected phones.
- `ToolProbeCandidates` returns an **ordered** list — `$ANDROID_HOME`, `$ANDROID_SDK_ROOT`,
  `~/Library/Android/sdk`, `/opt/homebrew/bin`, `/usr/local/bin`, then PATH. PATH alone is not
  enough: a Finder-launched `.app` inherits a minimal PATH that excludes `/opt/homebrew/bin`, so
  PATH-only detection passes every `./gradlew run` and fails on the first double-click.
  `ToolKind.sdkSubdirectory` exists because the emulator binary lives in `<sdk>/emulator`, not
  `platform-tools`.

## MirrorTuning

The calibration knob for the v1 capture loop. **`CaptureFloorMs` is still unmeasured** — its
KDoc carries the reason and the exact 5-step harness a human must run on real hardware. See the
NFR-1 waiver in `.pipeline/sdd/docs/android-device-discovery-mirroring/`.

## Build

```shell
./gradlew :data-device-api:desktopTest
./gradlew :data-device-api:compileTestKotlinIosArm64   # backtick-name guard, see testing.md
```
