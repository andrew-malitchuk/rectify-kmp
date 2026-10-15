package data.device.api

import data.device.api.source.parse.parseAvdList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pins the `emulator -list-avds` contract, log lines included. */
class AvdListParserTest {

    @Test
    fun `parseAvdList reads every configured image`() {
        val output = "Pixel_7_API_34\nPixel_Fold_API_33\n"
        assertEquals(listOf("Pixel_7_API_34", "Pixel_Fold_API_33"), parseAvdList(output).map { it.value })
    }

    @Test
    fun `parseAvdList drops the log lines the emulator interleaves`() {
        val output = """
            INFO | Storing crashdata in the temporary directory
            Pixel_7_API_34

            WARNING | encryption is off
        """.trimIndent()
        assertEquals(listOf("Pixel_7_API_34"), parseAvdList(output).map { it.value })
    }

    @Test
    fun `parseAvdList returns nothing for empty output`() {
        assertTrue(parseAvdList("").isEmpty())
        assertTrue(parseAvdList("\n\n").isEmpty())
    }
}
