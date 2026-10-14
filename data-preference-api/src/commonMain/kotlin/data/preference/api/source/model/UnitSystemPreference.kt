package data.preference.api.source.model

import data.core.source.resource.Resource

/**
 * Persisted preference holding the selected measurement unit system.
 *
 * @property unitSystem Unit system identifier string (e.g., `"metric"`, `"imperial"`).
 *   Defaults to [DEFAULT_UNIT_SYSTEM].
 */
public data class UnitSystemPreference(
    val unitSystem: String = DEFAULT_UNIT_SYSTEM,
) : Resource {
    public companion object {
        public const val DEFAULT_UNIT_SYSTEM: String = "metric"
    }
}
