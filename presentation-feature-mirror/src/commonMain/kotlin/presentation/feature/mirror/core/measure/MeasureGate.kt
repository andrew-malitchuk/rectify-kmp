package presentation.feature.mirror.core.measure

/**
 * Whether the Alt-hold should be measuring right now (FR-36 / FR-44 / AC-71).
 *
 * Four independent gates, all of which must hold:
 *
 * - **[altHeld]** — the modifier is down. Read uniformly from `isAltPressed` on every desktop
 *   host, with no macOS branch (step-2 gate ruling 4): Option *is* Alt as far as Compose is
 *   concerned.
 * - **[dragInProgress]** — a drag beats the modifier. FR-44 as reworded: Alt is **ignored**
 *   until the drag ends, rather than cancelling it. Without this, pressing Option mid-drag
 *   would start measuring the annotation under the cursor while the user is still drawing a
 *   different one.
 * - **[frozen]** — there is a pinned frame. Measuring a live stream would measure a bitmap that
 *   has already been replaced, and there is no hierarchy dump for a live frame at all.
 * - **[hoverIndex]** — the pointer is over an annotation. Measure mode is per-annotation; Alt
 *   held over empty frame measures nothing.
 *
 * Pure and parameter-only on purpose: this is the one decision in the Alt layer that can be
 * got wrong invisibly, and the repo has no Compose UI-test harness (`.claude/rules/testing.md`),
 * so a decision left inline in the composable is a decision nothing can check.
 *
 * @param altHeld Whether Alt/Option is currently down.
 * @param dragInProgress Whether a drag gesture is in flight (`dragStart != null`).
 * @param frozen Whether a frame is pinned (`frozen != null`).
 * @param hoverIndex Badge number of the annotation under the pointer, or `null`.
 * @return True when [hoverIndex] should be measured.
 */
internal fun shouldMeasure(
    altHeld: Boolean,
    dragInProgress: Boolean,
    frozen: Boolean,
    hoverIndex: Int?,
): Boolean = altHeld && !dragInProgress && frozen && hoverIndex != null

/**
 * The target [shouldMeasure] admits, as the value the dispatch edge is computed from.
 *
 * @param altHeld Whether Alt/Option is currently down.
 * @param dragInProgress Whether a drag gesture is in flight.
 * @param frozen Whether a frame is pinned.
 * @param hoverIndex Badge number of the annotation under the pointer, or `null`.
 * @return [hoverIndex] when measuring is admissible, `null` otherwise.
 */
internal fun measureTarget(
    altHeld: Boolean,
    dragInProgress: Boolean,
    frozen: Boolean,
    hoverIndex: Int?,
): Int? = hoverIndex.takeIf {
    shouldMeasure(
        altHeld = altHeld,
        dragInProgress = dragInProgress,
        frozen = frozen,
        hoverIndex = hoverIndex,
    )
}

/** What the Alt layer must dispatch on one change of its measure target. */
internal enum class MeasureDispatch {
    /** `StartMeasure(desired)` — measuring began, or moved to another annotation. */
    Start,

    /** `EndMeasure` — measuring stopped. */
    End,

    /** Nothing changed; no reducer pass. */
    None,
}

/**
 * The dispatch **edge**: what to send given what was last sent (AC-71 / TAC-24).
 *
 * This is why hover and `altHeld` live in `remember` rather than in Orbit. A pointer produces an
 * event per frame of movement; dispatching on the raw signal would be one reducer pass per frame
 * for the whole hold. Comparing against the last dispatched target collapses that to one pass
 * per *change*: hovering three annotations with Alt held is three [Start]s and one [End], not
 * three hundred.
 *
 * Moving between annotations is a [Start], not an End-then-Start: `StartMeasure` replaces the
 * readout wholesale, and an intervening `EndMeasure` would write the ⌥ note line for the
 * annotation the user merely passed over.
 *
 * All three recovery paths for a missed key-up (KeyUp, focus loss, the next pointer-down) work by
 * clearing `altHeld`, which makes `desired` null, which makes this return [End] — so a release
 * the key path misses still ends the measurement exactly once.
 *
 * @param active Target of the last dispatched `StartMeasure`, or `null` if none is live.
 * @param desired Target [measureTarget] admits now.
 * @return The single intent to dispatch, if any.
 */
internal fun measureDispatch(active: Int?, desired: Int?): MeasureDispatch = when {
    desired == active -> MeasureDispatch.None
    desired != null -> MeasureDispatch.Start
    else -> MeasureDispatch.End
}
