package common.core.source.execute

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Launches a suspendable [request] in [scope], routing its outcome to the provided callbacks.
 *
 * Signals loading state via [loading] before and after execution, delivers the result via
 * [result], and routes any thrown exception to [errorBlock]. An optional [debounce] delay
 * is applied before the request starts, which is useful for suppressing rapid re-triggers
 * (e.g. search input).
 *
 * @param T The type of the value produced by [request].
 * @param context The [CoroutineContext] on which [request] runs. Defaults to [Dispatchers.Default].
 * @param scope The [CoroutineScope] that owns the launched coroutine's lifecycle.
 * @param debounce Optional delay in milliseconds applied before [request] is invoked.
 * @param loading Callback invoked on [scope]'s dispatcher with `true` before [request] starts
 *   and `false` after it finishes or fails, including on cancellation. Never invoked in parallel.
 * @param result Callback invoked on [scope]'s dispatcher with the return value of [request]
 *   on success. Receives `null` if [request] itself returns `null`.
 * @param errorBlock Callback invoked on [scope]'s dispatcher with the caught [Throwable] if
 *   [request] throws. Receives [CancellationException] on coroutine cancellation — callers
 *   that do not want to handle cancellation should re-throw it.
 * @param request The suspendable operation to execute. Runs on [context] via [withContext].
 *   Throwing from [request] routes to [errorBlock].
 * @return The [Job] backing the launched coroutine.
 * @see executeResult
 */
public fun <T> executeCoroutine(
    context: CoroutineContext = Dispatchers.Default,
    scope: CoroutineScope,
    debounce: Long? = null,
    loading: ((Boolean) -> Unit)? = null,
    result: ((T?) -> Unit)? = null,
    errorBlock: ((Throwable) -> Unit)? = null,
    request: suspend CoroutineScope.() -> T?,
): Job {
    return scope.launch {
        try {
            debounce?.let {
                // NOTE: Delay runs inside the try block so cancellation during debounce
                // does not call errorBlock — CancellationException propagates normally.
                delay(it)
            }
            loading?.invoke(true)
            val response = withContext(context) { request() }
            result?.invoke(response)
        } catch (e: Throwable) {
            errorBlock?.invoke(e)
        } finally {
            // NOTE: loading(false) is always called, including on cancellation,
            // to prevent the caller from being stuck in a loading state.
            loading?.invoke(false)
        }
    }
}

/**
 * Launches a suspendable [request] that returns a [Result] in [scope], unwrapping success
 * and failure into separate callbacks.
 *
 * Behaves like [executeCoroutine] but folds the [Result] monad so that [result] only
 * receives successful values and [errorBlock] receives both [Result.failure] causes and
 * any uncaught exceptions from [request] itself.
 *
 * @param T The type of the successful value inside [Result].
 * @param context The [CoroutineContext] on which [request] runs. Defaults to [Dispatchers.Default].
 * @param scope The [CoroutineScope] that owns the launched coroutine's lifecycle.
 * @param debounce Optional delay in milliseconds applied before [request] is invoked.
 * @param loading Callback invoked on [scope]'s dispatcher with `true` before [request] starts
 *   and `false` after it finishes or fails, including on cancellation. Never invoked in parallel.
 * @param result Callback invoked on [scope]'s dispatcher with the unwrapped success value
 *   on [Result.success].
 * @param errorBlock Callback invoked on [scope]'s dispatcher with the [Throwable] on
 *   [Result.failure] or if [request] throws directly.
 * @param request The suspendable operation returning a nullable [Result]. Runs on [context]
 *   via [withContext]. If `null` is returned, neither [result] nor [errorBlock] is invoked.
 * @return The [Job] backing the launched coroutine.
 * @see executeCoroutine
 */
public fun <T> executeResult(
    context: CoroutineContext = Dispatchers.Default,
    scope: CoroutineScope,
    debounce: Long? = null,
    loading: ((Boolean) -> Unit)? = null,
    result: ((T?) -> Unit)? = null,
    errorBlock: ((Throwable) -> Unit)? = null,
    request: suspend CoroutineScope.() -> Result<T>?,
): Job {
    return scope.launch {
        try {
            debounce?.let {
                delay(it)
            }
            loading?.invoke(true)
            val monad = withContext(context) { request() }
            // NOTE: null monad means the caller explicitly opted out of producing a result.
            // Neither result nor errorBlock is called — this is intentional, not a bug.
            monad?.fold(
                onSuccess = { result?.invoke(it) },
                onFailure = { errorBlock?.invoke(it) },
            )
        } catch (e: Throwable) {
            errorBlock?.invoke(e)
        } finally {
            loading?.invoke(false)
        }
    }
}
