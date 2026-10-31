package presentation.core.ui.source.kit.molecule.row

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.GlyphBadge

/**
 * An annotation in the sidebar list — its index, its note, where it sits, and anything the
 * measure tool found about it.
 *
 * An annotation with no note yet is not an error: [comment] is `null` and the row says so in
 * muted copy rather than collapsing, because the mark still exists on the frame and the user
 * needs to be able to get back to it.
 *
 * @param index Mark index, matching the badge on the frame.
 * @param meta Position line, e.g. `218×77 @ 1035,862`.
 * @param onClick Called when the row is opened.
 * @param modifier Modifier applied to the root composable.
 * @param comment The note; `null` renders the empty state.
 * @param finding Optional measured finding, rendered in the measure blue.
 * @param selected Whether this row is the active annotation.
 * @param emptyLabel Copy shown when [comment] is `null`.
 */
@Composable
public fun AnnotationRow(
    index: Int,
    meta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    comment: String? = null,
    finding: String? = null,
    selected: Boolean = false,
    emptyLabel: String = "No comment yet",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Theme.cornerToken.nested)
            .background(if (selected) Theme.color.seg else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(Theme.spacing.spacingS + Theme.spacing.spacingXS),
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
    ) {
        GlyphBadge(text = index.toString(), containerColor = Theme.color.annotate)
        Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS)) {
            Text(
                text = comment ?: emptyLabel,
                style = Theme.typography.body,
                color = if (comment != null) Theme.color.ink else Theme.color.ink3,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = meta,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
            )
            if (finding != null) {
                Text(
                    text = finding,
                    style = Theme.typography.monoMeta,
                    color = Theme.color.measure,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnotationRowPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.surface)
                .padding(Theme.spacing.spacingS),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AnnotationRow(
                index = 1,
                comment = "Price sits 6px below the title — align baselines",
                meta = "218×77 @ 1035,862",
                finding = "⌥ top Δ6dp vs item_title · expected 0",
                selected = true,
                onClick = {},
            )
            AnnotationRow(index = 2, meta = "436×154 @ 354,1003", onClick = {})
        }
    }
}
