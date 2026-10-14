package dev.rectify.io.source.window

import androidx.compose.ui.window.application

/**
 * Launches the desktop application window.
 *
 * Called from the `:desktop-application` module's `main()` entry point — mirrors
 * [dev.rectify.io.source.viewcontroller.MainViewController] for iOS.
 *
 * Desktop renders [RectifyWindow] rather than `App()`: the Rectify shell *is* the desktop
 * root, so there is no navigation host here. `App()` stays the Android/iOS root, untouched.
 *
 * @see RectifyWindow
 */
public fun runDesktopApp() {
    application {
        RectifyWindow()
    }
}
