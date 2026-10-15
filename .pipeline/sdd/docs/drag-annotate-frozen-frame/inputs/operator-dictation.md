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
