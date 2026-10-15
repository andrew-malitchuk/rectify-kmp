# presentation-feature-mirror

The Android device-discovery and mirroring surface: tool detection, the device picker, the
waiting stage and the live frame. Orbit MVI, as `.claude/rules/orbit-mvi.md` prescribes —
with two documented deviations.

## Deviation 1: `MirrorHost`, not `MirrorScreen`

There is **no `Destination` for this surface**, and there will not be one. It is not a page the
navigation graph can reach: it is the set of slots the desktop window
(`compose-application/.../RectifyWindow.kt`) fills — toolbar switch, device chip, status line,
stage. The entry point is therefore `MirrorHost`, which owns no layout and hands
`(state, frame, onIntent)` to a content lambda.

`.claude/rules/navigation.md`'s five-step checklist is deliberately **not** applied: no
`Destination` case, no `navSavedStateConfiguration` entry, no `NavigationHost` branch. This
paragraph exists so a reviewer does not flag the omission as an oversight.

## Deviation 2: pixels never enter the reduced state

`MirrorUiState` carries no frame bytes. Decoded frames are published on
`MirrorViewModel.frame`, a `StateFlow<ImageBitmap?>` **outside** Orbit:

- a `ByteArray`-bearing type compares by identity, so a frame in reduced state could never
  deduplicate, and
- at up to ten frames a second it would mean ten reducer passes and ten full state copies a
  second, inherited by every future feature that reads this state.

Transport statistics are reduced at most once a second (a conflated collector that sleeps
between reductions), not once per frame. Pixels are a stream; UI semantics are a state machine.

## Layout

- `source/mirror/` — `MirrorContract`, `MirrorViewModel`, `MirrorHost`, `MirrorContent`.
  `MirrorContent` exposes `MirrorStageContent` (the four stage surfaces) and `MirrorStatusLine`
  (the status bar's leading slot); both are stateless and read `stringResource` for every
  piece of copy, which is why the semantic row/stage types in the contract carry no labels.
- `core/picker/` — `nextSelectableIndex` / `firstSelectableIndex`.
- `core/discovery/` — `applyDiscovery`, the pure discovery-to-state projection. It is separate
  because of one subtlety worth a direct test: `DiscoveryModel.NoDeviceFound` carries **no**
  emulator list, so the last known AVDs have to be retained across an escalation or the Boot
  shortcut goes empty exactly when the user needs it.
- `core/stats/` — `formatStatusLine`.

## Testing

`commonTest` compiles for **every** target: a backtick test name containing a comma, an
apostrophe or a colon passes on the JVM and fails `compileTestKotlinIosArm64` with
`Name contains illegal characters`. Plain-word camelCase names only.

Two environment facts the ViewModel tests depend on, both in `MirrorFakes.kt`:

- Orbit runs its container event loop on `Dispatchers.Default`, so a test has to pin it with
  `Orbit.configureDefaults { eventLoopDispatcher = { testDispatcher } }` (the library's own
  hook) or nothing the reducer does is visible to `advanceUntilIdle()`. No `orbit-test`
  dependency is used — it is not in the catalog.
- Since coroutines 1.11, `advanceUntilIdle()` ignores `backgroundScope` work, so the state
  subscription that triggers Orbit's lazy `onCreate` must be started with `runCurrent()`.
