<div class="hero">

# Rectify

Mirror a connected device, drag a box over the bug, and hand Claude Code a payload it can
actually act on — the frame, per-annotation crops, the view hierarchy and a generated prompt.
Then re-capture the device and let Rectify prove, per annotation, that the fix landed.

<a class="cta" href="guide/installation/">Get started</a>
<a class="cta" href="https://github.com/andrew-malitchuk/rectify-kmp/releases/latest">Download for macOS</a>

</div>

## The loop

Every other tool in this space does one leg of it. Rectify's whole claim is the round trip.

<div class="cards">
<div class="card">
<h3><span class="dot dot-measure"></span>Mirror</h3>
<p>A live frame from an Android handset or emulator over <code>adb</code>, or an iOS simulator
over <code>xcrun simctl</code> and <code>idb</code>.</p>
</div>
<div class="card">
<h3><span class="dot dot-annotate"></span>Annotate</h3>
<p>Drag on the frame. The exact pixels freeze, and the box becomes a numbered annotation with
a comment field.</p>
</div>
<div class="card">
<h3><span class="dot dot-measure"></span>Measure</h3>
<p>Hold <kbd>⌥</kbd> and the box snaps to the matched hierarchy node, reporting dp gaps to its
neighbours.</p>
</div>
<div class="card">
<h3><span class="dot dot-warn"></span>Send</h3>
<p>One action packages frame, crops, hierarchy XML and prompt, and publishes them for the local
MCP server to serve.</p>
</div>
<div class="card">
<h3><span class="dot dot-fixed"></span>Verify</h3>
<p>Re-capture the device and diff each annotated region against the frozen original. Matches
resolve; mismatches offer a one-click re-send.</p>
</div>
</div>

## Why it exists

Spotting a visual bug on a real device is easy. Describing it precisely is not.

The usual path is a screenshot, a sentence of prose about roughly where the problem is, and the
loss of everything that made the observation useful — the exact region, the pixel or dp gap, the
view that owns it. Then, once the agent claims a fix, there is no way to check it other than
re-opening the app and eyeballing the same spot from memory.

Rectify keeps that context attached to the report, and keeps the original frame around so the
check is a diff rather than a recollection.

## What it is not

- **Not a cloud service.** Everything runs locally. There is no account, no sync and no auth.
- **Not a remote-control tool.** The mirror is read-only; Rectify never injects taps or keys.
- **Not cross-platform as an app.** Rectify itself runs on the desktop only. The devices it
  talks to are Android and iOS; see [Module map](architecture/overview/) for why that
  distinction matters when reading the source.

## Where to go next

If you want to **use** it, start with [Installation](guide/installation/) and read the Guide in
order — it follows the loop above.

If you want to **work on** it, start with the [Module map](architecture/overview/). The
architecture section assumes you have the repository open.
