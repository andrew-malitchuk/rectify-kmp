# verify-and-rediff-loop — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-18T21:20:20Z |
| updatedAt | 2026-10-18T21:20:20Z |

## Gate

### type

human

### approvedBy

orchestrator-autonomous

### approved

true

### at

2026-10-18T21:20:20Z

## Provenance

### selfHash

216dc83b00387b1a224ecb006a5773fc4b440e4ebcd595dfa121ba08cd970e4e

### inputs

| artifact | hash |
| --- | --- |
| inputs/prd-handoff.json | 4aaa35b8cedb3876e3210e391036b5a38aa87f8610b8b2b18c6ebad234f7815c |

## Data

### title

Verify & Re-diff Loop

### goalStatement

The closed loop. On any sent batch, a 'Verify' action re-captures the live screen and diffs it per annotation box against the stored frozen frame using the confirmed combination of a pixel-region diff within the box plus a hierarchy/layout diff of the matched node. Annotations with no material difference are marked resolved; annotations that still differ surface a one-click follow-up that re-stages only that annotation for re-send through F-7. Diff thresholds must be tuned to hold the <5% false-positive target (MET-3) while surfacing real misses, and results stay human-reviewable. Why: no competitor found does per-annotation, per-region pixel+hierarchy verification against an original annotated frame — adb-mcp's verify_change only detects whether the UI changed at all, and AppContext's re-look is unstructured — so this is the strongest single differentiator and the answer to PROB-2.

### rawDescription

The closed loop. On any sent batch, a 'Verify' action re-captures the live screen and diffs it per annotation box against the stored frozen frame using the confirmed combination of a pixel-region diff within the box plus a hierarchy/layout diff of the matched node. Annotations with no material difference are marked resolved; annotations that still differ surface a one-click follow-up that re-stages only that annotation for re-send through F-7. Diff thresholds must be tuned to hold the <5% false-positive target (MET-3) while surfacing real misses, and results stay human-reviewable.

### sources

| type | ref | summary |
| --- | --- | --- |
| text | inputs/prd-handoff.json | Upstream PRD bundle (F-9, estimate XL, priority must, depends F-5/F-8). Defines FR-21 (Verify action on ANY sent batch—not gated on Fixed status), NFR-7/NFR-9 (persistence, theme parity), AC-23/AC-24 (diff per annotation, mark resolved or re-stage), SC-8/SC-9 (happy path and follow-up edge case), US-9/US-10 (developer and QA personas). Explicitly notes MET-3 target and per-annotation per-region verification as the core differentiator versus competitors. |
| text | inputs/design-verify-view.md | Design contract: Verify view with two 296×640 frames side-by-side (BEFORE/AFTER), per-annotation rects coloured by verdict (green=OK, orange/red=mismatch). Right panel 'Verification' (300px) shows result rows with ✓/! badges, result line (measurement deltas like 'top Δ6dp → 0dp'), verdict ('Fixed'/'Still off'). 'AGENT CHANGES' section lists files with +/− counts (e.g., CartItemView.kt +4 −2). Footer has 'Send follow-up · N' primary (⌘↩), 'Mark all as verified' secondary. Header shows four-step strip: Sent → Agent fixed · 3 files → Recaptured → Verified 2/3. Back button (← Back to live) resets to mirror/live state. |
| text | .pipeline/sdd/docs/history-batch-tracking/plan/plan.md | F-8 (merged) persists every batch: Room history_entry + history_annotation, frame/crop PNGs under ~/.rectify/history/<batchId>/, ≤128px thumbnail BLOB. Exposes HistoryRepository.updateStatus() (unused by F-8, reserved for F-9) and defines MirrorIntent.VerifyBatch as an inert stub. MirrorHistoryTab renders Verify button but gates it to Fixed status only. |
| text | .pipeline/sdd/docs/history-batch-tracking/review/review.md | F-8 tech debt: F-8 (crop file index 0-based in history vs 1-based in envelope—F-9 must know both), F-3 (stale Today header across midnight), F-4 (unbounded observe query), F-5 (data-repository-impl → data-database-impl module edge, Room backend no longer swappable). F-8 only writes Sent status, never Fixed. |
| text | .pipeline/sdd/docs/measure-mode/plan/plan.md | F-5 (merged) provides hierarchy-node snapping and dp measurement. Result strings like 'top Δ6dp → 0dp' and 'height 24dp → 32dp · want 48dp' are measure-mode output. F-9's hierarchy half of diff should reuse measure-mode rather than reinvent layout diff. |
| text | .pipeline/sdd/docs/resolve-and-send-mcp/plan/plan.md | F-7 (merged) ships AnnotationEnvelope (version + annotations + hierarchy + images), AnnotationHandoff interface, desktopMain FileSlotAnnotationHandoff (file-slot atomicity), CropEncoder seam (ImageBitmap + DeviceRect → ByteArray? for per-annotation crops), EnvelopeAssembly, mintSessionId. MCP server reads published slot. F-9's one-click follow-up must re-enter F-7's send path via MirrorIntent.ResolveAndSend on a single-annotation envelope. |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorHistoryTab.kt | HistoryEntryRow (lines 145–188) | Verify button currently gated to BatchStatusModel.Fixed only (line 179). Contradicts FR-21. Comment at line 177–178 confirms this is F-8's AC-36 gate, owned by F-9. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | verifyBatch (lines 388–390) | F-9's seam: currently logs and returns. Handler must implement re-capture, pixel+hierarchy diff, verdict marking, and one-click re-stage. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | MirrorIntent.VerifyBatch (lines 490–500) | Intent case defined but inert. Stub KDoc states F-9 owns re-capture and diff; F-8 ships only affordance. |
| rectify-kmp | data-database-impl/src/commonMain/kotlin/data/database/impl/core/dao/HistoryEntryDao.kt | observeAll (line 31) | F-8's tech debt F-4: no LIMIT, pulls every thumbnail BLOB and prompt string to MirrorUiState. F-9's diff loop must not repeat this pattern. |
| rectify-kmp | data-repository-impl/src/commonMain/kotlin/data/repository/impl/core/mapper/HistoryEntryMapper.kt | cropFile(index) vs envelope.cropFileName(index) | F-8's tech debt F-8: crop index 0-based in history, 1-based in handoff. F-9's diff loop must reconcile both conventions. |
| rectify-kmp | presentation-feature-mirror/src/desktopMain/kotlin/presentation/feature/mirror/core/crop/AwtCropEncoder.kt | CropEncoder seam (F-7 merged) | Crops ImageBitmap to DeviceRect, returns PNG bytes or null. F-9 reuses this for per-annotation crops in BEFORE frame during diff. |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| Verify & Re-diff View | 6833ecee (design-verify-view.md references raw/design/6833ecee.html) | raw/design/_verify-view.html | Two frames (296×640 each, BEFORE/AFTER), per-annotation rects coloured by verdict. Right panel: 'Verification' (300px wide), result rows with ✓/! badges and measurement deltas, 'AGENT CHANGES' file diff section, 'Send follow-up · N' / 'Mark all as verified' actions. Header: four-step strip and frame IDs. Back button absolute at top-left. |
| History Tab (context for verify affordance) | Raw cache available; MirrorHistoryTab.kt is current source of truth | raw/design/_history-tab.html | History rows, Today/Yesterday/Older groups, thumbnail, status pill, Verify button (currently gated to Fixed status only, contradicting FR-21). |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| this-project | F-8 history persistence (Room + file storage under ~/.rectify/history/<batchId>/); F-7 envelope assembly and slot publish; F-5 measure-mode output (dp deltas for hierarchy diff); F-6 payload/hierarchy/crop assembly. | Merged into development |

### glossary

| term | definition |
| --- | --- |
| Verify action | One-click affordance on a sent batch (history row) that re-captures live screen and diffs it per annotation against the stored frozen frame; marks resolved or surfaces follow-up. |
| Pixel-region diff | Comparison of image bytes in a crop region (the annotation's bounding box) between the frozen BEFORE frame and the re-captured AFTER frame. |
| Hierarchy/layout diff | Comparison of node properties (position, size, measured in dp) and text content between the original hierarchy snapshot and a re-measured hierarchy of the matched node in the re-captured frame. Uses F-5 measure-mode output format. |
| Material difference | Pixel or hierarchy diff that exceeds configured thresholds. The target is <5% false positives (MET-3). Below threshold → marked resolved (no follow-up needed). Above threshold → 'Still off' verdict, surfaces follow-up. |
| One-click follow-up | Re-stages only the annotation(s) still marked 'Still off' into a new AnnotationEnvelope, re-enters F-7 send path (MirrorIntent.ResolveAndSend), clears the rest to reduce agent churn. |
| Frozen frame | The PNG bytes originally captured from the device when the batch was sent (stored at ~/.rectify/history/<batchId>/frame.png). Serves as the BEFORE in diff comparisons. |
| Re-captured frame | A new screen capture from the device at verify-time, used as the AFTER in diff comparisons. Must match device state at send-time as closely as possible (same device, same fragment/activity, same annotations staged). |
| Verdict | Per-annotation conclusion: 'Fixed' (OK, green) when pixel+hierarchy diff is within thresholds, or 'Still off' (WARN/RED) when diff exceeds threshold. |
| AGENT CHANGES section | File-level diff summary shown in Verify view (e.g., CartItemView.kt +4 −2). Lists which source files the coding agent touched. Source TBD (ambiguity). |
| Four-step strip | Timeline shown in Verify view header: 'Sent 14:02' (when batch was sent) → 'Agent fixed · 3 files' (agent's response) → 'Recaptured 14:09' (when device was re-captured) → 'Verified 2/3' (diff result, active). |
| MET-3 (<5% false positives) | Metric: threshold tuning target. A 'false positive' is an annotation marked 'Fixed' by diff but the issue is still present on device, or marked 'Still off' when the issue is actually fixed. How is this measured? Ambiguity flagged. |

### attachments


### ambiguities

- CONTRADICTION: FR-21 states Verify action is NOT gated on a 'Fixed' status ('nothing in v1 is confirmed to set one'). But F-8 shipped with the Verify button gated to BatchStatusModel.Fixed only (MirrorHistoryTab.kt:179), and F-8 only ever writes 'Sent' status, making the button unreachable in practice. Must F-9 ungate the button to any sent batch, or does the gate remain justified? (Candidate resolution: F-8 was an initial AC-36 gate; F-9 removes it.)
- MET-3 TESTABILITY: MET-3 calls for a <5% false-positive target on the diff. Is there a test harness, golden dataset, or measurable definition of 'false positive' in this codebase? Or is threshold tuning by manual spot-check only? Must clarify whether MET-3 is verifiable here or a future instrumentation story.
- AGENT CHANGES DATA SOURCE: The design shows an 'AGENT CHANGES' section (CartItemView.kt +4 −2, etc.), implying Rectify knows which files the coding agent touched. F-7's AnnotationEnvelope contract does not obviously include file-level diffs or agent metadata. Where does this data come from? Is it a separate MCP query, metadata in the AnnotationEnvelope, or out-of-scope for v1?
- PIXEL DIFFING CROSS-PLATFORM: Verify must compare image regions (crops of BEFORE and AFTER frames). Does raw pixel access (e.g., ImageBitmap.readPixels or similar) exist and work identically on Android, iOS, and Desktop? Or must the pixel-diff algorithm live behind a CropEncoder-style expect/actual seam (e.g., PixelDiffer with desktopMain implementation)?
- VERIFY VIEW SCOPE AND NAVIGATION: Is the Verify view a new full-stage view replacing the live mirror (like 'Frozen' stage), or an overlay/modal over the live mirror? How does it interact with the single MirrorHost call site in RectifyWindow? The design shows it as a full-window replacement; confirm navigation flow.
- DESKTOP-ONLY: F-7 and F-8 are both desktop-only (F-7's MCP server, F-8's file storage). Confirm F-9 is also desktop-only (no Android/iOS verify affordance, no platform.ios/platform.android Verify UI). If so, diff algorithm and pixel access can be desktop-only expect/actual.

### dictation



### rawCacheDeltas

| id | type | ref | path | fetchedAt | hash | summary |
| --- | --- | --- | --- | --- | --- | --- |
| prd-handoff-f9 | text | inputs/prd-handoff.json | raw/prd-handoff.json | 2026-10-19T00:00:00Z | a7b4b0d1c39a04a00aec0acd67900ad85595779106bea6f82ae1265133169110 | Upstream PRD bundle (F-9, estimate XL, priority must). Contains FR-21 (Verify on any sent batch, not gated on Fixed), NFR-7/9, AC-23/24, SC-8/9, US-9/10, and goal statement emphasizing per-annotation per-region pixel+hierarchy verification as strongest differentiator. |
| design-verify-view | text | inputs/design-verify-view.md | raw/design-verify-view.md | 2026-10-19T00:00:00Z | design-verify-view-md | Extracted design contract for Verify view: two 296×640 frames (BEFORE/AFTER), per-annotation rects with verdict colours, right panel with result rows (measurement deltas), AGENT CHANGES section, 'Send follow-up · N' and 'Mark all as verified' actions, four-step header strip, back button. |
