package data.device.impl.di

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IdbHostSource
import data.device.api.source.datasource.MirrorTransport
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * The desktop graph resolves, including the `idb` probe behind the iOS status dot (AC-1).
 *
 * Resolution only — constructing these spawns nothing; a missing registration is a RUNTIME
 * failure that compiling the module cannot catch.
 */
class DeviceSourcesModuleTest {

    @Test
    fun everyDeviceDatasourceResolvesOnDesktop() {
        val koin = koinApplication { modules(dataDeviceImplModule) }.koin

        assertNotNull(koin.get<AdbHostSource>())
        assertNotNull(koin.get<IdbHostSource>())
        assertNotNull(koin.get<EmulatorSource>())
        assertNotNull(koin.get<DeviceCommandExecutor>())
        assertNotNull(koin.get<MirrorTransport>())
    }
}
