# payload-preview — candidate-1

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | candidate-1 |
| status | done |
| producedBy | sdd-build-plan-draft |
| schemaVersion | 1 |
| createdAt | 2026-10-17T10:55:56Z |
| updatedAt | 2026-10-17T10:55:56Z |

## Provenance

### selfHash

30a4259da3c3b845ece31857b0d6762fe697d313c03ebde72be0b89a5b99524a

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 2267cda5392a2e91907782c27c41817c193e0366ba5635c65ad9034941bce918 |

## Data

### meta

#### title

Payload Preview — Prompt / Crops / Hierarchy

#### version

1

#### status

draft

### goal

Give the reporter a faithful, inspectable preview of the exact bundle the agent will receive — generated prompt (editable, with a token estimate), per-annotation crops with the red outline baked in, the cached view hierarchy with matched nodes badged, and a footer naming the files and total size — assembled entirely from data the mirror feature already holds.

### problemStatement

The reporter currently has no way to see the actual bundle (prompt text, cropped evidence images, hierarchy tree, size) before it is sent to the agent. Trust in a one-shot handoff requires inspecting and lightly tuning what gets sent, not sending blind.

### highLevelDescription

The Payload tab becomes the second real surface of the mirror side panel. It is pure projection: FR-12..15 and FR-47..55 are satisfied by one internal pure package (presentation-feature-mirror/core/payload/) plus one public composable (source/mirror/MirrorPayloadTab.kt), with three freeze-scoped fields added to FrozenFrame, one new intent (EditPrompt), one additive HierarchyRow parameter, and exactly two edits outside the feature module (MirrorHost's content lambda widens to (state, frame, hierarchy, onIntent); RectifyWindow.kt gains one ShellTab.Payload branch plus one tabCounts entry). No new Gradle module, no new dependency, no new data source, no new Koin binding, no new MirrorUiState field.

### background

Payload Preview is PRD F-6 and the consumer of two already-shipped specs: drag-annotate-frozen-frame (F-4) supplies StagedAnnotation (rect, comment, index) and FrozenFrame (deviceSize); measure-mode (F-5) supplies the once-per-freeze cached HierarchySnapshotModel on MirrorViewModel.frozenHierarchy, the HierarchyStatus four-state verdict reduced into MirrorUiState, NodeSnap.snapTarget, and the ⌥-prefixed measurement line already folded into StagedAnnotation.comment. Preparation is human-approved with readyForPlan:true and carries seven FINAL engineering decisions (frameId on FrozenFrame stamped from a private ViewModel counter incremented in stageAnnotation's intent body only; Edit reusing MultilineInput with no auto-clear; 360+count*140 as the final estimate; footer in the Payload tab only; hierarchy delivered via the widened MirrorHost lambda; crop pad=14 in design space scaled by frameWidth/296; hierarchy dumped once at freeze, Android-only). This plan implements inside those decisions and does not re-open them.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | preparation/preparation.json (readyForPlan:true, human-approved 2026-10-17) | .pipeline/sdd/docs/payload-preview/preparation/preparation.json |
| code | Distilled design spec — prompt assembly, token formula, crop() maths, hierarchy phase machine, per-element tokens | .pipeline/sdd/docs/payload-preview/inputs/design-payload-preview.md |
| code | Design provenance by line range + recorded design gaps | .pipeline/sdd/docs/payload-preview/inputs/design-payload-source.md |
| code | Payload tab markup (lines ~355-435) and props builder (lines ~760-830) | .pipeline/sdd/docs/payload-preview/raw/pages/screens.html |
| code | MirrorContract.kt — FrozenFrame, StagedAnnotation, DeviceRect, MirrorIntent, MirrorUiState.hierarchyStatus, stagedCount | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt |
| code | MirrorViewModel.kt — stageAnnotation intent body, frozenHierarchy StateFlow, dumpHierarchy fire-and-forget | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt |
| code | Registry — components.json (83), styles.json (17), assets.json (8), features.json (16), bugs.json (0 entries) | .pipeline/sdd/docs/registry/components.json |

### scope

#### in

- Payload tab's four sections — Prompt (mono text + '≈ N tokens' + Edit), Crops (3-column grid, baked red outline, captions), Hierarchy (indented mono tree, matched nodes badged), footer (file summary + MB) — FR-12..FR-15, AC-13..AC-16.
- Prompt assembly from StagedAnnotation.rect + comment verbatim with no separate 'measured:' branch, the real device/resolution line, and the 'Attached:' trailer — FR-47, FR-52, FR-53, AC-13, AC-72, AC-73.
- tokenEstimate(count) = 360 + count*140 and payloadSizeLabel(count) = (1.6 + count*0.12) MB to one decimal, shipped as FINAL heuristics, O(1), no tokenizer dependency — FR-48, FR-51, AC-16, AC-78.
- cropWindow(rect, frameSize, boxW=84, boxH=72, pad=14) with pad interpreted in DESIGN space and scaled by frameWidth/296, rendered via one Canvas.drawImage(srcOffset, srcSize, dstOffset, dstSize) plus a 1px outline rect taken from the same returned value — FR-49, AC-14, AC-76.
- Hierarchy section reading the already-cached HierarchySnapshotModel / HierarchyStatus through the widened MirrorHost content lambda, badging nodes via the existing NodeSnap.snapTarget, and never triggering a dump — FR-50, AC-15, AC-74, AC-75, AS-22.
- New per-session sequential frameId on FrozenFrame, stamped from a private MirrorViewModel counter incremented in stageAnnotation's intent body only (never inside reduce) — FR-55, AC-14.
- Edit affordance toggling the prompt into the shipped MultilineInput atom; the edited text lives on FrozenFrame.promptOverride and persists until Edit is clicked again (no auto-clear when annotations change) — FR-53, AC-79, SC-24.
- Empty-state gate (zero annotations renders only the literal empty-state copy, no sections, no footer) and an explicit no-hierarchy state for HierarchyStatus.Absent/Unavailable — FR-54, AC-77, AC-80.
- One additive HierarchyRow parameter (matched: Boolean = false) for AC-75's ink/ink3 split; one new mirror-payload-tab registry entry; crop/section composables stay private to MirrorPayloadTab.kt and are NOT registered.
- internal const PAYLOAD_FORMAT_VERSION = 1 declared in core/payload (rendered nowhere) so NFR-12 has a real artifact the future send spec reads instead of a dangling requirement.
- Android only, matching measure-mode's AS-19/AS-23 boundary; Desktop-JVM hosted (the window layer), Android-sourced (the dump).

#### out

- iOS payload preview — no view-hierarchy-dump equivalent exists for iOS in this codebase; the design's 'idb · N nodes' label is deliberately unimplemented.
- Real tokenizer integration — the 360+count*140 heuristic is final for this spec; a tokenizer is a future upgrade only if the heuristic proves materially wrong.
- Actually sending the payload, or producing real bytes — nothing writes frame.png / hierarchy.xml yet; the send spec owns replacing both heuristics with measured values.
- A new Compose UI-test harness — geometry/visual criteria stay @Preview + demo, consistent with testing.md and measure-mode's precedent.
- Mirroring the payload footer into the Notes tab — Payload tab only (FINAL); MirrorNotesTab is unchanged.
- New HierarchyStatus states or hierarchy phase visuals beyond the shipped Absent/Dumping/Ready/Unavailable; the design's 'sent ✓' / 'on send' states have no owner until the send spec exists.
- The design's Frame / Hierarchy / Agent payload summary rows beyond the footer line — the Agent row has no data source anywhere in this repo (no agent path, no project root, no MCP config); deliberately unbuilt rather than stubbed with the design literal.
- Changes to SidePanelFrame, SidebarTabs, ShellTab or shortcutToTab — all untouched.

### assumptions

- AS-22: The hierarchy is already dumped once at freeze time and cached — HierarchySnapshotModel on MirrorViewModel.frozenHierarchy, readiness mirrored as MirrorUiState.hierarchyStatus. Payload Preview reads this cache only and never triggers its own dump.
- AS-23: Payload Preview ships Android-only, matching measure-mode's AS-19; iOS has no hierarchy-dump equivalent wired.
- AS-24: The design's RED/OK/WARN map to Theme.color.annotate / Theme.color.sentFixed / Theme.color.warnStillOff; ink/ink2/ink3/codeBg/line/stage already exist. No new colour token is needed.
- AS-25: Crop captions and every raw-pixel dimension reuse core/annotation's existing internal TIMES constant (U+00D7) and the metaLine pattern — no new formatter.
- AS-26: Measure-mode already folds any measurement finding into StagedAnnotation.comment as a ⌥-prefixed line, so the prompt's per-annotation text is the comment verbatim with no parallel finding field and no 'measured:' branch.
- AS-27: frameId's host, the Edit affordance's depth and the token formula's finality were resolved by the human-approved brainstorm survey and are FINAL inputs to this plan.
- StagedAnnotation.index is already stable and index-consistent with the Notes tab list, so '#{index+1}' in captions, prompt lines and hierarchy badges stays consistent across edits and deletes for free.
- MirrorHost has exactly one production call site (RectifyWindow.kt), so widening its content lambda is a one-definition / one-call-site change.

### constraints

| type | text |
| --- | --- |
| tech | Android only: the hierarchy dump source has no iOS equivalent in this codebase; Payload Preview inherits measure-mode's Android-only boundary. |
| tech | No new dependency and no version-catalog addition — reuse NodeSnap.snapTarget, projectMeasureNodes, AnnotationFormat's TIMES/metaLine, MeasureFormat, HierarchySnapshotModel/HierarchyStatus, MultilineInput and HierarchyRow as already shipped. |
| tech | The freeze counter MUST be incremented in stageAnnotation's INTENT body, never inside reduce — reduce's lambda can re-run against the winning value when it loses a race, which would double-increment or reuse a frameId. This is a real concurrency trap, not a style note. |
| tech | All colour and typography values come from Theme.* tokens only (annotate / sentFixed / warnStillOff / ink / ink2 / ink3 / codeBg / line / stage, and the existing mono type scale); no literal Color or sp value appears in the new composable. |
| tech | package-layout.md: core/ holds internal declarations only, source/ holds the public API grouped per concern, and explicit API mode (explicitApi()) is enforced repo-wide — every public declaration needs an explicit visibility and return type. |
| tech | testing.md: :compose-application has NO test wiring, so nothing verifiable may be stranded in RectifyWindow.kt or inside a @Composable. Desktop is the target that runs tests; a commonTest backtick name must contain no comma and no colon (an apostrophe is fine); String.format is JVM-only, so one-decimal formatting is done by hand. |
| tech | SidePanelFrame's content lambda is per-tab with no shared footer slot — the footer therefore lives inside MirrorPayloadTab.kt, which is also why mirroring it into Notes was rejected. |
| tech | The window layer gets exactly one `if (tab == ShellTab.Payload)` branch plus one tabCounts entry; no label, count computation or conditional beyond that may creep into RectifyWindow.kt. |
| tech | platform-coverage.md: Android, iOS and Desktop-JVM targets all exist in every feature module, so every new declaration must compile for all three in commonMain; any expect introduced would need a desktopMain actual (this plan introduces none). |

### dependencies

| on | note |
| --- | --- |
| drag-annotate-frozen-frame (features.json#drag-annotate-frozen-frame) | Supplies StagedAnnotation (rect, comment, index) and FrozenFrame (deviceSize) the prompt, crops and footer are built from; FrozenFrame is the contract this plan extends with three freeze-scoped fields. |
| measure-mode (shipped, qa-ready) | Supplies the cached HierarchySnapshotModel/HierarchyStatus the Hierarchy section reads, NodeSnap.snapTarget + projectMeasureNodes reused for badge matching, and the ⌥-prefixed finding lines already folded into annotation comments. |
| components.json#multiline-input | The Edit affordance reuses this atom as-is; no change to it is planned. |
| components.json#hierarchy-row | Needs one additive `matched: Boolean = false` parameter; its `tag` badge already defaults to Theme.color.annotate, so the badge itself needs no change. |
| components.json#mirror-host | Content lambda widens to (MirrorUiState, ImageBitmap?, HierarchySnapshotModel, (MirrorIntent) -> Unit) — registry prop delta due at execution. |
| components.json#rectify-window-shell | Consumed unchanged: one new sidePanel branch and one tabCounts entry at the RectifyWindow.kt call site only. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| The crop transform is the only real geometry here and its placement has no automated visual check — a correct cropWindow wired into a wrong drawImage src/dst pairing renders a plausible slice of the WRONG part of the frame, and the outline moves with it, so the error is self-consistent and survives review. | med | high | Every integer lives in the pure CropWindow result (srcOffset, srcSize, dstSize, outlineRect, scale) and is unit-tested against hand-computed cases including all four edge clamps and the pad scaling; the composable passes those fields straight through with zero arithmetic of its own; one light/dark @Preview with two annotations at known positions (one edge-touching) lets a reviewer verify a crop by eye. |
| A real dump is ~400 nodes and each row is a Canvas-bearing molecule inside a drag-resizable panel; a non-lazy list or a projection computed inside the row loop turns every resize frame into ~400 recompositions plus ~400 snapTarget calls. | med | med | LazyColumn with NO vertical arrangement spacing (HierarchyRow's indent guides only meet when rows are adjacent), and payloadHierarchyRows computed once in remember(hierarchy, annotations) — never per row. Backed by a unit test asserting the projection is a single pass over snapshot.nodes with snapTarget called once per annotation, not once per node. |
| FrozenFrame is a contract three features now read; adding three fields invites a fourth and turns it into the dumping ground its KDoc kept pixels out of. | med | low | All three new fields share one stated admission rule written into the KDoc — 'captured at freeze, dies with the frame'. Anything failing that test goes elsewhere. Explicit review item on the state task. |
| The edited prompt is the feature's only write path and the one place a user can lose work: a tab switch, a Resume Live, or the Notes-tab auto-select on the next drag can all navigate away from the editor. | med | med | Reduce the text onto FrozenFrame.promptOverride (not remember), dispatch EditPrompt per change exactly as CommentAnnotation already does, and unit-test that the override survives an annotation add and a delete and is dropped exactly once — on ResumeLive. |
| Displayed-vs-real drift is this spec's own success metric and nothing can enforce it yet: the footer names files and a size for a bundle no code produces. | high | med | Keep both formulas in one internal place (core/payload/PayloadEstimate.kt) the future send path must call, label the token count '≈', declare PAYLOAD_FORMAT_VERSION = 1 beside them, and record here that the send spec owns replacing both heuristics with measured values rather than re-deriving them next to the real bytes. |
| Three near-identical mono sizes (10.5 / 9.5 / 11.5) and five colour tokens are specified per element; one wrong mapping is invisible in review and makes the panel subtly unlike the design — the one thing a design-led feature is judged on. | med | low | Map every element to an existing Theme.typography mono token ONCE, up front, as the first act of the previews task, and verify the whole mapping in a light/dark preview pair rather than section by section. |
| Anything that creeps from MirrorPayloadTab into RectifyWindow.kt becomes permanently untestable, because :compose-application has no test wiring at all. | low | med | Hold the window layer to exactly one if branch plus one tabCounts entry — the shape MirrorNotesTab already established — and make that an explicit review item on the wiring task. |
| Two annotations can snap to the same hierarchy node, which the design never addresses; a naive implementation could emit a duplicated row or a merged badge. | low | low | Ratified in this plan: lowest annotation index wins, one badge per node, one row per node. Asserted by a unit test on payloadHierarchyRows. |

### requirements

#### functional

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

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-11 | performance | Token-count estimation for the generated prompt computes without perceptible delay (target under ~200ms) before Resolve & Send is enabled. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-13 | Given at least one staged annotation exists, when the user opens the Payload tab, then a generated text prompt with token-count estimate and an Edit affordance is shown. | must | test |
| AC-14 | Given staged annotations exist, when viewing Payload > Crops, then a 3-column grid shows each annotation's cropped image with its red outline baked in and caption 'from frame <id>'. | must | test |
| AC-15 | Given staged annotations exist, when viewing Payload > Hierarchy, then a monospace indented tree displays with matched/flagged nodes visually marked. | must | test |
| AC-16 | Given the Payload tab is open, then its footer summarizes 'frame.png · N crops · hierarchy.xml' plus a total size that matches the actual payload. | must | inspection |
| AC-72 | Crop captions and any pixel dimensions in the Payload tab render with the unspaced × (U+00D7) multiplication sign, reusing AnnotationFormat.kt's metaLine pattern — never a letter x and never a new formatter. | must | test |
| AC-73 | The per-annotation prompt line is built from StagedAnnotation.rect and StagedAnnotation.comment verbatim (which already carries any ⌥-prefixed measurement line from measure-mode's noteLine/replaceNoteLine) with no separate 'measured:' branch, regardless of whether the user ever held Option on that annotation. | must | test |
| AC-74 | The Hierarchy section's source label reads 'uiautomator · {nodes.size} nodes' from the already-cached HierarchySnapshotModel (MirrorViewModel.frozenHierarchy); it never triggers a new dump. | must | test |
| AC-75 | A hierarchy node is marked (red '#{annotationIndex}' badge, ink color) if and only if it is the result of NodeSnap.snapTarget for that annotation's rect; every other node renders in ink3 with no badge. | must | test |
| AC-76 | The crop red outline is positioned using exactly crop(annotationRect, 84, 72, 14, fixed=false, withRect=true)'s returned rect — never a differently-padded or unscaled rect. | must | test |
| AC-77 | Given zero staged annotations, the Payload tab renders only the literal empty-state copy ('Nothing staged. Draw a box and the payload builds here, exactly as Claude will get it.') — none of Prompt, Crops, Hierarchy or the footer render. | must | test |
| AC-78 | Token estimate (360 + count*140) and payload size ((1.6 + count*0.12) MB, one decimal) recompute synchronously as annotations are added/removed/edited, with no measurable lag (satisfies NFR-11's ~200ms budget trivially since both are O(1) arithmetic). | must | test |
| AC-79 | Clicking the Edit affordance toggles the prompt block into an editable text area (reusing the existing MultilineInput atom); the edited text, once changed, is what gets attached as the prompt instead of the auto-generated text. | should | test |
| AC-80 | When HierarchyStatus is Unavailable or Absent for the frozen frame, the Hierarchy section shows zero nodes and an explicit 'no hierarchy' state rather than an empty tree that looks like a bug, and the rest of the Payload tab (Prompt, Crops, footer) still renders normally. | must | demo |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-6 | Review payload before sending | happy | ["Open Payload tab", "Review generated prompt and token estimate", "Review crops grid", "Review hierarchy tree", "Check footer summary"] |
| SC-20 | Payload with zero staged annotations | edge | ["Freeze a frame with no annotations drawn", "Open the Payload tab", "Only the empty-state copy renders", "No Prompt/Crops/Hierarchy section or footer appears"] |
| SC-21 | Annotation with no matched hierarchy node | edge | ["Stage an annotation whose rect contains no hierarchy node within NodeSnap's slack", "Open Payload > Hierarchy", "No node carries that annotation's badge", "Open Payload > Crops: the crop still renders using the annotation's own rect for the outline"] |
| SC-22 | Prompt assembles correctly without any measurement | happy | ["Stage annotations without ever holding Option over any of them", "Open Payload > Prompt", "Each annotation line shows its comment (or 'no comment') with no finding text, and the prompt is otherwise well-formed"] |
| SC-23 | Freeze-time hierarchy dump was unavailable | error | ["Freeze a frame whose uiautomator dump failed or returned empty (HierarchyStatus.Unavailable)", "Open Payload > Hierarchy", "Section shows zero nodes with an explicit 'no hierarchy' state, not a silent empty tree", "Prompt, Crops and the footer still render normally"] |
| SC-24 | Edit the prompt before sending | happy | ["Open Payload tab with staged annotations", "Click the Edit affordance", "Prompt block becomes an editable text area", "Edited text becomes the prompt that would be attached to the send", "Add another annotation — the edited text is still there (no auto-clear)"] |

### definitionOfReady

- Preparation is human-approved with readyForPlan:true (satisfied 2026-10-17).
- The seven FINAL engineering decisions are recorded verbatim in this plan's constraints and architecture and are not re-litigated downstream.
- measure-mode is shipped and qa-ready, so HierarchySnapshotModel, HierarchyStatus, NodeSnap.snapTarget and projectMeasureNodes exist on main.
- The reuse set is identified against the registry: multiline-input, hierarchy-row, mirror-host, rectify-window-shell, side-panel-frame, theme tokens.
- The deviceLabel source is settled (derive at freeze from the already-selected device row's existing detail text, stripping the transport segment) so no new reduced field is needed.
- The duplicate-snap rule is ratified (lowest annotation index wins).
- frameId's display format is settled (bare counter, 'from frame 1' — no zero padding, no offset).

### definitionOfDone

- Every AC-13..AC-16 and AC-72..AC-80 is implemented and traced to at least one task and one test or named demo step.
- All pure logic lives in presentation-feature-mirror/core/payload/ as internal declarations; nothing verifiable is stranded in a @Composable or in RectifyWindow.kt.
- ./gradlew :presentation-feature-mirror:desktopTest is green, and ./gradlew :presentation-feature-mirror:build passes (cross-target compile + iosSimulatorArm64 run) before the work is declared done.
- No new commonTest backtick test name contains a comma or a colon.
- RectifyWindow.kt's diff is exactly one `if (tab == ShellTab.Payload)` branch plus one tabCounts entry.
- MirrorNotesTab.kt, SidePanelFrame.kt, SidebarTabs.kt, ShellTab and shortcutToTab are byte-unchanged.
- FrozenFrame's KDoc states the three-field admission rule ('captured at freeze, dies with the frame').
- Light and dark @Preview pairs exist for the populated tab, the empty state, and the no-hierarchy state.
- No new Gradle module, no version-catalog entry, no new Koin binding, no new MirrorUiState field, no new colour token.
- Registry deltas recorded for the orchestrator: components.json#hierarchy-row props gain `matched: Boolean = false`, components.json#mirror-host props widen to the four-arg content lambda, and a new components.json#mirror-payload-tab entry is added.

### successMetrics

- Token estimate and payload size shown in the UI always match what is actually assembled for the send — no displayed-vs-real drift (enforced today by both formulas having exactly one internal home the future send path must call).
- Every annotation's crop, caption and hierarchy badge stays index-consistent with the Notes tab list across edits and deletes.
- Opening the Payload tab on a ~400-node dump does not drop a visible frame, and dragging the panel edge stays smooth (no per-row projection work).
- Zero new public API outside MirrorPayloadTab and the one additive HierarchyRow parameter.

### architecture

#### approach

Two layers inside presentation-feature-mirror plus two one-line edits outside it. (1) An internal, Compose-free pure package core/payload/ holds every formula and transform the spec needs — prompt assembly, the 360+count*140 estimator, the MB/footer labels, the crop-window transform, the hierarchy-row projection, the status labels, and the format-version constant. (2) A new public composable source/mirror/MirrorPayloadTab.kt, sibling of the shipped MirrorNotesTab, renders Prompt / Crops / Hierarchy / footer from remember-keyed calls into that pure layer, behind an empty-state gate. State grows by three freeze-scoped FrozenFrame fields, one intent, and one private ViewModel counter; MirrorUiState gains nothing.

#### rationale

testing.md makes this the only shape that is actually verifiable in this repo: :compose-application has zero test wiring, no Compose UI-test harness exists, and desktopTest is the documented runner — so anything left inside a @Composable or in the window layer is untestable by construction. Putting every integer and every string in the pure layer turns the formula and format ACs (AC-72/73/74/75/76/77/78) into commonTest assertions, and leaves the composable as a pass-through with no arithmetic of its own, which is also the mitigation for the crop-placement risk. Canvas.drawImage(srcOffset, srcSize, dstOffset, dstSize) replaces the design's negative-margin CSS because Compose crops and scales in one call and the outline becomes one rect used twice, which cannot drift. The hierarchy arrives via the widened MirrorHost lambda because MirrorUiState's own KDoc deliberately keeps ~400-node dumps out of the reducer, and because re-resolving the ViewModel would depend on two call sites implicitly sharing a ViewModelStoreOwner. frameId/deviceLabel/promptOverride live on FrozenFrame because all three describe the pinned frame and must die with it automatically — the alternative's failure mode is a caption naming the previous frame.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Reduce hierarchy node rows into MirrorUiState | A real dump is ~400 nodes; putting them in reduced state pays a copy on every unrelated reduction and contradicts MirrorUiState's own documented rule that only the four-state HierarchyStatus verdict is reduced. FINAL decision 5 excludes it. |
| Re-resolve MirrorViewModel inside the Payload tab | Depends on two call sites implicitly sharing one ViewModelStoreOwner; silently yields a second ViewModel (and an empty hierarchy) the moment that stops being true. FINAL decision 5 excludes it. |
| Host the freeze counter in reduced state or increment it inside reduce | reduce's lambda can re-run against the winning value after losing a race, so the counter could double-increment or reuse an id. FINAL decision 1 puts it in stageAnnotation's intent body. |
| Add a tokenizer dependency for an exact token count | A new dependency and a version-catalog entry for a number nothing yet verifies against real bytes. FINAL decision 3 ships 360+count*140, labelled '≈ tokens'. |
| Port the design's negative-margin CSS crop (ml/mt/scale on a full frame image) | Needs a clipping Box plus an offset plus a scale modifier on a full-size bitmap per cell, with the outline positioned by a second, independent calculation that can drift from the first. One drawImage with one shared rect cannot drift. |
| Mirror the payload footer into MirrorNotesTab too | SidePanelFrame's content slot is per-tab with no shared footer, so it would mean editing a shipped tab for no AC. FINAL decision 4 scopes the footer to the Payload tab. |
| Give the Payload tab its own hierarchy dump | The dump costs ~2.7 s measured and is already cached once per freeze; a second dump would be slow, could disagree with the measurement the user already saw, and AC-74 forbids it. FINAL decision 7. |
| Build the crop grid with LazyVerticalGrid | At most a dozen cells; a lazy grid adds nested-scroll ambiguity inside an already-scrolling panel for no gain. Fixed weighted Rows match the design's repeat(3, minmax(0,1fr)) exactly. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| PromptAssembly.kt (buildPrompt) | logic | new |  | presentation-feature-mirror/core/payload/ — internal. Assembles the whole prompt string: headline with the count==1 plural branch, the real device/resolution line, one '#{i} {nodeId} · {w}×{h}' + quoted comment (or 'no comment') block per annotation with NO 'measured:' branch, then the 'Attached: frame.png, N crops, hierarchy.xml' trailer. Reuses core/annotation's TIMES. FR-47, FR-52, AC-13, AC-72, AC-73. |
| PayloadEstimate.kt (tokenEstimate / payloadSizeLabel / footerLabel / oneDecimal / PAYLOAD_FORMAT_VERSION) | logic | new |  | core/payload/ — internal. The two FINAL heuristics plus the footer string and a hand-rolled one-decimal formatter (String.format is JVM-only; promotes core/stats' formatFps trick to a shared internal helper). Also the single home of PAYLOAD_FORMAT_VERSION = 1 for NFR-12. FR-48, FR-51, AC-16, AC-78. |
| CropWindow.kt (cropWindow + CropWindow result type) | logic | new |  | core/payload/ — internal. The design's crop() generalised off its hardcoded 296×640: clamps the padded source window to the real frame, computes scale = min(boxW/cw, boxH/ch), and returns srcOffset, srcSize, dstSize, scale and outlineRect (container coordinates). pad is DESIGN space, scaled by frameWidth/296 (resolved contradiction). The design's `fixed` flag is dropped — it is false at this call site. FR-49, AC-76. |
| HierarchyOutline.kt (payloadHierarchyRows + PayloadNodeRow) | logic | new |  | core/payload/ — internal. Runs the existing projectMeasureNodes + NodeSnap.snapTarget once per annotation, builds a node→lowest-annotation-index map, then maps snapshot.nodes exactly once into PayloadNodeRow(nodeId, depth, last, badge, matched). Single pass, no work per rendered row. FR-50, AC-15, AC-75, SC-21. |
| PayloadLabels.kt (hierarchySourceLabel + emptyHierarchy copy keys) | logic | new |  | core/payload/ — internal. The design's phase machine reduced to the four already-shipped HierarchyStatus values: Dumping → 'dumping…' (warnStillOff), Ready → 'uiautomator · {n} nodes', Absent/Unavailable → the explicit no-hierarchy label. No new enum, no new status. FR-50, AC-74, AC-80. |
| MirrorPayloadTab | ui | new | components.json#mirror-payload-tab (to be added at execution) | presentation-feature-mirror/source/mirror/MirrorPayloadTab.kt — public, signature (state: MirrorUiState, hierarchy: HierarchySnapshotModel, onIntent: (MirrorIntent) -> Unit, modifier: Modifier = Modifier). Empty-state gate first, then Prompt / Crops / Hierarchy / footer from remember-keyed pure calls. Private section composables (PayloadPromptSection, PayloadCropsSection, CropThumb, PayloadHierarchySection, PayloadFooter) live in the same file and are deliberately NOT registered. FR-12..15, FR-53, FR-54. |
| HierarchyRow | ui | reuse | components.json#hierarchy-row | Reused for every tree row; gains ONE additive parameter `matched: Boolean = false` to split ink (matched) from ink3 (unmatched) because it currently hardcodes Theme.color.ink. Its `tag` badge already defaults to Theme.color.annotate, so the badge needs no change. AC-15, AC-75. |
| MultilineInput | ui | reuse | components.json#multiline-input | The editable prompt area behind the Edit affordance, used as-is. AC-79. |
| MirrorHost | ui | reuse | components.json#mirror-host | Koin/Orbit boundary; its content lambda widens to (MirrorUiState, ImageBitmap?, HierarchySnapshotModel, (MirrorIntent) -> Unit) so the cached hierarchy reaches the panel without entering reduced state. One definition, one call site. FINAL decision 5. |
| RectifyWindowShell / SidePanelFrame / SidebarTabs | ui | reuse | components.json#rectify-window-shell | Consumed unchanged. Only the RectifyWindow.kt call site changes: one ShellTab.Payload branch in the sidePanel lambda plus ShellTab.Payload → mirror.stagedCount in tabCounts. |
| NodeSnap.snapTarget / MeasureProjection.projectMeasureNodes | logic | reuse |  | Module-internal measure-mode functions reused verbatim for badge matching — AC-75 requires the SAME function measure-mode uses, not a second containment rule. |
| AnnotationFormat (TIMES / metaLine) | logic | reuse |  | The single source of the unspaced × (U+00D7) and the dimension pattern; AC-72/AS-25 forbid a new formatter. |
| MirrorContract (FrozenFrame, MirrorIntent) | data | reuse |  | Extended: FrozenFrame gains frameId/deviceLabel/promptOverride; MirrorIntent gains EditPrompt(text). MirrorUiState gains nothing. FR-53, FR-55, AC-79. |
| MirrorViewModel | logic | reuse |  | Gains a private freezeCounter incremented in stageAnnotation's INTENT body (never in reduce) and stamped onto the new FrozenFrame alongside deviceLabel; gains one EditPrompt handler that reduces promptOverride. FINAL decision 1. |
| Theme tokens (color / typography) | ui | reuse | styles.json#theme-color | annotate / sentFixed / warnStillOff / ink / ink2 / ink3 / codeBg / line / stage and the existing mono type scale cover every element in the design table; no new token (AS-24). |

#### dataModel

| entity | fields |
| --- | --- |
| FrozenFrame (extended) | ["deviceSize: IntSize (existing)", "annotations: List<StagedAnnotation> (existing)", "selectedIndex: Int? (existing)", "frameId: Int — NEW, per-session sequential freeze counter stamped from MirrorViewModel's private counter at freeze time (FR-55)", "deviceLabel: String — NEW, the real device + Android version captured at freeze (FR-52)", "promptOverride: String? — NEW, the user's edited prompt; null means use the generated text (FR-53, AC-79)", "KDoc admission rule for any future field: 'captured at freeze, dies with the frame'"] |
| CropWindow (new, internal) | ["srcOffset: IntOffset — top-left of the clamped padded window in device pixels", "srcSize: IntSize — size of that window in device pixels", "dstSize: IntSize — container size after scaling (the design's bw/bh)", "scale: Float — min(boxW/cw, boxH/ch)", "outlineRect: IntRect — the annotation rect in container coordinates (the design's r.l/r.t/r.w/r.h); AC-76 pins this"] |
| PayloadNodeRow (new, internal) | ["nodeId: String — the label HierarchyRow renders", "depth: Int — indentation (node.depth)", "last: Boolean — tree-guide hint for HierarchyRow", "badge: String? — '#{index+1}' for the lowest-index annotation that snapped to this node, else null", "matched: Boolean — drives ink vs ink3 via HierarchyRow's new parameter"] |
| MirrorIntent.EditPrompt (new) | ["text: String — the full edited prompt, dispatched per change like CommentAnnotation"] |
| StagedAnnotation (unchanged) | ["index: Int", "rect: DeviceRect", "comment: String (already carries any ⌥-prefixed measurement line — AS-26)"] |
| HierarchySnapshotModel / HierarchyStatus (unchanged, consumed) | ["nodes: List<…> with depth", "HierarchyStatus = Absent \| Dumping \| Ready \| Unavailable"] |

#### interfaces

| name | contract |
| --- | --- |
| buildPrompt | internal fun buildPrompt(frozen: FrozenFrame, nodeIdFor: (StagedAnnotation) -> String?): String — pure; returns the generated prompt. Callers prefer frozen.promptOverride when non-null. |
| tokenEstimate / payloadSizeLabel / footerLabel | internal fun tokenEstimate(count: Int): Int = 360 + count * 140; internal fun payloadSizeLabel(count: Int): String = oneDecimal(1.6 + count * 0.12) + " MB"; internal fun footerLabel(count: Int): String = "frame.png · $count crops · hierarchy.xml". All O(1), all pure. |
| cropWindow | internal fun cropWindow(rect: DeviceRect, frameSize: IntSize, boxW: Int = 84, boxH: Int = 72, designPad: Int = 14, designWidth: Int = 296): CropWindow — pad scaled by frameSize.width / designWidth, window clamped to the frame on all four sides. |
| payloadHierarchyRows | internal fun payloadHierarchyRows(snapshot: HierarchySnapshotModel, annotations: List<StagedAnnotation>): List<PayloadNodeRow> — one snapTarget call per annotation, one pass over snapshot.nodes, lowest annotation index wins on a shared node. |
| hierarchySourceLabel | internal fun hierarchySourceLabel(status: HierarchyStatus, nodeCount: Int): PayloadHierarchyLabel — (text, isWarning) for Dumping / Ready / Absent\|Unavailable; never triggers a dump. |
| PAYLOAD_FORMAT_VERSION | internal const val PAYLOAD_FORMAT_VERSION: Int = 1 — declared in core/payload, rendered nowhere; the artifact NFR-12 asks for and the value the future send spec stamps into the bundle. |
| MirrorPayloadTab | @Composable public fun MirrorPayloadTab(state: MirrorUiState, hierarchy: HierarchySnapshotModel, onIntent: (MirrorIntent) -> Unit, modifier: Modifier = Modifier) |
| MirrorHost content lambda (widened) | content: @Composable (MirrorUiState, ImageBitmap?, HierarchySnapshotModel, (MirrorIntent) -> Unit) -> Unit |
| HierarchyRow (extended) | public fun HierarchyRow(nodeId: String, depth: Int, modifier: Modifier = Modifier, last: Boolean = false, tag: String? = null, matched: Boolean = false, onClick: () -> Unit = {}) — `matched` is additive and defaults to the current behaviour's colour being applied only when true (unmatched rows become ink3). |

#### stateManagement

Orbit MVI, unchanged in shape. Reduced (user-meaningful, must survive a tab switch): FrozenFrame.frameId, FrozenFrame.deviceLabel, FrozenFrame.promptOverride via MirrorIntent.EditPrompt. Not reduced: the read/edit toggle (remember-local transient view state) and every derived projection (prompt text, crop windows, node rows, labels — all remember-keyed on their real inputs). Outside reduced state entirely: the ~400-node HierarchySnapshotModel, which stays on MirrorViewModel.frozenHierarchy and reaches the tab through the widened MirrorHost lambda. The freeze counter is a private var on the ViewModel, incremented in stageAnnotation's intent body only. No new Koin binding, no new StateFlow, no new MirrorUiState field.

#### navigation

- No navigation change. The Payload tab is an existing ShellTab reached by the side panel's tab strip and the already-shipped shortcutToTab mapping; this plan only fills its content slot.

#### affectedModules

- presentation-feature-mirror (new core/payload/ package, new source/mirror/MirrorPayloadTab.kt, MirrorContract.kt, MirrorViewModel.kt, MirrorHost.kt, commonTest additions)
- presentation-core-ui (HierarchyRow.kt — one additive parameter plus its preview)
- compose-application (RectifyWindow.kt — one sidePanel branch, one tabCounts entry, and the widened MirrorHost call site)
- presentation-core-localisation (UI chrome strings: empty state, section labels, Edit, no-hierarchy copy)

### projectContext

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| multiline-input | MultilineInput | components.json#multiline-input |
| hierarchy-row | HierarchyRow | components.json#hierarchy-row |
| mirror-host | MirrorHost | components.json#mirror-host |
| mirror-notes-tab | MirrorNotesTab | components.json#mirror-notes-tab |
| side-panel-frame | SidePanelFrame | components.json#side-panel-frame |
| sidebar-tabs | SidebarTabs | components.json#sidebar-tabs |
| rectify-window-shell | RectifyWindowShell | components.json#rectify-window-shell |
| shell-state | ShellState / rememberShellState (ShellTab, shortcutToTab) | components.json#shell-state |

#### reuseAssets


#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| theme-color | ThemeColor (annotate, sentFixed, warnStillOff, ink, ink2, ink3, codeBg, line, stage) | styles.json#theme-color |
| theme-typography | ThemeTypography — the existing mono scale for 10.5 / 9.5 / 11.5 equivalents | styles.json#theme-typography |
| theme-spacing | ThemeSpacing / ThemeSpacingToken for the 8 / 10 / 14 / 16 gaps | styles.json#theme-spacing |
| theme-radius-corner | ThemeRadius / ThemeCorner for the 6 / 8 dp crop and prompt radii | styles.json#theme-radius-corner |
| theme-stroke | ThemeStroke for the 1px crop outline and prompt hairline | styles.json#theme-stroke |

#### existingFeatures

| id | name | registryRef |
| --- | --- | --- |
| drag-annotate-frozen-frame | Drag-to-annotate & frozen frame | features.json#drag-annotate-frozen-frame |
| app-shell-window-layout | App shell & canonical window layout | features.json#app-shell-window-layout |
| android-device-discovery-mirroring | Android device discovery & live mirroring | features.json#android-device-discovery-mirroring |
| localisation | Localisation (Compose Resources + runtime locale override) | features.json#localisation |
| measure-mode | Measure mode (shipped, qa-ready; supplies the cached hierarchy, NodeSnap and the ⌥ note lines) |  |

#### newToBuild

- presentation-feature-mirror/core/payload/PromptAssembly.kt (internal)
- presentation-feature-mirror/core/payload/PayloadEstimate.kt (internal, incl. PAYLOAD_FORMAT_VERSION and oneDecimal)
- presentation-feature-mirror/core/payload/CropWindow.kt (internal)
- presentation-feature-mirror/core/payload/HierarchyOutline.kt (internal)
- presentation-feature-mirror/core/payload/PayloadLabels.kt (internal)
- presentation-feature-mirror/source/mirror/MirrorPayloadTab.kt (public; private section composables inside)
- commonTest: PromptAssemblyTest, PayloadEstimateTest, CropWindowTest, HierarchyOutlineTest, PayloadLabelsTest, plus MirrorViewModelPayloadTest
- Registry additions/deltas (orchestrator-owned): components.json#mirror-payload-tab (new), components.json#hierarchy-row props += matched, components.json#mirror-host props widened

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md | Module map (presentation-feature-mirror, presentation-core-ui, compose-application, desktop-application), convention plugins, toolchain and the build commands this plan's verification step uses. |
| .claude/rules/package-layout.md | core/ is internal-only and source/ is public grouped per concern — the reason the pure payload layer is core/payload/ and only MirrorPayloadTab is public; explicitApi() polices the boundary. |
| .claude/rules/testing.md | kotlin.test is wired once by the convention plugins; :compose-application has no test wiring; desktopTest is the runner; a cross-target build is required before done; backtick names must avoid commas and colons; no Compose UI-test harness exists. |
| .claude/rules/platform-coverage.md | Android + iOS + Desktop-JVM targets all exist, so everything new must compile in commonMain for all three; any expect would need a desktopMain actual (this plan introduces none). |
| .claude/rules/orbit-mvi.md | Intent vs reduce semantics — the basis of FINAL decision 1 (increment the freeze counter in the intent body because reduce's lambda can re-run on a lost race) and of the EditPrompt handler's shape. |
| .claude/rules/design-tokens.md | Every colour, type size, spacing, radius and stroke must come from Theme.*; maps the design's RED/OK/WARN and ink/ink2/ink3/codeBg/line/stage to shipped tokens with no new token. |
| .claude/rules/compose-preview-convention.md | The light/dark @Preview pairs are this repo's only rendering verification for the populated tab, the empty state and the no-hierarchy state. |
| .claude/rules/koin-di.md | Confirms the deliberate absence of DI work: no new binding or module, since the feature adds no use case, repository or ViewModel. |
| .claude/rules/localisation.md | UI chrome strings are localised via presentation-core-localisation while machine output (prompt body, 'uiautomator · N nodes', footer, captions) stays unlocalised — the split measure-mode already shipped. |
| .claude/rules/error-handling.md | Frames the no-hierarchy state (AC-80) as an explicit state rather than a silent empty list, consistent with how the mirror feature surfaces other degraded states. |

#### skills


#### agents


#### docs

| ref | why |
| --- | --- |
| .pipeline/sdd/docs/payload-preview/inputs/design-payload-preview.md | Authoritative on copy, formulas, geometry and per-element tokens — wins over prose where they disagree. |
| .pipeline/sdd/docs/payload-preview/inputs/design-payload-source.md | Provenance by line range plus the recorded design gaps (notably the Agent summary row with no data source). |
| .pipeline/sdd/docs/payload-preview/raw/pages/screens.html | Primary design source; read only the Payload tab markup (~355-435) and the props builder (~760-830). Never open raw/rectify-screens.html (450KB). |
| .pipeline/sdd/docs/measure-mode/plan/plan.md | Precedent for task shape, test strategy and performance budgeting in the same module — read in small windows, never whole (156KB sibling JSON). |

### errorStates

| state | behavior |
| --- | --- |
| empty | Zero staged annotations: the Payload tab renders ONLY the literal copy 'Nothing staged. Draw a box and the payload builds here, exactly as Claude will get it.' (ink3, 12/18, padding 16×14). Prompt, Crops, Hierarchy and the footer are not composed at all — an early return, not hidden sections. FR-54, AC-77, SC-20. |
| loading | HierarchyStatus.Dumping: the Hierarchy section's source label reads 'dumping…' in Theme.color.warnStillOff with no tree rows; Prompt, Crops and the footer render normally. No spinner is invented — the design specifies a label, and the dump is fire-and-forget behind an already-visible frame. |
| error | HierarchyStatus.Unavailable or Absent: the Hierarchy section shows zero rows plus an explicit no-hierarchy line (ink3) stating no hierarchy was captured for this frame — never a silent empty tree. Prompt, Crops and the footer still render. AC-80, SC-23. |
| error | An annotation that snaps to no node: its prompt line omits the node id (and its badge is absent from the tree) while the crop still renders from the annotation's own rect. No placeholder node is fabricated. SC-21. |

### observability

#### logs

- No new logging. The hierarchy dump's success/failure is already logged by measure-mode at its source; duplicating it in a projection would log the same event twice per recomposition-free read.
- If a diagnostic is ever needed, the one worth adding is a single debug line at freeze recording frameId + node count — explicitly deferred, not silently dropped.

#### metrics


#### analyticsEvents


### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | The Edit affordance is a real clickable with a content description ('Edit prompt') and a minimum 24dp touch/click target, not a bare Text with a click modifier. |
| A11Y-2 | The prompt block is selectable text (the design's user-select: text) so a reporter can copy the prompt out; selection must survive the read-only mode. |
| A11Y-3 | The hierarchy badge's meaning is not colour-only: the badge carries the '#{index+1}' label, so the match is readable without distinguishing annotate-red from ink3. |
| A11Y-4 | The no-hierarchy and dumping states are conveyed as text, not solely by colour (warnStillOff vs ink3). |
| A11Y-5 | Every new text colour pairing comes from shipped Theme tokens already swept by PaletteContrastTest; no new pairing is introduced that would need a new baseline. |
| A11Y-6 | Crop cells expose their caption as the cell's accessible text so the grid is not an unlabelled image wall. |

### performanceBudget

| metric | target |
| --- | --- |
| Token estimate + payload size recomputation (NFR-11, AC-78) | O(1) integer/float arithmetic, well under 1ms — the ~200ms budget is met by construction, not by measurement. Asserted indirectly by the pure unit tests; no benchmark harness is added. |
| Hierarchy projection (payloadHierarchyRows) on a ~400-node dump | One snapTarget call per annotation (typically <12) plus one pass over ~400 nodes, computed once in remember(hierarchy, annotations) — under ~5ms, and never repeated on panel resize or recomposition. |
| Hierarchy tree rendering on a ~400-node dump | LazyColumn composes only the visible window (~20-30 rows at panel height), so first paint and each scroll frame stay inside the 16ms budget; a plain Column (MirrorNotesTab's precedent) is explicitly rejected here. |
| Panel drag-resize while the Payload tab is open | No measurable per-frame allocation from the payload layer: no projection, no prompt assembly and no crop computation may run inside a row loop or an unkeyed composable. |
| Crop grid (up to ~12 cells) | One Canvas.drawImage per cell against the already-decoded frozen bitmap; no bitmap copy, no per-cell decode, no ImageBitmap allocation. |
| Prompt assembly | O(annotations) string building, recomputed only when annotations, deviceLabel or hierarchy change (remember-keyed); never on every keystroke of the editor (the editor's text is reduced, the generated text is not reassembled while overridden). |

### i18n

#### locales

- en
- uk

#### strings

- payload_empty_state — 'Nothing staged. Draw a box and the payload builds here, exactly as Claude will get it.'
- payload_section_prompt — 'PROMPT'
- payload_section_crops — 'CROPS'
- payload_section_hierarchy — 'HIERARCHY'
- payload_edit — 'Edit'
- payload_tokens — '≈ %d tokens'
- payload_from_frame — 'from frame %d'
- payload_hierarchy_dumping — 'dumping…'
- payload_hierarchy_none — the explicit no-hierarchy line for Absent/Unavailable

#### notes

localisation.md's split applies: UI chrome above is localised through presentation-core-localisation; machine output stays unlocalised — the prompt body, 'uiautomator · N nodes', the footer's 'frame.png · N crops · hierarchy.xml', the 'N MB' label and the '#n · w×h' captions. Measure-mode set this precedent with its unlocalised ⌥ note line. The × (U+00D7) is locale-independent and comes from the one shared TIMES constant.

### security

#### dataHandling

- The prompt, crops and hierarchy all stay in memory inside the already-frozen frame's lifetime; this feature writes nothing to disk and sends nothing anywhere.
- The hierarchy dump can contain on-screen text from the device under test (labels, user content). Payload Preview renders it locally only, and the preview is exactly the point — the reporter sees what would leave the machine before anything does.
- promptOverride is user-authored text held on FrozenFrame and destroyed with the frame; it is never persisted to preferences or the database.

#### permissions

- None new. The adb/uiautomator access needed for the dump is already granted and exercised by measure-mode at freeze time.

#### privacy

- No analytics, no telemetry and no logging of prompt text, comments or node ids is added by this feature.
- Because the preview is the last stop before a send, it is the natural place a future send spec should surface a redaction affordance — noted, not built here.

### rollout

#### featureFlags


#### migrations

- No persisted data and no schema change. FrozenFrame is in-memory only, so its three new fields need no migration.
- FrozenFrame's new fields should be declared with defaults (frameId: Int = 0, deviceLabel: String = "", promptOverride: String? = null) so existing construction sites and the commonTest fakes in MirrorFakes.kt keep compiling unchanged.

#### backwardCompatibility

Additive throughout. HierarchyRow's new parameter is defaulted, so every existing call site (including the styleguide) is unaffected. MirrorHost's content lambda is the one breaking signature change, and it has exactly one production call site (RectifyWindow.kt) plus any test usage, all updated in the same task. MirrorNotesTab, SidePanelFrame, SidebarTabs, ShellTab and shortcutToTab are byte-unchanged, so the Notes tab and every keyboard shortcut behave exactly as shipped.

### knownRisksFromQA


### knownRisksFromQANote

bugs.json contains 0 entries, so there is nothing for this section to draw from. No QA-sourced risk is invented. The engineering risks for the touched areas (crop placement, ~400-node tree, FrozenFrame growth, edited-prompt loss, displayed-vs-real drift, token-mapping drift, window-layer creep, duplicate snap targets) are carried in `risks` with mitigations instead.

### implementationPlan

| step | title | dependsOn | linkedAC | estimate | files | notes |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Extend FrozenFrame and MirrorIntent; stamp frameId and deviceLabel at freeze | [] | ["AC-14", "AC-79"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt", "presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelPayloadTest.kt (new)", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt"] | Add frameId: Int = 0, deviceLabel: String = "", promptOverride: String? = null to FrozenFrame with the KDoc admission rule ('captured at freeze, dies with the frame'). Add MirrorIntent.EditPrompt(text: String) and one handler reducing frozen.promptOverride. CRITICAL (FINAL decision 1): a private var freezeCounter on MirrorViewModel is incremented in stageAnnotation's INTENT body, before reduce — never inside reduce, whose lambda can re-run against the winning value on a lost race. deviceLabel is derived at freeze from the already-selected device row's existing detail text with the transport segment stripped (no new reduced field). Tests: frameId increments once per freeze and not per annotation; a second drag against the same pinned frame keeps the same frameId; promptOverride survives an annotation add and a delete; promptOverride is dropped exactly once, on ResumeLive. |
| 2 | core/payload/PayloadEstimate.kt — token estimate, MB label, footer label, oneDecimal, PAYLOAD_FORMAT_VERSION | [] | ["AC-16", "AC-78"] | S | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/PayloadEstimate.kt (new)", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PayloadEstimateTest.kt (new)"] | tokenEstimate(count) = 360 + count*140 (FINAL, no tokenizer); payloadSizeLabel(count) = oneDecimal(1.6 + count*0.12) + ' MB'; footerLabel(count) = 'frame.png · N crops · hierarchy.xml'. oneDecimal is hand-rolled via roundToInt (String.format is JVM-only) and promoted as a shared internal helper rather than copied from core/stats' formatFps. Also declares internal const val PAYLOAD_FORMAT_VERSION = 1 — NFR-12's artifact, rendered nowhere. Tests cover count 0/1/2/12, the one-decimal rounding boundary (e.g. count where the second decimal rounds up), and that the labels are exact strings including the middots. |
| 3 | core/payload/PromptAssembly.kt — buildPrompt | [1] | ["AC-13", "AC-72", "AC-73"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/PromptAssembly.kt (new)", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PromptAssemblyTest.kt (new)"] | Ports the design's pl[] assembly exactly: headline with the count==1 singular/plural branch; the device line from frozen.deviceLabel + frozen.deviceSize (FR-52 — never the design's hardcoded literal); per annotation '#{i+1} {nodeId} · {w}×{h}' then the comment verbatim in quotes or 'no comment'; blank line between blocks; 'Attached: frame.png, N crops, hierarchy.xml' trailer. NO 'measured:' branch (AS-26/AC-73). Uses core/annotation's existing internal TIMES — no new formatter (AC-72). nodeId arrives as a (StagedAnnotation) -> String? so the hierarchy coupling stays at the call site. Tests: one and two annotations; empty comment → 'no comment'; a comment already carrying a ⌥ line passes through byte-identical; no node match omits the node id without breaking the line; × is U+00D7 and unspaced in the dimension pair. |
| 4 | core/payload/CropWindow.kt — design-space crop transform | [] | ["AC-76", "AC-14"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/CropWindow.kt (new)", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/CropWindowTest.kt (new)"] | cropWindow(rect, frameSize, boxW=84, boxH=72, designPad=14, designWidth=296) returning CropWindow(srcOffset, srcSize, dstSize, scale, outlineRect). pad is DESIGN space scaled by frameSize.width/designWidth (the resolved contradiction — NOT 14 literal device px); window clamped to [0, frameSize] on all four sides; scale = min(boxW/cw, boxH/ch); outlineRect is the annotation rect expressed in container coordinates, which is the single value AC-76 pins and which the composable reuses for the red outline. The design's `fixed` flag is dropped (false at this call site). Tests: a centred rect on a 1080-wide frame (hand-computed); each of the four edge clamps (x-pad<0, y-pad<0, right overflow, bottom overflow); pad scaling verified on two frame widths (296 → 14, 1080 → ~51); a rect larger than the box scales down, not up; outlineRect stays inside dstSize for every case. |
| 5 | core/payload/HierarchyOutline.kt + PayloadLabels.kt — node-row projection and status labels | [] | ["AC-15", "AC-74", "AC-75", "AC-80"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/HierarchyOutline.kt (new)", "presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/PayloadLabels.kt (new)", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/HierarchyOutlineTest.kt (new)", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PayloadLabelsTest.kt (new)"] | payloadHierarchyRows runs the EXISTING projectMeasureNodes + NodeSnap.snapTarget once per annotation (AC-75 demands the same function measure-mode uses, not a second containment rule), builds a node→lowest-annotation-index map, then maps snapshot.nodes in a single pass to PayloadNodeRow(nodeId, depth, last, badge, matched). Duplicate snap target: lowest annotation index wins, one badge, one row (ratified here). hierarchySourceLabel maps the four shipped HierarchyStatus values only: Dumping → 'dumping…' + warning flag, Ready → 'uiautomator · {n} nodes' (AC-74), Absent/Unavailable → the explicit no-hierarchy label (AC-80). No new enum, no new status, no dump triggered. Tests: badge lands on the snapped node and nowhere else; an annotation with no match produces no badge (SC-21); two annotations on one node yield one badge with the lower index; depth/last are passed through unchanged; label text for all four statuses including nodeCount 0. |
| 6 | HierarchyRow — one additive `matched: Boolean = false` parameter | [] | ["AC-15", "AC-75"] | S | ["presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/HierarchyRow.kt"] | HierarchyRow currently hardcodes Theme.color.ink for its label, so AC-75's ink/ink3 split needs this one additive, defaulted parameter: matched=true → Theme.color.ink, matched=false → Theme.color.ink3. The `tag` badge already defaults to Theme.color.annotate, so the badge is untouched. Update the existing preview to show both states in light and dark. Every existing call site compiles unchanged because of the default. Registry delta (orchestrator-owned): components.json#hierarchy-row props gain the parameter. |
| 7 | MirrorPayloadTab.kt — file skeleton, empty-state gate, and the Prompt section with the Edit toggle | [1, 2, 3] | ["AC-13", "AC-77", "AC-79"] | L | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt (new)", "presentation-core-localisation (string resources for the chrome listed in i18n.strings)"] | Public signature (state, hierarchy, onIntent, modifier) mirroring MirrorNotesTab. FIRST statement is the gate: if (state.frozen?.annotations.isNullOrEmpty()) { empty-state Text; return } so no section and no footer is composed at all (FR-54/AC-77/SC-20) — an early return, not a hidden branch. Prompt section: header row with 'PROMPT' (mono, ink3, letter-spaced), '≈ N tokens' right-aligned from tokenEstimate, and a clickable 'Edit' (ink2, >=24dp target, content description). Body is either read-only selectable mono Text on Theme.color.codeBg with a Theme.color.line hairline, 8dp radius, 10dp padding and a max height with scroll, OR — when the local remember toggle is on — the shipped MultilineInput seeded with the effective prompt, dispatching MirrorIntent.EditPrompt(text) per change. Effective prompt = frozen.promptOverride ?: remember(annotations, deviceLabel, hierarchy) { buildPrompt(...) }. The override persists until Edit is clicked again — no auto-clear when annotations change (FINAL decision 2). The toggle is remember-local; the text is reduced. |
| 8 | MirrorPayloadTab.kt — Crops section (3-column grid, drawImage crop, baked outline, captions) | [4, 7] | ["AC-14", "AC-72", "AC-76"] | L | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt"] | Header: 'CROPS' plus 'from frame {frameId}' right-aligned (bare counter — settled: no zero padding, no offset). Grid: chunked(3) weighted Rows with 8dp gaps (the design's repeat(3, minmax(0,1fr))); a short last row keeps its cells the same width via Spacer(Modifier.weight(1f)) fillers. Each cell is a 72dp-high Box on Theme.color.stage with a 6dp radius containing ONE Canvas that calls drawImage(image = frozenBitmap, srcOffset = cw.srcOffset, srcSize = cw.srcSize, dstOffset = IntOffset.Zero, dstSize = cw.dstSize) and then strokes a 1px Theme.color.annotate rect at cw.outlineRect — the SAME returned value, so the outline cannot drift from the crop (AC-76). The composable performs NO arithmetic of its own. Crop windows are computed once in remember(annotations, frozen.deviceSize). Caption '#{i+1} · {w}×{h}' uses the annotation rect's own raw pixel dimensions and the shared TIMES (AC-72), single line with ellipsis, mono 9.5-equivalent, ink3. |
| 9 | MirrorPayloadTab.kt — Hierarchy section (LazyColumn tree, badges, no-hierarchy state) | [5, 6, 7] | ["AC-15", "AC-74", "AC-75", "AC-80"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt"] | Header: 'HIERARCHY' plus hierarchySourceLabel(state.hierarchyStatus, hierarchy.nodes.size) right-aligned, warnStillOff when dumping and ink3 otherwise. Rows: LazyColumn with NO vertical arrangement spacing (HierarchyRow's indent guides only meet when rows are adjacent) over payloadHierarchyRows computed once in remember(hierarchy, annotations) — never per row. Each row is HierarchyRow(nodeId, depth, last = row.last, tag = row.badge, matched = row.matched). Absent/Unavailable: render zero rows plus the explicit no-hierarchy line (ink3) and let Prompt, Crops and footer render normally (AC-80, SC-23). Never call dumpHierarchy from here (AC-74). |
| 10 | MirrorPayloadTab.kt — Payload footer (Payload tab only) | [2, 7] | ["AC-16", "AC-78"] | S | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt"] | A bottom row inside the tab (not in SidePanelFrame, which has no shared footer slot): footerLabel(count) on the left, single line with ellipsis, and payloadSizeLabel(count) on the right, both mono 10.5-equivalent in ink3, separated by a Theme.color.line hairline above. Payload tab ONLY — MirrorNotesTab stays byte-unchanged (FINAL decision 4). Both strings come straight from PayloadEstimate so the footer cannot disagree with the Prompt section's count. |
| 11 | Wiring — widen MirrorHost's content lambda and add the one Payload branch in RectifyWindow.kt | [7, 8, 9, 10] | ["AC-13", "AC-14", "AC-15", "AC-16", "AC-80"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorHost.kt", "compose-application/src/desktopMain/kotlin/.../window/RectifyWindow.kt", "presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt"] | MirrorHost's content lambda becomes (MirrorUiState, ImageBitmap?, HierarchySnapshotModel, (MirrorIntent) -> Unit) -> Unit, collecting viewModel.frozenHierarchy as state (FINAL decision 5 — NOT reduced into MirrorUiState, NOT a second ViewModel resolution). RectifyWindow.kt's diff is held to exactly one `if (tab == ShellTab.Payload) MirrorPayloadTab(state, hierarchy, onIntent)` branch plus ShellTab.Payload → state.stagedCount in tabCounts, because :compose-application has no test wiring and anything else that lands there is permanently unverifiable. SidePanelFrame, SidebarTabs, ShellTab and shortcutToTab must come out of this task byte-unchanged. Registry delta (orchestrator-owned): components.json#mirror-host props widened. |
| 12 | Previews and the one-pass token mapping — populated / empty / no-hierarchy, light and dark | [8, 9, 10] | ["AC-14", "AC-16", "AC-72", "AC-80"] | M | ["presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt (preview block)"] | Map every element from the design's typography/colour table to an existing Theme token ONCE, at the top of this task, and record the mapping in a comment — the mitigation for the three-near-identical-mono-sizes risk. Then ship light/dark @Preview pairs for: the populated tab with two annotations at known positions, one of them edge-touching (this is the only way the crop placement risk gets a human check — AC-14/AC-76); the empty state (AC-77); and the Unavailable no-hierarchy state (AC-80's demo evidence). No new UI-test harness is introduced (testing.md). |
| 13 | Verification pass — desktopTest, cross-target build, registry deltas, DoD sweep | [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12] | ["AC-13", "AC-14", "AC-15", "AC-16", "AC-72", "AC-73", "AC-74", "AC-75", "AC-76", "AC-77", "AC-78", "AC-79", "AC-80"] | S | ["presentation-feature-mirror/src/commonTest/** (all new tests)", "no production files — verification only"] | Run ./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest first (desktop is the documented runner), then ./gradlew :presentation-feature-mirror:build :presentation-core-ui:build before declaring done — commonTest compiles for every target, Kotlin/Native is stricter than the JVM, and `build` additionally RUNS the iosSimulatorArm64 suite (a difference that has hidden a real failure in this module before via Orbit's process-wide dispatcher defaults). Audit every new backtick test name for a comma or a colon (both fail compileTestKotlinIosArm64; an apostrophe is fine). Then sweep the Definition of Done: RectifyWindow.kt diff is one branch + one tabCounts entry; MirrorNotesTab/SidePanelFrame/SidebarTabs/ShellTab/shortcutToTab unchanged; no new module, dependency, Koin binding, MirrorUiState field or colour token; and hand the three registry deltas (hierarchy-row props, mirror-host props, new mirror-payload-tab entry) to the orchestrator rather than editing the registry. |

### testStrategy

#### unit

- PayloadEstimateTest — tokenEstimate at count 0/1/2/12; payloadSizeLabel's one-decimal rounding including a boundary case; footerLabel's exact string with middots; PAYLOAD_FORMAT_VERSION == 1 (AC-16, AC-78).
- PromptAssemblyTest — singular vs plural headline; the real device line (never the design literal); comment verbatim including a pre-existing ⌥ measurement line; empty comment → 'no comment'; absent node id; the 'Attached:' trailer; × is U+00D7 and unspaced (AC-13, AC-72, AC-73).
- CropWindowTest — hand-computed centred case; all four edge clamps; pad scaling on two frame widths (296 → 14, 1080 → ~51); downscale-only behaviour; outlineRect always inside dstSize (AC-76).
- HierarchyOutlineTest — badge lands only on the snapTarget node; no match → no badge (SC-21); two annotations on one node → one badge with the lower index; depth/last passthrough; single pass over nodes (AC-15, AC-75).
- PayloadLabelsTest — 'uiautomator · N nodes' for Ready (incl. N = 0), 'dumping…' + warning flag for Dumping, the explicit no-hierarchy label for Absent and Unavailable (AC-74, AC-80).
- All of the above live in presentation-feature-mirror/src/commonTest and use plain kotlin.test, which the convention plugins already wire — no new harness, no new test dependency. Backtick names must contain no comma and no colon.

#### integration

- MirrorViewModelPayloadTest — frameId increments once per freeze (not per annotation) and a second drag keeps the same frameId; the increment happens in the intent body, so a simulated concurrent stage cannot double-stamp; EditPrompt reduces promptOverride; promptOverride survives an annotation add and a delete (no auto-clear); promptOverride is dropped exactly once, on ResumeLive (AC-79).
- Existing MirrorViewModelAnnotationTest / MirrorViewModelMeasureTest / MirrorContractTest must stay green — the regression gate on the three new FrozenFrame fields and the widened MirrorHost signature.
- MirrorFakes.kt gains a FrozenFrame fixture with the new fields defaulted, so no existing fixture changes shape.

#### ui

- No Compose UI-test harness exists in this repo and none is introduced (testing.md). Rendering is verified by light/dark @Preview pairs: populated tab with two annotations (one edge-touching) for the crop geometry, the empty state, and the Unavailable no-hierarchy state.
- AC-16 is verified by inspection against the footer formula, and AC-80 by the demo walk-through of SC-23 (freeze with a failed dump; Hierarchy shows the explicit state while Prompt, Crops and the footer render normally).
- Manual demo pass over SC-6, SC-20, SC-21, SC-22, SC-23 and SC-24 on a real Android device before sign-off.

#### linkedScenarios

- SC-6
- SC-20
- SC-21
- SC-22
- SC-23
- SC-24

#### coverageGoal

Every pure function in core/payload/ has direct commonTest coverage (buildPrompt, tokenEstimate, payloadSizeLabel, footerLabel, cropWindow, payloadHierarchyRows, hierarchySourceLabel), and both new state behaviours (frameId stamping, promptOverride lifecycle) have reducer-level tests. Composables are deliberately uncovered — they hold no arithmetic and no formatting of their own, which is exactly what makes that acceptable. Commands: ./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest for the fast loop, then ./gradlew :presentation-feature-mirror:build :presentation-core-ui:build (cross-target compile plus the iosSimulatorArm64 run) before declaring done.

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-13 | ["FR-12", "FR-47", "FR-48", "FR-52"] | ["PromptAssembly.kt (buildPrompt)", "PayloadEstimate.kt", "MirrorPayloadTab (PayloadPromptSection)"] | ["PromptAssemblyTest", "PayloadEstimateTest", "SC-6", "SC-22"] |
| AC-14 | ["FR-13", "FR-49", "FR-55"] | ["CropWindow.kt", "MirrorPayloadTab (PayloadCropsSection, CropThumb)", "FrozenFrame.frameId"] | ["CropWindowTest", "MirrorViewModelPayloadTest", "Preview (populated, light/dark)", "SC-6"] |
| AC-15 | ["FR-14", "FR-50"] | ["HierarchyOutline.kt", "PayloadLabels.kt", "HierarchyRow (reuse + matched)", "MirrorPayloadTab (PayloadHierarchySection)"] | ["HierarchyOutlineTest", "PayloadLabelsTest", "SC-6", "SC-21"] |
| AC-16 | ["FR-15", "FR-51"] | ["PayloadEstimate.kt (footerLabel, payloadSizeLabel)", "MirrorPayloadTab (PayloadFooter)"] | ["PayloadEstimateTest", "inspection against the formula", "SC-6"] |
| AC-72 | ["FR-47", "FR-49"] | ["AnnotationFormat (TIMES, reuse)", "PromptAssembly.kt", "MirrorPayloadTab (CropThumb caption)"] | ["PromptAssemblyTest", "existing AnnotationFormatTest", "SC-6"] |
| AC-73 | ["FR-47"] | ["PromptAssembly.kt (buildPrompt)"] | ["PromptAssemblyTest", "SC-22"] |
| AC-74 | ["FR-50"] | ["PayloadLabels.kt (hierarchySourceLabel)", "MirrorHost (widened lambda, reuse)"] | ["PayloadLabelsTest", "SC-6"] |
| AC-75 | ["FR-50"] | ["HierarchyOutline.kt (payloadHierarchyRows)", "NodeSnap.snapTarget (reuse)", "HierarchyRow matched param"] | ["HierarchyOutlineTest", "SC-21"] |
| AC-76 | ["FR-49"] | ["CropWindow.kt (outlineRect)", "MirrorPayloadTab (CropThumb Canvas)"] | ["CropWindowTest", "Preview (edge-touching annotation)"] |
| AC-77 | ["FR-54"] | ["MirrorPayloadTab (empty-state gate)"] | ["Preview (empty state)", "SC-20"] |
| AC-78 | ["FR-48", "FR-51"] | ["PayloadEstimate.kt", "MirrorPayloadTab (remember-keyed derivations)"] | ["PayloadEstimateTest", "performanceBudget (O(1) by construction)", "SC-6"] |
| AC-79 | ["FR-53"] | ["MirrorIntent.EditPrompt", "FrozenFrame.promptOverride", "MultilineInput (reuse)", "MirrorPayloadTab (PayloadPromptSection)"] | ["MirrorViewModelPayloadTest", "SC-24"] |
| AC-80 | ["FR-50", "FR-14"] | ["PayloadLabels.kt", "MirrorPayloadTab (PayloadHierarchySection no-hierarchy branch)"] | ["PayloadLabelsTest", "Preview (Unavailable)", "SC-23 (demo)"] |

### decisionsSettledInThisPlan

- frameId display format: a bare counter — 'from frame 1'. No zero padding and no offset to imitate the design's five-digit example; FR-55 only asks for a sequential counter and a padded fake id would be a lie with extra steps.
- NFR-12's artifact: internal const val PAYLOAD_FORMAT_VERSION = 1 in core/payload/PayloadEstimate.kt, rendered nowhere, beside the two heuristics the send spec must also replace. This is the placement preparation recommended and this plan confirms it rather than deferring NFR-12 with no artifact.
- deviceLabel source: derived at freeze from the already-selected device row's existing detail text with the transport segment stripped. No new reduced field (selectedDeviceAndroidVersion) is added.
- Two annotations snapping to the same hierarchy node: lowest annotation index wins — one badge, one row, no merged badge. Ratified here and asserted by HierarchyOutlineTest.
- The design's Frame / Hierarchy / Agent payload summary rows beyond the footer line stay unbuilt: no AC covers them and the Agent row has no data source in this repo. Flagged so no executor silently adds them from the design literal.
- Real tokenizer integration stays out of scope — 360 + count*140 is FINAL for this spec, labelled '≈ tokens'.

### openQuestions

- The send feature (not yet specified) owns replacing both heuristics — the token estimate and the MB size — with values measured against the real bytes, and owns stamping PAYLOAD_FORMAT_VERSION into the bundle. Until it exists, the footer is honest about the formula and unverifiable about reality; this is recorded as a tracked debt, not a blocker.
- The design's 'sent ✓' / 'on send' hierarchy states have no owner in this spec because nothing sends yet; they should be added by the send spec rather than invented as new HierarchyStatus values here.

### proposedSurvey


### readyForBacklog

true
