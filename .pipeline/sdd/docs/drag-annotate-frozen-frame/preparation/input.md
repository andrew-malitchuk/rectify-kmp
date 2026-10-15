# drag-annotate-frozen-frame — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-15T02:53:59Z |
| updatedAt | 2026-10-15T02:53:59Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-15T02:53:59Z

## Provenance

### selfHash

f83b181d145a7e671e057d359355542bb30abad99893618af14f1c410b3d6664

### inputs

| artifact | hash |
| --- | --- |
| inputs/raw/prd-handoff.json | f8eb3fd130ed8590a707f7449ca5b4d1fb95b4aceba941a135d2c25da61fc3d7 |

## Data

### title

Drag-to-Annotate & Frozen Frame

### goalStatement

The core capture interaction: click-and-drag on the live mirror renders a dashed red outline with a live device-pixel size label and ruler/tick guides; release freezes the exact current frame and stages a numbered annotation anchored to the box. On the frozen frame everything outside annotation boxes dims and each annotation gets a popover (numbered badge, autofocused 'What's wrong here?' comment field, delete control, size/position meta line) with Enter to save and delete to remove and decrement the staged count. When popovers would crowd or overlap, they relocate to a gutter/sidebar panel linked to their boxes by leader lines. Why: this is the interaction that replaces 'screenshot plus prose description' and is the single most differentiating surface of the product.

### rawDescription

The core capture interaction: click-and-drag on the live mirror renders a dashed red outline with a live device-pixel size label and ruler/tick guides; release freezes the exact current frame and stages a numbered annotation anchored to the box. On the frozen frame everything outside annotation boxes dims and each annotation gets a popover (numbered badge, autofocused 'What's wrong here?' comment field, delete control, size/position meta line) with Enter to save and delete to remove and decrement the staged count. When popovers would crowd or overlap, they relocate to a gutter/sidebar panel linked to their boxes by leader lines. Why: this is the interaction that replaces 'screenshot plus prose description' and is the single most differentiating surface of the product.

### sources

| type | ref | summary |
| --- | --- | --- |
| text | inputs/raw/prd-handoff.json | Upstream PRD + UI/UX handoff: FR-6/FR-7/FR-8/FR-9 (drag to dashed outline + freeze frame + popover UI + gutter relocation); NFR-1/NFR-8/NFR-9 (latency, keyboard navigation, light/dark parity); AC-5/AC-6/AC-7/AC-8/AC-9; SC-3/SC-4. Depends on F-1 (shell) and F-2 (live mirror). |
| text | inputs/operator-dictation.md | Operator guidance: F-1 and F-2 are merged and green (160 desktop tests); much UI already exists (AnnotationMark, AnnotationPopover, AnnotationRow); POC solved the drag gesture including the density trap documented at line 124; five design comps are cached but none show annotations (discovery screens only); the dashed outline, size label, ruler/tick guides, dim treatment, badges, popover layout and gutter/leader lines are entirely unillustrated and require design conversation. |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/AnnotationMark.kt | AnnotationState enum (Drawing, Pinned, Selected, Frozen, Sent, StillOff); accentColor() resolver; mark body with dashed/solid border, fill alpha, corner handles, numbered badge, size label. |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationPopover.kt | Gutter popover with index badge and text; always dark (floats over unknown-brightness device content); max width 280 dp; placement and leader-line routing deferred to caller. |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/AnnotationRow.kt | Sidebar annotation row: index badge, comment (or 'No comment yet'), meta line (position), optional finding (measure blue); selection state highlighting. |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/StageSurface.kt | Bezel-less mirror region with faint dot grid (16 dp cells, 0.75 dp dots, 0.45 alpha); content slot for stage content (frame or empty state). |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/mirror/MirrorFrameView.kt | Stateless aspect-fit frame renderer: takes ImageBitmap, device name; FilterQuality.None; aspect-fit background under device content so dot grid is hidden precisely where the frame lands. |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | Mirror Orbit ViewModel. Pixels published on frame: StateFlow<ImageBitmap?> outside Orbit state (determinism: ByteArray compares by identity, identity copy per frame would be 10x/second state copies). Stats throttled 1s. Discovery and capture loop gated by sessionDeviceId flatMapLatest. |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | MirrorUiState, MirrorIntent, MirrorSideEffect contracts; MirrorSession (Idle/Streaming/Lost); MirrorStage (Picker/Mirror/NoSignal/IosDeferred); MirrorRow picker row model. |
| code | /Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Main.kt | POC drag proof: detectDragGestures with dragStart/dragEnd as Offset? (line ~116-120, ~203); PendingAnnotation(start, end) rect serialised as 'x1,y1,x2,y2' in device screenshot pixels (line ~65); density trap documented line ~124: without density correction rectangle lands density× from cursor (2× on Retina). toDisplayDp() conversion shown line 126. |
| text | raw/design/01-first-run-platform-source.png | Device-discovery comp: platform toggle (Android/iOS), tool version/path detail. Not an annotation screen. |
| text | raw/design/02-empty-no-device.png | Device-discovery comp: passive waiting stage with dashed phone outline and 'NO SIGNAL' label. Not an annotation screen. |
| text | raw/design/03-device-picker-first-run.png | Device-discovery comp: picker with device list, selectable rows, Boot/Start actions. Not an annotation screen. |
| text | raw/design/04-error-adb-not-on-path.png | Device-discovery comp: error card (adb not found). Not an annotation screen. |
| text | raw/design/05-error-no-device-detected.png | Device-discovery comp: error card (no device). Not an annotation screen. |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/AnnotationMark.kt | AnnotationMark | Renders a mark with state-driven color, dashed/solid border, badge, size label. Drawing state uses dash pattern floatArrayOf(6f, 4f). Reading from all six AnnotationState values. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/AnnotationMark.kt | AnnotationState | Enum: Drawing, Pinned, Selected, Frozen, Sent, StillOff. Frozen renders opaque plate instead of translucent wash. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/AnnotationMark.kt | accentColor | Maps state to Theme.color: Drawing/Pinned/Selected/Frozen→annotate (red), Sent→sentFixed, StillOff→warnStillOff. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationPopover.kt | AnnotationPopover | Always-dark row popover: optional index badge + text + max width 280 dp. No placement logic (caller's responsibility). |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/AnnotationRow.kt | AnnotationRow | Sidebar list row: index badge (red), comment or 'No comment yet' (muted), meta line, optional finding. Selection state background. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/StageSurface.kt | StageSurface | Bezel-less stage: background=Theme.color.stage, Canvas dot grid (16 dp cells, 0.75 dp radius, 0.45 alpha over ink3), content slot on top. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/mirror/MirrorFrameView.kt | MirrorFrameView | Dumb stateless frame renderer: ImageBitmap + device name. Aspect-fit with matchHeightConstraintsFirst, FilterQuality.None, opaque background under frame only. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | MirrorViewModel.frame | StateFlow<ImageBitmap?> published outside Orbit state: up to 10 frames/second, bytes compare by identity, so state-inside would force full copy per frame. Stats throttled to 1s ticks. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | MirrorViewModel.sessionDeviceId | MutableStateFlow<String?> gates both discovery and capture loop via flatMapLatest: one adb session at a time, polling pauses while mirroring. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | MirrorUiState | Orbit state: platform, tools, discovery, rows, picker open flag, highlighted index, selected device, session state, stats, AVDs. No frame bytes. |
| rectify-poc | app/src/main/kotlin/rectify/Main.kt | PendingAnnotation | Line ~93: data class(start: Offset, end: Offset) + note mutableState. Rect serialized as 'x1,y1,x2,y2' in device screenshot pixels. |
| rectify-poc | app/src/main/kotlin/rectify/Main.kt | detectDragGestures | Line ~116-120, ~203: dragStart/dragEnd as Offset? state. Density trap at line ~124: 'without correcting for density the rectangle lands density× further from the cursor' (2× on Retina). |
| rectify-poc | app/src/main/kotlin/rectify/Main.kt | toDisplayDp | Line 126: lambda (this / density).dp. Converts raw screen pixels to dp by dividing out density, so Compose's dp→px multiplication restores device pixel coordinates correctly. |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| 01-first-run-platform-source | N/A (cached PNG only; source URL is authenticated) | raw/design/01-first-run-platform-source.png | Platform toggle + tool detail. Device-discovery screen, not annotation UI. |
| 02-empty-no-device | N/A (cached PNG only; source URL is authenticated) | raw/design/02-empty-no-device.png | Waiting stage with dashed phone outline and 'NO SIGNAL'. Device-discovery screen, not annotation UI. |
| 03-device-picker-first-run | N/A (cached PNG only; source URL is authenticated) | raw/design/03-device-picker-first-run.png | Device picker with selectable rows. Device-discovery screen, not annotation UI. |
| 04-error-adb-not-on-path | N/A (cached PNG only; source URL is authenticated) | raw/design/04-error-adb-not-on-path.png | Error card (adb not found). Device-discovery screen, not annotation UI. |
| 05-error-no-device-detected | N/A (cached PNG only; source URL is authenticated) | raw/design/05-error-no-device-detected.png | Error card (no device). Device-discovery screen, not annotation UI. |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| external | Rectify POC (rectify-poc/app/src/main/kotlin/rectify/Main.kt): drag gesture with detectDragGestures, start/end offsets, PendingAnnotation rect stored as 'x1,y1,x2,y2' in device pixels, density trap documented (rectangle lands density× from cursor without correction; 2× on Retina). Solution proven but needs coordinate-space discipline and formal integration. | file:///Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Main.kt#L93,L116,L124,L126 |

### glossary

| term | definition |
| --- | --- |
| Device screenshot pixels | The coordinate space the Android device reports (e.g. 1080×2400). This is the space annotations must be stored in for reproducibility across density changes. |
| Composable dp | Compose's density-independent pixel unit. A 100.dp on a 2.0x Retina becomes 200 physical pixels. Within the Compose layout system, all coordinates and sizes use dp. |
| Host physical pixels | The display hardware's actual pixels. On a Retina display this is 2× the nominal resolution (scale=2.0); raw pointer events and canvas drawing APIs report coordinates in this space. |
| Density | The multiplier from dp to physical pixels. On a 2.0x Retina display, density=2.0. LocalDensity.current.density provides this value. The density trap: Compose's dp→px multiplies by density, so a raw pointer event already in physical pixels must be divided by density before being passed to .offset(dp), or the rectangle lands 2× too far from the cursor. |
| Dashed outline | The visible rectangle being drawn during drag. AnnotationMark with state=Drawing renders a dashed red border (dash pattern: 6 px on, 4 px off per POC line 45). |
| Ruler/tick guides | Horizontal and vertical pixel-measurement rulers shown during drag, allowing the user to read exact coordinates and dimensions. Not yet designed or specified visually. |
| Frozen frame | The device screenshot captured at the moment the user releases the drag. The live capture loop pauses and the stage switches to show this static frame with annotations overlaid. Design question: is this the frame visible at drag start or at release? |
| Staged annotation | An annotation that is committed locally but not yet sent to the device team. It carries a position, size, and comment (initially empty, autofocused for editing). Staged annotations are held in memory and can be deleted before send. |
| Popover | A small dark card (AnnotationPopover) floating on or near an annotation mark, containing the index badge, comment field, and delete control. When multiple popovers would overlap, they relocate to a sidebar gutter. |
| Gutter/sidebar | A vertical panel (possibly the Notes tab repurposed, or a new annotation-specific sidebar) where annotation popovers relocate when they would crowd the frame. Leader lines connect each popover to its mark. |
| Dim treatment | When a frame is frozen, everything outside annotation boxes is darkened, so the annotated regions stand out. The AnnotationMark in Frozen state renders an opaque surface instead of translucent wash, allowing the dim to work correctly. |
| Size label | A live measurement (e.g. '545 × 254') displayed below or adjacent to the annotation mark. During drag it updates as the user drags. FR-6 specifies this must be in device pixels, not dp or physical pixels. |
| Numbered badge | A small square badge (e.g. '1', '2', '3') shown on the top-left corner of each annotation mark, identifying it in the list. GlyphBadge component with Theme.color.annotate (red) background. |

### attachments


### ambiguities

- NO VISUAL REFERENCE EXISTS FOR THIS FEATURE. All five cached design comps (raw/design/*.png) are device-discovery screens. The dashed red outline, the live device-pixel size label, the ruler/tick guides, the dim-outside-boxes treatment, the numbered badge, the popover layout, the gutter/sidebar, and the leader-line routing are entirely unillustrated. The design source URL (https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html) is authenticated and cannot be fetched. Design decisions for all visual elements must be made or clarified before or during Clarify step.
- THREE COORDINATE SPACES ARE IN PLAY: (1) Device screenshot pixels — the storage space (what the device reports, e.g. 1080×2400); (2) Composable dp — Compose's layout space; (3) Host physical pixels — display hardware coordinates (2.0× on a Retina display). FR-6 demands a device-pixel size label. The POC documents that without correcting for density the rectangle lands density× from the cursor (2× on Retina, line 124 of rectify-poc/Main.kt). Pointer events arrive in physical pixels; Modifier.offset/size take dp. An off-by-density error here silently corrupts every annotation's anchor. Coordinate conversion must be explicit, testable, and verified.
- FRAME FREEZE TIMING AMBIGUITY (FR-7): The dictation says 'freeze the exact current frame', but the capture loop is still running when drag starts. Is the 'exact' frame the one visible at drag start, or the one visible at release? The two may differ if the device app updates mid-drag. This affects whether the user's comment is anchored to what they saw or what ended up on screen.
- STAGED ANNOTATION DISPLAY LOCATION: F-1 shipped a Notes tab as an empty slot and the toolbar's stagedCounter/clearAction as static stubs. Whether this spec fills the Notes tab with staged annotations (so they render there + on the frozen frame) or leaves that to F-6 (payload-preview) must be decided explicitly. Not drifting into it.
- FR-9 GUTTER/LEADER LAYOUT RULES: When popovers relocate to a sidebar, how are they positioned relative to their marks? The style guide (mentioned in AnnotationPopover KDoc) staggers elbows by 8 px, but the full algorithm (vertical order, horizontal offset, leader-line attachment points) is unspecified. Design conversations will be needed.
- FROZEN FRAME UI: Once the frame is frozen, what screen state replaces the live mirror content? Does it show the device name/model? Does it have a button to resume live streaming or start a new capture? Does the NotesTab activate automatically or require a click? These are unspecified.
- KEYBOARD NAVIGATION (NFR-8): The spec requires 'fully keyboard-navigable' with 'arrow keys/Enter for pickers, Enter/Escape for popovers'. The Enter/Escape behaviour for popover fields (save on Enter, dismiss on Escape) is clear; does the focus order move between annotations arrow-key style or require Tab? When multiple popovers are in a gutter, what is the focus order?
- COORDINATE CONVERSION TESTING: The POC demonstrates the conversion but does not ship tests. This spec must establish a concrete test (even if just a demo or assertion) that verifies: (a) a drag on Retina computes rect bounds correctly, (b) rect serialized as 'x1,y1,x2,y2' matches the visual overlay, (c) the POC's toDisplayDp() or equivalent is used and the density trap is solved. Without a test, the fix is invisible and vulnerable to reversion.

### dictation

```
# Operator dictation — drag-annotate-frozen-frame (F-4)

Autonomous run. Gates are closed by the orchestrator taking the recommended option; the rationale
is recorded in each artifact's `survey[]` with `source: ai-proposed` (no human chose them).

## 1. F-1 and F-2 are DONE and merged — build on them

Both shipped to `development` with verdict `complete-with-waivers` and review `approve-with-nits`.
**160 desktop tests are green. Keep them green.**

**F-1 gave you the shell** (`presentation-core-ui/.../kit/organism/shell/`):
`RectifyWindowShell` (slot contract), `ShellToolbar` (six named slots incl. `stagedCounter`,
`clearAction`, `primaryAction`), `ShellStatusBar`, `StageSurface` (the bezel-less mirror region
with a required `content` slot — **this is where the drag happens**), `SidePanelFrame` with the
three tabs Notes/Payload/History, `ShellState`.

**F-2 gave you the live mirror**:
- `presentation-feature-mirror` — Orbit MVI. `MirrorViewModel` publishes decoded pixels on a
  `StateFlow<ImageBitmap?>` held OUTSIDE the Orbit state (deliberately: a bitmap reduced into
  state 10x/second would be a full state copy per frame, and every annotation feature built on it
  would inherit that cost — **do not undo this**). Stats on a separate 1s ticker.
- `MirrorFrameView` (`kit/molecule/mirror/`) renders the `ImageBitmap`.
- `data-device-api` / `data-device-impl` — device commands, the only `ProcessBuilder`,
  `ScreencapMirrorTransport`'s adaptive capture loop.
- `DiscoveryModel` now includes `PollFailed` as well as `Detecting`/`ToolMissing`/
  `NoDeviceFound`/`Found`.

## 2. Much of this feature's UI ALREADY EXISTS — verify before authoring anything

The kit already ships, and you must read all three before designing:
- `kit/atom/annotation/AnnotationMark.kt` — with an `AnnotationState` enum and an accent-colour
  resolver.
- `kit/molecule/annotation/AnnotationPopover.kt`
- `kit/molecule/row/AnnotationRow.kt`

`presentation-core-ui` has ~115 components. F-1 and F-2 both found that the honest answer was
"assemble, don't author". Expect the same here and justify every new component.

The signal colour for annotation is `Theme.color.annotate` (the red), already in both palettes and
**identical in light and dark by design** — the contrast sweep in `presentation-core-styling`
covers it.

## 3. The proof-of-concept already solved the drag, including a real trap

`/Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Main.kt`:
- `detectDragGestures` with `dragStart`/`dragEnd` as `Offset?` state (lines ~116-120, ~203).
- `PendingAnnotation(start, end)` (line ~93) — a drawn rectangle plus its comment, pinned until
  deleted or sent.
- Its `rect` is serialised as `"x1,y1,x2,y2"` **in device screenshot pixel coordinates**, not
  composable coordinates.
- **The trap, already documented at line ~124:** without correcting for density the rectangle
  lands `density`× further from the cursor than it should — 2× on a Retina display. The host
  reports a `scale=2.0` Retina display (F-1's T-12 probe confirmed this), so this WILL bite.
  FR-6 requires a *device-pixel* size label, so there are three coordinate spaces in play —
  composable dp, host physical pixels, and device screenshot pixels. Get the conversion explicit
  and test it; an off-by-density bug here silently corrupts every annotation's anchor.

## 4. Design comps — the significant gap

Five comps are cached at `raw/design/` but **NOT ONE of them shows an annotation**. They are all
device-discovery screens (picker, NO SIGNAL, two error cards). So:
- The dashed red outline, the live pixel-size label, the ruler/tick guides, the dim-outside-boxes
  treatment, the numbered badge, the popover layout and the gutter/leader-line relocation have
  **no visual reference at all**.
- Design source URL (authenticated, do not try to fetch):
  `https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html`

Treat this as the spec's main open question. Derive what you can from the existing
`AnnotationMark`/`AnnotationPopover` components and the shipped design tokens, state plainly what
is invented, and keep invented values as named constants rather than scattering magic numbers —
F-1 did exactly this for the stage dot grid and it made the design conversation possible later.

## 5. Requirements

FR-6 (drag → dashed red outline + live device-pixel size label + ruler/tick guides), FR-7 (release
→ freeze the exact frame, create a staged annotation anchored to the box), FR-8 (frozen frame →
dim outside boxes, per-annotation popover with numbered badge, autofocused comment field, delete
control), FR-9 *should* (crowded popovers relocate to a gutter/sidebar with leader lines).
NFR-1, NFR-8 (Enter saves, Escape dismisses — keyboard-navigable), NFR-9 (light/dark parity).
AC-5, AC-6, AC-7, AC-8 are `test`; AC-9 is `demo`. SC-3, SC-4.

Note FR-7's wording: "**freeze the exact current frame**". The capture loop is still running when
the drag starts, so which frame is "exact" is a real design question — the one visible at drag
start, or at release? Decide it explicitly.

The toolbar's `stagedCounter` and `clearAction` slots are currently static stubs from F-1. Staged
annotations are what they were reserved for; wiring them is in scope.

## 6. Conventions and traps that have already bitten this chain

`CLAUDE.md` + all 19 `.claude/rules/`. Specifically:
- `design-tokens.md` — no literal `Color(0x…)`, no raw `.dp` at a call site, no hardcoded
  `TextStyle`; strict atom → molecule → organism.
- `package-layout.md` — `core/` internal-only, `source/` public, `explicitApi()` repo-wide.
- `orbit-mvi.md` — the four-file convention for `presentation-feature-*`.
- **A `commonTest` backtick test name containing a comma, apostrophe or colon compiles on the JVM
  and FAILS `compileTestKotlinIosArm64`.** F-1 shipped that bug and broke the repo-wide build.
- `resultLauncher` catches `Throwable` including `CancellationException` → `withTimeoutOrNull`,
  never `withTimeout`.
- A fresh git worktree has no `local.properties`, so Gradle dies with "SDK location not found"
  until it is copied in. The orchestrator seeds this now.
- The Kotlin daemon is capped at 3GB — **build per module**, not several KMP modules at once.

## 7. Scope

Annotation drawing, freezing and editing only. Measurement (`measure-mode`, F-5), payload preview
(F-6), MCP send (F-7), history (F-8) and the error cards (F-3) are all later specs. The side
panel's Notes tab exists as a slot from F-1 — decide explicitly whether staged annotations render
there in this spec or whether that is F-6's, and say which.
```
