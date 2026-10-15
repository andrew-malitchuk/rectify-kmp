# drag-annotate-frozen-frame — T-12/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-12/report |
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

0cda6009a59777f3659817e03f309730285f9c1cdbe77beff8e5eb4117a2da3f

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-12

### type

ui

### outcome

success

### touchedPaths

- presentation-core-localisation/.../values/strings.xml
- MirrorAnnotationStage.kt
- MirrorNotesTab.kt

### testRun

#### command

./gradlew :presentation-core-localisation:compileKotlinDesktop

#### passed

true

#### output

BUILD SUCCESSFUL; every key verified to grep to at least one non-XML call site.

### implementation

#### filesChanged

- presentation-core-localisation/.../values/strings.xml
- MirrorAnnotationStage.kt
- MirrorNotesTab.kt

#### summary

SIX mirror_* keys, not seven. mirror_annotation_delete is the popover close control's contentDescription - close IS delete here per FR-8, and a screen reader saying Close would be wrong.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| strings.xml | mirror_status_frozen was DELIBERATELY NOT ADDED against TAC-1's literal count of seven. | The FROZEN label is StatusIndicatorState.Frozen.name.uppercase(), produced exactly the way LIVE is, so a key would have no call site - and localisation.md names unreferenced keys as a smell this repo has accumulated before. Six keys, six referenced. | warn |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- NFR-9
- FR-8
