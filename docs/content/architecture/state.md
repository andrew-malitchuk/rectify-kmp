# State management

Feature screens use [Orbit MVI](https://orbit-mvi.org/). `presentation-feature-mirror` is the
only feature module and therefore the reference shape — match it rather than inventing a
variant.

## Four files

Under `source/<feature>/`:

| File | Holds |
|---|---|
| `XContract.kt` | `XState` (UI snapshot, defaults for every field), `XSideEffect` (one-shot events), `XIntent` (every user action) |
| `XViewModel.kt` | `ContainerHost<XState, XSideEffect>`, intent dispatch, all the logic |
| `XScreen.kt` | Resolves the ViewModel from Koin, collects state and side effects |
| `XContent.kt` | Stateless. No Koin, no ViewModel — takes state and emits `onIntent` |

The split exists so that everything below `XScreen` can be rendered from literals. The content
layer never resolves a dependency, which is why the whole shell can be previewed and screenshot
without a running Koin graph.

## The shape

```kotlin
class MirrorViewModel(/* use cases */) :
    ContainerHost<MirrorUiState, MirrorSideEffect>, ViewModel() {

    override val container = container<MirrorUiState, MirrorSideEffect>(MirrorUiState()) {
        // init-time work only
    }

    fun handleIntent(intent: MirrorIntent) = when (intent) {
        is MirrorIntent.SelectPlatform -> selectPlatform(intent.platform)
        MirrorIntent.ResolveAndSend -> resolveAndSend()
        // …exhaustive, no else
    }

    private fun selectPlatform(platform: PlatformModel) = intent {
        reduce { state.copy(platform = platform, /* … */) }
    }
}
```

The `when` is exhaustive with no `else`. A new intent that nobody handles is then a compile
error rather than a click that silently does nothing.

## Derived state, not stored state

`MirrorUiState` stores facts and derives presentation from them. The stage is the clearest
example:

```kotlin
val stage: MirrorStage
    get() = when {
        isPickerOpen -> MirrorStage.Picker
        verify != null -> MirrorStage.Verify
        frozen != null -> MirrorStage.Frozen
        session is MirrorSession.Streaming -> MirrorStage.Mirror
        else -> MirrorStage.NoSignal
    }
```

The ordering is load-bearing and documented at the property. `Verify` outranks `Frozen` because
a verification run carries its own before/after pixels — showing the pinned live frame
underneath would be a third frame nobody asked for.

Storing `stage` as a field instead would mean every reducer has to remember to update it, and
the first one that forgets produces a state that contradicts itself.

Predicates work the same way. `hasDeviceTarget` drives both the side panel's visibility and the
toolbar's trailing actions, so the two cannot disagree about what "has a device" means.

## Testing

The ViewModel is where the tests are, and they go through `handleIntent` rather than calling
private methods. Two environment facts they all depend on:

- Orbit's dispatcher defaults are **process-wide**. A test class that does not pin them can
  pass only because a sibling class already did, and then fail when the class order changes.
  Every suite sets them in `@BeforeTest` and calls `Orbit.resetDefaults()` in `@AfterTest` — on
  every path, not just the happy one.
- Assertions compare *lists of labelled strings* rather than individual values, so a failure
  reports every field at once instead of stopping at the first mismatch.

```kotlin
assertEquals(
    expected = listOf("platform=Ios", "rows=0", "selectedDeviceId=null"),
    actual = listOf(
        "platform=${state.platform}",
        "rows=${state.rows.size}",
        "selectedDeviceId=${state.selectedDeviceId}",
    ),
)
```
