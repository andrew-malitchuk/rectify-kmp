# drag-annotate-frozen-frame — audit

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | audit |
| status | done |
| producedBy | sdd-build-validation-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-15T05:34:05Z |
| updatedAt | 2026-10-15T05:34:05Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-15T05:34:05Z

## Provenance

### selfHash

f5427c95f2ff7da5efd11382b00bc206e541a89b2fb43e5fb53976db479389fb

### inputs

| artifact | hash |
| --- | --- |
| plan/plan.json | 19e5721b179547175cdc20448c90994475aefbd6f9c608c9a5f955f674e098cf |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### iteration

1

### verdict

complete-with-waivers

### summary

13/13 backlog tasks are genuinely implemented in the merged tree (verified file-by-file against the diff, not against reports). A forced --rerun-tasks per-module desktop run executed 191 tests across 7 test-bearing modules with 0 failures and 0 skips. No AC is unimplemented. Four ACs (AC-5, AC-7, AC-9, AC-51) cannot be closed by any artefact in this repo and need a human demo/render pass; that is an environment limit already recorded, not a defect. Two substantive behavioural findings are raised for a human ruling (AF-1 popover visibility asymmetry, AF-2 SC-4 demo is unreachable without pressing Escape first). Scope discipline is clean.

### findings

| id | severity | about | area | ref | target | recommendedTarget | issue | evidence | description |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| AF-1 | warn | ac | ac | FR-8 | T-11 | T-11 | Popover visibility is asymmetric between the crowded and uncrowded cases, and the uncrowded case loses information. SelectedComment renders a popover for the SELECTED annotation only (`val selected = frozen.selected ?: return`). AnnotationGutter — which renders a read-only AnnotationPopover for every NON-selected annotation — is composed only when `anyOverlap(...)` is true. Net effect: with two uncrowded annotations, the comment typed on #1 disappears from the stage the moment #2 is selected (it survives only in the Notes side panel); with two CROWDED annotations the same comment stays visible in the gutter. Crowding therefore makes more content appear, not less. FR-8 reads 'show a per-annotation popover'; the implementation shows one editable popover plus, conditionally, read-only copies. | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt:273-286 (`if (anyOverlap(nominalPopoverRects(...))) { AnnotationGutter(...) }` then unconditional `SelectedComment(...)`) and :478 (`val selected = frozen.selected ?: return`). No test covers it; no preview shows the uncrowded-two-annotation case (MirrorFrozenStagePreview has selectedIndex = 2 and its two marks are far apart, so it renders exactly this state with annotation #1's comment invisible). | Non-selected annotations show no popover unless crowding triggers the gutter, so committed comments vanish from the stage in the uncrowded case while staying visible in the crowded one. |
| AF-2 | warn | sc | sc | SC-4 | T-11 | T-11 | SC-4's canonical state ('draw three annotation boxes close together, popovers relocate to the gutter') is not reachable by performing SC-4's own steps. `stageAnnotation` auto-selects every new annotation (`selectedIndex = staged.size`) and `AnnotationGutter` excludes the selected one (`filter { it.index != frozen.selectedIndex }`), so after the third drag the gutter holds 2 rows, never 3. The operator must press Escape before the state the scenario describes exists. T-11's own report flags this, so it is known — but it means the SC-4/AC-9 demo screenshot cannot be shot by following the scenario verbatim, and the scenario text should be amended rather than the behaviour quietly diverging from it. | MirrorViewModel.kt stageAnnotation: `reduce { state.copy(frozen = frozen.copy(annotations = staged, selectedIndex = staged.size)) }`; MirrorAnnotationStage.kt:378 `val relocated = frozen.annotations.filter { it.index != frozen.selectedIndex }`; execution/T-11/report.json needsHumanReview.reason. | Auto-selection of each new annotation means at most n-1 popovers ever reach the gutter; SC-4 as written cannot be demonstrated without an extra Escape. |
| AF-3 | warn | ac | ac | AC-5 | human-gate | human-gate | AC-5 and AC-7 carry verificationMethod 'test' from the PRD, and the rendered half of both is asserted by nothing: the repo has zero Compose UI tests and this host cannot load Skiko in-process. Unasserted: the dashed border, the live Ruler tick guides, the 42% scrim, popover autofocus and the delete affordance. The automatable halves ARE genuinely covered (FrameGeometryTest 9/9, AnnotationFormatTest 3/3, MirrorViewModelAnnotationTest 13/13), and the substitution is pre-declared in plan.json's verificationNote and preparation binding decision #9 — so this is a waiver to grant at the gate, not a change request. Adopting a compose-ui-test harness is explicitly out of scope and already raised as a follow-up ticket. | plan.json acceptanceCriteria[AC-5].verificationNote; preparation.json bindingDecisions[9]; real run: 191 tests / 0 failures, of which 0 are UI tests (no compose-ui-test dependency anywhere in gradle/libs.versions.toml). | AC-5/AC-7 verification-method downgrade (test -> inspection+demo). Needs an explicit human waiver at the gate. |
| AF-4 | warn | task | state | state.json | orchestrator | orchestrator | state.json carries `"tasks": {}` — zero per-task records — despite 13 executed tasks with reports on disk, so task completeness could not be reconciled against recorded status at all and had to be derived entirely from the real file tree and the diff. Separately, .pipeline/sdd/docs/index.json still records this spec at `currentStage: "preparation"` while state.json says `validation` and marks execution done. | .pipeline/sdd/docs/drag-annotate-frozen-frame/state.json line 33 `"tasks": {}`; .pipeline/sdd/docs/index.json entry for drag-annotate-frozen-frame. | Pipeline bookkeeping is stale: empty tasks map in state.json and a preparation-stage entry in docs/index.json. |
| AF-5 | info | diff | diff | T-8 | T-8 | T-8 | B3's hit-test reasoning is SOUND and the fix is correct. Verified: (a) detectDragGestures begins with awaitFirstDown(requireUnconsumed = false), so consumption by a descendant genuinely cannot stop it — only failing to be hit can; (b) Compose hit-tests a Box's children back-to-front and stops descending into lower siblings once one reports a hit, and the drag layer is an EARLIER sibling than SelectedComment and the Resume Live button, so the popover plate and the button take the pointer; (c) the shield is on an ANCESTOR of the text field, and ancestors do not remove descendants from the hit path — the field keeps its own click, caret placement and selection-drag gestures, which are unmodified kit code. One nit: the shield's `awaitEachGesture { awaitFirstDown(...) }` body is ceremonial — a pointerInput node participates in hit testing regardless of what its handler does, so `pointerInput(Unit) {}` would shield identically. Harmless; keep or simplify. | MirrorAnnotationStage.kt:226-296 (sibling ordering) and :532-536 (popoverPointerShield); CommentPopover.kt CommentField is an unmodified BasicTextField under that Box. | B3's hit-test fix verified sound; shield's suspend body is ceremonial but harmless. |
| AF-6 | info | diff | diff | T-11 | T-11 | T-11 | The gutter's relocated AnnotationPopover rows carry no popoverPointerShield, and the gutter sits inside the frame box, so a drag begun on a relocated popover falls through to the drag layer and stages a new annotation across the gutter. Same bug class the shield was introduced for, one composable over. Low impact (the gutter only appears in the crowded case, and the result is a stray annotation the user can delete). | MirrorAnnotationStage.kt:403-412 — AnnotationPopover with only align/offset/width modifiers; contrast :503 `.popoverPointerShield()`. | Gutter popovers are not shielded from the drag layer. |
| AF-7 | info | diff | performance | NFR-1 | T-6 | T-6 | `dragCurrent` is read in MirrorAnnotationStage's composable body (via `dragRect`), so every pointer-move event recomposes the whole stage subtree, including MirrorFrameView and its Image, rather than invalidating only the draw phase. NFR-1 as written concerns the capture/decode/transport path, which the diff genuinely does not touch, so NFR-1 holds — this is a UI-thread cost during a drag only. Acceptable at this fidelity; note it if a drag ever feels heavy on a large window. | MirrorAnnotationStage.kt:201-217 (`var dragCurrent by remember`, `val dragRect = dragStart?.let { ... }` in the composable body) and :220 MirrorFrameView composed below it. | Live drag recomposes the stage subtree per pointer move instead of invalidating draw only. |
| AF-8 | info | diff | design-tokens | CommentPopover.kt | none | none | RULING on the pre-flagged item: CommentPopover.kt's hardcoded colours are FOUR, not three (PopoverInk, PopoverMetaInk, PopoverMeasureInk, FieldFill at lines 68-73), all pre-existing — `git show development:...CommentPopover.kt` has them at lines 66-71, introduced in 38b415a 'feat: add shared ui kit components'. This spec added zero colour literals to the file (its diff adds only closeLabel/focusRequester plumbing), and the literals carry an explicit KDoc rationale (the plate is deliberately dark in BOTH themes, so theme-following ink would invert and vanish). Not a defect of F-4 and not a change request against it. The real debt is that design-tokens.md forbids the pattern without carving out the always-dark-over-unknown-content exception that CommentPopover, CounterBadge and the new scrim all rely on — a doc/tokens follow-up, owned by nobody in this spec. | git show development:presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/annotation/CommentPopover.kt \| grep 'Color(0x' -> lines 66,67,68,71; branch diff for that file contains no Color literal. | The three (actually four) CommentPopover colour literals are pre-existing and documented; not an F-4 defect. design-tokens.md should legitimise the exception. |
| AF-9 | info | diff | inconsistent-pattern | T-5 | T-5 | T-5 | StyleguideScreen.kt gained two cosmetic blemishes: a duplicated `ShowDivider()` immediately followed by another `ShowDivider()` at the new overlay section, and two imports inserted out of alphabetical order (`...kit.molecule.annotation.AnnotationOverlay` / `AnnotationOverlayMark` placed after `...kit.organism.error.ErrorCard`). The repo has no ktlint/detekt/spotless (grepped: zero hits in any .kts/.toml/.properties), so neither fails any build — showcase-only noise. | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/showcase/StyleguideScreen.kt diff hunk @@ -433,6 +436,19 @@ (two consecutive `ShowDivider()`) and @@ -65,6 +66,8 @@ (import order). | Duplicate ShowDivider and out-of-order imports in StyleguideScreen; cosmetic, no lint tooling to catch them. |
| AF-10 | info | ac | ac | AC-47 | none | none | RULING on the hand-written FakeFrameBitmap: the test is a REAL assertion, not circular. The fake supplies only identity plus two deliberately distinct dimensions (width = 100 + bytes.size, height = 200 + 2*bytes.size); it has no knowledge of and no influence over WHICH instance the ViewModel pins, which is the entirety of AC-47. The load-bearing assertion is `assertSame(atRelease, viewModel.frozenFrame.value)` — object identity, which a fake cannot fake. Two independent strengtheners: (1) both axes differ per frame, so a one-dimension coincidence cannot pass it; (2) the asserted deviceSize (109x218) is deliberately unequal to MirrorFrameModel.widthPx/heightPx (1080x2400, constant across both frames), which proves FrozenFrame.deviceSize is derived from the pinned BITMAP rather than from the frame model — a stronger claim than the AC asked for. Residual limit, correctly bounded: readPixels is a no-op, so the test asserts which bitmap object was pinned, never which pixels. Since the ViewModel only ever stores a reference, that is exactly the right scope. The companion test `secondDragAddsAnnotationWithoutRefreezing` covers the opposite direction (a newer frame must NOT displace the pinned one). Implementing the ImageBitmap interface by hand was the correct call over adding a Skiko dependency or a build.gradle.kts edit. | MirrorFakes.kt FakeFrameBitmap + sizedFrameDecoder; MirrorViewModelAnnotationTest.kt:98-115 and :117-138; MirrorViewModel.kt stageAnnotation `val held = currentFrame.value ?: return@intent` (the single read). | AC-47's hand-rolled ImageBitmap fake is sound — assertSame on identity cannot be satisfied circularly. |
| AF-11 | info | diff | freeze-semantics | FR-30 | none | none | CONFIRMED from the real diff: freezing is render-only and Resume Live costs no reconnection. MirrorViewModel.kt's diff has exactly five hunks (@@ -1, -82, -103, -169, -225); `sessionDeviceId` appears in only two of them, both pre-existing lines inside selectPlatform/stopSession that already set it to null, and the flatMapLatest capture gate / observeFrames region is not in any hunk at all — byte-identical. stageAnnotation and resumeLive touch only currentFrozenFrame and the reducer. The test evidence matches the code: resumeLiveClearsTheFrozenFrameAndAnnotations asserts session == Streaming(PHONE), startedDevices == [PHONE] and activeCollectors == 1 after Resume Live, i.e. the collector was never torn down. | git diff development...sdd/drag-annotate-frozen-frame -- MirrorViewModel.kt \| grep '^@@' -> 5 hunks; MirrorViewModelAnnotationTest.kt:229-243. | Freeze semantics verified byte-identical on the transport path; Resume Live is free. |
| AF-12 | info | task | i18n | T-12 | none | none | RULING on six-not-seven keys: T-12 is CORRECT. The FROZEN label is produced by `StatusIndicatorState.Frozen.name.uppercase()` on the line directly above the pre-existing `StatusIndicatorState.Live.name.uppercase()` that produces LIVE — a mirror_status_frozen key would have had no call site, and adding one would have made FROZEN localisable while LIVE stayed machine-derived, which is worse than either option alone. All six added keys resolve to a real stringResource call site (verified by grep): placeholder/save_hint/delete/action_resume_live in MirrorAnnotationStage.kt:288,512,514,516 and empty_comment/notes_empty in MirrorNotesTab.kt:53,61 (+ empty_comment again at MirrorAnnotationStage.kt:382). Not a missed requirement. | MirrorContent.kt indicatorLabel(): `frozen != null -> StatusIndicatorState.Frozen.name.uppercase()` immediately above the Live branch; grep of all six keys across *.kt. | Six localisation keys is the right number; mirror_status_frozen would be a dead key. |
| AF-13 | info | diff | scope | scope | none | none | Scope discipline is clean. 23 source files changed, all inside this spec's declared surface (presentation-feature-mirror, presentation-core-ui kit, compose-application window wiring, localisation strings). Grepping every added line for measure/payload/mcp/history/errorcard/export/serial returns only prose matches (a comment about the Payload tab, 'measured'/'measurement' in KDoc, `serial = PHONE` in a pre-existing test fixture helper). Nothing from F-3, F-5, F-6, F-7 or F-8 leaked in. No new Gradle module, no build.gradle.kts edit, no libs.versions.toml change, no persistence or serialization. | git diff development...sdd/drag-annotate-frozen-frame --stat -- ':!.pipeline' -> 23 files, 2107 insertions, 22 deletions; scope grep over added lines. | No out-of-spec feature work; declared out-of-scope features stayed out. |
| AF-14 | info | task | flags | T-13 | human-gate | human-gate | Seven of thirteen reports carry needsHumanReview and none carries failed or blocked. Five of the seven (T-5, T-6, T-7, T-8, T-13) are one root cause — this host cannot load Skiko in-process and denies screen capture, so nothing visual is asserted anywhere; that is a recorded environment limit. The other two are genuine PR notes a reviewer must read: T-9 (the side panel is now permanently composed, so Cmd+1/2/3 goes live for the first time and every pre-existing screenshot of the panel-less window is stale) and T-11 (the SC-4 screenshot is still owed — see AF-2). T-1 additionally records a design choice worth confirming: isMeaningfulDrag requires MIN_DRAG_DEVICE_PX in BOTH axes (AND, not OR), which the spec named the constant for but did not pin the conjunction of; 8 px in both axes is asserted by subMinimumDragIsNotMeaningful. | execution/T-*/report.json needsHumanReview blocks; FrameGeometry.kt:122-125; FrameGeometryTest.subMinimumDragIsNotMeaningful. | 7 needsHumanReview flags, 0 failed, 0 blocked; five share one environment root cause. |
| AF-15 | info | task | plan | traceabilityMatrix | orchestrator | orchestrator | The brief describes a 33-row traceability matrix; plan.json's data.traceabilityMatrix actually holds 31 rows (12 AC + 10 FR + 2 NFR + 7 SC... precisely: AC-5..AC-9, AC-46..AC-52 = 12; FR-6..FR-9, FR-26..FR-31 = 10; NFR-1, NFR-8, NFR-9 = 3; SC-3, SC-4, SC-8..SC-11 = 6). Every row was audited. No row is missing an owner; the count in the brief is simply off by two. | python3 len(plan.json data.traceabilityMatrix) == 31. | Matrix is 31 rows, not 33; all 31 audited. |

### gaps

#### unsatisfiedAC


#### unsatisfiedSC


#### incompleteTasks


#### note

No acceptance criterion is unimplemented and no backlog task is incomplete. AC-5, AC-7, AC-9 and AC-51 are code-complete but cannot be CLOSED without a human render/demo pass (no Compose UI-test harness, no Skiko in-process, no screen capture on this host). They are listed under awaitingHumanConfirmation rather than as gaps so the next agent does not raise a change request for work that is explicitly out of scope.

### awaitingHumanConfirmation

| ref | why |
| --- | --- |
| AC-5 | Dashed border and live Ruler tick guides are asserted by nothing; preview + manual demo only. |
| AC-7 | 42% scrim, popover autofocus and the delete affordance are asserted by nothing; preview + manual demo only. |
| AC-9 | verificationMethod: demo. Screenshot still owed, and see AF-2 — press Escape before capturing. |
| AC-51 | verificationMethod: demo. The reducer half is tested; 'live pixels resume with no reconnection stall' is perceptual. |
| NFR-9 | Light/dark parity across the four new surfaces is a rendered check no agent could run. |
| NFR-1 | The fps-unchanged-while-frozen reading is a demo check; the code-level claim (capture path untouched) is verified — see AF-11. |

### unresolvedFlags

| task | flag | reason |
| --- | --- | --- |
| T-1 | needsHumanReview | isMeaningfulDrag requires MIN_DRAG_DEVICE_PX in BOTH axes (AND, not OR); the spec named the constant but not the conjunction. Chosen and documented; confirm. |
| T-5 | needsHumanReview | Needs one rendered pass — this host cannot load Skiko in-process and the repo has no Compose UI-test harness, so nothing visual is asserted. |
| T-6 | needsHumanReview | Same rendered-pass limitation (live drag layer: dashed border, ruler guides, size label). |
| T-7 | needsHumanReview | Same rendered-pass limitation (frozen stage: scrim, pinned marks, popover, Resume Live, FROZEN dot). |
| T-8 | needsHumanReview | Same rendered-pass limitation (autofocus, Enter-to-save, Escape-to-collapse by keyboard only). |
| T-9 | needsHumanReview | PR note: the side panel is now permanently composed and Cmd+1/2/3 goes live for the first time, so the stage is narrower from here on and pre-existing screenshots of the panel-less window are stale. |
| T-11 | needsHumanReview | SC-4 screenshot still owed; a freshly staged annotation is auto-selected, so press Escape before capturing all three in the gutter (see AF-2). |
| T-13 | needsHumanReview | Light/dark parity (NFR-9) and the NFR-1 fps-while-frozen check are screenshot/demo work this host cannot perform. |

### regressionRun

#### command

per module (3GB daemon cap), forced: ./gradlew --console=plain --rerun-tasks :<module>:desktopTest for common-core, data-device-api, data-device-impl, data-preference-impl, data-repository-impl, presentation-core-styling, presentation-core-ui, presentation-feature-mirror, with ./gradlew --stop between each

#### passed

true

#### forcedRerun

true

#### totalTests

191

#### failures

0

#### skipped

0

#### output

```
===== :common-core:desktopTest =====
BUILD SUCCESSFUL in 1s
7 actionable tasks: 7 executed
EXIT=0 for common-core        (no test source set; no results dir)
===== :data-device-api:desktopTest =====
BUILD SUCCESSFUL in 7s
8 actionable tasks: 8 executed
EXIT=0 for data-device-api    tests=39 failures=0 errors=0
===== :data-device-impl:desktopTest =====
BUILD SUCCESSFUL in 8s
11 actionable tasks: 11 executed
EXIT=0 for data-device-impl   tests=16 failures=0 errors=0
===== :data-preference-impl:desktopTest =====
BUILD SUCCESSFUL in 6s
11 actionable tasks: 11 executed
EXIT=0 for data-preference-impl  tests=5 failures=0 errors=0
===== :data-repository-impl:desktopTest =====
BUILD SUCCESSFUL in 7s
26 actionable tasks: 26 executed
EXIT=0 for data-repository-impl  tests=35 failures=0 errors=0
===== :presentation-core-styling:desktopTest =====
BUILD SUCCESSFUL in 7s
17 actionable tasks: 17 executed
EXIT=0 for presentation-core-styling  tests=5 failures=0 errors=0
===== :presentation-core-ui:desktopTest =====
BUILD SUCCESSFUL in 12s
26 actionable tasks: 26 executed
EXIT=0 for presentation-core-ui  tests=29 failures=0 errors=0
===== :presentation-feature-mirror:desktopTest =====
> Task :presentation-feature-mirror:desktopTestProcessResources
> Task :presentation-feature-mirror:desktopTestClasses
> Task :presentation-feature-mirror:desktopTest
BUILD SUCCESSFUL in 13s
53 actionable tasks: 53 executed
EXIT=0 for presentation-feature-mirror  tests=62 failures=0 errors=0
ALLDONE

Every one of the eight runs reports 'N actionable tasks: N executed' with zero UP-TO-DATE, confirming --rerun-tasks actually re-ran the work (the F-2 failure mode did not recur). Per-class XML, :presentation-feature-mirror (all skipped=0 failures=0 errors=0): AnnotationFormatTest 3, FrameGeometryTest 9, MirrorViewModelAnnotationTest 13, MirrorViewModelDiscoveryTest 5, MirrorViewModelSessionTest 9, PickerDismissabilityTest 2, PickerNavigationTest 8, PopoverCrowdingTest 4, PopoverKeysTest 3, StatsFormatterTest 6 = 62. Grand total across the 7 test-bearing modules = 191 tests, 0 failures, 0 errors, 0 skipped. (The orchestrator's earlier figure of 192 is one higher; the XML on disk from this forced run says 191.) Raw log: /private/tmp/claude-501/-Users-andrew-malitchuk-StudioProjects-rectify-kmp/9407bf81-5643-42e4-a71f-1aaa238778b9/scratchpad/regression.log (772 lines).
```

#### newFailures


### aggregateDiffFindings

| id | category | location | issue |
| --- | --- | --- | --- |
| AD-1 | inconsistent-pattern | MirrorAnnotationStage.kt:273-286, :403-412, :478 | Popover visibility asymmetry between the crowded and uncrowded cases (AF-1), and gutter popovers are unshielded against the drag layer while the selected popover is shielded (AF-6). Both sit on the T-7/T-11 seam, which is why neither task caught it. |
| AD-2 | performance | MirrorAnnotationStage.kt:201-220 | Drag state is read in the composable body, so each pointer move recomposes the stage subtree including the frame Image rather than invalidating draw only (AF-7). Does not affect NFR-1's capture/transport budget. |
| AD-3 | security | whole diff | No security surface touched. No file I/O, no serialization, no network, no new dependency, no reflection, no logging of frame bytes. The frozen bitmap is a second in-memory reference released on Resume Live, platform switch and session stop (MirrorViewModel.kt resumeLive/selectPlatform/stopSession all null currentFrozenFrame), so a pinned frame cannot outlive the session that produced it. Annotation text never leaves memory (export is F-7). |
| AD-4 | dead-code | MirrorAnnotationStage.kt:532-536 | popoverPointerShield's awaitEachGesture body does no work that hit-testing does not already do (AF-5). Also: a duplicated ShowDivider() and two out-of-order imports in StyleguideScreen.kt (AF-9). Nothing unreachable; no orphaned declarations — every new internal (FrameGeometry, MIN_DRAG_DEVICE_PX, metaLine, sizeLabel, anyOverlap, closesPopover, NOMINAL_POPOVER_*) has both a production call site and a test. |
| AD-5 | inconsistent-pattern | compose-application/.../RectifyWindow.kt:259,264,273 | The toolbar's 'Clear' / 'staged' / 'Resolve & Send' strings remain hardcoded English in a file this spec edited. Pre-existing and consistent with that file (zero stringResource calls in RectifyWindow.kt, by its own convention of driving the shell from literals), so T-12 correctly left them alone — recorded only so a later i18n pass over compose-application knows where to start. |
| AD-6 | inconsistent-pattern | compose-application/.../RectifyWindow.kt LaunchedEffect(hasStaged) | Notes auto-selection keys on `stagedCount >= 1`, so after Clear Staged drops the count to 0 the next annotation re-selects the Notes tab. Matches the recorded intent ('the first annotation of a frozen frame only') on any reasonable reading, and the user explicitly asked for Clear in that case. Noted, not faulted. |

### registryConsistency

#### consistent

true

#### issues

- .pipeline/sdd/docs/registry/ does not exist in this project — declared a known non-defect for this run, so no phantom/missing component reconciliation was possible or required.
- Adjacent (not registry): .pipeline/sdd/docs/index.json still lists drag-annotate-frozen-frame at currentStage 'preparation' while state.json says 'validation' with execution done — see AF-4.

#### phantomEntries


#### missingEntries


### tasksChecked

| id | status | done | evidence |
| --- | --- | --- | --- |
| T-1 | done | true | presentation-feature-mirror/.../core/geometry/FrameGeometry.kt (133 lines, internal, pure, no @Composable/ViewModel import) + FrameGeometryTest.kt with all nine planned test names present and green, including pointerToDeviceToDpRoundTripLandsUnderTheCursor and nonIntegerScaleRoundsEachCornerIndependently. MIN_DRAG_DEVICE_PX = 8 asserted. |
| T-2 | done | true | MirrorContract.kt DeviceRect (normalised, width/height derived) + core/annotation/AnnotationFormat.kt metaLine/sizeLabel with the two documented shapes; AnnotationFormatTest 3/3 green including the zero-size case. |
| T-3 | done | true | MirrorContract.kt gained StagedAnnotation, FrozenFrame (+ selected), MirrorStage.Frozen ordered ahead of Streaming in the stage derivation, and exactly the six intents; MirrorUiState.stagedCount added. |
| T-4 | done | true | MirrorViewModel.kt frozenFrame StateFlow + six handlers; MirrorViewModelAnnotationTest 13/13 green. The Skiko probe failed as reported, and the branch taken (hand-written ImageBitmap fake) is sound — see AF-10. The draft's unreachable FrozenFrame.deviceSize fallback was correctly not used; deviceSize is read from the pinned bitmap. |
| T-5 | done | true | New kit molecule AnnotationOverlay.kt (scrim via clipPath(ClipOp.Difference) + one drawRect, no saveLayer, bitmap never read) with light/dark previews; both defaulted parameters shipped (MirrorFrameView.overlay, RectifyWindowShell.tabCounts); StyleguideScreen section added. AnnotationState.Frozen correctly NOT used (binding decision #7). |
| T-6 | done | true | MirrorLiveStage + the shared MirrorAnnotationStage drag layer; drag offsets in remember, exactly one reducer pass per gesture at onDragEnd and only when isMeaningfulDrag; rememberUpdatedState keeps the Unit-keyed gesture block current without restarting mid-drag. |
| T-7 | done | true | MirrorFrozenStage shares the live body (which is what makes AC-52 fall out), scrim via dimmed = frozen != null, Pinned/Selected marks, CommentPopover, Resume Live AppButton, and MirrorContent.indicatorState()/indicatorLabel() now return StatusIndicatorState.Frozen ahead of Streaming. |
| T-8 | done | true | core/annotation/PopoverKeys.kt closesPopover + PopoverKeysTest 3/3 (including the meta-held disjointness with the window's Cmd+1/2/3); FocusRequester threaded into CommentPopover's BasicTextField (the risk-register contingency was needed and taken); onPreviewKeyEvent scoped to the popover subtree. Hit-test fix verified — AF-5. |
| T-9 | done | true | RectifyWindow.kt: stagedCounter -> CounterBadge(count, Empty/Staged), clearAction -> ClearStaged with enabled = stagedCount > 0, sidePanel -> MirrorNotesTab on ShellTab.Notes, tabCounts = mapOf(Notes to stagedCount), LaunchedEffect Notes auto-select, isPanelVisible = true. MirrorNotesTab.kt renders AnnotationRow per annotation in index order with the empty state. |
| T-10 | done | true | core/annotation/PopoverCrowding.kt anyOverlap (O(n^2), half-open via Rect.overlaps) + NOMINAL_POPOVER_WIDTH_DP/HEIGHT_DP; PopoverCrowdingTest 4/4 green including touching-edges-do-not-count. |
| T-11 | done | true | AnnotationGutter in MirrorAnnotationStage.kt: right-edge column inside the frame box, straight Canvas leader lines in the overlay's own space, GutterRowHeight pitched at the two-line 54 dp, last row clamped. Elbow-stagger correctly deferred. See AF-2 and AF-6 for the two caveats. |
| T-12 | done | true | Six keys added to strings.xml, all six resolve to a real stringResource call site (grep-verified). The seventh (mirror_status_frozen) was correctly refused — AF-12. |
| T-13 | done | true | Code-side complete; the light/dark parity pass and the fps-while-frozen reading are the demo work this host cannot do (flagged). Independently re-ran the full desktop suite here: 191/191 green with --rerun-tasks forced. |

### acceptanceCriteriaChecked

| id | satisfied | partial | verificationMethod | evidence |
| --- | --- | --- | --- | --- |
| AC-5 | true | true | test | Implementation: MirrorLiveStage drag layer + AnnotationState.Drawing mark with sizeLabel + two Rulers fed device px. Tests: FrameGeometryTest 9/9 (where the box lands, both legs), AnnotationFormatTest.sizeLabelMatchesTheMarkShape (what the label says). NOT asserted: dashed border, ruler ticks, real-time rendering — preview + human demo only (AF-3). |
| AC-6 | true |  | test | MirrorViewModelAnnotationTest.firstStageAnnotationFreezesTheFrame green: stage == Frozen, one annotation at the drawn rect, frozenFrame non-null, deviceSize from the pinned bitmap. |
| AC-7 | true | true | test | Implementation: AnnotationOverlay scrim (dimmed when frozen) + CommentPopover with badge, placeholder, delete control and metaLine; autofocus via LaunchedEffect(selected.index) + FocusRequester on the field. Tests: AnnotationFormatTest.metaLineMatchesTheDocumentedShape, firstStageAnnotationFreezesTheFrame (selectedIndex set, so the popover is composed). NOT asserted: dim, autofocus, delete affordance (AF-3). See also AF-1 on multi-annotation popover visibility. |
| AC-8 | true |  | test | commentCommitStoresTheText, deleteDecrementsStagedCountAndRenumbers, deleteOfSelectedAnnotationClearsOrClampsSelection all green; CommentPopover.onClose wired to DeleteAnnotation; Enter handled by closesPopover (text is already committed per keystroke, so Enter has nothing left to save — documented, and PopoverKeysTest covers the mapping). |
| AC-9 | true | true | demo | popoverCrowding + AnnotationGutter + leader lines implemented; PopoverCrowdingTest 4/4 green. Demo-verified by design — screenshot owed, and see AF-2 (at most n-1 popovers reach the gutter). |
| AC-46 | true |  | test | FrameGeometryTest.densityTwoDoesNotScaleTheStoredRect (asserts the density=2 result EQUALS the density=1 result — the storage leg has no density term), toDpDividesByDensityExactlyOnce, pointerToDeviceToDpRoundTripLandsUnderTheCursor (POC-trap reproduction), nonIntegerScaleRoundsEachCornerIndependently. All green. Density appears in FrameGeometry only inside toDp. |
| AC-47 | true |  | test | freezeUsesTheFrameHeldAtRelease green, asserting assertSame on the instance plus both dimensions (109x218) against a decoder whose output differs in both axes; MirrorViewModel reads currentFrame.value exactly once, at release. Fake ruled non-circular — AF-10. |
| AC-48 | true |  | test | clearStagedEmptiesTheListAndKeepsTheFrameFrozen green (count 0, frame still pinned via assertSame, stage still Frozen); RectifyWindow.kt inspection confirms the Empty-at-0-else-Staged mapping and enabled = stagedCount > 0. compose-application has no test source set, so the call site is inspection. |
| AC-49 | true |  | test | MirrorNotesTab renders one AnnotationRow per annotation in list order (which deleteDecrementsStagedCountAndRenumbers proves is contiguous badge order) and dispatches SelectAnnotation(index) on click; selectAnnotationNullKeepsTheAnnotationAndTheCount green; tabCounts forwarding present so the panel count is reachable. Row-click-highlights-mark is inspection + demo. |
| AC-50 | true |  | test | PopoverKeysTest 3/3: Enter/NumPadEnter/Escape close, bare digits do not, every meta-held key is left to the window. selectAnnotationNullKeepsTheAnnotationAndTheCount green (annotation and count survive). handleShellShortcut still matches only isMetaPressed and Picker/Frozen remain mutually exclusive enum branches. |
| AC-51 | true | true | demo | resumeLiveClearsTheFrozenFrameAndAnnotations and dragAfterResumeLiveFreezesANewFrame green; the no-reconnection claim is structurally proven (AF-11: session still Streaming, 1 active collector, capture path untouched). The perceptual half is demo-only. |
| AC-52 | true |  | test | secondDragAddsAnnotationWithoutRefreezing green: assertSame on the pinned bitmap after a newer frame arrived, indices [1,2], deviceSize unchanged. Code: `if (currentFrozenFrame.value == null)` guard, and the frozen stage shares the live stage's drag layer. |

### scenariosChecked

| id | satisfied | note |
| --- | --- | --- |
| SC-3 | true | State transitions covered by firstStageAnnotationFreezesTheFrame + commentCommitStoresTheText; the end-to-end gesture, autofocus and Enter-to-save are demo. This spec's annotation SC-3, deliberately not F-1's shell SC-3. |
| SC-4 | false | Predicate covered by PopoverCrowdingTest.oneOverlappingPairCrowdsEverything, but the scenario as written is not reachable: auto-selection keeps one popover off the gutter (AF-2). Either amend the scenario steps or change the auto-selection, then shoot the screenshot. This spec's annotation SC-4, deliberately not F-1's shell SC-4. |
| SC-8 | true | All four named FrameGeometryTest cases present and green; the POC's 2x trap is reproduced and caught. |
| SC-9 | true | secondDragAddsAnnotationWithoutRefreezing green — second annotation against the same pinned bitmap, no stage transition. |
| SC-10 | true | Escape path (SelectAnnotation(null)) and delete-with-renumber both covered by green reducer tests plus PopoverKeysTest; the reselect-from-the-Notes-row click is inspection + demo. |
| SC-11 | true | resumeLiveClearsTheFrozenFrameAndAnnotations + dragAfterResumeLiveFreezesANewFrame green; no-stall is structural (AF-11), perceptual confirmation is demo. |

### flags

| kind | target | detail |
| --- | --- | --- |
| needsHumanReview | T-1 | isMeaningfulDrag uses AND across both axes; confirm the conjunction. |
| needsHumanReview | T-5 | Rendered pass owed (no Skiko in-process, no UI-test harness). |
| needsHumanReview | T-6 | Rendered pass owed. |
| needsHumanReview | T-7 | Rendered pass owed. |
| needsHumanReview | T-8 | Rendered pass owed (keyboard-only walk of SC-10). |
| needsHumanReview | T-9 | Cmd+1/2/3 goes live and the panel is permanently composed; prior screenshots are stale. |
| needsHumanReview | T-11 | SC-4 screenshot owed; press Escape before capturing (AF-2). |
| needsHumanReview | T-13 | NFR-9 light/dark parity and the NFR-1 fps reading are demo work. |

### traceability

| ref | verificationMethod | satisfied | evidence |
| --- | --- | --- | --- |
| AC-5 | test | true | Partial: automatable half tested (FrameGeometryTest, AnnotationFormatTest.sizeLabel); rendered half preview+demo only — AF-3. |
| AC-6 | test | true | firstStageAnnotationFreezesTheFrame green. |
| AC-7 | test | true | Partial: metaLine + reducer state tested; dim/autofocus/delete affordance demo only — AF-3. Multi-annotation popover visibility raised as AF-1. |
| AC-8 | test | true | commentCommitStoresTheText, deleteDecrementsStagedCountAndRenumbers, deleteOfSelectedAnnotationClearsOrClampsSelection green. |
| AC-9 | demo | false | Requires human confirmation; screenshot owed and the scenario needs an extra Escape (AF-2). Predicate itself is green. |
| AC-46 | test | true | FrameGeometryTest 9/9, both legs, POC trap reproduced. |
| AC-47 | test | true | freezeUsesTheFrameHeldAtRelease green; fake ruled non-circular (AF-10). |
| AC-48 | test | true | clearStagedEmptiesTheListAndKeepsTheFrameFrozen green + RectifyWindow.kt inspection. |
| AC-49 | test | true | MirrorNotesTab inspection + renumbering/selection reducer tests green. |
| AC-50 | test | true | PopoverKeysTest 3/3 + selectAnnotationNullKeepsTheAnnotationAndTheCount green. |
| AC-51 | demo | false | Requires human confirmation of the perceptual half; reducer half green and the no-reconnection claim is structurally proven (AF-11). |
| AC-52 | test | true | secondDragAddsAnnotationWithoutRefreezing green. |
| FR-6 | test | true | Drag layer + Drawing mark + sizeLabel + two Rulers implemented; same partial-render caveat as AC-5. |
| FR-7 | test | true | stageAnnotation pins currentFrame.value once at release; AC-6 and AC-47 tests green. |
| FR-8 | test | false | Badge, autofocused field with the exact placeholder, delete control and meta line are all present on the SELECTED annotation, and the scrim dims outside every mark. 'Per-annotation popover' is only literally true in the crowded case — AF-1 is the open question for the human gate. |
| FR-9 | demo | false | Gutter + leader lines implemented at the agreed demo fidelity; AC-9 demo still owed, AF-2 caveat. |
| FR-26 | test | true | FrameGeometry is internal, pure, no @Composable and no ViewModel/Orbit import; density occurs only in toDp (lines 106-109); the only other density use in new code is AnnotationOverlay.kt:95-98, an ordinary Dp->px Path conversion inside remember, not a second device-space conversion. No density arithmetic on frame coordinates anywhere else. |
| FR-27 | test | true | RectifyWindow.kt stagedCounter/clearAction wired; clearStaged reducer test green and leaves the frame pinned. |
| FR-28 | inspection | true | MirrorNotesTab.kt one AnnotationRow per annotation with index, comment-or-empty-copy and metaLine, two previews; RectifyWindowShell.tabCounts forwarding added so the count is reachable. |
| FR-29 | test | true | closesPopover + onPreviewKeyEvent on the popover subtree; PopoverKeysTest proves disjointness with the meta-only window shortcuts. No new dispatch layer. |
| FR-30 | inspection | true | AF-11: five diff hunks in MirrorViewModel.kt, none touching sessionDeviceId's gate or observeFrames; Resume Live only nulls the pinned bitmap and the reducer slice; StatusIndicatorState.Frozen now has its first production caller. |
| FR-31 | test | true | secondDragAddsAnnotationWithoutRefreezing green; frozen stage shares the live drag layer. |
| NFR-1 | inspection | true | Capture/decode/transport path bit-identical (AF-11); scrim is one clipPath + one drawRect with no saveLayer and the ImageBitmap never re-read; zero reducer passes during a drag (one at release, guarded by isMeaningfulDrag). UI-side recomposition cost noted in AF-7. The fps-unchanged demo reading is owed. |
| NFR-8 | inspection | true | FocusRequester bound to the field itself (not the group), autofocus re-fires on selection change, Enter/Escape both reachable, close control's contentDescription says 'Delete annotation' rather than 'Close'. The keyboard-only walk of SC-10 is demo. |
| NFR-9 | inspection | false | Light/dark @Previews exist on AnnotationOverlay (x2), MirrorNotesTab (x2) and the three stage previews, plus the StyleguideScreen section; no literal Color(0x, raw .dp at a call site or inline TextStyle was introduced (the four CommentPopover literals are pre-existing and documented — AF-8). The actual rendered parity pass is the human demo that is owed. |
| SC-3 | demo | false | State transitions green; end-to-end gesture demo owed. |
| SC-4 | demo | false | Not reachable as written — AF-2. |
| SC-8 | test | true | Four named FrameGeometryTest cases green. |
| SC-9 | test | true | secondDragAddsAnnotationWithoutRefreezing green. |
| SC-10 | test | true | Escape and delete paths green; the row-reselect click is inspection + demo. |
| SC-11 | test | true | Both named tests green; perceptual half demo. |
