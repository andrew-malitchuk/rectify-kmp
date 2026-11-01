package presentation.feature.mirror.core.discovery

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import presentation.feature.mirror.core.picker.firstSelectableIndex
import presentation.feature.mirror.source.mirror.MirrorRow
import presentation.feature.mirror.source.mirror.MirrorRowGroup
import presentation.feature.mirror.source.mirror.MirrorRowKind
import presentation.feature.mirror.source.mirror.MirrorUiState

/**
 * Folds one discovery emission into the reduced state.
 *
 * Pure, and separated from the ViewModel because the subtle part is not the plumbing but the
 * **retention**: [DiscoveryModel.NoDeviceFound] carries no emulator list, so projecting it
 * naively would empty the AVD picker and the Boot button exactly when the user needs them.
 * The last known emulators survive the escalation, demoted to cold.
 */
internal fun MirrorUiState.applyDiscovery(discovery: DiscoveryModel): MirrorUiState =
    when (discovery) {
        // A probe in flight says nothing about what is attached — keep what is on screen.
        DiscoveryModel.Detecting -> copy(discovery = discovery)

        // Same reasoning: a poll that failed produced no information, so the rows stand and
        // only the status label changes. Clearing them would make an adb hiccup look like
        // the device had been unplugged.
        is DiscoveryModel.PollFailed -> copy(discovery = discovery)

        // No tool means no information at all, so the list is cleared rather than kept stale.
        is DiscoveryModel.ToolMissing -> copy(
            discovery = discovery,
            rows = emptyList(),
            avds = emptyList(),
            selectedAvd = null,
            highlightedIndex = 0,
        )

        is DiscoveryModel.NoDeviceFound -> withRows(
            discovery = discovery,
            newRows = rows.filter { it.group == MirrorRowGroup.Emulator }.map(MirrorRow::demotedToCold),
        ).copy(
            // Nothing is left to pick; closing the picker is what surfaces the passive
            // NO SIGNAL stage of AC-4. The toolbar chip reopens it (AC-43).
            isPickerOpen = false,
        )

        is DiscoveryModel.Found -> withRows(
            discovery = discovery,
            newRows = discovery.physical.map(DeviceModel::toRow) +
                discovery.emulators.map(EmulatorModel::toRow),
        )

        // Cold simulators are real, pickable rows: MirrorRowKind.Boot already means "cold,
        // committing boots it", so the existing Emulator group carries them unchanged. The
        // picker stays open — unlike NoDeviceFound there IS something to choose.
        is DiscoveryModel.SimulatorNotBooted -> withRows(
            discovery = discovery,
            newRows = discovery.simulators.map(EmulatorModel::toRow),
        )

        // The untrusted handset is the only candidate, so there is nothing to list: the card
        // owns the stage and the rows would be a list of one unusable row.
        is DiscoveryModel.DeviceNotTrusted -> copy(discovery = discovery)
    }

/**
 * Replaces the row list, keeping the cursor on the same row when it is still selectable and
 * falling back to the first usable one otherwise, and refreshes the AVD shortcut list.
 */
private fun MirrorUiState.withRows(discovery: DiscoveryModel, newRows: List<MirrorRow>): MirrorUiState {
    val highlightedId = highlightedRow?.id
    val keptIndex = newRows.indexOfFirst { it.id == highlightedId && it.selectable }
    val avdNames = newRows.mapNotNull { it.avdName }
    return copy(
        discovery = discovery,
        rows = newRows,
        highlightedIndex = if (keptIndex >= 0) keptIndex else firstSelectableIndex(newRows),
        avds = avdNames,
        selectedAvd = selectedAvd.takeIf { it in avdNames } ?: avdNames.firstOrNull(),
    )
}

/** A retained emulator row after escalation: whatever it was, it is not running now. */
private fun MirrorRow.demotedToCold(): MirrorRow = copy(
    id = avdName ?: id,
    serial = null,
    kind = MirrorRowKind.Boot,
    selectable = true,
)

private fun DeviceModel.toRow(): MirrorRow = MirrorRow(
    id = id,
    name = displayName,
    serial = serial,
    group = MirrorRowGroup.Physical,
    kind = MirrorRowKind.Detail,
    // Why a device is unusable is the answer the user came for, so the tool's own word for it
    // is shown verbatim instead of being hidden behind a disabled row with no explanation.
    detail = if (state == DeviceStateModel.Device) {
        listOfNotNull(androidVersion?.let { "Android $it" }, transportLabel).joinToString(" · ")
    } else {
        state.name.lowercase()
    },
    selectable = isSelectable,
)

private fun EmulatorModel.toRow(): MirrorRow = MirrorRow(
    id = runningDeviceId ?: avdName,
    name = avdName,
    serial = runningDeviceId,
    group = MirrorRowGroup.Emulator,
    kind = if (isRunning) MirrorRowKind.Running else MirrorRowKind.Boot,
    // A cold AVD is selectable: committing it boots the image instead of starting a session.
    selectable = true,
    avdName = avdName,
    detail = rowDetail(),
)

/**
 * PP-TD-9's iOS half: a simulator row says which runtime it is.
 *
 * Android is untouched by construction — it leaves [EmulatorModel.runtime] null and keeps
 * today's empty detail, so PP-TD-9's Android half stays open under its own gate.
 *
 * The udid head rides along because the runtime ALONE does not disambiguate: the captured
 * `simctl list devices available` from this host lists two `iPhone 15 Pro` under the single
 * iOS 17.5 runtime, so name + runtime still collides and only the udid does not.
 */
private fun EmulatorModel.rowDetail(): String = when (runtime) {
    null -> ""
    else -> listOfNotNull(runtime, runningDeviceId?.take(UDID_HEAD_LENGTH)).joinToString(" · ")
}

/** Enough udid to tell two same-named, same-runtime simulators apart without filling the row. */
private const val UDID_HEAD_LENGTH = 8
