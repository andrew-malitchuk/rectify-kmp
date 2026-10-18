# Correction: FR-33's stated mechanism was wrong (the outcome is not)

**Raised by:** plan drafters A and B independently, both from reading the code.
**Applies to:** binding decision #2, as worded in `preparation.json` and in the
Stage-2 drafting briefs.

## What the decision said

> Involuntary disconnect never reaches resumeLive's clear logic; it moves to a
> separate "disconnected, preserved" sub-state.

The first clause describes work to be done. It is **already true in shipped
code**, so an executor implementing it literally would restructure
`onSessionLost` for no behavioural change.

## What the code actually does

`MirrorViewModel.kt:182-185`:

```kotlin
private suspend fun onSessionLost(deviceId: String) {
    intent { reduce { state.copy(session = MirrorSession.Lost(deviceId)) } }
    sessionDeviceId.value = null
}
```

It sets `MirrorSession.Lost` and nulls `sessionDeviceId`. It does **not** touch
`currentFrozenFrame` or `state.frozen`. Clearing lives only in
`resumeLive` (`MirrorViewModel.kt:337-340`):

```kotlin
private fun resumeLive() = intent {
    currentFrozenFrame.value = null
    reduce { state.copy(frozen = null) }
}
```

So the involuntary path already preserves annotations. F-4's
`sessionLostWhileFrozenKeepsTheAnnotations` passes non-vacuously, and the
earlier note calling NFR-6 "violated on the disconnect half" was imprecise.

## Where the violation actually is

`MirrorAnnotationStage.kt:292-298` composes the Resume Live button gated on
nothing but `frozen != null`:

```kotlin
frozen?.let { pinned ->
    ...
    AppButton(text = stringResource(Res.string.mirror_action_resume_live),
              onClick = { onIntent(MirrorIntent.ResumeLive) }, ...)
}
```

`Reconnect` exists nowhere in the codebase (verified by grep across
`presentation-feature-mirror` and `data-device-api`). So a user whose device
dropped mid-session is offered exactly one action, and it destroys their
annotations. NFR-6 is violated through the UI affordance, not through the
state transition.

## Corrected scope for FR-33

The work is **additive and subtractive in the UI layer**, not a reroute in the
view model:

1. Add `MirrorIntent.Reconnect` and its handler (nothing to undo first).
2. Gate the Resume Live button so it is not offered while the session is
   `Lost` — the button's own behaviour stays byte-for-byte unchanged, which is
   what keeps F-4's confirmed AC-51 intact.
3. Offer Reconnect in that state instead.

## Consequence for the test plan

AC-54's red-demonstrating run must drive **`MirrorIntent.ResumeLive`** while
the session is `Lost`, and assert the annotations survive because the button
was never offered. A test that drives `onSessionLost` alone would pass against
unmodified `main` — i.e. it would be this chain's **second vacuous test**, which
is precisely the failure mode the no-vacuous-tests constraint exists to stop.
