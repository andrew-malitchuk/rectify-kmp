package data.preference.impl

import com.russhwolf.settings.MapSettings
import data.preference.api.source.model.AdbPathPreference
import data.preference.impl.source.datasource.AdbPathPreferenceSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the persistence contract of `pref_adb_path` against an in-memory [MapSettings].
 * An unset path must read as an empty string so an existing install keeps probing the
 * usual locations, and a written path must survive the relaunch proxy — a fresh source
 * over the same backing store.
 */
class AdbPathPreferenceSourceTest {

    @Test
    fun `unset path reads as empty`() = runTest {
        val source = AdbPathPreferenceSourceImpl(MapSettings())

        assertEquals(AdbPathPreference(""), source.getData())
        assertEquals(AdbPathPreference(""), source.observeData().first())
    }

    @Test
    fun `adb path round trips through MapSettings`() = runTest {
        val source = AdbPathPreferenceSourceImpl(MapSettings())

        source.setData(AdbPathPreference("/opt/homebrew/bin/adb"))

        assertEquals(AdbPathPreference("/opt/homebrew/bin/adb"), source.getData())
        assertEquals(AdbPathPreference("/opt/homebrew/bin/adb"), source.observeData().first())
    }

    @Test
    fun `a fresh source over the same settings sees the stored path`() = runTest {
        val settings = MapSettings()
        AdbPathPreferenceSourceImpl(settings).setData(AdbPathPreference("/usr/local/bin/adb"))

        val relaunched = AdbPathPreferenceSourceImpl(settings)

        assertEquals(AdbPathPreference("/usr/local/bin/adb"), relaunched.getData())
        assertEquals(AdbPathPreference("/usr/local/bin/adb"), relaunched.observeData().first())
    }

    @Test
    fun `the storage key is pref_adb_path`() = runTest {
        val settings = MapSettings()

        AdbPathPreferenceSourceImpl(settings).setData(AdbPathPreference("/usr/bin/adb"))

        assertEquals("/usr/bin/adb", settings.getStringOrNull("pref_adb_path"))
    }
}
