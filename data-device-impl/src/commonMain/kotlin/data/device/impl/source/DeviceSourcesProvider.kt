package data.device.impl.source

import org.koin.core.module.Module

/**
 * Registers the platform's device datasources in the Koin module.
 *
 * - **Desktop:** the real implementations, driving `adb` / `emulator` child processes.
 * - **Android / iOS:** [data.device.impl.core.unsupported.UnsupportedAdbHostSource] and
 *   friends, so the graph resolves on every target even though no process may be spawned
 *   there.
 */
internal expect fun Module.provideDeviceSources()
