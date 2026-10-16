# T-2 — Operator go/no-go gate on the T-1 spike

Recorded 2026-10-17 by the orchestrator at the Stage 4 wave-2 pause, from the
operator's answers at the gate. Evidence: `execution/T-1/spike-note.md` and
`execution/T-1/report.md` (task T-1, outcome PASS).

## Decision: **GO — continue as planned**

The operator chose **continue as planned**. The two descope exits the task was
required to present were NOT taken:

| candidate exit | taken? |
|---|---|
| Continue as planned (freeze-time `exec-out uiautomator dump /dev/tty`) | **YES** |
| Add a `/sdcard` dump + `cat`/`pull` fallback command | no |
| Descope FR-36..FR-40, ship the FR-41 fallback only | no |
| Stop the spec | no |

### Why the spike supports GO

- **The command works on real hardware.** Three dumps from a Pixel 8 (`shiba`,
  API 36) came back non-empty, complete and bounds-correct. No `FLAG_SECURE`
  screen was attempted, so that remains unmeasured.
- **The timing assumption is confirmed, not merely assumed.** 2317 / 2711 /
  2938 ms, mean ~2.66 s, against the plan's 2665 ms baseline. Cost is dominated
  by fixed `adb` + accessibility round-trip overhead, not serialisation (a 4.5x
  size range moved the number only 620 ms). NFR-2 therefore holds **only**
  because the dump happens at freeze time; dump-on-Option-key-down stays off
  the table.
- **Density is known** — `wm density` = 420, no override, 2.625 px/dp. The
  plan's "unknown density => label everything px" mitigation is not needed on
  this device, but stays as a code path for devices that report nothing.
- **Dump bounds pair with the OVERRIDE screen size, not the physical one.**
  All three roots are `[0,0][984,2132]`, matching `wm size`'s override line
  exactly and disagreeing with the physical 1080x2400 by 9.76%. The existing
  `parseWmSize` already prefers the override line -- **do not invert it**.
  Reporting layout dp is sound; claiming physical millimetres is not.

## Four findings the plan did not anticipate, and the rulings on them

### 1. The output is not XML — ruling: cut at `</hierarchy>`

Every dump ends with a non-XML trailer glued onto the closing tag, carrying
AOSP's own misspelling:

```
</hierarchy>UI hierchary dumped to: /dev/tty\n
```

`hierchary`, not `hierarchy` — matching the correct spelling will not find it.
Any XML parser fed raw stdout fails on trailing content. Pinned by
`UIAUTOMATOR_DUMP_TRAILER` and asserted in `UiAutomatorDumpFixturesTest`.

Two further parse hazards from the same note: the XML declaration uses **single
quotes** (`<?xml version='1.0'`), so matching `version="1.0"` fails; and the
Settings dump contains **U+2011 non-breaking hyphen**, so text handling must be
UTF-8 safe. Attribute values are entity-escaped (`&amp;`).

### 2. Inverted rectangles in a vanilla dump — ruling: **normalise on parse**

Three nodes on the partially-scrolled-off "Wallpaper & style" row have bottom
above top (e.g. `[42,2083][189,2069]`, height **-14**). This is a first,
ordinary dump of the default Settings screen, not an exotic edge case.

**Operator ruling: normalise on parse.** Coordinates are swapped so every rect
leaves the parser with non-negative extents, in one place, before any geometry
sees it. `snapTarget`, `neighboursOf`, `gapsOf` and `findingOf` therefore keep
their positive-extent assumption and need no defensive guard each. The rejected
alternatives were dropping inverted nodes (silently removes real views from
snap candidates and neighbour selection) and making each pure function
defensive (same guard in five places, inviting one to miss it).

### 3. `resource-id` is useless on Compose — ruling: **text -> content-desc -> class+index**

83% non-empty `resource-id` on the View app; **6%** on the Compose app, and
that single hit is `android:id/content`, the Android content frame, not a
Compose element. `class` is no better: Compose nodes are anonymous
`android.view.View`. What Compose does populate is `text` and `content-desc`.

Note also: **Settings' own all-apps list is Compose** (`SpaActivity`) on
Android 16. "View-based apps" is a shrinking target on a modern Pixel.

**Operator ruling: a four-step fallback chain for the node identifier** --
`resource-id`, then the node's own `text`, then `content-desc`, then
class + `[n]` collision index as the last resort.

The operator first chose class+index, then revised to this chain once the note's
detail surfaced that Compose classes are anonymous too, which would have made
the Measuring panel's headline a bare `View[3]` on exactly the apps this tool
targets. The `[n]` collision index from the plan's earlier ratification is
retained as the tail of the chain, not replaced.

**This supersedes the plan's node-id format** for Stage 5 reconciliation
purposes: any downstream task that assumed `resource-id` as the sole node key
is now wrong and must implement the chain. Affects the node-id derivation in
T-4 (`HierarchyNodeResource`) and its formatting in T-7 (`MeasureFormat`).

### 4. Clipped rows are the normal case in a list — no ruling needed

Capture #3's topmost label is a row clipped by the viewport, its app-name
sibling scrolled off above. Any "topmost element" logic sees a half-element.
No design change: the snap algorithm is containment-based and does not have
topmost-element logic. Recorded so Stage 5 does not treat it as a surprise.

## Downstream effect

Every task from T-3 onward is unblocked. Two task definitions are affected by
the rulings above and the dispatcher carries them as explicit context, rather
than amending the human-gated immutable backlog mid-execution:

- **T-4** (`parseUiAutomatorDump` + `HierarchyNodeResource`) -- cut the trailer;
  tolerate a single-quoted XML declaration; be UTF-8 safe; normalise inverted
  rects; derive the node id through the four-step chain.
- **T-7** (`MeasureFormat`) -- format the node id produced by that chain,
  including the `[n]` collision index.

The spike was **PASS**, so the DoD clause about a BLOCKED spike ("the spec
cannot close with that unaddressed") does not apply.
