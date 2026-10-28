package data.preference.impl.source

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import java.util.prefs.Preferences

/**
 * Desktop platform implementation of [provideSettings].
 *
 * Registers a [Settings] singleton backed by [PreferencesSettings], which delegates to the
 * JVM's standard [Preferences] API (`java.util.prefs`), namespaced under the app's package.
 *
 * @see PreferencesSettings
 * @see data.preference.impl.source.provideSettings
 */
internal fun Module.provideSettings() {
    single<Settings> {
        PreferencesSettings(Preferences.userRoot().node("dev.rectify.io"))
    }
}
