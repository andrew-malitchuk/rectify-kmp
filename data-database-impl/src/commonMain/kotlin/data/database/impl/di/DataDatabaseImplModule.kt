package data.database.impl.di

import data.database.impl.core.provideDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin module that provides the local Room database instance. */
public val dataDatabaseImplModule: Module = module {
    provideDatabaseBuilder()
}
