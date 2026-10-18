package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.chip.Chip
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlay
import presentation.core.ui.source.kit.molecule.mirror.MirrorFrameView
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.core.ui.source.kit.molecule.row.VerificationRow
import presentation.core.ui.source.kit.molecule.state.ErrorState
import presentation.core.ui.source.kit.molecule.status.StatusLine
import presentation.feature.mirror.core.geometry.FrameGeometry
import presentation.feature.mirror.core.verify.VerifySide
import presentation.feature.mirror.core.verify.frameColumnLabel
import presentation.feature.mirror.core.verify.verifyErrorBodyKey
import presentation.feature.mirror.core.verify.verifyErrorTitleKey
import presentation.feature.mirror.core.verify.verifyMarks
import presentation.feature.mirror.core.verify.verifySteps
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_back_to_live
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_column_after
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_column_before
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_mark_all
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_outcome_fixed
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_outcome_still_off
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_panel_title
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_phase_diffing
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_phase_loading
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_phase_recapturing
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_send_follow_up
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_verify_summary

/**
 * The design's frame column, `296×640`, and the panel beside it, `300`.
 *
 * The three figures the verify view is drawn to that no token expresses, named here on the
 * `MeasureCardWidth` / `ChipHeight` precedent rather than inlined at a call site. The column is
 * a **box the frame is letterboxed inside**, never the frame's own size — see
 * [VerifyFrameColumn].
 * The height is a ceiling: a short window shrinks the box and the letterbox follows it, which is
 * exactly why no overlay coordinate is ever derived from these constants.
 */
private val FrameColumnWidth: Dp = 296.dp
private val FrameColumnMaxHeight: Dp = 640.dp
private val PanelWidth: Dp = 300.dp

/** The 6 px dot that precedes a column caption. */
private val CaptionDotSize: Dp = 6.dp

/**
 * The keyboard shortcut printed on the follow-up button.
 *
 * Machine text, like every other key hint in this app, and true: `RectifyWindow` points its
 * ⌘↩ holder at [MirrorIntent.SendFollowUp] whenever this stage has something to re-send.
 */
private const val FOLLOW_UP_SHORTCUT = "⌘↵"

/**
 * The verification view: the two frames it was decided on, and the decision (AC-23/AC-24).
 *
 * Stateless. Everything semantic arrives in [verify]; the two bitmaps arrive separately in
 * [frames] because pixels never enter the reduced state (`CLAUDE.md` deviation 2), and the one
 * piece of state this file owns is the **measured** size of each painted frame box.
 *
 * Three mutually exclusive surfaces, in the order a run reaches them:
 * - an aborted run shows [ErrorState] and nothing else — [VerifyUiState] guarantees a
 *   non-`null` error carries no rows, so a panel here could only show `0 of 0 fixed` beside a
 *   failure, which reads as a verdict;
 * - a run still working shows the three-step [StatusLine];
 * - a finished run shows the frames and the panel.
 *
 * The `← Back to live` chip is the single exit and sits over all three, because the error and
 * the progress states are exactly where a user most needs a way out. It dispatches
 * [MirrorIntent.ExitVerify], which already cancels the run and clears every field — there is no
 * second teardown here that could drift from it.
 *
 * Cut from the design on purpose, both for want of a data source rather than for want of time:
 * the four-step `Sent → Agent fixed · 3 files → Recaptured → Verified 2/3` strip and the
 * `AGENT CHANGES` file list. Nothing in this app knows which files the agent touched — the same
 * call F-8 made on its activity sub-line.
 *
 * @param verify The run in flight or on screen.
 * @param frames The BEFORE/AFTER pair, or `null` until the run has both.
 * @param onIntent Intent sink.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
internal fun MirrorVerifyStage(
    verify: VerifyUiState,
    frames: VerifyFrames?,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when {
                verify.error != null -> VerifyErrorState(
                    error = verify.error,
                    deviceLabel = verify.deviceLabel,
                )

                frames == null -> VerifyProgress(
                    phase = verify.phase,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> VerifyFrameColumns(verify = verify, frames = frames)
            }
            Chip(
                label = stringResource(Res.string.mirror_verify_back_to_live),
                selected = false,
                onClick = { onIntent(MirrorIntent.ExitVerify) },
                modifier = Modifier.align(Alignment.TopStart).padding(Theme.spacing.spacingL),
            )
        }
        if (verify.error == null) {
            VerificationPanel(verify = verify, onIntent = onIntent)
        }
    }
}

/** The two columns, `BEFORE` on the left, side by side with the design's 56 dp between them. */
@Composable
private fun VerifyFrameColumns(verify: VerifyUiState, frames: VerifyFrames) {
    val gap = Theme.spacing.spacing3XL + Theme.spacing.spacingS
    Row(
        modifier = Modifier.fillMaxSize().padding(Theme.spacing.spacingXL),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
    ) {
        VerifyFrameColumn(
            caption = frameColumnLabel(
                prefix = stringResource(Res.string.mirror_verify_column_before),
                atMs = verify.sentAtMs,
            ),
            // Neutral, like the rects on it: the BEFORE frame predates every verdict.
            dotColor = Theme.color.ink3,
            frame = frames.before,
            rows = verify.rows,
            side = VerifySide.Before,
            deviceLabel = verify.deviceLabel,
        )
        VerifyFrameColumn(
            caption = frameColumnLabel(
                prefix = stringResource(Res.string.mirror_verify_column_after),
                atMs = verify.recapturedAtMs,
            ),
            dotColor = Theme.color.sentFixed,
            frame = frames.after,
            rows = verify.rows,
            side = VerifySide.After,
            deviceLabel = verify.deviceLabel,
        )
    }
}

/**
 * One captioned frame with its annotation rects on it.
 *
 * [MirrorFrameView] letterboxes the frame inside this column's box and composes [overlay]
 * *inside* the aspect-fit result, so the box the overlay measures with `onSizeChanged` is
 * literally the painted rectangle. That measured size is the only input [FrameGeometry] gets —
 * the nominal [FrameColumnWidth]/[FrameColumnMaxHeight] never reach a coordinate, which is what
 * keeps a device whose aspect ratio is not the mock's from putting every rect in the wrong
 * place (bugs.json#PP-TD-1).
 */
@Composable
private fun VerifyFrameColumn(
    caption: String,
    dotColor: Color,
    frame: ImageBitmap,
    rows: List<VerifyRow>,
    side: VerifySide,
    deviceLabel: String,
) {
    val shape = Theme.cornerToken.badge
    val captionGap = Theme.spacing.spacingS + Theme.spacing.spacingXS
    Column(
        modifier = Modifier.width(FrameColumnWidth).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(captionGap),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(CaptionDotSize).clip(CircleShape).background(dotColor))
            Text(text = caption, style = Theme.typography.monoMeta, color = Theme.color.ink2)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(max = FrameColumnMaxHeight)
                .clip(shape)
                .border(width = Theme.stroke.thin, color = Theme.color.line2, shape = shape),
        ) {
            MirrorFrameView(frame = frame, deviceName = deviceLabel) {
                var framePx by remember { mutableStateOf(Size.Zero) }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .onSizeChanged { size -> framePx = size.toSize() },
                ) {
                    val geometry = FrameGeometry(
                        framePx = framePx,
                        deviceSize = IntSize(width = frame.width, height = frame.height),
                        density = LocalDensity.current.density,
                    )
                    AnnotationOverlay(
                        marks = verifyMarks(geometry = geometry, rows = rows, side = side),
                    )
                }
            }
        }
    }
}

/** `Loading batch › Recapturing › Diffing regions` while the run works (NFR-15). */
@Composable
private fun VerifyProgress(phase: VerifyPhase, modifier: Modifier = Modifier) {
    StatusLine(
        steps = verifySteps(
            phase = phase,
            loadingLabel = stringResource(Res.string.mirror_verify_phase_loading),
            recapturingLabel = stringResource(Res.string.mirror_verify_phase_recapturing),
            diffingLabel = stringResource(Res.string.mirror_verify_phase_diffing),
        ),
        modifier = modifier.widthIn(max = FrameColumnWidth * 2),
    )
}

/** An aborted run: what stopped it, and what the user can do about it. */
@Composable
private fun VerifyErrorState(error: VerifyError, deviceLabel: String) {
    val bodyKey = verifyErrorBodyKey(error)
    ErrorState(
        title = stringResource(verifyErrorTitleKey(error)),
        // The one case whose copy names the device, because the fix is to start mirroring it.
        description = if (error == VerifyError.NoLiveSession) {
            stringResource(bodyKey, deviceLabel)
        } else {
            stringResource(bodyKey)
        },
        modifier = Modifier.padding(Theme.spacing.spacing2XL),
    )
}

/**
 * The 300 dp result panel: `n of m fixed`, one [VerificationRow] per annotation, two actions.
 *
 * `Send follow-up · N` is **hard-disabled** at `N == 0`, not dimmed: with nothing still off
 * there is nothing to re-send, and a click that silently stages an empty batch is worse than a
 * button that says no.
 */
@Composable
private fun VerificationPanel(verify: VerifyUiState, onIntent: (MirrorIntent) -> Unit) {
    val stillOff = verify.stillOffRows.size
    val fixedLabel = stringResource(Res.string.mirror_verify_outcome_fixed)
    val stillOffLabel = stringResource(Res.string.mirror_verify_outcome_still_off)
    Box(modifier = Modifier.fillMaxHeight().width(Theme.stroke.thin).background(Theme.color.line))
    Column(modifier = Modifier.width(PanelWidth).fillMaxHeight().background(Theme.color.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme.spacing.spacing2XL + Theme.spacing.spacingS + Theme.spacing.spacingXS)
                .padding(horizontal = Theme.spacing.spacingS + Theme.spacing.spacingXS),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.mirror_verify_panel_title),
                style = Theme.typography.bodyStrong,
                color = Theme.color.ink,
            )
            Text(
                text = stringResource(
                    Res.string.mirror_verify_summary,
                    verify.fixedCount,
                    verify.rowCount,
                ),
                style = Theme.typography.monoMeta,
                color = Theme.color.ink2,
            )
        }
        PanelRule()
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            verify.rows.forEach { row ->
                VerificationRow(
                    title = row.comment,
                    // Machine measurement, built by core/verify. Never localised.
                    delta = row.resultLine,
                    outcome = row.outcome,
                    outcomeLabel = if (row.outcome == VerificationOutcome.Fixed) {
                        fixedLabel
                    } else {
                        stillOffLabel
                    },
                )
            }
        }
        PanelRule()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Theme.spacing.spacingS + Theme.spacing.spacingXS),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            AppButton(
                text = stringResource(Res.string.mirror_verify_send_follow_up, stillOff),
                onClick = { onIntent(MirrorIntent.SendFollowUp) },
                size = AppButtonSize.Dialog,
                shortcut = FOLLOW_UP_SHORTCUT,
                fillMaxWidth = true,
                enabled = stillOff > 0,
            )
            AppButton(
                text = stringResource(Res.string.mirror_verify_mark_all),
                onClick = { onIntent(MirrorIntent.MarkAllVerified) },
                variant = AppButtonVariant.Ghost,
                fillMaxWidth = true,
            )
        }
    }
}

/** The panel's hairline rules, under the header and over the footer. */
@Composable
private fun PanelRule() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Theme.stroke.thin)
            .background(Theme.color.line),
    )
}

/** Preview-only stand-ins; a real screenshot is never available in a preview. */
private fun previewFrame(): ImageBitmap = ImageBitmap(width = 1080, height = 2400)

private fun previewVerify(): VerifyUiState = VerifyUiState(
    batchId = "37281FDH2000AB-f3-1760000000000",
    deviceLabel = "Pixel 8 Pro",
    sentAtMs = 1_760_000_520_000,
    recapturedAtMs = 1_760_000_940_000,
    phase = VerifyPhase.Ready,
    rows = listOf(
        VerifyRow(
            index = 0,
            comment = "Price sits 6px below the title",
            beforeRect = DeviceRect(left = 120, top = 420, right = 960, bottom = 674),
            afterRect = DeviceRect(left = 120, top = 414, right = 960, bottom = 668),
            resultLine = "top Δ6dp → 0dp",
            outcome = VerificationOutcome.Fixed,
        ),
        VerifyRow(
            index = 1,
            comment = "Stepper hit target",
            beforeRect = DeviceRect(left = 80, top = 1500, right = 298, bottom = 1577),
            afterRect = DeviceRect(left = 80, top = 1500, right = 298, bottom = 1602),
            resultLine = "height 24dp → 32dp · want 48dp",
            outcome = VerificationOutcome.StillOff,
        ),
    ),
)

@Composable
private fun MirrorVerifyStageReadyPreviewContent() {
    Box(modifier = Modifier.background(Theme.color.stage)) {
        MirrorVerifyStage(
            verify = previewVerify(),
            frames = VerifyFrames(before = previewFrame(), after = previewFrame()),
            onIntent = {},
        )
    }
}

@Composable
private fun MirrorVerifyStageErrorPreviewContent() {
    Box(modifier = Modifier.background(Theme.color.stage)) {
        MirrorVerifyStage(
            verify = VerifyUiState(
                batchId = "37281FDH2000AB-f3-1760000000000",
                deviceLabel = "Pixel 8 Pro",
                sentAtMs = 1_760_000_520_000,
                error = VerifyError.NoLiveSession,
            ),
            frames = null,
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun MirrorVerifyStageReadyPreview() {
    AppTheme { MirrorVerifyStageReadyPreviewContent() }
}

@Preview
@Composable
private fun MirrorVerifyStageReadyDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) { MirrorVerifyStageReadyPreviewContent() }
}

@Preview
@Composable
private fun MirrorVerifyStageErrorPreview() {
    AppTheme { MirrorVerifyStageErrorPreviewContent() }
}

@Preview
@Composable
private fun MirrorVerifyStageErrorDarkPreview() {
    AppTheme(mode = ThemeMode.Dark) { MirrorVerifyStageErrorPreviewContent() }
}
