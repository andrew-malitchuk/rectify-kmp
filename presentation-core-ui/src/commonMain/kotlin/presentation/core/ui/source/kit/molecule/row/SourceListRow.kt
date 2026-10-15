package presentation.core.ui.source.kit.molecule.row

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.indicator.RadioMark

/** Row geometry. */
private val RowHeight: Dp = 48.dp

/** Dim applied to a row that cannot be picked (no adb transport, emulator mid-boot). */
private const val DISABLED_ALPHA = 0.38f

/**
 * A device row in the source list — the picker that chooses what Rectify mirrors.
 *
 * The trailing area is a slot rather than a fixed set of fields, because what belongs there
 * depends entirely on the device's state: a transport for a connected handset, a run state for
 * a warm emulator, a boot action for a cold one.
 *
 * @param name Device name.
 * @param selected Whether this is the active source.
 * @param onClick Called when the row is picked.
 * @param modifier Modifier applied to the root composable.
 * @param serial Optional device serial, rendered mono because the device reported it.
 * @param enabled Whether the row can be picked. `false` drops the click handler entirely and
 *   marks the row disabled for accessibility services, so it is *announced* as unavailable
 *   rather than merely dimmed — a dim alone is invisible to a screen reader (A11Y-6).
 * @param trailing Trailing slot — transport, run state, or an action. Not dimmed with the row:
 *   a cold emulator's `Boot` button stays live while the row itself is unpickable.
 */
@Composable
public fun SourceListRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    serial: String? = null,
    enabled: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val contentAlpha = if (enabled) 1f else DISABLED_ALPHA
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .background(if (selected) Theme.color.hover else Color.Transparent)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.RadioButton,
                        onClick = onClick,
                    )
                } else {
                    Modifier.semantics { disabled() }
                },
            )
            .padding(horizontal = Theme.spacing.spacingL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
    ) {
        RadioMark(
            selected = selected,
            modifier = Modifier.alpha(contentAlpha),
        )
        Text(
            text = name,
            style = Theme.typography.bodyStrong,
            color = Theme.color.ink.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (serial != null) {
            Text(
                text = serial,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        trailing()
    }
}

/**
 * The group header above a run of [SourceListRow]s — `Physical device`, `Emulator`.
 *
 * @param text Group name.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun SourceListHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = Theme.typography.caption,
        color = Theme.color.ink3,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Theme.spacing.spacingL, vertical = Theme.spacing.spacingS),
    )
}

@Preview(showBackground = true)
@Composable
private fun SourceListRowPreview() {
    AppTheme {
        Column(modifier = Modifier.background(Theme.color.surface)) {
            SourceListHeader(text = "Physical device")
            SourceListRow(
                name = "Pixel 8 Pro",
                serial = "37281FDH…",
                selected = true,
                onClick = {},
            ) {
                Text(
                    text = "USB",
                    style = Theme.typography.monoMeta,
                    color = Theme.color.ink2,
                )
            }
            SourceListRow(name = "Pixel_7_API_34", selected = false, onClick = {}) {
                StatusDotLabel(label = "Running")
            }
            SourceListRow(
                name = "Pixel_Fold_API_35",
                serial = "shut down",
                selected = false,
                onClick = {},
                enabled = false,
            ) {
                AppButton(
                    text = "Boot",
                    onClick = {},
                    variant = AppButtonVariant.Secondary,
                    size = AppButtonSize.ListAction,
                )
            }
        }
    }
}

/** Small helper used by the preview; real call sites pass whatever their state needs. */
@Composable
private fun StatusDotLabel(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS + 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(Theme.color.sentFixed, androidx.compose.foundation.shape.CircleShape),
        )
        Text(text = label, style = Theme.typography.secondary, color = Theme.color.ink2)
    }
}
