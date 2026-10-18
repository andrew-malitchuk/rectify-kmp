# T-18 — blocked on human interaction, not on hardware

The Pixel 8 (`38031FDJH00JVF`) is attached and working: discovery finds it with no
timeouts on a warm daemon, and `adb exec-out screencap -p` returns a valid PNG at
984x2132 (matching the device's Override size), 1.9 MiB, 3418 ms.

So the hardware is no longer the blocker. **The remaining blocker is that T-18's
steps are things a person does with a mouse and a cable**, and none of them can be
performed from a shell:

| step | why it cannot be automated here |
|---|---|
| Select the device and start mirroring | a click in the picker |
| Stage two annotations on the frozen frame | a **drag** on the stage — the gesture IS the feature |
| Physically unplug the USB cable | a hand on a cable |
| Edit a comment while disconnected | typing into a focused field |
| Replug and press Reconnect | a hand, then a click |

There is no Compose UI-test harness in this repo (and this spec deliberately does
not introduce one), and macOS Screen Recording permission was denied earlier in
this session, so screenshot-driven automation is not available either.

Faking it would be worse than leaving it. Killing the adb server to make the device
vanish would exercise `.catch { onSessionLost(device) }`, but only from a state I
cannot reach — there would be no frozen frame and no staged annotations, so the
exact thing T-18 verifies (that *work in progress* survives) would be untested
while the report claimed otherwise.

## What IS already proven about this path, without hardware

Every behavioural claim T-18 would confirm is covered by virtual-time tests that
drive the real `.catch` through `MirrorFakes.failStream`, which breaks an
**already-running** collection — the property that makes them non-vacuous:

| T-18 criterion | the test that proves it |
|---|---|
| annotations and frame survive the loss | `reconnectAfterInvoluntaryLossKeepsAnnotationsAndFrame`, mutation-proven |
| a comment edit while disconnected is accepted | `annotationsStayEditableWhileTheSessionIsLost`, two mutations |
| the banner reports the staged count | `streamDropBannerReportsTheStagedCount` |
| the banner appears only for Lost + frozen | `dropBannerIsNullWhileTheSessionIsStreaming` |
| Resume Live is withheld while Lost | `resumeLiveIsNotOfferedWhileTheSessionIsLost` |
| Resume Live still clears when offered | F-4's `resumeLiveClearsTheFrozenFrameAndAnnotations` |

What hardware would add is narrow but real: that a *physical* USB removal produces
the same `Lost` the fake produces, and the replug serial-change edge (USB
re-enumeration can change the serial, and FR-35 requires the banner to PERSIST
rather than silently recover). Neither is reachable from a shell.

## Honest status

**BLOCKED, not done, and not waived.** Nothing was judged acceptable here; the work
needs a person for about two minutes:

1. Launch, pick the Pixel 8, press Start mirroring
2. Drag two boxes on the stage, type a comment in one
3. Pull the USB cable — the banner should appear top-centre reading
   "Stream dropped / Your 2 annotations are kept on the frozen frame. / Reconnect",
   and Resume Live should be **gone** from the top-right
4. Edit the comment while unplugged — it should accept the edit
5. Replug, press Reconnect

Step 3 is the one that matters most: it is the NFR-6 violation this entire spec
exists to fix, and it is the one gate that **no test guards** (see
`T-16-coverage-honest.md` — mutating `if (canResumeLive)` back to `if (true)`
leaves all 231 tests green).
