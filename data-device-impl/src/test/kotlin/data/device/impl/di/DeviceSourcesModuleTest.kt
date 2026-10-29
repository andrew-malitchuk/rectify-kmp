package data.device.impl.di

import data.device.api.source.datasource.ANDROID_SOURCE
import data.device.api.source.datasource.AdbHostSource
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.EmulatorSource
import data.device.api.source.datasource.IOS_SOURCE
import data.device.api.source.datasource.IosHostSource
import data.device.api.source.datasource.MirrorTransport
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Every device graph resolves every registration it will be asked for at launch.
 *
 * RESOLUTION only — constructing these spawns nothing. A missing registration is a RUNTIME
 * failure that compiling the module cannot catch, and the failure is a crash on the very
 * first frame, so this test is the only machine guard between a forgotten qualifier and a
 * desktop app that will not start. It earned that description the hard way: a wave of this
 * spec left `DeviceRepositoryImpl` asking for bindings nobody registered, and the whole suite
 * stayed green because nothing resolved the graph.
 */
class DeviceSourcesModuleTest {

    @Test
    fun everyDeviceDatasourceResolvesOnDesktop() {
        assertEveryRegistrationResolves(koinFor(dataDeviceImplModule))
    }

    @Test
    fun noUnqualifiedExecutorOrTransportResolves() {
        // The other half of the qualifier contract: there must be no unqualified binding to
        // fall back on, or a caller that forgets the qualifier silently gets whichever
        // mirrored platform was registered last instead of failing loudly.
        val koin = koinFor(dataDeviceImplModule)
        assertNull(koin.getOrNull<DeviceCommandExecutor>())
        assertNull(koin.getOrNull<MirrorTransport>())
    }

    private fun assertEveryRegistrationResolves(koin: Koin) {
        assertNotNull(koin.get<AdbHostSource>())
        assertNotNull(koin.get<IosHostSource>())
        assertNotNull(koin.get<EmulatorSource>())
        // One executor and one transport PER PLATFORM, so both qualifiers are asked for on
        // every graph: omitting one is invisible on the platform that does not use it.
        assertNotNull(koin.get<DeviceCommandExecutor>(named(ANDROID_SOURCE)))
        assertNotNull(koin.get<DeviceCommandExecutor>(named(IOS_SOURCE)))
        assertNotNull(koin.get<MirrorTransport>(named(ANDROID_SOURCE)))
        assertNotNull(koin.get<MirrorTransport>(named(IOS_SOURCE)))
    }

    // The real source lives in data-preference-impl, which this module must not depend on;
    // the contract is all `ToolResolver` needs, so the contract is all the test supplies.
    private fun koinFor(sources: Module): Koin = koinApplication {
        modules(module { single<AdbPathPreferenceSource> { UnsetAdbPath } }, sources)
    }.koin
}

/** An `adb` path the user never chose - the default state of the preference. */
private object UnsetAdbPath : AdbPathPreferenceSource {
    override suspend fun getData(): AdbPathPreference = AdbPathPreference()
    override suspend fun setData(data: AdbPathPreference) = Unit
    override fun observeData(): Flow<AdbPathPreference> = flowOf(AdbPathPreference())
}
