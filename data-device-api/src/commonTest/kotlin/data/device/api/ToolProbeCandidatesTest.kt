package data.device.api

import data.device.api.source.model.ToolKind
import data.device.api.source.tool.probeCandidates
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the packaged app risk without touching a filesystem: a GUI process inherits a
 * minimal PATH, so the SDK and Homebrew locations must be probed in a fixed order first.
 */
class ToolProbeCandidatesTest {

    private val home = "/Users/dev"

    @Test
    fun `an empty environment falls back to the default sdk location and homebrew`() {
        assertEquals(
            listOf(
                "/Users/dev/Library/Android/sdk/platform-tools/adb",
                "/opt/homebrew/bin/adb",
                "/usr/local/bin/adb",
            ),
            probeCandidates(ToolKind.Adb, emptyMap(), home),
        )
    }

    @Test
    fun `ANDROID_HOME is probed first`() {
        assertEquals(
            listOf(
                "/opt/sdk/platform-tools/adb",
                "/Users/dev/Library/Android/sdk/platform-tools/adb",
                "/opt/homebrew/bin/adb",
                "/usr/local/bin/adb",
            ),
            probeCandidates(ToolKind.Adb, mapOf("ANDROID_HOME" to "/opt/sdk"), home),
        )
    }

    @Test
    fun `ANDROID_SDK_ROOT is probed after ANDROID_HOME`() {
        assertEquals(
            listOf(
                "/opt/sdk/platform-tools/adb",
                "/srv/sdk/platform-tools/adb",
                "/Users/dev/Library/Android/sdk/platform-tools/adb",
                "/opt/homebrew/bin/adb",
                "/usr/local/bin/adb",
            ),
            probeCandidates(
                ToolKind.Adb,
                mapOf("ANDROID_HOME" to "/opt/sdk", "ANDROID_SDK_ROOT" to "/srv/sdk"),
                home,
            ),
        )
    }

    @Test
    fun `a homebrew only machine probes just the two brew prefixes`() {
        assertEquals(
            listOf("/opt/homebrew/bin/adb", "/usr/local/bin/adb"),
            probeCandidates(ToolKind.Adb, emptyMap(), homeDir = ""),
        )
    }

    @Test
    fun `PATH entries come last and are not repeated`() {
        assertEquals(
            listOf("/opt/homebrew/bin/adb", "/usr/local/bin/adb", "/usr/bin/adb"),
            probeCandidates(
                ToolKind.Adb,
                mapOf("PATH" to "/opt/homebrew/bin:/usr/bin:/usr/local/bin/"),
                homeDir = "",
            ),
        )
    }

    @Test
    fun `the emulator binary lives outside platform tools`() {
        assertEquals(
            listOf(
                "/opt/sdk/emulator/emulator",
                "/Users/dev/Library/Android/sdk/emulator/emulator",
                "/opt/homebrew/bin/emulator",
                "/usr/local/bin/emulator",
            ),
            probeCandidates(ToolKind.Emulator, mapOf("ANDROID_HOME" to "/opt/sdk"), home),
        )
    }

    @Test
    fun `a tool that does not ship in the sdk skips the sdk roots`() {
        assertEquals(
            listOf("/opt/homebrew/bin/idb", "/usr/local/bin/idb"),
            probeCandidates(ToolKind.Idb, mapOf("ANDROID_HOME" to "/opt/sdk"), home),
        )
    }
}
