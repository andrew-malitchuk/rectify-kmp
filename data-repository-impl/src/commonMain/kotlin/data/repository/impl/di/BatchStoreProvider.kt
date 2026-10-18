package data.repository.impl.di

import org.koin.core.module.Module

/**
 * Registers a [data.repository.impl.source.store.BatchFileStore] in the Koin module.
 *
 * - **Desktop:** `FileSystemBatchFileStore`, writing under `~/.rectify/history/`.
 * - **Android / iOS:** a store that always answers `null`, so the graph resolves on every
 *   target even though there is no desktop file slot to feed it (AS-29) — unreachable in
 *   practice, because `UnsupportedAnnotationHandoff.publish` throws long before a batch would
 *   reach here.
 *
 * Shape copied verbatim from `presentation-feature-mirror`'s `Module.provideHandoffSources()`.
 */
internal expect fun Module.provideBatchStore()
