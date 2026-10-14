# rectify — evaluation

## Envelope

| field | value |
| --- | --- |
| stage | draft |
| step | evaluation |
| status | done |
| producedBy | sdd-prd-run-pipeline |
| schemaVersion | 1 |
| createdAt | 2026-10-11T00:00:00Z |
| updatedAt | 2026-10-11T12:35:06Z |

## Gate

### type

auto

### approved

true

### approvedBy

sdd-prd-run-pipeline

## Provenance

### selfHash

91dc6c43257980013d275db0941883858e348728fc17391964914c44efc1b1da

### inputs

| artifact | hash |
| --- | --- |
| candidate-1.json | c89691ed2141b59c4f6e584f8e145988cc36d34feff86704c16ca395fff4571e |

## Data

### candidates

| index | title | score |
| --- | --- | --- |
| 0 | Rectify — Live Device Annotation & Agent Handoff for Mobile UI Bugs | 1.0 |

### selectedIndex

0

### scores


### corrections


### rationale

fast effort tier (prdDrafts=1): only one candidate was composed, so the competitive judge was skipped per the pipeline's N==1 rule. This candidate is the sole input to refine, with no corrections — refine still runs its traceability re-check and finalization pass.
