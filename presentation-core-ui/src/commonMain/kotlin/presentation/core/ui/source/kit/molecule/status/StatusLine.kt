package presentation.core.ui.source.kit.molecule.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Line geometry. */
private val LineHeight: Dp = 34.dp
private val LineRadius: Dp = 8.dp
private val DotSize: Dp = 7.dp

/**
 * How a single [StatusStep] is rendered.
 *
 * The value carries both the dot color and the label emphasis, because in this control they
 * always move together — a step is never a coloured dot with a muted label.
 */
public enum class StatusStepState {
    /** Not reached yet, or already behind — muted dot and label. */
    Pending,

    /** Running and capturing. Red for the same reason a record light is. */
    Live,

    /** Paused on a frame. */
    Frozen,

    /** Working — dumping, uploading, sending. */
    Busy,

    /** Finished successfully. */
    Done,

    /** Failed here. */
    Error,
}

/**
 * One step in the pipeline shown by [StatusLine].
 *
 * @param label Step name, e.g. `Dumping hierarchy`.
 * @param state How this step is rendered.
 */
@Immutable
public data class StatusStep(
    val label: String,
    val state: StatusStepState = StatusStepState.Pending,
)

/**
 * The pipeline status line — `Streaming › Frozen › Dumping hierarchy › Sent`.
 *
 * Every step carries its own state rather than the line taking a single "active index",
 * because a failed run shows two at once: the step that broke, in red, and the state the
 * session fell back to.
 *
 * Step labels are mono: they name pipeline stages the machine reports, not prose.
 *
 * @param steps Ordered pipeline steps.
 * @param modifier Modifier applied to the root composable.
 * @param trailing Short state word shown right-aligned, e.g. `live`, `failed`; `null` hides it.
 */
@Composable
public fun StatusLine(
    steps: List<StatusStep>,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LineHeight)
            .clip(RoundedCornerShape(LineRadius))
            .background(Theme.color.chrome)
            .padding(horizontal = Theme.spacing.spacingS + Theme.spacing.spacingXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, step ->
            if (index > 0) {
                Text(
                    text = "›",
                    style = Theme.typography.monoData,
                    color = Theme.color.ink3,
                    modifier = Modifier.padding(horizontal = Theme.spacing.spacingS),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS + 2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(DotSize)
                        .clip(CircleShape)
                        .background(step.state.dotColor()),
                )
                Text(
                    text = step.label,
                    style = Theme.typography.monoData.copy(
                        fontWeight = if (step.state == StatusStepState.Pending) {
                            FontWeight.Normal
                        } else {
                            FontWeight.Medium
                        },
                    ),
                    color = step.state.labelColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = trailing,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun StatusStepState.dotColor(): Color = when (this) {
    StatusStepState.Pending -> Theme.color.ink3.copy(alpha = 0.5f)
    StatusStepState.Live, StatusStepState.Error -> Theme.color.annotate
    StatusStepState.Frozen -> Theme.color.ink
    StatusStepState.Busy -> Theme.color.warnStillOff
    StatusStepState.Done -> Theme.color.sentFixed
}

@Composable
private fun StatusStepState.labelColor(): Color = when (this) {
    StatusStepState.Pending -> Theme.color.ink3
    StatusStepState.Error -> Theme.color.annotate
    else -> Theme.color.ink
}

@Preview(showBackground = true)
@Composable
private fun StatusLinePreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            StatusLine(
                steps = listOf(
                    StatusStep("Streaming", StatusStepState.Live),
                    StatusStep("Frozen"),
                    StatusStep("Dumping hierarchy"),
                    StatusStep("Sent"),
                ),
                trailing = "live",
            )
            StatusLine(
                steps = listOf(
                    StatusStep("Stream dropped", StatusStepState.Error),
                    StatusStep("Frozen", StatusStepState.Frozen),
                    StatusStep("Dumping hierarchy"),
                    StatusStep("Sent"),
                ),
                trailing = "failed",
            )
        }
    }
}
