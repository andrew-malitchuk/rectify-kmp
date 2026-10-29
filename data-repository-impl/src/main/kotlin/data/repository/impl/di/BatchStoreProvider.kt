package data.repository.impl.di

import data.repository.impl.core.store.FileSystemBatchFileReader
import data.repository.impl.core.store.FileSystemBatchFileStore
import data.repository.impl.source.store.BatchFileReader
import data.repository.impl.source.store.BatchFileStore
import org.koin.core.module.Module

/**
 * Desktop implementation of [provideBatchStore] — the real `~/.rectify/history/` writer.
 *
 * Not `singleOf`: the only constructor parameter is the history root, which has a default and
 * is not a Koin dependency.
 */
internal fun Module.provideBatchStore() {
    single<BatchFileStore> { FileSystemBatchFileStore() }
}

/** Desktop implementation of [provideBatchFileReader] — the `java.io` reader. */
internal fun Module.provideBatchFileReader() {
    single<BatchFileReader> { FileSystemBatchFileReader() }
}
