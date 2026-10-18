# payload-preview — T-7/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-7/report |
| status | done |
| producedBy | sdd-build-frontend-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-17T17:57:16Z |
| updatedAt | 2026-10-17T17:57:16Z |

## Gate

### type

auto

### approvedBy

orchestrator-verified

### approved

true

### at

2026-10-17T17:57:16Z

## Provenance

### selfHash

3d3c2b3e20c3d9a5ea8423f3aac3ce8cbcccf8b0cc31905dc6fa64ff9922c968

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | a0d78474590386b4950f70a3807ae7a5e9006ae680e326103a8e1d652c3aef23 |

## Data

### taskId

T-7

### type

ui

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt
- presentation-core-localisation/src/commonMain/composeResources/values/strings.xml

### tdd

#### cycle

n/a

#### testsFrozen

true

#### testsWritten

- none — this repo has no Compose UI-test harness (no runComposeUiTest, no ui-test-junit4). The two previews are T-7's visual evidence per .claude/rules/testing.md. No UI-test dependency was invented.

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun  then  :presentation-feature-mirror:build :presentation-core-ui:build

#### passed

true

#### output

Verified independently by the orchestrator with --rerun: mirror 248 tests, 0 failures, 0 errors; core-ui 29 tests, 0 failures — both exactly the wave-3 baseline, as expected since T-7 adds no tests. The real gate here is that everything still compiles and passes. Cross-target :build BUILD SUCCESSFUL for both modules (338 tasks), including the iosSimulatorArm64 suites.

### implementation

#### filesChanged

- presentation-feature-mirror/.../source/mirror/MirrorPayloadTab.kt
- presentation-core-localisation/.../values/strings.xml

#### summary

New public MirrorPayloadTab(state, hierarchy, onIntent, modifier): Unit, modelled on the shipped MirrorNotesTab sibling. The empty gate is a literal early return as the first statement after reading state.frozen: no frame or zero annotations renders only the localised empty-state copy and returns, composing nothing else (verified by the orchestrator against the DoD). Otherwise one private PayloadPromptSection renders inside a scrolling Column already spaced for the sections waves 5-7 append. The section uses Theme.typography.overline (already mono JetBrains 10.5sp / 0.08em — the exact design spec, so no new token), a right-aligned '~ N tokens' readout from core/payload's tokenEstimate(), and a Ghost AppButton at AppButtonSize.ListAction (24dp) with a localised contentDescription. Read mode is a SelectionContainer-wrapped mono Text on Theme.color.codeBg with a thin line border, clipped to Theme.corner.xs, heightIn(max = 214dp) + verticalScroll. Edit mode swaps in the shipped MultilineInput atom (not a hand-rolled field) dispatching MirrorIntent.EditPrompt per change. Effective prompt = frozen.promptOverride ?: remember(frozen.annotations, frozen.deviceLabel, frozen.deviceSize, hierarchy) { buildPrompt(frozen) { null } } — keyed assembly rather than per-frame work, and an override is never auto-cleared when annotations change (AC-79). The Edit toggle is a local remember(frozen.frameId); MirrorUiState gained no field (orchestrator confirmed MirrorContract.kt is untouched).

#### reusedFromRegistry

| registryRef | how |
| --- | --- |
| components.json#multiline-input | Edit mode's text field, reused as-is with textStyle = Theme.typography.monoData and maxHeight = 214dp. No text field written. |
| components.json#app-button | The Edit affordance — Ghost variant at ListAction size for the >=24dp touch target. |

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| desktopTest | true | 1 | [] |
| cross-target-build | true | 2 | ["First run failed on linkReleaseFrameworkIosArm64 — the already-root-caused parallel-link heap exhaustion. No code change; unchanged re-run green. Orchestrator's own re-run also green."] |
| dod-empty-gate | true | 1 | ["Orchestrator verified the gate is a literal early return as the first statement, not a conditionally-rendered sibling."] |
| dod-explicit-api | true | 1 | ["Explicit public modifier and explicit ': Unit' return type present, per explicitApi()."] |
| dod-private-sections | true | 1 | ["Only MirrorPayloadTab is public; PayloadPromptSection, previewState and both previews are private and unregistered."] |
| a11y-touch-target | true | 1 | ["AppButtonSize.InlineRow (22dp, what the sibling CommandBlock uses) is below the >=24dp floor; ListAction (24dp) chosen as the smallest preset that clears it rather than hardcoding a height on a 22dp button."] |

### troubleshooting

| at | severity | issue | resolution |
| --- | --- | --- | --- |
| cross-target build | warn | linkReleaseFrameworkIosArm64 failed on the first :build invocation. | Known, already root-caused infra issue: the Kotlin/Native release linker exhausts Java heap under ~4 parallel linkReleaseFramework* tasks. Re-ran once unchanged: green. Not chased with code. |
| design tokens | info | The design's 214dp prompt max-height and 10dp body inset have no token (the scale is strictly 4/8/16/24/32/48/64), and design-tokens.md reads as an absolute ban on raw .dp. | ADJUDICATED BY THE ORCHESTRATOR: the rule forbids raw .dp AT CALL SITES, and the new file has zero of those. It declares two named private val Dp constants, which is the established pattern in this very package — verified: MirrorAnnotationStage.kt (MeasureCardWidth 228.dp, GutterRowHeight 54.dp, BannerTopInset 14.dp), MirrorErrorStage.kt (ErrorCardWidth 460.dp), MirrorContent.kt (NoticeMaxWidth 420.dp). No violation, no change needed; adding single-use ThemeSize fields for one composable's geometry would be worse. |
| MultilineInput seeding | info | MultilineInput does `var text by remember(initialText)`, so passing the live effective prompt would re-key its internal state on every keystroke once EditPrompt round-trips through FrozenFrame.promptOverride. | Seeded with `remember(isEditing) { effective }` — captured once when the toggle flips, stable for the editing session. A real bug avoided, not a style choice. |
| nodeIdFor | warn | nodeIdFor is STUBBED as `{ null }`, so prompt annotation blocks currently print '#N 840x254' with no node-id segment. | This is buildPrompt's documented no-candidate contract (segment and separator both dropped, no 'View (no match)' sentinel), so the output is honest rather than broken — but it IS a stub. T-9 must supply a real resolver over the hierarchy param using the same snapTarget arithmetic the measure panel uses, and remove the ponytail: comment plus the 'Not read yet' KDoc sentence. hierarchy is already a remember key, so no key-set change is needed. Logged as a T-9 carry-forward. |
| module dependency | info | presentation-feature-mirror declares projects.domainCore as `implementation`, yet MirrorPayloadTab is public with HierarchySnapshotModel in its signature. | Fine today — its only caller MirrorHost is in-module, and MirrorUiState already leaks domain-core types the same way. Flagged as a T-11 carry-forward: if the widened MirrorHost lambda ever has to be satisfied from outside this module, that dependency must become `api`. No build file touched. |
| @Preview deprecation | info | Two 'annotation class Preview is deprecated' warnings on the new previews. | Pre-existing and repo-wide — MirrorNotesTab.kt emits the identical pair from the same org.jetbrains.compose.ui.tooling.preview.Preview import. Matched the sibling rather than diverging in one new file. |

### registryUpdates

| registry | entry | change |
| --- | --- | --- |
| components.json | mirror-payload-tab | NEW entry. MirrorPayloadTab(state: MirrorUiState, hierarchy: HierarchySnapshotModel, onIntent: (MirrorIntent) -> Unit, modifier: Modifier = Modifier). PayloadPromptSection stays private and unregistered. |

### needsHumanReview

#### required

false

#### reason

Gates green and all five DoD lines independently verified by the orchestrator. Visual evidence is the populated/empty preview pair; no UI-test harness exists in this repo.

### linkedAC

- AC-13
- AC-77
- AC-79

### backlogUpdate

#### taskId

T-7

#### status

done

### coverage

skipped (no UI-test harness in this repo)
