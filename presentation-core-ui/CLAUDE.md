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
        annotation/       ← AnnotationMark, MeasureMark
        badge/            ← CounterBadge, GlyphBadge
        button/           ← AppButton (3 variants × 6 sizes), IconButton, Toggle, ButtonSizeType
        chip/             ← Chip
        container/        ← SafeContainer, WindowSize, LocalWindowSize, IconContainer, MaxSideDetector
        divider/          ← HorizontalAnimatedDivider
        glyph/            ← ErrorGlyph
        handle/           ← PanelResizeHandle (8 dp grab strip; owns the stage/panel seam, reports drag deltas, owns no width)
        icon/             ← 22 SVG ImageVector constants (AlertTriangle … Wizard)
        indicator/        ← StatusIndicator + StatusIndicatorState, RadioMark
        input/            ← Input (animated border), FilledTextField (filled), MultilineInput
        keycap/           ← KeyCap
        pill/             ← StatusPill + StatusPillTone
        progress/         ← SegmentedProgressIndicator (Canvas, no Material dependency)
        slider/           ← WavySlider (gesture, wavy fill track)
        snackbar/         ← StackedSnackbarHost, StackedSnakbarHostState, rememberStackedSnackbarHostState()
        tag/              ← MeasureTag
        text/             ← AutoSizeText, SectionHeader, SectionHeaderWithIcon
        tile/             ← BentoTile
        toggle/           ← SimpleToggle (animated thumb)
        tween/            ← cupertinoTween()
      molecule/           ← compound components built from atoms
        annotation/       ← AnnotationPopover, Ruler
        bar/tab/          ← TabBar + TabBarItem, SidebarTabs + SidebarTabItem
        button/           ← SegmentedButtonGroup
        card/             ← PlatformToolCard + PlatformToolState
        chip/             ← ChipGroup, DeviceChip + DeviceChipState
        command/          ← CommandBlock
        form/             ← FormSection (label + content slot + error)
        header/           ← ScreenHeader, NavigationHeader, ActionHeader, SimpleHeader
        item/             ← SelectableCard, SelectionRow, LanguageOptionRow
        mirror/           ← MirrorFrameView
        row/              ← SourceListRow + SourceListHeader, AnnotationRow, HierarchyRow,
                            HistoryRow, KeyValueRow, VerificationRow
        select/           ← EmulatorSelect
        state/            ← ErrorState
        status/           ← StatusLine + StatusStep, FeedbackBar
      organism/           ← self-contained sections
        animatedsequence/ ← AnimationSequenceHost, AnimatedItem, SequentialAnimationScope
        annotation/       ← CommentPopover, MeasureCard
        bottomsheet/      ← AppBottomSheet, ConfirmationSheet
        error/            ← ErrorCard
        picker/           ← DevicePickerPanel + PickerPlatform / PickerDevice / PickerDeviceTrailing
        pulltorefresh/    ← AppPullToRefreshBox
        stage/            ← NoSignalStage
        shell/            ← RectifyWindowShell, ShellToolbar, ShellStatusBar, StageSurface,
                            SidePanelFrame + ShellState / ShellTab / ShellWindowDefaults / clampPanelWidth
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
| `PanelResizeHandle` | 8 dp horizontal grab strip filled with `stage` + the single seam hairline; emits drag deltas, no clamping or persistence |
| `StatusIndicator` | 7 dp dot + mono label; `Live`/`Frozen`/`Dumping`/`Sent`/`Dropped`/`Offline` |
| `Input` | Single-line with animated border, optional regex, clear button |
| `FilledTextField` | Single-line with solid fill (no border); compact form variant |
| `MultilineInput` | Scrollable multi-line text area (120–240 dp) |
| `WavySlider` | Gesture slider with wavy filled track and circular thumb |
| `StackedSnackbarHost` | Host for stacked snackbars with swipe-to-dismiss |
| `StackedSnakbarHostState` | State holder: `showSnackbar()` / `showCustomSnackbar()` |
| `AutoSizeText` | Height-constrained auto-shrinking text; 4-stage algorithm |
| `SectionHeader` | Uppercase section label, bold, letter-spaced |
| `SectionHeaderWithIcon` | Section label row with leading tinted icon |
| `cupertinoTween()` | iOS cubic-bezier tween (0.2833, 0.99, 0.31833, 0.99); 400 ms default |
| `AppButton` | The Rectify button — 3 variants × 6 interaction states × 6 size tiers; `busy` implies disabled |
| `RadioMark` | Single-choice mark: ink dot when picked, ring otherwise. Canvas-drawn, no ripple, no semantics — the clickable parent owns `Role.RadioButton` |
| `StatusPill` | `● Fixed` / `● In progress` — neutral fill, only the dot carries the tone |
| `CounterBadge` | Circular count + optional label (`0 staged`); `Empty`/`Staged`/`Sent` fills |
| `GlyphBadge` | Square badge for a short glyph — annotation index, `✓`, `!` |
| `KeyCap` | A key rendered as a cap — `⌘`, `↵`, `esc`. Presentational; never handles input |
| `AnnotationMark` | Numbered mark pinned on the frame; state drives colour and border |
| `MeasureMark` | Measurement endpoint mark; role drives the outline treatment |
| `MeasureTag` | Signal-filled chip labelling a mark's size or a ruler extent |
| `ErrorGlyph` | Leading glyph naming the kind of problem an error reports |
| `BentoTile` | Fixed-height bento grid tile (free-form heights deliberately not allowed) |
| `SegmentedProgressIndicator` | Segmented progress bar; the RECTIFY replacement for the old wavy indicator |

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
| `SourceListRow` + `SourceListHeader` | Device row in the picker: `RadioMark`, mono serial, trailing slot. `enabled = false` drops the click handler *and* marks the row disabled (A11Y-6), it does not merely dim |
| `PlatformToolCard` | `PLATFORM` card (comp 01): name + trailing `RadioMark`, tool dot + mono `adb 35.0.1 · /opt/homebrew/bin` line; ink outline when selected |
| `EmulatorSelect` | AVD dropdown (M3 `DropdownMenu`). Zero AVDs → disabled with an explanatory label, never an empty open menu |
| `MirrorFrameView` | Dumb, stateless aspect-fit `ImageBitmap` frame; opaque only where it covers, `FilterQuality.None`, device named in the contentDescription |
| `SidebarTabs` | Panel tab row (`Annotations 3 · Payload · History 1`); filled chip selection |
| `DeviceChip` | Toolbar device chip; connection state drives the dot |
| `CommandBlock` | Copyable shell command — `$ brew install …` |
| `StatusLine` | Pipeline line `Streaming › Frozen › Dumping › Sent`; per-step state |
| `FeedbackBar` | Outcome bar; the outcome drives the leading mark |
| `AnnotationRow` / `HierarchyRow` / `HistoryRow` / `KeyValueRow` / `VerificationRow` | Sidebar list rows: annotation, dumped node, past session, key/value, re-check result |
| `AnnotationPopover` | Gutter popover carrying a comment off a crowded frame |
| `Ruler` | Horizontal ruler measuring the frame in device pixels |

### Organisms — `source/kit/organism/`

| File | Purpose |
|------|---------|
| `AppBottomSheet` | Modal bottom sheet: squircle top, drag handle, optional title |
| `ConfirmationSheet` | Confirm/dismiss sheet; `isDestructive` → red-bordered variant |
| `AppPullToRefreshBox` | `NestedScrollConnection`-based PTR; proportional `WavyProgressIndicator` |
| `AnimationSequenceHost` | Staggered enter/exit sequences; hierarchical parent/child hosts |
| `AnimatedItem` | Single item participating in a sequence (index, delay, transitions) |
| `RectifyWindowShell` | Window slot contract: `Column[toolbar, Row[stage, handle, panel], statusBar]`; `sidePanel == null` composes neither handle nor panel |
| `ShellToolbar` | Top chrome bar, 52 dp, fixed named slots + traffic-light leading inset |
| `ShellStatusBar` | Bottom chrome bar, 28 dp, leading / trailing slots |
| `StageSurface` | Bezel-less mirror region on `Theme.color.stage` + Canvas dot grid |
| `SidePanelFrame` | Panel chrome: `SidebarTabs` header (Notes · Payload · History), per-tab content slot |
| `ShellState` / `ShellTab` / `ShellWindowDefaults` | Hoisted shell state, the three tabs, window & panel geometry + `clampPanelWidth` / `shortcutToTab` |
| `DevicePickerPanel` | First-run picker (comps 01/03). Fills the stage content slot **opaquely** with `Theme.color.bg` so no dots read through; owns a `FocusRequester` + root `onPreviewKeyEvent` (↑↓/↵ → intents) and **no selection state** |
| `NoSignalStage` | Empty stage (comp 02): dashed phone outline + numbered recovery steps + `EmulatorSelect`/`Boot`. Renders **on** the dot grid — paints no background |
| `ErrorCard` | The standard actionable error report |
| `CommentPopover` | Always-dark comment popover |
| `MeasureCard` | Measured-dimension card |

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

### Picker copy must agree with picker behaviour
`DevicePickerPanel` ships an explicit `Start mirroring` CTA — nothing mirrors until `onStart`
fires. `NoSignalStage`'s subtitle therefore defaults to
`Plug one in or boot an emulator, then press Start mirroring.` and **not** comp 02's
`Mirroring starts on its own as soon as adb sees one.`, which promises an auto-start this build
does not implement. The two are a pair: changing one without the other makes the app lie about
itself. The shipped copy lives in `presentation-core-localisation` as `mirror_waiting_subtitle`.

### Kit components take copy as parameters, not resources
`presentation-core-ui` depends on `presentation-core-styling` only — never on
`presentation-core-localisation`. Every string a kit component renders is a parameter with an
English default (so the `@Preview` works standalone); feature modules pass
`stringResource(Res.string.…)`. Machine output (serials, versions, `adb` command lines, fps)
is rendered mono and deliberately carries no resource key at all.

## Build

```shell
./gradlew :presentation-core-ui:build
```
