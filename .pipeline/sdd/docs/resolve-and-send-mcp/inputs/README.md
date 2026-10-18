# Stage-0 inputs — resolve-and-send-mcp (F-7)

## Design references
Extracted from `~/Downloads/Rectify Screens.html` (a self-extracting DC bundle;
the raw file is unreadable without decoding).

- `design-rectify-ui-kit.html` — Rectify UI Kit: button sizes/states (toolbar
  `Resolve & Send` primary + `Clear` ghost, disabled-until-1-annotation state,
  `⌘↩` shortcut affix), toast spec ("Sent 3 annotations to Claude Code" +
  `Open session` action), and the send status step list
  `['Streaming','Frozen','Dumping hierarchy','Sent']` incl. the failure variant
  `['Stream dropped','Frozen','Dumping hierarchy','Sent']`.
- `design-rectify-window-screens.html` — Rectify Window, all 16 screens
  (`drag`, `empty`, `err-adb`, `err-multi`, `err-nodevice`, `err-sim`,
  `err-stream`, `err-trust`, `history`, `measure`, `one`, `payload`, `picker`,
  `success`, `three`, `verify`). Relevant here: `payload`, `success`,
  `err-stream`. Note `sendLabel: phase === 'dumping' ? 'Sending…' : 'Resolve & Send'`.

## PRD handoff
`.pipeline/prd/docs/rectify/handoff/resolve-and-send-mcp.{json,md}` — FR-16..19,
NFR-3/4/12, AC-17..21 + AC-32. dependsOn F-6 (payload-preview, merged).

## Note
The path `~/Documents/rectify/.pipeline` passed on the command line is a STALE
copy of this project's PRD workspace (its portfolio still shows every
deliverable `not-started`). The authoritative workspace is this repo's
`.pipeline/`. Not used as an input.
