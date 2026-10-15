package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.AvdName
import data.device.api.source.model.AvdResource
import data.device.api.source.model.DeviceId
import data.repository.impl.core.mapper.base.ModelResourceMapper
import domain.core.source.model.device.EmulatorModel

/**
 * Bidirectional mapper between [EmulatorModel] and [AvdResource].
 *
 * Never throws: `isRunning` is derived from the presence of a running serial rather than
 * stored twice, and a name or serial that would not validate falls back to [UNKNOWN_AVD] or
 * to no running instance.
 */
internal object EmulatorResourceMapper : ModelResourceMapper<EmulatorModel, AvdResource> {

    private val unknownAvd: AvdName = requireNotNull(AvdName.of(UNKNOWN_AVD)) {
        "$UNKNOWN_AVD is a valid AVD name by inspection"
    }

    /** @see ModelResourceMapper.toModel */
    override val toModel: Mapper<AvdResource, EmulatorModel> =
        Mapper { resource ->
            EmulatorModel(
                avdName = resource.name.value,
                isRunning = resource.runningAs != null,
                runningDeviceId = resource.runningAs?.value,
            )
        }

    /** @see ModelResourceMapper.toResource */
    override val toResource: Mapper<EmulatorModel, AvdResource> =
        Mapper { model ->
            AvdResource(
                name = AvdName.of(model.avdName) ?: unknownAvd,
                runningAs = model.runningDeviceId?.let(DeviceId::of),
            )
        }
}

/** Name used when an [EmulatorModel] carries one the emulator tool would never report. */
private const val UNKNOWN_AVD = "unknown"
