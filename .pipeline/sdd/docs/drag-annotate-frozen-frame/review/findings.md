# drag-annotate-frozen-frame — findings

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | findings |
| status | done |
| producedBy | sdd-build-correctness-review |
| schemaVersion | 1 |
| createdAt | 2026-10-15T05:49:23Z |
| updatedAt | 2026-10-15T05:49:23Z |

## Gate

### type

auto

### approvedBy

sdd-build-correctness-review

### approved

true

### at

2026-10-15T05:49:23Z

## Provenance

### selfHash

e6856a0ac075d49b4a3b2451029542103ec2b6df7d5df8f1838a0570806794da

### inputs

| artifact | hash |
| --- | --- |
| plan/plan.json | b56b93425a8c4eedc0d8b7ad764b2e5531cbad86920134eae05f4c93ea46a492 |
| validation/verdict.json | b67aa2a7aa05de36010a41336c3343234dfeb0d5306f9d7cda26a585049e900e |

## Data

### findings

| id | dimension | severity | file | line | ruleOrACRef | detail | verified |
| --- | --- | --- | --- | --- | --- | --- | --- |
| COR-1 | correctness | major | presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelAnnotationTest.kt | 279 | errorState:error (MirrorSession.Lost while frozen keeps the frozen frame and its annotations) | sessionLostWhileFrozenKeepsTheAnnotations is vacuous: FakeStartMirroring throws `failure` from `.onStart { ... }` (MirrorFakes.kt:88-92), which already ran when the session was opened inside streamingWithFrame(). Setting `mirroring.failure` afterwards and calling pushFrame() emits a normal frame into the already-collected MutableSharedFlow — nothing throws, `.catch { onSessionLost(device) }` never runs, and the session stays Streaming. Both assertions (stagedCount == 2, stage == Frozen) hold even if the loss path is deleted outright, so the one errorState the test names is unverified. The test would catch no mutation of onSessionLost. Fix: assert `MirrorSession.Lost(PHONE)` was reached (and give the fake a way to fail an already-running collection, e.g. a failing emission), which also makes the surviving-annotations claim meaningful. | false |
| COR-2 | correctness | minor | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/annotation/PopoverCrowding.kt | 60 | AC-9 / SC-4 | The crowding predicate runs on UNCLAMPED nominal rects, but placement clamps: AnchoredComments (MirrorAnnotationStage.kt:373-375) coerces x into [0, width-280] and y into [0, height-36]. Two marks stacked low on the frame (e.g. mark bottoms at 580 dp and 700 dp in a 600 dp-tall frame box, same left edge) produce nominal popover tops 120 dp apart — anyOverlap == false, so both stay anchored — yet both clamp to the identical maxY and are then drawn exactly on top of each other. The same holds against maxX at the right edge. The gutter is precisely the escape hatch for that collision and it is never taken. Cheapest fix: clamp inside nominalPopoverRects with the same two expressions the stage uses. | false |
| COR-3 | correctness | minor | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/annotation/PopoverCrowding.kt | 27 | AC-9 | NOMINAL_POPOVER_HEIGHT_DP (36 dp, one line of AnnotationPopover) is applied to EVERY annotation including the selected one, whose real plate is CommentPopover — a text field plus meta line plus save hint, several times taller than 36 dp. The documented tolerance is 'a rectangle a few dp too wide', not a 2-3x height underestimate for one member of the set. Result: a read-only popover anchored 40-80 dp below the selected mark is judged uncrowded and is then drawn underneath the editable popover. Model the selected annotation with the CommentPopover footprint (one extra constant) rather than the read-only one. | false |
| COR-4 | correctness | minor | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt | 412 | AC-9 / errorState:edge (crowded annotations) | AnnotationGutter pitches rows at `inset + (GutterRowHeight + inset) * position` and then `.coerceAtMost(lastTop)`. Once the column runs out of height (62 dp pitch, so ~9 rows in a 600 dp frame box) every further row is pinned to the SAME lastTop and the rows are drawn exactly on top of one another, with their leader lines converging on one point — the unreadable state the gutter exists to prevent, silently. There is no scroll, no cap and no overflow indicator. At minimum compress the pitch (or drop the overflow into the Notes tab, which already lists everything) instead of stacking identical offsets. | false |
| COR-5 | correctness | minor | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 298 | AC-8 / orbit-mvi.md | All five annotation handlers (stageAnnotation:265, selectAnnotation:277, commentAnnotation:282, deleteAnnotation:298, clearStaged:313) read `state.frozen` OUTSIDE `reduce` and then write `frozen.copy(...)` INSIDE it. Orbit launches each intent in its own coroutine, so two overlapping intents both capture the same pre-image and the second reduce discards the first one's change — e.g. a keystroke (CommentAnnotation fires per character) racing a DeleteAnnotation resurrects the deleted annotation, or a Clear racing a keystroke un-clears the list. The handlers have no suspension point between read and reduce so the window is narrow, but it is free to close: move the read inside `reduce { }` (`state.frozen?.let { ... } ?: state`). Note the renumbering logic itself is correct — following the selected object through `kept.indexOf(annotation) + 1` matches `mapIndexed { position -> position + 1 }` exactly, including the deleted-selected -> null case, and deleteOfSelectedAnnotationClearsOrClampsSelection genuinely exercises both branches. | false |
| COR-6 | correctness | nit | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt | 202 | errorState:loading (frame box 0 on first composition) | MirrorLiveStage and MirrorFrozenStage are separate branches of MirrorStageContent's `when`, so the release that freezes the frame DISPOSES the live stage's subtree and mounts a fresh one: `framePx` goes back to Size.Zero and FrameGeometry is degenerate until onSizeChanged lands. For that first pass every mark converts to Rect.Zero and the scrim covers the whole frame, so the transition can flash marks collapsed at the top-left corner. The degenerate guard behaves exactly as the plan's loading errorState requires (no divide-by-zero), this is only the cosmetic consequence of remounting. Hoisting `framePx` above the live/frozen branch, or giving both branches one call site, removes it. | false |
| COR-7 | correctness | nit | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/geometry/FrameGeometry.kt | 105 | AC-46 | toDp is the only member with no degenerate-input guard: toDevice checks isDegenerate, toFrame checks isDegenerate AND deviceSize > 0, toDp divides by `density` unchecked and would emit Infinity/NaN dp at density 0. LocalDensity never reports 0 today, so this is hygiene, not a live defect — but the type is otherwise exhaustively defensive and a reader will assume the omission is meaningful. | false |
| COR-8 | correctness | nit | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt | 362 | AC-7 | `Dp.Hairline` is used as the lower bound of a positional clamp in four places (362-363, 373-374, 409-410, 504-505, 514-515). Hairline is a stroke-width idiom meaning 'thinnest drawable line', not 'coordinate zero'; `0.dp` says what is meant. It happens to be Dp(0f) so behaviour is correct, and the coerceAtLeast keeps coerceIn's min <= max precondition satisfied — this is purely about reading the intent. | false |
| CNV-1 | conventions | nit | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorAnnotationStage.kt | 113 | .claude/rules/design-tokens.md | GutterRowHeight (54.dp) and POPOVER_TAIL_GAP_DP (6 dp, a spacing value) are inlined dimensions in a feature module rather than Theme tokens. They are named, private, documented against the composition they describe and have no existing Theme.spacing equivalent (6 dp sits between spacingXS and spacingS), so this is the acceptable form of the deviation — but it is a deviation, and the 42% scrim alpha in AnnotationOverlay.kt:36 is the same shape. If any of the three gets a sibling, promote them to ThemeSpacing/ThemeColor instead of adding a fourth constant. Verified clean otherwise: zero literal Color(0x…) added outside the waived CommentPopover, atom -> molecule -> organism layering intact (AnnotationOverlay imports only atoms; MirrorFrameView's new overlay slot is layer-neutral), explicitApi satisfied, zero build.gradle.kts and zero libs.versions.toml edits across all four batches, and every new commonTest backtick-free name compiles for iosArm64. | false |
| ARC-1 | architecture | nit | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/mirror/MirrorFrameView.kt | 42 | presentation-feature-mirror/CLAUDE.md#deviation-2 | The state split is sound — MirrorUiState carries IntSize + annotations only, both bitmaps live on StateFlows outside Orbit, MirrorHost collapses them with `frozenFrame ?: frame` so no call site widened, and no reduced-state path can ever hold an ImageBitmap. Only doc drift: MirrorFrameView's new `overlay` KDoc says 'annotation marks and their scrim', while the stage also passes the comment popovers, the gutter canvas and the Resume Live button through that slot. | false |
