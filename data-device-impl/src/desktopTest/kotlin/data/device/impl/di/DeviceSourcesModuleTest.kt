package data.device.impl.di

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IdbHostSource
import data.device.api.source.datasource.MirrorTransport
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
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
        // The real source lives in data-preference-impl, which this module must not depend on;
        // the contract is all `ToolResolver` needs, so the contract is all the test supplies.
        val preferences = module {
            single<AdbPathPreferenceSource> { UnsetAdbPath }
        }
        val koin = koinApplication { modules(preferences, dataDeviceImplModule) }.koin

        assertNotNull(koin.get<AdbHostSource>())
        assertNotNull(koin.get<IdbHostSource>())
        assertNotNull(koin.get<EmulatorSource>())
        assertNotNull(koin.get<DeviceCommandExecutor>())
        assertNotNull(koin.get<MirrorTransport>())
    }
}

/** An `adb` path the user never chose - the default state of the preference. */
private object UnsetAdbPath : AdbPathPreferenceSource {
    override suspend fun getData(): AdbPathPreference = AdbPathPreference()
    override suspend fun setData(data: AdbPathPreference) = Unit
    override fun observeData(): Flow<AdbPathPreference> = flowOf(AdbPathPreference())
}
