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
import domain.core.source.model.device.PlatformModel
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
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_boot_simulator
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_choose_another
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_locate_adb
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_mirror_device
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_restart_adb
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_retry
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_trusted_retry
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_action_use_simulator
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_device_not_trusted_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_device_not_trusted_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_multiple_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_multiple_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_no_device_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_no_device_body_ios
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_no_device_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_no_device_title_ios
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_poll_failed_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_poll_failed_body_ios
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_simulator_not_booted_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_simulator_not_booted_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_poll_failed_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_poll_failed_title_ios
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_tool_missing_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_tool_missing_body_ios
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_tool_missing_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_error_tool_missing_title_ios

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
 * All six states the design draws are wired, the two iOS ones (`err-sim`, `err-trust`)
 * included — they were deferred under NFR-10 and are live as of F-10. [MirrorErrorKind] keeps
 * the full shape of the problem space — see its KDoc — and [MirrorErrorKind.PollFailed] in
 * particular is a kind of its own with plan-authored copy, because "the tool did not answer"
 * and "the tool answered, nothing attached" are opposite diagnoses.
 *
 * The four SHARED kinds read [MirrorErrorCard.platform] and resolve iOS-worded copy and
 * iOS-safe actions: on iOS none of them may offer "Locate adb…", "Restart adb" or
 * "Boot emulator instead", because each is wired to an Android-only path. Routing iOS to no
 * card at all instead was the alternative, and it is a blank stage for four real conditions.
 * [MirrorErrorKind.MultipleDevices] needs no split — its copy names no binary.
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
        // The copy split for the four SHARED kinds. Resolved once here rather than branched
        // inside each arm: `adb` is wrong on an iOS bench in both directions — there is no
        // platform-tools to install and no daemon to restart — and an action whose label names
        // a binary this platform does not use is the dead button FR-37 forbids.
        val ios = card.platform == PlatformModel.Ios
        when (card.kind) {
            MirrorErrorKind.ToolMissing -> ErrorCard(
                title = stringResource(
                    if (ios) Res.string.mirror_error_tool_missing_title_ios
                    else Res.string.mirror_error_tool_missing_title,
                ),
                body = stringResource(
                    if (ios) Res.string.mirror_error_tool_missing_body_ios
                    else Res.string.mirror_error_tool_missing_body,
                ),
                kind = ErrorKind.Fixable,
                command = card.command,
                // "Locate adb…" opens an adb file picker and writes the adb path preference,
                // so on iOS it is not merely mis-worded, it is wired to the wrong thing: the
                // retry is promoted to primary and no secondary is offered.
                primaryActionLabel = stringResource(
                    if (ios) Res.string.mirror_error_action_retry
                    else Res.string.mirror_error_action_locate_adb,
                ),
                onPrimaryAction = {
                    onIntent(if (ios) MirrorIntent.RetryDiscovery else MirrorIntent.LocateAdb)
                },
                secondaryActionLabel = stringResource(Res.string.mirror_error_action_retry)
                    .takeUnless { ios },
                onSecondaryAction = { onIntent(MirrorIntent.RetryDiscovery) },
                diagnostic = diagnostic,
            )

            MirrorErrorKind.NoDevice -> ErrorCard(
                title = stringResource(
                    if (ios) Res.string.mirror_error_no_device_title_ios
                    else Res.string.mirror_error_no_device_title,
                ),
                body = stringResource(
                    if (ios) Res.string.mirror_error_no_device_body_ios
                    else Res.string.mirror_error_no_device_body,
                ),
                kind = ErrorKind.Fixable,
                command = card.command,
                // RestartAdb, never RetryDiscovery: re-polling cannot clear a wedged daemon, so
                // a "Restart adb" that only re-polled would be a lying label (FR-37). There is
                // no daemon at all on iOS, so there the poll itself is the only thing to re-run.
                primaryActionLabel = stringResource(
                    if (ios) Res.string.mirror_error_action_retry
                    else Res.string.mirror_error_action_restart_adb,
                ),
                onPrimaryAction = {
                    onIntent(if (ios) MirrorIntent.RetryDiscovery else MirrorIntent.RestartAdb)
                },
                // Hidden rather than disabled when no AVD is known — a dead button is the
                // defect FR-37 exists to forbid. `canBootEmulator` counts ANDROID avds, so the
                // secondary is Android-only: a simulator to boot is `err-sim`'s own card.
                secondaryActionLabel = stringResource(Res.string.mirror_error_action_boot_emulator)
                    .takeIf { card.canBootEmulator && !ios },
                onSecondaryAction = { onIntent(MirrorIntent.BootSelectedAvd) },
                diagnostic = diagnostic,
            )

            MirrorErrorKind.PollFailed -> ErrorCard(
                title = stringResource(
                    if (ios) Res.string.mirror_error_poll_failed_title_ios
                    else Res.string.mirror_error_poll_failed_title,
                ),
                body = stringResource(
                    if (ios) Res.string.mirror_error_poll_failed_body_ios
                    else Res.string.mirror_error_poll_failed_body,
                ),
                kind = ErrorKind.Failed,
                // No command block: there is nothing to re-run but the poll itself, which is
                // what the primary does.
                primaryActionLabel = stringResource(Res.string.mirror_error_action_retry),
                onPrimaryAction = { onIntent(MirrorIntent.RetryDiscovery) },
                diagnostic = diagnostic,
            )

            // err-sim. Primary boots BY UDID: card.primaryDeviceId carries the simulator's
            // udid, never its name, because two simulators can share a name. Secondary opens
            // the picker, which errorCard() yields the stage to for this variant - without
            // that yield this button would be dead.
            MirrorErrorKind.SimulatorNotBooted -> ErrorCard(
                title = stringResource(Res.string.mirror_error_simulator_not_booted_title),
                body = stringResource(Res.string.mirror_error_simulator_not_booted_body),
                kind = ErrorKind.Fixable,
                command = card.command,
                primaryActionLabel = stringResource(Res.string.mirror_error_action_boot_simulator)
                    .takeIf { card.primaryDeviceId != null },
                onPrimaryAction = { card.primaryDeviceId?.let { onIntent(MirrorIntent.BootRow(it)) } },
                secondaryActionLabel = stringResource(Res.string.mirror_error_action_choose_another),
                onSecondaryAction = { onIntent(MirrorIntent.OpenPicker) },
                diagnostic = diagnostic,
            )

            // err-trust. RetryDiscovery, not RestartAdb: the user fixes this on the handset and
            // the only thing this app can do is look again, which RetryDiscovery already cuts
            // the poll interval short to do.
            MirrorErrorKind.DeviceNotTrusted -> ErrorCard(
                title = stringResource(Res.string.mirror_error_device_not_trusted_title),
                body = stringResource(Res.string.mirror_error_device_not_trusted_body),
                kind = ErrorKind.Fixable,
                command = card.command,
                // Worded as the thing the user just did on the handset, not as a bare "Retry":
                // the whole card is a wait on a person, and the label is what tells them the
                // app is waiting on them rather than on itself.
                primaryActionLabel = stringResource(Res.string.mirror_error_action_trusted_retry),
                onPrimaryAction = { onIntent(MirrorIntent.RetryDiscovery) },
                // Hidden when no simulator exists - the defining dead-button case.
                secondaryActionLabel = stringResource(Res.string.mirror_error_action_use_simulator)
                    .takeIf { card.canBootEmulator },
                onSecondaryAction = { onIntent(MirrorIntent.OpenPicker) },
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

/** Every state the card can be in, so one preview pair covers all six kinds (TAC-3 / TAC-5). */
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
        // err-sim. The udid, never the name: both simulators on this host's capture are
        // called `iPhone 15 Pro`, which is why the row detail carries the runtime.
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.SimulatorNotBooted,
                command = "xcrun simctl boot 5F20FFD2-3B90-42DA-9227-26725B78EA58",
                diagnostic = "xcrun 26.3 · last poll 0.8 s ago · 54 shut down",
                primaryDeviceId = "5F20FFD2-3B90-42DA-9227-26725B78EA58",
                platform = PlatformModel.Ios,
            ),
            onIntent = {},
        )
        // err-trust, with the simulator fallback live.
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.DeviceNotTrusted,
                command = "idb list-targets",
                diagnostic = "Andrew iPhone · 00008130-001A2B3C4D5E6F00",
                primaryDeviceId = "00008130-001A2B3C4D5E6F00",
                canBootEmulator = true,
                platform = PlatformModel.Ios,
            ),
            onIntent = {},
        )
        // A SHARED kind on iOS, so the preview pair also covers the copy split: no "Locate
        // adb…", no adb command block.
        MirrorErrorStage(
            card = MirrorErrorCard(
                kind = MirrorErrorKind.ToolMissing,
                command = "xcode-select --install",
                diagnostic = "/usr/bin/xcrun · \$DEVELOPER_DIR unset",
                platform = PlatformModel.Ios,
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
