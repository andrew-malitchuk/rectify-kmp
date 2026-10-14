# common-core

> Cross-layer utilities — type-mapping contract and coroutine execution helpers for all KMP modules.

## Responsibility

Provides two foundational primitives reused across data, domain, and presentation layers:
a `Mapper<I, O>` functional interface as the canonical model-transformation contract, and
`executeCoroutine` / `executeResult` helpers that launch suspend operations with structured
loading, result, and error callbacks.

This is a leaf module — it has no internal project dependencies and introduces no framework coupling.

## Dependencies

None.

## Public API

| Class / Function | Description |
|---|---|
| `Mapper<I, O>` | Functional interface for model transformations: `fun map(input: I): O` |
| `executeCoroutine<T>()` | Launches a suspending request with `loading`, `result`, and `errorBlock` callbacks |
| `executeResult<T>()` | Launches a suspending `Result<T>` request, folding success/failure into separate callbacks |

## Usage

### Mapper

Implement as a named object for complex mappings or pass as a lambda for one-liners:

```kotlin
// Named object
object UserNetworkMapper : Mapper<UserNetworkModel, UserDomainModel> {
    override fun map(input: UserNetworkModel): UserDomainModel =
        UserDomainModel(
            id = input.id,
            name = input.name.orEmpty(),
        )
}

// Lambda (fun interface)
val toDisplayName: Mapper<UserDomainModel, String> = Mapper { it.name }
```

### executeCoroutine

Use when your suspend operation throws on failure (no `Result` wrapper):

```kotlin
executeCoroutine(
    scope = viewModelScope,
    loading = { isLoading -> _state.update { it.copy(isLoading = isLoading) } },
    result = { data -> _state.update { it.copy(items = data.orEmpty()) } },
    errorBlock = { error ->
        if (error is CancellationException) throw error  // re-throw cancellation
        _state.update { it.copy(error = error.message) }
    },
    request = { repository.getItems() },
)
```

### executeResult

Use when your suspend operation returns `Result<T>` (e.g. network calls wrapped by a repository):

```kotlin
executeResult(
    scope = viewModelScope,
    loading = { isLoading -> _state.update { it.copy(isLoading = isLoading) } },
    result = { data -> _state.update { it.copy(items = data.orEmpty()) } },
    errorBlock = { error -> _state.update { it.copy(error = error.message) } },
    request = { repository.fetchItems() },  // returns Result<List<Item>>
)
```

### Debounce (search input)

```kotlin
executeCoroutine(
    scope = viewModelScope,
    debounce = 300L,
    request = { repository.search(query) },
    result = { results -> _state.update { it.copy(results = results.orEmpty()) } },
)
```

## Choosing Between `executeCoroutine` and `executeResult`

| Scenario | Use |
|---|---|
| Repository/DataSource throws on error | `executeCoroutine` |
| Repository returns `Result<T>` | `executeResult` |
| You need to distinguish error cause vs. null success | `executeResult` |

## Pitfalls

**`CancellationException` in `executeCoroutine`**: when a coroutine is cancelled, the
`errorBlock` is called with a `CancellationException`. If your error handler does not
re-throw it, the coroutine cooperative cancellation contract is broken. Always check and
re-throw:

```kotlin
errorBlock = { e ->
    if (e is CancellationException) throw e
    handleError(e)
},
```

**`executeResult` with null return**: if `request` returns `null`, neither `result` nor
`errorBlock` is called. This is intentional — `null` signals the caller opted out of
producing a value for this invocation.

**Debounce is inside the `try` block**: cancellation during a debounce window does not
invoke `errorBlock`. The `CancellationException` propagates normally and `loading(false)`
is still called via `finally`.

## Testing

```bash
./gradlew :common-core:test
```
