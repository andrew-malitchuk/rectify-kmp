package presentation.feature.mirror.core.payload

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus

/**
 * Whether the pinned frame has anything to preview as a payload.
 *
 * Named and lifted out of the composable so AC-77 — "zero staged annotations renders only the
 * empty-state copy" — can be asserted by a unit test. The condition is two clauses and would be
 * trivial to inline, but inlined it sat inside `MirrorPayloadTab`'s body where this project has
 * no way to reach it: there is no Compose UI-test harness (see `.claude/rules/testing.md`), so a
 * gate expressed only as an `if` in a `@Composable` is provable by nothing but eye. A test that
 * re-wrote the same boolean would have asserted against itself and proved nothing either.
 *
 * Both clauses are load-bearing and neither implies the other. `frozen == null` is the live
 * stream — nothing is pinned, so there is no frame to describe. A non-null frame with an empty
 * annotation list is the moment after `ClearStaged`: the frame is still pinned, but a payload
 * with no annotations would carry a prompt asking to fix nothing, crops of nothing, and a
 * `0 crops` footer.
 *
 * Carries a `contract` so a `true` result smart-casts [frozen] to non-null at the call site, the
 * same way `isNullOrEmpty` does in the stdlib. Without it, lifting the condition out of the
 * composable would cost every later `frozen.` access a `!!` — trading one untestable `if` for a
 * scattering of non-null assertions is not a trade worth making.
 *
 * @param frozen The pinned frame, or `null` while the stream is live.
 * @return `true` when at least one annotation is staged on a pinned frame.
 */
@OptIn(ExperimentalContracts::class)
internal fun hasPayload(frozen: FrozenFrame?): Boolean {
    contract { returns(true) implies (frozen != null) }
    return frozen != null && frozen.annotations.isNotEmpty()
}

/** What the Payload tab's Hierarchy block puts under its label. */
internal enum class HierarchyOutlineDisplay {
    /** The dumped tree. */
    Rows,

    /** One line of copy saying there is no hierarchy to show. */
    NoHierarchyLine,

    /** Label and readout only — the readout already says which state this is. */
    Nothing,
}

/**
 * Whether the Hierarchy block draws the tree, the no-hierarchy line, or nothing (AC-80).
 *
 * **The status is tested before the row count, and that order is the whole point.** A held
 * snapshot does not mean a usable one: `MirrorViewModel.dumpHierarchyFor` publishes the dump
 * before it reduces the verdict, so a frame-size mismatch — the measured 9.76 % `uiautomator`
 * display-override gap documented on [domain.core.source.model.device.HierarchySnapshotModel],
 * a real device condition — leaves several hundred rows projected from a snapshot whose status
 * is [HierarchyStatus.Unavailable]. Rows first printed that whole unbadged tree *underneath* a
 * readout saying "no hierarchy": a verdict contradicted by the thing rendered below it, and
 * zero-rows is exactly what AC-80 asks for.
 *
 * Lifted out of the composable for the same reason as [hasPayload]: this project has no Compose
 * UI-test harness (`.claude/rules/testing.md`), so branch order expressed only as a `when` in a
 * `@Composable` is provable by nothing but eye — and the AC-80 preview pair pins an empty
 * snapshot, so it steers around the only combination that was wrong.
 *
 * A row count rather than the list: nothing here reads a row, and the count is all the decision
 * turns on.
 *
 * @param status Hierarchy verdict reduced for the pinned frame.
 * @param rowCount Rows projected from the held snapshot; `0` when nothing was dumped.
 * @return Which of the three bodies the block should compose.
 */
internal fun hierarchyOutlineDisplay(
    status: HierarchyStatus,
    rowCount: Int,
): HierarchyOutlineDisplay = when {
    status == HierarchyStatus.Absent || status == HierarchyStatus.Unavailable ->
        HierarchyOutlineDisplay.NoHierarchyLine
    rowCount > 0 -> HierarchyOutlineDisplay.Rows
    else -> HierarchyOutlineDisplay.Nothing
}
