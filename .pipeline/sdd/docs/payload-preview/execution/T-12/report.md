# payload-preview — T-12/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-12/report |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T20:00:30Z |
| updatedAt | 2026-10-17T20:00:30Z |

## Gate

### type

auto

### approvedBy

orchestrator-verified

### approved

true

### at

2026-10-17T20:00:30Z

## Provenance

### selfHash

e67c30002232f12998ea1bfa37cdc55d1e6009ac465fb31737ae9f5cf76f9027

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | a0d78474590386b4950f70a3807ae7a5e9006ae680e326103a8e1d652c3aef23 |

## Data

### taskId

T-12

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

- none, and none permitted — the DoD forbids adding ui-test-junit4 or runComposeUiTest. Previews are this repo's only visual evidence; the existing 248 mirror tests cover the logic they render (cropWindow clamp, payloadHierarchyRows snap, footer labels) and were re-run as the regression guard.

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun  then  :compose-application:compileKotlinDesktop  then  :presentation-feature-mirror:build :presentation-core-ui:build

#### passed

true

#### output

Verified independently by the orchestrator with --rerun: mirror 248 tests, 0 failures, 0 errors; core-ui 29 tests, 0 failures — exact baseline. :compose-application:compileKotlinDesktop BUILD SUCCESSFUL (the only check for the RectifyWindow wiring, since that module deliberately has no test wiring). Cross-target :build BUILD SUCCESSFUL for both modules (338 tasks).

### implementation

#### filesChanged

- presentation-feature-mirror/.../source/mirror/MirrorPayloadTab.kt

#### summary

Previews and the token mapping only; no shipped token choice was changed. (1) A one-pass token-mapping block comment at the head of the preview block reconciling every design figure against what the code actually uses, with divergences written down AS divergences rather than asserted as matches. (2) The three single previews became six (three states x Light/Dark) following the repo's HierarchyRow pattern: one private MirrorPayloadTabPreviewContent helper plus six AppTheme(mode = ThemeMode.Light/Dark) wrappers, so each differs only by theme mode and state. PreviewPanelHeight stays applied to the four populated/no-hierarchy previews because T-10's pinned footer rides a weight(1f) content area that collapses under an infinite height constraint; the empty pair omits it since the tab early-returns first. (3) Preview annotation #2 moved to an edge-touching rect so cropWindow's clamp is finally exercised.

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | [] |
| cross-target-build | true | 2 | ["First run exited 0 but its piped log was lost to a 0-byte capture; re-ran redirected to a file for a readable tail. Both BUILD SUCCESSFUL — the heap OOM did not occur on this agent's runs."] |
| dod-six-previews | true | 1 | ["Six previews: MirrorPayloadTabPreviewLight/Dark, MirrorPayloadTabNoHierarchyPreviewLight/Dark, MirrorPayloadTabEmptyPreviewLight/Dark."] |
| dod-no-ui-test-harness | true | 1 | ["No ui-test-junit4, no runComposeUiTest, no new dependency."] |
| dod-token-mapping-comment | true | 1 | ["Present at the head of the preview block, including the five divergence rows."] |

### troubleshooting

| at | severity | issue | resolution |
| --- | --- | --- | --- |
| preview annotation #2 | info | The pre-existing preview rects (left=120 and left=80) both sat clear of the scaled pad (~51px on a 1080-wide frame), so NO preview had ever exercised cropWindow's clamp — which is precisely what TAC-1 asks a human to eyeball. | Annotation #2 moved to DeviceRect(0, 2248, 432, 2400) so cropX AND cropH both clamp, and its matching hierarchy node moved to an exactly-coincident NavigationBar child so the badge and matched-ink row still land (snapTarget picks the largest contained node, so the exact match wins). |
| token divergence: prompt body | warn | Design figure says 10.5px mono; the code renders Theme.typography.monoData = 11sp. | RECORDED, NOT FIXED — deliberately outside T-12's scope, which records the mapping rather than re-skinning the tab. No 10.5sp mono-with-tabular-figures token exists, and monoMeta (10.5sp) is the meta scale which the footer owns. Needs a designer's ruling at the Stage 5 gate. |
| token divergence: Crops frame tag | warn | The design's ink2 row covers 'frame/agent labels', but the Crops 'from frame #N' tag renders in ink3. | RECORDED, NOT FIXED. Needs a designer's ruling at the Stage 5 gate. |
| token near-misses | info | Three further rows do not line up exactly. | Recorded: hierarchy row labels are monoData 11sp/16sp vs the figure's 10.5px/17px; the ink2 row has no real consumer in this tab (the empty and no-hierarchy copy use non-mono `secondary`, for which the figure has no row); and the red WARN state does not exist on this surface — the hierarchy verdict is plain ink3 text, so WARN belongs elsewhere. |
| @Preview deprecation | info | The six new previews emit the repo-wide 'annotation class Preview is deprecated' warning. | Pre-existing and identical across every preview in the module. Migrating the annotation is a repo-wide change, not this task. |

### registryUpdates


### needsHumanReview

#### required

true

#### reason

KEPT, not downgraded. Two token rows diverge from the design figure and were recorded rather than fixed (prompt body 11sp vs 10.5px; Crops frame tag ink3 vs ink2) — a designer must rule on both. Separately, previews are visual evidence only once a human opens them: AC-80's primary evidence and TAC-1/TAC-4 all need someone to look at the six previews in Android Studio. Routes to the Stage 5 human gate alongside HV-1.

### linkedAC

- AC-14
- AC-16
- AC-72
- AC-80

### backlogUpdate

#### taskId

T-12

#### status

done

### coverage

skipped (no UI-test harness in this repo)
