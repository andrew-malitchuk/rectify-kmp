package data.device.impl.source

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IdbHostSource
import data.device.api.source.datasource.MirrorTransport
import data.device.impl.core.tool.ToolResolver
import data.device.impl.source.datasource.AdbHostSourceImpl
import data.device.impl.source.datasource.DeviceCommandExecutorImpl
import data.device.impl.source.datasource.EmulatorSourceImpl
import data.device.impl.source.datasource.IdbHostSourceImpl
import data.device.impl.source.transport.ScreencapMirrorTransport
import data.preference.api.source.datasource.AdbPathPreferenceSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

/**
 * Desktop implementation of [provideDeviceSources].
 *
 * Registers the real `adb` / `emulator` backed sources. [ToolResolver] is built by hand
 * rather than with `singleOf` because its parameters are host facts with defaults
 * (environment, home directory, executable predicate), not Koin dependencies - except
 * `userPath`, which is read fresh on every probe so a path the user changes mid-session takes
 * effect without a restart.
 */
internal actual fun Module.provideDeviceSources() {
    single {
        val adbPath = get<AdbPathPreferenceSource>()
        ToolResolver(userPath = { adbPath.getData().path })
    }
    singleOf(::AdbHostSourceImpl) bind AdbHostSource::class
    singleOf(::EmulatorSourceImpl) bind EmulatorSource::class
    singleOf(::IdbHostSourceImpl) bind IdbHostSource::class
    singleOf(::DeviceCommandExecutorImpl) bind DeviceCommandExecutor::class
    // Not singleOf: the transport's second parameter is an injectable clock with a default,
    // and singleOf would try to resolve it from Koin instead of honouring the default.
    single<MirrorTransport> { ScreencapMirrorTransport(executor = get()) }
}
