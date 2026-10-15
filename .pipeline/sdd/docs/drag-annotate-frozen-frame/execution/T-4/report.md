# drag-annotate-frozen-frame — T-4/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-4/report |
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

8c3166b30dc817add55311cca063c4a6980b97ccdfefa4315274c7b140821384

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | ad34003d23112d2823248241d2b777a5132e8b7f4e93f70c07ddb7d25a6422f1 |

## Data

### taskId

T-4

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/.../MirrorViewModel.kt
- +MirrorFakes.kt
- +MirrorViewModelAnnotationTest.kt

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest --rerun-tasks ; :compileTestKotlinIosArm64

#### passed

true

#### output

MirrorViewModelAnnotationTest 13 tests, 0 failures. MUTATION-VERIFIED: removing the re-freeze guard, removing delete renumbering, and pinning a wrong bitmap size each produced real failures.

### implementation

#### filesChanged

- presentation-feature-mirror/.../MirrorViewModel.kt
- +MirrorFakes.kt
- +MirrorViewModelAnnotationTest.kt

#### summary

frozenFrame StateFlow beside frame; six handlers. StageAnnotation reads currentFrame.value EXACTLY ONCE at release (that read is AC-47) and freezes only when nothing is pinned, so a second drag annotates the same frame with no stage transition. deleteAnnotation renumbers survivors and follows the selected annotation OBJECT through renumbering rather than doing index arithmetic. sessionDeviceId and the flatMapLatest gate are byte-identical - freezing is a render decision, so Resume Live costs nothing.

#### reusedFromRegistry


### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| MirrorViewModel.kt | SKIKO PROBE FAILED: ImageBitmap(2,3) throws LibraryLoadException in this module's desktopTest - only compose-application wires compose.desktop.currentOs, and AS-14 forbids adding it. | Rather than dropping AC-47 to inspection+demo, noted that ImageBitmap is a plain interface and built FakeFrameBitmap in commonTest. AC-47 keeps a REAL automated assertion on frame identity plus both dimensions - strictly better than the planned fallback. | warn |

### needsHumanReview

#### required

false

#### reason



### linkedAC

- AC-6
- AC-7
- AC-8
- AC-47
- AC-48
- AC-49
- AC-50
- AC-51
- AC-52
- FR-7
- FR-27
- FR-30
- FR-31
- NFR-1
- SC-9
- SC-10
- SC-11
