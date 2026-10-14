package presentation.core.ui.source.kit.atom.tile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.core.styling.core.Theme
import presentation.core.styling.source.theme.AppTheme
import presentation.core.ui.source.kit.atom.text.AutoSizeText

/**
 * Fixed tile heights. Arbitrary heights are not permitted — a bento grid with free-form
 * heights stops being a grid within about three screens.
 */
public enum class BentoTileHeight(internal val dp: Dp) {
    /** 104 dp — a caption and one value. */
    S(104.dp),

    /** 140 dp — a caption, a value and a unit or trend line. */
    M(140.dp),

    /** 196 dp — hero values, dials, charts. */
    L(196.dp),

    /** 240 dp — a full-width hero tile with actions. */
    XL(240.dp),
}

/**
 * Visual role of a [BentoTile].
 *
 * At most **two** accent-colored tiles may appear on one screen. Past that the grid stops
 * reading as an instrument and starts reading as a marketplace.
 */
public enum class BentoTileVariant {
    /** Default graphite tile. */
    Surface,

    /** The one hero tile on the screen — hero accent fill, always placed first. */
    Hero,

    /** Secondary analytics tile — cool accent fill. */
    Cool,

    /** Bright contrast tile — bone accent fill. */
    Bone,

    /** Transparent tile with an outline. Use for rows and low-emphasis content. */
    Outline,
}

/**
 * A single cell of the RECTIFY bento grid.
 *
 * The rule the whole layout rests on: **one tile carries one thought**. A mono caption at
 * the top, a dominant value at the bottom, nothing else. Anything that needs two ideas
 * needs two tiles.
 *
 * The value is rendered with [AutoSizeText] rather than plain [Text] — a 52 sp numeral in a
 * two-column grid runs out of room on a 360 dp screen, and a clipped speed reading is worse
 * than a slightly smaller one.
 *
 * ```kotlin
 * BentoTile(
 *     caption = "REVENUE",
 *     value = "128",
 *     unit = "K",
 *     variant = BentoTileVariant.Hero,
 *     height = BentoTileHeight.M,
 * )
 * ```
 *
 * @param modifier Modifier applied to the root container. Pass `Modifier.weight(1f)` from a
 *   [Row] to size the tile to one column, or let it fill the width for a two-column span.
 * @param caption Mono caption above the value. Rendered uppercase by the caller.
 * @param value Dominant value. Omit to render a loading tile — a tile without a value.
 * @param unit Optional unit shown beside the value.
 * @param variant Visual role; defaults to [BentoTileVariant.Surface].
 * @param height Fixed height preset; defaults to [BentoTileHeight.S].
 * @param onClick Optional tap handler. `null` makes the tile non-interactive.
 * @param content Optional slot rendered in place of the value row — dials, charts, rows.
 */
@Composable
public fun BentoTile(
    modifier: Modifier = Modifier,
    caption: String? = null,
    value: String? = null,
    unit: String? = null,
    variant: BentoTileVariant = BentoTileVariant.Surface,
    height: BentoTileHeight = BentoTileHeight.S,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val shape = if (variant == BentoTileVariant.Hero) {
        Theme.cornerToken.tileHero
    } else {
        Theme.cornerToken.tile
    }
    val container = variant.containerColor()
    val content_ = variant.contentColor()
    val captionColor = variant.captionColor()

    Column(
        modifier = modifier
            // Fixed, not minimum: a tile that grows to fit its content silently breaks the
            // row it shares with its neighbour. Content that does not fit is a signal to pick
            // a taller preset, not something the tile should paper over.
            .height(height.dp)
            .clip(shape)
            .background(container)
            .then(
                if (variant == BentoTileVariant.Outline) {
                    Modifier.border(Theme.stroke.thin, Theme.color.line2, shape)
                } else {
                    Modifier
                },
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(Theme.spacing.spacingL),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        if (caption != null) {
            Text(
                text = caption,
                style = Theme.typography.overline,
                color = captionColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (content != null) {
            content()
        } else if (value != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AutoSizeText(
                    text = value,
                    style = variant.valueStyle(),
                    color = content_,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (unit != null) {
                    Text(
                        text = unit,
                        style = Theme.typography.overline,
                        color = captionColor,
                        modifier = Modifier.padding(start = Theme.spacing.spacingS),
                    )
                }
            }
        }
    }
}

/**
 * Resolves the value type style.
 *
 * The hero tile gets the display role and every other tile gets the numeral role — it is
 * not a knob the caller can turn. "One hero tile per screen" only holds if being the hero
 * is what makes a number big, rather than the caller deciding case by case.
 */
@Composable
private fun BentoTileVariant.valueStyle(): TextStyle = when (this) {
    BentoTileVariant.Hero -> Theme.typography.display
    else -> Theme.typography.numeralXl
}

/** Resolves the tile fill. */
@Composable
private fun BentoTileVariant.containerColor(): Color = when (this) {
    BentoTileVariant.Surface -> Theme.color.surface
    BentoTileVariant.Hero -> Theme.color.primary
    BentoTileVariant.Cool -> Theme.color.primaryHover
    BentoTileVariant.Bone -> Theme.color.chrome
    BentoTileVariant.Outline -> Color.Transparent
}

/** Resolves the value color. */
@Composable
private fun BentoTileVariant.contentColor(): Color = when (this) {
    BentoTileVariant.Surface, BentoTileVariant.Outline -> Theme.color.ink
    BentoTileVariant.Hero, BentoTileVariant.Cool, BentoTileVariant.Bone ->
        Theme.color.onPrimary
}

/** Resolves the caption / unit color — one step quieter than the value. */
@Composable
private fun BentoTileVariant.captionColor(): Color = when (this) {
    BentoTileVariant.Surface, BentoTileVariant.Outline -> Theme.color.ink2
    BentoTileVariant.Hero, BentoTileVariant.Cool, BentoTileVariant.Bone ->
        Theme.color.onPrimary.copy(alpha = CAPTION_ON_ACCENT_ALPHA)
}

/** Caption opacity on accent fills — the only opacity in the tile. */
private const val CAPTION_ON_ACCENT_ALPHA = 0.72f

@Preview(showBackground = true)
@Composable
private fun BentoTilePreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(Theme.color.bg)
                .padding(Theme.spacing.spacingS),
            verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        ) {
            BentoTile(
                modifier = Modifier.fillMaxWidth(),
                caption = "REVENUE",
                value = "128",
                unit = "K",
                variant = BentoTileVariant.Hero,
                height = BentoTileHeight.M,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                BentoTile(
                    modifier = Modifier.weight(1f),
                    caption = "USERS",
                    value = "3 240",
                    unit = "ACTIVE",
                )
                BentoTile(
                    modifier = Modifier.weight(1f),
                    caption = "UPTIME",
                    value = "82",
                    unit = "/ 100",
                    variant = BentoTileVariant.Cool,
                )
            }
            BentoTile(
                modifier = Modifier.fillMaxWidth(),
                caption = "STORAGE SAVED",
                value = "1.8",
                unit = "GB",
                variant = BentoTileVariant.Outline,
            )
        }
    }
}
