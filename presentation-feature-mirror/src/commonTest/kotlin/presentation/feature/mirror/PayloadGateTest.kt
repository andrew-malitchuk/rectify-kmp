package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import presentation.feature.mirror.core.payload.hasPayload
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.StagedAnnotation

/**
 * AC-77: the Payload tab shows only its empty-state copy until something is actually staged.
 *
 * The three states below are the three the tab can be opened in, and they are distinct for
 * different reasons — a live stream, a pinned frame that was just cleared, and a real payload.
 * The middle one is the case an `isEmpty()`-only check would get wrong.
 */
class PayloadGateTest {

    @Test
    fun liveStreamWithNoPinnedFrameHasNoPayload() {
        assertFalse(hasPayload(null))
    }

    @Test
    fun pinnedFrameWithNoStagedAnnotationsHasNoPayload() {
        // The moment after ClearStaged: still frozen, but a payload here would carry a prompt
        // asking to fix nothing and a "0 crops" footer.
        val frozen = FrozenFrame(deviceSize = IntSize(width = 1080, height = 2400))
        assertFalse(hasPayload(frozen))
    }

    @Test
    fun pinnedFrameWithOneStagedAnnotationHasPayload() {
        val frozen = FrozenFrame(
            deviceSize = IntSize(width = 1080, height = 2400),
            annotations = listOf(
                StagedAnnotation(
                    index = 1,
                    rect = DeviceRect(left = 120, top = 420, right = 960, bottom = 674),
                ),
            ),
        )
        assertTrue(hasPayload(frozen))
    }
}
