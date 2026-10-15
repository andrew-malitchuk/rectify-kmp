package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * Outcome of device discovery.
 *
 * Every state here is a SUCCESS value: a missing tool or an empty device list is
 * information to render, never an exception to map into a `Failure`.
 */
public sealed class DiscoveryModel : Model {

    /** First poll has not completed yet. */
    public data object Detecting : DiscoveryModel()

    /**
     * The platform tool could not be found on this machine.
     *
     * @property tool Binary name that was looked for, e.g. `adb`.
     * @property probedPaths Every candidate path tried, in probe order.
     * @property installCommand Exact shell line that installs the tool.
     * @property envHint Environment variable hint shown alongside the install command.
     */
    public data class ToolMissing(
        val tool: String,
        val probedPaths: List<String>,
        val installCommand: String,
        val envHint: String,
    ) : DiscoveryModel()

    /**
     * The tool works but no usable device showed up.
     *
     * @property toolVersion Version of the tool that answered.
     * @property lastPollEpochMs Wall-clock time of the last completed poll.
     * @property attempts Consecutive empty polls so far.
     * @property attachedCount Devices seen but not usable, e.g. unauthorized ones.
     */
    public data class NoDeviceFound(
        val toolVersion: String,
        val lastPollEpochMs: Long,
        val attempts: Int,
        val attachedCount: Int,
    ) : DiscoveryModel()

    /**
     * A poll could not be completed because the tool itself failed or timed out.
     *
     * Deliberately NOT [NoDeviceFound]: that state means we looked and nothing was attached,
     * this one means we learned nothing at all. Collapsing the two would tell a user holding
     * a connected phone that there is no device, and would restart the empty-poll escalation
     * on every adb hiccup. The next poll retries on its normal cadence.
     *
     * @property tool Binary name that failed, e.g. `adb`.
     * @property reason Tool's own words for the failure - stderr tail or a timeout note.
     */
    public data class PollFailed(
        val tool: String,
        val reason: String,
    ) : DiscoveryModel()

    /**
     * At least one device or configured emulator is known.
     *
     * @property physical Attached devices, usable or not.
     * @property emulators Configured AVDs, running or not.
     */
    public data class Found(
        val physical: List<DeviceModel>,
        val emulators: List<EmulatorModel>,
    ) : DiscoveryModel()
}
