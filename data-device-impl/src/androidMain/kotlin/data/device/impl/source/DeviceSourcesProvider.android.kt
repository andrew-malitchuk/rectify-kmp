package data.device.impl.source

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
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
 * Android implementation of [provideDeviceSources].
 *
 * Registers the no-op sources: there is no `adb` on a mobile target, so the graph resolves
 * and common code links, while any attempt to run a command fails loudly instead of
 * silently doing nothing.
 *
 * The executor and the transport are registered under BOTH qualifiers from the same two
 * objects. Omitting either is invisible on desktop and a Koin crash at launch here.
 */
internal actual fun Module.provideDeviceSources() {
    single<AdbHostSource> { UnsupportedAdbHostSource }
    single<EmulatorSource> { UnsupportedEmulatorSource }
    single<IosHostSource> { UnsupportedIosHostSource }
    single<DeviceCommandExecutor>(named(ANDROID_SOURCE)) { UnsupportedDeviceCommandExecutor }
    single<DeviceCommandExecutor>(named(IOS_SOURCE)) { UnsupportedDeviceCommandExecutor }
    single<MirrorTransport>(named(ANDROID_SOURCE)) { UnsupportedMirrorTransport }
    single<MirrorTransport>(named(IOS_SOURCE)) { UnsupportedMirrorTransport }
}
