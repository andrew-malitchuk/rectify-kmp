package data.repository.impl.di

import data.device.api.source.datasource.ANDROID_SOURCE
import data.device.api.source.datasource.IOS_SOURCE
import data.repository.impl.source.ConfigureRepositoryImpl
import data.repository.impl.source.DeviceRepositoryImpl
import data.repository.impl.source.repository.HistoryRepositoryImpl
import domain.repository.api.source.repository.ConfigureRepository
import domain.repository.api.source.repository.DeviceRepository
import domain.repository.api.source.repository.HistoryRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

public val dataRepositoryImplModule: Module = module {
    singleOf(::ConfigureRepositoryImpl) bind ConfigureRepository::class
    // Built by hand, not with singleOf, for two reasons Koin cannot infer: the last
    // constructor parameter is a clock with a default, and the executor/transport pairs are
    // QUALIFIED - there is no unqualified binding of either, so an unnamed get() would not
    // resolve at app launch.
    single<DeviceRepository> {
        DeviceRepositoryImpl(
            adbHostSource = get(),
            iosHostSource = get(),
            emulatorSource = get(),
            androidExecutor = get(named(ANDROID_SOURCE)),
            iosExecutor = get(named(IOS_SOURCE)),
            androidTransport = get(named(ANDROID_SOURCE)),
            iosTransport = get(named(IOS_SOURCE)),
            adbPathPreferenceSource = get(),
        )
    }
    provideBatchStore()
    provideBatchFileReader()
    singleOf(::HistoryRepositoryImpl) bind HistoryRepository::class
}
