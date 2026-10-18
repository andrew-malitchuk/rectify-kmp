# measure-mode — evaluation

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | evaluation |
| status | done |
| producedBy | sdd-build-plan-architecture |
| schemaVersion | 1 |
| createdAt | 2026-10-16T18:16:11Z |
| updatedAt | 2026-10-16T18:16:11Z |

## Gate

### type

auto

### approvedBy

orchestrator

### approved

true

### at

2026-10-16T18:16:11Z

## Provenance

### selfHash

1dd86c7b0e4342008033aba029839ba3b4318a20465bbd79a3f77d11381a88a0

### inputs

| artifact | hash |
| --- | --- |
| plan/candidates/candidate-1.json | feb828f7df347de765a275caebe1c05d94a14bd43bfea9587b180e6dd4b2738f |

## Data

### candidates

| id | file | strengths | weaknesses |
| --- | --- | --- | --- |
| candidate-1 | plan/candidates/candidate-1.json | ["Sole candidate: effort tier fast resolves planDrafts=1, so no competitive judge ran.", "Draft self-reported four points where the Stage 1 architecture does not survive contact with the real code."] | ["Not competitively scored — the refine pass and the mandatory human gate are the only quality backstops at this tier."] |

### selected

candidate-1

### rationale

N=1 under effort tier 'fast' (config.planDrafts=null, $effort_tiers.fast.planDrafts=1). Per the Stage 2 contract the competitive-judge machinery is skipped and this evaluation is synthesized so the resume and schema contracts hold. No scores were computed because nothing was compared.

### corrections


### deletedCandidates
