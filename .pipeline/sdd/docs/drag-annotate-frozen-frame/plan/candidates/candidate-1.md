# drag-annotate-frozen-frame — candidates/candidate-1

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | candidates/candidate-1 |
| status | done |
| producedBy | sdd-build-plan-draft |
| schemaVersion | 1 |
| createdAt | 2026-10-15T03:34:07Z |
| updatedAt | 2026-10-15T03:34:07Z |

## Gate

### type

auto

### approvedBy

sdd-build-plan-draft

### approved

true

### at

2026-10-15T03:34:07Z

## Provenance

### selfHash

22fa611c8fece6ae41ee67908f43ff1936d0abc0986a9dfd6a79156e7112dd22

### inputs

| artifact | hash |
| --- | --- |
| preparation/preparation.json | 2e847f2a4b2049c279333c6ef0c618bd8f2329c2beb0adbed90b9bdb30bcdd36 |

## Data

### meta

#### title

Drag-to-Annotate & Frozen Frame (F-4)

#### version

1

#### status

draft

### goal

Give a tester one drag gesture on the live Android mirror that freezes the exact frame they saw the bug in, boxes it, numbers it and opens a focused place to describe it — with the box stored in device screenshot pixels so it stays correct across host density, window resize and panel resize. New code is deliberately tiny: one pure geometry function, one kit molecule (the dim scrim), two defaulted kit parameters, and six Orbit intents. Everything visible is assembled from shipped, previewed kit components.

### problemStatement

F-1 (shell) and F-2 (device discovery + live mirror) deliver a live mirror with no way to mark anything on it — a tester can only describe a bug in prose or attach a raw screenshot, which is exactly what this product exists to replace. rectify-poc proved the drag-box interaction is feasible but also documented a silent density-conversion bug: pointer events and the composable's own layout are in raw screen pixels, while Modifier.offset/size take Dp which Compose multiplies by density again, so every drawn rectangle lands `density`x from the cursor (2x on this Retina host). That bug corrupts every annotation's anchor and no existing test would catch it. Separately, F-1 shipped three slots explicitly reserved for this feature — ShellToolbar.stagedCounter, ShellToolbar.clearAction and SidePanelFrame's Notes tab — as inert stubs, and StatusIndicatorState.Frozen shipped with zero callers. This spec closes the coordinate hole and fills the reserved slots.

### highLevelDescription

Four layers, no new Gradle module, no new catalog entry, no persistence. (1) GEOMETRY — a new internal, pure, non-Composable FrameGeometry in presentation-feature-mirror/core/geometry/, unit-tested BEFORE any UI exists. Pointer offsets from pointerInput and the frame box size from onSizeChanged are in the SAME pixel space, so `devicePx = pointerPx / frameBoxPx * deviceSize` contains no density at all; density applies in exactly one place, converting back out for rendering. (2) STATE — MirrorUiState gains `frozen: FrozenFrame?` holding a List<StagedAnnotation> and a selectedIndex; the frozen ImageBitmap itself goes on a second StateFlow beside `frame`, with the reducer storing only its IntSize. Annotations belong IN reduced state for the exact inverse of F-2's reason for keeping frames out: they are a handful of small value-equal records mutated by discrete user actions and read simultaneously by three consumers (toolbar badge, Notes tab, stage marks), whereas frames are identity-compared blobs arriving ten times a second. (3) FREEZE — a render-path switch only. sessionDeviceId and F-2's flatMapLatest capture gate are never touched, so Resume Live costs no new adb session and no first-frame latency; drag-in-progress offsets live in composable `remember` and never enter Orbit. (4) UI — assemble from AnnotationMark, Ruler, CommentPopover, AnnotationPopover, AnnotationRow, CounterBadge, StatusIndicatorState.Frozen and AppButton; author exactly one new kit molecule (AnnotationOverlay = scrim via clipPath(ClipOp.Difference) + one AnnotationMark per rect) and add two source-compatible defaulted parameters to shipped kit components. FR-9's gutter is an in-stage right-edge column, not the Notes tab, because FR-28 already fills that tab.

### background

Branch sdd/drag-annotate-frozen-frame off development, with F-1 and F-2 merged and 160 desktop tests green. Preparation is signed off (readyForPlan true) with 10 FR, 3 NFR, 12 AC, 6 scenarios, 4 assumptions, 9 binding decisions, 8 open risks and 11 open questions, all carried forward here unchanged. All nine binding decisions are settled and this plan plans TO them rather than reopening them. No design comp exists for any part of this feature — all five cached comps are device-discovery screens — so every invented visual value ships as a named private constant for a later one-line design pass, the precedent F-1 set with the stage dot grid.

### sources

| type | ref | cached |
| --- | --- | --- |
| code | .pipeline/sdd/docs/drag-annotate-frozen-frame/preparation/preparation.json | preparation/preparation.json |
| code | .pipeline/sdd/docs/drag-annotate-frozen-frame/preparation/brainstorm.json | preparation/brainstorm.json |
| code | .pipeline/sdd/docs/drag-annotate-frozen-frame/preparation/clarify.json | preparation/clarify.json |
| code | .pipeline/sdd/docs/drag-annotate-frozen-frame/preparation/input.json | preparation/input.json |
| code | /Users/andrew-malitchuk/StudioProjects/rectify-poc/app/src/main/kotlin/rectify/Main.kt#L122-L237 | reference only, not imported — documents the density trap (L122-L126) and the per-corner /density/scale conversion (L232-L237) that FrameGeometry.toDevice replaces with one tested function |
| code | .pipeline/sdd/docs/android-device-discovery-mirroring/plan/plan.json | F-2's plan — shape reference, dependency contract for MirrorViewModel.frame and the sessionDeviceId gate |

### scope

#### in

- Drag-to-draw dashed outline + live device-pixel size label + ruler/tick guides on the live mirror (FR-6)
- Freeze-on-release of the exact release-time frame, creating a staged annotation anchored to the box (FR-7)
- Frozen-frame dim-outside-boxes treatment + per-annotation comment popover (badge, autofocus, delete, meta line) (FR-8)
- Crowded-popover relocation to an in-stage right-edge gutter with straight leader lines, at demo fidelity (FR-9)
- Canonical device-screenshot-pixel coordinate conversion as one pure, internal, unit-tested function (FR-26)
- Wiring ShellToolbar's stagedCounter and clearAction stubs (FR-27)
- Filling the side panel's Notes tab with AnnotationRows + the Notes tab count (FR-28)
- Keyboard scoping for popover Enter/Escape via onPreviewKeyEvent on the popover focus subtree, no new dispatch layer (FR-29)
- Resume Live control; freeze is render-only and the capture loop is never paused (FR-30)
- Repeated annotation on an already-frozen frame without re-freezing (FR-31)
- Two source-compatible defaulted parameters on shipped kit components: MirrorFrameView(overlay) and RectifyWindowShell(tabCounts)
- One new kit molecule: AnnotationOverlay (scrim + marks)
- New localisation keys in presentation-core-localisation for the copy this feature's own composables render

#### out

- Measurement / measure-mode (F-5) — MeasureCard/MeasureMark exist in the kit but are not wired here
- Payload preview (F-6) and MCP send (F-7) — the Payload tab and any network/export behaviour
- Capture/annotation history (F-8) — multi-frame sessions; exactly one frozen frame lives in memory at a time (AS-11)
- Error cards (F-3)
- Device rotation / resolution change mid-annotation-session (AS-12)
- The gutter/leader-line 8 px elbow-stagger visual polish named in AnnotationPopover's KDoc (deferred)
- Pausing the capture/transport loop while frozen for power/perf (FR-30 keeps it running; a later optimisation pass may revisit)
- Introducing a Compose UI-test harness/compose-ui-test dependency — raised as its own follow-up ticket, not done here
- Annotation move/resize after creation — AnnotationMark's showHandles exists but no resize gesture is wired
- Lifting StagedAnnotation into domain-core — it has one consumer and no serialization need until F-7
- Any new Gradle module, convention-plugin change, Koin module or version-catalog entry

### assumptions

- AS-10: Manual/dev verification happens at host density 2.0 (Retina, per F-1's T-12 probe), but FrameGeometry takes density as a parameter rather than hardcoding 2.0.
- AS-11: Exactly one frozen frame and its staged-annotation set exist in memory at a time within this spec's scope; multi-frame capture history is F-8's.
- AS-12: The mirrored device's screen does not rotate or change resolution mid-annotation-session; handling that is out of scope.
- AS-13: Clicking the toolbar's clearAction empties the staged list without a confirmation dialog — a direct, reversible-by-redrawing action, not a destructive-confirm flow, consistent with the toolbar's existing design.
- AS-14 (plan): presentation-feature-mirror's build.gradle.kts needs NO edit. It already declares projects.presentationCoreUi, projects.presentationCoreLocalisation, projects.presentationCoreStyling, compose.material3 and libs.kotlinx.coroutines.test, and FeatureConventionPlugin already wires kotlin-test into commonTest. Verified by reading both files — this is the class of omission that bit F-2's draft plan.
- AS-15 (plan): No Koin or initKoin change is needed. MirrorViewModel's constructor gains no new use case, so PresentationFeatureMirrorModule.kt and InitKoin.kt's load-order-significant list are both untouched. Verified by reading MirrorViewModel's constructor.
- AS-16 (plan): MirrorHost's and MirrorStageContent's PUBLIC signatures do not change. MirrorHost internally computes displayFrame = frozenFrame ?: frame and passes the single bitmap it already passes, so RectifyShellContent's parameter list and every existing call site stay source-compatible.

### constraints

| type | text |
| --- | --- |
| tech | No literal Color(0x...), no raw .dp at a call site, no hardcoded TextStyle. Every invented value (scrim alpha, gutter width, Resume Live inset, minimum drag size, nominal popover bounds) is a named private file-top constant with a KDoc rationale — the pattern MirrorContent.kt's NoticeMaxWidth and the kit's own geometry blocks already use — or a new Theme token. Per .claude/rules/design-tokens.md. |
| tech | A commonTest backtick test name containing a comma, apostrophe or colon compiles on the JVM and fails compileTestKotlinIosArm64 with 'Name contains illegal characters'. This already broke the repo once in F-1. Plain camelCase test names only, no backticks at all in new tests. |
| tech | presentation-core-ui takes no dependency on presentation-core-localisation. AnnotationOverlay and every other kit-level change takes copy as a parameter with an English default; the feature module supplies stringResource(). |
| tech | presentation-core-ui enforces strict atom -> molecule -> organism layering. AnnotationOverlay lives in molecule/annotation/ and imports AnnotationMark (atom) only — never CommentPopover (organism). Popover placement and leader-line routing stay with the feature-module host. |
| tech | Orbit MVI four-file convention for presentation-feature-mirror, with the module's two documented deviations intact (MirrorHost not MirrorScreen; no pixels in reduced state). The no-pixels rule extends to the frozen bitmap: it goes on a StateFlow beside frame, never into MirrorUiState. |
| tech | core/ holds internal declarations only; source/ holds the public API. FrameGeometry, AnnotationFormat and popoverCrowding are internal and live under core/; StagedAnnotation / DeviceRect / FrozenFrame are public contract types and live in source/mirror/MirrorContract.kt. explicitApi() is enforced repo-wide: every public declaration needs an explicit visibility modifier and return type. |
| tech | FrameGeometry is internal, pure, has no @Composable and no ViewModel side effects — the single code path for every pointer-px to device-px conversion. No other module may grow its own variant (FR-26). |
| tech | Do NOT use AnnotationState.Frozen. It paints an opaque Theme.color.surface fill (AnnotationMark.kt line 98) that would cover the exact device pixels the annotation points at, contradicting its own KDoc. Use Pinned/Selected plus the new scrim. AnnotationState.Frozen remains shipped with no caller in this spec — record that so a later reviewer does not read it as an oversight. |
| tech | Zero new version-catalog entries, zero new Gradle modules, zero convention-plugin changes. Build per module — the Kotlin daemon is capped at 3 GB, so :presentation-core-ui and :presentation-feature-mirror are built separately, not via a root `build`. |
| tech | withTimeoutOrNull, never withTimeout, anywhere a timeout is needed. |
| business | AC-9 / FR-9 ship at minimum-demo fidelity only — straight Canvas leader lines, nominal popover bounds, no elbow-stagger polish. Explicitly deferred, not silently dropped. |
| business | AC-5 and AC-7 carry verificationMethod: test from the PRD, but this repo has ZERO Compose UI tests among its 160 green. They are satisfied here by pure geometry/format tests plus reducer tests plus a light/dark @Preview and a manual demo. No rendered coverage is claimed that will not exist; 'adopt compose-ui-test' is raised as its own follow-up ticket. |

### dependencies

| on | note |
| --- | --- |
| F-1 (app-shell-window-layout) | Reuses ShellToolbar's stagedCounter/clearAction slots (both currently inert stubs in RectifyWindow.kt lines 232-242), SidePanelFrame's per-tab content slot and tabCounts map, StageSurface's content slot, and ShellTab/shortcutToTab's Cmd+1/2/3 mapping. VERIFIED GAP: RectifyWindowShell composes SidePanelFrame WITHOUT forwarding tabCounts (RectifyWindowShell.kt lines 87-92), so FR-28's Notes count needs a +1 defaulted parameter on RectifyWindowShell. The brainstorm's claim that 'no shell change is needed at all' is wrong on this point. |
| F-2 (android-device-discovery-mirroring) | Reuses MirrorViewModel.frame (StateFlow<ImageBitmap?>), MirrorUiState / MirrorStage / MirrorIntent, MirrorHost's content lambda, MirrorStageContent's stage when(), MirrorStatusLine's private indicatorState(), and the sessionDeviceId-gated flatMapLatest capture loop that must keep running unpaused while frozen. The gate itself is NOT edited. |
| presentation-core-ui kit | AnnotationMark, Ruler, CommentPopover, AnnotationPopover, AnnotationRow, CounterBadge, GlyphBadge, MeasureTag, StatusIndicator(Frozen), AppButton, MirrorFrameView, SidePanelFrame, ShellToolbar. All signatures verified against source. Two get a defaulted parameter; one new molecule is authored; the rest are used unchanged. |
| presentation-core-localisation | New mirror_annotation_* / mirror_action_resume_live / mirror_status_frozen keys in values/strings.xml. Only the English values/ file exists; keys follow the <feature>_<element> convention. |
| rectify-poc Main.kt | Reference only, never imported. Lines 122-126 document the density trap; lines 232-237 are the per-corner /density/scale conversion FrameGeometry.toDevice replaces with one tested function. |

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Density-trap regression: a future call site converts px to dp by hand instead of going through FrameGeometry, and every annotation anchor silently shifts by 2x on Retina. | med | high | One pure conversion path (FR-26) built and tested as step 1 before any UI exists; AC-46's fixture reproduces the POC's exact trap; FrameGeometry kept internal so no other module can grow its own variant; a code-review checklist line that any new .dp arithmetic on frame coordinates is a defect. |
| The annotation overlay is drawn in a box that is not pixel-identical to MirrorFrameView's aspect-fit image, producing a constant small offset that no unit test can see. | med | high | Compose the overlay INSIDE MirrorFrameView's own aspect-fit Box via the new overlay slot, so the overlay literally cannot be a different rectangle. Duplicating the aspectRatio maths in the feature module is explicitly forbidden. Verify once visually at two window sizes and with the side panel at both width bounds. |
| A FocusRequester attached to CommentPopover's root Column may not forward focus to the inner BasicTextField, breaking the autofocus AC-7 requires. VERIFIED: CommentPopover has no focusRequester parameter and its BasicTextField owns a private interactionSource (CommentPopover.kt lines 110-135, 228-235). | med | med | First try parent FocusRequester + Modifier.focusRequester().focusGroup() on the popover's root. If focus does not land on the field, add `focusRequester: FocusRequester? = null` to CommentPopover and pass it to the BasicTextField — a 3-line, source-compatible, defaulted-parameter kit change. Budgeted in step 8, not a surprise. |
| Making sidePanel non-null activates Cmd+1/2/3 for the first time: handleShellShortcut currently returns false (does not consume) while the panel is absent (RectifyWindow.kt lines 326-330). F-1's shortcut behaviour becomes live in this spec. | high | low | Expected and desirable — F-1's own comment says the panel is null only because there was no content, which is exactly what this spec fixes. handleShellShortcut is private and untested, and ShortcutToTabTest.kt tests only the pure mapper, so the 160 tests are not mechanically at risk; the exposure is behavioural. Run the full desktop suite immediately after step 9 and call the change out in the PR. |
| A commonTest backtick name containing a comma, apostrophe or colon compiles on the JVM and breaks compileTestKotlinIosArm64. This broke the repo once in F-1 (fixed at a71d232). | med | high | Plain camelCase test names only, no backticks at all in the five new test files. Run :presentation-feature-mirror:compileTestKotlinIosArm64 and :presentation-core-ui:compileTestKotlinIosArm64 before the PR, not after. |
| AC-47's ViewModel test needs two distinguishable ImageBitmap instances, but MirrorFakes deliberately uses decodeFrame = { null } to keep a graphics backend out of unit tests. Allocating ImageBitmap(w,h) in a headless desktopTest JVM pulls in Skiko's native library. | med | med | Add a `decodeFrame` parameter to MirrorFakes.mirrorViewModel() (defaulting to the existing null decoder) and a fake decoder that maps byte content to ImageBitmap(width, height) of distinguishing sizes; assert on frozenFrame.value?.width, not on pixel content. ImageBitmap(1080, 2400) is already constructed in commonMain by MirrorFrameViewPreview, so it compiles on every target. FALLBACK if Skiko will not load headless: have the freeze handler also reduce the release-time IntSize into FrozenFrame.deviceSize and assert on that instead — the reducer already stores it, so the assertion moves with zero production change. |
| Keeping the capture loop running while frozen means continuous screencap on the device during what may be minutes of typing. | high | low | Accepted by FR-30 and already a logged open question. If it bites, the fix is an isFrozen flag folded into the transport's adaptive interval, not a session teardown — setting sessionDeviceId = null would also restart discovery polling and tear the adb session down. |
| The scrim colour is a deliberate literal (always dark in both themes) rather than a Theme token, which design-tokens.md generally forbids. | low | low | Follows the documented exception pattern already in CommentPopover (PopoverInk, FieldFill) and CounterBadge (Color.White on a signal fill): fixed values with an explicit KDoc rationale because they sit over unknown-brightness device content. One named constant in one file; promote to a ThemeColor token the moment a second call site appears. |
| FR-9's relocation threshold uses nominal popover-bounds constants rather than measured sizes, so a long comment could crowd without triggering relocation. | low | low | AC-9 is demo-verified, not test-verified. Measuring in order to decide whether to compose is a circular layout dependency that would need SubcomposeLayout — explicitly not worth it at this fidelity. Named constant, one-line tune. |
| Adding MirrorStage.Frozen makes the when(state.stage) in MirrorStageContent non-exhaustive. | high | low | This is a feature, not a risk: MirrorStage is an enum and the when has no else branch, so the compiler forces the Frozen branch to be written. Step 3 is compile-only and will fail until step 6 lands the branch — sequence them in the same slice or land a `-> Unit` placeholder in step 3. |
| Deleting the last annotation leaves the frame frozen with zero holes, so the scrim degrades to a full-frame dim over a static picture with nothing marked. | med | low | Accepted as the honest rendering — AC-48 explicitly keeps the stage frozen after Clear, and the Resume Live control stays visible and is the way out. AnnotationOverlay must handle an empty rect list without building a degenerate Path. |

### requirements

#### functional

| id | text |
| --- | --- |
| FR-6 | Support click-and-drag on the live mirror to draw a selection box, rendering a dashed red outline, a live device-pixel size label, and ruler/tick coordinate guides during the drag. |
| FR-7 | On drag release, freeze the exact current frame and create a new staged annotation anchored to the drawn box. |
| FR-8 | On a frozen frame, dim everything outside annotation boxes and show a per-annotation popover with a numbered badge, an autofocused free-text comment field (placeholder 'What's wrong here?'), a delete control, and a size/position meta line; Enter saves, clicking the delete control removes the annotation. |
| FR-9 | When annotation popovers would crowd/overlap, relocate them to a gutter/sidebar panel connected to their boxes via leader lines. |
| FR-26 | Establish device screenshot pixels as the single canonical storage space for annotation geometry. Implement the pointer-px -> density -> composable-dp -> frame-display-scale -> device-px conversion as one pure, platform-agnostic function (no Composable, no ViewModel side effects) rather than inline per call site, so every caller (the live drag box, the size label, the ruler extent) shares one code path and one set of tests. |
| FR-27 | Wire the toolbar's stagedCounter slot to the live staged-annotation count (CounterBadgeState.Empty at 0, Staged otherwise) and the clearAction slot to removing every staged annotation while leaving the stage frozen (not ending the session). |
| FR-28 | Fill the side panel's Notes tab with one AnnotationRow per staged annotation (index, comment or the empty-state copy, position meta line), kept in sync with the same staged-annotation list FR-27 wires to the toolbar; selecting a row selects the corresponding mark on the frame. |
| FR-29 | Scope annotation keyboard handling to the focused popover: Enter commits the comment text and keeps the annotation pinned; Escape collapses the popover back to a pinned, unselected mark without deleting it; the delete control (CommentPopover's existing close affordance, repurposed to mean delete per FR-8) removes the annotation and decrements the staged count. None of these three consume or bubble into the window-level Cmd+1/2/3 tab shortcut or the device picker's own Escape handler — both already key off disjoint event shapes (meta-modified keys vs. a focus subtree that cannot coexist with the frozen-frame stage). |
| FR-30 | On freeze, replace the live MirrorFrameView with a static render of the captured bitmap plus a dim/scrim treatment outside every annotation box, retain the device name in the existing toolbar/status chrome, and expose a 'Resume Live' control that discards the frozen frame and its staged annotations and returns the stage to live streaming. Freezing is a display-level snapshot only: the underlying sessionDeviceId-gated capture/transport loop is never paused or restarted, so resuming needs no new adb session. |
| FR-31 | While a frame is frozen, further drag-and-release gestures on the stage add additional staged annotations against the same frozen frame rather than re-triggering the freeze transition or replacing the captured bitmap. |

#### nonFunctional

| id | category | text |
| --- | --- | --- |
| NFR-1 | performance | Live mirror glass-to-glass latency should stay low enough (target under ~150 ms) that drag-to-select and freeze feel responsive. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |

### acceptanceCriteria

| id | text | priority | verificationMethod | verificationNote |
| --- | --- | --- | --- | --- |
| AC-5 | Given live mirror is streaming, when the user drags on the mirror, then a dashed red outline, live pixel-size label, and ruler guides render in real time. | must | test | PRD-inherited mismatch: no Compose UI-test harness exists in this repo (zero UI tests among 160 green). Satisfied by FrameGeometryTest (where the box lands) + AnnotationFormatTest (what the size label says) + a light/dark @Preview of the drawing state + a manual demo via ./gradlew :desktop-application:run. The dashed-ness of the border and the Ruler's ticks are verified by preview and demo, not by assertion. 'Adopt compose-ui-test' is raised as its own follow-up ticket, not silently substituted. |
| AC-6 | When the user releases the drag, then the exact frame is frozen and a new staged annotation is created anchored to the drawn box. | must | test |  |
| AC-7 | Given a frozen frame with one annotation, then everything outside the box dims and a popover with numbered badge, autofocused comment field, delete control, and meta line appears. | must | test | Same PRD-inherited test/no-harness mismatch as AC-5. Satisfied by MirrorViewModelAnnotationTest (stage == Frozen with one annotation and selectedIndex set, so the popover is composed) + AnnotationFormatTest (the meta line string) + a light/dark @Preview of AnnotationOverlay and the frozen stage + a manual demo for the dim, the autofocus and the delete affordance. |
| AC-8 | Given an annotation popover is focused, when the user presses Enter, then the comment saves; when the user clicks the delete control, then the annotation is removed and the staged count decrements. | must | test |  |
| AC-9 | Given three annotations are drawn close together, when their popovers would overlap, then they relocate to a sidebar/gutter panel connected via leader lines. | should | demo | The pure overlap predicate IS unit-tested (PopoverCrowdingTest); the relocation rendering and the leader lines are demo-verified. The gutter is the stage's own right edge, not the Notes tab (binding decision 8). |
| AC-46 | Given a host density of 2.0 and a known frame display scale, when a drag's raw pointer start/end offsets are converted through the canonical coordinate function, then the resulting device-pixel rect matches the hand-computed expected value (density-trap regression test — the POC's documented 2x offset must not reproduce). | must | test |  |
| AC-47 | Given the mirror is streaming and MirrorViewModel.frame publishes a new bitmap mid-drag, when the user releases, then the frozen bitmap is the one frame held at release time, not the one held at drag start. | must | test |  |
| AC-48 | Given N staged annotations, then the toolbar's stagedCounter slot renders CounterBadge(count=N, state=Staged) (or Empty at N=0); clicking the clearAction slot empties the staged list and the badge returns to Empty/0 while the stage remains frozen. | must | test | The reducer half (list emptied, frozen survives Clear) is unit-tested. The CounterBadge Empty/Staged mapping is a one-expression private helper in RectifyWindow.kt and is inspected, since compose-application has no test source set. |
| AC-49 | Given staged annotations exist, then the side panel's Notes tab renders one AnnotationRow per annotation in index order; clicking a row selects its corresponding mark on the frame (AnnotationState.Selected). | must | test | The selection reducer (SelectAnnotation sets selectedIndex; the stage maps selectedIndex to AnnotationState.Selected) is unit-tested and the ordering invariant (annotations always index-ordered, indices recomputed after delete) is unit-tested. One-row-per-annotation rendering is inspected in the Notes tab preview and the demo. |
| AC-50 | Given an annotation popover has focus, pressing Escape collapses only that popover (keeping the annotation pinned, count unchanged) without affecting the device picker or the active side-panel tab; Cmd+1/2/3 still switches side-panel tabs while a popover is focused. | must | test | The reducer half (SelectAnnotation(null) keeps the annotation and the count) is unit-tested. The non-interference half is argued structurally and demo-verified: handleShellShortcut matches only isMetaPressed (RectifyWindow.kt line 327 via shortcutToTab), and MirrorStage.Picker and MirrorStage.Frozen are mutually exclusive enum values so the picker's focus subtree cannot coexist with the popover's. ShortcutToTabTest.kt already covers the meta-only mapping. |
| AC-51 | Given a frame is frozen with staged annotations, when the user activates the Resume Live control, then the stage returns to the live mirror and the staged annotation list is cleared; a subsequent drag freezes a new frame. | must | demo | The reducer half (ResumeLive nulls frozen and frozenFrame; stage returns to Mirror; a following StageAnnotation freezes again) IS unit-tested even though the AC is marked demo — the demo covers the perceptual half, that live frames resume with no reconnection stall. |
| AC-52 | Given a frame is already frozen with one staged annotation, when the user drags and releases again on the stage, then a second staged annotation is added against the same frozen frame without re-triggering the freeze transition. | must | test |  |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-3 | Draw and freeze a single annotation | happy | ["Drag on the live mirror to draw a box", "Release to freeze the exact frame", "Popover appears autofocused", "Type a comment and press Enter to save"] |
| SC-4 | Multiple crowded annotations relocate to gutter | edge | ["Draw three annotation boxes close together", "Popovers would overlap", "Popovers relocate to the stage's right-edge gutter with straight leader lines"] |
| SC-8 | Density-trap regression on a Retina host | edge | ["Drag from a known pointer offset on a density=2.0 host", "Convert through the canonical coordinate function", "Assert the resulting device-pixel rect lands at the cursor, not density-x offset from it"] |
| SC-9 | Multiple annotations against one frozen frame | edge | ["Drag once to freeze the frame and create annotation #1", "Drag a second time on the still-frozen stage", "Assert annotation #2 is added against the same frozen bitmap, with no second freeze transition"] |
| SC-10 | Escape dismisses, delete removes | edge | ["Open an annotation's popover", "Press Escape — annotation remains pinned, staged count unchanged", "Reselect the annotation's row and click the delete control", "Annotation is removed and the staged count decrements"] |
| SC-11 | Resume live after annotating | happy | ["Freeze a frame and add one annotation", "Click Resume Live", "Stage returns to the live streaming mirror and the staged list is empty", "Drag again to freeze a new frame"] |

### definitionOfReady

- Preparation is signed off with readyForPlan true — done, gate approved 2026-10-15.
- Branch sdd/drag-annotate-frozen-frame exists off development with F-1 and F-2 merged.
- The 160 desktop tests are green on the branch tip before the first commit (baseline recorded).
- Every reused kit component's real signature has been read and confirmed to fit — done; the three that needed a change (MirrorFrameView, RectifyWindowShell, conditionally CommentPopover) are named with their exact parameter additions.
- The module-wiring question is settled: presentation-feature-mirror is EXTENDED, no new Gradle module, and neither its build.gradle.kts nor initKoin() needs an edit (AS-14, AS-15).
- The nine binding decisions are accepted as settled and not reopened during implementation.
- It is accepted up front that AC-5 and AC-7 will not have rendered-assertion coverage in this deliverable.

### definitionOfDone

- All 10 FR and 12 AC are satisfied or explicitly recorded as inspection/demo-verified with the evidence named.
- FrameGeometry exists, is internal and pure, has no @Composable and no ViewModel reference, and is the only place in the repo that converts pointer px to device px — verified by grep for `/ density` and `.dp` arithmetic on frame coordinates.
- ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest :data-device-api:desktopTest :data-device-impl:desktopTest :presentation-feature-mirror:desktopTest is green, with the pre-existing 160 still passing plus the new tests.
- ./gradlew :presentation-core-ui:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64 is green — the F-1 backtick-name trap does not recur.
- ./gradlew :presentation-core-ui:build and ./gradlew :presentation-feature-mirror:build and ./gradlew :compose-application:build each pass, run separately for the 3 GB daemon cap.
- ./gradlew :desktop-application:run launches, the Koin graph resolves with no 'No definition found', and SC-3 / SC-9 / SC-10 / SC-11 are walked through by hand at host density 2.0 with the result recorded.
- AC-9's relocation is demonstrated once with three crowded annotations and a screenshot attached to the PR.
- Every new invented value is a named constant with a KDoc rationale; grep finds no literal Color(0x…), no raw .dp at a call site and no inline TextStyle in the new code.
- AnnotationOverlay appears in StyleguideScreen with light and dark previews.
- New localisation keys exist in presentation-core-localisation/values/strings.xml and every one of them is referenced.
- The PR text calls out the two behaviour changes this spec makes live for the first time: Cmd+1/2/3 tab switching, and the side panel being permanently composed.
- A follow-up ticket 'Adopt compose-ui-test (desktop) and add rendering assertions for AC-5/AC-7' is filed.

### successMetrics

- Every drag-to-annotate action on the density-2.0 dev host lands the drawn box within 1 device pixel of the pointer — no density-trap regression.
- The existing 160 desktop tests stay green; this spec adds roughly 25-30 tests rather than destabilising any.
- New production code stays small: one new pure file, one new kit molecule, two defaulted kit parameters, six intents. If the diff grows past roughly 900 added production lines, the reuse-first premise has failed and the plan should be revisited.
- Resume Live returns to live pixels with no visible reconnection stall, because no adb session was torn down.
- Zero new Gradle modules, zero new catalog entries, zero convention-plugin edits, zero initKoin edits.

### architecture

#### approach

Extend presentation-feature-mirror in place across four layers — pure geometry, Orbit state, a render-only freeze, and assembled UI — plus two source-compatible defaulted parameters and one new molecule in presentation-core-ui, and a call-site rewiring in compose-application's RectifyWindow.kt. No new Gradle module, no new Koin module, no persistence, no serialization.

#### rationale

Every visual element the input artifact called unillustrated already exists as a shipped, previewed kit component except the dim-outside-boxes scrim, which is small enough to author as one molecule now. This matches the 'assemble, don't author' norm F-1 and F-2 both independently reached. The coordinate conversion is isolated into one pure internal function because an off-by-density bug there silently corrupts every annotation anchor and no other test would catch it; device screenshot pixels is the canonical storage space because it is resolution- and window-stable and is exactly what FR-6's label, Ruler's extentPx and F-7's eventual payload all need. Freeze-at-release rather than at drag start, because FR-6 frames the drag as happening live, so 'the exact current frame' reads as current-at-release — and reading MirrorViewModel.frame's latest value once inside the release handler needs no buffering, which makes AC-47 true by construction. Annotations go into the Orbit reduced state, the opposite of frames, because they are small, value-equal, mutated a handful of times per session and read by three simultaneous consumers — exactly the shape reduced state exists for — whereas frames are identity-compared blobs arriving ten times a second. A new presentation-feature-annotation module was rejected: it would need the live frame StateFlow and the stage state from MirrorViewModel, and two ViewModels sharing a bitmap stream is worse coupling than one class with six more handlers, on top of a new convention-plugin wiring and a load-order-significant initKoin entry for a split that F-5..F-8 have not yet justified.

#### alternativesConsidered

| title | whyRejected |
| --- | --- |
| Inline coordinate conversion per call site, POC-style | Three call sites (drag box, size label, ruler extent) would each have to get density right independently, and the POC shape is exactly what produced the documented 2x offset. It is also untestable without a display. This is the one place in the spec where laziness buys a silent data-corruption bug. |
| Store annotations in composable dp and convert only at send time (F-7's job) | dp is not resolution-stable across an aspect-fit frame — the stored value breaks the moment the window or the side panel is resized, which the shell supports as a first-class interaction. |
| Put the frozen ImageBitmap into MirrorUiState alongside the annotations | Violates presentation-feature-mirror's documented deviation 2: a ByteArray-bearing type compares by identity and could never deduplicate in the reducer. The bitmap goes on a second StateFlow beside frame; the reducer stores only its IntSize, which is all the meta lines, the Ruler extent and FrameGeometry actually need. |
| A new presentation-feature-annotation Gradle module | Needs the live frame StateFlow and the stage state from MirrorViewModel; two ViewModels sharing a bitmap stream is worse coupling than one class with six more handlers. Also costs a new convention-plugin wiring, a new Koin module and a load-order-significant initKoin entry, for a split F-5/F-6/F-7/F-8 have not landed to justify. When F-7 needs StagedAnnotation in a payload, lift the MODEL to domain-core — a 20-line move that needs no module today. |
| Pause the capture/transport loop while frozen | Pausing means setting sessionDeviceId = null, which also restarts discovery polling and tears the adb session down — a far bigger behavioural change than the power it saves, and it would put a reconnection stall in front of Resume Live. Explicitly accepted by FR-30 and recorded as a deferred optimisation. |
| Use AnnotationState.Frozen for frozen-frame marks | AnnotationMark.kt line 98 fills the mark with an opaque Theme.color.surface in that state, covering the exact device pixels the annotation points at — it would hide the bug the user is reporting. Pinned/Selected plus the new scrim needs no kit change. Binding decision 7. |
| Scrim as an even-odd hole path, as four rects around each box, or via BlendMode.Clear in a saveLayer | An even-odd path XORs overlapping holes and would re-dim their intersection; four-rects-per-box is simply wrong with two or more boxes; BlendMode.Clear needs a saveLayer, allocating a full-frame offscreen Skia surface every draw. clipPath(ClipOp.Difference) + one drawRect is a single path fill that unions overlapping holes correctly and never touches the ImageBitmap. |
| FR-9's gutter inside the Notes tab, as the clarify gate literally worded it | FR-28 already fills the Notes tab with one AnnotationRow per annotation, so every annotation would render twice in the same panel, and each leader line would have to cross from the stage over the resize handle into a separately-composed panel via onGloballyPositioned window coordinates. It also silently breaks when the user hides the side panel. Binding decision 8 revises this to an in-stage right-edge gutter where the mark and the line live in one coordinate space. |
| Add compose-ui-test (desktop) in this spec and write real rendering assertions for AC-5/AC-7 | That is a cross-cutting decision about the project's whole testing strategy, landing a new test dependency in every module through the convention plugins. It deserves its own spec. Nearly all the real risk in AC-5/AC-7 — where the box lands, what the label says, which marks exist — is pure and is covered here; what a renderer would add is 'the dashed border is dashed', which a preview shows faster. Raised as a follow-up ticket. |
| Author a second VerticalRuler kit component for FR-6's vertical guide | Ruler's own KDoc rules it out: 'the vertical ruler in the reference is the same strip rotated by the host, and duplicating the tick maths for it would buy nothing.' The host rotates the existing Ruler in a fixed-size Box with swapped dimensions. |
| Change MirrorHost's and MirrorStageContent's signatures to carry both the live and the frozen bitmap | Two positional nullable ImageBitmaps is a footgun, and it would churn RectifyShellContent's parameter list and every existing call site. MirrorHost instead computes displayFrame = frozenFrame ?: frame internally; the fallback is unreachable because StageAnnotation is dropped when frame is null, so frozen != null implies a non-null bitmap. |

#### components

| name | kind | status | registryRef | responsibility |
| --- | --- | --- | --- | --- |
| FrameGeometry | logic | new |  | NEW FILE presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/geometry/FrameGeometry.kt. `internal data class FrameGeometry(val framePx: Size, val deviceSize: IntSize, val density: Float)` with three pure members: `toDevice(start: Offset, end: Offset): IntRect` (normalises reversed drags, scales per axis by deviceSize/framePx, rounds each corner independently rather than rounding the size so adjacent rects cannot overlap by a rounding artifact, clamps into 0..deviceSize); `toFrame(rect: IntRect): Rect` (device px -> frame-local px, for rendering); `toDp(rect: Rect): DpRect` (frame px -> dp via density — THE ONLY place density is ever applied); plus `isMeaningfulDrag(rect: IntRect): Boolean` against MIN_DRAG_DEVICE_PX. Returns an empty rect when framePx is zero (first composition, before onSizeChanged) instead of dividing by zero. Internal, pure, no @Composable, no ViewModel reference — FR-26. |
| AnnotationFormat | logic | new |  | NEW FILE presentation-feature-mirror/.../core/annotation/AnnotationFormat.kt. `internal fun metaLine(rect: DeviceRect): String` producing CommentPopover's and AnnotationRow's documented shape `218×77 @ 1035,862`, and `internal fun sizeLabel(rect: DeviceRect): String` producing AnnotationMark's `545 × 254` shape. Pure and directly testable — this is the testable half of AC-5's 'live pixel-size label' and AC-7's 'meta line'. |
| popoverCrowding | logic | new |  | NEW FILE presentation-feature-mirror/.../core/annotation/PopoverCrowding.kt. `internal fun anyOverlap(rects: List<Rect>): Boolean` — an O(n^2) pairwise bounding-box test over a handful of nominal popover bounds. Uses constants rather than measured sizes because measuring in order to decide whether to compose is a circular layout dependency. Drives FR-9/AC-9. |
| MirrorContract additions | logic | reuse | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | CHANGED FILE. Adds public `DeviceRect(left, top, right, bottom)` with width/height, `StagedAnnotation(index, rect, comment)`, `FrozenFrame(deviceSize: IntSize, annotations: List<StagedAnnotation>, selectedIndex: Int?)`; a `frozen: FrozenFrame? = null` field on MirrorUiState; a `Frozen` entry on the MirrorStage enum with the stage getter ordered Picker > IosDeferred > Frozen > Streaming/Mirror > NoSignal; and six MirrorIntent cases: StageAnnotation(rect), SelectAnnotation(index: Int?), CommentAnnotation(index, text), DeleteAnnotation(index), ClearStaged, ResumeLive. A convenience `val stagedCount: Int get() = frozen?.annotations?.size ?: 0` so the toolbar does not reach through two nullables. No new sealed hierarchy outside the contract file — Orbit four-file convention preserved. |
| MirrorViewModel additions | logic | reuse | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | CHANGED FILE. Adds `private val currentFrozenFrame = MutableStateFlow<ImageBitmap?>(null)` and `public val frozenFrame: StateFlow<ImageBitmap?>`, six private handlers dispatched from the existing handleIntent when(), and extends selectPlatform() and stopSession() to null both the frozen bitmap and the frozen state. StageAnnotation does: read currentFrame.value once (null -> drop the intent), set currentFrozenFrame.value = currentFrozenFrame.value ?: thatFrame, then reduce the annotation in with the next index. sessionDeviceId and the flatMapLatest gate are NEVER touched. Constructor unchanged, so no Koin or initKoin edit. |
| MirrorHost | ui | reuse | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorHost.kt | CHANGED FILE, UNCHANGED PUBLIC SIGNATURE. Collects viewModel.frozenFrame alongside viewModel.frame and passes `frozenFrame ?: frame` as the single bitmap to the existing three-argument content lambda. Keeps RectifyShellContent's parameter list and every existing call site source-compatible. |
| MirrorContent / MirrorStageContent / MirrorStatusLine | ui | reuse | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContent.kt | CHANGED FILE. The when(state.stage) gains a Frozen branch (the compiler forces it — MirrorStage is an enum with no else). The Mirror branch delegates to MirrorLiveStage and the Frozen branch to MirrorFrozenStage. The private indicatorState() gains `state.frozen != null -> StatusIndicatorState.Frozen` ahead of the Streaming check, and indicatorLabel() gains the matching stringResource — this is the first production caller of the shipped-but-unused StatusIndicatorState.Frozen. |
| MirrorAnnotationStage | ui | new |  | NEW FILE presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt. Two internal composables plus the file-top named constants. MirrorLiveStage: MirrorFrameView(frame, deviceName, overlay = { ... }) where the overlay carries Modifier.onSizeChanged into a remembered framePx, a Modifier.pointerInput/detectDragGestures layer holding dragStart/dragCurrent in remember (never in Orbit), one AnnotationMark(state = Drawing, sizeLabel = sizeLabel(rect)) positioned from FrameGeometry.toFrame -> toDp, and a horizontal Ruler(totalPx = deviceWidth, extentPx = selection) at the top edge plus the same Ruler rotated 270 degrees in a fixed-size Box at the leading edge for the vertical guide. On release it emits StageAnnotation only when isMeaningfulDrag passes. MirrorFrozenStage: the same MirrorFrameView over the frozen bitmap with an overlay carrying AnnotationOverlay (scrim + Pinned/Selected marks), the per-mark CommentPopover or the FR-9 gutter, and a Resume Live AppButton(size = AppButtonSize.Toolbar) at the stage's top-right. |
| MirrorNotesTab | ui | new |  | NEW FILE presentation-feature-mirror/.../source/mirror/MirrorNotesTab.kt. `public fun MirrorNotesTab(state: MirrorUiState, onIntent: (MirrorIntent) -> Unit, modifier: Modifier)` — public because compose-application composes it. One AnnotationRow per staged annotation in index order (index, comment-or-null for the empty copy, meta = metaLine(rect), selected = index == selectedIndex, onClick -> SelectAnnotation). Renders an empty-state line when there are no annotations. Carries its own @Preview per the compose-preview-convention. |
| AnnotationOverlay | ui | new |  | NEW FILE presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationOverlay.kt. The one genuinely new visual piece. Stateless: takes a list of frame-local px rects with their AnnotationState, badge and sizeLabel, plus a `dimmed: Boolean`. Draws one Canvas doing `clipPath(unionPath, ClipOp.Difference) { drawRect(ScrimFill) }` when dimmed and the rect list is non-empty, then composes one AnnotationMark per rect positioned by Modifier.offset on dp derived from the caller. Imports atoms only (AnnotationMark), so the molecule layer is correct. ScrimFill is a named private constant, deliberately dark in both themes with a KDoc rationale following CommentPopover's and CounterBadge's documented precedent. Ships with light and dark @Previews and a StyleguideScreen entry. |
| MirrorFrameView | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/mirror/MirrorFrameView.kt | CHANGED, +1 SOURCE-COMPATIBLE DEFAULTED PARAMETER: `overlay: @Composable BoxScope.() -> Unit = {}`, composed as the last child of the inner aspect-fit Box (the one carrying .aspectRatio and the semantics contentDescription). This is what guarantees the annotation layer is literally the same rectangle as the image and keeps the aspect-fit maths in one file. No other change; the existing preview and every existing call site keep compiling. |
| RectifyWindowShell | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/RectifyWindowShell.kt | CHANGED, +1 SOURCE-COMPATIBLE DEFAULTED PARAMETER: `tabCounts: Map<ShellTab, Int> = emptyMap()`, forwarded to the SidePanelFrame call it already makes. VERIFIED GAP — the shell composes SidePanelFrame at lines 87-92 without passing tabCounts, so SidePanelFrame's existing tabCounts parameter is currently unreachable from the window. FR-28's Notes count cannot be wired without this. The brainstorm's 'no shell change needed at all' is incorrect on this point. |
| CommentPopover | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/annotation/CommentPopover.kt | USED UNCHANGED IF POSSIBLE. Already defaults placeholder to exactly "What's wrong here?", renders the index badge, the meta line, a focus-gated saveHint and an onClose the PRD repurposes as delete — FR-8 line by line. CONTINGENT +1 DEFAULTED PARAMETER `focusRequester: FocusRequester? = null` forwarded to the BasicTextField, only if parent-delegated focus does not reach the field (see risk 3). The BasicTextField currently owns a private interactionSource and has no focus hook, so the contingency is real and budgeted in step 8. |
| AnnotationMark + AnnotationState | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/AnnotationMark.kt | ZERO CHANGES. Drawing state already carries the 6/4 dashed border, the GlyphBadge corner index badge and the MeasureTag size label — the whole of FR-6's drag box. Use Drawing live, Pinned when frozen, Selected for the active mark. AnnotationState.Frozen is deliberately never used (binding decision 7). |
| Ruler | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/Ruler.kt | ZERO CHANGES. Verified to take `totalPx: Int` and `extentPx: IntRange` already in DEVICE pixels — exactly this spec's canonical storage space — and to draw the red extent plus the delta MeasureTag itself. The vertical guide is this same component rotated 270 degrees by the host, as its own KDoc prescribes. |
| AnnotationPopover | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationPopover.kt | ZERO CHANGES. The read-only gutter form for FR-9. Its KDoc already states that placement and leader routing belong to the host, which is exactly the division of labour here. |
| AnnotationRow | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/AnnotationRow.kt | ZERO CHANGES. Purpose-built for FR-28: index GlyphBadge, comment or the defaulted 'No comment yet' copy, meta line, selected highlight, onClick. The feature supplies the empty copy through stringResource rather than relying on the English default. |
| CounterBadge + CounterBadgeState | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/badge/CounterBadge.kt | ZERO CHANGES. FR-27's stagedCounter verbatim — Empty/Staged fills and the '3 staged' label already exist, and RectifyWindow.kt line 233 already composes it with a hardcoded count = 0 that this spec replaces with the live count. |
| StatusIndicator + StatusIndicatorState.Frozen | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/indicator/StatusIndicator.kt | ZERO CHANGES. Shipped with zero callers — 'the stream is paused on a frame so it can be annotated'. MirrorStatusLine's private indicatorState() gains one branch, giving the frozen stage its status-bar signal for free and giving the enum value its first caller. |
| AppButton | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt | ZERO CHANGES. The clearAction (Ghost, enabled when stagedCount > 0) and the Resume Live control. NOTE the real parameter name is `size: AppButtonSize`, NOT ButtonSizeType — ButtonSizeType belongs to IconButton. The brainstorm said ButtonSizeType; it is wrong. |
| ShellToolbar / SidePanelFrame / ShellState | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ | ZERO CHANGES to all three. ShellToolbar's stagedCounter and clearAction are named slots reserved for this feature. SidePanelFrame already accepts tabCounts (it just was not being handed one). ShellState.activeTab has a private setter, so the Notes auto-activation must go through shellState.selectTab(ShellTab.Notes) from RectifyWindow — the feature module cannot and must not reach it. |
| RectifyWindow.kt | ui | reuse | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt | CHANGED FILE. stagedCounter -> CounterBadge(count = mirror.stagedCount, state = Empty-at-0-else-Staged). clearAction -> AppButton(enabled = stagedCount > 0, onClick = ClearStaged). sidePanel becomes unconditionally non-null, composing MirrorNotesTab on the Notes tab — which means the `sidePanel` val moves inside the MirrorHost content lambda (it now needs mirror and onIntent) and handleShellShortcut's isPanelVisible becomes a constant true. tabCounts = mapOf(ShellTab.Notes to stagedCount). A LaunchedEffect on 'stagedCount >= 1' calls shellState.selectTab(ShellTab.Notes) so the first annotation of a frozen frame reveals the panel that proves it exists; it deliberately does not re-select on later annotations. The window's existing literal labels (Clear, Resolve & Send, No device, staged) stay literal — that is F-1's established pattern for this file and changing it is out-of-scope churn; all NEW copy is rendered by the feature module through stringResource. |
| Localisation strings | data | reuse | presentation-core-localisation/src/commonMain/composeResources/values/strings.xml | CHANGED FILE. New keys following the <feature>_<element> convention: mirror_annotation_placeholder, mirror_annotation_save_hint, mirror_annotation_empty_comment, mirror_annotation_notes_empty, mirror_annotation_delete, mirror_action_resume_live, mirror_status_frozen. Only the English values/ file exists. Every key must have a live reference — this repo has previously accumulated unused keys. |
| StyleguideScreen | ui | reuse | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/showcase/StyleguideScreen.kt | CHANGED FILE, one added section showing AnnotationOverlay over a placeholder bitmap with two holes, so visual QA can see the scrim in both themes. |

#### dataModel

| entity | fields |
| --- | --- |
| DeviceRect | ["left: Int — device screenshot pixels", "top: Int — device screenshot pixels", "right: Int — device screenshot pixels, exclusive", "bottom: Int — device screenshot pixels, exclusive", "width: Int (derived)", "height: Int (derived)"] |
| StagedAnnotation | ["index: Int — 1-based, matches the GlyphBadge and the AnnotationRow badge", "rect: DeviceRect — the canonical anchor", "comment: String = \"\" — empty means the row renders its empty-state copy"] |
| FrozenFrame | ["deviceSize: IntSize — the frozen bitmap's dimensions; the ONLY pixel-derived value that enters reduced state", "annotations: List<StagedAnnotation> — always index-ordered, indices recomputed after a delete", "selectedIndex: Int? — null means no popover is open; clamped or nulled whenever the selected annotation is removed"] |
| FrameGeometry (internal, transient, never stored) | ["framePx: Size — the aspect-fit frame box in the same pixel space as the pointer offsets", "deviceSize: IntSize — read from the decoded ImageBitmap, never from GetProp", "density: Float — applied on the render leg only"] |

#### interfaces

| name | contract |
| --- | --- |
| FrameGeometry.toDevice | (start: Offset, end: Offset) -> IntRect. Pure. Normalises a reversed drag to a positive rect, converts via devicePx = pointerPx / framePx * deviceSize with NO density term, rounds each corner independently, clamps into 0..deviceSize on both axes. Returns IntRect.Zero when framePx has a zero dimension. |
| FrameGeometry.toFrame | (rect: IntRect) -> Rect. Pure. Device px back to frame-local px for rendering. Inverse of toDevice up to rounding. |
| FrameGeometry.toDp | (rect: Rect) -> DpRect. Pure. Frame px to dp by dividing by density. The single place in the entire feature where density appears. |
| FrameGeometry.isMeaningfulDrag | (rect: IntRect) -> Boolean. True when both width and height are at least MIN_DRAG_DEVICE_PX (8). Guards the worst accidental interaction in a live-mirroring tool: a stray click freezing the mirror. |
| MirrorViewModel.frozenFrame | StateFlow<ImageBitmap?>. Non-null exactly while MirrorUiState.frozen is non-null. Set only inside the StageAnnotation handler and cleared inside ResumeLive, selectPlatform and stopSession — always in the same intent as the matching reduce, so the two channels cannot drift. |
| MirrorFrameView.overlay | @Composable BoxScope.() -> Unit = {}. Composed as the last child of the aspect-fit Box, so the overlay's coordinate space is byte-identical to the painted image's. Defaulted, so every existing call site is unaffected. |
| RectifyWindowShell.tabCounts | Map<ShellTab, Int> = emptyMap(). Forwarded verbatim to SidePanelFrame's existing tabCounts parameter. Defaulted, so every existing call site and preview is unaffected. |
| MirrorNotesTab | public @Composable (state: MirrorUiState, onIntent: (MirrorIntent) -> Unit, modifier: Modifier) -> Unit. The only new public composable in the feature module; composed by RectifyWindow into SidePanelFrame's Notes slot. |

#### stateManagement

Orbit MVI in presentation-feature-mirror, four-file convention, existing container. Three tiers by mutation frequency: (1) transient drag offsets live in composable `remember` and never reach Orbit — a pointer move at 60 Hz through the reducer is the mistake F-2 avoided for frames; (2) pixel blobs (the live frame and now the frozen frame) live on plain StateFlows outside the reducer, preserving the module's documented deviation 2; (3) semantic state (the staged annotation list, the selected index, the frozen frame's IntSize) lives in MirrorUiState because it is small, value-equal, mutated a handful of times per session and read simultaneously by the toolbar badge, the Notes tab and the stage marks. MirrorStage.Frozen is derived from `frozen != null` in the existing stage getter, so no second flag exists to fall out of sync. The frozen bitmap and the frozen reduced state are set inside the same intent, and the render path falls back to the live frame if the bitmap were ever null — one `?:`, no sync machinery.

#### navigation

- None. presentation-feature-mirror has no Destination and will not get one — it is a set of shell slots the desktop window fills, as its module CLAUDE.md documents (deviation 1). The five-step checklist in .claude/rules/navigation.md is deliberately not applied: no Destination case, no navSavedStateConfiguration entry, no NavigationHost branch.
- The only navigation-like change is within the shell: Cmd+1/2/3 side-panel tab switching becomes live for the first time because the panel slot stops being null, and the first annotation of a frozen frame auto-selects the Notes tab.

#### affectedModules

- presentation-feature-mirror — 2 new core files, 2 new source files, 4 changed files, 3 new test files, 1 changed test helper. No build.gradle.kts edit (AS-14). No Koin edit (AS-15).
- presentation-core-ui — 1 new molecule, 2 defaulted parameters on shipped components (MirrorFrameView, RectifyWindowShell), 1 contingent defaulted parameter (CommentPopover), 1 StyleguideScreen section, 1 new test file. No build.gradle.kts edit.
- presentation-core-localisation — 7 new string keys in values/strings.xml. No build.gradle.kts edit.
- compose-application — RectifyWindow.kt (desktopMain) call-site rewiring only. No build.gradle.kts edit, no InitKoin.kt edit.
- NOT touched: data-device-api, data-device-impl, data-repository-impl, domain-core, domain-usecase-api, domain-usecase-impl, build-logic, gradle/libs.versions.toml, aos-application, ios-application, desktop-application.

### projectContext

#### reuseComponents

| id | name | registryRef | note |
| --- | --- | --- | --- |
| AnnotationMark | AnnotationMark + AnnotationState | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/annotation/AnnotationMark.kt | Zero changes. Drawing/Pinned/Selected used; Frozen deliberately not used. |
| Ruler | Ruler | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/Ruler.kt | Zero changes. totalPx/extentPx are already in device pixels. |
| CommentPopover | CommentPopover | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/annotation/CommentPopover.kt | Zero changes intended; one contingent defaulted focusRequester parameter if parent-delegated focus does not reach the BasicTextField. |
| AnnotationPopover | AnnotationPopover | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationPopover.kt | Zero changes. FR-9's gutter form. |
| AnnotationRow | AnnotationRow | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/AnnotationRow.kt | Zero changes. FR-28's Notes tab row. |
| CounterBadge | CounterBadge + CounterBadgeState | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/badge/CounterBadge.kt | Zero changes. FR-27's stagedCounter. |
| StatusIndicator | StatusIndicator + StatusIndicatorState.Frozen | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/indicator/StatusIndicator.kt | Zero changes. Frozen was shipped with no callers; this spec is its first. |
| AppButton | AppButton (Ghost / Primary, AppButtonSize) | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/button/AppButton.kt | Zero changes. The size parameter is AppButtonSize, not ButtonSizeType. |
| GlyphBadge | GlyphBadge | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/badge/GlyphBadge.kt | Zero changes. Used transitively by AnnotationMark and AnnotationRow. |
| MeasureTag | MeasureTag | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/tag/MeasureTag.kt | Zero changes. Used transitively by AnnotationMark's sizeLabel and Ruler's extent label. |
| ShellToolbar | ShellToolbar (stagedCounter / clearAction slots) | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellToolbar.kt | Zero changes. Slots reserved for this feature; only the window call site changes. |
| SidePanelFrame | SidePanelFrame (tabCounts + per-tab content slot) | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/SidePanelFrame.kt | Zero changes to the component itself — but its tabCounts parameter is currently unreachable because RectifyWindowShell does not forward one. |
| ShellState | ShellState / ShellTab / shortcutToTab | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/shell/ShellState.kt | Zero changes. selectTab is the only way to set the active tab (activeTab has a private setter). |
| DevicePickerPanel | DevicePickerPanel's onPreviewKeyEvent pattern | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/picker/DevicePickerPanel.kt | Pattern reuse only, no code change: the established in-repo way to scope keys to a focus subtree. The popover copies it instead of inventing a dispatch layer. |
| MirrorViewModel.frame | MirrorViewModel.frame + the sessionDeviceId gate | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | frame.value read once at release IS the freeze (AC-47). The gate is deliberately not touched, which is what makes Resume Live free (FR-30). |
| MirrorFakes | MirrorFakes / Orbit test harness | presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt | Extended, not replaced: already pins Orbit's eventLoopDispatcher via Orbit.configureDefaults and documents the runCurrent() subscription quirk. New tests extend it instead of re-solving both traps. Gains an optional decodeFrame parameter and a fake decoder. |

#### reuseAssets

| id | name | registryRef |
| --- | --- | --- |
| X | X icon (CommentPopover's close affordance, repurposed as delete) | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/atom/icon/X.kt |

#### reuseStyles

| id | name | registryRef |
| --- | --- | --- |
| Theme.color.annotate | The annotation red — the signal colour reserved for annotations | presentation-core-styling Theme.color |
| Theme.color.pop | The always-dark popover plate | presentation-core-styling Theme.color |
| Theme.color.seg | Selected-row fill (AnnotationRow) and CounterBadgeState.Empty fill | presentation-core-styling Theme.color |
| Theme.spacing / Theme.stroke / Theme.typography / Theme.cornerToken | All spacing, stroke widths, type roles and corner tokens for the new molecule and the new feature composables | presentation-core-styling Theme |

#### existingFeatures

| id | name | note |
| --- | --- | --- |
| F-1 | app-shell-window-layout | Merged. Supplies the toolbar slots, the side panel frame, the stage surface and the Cmd+1/2/3 mapping that this spec activates. |
| F-2 | android-device-discovery-mirroring | Merged. Supplies MirrorViewModel, the live frame StateFlow and the sessionDeviceId-gated capture loop that this spec must leave running. |

#### newToBuild

| id | name | note |
| --- | --- | --- |
| FrameGeometry | presentation-feature-mirror/.../core/geometry/FrameGeometry.kt | internal, pure, tested first |
| AnnotationFormat | presentation-feature-mirror/.../core/annotation/AnnotationFormat.kt | internal, pure — metaLine / sizeLabel |
| PopoverCrowding | presentation-feature-mirror/.../core/annotation/PopoverCrowding.kt | internal, pure — anyOverlap |
| MirrorAnnotationStage | presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt | internal composables: MirrorLiveStage + MirrorFrozenStage |
| MirrorNotesTab | presentation-feature-mirror/.../source/mirror/MirrorNotesTab.kt | public composable, the Notes tab content |
| AnnotationOverlay | presentation-core-ui/.../kit/molecule/annotation/AnnotationOverlay.kt | THE one new kit component: scrim + marks |
| FrameGeometryTest | presentation-feature-mirror/src/commonTest/.../FrameGeometryTest.kt | new test file |
| AnnotationFormatTest | presentation-feature-mirror/src/commonTest/.../AnnotationFormatTest.kt | new test file |
| PopoverCrowdingTest | presentation-feature-mirror/src/commonTest/.../PopoverCrowdingTest.kt | new test file |
| MirrorViewModelAnnotationTest | presentation-feature-mirror/src/commonTest/.../MirrorViewModelAnnotationTest.kt | new test file |

#### registryNote

No registry exists at .pipeline/sdd/docs/registry/ (components.json, assets.json, styles.json, features.json, bugs.json are all absent) — verified by glob. Every registryRef above is therefore a verified source path rather than a registry pointer, and knownRisksFromQA is populated from the two merged specs' recorded defects rather than from bugs.json. The orchestrator owns any registry delta.

### resources

#### rules

| ref | why |
| --- | --- |
| CLAUDE.md#Build Commands | Per-module gradle invocations; the Kotlin daemon is capped at 3 GB so modules are built separately, never via a root build. |
| .claude/rules/design-tokens.md | No literal Color(0x…), no raw .dp at a call site, no inline TextStyle; strict atom -> molecule -> organism layering, which places AnnotationOverlay in molecule/annotation/ importing atoms only. |
| .claude/rules/orbit-mvi.md | The four-file contract shape for MirrorContract/MirrorViewModel additions; handlers are `= intent { reduce { state.copy(...) } }`. |
| .claude/rules/package-layout.md | core/ is internal-only (FrameGeometry, AnnotationFormat, PopoverCrowding), source/ is the public surface (DeviceRect, StagedAnnotation, MirrorNotesTab); explicitApi() polices the boundary at compile time. |
| .claude/rules/testing.md | No test exists for Compose UI anywhere; commonTest names must be plain camelCase with no backticks; the convention plugin already wires kotlin-test so no per-module test dependency is added. |
| .claude/rules/localisation.md | <feature>_<element> key convention; only values/strings.xml exists; grep before assuming a key is live and keep a key only as long as something references it. |
| .claude/rules/koin-di.md | Read to confirm the negative: no new use case means no new binding, so the load-order-significant initKoin list is untouched. |
| .claude/rules/navigation.md | Read to confirm the negative: presentation-feature-mirror has no Destination by design, so the five-step checklist does not apply. |
| .claude/rules/compose-preview-convention.md | Every new content composable ends with a private <Name>Preview wrapped in AppTheme and passing a default-constructed state with no-op callbacks; previews are the documented evidence for AC-5/AC-7's unrenderable half. |
| .claude/rules/convention-plugins.md | Confirms FeatureConventionPlugin already supplies kotlin-test to commonTest and registers the desktop target, so no build-logic change is needed. |
| .claude/rules/platform-coverage.md | commonTest compiles for iOS even though only desktopTest runs — the reason the backtick-name trap matters and the reason compileTestKotlinIosArm64 is in the Definition of Done. |
| .claude/rules/version-catalog-hygiene.md | Zero new catalog entries in this spec; read to confirm nothing new is required. |
| presentation-feature-mirror/CLAUDE.md | The module's two documented deviations (MirrorHost not MirrorScreen; no pixels in reduced state) and the two Orbit test-environment facts (configureDefaults pinning, runCurrent for the subscription) that the new ViewModel tests depend on. |
| presentation-core-ui/CLAUDE.md | The atom/molecule/organism placement table and the 'kit components take copy as parameters, not resources' rule that AnnotationOverlay must follow. |
| compose-application/CLAUDE.md | initKoin's load-order significance and the core/ vs source/ split in the window module. |

#### skills

| name | why |
| --- | --- |
| sdd-build-run-pipeline | Owns the stage gates, the plan schema this payload validates against, and the Stage 3 backlog slicing that consumes implementationPlan. |

#### agents


#### docs

| ref | why |
| --- | --- |
| rectify-poc/app/src/main/kotlin/rectify/Main.kt#L122-L126 | The density-trap comment, verbatim: pointer events and layout are raw screen px while Modifier.offset/size take Dp, which Compose multiplies by density again. |
| rectify-poc/app/src/main/kotlin/rectify/Main.kt#L229-L237 | The per-corner `/ density / scale` conversion that FrameGeometry.toDevice replaces with one tested function; its comment confirms device screenshot pixels as the target space. |
| .pipeline/sdd/docs/android-device-discovery-mirroring/plan/plan.json | Shape reference for this plan, and the source of the per-module desktopTest command list used in the Definition of Done. |

### errorStates

| state | behavior |
| --- | --- |
| empty | Frozen frame with zero annotations (after Clear, or after deleting the last one): the stage stays frozen per AC-48, the scrim degrades to a plain full-frame dim because the hole list is empty, and the Resume Live control stays visible as the way out. AnnotationOverlay must not build a degenerate Path from an empty list. |
| empty | Notes tab with zero annotations: renders one muted line of empty-state copy (mirror_annotation_notes_empty), never a blank panel. The tab count renders as absent rather than 0 so SidebarTabs does not show a meaningless zero. |
| empty | Annotation with no comment yet: AnnotationRow renders its empty-state copy rather than collapsing, because the mark still exists on the frame and the user needs a way back to it. |
| loading | Frame box size is 0 on first composition, before onSizeChanged reports: FrameGeometry returns an empty rect and the drag layer composes nothing rather than dividing by zero. |
| loading | Session started but no frame decoded yet (frame == null on the Mirror stage): unchanged F-2 behaviour, the stage's dot grid shows. A drag release in this state drops the StageAnnotation intent and the stage stays live — nothing to freeze. |
| error | Sub-minimum drag (a click, or under MIN_DRAG_DEVICE_PX in either axis): discarded silently — no annotation, no freeze. Without this a stray click on the stage freezes the mirror, the worst accidental interaction in a live-mirroring tool. |
| error | Drag starting inside the frame and ending in the letterbox, or a reversed right-to-left / bottom-to-top drag: toDevice normalises to a positive rect and clamps into device bounds, so the stored rect is never negative and never past width/height. |
| error | Delete of the currently selected annotation: selectedIndex is recomputed — clamped into the new range or nulled — never left dangling at a removed index, and the remaining annotations' indices are renumbered so the badges stay 1..N. |
| error | MirrorSession.Lost while frozen: the frozen frame and its annotations are KEPT. The user is mid-sentence and a device dying must not erase their work. The status indicator stays Frozen; Resume Live then lands on NoSignal. |
| error | Platform switched to iOS, or StopMirroring, while frozen: selectPlatform/stopSession already null the session and must also null both the frozen state and the frozen bitmap, or a stale Android frame would sit under the iOS notice. |
| error | An annotation drawn at the extreme right or bottom edge: the CommentPopover (min 300 dp wide) would overflow the stage, so its x is clamped into the stage bounds. Its tail has a fixed 18 dp inset and will not follow — a known demo-fidelity limitation whose eventual fix is a tailOffset parameter. |
| error | Escape pressed with no popover focused: falls through untouched. The device picker's own Escape handler and the shell must still behave (AC-50). |

### observability

#### logs

- No new Kermit logger is introduced in presentation-feature-mirror — it has none today and a reducer that logs every annotation would be noise. The existing RectifyWindow logger keeps its warnings.
- One exception worth considering at review time only: a single Logger.w when StageAnnotation is dropped because frame == null, since that is the one silent discard a user could report as 'the drag did nothing'. Deferred unless review asks for it.

#### metrics

- No new metric. The existing MirrorStatsModel line (tool version, frame dimensions, fps) keeps rendering while frozen, which is itself the honest signal that the capture loop was never paused.

#### analyticsEvents

- None. This project ships no analytics pipeline and this spec does not introduce one.

### accessibility

| id | requirement |
| --- | --- |
| A11Y-1 | NFR-8: the comment popover is fully keyboard operable — Enter commits the comment and keeps the annotation pinned, Escape collapses the popover to a pinned unselected mark without deleting it. Both are handled by onPreviewKeyEvent on the popover's own focus subtree, the pattern DevicePickerPanel already established. |
| A11Y-2 | The popover's comment field receives focus automatically when an annotation is created or selected, so the user can type immediately without reaching for the pointer (AC-7). If parent-delegated focus does not reach the BasicTextField, CommentPopover gains a defaulted focusRequester parameter. |
| A11Y-3 | Enter and Escape in the popover must not consume or bubble into the window-level Cmd+1/2/3 shortcut, and Cmd+1/2/3 must keep switching tabs while a comment field has focus. The event shapes are disjoint (handleShellShortcut matches only isMetaPressed) and the stages are mutually exclusive, so no new dispatch layer is added (AC-50). |
| A11Y-4 | Typing a bare digit into the comment field must never switch side-panel tabs — the same isMetaPressed disjointness. Explicitly walked through in the demo. |
| A11Y-5 | The delete control is a real Role.Button with a content description (CommentPopover's CloseButton already is), and the Resume Live control is an AppButton with visible text rather than an icon-only affordance. |
| A11Y-6 | MirrorFrameView's existing semantics contentDescription naming the mirrored device is preserved when the overlay slot is added — the overlay is composed inside that Box and must not replace or clear its semantics. |
| A11Y-7 | NFR-9: light and dark parity for every new surface. The scrim and the popovers are deliberately dark in BOTH themes (they sit over unknown-brightness device content), which is the documented exception pattern, not a parity failure — the mark borders, the badges, the Notes rows and the Resume Live button all follow the theme and are checked in both. |
| A11Y-8 | Annotation state is never signalled by colour alone: every mark carries a numbered GlyphBadge and every Notes row repeats that number, so the frame and the sidebar stay cross-referenceable without colour discrimination. |

### performanceBudget

| metric | target |
| --- | --- |
| Scrim + overlay draw cost over a live-sized frame | One Canvas, one Path built from at most ~10 rects, one clipPath(ClipOp.Difference) and one drawRect per draw. NO saveLayer, NO offscreen Skia surface, and the ImageBitmap is never read, copied or composited. Budget: at most one extra full-frame-box fill per draw pass — the frame box is the aspect-fit display rectangle (roughly 713x1584 px at the default window on a 1080x2400 device), not the 1080x2400 source bitmap. |
| Path construction per draw | O(n) in the number of annotations, n <= ~10 in practice. Build the union Path inside remember(rects) so a pointer move or a recomposition that does not change the rect list does not rebuild it. |
| Drag feedback latency | The drawn box follows the pointer within one frame (<= 16 ms at 60 Hz). Achieved by keeping dragStart/dragCurrent in composable remember so a pointer move recomposes only the overlay subtree and never passes through the Orbit reducer. |
| Reducer passes per gesture | Exactly one (the StageAnnotation on release). Zero during the drag. This is the invariant that keeps a 60 Hz gesture from becoming 60 state copies a second, mirroring F-2's frame decision. |
| Live mirror glass-to-glass latency (NFR-1) | Unchanged at under ~150 ms. Freeze is render-only — sessionDeviceId and the flatMapLatest gate are untouched — so the capture/decode/transport path has exactly the same cost before, during and after a freeze. |
| Resume Live responsiveness | First live frame visible within one capture interval (~100 ms at the current cadence), with no adb reconnection. This is the whole payoff of the render-only freeze and is the headline thing the SC-11 demo checks. |
| Frozen-stage steady-state cost | No per-frame work on the stage while frozen — the static bitmap and the scrim redraw only on recomposition, not on each arriving capture. Accepted cost: the device keeps running screencap behind a static picture (FR-30), logged as a deferred optimisation whose fix would be an isFrozen flag in the transport's adaptive interval, not a session teardown. |
| FR-9 crowding check | O(n^2) pairwise over n <= ~10 nominal rects, recomputed only when the annotation list or the frame box changes — well under a microsecond and explicitly not worth a sweep-line. |
| Memory | At most one extra ImageBitmap alive at a time (the frozen frame), released on Resume Live, platform switch or StopMirroring. No history, no buffering, no serialization (AS-11). |
| Added production code | Roughly 900 added lines or fewer across all four modules. Exceeding that means the reuse-first premise has failed and the plan should be revisited rather than the budget raised. |

### i18n

#### locales

- en (values/strings.xml) — the only locale file that exists today

#### strings

- mirror_annotation_placeholder — "What's wrong here?" — passed to CommentPopover, which already defaults to this exact string; the feature still passes it so the copy is translatable.
- mirror_annotation_save_hint — "⏎ save" — CommentPopover's focus-gated saveHint.
- mirror_annotation_empty_comment — "No comment yet" — AnnotationRow's emptyLabel, overriding the kit's English default.
- mirror_annotation_notes_empty — the Notes tab's zero-annotation line.
- mirror_annotation_delete — accessibility label for the delete control, which means DELETE here rather than close.
- mirror_action_resume_live — "Resume Live" — the stage's top-right control.
- mirror_status_frozen — "FROZEN" — MirrorStatusLine's label for StatusIndicatorState.Frozen.

#### notes

presentation-core-ui must not depend on presentation-core-localisation, so AnnotationOverlay and every other kit-level change takes copy as a parameter with an English default and the feature module supplies stringResource(). Machine output — the device-pixel size label and the `218×77 @ 1035,862` meta line — is rendered mono and deliberately carries no resource key, matching how serials, versions and fps are already handled. RectifyWindow.kt's existing literal labels (Clear, Resolve & Send, No device, staged) stay literal: that is F-1's established pattern for that file and converting them is out-of-scope churn for this spec; it should be its own small ticket.

### security

#### dataHandling

- Annotations are held in memory only. No disk write, no serialization, no new data module, no file I/O — the POC's JSON writer is F-7's and F-8's business (AS-11).
- The frozen bitmap is a reference to an already-decoded in-memory frame. Nothing new is captured, copied to disk or transmitted.
- Comment text is free user input but never reaches a shell command, a file path or a network call in this spec. It is rendered and held, nothing more.

#### permissions

- No new OS permission, no new adb capability, no new process invocation. The capture loop this spec rides on is F-2's and is unchanged.

#### privacy

- SEC-1 stays closed. Frame dimensions are read from the decoded ImageBitmap, never from GetProp. F-2 left GetProp with zero production callers and NFR-5 holds there only by that absence; this spec creates no new caller — a grep for GetProp must still return zero production hits after this lands.
- No telemetry, no analytics, no crash reporting: a device screenshot and a bug note never leave the host in this deliverable.

### rollout

#### featureFlags

- None. The feature is unconditional once merged — a flag for a desktop developer tool with one user-facing surface would be scaffolding for a rollback that a revert already covers.

#### migrations

- None. No persistence, no schema, no stored preference is added or changed.

#### backwardCompatibility

Every kit API change is a defaulted parameter added at the end of the parameter list (MirrorFrameView.overlay, RectifyWindowShell.tabCounts, contingently CommentPopover.focusRequester), so every existing call site, preview and test compiles unchanged. MirrorHost's and MirrorStageContent's public signatures do not change at all. Two user-visible behaviour changes DO land and must be called out in the PR: (1) Cmd+1/2/3 side-panel tab switching becomes live for the first time, because handleShellShortcut only consumed the event while a panel was on screen and the panel was always null; (2) the side panel is now composed permanently rather than never, so the window layout differs from F-1's shipped appearance from the first frame. Both are intended — F-1's own code comment says the panel was null only because there was no content to put in it, which is precisely what this spec supplies. MirrorStage gaining a Frozen enum entry is a source-compatible change inside the feature module, and the compiler enforces the new when branch rather than letting it be forgotten.

### knownRisksFromQA

| bugRef | description | watchFor | mitigation |
| --- | --- | --- | --- |
| app-shell-window-layout/validation — backtick test-name defect (fixed at commit a71d232) | A commonTest backtick test name containing a comma, apostrophe or colon compiled on the JVM and failed compileTestKotlinIosArm64 with 'Name contains illegal characters'. It broke the repo once during F-1 and is recorded in both .claude/rules/testing.md and presentation-feature-mirror/CLAUDE.md. | Any new test name in FrameGeometryTest, AnnotationFormatTest, PopoverCrowdingTest, MirrorViewModelAnnotationTest or the kit's new test file that uses backticks at all. | Plain camelCase names, zero backticks, and ./gradlew :presentation-feature-mirror:compileTestKotlinIosArm64 :presentation-core-ui:compileTestKotlinIosArm64 run before the PR rather than after. |
| android-device-discovery-mirroring/plan — draft specified an initKoin edit but omitted the build.gradle.kts edit | F-2's draft plan named a Koin wiring change without the matching Gradle dependency, which would not have compiled. The plan review caught it. | Any file this plan names that needs a module dependency or a DI registration the plan has not also named. | Both were checked against source for this spec and both are negative: presentation-feature-mirror/build.gradle.kts already declares presentationCoreUi, presentationCoreLocalisation, presentationCoreStyling, compose.material3 and kotlinx-coroutines-test, and FeatureConventionPlugin already wires kotlin-test into commonTest (AS-14); MirrorViewModel's constructor gains no parameter so no Koin or initKoin change exists (AS-15). Recorded here so the refine pass can re-check rather than re-derive. |
| rectify-poc Main.kt#L122-L126 — the density trap, documented in the POC's own source comment | Pointer events and composable layout are in raw screen pixels, but Modifier.offset/size take Dp which Compose multiplies by density again; without dividing back out, every drawn rectangle lands density-x further from the cursor than it should — 2x on this Retina host. | Any new `/ density`, `* density`, `.dp` or `.toPx()` arithmetic applied to frame or annotation coordinates outside FrameGeometry. Visually: a box that lands roughly twice as far from the cursor as the pointer travelled. | One pure conversion path kept internal (FR-26), AC-46's fixture reproducing the exact trap at density 2.0 with a non-integer frame scale, FrameGeometry built and tested before any UI exists, and a grep in the Definition of Done. |
| presentation-core-ui — AnnotationMark.kt line 98, Frozen state contradicts its own KDoc | AnnotationState.Frozen fills the mark with an opaque Theme.color.surface, while its KDoc justifies the state by saying the host dims everything else. An opaque plate and a visible annotated region are mutually exclusive — using it would hide the exact pixels the annotation points at, i.e. hide the bug being reported. | Any use of AnnotationState.Frozen in this spec's code, and any later reviewer reading its zero callers as an oversight. | Binding decision 7: use Pinned/Selected plus the new scrim; no kit change. Record in the PR that AnnotationState.Frozen is deliberately uncalled after this spec, so the absence is legible. |
| presentation-core-ui — CommentPopover.kt, fixed 18 dp tail inset with no placement logic | The popover draws its own tail at a fixed 18 dp inset and has no idea where its mark sits. A popover clamped away from its mark's left edge will point at the wrong place. | Annotations near the stage's right or bottom edge, where the 300 dp minimum popover width forces a clamp. | Accepted at demo fidelity and recorded as an open question. The eventual fix is a tailOffset parameter; it is not taken here because it is cosmetic and the gutter relocation already handles the crowded case. |
| presentation-core-ui — RectifyWindowShell.kt lines 87-92, SidePanelFrame.tabCounts is unreachable | SidePanelFrame accepts a tabCounts map, but RectifyWindowShell composes SidePanelFrame without forwarding one, so no caller can ever set a tab count. Found while verifying this spec's reuse claims; the brainstorm asserted the opposite. | An implementer assuming the count can be set from RectifyWindow alone and quietly dropping FR-28's count, or adding a second SidePanelFrame call site to work around it. | Step 5 adds the defaulted tabCounts parameter to RectifyWindowShell and forwards it. Called out explicitly in implementationPlan step 5 so it cannot be sliced away. |
| presentation-feature-mirror/CLAUDE.md — Orbit test-environment traps | Two environment facts the existing ViewModel tests depend on: Orbit runs its container event loop on Dispatchers.Default, so a test must pin it via Orbit.configureDefaults or nothing the reducer does is visible to advanceUntilIdle(); and since coroutines 1.11, advanceUntilIdle() ignores backgroundScope work, so the state subscription that triggers Orbit's lazy onCreate must be started with runCurrent(). | A new annotation test that builds its own ViewModel harness instead of using MirrorFakes and then mysteriously observes an unchanged initial state. | MirrorViewModelAnnotationTest extends MirrorFakes' mirrorViewModel() / useTestDispatcherForOrbit() / subscribe() helpers rather than re-solving either trap. |

### implementationPlan

| step | title | dependsOn | linkedAC | estimate | files | detail | verify |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | FrameGeometry (pure) + its tests — FIRST, before any UI exists | [] | ["AC-46", "AC-5"] | M | ["NEW presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/geometry/FrameGeometry.kt", "NEW presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/FrameGeometryTest.kt"] | internal data class FrameGeometry(framePx: Size, deviceSize: IntSize, density: Float) with toDevice/toFrame/toDp/isMeaningfulDrag and the MIN_DRAG_DEVICE_PX = 8 constant. No @Composable, no ViewModel import, no UI touched. Tests (all camelCase, no backticks): densityTwoDoesNotScaleTheStoredRect (AC-46/SC-8 — framePx 1426x3168 pointer px against a 1080x2400 device at density 2.0, hand-computed expectation, asserting the absence of a factor of 2), reversedDragNormalisesToPositiveRect, dragEndingInLetterboxClampsToDeviceBounds, nonIntegerScaleRoundsEachCornerIndependently, zeroFrameBoxReturnsEmptyRect, roundTripToFrameAndBackIsStable, subMinimumDragIsNotMeaningful. | ./gradlew :presentation-feature-mirror:desktopTest |
| 2 | AnnotationFormat (pure) — the testable half of the size label and the meta line | [1] | ["AC-5", "AC-7"] | S | ["NEW presentation-feature-mirror/.../core/annotation/AnnotationFormat.kt", "NEW presentation-feature-mirror/src/commonTest/.../AnnotationFormatTest.kt"] | internal metaLine(rect) producing CommentPopover's and AnnotationRow's documented `218×77 @ 1035,862` shape and internal sizeLabel(rect) producing AnnotationMark's `545 × 254` shape. Depends on DeviceRect, so land DeviceRect with step 3 or keep this step's functions on four Ints and adapt in step 3 — the slice may choose. Tests: metaLineMatchesTheDocumentedShape, sizeLabelMatchesTheMarkShape, zeroSizeRectFormatsWithoutCrashing. | ./gradlew :presentation-feature-mirror:desktopTest |
| 3 | MirrorContract additions — types, stage value, six intents | [2] | ["AC-6", "AC-48", "AC-49", "AC-50", "AC-51", "AC-52"] | M | ["CHANGED presentation-feature-mirror/.../source/mirror/MirrorContract.kt", "CHANGED presentation-feature-mirror/.../source/mirror/MirrorContent.kt (placeholder `MirrorStage.Frozen -> Unit` branch only)"] | Public DeviceRect, StagedAnnotation, FrozenFrame; MirrorUiState.frozen and the derived stagedCount; MirrorStage.Frozen with the stage getter ordered Picker > IosDeferred > Frozen > Streaming > NoSignal; six MirrorIntent cases. Compile-only. The when(state.stage) in MirrorStageContent is exhaustive with no else, so the compiler forces a branch — add a `-> Unit` placeholder here and fill it in step 7. Every new public declaration needs an explicit visibility modifier and return type (explicitApi). | ./gradlew :presentation-feature-mirror:compileKotlinDesktop |
| 4 | MirrorViewModel: frozenFrame StateFlow + six handlers + their tests | [3] | ["AC-6", "AC-8", "AC-47", "AC-48", "AC-49", "AC-50", "AC-51", "AC-52"] | L | ["CHANGED presentation-feature-mirror/.../source/mirror/MirrorViewModel.kt", "CHANGED presentation-feature-mirror/src/commonTest/.../MirrorFakes.kt (optional decodeFrame parameter + a size-distinguishing fake decoder)", "NEW presentation-feature-mirror/src/commonTest/.../MirrorViewModelAnnotationTest.kt"] | Add currentFrozenFrame/frozenFrame and the six handlers; extend selectPlatform and stopSession to clear both channels. sessionDeviceId and observeFrames are NOT edited — verify by diff that the flatMapLatest block is untouched. Tests (camelCase, no backticks): firstStageAnnotationFreezesTheFrame (AC-6), freezeUsesTheFrameHeldAtRelease (AC-47), secondDragAddsAnnotationWithoutRefreezing (AC-52/SC-9), stageAnnotationWithNoFrameIsDropped, commentCommitStoresTheText (AC-8), deleteDecrementsStagedCountAndRenumbers (AC-8/SC-10), deleteOfSelectedAnnotationClearsOrClampsSelection, selectAnnotationNullKeepsTheAnnotationAndTheCount (AC-50/SC-10), clearStagedEmptiesTheListAndKeepsTheFrameFrozen (AC-48), resumeLiveClearsTheFrozenFrameAndAnnotations (AC-51), dragAfterResumeLiveFreezesANewFrame (AC-51/SC-11), selectPlatformIosClearsTheFrozenFrame, sessionLostWhileFrozenKeepsTheAnnotations. | ./gradlew :presentation-feature-mirror:desktopTest && ./gradlew :presentation-feature-mirror:compileTestKotlinIosArm64 |
| 5 | Kit layer: AnnotationOverlay molecule + the two defaulted parameters + Styleguide | [1] | ["AC-7", "AC-49", "NFR-9"] | L | ["NEW presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/annotation/AnnotationOverlay.kt", "CHANGED presentation-core-ui/.../kit/molecule/mirror/MirrorFrameView.kt (+ overlay: @Composable BoxScope.() -> Unit = {})", "CHANGED presentation-core-ui/.../kit/organism/shell/RectifyWindowShell.kt (+ tabCounts: Map<ShellTab, Int> = emptyMap(), forwarded to SidePanelFrame)", "CHANGED presentation-core-ui/.../source/showcase/StyleguideScreen.kt"] | AnnotationOverlay: one Canvas doing clipPath(unionPath, ClipOp.Difference){ drawRect(ScrimFill) } guarded on a non-empty rect list, then one AnnotationMark per rect. Imports AnnotationMark (atom) only — never CommentPopover (organism). ScrimFill is a named private constant, deliberately dark in both themes, with the KDoc rationale CommentPopover and CounterBadge already set the precedent for. Build the union Path inside remember(rects). Light and dark @Previews. MirrorFrameView's overlay goes inside the inner aspect-fit Box (the one with .aspectRatio and the semantics block), as its last child, and must not disturb the existing contentDescription. RectifyWindowShell's tabCounts closes the verified gap at its lines 87-92 — FR-28's count is unreachable without it. All three parameters are defaulted and appended, so every existing preview, call site and test still compiles. | ./gradlew :presentation-core-ui:build (built alone — 3 GB daemon cap) |
| 6 | Live drag layer on the Mirror stage | [4, 5] | ["AC-5", "AC-6"] | L | ["NEW presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt (MirrorLiveStage half + the file-top named constants)", "CHANGED presentation-feature-mirror/.../source/mirror/MirrorHost.kt (collect frozenFrame; pass frozenFrame ?: frame — PUBLIC SIGNATURE UNCHANGED)", "CHANGED presentation-feature-mirror/.../source/mirror/MirrorContent.kt (Mirror branch delegates to MirrorLiveStage)"] | MirrorFrameView(frame, deviceName, overlay = { ... }). Inside the overlay: Modifier.onSizeChanged into a remembered framePx; a pointerInput/detectDragGestures layer with dragStart/dragCurrent in remember (NEVER in Orbit); one AnnotationMark(state = Drawing, sizeLabel = sizeLabel(deviceRect)) positioned via FrameGeometry.toFrame then toDp; a horizontal Ruler(totalPx = deviceWidth, extentPx = x-extent) at the top edge and the same Ruler rotated 270 degrees inside a fixed-size Box at the leading edge for the vertical guide — authoring a VerticalRuler is ruled out by Ruler's own KDoc. On release, emit StageAnnotation only when isMeaningfulDrag passes. Every invented value (ruler inset, gutter and control geometry) is a named file-top private constant. | ./gradlew :presentation-feature-mirror:build, then ./gradlew :desktop-application:run and drag on a live mirror at density 2.0 — the box must land under the cursor |
| 7 | Frozen stage: static frame, scrim, pinned marks, CommentPopover, Resume Live, FROZEN indicator | [6] | ["AC-7", "AC-8", "AC-51", "AC-52"] | L | ["CHANGED presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt (MirrorFrozenStage half)", "CHANGED presentation-feature-mirror/.../source/mirror/MirrorContent.kt (fill the Frozen branch; add the Frozen case to the private indicatorState() and indicatorLabel())"] | MirrorFrozenStage composes MirrorFrameView over the frozen bitmap with an overlay carrying AnnotationOverlay(dimmed = true, marks = Pinned, with the selected index as Selected), one CommentPopover for the selected mark (value, onValueChange -> CommentAnnotation, onClose -> DeleteAnnotation per FR-8, index, meta = metaLine(rect), saveHint, placeholder — all via stringResource), the drag layer still active so FR-31/AC-52 works on the frozen stage, and a Resume Live AppButton(size = AppButtonSize.Toolbar) at the stage's top-right over the scrim. Clamp the popover's x into the stage bounds; the fixed 18 dp tail inset will not follow, which is the accepted demo-fidelity limitation. indicatorState() gains `frozen != null -> StatusIndicatorState.Frozen` ahead of the Streaming check — the first production caller of that shipped-but-unused enum value. | ./gradlew :presentation-feature-mirror:build; demo SC-3 and SC-9 by hand |
| 8 | Focus and keyboard: autofocus, Enter to save, Escape to collapse | [7] | ["AC-7", "AC-8", "AC-50", "NFR-8"] | M | ["CHANGED presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt", "CONTINGENT CHANGE presentation-core-ui/.../kit/organism/annotation/CommentPopover.kt (+ focusRequester: FocusRequester? = null forwarded to the BasicTextField)"] | A FocusRequester plus Modifier.focusRequester().focusGroup() on the popover's root, requested in a LaunchedEffect keyed on the selected index, and onPreviewKeyEvent on that subtree handling Enter (commit, keep pinned) and Escape (SelectAnnotation(null), keep the annotation and the count) and consuming only those two. No global dispatch layer — handleShellShortcut matches only isMetaPressed and MirrorStage.Picker and MirrorStage.Frozen are mutually exclusive, so the conflict does not exist. CONTINGENCY: CommentPopover has no focus hook and its BasicTextField owns a private interactionSource; if parent-delegated focus does not reach the field, add the defaulted focusRequester parameter — budgeted here, not a surprise. | ./gradlew :presentation-core-ui:build && ./gradlew :presentation-feature-mirror:build; demo SC-10 by hand, including typing a digit and pressing Cmd+2 while the field has focus |
| 9 | Window wiring: stagedCounter, clearAction, the Notes tab, the tab count, Notes auto-select — then the FULL desktop suite | [7] | ["AC-48", "AC-49"] | L | ["NEW presentation-feature-mirror/.../source/mirror/MirrorNotesTab.kt", "CHANGED compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt"] | MirrorNotesTab: public composable, one AnnotationRow per annotation in index order with selected/onClick wired to SelectAnnotation, an empty-state line, and its own private @Preview. RectifyWindow: stagedCounter -> CounterBadge(mirror.stagedCount, Empty at 0 else Staged); clearAction -> AppButton(enabled = stagedCount > 0, onClick -> ClearStaged); the sidePanel val MOVES INSIDE the MirrorHost content lambda because it now needs mirror and onIntent, and becomes unconditionally non-null; handleShellShortcut's isPanelVisible becomes constant true; tabCounts = mapOf(ShellTab.Notes to stagedCount) passed through the parameter step 5 added; a LaunchedEffect on (stagedCount >= 1) calling shellState.selectTab(ShellTab.Notes) so the first annotation reveals the panel, deliberately not re-selecting on later annotations. Existing literal labels in this file stay literal. THIS IS THE COMMIT THAT MAKES Cmd+1/2/3 LIVE AND THE PANEL PERMANENT — run the full desktop suite here, not at the end, and note both changes in the PR. | ./gradlew :compose-application:build, then the full suite: ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest :data-device-api:desktopTest :data-device-impl:desktopTest :presentation-feature-mirror:desktopTest — all 160 pre-existing plus the new ones must be green |
| 10 | FR-9: popoverCrowding + the in-stage right-edge gutter + straight leader lines | [8, 9] | ["AC-9"] | M | ["NEW presentation-feature-mirror/.../core/annotation/PopoverCrowding.kt", "NEW presentation-feature-mirror/src/commonTest/.../PopoverCrowdingTest.kt", "CHANGED presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt"] | internal anyOverlap(rects: List<Rect>): Boolean — O(n^2) pairwise bounding-box test over nominal popover bounds constants, because measuring in order to decide whether to compose is a circular layout dependency needing SubcomposeLayout. When it returns true, stack read-only AnnotationPopovers in index order in a named-width column at the stage's right edge and draw one straight Canvas line from each mark to its popover — all in the overlay's own coordinate space, no cross-composable onGloballyPositioned. Demo fidelity: explicitly no 8 px elbow-stagger. Tests: nonOverlappingRectsDoNotCrowd, touchingEdgesDoNotCount, oneOverlappingPairCrowdsEverything, emptyAndSingleListsNeverCrowd. | ./gradlew :presentation-feature-mirror:desktopTest; demo SC-4 with three crowded annotations and attach a screenshot |
| 11 | Localisation keys, light/dark parity pass, and the final full regression | [10] | ["AC-5", "AC-7", "NFR-9", "NFR-1"] | M | ["CHANGED presentation-core-localisation/src/commonMain/composeResources/values/strings.xml", "CHANGED presentation-feature-mirror/.../source/mirror/MirrorAnnotationStage.kt and MirrorNotesTab.kt and MirrorContent.kt (swap any remaining inline copy for stringResource)"] | Add the seven mirror_annotation_* / mirror_action_resume_live / mirror_status_frozen keys and reference every one — this repo has previously accumulated unused keys, so grep each. Then the NFR-9 pass: screenshot the live drag stage, the frozen stage with one annotation, the crowded gutter and the Notes tab in both light and dark, confirm the scrim and the popovers are deliberately dark in both while every theme-following surface actually follows, and check contrast on the Notes rows. Confirm NFR-1 by watching the stats line keep reporting fps while frozen. Finally the full regression, run in two halves if the 3 GB daemon complains. | ./gradlew :presentation-core-localisation:build; the full desktopTest list from step 9; ./gradlew :presentation-core-ui:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64; ./gradlew :desktop-application:run for the final demo of SC-3/4/9/10/11 |

### testStrategy

#### unit

- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/FrameGeometryTest.kt — densityTwoDoesNotScaleTheStoredRect (AC-46/SC-8, the POC trap fixture at density 2.0 with a non-integer frame scale), reversedDragNormalisesToPositiveRect, dragEndingInLetterboxClampsToDeviceBounds, nonIntegerScaleRoundsEachCornerIndependently, zeroFrameBoxReturnsEmptyRect, roundTripToFrameAndBackIsStable, subMinimumDragIsNotMeaningful. The highest-value file in the spec: it is the only failure mode that is silent.
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/AnnotationFormatTest.kt — metaLineMatchesTheDocumentedShape, sizeLabelMatchesTheMarkShape, zeroSizeRectFormatsWithoutCrashing. This is the automatable half of AC-5's 'live pixel-size label' and AC-7's 'meta line'.
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PopoverCrowdingTest.kt — nonOverlappingRectsDoNotCrowd, touchingEdgesDoNotCount, oneOverlappingPairCrowdsEverything, emptyAndSingleListsNeverCrowd. The automatable half of AC-9.
- All test names are plain camelCase with no backticks at all: a backtick name containing a comma, apostrophe or colon compiles on the JVM and fails compileTestKotlinIosArm64, which already broke this repo once in F-1.

#### integration

- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelAnnotationTest.kt — the Orbit reducer suite, extending MirrorFakes' existing harness (Orbit.configureDefaults pinning + the runCurrent subscription quirk) rather than re-solving either trap. Thirteen tests: firstStageAnnotationFreezesTheFrame (AC-6), freezeUsesTheFrameHeldAtRelease (AC-47), secondDragAddsAnnotationWithoutRefreezing (AC-52/SC-9), stageAnnotationWithNoFrameIsDropped, commentCommitStoresTheText (AC-8), deleteDecrementsStagedCountAndRenumbers (AC-8/SC-10), deleteOfSelectedAnnotationClearsOrClampsSelection, selectAnnotationNullKeepsTheAnnotationAndTheCount (AC-50/SC-10), clearStagedEmptiesTheListAndKeepsTheFrameFrozen (AC-48), resumeLiveClearsTheFrozenFrameAndAnnotations (AC-51), dragAfterResumeLiveFreezesANewFrame (AC-51/SC-11), selectPlatformIosClearsTheFrozenFrame, sessionLostWhileFrozenKeepsTheAnnotations.
- MirrorFakes.kt gains an optional decodeFrame parameter on mirrorViewModel() plus a fake decoder that maps byte content to ImageBitmap(width, height) of distinguishing sizes, so AC-47 can assert frozenFrame.value?.width rather than pixel content. If Skiko will not load in a headless desktopTest JVM, the fallback is to assert on FrozenFrame.deviceSize, which the reducer already stores — the assertion moves with zero production change.
- No new integration test touches adb, a device, a process or the filesystem. This spec adds no data-layer or domain-layer code at all.

#### ui

- NONE, and that is stated plainly rather than papered over. This repo has ZERO Compose UI tests among its 160 green, no compose-ui-test dependency in the catalog, and introducing one is explicitly out of scope — it is a cross-cutting decision that would land a test dependency in every module through the convention plugins and deserves its own spec.
- In place of rendered assertions: light and dark @Previews on AnnotationOverlay (new molecule), on MirrorNotesTab, and on the live and frozen stage composables, per .claude/rules/compose-preview-convention.md; an AnnotationOverlay section added to StyleguideScreen for visual QA; and a scripted manual demo via ./gradlew :desktop-application:run walking SC-3, SC-4, SC-9, SC-10 and SC-11 at host density 2.0, with screenshots attached to the PR.
- FOLLOW-UP TICKET TO FILE: 'Adopt compose-ui-test (desktop) and add rendering assertions for AC-5 and AC-7.' This is the honest resolution of the PRD-inherited mismatch — AC-5 and AC-7 are marked verificationMethod: test and will not have rendered coverage in this deliverable.

#### linkedScenarios

- SC-3
- SC-4
- SC-8
- SC-9
- SC-10
- SC-11

#### coverageGoal

Every pure function in this spec (FrameGeometry's four members, metaLine, sizeLabel, anyOverlap) is covered, and every one of the six new intents has at least one reducer test — roughly 25-30 new tests. Coverage is NOT pursued in the UI layer because no harness exists to pursue it with. Of the 12 ACs: 9 are automated (AC-6, AC-8, AC-46, AC-47, AC-48, AC-49, AC-50, AC-51, AC-52 — AC-51 is marked demo but its reducer half is tested anyway), and 3 are partially automated with the remainder verified by preview and demo (AC-5, AC-7, AC-9). The pass bar is the full per-module desktopTest list green with the 160 pre-existing tests intact, plus :presentation-core-ui:compileTestKotlinIosArm64 and :presentation-feature-mirror:compileTestKotlinIosArm64 green.

#### commands

- Per-step unit/reducer runs: ./gradlew :presentation-feature-mirror:desktopTest
- Kit build (alone, 3 GB daemon cap): ./gradlew :presentation-core-ui:build
- Feature build: ./gradlew :presentation-feature-mirror:build
- Window build: ./gradlew :compose-application:build
- FULL REGRESSION (the exact command, run after step 9 and again at step 11; split into two halves if the daemon complains): ./gradlew :presentation-core-styling:desktopTest :presentation-core-ui:desktopTest :data-preference-impl:desktopTest :data-repository-impl:desktopTest :data-device-api:desktopTest :data-device-impl:desktopTest :presentation-feature-mirror:desktopTest
- iOS test-compile trap check (before the PR, not after): ./gradlew :presentation-core-ui:compileTestKotlinIosArm64 :presentation-feature-mirror:compileTestKotlinIosArm64
- Manual demo: ./gradlew :desktop-application:run

### traceabilityMatrix

| ac | kind | functional | components | steps | tests |
| --- | --- | --- | --- | --- | --- |
| AC-5 | AC | ["FR-6", "FR-26"] | ["FrameGeometry", "AnnotationFormat", "MirrorAnnotationStage (MirrorLiveStage)", "AnnotationMark", "Ruler", "MirrorFrameView.overlay"] | [1, 2, 5, 6] | ["FrameGeometryTest (where the box lands)", "AnnotationFormatTest.sizeLabelMatchesTheMarkShape (what the label says)", "SC-8", "INSPECTION: light/dark @Preview of the Drawing-state mark and the Ruler", "DEMO: SC-3 drag on a live mirror at density 2.0 — no Compose UI-test harness exists, so the dashed border and the live tick guides are NOT asserted"] |
| AC-6 | AC | ["FR-7"] | ["MirrorViewModel.StageAnnotation handler", "MirrorContract (FrozenFrame, MirrorStage.Frozen)", "MirrorAnnotationStage"] | [3, 4, 6] | ["MirrorViewModelAnnotationTest.firstStageAnnotationFreezesTheFrame", "SC-3"] |
| AC-7 | AC | ["FR-8", "FR-30"] | ["AnnotationOverlay (scrim)", "CommentPopover", "AnnotationMark (Pinned/Selected)", "AnnotationFormat.metaLine", "MirrorAnnotationStage (MirrorFrozenStage)"] | [2, 5, 7, 8] | ["AnnotationFormatTest.metaLineMatchesTheDocumentedShape", "MirrorViewModelAnnotationTest.firstStageAnnotationFreezesTheFrame (stage == Frozen with selectedIndex set, so the popover is composed)", "INSPECTION: light/dark @Preview of AnnotationOverlay and the frozen stage", "DEMO: SC-3 — the dim, the autofocus and the delete affordance are NOT asserted; no UI-test harness exists"] |
| AC-8 | AC | ["FR-8", "FR-29"] | ["MirrorViewModel.CommentAnnotation / DeleteAnnotation handlers", "CommentPopover onClose repurposed as delete", "onPreviewKeyEvent Enter handling"] | [4, 7, 8] | ["MirrorViewModelAnnotationTest.commentCommitStoresTheText", "MirrorViewModelAnnotationTest.deleteDecrementsStagedCountAndRenumbers", "MirrorViewModelAnnotationTest.deleteOfSelectedAnnotationClearsOrClampsSelection", "SC-10"] |
| AC-9 | AC | ["FR-9"] | ["popoverCrowding", "AnnotationPopover", "MirrorAnnotationStage gutter column + Canvas leader lines"] | [10] | ["PopoverCrowdingTest (all four)", "DEMO: SC-4 with three crowded annotations, screenshot attached — AC-9 is demo-verified by design, at minimum fidelity with no elbow-stagger"] |
| AC-46 | AC | ["FR-26"] | ["FrameGeometry.toDevice"] | [1] | ["FrameGeometryTest.densityTwoDoesNotScaleTheStoredRect", "FrameGeometryTest.nonIntegerScaleRoundsEachCornerIndependently", "SC-8"] |
| AC-47 | AC | ["FR-7"] | ["MirrorViewModel.StageAnnotation handler reading currentFrame.value once at release", "MirrorFakes fake decoder"] | [4] | ["MirrorViewModelAnnotationTest.freezeUsesTheFrameHeldAtRelease (fallback assertion on FrozenFrame.deviceSize if Skiko will not load headless)"] |
| AC-48 | AC | ["FR-27"] | ["MirrorViewModel.ClearStaged handler", "MirrorUiState.stagedCount", "CounterBadge", "AppButton (clearAction)", "RectifyWindow.kt"] | [4, 9] | ["MirrorViewModelAnnotationTest.clearStagedEmptiesTheListAndKeepsTheFrameFrozen", "INSPECTION: the Empty-at-0-else-Staged mapping is a one-expression private helper in RectifyWindow.kt; compose-application has no test source set", "DEMO: stage three annotations, click Clear, confirm the badge returns to 0/Empty and the stage stays frozen"] |
| AC-49 | AC | ["FR-28"] | ["MirrorNotesTab", "AnnotationRow", "MirrorViewModel.SelectAnnotation handler", "RectifyWindowShell.tabCounts", "SidePanelFrame"] | [4, 5, 9] | ["MirrorViewModelAnnotationTest.selectAnnotationNullKeepsTheAnnotationAndTheCount", "MirrorViewModelAnnotationTest.deleteDecrementsStagedCountAndRenumbers (the index-order invariant)", "INSPECTION: MirrorNotesTab @Preview, one row per annotation in index order", "DEMO: click a row, confirm the corresponding mark turns Selected on the frame"] |
| AC-50 | AC | ["FR-29"] | ["onPreviewKeyEvent on the popover focus subtree", "MirrorViewModel.SelectAnnotation(null)", "handleShellShortcut (unchanged)", "shortcutToTab (unchanged)"] | [4, 8] | ["MirrorViewModelAnnotationTest.selectAnnotationNullKeepsTheAnnotationAndTheCount", "ShortcutToTabTest (pre-existing, covers the meta-only mapping)", "INSPECTION: handleShellShortcut matches only isMetaPressed, and MirrorStage.Picker and MirrorStage.Frozen are mutually exclusive enum values", "DEMO: SC-10 plus typing a digit and pressing Cmd+2 while the comment field has focus"] |
| AC-51 | AC | ["FR-30"] | ["MirrorViewModel.ResumeLive handler", "AppButton (Resume Live)", "MirrorAnnotationStage (MirrorFrozenStage)"] | [4, 7] | ["MirrorViewModelAnnotationTest.resumeLiveClearsTheFrozenFrameAndAnnotations", "MirrorViewModelAnnotationTest.dragAfterResumeLiveFreezesANewFrame", "DEMO: SC-11 — the perceptual half, that live frames resume with no reconnection stall, is demo-only"] |
| AC-52 | AC | ["FR-31"] | ["MirrorViewModel.StageAnnotation handler (frozenFrame.value ?: currentFrame.value)", "MirrorAnnotationStage drag layer active on the frozen stage"] | [4, 7] | ["MirrorViewModelAnnotationTest.secondDragAddsAnnotationWithoutRefreezing", "SC-9"] |
| FR-6 | FR | ["FR-6"] | ["MirrorAnnotationStage (MirrorLiveStage)", "AnnotationMark", "Ruler", "MirrorFrameView.overlay", "AnnotationFormat.sizeLabel"] | [2, 5, 6] | ["AC-5", "AnnotationFormatTest", "FrameGeometryTest", "DEMO: SC-3"] |
| FR-7 | FR | ["FR-7"] | ["MirrorViewModel.StageAnnotation handler", "MirrorContract.FrozenFrame"] | [3, 4] | ["AC-6", "AC-47", "MirrorViewModelAnnotationTest.firstStageAnnotationFreezesTheFrame", "MirrorViewModelAnnotationTest.freezeUsesTheFrameHeldAtRelease"] |
| FR-8 | FR | ["FR-8"] | ["AnnotationOverlay", "CommentPopover", "AnnotationFormat.metaLine"] | [2, 5, 7] | ["AC-7", "AC-8", "AnnotationFormatTest", "INSPECTION: AnnotationOverlay light/dark @Preview", "DEMO: SC-3"] |
| FR-9 | FR | ["FR-9"] | ["popoverCrowding", "AnnotationPopover", "gutter column + leader lines"] | [10] | ["AC-9", "PopoverCrowdingTest", "DEMO: SC-4"] |
| FR-26 | FR | ["FR-26"] | ["FrameGeometry"] | [1] | ["AC-46", "FrameGeometryTest (all seven)", "SC-8", "INSPECTION: FrameGeometry is internal with no @Composable and no ViewModel import; grep finds no density arithmetic on frame coordinates anywhere else"] |
| FR-27 | FR | ["FR-27"] | ["CounterBadge", "AppButton (clearAction)", "MirrorViewModel.ClearStaged", "RectifyWindow.kt"] | [4, 9] | ["AC-48", "MirrorViewModelAnnotationTest.clearStagedEmptiesTheListAndKeepsTheFrameFrozen", "INSPECTION: the RectifyWindow.kt call site"] |
| FR-28 | FR | ["FR-28"] | ["MirrorNotesTab", "AnnotationRow", "RectifyWindowShell.tabCounts", "SidePanelFrame"] | [5, 9] | ["AC-49", "INSPECTION: MirrorNotesTab @Preview and the RectifyWindowShell tabCounts forwarding that step 5 adds", "DEMO: Notes tab count matches the toolbar badge"] |
| FR-29 | FR | ["FR-29"] | ["onPreviewKeyEvent on the popover focus subtree", "CommentPopover onClose as delete"] | [8] | ["AC-8", "AC-50", "ShortcutToTabTest (pre-existing)", "DEMO: SC-10"] |
| FR-30 | FR | ["FR-30"] | ["MirrorViewModel.ResumeLive", "MirrorAnnotationStage (MirrorFrozenStage)", "StatusIndicatorState.Frozen", "AnnotationOverlay scrim"] | [4, 7] | ["AC-51", "MirrorViewModelAnnotationTest.resumeLiveClearsTheFrozenFrameAndAnnotations", "INSPECTION: diff confirms sessionDeviceId and the flatMapLatest gate are untouched", "DEMO: SC-11, no reconnection stall; the stats line keeps reporting fps while frozen"] |
| FR-31 | FR | ["FR-31"] | ["MirrorViewModel.StageAnnotation handler", "drag layer active on the frozen stage"] | [4, 7] | ["AC-52", "MirrorViewModelAnnotationTest.secondDragAddsAnnotationWithoutRefreezing", "SC-9"] |
| NFR-1 | NFR | ["FR-30"] | ["MirrorViewModel (capture gate untouched)", "AnnotationOverlay (no saveLayer)", "drag offsets in remember"] | [4, 5, 6, 11] | ["INSPECTION: the diff touches neither sessionDeviceId nor observeFrames, so the capture/decode/transport path is bit-identical", "INSPECTION: performanceBudget — one path fill, no saveLayer, zero reducer passes during a drag", "DEMO: the stats line's fps reading is unchanged before, during and after a freeze"] |
| NFR-8 | NFR | ["FR-29"] | ["onPreviewKeyEvent", "FocusRequester autofocus", "CommentPopover (contingent focusRequester parameter)"] | [8] | ["AC-50", "A11Y-1", "A11Y-2", "A11Y-3", "A11Y-4", "DEMO: SC-10 walked by keyboard only, from creating an annotation to typing, saving, escaping and deleting"] |
| NFR-9 | NFR | ["FR-8", "FR-30"] | ["AnnotationOverlay ScrimFill", "AnnotationMark (Theme.color.annotate)", "CommentPopover / AnnotationPopover (deliberately dark in both themes)", "MirrorNotesTab", "StyleguideScreen"] | [5, 11] | ["A11Y-7", "A11Y-8", "INSPECTION: light and dark @Previews on every new composable; the AnnotationOverlay StyleguideScreen section", "INSPECTION: grep finds no literal Color(0x…), no raw .dp at a call site and no inline TextStyle in the new code", "DEMO: step 11's light/dark parity pass over all four new surfaces"] |
| SC-3 | SC | ["FR-6", "FR-7", "FR-8"] | ["MirrorLiveStage", "MirrorFrozenStage", "CommentPopover"] | [6, 7, 8] | ["MirrorViewModelAnnotationTest.firstStageAnnotationFreezesTheFrame + commentCommitStoresTheText cover the state transitions", "DEMO: the end-to-end gesture, autofocus and Enter-to-save"] |
| SC-4 | SC | ["FR-9"] | ["popoverCrowding", "gutter column", "AnnotationPopover"] | [10] | ["PopoverCrowdingTest.oneOverlappingPairCrowdsEverything", "DEMO: three crowded annotations, screenshot attached. NOTE: F-1 bound different content to SC-3/SC-4; these ids are spec-scoped, not globally unique"] |
| SC-8 | SC | ["FR-26"] | ["FrameGeometry.toDevice"] | [1] | ["FrameGeometryTest.densityTwoDoesNotScaleTheStoredRect", "FrameGeometryTest.nonIntegerScaleRoundsEachCornerIndependently"] |
| SC-9 | SC | ["FR-31"] | ["MirrorViewModel.StageAnnotation handler", "MirrorFrozenStage drag layer"] | [4, 7] | ["MirrorViewModelAnnotationTest.secondDragAddsAnnotationWithoutRefreezing", "DEMO: two drags on one frozen frame, badges 1 and 2, one scrim with two holes"] |
| SC-10 | SC | ["FR-8", "FR-29"] | ["onPreviewKeyEvent", "CommentPopover onClose as delete", "MirrorNotesTab row selection"] | [4, 8, 9] | ["MirrorViewModelAnnotationTest.selectAnnotationNullKeepsTheAnnotationAndTheCount", "MirrorViewModelAnnotationTest.deleteDecrementsStagedCountAndRenumbers", "DEMO: Escape then reselect-from-the-Notes-row then delete"] |
| SC-11 | SC | ["FR-30"] | ["MirrorViewModel.ResumeLive", "Resume Live AppButton"] | [4, 7] | ["MirrorViewModelAnnotationTest.resumeLiveClearsTheFrozenFrameAndAnnotations", "MirrorViewModelAnnotationTest.dragAfterResumeLiveFreezesANewFrame", "DEMO: live pixels resume with no reconnection stall, then a second freeze"] |

### openQuestions

- Whether to pause the capture/transport loop while frozen (power/perf optimisation) is left for later — FR-30 assumes it keeps running. If it bites, the fix is an isFrozen flag folded into the transport's adaptive interval, not a session teardown.
- The gutter/leader-line 8 px elbow-stagger visual polish named in AnnotationPopover's KDoc is not implemented in v1 — a follow-up design/visual-QA pass should revisit it.
- Whether a frozen frame should ever be allowed to persist annotations across a Resume-Live -> freeze-again cycle (multi-frame annotation sets) is explicitly deferred to F-8 (history).
- Vertical ruler for FR-6's 'ruler/tick guides': this plan takes the recommendation — horizontal plus vertical via the existing Ruler rotated 270 degrees in a fixed-size Box, as the component's own KDoc anticipates, rather than authoring a second VerticalRuler. The rotation wrapper does not exist yet and is built in step 6; if rotation produces an unacceptable tick-label orientation, the fallback is horizontal-only, which still satisfies FR-6's wording.
- Where the Resume Live control sits: this plan takes the recommendation — a stage top-right overlay AppButton over the scrim, because the toolbar's primaryAction is reserved for F-7's 'Resolve & Send' and the status bar is only 28 dp tall. The geometry constant is flagged for design review.
- Minimum drag size before an annotation is created: this plan takes the recommendation — a named MIN_DRAG_DEVICE_PX constant at 8 device px in both axes. The exact value is a one-line tune and is flagged for review after the first real session.
- Does the Notes tab auto-activate on first freeze: this plan takes the recommendation — auto-select Notes on the FIRST annotation of a frozen frame only, never on subsequent ones, so a user who deliberately switched to Payload is not fought. Implemented in RectifyWindow because ShellState.activeTab has a private setter.
- No design comp exists for ANY part of this feature — the scrim alpha, the Resume Live placement and geometry, the ruler placement, the gutter width and the leader-line routing are all invented. Every one ships as a named private constant so the later design conversation is a one-line change each (precedent: F-1's stage dot grid).
- CommentPopover's X affordance means DELETE here (FR-8) while Escape means COLLAPSE (FR-29) — two visually similar dismissals with different destructiveness and no confirm. No design input exists on how to visually differentiate them; shipped as-is and flagged.
- CommentPopover's tail has a fixed 18 dp inset and no placement logic, so a popover clamped away from its mark's edge will point at the wrong place. Accepted at demo fidelity — a tailOffset parameter is the eventual fix.
- StagedAnnotation lives in the presentation contract for now (one consumer, no serialization need); lift it to domain-core only when F-7 needs it in an MCP payload — that is a package move, not a redesign.
- NEW (plan): making the side panel permanently composed changes the window's appearance from the first frame and activates Cmd+1/2/3 for the first time. This plan chooses permanent over session-gated because the alternative needs the mirror state inside the Window's onPreviewKeyEvent, where it is not available without a composition-time side effect. If a session-gated panel is wanted, it is a separate small refactor of how isPanelVisible is hoisted.
- NEW (plan): RectifyWindow.kt's existing literal labels (Clear, Resolve & Send, No device, staged) are left unlocalised, consistent with F-1's choice for that file. Converting them is a separate small ticket, not this spec's churn.

### readyForBacklog

false
