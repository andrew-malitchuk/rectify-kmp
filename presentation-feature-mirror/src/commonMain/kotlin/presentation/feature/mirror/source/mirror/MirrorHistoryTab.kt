package presentation.feature.mirror.source.mirror

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.core.ThemeMode
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.molecule.row.HistoryRow
import presentation.feature.mirror.core.history.HistoryBucket
import presentation.feature.mirror.core.history.groupHistory
import presentation.feature.mirror.core.history.metaTime
import presentation.feature.mirror.core.history.noteCountKey
import presentation.feature.mirror.core.history.shortBatchId
import presentation.feature.mirror.core.history.statusTone
import rectify_kmp.presentation_core_localisation.generated.resources.Res
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_annotation_empty_comment
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_empty
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_footer
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_group_older
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_group_today
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_group_yesterday
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_meta
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_status_fixed
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_status_followup
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_status_in_progress
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_status_sent
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_status_verified
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_thumbnail_description
import rectify_kmp.presentation_core_localisation.generated.resources.mirror_history_verify
import kotlin.time.Clock

/**
 * The side panel's History tab: every batch ever sent, grouped by calendar day (FR-26..FR-36).
 *
 * Composed entirely from shipped design-system parts — [HistoryRow] (which owns the plate, the
 * title/meta column and the status pill) and [AppButton]. Nothing here is a new atom, and no
 * text, time, id or tone decision is made in this file: they all come from
 * `core/history/HistoryFormatting.kt`, which is pure and tested.
 *
 * Row click is deliberately a no-op this release (known issue A11Y-8): there is no detail view
 * to open yet, and inventing one here would be the second place batch state lives.
 *
 * @param state Current mirror state; only [MirrorUiState.history] is read.
 * @param onIntent Intent sink — [MirrorIntent.VerifyBatch] is the only intent this tab fires.
 * @param modifier Modifier applied to the root composable. The window hands this `weight(1f)`,
 *   which is what gives the list its height and pins the footer below it.
 */
@Composable
public fun MirrorHistoryTab(
    state: MirrorUiState,
    onIntent: (MirrorIntent) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    if (state.history.isEmpty()) {
        Text(
            text = stringResource(Res.string.mirror_history_empty),
            style = Theme.typography.secondary,
            color = Theme.color.ink2,
            modifier = modifier.fillMaxWidth().padding(Theme.spacing.spacingL),
        )
        return
    }

    // One wall-clock read per list, not per recomposition: the clock inside the key would make
    // the key change every frame, and two reads could put one batch in two different buckets
    // across midnight. The list is re-grouped when the list changes, which is when it matters.
    val groups = remember(state.history) {
        groupHistory(state.history, Clock.System.now().toEpochMilliseconds())
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(Theme.spacing.spacingS),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
        ) {
            groups.forEach { group ->
                item(key = group.bucket.name) { HistoryGroupHeader(bucket = group.bucket) }
                items(items = group.entries, key = { it.id }) { entry ->
                    HistoryEntryRow(entry = entry, onIntent = onIntent)
                }
            }
        }
        HistoryFooter(newest = state.history.maxByOrNull { it.sentAtMs })
    }
}

/** The uppercase period header, emitted once per group by the list above it. */
@Composable
private fun HistoryGroupHeader(bucket: HistoryBucket) {
    val label = stringResource(
        when (bucket) {
            HistoryBucket.Today -> Res.string.mirror_history_group_today
            HistoryBucket.Yesterday -> Res.string.mirror_history_group_yesterday
            HistoryBucket.Older -> Res.string.mirror_history_group_older
        },
    )
    Text(
        text = label.uppercase(),
        style = Theme.typography.overline,
        color = Theme.color.ink3,
        modifier = Modifier.padding(horizontal = Theme.spacing.spacingS, vertical = Theme.spacing.spacingXS),
    )
}

/**
 * One batch as a [HistoryRow].
 *
 * The thumbnail decode sits in `remember(entry.id)` and is keyed by the **id**, not by the
 * model: [HistoryEntryModel] holds a `ByteArray`, so it compares by identity and every
 * re-emission of the same row from the database would otherwise decode the PNG again (the
 * module's deviation 2, applied to history instead of frames).
 *
 * A row whose bytes are `null` — the Android/iOS stand-in, or an encoder that failed — renders
 * [HistoryRow]'s bare plate, and so does a row whose bytes do not decode. Neither is worth a
 * crash or a broken-image placeholder.
 */
@Composable
private fun HistoryEntryRow(entry: HistoryEntryModel, onIntent: (MirrorIntent) -> Unit) {
    val thumbnail: ImageBitmap? = remember(entry.id) {
        entry.thumbPng?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
    }
    val title = entry.title.ifBlank { stringResource(Res.string.mirror_annotation_empty_comment) }
    val meta = stringResource(
        Res.string.mirror_history_meta,
        stringResource(noteCountKey(entry.annotationCount), entry.annotationCount),
        metaTime(entry.sentAtMs),
        shortBatchId(entry.id),
    )
    val thumbnailDescription = stringResource(Res.string.mirror_history_thumbnail_description, title)
    val verifyLabel = stringResource(Res.string.mirror_history_verify)

    HistoryRow(
        title = title,
        meta = meta,
        statusLabel = stringResource(entry.status.labelKey()),
        statusTone = statusTone(entry.status),
        onClick = {},
        thumbnail = thumbnail?.let {
            {
                Image(
                    bitmap = it,
                    contentDescription = thumbnailDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
        trailing = {
            // F-9 owns the diff; this release only has to give a fixed batch somewhere to
            // click (AC-36). Any other status has nothing to verify yet.
            if (entry.status == BatchStatusModel.Fixed) {
                AppButton(
                    text = verifyLabel,
                    onClick = { onIntent(MirrorIntent.VerifyBatch(entry.id)) },
                    size = AppButtonSize.ListAction,
                )
            }
        },
    )
}

/**
 * The agent/session line (AC-37), sourced from the newest batch.
 *
 * **Correction of record, same shape as FR-30's:** FR-35 / AC-37 say this line shows the
 * `sessionId`, and what ships is [shortBatchId] of it — the four-character tag. The design
 * contract (`inputs/design-history-tab.md`) prints `claude · ~/code/shopkit · session 7f3a`,
 * a four-character tag, while the raw id is `<deviceId>-f<frameId>-<epochMillis>`, which only
 * ellipsises away in a narrow panel and tells the reader nothing the row does not. The tag
 * also matches the row's own `#a41f` meta tag, so footer and row read as the same batch. The
 * project path the design shows is omitted — `AnnotationEnvelope` carries no path field.
 *
 * Hidden entirely when nothing has been sent; the empty state returns before this is reached.
 */
@Composable
private fun HistoryFooter(newest: HistoryEntryModel?) {
    if (newest == null) return
    Column(modifier = Modifier.fillMaxWidth()) {
        // Same hairline as `PayloadFooter`'s: a `line`-filled Box spanning the full panel width.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Theme.stroke.thin)
                .background(Theme.color.line),
        )
        Text(
            text = stringResource(Res.string.mirror_history_footer, shortBatchId(newest.id)),
            style = Theme.typography.monoMeta,
            color = Theme.color.ink3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(Theme.spacing.spacingS),
        )
    }
}

/** Status copy. One branch per value, so a sixth status is a compile error — as `statusTone`. */
private fun BatchStatusModel.labelKey(): StringResource = when (this) {
    BatchStatusModel.Sent -> Res.string.mirror_history_status_sent
    BatchStatusModel.InProgress -> Res.string.mirror_history_status_in_progress
    BatchStatusModel.Fixed -> Res.string.mirror_history_status_fixed
    BatchStatusModel.Verified -> Res.string.mirror_history_status_verified
    BatchStatusModel.FollowUpSent -> Res.string.mirror_history_status_followup
}

private val PreviewPanelHeight: Dp = 420.dp

private const val HOUR_MS = 60L * 60L * 1000L
private const val DAY_MS = 24L * HOUR_MS

/**
 * All five statuses across all three groups, so the tone table and the Fixed-only Verify button
 * are both visible in one render. The `FollowUpSent` row carries deliberately undecodable bytes:
 * a corrupt thumbnail must fall back to the bare plate rather than crash the list.
 */
private fun previewState(): MirrorUiState {
    val now = Clock.System.now().toEpochMilliseconds()
    return MirrorUiState(
        isPickerOpen = false,
        history = listOf(
            HistoryEntryModel(
                id = "emulator-5554-f12-${now - HOUR_MS}",
                sentAtMs = now - HOUR_MS,
                status = BatchStatusModel.Fixed,
                title = "Price sits 6px below the title",
                annotationCount = 3,
            ),
            HistoryEntryModel(
                id = "emulator-5554-f11-${now - 2 * HOUR_MS}",
                sentAtMs = now - 2 * HOUR_MS,
                status = BatchStatusModel.InProgress,
                title = "Checkout button is 16dp off the margin",
                annotationCount = 1,
            ),
            HistoryEntryModel(
                id = "emulator-5554-f10-${now - 3 * HOUR_MS}",
                sentAtMs = now - 3 * HOUR_MS,
                status = BatchStatusModel.Sent,
                title = "",
                annotationCount = 2,
            ),
            HistoryEntryModel(
                id = "emulator-5554-f9-${now - DAY_MS}",
                sentAtMs = now - DAY_MS,
                status = BatchStatusModel.Verified,
                title = "Promo placeholder clipped at 360dp",
                annotationCount = 1,
            ),
            HistoryEntryModel(
                id = "emulator-5554-f8-${now - 5 * DAY_MS}",
                sentAtMs = now - 5 * DAY_MS,
                status = BatchStatusModel.FollowUpSent,
                title = "Total row reads lighter than subtotal",
                annotationCount = 2,
                thumbPng = byteArrayOf(1, 2, 3),
            ),
        ),
    )
}

@Composable
private fun MirrorHistoryTabPreviewContent(state: MirrorUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Theme.color.surface)) {
        MirrorHistoryTab(state = state, onIntent = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorHistoryTabPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        MirrorHistoryTabPreviewContent(
            state = previewState(),
            modifier = Modifier.height(PreviewPanelHeight),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorHistoryTabPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        MirrorHistoryTabPreviewContent(
            state = previewState(),
            modifier = Modifier.height(PreviewPanelHeight),
        )
    }
}

/** Nothing sent yet: the early return, with no footer. No pinned height — there is no weighted child. */
@Preview(showBackground = true)
@Composable
private fun MirrorHistoryTabEmptyPreviewLight() {
    AppTheme(mode = ThemeMode.Light) {
        MirrorHistoryTabPreviewContent(state = MirrorUiState(isPickerOpen = false))
    }
}

@Preview(showBackground = true)
@Composable
private fun MirrorHistoryTabEmptyPreviewDark() {
    AppTheme(mode = ThemeMode.Dark) {
        MirrorHistoryTabPreviewContent(state = MirrorUiState(isPickerOpen = false))
    }
}
