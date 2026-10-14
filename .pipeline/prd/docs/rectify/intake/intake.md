# rectify — intake

## Envelope

| field | value |
| --- | --- |
| stage | intake |
| step | intake |
| status | done |
| producedBy | sdd-prd-intake-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-11T00:00:00Z |
| updatedAt | 2026-10-11T11:51:01Z |

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

a01058040380b87160b6a1cb991828cb5f0c75cf375786c37a0f290032666c57

## Data

### title

Rectify: Live-Device UI Bug Annotation for Claude Code

### ideaStatement

Rectify is a native macOS desktop app that live-mirrors a connected Android device or iOS simulator/device, allowing developers and QA testers to point at and annotate UI bugs by drawing bounding boxes, optionally measuring misalignment, and sending structured, measurement-rich bug reports directly to a locally running Claude Code agent session. The closed loop — send → agent fixes → re-verify by diffing the live screen — confirms fixes and enables quick follow-ups.

### rawDescription

Rectify is a macOS tool that sits between a developer/QA tester and a mobile app under test. It live-mirrors a connected device (Android via adb+scrcpy or iOS via idb+simctl) in its own window, frozen at user discretion. When a visual bug is spotted, the user drags to draw a bounding box around it; releasing freezes that exact frame. They add a free-text comment per box ("What's wrong here?"). Optionally, holding Option over a box enters measurement mode: the tool snaps to the underlying view hierarchy, shows pixel/dp gaps to neighbors via ruler guides, and highlights misalignment in red, all attached to the annotation. When ready, 'Resolve & Send' packages the full frame screenshot, per-annotation crops, a view-hierarchy XML dump, and an auto-generated prompt (with token-count estimate) and sends it to the locally running Claude Code coding agent operating on the actual app repo. Sending walks a visible status sequence and confirms with a toast. A History tab tracks every batch sent, grouped by time, with per-batch status (Fixed, In progress, etc.) and a Verify action that re-captures the live screen and diffs it against the original frozen frame per annotation — if a fix is confirmed, it's marked resolved; if not, a one-click follow-up re-sends. The toolbar offers: a platform switch (Android/iOS), device indicator with status and a device picker dropdown, a staged annotation counter, a Clear action, and the primary Resolve & Send button (disabled while sending). A status bar shows stream stats (tool version, resolution, fps, latency). A design-system supports full light/dark themes and uses signal colors (red for bug boxes, green for fixed, orange for warnings, blue for measurements). All 16 screens in the design catalog span from first-run setup through error states, mirrored live, frozen with annotations, measurement/payload preview, send confirmation, history, and verification — representing the complete feature and state inventory for the v1 product.

### sources

| type | ref | cached | fetchedAt | hash | summary |
| --- | --- | --- | --- | --- | --- |
| url | https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html | true | 2026-10-11T00:00:00Z | see raw/index.json#rectify-design-project | Claude Design project 'Rectify: Bug annotation tool' — 16-screen design catalog (light + dark theme for each) showing: first-run platform/device selection, live mirroring with drag-to-annotate, frozen state with inline/sidebar popovers, optional measurement mode (Option key), payload preview (prompt/crops/hierarchy), send status progression, history list with fix status, verify/re-diff flow, and error states (adb not found, no device, multiple devices, simulator not booted, iOS untrusted, stream dropped). Also includes design tokens (light/dark CSS custom properties, signal colors OKLCH, typography SF Pro + JetBrains Mono), component specs (main window ~1240×820, mirrored phone ~296×640 scaled to device pixels), two layout iterations (canonical 'Rectify Window' with 3-tab side panel Notes/Payload/History vs earlier 'v1' with simpler layout), and a Mock Cart illustrative fixture (e-commerce app with buggy vs fixed states demonstrating the annotate→fix→verify loop). Project owner: Andrew. Tagline: 'Point at a UI bug on a device, box it, comment, send to Claude Code.' |

### references

| kind | ref | note |
| --- | --- | --- |
| code | mcp__rectify__get_latest_annotation | Local MCP tool available in the Claude Code environment, indicating an existing Rectify backend/MCP server with at least a get_latest_annotation endpoint, suggesting the handoff/integration architecture is already partially implemented. |

### existingSolutions

| where | summary | link |
| --- | --- | --- |
| Local MCP integration | A local 'rectify' MCP server already exists exposing at least a get_latest_annotation tool. This is the likely transport mechanism for Rectify (the macOS app) to hand off annotation/frame/hierarchy payloads to Claude Code. Suggests backend plumbing and agent integration are already live. | mcp__rectify__get_latest_annotation |

### glossary

| term | definition |
| --- | --- |
| Annotation | A bounding box drawn by the user on the frozen mirrored screen, marking the location and extent of a UI bug, with an associated free-text comment and optional measurement findings. |
| Staged | Annotations that have been drawn and commented on but not yet sent to Claude Code. The toolbar shows a staged counter badge. Staged annotations are cleared after a successful send. |
| Frozen frame | The exact screenshot captured and held when the user releases a drag selection. All annotations are pinned to this frame. A new drag-release refreezes to a fresh frame. |
| Resolve & Send | The primary action that packages all staged annotations, the frozen frame, per-annotation crops, the view-hierarchy dump, and an auto-generated prompt, then transmits them to the locally running Claude Code agent session. |
| Measure mode (⌥ Measure) | An optional overlay mode activated by holding the Option key over a box, which snaps the box to the underlying matched view in the view hierarchy, shows dashed guide boxes for neighbors, pixel/dp gap measurements along extension lines, and highlights misalignment in red (e.g., hit-target size violations). These measurements become part of the annotation's note. |
| Verify | An action available in History for annotations with status 'Fixed'. Clicking Verify re-captures the live screen and diffs it per-annotation against the original frozen frame to confirm the fix is real or detect regressions. A one-click follow-up re-sends if issues remain. |
| Hierarchy dump (view hierarchy) | A monospace-rendered tree of the app's view/widget structure exported (for Android: accessibility XML or view dump; for iOS: view hierarchy plist/tree) and sent alongside the frame and annotations, with the matched/flagged nodes called out to help the agent understand the structural context. |
| adb (Android Debug Bridge) | Command-line tool (part of Android Platform Tools) that enables communication with an Android device or emulator, used by Rectify to list devices and hand off to scrcpy for mirroring. |
| idb | Command-line tool for iOS that lists simulators and physical devices, used by Rectify to manage and mirror iOS simulators. |
| scrcpy | Command-line tool that mirrors an Android device's screen to the desktop over USB or TCP, with touch forwarding and minimal latency. Rectify runs scrcpy in the background to feed the live stream. |
| simctl | Command-line tool (part of Xcode) that controls iOS simulators (boot, shut down, list, etc.). Rectify uses simctl to manage and mirror iOS simulators. |
| Claude Code session | A locally running Claude Code agent instance that receives the annotation payload (frame, crops, hierarchy, prompt) and attempts to fix the identified UI bugs by editing the app's source code. The agent's fix status and a link to the session appear in the History tab. |
| Mock Cart | An illustrative e-commerce cart screen used as sample mirrored-app content in design screenshots, with buggy vs fixed variants (cramped stepper, clipped text) to demonstrate the annotate→fix→verify loop. Not a Rectify product feature itself. |

### ambiguities

- Which window layout iteration is authoritative for v1: the current 'Rectify Window' with a 3-tab side panel (Notes/Payload/History) and bezel-less mirrored phone, or the earlier 'Rectify Window v1' with a simpler single Annotations+Payload panel and black-bezel device frame? Or was the History tab added later as a deliberate evolution and both are in-scope?
- Exact transport/protocol for sending annotation payloads to Claude Code: is it strictly via the local MCP server (mcp__rectify__get_latest_annotation and related endpoints), or is there also a REST webhook, file-system drop, or other mechanism?
- [RESOLVED via Stage 1 survey] iOS ships at feature parity with Android in v1, or is iOS a later phase? RESOLVED: v1 is Android-first; iOS support (including iOS-specific flows: idb/simctl device picker, simulator-not-booted and device-not-trusted error states) ships in v1.1+. Stage 2 should carry this as a confirmed scope boundary, not an open question.
- What does 'Verify' diffing actually compare: pixel-level image diff, layout/view-hierarchy diff, or a combination? How are false positives (benign visual changes) or false negatives (a fix that didn't actually resolve the reported issue) detected and handled?
- Does Rectify support mirroring multiple devices simultaneously, or is it single-device-per-session? Can a user switch devices mid-session, and if so, does that clear staged annotations?
- Do annotations and history persist across app restarts (e.g., stored in a local SQLite DB, JSON file, or in-memory only)? If persistent, what is the data model and retention policy?
- Is there any auth/account/login model, or is Rectify entirely local-only (no cloud sync, no multi-user, no account needed)?
- Platform scope: is Rectify macOS-only in v1, or should Windows/Linux desktop be explicitly in-scope or out-of-scope for roadmap planning?
- How does Rectify determine 'this bug is fixed' — is it an automated pixel/layout comparison, or does the user manually confirm in the Verify step?
- When the device stream drops mid-flow (screen 16: 'Stream dropped'), are staged annotations and the frozen frame recoverable after reconnect, or are they lost?
- Does the device dropdown (in the toolbar) allow switching devices during an annotation session? If yes, what is the UX for unsaved annotations?

### dictation

```
User request (Ukrainian): "Чи можеш ти будь ласка проаналізувати даний дизайн і на базі нього скласти PRD?" (Can you please analyze this design and build a PRD based on it?) — provided alongside a Claude Design project link (https://claude.ai/design/p/3f54708e-1e7a-4472-92cb-74e0cca25a8f?file=Rectify+Screens.dc.html), which the orchestrator fetched directly via the design-system API. Full extracted design content follows — treat this as the authoritative source.

PROJECT NAME (from Claude Design project metadata): "Rectify: Bug annotation tool". Owner: Andrew.

TAGLINE (from the Screens doc hero): "Point at a UI bug on a device, box it, comment, send to Claude Code. Every window below is live — drag on the phone to annotate."

See intake rawDescription and screens catalog above for full detail; original design files cached at raw/index.json#rectify-design-project.
```
