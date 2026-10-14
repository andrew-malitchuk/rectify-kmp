# presentation-core-styling

> Rectify design system tokens — colors, typography, spacing, shapes, stroke, and motion.

## Responsibility

Defines the visual language of the Rectify app through a token-based design system.
Provides `AppTheme` composable that resolves dark/light palettes, builds typography from
**Roboto Flex** (everything a person reads) and **JetBrains Mono** (everything a machine
produced) — each a single bundled variable font, with weights selected via
`FontVariation.Settings` rather than separate static-weight files — and
distributes all tokens via `staticCompositionLocalOf`. Every UI component reads tokens through
the `Theme` singleton.

## Brand Rules

- **Accent discipline:** `primary`/`primaryHover`/`primaryPress` is a single accent ladder (not multiple hues) — don't introduce a second brand color
- **Default radius: 0 dp.** Larger radii are reserved for specific corner tokens (`card`, `button`, `chip`, `input`) — see `ThemeCornerToken`
- **Borders, not elevation:** use `BorderStroke(Theme.stroke.thin, Theme.color.line2)`, never `CardDefaults.cardElevation()`
- **Spacing scale:** `ThemeSpacing` (XS/S/L/XL/2XL/3XL/4XL) only — no arbitrary values
- **Casing:** `label` and `overline` must be rendered UPPERCASE at the call-site

## Dependencies

None. This is a core module with no internal project dependencies.

## Public API

| Class / Object | Description |
|---|---|
| `AppTheme` | Top-level composable provider — `mode: ThemeMode`, `reducedMotion: Boolean = false` |
| `Theme` | Singleton accessor: `Theme.color`, `Theme.typography`, `Theme.spacing`, `Theme.spacingToken`, `Theme.radius`, `Theme.corner`, `Theme.cornerToken`, `Theme.size`, `Theme.stroke`, `Theme.motion` |
| `ThemeMode` | Enum: `Light`, `Dark`, `System` |
| `ThemeColor` | Surfaces (`bg`/`chrome`/`stage`/`surface`/`pop`), ink (`ink`/`ink2`/`ink3`/`primary`/`primaryHover`/`primaryPress`/`onPrimary`/`focus`), lines & fills (`line`/`line2`/`hover`/`seg`/`codeBg`), and signal (`annotate`/`sentFixed`/`warnStillOff`/`measure` — same in both themes) roles |
| `ThemeFontSize` | 16-level type scale: titleL / titleM / titleS / body / bodyStrong / comment / label / secondary / small / caption / monoData / monoMeta / overline / monoBadge / monoTag / monoRuler, plus 3 legacy hero-numeral sizes (display / numeralXl / numeralLg) kept only for `BentoTile` |
| `ThemeLineHeight` | Matching line heights |
| `ThemeTypography` | Ready-to-use `TextStyle` tokens (Roboto Flex + JetBrains Mono) |
| `ThemeSpacing` | 7-level scale: spacingXS(4) / spacingS(8) / spacingL(16) / spacingXL(24) / spacing2XL(32) / spacing3XL(48) / spacing4XL(64) |
| `ThemeSpacingToken` | Semantic mapping: contentPadding / itemGap / sectionGap / inlinePadding / compactGap |
| `ThemeRadius` | 2 values: none(0) / m(4) |
| `ThemeCorner` | Matching RoundedCornerShapes |
| `ThemeCornerToken` | card / button / chip / input — all map to `corner.none` |
| `ThemeStroke` | 3 values: hairline(0.5) / thin(1) / regular(2) dp |
| `ThemeMotion` | durations + easings + reducedMotion flag |
| `ThemeMotionDurations` | instantMs / fastMs / normalMs / slowMs / tileCascadeMs / numeralRefreshMs — all collapse to 0 when `reducedMotion` is on (`numeralRefreshMs` is a throttle, not an animation, and stays fixed) |
| `ThemeMotionEasings` | easeOut / easeIn / easeInOut / step |

## Usage

```kotlin
// Wrap app content in theme
AppTheme(mode = ThemeMode.Dark) {
    Text(
        text = "PAYLOAD",           // uppercase at call-site
        style = Theme.typography.overline,
        color = Theme.color.ink3,
    )
    Text(
        text = "Choose what to mirror",
        style = Theme.typography.titleL,
        color = Theme.color.ink,
    )
    Spacer(modifier = Modifier.height(Theme.spacing.spacingL))
}
```

## Testing

```bash
./gradlew :presentation-core-styling:test
```
