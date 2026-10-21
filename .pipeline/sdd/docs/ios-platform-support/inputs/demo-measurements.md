# Live toolchain measurements — T-26 demo, 2026-10-19

Executed against the real installed toolchain on this host (Xcode 26.3). The host was
returned to its original state afterwards (simulator shut down, 0 booted).

## Boot a cold simulator — AC-31's simulator-not-booted half, closed for real
```
$ xcrun simctl boot 5F20FFD2-3B90-42DA-9227-26725B78EA58     # iPhone 16, iOS 18.1
exit=0, 2.79 s wall
$ xcrun simctl list devices booted | grep -c Booted
1
```
Starting state was **0 booted across all four runtimes**, which is what made the
simulator-not-booted card reproducible without staging anything.

## Screenshot to stdout — the assumption `ScreencapMirrorTransport` reuse rests on
```
$ xcrun simctl io <udid> screenshot --type=png - > shot.png
exit=0
shot.png: PNG image data, 1179 x 2556, 8-bit/color RGBA, non-interlaced   (68 641 bytes)
$ xxd -l 8 shot.png
00000000: 8950 4e47 0d0a 1a0a                      .PNG....
```
**Confirmed:** the PNG signature is at offset 0 and the IHDR carries 1179×2556, so
`trimToPngSignature` + the width/height read at offsets 16/20 work unchanged, and
`MirrorFrame(bytes, widthPx, heightPx, capturedAtMs)` is satisfiable verbatim.
`simctl` writes its status line (`Wrote screenshot to: -`) to **stderr**, not stdout, so
the byte stream is clean. This is the measurement that justifies editing
`ScreencapMirrorTransport.kt` zero times.

## The already-booted swallow — T-12's named constant, verified
```
$ xcrun simctl boot <already-booted udid>
An error was encountered processing the command (domain=com.apple.CoreSimulator.SimError, code=405):
Unable to boot device in current state: Booted
exit=149
```
The stderr token `Unable to boot device in current state: Booted` matches
`IosDeviceCommandExecutor`'s constant **exactly**. The exit code is **149** — an
unguessable value, which vindicates the decision to key on the stderr token and never on
a number. Treating this as success is correct: the user asked for a booted simulator and
has one.

## Not measured, still open
- `idb list-targets` did not report the booted simulator within a 20 s window (it costs
  5.27 s at rest and likely needs `idb_companion` for live state). The simulator path does
  not depend on it — `simctl` is the source of truth there — but the **physical-device**
  path still has no hardware evidence.
- `idb screenshot -` remains **UNMEASURED**: no trusted physical iPhone on this host.
- No GUI was driven. Clicking "Boot simulator" in a rendered error card is still
  manual-not-executed; what is proven is that the command the button dispatches works.

---

# captureRoundTripMs — measured (closes audit finding AF-4)

The plan's performance budget asked for this number "once a simulator is booted". A
simulator was booted, so here it is. Ten consecutive captures through the **exact argv the
adapter uses**, against a booted iPhone 16 (iOS 18.1):

```
608 349 336 328 305 279 294 278 277 279   (ms)
n=10  min=277  median=300  max=608  mean=333
```
The 608 ms first sample is cold-start; steady state is ~280 ms.

## What this changes

**The iOS mirror ceiling is ~3.3 fps, not 10.** `MirrorTuning.CaptureFloorMs = 100` exists
to cap Android at ~10 fps, and its own KDoc admits the figure is unmeasured. On iOS that
floor is **never the binding constraint** — the capture itself is, by a factor of three.
`ScreencapMirrorTransport`'s adaptive loop handles this correctly without modification
(it waits `CaptureFloorMs - captureMs`, which is simply always ≤ 0 here, so it never
sleeps), which is a point in favour of the reuse decision rather than against it. But any
doc or expectation that says "about 10 fps" is Android-only and should not be read as a
cross-platform claim.

**Frame weight differs enormously with content.** An idle/黑 springboard capture was
**68 641 bytes**; a booted simulator showing real UI was **3 049 017 bytes** — 44× larger.
At ~3.3 fps that is roughly **9 MB/s** moving through `ProcessRunner`'s stdout buffer and
into `decodeToImageBitmap()`. Android's `screencap` output on a 1080×2400 device is in the
same order, so this is not iOS-specific, but it is worth knowing before anyone raises the
frame rate.

## Honest limits of this measurement
Single device, single runtime, idle UI, this host only. It is a ballpark that refutes the
10 fps assumption for iOS; it is not a tuned budget. `idb screenshot` (the physical-device
path) is still **UNMEASURED** — no trusted hardware here.
