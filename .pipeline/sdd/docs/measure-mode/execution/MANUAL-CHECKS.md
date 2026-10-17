# measure-mode — checks only a human can perform

Every acceptance criterion in this spec that a machine can verify **is** verified:
197 tests green in `presentation-feature-mirror` on desktop and
`iosSimulatorArm64`, `:presentation-core-ui:build` green, and the whole measure
algorithm asserted against the design's own `measure()` over its real 19-node
`ELS` fixture (`inputs/design-measure-source.md`).

What remains cannot be machine-checked **in this project, on this host**:

- there is **no Compose UI-test harness** anywhere in the repo (no
  `runComposeUiTest`, no `ui-test-junit4`), and introducing one is a real
  infrastructure decision, not a casual addition;
- **this host cannot load Skiko**, so no composable can be rendered headlessly;
- the Option-hold interaction needs a live window, a pointer and a keyboard.

So the five checks below are recorded **BLOCKED, not waived**. That distinction
is this repo's own precedent (`HARDWARE-BLOCKED.md`, and the
drag-annotate-frozen-frame verdict's `W-2`): **a compiled-but-unrendered
`@Preview` is not verification.** Until someone runs these, the visual and
interaction criteria are unproven — not passed.

Each check below states what to run, what to do, what correct looks like, and
what failure looks like, so it can be executed without reading the rest of the
spec.

---

## Setup, once

```shell
cd /Users/andrew-malitchuk/StudioProjects/rectify-kmp
git checkout sdd/measure-mode
adb devices -l                      # one authorised device; measure mode is Android-only
./gradlew :desktop-application:run
```

In the app: pick the Android platform, choose your device, wait for live frames,
then **drag a box over something on screen** to freeze the frame and stage an
annotation. Measure mode only engages on a **frozen** frame with at least one
annotation — that is deliberate and gated in code (`shouldMeasure`).

The hierarchy dump starts the moment the frame freezes and takes **~2.7 s** on a
Pixel 8. The status line reads `DUMPING` while it runs.

---

## MC-1 — the dump window actually shows (AC-72)

**Do:** freeze a frame and watch the status line and its indicator dot for the
first ~3 seconds.

**Correct:** the dot and the label both say *dumping* together, then both switch
to the frozen state once the dump lands. `AC-72` exists for exactly this window.

**Failure:** the dot says one thing while the label beside it says `FROZEN` —
the status line contradicting its own indicator. (T-12 fixed one instance of
this; this check is whether any remains.) Also a failure: the window never
appears at all, meaning the dump is resolving before the UI can show it, or the
status is stuck at dumping forever.

## MC-2 — the stuck-panel risk, which `AS-21` wrongly assumed away

This is the **highest-risk check in the spec**. `onPreviewKeyEvent` needs focus,
and the stage root requested it nowhere before this spec; the implementation
hedges with a focus-independent pointer sampler plus three independent ways to
end a measure. `shouldMeasure`/`measureDispatch` are unit-tested
(`MeasureGateTest`, 14 cases including `aMissedKeyUpStillEndsThroughFocusLossOrPointerDown`),
but **whether the real key-up ever arrives is not testable here.**

**Do, in order, holding Option (⌥) over an annotation each time:**

1. Hold Option, then **Cmd-Tab away** to another app while still holding. Come back.
2. Hold Option, then **click outside the window** without releasing.
3. Hold Option, release it **outside** the app window.
4. Hold Option and **start a new drag** while holding.

**Correct:** in every case the Measuring panel disappears — via key-up, focus
loss, or the next pointer-down, whichever fires first. In case 4 nothing
measures at all: Alt is ignored until the drag ends (`FR-44` as reworded by the
operator).

**Failure:** the Measuring panel stays on screen with no Option held — a stuck
panel. Secondary failure: the `⌥` note line never gets written because
`EndMeasure` never fired.

## MC-3 — typing in a popover still works (AC-71)

The overlay root now calls `.focusable()` and requests focus. The plan flagged
that this **may perturb the comment popover's typing focus**, which is why this
check exists and why its demo was scheduled before the end of the spec.

**Do:** with a frozen frame, open a comment popover on an annotation and type a
sentence. Then measure something with Option, release, and type in the popover
again.

**Correct:** typing works normally both times, every character lands, and the
caret behaves. The popover is suppressed *while* a readout exists, and comes
back afterwards.

**Failure:** keystrokes go missing, the popover loses focus when the overlay
takes it, or the popover never reappears after measuring.

## MC-4 — the two new previews, actually rendered (AC-67)

**Do:** open
`presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt` in
Android Studio and render **both**:

- `MirrorFrozenStageMeasuringLightPreview`
- `MirrorFrozenStageMeasuringDarkPreview`

**Correct:** the Measuring card floats at the frame's **top-left** (fixed
`TopStart`, 228 dp wide); its header pairs the `⌥` keycap with "Measuring"; the
node id, the five value rows and the finding block read cleanly; and the footer
sentence *"Numbers attach to the annotation and go into the prompt."* sits
**inside the card's border**. Dashed neighbour outlines, a solid target outline,
the mid-axis guides and their numerals are all legible, in both themes.

**Failure:** the footer falls outside the border (the whole point of the additive
`footer` parameter); the card is clipped or off-frame; hairlines or the finding
wash vanish in one theme; the guides land in the wrong place.

**Known and accepted:** the card can cover an annotation in the screen's
top-left corner. The operator chose fixed `TopStart` and accepted the
occlusion; the TopEnd flip is **not** authorised. If this proves annoying in
practice it comes back as a change request, not a quiet fix.

**Also judge at token fidelity, not pixels:** padding, gap, radius and border
come from `Theme.spacing.spacingL`/`spacingS`/`cornerToken.card`/`Theme.color.line`,
which differ from the design's raw 12/10/10/`line2` because
`.claude/rules/design-tokens.md` forbids raw dp at call sites. Right tokens, not
identical numbers, is the standard here.

## MC-5 — the numbers are true on real hardware (AC-10, AC-11, AC-65)

The algorithm is verified against the design's fixture, but **whether the numbers
describe the actual phone screen** depends on the dump, the density and the
frozen frame agreeing — and that chain only exists on a device.

**Do:** measure a view whose real size you can check independently (a button, a
list row). Compare the panel's `Size` against the same element in Android
Studio's Layout Inspector, and confirm the snapped node id names the view you
actually pointed at.

**Correct:** sizes match Layout Inspector within rounding; the node id is the
view you aimed at, not its parent and not the whole screen; neighbours are the
adjacent views; and on a Compose screen the id falls back to something
meaningful (`text` or `content-desc`) rather than a bare `View[3]`.

**Failure modes worth naming:**
- **Every measure snaps to the whole screen** — the root-exclusion guard has
  regressed. `projectMeasureNodes` drops full-frame-covering nodes precisely
  because the hierarchy root otherwise always wins on area.
- **Numbers are consistently ~2.6× off** — a density or override-vs-physical
  size mismatch. The dump reports the *override* resolution (984×2132 on this
  Pixel 8) while `wm size`'s physical line says 1080×2400.
- **Everything is labelled `px`** — the density came back unknown, so the UI is
  correctly refusing to show a wrong `dp`. Honest, but check why.
- **Misalignment findings never fire** — the thresholds are being read in the
  wrong unit. This bug existed and was fixed; the tests that catch it only exist
  at 420 dpi, because at 160 dpi px and dp are identical and it is invisible.

---

## Recording the result

For each check, append the outcome to this file (pass / fail / not run, with a
date and the device). A **failed** check is a change request against the task
that owns it: MC-1 → T-10/T-12, MC-2 and MC-3 → T-11, MC-4 → T-12, MC-5 →
T-4/T-5/T-6.

Until each is recorded, its criteria stay **blocked** in
`validation/verdict.md`. They are not waived, and the spec cannot be called
complete on the strength of the automated suite alone.
