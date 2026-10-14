package data.preference.impl.source.datasource

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import data.preference.api.source.datasource.UnitSystemPreferenceSource
import data.preference.api.source.model.UnitSystemPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class UnitSystemPreferenceSourceImpl(
    private val settings: Settings,
) : UnitSystemPreferenceSource {

    private val _flow = MutableStateFlow(readFromSettings())

    private fun readFromSettings(): UnitSystemPreference =
        UnitSystemPreference(
            unitSystem = settings.getStringOrNull(KEY_UNIT_SYSTEM) ?: UnitSystemPreference.DEFAULT_UNIT_SYSTEM,
        )

    override suspend fun getData(): UnitSystemPreference = readFromSettings()

    override suspend fun setData(data: UnitSystemPreference) {
        settings[KEY_UNIT_SYSTEM] = data.unitSystem
        _flow.value = data
    }

    override fun observeData(): Flow<UnitSystemPreference> = _flow.asStateFlow()

    private companion object {
        const val KEY_UNIT_SYSTEM = "pref_unit_system"
    }
}
