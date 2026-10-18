# Design reference — Verify & re-diff view (extracted from the "Rectify Screens.html" prototype)

Source: `raw/rectify-screens.html`; decoded modules in `raw/design/`. The verify view markup is
in `raw/design/6833ecee.html`; the extracted block is `raw/design/_verify-view.html`.

## Entry and exit
Entered from the History tab's **Verify** button (`hi.verify → setState({ view: 'verify' })`).
Exited by a **"← Back to live"** chip, absolute at `left:16px; top:16px`, `height 26px`,
`padding 0 10px`, `border-radius 6px`, `1px solid var(--line2)`, `background var(--surface)`,
`12px`, `var(--ink2)`. Its handler also resets to the live mirror and selects the History tab:
`{ view:'mirror', phase:'live', anns:[], touched:true, tab:'history' }`.

## Stage: two frames side by side
The canvas is the standard dotted stage (`background var(--stage)` +
`radial-gradient(var(--stageDot) 1px, transparent 1px)`, `background-size 16px 16px`),
`display:flex; align-items:center; justify-content:center; gap:56px`.

Two columns (`vcols`), each `flex-direction:column; gap:12px; margin-top:24px`:

| column | label |
| --- | --- |
| before | `BEFORE · FRAME 18422 · 14:02`, dot `var(--ink3)`, `fixed:false` |
| after | `AFTER · FRAME 18977 · 14:09`, dot = OK colour, `fixed:true` |

Label row: `gap 7px`, JetBrains Mono `10px`, `letter-spacing .06em`, `var(--ink2)`, preceded by
a 6px dot.

Each frame is `296×640` with `border-radius 4px` and
`box-shadow: 0 0 0 1px var(--line2), 0 20px 50px rgba(0,0,0,.16)`.

Over each frame, one **rect per annotation** (`v.rects`): `1.5px solid {col}`, `background {fill}`,
`border-radius 3px`, absolutely positioned. Each carries a numeric badge at
`left:-1.5px; top:-21px`, `min-width 18px`, `height 17px`, `padding 0 5px`, `border-radius 4px`,
`background {col}`, white text, JetBrains Mono `10px/600`.

**Colour rule:** in the BEFORE column every rect is neutral; in the AFTER column each rect is
coloured by its own verdict — OK (green) when that annotation verified, WARN/RED when it still
differs. So the per-annotation result is legible directly on the frame.

## Right panel — "Verification" (width 300px)
`background var(--bg)`, `border-left 1px solid var(--line)`.

**Header** — `height 44px`, `padding 0 14px`, `border-bottom 1px solid var(--line)`,
`space-between`: title **Verification** (`12.5px/600`) and a summary
(`vSummary = "<n> of 3 fixed"`, JetBrains Mono `10.5px`, `var(--ink2)`).

**Result rows** (`vrows`, one per annotation) — `gap 10px`, `padding 9px 8px`, `border-radius 7px`:
- an 18×18 square badge, `border-radius 4px`, `background {col}`, white JetBrains Mono `10px/600`,
  showing `✓` when verified and `!` when not;
- the annotation **comment** (`12.5px/17px`, `text-wrap:pretty`);
- the **result** line — JetBrains Mono `10.5px/15px`, `var(--ink2)`;
- the **verdict** word — `11px/500` in the row's colour: `Fixed` or `Still off`.

Prototype data (`VER`, paired with the three annotations):

| # | result | verdict |
| --- | --- | --- |
| 1 | `top Δ6dp → 0dp` | `Fixed` (OK) |
| 2 | `height 24dp → 32dp · want 48dp` | `Still off` (WARN) |
| 3 | `placeholder fits · no clipping` | `Fixed` (OK) |

Note the result strings are **measurement deltas**, not pixel percentages — they read like
`measure-mode` (F-5) output. That is the hierarchy/layout half of the diff talking.

**"AGENT CHANGES" section** — an uppercase JetBrains Mono header (`padding 14px 8px 6px`,
`10.5px`, `letter-spacing .08em`, `var(--ink3)`), then one row per file (`vfiles`):
`space-between`, `padding 3px 8px`, JetBrains Mono `10.5px` — name in `var(--ink2)`,
then `+{added}` in `var(--ok)` and `−{deleted}` in `var(--red)`.
Prototype: `CartItemView.kt +4 −2`, `QtyStepper.kt +1 −1`, `strings.xml +1 −1`.

**Footer actions** — `border-top 1px solid var(--line)`, `padding 12px 14px 14px`, `gap 6px`:
- **`Send follow-up · 1`** — primary, `height 30px`, `border-radius 7px`,
  `background var(--primary)`, `color var(--onPrimary)`, `font-weight 500`, with a `⌘↩` hint
  (JetBrains Mono `10.5px`, `opacity .6`). The count is the number of still-off annotations.
- **`Mark all as verified`** — secondary, `height 28px`, `var(--ink2)`, no fill.

## Header / toolbar while in verify view
- `stats` line: `frame 18422 ↔ 18977 · 3 regions diffed`
- `count` badge: `3` on a WARN background
- **Step strip** (`steps`), four items, the last one active (`var(--ink)` + WARN dot), the rest
  `var(--ink3)`:
  `Sent 14:02` → `Agent fixed · 3 files` → `Recaptured 14:09` → `Verified 2/3`

That strip is the clearest statement of the loop this feature implements.
