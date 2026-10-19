package data.device.impl.source

import org.koin.core.module.Module

/**
 * Koin qualifier for the Android half of a two-platform interface.
 *
 * Declared here, in `commonMain`, so all three `provideDeviceSources()` actuals spell it the
 * same way: a qualifier typo is not a compile error, it is a crash at app launch on whichever
 * platform got it wrong.
 */
internal const val ANDROID_SOURCE: String = "android"

/** Koin qualifier for the iOS half. @see ANDROID_SOURCE */
internal const val IOS_SOURCE: String = "ios"

/**
 * Registers the platform's device datasources in the Koin module.
 *
 * - **Desktop:** the real implementations, driving `adb` / `emulator` / `xcrun` / `idb` child
 *   processes.
 * - **Android / iOS:** [data.device.impl.core.unsupported.UnsupportedAdbHostSource] and
 *   friends, so the graph resolves on every target even though no process may be spawned
 *   there.
 *
 * [data.device.api.source.datasource.DeviceCommandExecutor] and
 * [data.device.api.source.datasource.MirrorTransport] exist once per platform and are
 * therefore ALWAYS qualified — [ANDROID_SOURCE] or [IOS_SOURCE], in every actual. No
 * unqualified binding of either survives anywhere, so a caller that forgets the qualifier
 * fails loudly instead of silently resolving whichever one was registered last.
 */
internal expect fun Module.provideDeviceSources()
