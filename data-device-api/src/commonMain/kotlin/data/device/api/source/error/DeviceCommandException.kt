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
)
