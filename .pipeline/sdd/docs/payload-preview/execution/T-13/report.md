# payload-preview — T-13/report

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-13/report |
| status | done |
| producedBy | sdd-build-test-audit |
| schemaVersion | 1 |
| createdAt | 2026-10-17T20:25:29Z |
| updatedAt | 2026-10-17T20:25:29Z |

## Gate

### type

auto

### approvedBy

orchestrator-verified

### approved

true

### at

2026-10-17T20:25:29Z

## Provenance

### selfHash

2efdaec5d64a40c8352d9c9bd3cef63c5a658eb5fca138a3938804aa9b53220a

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | a0d78474590386b4950f70a3807ae7a5e9006ae680e326103a8e1d652c3aef23 |

## Data

### taskId

T-13

### type

test

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/PayloadGate.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/PayloadGateTest.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorPayloadTab.kt

### tdd

#### cycle

green

#### testsFrozen

true

#### testsWritten

- PayloadGateTest.liveStreamWithNoPinnedFrameHasNoPayload
- PayloadGateTest.pinnedFrameWithNoStagedAnnotationsHasNoPayload
- PayloadGateTest.pinnedFrameWithOneStagedAnnotationHasPayload

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :presentation-core-ui:desktopTest --rerun  then  :presentation-feature-mirror:build :presentation-core-ui:build --max-workers=1

#### passed

true

#### output

TAC-2 PASS: mirror desktopTest 251 tests, 0 failures, 0 errors (248 -> 251 with the new PayloadGateTest 3/3); core-ui 29, 0 failures. TAC-3 PASS: cross-target :build BUILD SUCCESSFUL in 3m18s serialized, and the iosSimulatorArm64 suite GENUINELY RAN at mirror 251/0 and core-ui 29/0 on real Kotlin/Native - the gate that has previously hidden a real failure in this project because Orbit's dispatcher defaults are process-wide. The parallel :build fails with 'e: Compilation failed: Java heap space' on linkReleaseFramework* against a 4 GiB daemon heap; --max-workers=1 is the established workaround for that pre-existing infra issue.

### implementation

#### filesChanged

- core/payload/PayloadGate.kt (new)
- commonTest/PayloadGateTest.kt (new)
- source/mirror/MirrorPayloadTab.kt (gate re-pointed)

#### summary

Verification sweep plus the one gap it found. The audit produced a 13-row AC traceability table and a line-by-line plan DoD sweep: 6 ACs clean automated passes (AC-16, 73, 74, 75, 76, 80), 6 split between an automated logic half and a preview-only UI half (AC-13, 14, 15, 72, 78, 79), and ONE real gap - AC-77 had ZERO automated assertions despite its plan verificationMethod being 'test'. Fixed on the engineer's decision: the empty-state predicate was lifted out of the composable into a named internal fun hasPayload(frozen) in core/payload/, with three unit tests. It carries a kotlin.contracts contract so a true result smart-casts frozen to non-null exactly as before - without that, lifting the condition would have cost every later frozen. access a !!, trading one untestable if for a scattering of non-null assertions. MirrorPayloadTab's gate now reads `if (!hasPayload(frozen))`, unchanged in behaviour and still the first statement of the body.

### validations

| stage | passed | iterations | issues |
| --- | --- | --- | --- |
| tac-1-ac-traceability | true | 1 | ["All 13 ACs trace to a task and to evidence. 6 clean automated; 6 automated-logic + preview-only UI half; AC-77 was the one gap and is now closed by PayloadGateTest.", "AC-80's primary evidence is the Unavailable preview pair by explicit Stage 1 engineer decision (a forced real-device dump failure is not reproducible on demand) - confirmed present and correctly wired with hierarchyStatus = Unavailable and a non-null frame."] |
| tac-2-desktop-tests | true | 1 | ["mirror 251/0/0, core-ui 29/0/0, forced --rerun."] |
| tac-3-cross-target | true | 2 | ["Parallel :build OOMs on the Kotlin/Native release linker; serialized BUILD SUCCESSFUL with the iosSimulatorArm64 suite actually executing at 251/0 and 29/0."] |
| tac-4-test-names | true | 1 | ["Zero backtick fun names across all 8 touched commonTest files, so the comma/colon hazard that breaks compileTestKotlinIosArm64 cannot exist by construction."] |
| tac-5-untouched-shell | true | 1 | ["RectifyWindow.kt's diff is one import, the lambda destructuring, one when-branch, a rewritten stale comment and one tabCounts entry. MirrorNotesTab.kt, SidePanelFrame.kt, SidebarTabs.kt and ShellTab.kt (which holds shortcutToTab) are byte-unchanged."] |
| tac-6-no-new-infrastructure | true | 1 | ["No settings.gradle.kts, no */build.gradle.kts, no libs.versions.toml, no gradle.properties, no DI/*Module.kt, no ThemeColor/presentation-core-styling change. The three new fields are on FrozenFrame (MirrorContract.kt:553-555); MirrorUiState retains its original 15 with none leaked in."] |
| plan-dod-sweep | true | 1 | ["10/10 lines pass. One wording nit: the FrozenFrame KDoc conveys the 'captured at freeze, dies with the frame' admission rule semantically but not as that literal phrase.", "One wording nit: the DoD says RectifyWindow gains an 'if' branch; the real construct is a `when` branch - same minimal-diff intent."] |
| registry-reconciliation | true | 1 | ["All three applied deltas verified byte-exact against source: hierarchy-row's matched param (HierarchyRow.kt:67), mirror-host's 4-arg content lambda, and the new mirror-payload-tab entry (MirrorPayloadTab.kt:193-199)."] |

### troubleshooting

| at | severity | issue | resolution |
| --- | --- | --- | --- |
| AC-77 gap | warn | AC-77 ('zero staged annotations renders ONLY the empty-state copy') had verificationMethod 'test' but zero automated assertions - its gate was an inline `if` inside the @Composable, and this repo has no Compose UI-test harness. A test re-writing the same boolean would have asserted against itself and proved nothing. | RESOLVED on the engineer's decision at the gate: predicate extracted to core/payload/hasPayload with three unit tests covering the three states the tab can be opened in. The middle one is load-bearing - a non-null frame with an empty annotation list, the moment after ClearStaged, which an isEmpty()-only check gets wrong. Knowingly extended T-13 past its declared commonTest/** scope into commonMain; authorised and recorded so Stage 5 does not read it as drift. |
| smart-cast regression | info | Lifting the condition out of the composable broke Kotlin's smart cast: the inline `frozen == null \|\|` had narrowed frozen to non-null after the early return, so six later accesses failed to compile with UNSAFE_CALL. | Added a kotlin.contracts contract ('returns(true) implies (frozen != null)'), the same mechanism stdlib's isNullOrEmpty uses. Smart cast restored, call site unchanged, no !! introduced anywhere. Caught by the compiler on the first run, not shipped. |
| cross-target build | warn | The parallel :build still fails with Java heap space on linkReleaseFramework*. | Pre-existing, fully characterized infra issue unrelated to this spec; --max-workers=1 passes. A gradle.properties fix (kotlin.native.linkerThreads or a higher daemon -Xmx) belongs in a standalone infra task - deliberately not done here, since gradle.properties is shared and TAC-6 forbids touching it. |

### registryUpdates


### needsHumanReview

#### required

true

#### reason

Stage 4 is complete and every gate is green, but a set of items genuinely needs human eyes at the Stage 5 gate: (1) six ACs (13, 14, 15, 72, 78, 79) have an automated logic half but their actual Compose rendering/interaction is proven only by the 6 @Preview pairs, which nobody has opened yet; (2) AC-80's primary evidence is a preview pair by design; (3) T-12's two recorded design divergences (prompt body monoData 11sp vs the figure's 10.5px; Crops frame tag ink3 vs the implied ink2) need a designer's ruling; (4) HV-1, the real-device manual demo over SC-6/SC-20..24, is owed at that gate by explicit engineer decision.

### linkedAC

- AC-13
- AC-14
- AC-15
- AC-16
- AC-72
- AC-73
- AC-74
- AC-75
- AC-76
- AC-77
- AC-78
- AC-79
- AC-80

### backlogUpdate

#### taskId

T-13

#### status

done

### coverage

skipped (config.coverageTool = none)

### missingTestsFound

- AC-77 empty-state gate had no automated assertion (closed by PayloadGateTest)
- AC-14's literal 'from frame ' caption string has no automated assertion - preview-only, low severity since frameId stamping itself is tested
- AC-79's click-to-edit toggle UI has no automated assertion - preview-only, low severity since promptOverride persistence is tested rigorously

### testsAdded

- PayloadGateTest (3 tests)
