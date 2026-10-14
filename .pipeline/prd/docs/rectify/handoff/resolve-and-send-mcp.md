# rectify — resolve-and-send-mcp

## Envelope

| field | value |
| --- | --- |
| stage | handoff |
| step | resolve-and-send-mcp |
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

ec5c0e2417b2e1a118ab1dffacb934e514a210f2618e56221cc8ea223f9751df

## Data

### sourcePrd

rectify

### id

F-7

### slug

resolve-and-send-mcp

### title

Resolve & Send via Local MCP

### goalStatement

The handoff itself: toolbar staged-annotation counter, 'Clear' action, and the primary 'Resolve & Send' button (disabled at zero staged annotations and while a send is in flight). On send, Rectify packages the full frame screenshot, per-annotation crops, view-hierarchy XML dump and generated prompt into a versioned payload envelope and transmits it to the locally running Claude Code agent through the existing local 'rectify' MCP server for retrieval via a get_latest_annotation-style tool — the sole transport in v1, no file or webhook fallback. During send a status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) animates with colored dots, then a toast confirms 'Sent N annotations… · <time>s' with an 'Open session' link; afterwards live mirroring resumes automatically and the counter resets to 0. All data stays local; typical localhost sends complete in under ~2s. Why: this is GOAL-4 and the point where everything captured becomes actionable for the agent; research notes MCP tool naming and payload schema should be deliberate design, since Aki's near-identical tool surface already exists for desktop.

### rawDescription

The handoff itself: toolbar staged-annotation counter, 'Clear' action, and the primary 'Resolve & Send' button (disabled at zero staged annotations and while a send is in flight). On send, Rectify packages the full frame screenshot, per-annotation crops, view-hierarchy XML dump and generated prompt into a versioned payload envelope and transmits it to the locally running Claude Code agent through the existing local 'rectify' MCP server for retrieval via a get_latest_annotation-style tool — the sole transport in v1, no file or webhook fallback. During send a status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) animates with colored dots, then a toast confirms 'Sent N annotations… · <time>s' with an 'Open session' link; afterwards live mirroring resumes automatically and the counter resets to 0. All data stays local; typical localhost sends complete in under ~2s. Why: this is GOAL-4 and the point where everything captured becomes actionable for the agent; research notes MCP tool naming and payload schema should be deliberate design, since Aki's near-identical tool surface already exists for desktop.

### priority

must

### phase

PH-1

### estimate

L

### dependsOn

- F-6

### linkedGoals

- GOAL-3
- GOAL-4

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-16 | In the toolbar, show a staged-annotation counter, a 'Clear' action that removes all staged annotations, and a primary 'Resolve & Send' button that is disabled while zero annotations are staged or while a send is in progress. | must |
| FR-17 | On 'Resolve & Send', package the full frame screenshot, per-annotation crops, view-hierarchy XML dump, and generated prompt, and transmit them to the locally running Claude Code agent via the local 'rectify' MCP server (e.g. for retrieval through a `get_latest_annotation`-style tool). | must |
| FR-18 | During send, show a visible status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) with colored dots, then confirm with a toast ('Sent N annotations to Claude Code · frame + hierarchy · <time>s') including an 'Open session' link. | must |
| FR-19 | After a successful send, resume live mirroring automatically and reset the staged-annotation counter to 0. | must |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-3 | performance | Resolve & Send payload transmission to the local MCP server should typically complete in under ~2s on localhost for a typical batch. |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |

### acceptanceCriteria

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-17 | Given zero annotations are staged, then 'Resolve & Send' is disabled; given one or more are staged, it is enabled; 'Clear' removes all staged annotations and resets the counter to 0. | must | test |
| AC-18 | When the user clicks 'Resolve & Send', then the frame screenshot, per-annotation crops, hierarchy XML, and generated prompt are transmitted via the local MCP server such that a Claude Code session calling the retrieval tool receives the complete payload. | must | test |
| AC-19 | When sending, then the status sequence Streaming -> Frozen -> Dumping hierarchy -> Sent displays in order with colored dots, and on completion a toast shows the sent-count/time summary with an 'Open session' link. | must | demo |
| AC-20 | While a send is in progress, 'Resolve & Send' is disabled and only re-enables after the toast confirms or the send fails. | must | test |
| AC-21 | After a successful send, then live mirroring resumes automatically and the staged-annotation counter resets to 0. | must | test |
| AC-32 | Given a staged annotation batch is sent, then the persisted payload envelope carries an explicit format/schema version field that the receiving MCP server can read to select its parsing path. | should | inspection |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-7 | Resolve & Send happy path | happy | ["Click Resolve & Send", "Status sequence animates through each stage", "Toast confirms with Open session link", "Mirroring resumes and counter resets"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-7 | Mobile Developer (PER-1) | one 'Resolve & Send' action that delivers the whole batch to my running Claude Code session over the local rectify MCP server | the agent can start fixing with full context and zero copy-paste | ["FR-16", "FR-17", "FR-18", "FR-19"] | ["AC-17", "AC-18", "AC-19", "AC-20", "AC-21", "AC-32"] | ["SC-7"] | must |

### sddReady

true
