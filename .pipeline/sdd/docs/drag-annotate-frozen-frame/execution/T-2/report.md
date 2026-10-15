# drag-annotate-frozen-frame — T-2/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-2/report |
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

b12637ce5f6255f54d1750536857a4147f0d083fb630a93e6d916cd6cedc7131

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-2

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/.../source/mirror/MirrorContract.kt
- +core/annotation/AnnotationFormat.kt
- +AnnotationFormatTest.kt

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks

#### passed

true

#### output

AnnotationFormatTest 3 tests, 0 failures.

### implementation

#### filesChanged

- presentation-feature-mirror/.../source/mirror/MirrorContract.kt
- +core/annotation/AnnotationFormat.kt
- +AnnotationFormatTest.kt

#### summary

public DeviceRect with derived width/height; internal metaLine/sizeLabel producing the kit's exact shapes. Zero-size rect formats without crashing.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| AnnotationFormat.kt | Backlog prose writes sizes in ASCII (218x77) but AnnotationRow, CommentPopover and AnnotationMark all ship U+00D7. | Implemented with U+00D7 per the DoR's confirm-from-source instruction; one private const is the single switch point if ASCII was intended. | info |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-5
- AC-7
- FR-6
- FR-8
