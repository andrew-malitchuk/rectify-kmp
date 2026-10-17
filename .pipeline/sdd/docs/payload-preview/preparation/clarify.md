# payload-preview — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | done |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-17T09:56:16Z |
| updatedAt | 2026-10-17T09:56:16Z |

## Gate

### type

human

### approvedBy

user

### approved

true

### at

2026-10-17T09:56:16Z

## Provenance

### selfHash

223e969e953c5cf8d8b3a41bdf9ca66998ebbfd300e45d02e92f3c59e26920b6

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 69585a72b0f9249a23960d3f78e230b9e4985b875498f4fdab271c973280db91 |

## Data

### overview

Payload Preview (PRD F-6) is the Payload tab's three-section readout — Prompt, Crops, Hierarchy — plus a footer summary, letting a reporter see exactly what MCP/Claude Code will receive before sending. It is pure assembly over data two completed specs already produce: F-4 (drag-annotate-frozen-frame)'s StagedAnnotation list and F-5 (measure-mode)'s cached HierarchySnapshotModel and finding-carrying annotation comments. Android-hosted, Desktop-rendered (presentation-feature-mirror); no new platform surface.

### problemStatement

The reporter currently has no way to see the actual bundle (prompt text, cropped evidence images, hierarchy tree, size) before it is sent to the agent — trust in the one-shot handoff requires inspecting and lightly tuning what gets sent, not sending blind.

### purpose

Render the Payload tab's Prompt/Crops/Hierarchy sections and footer from data F-4 and F-5 already produce, so the reporter can verify and (minimally) tune the exact payload before Resolve & Send.

### goals

- Assemble the prompt, token estimate, crop grid and hierarchy tree from already-shipped state (StagedAnnotation, HierarchySnapshotModel, DeviceModel) with zero new data sources.
- Keep the token estimate and payload size formulas honest heuristics that visibly match what is actually attached, never drifting from the real send.
- Degrade clearly, not silently, when zero annotations are staged or the hierarchy dump produced nothing usable (HierarchyStatus.Unavailable).

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-13 | Given at least one staged annotation exists, when the user opens the Payload tab, then a generated text prompt with token-count estimate and an Edit affordance is shown. | must | test | false |
| AC-14 | Given staged annotations exist, when viewing Payload > Crops, then a 3-column grid shows each annotation's cropped image with its red outline baked in and caption 'from frame <id>'. | must | test | false |
| AC-15 | Given staged annotations exist, when viewing Payload > Hierarchy, then a monospace indented tree displays with matched/flagged nodes visually marked. | must | test | false |
| AC-16 | Given the Payload tab is open, then its footer summarizes 'frame.png · N crops · hierarchy.xml' plus a total size that matches the actual payload. | must | inspection | false |
| AC-72 | Crop captions and any pixel dimensions in the Payload tab render with the unspaced × (U+00D7) multiplication sign, reusing AnnotationFormat.kt's metaLine pattern — never a letter x and never a new formatter. | must | test | false |
| AC-73 | The per-annotation prompt line is built from StagedAnnotation.rect and StagedAnnotation.comment verbatim (which already carries any ⌥-prefixed measurement line from measure-mode's noteLine/replaceNoteLine) with no separate 'measured:' branch, regardless of whether the user ever held Option on that annotation. | must | test | false |
| AC-74 | The Hierarchy section's source label reads 'uiautomator · {nodes.size} nodes' from the already-cached HierarchySnapshotModel (MirrorViewModel.frozenHierarchy); it never triggers a new dump. | must | test | false |
| AC-75 | A hierarchy node is marked (red '#{annotationIndex}' badge, ink color) if and only if it is the result of NodeSnap.snapTarget for that annotation's rect; every other node renders in ink3 with no badge. | must | test | false |
| AC-76 | The crop red outline is positioned using exactly crop(annotationRect, 84, 72, 14, fixed=false, withRect=true)'s returned rect — never a differently-padded or unscaled rect. | must | test | false |
| AC-77 | Given zero staged annotations, the Payload tab renders only the literal empty-state copy ('Nothing staged. Draw a box and the payload builds here, exactly as Claude will get it.') — none of Prompt, Crops, Hierarchy or the footer render. | must | test | false |
| AC-78 | Token estimate (360 + count*140) and payload size ((1.6 + count*0.12) MB, one decimal) recompute synchronously as annotations are added/removed/edited, with no measurable lag (satisfies NFR-11's ~200ms budget trivially since both are O(1) arithmetic). | must | test | false |
| AC-79 | Clicking the Edit affordance toggles the prompt block into an editable text area (reusing the existing MultilineInput atom); the edited text, once changed, is what gets attached as the prompt instead of the auto-generated text. | should | test | false |
| AC-80 | When HierarchyStatus is Unavailable or Absent for the frozen frame, the Hierarchy section shows zero nodes and an explicit 'no hierarchy' state rather than an empty tree that looks like a bug, and the rest of the Payload tab (Prompt, Crops, footer) still renders normally. | must | demo | false |

### functionalRequirements

| id | text |
| --- | --- |
| FR-12 | In the Payload tab's Prompt section, show the auto-generated text prompt, its token-count estimate, and an Edit affordance. |
| FR-13 | In the Payload tab's Crops section, show a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'. |
| FR-14 | In the Payload tab's Hierarchy section, show a monospace, indented view-hierarchy tree with matched/flagged nodes visually marked. |
| FR-15 | Show a Payload footer summarizing contents ('frame.png · N crops · hierarchy.xml') and total payload size. |
| FR-47 | Prompt assembly builds one line per staged annotation from its rect (metaLine-style) plus its comment verbatim — comment already carries any ⌥-prefixed measurement line from measure-mode, so no separate finding branch exists. |
| FR-48 | Token estimate = 360 + (staged annotation count × 140); recomputed whenever the annotation count changes, with no tokenizer dependency. |
| FR-49 | For each staged annotation, compute crop(rect, bw=84, bh=72, pad=14, fixed=false, withRect=true) and render the cropped frame slice with the baked red outline plus caption '#{index}·{w}×{h}' using the annotation rect's own raw pixel dimensions. |
| FR-50 | Hierarchy section renders HierarchySnapshotModel.nodes as indented rows (indent = node.depth) labeled 'uiautomator · {nodes.size} nodes'; a node returned by NodeSnap.snapTarget for any staged annotation gets a red '#{annotationIndex}' badge and ink color, every other node is ink3. |
| FR-51 | Footer renders 'frame.png · {count} crops · hierarchy.xml' (left) and '{(1.6 + count*0.12).toFixed(1)} MB' (right); Payload tab only. |
| FR-52 | The prompt's device/resolution line reads the real selected device (DeviceModel.displayName + DeviceModel.androidVersion) and the real frozen frame size (FrozenFrame.deviceSize) — never the design's hardcoded 'Pixel 8 Pro · Android 15 · 1344×2992' literal. |
| FR-53 | Edit affordance click toggles the prompt block between read-only mono text and an editable MultilineInput; the edited text, once non-empty and changed, overrides the auto-generated prompt for sending. |
| FR-54 | When zero annotations are staged, the Payload tab renders only the empty-state copy; Prompt, Crops, Hierarchy and the footer do not render at all (matches the design's own count === 0 gate). |
| FR-55 | Crops section header shows 'from frame {frameId}' where frameId is a new per-session sequential freeze counter (increments once per freeze), not a device timestamp or literal placeholder. |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-11 | performance | Token-count estimation for the generated prompt computes without perceptible delay (target under ~200ms) before Resolve & Send is enabled. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |

### outOfScope

- iOS payload preview — no view-hierarchy-dump equivalent exists for iOS in this codebase (same boundary as measure-mode's AS-19); ios-platform-support is a separate backlog item.
- Real tokenizer integration — ships the design's 360+count*140 heuristic; a genuine tokenizer dependency is a future upgrade only if the heuristic proves materially wrong.
- A new Compose UI-test harness — visual/geometry acceptance criteria (AC-67-style, AC-80) stay manual (demo), consistent with testing.md and measure-mode's precedent.
- Mirroring the payload footer into the Notes tab (unless the survey answer below picks that option) — SidePanelFrame's content slot is per-tab with no shared footer, so this is additive scope, not a given.
- New HierarchyStatus states or hierarchy phase visuals beyond dumping/ready/sent/unavailable — reuses the enum measure-mode already shipped, no new states invented here.

### constraints

| type | text |
| --- | --- |
| tech | Android only: the hierarchy dump source (HierarchySnapshotModel) has no iOS equivalent in this codebase; payload-preview inherits measure-mode's Android-only boundary. |
| tech | No new dependency for tokenization or hierarchy parsing — reuse NodeSnap.snapTarget, AnnotationFormat.TIMES/metaLine, MeasureFormat's noteLine shape, and HierarchySnapshotModel/HierarchyStatus as already shipped. |
| tech | All colors/typography must come from Theme.* tokens: RED→Theme.color.annotate, OK→Theme.color.sentFixed, WARN→Theme.color.warnStillOff (verified in ThemeColor.kt), plus ink/ink2/ink3/codeBg/line — no new tokens expected. |
| tech | SidePanelFrame's content lambda is per-tab with no shared footer slot; a Notes-tab-mirrored footer (if chosen) must be added directly to MirrorNotesTab, not the SidePanelFrame organism. |

### assumptions

| id | text |
| --- | --- |
| AS-22 | The view hierarchy is already dumped once at freeze time and cached (measure-mode's AS-18): HierarchySnapshotModel lives on MirrorViewModel.frozenHierarchy, its readiness mirrored via MirrorUiState.hierarchyStatus (HierarchyStatus.Absent/Dumping/Ready/Unavailable). Payload-preview reads this cache only and never triggers its own dump — this resolves input ambiguity #4 (hierarchy source & timing) from measure-mode's already-shipped decision rather than re-surveying it. |
| AS-23 | Payload-preview ships Android-only, matching measure-mode's AS-19; iOS has no hierarchy-dump equivalent wired and stays out of scope. |
| AS-24 | The design's WARN/OK/RED constants map to Theme.color.warnStillOff / Theme.color.sentFixed / Theme.color.annotate respectively (verified by reading ThemeColor.kt's doc comments) — no new color tokens are needed. |
| AS-25 | Crop caption and any other raw-pixel dimension text reuses the existing unspaced × (U+00D7) pattern from AnnotationFormat.kt's metaLine rather than inventing a new 'D()' formatter — this resolves input ambiguity #8. |
| AS-26 | Measure-mode already folds any measurement finding into StagedAnnotation.comment (⌥-prefixed line via noteLine/replaceNoteLine); the prompt's per-annotation text is therefore the comment verbatim with no parallel finding field to check — this resolves input ambiguity #5 without a special-case branch. |
| AS-27 | Frame id, the Edit affordance's depth, and the token formula's finality are the three decisions genuinely not resolvable from existing code or design text; the proposedSurvey below carries the recommended defaults until the orchestrator's survey returns user edits. |

### dependencies

| on | note |
| --- | --- |
| drag-annotate-frozen-frame | Supplies StagedAnnotation (rect, comment, index) and FrozenFrame (deviceSize) that the prompt, crops and footer are built from. |
| measure-mode | Supplies the cached HierarchySnapshotModel/HierarchyStatus the Hierarchy section reads, the NodeSnap.snapTarget function reused for badge matching, and the ⌥-prefixed finding lines already folded into annotation comments. |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-6 | Review payload before sending | happy | ["Open Payload tab", "Review generated prompt and token estimate", "Review crops grid", "Review hierarchy tree", "Check footer summary"] |
| SC-20 | Payload with zero staged annotations | edge | ["Freeze a frame with no annotations drawn", "Open the Payload tab", "Only the empty-state copy renders", "No Prompt/Crops/Hierarchy section or footer appears"] |
| SC-21 | Annotation with no matched hierarchy node | edge | ["Stage an annotation whose rect contains no hierarchy node within NodeSnap's slack", "Open Payload > Hierarchy", "No node carries that annotation's badge", "Open Payload > Crops: the crop still renders using the annotation's own rect for the outline"] |
| SC-22 | Prompt assembles correctly without any measurement | happy | ["Stage annotations without ever holding Option over any of them", "Open Payload > Prompt", "Each annotation line shows its comment (or 'no comment') with no finding text, and the prompt is otherwise well-formed"] |
| SC-23 | Freeze-time hierarchy dump was unavailable | error | ["Freeze a frame whose uiautomator dump failed or returned empty (HierarchyStatus.Unavailable)", "Open Payload > Hierarchy", "Section shows zero nodes with an explicit 'no hierarchy' state, not a silent empty tree", "Prompt, Crops and the footer still render normally"] |
| SC-24 | Edit the prompt before sending | happy | ["Open Payload tab with staged annotations", "Click the Edit affordance", "Prompt block becomes an editable text area", "Edited text becomes the prompt that would be attached to the send"] |

### successMetrics

- Token estimate and payload size shown in the UI always match what is actually assembled for the send — no displayed-vs-real drift.
- Every annotation's crop, caption and hierarchy badge stay index-consistent with the Notes tab list across edits/deletes.

### openQuestions

- Should an edited prompt override auto-clear when annotations subsequently change, or persist until the user clicks Edit again? Default assumed: persists until Edit is clicked again (no auto-clear) — simplest, no silent data loss.
- Real tokenizer integration remains a candidate future upgrade over the heuristic formula if it proves materially misleading at scale; not pursued in this spec.
- Where the new per-session freeze counter (FR-55) is hosted (FrozenFrame vs. MirrorViewModel) is left to Stage 2 planning.

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Crops section header shows 'from frame {frameId}' — where does frameId come from? (No frame id field exists anywhere in the codebase today; this mints one.) | ["New per-session sequential freeze counter (increments by 1 each time the stage freezes; simplest new field, no clock dependency)", "Device-timestamp-based id (epoch millis at freeze time; requires reading the device/system clock at freeze)", "Drop the label entirely for v1 (Crops header shows just 'CROPS', no 'from frame <id>' caption)"] | New per-session sequential freeze counter (increments by 1 each time the stage freezes; simplest new field, no clock dependency) | Neither FrozenFrame nor StagedAnnotation carries any id or timestamp today, so every option mints a new field. A sequential counter is the smallest addition (one Int, no clock/platform dependency) and is deterministic for tests, unlike a wall-clock timestamp. |
| What does clicking the Prompt section's 'Edit' affordance do? | ["Toggle the prompt block into an editable text area (reuse the existing MultilineInput atom); edited text becomes the prompt actually sent", "Copy-to-clipboard only — no editing capability, button just copies the generated text", "Render the affordance but leave it disabled/inert for v1 — satisfies FR-12's 'shown' requirement via inspection only, no behavior yet"] | Toggle the prompt block into an editable text area (reuse the existing MultilineInput atom); edited text becomes the prompt actually sent | presentation-core-ui already ships MultilineInput (120-240dp scrollable text atom) with zero new component work, and US-6's stated reason for this feature is letting the reporter 'trust and tune' the payload — a disabled or copy-only Edit button satisfies the letter of FR-12 but not the feature's own purpose. |
| Does the token-count estimate ship as the design's heuristic formula (360 + count*140), or integrate a real tokenizer? | ["Ship the heuristic formula as the real, final estimate — O(1) arithmetic, trivially meets NFR-11's ~200ms budget, no new dependency", "Integrate a genuine tokenizer library against the prompt text for an accurate count"] | Ship the heuristic formula as the real, final estimate — O(1) arithmetic, trivially meets NFR-11's ~200ms budget, no new dependency | No tokenizer dependency exists in the version catalog today, and the design explicitly specifies this formula as final, not a placeholder. Adding a tokenizer is a real dependency decision nothing in the input asks for; the heuristic is honest about being an estimate ('≈ {tokens} tokens'). |
| Does the payload footer (frame.png · N crops · hierarchy.xml · size) appear only in the Payload tab, or also mirrored into the Notes tab footer? | ["Payload tab only — matches the design screenshot literally, zero changes to MirrorNotesTab", "Both tabs — add the same summary row to MirrorNotesTab as well, duplicating it"] | Payload tab only — matches the design screenshot literally, zero changes to MirrorNotesTab | The design's own screenshot only shows this footer under the Payload tab; SidePanelFrame's content slot is per-tab with no shared footer mechanism today, so mirroring it into Notes is additive scope nothing in the input requires. |

### survey

| question | options | answer | source |
| --- | --- | --- | --- |
| Crops section header shows 'from frame {frameId}' — where does frameId come from? (No frame id field exists anywhere in the codebase today; this mints one.) | ["New per-session sequential freeze counter (increments by 1 each time the stage freezes; simplest new field, no clock dependency)", "Device-timestamp-based id (epoch millis at freeze time; requires reading the device/system clock at freeze)", "Drop the label entirely for v1 (Crops header shows just 'CROPS', no 'from frame <id>' caption)"] | New per-session sequential freeze counter (increments by 1 each time the stage freezes; simplest new field, no clock dependency) | user-edited |
| What does clicking the Prompt section's 'Edit' affordance do? | ["Toggle the prompt block into an editable text area (reuse the existing MultilineInput atom); edited text becomes the prompt actually sent", "Copy-to-clipboard only — no editing capability, button just copies the generated text", "Render the affordance but leave it disabled/inert for v1 — satisfies FR-12's 'shown' requirement via inspection only, no behavior yet"] | Toggle the prompt block into an editable text area (reuse the existing MultilineInput atom); edited text becomes the prompt actually sent | user-edited |
| Does the token-count estimate ship as the design's heuristic formula (360 + count*140), or integrate a real tokenizer? | ["Ship the heuristic formula as the real, final estimate — O(1) arithmetic, trivially meets NFR-11's ~200ms budget, no new dependency", "Integrate a genuine tokenizer library against the prompt text for an accurate count"] | Ship the heuristic formula as the real, final estimate — O(1) arithmetic, trivially meets NFR-11's ~200ms budget, no new dependency | user-edited |
| Does the payload footer (frame.png · N crops · hierarchy.xml · size) appear only in the Payload tab, or also mirrored into the Notes tab footer? | ["Payload tab only — matches the design screenshot literally, zero changes to MirrorNotesTab", "Both tabs — add the same summary row to MirrorNotesTab as well, duplicating it"] | Payload tab only — matches the design screenshot literally, zero changes to MirrorNotesTab | user-edited |
