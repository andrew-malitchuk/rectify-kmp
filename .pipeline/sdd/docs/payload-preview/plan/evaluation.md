# payload-preview — evaluation

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | evaluation |
| status | done |
| producedBy | sdd-build-plan-architecture |
| schemaVersion | 1 |
| createdAt | 2026-10-17T10:56:17Z |
| updatedAt | 2026-10-17T10:56:17Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-17T10:56:17Z

## Provenance

### selfHash

7338002bd711e9bbe61125b0bd17084d512ca709ac8d6a949bbb88cbe3a44126

### inputs

| artifact | hash |
| --- | --- |
| plan/candidates/candidate-1.json | 30a4259da3c3b845ece31857b0d6762fe697d313c03ebde72be0b89a5b99524a |

## Data

### candidates

| id | file | strengths | weaknesses |
| --- | --- | --- | --- |
| candidate-1 | plan/candidates/candidate-1.json | ["Sole candidate: effort tier fast resolves planDrafts=1, so no competitive judge ran.", "All 13 ACs (AC-13..16, AC-72..80) covered with a per-task file scope and a 13-row traceability matrix.", "Settled all six of preparation's open questions rather than deferring them to execution."] | ["Not competitively scored - the refine pass and the mandatory human gate are the only quality backstops at this tier.", "First draft attempt stalled (watchdog, 600s, nothing written); this is the retry, so the plan had one fewer self-review pass than a clean run."] |

### selected

candidate-1

### rationale

N=1 under effort tier 'fast' (config.planDrafts=null, $effort_tiers.fast.planDrafts=1). Per the Stage 2 contract the competitive-judge machinery is skipped and this evaluation is synthesized so the resume and schema contracts hold. No scores were computed because nothing was compared.

### corrections


### deletedCandidates
