package domain.core.source.model.device

import domain.core.source.model.base.Model

/** Mirroring target platform. */
public enum class PlatformModel : Model {
    /** Android devices and emulators, driven through `adb`. */
    Android,

    /** iOS simulators and devices, driven through `xcrun simctl` and `idb`. */
    Ios,
}
