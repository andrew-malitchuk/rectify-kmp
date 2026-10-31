package presentation.core.ui.source.kit.molecule.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.ChevronDown

/** Chip geometry. */
private val ChipHeight: Dp = 32.dp
private val DotSize: Dp = 7.dp

/**
 * Connection state of a [DeviceChip], driving the status dot.
 */
public enum class DeviceChipState {
    /** Device is connected and ready to mirror. */
    Ready,

    /** Device is reachable but degraded — shutting down, low battery, throttled. */
    Warn,

    /** Device dropped off and is being recovered. */
    Lost,

    /** No device selected. */
    None,
}

/**
 * The toolbar's device selector — a status dot, the device name, optional machine-readable
 * detail, and a disclosure chevron.
 *
 * [detail] carries whatever the machine reported (`USB`, `Shutdown`, `reconnecting`) and is
 * rendered in mono per the style guide's "numbers and machine output are mono" rule, while the
 * device name stays in the UI face because a person named it.
 *
 * @param name Device name, e.g. `Pixel 8 Pro`.
 * @param state Connection state driving the status dot color.
 * @param onClick Called when the chip is tapped to open the device picker.
 * @param modifier Modifier applied to the root composable.
 * @param detail Optional machine-reported detail shown after the name; `null` hides it.
 */
@Composable
public fun DeviceChip(
    name: String,
    state: DeviceChipState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    val shape = Theme.cornerToken.chip
    Row(
        modifier = modifier
            .height(ChipHeight)
            .clip(shape)
            .background(Theme.color.surface)
            .border(width = Theme.stroke.thin, color = Theme.color.line, shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Theme.spacing.spacingS + Theme.spacing.spacingXS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Box(
            modifier = Modifier
                .size(DotSize)
                .clip(CircleShape)
                .background(state.dotColor()),
        )
        Text(
            text = name,
            style = Theme.typography.bodyStrong,
            color = Theme.color.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (detail != null) {
            Text(
                text = detail,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = ChevronDown,
            contentDescription = null,
            tint = Theme.color.ink3,
            modifier = Modifier.size(Theme.size.iconXS),
        )
    }
}

@Composable
private fun DeviceChipState.dotColor(): Color = when (this) {
    DeviceChipState.Ready -> Theme.color.sentFixed
    DeviceChipState.Warn -> Theme.color.warnStillOff
    DeviceChipState.Lost -> Theme.color.annotate
    DeviceChipState.None -> Theme.color.ink3
}

@Preview(showBackground = true)
@Composable
private fun DeviceChipPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            horizontalAlignment = Alignment.Start,
        ) {
            DeviceChip(name = "Pixel 8 Pro", state = DeviceChipState.Ready, detail = "USB", onClick = {})
            DeviceChip(name = "iPhone 15 Pro", state = DeviceChipState.Warn, detail = "Shutdown", onClick = {})
            DeviceChip(name = "Pixel 8 Pro", state = DeviceChipState.Lost, detail = "reconnecting", onClick = {})
            DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {})
        }
    }
}
