package data.repository.impl.di

import data.repository.impl.source.ConfigureRepositoryImpl
import domain.repository.api.source.repository.ConfigureRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public val dataRepositoryImplModule: Module = module {
    singleOf(::ConfigureRepositoryImpl) bind ConfigureRepository::class
}
