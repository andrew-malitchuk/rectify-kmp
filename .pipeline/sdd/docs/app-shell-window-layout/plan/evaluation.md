# app-shell-window-layout — evaluation

## Envelope

| field | value |
| --- | --- |
| stage | plan |
| step | evaluation |
| status | done |
| producedBy | sdd-build-run-pipeline |
| schemaVersion | 1 |
| createdAt | 2026-10-14T18:59:47Z |
| updatedAt | 2026-10-14T18:59:55Z |

## Gate

### type

auto

### approvedBy

sdd-build-run-pipeline

### approved

true

### at

2026-10-14T18:59:55Z

## Provenance

### selfHash

ab51ca4e1e8d6b55e464d6edb69fbe039fbbd5918b8ad59bac5e230e91bf3af2

### inputs

| artifact | hash |
| --- | --- |
| plan/candidates/candidate-1.json | 010a71c30e34b2ead94e019806befba01e1c0e85ff878f3e03969abc08c42431 |

## Data

### candidates

| id | score | rationale |
| --- | --- | --- |
| candidate-1 | 0 | Sole candidate: fast tier sets planDrafts=1, so the competitive judge step is skipped per the Stage 2 contract. No scoring was performed and no candidates were deleted. |

### selected

candidate-1

### rationale

fast effort tier => planDrafts=1 => no sdd-build-plan-evaluate spawn. This evaluation is synthesized by the orchestrator so the resume/schema contract holds; it records no comparative judgement because there was nothing to compare.

### corrections


### deletedCandidates
