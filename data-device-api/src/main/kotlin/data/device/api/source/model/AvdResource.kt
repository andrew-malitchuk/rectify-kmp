package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * A configured AVD, with the running instance attached when there is one.
 *
 * @property name AVD name as `emulator -list-avds` reports it.
 * @property runningAs Serial of the running instance, or `null` when not running.
 */
public data class AvdResource(
    val name: AvdName,
    val runningAs: DeviceId? = null,
) : Resource
