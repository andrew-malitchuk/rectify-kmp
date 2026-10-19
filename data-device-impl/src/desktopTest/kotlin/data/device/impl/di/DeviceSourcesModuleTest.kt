package data.device.impl.di

import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.datasource.MirrorTransport
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * The desktop graph resolves, including the iOS host source behind the iOS status dot (AC-1).
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
        assertNotNull(koin.get<IosHostSource>())
        assertNotNull(koin.get<EmulatorSource>())
        // Qualified on purpose: there is one executor and one transport PER PLATFORM, and no
        // unqualified binding of either exists any more. T-14 widens this into the full
        // three-graph guard.
        assertNotNull(koin.get<DeviceCommandExecutor>(named("android")))
        assertNotNull(koin.get<DeviceCommandExecutor>(named("ios")))
        assertNotNull(koin.get<MirrorTransport>(named("android")))
        assertNotNull(koin.get<MirrorTransport>(named("ios")))
    }
}

/** An `adb` path the user never chose - the default state of the preference. */
private object UnsetAdbPath : AdbPathPreferenceSource {
    override suspend fun getData(): AdbPathPreference = AdbPathPreference()
    override suspend fun setData(data: AdbPathPreference) = Unit
    override fun observeData(): Flow<AdbPathPreference> = flowOf(AdbPathPreference())
}
