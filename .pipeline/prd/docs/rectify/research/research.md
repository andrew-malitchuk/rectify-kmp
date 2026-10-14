# rectify — research

## Envelope

| field | value |
| --- | --- |
| stage | research |
| step | research |
| status | done |
| producedBy | sdd-prd-research-competitor |
| schemaVersion | 1 |
| createdAt | 2026-10-11T00:00:00Z |
| updatedAt | 2026-10-11T12:24:19Z |

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

48da83fe494629d4c20c424aedf84800fc22ede25689ed829e49f371434f394d

### inputs

| artifact | hash |
| --- | --- |
| clarify.json | 5f67cd1a15072b7df0589a831081de9b5b759090135a91ef52734c1f40d5f54b |

## Data

### enabled

true

### skipped

false

### competitors

| name | positioning | strengths | gaps | source |
| --- | --- | --- | --- | --- |
| Jam.dev (+ Jam MCP, Jam for iOS) | Screen-recording bug reporter for web and (as of 2026) iOS, with an MCP server that pipes console logs, network requests, user actions, and video/transcript into AI coding agents (Claude, Cursor). | ["Already has a working MCP server read by Claude Code/Cursor to pull rich session context", "Shipped a native iOS app for mobile bug capture in 2026", "Mature PM-tool integrations (Jira, GitHub, Linear, Notion, Slack)"] | ["No Android app yet (iOS only as of 2026; Android explicitly 'up next' per user reviews)", "No live device mirror — captures recorded sessions, not a real-time annotatable mirror", "No bounding-box drag-to-annotate on a live screen, no pixel/dp measurement mode, no view-hierarchy capture", "No closed-loop pixel/hierarchy diff to verify a fix against the original bug frame"] | https://strawberryjam.substack.com/p/meet-the-new-jam-mcp |
| Jam for iOS | Mobile screen recorder for bug/feedback capture, positioned as 'the first screen recorder made for bug reporting on mobile.' | ["Native mobile client", "Auto-attaches OS/device/battery metadata to recordings"] | ["iOS only, no Android", "Recording-based, not a live mirror with drag-to-box annotation or measurement"] | https://jam.dev/blog/launching-today-jam-for-ios/ |
| Marker.io | Website/web-app visual feedback and bug-reporting tool with 2-way sync into PM tools (Jira, Linear, etc.). | ["Rich annotation shapes (arrows, rectangles, text, blur)", "Console/network/metadata capture", "Guest portal for external stakeholders"] | ["Web/browser only — no native mobile device mirroring", "No AI-coding-agent integration found", "No measurement-against-hierarchy or closed-loop verify feature"] | https://marker.io/ |
| BugHerd | Website annotation tool and Kanban-style bug tracker ('sticky notes on the website'). | ["Simple point-and-click annotation pinned to DOM elements", "Auto-captured browser/OS/resolution metadata", "Zapier/GitHub/Slack/Jira integrations"] | ["Mobile support is limited to mobile *websites* via a JS snippet, not native app device mirroring", "No AI-agent handoff or MCP integration found", "No measurement or verify/diff loop"] | https://bugherd.com/website-annotation-tool |
| Userback / Pastel / Markup.io | Web/website visual-feedback annotation tools (sticky-note/comment style) aimed at design review and client feedback, not native mobile apps. | ["Annotated screenshots, screen recordings, session replay (Userback)", "Simple cross-device (desktop/mobile *web*) comment UX (Pastel)"] | ["No native Android/iOS device mirroring", "No AI coding-agent integration found", "No measurement mode or hierarchy capture"] | https://userback.io/feature/screen-annotation/ |
| BrowserStack App Live | Cloud real-device lab for manual mobile app testing, with an annotation toolbar and multi-device interaction sync. | ["Live mirroring of real physical Android/iOS devices (cloud-hosted)", "Built-in screenshot annotation toolbar", "Synchronized multi-device testing (up to 4 devices)"] | ["No AI coding-agent / MCP integration found", "No pixel/dp measurement against view hierarchy", "No closed-loop fix-verification feature", "Cloud/enterprise QA tool, not a lightweight local dev tool"] | https://www.browserstack.com/app-live/features |
| adb-mcp (iksnerd) | MCP server giving AI coding agents (Claude Code, Cursor, VS Code) 73 tools to drive Android emulators/devices over adb — screenshots, UI-hierarchy reads, tap/swipe, logcat, Gradle builds. | ["Already bridges Android device state (screenshots + UI hierarchy with true-pixel element centers) directly to Claude Code via MCP", "Has a `verify_change` parameter that checks whether an interaction actually changed the UI — a primitive closed-loop check"] | ["Built for agent-driven automation/testing, not human-driven visual bug annotation — no drag-to-box UI, no live mirror window for a human to point at", "`verify_change` only detects 'did the UI change,' not a structured pixel+hierarchy diff against a specific previously-annotated region", "No measurement mode, no History/batch tracking UI"] | https://github.com/iksnerd/adb-mcp |
| Aki (macOS, local MCP) | Mac app for pointing at on-screen UI elements (browser windows and native macOS apps) and routing instructions to the correct running Claude Code session via a local MCP server. | ["Already ships a near-identical MCP pattern: `read_annotations`, `wait_for_annotations`, `get_annotation_image`, `resolve_annotation` tools consumed by Claude Code", "Fast point-and-annotate keyboard-driven UX (⇧⌘A, point, write, ⌘⏎)", "Auto-routes to the correct session by detecting localhost port/project folder"] | ["Desktop/browser-only — no native mobile device mirroring (Android or iOS) at all", "Pointer-based element selection, not drag-to-draw bounding boxes with freeform regions", "No measurement mode, no view-hierarchy capture or matching", "No History tab or closed-loop Verify/re-diff feature described"] | https://dev.to/muriloprataviera/i-run-5-claude-code-sessions-at-once-so-i-built-a-mac-app-to-point-at-what-needs-fixing-1513 |
| Bugshot (open-source Claude Code skill) | Claude Code skill that opens a real Chrome window with a capture overlay; drag-select a broken area, add a note, send to Claude. | ["Pixel-perfect coordinates + annotation sent directly into the active Claude Code session", "Zero-infra install via `npx skills add`"] | ["Chrome/web only — no mobile device support", "No measurement mode, no view-hierarchy capture, no closed-loop verification"] | https://dev.to/lokeshgrootan/point-at-ui-bugs-instead-of-describing-them-to-claude-code-48bm |
| UI Annotator (MCP server) | Chrome-extension-based annotator for `localhost` web pages; drops numbered pins with comments and sends annotated screenshots + element JSON to Claude Code. | ["Fully local, no cloud/auth", "Structured element-level JSON alongside screenshots"] | ["localhost web pages only — no native mobile device support", "No measurement mode or verify/diff loop described"] | https://mcpmarket.com/server/ui-annotator |
| Device Screenshot (Claude Code skill) | Claude Code skill that captures and analyzes screenshots from iOS simulators, physical iOS devices, and Android devices via ADB/Xcode tooling, for layout/rendering inspection against design specs. | ["Cross-platform (iOS + Android) screenshot capture already wired into Claude Code"] | ["Static screenshot capture only — no live mirroring, no drag-to-box annotation, no measurement, no view-hierarchy dump, no closed-loop verify"] | https://mcpmarket.com/tools/skills/device-screenshot |
| AppContext | Local macOS menu-bar MCP server exposing a live screenshot of the iOS simulator or current Chrome tab to Claude Code over SSE, with a platform-switch tool. | ["Live (on-demand) visual context for the agent across iOS simulator and web, with a basic agent-driven re-check loop: 'Claude calls fetch_liveview again to verify; if still wrong, it iterates'"] | ["No human-drawn annotation at all — just raw screenshots for the agent to interpret", "No Android support, no measurement mode, no view-hierarchy capture", "Its 'verify' is an unstructured agent re-look, not a pixel-region + hierarchy diff against a specific original annotated frame"] | https://appcontext.dev/docs/claude-code-visual-context/ |
| Percy (BrowserStack) / Applitools / Chromatic | CI-pipeline visual-regression and design-QA tools that diff screenshots across builds, increasingly with AI-assisted 'smart' diffing (Percy's 2025 Visual Review Agent; Applitools' Visual AI). | ["Mature pixel-diff and (Applitools) AI-based visual diffing that suppresses noise and highlights meaningful changes", "Percy's Visual Review Agent reportedly cuts review time and filters ~40% of false-positive diffs"] | ["Built for CI/build-to-build regression testing, not live human annotation of a running device or handoff to a coding agent", "No bounding-box bug annotation, no view-hierarchy node matching tied to a specific user-drawn box, no MCP/agent integration found"] | https://momentic.ai/blog/best-visual-regression-testing-tools |
| Android Studio Layout Inspector | IDE-bundled tool to inspect a running app's live view hierarchy, attributes, and layout/measure data on an emulator or device. | ["Authoritative, first-party view-hierarchy + measure/layout data source for Android", "Live, real-time inspection"] | ["No bug annotation workflow, no screenshot/crop packaging, no agent handoff, no cross-session history or verify loop — it's a developer-facing inspector, not a reporting tool"] | https://developer.android.com/studio/debug/layout-inspector |
| Reveal (iOS/tvOS) / Flipper (iOS+Android+RN) | Third-party native view-hierarchy debuggers with live editing and a dedicated measure tool for distances between views. | ["Cross-platform view-hierarchy + measure tooling (Flipper)", "Live-editable layout inspection in a desktop companion app (Reveal)"] | ["No bounding-box bug annotation tied to a live mirror, no payload packaging or MCP/agent handoff, no history/verify loop"] | https://revealapp.com/features/ |
| Figma Dev Mode MCP server | Official Figma MCP server giving AI coding agents (Claude Code, Cursor, Windsurf) structured access to design-file components, variables, and Code Connect mappings. | ["First-party, well-distributed MCP integration already in production", "Structured (non-screenshot) design context beats pixel-guessing for layout fidelity"] | ["Operates on static design files, not a live running mobile app or a human-reported bug region", "No device mirroring, no bug annotation, no measurement-against-runtime-hierarchy, no fix-verification loop"] | https://www.figma.com/blog/introducing-figma-mcp-server/ |

### priorArt

| name | note | source |
| --- | --- | --- |
| Android Hierarchy Viewer (legacy) | Deprecated predecessor to Layout Inspector; established the pattern of visualizing an Android app's live view-hierarchy tree alongside measure/layout data — the conceptual ancestor of Rectify's hierarchy-dump + measure-mode feature, but with no annotation or agent-handoff concept. | https://developer.android.com/studio/debug/layout-inspector |
| mobile-mcp / ios-simulator-mcp / maestro-mcp / mobile-device-mcp (MCP servers) | A fast-growing 2025-2026 ecosystem of MCP servers that already bridge iOS/Android device/simulator state (screenshots, UI trees, taps) to AI agents — strong prior art that 'device state over MCP to a coding agent' is a validated, increasingly common pattern, though all found examples are agent-driven automation/testing tools, not human-annotation-driven bug reporting with live mirroring. | https://github.com/mobile-next/mobile-mcp |
| Pixelmatch-based Playwright/Cursor visual-diff workflows | Community workflows combining Playwright MCP screenshots with the Pixelmatch diffing library and Cursor Rules to drive iterative AI-agent UI fixes from visual diffs — prior art for 'pixel diff feeds back into an AI coding agent loop,' conceptually adjacent to Rectify's Verify step, but web-browser-based and not tied to a device view-hierarchy. | https://egghead.io/ai-driven-design-workflow-playwright-mcp-screenshots-visual-diffs-and-cursor-rules~aulxx |
| Percy Visual Review Agent / Applitools Visual AI | Prior art for moving beyond naive pixel-diffing to 'smart' AI-assisted diff highlighting that suppresses benign noise — directly relevant prior art for how Rectify's Verify diff engine could reduce false positives/negatives, though neither tool targets live mobile mirroring or per-annotation bug tracking. | https://momentic.ai/blog/best-visual-regression-testing-tools |
| scrcpy (open source) | The underlying open-source Android screen-mirroring engine (keyboard/mouse control, low-latency streaming) that Rectify itself depends on per the intake/clarify docs; several thin GUI wrappers exist (scrcpy-gui, scrcpyui) but none add bug annotation or agent handoff. | https://github.com/SimonAKing/scrcpy-gui |

### differentiators

- Live native-device mirror (adb+scrcpy) as the primary annotation surface with direct drag-to-box drawing in one native macOS window — every Claude-Code-adjacent annotation tool found (Bugshot, UI Annotator, Aki, AppContext, Device Screenshot skill) targets desktop/browser/localhost or static screenshots, none mirror a live Android/iOS device for freeform box annotation.
- Option-key measure mode that snaps to the matched view-hierarchy node and attaches pixel/dp gap data directly to the annotation's note — not found in any bug-report-to-agent tool; the closest analogs (Android Studio Layout Inspector, Reveal, Flipper's measure tool) are standalone inspectors with no annotation-to-agent handoff at all.
- Purpose-built per-annotation payload (full frame + per-box crop + hierarchy XML + auto-generated prompt) delivered straight into a running Claude Code session via a local MCP server — distinct from PM-tool-routed tools (Marker.io, BugHerd, Userback, Pastel) and from generic session-context dumps (Jam MCP's console/network/video bundle) which aren't structured per discrete UI region.
- Closed-loop Verify that re-captures the live device and runs a combined pixel-region-diff + hierarchy/layout-diff per individual annotation against the original frozen frame, with one-click re-send on mismatch — no tool found does this; the nearest analog, AppContext's agent-driven 'fetch_liveview again' re-check, is an unstructured re-look rather than a per-annotation, per-region diff tied to a History record, and adb-mcp's `verify_change` only detects whether the UI changed at all, not whether the specific reported region now matches.

### threats

- Jam.dev is the most adjacent, best-resourced threat: it already ships an MCP server piping rich session context to Claude Code/Cursor and shipped a native iOS mobile client in 2026; Android support is explicitly on its roadmap per user demand, so a live-mirror-plus-annotation Android feature is a plausible next move that would directly undercut Rectify's wedge (confidence: medium — based on Jam's public roadmap signals, not a stated commitment to this exact feature).
- The 'point at a UI bug, send to Claude Code' pattern is being independently reinvented by multiple small, open-source tools in 2026 (Bugshot, UI Annotator, Aki) — this signals a low technical barrier to entry; any of these could add live device mirroring and a measurement/hierarchy layer and converge on Rectify's exact feature set (confidence: medium).
- Anthropic itself is expanding Claude Code/Claude Desktop's native visual tooling (in-app browser, screenshot paste, visual element annotation, Computer Use autonomous debugging) — if Anthropic ships first-party device mirroring or screen annotation, it could absorb Rectify's core value proposition at the platform layer (confidence: low — based on observed trajectory and third-party roadmap commentary, not an announced product).
- The Android/iOS MCP-bridge ecosystem (adb-mcp, mobile-mcp, ios-simulator-mcp, maestro-mcp, mobile-device-mcp) is maturing quickly; any of these agent-automation-focused servers adding a human-annotation UI layer would collapse much of Rectify's current differentiation (confidence: medium).

### evidence

| claim | source | confidence |
| --- | --- | --- |
| Jam.dev's MCP server pipes console logs, network requests, user actions, and video/transcript from a recorded 'Jam' into AI coding agents like Claude and Cursor. | https://strawberryjam.substack.com/p/meet-the-new-jam-mcp | high |
| Jam shipped a native iOS app for mobile bug recording in 2026; Android support is not yet released but is desired/roadmapped. | https://jam.dev/blog/launching-today-jam-for-ios/ | high |
| BrowserStack App Live supports live mirroring of real devices plus a built-in screenshot annotation toolbar and synchronized multi-device interaction testing. | https://www.browserstack.com/app-live/features | high |
| adb-mcp exposes ~73-79 tools for Android emulator/device control via MCP, including screenshots and UI-hierarchy reads with true-pixel element centers, aimed at agent-driven automation/testing rather than human bug annotation; it includes a `verify_change` parameter to detect whether an interaction changed the UI. | https://github.com/iksnerd/adb-mcp | high |
| Aki is a macOS desktop/browser-only app that lets a user point at UI elements and send instructions to a running Claude Code session via a local MCP server exposing read_annotations/wait_for_annotations/get_annotation_image/resolve_annotation tools; it has no live mobile-device mirroring, no measurement mode, and no described History/Verify feature. | https://dev.to/muriloprataviera/i-run-5-claude-code-sessions-at-once-so-i-built-a-mac-app-to-point-at-what-needs-fixing-1513 | high |
| Bugshot is a Chrome-only, open-source Claude Code skill for drag-select-and-annotate bug reporting with no mobile support, measurement, or verify loop. | https://dev.to/lokeshgrootan/point-at-ui-bugs-instead-of-describing-them-to-claude-code-48bm | medium |
| UI Annotator is a localhost-only Chrome-extension MCP tool using numbered pins/comments, with no mobile device support, measurement mode, or verification loop. | https://mcpmarket.com/server/ui-annotator | medium |
| The 'Device Screenshot' Claude Code skill captures static screenshots from iOS simulators/devices and Android devices for layout analysis, with no live mirroring, annotation, measurement, or hierarchy dump. | https://mcpmarket.com/tools/skills/device-screenshot | medium |
| AppContext is a local macOS menu-bar MCP server providing live screenshots of an iOS simulator or Chrome tab to Claude Code, with an agent-driven 'fetch_liveview again to verify' loop but no human annotation, measurement, or hierarchy capture. | https://appcontext.dev/docs/claude-code-visual-context/ | medium |
| Percy launched an AI-powered 'Visual Review Agent' in late 2025 that uses smart bounding-box highlighting instead of raw pixel-level diffing and filters ~40% of false-positive visual changes. | https://momentic.ai/blog/best-visual-regression-testing-tools | medium |
| Android Studio's Layout Inspector exposes a live component tree, attributes, and measure/layout data for a running app, but has no bug-annotation, payload-packaging, or agent-handoff workflow. | https://developer.android.com/studio/debug/layout-inspector | high |
| Figma's Dev Mode MCP server gives AI coding agents structured (non-screenshot) access to design-file components/variables/Code Connect mappings, operating on static design files rather than a live running mobile app. | https://www.figma.com/blog/introducing-figma-mcp-server/ | high |
| No credible source was found of any existing tool combining live native-device mirroring, freeform drag-to-box annotation, Option-key view-hierarchy measurement, and a direct local-MCP handoff to a coding agent with closed-loop pixel+hierarchy Verify — the full Rectify combination appears novel as of this research pass (October 2026). | aggregated across all sources above | low |

### implicationsForPrd

- Position Rectify's moat explicitly as the *combination* — live native-device mirror + drag-to-box annotation + Option-key measure-vs-hierarchy + MCP handoff + closed-loop pixel/hierarchy Verify — since every individual piece already exists in some competitor or prior-art tool, but no found product combines all of them; the PRD's differentiation section should state this combination claim, not any single feature, as the defensible wedge.
- Prioritize shipping the Android mirror-plus-annotate flow and the Verify diff engine ahead of any sign that Jam.dev (which already has MCP + a 2026 iOS client and explicit Android demand) extends to Android with live mirroring — this is the PRD's most time-sensitive competitive window (confidence: medium per evidence above).
- Because Aki's MCP tool surface (`read_annotations`/`get_annotation_image`/`resolve_annotation`) closely parallels Rectify's own `rectify` MCP server pattern (`get_latest_annotation`), the PRD/technical design should treat MCP tool-naming and payload schema as a point of intentional design, not an incidental detail, given a nearly identical tool already exists for desktop/browser use cases.
- Flag as a low-confidence, speculative-but-monitorable risk (not yet evidenced) that Anthropic could ship native device-mirroring or screen-annotation inside Claude Code/Claude Desktop, or that an enterprise device-cloud vendor (BrowserStack/Genymotion) could add an MCP/agent integration — recommend a light 'watch list' note in the PRD's risks section rather than a scope change.
- This competitive landscape is a snapshot of a fast-moving MCP/agent-tooling ecosystem as of October 2026 (several of the closest competitors — Aki, Bugshot, UI Annotator, AppContext, Jam MCP — only emerged or added agent integrations within the current year); recommend the PRD schedule a re-scan of this competitive section at the next major release milestone rather than treating it as static.
