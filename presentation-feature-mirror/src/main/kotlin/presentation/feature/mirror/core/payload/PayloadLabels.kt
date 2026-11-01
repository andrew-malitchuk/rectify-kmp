package presentation.feature.mirror.core.payload

import presentation.feature.mirror.source.mirror.HierarchyStatus

/**
 * The payload preview's hierarchy-source line.
 *
 * Machine text, never localised — these strings are copied into bug reports and must read the
 * same on every machine, which is why the middle dot (U+00B7) and the ellipsis (U+2026) are
 * literal characters here rather than ASCII stand-ins.
 *
 * A closed four-way `when` over the shipped [HierarchyStatus] values, deliberately with no
 * `else`: a fifth status must fail to compile here rather than fall through to "no hierarchy".
 * [HierarchyStatus.Absent] and [HierarchyStatus.Unavailable] are distinct domain states but
 * share one label — the reader only needs to know there is nothing to measure against; no new
 * status is invented to tell them apart, and nothing here can start a dump.
 *
 * @param status Verdict reduced for the pinned frame.
 * @param nodeCount Size of the captured snapshot. Read only when [status] is
 *   [HierarchyStatus.Ready] — including `0`, which is a usable (empty) snapshot and still
 *   reports its count.
 */
internal fun hierarchySourceLabel(status: HierarchyStatus, nodeCount: Int): String =
    when (status) {
        HierarchyStatus.Ready -> "uiautomator · $nodeCount nodes"
        HierarchyStatus.Dumping -> "dumping…"
        HierarchyStatus.Absent, HierarchyStatus.Unavailable -> "no hierarchy"
    }
