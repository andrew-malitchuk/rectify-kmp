# Codebase recon — F-10 iOS Platform Support

Read-only survey at `sdd/ios-platform-support` (off `development`, F-1..F-9 all merged).
Authoritative for "what already exists" — **do not re-derive**.

## Verdict on the PRD's central claim

The PRD says only "the device-adapter layer and the iOS error states are new". **Half true.**

- The *interfaces* are already the right shape: five datasource interfaces, one Koin provider
  function, a neutral `MirrorFrame`/`MirrorTransport` contract, and a measure/verify toolkit that
  genuinely never mentions Android.
- **But there is no platform abstraction at the repository or domain level.** `DeviceRepository`
  takes no `platform` parameter anywhere, `DeviceRepositoryImpl` reaches for `adbHostSource`
  unconditionally, each interface has exactly one unqualified Koin binding, and the argv builders
  are a closed `adb`-shaped set.

So F-10 is **"extract a dispatch seam, then add an actual"**, not "add an actual". The free part
is bigger than expected though: everything from `HierarchySnapshotModel` downward (measure, snap,
gaps, verify, diff) is already structurally neutral.

## 1. The platform switch

- `PlatformModel { Android, Ios }` — `domain-core/.../source/model/device/PlatformModel.kt`.
  `Ios` KDoc: *"iOS simulators, driven through `idb`. Not wired in this feature."*
- Selected in two places: the desktop toolbar segmented control
  (`RectifyWindow.kt:370-382`, `PLATFORM_SOURCES = listOf("Android","iOS")` at :99) and the picker
  platform cards (`MirrorContent.kt:348-362`, `platformCards()` hardcodes
  `enabled = platform == PlatformModel.Android`).
- **What happens today when iOS is picked** — `MirrorViewModel.selectPlatform()` (:946-956) nulls
  `sessionDeviceId`, releases the frozen frame and hierarchy, and reduces
  `state.copy(platform = …, session = Idle, frozen = null).withoutMeasurements()`.
  It kills the session and **starts no iOS discovery**.
- Deferral is a **stage, not a card** — `MirrorStage.IosDeferred` (`MirrorContract.kt:102-103`),
  derived at `:358` (`platform == Ios -> IosDeferred`), rendered as one
  `Text(stringResource(Res.string.mirror_ios_deferred))` (`MirrorContent.kt:131-139`).

> ⚠️ **Trap.** `MirrorStageContent` computes `val card = state.errorCard(nowMs)` **before** the
> stage `when` and renders it if non-null (`MirrorContent.kt:124-128`). `errorCard()` is
> completely platform-blind and the discovery poll keeps running `adb` while iOS is selected — so
> today, picking iOS on a machine with no Android device shows the **Android** "no device" card,
> not the iOS copy. Any iOS error state must enter through that same projection or be shadowed.

## 2. The Android device adapter — what must be generalised

| Layer | Symbol | File |
|---|---|---|
| Domain iface | `DeviceRepository` (`observeDiscovery`, `mirror`, `dumpHierarchy`, `detectTools`, `bootEmulator`, `restartAdbServer`, `setAdbPath`) | `domain-repository-api/.../repository/DeviceRepository.kt` |
| Repository | `DeviceRepositoryImpl` (+ `poll()`, `listDevicesEscalating()`, `healDaemon()`, `killThenStart()`, `PollOutcome`) | `data-repository-impl/.../source/DeviceRepositoryImpl.kt` |
| Datasource ifaces | `AdbHostSource`, `EmulatorSource`, `IdbHostSource`, `DeviceCommandExecutor`, `MirrorTransport` | `data-device-api/.../source/datasource/*.kt` |
| Desktop impls | `AdbHostSourceImpl`, `EmulatorSourceImpl`, `IdbHostSourceImpl`, `DeviceCommandExecutorImpl`, `ScreencapMirrorTransport` | `data-device-impl/src/desktopMain/.../source/{datasource,transport}/` |
| Tool location | `ToolResolver`, `probeCandidates`, `ToolKind`, `ToolProbe` | `data-device-impl/src/desktopMain/.../core/tool/ToolResolver.kt`, `data-device-api/.../source/tool/ToolProbeCandidates.kt` |
| Process | `ProcessRunner` | `data-device-impl/src/desktopMain/.../core/process/ProcessRunner.kt` |
| Argv | `buildHostArgv(toolPath, HostCommand)`, `buildDeviceArgv(toolPath, DeviceId, DeviceCommand)` | `data-device-api/.../source/parse/DeviceArgv.kt` |
| Parsers | `parseAdbDevices`, `parseAvdList`, `parseWmSize`, `trimToPngSignature`, `parseUiAutomatorDump` | `data-device-api/.../source/parse/*.kt` |
| Mappers | `DeviceResourceMapper`, `HierarchySnapshotMapper` (`HierarchyDumpStdout`), `MirrorFrameMapper`, `ToolProbeMapper` | `data-repository-impl/.../core/mapper/` |

**Interfaces exist and are clean** — `DeviceCommandExecutor.run(DeviceId, DeviceCommand): ByteArray`,
`MirrorTransport.frames(DeviceId): Flow<MirrorFrame>` + `stats: StateFlow<TransportStats>`,
`AdbHostSource.{probe,listDevices,startServer,killServer}`. `ScreencapMirrorTransport` depends only
on `DeviceCommandExecutor` — it never names `adb`.

**But `adb` is hardcoded in four load-bearing places:**
1. `buildDeviceArgv` **always** emits `[toolPath, "-s", device.value, …]` (a documented NFR-5
   security invariant). `simctl`/`idb` take `-u`/`--udid`/positional udid — it cannot express either.
2. `DeviceCommand` arguments are literal adb shell lines (`exec-out screencap -p`,
   `shell wm size`, `exec-out uiautomator dump /dev/tty`).
3. `DeviceCommandExecutorImpl` resolves `ToolKind.Adb` unconditionally; `DeviceRepositoryImpl.poll()`
   calls `adbHostSource.probe()`/`listDevices()` unconditionally and labels every failure
   `ToolKind.Adb.binaryName`.
4. Koin binds one instance per interface, no qualifier — `DeviceSourcesProvider.desktop.kt`.

**The seam:** `internal actual fun Module.provideDeviceSources()` in
`data-device-impl/src/desktopMain/.../source/DeviceSourcesProvider.desktop.kt`.
Cheapest shape that avoids touching every call site: keep the five interfaces, register the Android
impls under `named("android")` plus iOS siblings under `named("ios")`, and make
`DeviceRepositoryImpl` select by a `platform` it is **told** — which means adding
`platform: PlatformModel` to `DeviceRepository`'s methods (currently absent) and threading it from
`MirrorViewModel`, which already holds `state.platform`. Alternative with a smaller diff but worse
shape: a dispatching decorator over a `StateFlow<PlatformModel>`.

Also needs widening: `ToolKind` (no `simctl`/`xcrun` kind exists), `HostCommand` and `DeviceCommand`
— both explicitly closed sets whose KDoc says widening them must be a visible API change.

## 3. `ProcessRunner` — reusable verbatim

`internal object`, `suspend fun run(argv: List<String>, timeoutMs: Long): ByteArray` and
`fun spawnDetached(argv: List<String>)`. Knows nothing about `adb`.
Throws `DeviceCommandException(argv, exitCode, stderrTail)` on non-zero exit, on `IOException` at
start (`exitCode = null`), and on watchdog expiry (`stderrTail = "${TIMEOUT_REASON_PREFIX}${ms}ms"`);
`isTimeout()` is the discriminator. Uses `withTimeoutOrNull`, never `withTimeout`. Both pipes drained
concurrently; `finally { destroyForcibly() }`; shutdown hook over `liveChildren`.

> **The one gap for iOS:** `run()` buffers stdout to EOF. A long-lived `idb video-stream` would need
> a second entry point (`fun stream(argv): Flow<ByteArray>`). **Polling needs nothing new** —
> `xcrun simctl io <udid> screenshot --type=png -` / `idb screenshot` fit `run()` unchanged.

## 4. Error cards

- Model: `MirrorErrorKind { ToolMissing, NoDevice, PollFailed, MultipleDevices }` and
  `MirrorErrorCard(kind, command, diagnostic, rows, primaryDeviceId, canBootEmulator)` —
  `MirrorContract.kt:132-173`. Compose-free and string-free by design.
- Projection: `MirrorUiState.errorCard(nowMs)` in
  `presentation-feature-mirror/.../core/discovery/DiscoveryErrorProjection.kt`, driven entirely off
  `DiscoveryModel`. Also `indicatorDot(card)` and `dropBanner()`.
- Composable: `MirrorErrorStage(card, onIntent, modifier)` → the shipped kit organism
  `ErrorCard(title, body, kind, command, onCopyCommand, primaryActionLabel, onPrimaryAction,
  secondaryActionLabel, onSecondaryAction, diagnostic)`.
- Actions are plain lambdas dispatching intents (`LocateAdb`, `RetryDiscovery`, `RestartAdb`,
  `BootSelectedAvd`, `HighlightRow`+`Commit`); copy via `mirror_error_*` string resources.
- **The design already names your two cards:** `MirrorErrorKind`'s KDoc (:122-131) and
  `MirrorErrorStage`'s (:58-62) both record **`err-sim` and `err-trust` as drawn-but-deferred**.

**Cost:** small at the UI end, non-trivial at the state end. Free-ish: +2 `MirrorErrorKind` members,
+2 `when` branches, ~8 strings, +4 intents and handlers. **The real work:** `errorCard()` can only
project what `DiscoveryModel` carries, and `DiscoveryModel` has no simulator-not-booted or
device-not-trusted state. You need new `DiscoveryModel` variants produced by the iOS poll, **plus**
making `errorCard()` platform-aware so the Android cards stop shadowing the iOS ones.

## 5. The device picker

`DiscoveryModel.Found(physical, emulators)` → `applyDiscovery` → `List<MirrorRow>` with
`MirrorRowGroup { Physical, Emulator }` and `MirrorRowKind { Detail, Running, Boot }` →
`DevicePickerPanel(platforms, devices, …)` with `PickerPlatform(id, title, detail, toolState, enabled)`.

Platform affects the list **only** via `platformCards()`'s hardcoded `enabled` and the tools probe
line — the row list itself is not filtered by platform at all.

**Simulator-vs-physical belongs on the existing `MirrorRowGroup`**: `Physical`/`Emulator` maps 1:1,
and `MirrorRowKind.Boot` already models "cold, committing boots it" — exactly a shut-down simulator.
Only the group label is Android-worded (`mirror_group_emulator`). Reusing the pair beats a third group.

**NFR-8 is already generic and free.** `pickerKeyAction(key)` maps
`DirectionUp/DirectionDown/Enter/NumPadEnter/Escape` → `MoveUp/MoveDown/Start/Close`, wired through
one `onPreviewKeyEvent` with a `FocusRequester`; `nextSelectableIndex` does the arithmetic. Nothing
in that path knows about adb.

## 6. Hierarchy model — how far the uiautomator shape leaks

**uiautomator-specific (must change or be gated):**
`parseUiAutomatorDump` (hand-rolled scanner tolerating AOSP's malformed trailer);
`HierarchyNodeResource` (`resourceId`/`text`/`contentDesc`/`clickable` + its label chain);
`DeviceCommand.UiAutomatorDump` and its argv; `HierarchySnapshotMapper`/`HierarchyDumpStdout`;
`DeviceRepositoryImpl.dumpHierarchy` (two executor calls); `HierarchySnapshotModel.rawDump`
(KDoc: "the `uiautomator dump` stdout … VERBATIM"); `AnnotationEnvelope.hierarchyXml` (**the wire
field is literally named XML**); `PayloadLabels.kt:25` (`"uiautomator · $n nodes"`);
the stored artifact named `hierarchy.xml`.
Already gated: `attachedHierarchyXml(platform, …)` is `if (platform == Android && …)`, so **iOS
degrades to `""` for free today**.

**Already neutral (no change needed):**
`HierarchySnapshotModel(nodes, frameWidthPx, frameHeightPx, densityDpi, rawDump)` and
`HierarchyNodeModel(id, className, left, top, right, bottom, isTappable, depth)` — `className` and
`isTappable` map cleanly onto AX `type` and tappability. The whole measure toolkit (`MeasureNode`,
`projectMeasureNodes`, `snapTarget`, `neighboursOf`, `measure`) and **all of F-9's verify path**
(`RegionDiff`, `RegionPixels`, `VerifyVerdict`, `BeforeAfterFormat`, `FollowUp`) are rect- and
pixel-based and genuinely platform-neutral.

> **Net:** the risk is confined to the parse + transport + envelope edge. Produce
> `List<HierarchyNodeModel>` + root bounds + a density from `idb ui describe-all` JSON and the whole
> measure/verify stack works untouched. Decide early whether `rawDump`/`hierarchyXml` carries the AX
> JSON (rename or document) or stays `""` on iOS as it does today.
> Note `data-device-api` has **zero** dependencies beyond `:data-core` and **no serialization
> plugin** — matching the hand-rolled-parser precedent means a hand-rolled JSON reader, or adding
> `kotlinx-serialization-json` (in the catalog, unused there).

## 7. Frame capture

**Repeated `adb exec-out screencap -p`, not `screenrecord`.** `ScreencapMirrorTransport.frames()` is
an adaptive single-flight loop: capture → `trimToPngSignature` → read W/H from the PNG IHDR
(offsets 16/20) → emit → `delay(CaptureFloorMs - captureMs)`.
`MirrorTuning`: `CaptureFloorMs = 100` (~10 fps ceiling, KDoc flags it **still unmeasured**),
`CaptureTimeoutMs = 5_000`, `DumpTimeoutMs = 10_000` (measured 2.3–2.9 s),
`HostCommandTimeoutMs = 3_000`, `StatsTickMs = 1_000`.
`TransportStats(fps, latencyMs = null by design, captureRoundTripMs, droppedFrames, widthPx, heightPx)`.

**iOS contract to satisfy:** a `MirrorTransport` emitting `MirrorFrame(bytes, widthPx, heightPx,
capturedAtMs)` where `bytes` is an **encoded PNG** (the UI calls `decodeToImageBitmap()`), plus live
stats. Cancelling the collector must stop the loop and kill the child; a mid-stream
`DeviceCommandException` must propagate (that is what flips the session to `Lost`).
**Lazy path:** reuse `ScreencapMirrorTransport` verbatim by generalising only the executor
(`xcrun simctl io <udid> screenshot --type=png -`, `idb screenshot` for physical).
`idb video-stream` is higher-fps but costs the streaming-`ProcessRunner` gap **plus** H.264/MJPEG
decode that `decodeToImageBitmap` cannot do.

## 8. Everything that currently says "iOS: not supported" — each is a decision point

1. `MirrorStage.IosDeferred` (`MirrorContract.kt:102-103`)
2. the `platform == Ios -> IosDeferred` branch (`MirrorContract.kt:358`)
3. string `mirror_ios_deferred` (`strings.xml:102`)
4. `platformCards()` `enabled = platform == Android` (`MirrorContent.kt:360`) + `PickerPlatform.enabled` KDoc
5. `selectPlatform()` starts no iOS discovery (`MirrorViewModel.kt:946`)
6. `IdbHostSource` — probe-only, KDoc "that is the whole iOS surface this release"; `IdbHostSourceImpl` runs nothing
7. `ToolResolver.readVersion`: `ToolKind.Idb -> return null`; `installCommand()` → the brew tap; `envHint()` → `""`
8. `ToolKind.Idb("idb", null)` — null sdk subdir, so only Homebrew prefixes + `PATH`. **No `simctl`/`xcrun` kind exists.**
9. `detectTools()` probes idb for display only; `ToolProbeMapper` maps `Idb → PlatformModel.Ios`
10. `attachedHierarchyXml(...)` gated on Android (`PromptAssembly.kt:137`)
11. `MirrorErrorKind` + `MirrorErrorStage` KDoc: `err-sim`/`err-trust` deferred under NFR-10
12. `PlatformModel.Ios` KDoc "Not wired in this feature."
13. **Orthogonal — do not confuse:** the `iosMain` stand-ins (`UnsupportedAdbHostSource`,
    `UnsupportedMirrorTransport`, `UnsupportedAnnotationHandoff`, null `CropEncoder`, …) are for the
    app *running on* a phone, not for mirroring an iPhone. F-10 touches none of them, **but every new
    datasource interface needs a matching `Unsupported*`** so the iOS/Android graphs still resolve.

## 9. `:ios-application`

**Confirmed absent from `settings.gradle.kts`** — it is an Xcode project, not a Gradle module, so
that is correct by construction. Contains `ios-application.xcodeproj` (run-script phase
`:compose-application:embedAndSignAppleFrameworkForXcode`), `Config.xcconfig`, `iOSApp.swift`,
`ContentView.swift` wrapping `MainViewControllerKt.MainViewController()`.
**F-10 does not need it** — F-10 is a *desktop-host* capability: the Mac drives simulators through
`idb`/`simctl`. `platform-coverage.md` item 3 explicitly says don't disturb its scheme.

## Binding rules

- **platform-coverage.md** — Android + iosArm64 + iosSimulatorArm64 + `jvm("desktop")`; "Desktop" is
  the current-OS JVM only, no `macosMain`. **All `idb`/`simctl` code goes in
  `data-device-impl/src/desktopMain/`**, and that module's `commonMain` must keep naming no `java.*`.
- **error-handling.md** — throw/map in domain-core → `resultLauncher(errorMapper = Failure.Technical::Kind)`
  in every `*UseCaseImpl` → ViewModel `.onFailure { postSideEffect(ShowError) }`. There is a recorded
  known gap (`ShowError` ignores `messageId` and shows a hardcoded "Error") — **do not copy it**.
  A missing tool or empty device list is a **success value, never an exception** — the iOS cards ride
  the `MirrorErrorCard` projection, not `Failure`.
- **External-process conventions** (enforced in code/KDoc, not a rules file): one process-starting
  site; closed `DeviceCommand`/`HostCommand` sets ("adding a capability has to be a visible API
  change"); one argv construction point enforcing the device selector (NFR-5); every budget named in
  `MirrorTuning`; `TIMEOUT_REASON_PREFIX` shared producer↔consumer. **Follow all five or the
  escalation ladder's invariants break.**
- **koin-di.md** — one module per Gradle module, load order significant, `singleOf(::Impl) bind Iface::class`;
  `provideDeviceSources()` is the single registration seam. If you add qualifiers the rule prefers
  explicit `single { … get<T>() }` over `singleOf`.
- **package-layout.md** / `explicitApi()` — `core/` internal-only, `source/` public, explicit
  visibility and return types.
- **testing.md** — desktop is the target that runs, but run a cross-target build before declaring
  done; Kotlin/Native rejects backtick test names with a comma or colon. No Compose UI-test harness.
- **mappers.md** — `Mapper<I,O>` is a `fun interface`; **a mapper never throws**, unknown values get a
  safe default in `toModel`.
- **expect/actual vs `fun interface`** — the repo's consistent convention: `expect`/`actual` for the
  **DI registration function** only; plain `interface` + Koin binding for behaviour. There are no
  `expect class` datasources. **The iOS adapter is new classes bound inside the existing `desktopMain`
  actual, never a new `expect`.** Injected-default lambdas are the established test seam
  (`ToolResolver(isExecutable, identifyAdb, userPath)`, `ScreencapMirrorTransport(nowMs)`).

**Testable without a device, per existing precedent:** parsers with fixture files; `probeCandidates`
with a fake env; `ToolResolver` via its injected predicates; the transport loop against a fake
executor under virtual time; the Koin graph; the error-card projection and `applyDiscovery`.
Only `ProcessRunnerTest` spawns real processes — and `ToolResolver`'s KDoc warns that spawning inside
`runTest`'s virtual clock always times out, which is why `identifyAdb` is a parameter.
