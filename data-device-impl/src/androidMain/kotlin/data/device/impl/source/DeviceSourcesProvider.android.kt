package data.device.impl.source

import org.koin.core.module.Module

/**
 * Android implementation of [provideDeviceSources].
 *
 * Both mobile targets register the SAME no-op graph, so the body lives once in
 * [provideUnsupportedDeviceSources] rather than being copied per actual — see its KDoc for
 * why that duplication was a launch-crash hazard rather than a style preference.
 */
internal actual fun Module.provideDeviceSources() {
    provideUnsupportedDeviceSources()
}
