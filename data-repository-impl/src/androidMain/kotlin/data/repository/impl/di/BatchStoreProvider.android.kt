package data.repository.impl.di

import data.repository.impl.source.store.BatchFileStore
import org.koin.core.module.Module

/**
 * Android implementation of [provideBatchStore].
 *
 * There is no handoff slot on a mobile target, so nothing can ever be sent and nothing can ever
 * be recorded — the graph resolves with a store that always answers `null` (AS-29), stated
 * explicitly rather than defaulted in.
 */
internal actual fun Module.provideBatchStore() {
    single<BatchFileStore> { BatchFileStore { _, _ -> null } }
}
