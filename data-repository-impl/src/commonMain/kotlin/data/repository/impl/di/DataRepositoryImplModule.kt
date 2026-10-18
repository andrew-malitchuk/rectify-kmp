package data.repository.impl.di

import data.repository.impl.source.ConfigureRepositoryImpl
import data.repository.impl.source.DeviceRepositoryImpl
import data.repository.impl.source.repository.HistoryRepositoryImpl
import domain.repository.api.source.repository.ConfigureRepository
import domain.repository.api.source.repository.DeviceRepository
import domain.repository.api.source.repository.HistoryRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public val dataRepositoryImplModule: Module = module {
    singleOf(::ConfigureRepositoryImpl) bind ConfigureRepository::class
    // Built by hand, not with singleOf: the last constructor parameter is a clock with a
    // default, which Koin would otherwise try to resolve as a dependency.
    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    provideBatchStore()
    singleOf(::HistoryRepositoryImpl) bind HistoryRepository::class
}
