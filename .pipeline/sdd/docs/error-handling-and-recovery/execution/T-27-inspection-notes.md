# T-27 — AC-55 and AC-56 inspection notes

Both ACs carry `verificationMethod: inspection`, a deviation from preparation's
literal `test` that the Stage-2 judge accepted. This file is that inspection. It
deliberately adds **no assertion**, because the only assertions available would be
ones that cannot fail for the reason the AC cares about — which is this chain's
recurring defect, not a style preference.

## AC-55 — every action is keyboard reachable

### The components, named

| surface | component | reachability |
|---|---|---|
| Error card primary (`Locate adb…`, `Restart adb`, `Mirror <device>`, `Retry`) | `AppButton` | a Material3 `Button` underneath, so it takes focus and fires on Space/Enter without any work from us |
| Error card secondary (`Retry`, `Boot emulator instead`, `Choose another`) | `AppButton`, Ghost variant | same |
| Multi-device rows | `SourceListRow` | `enabled = false` **drops the click handler and marks the row disabled** rather than merely dimming it (A11Y-6, recorded in `presentation-core-ui/CLAUDE.md`), so an unauthorised handset is skipped by focus traversal instead of being a focusable dead end |
| Stream-drop banner action (`Reconnect`) | `AppButton`, Primary variant, inside `FeedbackBar` | same |
| `Copy` on the command block | `CommandBlock` | same |

Reachability is **structural**: every one is a real focusable Compose node in a
`Column`/`Row`, in reading order, with nothing intercepting focus. No custom
`focusProperties`, no `clearAndSetSemantics`, no manual `FocusRequester` chain
anywhere in `MirrorErrorStage.kt` — the file composes shipped kit atoms and
nothing else.

### Why no test

A keyboard-traversal assertion needs a Compose UI-test harness. This repo has
none (`.claude/rules/testing.md`: *"No Compose UI-test harness exists… Introducing
a UI-test harness is a real infrastructure decision, not a casual addition"*), and
F-3 deliberately does not add one.

What could be written without a harness is a test asserting the components are
`AppButton`s — which proves nothing about focus, only about our own source code,
and would pass even if focus were broken. **That is a test dressed up as a proof,
and TAC-3 forbids it.**

### What would actually close this

A `runComposeUiTest` harness, then a traversal test per card state. That is its
own spec. Until then: the claim is structural reachability via shipped components
with no focus overrides, verified by reading, and recorded as such.

## AC-56 — light and dark parity

### The only new colour in this spec

`overlayScrim` (T-30). One token, the **same fixed value in both palettes** —
`Color(0xFF0C0C0E)` — because the design writes it as a literal rather than a
theme variable: the scrim sits over mirrored device UI of unknown brightness, so a
theme-following value can wash out against a light screenshot, the same reasoning
that makes `CommentPopover` always dark.

`StatusIndicatorState.Blocked` (T-32) introduced no colour: it reuses the existing
`warnStillOff` signal token.

Everything else in `MirrorErrorStage.kt`, `FeedbackBar` and the status line reads
existing `Theme.color` tokens, both palettes of which are already swept.

### Coverage decision: exempt by name, not silently skipped

`overlayScrim` is **not** in `PaletteContrastTest`'s pairing sweep, and that is
deliberate and enforced:

- A scrim is neither text (SC 1.4.3) nor a UI-component boundary (SC 1.4.11). Its
  job is to *lower* the contrast of what is under it, so there is no ratio it is
  supposed to clear — not even in the waived sense the hairlines have, which at
  least nominally fall under 1.4.11.
- More decisively, it is the only token with **no palette surface behind it**.
  The sweep's method is to composite a token over the surfaces it is actually
  drawn on (A11Y-2); for this one there are none. `overlayScrim over bg/chrome/
  stage/surface` would measure four pairings the app never paints.
- Its 0.42 alpha lives in `presentation-core-ui`, so `presentation-core-styling`
  could not composite it correctly even if a surface existed.

This is **not silence**. `PaletteContrastTest` now carries
`every declared colour token is either swept or explicitly exempt`, which reads
the token names out of `ThemeColor`'s own `toString()` and fails unless each is
either measured or named in `EXEMPT_TOKENS` with its reason. `overlayScrim` and
`stageDot` are the two named entries.

That test exists because the older
`every pairing in both palettes is measured - none silently skipped` asserted only
the pairing-list length, so a new token could be added, never measured, and the
suite would stay green while promising otherwise — which is exactly how both of
those tokens came to be unmeasured in the first place. Proven by mutation:
emptying `EXEMPT_TOKENS` fails with the unmeasured tokens named.

### Rendering parity

Covered by `@Preview` pairs in light and dark, per the repo's established
convention. `MirrorFrozenStageCrowdedPreview` was deliberately given the dropped
state — `canResumeLive = false` plus a populated banner — so the banner and the
withheld Resume Live both render in both themes. `MirrorErrorStage` ships light
and dark preview pairs per card state.

A human looking at a picture is the honest description of this check, and it is
the only rendering check this repo has.
