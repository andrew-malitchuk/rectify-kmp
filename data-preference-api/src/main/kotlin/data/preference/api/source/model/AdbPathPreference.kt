package data.preference.api.source.model

import data.core.source.resource.Resource

/**
 * Persisted preference holding the user-chosen path to an `adb` binary.
 *
 * @property path Absolute path to an `adb` executable, or an empty string when the
 *   user has not pointed Rectify at one — an unset path means "probe the usual
 *   locations", so an existing install behaves exactly as before.
 * @see data.preference.api.source.datasource.AdbPathPreferenceSource
 */
public data class AdbPathPreference(
    val path: String = "",
) : Resource
