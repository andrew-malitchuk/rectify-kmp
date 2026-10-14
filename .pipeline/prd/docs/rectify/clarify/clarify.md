# rectify — clarify

## Envelope

| field | value |
| --- | --- |
| stage | clarify |
| step | clarify |
| status | done |
| producedBy | sdd-prd-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-11T00:00:00Z |
| updatedAt | 2026-10-11T12:06:34Z |

## Gate

### type

human

### approved

true

### approvedBy

human

### at

2026-10-11T00:00:00Z

## Provenance

### selfHash

5f67cd1a15072b7df0589a831081de9b5b759090135a91ef52734c1f40d5f54b

### inputs

| artifact | hash |
| --- | --- |
| intake.json | a01058040380b87160b6a1cb991828cb5f0c75cf375786c37a0f290032666c57 |

## Data

### problemStatement

Developers and QA testers who spot visual/UI bugs on a connected Android device (or iOS device in v1.1+) have no fast, structured way to hand precise, measurement-rich bug context (exact region, pixel/dp gaps, underlying view-hierarchy node) to a locally running Claude Code coding agent, and no built-in way to confirm a fix actually landed without manually re-testing the same spot by hand.

### problems

| id | text |
| --- | --- |
| PROB-1 | Reporting a UI bug today means manually screenshotting, describing position/size in prose, and losing precise measurement/hierarchy context — producing slow, ambiguous handoffs to a coding agent. |
| PROB-2 | There is no closed loop: after a coding agent claims a fix, the reporter must manually re-open the app and eyeball the same spot, with no diff against the original bug frame. |
| PROB-3 | Visual bug triage across Android (adb/scrcpy) and iOS (idb/simctl) today requires manual device wrangling with no single tool bridging device mirroring, annotation, and agent handoff. |
| PROB-4 | An existing local MCP server (`rectify`, exposing `get_latest_annotation`) already anticipates a structured handoff path, but no client currently produces or sends that structured payload. |

### personas

| id | name | description | needs |
| --- | --- | --- | --- |
| PER-1 | Mobile Developer | Engineer running the app under test and a local Claude Code session against its repo; spots bugs while manually exercising the app and wants fixes applied with minimal back-and-forth. | ["Annotate a bug precisely (box + measurement) without leaving the live device mirror", "Send full visual + structural context to Claude Code in one action", "Confirm a fix actually resolved the specific annotated issue before moving on"] |
| PER-2 | QA Tester | Tester running structured or exploratory passes on a device, often without direct repo access; relies on Rectify to hand off findings to a developer's running Claude Code session. | ["Flag multiple bugs quickly during a single pass without losing context between them", "Produce reports precise enough that a developer/agent doesn't need to re-ask what's wrong", "See the status of previously reported batches (fixed / in progress) without re-testing everything"] |

### goals

| id | text | type |
| --- | --- | --- |
| GOAL-1 | Let a reporter capture a precise, measurement-rich UI bug (box, comment, optional dp/pixel gaps, hierarchy node) in seconds without leaving the live device mirror. | user |
| GOAL-2 | Close the loop between sending a bug report and confirming the fix, without requiring manual re-testing of the same UI region. | user |
| GOAL-3 | Reduce back-and-forth between bug reporter and coding agent by delivering one structured payload (screenshot, crops, hierarchy XML, prompt) per send. | business |
| GOAL-4 | Provide a reliable local handoff mechanism to a running Claude Code session via the existing `rectify` MCP server. | technical |
| GOAL-5 | Deliver a complete Android-first v1 workflow (mirror -> annotate -> measure -> send -> verify), with iOS deferred to v1.1+. | business |

### nonGoals

- Cloud sync, multi-user accounts, or any auth/login model in v1
- Windows or Linux support
- Simultaneous multi-device mirroring in v1
- iOS mirroring and iOS-specific flows in v1 (deferred to v1.1+)
- Automated bug fixing or code generation inside Rectify itself (fixing happens in the Claude Code agent, not in Rectify)

### successMetrics

| id | text | target |
| --- | --- | --- |
| MET-1 | Time from spotting a bug to Claude Code receiving the structured report. | Median under 30s per sent batch |
| MET-2 | Share of sent batches that reach a 'Fixed' status confirmed by one Verify cycle. | >= 70% within first Verify attempt |
| MET-3 | Verify false-positive rate (marks a batch resolved when the issue persists). | < 5% |
| MET-4 | First-attempt device-connect success rate (adb/idb detection through live mirror start) for supported setups. | >= 95% |

### functionalRequirements

| id | text | priority |
| --- | --- | --- |
| FR-1 | On launch, detect installed adb and idb tool versions and show a status dot per platform in the toolbar. | must |
| FR-2 | Show a platform switch (Android/iOS) in the toolbar on every screen; Android is functional in v1, iOS is deferred to v1.1+. | must |
| FR-3 | Provide a device picker dropdown with Physical-device and Emulator sections (running, or shut-down with an inline Boot button), navigable via arrow keys and Enter. | must |
| FR-4 | Provide a primary 'Start mirroring' action that connects to the selected device and begins the live mirror stream. | must |
| FR-5 | When no device is connected, poll adb every 2s and show a 'NO SIGNAL' empty state with guidance to plug in a USB-debug device or boot an emulator inline. | must |
| FR-6 | Support click-and-drag on the live mirror to draw a selection box, rendering a dashed red outline, a live device-pixel size label, and ruler/tick coordinate guides during the drag. | must |
| FR-7 | On drag release, freeze the exact current frame and create a new staged annotation anchored to the drawn box. | must |
| FR-8 | On a frozen frame, dim everything outside annotation boxes and show a per-annotation popover with a numbered badge, an autofocused free-text comment field (placeholder 'What's wrong here?'), a delete control, and a size/position meta line; Enter saves, clicking the delete control removes the annotation. | must |
| FR-9 | When annotation popovers would crowd/overlap, relocate them to a gutter/sidebar panel connected to their boxes via leader lines. | should |
| FR-10 | While holding Option over an annotation box, enter Measure mode: snap to the matched underlying view-hierarchy node, show dashed guide boxes for neighboring views and dp/pixel gap measurements in a floating 'Measuring' panel, highlight misalignment in red, and attach the measurement data to that annotation's note (prefixed with the Option symbol). | must |
| FR-11 | Use the canonical 'Rectify Window' layout: a bezel-less live mirror with a side panel exposing exactly three tabs (Notes, Payload, History). | must |
| FR-12 | In the Payload tab's Prompt section, show the auto-generated text prompt, its token-count estimate, and an Edit affordance. | must |
| FR-13 | In the Payload tab's Crops section, show a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'. | must |
| FR-14 | In the Payload tab's Hierarchy section, show a monospace, indented view-hierarchy tree with matched/flagged nodes visually marked. | must |
| FR-15 | Show a Payload footer summarizing contents ('frame.png · N crops · hierarchy.xml') and total payload size. | must |
| FR-16 | In the toolbar, show a staged-annotation counter, a 'Clear' action that removes all staged annotations, and a primary 'Resolve & Send' button that is disabled while zero annotations are staged or while a send is in progress. | must |
| FR-17 | On 'Resolve & Send', package the full frame screenshot, per-annotation crops, view-hierarchy XML dump, and generated prompt, and transmit them to the locally running Claude Code agent via the local 'rectify' MCP server (e.g. for retrieval through a `get_latest_annotation`-style tool). | must |
| FR-18 | During send, show a visible status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) with colored dots, then confirm with a toast ('Sent N annotations to Claude Code · frame + hierarchy · <time>s') including an 'Open session' link. | must |
| FR-19 | After a successful send, resume live mirroring automatically and reset the staged-annotation counter to 0. | must |
| FR-20 | Provide a History tab listing every batch ever sent, grouped by time period, each row showing a thumbnail crop, title/summary, timestamp, and a status chip (e.g. Fixed, In progress). | must |
| FR-21 | On batches with status 'Fixed', provide a 'Verify' action that re-captures the live screen and diffs it, per annotation box, against the original frozen frame; matches mark the annotation resolved, mismatches surface a one-click follow-up that re-stages that single annotation for re-send. | must |
| FR-22 | Show a status bar with live stream stats: tool version, resolution, fps, and latency. | should |
| FR-23 | Render structured error-card states (icon-in-circle, title, body, optional copyable command block, primary+secondary actions, footer diagnostic line) for: adb not in PATH, no device detected, and multiple devices connected (with an inline device list/picker instead of a modal, defaulting the primary action to the top-listed device). | must |
| FR-24 | On a mid-session stream drop, show a non-blocking floating banner over the stale mirror ('Stream dropped' / 'Your N annotations are kept on the frozen frame' / 'Reconnect'); staged annotations stay pinned and 'Resolve & Send' remains usable while disconnected. | must |
| FR-25 | (v1.1+) Support iOS simulator/device mirroring via idb+simctl, including simulator-not-booted and device-not-trusted error-card states. | could |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-1 | performance | Live mirror glass-to-glass latency should stay low enough (target under ~150ms) that drag-to-select and freeze feel responsive. |
| NFR-2 | performance | Measure-mode hierarchy-node lookup on Option-hover should resolve in under ~100ms so the measuring panel feels instantaneous. |
| NFR-3 | performance | Resolve & Send payload transmission to the local MCP server should typically complete in under ~2s on localhost for a typical batch. |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication. |
| NFR-5 | security | adb/idb commands are only ever invoked against the user's explicitly selected, connected device — never an arbitrary or remote target. |
| NFR-6 | reliability | Stream-drop handling must preserve all staged annotations without loss across a disconnect/reconnect cycle. |
| NFR-7 | reliability | History and batch metadata persist locally and survive app restarts. |
| NFR-8 | a11y | Device picker, annotation popovers, and error-card actions are fully keyboard-navigable (arrow keys/Enter for pickers, Enter/Escape for popovers). |
| NFR-9 | ux | All screens support full light and dark theme parity using the defined signal-color system (red = bug, green = fixed, orange = warning, blue = measurement) with sufficient contrast in both themes. |
| NFR-10 | compatibility | v1 targets macOS only; no Windows/Linux builds. |
| NFR-11 | performance | Token-count estimation for the generated prompt computes without perceptible delay (target under ~200ms) before Resolve & Send is enabled. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-1 | Given adb and idb are installed, when the app launches, then the toolbar shows a status dot reflecting detected tool versions for both platforms. | must | demo | false |
| AC-2 | Given a physical device is connected and an emulator is running, when the device picker is opened, then both appear in distinct Physical/Emulator sections and arrow keys plus Enter select an entry. | must | test | false |
| AC-3 | Given a device is selected in the picker, when the user clicks 'Start mirroring', then the live mirror begins streaming. | must | test | false |
| AC-4 | Given no device is connected, when adb polling runs, then the 'NO SIGNAL' state shows with guidance text and an inline emulator picker plus Boot button. | must | demo | false |
| AC-5 | Given live mirror is streaming, when the user drags on the mirror, then a dashed red outline, live pixel-size label, and ruler guides render in real time. | must | test | false |
| AC-6 | When the user releases the drag, then the exact frame is frozen and a new staged annotation is created anchored to the drawn box. | must | test | false |
| AC-7 | Given a frozen frame with one annotation, then everything outside the box dims and a popover with numbered badge, autofocused comment field, delete control, and meta line appears. | must | test | false |
| AC-8 | Given an annotation popover is focused, when the user presses Enter, then the comment saves; when the user clicks the delete control, then the annotation is removed and the staged count decrements. | must | test | false |
| AC-9 | Given three annotations are drawn close together, when their popovers would overlap, then they relocate to a sidebar/gutter panel connected via leader lines. | should | demo | false |
| AC-10 | Given a frozen frame with an annotation box, when the user holds Option over it, then the tool snaps to the matched view-hierarchy node and shows neighbor guide boxes with dp gap measurements in a 'Measuring' panel. | must | test | false |
| AC-11 | Given a measured element has misalignment (e.g. undersized hit target), then the offending dimension is highlighted in red and the note is prefixed with the Option-mode marker and carries the measurement data. | must | inspection | false |
| AC-12 | Given the canonical window layout is confirmed, then the side panel exposes exactly three tabs: Notes, Payload, History, over a bezel-less mirror. | must | inspection | true |
| AC-13 | Given at least one staged annotation exists, when the user opens the Payload tab, then a generated text prompt with token-count estimate and an Edit affordance is shown. | must | test | false |
| AC-14 | Given staged annotations exist, when viewing Payload > Crops, then a 3-column grid shows each annotation's cropped image with its red outline baked in and caption 'from frame <id>'. | must | test | false |
| AC-15 | Given staged annotations exist, when viewing Payload > Hierarchy, then a monospace indented tree displays with matched/flagged nodes visually marked. | must | test | false |
| AC-16 | Given the Payload tab is open, then its footer summarizes 'frame.png · N crops · hierarchy.xml' plus a total size that matches the actual payload. | must | inspection | false |
| AC-17 | Given zero annotations are staged, then 'Resolve & Send' is disabled; given one or more are staged, it is enabled; 'Clear' removes all staged annotations and resets the counter to 0. | must | test | false |
| AC-18 | When the user clicks 'Resolve & Send', then the frame screenshot, per-annotation crops, hierarchy XML, and generated prompt are transmitted via the local MCP server such that a Claude Code session calling the retrieval tool receives the complete payload. | must | test | true |
| AC-19 | When sending, then the status sequence Streaming -> Frozen -> Dumping hierarchy -> Sent displays in order with colored dots, and on completion a toast shows the sent-count/time summary with an 'Open session' link. | must | demo | false |
| AC-20 | While a send is in progress, 'Resolve & Send' is disabled and only re-enables after the toast confirms or the send fails. | must | test | false |
| AC-21 | After a successful send, then live mirroring resumes automatically and the staged-annotation counter resets to 0. | must | test | false |
| AC-22 | Given batches have been sent previously, when the History tab is opened, then batches are grouped by time period, each showing thumbnail, summary, timestamp, and status chip. | must | test | false |
| AC-23 | Given a batch has status 'Fixed', when the user clicks 'Verify', then the app re-captures the live screen and diffs it, per annotation box, against the stored frozen frame using a pixel-region diff plus a hierarchy/layout diff of the matched node. | must | test | true |
| AC-24 | Given a Verify diff finds no material difference for an annotation, then that annotation is marked resolved; given a difference remains, then a one-click follow-up re-stages only that annotation for re-send. | must | test | false |
| AC-25 | Given live mirror is active, then the status bar displays current tool version, resolution, fps, and latency, updating at least once per second. | should | demo | false |
| AC-26 | Given adb is not found on PATH, then an error card renders with the install command (with Copy), primary 'Locate adb…' and secondary 'Retry' actions. | must | test | false |
| AC-27 | Given no Android device is detected after launch, then an error card renders with the kill-server command and primary 'Restart adb' / secondary 'Boot emulator instead' actions. | must | test | false |
| AC-28 | Given 3 devices are connected simultaneously, then the error card shows an inline device list (not a modal) and the primary action names the top device (e.g. 'Mirror Pixel 8 Pro'). | must | test | false |
| AC-29 | Given the stream drops mid-session with staged annotations present, then a non-blocking banner appears stating the stream dropped and that annotations are kept on the frozen frame, with a 'Reconnect' primary action; 'Resolve & Send' remains clickable and annotations remain pinned throughout. | must | test | false |
| AC-30 | Given the user toggles light/dark theme, then all screens render correctly in both themes with consistent signal colors (red/green/orange/blue) and no unreadable contrast regions. | must | inspection | false |
| AC-31 | (v1.1+) Given iOS is selected as platform, when the simulator isn't booted, then an error card with 'Boot simulator' primary / 'Choose another' secondary renders; when the device isn't trusted, an error card with "I've tapped Trust — retry" primary / 'Use simulator' secondary renders. | could | test | false |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-1 | First-run setup and tool detection | happy | ["Launch app", "Detect adb/idb and show status dots", "Open device picker showing Physical/Emulator sections", "Select a device via keyboard or click", "Click Start mirroring"] |
| SC-2 | No device connected (empty state) | edge | ["No device is connected", "App polls adb every 2s", "Show NO SIGNAL placeholder with guidance", "Offer inline emulator picker + Boot button"] |
| SC-3 | Draw and freeze a single annotation | happy | ["Drag on the live mirror to draw a box", "Release to freeze the exact frame", "Popover appears autofocused", "Type a comment and press Enter to save"] |
| SC-4 | Multiple crowded annotations relocate to gutter | edge | ["Draw three annotation boxes close together", "Popovers would overlap", "Popovers relocate to sidebar gutter with leader lines"] |
| SC-5 | Measure a misaligned element | happy | ["Hold Option over an annotation box", "View snaps to matched hierarchy node", "Neighbor guide boxes and dp gaps render", "Misalignment highlighted red and attached to the note"] |
| SC-6 | Review payload before sending | happy | ["Open Payload tab", "Review generated prompt and token estimate", "Review crops grid", "Review hierarchy tree", "Check footer summary"] |
| SC-7 | Resolve & Send happy path | happy | ["Click Resolve & Send", "Status sequence animates through each stage", "Toast confirms with Open session link", "Mirroring resumes and counter resets"] |
| SC-8 | Review and verify a fixed batch in History | happy | ["Open History tab", "Locate a Fixed batch", "Click Verify", "Diff runs per annotation against the frozen frame", "Annotation marked resolved"] |
| SC-9 | Follow-up re-send after a failed verify | edge | ["Verify finds a remaining mismatch for an annotation", "Click the one-click follow-up", "That annotation is re-staged", "Resolve & Send again"] |
| SC-10 | adb not on PATH | error | ["Launch with adb missing from PATH", "Error card renders with install command", "User copies command or clicks Locate adb…/Retry"] |
| SC-11 | No Android device detected | error | ["adb present but no device attached", "Error card renders with kill-server command", "User runs suggested command or boots an emulator instead"] |
| SC-12 | Multiple devices connected | error | ["3 devices are connected simultaneously", "Inline device list/picker renders (no modal)", "User accepts the top-named device or picks another"] |
| SC-13 | Stream dropped mid-annotation | error | ["Stream drops while annotations are staged", "Non-blocking banner appears over the stale mirror", "Annotations remain pinned", "User clicks Reconnect or still sends via Resolve & Send"] |
| SC-14 | (v1.1+) iOS simulator not booted / device not trusted | error | ["Select iOS platform", "Simulator not booted -> error card offers Boot simulator", "Or device not trusted -> error card offers Trust-and-retry / Use simulator"] |

### assumptions

| id | text |
| --- | --- |
| AS-1 | Only one device is mirrored at a time per session; switching devices clears or prompts to clear staged annotations. |
| AS-2 | History and batch metadata persist locally (e.g. local file/DB store) across app restarts; not synced to any cloud service. |
| AS-3 | Rectify is a fully local, single-user, no-auth desktop tool in v1; there is no account/login model. |
| AS-4 | v1 ships macOS-only; Windows and Linux are not supported. |
| AS-5 | iOS support (idb/simctl flows, including error screens for simulator-not-booted and device-not-trusted) ships in the v1.1+ milestone, not v1; Android is the v1 ground-truth platform. |
| AS-6 | [CONFIRMED via Stage 2 survey] The existing local 'rectify' MCP server and its get_latest_annotation-style tool are the sole mechanism by which the Claude Code agent retrieves sent payloads — no file-system or webhook fallback in v1. |
| AS-7 | [CONFIRMED via Stage 2 survey] The canonical 'Rectify Window' (3-tab Notes/Payload/History, bezel-less mirror) is the authoritative v1 window layout — it is the layout every one of the 16 catalog screens actually uses. 'Rectify Window v1' is a superseded earlier iteration and is out of scope. |

### constraints

| type | text |
| --- | --- |
| tech | Android mirroring/control depends on adb + scrcpy; iOS (v1.1+) depends on idb + simctl — both must be compatible with the user's installed Android SDK / Xcode toolchain. |
| tech | View-hierarchy capture for measurement and the hierarchy dump is bounded by what each platform's inspection API exposes (e.g. uiautomator-style dump for Android). |
| business | Claude Code must be running locally with the 'rectify' MCP server available for Resolve & Send to have a receiving agent; Rectify is not a standalone bug tracker. |
| time | v1 scope is Android-first per confirmed decision; iOS mirroring and iOS-specific error screens (14, 15) are deferred to v1.1+. |

### dependencies

| on | note |
| --- | --- |
| adb / Android Debug Bridge + scrcpy | Required for Android device discovery, mirroring, and view-hierarchy dumps in v1. |
| idb + simctl (v1.1+) | Required for iOS simulator/device mirroring and control, deferred to v1.1+. |
| Local Claude Code coding-agent session | Must be running and attached to the app repo under test to receive Resolve & Send payloads. |
| rectify MCP server (existing) | Exposes get_latest_annotation; presumed primary transport for delivering sent payloads to the Claude Code session, pending survey confirmation. |

### outOfScope

- Windows/Linux support
- Cloud sync, multi-user accounts, or any auth model
- iOS mirroring and iOS-specific error states (screens 14, 15) — deferred to v1.1+
- Simultaneous multi-device mirroring
- Automated bug fixing or code generation inside Rectify itself

### openQuestions

- Exact local storage format/engine for History persistence (e.g. SQLite vs. flat files) — left for design stage.
- Exact UX for mid-session device switching with staged annotations (silent clear vs. confirm dialog) — left as an implementation detail pending design review.
- Minimum supported macOS version.

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Which window layout is authoritative for v1? | ["Canonical 'Rectify Window': 3-tab side panel (Notes/Payload/History), bezel-less mirror", "Earlier 'Rectify Window v1': single Annotations+Payload panel, black-bezel device frame, no History tab"] | Canonical 'Rectify Window': 3-tab side panel (Notes/Payload/History), bezel-less mirror | All 16 catalog screens — including the explicitly in-scope History and Verify screens — use the 3-tab canonical layout; the single-panel v1 design has no History tab at all, contradicting the confirmed in-scope screen inventory. |
| What is the transport/protocol for delivering a Resolve & Send payload to the Claude Code agent? | ["Local MCP server only — the existing 'rectify' MCP tool (e.g. get_latest_annotation) is the sole transport", "MCP server plus a fallback mechanism (e.g. writing payload files to a known local path the agent can also read directly)"] | Local MCP server only — the existing 'rectify' MCP tool (e.g. get_latest_annotation) is the sole transport | An MCP server named 'rectify' already exists and exposes a retrieval tool; this is the simplest, most direct signal of intended integration and avoids building/maintaining a second transport without evidence it's needed. |
| What should the Verify action diff to decide if an annotation is resolved? | ["Pixel-region diff only, within the annotated box", "Hierarchy/layout diff only, for the matched view node", "Combination: pixel-region diff within the box plus hierarchy/layout diff of the matched node"] | Combination: pixel-region diff within the box plus hierarchy/layout diff of the matched node | Measure mode already captures and matches a hierarchy node per annotation, and many real UI bugs (misalignment, sizing) are structural rather than purely pixel-level; combining both diff types reduces false positives/negatives versus either alone. |
