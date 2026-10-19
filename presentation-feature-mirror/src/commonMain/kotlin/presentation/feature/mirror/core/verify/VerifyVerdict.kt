package presentation.feature.mirror.core.verify

import presentation.core.ui.source.kit.molecule.row.VerificationOutcome

/**
 * The verdict for one annotation — the AND-rule, in exactly one expression and nowhere else.
 *
 * `Fixed` requires **change evidence**, not merely the absence of a complaint:
 * the annotation still snaps to a node, the AFTER measurement reports no finding, **and** the
 * region's pixels actually moved. The literal AC-24 wording ("no material difference →
 * resolved") would report an agent that did nothing at all as having fixed the bug, which is
 * the one failure mode that destroys trust in the whole loop.
 *
 * The [MIN_REGION_PIXELS] branch short-circuits **only the pixel conjunct**, and only for a
 * region that was genuinely read and genuinely tiny: under that size the ratio is noise in both
 * directions, so the hierarchy half decides alone. A tiny region that still has an
 * [afterFinding] is still `StillOff`.
 *
 * `regionPx == 0` is NOT that case and deliberately falls through to `StillOff`. Zero is the
 * "no region could be read" sentinel — a degenerate or fully-off-frame stored rect — so
 * waiving the pixel conjunct for it would hand out `Fixed` on the hierarchy half alone with
 * provably zero pixel evidence, which is the exact failure the change-evidence rule above
 * exists to prevent.
 *
 * Two accepted ceilings, both cheaper than their opposite (a false `Still off` costs one click
 * of "Mark all as verified"; a false `Fixed` costs the user's trust):
 * - a NEW, unrelated finding in AFTER also reads `Still off` — it is a regression and deserves
 *   the flag;
 * - a sub-[MIN_REGION_PIXELS] region whose finding vanished because a neighbour moved reads
 *   `Fixed` with zero changed pixels.
 *
 * @param matched Whether the AFTER readout snapped to a real node — `nodeId != NO_MATCH_NODE_ID`.
 *   An unmatched annotation has no layout oracle at all, so it can never be `Fixed`.
 * @param afterFinding `MeasureReadout.finding` of the AFTER measurement — a message `String?`,
 *   because `measure()` already snaps and already calls `findingOf`. There is no second oracle.
 * @param changedRatio Result of [changedRatio] for the annotation's region.
 * @param regionPx Area of the diffed region in pixels, `0` when no region could be read.
 */
internal fun verdictOf(
    matched: Boolean,
    afterFinding: String?,
    changedRatio: Float,
    regionPx: Int,
): VerificationOutcome =
    if (
        matched && afterFinding == null &&
        (regionPx in 1 until MIN_REGION_PIXELS || changedRatio > REGION_CHANGE_RATIO)
    ) {
        VerificationOutcome.Fixed
    } else {
        VerificationOutcome.StillOff
    }
