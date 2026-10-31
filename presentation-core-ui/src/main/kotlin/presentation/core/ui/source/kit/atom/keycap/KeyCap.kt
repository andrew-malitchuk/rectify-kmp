package presentation.core.ui.source.kit.atom.keycap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme

/** Key-cap geometry. Small enough to sit inline in a toolbar without stealing attention. */
private val KeyCapHeight: Dp = 20.dp
private val KeyCapRadius: Dp = 6.dp
private val KeyCapPadding: Dp = 5.dp

/**
 * A keyboard key rendered as a small cap — `⌥`, `⌘`, `↵`, `esc`.
 *
 * Purely presentational: a key cap never handles input, it only names the key the user should
 * press. Glyphs follow the style guide's formatting rule — macOS glyphs, mono, never spelled
 * out as `Cmd+Enter`.
 *
 * @param text The key glyph or short name to display.
 * @param modifier Modifier applied to the root composable.
 */
@Composable
public fun KeyCap(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(KeyCapRadius)
    Box(
        modifier = modifier
            .height(KeyCapHeight)
            // Square for a single glyph, growing only for longer names like "esc".
            .defaultMinSize(minWidth = KeyCapHeight)
            .clip(shape)
            .background(Theme.color.surface)
            .border(width = Theme.stroke.thin, color = Theme.color.line, shape = shape)
            .padding(horizontal = KeyCapPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = Theme.typography.monoMeta,
            color = Theme.color.ink2,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyCapPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingL),
            horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyCap(text = "⌥")
            KeyCap(text = "⌘")
            KeyCap(text = "↵")
            KeyCap(text = "esc")
        }
    }
}
