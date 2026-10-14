package presentation.core.styling.source.attribute

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import org.jetbrains.compose.resources.Font
import presentation.core.styling.core.ThemeTypography
import rectify_kmp.presentation_core_styling.generated.resources.Res
import rectify_kmp.presentation_core_styling.generated.resources.jetbrains_mono
import rectify_kmp.presentation_core_styling.generated.resources.roboto_flex

/**
 * Tabular-figure feature tag. Applied to mono roles that read as data so digits keep a fixed
 * advance width and live values stop jittering as they update.
 */
private const val TABULAR_NUMS = "tnum"

/**
 * Builds the Roboto [FontFamily] from the single bundled Roboto Flex variable font, selecting
 * weight instances via [FontVariation.Settings] rather than separate static-weight files.
 *
 * Used for everything a person reads: titles, labels, comments, errors. Regular/Medium/SemiBold
 * cover the UI Kit v2 scale; Bold/ExtraBold exist only for the legacy
 * [presentation.core.styling.core.ThemeTypography.numeralXl]/
 * [presentation.core.styling.core.ThemeTypography.numeralLg]/
 * [presentation.core.styling.core.ThemeTypography.display] hero-numeral roles.
 */
@Composable
internal fun RobotoFontFamily(): FontFamily =
    FontFamily(
        Font(
            resource = Res.font.roboto_flex,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
        Font(
            resource = Res.font.roboto_flex,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            resource = Res.font.roboto_flex,
            weight = FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(600)),
        ),
        Font(
            resource = Res.font.roboto_flex,
            weight = FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
        Font(
            resource = Res.font.roboto_flex,
            weight = FontWeight.ExtraBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(800)),
        ),
    )

/**
 * Builds the JetBrains Mono [FontFamily] from the single bundled variable font, selecting the
 * Regular (400) and Medium (500) weight instances via [FontVariation.Settings].
 *
 * Used for everything a machine produced: ids, coordinates, sizes, commands, versions,
 * timestamps, shortcuts, status.
 */
@Composable
internal fun JetBrainsMonoFontFamily(): FontFamily =
    FontFamily(
        Font(
            resource = Res.font.jetbrains_mono,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
        Font(
            resource = Res.font.jetbrains_mono,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
    )

/**
 * Assembles the complete [ThemeTypography] from [attributeFontSize], [attributeLineHeight]
 * and the two bundled font families.
 *
 * [ThemeTypography.label] and [ThemeTypography.overline] must be rendered uppercase at the
 * call-site — casing is a convention, not a TextStyle property.
 */
@Composable
internal fun AttributeTypography(): ThemeTypography {
    val roboto = RobotoFontFamily()
    val jetBrainsMono = JetBrainsMonoFontFamily()

    return ThemeTypography(
        titleL = TextStyle(
            fontSize = attributeFontSize.titleL,
            lineHeight = attributeLineHeight.titleL,
            fontWeight = FontWeight.SemiBold,
            fontFamily = roboto,
            letterSpacing = (-0.01).em,
        ),
        titleM = TextStyle(
            fontSize = attributeFontSize.titleM,
            lineHeight = attributeLineHeight.titleM,
            fontWeight = FontWeight.SemiBold,
            fontFamily = roboto,
            letterSpacing = (-0.01).em,
        ),
        titleS = TextStyle(
            fontSize = attributeFontSize.titleS,
            lineHeight = attributeLineHeight.titleS,
            fontWeight = FontWeight.SemiBold,
            fontFamily = roboto,
            letterSpacing = (-0.01).em,
        ),
        body = TextStyle(
            fontSize = attributeFontSize.body,
            lineHeight = attributeLineHeight.body,
            fontWeight = FontWeight.Normal,
            fontFamily = roboto,
        ),
        bodyStrong = TextStyle(
            fontSize = attributeFontSize.bodyStrong,
            lineHeight = attributeLineHeight.bodyStrong,
            fontWeight = FontWeight.Medium,
            fontFamily = roboto,
        ),
        comment = TextStyle(
            fontSize = attributeFontSize.comment,
            lineHeight = attributeLineHeight.comment,
            fontWeight = FontWeight.Normal,
            fontFamily = roboto,
        ),
        label = TextStyle(
            fontSize = attributeFontSize.label,
            lineHeight = attributeLineHeight.label,
            fontWeight = FontWeight.SemiBold,
            fontFamily = roboto,
        ),
        secondary = TextStyle(
            fontSize = attributeFontSize.secondary,
            lineHeight = attributeLineHeight.secondary,
            fontWeight = FontWeight.Normal,
            fontFamily = roboto,
        ),
        small = TextStyle(
            fontSize = attributeFontSize.small,
            lineHeight = attributeLineHeight.small,
            fontWeight = FontWeight.Medium,
            fontFamily = roboto,
        ),
        caption = TextStyle(
            fontSize = attributeFontSize.caption,
            lineHeight = attributeLineHeight.caption,
            fontWeight = FontWeight.Medium,
            fontFamily = roboto,
        ),
        monoData = TextStyle(
            fontSize = attributeFontSize.monoData,
            lineHeight = attributeLineHeight.monoData,
            fontWeight = FontWeight.Normal,
            fontFamily = jetBrainsMono,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        monoMeta = TextStyle(
            fontSize = attributeFontSize.monoMeta,
            lineHeight = attributeLineHeight.monoMeta,
            fontWeight = FontWeight.Normal,
            fontFamily = jetBrainsMono,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        overline = TextStyle(
            fontSize = attributeFontSize.overline,
            lineHeight = attributeLineHeight.overline,
            fontWeight = FontWeight.Normal,
            fontFamily = jetBrainsMono,
            letterSpacing = 0.08.em,
        ),
        monoBadge = TextStyle(
            fontSize = attributeFontSize.monoBadge,
            lineHeight = attributeLineHeight.monoBadge,
            fontWeight = FontWeight.Medium,
            fontFamily = jetBrainsMono,
            letterSpacing = 0.06.em,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        monoTag = TextStyle(
            fontSize = attributeFontSize.monoTag,
            lineHeight = attributeLineHeight.monoTag,
            fontWeight = FontWeight.Medium,
            fontFamily = jetBrainsMono,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        monoRuler = TextStyle(
            fontSize = attributeFontSize.monoRuler,
            lineHeight = attributeLineHeight.monoRuler,
            fontWeight = FontWeight.Normal,
            fontFamily = jetBrainsMono,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        // Legacy — not part of the UI Kit v2 scale, see ThemeFontSize.display.
        display = TextStyle(
            fontSize = attributeFontSize.display,
            lineHeight = attributeLineHeight.display,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = roboto,
            letterSpacing = (-0.045).em,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        numeralXl = TextStyle(
            fontSize = attributeFontSize.numeralXl,
            lineHeight = attributeLineHeight.numeralXl,
            fontWeight = FontWeight.Bold,
            fontFamily = roboto,
            letterSpacing = (-0.035).em,
            fontFeatureSettings = TABULAR_NUMS,
        ),
        numeralLg = TextStyle(
            fontSize = attributeFontSize.numeralLg,
            lineHeight = attributeLineHeight.numeralLg,
            fontWeight = FontWeight.Bold,
            fontFamily = roboto,
            letterSpacing = (-0.03).em,
            fontFeatureSettings = TABULAR_NUMS,
        ),
    )
}
