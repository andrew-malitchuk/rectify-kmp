package data.repository.impl.core.mapper

import common.core.source.mapper.Mapper
import data.device.api.source.model.MirrorFrame
import data.repository.impl.core.mapper.base.ModelResourceMapper
import domain.core.source.model.device.MirrorFrameModel

/**
 * Bidirectional mapper between [MirrorFrameModel] and [MirrorFrame].
 *
 * The byte array is passed by reference, not copied: this runs once per frame and a copy
 * per frame would be megabytes of garbage per second for no benefit.
 */
internal object MirrorFrameMapper : ModelResourceMapper<MirrorFrameModel, MirrorFrame> {

    /** @see ModelResourceMapper.toModel */
    override val toModel: Mapper<MirrorFrame, MirrorFrameModel> =
        Mapper { frame ->
            MirrorFrameModel(
                bytes = frame.bytes,
                widthPx = frame.widthPx,
                heightPx = frame.heightPx,
                capturedAtMs = frame.capturedAtMs,
            )
        }

    /** @see ModelResourceMapper.toResource */
    override val toResource: Mapper<MirrorFrameModel, MirrorFrame> =
        Mapper { model ->
            MirrorFrame(
                bytes = model.bytes,
                widthPx = model.widthPx,
                heightPx = model.heightPx,
                capturedAtMs = model.capturedAtMs,
            )
        }
}
