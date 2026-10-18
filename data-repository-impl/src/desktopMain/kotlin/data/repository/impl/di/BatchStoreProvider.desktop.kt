package data.repository.impl.di

import data.repository.impl.core.store.FileSystemBatchFileStore
import data.repository.impl.source.store.BatchFileStore
import org.koin.core.module.Module

/**
 * Desktop implementation of [provideBatchStore] — the real `~/.rectify/history/` writer.
 *
 * Not `singleOf`: the only constructor parameter is the history root, which has a default and
 * is not a Koin dependency.
 */
internal actual fun Module.provideBatchStore() {
    single<BatchFileStore> { FileSystemBatchFileStore() }
}
