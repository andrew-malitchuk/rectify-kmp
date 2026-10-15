package presentation.core.ui.source.kit.organism.annotation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.X

/** Popover geometry. */
private val PopoverRadius: Dp = 12.dp
private val PopoverMinWidth: Dp = 300.dp
private val PopoverMaxWidth: Dp = 380.dp
private val IndexBadgeSize: Dp = 22.dp
private val IndexBadgeRadius: Dp = 5.dp
private val FieldRadius: Dp = 7.dp
private val FieldHeight: Dp = 30.dp
private val TailWidth: Dp = 14.dp
private val TailHeight: Dp = 7.dp
private val TailInset: Dp = 18.dp
private val CloseSize: Dp = 22.dp

/**
 * Copy on the always-dark popover. These are deliberate literals rather than ink tokens: the
 * popover keeps the same dark plate in **both** themes, so theme-following ink would invert and
 * vanish against it.
 *
 * Values come straight from the style guide's note on the dark popover — body `#f4f4f5`, meta
 * white at 42%, and the measure note lifted to `oklch(0.74 0.2 255)` so the blue still carries
 * enough contrast once it is sitting on near-black.
 */
private val PopoverInk = Color(0xFFF4F4F5)
private val PopoverMetaInk = Color(0xFFFFFFFF).copy(alpha = 0.42f)
private val PopoverMeasureInk = Color(0xFF6DADFF)

/** Fill of the comment field, a step up from the popover plate so the input reads as inset. */
private val FieldFill = Color(0xFFFFFFFF).copy(alpha = 0.07f)

/**
 * The comment popover — where an annotation's note is actually written.
 *
 * Always dark, in both themes, because it floats over mirrored device UI whose brightness is
 * unknown: a theme-following surface would disappear against a light screenshot. Every color in
 * here is therefore fixed rather than drawn from the ink tokens.
 *
 * This is the **editing** form of an annotation note. Its read-only counterpart, shown out in
 * the gutter once a note is committed, is
 * [AnnotationPopover][presentation.core.ui.source.kit.molecule.annotation.AnnotationPopover] —
 * reach for that one when there is nothing to type into.
 *
 * Positioning is the host's: this component draws its own tail but has no idea where the mark
 * it belongs to sits.
 *
 * @param value Current comment text.
 * @param onValueChange Called as the user types.
 * @param onClose Called when the user dismisses the popover.
 * @param modifier Modifier applied to the root composable.
 * @param index Mark index shown in the leading badge.
 * @param placeholder Hint shown while [value] is empty.
 * @param meta Machine-readable position line, e.g. `218×77 @ 1035,862`; `null` hides it.
 * @param finding Optional measured finding, e.g. `⌥ top Δ6dp vs item_title`, shown under [meta].
 * @param saveHint Optional shortcut hint shown while the field has focus, e.g. `⏎ save`.
 * @param closeLabel Accessibility label of the close control. Defaulted, because a host where
 *   closing *deletes* the annotation (FR-8) has to say so rather than announcing "Close".
 * @param focusRequester Requester bound to the comment field itself, so a host can focus it the
 *   moment the popover appears (A11Y-2). Bound to the field and not to the popover root on
 *   purpose: delegating focus through a group is one more thing that can silently land
 *   somewhere else, and the only focusable thing in here is the field.
 */
@Composable
public fun CommentPopover(
    value: String,
    onValueChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 1,
    placeholder: String = "What's wrong here?",
    meta: String? = null,
    finding: String? = null,
    saveHint: String? = null,
    closeLabel: String = "Close",
    focusRequester: FocusRequester? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier.widthIn(min = PopoverMinWidth, max = PopoverMaxWidth)) {
        PopoverTail()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PopoverRadius))
                .background(Theme.color.pop)
                .padding(Theme.spacing.spacingS + Theme.spacing.spacingXS),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
            ) {
                IndexBadge(index = index)
                CommentField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = placeholder,
                    focused = isFocused,
                    interactionSource = interactionSource,
                    focusRequester = focusRequester,
                    modifier = Modifier.weight(1f),
                )
                CloseButton(onClose = onClose, closeLabel = closeLabel)
            }

            if (meta != null || (isFocused && saveHint != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (meta != null) {
                        Text(
                            text = meta,
                            style = Theme.typography.monoMeta,
                            color = PopoverMetaInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (isFocused && saveHint != null) {
                        Text(
                            text = saveHint,
                            style = Theme.typography.monoMeta,
                            color = PopoverMetaInk,
                            maxLines = 1,
                        )
                    }
                }
            }

            if (finding != null) {
                Text(
                    text = finding,
                    style = Theme.typography.monoMeta,
                    color = PopoverMeasureInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** The upward pointer that ties the popover to its mark. */
@Composable
private fun PopoverTail() {
    val fill = Theme.color.pop
    Canvas(
        modifier = Modifier
            .padding(start = TailInset)
            .width(TailWidth)
            .height(TailHeight),
    ) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path = path, color = fill)
    }
}

@Composable
private fun IndexBadge(index: Int) {
    Box(
        modifier = Modifier
            .size(IndexBadgeSize)
            .clip(RoundedCornerShape(IndexBadgeRadius))
            .background(Theme.color.annotate),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = index.toString(),
            style = Theme.typography.monoBadge,
            // On a signal fill — white in both themes.
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Composable
private fun CommentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    focused: Boolean,
    interactionSource: MutableInteractionSource,
    focusRequester: FocusRequester?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(FieldRadius)
    val style = Theme.typography.body
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style.copy(color = PopoverInk),
        cursorBrush = SolidColor(Theme.color.annotate),
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = modifier
            .height(FieldHeight)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .clip(shape)
            .background(FieldFill)
            .then(
                if (focused) {
                    Modifier.border(Theme.stroke.thin, Theme.color.annotate, shape)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = Theme.spacing.spacingS),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = style,
                        color = PopoverMetaInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun CloseButton(onClose: () -> Unit, closeLabel: String) {
    Box(
        modifier = Modifier
            .size(CloseSize)
            .clip(RoundedCornerShape(FieldRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClose,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = X,
            contentDescription = closeLabel,
            tint = PopoverMetaInk,
            modifier = Modifier.size(Theme.size.iconXS),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentPopoverPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL),
        ) {
            CommentPopover(
                value = "",
                onValueChange = {},
                onClose = {},
                index = 1,
                meta = "218×77 @ 1035,862",
                saveHint = "⏎ save",
            )
            CommentPopover(
                value = "Stepper is 24dp tall; hit target too small",
                onValueChange = {},
                onClose = {},
                index = 2,
                meta = "218×77 @ 1035,862",
            )
            CommentPopover(
                value = "Price sits lower than the title",
                onValueChange = {},
                onClose = {},
                index = 1,
                meta = "218×77 @ 1035,862",
                finding = "⌥ top Δ6dp vs item_title",
            )
        }
    }
}
