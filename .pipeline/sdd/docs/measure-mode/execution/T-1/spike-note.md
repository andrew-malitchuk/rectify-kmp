# T-1 spike note — three real `uiautomator dump` captures

Device: **Pixel 8** (`shiba`, serial `38031FDJH00JVF`), Android **16** / API **36**.
`adb` binary `/opt/homebrew/bin/adb`. Captured 2026-10-16, screen on and unlocked.
Command under test, unchanged for all three:
`adb exec-out uiautomator dump /dev/tty`.

All numbers below are **measured in this session**, none reconstructed. Wall clock was
taken around the `subprocess.run` of the `exec-out` call with `time.monotonic()`.

## Verdict in one line

**It works.** Three dumps came back non-empty, complete and bounds-correct. Cost is
**2.3–2.9 s**, i.e. the plan's ~2.6 s assumption is right. Two hazards the parser must
handle were found and are now pinned by fixtures.

## The three captures

| # | Fixture constant | App / screen | UI kind | Wall clock | Bytes | Nodes | non-empty `resource-id` |
|---|---|---|---|---|---|---|---|
| 1 | `VIEW_BASED_SETTINGS_DUMP` | `com.android.settings` SettingsHomepageActivity | View (`RecyclerView`, `LinearLayout`, `TextView`) | **2938 ms** | 26 645 | 69 | 57 (**83 %**) |
| 2 | `COMPOSE_APP_DUMP` | `dev.duotone.kmp.demo` (Compose Multiplatform) | Compose (`ComposeView` + 8 anonymous `android.view.View`) | **2317 ms** | 6 142 | 17 | 1 (**6 %**, and it is only `android:id/content`) |
| 3 | `MID_SCROLL_LIST_DUMP` | Settings all-apps list (`.Settings$ManageApplicationsActivity` → `SpaActivity`) | Compose list, captured **mid-drag** | **2711 ms** | 28 070 | 79 | — |

Total fixture payload 60 857 bytes; the committed `.kt` file is 65 659 bytes including KDoc
and tests. Each dump is one very long line — that is how the device emits it, and it is kept
that way.

A fourth capture (2591 ms, 27 682 bytes) was taken of the **settled** all-apps list purely to
prove #3 was really mid-scroll. It is **not** committed — it adds no shape the other three
lack.

Nothing was empty. Nothing was truncated: all three end `</hierarchy>` + trailer, and node
nesting closes cleanly. No `FLAG_SECURE` screen was attempted.

### Timing read

2317 / 2711 / 2938 ms, mean ≈ **2.66 s**, against the 2665 ms screencap baseline from
`android-device-discovery-mirroring/MEASURED-nfr-1-capture-rate.md`. Same order, essentially
the same number. Cost does **not** track dump size — the 6 KB Compose dump was fastest and
the 26 KB Settings dump slowest, but the spread is only 620 ms across a 4.5× size range, so
this is dominated by fixed `adb` + accessibility round-trip overhead, not serialisation.

This confirms the design doc's NFR-2 conclusion: **~2.7 s means you cannot dump on Option
key-down**. The hierarchy has to already be in hand at freeze time.

## Density and screen size

```
$ adb shell wm density
Physical density: 420

$ adb shell wm size
Physical size: 1080x2400
Override size:  984x2132
```

`wm density` reports **420** with **no override line**. 420/160 = **2.625 px/dp**.

## Root bounds vs real screen size — agreement verdict

All three dumps carry the identical root node:

```
class="android.widget.FrameLayout" bounds="[0,0][984,2132]"
```

- **Agrees exactly** with `wm size`'s **Override** line (984×2132).
- **Disagrees** with the **Physical** line (1080×2400) by **9.76 %**.

So this device has a display-size override active, and `uiautomator` reports the **override**
(logical) resolution. The existing `parseWmSize` in `data-device-api` already prefers the
override line — **it is already correct for this path, do not invert it.** Anyone pairing dump
bounds with the physical size would be 9.76 % wrong on every number.

Consequence for px↔dp: dump px ÷ 2.625 gives the dp the app actually laid out at
(984 px = 374.9 dp wide, 2132 px = 812.2 dp tall), which is right. But a dp here is **not** a
physical dp — the 984 px surface is upscaled onto a 1080 px panel. Reporting layout dp is
sound; claiming physical millimetres is not.

No bounds in any dump exceed the root, and none are negative.

## Hazards found — these are the actual deliverable

**1. The output is not XML.** Every dump ends with a non-XML trailer glued directly onto the
closing tag, with AOSP's own spelling mistake:

```
</hierarchy>UI hierchary dumped to: /dev/tty\n
```

Note **"hierchary"**, not "hierarchy" — matching on the correct spelling will not find it. Any
XML parser fed the raw stdout fails on trailing content. The parser must cut at the end of
`</hierarchy>` (or strip this exact suffix). Pinned by
`UIAUTOMATOR_DUMP_TRAILER` and asserted in `UiAutomatorDumpFixturesTest`.

**2. Real dumps contain inverted rectangles.** In the Settings dump, three nodes on the
partially-scrolled-off "Wallpaper & style" row have bottom **above** top:

```
[42,2083][189,2069]   h = -14   LinearLayout  com.android.settings:id/icon_frame
[84,2083][189,2069]   h = -14   ImageView     android:id/icon
[221,2080][568,2069]  h = -11   TextView      android:id/title  text="Wallpaper &amp; style"
```

`bottom - top` yields **negative sizes** on a first, ordinary dump of the default Settings
screen. This is not an exotic edge case and it will produce nonsense measurements (and
possibly negative deltas) unless clamped. Zero-area rects did not occur, but inverted ones did.

**3. `resource-id` is useless on Compose.** 83 % coverage on the View app, **6 %** on the
Compose app — and the one hit is just `android:id/content`, the Android content frame, not a
Compose element. Any node identification, labelling or sibling-matching keyed on `resource-id`
silently degrades to nothing on a Compose screen. `class` is no better: Compose nodes are
anonymous `android.view.View`. What Compose *does* give is `text` and `content-desc`.

Worth flagging for scope: **Settings' own all-apps list is Compose** (`SpaActivity`) on
Android 16. "View-based apps" is a shrinking target on a modern Pixel.

**4. Clipped rows are the normal case in a list.** Capture #3's topmost label is a row clipped
by the viewport — its `text="133 MB"` sits exactly on the scroll container's top edge
(y = 264), with its app-name sibling scrolled off above. Verified mid-drag rather than settled
by holding a 5 s `input swipe` and dumping 1.0 s in; the settled capture of the same screen
started a full row lower. Any "topmost element" logic sees a half-element.

**5. Encoding.** The Settings dump contains **U+2011 non-breaking hyphen**. Text handling must
be UTF-8 safe. Attribute values are entity-escaped (`&amp;`). The XML declaration uses
**single** quotes (`<?xml version='1.0' ...`), so matching on `<?xml version="1.0"` fails.

## Raw-string fidelity

Checked before committing: none of the three payloads contains `$`, `"""`, a backslash or a
CR. So each is a byte-faithful Kotlin raw string with **zero escaping** and no hand-editing.
`UiAutomatorDumpFixturesTest` pins each dump's exact byte size (26 645 / 6 142 / 28 070) via
`encodeToByteArray().size`, so any re-encoding or stray edit in review fails the build.

## What T-2 has to decide

1. The ~2.7 s cost is **confirmed, not merely assumed** — so the "dump once at freeze time"
   design holds, and dump-on-keypress is off the table. No replan needed on timing.
2. Density is known (420, no override), so the `px`-only fallback mitigation is **not**
   needed on this device. It is still needed as a code path for devices that report nothing.
3. **New information the plan did not anticipate:** the `"hierchary"` trailer, inverted rects
   in a vanilla dump, and `resource-id` being absent on Compose. The third is the one with
   design consequences — if any downstream task assumed `resource-id` as the node key, it
   needs to move to `text`/`content-desc`.
4. The override-vs-physical size gap means dump bounds must be compared against the override
   size. Existing `parseWmSize` already does this.
