package presentation.core.ui.source.kit.molecule.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import presentation.core.ui.source.kit.atom.indicator.RadioMark

/** Card geometry (comp 01 / 03). */
private val CardHeight: Dp = 80.dp
private val ToolDotSize: Dp = 7.dp

/** Dim applied to a platform that exists but cannot be chosen yet (iOS, this release). */
private const val DISABLED_ALPHA = 0.5f

/**
 * Whether the tool backing a platform was found on `PATH`, and what to say about it.
 *
 * The card never hides a platform whose tool is missing: knowing `adb` is not installed is the
 * answer to "why is nothing here", so the card reports the absence instead of disappearing.
 */
public enum class PlatformToolState {
    /** Tool found; [PlatformToolCard.detail] carries its version and location. */
    Found,

    /** Tool not on `PATH`. */
    Missing,
}

/**
 * A platform choice in the picker's `PLATFORM` section — `Android` / `iOS` (comp 01).
 *
 * Two lines and a radio: the platform name, and underneath it the *evidence* that the platform
 * is usable — the detected tool, its version and where it came from. That second line is raw
 * machine output (`adb 35.0.1 · /opt/homebrew/bin`), so it is mono, composed by the caller and
 * deliberately not localised.
 *
 * The whole card is the hit target; [RadioMark] is decoration, and `Role.RadioButton` plus the
 * selected state live on the card. [enabled] `false` drops the click handler entirely rather
 * than only dimming, so an unavailable platform is announced as disabled (A11Y-6) — that is the
 * shape iOS ships in for this release.
 *
 * @param title Platform name.
 * @param detail Machine-produced tool line, rendered mono. Never localised.
 * @param toolState Whether the tool was found; drives the dot colour.
 * @param selected Whether this is the active platform.
 * @param onClick Called when the card is picked.
 * @param modifier Modifier applied to the root composable.
 * @param enabled Whether the platform can be picked.
 */
@Composable
public fun PlatformToolCard(
    title: String,
    detail: String,
    toolState: PlatformToolState,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentAlpha = if (enabled) 1f else DISABLED_ALPHA
    // Selected reads as an ink outline rather than a fill: the card already carries a radio,
    // and filling it too would make the unselected sibling look switched off.
    val borderColor = if (selected) Theme.color.ink else Theme.color.line
    val borderWidth = if (selected) Theme.stroke.regular else Theme.stroke.thin
    val shape = Theme.cornerToken.card

    Column(
        modifier = modifier
            .height(CardHeight)
            .clip(shape)
            .background(Theme.color.surface)
            .border(width = borderWidth, color = borderColor.copy(alpha = contentAlpha), shape = shape)
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
            .padding(horizontal = Theme.spacing.spacingL, vertical = Theme.spacing.spacingS + Theme.spacing.spacingXS),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = Theme.typography.titleS,
                color = Theme.color.ink.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            RadioMark(selected = selected)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            Box(
                modifier = Modifier
                    .size(ToolDotSize)
                    .clip(CircleShape)
                    .background(toolState.dotColor().copy(alpha = contentAlpha)),
            )
            Text(
                text = detail,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink2.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlatformToolState.dotColor(): Color = when (this) {
    PlatformToolState.Found -> Theme.color.sentFixed
    PlatformToolState.Missing -> Theme.color.warnStillOff
}

@Preview(showBackground = true)
@Composable
private fun PlatformToolCardPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
        ) {
            PlatformToolCard(
                title = "Android",
                detail = "adb 35.0.1 · /opt/homebrew/bin",
                toolState = PlatformToolState.Found,
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            PlatformToolCard(
                title = "iOS",
                detail = "idb 1.1.8 · Xcode 16.0",
                toolState = PlatformToolState.Found,
                selected = false,
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
