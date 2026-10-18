package data.device.api.source.error

/**
 * Thrown when an external tool invocation fails, times out, or its binary is missing.
 *
 * This is the cause carried by `Failure.Technical.Process`.
 *
 * @property argv Exact argument vector that was run.
 * @property exitCode Process exit code, or `null` when the process never produced one
 *   (timed out or could not be started).
 * @property stderrTail Tail of the process stderr, capped by the caller.
 */
public class DeviceCommandException(
    public val argv: List<String>,
    public val exitCode: Int?,
    public val stderrTail: String,
) : RuntimeException(
    "${argv.joinToString(" ")} failed (exit ${exitCode ?: "none"}): ${stderrTail.ifBlank { "no output" }}"
) {

    /**
     * Whether this failure is the watchdog expiring, as opposed to any other failure.
     *
     * [exitCode] alone cannot answer this. It is `null` for two different outcomes: the budget
     * running out, and the binary never starting at all (an `IOException` message, also with no
     * exit code). Only the first is a timeout, so the reason shape has to be consulted too.
     *
     * The three cases this separates:
     *
     * | failure | exitCode | stderrTail | isTimeout |
     * |---|---|---|---|
     * | watchdog expired | `null` | starts with [TIMEOUT_REASON_PREFIX] | `true` |
     * | non-zero exit | the code | tool stderr tail | `false` |
     * | could not start | `null` | the `IOException` message | `false` |
     */
    public fun isTimeout(): Boolean = exitCode == null && stderrTail.startsWith(TIMEOUT_REASON_PREFIX)

    public companion object {

        /**
         * The one definition of the timeout reason text, shared by its producer and consumer.
         *
         * The producer formats the reason from this constant (`ProcessRunner`'s watchdog throw
         * site, the only one in the repository); the consumer matches on it via [isTimeout]
         * (the discovery escalation ladder). Both ends are bound to this symbol on purpose: with
         * the match inlined as a string literal at the consumer, rewording the producer's
         * message would silently disarm the ladder while every test stayed green — the livelock
         * returns and nothing goes red. Bound here, that drift is a compile error instead.
         */
        public const val TIMEOUT_REASON_PREFIX: String = "timed out after "
    }
}
