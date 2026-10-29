package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.IosTargetResource
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel

/**
 * One-direction mapper from an `idb` target row to the [DeviceModel] the picker renders.
 *
 * One direction only, like [SimulatorResourceMapper]: [DeviceModel] has no `targetType` to
 * map back to.
 *
 * [IosTargetResource.trusted] is the whole of selectability here, and it maps onto the
 * EXISTING [DeviceStateModel] vocabulary rather than adding an iOS-only state: an untrusted
 * handset is [DeviceStateModel.Unauthorized], which is literally what it is and what the
 * Android `unauthorized` row already renders as.
 *
 * Never throws; the tool's own words for name and state ride through verbatim.
 */
internal object IosTargetResourceMapper {

    /** @see IosTargetResourceMapper */
    val toModel: Mapper<IosTargetResource, DeviceModel> =
        Mapper { resource ->
            val state = if (resource.trusted) DeviceStateModel.Device else DeviceStateModel.Unauthorized
            DeviceModel(
                id = resource.udid,
                displayName = resource.name,
                serial = resource.udid,
                state = state,
                isEmulator = false,
                isSelectable = resource.trusted,
            )
        }
}
