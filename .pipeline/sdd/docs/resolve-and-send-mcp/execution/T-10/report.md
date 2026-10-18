# resolve-and-send-mcp — T-10

## Envelope

| field | value |
| --- | --- |
| stage | execution |
| step | T-10 |
| status | done |
| producedBy | sdd-build-business-implement |
| schemaVersion | 1 |
| createdAt | 2026-10-18T15:48:27Z |
| updatedAt | 2026-10-18T15:48:27Z |

## Gate

### type

auto

### approvedBy

sdd-build-business-implement

### approved

true

### at

2026-10-18T15:48:27Z

## Provenance

### selfHash

6a7dca3a8dafc21684d18d6592855d7fcfd38dd308be8bb89a0cd5aab6df0963

### inputs

| artifact | hash |
| --- | --- |
| backlog/backlog.json | 13736f4c3b002eb7483a12681dfff64471a26926e7be014daa6853d369d3d83b |

## Data

### taskId

T-10

### type

business-logic

### outcome

success

### touchedPaths

- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt
- compose-application/src/desktopMain/kotlin/dev/rectify/io/core/clipboard/SystemClipboard.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/send/SendGate.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContent.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/ClearGateTest.kt

### tdd

#### testsFrozen

true

#### cycle

green

#### testsWrittenBy

sdd-build-business-test

#### implementedBy

sdd-build-business-implement (wave-5)

### testRun

#### command

./gradlew :presentation-feature-mirror:desktopTest :data-handoff-api:desktopTest :mcp-application:test

#### passed

true

#### output

```
BUILD SUCCESSFUL in 4s
presentation-feature-mirror tests=353 failures=0 (347 pre-existing + 6 ClearGateTest)
data-handoff-api tests=14 failures=0
mcp-application tests=19 failures=0
```

### implementation

#### summary

canClear(state) = stagedCount > 0 && !send.isInFlight in core/send/SendGate.kt. MirrorUiState gained two public one-line delegates canSend/canClear (aliased imports canSendGate/canClearGate) because the gates are internal to core/ while the toolbar reading them lives in :compose-application; the gate functions remain the single definition. RectifyWindow's stubbed AppButton is now live (enabled=mirror.canSend, busy=send.isInFlight, busyText=Sending..., dispatches ResolveAndSend); Clear moved from stagedCount>0 to mirror.canClear; StackedSnackbarHost newly mounted at the window root; handleShellShortcut extended with Cmd+Enter. MirrorContent gained a private SendStatusOverlay reusing the shipped StatusLine fed by sendSteps/sendTrailing. New desktopMain SystemClipboard.kt (AWT, runCatching + Kermit warning).

#### filesChanged

- compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt
- compose-application/src/desktopMain/kotlin/dev/rectify/io/core/clipboard/SystemClipboard.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/send/SendGate.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContent.kt
- presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt
- presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/ClearGateTest.kt

### validations

| stage | passed | iterations | issues | note |
| --- | --- | --- | --- | --- |
| tests | true | 1 | [] |  |
| compile | true | 1 | [] | ./gradlew :compose-application:compileKotlinDesktop :presentation-feature-mirror:compileTestKotlinIosArm64 -> BUILD SUCCESSFUL. No test wiring added to :compose-application (it has none by design). |
| TAC-4-snackbar-duration-review | true | 1 | [] | ORCHESTRATOR-VERIFIED IN SOURCE because no test can catch this: RectifyWindow.kt:316 passes duration = StackedSnackbarDuration.Long explicitly. The host type is spelled StackedSnakbarHostState (sic) and showSnackbar defaults to Indefinite, so omitting this would mean the toast never dismisses. Long (10s) chosen over Short (4s) so the Open session action stays keyboard-reachable for the toast's whole life (A11Y-2). |
| cmd-enter-wiring-review | true | 1 | [] | ORCHESTRATOR-VERIFIED IN SOURCE: var sendAction by remember { mutableStateOf<(() -> Unit)?>(null) } at RectifyWindow.kt:153 beside shellState, assigned from inside MirrorHost via LaunchedEffect(canSend) at :222, consumed at :498-499 where a null holder returns false and falls through rather than swallowing Return. Correct per the onPreviewKeyEvent-is-a-Window-parameter constraint. |
| design-tokens-review | true | 1 | [] | ORCHESTRATOR-VERIFIED: git diff over the whole wave shows zero added Color(0x..., N.dp or TextStyle(... literals. Overlay fade uses tween(Theme.motion.durations.fastMs); reducedMotion is read from the Theme (attributeMotion zeroes that set), never passed as a boolean parameter (A11Y-6). |

### troubleshooting

| at | issue | resolution | severity |
| --- | --- | --- | --- |
| T-10 | HUMAN DECISION IMPLEMENTED: SendPhase never returned to Idle after Sent/Failed while the overlay is visible whenever send != Idle, so it would have stayed on screen forever after the first send. The user chose reset-at-existing-triggers (no new intent, timer or state field). Implemented in reduced state at stageAnnotation (:462), clearStaged (:660) and resumeLive (:870). | Implemented as chosen | info |
| T-10 | DELIBERATE REFINEMENT OF THAT DECISION, FLAGGED FOR REVIEW: the reset is NOT written into the success reduce (:839/:844), which inlines its own resume and keeps SendPhase.Sent. Resetting there would make AC-19's fourth dot (Sent) unobservable - the very thing the AC asks to display. CONSEQUENCE: on the happy path the completed four-dot line lingers over the now-live stage (its Frozen dot reading stale) until the user's next drag, clear or resume. Accepted by the orchestrator on the grounds that the dots describe what that send did rather than current state, and the toast confirms alongside. If review prefers the instant reset it is one copy - but AC-19's demo evidence goes with it. | Accepted; routed to Stage 6 review and the T-13 manual demo | warn |
| T-10 | Lost-session success (frame pinned, batch emptied, canResumeLive false) is not stuck: the next annotation drag on the still-pinned frame routes through stageAnnotation and clears the overlay; Reconnect restores canResumeLive. Clear is correctly unreachable there at 0 staged, which is why the stageAnnotation reset is the one that matters for SC-27. | Verified reasoning | info |
| T-10 | FOR T-13 MANUAL DEMO - three things nothing here covers: (1) the toast actually dismissing after ~10s; (2) the live Cmd+Enter dispatch matching the button's enablement; (3) whether the top-centre snackbar visually collides with the new top-centre status overlay when a send completes while the overlay is still up - the toast sits above the shell and the overlay inside the stage, so they should stack rather than overlap, but that is unverified by anything but a human. | Handed to T-13 | warn |
| T-10 | TAC-6 partial: the toast @Preview pair lives in compose-application/desktopMain (RectifyWindow.kt). That module has uiToolingPreview and compiles, but this is its FIRST @Preview and it is desktop-only, so Android Studio's preview pane will not render it - a human must eyeball it in the running app. The status-overlay pair in MirrorContent.kt is a normal commonMain pair and renders fine. | Flagged for human preview inspection | warn |
| T-10 | RECORDED DEBT as planned: 'Resolve & Send', 'Sending...', 'Clear', 'Open session' are plain constants in RectifyWindow.kt, not string resources. Localisation was explicitly out of scope for this task. | Known debt | info |

### linkedAC

- AC-17
- AC-19
- AC-20
- FR-16
- FR-18
- FR-58

### backlogUpdate

#### taskId

T-10

#### status

done

### needsHumanReview

#### required

true

#### reason

The completed status line lingers over the live stage after a successful send (deliberate, to keep AC-19's Sent dot observable); plus the desktop-only toast @Preview cannot render in the IDE preview pane.
