package data.repository.impl

import data.preference.api.source.datasource.AdbPathPreferenceSource
import data.preference.api.source.model.AdbPathPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * In-memory stand-in for the user-chosen `adb` path.
 *
 * Lives in its own file because both device repository tests need it, and recording [written]
 * is what lets `setAdbPath`'s pass-through be checked without a settings backend.
 */
internal class FakeAdbPathPreferenceSource(
    private var current: AdbPathPreference = AdbPathPreference(),
) : AdbPathPreferenceSource {

    val written: MutableList<AdbPathPreference> = mutableListOf()

    override suspend fun getData(): AdbPathPreference = current

    override suspend fun setData(data: AdbPathPreference) {
        current = data
        written += data
    }

    override fun observeData(): Flow<AdbPathPreference> = flowOf(current)
}
