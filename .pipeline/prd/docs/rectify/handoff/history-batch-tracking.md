# rectify — history-batch-tracking

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | history-batch-tracking |
| status | done |
| producedBy | prd.py handoff |
| schemaVersion | 1 |
| createdAt | 2026-10-11T16:12:17Z |
| updatedAt | 2026-10-11T16:12:17Z |
| dryRun | false |

## Gate

### type

auto

### approvedBy

prd.py

### approved

true

### at

2026-10-11T16:12:17Z

## Provenance

### inputs

| artifact | hash |
| --- | --- |
| prd.json | a7b4b0d1c39a04a00aec0acd67900ad85595779106bea6f82ae1265133169110 |

### selfHash

52cb69f1e98ef5a10756195afec4ca9c7cde66751e3051d18851cea89e0f2e4d

## Data

### sourcePrd

rectify

### id

F-8

### slug

history-batch-tracking

### title

History & Batch Tracking

### goalStatement

The History tab plus the local persistence layer behind it: every batch ever sent is recorded with its frozen frame, per-annotation records, generated prompt and hierarchy dump, and listed grouped by time period with a thumbnail crop, title/summary, timestamp and status chip (Fixed, In progress, etc.). All records survive app restarts and never leave the machine. Why: PER-2 needs to see what has already been reported and its state without re-testing everything, and the Verify loop needs a durable original-frame record to diff against.

### rawDescription

The History tab plus the local persistence layer behind it: every batch ever sent is recorded with its frozen frame, per-annotation records, generated prompt and hierarchy dump, and listed grouped by time period with a thumbnail crop, title/summary, timestamp and status chip (Fixed, In progress, etc.). All records survive app restarts and never leave the machine. Why: PER-2 needs to see what has already been reported and its state without re-testing everything, and the Verify loop needs a durable original-frame record to diff against.

### priority

must

### phase

PH-1

### estimate

M

### dependsOn

- F-7

### linkedGoals

- GOAL-2
- GOAL-3

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-20 | Provide a History tab listing every batch ever sent, grouped by time period, each row showing a thumbnail crop, title/summary, timestamp, and a status chip (e.g. Fixed, In progress). | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication. |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-22 | Given batches have been sent previously, when the History tab is opened, then batches are grouped by time period, each showing thumbnail, summary, timestamp, and status chip. | must | test |
| AC-33 | Given History contains at least one batch, when the app is quit and relaunched, then all batches, their status chips, stored frozen frames, and per-annotation records are still present and openable. | must | test |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Open History tab", "Locate a Fixed batch", "Click Verify", "Diff runs per annotation against the frozen frame", "Annotation marked resolved"] |
| SC-15 | History survives an app restart | edge | ["Send two batches", "Quit Rectify", "Relaunch Rectify", "Open History tab", "Both batches, statuses and stored frames are intact"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-8 | QA Tester (PER-2) | a History tab listing every batch I've sent grouped by time with status chips | I can see what's fixed or in progress without re-testing everything | ["FR-20"] | ["AC-22", "AC-33"] | ["SC-8", "SC-15"] | must |

### sddReady

true
