package data.device.api.source.model

import kotlin.jvm.JvmInline

/**
 * A validated emulator image (AVD) name.
 *
 * Same reasoning as [DeviceId]: the boot command takes this type, so a free-text
 * name can never reach an argv.
 *
 * @property value Validated AVD name, e.g. `Pixel_7_API_34`.
 */
@JvmInline
public value class AvdName private constructor(public val value: String) {

    public companion object {

        private val ALLOWED: Regex = Regex("[A-Za-z0-9._-]+")

        /**
         * Validates [raw] as an AVD name.
         *
         * @return The name, or `null` when [raw] is blank, starts with a dash or holds
         *   a character outside `[A-Za-z0-9._-]`.
         */
        public fun of(raw: String): AvdName? = when {
            raw.isBlank() -> null
            raw.startsWith("-") -> null
            !ALLOWED.matches(raw) -> null
            else -> AvdName(raw)
        }
    }
}
