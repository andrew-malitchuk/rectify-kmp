# history-batch-tracking — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | done |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-18T18:25:44Z |
| updatedAt | 2026-10-18T18:25:44Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T18:25:44Z

## Provenance

### selfHash

d974c9ddd8498776c1d1e0745017c59654b2d912616181cb1110bca3021f73f3

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | a5bed37cd18f641dd903ce2bbddc2c3d4346ed08fcace9dde987538436614ec8 |

## Data

### overview

A History tab (third segment of RectifyWindow's existing inspector tab strip, desktop-only) plus the local persistence layer behind it. Every batch F-7 (resolve-and-send-mcp) publishes via AnnotationHandoff is copied into durable, app-owned storage (Room KMP database + per-batch image files) the moment it is sent, and listed grouped by time period (Today/Yesterday/Older) with a 44x44 thumbnail crop, a one-line title, a meta line, and a status chip. Status transitions beyond the initial 'Sent' state, the Verify/re-diff logic itself, and any live agent-activity feed are explicitly out of scope and belong to F-9 (verify-and-rediff-loop) or a future spec.

### problemStatement

PER-2 (QA tester) currently has no durable record of batches already sent to the coding agent: once F-7 publishes a new batch, the previous one's images are pruned from the handoff slot and nothing persists across an app restart, so there is no way to see what has already been reported and its state without re-testing everything. The upcoming Verify loop (F-9) also needs a durable original-frame record to diff against, which does not exist today because F-7's handoff slot only ever retains the most recent send.

### purpose

Persist every batch F-7 ever sends (frozen frame, per-annotation crops, hierarchy dump, prompt, session id, timestamp) in a local database that survives app restarts, and surface it as a scrollable, grouped History list in the existing inspector tab strip so PER-2 can review prior sends and their status without re-testing, and so F-9 has a durable frame to diff against.

### goals

- Make every F-7 publish durable: copy its frame + crop images into app-owned storage before F-7 prunes the handoff slot, and persist its metadata to the Room database.
- Render a History tab (third tab-strip segment) listing every persisted batch, grouped by time period, matching the prototype's row anatomy (thumbnail, title, meta line, status chip).
- Keep the surface strictly local: no network egress (NFR-4), survive app restart (NFR-7).
- Expose exactly enough of a status model (and an update hook) for F-9 to later drive In progress / Fixed / Verified / Follow-up sent transitions, without F-8 implementing any of those transitions itself.

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-22 | Given batches have been sent previously, when the History tab is opened, then batches are grouped by time period, each showing thumbnail, summary, timestamp, and status chip. | must | test | false |
| AC-33 | Given History contains at least one batch, when the app is quit and relaunched, then all batches, their status chips, stored frozen frames, and per-annotation records are still present and openable. | must | test | false |
| AC-34 | Given the History tab badge, when zero batches have status In progress, then no badge is shown on the tab; when N batches are In progress, the badge shows N. | must | test | false |
| AC-35 | Given a batch has more than one annotation, when its History row thumbnail is generated, then the crop is computed from the union bounding box of all of the batch's annotation rects, not just the first. | must | test | false |
| AC-36 | Given a batch's status, when its History row renders, then the Verify button is shown only for status Fixed and is absent for every other status. | must | demo | false |
| AC-37 | Given at least one batch has been persisted, when the History list footer renders, then it shows 'claude . session <id>' sourced from the most recently persisted batch's sessionId. | should | inspection | false |
| AC-38 | Given F-7 prunes the handoff slot's image files after each publish, when a batch is persisted, then its frame and crop images have already been copied into app-owned storage so they remain available after subsequent sends and app restarts. | must | test | false |

### functionalRequirements

| id | text |
| --- | --- |
| FR-20 | Provide a History tab listing every batch ever sent, grouped by time period, each row showing a thumbnail crop, title/summary, timestamp, and a status chip (e.g. Fixed, In progress). |
| FR-26 | On every successful AnnotationHandoff.publish (F-7), persist a new Batch record (frozen frame copy, per-annotation crop copies, hierarchy dump, prompt, sessionId, sentAtMs, device info) to the local Room database with initial status Sent. |
| FR-27 | Copy the frozen frame PNG and each per-annotation crop PNG from the handoff slot into app-owned storage at persist time, because F-7 prunes every handoff-slot image directory except the latest after each publish and a bare path reference would go stale. |
| FR-28 | Render History rows grouped under Today / Yesterday / Older headers, based on the batch's local calendar day relative to now, newest batch first within each group. |
| FR-29 | Compute each row's 44x44 thumbnail by running the existing crop() algorithm (pad=18, bw=bh=40) against the union bounding box of all of the batch's annotation rects. |
| FR-30 | Render each row's title as the batch's first annotation's comment text (ellipsised, single line) and its meta line as '<n> notes . <HH:mm sent time> . #<last 4 hex chars of batch id>'. |
| FR-31 | Render a status chip with a tone dot per BatchStatus value (Sent, In progress, Fixed, Verified, Follow-up sent), matching the design's tone mapping; Sent shares Verified's neutral tone since the prototype defines no distinct tone for it. |
| FR-32 | Show a Verify button only on rows with status Fixed; clicking it invokes a caller-supplied callback (no-op by default). The diff/verify logic itself is out of scope (F-9). |
| FR-33 | Expose a BatchRepository.updateStatus(batchId, BatchStatus) API for a future consumer (F-9) to drive In progress / Fixed / Verified / Follow-up sent transitions; F-8 itself never calls it beyond the initial Sent write. |
| FR-34 | Show History as the third segment of RectifyWindow's existing inspector tab strip, with a badge equal to the count of batches currently in In progress status (hidden when zero). |
| FR-35 | Show a footer line 'claude . session <sessionId>' sourced from the most recently persisted batch's sessionId; project path is omitted because AnnotationEnvelope carries no such field. |
| FR-36 | Keep all persisted batches visible indefinitely; no delete or retention UI ships in this spec. |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication. |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-13 | performance | The History list renders via a lazily-virtualized list (Compose LazyColumn) so UI render cost stays independent of total persisted batch count. |
| NFR-14 | performance | Copying frame and crop PNGs to durable per-batch storage at publish time runs off the UI thread, consistent with AnnotationHandoff's existing suspend contract. |

### outOfScope

- Verify/re-diff execution logic itself (F-9 verify-and-rediff-loop) -- F-8 ships only the button affordance and visibility rule.
- Automatic status transitions beyond the initial Sent write (In progress / Fixed / Verified / Follow-up sent are set by future consumers via BatchRepository.updateStatus).
- Search and filter UI for the History list.
- Row-click detail view or re-opening the original annotation UI from a History row.
- Batch deletion and retention-policy UI.
- Android/iOS support -- History is desktop-only, matching F-7's desktop-only dependency and RectifyWindow's desktop-only host.
- Live agent-activity 'sub' line under the status chip (e.g. 'editing CartScreen.kt') -- no data source exists in the F-7 handoff contract.

### constraints

| type | text |
| --- | --- |
| tech | Desktop-only (jvm target) -- matches F-7's desktop-only AnnotationHandoff and RectifyWindow, the only host for the tab strip. |
| tech | Must use the existing Room KMP scaffold (data-database-api/data-database-impl) as the persistence layer -- its first real consumer; wire KSP for the desktop target per platform-coverage.md. |
| tech | Must ingest exclusively via the existing AnnotationHandoff/AnnotationEnvelope contract from data-handoff-api -- no changes to F-7's shipped, merged wire contract. |
| business | NFR-4: no network egress -- all batch storage is local filesystem plus local database. |

### assumptions

| id | text |
| --- | --- |
| AS-1 | A single integration point (the AnnotationHandoff.publish call site, or an equivalent completion hook in presentation-feature-mirror) is sufficient to trigger a persist-to-history write; F-8 does not need to poll the handoff slot. |
| AS-2 | The 'title/summary' required by AC-22 is satisfied by the batch's first annotation's comment text, matching the design's single-line title row; no separate generated summary field is needed. |
| AS-3 | A static 'claude' agent label in the footer is acceptable because the only shipped MCP consumer is Claude Code (mcp-application's single get_latest_annotation tool). |
| AS-4 | Sent status's visual tone is unspecified in the design's status table; it is assumed to share Verified's neutral/ink3 tone until F-9 or product specifies otherwise. |

### dependencies

| on | note |
| --- | --- |
| F-7 resolve-and-send-mcp | Source of AnnotationHandoff/AnnotationEnvelope and the frame.png/crop-N.png files F-8 must copy before they are pruned. |
| data-database-api / data-database-impl (Room KMP scaffold) | F-8 is the first real consumer; schema and DAO live here. |
| F-9 verify-and-rediff-loop (future) | Will call BatchRepository.updateStatus and own the Verify button's actual diff behavior. |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Open History tab", "Locate a Fixed batch", "Click Verify", "Diff runs per annotation against the frozen frame", "Annotation marked resolved"] |
| SC-15 | History survives an app restart | edge | ["Send two batches", "Quit Rectify", "Relaunch Rectify", "Open History tab", "Both batches, statuses and stored frames are intact"] |
| SC-16 | New batch appears while History is open | happy | ["Open History tab", "Resolve & Send a new batch", "A new row appears at the top of the Today group with status Sent", "The tab badge count stays unchanged because Sent is not In progress"] |
| SC-17 | Multi-annotation batch thumbnail | edge | ["Resolve & Send a batch with 3 annotations scattered across the frame", "Open History tab", "The row's thumbnail shows a crop centered on the union bounding box of all 3 annotation rects"] |
| SC-18 | Image copy fails during persist | error | ["Resolve & Send succeeds and publishes to the handoff slot", "The per-batch frame/crop file copy fails (e.g. disk full)", "The batch record is still persisted with its metadata and status chip", "The row shows a placeholder thumbnail instead of blocking the History entry"] |

### successMetrics

- PER-2 can determine a previously sent batch's status from History alone, with zero re-testing.
- Zero history data loss across restart (AC-33) across a week of normal usage.

### openQuestions

- Should Sent status get its own distinct tone/color once product reviews the design, instead of sharing Verified's neutral tone (AS-4)?
- If disk usage from retained frame/crop copies becomes a real problem, what retention policy (age cap, count cap, manual delete) should a future spec add? (No cap is in scope for F-8 -- see outOfScope.)
- Should a future MCP tool/heartbeat channel be added so the 'editing X.kt' activity sub-line becomes possible, or is that permanently out of scope for a single-request-response MCP server?

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| What drives the batch status state machine in this spec (F-8)? | ["F-8 only ever writes the initial status Sent; all later transitions (In progress/Fixed/Verified/Follow-up sent) are set by a future consumer (F-9) via a repository updateStatus API that F-8 exposes but never calls itself", "F-8 implements full state-machine logic (polling, MCP heartbeats) to drive all five states itself", "F-8 hardcodes every batch's status to Sent forever, with no updateStatus API at all"] | F-8 only ever writes the initial status Sent; all later transitions (In progress/Fixed/Verified/Follow-up sent) are set by a future consumer (F-9) via a repository updateStatus API that F-8 exposes but never calls itself | ai-proposed | F-9 (verify-and-rediff-loop) owns the Verify/diff logic per the brief; F-8 persisting-only while exposing the setter keeps F-9 unblocked without F-8 building speculative transition logic it has no data channel to trigger (no heartbeat/update path exists from the MCP agent today). |
| Is 'Follow-up sent' status something F-8 produces automatically, or just a persisted enum value set by a future caller? | ["Just another BatchStatus value settable via the same updateStatus API -- F-8 never sets it itself", "F-8 auto-detects a second send against the same frame and marks the prior batch Follow-up sent", "Build a dedicated UI action for the user to mark a batch Follow-up sent manually"] | Just another BatchStatus value settable via the same updateStatus API -- F-8 never sets it itself | ai-proposed | Consistent with the state-machine decision; detecting 'same frame, second send' requires frame-identity matching logic nothing in the brief requests, and a manual UI control isn't in the design. |
| Where does the History footer's session line ('claude . ~/code/shopkit . session 7f3a') get its data? | ["Per-batch sessionId from the most recently persisted batch, formatted 'claude . session <id>', with project path omitted because AnnotationEnvelope carries no path field", "Add a new project-path/agent-name field to AnnotationEnvelope (changes F-7's already-merged wire contract)", "Hardcode a static placeholder string, ignoring sessionId entirely"] | Per-batch sessionId from the most recently persisted batch, formatted 'claude . session <id>', with project path omitted because AnnotationEnvelope carries no path field | ai-proposed | AnnotationEnvelope already ships sessionId; the agent is always 'claude' (mcp-application's only tool). Modifying F-7's shipped, merged contract for a footer label is disproportionate, and a placeholder would show wrong data. |
| Where does the optional 'sub' activity line ('editing CartScreen.kt') under an in-progress row's status chip get its data? | ["Omit it entirely in v1 -- no mechanism exists in the F-7 contract to report live agent activity", "Build a new MCP tool/polling mechanism so the agent can report live activity", "Fake it with a static placeholder string"] | Omit it entirely in v1 -- no mechanism exists in the F-7 contract to report live agent activity | ai-proposed | A new cross-process reporting channel is far beyond F-8's persist-and-list scope and isn't requested by any FR/AC; faking data is worse than omitting the feature. Carried forward as an open question. |
| How should History group rows by time period? | ["Today / Yesterday / Older, based on local calendar-day boundaries", "Rolling N-day buckets (e.g. Last 7 days, Last 30 days)", "No bucketing -- a flat chronological list"] | Today / Yesterday / Older, based on local calendar-day boundaries | ai-proposed | Matches the only two buckets the design actually shows, plus one catch-all so the algorithm is total for older batches; finer granularity is unrequested. |
| Does the History tab need search or filter affordances? | ["None in this spec -- a flat scrolling list only", "Add a text search box", "Add status filter chips"] | None in this spec -- a flat scrolling list only | ai-proposed | Not present in the design or any FR/AC; YAGNI. |
| How should the History list handle pagination or large batch counts? | ["No app-level pagination; render via Compose LazyColumn (already virtualizes off-screen rows) and no storage cap in v1", "Page-load N at a time with a 'load more' control", "Hard-cap displayed history to the last N batches"] | No app-level pagination; render via Compose LazyColumn (already virtualizes off-screen rows) and no storage cap in v1 | ai-proposed | LazyColumn already solves the only real cost (layout/composition of off-screen rows) for free; paging or capping UI is extra state no current scale justifies. |
| What happens when a user clicks on a History row (not the Verify button)? | ["Row background click is a no-op in v1; only the Verify button on Fixed rows is actionable", "Row click opens a new detail view", "Row click immediately opens F-9's verify flow regardless of status"] | Row background click is a no-op in v1; only the Verify button on Fixed rows is actionable | ai-proposed | No detail view exists in the design; only Fixed rows show the Verify affordance per the prototype's canVerify flag, so wiring row-click to undesigned behavior would be speculative. |
| For a batch with multiple annotations, what single crop does the History thumbnail show? | ["Feed the union bounding box of all of the batch's annotation rects into the existing crop() formula (same math, one combined rect)", "Use only the first annotation's rect", "Render a montage of all crops"] | Feed the union bounding box of all of the batch's annotation rects into the existing crop() formula (same math, one combined rect) | ai-proposed | Reuses the already-specified crop() algorithm unchanged -- it just generalizes the input rect from one annotation to the batch's combined extent, with zero new formula. |
| Should F-8 copy the frozen frame/crop bytes into its own storage, or just keep a reference to F-7's handoff slot path? | ["Copy frame.png + crop-N.png into app-owned per-batch storage at persist time; the database stores file paths to the copies", "Store only a reference to the handoff slot path", "Store full image bytes as BLOBs directly in the Room database"] | Copy frame.png + crop-N.png into app-owned per-batch storage at persist time; the database stores file paths to the copies | ai-proposed | F-7 explicitly prunes every handoff-slot image directory except the latest after each publish, so a bare reference would go stale the moment the next batch is sent -- directly breaking AC-33. Files-on-disk plus a DB path matches data-handoff-api's own existing pattern instead of inventing BLOB storage. |
| What retention or deletion policy should History batches have? | ["No deletion/retention cap in v1 -- every persisted batch stays forever (matches AC-33); unbounded disk growth over time is an accepted, known ceiling for now", "Auto-evict the oldest batches past a fixed count or age", "Ship a manual delete-row UI control"] | No deletion/retention cap in v1 -- every persisted batch stays forever (matches AC-33); unbounded disk growth over time is an accepted, known ceiling for now | ai-proposed | AC-33 requires every batch to remain present and openable after restart; building eviction or delete UI that no requirement asked for risks silently discarding exactly the data AC-33 and PER-2's workflow depend on. Flagged as a known ceiling for a future retention spec. |
| How far does the Verify button go in this spec, given F-9 (verify-and-rediff-loop) owns the actual diff logic? | ["Show the Verify button only on Fixed-status rows; clicking it calls a caller-supplied callback/hook with no diff logic implemented here -- the actual verify/re-diff behavior belongs to F-9", "Omit the Verify button entirely from F-8 and defer the whole affordance to F-9", "Show the Verify button on every row regardless of status"] | Show the Verify button only on Fixed-status rows; clicking it calls a caller-supplied callback/hook with no diff logic implemented here -- the actual verify/re-diff behavior belongs to F-9 | ai-proposed | SC-8 explicitly includes 'Click Verify' as a step reachable from History, and the design's canVerify flag gates it on Fixed status -- shipping the affordance (button + visibility rule) satisfies SC-8's first steps while keeping the diff/re-verify logic itself inside F-9 as directed. |
| Which platforms does the History tab ship on? | ["Desktop-only -- matches F-7 (desktop-only dependency) and RectifyWindow (the tab-strip host, desktop-only)", "Android/iOS/Desktop, per the PRD's generic platform list", "Desktop + Android, skip iOS"] | Desktop-only -- matches F-7 (desktop-only dependency) and RectifyWindow (the tab-strip host, desktop-only) | ai-proposed | data-handoff-api has no androidMain/iosMain actual (AnnotationHandoff is unsupported there) and RectifyWindow itself is desktop-only, so there is no annotation data to show History for on Android/iOS. |
