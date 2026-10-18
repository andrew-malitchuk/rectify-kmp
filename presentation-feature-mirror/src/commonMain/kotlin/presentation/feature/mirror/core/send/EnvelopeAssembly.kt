package presentation.feature.mirror.core.send

import androidx.compose.ui.unit.IntSize
import data.handoff.api.source.envelope.AnnotationEnvelope
import data.handoff.api.source.envelope.EnvelopeAnnotation
import data.handoff.api.source.envelope.EnvelopeDevice
import data.handoff.api.source.envelope.HandoffImage
import domain.core.source.model.device.HierarchySnapshotModel
import domain.core.source.model.device.PlatformModel
import presentation.feature.mirror.core.payload.buildPrompt
import presentation.feature.mirror.core.payload.cropWindow
import presentation.feature.mirror.core.payload.payloadHierarchyRows
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.math.roundToInt

/** Bare file name of the attached full frame, referenced from [AnnotationEnvelope.framePath]. */
internal const val FRAME_FILE_NAME: String = "frame.png"

/** Bare file name of annotation [index]'s crop, referenced from [EnvelopeAnnotation.cropPath]. */
private fun cropFileName(index: Int): String = "crop-$index.png"

/**
 * Projects everything F-6 already computed (prompt, crop geometry, hierarchy status) for the
 * pinned frame into the wire [AnnotationEnvelope] plus the [HandoffImage]s it references.
 * **Zero new business rules beyond version-stamping and hierarchy-availability branching** — see
 * this task's hypothesis; every number below is read from an existing seam, never recomputed.
 *
 * [encodeCrop] is deliberately `suspend (DeviceRect) -> ByteArray?`, not T-5's raw `CropEncoder`:
 * this function has no `ImageBitmap` of its own (the pinned frame's pixels never left
 * [presentation.feature.mirror.source.mirror.MirrorViewModel.frozenHeld]), so the caller (T-9,
 * out of scope here) binds the bitmap once via `{ rect -> cropEncoder.encode(bitmap, rect) }`
 * before calling this function. That keeps `EnvelopeAssembly` provable with a plain lambda in
 * `commonTest`, with no graphics backend required.
 *
 * Crop geometry is read **only** from [presentation.feature.mirror.core.payload.cropWindow]'s
 * `.srcOffset` / `.srcSize` (already device-pixel space). `.dstSize` / `.scale` are preview-only
 * — mixing either into this path is exactly `bugs.json#PP-TD-1`. The padded window is rounded to
 * a [DeviceRect] as `left/top = round(srcOffset)`, `right/bottom = round(srcOffset + srcSize)` —
 * one rounding of each span's total extent, not independent per-edge rounding, so the rounded
 * width/height never drifts a pixel from the rounded edges.
 *
 * A `null` [encodeCrop] result for one annotation yields `cropPath = null` for that annotation
 * only — it never aborts the rest of the send (T-5's own `AS-29`-adjacent contract: the encoder
 * itself never throws, and a `null` is reported exactly like a bad-PNG decode failure).
 *
 * @param frozen The pinned frame — annotations, device size/label, frame id, prompt override.
 * @param frameBytes The original adb-captured PNG for [frozen], from
 *   [presentation.feature.mirror.source.mirror.MirrorViewModel.frozenHeld]. Attached verbatim as
 *   [FRAME_FILE_NAME].
 * @param hierarchy The held snapshot for [frozen], from `MirrorViewModel.frozenHierarchy`.
 * @param hierarchyStatus The reduced verdict for [hierarchy] — distinguishes "no dump" from "an
 *   empty dump" the same way [presentation.feature.mirror.source.mirror.MirrorUiState.hierarchyStatus]
 *   does.
 * @param platform Target platform of the session that produced [frozen]. AS-29: the hierarchy
 *   boundary is Android-only, inherited from F-5/F-6.
 * @param encodeCrop Encodes one already-padded crop region to PNG bytes, or `null` on failure.
 *   See this function's own KDoc above for why this is a bound closure, not a raw `CropEncoder`.
 * @param sentAtMs Wall-clock stamp of the send, from the injected clock seam — also the on-disk
 *   slot's directory name and [mintSessionId]'s third input.
 * @param deviceId Device the session ran on, or `null`/blank when it has already been degraded —
 *   forwarded to [mintSessionId] and to [data.handoff.api.source.envelope.EnvelopeDevice.id]
 *   (AS-28's "unknown" degradation happens inside `mintSessionId`, called exactly once here).
 * @param version Wire format version — [presentation.feature.mirror.core.payload.PAYLOAD_FORMAT_VERSION]
 *   at the one real call site. Passed in, never read directly: the constant stays `internal` to
 *   `core/payload/PayloadEstimate.kt` and is never re-exported or moved (AS-28).
 * @return The assembled envelope paired with every [HandoffImage] it references — the frame plus
 *   one crop per annotation whose [encodeCrop] call succeeded.
 */
internal suspend fun assembleEnvelope(
    frozen: FrozenFrame,
    frameBytes: ByteArray,
    hierarchy: HierarchySnapshotModel,
    hierarchyStatus: HierarchyStatus,
    platform: PlatformModel,
    encodeCrop: suspend (DeviceRect) -> ByteArray?,
    sentAtMs: Long,
    deviceId: String?,
    version: Int,
): Pair<AnnotationEnvelope, List<HandoffImage>> {
    // Android-only (AS-29) and only for a verdict that says a real dump is held. An Empty
    // snapshot carries `rawDump == ""`, so a stale Ready verdict over it degrades for free
    // rather than through a second branch.
    val hierarchyXml =
        if (platform == PlatformModel.Android && hierarchyStatus == HierarchyStatus.Ready) {
            hierarchy.rawDump
        } else {
            ""
        }

    val images = mutableListOf(HandoffImage(name = FRAME_FILE_NAME, bytes = frameBytes))
    val annotations = frozen.annotations.map { annotation ->
        // A null encode is this one annotation's crop going missing, never the send failing.
        val bytes = encodeCrop(paddedCropRect(annotation.rect, frozen.deviceSize))
        val name = bytes?.let { cropFileName(annotation.index) }
        if (bytes != null && name != null) images += HandoffImage(name = name, bytes = bytes)
        EnvelopeAnnotation(
            index = annotation.index,
            left = annotation.rect.left,
            top = annotation.rect.top,
            right = annotation.rect.right,
            bottom = annotation.rect.bottom,
            comment = annotation.comment,
            cropPath = name,
        )
    }

    val envelope = AnnotationEnvelope(
        version = version,
        sessionId = mintSessionId(deviceId, frozen.frameId, sentAtMs),
        sentAtMs = sentAtMs,
        device = EnvelopeDevice(
            id = deviceId?.takeIf { it.isNotBlank() } ?: UNKNOWN_DEVICE_ID,
            label = frozen.deviceLabel,
            widthPx = frozen.deviceSize.width,
            heightPx = frozen.deviceSize.height,
        ),
        prompt = frozen.promptOverride ?: buildPrompt(frozen, nodeIdResolver(frozen, hierarchy)),
        hierarchyXml = hierarchyXml,
        hierarchyAvailable = hierarchyXml.isNotEmpty(),
        framePath = FRAME_FILE_NAME,
        annotations = annotations,
    )
    return envelope to images
}

/**
 * The padded source window for [rect], rounded to whole device pixels.
 *
 * Reads only `.srcOffset` / `.srcSize` — `.dstSize` / `.scale` are the Payload tab's preview
 * geometry and would ship a cell-sized thumbnail to the agent (`bugs.json#PP-TD-1`). Each span's
 * total extent is rounded once (offset, and offset + size) so the rounded width never drifts a
 * pixel from the rounded edges.
 */
private fun paddedCropRect(rect: DeviceRect, frameSize: IntSize): DeviceRect {
    val window = cropWindow(rect, frameSize)
    return DeviceRect(
        left = window.srcOffset.x.roundToInt(),
        top = window.srcOffset.y.roundToInt(),
        right = (window.srcOffset.x + window.srcSize.width).roundToInt(),
        bottom = (window.srcOffset.y + window.srcSize.height).roundToInt(),
    )
}

/**
 * Names each annotation's snapped node for [buildPrompt], exactly as the Payload tab's preview
 * does — the badge on a [payloadHierarchyRows] row IS the annotation index that landed on it, so
 * the prompt sent matches the prompt previewed with no second snapping pass here.
 */
private fun nodeIdResolver(
    frozen: FrozenFrame,
    hierarchy: HierarchySnapshotModel,
): (StagedAnnotation) -> String? {
    val rows = payloadHierarchyRows(hierarchy, frozen.deviceSize, frozen.annotations.map { it.rect })
    return { annotation -> rows.firstOrNull { it.badge == annotation.index }?.nodeId }
}
