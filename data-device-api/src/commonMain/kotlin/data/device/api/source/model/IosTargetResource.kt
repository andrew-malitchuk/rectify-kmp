package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * One row of `idb list-targets`.
 *
 * Covers simulators as well as physical handsets — `idb` lists both — so the physical half is
 * selected on [targetType], not by assuming the command only returns devices.
 *
 * @property name Target name as the tool prints it.
 * @property udid Target identifier.
 * @property state Tool's own word, verbatim.
 * @property targetType `simulator` or `device`, verbatim.
 * @property trusted Whether this target can actually be driven. Always `true` for a
 *   simulator; for a physical device it is the answer to "has the user tapped Trust".
 */
public data class IosTargetResource(
    val name: String,
    val udid: String,
    val state: String,
    val targetType: String,
    val trusted: Boolean,
) : Resource
