package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * A configured emulator image (AVD), running or not.
 *
 * @property avdName Name of the AVD as the emulator tool reports it.
 * @property isRunning `true` when an attached device is this AVD.
 * @property runningDeviceId Identifier of the running instance, when [isRunning].
 */
public data class EmulatorModel(
    val avdName: String,
    val isRunning: Boolean,
    val runningDeviceId: String? = null,
) : Model
