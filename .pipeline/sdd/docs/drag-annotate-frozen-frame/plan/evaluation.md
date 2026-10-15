# drag-annotate-frozen-frame — evaluation

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | evaluation |
| status | done |
| producedBy | sdd-build-run-pipeline |
| schemaVersion | 1 |
| createdAt | 2026-10-15T03:34:07Z |
| updatedAt | 2026-10-15T03:34:07Z |

## Gate

### type

auto

### approvedBy

sdd-build-run-pipeline

### approved

true

### at

2026-10-15T03:34:07Z

## Provenance

### selfHash

82a4af496c9accdae585f220cf17ffa8fbddbcc8fb013817cc99ea68fd2ef070

### inputs

| artifact | hash |
| --- | --- |
| plan/candidates/candidate-1.json | 22fa611c8fece6ae41ee67908f43ff1936d0abc0986a9dfd6a79156e7112dd22 |

## Data

### candidates

| id | score | rationale |
| --- | --- | --- |
| candidate-1 | 0 | Sole candidate: fast tier sets planDrafts=1, so the judge step is skipped per the Stage 2 contract. |

### selected

candidate-1

### rationale

fast effort tier => planDrafts=1 => no evaluate spawn. Synthesized by the orchestrator so the resume/schema contract holds.

### corrections


### deletedCandidates
