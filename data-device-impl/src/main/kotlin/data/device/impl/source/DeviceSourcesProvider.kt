package data.device.impl.source

import data.device.api.source.datasource.ANDROID_SOURCE
import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IOS_SOURCE
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.impl.core.tool.ToolResolver
import data.device.impl.source.datasource.AdbDeviceCommandExecutor
import data.device.impl.source.datasource.AdbHostSourceImpl
import data.device.impl.source.datasource.EmulatorSourceImpl
import data.device.impl.source.datasource.IosDeviceCommandExecutor
import data.device.impl.source.datasource.IosHostSourceImpl
import data.device.impl.source.transport.ScreencapMirrorTransport
import data.preference.api.source.datasource.AdbPathPreferenceSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind

/**
 * Desktop implementation of [provideDeviceSources].
 *
 * Registers the real `adb` / `emulator` / `xcrun` / `idb` backed sources. [ToolResolver] is
 * built by hand rather than with `singleOf` because its parameters are host facts with
 * defaults (environment, home directory, executable predicate), not Koin dependencies -
 * except `userPath`, which is read fresh on every probe so a path the user changes
 * mid-session takes effect without a restart.
 *
 * The host sources stay unqualified - there is one `adb` and one `xcrun` on this host - while
 * the executor and the transport are registered once per platform under [ANDROID_SOURCE] /
 * [IOS_SOURCE]. The two transports are the SAME class twice, differing only in which executor
 * they were handed: `ScreencapMirrorTransport` is reused for iOS, not duplicated.
 */
internal fun Module.provideDeviceSources() {
    single {
        val adbPath = get<AdbPathPreferenceSource>()
        ToolResolver(userPath = { adbPath.getData().path })
    }
    singleOf(::AdbHostSourceImpl) bind AdbHostSource::class
    singleOf(::EmulatorSourceImpl) bind EmulatorSource::class
    singleOf(::IosHostSourceImpl) bind IosHostSource::class
    // Explicit single, never singleOf, for all four below: two qualified dependencies Koin
    // cannot infer, and a transport whose second parameter is an injectable clock with a
    // default that singleOf would try to resolve instead of honouring.
    single<DeviceCommandExecutor>(named(ANDROID_SOURCE)) { AdbDeviceCommandExecutor(tools = get()) }
    single<DeviceCommandExecutor>(named(IOS_SOURCE)) { IosDeviceCommandExecutor(tools = get(), hosts = get()) }
    single<MirrorTransport>(named(ANDROID_SOURCE)) {
        ScreencapMirrorTransport(executor = get(named(ANDROID_SOURCE)))
    }
    single<MirrorTransport>(named(IOS_SOURCE)) {
        ScreencapMirrorTransport(executor = get(named(IOS_SOURCE)))
    }
}
