# data-device-impl — Claude Instructions

## Module Purpose

The implementation behind `data-device-api`'s four datasource interfaces. **This module contains
the only `ProcessBuilder` in the repository.** Nothing else in Rectify spawns an external process.

## The One Rule

`ProcessBuilder` lives in exactly one file:

```
desktopMain/kotlin/data/device/impl/core/process/ProcessRunner.kt
```

It is `internal`, and it is `desktopMain` only. `commonMain` imports no `java.*` or `javax.*`.
If you need to run something, route it through `ProcessRunner` — do not add a second spawn site.
A quick audit:

```shell
grep -rln "ProcessBuilder" --include="*.kt" . | grep -v /build/   # must print exactly one path
```

## Package Layout

```
data.device.impl/
├── core/                              — internal only
│   ├── process/ProcessRunner.kt       — THE ProcessBuilder site
│   ├── tool/ToolResolver.kt           — probes the user's path FIRST, then the ordered
│   │                                    candidate list; caches successes only, and bypasses
│   │                                    the cache entirely while an override is set
│   └── unsupported/UnsupportedDeviceSources.kt  — commonMain; the Android/iOS answer
├── di/DataDeviceImplModule.kt
└── source/
    ├── DeviceSourcesProvider.kt       — internal expect fun Module.provideDeviceSources()
    ├── datasource/{AdbHostSource,EmulatorSource,DeviceCommandExecutor}Impl.kt
    └── transport/ScreencapMirrorTransport.kt
```

Platform actuals: `desktopMain` wires the real sources; `androidMain` and `iosMain` both register
`UnsupportedDeviceSources`, which report `found = false` and throw `DeviceCommandException` on any
attempt to run something. That is how NFR-10 (macOS-only v1) holds while every target still links.
The `expect`/`actual` Koin provider mirrors `data-preference-impl`'s `provideSettings()` exactly.

## ProcessRunner — the non-obvious parts

Each of these is load-bearing; none is decoration:

- **Two concurrent drains.** stdout and stderr are read on separate coroutines. Reading them
  sequentially deadlocks the moment a large `screencap` payload meets a long stderr.
- **`withTimeoutOrNull`, never `withTimeout`.** `resultLauncher` catches `Throwable`, which
  includes `CancellationException` — a `withTimeout` here would be swallowed and re-reported as a
  `Failure`, hiding real cancellation. This is a repo-wide hazard, not a local one.
- **`destroyForcibly` in `finally`** plus a `ConcurrentHashMap` live-child registry and one
  shutdown hook, so closing the window leaves no orphans.
- **`spawnDetached` is deliberately NOT registered** with that hook. Booting an emulator is the
  user's process, not ours; closing Rectify must not kill their emulator.

## ScreencapMirrorTransport

The v1 transport: an adaptive single-flight loop. It awaits each capture before issuing the next
(so adb processes cannot stack up), trims to the PNG signature, reads width/height from the IHDR
**per frame** so device rotation is followed, counts overdue slots as dropped frames, then delays
only the remainder of `MirrorTuning.CaptureFloorMs`.

**This is a staged implementation.** It knowingly does not meet NFR-1's ~150 ms glass-to-glass
target; `MirrorTransport` exists so the real transport (scrcpy-server + in-JVM H.264 decode) can
drop in without touching the UI. The waiver is recorded and flagged for re-review before ship.

`nowMs: () -> Long` is injected (defaulting to `System::currentTimeMillis`) purely so the
dropped-frame arithmetic and the fps window are testable — wall clock cannot be moved by virtual
time. For the same reason its Koin binding is `single<MirrorTransport> { … }` and **not**
`singleOf`, which would try to resolve the clock from the graph instead of honouring the default.

## ToolResolver's user-path seam

`ToolResolver` takes a fourth seam, `userPath: suspend () -> String?`, read fresh on every
probe so a path chosen mid-session takes effect without a restart. Three properties are
load-bearing:

- **The override goes through the SAME `isExecutable` predicate as every other candidate.**
  It arrives from a file dialog, i.e. from outside the program, so this is a trust boundary —
  but it is enforced by *reuse*, not by a second check. Two validators in two places is how
  they drift apart. A path that is missing, a directory, or not executable therefore falls
  through to the ordered candidates rather than erroring.
- **It lands in `probedPaths`**, so the error card's diagnostic footer can report what was
  actually tried — "why didn't it find mine" is the question that state exists to answer.
- **The cache is bypassed in BOTH directions while an override is set.** The read guard alone
  looks sufficient; it is not. Clear the override and a stale entry resurrects the path the
  user just removed. That bug was found by a mutation that *passed*, which is what prompted
  the extra test rather than another mutation.

## Testing

24 `desktopTest` tests. Ten drive the transport through a fake `DeviceCommandExecutor` — IHDR size
read, warning-line trim, non-PNG and truncated stdout costing one frame rather than the session,
the overdue-slot arithmetic, the fps window, per-session stat reset, and a vanished device ending
the loop. All hardware-free.

**Still unverified without a device:** `ProcessRunner`'s real timeout/orphan/drain behaviour, and
any capture-rate or latency number. Those are code-review-only until someone attaches an
authorised handset.

The rest cover `AdbHostSourceImpl`'s daemon commands and `ToolResolver`'s user-path seam,
including the three rejection cases above. All hardware-free.

One of the daemon tests genuinely spawns a real child and pays both a ~3.2 s fork and a 3 s
timeout, so it takes ~6.5 s. That is deliberate: a stubbed clock catches a wrong comparison but
not a wrong constant, and a wrong constant is exactly what nearly shipped — `startServer()` was
written with `HostCommandTimeoutMs` (3 s) while its own KDoc two lines above said
`DaemonStartTimeoutMs`, i.e. the one function whose purpose is surviving a measured 3.03 s cold
fork was given the one budget that cannot. See
`.pipeline/sdd/docs/error-handling-and-recovery/execution/NEAR-MISS-t4-budget.md`.

```shell
./gradlew :data-device-impl:desktopTest
./gradlew :data-device-impl:build
```
