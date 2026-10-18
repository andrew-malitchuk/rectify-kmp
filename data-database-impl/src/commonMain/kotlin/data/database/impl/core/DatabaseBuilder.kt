package data.database.impl.core

import data.database.impl.core.database.AppDatabase
import org.koin.core.module.Module

/**
 * Registers a platform-specific [AppDatabase] singleton in the Koin module.
 *
 * - **Android:** uses the application [Context] to locate the database file.
 * - **iOS / macOS:** stores the database in the app's Documents directory.
 * - **Desktop:** stores the database at `~/.rectify/rectify.db`.
 */
internal expect fun Module.provideDatabaseBuilder()
