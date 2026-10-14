# rectify — measure-mode

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | measure-mode |
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

5de07092964dd6195a47a4704e04ca4893cb5e1cd1afbf53e878815b81957fa0

## Data

### sourcePrd

rectify

### id

F-5

### slug

measure-mode

### title

Option-Key Measure Mode

### goalStatement

Holding Option over an annotation box enters Measure mode: Rectify snaps to the matched underlying view-hierarchy node, draws dashed guide boxes for neighboring views, and shows dp/pixel gap measurements in a floating 'Measuring' panel, highlighting misaligned or undersized dimensions in red. On release, the measurement data is appended to that annotation's note prefixed with the Option marker, so the sent payload carries exact numbers instead of estimates. Must resolve node lookup in under ~100ms to feel instantaneous, and must degrade clearly when no node matches. Why: no bug-report-to-agent tool found in research has hierarchy-snapped measurement; the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path.

### rawDescription

Holding Option over an annotation box enters Measure mode: Rectify snaps to the matched underlying view-hierarchy node, draws dashed guide boxes for neighboring views, and shows dp/pixel gap measurements in a floating 'Measuring' panel, highlighting misaligned or undersized dimensions in red. On release, the measurement data is appended to that annotation's note prefixed with the Option marker, so the sent payload carries exact numbers instead of estimates. Must resolve node lookup in under ~100ms to feel instantaneous, and must degrade clearly when no node matches. Why: no bug-report-to-agent tool found in research has hierarchy-snapped measurement; the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path.

### priority

must

### phase

PH-1

### estimate

L

### dependsOn

- F-4

### linkedGoals

- GOAL-1
- GOAL-3

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-10 | While holding Option over an annotation box, enter Measure mode: snap to the matched underlying view-hierarchy node, show dashed guide boxes for neighboring views and dp/pixel gap measurements in a floating 'Measuring' panel, highlight misalignment in red, and attach the measurement data to that annotation's note (prefixed with the Option symbol). | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-2 | performance | Measure-mode hierarchy-node lookup on Option-hover should resolve in under ~100ms so the measuring panel feels instantaneous. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-10 | Given a frozen frame with an annotation box, when the user holds Option over it, then the tool snaps to the matched view-hierarchy node and shows neighbor guide boxes with dp gap measurements in a 'Measuring' panel. | must | test |
| AC-11 | Given a measured element has misalignment (e.g. undersized hit target), then the offending dimension is highlighted in red and the note is prefixed with the Option-mode marker and carries the measurement data. | must | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-5 | Measure a misaligned element | happy | ["Hold Option over an annotation box", "View snaps to matched hierarchy node", "Neighbor guide boxes and dp gaps render", "Misalignment highlighted red and attached to the note"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-5 | Mobile Developer (PER-1) | to hold Option over a box and get the matched hierarchy node plus dp/pixel gaps attached to the note | the agent receives exact measurements instead of my prose estimate of the spacing | ["FR-10"] | ["AC-10", "AC-11"] | ["SC-5"] | must |

### sddReady

true
