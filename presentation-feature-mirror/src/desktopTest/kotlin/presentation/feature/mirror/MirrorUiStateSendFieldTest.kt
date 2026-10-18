package presentation.feature.mirror

import presentation.feature.mirror.source.mirror.MirrorUiState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TAC-2 (T-8): `MirrorUiState` gains **exactly one** new field (`send: SendPhase`); every field
 * that existed before this task keeps its name.
 *
 * Reflection rather than a field-by-field equality check: `MirrorUiState` is a public class, so
 * `java.lang.reflect` sees its real declared fields regardless of declaration order or how the
 * source is formatted — same precedent as `MirrorUiStateStructureTest` (T-1). Synthetic
 * Compose-compiler fields (`$stable`) are excluded, same as that test implicitly does by
 * filtering on type rather than listing every field.
 *
 * Desktop-only: `java.lang.reflect` is JVM-only.
 */
class MirrorUiStateSendFieldTest {

    private val fieldsBeforeThisTask = setOf(
        "platform",
        "tools",
        "discovery",
        "rows",
        "isPickerOpen",
        "highlightedIndex",
        "selectedDeviceId",
        "selectedDeviceName",
        "session",
        "stats",
        "avds",
        "selectedAvd",
        "frozen",
        "measure",
        "hierarchyStatus",
    )

    @Test
    fun exactlyOneFieldIsAddedAndItIsNamedSend() {
        val declaredFieldNames = MirrorUiState::class.java.declaredFields
            .map { it.name }
            .filterNot { it.startsWith("$") }
            .toSet()

        val added = declaredFieldNames - fieldsBeforeThisTask
        val removed = fieldsBeforeThisTask - declaredFieldNames

        assertEquals(setOf("send"), added, "expected exactly one new field named 'send'")
        assertEquals(emptySet(), removed, "no pre-existing field may be removed or renamed")
    }
}
