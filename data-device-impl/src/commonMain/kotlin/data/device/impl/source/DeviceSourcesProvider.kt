package data.device.impl.source

import data.device.api.source.datasource.ANDROID_SOURCE
import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IOS_SOURCE
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.impl.core.unsupported.UnsupportedAdbHostSource
import data.device.impl.core.unsupported.UnsupportedDeviceCommandExecutor
import data.device.impl.core.unsupported.UnsupportedEmulatorSource
import data.device.impl.core.unsupported.UnsupportedIosHostSource
import data.device.impl.core.unsupported.UnsupportedMirrorTransport
import org.koin.core.module.Module
import org.koin.core.qualifier.named

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

/**
 * The no-op graph both mobile targets register, written ONCE.
 *
 * Android and iOS registered byte-identical bodies until this existed, and that duplication
 * was the hazard: a qualifier added to one actual and forgotten in the other is invisible on
 * desktop and a Koin crash at launch on the target that missed it. One body means
 * `DeviceSourcesModuleTest` can resolve the mobile graph from `desktopTest` — where the
 * `androidMain`/`iosMain` actuals themselves are not even compiled — and actually guard it.
 *
 * There is no `adb` on a phone, so every source here fails loudly when called rather than
 * silently doing nothing; the registrations exist so the graph RESOLVES on every target.
 */
internal fun Module.provideUnsupportedDeviceSources() {
    single<AdbHostSource> { UnsupportedAdbHostSource }
    single<EmulatorSource> { UnsupportedEmulatorSource }
    single<IosHostSource> { UnsupportedIosHostSource }
    single<DeviceCommandExecutor>(named(ANDROID_SOURCE)) { UnsupportedDeviceCommandExecutor }
    single<DeviceCommandExecutor>(named(IOS_SOURCE)) { UnsupportedDeviceCommandExecutor }
    single<MirrorTransport>(named(ANDROID_SOURCE)) { UnsupportedMirrorTransport }
    single<MirrorTransport>(named(IOS_SOURCE)) { UnsupportedMirrorTransport }
}
