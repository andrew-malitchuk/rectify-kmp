package presentation.feature.mirror.core.verify

import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.StringResource
import presentation.core.ui.source.kit.atom.annotation.AnnotationState
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlayMark
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.core.ui.source.kit.molecule.status.StatusStep
import presentation.core.ui.source.kit.molecule.status.StatusStepState
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.core.history.metaTime
import presentation.feature.mirror.core.measure.SEPARATOR
import presentation.feature.mirror.source.mirror.VerifyError
import presentation.feature.mirror.source.mirror.VerifyPhase
import presentation.feature.mirror.source.mirror.VerifyRow
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_disconnected_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_disconnected_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_hierarchy_unusable_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_hierarchy_unusable_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_no_live_session_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_no_live_session_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_not_on_disk_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_not_on_disk_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_size_mismatch_body
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_error_size_mismatch_title

/** Which of the two frames a projection is for — the only thing that differs between them. */
internal enum class VerifySide {
    /** The frame as it was sent: every rect neutral, because no verdict existed yet. */
    Before,

    /** The frame as re-captured: each rect takes its own verdict colour. */
    After,
}

/**
 * `BEFORE · 14:02` — a frame column's caption.
 *
 * The clock is dropped entirely when [atMs] is `0`, which is what
 * [presentation.feature.mirror.source.mirror.VerifyUiState.recapturedAtMs] reads until the
 * recapture lands: `AFTER · 01:00` (the epoch in the local zone) would be a timestamp the user
 * could reasonably act on, and it would be a lie.
 *
 * The design's own caption also carries a frame id (`BEFORE · FRAME 18422 · 14:02`). There is
 * none to carry: `frameId` is a freeze counter of the *live* session, and neither the stored
 * batch nor the fresh recapture has one. Two words and a clock, rather than a fabricated
 * number.
 *
 * @param prefix The localised column word, `BEFORE` or `AFTER`.
 * @param atMs When that frame was taken, epoch milliseconds; `0` when it does not exist yet.
 * @param zone Time zone the clock is rendered in.
 */
internal fun frameColumnLabel(
    prefix: String,
    atMs: Long,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): String = if (atMs <= 0L) prefix else "$prefix$SEPARATOR${metaTime(atMs, zone)}"

/**
 * One overlay mark per annotation, positioned from the **measured** frame box.
 *
 * Every rect goes through the one [FrameGeometry] the caller built from `onSizeChanged`, never
 * from the design's nominal 296×640 column: the column is a *box* the frame is letterboxed
 * inside, and the device's aspect ratio is not the mock's, so deriving a rect from the nominal
 * size puts every mark somewhere the pixels are not (bugs.json#PP-TD-1).
 *
 * The badge is `index + 1`: [VerifyRow.index] is history's own 0-based order, and the design
 * numbers the rects from 1.
 *
 * @param geometry Conversion built from the measured frame box.
 * @param rows The verified annotations.
 * @param side Which frame these marks are drawn on.
 */
internal fun verifyMarks(
    geometry: FrameGeometry,
    rows: List<VerifyRow>,
    side: VerifySide,
): List<AnnotationOverlayMark> = rows.map { row ->
    val rect = if (side == VerifySide.Before) row.beforeRect else row.afterRect
    AnnotationOverlayMark(
        bounds = geometry.toDp(geometry.toFrame(rect)),
        state = when {
            side == VerifySide.Before -> AnnotationState.Pinned
            row.outcome == VerificationOutcome.Fixed -> AnnotationState.Sent
            else -> AnnotationState.StillOff
        },
        badge = (row.index + 1).toString(),
    )
}

/**
 * The run's progress, as the shipped [StatusStep] projection — the `sendSteps` precedent.
 *
 * Three steps, not four: [VerifyPhase.Ready] is not a step, it is every step being done, and
 * the panel beside it is the result. The recapture alone costs ~2.7 s (NFR-15), which is why
 * the surface says which of the three it is on rather than showing one undifferentiated
 * spinner.
 *
 * @param phase How far the run has got.
 * @param loadingLabel Localised label for the disk read.
 * @param recapturingLabel Localised label for the hierarchy dump.
 * @param diffingLabel Localised label for the per-annotation diff.
 */
internal fun verifySteps(
    phase: VerifyPhase,
    loadingLabel: String,
    recapturingLabel: String,
    diffingLabel: String,
): List<StatusStep> = listOf(loadingLabel, recapturingLabel, diffingLabel)
    .mapIndexed { index, label ->
        StatusStep(
            label = label,
            state = when {
                index < phase.ordinal -> StatusStepState.Done
                index == phase.ordinal -> StatusStepState.Busy
                else -> StatusStepState.Pending
            },
        )
    }

/**
 * The headline for an aborted run.
 *
 * Five distinct cases, never folded: the whole reason [VerifyError] is an enum rather than a
 * boolean is that "something went wrong" and "start mirroring first" ask the user for
 * completely different things.
 */
internal fun verifyErrorTitleKey(error: VerifyError): StringResource = when (error) {
    VerifyError.NoLiveSession -> Res.string.mirror_verify_error_no_live_session_title
    VerifyError.NotOnDisk -> Res.string.mirror_verify_error_not_on_disk_title
    VerifyError.SizeMismatch -> Res.string.mirror_verify_error_size_mismatch_title
    VerifyError.HierarchyUnusable -> Res.string.mirror_verify_error_hierarchy_unusable_title
    VerifyError.Disconnected -> Res.string.mirror_verify_error_disconnected_title
}

/**
 * What to do about an aborted run.
 *
 * [VerifyError.NoLiveSession]'s body is the one that takes a format argument — the device the
 * batch was captured on, which is the device the user has to start mirroring.
 */
internal fun verifyErrorBodyKey(error: VerifyError): StringResource = when (error) {
    VerifyError.NoLiveSession -> Res.string.mirror_verify_error_no_live_session_body
    VerifyError.NotOnDisk -> Res.string.mirror_verify_error_not_on_disk_body
    VerifyError.SizeMismatch -> Res.string.mirror_verify_error_size_mismatch_body
    VerifyError.HierarchyUnusable -> Res.string.mirror_verify_error_hierarchy_unusable_body
    VerifyError.Disconnected -> Res.string.mirror_verify_error_disconnected_body
}
