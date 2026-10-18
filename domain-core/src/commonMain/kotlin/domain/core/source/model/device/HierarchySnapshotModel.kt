package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * One capture of a device's view hierarchy, geometry and density together.
 *
 * The frame size is the ROOT NODE's own bounds, not `wm size`: a device with a display
 * override reports the override resolution in the dump (measured 984x2132) while `wm size`'s
 * physical line reports the panel (1080x2400) — a 9.76% gap. Taking the root keeps the frame
 * in the same coordinate space as every node inside it.
 *
 * @property nodes Nodes in depth-first document order; empty when nothing could be read.
 * @property frameWidthPx Root node width in px, `0` when there is no root.
 * @property frameHeightPx Root node height in px, `0` when there is no root.
 * @property densityDpi Device density, or `0` when it is unknown. `0` is a SENTINEL, not a
 *   measurement: a consumer must fall back to reporting raw px rather than converting with a
 *   guessed density, since a wrong density scales every figure it touches.
 * @property rawDump The `uiautomator dump` stdout this snapshot was parsed from, VERBATIM —
 *   trailer and all — or `""` when nothing was read. [nodes] keeps only the attributes measure
 *   mode needs, so re-synthesizing XML from them would ship a document the device never
 *   emitted; this is the only non-lossy source for a consumer that must forward the dump
 *   itself. Defaulted so every construction site that does not have the raw text is unaffected.
 */
public data class HierarchySnapshotModel(
    val nodes: List<HierarchyNodeModel>,
    val frameWidthPx: Int,
    val frameHeightPx: Int,
    val densityDpi: Int,
    val rawDump: String = "",
) : Model {

    public companion object {

        /** Nothing was read — an unreadable, empty or node-less dump. */
        public val Empty: HierarchySnapshotModel = HierarchySnapshotModel(emptyList(), 0, 0, 0)
    }
}
