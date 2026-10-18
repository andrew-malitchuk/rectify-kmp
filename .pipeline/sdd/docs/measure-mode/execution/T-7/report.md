# measure-mode — T-7

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-7 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-16T23:03:10Z |
| updatedAt | 2026-10-16T23:03:10Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-16T23:03:10Z

## Provenance

### selfHash

4d392f3585bb521cb12ea9a42ca1fd80f553b5296b99e3525e877b41eaead6b4

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-5/report.json | 5dacded2f743c5dd5ed781cc67fe1b56412e100b302785855e502b942dbf5344 |

## Data

### taskId

T-7

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Findings.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureFormat.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/annotation/AnnotationFormat.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Measure.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

#### followUpTestsAdded

| file | cases | redProof | why |
| --- | --- | --- | --- |
| presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MeasureThresholdUnitsTest.kt | ["topMisalignmentFiresOnADeltaOverTenPxThatIsUnderTenDpOnceConverted", "leftMisalignmentFiresOnADeltaOverTwentyFourPxThatIsUnderTwentyFourDpOnceConverted", "topMisalignmentStillStopsAtTheBoundWhenTheConvertedDeltaExceedsIt", "aDriftTooSmallToShowAsANonZeroFigureIsTreatedAsAlignedRatherThanReportedAsZero", "withUnknownDensityTheMisalignmentBoundsApplyToPxAndTheMessageSaysSo", "withUnknownDensityAPxDeltaOverTheBoundIsStillOverTheBound", "theTouchTargetBoundIsReadInDpAndNotInPx", "theTouchTargetFloorIsInclusiveAtNonBaselineDensityToo"] | Reverted displayDelta to the old raw-px expression and re-ran: 3 of the 8 FAILED (topMisalignmentFires..., leftMisalignmentFires..., aDriftTooSmallToShow...), then restored and re-ran green. The probe was deleted afterwards. | Every pre-existing measure test runs at densityDpi 160 where px == dp, so no frozen assertion could see the unit bug. These cases all run at 420 dpi and pick deltas where the px and dp readings disagree about which side of the bound they fall on. |

#### followUpNote

No existing assertion was weakened, changed or deleted; the twelve frozen files are untouched.

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :presentation-feature-mirror:compileTestKotlinIosArm64

#### passed

true

#### output

```
> Task :presentation-feature-mirror:compileKotlinDesktop
> Task :presentation-feature-mirror:compileTestKotlinDesktop
> Task :presentation-feature-mirror:desktopTest
> Task :presentation-feature-mirror:compileTestKotlinIosArm64
BUILD SUCCESSFUL in 1s
-- desktopTest XML results (--rerun-tasks), T-7's three frozen files plus the new one --
FindingsTest[desktop]          tests="11" skipped="0" failures="0" errors="0"
GuideSuppressionTest[desktop]  tests="4" skipped="0" failures="0" errors="0"
MeasureFormatTest[desktop]     tests="12" skipped="0" failures="0" errors="0"
MeasureThresholdUnitsTest[desktop] NEW tests="8" skipped="0" failures="0" errors="0"
-- module-wide totals (all 29 classes) --
tests=169 skipped=0 failures=0 errors=0
```

### implementation

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Findings.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureFormat.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/annotation/AnnotationFormat.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Measure.kt

#### summary

Findings.kt: FindingKind, Finding(kind, message) and findingOf as one ordered when with a single return value, so the never-two-findings invariant (AC-64) holds by construction with no dedup step. Top misalignment compares the target's top against the LEFT neighbour's and requires a non-zero delta within 10px; left misalignment is the same shape on the TOP neighbour's left edge within 24px; touch target needs a tappable node, a known density and a converted height under 32. A missing neighbour and a zero delta collapse into the same 0 sentinel because both report nothing, which removes a nullable-delta branch. findingOf returns the kind, not just the string, because the rail is drawn for TopMisalignment only. An unknown density silences the touch-target branch outright rather than comparing raw px against a dp threshold, which would fire on every node of a 420dpi device. MeasureFormat.kt: sizeLabel/deltaLabel/plainValueLabel/positionLabel, the three verbatim finding strings (U+00B7 separator, the neighbour's own id, 'min touch 48' alongside the 32 firing threshold), noteLine (the U+2325 glyph, the size row unlabelled, the finding appended as a final segment) and replaceNoteLine (drop any glyph-prefixed line, keep the user's prose, no leading newline on an empty comment). guideNumeral lives here and is the FR-39 suppression rule: it compares displayValueOf (post-conversion, pre-rounding) against 1.0, so a 2px gap at 420dpi (0.76dp) is absent rather than a rounded-up 1dp, while the panel's own value row for that side still prints. AnnotationFormat's TIMES promoted from private to internal and reused, so there is exactly one U+00D7 constant in the module. Nothing was written for node-id derivation: HierarchyNodeModel.id already carries the data layer's four-step chain including the [n] collision index, so T-7's backlog mention of it is already satisfied upstream. FOLLOW-UP CORRECTION 1 (threshold units): the 10 and 24 misalignment bounds were comparing RAW DEVICE PIXELS against numbers the design means in its own dp-like single space (frame 296x640), which on T-1's 420 dpi Pixel 8 made the top bound +/-3.8dp -- 2.6x stricter than designed -- so the finding would essentially never fire. All three bounds now compare the display unit actually in use, via a new internal displayDelta(fromPx, toPx, densityDpi) = toDisplayUnit(abs(delta)).first. Conversion comes FIRST, consistent with the guide-suppression extension (FR-39) and for the same reason. displayDelta is deliberately the ROUNDED display value, i.e. the same number the message prints, which makes the non-zero condition mean 'displays as a non-zero delta' and is why no message can ever read 'Δ0dp' (a 1px drift at 420 dpi is 0.38dp). Unknown density applies the bounds to px and the message carries the px suffix it already carried -- honest about what it measured rather than going silently dark. Touch target is unchanged: a minimum-touch-size guideline is inherently a dp rule, so it stays dp-only and stays silenced when density is unknown. Both constants renamed TOP_MISALIGNMENT_MAX / LEFT_MISALIGNMENT_MAX (the _PX suffix was now a lie), kept as named constants with a comment pointing at inputs/design-measure-source.md and explaining the dp-like-single-space reasoning. The two firing conditions collapsed to `delta in 1..MAX`, and Measure.kt's rail label now reuses the same displayDelta, so the rail can never disagree with the finding it illustrates.

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| T-7 scope vs the node-id format | The backlog gives T-7 'the node id format with its [n] collision index', but HierarchyNodeModel.id already carries the four-step resource-id -> text -> content-desc -> class[n] chain resolved in the data layer, and no frozen test asserts any re-derivation here. | Nothing implemented; MeasureFormat formats the id it is handed. Recorded for Stage 5 so the backlog clause reconciles to the T-2 gate ruling rather than reading as missing work. | info |
| guideNumeral | Suppression ordering (before vs after px->dp conversion, before vs after rounding) is this project's extension; the design has no density conversion and takes no position. | Implemented as after-conversion, before-rounding, which is the only ordering that satisfies the frozen 2px-at-420dpi case, and documented as an extension at the declaration site with the design's own 'if (v < 1) return' noted. | info |
| findingOf threshold units | Shipped in the first pass comparing the 10 and 24 bounds in raw device pixels. A real defect, not a style choice: the design's numbers are logical/dp-like units, so on a 420 dpi device the bound was 2.6x too strict and the top-misalignment finding would silently stop firing -- worse than an absent finding, because the panel then looks like it checked and found nothing. It was invisible at 160 dpi, which is the density every one of the twelve frozen test files uses, so the green suite proved nothing about the unit. | Corrected in the Stage 4 follow-up pass: all three bounds compare display units via displayDelta / toDisplayUnit, px only when density is unknown. MeasureThresholdUnitsTest pins it at 420 dpi with deltas where the px and dp readings disagree, and 3 of its 8 cases were verified to FAIL against the old raw-px code before the fix was restored. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-60
- AC-61
- AC-62
- AC-63
- AC-64
- AC-66

### validations

| what | how | result |
| --- | --- | --- |
| top bound in display units | 21px top delta at 420 dpi = exactly 8dp. Raw px reads 21 > 10 and stays silent; converted reads 8 <= 10 and fires 'top Δ6dp'-shaped message 'top Δ8dp vs item_title · expected 0'. | pass |
| left bound in display units | 42px left delta at 420 dpi = exactly 16dp. Raw px reads 42 > 24 and stays silent; converted reads 16 <= 24 and fires 'left Δ16dp vs item_icon · expected 0'. | pass |
| conversion is not loosening | 29px = 11.05dp -> 11 > 10, still silent. | pass |
| no message can read Δ0 | 1px at 420 dpi = 0.38dp -> reads 0, treated as aligned rather than reported as a zero delta. | pass |
| unknown density keeps the misalignment bounds working, in px | densityDpi 0, 8px delta -> 'top Δ8px vs item_title · expected 0'; 21px delta -> no finding. | pass |
| touch target stays dp-only and stays silenced at unknown density | 63px at 420 dpi = 24dp fires 'height 24dp · min touch 48dp'; 84px = exactly 32dp does not; the frozen touchTargetNeverFiresWhenDensityIsUnknownEvenIfOtherwiseTrue still passes unchanged. | pass |
