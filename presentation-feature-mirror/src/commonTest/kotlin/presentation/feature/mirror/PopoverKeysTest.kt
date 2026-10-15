package presentation.feature.mirror

import androidx.compose.ui.input.key.Key
import presentation.feature.mirror.core.annotation.closesPopover
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PopoverKeysTest {

    @Test
    fun enterAndEscapeCloseThePopover() {
        assertTrue(closesPopover(isMeta = false, key = Key.Enter))
        assertTrue(closesPopover(isMeta = false, key = Key.NumPadEnter))
        assertTrue(closesPopover(isMeta = false, key = Key.Escape))
    }

    @Test
    fun aBareDigitIsTypedIntoTheFieldInstead() {
        assertFalse(closesPopover(isMeta = false, key = Key.One))
        assertFalse(closesPopover(isMeta = false, key = Key.Two))
        assertFalse(closesPopover(isMeta = false, key = Key.Three))
    }

    @Test
    fun theWindowKeepsEveryMetaShortcut() {
        assertFalse(closesPopover(isMeta = true, key = Key.One))
        assertFalse(closesPopover(isMeta = true, key = Key.Two))
        assertFalse(closesPopover(isMeta = true, key = Key.Three))
        assertFalse(closesPopover(isMeta = true, key = Key.Enter))
        assertFalse(closesPopover(isMeta = true, key = Key.Escape))
    }
}
