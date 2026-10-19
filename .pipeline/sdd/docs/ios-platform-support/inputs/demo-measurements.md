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
