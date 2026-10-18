package presentation.core.ui.source.kit.organism.annotation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.keycap.KeyCap

/** Card geometry. */
private val CardMaxWidth: Dp = 360.dp

/** Opacity of the wash behind a finding, over the card's own surface. */
private const val FINDING_WASH_ALPHA = 0.10f

/**
 * One measured dimension — a label and its value.
 *
 * @param label What was measured, e.g. `Top`.
 * @param value The measurement, already formatted with its unit, e.g. `9dp`.
 */
@Immutable
public data class MeasureSpec(
    val label: String,
    val value: String,
)

/**
 * The measure card — the readout for the node currently under the measure tool.
 *
 * The node id and every value are mono, because they are machine output; only the row labels
 * and the title are in the UI face. The optional [finding] is the one place red appears on this
 * card: it reports a misalignment, which is an annotation-grade problem rather than a
 * measurement.
 *
 * Unlike [CommentPopover] this is a panel rather than a tailed bubble: it is self-contained,
 * carries no pointer at anything, and follows the theme normally. Where it sits is the caller's
 * choice — `MirrorAnnotationStage` floats it over the frozen frame at `TopStart`, which is how
 * measure mode presents it; a sidebar is equally valid.
 *
 * @param nodeId Identifier of the measured node, e.g. `TextView#item_price`.
 * @param specs Measured dimensions, rendered in order.
 * @param modifier Modifier applied to the root composable.
 * @param title Card title.
 * @param index Optional measurement index shown top-right, e.g. `1` renders as `#1`.
 * @param finding Optional misalignment note shown in a red block at the bottom.
 * @param footer Optional sentence below the finding, inside the card's own border — what becomes
 *   of these numbers, or why there are none to show.
 */
@Composable
public fun MeasureCard(
    nodeId: String,
    specs: List<MeasureSpec>,
    modifier: Modifier = Modifier,
    title: String = "Measuring",
    index: Int? = null,
    finding: String? = null,
    footer: String? = null,
) {
    Column(
        modifier = modifier
            .widthIn(max = CardMaxWidth)
            .clip(Theme.cornerToken.card)
            .background(Theme.color.surface)
            .border(Theme.stroke.thin, Theme.color.line, Theme.cornerToken.card)
            .padding(Theme.spacing.spacingL),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            // The tool is entered with ⌥, so the card names the key that opened it.
            KeyCap(text = "⌥")
            Text(
                text = title,
                style = Theme.typography.titleS,
                color = Theme.color.ink,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (index != null) {
                Text(
                    text = "#$index",
                    style = Theme.typography.monoMeta,
                    color = Theme.color.ink3,
                    maxLines = 1,
                )
            }
        }

        Text(
            text = nodeId,
            style = Theme.typography.monoData,
            color = Theme.color.measure,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.padding(top = Theme.spacing.spacingXS))

        specs.forEach { spec ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = spec.label,
                    style = Theme.typography.body,
                    color = Theme.color.ink,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = spec.value,
                    style = Theme.typography.monoData,
                    color = Theme.color.ink,
                    maxLines = 1,
                )
            }
        }

        if (finding != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Theme.cornerToken.nested)
                    .background(Theme.color.annotate.copy(alpha = FINDING_WASH_ALPHA))
                    .padding(Theme.spacing.spacingS),
            ) {
                Text(
                    text = finding,
                    style = Theme.typography.monoData,
                    color = Theme.color.annotate,
                )
            }
        }

        // Below the finding and inside the border, in the card's own padded column: the sentence
        // is about the card's contents, so it must not look like a caption floating under it.
        if (footer != null) {
            Text(
                text = footer,
                style = Theme.typography.body,
                color = Theme.color.ink3,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasureCardPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
        ) {
            MeasureCard(
                nodeId = "TextView#item_price",
                index = 1,
                specs = listOf(
                    MeasureSpec("Size", "48×17dp"),
                    // 15, not the 9 this preview shipped with. The design's own measure() over
                    // its own ELS fixture gives 15 for item_price (nearest node above is
                    // divider_1, bottom edge 183, so 198 - 183 = 15); no node in the fixture has
                    // a bottom edge at 189, so 9 was never run through the algorithm. Corrected
                    // per the step-2 gate addendum, "the design's real measure() recovered".
                    MeasureSpec("Top", "15dp"),
                    MeasureSpec("Right", "20dp"),
                    MeasureSpec("Bottom", "49dp"),
                    MeasureSpec("Left", "62dp"),
                ),
                finding = "top Δ6dp vs item_title · expected 0",
                footer = "Numbers attach to the annotation and go into the prompt.",
            )
        }
    }
}
