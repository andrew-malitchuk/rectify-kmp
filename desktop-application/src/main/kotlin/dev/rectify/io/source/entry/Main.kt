package dev.rectify.io.source.entry

import dev.rectify.io.source.di.initKoin
import dev.rectify.io.source.window.runDesktopApp

/**
 * JVM entry point for the desktop application.
 *
 * The desktop counterpart of [dev.rectify.io.source.activity.MainActivity] (Android) and
 * [dev.rectify.io.source.viewcontroller.MainViewController] (iOS) — starts the Koin dependency
 * graph (there is no `Application.onCreate()` on desktop to do it), then hands control to the
 * shared Compose UI.
 */
fun main() {
    initKoin()
    runDesktopApp()
}
