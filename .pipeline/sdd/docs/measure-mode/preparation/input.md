# measure-mode — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-16T16:55:59Z |
| updatedAt | 2026-10-16T17:08:13Z |

## Gate

### type

human

### approved

true

## Provenance

### selfHash

14ec31f1aacba4b0ad7a6ccb07bfdf222efc2a533ca7a39f205003450534d014

## Data

### title

Option-Key Measure Mode

### goalStatement

Holding Option over an annotation box enters Measure mode: Rectify snaps to the matched underlying view-hierarchy node, draws dashed guide boxes for neighboring views, and shows dp/pixel gap measurements in a floating 'Measuring' panel, highlighting misaligned or undersized dimensions in red. On release, the measurement data is appended to that annotation's note prefixed with the Option marker, so the sent payload carries exact numbers instead of estimates. Must resolve node lookup in under ~100ms to feel instantaneous, and must degrade clearly when no node matches. Why: no bug-report-to-agent tool found in research has hierarchy-snapped measurement; the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path.

### rawDescription

Holding Option over an annotation box enters Measure mode: Rectify snaps to the matched underlying view-hierarchy node, draws dashed guide boxes for neighboring views, and shows dp/pixel gap measurements in a floating 'Measuring' panel, highlighting misaligned or undersized dimensions in red. On release, the measurement data is appended to that annotation's note prefixed with the Option marker, so the sent payload carries exact numbers instead of estimates. Must resolve node lookup in under ~100ms to feel instantaneous, and must degrade clearly when no node matches. Why: no bug-report-to-agent tool found in research has hierarchy-snapped measurement; the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path.

### sources

| type | ref | summary | cached | fetchedAt | hash |
| --- | --- | --- | --- | --- | --- |
| text | .pipeline/sdd/docs/measure-mode/inputs/design-measure-mode.md | AUTHORITATIVE algorithm specification extracted from design HTML: node snap via 4px slack largest-area-first, neighbour selection by side with band tests, gap measurements to frame edge when no neighbour, guide suppression below 1 unit, three-branch finding logic (top misalignment ±10px, left misalignment ±24px, touch target <32dp), panel geometry and typography tokens (monoTag, monoRuler), no-match fallback to View annotation with frame rect. |  |  |  |
| text | .pipeline/prd/docs/rectify/handoff/measure-mode.md | PRD feature bundle: F-5, priority must, phase PH-1, estimate L. Depends on F-4 (drag-annotate-frozen-frame). FR-10: enter measure mode on Option hold, snap to node, show guides and panel, highlight misalignment red, attach data to note. NFR-2: <~100ms lookup. NFR-9: light/dark theme parity. AC-10: frozen frame + Option → snap + guides + panel. AC-11: misalignment → red highlight + marker prefix + note attachment. SC-5: happy path—hold, snap, guides, misalignment red. US-5: developer gets exact measurements instead of prose estimate. |  |  |  |
| url | /Users/andrew-malitchuk/Downloads/Rectify Screens.html | Raw design source: JS bundler page with __bundler/manifest payload containing the working measure(annotation, unit) implementation (executable specification), ELS fixture (view hierarchy), and all screen comps including measure panel geometry, keycap, typography, finding highlight, footer copy. | raw/rectify-screens.html | 2026-10-16T16:55:37Z | 4ecfc8eb64802516509584319f01bfa48a47129f952abf2cfce96cd869bfb158 |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/annotation/MeasureCard.kt | MeasureCard(nodeId, specs, title, index, finding) | 360dp spec-sheet organism; accepts MeasureSpec(label, value) list. Previewed. Zero callers. Ships with hardcoded finding wash alpha and uses Theme.color.measure (blue). Ready to wire. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/MeasureMark.kt | MeasureMark(modifier, role: MeasureMarkRole) | Canvas-drawn dashed/solid outline in Theme.color.measure. Role enum: Target (solid) / Neighbour (dashed, 4/3px dash intervals). Previewed. Zero callers. Gap labels are MeasureTag; connector lines are caller's responsibility. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/tag/MeasureTag.kt | MeasureTag(text, modifier, containerColor) | Signal-filled chip (4dp radius, 5/2dp padding); label is literal white in both themes (sits on signal only). Formatting is caller's job. Already carries monoTag typography. Previewed. Zero callers. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt | MirrorAnnotationStage(frame, frozen, ...onIntent) | The mirror render layer with pointerInput (detectDragGestures) and isMetaPressed available. Current: drag → annotate (frozen). Measure mode hooks here: Option-hold detection during drag, measure logic dispatch, result accumulation. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/geometry/FrameGeometry.kt | FrameGeometry(framePx, deviceSize, density) | Pure geometry: pointerPx ↔ devicePx ↔ DpRect. Already proven for drag. Measure algorithm consumes device-space rects (x/y/w/h), so measures live in device-pixel frame. |
| rectify-kmp | presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/ThemeTypography.kt | Theme.typography.monoTag, Theme.typography.monoRuler | Design spec names both: monoTag (9.5/13, 500) for Δ labels; monoRuler (8.5/12, 400) for ruler numerals. Both ship in Theme.typography. Check if they exist or need creation. |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| Rectify Screens HTML | measure-panel | raw/rectify-screens.html | Floating card, position absolute top:16px left:16px, 228px wide, 10px radius, surface bg, line2 border. Header: ⌥ keycap + 'Measuring' 600 + index right-aligned mono 10.5. Node id mono 11 blue. Rows gap:4px (Size / Top / Right / Bottom / Left in k/v mono pairs). Finding: 7/8 padding, 6px radius, red text on 10% annotate wash. Footer: 11px ink3 explanation copy. |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| this-project | MeasureCard — full 360dp panel card component with node id, spec rows, optional finding block. Handles layout, theming, finding highlight. Not integrated; no caller wires its inputs. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/annotation/MeasureCard.kt |
| this-project | MeasureMark — atom outline renderer, Target (solid) / Neighbour (dashed) roles. Positioned via Modifier on caller's coordinates. No content, no intents. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/MeasureMark.kt |
| this-project | MeasureTag — atom dimension/offset label chip. White text on colored fills (default annotate red, measure blue). Formatting is caller's responsibility. Works in any context. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/tag/MeasureTag.kt |
| this-project | AnnotationOverlay — F-4 molecule: dim scrim + clipPath(Difference) cut-outs + one AnnotationMark per DpRect. Same pattern applies to MeasureMarks (Target + Neighbours). | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationOverlay.kt |
| this-project | drag-annotate-frozen-frame (F-4) — completed and validated. Delivers frozen frame, staged annotations, comment popover, FrameGeometry. Measure mode reuses its FrozenFrame/StagedAnnotation data, same pointerInput layer. | .pipeline/sdd/docs/drag-annotate-frozen-frame/validation/verdict.md |

### glossary

| term | definition |
| --- | --- |
| snap | Automatic alignment to the matched underlying view-hierarchy node containing the annotation box. Largest fully-contained node within 4px slack wins; no match → fallback to annotation's own box labeled 'View (no match)'. |
| slack | 4 pixel tolerance on every edge of the annotation box when testing node containment. A drag that misses by a pixel still snaps. |
| neighbour | A non-overlapping node adjacent to the snapped node on one of the four sides (L/R/T/B). Nearest non-overlapping node per side (right-most left, left-most right, lowest top, highest bottom). |
| unit | The measurement unit for all numeric values: dp (density-independent pixels on Android) or pt (points on iOS) or px (device pixels for frame geometry). Design spec uses dp/px; caller must format correctly. |
| guide | 1px dashed line drawn at the target's mid-axes (horizontal: centre y; vertical: centre x), labelled with the gap measure. Suppressed if gap < 1 unit. |
| finding | One of three misalignment/size issues highlighted in red: (1) top misalignment vs left neighbour (±10px tolerance), (2) left misalignment vs top neighbour (±24px tolerance), (3) touch target too small (<32dp height). First match wins; only one per measure. |
| node | A single element in the Android view hierarchy (or iOS equivalent), identified by a unique id string (e.g. 'TextView#item_price'). Has x/y/w/h device-pixel bounds and children. |
| measurement panel | Floating card that displays the snapped node's id, dimensions, position offsets (Top/Right/Bottom/Left to neighbours or frame), and any finding. Shown while Option is held; persisted to the annotation note on release. |

### attachments

| name | path | type |
| --- | --- | --- |
| design-measure-mode.md | .pipeline/sdd/docs/measure-mode/inputs/design-measure-mode.md | markdown |

### ambiguities

- NODE HIERARCHY DATA SOURCE: The design HTML ships a working measure(annotation, unit) function that runs against an ELS fixture (array of {x,y,w,h,id} objects). The app has no equivalent: frames are raw ImageBitmap blobs. Where does the node list come from at measure time? (a) dumped from adb uiautomator (reachable on Android only, >2sec per dump, conflicts with NFR-2); (b) cached once at freeze time (makes sense with F-4's frame capture, but requires freeze-time uiautomator call); (c) never, and measure snaps only within the visual annotation box (no real hierarchy data, defeats the feature's purpose); (d) simulator mock (for dev/test, not production). Design spec assumes real hierarchy; Stage 1 must commit to the source.
- OPTION KEY DETECTION: Compose has isMetaPressed (Cmd on macOS, Ctrl on Windows/Linux) but no isOptionPressed/isAltPressed. On macOS, Option is Alt. Is the requirement Option (⌥) macOS-only, Alt-equivalent on other platforms, or does Compose Desktop even expose it? MirrorAnnotationStage imports isMetaPressed but never consumes it; detect-Option-on-drag requires Compose Desktop to expose Alt/Option through KeyEvent or PointerEvent.isAltPressed (not verified in this codebase).
- MEASUREMENT PANEL PLACEMENT: Design spec shows a floating card (position absolute, top-left corner). F-4's side panel (Notes tab) is already designed for annotations. Should the measure panel float over the stage (occludes content, design-accurate) or integrate into the side panel (less intrusive, mirrors F-4 pattern)? Current components suggest floating (MeasureCard has widthIn(max = 360.dp) and is a self-contained panel), but no explicit wiring location.
- MEASURE DATA LIFETIME: On Option release, does the measurement data persist to the annotation's note field (appended with ⌥ prefix, per AC-11), or display only (no persistence until the annotation is sent)? If persist: note becomes read-only until measure completes? If display-only: how is it recovered if the panel is closed before sending? Design copy says 'Numbers attach to the annotation and go into the prompt', implying persistence.
- PERFORMANCE CONSTRAINT DEPENDENCY: NFR-2 requires <~100ms for hierarchy lookup to feel instantaneous. Design spec warns: 'NFR-2 is satisfiable only if the hierarchy is already in hand when Option goes down — dumped once at freeze time, not on key-down. Any design that dumps on key-press will miss ~100ms by an order of magnitude.' This is a Stage 1 decision, not a Stage 4 surprise: commit now to hierarchy source and timing.
- NEIGHBOUR DETECTION ON NO-SNAP: When no node matches (fallback to 'View (no match)'), what are the neighbours? The annotation box itself has no children, so every UI element is equidistant. Should neighbours be skipped (no guides drawn) or calculated against frame edges? Design assumes a matched node; no spec for the fallback case.
- THEME TOKENS: Design spec names monoTag and monoRuler. Check Theme.typography: are they present and correct (9.5/13/500 and 8.5/12/400 respectively)? If missing, add them before measure-mode spec begins.
- UNIT FORMATTING: Dimensions must use '×' (U+00D7), deltas must use 'Δ' with no space before unit (e.g. 'Δ6dp'), positions use parentheses ('(1035, 862)'). The design's own table is authoritative. Are these rules enforced anywhere (a lint rule, a test, a formatter) or only by convention?

### dictation
