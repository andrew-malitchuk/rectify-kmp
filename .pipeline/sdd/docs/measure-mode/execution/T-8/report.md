# measure-mode — T-8

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-8 |
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

be0e2bf284621fec9790d255c95e1b22f784dd3246341d91f90f51f329088fdd

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-5/report.json | 5dacded2f743c5dd5ed781cc67fe1b56412e100b302785855e502b942dbf5344 |

## Data

### taskId

T-8

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Measure.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

#### followUpTestsAdded

| file | cases | why | oracle | countAsserted |
| --- | --- | --- | --- | --- |
| presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MeasureGuideGeometryTest.kt | ["allFourGuidesMatchTheDesignsOwnHAndVGeometryOnTheOracleFixture", "aGuideWhoseGapIsUnderOneDisplayUnitIsAbsentFromTheListEntirely"] | MeasureReadout.guides is what the overlay draws and nothing asserted its contents -- only guideNumeral's suppression was pinned -- so wrong endpoints would have put every line in the wrong place with no test failing. | Expected endpoints derived by re-executing inputs/design-measure-source.md's own H/V helpers over its own ELS array and item_price annotation in a throwaway script (the same method MeasureEndToEndTest used for the value rows), not read back off the implementation: H(166,228,206.5,62) H(276,296,206.5,20) V(183,198,252.0,15) V(215,264,252.0,49). The script was deleted afterwards. | Both cases assert the whole guide LIST, not endpoint-by-endpoint, because H/V are `if (v < 1) return` -- a silently missing guide would otherwise pass. Four guides on the oracle fixture; three in the suppression case, where a 2px left gap is 0.76dp at 420 dpi. |

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
-- desktopTest XML results (--rerun-tasks), T-8's three frozen files plus the new one --
MeasureEndToEndTest[desktop]   tests="3" skipped="0" failures="0" errors="0"
MeasureFallbackTest[desktop]   tests="3" skipped="0" failures="0" errors="0"
MeasureRailTest[desktop]       tests="4" skipped="0" failures="0" errors="0"
MeasureGuideGeometryTest[desktop] NEW tests="2" skipped="0" failures="0" errors="0"
-- module-wide totals (all 29 classes) --
tests=169 skipped=0 failures=0 errors=0
```

### implementation

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Measure.kt

#### summary

measure(annotation, index, nodes, frameSize, densityDpi) composes the whole arithmetic into one MeasureReadout, mirroring the design's recovered measure(a, u). Snap or fall back to a synthetic MeasureNode(NO_MATCH_NODE_ID, annotation, isTappable = false) -- the untappable flag is what makes the fallback report no finding, matching the design's undefined t.tap; then neighbours, gaps, the five formatted values, the four suppression-filtered gap guides along the target's centre lines, the single finding, the rail, and the note line. neighbourRects is [left, right, top, bottom] with absent sides dropped and a 1px height floor so a hairline divider stays a visible outline (the design's Math.max(e.h, 1); no ELS node has h == 0, so this is implemented from the design and unexercised by the oracle, as its test comment records). The rail populates for FindingKind.TopMisalignment only and runs from the left neighbour's left edge, along that neighbour's own top, to the target's right edge. Reproduces the design's 19-node ELS oracle exactly for item_price (48x17dp, top 15dp, right 20dp, bottom 49dp, left 62dp, 'top Δ6dp vs item_title · expected 0', three neighbour rects, rail (84,192)->(276,192)) and for the no-containment region (View (no match), 10x10dp / 11 / 136 / 34 / 34). Five linear passes over nodes and no nesting, so AC-68's 200 calls over 319 nodes stay far inside the 5s ceiling. FOLLOW-UP CORRECTION 2 (guides geometry): no code change was needed -- the four guides' endpoints already matched the design's own H/V helpers exactly, confirmed by re-executing them over the ELS fixture rather than by reading the implementation. They are now pinned by MeasureGuideGeometryTest, count included. The one deviation found is sub-pixel and deliberate: the design's mid-y for an odd-height target is a real-valued 206.5 while this implementation truncates to 206, because the overlay draws on an integer pixel grid and IntOffset cannot carry a half pixel; it is now stated in gapGuides' KDoc and in the test.

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| MeasureEndToEndTest Top row | The oracle's Top gap is 15dp while MeasureCard.kt's shipped @Preview says 9dp. | Followed the algorithm per the gate addendum: the nearest horizontally-overlapping node above item_price is divider_1 (bottom edge 183), giving 198-183 = 15. No node in the fixture has a bottom edge at 189, so 9 is unreachable. The preview was not reconciled -- T-12 owns that one-line correction. | info |
| MeasureReadout.guides (RESOLVED in the follow-up pass) | No frozen test asserts the contents of readout.guides; only guideNumeral's suppression is pinned, via T-7's GuideSuppressionTest. | Closed in the Stage 4 follow-up: MeasureGuideGeometryTest asserts all four guides' sides, labels and endpoints against the design's own H/V helpers re-executed over the ELS fixture, plus the count, plus a suppression case at 420 dpi. The implementation needed no change -- the geometry was already right -- so what the follow-up bought is that a future edit can no longer move the lines silently. Only the sub-pixel mid-y truncation (206 vs the design's 206.5) differs, and it is documented rather than fixed. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-65
- AC-68
- AC-10

### validations

| what | how | result |
| --- | --- | --- |
| all four guide endpoints vs the design's H/V | item_price on the 19-node ELS fixture at 160 dpi: Left 62dp (166,206)->(228,206), Right 20dp (276,206)->(296,206), Top 15dp (252,183)->(252,198), Bottom 49dp (252,215)->(252,264), in the design's own call order. | pass -- matched the implementation as shipped, no code change needed |
| guide count under suppression | 2px left gap at 420 dpi (0.76dp): the Left guide is absent entirely and the list is exactly [Right, Top, Bottom] with their own endpoints asserted. | pass |
| mid-line placement on an odd height | The design's my = t.y + t.h/2 is 206.5 for item_price; the implementation truncates to 206 on the integer pixel grid the overlay draws on. Recorded in the test and at the declaration site rather than changed -- half a pixel either way is not a placement anyone can see, and IntOffset cannot carry 206.5. | accepted deviation, documented |
