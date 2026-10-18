# measure-mode — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | done |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-16T17:13:22Z |
| updatedAt | 2026-10-16T17:18:33Z |

## Gate

### type

human

### approved

true

## Provenance

### selfHash

1c524b117a9c2657dcfc33f11d40a6e4d4c814c60ae84dfd2f6a29ce39a78cbb

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 14ec31f1aacba4b0ad7a6ccb07bfdf222efc2a533ca7a39f205003450534d014 |

## Data

### overview

Option-Key Measure Mode (PRD F-5): holding Option/Alt over a staged annotation on a frozen frame snaps to the matched Android view-hierarchy node (dumped once at freeze time, cached in memory), draws dashed neighbour guides, and shows dp/pixel gap measurements plus a highlighted misalignment finding in a floating 'Measuring' panel. On release, the measurement summary is appended to that annotation's note, prefixed with the Option glyph, so the agent payload carries exact numbers instead of a prose estimate. Android only; iOS is out of scope. Builds on F-4 (drag-annotate-frozen-frame)'s frozen frame, staged annotations and pointerInput layer.

### problemStatement

No bug-report-to-agent tool in the PRD's research has hierarchy-snapped measurement attached to the report itself — the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path, so a developer still has to eyeball spacing and type a guess into the note.

### purpose

Let a developer hold Option over an annotation and get the matched hierarchy node's exact dimensions, neighbour gaps and a misalignment/touch-target finding attached straight to the annotation note, so the report the agent receives carries numbers instead of an estimate.

### goals

- Resolve the Option-down -> panel-visible measurement (snap + neighbour + gap + finding) in under ~100ms using an already-cached hierarchy, per NFR-2.
- Degrade clearly (not silently) to a 'View (no match)' fallback with full measurements when no hierarchy node matches the annotation box.
- Attach exact measurement data to the annotation note on Option release so the sent payload is numeric, not prose.

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-10 | Given a frozen frame with an annotation box, when the user holds Option over it, then the tool snaps to the matched view-hierarchy node and shows neighbor guide boxes with dp gap measurements in a 'Measuring' panel. | must | test | false |
| AC-11 | Given a measured element has misalignment (e.g. undersized hit target), then the offending dimension is highlighted in red and the note is prefixed with the Option-mode marker and carries the measurement data. | must | inspection | false |
| AC-57 | Given an annotation rect and a list of hierarchy nodes, the snap function selects the node fully contained within the annotation box with a 4px slack on every edge, and when multiple nodes qualify it picks the one with the largest w*h area (largest-area-first tie-break, not nearest-centre or first-found). | must | test | false |
| AC-58 | Given a snapped target node and the hierarchy list, the neighbour function returns, per side (left/right/top/bottom), the nearest non-overlapping node per the band-overlap + closest-edge rule in design-measure-mode.md: right-most-left, left-most-right, lowest-top, highest-bottom, each requiring the opposite-axis band overlap. | must | test | false |
| AC-59 | When a side has no neighbour, the gap function measures to the frame edge for that side (0 for left/top, FRAME_W/FRAME_H for right/bottom) instead of omitting the measurement. | must | test | false |
| AC-60 | A guide (dashed mid-axis line + numeral) is suppressed entirely when its computed gap is less than 1 unit; it is never rounded up and never shown as zero. | must | test | false |
| AC-61 | When the left neighbour's y differs from the target's y by more than 0 and at most 10px, the finding selector returns the top-misalignment finding ('top D{d}{unit} vs {id} - expected 0'), taking precedence even if a left-misalignment or touch-target condition is also true. | must | test | false |
| AC-62 | When no top-misalignment finding applies and the top neighbour's x differs from the target's x by more than 0 and at most 24px, the finding selector returns the left-misalignment finding ('left D{d}{unit} vs {id} - expected 0'). | must | test | false |
| AC-63 | When neither alignment finding applies and the target is a tappable node with height <32dp, the finding selector returns the touch-target finding ('height {h}{unit} - min touch 48{unit}'). | should | test | false |
| AC-64 | The finding selector never returns more than one finding per measure, regardless of how many of the three conditions (top misalignment, left misalignment, touch target) are independently true. | must | test | false |
| AC-65 | When no hierarchy node satisfies the snap containment test, the measure result reports node id 'View (no match)' and still contains full Size/Top/Right/Bottom/Left values, computed against the annotation's own rect as the target and the frame edges for missing neighbours. | must | test | false |
| AC-66 | Formatted output uses the multiplication sign U+00D7 for Size (never 'x' or ' X '), 'D{value}{unit}' with no space before the unit for deltas, and '({x}, {y})' parentheses for positions, per the design's own formatting table. | must | test | false |
| AC-67 | The floating Measuring panel renders at the design's geometry and copy (18px keycap, 'Measuring' + right-aligned index, mono node id in the measure-blue signal color, Size/Top/Right/Bottom/Left rows, red finding block, footer 'Numbers attach to the annotation and go into the prompt.') correctly in both light and dark themes. | should | demo | false |
| AC-68 | With the view hierarchy already resolved and cached from freeze time, the Option-down to panel-visible computation (snap + neighbour + gap + finding) completes in under ~100ms (NFR-2/NFR-13 scope), measured as a commonTest timing assertion over a representative hierarchy size. | must | test | false |
| AC-69 | A hierarchy dump that fails, times out, or returns empty at freeze time does not block or delay the freeze, and every subsequent measure in that session shows the 'View (no match)' fallback rather than crashing or hanging. | must | demo | false |
| AC-70 | On Option release, the active annotation's note field has the measurement summary appended, prefixed with the Option glyph and a space, and this is observable as a state change in the mirror feature's reducer without requiring a real key event in commonTest. | must | test | false |
| AC-71 | Measure mode activates only while the Option (macOS) / Alt (other hosts) key is held, detected via Compose's isAltPressed, over a staged or in-progress annotation box, and deactivates on key release. | should | demo | false |

### functionalRequirements

| id | text |
| --- | --- |
| FR-10 | While holding Option over an annotation box, enter Measure mode: snap to the matched underlying view-hierarchy node, show dashed guide boxes for neighboring views and dp/pixel gap measurements in a floating 'Measuring' panel, highlight misalignment in red, and attach the measurement data to that annotation's note (prefixed with the Option symbol). |
| FR-36 | Node snap: candidate nodes are those fully contained within the annotation box with a 4px slack on every edge; ties break by largest area first (width*height, descending). |
| FR-37 | Neighbour selection: for each side (left/right/top/bottom) pick the nearest non-overlapping node using the opposite-axis band-overlap test plus the closest-edge sort rule specified per side in design-measure-mode.md. |
| FR-38 | Gap computation: measure to the selected neighbour's facing edge, or to the frame edge (0, or FRAME_W/FRAME_H) when no neighbour exists on that side. |
| FR-39 | Guide rendering: draw a 1px dashed guide line plus a centred numeral at the target's mid-axis for each side's gap, suppressed entirely (no line, no numeral) when that gap is less than 1 unit. |
| FR-40 | Finding selection: evaluate in strict order -- (1) top misalignment vs the left neighbour within +/-10px, (2) left misalignment vs the top neighbour within +/-24px, (3) touch-target height under 32dp -- first match wins; at most one finding is ever shown. |
| FR-41 | No-match fallback: when no node satisfies the snap containment test, measure against the annotation's own rect as the target, label the node 'View (no match)', and still produce full Size/Top/Right/Bottom/Left measurements using the same neighbour/gap pipeline (frame-edge gaps where no neighbour exists). |
| FR-42 | Panel placement: the Measuring panel renders as a floating card over the stage at the design's geometry (position top:16px/left:16px, 228px wide, 10px radius), reusing the existing MeasureCard organism. |
| FR-43 | On Option release, append the measurement summary to the active annotation's note field, prefixed with the Option glyph and a space (e.g. 'Option top D6dp vs item_title'). |
| FR-44 | Measure mode activates while the Option/Alt key (Compose's isAltPressed) is held during pointer hover/drag over a staged or in-progress annotation box on the frozen frame; MirrorAnnotationStage's existing onPreviewKeyEvent gains an isAltPressed branch alongside its current isMetaPressed check. |
| FR-45 | Format all measure-mode numeric text per the design table: dimensions as '{w}x{h}{unit}' using the U+00D7 multiplication sign, deltas as 'D{value}{unit}' with no space before the unit, and positions as '({x}, {y})'. |
| FR-46 | Extend the F-4 freeze path to dump the view hierarchy once per freeze and cache it in memory alongside the frozen frame; a failed or empty dump yields an empty hierarchy list rather than blocking the freeze, driving the FR-41 fallback for every measure in that session. |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-2 | performance | Measure-mode hierarchy-node lookup on Option-hover should resolve in under ~100ms so the measuring panel feels instantaneous. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-13 | performance | NFR-2's <~100ms budget applies specifically to the Option-down -> panel-visible path (snap + neighbour + gap + finding computation against the already-cached in-memory hierarchy); the freeze-time uiautomator dump (measured at 2665ms for a screenshot alone on the project's reference device) is explicitly excluded from this budget -- that cost lands on the freeze the user already initiated, not on Option-down. |
| NFR-14 | reliability | A failed, timed-out, or empty hierarchy dump at freeze time must not block or delay the freeze; it degrades to an empty cached hierarchy, which in turn degrades every measure in that session to the FR-41 no-match fallback. |

### outOfScope

- iOS measure support -- no view-hierarchy dump equivalent exists for iOS in this codebase; Android's adb/uiautomator path is the only hierarchy source this spec builds.
- A new Compose UI-test harness -- this repo has none (testing.md); visual/geometry acceptance criteria (AC-67, AC-69, AC-71) stay manual (demo), not automated.
- Simulator/mock hierarchy fixtures for production builds -- a dev/test-only concern, not a shipped data source.
- Literal macOS-only Option-key special-casing -- the spec uses Compose's isAltPressed uniformly across desktop hosts (see AS-21) rather than gating by OS.

### constraints

| type | text |
| --- | --- |
| tech | Android only: the hierarchy dump source is 'adb shell uiautomator dump', which has no iOS or desktop-native equivalent in this codebase. |
| tech | No Compose UI-test harness exists in this repo (testing.md) -- visual/geometry acceptance criteria are verified by manual run (demo), not an automated assertion. |
| tech | All new colors/spacing/typography must come from Theme.* tokens (design-tokens.md); MeasureCard/MeasureMark/MeasureTag already carry the needed tokens, including the already-existing monoTag/monoRuler (see AS-20) -- no new token additions are expected for this spec. |
| tech | MirrorAnnotationStage's feature logic follows Orbit MVI via MirrorViewModel/MirrorContract (orbit-mvi.md); measure state and the note-append on Option release must go through MirrorIntent/MirrorContract's reducer, not local Compose state. |

### assumptions

| id | text |
| --- | --- |
| AS-18 | The view hierarchy is dumped exactly once, at freeze time (extending F-4's freeze step), and cached in memory alongside the frozen frame for that frame's lifetime; Option-down reads this in-memory cache only and never triggers a new dump. |
| AS-19 | Measure mode ships Android-only in this spec; iOS has no view-hierarchy-dump equivalent wired and is explicitly out of scope (see outOfScope). |
| AS-20 | Theme.typography.monoTag (9.5sp/13sp line height, Medium weight, JetBrains Mono) and Theme.typography.monoRuler (8.5sp/12sp line height, Normal weight, JetBrains Mono) already exist and match the design spec exactly -- verified by reading FontSize.kt/LineHeight.kt/Typography.kt in presentation-core-styling; no new typography tokens are needed. |
| AS-21 | Compose Multiplatform exposes isAltPressed uniformly across platforms in the same androidx.compose.ui.input.key package as the already-imported (but unused) isMetaPressed in MirrorAnnotationStage.kt, and this project uses it as the single Option/Alt detection signal rather than special-casing macOS. |

### dependencies

| on | note |
| --- | --- |
| drag-annotate-frozen-frame | Measure mode extends F-4's freeze path with a hierarchy-dump step (FR-46), and reuses its FrozenFrame/StagedAnnotation state, FrameGeometry device-pixel<->Dp conversion, and MirrorAnnotationStage's pointerInput/onPreviewKeyEvent layer. |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-5 | Measure a misaligned element | happy | ["Hold Option over an annotation box", "View snaps to matched hierarchy node", "Neighbor guide boxes and dp gaps render", "Misalignment highlighted red and attached to the note"] |
| SC-16 | Measure an annotation with no matching hierarchy node | edge | ["Hold Option over an annotation box whose rect contains no fully-contained hierarchy node", "Panel shows node id 'View (no match)'", "Size/Top/Right/Bottom/Left are still computed against the annotation rect and frame edges", "No finding is shown unless the fallback rect itself is undersized"] |
| SC-17 | Release persists the measurement to the note | happy | ["Hold Option over an annotation and view the Measuring panel", "Release Option", "The annotation's note gains a line prefixed with the Option glyph summarizing the measurement", "Sending the report includes the exact numbers instead of a prose estimate"] |
| SC-18 | Touch-target finding on an undersized tappable element | edge | ["Hold Option over a tappable element matched to a node under 32dp tall", "No top or left misalignment is present", "The panel shows the finding 'height {h}{unit} - min touch 48{unit}' in red"] |
| SC-19 | Freeze-time hierarchy dump fails | error | ["User freezes the frame", "The uiautomator dump fails or returns empty", "The freeze completes anyway with an empty cached hierarchy", "Every measure during that session falls back to 'View (no match)'"] |

### successMetrics

- Option-down to panel-visible render consistently completes under ~100ms given an already-cached hierarchy (NFR-2/NFR-13).
- Every sent annotation whose note contains an Option-prefixed line carries numeric values, not a prose estimate -- the PRD's stated reason for building this feature.

### openQuestions

- Does a re-freeze (new frame capture) re-dump the hierarchy, or does the cached hierarchy from a prior freeze persist across multiple freezes of the same session? Not addressed by the design spec or the freeze-time decision.
- iOS measure-mode parity timeline is undecided -- out of scope here but likely a future PRD item once an iOS hierarchy-dump equivalent exists.
- Should the appended note line be localized, or does it stay literal/unlocalized like CommandBlock's machine output (localisation.md's convention)? Not decided for this spec.

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Where does the Measuring panel render? | ["Float over the stage (design-accurate: position top:16px/left:16px, reuses MeasureCard as-is with zero new wiring container)", "Integrate into the side-panel Notes tab (less intrusive, mirrors F-4's annotation pattern)"] | Float over the stage (design-accurate: position top:16px/left:16px, reuses MeasureCard as-is with zero new wiring container) | MeasureCard already ships as a self-contained widthIn(max=360.dp) card with zero callers and no assumption about a host container, and the design's own geometry is an absolute-positioned overlay. StageSurface's content slot already hosts a floating affordance the same way (CommentPopover). Side-panel integration would require a new SidePanelFrame tab and layout change nothing in the input asks for. |
| What happens to the measurement data on Option release? | ["Persist to the annotation note, prefixed with the Option glyph (per AC-11 wording and the design's footer copy)", "Display-only -- panel closes on release, no note write"] | Persist to the annotation note, prefixed with the Option glyph (per AC-11 wording and the design's footer copy) | AC-11 explicitly states the note 'is prefixed with the Option-mode marker and carries the measurement data,' and the design's footer copy is 'Numbers attach to the annotation and go into the prompt' -- persistence is the stated purpose of the feature (exact numbers instead of prose estimates). |
| How is the Option/Alt key detected across hosts? | ["Use Compose's isAltPressed uniformly on every desktop host, labelled with the Option glyph in UI copy regardless of OS", "Detect literally: Option only on macOS via an explicit platform check, no trigger on Windows/Linux desktop hosts"] | Use Compose's isAltPressed uniformly on every desktop host, labelled with the Option glyph in UI copy regardless of OS | isAltPressed lives in the same androidx.compose.ui.input.key package as the isMetaPressed already imported (but unused) in MirrorAnnotationStage.kt, giving one code path instead of a platform branch. The desktop target builds for whatever OS it's compiled on (platform-coverage.md), so a macOS-only gate would silently disable the feature on a non-macOS desktop build for no reason stated in the input. |
| What are the neighbours/guides when Measure mode falls back to 'View (no match)'? | ["Compute neighbours/gaps against the frame edges using the annotation's own rect as the target (same pipeline, no special-casing)", "Skip neighbours/guides entirely in the fallback case -- show Size only, no Top/Right/Bottom/Left"] | Compute neighbours/gaps against the frame edges using the annotation's own rect as the target (same pipeline, no special-casing) | design-measure-mode.md states the fallback 'still produces full measurements,' and AC-11 requires degrading 'clearly,' not degrading to a partial panel. Treating the annotation rect as the target and running the existing gap-to-frame-edge path (already specified for 'no neighbour on a side') needs no new branch -- it's the same function with every neighbour slot empty. |
