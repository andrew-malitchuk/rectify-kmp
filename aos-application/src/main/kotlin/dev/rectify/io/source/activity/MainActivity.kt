package dev.rectify.io.source.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import dev.rectify.io.source.app.App

/**
 * Single-activity entry point for the Android application.
 *
 * Enables edge-to-edge rendering so the app draws behind system bars,
 * then hands full control to the shared Compose [App] composable.
 * The activity itself contains no business logic — it acts purely as
 * an Android lifecycle host for the KMP UI layer.
 *
 * @see App
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // NOTE: enableEdgeToEdge() must be called before super.onCreate() to take
        // effect before the window is attached; calling it after produces a visual flicker.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}