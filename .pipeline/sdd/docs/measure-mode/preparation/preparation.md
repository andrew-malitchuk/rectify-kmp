# measure-mode — preparation

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | preparation |
| status | done |
| producedBy | sdd-build-preparation-compile |
| schemaVersion | 1 |
| createdAt | 2026-10-16T17:38:40Z |
| updatedAt | 2026-10-16T18:00:42Z |

## Gate

### type

human

### approved

true

## Provenance

### selfHash

927a20398b5a29dfd69612482d90fb1873aa653b32ddd7175548525ebbd20787

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 14ec31f1aacba4b0ad7a6ccb07bfdf222efc2a533ca7a39f205003450534d014 |
| preparation/clarify.json | 1c524b117a9c2657dcfc33f11d40a6e4d4c814c60ae84dfd2f6a29ce39a78cbb |
| preparation/brainstorm.json | f927d09add206871e310cd7d9b9b257de3bf00dd0df87927ad274b6d4a038fd3 |

## Data

### summary

Option-Key Measure Mode (PRD F-5): holding Option/Alt over a staged annotation on a frozen frame snaps to the matched Android view-hierarchy node (dumped once at freeze time, cached in memory), draws dashed neighbour guides, and shows dp/pixel gap measurements plus a highlighted misalignment finding in a floating 'Measuring' panel. On release, the measurement summary is appended to that annotation's note, prefixed with the Option glyph. Android only; iOS out of scope. Builds on F-4 (drag-annotate-frozen-frame)'s frozen frame, staged annotations and pointerInput layer. All three Stage 1 artifacts (input, clarify, brainstorm) are human-approved; this deliverable consolidates them for Stage 2 planning sign-off.

### problemStatement

No bug-report-to-agent tool in the PRD's research has hierarchy-snapped measurement attached to the report itself -- the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path, so a developer still has to eyeball spacing and type a guess into the note.

### confirmedRequirements

#### functional

| id | text | stage2Note |
| --- | --- | --- |
| FR-10 | While holding Option over an annotation box, enter Measure mode: snap to the matched underlying view-hierarchy node, show dashed guide boxes for neighboring views and dp/pixel gap measurements in a floating 'Measuring' panel, highlight misalignment in red, and attach the measurement data to that annotation's note (prefixed with the Option symbol). |  |
| FR-36 | Node snap: candidate nodes are those fully contained within the annotation box with a 4px slack on every edge; ties break by largest area first (width*height, descending). |  |
| FR-37 | Neighbour selection: for each side (left/right/top/bottom) pick the nearest non-overlapping node using the opposite-axis band-overlap test plus the closest-edge sort rule specified per side in design-measure-mode.md. |  |
| FR-38 | Gap computation: measure to the selected neighbour's facing edge, or to the frame edge (0, or FRAME_W/FRAME_H) when no neighbour exists on that side. |  |
| FR-39 | Guide rendering: draw a 1px dashed guide line plus a centred numeral at the target's mid-axis for each side's gap, suppressed entirely (no line, no numeral) when that gap is less than 1 unit. |  |
| FR-40 | Finding selection: evaluate in strict order -- (1) top misalignment vs the left neighbour within +/-10px, (2) left misalignment vs the top neighbour within +/-24px, (3) touch-target height under 32dp -- first match wins; at most one finding is ever shown. |  |
| FR-41 | No-match fallback: when no node satisfies the snap containment test, measure against the annotation's own rect as the target, label the node 'View (no match)', and still produce full Size/Top/Right/Bottom/Left measurements using the same neighbour/gap pipeline (frame-edge gaps where no neighbour exists). |  |
| FR-42 | Panel placement: the Measuring panel renders as a floating card over the stage at the design's geometry (position top:16px/left:16px, 228px wide, 10px radius), reusing the existing MeasureCard organism. |  |
| FR-43 | On Option release, append the measurement summary to the active annotation's note field, prefixed with the Option glyph and a space (e.g. 'Option top D6dp vs item_title'). |  |
| FR-44 | Measure mode activates while the Option/Alt key (Compose's isAltPressed) is held during pointer hover/drag over a staged or in-progress annotation box on the frozen frame; MirrorAnnotationStage's existing onPreviewKeyEvent gains an isAltPressed branch alongside its current isMetaPressed check. | FOLLOW-UP: operator decision 7 narrows this wording -- Alt during an in-progress drag is ignored until the drag ends, to preserve the one-reduce-per-gesture rule. The text above still reads 'staged or in-progress' verbatim from clarify.json; Stage 2 planning must either reword FR-44 to drop 'or in-progress' or add an explicit guard clause, rather than silently reinterpreting it. |
| FR-45 | Format all measure-mode numeric text per the design table: dimensions as '{w}x{h}{unit}' using the U+00D7 multiplication sign, deltas as 'D{value}{unit}' with no space before the unit, and positions as '({x}, {y})'. |  |
| FR-46 | Extend the F-4 freeze path to dump the view hierarchy once per freeze and cache it in memory alongside the frozen frame; a failed or empty dump yields an empty hierarchy list rather than blocking the freeze, driving the FR-41 fallback for every measure in that session. |  |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-2 | performance | Measure-mode hierarchy-node lookup on Option-hover should resolve in under ~100ms so the measuring panel feels instantaneous. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-13 | performance | NFR-2's <~100ms budget applies specifically to the Option-down -> panel-visible path (snap + neighbour + gap + finding computation against the already-cached in-memory hierarchy); the freeze-time uiautomator dump (measured at 2665ms for a screenshot alone on the project's reference device) is explicitly excluded from this budget -- that cost lands on the freeze the user already initiated, not on Option-down. |
| NFR-14 | reliability | A failed, timed-out, or empty hierarchy dump at freeze time must not block or delay the freeze; it degrades to an empty cached hierarchy, which in turn degrades every measure in that session to the FR-41 no-match fallback. |

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed | stage2Note |
| --- | --- | --- | --- | --- | --- |
| AC-10 | Given a frozen frame with an annotation box, when the user holds Option over it, then the tool snaps to the matched view-hierarchy node and shows neighbor guide boxes with dp gap measurements in a 'Measuring' panel. | must | test | false |  |
| AC-11 | Given a measured element has misalignment (e.g. undersized hit target), then the offending dimension is highlighted in red and the note is prefixed with the Option-mode marker and carries the measurement data. | must | inspection | false |  |
| AC-57 | Given an annotation rect and a list of hierarchy nodes, the snap function selects the node fully contained within the annotation box with a 4px slack on every edge, and when multiple nodes qualify it picks the one with the largest w*h area (largest-area-first tie-break, not nearest-centre or first-found). | must | test | false |  |
| AC-58 | Given a snapped target node and the hierarchy list, the neighbour function returns, per side (left/right/top/bottom), the nearest non-overlapping node per the band-overlap + closest-edge rule in design-measure-mode.md: right-most-left, left-most-right, lowest-top, highest-bottom, each requiring the opposite-axis band overlap. | must | test | false |  |
| AC-59 | When a side has no neighbour, the gap function measures to the frame edge for that side (0 for left/top, FRAME_W/FRAME_H for right/bottom) instead of omitting the measurement. | must | test | false |  |
| AC-60 | A guide (dashed mid-axis line + numeral) is suppressed entirely when its computed gap is less than 1 unit; it is never rounded up and never shown as zero. | must | test | false |  |
| AC-61 | When the left neighbour's y differs from the target's y by more than 0 and at most 10px, the finding selector returns the top-misalignment finding ('top D{d}{unit} vs {id} - expected 0'), taking precedence even if a left-misalignment or touch-target condition is also true. | must | test | false |  |
| AC-62 | When no top-misalignment finding applies and the top neighbour's x differs from the target's x by more than 0 and at most 24px, the finding selector returns the left-misalignment finding ('left D{d}{unit} vs {id} - expected 0'). | must | test | false |  |
| AC-63 | When neither alignment finding applies and the target is a tappable node with height <32dp, the finding selector returns the touch-target finding ('height {h}{unit} - min touch 48{unit}'). | should | test | false |  |
| AC-64 | The finding selector never returns more than one finding per measure, regardless of how many of the three conditions (top misalignment, left misalignment, touch target) are independently true. | must | test | false |  |
| AC-65 | When no hierarchy node satisfies the snap containment test, the measure result reports node id 'View (no match)' and still contains full Size/Top/Right/Bottom/Left values, computed against the annotation's own rect as the target and the frame edges for missing neighbours. | must | test | false |  |
| AC-66 | Formatted output uses the multiplication sign U+00D7 for Size (never 'x' or ' X '), 'D{value}{unit}' with no space before the unit for deltas, and '({x}, {y})' parentheses for positions, per the design's own formatting table. | must | test | false |  |
| AC-67 | The floating Measuring panel renders at the design's geometry and copy (18px keycap, 'Measuring' + right-aligned index, mono node id in the measure-blue signal color, Size/Top/Right/Bottom/Left rows, red finding block, footer 'Numbers attach to the annotation and go into the prompt.') correctly in both light and dark themes. | should | demo | false |  |
| AC-68 | With the view hierarchy already resolved and cached from freeze time, the Option-down to panel-visible computation (snap + neighbour + gap + finding) completes in under ~100ms (NFR-2/NFR-13 scope), measured as a commonTest timing assertion over a representative hierarchy size. | must | test | false |  |
| AC-69 | A hierarchy dump that fails, times out, or returns empty at freeze time does not block or delay the freeze, and every subsequent measure in that session shows the 'View (no match)' fallback rather than crashing or hanging. | must | demo | false | AC-69 covers dump FAILURE only. Operator decision 8 (reduce a Dumping status during the ~2.6s pending window before the dump lands) has no corresponding acceptance criterion today -- see openQuestions for the Stage 2 follow-up to mint one. |
| AC-70 | On Option release, the active annotation's note field has the measurement summary appended, prefixed with the Option glyph and a space, and this is observable as a state change in the mirror feature's reducer without requiring a real key event in commonTest. | must | test | false |  |
| AC-71 | Measure mode activates only while the Option (macOS) / Alt (other hosts) key is held, detected via Compose's isAltPressed, over a staged or in-progress annotation box, and deactivates on key release. | should | demo | false |  |

### scope

#### in

- Android-only Option-key Measure mode: snap to matched view-hierarchy node, dashed neighbour guides, dp/pixel gap measurements, misalignment/touch-target finding, floating 'Measuring' panel (FR-10, FR-36..45, AC-10/11, AC-57..68, AC-70/71).
- One-time view-hierarchy dump at freeze time via 'adb shell uiautomator dump', cached in memory for that frame's lifetime, with never-throw parsing and a never-block-the-freeze guarantee (FR-46, NFR-13/14, AS-18, AC-69).
- Note persistence on Option release: measurement summary appended (and replaced on repeat holds) to the active annotation's note, prefixed with the Option glyph (FR-43, AC-70, SC-17).
- Reuse of existing zero-caller components (MeasureCard, MeasureMark, MeasureTag, monoTag/monoRuler typography) with no signature changes; reuse of F-4's FrozenFrame/StagedAnnotation state and FrameGeometry conversion (AS-20, FR-42).
- Uniform isAltPressed detection across every desktop host, no macOS-only special-casing (AS-21, FR-44, AC-71).
- A blocking hardware spike before Stage 4 parser/test work: capture 3 real uiautomator dump outputs from an attached device as commonTest fixtures (operator decision 10).

#### out

- iOS measure support -- no view-hierarchy dump equivalent exists for iOS in this codebase; Android's adb/uiautomator path is the only hierarchy source this spec builds.
- A new Compose UI-test harness -- this repo has none (testing.md); visual/geometry acceptance criteria (AC-67, AC-69, AC-71) stay manual (demo), not automated.
- Simulator/mock hierarchy fixtures for production builds -- a dev/test-only concern, not a shipped data source.
- Literal macOS-only Option-key special-casing -- the spec uses Compose's isAltPressed uniformly across desktop hosts (see AS-21) rather than gating by OS.
- Pausing the capture loop during the freeze-time dump -- the transient fps dip is accepted and measured via the transport's existing dropped-frame counter (operator decision 9).
- An on-device-file + 'cat' fallback for 'uiautomator dump' on hosts where '/dev/tty' output is unavailable (older API levels / some OEM builds) -- left unspecified pending the hardware spike's findings.

### constraints

| type | text |
| --- | --- |
| tech | Android only: the hierarchy dump source is 'adb shell uiautomator dump', which has no iOS or desktop-native equivalent in this codebase. |
| tech | No Compose UI-test harness exists in this repo (testing.md) -- visual/geometry acceptance criteria are verified by manual run (demo), not an automated assertion. |
| tech | All new colors/spacing/typography must come from Theme.* tokens (design-tokens.md); MeasureCard/MeasureMark/MeasureTag already carry the needed tokens, including the already-existing monoTag/monoRuler (see AS-20) -- no new token additions are expected for this spec. |
| tech | MirrorAnnotationStage's feature logic follows Orbit MVI via MirrorViewModel/MirrorContract (orbit-mvi.md); measure state and the note-append on Option release must go through MirrorIntent/MirrorContract's reducer, not local Compose state. |
| process | Operator decision 10 (10-item list, item 10): a blocking hardware spike -- capturing 3 real 'uiautomator dump' outputs from an attached device -- must land before the parser tests and px<->dp conversion are written, so Stage 4 planning needs a phone attached when it reaches that task. |

### assumptions

| id | text | stage2Note |
| --- | --- | --- |
| AS-18 | The view hierarchy is dumped exactly once, at freeze time (extending F-4's freeze step), and cached in memory alongside the frozen frame for that frame's lifetime; Option-down reads this in-memory cache only and never triggers a new dump. |  |
| AS-19 | Measure mode ships Android-only in this spec; iOS has no view-hierarchy-dump equivalent wired and is explicitly out of scope (see outOfScope). |  |
| AS-20 | Theme.typography.monoTag (9.5sp/13sp line height, Medium weight, JetBrains Mono) and Theme.typography.monoRuler (8.5sp/12sp line height, Normal weight, JetBrains Mono) already exist and match the design spec exactly -- verified by reading FontSize.kt/LineHeight.kt/Typography.kt in presentation-core-styling; no new typography tokens are needed. |  |
| AS-21 | Compose Multiplatform exposes isAltPressed uniformly across platforms in the same androidx.compose.ui.input.key package as the already-imported (but unused) isMetaPressed in MirrorAnnotationStage.kt, and this project uses it as the single Option/Alt detection signal rather than special-casing macOS. | FOLLOW-UP (risk): this assumption currently presumes Alt key-up always arrives. Brainstorm's risk analysis flags that onPreviewKeyEvent needs focus, the stage root does not currently request it, and Compose Desktop does not deliver key events to an unfocused window -- so a stuck Measuring panel with a stale readout is plausible. Mitigation (focus request on stage root + EndMeasure on focus-loss and next pointer-down) must be designed into Stage 2's plan, not left to AS-21's optimistic framing. |

### dependencies

| on | note |
| --- | --- |
| drag-annotate-frozen-frame | Measure mode extends F-4's freeze path with a hierarchy-dump step (FR-46), and reuses its FrozenFrame/StagedAnnotation state, FrameGeometry device-pixel<->Dp conversion, and MirrorAnnotationStage's pointerInput/onPreviewKeyEvent layer. |

### chosenApproach

Four layers, each already grooved by F-2/F-4, plus one new pure package. (1) ADB: add DeviceCommand.UiAutomatorDump to the closed sealed set with its own argv case and timeout budget; parse stdout with a hand-rolled, never-throwing commonMain scanner (parseUiAutomatorDump) rather than a JVM XML library, keeping the parser fixture-testable with no device and no desktopMain dependency. (2) Domain: HierarchySnapshotModel/HierarchyNodeModel (with an Empty sentinel) plus a new DeviceRepository.dumpHierarchy member and a DumpHierarchyUseCase through resultLauncher, reusing the already-shipped-but-unwired parseWmDensity path for densityDpi. (3) Pure measure logic lives in presentation-feature-mirror/core/measure/ (internal, Compose-free, Orbit-free): snapTarget, neighboursOf, gapsOf, findingOf, MeasureFormat, PixelUnits, composed by measure(...) -- the only layer this repo's commonTest harness can actually exercise for every design-measure-mode.md rule. (4) Orbit + render: the hierarchy snapshot sits on a third StateFlow beside frame/frozenFrame (machine output, never a reducer decision), filled fire-and-forget on the freeze transition; MirrorIntent.StartMeasure/EndMeasure run the measure() computation and the note-append through the reducer (two reduces per Option-hold), while the 60Hz hover/Alt-held state stays in `remember`, mirroring F-4's dragStart/dragCurrent discipline. Rendering reuses MeasureCard/MeasureMark/MeasureTag unchanged, plus one Canvas for the mid-axis guide lines, all positioned through the existing FrameGeometry.

### rationale

Every layer choice resolves to the option that keeps the feature inside this repo's only real verification surface (commonTest; no Compose UI-test harness, no Skiko on this host) without adding a dependency, a Gradle module, or a signature change to components that already ship. The domain-core alternative (a MeasureAnnotationUseCase) was rejected because it would churn F-4's confirmed DeviceRect contract and wrap pure arithmetic in a suspend/Result boundary inside a ~100ms budget nobody asked to widen; a javax.xml parser was rejected because it would force the parser into desktopMain and off the iosArm64 commonTest compile path, which data-device-api's module convention treats as load-bearing. Keeping measure state in Orbit (not plain Compose state) was mandated by clarify.json's own constraint and is also the only way AC-70's note-append is testable without a real key event.

### architectureOverview

Layer 1 (data-device-api/data-device-impl): DeviceCommand.UiAutomatorDump + buildDeviceArgv case -> ['exec-out','uiautomator','dump','/dev/tty'] (stdout only, no on-device file, no second adb pull), its own DeviceCommandExecutorImpl timeout budget, and parseUiAutomatorDump(stdout): List<HierarchyNodeResource> in source/parse/ -- flattens depth-first, reads bounds/class/resource-id/clickable, tolerates leading/trailing noise lines, never throws. Layer 2 (domain-core, domain-repository-api, data-repository-impl, domain-usecase-api/impl): HierarchyNodeModel/HierarchySnapshotModel(nodes, frameWidthPx, frameHeightPx, densityDpi) with an Empty sentinel; DeviceRepository.dumpHierarchy (new member, no new repository) maps resources via a one-direction Mapper and reuses parseWmDensity for density; DumpHierarchyUseCase returns Result<HierarchySnapshotModel> through resultLauncher. Layer 3 (presentation-feature-mirror/core/measure/, internal): pure functions snapTarget/neighboursOf/gapsOf/findingOf/MeasureFormat/PixelUnits/measure(...), no Compose or Orbit imports, mirroring the design's own executable measure(annotation, unit) spec as an end-to-end oracle. Layer 4 (Orbit + render): MirrorViewModel.frozenHierarchy StateFlow filled fire-and-forget on the freeze transition and cleared at the three existing currentFrozenFrame-null sites; MirrorContract gains MeasureReadout/MeasureValue/MeasureSide/MeasureGuide/HierarchyStatus, MirrorUiState.measure + hierarchyStatus, and MirrorIntent.StartMeasure/EndMeasure; MirrorAnnotationStage wires a root focusRequester + onPreviewKeyEvent Alt branch, hit-testing, a MeasureOverlay sibling layer above AnnotationOverlay, and the floating MeasureCard in the outer Box's TopStart slot -- the same slot the drop banner and Resume Live already use.

### techStack

| area | choice | reason |
| --- | --- | --- |
| Hierarchy acquisition | adb exec-out uiautomator dump /dev/tty, through the existing closed DeviceCommand set and ProcessRunner | The only hierarchy source available on Android from a desktop host without instrumenting the app under test; stdout avoids an on-device file plus a second adb pull, and the closed sealed set keeps NFR-5 enforced by type. |
| XML parsing | Hand-rolled tag/attribute scanner in data-device-api/commonMain, no new dependency | Keeps the parser where that module requires every parser to live (commonMain, hardware-free tests); the input has four attributes we care about and is machine-generated. A JVM XML parser would force the code into desktopMain and off the common test path. |
| Pure measure logic | Internal Kotlin functions in presentation-feature-mirror/core/measure/, commonMain | package-layout.md puts internal infrastructure in core/ and the public result types in source/; this is the only layer the repo's test harness can reach (no Compose UI tests, Skiko unavailable on this host). |
| State management | Orbit MVI for the measure readout and the note append; remember for the 60 Hz hover/Alt state; a StateFlow beside frame/frozenFrame for the cached hierarchy | Honours the confirmed Orbit constraint and AC-70 while preserving the module's documented rule that high-frequency and pixel-like data never enter the reducer. |
| Rendering | The three existing measure components + one Compose Canvas for guides, all positioned via FrameGeometry | FR-42 and AC-67 are satisfiable with zero component changes; the single geometry type prevents the density-division bug class its own KDoc records. |
| Units | Device pixels for all geometry and tolerances; dp only at formatting, from wm density; MeasureUnit.Px when density is unknown | The design's algorithm is defined on device-pixel rects and its +/-10/+/-24 tolerances are stated in px, while its output table is in dp -- keeping the boundary at the formatter is the only way to satisfy both. |
| Testing | kotlin.test in commonTest only (per-rule unit tests + the design's ELS fixture as an end-to-end oracle + a timing assertion), desktopTest to run, compileTestKotlinIosArm64 as the gate | testing.md: kotlin.test is wired once by the convention plugins, commonTest compiles for every target, and backtick names must avoid commas and colons. No new harness, no UI test framework. |
| DI | Koin, one new use case binding in the existing device module; no new Koin module | koin-di.md and the existing MirrorViewModel constructor pattern -- the use case is just another constructor parameter. |

### openRisks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| The dump is the whole feature's input and its reliability is unmeasured on real hardware: 'uiautomator dump /dev/tty' can be empty on a secure window, truncated under memory pressure, slow enough to miss its budget, or absent on older/OEM builds. If it typically returns nothing useful, every measure degrades to 'View (no match)' and the feature ships as a fallback. | med | high | Make the fallback first-class and honest (FR-41 + hierarchyStatus reporting Dumping/Unavailable), never-throw parsing, its own timeout, and a hardware spike BEFORE Stage 4 that captures three real dumps as commonTest fixtures (operator decision 10). If the spike shows /dev/tty is unreliable, the file+cat fallback becomes a plan task rather than a surprise. |
| px->dp conversion is built on dead scaffolding: densityDpi has no production caller today, so a wrong or missing density silently multiplies every number in the panel by ~2.6x on a 420 dpi device -- and a confidently wrong number in an agent prompt is worse than a prose estimate, which is the feature's entire justification. | med | high | Read density with the dump in the same repository call; treat unknown density as MeasureUnit.Px and label it px rather than guessing; unit-test toDisplayUnit against known pairs (420 dpi: 1080 px = 411 dp); have the demo for AC-67 state the device's dpi so a reviewer can check one number by hand. |
| Alt key-up may never reach the app. onPreviewKeyEvent needs focus, the stage root does not currently request it, and Compose Desktop does not deliver key events to an unfocused window -- so a stuck Measuring panel with a stale readout is a plausible failure, and the note append (AC-70) would never fire. AS-21 currently assumes this away. | med | med | Request focus on the stage root (DevicePickerPanel precedent), dispatch EndMeasure from onFocusChanged and from the next pointer-down as well as from KeyUp, and treat 'Alt not pressed' on any arriving pointer event's keyboardModifiers as a release. Verify by manual demo early. |
| The freeze-time dump runs while the capture loop is still streaming (FR-30 keeps it alive), so two adb children contend for one device; the measured 2665ms screencap gets slower and the single-flight transport counts dropped frames, visibly degrading fps right when the user freezes. | med | med | Accepted for v1 (operator decision 9, no pause). Instrument via the existing TransportStats drop counter so the cost is observable and revisitable with a number instead of a guess. |
| On a Compose-built app the dump's resource-ids are mostly empty, so node ids become 'android.view.View' repeated dozens of times -- the panel's node id and finding copy lose their meaning while the snap itself remains geometrically correct. | med | med | Fall back to class-simple-name plus a disambiguating sibling index, and include a Compose-app dump among the parser fixtures so the degraded label is a deliberate, tested string rather than an accident. |
| A hand-rolled XML scanner in commonMain mishandles real-world attribute content (escaped entities, '>' inside a content-desc, self-closing vs nested nodes), producing wrong bounds rather than no bounds -- a silent wrongness the never-throw contract cannot catch. | med | med | Parse only the four attributes needed, drive the bounds regex off a strict '[l,t][r,b]' shape and skip any node whose bounds do not match, unescape the five XML entities explicitly, and fixture-test against real dumps including one with a quote and an ampersand in content-desc. |
| Guide geometry and panel fidelity (AC-67) have no automated check anywhere in the repo, so a mid-axis guide drawn in the wrong coordinate space (the exact bug class FrameGeometry's KDoc records, where a stray density division halved every mark on Retina) can only be caught by eye. | med | med | Route every measure coordinate through the one FrameGeometry with no second conversion, keep the overlay inside the same frame-sized Box as AnnotationOverlay, and ship light/dark @Previews of a measuring frozen stage as the review artifact. |
| Scope creep in the reducer: measure state, hierarchy status, the note append and the existing annotation handlers all mutate the same FrozenFrame, and MirrorViewModel is already ~470 lines with a documented race hazard about reading state outside reduce. | low | med | Follow the module's existing rule literally -- every new handler reads state.frozen INSIDE reduce -- and keep both new handlers to one reduce each. Extend MirrorViewModelAnnotationTest's race-precedent coverage rather than writing a new pattern. |
| Repeated Option-holds append repeated Option-prefixed lines, so a note sent to the agent could carry three contradictory measurements of the same box. | med | low | EndMeasure replaces an existing Option-prefixed line for that annotation instead of appending; covered by a reducer test. |

### openQuestions

- CONSISTENCY FOLLOW-UP (Stage 2, operator decision 7): FR-44's text still reads 'staged or in-progress'; the operator decision narrows this to staged-only (ignore Alt until an in-progress drag ends). Stage 2 must formally reword FR-44 or add an explicit guard clause rather than silently patching behaviour against unchanged requirement text.
- CONSISTENCY FOLLOW-UP (Stage 2, operator decision 8): the ~2.6s post-freeze window where the hierarchy dump has not yet landed has no acceptance criterion. AC-69 covers dump FAILURE, not PENDENCY. Stage 2 must mint a new AC (e.g. AC-72) covering the Dumping status path -- StatusIndicator's already-shipped-but-unproduced 'Dumping' state is the component to reuse, and the panel must report pendency rather than silently showing 'View (no match)' for ~2.6s before working.
- Does a re-freeze (new frame capture) re-dump the hierarchy, or does the cached hierarchy from a prior freeze persist across multiple freezes of the same session? brainstorm.md proposes: cleared with the pinned bitmap and re-dumped on every freeze transition (same sites that null currentFrozenFrame). Not yet a ratified requirement -- carry into Stage 2 as a confirmed design decision, not an assumption.
- iOS measure-mode parity timeline is undecided -- explicitly out of scope here but likely a future PRD item once an iOS hierarchy-dump equivalent exists.
- Should the appended note line be localized, or does it stay literal/unlocalized like CommandBlock's machine output (localisation.md's convention)? brainstorm.md proposes unlocalised (numeric machine text destined for an agent prompt) but this is a recommendation, not a ratified decision.
- What is 'tappable' for AC-63's touch-target finding? brainstorm.md proposes uiautomator's clickable="true" only (not OR'd with long-clickable/focusable) -- needs ratification since it decides whether AC-63 is ever observable.
- Node id format when resource-id is empty (common on Compose surfaces): brainstorm.md proposes class-simple-name plus a sibling disambiguation index rather than a bare class name or the fully-qualified name -- needs ratification before MeasureFormat is implemented.
- No fixture of a real uiautomator dump exists in the repo yet (operator decision 10): someone with the reference Pixel 8 must capture 2-3 dumps (a View-based app, a Compose app, a scrolling list) before the parser and px<->dp conversion tests can be written against reality. This is a blocking Stage 4 task requiring a phone attached.
- 'uiautomator dump /dev/tty' is not universally available on older API levels or some OEM builds that only write to a file. No fallback path (dump to /sdcard + cat) is specified; brainstorm.md proposes leaving it out for v1 and degrading via the existing fallback, pending the hardware spike's findings.

### readyForPlan

true

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Sign off this Preparation deliverable (consolidated requirements, acceptance criteria, architecture, risks and follow-ups for measure-mode) and open Stage 2 planning? | ["Approve as-is and proceed to Stage 2 planning", "Hold -- request changes before proceeding"] | Approve as-is and proceed to Stage 2 planning | All three upstream artifacts (input, clarify, brainstorm) are already human-approved; this step only consolidates them and performs consistency checks. Both genuine gaps found (FR-44's unrevised 'staged or in-progress' wording vs. operator decision 7, and the missing Dumping-pendency acceptance criterion vs. operator decision 8) are recorded as explicit Stage 2 follow-ups rather than silently patched, so nothing is hidden from the plan. Every acceptance criterion from clarify.md maps to a named component or pipeline step in brainstorm.md's decomposition -- no orphaned criteria were found. |

### artifacts

#### input

input.json

#### clarify

clarify.json

#### brainstorm

brainstorm.json
