package dev.rectify.io.core

import android.app.Application
import dev.rectify.io.source.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Android [Application] subclass that bootstraps the Koin dependency graph.
 *
 * Called once per process lifetime before any Activity or Service is created.
 * Registers the Android context and logger so that all Koin-injected components
 * receive a valid [android.content.Context] from the very first injection site.
 *
 * @see initKoin
 */
internal class RectifyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // NOTE: initKoin must be called before any component requests an injection.
        // androidContext() makes the Application context available to every Koin module
        // that declares androidContext() or androidApplication() in its definitions.
        initKoin {
            androidLogger()
            androidContext(this@RectifyApplication)
        }
    }
}
