# rectify — candidates

## Envelope

| field | value |
| --- | --- |
| stage | draft |
| step | candidates |
| status | done |
| producedBy | sdd-prd-draft-compose |
| schemaVersion | 1 |
| createdAt | 2026-10-11T00:00:00Z |
| updatedAt | 2026-10-11T12:34:51Z |

## Gate

### type

auto

### approved

true

### approvedBy

sdd-prd-draft-compose

## Provenance

### selfHash

c89691ed2141b59c4f6e584f8e145988cc36d34feff86704c16ca395fff4571e

### inputs

| artifact | hash |
| --- | --- |
| clarify.json | 5f67cd1a15072b7df0589a831081de9b5b759090135a91ef52734c1f40d5f54b |
| research.json | 48da83fe494629d4c20c424aedf84800fc22ede25689ed829e49f371434f394d |

## Data

### meta

#### title

Rectify — Live Device Annotation & Agent Handoff for Mobile UI Bugs

#### version

0.1.0

#### status

draft

### executiveSummary

Rectify is a local macOS tool that turns a live mirror of a connected Android device into an annotation surface: drag a box over a UI bug, hold Option to snap to the matched view-hierarchy node and capture exact dp/pixel gaps, then hit 'Resolve & Send' to deliver a structured payload (full frame, per-box crops, hierarchy XML, generated prompt) straight into a running Claude Code session through the existing local `rectify` MCP server. A History tab tracks every sent batch, and a closed-loop Verify re-captures the live device and runs a combined pixel-region + hierarchy/layout diff per annotation against the original frozen frame, marking resolved items or offering one-click re-send on mismatch. Competitive research (Oct 2026) found every individual piece already exists somewhere — Aki and Bugshot do point-and-annotate-to-Claude on desktop/browser, adb-mcp and mobile-mcp bridge Android state over MCP, Layout Inspector and Flipper do hierarchy measurement, Percy/Applitools do smart visual diffs, Jam.dev has an MCP server and a 2026 iOS client — but no product combines all five. The defensible wedge is therefore the *combination*: live native-device mirror + drag-to-box annotation + Option-key measure-vs-hierarchy + local MCP handoff + closed-loop per-annotation verify. v1 is deliberately Android-first (adb + scrcpy, macOS only, fully local, no auth) to ship the complete mirror → annotate → measure → send → verify loop before the most adjacent competitor (Jam.dev) extends to Android; iOS (idb + simctl) is scoped to v1.1+.

### problemStatement

Developers and QA testers who spot visual/UI bugs on a connected Android device (or iOS device in v1.1+) have no fast, structured way to hand precise, measurement-rich bug context (exact region, pixel/dp gaps, underlying view-hierarchy node) to a locally running Claude Code coding agent, and no built-in way to confirm a fix actually landed without manually re-testing the same spot by hand. Today that means screenshotting, describing position and size in prose, losing all measurement and hierarchy context (PROB-1), and — after the agent claims a fix — manually re-opening the app to eyeball the same region with no diff against the original bug frame (PROB-2). Bridging mirroring, annotation and agent handoff across Android (adb/scrcpy) and iOS (idb/simctl) currently requires manual device wrangling with no single tool (PROB-3), even though a local `rectify` MCP server exposing `get_latest_annotation` already anticipates the structured handoff — with no client producing that payload (PROB-4).

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

### requirements

#### functional

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

#### nonFunctional

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

| id | text | priority | verificationMethod |
| --- | --- | --- | --- |
| AC-1 | Given adb and idb are installed, when the app launches, then the toolbar shows a status dot reflecting detected tool versions for both platforms. | must | demo |
| AC-2 | Given a physical device is connected and an emulator is running, when the device picker is opened, then both appear in distinct Physical/Emulator sections and arrow keys plus Enter select an entry. | must | test |
| AC-3 | Given a device is selected in the picker, when the user clicks 'Start mirroring', then the live mirror begins streaming. | must | test |
| AC-4 | Given no device is connected, when adb polling runs, then the 'NO SIGNAL' state shows with guidance text and an inline emulator picker plus Boot button. | must | demo |
| AC-5 | Given live mirror is streaming, when the user drags on the mirror, then a dashed red outline, live pixel-size label, and ruler guides render in real time. | must | test |
| AC-6 | When the user releases the drag, then the exact frame is frozen and a new staged annotation is created anchored to the drawn box. | must | test |
| AC-7 | Given a frozen frame with one annotation, then everything outside the box dims and a popover with numbered badge, autofocused comment field, delete control, and meta line appears. | must | test |
| AC-8 | Given an annotation popover is focused, when the user presses Enter, then the comment saves; when the user clicks the delete control, then the annotation is removed and the staged count decrements. | must | test |
| AC-9 | Given three annotations are drawn close together, when their popovers would overlap, then they relocate to a sidebar/gutter panel connected via leader lines. | should | demo |
| AC-10 | Given a frozen frame with an annotation box, when the user holds Option over it, then the tool snaps to the matched view-hierarchy node and shows neighbor guide boxes with dp gap measurements in a 'Measuring' panel. | must | test |
| AC-11 | Given a measured element has misalignment (e.g. undersized hit target), then the offending dimension is highlighted in red and the note is prefixed with the Option-mode marker and carries the measurement data. | must | inspection |
| AC-12 | Given the canonical window layout is confirmed, then the side panel exposes exactly three tabs: Notes, Payload, History, over a bezel-less mirror. | must | inspection |
| AC-13 | Given at least one staged annotation exists, when the user opens the Payload tab, then a generated text prompt with token-count estimate and an Edit affordance is shown. | must | test |
| AC-14 | Given staged annotations exist, when viewing Payload > Crops, then a 3-column grid shows each annotation's cropped image with its red outline baked in and caption 'from frame <id>'. | must | test |
| AC-15 | Given staged annotations exist, when viewing Payload > Hierarchy, then a monospace indented tree displays with matched/flagged nodes visually marked. | must | test |
| AC-16 | Given the Payload tab is open, then its footer summarizes 'frame.png · N crops · hierarchy.xml' plus a total size that matches the actual payload. | must | inspection |
| AC-17 | Given zero annotations are staged, then 'Resolve & Send' is disabled; given one or more are staged, it is enabled; 'Clear' removes all staged annotations and resets the counter to 0. | must | test |
| AC-18 | When the user clicks 'Resolve & Send', then the frame screenshot, per-annotation crops, hierarchy XML, and generated prompt are transmitted via the local MCP server such that a Claude Code session calling the retrieval tool receives the complete payload. | must | test |
| AC-19 | When sending, then the status sequence Streaming -> Frozen -> Dumping hierarchy -> Sent displays in order with colored dots, and on completion a toast shows the sent-count/time summary with an 'Open session' link. | must | demo |
| AC-20 | While a send is in progress, 'Resolve & Send' is disabled and only re-enables after the toast confirms or the send fails. | must | test |
| AC-21 | After a successful send, then live mirroring resumes automatically and the staged-annotation counter resets to 0. | must | test |
| AC-22 | Given batches have been sent previously, when the History tab is opened, then batches are grouped by time period, each showing thumbnail, summary, timestamp, and status chip. | must | test |
| AC-23 | Given a batch has status 'Fixed', when the user clicks 'Verify', then the app re-captures the live screen and diffs it, per annotation box, against the stored frozen frame using a pixel-region diff plus a hierarchy/layout diff of the matched node. | must | test |
| AC-24 | Given a Verify diff finds no material difference for an annotation, then that annotation is marked resolved; given a difference remains, then a one-click follow-up re-stages only that annotation for re-send. | must | test |
| AC-25 | Given live mirror is active, then the status bar displays current tool version, resolution, fps, and latency, updating at least once per second. | should | demo |
| AC-26 | Given adb is not found on PATH, then an error card renders with the install command (with Copy), primary 'Locate adb…' and secondary 'Retry' actions. | must | test |
| AC-27 | Given no Android device is detected after launch, then an error card renders with the kill-server command and primary 'Restart adb' / secondary 'Boot emulator instead' actions. | must | test |
| AC-28 | Given 3 devices are connected simultaneously, then the error card shows an inline device list (not a modal) and the primary action names the top device (e.g. 'Mirror Pixel 8 Pro'). | must | test |
| AC-29 | Given the stream drops mid-session with staged annotations present, then a non-blocking banner appears stating the stream dropped and that annotations are kept on the frozen frame, with a 'Reconnect' primary action; 'Resolve & Send' remains clickable and annotations remain pinned throughout. | must | test |
| AC-30 | Given the user toggles light/dark theme, then all screens render correctly in both themes with consistent signal colors (red/green/orange/blue) and no unreadable contrast regions. | must | inspection |
| AC-31 | (v1.1+) Given iOS is selected as platform, when the simulator isn't booted, then an error card with 'Boot simulator' primary / 'Choose another' secondary renders; when the device isn't trusted, an error card with "I've tapped Trust — retry" primary / 'Use simulator' secondary renders. | could | test |
| AC-32 | Given a staged annotation batch is sent, then the persisted payload envelope carries an explicit format/schema version field that the receiving MCP server can read to select its parsing path. | should | inspection |
| AC-33 | Given History contains at least one batch, when the app is quit and relaunched, then all batches, their status chips, stored frozen frames, and per-annotation records are still present and openable. | must | test |

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
| SC-15 | History survives an app restart | edge | ["Send two batches", "Quit Rectify", "Relaunch Rectify", "Open History tab", "Both batches, statuses and stored frames are intact"] |

### userStories

| id | asA | iWant | soThat | linkedFR | acceptanceCriteria | scenarios | priority |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-1 | Mobile Developer (PER-1) | Rectify to detect my adb/idb install and list my connected devices and emulators on launch | I can start mirroring the device under test without touching a terminal | ["FR-1", "FR-2", "FR-3", "FR-4"] | ["AC-1", "AC-2", "AC-3"] | ["SC-1"] | must |
| US-2 | QA Tester (PER-2) | a clear NO SIGNAL state with inline guidance and an emulator Boot button when nothing is attached | I can get to a working mirror myself without asking a developer for help | ["FR-5"] | ["AC-4"] | ["SC-2"] | must |
| US-3 | Mobile Developer (PER-1) | to drag a box directly on the live mirror and have the exact frame freeze on release | the bug is captured at the precise moment and region I saw it | ["FR-6", "FR-7"] | ["AC-5", "AC-6"] | ["SC-3"] | must |
| US-4 | QA Tester (PER-2) | an autofocused popover per box where I can type what's wrong, press Enter, or delete the box | I can flag several bugs in one pass without breaking flow | ["FR-8", "FR-9"] | ["AC-7", "AC-8", "AC-9"] | ["SC-3", "SC-4"] | must |
| US-5 | Mobile Developer (PER-1) | to hold Option over a box and get the matched hierarchy node plus dp/pixel gaps attached to the note | the agent receives exact measurements instead of my prose estimate of the spacing | ["FR-10"] | ["AC-10", "AC-11"] | ["SC-5"] | must |
| US-6 | Mobile Developer (PER-1) | to review the generated prompt, crops, hierarchy tree and payload size before sending | I can trust and tune what the agent will actually receive | ["FR-12", "FR-13", "FR-14", "FR-15"] | ["AC-13", "AC-14", "AC-15", "AC-16"] | ["SC-6"] | must |
| US-7 | Mobile Developer (PER-1) | one 'Resolve & Send' action that delivers the whole batch to my running Claude Code session over the local rectify MCP server | the agent can start fixing with full context and zero copy-paste | ["FR-16", "FR-17", "FR-18", "FR-19"] | ["AC-17", "AC-18", "AC-19", "AC-20", "AC-21", "AC-32"] | ["SC-7"] | must |
| US-8 | QA Tester (PER-2) | a History tab listing every batch I've sent grouped by time with status chips | I can see what's fixed or in progress without re-testing everything | ["FR-20"] | ["AC-22", "AC-33"] | ["SC-8", "SC-15"] | must |
| US-9 | Mobile Developer (PER-1) | a Verify action on Fixed batches that diffs the live screen against the original frozen frame per annotation | I can confirm the specific reported region actually changed instead of eyeballing it | ["FR-21"] | ["AC-23"] | ["SC-8"] | must |
| US-10 | QA Tester (PER-2) | a one-click follow-up that re-stages only the annotation whose Verify still fails | I can push a tight correction back to the agent without redoing the whole report | ["FR-21"] | ["AC-24"] | ["SC-9"] | must |
| US-11 | QA Tester (PER-2) | actionable error cards with copyable commands when adb is missing, no device is found, or several devices are attached | I can fix my own setup rather than filing a tooling ticket | ["FR-23"] | ["AC-26", "AC-27", "AC-28"] | ["SC-10", "SC-11", "SC-12"] | must |
| US-12 | Mobile Developer (PER-1) | my staged annotations to survive a mid-session stream drop and still be sendable | a flaky USB cable never costs me the bug report I just wrote | ["FR-24"] | ["AC-29"] | ["SC-13"] | must |
| US-13 | Mobile Developer (PER-1) | the canonical 3-tab window with live stream stats, readable in light and dark themes | I can work in my preferred appearance and always see stream health at a glance | ["FR-11", "FR-22"] | ["AC-12", "AC-25", "AC-30"] | ["SC-1"] | must |
| US-14 | Mobile Developer (PER-1) | (v1.1+) the same mirror/annotate/send flow against an iOS simulator or device | I can use one tool across both platforms my app ships on | ["FR-25"] | ["AC-31"] | ["SC-14"] | could |

### uxFlows

| name | steps | edgeCases |
| --- | --- | --- |
| First run: connect a device and start mirroring | ["Launch Rectify; toolbar shows per-platform tool status dots from adb/idb detection", "Platform switch defaults to Android", "Open the device picker; Physical and Emulator sections populate from adb", "Pick a device by click or arrow keys + Enter (shut-down emulators offer an inline Boot)", "Click 'Start mirroring'; bezel-less live mirror fills the window and the status bar starts reporting tool version / resolution / fps / latency"] | ["Nothing attached: NO SIGNAL placeholder with 2s adb polling, guidance text, inline emulator picker + Boot", "adb present but version unknown: status dot renders in a warning state rather than blocking launch", "Emulator boots mid-poll: picker refreshes it from shut-down to running without reopening"] |
| Annotate and measure a bug | ["Drag on the live mirror: dashed red outline, live device-pixel size label and ruler guides follow the cursor", "Release: the exact frame freezes, everything outside the box dims, a numbered annotation is staged", "Autofocused popover appears ('What's wrong here?'); type and press Enter to save, or use the delete control to drop it", "Draw additional boxes on the same frozen frame; the staged counter increments", "Hold Option over a box: view snaps to the matched hierarchy node, neighbor guide boxes and dp/pixel gaps render in the floating 'Measuring' panel", "Release Option: the measurement block is appended to that annotation's note, prefixed with the Option marker"] | ["Three boxes drawn close together: popovers relocate to the gutter panel with leader lines back to their boxes", "No hierarchy node matches the drawn box: Measure panel reports no match and the annotation keeps its plain comment", "Zero-area / accidental micro-drag: treated as a click, no annotation staged", "Escape in a popover dismisses without saving the in-progress text"] |
| Review the payload and send to Claude Code | ["Open the Payload tab: Prompt section shows the generated text, token estimate and an Edit affordance", "Crops section shows the 3-column grid of per-annotation crops with the red outline baked in, each captioned 'from frame <id>'", "Hierarchy section shows the monospace indented tree with matched/flagged nodes marked", "Footer confirms 'frame.png · N crops · hierarchy.xml' plus total size", "Click 'Resolve & Send' in the toolbar; status sequence steps Streaming -> Frozen -> Dumping hierarchy -> Sent", "Toast confirms 'Sent N annotations to Claude Code · frame + hierarchy · <time>s' with an 'Open session' link; mirroring resumes and the staged counter resets to 0"] | ["Zero annotations staged: 'Resolve & Send' is disabled", "Send in flight: the button stays disabled until the toast confirms or the send fails", "MCP server unreachable: send fails, annotations stay staged, the button re-enables for a retry", "'Clear' pressed: all staged annotations drop and the counter resets to 0"] |
| History review and closed-loop verify | ["Open the History tab: batches grouped by time period, each row with thumbnail crop, summary, timestamp and status chip", "Select a batch with a 'Fixed' chip and click 'Verify'", "Rectify re-captures the live screen and runs, per annotation box, a pixel-region diff plus a hierarchy/layout diff of the matched node", "Annotations with no material difference are marked resolved (green)", "Annotations still differing surface a one-click follow-up that re-stages just that annotation", "Re-send the single re-staged annotation via 'Resolve & Send'"] | ["App under test is on a different screen than the stored frame: Verify reports it cannot locate the region rather than falsely resolving", "No device currently mirroring: Verify prompts to start mirroring first", "Benign churn in the region (animation, changed copy) risks a false mismatch — diff thresholds are tuned and the result stays reviewable", "History after relaunch: all batches, chips and stored frames load from local persistence"] |
| Error and recovery states | ["adb missing from PATH: error card with icon, title, body, copyable install command, primary 'Locate adb…', secondary 'Retry', footer diagnostic line", "adb present but no device: error card with copyable kill-server command, primary 'Restart adb', secondary 'Boot emulator instead'", "Multiple devices attached: error card with an inline device list (no modal); primary action names the top device (e.g. 'Mirror Pixel 8 Pro')", "Stream drops mid-session: non-blocking floating banner over the stale mirror ('Stream dropped' / 'Your N annotations are kept on the frozen frame' / 'Reconnect')", "Reconnect restores streaming with staged annotations still pinned; 'Resolve & Send' remains usable while disconnected"] | ["Device unplugged and replugged on a different port: reconnect re-resolves the serial rather than erroring out", "Repeated reconnect failures: banner persists without blocking annotation or send", "Error-card actions are reachable by keyboard alone", "Every error card renders correctly in both light and dark themes"] |

### scope

#### in

- All 16 catalog screens of the canonical 'Rectify Window' layout (bezel-less mirror + 3-tab Notes/Payload/History side panel)
- Android v1: adb/scrcpy device discovery, emulator boot, live mirroring, status bar stream stats
- Drag-to-box annotation with frame freeze, per-annotation popover CRUD, crowded-popover gutter relocation
- Option-key Measure mode with view-hierarchy node matching, dp/pixel gaps and misalignment highlighting
- Payload tab: generated prompt + token estimate + Edit, crops grid, hierarchy tree, footer summary
- Resolve & Send over the local 'rectify' MCP server with status sequence and confirmation toast
- History tab with grouped batches, thumbnails, timestamps and status chips, persisted locally
- Closed-loop Verify: per-annotation pixel-region diff plus hierarchy/layout diff, with one-click follow-up re-send
- Structured error cards for adb-not-found, no-device, multi-device, plus the mid-session stream-drop banner
- Full light/dark theme parity across every screen using the signal-color system
- macOS-only, fully local, single-user, no-auth operation

#### out

- Windows/Linux support
- Cloud sync, multi-user accounts, or any auth model
- iOS mirroring and iOS-specific error states (screens 14, 15) — deferred to v1.1+
- Simultaneous multi-device mirroring
- Automated bug fixing or code generation inside Rectify itself
- Any non-MCP transport (file drop, webhook) for delivering payloads in v1
- The superseded 'Rectify Window v1' single-panel / black-bezel layout
- PM-tool integrations (Jira, Linear, GitHub issues) and external stakeholder portals

### assumptions

- AS-1: Only one device is mirrored at a time per session; switching devices clears or prompts to clear staged annotations.
- AS-2: History and batch metadata persist locally (e.g. local file/DB store) across app restarts; not synced to any cloud service.
- AS-3: Rectify is a fully local, single-user, no-auth desktop tool in v1; there is no account/login model.
- AS-4: v1 ships macOS-only; Windows and Linux are not supported.
- AS-5: iOS support (idb/simctl flows, including error screens for simulator-not-booted and device-not-trusted) ships in the v1.1+ milestone, not v1; Android is the v1 ground-truth platform.
- AS-6: [CONFIRMED via Stage 2 survey] The existing local 'rectify' MCP server and its get_latest_annotation-style tool are the sole mechanism by which the Claude Code agent retrieves sent payloads — no file-system or webhook fallback in v1.
- AS-7: [CONFIRMED via Stage 2 survey] The canonical 'Rectify Window' (3-tab Notes/Payload/History, bezel-less mirror) is the authoritative v1 window layout — it is the layout every one of the 16 catalog screens actually uses. 'Rectify Window v1' is a superseded earlier iteration and is out of scope.

### dependencies

- adb / Android Debug Bridge + scrcpy — required for Android device discovery, mirroring, and view-hierarchy dumps in v1.
- idb + simctl (v1.1+) — required for iOS simulator/device mirroring and control, deferred to v1.1+.
- A local Claude Code coding-agent session, running and attached to the app repo under test, to receive Resolve & Send payloads.
- The existing 'rectify' MCP server exposing get_latest_annotation — confirmed sole transport for delivering sent payloads.
- Platform view-hierarchy inspection APIs (uiautomator-style dump on Android) bound what Measure mode and the hierarchy dump can expose.
- macOS host with a compatible Android SDK (and, for v1.1+, Xcode) toolchain installed.

### risks

| text | likelihood | impact | mitigation |
| --- | --- | --- | --- |
| Jam.dev — already shipping an MCP server that pipes session context to Claude Code/Cursor and a native iOS client (2026), with Android explicitly demanded by users — adds Android plus live-mirror annotation and undercuts Rectify's wedge. | medium | high | Ship the Android mirror -> annotate -> send -> Verify loop end to end in PH-1 before broadening platforms; lean on the pieces Jam lacks (live mirror as annotation surface, Option measure-vs-hierarchy, per-annotation pixel+hierarchy diff) rather than recording-based capture. |
| The 'point at a UI bug, send to Claude Code' pattern is being independently reinvented by small open-source tools (Bugshot, UI Annotator, Aki), signalling a low barrier to entry; any could add device mirroring and a measurement layer and converge on Rectify's feature set. | medium | medium | Compete on the combination and on depth of the hardest parts (hierarchy-matched measurement and the closed-loop per-annotation diff), which are far more work than an annotation overlay; keep the payload schema versioned (NFR-12) so the format can evolve faster than clones. |
| Anthropic expands Claude Code/Desktop native visual tooling (in-app browser, screenshot paste, element annotation, Computer Use) and absorbs Rectify's value at the platform layer. | low | high | Watch-list item only, no scope change. Rectify integrates through MCP rather than replacing the agent, so first-party visual tooling is more likely to improve the receiving side than to replace device-side mirroring + measurement. |
| The Android/iOS MCP-bridge ecosystem (adb-mcp, mobile-mcp, ios-simulator-mcp, maestro-mcp) matures and one of them adds a human-annotation UI, collapsing much of the differentiation. | medium | medium | Treat MCP tool naming and payload schema as intentional design (research implication 3); keep Rectify's value in the human-facing annotation/measure/verify UX that agent-automation servers are not built for, and consider interoperating with those servers instead of duplicating them. |
| scrcpy/adb fragility across macOS, Android and device-vendor versions makes mirroring or hierarchy dumps fail on real user setups, jeopardising the >=95% first-attempt connect target (MET-4). | high | high | Invest in F-3 error handling as a first-class feature (structured error cards with copyable commands, inline multi-device picker, reconnect banner), pin and version-check tool detection at launch (FR-1), and test across a matrix of physical devices plus emulator API levels. |
| Verify produces false positives (marks resolved while the bug persists) or false negatives (flags benign animation/copy churn), breaking trust in the closed loop and the <5% false-positive target (MET-3). | high | high | Use the confirmed combined diff (pixel region + hierarchy/layout of the matched node) rather than pixels alone, borrow noise-suppression patterns from Percy/Applitools prior art, expose tunable thresholds, and always leave the Verify result human-reviewable with the one-click follow-up re-send. |
| Measure-mode node matching depends on what uiautomator-style dumps expose; Compose, WebViews, custom canvases and overlay surfaces may yield no usable node for a drawn box. | medium | medium | Degrade gracefully — annotation still sends with crop + comment when no node matches; surface 'no node matched' explicitly rather than guessing, and validate against Compose and WebView-heavy apps during PH-1. |
| Resolve & Send is useless if no Claude Code session with the 'rectify' MCP server is running, and AS-6 rules out any fallback transport in v1. | medium | medium | Detect MCP availability before enabling send, fail with a clear actionable error that keeps annotations staged, and revisit a fallback transport only if real usage shows the single-transport constraint blocking users. |
| Competitive landscape is a fast-moving October 2026 snapshot; several closest competitors emerged within the current year, so the positioning could stale quickly. | high | low | Re-scan the competitive section at the next major release milestone (per research implication 5) instead of treating it as static. |

### features

| id | slug | title | description | priority | linkedGoals | linkedFR | linkedNFR | linkedAC | linkedSC | linkedStories | dependsOn | phase | estimate | sddReady |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| F-1 | app-shell-window-layout | App Shell & Canonical Window Layout | The foundational macOS app shell implementing the canonical 'Rectify Window': a bezel-less live-mirror region with a side panel exposing exactly three tabs (Notes, Payload, History), plus the toolbar frame and status-bar chrome that every other feature plugs into. Includes the full light/dark theme system with the signal-color palette (red = bug, green = fixed, orange = warning, blue = measurement) and contrast parity in both themes. Why first: all 16 catalog screens import this layout, and every subsequent feature renders inside it, so the shell and theming must exist before any device, annotation or payload work can land. | must | ["GOAL-5"] | ["FR-11"] | ["NFR-9", "NFR-10"] | ["AC-12", "AC-30"] | [] | ["US-13"] | [] | PH-1 | M | true |
| F-2 | android-device-discovery-mirroring | Android Device Discovery & Live Mirroring | Everything needed to get a live Android screen into the window: launch-time adb/idb tool detection with per-platform toolbar status dots, the Android/iOS platform switch (Android functional in v1), the device picker with Physical and Emulator sections including inline Boot for shut-down emulators and arrow-key/Enter navigation, the primary 'Start mirroring' action over adb + scrcpy, the 2s-polling 'NO SIGNAL' empty state with inline emulator boot, and the status bar reporting tool version, resolution, fps and latency. Why: the live mirror is the annotation surface and the whole product's entry point; low latency here (NFR-1) is what makes drag-and-freeze feel responsive. | must | ["GOAL-1", "GOAL-5"] | ["FR-1", "FR-2", "FR-3", "FR-4", "FR-5", "FR-22"] | ["NFR-1", "NFR-5", "NFR-8", "NFR-10"] | ["AC-1", "AC-2", "AC-3", "AC-4", "AC-25"] | ["SC-1", "SC-2"] | ["US-1", "US-2", "US-13"] | ["F-1"] | PH-1 | L | true |
| F-3 | error-handling-and-recovery | Error Handling & Stream Recovery | The structured error-state system: a reusable error-card component (icon-in-circle, title, body, optional copyable command block, primary + secondary actions, footer diagnostic line) instantiated for adb-not-in-PATH ('Locate adb…' / 'Retry'), no-device-detected (kill-server command, 'Restart adb' / 'Boot emulator instead'), and multiple-devices-connected (inline device list rather than a modal, primary action naming the top device); plus the mid-session stream-drop banner that floats non-blockingly over the stale mirror, keeps staged annotations pinned, offers 'Reconnect', and leaves 'Resolve & Send' usable while disconnected. Why: research flags adb/scrcpy fragility across OS and device versions as the biggest execution risk to the >=95% first-attempt connect target, so recoverable failure is a product feature, not polish. | must | ["GOAL-1", "GOAL-5"] | ["FR-23", "FR-24"] | ["NFR-6", "NFR-8", "NFR-9"] | ["AC-26", "AC-27", "AC-28", "AC-29"] | ["SC-10", "SC-11", "SC-12", "SC-13"] | ["US-11", "US-12"] | ["F-1", "F-2"] | PH-1 | M | true |
| F-4 | drag-annotate-frozen-frame | Drag-to-Annotate & Frozen Frame | The core capture interaction: click-and-drag on the live mirror renders a dashed red outline with a live device-pixel size label and ruler/tick guides; release freezes the exact current frame and stages a numbered annotation anchored to the box. On the frozen frame everything outside annotation boxes dims and each annotation gets a popover (numbered badge, autofocused 'What's wrong here?' comment field, delete control, size/position meta line) with Enter to save and delete to remove and decrement the staged count. When popovers would crowd or overlap, they relocate to a gutter/sidebar panel linked to their boxes by leader lines. Why: this is the interaction that replaces 'screenshot plus prose description' and is the single most differentiating surface of the product. | must | ["GOAL-1", "GOAL-3"] | ["FR-6", "FR-7", "FR-8", "FR-9"] | ["NFR-1", "NFR-8", "NFR-9"] | ["AC-5", "AC-6", "AC-7", "AC-8", "AC-9"] | ["SC-3", "SC-4"] | ["US-3", "US-4"] | ["F-1", "F-2"] | PH-1 | L | true |
| F-5 | measure-mode | Option-Key Measure Mode | Holding Option over an annotation box enters Measure mode: Rectify snaps to the matched underlying view-hierarchy node, draws dashed guide boxes for neighboring views, and shows dp/pixel gap measurements in a floating 'Measuring' panel, highlighting misaligned or undersized dimensions in red. On release, the measurement data is appended to that annotation's note prefixed with the Option marker, so the sent payload carries exact numbers instead of estimates. Must resolve node lookup in under ~100ms to feel instantaneous, and must degrade clearly when no node matches. Why: no bug-report-to-agent tool found in research has hierarchy-snapped measurement; the closest analogs (Layout Inspector, Reveal, Flipper) are standalone inspectors with no handoff path. | must | ["GOAL-1", "GOAL-3"] | ["FR-10"] | ["NFR-2", "NFR-9"] | ["AC-10", "AC-11"] | ["SC-5"] | ["US-5"] | ["F-4"] | PH-1 | L | true |
| F-6 | payload-preview | Payload Preview (Prompt / Crops / Hierarchy) | The Payload tab that lets the reporter see exactly what the agent will receive before sending: a Prompt section with the auto-generated text, its token-count estimate (computed in under ~200ms) and an Edit affordance; a Crops section rendering a 3-column grid of per-annotation cropped images with the red outline baked in, each captioned 'from frame <id>'; a Hierarchy section showing a monospace indented view-hierarchy tree with matched/flagged nodes visually marked; and a footer summarizing 'frame.png · N crops · hierarchy.xml' plus a total size that matches the real payload. Why: trust in the one-shot handoff depends on the reporter being able to inspect and tune the bundle, and this is where the payload assembly (and its versioned envelope) is actually built. | must | ["GOAL-3"] | ["FR-12", "FR-13", "FR-14", "FR-15"] | ["NFR-11", "NFR-12"] | ["AC-13", "AC-14", "AC-15", "AC-16"] | ["SC-6"] | ["US-6"] | ["F-4", "F-5"] | PH-1 | M | true |
| F-7 | resolve-and-send-mcp | Resolve & Send via Local MCP | The handoff itself: toolbar staged-annotation counter, 'Clear' action, and the primary 'Resolve & Send' button (disabled at zero staged annotations and while a send is in flight). On send, Rectify packages the full frame screenshot, per-annotation crops, view-hierarchy XML dump and generated prompt into a versioned payload envelope and transmits it to the locally running Claude Code agent through the existing local 'rectify' MCP server for retrieval via a get_latest_annotation-style tool — the sole transport in v1, no file or webhook fallback. During send a status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) animates with colored dots, then a toast confirms 'Sent N annotations… · <time>s' with an 'Open session' link; afterwards live mirroring resumes automatically and the counter resets to 0. All data stays local; typical localhost sends complete in under ~2s. Why: this is GOAL-4 and the point where everything captured becomes actionable for the agent; research notes MCP tool naming and payload schema should be deliberate design, since Aki's near-identical tool surface already exists for desktop. | must | ["GOAL-3", "GOAL-4"] | ["FR-16", "FR-17", "FR-18", "FR-19"] | ["NFR-3", "NFR-4", "NFR-12"] | ["AC-17", "AC-18", "AC-19", "AC-20", "AC-21", "AC-32"] | ["SC-7"] | ["US-7"] | ["F-6"] | PH-1 | L | true |
| F-8 | history-batch-tracking | History & Batch Tracking | The History tab plus the local persistence layer behind it: every batch ever sent is recorded with its frozen frame, per-annotation records, generated prompt and hierarchy dump, and listed grouped by time period with a thumbnail crop, title/summary, timestamp and status chip (Fixed, In progress, etc.). All records survive app restarts and never leave the machine. Why: PER-2 needs to see what has already been reported and its state without re-testing everything, and the Verify loop needs a durable original-frame record to diff against. | must | ["GOAL-2", "GOAL-3"] | ["FR-20"] | ["NFR-4", "NFR-7"] | ["AC-22", "AC-33"] | ["SC-8", "SC-15"] | ["US-8"] | ["F-7"] | PH-1 | M | true |
| F-9 | verify-and-rediff-loop | Verify & Re-diff Loop | The closed loop. On a batch with status 'Fixed', a 'Verify' action re-captures the live screen and diffs it per annotation box against the stored frozen frame using the confirmed combination of a pixel-region diff within the box plus a hierarchy/layout diff of the matched node. Annotations with no material difference are marked resolved; annotations that still differ surface a one-click follow-up that re-stages only that annotation for re-send through F-7. Diff thresholds must be tuned to hold the <5% false-positive target (MET-3) while surfacing real misses, and results stay human-reviewable. Why: no competitor found does per-annotation, per-region pixel+hierarchy verification against an original annotated frame — adb-mcp's verify_change only detects whether the UI changed at all, and AppContext's re-look is unstructured — so this is the strongest single differentiator and the answer to PROB-2. | must | ["GOAL-2"] | ["FR-21"] | ["NFR-7", "NFR-9"] | ["AC-23", "AC-24"] | ["SC-8", "SC-9"] | ["US-9", "US-10"] | ["F-5", "F-8"] | PH-1 | XL | true |
| F-10 | ios-platform-support | iOS Platform Support (v1.1+) | Extends the proven Android flow to iOS by implementing the platform switch's iOS branch over idb + simctl: simulator and physical-device discovery, live mirroring, screenshot and view-hierarchy capture, and the two iOS-specific error cards — simulator-not-booted ('Boot simulator' / 'Choose another') and device-not-trusted ("I've tapped Trust — retry" / 'Use simulator'). Reuses F-1's shell, F-4/F-5's annotation and measure surfaces, F-7's MCP handoff and F-9's Verify engine behind a platform abstraction, so only the device-adapter layer and the iOS error states are new. Why: deliberately deferred out of v1 (AS-5) so the complete Android loop ships first; it is the obvious parity follow-up once that loop is validated. | could | ["GOAL-5"] | ["FR-25"] | ["NFR-5", "NFR-8", "NFR-9", "NFR-10"] | ["AC-31"] | ["SC-14"] | ["US-14"] | ["F-2", "F-3", "F-9"] | PH-2 | L | true |

### roadmap

#### phases

| id | title | goal | features | exitCriteria |
| --- | --- | --- | --- | --- |
| PH-1 | v1 — Android MVP | Ship the complete Android-only loop — mirror -> annotate -> measure -> send -> verify — as a fully local macOS app, closing the competitive window before any adjacent tool (notably Jam.dev) adds Android live-mirror annotation. | ["F-1", "F-2", "F-3", "F-4", "F-5", "F-6", "F-7", "F-8", "F-9"] | ["Canonical 3-tab window and light/dark parity verified (AC-12, AC-30)", "Tool detection, device picker, Start mirroring and NO SIGNAL state all pass (AC-1, AC-2, AC-3, AC-4), with status-bar stream stats live (AC-25)", "Drag-to-box, frame freeze, popover CRUD and gutter relocation all pass (AC-5 through AC-9)", "Option Measure mode snaps to the matched hierarchy node and attaches measurement data with red misalignment highlighting (AC-10, AC-11)", "Payload tab prompt/token estimate, crops grid, hierarchy tree and footer totals all pass (AC-13 through AC-16)", "Resolve & Send delivers the complete versioned payload over the local 'rectify' MCP server with correct enable/disable, status sequence, toast, auto-resume and counter reset (AC-17 through AC-21, AC-32)", "History lists grouped batches with chips and survives relaunch (AC-22, AC-33)", "Verify runs the combined pixel-region + hierarchy diff per annotation and the one-click follow-up re-stages a single annotation (AC-23, AC-24)", "All three error cards and the stream-drop banner behave as specified with no annotation loss (AC-26 through AC-29)", "MET-1 median send under 30s and MET-4 first-attempt connect >=95% measured on the supported device/emulator matrix"] |
| PH-2 | v1.1 — iOS parity | Bring the validated Android loop to iOS simulators and physical devices via idb + simctl behind a platform abstraction, with the iOS-specific error states, without regressing any Android behaviour. | ["F-10"] | ["iOS platform switch mirrors a booted simulator and a trusted physical device, with annotation, measure, send and verify all functioning on iOS", "Simulator-not-booted and device-not-trusted error cards render with the specified primary/secondary actions (AC-31)", "Full PH-1 Android acceptance suite still passes after the platform abstraction lands (no regression on AC-1 through AC-30, AC-32, AC-33)", "MET-4 first-attempt connect >=95% also holds for iOS simulator and trusted-device setups"] |

### traceabilityMatrix

| goal | fr | stories | features |
| --- | --- | --- | --- |
| GOAL-1 | ["FR-1", "FR-2", "FR-3", "FR-4", "FR-5", "FR-6", "FR-7", "FR-8", "FR-9", "FR-10", "FR-22", "FR-23", "FR-24"] | ["US-1", "US-2", "US-3", "US-4", "US-5", "US-11", "US-12"] | ["F-2", "F-3", "F-4", "F-5"] |
| GOAL-2 | ["FR-20", "FR-21"] | ["US-8", "US-9", "US-10"] | ["F-8", "F-9"] |
| GOAL-3 | ["FR-6", "FR-7", "FR-8", "FR-10", "FR-12", "FR-13", "FR-14", "FR-15", "FR-16", "FR-17", "FR-18", "FR-19", "FR-20"] | ["US-4", "US-5", "US-6", "US-7", "US-8"] | ["F-4", "F-5", "F-6", "F-7", "F-8"] |
| GOAL-4 | ["FR-16", "FR-17", "FR-18", "FR-19"] | ["US-7"] | ["F-6", "F-7"] |
| GOAL-5 | ["FR-1", "FR-2", "FR-5", "FR-11", "FR-23", "FR-25"] | ["US-1", "US-2", "US-11", "US-13", "US-14"] | ["F-1", "F-2", "F-3", "F-10"] |

### openQuestions

- Exact local storage format/engine for History persistence (e.g. SQLite vs. flat files) — left for design stage (affects F-8).
- Exact UX for mid-session device switching with staged annotations (silent clear vs. confirm dialog) — implementation detail pending design review (AS-1, affects F-2/F-4).
- Minimum supported macOS version.
- Concrete diff thresholds and noise-suppression strategy for Verify (what counts as 'no material difference' per region and per hierarchy node) — needs calibration against real device data in F-9 to hold MET-3 < 5%.
- Exact MCP tool name and payload envelope schema version 1 shape for the 'rectify' server, given Aki's near-identical tool surface — treat as intentional design in F-7.
- Whether 'In progress' -> 'Fixed' batch status is set manually by the user, inferred from the agent session, or both (affects which batches expose Verify in F-8/F-9).

### readyForReview

true

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Should the two 'should'-priority Android items — F-4's crowded-popover gutter relocation (FR-9/AC-9) and F-2's live stream-stats status bar (FR-22/AC-25) — stay inside the v1 PH-1 cut, or ship as fast-follows after the loop is validated? | ["Both stay in PH-1 (full catalog fidelity at v1)", "Both become fast-follows after the PH-1 loop ships", "Keep the status bar in PH-1, defer gutter relocation to a fast-follow"] | Both stay in PH-1 (full catalog fidelity at v1) | Both appear in the confirmed 16-screen catalog and are small relative to F-9's diff engine; deferring them saves little schedule while visibly degrading the screens users first see. Worth confirming only because the competitive window on the Android loop is the time-sensitive constraint per research. |
| How is a batch's 'Fixed' status — the gate that exposes the Verify action — established? | ["User sets it manually in History", "Inferred automatically from the Claude Code session reporting completion over MCP", "Verify is available on every batch regardless of status"] | Verify is available on every batch regardless of status | FR-21 scopes Verify to 'Fixed' batches, but nothing in v1 is confirmed to produce that status — manual marking adds a step that undercuts MET-1/MET-2, and MCP-driven inference is unspecified upstream work. Allowing Verify anywhere is the smaller build and still satisfies AC-23/AC-24; this needs a decision before F-8/F-9 are specced. |
