package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * A configured emulator image (AVD) or iOS simulator, running or not.
 *
 * @property avdName Name of the AVD as the emulator tool reports it; the simulator name on iOS.
 * @property isRunning `true` when an attached device is this AVD.
 * @property runningDeviceId Identifier of the running instance.
 *
 *   On iOS this carries the simulator's **udid even when the simulator is SHUT DOWN**, which
 *   is why there is no second identity field: a simulator name is not a key. The captured
 *   `simctl list devices available` from this host has `iPad Pro 11-inch (M4)` under all four
 *   installed runtimes AND two `iPhone 15 Pro` under iOS 17.5 alone — so even name+runtime
 *   does not identify one. Every iOS boot and capture is addressed by this value.
 * @property runtime Runtime the image targets, e.g. `iOS 18.5`, or `null` on Android.
 *
 *   Display only — identity is [runningDeviceId]'s job. It exists because the picker would
 *   otherwise list `iPhone 16` twice with nothing to tell the two apart, and because there is
 *   nowhere else to carry it: the discovery variants hold a plain `List<EmulatorModel>`.
 */
public data class EmulatorModel(
    val avdName: String,
    val isRunning: Boolean,
    val runningDeviceId: String? = null,
    val runtime: String? = null,
) : Model
