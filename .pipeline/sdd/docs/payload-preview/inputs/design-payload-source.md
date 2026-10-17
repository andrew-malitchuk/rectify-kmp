# Payload Preview design — provenance and ambiguities

## File locations

All extracted from `.pipeline/sdd/docs/payload-preview/raw/pages/screens.html` (cached on 2026-10-17).

| Claim | Lines | Evidence |
|-------|-------|----------|
| Tab structure, empty state text | 372–376 | `<sc-if value="{{ tabPayload }}"...` with `<sc-if value="{{ noAnns }}"...` → "Nothing staged..." |
| Prompt section: label, token estimate, Edit affordance | 378–381 | Header row with `PROMPT` label, `≈ {{ tokens }} tokens`, `Edit` link |
| Prompt block styling: mono, background, border, height, scrolling | 380 | `style="font-family:'JetBrains Mono'... white-space:pre-wrap; padding:10px; border-radius:8px; background:var(--codeBg); border:1px solid var(--line); max-height:214px; overflow:auto;"` |
| Crops section: header, 3-column grid, caption format | 382–396 | `<div style="display:grid; grid-template-columns:repeat(3, minmax(0, 1fr)); gap:8px">` with `{{ c.cap }}` captions |
| Crops caption format (exact string) | 393 | `'#'+(i+1)+' · '+D(a.w)+'×'+D(a.h)` where `D()` is dimension formatter |
| Hierarchy section: header, source label, tree rows, badges | 398–408 | `HIERARCHY` label, `{{ hierSrc }}`, `<sc-for list="{{ hier }}"...` with optional `{{ hl.mark }}` badges |
| Hierarchy badge styling | 404 | `style="padding:0 4px; border-radius:3px; background:var(--red); color:#fff; font-size:9.5px; line-height:13px">` |
| Footer: frame/crops/hierarchy, size | 411 | `frame.png · {{ count }} crops · hierarchy.xml` left-aligned, `{{ payloadSize }}` right-aligned |
| Prompt assembly algorithm | 779 | `const pl = ['Fix ' + count...` through `pl.push('Attached: frame.png, ...')`; full pseudo-code in design |
| Token count formula | 789 | `tokens: String(360 + count * 140)` |
| Payload size formula | 789 | `payloadSize: (1.6 + count * 0.12).toFixed(1) + ' MB'` |
| Crop grid helper function | 773–776 | `const crop = (r, bw, bh, pad, fixed, withRect) => {...}` with all math for scaling and positioning |
| Crop invocation for Payload tab | 790 | `crops: raw.map((a, i) => ({ ...crop(a, 84, 72, 14, false, true), cap: ... }))` |
| Hierarchy source labels | 791 | `hierSrc: ios ? 'idb · 388 nodes' : 'uiautomator · 412 nodes'` |
| Hierarchy node marking | 792 | `marks` object built from measurement results; nodes with matches render `hl.mark` badge |
| Hierarchy state machine (row 817) | 817 | `phase === 'dumping' ? 'dumping…' : sent ? '412 nodes ✓' : 'on send'` with colour codes |

## Typography

All font specifications from lines 340–411 (the Payload tab markup):

| Element | Source | Specification |
|---------|--------|---|
| Section labels | 365, 379, 383, 399 | `JetBrains Mono`, `font-size:10.5px`, `letter-spacing:0.08em`, `text-transform:uppercase`, `color:var(--ink3)` |
| Prompt mono block | 380 | `JetBrains Mono`, `font-size:10.5px`, `line-height:16px`, `white-space:pre-wrap` |
| Token estimate label | 379 | `JetBrains Mono`, `font-size:10.5px`, `color:var(--ink3)` |
| Edit affordance | 379 | `font-size:11.5px`, `color:var(--ink2)` |
| Crop caption | 393 | `JetBrains Mono`, `font-size:9.5px`, `color:var(--ink3)`, `white-space:nowrap`, `overflow:hidden`, `text-overflow:ellipsis` |
| Hierarchy tree | 400 | `JetBrains Mono`, `font-size:10.5px`, `line-height:17px` |
| Hierarchy node badge | 404 | `font-size:9.5px`, `line-height:13px`, white text on red background |
| Footer summary | 411 | `JetBrains Mono`, `font-size:10.5px`, `color:var(--ink3)` |

All use `JetBrains Mono` for machine data (annotations, dimensions, node ids, hierarchy).

## Confirmed ambiguities / design gaps

### 1. Frame ID label in crops section
Design shows `from frame 18422` (lines 383). Is this a literal ID from the frozen frame (device/
timestamp-based), or just the demo showing a made-up number? If literal, where does the ID come from?

### 2. Edit affordance on Prompt section
Design shows an "Edit" link (11.5px, ink2, right-aligned after token estimate). What does Edit do?
- Launch a prompt editor overlay (new UI)?
- Open the prompt in the crop panel for copy/paste?
- Hide the button if the prompt is locked?
- Is this a Stage 2 decision or required for Stage 4?

### 3. Red outline in crops
The crops render a `<div style="... border:1px solid var(--red)">` inside the scaled frame. The
design assumes this is the annotation rect scaled into the crop view. But crops also take a
`fixed` parameter — what does it do, and does it affect the red outline?

### 4. Hierarchy dump timing and source
Design shows `hierSrc: ios ? 'idb · 388 nodes' : 'uiautomator · 412 nodes'` but offers no
implementation path. The payload feature depends on measure-mode (F-5), which already requires
a decision on hierarchy source (see measure-mode spec, ambiguity section). Payload-preview
assumes the hierarchy is ready at freeze time; is it shared with measure-mode or fetched separately?

### 5. Measurement results in prompt
Line 780 includes `if (q.finding) pl.push('   measured: ' + q.finding);` so the prompt includes
measurement findings from measure-mode. This creates a hidden dependency: if measure-mode is not
enabled (or the user didn't hold Option), does the prompt still assemble correctly? Does the
prompt assembly fail if `mAll` is empty?

### 6. Device name and screen resolution in prompt
Line 779 pushes `'Device: ' + dev.name + ' · ' + (ios ? 'iOS 18 · 1179×2556' : 'Android 15 · 1344×2992')`.
This is hardcoded. Should it read the actual device name, OS version and actual screen resolution
from the captured frame metadata?

### 7. Prompt computation under 200ms (NFR-11)
Design shows `tokens: String(360 + count * 140)` as a simple arithmetic formula. Does this meet
the ~200ms target, or does the design expect actual tokenization (which would be much slower)?
The estimate is meant to compute "without perceptible delay" — is the heuristic formula sufficient
or does something more expensive happen?

### 8. Caption dimension format
Crop captions use `'#'+(i+1)+' · '+D(a.w)+'×'+D(a.h)` where `D()` is called on width/height.
Looking at the design, `D()` appears to be a simple formatter. What does it do? Round? Truncate?
Is it the same `D()` used in measure-mode (where it formats integer dimensions)?

### 9. Payload footer visibility
The footer (`frame.png · {count} crops · hierarchy.xml` + size) appears at line 411 in the
Payload tab. Is it also shown in the Notes tab footer when Payload tab is inactive? The design
only shows Payload tab content; Notes tab footer is not illustrated.

### 10. Colour variable names
Design uses `WARN`, `OK`, `RED` constants in the state machine (line 817). These are capitalized
in the JS design. Are these the actual `Theme.color` names, or just color references? Assume
`WARN = Theme.color.warn`, `OK = Theme.color.ok`, `RED = Theme.color.red` or does the mapping
differ?

## Source version
Design HTML cached: `4ecfc8eb64802516509584319f01bfa48a47129f952abf2cfce96cd869bfb158`
Extracted: 2026-10-17T12:40:00Z
