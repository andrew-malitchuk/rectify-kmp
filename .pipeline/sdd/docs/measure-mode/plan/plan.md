# measure-mode — plan

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | plan |
| status | done |
| producedBy | sdd-build-plan-refine |
| schemaVersion | 1 |
| createdAt | 2026-10-16T18:52:45Z |
| updatedAt | 2026-10-16T18:53:36Z |

## Gate

### type

human

### approved

true

## Provenance

### selfHash

6c44577da075cc3e5585af1c8fc8afa4d2b5d70dc1bfc5115184e0bb62c1a30a

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 927a20398b5a29dfd69612482d90fb1873aa653b32ddd7175548525ebbd20787 |
| plan/evaluation.json | 1dd86c7b0e4342008033aba029839ba3b4318a20465bbd79a3f77d11381a88a0 |
| plan/candidates/candidate-1.json | feb828f7df347de765a275caebe1c05d94a14bd43bfea9587b180e6dd4b2738f |

## Data

### meta

#### title

Option-Key Measure Mode (measure-mode)

#### version

1

#### status

draft

### goal

Holding Option/Alt with the pointer over a staged annotation on a frozen frame snaps to the matched Android view-hierarchy node (dumped once at freeze time and cached in memory), draws dashed neighbour guides with dp/px gap numerals, reports at most one misalignment or touch-target finding in a floating 'Measuring' card over the stage, and on release writes the measurement into that annotation's note prefixed with the Option glyph -- so the report sent to an agent carries exact numbers instead of a prose estimate.

### problemStatement

No bug-report-to-agent tool in the PRD's research has hierarchy-snapped measurement attached to the report itself. The closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path, so a developer still eyeballs spacing and types a guess into the note. The guess is the defect this feature removes; a confidently wrong number would be worse than the guess, which is why unit resolution and dump honesty are first-class concerns in this plan rather than polish.

### highLevelDescription

Four layers over existing plumbing, zero new Gradle modules, zero new dependencies, one new pure package. (1) adb: one new case in the closed DeviceCommand set (`exec-out uiautomator dump /dev/tty`) with its own timeout budget, plus a hand-rolled never-throwing parser in data-device-api/commonMain. (2) domain: HierarchySnapshotModel (nodes + dump frame size + densityDpi) with an Empty sentinel that IS the FR-41 fallback, a new DeviceRepository.dumpHierarchy member and a DumpHierarchyUseCase through resultLauncher. (3) pure logic: presentation-feature-mirror/core/measure/ -- internal, Compose-free, Orbit-free functions implementing every numeric rule in design-measure-mode.md, which is the only layer this repo's test harness can exercise. (4) state and render: the snapshot on a third StateFlow beside frame/frozenFrame filled fire-and-forget on the freeze transition, MirrorIntent.StartMeasure/EndMeasure through the reducer, and rendering through the three already-shipped zero-caller Measure* components plus one Canvas for the guides and the design's alignment rail, positioned entirely through the existing FrameGeometry.

### background

F-5 in the PRD, depending on F-4 (drag-annotate-frozen-frame) for the frozen frame, staged annotations, FrameGeometry and the stage's pointerInput/onPreviewKeyEvent layer. The design ships a working `measure(annotation, unit)` implementation rather than a mockup, so design-measure-mode.md is an executable specification and is authoritative on algorithm, copy and geometry. Three components (MeasureCard, MeasureMark, MeasureTag), two typography tokens (monoTag, monoRuler), the `measure` colour role, StatusIndicator's `Dumping` state and the whole wm-density parse path were shipped by earlier specs with zero production callers -- this spec is mostly assembly and wiring of parts that already exist, plus one genuinely new input path (the dump) whose reliability nobody has measured on hardware.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/measure-mode/inputs/design-measure-mode.md | .pipeline/sdd/docs/measure-mode/inputs/design-measure-mode.md |
| confluence | .pipeline/prd/docs/rectify/handoff/measure-mode.md | .pipeline/prd/docs/rectify/handoff/measure-mode.md |
| url | /Users/andrew-malitchuk/Downloads/Rectify Screens.html | raw/rectify-screens.html |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/geometry/FrameGeometry.kt |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/discovery/DiscoveryErrorProjection.kt |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/annotation/AnnotationFormat.kt |  |
| code | data-device-api/src/commonMain/kotlin/data/device/api/source/parse/DeviceArgv.kt |  |
| code | data-device-api/src/commonMain/kotlin/data/device/api/source/parse/WmOutputParser.kt |  |
| code | data-device-api/src/commonMain/kotlin/data/device/api/source/tuning/MirrorTuning.kt |  |
| code | data-device-impl/src/desktopMain/kotlin/data/device/impl/source/datasource/DeviceCommandExecutorImpl.kt |  |
| code | data-repository-impl/src/commonMain/kotlin/data/repository/impl/source/DeviceRepositoryImpl.kt |  |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/annotation/MeasureCard.kt |  |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/MeasureMark.kt |  |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/tag/MeasureTag.kt |  |
| code | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/indicator/StatusIndicator.kt |  |

### scope

#### in

- Android-only Option-key measure mode over a STAGED annotation on a frozen frame: node snap, dashed neighbour guides, gap numerals, the design's alignment rail for a top-misalignment finding, one finding, floating Measuring card (FR-10, FR-36..FR-45, AC-10, AC-11, AC-57..AC-68, AC-70, AC-71).
- One hierarchy dump per freeze transition via `adb exec-out uiautomator dump /dev/tty`, cached in memory for that frozen frame's lifetime, parsed by a never-throwing commonMain scanner, never blocking or delaying the freeze (FR-46, NFR-13, NFR-14, AS-18, AC-69).
- Honest pendency: hierarchyStatus = Dumping during the ~2.6 s window before the dump lands, reported through StatusIndicator's already-shipped Dumping state and in the panel, with FR-41 fallback values underneath (operator decision 8, AC-72).
- Note persistence on Option release: one Option-glyph-prefixed line per annotation, written into StagedAnnotation.comment and REPLACED rather than appended on a repeat hold (FR-43, AC-70, SC-17).
- Density wiring: the dead-but-tested parseWmDensity / DeviceCommand.WmDensity path becomes a production caller; unknown density degrades the whole readout to MeasureUnit.Px with `px` labels rather than printing dp computed from a guess.
- Reuse of MeasureCard, MeasureMark, MeasureTag, KeyCap, StatusIndicator, AnnotationOverlay's coordinate space, FrameGeometry, AnnotationFormat's × constant, resultLauncher and the existing Koin device wiring.
- Uniform Compose isAltPressed detection on every desktop host, plus the focus-independent pointer-modifier sampler and the release-on-focus-loss / release-on-next-pointer-down guards the key path needs to be trustworthy (AS-21, FR-44, AC-71).
- A blocking hardware spike as step 1, followed by an explicit operator go/no-go gate as step 2: three real uiautomator dumps captured from the attached device and committed as commonTest fixtures (operator decisions 10 and -- for the gate -- the Stage 2 pre-gate decision 2).
- One additive, default-valued `footer: String? = null` parameter on MeasureCard, accepted by the operator at the Stage 2 pre-gate as a scoped deviation: decision 6 now reads 'no BREAKING signature change'. The organism has zero existing callers, so the parameter is source-compatible, and AC-67's footer sentence renders inside the card border exactly as the design shows.

#### out

- iOS measure support -- no view-hierarchy-dump equivalent is wired for iOS in this codebase (AS-19).
- A Compose UI-test harness. This repo has none and this host cannot load Skiko in-process (the drag-annotate verdict's W-1 records `ImageBitmap(2,3)` throwing LibraryLoadException), so geometry and panel fidelity (AC-67, AC-69, AC-71) stay manual demo checks and this plan says so rather than inventing coverage.
- Measuring an in-progress drag rect. Alt is ignored until the drag ends (operator decision 7); FR-44 is reworded below instead of being silently reinterpreted.
- Dumping on Option-down. Ruled out by the design's own NFR-2 analysis: a ~2.6 s-class adb child on a key press misses a ~100 ms budget by an order of magnitude.
- Pausing the capture loop during the freeze-time dump. The transient fps dip is accepted and observed via the transport's existing dropped-frame counter (operator decision 9).
- A `dump /sdcard/x.xml` + `cat` fallback for hosts where `/dev/tty` output is unavailable. Deliberately unspecified until the step-1 spike reports; it is one of the three recorded exits the operator chooses between at the step-2 gate, not a surprise.
- Node text / content-desc extraction. Only bounds, class, resource-id and clickable are parsed -- the dump carries on-screen user text and this feature has no use for it (see security.privacy).
- A hierarchy tree view in the side panel. HierarchyRow exists for it; depth is kept on the node model so a later spec can use it, but nothing renders it here.
- A `position` row in the Measuring panel. The design's five rows are Size/Top/Right/Bottom/Left only; FR-45's `({x}, {y})` form is encoded in MeasureFormat and asserted by AC-66, with no production call site until a surface needs one.
- macOS-only Option special-casing; a new Theme token; a new Gradle module; a new third-party dependency; a new Koin module.

### assumptions

- AS-18 -- the hierarchy is dumped exactly once, on the freeze transition, and cached in memory beside the frozen frame for that frame's lifetime; Option-down reads the cache only and never triggers a dump.
- AS-19 -- Android only this spec; iOS has no dump equivalent wired.
- AS-20 -- Theme.typography.monoTag (9.5sp/13sp, Medium, JetBrains Mono) already exists and matches the design, and is the style MeasureTag carries; verified again against FontSize.kt / LineHeight.kt / Typography.kt. No new typography token. CORRECTED IN REFINE: monoRuler also exists but this spec does NOT use it -- the Ruler molecule owns it, and MeasureMark's own KDoc designates MeasureTag as the carrier of gap labels. monoRuler is listed nowhere as a reused style here, so AS-20 does not imply a caller this plan never writes.
- AS-21 -- Compose exposes isAltPressed uniformly in the same androidx.compose.ui.input.key package as the already-used isMetaPressed, and this spec uses it on every desktop host with no OS branch. NOTE: this plan does NOT inherit AS-21's optimism that Alt key-up always arrives -- see risks and the Alt-detection component for the focus request, the pointer-modifier sampler and the two release guards.
- DECISION (ratified Stage 2, reversible with one word) -- a re-freeze re-dumps: the cached snapshot is cleared at exactly the three sites that already null currentFrozenFrame (selectPlatform at MirrorViewModel.kt:208, stopSession at :283, resumeLive at :443) and re-filled on the next freeze transition. reconnect() deliberately clears nothing, matching its existing frame-and-annotation-preserving behaviour. This closes Stage 1's open question.
- DECISION (ratified Stage 2) -- `tappable` for AC-63 means uiautomator `clickable="true"` only, not OR'd with long-clickable or focusable. The broader predicate fires on scroll containers and text fields and would make a 'should' finding noisy.
- DECISION (ratified Stage 2) -- the Option-prefixed note line is unlocalised machine text, like CommandBlock's output: it is numeric text destined for an agent prompt and must not become locale-dependent. Its exact format is FR-43's, character for character. The panel's row labels, title and footer sentence ARE localised.
- DECISION (ratified Stage 2) -- node id format: class simple name + '#' + resource-id short name when a resource-id exists; class simple name alone when it does not; class simple name + '[n]' with a 1-based depth-first occurrence index when a bare class name would collide with a sibling. Never a dangling '#'. Literal 'View (no match)' for the FR-41 fallback.
- DECISION (ratified Stage 2, operator pre-gate decision 1) -- MeasureCard gains ONE additive, default-valued `footer: String? = null` parameter. Operator decision 6 is restated as 'no BREAKING signature change'; this parameter is source-compatible on an organism with zero existing callers. The same commit updates MeasureCard's KDoc, which today says the card 'lives in the sidebar' and is 'a panel, not an overlay' while this spec floats it over the stage -- presentation-core-ui's rule is that a component must not lie about what the app does, and that includes its own documentation.
- DECISION (ratified Stage 2, operator pre-gate) -- AC-67 is judged at TOKEN fidelity, not px-exactness: MeasureCard's padding is Theme.spacing.spacingL (16dp) against the design's 12, its column gap spacingS (8) against 10, its radius Theme.cornerToken.card against 10, its border Theme.color.line against the design's line2. design-tokens.md forbids raw dp and raw Color at a call site, so token fidelity is the house rule. Px-exactness would be a presentation-core-styling token change and a separate spec.
- DECISION (ratified Stage 2, operator pre-gate decision 2) -- step 2 is an explicit operator go/no-go gate. The spike commits whatever it captures and reports its findings; this plan does NOT pre-commit to any failure exit. If the dumps are unusable, the operator chooses, with the spike's evidence in hand, between (a) the `dump /sdcard/window_dump.xml` + `cat` second command case, (b) descoping FR-36..FR-40 for this release and shipping the FR-41 fallback path only, or (c) stopping the spec. Stage 4 PAUSES at that gate.
- uiautomator bounds are in the same device-pixel space as screencap output, so the dump's root bounds can be compared against FrozenFrame.deviceSize as a rotation/override guard. The step-1 spike is what turns this from an assumption into a measurement.

### constraints

| type | text |
| --- | --- |
| tech | Android only: the hierarchy source is `adb shell uiautomator dump`, which has no iOS or desktop-native equivalent in this codebase. |
| tech | No Compose UI-test harness exists (testing.md) and this host cannot load Skiko in-process, so no visual assertion is possible -- not even a rendered @Preview. Every numeric rule must be behind a pure function unit-tested in commonTest; everything else is demo-verified and labelled as such. |
| tech | commonTest compiles for every target. Backtick test names must contain no comma and no colon (an apostrophe IS fine -- corrected by experiment in testing.md; presentation-feature-mirror/CLAUDE.md is stale on that point). presentation-feature-mirror/CLAUDE.md asks for plain-word camelCase names in this module, so every test this spec adds in that module uses a camelCase identifier with no backticks at all and the illegal-character hazard cannot arise. |
| tech | Every colour, spacing, radius, stroke and text style comes from Theme.* (design-tokens.md). No raw Color or TextStyle at a call site; the one geometry literal this spec needs (the design's 228 px panel width) follows the module's existing precedent of a named private val in the feature file, exactly as MirrorErrorStage's `ErrorCardWidth = 460.dp` and MirrorAnnotationStage's `BannerTopInset = 14.dp` already do. |
| tech | data-device-api's load-bearing rule: every parser lives in that module's commonMain, with no java.*/javax.* import, so it is fixture-testable with no phone. NFR-5 is enforced by type -- DeviceCommand stays a closed sealed set and buildDeviceArgv stays the single [adb, -s, id, ...] construction point. No raw-argv escape hatch. |
| tech | DeviceCommandExecutorImpl.timeoutMs() ends in `else -> MirrorTuning.HostCommandTimeoutMs`, so adding a sealed case is NOT a compile error there. The dump's timeout branch has to be added deliberately, in the same commit as the case, or a 2.6 s-class command silently inherits a 3_000 ms budget. |
| tech | Orbit MVI: the measure readout and the note write go through MirrorIntent/the reducer, never local Compose state. Conversely, pixel-rate data (hover offset, Alt-held flag, the in-progress drag) must stay in `remember` -- the module's documented discipline. Every new reducer handler reads state.frozen INSIDE reduce (the documented race hazard). |
| tech | domain-usecase-impl bodies run inside resultLauncher(errorMapper = Failure.Technical::Platform); nothing throws past that boundary (error-handling.md, domain-layer.md). The mapper for the dump is one-direction `common-core.Mapper`, not a ModelResourceMapper, because there is no model->resource direction (mappers.md). |
| time | Operator decision 10: capturing three real `uiautomator dump` outputs from an attached device is a BLOCKING step 1, and step 2 is an operator go/no-go gate on its findings. Parser tests and px<->dp conversion must not be written before it, so the first task of execution needs a phone attached. |
| business | The feature's justification is that the numbers in the note are exact. A confidently wrong number (wrong density, stale hierarchy, wrong coordinate space) is a worse outcome than no number, which is why unknown density degrades to px and a geometry mismatch degrades to Unavailable instead of measuring anyway. |

### dependencies

| on | note |
| --- | --- |
| drag-annotate-frozen-frame (F-4) | Provides the frozen frame, StagedAnnotation/FrozenFrame state, FrameGeometry, the stage's pointerInput layer and the onPreviewKeyEvent precedent. This spec extends stageAnnotation's freeze transition and adds a sibling overlay; it changes no F-4 behaviour. |
| android-device-discovery-mirroring (F-2) | Provides DeviceCommand/buildDeviceArgv/DeviceCommandExecutor/ProcessRunner/ToolResolver, MirrorTuning, parseWmDensity and the Koin device wiring. The dump adds one sealed case and one timeout branch to it. |
| A physically attached, USB-debugging-authorised Android device | Blocking for step 1 (three real dumps as fixtures) and for the AC-67/AC-69/AC-71/AC-72 demos. Two precedents say this cannot be assumed: MirrorTuning.CaptureFloorMs shipped unmeasured because no authorised device was reachable, and error-handling-and-recovery's HARDWARE-BLOCKED.md records the reference Pixel 8 being unplugged mid-session with two tasks left blocked. If the device is absent, those tasks are BLOCKED, not waived and not done. |
| presentation-core-localisation | Five row labels, the card title, the footer sentence and the Dumping/Unavailable copy are new string keys (values/strings.xml, English only today). |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Dump reliability is unmeasured on real hardware. `uiautomator dump /dev/tty` can return nothing on a FLAG_SECURE window, be truncated under memory pressure, describe a system overlay rather than the app, or be unsupported on older API levels and some OEM builds that only write to a file. If it typically returns nothing useful, every measure degrades to 'View (no match)' and the feature ships as a fallback. | med | high | Operator decision 10's spike is step 1 and blocks everything downstream: capture three real dumps (View-based app, Compose app, scrolling list) on the attached device and commit them as commonTest fixtures. The parser never throws, the dump has its own timeout, and hierarchyStatus reports Dumping/Unavailable rather than lying. IF THE SPIKE FINDS DUMPS UNUSABLE the plan does NOT pre-commit an exit (operator pre-gate decision 2): Stage 4 pauses at the step-2 gate and the operator chooses, with the spike's evidence, between (a) adding the `dump /sdcard/window_dump.xml` + `cat` fallback as a second DeviceCommand case and re-running the spike, (b) descoping FR-36..FR-40 for this release and shipping the FR-41 fallback path only (annotation-rect measurement against frame edges, which needs no hierarchy at all and still beats a prose estimate), or (c) stopping the spec and reporting rather than building four layers on an input that does not exist. |
| px->dp rests on dead scaffolding. parseWmDensity, DeviceCommand.WmDensity and DeviceInfoResource exist, are tested, and have ZERO production callers -- grep confirms only DeviceArgvTest and WmOutputParserTest reference them. A wrong or missing density silently multiplies every number in the panel by ~2.6x on a 420 dpi device, and a confidently wrong number in an agent prompt is worse than the prose estimate this feature exists to replace. | med | high | Read `wm density` in the SAME repository call as the dump so the pair can never drift. Treat unknown density as MeasureUnit.Px and label every value `px` (the design's own units table admits `1344 px`) rather than guessing -- which also means the <32dp touch-target finding cannot fire, deliberately. Unit-test toDisplayUnit against known pairs (420 dpi: 1080 px = 411 dp; 160 dpi: 1 px = 1 dp) and assert the Px-fallback path. DeviceRepositoryImpl takes an injectable DeviceCommandExecutor and lives in commonMain, so the one-call pairing is itself assertable with a fake -- see testStrategy's DeviceHierarchyDumpTest. The AC-67 demo must state the device's reported dpi so a reviewer can verify one number by hand. |
| Alt key-up may never arrive. onPreviewKeyEvent needs focus, the stage root never requests it today (only the selected CommentPopover does, in a LaunchedEffect keyed on the selected index), and Compose Desktop delivers no key events to an unfocused window -- so a stuck Measuring panel with a stale readout is plausible and the AC-70 note write would never fire. AS-21 assumes this away. | med | med | Three defences, all planned rather than hoped for: (1) a FocusRequester + .focusable() on a NEW overlay root with focus requested whenever a frame is frozen (the DevicePickerPanel precedent), feeding a NEW onPreviewKeyEvent isAltPressed branch -- the existing onPreviewKeyEvent belongs to SelectedComment and exists only while a popover is open, so it cannot be the host for this; (2) a focus-INDEPENDENT sampler -- the overlay's own pointerInput reads `event.keyboardModifiers.isAltPressed` on every pointer event at PointerEventPass.Initial without consuming anything (the stage already uses that pass for awaitFirstDown), which is how hover position arrives anyway, so a release the key path missed is corrected on the first pointer movement; (3) EndMeasure dispatched from onFocusChanged(lost) and from the next pointer-down as well as from KeyUp. Verified by manual demo EARLY (step 11), not at the end, and recorded as the one behaviour most likely to need a second iteration. |
| Adding .focusable() to a new stage overlay root can steal keyboard focus from the CommentPopover's text field or alter tab order, regressing F-4's typing flow (AC-7/AC-8/A11Y-2). | med | med | The popover already re-requests focus in a LaunchedEffect keyed on the selected index, so it wins whenever it opens; the overlay root requests focus only when `frozen != null && frozen.selectedIndex == null`, and the measure overlay suppresses popovers while a readout exists anyway. Covered by a manual check in the step-11 demo script (open a popover, type, hold Alt, confirm the text is intact) because no automated harness can see focus here. |
| The freeze-time dump runs while the capture loop is still streaming (FR-30 keeps it alive), so two adb children contend for one device: the measured 2665 ms screencap gets slower and the single-flight transport counts dropped frames right when the user freezes. | med | med | Accepted for v1 (operator decision 9, no pause). Bounded to one dump per freeze, and the user is looking at pinned pixels for its whole duration. Instrumented via the existing TransportStats dropped-frame counter so the cost is a number, not a guess, and the decision can be revisited with evidence. |
| On a Compose-built app the dump's resource-ids are mostly empty, so node ids degrade to `android.view.View` repeated dozens of times: the panel's node id and the finding's 'vs {id}' segment identify nothing while the snap remains geometrically correct -- the panel looks broken while being right. | med | med | Ratified id format (class simple name, plus '[n]' occurrence index on collision, never a dangling '#'), and a Compose-app dump is one of the three required step-1 fixtures so the degraded label is a tested string rather than an accident. |
| A hand-rolled XML scanner in commonMain mishandles real attribute content (escaped entities, '>' inside a content-desc, self-closing vs nested nodes) and produces WRONG bounds rather than no bounds -- a silent wrongness the never-throw contract cannot catch. | med | med | Parse only the four attributes needed, drive bounds off a strict `[l,t][r,b]` shape and SKIP any node whose bounds do not match that shape, unescape exactly the five XML entities, and fixture-test against real dumps including one with a quote and an ampersand in a content-desc. Skipping a node is always preferred over inventing a rect. |
| Guide geometry and panel fidelity have no automated check anywhere in this repo. A mid-axis guide drawn in the wrong coordinate space is the exact bug class FrameGeometry's KDoc records, where a stray density division halved every mark on a Retina host, and only an eye can catch it here. | med | med | Every measure coordinate goes through the one FrameGeometry with no second conversion; the measure overlay lives inside the same frame-sized Box as AnnotationOverlay so it shares one coordinate space; light and dark @Previews of a measuring frozen stage ship as the review artifact (compose-preview-convention.md). Stated plainly in testStrategy as uncovered by tests -- and, per the drag-annotate verdict's W-2, a preview that exists but was never RENDERED is not verification either, so the demo script names the render step explicitly. |
| Reducer scope creep: measure state, hierarchy status, the note write and the existing annotation handlers all mutate the same FrozenFrame, and MirrorViewModel is already ~470 lines with a documented race hazard about reading state outside reduce. | low | med | Both new handlers read state.frozen INSIDE reduce and perform exactly one reduce each; frozenHierarchy is read with .value inside intent {} exactly as stageAnnotation already reads currentFrame.value. Extend MirrorViewModelAnnotationTest's race precedent instead of writing a new pattern. |
| Repeated Option-holds could grow the note without bound, so an annotation sent to an agent carries three contradictory measurements of the same box. | med | low | EndMeasure REPLACES the existing Option-prefixed line in StagedAnnotation.comment for that annotation rather than appending a second one; a reducer test holds twice and asserts exactly one Option line survives with the newer values, with the user's own prose preserved. |
| Hovering across several annotations while Alt stays down fires one StartMeasure per target, so the 'exactly two reducer passes per Option-hold' figure is really 'one per measured target plus one release'. Sloppy edge detection would turn it into one reduce per pointer frame -- the pattern F-4 forbids. | low | med | Dispatch is gated on a TRANSITION in the remembered (altHeld, targetIndex) pair, computed from data already in `remember`; nothing is dispatched while that pair is unchanged. A pure `annotationAt(point, annotations)` function makes the topmost-wins tie-break assertable, and the gate is reviewed explicitly in step 11. |
| REFINE-ADDED. Two new MirrorIntent cases can land as `-> Unit` branches in handleIntent and compile, test green and ship as a feature that does nothing -- the exact trap error-handling-and-recovery recorded in CARRY-FORWARD-inert-intents.md, where five intents sat inert behind compile-forced branches. | low | high | StartMeasure/EndMeasure land in the SAME step as their handlers (step 9 mints them, step 10 implements them, and step 10 is the step whose definition of done includes the reducer tests). The structural exit check from that document is adopted verbatim: `grep -n '\-> Unit' presentation-feature-mirror/.../MirrorViewModel.kt` must print nothing, and it is in this plan's definitionOfDone because a green suite demonstrably cannot catch it. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-10 | While holding Option over an annotation box, enter Measure mode: snap to the matched underlying view-hierarchy node, show dashed guide boxes for neighbouring views and dp/pixel gap measurements in a floating 'Measuring' panel, highlight misalignment in red, and attach the measurement data to that annotation's note (prefixed with the Option symbol). |
| FR-36 | Node snap: candidate nodes are those fully contained within the annotation box with a 4 px slack on every edge (e.x >= a.x-4, e.y >= a.y-4, e.x+e.w <= a.x+a.w+4, e.y+e.h <= a.y+a.h+4); ties break by largest area first (w*h descending), not nearest-centre and not first-found. |
| FR-37 | Neighbour selection: overlapping nodes are excluded entirely, then per side pick the nearest non-overlapping node using the opposite-axis band-overlap test plus the per-side closest-edge sort in design-measure-mode.md -- L: vertical band overlap and e.x+e.w <= t.x, right-most wins; R: vertical band overlap and e.x >= t.x+t.w, left-most wins; T: horizontal band overlap and e.y+e.h <= t.y, lowest wins; B: horizontal band overlap and e.y >= t.y+t.h, highest wins. Band overlap is strict (e.y < t.y+t.h && e.y+e.h > t.y), so a merely diagonal node is a neighbour on neither axis. |
| FR-38 | Gap computation: lx = L ? L.x+L.w : 0, rx = R ? R.x : FRAME_W, ty = T ? T.y+T.h : 0, by = B ? B.y : FRAME_H; gaps are left = t.x-lx, right = rx-(t.x+t.w), top = t.y-ty, bottom = by-(t.y+t.h). With no neighbour on a side the gap is measured to the frame edge, never omitted. |
| FR-39 | Guide rendering: for each side draw a 1 px dashed guide line on the target's mid-axis (my = t.y+t.h/2, mx = t.x+t.w/2) with a numeral centred on the span, suppressed entirely -- no line, no numeral -- when that gap is less than 1 unit in the DISPLAY unit (the design's `if (v < 1) return`, evaluated after px->dp conversion). Suppression applies to guides only; the panel's five Size/Top/Right/Bottom/Left rows always print their value. ADDED IN REFINE (the candidate dropped it; the design is authoritative on geometry): when the selected finding is the top misalignment, ALSO draw the design's alignment rail -- a line at the left neighbour's y spanning from the neighbour's left edge across to the target's right edge, labelled Δ{d}{unit} offset above it by Theme.spacing.spacingL (the design's raw `16 px`, at the same token fidelity FR-42 uses). The rail is carried on MeasureReadout.rail and is populated only for that one finding branch. |
| FR-40 | Finding selection: evaluate in strict order and stop at the first match -- (1) top misalignment, L exists && L.y != t.y && \|L.y-t.y\| <= 10 px; (2) left misalignment, T exists && T.x != t.x && \|T.x-t.x\| <= 24 px; (3) touch target, target is tappable && height < 32 dp. At most one finding is ever shown. The 10/24 asymmetry is intentional and both tolerances stay in device pixels; only the reported delta converts. The 32 threshold is deliberately the one rule evaluated in DP and not in px, because it is an Android platform guideline (paired with the literal `min touch 48` in its own copy) rather than a design tolerance -- which is also why the finding cannot fire at all when density is unknown and the unit degrades to Px. |
| FR-41 | No-match fallback: when no node satisfies the snap containment test, measure against the annotation's own rect as the target, label the node 'View (no match)', and still produce full Size/Top/Right/Bottom/Left measurements through the same neighbour/gap pipeline (frame-edge gaps where no neighbour exists). An empty or unavailable cached hierarchy reaches this same path through HierarchySnapshotModel.Empty. |
| FR-42 | Panel placement: the Measuring panel renders as a floating card over the stage at the design's geometry (top 16 / left 16 inside the frame box, 228 px wide, card radius), reusing the existing MeasureCard organism aligned TopStart in the frame-sized Box that MirrorFrameView's content fills -- the same Box where the drop banner already sits at TopCenter and Resume Live at TopEnd, so no slot collides. Padding, gap and radius come from Theme tokens (spacingL/spacingS/cornerToken.card) rather than the design's raw 12/10/10 px -- design-tokens.md forbids raw dp at a call site, and token fidelity is the house rule the AC-67 demo is judged against. The one named private val is `MeasureCardWidth: Dp = 228.dp`, following MirrorErrorStage's shipped `ErrorCardWidth: Dp = 460.dp` precedent. |
| FR-43 | On Option release, write the measurement summary into the active annotation's note -- the StagedAnnotation.comment field -- as a single line prefixed with the Option glyph and a space; a pre-existing Option-prefixed line for that annotation is REPLACED, not duplicated, and the user's own prose on other lines is preserved. Format: `⌥ {nodeId} {w}×{h}{unit} · top {t}{unit} · right {r}{unit} · bottom {b}{unit} · left {l}{unit}` using × (U+00D7) for the dimension separator and · (U+00B7) as the segment separator, with ` · {finding}` appended when a finding exists (the design's own example, `⌥ top Δ6dp vs item_title`, is this line's finding segment). Unlocalised machine text. |
| FR-44 | REWORDED IN STAGE 2 (operator decision 7; id unchanged). Measure mode activates while the Option/Alt key is held -- detected via Compose's isAltPressed, uniformly on every desktop host -- with the pointer over a STAGED annotation box on a frozen frame, and deactivates on release. Alt arriving while a drag is in progress is IGNORED until that drag ends: the phrase 'or in-progress' is struck, because measuring a live drag rect means recomputing the readout per pointer frame, which is the 60-Hz-into-the-reducer pattern the module forbids, and there is no committed annotation for FR-43's note to be written to. Implementation: a NEW onPreviewKeyEvent with an isAltPressed branch on a NEW focus-requesting measure-overlay root -- NOT the existing onPreviewKeyEvent, which belongs to SelectedComment and only exists while a popover is open, though it stays the precedent for the isMetaPressed-style modifier read -- complemented by a focus-independent read of each pointer event's keyboardModifiers at PointerEventPass.Initial; a release is also inferred from focus loss and from the next pointer-down. |
| FR-45 | Format all measure-mode numeric text per the design's table: dimensions as `{w}×{h}{unit}` using the × multiplication sign (U+00D7, never 'x', never ' X '), deltas as `Δ{value}{unit}` with U+0394 and no space before the unit, positions as `({x}, {y})`, and the finding copy verbatim -- `top Δ{d}{unit} vs {leftNeighbourId} · expected 0`, `left Δ{d}{unit} vs {topNeighbourId} · expected 0`, `height {h}{unit} · min touch 48{unit}`. CORRECTED IN REFINE: the candidate dropped the `vs {id}` segment from the first two strings; the design specifies it, MeasureCard's own shipped @Preview renders `top Δ6dp vs item_title · expected 0`, and A11Y-M2's colour-is-not-the-only-carrier argument depends on it naming the neighbour. Values are rounded to whole units at formatting time only. The position form has no panel row today -- the design's five rows are Size/Top/Right/Bottom/Left -- so `position()` exists as a one-line formatter asserted by AC-66 with no production caller until a surface needs one; it is not to be grown into a feature. |
| FR-46 | Extend the F-4 freeze path to dump the view hierarchy once per freeze and cache it in memory alongside the frozen frame, together with the dump's own frame size and the device's densityDpi; a failed, timed-out, empty or unparseable dump yields HierarchySnapshotModel.Empty rather than blocking the freeze, driving the FR-41 fallback for every measure against that frame. The cached snapshot is cleared at the three existing sites that null the frozen bitmap (selectPlatform, stopSession, resumeLive) and re-dumped on the next freeze transition; reconnect() clears nothing. The launch is fire-and-forget but its OUTCOME is never discarded: it always resolves hierarchyStatus to Ready or Unavailable. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-2 | performance | Measure-mode hierarchy-node lookup on Option-hover resolves in under ~100 ms so the measuring panel feels instantaneous. |
| NFR-9 | ux | Full light and dark theme parity using the defined signal-colour system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. Measure marks, numerals, the alignment rail and the node id are Theme.color.measure; the finding block is Theme.color.annotate at 10% over surface; a measure mark is never red and an annotation mark is never blue. |
| NFR-13 | performance | NFR-2's <~100 ms budget applies specifically to the Option-down -> panel-visible path (snap + neighbour + gap + finding + formatting against the already-cached in-memory hierarchy). The freeze-time uiautomator dump -- 2665 ms measured for a screenshot alone on the reference device -- is explicitly EXCLUDED: that cost lands on a freeze the user already initiated, not on Option-down. |
| NFR-14 | reliability | A failed, timed-out, unparseable or empty hierarchy dump at freeze time must not block or delay the freeze. It degrades to an empty cached hierarchy with hierarchyStatus = Unavailable, which degrades every measure against that frame to the FR-41 no-match fallback -- never a crash, never a hang, never a silent wrong number, and never a status left stuck at Dumping. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-10 | Given a frozen frame with a staged annotation box, when the user holds Option over it, then the tool snaps to the matched view-hierarchy node and shows neighbour guide boxes with dp gap measurements in a 'Measuring' panel. | must | demo |
| AC-11 | Given a measured element has a misalignment or undersized hit target, then the offending dimension is highlighted in red in the panel's finding block and the annotation's note carries the Option-mode marker plus the measurement data. | must | inspection |
| AC-57 | Given an annotation rect and a list of hierarchy nodes, the snap function selects the node fully contained within the annotation box with a 4 px slack on every edge, and when multiple nodes qualify it picks the one with the largest w*h area -- largest-area-first, not nearest-centre and not first-found. | must | test |
| AC-58 | Given a snapped target node and the hierarchy list, the neighbour function returns, per side (left/right/top/bottom), the nearest non-overlapping node per the band-overlap + closest-edge rule in design-measure-mode.md: right-most-left, left-most-right, lowest-top, highest-bottom, each requiring the opposite-axis band overlap. | must | test |
| AC-59 | When a side has no neighbour, the gap function measures to the frame edge for that side (0 for left/top, FRAME_W/FRAME_H for right/bottom) instead of omitting the measurement. | must | test |
| AC-60 | A guide (dashed mid-axis line + numeral) is suppressed entirely when its computed gap is less than 1 unit; it is never rounded up and never shown as zero. | must | test |
| AC-61 | When the left neighbour's y differs from the target's y by more than 0 and at most 10 px, the finding selector returns the top-misalignment finding ('top Δ{d}{unit} vs {leftNeighbourId} · expected 0'), taking precedence even if a left-misalignment or touch-target condition is also true. | must | test |
| AC-62 | When no top-misalignment finding applies and the top neighbour's x differs from the target's x by more than 0 and at most 24 px, the finding selector returns the left-misalignment finding ('left Δ{d}{unit} vs {topNeighbourId} · expected 0'). | must | test |
| AC-63 | When neither alignment finding applies and the target is a tappable node with height <32dp, the finding selector returns the touch-target finding ('height {h}{unit} · min touch 48{unit}'). | should | test |
| AC-64 | The finding selector never returns more than one finding per measure, regardless of how many of the three conditions (top misalignment, left misalignment, touch target) are independently true. | must | test |
| AC-65 | When no hierarchy node satisfies the snap containment test, the measure result reports node id 'View (no match)' and still contains full Size/Top/Right/Bottom/Left values, computed against the annotation's own rect as the target and the frame edges for missing neighbours. | must | test |
| AC-66 | Formatted output uses the multiplication sign × (U+00D7) for Size (never 'x' or ' X '), 'Δ{value}{unit}' with U+0394 and no space before the unit for deltas, and '({x}, {y})' parentheses for positions, per the design's own formatting table. | must | test |
| AC-67 | The floating Measuring panel renders at the design's geometry and copy (Option keycap, 'Measuring' title + right-aligned index, mono node id in the measure-blue signal colour, Size/Top/Right/Bottom/Left rows, red finding block, footer 'Numbers attach to the annotation and go into the prompt.') correctly in both light and dark themes. Geometry is judged at token fidelity (Theme padding/gap/radius), not px-exactness against the design's raw 12/10/10, per FR-42. The footer sentence renders INSIDE the card border, via the additive `footer` parameter accepted at the Stage 2 pre-gate. | should | demo |
| AC-68 | With the view hierarchy already resolved and cached from freeze time, the Option-down to panel-visible computation (snap + neighbour + gap + finding + formatting) completes in under ~100 ms (NFR-2/NFR-13 scope), measured as a commonTest timing assertion over a representative hierarchy size. The assertion uses a generous ceiling over many iterations so it catches an accidental O(n^2) rather than measuring the host. | must | test |
| AC-69 | A hierarchy dump that fails, times out, or returns empty at freeze time does not block or delay the freeze, and every subsequent measure against that frame shows the 'View (no match)' fallback rather than crashing or hanging. | must | demo |
| AC-70 | On Option release, the active annotation's note carries the measurement summary as a single line prefixed with the Option glyph and a space, replacing any earlier Option-prefixed line for that annotation, and this is observable as a state change in the mirror feature's reducer without requiring a real key event in commonTest. | must | test |
| AC-71 | Measure mode activates only while the Option (macOS) / Alt (other hosts) key is held, detected via Compose's isAltPressed, with the pointer over a staged annotation box on a frozen frame, and deactivates on release -- including a release the key path never saw, which is recovered from focus loss or the next pointer event's modifiers. Alt during an in-progress drag does nothing (FR-44 as reworded). | should | demo |
| AC-72 | NEW IN STAGE 2 (operator decision 8; closes the pendency gap AC-69 leaves open). Given a freeze whose hierarchy dump has not landed yet (the ~2.6 s window), when the user holds Option over a staged annotation, then the reduced state reports hierarchyStatus = Dumping from the moment of the freeze until the dump resolves, the status line shows StatusIndicator's already-shipped Dumping state, and the Measuring panel reports the hierarchy as still being read while showing FR-41 fallback values underneath -- it never presents 'View (no match)' as a settled answer for ~2.6 s and then silently starts working. Honest verification split: the state machine is asserted in commonTest (the freeze sets Dumping before the use case resolves; Ready/Unavailable on resolution; the pure indicatorDot projection maps Dumping ahead of Frozen), while the rendered pendency copy itself can only be confirmed by the AC-67 light/dark demo, because this repo has no Compose UI-test harness. | must | test |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-5 | Measure a misaligned element | happy | ["Hold Option over a staged annotation box on a frozen frame", "The readout snaps to the matched hierarchy node", "Neighbour guide boxes and dp gap numerals render over the frame, plus the alignment rail for the top-misalignment finding", "The misalignment is highlighted red in the panel and lands in the note on release"] |
| SC-16 | Measure an annotation with no matching hierarchy node | edge | ["Hold Option over an annotation box whose rect contains no fully-contained hierarchy node", "The panel shows node id 'View (no match)'", "Size/Top/Right/Bottom/Left are still computed against the annotation rect and the frame edges", "No finding is shown unless the fallback rect itself is undersized and tappable"] |
| SC-17 | Release persists the measurement to the note | happy | ["Hold Option over an annotation and read the Measuring panel", "Release Option", "The annotation's note carries one Option-glyph-prefixed line summarising the measurement", "Holding Option again and releasing replaces that line rather than adding a second one", "Sending the report includes the exact numbers instead of a prose estimate"] |
| SC-18 | Touch-target finding on an undersized tappable element | edge | ["Hold Option over a tappable element matched to a node under 32 dp tall", "No top or left misalignment is present", "The panel shows the finding 'height {h}{unit} · min touch 48{unit}' in red", "With density unknown the unit is px and this finding deliberately does not fire"] |
| SC-19 | Freeze-time hierarchy dump fails | error | ["The user freezes the frame with a drag", "The uiautomator dump fails, times out or returns empty", "The freeze completes anyway, with an empty cached hierarchy and hierarchyStatus = Unavailable (never left at Dumping)", "Every measure against that frame falls back to 'View (no match)' with full values against the annotation rect"] |

### definitionOfReady

- Preparation deliverable approved (it is) and this plan approved with FR-44's rewording, AC-72, the corrected finding copy and the added alignment rail accepted.
- An authorised Android device is attached and `adb devices -l` lists it as `device`, so step 1 can run. If it is not, step 1 and the step-13 demos are recorded BLOCKED rather than waived (HARDWARE-BLOCKED.md's framing).
- The ratified decisions in `assumptions` are accepted or overruled: re-freeze re-dumps and clears at the three existing sites; tappable = clickable only; the note line is unlocalised and takes FR-43's exact format; the node id format with its '[n]' collision index; the MeasureCard `footer` deviation under the reworded decision 6; token-fidelity AC-67; and the step-2 operator go/no-go gate with no pre-committed exit.
- `./gradlew :data-device-api:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest` is green on the current HEAD before any change lands.

### definitionOfDone

- Step 1's three real dumps are committed as commonTest fixtures and the parser is tested against them, not against a hand-written guess.
- Step 2's go/no-go gate is recorded in writing (dump wall-clock, root-bounds-vs-screencap agreement, resource-id density on a Compose app, `wm density` output) and, if the dumps are unusable, the operator's chosen exit is recorded before any further step runs.
- Every numeric rule of design-measure-mode.md has at least one commonTest assertion: snap containment + 4 px slack + largest-area tie-break, per-side neighbour selection, gap-to-frame-edge, sub-unit guide suppression, the three-branch finding precedence including the never-two assertion and the `vs {id}` segment, the fallback, px->dp including the Px degradation, and every formatting rule.
- The design's own ELS fixture is ported into commonTest as an end-to-end oracle for measure(...) and matches, including the alignment rail for its top-misalignment case.
- Reducer tests cover AC-70 (note line written into StagedAnnotation.comment, replaced on repeat, user prose preserved), AC-72 (Dumping -> Ready/Unavailable), the missing-index guard and the Unavailable path of SC-19 against a fake DumpHierarchyUseCase.
- A repository-level test drives DeviceRepositoryImpl.dumpHierarchy through a fake DeviceCommandExecutor: the dump and `wm density` are issued in ONE call, and garbage stdout maps to an Empty-shaped snapshot.
- DeviceArgvTest's exhaustive case list includes UiAutomatorDump and asserts its argv exactly, and a test asserts UiAutomatorDump maps to MirrorTuning.DumpTimeoutMs by reading the constant rather than a literal.
- `grep -n '\-> Unit' presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt` prints nothing -- the structural check from CARRY-FORWARD-inert-intents.md that no new intent shipped inert.
- `./gradlew :data-device-api:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest` green; `compileTestKotlinIosArm64` green for data-device-api and presentation-feature-mirror (the backtick-name guard); `:presentation-core-ui:build` green because MeasureCard is touched.
- Light and dark @Previews of a measuring frozen stage exist AND have actually been rendered by a human (a preview that compiles but was never rendered is the drag-annotate verdict's W-2 waiver repeating); the AC-67/AC-69/AC-71/AC-72 demo script has been run on real hardware with the device's dpi recorded in the verdict so one number can be checked by hand.
- MeasureCard's KDoc no longer describes the card as a sidebar panel if this spec floats it over the stage, and documents the `footer` slot.
- No raw Color/dp/TextStyle introduced at a call site except the one named private val for the design's 228 px panel width; explicitApi satisfied; no new dependency, module or Koin module.
- The `px` degradation path has been seen at least once in the demo (e.g. by stubbing density unknown) so the honest-fallback claim is observed, not assumed.

### successMetrics

- Option-down to panel-visible completes under ~100 ms against an already-cached hierarchy (NFR-2/NFR-13), with the commonTest ceiling assertion as the regression guard.
- Every sent annotation whose note carries an Option-prefixed line carries numeric values rather than a prose estimate -- the PRD's stated reason for the feature.
- On the reference device, at least 2 of 3 step-1 dumps parse into a node list whose root bounds match the frozen frame's device size; if fewer, the step-2 gate decides the exit.
- Measured dropped-frame delta across a freeze-with-dump is recorded as a number (not an impression) from TransportStats, so operator decision 9 can be revisited with evidence.

### architecture

#### approach

Four layers over existing plumbing plus one new pure package: (1) one new closed-set DeviceCommand case and a never-throwing commonMain parser in data-device-api; (2) a HierarchySnapshotModel carried through one new DeviceRepository member and one new resultLauncher use case; (3) all measure arithmetic as internal, Compose-free, Orbit-free functions in presentation-feature-mirror/core/measure/, returning the public result types in MirrorContract; (4) the cached snapshot on a third StateFlow beside frame/frozenFrame, two new intents through the Orbit reducer, and rendering through the three shipped Measure* components plus one Canvas, every coordinate routed through the single existing FrameGeometry.

#### rationale

Every layer choice resolves to the option that keeps this feature inside the repo's only real verification surface -- commonTest, with no Compose UI harness and no Skiko on this host -- without adding a dependency, a module, or a behaviour change to F-4. The split in layer 3 is drawn exactly where testability lives: nothing in core/measure imports Compose or Orbit, so every rule in the design's executable spec becomes an assertion. The split in layer 4 is drawn where frequency changes: 60 Hz hover and Alt state stay in `remember` (F-4's documented discipline), while the user-meaningful readout and the note write go through the reducer, which is also the only way AC-70 is testable without a real key event. The dump stays fire-and-forget off the freeze transition because the design's own NFR-2 analysis shows any on-keypress dump misses the budget by an order of magnitude.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Measure algorithm in domain-core behind a MeasureAnnotationUseCase | domain-core must stay dependency-free, so DeviceRect would have to be duplicated or moved, churning F-4's confirmed contract; and it wraps pure arithmetic in a suspend/Result boundary inside a ~100 ms budget nobody asked to widen. Six files across three modules to express one pure function, bought for a reuse (iOS) that is explicitly out of scope. |
| Parse the dump with javax.xml or an XML library in desktopMain | Breaks data-device-api's load-bearing invariant that every parser is commonMain and hardware-free, drags the desktop impl onto the test classpath, and makes iosArm64 commonTest compilation of the fixtures impossible -- all for a machine-generated format with four attributes we care about. |
| Hierarchy nodes inside MirrorUiState or inside FrozenFrame | Puts hundreds of machine-generated nodes into a state object copied on every unrelated reduction and widens the public contract with data no UI renders. FrozenFrame's own KDoc draws this line for exactly this reason. |
| All measure state as plain hoisted Compose state (the ShellState precedent) | Contradicts the confirmed Orbit constraint, and AC-70's note write has to happen in the reducer anyway, so the state would be split regardless. Nothing in this repo could test it. |
| Dump on Option-down for a guaranteed-fresh hierarchy | A ~2.6 s-class adb child on the critical path of a key press against a ~100 ms budget. Ruled out by design-measure-mode.md's own analysis and by operator decision 1. |
| Key events alone for Alt detection (AS-21 as written) | onPreviewKeyEvent needs focus the stage root never requests -- the only one in the file belongs to SelectedComment and exists only while a popover is open -- so a missed key-up leaves a stuck panel and never fires the note write. Kept as the primary signal but complemented by the pointer-modifier sampler and two release guards. |
| Render the AC-67 footer sentence as a sibling Text under the card instead of touching MeasureCard | The design puts the footer INSIDE the card's padded, bordered column; a sibling below the border is visibly a different component. An additive `footer: String? = null` on an organism with zero existing callers is the smaller, source-compatible change -- accepted by the operator at the Stage 2 pre-gate, with decision 6 restated as 'no BREAKING signature change'. |
| Drawing the design's alignment rail as its own MeasureSide enum case | The rail is not a measured side -- it belongs to exactly one finding branch and carries that finding's delta. One nullable `rail: MeasureGuide?` on MeasureReadout expresses it without widening the five-row enum that drives the panel's localised labels. |
| Pause the capture loop for the duration of the dump | Operator decision 9. Freezing is deliberately a render decision, not a transport one; pausing adds a transport state nothing else needs plus a resume path that can fail, to save an fps dip the user cannot see behind pinned pixels. |

#### components

| name | kind | status | responsibility | registryRef |
| --- | --- | --- | --- | --- |
| DeviceCommand.UiAutomatorDump + buildDeviceArgv case | data | new | One new case in the closed sealed set, with argv `[adb, -s, <id>, exec-out, uiautomator, dump, /dev/tty]` -- stdout only, no on-device file, no second adb pull. Keeps NFR-5 enforced by type. DeviceArgvTest's deliberately exhaustive list gains this case. Files: data-device-api/source/model/DeviceCommand.kt + source/parse/DeviceArgv.kt. |  |
| MirrorTuning.DumpTimeoutMs + DeviceCommandExecutorImpl.timeoutMs() branch | data | new | The dump gets its own budget (10_000 ms) instead of inheriting HostCommandTimeoutMs (3_000 ms), which a 2.6 s-class device command would trip intermittently. This is a watchdog, not a calibration: the NEAR-MISS-t4-budget.md precedent says do not trim it toward the measured cost. The executor's `when` reads `Screencap -> CaptureTimeoutMs; else -> HostCommandTimeoutMs`, so this is a one-line branch -- and because the `else` means a missing branch still compiles, the branch and the case land in the same commit. |  |
| parseUiAutomatorDump + HierarchyNodeResource | data | new | Pure, commonMain, never-throwing depth-first flatten of the uiautomator XML into device-pixel nodes (bounds, class, resource-id, clickable, depth). Tolerates a leading warning line and the trailing `UI hierchary dumped to: /dev/tty` line (the PngTrim precedent), unescapes exactly the five XML entities, requires a strict `[l,t][r,b]` bounds shape and SKIPS any node that lacks it, and returns an empty list rather than throwing on anything it cannot read. Files: data-device-api/source/parse/UiAutomatorDumpParser.kt + source/model/. |  |
| HierarchyNodeModel / HierarchySnapshotModel (+ Empty) | data | new | The domain carrier in domain-core/source/model/device/: nodes (id parts, rect, isTappable, depth) plus the dump's own frame width/height and densityDpi (sentinel: 0 or absent means unknown). `Empty` is the first-class value that drives FR-41 for a failed, timed-out, empty or unparseable dump. depth is retained so a later spec can feed HierarchyRow. |  |
| DeviceRepository.dumpHierarchy + HierarchySnapshotMapper | data | new | One new member on the existing repository (no new repository): run the dump and `wm density` in the SAME call so the geometry and the density can never drift apart, map resources to the domain snapshot through a one-direction `common-core.Mapper` in data-repository-impl/core/mapper/ (not a ModelResourceMapper -- there is no reverse direction). Reuses the shipped-but-uncalled parseWmDensity, whose Override-over-Physical preference is already tested. DeviceRepositoryImpl is commonMain and takes an injectable DeviceCommandExecutor, so this member is testable with a fake and no phone. |  |
| DumpHierarchyUseCase / DumpHierarchyUseCaseImpl | logic | new | `suspend operator fun invoke(deviceId: String): Result<HierarchySnapshotModel>` through resultLauncher(errorMapper = Failure.Technical::Platform), so a missing adb, a dead device or a timeout arrives as a Failure instead of an exception crossing into presentation. Registered with singleOf alongside the other device use cases in the existing Koin module -- no new module. |  |
| core/measure/MeasureNode.kt + MeasureProjection.kt | logic | new | Internal feature-local node type reusing the contract's existing DeviceRect (not a second rect type) and the HierarchySnapshotModel -> List<MeasureNode> projection, including the geometry-mismatch guard: if the dump's root bounds disagree with FrozenFrame.deviceSize on either axis the snapshot is discarded as Unavailable rather than measured against coordinates from a different screen. The DiscoveryProjection precedent. This is the only part of core/measure that depends on the domain model, hence step 6's dependency on step 5. |  |
| core/measure/NodeSnap.kt -- snapTarget(annotation, nodes, slackPx = 4) | logic | new | FR-36 / AC-57: fully-contained candidates with 4 px slack per edge, largest w*h first, null when nothing qualifies. One pure function, one test file. |  |
| core/measure/Neighbours.kt -- neighboursOf(target, nodes) | logic | new | FR-37 / AC-58: overlapping nodes excluded entirely, then per side the strict band-overlap test plus the closest-edge pick (right-most-left, left-most-right, lowest-top, highest-bottom). Returns a MeasureNeighbours record with four nullable sides. |  |
| core/measure/Gaps.kt -- gapsOf(target, neighbours, frameSize) | logic | new | FR-38 / AC-59: gap to the neighbour's facing edge, or to the frame edge (0 / FRAME_W / FRAME_H) when a side has none. Device pixels; unit conversion happens after, never here. |  |
| core/measure/Findings.kt -- findingOf(target, neighbours, unit, densityDpi) | logic | new | FR-40 / AC-61..AC-64: an ordered `when` with a single return -- top misalignment (+/-10 px vs the LEFT neighbour), then left misalignment (+/-24 px vs the TOP neighbour), then touch target (tappable && height < 32 dp). Tolerances stay in device px; only the reported delta converts, and the finding string names the neighbour (`vs {id}`). Returns null when none applies, and cannot return the touch-target finding when the unit is Px. Also reports WHICH branch fired, so Measure.kt knows whether to build the alignment rail. |  |
| core/measure/PixelUnits.kt -- toDisplayUnit(px, densityDpi) and MeasureUnit | logic | new | px * 160 / densityDpi, rounded to whole units at the boundary; MeasureUnit.Px with a `px` label whenever densityDpi is unknown or non-positive. Also the single place FR-39's `< 1 unit` suppression is evaluated -- deliberately AFTER conversion, because the design's `if (v < 1) return` is in the display unit. Kept strictly separate from FrameGeometry: PixelUnits is device px -> display unit for TEXT, FrameGeometry is host px <-> device px <-> Dp for LAYOUT. |  |
| core/measure/MeasureFormat.kt | logic | new | FR-45 / FR-43 / AC-66: `{w}×{h}{unit}`, `Δ{v}{unit}` with no space, `({x}, {y})`, the three verbatim finding strings including their `vs {id}` segment, the node id format, and the `⌥ ` note line plus replaceNoteLine. Reuses AnnotationFormat's existing private TIMES constant (`private const val TIMES = '×'`), promoted to internal, so the module keeps exactly one multiplication sign. |  |
| core/measure/Measure.kt -- measure(annotation, nodes, frameSize, densityDpi) | logic | new | The single composition point mirroring the design's own `measure(annotation, unit)`: snap (or fallback), neighbours, gaps, suppression, finding, the alignment rail for the top-misalignment branch, formatting -> MeasureReadout. The only function the reducer calls, and the target of the AC-68 timing assertion and of the design's ELS end-to-end oracle. |  |
| core/measure/AnnotationHitTest.kt -- annotationAt(point, annotations) | logic | new | Pure topmost-wins hit test in device pixels (highest index -- last staged, drawn on top), so the overlapping-annotations tie-break is asserted rather than implied by iteration order. |  |
| MirrorContract result types: MeasureReadout / MeasureValue / MeasureSide / MeasureGuide / HierarchyStatus | logic | new | Public, Compose-free, string-light result types in source/mirror/MirrorContract.kt beside DeviceRect and StagedAnnotation. They are what core/measure RETURNS, so they land with the pure layer rather than after it. Values are formatted strings; row labels are MeasureSide semantics resolved with stringResource in the content layer, the MirrorRow/MirrorErrorCard precedent. MeasureReadout carries `rail: MeasureGuide?` for the design's alignment rail. |  |
| MirrorUiState.measure + hierarchyStatus; MirrorIntent.StartMeasure(index) / EndMeasure + their handlers | logic | new | The reducer passes of an Option-hold: StartMeasure reads frozenHierarchy.value inside intent {}, runs measure(...) once, and stores the readout in one reduce; EndMeasure writes the Option-prefixed note line into StagedAnnotation.comment (replacing any prior one) and clears the readout in one reduce. Both read state.frozen inside reduce; both return state unchanged when the index no longer exists (`?: return@reduce state`, the existing guard). Neither may land as a `-> Unit` branch in handleIntent -- see knownRisksFromQA. |  |
| MirrorViewModel.frozenHierarchy: StateFlow<HierarchySnapshotModel> + dumpAtFreeze() | logic | new | A third StateFlow beside frame/frozenFrame -- machine output, never a reducer decision. Filled fire-and-forget from stageAnnotation on the null->set freeze transition (`viewModelScope.launch { ... }`, never awaited, so the freeze cannot be delayed), using state.selectedDeviceId; reduces hierarchyStatus = Dumping immediately and Ready/Unavailable on resolution -- the Result's VALUE may be dropped on failure, its OUTCOME never. Cleared, with hierarchyStatus = Absent, at the three sites that already null currentFrozenFrame (MirrorViewModel.kt:208 selectPlatform, :283 stopSession, :443 resumeLive); reconnect() untouched. MirrorViewModel gains one constructor parameter, wired in the existing explicit `viewModel { MirrorViewModel(...) }` definition. |  |
| indicatorDot Dumping branch (core/discovery) | logic | new | CORRECTED IN REFINE. The shipped projection is `frozen != null -> Frozen` FIRST, ahead of Streaming/Lost -- its KDoc says so explicitly and DiscoveryErrorProjectionTest pins both `frozenOverridesDiscovery` and `lost`. The change is therefore ONE branch inserted at the very top -- `frozen != null && hierarchyStatus == Dumping -> Dumping` -- leaving the existing order untouched. Resulting precedence: Dumping > Frozen > Live > Dropped > Blocked > Offline. The candidate's 'Lost/Dropped > Dumping > Frozen > Live' would have reordered shipped behaviour and failed the existing table. Pure, and the one piece of AC-72 a test can actually see. |  |
| MeasureOverlay (private composable in MirrorAnnotationStage.kt) | ui | new | A sibling layer above AnnotationOverlay inside the SAME frame-sized Box, so every coordinate goes through the one FrameGeometry: one Target MeasureMark, up to four Neighbour MeasureMarks, one Canvas drawing the 1 px dashed mid-axis guides (PathEffect.dashPathEffect, the AnnotationGutter leader-line precedent) plus the top-misalignment alignment rail, and a MeasureTag numeral centred on each unsuppressed span and on the rail, with containerColor = Theme.color.measure. |  |
| Alt detection layer: overlay-root FocusRequester + a NEW onPreviewKeyEvent + pointer keyboardModifiers sampler | ui | new | FR-44 / AC-71. Hover offset, altHeld and the last dispatched (altHeld, targetIndex) pair live in `remember`. A pointerInput at PointerEventPass.Initial reads each event's position and keyboardModifiers.isAltPressed without consuming anything (focus-independent; the stage already awaits first-down on that pass); a NEW onPreviewKeyEvent on a focus-requesting root covers the stationary-pointer case; EndMeasure is dispatched on KeyUp, on onFocusChanged(lost) and on the next pointer-down. Gated off entirely while dragStart != null (operator decision 7) and while frozen == null. |  |
| MeasureCard call site + additive `footer` parameter | ui | reuse | Floats in the frame-sized Box at TopStart with Theme.spacing.spacingL padding and a named private `MeasureCardWidth: Dp = 228.dp` ahead of the organism's own widthIn(max = 360.dp) -- no geometry change needed. ONE additive, default-valued `footer: String? = null` parameter is added to the organism (ink3 text inside the card's column, below the finding block) because the shipped card has no footer slot and AC-67's sentence lives inside the card border in the design. Zero existing callers, so the change is source-compatible; accepted at the Stage 2 pre-gate under the reworded decision 6 ('no BREAKING signature change'). The same edit corrects the KDoc's 'lives in the sidebar / a panel, not an overlay' sentence. | components.json#measure-card |
| MeasureMark (atom) | ui | reuse | Target (solid) and Neighbour (dashed 4/3) outlines in Theme.color.measure, positioned entirely by the caller's Modifier. Its KDoc explicitly leaves connector lines to the host and names MeasureTag as the carrier of gap labels, which is why the guides and the rail are the caller's Canvas. No signature change. | components.json#measure-mark |
| MeasureTag (atom) | ui | reuse | The gap numerals and the rail's delta label; already carries Theme.typography.monoTag and the white-on-signal rule (its own @Preview ships `Δ6dp` on Theme.color.measure). Called with containerColor = Theme.color.measure, since its default is annotate/red, which a measure mark must never be. Formatting is the caller's job -- MeasureFormat's. No signature change. | components.json#measure-tag |
| StatusIndicator Dumping state | ui | reuse | Shipped with a `Dumping` state that nothing has ever produced. Operator decision 8's reporting surface, reached through the single new indicatorDot branch -- zero new components. (StatusLine also ships a Dumping step; this spec does not wire it, because the mirror status bar renders StatusIndicator via indicatorDot, not StatusLine.) | components.json#status-indicator |
| KeyCap (atom) | ui | reuse | The Option keycap in the card header; already inside MeasureCard with the literal ⌥ glyph. Nothing to do. | components.json#key-cap |
| AnnotationOverlay / AnnotationOverlayMark | ui | reuse | Unchanged. Supplies the Dp-space coordinate convention and the sibling-layer pattern MeasureOverlay sits above; the scrim's cut-outs keep the measured pixels visible. | components.json#annotation-overlay |
| StageSurface | ui | reuse | Unchanged. The measure card floats inside the stage content, in the same frame-sized box the drop banner (TopCenter) and Resume Live (TopEnd) already use -- exactly as CommentPopover already floats there. | components.json#stage-surface |
| FrameGeometry | logic | reuse | The only pointer<->device-px<->frame-px<->Dp converter and the single density site. Every mark, guide, rail and tag position goes through toFrame/toDp with NO second conversion -- the documented Retina half-size bug class. |  |
| resultLauncher + Failure.Technical.Platform | logic | reuse | The dump use case needs no bespoke error type; the ViewModel's getOrNull() is what turns a Failure into the Empty snapshot (NFR-14), while still reducing Unavailable so the outcome is never discarded. |  |
| Measure strings in presentation-core-localisation | ui | new | mirror_measure_title, mirror_measure_row_size/_top/_right/_bottom/_left, mirror_measure_footer, mirror_measure_hierarchy_dumping, mirror_measure_hierarchy_unavailable, mirror_status_dumping. Values (English) only; the Option note line, the finding copy and the node id stay unlocalised machine text. |  |

#### dataModel

| entity | fields |
| --- | --- |
| HierarchyNodeResource (data-device-api) | ["className: String", "resourceId: String", "isClickable: Boolean", "left: Int", "top: Int", "right: Int", "bottom: Int", "depth: Int"] |
| HierarchyNodeModel (domain-core) | ["id: String (formatted per the ratified node-id rule)", "className: String", "left/top/right/bottom: Int (device px)", "isTappable: Boolean", "depth: Int"] |
| HierarchySnapshotModel (domain-core) | ["nodes: List<HierarchyNodeModel>", "frameWidthPx: Int", "frameHeightPx: Int", "densityDpi: Int (0 = unknown)", "companion Empty = HierarchySnapshotModel(emptyList(), 0, 0, 0)"] |
| MeasureNode (feature-internal) | ["id: String", "rect: DeviceRect (reused from MirrorContract)", "isTappable: Boolean"] |
| MeasureNeighbours (feature-internal) | ["left: MeasureNode?", "right: MeasureNode?", "top: MeasureNode?", "bottom: MeasureNode?"] |
| MeasureGaps (feature-internal) | ["leftPx: Int", "rightPx: Int", "topPx: Int", "bottomPx: Int"] |
| MeasureUnit (public enum) | ["Dp", "Px"] |
| MeasureSide (public enum) | ["Size", "Top", "Right", "Bottom", "Left"] |
| MeasureValue (public) | ["side: MeasureSide", "value: String (already formatted with its unit)"] |
| MeasureGuide (public) | ["side: MeasureSide", "label: String", "from: device-px start point (x, y)", "to: device-px end point (x, y)"] |
| MeasureReadout (public) | ["annotationIndex: Int", "nodeId: String", "targetRect: DeviceRect", "neighbourRects: List<DeviceRect>", "values: List<MeasureValue>", "guides: List<MeasureGuide> (sub-unit sides already dropped)", "rail: MeasureGuide? (the design's alignment rail; set only for the top-misalignment finding)", "finding: String?", "unit: MeasureUnit", "noteLine: String (the exact ⌥-prefixed line EndMeasure writes)"] |
| HierarchyStatus (public enum) | ["Absent", "Dumping", "Ready", "Unavailable"] |
| MirrorUiState (extended) | ["+ measure: MeasureReadout? = null", "+ hierarchyStatus: HierarchyStatus = Absent"] |

#### interfaces

| name | contract |
| --- | --- |
| buildDeviceArgv(toolPath, device, DeviceCommand.UiAutomatorDump) | -> [toolPath, "-s", id, "exec-out", "uiautomator", "dump", "/dev/tty"]. Asserted exactly in DeviceArgvTest's exhaustive list. |
| parseUiAutomatorDump(stdout: String): List<HierarchyNodeResource> | Pure, commonMain, NEVER throws. Depth-first order. Skips any node whose bounds do not match the strict `[l,t][r,b]` shape. Returns an empty list for empty, truncated, non-XML or unrecognised input. Unescapes &amp; &lt; &gt; &quot; &apos; only. |
| DeviceRepository.dumpHierarchy(deviceId: String): HierarchySnapshotModel | Runs UiAutomatorDump and WmDensity in one call and maps both into the snapshot. Throws DeviceCommandException on a dead device / missing adb / timeout -- the use case is what converts that into a Failure. Returns an Empty-shaped snapshot (empty node list) rather than throwing when the dump merely produced nothing parseable. |
| DumpHierarchyUseCase.invoke(deviceId: String): Result<HierarchySnapshotModel> | resultLauncher(errorMapper = Failure.Technical::Platform). Never throws past this boundary. The ViewModel's getOrNull() ?: Empty is the NFR-14 degradation point, and the Result is always branched on to reduce Ready or Unavailable. |
| measure(annotation: DeviceRect, index: Int, nodes: List<MeasureNode>, frameSize: IntSize, densityDpi: Int): MeasureReadout | Total function -- never null, never throws, no I/O, no allocation per frame beyond its own result. With no snap match it measures the annotation rect and reports 'View (no match)'. With densityDpi <= 0 the whole readout is MeasureUnit.Px. Deterministic for a given input, which is what makes the design's ELS fixture usable as an oracle. |
| toDisplayUnit(px: Int, densityDpi: Int): Pair<Int, MeasureUnit> | dp = round(px * 160 / densityDpi) when densityDpi > 0, else (px, MeasureUnit.Px). The only density arithmetic outside FrameGeometry, and it never touches host density. |
| MeasureFormat.noteLine(readout): String | `⌥ {nodeId} {w}×{h}{unit} · top {t}{unit} · right {r}{unit} · bottom {b}{unit} · left {l}{unit}` plus ` · {finding}` when present. MeasureFormat.replaceNoteLine(existingComment, line) drops any prior line starting with `⌥ ` and appends the new one, preserving the user's own prose. |
| annotationAt(point: device-px offset, annotations: List<StagedAnnotation>): StagedAnnotation? | Topmost (highest index) containing annotation, or null. Pure. |
| MirrorIntent.StartMeasure(index: Int) / MirrorIntent.EndMeasure | StartMeasure: one reduce, sets state.measure from measure(...) against frozenHierarchy.value; no-op when the index is gone or no frame is frozen. EndMeasure: one reduce, writes the note line onto that annotation's comment and clears state.measure; no-op when state.measure is null (so a spurious release costs nothing). Neither is permitted to be a `-> Unit` branch. |

#### stateManagement

Orbit MVI with three deliberate tiers, matching the module's documented discipline. (1) `remember` inside MirrorAnnotationStage: hover offset, altHeld, and the last dispatched (altHeld, targetIndex) edge -- pointer-rate data that must never enter the reducer. (2) The Orbit reducer: MirrorUiState.measure (the resolved readout) and MirrorUiState.hierarchyStatus, written by exactly two new handlers at one reduce each, both reading state.frozen inside reduce because Orbit launches each intent in its own coroutine and reduces with a CAS update loop. (3) A plain StateFlow outside Orbit: frozenHierarchy, a potentially-hundreds-of-nodes machine artefact that drives no reducer decision -- the same reasoning that keeps frame and frozenFrame out of state, read with .value inside intent {} exactly as stageAnnotation already reads currentFrame.value.

#### navigation

- No navigation change. This surface has no Destination by design (presentation-feature-mirror/CLAUDE.md deviation 1): it is the set of slots the desktop window fills, entered through MirrorHost. navigation.md's five-step checklist is deliberately not applied.

#### affectedModules

- data-device-api (new DeviceCommand case, argv branch, parser, resource model, MirrorTuning constant)
- data-device-impl (one timeout branch in DeviceCommandExecutorImpl)
- domain-core (HierarchyNodeModel / HierarchySnapshotModel)
- domain-repository-api (one new DeviceRepository member)
- data-repository-impl (dumpHierarchy implementation + one-direction mapper + its fake-executor test)
- domain-usecase-api (DumpHierarchyUseCase)
- domain-usecase-impl (impl + Koin registration in the existing module)
- presentation-feature-mirror (core/measure/ package, contract additions, ViewModel additions, stage wiring, indicatorDot branch, tests)
- presentation-core-ui (one additive default-valued MeasureCard parameter + its KDoc correction)
- presentation-core-localisation (new string keys)

### projectContext

#### reuseComponents

| id | name | registryRef |
| --- | --- | --- |
| measure-card | MeasureCard | components.json#measure-card |
| measure-mark | MeasureMark (Target / Neighbour) | components.json#measure-mark |
| measure-tag | MeasureTag | components.json#measure-tag |
| status-indicator | StatusIndicator (Dumping state) | components.json#status-indicator |
| key-cap | KeyCap | components.json#key-cap |
| annotation-overlay | AnnotationOverlay / AnnotationOverlayMark | components.json#annotation-overlay |
| stage-surface | StageSurface | components.json#stage-surface |
| mirror-stage-content | MirrorStageContent / MirrorStatusLine | components.json#mirror-stage-content |
| comment-popover | CommentPopover (suppressed while measuring; the float-over-stage precedent) | components.json#comment-popover |
| hierarchy-row | HierarchyRow (NOT used here; the reason node depth is retained on the model) | components.json#hierarchy-row |

#### reuseAssets

- No new asset, icon or font. The Option glyph and the multiplication/delta/middot signs are text; JetBrains Mono is already bundled for the mono styles.

#### reuseStyles

| name | registryRef |
| --- | --- |
| Theme.typography.monoTag (MeasureTag's style -- gap numerals and the rail's delta) | styles.json#theme-typography |
| Theme.typography.monoData / monoMeta / titleS / body (inside MeasureCard) | styles.json#theme-typography |
| Theme.color.measure (measurement blue), Theme.color.annotate (finding red at 10%), ink/ink2/ink3, surface, line | styles.json#theme-color |
| Theme.spacing spacingXS/S/L (4/8/16) -- the 16 dp card inset is exactly the design's top:16/left:16, and spacingL is also the rail label's offset | styles.json#theme-spacing |
| Theme.cornerToken.card / nested; Theme.stroke.thin for the 1 px guides, the rail and the mark outlines | styles.json#theme-radius-corner |
| Light/dark palette parity is swept by PaletteContrastTest, which already covers these roles | styles.json#palette-contrast-test |

#### existingFeatures

| id | name | registryRef | note |
| --- | --- | --- | --- |
| drag-annotate-frozen-frame | Drag-to-annotate & frozen frame (F-4) | features.json#drag-annotate-frozen-frame | Extended at exactly two points: the freeze transition inside stageAnnotation (fire-and-forget dump) and the stage's frame-sized Box (one sibling layer + the Alt detection modifiers). No existing behaviour changes. Its verdict's W-1/W-2/W-3 waivers are this spec's recurrence set for anything visual. |
| android-device-discovery-mirroring | Android device discovery & live mirroring (F-2) | features.json#android-device-discovery-mirroring | Supplies the adb plumbing; gains one sealed case and one timeout branch. The capture loop is deliberately left running during the dump. |
| error-propagation | Failure / Result error propagation | features.json#error-propagation | The dump use case is one more resultLauncher(Failure.Technical::Platform) body; no new error surface, no new side effect. |
| error-handling-and-recovery | Error handling & recovery | features.json#error-handling-and-recovery | Source of three precedents used here: the timeout-constant near-miss (DumpTimeoutMs gets its own generous budget), the discarded-Result finding (a fire-and-forget dump must still resolve its status), and the inert-intent carry-forward (no `-> Unit` handler branches). |
| localisation | Localisation | features.json#localisation | New keys in values/strings.xml; the Option note line and the finding copy are deliberately unlocalised machine text. |

#### newToBuild

- DeviceCommand.UiAutomatorDump + its argv branch + MirrorTuning.DumpTimeoutMs + the executor timeout branch
- parseUiAutomatorDump + HierarchyNodeResource (+ three real-device commonTest fixtures from the step-1 spike)
- HierarchyNodeModel / HierarchySnapshotModel (+ Empty) in domain-core
- DeviceRepository.dumpHierarchy + HierarchySnapshotMapper + a fake-executor repository test
- DumpHierarchyUseCase + Impl + Koin registration
- presentation-feature-mirror/core/measure/: MeasureNode, MeasureProjection, NodeSnap, Neighbours, Gaps, Findings, PixelUnits, MeasureFormat, Measure, AnnotationHitTest
- MirrorContract: MeasureReadout (incl. rail) / MeasureValue / MeasureSide / MeasureGuide / HierarchyStatus + two MirrorUiState fields + two intents
- MirrorViewModel: frozenHierarchy StateFlow, dumpAtFreeze, startMeasure/endMeasure handlers, three clear sites
- indicatorDot Dumping branch (one branch inserted above the existing `frozen != null` case)
- MeasureOverlay private composable + the guide/rail Canvas + the Alt detection layer + the floating MeasureCard call site
- One additive `footer: String? = null` parameter on MeasureCard, plus its KDoc correction
- Ten new localisation keys
- Light/dark @Previews of a measuring frozen stage

### resources

#### rules

| ref | why |
| --- | --- |
| .claude/rules/package-layout.md | core/ is internal-only, source/ is the public surface, and no empty core/ is created for consistency. Decides that the measure algorithm is internal in core/measure/ while MeasureReadout and friends are public in source/mirror/. |
| .claude/rules/orbit-mvi.md | Contract/ViewModel/Content shape, intents as sealed cases, one handler per intent. Constrains StartMeasure/EndMeasure to the existing pattern. |
| presentation-feature-mirror/CLAUDE.md | The two documented deviations (MirrorHost rather than a Destination; pixels never enter reduced state) and the local testing facts: Orbit dispatcher pinning plus runCurrent() in MirrorFakes, and plain-word camelCase test names. NOTE its apostrophe claim is stale -- testing.md corrected it by experiment -- but its camelCase rule stands and makes the point moot here. |
| .claude/rules/koin-di.md | Constructor injection, singleOf/factoryOf beside the existing device use cases, no new Koin module. The mirror module's `viewModel { MirrorViewModel(...) }` is explicit because of the decode seam's default -- the new use case is added there by hand. |
| .claude/rules/design-tokens.md | No literal Color, raw dp or hardcoded TextStyle at a call site; atom -> molecule -> organism layering. Drives FR-42's token-fidelity reading and the single named private val for the 228 px width. |
| .claude/rules/domain-layer.md | domain-core stays dependency-free; one interface per use case with a single suspend operator invoke returning Result<T>; dependency direction is one-way. |
| .claude/rules/error-handling.md | resultLauncher(errorMapper = Failure.Technical::Platform) in the use case impl; nothing swallowed partway. Also warns against copying the hardcoded 'Error' ShowError shortcut -- this spec posts no side effect at all for a failed dump, it degrades. |
| .claude/rules/mappers.md | ModelResourceMapper is reserved for round-trips; the dump needs a one-direction Mapper, so this rule decides the mapper's shape. |
| .claude/rules/testing.md | kotlin.test is wired once by the convention plugins (never per module); the desktop target is the one that runs; commonTest compiles for every target so backtick names must carry no comma or colon (an apostrophe is fine); `:<module>:build` additionally RUNS the iosSimulatorArm64 suite, which is why the AC-68 timing assertion needs a generous ceiling; no Compose UI harness exists and introducing one is a real infrastructure decision. |
| .claude/rules/platform-coverage.md | Android + iOS + Desktop(JVM) only; no macOS/Linux native targets. The parser must compile for iosArm64 from commonMain, which is why no java.*/javax.* XML parser is used. |
| .claude/rules/localisation.md | Key convention `<feature>_<element>`, English-only values/ today, and the precedent that machine text carries no resource key -- the basis for leaving the Option note line and the finding copy unlocalised. |
| .claude/rules/compose-preview-convention.md | Private `<Name>Preview` wrapped in AppTheme with default-constructed state; the measuring-stage previews are the only rendering check this repo has -- and only once a human actually renders them. |
| data-device-api/CLAUDE.md | Every pure parser lives in that module's commonMain; NFR-5 is enforced by type via the closed DeviceCommand set and the single buildDeviceArgv construction point; no raw-argv escape hatch; PngTrim is the never-throw precedent. |
| data-device-impl/CLAUDE.md | One ProcessBuilder site; withTimeoutOrNull not withTimeout; the NEAR-MISS-t4-budget.md lesson that a wrong timeout constant is the failure mode a stubbed clock does not catch -- the direct justification for DumpTimeoutMs. |
| presentation-core-ui/CLAUDE.md | Kit components take copy as parameters with English defaults and never import localisation; atom/molecule/organism layering; components must not lie about what the app does -- which this spec extends to the KDoc, since MeasureCard's currently describes a sidebar panel. |
| CLAUDE.md | Module map, build commands and the toolchain facts (explicitApi everywhere, Kotlin 2.4.21, desktop as the runnable target). |
| .claude/rules/version-catalog-hygiene.md | No new catalog entry is needed or wanted; this spec adds zero dependencies. |

#### docs

| ref | why |
| --- | --- |
| .pipeline/sdd/docs/measure-mode/inputs/design-measure-mode.md | AUTHORITATIVE on algorithm, copy and geometry; an extraction of a working measure(annotation, unit) implementation, so its ELS fixture is a test oracle rather than an interpretation. Re-read in this refine pass, which is how the dropped `vs {id}` segment and the missing alignment rail were caught. |
| .pipeline/sdd/docs/android-device-discovery-mirroring/MEASURED-nfr-1-capture-rate.md | The 2665 ms screencap measurement that sets the dump's cost expectation and underpins NFR-13's exclusion. |
| .pipeline/sdd/docs/error-handling-and-recovery/execution/NEAR-MISS-t4-budget.md | The recorded near-miss where a command whose own KDoc named one budget was given another; the reason the dump gets its own timeout constant and a test that reads it. |
| .pipeline/sdd/docs/error-handling-and-recovery/execution/CARRY-FORWARD-inert-intents.md | Five intents once shipped as `-> Unit` branches that compiled and tested green. Supplies this plan's `grep -n '-> Unit'` exit check for StartMeasure/EndMeasure. |
| .pipeline/sdd/docs/error-handling-and-recovery/execution/HARDWARE-BLOCKED.md | The reference Pixel 8 was unplugged mid-session and two tasks were recorded BLOCKED rather than waived. The framing this spec adopts for step 1 and the step-13 demos. |
| .pipeline/sdd/docs/error-handling-and-recovery/inputs/reproduced-defect-adb-cold-start.md | The measured 3.03 s adb cold start and the watchdog-not-calibration framing DumpTimeoutMs copies. |
| .pipeline/sdd/docs/drag-annotate-frozen-frame/validation/verdict.md | W-1/W-2/W-3: AC-5/AC-7 carried verificationMethod=test but nothing visual is asserted anywhere, no window was rendered and no screenshot taken because this host cannot load Skiko. The reason AC-10 is demo here and the reason a compiled-but-unrendered preview does not count as verification. |
| .pipeline/sdd/docs/measure-mode/preparation/preparation.md | The approved Stage 1 deliverable this plan implements, including the two follow-ups resolved here (FR-44's rewording, AC-72's minting) and the AC-61/AC-62 finding copy the candidate had truncated. |

### errorStates

| state | behavior |
| --- | --- |
| loading | hierarchyStatus = Dumping for the ~2.6 s between the freeze transition and the dump resolving. The status line shows StatusIndicator's Dumping state via the single new indicatorDot branch; an Option-hold in this window still produces a full readout from the FR-41 fallback, with the panel saying the hierarchy is still being read. The freeze itself is never delayed and no spinner blocks anything (AC-72, operator decision 8). |
| empty | A dump that succeeded but yielded nothing usable -- empty stdout, unparseable XML, every node skipped for bad bounds, or a FLAG_SECURE window -- is indistinguishable from an empty hierarchy and is treated as one: HierarchySnapshotModel.Empty, hierarchyStatus = Unavailable, and every measure reports 'View (no match)' with full values against the annotation rect and the frame edges (FR-41, AC-65, AC-69, SC-16, SC-19). |
| empty | Density unknown (`wm density` failed or parsed to null): the readout is emitted in MeasureUnit.Px and every value is labelled `px`; the <32dp touch-target finding cannot fire. No dp number is ever computed from a guessed density. |
| error | Dump exits non-zero, times out, the device vanishes mid-dump, or adb is missing: DumpHierarchyUseCase returns Failure.Technical.Platform, the ViewModel takes getOrNull() ?: Empty, reduces hierarchyStatus = Unavailable, and the freeze -- which already completed -- is untouched. No side effect, no snackbar, no error card: this is a degradation, not an interruption of something the user asked for (NFR-14, AC-69, SC-19). The status is never left at Dumping. |
| error | Geometry mismatch (the dump's root bounds disagree with FrozenFrame.deviceSize, e.g. a rotation or resolution override between capture and dump): the snapshot is DISCARDED by MeasureProjection and reported Unavailable rather than measured against coordinates from a different screen. |
| error | A release the key path never saw (Cmd-Tab, focus lost, window deactivated): EndMeasure is dispatched from onFocusChanged and from the next pointer event whose keyboardModifiers no longer carry Alt, so the panel cannot stick and the measurement still reaches the note. A spurious EndMeasure with no readout is a no-op. |
| empty | Option held with no annotation under the cursor, over a live (never-frozen) stage, or during an in-progress drag: no intent is dispatched, nothing renders, no state changes. Not an error state and not a reduced flag. |
| error | StartMeasure arrives for an index deleted between hover and key-down: the reducer returns state unchanged via the same `?: return@reduce state` guard every other annotation handler uses. |

### observability

#### logs

- No logging framework is wired in data-device-api/impl (kermit exists only in presentation-core-ui), so this spec adds no log line rather than introducing a logger behind the operator's back. The dump's outcome is observable instead as hierarchyStatus in the reduced state, which is what the status line renders.
- DeviceCommandException already carries a bounded stderr tail (MirrorTuning.StderrTailBytes = 2_048), so a failing dump's stderr is preserved in the Failure's cause for a debugger or a future reporter.

#### metrics

- TransportStats.droppedFrames before/after a freeze-with-dump -- the number that makes operator decision 9's accepted fps dip measurable rather than an impression.
- Dump wall-clock duration, available at the demo via the step-1 spike's own `time adb exec-out uiautomator dump /dev/tty` runs; recorded in the spike note, not instrumented in the app.
- The AC-68 commonTest timing figure for measure(...) over the real fixture's node count, recorded in the verdict as the regression baseline.

#### analyticsEvents

- None. This app ships no analytics pipeline and this spec does not start one.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-M1 | Measure mode is an inspection affordance, not the only route to any outcome: every number it reports is also obtainable from the annotation's own meta line, and no existing keyboard path (arrow/enter row selection, the popover's esc/enter handling, Resume Live) is altered or shadowed by the Alt branch or by the overlay root's focusable(). |
| A11Y-M2 | Colour is never the only carrier: the finding is red AND is literal text naming the dimension, the neighbour and the expected value ('top Δ6dp vs item_title · expected 0' -- which is why the refine pass restored the `vs {id}` segment the candidate had dropped), and the panel's five rows are labelled in words, so a reader who cannot distinguish the measure-blue from the annotate-red still gets the whole message. |
| A11Y-M3 | Light and dark parity for every measure surface (NFR-9), with contrast swept by the existing PaletteContrastTest roles; the measure overlay deliberately carries no wash so the UI under measurement stays readable (MeasureMark's shipped rule). |
| A11Y-M4 | Focus is never trapped or stolen irrecoverably: the overlay root requests focus only when a frame is frozen with no popover open, the popover re-requests focus whenever it opens, and losing window focus ends measure mode rather than leaving an inert panel over the stage. |

### performanceBudget

| metric | target |
| --- | --- |
| NFR-2 / NFR-13 -- Option-down (or Alt-detected hover) to panel-visible: snap + neighbours + gaps + suppression + finding + formatting against the already-cached in-memory hierarchy, plus one Orbit reduce | < ~100 ms. Guarded in commonTest by a ceiling assertion over many iterations of measure(...) against the real fixture's node count, sized to catch an accidental O(n^2) rather than to benchmark the host. Explicitly EXCLUDES the dump. |
| Freeze-time hierarchy dump (explicitly outside the NFR-2 budget) | Fire-and-forget; must add 0 ms of blocking to the freeze transition (the launch is never awaited). MirrorTuning.DumpTimeoutMs = 10_000 ms as a watchdog, ~4x headroom over the 2665 ms screencap-class cost, deliberately NOT trimmed toward the measured number (NEAR-MISS-t4-budget.md). |
| Reducer passes per Option-hold | Exactly 2 per measured target (one StartMeasure, one EndMeasure), dispatched only on a transition of the remembered (altHeld, targetIndex) pair -- so hovering three annotations with Alt held is three StartMeasures plus one EndMeasure, which is the honest reading of Stage 1's 'two passes per hold'. Zero reduces while the pointer moves within the same target, and zero while a drag is in progress. |
| Capture-loop cost of a concurrent dump | Bounded to one dump per freeze; the dropped-frame delta is recorded as a number from TransportStats at the demo. No target is asserted -- operator decision 9 accepts the dip, and this budget exists so it can be revisited with evidence. |
| Reduced-state size | MirrorUiState grows by one nullable readout and one enum only. The node list never enters reduced state, so an unrelated reduction still copies a small object. |

### i18n

#### locales

- en (values/strings.xml is the only locale shipped today; missing keys in a future locale fall back to values/)

#### strings

- mirror_measure_title -- 'Measuring'
- mirror_measure_row_size -- 'Size'
- mirror_measure_row_top -- 'Top'
- mirror_measure_row_right -- 'Right'
- mirror_measure_row_bottom -- 'Bottom'
- mirror_measure_row_left -- 'Left'
- mirror_measure_footer -- 'Numbers attach to the annotation and go into the prompt.'
- mirror_measure_hierarchy_dumping -- pendency copy for AC-72
- mirror_measure_hierarchy_unavailable -- the no-hierarchy explanation for AC-69
- mirror_status_dumping -- the status-line label beside the Dumping dot

#### notes

Row labels, the title and the footer are localised and resolved with stringResource in the content layer, which is why MeasureSide carries semantics and no copy (the MirrorRow/MirrorErrorCard precedent). Deliberately NOT localised: the node id, every numeric value and the whole Option-prefixed note line -- that line is machine text destined for an agent prompt, and localising it would make the sent payload locale-dependent (localisation.md's machine-text carve-out, CommandBlock's precedent). The finding copy ('top Δ6dp vs item_title · expected 0') is likewise literal design copy, unlocalised, because it is embedded verbatim in the note line.

### security

#### dataHandling

- The parser reads only four attributes per node -- bounds, class, resource-id and clickable. The dump also contains every on-screen text and content-desc value, i.e. potentially the user's own data from the app under test; none of it is parsed, stored, reduced or written into a note. This is a deliberate minimisation, not an oversight.
- The snapshot lives only in memory on a StateFlow for the lifetime of one frozen frame and is cleared at the three existing frame-clear sites. Nothing is persisted to disk, to preferences or to the (scaffold-only) database.
- Only the formatted measurement line reaches the annotation note, and therefore the eventual agent payload: numbers, a node id and the finding sentence.

#### permissions

- No new OS permission. The dump requires what mirroring already requires: an authorised USB-debugging session on a device the user explicitly selected.
- NFR-5 holds unchanged: the new command is a case in the closed DeviceCommand set and goes through buildDeviceArgv's single `[adb, -s, <selected id>, ...]` construction point, so it cannot address a device the user did not pick. No raw-argv escape hatch is added.

#### privacy

- `uiautomator dump` runs ON the user's own attached device at the user's own freeze action; nothing leaves the host.
- Writing to `/dev/tty` means no file is created on the device, so the dump leaves no artefact behind on the phone to be cleaned up or leaked.
- A FLAG_SECURE window yields an empty or overlay-only dump, which this spec handles as a degradation rather than attempting any workaround -- the app does not try to defeat a screen-capture protection.

### rollout

#### featureFlags

- None. This repo has no feature-flag mechanism and adding one for a desktop-only internal tool would be the unrequested abstraction. The feature is gated by its own preconditions instead -- a frozen frame, Android, a staged annotation under the pointer -- which are exactly the conditions under which it is meaningful.

#### migrations

- None. No persisted schema, no preference, no database table is touched; the only new state is in-memory.

#### backwardCompatibility

Purely additive. DeviceCommand gains a case -- the exhaustive `when` in DeviceArgv is a compile error until it learns it, while DeviceCommandExecutorImpl.timeoutMs() has an `else` and is NOT, which is why the timeout branch is named as a same-commit obligation and asserted by a test. DeviceRepository gains a member, which breaks only in-repo implementations -- there is one. MirrorUiState gains two defaulted fields, MirrorIntent two cases, MeasureCard one defaulted parameter with zero existing callers (source-compatible; the Stage 2 pre-gate restated decision 6 as 'no BREAKING signature change'). indicatorDot gains one branch above its existing first case, so every row of DiscoveryErrorProjectionTest's current table still holds. F-4 behaviour is unchanged: Resume Live, Reconnect, staging, commenting, deleting and the drop banner all keep their current semantics, and the only edit inside stageAnnotation is an unawaited launch on the existing freeze branch.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| bugs.json (registry is an empty array) | The QA bug registry (.pipeline/sdd/docs/registry/bugs.json) is literally `[]` at the time of planning, so there are no logged defects to pull for the touched areas. Verified in this refine pass; the candidate's substitution is sound and is kept and extended. The entries below are the repo's own recorded near-misses and waivers for exactly these modules, drawn from the three shipped specs' verdicts and the error-handling execution notes -- the closest honest substitute. | A bugs.json that has gained entries by Stage 4 -- re-run this recurrence pass before execution rather than trusting this snapshot. | Use the recorded near-miss/waiver documents below as the recurrence set, and re-read bugs.json at the start of Stage 3. |
| .pipeline/sdd/docs/error-handling-and-recovery/execution/NEAR-MISS-t4-budget.md | A device/host command was written with HostCommandTimeoutMs (3_000 ms) while its own KDoc two lines above named DaemonStartTimeoutMs -- the one function whose purpose was surviving a measured 3.03 s cost got the one budget that cannot. A stubbed clock catches a wrong comparison but not a wrong constant. | DeviceCommandExecutorImpl.timeoutMs() falling through to `else -> MirrorTuning.HostCommandTimeoutMs` for UiAutomatorDump. The `when` has an `else`, so a missing branch compiles clean and turns a 2.6 s-class command into intermittent failures that look like flaky hardware. | Add the explicit `DeviceCommand.UiAutomatorDump -> MirrorTuning.DumpTimeoutMs` branch in the same commit as the sealed case, and assert the mapping in a test that reads the constant rather than a literal. Named in step 3's definition of done. |
| .pipeline/sdd/docs/error-handling-and-recovery/execution/CARRY-FORWARD-inert-intents.md | Five MirrorIntent cases shipped with compile-forced `-> Unit` branches in handleIntent. Everything compiled, every test stayed green, and a button wired to one of them would have done nothing -- the exact defect that spec's FR-37 existed to forbid. A `-> Unit` branch is invisible at the call site. | StartMeasure/EndMeasure landing as `-> Unit` branches because the contract step (9) ran ahead of the handler step (10), or a later step wiring the Alt layer to an intent whose handler was never written. Equally: an Option-hold that renders a panel but writes no note, which is the same defect wearing the UI's clothes. | Steps 9 and 10 are adjacent and step 10 owns the reducer tests, so no intent exists for more than one step without a handler. The structural exit check is adopted verbatim into definitionOfDone: `grep -n '\-> Unit' .../MirrorViewModel.kt` must print nothing. |
| .pipeline/sdd/docs/drag-annotate-frozen-frame/ (FrameGeometry KDoc, proof-of-concept density defect) | A stray density division outside the single conversion site halved every annotation mark on a Retina display. FrameGeometry exists, and documents itself, specifically to prevent a second conversion. | Any `/ density`, `* density`, `.toPx()` or `.dp` arithmetic in the measure overlay, the guide Canvas or the rail outside FrameGeometry; also any attempt to convert the +/-10 / +/-24 px tolerances, which must stay in device pixels (and conversely any attempt to evaluate the 32 touch-target threshold in px, which must stay in dp). | Route every coordinate through geometry.toFrame then geometry.toDp exactly once, keep the overlay inside the same frame-sized Box as AnnotationOverlay, and keep PixelUnits (device px -> display unit, for TEXT) strictly separate from FrameGeometry (host px <-> device px <-> Dp, for LAYOUT). Reviewed as a named check in step 11. |
| .pipeline/sdd/docs/drag-annotate-frozen-frame/ (MirrorViewModel reduce-race precedent, MirrorViewModelAnnotationTest) | Reading state.frozen outside `reduce` let two overlapping intents capture the same pre-image, so a per-keystroke comment racing a delete resurrected a deleted annotation. Every annotation handler therefore reads inside reduce, where Orbit's CAS update loop re-runs the lambda. | startMeasure/endMeasure reading state.frozen (or state.measure) outside reduce, or performing two reduces, which would reopen the same race against CommentAnnotation and DeleteAnnotation -- and EndMeasure writes to the same `comment` field the popover types into, so this is the highest-risk collision in the spec. | One reduce per handler, every read inside it, the `?: return@reduce state` guard for a vanished index, and an extension of MirrorViewModelAnnotationTest's race case covering a measure release racing a delete on the same index. |
| .pipeline/sdd/docs/error-handling-and-recovery/ (COR-7, discarded Result) | Three recovery handlers discarded the Result their use cases returned, making a real failure indistinguishable from a cancelled dialog -- the dead-button class of defect. | `dumpHierarchy(id)` launched and its Result ignored in a way that leaves hierarchyStatus at Dumping forever, so the panel claims a dump is still in flight after it failed. Discarding the VALUE is correct here; discarding the OUTCOME is the bug. | The fire-and-forget launch must still branch on the Result to reduce Ready or Unavailable, and the reducer test asserts the Unavailable transition against a failing fake use case (SC-19). FR-46 and NFR-14 both state it so it cannot be read as optional. |
| .pipeline/sdd/docs/android-device-discovery-mirroring/ (NFR-1 capture-rate waiver, MirrorTuning.CaptureFloorMs KDoc) + error-handling-and-recovery/execution/HARDWARE-BLOCKED.md | A performance constant was shipped unmeasured because no authorised device was reachable, and its KDoc had to carry the harness a human must still run. Later, the reference Pixel 8 that every measurement rested on was unplugged mid-session, leaving two tasks that could only be recorded BLOCKED -- explicitly not waived, because a waiver implies a decision to accept a risk rather than work that could not be attempted. | The same shape here: writing px->dp conversion, the dump timeout or the parser against a hand-written guess because no phone was attached; or step 1 being quietly reported 'done' with synthetic fixtures. Also the inverse failure: marking the AC-67/AC-69/AC-71/AC-72 hardware demos as waived when the honest status is blocked. | Operator decision 10's spike is step 1 and BLOCKS every downstream step; the three real fixtures are the artefact that proves it ran, and step 2 is an operator gate on their content. With no device, step 1 and step 13's demos are recorded BLOCKED and the spec does not close. |
| .pipeline/sdd/docs/drag-annotate-frozen-frame/validation/verdict.md (W-1, W-2, W-3) | F-4 closed with three visual waivers: AC-5/AC-7 carried verificationMethod=test from the PRD although this repo has ZERO Compose UI tests and this host cannot load Skiko in-process (`ImageBitmap(2,3)` throws LibraryLoadException); nothing visual was asserted anywhere; no window was rendered and no screenshot taken; the light/dark previews existed but were never rendered. | This spec repeating all three: an AC whose verificationMethod claims automated coverage the project cannot provide (which is why AC-10 is demo here, not test), and an AC-67/AC-71 sign-off justified by 'the @Preview exists' rather than by a rendered artefact a human looked at. | Every rendering criterion is labelled demo or inspection in this plan, testStrategy.ui states plainly that there is no UI coverage and names exactly what is uncovered, and definitionOfDone requires the previews to have been RENDERED, not merely to compile. |
| presentation-core-ui/CLAUDE.md (picker-copy waiver: components must not lie about behaviour) | Shipped copy once promised an auto-start the build did not implement (`NoSignalStage`'s comp-02 subtitle vs DevicePickerPanel's explicit Start mirroring CTA), and the fix had to be a paired copy/behaviour change. | The card's footer sentence ('Numbers attach to the annotation and go into the prompt.') rendering before the note write actually works, or the panel showing 'View (no match)' while a dump is still pending -- both are the same class of lie, and the second is exactly what AC-72 exists to prevent. Also MeasureCard's own KDoc, which currently calls the card a sidebar panel that is 'not an overlay' while this spec floats it over the stage. | The footer copy lands in step 12, after EndMeasure's note write is implemented and reducer-tested in step 10, so it cannot ship ahead of the behaviour it promises; AC-72's Dumping copy is wired in the same step as the status it reports; and the KDoc correction is in definitionOfDone alongside the `footer` parameter. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate |
| --- | --- | --- | --- | --- |
| 1 | BLOCKING HARDWARE SPIKE: capture three real uiautomator dumps (View-based app, Compose app, scrolling list) as commonTest fixtures, plus `wm density` output and timed `exec-out` runs. Commits whatever it captures; no code depends on its outcome yet. | [] | ["AC-57", "AC-58", "AC-65", "AC-69"] | 0.5d (needs an authorised device attached; BLOCKED, not waived, if none is) |
| 2 | OPERATOR GO/NO-GO GATE -- Stage 4 PAUSES HERE. Record the spike's findings (dump wall-clock, root-bounds-vs-screencap agreement, resource-id density on the Compose app, `wm density` output, whether /dev/tty yielded parseable XML at all) and hand them to the operator. This plan deliberately does NOT pre-commit a failure exit: if the dumps are unusable the OPERATOR chooses, with the evidence, between (a) adding the `dump /sdcard/window_dump.xml` + `cat` second DeviceCommand case and re-running the spike, (b) descoping FR-36..FR-40 and shipping the FR-41 fallback path only, or (c) stopping the spec. No downstream step starts until that choice is recorded. | [1] | ["AC-69", "AC-72"] | 0.25d |
| 3 | adb layer: DeviceCommand.UiAutomatorDump + buildDeviceArgv case + MirrorTuning.DumpTimeoutMs + the DeviceCommandExecutorImpl timeout branch IN THE SAME COMMIT (its `when` has an `else`, so a missing branch compiles); extend DeviceArgvTest's exhaustive list and assert the timeout mapping by reading the constant | [2] | ["AC-69"] | 0.25d |
| 4 | parseUiAutomatorDump + HierarchyNodeResource in data-device-api/commonMain, fixture-tested against the step-1 dumps plus synthetic noise cases (leading warning line, trailing 'dumped to' line, entity-escaped attribute, '>' inside an attribute value, truncated, empty, bad bounds) | [1, 3] | ["AC-65", "AC-69"] | 1d |
| 5 | Domain + repository: HierarchyNodeModel/HierarchySnapshotModel(+Empty), DeviceRepository.dumpHierarchy issuing the dump and `wm density` in ONE call with the one-direction mapper and the reused parseWmDensity path, DumpHierarchyUseCase/Impl through resultLauncher, Koin registration in the existing module; DeviceHierarchyDumpTest against a fake DeviceCommandExecutor | [4] | ["AC-69", "AC-72"] | 0.75d |
| 6 | core/measure/ bottom-up part 1 -- the public MirrorContract result types core/measure RETURNS (MeasureSide, MeasureValue, MeasureGuide, MeasureReadout incl. `rail`, HierarchyStatus), then MeasureNode, MeasureProjection (incl. the geometry-mismatch guard), PixelUnits (incl. the Px degradation), snapTarget, neighboursOf, gapsOf, annotationAt; one test file per rule. Only MeasureProjection needs step 5's domain model -- the geometry functions can start as soon as step 2's gate clears | [2, 5] | ["AC-57", "AC-58", "AC-59"] | 1.5d |
| 7 | core/measure/ part 2 -- findingOf (ordered, single return, never-two assertion, reporting WHICH branch fired so the rail can be built), guide suppression below 1 display unit, MeasureFormat (× via the promoted TIMES constant, Δ delta, position, node id with the '[n]' collision index, the three finding strings INCLUDING their `vs {id}` segment, the ⌥ note line and its replace rule) | [6] | ["AC-60", "AC-61", "AC-62", "AC-63", "AC-64", "AC-66"] | 1d |
| 8 | Compose measure(...) over the parts, including the alignment rail for the top-misalignment branch; port the design's own ELS fixture into commonTest as the end-to-end oracle; add the fallback case and the AC-68 ceiling-timing assertion over the real fixture's node count | [6, 7] | ["AC-65", "AC-68", "AC-10"] | 0.75d |
| 9 | MirrorContract state surface: MirrorUiState.measure + hierarchyStatus (both defaulted), MirrorIntent.StartMeasure/EndMeasure; extend MirrorContractTest. Minted immediately before their handlers, never ahead of them | [8] | ["AC-70", "AC-72"] | 0.5d |
| 10 | MirrorViewModel: frozenHierarchy StateFlow, the unawaited dumpAtFreeze on the freeze transition with Dumping -> Ready/Unavailable (outcome never discarded), clearing at the three existing null-out sites, startMeasure/endMeasure handlers (one reduce each, reads inside reduce, real bodies -- no `-> Unit`); the single indicatorDot Dumping branch inserted ABOVE the existing `frozen != null` case; MirrorViewModelMeasureTest + the extended projection table + the extended reduce-race case | [5, 9] | ["AC-69", "AC-70", "AC-72"] | 1.25d |
| 11 | MirrorAnnotationStage wiring: hover + altHeld + dispatch-edge in remember, the focus-independent pointer-modifier sampler at PointerEventPass.Initial, a NEW overlay-root FocusRequester + onPreviewKeyEvent Alt branch, EndMeasure on focus loss and next pointer-down, the drag-in-progress guard, MeasureOverlay (target + neighbour marks, dashed mid-axis guide Canvas, the alignment rail, MeasureTag numerals), popover suppression while measuring; EARLY manual demo of the stuck-panel and popover-focus risks | [10] | ["AC-10", "AC-11", "AC-71"] | 1.5d |
| 12 | Floating MeasureCard call site (TopStart in the frame-sized Box, spacingL inset, named MeasureCardWidth = 228.dp) + the additive `footer` parameter and the KDoc correction on the organism; the ten localisation keys; light and dark @Previews of a measuring frozen stage | [11] | ["AC-67", "AC-11"] | 0.75d |
| 13 | Cross-target gate and demo: `:data-device-api:desktopTest :data-repository-impl:desktopTest :presentation-feature-mirror:desktopTest`, compileTestKotlinIosArm64 for data-device-api and presentation-feature-mirror, `:presentation-core-ui:build`, the `-> Unit` grep; then a HUMAN-rendered preview pass and the hardware demo script for AC-10/11/67/69/71/72 with the device's dpi and the dropped-frame delta recorded. Anything unrunnable for want of a device or a renderer is recorded BLOCKED, not waived | [12] | ["AC-10", "AC-11", "AC-67", "AC-69", "AC-71", "AC-72"] | 0.5d |

### testStrategy

#### unit

- Naming: every test this spec adds in presentation-feature-mirror uses a plain camelCase identifier with no backticks (that module's CLAUDE.md rule), and the parser/argv tests in data-device-api follow the existing backtick style there with no comma and no colon in the name (testing.md, corrected by experiment -- an apostrophe is fine). Nothing proposed below can fail compileTestKotlinIosArm64 on an illegal character.
- UiAutomatorDumpParserTest (data-device-api/commonTest) -- against the three real step-1 fixtures: node count, depth-first order, exact bounds of a known node, resource-id and clickable extraction, the Compose-app fixture's empty-resource-id labelling. Plus synthetic cases: leading warning line, trailing 'UI hierchary dumped to' line, &amp;/&quot; in a content-desc, a '>' inside an attribute value, a truncated document, empty input, and a node whose bounds do not match `[l,t][r,b]` (skipped, not guessed). Never-throws is asserted, not assumed.
- DeviceArgvTest (extended) -- UiAutomatorDump produces exactly [adb, -s, id, exec-out, uiautomator, dump, /dev/tty]; the exhaustive case list grows by one so a future command cannot be added silently.
- DeviceCommandExecutor timeout mapping -- UiAutomatorDump resolves to MirrorTuning.DumpTimeoutMs, read from the constant (the NEAR-MISS-t4-budget lesson: a wrong constant is invisible to a stubbed clock, and this `when` has an `else` that hides a missing branch).
- DeviceHierarchyDumpTest (data-repository-impl/commonTest, ADDED IN REFINE) -- with a fake DeviceCommandExecutor: dumpHierarchy issues UiAutomatorDump and WmDensity in ONE call (so geometry and density cannot drift -- the candidate asserted this invariant in prose and tested it nowhere); a parseable dump maps to the snapshot with the parsed densityDpi; garbage or empty stdout maps to an Empty-shaped snapshot rather than throwing; a null density maps to densityDpi = 0.
- NodeSnapTest (AC-57) -- containment with 4 px slack on each edge independently, a node 5 px outside rejected, the largest-area winner chosen over a first-found and over a nearer-centre candidate, equal-area determinism, null when nothing qualifies, a degenerate 0-area annotation.
- NeighboursTest (AC-58) -- per side: band overlap required, overlapping nodes excluded entirely, right-most-left / left-most-right / lowest-top / highest-bottom picks, a diagonal node excluded on both axes, a node exactly edge-touching included per the `<=` form, sides independently null.
- GapsTest (AC-59) -- gaps to each neighbour's facing edge; each missing side measured to 0 / FRAME_W / FRAME_H; all four missing at once; never omitted.
- GuideSuppressionTest (AC-60) -- a 2 px gap on a 420 dpi device converts below 1 dp and yields no guide and no numeral; a gap at exactly 1 unit renders; the panel's five value rows still print even when their guide is suppressed; nothing is rounded up to 1.
- FindingsTest (AC-61..AC-64) -- top misalignment inside +/-10 px fires; at 11 px does not; 0 delta does not; left misalignment inside +/-24 px fires only when no top misalignment applies; touch target fires only when neither alignment finding applies and the node is clickable and under 32 dp; a case where all three conditions hold returns exactly ONE finding and it is the top misalignment; a tappable 20 dp node with density unknown returns no finding; each alignment finding names its neighbour (`vs {id}`).
- PixelUnitsTest -- 420 dpi: 1080 px = 411 dp; 160 dpi: 1 px = 1 dp; 0 or negative dpi returns (px, MeasureUnit.Px); rounding is to the nearest whole unit.
- MeasureFormatTest (AC-66, FR-43, FR-45) -- Size uses × asserted by code point U+00D7 (never the letter 'x'); delta is U+0394 + value + unit with no space; position is '({x}, {y})'; the three finding strings verbatim including the `vs {id}` segment and the U+00B7 separator; node id with a resource-id, without one, and with a sibling collision ('[n]'); the note line's full shape; replaceNoteLine drops a prior ⌥ line, keeps the user's own prose, and leaves a note with no ⌥ line untouched.
- MeasureFallbackTest (AC-65) -- an empty node list and a list with no contained node both report 'View (no match)' with five populated values computed against the annotation rect and the frame edges.
- MeasureProjectionTest -- HierarchySnapshotModel.Empty projects to an empty node list; a snapshot whose root bounds disagree with the frozen frame's device size on either axis is discarded.
- AnnotationHitTestTest -- topmost (highest index) annotation wins among overlapping ones; a point outside every annotation returns null.
- MeasureRailTest (ADDED IN REFINE) -- the top-misalignment branch populates MeasureReadout.rail spanning the left neighbour's left edge to the target's right edge at the neighbour's y, labelled with the same Δ delta as the finding; the left-misalignment and touch-target branches and the no-finding case leave rail null.
- MeasureEndToEndTest (AC-10's computable half, AC-68) -- the design's own ELS fixture as an oracle: one measure(...) call reproduces the design's expected node id, five values, guide set, rail and finding; plus the ceiling-timing assertion over the real fixture's node count with a generous bound so it catches an accidental O(n^2) rather than measuring the host. NOTE: `:<module>:build` also RUNS this on iosSimulatorArm64, so the bound must tolerate the slowest target; if it ever proves flaky, convert it to a node-count scaling assertion (time for 10n is not more than ~k x time for n) rather than deleting the guard.

#### integration

- MirrorViewModelMeasureTest (AC-70) -- with a staged annotation and a fake DumpHierarchyUseCase returning a known snapshot: StartMeasure stores a readout whose values match the pure function; EndMeasure writes exactly one ⌥-prefixed line into that annotation's comment and clears the readout; a second hold-and-release REPLACES that line rather than appending; StartMeasure for a deleted index leaves state unchanged; EndMeasure with no readout is a no-op.
- MirrorViewModelMeasureTest (AC-72, SC-19) -- the freeze transition reduces hierarchyStatus = Dumping before the fake use case completes; a success reduces Ready and populates frozenHierarchy; a Failure reduces Unavailable, leaves frozenHierarchy Empty, and does NOT delay or alter the freeze (the frozen frame and the staged annotation are present either way); ResumeLive/StopMirroring/SelectPlatform clear both the snapshot and the status, Reconnect clears neither.
- DiscoveryErrorProjectionTest (extended) -- the indicatorDot table gains the Dumping cases and pins the corrected precedence Dumping > Frozen > Live > Dropped > Blocked > Offline. Every existing row, including frozenOverridesDiscovery and lost, must still pass unchanged: the new branch is inserted above `frozen != null`, it does not reorder anything. This is the one automatable part of AC-72's reporting.
- MirrorViewModelAnnotationTest (extended) -- the existing reduce-race precedent gains a measure case: an EndMeasure note write racing a DeleteAnnotation on the same index leaves no resurrected annotation.
- MirrorContractTest (extended) -- defaults of the two new MirrorUiState fields; the readout's value-equality (so an unchanged measure cannot cause a recomposition storm).

#### ui

- NONE, and that is a limitation, not a choice: this repo has no Compose UI-test harness (no runComposeUiTest, no ui-test-junit4) and this host cannot load Skiko in-process -- the drag-annotate verdict's W-1 records `ImageBitmap(2,3)` throwing LibraryLoadException -- so no automated visual or geometry assertion is possible. Introducing a harness is a real infrastructure decision outside this spec's scope (testing.md).
- Substitute 1 -- @Preview pairs: a measuring frozen stage in light and dark (target + neighbour marks, guides, rail, numerals, the floating card with a finding and footer, the Dumping variant). These count only once a human has RENDERED them: W-2 of the F-4 verdict waived exactly this because previews existed and no window was ever drawn.
- Substitute 2 -- a written demo script run on hardware for AC-10, AC-11, AC-67, AC-69, AC-71 and AC-72's visible half, recording the device's reported dpi so a reviewer can verify one dp figure by hand, the px-degradation path seen at least once, and the TransportStats dropped-frame delta across the freeze.
- Explicitly uncovered by any test: mid-axis guide and rail placement in the frame's coordinate space, panel geometry and copy fidelity, Alt key-up delivery and focus behaviour, the real dump's content. Each is mitigated by construction (one FrameGeometry, one coordinate space, three release paths, three real fixtures) and verified by eye -- or recorded BLOCKED if no eye and no device are available.

#### linkedScenarios

- SC-5
- SC-16
- SC-17
- SC-18
- SC-19

#### coverageGoal

Every numeric rule in design-measure-mode.md has at least one commonTest assertion (snap containment and the 4 px slack, largest-area-first tie-break, per-side neighbour selection, gap-to-frame-edge, sub-unit guide suppression, the three-branch finding precedence plus the never-two assertion and the `vs {id}` segment, the alignment rail, the no-match fallback, px->dp including the Px degradation, and every formatting rule), plus the design's ELS fixture as an end-to-end oracle and the parser against three real device dumps. The repository's one-call dump+density pairing and the Empty mapping are asserted against a fake executor. The reducer's two new handlers and the Dumping/Unavailable transitions are asserted without a key event or a renderer. No percentage target: the honest statement is that 100% of the arithmetic is covered and 0% of the rendering is, and the plan says which criteria are therefore demo-only (AC-10, AC-11, AC-67, AC-69, AC-71, and the visible half of AC-72).

### traceabilityMatrix

| ac | functional | components | tests |
| --- | --- | --- | --- |
| AC-10 | ["FR-10", "FR-36", "FR-37", "FR-38", "FR-39", "FR-42", "FR-44", "FR-46"] | ["core/measure/Measure.kt", "MeasureOverlay", "MeasureCard call site + additive footer parameter", "Alt detection layer", "MirrorViewModel.frozenHierarchy"] | ["SC-5", "MeasureEndToEndTest", "NodeSnapTest", "NeighboursTest", "GapsTest", "demo (rendering, step 13)"] |
| AC-11 | ["FR-10", "FR-40", "FR-43", "FR-45"] | ["core/measure/Findings.kt", "core/measure/MeasureFormat.kt", "MeasureCard call site (finding block)", "MirrorIntent.EndMeasure handler"] | ["SC-5", "SC-17", "FindingsTest", "MeasureFormatTest", "MirrorViewModelMeasureTest", "inspection (red block in both themes)"] |
| AC-57 | ["FR-36"] | ["core/measure/NodeSnap.kt", "core/measure/MeasureProjection.kt"] | ["NodeSnapTest", "MeasureEndToEndTest", "UiAutomatorDumpParserTest (the real fixtures the snap runs against)"] |
| AC-58 | ["FR-37"] | ["core/measure/Neighbours.kt"] | ["NeighboursTest", "MeasureEndToEndTest"] |
| AC-59 | ["FR-38"] | ["core/measure/Gaps.kt"] | ["GapsTest", "MeasureFallbackTest"] |
| AC-60 | ["FR-39"] | ["core/measure/PixelUnits.kt", "core/measure/Measure.kt", "MeasureOverlay (guide Canvas)"] | ["GuideSuppressionTest"] |
| AC-61 | ["FR-40", "FR-39", "FR-45"] | ["core/measure/Findings.kt", "core/measure/MeasureFormat.kt", "MeasureOverlay (alignment rail)"] | ["FindingsTest", "MeasureFormatTest", "MeasureRailTest"] |
| AC-62 | ["FR-40", "FR-45"] | ["core/measure/Findings.kt", "core/measure/MeasureFormat.kt"] | ["FindingsTest", "MeasureFormatTest"] |
| AC-63 | ["FR-40", "FR-45"] | ["core/measure/Findings.kt", "core/measure/PixelUnits.kt", "parseUiAutomatorDump (clickable)"] | ["SC-18", "FindingsTest", "PixelUnitsTest", "UiAutomatorDumpParserTest"] |
| AC-64 | ["FR-40"] | ["core/measure/Findings.kt"] | ["FindingsTest (all-three-true case)"] |
| AC-65 | ["FR-41", "FR-38", "FR-46"] | ["core/measure/Measure.kt", "core/measure/MeasureProjection.kt", "HierarchySnapshotModel.Empty"] | ["SC-16", "MeasureFallbackTest", "MeasureProjectionTest"] |
| AC-66 | ["FR-45", "FR-43"] | ["core/measure/MeasureFormat.kt", "AnnotationFormat.TIMES (promoted to internal)"] | ["MeasureFormatTest (asserted by code point)"] |
| AC-67 | ["FR-42", "FR-45", "NFR-9"] | ["MeasureCard (+ footer parameter, + KDoc correction)", "KeyCap", "MeasureCard call site", "measure localisation keys", "light/dark @Previews"] | ["demo (hardware, both themes, dpi recorded, previews actually rendered)", "PaletteContrastTest (token contrast only)"] |
| AC-68 | ["FR-36", "FR-37", "FR-38", "FR-40", "NFR-2", "NFR-13"] | ["core/measure/Measure.kt", "MirrorIntent.StartMeasure handler", "MirrorViewModel.frozenHierarchy"] | ["MeasureEndToEndTest (ceiling-timing assertion over the real fixture)"] |
| AC-69 | ["FR-41", "FR-46", "NFR-14"] | ["DumpHierarchyUseCase", "MirrorViewModel.dumpAtFreeze", "parseUiAutomatorDump (never-throw)", "MirrorTuning.DumpTimeoutMs + the executor branch"] | ["SC-19", "MirrorViewModelMeasureTest (Unavailable path)", "DeviceHierarchyDumpTest", "UiAutomatorDumpParserTest (empty/truncated)", "demo (unplug mid-freeze)"] |
| AC-70 | ["FR-43", "FR-10"] | ["MirrorIntent.EndMeasure handler", "core/measure/MeasureFormat.kt (noteLine / replaceNoteLine)", "MirrorUiState.measure"] | ["SC-17", "MirrorViewModelMeasureTest", "MirrorViewModelAnnotationTest (race with delete)", "MeasureFormatTest"] |
| AC-71 | ["FR-44"] | ["Alt detection layer (focus + pointer-modifier sampler + three release paths)", "core/measure/AnnotationHitTest.kt", "MirrorIntent.StartMeasure/EndMeasure"] | ["AnnotationHitTestTest (target choice only)", "MirrorViewModelMeasureTest (a spurious EndMeasure is a no-op)", "demo (hold, release, Cmd-Tab mid-hold, Alt during a drag)"] |
| AC-72 | ["FR-46", "FR-41", "NFR-13", "NFR-14"] | ["MirrorViewModel.dumpAtFreeze (Dumping reduce)", "HierarchyStatus", "indicatorDot Dumping branch", "StatusIndicator (Dumping)", "MeasureCard call site (pendency copy)"] | ["MirrorViewModelMeasureTest (Dumping -> Ready/Unavailable)", "DiscoveryErrorProjectionTest (extended table, existing rows unchanged)", "demo (freeze then hold Option within ~2.6 s) for the rendered copy"] |

### openQuestions

- The design draws an ALIGNMENT RAIL for the top-misalignment finding -- 'from the left neighbour across to the target's right edge, labelled Δ{d}{unit} 16 px above it' -- which the candidate omitted entirely. This refine pass added it (FR-39, MeasureReadout.rail, MeasureOverlay, MeasureRailTest) because the design file is authoritative on geometry and the cost is one Canvas line plus one MeasureTag. Survey question 2 offers the operator the chance to defer it instead.
- The measure card floats at TopStart inside the frame box, so it can cover an annotation in the top-left corner of the device screen. The design places it there, and popovers are suppressed while measuring, but a measured annotation underneath the card is still obscured. Survey question 3 asks whether to keep that as designed or pre-authorise the cheap fallback (flip to TopEnd when the measured rect intersects the card).
- MirrorTuning.DumpTimeoutMs is set to 10_000 ms as a watchdog with ~4x headroom over the 2665 ms screencap-class cost, deliberately not trimmed toward it. Step 1 measures the real dump cost; if a dump is materially slower than a screencap the constant should be revisited at the step-2 gate, not at review.
- bugs.json is still an empty array, so the recurrence pass drew on the three shipped specs' verdicts and the error-handling execution notes instead. Re-run the pass at the start of Stage 3 in case entries land in the meantime.
- iOS measure-mode parity remains undecided and out of scope (AS-19); it needs an iOS hierarchy-dump equivalent that does not exist in this codebase.
- NOT a question, recorded so it is not mistaken for one: the step-1 spike's failure exit is the operator's to choose at the step-2 gate, with the spike's evidence in hand (pre-gate decision 2). This plan lists the three candidate exits and commits to none.

### readyForBacklog

true
