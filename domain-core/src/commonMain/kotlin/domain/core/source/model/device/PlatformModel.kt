package domain.core.source.model.device

import domain.core.source.model.base.Model

/** Mirroring target platform. */
public enum class PlatformModel : Model {
    /** Android devices and emulators, driven through `adb`. */
    Android,

    /** iOS simulators, driven through `idb`. Not wired in this feature. */
    Ios,
}
