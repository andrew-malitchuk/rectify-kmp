package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * One row of `adb devices -l`.
 *
 * @property id Resolved serial.
 * @property state Reported connection state; non-`Device` rows are kept, not dropped.
 * @property model `model:` tag, when present.
 * @property product `product:` tag, when present.
 * @property transport `transport_id:` tag, when present.
 * @property isEmulator `true` for an `emulator-*` serial.
 */
public data class DeviceResource(
    val id: DeviceId,
    val state: DeviceStateResource,
    val model: String? = null,
    val product: String? = null,
    val transport: String? = null,
    val isEmulator: Boolean = false,
) : Resource
