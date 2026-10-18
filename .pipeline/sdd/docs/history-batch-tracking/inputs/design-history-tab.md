# Design reference — History tab (extracted from "Rectify Screens.html" prototype)

Source: `raw/rectify-screens.html` (bundled prototype). Decoded modules in `raw/design/`.
The History markup lives in `raw/design/6833ecee.html`; the extracted block is
`raw/design/_history-tab.html`.

## Where it lives
Right inspector panel, third segment of the existing tab strip:

```
tabs = [ ['notes','Annotations', <ann count> | null],
         ['payload','Payload', null],
         ['history','History', <in-progress count> | null] ]
```

- The History tab shows a **badge = number of batches with status `In progress`** (null when 0).
- Selected tab: `background var(--seg)`, `color var(--ink)`; unselected `transparent` / `var(--ink3)`.

## List structure
A vertically scrolling list, `padding 6px`, `gap 2px` between rows.

Rows are **grouped by time period**. A group header is emitted only on the first row of a
group: `padding 10px 8px 4px`, JetBrains Mono, `10.5px`, `letter-spacing .08em`,
UPPERCASE, `color var(--ink3)`. Prototype groups: `Today`, `Yesterday`.

## Row anatomy
`display:flex; gap:10px; padding:8px; border-radius:7px`, hover → `background var(--hover)`.

1. **Thumbnail** — 44×44 `flex:none`, `border-radius 6px`, `background var(--stage)`,
   centred. Inside it a *crop* of the frozen frame around the annotated element:
   the crop box is hairlined with `box-shadow 0 0 0 1px var(--line2)`, `border-radius 3px`,
   and the full frame (296×640 in the prototype) is scaled/offset so the element fills it.
   Crop math from the prototype (`crop(r, bw=40, bh=40, pad=18)`):
   `cx=max(0,x-pad); cy=max(0,y-pad); cw=min(W-cx,w+2pad); ch=min(H-cy,h+2pad);`
   `s=min(bw/cw, bh/ch)`; box = `cw*s × ch*s`, frame offset `-cx*s, -cy*s`, `scale(s)`.
2. **Text column** — `flex:1; min-width:0; gap:3px`
   - **title** — `12.5px/17px`, single line, ellipsised. e.g. *"Price sits 6px below the title"*
   - **meta** — JetBrains Mono `10.5px`, `var(--ink3)`: `"3 notes · 14:02 · #a41f"`
     (annotation count · time sent · short batch id)
   - **status row** — `gap:6px`, `margin-top:3px`:
     - **status chip** — `height 20px`, `padding 0 8px`, `border-radius 10px`,
       `1px solid var(--line2)`, `font 11px/500`, `color var(--ink2)`, preceded by a
       6px tone dot (`background = tone colour`).
     - optional **sub** line — JetBrains Mono `10px`, `var(--ink3)`, ellipsised
       (e.g. `editing CartScreen.kt`) — live agent activity for an in-progress batch.
     - spacer, then an optional **Verify** button — `height 22px`, `padding 0 9px`,
       `border-radius 6px`, `background var(--primary)`, `color var(--onPrimary)`,
       `11.5px/500`. Shown only when `canVerify` (status `Fixed`). Opens the verify view
       (that view belongs to F-9 verify-and-rediff-loop, NOT this spec).

## Footer
`flex:none; padding 10px 14px; border-top 1px solid var(--line)`, JetBrains Mono `10.5px`,
`var(--ink3)` — the agent/session line: `claude · ~/code/shopkit · session 7f3a`.

## Statuses in the prototype (`HIST`)
| title | meta | status | tone | extras |
| --- | --- | --- | --- | --- |
| Price sits 6px below the title | 3 notes · 14:02 · #a41f | `Fixed` | OK (green) | canVerify |
| Checkout button is 16dp off the margin | 1 note · 13:47 · #9c02 | `In progress` | WARN (amber) | sub: `editing CartScreen.kt` |
| Promo placeholder clipped at 360dp | 1 note · 12:10 · #77b1 | `Verified` | `var(--ink3)` (neutral) | — |
| Total row reads lighter than subtotal (group `Yesterday`) | 2 notes · 18:31 · #51e8 | `Follow-up sent` | RED | — |

So the status vocabulary is at least: **Sent / In progress / Fixed / Verified / Follow-up sent**,
each with a tone colour; newly sent batches enter at the top of `Today`.
