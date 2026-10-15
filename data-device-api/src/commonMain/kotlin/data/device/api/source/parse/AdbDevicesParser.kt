package data.device.api.source.parse

import data.device.api.source.model.DeviceId
import data.device.api.source.model.DeviceResource
import data.device.api.source.model.DeviceStateResource

private val WHITESPACE = Regex("\\s+")

/** Tags `adb devices -l` appends; anything else holding a colon (a URL in an error hint) is ignored. */
private val KNOWN_TAGS = listOf("model", "product", "transport_id")

/**
 * Parses the stdout of `adb devices -l`.
 *
 * Non-device rows (unauthorized, offline, no permissions, recovery, sideload) are KEPT:
 * the user needs to see an attached-but-unusable device and why it is unusable.
 *
 * @param output Raw stdout, header line and daemon chatter included.
 * @return One [DeviceResource] per parsable row, in the order the tool listed them.
 */
public fun parseAdbDevices(output: String): List<DeviceResource> = output.lineSequence()
    .map(String::trim)
    .filter { it.isNotEmpty() }
    .filterNot { it.startsWith("List of devices") || it.startsWith("*") || it.startsWith("adb server") }
    .mapNotNull(::parseRow)
    .toList()

private fun parseRow(line: String): DeviceResource? {
    val parts = line.split(WHITESPACE)
    if (parts.size < 2) return null
    val id = DeviceId.of(parts[0]) ?: return null
    val rest = line.removePrefix(parts[0]).trim()
    return DeviceResource(
        id = id,
        state = parseState(rest),
        model = parts.tag("model"),
        product = parts.tag("product"),
        transport = parts.tag("transport_id"),
        isEmulator = parts[0].startsWith("emulator-"),
    )
}

/** `no permissions` is two words and carries a trailing hint, so match on the whole remainder. */
private fun parseState(rest: String): DeviceStateResource = when {
    rest.startsWith("no permissions") -> DeviceStateResource.NoPermissions
    else -> when (rest.split(WHITESPACE).first()) {
        "device" -> DeviceStateResource.Device
        "unauthorized" -> DeviceStateResource.Unauthorized
        "offline" -> DeviceStateResource.Offline
        "recovery" -> DeviceStateResource.Recovery
        "sideload" -> DeviceStateResource.Sideload
        else -> DeviceStateResource.Unknown
    }
}

private fun List<String>.tag(key: String): String? {
    require(key in KNOWN_TAGS)
    return firstOrNull { it.startsWith("$key:") }?.substringAfter(':')?.takeIf { it.isNotBlank() }
}
