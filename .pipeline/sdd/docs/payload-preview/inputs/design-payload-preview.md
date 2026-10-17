# Payload Preview as the design actually specifies it

Extracted from `~/Downloads/Rectify Screens.html` (unpacked from its
`__bundler/manifest` payload — it is a JS bundler page, not markup). The design
ships a working props builder (`const crop(...)` and prompt assembly logic), not a mockup, so this
is an executable specification rather than an interpretation of a picture.

**Authoritative on copy, state machine, formulas and geometry.** Where this and prose disagree,
this wins.

## Tab structure

The Payload tab is the second tab of the side panel, alongside Annotations and History.
When no annotations are staged, it shows:
```
Nothing staged. Draw a box and the payload builds here, exactly as Claude will get it.
```

When annotations are staged, it renders three sections: Prompt, Crops, Hierarchy.

## Prompt section

**Header row:**
- Label "PROMPT" (uppercase, 10.5px, JetBrains Mono, letter-spacing 0.08em, ink3)
- Token estimate right-aligned: `≈ {tokens} tokens` (10.5px, JetBrains Mono, ink3)
- "Edit" affordance (right-aligned after token estimate, 11.5px, ink2, clickable)

**Prompt block:**
- JetBrains Mono, 10.5px, line-height 16px
- Background `var(--codeBg)`, border 1px `var(--line)`
- Padding 10px, border-radius 8px
- Max-height 214px, scrollable overflow
- Prompt text is user-selectable (white-space: pre-wrap)

**Prompt assembly (from design):**
```
const pl = [
  'Fix ' + count + ' UI issue' + (count === 1 ? '' : 's') + ' on the Cart screen.',
  'Device: ' + dev.name + ' · ' + (ios ? 'iOS 18 · 1179×2556' : 'Android 15 · 1344×2992'),
  ''
];
raw.forEach((a, i) => {
  const q = mAll[i];  // measurement result
  pl.push('#' + (i + 1) + ' ' + q.node + ' · ' + q.t.w + '×' + q.t.h + unit);
  pl.push('   "' + (a.c || 'no comment') + '"');
  if (q.finding) pl.push('   measured: ' + q.finding);
  pl.push('');
});
pl.push('Attached: frame.png, ' + count + ' crops, hierarchy.xml');
prompt: pl.join('\n')
```

**Token count formula:**
```
tokens: String(360 + count * 140)
```
where `count` is the number of staged annotations. Baseline 360 tokens + 140 per annotation.

## Crops section

**Header row:**
- Label "CROPS" (uppercase, 10.5px, JetBrains Mono, letter-spacing 0.08em, ink3)
- Source label right-aligned: `from frame {frameId}` (10.5px, JetBrains Mono, ink3)

**Grid layout:**
- 3 columns, gap 8px
- Grid uses `grid-template-columns: repeat(3, minmax(0, 1fr))`

**Per-crop item:**
- Container: flex column, gap 4px
- Image area: 72px height, border-radius 6px, background `var(--stage)`, centered flex
  - Crop rect: width/height calculated by the `crop(r, bw, bh, pad, fixed, withRect)` function
  - Contains the frame image (296px wide × 640px tall, scaleDown via transform)
  - Red outline (1px, `var(--red)`) baked into the crop, positioned absolutely over the frame
- Caption: JetBrains Mono, 9.5px, ink3, single line with ellipsis
  - Format: `#{index+1} · {width}×{height}`
  - Example: `#1 · 320×640` (using actual crop dimensions from the rect, not the container)

**Crop transform (from design):**
```
const crop = (r, bw, bh, pad, fixed, withRect) => {
  const cx = Math.max(0, r.x - pad);
  const cy = Math.max(0, r.y - pad);
  const cw = Math.min(296 - cx, r.w + pad * 2);
  const ch = Math.min(640 - cy, r.h + pad * 2);
  const s = Math.min(bw / cw, bh / ch);
  return {
    bw: px(cw * s),         // container width in px
    bh: px(ch * s),         // container height in px
    ml: px(-cx * s),        // frame left margin (offsets negative position)
    mt: px(-cy * s),        // frame top margin (offsets negative position)
    tf: 'scale(' + s + ')',  // transform: scale(s)
    fixed: !!fixed,
    r: withRect ? {
      l: px((r.x - cx) * s),  // rect left relative to crop area
      t: px((r.y - cy) * s),  // rect top relative to crop area
      w: px(r.w * s),         // rect width scaled
      h: px(r.h * s)          // rect height scaled
    } : null
  };
};
```

For the Payload tab crops: `crop(a, 84, 72, 14, false, true)` where:
- `a` is the annotation rect (`{x, y, w, h}`)
- `84` is target width (px)
- `72` is target height (px, matching the container height)
- `14` is padding (px, adds 14px margin on all sides before scaling)
- `false` is fixed (no special handling)
- `true` means include the rect output for the red outline

**Dimensions note:**
All dimensions use `×` (U+00D7), not `x`. The design explicitly states this is part of the spec
(cf. measure-mode spec, formatting rules).

## Hierarchy section

**Header row:**
- Label "HIERARCHY" (uppercase, 10.5px, JetBrains Mono, letter-spacing 0.08em, ink3)
- Source label right-aligned (10.5px, JetBrains Mono, ink3)
  - Android: `uiautomator · {nodeCount} nodes` (e.g., `uiautomator · 412 nodes`)
  - iOS: `idb · {nodeCount} nodes` (e.g., `idb · 388 nodes`)

**Tree rows:**
- Flex column, JetBrains Mono, 10.5px, line-height 17px
- Per row:
  - Text (monospace, indented): node name in `pre` (preserves indentation)
  - When the node is matched/flagged: red badge on the right
    - Badge: padding 0 4px, border-radius 3px, background `var(--red)`, white text
    - Badge text: format `#{annotation+1}` when this node is referenced by an annotation

**Row coloring:**
- Matched nodes (appear in badge): ink color
- Unmatched nodes: ink3 color
- Badge always white on red background

**Source of hierarchy:**
The hierarchy is dumped once at freeze time (adb shell uiautomator dump / idb debug tree dump).
It is cached alongside the frozen frame and made available for payload assembly. See `hierSrc`
in the design, which reads one of: `uiautomator · 412 nodes` or `idb · 388 nodes`.

## Payload summary (Notes tab footer)

When the Payload tab is active, the tab footer is replaced with a payload summary visible in
both Notes and Payload tabs. This is implemented as the `payload` array in the design:

```
payload: [
  { k: 'Frame', v: ios ? '1179×2556 PNG' : '1344×2992 PNG', col: 'var(--ink2)' },
  { k: 'Hierarchy', v: phase === 'dumping' ? 'dumping…' : sent ? '412 nodes ✓' : 'on send', col: phase === 'dumping' ? WARN : sent ? OK : 'var(--ink3)' },
  { k: 'Agent', v: 'claude · ~/code/shopkit', col: 'var(--ink2)' }
]
```

**Rendering (in the footer, lines 411):**
```
<span>frame.png · {count} crops · hierarchy.xml</span>
<span>{payloadSize}</span>
```

The left side is a single text line (with ellipsis on overflow), the right side is `{size} MB`.

**Payload size formula:**
```
payloadSize: (1.6 + count * 0.12).toFixed(1) + ' MB'
```
Baseline 1.6 MB + 0.12 MB per crop. Fixed to 1 decimal place.

## Hierarchy phase state machine

The Hierarchy summary row changes colour and label based on the `phase` of the frame:

```
phase === 'dumping' ? 'dumping…' : sent ? '412 nodes ✓' : 'on send'
col: phase === 'dumping' ? WARN : sent ? OK : 'var(--ink3)'
```

- **dumping**: "dumping…", warning/orange colour, indicates hierarchy is being extracted
- **sent**: "412 nodes ✓", OK/green colour, hierarchy has been sent to the agent
- **frozen** (or other): "on send", ink3 colour, ready to send

This state machine controls the reliability promise to the user: they cannot send until the
hierarchy is available, and they see explicit confirmation once it has been sent.

## Typography and colour tokens used

| Element | Token | Notes |
|---------|-------|-------|
| Section labels (PROMPT/CROPS/HIERARCHY) | 10.5px, `JetBrains Mono`, uppercase, letter-spacing 0.08em, `ink3` | Section header styling |
| Prompt text | 10.5px, `JetBrains Mono`, line-height 16px | Main content rendering |
| Token estimate | 10.5px, `JetBrains Mono`, `ink3` | Inline estimate label |
| Edit affordance | 11.5px, `ink2` | Action text, clickable |
| Crops caption | 9.5px, `JetBrains Mono`, `ink3` | Crop identifiers |
| Hierarchy labels | 10.5px, `JetBrains Mono`, line-height 17px | Tree rendering |
| Hierarchy badges | 9.5px, white text on `var(--red)`, background `var(--red)` | Matched node markers |
| Footer text | 10.5px, `JetBrains Mono`, `ink3` | Summary copy |

**Colours:**
- `var(--ink2)`: frame/agent labels
- `var(--ink3)`: unmatched nodes, section headers, footer, captions
- `var(--ink)`: matched nodes
- `var(--codeBg)`: prompt block background
- `var(--line)`: prompt block border, crop container borders
- `var(--red)`: crop red outline, hierarchy badges, WARN state
- `var(--ok)` (green): sent confirmation
- `WARN` (orange): dumping state

All these tokens must be available in `Theme.color` and `Theme.typography`.

## Empty state

```
Nothing staged. Draw a box and the payload builds here, exactly as Claude will get it.
```

- Padding 16px 14px
- Font 12px, line-height 18px, `ink3`, text-wrap pretty
- Rendered when `count === 0`
