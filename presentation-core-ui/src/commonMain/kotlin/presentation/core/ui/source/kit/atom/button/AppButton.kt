package presentation.core.ui.source.kit.atom.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.ChevronRight

/**
 * Per-size geometry and type. Lives in the component rather than in [Theme] on purpose: this
 * is the button's own geometry, not a layout rhythm other components should share.
 */
private data class AppButtonMetrics(
    val height: Dp,
    val horizontalPadding: Dp,
    val radius: Dp,
    val textStyle: TextStyle,
)

@Composable
private fun AppButtonSize.metrics(): AppButtonMetrics = when (this) {
    AppButtonSize.InlineRow -> AppButtonMetrics(
        height = 22.dp,
        horizontalPadding = 9.dp,
        radius = 6.dp,
        textStyle = Theme.typography.small,
    )
    AppButtonSize.ListAction -> AppButtonMetrics(
        height = 24.dp,
        horizontalPadding = 10.dp,
        radius = 6.dp,
        textStyle = Theme.typography.secondary.copy(fontWeight = FontWeight.Medium),
    )
    AppButtonSize.Banner -> AppButtonMetrics(
        height = 26.dp,
        horizontalPadding = 11.dp,
        radius = 6.dp,
        textStyle = Theme.typography.secondary.copy(fontWeight = FontWeight.Medium),
    )
    AppButtonSize.Toolbar -> AppButtonMetrics(
        height = 28.dp,
        horizontalPadding = 12.dp,
        radius = 7.dp,
        textStyle = Theme.typography.bodyStrong,
    )
    AppButtonSize.Dialog -> AppButtonMetrics(
        height = 30.dp,
        horizontalPadding = 14.dp,
        radius = 7.dp,
        textStyle = Theme.typography.bodyStrong,
    )
    AppButtonSize.PageCta -> AppButtonMetrics(
        height = 32.dp,
        horizontalPadding = 16.dp,
        radius = 8.dp,
        textStyle = Theme.typography.bodyStrong,
    )
}

/**
 * Visual role of an [AppButton].
 *
 * Note that no variant carries a status color. Ok / warn / danger belong to the data layer —
 * a button that turns red is telling the user about the *button*, which is never the point.
 */
public enum class AppButtonVariant {
    /** Primary call to action — ink fill. One per screen. */
    Primary,

    /** Secondary action — filled with a visible border, lower emphasis than [Primary]. */
    Secondary,

    /** Lowest-emphasis action — text only, no fill or border at rest. */
    Ghost,
}

/**
 * Size preset of an [AppButton] — 6 tiers, one per context. [Toolbar] is the default.
 *
 * @param InlineRow 22 dp — inline row action (e.g. a "Verify" link next to a list row).
 * @param ListAction 24 dp — list / toast action.
 * @param Banner 26 dp — overlay & banner action.
 * @param Toolbar 28 dp — toolbar, the default size.
 * @param Dialog 30 dp — dialog & error card action.
 * @param PageCta 32 dp — page-level call to action.
 */
public enum class AppButtonSize {
    InlineRow,
    ListAction,
    Banner,
    Toolbar,
    Dialog,
    PageCta,
}

/**
 * The Rectify button — 3 variants × 6 interaction states × 6 sizes.
 *
 * Every control here is a rounded rectangle with a small, size-dependent corner radius (6–8 dp)
 * — not a pill. Press feedback, hover, and focus are all color steps, not scale or shadow
 * animation, and their duration comes from [Theme.motion] so it collapses to 0 ms automatically
 * while driving.
 *
 * [busy] implies non-interactive and renders a spinner ahead of [busyText] (falls back to
 * [text] when `null`) — callers don't need to also pass `enabled = false`.
 *
 * ```kotlin
 * AppButton(
 *     text = "Resolve & Send",
 *     onClick = { viewModel.resolveAndSend() },
 *     shortcut = "⌘↵",
 * )
 * ```
 *
 * @param text Label displayed in the button.
 * @param onClick Callback invoked on tap.
 * @param modifier Modifier applied to the root composable.
 * @param variant Visual role; defaults to [AppButtonVariant.Primary].
 * @param size Size preset; defaults to [AppButtonSize.Toolbar].
 * @param icon Optional trailing icon.
 * @param shortcut Optional trailing keyboard-shortcut hint (e.g. `"⌘↵"`), rendered muted.
 * @param fillMaxWidth Whether the button stretches to the full available width.
 * @param enabled Whether the button accepts input.
 * @param busy When `true`, shows a spinner and [busyText], and stops accepting input.
 * @param busyText Label shown while [busy]; falls back to [text] when `null`.
 */
@Composable
public fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    size: AppButtonSize = AppButtonSize.Toolbar,
    icon: ImageVector? = null,
    shortcut: String? = null,
    fillMaxWidth: Boolean = false,
    enabled: Boolean = true,
    busy: Boolean = false,
    busyText: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isInteractive = enabled && !busy

    // Focus-visible, not focus: clicking a button also focuses it, and a ring that lingers
    // after a click reads as a stuck toggle. The ring is for keyboard navigation, so it only
    // shows while the user is actually driving with the keyboard.
    val inputMode = LocalInputModeManager.current
    val showFocusRing = isFocused && inputMode.inputMode == InputMode.Keyboard

    val container = variant.containerColor(interactive = isInteractive, pressed = isPressed, hovered = isHovered)
    val content = variant.contentColor(interactive = isInteractive)
    val outline = variant.outlineColor(interactive = isInteractive)

    val animatedContainer by animateColorAsState(
        targetValue = container,
        animationSpec = tween(
            durationMillis = Theme.motion.durations.fastMs,
            easing = Theme.motion.easings.easeOut,
        ),
        label = "AppButton: container",
    )

    val metrics = size.metrics()
    val shape = RoundedCornerShape(metrics.radius)
    val textStyle = metrics.textStyle

    Box(
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .height(metrics.height)
            .defaultMinSize(minWidth = metrics.height)
            .clip(shape)
            .background(animatedContainer)
            .then(
                if (showFocusRing) {
                    Modifier.border(BorderStroke(Theme.stroke.regular, Theme.color.focus), shape)
                } else if (outline != Color.Transparent) {
                    Modifier.border(BorderStroke(Theme.stroke.thin, outline), shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isInteractive,
                onClick = onClick,
                role = Role.Button,
            )
            .padding(horizontal = metrics.horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(textStyle.fontSize.value.dp),
                    color = content,
                    strokeWidth = 1.5.dp,
                )
                Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
            }
            Text(
                text = if (busy) busyText ?: text else text,
                style = textStyle,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!busy && icon != null) {
                Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(Theme.size.iconS),
                )
            }
            if (!busy && shortcut != null) {
                Spacer(modifier = Modifier.width(Theme.spacing.spacingS))
                Text(
                    text = shortcut,
                    style = textStyle,
                    color = content.copy(alpha = SHORTCUT_ALPHA),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Resolves the fill color for the current interaction state.
 *
 * `hover` and `seg` are **translucent overlay** tokens, not solid fills: they are composited
 * *onto* a variant's resting surface rather than replacing it. Swapping a solid surface token
 * for a translucent one (or for a different solid token two steps down the ladder) makes the
 * fill jump instead of stepping, and inverts in dark mode — the overlay is white there, so it
 * lightens where a solid swap would darken.
 */
@Composable
private fun AppButtonVariant.containerColor(interactive: Boolean, pressed: Boolean, hovered: Boolean): Color {
    if (!interactive) {
        return when (this) {
            // A solid muted surface, not a translucent overlay: disabled must read as filled.
            AppButtonVariant.Primary, AppButtonVariant.Secondary -> Theme.color.chrome
            AppButtonVariant.Ghost -> Color.Transparent
        }
    }
    return when (this) {
        // Primary has its own purpose-built solid ladder.
        AppButtonVariant.Primary -> when {
            pressed -> Theme.color.primaryPress
            hovered -> Theme.color.primaryHover
            else -> Theme.color.primary
        }
        // Solid base, overlays composited on top so each state is one gentle step.
        AppButtonVariant.Secondary -> when {
            pressed -> Theme.color.seg.compositeOver(Theme.color.surface)
            hovered -> Theme.color.hover.compositeOver(Theme.color.surface)
            else -> Theme.color.surface
        }
        // Transparent at rest, so the overlays composite against the page behind the button.
        AppButtonVariant.Ghost -> when {
            pressed -> Theme.color.seg
            hovered -> Theme.color.hover
            else -> Color.Transparent
        }
    }
}

/** Resolves the label / icon color. */
@Composable
private fun AppButtonVariant.contentColor(interactive: Boolean): Color {
    if (!interactive) return Theme.color.ink3
    return when (this) {
        AppButtonVariant.Primary -> Theme.color.onPrimary
        AppButtonVariant.Secondary -> Theme.color.ink
        AppButtonVariant.Ghost -> Theme.color.ink2
    }
}

/**
 * Resolves the border color for [AppButtonVariant.Secondary]; [Color.Transparent] for the
 * other variants means no border is drawn at rest (the focus ring, drawn by the caller, is
 * independent of this).
 */
@Composable
private fun AppButtonVariant.outlineColor(interactive: Boolean): Color = when (this) {
    AppButtonVariant.Secondary -> if (interactive) Theme.color.line2 else Theme.color.line
    else -> Color.Transparent
}

/** Opacity applied to the trailing shortcut hint relative to the label. */
private const val SHORTCUT_ALPHA = 0.6f

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .fillMaxWidth()
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            AppButton(text = "Start mirroring", onClick = {}, icon = ChevronRight, size = AppButtonSize.PageCta)
            AppButton(text = "Boot", onClick = {}, variant = AppButtonVariant.Secondary)
            AppButton(text = "Clear", onClick = {}, variant = AppButtonVariant.Ghost)
            AppButton(text = "Unavailable", onClick = {}, enabled = false)
            AppButton(text = "Sending", onClick = {}, busy = true, busyText = "Sending…")
            AppButton(
                text = "Resolve & Send",
                onClick = {},
                shortcut = "⌘↵",
            )
            AppButton(
                text = "Verify",
                onClick = {},
                variant = AppButtonVariant.Secondary,
                size = AppButtonSize.InlineRow,
            )
        }
    }
}
