package data.device.impl.source

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.MirrorTransport
import data.device.impl.core.unsupported.UnsupportedAdbHostSource
import data.device.impl.core.unsupported.UnsupportedDeviceCommandExecutor
import data.device.impl.core.unsupported.UnsupportedEmulatorSource
import data.device.impl.core.unsupported.UnsupportedMirrorTransport
import org.koin.core.module.Module

/**
 * Android implementation of [provideDeviceSources].
 *
 * Registers the no-op sources: there is no `adb` on a mobile target, so the graph resolves
 * and common code links, while any attempt to run a command fails loudly instead of
 * silently doing nothing.
 */
internal actual fun Module.provideDeviceSources() {
    single<AdbHostSource> { UnsupportedAdbHostSource }
    single<EmulatorSource> { UnsupportedEmulatorSource }
    single<DeviceCommandExecutor> { UnsupportedDeviceCommandExecutor }
    single<MirrorTransport> { UnsupportedMirrorTransport }
}
