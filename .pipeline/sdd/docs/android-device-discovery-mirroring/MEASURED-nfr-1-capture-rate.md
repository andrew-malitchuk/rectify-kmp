# NFR-1 capture rate: measured on real hardware

**Status:** the waiver's open question is now CLOSED by measurement.
The answer is worse than the waiver assumed, and no tuning closes the gap.

Measured 2026-10-15 on the first authorised device to reach this project.

## Step 1 — device identity (per MirrorTuning.CaptureFloorMs's own harness)

| | |
|---|---|
| Model | Pixel 8 (`shiba`), Android 16 |
| Serial | `38031FDJH00JVF` |
| Link | USB (`usb:0-0`) |
| Physical size | 1080x2400, density 420 |
| Override size | 984x2132 |

## Step 2 — capture round-trip

40 consecutive `adb exec-out screencap -p` invocations, the exact command
`ScreencapMirrorTransport` issues:

| statistic | value |
|---|---|
| mean | **2665 ms** |
| median | 2688 ms |
| p95 | 2983 ms |
| min / max | 2096 ms / 3194 ms |
| payload | 1869 KiB per frame |
| achieved rate | **0.4 fps** |

Second run, 12 captures each, isolating on-device PNG encoding:

| command | mean | p95 | payload | rate |
|---|---|---|---|---|
| `screencap -p` (PNG, what we ship) | 2814 ms | 2937 ms | 1.8 MiB | 0.4 fps |
| `screencap` (raw RGBA) | **1166 ms** | 1198 ms | 8.0 MiB | 0.9 fps |

So on-device PNG encoding costs roughly 1.65 s per frame, and dropping it —
despite moving 4.4x more bytes over the wire — is 2.4x faster.

## What this settles

1. **NFR-1 (~150 ms glass-to-glass) is unreachable by this transport.** Not
   marginally: the mean frame cost is ~18x the whole latency budget, and the
   best case after removing PNG entirely is still ~8x it. The existing waiver
   called this "does not meet"; the measurement makes it "cannot meet".

2. **`CaptureFloorMs = 100` is inert.** The floor only delays the remainder of
   the interval after a capture returns. Captures take 1166–2814 ms, so the
   floor never binds and its value is irrelevant — it is not a tuning knob on
   this hardware, it is dead configuration. Step 5 of its own harness ("set this
   floor just above the measured mean round-trip") would imply ~2800 ms, which
   would be an honest number but an absurd one to ship as a *floor*.

3. **`CaptureTimeoutMs = 5000` is tighter than it looks.** p95 is 2983 ms and
   max 3194 ms on a flagship over USB on an idle machine. A slower device, a
   busier host or a USB 2 hub plausibly crosses 5 s, and that path reports a
   dropped frame.

4. **Switching to raw `screencap` is a real, measured 2.4x win** available
   without changing the architecture — but it buys 0.4 -> 0.9 fps, which does
   not change the verdict, only the misery.

## Recommendation

Stop treating the polling transport as tunable. `MirrorTransport` was
deliberately introduced as a seam so scrcpy-server + in-JVM H.264 decode could
replace it without touching the UI (`data-device-impl/CLAUDE.md`); this
measurement is the trigger condition for actually doing that.

Until then the honest framing for the product is "frozen-frame annotation with
a slow refresh", not "live mirror". That wording question belongs to a human,
not to this pipeline — it changes what the product claims to be.
