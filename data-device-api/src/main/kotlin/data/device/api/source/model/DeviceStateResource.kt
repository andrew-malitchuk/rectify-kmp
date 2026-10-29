package data.device.api.source.model

import data.core.source.resource.Resource

/** Device state column reported by `adb devices -l`. */
public enum class DeviceStateResource : Resource {
    /** `device` — online and usable. */
    Device,

    /** `unauthorized` — USB debugging prompt not accepted. */
    Unauthorized,

    /** `offline` — known but not responding. */
    Offline,

    /** `no permissions` — the host user cannot access the USB node. */
    NoPermissions,

    /** `recovery`. */
    Recovery,

    /** `sideload`. */
    Sideload,

    /** Any state string this version does not model. */
    Unknown,
}
