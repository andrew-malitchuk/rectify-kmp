package data.device.impl.di

import data.device.impl.source.provideDeviceSources
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin dependency injection module for the device data layer.
 *
 * Registers, per platform, singletons for
 * [data.device.api.source.datasource.AdbHostSource],
 * [data.device.api.source.datasource.EmulatorSource],
 * [data.device.api.source.datasource.DeviceCommandExecutor] and
 * [data.device.api.source.datasource.MirrorTransport].
 */
public val dataDeviceImplModule: Module = module {
    provideDeviceSources()
}
