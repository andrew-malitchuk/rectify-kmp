package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * Static properties of a device, read once per session.
 *
 * @property widthPx Display width in pixels.
 * @property heightPx Display height in pixels.
 * @property densityDpi Display density.
 * @property model `ro.product.model`.
 * @property apiLevel `ro.build.version.sdk`.
 */
public data class DeviceInfoResource(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val model: String,
    val apiLevel: Int,
) : Resource
