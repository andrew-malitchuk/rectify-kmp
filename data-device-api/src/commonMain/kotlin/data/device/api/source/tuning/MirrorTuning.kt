package data.device.api.source.tuning

/**
 * The one calibration surface for discovery and mirroring.
 *
 * Real hardware differs: a slow phone needs a higher capture floor, a busy host needs a
 * longer timeout. Every number here is named so it can be tuned in one place instead of
 * being rediscovered as a literal inside a loop.
 */
public object MirrorTuning {

    /**
     * Minimum delay between two captures, in milliseconds.
     *
     * STILL UNMEASURED, and deliberately left at its original value rather than quietly
     * tuned: no authorised device was reachable from the machine this was built on (the only
     * handset attached reported `unauthorized`, and the one installed AVD — an arm64
     * `android-tv` image — never left `offline`), so every candidate number would have been a
     * guess dressed up as a measurement.
     *
     * To settle it, with a device whose USB-debugging prompt has been accepted:
     * 1. `adb devices -l` — record the model, `wm size` for the resolution, and the link
     *    (USB 2, USB 3 or TCP).
     * 2. Run a 60 s session and read the achieved rate off `TransportStats.fps` and the
     *    round-trip off `TransportStats.captureRoundTripMs` — both are measured by the
     *    transport, not modelled. `TransportStats.latencyMs` is NOT the instrument here: the
     *    polling transport leaves it null because it cannot see glass-to-glass, which is what
     *    step 3 is for. Record mean and p95, and `droppedFrames` at the end.
     * 3. Wave test for end-to-end latency: run a stopwatch app on the device and photograph
     *    the device and the mirror together.
     * 4. Repeat once with the stage's dot grid covered by the frame and once with it visible,
     *    to settle whether the grid costs frame time (PRF-1).
     * 5. Set this floor just above the measured mean capture round-trip — above it the loop
     *    only burns CPU, below it the overrun counter inflates.
     */
    public const val CaptureFloorMs: Long = 100L

    /** Timeout for a single screen capture, in milliseconds. */
    public const val CaptureTimeoutMs: Long = 5_000L

    /** Timeout for a deviceless host command, in milliseconds. */
    public const val HostCommandTimeoutMs: Long = 3_000L

    /**
     * Watchdog for an explicit `adb start-server`, in milliseconds.
     *
     * This is a WATCHDOG, not a calibration. Unlike [CaptureFloorMs] — whose KDoc above
     * documents an unmeasured number and the hardware harness that would settle it — the cost
     * this one covers IS measured, and the number is not an estimate of it. It is an upper
     * bound on "something is wrong", deliberately far above the real cost so that no machine
     * has to be guessed at.
     *
     * The measurement, from `.pipeline/sdd/docs/error-handling-and-recovery/inputs/
     * reproduced-defect-adb-cold-start.md`: a cold `adb start-server` (each run preceded by
     * `adb kill-server`) took **3.03 s on all three runs, identical to the hundredth of a
     * second**. That reproducibility is the point — it is a fixed internal delay inside adb's
     * fork-server path, not variable work whose spread would have to be modelled. A warm
     * daemon answers in 0.01 s. So 15 s carries ~5x headroom over the only cost there is.
     *
     * **Do not tune this down toward 3.03 s.** The defect being fixed is exactly that:
     * [HostCommandTimeoutMs] is 3000 ms, the daemon start is 3030 ms, so the old budget missed
     * deterministically on every attempt and `destroyForcibly` killed the daemon mid-start —
     * a livelock, not slowness. Trimming the headroom here re-creates it on the first machine
     * that is 1% slower.
     */
    public const val DaemonStartTimeoutMs: Long = 15_000L

    /** Device discovery poll interval, in milliseconds. */
    public const val DiscoveryPollMs: Long = 2_000L

    /** Statistics emission interval, in milliseconds. */
    public const val StatsTickMs: Long = 1_000L

    /** Maximum number of stderr bytes kept in a [data.device.api.source.error.DeviceCommandException]. */
    public const val StderrTailBytes: Int = 2_048

    /** Consecutive empty polls before discovery escalates to a no-device state. */
    public const val EmptyPollEscalation: Int = 3
}
