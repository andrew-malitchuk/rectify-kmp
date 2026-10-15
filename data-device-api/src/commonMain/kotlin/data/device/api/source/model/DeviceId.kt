package data.device.api.source.model

import kotlin.jvm.JvmInline

/**
 * A resolved device identifier.
 *
 * The constructor is private on purpose: every device-scoped command is addressed by a
 * [DeviceId], so a caller cannot express a command for an unvalidated or absent device.
 * [of] is the only mint point.
 *
 * @property value Validated tool serial, e.g. `emulator-5554`.
 */
@JvmInline
public value class DeviceId private constructor(public val value: String) {

    public companion object {

        /** Serials are ASCII identifiers; anything else is a parse error, not a device. */
        private val ALLOWED: Regex = Regex("[A-Za-z0-9._:-]+")

        /**
         * Validates [raw] as a device serial.
         *
         * Rejects blank input, anything holding whitespace, a leading dash (it would be
         * read as a flag by the tool) and any character outside `[A-Za-z0-9._:-]`.
         *
         * @return The identifier, or `null` when [raw] is not a usable serial.
         */
        public fun of(raw: String): DeviceId? = when {
            raw.isBlank() -> null
            raw.startsWith("-") -> null
            !ALLOWED.matches(raw) -> null
            else -> DeviceId(raw)
        }
    }
}
