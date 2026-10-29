package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * One device row of `xcrun simctl list devices available`.
 *
 * @property name Simulator name as the tool prints it, parentheses and all
 *   (`iPhone SE (3rd generation)`). NOT a key: the captured fixture lists two `iPhone 15 Pro`
 *   under the single iOS 17.5 runtime.
 * @property udid The only identifier that addresses one simulator.
 * @property runtime Section header the row sat under, e.g. `iOS 18.5`.
 * @property state Tool's own word, verbatim — `Booted`, `Shutdown` or whatever else it says.
 *   Kept unparsed for the same reason [DeviceResource.state]'s unusable rows are kept: the
 *   user needs to see what the tool said.
 */
public data class SimulatorResource(
    val name: String,
    val udid: String,
    val runtime: String,
    val state: String,
) : Resource
