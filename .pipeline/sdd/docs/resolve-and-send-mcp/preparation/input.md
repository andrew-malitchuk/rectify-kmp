# resolve-and-send-mcp — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-18T10:24:00Z |
| updatedAt | 2026-10-18T10:29:55Z |

## Gate

### type

human

### approved

true

## Provenance

### selfHash

984bb3fdd6caffdcad5fd46524735e64febb9d77c2e07fbda3a73ac7c6a8561e

### inputs

| artifact | hash |
| --- | --- |
| resolve-and-send-mcp.json | ec5c0e2417b2e1a118ab1dffacb934e514a210f2618e56221cc8ea223f9751df |

## Data

### title

Resolve & Send via Local MCP

### goalStatement

The handoff itself: toolbar staged-annotation counter, 'Clear' action, and the primary 'Resolve & Send' button (disabled at zero staged annotations and while a send is in flight). On send, Rectify packages the full frame screenshot, per-annotation crops, view-hierarchy XML dump and generated prompt into a versioned payload envelope and transmits it to the locally running Claude Code agent through the existing local 'rectify' MCP server for retrieval via a get_latest_annotation-style tool — the sole transport in v1, no file or webhook fallback. During send a status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) animates with colored dots, then a toast confirms 'Sent N annotations… · <time>s' with an 'Open session' link; afterwards live mirroring resumes automatically and the counter resets to 0. All data stays local; typical localhost sends complete in under ~2s.

### rawDescription

The handoff itself: toolbar staged-annotation counter, 'Clear' action, and the primary 'Resolve & Send' button (disabled at zero staged annotations and while a send is in flight). On send, Rectify packages the full frame screenshot, per-annotation crops, view-hierarchy XML dump and generated prompt into a versioned payload envelope and transmits it to the locally running Claude Code agent through the existing local 'rectify' MCP server for retrieval via a get_latest_annotation-style tool — the sole transport in v1, no file or webhook fallback. During send a status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) animates with colored dots, then a toast confirms 'Sent N annotations… · <time>s' with an 'Open session' link; afterwards live mirroring resumes automatically and the counter resets to 0. All data stays local; typical localhost sends complete in under ~2s.

### sources

| type | ref | summary | cached |
| --- | --- | --- | --- |
| text | .pipeline/prd/docs/rectify/handoff/resolve-and-send-mcp.json | Upstream PRD handoff: F-7, FR-16..19, NFR-3/4/12, AC-17..21/AC-32, priority must, phase PH-1, estimate L, dependsOn F-6, linkedGoals GOAL-3/GOAL-4. Payload envelope transmitted via local 'rectify' MCP server with get_latest_annotation-style tool. Status sequence Streaming→Frozen→Dumping hierarchy→Sent with toast confirmation + Open session link. Resume mirroring and reset counter post-send. All data local, <2s typical. |  |
| text | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/README.md | Input manifest: design refs (UI kit buttons, toast spec, send status steps including failure variant); design screenshots (payload, success, err-stream screens); PRD handoff location. |  |
| figma | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | Rectify UI Kit v0.4: Resolve & Send button (primary, 28px height, 12px padding, r7, 13/500 weight, --primary bg, --onPrimary text) with ⌘↩ shortcut affix (JetBrains Mono 10.5px, opacity 0.6). Clear action (ghost, transparent, --ink2). Disabled state (--seg bg, --ink3 fg, caption 'disabled until 1 annotation'). Toast spec ('Sent 3 annotations to Claude Code', 24px Open session action, r6, 12/500). Send status step list: ['Streaming','Frozen','Dumping hierarchy','Sent']; failure variant: ['Stream dropped','Frozen','Dumping hierarchy','Sent'] with failed flag. | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html |
| figma | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html | Rectify Window screens: 16 states including payload, success, err-stream. Implementation hint: canSend/cannotSend logic; sendLabel = phase === 'dumping' ? 'Sending…' : 'Resolve & Send'. | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html |
| code | .pipeline/sdd/docs/payload-preview/plan/plan.data.json | F-6 payload-preview already shipped: PAYLOAD_FORMAT_VERSION = 1 (in core/payload); FrozenFrame carries frameId (stamped from private ViewModel counter on stageAnnotation), deviceSize, annotations, promptOverride; payload bundle = full frame screenshot + per-annotation crops + hierarchy XML + generated prompt. Frame bitmap on MirrorViewModel.frame StateFlow<ImageBitmap?>, hierarchy on MirrorViewModel.frozenHierarchy StateFlow<HierarchySnapshotModel>. |  |
| code | .pipeline/sdd/docs/registry/features.json#error-handling-and-recovery | F-3 shipped: MirrorErrorCard/MirrorDropBanner projections, Reconnect/RetryDiscovery/RestartAdb intents, MirrorUiState.canResumeLive property, error escalation with BUDGETED_ESCALATION on adb timeout. |  |
| code | .pipeline/sdd/docs/registry/features.json#android-device-discovery-mirroring | F-2 shipped: MirrorTransport interface (frames: Flow<MirrorFrame>, stats: StateFlow<TransportStats>), ScreencapMirrorTransport impl, live resolution/fps/latency stats in status bar. |  |
| code | .pipeline/sdd/docs/registry/features.json#drag-annotate-frozen-frame | F-4 shipped: StagedAnnotation (rect, comment, index), FrozenFrame (deviceSize, annotations, selectedIndex, frameId, deviceLabel, promptOverride), FrameGeometry pure conversion (storage + render), Resume Live intent. |  |
| code | .pipeline/sdd/docs/registry/features.json#app-shell-window-layout | F-1 shipped: RectifyWindowShell (ShellToolbar with slots, StageSurface, SidePanelFrame with Notes/Payload/History tabs, ShellStatusBar), ShellTab enum, ShellState/ShellWindowDefaults (1240x820 initial, 1024x700 min), pref_side_panel_width. |  |
| code | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | MirrorContract: MirrorUiState (platform, session, frozen, measure, hierarchyStatus, stagedCount getter, canResumeLive property), MirrorIntent (StageAnnotation, SelectAnnotation, CommentAnnotation, DeleteAnnotation, ClearStaged, EditPrompt, ResumeLive, Reconnect, RetryDiscovery, RestartAdb, LocateAdb, AdbPathChosen), FrozenFrame, MirrorErrorCard, MirrorDropBanner. MirrorSession (Idle\|Streaming\|Lost). MirrorStage (Picker\|Mirror\|NoSignal\|IosDeferred\|Frozen). |  |
| code | .pipeline/prd/docs/rectify/research/research.json | Competitor research: Aki (macOS, local MCP with tools read_annotations, wait_for_annotations, get_annotation_image, resolve_annotation); adb-mcp (73 tools for Claude Code/Cursor); Jam.dev MCP (logs/network/actions to agent); Bugshot (skill). Aki is nearest precedent for tool naming and session routing. |  |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| this-project | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorContract.kt | MirrorContract | Existing intent/state surface: ResumeLive, Reconnect, ClearStaged, EditPrompt intents; MirrorUiState.canResumeLive, stagedCount; FrozenFrame, StagedAnnotation; will need new Send-related intent(s) and send-status projection. |
| this-project | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | MirrorViewModel | Existing state machine: frame StateFlow<ImageBitmap?>, frozenHierarchy StateFlow<HierarchySnapshotModel>, stageAnnotation intent handler that increments frameId. Will need send handler that packages payload, invokes MCP client, updates send-status state, then resumes live. |
| this-project | data-device-api/src/commonMain/kotlin/data/device/api/source/datasource/MirrorTransport.kt | MirrorTransport | Screenshot capture transport seam; frames(device): Flow<MirrorFrame>, stats: StateFlow. F-7's MCP transport is independent (different contract, payload vs frames). |
| this-project | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/core/payload/ | PAYLOAD_FORMAT_VERSION | F-6 declares internal const PAYLOAD_FORMAT_VERSION = 1 in core/payload for AC-32. F-7 must read and transmit this version in the envelope sent via MCP. |
| this-project | compose-application/src/desktopMain/kotlin/presentation/application/source/window/RectifyWindow.kt | RectifyWindow | Desktop window entry point: toolbar, stage slots, side panel with ShellTab branches. F-7 will add button + toast in toolbar/shell, and send status indicator (FR-18). |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| design-rectify-ui-kit.html | resolve-send-button-primary | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | Resolve & Send button: primary state, 28px height, 12px padding, r7, 13/500 weight, --primary bg, --onPrimary text. Disabled state: --seg bg, --ink3 fg, caption 'disabled until 1 annotation'. ⌘↩ shortcut affix (JetBrains Mono 10.5px, opacity 0.6). |
| design-rectify-ui-kit.html | clear-action-ghost | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | Clear action: ghost button, transparent bg, --ink2 fg, same 28px height, dismisses all staged annotations. |
| design-rectify-ui-kit.html | send-status-steps | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | Send status step list: ['Streaming','Frozen','Dumping hierarchy','Sent'] with colored dots. Failure variant: ['Stream dropped','Frozen','Dumping hierarchy','Sent'] with failed flag. |
| design-rectify-ui-kit.html | send-complete-toast | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | Toast spec: 'Sent 3 annotations to Claude Code · frame + hierarchy · <time>s' + 24px 'Open session' action button, r6, 12/500 weight. |
| design-rectify-window-screens.html | payload-screen | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html | Payload screen: staged counter, frame/crops/hierarchy preview, Resolve & Send button, Toast on completion. |
| design-rectify-window-screens.html | success-screen | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html | Success screen post-send: resuming live mirror, counter at 0, toast confirmation + Open session link. |
| design-rectify-window-screens.html | err-stream-screen | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html | err-stream screen: 'Stream dropped' banner over frozen frame with annotations, Reconnect option (does NOT destroy annotations per FR-24/AC-51). |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| this-project | F-6 payload-preview (merged): PAYLOAD_FORMAT_VERSION = 1 in core/payload; FrozenFrame augmented with frameId, deviceLabel, promptOverride; payload bundle assembled = frame screenshot + per-annotation crops + hierarchy XML + generated prompt. MirrorViewModel.frame and MirrorViewModel.frozenHierarchy already publish their data. No send transport yet. | .pipeline/sdd/docs/payload-preview/plan/plan.data.json |
| this-project | F-3 error-handling-and-recovery (merged): MirrorErrorCard, MirrorDropBanner projections; Reconnect intent (re-attempt session, keep frozen frame + annotations); MirrorUiState.canResumeLive property guards Resume affordance during device loss. Send failure ('Stream dropped') must integrate with existing error surface. | .pipeline/sdd/docs/registry/features.json#error-handling-and-recovery |
| this-project | F-4 drag-annotate-frozen-frame (merged): StagedAnnotation (rect, comment, index), FrozenFrame as reduced state, ResumeLive intent (unpin, drop annotations, back to live). No send or status tracking yet. | .pipeline/sdd/docs/registry/features.json#drag-annotate-frozen-frame |
| this-project | F-2 android-device-discovery-mirroring (merged): MirrorTransport interface for screenshot capture; live discovery, device picker, mirroring loop. No payload transmission transport. | .pipeline/sdd/docs/registry/features.json#android-device-discovery-mirroring |
| this-project | F-1 app-shell-window-layout (merged): RectifyWindowShell, ShellToolbar, SidePanelFrame with Notes/Payload/History tabs, ShellStatusBar. F-7 counter badge and Resolve & Send button mount in toolbar; send status and toast render in shell host. | .pipeline/sdd/docs/registry/features.json#app-shell-window-layout |
| external | Aki (macOS annotation tool): ships local MCP server with tools read_annotations, wait_for_annotations, get_annotation_image, resolve_annotation for Claude Code session handoff. Nearest precedent for tool naming and session routing. | https://dev.to/muriloprataviera/i-run-5-claude-code-sessions-at-once-so-i-built-a-mac-app-to-point-at-what-needs-fixing-1513 |
| external | adb-mcp (iksnerd): MCP server with 73 tools driving Android emulators/devices over adb, including screenshots + UI hierarchy reads, tap/swipe, logcat. No annotation UI, but demonstrates agent-side hierarchy integration. | https://github.com/iksnerd/adb-mcp |

### glossary

| term | definition |
| --- | --- |
| payload envelope | Versioned container transmitted via MCP: {version: 1, frameScreenshot, perAnnotationCrops[], hierarchyXml, generatedPrompt, ...}. AC-32 requires explicit format/schema version field. |
| staged annotations | List of user-drawn bounding boxes with comments, pinned to a frozen frame. Counter shows in toolbar. Cleared by Clear action or ResumeLive intent. Sent as payload on Resolve & Send. |
| freeze | Render-path switch that pins one frame while capture loop keeps running. Frame gets a frameId stamp. Annotations are collected against frozen frame. Unfreeze via ResumeLive intent. |
| send status sequence | Animation of four steps: Streaming → Frozen → Dumping hierarchy → Sent. Failure variant: Stream dropped → Frozen → Dumping hierarchy → Sent. Each step marked with colored dot indicator. |
| Open session link | Toast action that (semantics TBD) opens the Claude Code session that received the payload. Behavior varies by platform (desktop). |

### attachments

| name | path | type |
| --- | --- | --- |
| design-rectify-ui-kit.html | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-ui-kit.html | html |
| design-rectify-window-screens.html | .pipeline/sdd/docs/resolve-and-send-mcp/inputs/design-rectify-window-screens.html | html |

### ambiguities

- Q: Does the local 'rectify' MCP server already exist in this repo, or outside it, or is it still to be built? The handoff says 'the existing local rectify MCP server' but no MCP code is found in src/. Is it an external dependency, or new infrastructure for F-7?
- Q: What is the exact name of the retrieval tool the agent calls to get the payload? The handoff says 'a get_latest_annotation-style tool' but is it get_latest_annotation, resolve_annotation (per Aki), or something else? Must be settled to define the MCP contract.
- Q: What does the 'Open session' link do on desktop? (A) open a web browser to a session URL? (B) launch/focus a running Claude Code instance? (C) copy a session ID to clipboard? (D) something else? Design shows the affordance but semantics are unclear.
- Q: What happens to staged annotations if the send fails mid-flight? Do they persist (user can retry)? Are they cleared (user restarts)? Or something in between? AC-20/AC-21 cover success but failure recovery is not spelled out.
- Q: Does the payload envelope already carry a format version field in F-6's structure, or does F-7 add it for AC-32? F-6 plan mentions 'internal const PAYLOAD_FORMAT_VERSION = 1' but does not say whether it is serialized into the transmitted bytes.
- Q: How does the MCP transport work? (A) stdio (subprocess spawning)? (B) localhost TCP socket (port TBD)? (C) HTTP (port TBD)? (D) something else? No transport plumbing is visible in the codebase.
- Q: What is the scope of the 'Dumping hierarchy' phase (FR-18, FR-50)? Is it Android-only (matching F-5 measure-mode), or does iOS build a hierarchy dump too? Design shows it unconditionally in the status sequence but F-6 plan says 'Android only, matching measure-mode's AS-19/AS-23 boundary.'
- Q: Does the send operation block the UI (caller waits for completion), or run in background with status updates streamed? Design shows step-by-step animation but flow (blocking vs async) is not stated.

### dictation

No dictation provided — this spec is a pure upstream handoff (F-7, PRD feature bundle) with no engineer interview.

### rawCacheDeltas
