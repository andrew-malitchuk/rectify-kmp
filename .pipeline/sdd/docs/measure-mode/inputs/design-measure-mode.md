# Measure mode as the design actually specifies it

Extracted from `~/Downloads/Rectify Screens.html` (unpacked from its
`__bundler/manifest` payload — it is a JS bundler page, not markup). The design
ships a working `measure(annotation, unit)` implementation, not a mockup, so this
is an executable specification rather than an interpretation of a picture.

**Authoritative on algorithm, copy and geometry.** Where this and prose disagree,
this wins.

## The algorithm, step by step

### 1. Node snap — largest fully-contained node wins

```js
let t = ELS.filter(e => e.x >= a.x - 4 && e.y >= a.y - 4 &&
                        e.x + e.w <= a.x + a.w + 4 && e.y + e.h <= a.y + a.h + 4)
           .sort((p, q) => q.w * q.h - p.w * p.h)[0];
```

- Candidates are nodes **fully contained** in the annotation rect, with a **4 px
  slack on every edge** (so a drag that misses by a pixel still snaps).
- Ties break by **largest area first** — `q.w*q.h - p.w*p.h`, descending. Not
  nearest-centre, not first-found. A drag around a row snaps to the row, not to the
  first label inside it.
- **No match is a first-class outcome, not an error.** It falls back to the
  annotation's own rounded rect and reports node `"View (no match)"`. AC-11's
  "degrade clearly when no node matches" is this, and the fallback still produces
  full measurements — the user is told the numbers are for their box, not a view.

### 2. Neighbour selection — nearest non-overlapping node per side

Overlapping nodes are excluded entirely (`!(overlaps)`), then per side:

| side | filter | sort | picks |
|---|---|---|---|
| L | vertically overlaps target, `e.x+e.w <= t.x` | `(q.x+q.w) - (p.x+p.w)` | right-most of those left of it |
| R | vertically overlaps target, `e.x >= t.x+t.w` | `p.x - q.x` | left-most of those right of it |
| T | horizontally overlaps target, `e.y+e.h <= t.y` | `(q.y+q.h) - (p.y+p.h)` | lowest of those above it |
| B | horizontally overlaps target, `e.y >= t.y+t.h` | `p.y - q.y` | highest of those below it |

"Vertically overlaps" is `e.y < t.y+t.h && e.y+e.h > t.y` — a strict band test, so a
node merely diagonal to the target is not a neighbour on either axis.

### 3. Gaps — measured to the neighbour, or to the frame edge

```js
lx = L ? L.x + L.w : 0        rx = R ? R.x : FRAME_W
ty = T ? T.y + T.h : 0        by = B ? B.y : FRAME_H
g = { left: t.x - lx, right: rx - (t.x+t.w), top: t.y - ty, bottom: by - (t.y+t.h) }
```

With no neighbour on a side the gap is measured **to the frame edge**, not omitted.

### 4. Guides — suppressed below 1 unit

Both `H()` and `V()` begin `if (v < 1) return;`. A sub-unit gap draws **no line and
no numeral** — it is not rounded up and not drawn as zero. Guides are 1 px, laid on
the target's mid-axes (`my = t.y + t.h/2`, `mx = t.x + t.w/2`), with the numeral
centred on the span.

### 5. Findings — a three-branch `if/else if/else if`, first match wins

This is the red-highlighted line in the panel, and the ORDER is part of the spec:

1. **Top misalignment** — `L && L.y !== t.y && |L.y - t.y| <= 10`
   → `` `top Δ{d}{unit} vs {L.id} · expected 0` ``
   Also draws an alignment rail from the left neighbour across to the target's right
   edge, labelled `Δ{d}{unit}` 16 px above it.
2. **Left misalignment** — `T && T.x !== t.x && |T.x - t.x| <= 24`
   → `` `left Δ{d}{unit} vs {T.id} · expected 0` ``
3. **Touch target** — `t.tap && t.h < 32`
   → `` `height {h}{unit} · min touch 48{unit}` ``

Note the asymmetry, and keep it: the vertical tolerance is **10**, the horizontal
**24**. Wider horizontally because text of differing lengths legitimately starts at
the same x far more often than it shares a y. And `expected 0` is literal — the
design asserts the intended value, not just the delta.

Only **one** finding is ever shown. A box that is both misaligned and too small
reports the misalignment.

## Panel copy and geometry

Floating card, `position:absolute; left:16px; top:16px; z-index:20`, `width:228px`,
`padding:12px`, `radius:10px`, `background:surface`, `1px solid line2`, `shadow`,
`gap:10px` column.

Header row: an 18 px `⌥` keycap (`1px solid line2`, radius 4, 11 px `ink2`), the word
**Measuring** at weight 600, then the annotation index right-aligned in mono 10.5 px
`ink3`.

Then the node id in mono 11 px `var(--measure)` — the blue, not ink.

Then the rows, `gap:4px`, each `k` in 12 px `ink2` left and `v` in mono 11 px right:

```
Size    {w}×{h}{unit}
Top     {top}{unit}
Right   {right}{unit}
Bottom  {bottom}{unit}
Left    {left}{unit}
```

`Size` uses **`×` (U+00D7)**, not `x`. The UI-kit page states the rule explicitly:
`1344×2992` is right, `1344x2992` and `1344 X 2992` are wrong.

The finding, when present: `padding:7px 8px`, `radius:6px`,
`background:oklch(0.63 0.22 27 / 0.1)`, text `var(--red)`, mono 10.5/15.

Footer, 11 px `ink3`: **"Numbers attach to the annotation and go into the prompt."**
That sentence is the whole justification for FR-10 — it tells the user why measuring
is not just an inspector.

## Formatting rules, from the UI-kit page's own table

| field | right | wrong |
|---|---|---|
| Dimensions | `1344×2992` | `1344x2992`, `1344 X 2992` |
| Position | `(1035, 862)` | size first, then `@ x,y` |
| Delta | `top Δ6dp · expected 0` | `6 px off` |
| Units | `24dp · 17pt · 1344 px` | — |

Delta format is `Δ` + value + unit with **no space before the unit**. The note
appended to the annotation is prefixed `⌥` — e.g. `⌥ top Δ6dp vs item_title`.

## Typography tokens this needs

The UI kit names two mono styles that exist for exactly this feature:

- `mono-tag` — 9.5/13, 500, `#fff` on signal — "Measure labels, hierarchy tags",
  specimen `Δ6dp`
- `mono-ruler` — 8.5/12, 400, `ink3` — "**Ruler numerals only**", specimen `1000`

Check whether `Theme.typography` already carries both before adding anything.

## What this tells us about NFR-2 (~100 ms)

The algorithm is a handful of linear scans over the node list with no allocation per
frame and no I/O — trivially inside 100 ms for a typical hierarchy. **The cost is not
here; it is in getting the hierarchy at all.** `uiautomator dump` on a real device is
the expensive step, and measured capture cost on the attached Pixel 8 was already
2665 ms for a screenshot alone (see `android-device-discovery-mirroring/
MEASURED-nfr-1-capture-rate.md`).

So NFR-2 is satisfiable **only if the hierarchy is already in hand** when Option goes
down — i.e. dumped once at freeze time, not on key-down. Any design that dumps on
key-press will miss ~100 ms by an order of magnitude. This needs to be an explicit
decision in Stage 1, not discovered in Stage 4.
