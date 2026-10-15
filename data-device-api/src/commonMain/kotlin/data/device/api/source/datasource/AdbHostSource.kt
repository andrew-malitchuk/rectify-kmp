package data.device.api.source.datasource

import data.device.api.source.model.DeviceResource
import data.device.api.source.model.ToolProbe

/**
 * The deviceless half of `adb`, enumerated explicitly so that "this command has no
 * device" stays an auditable exception rather than a nullable parameter on
 * [DeviceCommandExecutor].
 */
public interface AdbHostSource {

    /**
     * Locates `adb` and reads its version.
     *
     * A missing binary yields `ToolProbe(found = false, …)`, never an exception.
     */
    public suspend fun probe(): ToolProbe

    /** Lists attached devices, including the ones that are not usable. */
    public suspend fun listDevices(): List<DeviceResource>

    /**
     * Forks the adb daemon and waits for it to be serving.
     *
     * Separate from [listDevices] because it needs its own, far longer budget: a cold fork
     * costs a measured, fixed 3.03 s, so letting a discovery poll trigger it implicitly means
     * the poll's 3 s budget expires mid-fork and kills the daemon it just started — the
     * livelock this member exists to break. Idempotent and free against a healthy running
     * daemon (0.00 s, same pid), but it does NOT recover a *wedged* one: it hangs there, so it
     * is not safe to call unconditionally ahead of every poll.
     *
     * @throws data.device.api.source.error.DeviceCommandException when `adb` is missing, the
     *   daemon start exceeds its watchdog, or the process exits non-zero.
     */
    public suspend fun startServer()

    /**
     * Kills the adb daemon.
     *
     * The repair half of `kill-server` then [startServer], which is the only sequence that
     * clears a daemon that is alive but not serving. Budgeted like any other deviceless
     * command — closing a local socket is not a fork.
     *
     * @throws data.device.api.source.error.DeviceCommandException when `adb` is missing, the
     *   call exceeds its budget, or the process exits non-zero.
     */
    public suspend fun killServer()
}
