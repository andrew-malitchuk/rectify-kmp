# The design's error states, banner and scrim — extracted from the real design file

Source: `~/Downloads/Rectify Screens.html`, unpacked from its `__bundler/manifest`
payload. The screens file defines `ERRORS`, `STATUS`, `DEVS` and the stream-drop
banner directly. **This is authoritative on copy and geometry** — it is the
design, not an inference from it.

Two of my own earlier statements were WRONG and are corrected below.

## Error card anatomy (one card, five states)

Card: `width:460px`, `padding:24px`, `border-radius:14px`, `background:surface`,
`1px solid line`, plus `shadow`. Composed on the dot-grid stage, not replacing it.

Rows, in order:
1. Header row, `gap:14px`, align flex-start — a 28px circular glyph
   (`background: toneBg`, `color: tone`, weight 700, 15px… actually 14px) then a
   column with a 15px/600/-0.01em title and an `ink2` body at 19px line-height.
2. Optional command block: `padding:9px 10px 9px 12px`, `radius:8px`,
   `background:codeBg`, `1px solid line`; a `$` in `ink3` mono 11.5px, the
   command in mono 11.5px, and a 22px `Copy` button with a `line2` border.
3. Optional device list (the multi-device state).
4. Actions row, **`padding-left:42px`** — primary is `height:30px`,
   `padding:0 14px`, `radius:7px`, `background:primary`/`onPrimary`, weight 500;
   secondary is the same height, `padding:0 12px`, `color:ink2`, no fill.
5. Footer: `padding:10px 0 0 42px`, `border-top:1px solid line`, mono 10.5px
   `ink3`.

The 42px left inset on the actions row and footer aligns them under the title
column (28px glyph + 14px gap), not under the glyph. Easy to miss.

## The five states, verbatim

| key | tone/glyph | title | primary | secondary | footer |
|---|---|---|---|---|---|
| `err-adb` | WARN `!` | adb isn't on your PATH | **Locate adb…** | Retry | `which adb → not found · $ANDROID_HOME unset` |
| `err-nodevice` | WARN `!` | No Android device detected | **Restart adb** | Boot emulator instead | `adb 35.0.1 · last poll 0.8 s ago · 0 attached` |
| `err-multi` | ink2 `?` | 3 devices connected | **Mirror Pixel 8 Pro** | — | `adb devices -l · 3 attached` |
| `err-sim` | WARN `!` | iPhone 15 Pro simulator isn't booted | Boot simulator | Choose another | `simctl · 8DE3…A41F · Shutdown` |
| `err-trust` | WARN `!` | Trust this Mac on the iPhone | I've tapped Trust — retry | Use simulator | `idb_companion · 00008110-001A… · pairing: untrusted` |

Bodies:
- `err-adb`: "Rectify uses Android platform-tools to find devices. Install them, or point Rectify at an adb you already have." — cmd `brew install --cask android-platform-tools`
- `err-nodevice`: "adb is running but sees nothing. On the phone, turn on USB debugging and tap Allow on the prompt." — cmd `adb kill-server && adb devices`
- `err-multi`: "Pick one to mirror. Rectify remembers it until you quit."

`err-sim` and `err-trust` are iOS and therefore **out of scope** here (NFR-10,
macOS/Android v1) — but they are the reason the card has five states, so do not
"simplify" the component to three.

### Three independent confirmations of decisions already taken

1. **`err-multi`'s primary is `Mirror Pixel 8 Pro`** — the top row, as a
   one-click shortcut, with every row listed and selectable. That is binding
   decision #3 / FR-34 exactly, arrived at independently.
2. **`err-nodevice`'s own command is `adb kill-server && adb devices`.** The
   design reaches for kill-server for this state. Independent support for
   minting `HostCommand.KillServer` — now justified three ways: copy fidelity
   (the judge), the measured wedged-daemon finding, and the design itself.
3. **`err-adb` is primary `Locate adb…` + secondary `Retry`** — matching
   binding decision #5 and plan.json's FR-37 mapping.

## Status-bar line per state (currently unplanned)

```
err-adb       WARN  "adb not found in PATH"
err-nodevice  WARN  "adb devices → 0 attached"
err-multi     WARN  "3 devices attached — waiting for choice"
```

plan.json routes the card but says nothing about the status line changing with
it. The design pairs them. Worth a task; low cost, and without it the status bar
keeps claiming "Waiting for device…" while a card says adb is missing.

## CORRECTION 1 — AC-29's banner copy: the design matches preparation, not the deviation

The design's banner, verbatim:

> ● **Stream dropped**  Your {count} annotations are kept on the frozen frame.  [Reconnect]

Geometry: absolutely positioned `top:14px`, `left:50%`, `translateX(-50%)`,
`z-index:20`; `padding:8px 8px 8px 14px`, `radius:10px`, `background:surface`,
`1px solid line2`, `shadow`, `white-space:nowrap`; a 7px red dot, the label at
weight 500, the detail in `ink2`, then a 26px `primary`-filled Reconnect at
`padding:0 12px`, 12px/500.

**This contradicts plan.json's deviation #5**, which shortened the copy to
`"%d annotations kept"` / `"1 annotation kept"` and dropped the detail line.
The design has a single detail sentence and no singular variant. Since the
design is authoritative on copy and `presentation-core-ui/CLAUDE.md` requires
copy to agree with behaviour, **deviation #5 should be reverted** to the
design's wording — which is also what preparation's AC-29 said literally.

Note the banner is `surface`, not an error fill: only the 7px dot carries the
red. Same restraint as `StatusPill`.

## CORRECTION 2 — the scrim exists, and SCRIM_ALPHA was right

I previously recorded "SCRIM_ALPHA — the design has no scrim at all". **That was
wrong.** The design dims the frame:

```js
fill: 'rgba(12,12,14,0.42)'   // masked: annotation rects punch through
```

- **`0.42` matches our invented `SCRIM_ALPHA` exactly.** Confirmed, not guessed.
- **The colour does not match.** Ours is `Theme.color.pop.copy(alpha = …)`, so
  it is `#18191C` (24,25,28) in light and `#2C2D31` (44,45,49) in dark. The
  design's is a fixed `rgb(12,12,14)` in both themes. In dark ours is
  materially weaker than intended.
- **Theme-following is wrong here for the same reason the popover is always
  dark:** the scrim sits over mirrored device UI of unknown brightness. It
  should be a fixed near-black, like the design's.
- Mechanism matches and ours is cheaper: the design masks an SVG rect, we use
  `clipPath(ClipOp.Difference)` over one `drawRect`. Keep ours.

### A behavioural difference worth a decision

The design gates the scrim on `raw.length > 0 && (phase === 'frozen' ||
'dumping' || 'failed')` — **with zero annotations it does not dim at all.**
`AnnotationOverlay` instead dims the whole frame when `dimmed` is true and
`marks` is empty (`if (marks.isEmpty()) drawRect(scrim)`).

Also note `phase === 'failed'` is in that list: **the design dims on stream
drop**, which is squarely FR-24's state. So the banner and the scrim appear
together, and the zero-annotation case is exactly the one plan.json's deviation
#4 was hedging about. The design answers it: no annotations, no scrim, no count.

## CORRECTION 3 — `err-multi`'s body promises persistence that does not exist

The decompose agent flagged this back and judged it "copy only, already true via
`sessionDeviceId` persisting for the app's lifetime". **That is wrong**, verified
against the code.

Design copy for `err-multi`:

> Pick one to mirror. **Rectify remembers it until you quit.**

`MirrorViewModel.sessionDeviceId` is a plain `MutableStateFlow<String?>` (line
79) with no persistence behind it, and **three separate paths null it**:

| line | path | when |
|---|---|---|
| 184 | `onSessionLost` | the device drops — i.e. exactly the F-3 scenario |
| 190 | `selectPlatform` | the user switches Android/iOS |
| 246 | `stopSession` | the session is stopped |

`data-preference-api` holds no device-selection key at all (grepped: no
`deviceId` / `lastDevice` / `selectedDevice`).

So the selection survives until the **first session drop**, not "until you
quit". On a machine where the cable is flaky — the very situation this spec
exists for — the promise fails immediately and the user is returned to the
picker with nothing pre-selected.

### Why this is not a nit

`presentation-core-ui/CLAUDE.md` carries this exact precedent under "Picker copy
must agree with picker behaviour": comp 02's `Mirroring starts on its own as
soon as adb sees one` was **deliberately not shipped** because it promised an
auto-start the build does not implement, and the module's rule is that the copy
and the behaviour are a pair — changing one without the other makes the app lie
about itself. This is the same class of defect, found the same way.

### Three options, in cost order

1. **Change the copy** (cheapest, no behaviour change): "Pick one to mirror."
   full stop, or "Rectify keeps using it for this session." Honest about what
   `sessionDeviceId` actually does.
2. **Preserve across reconnect only** (small): have `onSessionLost` keep the id
   — it already carries it in `MirrorSession.Lost(deviceId)`, which is where
   FR-35's Reconnect reads it from — and re-select it on a successful reconnect.
   This makes the copy true for the drop/reconnect cycle but still not across
   a platform switch or an explicit stop.
3. **Persist a preference** (largest, new scope): a `pref_last_device_id` in
   `data-preference-api` + restore on launch. This is what the design's words
   literally mean, and it is **new scope not in any FR** — it would need its own
   requirement rather than being smuggled in as a copy string.

**Recommended: option 1**, with option 2 folded into FR-35's work if it falls
out for free. Option 3 is a product decision and a separate spec; shipping the
design's sentence without it would ship a lie, which is the one thing the
module rule forbids.
