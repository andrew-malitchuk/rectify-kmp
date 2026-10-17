# payload-preview — T-9/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-9/report |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T19:14:05Z |
| updatedAt | 2026-10-17T19:14:05Z |

## Gate

### type

auto

### approvedBy

orchestrator-verified

### approved

true

### at

2026-10-17T19:14:05Z

## Provenance

### selfHash

6d3b44787c6038bafa5f0d6fb9a9cad81f73dff9dd96e9586e737399fcf9dd7f

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | a0d78474590386b4950f70a3807ae7a5e9006ae680e326103a8e1d652c3aef23 |

## Data

### taskId

T-9

### type

ui

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt

### tdd

#### cycle

n/a

#### testsFrozen

true

#### testsWritten

- none — T-9 is render-only wiring over wave-1 logic (payloadHierarchyRows, hierarchySourceLabel) already covered by HierarchyOutlineTest + PayloadLabelsTest. No Compose UI-test harness exists in this repo; the evidence is now three previews: populated-with-dump, no-hierarchy, and empty.

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun  then  :presentation-feature-mirror:build :presentation-core-ui:build

#### passed

true

#### output

Verified independently by the orchestrator with --rerun: mirror 248 tests, 0 failures, 0 errors; core-ui 29 tests, 0 failures — baseline unchanged as expected. Cross-target :build BUILD SUCCESSFUL for both modules (338 tasks), including the iosSimulatorArm64 suites.

### implementation

#### filesChanged

- presentation-feature-mirror/.../source/mirror/MirrorPayloadTab.kt

#### summary

Added the private PayloadHierarchySection: PayloadSectionLabel("HIERARCHY") reused from wave 5 plus a right-aligned hierarchySourceLabel(state.hierarchyStatus, hierarchy.nodes.size), then a LazyColumn with no vertical arrangement spacing (HierarchyRow draws its own indent guides, so rows must be adjacent) rendering HierarchyRow(nodeId, depth, last, tag = row.badge?.let { "#" + it }, matched). Height capped at HierarchyOutlineMaxHeight so it measures inside the verticalScroll parent. Absent/Unavailable renders an explicit no-hierarchy line with zero rows while Prompt and Crops render normally (AC-80). dumpHierarchy is never referenced. T-7's nodeIdFor stub is discharged with a real resolver that inverts T-5's rows. 214 insertions / 13 deletions in one file.

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | [] |
| cross-target-build | true | 2 | ["First run failed in linkReleaseFrameworkIosArm64 (known Kotlin/Native release-linker heap exhaustion); green on an unchanged re-run, with all Android/iOS test tasks UP-TO-DATE in run 2 meaning they had already passed in run 1. Orchestrator's own re-run also green."] |
| dod-no-dump | true | 1 | ["Orchestrator verified grep for dumpHierarchy, 'ponytail:' and 'Not read yet' in the file returns 0 — the section only reads the cached snapshot, and T-7's two markers are gone."] |
| dod-remember-once | true | 1 | ["payloadHierarchyRows is projected once in remember(hierarchy, frozen.annotations) at the tab level — never in the LazyColumn's item scope. Orchestrator confirmed the prompt's remember key set is byte-identical to wave 4's (annotations, deviceLabel, deviceSize, hierarchy)."] |
| dod-private-sections | true | 1 | ["Only MirrorPayloadTab is public; PayloadHierarchySection and all helpers/previews are private and unregistered."] |

### troubleshooting

| at | severity | issue | resolution |
| --- | --- | --- | --- |
| cross-target build | warn | linkReleaseFrameworkIosArm64 'Compilation finished with errors' on the first :build. | Known, already root-caused infra issue (release-linker heap exhaustion under parallel link tasks). Re-ran once unchanged: BUILD SUCCESSFUL. Not chased with code. |
| LazyColumn in a scrollable parent | info | SPEC DEFECT pre-decided by the orchestrator: the mandated LazyColumn inside the root verticalScroll Column throws 'Vertical viewport was given unbounded height to measure'. | Kept LazyColumn (the plan's performance budget relies on it composing only the visible window of a ~400-node dump) and capped the constraint with heightIn(max = HierarchyOutlineMaxHeight = 338.dp) — 13 rows at HierarchyRow's shipped 26dp, a whole number so no row clips mid-height, and the largest block the panel can give the tree without pushing T-10's footer off an 820dp window given the prompt claims up to 214dp and the crop grid two 72dp rows. Same technique wave 4 used for PromptBodyMaxHeight. Lazy windowing intact: ~13 of 400 nodes composed. |
| HierarchyRow tag type | info | SPEC DEFECT pre-decided by the orchestrator: the backlog's 'tag = row.badge' does not compile — HierarchyRow.tag is String?, PayloadNodeRow.badge is Int?. | tag = row.badge?.let { HIERARCHY_TAG_PREFIX + it } with HIERARCHY_TAG_PREFIX = "#", mirroring the existing CROP_BADGE_PREFIX. |
| LazyColumn item key | info | items(rows, key = { it.nodeId }) is the idiomatic default but would hard-crash on a duplicate node id. | ACCEPTED BY THE ORCHESTRATOR: no key supplied. HierarchySnapshotModel node ids are only documented-unique, the list never reorders in place, and a duplicate LazyColumn key is a crash rather than a glitch. Documented in KDoc. |
| no-hierarchy line gating | info | AC-80's letter suggests keying the no-hierarchy line off empty rows. | ACCEPTED BY THE ORCHESTRATOR: gated on status in {Absent, Unavailable} instead, so a Dumping state or a genuinely empty Ready dump is not told 'No view hierarchy for this frame' while the readout beside the label says 'dumping…'. Reads slightly beyond AC-80's letter and is the correct reading. |
| nodeIdFor resolver | info | T-7 left buildPrompt(frozen) { null }, so prompt blocks printed no node-id segment. | Discharged: buildPrompt(frozen) { annotation -> rows.firstOrNull { it.badge == annotation.index }?.nodeId }. No projectMeasureNodes/snapTarget in the composable — it inverts T-5's single projection, which is sound because StagedAnnotation.index is 1-based contiguous and renumbered on delete, exactly how payloadHierarchyRows numbers badges. Rows are projected once at the tab level and shared by the prompt resolver and the outline rather than computed twice. |
| no-hierarchy copy | info | No localised key exists for the no-hierarchy line and strings.xml was outside this wave's single-file scope. | Reused the shipped mirror_measure_hierarchy_unavailable rather than inventing a key (a non-existent resource reference fails the build). Its copy is accurate here, but the 'these are measured' clause points at the measure panel's readouts rather than at the payload prompt — a dedicated mirror_payload_hierarchy_absent would read better. Logged as copy polish for T-12 or the Stage 6 review; not a blocker. |
| nested scrolling | info | The bounded LazyColumn means the outline scrolls under the pointer and the tab takes over at either end. | Standard Compose nested-scroll behaviour and the price of keeping LazyColumn. With no UI-test harness this was verified by construction and documented in KDoc, not by gesture — a candidate for the Stage 5 real-device manual pass (HV-1). |

### registryUpdates


### needsHumanReview

#### required

false

#### reason

Gates green and every DoD line independently verified. Nested-scroll feel is the one thing only a human can judge; it is covered by HV-1's real-device pass at the Stage 5 gate.

### linkedAC

- AC-15
- AC-74
- AC-75
- AC-80

### backlogUpdate

#### taskId

T-9

#### status

done

### coverage

skipped (no UI-test harness in this repo)
