# payload-preview — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-17T09:35:41Z |
| updatedAt | 2026-10-17T09:37:40Z |

## Gate

### type

human

### approved

true

## Provenance

### selfHash

69585a72b0f9249a23960d3f78e230b9e4985b875498f4fdab271c973280db91

## Data

### title

Payload Preview (Prompt / Crops / Hierarchy)

### goalStatement

The Payload tab that lets the reporter see exactly what the agent will receive before sending: a Prompt section with the auto-generated text, its token-count estimate (computed in under ~200ms) and an Edit affordance; a Crops section rendering a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'; a Hierarchy section showing a monospace indented view-hierarchy tree with matched/flagged nodes visually marked; and a footer summarizing 'frame.png · N crops · hierarchy.xml' plus a total size that matches the real payload. Why: trust in the one-shot handoff depends on the reporter being able to inspect and tune the bundle, and this is where the payload assembly (and its versioned envelope) is actually built.

### rawDescription

The Payload tab that lets the reporter see exactly what the agent will receive before sending: a Prompt section with the auto-generated text, its token-count estimate (computed in under ~200ms) and an Edit affordance; a Crops section rendering a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'; a Hierarchy section showing a monospace indented view-hierarchy tree with matched/flagged nodes visually marked; and a footer summarizing 'frame.png · N crops · hierarchy.xml' plus a total size that matches the real payload. Why: trust in the one-shot handoff depends on the reporter being able to inspect and tune the bundle, and this is where the payload assembly (and its versioned envelope) is actually built.

### sources

| type | ref | summary | cached | fetchedAt | hash |
| --- | --- | --- | --- | --- | --- |
| text | .pipeline/prd/docs/rectify/handoff/payload-preview.md | PRD feature bundle: F-6, priority must, phase PH-1, estimate M. Depends on F-4 (drag-annotate-frozen-frame) and F-5 (measure-mode). FR-12/13/14/15: show prompt with token estimate + Edit, 3-col crop grid with red outline + captions, hierarchy tree with flagged nodes, footer with summary. NFR-11: token computation <~200ms. NFR-12: payload format versioned. AC-13/14/15/16: test/inspection verifications. SC-6: happy path review. US-6: developer trusts and tunes what agent receives. |  |  |  |
| text | .pipeline/sdd/docs/payload-preview/inputs/design-payload-preview.md | AUTHORITATIVE design specification extracted from design HTML: Prompt section markup (label + token formula 360+count*140 + Edit link + mono block max-height 214px); Crops section 3-column grid with 84×72px containers, crop(r,bw,bh,pad,fixed,withRect) transform math, caption format #N·WxH; Hierarchy section with node id colouring (ink/ink3) and red badges for matches; footer 'frame.png · N crops · hierarchy.xml' + formula (1.6+count*0.12).toFixed(1)+' MB'; empty state copy; hierarchy phase state machine (dumping/sent/frozen). |  |  |  |
| url | /Users/andrew-malitchuk/Downloads/Rectify Screens.html | Raw design source: JS bundler page with __bundler/manifest payload. Lines 372–412 define the Payload tab markup (empty state, Prompt/Crops/Hierarchy sections, footer). Lines 773–791 contain the executable prop builder: crop() transform, prompt assembly (pl.join), token/size formulas, crops array, hierarchy rendering, payload summary rows. Line 528 shows canonical demo state (tab:'payload'). | raw/rectify-screens.html | 2026-10-17T12:40:00Z | 4ecfc8eb64802516509584319f01bfa48a47129f952abf2cfce96cd869bfb158 |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/SidePanelFrame.kt | SidePanelFrame(activeTab, onTabSelect, modifier, tabCounts, content) | The three-tab panel frame (Notes/Payload/History). Payload tab content slot is a second-order composable parameter. Already exists; Payload implementation wires its content lambda here. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellTab.kt | ShellTab.Payload, shortcutToTab(isMeta, key) | Enum case for Payload tab (already exists); Cmd+2 shortcut mapping already in place. No changes needed. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/bar/tab/SidebarTabs.kt | SidebarTabs(items, selectedIndex, onSelect) | Tab strip header molecule (already exists). Renders the three tab labels. No wiring needed for Payload tab. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/HierarchyRow.kt | HierarchyRow(text, mark, modifier) | Single hierarchy tree row with optional badge. Renders indented node text + optional red match marker. Reusable for Hierarchy section. Zero open questions. |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/AnnotationRow.kt | AnnotationRow(...) | Existing annotation row in Notes tab. Study its row layout and styling as a pattern for Crops/Hierarchy sections. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorNotesTab.kt | MirrorNotesTab(...), MirrorNotesTabContent(...) | The existing Notes tab content implementation. Mirrors its state-wiring pattern for Payload tab. Reads annotations from state and renders them. Payload tab will read annotations + measurements and render three sections. |
| rectify-kmp | presentation-core-styling/src/commonMain/kotlin/presentation/core/styling/core/Theme.kt | Theme.color, Theme.typography, Theme.spacing | Design tokens: must verify ink/ink2/ink3, codeBg, line, red, ok, warn colours; and JetBrains Mono typography tokens at 9.5/10.5/11.5px. Hierarchy section uses colour references that must exist. |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| Rectify Screens HTML | payload-tab-prompt | raw/rectify-screens.html | Lines 378–381: Prompt section header (PROMPT label, token estimate ≈360+count*140 tokens, Edit link) and mono block (10.5px JetBrains Mono, max-height 214px, codeBg bg, line border, scrollable). Prompt assembly is lines 779–781 pseudo-code. |
| Rectify Screens HTML | payload-tab-crops | raw/rectify-screens.html | Lines 382–396: Crops section header (CROPS label, from frame {frameId}) and 3-column grid (gap 8px, grid-template-columns repeat(3,1fr)). Per-crop: 72px height container, scaled frame inside with red outline, caption #N·W×H (9.5px mono). crop() transform math lines 773–776. |
| Rectify Screens HTML | payload-tab-hierarchy | raw/rectify-screens.html | Lines 398–408: Hierarchy section header (HIERARCHY label, source label uiautomator·412nodes or idb·388nodes) and tree rows (10.5px JetBrains Mono, pre-wrap indentation, optional red badge for matches). Coloring: matched=ink, unmatched=ink3. Badge: padding 0 4px, radius 3px, red bg, white text, 9.5px. |
| Rectify Screens HTML | payload-tab-footer | raw/rectify-screens.html | Line 411: Footer row (flex justify-between). Left: 'frame.png · {count} crops · hierarchy.xml' (ellipsis on overflow). Right: payload size formula (1.6+count*0.12).toFixed(1)+' MB'. Both 10.5px JetBrains Mono, ink3. |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| this-project | SidePanelFrame — the three-tab panel chrome with tab header (SidebarTabs) and content slot. Notes tab already wired; Payload tab content is empty. Reuse the frame, wire the Payload content composable. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/SidePanelFrame.kt |
| this-project | MirrorNotesTab — the working Notes tab content implementation. Study its wiring pattern: reads staged annotations from state, maps to UI. Payload will follow the same pattern: reads annotations + measurements, renders sections. | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorNotesTab.kt |
| this-project | HierarchyRow — a single hierarchy tree row with optional badge. Handles indentation display + red match marker. No integration yet; ready to use in Hierarchy section. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/HierarchyRow.kt |
| this-project | drag-annotate-frozen-frame (F-4) — completed and validated. Delivers staged annotations (raw[]), frame geometry. Payload tab consumes these. | .pipeline/sdd/docs/drag-annotate-frozen-frame/validation/verdict.md |
| this-project | measure-mode (F-5) — completed and validated. Delivers measurement results (mAll[], each with node id, dimensions, findings). Payload assembly includes findings in prompt. | .pipeline/sdd/docs/measure-mode/validation/verdict.md |

### glossary

| term | definition |
| --- | --- |
| Payload tab | The second of three side panel tabs (Notes / Payload / History). Shows what the agent will receive: prompt text, cropped images, view hierarchy tree, and a size summary. |
| Prompt section | First section of Payload tab. Contains auto-generated text that will be sent to the agent, a token-count estimate (baseline 360 + 140 per annotation), and an Edit affordance. |
| Crops section | Second section of Payload tab. A 3-column grid of thumbnail crops, one per annotation, each showing the annotation box cropped from the frame with the red outline baked in. |
| Hierarchy section | Third section of Payload tab. A monospace indented view-hierarchy tree (dumped at freeze time), with red badges marking nodes that match annotated regions. |
| Payload footer | Summary line at the bottom of the Payload tab showing 'frame.png · N crops · hierarchy.xml' (left) and total payload size in MB (right). |
| Token estimate | Approximate token count of the prompt that will be sent to Claude. Formula: 360 + (annotation count × 140). Must compute in under ~200ms. |
| Payload size | Estimated total size of all files (frame PNG + crops + hierarchy XML). Formula: (1.6 + annotation count × 0.12) MB, fixed to 1 decimal place. |
| crop(r, bw, bh, pad, fixed, withRect) | Transform function that scales and positions a frame slice into a bounding container. Input: annotation rect r, target width bw, target height bh, padding pad. Output: container dimensions (bw, bh), frame offset (ml, mt), scale transform, and optionally the scaled rect for the red outline. |
| Hierarchy dump | The complete view hierarchy tree extracted from the Android device (uiautomator dump) or iOS simulator (idb debug tree). Captured once at freeze time, cached alongside the frame, and made available for payload assembly and measure mode. |
| Hierarchy phase | State of the hierarchy relative to sending: 'dumping' (extract in progress), 'frozen' (ready to send), 'sent' (confirmed sent to agent). |
| Edit affordance | User-facing action (button/link) in the Prompt section header that allows editing the auto-generated prompt before sending. |

### attachments

| name | path | type |
| --- | --- | --- |
| design-payload-preview.md | .pipeline/sdd/docs/payload-preview/inputs/design-payload-preview.md | markdown |
| design-payload-source.md | .pipeline/sdd/docs/payload-preview/inputs/design-payload-source.md | markdown |

### ambiguities

- FRAME ID LABEL: Design shows 'from frame 18422' in the Crops section header. Is this a literal frame ID (device-timestamp-based), a sequential counter, or just demo placeholder? Where does this ID come from in the real implementation?
- EDIT AFFORDANCE: What does clicking 'Edit' on the Prompt section do? (a) Launch an overlay prompt editor so the user can tune the text before sending? (b) Copy-to-clipboard or in-place text selection? (c) Hide/disable the button under certain states (locked/sent)? Is this a Stage 2 spec decision or critical for Stage 4 implementation?
- CROP RED OUTLINE: The design renders a 1px red border inside the cropped thumbnail, assumed to be the annotation rect scaled into the crop view. Does the red outline always match the annotation rect, or can it differ? How does the 'fixed' parameter in crop() affect outline rendering?
- HIERARCHY SOURCE & TIMING: Design shows 'uiautomator · 412 nodes' but no implementation path. Hierarchy is needed for both measure-mode and payload-preview. Are they dumped once at freeze time (shared) or separately? Who owns the dump and cache? Payload-preview assumes it is ready; what is the failure mode if dump fails or is slow?
- MEASUREMENT RESULTS IN PROMPT: Prompt assembly includes 'if (q.finding) pl.push(...measurement finding...)' — so measure-mode findings are auto-included in the prompt. What if the user does not hold Option (no measurements) or measure-mode is disabled? Does the prompt still assemble correctly? Is q.finding nullable/optional?
- DEVICE/RESOLUTION HARDCODING: Prompt includes 'Device: Pixel 8 Pro · Android 15 · 1344×2992' (hardcoded in design). Should this read actual device name, OS version, and screen resolution from the captured frame metadata instead?
- TOKEN COMPUTATION PERFORMANCE (NFR-11): Design uses a simple heuristic formula (360 + count*140). Does this formula alone meet the ~200ms target, or is heavier tokenization needed? Is the heuristic acceptable as a final estimate or a placeholder pending real tokenizer integration?
- CAPTION DIMENSION FORMATTER: Crop captions use the format '#N · W×H' where W and H come from a formatter called D(). What does D() do? Is it the same as the measure-mode dimension formatter? Does it round or truncate? Must it use × (U+00D7) or will x work?
- PAYLOAD FOOTER IN NOTES TAB: The footer shown in Payload tab (frame.png · N crops · hierarchy.xml + size) — should it also appear in the Notes tab footer when that tab is active, or only in Payload tab? Design illustrates Payload tab only.
- COLOUR VARIABLE MAPPING: Design JS uses WARN, OK, RED constants. Are these Theme.color.warn / Theme.color.ok / Theme.color.red, or do they map to different tokens? Check existing Theme definitions before wiring.
- HIERARCHY PHASE STATE MACHINE: State changes colour/label (dumping → orange warning, sent → green checkmark). Is the Hierarchy row only visible when annotations exist, or is it shown even in empty state? If shown empty, what label/colour does it have?
- PROMPT BLOCK SCROLLING BEHAVIOR: Max-height 214px with overflow:auto. When the prompt is scrollable, does the Edit affordance stay fixed in the header or scroll out of view with the content? What is the interaction model for a very long prompt?

### dictation

Launched via `/sdd-build-run-pipeline payload-preview --fast` with design HTML (cached on 2026-10-17) and the portfolio handoff (.pipeline/flow/portfolio.md) as primary inputs. No upstream input.json handoff from UI/UX. Payload-preview is [>] ready in the portfolio, depends on drag-annotate-frozen-frame (F-4) and measure-mode (F-5), both SDD-done. This is a fast-track spec seeded from PRD and design, no ambiguities pre-supplied.
