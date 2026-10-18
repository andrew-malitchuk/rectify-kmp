package data.preference.impl.source.datasource

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default implementation of [AdbPathPreferenceSource].
 *
 * Persists the user-chosen `adb` path via [Settings] and exposes
 * a [MutableStateFlow] for reactive observation.
 *
 * @property settings Platform settings store injected by Koin.
 * @see AdbPathPreferenceSource
 */
internal class AdbPathPreferenceSourceImpl(
    private val settings: Settings,
) : AdbPathPreferenceSource {

    /** In-memory state flow initialised with the persisted value on construction. */
    private val _flow = MutableStateFlow(readFromSettings())

    /**
     * Reads the current `adb` path from the platform [Settings].
     *
     * Falls back to an empty path when nothing has been persisted.
     *
     * @return An [AdbPathPreference] with the stored path, or an empty one.
     */
    private fun readFromSettings(): AdbPathPreference {
        return AdbPathPreference(
            path = settings.getStringOrNull(KEY_ADB_PATH).orEmpty(),
        )
    }

    /**
     * Retrieves the current `adb` path from persistent storage.
     *
     * @return The latest [AdbPathPreference].
     */
    override suspend fun getData(): AdbPathPreference = readFromSettings()

    /**
     * Persists the given [data] and notifies observers.
     *
     * @param data The [AdbPathPreference] to persist.
     */
    override suspend fun setData(data: AdbPathPreference) {
        settings[KEY_ADB_PATH] = data.path
        _flow.value = data
    }

    /**
     * Observes the `adb` path as a reactive [Flow].
     *
     * @return A read-only [Flow] emitting [AdbPathPreference] on every change.
     */
    override fun observeData(): Flow<AdbPathPreference> = _flow.asStateFlow()

    private companion object {
        /** Settings key under which the user-chosen `adb` path is stored. */
        const val KEY_ADB_PATH = "pref_adb_path"
    }
}
