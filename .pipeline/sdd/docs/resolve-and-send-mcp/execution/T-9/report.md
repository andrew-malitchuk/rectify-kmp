# resolve-and-send-mcp — T-9

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-9 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-18T15:08:33Z |
| updatedAt | 2026-10-18T15:08:33Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-18T15:08:33Z

## Provenance

### selfHash

2833dec1d82846e5d03b60141f2e958bec1cd489b81f811bc56675962df326b9

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 13736f4c3b002eb7483a12681dfff64471a26926e7be014daa6853d369d3d83b |

## Data

### taskId

T-9

### type

business-logic

### outcome

success

### touchedPaths

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/di/PresentationFeatureMirrorModule.kt
- presentation-feature-mirror/build.gradle.kts
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelResolveAndSendTest.kt
- presentation-feature-mirror/src/desktopTest/kotlin/presentation/feature/mirror/MirrorViewModelResolveAndSendDesktopTest.kt

### tdd

#### testsFrozen

true

#### cycle

green

#### testsWrittenBy

sdd-build-business-test

#### implementedBy

sdd-build-business-implement (wave-4)

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :data-handoff-api:desktopTest :mcp-application:test

#### passed

true

#### output

```
BUILD SUCCESSFUL in 6s
presentation-feature-mirror:desktopTest tests=347 failures=0 errors=0
  MirrorViewModelResolveAndSendTest tests=5 failures=0 (TAC-1..5)
  MirrorViewModelResolveAndSendDesktopTest tests=2 failures=0 (TAC-6/7)
data-handoff-api:desktopTest tests=14 failures=0
mcp-application:test tests=19 failures=0
```

### implementation

#### summary

MirrorIntent.ResolveAndSend routes to a real resolveAndSend(): canSend guard, then a SEPARATE currentFrozenHeld guard reducing SendPhase.Failed plus a Kermit warning, then Preparing -> off-main assembly -> Publishing -> handoff.publish, timed with the injected elapsed seam. Success reads the count off the frame that was sent (pre-reset by construction), reduces Sent, resumes live unless MirrorSession.Lost (SC-27, where it only empties the batch), and posts SendComplete with toastLabel. Failure reduces Failed and touches nothing else, so UnsupportedAnnotationHandoff.publish throwing on Android/iOS is a visible failure rather than a crash. Refactor: clearStaged's body became a shared MirrorUiState.withoutStagedBatch() used by both clear and the Lost-session success path.

#### filesChanged

- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/di/PresentationFeatureMirrorModule.kt
- presentation-feature-mirror/build.gradle.kts
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorViewModelResolveAndSendTest.kt
- presentation-feature-mirror/src/desktopTest/kotlin/presentation/feature/mirror/MirrorViewModelResolveAndSendDesktopTest.kt

### validations

| stage | passed | iterations | issues | note |
| --- | --- | --- | --- | --- |
| tests | true | 1 | [] |  |
| cross-target-compile | true | 1 | [] | ./gradlew :presentation-feature-mirror:compileTestKotlinIosArm64 -> BUILD SUCCESSFUL, no e: |
| AS-30-dispatcher-review | true | 1 | [] | ORCHESTRATOR-VERIFIED BY READING THE SOURCE, because the TAC-7 test cannot prove this on its own. Two explicit withContext(Dispatchers.Default) blocks in resolveAndSend(): MirrorViewModel.kt:795 wraps the whole assembleEnvelope(...) call (every encodeCrop.encode runs inside it, since the curried lambda is only invoked from assembleEnvelope's annotation loop) and :811 wraps handoff.publish (the file-slot write). reduce(Publishing) at :810 and the final reduces/postSideEffect run back on the intent's own context. Matches the module's established decodePng hop; there is no injected IO dispatcher here. |
| placeholder-removed | true | 1 | [] | ORCHESTRATOR-VERIFIED: grep -rn 'TODO(T-9)' presentation-feature-mirror/src/ returns no matches. The wave-2 placeholder branch was replaced, not appended to. |
| koin-style | true | 1 | [] | ORCHESTRATOR-VERIFIED: PresentationFeatureMirrorModule.kt:31 calls provideHandoffSources(); :32 uses viewModel { } with explicit get<T>(), never viewModelOf. |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| T-9 | LATENT RUNTIME CRASH CAUGHT AT IMPLEMENTATION, missed by every prior gate: provideHandoffSources() was never wired into presentationFeatureMirrorModule, so get<AnnotationHandoff>() would have thrown NoDefinitionFound at runtime. It compiled and passed tests because T-5's HandoffSourcesProviderTest registers the provider in a throwaway module, stepping over the real graph. Fixed here. | provideHandoffSources() added to the module; T-10 must not re-add it | warn |
| T-9 | FOR T-10: SendPhase is left terminal (Sent/Failed) after a send - nothing resets it to Idle. canSend is unaffected (neither value is in-flight), but if the status overlay is driven off send != Idle, the UI needs its own dismissal or T-10 must add a reset intent. | Handed to T-10 | warn |
| T-9 | FOR T-10: on a Lost-session success the frame stays pinned with an empty batch, so the Resolve & Send button correctly goes disabled while the frozen frame is still on the stage. | Handed to T-10 | info |
| T-9 | TAC-7's test is only weakly probative, as the red author stated plainly: it shows the work avoided the thread driving Orbit's pinned test dispatcher, but a correct implementation could satisfy it by hopping to any dispatcher. The real guarantee was confirmed by source review (see the AS-30 validation entry), not by the assertion. | Verified by review | info |
| T-9 | nowMs and elapsed stay at their constructor defaults in Koin - no bindings exist for () -> Long / TimeSource and inventing them would be two singletons nothing else reads. handoff and encodeCrop are the two injected seams. | Deliberate | info |
| T-9 | presentation-feature-mirror gained implementation(libs.kermit) - the module had no logger dependency. Precedent: presentation-core-ui, data-repository-impl. | New module dependency, no new catalog entry | info |

### linkedAC

- AC-17
- AC-18
- AC-20
- AC-21
- AS-30
- AS-31
- SC-25
- SC-27
- FR-19

### backlogUpdate

#### taskId

T-9

#### status

done

### needsHumanReview

#### required

true

#### reason

A latent Koin wiring gap (missing provideHandoffSources) would have crashed at runtime despite green tests - worth a reviewer's eye on whether other provide* hooks are similarly unwired.
