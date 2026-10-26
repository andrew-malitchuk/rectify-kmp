# domain-core

> Core domain models, marker interfaces, and failure types shared across all domain and upper layers.

## Responsibility

Defines the shared vocabulary of the domain layer. All entity models, the base `Model` marker interface, and the sealed `Failure` hierarchy live here. Every module above the data layer depends on these types.

## Dependencies

None. This is a leaf module with no internal project dependencies.

## Public API

| Class / Interface | Description |
|---|---|
| `Model` | Marker interface for all domain models |
| `Failure` | Sealed class — root of the error hierarchy |
| `Failure.Technical` | Unexpected technical errors (wraps `Throwable`) |
| `Failure.Logic` | Business logic errors |
| `ThemeModel` | App theme enumeration (Light, Dark, System) |

## Usage

```kotlin
// Error handling with sealed Failure hierarchy
when (val failure = result.exceptionOrNull()) {
    is Failure.Technical -> log(failure.throwable)
    is Failure.Logic -> showError(failure)
}
```

## Testing

```bash
./gradlew :domain-core:test
```
