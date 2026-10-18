# measure-mode — T-6

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-6 |
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

4041d08a735df1a8e13714afe8f7557f3ead34802a16061c5c1e1f27b1103e55

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 6f849f4416e8adcf94559bb05055afe9181ce0fe26bbcf6da2d9619f09dab0d8 |
| execution/T-5/report.json | 5dacded2f743c5dd5ed781cc67fe1b56412e100b302785855e502b942dbf5344 |

## Data

### taskId

T-6

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureNode.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/NodeSnap.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Neighbours.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Gaps.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/PixelUnits.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureProjection.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/AnnotationHitTest.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt

### tdd

#### cycle

red-green-refactor

#### testsFrozen

true

#### followUpTestsAdded


#### followUpNote

Follow-up pass added no test for T-6: its one follow-up item is a KDoc clarification on projectMeasureNodes, already pinned by the frozen theFullFrameRootNodeIsNeverItselfACandidate.

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
-- desktopTest XML results (--rerun-tasks), T-6's six frozen files --
NodeSnapTest[desktop]          tests="7" skipped="0" failures="0" errors="0"
NeighboursTest[desktop]        tests="9" skipped="0" failures="0" errors="0"
GapsTest[desktop]              tests="6" skipped="0" failures="0" errors="0"
PixelUnitsTest[desktop]        tests="5" skipped="0" failures="0" errors="0"
MeasureProjectionTest[desktop] tests="4" skipped="0" failures="0" errors="0"
AnnotationHitTestTest[desktop] tests="4" skipped="0" failures="0" errors="0"
-- module-wide totals (all 29 classes) --
tests=169 skipped=0 failures=0 errors=0
```

### implementation

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureNode.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/NodeSnap.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Neighbours.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/Gaps.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/PixelUnits.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/MeasureProjection.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/measure/AnnotationHitTest.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt

#### summary

New internal package core/measure holding the Compose-free numeric rules, plus the public result types the UI will consume on MirrorContract.kt. snapTarget filters candidates fully contained within 4px slack per edge and takes maxByOrNull on area, which gives largest-area-wins with document-order tie-breaking in one linear pass (no sort, no nearest-centre, no first-found). neighboursOf drops the target itself and every strictly-overlapping node, then splits the survivors into a vertical and a horizontal band once and takes the closest facing edge per side, so a diagonal node fails both band tests and is excluded on both axes; a node whose edge merely touches the target survives the strict overlap test and is the zero-gap neighbour. gapsOf substitutes 0 / frame.width / frame.height for an absent neighbour, so no side is ever omitted. toDisplayUnit is px*160/densityDpi rounded to nearest, with a non-positive densityDpi degrading to the raw px value labelled MeasureUnit.Px per the degradation ruling; it never divides by the frame, so a zero-node snapshot carrying a real density is unaffected. displayValueOf exposes the same figure unrounded, which is what T-7's suppression rule needs. projectMeasureNodes discards the whole snapshot when its root-derived frame disagrees with FrozenFrame.deviceSize (T-1's 984x2132-vs-1080x2400 case) and filters out every node covering the whole frame, not just depth 0, because a real decor chain stacks several full-frame ancestors and all of them would beat real nodes on area. annotationAt is a pure topmost-wins (highest index) hit test with exclusive right/bottom edges. MirrorContract gains MeasureUnit (with a suffix property), MeasureSide (with a lowercase label used by T-7's note line), HierarchyStatus, MeasureValue, MeasureGuide (IntOffset endpoints, reused rather than a new point type) and MeasureReadout.

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| projectMeasureNodes | The hierarchy root fully contains every other node and always wins snapTarget on area, so without an exclusion every real measure snaps to the root. Recorded in the gate addendum but absent from plan and backlog. | Excluded by geometry (left==0, top==0, right==frame.width, bottom==frame.height) rather than by depth==0, so the several full-frame ancestors a real decor chain stacks (DecorView -> LinearLayout -> FrameLayout -> content) are all excluded, not only the first one. Accepted in the Stage 4 follow-up as correct and DELIBERATELY WIDER than the step-2 gate record's narrower 'the root' phrasing; the KDoc now says so explicitly so a reviewer comparing the two resolves it at the declaration site. Pinned by theFullFrameRootNodeIsNeverItselfACandidate. | info |
| findingOf threshold units (RESOLVED in the follow-up pass) | The misalignment thresholds (10 / 24) compared raw device pixels while the touch-target threshold (32dp) compared display units. This was a REAL BUG, not just an undocumented choice: the design works in a single dp-like space (its frame is 296x640), so on T-1's 420 dpi Pixel 8 a raw-px bound of 10 is +/-3.8dp -- 2.6x stricter than designed -- and the top-misalignment finding would essentially never fire. What hid it is the useful part for Stage 5: all twelve frozen test files use densityDpi 160, where px == dp, so NO frozen assertion could distinguish the two unit choices, and the suite was green either way. | Fixed in T-7's Findings.kt during the follow-up pass: all three bounds now compare the display unit in use. See T-7's own entry for the fix and the two new tests that fail against the old raw-px behaviour. | info |
| MeasureUnit.suffix / MeasureSide.label | noteLine needs the lowercase side word and every label needs the unit suffix. | Added as properties on the two public enums rather than as a private lookup table in core/measure, so the single source of each machine string sits next to the type it describes. These are machine strings, never localised, same precedent as MirrorErrorCard.diagnostic. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-57
- AC-58
- AC-59
