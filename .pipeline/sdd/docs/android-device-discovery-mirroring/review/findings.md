# android-device-discovery-mirroring — findings

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | findings |
| status | done |
| producedBy | sdd-build-correctness-review |
| schemaVersion | 1 |
| createdAt | 2026-10-15T02:35:57Z |
| updatedAt | 2026-10-15T02:35:57Z |

## Gate

### type

auto

### approvedBy

sdd-build-correctness-review

### approved

true

### at

2026-10-15T02:35:57Z

## Provenance

### selfHash

10743f1720d6125f07d32a92800617f433a46a5107ec8675e8e49d94942c4c6a

### inputs

| artifact | hash |
| --- | --- |
| plan/plan.json | 685e70a97c538c5bf3ac6b5b50a06601c803863ee89f04bc6dfccfc721dd1d3e |
| validation/verdict.json | 6f2f0e1caf29e1d1f5e3b0c8c611807f08a754bda8eccaf2fd35c12a139bbacb |

## Data

### findings

| id | dimension | severity | file | line | ruleOrACRef | detail | verified |
| --- | --- | --- | --- | --- | --- | --- | --- |
| COR-1 | correctness | blocking | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 120 | .claude/rules/error-handling.md \| AC-4 \| errorState:tool-missing | The discovery collector has no `.catch`, and nothing upstream of it has one either. `DeviceRepositoryImpl.observeDiscovery` (DeviceRepositoryImpl.kt:96) calls `adbHostSource.listDevices()` inside the `while (true)` poll; that path reaches `ProcessRunner.run`, which throws `DeviceCommandException` on a non-zero `adb` exit (`could not install smartsocket listener` when port 5037 is taken) and on exceeding `MirrorTuning.HostCommandTimeoutMs` (3 s) — the first `adb devices -l` on a cold machine is what starts the adb server, so the 3 s budget is realistically exceeded on first launch. `ObserveDeviceDiscoveryUseCaseImpl` is a bare passthrough with no `resultLauncher`, so the raw data-layer exception escapes the flow, kills the `viewModelScope.launch` child uncaught, and discovery never polls again: the picker freezes on its last rows forever, no ToolMissing/NoDeviceFound state is rendered, and the only recovery is restarting the app. `observeFrames` directly below DOES guard its flow with `.catch { onSessionLost(device) }`, so the omission is asymmetric rather than deliberate. This also violates the error-handling rule's "every error is mapped to a `Failure` subtype" / "never throws past this boundary" — a `DeviceCommandException` crosses into the presentation layer unmapped. | true |
| COR-2 | correctness | major | data-device-impl/src/desktopMain/kotlin/data/device/impl/core/process/ProcessRunner.kt | 72 | ProcessRunner.kt:43 KDoc "Wall-clock budget for the whole invocation" \| NFR-5 | `withTimeoutOrNull` wraps only `process.waitFor()`; the two stream drains are awaited AFTER it (lines 72-73), outside any budget, so the documented whole-invocation budget is not what the code enforces. The failure mode this leaves open is the classic one for this exact tool: if the child hands its stdout/stderr write end to a grandchild that outlives it — `adb` auto-starting its own server is the textbook case — `inputStream.readBytes()` never reaches EOF. Then (a) `stdout.await()` never returns, so the `finally` that calls `destroyForcibly()` and removes the entry from `liveChildren` never runs (a permanent registry leak plus a child the shutdown hook will still try to kill), and (b) `runInterruptible`'s thread interrupt does not unblock a `read()` on a pipe FD, so cancelling the collector leaks a `Dispatchers.IO` thread permanently rather than freeing it. For the mirror loop the visible symptom is a session that stops producing frames with no timeout, no error and no Lost state. Moving both awaits inside the `withTimeoutOrNull` block closes it. Accepted waiver W-6 covers the absence of a test, not the absence of the bound. | true |
| COR-3 | correctness | minor | data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt | 143 | AC-45 \| AF-1 | AF-1 correctly moved the capture round-trip out of `latencyMs`, but gave it no destination. `MirrorStatsModel` has no `captureRoundTripMs` field, so the value the transport computes at ScreencapMirrorTransport.kt:98 is dropped here and is unobservable anywhere in a running app — while `MirrorTuning`'s own calibration procedure (MirrorTuning.kt:25) instructs a tuner to "read the round-trip off TransportStats.captureRoundTripMs" during a 60 s session, which the shipped app provides no way to do. `droppedFrames` has the same fate one layer later: it reaches `MirrorStatsModel` but `formatStatusLine` never renders it. Net effect: the only two numbers this transport can measure honestly are both write-only. | true |
| COR-4 | correctness | minor | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 142 | AC-25 | The stats collector is unconditional and starts at container creation, and `observeMirrorStats()` is a `combine` of two StateFlows that both have initial values — so `state.stats` becomes non-null within the first tick of app start, before any session exists. `MirrorStatusLine` therefore renders `adb · 0.0 fps · polling · latency —` permanently: at launch beside a "waiting" indicator, and again after StopMirroring/Lost, where the line keeps the last session's fps. `formatStatusLine`'s documented "empty string when there is nothing to report" branch (StatsFormatter.kt:32) is consequently dead in production despite `noStatisticsMeansNoLine` pinning it. Gating the collector (or the line) on an active session would make the stat line mean what its KDoc says. | true |
| COR-5 | correctness | minor | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 118 | AC-43 | Discovery is gated on `sessionDeviceId`, but reopening the picker (`MirrorIntent.OpenPicker`, the AC-43 toolbar chip) only flips `isPickerOpen` and does not release that gate. A user who reopens the picker mid-session is shown a device list frozen at the moment the session started: a phone plugged in or unplugged during the session never appears or disappears, and the list cannot refresh until the session is stopped — with no indication that what is on screen is stale. Either stop the session when the picker reopens, or gate discovery on `sessionDeviceId == null \|\| isPickerOpen`. | true |
| COR-6 | performance | minor | data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt | 198 | DeviceRepositoryImpl.kt:183 KDoc "asked once per attached serial" | `avdNameOf` caches only a successful name. An emulator that will not answer `emu avd name` (the console port is locked, the image is mid-boot, or it replies `KO:`) falls through the `?: return null` paths without touching `avdNames`, so it is re-asked on every 2 s poll — one extra `adb -s <serial> emu avd name` child process per such emulator per poll, forever, which contradicts the method's own KDoc. The polling test at DeviceDiscoveryPollingTest.kt:168 asserts the once-only behaviour for the success case only; the unnamed case (covered by `an emulator that will not name itself is listed under its serial`) is never checked for call count. Caching a negative result with a bounded retry, or at least backing off, would bound the churn. | true |
| COR-7 | performance | minor | data-device-impl/src/desktopMain/kotlin/data/device/impl/core/process/ProcessRunner.kt | 50 | ProcessRunner.kt:59 (the file's own `Dispatchers.IO` discipline) | `ProcessBuilder(argv).start()` is a blocking fork/exec and runs on whatever dispatcher the caller happens to be on — the same is true of `spawnDetached` (line 99) and of `ToolResolver`'s executable predicate (ToolResolver.kt:29), which stats up to ~20 candidate paths per probe inside `candidates.firstOrNull(isExecutable)` (ToolResolver.kt:53). In practice that is an Orbit intent dispatcher or the discovery collector's dispatcher, i.e. a shared `Dispatchers.Default` worker, not `Dispatchers.IO`. The file is otherwise careful to put the pipe reads and `waitFor` on `Dispatchers.IO`, so this reads as an oversight rather than a decision; wrapping `start()` and the executable scan in `runInterruptible(Dispatchers.IO)` would make the discipline uniform. | true |
| SEC-1 | security | minor | data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt | 64 | NFR-5 | `DeviceCommand.GetProp(name: String)` is the one member of the otherwise closed command set that carries unvalidated free text into an argv, and it is a `public data class` in `data-device-api` with no `of()`-style mint point — unlike `DeviceId` and `AvdName`, which exist precisely so a raw String cannot reach a command line. The consequence is worse than an extra flag: `adb shell` re-joins its arguments and hands the result to the DEVICE's shell, so `GetProp("x; <command>")` would execute `<command>` on the device. Today nothing constructs a `GetProp` outside `DeviceArgvTest`, so there is no reachable path and NFR-5 holds — but it holds by the absence of a caller, not by the type system, which is the thing NFR-5 asks for. Validating `name` the way `DeviceId` validates a serial (or dropping `GetProp` until something needs it) closes the hole before a caller appears. | true |
| MNT-1 | maintainability | minor | data-device-api/src/commonMain/kotlin/data/device/api/source/model/DeviceCommand.kt | 15 | YAGNI \| data-device-api/CLAUDE.md (closed command set) | `WmSize`, `WmDensity` and `GetProp`, their `buildDeviceArgv` branches, the whole of `WmOutputParser` (`parseWmSize`/`parseWmDensity`) and the seven tests covering them have no production caller anywhere in the repo — verified by grep: every reference outside the declarations is a test. That is speculative surface in the one module whose stated purpose is to keep the set of runnable commands minimal and auditable, and it is what makes SEC-1 possible. Delete them until a feature needs them; the parsers are two regexes that can be rewritten in minutes. | true |
| MNT-2 | maintainability | nit | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | 216 | YAGNI | `MirrorIntent.StopMirroring` has no production dispatcher — the only sender is `MirrorViewModelSessionTest.kt:167`. There is no Stop affordance in the toolbar, the picker or the stage, so a started session can only be ended by switching platform or losing the device. Either wire the affordance (the state machine and its test are already there) or drop the intent; a public contract member that only tests can reach is the kind of thing the next reader assumes is live. | true |
| MNT-3 | maintainability | nit | presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelSessionTest.kt | 199 | .claude/rules/testing.md | `arrowAndEnterIntentsCarryNoModifierKeySemantics` cannot fail: `assertSame(MirrorIntent.Commit, MirrorIntent.Commit)` is a tautology for a `data object`, and the other two lines assert the compiler-generated `equals` and the constructor parameter of a `data class`. The property it claims to guard (that a modifier flag cannot be smuggled into an intent) would be broken by ADDING a field, which this test would not notice. The other 11 tests in the file are substantive; this one is the only tautological test in the 152 and should be deleted rather than maintained. | true |
| CNV-1 | conventions | nit | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/mirror/MirrorFrameView.kt | 58 | .claude/rules/localisation.md \| A11Y | `contentDescription = "Live mirror of $deviceName"` is a hardcoded English sentence built inside the component, while every other string in the mirror kit (`DevicePickerPanel`, `NoSignalStage`) is a parameter precisely so the feature layer can pass `stringResource(...)`. It is also the only accessibility string on the live stage, so it is the one a screen reader announces and the one that will never translate. Make it a parameter with the current text as its default, the way the sibling components do. | true |
