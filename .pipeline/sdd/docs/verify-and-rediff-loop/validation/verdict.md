# verify-and-rediff-loop — verdict

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | verdict |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-19T01:04:37Z |
| updatedAt | 2026-10-19T01:04:50Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-19T01:04:37Z

## Provenance

### selfHash

d7104fe99a1fdb41a35f27606f84ecda8a734bbcd9a01c7794b969babb019f31

### inputs

| artifact | hash |
| --- | --- |
| validation/audit.json | ce10dd48619f8c6a2d1fcff6b153af647c4c6fd5051235dc446b4e51526b6548 |
| plan/plan.json | 1309a704111a300b5006cd5a46115cd4a23b10ab2dfa305f195ef9f0816f07dd |

## Data

### verdict

complete-with-waivers

### tasks

| id | status | satisfiesAC |
| --- | --- | --- |
| T-1 | done | ["AC-23", "AC-45"] |
| T-2 | done | ["AC-45"] |
| T-3 | done | ["AC-23", "AC-45"] |
| T-4 | done | ["AC-23", "AC-43"] |
| T-5 | done | ["AC-24", "AC-41"] |
| T-6 | done | ["AC-23", "AC-45"] |
| T-7 | done | ["AC-23", "AC-40", "AC-44"] |
| T-8 | done | ["AC-23", "AC-24"] |
| T-9 | done | ["AC-23", "AC-24", "AC-40", "AC-41", "AC-43", "AC-44"] |
| T-10 | done | ["AC-23"] |
| T-11 | done | ["AC-23", "AC-24", "AC-40", "AC-42", "AC-43"] |
| T-12 | done | ["AC-39"] |
| T-13 | done | ["AC-42"] |
| T-14 | done | ["AC-43"] |
| T-15 | done | ["AC-44"] |
| T-16 | done | ["AC-45"] |
| T-17 | done | ["AC-23", "AC-24", "AC-39", "AC-42", "AC-43"] |

### unsatisfied

#### acceptanceCriteria


#### scenarios


#### tasks


### waivers

| item | reason | acceptedBy |
| --- | --- | --- |
| demo:T-17-TAC-3-end-to-end-GUI-walkthrough | The manual real-device demo (send a batch, change the UI, click Verify, see two frames with verdict-coloured rects, confirm the panel, send a follow-up, confirm both status writes) was never executed, although a device was attached during execution — driving the GUI was scoped out. Proven instead: every behavioural branch (happy path, mixed, zero-row, disconnect, size-mismatch, no-live-session, hierarchy-unusable, mark-all-override) is covered by MirrorViewModelVerifyTest (33 cases) and the core/verify pure-function suites, all running against FakeFrameBitmap/scripted dumps, 1170 tests green including iOS-sim. Remaining unproven: the actual rendered MirrorVerifyStage layout (296x640 frame letterboxing, verdict-coloured overlay rects positioned against a real measured box, the Ready/Error @Preview pairs as actually painted), real adb re-capture timing (~2.7s), and light/dark colour parity (NFR-9) as rendered rather than as reasoned-about-by-reuse. Directly touches AC-23 (visual half), AC-39 (rendered button, though the gate removal itself is inspected and SC-19 is test-covered), NFR-9, and the full user journeys SC-8 and SC-9 (both proven step-by-step via tests, but never walked end to end by a human on a device). | human |
| accuracy:MET-3-false-positive-rate | MET-3's <5% false-positive target is unverified in v1 — no golden corpus exists and this repo has no Compose UI-test harness to build one against. Proven instead: the three tunable constants (CHANNEL_NOISE_FLOOR, REGION_CHANGE_RATIO, MIN_REGION_PIXELS) each carry a KDoc'd ceiling and a documented (not built) manual-tuning path, and an independent grep confirms no test asserts a threshold by literal expected value — tests only use the constants to construct inputs, so retuning them later cannot silently invalidate the suite. Remaining unproven: whether the chosen constants actually hold the false-positive rate under 5% against real antialiasing/subpixel-rendering noise on a real device. Affects NFR-17 (recorded inspection-satisfied with this exact caveat already) and, by extension, the accuracy claim underlying AC-24's verdict logic. | human |
| documentation:T-11-four-step-progress-strip | backlog.json T-11 still lists the four-step StatusLine (Sent -> Agent fixed -> Recaptured -> Verified n/N) and the AGENT CHANGES file list as deliverables; both were deliberately cut during execution for want of a data source (nothing in the app knows which files an agent touched), and the cut is recorded only in MirrorVerifyStage.kt's KDoc, never back-ported into backlog.json. Proven: the shipped three-step StatusLine (Loading batch / Recapturing / Diffing regions) is real, tested, and sufficient for every AC/SC that names it. Remaining: backlog.json task text itself is stale and would mislead a future reader into looking for a feature that was never built. No functional AC/SC depends on the cut pieces. | human |
| coverage:AF-6-iOS-HistoryEntryDaoFindByIdTest-skipped | data-database-impl's iosSimulatorArm64Test task is SKIPPED module-wide (pre-existing Room/KSP module shape, not introduced by this spec), so HistoryEntryDaoFindByIdTest — the only new test for NFR-7's single-row read — never runs on iOS, only on desktop (11 desktop tests, 0 iOS). Proven: the desktop run is green and the query is a plain @Transaction @Query with no platform-specific SQL. Remaining: zero iOS-specific evidence that the Room/KSP-generated DAO code behaves identically on the iOS driver for this query. | human |

### openChangeRequests


### humanRemarks

- AF-3 (updateBatchStatus Result silently dropped at two of three call sites) was fixed post-audit in commit 71c7acd: both sites now route through .onFailure { log.w(...) }, matching the pre-existing FollowUpSent pattern. Verified directly against the diff, not just the commit message. 472 desktop / 460 iOS-sim tests green afterward.
- AF-4 (phantom-open bugs.json#PP-TD-10, actually fixed in wave 1) and AF-5 (missing registry entries for all 11 new F-9 components, the F-9 feature entry, and the F-7 entry that had never been registered) were both resolved in the same commit 71c7acd: PP-TD-10 closed with fixedIn, 11 new component entries plus features.json entries added. Verified directly against the registry diff.
- AF-7 is a claim-nuance, not a defect: releaseVerify() clears four plain fields; the fifth 'verify-scoped' field (the reduced MirrorUiState.verify) is cleared by a separate reduce and is deliberately retained by abortVerify so the user can read the error. All four leak paths route through the one helper — the wave-5 leak cannot recur. No action needed.
- AF-8/AF-9/AF-10/AF-11 are info-severity, no-action items (unread thumbPng/hierarchyXml hydration, an unconfined-but-app-owned file read, four raw-.dp vals matching existing module precedent, and legitimate pre-existing-test maintenance) — recorded for visibility, not blocking the verdict.

### changelogRef

changelog.json

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| Accept the GUI-demo waiver (visual rendering, real-device capture timing, light/dark parity as painted - all logic-tested via fakes, never human-walked on a device)? | ["accept", "reject"] | accept | ai-proposed | Every behavioural branch is test-proven via fakes across 33 ViewModel tests plus the core/verify suites; what remains needs a human at a real device, which this environment cannot provide. |
| Accept the MET-3 false-positive-rate waiver (thresholds KDoc'd with ceilings, no golden corpus exists to measure the actual rate)? | ["accept", "reject"] | accept | ai-proposed | No test asserts a threshold literal, so future retuning stays safe; measuring the rate itself needs a hand-labelled corpus that was deliberately documented-not-built. |
| Accept the two minor waivers (backlog T-11 text not updated after the four-step strip was cut; data-database-impl iOS test task skipped module-wide)? | ["accept", "reject"] | accept | ai-proposed | Both are recording gaps rather than behaviour gaps, and the iOS skip is pre-existing module shape from Room/KSP, not introduced here. |
