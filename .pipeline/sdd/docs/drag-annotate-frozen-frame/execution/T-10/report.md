# drag-annotate-frozen-frame — T-10/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-10/report |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-15T05:15:01Z |
| updatedAt | 2026-10-15T05:15:01Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-15T05:15:01Z

## Provenance

### selfHash

1c1c6f3b1c8d7c346da6aeb74574f92ad1fb5047f12dd4b6ce968ee1aebd05de

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-10

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/.../core/annotation/PopoverCrowding.kt
- +PopoverCrowdingTest.kt

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks

#### passed

true

#### output

PopoverCrowdingTest 4 tests, 0 failures.

### implementation

#### filesChanged

- presentation-feature-mirror/.../core/annotation/PopoverCrowding.kt
- +PopoverCrowdingTest.kt

#### summary

internal anyOverlap(List<Rect>) delegating to Compose's own Rect.overlaps, which is already half-open so touching edges do not count. NOMINAL_POPOVER_WIDTH_DP=280f read from AnnotationPopover.PopoverMaxWidth rather than guessed.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| PopoverCrowding.kt | NOMINAL_POPOVER_HEIGHT_DP=36f was derived arithmetic, not measured. | T-11 later CONFIRMED it: AnnotationPopover is max(20dp badge, 19sp body) + 2x8dp = 36. It is the one-line figure, so the gutter pitches rows at a separate 54dp while leader lines aim at the one-line centre. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-9
- FR-9
- SC-4
