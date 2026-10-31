package presentation.core.ui.source.kit.molecule.row

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.tag.MeasureTag

/** Row geometry. Rows are a fixed height so the guides of adjacent rows meet exactly. */
private val RowHeight: Dp = 26.dp
private val IndentWidth: Dp = 16.dp

/**
 * A node in the dumped view hierarchy.
 *
 * The tree guides are **drawn, not typed**. Box-drawing glyphs only join up when the row height
 * happens to match the font's line box, so any caller that spaced its rows — or any future
 * change to the mono face — would silently break the tree into floating dashes. Drawing them
 * means the guides meet exactly as long as rows are adjacent.
 *
 * Rows must therefore be stacked with **no vertical arrangement spacing**; the fixed
 * [RowHeight] already gives them room.
 *
 * @param nodeId The node, e.g. `TextView#item_price`.
 * @param depth Indent level, starting at 0.
 * @param modifier Modifier applied to the root composable.
 * @param last Whether the guide in **this node's own column** ends here. It is about the guide,
 *   not about sibling order: a node that is the last of its siblings but has children listed
 *   under it is *not* `last`, because its children's guide runs down the same column and the
 *   line has to reach them.
 * @param tag Optional annotation tag, e.g. `#1`, marking a node an annotation matched.
 * @param matched Whether an annotation matched this node. Matched nodes read at full ink; the
 *   rest of the dump recedes to [Theme.color.ink3] so the matches stand out in a long tree.
 * @param onClick Called when the node is picked.
 */
@Composable
public fun HierarchyRow(
    nodeId: String,
    depth: Int,
    modifier: Modifier = Modifier,
    last: Boolean = false,
    tag: String? = null,
    matched: Boolean = false,
    onClick: () -> Unit = {},
) {
    val guideColor = Theme.color.ink3
    val density = LocalDensity.current
    val strokePx = with(density) { Theme.stroke.thin.toPx() }
    val indentPx = with(density) { IndentWidth.toPx() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Theme.spacing.spacingL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(
            modifier = Modifier
                .width(IndentWidth * (depth + 1))
                .fillMaxHeight(),
        ) {
            val midY = size.height / 2f

            // Guides of the ancestors this node still sits under.
            repeat(depth) { level ->
                val x = level * indentPx + indentPx / 2f
                drawLine(
                    color = guideColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokePx,
                )
            }

            // This node's own branch: down to the elbow, and onward only if siblings follow.
            val branchX = depth * indentPx + indentPx / 2f
            drawLine(
                color = guideColor,
                start = Offset(branchX, 0f),
                end = Offset(branchX, if (last) midY else size.height),
                strokeWidth = strokePx,
            )
            drawLine(
                color = guideColor,
                start = Offset(branchX, midY),
                end = Offset(size.width, midY),
                strokeWidth = strokePx,
            )
        }
        Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
        Text(
            text = nodeId,
            style = Theme.typography.monoData,
            color = if (matched) Theme.color.ink else Theme.color.ink3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (tag != null) {
            Spacer(modifier = Modifier.weight(1f))
            MeasureTag(text = tag)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HierarchyRowPreviewLight() {
    AppTheme(mode = ThemeMode.Light) { HierarchyRowPreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun HierarchyRowPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) { HierarchyRowPreviewContent() }
}

@Composable
private fun HierarchyRowPreviewContent() {
    // No arrangement spacing — see the KDoc.
    Column(
        modifier = Modifier
            .background(Theme.color.surface)
            .padding(vertical = Theme.spacing.spacingS),
        verticalArrangement = Arrangement.Top,
    ) {
        // Not `last`: its children are listed below and share this column's guide.
        HierarchyRow(nodeId = "CartItemView[1]", depth = 0)
        HierarchyRow(nodeId = "TextView#item_price", depth = 1, tag = "#1", matched = true)
        HierarchyRow(nodeId = "ImageView#item_icon", depth = 1)
        HierarchyRow(nodeId = "QtyStepper#qty", depth = 1, last = true, tag = "#2", matched = true)
    }
}
