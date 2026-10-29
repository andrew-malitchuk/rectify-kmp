package data.repository.impl

import data.device.api.source.datasource.ANDROID_SOURCE
import data.device.api.source.datasource.DeviceCommandExecutor
import data.device.api.source.datasource.IOS_SOURCE
import data.device.api.source.datasource.MirrorTransport
import data.device.impl.di.dataDeviceImplModule
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import data.repository.impl.di.dataRepositoryImplModule
import domain.repository.api.source.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * The device seam resolved across BOTH modules at once.
 *
 * `DeviceSourcesModuleTest` over in data-device-impl resolves one module at a time, and that
 * is exactly the gap that let a wave of this spec ship a desktop app which could not start
 * while the suite stayed green for three waves: nothing anywhere composed
 * `dataDeviceImplModule` with `dataRepositoryImplModule`, so a consumer asking for a binding
 * nobody registered was invisible.
 *
 * It lives HERE, in data-repository-impl, because this is the consuming side of the seam —
 * `DeviceRepositoryImpl` is the only type that takes both qualified pairs — and because the
 * module that owns `initKoin()`, `:compose-application`, deliberately has no test wiring
 * (see `.claude/rules/testing.md`). One layer down is as close to the real list as a test
 * that actually runs can get, and the two device modules are the only pair the registration
 * failure ever spanned. data-device-impl is a test-only dependency for that reason.
 *
 * RESOLUTION only: nothing here spawns a process or touches the filesystem.
 */
class ComposedDeviceGraphTest {

    @Test
    fun deviceRepositoryResolvesAcrossTheComposedDeviceAndRepositoryModules() {
        val koin = koinApplication {
            // The order `initKoin()` uses: device sources ahead of the repository that
            // consumes them. Koin resolves lazily, so this is defensive there and here alike.
            modules(preferenceContract, dataDeviceImplModule, dataRepositoryImplModule)
        }.koin

        assertNotNull(koin.get<DeviceRepository>())
    }

    /**
     * Both qualified pairs stay reachable once the two modules are composed.
     *
     * The constructor above would already fail on a missing one, but it fails as one opaque
     * error; asking for each binding by name says WHICH qualifier went missing.
     */
    @Test
    fun bothQualifiedExecutorAndTransportPairsResolveOnTheComposedGraph() {
        val koin = koinApplication {
            modules(preferenceContract, dataDeviceImplModule, dataRepositoryImplModule)
        }.koin

        assertNotNull(koin.get<DeviceCommandExecutor>(named(ANDROID_SOURCE)))
        assertNotNull(koin.get<DeviceCommandExecutor>(named(IOS_SOURCE)))
        assertNotNull(koin.get<MirrorTransport>(named(ANDROID_SOURCE)))
        assertNotNull(koin.get<MirrorTransport>(named(IOS_SOURCE)))
    }
}

/**
 * The one registration a real launch gets from data-preference-impl, which neither module
 * under test may depend on. The contract is all `ToolResolver` reads, so the contract is all
 * this supplies.
 */
private val preferenceContract = module {
    single<AdbPathPreferenceSource> {
        object : AdbPathPreferenceSource {
            override suspend fun getData(): AdbPathPreference = AdbPathPreference()
            override suspend fun setData(data: AdbPathPreference) = Unit
            override fun observeData(): Flow<AdbPathPreference> = flowOf(AdbPathPreference())
        }
    }
}
