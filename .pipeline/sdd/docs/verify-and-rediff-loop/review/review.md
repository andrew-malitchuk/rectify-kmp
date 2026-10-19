# verify-and-rediff-loop — review

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | review |
| status | done |
| producedBy | sdd-build-correctness-review |
| schemaVersion | 1 |
| createdAt | 2026-10-19T01:30:53Z |
| updatedAt | 2026-10-19T01:30:53Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T01:30:53Z

## Provenance

### selfHash

8036d22e8515bbb815985986adaac9d605d7ebefac710c65077266fd4f40b5ad

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | d7104fe99a1fdb41a35f27606f84ecda8a734bbcd9a01c7794b969babb019f31 |

## Data

### verdict

approve-with-nits

### reviewers

| dimension | agent | findingCount |
| --- | --- | --- |
| merged (correctness+concurrency+architecture+conventions+performance+security+maintainability) | sdd-build-correctness-review | 7 |

### summary

Single merged reviewer at fast tier over the aggregate diff. The read path is clean and correctly layered, core/verify honours the ImageBitmap single-file rule, and conventions are genuinely clean (no literal colours, no inline TextStyle, no raw dp at a call site). The original verdict was request-changes on COR-1, a real cancellation defect the 1170-test suite structurally could not catch. All seven findings were fixed in commit f789015 and the verdict is revised to approve-with-nits.

### findings

| id | severity | dimension | file | line | rule | summary | failureScenario | status | fixedIn | note |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| COR-1 | blocking | correctness | domain-usecase-impl/src/commonMain/kotlin/domain/usecase/impl/core/ResultLauncher.kt | 25 | AC-44 / SC-21 / NFR-16 | Cancelling verifyJob did not stop the run: resultLauncher caught Throwable and swallowed CancellationException, and Orbit's reduce is a non-suspending StateFlow.update with no cancellation check. | Verify parks in the ~2.7s dump; the user presses Back-to-live; exitVerify cancels and clears, then the cancelled run resumes, sees a null snapshot and calls abortVerify(Disconnected) - throwing the user back into the Verify stage reading 'The device went away' for a device that never dropped. On a real device adb's blocking read is not interruptible at all, so the dump succeeds, the abandoned run walks steps 5-7, republishes two full-frame bitmaps after the exit that existed to drop them, and writes Verified to the database for a run the user abandoned. | fixed | f789015 | Fixed at both layers. The red-then-green proof is the important part: once the fakes were made to swallow cancellation the way production did, two PRE-EXISTING tests failed - they had been passing on the fakes' better manners. |
| COR-2 | major | correctness | compose-application/src/desktopMain/kotlin/dev/rectify/io/source/window/RectifyWindow.kt | 230 | A11Y-2 / TAC-3 | canSendGate was never taught about the Verify stage, so the toolbar Send button and the shortcut it advertises dispatched two different intents. | Freeze a frame, stage two annotations, Verify an old batch, press the shortcut: sendFollowUp overwrites the frozen frame and silently destroys the staged annotations, under a label describing the opposite action. Click the toolbar Send instead and Preparing/Publishing/Failed all render behind the verify stage, invisible. | fixed | f789015 |  |
| COR-3 | minor | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorVerifyStage.kt | 144 | NFR-15 | The stage branched progress-vs-result on frames == null, but the frames publish at the START of Diffing, before any row is computed. | For the whole diff window the panel shows empty frame columns and reads '0 of 0 fixed' with Mark-all-as-verified enabled, asserting a result that does not exist. | fixed | f789015 |  |
| COR-4 | minor | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | 537 | SC-23 | deviceLabel was seeded from the live selection and only replaced with the batch's own label after the NoLiveSession abort. | App opened with history and no device selected - the primary NoLiveSession case - renders 'Nothing is streaming from , so there is no fresh frame to compare against.' with a blank where the device name belongs. | fixed | f789015 | The fixture hid it too: phone() and sentBatch() both used 'Pixel 8 Pro', so the obvious assertion would have passed against the live name. |
| COR-5 | minor | correctness | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/verify/VerifyVerdict.kt | 40 | AC-24 as amended | regionPx == 0 is the documented no-region-could-be-read sentinel, but the guard treated it as 'too small to measure' and waived the pixel conjunct entirely. | A degenerate or fully-off-frame stored rect yields Fixed on the hierarchy half alone, with provably zero pixel evidence - inverting the very change-evidence correction this spec is built on. | fixed | f789015 |  |
| COR-6 | nit | maintainability | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/verify/BeforeAfterFormat.kt | 23 |  | TOUCH_TARGET_ADVISED_DP = 48 duplicated the literal in MeasureFormat.kt with a KDoc pointing at the other copy. | Two copies of a design constant drift apart. | fixed | f789015 |  |
| COR-7 | nit | maintainability | presentation-feature-mirror/src/commonTest/kotlin/presentation/feature/mirror/MirrorFakes.kt | 404 |  | Two consecutive KDoc blocks on FakeUpdateBatchStatus; the first is dead. | A reader edits the block the compiler ignores. | fixed | f789015 |  |

### changeRequests

| id | about | target | status | resolvedIn |
| --- | --- | --- | --- | --- |
| RCR-1 | COR-1 cancellation reaching a checkpoint, at both the resultLauncher and ViewModel layers | ResultLauncher.kt + MirrorViewModel.kt | resolved | f789015 |
| RCR-2 | COR-2 gate canSend on verify == null | core/send/SendGate.kt + RectifyWindow.kt | resolved | f789015 |
| RCR-3 | COR-3/4/5 one-liners plus the two nits | MirrorVerifyStage.kt, MirrorViewModel.kt, VerifyVerdict.kt, Findings.kt, MirrorFakes.kt | resolved | f789015 |

### techDebt

- ProcessRunner's withTimeoutOrNull choice stays correct after the resultLauncher fix but for the opposite reason - a TimeoutCancellationException would now unwind rather than surface the named timeout prefix the escalation ladder matches on. Comment updated, code deliberately unchanged.
- The write-after-abandon path could not be reproduced in-test (the zombie run unwinds at the diff before reaching the status write); the guard is in, but the evidence stops at the republished pixels.
- MET-3's false-positive rate remains unmeasured - no golden corpus.
- The end-to-end GUI demo was never walked on a real device.
- backlog T-11 still lists the cut four-step progress strip.
- data-database-impl's iOS test task is NO-SOURCE, so the DAO test never runs on iOS.

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| COR-1 showed that cancelling verifyJob did not stop a run in production, and the 1170-test suite could not catch it because the fakes propagated cancellation better than production did. How should the PR proceed? | ["Fix COR-1 + COR-2 before merge", "Fix everything COR-1..COR-5 before merge", "Merge now and file them as follow-ups"] | Fix everything COR-1..COR-5 before merge | ai-proposed | COR-1 writes durable wrong database state and breaks the feature's own documented lifecycle contract. COR-5 inverts the change-evidence semantics the whole spec rests on, and COR-3/4 are user-visible one-liners - batching them was one pass over files already open, and the reviewer explicitly allowed it. |

### approved

true
