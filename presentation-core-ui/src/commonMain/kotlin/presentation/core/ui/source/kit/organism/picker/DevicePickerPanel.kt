package presentation.core.ui.source.kit.organism.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.molecule.card.PlatformToolCard
import presentation.core.ui.source.kit.molecule.card.PlatformToolState
import presentation.core.ui.source.kit.molecule.row.SourceListHeader
import presentation.core.ui.source.kit.molecule.row.SourceListRow

/** Panel geometry (comps 01 / 03). */
private val ContentMaxWidth: Dp = 648.dp
private val RunningDotSize: Dp = 7.dp

/**
 * A platform the picker can offer — `Android`, `iOS`.
 *
 * @param id Stable identifier reported back through `onPlatformClick`.
 * @param title Platform name.
 * @param detail Detected tool line, e.g. `adb 35.0.1 · /opt/homebrew/bin`. Machine output.
 * @param toolState Whether the tool was found on `PATH`.
 * @param enabled Whether the platform can be chosen at all (iOS ships disabled this release).
 */
@Immutable
public data class PickerPlatform(
    val id: String,
    val title: String,
    val detail: String,
    val toolState: PlatformToolState,
    val enabled: Boolean = true,
)

/** Which group heading a device sits under in the source list. */
public enum class PickerDeviceGroup {
    /** A handset on a USB or TCP transport. */
    Physical,

    /** An AVD, running or cold. */
    Emulator,
}

/**
 * What the picker shows at the trailing edge of a device row.
 *
 * Three shapes, because a row's trailing area answers three different questions — what the
 * device *is*, what it is *doing*, and what you can *do to it*.
 */
public sealed interface PickerDeviceTrailing {
    /** Plain machine detail, e.g. `Android 15 · USB`. */
    @Immutable
    public data class Detail(val text: String) : PickerDeviceTrailing

    /** A warm emulator — dot plus run-state copy. */
    @Immutable
    public data class Running(val label: String) : PickerDeviceTrailing

    /** A cold emulator — an action that boots it. */
    @Immutable
    public data class Boot(val label: String) : PickerDeviceTrailing
}

/**
 * A row in the picker's source list.
 *
 * @param id Stable identifier (serial or AVD name) reported back through `onDeviceClick`.
 * @param name Display name.
 * @param serial Serial or AVD state, rendered mono. Machine output.
 * @param group Heading this row belongs under.
 * @param trailing What the trailing edge shows.
 * @param selectable `false` for a device that exists but cannot be mirrored yet — a cold
 *   emulator, or one that is unauthorised.
 */
@Immutable
public data class PickerDevice(
    val id: String,
    val name: String,
    val serial: String?,
    val group: PickerDeviceGroup,
    val trailing: PickerDeviceTrailing,
    val selectable: Boolean = true,
)

/**
 * What a key press means to the picker's root, or `null` when the event is none of its
 * business and must bubble.
 *
 * A value rather than a `when` inside the modifier so the mapping is assertable without a
 * window — the same reason `shortcutToTab` exists for the shell's Cmd+1/2/3.
 */
internal enum class PickerKeyAction {
    /** `↑` — move the cursor one row towards the top. */
    MoveUp,

    /** `↓` — move the cursor one row towards the bottom. */
    MoveDown,

    /** `↵` — commit the highlighted row. */
    Start,

    /** `esc` — dismiss the panel (A11Y-4). */
    Close,
}

/** @return What [key] means here, or `null` for every key the picker does not own. */
internal fun pickerKeyAction(key: Key): PickerKeyAction? = when (key) {
    Key.DirectionUp -> PickerKeyAction.MoveUp
    Key.DirectionDown -> PickerKeyAction.MoveDown
    Key.Enter, Key.NumPadEnter -> PickerKeyAction.Start
    Key.Escape -> PickerKeyAction.Close
    else -> null
}

/**
 * The first-run device picker (comps 01 / 03) — "choose what to mirror".
 *
 * **Opaque on purpose.** It fills `StageSurface`'s content slot with [Theme.color.bg], so the
 * stage's dot grid does not show through: the picker is a decision the user has to make before
 * anything can be mirrored, and a texture behind it would read as content waiting underneath.
 * That is also why it is not a card — it *is* the stage while it is up.
 *
 * **Holds no selection state.** [selectedPlatformId] and [selectedDeviceId] come in, intents go
 * out. The only thing it owns is a [FocusRequester] and the root `onPreviewKeyEvent` that turns
 * arrow keys and `↵` into [onMoveSelection] / [onStart] — key handling has to live on a focused
 * node, and that node is this panel's root, but deciding *which* device an arrow key lands on
 * is the caller's job (it owns the list order).
 *
 * The CTA is explicit: nothing starts mirroring until [onStart] fires. Any empty-state copy
 * shown elsewhere must agree with that — see `NoSignalStage`'s subtitle.
 *
 * All copy is a parameter; `presentation-core-ui` has no resource dependency, so call sites
 * pass `stringResource(Res.string.mirror_*)`.
 *
 * @param platforms Platform cards, in display order.
 * @param selectedPlatformId Active platform, or `null`.
 * @param onPlatformClick Called with the platform id the user picked.
 * @param devices Source rows, in display order. Grouped by [PickerDevice.group] for headings.
 * @param selectedDeviceId Active device, or `null`.
 * @param onDeviceClick Called with the device id the user picked.
 * @param onDeviceAction Called when a row's trailing action (`Boot`) is pressed.
 * @param onMoveSelection Called with `-1` / `+1` when the user arrows through the list.
 * @param onStart Called when the user commits — the CTA or `↵`.
 * @param modifier Modifier applied to the root composable.
 * @param title Headline copy.
 * @param subtitle Supporting copy.
 * @param platformSectionLabel `PLATFORM` section label.
 * @param sourceSectionLabel `SOURCE` section label.
 * @param sourceSummary Right-aligned machine summary, e.g. `3 found · adb devices -l`.
 * @param physicalGroupLabel Heading above the physical devices.
 * @param emulatorGroupLabel Heading above the emulators.
 * @param keyHint Mono keyboard hint, e.g. `↑↓ select · ↵ start`.
 * @param startLabel CTA label.
 * @param closeLabel Label of the dismiss affordance.
 * @param onClose Dismisses the panel — `esc` or the close button. `null` means the picker
 *   CANNOT be dismissed, which is the honest state before the first device is chosen: there
 *   is nothing behind the panel to go back to, so no affordance is drawn and `esc` is left to
 *   bubble rather than silently swallowed (A11Y-4).
 */
@Composable
@Suppress("LongParameterList")
public fun DevicePickerPanel(
    platforms: List<PickerPlatform>,
    selectedPlatformId: String?,
    onPlatformClick: (String) -> Unit,
    devices: List<PickerDevice>,
    selectedDeviceId: String?,
    onDeviceClick: (String) -> Unit,
    onDeviceAction: (String) -> Unit,
    onMoveSelection: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Choose what to mirror",
    subtitle: String = "Switch platform or device any time from the toolbar.",
    platformSectionLabel: String = "PLATFORM",
    sourceSectionLabel: String = "SOURCE",
    sourceSummary: String = "",
    physicalGroupLabel: String = "Physical device",
    emulatorGroupLabel: String = "Emulator",
    keyHint: String = "↑↓ select · ↵ start",
    startLabel: String = "Start mirroring",
    closeLabel: String = "Close",
    onClose: (() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Opaque: the stage's dot grid must not read through the picker.
            .background(Theme.color.bg)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (pickerKeyAction(event.key)) {
                    PickerKeyAction.MoveUp -> { onMoveSelection(-1); true }
                    PickerKeyAction.MoveDown -> { onMoveSelection(1); true }
                    PickerKeyAction.Start -> { onStart(); true }
                    PickerKeyAction.Close -> onClose?.let { close -> close(); true } ?: false
                    null -> false
                }
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .padding(horizontal = Theme.spacing.spacingXL, vertical = Theme.spacing.spacing4XL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXL),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
                ) {
                    Text(text = title, style = Theme.typography.titleL, color = Theme.color.ink)
                    Text(text = subtitle, style = Theme.typography.body, color = Theme.color.ink2)
                }
                // Drawn only when there is something to go back to — a dismiss button that
                // dismisses nothing is worse than no button (A11Y-4).
                if (onClose != null) {
                    AppButton(
                        text = closeLabel,
                        onClick = onClose,
                        variant = AppButtonVariant.Secondary,
                        size = AppButtonSize.ListAction,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                PickerSectionLabel(label = platformSectionLabel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS + Theme.spacing.spacingXS),
                ) {
                    platforms.forEach { platform ->
                        PlatformToolCard(
                            title = platform.title,
                            detail = platform.detail,
                            toolState = platform.toolState,
                            selected = platform.id == selectedPlatformId,
                            onClick = { onPlatformClick(platform.id) },
                            enabled = platform.enabled,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                PickerSectionLabel(label = sourceSectionLabel, trailing = sourceSummary)
                SourceList(
                    devices = devices,
                    selectedDeviceId = selectedDeviceId,
                    onDeviceClick = onDeviceClick,
                    onDeviceAction = onDeviceAction,
                    physicalGroupLabel = physicalGroupLabel,
                    emulatorGroupLabel = emulatorGroupLabel,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = keyHint, style = Theme.typography.monoMeta, color = Theme.color.ink2)
                Spacer(modifier = Modifier.weight(1f))
                AppButton(
                    text = startLabel,
                    onClick = onStart,
                    variant = AppButtonVariant.Primary,
                    size = AppButtonSize.PageCta,
                    enabled = selectedDeviceId != null,
                )
            }
        }
    }
}

/**
 * A section label with an optional right-aligned machine summary.
 *
 * Not [presentation.core.ui.source.kit.atom.text.SectionHeader]: that atom hard-codes
 * `fillMaxWidth` and 16 dp horizontal padding, which would indent the label away from the cards
 * underneath it. Same tokens, no padding.
 */
@Composable
private fun PickerSectionLabel(label: String, trailing: String = "") {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = Theme.typography.overline, color = Theme.color.ink2)
        if (trailing.isNotEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = trailing, style = Theme.typography.monoMeta, color = Theme.color.ink2)
        }
    }
}

/** The bordered list of grouped device rows. */
@Composable
private fun SourceList(
    devices: List<PickerDevice>,
    selectedDeviceId: String?,
    onDeviceClick: (String) -> Unit,
    onDeviceAction: (String) -> Unit,
    physicalGroupLabel: String,
    emulatorGroupLabel: String,
) {
    val shape = Theme.cornerToken.card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Theme.color.surface)
            .border(width = Theme.stroke.thin, color = Theme.color.line, shape = shape),
    ) {
        PickerDeviceGroup.entries.forEach { group ->
            val rows = devices.filter { it.group == group }
            if (rows.isEmpty()) return@forEach
            SourceListHeader(
                text = when (group) {
                    PickerDeviceGroup.Physical -> physicalGroupLabel
                    PickerDeviceGroup.Emulator -> emulatorGroupLabel
                },
            )
            rows.forEach { device ->
                SourceListRow(
                    name = device.name,
                    serial = device.serial,
                    selected = device.id == selectedDeviceId,
                    onClick = { onDeviceClick(device.id) },
                    enabled = device.selectable,
                ) {
                    DeviceTrailing(trailing = device.trailing, onAction = { onDeviceAction(device.id) })
                }
            }
        }
    }
}

@Composable
private fun DeviceTrailing(trailing: PickerDeviceTrailing, onAction: () -> Unit) {
    when (trailing) {
        is PickerDeviceTrailing.Detail -> Text(
            text = trailing.text,
            style = Theme.typography.secondary,
            color = Theme.color.ink2,
        )

        is PickerDeviceTrailing.Running -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
        ) {
            Box(
                modifier = Modifier
                    .size(RunningDotSize)
                    .clip(CircleShape)
                    .background(Theme.color.sentFixed),
            )
            Text(text = trailing.label, style = Theme.typography.secondary, color = Theme.color.ink2)
        }

        is PickerDeviceTrailing.Boot -> AppButton(
            text = trailing.label,
            onClick = onAction,
            variant = AppButtonVariant.Secondary,
            size = AppButtonSize.ListAction,
        )
    }
}

@Composable
private fun DevicePickerPanelSample() {
    DevicePickerPanel(
        platforms = listOf(
            PickerPlatform(
                id = "android",
                title = "Android",
                detail = "adb 35.0.1 · /opt/homebrew/bin",
                toolState = PlatformToolState.Found,
            ),
            PickerPlatform(
                id = "ios",
                title = "iOS",
                detail = "idb 1.1.8 · Xcode 16.0",
                toolState = PlatformToolState.Found,
                enabled = false,
            ),
        ),
        selectedPlatformId = "android",
        onPlatformClick = {},
        devices = listOf(
            PickerDevice(
                id = "37281FDH2000AB",
                name = "Pixel 8 Pro",
                serial = "37281FDH2000AB",
                group = PickerDeviceGroup.Physical,
                trailing = PickerDeviceTrailing.Detail("Android 15 · USB"),
            ),
            PickerDevice(
                id = "emulator-5554",
                name = "Pixel_7_API_34",
                serial = "emulator-5554",
                group = PickerDeviceGroup.Emulator,
                trailing = PickerDeviceTrailing.Running("Running"),
            ),
            PickerDevice(
                id = "Pixel_Fold_API_35",
                name = "Pixel_Fold_API_35",
                serial = "shut down",
                group = PickerDeviceGroup.Emulator,
                trailing = PickerDeviceTrailing.Boot("Boot"),
                selectable = false,
            ),
        ),
        selectedDeviceId = "37281FDH2000AB",
        onDeviceClick = {},
        onDeviceAction = {},
        onMoveSelection = {},
        onStart = {},
        sourceSummary = "3 found · adb devices -l",
    )
}

@Preview(showBackground = true)
@Composable
private fun DevicePickerPanelPreview() {
    AppTheme(mode = ThemeMode.Light) {
        DevicePickerPanelSample()
    }
}

@Preview(showBackground = true)
@Composable
private fun DevicePickerPanelDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) {
        DevicePickerPanelSample()
    }
}
