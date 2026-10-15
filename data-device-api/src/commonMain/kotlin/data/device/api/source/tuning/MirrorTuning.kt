package data.device.api.source.tuning

/**
 * The one calibration surface for discovery and mirroring.
 *
 * Real hardware differs: a slow phone needs a higher capture floor, a busy host needs a
 * longer timeout. Every number here is named so it can be tuned in one place instead of
 * being rediscovered as a literal inside a loop.
 */
public object MirrorTuning {

    /** Minimum delay between two captures, in milliseconds. */
    public const val CaptureFloorMs: Long = 100L

    /** Timeout for a single screen capture, in milliseconds. */
    public const val CaptureTimeoutMs: Long = 5_000L

    /** Timeout for a deviceless host command, in milliseconds. */
    public const val HostCommandTimeoutMs: Long = 3_000L

    /** Device discovery poll interval, in milliseconds. */
    public const val DiscoveryPollMs: Long = 2_000L

    /** Statistics emission interval, in milliseconds. */
    public const val StatsTickMs: Long = 1_000L

    /** Maximum number of stderr bytes kept in a [data.device.api.source.error.DeviceCommandException]. */
    public const val StderrTailBytes: Int = 2_048

    /** Consecutive empty polls before discovery escalates to a no-device state. */
    public const val EmptyPollEscalation: Int = 3
}
