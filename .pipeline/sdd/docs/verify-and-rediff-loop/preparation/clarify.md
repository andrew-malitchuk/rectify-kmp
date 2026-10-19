# verify-and-rediff-loop — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | done |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-18T21:25:15Z |
| updatedAt | 2026-10-18T21:25:15Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T21:25:15Z

## Provenance

### selfHash

d304c4c8e354bd6f7f5d74d4f2022e7994a08a0269ebadc7f4f33c651e7df78d

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 216dc83b00387b1a224ecb006a5773fc4b440e4ebcd595dfa121ba08cd970e4e |

## Data

### overview

F-9 closes the Rectify loop: a 'Verify' action on any sent History batch re-captures the live device screen, diffs it per annotation against the batch's stored frozen frame (pixel-region diff of the box plus a hierarchy/layout diff of the matched node, reusing F-5 measure-mode), marks each annotation Fixed or Still off, and lets the user send a one-click follow-up that re-stages only the still-off annotations back through F-7. It also finally exercises F-8's unused HistoryRepository.updateStatus hook, and removes the Fixed-only gate on the Verify button that currently makes it unreachable (F-8 never writes Fixed).

### problemStatement

F-8 shipped a Verify button gated to BatchStatusModel.Fixed, but nothing in v1 ever writes Fixed, so the button is dead on arrival and the PRD's differentiator (per-annotation, per-region pixel+hierarchy verification) is unbuilt. Users have no way to confirm an agent's fix actually landed without manually re-comparing frames by eye, and no tight way to push back just the annotations that are still wrong.

### purpose

Give the user a one-click way to confirm whether a coding agent's fix matches each specific reported annotation, and a one-click way to re-report exactly the annotations that don't, without re-doing the whole batch.

### goals

- Make the Verify action reachable on any sent batch, not only a status value nothing produces.
- Automate the per-annotation pixel+hierarchy comparison against the stored frozen frame so the user does not eyeball two screenshots.
- Let the user close the loop with a single click that re-stages and re-sends only the annotations still off.
- Start actually using BatchStatusModel's Verified / Follow-up sent values via F-8's already-exposed updateStatus hook.

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-23 | Given any sent batch, when the user clicks 'Verify', then the app re-captures the live screen and diffs it, per annotation box, against the stored frozen frame using a pixel-region diff plus a hierarchy/layout diff of the matched node. | must | test | false |
| AC-24 | Given a Verify diff finds no material difference for an annotation, then that annotation is marked resolved; given a difference remains, then a one-click follow-up re-stages only that annotation for re-send. | must | test | false |
| AC-39 | Given the History tab, when a row's batch status is anything other than Fixed (e.g. Sent), then the Verify button is still rendered and clickable — the BatchStatusModel.Fixed gate at MirrorHistoryTab.kt:179 is removed, superseding F-8's AC-36. | must | test | false |
| AC-40 | Given Verify is clicked, when the re-captured frame's pixel dimensions differ from the stored frozen frame's (resolution or orientation changed), then no per-annotation diff runs and an error state is shown instead of a misaligned comparison. | must | test | false |
| AC-41 | Given a Verify run, when an annotation's original hierarchy node cannot be matched in the re-captured hierarchy (moved, renamed, or removed), then that annotation's verdict is 'Still off' regardless of its pixel-region diff result. | must | test | false |
| AC-42 | Given at least one annotation is 'Still off' after Verify, when the user clicks 'Send follow-up · N', then exactly the still-off annotations are re-staged from the re-captured (AFTER) frame and crops and sent through F-7's existing send path, and the batch status updates to Follow-up sent. | must | test | false |
| AC-43 | Given a Verify run completes with every annotation 'Fixed', or the user clicks 'Mark all as verified', then the batch status updates to Verified via HistoryRepository.updateStatus. | must | test | false |
| AC-44 | Given a Verify run is in progress (re-capture or diffing), when the device disconnects, then the run aborts, the original batch status is left unchanged, and the user can retry Verify once reconnected. | must | test | false |
| AC-45 | Given the diff reads image bytes for comparison, then it reads the full-resolution stored frame.png (and per-annotation crop files), never the ≤128px thumbnail BLOB. | must | inspection | false |

### functionalRequirements

| id | text |
| --- | --- |
| FR-21 | On any sent batch (not gated on a 'Fixed' status, since nothing in v1 is confirmed to set one), provide a 'Verify' action that re-captures the live screen and diffs it, per annotation box, against the original frozen frame; matches mark the annotation resolved, mismatches surface a one-click follow-up that re-stages that single annotation for re-send. |
| FR-37 | Remove the BatchStatusModel.Fixed gate on the History row's Verify button (MirrorHistoryTab.kt HistoryEntryRow trailing slot) so it renders and is clickable for every sent batch regardless of status. |
| FR-38 | On Verify click, enter a Verify phase that re-captures the live device frame and hierarchy for the device that sent the batch (AS-1/AS-2), reusing the existing mirror capture pipeline rather than a new one. |
| FR-39 | For each annotation, crop the matching region from the frozen and re-captured frames (reusing the existing CropEncoder seam) and compute a pixel-region diff behind a new desktop-only expect/actual seam (PixelDiffer), with null-returning Android/iOS stand-ins matching the CropEncoder/BatchFileStore pattern (AS-29 precedent). |
| FR-40 | For each annotation, re-run F-5 measure-mode's node matching against the re-captured hierarchy and compare it to the original stored measurement, producing a dp-delta result string in the same format as live measure-mode (e.g. 'top Δ6dp → 0dp'). |
| FR-41 | Mark an annotation's verdict 'Fixed' only when both the pixel-region diff and the hierarchy/layout diff are within their configured thresholds; otherwise mark it 'Still off'. An annotation whose hierarchy node cannot be matched in the re-captured frame is always 'Still off', never silently skipped as 'Fixed'. |
| FR-42 | Render the Verify view per design-verify-view.md as a new phase inside the existing MirrorHost/MirrorContent composition (no second MirrorHost call site): two 296×640 frames (BEFORE/AFTER) with per-annotation rects coloured by verdict, a 300px 'Verification' panel with result rows and verdict words, an 'AGENT CHANGES' section cut for v1 (see out-of-scope), footer actions 'Send follow-up · N' and 'Mark all as verified', a four-step header strip, and a '← Back to live' chip that resets to the live mirror with the History tab selected. |
| FR-43 | 'Send follow-up · N' re-stages only the Still-off annotations into a new AnnotationEnvelope sourced from the re-captured (AFTER) frame and its crops — not the stale frozen frame — and re-enters F-7's send path via MirrorIntent.ResolveAndSend. |
| FR-44 | 'Mark all as verified' force-sets every annotation's verdict to Fixed without further diffing and writes the batch status to Verified. |
| FR-45 | Persist verify outcomes through HistoryRepository.updateStatus: Verified when a run ends with every annotation Fixed (or via the Mark-all override), Follow-up sent immediately after a follow-up send. No v1 code path writes In progress or BatchStatusModel.Fixed — that remains an intentional gap (see out-of-scope). |
| FR-46 | If the re-captured frame's pixel dimensions differ from the stored frozen frame's, abort the diff before any per-annotation comparison runs and surface an error state rather than comparing misaligned regions. |
| FR-47 | Reconcile the crop-index convention mismatch noted in F-8's review (0-based in HistoryEntryMapper/history storage, 1-based in the AnnotationHandoff envelope) at the same boundary F-8 already draws it at; the Verify diff loop iterates annotations using history's 0-based indices and only converts to 1-based when building the follow-up envelope. |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-15 | performance | Re-capture and the per-annotation diff loop run off the UI thread (consistent with AnnotationHandoff's existing suspend contract) and show a visible in-progress state, since N region diffs plus a fresh capture can take long enough to need one. |
| NFR-16 | reliability | A device disconnect mid-verify aborts the run cleanly: no partial status write, the original batch status is untouched, and the user lands in a retryable state rather than a stuck spinner. |
| NFR-17 | maintainability | MET-3's <5% false-positive target is not automatically verifiable in this codebase in v1 — there is no golden before/after dataset and no Compose UI-test harness. Diff thresholds are tunable constants validated by manual spot-check only; MET-3 is recorded as unverified-in-v1 rather than asserted by an automated test. |

### outOfScope

- The 'AGENT CHANGES' file-diff section (e.g. 'CartItemView.kt +4 −2') — no data source exists (AnnotationEnvelope carries no file-level diff or agent metadata, and there is no git/MCP integration to query it); cut for v1, matching F-8's precedent of cutting the equivalent activity sub-line.
- Automated or CI-measured verification of the MET-3 <5% false-positive number — recorded as unverified-in-v1; see NFR-17.
- Writing BatchStatusModel.InProgress or BatchStatusModel.Fixed from any v1 flow — In progress would need an agent-side 'I started working' signal F-7's MCP contract doesn't have, and nothing in v1 is the natural owner of Fixed.
- Re-targeting Verify to a device other than the one that sent the batch, or verifying against a different app build.
- Android/iOS Verify UI or pixel diffing — desktop-only, matching F-7/F-8.
- Batch-level (whole-screen) diffing or verifying more than one batch at a time — this feature is per-annotation, per-region, by design (the stated differentiator).

### constraints

| type | text |
| --- | --- |
| tech | The Verify view must not introduce a second MirrorHost call site; it is a new phase rendered inside the existing MirrorHost/MirrorContent composition in presentation-feature-mirror, per that file's single-call-site KDoc warning. |
| tech | Pixel diffing lives behind a desktop-only expect/actual seam (new PixelDiffer), same shape as the existing CropEncoder/BatchFileStore seams — null-returning Android/iOS actuals, never a crash. |
| tech | The diff must read the full-resolution frame.png and crop files F-8 persists on disk, never the ≤128px thumbnail BLOB. |
| tech | Hierarchy/layout diffing reuses F-5 measure-mode's existing node-matching and dp-delta formatting rather than a new layout-diff algorithm. |
| business | Desktop (macOS) only in v1, matching F-7/F-8 scope and NFR-10. |

### assumptions

| id | text |
| --- | --- |
| AS-1 | A capture pipeline equivalent to the live mirror's (adb screencap + hierarchy dump) can be invoked on demand for a specific device without the live mirror already running, to produce the 're-captured' frame. |
| AS-2 | The device used for Verify is the same device that was mirrored when the batch was originally sent; v1 does not attempt to re-target a disconnected/replaced device. |
| AS-3 | F-5's measure-mode node-matching can run headlessly (without the Option-hover UI) against a stored hierarchy snapshot to compute a dp-delta; no new matching algorithm is needed. |
| AS-4 | F-8's persisted frame.png, crop files, and hierarchy XML are sufficient diff inputs; no additional data needs to be captured at send-time beyond what F-8 already stores. |
| AS-5 | PRD Stage-4's resolution ('Verify is available on any sent batch regardless of status; History status chips remain informational only') is authoritative and supersedes F-8's AC-36 gate outright — this clarify does not re-open that question. |

### dependencies

| on | note |
| --- | --- |
| F-7 (resolve-and-send-mcp) | One-click follow-up re-enters MirrorIntent.ResolveAndSend / AnnotationEnvelope assembly / CropEncoder for the re-staged annotation. |
| F-8 (history-batch-tracking) | Consumes HistoryRepository.updateStatus, stored frame.png/crop files, BatchStatusModel values, and the existing (inert) MirrorIntent.VerifyBatch stub / verifyBatch handler seam. |
| F-5 (measure-mode) | Hierarchy/layout diff reuses measure-mode's node matching and dp-delta result-string format. |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-8 | Review and verify a batch in History | happy | ["Open History tab", "Locate a Fixed batch", "Click Verify", "Diff runs per annotation against the frozen frame", "Annotation marked resolved"] |
| SC-9 | Follow-up re-send after a failed verify | edge | ["Verify finds a remaining mismatch for an annotation", "Click the one-click follow-up", "That annotation is re-staged", "Resolve & Send again"] |
| SC-19 | Verify a batch whose status is still Sent | edge | ["Open History tab", "Locate a batch with status Sent (not Fixed)", "Confirm the Verify button is present and clickable", "Click Verify", "Diff runs exactly as it would on any other status"] |
| SC-20 | Mark all as verified override | happy | ["Open the Verify view with 2 of 3 annotations Still off", "Click 'Mark all as verified'", "All annotations show Fixed", "Batch status updates to Verified"] |
| SC-21 | Device disconnects mid-verify | error | ["Click Verify on a sent batch", "Re-capture or diffing is in progress", "Device disconnects", "Verify run aborts with an error state", "Original batch status is unchanged and Verify can be retried after reconnect"] |
| SC-22 | Resolution mismatch aborts the diff | error | ["Click Verify on a batch sent at one device resolution/orientation", "Re-capture returns a frame at a different resolution/orientation", "Diff aborts before comparing any annotation", "An error state is shown instead of a misaligned result"] |

### successMetrics

- Every sent batch shows a clickable Verify action regardless of status (AC-39) — zero batches stuck behind an unreachable gate.
- MET-3 (<5% false positives) tracked via manual threshold spot-check only in v1; not asserted by an automated test (NFR-17).

### openQuestions

- Should a later feature give the coding agent an MCP-side signal to write BatchStatusModel.InProgress, or does that value stay permanently unused?
- Should Verify ever support re-targeting a reconnected-but-different device than the one that sent the original batch?
- Should the 'AGENT CHANGES' file-diff section come back later via a git-diff-capable MCP tool, or is it permanently out of scope?

### survey

| question | options | answer | source | rationale |
| --- | --- | --- | --- | --- |
| FR-21 says Verify is available on any sent batch, not gated on Fixed; F-8 shipped it gated to Fixed-only (dead code, since F-8 never writes Fixed). How should F-9 resolve this? | ["Remove the Fixed-only gate; Verify button renders on every sent batch regardless of status", "Keep the gate and also make something write Fixed first", "Leave the gate as-is and treat Verify as unreachable in v1"] | Remove the Fixed-only gate; Verify button renders on every sent batch regardless of status | ai-proposed | The PRD's own Stage-4 survey already resolved this ('Verify is available on any sent batch regardless of status; History status chips remain informational only'). F-9 just needs to implement that resolution by deleting MirrorHistoryTab.kt's status check; inventing a new Fixed-writer would be scope creep nothing asked for. |
| MET-3 targets <5% false positives on the diff, but there's no golden dataset or UI-test harness in this repo. How should F-9 handle that target? | ["Record MET-3 as unverified-in-v1; thresholds are tunable constants validated by manual spot-check", "Build a golden before/after image dataset and an automated false-positive-rate test now", "Drop MET-3 from the spec entirely"] | Record MET-3 as unverified-in-v1; thresholds are tunable constants validated by manual spot-check | ai-proposed | Building a golden dataset and harness is its own XL-sized infrastructure project with no existing precedent in this codebase (testing.md explicitly notes no Compose UI-test harness exists). Pretending a measured percentage exists without one would be dishonest; dropping the metric loses a real product signal. Naming the gap and the tuning method is the honest, smallest-scope answer. |
| The design's 'AGENT CHANGES' section implies Rectify knows which files the agent touched, but F-7's AnnotationEnvelope carries no such field and there's no git/MCP integration to source it. What should F-9 do? | ["Cut the AGENT CHANGES section for v1 (out of scope)", "Add a new MCP tool/field so the agent reports changed files back to Rectify", "Fake it with placeholder data"] | Cut the AGENT CHANGES section for v1 (out of scope) | ai-proposed | F-8 already cut an equivalent 'activity sub-line' for the identical reason (no data source), setting direct precedent. Adding a new reverse-channel from the agent back to Rectify is a separate, unscoped feature; placeholder data would be actively misleading in a verification tool. |
| How should per-annotation pixel-region diffing be implemented cross-platform? | ["Desktop-only expect/actual seam (new PixelDiffer), null-returning Android/iOS stand-ins, matching CropEncoder/BatchFileStore", "A single commonMain pixel-comparison implementation using ImageBitmap APIs", "Delegate pixel comparison to a new third-party image-diff library"] | Desktop-only expect/actual seam (new PixelDiffer), null-returning Android/iOS stand-ins, matching CropEncoder/BatchFileStore | ai-proposed | F-9 is desktop-only already (decision below), and this repo has two direct precedents (CropEncoder, BatchFileStore) for exactly this shape. Reusing the established pattern is smaller and more consistent than a new cross-platform abstraction or a new dependency for a few lines of byte comparison. |
| How should the Verify view fit into navigation, given MirrorHost has exactly one call site and its KDoc warns to keep it that way? | ["A new phase/state inside the existing MirrorHost/MirrorContent composition (no new Destination, no second call site)", "A new Destination reached through the navigation graph", "A modal/overlay dialog on top of the live mirror"] | A new phase/state inside the existing MirrorHost/MirrorContent composition (no new Destination, no second call site) | ai-proposed | presentation-feature-mirror's own CLAUDE.md documents MirrorHost as deliberately outside the navigation graph (Deviation 1) with no Destination case. The design's full-stage replacement with a 'Back to live' chip is exactly the shape of the existing live/frozen phase switch already in MirrorUiState — extending that state machine is the smallest change that respects the documented one-call-site constraint. |
| Is F-9's Verify feature desktop-only, like F-7 and F-8? | ["Yes — desktop-only, consistent with F-7/F-8 and NFR-10 (macOS-only v1)", "No — ship Android/iOS Verify UI and pixel diffing too"] | Yes — desktop-only, consistent with F-7/F-8 and NFR-10 | ai-proposed | NFR-10 already scopes all of v1 to macOS-only; F-7's send path and F-8's history storage this feature builds directly on top of are both desktop-only. Diverging for this one feature would be inconsistent and unrequested. |
| What is the pass/fail rule combining the pixel-region diff and the hierarchy/layout diff into one verdict? | ["AND — 'Fixed' only when both diffs are within threshold; either one failing (including an unmatched hierarchy node) means 'Still off'", "OR — 'Fixed' when either diff is within threshold", "Weighted score combining both into a single number against one threshold"] | AND — 'Fixed' only when both diffs are within threshold; either one failing (including an unmatched hierarchy node) means 'Still off' | ai-proposed | A visually-identical region that actually matched the wrong hierarchy node (or no node) would be a false 'Fixed' under OR or a naive weighted score — the worse failure mode for a trust-building verification tool. Requiring both signals to agree before declaring success is the conservative, defensible default; a moved/renamed/missing node is treated as 'Still off' by the same rule rather than as a special case. |
| What happens to the one-click follow-up's staged frame and crops — reuse the original frozen frame, or stage against the newly re-captured frame? | ["Stage the follow-up from the newly re-captured (AFTER) frame and its crops", "Stage the follow-up from the original frozen (BEFORE) frame, unchanged", "Let the user choose which frame to stage from"] | Stage the follow-up from the newly re-captured (AFTER) frame and its crops | ai-proposed | The agent needs to see current reality to make a second attempt, not the stale frame from before its own first fix; staging from BEFORE would send the agent back to a state it already addressed. A user-facing choice is unrequested complexity for a one-click action. |
| Should F-9 finally write the unused BatchStatusModel values (In progress, Fixed, Verified, Follow-up sent) via F-8's updateStatus hook? | ["Write Verified and Follow-up sent from F-9's own flow; leave In progress and Fixed unwritten (no v1 producer exists for either)", "Write all four values by also inventing producers for In progress and Fixed", "Don't call updateStatus at all in F-9"] | Write Verified and Follow-up sent from F-9's own flow; leave In progress and Fixed unwritten (no v1 producer exists for either) | ai-proposed | F-9 is the natural owner of exactly the two statuses its own flow produces. In progress would require an agent-side MCP signal that doesn't exist (a separate, unscoped feature), and nothing in v1 is positioned to ever mean 'Fixed' as distinct from 'Verified'. Not calling updateStatus at all would waste the exact hook F-8 built for this feature. |
