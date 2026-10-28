package data.preference.api.source.model

import data.core.source.resource.Resource

/**
 * Persisted preference holding the shell side panel width.
 *
 * @property widthDp Panel width in density-independent pixels.
 *   Defaults to [DEFAULT_WIDTH_DP]. Clamping to the allowed range is the
 *   presentation layer's concern, not this resource's.
 * @see data.preference.api.source.datasource.SidePanelWidthPreferenceSource
 */
public data class SidePanelWidthPreference(
    val widthDp: Int = DEFAULT_WIDTH_DP,
) : Resource {
    public companion object {
        /** Default side panel width applied when no preference has been stored yet. */
        public const val DEFAULT_WIDTH_DP: Int = 320
    }
}
