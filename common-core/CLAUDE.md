# common-core — Claude Instructions

## Module Purpose

Leaf module providing two cross-layer primitives: `Mapper<I, O>` and the `executeCoroutine` /
`executeResult` coroutine execution helpers. No framework dependencies, no DI wiring — consumed
directly by any module that needs it.

## Package Layout Convention

```
common.core/
├── core/     — reserved for internal/private components (currently empty)
└── source/   — public API, one subfolder per semantic group
    ├── execute/  — executeCoroutine, executeResult
    └── mapper/   — Mapper<I, O>
```

**Rule:** anything `internal` goes in `core/`. Public declarations go in `source/<group>/`.
Never put public declarations directly under `common.core` — always nest under `source/`.

## When to Use `executeCoroutine` vs `executeResult`

| Scenario | Correct helper |
|---|---|
| Suspend function throws on error | `executeCoroutine` |
| Suspend function returns `Result<T>` | `executeResult` |
| You need null to mean "no value produced" | `executeResult` (null monad skips all callbacks) |

## CancellationException Pitfall

`executeCoroutine` routes **all** `Throwable`s — including `CancellationException` — to
`errorBlock`. Code that swallows `CancellationException` breaks structured concurrency.
Always re-throw in `errorBlock`:

```kotlin
errorBlock = { e ->
    if (e is CancellationException) throw e
    handleError(e)
}
```

`executeResult` does not have this problem because `Result` cannot wrap `CancellationException`
(it is re-thrown before `Result.failure` is created by coroutine internals).

## Debounce Behavior

Debounce delay is inside the `try` block. This means:
- Cancellation during the debounce window does **not** call `errorBlock`.
- `loading(false)` **is** still called via `finally` even when cancelled mid-debounce.

Do not add debounce outside the helper — it would bypass the `finally` guard.

## Callback Threading

All callbacks (`loading`, `result`, `errorBlock`) are invoked on **the scope's dispatcher**,
not on `context`. Only the `request` lambda runs on `context` (via `withContext`). This means
callbacks are safe to update UI state when `scope` is a `viewModelScope` (Main dispatcher).

## Adding a New Utility

1. Create a new file under `source/<semantic-group>/`.
2. Declare all symbols `public` — `explicitApi()` is enforced.
3. Document every public declaration with KDoc following the `/kmp-generate-kdoc` skill rules.
4. Update `README.md` Public API table and add a Usage example.
5. This module has no DI module file — utilities are used directly, not injected.
