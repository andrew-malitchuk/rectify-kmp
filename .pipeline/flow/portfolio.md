# Portfolio

_generated 2026-10-14T17:44:12Z · 10 deliverable(s)_

| # | Deliverable | Prio | PRD | UI/UX | SDD | QA | Overall | Depends on |
|---|-------------|------|-----|-------|-----|----|---------|-----------|
| 1 | **Android Device Discovery & Live Mirroring**<br>`android-device-discovery-mirroring` | must | done | n/a | not-started | not-started | [!] blocked-deps | app-shell-window-layout |
| 2 | **App Shell & Canonical Window Layout**<br>`app-shell-window-layout` | must | done | n/a | not-started | not-started | [>] ready | — |
| 3 | **Drag-to-Annotate & Frozen Frame**<br>`drag-annotate-frozen-frame` | must | done | n/a | not-started | not-started | [!] blocked-deps | app-shell-window-layout, android-device-discovery-mirroring |
| 4 | **Error Handling & Stream Recovery**<br>`error-handling-and-recovery` | must | done | n/a | not-started | not-started | [!] blocked-deps | app-shell-window-layout, android-device-discovery-mirroring |
| 5 | **History & Batch Tracking**<br>`history-batch-tracking` | must | done | n/a | not-started | not-started | [!] blocked-deps | resolve-and-send-mcp |
| 6 | **iOS Platform Support (v1.1+)**<br>`ios-platform-support` | could | done | n/a | not-started | not-started | [!] blocked-deps | android-device-discovery-mirroring, error-handling-and-recovery, verify-and-rediff-loop |
| 7 | **Option-Key Measure Mode**<br>`measure-mode` | must | done | n/a | not-started | not-started | [!] blocked-deps | drag-annotate-frozen-frame |
| 8 | **Payload Preview (Prompt / Crops / Hierarchy)**<br>`payload-preview` | must | done | n/a | not-started | not-started | [!] blocked-deps | drag-annotate-frozen-frame, measure-mode |
| 9 | **Resolve & Send via Local MCP**<br>`resolve-and-send-mcp` | must | done | n/a | not-started | not-started | [!] blocked-deps | payload-preview |
| 10 | **Verify & Re-diff Loop**<br>`verify-and-rediff-loop` | must | done | n/a | not-started | not-started | [!] blocked-deps | measure-mode, history-batch-tracking |

## What to do next

- **ready** (1) → sdd-flow-prepare-input, then sdd-flow-launch-spec: `app-shell-window-layout`
- **blocked-deps** (9) → launch the blocking dependencies first: `android-device-discovery-mirroring`, `drag-annotate-frozen-frame`, `error-handling-and-recovery`, `history-batch-tracking`, `ios-platform-support`, `measure-mode`, `payload-preview`, `resolve-and-send-mcp`, `verify-and-rediff-loop`
