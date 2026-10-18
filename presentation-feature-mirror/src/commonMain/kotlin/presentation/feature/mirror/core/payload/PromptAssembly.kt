package presentation.feature.mirror.core.payload

import presentation.feature.mirror.core.annotation.TIMES
import presentation.feature.mirror.source.mirror.FrozenFrame
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
 *
 * The dimension pair interpolates [TIMES] directly rather than reusing either module's size
 * formatter: `core/annotation`'s space-pads for a mark's own tag (`48 × 17`) and `core/measure`'s
 * appends a unit suffix (`48×17dp`). Neither shape belongs in this line. The `·` separators are
 * literal for the same reason, matching [hierarchySourceLabel].
 *
 * @param frozen The pinned frame; its annotations are already in badge order.
 * @param nodeIdFor Resolves the hierarchy node an annotation snapped to, or `null` when nothing
 *   matched. Nullable on purpose: "no candidate" is printed as *nothing* — the node-id segment
 *   and its separator are both dropped — rather than as the measure panel's `"View (no match)"`
 *   sentinel, which would read to Claude as a real node name.
 * @return The assembled prompt, `\n`-joined and with no trailing newline.
 */
internal fun buildPrompt(frozen: FrozenFrame, nodeIdFor: (StagedAnnotation) -> String?): String {
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
    return (header + blocks + "Attached: frame.png, $count crops, hierarchy.xml")
        .joinToString("\n")
}
