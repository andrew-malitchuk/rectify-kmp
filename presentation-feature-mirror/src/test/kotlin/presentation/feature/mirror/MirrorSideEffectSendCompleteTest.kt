package presentation.feature.mirror

import presentation.feature.mirror.source.mirror.MirrorSideEffect
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TAC-3 (T-8): `MirrorSideEffect.SendComplete` carries `count`, `summary` and `sessionId`, so the
 * host can show the toast and copy the session id to the clipboard without querying the
 * ViewModel for anything else — the same reasoning as the shipped
 * [MirrorSideEffect.LocateAdb].
 */
class MirrorSideEffectSendCompleteTest {

    @Test
    fun sendCompleteCarriesCountSummaryAndSessionId() {
        val effect = MirrorSideEffect.SendComplete(
            count = 3,
            summary = "3 annotations sent",
            sessionId = "sess-7f2c",
        )

        assertEquals(3, effect.count)
        assertEquals("3 annotations sent", effect.summary)
        assertEquals("sess-7f2c", effect.sessionId)
    }
}
