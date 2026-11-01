package presentation.feature.mirror

import androidx.compose.ui.graphics.ImageBitmap
import presentation.feature.mirror.source.mirror.MirrorUiState
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * T-1 / TAC-3 — `HeldFrame` (and raw pixels generally) must never enter the reduced state, per
 * `presentation-feature-mirror/CLAUDE.md` deviation 2.
 *
 * Reflection rather than source-text matching: `MirrorUiState` is a public class, so
 * `java.lang.reflect` sees its real declared fields regardless of how the file is formatted or
 * commented — unlike a grep over the source text, this survives a refactor that moves or
 * reorders properties, and would catch a `ByteArray`/`ImageBitmap` field added under any name.
 *
 * Uses `java.lang.reflect`.
 */
class MirrorUiStateStructureTest {

    @Test
    fun mirrorUiStateCarriesNoFrameBytesOrBitmap() {
        val forbidden = setOf(ByteArray::class.java, ImageBitmap::class.java)
        val offenders = MirrorUiState::class.java.declaredFields
            .filter { it.type in forbidden }
            .map { "${it.name}: ${it.type.simpleName}" }

        assertTrue(
            offenders.isEmpty(),
            "MirrorUiState must stay pixel-free (deviation 2) - found: $offenders",
        )
    }
}
