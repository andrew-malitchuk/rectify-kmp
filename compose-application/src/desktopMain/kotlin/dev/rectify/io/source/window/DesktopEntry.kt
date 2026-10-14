package dev.rectify.io.source.window

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.rectify.io.source.app.App

/**
 * Launches the desktop application window hosting the shared [App] composable.
 *
 * Called from the `:desktop-application` module's `main()` entry point — mirrors
 * [dev.rectify.io.source.viewcontroller.MainViewController] for iOS.
 *
 * @see App
 */
public fun runDesktopApp() {
    application {
        Window(onCloseRequest = ::exitApplication, title = "Rectify") {
            App()
        }
    }
}
