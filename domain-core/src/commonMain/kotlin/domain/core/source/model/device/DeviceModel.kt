package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * A device the user can pick as a mirroring source.
 *
 * @property id Stable identifier used to address the device (the tool serial).
 * @property displayName Human-readable label shown in the picker.
 * @property serial Raw serial reported by the tool.
 * @property state Connection state; only [DeviceStateModel.Device] can be mirrored.
 * @property isEmulator `true` for an emulator instance rather than physical hardware.
 * @property androidVersion Release string when known.
 * @property transportLabel Transport identifier when known.
 * @property isSelectable `true` when the device can be selected for mirroring.
 */
public data class DeviceModel(
    val id: String,
    val displayName: String,
    val serial: String,
    val state: DeviceStateModel,
    val isEmulator: Boolean,
    val androidVersion: String? = null,
    val transportLabel: String? = null,
    val isSelectable: Boolean = state == DeviceStateModel.Device,
) : Model
