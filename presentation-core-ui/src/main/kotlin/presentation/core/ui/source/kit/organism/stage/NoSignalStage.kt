package presentation.core.ui.source.kit.organism.stage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.badge.CounterBadge
import presentation.core.ui.source.kit.atom.badge.CounterBadgeState
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.molecule.select.EmulatorSelect

/**
 * Phone-outline geometry, ported verbatim from `StageSurface`'s private empty-state preview so
 * the shipped empty stage and the preview that defined it cannot drift apart.
 */
private val OutlineWidth: Dp = 180.dp
private val OutlineHeight: Dp = 380.dp
private val OutlineRadius: Dp = 28.dp
private val DashOn: Dp = 6.dp
private val DashOff: Dp = 5.dp

/** Matches `StageSurface`'s dot-grid alpha, doubled — the outline reads as the same texture. */
private const val OUTLINE_ALPHA = 0.9f

/** Copy column geometry (comp 02). */
private val CopyColumnMaxWidth: Dp = 420.dp
private val OutlineToCopyGap: Dp = 88.dp
private val FooterDotSize: Dp = 7.dp

/**
 * The empty stage — comp 02's dashed phone outline beside the "how to get a device" copy.
 *
 * Renders **on** `StageSurface`'s dotted ground, not over it: nothing here paints an opaque
 * background, because the dot grid is what tells the user the stage is alive and merely empty.
 * (Contrast `DevicePickerPanel`, which is a modal choice and *does* cover the dots.)
 *
 * The subtitle defaults to the explicit-CTA wording and not comp 02's
 * `Mirroring starts on its own as soon as adb sees one.` — this build ships a
 * `Start mirroring` button, and an empty state promising auto-start beside a button that
 * demands a click is the app lying about its own behaviour. If auto-start is ever implemented,
 * change both together.
 *
 * All copy is a parameter with an English default so the component previews standalone;
 * `presentation-core-ui` has no resource dependency, so real call sites pass
 * `stringResource(Res.string.mirror_*)`.
 *
 * @param avds AVD names for the boot shortcut; empty renders [EmulatorSelect] disabled.
 * @param emptyAvdLabel Shown inside the select when [avds] is empty — platform-specific copy.
 * @param selectedAvd Currently chosen AVD.
 * @param onSelectAvd Called when a different AVD is picked.
 * @param onBoot Called when the user asks to boot [selectedAvd].
 * @param modifier Modifier applied to the root composable.
 * @param noSignalLabel Mono label centred in the phone outline.
 * @param title Headline copy.
 * @param subtitle Supporting copy. Must stay consistent with whether mirroring auto-starts.
 * @param stepUsb Step 1 copy — the physical-device path.
 * @param stepEmulator Step 2 copy — the emulator path.
 * @param bootLabel Label of the boot action.
 * @param footer Machine footer naming the poll command and interval. Not localised prose.
 */
@Composable
public fun NoSignalStage(
    avds: List<String>,
    selectedAvd: String?,
    onSelectAvd: (String) -> Unit,
    onBoot: () -> Unit,
    modifier: Modifier = Modifier,
    noSignalLabel: String = "NO SIGNAL",
    title: String = "Waiting for an Android device",
    subtitle: String = "Plug one in or boot an emulator, then press Start mirroring.",
    stepUsb: String = "Plug in a phone with USB debugging on and accept the prompt.",
    stepEmulator: String = "Or boot an emulator",
    bootLabel: String = "Boot",
    footer: String = "adb devices · polling every 2 s",
    emptyAvdLabel: String = "No AVDs installed",
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OutlineToCopyGap),
    ) {
        PhoneOutline(label = noSignalLabel)
        Column(
            modifier = Modifier.widthIn(max = CopyColumnMaxWidth),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                Text(text = title, style = Theme.typography.titleM, color = Theme.color.ink)
                Text(text = subtitle, style = Theme.typography.body, color = Theme.color.ink2)
            }
            WaitingStep(index = 1, text = stepUsb)
            Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                WaitingStep(index = 2, text = stepEmulator)
                Row(
                    modifier = Modifier.padding(start = Theme.spacing.spacingXL + Theme.spacing.spacingS),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
                ) {
                    EmulatorSelect(
                        avds = avds,
                        selected = selectedAvd,
                        onSelect = onSelectAvd,
                        emptyLabel = emptyAvdLabel,
                    )
                    AppButton(text = bootLabel, onClick = onBoot, size = AppButtonSize.Toolbar, enabled = avds.isNotEmpty())
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            ) {
                Box(
                    modifier = Modifier
                        .size(FooterDotSize)
                        .clip(CircleShape)
                        .background(Theme.color.ink3),
                )
                Text(text = footer, style = Theme.typography.monoMeta, color = Theme.color.ink2)
            }
        }
    }
}

/** One numbered instruction — the badge is a plain neutral counter, as in comp 02. */
@Composable
private fun WaitingStep(index: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
    ) {
        CounterBadge(count = index, state = CounterBadgeState.Empty)
        Text(text = text, style = Theme.typography.body, color = Theme.color.ink2)
    }
}

/** The dashed phone silhouette with its centred mono label. */
@Composable
private fun PhoneOutline(label: String) {
    val outlineColor = Theme.color.line2.copy(alpha = OUTLINE_ALPHA)
    val strokeWidth = Theme.stroke.thin
    Box(
        modifier = Modifier
            .width(OutlineWidth)
            .height(OutlineHeight),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = strokeWidth.toPx() / 2f
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(OutlineRadius.toPx()),
                style = Stroke(
                    width = strokeWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(DashOn.toPx(), DashOff.toPx()),
                    ),
                ),
            )
        }
        Text(text = label, style = Theme.typography.monoMeta, color = Theme.color.ink2)
    }
}

@Preview(showBackground = true)
@Composable
private fun NoSignalStagePreview() {
    AppTheme(mode = ThemeMode.Light) {
        Box(
            modifier = Modifier.fillMaxSize().background(Theme.color.stage),
            contentAlignment = Alignment.Center,
        ) {
            NoSignalStage(
                avds = listOf("Pixel_7_API_34", "Pixel_Fold_API_35"),
                selectedAvd = "Pixel_7_API_34",
                onSelectAvd = {},
                onBoot = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoSignalStageEmptyPreview() {
    AppTheme(mode = ThemeMode.Dark) {
        Box(
            modifier = Modifier.fillMaxSize().background(Theme.color.stage),
            contentAlignment = Alignment.Center,
        ) {
            NoSignalStage(
                avds = emptyList(),
                selectedAvd = null,
                onSelectAvd = {},
                onBoot = {},
            )
        }
    }
}
