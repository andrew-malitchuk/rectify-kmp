# CLAUDE.md — presentation-core-ui

## Purpose

Shared Compose Multiplatform design-system library. Provides every reusable UI component, animation utility, responsive layout helper, icon asset, and the live Styleguide screen that all `presentation-feature-*` modules import from.

## Convention Plugins

- `dev.rectify.io.convention.feature`

## Package Layout

```
presentation/core/ui/
  core/
    configuration/        ← AnimationConfiguration (SHORT=150ms, DEFAULT=500ms, SNACKBAR=5s)
    draw/                 ← WavyPathBuilder + WavyPathDefaults (shared by slider + progress)
    ext/                  ← Int.has(bit) bitwise helper
    modifier/             ← ShimmerProvider composable, LocalShimmerState, shimmerable() modifier
  source/
    icon/                 ← iconCatalogue list + resolveIcon() (public API for icon picker)
    kit/
      atom/               ← indivisible building blocks
        button/           ← IconButton, Toggle, ButtonSizeType
        chip/             ← Chip
        container/        ← SafeContainer, WindowSize, LocalWindowSize, IconContainer, MaxSideDetector
        divider/          ← HorizontalAnimatedDivider
        icon/             ← 22 SVG ImageVector constants (AlertTriangle … Wizard)
        indicator/        ← StatusIndicator + StatusIndicatorState
        input/            ← Input (animated border), FilledTextField (filled), MultilineInput
        progress/         ← WavyProgressIndicator (Canvas, no Material dependency)
        shape/            ← SquircleShape, GentleSquircleShape, CornerSmoothing; util math
        slider/           ← WavySlider (gesture, wavy fill track)
        snackbar/         ← StackedSnackbarHost, StackedSnakbarHostState, rememberStackedSnackbarHostState()
        text/             ← AutoSizeText, SectionHeader, SectionHeaderWithIcon
        toggle/           ← SimpleToggle (animated thumb)
        tween/            ← cupertinoTween()
      molecule/           ← compound components built from atoms
        bar/tab/          ← TabBar + TabBarItem
        button/           ← SegmentedButtonGroup
        chip/             ← ChipGroup
        form/             ← FormSection (label + content slot + error)
        header/           ← ScreenHeader, NavigationHeader, ActionHeader, SimpleHeader
        item/             ← SelectableCard, SelectionRow, LanguageOptionRow
        state/            ← ErrorState
      organism/           ← self-contained sections
        animatedsequence/ ← AnimationSequenceHost, AnimatedItem, SequentialAnimationScope
        bottomsheet/      ← AppBottomSheet, ConfirmationSheet
        pulltorefresh/    ← AppPullToRefreshBox
    showcase/             ← StyleguideScreen (visual QA; all tokens + components)
```

> **Rule:** atoms have no imports from molecules/organisms. Molecules may import atoms only. Organisms may import molecules and atoms. Feature modules import from any layer.

## Key Files

### `core/`

| File | Purpose |
|------|---------|
| `AnimationConfiguration` | Global duration constants (`SHORT`, `DEFAULT`, `SNACKBAR`) |
| `WavyPathBuilder` | Builds sinusoidal `Path`s with amplitude ramping; `WavyPathDefaults` (speed, height) |
| `Int.kt` | `infix fun Int.has(bit)` — bitwise flag checking for button interaction states |
| `Shimmer.kt` | `ShimmerState`, `LocalShimmerState`, `ShimmerProvider`, `shimmerable()` extension |

### `source/icon/`

| File | Purpose |
|------|---------|
| `IconCatalogue` | `iconCatalogue: List<Pair<String, ImageVector>>` (17 entries) + `resolveIcon(key)` |

### Atoms — `source/kit/atom/`

| File | Purpose |
|------|---------|
| `IconButton` | Icon-only; SELECTED bitmask state via `StateIconButton` |
| `Toggle` (button) | Cupertino draggable toggle with aspect-ratio and color animations |
| `Chip` | Selectable chip; filled/outlined by selection state |
| `SafeContainer` | Scaffold with system+IME insets; provides `LocalWindowSize`; hosts snackbar |
| `WindowSize` + `LocalWindowSize` | Adaptive breakpoints; `Dp.toWindowSize()` converter |
| `IconContainer` | Squircle tile: tinted icon on coloured background |
| `MaxSideDetector` | Portrait vs landscape content slot switch |
| `HorizontalAnimatedDivider` | Alpha-animated divider driven by `isVisible` |
| `StatusIndicator` | 6 dp dot + label; `Connected`/`Offline`/`Standby`/`Active` states |
| `Input` | Single-line with animated border, optional regex, clear button |
| `FilledTextField` | Single-line with solid fill (no border); compact form variant |
| `MultilineInput` | Scrollable multi-line text area (120–240 dp) |
| `WavyProgressIndicator` | Canvas-drawn wavy progress; no Material dependency |
| `SquircleShape` / `GentleSquircleShape` | Squircle shape classes with smoothing control |
| `CornerSmoothing` | Presets: `None`=0, `Small`=20, `Medium`=48, `High`=67, `Full`=100 |
| `WavySlider` | Gesture slider with wavy filled track and circular thumb |
| `StackedSnackbarHost` | Host for stacked snackbars with swipe-to-dismiss |
| `StackedSnakbarHostState` | State holder: `showSnackbar()` / `showCustomSnackbar()` |
| `AutoSizeText` | Height-constrained auto-shrinking text; 4-stage algorithm |
| `SectionHeader` | Uppercase section label, bold, letter-spaced |
| `SectionHeaderWithIcon` | Section label row with leading tinted icon |
| `cupertinoTween()` | iOS cubic-bezier tween (0.2833, 0.99, 0.31833, 0.99); 400 ms default |

### Molecules — `source/kit/molecule/`

| File | Purpose |
|------|---------|
| `ScreenHeader` | Back-button toolbar; optional trailing slot; scroll divider |
| `NavigationHeader` | Title + leading back `IconButton` |
| `ActionHeader` | Title + trailing action `IconButton` |
| `SimpleHeader` | Minimal title label |
| `TabBar` | Pill bottom bar; animated icon tinting; reselect callback |
| `SegmentedButtonGroup` | Material3 single-choice segmented row |
| `ChipGroup` | Wrapping `FlowRow` of single-selection `Chip`s |
| `FormSection` | Label + content slot + optional error message |
| `SelectableCard` | Title + subtitle card; brand border when selected |
| `SelectionRow` | Current-value row with trailing chevron (navigate to picker) |
| `LanguageOptionRow` | Language option: name + ISO code, brand border when selected |
| `ErrorState` | Full-screen error placeholder: icon, title, description |

### Organisms — `source/kit/organism/`

| File | Purpose |
|------|---------|
| `AppBottomSheet` | Modal bottom sheet: squircle top, drag handle, optional title |
| `ConfirmationSheet` | Confirm/dismiss sheet; `isDestructive` → red-bordered variant |
| `AppPullToRefreshBox` | `NestedScrollConnection`-based PTR; proportional `WavyProgressIndicator` |
| `AnimationSequenceHost` | Staggered enter/exit sequences; hierarchical parent/child hosts |
| `AnimatedItem` | Single item participating in a sequence (index, delay, transitions) |

### `source/showcase/`

| File | Purpose |
|------|---------|
| `StyleguideScreen` | Scrollable visual QA: all tokens + every design-system component |

## Module Dependencies

- `presentation-core-styling` — design tokens (colors, typography, spacing, shapes, corner tokens)
- `compose.material3` — Material 3 components and theming
- `stately-collections` — `ConcurrentMutableMap` (thread-safe animation item registry)
- `atomicfu` — atomic boolean for animation-in-progress tracking
- `kermit` — structured logging inside `AnimationSequenceHost`

## Key Design Decisions

### IconButton bitmask pattern
`ButtonInteractionState` encodes hover/pressed/focused/selected as bit flags. `ButtonColor` implementations resolve border/foreground/background from the combined flag integer. This avoids per-state branching at the draw layer — used by `IconButton`.

### Wavy path sharing
`WavyPathBuilder` and `WavyPathDefaults` live in `core/draw/` and are used by both `WavyProgressIndicator` (progress fill) and `WavySlider` (track fill). Neither atom depends on the other.

### Snackbar stack concurrency
`StackedSnakbarHostState` uses a `ConcurrentMutableMap` to manage simultaneous snackbar items. `StackedSnackbarAnimation` wraps spring or tween specs so callers choose a physics feel without touching animation code.

### AnimationSequenceHost lifecycle
`AnimatedItem` registers itself with `LocalSequentialAnimationHost` via `DisposableEffect` and deregisters on leave. The host calculates `enterDuration + delay` per item to sequence transitions correctly across a parent/child tree.

## Build

```shell
./gradlew :presentation-core-ui:build
```
