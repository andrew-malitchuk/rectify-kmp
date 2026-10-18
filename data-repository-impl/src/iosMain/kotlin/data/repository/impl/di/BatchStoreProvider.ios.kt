package data.repository.impl.di

import data.repository.impl.source.store.BatchFileReader
import data.repository.impl.source.store.BatchFileStore
import org.koin.core.module.Module

/**
 * iOS implementation of [provideBatchStore].
 *
 * There is no handoff slot on a mobile target, so nothing can ever be sent and nothing can ever
 * be recorded — the graph resolves with a store that always answers `null` (AS-29), stated
 * explicitly rather than defaulted in.
 */
internal actual fun Module.provideBatchStore() {
    single<BatchFileStore> { BatchFileStore { _, _ -> null } }
}

/**
 * Ios implementation of [provideBatchFileReader].
 *
 * Nothing was ever written on this target, so there is nothing to read back: the graph
 * resolves with a reader that always answers `null` (AS-29).
 */
internal actual fun Module.provideBatchFileReader() {
    single<BatchFileReader> { BatchFileReader { null } }
}
