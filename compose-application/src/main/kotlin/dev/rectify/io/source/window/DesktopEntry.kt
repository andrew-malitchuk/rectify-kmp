package dev.rectify.io.source.window

import androidx.compose.ui.window.application

/**
 * Launches the desktop application window.
 *
 * Called from the `:desktop-application` module's `main()` entry point.
 *
 * Renders [RectifyWindow] rather than `App()`: the Rectify shell *is* the application root,
 * so there is no navigation host here. `App()` stays the navigation-host root, untouched.
 *
 * @see RectifyWindow
 */
public fun runDesktopApp() {
    application {
        RectifyWindow()
    }
}
