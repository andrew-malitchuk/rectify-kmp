# The design's own measure() and ELS fixture — extracted verbatim

Recovered 2026-10-17 from the cached design bundle
(`raw/rectify-screens.html`, sha256 4ecfc8eb…) by gzip-decoding its
base64 `__bundler/manifest` blobs. The 149-line `design-measure-mode.md`
is a prose extraction of this code; **this file is the code itself** and
outranks both that summary and any paraphrase of it elsewhere in the spec.

This is a **true oracle**: the design ships a working implementation, so
`measure(annotation, unit)` run over `ELS` produces output that can be
asserted against rather than interpreted. `item_price` is `w:48 h:17` with
`node: 'TextView#item_price'`, which is exactly what `MeasureCard.kt`'s
shipped @Preview renders (`48×17dp`, `top Δ6dp vs item_title · expected 0`)
— the preview was generated from this fixture.

## ELS — 19 nodes

```js
[
  { id: 'cart_title', x: 20, y: 50, w: 58, h: 31 },
  { id: 'item_image_1', x: 20, y: 110, w: 52, h: 52 },
  { id: 'item_title_1', x: 84, y: 110, w: 104, h: 17 },
  { id: 'item_meta_1', x: 84, y: 128, w: 52, h: 15 },
  { id: 'qty_1', x: 84, y: 146, w: 84, h: 24, tap: 1 },
  { id: 'item_price_1', x: 228, y: 110, w: 48, h: 17 },
  { id: 'divider_1', x: 20, y: 182, w: 256, h: 1 },
  { id: 'item_image', x: 20, y: 192, w: 52, h: 52, node: 'ImageView#item_image' },
  { id: 'item_title', x: 84, y: 192, w: 82, h: 17, node: 'TextView#item_title' },
  { id: 'item_meta', x: 84, y: 208, w: 32, h: 15 },
  { id: 'qty', x: 84, y: 226, w: 84, h: 24, tap: 1, node: 'QtyStepper#qty' },
  { id: 'item_price', x: 228, y: 198, w: 48, h: 17, node: 'TextView#item_price' },
  { id: 'divider', x: 20, y: 264, w: 256, h: 1 },
  { id: 'promo', x: 20, y: 278, w: 256, h: 40, tap: 1, node: 'TextInputLayout#promo' },
  { id: 'subtotal', x: 20, y: 340, w: 256, h: 16 },
  { id: 'shipping', x: 20, y: 364, w: 256, h: 16 },
  { id: 'total', x: 20, y: 404, w: 256, h: 19, node: 'SummaryView#totals' },
  { id: 'returns_note', x: 20, y: 440, w: 160, h: 15 },
  { id: 'checkout', x: 36, y: 566, w: 240, h: 50, tap: 1, node: 'Button#checkout' }
]```

## measure(a, u)

```js
function measure(a, u) {
  let t = ELS.filter(e => e.x >= a.x - 4 && e.y >= a.y - 4 && e.x + e.w <= a.x + a.w + 4 && e.y + e.h <= a.y + a.h + 4).sort((p, q) => q.w * q.h - p.w * p.h)[0];
  if (!t) t = { id: null, x: Math.round(a.x), y: Math.round(a.y), w: Math.round(a.w), h: Math.round(a.h) };
  const o = ELS.filter(e => e !== t && !(e.x < t.x + t.w && e.x + e.w > t.x && e.y < t.y + t.h && e.y + e.h > t.y));
  const vO = e => e.y < t.y + t.h && e.y + e.h > t.y, hO = e => e.x < t.x + t.w && e.x + e.w > t.x;
  const L = o.filter(e => vO(e) && e.x + e.w <= t.x).sort((p, q) => (q.x + q.w) - (p.x + p.w))[0];
  const R = o.filter(e => vO(e) && e.x >= t.x + t.w).sort((p, q) => p.x - q.x)[0];
  const T = o.filter(e => hO(e) && e.y + e.h <= t.y).sort((p, q) => (q.y + q.h) - (p.y + p.h))[0];
  const B = o.filter(e => hO(e) && e.y >= t.y + t.h).sort((p, q) => p.y - q.y)[0];
  const my = t.y + t.h / 2, mx = t.x + t.w / 2;
  const lx = L ? L.x + L.w : 0, rx = R ? R.x : 296, ty = T ? T.y + T.h : 0, by = B ? B.y : 640;
  const g = { left: t.x - lx, right: rx - (t.x + t.w), top: t.y - ty, bottom: by - (t.y + t.h) };
  const P = v => v + 'px', lines = [], labels = [];
  const H = (x1, x2, y, v) => { if (v < 1) return; lines.push({ l: P(x1), t: P(y), w: P(x2 - x1), h: '1px' }); labels.push({ l: P((x1 + x2) / 2), t: P(y), text: String(v) }); };
  const V = (y1, y2, x, v) => { if (v < 1) return; lines.push({ l: P(x), t: P(y1), w: '1px', h: P(y2 - y1) }); labels.push({ l: P(x), t: P((y1 + y2) / 2), text: String(v) }); };
  H(lx, t.x, my, g.left); H(t.x + t.w, rx, my, g.right); V(ty, t.y, mx, g.top); V(t.y + t.h, by, mx, g.bottom);
  let al = null, finding = null;
  if (L && L.y !== t.y && Math.abs(L.y - t.y) <= 10) {
    const d = Math.abs(L.y - t.y);
    finding = 'top Δ' + d + u + ' vs ' + L.id + ' · expected 0';
    al = { l: P(L.x), t: P(L.y), w: P(t.x + t.w - L.x), lx: P(L.x), ly: P(L.y - 16), text: 'Δ' + d + u };
  } else if (T && T.x !== t.x && Math.abs(T.x - t.x) <= 24) {
    finding = 'left Δ' + Math.abs(T.x - t.x) + u + ' vs ' + T.id + ' · expected 0';
  } else if (t.tap && t.h < 32) finding = 'height ' + t.h + u + ' · min touch 48' + u;
  const nb = [L, R, T, B].filter(Boolean).map(e => ({ l: P(e.x), t: P(e.y), w: P(e.w), h: P(Math.max(e.h, 1)) }));
  return { t, g, lines, labels, al, finding, nb, node: t.node || (t.id ? 'View#' + t.id : 'View (no match)') };
}```

## Details this settles that the prose summary did not

1. **The design's frame is 296 × 640.** `rx = R ? R.x : 296` and
   `by = B ? B.y : 640` are the right/bottom frame edges used for the
   no-neighbour gap. Any oracle test must use these, not an invented frame.
2. **Finding messages use the neighbour's `id`, not its `node` label** —
   `'top Δ' + d + u + ' vs ' + L.id + ' · expected 0'`. So the shipped preview's
   `vs item_title` is `L.id`, even though that node also carries
   `node: 'TextView#item_title'`.
3. **The touch-target branch fires on `t.tap && t.h < 32` but its message says
   `min touch 48`** — `'height ' + t.h + u + ' · min touch 48' + u`. The
   threshold and the advice are deliberately different numbers; do not
   "fix" one to match the other.
4. **Top misalignment compares tops and requires a non-zero delta**:
   `L && L.y !== t.y && Math.abs(L.y - t.y) <= 10`. Left misalignment is the
   same shape on `T.x` with a 24 bound.
5. **Guide suppression is `if (v < 1) return` on the gap value in the design's
   own single coordinate space.** The design has no density conversion at all —
   `u` is only a label suffix — so it takes no position on px-vs-dp ordering.
   This project's rule that suppression is evaluated AFTER px→dp conversion is
   therefore an **extension** for real devices, not a contradiction. Keep it.
6. **The node label chain is** `t.node || (t.id ? 'View#' + t.id : 'View (no
   match)')`. That is the design's scheme for its own fixture; this project's
   four-step `resource-id → text → content-desc → class[n]` chain is the
   equivalent for real uiautomator dumps. Only the `View (no match)` string is
   shared, and it is verbatim.
7. **Neighbour rects render with `Math.max(e.h, 1)`** — a 1px floor so the
   1px-high dividers (`divider`, `divider_1`) stay visible.
8. **The rail** is `{l: L.x, t: L.y, w: t.x + t.w - L.x}` with its label at
   `L.x, L.y - 16` reading `'Δ' + d + u`: from the left neighbour's left edge,
   at the neighbour's top, spanning to the target's right edge, labelled 16px
   above.
9. **Overlap exclusion is strict** (`<` / `>` on all four sides), so an
   edge-touching node is NOT excluded as overlapping.
