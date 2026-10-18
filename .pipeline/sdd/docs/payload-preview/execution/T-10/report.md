# payload-preview — T-10/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-10/report |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T19:26:24Z |
| updatedAt | 2026-10-17T19:26:24Z |

## Gate

### type

auto

### approvedBy

orchestrator-verified

### approved

true

### at

2026-10-17T19:26:24Z

## Provenance

### selfHash

8a240a5cb2e8df6c8d72d7909ee85dd3519aed4344ea8414a4f2d3e9c6b3b174

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | a0d78474590386b4950f70a3807ae7a5e9006ae680e326103a8e1d652c3aef23 |

## Data

### taskId

T-10

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

- none — the footer's two strings are already pinned character-exactly by wave 2's PayloadEstimateTest (footerLabel, payloadSizeLabel), and T-10 adds no new logic. No Compose UI-test harness exists in this repo; visual evidence is the populated previews.

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

Added a PINNED private PayloadFooter: a full-width Theme.stroke.thin / Theme.color.line hairline over a Row(SpaceBetween, spacingS) with footerLabel(count) left (maxLines=1, TextOverflow.Ellipsis, weight(1f, fill=false)) and payloadSizeLabel(count) right, both monoMeta on ink3. Both strings come straight from core/payload/PayloadEstimate.kt — verified no 1.6/0.12 literals anywhere in the file. Restructured the root into an outer Column with the three existing sections moved into an inner weight(1f) Column that keeps the verticalScroll and content padding, so the footer pins below the scrolling area. Borrowed ShellStatusBar's hairline idiom (a line-filled Box rather than a border) so the rule spans the panel width while the row keeps its inset. Added private val PreviewPanelHeight = 640.dp to the populated previews — required, not cosmetic: a weight child under an infinite height constraint collapses to zero, so a height-wrapping preview would render the footer over an empty body. 97 insertions / 22 deletions, one file.

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | [] |
| cross-target-build | true | 2 | ["First run: linkReleaseFrameworkIosSimulatorArm64 (core-ui) and linkReleaseFrameworkIosArm64 (mirror) both failed with 'Compilation failed: Java heap space' — the known release-linker OOM under parallel link tasks. Re-ran once unchanged: BUILD SUCCESSFUL. Orchestrator's own re-run also green."] |
| tac-2-notestab-unchanged | true | 1 | ["Orchestrator verified `git diff --stat -- *MirrorNotesTab.kt *SidePanelFrame.kt *SidebarTabs.kt` returns ZERO lines — all three byte-unchanged, satisfying TAC-2 and both DoD 'untouched' lines."] |
| dod-no-recomputation | true | 1 | ["Orchestrator verified: no 1.6 or 0.12 literal anywhere; both strings are direct footerLabel/payloadSizeLabel calls."] |
| ac-77-empty-gate-precedence | true | 1 | ["Orchestrator verified the empty-state early return is still the first statement of the body — before the rows remember, before the outer Column, before PayloadFooter. Zero annotations renders only the empty copy: no hairline, no footer."] |

### troubleshooting

| at | severity | issue | resolution |
| --- | --- | --- | --- |
| cross-target build | warn | linkReleaseFramework* OOM (Java heap space) on iosArm64 and iosSimulatorArm64. | Known, already root-caused infra issue. Re-ran unchanged: passed. Not chased with code. |
| pinned vs scrolling footer | info | The design's footer is flex:none with border-top below a scrolling content area, but T-7's root was a single verticalScroll Column, so an appended footer would scroll away. The orchestrator left this as a genuine judgement call, noting a pinned footer plus the 214dp prompt and 338dp hierarchy caps might squeeze the content area at small window heights. | PINNED, and the orchestrator accepted the reasoning: the payload size is the number a user checks WHILE reading the prompt, so a footer that scrolls away is missing exactly when it is consulted, and the design's flex:none + border-top is unambiguous. The squeeze worry does not bite because PromptBodyMaxHeight and HierarchyOutlineMaxHeight are heightIn(max=) CAPS rather than minimums and those blocks scroll internally — a short content area shrinks the sections rather than fighting the footer, which itself costs one hairline plus ~29dp. |
| host height contract | warn | Pinning via weight(1f) silently collapses the content area to zero when the parent supplies an infinite max height — which is exactly what the previews' bare wrapper Column did. | Previews now pin an explicit PreviewPanelHeight (SidePanelFrame's own preview does the same) and the constraint is documented in the tab's KDoc. CARRIED FORWARD TO T-11: it must wire the tab into a bounded-height parent. SidePanelFrame satisfies this (its content slot is a child of a fillMaxHeight Column), but T-10 could NOT verify it at runtime because nothing hosts the tab until T-11 — if the wiring is unbounded, the whole body renders at zero height with only the footer visible, which will be immediately obvious. |
| typography token | info | The orchestrator's brief said to use the mono token the other sections already use (monoTag, 9.5sp); the implementer used monoMeta (10.5sp) instead and flagged the deviation rather than making it silently. | ORCHESTRATOR ACCEPTED: the design spec table states 'Footer text \| 10.5px, JetBrains Mono, ink3' explicitly, and monoMeta is an existing, widely-used token in this feature (MirrorContent's status line, ShellStatusBar). Design-faithful beats sibling-consistent when the design names a figure. No new token invented. |
| design prose vs engineer decision | info | The design extract's prose says the payload summary is 'visible in both Notes and Payload tabs'. | Left overridden, as decided at the Stage 1 human gate: Payload tab only, zero changes to MirrorNotesTab. The implementer did not 'fix' it back toward the design. |

### registryUpdates


### needsHumanReview

#### required

false

#### reason

Gates green and every DoD/TAC line independently verified. The pinned layout's runtime behaviour depends on T-11 supplying a bounded height, which T-11 must confirm; it is also covered by HV-1's real-device pass at the Stage 5 gate.

### linkedAC

- AC-16
- AC-78

### backlogUpdate

#### taskId

T-10

#### status

done

### coverage

skipped (no UI-test harness in this repo)
