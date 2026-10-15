package data.device.api.source.model

/**
 * The closed set of device-scoped commands this app may run.
 *
 * There is deliberately no vararg runner, no free-text command, no `scrcpy` and no
 * `connect`/`-H`/`-P` member: adding a capability has to be a visible API change here.
 */
public sealed interface DeviceCommand {

    /** Capture the screen as PNG on stdout. */
    public data object Screencap : DeviceCommand

    /** Read the display resolution. */
    public data object WmSize : DeviceCommand

    /** Read the display density. */
    public data object WmDensity : DeviceCommand

    /**
     * Read one system property.
     *
     * @property name Property name, e.g. `ro.product.model`.
     */
    public data class GetProp(public val name: String) : DeviceCommand

    /** Ask a running emulator for its AVD name. */
    public data object EmuAvdName : DeviceCommand
}
