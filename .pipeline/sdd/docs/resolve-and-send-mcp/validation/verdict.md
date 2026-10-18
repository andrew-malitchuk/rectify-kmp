# resolve-and-send-mcp — verdict

## Envelope

| field | value |
| --- | --- |
| stage | validation |
| step | verdict |
| status | done |
| producedBy | sdd-build-validation-verdict |
| schemaVersion | 1 |
| createdAt | 2026-10-18T16:42:52Z |
| updatedAt | 2026-10-18T16:46:22Z |

## Gate

### type

human

### approved

true

### approvedBy

human

## Provenance

### selfHash

d1af14cc4770f72c0bb2218d6cccd7f608673fe829187096c5bc3b51805e256f

### inputs

| artifact | hash |
| --- | --- |
| validation/audit.json | ce38baf2ea83ce415d2d6aa00e7fb679b8c3dfee0a073f4a7e16687d034bbfff |

## Data

### verdict

complete-with-waivers

### tasks

| id | status | note |
| --- | --- | --- |
| T-1 | done |  |
| T-2 | done |  |
| T-3 | done |  |
| T-4 | done |  |
| T-5 | done |  |
| T-6 | done |  |
| T-7 | done |  |
| T-8 | done |  |
| T-9 | done |  |
| T-10 | done |  |
| T-11 | done |  |
| T-12 | done |  |
| T-13 | blocked | Automated half complete and green (TAC-1/2/5/6/7 plus live MCP-server verification). Manual end-to-end demo TAC-3/TAC-4 not performed. |

### unsatisfied

#### acceptanceCriteria

- AC-18
- AC-81
- AC-19
- NFR-3

#### scenarios

- SC-7 (end-to-end form only - a real frame, a real device and a real agent; its ViewModel-level form is covered)

#### tasks

- T-13

### waivers

| item | reason | acceptedBy |
| --- | --- | --- |
| AC-19 animated progression | Human gate (AF-9) chose to waive as partially satisfied rather than move the hierarchy dump into the send path, which is an architectural change affecting the F-5/F-6 boundary late in the spec. The divergence is documented in sendSteps' KDoc, not hidden. | human |
| NFR-3 has no automated coverage | Acknowledged explicitly by the user at the Stage 3 backlog gate. No perf-test harness exists in this repo and the plan scoped NFR-3 as manual-measurement-only (its own coverageGoal gap #5). | human |
| AF-6 :data-handoff-api still resolves lifecycle/Compose | The installDist fix is consumer-side. Root cause is LibraryConventionPlugin wiring lifecycle-viewmodel + lifecycle-runtime-compose into every library module, which contradicts its own KDoc. Out of F-7's scope - a build-logic change with repo-wide blast radius. Routed to Stage 7. | orchestrator |
| AF-4 preview-vs-send crop count | The preview passes the staged count because nothing is encoded until the send runs; the send passes the真 attached count. They diverge only when a crop encode fails. Disclosed rather than hidden; fixing it means moving crop encoding ahead of the send button. | orchestrator |
| AF-3 stale plan prose | The plan claims the new version tests replace PP-TD-4's tautological assertion; they do not, and that assertion is untouched. Documentation defect with no code impact. | orchestrator |
| AF-7 frozenHeld is a test-only seam | Introduced at the orchestrator's request so T-1's pairing invariant could be asserted at all. Production reads the private backing field. No defect; the KDoc overstates its role. | orchestrator |
| AF-8 registry gaps | features.json has no entry for F-5, F-6 or F-7 while index.json lists all three. Pre-existing for F-5/F-6. Routed to Stage 7 as a registry delta. | orchestrator |
| Localisation of new UI strings | Explicitly out of scope per the backlog; 'Resolve & Send', 'Sending...', 'Clear', 'Open session' are plain constants. | orchestrator |

### openChangeRequests

| id | about | target | source |
| --- | --- | --- | --- |
| CR-OPEN-1 | Perform and record the mandatory manual end-to-end demo: a real Claude Code session calling get_latest_annotation and receiving the payload; a second call with no new send returning an identical result (AC-81); the measured end-to-end elapsed time against NFR-3's <=2s; and the inline tool-result byte size against the 512 KB budget. Also eyeball the toast dismissing after its ~10s duration, the live Cmd+Enter dispatch matching the button's enablement, and whether the top-centre toast collides with the top-centre status overlay. | T-13 | audit |

### humanRemarks

- Three Stage 5 change requests were raised and fixed on the integration branch: AF-1 (a reproduced server crash - a non-bare path in the slot killed the MCP process instead of being rejected), AF-5 (the status overlay outlived the state it described), and AF-4 (the generated prompt named a hierarchy.xml file that never exists on any path and could overstate the crop count).
- AF-1 is the find of this stage: it was invisible to a 916-test green suite because the test double's resolve() was more permissive than the real contract. The fix corrected the double rather than adding a second one.
- The orchestrator independently re-verified, against the real installDist binary rather than from executor reports: initialize/tools-list/tools-call, empty-slot text, unknown-method -32601 and unparseable -32700 with loop survival in both cases, one-line responses, and the 7-jar pinned classpath.
- The spec is NOT fully done. T-13 remains blocked and CR-OPEN-1 is the single blocking item.

### changelogRef

git log development..sdd/resolve-and-send-mcp

### unsatisfiedDetail

| id | why |
| --- | --- |
| AC-18 | Server half independently verified against the real installDist binary over stdio. The CLIENT half - a real Claude Code session receiving the payload - is unverified pending T-13's demo. |
| AC-81 | Byte-identical repeat call verified at the storage layer and against the real server binary. The agent-facing repeat call through a real Claude Code session is unverified pending T-13's demo. |
| AC-19 | Partially satisfied. The four steps render with correct states and the Stream-dropped failure variant works, but the animated in-order progression the AC's wording implies is not achievable while the hierarchy dump happens at freeze time rather than during the send. |
| NFR-3 | Never measured. No perf harness exists; the <=2s budget was scoped as manual-measurement-only and acknowledged as such at the Stage 3 gate. T-13's demo is its only measurement point. |
