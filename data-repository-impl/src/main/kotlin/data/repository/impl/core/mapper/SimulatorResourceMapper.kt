package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.SimulatorResource
import domain.core.source.model.device.EmulatorModel

/** `simctl`'s own word for a simulator that is running; every other state is cold. */
private const val BOOTED_STATE = "Booted"

/**
 * One-direction mapper from a [SimulatorResource] to the [EmulatorModel] the picker renders.
 *
 * Deliberately NOT a [data.repository.impl.core.mapper.base.ModelResourceMapper], for the
 * same reason [HierarchySnapshotMapper] is not: only one direction exists. An
 * [EmulatorModel] has no runtime-independent `state` string to map back to, so a
 * `toResource` could only invent one.
 *
 * A simulator is an [EmulatorModel] rather than a `DeviceModel` because the picker's
 * "cold row, committing boots it" behaviour is exactly what a shut-down simulator needs, and
 * reusing it is what keeps the iOS rows out of the Android device grouping.
 *
 * Never throws: the udid rides through verbatim (it is the only identity a simulator has,
 * booted or not) and an unrecognised state is simply not [BOOTED_STATE].
 */
internal object SimulatorResourceMapper {

    /** @see SimulatorResourceMapper */
    val toModel: Mapper<SimulatorResource, EmulatorModel> =
        Mapper { resource ->
            EmulatorModel(
                avdName = resource.name,
                isRunning = isBooted(resource),
                // The udid even when SHUT DOWN - a simulator name is not a key, so this is
                // what every boot and capture addresses.
                runningDeviceId = resource.udid,
                runtime = resource.runtime,
            )
        }

    /** Whether this row is the one a session could attach to right now. */
    fun isBooted(resource: SimulatorResource): Boolean = resource.state.equals(BOOTED_STATE, ignoreCase = true)
}
