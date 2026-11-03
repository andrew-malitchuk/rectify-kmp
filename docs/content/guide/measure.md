# Measure mode

A box says *this region is wrong*. Measure mode says *by how much, against what*.

## Holding Option

Hold <kbd>⌥</kbd> with the pointer over an annotation box on a frozen frame. Rectify matches the
box against the view hierarchy it dumped from the device, snaps to the node underneath, and
switches the stage into measuring:

- The matched node is outlined precisely, replacing your rough drag with the view's real bounds.
- Neighbouring views get dashed guide boxes.
- A floating **Measuring** panel reports the gaps between them, in dp and device pixels.
- Misalignment is called out in red.

Release <kbd>⌥</kbd> and the stage returns to normal.

## What gets attached

The measurement is written into that annotation's note, prefixed with the <kbd>⌥</kbd> symbol so
it is distinguishable from what you typed:

```
⌥ item_price 48×17dp
```

That line travels with the payload. The agent receives *the gap is 4dp where the sibling is 8dp*
rather than *this looks too close to the thing above it*.

## Why dp and not just pixels

A pixel measurement is only meaningful alongside the density it was taken at, and the density is
a property of the device, not of the screenshot. Rectify reads it from the hierarchy dump and
converts, so the number means the same thing on a 420 dpi handset as on a 560 dpi one — which is
the number a layout fix is actually expressed in.

## When the hierarchy is missing

Measure mode needs a hierarchy dump. If the dump failed — the device disconnected, the dump
timed out, the app under test blocks it — the status line says so, and <kbd>⌥</kbd> does
nothing rather than snapping to stale coordinates from a previous frame.

The annotation still works. You lose the measurement, not the report.

## Accuracy

The overlay measures the *painted* rectangle, not the nominal column size. The frame is
letterboxed inside whatever space the stage has, and every coordinate is derived from the
measured result of that letterboxing rather than from the design's intended dimensions.

This matters on any device whose aspect ratio is not the one the layout was mocked at — deriving
from nominal sizes puts every rectangle in the wrong place, which is a bug this project has
already had once and now guards against.
