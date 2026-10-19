package domain.usecase.impl.core

import domain.core.source.monad.Failure
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes [block] and wraps its result in [Result.success].
 *
 * If [block] throws a [Failure] (a domain-level error), it is propagated as-is
 * inside [Result.failure]. Any other [Throwable] is first mapped through
 * [errorMapper] before being wrapped, ensuring that infrastructure exceptions
 * are translated into the appropriate [Failure] subtype.
 *
 * **[CancellationException] is rethrown, never mapped.** Cancellation is not a failure of the
 * operation, it is the caller's coroutine unwinding: swallowing it into a `Result.failure`
 * leaves a cancelled caller running on past the suspension point it was cancelled at, so a
 * job someone cancelled still reduces state and still writes to the database. Every use case
 * in this module goes through here, so the rethrow belongs here and not at a call site.
 *
 * @param T The success type.
 * @param errorMapper Converts unexpected [Throwable]s into a [Failure].
 * @param block The suspending computation to execute.
 * @return [Result.success] with the computed value, or [Result.failure] with a [Failure].
 */
internal suspend inline fun <T> resultLauncher(
    errorMapper: (Throwable) -> Failure,
    crossinline block: suspend () -> T,
): Result<T> = try {
    Result.success(block())
} catch (failure: Failure) {
    Result.failure(failure)
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    Result.failure(errorMapper(throwable))
}
