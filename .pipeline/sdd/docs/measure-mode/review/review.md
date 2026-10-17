# measure-mode — review

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | review |
| status | done |
| producedBy | orchestrator-under-operator-delegation |
| schemaVersion | 1 |
| createdAt | 2026-10-17T09:06:32Z |
| updatedAt | 2026-10-17T09:06:32Z |

## Gate

### type

human

### approvedBy

orchestrator-under-operator-delegation

### approved

true

### at

2026-10-17T09:06:32Z

## Provenance

### selfHash

03b287d056543ab648a0f1eb38e5ec97e8311f57367e75cd2292142d91b4dd7e

### inputs

| artifact | hash |
| --- | --- |
| validation/verdict.json | d89995b840ce1bd1b758ea24493424c5346926eb89ed4a065228e312f7999e84 |

## Data

### verdict

approve-with-nits

### summary

One merged reviewer over the aggregate diff of 62 files / 4801 insertions (effort tier fast: reviewPanel=1, findingVerify=none), run by the orchestrator under the operator's delegation. The measure algorithm was read line-by-line against the design's own recovered measure(a,u) rather than against a paraphrase of it, and the convention boundaries were checked mechanically. One real defect was found, fixed and red-proved during the review; the rest are nits and recorded debt. The architecture holds: 0 new modules, 0 new dependencies, one new internal Compose-free package, and the three Measure* components wired rather than re-authored as the plan intended.

### blocking


### major

- FIXED DURING REVIEW - hierarchyStatus reported Ready for a hierarchy nothing could use. The status was derived from the dump command's outcome alone, so a dump that succeeded but whose own frame size disagreed with the frozen frame reduced Ready while projectMeasureNodes discarded that snapshot wholesale and every measure silently fell back to View (no match). Same defect class as the indicator dot that said one thing while the label beside it read FROZEN, and as the pendency gap AC-72 exists to close. Reachable whenever the device's geometry changes inside the ~2.7s dump window, and T-1 measured a 9.76 percent gap between what uiautomator reports and the physical screen. Fixed at dump resolution rather than at measure time, since the frozen frame's size is already known when the dump lands. Proved by reverting the fix and watching the new test fail (MirrorViewModelMeasureTest.aDumpWhoseFrameDisagreesWithTheFrozenFrameReducesUnavailableNotReady). 198 tests green on desktop and iosSimulatorArm64 after.

### minor

- neighboursOf excludes the target with structural inequality (it != target) where the design uses identity (e !== t). Two structurally identical nodes would both be dropped. Unreachable in the real pipeline because the four-step node-id chain uniquifies ids before a MeasureNode is ever built, so this is a latent difference rather than a bug - but it is a difference from the authority, and worth a comment at the call site.
- findingOf's non-zero guard reads the CONVERTED delta (topDelta in 1..10) where the design guards the RAW value (L.y !== t.y). A 1px drift at 420 dpi converts to 0.38dp, rounds to 0, and is therefore treated as aligned rather than reported as a nonsensical 'Δ0dp'. Deliberate, and the frozen test name says so (aDriftTooSmallToShowAsANonZeroFigureIsTreatedAsAlignedRatherThanReportedAsZero) - recorded so a future reader does not 'fix' it back toward the design and reintroduce Δ0.

### nits

- TOUCH_TARGET_MIN_DP is internal while TOP_MISALIGNMENT_MAX and LEFT_MISALIGNMENT_MAX beside it are private. Only the first is read from a test, so the visibilities are honest, but the inconsistency reads as an oversight.
- core/measure holds 32 internal and 14 private declarations and zero public ones, which is exactly what package-layout.md asks of a core/ package. Noted as verified rather than as a problem.

### changeRequests


### techDebt

- The dumpJob cancellation path in releaseHierarchy() has no test. A late dump resolving about a released frame would not be caught; the symptom is a stale status, visible and minor. Reported honestly by its implementer rather than discovered here.
- The design's max(h, 1) neighbour-height floor is unreachable from the oracle fixture because no ELS node has h == 0. Implemented because the design specifies it, unexercised because the authority cannot reach it.
- Mid-axis guide endpoints are asserted against the design's own H/V helpers, but the sub-pixel case is not: the design's mid-y is a real-valued 206.5 on an odd-height target and the implementation truncates to 206, because IntOffset cannot carry half a pixel. Whether that reads as centred on screen is MC-4's business.
- presentation-feature-mirror/di/PresentationFeatureMirrorModule.kt (2 lines) fell outside T-10's declared predictedScope. The new constructor parameter is an interface and so cannot carry a default, which means the app would not compile without it. No new binding was introduced. The clean desktop-app boot verified at T-13 is what proves Koin resolves it at runtime - no unit test covers that.

### followUps

- Run execution/MANUAL-CHECKS.md (MC-1..MC-5). Four acceptance criteria are BLOCKED on them, not waived, and the single most valuable one is MC-5's FLAG_SECURE case: the T-1 spike deliberately never attempted a secure screen, which is the input most likely to return an empty dump in the field and the one AC-69 most depends on.
- The misalignment-threshold bug (raw px vs converted units) was invisible at 160 dpi because px and dp are identical there. Every test in this project that involves density should state its dpi deliberately - a suite that only ever uses the baseline cannot distinguish a unit error from correct behaviour. Worth raising in testing.md rather than leaving as spec-local knowledge.
- MeasureCard's shipped @Preview carried a value its own algorithm cannot produce (Top 9dp against the oracle's 15), and four of its five matching values made it look trustworthy. A preview is illustration, not specification - worth saying so in compose-preview-convention.md.
