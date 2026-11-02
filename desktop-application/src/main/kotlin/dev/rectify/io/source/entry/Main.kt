package dev.rectify.io.source.entry

import dev.rectify.io.source.di.initKoin
import dev.rectify.io.source.window.runDesktopApp

/**
 * JVM entry point for the desktop application.
 *
 * Starts the Koin dependency graph (nothing else bootstraps it on desktop), then hands
 * control to the shared Compose UI.
 */
fun main() {
    initKoin()
    runDesktopApp()
}
