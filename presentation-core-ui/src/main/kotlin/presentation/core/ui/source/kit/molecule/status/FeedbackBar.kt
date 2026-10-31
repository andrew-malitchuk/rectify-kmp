package presentation.core.ui.source.kit.molecule.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.icon.Check

/** Bar geometry. */
private val BarRadius: Dp = 12.dp
private val MarkSize: Dp = 22.dp
private val DotSize: Dp = 9.dp

/**
 * Outcome reported by a [FeedbackBar], driving its leading mark.
 */
public enum class FeedbackStatus {
    /** The action completed — a filled check. */
    Success,

    /** The action failed — a plain signal dot. */
    Error,

    /** Something is still running. */
    Busy,
}

/**
 * The feedback bar shown after an action resolves — `Sent 3 annotations to Claude Code`,
 * `Stream dropped`.
 *
 * [timing] and [detail] are separate on purpose: a duration is machine output and renders in
 * mono, while a consequence like `Annotations kept.` is prose and renders in the UI face. The
 * style guide draws that line explicitly, so the component does too rather than leaving it to
 * each call site.
 *
 * Errors say what to do: the title states the fact, [detail] reassures, and [actionLabel] is a
 * verb. Pass [AppButtonVariant.Primary] for a recovery the user must act on, and leave the
 * default ghost for an optional follow-up.
 *
 * @param text The headline — what happened.
 * @param modifier Modifier applied to the root composable.
 * @param status Outcome driving the leading mark.
 * @param timing Machine-reported duration, e.g. `1.4 s`; rendered mono. `null` hides it.
 * @param detail Prose consequence, e.g. `Annotations kept.`; `null` hides it.
 * @param actionLabel Label of the trailing action; `null` renders no action.
 * @param onAction Called when the action is tapped.
 * @param actionVariant Emphasis of the trailing action.
 */
@Composable
public fun FeedbackBar(
    text: String,
    modifier: Modifier = Modifier,
    status: FeedbackStatus = FeedbackStatus.Success,
    timing: String? = null,
    detail: String? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    actionVariant: AppButtonVariant = AppButtonVariant.Ghost,
) {
    Row(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(BarRadius))
            .background(Theme.color.surface)
            .border(
                Theme.stroke.thin,
                Theme.color.line,
                androidx.compose.foundation.shape.RoundedCornerShape(BarRadius),
            )
            .padding(
                horizontal = Theme.spacing.spacingS + Theme.spacing.spacingXS,
                vertical = Theme.spacing.spacingS,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        StatusMark(status = status)
        Text(
            text = text,
            style = Theme.typography.bodyStrong,
            color = Theme.color.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (timing != null) {
            Text(
                text = timing,
                style = Theme.typography.monoMeta,
                color = Theme.color.ink3,
                maxLines = 1,
            )
        }
        if (detail != null) {
            Text(
                text = detail,
                style = Theme.typography.secondary,
                color = Theme.color.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (actionLabel != null) {
            AppButton(
                text = actionLabel,
                onClick = onAction,
                variant = actionVariant,
                size = AppButtonSize.ListAction,
            )
        }
    }
}

/** The leading mark — a filled check for success, a signal dot otherwise. */
@Composable
private fun StatusMark(status: FeedbackStatus) {
    when (status) {
        FeedbackStatus.Success -> Box(
            modifier = Modifier
                .size(MarkSize)
                .clip(CircleShape)
                .background(Theme.color.sentFixed),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Check,
                contentDescription = null,
                // On a signal fill — white in both themes.
                tint = Color.White,
                modifier = Modifier.size(Theme.size.iconXXS),
            )
        }

        FeedbackStatus.Error, FeedbackStatus.Busy -> Box(
            modifier = Modifier
                .size(MarkSize),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(DotSize)
                    .clip(CircleShape)
                    .background(
                        if (status == FeedbackStatus.Error) {
                            Theme.color.annotate
                        } else {
                            Theme.color.warnStillOff
                        },
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackBarPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            horizontalAlignment = Alignment.Start,
        ) {
            FeedbackBar(
                text = "Sent 3 annotations to Claude Code",
                timing = "1.4 s",
                actionLabel = "Open session",
            )
            FeedbackBar(
                text = "Stream dropped",
                status = FeedbackStatus.Error,
                detail = "Annotations kept.",
                actionLabel = "Reconnect",
                actionVariant = AppButtonVariant.Primary,
            )
        }
    }
}
