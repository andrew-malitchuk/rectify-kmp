package data.device.api.source.parse

import data.device.api.source.model.AvdName

/**
 * Parses the stdout of `emulator -list-avds`.
 *
 * The tool interleaves `INFO |` / `WARNING |` log lines with the names; anything that is
 * not a valid AVD name is dropped rather than surfaced as a phantom emulator.
 *
 * @return One [AvdName] per listed image, in listed order.
 */
public fun parseAvdList(output: String): List<AvdName> = output.lineSequence()
    .map(String::trim)
    .mapNotNull(AvdName::of)
    .toList()
