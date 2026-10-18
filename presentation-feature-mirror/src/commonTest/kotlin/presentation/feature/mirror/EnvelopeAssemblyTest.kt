package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.PlatformModel
import kotlinx.coroutines.test.runTest
import presentation.feature.mirror.core.payload.PAYLOAD_FORMAT_VERSION
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.core.payload.cropWindow
import presentation.feature.mirror.core.send.FRAME_FILE_NAME
import presentation.feature.mirror.core.send.assembleEnvelope
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val DEVICE_ID = "RF8M90ABCDE"
private val FRAME_SIZE = IntSize(1080, 2400)
private val FRAME_BYTES = byteArrayOf(1, 2, 3)

/** Non-empty raw dump, distinguishable from [HierarchySnapshotModel.Empty]'s `""`. */
private const val RAW_DUMP = "<hierarchy><node/></hierarchy>"

private fun oneAnnotationFrame(
    promptOverride: String? = null,
    comment: String = "",
): FrozenFrame = FrozenFrame(
    deviceSize = FRAME_SIZE,
    deviceLabel = "Android 15",
    frameId = 7,
    promptOverride = promptOverride,
    annotations = listOf(
        StagedAnnotation(index = 1, rect = DeviceRect(100, 100, 200, 200), comment = comment),
    ),
)

/**
 * The rounded [DeviceRect] `assembleEnvelope` must hand to `encodeCrop` for [rect] on a frame of
 * [frameSize] — computed independently from the production formula's documented contract
 * (`EnvelopeAssembly.kt`'s own KDoc): round each of `.srcOffset` and `.srcOffset + .srcSize`
 * ONCE, never `.dstSize` / `.scale` (bugs.json#PP-TD-1).
 */
private fun expectedCropRect(rect: DeviceRect, frameSize: IntSize): DeviceRect {
    val window = cropWindow(rect, frameSize)
    return DeviceRect(
        left = window.srcOffset.x.roundToInt(),
        top = window.srcOffset.y.roundToInt(),
        right = (window.srcOffset.x + window.srcSize.width).roundToInt(),
        bottom = (window.srcOffset.y + window.srcSize.height).roundToInt(),
    )
}

/**
 * T-6 / TAC-1..TAC-5 (AC-18, AC-32, AS-28, AS-29, FR-17, FR-57): `assembleEnvelope` projects the
 * payload bundle F-6 already computed into the wire [data.handoff.api.source.envelope.AnnotationEnvelope]
 * plus its [data.handoff.api.source.envelope.HandoffImage]s. Every case below asserts "consume,
 * never recompute" per this task's own hypothesis.
 */
class EnvelopeAssemblyTest {

    // -- TAC-1: version is passed through, never recomputed or hardcoded ------------------------

    @Test
    fun versionInTheAssembledEnvelopeEqualsThePayloadFormatVersionConstant() = runTest {
        val frozen = oneAnnotationFrame()

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Absent,
            platform = PlatformModel.Android,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals(PAYLOAD_FORMAT_VERSION, envelope.version)
    }

    @Test
    fun versionIsForwardedVerbatimRatherThanReadFromTheConstantInternally() = runTest {
        // A different literal than PAYLOAD_FORMAT_VERSION -- proves the function echoes its
        // argument instead of silently re-reading the real constant (AS-28).
        val frozen = oneAnnotationFrame()

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Absent,
            platform = PlatformModel.Android,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = 999,
        )

        assertEquals(999, envelope.version)
    }

    // -- TAC-2: promptOverride precedence --------------------------------------------------------

    @Test
    fun promptOverrideWinsOverBuildPromptWhenSet() = runTest {
        val frozen = oneAnnotationFrame(promptOverride = "Hand-typed override text")

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            // A non-empty, Ready, Android hierarchy would change buildPrompt's output -- proves
            // the override wins rather than merely matching by coincidence.
            hierarchy = HierarchySnapshotModel(
                nodes = emptyList(),
                frameWidthPx = FRAME_SIZE.width,
                frameHeightPx = FRAME_SIZE.height,
                densityDpi = 420,
                rawDump = RAW_DUMP,
            ),
            hierarchyStatus = HierarchyStatus.Ready,
            platform = PlatformModel.Android,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals("Hand-typed override text", envelope.prompt)
    }

    @Test
    fun buildPromptIsUsedWhenPromptOverrideIsNull() = runTest {
        val frozen = oneAnnotationFrame(promptOverride = null, comment = "Spacing looks off")

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            // Empty snapshot -- no node ids exist, so buildPrompt(frozen) { null } is exactly
            // what a correct implementation must reproduce with no node-matching of its own.
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Absent,
            platform = PlatformModel.Android,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals(buildPrompt(frozen) { null }, envelope.prompt)
    }

    // -- TAC-3: annotation count and rects survive assembly unchanged ---------------------------

    @Test
    fun annotationCountAndRectsSurviveAssemblyUnchanged() = runTest {
        val annotations = listOf(
            StagedAnnotation(index = 1, rect = DeviceRect(50, 60, 150, 160), comment = "First"),
            StagedAnnotation(index = 2, rect = DeviceRect(300, 400, 500, 700), comment = "Second"),
        )
        val frozen = FrozenFrame(deviceSize = FRAME_SIZE, annotations = annotations)

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Absent,
            platform = PlatformModel.Android,
            // Returns bytes keyed off the padded rect, never the raw annotation rect -- the
            // envelope's own annotation rects must still match the UNPADDED originals.
            encodeCrop = { rect -> byteArrayOf(rect.left.toByte()) },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals(2, envelope.annotations.size)
        annotations.forEach { original ->
            val projected = envelope.annotations.first { it.index == original.index }
            assertEquals(original.rect.left, projected.left)
            assertEquals(original.rect.top, projected.top)
            assertEquals(original.rect.right, projected.right)
            assertEquals(original.rect.bottom, projected.bottom)
            assertEquals(original.comment, projected.comment)
        }
    }

    // -- TAC-4: a null-returning CropEncoder for one annotation does not abort the send ----------

    @Test
    fun nullCropForOneAnnotationYieldsNullCropPathWithoutAbortingTheSend() = runTest {
        val annotations = listOf(
            StagedAnnotation(index = 1, rect = DeviceRect(0, 0, 100, 100), comment = "A"),
            StagedAnnotation(index = 2, rect = DeviceRect(200, 200, 300, 300), comment = "B"),
            StagedAnnotation(index = 3, rect = DeviceRect(400, 400, 500, 500), comment = "C"),
        )
        val frozen = FrozenFrame(deviceSize = FRAME_SIZE, annotations = annotations)
        var calls = 0

        val (envelope, images) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Absent,
            platform = PlatformModel.Android,
            // The SECOND call (annotation #2, in badge order) fails to encode; the other two
            // succeed with content distinguishable by call order.
            encodeCrop = { _ ->
                calls += 1
                if (calls == 2) null else byteArrayOf(calls.toByte())
            },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        // The send is NOT aborted: all three annotations are still present in the envelope.
        assertEquals(3, envelope.annotations.size)

        val first = envelope.annotations.first { it.index == 1 }
        val second = envelope.annotations.first { it.index == 2 }
        val third = envelope.annotations.first { it.index == 3 }

        assertNull(second.cropPath)
        assertEquals("crop-1.png", first.cropPath)
        assertEquals("crop-3.png", third.cropPath)

        // Exactly frame.png plus the two successful crops -- the failed one contributes no image.
        assertEquals(3, images.size)
        assertTrue(images.any { it.name == FRAME_FILE_NAME && it.bytes.contentEquals(FRAME_BYTES) })
        assertTrue(images.any { it.name == "crop-1.png" })
        assertTrue(images.any { it.name == "crop-3.png" })
        assertTrue(images.none { it.name == "crop-2.png" })
    }

    // -- TAC-5: hierarchy branch ------------------------------------------------------------------

    @Test
    fun hierarchyXmlIsEmptyAndUnavailableWhenStatusIsAbsent() = runTest {
        assertHierarchyUnavailable(hierarchyStatus = HierarchyStatus.Absent, platform = PlatformModel.Android)
    }

    @Test
    fun hierarchyXmlIsEmptyAndUnavailableWhenStatusIsUnavailable() = runTest {
        assertHierarchyUnavailable(hierarchyStatus = HierarchyStatus.Unavailable, platform = PlatformModel.Android)
    }

    @Test
    fun hierarchyXmlIsEmptyAndUnavailableWhenTheSnapshotItselfIsEmptyEvenIfStatusSaysReady() = runTest {
        // Defensive per TAC-5's own wording ("the snapshot is Empty/Absent/Unavailable"): a
        // Ready verdict over an Empty snapshot must still degrade, not just a bad verdict.
        val frozen = oneAnnotationFrame()

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Ready,
            platform = PlatformModel.Android,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals("", envelope.hierarchyXml)
        assertEquals(false, envelope.hierarchyAvailable)
    }

    @Test
    fun hierarchyXmlIsEmptyAndUnavailableWhenPlatformIsIosEvenWithAReadySnapshot() = runTest {
        // AS-29: the Android-only hierarchy boundary inherited from F-5/F-6.
        val frozen = oneAnnotationFrame()

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel(
                nodes = emptyList(),
                frameWidthPx = FRAME_SIZE.width,
                frameHeightPx = FRAME_SIZE.height,
                densityDpi = 420,
                rawDump = RAW_DUMP,
            ),
            hierarchyStatus = HierarchyStatus.Ready,
            platform = PlatformModel.Ios,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals("", envelope.hierarchyXml)
        assertEquals(false, envelope.hierarchyAvailable)
    }

    @Test
    fun hierarchyXmlCarriesTheRawDumpVerbatimWhenReadyOnAndroidWithARealSnapshot() = runTest {
        // The positive path, pinned alongside the four degraded ones so a correct Green
        // implementation does not accidentally return false/empty unconditionally.
        val frozen = oneAnnotationFrame()

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel(
                nodes = emptyList(),
                frameWidthPx = FRAME_SIZE.width,
                frameHeightPx = FRAME_SIZE.height,
                densityDpi = 420,
                rawDump = RAW_DUMP,
            ),
            hierarchyStatus = HierarchyStatus.Ready,
            platform = PlatformModel.Android,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals(RAW_DUMP, envelope.hierarchyXml)
        assertEquals(true, envelope.hierarchyAvailable)
    }

    private suspend fun assertHierarchyUnavailable(hierarchyStatus: HierarchyStatus, platform: PlatformModel) {
        val frozen = oneAnnotationFrame()

        val (envelope, _) = assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel(
                nodes = emptyList(),
                frameWidthPx = FRAME_SIZE.width,
                frameHeightPx = FRAME_SIZE.height,
                densityDpi = 420,
                // Non-empty raw dump on purpose: proves the status/platform branch wins even
                // when there WOULD be text to carry.
                rawDump = RAW_DUMP,
            ),
            hierarchyStatus = hierarchyStatus,
            platform = platform,
            encodeCrop = { null },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        assertEquals("", envelope.hierarchyXml)
        assertEquals(false, envelope.hierarchyAvailable)
    }

    // -- bugs.json#PP-TD-1: crop geometry must come from srcOffset/srcSize, never dstSize/scale --

    @Test
    fun cropEncoderReceivesThePaddedSrcOffsetSrcSizeGeometryNeverTheDstSizeScaleOne() = runTest {
        // Chosen so srcSize (~702x702) and dstSize (~72x72) are wildly different -- see
        // CropWindowTest's own "oversized" case for the hand-derived numbers. Mixing up the two
        // is impossible to miss against this fixture.
        val rect = DeviceRect(left = 300, top = 900, right = 900, bottom = 1500)
        val frozen = FrozenFrame(
            deviceSize = FRAME_SIZE,
            annotations = listOf(StagedAnnotation(index = 1, rect = rect, comment = "")),
        )
        var capturedRect: DeviceRect? = null

        assembleEnvelope(
            frozen = frozen,
            frameBytes = FRAME_BYTES,
            hierarchy = HierarchySnapshotModel.Empty,
            hierarchyStatus = HierarchyStatus.Absent,
            platform = PlatformModel.Android,
            encodeCrop = { r -> capturedRect = r; byteArrayOf(1) },
            sentAtMs = 1_760_000_000_000L,
            deviceId = DEVICE_ID,
            version = PAYLOAD_FORMAT_VERSION,
        )

        val expected = expectedCropRect(rect, FRAME_SIZE)
        val window = cropWindow(rect, FRAME_SIZE)

        assertEquals(expected, capturedRect)
        // Sanity: the correct answer is NOT what a dstSize/scale-based rect would look like --
        // if this fails, the fixture stopped discriminating and needs new numbers, not the
        // assembleEnvelope implementation.
        assertTrue(
            window.dstSize.width.roundToInt() != expected.width ||
                window.dstSize.height.roundToInt() != expected.height,
            "fixture no longer discriminates srcSize from dstSize -- pick a new rect",
        )
    }
}
