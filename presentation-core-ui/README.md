# presentation-core-ui

Shared Compose Multiplatform design-system library for the RECTIFY app. Provides all reusable UI primitives — atoms, molecules, and organisms — that feature modules consume. Also ships responsive layout utilities, animation infrastructure, icon assets, and a live Styleguide screen for visual QA.

## Module Type

Library (KMP + Compose Multiplatform)

## Architecture

```
presentation/core/ui/
  core/                     ← internal framework utilities
    configuration/          ← animation durations / transitions
    draw/                   ← wavy path math (shared by WavySlider + WavyProgressIndicator)
    ext/                    ← bitwise extension helpers
    modifier/               ← shimmer loading system (ShimmerProvider, shimmerable())
  source/
    icon/                   ← IconCatalogue + resolveIcon() for the icon picker
    kit/
      atom/                 ← indivisible building blocks
      molecule/             ← compound components built from atoms
      organism/             ← self-contained sections (sheets, sequences, pull-to-refresh)
    showcase/               ← StyleguideScreen for visual QA
```

The kit follows a strict **atom → molecule → organism** layering rule. Feature modules import from any layer they need.

---

## Atoms

### Buttons

| Component | Description |
|-----------|-------------|
| `Button` | General-purpose button with `ButtonStyle` and `ButtonSizeType` |
| `IconButton` | Icon-only button with selected state |
| `Toggle` (button) | Cupertino-style draggable on/off toggle |

**`ButtonStyle`** — `Primary` (outlined), `Secondary` (filled), `Text` (transparent).
**`ButtonSizeType`** — `Small`, `Medium`, `Large`, `XLarge`, `XXLarge`.

### Chip

| Component | Description |
|-----------|-------------|
| `Chip` | Selectable chip; filled when selected, outlined otherwise |

### Container

| Component | Description |
|-----------|-------------|
| `SafeContainer` | Scaffold with system-bar + IME insets; provides `LocalWindowSize`; hosts snackbar |
| `WindowSize` | Enum: `Compact` (<600dp), `Medium` (600–839dp), `Expanded` (≥840dp) |
| `LocalWindowSize` | CompositionLocal providing the current `WindowSize` |
| `IconContainer` | Squircle-shaped icon tile with tinted foreground on a tinted background |
| `MaxSideDetector` | Portrait vs landscape content slot switch |

### Divider

| Component | Description |
|-----------|-------------|
| `HorizontalAnimatedDivider` | Full-width divider with alpha fade; driven by `isVisible` |

### Indicator

| Component | Description |
|-----------|-------------|
| `StatusIndicator` | 6 dp dot + uppercase label; states: `Connected`, `Offline`, `Standby`, `Active` |

### Input

| Component | Description |
|-----------|-------------|
| `Input` | Single-line with animated border, optional regex validation, clear button |
| `FilledTextField` | Single-line with solid fill background; compact form style |
| `MultilineInput` | Multi-line scrollable text area (120–240 dp) |

### Progress

| Component | Description |
|-----------|-------------|
| `WavyProgressIndicator` | Wavy linear progress bar on Canvas; continuous wave shift, optional spread animation |

### Shape

| Class | Description |
|-------|-------------|
| `SquircleShape` | Squircle with configurable corner smoothing (0–100) |
| `GentleSquircleShape` | Squircle that morphs to arcs at large radii |
| `CornerSmoothing` | Presets: `None`=0, `Small`=20, `Medium`=48, `High`=67, `Full`=100 |

### Slider

| Component | Description |
|-----------|-------------|
| `WavySlider` | Gesture-driven slider with wavy fill track and circular thumb |

### Snackbar

| Component / Class | Description |
|-------------------|-------------|
| `StackedSnackbarHost` | Host composable with swipe-to-dismiss |
| `StackedSnakbarHostState` | State: `showSnackbar()` / `showCustomSnackbar()` |
| `rememberStackedSnackbarHostState()` | Factory composable |
| `StackedSnackbarAnimation` | `Bounce` (spring) or `Slide` (tween) |
| `StackedSnackbarDuration` | `Short` (4 s), `Long` (10 s), `Indefinite` |

### Text

| Component | Description |
|-----------|-------------|
| `AutoSizeText` | Height-constrained auto-shrinking text; 4-stage algorithm |
| `SectionHeader` | Uppercase section label, bold, letter-spaced |
| `SectionHeaderWithIcon` | Section label row with leading tinted icon |

---

## Molecules

### Headers

| Component | Description |
|-----------|-------------|
| `ScreenHeader` | Back-button toolbar with optional trailing slot and scroll divider |
| `NavigationHeader` | Title + back `IconButton` |
| `ActionHeader` | Title + trailing action `IconButton` |
| `SimpleHeader` | Minimal title label with padding |

### Tab Bar

| Component | Description |
|-----------|-------------|
| `TabBar` | Pill-shaped bottom bar with animated icon tinting; reselect callback |

### Buttons

| Component | Description |
|-----------|-------------|
| `SegmentedButtonGroup` | Material3 single-choice segmented buttons |

### Chips

| Component | Description |
|-----------|-------------|
| `ChipGroup` | Horizontally-wrapping `FlowRow` of single-selection `Chip`s |

### Form

| Component | Description |
|-----------|-------------|
| `FormSection` | Label above + content slot + optional error message below |

### Items

| Component | Description |
|-----------|-------------|
| `SelectableCard` | Title + subtitle card; brand border when selected |
| `SelectionRow` | Uppercase label + trailing chevron; tapping opens a picker screen |
| `LanguageOptionRow` | Language picker row: name, ISO code, brand border when selected |

### State

| Component | Description |
|-----------|-------------|
| `ErrorState` | Full-screen placeholder: warning icon, title, description |

---

## Organisms

| Component | Description |
|-----------|-------------|
| `AppBottomSheet` | Modal bottom sheet: squircle-rounded top, drag handle, optional title, nav-bar padding |
| `ConfirmationSheet` | Confirm/dismiss sheet; `isDestructive` → red-bordered confirm button |
| `AppPullToRefreshBox` | `NestedScrollConnection`-based PTR; proportional `WavyProgressIndicator` during pull |
| `AnimationSequenceHost` | Staggered enter/exit for child items; hierarchical parent/child host support |
| `AnimatedItem` | Child registered with `AnimationSequenceHost` (index, delay, transitions) |

---

## Shimmer System

```kotlin
ShimmerProvider {
    Box(modifier = Modifier.shimmerable(enabled = isLoading)) { /* content */ }
}
```

`ShimmerProvider` creates a `ShimmerState` and provides it via `LocalShimmerState`. The `shimmerable()` modifier consumes the local state to conditionally apply the shimmer effect.

## Icon System

```kotlin
// Full catalogue (17 icons) for the icon-picker UI
val icons: List<Pair<String, ImageVector>> = iconCatalogue

// Resolve a persisted key back to an ImageVector (fallback: HardDrive)
val icon: ImageVector = resolveIcon("wifi")
```

---

## Dependencies

| Library | Purpose |
|---------|---------|
| `presentation-core-styling` | Design tokens (colors, typography, spacing, corner tokens) |
| `compose.material3` | Material 3 components and theming |
| `stately-collections` | Thread-safe `ConcurrentMutableMap` for snackbar + animation state |
| `atomicfu` | Atomic boolean for animation progress tracking |
| `kermit` | Structured logging inside `AnimationSequenceHost` |

## Build

```shell
./gradlew :presentation-core-ui:build
```
