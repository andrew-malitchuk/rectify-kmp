package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * Connection state of an attached device, as reported by the platform tool.
 *
 * Only [Device] is mirrorable; every other state is still listed so the user can see
 * why a plugged-in device is unusable.
 */
public enum class DeviceStateModel : Model {
    /** Online and ready to accept commands. */
    Device,

    /** Attached but the USB debugging prompt has not been accepted. */
    Unauthorized,

    /** Known to the tool but not responding. */
    Offline,

    /** Attached but the host user cannot access the USB node. */
    NoPermissions,

    /** Booted into recovery. */
    Recovery,

    /** Booted into sideload. */
    Sideload,

    /** State string the tool reported that this version does not model. */
    Unknown,
}
