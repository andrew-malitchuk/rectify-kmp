# rectify — payload-preview

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | payload-preview |
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

ffecea71b5895592cb15cec484fad9bb7b72dfa076d898b98f626be4651bb668

## Data

### sourcePrd

rectify

### id

F-6

### slug

payload-preview

### title

Payload Preview (Prompt / Crops / Hierarchy)

### goalStatement

The Payload tab that lets the reporter see exactly what the agent will receive before sending: a Prompt section with the auto-generated text, its token-count estimate (computed in under ~200ms) and an Edit affordance; a Crops section rendering a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'; a Hierarchy section showing a monospace indented view-hierarchy tree with matched/flagged nodes visually marked; and a footer summarizing 'frame.png · N crops · hierarchy.xml' plus a total size that matches the real payload. Why: trust in the one-shot handoff depends on the reporter being able to inspect and tune the bundle, and this is where the payload assembly (and its versioned envelope) is actually built.

### rawDescription

The Payload tab that lets the reporter see exactly what the agent will receive before sending: a Prompt section with the auto-generated text, its token-count estimate (computed in under ~200ms) and an Edit affordance; a Crops section rendering a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'; a Hierarchy section showing a monospace indented view-hierarchy tree with matched/flagged nodes visually marked; and a footer summarizing 'frame.png · N crops · hierarchy.xml' plus a total size that matches the real payload. Why: trust in the one-shot handoff depends on the reporter being able to inspect and tune the bundle, and this is where the payload assembly (and its versioned envelope) is actually built.

### priority

must

### phase

PH-1

### estimate

M

### dependsOn

- F-4
- F-5

### linkedGoals

- GOAL-3
- GOAL-4

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-12 | In the Payload tab's Prompt section, show the auto-generated text prompt, its token-count estimate, and an Edit affordance. | must |
| FR-13 | In the Payload tab's Crops section, show a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'. | must |
| FR-14 | In the Payload tab's Hierarchy section, show a monospace, indented view-hierarchy tree with matched/flagged nodes visually marked. | must |
| FR-15 | Show a Payload footer summarizing contents ('frame.png · N crops · hierarchy.xml') and total payload size. | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-11 | performance | Token-count estimation for the generated prompt computes without perceptible delay (target under ~200ms) before Resolve & Send is enabled. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-13 | Given at least one staged annotation exists, when the user opens the Payload tab, then a generated text prompt with token-count estimate and an Edit affordance is shown. | must | test |
| AC-14 | Given staged annotations exist, when viewing Payload > Crops, then a 3-column grid shows each annotation's cropped image with its red outline baked in and caption 'from frame <id>'. | must | test |
| AC-15 | Given staged annotations exist, when viewing Payload > Hierarchy, then a monospace indented tree displays with matched/flagged nodes visually marked. | must | test |
| AC-16 | Given the Payload tab is open, then its footer summarizes 'frame.png · N crops · hierarchy.xml' plus a total size that matches the actual payload. | must | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-6 | Review payload before sending | happy | ["Open Payload tab", "Review generated prompt and token estimate", "Review crops grid", "Review hierarchy tree", "Check footer summary"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-6 | Mobile Developer (PER-1) | to review the generated prompt, crops, hierarchy tree and payload size before sending | I can trust and tune what the agent will actually receive | ["FR-12", "FR-13", "FR-14", "FR-15"] | ["AC-13", "AC-14", "AC-15", "AC-16"] | ["SC-6"] | must |

### sddReady

true
