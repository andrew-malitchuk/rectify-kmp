package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.styling.core.ThemeMode
import presentation.core.ui.source.kit.atom.glyph.ErrorKind
import presentation.core.ui.source.kit.molecule.row.SourceListRow
import presentation.core.ui.source.kit.organism.error.ErrorCard
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_boot_emulator
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_locate_adb
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_mirror_device
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_restart_adb
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_retry
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_multiple_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_multiple_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_no_device_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_no_device_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_poll_failed_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_poll_failed_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_tool_missing_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_tool_missing_title

/**
 * Card width, exactly the design's `width:460px`.
 *
 * A named constant and not a `Theme.spacing` composition for the reason `NoticeMaxWidth` next
 * door is one: 460 is a reading measure the design states outright, not a step on the 4/8 dp
 * scale. The shipped [ErrorCard] caps itself at 520 dp, so this narrows it to the design figure
 * rather than widening anything.
 */
private val ErrorCardWidth: Dp = 460.dp

/**
 * The actionable error card on the stage (AC-26…AC-28, AC-53).
 *
 * Stateless and dumb: [card] has already decided *which* error this is in the pure
 * `errorCard()` projection, and everything here is copy resolution plus the intent each button
 * dispatches. All four kinds go through the shipped [ErrorCard] — no new kit primitive, and the
 * geometry (24 dp padding, the 42 dp inset that aligns the actions and footer under the title
 * column, the command block's radius-8 `codeBg` plate, the circular glyph) is the organism's
 * own and is deliberately not re-specified at this call site.
 *
 * The design draws five states; two of them (`err-sim`, `err-trust`) are iOS and deferred by
 * NFR-10, so only the Android four are wired. [MirrorErrorKind] keeps the full shape of the
 * problem space — see its KDoc — and [MirrorErrorKind.PollFailed] in particular is a kind of
 * its own with plan-authored copy, because "adb did not answer" and "adb answered, nothing
 * attached" are opposite diagnoses.
 *
 * **Deviation from the design, recorded:** the design nests the multi-device list *inside* the
 * card, between the body and the actions row. The shipped `ErrorCard` exposes no content slot
 * and adding one is outside this task's two-file scope, so the list renders as a panel directly
 * under the card in the same width, radius and surface tokens — reading as one card divided by
 * its own hairline — with the `Mirror <top device>` shortcut staying in the card above it.
 *
 * @param card Which error to report, from `MirrorUiState.errorCard`.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
internal fun MirrorErrorStage(
    card: MirrorErrorCard,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Machine text, so an empty one means "no footer" rather than an empty rule and a blank line.
    val diagnostic = card.diagnostic.takeIf { it.isNotEmpty() }
    Column(
        modifier = modifier.width(ErrorCardWidth),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        when (card.kind) {
            MirrorErrorKind.ToolMissing -> ErrorCard(
                title = stringResource(Res.string.mirror_error_tool_missing_title),
                body = stringResource(Res.string.mirror_error_tool_missing_body),
                kind = ErrorKind.Fixable,
                command = card.command,
                primaryActionLabel = stringResource(Res.string.mirror_error_action_locate_adb),
                onPrimaryAction = { onIntent(MirrorIntent.LocateAdb) },
                secondaryActionLabel = stringResource(Res.string.mirror_error_action_retry),
                onSecondaryAction = { onIntent(MirrorIntent.RetryDiscovery) },
                diagnostic = diagnostic,
            )

            MirrorErrorKind.NoDevice -> ErrorCard(
                title = stringResource(Res.string.mirror_error_no_device_title),
                body = stringResource(Res.string.mirror_error_no_device_body),
                kind = ErrorKind.Fixable,
                command = card.command,
                // RestartAdb, never RetryDiscovery: re-polling cannot clear a wedged daemon, so
                // a "Restart adb" that only re-polled would be a lying label (FR-37).
                primaryActionLabel = stringResource(Res.string.mirror_error_action_restart_adb),
                onPrimaryAction = { onIntent(MirrorIntent.RestartAdb) },
                // Hidden rather than disabled when no AVD is known — a dead button is the
                // defect FR-37 exists to forbid.
                secondaryActionLabel = stringResource(Res.string.mirror_error_action_boot_emulator)
                    .takeIf { card.canBootEmulator },
                onSecondaryAction = { onIntent(MirrorIntent.BootSelectedAvd) },
                diagnostic = diagnostic,
            )

            MirrorErrorKind.PollFailed -> ErrorCard(
                title = stringResource(Res.string.mirror_error_poll_failed_title),
                body = stringResource(Res.string.mirror_error_poll_failed_body),
                kind = ErrorKind.Failed,
                // No command block: there is nothing to re-run but the poll itself, which is
                // what the primary does.
                primaryActionLabel = stringResource(Res.string.mirror_error_action_retry),
                onPrimaryAction = { onIntent(MirrorIntent.RetryDiscovery) },
                diagnostic = diagnostic,
            )

            MirrorErrorKind.MultipleDevices -> {
                val top = card.rows.firstOrNull { it.id == card.primaryDeviceId }
                ErrorCard(
                    // The count is the list the card actually shows, not the handset subset:
                    // a headline that says three while four rows sit under it is a lie about
                    // the user's own bench.
                    title = stringResource(Res.string.mirror_error_multiple_title, card.rows.size),
                    // "…for this session", never the design's "until you quit": sessionDeviceId
                    // is nulled by onSessionLost, selectPlatform and stopSession, so the longer
                    // promise is false (inputs/design-error-states.md CORRECTION 3).
                    body = stringResource(Res.string.mirror_error_multiple_body),
                    kind = ErrorKind.Choice,
                    primaryActionLabel = top
                        ?.let { stringResource(Res.string.mirror_error_action_mirror_device, it.name) },
                    onPrimaryAction = {
                        top?.let { row ->
                            onIntent(MirrorIntent.HighlightRow(row.id))
                            onIntent(MirrorIntent.Commit)
                        }
                    },
                    diagnostic = diagnostic,
                )
                DeviceList(rows = card.rows, onIntent = onIntent)
            }
        }
    }
}

/**
 * The inline device list of the multi-device card (AC-28 / FR-34).
 *
 * Every attached row is listed, the unauthorised one included: it renders through
 * [SourceListRow]'s `enabled = false`, which drops the click handler *and* announces the row as
 * disabled (A11Y-6), rather than being hidden from a user who is holding the phone they just
 * plugged in and wondering where it went.
 *
 * No row is ever drawn selected. A click commits immediately, so there is no chosen-but-
 * uncommitted state for a filled [presentation.core.ui.source.kit.atom.indicator.RadioMark] to
 * report; the card's own `Mirror <top device>` label is what names the shortcut target.
 */
@Composable
private fun DeviceList(rows: List<MirrorRow>, onIntent: (MirrorIntent) -> Unit) {
    Column(
        modifier = Modifier
            .clip(Theme.cornerToken.card)
            .background(Theme.color.surface)
            .border(Theme.stroke.thin, Theme.color.line, Theme.cornerToken.card)
            .padding(vertical = Theme.spacing.spacingS),
    ) {
        rows.forEach { row ->
            SourceListRow(
                name = row.name,
                selected = false,
                // Two intents, in the picker's own order: the cursor moves to the row, then the
                // commit acts on it. Orbit processes them in dispatch order, so this is the
                // same path ↑↓ then ↵ takes.
                onClick = {
                    onIntent(MirrorIntent.HighlightRow(row.id))
                    onIntent(MirrorIntent.Commit)
                },
                serial = row.serial,
                enabled = row.selectable,
            ) {
                if (row.detail.isNotEmpty()) {
                    Text(
                        text = row.detail,
                        style = Theme.typography.monoMeta,
                        color = Theme.color.ink2,
                    )
                }
            }
        }
    }
}

/** Every state the card can be in, so one preview pair covers all four kinds (TAC-3). */
@Composable
private fun MirrorErrorStageSample() {
    Column(
        modifier = Modifier
            .background(Theme.color.stage)
            .padding(Theme.spacing.spacingL),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
    ) {
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.ToolMissing,
                command = "brew install --cask android-platform-tools",
                diagnostic = "which adb → not found · \$ANDROID_HOME unset",
            ),
            onIntent = {},
        )
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.NoDevice,
                command = "adb kill-server && adb devices",
                diagnostic = "adb 35.0.1 · last poll 0.8 s ago · 0 attached",
                canBootEmulator = true,
            ),
            onIntent = {},
        )
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.PollFailed,
                diagnostic = "adb poll failed · adb server version (40) doesn't match this client (41)",
            ),
            onIntent = {},
        )
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.MultipleDevices,
                rows = listOf(
                    MirrorRow(
                        id = "37281FDH2000AB",
                        name = "Pixel 8 Pro",
                        serial = "37281FDH2000AB",
                        group = MirrorRowGroup.Physical,
                        kind = MirrorRowKind.Detail,
                        detail = "Android 15 · USB",
                    ),
                    MirrorRow(
                        id = "RF8M90ABCDE",
                        name = "Galaxy S24",
                        serial = "RF8M90ABCDE",
                        group = MirrorRowGroup.Physical,
                        kind = MirrorRowKind.Detail,
                        detail = "Android 14 · USB",
                    ),
                    MirrorRow(
                        id = "0A1B2C3D",
                        name = "0A1B2C3D",
                        serial = "0A1B2C3D",
                        group = MirrorRowGroup.Physical,
                        kind = MirrorRowKind.Detail,
                        detail = "unauthorized",
                        selectable = false,
                    ),
                ),
                primaryDeviceId = "37281FDH2000AB",
                diagnostic = "adb devices -l · 3 attached",
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun MirrorErrorStagePreview() {
    AppTheme(mode = ThemeMode.Light) {
        Box { MirrorErrorStageSample() }
    }
}

@Preview
@Composable
private fun MirrorErrorStageDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) {
        Box { MirrorErrorStageSample() }
    }
}
