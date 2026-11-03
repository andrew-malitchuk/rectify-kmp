# Verify and history

## History

<kbd>⌘3</kbd>, or the **History** tab. Every batch you have ever sent, newest first, grouped by
day. Each row carries a thumbnail, the first comment as a title, the note count, the time, a
short batch id, and a status chip.

History survives restarts — it is a real SQLite database at `~/.rectify/rectify.db`, with the
frames and crops beside it under `~/.rectify/history/`.

## Verify

Each row has a **Verify** action. It is not gated on status: nothing in the current version
automatically marks a batch fixed, so gating would make the button unreachable exactly when you
want it.

Verify does four things:

1. **Loads the batch** back off disk — the original frozen frame and every annotation rectangle.
2. **Re-captures** the device as it is right now.
3. **Diffs** each annotation region, comparing pixels against the original and re-reading the
   view hierarchy for layout changes.
4. **Reports** a verdict per annotation, not per batch.

The stage shows BEFORE and AFTER side by side, each with its annotation rectangles drawn on, so
you can see what the verdict is talking about.

## Reading the verdicts

| Verdict | Means |
|---|---|
| <span class="dot dot-fixed"></span> **Fixed** | The region changed in a way consistent with the report. |
| <span class="dot dot-warn"></span> **Still off** | The region is materially unchanged. |

The result panel sums it up as `n of m fixed`, and each row shows the measurement that led to
the call — `top Δ4dp → 4dp`, `66×74dp → 66×74dp`.

A combined pixel and hierarchy diff is used rather than pixels alone because a layout fix can
move a view without changing many pixels, and an unrelated content change can alter a great many
pixels without touching the bug.

## Acting on the result

**Send follow-up** re-stages only the annotations that came back *Still off* and sends them as a
new batch — the agent gets the ones that still need work, not a re-run of everything.

**Mark all as verified** closes the batch out by hand, for the cases where you can see the fix
is right and the diff is being pedantic.

**Back to live** leaves verification and returns to the mirror.

## Requirements

Verify needs the device attached and showing the same screen. It re-captures what is in front of
it — it cannot navigate your app. Get the app to the screen the batch came from first.

If the device is gone, verification reports that rather than guessing; the batch stays in
history and can be verified later.
