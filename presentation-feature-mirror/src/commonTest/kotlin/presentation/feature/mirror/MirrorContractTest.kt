package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MirrorContractTest {

    private fun frozen() = FrozenFrame(
        deviceSize = IntSize(1080, 2400),
        annotations = listOf(StagedAnnotation(index = 1, rect = DeviceRect(10, 20, 30, 40))),
    )

    @Test
    fun resumeLiveIsNotOfferedWhileTheSessionIsLost() {
        val streaming = MirrorUiState(
            frozen = frozen(),
            session = MirrorSession.Streaming(deviceId = "emulator-5554"),
        )
        assertTrue(streaming.canResumeLive)

        val lost = streaming.copy(session = MirrorSession.Lost(deviceId = "emulator-5554"))
        assertFalse(lost.canResumeLive)

        assertFalse(streaming.copy(frozen = null).canResumeLive)
    }
}
