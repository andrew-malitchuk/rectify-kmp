package data.preference.impl.source.datasource

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import data.preference.api.source.datasource.SidePanelWidthPreferenceSource
import data.preference.api.source.model.SidePanelWidthPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default implementation of [SidePanelWidthPreferenceSource].
 *
 * Persists the side panel width via [Settings] and exposes
 * a [MutableStateFlow] for reactive observation.
 *
 * @property settings Platform settings store injected by Koin.
 * @see SidePanelWidthPreferenceSource
 */
internal class SidePanelWidthPreferenceSourceImpl(
    private val settings: Settings,
) : SidePanelWidthPreferenceSource {

    /** In-memory state flow initialised with the persisted value on construction. */
    private val _flow = MutableStateFlow(readFromSettings())

    /**
     * Reads the current side panel width from the platform [Settings].
     *
     * Falls back to [SidePanelWidthPreference.DEFAULT_WIDTH_DP] when nothing has been persisted.
     *
     * @return A [SidePanelWidthPreference] with the stored or default width.
     */
    private fun readFromSettings(): SidePanelWidthPreference {
        return SidePanelWidthPreference(
            widthDp = settings.getIntOrNull(KEY_SIDE_PANEL_WIDTH)
                ?: SidePanelWidthPreference.DEFAULT_WIDTH_DP,
        )
    }

    /**
     * Retrieves the current side panel width from persistent storage.
     *
     * @return The latest [SidePanelWidthPreference].
     */
    override suspend fun getData(): SidePanelWidthPreference = readFromSettings()

    /**
     * Persists the given [data] and notifies observers.
     *
     * @param data The [SidePanelWidthPreference] to persist.
     */
    override suspend fun setData(data: SidePanelWidthPreference) {
        settings[KEY_SIDE_PANEL_WIDTH] = data.widthDp
        _flow.value = data
    }

    /**
     * Observes the side panel width as a reactive [Flow].
     *
     * @return A read-only [Flow] emitting [SidePanelWidthPreference] on every change.
     */
    override fun observeData(): Flow<SidePanelWidthPreference> = _flow.asStateFlow()

    private companion object {
        /** Settings key under which the side panel width is stored. */
        const val KEY_SIDE_PANEL_WIDTH = "pref_side_panel_width"
    }
}
