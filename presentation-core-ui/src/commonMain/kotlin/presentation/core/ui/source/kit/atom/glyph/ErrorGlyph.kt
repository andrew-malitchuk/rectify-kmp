package presentation.core.ui.source.kit.atom.glyph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.icon.X

/** Glyph geometry. */
private val GlyphSize: Dp = 34.dp

/** Opacity of the wash behind the glyph. */
private const val WASH_ALPHA = 0.15f

/**
 * What kind of problem an [ErrorGlyph] announces.
 *
 * The three are deliberately distinguishable before the text is read: the colour alone tells
 * the user whether this is something they can fix, something they must choose between, or
 * something that is already over.
 */
public enum class ErrorKind {
    /** The user can fix this — there is a command or an action that resolves it. */
    Fixable,

    /** The app needs a decision before it can continue. */
    Choice,

    /** The operation failed and will not be retried on its own. */
    Failed,
}

/**
 * The circular glyph that leads an error — `!`, `?`, or `×` on a tinted wash.
 *
 * @param kind The kind of problem, driving colour and glyph.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun ErrorGlyph(
    kind: ErrorKind,
    modifier: Modifier = Modifier,
) {
    val accent = kind.accentColor()
    Box(
        modifier = modifier
            .size(GlyphSize)
            .clip(CircleShape)
            .background(accent.copy(alpha = WASH_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            // Drawn rather than typed: a stroked cross reads as "closed", where the × glyph
            // reads as a multiplication sign at this size.
            ErrorKind.Failed -> Icon(
                imageVector = X,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(Theme.size.iconXS),
            )

            ErrorKind.Fixable, ErrorKind.Choice -> Text(
                text = if (kind == ErrorKind.Fixable) "!" else "?",
                style = Theme.typography.titleS.copy(fontWeight = FontWeight.SemiBold),
                color = accent,
            )
        }
    }
}

@Composable
private fun ErrorKind.accentColor(): Color = when (this) {
    ErrorKind.Fixable -> Theme.color.warnStillOff
    ErrorKind.Choice -> Theme.color.ink2
    ErrorKind.Failed -> Theme.color.annotate
}

@Preview(showBackground = true)
@Composable
private fun ErrorGlyphPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            ErrorGlyph(kind = ErrorKind.Fixable)
            ErrorGlyph(kind = ErrorKind.Choice)
            ErrorGlyph(kind = ErrorKind.Failed)
        }
    }
}
