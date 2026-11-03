# Device transport

How a desktop JVM ends up with a bitmap of a phone screen, and a view tree to go with it.

## One process-spawn site

`ProcessBuilder` appears in exactly one file in the entire repository:

```
data-device-impl/src/main/kotlin/data/device/impl/core/process/ProcessRunner.kt
```

It is `internal`. Everything that needs to run something routes through it. The audit is one
command, and it must print exactly one path:

```shell
grep -rln "ProcessBuilder" --include="*.kt" . | grep -v /build/
```

Four details inside it are load-bearing rather than decorative:

- **Two concurrent drains.** stdout and stderr are read on separate coroutines. Reading them
  sequentially deadlocks the moment a large `screencap` payload meets a long stderr.
- **`withTimeoutOrNull`, never `withTimeout`.** The repo-wide `resultLauncher` catches
  `Throwable`, which includes `CancellationException` — a `withTimeout` here would be swallowed
  and re-reported as an ordinary failure, hiding real cancellation.
- **`destroyForcibly` in `finally`**, plus a live-child registry and a shutdown hook, so closing
  the window leaves no orphaned `adb` processes.
- **`spawnDetached` is deliberately *not* registered** with that hook. Booting an emulator
  starts the user's process, not ours; quitting Rectify must not kill their emulator.

## Finding the tools

`ToolResolver` probes in a fixed order: the path the user chose, `$ANDROID_HOME`,
`$ANDROID_SDK_ROOT`, `~/Library/Android/sdk`, `/opt/homebrew/bin`, `/usr/local/bin`, then
`PATH`.

`PATH` alone is not enough: a GUI application launched from Finder does not inherit the shell
`PATH`, so a developer whose terminal finds `adb` perfectly well would still see "not found".

Successes are cached; the user's override is re-read on **every** probe, so changing it
mid-session takes effect without a restart.

## Parsers live in the api module

Every parser — `AdbDevicesParser`, `SimctlDevicesParser`, `IdbTargetsParser`, `AvdListParser`,
`UiAutomatorDumpParser`, `WmOutputParser`, `PngTrim` — is in `data-device-api`, not the impl.

They are pure functions over strings and bytes. Keeping them in the api module means the
discovery logic is testable with `kotlin.test` and **no device attached**, which is most of what
this feature is. Moving one into `data-device-impl` would drag the process layer onto the test
classpath and make those tests need hardware.

86 tests cover the parsers and their invariants.

Parsers never throw. Empty, truncated or otherwise unreadable input yields an empty result, and
drift is caught one layer up — `IosHostSourceImpl` raises output it did not recognise rather
than letting a silently-empty parse look like "no devices".

## The capture loop

`ScreencapMirrorTransport` is an adaptive single-flight loop:

1. Await each capture before issuing the next, so `adb` processes cannot stack up.
2. Trim to the PNG signature.
3. Read width and height from the IHDR chunk **per frame**, so device rotation is followed
   without a reconnect.
4. Count overdue slots as dropped frames.
5. Delay only the remainder of the capture floor.

The same class serves both platforms — Android and iOS differ only in which executor they were
handed.

**This is a staged implementation.** It knowingly does not meet the ~150 ms glass-to-glass
target in the requirements. `MirrorTransport` exists as an interface precisely so the real
transport (scrcpy-server with in-JVM H.264 decode) can drop in without touching the UI. The
waiver is recorded.

`nowMs: () -> Long` is injected, defaulting to `System::currentTimeMillis`, purely so the
dropped-frame arithmetic and the fps window are testable — wall-clock time cannot be moved by a
virtual-time dispatcher.

## Qualified bindings

See [Dependency injection](../di/). The short version: `DeviceCommandExecutor` and
`MirrorTransport` exist once per mirrored platform and are **always** qualified, with no
unqualified fallback, so a forgotten qualifier fails loudly.
