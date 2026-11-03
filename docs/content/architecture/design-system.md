# Design system

Two modules: `presentation-core-styling` holds the tokens, `presentation-core-ui` holds the
components built from them. This documentation site is themed from the same values — see
[the note at the end](#this-site).

## Tokens

`Theme` exposes grouped token sets, reached as `Theme.color.ink`, `Theme.spacing.spacingL`,
`Theme.typography.body` and so on. Nothing in the UI hardcodes a colour or a size.

### Colour

Authored in OKLCH and converted to sRGB at build time, with the original triplet kept in a
trailing comment for traceability.

| Group | Light | Dark |
|---|---|---|
| `bg` | `#F6F6F5` | `#1E1E20` |
| `chrome` | `#EDEDEB` | `#262628` |
| `stage` | `#E7E7E4` | `#151517` |
| `surface` | `#FFFFFF` | `#2B2B2E` |
| `ink` / `ink2` / `ink3` | `#1A1A1C` / `#5B5B61` / `#8B8B91` | `#F1F1F2` / `#A3A3AA` / `#707077` |
| `focus` | `#007DF2` | `#53A0FF` |

Lines and fills are **alpha over the surface**, not opaque greys: `line` is
`rgba(0,0,0,.08)`, `line2` `.14`, `hover` `.045`, `seg` `.06`, `codeBg` `.035`. That is why
they read correctly on `surface` and on `stage` without a second set of tokens.

The signal colours are identical in both themes by design:

| Token | Value | Means |
|---|---|---|
| <span class="dot dot-annotate"></span> `annotate` | `#F13936` | An annotation rectangle |
| <span class="dot dot-fixed"></span> `sentFixed` | `#3EAB5E` | Sent, or verified fixed |
| <span class="dot dot-warn"></span> `warnStillOff` | `#E69825` | Working, or still off |
| <span class="dot dot-measure"></span> `measure` | `#007DF2` | Measure mode |

### Type

Roboto Flex for everything a person reads; JetBrains Mono for everything a machine reports —
versions, resolutions, fps, ids, file names, key hints. The mono roles carry the `tnum`
feature so digits keep a fixed advance and live values stop jittering as they update.

The scale is deliberately small — `body` is 13sp — because Rectify is a dense desktop tool.

### Spacing and radii

Spacing: 4, 8, 16, 24, 32, 48, 64. Radii: 8, 12, 16, 22, 28, and a pill.

## The UI kit

`presentation-core-ui` is organised atomically: **58 atoms**, **21 molecules**, **14
organisms**.

- **atom** — a single control or mark: `AppButton`, `Chip`, `CounterBadge`, `StatusIndicator`,
  icons, the stage spinner.
- **molecule** — a small composition with one job: `DeviceRow`, `VerificationRow`,
  `StatusLine`, `EmulatorSelect`.
- **organism** — a region of the window: `RectifyWindowShell`, `DevicePickerPanel`,
  `NoSignalStage`, `SidePanelFrame`.

Components take their width from the caller rather than filling by default. `StatusLine`
learned this the hard way: an internal `fillMaxWidth()` made every use a full-bleed band, which
was right for the send overlay and wrong for the verify progress indicator.

## Contrast is tested, not asserted by eye

`PaletteContrastTest` sweeps both shipped palettes. It lives in the styling module's own
`src/test` because both palettes are `internal`.

The important detail: it **composites the alpha tokens over their base surface** before
measuring. Measuring `rgba(0,0,0,.08)` on its own reports nonsense.

Hairlines at 8–14% alpha cannot reach WCAG 3:1 by construction. Rather than asserting a target
the design can never meet and leaving a permanently red suite, those are recorded as frozen
baselines with `// WAIVED:` comments — 52 of them — each naming why. A waiver you can read beats
a test nobody runs.

There is also a test that every declared colour token is either swept or explicitly exempt, so
a new token cannot be added and silently skipped.

## Previews instead of a UI-test harness

There is no `runComposeUiTest` and no `ui-test-junit4` in this project. Rendering is covered by
`@Preview` pairs in light and dark.

Every screen-level content composable ends with a private preview, named
`<ComposableName>Preview`, wrapped in `AppTheme { }` — a preview outside `AppTheme` crashes on
the first `Theme.*` access. Previews take a default-constructed state and no-op callbacks; they
never wire a real ViewModel or Koin.

Introducing a UI-test harness would be a real infrastructure decision, not a casual addition.

## This site

The CSS here is generated from the token values above: same palette, same two fonts, same
radii, same alpha-over-surface line treatment, both themes.

The one deliberate departure is the type scale. The app sets `body` at 13sp because it is
inspected at arm's length; 13px of running prose in a browser is uncomfortable, so sizes are
scaled up. Weights, line-height ratios and font choices are unchanged.
