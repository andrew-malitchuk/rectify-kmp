package data.preference.api.source.datasource

import data.preference.api.source.datasource.base.PreferenceSource
import data.preference.api.source.model.AdbPathPreference

/**
 * Preference data source for the user-chosen `adb` binary path.
 *
 * @see AdbPathPreference
 */
public interface AdbPathPreferenceSource : PreferenceSource<AdbPathPreference>
