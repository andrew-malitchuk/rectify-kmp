package data.database.impl.di

import data.database.impl.core.database.AppDatabase
import data.database.impl.core.datasource.HistoryDataSourceImpl
import data.database.impl.core.provideDatabaseBuilder
import data.database.impl.source.datasource.HistoryDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin module that provides the local Room database instance and its data sources. */
public val dataDatabaseImplModule: Module = module {
    provideDatabaseBuilder()
    // Explicit over singleOf: the DAO is not a bound type of its own, it is reached through
    // the database singleton.
    single<HistoryDataSource> { HistoryDataSourceImpl(get<AppDatabase>().historyEntryDao()) }
}
