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
