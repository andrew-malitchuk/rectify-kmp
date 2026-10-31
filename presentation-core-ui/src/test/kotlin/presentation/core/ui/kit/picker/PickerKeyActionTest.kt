package presentation.core.ui.kit.picker

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import presentation.core.ui.source.kit.organism.picker.PickerKeyAction
import presentation.core.ui.source.kit.organism.picker.pickerKeyAction

/** A11Y-4 and AC-43: the picker is keyboard-navigable AND keyboard-dismissable. */
class PickerKeyActionTest {

    @Test
    fun `escape closes the picker`() {
        // The one key that used to have no handler at all, which made the panel a one-way
        // door: the only exit was Enter, and Enter starts a session.
        assertEquals(PickerKeyAction.Close, pickerKeyAction(Key.Escape))
    }

    @Test
    fun `arrows move the cursor and enter commits`() {
        assertEquals(PickerKeyAction.MoveUp, pickerKeyAction(Key.DirectionUp))
        assertEquals(PickerKeyAction.MoveDown, pickerKeyAction(Key.DirectionDown))
        assertEquals(PickerKeyAction.Start, pickerKeyAction(Key.Enter))
        assertEquals(PickerKeyAction.Start, pickerKeyAction(Key.NumPadEnter))
    }

    @Test
    fun `every other key bubbles`() {
        assertNull(pickerKeyAction(Key.Tab))
        assertNull(pickerKeyAction(Key.Spacebar))
        assertNull(pickerKeyAction(Key.A))
        assertNull(pickerKeyAction(Key.One))
    }
}
