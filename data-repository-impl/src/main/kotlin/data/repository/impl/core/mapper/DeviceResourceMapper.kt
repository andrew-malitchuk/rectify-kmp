package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource
import data.repository.impl.core.mapper.base.ModelResourceMapper
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel

/**
 * Bidirectional mapper between [DeviceModel] and [DeviceResource].
 *
 * Never throws: an unmodelled state becomes [DeviceStateModel.Unknown], and a serial that
 * would not pass [DeviceId.of] becomes [UNKNOWN_SERIAL] rather than an exception. The
 * display name falls back `model` to `product` to the serial, so a row always has a label.
 */
internal object DeviceResourceMapper : ModelResourceMapper<DeviceModel, DeviceResource> {

    /** Stand-in serial for the round trip of a model that never came from a parsed row. */
    private val unknownSerial: DeviceId = requireNotNull(DeviceId.of(UNKNOWN_SERIAL)) {
        "$UNKNOWN_SERIAL is a valid serial by inspection"
    }

    /** @see ModelResourceMapper.toModel */
    override val toModel: Mapper<DeviceResource, DeviceModel> =
        Mapper { resource ->
            val state = resource.state.toStateModel()
            DeviceModel(
                id = resource.id.value,
                displayName = resource.model ?: resource.product ?: resource.id.value,
                serial = resource.id.value,
                state = state,
                isEmulator = resource.isEmulator,
                transportLabel = resource.transport,
                isSelectable = state == DeviceStateModel.Device,
            )
        }

    /** @see ModelResourceMapper.toResource */
    override val toResource: Mapper<DeviceModel, DeviceResource> =
        Mapper { model ->
            DeviceResource(
                id = DeviceId.of(model.serial) ?: unknownSerial,
                state = model.state.toStateResource(),
                model = model.displayName,
                transport = model.transportLabel,
                isEmulator = model.isEmulator,
            )
        }

    private fun DeviceStateResource.toStateModel(): DeviceStateModel = when (this) {
        DeviceStateResource.Device -> DeviceStateModel.Device
        DeviceStateResource.Unauthorized -> DeviceStateModel.Unauthorized
        DeviceStateResource.Offline -> DeviceStateModel.Offline
        DeviceStateResource.NoPermissions -> DeviceStateModel.NoPermissions
        DeviceStateResource.Recovery -> DeviceStateModel.Recovery
        DeviceStateResource.Sideload -> DeviceStateModel.Sideload
        DeviceStateResource.Unknown -> DeviceStateModel.Unknown
    }

    private fun DeviceStateModel.toStateResource(): DeviceStateResource = when (this) {
        DeviceStateModel.Device -> DeviceStateResource.Device
        DeviceStateModel.Unauthorized -> DeviceStateResource.Unauthorized
        DeviceStateModel.Offline -> DeviceStateResource.Offline
        DeviceStateModel.NoPermissions -> DeviceStateResource.NoPermissions
        DeviceStateModel.Recovery -> DeviceStateResource.Recovery
        DeviceStateModel.Sideload -> DeviceStateResource.Sideload
        DeviceStateModel.Unknown -> DeviceStateResource.Unknown
    }
}

/** Serial used when a [DeviceModel] carries one no tool would ever report. */
private const val UNKNOWN_SERIAL = "unknown"
