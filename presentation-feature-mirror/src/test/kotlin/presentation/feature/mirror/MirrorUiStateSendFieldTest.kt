package presentation.feature.mirror

import presentation.feature.mirror.source.mirror.MirrorUiState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `MirrorUiState` gains **exactly one** new field per task, and no earlier field is ever
 * renamed or dropped.
 *
 * Written for T-8's `send: SendPhase` (TAC-2), re-baselined by F-8's T-9 for
 * `history: List<HistoryEntryModel>` — the only field in this class that carries bytes — and
 * again by F-9's T-7 for `verify: VerifyUiState?`, which deliberately carries none: its two
 * bitmaps ride a separate `StateFlow`. Re-baselining is the intended maintenance
 * of this test: the invariant it guards is "one considered field at a time, nothing renamed",
 * not "this class is frozen at fifteen fields".
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
        "send",
        "history",
    )

    @Test
    fun exactlyOneFieldIsAddedAndItIsNamedVerify() {
        val declaredFieldNames = MirrorUiState::class.java.declaredFields
            .map { it.name }
            .filterNot { it.startsWith("$") }
            .toSet()

        val added = declaredFieldNames - fieldsBeforeThisTask
        val removed = fieldsBeforeThisTask - declaredFieldNames

        assertEquals(setOf("verify"), added, "expected exactly one new field named 'verify'")
        assertEquals(emptySet(), removed, "no pre-existing field may be removed or renamed")
    }
}
