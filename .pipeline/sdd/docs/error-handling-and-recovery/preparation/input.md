# error-handling-and-recovery — input

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | input |
| status | done |
| producedBy | sdd-build-discussion-aggregate |
| schemaVersion | 1 |
| createdAt | 2026-10-15T06:06:43Z |
| updatedAt | 2026-10-15T06:06:43Z |

## Gate

### type

human

### approvedBy

orchestrator-auto

### approved

true

### at

2026-10-15T06:06:43Z

## Provenance

### selfHash

dabd7fc37392dc7bdb92b2e3b34fbf9905d48ef77e1a2330405cb32e3ba8b2e5

### inputs

| artifact | hash |
| --- | --- |
| inputs/raw/prd-handoff.json | 1dec12c70191083c62977c55269548096a4005a3395fc2b7491758ccebc1d56c |

## Data

### title

Error Handling & Stream Recovery

### goalStatement

The structured error-state system: a reusable error-card component (icon-in-circle, title, body, optional copyable command block, primary + secondary actions, footer diagnostic line) instantiated for adb-not-in-PATH ('Locate adb…' / 'Retry'), no-device-detected (kill-server command, 'Restart adb' / 'Boot emulator instead'), and multiple-devices-connected (inline device list rather than a modal, primary action naming the top device); plus the mid-session stream-drop banner that floats non-blockingly over the stale mirror, keeps staged annotations pinned, offers 'Reconnect', and leaves 'Resolve & Send' usable while disconnected. Why: research flags adb/scrcpy fragility across OS and device versions as the biggest execution risk to the >=95% first-attempt connect target, so recoverable failure is a product feature, not polish.

### rawDescription

The structured error-state system: a reusable error-card component (icon-in-circle, title, body, optional copyable command block, primary + secondary actions, footer diagnostic line) instantiated for adb-not-in-PATH ('Locate adb…' / 'Retry'), no-device-detected (kill-server command, 'Restart adb' / 'Boot emulator instead'), and multiple-devices-connected (inline device list rather than a modal, primary action naming the top device); plus the mid-session stream-drop banner that floats non-blockingly over the stale mirror, keeps staged annotations pinned, offers 'Reconnect', and leaves 'Resolve & Send' usable while disconnected. Why: research flags adb/scrcpy fragility across OS and device versions as the biggest execution risk to the >=95% first-attempt connect target, so recoverable failure is a product feature, not polish.

### sources

| type | ref | summary |
| --- | --- | --- |
| text | inputs/raw/prd-handoff.json | PRD handoff from product & design: FR-23 (structured error card), FR-24 (stream-drop banner), NFR-6 (preserve annotations across disconnect), NFR-8 (keyboard navigation), NFR-9 (light/dark parity), AC-26/AC-27/AC-28/AC-29 (all test), SC-10..SC-13. |
| text | inputs/operator-dictation.md | Operator dictation: F-1, F-2, F-4 merged (196 tests green); ErrorCard exists fully-formed per FR-23; F-2's typed states (ToolMissing, NoDeviceFound, PollFailed) left for this spec to wire; F-4's session-loss clears annotations (violates NFR-6); two comps exist (04, 05), two don't (AC-28, FR-24); ID collision on SC-10/SC-11. |

### codeReferences

| repo | path | symbol | note |
| --- | --- | --- | --- |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/organism/error/ErrorCard.kt | ErrorCard(title, body, modifier, kind, command, onCopyCommand, primaryActionLabel, onPrimaryAction, secondaryActionLabel, onSecondaryAction, diagnostic) | Component exists with full shape; exactly matches FR-23 requirement. Signature: title, body, kind: ErrorKind = Fixable, command: String?, onCopyCommand, primaryActionLabel: String?, onPrimaryAction, secondaryActionLabel: String?, onSecondaryAction, diagnostic: String? |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/command/CommandBlock.kt | CommandBlock(command, onCopy, modifier, copyLabel) | Copyable command block already exists; used by ErrorCard for command display |
| rectify-kmp | presentation-core-ui/src/commonMain/kotlin/presentation/core/ui/source/kit/molecule/row/SourceListRow.kt | SourceListRow | Device list row component; F-2 added `enabled` parameter (A11Y-6: disabled = no click handler AND marked disabled for a11y, not just dimmed). Candidate for AC-28's inline device list. |
| rectify-kmp | domain-core/src/commonMain/kotlin/domain/core/source/model/device/DiscoveryModel.kt | ToolMissing(tool, probedPaths, installCommand, envHint), NoDeviceFound(toolVersion, lastPollEpochMs, attempts, attachedCount), PollFailed(tool, reason) | F-2 left three typed states for F-3 to render: ToolMissing carries probedPaths (every path tried in order, rendered in comp 04 footer); NoDeviceFound carries stats; PollFailed (NOT NoDeviceFound per KDoc: one means nothing attached, one means nothing learned) — F-3 must decide whether it gets its own card or folds into another state. |
| rectify-kmp | presentation-feature-mirror/src/commonMain/kotlin/presentation/feature/mirror/source/mirror/MirrorViewModel.kt | onSessionLost(deviceId), resumeLive() | F-4 session loss: onSessionLost sets MirrorSession.Lost and nulls sessionDeviceId (lines 182-185); resumeLive clears currentFrozenFrame.value and frozen state (lines 337-340), deliberately erasing annotations. Violates NFR-6 — disconnect must NOT take resumeLive path. |

### designReferences

| screen | nodeId | cached | description |
| --- | --- | --- | --- |
| 04-error-adb-not-on-path | raw/design/04-error-adb-not-on-path.png | raw/design/04-error-adb-not-on-path.png | Comp 04: Error card for adb not on PATH. Icon-in-circle (!), title 'adb isn't on your PATH', body explaining platform-tools, mono command block 'brew install --cask android-platform-tools' with Copy button, primary 'Locate adb…' (dark fill), secondary 'Retry' (text), footer 'which adb → not found · $ANDROID_HOME unset'. Status bar: '● adb not found in PATH'. Centred on dotted stage, not on bg. Maps to AC-26 / SC-10. |
| 05-error-no-device-detected | raw/design/05-error-no-device-detected.png | raw/design/05-error-no-device-detected.png | Comp 05: Error card for no device detected. Same frame as 04, title 'No Android device detected', body about USB debugging, command 'adb kill-server && adb devices', primary 'Restart adb', secondary 'Boot emulator instead', footer 'adb 35.0.1 · last poll 0.8 s ago · 0 attached'. Maps to AC-27 / SC-11. |
| 01-first-run-platform-source | raw/design/01-first-run-platform-source.png | raw/design/01-first-run-platform-source.png | Comp 01: Device picker initial state (context only, not this spec's responsibility — F-2 delivered this). Physical + Emulator sections, full stage coverage on bg (not dotted stage). |
| 02-empty-no-device | raw/design/02-empty-no-device.png | raw/design/02-empty-no-device.png | Comp 02: NO SIGNAL empty state (context only, F-2 delivered). Dashed phone outline, recovery steps, inline Boot button. Renders on dotted stage, not on bg. |
| 03-device-picker-first-run | raw/design/03-device-picker-first-run.png | raw/design/03-device-picker-first-run.png | Comp 03: Device picker when device already selected (context only, F-2 delivered). Same frame as 01, used on reopening from toolbar chip. |

### ticketRefs


### existingSolutions

| where | summary | link |
| --- | --- | --- |
| this-project | F-1 (app-shell-window-layout): Shell, toolbar, status bar, stage infrastructure all exist. F-2 (android-device-discovery-mirroring): DiscoveryModel typed states, device picker, detection, 2s polling all live. F-4 (drag-annotate-frozen-frame): Session lifecycle, frame freeze/unfreeze, annotations wiring — but session-loss implementation clears annotations (violates NFR-6). | .pipeline/sdd/docs/[F-1/F-2/F-4 specs] |

### glossary

| term | definition |
| --- | --- |
| ToolMissing | F-2 typed state (DiscoveryModel.ToolMissing): adb/idb binary not found. Carries probedPaths (every candidate path tried in order), installCommand (platform-specific shell line), envHint. Rendered by comp 04. |
| NoDeviceFound | F-2 typed state (DiscoveryModel.NoDeviceFound): tool works but no usable device attached across repeated polls. Carries toolVersion, lastPollEpochMs (wall-clock of last completed poll), attempts (consecutive empty polls), attachedCount (devices present but unusable, e.g. unauthorized). Rendered by comp 05. |
| PollFailed | F-2 typed state (DiscoveryModel.PollFailed): poll could not complete because tool itself failed or timed out. Deliberately NOT NoDeviceFound per KDoc: that asserts the bench is empty, this one asserts nothing learned. Carries tool name, reason (tool's own stderr tail or timeout note). F-3 must decide whether to render it. |
| NFR-6 (Annotation Persistence) | Stream-drop handling must preserve all staged annotations without loss across a disconnect/reconnect cycle. This is the real engineering content; everything else is wiring. F-4's resumeLive currently clears annotations (violates this); F-3 must change the path so a disconnect does NOT trigger resumeLive's full reset. |
| FR-23 (Structured Error Card) | Render error-card states for: adb not in PATH (FR-23a), no device detected (FR-23b), multiple devices connected with inline device list (FR-23c). ErrorCard component fully exists; this spec wires it to the three typed states. |
| FR-24 (Stream-Drop Banner) | On mid-session stream drop, show non-blocking floating banner over stale mirror: 'Stream dropped' / 'Your N annotations are kept on the frozen frame' / 'Reconnect' button. Staged annotations stay pinned, 'Resolve & Send' remains usable while disconnected. No comp exists; derive from shipped components. |
| AC-26 / SC-10 | Given adb is not found on PATH, error card renders with install command (Copy button), primary 'Locate adb…', secondary 'Retry'. Verification: test. Comp 04. |
| AC-27 / SC-11 | Given no Android device detected after launch, error card renders with kill-server command, primary 'Restart adb', secondary 'Boot emulator instead'. Verification: test. Comp 05. |
| AC-28 / SC-12 | Given 3 devices connected simultaneously, error card shows inline device list (not modal), primary action names top device (e.g. 'Mirror Pixel 8 Pro'). Verification: test. No comp. |
| AC-29 / SC-13 | Given stream drops mid-session with staged annotations, non-blocking banner appears stating stream dropped + annotations kept, with 'Reconnect' primary action; 'Resolve & Send' remains clickable, annotations remain pinned. Verification: test. No comp. |
| ID Collision: SC-10 & SC-11 | F-4 bound SC-10 ('Notes tab auto-switch') and SC-11 (scenario from F-4) to its own scenarios. PRD uses SC-10..SC-13 for error scenarios. Keep PRD's canonical ids; record the clash explicitly so Stage 5 does not conflate. F-4 had identical problem with F-1's SC-3/SC-4 and recording it kept it straight. |
| SEC-1 (Open from F-2) | F-2's DeviceCommand.GetProp takes unvalidated free text into 'adb shell getprop <name>', which adb re-joins into a device-side sh command line. Unused in production today, but contradicts 'no free text' claim. If this spec needs device properties, do not become the first caller without closing SEC-1 first. |

### attachments


### ambiguities

- NFR-6 is the only real engineering content. Everything else is wiring. F-4's resumeLive deliberately clears the frozen frame AND annotations; a disconnect must NOT take that path. What survives today on a mid-session drop (currently: frame clears, annotations lost), and what must change to satisfy NFR-6 (annotations must survive)? Propose a test that would actually fail if annotations were dropped; F-4 shipped a vacuous session-loss test.
- No comp exists for AC-28 (multiple devices connected with inline device list) or FR-24 (stream-drop banner). Both are unillustrated; derive them from shipped components (SourceListRow + AppButton for AC-28, and a floating overlay banner for FR-24) and keep invented values as named constants.
- ID COLLISION explicit: F-4 bound SC-10 and SC-11 to its own scenarios ('Notes tab auto-switch' and similar). This PRD uses SC-10..SC-13 for the error scenarios. Keep the PRD's canonical ids but state the clash explicitly so Stage 5 does not conflate them — F-4 had the same problem with F-1's SC-3/SC-4 and recording it was what kept it straight.
- PollFailed handling: F-2's PollFailed state (tool failed/timed out, nothing learned) carries tool name + reason. Should it get its own error card, or fold into NoDeviceFound's flow? The KDoc explicitly says they are different, but the UX is not illustrated.
- Multi-device picker (AC-28): SourceListRow carries an `enabled` parameter that F-2 added (A11Y-6: disabled rows have no click handler AND are marked disabled for a11y, not just dimmed). Is inline list rendering + multi-selection flow correct, or does it need a modal-like interaction (like comps 01/03)?

### dictation

```
# Operator dictation — error-handling-and-recovery (F-3)

Autonomous run. Gates are closed by the orchestrator taking the recommended option; rationale is
recorded in each artifact's `survey[]` with `source: ai-proposed` (no human chose them).

## 1. This spec is mostly WIRING — verify that before planning new work

F-1, F-2 and F-4 are merged to `development`. **196 desktop tests green. Keep them green.**

**`ErrorCard` already has exactly the shape FR-23 describes.** Read
`presentation-core-ui/.../kit/organism/error/ErrorCard.kt:63-75`:

```
ErrorCard(title, body, modifier, kind: ErrorKind = Fixable, command: String? = null,
          onCopyCommand, primaryActionLabel: String? = null, onPrimaryAction,
          secondaryActionLabel: String? = null, onSecondaryAction, diagnostic: String? = null)
```

Icon-in-circle, title, body, optional copyable command block, primary + secondary actions, footer
diagnostic — the whole requirement, already built. `CommandBlock(command, onCopy, modifier,
copyLabel)` exists too, at `kit/molecule/command/CommandBlock.kt:47`.

**F-2 deliberately left you the typed states rather than rendering the cards itself.** In
`domain-core/.../source/model/device/DiscoveryModel.kt`:
- `ToolMissing(...)` — carries the tool, **`probedPaths`** (every path the ordered probe tried)
  and `installCommand`. Comp 04's footer literally renders those probed paths.
- `NoDeviceFound(...)` — carries `toolVersion`, `lastPollEpochMs`, `attempts`, `attachedCount`.
- `PollFailed(...)` — added by F-2's review CR. Its KDoc says explicitly it is **not**
  `NoDeviceFound`, because a failed poll asserts nothing while an empty poll asserts the bench is
  empty. Decide whether F-3 renders it, and say so.

So expect the honest answer to be "assemble, don't author" again, as it was for F-1, F-2 and F-4.
Browse the whole `kit/` tree (~120 components) before authoring anything.

## 2. Design comps — two of the five are yours

`raw/design/04-error-adb-not-on-path.png` and `05-error-no-device-detected.png` are this spec's
screens, cached and measured-against-able:
- **04**: `!` in a circle, "adb isn't on your PATH", body explaining platform-tools, a mono command
  block `brew install --cask android-platform-tools` with a `Copy` button, primary **Locate adb…**
  (dark fill), secondary **Retry** (text), and a muted footer
  `which adb → not found · $ANDROID_HOME unset`. Status bar reads `● adb not found in PATH`.
- **05**: same frame, "No Android device detected", body about USB debugging, command
  `adb kill-server && adb devices`, primary **Restart adb**, secondary **Boot emulator instead**,
  footer `adb 35.0.1 · last poll 0.8 s ago · 0 attached`.

Both sit centred on the dotted stage, not on `bg`. Measure against them the way F-1 did —
normalise the comp to the shell's logical space and compare container landmarks. That method found
two real pixel defects in F-1.

**No comp exists for AC-28 (multiple devices) or FR-24 (the stream-drop banner).** Those are
unillustrated; derive them from the shipped components and keep invented values as named constants.

## 3. Requirements

FR-23 (the structured error-card states), FR-24 (**non-blocking floating banner** over the stale
mirror on a mid-session drop: "Stream dropped" plus "Your N annotations are kept on the frozen
frame"). NFR-6 (**no annotation loss across a disconnect/reconnect cycle** — this is the one with
teeth), NFR-8 (keyboard-navigable error-card actions), NFR-9 (light/dark parity).
AC-26, AC-27, AC-28, AC-29 — **all four are `test`**, not demo. SC-10..SC-13.

**ID COLLISION, handle it explicitly:** F-4 just bound **SC-10 and SC-11** to its own scenarios
(`Notes tab auto-switch` and similar). This PRD uses SC-10..SC-13 for the error scenarios. Keep
the PRD's canonical ids but state the clash in the payload so Stage 5 does not conflate them —
F-4 had the same problem with F-1's SC-3/SC-4 and recording it was what kept it straight.

## 4. NFR-6 is the real engineering content

Everything else is wiring; "preserve all staged annotations without loss across a disconnect/
reconnect cycle" is not. F-4 put annotations in the Orbit state and the frozen bitmap on a
separate `StateFlow`, and its `resumeLive` deliberately clears both. A disconnect must NOT take
that path. Trace what currently happens on session loss — F-4's `onSessionLost` sets
`MirrorSession.Lost` and nulls `sessionDeviceId` — and work out exactly what survives today versus
what NFR-6 requires. Propose a test that would actually fail if annotations were dropped;
F-4 shipped a vacuous session-loss test that passed against deleted code, so this area has
already burned us once.

## 5. Conventions and traps this chain has already hit

`CLAUDE.md` + all 19 `.claude/rules/`.
- No literal `Color(0x…)`, raw `.dp` at a call site, or hardcoded `TextStyle`; strict
  atom → molecule → organism; `core/` internal-only; `explicitApi()`.
- **A `commonTest` backtick name containing a comma, apostrophe or colon compiles on the JVM and
  FAILS `compileTestKotlinIosArm64`.** F-1 shipped that and broke the repo build.
- `withTimeoutOrNull`, never `withTimeout` — `resultLauncher` catches `CancellationException`.
- Every new user-facing string needs a `mirror_*`-style key that is actually referenced. F-4's
  T-12 refused to add a seventh dead key; do the same.
- **A test that cannot fail is worse than no test.** Two specs in this chain have now shipped one.
  If you claim mutation-verification, actually run the mutation.
- A fresh worktree lacks `local.properties`; the orchestrator seeds it.
- Kotlin daemon capped at 3GB — build per module.

Known-good to reuse: `ErrorCard`, `CommandBlock`, `AppButton`, `StatusIndicator`, `DeviceChip`,
`SourceListRow` (for AC-28's inline device list — F-2 added an `enabled` parameter).

## 6. Scope

Error states, recovery actions and the stream-drop banner. Measurement (F-5), payload preview
(F-6), MCP send (F-7), history (F-8) and iOS (F-10) stay out. F-2's `SEC-1` is still open
(`GetProp` takes unvalidated free text into `adb shell` with zero production callers) — if this
spec needs device properties, do not become its first caller without closing it.

```

### rawCacheDeltas
