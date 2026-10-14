# rectify — verify-and-rediff-loop

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | verify-and-rediff-loop |
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

4aaa35b8cedb3876e3210e391036b5a38aa87f8610b8b2b18c6ebad234f7815c

## Data

### sourcePrd

rectify

### id

F-9

### slug

verify-and-rediff-loop

### title

Verify & Re-diff Loop

### goalStatement

The closed loop. On any sent batch, a 'Verify' action re-captures the live screen and diffs it per annotation box against the stored frozen frame using the confirmed combination of a pixel-region diff within the box plus a hierarchy/layout diff of the matched node. Annotations with no material difference are marked resolved; annotations that still differ surface a one-click follow-up that re-stages only that annotation for re-send through F-7. Diff thresholds must be tuned to hold the <5% false-positive target (MET-3) while surfacing real misses, and results stay human-reviewable. Why: no competitor found does per-annotation, per-region pixel+hierarchy verification against an original annotated frame — adb-mcp's verify_change only detects whether the UI changed at all, and AppContext's re-look is unstructured — so this is the strongest single differentiator and the answer to PROB-2.

### rawDescription

The closed loop. On any sent batch, a 'Verify' action re-captures the live screen and diffs it per annotation box against the stored frozen frame using the confirmed combination of a pixel-region diff within the box plus a hierarchy/layout diff of the matched node. Annotations with no material difference are marked resolved; annotations that still differ surface a one-click follow-up that re-stages only that annotation for re-send through F-7. Diff thresholds must be tuned to hold the <5% false-positive target (MET-3) while surfacing real misses, and results stay human-reviewable. Why: no competitor found does per-annotation, per-region pixel+hierarchy verification against an original annotated frame — adb-mcp's verify_change only detects whether the UI changed at all, and AppContext's re-look is unstructured — so this is the strongest single differentiator and the answer to PROB-2.

### priority

must

### phase

PH-1

### estimate

XL

### dependsOn

- F-5
- F-8

### linkedGoals

- GOAL-2

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-21 | [CONFIRMED via Stage 4 survey] On any sent batch (not gated on a 'Fixed' status, since nothing in v1 is confirmed to set one), provide a 'Verify' action that re-captures the live screen and diffs it, per annotation box, against the original frozen frame; matches mark the annotation resolved, mismatches surface a one-click follow-up that re-stages that single annotation for re-send. | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-23 | Given any sent batch, when the user clicks 'Verify', then the app re-captures the live screen and diffs it, per annotation box, against the stored frozen frame using a pixel-region diff plus a hierarchy/layout diff of the matched node. | must | test |
| AC-24 | Given a Verify diff finds no material difference for an annotation, then that annotation is marked resolved; given a difference remains, then a one-click follow-up re-stages only that annotation for re-send. | must | test |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Open History tab", "Locate a Fixed batch", "Click Verify", "Diff runs per annotation against the frozen frame", "Annotation marked resolved"] |
| SC-9 | Follow-up re-send after a failed verify | edge | ["Verify finds a remaining mismatch for an annotation", "Click the one-click follow-up", "That annotation is re-staged", "Resolve & Send again"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-9 | Mobile Developer (PER-1) | a Verify action on Fixed batches that diffs the live screen against the original frozen frame per annotation | I can confirm the specific reported region actually changed instead of eyeballing it | ["FR-21"] | ["AC-23"] | ["SC-8"] | must |
| US-10 | QA Tester (PER-2) | a one-click follow-up that re-stages only the annotation whose Verify still fails | I can push a tight correction back to the agent without redoing the whole report | ["FR-21"] | ["AC-24"] | ["SC-9"] | must |

### sddReady

true
