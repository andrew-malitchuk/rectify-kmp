package presentation.feature.mirror.core.payload

import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.PlatformModel
import presentation.feature.mirror.core.annotation.TIMES
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.StagedAnnotation

/** Indent of an annotation's comment line, three spaces as the design's `pl[]` block writes it. */
private const val COMMENT_INDENT = "   "

/** Stand-in printed in place of a comment the user never typed. */
private const val NO_COMMENT = "no comment"

/**
 * The generated prompt for the pinned frame — headline, device line, one block per annotation,
 * attachment trailer.
 *
 * Machine text, never localised: it is pasted into Claude, so it must read identically on every
 * machine (same reason as [hierarchySourceLabel]). Three deliberate divergences from the
 * design's `pl[]` assembly, each load-bearing rather than an omission:
 *
 * 1. **The device line is real.** The design hardcodes `"Android 15 · 1344×2992"`; here it is
 *    [FrozenFrame.deviceLabel] and [FrozenFrame.deviceSize], both stamped at freeze, so the
 *    prompt describes the frame that was actually captured. The label is **optional**: an
 *    emulator row carries no detail (`EmulatorModel.toRow` never sets `MirrorRow.detail`, which
 *    defaults to `""`), so [FrozenFrame.deviceLabel] is blank on a first-class path. It is then
 *    dropped together with its separator — the same `listOfNotNull` idiom the annotation head
 *    line uses for an unmatched node id — leaving the bare resolution rather than a
 *    leading-space dangling-dot `" · 1080×2400"`. No placeholder: a blank label is nothing
 *    to say, and inventing a device name would be divergence 3's mistake.
 * 2. **No `measured:` branch.** `core/measure`'s `noteLine` already folds a measurement into the
 *    user's comment as a `⌥`-prefixed line, so the comment is emitted **verbatim** — appending a
 *    second measurement segment here would duplicate it.
 * 3. **No screen clause.** The design's `" on the Cart screen."` names its own demo app; this
 *    codebase has no screen-name source, and inventing one would put fabricated data in every
 *    prompt.
 * 4. **The trailer lists what was actually attached** (Stage 5 `AF-4`). The design's
 *    unconditional `"Attached: frame.png, N crops, hierarchy.xml"` was false on three reachable
 *    paths: a hierarchy-less send still promised a hierarchy, no file named `hierarchy.xml` has
 *    ever existed on any path (the XML rides inline in `AnnotationEnvelope.hierarchyXml`, and
 *    `EnvelopeAssembly` emits no such image), and `N` was the *staged* count even when some
 *    crop failed to encode and was never attached. See [attachmentTrailer].
 *
 * The dimension pair interpolates [TIMES] directly rather than reusing either module's size
 * formatter: `core/annotation`'s space-pads for a mark's own tag (`48 × 17`) and `core/measure`'s
 * appends a unit suffix (`48×17dp`). Neither shape belongs in this line. The `·` separators are
 * literal for the same reason, matching [hierarchySourceLabel].
 *
 * @param frozen The pinned frame; its annotations are already in badge order.
 * @param cropCount Crops **actually attached**, not staged — on the send path the number of
 *   annotations whose `cropPath` is non-null. Defaults to the staged count, which is what the
 *   Payload tab's preview must assume: nothing has been encoded yet when it renders.
 * @param hierarchyAttached Whether the view hierarchy XML really ships with this payload — i.e.
 *   [attachedHierarchyXml] is non-empty. Defaults to `false`, the only safe default: a prompt
 *   that stays silent about a hierarchy is never wrong, one that promises an absent hierarchy
 *   always is.
 * @param nodeIdFor Resolves the hierarchy node an annotation snapped to, or `null` when nothing
 *   matched. Nullable on purpose: "no candidate" is printed as *nothing* — the node-id segment
 *   and its separator are both dropped — rather than as the measure panel's `"View (no match)"`
 *   sentinel, which would read to Claude as a real node name.
 * @return The assembled prompt, `\n`-joined and with no trailing newline.
 */
internal fun buildPrompt(
    frozen: FrozenFrame,
    cropCount: Int = frozen.annotations.size,
    hierarchyAttached: Boolean = false,
    nodeIdFor: (StagedAnnotation) -> String?,
): String {
    val count = frozen.annotations.size
    val header = listOf(
        "Fix $count UI ${if (count == 1) "issue" else "issues"}.",
        listOfNotNull(
            frozen.deviceLabel.ifBlank { null },
            "${frozen.deviceSize.width}$TIMES${frozen.deviceSize.height}",
        ).joinToString(" · "),
        "",
    )
    val blocks = frozen.annotations.flatMap { annotation ->
        val head = listOfNotNull(
            nodeIdFor(annotation),
            "${annotation.rect.width}$TIMES${annotation.rect.height}",
        ).joinToString(" · ")
        listOf(
            "#${annotation.index} $head",
            "$COMMENT_INDENT\"${annotation.comment.ifEmpty { NO_COMMENT }}\"",
            "",
        )
    }
    return (header + blocks + attachmentTrailer(cropCount, hierarchyAttached)).joinToString("\n")
}

/**
 * The prompt's last line — an inventory of what the agent can actually reach, never a fixed
 * manifest (Stage 5 `AF-4`).
 *
 * A segment is printed only when the thing it names exists: no crops staged (or every encode
 * failed) drops the crop segment entirely rather than printing `0 crops`, and the hierarchy
 * segment appears only for a payload that really carries the XML. The hierarchy is named as
 * *inline* and explicitly not a file, because there is no `hierarchy.xml` to open — it arrives
 * in the envelope's `hierarchyXml` field, which the MCP server renders as the tool result's
 * `hierarchy.xml` **JSON key**. That near-collision is exactly what sent the agent file-hunting.
 *
 * Singular/plural follows the headline's own `if (count == 1)` precedent — `1 crop`, never the
 * old `1 crops`.
 */
private fun attachmentTrailer(cropCount: Int, hierarchyAttached: Boolean): String =
    listOfNotNull(
        FRAME_FILE_NAME,
        when {
            cropCount <= 0 -> null
            cropCount == 1 -> "1 crop"
            else -> "$cropCount crops"
        },
        "inline view hierarchy XML (no file)".takeIf { hierarchyAttached },
    ).joinToString(separator = ", ", prefix = "Attached: ")

/**
 * The hierarchy XML this payload would really ship, or `""` when it ships none — the single
 * source of truth behind both the envelope's `hierarchyXml` field and the prompt's hierarchy
 * segment.
 *
 * Shared rather than inlined at each call site on purpose: `AF-4` was two strings disagreeing
 * with one envelope, so the Payload tab's preview and `EnvelopeAssembly` now ask this one
 * function (and the toast reads the published envelope's own flag, which comes from here).
 *
 * Android-only (`AS-29`), and only for a verdict that says a real dump is held — an Empty
 * snapshot carries `rawDump == ""`, so a stale [HierarchyStatus.Ready] over it degrades
 * for free rather than through a second branch.
 */
internal fun attachedHierarchyXml(
    platform: PlatformModel,
    status: HierarchyStatus,
    hierarchy: HierarchySnapshotModel,
): String =
    if (platform == PlatformModel.Android && status == HierarchyStatus.Ready) {
        hierarchy.rawDump
    } else {
        ""
    }

/**
 * Bare file name of the attached full frame — the prompt's trailer names it, the envelope's
 * `framePath` points at it and `EnvelopeAssembly` emits the `HandoffImage` under it. One
 * constant, because `AF-4` is what happens when the prompt's idea of an attachment and the
 * envelope's drift apart.
 */
internal const val FRAME_FILE_NAME: String = "frame.png"
