# rectify — review

## Envelope

| field | value |
| --- | --- |
| stage | review |
| step | review |
| status | done |
| producedBy | sdd-prd-review-consolidate |
| schemaVersion | 1 |
| createdAt | 2026-10-11T00:00:00Z |
| updatedAt | 2026-10-11T16:12:36Z |

## Gate

### type

human

### approved

true

### approvedBy

human

### at

2026-10-11T00:00:00Z

## Provenance

### selfHash

492392189546e2c4965dbf8248a6ba20fd59a1375b7bea11900d9ae916187d71

### inputs

| artifact | hash |
| --- | --- |
| prd.json | a7b4b0d1c39a04a00aec0acd67900ad85595779106bea6f82ae1265133169110 |

## Data

### verdict

approve-with-nits

### findings

| id | dimension | severity | target | description | verified | resolution |
| --- | --- | --- | --- | --- | --- | --- |
| F-COMPLETE-1 | completeness | major | traceabilityMatrix rows (GOAL-1, GOAL-3, GOAL-4, GOAL-5) | Several traceabilityMatrix rows omitted FR/story ids that belong to a feature already listed in that same row (GOAL-3's row listed F-4 but omitted FR-9; GOAL-4's row listed F-6 but omitted FR-12/13/14/15 and US-6; GOAL-5's row listed F-2 but omitted FR-3/FR-4/FR-22, and listed F-3 but omitted FR-24; GOAL-1's row listed F-2 but omitted US-13). | true | fixed-in-finalize |
| F-CLARITY-1 | clarity | major | features[F-9].description; uxFlows['History review and closed-loop verify'] step 2 | F-9.description and the Verify uxFlow step 2 still said Verify requires a batch with status 'Fixed', contradicting the Stage-4-survey-confirmed FR-21/AC-23 wording that Verify is available on any sent batch, not gated on status. | true | fixed-in-finalize |
| FEA-1 | feasibility | major | features[F-9].estimate | F-9 bundles three independently-buildable deliverables (pixel-diff engine, hierarchy-diff engine, Verify UX/orchestration) plus unresolved threshold-calibration research into one XL SDD-candidate feature — too large for one spec. Suggested split: F-9a (pixel diff engine), F-9b (hierarchy diff engine), F-9c (Verify UX & re-stage orchestration, depends on F-9a+F-9b). | true | tracked-as-change-request |
| TEST-1 | testability | major | NFR-1, NFR-2, NFR-11 (and AC-25/AC-10/AC-11/AC-13) | NFR-1/NFR-2/NFR-11 have no AC asserting their numeric timing targets are actually met; the referenced ACs only check functional display/behavior, not timing. | true | tracked-as-change-request |
| TEST-2 | testability | major | MET-2, MET-3 (and AC-23/AC-24) | MET-2/MET-3 (rate-based success metrics) have no AC or stated protocol defining HOW the rate would be measured (e.g. labeled before/after corpus, sampling methodology); AC-23/AC-24 only cover single-annotation mechanical behavior. | true | tracked-as-change-request |
| F-CLARITY-2 | clarity | minor | FR-9 / AC-9 | FR-9/AC-9 give no concrete trigger threshold for when popovers 'would crowd/overlap.' | false | open-issue |
| F-CLARITY-3 | clarity | minor | FR-10 / AC-11 | FR-10/AC-11 never define the dp/px threshold that constitutes 'misalignment.' | false | open-issue |
| FEA-4 | feasibility | minor | features[F-10] | F-10 (iOS) assumes a platform-abstraction seam that no PH-1 feature is explicitly scoped to build; its L estimate may be optimistic. | false | open-issue |
| FEA-5 | feasibility | minor | features[F-7] | No FR specifies the actual write-side contract between Rectify and the existing 'rectify' MCP server (F-7's integration interface is underspecified). | false | open-issue |
| TEST-3 | testability | minor | AC-16, AC-25, AC-32 | AC-16, AC-25, AC-32 use verificationMethod 'inspection'/'demo' where a 'test' would be more appropriate (all are concrete, automatable assertions). | false | open-issue |
| TEST-4 | testability | nits | SC-6 | SC-6's steps ('Review generated prompt...', 'Review crops grid...') lack observable assertions a test could be written from directly. | false | open-issue |

### changeRequests

| id | severity | description | target | status |
| --- | --- | --- | --- | --- |
| CR-1 | major | Regenerate traceabilityMatrix[].fr/stories for the GOAL-1/3/4/5 rows as the union of linkedFR/linkedStories across the features already listed in each row. | traceabilityMatrix (GOAL-1, GOAL-3, GOAL-4, GOAL-5 rows) | resolved-at-finalize |
| CR-2 | major | Reword features[F-9].description, the 'History review and closed-loop verify' uxFlow step 2, and SC-8's title to remove the stale 'Fixed'-status gate on Verify, aligning with the Stage-4-confirmed FR-21/AC-23 wording that Verify applies to any sent batch. | features[F-9].description; uxFlows['History review and closed-loop verify'].steps[1]; scenarios[SC-8].title | resolved-at-finalize |
| CR-3 | major | Split F-9 into F-9a (pixel-diff engine), F-9b (hierarchy-diff engine), and F-9c (Verify UX & re-stage orchestration, depends on F-9a+F-9b) so no single SDD-candidate feature bundles three independently-buildable deliverables plus unresolved threshold-calibration research at XL size. | features[F-9] | open — defer to SDD Stage 1/2 spec-sizing for F-9; do not block this PRD's handoff |
| CR-4 | major | Add a dedicated performance acceptance criterion per NFR-1/NFR-2/NFR-11 (or fold a timing assertion into AC-25/AC-10/AC-13) so the stated numeric timing targets are actually testable. | NFR-1, NFR-2, NFR-11 / AC-25, AC-10, AC-11, AC-13 | open — defer to SDD Stage 1/2 spec-sizing; do not block this PRD's handoff |
| CR-5 | major | Add an AC or validation task defining a reproducible measurement protocol (e.g. labeled before/after corpus, sampling methodology) for MET-2/MET-3 before F-9's diff thresholds are spec'd for SDD. | MET-2, MET-3 / AC-23, AC-24 | open — defer to SDD Stage 1/2 spec-sizing for F-9; do not block this PRD's handoff |

### openIssues

- F-CLARITY-2: FR-9/AC-9 give no concrete trigger threshold for when popovers 'would crowd/overlap' — add a numeric or density-based threshold.
- F-CLARITY-3: FR-10/AC-11 never define the dp/px threshold that constitutes 'misalignment' — specify a tolerance value.
- FEA-4: F-10 (iOS) assumes a platform-abstraction seam no PH-1 feature is scoped to build; revisit F-10's L estimate once that seam's owner is identified.
- FEA-5: F-7's write-side contract with the existing 'rectify' MCP server is underspecified — no FR defines the actual integration interface.
- TEST-3: AC-16, AC-25, AC-32 use verificationMethod 'inspection'/'demo' where 'test' would better fit these concrete, automatable assertions.
- TEST-4: SC-6's steps ('Review generated prompt...', 'Review crops grid...') lack observable assertions a test could be written from directly.

### humanRemarks

- Human approved approve-with-nits: ship the PRD now. CR-1 and CR-2 (mechanical, no new decisions) were applied directly to draft/prd.json before finalize. CR-3/CR-4/CR-5 remain open and are deferred to SDD-stage spec sizing for F-9 — they do not block this handoff.

### readyForSdd

true
